# pf-attendance

P09 勤怠・工数の製品リポジトリです。**学習用であり、本番の勤怠 SaaS や給与計算、労基法準拠の置き換えではありません。** 簡易モデルです。金額は出しません。従業員は架空の「開発部 8 名」だけです。

いまのスライスは **従業員マスタ、打刻イベント、日次労働時間（休憩控除）** です。月次カレンダー、申請承認、工数按分、月次締め、P01 接続は未着手です。夜勤は入れません（勤務日は打刻時刻の Asia/Tokyo 暦日）。

```
apps/api    Java 21 / Spring Boot。打刻は追記。サーバー時刻が正
apps/web    Next.js。打刻ホームと本日サマリー
deploy/     Postgres + API + Web
```

認証は `X-Dev-User-Sub`（P01 OIDC は未配線）。分数は整数（秒は切り捨て）。

## 日境界（Asia/Tokyo）

| サーバー時刻 (UTC) | JST | 勤務日 |
| --- | --- | --- |
| 2026-08-18T14:59:00Z | 2026-08-18 23:59 | 2026-08-18 |
| 2026-08-18T15:00:00Z | 2026-08-19 00:00 | 2026-08-19 |

23:59 の出勤と 00:00 の出勤は別日です。日をまたぐ退勤は夜勤になるので、このスライスではモデルしません。

## 起動（Compose）

```powershell
copy deploy\.env.example deploy\.env
docker compose -f deploy/compose.yaml --env-file deploy/.env up --build
```

| URL | 用途 |
| --- | --- |
| http://localhost:3019 | 打刻ホーム |
| http://localhost:8019/health | API liveness |
| http://localhost:8019/ready | API readiness（Postgres ping） |

既定ユーザーは `aoki.haru`（青木 陽）。上長デモは `sato.mei`。他人の打刻は日次サマリーに出ません。

## デモ手順

1. http://localhost:3019 で出勤 → 休憩開始 → 休憩終了 → 退勤
2. 労働（分）が休憩控除後の整数になることを見る
3. 従業員を `sato.mei` に切り替えると空の本日になる
4. 不正な順序（休憩中の退勤、二重出勤）は 409

例: 09:00–18:00 JST で昼 1 時間休憩なら労働 480 分。

## テスト

ホストに JDK 21 が無い場合は Maven イメージで実行します。

```powershell
docker run --rm -v ${PWD}/apps/api:/src -w /src maven:3.9.9-eclipse-temurin-21 mvn -B test
```

DB なし。日境界と休憩控除は純関数、打刻 HTTP はメモリ店舗 + MockMvc。

## 既知の制限

- 月次カレンダー、修正申請、承認、有給、工数按分、締め、CSV、未打刻リマインドは無い
- 代理打刻と監査ログは無い
- 締め後 409 は未実装（締め自体が未実装）
- overlay F / k8s は未着手
- 36 協定の法解釈はしない

設計: `project/portfolio-plan/attendance/DESIGN.md`  
人間向け書類: `project/portfolio-plan/attendance/docs/`

AWS 3-tier（`../pf-cloud-aws`、P02 アイデア 16）はこの製品の Compose 単体デモとは別経路です。環境変数名は Compose と同じです。overlay F は未着手です。
