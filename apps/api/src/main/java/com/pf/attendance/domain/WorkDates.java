package com.pf.attendance.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

/**
 * Work calendar days are Asia/Tokyo. Japan has no DST, so 23:59 and 00:00
 * stay on opposite sides of the same offset.
 */
public final class WorkDates {
  public static final ZoneId ZONE = ZoneId.of("Asia/Tokyo");

  private WorkDates() {}

  public static LocalDate of(Instant instant) {
    return instant.atZone(ZONE).toLocalDate();
  }
}
