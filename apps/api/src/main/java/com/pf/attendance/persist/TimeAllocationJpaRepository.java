package com.pf.attendance.persist;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TimeAllocationJpaRepository extends JpaRepository<TimeAllocationEntity, String> {
  List<TimeAllocationEntity> findByEmployeeIdAndWorkDate(String employeeId, LocalDate workDate);
}
