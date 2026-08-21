package com.pf.attendance.app;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class MemoryOrgSettingsStore implements OrgSettingsStore {
  private final Map<String, OrgPeriodSettings> byOrg = new ConcurrentHashMap<>();

  @Override
  public OrgPeriodSettings getOrDefault(String orgId) {
    return byOrg.getOrDefault(orgId, OrgPeriodSettings.defaults(orgId));
  }

  @Override
  public void save(OrgPeriodSettings settings) {
    byOrg.put(settings.orgId(), settings);
  }

  @Override
  public Optional<OrgPeriodSettings> find(String orgId) {
    return Optional.ofNullable(byOrg.get(orgId));
  }

  public void clear() {
    byOrg.clear();
  }
}
