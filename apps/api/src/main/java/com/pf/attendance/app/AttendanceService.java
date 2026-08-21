package com.pf.attendance.app;

import com.pf.attendance.domain.DailyHoursCalculator;
import com.pf.attendance.domain.DailySummary;
import com.pf.attendance.domain.MonthSummary;
import com.pf.attendance.domain.PeriodClosedException;
import com.pf.attendance.domain.PunchEvent;
import com.pf.attendance.domain.PunchRules;
import com.pf.attendance.domain.PunchType;
import com.pf.attendance.domain.WorkDates;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import org.springframework.stereotype.Service;

@Service
public class AttendanceService {
  private final EmployeeStore employees;
  private final PunchStore punches;
  private final WorkflowStore workflow;
  private final Clock clock;

  public AttendanceService(
      EmployeeStore employees, PunchStore punches, WorkflowStore workflow, Clock clock) {
    this.employees = employees;
    this.punches = punches;
    this.workflow = workflow;
    this.clock = clock;
  }

  public Employee requireByOrgAndSub(String orgId, String sub) {
    return employees
        .findByOrgIdAndSub(orgId, sub)
        .orElseThrow(() -> new UnknownEmployeeException(sub));
  }

  public PunchEvent punch(String employeeId, PunchType type) {
    Employee employee =
        employees
            .findById(employeeId)
            .orElseThrow(() -> new UnknownEmployeeException(employeeId));
    Instant now = Instant.now(clock);
    LocalDate workDate = WorkDates.of(now);
    if (workflow.isMonthClosed(employee.orgId(), YearMonth.from(workDate))) {
      throw new PeriodClosedException("month is closed");
    }
    List<PunchEvent> existing = punches.findByEmployeeAndWorkDate(employee.id(), workDate);
    PunchRules.assertAllowed(existing, type);
    PunchEvent event =
        new PunchEvent(Ids.ulid(), employee.id(), type, now, workDate, "web");
    punches.append(event);
    return event;
  }

  public DailySummary dailySummary(String employeeId, LocalDate workDate) {
    if (employees.findById(employeeId).isEmpty()) {
      throw new UnknownEmployeeException(employeeId);
    }
    List<PunchEvent> events = punches.findByEmployeeAndWorkDate(employeeId, workDate);
    Instant asOf = WorkDates.of(Instant.now(clock)).equals(workDate) ? Instant.now(clock) : null;
    return DailyHoursCalculator.compute(workDate, events, asOf);
  }

  public MonthSummary monthSummary(String employeeId, YearMonth month) {
    if (employees.findById(employeeId).isEmpty()) {
      throw new UnknownEmployeeException(employeeId);
    }
    List<DailySummary> days = new ArrayList<>();
    LocalDate cursor = month.atDay(1);
    LocalDate last = month.atEndOfMonth();
    while (!cursor.isAfter(last)) {
      days.add(dailySummary(employeeId, cursor));
      cursor = cursor.plusDays(1);
    }
    return new MonthSummary(month, List.copyOf(days));
  }

  public WorkRequest submitRequest(String employeeId, String type, LocalDate workDate, String reason) {
    Employee employee =
        employees.findById(employeeId).orElseThrow(() -> new UnknownEmployeeException(employeeId));
    if (!WorkRequest.LEAVE.equals(type) && !WorkRequest.PUNCH_CORRECTION.equals(type)) {
      throw new IllegalArgumentException("type must be leave or punch_correction");
    }
    if (reason == null || reason.isBlank()) {
      throw new IllegalArgumentException("reason is required");
    }
    if (workflow.isMonthClosed(employee.orgId(), YearMonth.from(workDate))) {
      throw new PeriodClosedException("month is closed");
    }
    WorkRequest row =
        new WorkRequest(
            Ids.ulid(),
            employeeId,
            type,
            WorkRequest.PENDING,
            workDate,
            reason.trim(),
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
    if (workflow.isMonthClosed(employee.orgId(), YearMonth.from(workDate))) {
      throw new PeriodClosedException("month is closed");
    }
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
    requireManager(actor);
    StringJoiner lines = new StringJoiner("\n");
    // minutes-v1 prefix unchanged for P16; Stage A SES columns appended.
    lines.add(
        "sub,displayName,workDate,workMinutes,breakMinutes,status,engagement,worksiteCode,worksiteName");
    for (Employee employee : employees.findAllByOrgId(actor.orgId())) {
      MonthSummary summary = monthSummary(employee.id(), month);
      for (DailySummary day : summary.days()) {
        lines.add(
            String.join(
                ",",
                employee.sub(),
                csv(employee.displayName()),
                day.workDate().toString(),
                Integer.toString(day.workMinutes()),
                Integer.toString(day.breakMinutes()),
                day.status().name().toLowerCase(),
                employee.engagement(),
                csv(employee.worksiteCode()),
                csv(employee.worksiteName())));
      }
    }
    return lines.toString() + "\n";
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
