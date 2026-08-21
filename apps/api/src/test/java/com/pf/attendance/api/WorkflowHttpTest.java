package com.pf.attendance.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
@Import(WorkflowHttpTest.ClockConfig.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class WorkflowHttpTest {
  @Autowired private MockMvc mvc;
  @Autowired private Clock clock;

  @Test
  void memberCannotCloseOrSeeInbox() throws Exception {
    mvc.perform(post("/v1/months/2026-08/close").header("X-Dev-User-Sub", "aoki.haru"))
        .andExpect(status().isForbidden())
        .andExpect(jsonPath("$.error.code").value("forbidden"));
    mvc.perform(get("/v1/approvals").header("X-Dev-User-Sub", "aoki.haru"))
        .andExpect(status().isForbidden());
  }

  @Test
  void leaveRequestIsApprovedThenMonthCloseBlocksPunch() throws Exception {
    MutableClock mutable = (MutableClock) clock;
    mutable.setInstant(Instant.parse("2026-08-19T00:00:00Z"));
    MvcResult created =
        mvc.perform(
                post("/v1/requests")
                    .header("X-Dev-User-Sub", "aoki.haru")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"type\":\"leave\",\"workDate\":\"2026-08-20\",\"reason\":\"譛臥ｵｦ\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.status").value("pending"))
            .andReturn();
    String body = created.getResponse().getContentAsString();
    String id = body.replaceAll(".*\"id\":\"([^\"]+)\".*", "$1");

    mvc.perform(get("/v1/approvals").header("X-Dev-User-Sub", "sato.mei"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.requests.length()").value(1));

    mvc.perform(
            post("/v1/requests/" + id + "/decision")
                .header("X-Dev-User-Sub", "sato.mei")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"approve\":true}"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.status").value("approved"));

    mvc.perform(
            post("/v1/punches")
                .header("X-Dev-User-Sub", "aoki.haru")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"clock_in\"}"))
        .andExpect(status().isCreated());
    mutable.setInstant(Instant.parse("2026-08-19T08:00:00Z"));
    mvc.perform(
            post("/v1/punches")
                .header("X-Dev-User-Sub", "aoki.haru")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"clock_out\"}"))
        .andExpect(status().isCreated());

    mvc.perform(
            post("/v1/allocations")
                .header("X-Dev-User-Sub", "aoki.haru")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"workDate\":\"2026-08-19\",\"project\":\"P09\",\"minutes\":60}"))
        .andExpect(status().isCreated());

    mvc.perform(post("/v1/months/2026-08/close").header("X-Dev-User-Sub", "sato.mei"))
        .andExpect(status().isNoContent());

    mvc.perform(
            post("/v1/punches")
                .header("X-Dev-User-Sub", "aoki.haru")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"type\":\"clock_in\"}"))
        .andExpect(status().isConflict())
        .andExpect(jsonPath("$.error.code").value("period_closed"));

    mvc.perform(get("/v1/months/2026-08/export.csv").header("X-Dev-User-Sub", "sato.mei"))
        .andExpect(status().isOk())
        .andExpect(header().string("X-Attendance-Export-Contract", "minutes-v1"))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("aoki.haru")));

    mvc.perform(get("/v1/reminders/unpunched").param("date", "2026-08-19").header("X-Dev-User-Sub", "sato.mei"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.employees[?(@.sub=='sato.mei')]").exists());
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
