package com.pf.attendance.api;

import com.pf.attendance.app.AttendanceService;
import com.pf.attendance.app.Employee;
import com.pf.attendance.domain.DailySummary;
import com.pf.attendance.domain.MonthSummary;
import com.pf.attendance.security.EmployeePrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MonthSummaryController {
  private final AttendanceService attendance;

  public MonthSummaryController(AttendanceService attendance) {
    this.attendance = attendance;
  }

  @GetMapping("/v1/me/month-summary")
  public Map<String, Object> monthSummary(
      HttpServletRequest request, @RequestParam String month) {
    Employee employee = EmployeePrincipal.require(request);
    YearMonth ym = YearMonth.parse(month);
    MonthSummary summary = attendance.monthSummary(employee.id(), ym);
    List<Map<String, Object>> days = new ArrayList<>();
    for (DailySummary day : summary.days()) {
      Map<String, Object> row = new LinkedHashMap<>();
      row.put("workDate", day.workDate().toString());
      row.put("workMinutes", day.workMinutes());
      row.put("breakMinutes", day.breakMinutes());
      row.put("status", day.status().name().toLowerCase(Locale.ROOT));
      row.put("punchCount", day.punches().size());
      days.add(row);
    }
    return Map.of(
        "month", summary.month().toString(),
        "zone", "Asia/Tokyo",
        "days", days);
  }
}
