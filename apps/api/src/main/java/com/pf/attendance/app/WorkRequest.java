package com.pf.attendance.app;

import java.time.Instant;
import java.time.LocalDate;

public record WorkRequest(
    String id,
    String employeeId,
    String type,
    String status,
    LocalDate workDate,
    String reason,
    Instant createdAt,
    Instant decidedAt,
    String decidedBy) {
  public static final String LEAVE = "leave";
  public static final String PUNCH_CORRECTION = "punch_correction";
  public static final String PENDING = "pending";
  public static final String APPROVED = "approved";
  public static final String REJECTED = "rejected";
}
