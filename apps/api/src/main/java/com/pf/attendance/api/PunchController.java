package com.pf.attendance.api;

import com.pf.attendance.app.AttendanceService;
import com.pf.attendance.app.Employee;
import com.pf.attendance.domain.PunchEvent;
import com.pf.attendance.domain.PunchType;
import com.pf.attendance.security.EmployeePrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PunchController {
  private final AttendanceService attendance;

  public PunchController(AttendanceService attendance) {
    this.attendance = attendance;
  }

  @PostMapping("/v1/punches")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> punch(
      HttpServletRequest request, @Valid @RequestBody PunchRequest body) {
    Employee employee = EmployeePrincipal.require(request);
    PunchType type = PunchType.fromWire(body.type());
    PunchEvent event = attendance.punch(employee.id(), type);
    return toJson(event);
  }

  static Map<String, Object> toJson(PunchEvent event) {
    return Map.of(
        "id", event.id(),
        "employeeId", event.employeeId(),
        "type", event.type().wire(),
        "punchedAt", event.punchedAt().toString(),
        "workDate", event.workDate().toString(),
        "source", event.source());
  }

  public record PunchRequest(@NotBlank String type) {}
}
