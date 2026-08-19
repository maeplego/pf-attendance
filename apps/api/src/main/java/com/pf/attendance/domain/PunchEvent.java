package com.pf.attendance.domain;

import java.time.Instant;
import java.time.LocalDate;

public record PunchEvent(
    String id,
    String employeeId,
    PunchType type,
    Instant punchedAt,
    LocalDate workDate,
    String source) {}
