package com.pf.attendance.persist;

import com.pf.attendance.app.Employee;
import com.pf.attendance.app.EmployeeStore;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "attendance.store", havingValue = "jpa")
public class JpaEmployeeStore implements EmployeeStore {
  private final EmployeeJpaRepository repo;

  public JpaEmployeeStore(EmployeeJpaRepository repo) {
    this.repo = repo;
  }

  @Override
  public Optional<Employee> findBySub(String sub) {
    return repo.findBySub(sub).map(this::toDomain);
  }

  @Override
  public Optional<Employee> findById(String id) {
    return repo.findById(id).map(this::toDomain);
  }

  @Override
  public void save(Employee employee) {
    repo.save(
        new EmployeeEntity(employee.id(), employee.sub(), employee.displayName(), employee.role()));
  }

  @Override
  public List<Employee> findAll() {
    return repo.findAll().stream().map(this::toDomain).toList();
  }

  @Override
  public boolean isEmpty() {
    return repo.count() == 0;
  }

  private Employee toDomain(EmployeeEntity entity) {
    return new Employee(entity.getId(), entity.getSub(), entity.getDisplayName(), entity.getRole());
  }
}
