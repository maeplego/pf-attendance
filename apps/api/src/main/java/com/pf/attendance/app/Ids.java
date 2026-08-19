package com.pf.attendance.app;

import com.github.f4b6a3.ulid.UlidCreator;

public final class Ids {
  private Ids() {}

  public static String ulid() {
    return UlidCreator.getUlid().toString();
  }
}
