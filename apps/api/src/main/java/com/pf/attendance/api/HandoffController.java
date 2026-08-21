package com.pf.attendance.api;

import com.pf.attendance.app.AttendanceService;
import com.pf.attendance.app.Employee;
import com.pf.attendance.app.handoff.HandoffReceipt;
import com.pf.attendance.app.handoff.TimesheetHandoffPort;
import com.pf.attendance.security.EmployeePrincipal;
import jakarta.servlet.http.HttpServletRequest;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HandoffController {
  private final AttendanceService attendance;

  public HandoffController(AttendanceService attendance) {
    this.attendance = attendance;
  }

  @GetMapping("/v1/months/{month}/handoff.csv")
  public ResponseEntity<String> export(HttpServletRequest request, @PathVariable String month) {
    Employee actor = EmployeePrincipal.require(request);
    String csv = attendance.exportHandoffCsv(actor, YearMonth.parse(month));
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"handoff-" + month + ".csv\"")
        .header("X-Attendance-Handoff-Contract", TimesheetHandoffPort.CONTRACT)
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(csv);
  }

  @PostMapping(value = "/v1/months/{month}/handoffs", consumes = "text/csv")
  public Map<String, Object> ingest(
      HttpServletRequest request,
      @PathVariable String month,
      @RequestParam(required = false, defaultValue = "csv-upload") String sourceHint,
      @RequestBody String body) {
    Employee actor = EmployeePrincipal.require(request);
    HandoffReceipt receipt =
        attendance.ingestHandoffCsv(actor, YearMonth.parse(month), body, sourceHint);
    return toReceipt(receipt);
  }

  @GetMapping("/v1/months/{month}/handoffs")
  public Map<String, Object> list(HttpServletRequest request, @PathVariable String month) {
    Employee actor = EmployeePrincipal.require(request);
    return Map.of(
        "month",
        month,
        "receipts",
        attendance.listHandoffs(actor, YearMonth.parse(month)).stream()
            .map(HandoffController::toReceipt)
            .toList());
  }

  static Map<String, Object> toReceipt(HandoffReceipt r) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("id", r.id());
    out.put("employerOrgId", r.employerOrgId());
    out.put("month", r.month().toString());
    out.put("sourceHint", r.sourceHint());
    out.put("acceptedAt", r.acceptedAt().toString());
    out.put("acceptedBySub", r.acceptedBySub());
    out.put("lineCount", r.lines().size());
    return out;
  }
}
