package com.pf.attendance.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class WorkDatesTest {

  @Test
  void twentyThreeFiftyNineTokyoStaysOnThatCalendarDay() {
    // 2026-08-18 23:59 JST = 2026-08-18 14:59 UTC
    assertThat(WorkDates.of(Instant.parse("2026-08-18T14:59:00Z")))
        .isEqualTo(LocalDate.of(2026, 8, 18));
  }

  @Test
  void midnightTokyoStartsTheNextCalendarDay() {
    // 2026-08-19 00:00 JST = 2026-08-18 15:00 UTC
    assertThat(WorkDates.of(Instant.parse("2026-08-18T15:00:00Z")))
        .isEqualTo(LocalDate.of(2026, 8, 19));
  }
}
