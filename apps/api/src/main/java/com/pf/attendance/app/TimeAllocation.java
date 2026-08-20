package com.pf.attendance.app;

import java.time.LocalDate;

public record TimeAllocation(String id, String employeeId, LocalDate workDate, String project, int minutes) {}
