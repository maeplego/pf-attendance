package com.pf.attendance.domain;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * Closed intervals only, plus an optional as-of instant for an open today.
 * Break time is subtracted from paid work by tracking disjoint segments.
 */
public final class DailyHoursCalculator {
  private DailyHoursCalculator() {}

  public static DailySummary compute(LocalDate workDate, List<PunchEvent> punches) {
    return compute(workDate, punches, null);
  }

  public static DailySummary compute(LocalDate workDate, List<PunchEvent> punches, Instant asOf) {
    Objects.requireNonNull(workDate, "workDate");
    List<PunchEvent> ordered = PunchRules.sorted(punches);
    Instant workCursor = null;
    Instant breakCursor = null;
    long workSeconds = 0;
    long breakSeconds = 0;
    PunchState state = PunchState.ABSENT;

    for (PunchEvent punch : ordered) {
      Instant at = punch.punchedAt();
      switch (punch.type()) {
        case CLOCK_IN -> {
          state = PunchRules.next(state, punch.type());
          workCursor = at;
        }
        case BREAK_START -> {
          state = PunchRules.next(state, punch.type());
          if (workCursor != null) {
            workSeconds += seconds(workCursor, at);
            workCursor = null;
          }
          breakCursor = at;
        }
        case BREAK_END -> {
          state = PunchRules.next(state, punch.type());
          if (breakCursor != null) {
            breakSeconds += seconds(breakCursor, at);
            breakCursor = null;
          }
          workCursor = at;
        }
        case CLOCK_OUT -> {
          state = PunchRules.next(state, punch.type());
          if (workCursor != null) {
            workSeconds += seconds(workCursor, at);
            workCursor = null;
          }
        }
      }
    }

    if (asOf != null) {
      if (state == PunchState.CLOCKED_IN && workCursor != null && !asOf.isBefore(workCursor)) {
        workSeconds += seconds(workCursor, asOf);
      }
      if (state == PunchState.ON_BREAK && breakCursor != null && !asOf.isBefore(breakCursor)) {
        breakSeconds += seconds(breakCursor, asOf);
      }
    }

    return new DailySummary(
        workDate,
        Minutes.ofSeconds(workSeconds),
        Minutes.ofSeconds(breakSeconds),
        state,
        List.copyOf(ordered));
  }

  private static long seconds(Instant start, Instant end) {
    return Duration.between(start, end).getSeconds();
  }
}
