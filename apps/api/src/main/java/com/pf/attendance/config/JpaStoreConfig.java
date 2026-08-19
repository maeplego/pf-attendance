package com.pf.attendance.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@Configuration
@ConditionalOnProperty(name = "attendance.store", havingValue = "jpa")
@EnableJpaRepositories(basePackages = "com.pf.attendance.persist")
@EntityScan(basePackages = "com.pf.attendance.persist")
public class JpaStoreConfig {}
