"use client";

import { WorkflowPanel } from "@/components/WorkflowPanel";

export default function WorkflowPage() {
  return (
    <>
      <h1>申請・締め</h1>
      <p className="muted">
        上長は承認・月次締め・CSV。一般は申請と工数按分。給与金額は出しません。従業員は架空です。
      </p>
      <WorkflowPanel />
    </>
  );
}
