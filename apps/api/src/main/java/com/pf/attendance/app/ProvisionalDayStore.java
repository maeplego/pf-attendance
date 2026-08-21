package com.pf.attendance.app;

import java.time.LocalDate;
import java.util.Optional;

public interface ProvisionalDayStore {
  void save(ProvisionalDay day);

  Optional<ProvisionalDay> find(String employeeId, LocalDate workDate);

  void delete(String employeeId, LocalDate workDate);
}
