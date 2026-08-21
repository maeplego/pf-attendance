package com.pf.attendance.domain;

import java.time.LocalTime;
import java.util.List;

/** Compare real punches to {@link WorkSchedule} for late / early leave / simple overtime. */
public final class ScheduleVariance {
  private ScheduleVariance() {}

  public record Result(int lateMinutes, int earlyLeaveMinutes, int overtimeMinutes) {}

  public static Result compute(WorkSchedule schedule, List<PunchEvent> punches, int workMinutes) {
    if (punches == null || punches.isEmpty()) {
      return new Result(0, 0, 0);
    }
    LocalTime in = null;
    LocalTime out = null;
    for (PunchEvent p : PunchRules.sorted(punches)) {
      LocalTime t = p.punchedAt().atZone(WorkDates.ZONE).toLocalTime();
      if (p.type() == PunchType.CLOCK_IN && in == null) {
        in = t;
      }
      if (p.type() == PunchType.CLOCK_OUT) {
        out = t;
      }
    }
    int late = 0;
    int early = 0;
    if (in != null && in.isAfter(schedule.scheduledStart())) {
      late = (int) java.time.Duration.between(schedule.scheduledStart(), in).toMinutes();
    }
    if (out != null && out.isBefore(schedule.scheduledEnd())) {
      early = (int) java.time.Duration.between(out, schedule.scheduledEnd()).toMinutes();
    }
    int ot = Math.max(0, workMinutes - schedule.scheduledNetMinutes());
    return new Result(late, early, ot);
  }
}
