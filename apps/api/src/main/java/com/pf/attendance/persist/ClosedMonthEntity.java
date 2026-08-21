package com.pf.attendance.persist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "closed_months")
@IdClass(ClosedMonthId.class)
public class ClosedMonthEntity {
  @Id
  @Column(name = "org_id", nullable = false)
  private String orgId;

  @Id
  @Column(length = 7)
  private String month;

  @Column(name = "closed_by", nullable = false, length = 128)
  private String closedBy;

  public ClosedMonthEntity() {}

  public ClosedMonthEntity(String orgId, String month, String closedBy) {
    this.orgId = orgId;
    this.month = month;
    this.closedBy = closedBy;
  }

  public String getOrgId() {
    return orgId;
  }

  public String getMonth() {
    return month;
  }

  public String getClosedBy() {
    return closedBy;
  }
}
