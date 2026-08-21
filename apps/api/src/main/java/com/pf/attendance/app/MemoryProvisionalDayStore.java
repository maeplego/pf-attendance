package com.pf.attendance.app;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class MemoryProvisionalDayStore implements ProvisionalDayStore {
  private final Map<String, ProvisionalDay> byKey = new ConcurrentHashMap<>();

  private static String key(String employeeId, LocalDate workDate) {
    return employeeId + "|" + workDate;
  }

  @Override
  public void save(ProvisionalDay day) {
    byKey.put(key(day.employeeId(), day.workDate()), day);
  }

  @Override
  public Optional<ProvisionalDay> find(String employeeId, LocalDate workDate) {
    return Optional.ofNullable(byKey.get(key(employeeId, workDate)));
  }

  @Override
  public void delete(String employeeId, LocalDate workDate) {
    byKey.remove(key(employeeId, workDate));
  }

  public void clear() {
    byKey.clear();
  }
}
