package com.pf.attendance.domain;

import java.time.Duration;
import java.time.Instant;

/**
 * Labor totals are integer minutes. Seconds are truncated toward zero so
 * floating-point hours never appear in storage or API responses.
 */
public final class Minutes {
  private Minutes() {}

  public static int between(Instant start, Instant end) {
    if (end.isBefore(start)) {
      throw new IllegalArgumentException("end is before start");
    }
    return (int) Duration.between(start, end).toMinutes();
  }

  public static int ofSeconds(long seconds) {
    if (seconds < 0) {
      throw new IllegalArgumentException("seconds must be >= 0");
    }
    return (int) (seconds / 60);
  }
}
