package com.pf.attendance.app;

import java.util.List;

public final class DemoEmployees {
  private DemoEmployees() {}

  /**
   * Fictional 開発部. Names are not real people.
   */
  public static List<Employee> roster() {
    return List.of(
        new Employee("01J9EMP0000000000000000001", "aoki.haru", "青木 陽", "member"),
        new Employee("01J9EMP0000000000000000002", "sato.mei", "佐藤 芽衣", "manager"),
        new Employee("01J9EMP0000000000000000003", "kondo.minato", "近藤 湊", "member"),
        new Employee("01J9EMP0000000000000000004", "fujii.an", "藤井 杏", "member"),
        new Employee("01J9EMP0000000000000000005", "murakami.hayate", "村上 颯", "member"),
        new Employee("01J9EMP0000000000000000006", "okada.ritsu", "岡田 律", "member"),
        new Employee("01J9EMP0000000000000000007", "nakamura.nagi", "中村 凪", "member"),
        new Employee("01J9EMP0000000000000000008", "takahashi.saku", "高橋 朔", "member"));
  }
}
