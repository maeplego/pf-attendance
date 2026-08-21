package com.pf.attendance.app.handoff;

/** Day line in a worksite→employer timesheet handoff (no yen). */
public record HandoffDayLine(
    String sub,
    String worksiteCode,
    String worksiteName,
    String workDate,
    int workMinutes,
    int breakMinutes,
    String status) {}
