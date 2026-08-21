package com.pf.attendance.app;

import java.time.LocalDate;

/** Employee estimate for a future day after closeByDay (見込み). Real punches override this. */
public record ProvisionalDay(
    String employeeId, LocalDate workDate, int workMinutes, int breakMinutes, String note) {}
