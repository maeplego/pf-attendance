package com.pf.attendance.app;

import java.util.List;
import java.util.Optional;

public interface EmployeeStore {
  Optional<Employee> findBySub(String sub);

  Optional<Employee> findById(String id);

  void save(Employee employee);

  List<Employee> findAll();

  boolean isEmpty();
}
