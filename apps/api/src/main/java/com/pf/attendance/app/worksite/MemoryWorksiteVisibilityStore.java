package com.pf.attendance.app.worksite;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

@Component
public class MemoryWorksiteVisibilityStore implements WorksiteVisibilityStore {
  private final CopyOnWriteArrayList<CrossOrgAssignment> rows = new CopyOnWriteArrayList<>();

  @Override
  public void save(CrossOrgAssignment assignment) {
    rows.removeIf(
        a ->
            a.worksiteOrgId().equals(assignment.worksiteOrgId())
                && a.employerOrgId().equals(assignment.employerOrgId())
                && a.employeeSub().equals(assignment.employeeSub()));
    rows.add(assignment);
  }

  @Override
  public List<CrossOrgAssignment> findByWorksiteOrgId(String worksiteOrgId) {
    List<CrossOrgAssignment> out = new ArrayList<>();
    for (CrossOrgAssignment a : rows) {
      if (a.worksiteOrgId().equals(worksiteOrgId)) {
        out.add(a);
      }
    }
    return List.copyOf(out);
  }

  @Override
  public boolean isEmptyForWorksite(String worksiteOrgId) {
    return findByWorksiteOrgId(worksiteOrgId).isEmpty();
  }

  public void clear() {
    rows.clear();
  }
}
