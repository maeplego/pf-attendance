package com.pf.attendance.domain;

import java.time.YearMonth;
import java.util.List;

public record MonthSummary(YearMonth month, List<DailySummary> days) {}
