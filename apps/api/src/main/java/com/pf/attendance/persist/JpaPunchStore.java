package com.pf.attendance.persist;

import com.pf.attendance.app.PunchStore;
import com.pf.attendance.domain.PunchEvent;
import java.time.LocalDate;
import java.util.List;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

@Repository
@ConditionalOnProperty(name = "attendance.store", havingValue = "jpa")
public class JpaPunchStore implements PunchStore {
  private final PunchJpaRepository repo;

  public JpaPunchStore(PunchJpaRepository repo) {
    this.repo = repo;
  }

  @Override
  public void append(PunchEvent event) {
    repo.save(
        new PunchEntity(
            event.id(),
            event.employeeId(),
            event.type(),
            event.punchedAt(),
            event.workDate(),
            event.source()));
  }

  @Override
  public List<PunchEvent> findByEmployeeAndWorkDate(String employeeId, LocalDate workDate) {
    return repo.findByEmployeeIdAndWorkDateOrderByPunchedAtAscIdAsc(employeeId, workDate).stream()
        .map(
            entity ->
                new PunchEvent(
                    entity.getId(),
                    entity.getEmployeeId(),
                    entity.getType(),
                    entity.getPunchedAt(),
                    entity.getWorkDate(),
                    entity.getSource()))
        .toList();
  }
}
