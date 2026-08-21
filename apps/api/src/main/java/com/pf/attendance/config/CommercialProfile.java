package com.pf.attendance.config;

/** Shared rules for ATTENDANCE_ENV staging/production (no Spring lifecycle). */
public final class CommercialProfile {
  private CommercialProfile() {}

  public static String normalize(String raw) {
    if (raw == null || raw.isBlank()) {
      return "development";
    }
    return switch (raw.trim().toLowerCase()) {
      case "dev", "development", "local", "demo" -> "development";
      case "staging", "stage" -> "staging";
      case "production", "prod" -> "production";
      default -> raw.trim().toLowerCase();
    };
  }

  public static void validate(String env, boolean devAuth, String oidcIssuer) {
    String normalized = normalize(env);
    if (!normalized.equals("development")
        && !normalized.equals("staging")
        && !normalized.equals("production")) {
      throw new IllegalStateException(
          "unsupported ATTENDANCE_ENV \""
              + env
              + "\" (use development, staging, or production)");
    }
    if ((normalized.equals("staging") || normalized.equals("production")) && devAuth) {
      throw new IllegalStateException(
          "ATTENDANCE_DEV_AUTH must be false when ATTENDANCE_ENV=" + normalized);
    }
    if ((normalized.equals("staging") || normalized.equals("production"))
        && (oidcIssuer == null || oidcIssuer.isBlank())) {
      throw new IllegalStateException(
          "ATTENDANCE_OIDC_ISSUER (or OIDC) is required when ATTENDANCE_ENV=" + normalized);
    }
  }
}
