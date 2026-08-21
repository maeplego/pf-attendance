package com.pf.attendance.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WorkScheduleTest {
  @Test
  void laborHintUsesSixAndEightHourThresholds() {
    assertThat(WorkSchedule.laborHintBreakMinutes(5 * 60)).isZero();
    assertThat(WorkSchedule.laborHintBreakMinutes(6 * 60)).isEqualTo(45);
    assertThat(WorkSchedule.laborHintBreakMinutes(8 * 60)).isEqualTo(60);
  }

  @Test
  void scheduledNetIsSpanMinusBreak() {
    WorkSchedule s = WorkSchedule.defaults();
    assertThat(s.scheduledNetMinutes()).isEqualTo(480);
  }
}
