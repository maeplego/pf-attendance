package com.pf.attendance.persist;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EmployeeJpaRepository extends JpaRepository<EmployeeEntity, String> {
  Optional<EmployeeEntity> findBySub(String sub);
}
