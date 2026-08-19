package com.pf.attendance.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AttendanceServiceTest {
  private MemoryEmployeeStore employees;
  private MemoryPunchStore punches;
  private MutableClock clock;
  private AttendanceService service;
  private Employee aoki;
  private Employee sato;

  @BeforeEach
  void setUp() {
    employees = new MemoryEmployeeStore();
    punches = new MemoryPunchStore();
    clock = new MutableClock(Instant.parse("2026-08-19T00:00:00Z"));
    for (Employee employee : DemoEmployees.roster()) {
      employees.save(employee);
    }
    aoki = employees.findBySub("aoki.haru").orElseThrow();
    sato = employees.findBySub("sato.mei").orElseThrow();
    service = new AttendanceService(employees, punches, clock);
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
