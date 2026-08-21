package com.pf.attendance.app;

import java.util.Optional;

public interface OrgSettingsStore {
  OrgPeriodSettings getOrDefault(String orgId);

  void save(OrgPeriodSettings settings);

  Optional<OrgPeriodSettings> find(String orgId);
}
