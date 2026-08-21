package com.pf.attendance.config;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class CommercialProfileTest {
  @Test
  void normalizeAliases() {
    assertEquals("development", CommercialProfile.normalize(""));
    assertEquals("staging", CommercialProfile.normalize("STAGE"));
    assertEquals("production", CommercialProfile.normalize("prod"));
  }

  @Test
  void stagingRejectsDevAuth() {
    assertThrows(
        IllegalStateException.class,
        () -> CommercialProfile.validate("staging", true, "http://idp"));
  }

  @Test
  void stagingRequiresOidc() {
    assertThrows(
        IllegalStateException.class, () -> CommercialProfile.validate("staging", false, ""));
  }

  @Test
  void stagingOk() {
    assertDoesNotThrow(() -> CommercialProfile.validate("staging", false, "http://idp"));
  }
}
