package com.pf.attendance.app.handoff;

import java.util.ArrayList;
import java.util.List;

/** Parse/format worksite-minutes-v1 handoff CSV (no amount columns). */
public final class HandoffCsv {
  public static final String HEADER =
      "sub,worksiteCode,worksiteName,workDate,workMinutes,breakMinutes,status";

  private HandoffCsv() {}

  public static String format(List<HandoffDayLine> lines) {
    StringBuilder sb = new StringBuilder(HEADER).append('\n');
    for (HandoffDayLine line : lines) {
      sb.append(line.sub())
          .append(',')
          .append(csv(line.worksiteCode()))
          .append(',')
          .append(csv(line.worksiteName()))
          .append(',')
          .append(line.workDate())
          .append(',')
          .append(line.workMinutes())
          .append(',')
          .append(line.breakMinutes())
          .append(',')
          .append(line.status())
          .append('\n');
    }
    return sb.toString();
  }

  public static List<HandoffDayLine> parse(String text) {
    String[] raw = text.split("\\r?\\n");
    List<String> lines = new ArrayList<>();
    for (String l : raw) {
      if (!l.isBlank()) {
        lines.add(l.trim());
      }
    }
    if (lines.isEmpty()) {
      throw new IllegalArgumentException("empty handoff csv");
    }
    String header = lines.get(0).toLowerCase();
    if (!header.startsWith("sub,worksitecode,worksitename,workdate,workminutes,breakminutes,status")) {
      throw new IllegalArgumentException("unexpected handoff csv header: " + lines.get(0));
    }
    if (header.matches("(?i).*\\b(amount|yen|salary|pay|tax)\\b.*")) {
      throw new IllegalArgumentException("amount/tax columns are not allowed in handoff csv");
    }
    List<HandoffDayLine> out = new ArrayList<>();
    for (int i = 1; i < lines.size(); i++) {
      List<String> cols = split(lines.get(i));
      if (cols.size() < 7) {
        throw new IllegalArgumentException("row " + (i + 1) + ": expected 7 columns");
      }
      int workMinutes = Integer.parseInt(cols.get(4));
      int breakMinutes = Integer.parseInt(cols.get(5));
      out.add(
          new HandoffDayLine(
              cols.get(0), cols.get(1), cols.get(2), cols.get(3), workMinutes, breakMinutes, cols.get(6)));
    }
    return List.copyOf(out);
  }

  private static String csv(String value) {
    if (value == null) {
      return "";
    }
    if (value.contains(",") || value.contains("\"")) {
      return "\"" + value.replace("\"", "\"\"") + "\"";
    }
    return value;
  }

  private static List<String> split(String line) {
    List<String> out = new ArrayList<>();
    StringBuilder cur = new StringBuilder();
    boolean inQuotes = false;
    for (int i = 0; i < line.length(); i++) {
      char ch = line.charAt(i);
      if (inQuotes) {
        if (ch == '"') {
          if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
            cur.append('"');
            i++;
          } else {
            inQuotes = false;
          }
        } else {
          cur.append(ch);
        }
      } else if (ch == '"') {
        inQuotes = true;
      } else if (ch == ',') {
        out.add(cur.toString());
        cur.setLength(0);
      } else {
        cur.append(ch);
      }
    }
    out.add(cur.toString());
    return out;
  }
}
