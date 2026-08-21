package com.pf.attendance.app.export;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/** Selectable CSV columns for org export profiles (not a certified ERP schema). */
public enum CsvColumn {
  SUB,
  DISPLAY_NAME,
  WORK_DATE,
  WORK_MINUTES,
  BREAK_MINUTES,
  STATUS,
  ENGAGEMENT,
  WORKSITE_CODE,
  WORKSITE_NAME,
  PROVISIONAL;

  public String header() {
    return switch (this) {
      case SUB -> "sub";
      case DISPLAY_NAME -> "displayName";
      case WORK_DATE -> "workDate";
      case WORK_MINUTES -> "workMinutes";
      case BREAK_MINUTES -> "breakMinutes";
      case STATUS -> "status";
      case ENGAGEMENT -> "engagement";
      case WORKSITE_CODE -> "worksiteCode";
      case WORKSITE_NAME -> "worksiteName";
      case PROVISIONAL -> "provisional";
    };
  }

  public static CsvColumn parse(String raw) {
    String key = raw.trim().replace("-", "_");
    for (CsvColumn c : values()) {
      if (c.name().equalsIgnoreCase(key)
          || c.header().equalsIgnoreCase(raw.trim())
          || c.header().toLowerCase(Locale.ROOT).equals(raw.trim().toLowerCase(Locale.ROOT))) {
        return c;
      }
    }
    throw new IllegalArgumentException("unknown csv column: " + raw);
  }

  public static List<CsvColumn> parseList(List<String> raw) {
    return raw.stream().map(CsvColumn::parse).toList();
  }

  public static List<CsvColumn> parseCsv(String commaSeparated) {
    if (commaSeparated == null || commaSeparated.isBlank()) {
      return List.of();
    }
    return Arrays.stream(commaSeparated.split(",")).map(CsvColumn::parse).toList();
  }
}
