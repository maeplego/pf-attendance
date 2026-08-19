package com.pf.attendance.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.pf.attendance.support.MutableClock;
import java.time.Clock;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(PunchHttpTest.ClockConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PunchHttpTest {
  @Autowired private MockMvc mvc;
  @Autowired private Clock clock;

  @Test
  void healthDoesNotNeedAuth() throws Exception {
    mvc.perform(get("/health")).andExpect(status().isOk()).andExpect(jsonPath("$.ok").value(true));
    mvc.perform(get("/ready")).andExpect(status().isOk());
  }

  @Test
  void punchWithoutHeaderIs401() throws Exception {
    mvc.perform(
            post("/v1/punches")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"clock_in\"}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void punchUsesServerTimeAndIgnoresClientPunchedAt() throws Exception {
    ((MutableClock) clock).setInstant(Instant.parse("2026-08-19T00:00:00Z"));
    mvc.perform(
            post("/v1/punches")
                .header("X-Dev-User-Sub", "aoki.haru")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"clock_in\",\"punchedAt\":\"1999-01-01T00:00:00Z\"}"))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.type").value("clock_in"))
        .andExpect(jsonPath("$.punchedAt").value("2026-08-19T00:00:00Z"))
        .andExpect(jsonPath("$.workDate").value("2026-08-19"));
  }

  @Test
  void dailyHoursAfterBreakAre480() throws Exception {
    MutableClock mutable = (MutableClock) clock;
    mutable.setInstant(Instant.parse("2026-08-19T00:00:00Z"));
    punch("clock_in");
    mutable.setInstant(Instant.parse("2026-08-19T03:00:00Z"));
    punch("break_start");
    mutable.setInstant(Instant.parse("2026-08-19T04:00:00Z"));
    punch("break_end");
    mutable.setInstant(Instant.parse("2026-08-19T09:00:00Z"));
    punch("clock_out");

    mvc.perform(get("/v1/me/daily-summary").header("X-Dev-User-Sub", "aoki.haru"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.workMinutes").value(480))
        .andExpect(jsonPath("$.breakMinutes").value(60))
        .andExpect(jsonPath("$.status").value("clocked_out"));

    mvc.perform(get("/v1/me/daily-summary").header("X-Dev-User-Sub", "sato.mei"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.workMinutes").value(0))
        .andExpect(jsonPath("$.punches.length()").value(0));
  }

  @Test
  void tokyoMidnightSplitsWorkDates() throws Exception {
    MutableClock mutable = (MutableClock) clock;
    mutable.setInstant(Instant.parse("2026-08-18T14:59:00Z"));
    punch("clock_in");
    mutable.setInstant(Instant.parse("2026-08-18T15:00:00Z"));
    punch("clock_in");

    mvc.perform(
            get("/v1/me/daily-summary")
                .param("date", "2026-08-18")
                .header("X-Dev-User-Sub", "aoki.haru"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.workDate").value("2026-08-18"))
        .andExpect(jsonPath("$.punches.length()").value(1));
    mvc.perform(
            get("/v1/me/daily-summary")
                .param("date", "2026-08-19")
                .header("X-Dev-User-Sub", "aoki.haru"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.workDate").value("2026-08-19"))
        .andExpect(jsonPath("$.punches.length()").value(1));
  }

  @Test
  void unknownTypeIs400() throws Exception {
    mvc.perform(
            post("/v1/punches")
                .header("X-Dev-User-Sub", "aoki.haru")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"teleport\"}"))
        .andExpect(status().isBadRequest())
        .andExpect(jsonPath("$.error.code").value("validation_error"));
  }

  @Test
  void monthSummaryHas31DaysAndDoesNotLeakOtherEmployees() throws Exception {
    MutableClock mutable = (MutableClock) clock;
    mutable.setInstant(Instant.parse("2026-08-19T00:00:00Z"));
    punch("clock_in");
    mutable.setInstant(Instant.parse("2026-08-19T09:00:00Z"));
    punch("clock_out");

    mvc.perform(get("/v1/me/month-summary").param("month", "2026-08").header("X-Dev-User-Sub", "aoki.haru"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.month").value("2026-08"))
        .andExpect(jsonPath("$.zone").value("Asia/Tokyo"))
        .andExpect(jsonPath("$.days.length()").value(31))
        .andExpect(jsonPath("$.days[18].workDate").value("2026-08-19"))
        .andExpect(jsonPath("$.days[18].punchCount").value(2))
        .andExpect(jsonPath("$.days[18].workMinutes").value(540));

    mvc.perform(get("/v1/me/month-summary").param("month", "2026-08").header("X-Dev-User-Sub", "sato.mei"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.days[18].punchCount").value(0))
        .andExpect(jsonPath("$.days[18].workMinutes").value(0));
  }

  @Test
  void monthSummaryRejectsBadMonth() throws Exception {
    mvc.perform(get("/v1/me/month-summary").param("month", "2026-13").header("X-Dev-User-Sub", "aoki.haru"))
        .andExpect(status().isBadRequest());
  }

  private void punch(String type) throws Exception {
    mvc.perform(
            post("/v1/punches")
                .header("X-Dev-User-Sub", "aoki.haru")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"" + type + "\"}"))
        .andExpect(status().isCreated());
  }

  @TestConfiguration
  static class ClockConfig {
    @Bean
    @Primary
    Clock testClock() {
      return new MutableClock(Instant.parse("2026-08-19T00:00:00Z"));
    }
  }
}
