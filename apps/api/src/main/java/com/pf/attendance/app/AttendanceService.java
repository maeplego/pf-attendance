package com.pf.attendance.app;

import com.pf.attendance.domain.DailyHoursCalculator;
import com.pf.attendance.domain.DailySummary;
import com.pf.attendance.domain.MonthSummary;
import com.pf.attendance.domain.PunchConflictException;
import com.pf.attendance.domain.PunchEvent;
import com.pf.attendance.domain.PunchRules;
import com.pf.attendance.domain.PunchType;
import com.pf.attendance.domain.WorkDates;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AttendanceService {
  private final EmployeeStore employees;
  private final PunchStore punches;
  private final Clock clock;

  public AttendanceService(EmployeeStore employees, PunchStore punches, Clock clock) {
    this.employees = employees;
    this.punches = punches;
    this.clock = clock;
  }

  public Employee requireBySub(String sub) {
    return employees
        .findBySub(sub)
        .orElseThrow(() -> new UnknownEmployeeException(sub));
  }

  public PunchEvent punch(String employeeId, PunchType type) {
    Employee employee =
        employees
            .findById(employeeId)
            .orElseThrow(() -> new UnknownEmployeeException(employeeId));
    Instant now = Instant.now(clock);
    LocalDate workDate = WorkDates.of(now);
    List<PunchEvent> existing = punches.findByEmployeeAndWorkDate(employee.id(), workDate);
    PunchRules.assertAllowed(existing, type);
    PunchEvent event =
        new PunchEvent(Ids.ulid(), employee.id(), type, now, workDate, "web");
    punches.append(event);
    return event;
  }

  public DailySummary dailySummary(String employeeId, LocalDate workDate) {
    if (employees.findById(employeeId).isEmpty()) {
      throw new UnknownEmployeeException(employeeId);
    }
    List<PunchEvent> events = punches.findByEmployeeAndWorkDate(employeeId, workDate);
    Instant asOf = WorkDates.of(Instant.now(clock)).equals(workDate) ? Instant.now(clock) : null;
    return DailyHoursCalculator.compute(workDate, events, asOf);
  }

  public MonthSummary monthSummary(String employeeId, YearMonth month) {
    if (employees.findById(employeeId).isEmpty()) {
      throw new UnknownEmployeeException(employeeId);
    }
    List<DailySummary> days = new ArrayList<>();
    LocalDate cursor = month.atDay(1);
    LocalDate last = month.atEndOfMonth();
    while (!cursor.isAfter(last)) {
      days.add(dailySummary(employeeId, cursor));
      cursor = cursor.plusDays(1);
    }
    return new MonthSummary(month, List.copyOf(days));
  }
}
