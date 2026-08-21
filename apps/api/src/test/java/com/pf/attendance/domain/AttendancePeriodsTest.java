package com.pf.attendance.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.YearMonth;
import org.junit.jupiter.api.Test;

class AttendancePeriodsTest {
  @Test
  void calendarMonthWhenAnchorOne() {
    assertThat(AttendancePeriods.periodContaining(LocalDate.of(2026, 8, 15), 1))
        .isEqualTo(YearMonth.of(2026, 8));
    assertThat(AttendancePeriods.startInclusive(YearMonth.of(2026, 8), 1))
        .isEqualTo(LocalDate.of(2026, 8, 1));
    assertThat(AttendancePeriods.endInclusive(YearMonth.of(2026, 8), 1))
        .isEqualTo(LocalDate.of(2026, 8, 31));
  }

  @Test
  void twentyFirstAnchorSpansMonths() {
    assertThat(AttendancePeriods.periodContaining(LocalDate.of(2026, 8, 15), 21))
        .isEqualTo(YearMonth.of(2026, 8));
    assertThat(AttendancePeriods.periodContaining(LocalDate.of(2026, 8, 21), 21))
        .isEqualTo(YearMonth.of(2026, 9));
    assertThat(AttendancePeriods.startInclusive(YearMonth.of(2026, 8), 21))
        .isEqualTo(LocalDate.of(2026, 7, 21));
    assertThat(AttendancePeriods.endInclusive(YearMonth.of(2026, 8), 21))
        .isEqualTo(LocalDate.of(2026, 8, 20));
  }
}
