package com.pf.attendance.app;

import com.pf.attendance.domain.PunchEvent;
import java.time.LocalDate;
import java.util.List;

public interface PunchStore {
  void append(PunchEvent event);

  List<PunchEvent> findByEmployeeAndWorkDate(String employeeId, LocalDate workDate);
}
