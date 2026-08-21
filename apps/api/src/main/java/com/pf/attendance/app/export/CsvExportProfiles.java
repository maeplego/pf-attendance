package com.pf.attendance.app.export;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CsvExportProfiles {
  private CsvExportProfiles() {}

  public static final CsvExportProfile MINUTES_V1 =
      new CsvExportProfile(
          CsvExportProfile.MINUTES_V1,
          "P16 minutes-v1 (default)",
          true,
          List.of(
              CsvColumn.SUB,
              CsvColumn.DISPLAY_NAME,
              CsvColumn.WORK_DATE,
              CsvColumn.WORK_MINUTES,
              CsvColumn.BREAK_MINUTES,
              CsvColumn.STATUS,
              CsvColumn.ENGAGEMENT,
              CsvColumn.WORKSITE_CODE,
              CsvColumn.WORKSITE_NAME));

  /** Illustrative generic ERP-ish layout — not a vendor-certified format. */
  public static final CsvExportProfile ERP_GENERIC_JA =
      new CsvExportProfile(
          CsvExportProfile.ERP_GENERIC_JA,
          "Generic ERP-ish example (NOT vendor-certified)",
          true,
          List.of(
              CsvColumn.SUB,
              CsvColumn.WORK_DATE,
              CsvColumn.WORK_MINUTES,
              CsvColumn.BREAK_MINUTES,
              CsvColumn.STATUS,
              CsvColumn.PROVISIONAL));

  public static Map<String, CsvExportProfile> catalog() {
    Map<String, CsvExportProfile> out = new LinkedHashMap<>();
    out.put(MINUTES_V1.id(), MINUTES_V1);
    out.put(ERP_GENERIC_JA.id(), ERP_GENERIC_JA);
    return out;
  }

  public static CsvExportProfile resolve(String profileId, boolean includeHeader, List<CsvColumn> customColumns) {
    if (CsvExportProfile.CUSTOM.equals(profileId) || (customColumns != null && !customColumns.isEmpty())) {
      List<CsvColumn> cols =
          customColumns == null || customColumns.isEmpty() ? MINUTES_V1.columns() : List.copyOf(customColumns);
      return new CsvExportProfile(CsvExportProfile.CUSTOM, "Custom column order", includeHeader, cols);
    }
    CsvExportProfile preset = catalog().getOrDefault(profileId, MINUTES_V1);
    if (includeHeader != preset.includeHeader()) {
      return new CsvExportProfile(preset.id(), preset.label(), includeHeader, preset.columns());
    }
    return preset;
  }
}
