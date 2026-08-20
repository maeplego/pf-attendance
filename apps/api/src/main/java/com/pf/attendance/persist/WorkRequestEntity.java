package com.pf.attendance.persist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "work_requests")
public class WorkRequestEntity {
  @Id
  @Column(length = 26)
  private String id;

  @Column(name = "employee_id", nullable = false, length = 26)
  private String employeeId;

  @Column(name = "request_type", nullable = false, length = 32)
  private String type;

  @Column(nullable = false, length = 32)
  private String status;

  @Column(name = "work_date", nullable = false)
  private LocalDate workDate;

  @Column(nullable = false)
  private String reason;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "decided_at")
  private Instant decidedAt;

  @Column(name = "decided_by", length = 128)
  private String decidedBy;

  public WorkRequestEntity() {}

  public WorkRequestEntity(
      String id,
      String employeeId,
      String type,
      String status,
      LocalDate workDate,
      String reason,
      Instant createdAt,
      Instant decidedAt,
      String decidedBy) {
    this.id = id;
    this.employeeId = employeeId;
    this.type = type;
    this.status = status;
    this.workDate = workDate;
    this.reason = reason;
    this.createdAt = createdAt;
    this.decidedAt = decidedAt;
    this.decidedBy = decidedBy;
  }

  public String getId() {
    return id;
  }

  public String getEmployeeId() {
    return employeeId;
  }

  public String getType() {
    return type;
  }

  public String getStatus() {
    return status;
  }

  public LocalDate getWorkDate() {
    return workDate;
  }

  public String getReason() {
    return reason;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public Instant getDecidedAt() {
    return decidedAt;
  }

  public String getDecidedBy() {
    return decidedBy;
  }
}
