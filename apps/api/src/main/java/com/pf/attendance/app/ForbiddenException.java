package com.pf.attendance.app;

public final class ForbiddenException extends RuntimeException {
  public ForbiddenException(String message) {
    super(message);
  }
}
