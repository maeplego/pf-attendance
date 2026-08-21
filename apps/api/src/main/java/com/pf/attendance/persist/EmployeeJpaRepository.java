package com.pf.attendance.persist;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeJpaRepository extends JpaRepository<EmployeeEntity, String> {
  Optional<EmployeeEntity> findByOrgIdAndSub(String orgId, String sub);

  List<EmployeeEntity> findByOrgId(String orgId);

  long countByOrgId(String orgId);
}
