package com.pf.attendance.app;

import java.util.List;

public final class DemoEmployees {
  public static final String ORG_A = "org-demo-a";
  public static final String ORG_B = "org-demo-b";

  private DemoEmployees() {}

  /** Fictional 開発部. Names are not real people. Default org-demo-a. */
  public static List<Employee> roster() {
    return roster(ORG_A);
  }

  public static List<Employee> roster(String orgId) {
    String prefix = ORG_B.equals(orgId) ? "01J9EMPB" : "01J9EMPA";
    return List.of(
        new Employee(prefix + "000000000000000001", orgId, "aoki.haru", "青木 陽", "member"),
        new Employee(prefix + "000000000000000002", orgId, "sato.mei", "佐藤 芽衣", "manager"),
        new Employee(prefix + "000000000000000003", orgId, "kondo.minato", "近藤 湊", "member"),
        new Employee(prefix + "000000000000000004", orgId, "fujii.an", "藤井 杏", "member"),
        new Employee(prefix + "000000000000000005", orgId, "murakami.hayate", "村上 颯", "member"),
        new Employee(prefix + "000000000000000006", orgId, "okada.ritsu", "岡田 律", "member"),
        new Employee(prefix + "000000000000000007", orgId, "nakamura.nagi", "中村 凪", "member"),
        new Employee(prefix + "000000000000000008", orgId, "takahashi.saku", "高橋 朔", "member"));
  }
}
