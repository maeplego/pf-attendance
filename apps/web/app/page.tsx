"use client";

import { PunchHome } from "@/components/PunchHome";

export default function HomePage() {
  return (
    <>
      <section className="hero">
        <h1 className="page-title">打刻</h1>
        <p className="page-lead">
          サーバー時刻が正です。勤務日は Asia/Tokyo の暦日。労働時間は整数分（休憩控除）。給与計算はしません。従業員は架空です。
        </p>
      </section>
      <div className="card">
        <PunchHome />
      </div>
    </>
  );
}
