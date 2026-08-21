package com.pf.attendance.app.handoff;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

@Component
public class MockTimesheetHandoffAdapter implements TimesheetHandoffPort {
  private final CopyOnWriteArrayList<HandoffReceipt> receipts = new CopyOnWriteArrayList<>();

  @Override
  public HandoffReceipt accept(HandoffReceipt receipt) {
    receipts.add(receipt);
    return receipt;
  }

  @Override
  public List<HandoffReceipt> list(String employerOrgId, YearMonth month) {
    List<HandoffReceipt> out = new ArrayList<>();
    for (HandoffReceipt r : receipts) {
      if (r.employerOrgId().equals(employerOrgId) && r.month().equals(month)) {
        out.add(r);
      }
    }
    return List.copyOf(out);
  }

  /** Test helper. */
  public void clear() {
    receipts.clear();
  }
}
