"use client";

import { useCallback, useEffect, useState } from "react";
import { getDailySummary, getMe, postPunch, type DailySummary, type Me } from "@/lib/api";

const PEOPLE = [
  { sub: "aoki.haru", label: "青木 陽（一般）" },
  { sub: "sato.mei", label: "佐藤 芽衣（上長）" },
  { sub: "kondo.minato", label: "近藤 湊" },
  { sub: "fujii.an", label: "藤井 杏" },
  { sub: "murakami.hayate", label: "村上 颯" },
  { sub: "okada.ritsu", label: "岡田 律" },
  { sub: "nakamura.nagi", label: "中村 凪" },
  { sub: "takahashi.saku", label: "高橋 朔" },
];

export function PunchHome() {
  const [sub, setSub] = useState("aoki.haru");
  const [me, setMe] = useState<Me | null>(null);
  const [summary, setSummary] = useState<DailySummary | null>(null);
  const [err, setErr] = useState("");

  const reload = useCallback(() => {
    Promise.all([getMe(sub), getDailySummary(sub)])
      .then(([m, s]) => {
        setMe(m);
        setSummary(s);
        setErr("");
      })
      .catch((e) => setErr(String(e)));
  }, [sub]);

  useEffect(() => {
    reload();
  }, [reload]);

  const punch = (type: string) => {
    postPunch(sub, type)
      .then(reload)
      .catch((e) => setErr(String(e)));
  };

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
      {me ? (
        <p className="muted">
          {me.displayName} · {me.role} · ゾーン {me.zone}
        </p>
      ) : null}
      {err ? <p className="error">{err}</p> : null}

      <div className="punch-row">
        <button type="button" onClick={() => punch("clock_in")}>
          出勤
        </button>
        <button type="button" onClick={() => punch("break_start")}>
          休憩開始
        </button>
        <button type="button" onClick={() => punch("break_end")}>
          休憩終了
        </button>
        <button type="button" onClick={() => punch("clock_out")}>
          退勤
        </button>
      </div>

      {summary ? (
        <>
          <div className="summary">
            <div className="card">
              <div className="muted">勤務日</div>
              <div className="num">{summary.workDate}</div>
            </div>
            <div className="card">
              <div className="muted">労働（分）</div>
              <div className="num">{summary.workMinutes}</div>
            </div>
            <div className="card">
              <div className="muted">休憩（分）</div>
              <div className="num">{summary.breakMinutes}</div>
            </div>
            <div className="card">
              <div className="muted">状態</div>
              <div className="num">{summary.status}</div>
            </div>
          </div>
          <table>
            <thead>
              <tr>
                <th>種別</th>
                <th>サーバー時刻 (UTC)</th>
                <th>勤務日</th>
              </tr>
            </thead>
            <tbody>
              {summary.punches.map((p) => (
                <tr key={p.id}>
                  <td>{p.type}</td>
                  <td>{p.punchedAt}</td>
                  <td>{p.workDate}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {summary.punches.length === 0 ? <p className="muted">本日の打刻はまだありません。</p> : null}
        </>
      ) : null}
    </section>
  );
}
