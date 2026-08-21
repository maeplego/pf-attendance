package com.pf.attendance.app.export;

import java.util.List;

public record CsvExportProfile(String id, String label, boolean includeHeader, List<CsvColumn> columns) {
  public static final String MINUTES_V1 = "minutes-v1";
  public static final String ERP_GENERIC_JA = "erp-generic-ja";
  public static final String CUSTOM = "custom";
}
