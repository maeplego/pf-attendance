export default function LoggedOutPage() {
  return (
    <main style={{ padding: "2rem", fontFamily: "system-ui" }}>
      <h1>ログアウトしました</h1>
      <p>
        <a href="/">打刻</a>へ戻るか、<a href="/login">再ログイン</a>してください。
      </p>
    </main>
  );
}
