package com.pf.attendance.config;

import com.pf.attendance.app.DemoEmployees;
import com.pf.attendance.app.Employee;
import com.pf.attendance.app.EmployeeStore;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "attendance.seed-demo", havingValue = "true", matchIfMissing = true)
public class DemoSeed implements ApplicationRunner {
  private final EmployeeStore employees;

  public DemoSeed(EmployeeStore employees) {
    this.employees = employees;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (!employees.isEmpty()) {
      return;
    }
    for (Employee employee : DemoEmployees.roster()) {
      employees.save(employee);
    }
  }
}
