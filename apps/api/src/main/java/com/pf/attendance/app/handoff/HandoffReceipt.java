package com.pf.attendance.app.handoff;

import java.time.Instant;
import java.time.YearMonth;
import java.util.List;

/** Bundle accepted into an employer org (Stage B). Does not create punches. */
public record HandoffReceipt(
    String id,
    String employerOrgId,
    YearMonth month,
    String sourceHint,
    Instant acceptedAt,
    String acceptedBySub,
    List<HandoffDayLine> lines) {}
