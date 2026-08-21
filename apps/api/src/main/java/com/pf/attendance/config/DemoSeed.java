package com.pf.attendance.config;

import com.pf.attendance.app.DemoEmployees;
import com.pf.attendance.app.Employee;
import com.pf.attendance.app.EmployeeStore;
import com.pf.attendance.app.Ids;
import com.pf.attendance.app.worksite.CrossOrgAssignment;
import com.pf.attendance.app.worksite.WorksiteVisibilityStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "attendance.seed-demo", havingValue = "true", matchIfMissing = true)
public class DemoSeed implements ApplicationRunner {
  private final EmployeeStore employees;
  private final WorksiteVisibilityStore visibility;

  public DemoSeed(EmployeeStore employees, WorksiteVisibilityStore visibility) {
    this.employees = employees;
    this.visibility = visibility;
  }

  @Override
  public void run(ApplicationArguments args) {
    seedOrg(DemoEmployees.ORG_A);
    seedOrg(DemoEmployees.ORG_B);
    seedAssignments();
  }

  private void seedOrg(String orgId) {
    if (!employees.isEmptyForOrg(orgId)) {
      return;
    }
    for (Employee employee : DemoEmployees.roster(orgId)) {
      employees.save(employee);
    }
  }

  /** org-demo-b (worksite) can see SES assignees employed by org-demo-a (read-only guests). */
  private void seedAssignments() {
    if (!visibility.isEmptyForWorksite(DemoEmployees.ORG_B)) {
      return;
    }
    visibility.save(
        new CrossOrgAssignment(
            Ids.ulid(),
            DemoEmployees.ORG_A,
            DemoEmployees.ORG_B,
            "ise.yuto",
            "WS-CLIENT-A",
            "架空商事 本社開発"));
    visibility.save(
        new CrossOrgAssignment(
            Ids.ulid(),
            DemoEmployees.ORG_A,
            DemoEmployees.ORG_B,
            "shima.rena",
            "WS-CLIENT-B",
            "架空銀行 システム部"));
  }
}
