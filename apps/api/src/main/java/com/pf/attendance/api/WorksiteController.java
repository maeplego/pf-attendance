package com.pf.attendance.api;

import com.pf.attendance.app.AttendanceService;
import com.pf.attendance.app.Employee;
import com.pf.attendance.app.worksite.VisibleMember;
import com.pf.attendance.security.EmployeePrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorksiteController {
  private final AttendanceService attendance;

  public WorksiteController(AttendanceService attendance) {
    this.attendance = attendance;
  }

  @GetMapping("/v1/worksite/visible-members")
  public Map<String, Object> visibleMembers(HttpServletRequest request) {
    Employee actor = EmployeePrincipal.require(request);
    return Map.of(
        "orgId",
        actor.orgId(),
        "members",
        attendance.listVisibleMembers(actor).stream().map(WorksiteController::toMember).toList());
  }

  static Map<String, Object> toMember(VisibleMember m) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("sub", m.sub());
    out.put("displayName", m.displayName());
    out.put("role", m.role());
    out.put("kind", m.kind());
    out.put("employerOrgId", m.employerOrgId());
    out.put("worksiteCode", m.worksiteCode());
    out.put("worksiteName", m.worksiteName());
    out.put("payrollOwnedHere", VisibleMember.KIND_LOCAL.equals(m.kind()));
    return out;
  }
}
