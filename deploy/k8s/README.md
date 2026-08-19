# P09 attendance Kubernetes manifests

Spring Boot API + Next.js web。overlay smoke は `ATTENDANCE_DEV_AUTH` + `X-Dev-User-Sub`（シード従業員 `aoki.haru`）。単体 apply ではなく `pf-cloud-k8s` overlay `f-ops` から参照する。

Ingress（`pf-cloud-k8s`）:

| ホスト | Service | 用途 |
| --- | --- | --- |
| `attendance.localhost` | web:3019 | 打刻 UI |
| `attendance-api.localhost` | api:8019 | REST |

Postgres は platform の DB 名 `attendance`。

```powershell
cd ..\..\pf-cloud-k8s
.\scripts\cluster-smoke-f-ops.ps1
```
