package com.pf.attendance.api;

import com.pf.attendance.app.AttendanceService;
import com.pf.attendance.app.Employee;
import com.pf.attendance.app.OrgPeriodSettings;
import com.pf.attendance.security.EmployeePrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrgSettingsController {
  private final AttendanceService attendance;

  public OrgSettingsController(AttendanceService attendance) {
    this.attendance = attendance;
  }

  @GetMapping("/v1/org/period-settings")
  public Map<String, Object> get(HttpServletRequest request) {
    Employee actor = EmployeePrincipal.require(request);
    OrgPeriodSettings s = attendance.getPeriodSettings(actor);
    return Map.of("orgId", s.orgId(), "periodAnchorDay", s.periodAnchorDay());
  }

  @PutMapping("/v1/org/period-settings")
  public Map<String, Object> put(HttpServletRequest request, @RequestBody PeriodBody body) {
    Employee actor = EmployeePrincipal.require(request);
    OrgPeriodSettings s = attendance.putPeriodSettings(actor, body.periodAnchorDay());
    return Map.of("orgId", s.orgId(), "periodAnchorDay", s.periodAnchorDay());
  }

  public record PeriodBody(@Min(1) @Max(28) int periodAnchorDay) {}
}
