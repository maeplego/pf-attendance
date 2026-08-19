package com.pf.attendance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class DailyHoursCalculatorTest {

  private static final LocalDate DAY = LocalDate.of(2026, 8, 19);

  @Test
  void nineToSixMinusHourBreakIs480Minutes() {
    List<PunchEvent> punches =
        List.of(
            punch("1", PunchType.CLOCK_IN, "2026-08-19T00:00:00Z"),
            punch("2", PunchType.BREAK_START, "2026-08-19T03:00:00Z"),
            punch("3", PunchType.BREAK_END, "2026-08-19T04:00:00Z"),
            punch("4", PunchType.CLOCK_OUT, "2026-08-19T09:00:00Z"));

    DailySummary summary = DailyHoursCalculator.compute(DAY, punches);

    assertThat(summary.workMinutes()).isEqualTo(480);
    assertThat(summary.breakMinutes()).isEqualTo(60);
    assertThat(summary.status()).isEqualTo(PunchState.CLOCKED_OUT);
  }

  @Test
  void unfinishedBreakDoesNotCountOpenIntervalWithoutAsOf() {
    List<PunchEvent> punches =
        List.of(
            punch("1", PunchType.CLOCK_IN, "2026-08-19T00:00:00Z"),
            punch("2", PunchType.BREAK_START, "2026-08-19T03:00:00Z"));

    DailySummary summary = DailyHoursCalculator.compute(DAY, punches);

    assertThat(summary.workMinutes()).isEqualTo(180);
    assertThat(summary.breakMinutes()).isEqualTo(0);
    assertThat(summary.status()).isEqualTo(PunchState.ON_BREAK);
  }

  @Test
  void unfinishedBreakExtendsUntilAsOf() {
    List<PunchEvent> punches =
        List.of(
            punch("1", PunchType.CLOCK_IN, "2026-08-19T00:00:00Z"),
            punch("2", PunchType.BREAK_START, "2026-08-19T03:00:00Z"));

    DailySummary summary =
        DailyHoursCalculator.compute(DAY, punches, Instant.parse("2026-08-19T09:00:00Z"));

    assertThat(summary.workMinutes()).isEqualTo(180);
    assertThat(summary.breakMinutes()).isEqualTo(360);
    assertThat(summary.status()).isEqualTo(PunchState.ON_BREAK);
  }

  @Test
  void fiftySecondsIsZeroIntegerMinutes() {
    List<PunchEvent> punches =
        List.of(
            punch("1", PunchType.CLOCK_IN, "2026-08-19T00:00:00Z"),
            punch("2", PunchType.CLOCK_OUT, "2026-08-19T00:00:50Z"));

    DailySummary summary = DailyHoursCalculator.compute(DAY, punches);

    assertThat(summary.workMinutes()).isEqualTo(0);
    assertThat(summary.breakMinutes()).isEqualTo(0);
  }

  @Test
  void clockOutWhileOnBreakIsRejected() {
    List<PunchEvent> punches =
        List.of(
            punch("1", PunchType.CLOCK_IN, "2026-08-19T00:00:00Z"),
            punch("2", PunchType.BREAK_START, "2026-08-19T03:00:00Z"));

    assertThatThrownBy(
            () ->
                DailyHoursCalculator.compute(
                    DAY,
                    List.of(
                        punches.get(0),
                        punches.get(1),
                        punch("3", PunchType.CLOCK_OUT, "2026-08-19T09:00:00Z"))))
        .isInstanceOf(PunchConflictException.class);
  }

  private static PunchEvent punch(String id, PunchType type, String utc) {
    Instant at = Instant.parse(utc);
    return new PunchEvent(id, "emp-aoki", type, at, WorkDates.of(at), "web");
  }
}
