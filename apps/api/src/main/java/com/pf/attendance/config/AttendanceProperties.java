package com.pf.attendance.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "attendance")
public class AttendanceProperties {
  private String store = "memory";
  private boolean devAuth = true;
  private String corsOrigin = "http://localhost:3019";
  private boolean seedDemo = true;

  public String getStore() {
    return store;
  }

  public void setStore(String store) {
    this.store = store;
  }

  public boolean isDevAuth() {
    return devAuth;
  }

  public void setDevAuth(boolean devAuth) {
    this.devAuth = devAuth;
  }

  public String getCorsOrigin() {
    return corsOrigin;
  }

  public void setCorsOrigin(String corsOrigin) {
    this.corsOrigin = corsOrigin;
  }

  public boolean isSeedDemo() {
    return seedDemo;
  }

  public void setSeedDemo(boolean seedDemo) {
    this.seedDemo = seedDemo;
  }
}
