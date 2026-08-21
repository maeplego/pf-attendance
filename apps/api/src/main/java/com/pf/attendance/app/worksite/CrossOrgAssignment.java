package com.pf.attendance.app.worksite;

/** Read-only visibility of an employer employee at a worksite org (SES Stage C). */
public record CrossOrgAssignment(
    String id,
    String employerOrgId,
    String worksiteOrgId,
    String employeeSub,
    String worksiteCode,
    String worksiteName) {}
