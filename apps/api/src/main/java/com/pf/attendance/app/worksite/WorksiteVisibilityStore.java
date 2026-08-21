package com.pf.attendance.app.worksite;

import java.util.List;

public interface WorksiteVisibilityStore {
  void save(CrossOrgAssignment assignment);

  List<CrossOrgAssignment> findByWorksiteOrgId(String worksiteOrgId);

  boolean isEmptyForWorksite(String worksiteOrgId);
}
