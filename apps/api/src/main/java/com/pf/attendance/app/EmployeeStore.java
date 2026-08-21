package com.pf.attendance.app;

import java.util.List;
import java.util.Optional;

public interface EmployeeStore {
  Optional<Employee> findByOrgIdAndSub(String orgId, String sub);

  Optional<Employee> findById(String id);

  void save(Employee employee);

  List<Employee> findAllByOrgId(String orgId);

  boolean isEmptyForOrg(String orgId);
}
