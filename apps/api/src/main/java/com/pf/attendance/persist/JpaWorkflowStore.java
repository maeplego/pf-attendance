package com.pf.attendance.persist;

import com.pf.attendance.app.TimeAllocation;
import com.pf.attendance.app.WorkRequest;
import com.pf.attendance.app.WorkflowStore;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "attendance.store", havingValue = "jpa")
public class JpaWorkflowStore implements WorkflowStore {
  private final WorkRequestJpaRepository requests;
  private final TimeAllocationJpaRepository allocations;
  private final ClosedMonthJpaRepository closed;

  public JpaWorkflowStore(
      WorkRequestJpaRepository requests,
      TimeAllocationJpaRepository allocations,
      ClosedMonthJpaRepository closed) {
    this.requests = requests;
    this.allocations = allocations;
    this.closed = closed;
  }

  @Override
  public void saveRequest(WorkRequest request) {
    requests.save(
        new WorkRequestEntity(
            request.id(),
            request.employeeId(),
            request.type(),
            request.status(),
            request.workDate(),
            request.reason(),
            request.createdAt(),
            request.decidedAt(),
            request.decidedBy()));
  }

  @Override
  public Optional<WorkRequest> findRequest(String id) {
    return requests.findById(id).map(this::toRequest);
  }

  @Override
  public List<WorkRequest> listRequestsForEmployee(String employeeId) {
    return requests.findByEmployeeId(employeeId).stream().map(this::toRequest).toList();
  }

  @Override
  public List<WorkRequest> listPending() {
    return requests.findByStatus(WorkRequest.PENDING).stream().map(this::toRequest).toList();
  }

  @Override
  public void saveAllocation(TimeAllocation allocation) {
    allocations.save(
        new TimeAllocationEntity(
            allocation.id(),
            allocation.employeeId(),
            allocation.workDate(),
            allocation.project(),
            allocation.minutes()));
  }

  @Override
  public List<TimeAllocation> listAllocations(String employeeId, LocalDate workDate) {
    return allocations.findByEmployeeIdAndWorkDate(employeeId, workDate).stream()
        .map(
            e ->
                new TimeAllocation(
                    e.getId(), e.getEmployeeId(), e.getWorkDate(), e.getProject(), e.getMinutes()))
        .toList();
  }

  @Override
  public void closeMonth(String orgId, YearMonth month, String actorSub) {
    String key = month.toString();
    if (closed.existsByOrgIdAndMonth(orgId, key)) {
      throw new com.pf.attendance.domain.PeriodClosedException("month already closed");
    }
    closed.save(new ClosedMonthEntity(orgId, key, actorSub));
  }

  @Override
  public boolean isMonthClosed(String orgId, YearMonth month) {
    return closed.existsByOrgIdAndMonth(orgId, month.toString());
  }

  private WorkRequest toRequest(WorkRequestEntity e) {
    return new WorkRequest(
        e.getId(),
        e.getEmployeeId(),
        e.getType(),
        e.getStatus(),
        e.getWorkDate(),
        e.getReason(),
        e.getCreatedAt(),
        e.getDecidedAt(),
        e.getDecidedBy());
  }
}
