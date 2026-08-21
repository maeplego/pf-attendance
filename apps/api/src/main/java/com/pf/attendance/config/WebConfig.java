package com.pf.attendance.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
  private final AttendanceProperties properties;

  public WebConfig(AttendanceProperties properties) {
    this.properties = properties;
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry
        .addMapping("/**")
        .allowedOrigins(properties.getCorsOrigin())
        .allowedMethods("GET", "POST", "OPTIONS")
        .allowedHeaders("Content-Type", "Authorization", "X-Dev-User-Sub", "X-Dev-User-Org")
        .allowCredentials(false);
  }
}
