"use client";

import { PunchHome } from "@/components/PunchHome";

export default function HomePage() {
  return (
    <>
      <h1>打刻</h1>
      <p className="muted">
        サーバー時刻が正です。勤務日は Asia/Tokyo の暦日。労働時間は整数分（休憩控除）。給与計算はしません。従業員は架空です。
      </p>
      <PunchHome />
    </>
  );
}
