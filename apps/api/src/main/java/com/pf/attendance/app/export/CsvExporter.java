package com.pf.attendance.app.export;

import com.pf.attendance.app.Employee;
import com.pf.attendance.domain.DailySummary;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public final class CsvExporter {
  private CsvExporter() {}

  public static String render(
      CsvExportProfile profile, List<EmployeeDayRow> rows) {
    StringJoiner lines = new StringJoiner("\n");
    if (profile.includeHeader()) {
      StringJoiner header = new StringJoiner(",");
      for (CsvColumn col : profile.columns()) {
        header.add(col.header());
      }
      lines.add(header.toString());
    }
    for (EmployeeDayRow row : rows) {
      StringJoiner line = new StringJoiner(",");
      for (CsvColumn col : profile.columns()) {
        line.add(cell(col, row));
      }
      lines.add(line.toString());
    }
    return lines.toString() + "\n";
  }

  private static String cell(CsvColumn col, EmployeeDayRow row) {
    Employee e = row.employee();
    DailySummary d = row.day();
    return switch (col) {
      case SUB -> e.sub();
      case DISPLAY_NAME -> csv(e.displayName());
      case WORK_DATE -> d.workDate().toString();
      case WORK_MINUTES -> Integer.toString(d.workMinutes());
      case BREAK_MINUTES -> Integer.toString(d.breakMinutes());
      case STATUS -> d.status().name().toLowerCase();
      case ENGAGEMENT -> e.engagement();
      case WORKSITE_CODE -> csv(e.worksiteCode());
      case WORKSITE_NAME -> csv(e.worksiteName());
      case PROVISIONAL -> d.provisional() ? "1" : "0";
    };
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

  public record EmployeeDayRow(Employee employee, DailySummary day) {}

  public static List<EmployeeDayRow> flatten(Employee employee, List<DailySummary> days) {
    List<EmployeeDayRow> out = new ArrayList<>();
    for (DailySummary day : days) {
      out.add(new EmployeeDayRow(employee, day));
    }
    return out;
  }
}
