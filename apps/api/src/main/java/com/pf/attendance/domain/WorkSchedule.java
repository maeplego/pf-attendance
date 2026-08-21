package com.pf.attendance.domain;

import java.time.Duration;
import java.time.LocalTime;
import java.util.Locale;

/**
 * Org / worksite day-shift defaults. Break hint mirrors common 労基法34条 thresholds for demos only —
 * not legal advice.
 */
public record WorkSchedule(
    LocalTime scheduledStart,
    LocalTime scheduledEnd,
    int breakMinutes,
    BreakMode breakMode) {

  public enum BreakMode {
    FIXED,
    LABOR_HINT;

    public String wire() {
      return name().toLowerCase(Locale.ROOT);
    }

    public static BreakMode fromWire(String raw) {
      if (raw == null || raw.isBlank()) {
        return FIXED;
      }
      return switch (raw.trim().toLowerCase(Locale.ROOT)) {
        case "fixed" -> FIXED;
        case "labor_hint" -> LABOR_HINT;
        default -> throw new IllegalArgumentException("breakMode must be fixed or labor_hint");
      };
    }
  }

  public WorkSchedule {
    if (scheduledStart == null || scheduledEnd == null) {
      throw new IllegalArgumentException("scheduled start/end required");
    }
    if (!scheduledEnd.isAfter(scheduledStart)) {
      throw new IllegalArgumentException("scheduledEnd must be after scheduledStart (day shift only)");
    }
    if (breakMode == null) {
      breakMode = BreakMode.FIXED;
    }
    int span = (int) Duration.between(scheduledStart, scheduledEnd).toMinutes();
    if (breakMode == BreakMode.LABOR_HINT) {
      breakMinutes = laborHintBreakMinutes(span);
    } else if (breakMinutes < 0 || breakMinutes >= span) {
      throw new IllegalArgumentException("breakMinutes must be 0.." + (span - 1));
    }
  }

  public static WorkSchedule defaults() {
    return new WorkSchedule(LocalTime.of(9, 0), LocalTime.of(18, 0), 60, BreakMode.FIXED);
  }

  /** Gross span minus break — typical “所定労働” minutes for a full day. */
  public int scheduledNetMinutes() {
    int span = (int) Duration.between(scheduledStart, scheduledEnd).toMinutes();
    return Math.max(0, span - breakMinutes);
  }

  public LocalTime breakStart() {
    long mid = Duration.between(scheduledStart, scheduledEnd).toMinutes() / 2;
    return scheduledStart.plusMinutes(mid - (long) breakMinutes / 2);
  }

  public LocalTime breakEnd() {
    return breakStart().plusMinutes(breakMinutes);
  }

  /**
   * Educational: ≥8h work span → 60m, ≥6h → 45m. Based on common 労基法34条 reading; not compliance.
   */
  public static int laborHintBreakMinutes(int scheduledSpanMinutes) {
    if (scheduledSpanMinutes >= 8 * 60) {
      return 60;
    }
    if (scheduledSpanMinutes >= 6 * 60) {
      return 45;
    }
    return 0;
  }

  public static LocalTime parseTime(String raw, String field) {
    if (raw == null || raw.isBlank()) {
      throw new IllegalArgumentException(field + " is required");
    }
    return LocalTime.parse(raw.trim());
  }
}
