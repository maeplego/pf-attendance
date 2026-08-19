package com.pf.attendance.domain;

import java.util.Locale;

public enum PunchType {
  CLOCK_IN,
  CLOCK_OUT,
  BREAK_START,
  BREAK_END;

  public String wire() {
    return name().toLowerCase(Locale.ROOT);
  }

  public static PunchType fromWire(String raw) {
    if (raw == null || raw.isBlank()) {
      throw new IllegalArgumentException("type is required");
    }
    return switch (raw.trim().toLowerCase(Locale.ROOT)) {
      case "clock_in" -> CLOCK_IN;
      case "clock_out" -> CLOCK_OUT;
      case "break_start" -> BREAK_START;
      case "break_end" -> BREAK_END;
      default -> throw new IllegalArgumentException("unknown punch type: " + raw);
    };
  }
}
