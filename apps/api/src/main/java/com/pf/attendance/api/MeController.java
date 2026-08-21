package com.pf.attendance.api;

import com.pf.attendance.app.Employee;
import com.pf.attendance.domain.WorkDates;
import com.pf.attendance.security.EmployeePrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MeController {
  @GetMapping("/v1/me")
  public Map<String, String> me(HttpServletRequest request) {
    Employee employee = EmployeePrincipal.require(request);
    return Map.of(
        "id", employee.id(),
        "sub", employee.sub(),
        "displayName", employee.displayName(),
        "role", employee.role(),
        "engagement", employee.engagement(),
        "worksiteCode", employee.worksiteCode(),
        "worksiteName", employee.worksiteName(),
        "zone", WorkDates.ZONE.getId());
  }
}
