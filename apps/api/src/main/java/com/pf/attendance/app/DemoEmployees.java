package com.pf.attendance.app;

import java.util.ArrayList;
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
    List<Employee> rows = new ArrayList<>();
    rows.add(Employee.employed(prefix + "000000000000000001", orgId, "aoki.haru", "青木 陽", "member"));
    rows.add(Employee.employed(prefix + "000000000000000002", orgId, "sato.mei", "佐藤 芽衣", "manager"));
    rows.add(Employee.employed(prefix + "000000000000000003", orgId, "kondo.minato", "近藤 湊", "member"));
    rows.add(Employee.employed(prefix + "000000000000000004", orgId, "fujii.an", "藤井 杏", "member"));
    rows.add(Employee.employed(prefix + "000000000000000005", orgId, "murakami.hayate", "村上 颯", "member"));
    rows.add(Employee.employed(prefix + "000000000000000006", orgId, "okada.ritsu", "岡田 律", "member"));
    rows.add(Employee.employed(prefix + "000000000000000007", orgId, "nakamura.nagi", "中村 凪", "member"));
    rows.add(Employee.employed(prefix + "000000000000000008", orgId, "takahashi.saku", "高橋 朔", "member"));
    // SES-style: employed by this org, working at a fictional client worksite.
    rows.add(
        Employee.clientSite(
            prefix + "000000000000000009",
            orgId,
            "ise.yuto",
            "伊勢 悠人",
            "member",
            "WS-CLIENT-A",
            "架空商事 本社開発"));
    rows.add(
        Employee.clientSite(
            prefix + "000000000000000010",
            orgId,
            "shima.rena",
            "島 玲奈",
            "member",
            "WS-CLIENT-B",
            "架空銀行 システム部"));
    return List.copyOf(rows);
  }
}
