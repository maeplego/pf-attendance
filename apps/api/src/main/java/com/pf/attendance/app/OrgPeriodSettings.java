package com.pf.attendance.app;

public record OrgPeriodSettings(String orgId, int periodAnchorDay) {
  public OrgPeriodSettings {
    periodAnchorDay = com.pf.attendance.domain.AttendancePeriods.normalizeAnchor(periodAnchorDay);
  }
}
