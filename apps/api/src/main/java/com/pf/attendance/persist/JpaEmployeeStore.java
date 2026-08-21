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
  public Optional<Employee> findByOrgIdAndSub(String orgId, String sub) {
    return repo.findByOrgIdAndSub(orgId, sub).map(this::toDomain);
  }

  @Override
  public Optional<Employee> findById(String id) {
    return repo.findById(id).map(this::toDomain);
  }

  @Override
  public void save(Employee employee) {
    repo.save(
        new EmployeeEntity(
            employee.id(), employee.orgId(), employee.sub(), employee.displayName(), employee.role()));
  }

  @Override
  public List<Employee> findAllByOrgId(String orgId) {
    return repo.findByOrgId(orgId).stream().map(this::toDomain).toList();
  }

  @Override
  public boolean isEmptyForOrg(String orgId) {
    return repo.countByOrgId(orgId) == 0;
  }

  private Employee toDomain(EmployeeEntity entity) {
    return new Employee(
        entity.getId(), entity.getOrgId(), entity.getSub(), entity.getDisplayName(), entity.getRole());
  }
}
