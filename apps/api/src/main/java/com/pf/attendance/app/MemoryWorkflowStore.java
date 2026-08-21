package com.pf.attendance.app;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class MemoryWorkflowStore implements WorkflowStore {
  private final ConcurrentHashMap<String, WorkRequest> requests = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, TimeAllocation> allocations = new ConcurrentHashMap<>();
  private final ConcurrentHashMap<String, String> closed = new ConcurrentHashMap<>();

  @Override
  public void saveRequest(WorkRequest request) {
    requests.put(request.id(), request);
  }

  @Override
  public Optional<WorkRequest> findRequest(String id) {
    return Optional.ofNullable(requests.get(id));
  }

  @Override
  public List<WorkRequest> listRequestsForEmployee(String employeeId) {
    List<WorkRequest> out = new ArrayList<>();
    for (WorkRequest row : requests.values()) {
      if (row.employeeId().equals(employeeId)) {
        out.add(row);
      }
    }
    return List.copyOf(out);
  }

  @Override
  public List<WorkRequest> listPending() {
    List<WorkRequest> out = new ArrayList<>();
    for (WorkRequest row : requests.values()) {
      if (WorkRequest.PENDING.equals(row.status())) {
        out.add(row);
      }
    }
    return List.copyOf(out);
  }

  @Override
  public void saveAllocation(TimeAllocation allocation) {
    allocations.put(allocation.id(), allocation);
  }

  @Override
  public List<TimeAllocation> listAllocations(String employeeId, LocalDate workDate) {
    List<TimeAllocation> out = new ArrayList<>();
    for (TimeAllocation row : allocations.values()) {
      if (row.employeeId().equals(employeeId) && row.workDate().equals(workDate)) {
        out.add(row);
      }
    }
    return List.copyOf(out);
  }

  @Override
  public void closeMonth(String orgId, YearMonth month, String actorSub) {
    closed.put(closedKey(orgId, month), actorSub);
  }

  @Override
  public boolean isMonthClosed(String orgId, YearMonth month) {
    return closed.containsKey(closedKey(orgId, month));
  }

  private static String closedKey(String orgId, YearMonth month) {
    return orgId + "|" + month;
  }
}
