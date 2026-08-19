const API = process.env.NEXT_PUBLIC_ATTENDANCE_API_URL ?? "http://localhost:8019";

export type Punch = {
  id: string;
  employeeId: string;
  type: string;
  punchedAt: string;
  workDate: string;
  source: string;
};

export type DailySummary = {
  workDate: string;
  workMinutes: number;
  breakMinutes: number;
  status: string;
  punches: Punch[];
};

export type Me = {
  id: string;
  sub: string;
  displayName: string;
  role: string;
  zone: string;
};

export type MonthDay = {
  workDate: string;
  workMinutes: number;
  breakMinutes: number;
  status: string;
  punchCount: number;
};

export type MonthSummary = {
  month: string;
  zone: string;
  days: MonthDay[];
};

function headers(sub: string): HeadersInit {
  return { "Content-Type": "application/json", "X-Dev-User-Sub": sub };
}

async function readError(res: Response): Promise<string> {
  try {
    const body = (await res.json()) as { error?: { message?: string } };
    return body.error?.message ?? res.statusText;
  } catch {
    return res.statusText;
  }
}

export async function getMe(sub: string): Promise<Me> {
  const res = await fetch(`${API}/v1/me`, { headers: headers(sub) });
  if (!res.ok) throw new Error(await readError(res));
  return res.json();
}

export async function getDailySummary(sub: string): Promise<DailySummary> {
  const res = await fetch(`${API}/v1/me/daily-summary`, { headers: headers(sub) });
  if (!res.ok) throw new Error(await readError(res));
  return res.json();
}

export async function getMonthSummary(sub: string, month: string): Promise<MonthSummary> {
  const res = await fetch(`${API}/v1/me/month-summary?month=${encodeURIComponent(month)}`, {
    headers: headers(sub),
  });
  if (!res.ok) throw new Error(await readError(res));
  return res.json();
}

export async function postPunch(sub: string, type: string): Promise<Punch> {
  const res = await fetch(`${API}/v1/punches`, {
    method: "POST",
    headers: headers(sub),
    body: JSON.stringify({ type }),
  });
  if (!res.ok) throw new Error(await readError(res));
  return res.json();
}
