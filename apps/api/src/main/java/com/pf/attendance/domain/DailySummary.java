package com.pf.attendance.domain;

import java.time.LocalDate;
import java.util.List;

public record DailySummary(
    LocalDate workDate,
    int workMinutes,
    int breakMinutes,
    PunchState status,
    List<PunchEvent> punches) {}
