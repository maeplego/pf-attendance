package com.pf.attendance.app;

public record Employee(
    String id,
    String orgId,
    String sub,
    String displayName,
    String role,
    String engagement,
    String worksiteCode,
    String worksiteName) {

  public Employee {
    engagement = Engagement.normalize(engagement);
    worksiteCode = worksiteCode == null ? "" : worksiteCode.trim();
    worksiteName = worksiteName == null ? "" : worksiteName.trim();
  }

  /** In-house employee helper (empty worksite). */
  public static Employee employed(String id, String orgId, String sub, String displayName, String role) {
    return new Employee(id, orgId, sub, displayName, role, Engagement.EMPLOYED, "", "");
  }

  public static Employee clientSite(
      String id,
      String orgId,
      String sub,
      String displayName,
      String role,
      String worksiteCode,
      String worksiteName) {
    return new Employee(
        id, orgId, sub, displayName, role, Engagement.CLIENT_SITE, worksiteCode, worksiteName);
  }
}
