package com.pf.attendance.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class PunchRulesTest {

  @Test
  void secondClockInSameDayConflicts() {
    PunchEvent first =
        new PunchEvent(
            "1",
            "emp-aoki",
            PunchType.CLOCK_IN,
            Instant.parse("2026-08-19T00:00:00Z"),
            WorkDates.of(Instant.parse("2026-08-19T00:00:00Z")),
            "web");

    assertThatThrownBy(() -> PunchRules.assertAllowed(List.of(first), PunchType.CLOCK_IN))
        .isInstanceOf(PunchConflictException.class)
        .hasMessageContaining("already clocked in");
  }

  @Test
  void clockInThenBreakThenEndThenOutIsValid() {
    PunchEvent in =
        new PunchEvent(
            "1",
            "emp-aoki",
            PunchType.CLOCK_IN,
            Instant.parse("2026-08-19T00:00:00Z"),
            WorkDates.of(Instant.parse("2026-08-19T00:00:00Z")),
            "web");
    PunchRules.assertAllowed(List.of(), PunchType.CLOCK_IN);
    PunchRules.assertAllowed(List.of(in), PunchType.BREAK_START);
    assertThat(PunchRules.next(PunchState.ON_BREAK, PunchType.BREAK_END))
        .isEqualTo(PunchState.CLOCKED_IN);
    assertThat(PunchRules.next(PunchState.CLOCKED_IN, PunchType.CLOCK_OUT))
        .isEqualTo(PunchState.CLOCKED_OUT);
  }
}
