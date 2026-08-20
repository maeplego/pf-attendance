package com.pf.attendance.persist;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "closed_months")
public class ClosedMonthEntity {
  @Id
  @Column(length = 7)
  private String month;

  @Column(name = "closed_by", nullable = false, length = 128)
  private String closedBy;

  public ClosedMonthEntity() {}

  public ClosedMonthEntity(String month, String closedBy) {
    this.month = month;
    this.closedBy = closedBy;
  }

  public String getMonth() {
    return month;
  }

  public String getClosedBy() {
    return closedBy;
  }
}
