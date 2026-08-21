package com.pf.attendance.api;

import com.pf.attendance.app.AttendanceService;
import com.pf.attendance.app.Employee;
import com.pf.attendance.domain.PunchEvent;
import com.pf.attendance.domain.PunchType;
import com.pf.attendance.security.EmployeePrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
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
    LocalDate workDate =
        body.workDate() == null || body.workDate().isBlank() ? null : LocalDate.parse(body.workDate());
    LocalTime at =
        body.at() == null || body.at().isBlank() ? null : LocalTime.parse(body.at());
    PunchEvent event = attendance.punch(employee.id(), type, workDate, at);
    return toJson(event);
  }

  @PostMapping("/v1/me/days/{date}/apply-schedule")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> applySchedule(
      HttpServletRequest request, @PathVariable String date) {
    Employee employee = EmployeePrincipal.require(request);
    List<PunchEvent> events = attendance.applyScheduleDay(employee.id(), LocalDate.parse(date));
    return Map.of("punches", events.stream().map(PunchController::toJson).toList());
  }

  static Map<String, Object> toJson(PunchEvent event) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("id", event.id());
    out.put("employeeId", event.employeeId());
    out.put("type", event.type().wire());
    out.put("punchedAt", event.punchedAt().toString());
    out.put("workDate", event.workDate().toString());
    out.put("source", event.source());
    return out;
  }

  public record PunchRequest(@NotBlank String type, String workDate, String at) {}
}
