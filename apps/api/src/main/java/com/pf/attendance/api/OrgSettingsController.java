package com.pf.attendance.api;

import com.pf.attendance.app.AttendanceService;
import com.pf.attendance.app.Employee;
import com.pf.attendance.app.OrgPeriodSettings;
import com.pf.attendance.app.export.CsvExportProfiles;
import com.pf.attendance.security.EmployeePrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import java.util.LinkedHashMap;
import java.util.List;
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
    return toMap(attendance.getPeriodSettings(actor));
  }

  @PutMapping("/v1/org/period-settings")
  public Map<String, Object> put(HttpServletRequest request, @RequestBody PeriodBody body) {
    Employee actor = EmployeePrincipal.require(request);
    OrgPeriodSettings cur = attendance.getPeriodSettings(actor);
    OrgPeriodSettings next =
        new OrgPeriodSettings(
            actor.orgId(),
            body.periodAnchorDay() != null ? body.periodAnchorDay() : cur.periodAnchorDay(),
            body.closeByDay() != null ? body.closeByDay() : cur.closeByDay(),
            body.csvProfileId() != null ? body.csvProfileId() : cur.csvProfileId(),
            body.csvIncludeHeader() != null ? body.csvIncludeHeader() : cur.csvIncludeHeader(),
            body.csvColumns() != null ? body.csvColumns() : cur.csvColumns());
    return toMap(attendance.putPeriodSettings(actor, next));
  }

  @GetMapping("/v1/org/csv-profiles")
  public Map<String, Object> csvProfiles() {
    return Map.of(
        "profiles",
        CsvExportProfiles.catalog().values().stream()
            .map(
                p ->
                    Map.of(
                        "id",
                        p.id(),
                        "label",
                        p.label(),
                        "includeHeader",
                        p.includeHeader(),
                        "columns",
                        p.columns().stream().map(c -> c.header()).toList()))
            .toList(),
        "customId",
        "custom");
  }

  static Map<String, Object> toMap(OrgPeriodSettings s) {
    Map<String, Object> out = new LinkedHashMap<>();
    out.put("orgId", s.orgId());
    out.put("periodAnchorDay", s.periodAnchorDay());
    out.put("closeByDay", s.closeByDay());
    out.put("csvProfileId", s.csvProfileId());
    out.put("csvIncludeHeader", s.csvIncludeHeader());
    out.put("csvColumns", s.csvColumns());
    return out;
  }

  public record PeriodBody(
      @Min(1) @Max(28) Integer periodAnchorDay,
      @Min(0) @Max(28) Integer closeByDay,
      String csvProfileId,
      Boolean csvIncludeHeader,
      List<String> csvColumns) {}
}
