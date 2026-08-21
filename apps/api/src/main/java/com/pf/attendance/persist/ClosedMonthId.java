package com.pf.attendance.persist;

import java.io.Serializable;
import java.util.Objects;

public class ClosedMonthId implements Serializable {
  private String orgId;
  private String month;

  public ClosedMonthId() {}

  public ClosedMonthId(String orgId, String month) {
    this.orgId = orgId;
    this.month = month;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ClosedMonthId that)) {
      return false;
    }
    return Objects.equals(orgId, that.orgId) && Objects.equals(month, that.month);
  }

  @Override
  public int hashCode() {
    return Objects.hash(orgId, month);
  }
}
