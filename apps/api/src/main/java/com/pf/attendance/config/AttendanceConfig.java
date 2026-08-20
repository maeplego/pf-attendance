package com.pf.attendance.config;

import com.pf.attendance.app.EmployeeStore;
import com.pf.attendance.app.MemoryEmployeeStore;
import com.pf.attendance.app.MemoryPunchStore;
import com.pf.attendance.app.MemoryWorkflowStore;
import com.pf.attendance.app.PunchStore;
import com.pf.attendance.app.WorkflowStore;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AttendanceProperties.class)
public class AttendanceConfig {
  @Bean
  Clock clock() {
    return Clock.systemUTC();
  }

  @Bean
  @ConditionalOnProperty(name = "attendance.store", havingValue = "memory", matchIfMissing = true)
  EmployeeStore memoryEmployees() {
    return new MemoryEmployeeStore();
  }

  @Bean
  @ConditionalOnProperty(name = "attendance.store", havingValue = "memory", matchIfMissing = true)
  PunchStore memoryPunches() {
    return new MemoryPunchStore();
  }

  @Bean
  @ConditionalOnProperty(name = "attendance.store", havingValue = "memory", matchIfMissing = true)
  WorkflowStore memoryWorkflow() {
    return new MemoryWorkflowStore();
  }
}
