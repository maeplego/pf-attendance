import type { Metadata } from "next";
import { AuthBar } from "@/components/AuthBar";
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
        <div className="site-shell">
          <header className="site-header">
            <div className="site-brand">
              <strong>pf-attendance</strong>
              <span className="muted">学習用勤怠（架空の開発部）</span>
            </div>
            <nav className="site-nav">
              <a href="/">打刻</a>
              <a href="/calendar">月次カレンダー</a>
              <a href="/workflow">申請・締め</a>
            </nav>
            <AuthBar />
          </header>
          <main className="site-main">{children}</main>
        </div>
      </body>
    </html>
  );
}
