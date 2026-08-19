package com.pf.attendance.domain;

public final class PunchConflictException extends RuntimeException {
  public PunchConflictException(String message) {
    super(message);
  }
}
