"use client";

import { useEffect, useState } from "react";

type Session = {
  oidc: boolean;
  loggedIn: boolean;
  sub: string | null;
  displayName: string | null;
  devMode: boolean;
};

export function AuthBar() {
  const [session, setSession] = useState<Session | null>(null);

  useEffect(() => {
    fetch("/api/session", { credentials: "same-origin", cache: "no-store" })
      .then((res) => res.json())
      .then((body) => setSession(body as Session))
      .catch(() => setSession(null));
  }, []);

  if (!session?.oidc) {
    return (
      <span className="auth-bar muted">
        dev-auth（従業員セレクタ）
      </span>
    );
  }
  if (!session.loggedIn) {
    return (
      <span className="auth-bar">
        <a href="/login">P01 ログイン</a>
      </span>
    );
  }
  return (
    <span className="auth-bar">
      <span className="muted">{session.displayName ?? session.sub}</span>
      {" · "}
      <a href="/logout">ログアウト</a>
    </span>
  );
}
