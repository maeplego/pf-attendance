package com.pf.attendance.persist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.pf.attendance.domain.PunchType;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "punches")
public class PunchEntity {
  @Id
  @Column(length = 26)
  private String id;

  @Column(name = "employee_id", nullable = false, length = 26)
  private String employeeId;

  @Enumerated(EnumType.STRING)
  @Column(name = "punch_type", nullable = false, length = 32)
  private PunchType type;

  @Column(name = "punched_at", nullable = false)
  private Instant punchedAt;

  @Column(name = "work_date", nullable = false)
  private LocalDate workDate;

  @Column(nullable = false, length = 32)
  private String source;

  public PunchEntity() {}

  public PunchEntity(
      String id,
      String employeeId,
      PunchType type,
      Instant punchedAt,
      LocalDate workDate,
      String source) {
    this.id = id;
    this.employeeId = employeeId;
    this.type = type;
    this.punchedAt = punchedAt;
    this.workDate = workDate;
    this.source = source;
  }

  public String getId() {
    return id;
  }

  public String getEmployeeId() {
    return employeeId;
  }

  public PunchType getType() {
    return type;
  }

  public Instant getPunchedAt() {
    return punchedAt;
  }

  public LocalDate getWorkDate() {
    return workDate;
  }

  public String getSource() {
    return source;
  }
}
