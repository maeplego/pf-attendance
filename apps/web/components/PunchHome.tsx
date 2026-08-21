"use client";

import { useCallback, useEffect, useState } from "react";
import { getDailySummary, getMe, postPunch, type DailySummary, type Me } from "@/lib/api";
import { DEMO_PEOPLE } from "@/lib/people";

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
      <p className="muted">
        開発モード: セレクタはデモ用の従業員切替です。他人の打刻は API 上その sub として記録されます（本番は IdP で本人に固定）。
      </p>
      <label>
        従業員（架空・デモ切替）
        <div>
          <select value={sub} onChange={(e) => setSub(e.target.value)}>
            {DEMO_PEOPLE.map((p) => (
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
