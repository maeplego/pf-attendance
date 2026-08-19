package com.pf.attendance.security;

import com.pf.attendance.app.Employee;

public final class EmployeePrincipal {
  public static final String ATTR = "attendance.employee";

  private EmployeePrincipal() {}

  public static Employee require(jakarta.servlet.http.HttpServletRequest request) {
    Object value = request.getAttribute(ATTR);
    if (value instanceof Employee employee) {
      return employee;
    }
    throw new IllegalStateException("missing employee principal");
  }
}
