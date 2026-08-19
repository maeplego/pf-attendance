package com.pf.attendance.app;

public final class UnknownEmployeeException extends RuntimeException {
  public UnknownEmployeeException(String key) {
    super("unknown employee: " + key);
  }
}
