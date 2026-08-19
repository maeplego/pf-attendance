"use client";

import { useCallback, useEffect, useMemo, useState } from "react";
import { getMonthSummary, type MonthDay } from "@/lib/api";

const PEOPLE = [
  { sub: "aoki.haru", label: "青木 陽（一般）" },
  { sub: "sato.mei", label: "佐藤 芽衣（上長）" },
  { sub: "kondo.minato", label: "近藤 湊" },
  { sub: "fujii.an", label: "藤井 杏" },
];

const WEEKDAYS = ["月", "火", "水", "木", "金", "土", "日"];

function shiftMonth(month: string, delta: number): string {
  const [y, m] = month.split("-").map(Number);
  const d = new Date(Date.UTC(y, m - 1 + delta, 1));
  const mm = String(d.getUTCMonth() + 1).padStart(2, "0");
  return `${d.getUTCFullYear()}-${mm}`;
}

function mondayOffset(year: number, month: number): number {
  return (new Date(Date.UTC(year, month - 1, 1)).getUTCDay() + 6) % 7;
}

export function MonthCalendar() {
  const [sub, setSub] = useState("aoki.haru");
  const [month, setMonth] = useState("2026-08");
  const [days, setDays] = useState<MonthDay[]>([]);
  const [err, setErr] = useState("");

  const reload = useCallback(() => {
    getMonthSummary(sub, month)
      .then((s) => {
        setDays(s.days);
        setErr("");
      })
      .catch((e) => setErr(String(e)));
  }, [sub, month]);

  useEffect(() => {
    reload();
  }, [reload]);

  const cells = useMemo(() => {
    const [y, m] = month.split("-").map(Number);
    const pad = mondayOffset(y, m);
    const blanks = Array.from({ length: pad }, (_, i) => ({ key: `b${i}`, day: null as MonthDay | null }));
    const filled = days.map((d) => ({ key: d.workDate, day: d }));
    return [...blanks, ...filled];
  }, [days, month]);

  return (
    <section>
      <label>
        従業員（架空）
        <div>
          <select value={sub} onChange={(e) => setSub(e.target.value)}>
            {PEOPLE.map((p) => (
              <option key={p.sub} value={p.sub}>
                {p.label}
              </option>
            ))}
          </select>
        </div>
      </label>
      <div className="month-nav">
        <button type="button" className="ghost" onClick={() => setMonth(shiftMonth(month, -1))}>
          ←
        </button>
        <strong>{month}</strong>
        <button type="button" className="ghost" onClick={() => setMonth(shiftMonth(month, 1))}>
          →
        </button>
      </div>
      <p className="muted">勤務日は Asia/Tokyo。分数は整数。他人の打刻は出ません。給与は計算しません。</p>
      {err ? <p className="error">{err}</p> : null}
      <div className="cal-weekdays">
        {WEEKDAYS.map((w) => (
          <div key={w}>{w}</div>
        ))}
      </div>
      <div className="cal-grid">
        {cells.map((c) =>
          c.day ? (
            <div
              key={c.key}
              className={`cal-cell${c.day.punchCount > 0 ? " has-punch" : ""}`}
              title={`${c.day.workDate} ${c.day.workMinutes}分`}
            >
              <div className="cal-num">{Number(c.day.workDate.slice(8))}</div>
              <div className="cal-min">{c.day.workMinutes}分</div>
            </div>
          ) : (
            <div key={c.key} className="cal-cell empty" />
          ),
        )}
      </div>
    </section>
  );
}
