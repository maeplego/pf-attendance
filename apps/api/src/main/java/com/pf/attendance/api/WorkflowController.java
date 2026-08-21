package com.pf.attendance.api;

import com.pf.attendance.app.AttendanceService;
import com.pf.attendance.app.Employee;
import com.pf.attendance.app.TimeAllocation;
import com.pf.attendance.app.WorkRequest;
import com.pf.attendance.security.EmployeePrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WorkflowController {
  private final AttendanceService attendance;

  public WorkflowController(AttendanceService attendance) {
    this.attendance = attendance;
  }

  @PostMapping("/v1/requests")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> submit(HttpServletRequest request, @Valid @RequestBody RequestBodyDto body) {
    Employee employee = EmployeePrincipal.require(request);
    WorkRequest row =
        attendance.submitRequest(employee.id(), body.type(), LocalDate.parse(body.workDate()), body.reason());
    return toRequest(row);
  }

  @GetMapping("/v1/requests")
  public Map<String, Object> list(HttpServletRequest request) {
    Employee employee = EmployeePrincipal.require(request);
    return Map.of("requests", attendance.myRequests(employee.id()).stream().map(WorkflowController::toRequest).toList());
  }

  @GetMapping("/v1/approvals")
  public Map<String, Object> inbox(HttpServletRequest request) {
    Employee employee = EmployeePrincipal.require(request);
    return Map.of("requests", attendance.inbox(employee).stream().map(WorkflowController::toRequest).toList());
  }

  @PostMapping("/v1/requests/{id}/decision")
  public Map<String, Object> decide(
      HttpServletRequest request, @PathVariable String id, @Valid @RequestBody DecisionBody body) {
    Employee employee = EmployeePrincipal.require(request);
    return toRequest(attendance.decide(employee, id, body.approve()));
  }

  @PostMapping("/v1/allocations")
  @ResponseStatus(HttpStatus.CREATED)
  public Map<String, Object> allocate(HttpServletRequest request, @Valid @RequestBody AllocationBody body) {
    Employee employee = EmployeePrincipal.require(request);
    TimeAllocation row =
        attendance.allocate(employee.id(), LocalDate.parse(body.workDate()), body.project(), body.minutes());
    return toAlloc(row);
  }

  @GetMapping("/v1/allocations")
  public Map<String, Object> allocations(HttpServletRequest request, @RequestParam String date) {
    Employee employee = EmployeePrincipal.require(request);
    List<TimeAllocation> rows = attendance.allocations(employee.id(), LocalDate.parse(date));
    return Map.of("allocations", rows.stream().map(WorkflowController::toAlloc).toList());
  }

  @PostMapping("/v1/months/{month}/close")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void close(HttpServletRequest request, @PathVariable String month) {
    Employee employee = EmployeePrincipal.require(request);
    attendance.closeMonth(employee, YearMonth.parse(month));
  }

  @GetMapping("/v1/months/{month}/export.csv")
  public ResponseEntity<String> export(
      HttpServletRequest request,
      @PathVariable String month,
      @RequestParam(required = false) String profile,
      @RequestParam(required = false) Boolean header,
      @RequestParam(required = false) String columns) {
    Employee employee = EmployeePrincipal.require(request);
    List<String> cols =
        columns == null || columns.isBlank()
            ? List.of()
            : List.of(columns.split(","));
    String csv =
        attendance.monthCsv(employee, YearMonth.parse(month), profile, header, cols);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"attendance-" + month + ".csv\"")
        // P16 payroll ingest contract: minutes only, no yen/tax columns.
        .header("X-Attendance-Export-Contract", "minutes-v1")
        .contentType(MediaType.parseMediaType("text/csv"))
        .body(csv);
  }

  @GetMapping("/v1/months/{month}/timesheet.pdf")
  public ResponseEntity<byte[]> timesheetPdf(
      HttpServletRequest request,
      @PathVariable String month,
      @RequestParam(required = false) String employeeSub) {
    Employee actor = EmployeePrincipal.require(request);
    byte[] pdf = attendance.monthPdf(actor, YearMonth.parse(month), employeeSub);
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"timesheet-" + month + ".pdf\"")
        .contentType(MediaType.APPLICATION_PDF)
        .body(pdf);
  }

  @GetMapping("/v1/reminders/unpunched")
  public Map<String, Object> unpunched(HttpServletRequest request, @RequestParam String date) {
    Employee actor = EmployeePrincipal.require(request);
    return Map.of(
        "date",
        date,
        "employees",
        attendance.unpunched(actor, LocalDate.parse(date)).stream()
            .map(
                e ->
                    Map.of(
                        "sub", e.sub(),
                        "displayName", e.displayName(),
                        "role", e.role(),
                        "engagement", e.engagement(),
                        "worksiteCode", e.worksiteCode(),
                        "worksiteName", e.worksiteName()))
            .toList());
  }

  static Map<String, Object> toRequest(WorkRequest row) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("id", row.id());
    out.put("employeeId", row.employeeId());
    out.put("type", row.type());
    out.put("status", row.status());
    out.put("workDate", row.workDate().toString());
    out.put("reason", row.reason());
    out.put("createdAt", row.createdAt().toString());
    out.put("decidedAt", row.decidedAt() == null ? null : row.decidedAt().toString());
    out.put("decidedBy", row.decidedBy());
    return out;
  }

  static Map<String, Object> toAlloc(TimeAllocation row) {
    return Map.of(
        "id", row.id(),
        "employeeId", row.employeeId(),
        "workDate", row.workDate().toString(),
        "project", row.project(),
        "minutes", row.minutes());
  }

  public record RequestBodyDto(@NotBlank String type, @NotBlank String workDate, @NotBlank String reason) {}

  public record DecisionBody(boolean approve) {}

  public record AllocationBody(@NotBlank String workDate, @NotBlank String project, @Positive int minutes) {}
}
