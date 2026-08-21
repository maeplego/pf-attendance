package com.pf.attendance.api;

import com.pf.attendance.app.AttendanceService;
import com.pf.attendance.app.Employee;
import com.pf.attendance.app.ProvisionalDay;
import com.pf.attendance.security.EmployeePrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProvisionalController {
  private final AttendanceService attendance;

  public ProvisionalController(AttendanceService attendance) {
    this.attendance = attendance;
  }

  @PutMapping("/v1/me/provisional-days")
  @ResponseStatus(HttpStatus.OK)
  public Map<String, Object> put(HttpServletRequest request, @RequestBody Body body) {
    Employee actor = EmployeePrincipal.require(request);
    ProvisionalDay row =
        attendance.putProvisional(
            actor,
            LocalDate.parse(body.workDate()),
            body.workMinutes(),
            body.breakMinutes() == null ? 0 : body.breakMinutes(),
            body.note());
    return Map.of(
        "employeeId",
        row.employeeId(),
        "workDate",
        row.workDate().toString(),
        "workMinutes",
        row.workMinutes(),
        "breakMinutes",
        row.breakMinutes(),
        "note",
        row.note(),
        "provisional",
        true);
  }

  public record Body(
      @NotBlank String workDate,
      @Min(0) int workMinutes,
      Integer breakMinutes,
      String note) {}
}
