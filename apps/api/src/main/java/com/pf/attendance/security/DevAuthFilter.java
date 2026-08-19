package com.pf.attendance.security;

import com.pf.attendance.app.AttendanceService;
import com.pf.attendance.app.Employee;
import com.pf.attendance.app.UnknownEmployeeException;
import com.pf.attendance.config.AttendanceProperties;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class DevAuthFilter extends OncePerRequestFilter {
  public static final String HEADER = "X-Dev-User-Sub";

  private final AttendanceProperties properties;
  private final AttendanceService attendance;

  public DevAuthFilter(AttendanceProperties properties, AttendanceService attendance) {
    this.properties = properties;
    this.attendance = attendance;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return "/health".equals(path) || "/ready".equals(path);
  }

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      filterChain.doFilter(request, response);
      return;
    }
    if (!properties.isDevAuth()) {
      unauthorized(response, "dev auth is required until P01 is wired");
      return;
    }
    String sub = request.getHeader(HEADER);
    if (sub == null || sub.isBlank()) {
      unauthorized(response, "X-Dev-User-Sub is required");
      return;
    }
    try {
      Employee employee = attendance.requireBySub(sub.trim());
      request.setAttribute(EmployeePrincipal.ATTR, employee);
      filterChain.doFilter(request, response);
    } catch (UnknownEmployeeException ex) {
      unauthorized(response, "unknown employee");
    }
  }

  private static void unauthorized(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response
        .getWriter()
        .write("{\"error\":{\"code\":\"unauthorized\",\"message\":\"" + message + "\"}}");
  }
}
