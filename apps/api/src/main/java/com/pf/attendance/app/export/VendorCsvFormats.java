package com.pf.attendance.app.export;

import com.pf.attendance.app.Employee;
import com.pf.attendance.app.LeaveKind;
import com.pf.attendance.domain.AttendancePeriods;
import com.pf.attendance.domain.DailySummary;
import com.pf.attendance.domain.MonthSummary;
import com.pf.attendance.domain.PunchEvent;
import com.pf.attendance.domain.PunchType;
import com.pf.attendance.domain.WorkDates;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.BiFunction;

/**
 * Vendor CSV layouts taken from publicly documented official help pages. See
 * {@code portfolio-plan/attendance/DESIGN.md}「外部ソフト CSV」for source URLs and gaps.
 */
public final class VendorCsvFormats {
  public static final String MF_ATTENDANCE_PUNCH_V1 = "mf-attendance-punch-v1";
  public static final String FREEE_HR_MONTHLY_V1 = "freee-hr-monthly-v1";

  private static final ZoneId TOKYO = WorkDates.ZONE;
  private static final DateTimeFormatter MF_DATE = DateTimeFormatter.ofPattern("yyyy/MM/dd");
  private static final DateTimeFormatter MF_TIME = DateTimeFormatter.ofPattern("HH:mm");
  private static final DateTimeFormatter ISO_DATE = DateTimeFormatter.ISO_LOCAL_DATE;

  private VendorCsvFormats() {}

  public static boolean isVendorProfile(String profileId) {
    return MF_ATTENDANCE_PUNCH_V1.equals(profileId) || FREEE_HR_MONTHLY_V1.equals(profileId);
  }

  /**
   * Money Forward Cloud Attendance — 日次勤怠データ import.
   * Source: https://biz.moneyforward.com/support/attendance/guide/link/import1.html (retrieved 2026-08-21)
   */
  public static String moneyForwardPunchCsv(
      List<Employee> employees,
      YearMonth period,
      int periodAnchorDay,
      BiFunction<String, LocalDate, List<PunchEvent>> punchesByEmployeeDate) {
    StringJoiner lines = new StringJoiner("\n");
    lines.add("従業員番号,苗字,名前,打刻所属日,打刻日,打刻時刻,打刻種別");
    LocalDate start = AttendancePeriods.startInclusive(period, periodAnchorDay);
    LocalDate end = AttendancePeriods.endInclusive(period, periodAnchorDay);
    for (Employee employee : employees) {
      String[] name = splitJapaneseName(employee.displayName());
      for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
        for (PunchEvent punch : punchesByEmployeeDate.apply(employee.id(), d)) {
          LocalTime time = LocalTime.ofInstant(punch.punchedAt(), TOKYO);
          lines.add(
              String.join(
                  ",",
                  csv(employee.sub()),
                  csv(name[0]),
                  csv(name[1]),
                  d.format(MF_DATE),
                  punch.workDate().format(MF_DATE),
                  time.format(MF_TIME),
                  mfPunchType(punch.type())));
        }
      }
    }
    return lines.toString() + "\n";
  }

  /**
   * freee 人事労務 — freee形式 monthly attendance import (column names from freee/KOT linkage docs).
   * Overtime / holiday breakdown fields are emitted as 0 — P09 does not compute 36-hour OT categories.
   * Source columns listed in freee help / KOT「freee人事労務とのCSV連携」articles (retrieved 2026-08-21).
   */
  public static String freeeHrMonthlyCsv(
      List<Employee> employees,
      YearMonth period,
      List<MonthSummary> summaries,
      List<Integer> paidLeaveDays) {
    StringJoiner lines = new StringJoiner("\n");
    lines.add(
        "従業員番号,氏名,所定労働時間（分）,法定内残業時間（分）,時間外労働時間（分）,深夜労働時間（分）,法定休日労働時間（分）,総労働時間（分）,総労働日数,所定労働出勤日数,所定休日出勤日数,法定休日出勤日数,遅刻時間（分）,早退時間（分）,欠勤日数,遅刻日数,早退日数,有休取得日数,集計開始日,集計終了日");
    for (int i = 0; i < employees.size(); i++) {
      Employee employee = employees.get(i);
      MonthSummary summary = summaries.get(i);
      int leaveDays = paidLeaveDays == null || i >= paidLeaveDays.size() ? 0 : paidLeaveDays.get(i);
      int totalWork = 0;
      int workDays = 0;
      int lateMinutes = 0;
      int earlyMinutes = 0;
      int lateDays = 0;
      int earlyDays = 0;
      int absenceDays = 0;
      LocalDate first = null;
      LocalDate last = null;
      for (DailySummary day : summary.days()) {
        if (first == null) {
          first = day.workDate();
        }
        last = day.workDate();
        totalWork += day.workMinutes();
        if (day.workMinutes() > 0 || day.provisional()) {
          workDays++;
        }
        lateMinutes += day.lateMinutes();
        earlyMinutes += day.earlyLeaveMinutes();
        if (day.lateMinutes() > 0) {
          lateDays++;
        }
        if (day.earlyLeaveMinutes() > 0) {
          earlyDays++;
        }
        if (LeaveKind.ABSENCE.equals(day.leaveKind())) {
          absenceDays++;
        }
      }
      lines.add(
          String.join(
              ",",
              csv(employee.sub()),
              csv(employee.displayName()),
              "0",
              "0",
              "0",
              "0",
              "0",
              Integer.toString(totalWork),
              Integer.toString(workDays),
              Integer.toString(workDays),
              "0",
              "0",
              Integer.toString(lateMinutes),
              Integer.toString(earlyMinutes),
              Integer.toString(absenceDays),
              Integer.toString(lateDays),
              Integer.toString(earlyDays),
              Integer.toString(leaveDays),
              first == null ? "" : first.format(ISO_DATE),
              last == null ? "" : last.format(ISO_DATE)));
    }
    return lines.toString() + "\n";
  }

  public static List<CatalogEntry> catalogEntries() {
    List<CatalogEntry> out = new ArrayList<>();
    out.add(
        new CatalogEntry(
            MF_ATTENDANCE_PUNCH_V1,
            "Money Forward Cloud Attendance punch import",
            "https://biz.moneyforward.com/support/attendance/guide/link/import1.html",
            "official-help",
            true));
    out.add(
        new CatalogEntry(
            FREEE_HR_MONTHLY_V1,
            "freee HR monthly (freee形式; OT fields zeroed)",
            "https://support.freee.co.jp/hc/ja/articles/204922194",
            "official-help-partial",
            true));
    return out;
  }

  private static String mfPunchType(PunchType type) {
    return switch (type) {
      case CLOCK_IN -> "出勤";
      case CLOCK_OUT -> "退勤";
      case BREAK_START -> "休憩開始";
      case BREAK_END -> "休憩終了";
    };
  }

  /** Best-effort split of "姓 名". */
  static String[] splitJapaneseName(String displayName) {
    if (displayName == null || displayName.isBlank()) {
      return new String[] {"", ""};
    }
    String trimmed = displayName.trim();
    String[] parts = trimmed.split("\\s+", 2);
    if (parts.length == 1) {
      return new String[] {parts[0], ""};
    }
    return new String[] {parts[0], parts[1]};
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

  public record CatalogEntry(
      String id, String label, String sourceUrl, String fidelity, boolean includeHeader) {}
}
