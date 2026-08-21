package com.pf.attendance.app;

import com.pf.attendance.app.export.CsvColumn;
import com.pf.attendance.app.export.CsvExportProfile;
import com.pf.attendance.app.export.CsvExportProfiles;
import com.pf.attendance.app.export.CsvExporter;
import com.pf.attendance.app.export.PdfTimesheetRenderer;
import com.pf.attendance.app.export.VendorCsvFormats;
import com.pf.attendance.app.handoff.HandoffCsv;
import com.pf.attendance.app.handoff.HandoffDayLine;
import com.pf.attendance.app.handoff.HandoffReceipt;
import com.pf.attendance.app.handoff.TimesheetHandoffPort;
import com.pf.attendance.app.worksite.CrossOrgAssignment;
import com.pf.attendance.app.worksite.VisibleMember;
import com.pf.attendance.app.worksite.WorksiteVisibilityStore;
import com.pf.attendance.domain.AttendancePeriods;
import com.pf.attendance.domain.DailyHoursCalculator;
import com.pf.attendance.domain.DailySummary;
import com.pf.attendance.domain.MonthSummary;
import com.pf.attendance.domain.PeriodClosedException;
import com.pf.attendance.domain.PunchEvent;
import com.pf.attendance.domain.PunchRules;
import com.pf.attendance.domain.PunchState;
import com.pf.attendance.domain.PunchType;
import com.pf.attendance.domain.WorkDates;
import com.pf.attendance.domain.ScheduleVariance;
import com.pf.attendance.domain.WorkSchedule;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class AttendanceService {
  private final EmployeeStore employees;
  private final PunchStore punches;
  private final WorkflowStore workflow;
  private final TimesheetHandoffPort handoff;
  private final WorksiteVisibilityStore visibility;
  private final OrgSettingsStore orgSettings;
  private final ProvisionalDayStore provisionals;
  private final Clock clock;

  public AttendanceService(
      EmployeeStore employees,
      PunchStore punches,
      WorkflowStore workflow,
      TimesheetHandoffPort handoff,
      WorksiteVisibilityStore visibility,
      OrgSettingsStore orgSettings,
      ProvisionalDayStore provisionals,
      Clock clock) {
    this.employees = employees;
    this.punches = punches;
    this.workflow = workflow;
    this.handoff = handoff;
    this.visibility = visibility;
    this.orgSettings = orgSettings;
    this.provisionals = provisionals;
    this.clock = clock;
  }

  public Employee requireByOrgAndSub(String orgId, String sub) {
    return employees
        .findByOrgIdAndSub(orgId, sub)
        .orElseThrow(() -> new UnknownEmployeeException(sub));
  }

  public PunchEvent punch(String employeeId, PunchType type) {
    return punch(employeeId, type, null, null);
  }

  /**
   * Live punch when workDate/at are null. Manual / forgotten punch when workDate is set; at null
   * uses org schedule defaults for clock_in / clock_out.
   */
  public PunchEvent punch(String employeeId, PunchType type, LocalDate workDate, LocalTime at) {
    Employee employee =
        employees
            .findById(employeeId)
            .orElseThrow(() -> new UnknownEmployeeException(employeeId));
    Instant now = Instant.now(clock);
    LocalDate day = workDate == null ? WorkDates.of(now) : workDate;
    assertPeriodOpen(employee.orgId(), day);
    if (day.isAfter(WorkDates.of(now))) {
      throw new IllegalArgumentException("cannot punch a future workDate");
    }
    WorkSchedule schedule = orgSettings.getOrDefault(employee.orgId()).workSchedule();
    LocalTime time = at;
    if (time == null && workDate != null) {
      time =
          switch (type) {
            case CLOCK_IN -> schedule.scheduledStart();
            case CLOCK_OUT -> schedule.scheduledEnd();
            case BREAK_START -> schedule.breakStart();
            case BREAK_END -> schedule.breakEnd();
          };
    }
    Instant stamped =
        workDate == null
            ? now
            : day.atTime(time).atZone(WorkDates.ZONE).toInstant();
    List<PunchEvent> existing = punches.findByEmployeeAndWorkDate(employee.id(), day);
    PunchRules.assertAllowed(existing, type);
    String source = workDate == null ? "web" : "manual";
    PunchEvent event = new PunchEvent(Ids.ulid(), employee.id(), type, stamped, day, source);
    punches.append(event);
    return event;
  }

  /** Fill a full day from org schedule when the day has no punches yet. */
  public List<PunchEvent> applyScheduleDay(String employeeId, LocalDate workDate) {
    Employee employee =
        employees.findById(employeeId).orElseThrow(() -> new UnknownEmployeeException(employeeId));
    assertPeriodOpen(employee.orgId(), workDate);
    if (workDate.isAfter(WorkDates.of(Instant.now(clock)))) {
      throw new IllegalArgumentException("cannot apply schedule to a future workDate");
    }
    if (!punches.findByEmployeeAndWorkDate(employeeId, workDate).isEmpty()) {
      throw new IllegalArgumentException("day already has punches");
    }
    WorkSchedule schedule = orgSettings.getOrDefault(employee.orgId()).workSchedule();
    List<PunchEvent> created = new ArrayList<>();
    created.add(punch(employeeId, PunchType.CLOCK_IN, workDate, schedule.scheduledStart()));
    if (schedule.breakMinutes() > 0) {
      created.add(punch(employeeId, PunchType.BREAK_START, workDate, schedule.breakStart()));
      created.add(punch(employeeId, PunchType.BREAK_END, workDate, schedule.breakEnd()));
    }
    created.add(punch(employeeId, PunchType.CLOCK_OUT, workDate, schedule.scheduledEnd()));
    return List.copyOf(created);
  }

  public DailySummary dailySummary(String employeeId, LocalDate workDate) {
    Employee employee =
        employees.findById(employeeId).orElseThrow(() -> new UnknownEmployeeException(employeeId));
    WorkSchedule schedule = orgSettings.getOrDefault(employee.orgId()).workSchedule();
    List<PunchEvent> events = punches.findByEmployeeAndWorkDate(employeeId, workDate);
    Instant asOf = WorkDates.of(Instant.now(clock)).equals(workDate) ? Instant.now(clock) : null;
    DailySummary computed = DailyHoursCalculator.compute(workDate, events, asOf);
    ScheduleVariance.Result variance =
        ScheduleVariance.compute(schedule, events, computed.workMinutes());

    Optional<WorkRequest> approvedLeave =
        workflow.listRequestsForEmployee(employeeId).stream()
            .filter(r -> WorkRequest.LEAVE.equals(r.type()))
            .filter(r -> WorkRequest.APPROVED.equals(r.status()))
            .filter(r -> r.workDate().equals(workDate))
            .findFirst();

    if (!events.isEmpty()) {
      return new DailySummary(
          workDate,
          computed.workMinutes(),
          computed.breakMinutes(),
          computed.status(),
          events,
          false,
          approvedLeave.map(WorkRequest::leaveKind).orElse(""),
          variance.lateMinutes(),
          variance.earlyLeaveMinutes(),
          variance.overtimeMinutes());
    }

    if (approvedLeave.isPresent()) {
      String kind = approvedLeave.get().leaveKind();
      int net = schedule.scheduledNetMinutes();
      int work =
          switch (kind) {
            case LeaveKind.ABSENCE -> 0;
            case LeaveKind.AM_HALF, LeaveKind.PM_HALF -> net / 2;
            default -> net; // paid
          };
      int brk =
          LeaveKind.ABSENCE.equals(kind)
              ? 0
              : LeaveKind.isHalf(kind) ? schedule.breakMinutes() / 2 : schedule.breakMinutes();
      return new DailySummary(
          workDate, work, brk, PunchState.ON_LEAVE, List.of(), false, kind, 0, 0, 0);
    }

    Optional<ProvisionalDay> provisional = provisionals.find(employeeId, workDate);
    if (provisional.isPresent()) {
      ProvisionalDay p = provisional.get();
      return new DailySummary(
          workDate,
          p.workMinutes(),
          p.breakMinutes(),
          PunchState.PROVISIONAL,
          List.of(),
          true,
          "",
          0,
          0,
          0);
    }
    return new DailySummary(
        workDate,
        computed.workMinutes(),
        computed.breakMinutes(),
        computed.status(),
        List.of(),
        false,
        "",
        0,
        0,
        0);
  }

  public MonthSummary monthSummary(String employeeId, YearMonth month) {
    Employee employee =
        employees.findById(employeeId).orElseThrow(() -> new UnknownEmployeeException(employeeId));
    int anchor = orgSettings.getOrDefault(employee.orgId()).periodAnchorDay();
    List<DailySummary> days = new ArrayList<>();
    LocalDate cursor = AttendancePeriods.startInclusive(month, anchor);
    LocalDate last = AttendancePeriods.endInclusive(month, anchor);
    while (!cursor.isAfter(last)) {
      days.add(dailySummary(employeeId, cursor));
      cursor = cursor.plusDays(1);
    }
    return new MonthSummary(month, List.copyOf(days));
  }

  public WorkRequest submitRequest(
      String employeeId, String type, LocalDate workDate, String reason) {
    return submitRequest(employeeId, type, workDate, reason, null);
  }

  public WorkRequest submitRequest(
      String employeeId, String type, LocalDate workDate, String reason, String leaveKind) {
    Employee employee =
        employees.findById(employeeId).orElseThrow(() -> new UnknownEmployeeException(employeeId));
    if (!WorkRequest.LEAVE.equals(type) && !WorkRequest.PUNCH_CORRECTION.equals(type)) {
      throw new IllegalArgumentException("type must be leave or punch_correction");
    }
    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException("reason is required");
    }
    assertPeriodOpen(employee.orgId(), workDate);
    String kind = LeaveKind.normalize(type, leaveKind);
    WorkRequest row =
        new WorkRequest(
            Ids.ulid(),
            employeeId,
            type,
            WorkRequest.PENDING,
            workDate,
            reason.trim(),
            kind,
            Instant.now(clock),
            null,
            null);
    workflow.saveRequest(row);
    return row;
  }

  public List<WorkRequest> myRequests(String employeeId) {
    return workflow.listRequestsForEmployee(employeeId);
  }

  public List<WorkRequest> inbox(Employee actor) {
    requireManager(actor);
    List<WorkRequest> out = new ArrayList<>();
    for (WorkRequest row : workflow.listPending()) {
      Optional<Employee> subject = employees.findById(row.employeeId());
      if (subject.isPresent() && actor.orgId().equals(subject.get().orgId())) {
        out.add(row);
      }
    }
    return List.copyOf(out);
  }

  public WorkRequest decide(Employee actor, String requestId, boolean approve) {
    requireManager(actor);
    WorkRequest existing =
        workflow.findRequest(requestId).orElseThrow(() -> new IllegalArgumentException("request not found"));
    Employee subject =
        employees
            .findById(existing.employeeId())
            .orElseThrow(() -> new IllegalArgumentException("request not found"));
    if (!actor.orgId().equals(subject.orgId())) {
      throw new ForbiddenException("cross-org request");
    }
    if (!WorkRequest.PENDING.equals(existing.status())) {
      throw new IllegalArgumentException("request already decided");
    }
    WorkRequest next =
        new WorkRequest(
            existing.id(),
            existing.employeeId(),
            existing.type(),
            approve ? WorkRequest.APPROVED : WorkRequest.REJECTED,
            existing.workDate(),
            existing.reason(),
            existing.leaveKind(),
            existing.createdAt(),
            Instant.now(clock),
            actor.sub());
    workflow.saveRequest(next);
    return next;
  }

  public TimeAllocation allocate(String employeeId, LocalDate workDate, String project, int minutes) {
    Employee employee =
        employees.findById(employeeId).orElseThrow(() -> new UnknownEmployeeException(employeeId));
    if (project == null || project.isBlank()) {
      throw new IllegalArgumentException("project is required");
    }
    if (minutes <= 0) {
      throw new IllegalArgumentException("minutes must be positive");
    }
    assertPeriodOpen(employee.orgId(), workDate);
    int used = workflow.listAllocations(employeeId, workDate).stream().mapToInt(TimeAllocation::minutes).sum();
    int work = dailySummary(employeeId, workDate).workMinutes();
    if (used + minutes > work) {
      throw new IllegalArgumentException("allocations exceed work minutes");
    }
    TimeAllocation row = new TimeAllocation(Ids.ulid(), employeeId, workDate, project.trim(), minutes);
    workflow.saveAllocation(row);
    return row;
  }

  public List<TimeAllocation> allocations(String employeeId, LocalDate workDate) {
    return workflow.listAllocations(employeeId, workDate);
  }

  public void closeMonth(Employee actor, YearMonth month) {
    requireManager(actor);
    if (workflow.isMonthClosed(actor.orgId(), month)) {
      throw new PeriodClosedException("month already closed");
    }
    workflow.closeMonth(actor.orgId(), month, actor.sub());
  }

  public boolean isMonthClosed(Employee actor, YearMonth month) {
    return workflow.isMonthClosed(actor.orgId(), month);
  }

  public String monthCsv(Employee actor, YearMonth month) {
    return monthCsv(actor, month, null, null, null);
  }

  public String monthCsv(
      Employee actor,
      YearMonth month,
      String profileId,
      Boolean includeHeader,
      List<String> columnsOverride) {
    requireManager(actor);
    OrgPeriodSettings settings = orgSettings.getOrDefault(actor.orgId());
    String profile =
        profileId == null || profileId.isBlank() ? settings.csvProfileId() : profileId.trim();
    List<Employee> orgEmployees = employees.findAllByOrgId(actor.orgId());

    if (VendorCsvFormats.MF_ATTENDANCE_PUNCH_V1.equals(profile)) {
      return VendorCsvFormats.moneyForwardPunchCsv(
          orgEmployees,
          month,
          settings.periodAnchorDay(),
          punches::findByEmployeeAndWorkDate);
    }
    if (VendorCsvFormats.FREEE_HR_MONTHLY_V1.equals(profile)) {
      List<MonthSummary> summaries = new ArrayList<>();
      List<Integer> paidLeaveDays = new ArrayList<>();
      for (Employee employee : orgEmployees) {
        summaries.add(monthSummary(employee.id(), month));
        paidLeaveDays.add(countApprovedPaidLeaveDays(employee.id(), month));
      }
      return VendorCsvFormats.freeeHrMonthlyCsv(orgEmployees, month, summaries, paidLeaveDays);
    }

    boolean header = includeHeader == null ? settings.csvIncludeHeader() : includeHeader;
    List<CsvColumn> cols =
        columnsOverride == null || columnsOverride.isEmpty()
            ? CsvColumn.parseList(settings.csvColumns())
            : CsvColumn.parseList(columnsOverride);
    CsvExportProfile resolved = CsvExportProfiles.resolve(profile, header, cols);
    List<CsvExporter.EmployeeDayRow> rows = new ArrayList<>();
    for (Employee employee : orgEmployees) {
      rows.addAll(CsvExporter.flatten(employee, monthSummary(employee.id(), month).days()));
    }
    return CsvExporter.render(resolved, rows);
  }

  public byte[] monthPdf(Employee actor, YearMonth month, String employeeSub) {
    requireManager(actor);
    String disclaimer =
        "Demo timesheet for print/sign workflows. Not a legal attendance record. No yen/tax.";
    List<PdfTimesheetRenderer.EmployeeSheet> sheets = new ArrayList<>();
    if (employeeSub != null && !employeeSub.isBlank()) {
      Employee target =
          employees
              .findByOrgIdAndSub(actor.orgId(), employeeSub.trim())
              .orElseThrow(() -> new UnknownEmployeeException(employeeSub));
      sheets.add(new PdfTimesheetRenderer.EmployeeSheet(target, monthSummary(target.id(), month)));
    } else {
      for (Employee employee : employees.findAllByOrgId(actor.orgId())) {
        sheets.add(new PdfTimesheetRenderer.EmployeeSheet(employee, monthSummary(employee.id(), month)));
      }
    }
    return PdfTimesheetRenderer.renderAll(sheets, disclaimer);
  }

  public ProvisionalDay putProvisional(
      Employee actor, LocalDate workDate, int workMinutes, int breakMinutes, String note) {
    OrgPeriodSettings settings = orgSettings.getOrDefault(actor.orgId());
    if (settings.closeByDay() <= 0) {
      throw new IllegalArgumentException("closeByDay is not configured; set org period-settings first");
    }
    if (workDate.getDayOfMonth() <= settings.closeByDay()) {
      throw new IllegalArgumentException(
          "provisional days are only for dates after closeByDay=" + settings.closeByDay());
    }
    assertPeriodOpen(actor.orgId(), workDate);
    if (workMinutes < 0 || breakMinutes < 0) {
      throw new IllegalArgumentException("minutes must be >= 0");
    }
    if (!punches.findByEmployeeAndWorkDate(actor.id(), workDate).isEmpty()) {
      throw new IllegalArgumentException("real punches already exist for this date");
    }
    ProvisionalDay row =
        new ProvisionalDay(
            actor.id(),
            workDate,
            workMinutes,
            breakMinutes,
            note == null ? "" : note.trim());
    provisionals.save(row);
    return row;
  }

  public List<Employee> unpunched(Employee actor, LocalDate workDate) {
    List<Employee> missing = new ArrayList<>();
    for (Employee employee : employees.findAllByOrgId(actor.orgId())) {
      if (punches.findByEmployeeAndWorkDate(employee.id(), workDate).isEmpty()) {
        missing.add(employee);
      }
    }
    return List.copyOf(missing);
  }

  /**
   * Build worksite→employer handoff CSV from local punches of {@code client_site} employees.
   * Demo stand-in for "worksite approved timesheet package".
   */
  public String exportHandoffCsv(Employee actor, YearMonth month) {
    requireManager(actor);
    List<HandoffDayLine> lines = new ArrayList<>();
    for (Employee employee : employees.findAllByOrgId(actor.orgId())) {
      if (!Engagement.CLIENT_SITE.equals(employee.engagement())) {
        continue;
      }
      MonthSummary summary = monthSummary(employee.id(), month);
      for (DailySummary day : summary.days()) {
        if (day.workMinutes() == 0 && day.breakMinutes() == 0 && day.punches().isEmpty()) {
          continue;
        }
        lines.add(
            new HandoffDayLine(
                employee.sub(),
                employee.worksiteCode(),
                employee.worksiteName(),
                day.workDate().toString(),
                day.workMinutes(),
                day.breakMinutes(),
                day.status().name().toLowerCase()));
      }
    }
    return HandoffCsv.format(lines);
  }

  /** Accept employer-side handoff CSV. Validates subs are client_site in this org. Does not create punches. */
  public HandoffReceipt ingestHandoffCsv(Employee actor, YearMonth month, String csvBody, String sourceHint) {
    requireManager(actor);
    List<HandoffDayLine> parsed = HandoffCsv.parse(csvBody);
    for (HandoffDayLine line : parsed) {
      Employee target =
          employees
              .findByOrgIdAndSub(actor.orgId(), line.sub())
              .orElseThrow(() -> new IllegalArgumentException("unknown sub in handoff: " + line.sub()));
      if (!Engagement.CLIENT_SITE.equals(target.engagement())) {
        throw new IllegalArgumentException("handoff sub is not client_site: " + line.sub());
      }
    }
    HandoffReceipt receipt =
        new HandoffReceipt(
            Ids.ulid(),
            actor.orgId(),
            month,
            sourceHint == null || sourceHint.isBlank() ? "csv-upload" : sourceHint.trim(),
            Instant.now(clock),
            actor.sub(),
            parsed);
    return handoff.accept(receipt);
  }

  public List<HandoffReceipt> listHandoffs(Employee actor, YearMonth month) {
    requireManager(actor);
    return handoff.list(actor.orgId(), month);
  }

  /**
   * Worksite team board: local employees + cross-org guests (read-only). Guests are not in this
   * org's employee table, so month CSV / payroll export cannot include them.
   */
  public List<VisibleMember> listVisibleMembers(Employee actor) {
    List<VisibleMember> out = new ArrayList<>();
    for (Employee employee : employees.findAllByOrgId(actor.orgId())) {
      out.add(
          new VisibleMember(
              employee.sub(),
              employee.displayName(),
              employee.role(),
              VisibleMember.KIND_LOCAL,
              actor.orgId(),
              employee.worksiteCode(),
              employee.worksiteName()));
    }
    for (CrossOrgAssignment a : visibility.findByWorksiteOrgId(actor.orgId())) {
      String displayName =
          employees
              .findByOrgIdAndSub(a.employerOrgId(), a.employeeSub())
              .map(Employee::displayName)
              .orElse(a.employeeSub());
      out.add(
          new VisibleMember(
              a.employeeSub(),
              displayName,
              "guest",
              VisibleMember.KIND_GUEST,
              a.employerOrgId(),
              a.worksiteCode(),
              a.worksiteName()));
    }
    return List.copyOf(out);
  }

  public OrgPeriodSettings getPeriodSettings(Employee actor) {
    return orgSettings.getOrDefault(actor.orgId());
  }

  public OrgPeriodSettings putPeriodSettings(Employee actor, int periodAnchorDay) {
    OrgPeriodSettings cur = orgSettings.getOrDefault(actor.orgId());
    return putPeriodSettings(
        actor,
        new OrgPeriodSettings(
            actor.orgId(),
            periodAnchorDay,
            cur.closeByDay(),
            cur.csvProfileId(),
            cur.csvIncludeHeader(),
            cur.csvColumns(),
            cur.scheduledStart(),
            cur.scheduledEnd(),
            cur.breakMinutes(),
            cur.breakMode()));
  }

  public OrgPeriodSettings putPeriodSettings(Employee actor, OrgPeriodSettings next) {
    requireManager(actor);
    if (!actor.orgId().equals(next.orgId())) {
      throw new ForbiddenException("org mismatch");
    }
    orgSettings.save(next);
    return next;
  }

  private int countApprovedPaidLeaveDays(String employeeId, YearMonth month) {
    Employee employee =
        employees.findById(employeeId).orElseThrow(() -> new UnknownEmployeeException(employeeId));
    int anchor = orgSettings.getOrDefault(employee.orgId()).periodAnchorDay();
    LocalDate start = AttendancePeriods.startInclusive(month, anchor);
    LocalDate end = AttendancePeriods.endInclusive(month, anchor);
    int count = 0;
    for (WorkRequest row : workflow.listRequestsForEmployee(employeeId)) {
      if (!WorkRequest.LEAVE.equals(row.type()) || !WorkRequest.APPROVED.equals(row.status())) {
        continue;
      }
      if (row.workDate().isBefore(start) || row.workDate().isAfter(end)) {
        continue;
      }
      if (LeaveKind.PAID.equals(row.leaveKind())) {
        count++;
      }
    }
    return count;
  }

  private void assertPeriodOpen(String orgId, LocalDate workDate) {
    int anchor = orgSettings.getOrDefault(orgId).periodAnchorDay();
    YearMonth period = AttendancePeriods.periodContaining(workDate, anchor);
    if (workflow.isMonthClosed(orgId, period)) {
      throw new PeriodClosedException("month is closed");
    }
  }

  private static void requireManager(Employee actor) {
    if (!"manager".equals(actor.role())) {
      throw new ForbiddenException("manager role required");
    }
  }

  private static String csv(String value) {
    if (value.contains(",") || value.contains("\"")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }
}
