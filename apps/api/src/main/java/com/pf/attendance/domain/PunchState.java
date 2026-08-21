package com.pf.attendance.domain;

public enum PunchState {
  ABSENT,
  CLOCKED_IN,
  ON_BREAK,
  CLOCKED_OUT,
  /** Estimate entered before the calendar day occurs (見込み). Not a real punch sequence. */
  PROVISIONAL,
  /** Approved leave day without punches (有給・半休・欠勤). */
  ON_LEAVE
}
