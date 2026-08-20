"use client";

import { MonthCalendar } from "@/components/MonthCalendar";

export default function CalendarPage() {
  return (
    <>
      <h1>月次カレンダー</h1>
      <p className="muted">
        各マスは Asia/Tokyo の暦日の労働分です。申請・承認・締めは「申請・締め」タブです。
      </p>
      <MonthCalendar />
    </>
  );
}
