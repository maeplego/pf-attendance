package com.pf.attendance.app;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class MemoryEmployeeStore implements EmployeeStore {
  private final ConcurrentHashMap<String, Employee> byId = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, String> idBySub = new ConcurrentHashMap<>();

  @Override
  public Optional<Employee> findBySub(String sub) {
    String id = idBySub.get(sub);
    return id == null ? Optional.empty() : Optional.ofNullable(byId.get(id));
  }

  @Override
  public Optional<Employee> findById(String id) {
    return Optional.ofNullable(byId.get(id));
  }

  @Override
  public void save(Employee employee) {
    byId.put(employee.id(), employee);
    idBySub.put(employee.sub(), employee.id());
  }

  @Override
  public List<Employee> findAll() {
    return List.copyOf(new ArrayList<>(byId.values()));
  }

  @Override
  public boolean isEmpty() {
    return byId.isEmpty();
  }
}
