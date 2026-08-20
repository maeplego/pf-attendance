package com.pf.attendance.persist;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ClosedMonthJpaRepository extends JpaRepository<ClosedMonthEntity, String> {}
