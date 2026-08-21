package com.pf.attendance.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class MemoryEmployeeStore implements EmployeeStore {
  private final ConcurrentHashMap<String, Employee> byId = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, String> idByOrgAndSub = new ConcurrentHashMap<>();

  @Override
  public Optional<Employee> findByOrgIdAndSub(String orgId, String sub) {
    String id = idByOrgAndSub.get(key(orgId, sub));
    return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
  }

  @Override
  public Optional<Employee> findById(String id) {
    return Optional.ofNullable(byId.get(id));
  }

  @Override
  public void save(Employee employee) {
    byId.put(employee.id(), employee);
    idByOrgAndSub.put(key(employee.orgId(), employee.sub()), employee.id());
  }

  @Override
  public List<Employee> findAllByOrgId(String orgId) {
    List<Employee> out = new ArrayList<>();
    for (Employee employee : byId.values()) {
      if (orgId.equals(employee.orgId())) {
        out.add(employee);
      }
    }
    return List.copyOf(out);
  }

  @Override
  public boolean isEmptyForOrg(String orgId) {
    return findAllByOrgId(orgId).isEmpty();
  }

  private static String key(String orgId, String sub) {
    return orgId + "\0" + sub;
  }
}
