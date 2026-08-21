package com.pf.attendance.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CommercialProfileRunner implements ApplicationRunner {
  private final AttendanceProperties properties;

  public CommercialProfileRunner(AttendanceProperties properties) {
    this.properties = properties;
  }

  @Override
  public void run(ApplicationArguments args) {
    CommercialProfile.validate(
        properties.getEnv(), properties.isDevAuth(), properties.getOidcIssuer());
  }
}
