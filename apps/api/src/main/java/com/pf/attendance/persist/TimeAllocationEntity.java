package com.pf.attendance.persist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "time_allocations")
public class TimeAllocationEntity {
  @Id
  @Column(length = 26)
  private String id;

  @Column(name = "employee_id", nullable = false, length = 26)
  private String employeeId;

  @Column(name = "work_date", nullable = false)
  private LocalDate workDate;

  @Column(nullable = false, length = 64)
  private String project;

  @Column(nullable = false)
  private int minutes;

  public TimeAllocationEntity() {}

  public TimeAllocationEntity(String id, String employeeId, LocalDate workDate, String project, int minutes) {
    this.id = id;
    this.employeeId = employeeId;
    this.workDate = workDate;
    this.project = project;
    this.minutes = minutes;
  }

  public String getId() {
    return id;
  }

  public String getEmployeeId() {
    return employeeId;
  }

  public LocalDate getWorkDate() {
    return workDate;
  }

  public String getProject() {
    return project;
  }

  public int getMinutes() {
    return minutes;
  }
}
