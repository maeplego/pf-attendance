# pf-attendance

学習用の勤怠です。架空の「開発部 8 名」の打刻、休憩控除した日次労働時間、月次カレンダーまでです。金額は出しません。**本番の勤怠 SaaS や給与計算、労基法準拠の置き換えではありません。**

| ディレクトリ | 役割 |
| --- | --- |
| `apps/api` | Java 21 / Spring Boot。打刻は追記。サーバー時刻が正 |
| `apps/web` | 打刻ホームと月次カレンダー |
| `deploy/` | Postgres + API + Web |

認証は開発ヘッダです。分数は整数（秒は切り捨て）。勤務日は打刻時刻の Asia/Tokyo 暦日です。夜勤（日をまたぐ勤務）はモデルしていません。

UTC 15:00 が JST 翌日 0:00 なので、23:59 の出勤と 00:00 の出勤は別日です。

## 起動

```powershell
copy deploy\.env.example deploy\.env
docker compose -f deploy/compose.yaml --env-file deploy/.env up --build
```

| URL | 用途 |
| --- | --- |
| http://localhost:3019 | 打刻 |
| http://localhost:3019/calendar | 月次カレンダー |
| http://localhost:3019/workflow | 申請・承認・工数・締め |
| http://localhost:8019/health | API |

既定ユーザーは `aoki.haru`（青木 陽）。上長デモは `sato.mei`。他人の打刻は日次サマリーに出ません。

## デモ

1. 出勤 → 休憩開始 → 休憩終了 → 退勤
2. 労働分が休憩控除後の整数になることを見る（例: 09:00–18:00 で昼 1 時間なら 480 分）
3. `sato.mei` に切り替えると、本日は空になる
4. 不正な順序（休憩中の退勤、二重出勤）は 409
5. 一般で休暇申請 → 上長で承認 → 上長で月次締め → 打刻が 409
6. 労働分を超える工数按分は 400。CSV は上長のみ

申請承認、工数按分、月次締め、未打刻一覧まであります。代理打刻はありません。給与計算はありません。

## テスト

ホストに JDK 21 が無いときは Maven イメージで実行できます。

```powershell
docker run --rm -v ${PWD}/apps/api:/src -w /src maven:3.9.9-eclipse-temurin-21 mvn -B test
```

設計の詳細は [portfolio-plan](https://github.com/maeplego/portfolio-plan) の `portfolio-plan/attendance/docs/` です。AWS 3-tier モジュールは [pf-cloud-aws](https://github.com/maeplego/pf-cloud-aws) で、ここでの Compose デモとは別経路です。

## ライセンスと利用条件

本リポジトリは **デモ・学習・社内評価用** です。現状品質に **保証はありません**。

- 許可: クローン、ローカル実行、学習、非本番の評価
- 別契約が必要: 本番運用、有償サービスへの組込み、再販・托管の提供

詳細は [LICENSE](./LICENSE) と [licensing.md](https://github.com/maeplego/portfolio-plan/blob/master/portfolio-plan/licensing.md) を参照してください。

