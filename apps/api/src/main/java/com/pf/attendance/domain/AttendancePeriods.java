package com.pf.attendance.domain;

import java.time.LocalDate;
import java.time.YearMonth;

/**
 * Maps civil dates to a labeled attendance period. Anchor 1 = calendar month. Anchor 21 means period
 * {@code YYYY-MM} runs from previous month's 21st through this month's 20th.
 */
public final class AttendancePeriods {
  private AttendancePeriods() {}

  public static int normalizeAnchor(int anchorDay) {
    if (anchorDay < 1 || anchorDay > 28) {
      throw new IllegalArgumentException("periodAnchorDay must be 1..28");
    }
    return anchorDay;
  }

  public static YearMonth periodContaining(LocalDate date, int anchorDay) {
    int anchor = normalizeAnchor(anchorDay);
    if (anchor == 1) {
      return YearMonth.from(date);
    }
    if (date.getDayOfMonth() >= anchor) {
      return YearMonth.from(date).plusMonths(1);
    }
    return YearMonth.from(date);
  }

  public static LocalDate startInclusive(YearMonth period, int anchorDay) {
    int anchor = normalizeAnchor(anchorDay);
    if (anchor == 1) {
      return period.atDay(1);
    }
    return period.minusMonths(1).atDay(anchor);
  }

  public static LocalDate endInclusive(YearMonth period, int anchorDay) {
    int anchor = normalizeAnchor(anchorDay);
    if (anchor == 1) {
      return period.atEndOfMonth();
    }
    return period.atDay(anchor - 1);
  }
}
