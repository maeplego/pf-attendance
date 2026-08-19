package com.pf.attendance.api;

import com.pf.attendance.config.AttendanceProperties;
import java.sql.Connection;
import java.util.Map;
import javax.sql.DataSource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
  private final AttendanceProperties properties;
  private final ObjectProvider<DataSource> dataSources;

  public HealthController(AttendanceProperties properties, ObjectProvider<DataSource> dataSources) {
    this.properties = properties;
    this.dataSources = dataSources;
  }

  @GetMapping("/health")
  public Map<String, Boolean> health() {
    return Map.of("ok", true);
  }

  @GetMapping("/ready")
  public ResponseEntity<Map<String, Boolean>> ready() {
    if (!"jpa".equals(properties.getStore())) {
      return ResponseEntity.ok(Map.of("ok", true));
    }
    DataSource ds = dataSources.getIfAvailable();
    if (ds == null) {
      return ResponseEntity.status(503).body(Map.of("ok", false));
    }
    try (Connection conn = ds.getConnection()) {
      if (conn.isValid(2)) {
        return ResponseEntity.ok(Map.of("ok", true));
      }
    } catch (Exception ex) {
      return ResponseEntity.status(503).body(Map.of("ok", false));
    }
    return ResponseEntity.status(503).body(Map.of("ok", false));
  }
}
