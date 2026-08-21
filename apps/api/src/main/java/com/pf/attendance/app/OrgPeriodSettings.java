package com.pf.attendance.app;

import com.pf.attendance.app.export.CsvColumn;
import com.pf.attendance.app.export.CsvExportProfile;
import com.pf.attendance.domain.AttendancePeriods;
import com.pf.attendance.domain.WorkSchedule;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record OrgPeriodSettings(
    String orgId,
    int periodAnchorDay,
    int closeByDay,
    String csvProfileId,
    boolean csvIncludeHeader,
    List<String> csvColumns,
    String scheduledStart,
    String scheduledEnd,
    int breakMinutes,
    String breakMode) {

  private static final DateTimeFormatter HM = DateTimeFormatter.ofPattern("HH:mm");

  public OrgPeriodSettings {
    periodAnchorDay = AttendancePeriods.normalizeAnchor(periodAnchorDay);
    if (closeByDay < 0 || closeByDay > 28) {
      throw new IllegalArgumentException("closeByDay must be 0..28 (0=disabled)");
    }
    csvProfileId =
        csvProfileId == null || csvProfileId.isBlank() ? CsvExportProfile.MINUTES_V1 : csvProfileId.trim();
    csvColumns = csvColumns == null ? List.of() : List.copyOf(csvColumns);
    for (String col : csvColumns) {
      CsvColumn.parse(col);
    }
    WorkSchedule schedule = toSchedule(scheduledStart, scheduledEnd, breakMinutes, breakMode);
    scheduledStart = schedule.scheduledStart().format(HM);
    scheduledEnd = schedule.scheduledEnd().format(HM);
    breakMinutes = schedule.breakMinutes();
    breakMode = schedule.breakMode().wire();
  }

  public WorkSchedule workSchedule() {
    return toSchedule(scheduledStart, scheduledEnd, breakMinutes, breakMode);
  }

  public static OrgPeriodSettings defaults(String orgId) {
    WorkSchedule s = WorkSchedule.defaults();
    return new OrgPeriodSettings(
        orgId,
        1,
        0,
        CsvExportProfile.MINUTES_V1,
        true,
        List.of(),
        s.scheduledStart().format(HM),
        s.scheduledEnd().format(HM),
        s.breakMinutes(),
        s.breakMode().wire());
  }

  private static WorkSchedule toSchedule(String start, String end, int breakMinutes, String breakMode) {
    WorkSchedule.BreakMode mode = WorkSchedule.BreakMode.fromWire(breakMode);
    String s = start == null || start.isBlank() ? "09:00" : start;
    String e = end == null || end.isBlank() ? "18:00" : end;
    int bm = breakMinutes < 0 ? 60 : breakMinutes;
    return new WorkSchedule(
        WorkSchedule.parseTime(s, "scheduledStart"),
        WorkSchedule.parseTime(e, "scheduledEnd"),
        bm,
        mode);
  }
}
