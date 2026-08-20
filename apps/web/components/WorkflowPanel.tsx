"use client";

import { useCallback, useEffect, useState } from "react";
import {
  closeMonth,
  decideRequest,
  exportMonthCsv,
  listAllocations,
  listApprovals,
  listRequests,
  postAllocation,
  postRequest,
  unpunched,
  type TimeAllocationRow,
  type WorkRequestRow,
} from "@/lib/api";

const PEOPLE = [
  { sub: "aoki.haru", label: "青木 陽（一般）" },
  { sub: "sato.mei", label: "佐藤 芽衣（上長）" },
];

export function WorkflowPanel() {
  const [sub, setSub] = useState("aoki.haru");
  const [type, setType] = useState("leave");
  const [workDate, setWorkDate] = useState("2026-08-20");
  const [reason, setReason] = useState("有給");
  const [project, setProject] = useState("P09");
  const [minutes, setMinutes] = useState(60);
  const [allocDate, setAllocDate] = useState("2026-08-19");
  const [month, setMonth] = useState("2026-08");
  const [mine, setMine] = useState<WorkRequestRow[]>([]);
  const [inbox, setInbox] = useState<WorkRequestRow[]>([]);
  const [allocs, setAllocs] = useState<TimeAllocationRow[]>([]);
  const [missing, setMissing] = useState<{ sub: string; displayName: string }[]>([]);
  const [csv, setCsv] = useState("");
  const [err, setErr] = useState("");

  const reload = useCallback(() => {
    Promise.all([
      listRequests(sub),
      listAllocations(sub, allocDate),
      listApprovals(sub).catch(() => ({ requests: [] as WorkRequestRow[] })),
      unpunched(sub, allocDate).catch(() => ({ employees: [] as { sub: string; displayName: string }[] })),
    ])
      .then(([r, a, inboxBody, miss]) => {
        setMine(r.requests);
        setAllocs(a.allocations);
        setInbox(inboxBody.requests);
        setMissing(miss.employees ?? []);
        setErr("");
      })
      .catch((e) => setErr(String(e)));
  }, [sub, allocDate]);

  useEffect(() => {
    reload();
  }, [reload]);

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
      {err ? <p className="error">{err}</p> : null}

      <h2>申請</h2>
      <p className="muted">休暇または打刻修正。締め後の月は 409 です。</p>
      <div className="punch-row">
        <select value={type} onChange={(e) => setType(e.target.value)}>
          <option value="leave">休暇</option>
          <option value="punch_correction">打刻修正</option>
        </select>
        <input value={workDate} onChange={(e) => setWorkDate(e.target.value)} />
        <input value={reason} onChange={(e) => setReason(e.target.value)} />
        <button
          type="button"
          onClick={() =>
            postRequest(sub, type, workDate, reason)
              .then(reload)
              .catch((e) => setErr(String(e)))
          }
        >
          提出
        </button>
      </div>
      <ul>
        {mine.map((r) => (
          <li key={r.id}>
            {r.workDate} {r.type} {r.status} — {r.reason}
          </li>
        ))}
      </ul>

      <h2>上長 inbox</h2>
      {inbox.length === 0 ? <p className="muted">一般ユーザーでは空／403。上長に切り替えてください。</p> : null}
      {inbox.map((r) => (
        <div key={r.id} className="punch-row">
          <span>
            {r.workDate} {r.type} {r.reason}
          </span>
          <button type="button" onClick={() => decideRequest(sub, r.id, true).then(reload)}>
            承認
          </button>
          <button type="button" onClick={() => decideRequest(sub, r.id, false).then(reload)}>
            却下
          </button>
        </div>
      ))}

      <h2>工数按分</h2>
      <p className="muted">当日の労働分を超えると 400 です。</p>
      <div className="punch-row">
        <input value={allocDate} onChange={(e) => setAllocDate(e.target.value)} />
        <input value={project} onChange={(e) => setProject(e.target.value)} />
        <input
          type="number"
          value={minutes}
          onChange={(e) => setMinutes(Number(e.target.value))}
        />
        <button
          type="button"
          onClick={() =>
            postAllocation(sub, allocDate, project, minutes)
              .then(reload)
              .catch((e) => setErr(String(e)))
          }
        >
          登録
        </button>
      </div>
      <ul>
        {allocs.map((a) => (
          <li key={a.id}>
            {a.project} {a.minutes} 分
          </li>
        ))}
      </ul>

      <h2>月次締め / CSV / 未打刻</h2>
      <div className="punch-row">
        <input value={month} onChange={(e) => setMonth(e.target.value)} />
        <button
          type="button"
          onClick={() =>
            closeMonth(sub, month)
              .then(reload)
              .catch((e) => setErr(String(e)))
          }
        >
          締め（上長）
        </button>
        <button
          type="button"
          onClick={() =>
            exportMonthCsv(sub, month)
              .then(setCsv)
              .catch((e) => setErr(String(e)))
          }
        >
          CSV
        </button>
      </div>
      {csv ? <pre>{csv}</pre> : null}
      <p className="muted">未打刻（{allocDate}）</p>
      <ul>
        {missing.map((e) => (
          <li key={e.sub}>
            {e.displayName} ({e.sub})
          </li>
        ))}
      </ul>
    </section>
  );
}
