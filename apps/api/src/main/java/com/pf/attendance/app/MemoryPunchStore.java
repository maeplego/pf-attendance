package com.pf.attendance.app;

import com.pf.attendance.domain.PunchEvent;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class MemoryPunchStore implements PunchStore {
  private final List<PunchEvent> events = new CopyOnWriteArrayList<>();

  @Override
  public void append(PunchEvent event) {
    events.add(event);
  }

  @Override
  public List<PunchEvent> findByEmployeeAndWorkDate(String employeeId, LocalDate workDate) {
    List<PunchEvent> out = new ArrayList<>();
    for (PunchEvent event : events) {
      if (event.employeeId().equals(employeeId) && event.workDate().equals(workDate)) {
        out.add(event);
      }
    }
    return List.copyOf(out);
  }
}
