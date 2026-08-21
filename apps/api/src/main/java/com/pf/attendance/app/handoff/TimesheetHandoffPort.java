package com.pf.attendance.app.handoff;

import java.time.YearMonth;
import java.util.List;

/**
 * Port for worksite→employer timesheet handoff (SES Stage B). Lab uses {@link MockTimesheetHandoffAdapter}.
 * Does not claim multi-company auth; Stage C adds read boundaries.
 */
public interface TimesheetHandoffPort {
  String CONTRACT = "worksite-minutes-v1";

  /** Persist an accepted handoff into the employer org. */
  HandoffReceipt accept(HandoffReceipt receipt);

  List<HandoffReceipt> list(String employerOrgId, YearMonth month);
}
