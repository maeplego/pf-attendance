package com.pf.attendance.persist;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WorkRequestJpaRepository extends JpaRepository<WorkRequestEntity, String> {
  List<WorkRequestEntity> findByEmployeeId(String employeeId);

  List<WorkRequestEntity> findByStatus(String status);
}
