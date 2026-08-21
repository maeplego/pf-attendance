package com.pf.attendance.domain;

import java.util.Comparator;
import java.util.List;

/**
 * One day-shift sequence per Tokyo work date. Night shifts are out of scope:
 * a punch after 00:00 is a new day, not a continuation.
 */
public final class PunchRules {
  private PunchRules() {}

  public static PunchState stateOf(List<PunchEvent> punches) {
    PunchState state = PunchState.ABSENT;
    for (PunchEvent punch : sorted(punches)) {
      state = next(state, punch.type());
    }
    return state;
  }

  public static void assertAllowed(List<PunchEvent> existing, PunchType nextType) {
    next(stateOf(existing), nextType);
  }

  public static PunchState next(PunchState state, PunchType type) {
    return switch (state) {
      case ABSENT -> {
        if (type == PunchType.CLOCK_IN) {
          yield PunchState.CLOCKED_IN;
        }
        throw new PunchConflictException("clock_in is required first");
      }
      case CLOCKED_IN -> switch (type) {
        case BREAK_START -> PunchState.ON_BREAK;
        case CLOCK_OUT -> PunchState.CLOCKED_OUT;
        case CLOCK_IN -> throw new PunchConflictException("already clocked in");
        case BREAK_END -> throw new PunchConflictException("not on break");
      };
      case ON_BREAK -> {
        if (type == PunchType.BREAK_END) {
          yield PunchState.CLOCKED_IN;
        }
        throw new PunchConflictException("end the break before clock_out or another break");
      }
      case CLOCKED_OUT -> throw new PunchConflictException("already clocked out for this work date");
      case PROVISIONAL, ON_LEAVE ->
          throw new PunchConflictException("leave/provisional day is not a punch state");
    };
  }

  static List<PunchEvent> sorted(List<PunchEvent> punches) {
    return punches.stream()
        .sorted(Comparator.comparing(PunchEvent::punchedAt).thenComparing(PunchEvent::id))
        .toList();
  }
}
