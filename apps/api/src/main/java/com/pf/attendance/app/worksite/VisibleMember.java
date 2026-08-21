package com.pf.attendance.app.worksite;

/** Roster row for worksite team board: local employee or cross-org guest (read-only). */
public record VisibleMember(
    String sub,
    String displayName,
    String role,
    String kind,
    String employerOrgId,
    String worksiteCode,
    String worksiteName) {
  public static final String KIND_LOCAL = "local";
  public static final String KIND_GUEST = "guest";
}
