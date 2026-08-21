package com.pf.attendance.api;

import com.pf.attendance.app.AttendanceService;
import com.pf.attendance.app.Employee;
import com.pf.attendance.domain.DailySummary;
import com.pf.attendance.domain.WorkDates;
import com.pf.attendance.security.EmployeePrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DailySummaryController {
  private final AttendanceService attendance;
  private final Clock clock;

  public DailySummaryController(AttendanceService attendance, Clock clock) {
    this.attendance = attendance;
    this.clock = clock;
  }

  @GetMapping("/v1/me/daily-summary")
  public Map<String, Object> dailySummary(
      HttpServletRequest request, @RequestParam(required = false) String date) {
    Employee employee = EmployeePrincipal.require(request);
    LocalDate workDate =
        date == null || date.isBlank() ? WorkDates.of(Instant.now(clock)) : LocalDate.parse(date);
    DailySummary summary = attendance.dailySummary(employee.id(), workDate);
    List<Map<String, Object>> punches =
        summary.punches().stream().map(PunchController::toJson).toList();
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("workDate", summary.workDate().toString());
    out.put("workMinutes", summary.workMinutes());
    out.put("breakMinutes", summary.breakMinutes());
    out.put("status", summary.status().name().toLowerCase(Locale.ROOT));
    out.put("punches", punches);
    out.put("provisional", summary.provisional());
    out.put("leaveKind", summary.leaveKind());
    out.put("lateMinutes", summary.lateMinutes());
    out.put("earlyLeaveMinutes", summary.earlyLeaveMinutes());
    out.put("overtimeMinutes", summary.overtimeMinutes());
    return out;
  }
}
