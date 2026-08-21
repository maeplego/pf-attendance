package com.pf.attendance.domain;

public enum PunchState {
  ABSENT,
  CLOCKED_IN,
  ON_BREAK,
  CLOCKED_OUT,
  /** Estimate entered before the calendar day occurs (見込み). Not a real punch sequence. */
  PROVISIONAL
}
