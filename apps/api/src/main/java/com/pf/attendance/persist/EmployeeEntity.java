package com.pf.attendance.persist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
    name = "employees",
    uniqueConstraints = @UniqueConstraint(name = "employees_org_sub", columnNames = {"org_id", "sub"}))
public class EmployeeEntity {
  @Id
  @Column(length = 26)
  private String id;

  @Column(name = "org_id", nullable = false)
  private String orgId;

  @Column(nullable = false, length = 128)
  private String sub;

  @Column(name = "display_name", nullable = false, length = 200)
  private String displayName;

  @Column(nullable = false, length = 32)
  private String role;

  public EmployeeEntity() {}

  public EmployeeEntity(String id, String orgId, String sub, String displayName, String role) {
    this.id = id;
    this.orgId = orgId;
    this.sub = sub;
    this.displayName = displayName;
    this.role = role;
  }

  public String getId() {
    return id;
  }

  public String getOrgId() {
    return orgId;
  }

  public String getSub() {
    return sub;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getRole() {
    return role;
  }
}
