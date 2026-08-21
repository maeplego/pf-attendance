package com.pf.attendance.app;

import com.pf.attendance.app.export.CsvColumn;
import com.pf.attendance.app.export.CsvExportProfile;
import com.pf.attendance.domain.AttendancePeriods;
import java.util.List;

public record OrgPeriodSettings(
    String orgId,
    int periodAnchorDay,
    int closeByDay,
    String csvProfileId,
    boolean csvIncludeHeader,
    List<String> csvColumns) {

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
  }

  public static OrgPeriodSettings defaults(String orgId) {
    return new OrgPeriodSettings(orgId, 1, 0, CsvExportProfile.MINUTES_V1, true, List.of());
  }
}
