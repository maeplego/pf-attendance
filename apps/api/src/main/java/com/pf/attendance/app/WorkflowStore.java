package com.pf.attendance.app;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

public interface WorkflowStore {
  void saveRequest(WorkRequest request);

  Optional<WorkRequest> findRequest(String id);

  List<WorkRequest> listRequestsForEmployee(String employeeId);

  List<WorkRequest> listPending();

  void saveAllocation(TimeAllocation allocation);

  List<TimeAllocation> listAllocations(String employeeId, LocalDate workDate);

  void closeMonth(String orgId, YearMonth month, String actorSub);

  boolean isMonthClosed(String orgId, YearMonth month);
}
