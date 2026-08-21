package com.pf.attendance.domain;

import java.time.LocalDate;
import java.util.List;

public record DailySummary(
    LocalDate workDate,
    int workMinutes,
    int breakMinutes,
    PunchState status,
    List<PunchEvent> punches,
    boolean provisional,
    String leaveKind,
    int lateMinutes,
    int earlyLeaveMinutes,
    int overtimeMinutes) {

  public DailySummary(
      LocalDate workDate,
      int workMinutes,
      int breakMinutes,
      PunchState status,
      List<PunchEvent> punches) {
    this(workDate, workMinutes, breakMinutes, status, punches, false, "", 0, 0, 0);
  }

  public DailySummary {
    leaveKind = leaveKind == null ? "" : leaveKind;
    punches = punches == null ? List.of() : List.copyOf(punches);
  }
}
