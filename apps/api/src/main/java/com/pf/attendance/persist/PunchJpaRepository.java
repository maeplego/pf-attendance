package com.pf.attendance.persist;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PunchJpaRepository extends JpaRepository<PunchEntity, String> {
  List<PunchEntity> findByEmployeeIdAndWorkDateOrderByPunchedAtAscIdAsc(
      String employeeId, LocalDate workDate);
}
