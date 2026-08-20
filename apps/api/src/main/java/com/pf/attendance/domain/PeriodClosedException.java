package com.pf.attendance.domain;

public final class PeriodClosedException extends RuntimeException {
  public PeriodClosedException(String message) {
    super(message);
  }
}
