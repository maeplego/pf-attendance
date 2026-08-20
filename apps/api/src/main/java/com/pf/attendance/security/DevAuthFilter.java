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
  private final OidcUserinfoClient oidc;

  public DevAuthFilter(
      AttendanceProperties properties, AttendanceService attendance, OidcUserinfoClient oidc) {
    this.properties = properties;
    this.attendance = attendance;
    this.oidc = oidc;
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
    String sub = null;
    if (properties.isDevAuth()) {
      sub = trimToNull(request.getHeader(HEADER));
    }
    if (sub == null) {
      sub = bearerSub(request);
    }
    if (sub == null) {
      unauthorized(response, properties.isDevAuth() ? "X-Dev-User-Sub is required" : "Bearer token required");
      return;
    }
    try {
      Employee employee = attendance.requireBySub(sub);
      request.setAttribute(EmployeePrincipal.ATTR, employee);
      filterChain.doFilter(request, response);
    } catch (UnknownEmployeeException ex) {
      unauthorized(response, "unknown employee");
    }
  }

  private String bearerSub(HttpServletRequest request) {
    String issuer = properties.getOidcIssuer();
    if (issuer == null || issuer.isBlank()) {
      return null;
    }
    String authz = request.getHeader("Authorization");
    if (authz == null || !authz.startsWith("Bearer ")) {
      return null;
    }
    String token = authz.substring("Bearer ".length()).trim();
    String base = properties.getOidcInternalBase();
    if (base == null || base.isBlank()) {
      base = issuer;
    }
    return oidc.resolveSub(base, token);
  }

  private static String trimToNull(String value) {
    if (value == null || value.isBlank()) {
      return null;
    }
    return value.trim();
  }

  private static void unauthorized(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response
        .getWriter()
        .write("{\"error\":{\"code\":\"unauthorized\",\"message\":\"" + message + "\"}}");
  }
}
