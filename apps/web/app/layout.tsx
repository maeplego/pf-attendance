import type { Metadata } from "next";
import "./globals.css";

export const dynamic = "force-dynamic";

export const metadata: Metadata = {
  title: "pf-attendance",
  description: "学習用勤怠。日境界は Asia/Tokyo。分数は整数。架空の従業員のみ。",
};

export default function RootLayout({ children }: { children: React.ReactNode }) {
  return (
    <html lang="ja">
      <body>
        <header style={{ padding: "1rem 1.25rem", borderBottom: "1px solid #e2e8f0", background: "#fff" }}>
          <strong>pf-attendance</strong>
          <span className="muted" style={{ marginLeft: "0.75rem" }}>
            学習用勤怠（架空の開発部）
          </span>
        </header>
        <main style={{ padding: "1rem 1.25rem 3rem", maxWidth: 880 }}>{children}</main>
      </body>
    </html>
  );
}
