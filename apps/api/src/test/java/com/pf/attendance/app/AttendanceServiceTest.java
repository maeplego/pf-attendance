package com.pf.attendance.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.pf.attendance.app.handoff.MockTimesheetHandoffAdapter;
import com.pf.attendance.app.worksite.CrossOrgAssignment;
import com.pf.attendance.app.worksite.MemoryWorksiteVisibilityStore;
import com.pf.attendance.app.worksite.VisibleMember;
import com.pf.attendance.domain.DailySummary;
import com.pf.attendance.domain.MonthSummary;
import com.pf.attendance.domain.PunchConflictException;
import com.pf.attendance.domain.PunchEvent;
import com.pf.attendance.domain.PunchType;
import com.pf.attendance.domain.WorkDates;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AttendanceServiceTest {
  private MemoryEmployeeStore employees;
  private MemoryPunchStore punches;
  private MemoryWorkflowStore workflow;
  private MockTimesheetHandoffAdapter handoff;
  private MemoryWorksiteVisibilityStore visibility;
  private MemoryOrgSettingsStore orgSettings;
  private MutableClock clock;
  private AttendanceService service;
  private Employee aoki;
  private Employee sato;
  private Employee ise;

  @BeforeEach
  void setUp() {
    employees = new MemoryEmployeeStore();
    punches = new MemoryPunchStore();
    workflow = new MemoryWorkflowStore();
    handoff = new MockTimesheetHandoffAdapter();
    visibility = new MemoryWorksiteVisibilityStore();
    orgSettings = new MemoryOrgSettingsStore();
    MemoryProvisionalDayStore provisionals = new MemoryProvisionalDayStore();
    clock = new MutableClock(Instant.parse("2026-08-19T00:00:00Z"));
    for (Employee employee : DemoEmployees.roster()) {
      employees.save(employee);
    }
    for (Employee employee : DemoEmployees.worksiteHostRoster(DemoEmployees.ORG_B)) {
      employees.save(employee);
    }
    visibility.save(
        new CrossOrgAssignment(
            "asgn1",
            DemoEmployees.ORG_A,
            DemoEmployees.ORG_B,
            "ise.yuto",
            "WS-CLIENT-A",
            "架空商事 本社開発"));
    aoki = employees.findByOrgIdAndSub(DemoEmployees.ORG_A, "aoki.haru").orElseThrow();
    sato = employees.findByOrgIdAndSub(DemoEmployees.ORG_A, "sato.mei").orElseThrow();
    ise = employees.findByOrgIdAndSub(DemoEmployees.ORG_A, "ise.yuto").orElseThrow();
    service =
        new AttendanceService(
            employees, punches, workflow, handoff, visibility, orgSettings, provisionals, clock);
  }

  @Test
  void punchSequenceYields480WorkMinutesAfterBreak() {
    service.punch(aoki.id(), PunchType.CLOCK_IN);
    clock.setInstant(Instant.parse("2026-08-19T03:00:00Z"));
    service.punch(aoki.id(), PunchType.BREAK_START);
    clock.setInstant(Instant.parse("2026-08-19T04:00:00Z"));
    service.punch(aoki.id(), PunchType.BREAK_END);
    clock.setInstant(Instant.parse("2026-08-19T09:00:00Z"));
    service.punch(aoki.id(), PunchType.CLOCK_OUT);

    DailySummary summary = service.dailySummary(aoki.id(), LocalDate.of(2026, 8, 19));
    assertThat(summary.workMinutes()).isEqualTo(480);
    assertThat(summary.breakMinutes()).isEqualTo(60);
  }

  @Test
  void tokyoMidnightIsANewWorkDate() {
    clock.setInstant(Instant.parse("2026-08-18T14:59:00Z"));
    PunchEvent late = service.punch(aoki.id(), PunchType.CLOCK_IN);
    assertThat(late.workDate()).isEqualTo(LocalDate.of(2026, 8, 18));

    clock.setInstant(Instant.parse("2026-08-18T15:00:00Z"));
    PunchEvent midnight = service.punch(aoki.id(), PunchType.CLOCK_IN);
    assertThat(midnight.workDate()).isEqualTo(LocalDate.of(2026, 8, 19));

    assertThat(service.dailySummary(aoki.id(), LocalDate.of(2026, 8, 18)).punches()).hasSize(1);
    assertThat(service.dailySummary(aoki.id(), LocalDate.of(2026, 8, 19)).punches()).hasSize(1);
  }

  @Test
  void monthSummaryListsEveryCivilDayAndKeepsPunchesOnTheirTokyoDate() {
    clock.setInstant(Instant.parse("2026-08-18T14:59:00Z"));
    service.punch(aoki.id(), PunchType.CLOCK_IN);
    clock.setInstant(Instant.parse("2026-08-19T00:00:00Z"));
    service.punch(aoki.id(), PunchType.CLOCK_IN);
    clock.setInstant(Instant.parse("2026-08-19T09:00:00Z"));
    service.punch(aoki.id(), PunchType.CLOCK_OUT);

    MonthSummary month = service.monthSummary(aoki.id(), YearMonth.of(2026, 8));
    assertThat(month.days()).hasSize(31);
    DailySummary day18 =
        month.days().stream().filter(d -> d.workDate().equals(LocalDate.of(2026, 8, 18))).findFirst().orElseThrow();
    DailySummary day19 =
        month.days().stream().filter(d -> d.workDate().equals(LocalDate.of(2026, 8, 19))).findFirst().orElseThrow();
    DailySummary empty =
        month.days().stream().filter(d -> d.workDate().equals(LocalDate.of(2026, 8, 1))).findFirst().orElseThrow();
    assertThat(day18.punches()).hasSize(1);
    assertThat(day19.punches()).hasSize(2);
    assertThat(day19.workMinutes()).isEqualTo(540);
    assertThat(empty.punches()).isEmpty();
    assertThat(service.monthSummary(sato.id(), YearMonth.of(2026, 8)).days())
        .allMatch(d -> d.punches().isEmpty());
  }

  @Test
  void anotherEmployeeDoesNotSeePunches() {
    service.punch(aoki.id(), PunchType.CLOCK_IN);
    DailySummary other = service.dailySummary(sato.id(), WorkDates.of(clock.instant()));
    assertThat(other.punches()).isEmpty();
    assertThat(other.workMinutes()).isZero();
  }

  @Test
  void serverClockIsUsedNotClientInstant() {
    clock.setInstant(Instant.parse("2026-08-19T00:00:00Z"));
    PunchEvent event = service.punch(aoki.id(), PunchType.CLOCK_IN);
    assertThat(event.punchedAt()).isEqualTo(Instant.parse("2026-08-19T00:00:00Z"));
  }

  @Test
  void secondClockInConflicts() {
    service.punch(aoki.id(), PunchType.CLOCK_IN);
    assertThatThrownBy(() -> service.punch(aoki.id(), PunchType.CLOCK_IN))
        .isInstanceOf(PunchConflictException.class);
  }

  @Test
  void managerApprovesLeaveAndMemberCannotClose() {
    WorkRequest req = service.submitRequest(aoki.id(), WorkRequest.LEAVE, LocalDate.of(2026, 8, 20), "有給");
    assertThatThrownBy(() -> service.closeMonth(aoki, YearMonth.of(2026, 8))).isInstanceOf(ForbiddenException.class);
    WorkRequest decided = service.decide(sato, req.id(), true);
    assertThat(decided.status()).isEqualTo(WorkRequest.APPROVED);
  }

  @Test
  void closedMonthRejectsPunchAndAllocationExceedsWork() {
    service.punch(aoki.id(), PunchType.CLOCK_IN);
    clock.setInstant(Instant.parse("2026-08-19T08:00:00Z"));
    service.punch(aoki.id(), PunchType.CLOCK_OUT);
    TimeAllocation row = service.allocate(aoki.id(), LocalDate.of(2026, 8, 19), "P09", 120);
    assertThat(row.minutes()).isEqualTo(120);
    assertThatThrownBy(() -> service.allocate(aoki.id(), LocalDate.of(2026, 8, 19), "P12", 500))
        .isInstanceOf(IllegalArgumentException.class);
    service.closeMonth(sato, YearMonth.of(2026, 8));
    assertThatThrownBy(() -> service.punch(aoki.id(), PunchType.CLOCK_IN))
        .isInstanceOf(com.pf.attendance.domain.PeriodClosedException.class);
    String csv = service.monthCsv(sato, YearMonth.of(2026, 8));
    assertThat(csv).contains("aoki.haru");
    assertThat(csv.split("\n", 2)[0])
        .isEqualTo(
            "sub,displayName,workDate,workMinutes,breakMinutes,status,engagement,worksiteCode,worksiteName");
    assertThat(csv).contains("employed");
    assertThat(csv).contains("ise.yuto");
    assertThat(csv).contains("client_site");
    assertThat(csv).contains("WS-CLIENT-A");
    assertThat(csv).contains("架空商事 本社開発");
    assertThat(service.unpunched(sato, LocalDate.of(2026, 8, 19))).extracting(Employee::sub).contains("sato.mei");
  }

  @Test
  void handoffExportAndIngestForClientSiteOnly() {
    service.punch(ise.id(), PunchType.CLOCK_IN);
    clock.setInstant(Instant.parse("2026-08-19T08:00:00Z"));
    service.punch(ise.id(), PunchType.CLOCK_OUT);

    String packageCsv = service.exportHandoffCsv(sato, YearMonth.of(2026, 8));
    assertThat(packageCsv).contains("ise.yuto");
    assertThat(packageCsv).contains("WS-CLIENT-A");
    assertThat(packageCsv).doesNotContain("aoki.haru");

    var receipt = service.ingestHandoffCsv(sato, YearMonth.of(2026, 8), packageCsv, "worksite-demo");
    assertThat(receipt.lines()).isNotEmpty();
    assertThat(receipt.sourceHint()).isEqualTo("worksite-demo");
    assertThat(service.listHandoffs(sato, YearMonth.of(2026, 8))).hasSize(1);

    assertThatThrownBy(
            () ->
                service.ingestHandoffCsv(
                    sato,
                    YearMonth.of(2026, 8),
                    "sub,worksiteCode,worksiteName,workDate,workMinutes,breakMinutes,status\n"
                        + "aoki.haru,x,y,2026-08-19,60,0,complete\n",
                    "bad"))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("client_site");
  }

  @Test
  void worksiteSeesGuestsButPayrollCsvStaysEmployerOnly() {
    Employee worksiteManager =
        employees.findByOrgIdAndSub(DemoEmployees.ORG_B, "sato.mei").orElseThrow();
    var members = service.listVisibleMembers(worksiteManager);
    assertThat(members)
        .anyMatch(m -> VisibleMember.KIND_GUEST.equals(m.kind()) && "ise.yuto".equals(m.sub()));
    assertThat(members)
        .anyMatch(m -> VisibleMember.KIND_LOCAL.equals(m.kind()) && "aoki.haru".equals(m.sub()));

    String worksiteCsv = service.monthCsv(worksiteManager, YearMonth.of(2026, 8));
    assertThat(worksiteCsv).doesNotContain("ise.yuto");
    assertThat(worksiteCsv).contains("aoki.haru");

    String employerCsv = service.monthCsv(sato, YearMonth.of(2026, 8));
    assertThat(employerCsv).contains("ise.yuto");
  }

  @Test
  void periodAnchorDayChangesSummaryRangeAndCloseBoundary() {
    service.putPeriodSettings(sato, 21);
    MonthSummary summary = service.monthSummary(aoki.id(), YearMonth.of(2026, 8));
    assertThat(summary.days().getFirst().workDate()).isEqualTo(LocalDate.of(2026, 7, 21));
    assertThat(summary.days().getLast().workDate()).isEqualTo(LocalDate.of(2026, 8, 20));

    service.closeMonth(sato, YearMonth.of(2026, 8));
    // 2026-08-19 is still in closed period 2026-08 when anchor=21
    assertThatThrownBy(() -> service.punch(aoki.id(), PunchType.CLOCK_IN))
        .isInstanceOf(com.pf.attendance.domain.PeriodClosedException.class);

    clock.setInstant(Instant.parse("2026-08-21T01:00:00Z")); // JST 08-21 10:00 → period 2026-09
    PunchEvent ok = service.punch(aoki.id(), PunchType.CLOCK_IN);
    assertThat(ok.workDate()).isEqualTo(LocalDate.of(2026, 8, 21));
  }

  @Test
  void csvProfileAndProvisionalAndPdf() {
    service.putPeriodSettings(
        sato,
        new OrgPeriodSettings(
            DemoEmployees.ORG_A, 1, 25, "erp-generic-ja", false, List.of()));
    service.putProvisional(aoki, LocalDate.of(2026, 8, 26), 480, 60, "見込み");
    var day = service.dailySummary(aoki.id(), LocalDate.of(2026, 8, 26));
    assertThat(day.provisional()).isTrue();
    assertThat(day.workMinutes()).isEqualTo(480);

    String csv = service.monthCsv(sato, YearMonth.of(2026, 8), "erp-generic-ja", false, List.of());
    assertThat(csv).doesNotStartWith("sub,");
    assertThat(csv).contains("aoki.haru,2026-08-26,480,60,provisional,1");

    String custom =
        service.monthCsv(
            sato,
            YearMonth.of(2026, 8),
            "custom",
            true,
            List.of("workDate", "sub", "workMinutes"));
    assertThat(custom.split("\n", 2)[0]).isEqualTo("workDate,sub,workMinutes");

    byte[] pdf = service.monthPdf(sato, YearMonth.of(2026, 8), "aoki.haru");
    assertThat(pdf.length).isGreaterThan(100);
    assertThat(new String(pdf, 0, 4)).isEqualTo("%PDF");
  }

  @Test
  void moneyForwardAndFreeeVendorProfiles() {
    service.punch(aoki.id(), PunchType.CLOCK_IN);
    clock.setInstant(Instant.parse("2026-08-19T08:00:00Z"));
    service.punch(aoki.id(), PunchType.CLOCK_OUT);

    String mf =
        service.monthCsv(sato, YearMonth.of(2026, 8), "mf-attendance-punch-v1", null, List.of());
    assertThat(mf.split("\n", 2)[0])
        .isEqualTo("従業員番号,苗字,名前,打刻所属日,打刻日,打刻時刻,打刻種別");
    assertThat(mf).contains("aoki.haru,青木,陽,");
    assertThat(mf).contains("出勤");
    assertThat(mf).contains("退勤");

    String freee =
        service.monthCsv(sato, YearMonth.of(2026, 8), "freee-hr-monthly-v1", null, List.of());
    assertThat(freee).startsWith("従業員番号,氏名,所定労働時間（分）");
    assertThat(freee).contains("aoki.haru");
    assertThat(freee).contains("総労働時間（分）");
  }

  private static final class MutableClock extends Clock {
    private Instant instant;

    private MutableClock(Instant instant) {
      this.instant = instant;
    }

    private void setInstant(Instant instant) {
      this.instant = instant;
    }

    @Override
    public java.time.ZoneId getZone() {
      return ZoneOffset.UTC;
    }

    @Override
    public Clock withZone(java.time.ZoneId zone) {
      return Clock.fixed(instant, zone);
    }

    @Override
    public Instant instant() {
      return instant;
    }
  }
}
