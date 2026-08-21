package com.pf.attendance.app;

/**
 * How the person relates to this tenant (employer org). Not a labor-law classification.
 * employed = in-house; client_site = SES-style assignment at a customer worksite (still employed by this org).
 */
public final class Engagement {
  public static final String EMPLOYED = "employed";
  public static final String CLIENT_SITE = "client_site";

  private Engagement() {}

  public static String normalize(String raw) {
    if (raw == null || raw.isBlank()) {
      return EMPLOYED;
    }
    String v = raw.trim();
    if (EMPLOYED.equals(v) || CLIENT_SITE.equals(v)) {
      return v;
    }
    throw new IllegalArgumentException("unknown engagement: " + raw);
  }
}
