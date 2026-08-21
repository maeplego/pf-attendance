package com.pf.attendance.app;

/** Leave subtype for type=leave. Empty for punch_correction. */
public final class LeaveKind {
  public static final String PAID = "paid";
  public static final String AM_HALF = "am_half";
  public static final String PM_HALF = "pm_half";
  public static final String ABSENCE = "absence";

  private LeaveKind() {}

  public static String normalize(String type, String leaveKind) {
    if (!WorkRequest.LEAVE.equals(type)) {
      return "";
    }
    if (leaveKind == null || leaveKind.isBlank()) {
      return PAID;
    }
    return switch (leaveKind.trim().toLowerCase()) {
      case PAID, "有給", "paid_leave" -> PAID;
      case AM_HALF, "am", "午前休" -> AM_HALF;
      case PM_HALF, "pm", "午後休" -> PM_HALF;
      case ABSENCE, "欠勤" -> ABSENCE;
      default -> throw new IllegalArgumentException(
          "leaveKind must be paid, am_half, pm_half, or absence");
    };
  }

  public static boolean isHalf(String leaveKind) {
    return AM_HALF.equals(leaveKind) || PM_HALF.equals(leaveKind);
  }
}
