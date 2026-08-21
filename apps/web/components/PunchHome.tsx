"use client";

import { useCallback, useEffect, useState } from "react";
import {
  applyScheduleDay,
  getDailySummary,
  getMe,
  getPeriodSettings,
  postPunch,
  type DailySummary,
  type Me,
} from "@/lib/api";
import { DEMO_PEOPLE, tokyoToday } from "@/lib/people";

export function PunchHome() {
  const [sub, setSub] = useState("aoki.haru");
  const [me, setMe] = useState<Me | null>(null);
  const [summary, setSummary] = useState<DailySummary | null>(null);
  const [scheduleLabel, setScheduleLabel] = useState("09:00–18:00 / 休憩60分");
  const [backfillDate, setBackfillDate] = useState(tokyoToday());
  const [err, setErr] = useState("");
  const [info, setInfo] = useState("");

  const reload = useCallback(() => {
    Promise.all([
      getMe(sub),
      getDailySummary(sub),
      getPeriodSettings(sub).catch(() => null),
    ])
      .then(([m, s, period]) => {
        setMe(m);
        setSummary(s);
        if (period) {
          setScheduleLabel(
            `${period.scheduledStart}–${period.scheduledEnd} / 休憩${period.breakMinutes}分（所定${period.scheduledNetMinutes}分）`,
          );
        }
        setErr("");
      })
      .catch((e) => setErr(String(e)));
  }, [sub]);

  useEffect(() => {
    reload();
  }, [reload]);

  const punch = (type: string) => {
    postPunch(sub, type)
      .then(() => {
        setInfo("");
        reload();
      })
      .catch((e) => setErr(String(e)));
  };

  const forgotten = (type: "clock_in" | "clock_out", at?: string) => {
    postPunch(sub, type, { workDate: backfillDate, at })
      .then(() => {
        setInfo(
          at
            ? `${backfillDate} に ${type} ${at} を記録しました。`
            : `${backfillDate} に所定時刻で ${type} を記録しました。`,
        );
        reload();
      })
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
          {me.displayName} · {me.role}
          {me.engagement === "client_site" && me.worksiteName
            ? ` · 客先 ${me.worksiteName}`
            : ""}{" "}
          · ゾーン {me.zone}
        </p>
      ) : null}
      <p className="muted">勤務プロファイル（org）: {scheduleLabel}</p>
      {err ? <p className="error">{err}</p> : null}
      {info ? <p className="muted">{info}</p> : null}

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

      <h2>忘れ打刻・事後入力</h2>
      <p className="muted">
        日付を選び、所定始業／定時で 1 打刻、または 1 日分をスケジュール適用。時刻を空にすると org プロファイルの既定値を使います。法的な休憩義務の判定はしません（labor_hint は教育用）。
      </p>
      <div className="punch-row">
        <input type="date" value={backfillDate} onChange={(e) => setBackfillDate(e.target.value)} />
        <button type="button" onClick={() => forgotten("clock_in")}>
          所定で出勤
        </button>
        <button type="button" onClick={() => forgotten("clock_out")}>
          定時で退勤
        </button>
        <button
          type="button"
          onClick={() =>
            applyScheduleDay(sub, backfillDate)
              .then(() => {
                setInfo(`${backfillDate} に所定スケジュール（出・休・退）を適用しました。`);
                reload();
              })
              .catch((e) => setErr(String(e)))
          }
        >
          1日分を所定どおり
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
            <div className="card">
              <div className="muted">遅刻／早退／残業（分）</div>
              <div className="num">
                {summary.lateMinutes ?? 0}/{summary.earlyLeaveMinutes ?? 0}/{summary.overtimeMinutes ?? 0}
              </div>
            </div>
          </div>
          <table>
            <thead>
              <tr>
                <th>種別</th>
                <th>サーバー時刻 (UTC)</th>
                <th>勤務日</th>
                <th>source</th>
              </tr>
            </thead>
            <tbody>
              {summary.punches.map((p) => (
                <tr key={p.id}>
                  <td>{p.type}</td>
                  <td>{p.punchedAt}</td>
                  <td>{p.workDate}</td>
                  <td>{p.source}</td>
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
