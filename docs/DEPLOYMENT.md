# Emiratiyo Investments — Deployment

**Platform:** Fly.io
**Live URL:** `https://emiratiyo-api.fly.dev`
**App:** `emiratiyo-api` · Region: `bom` (Mumbai)

---

## Prerequisites

```bash
# Install Fly CLI
curl -L https://fly.io/install.sh | sh

# Authenticate
flyctl auth login
```

---

## Deploy

```bash
# From repo root
flyctl deploy
```

The application is containerized using the `Dockerfile` in the root and deployed as a micro-VM.

---

## Secrets Management

The following secrets must be set in the Fly.io environment:

| Secret | Description |
|---|---|
| `EM_RESEND_API_KEY` | Resend API key for email delivery |
| `EM_CONTACT_RECIPIENT_EMAIL` | Target email for submissions |
| `EMIRA_GEMINI_PRIMARY_KEY` | Primary AI key |
| `EMIRA_GEMINI_BACKUP_KEY` | Backup AI key |
| `EMIRA_INTERNAL_SECRET` | Header secret for analyst endpoints |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL password |
| `SPRING_DATA_REDIS_PASSWORD` | Upstash Redis password |

**Setting Secrets:**
```bash
flyctl secrets set EMIRA_GEMINI_PRIMARY_KEY=AIzaSy...
```

---

## Fly.toml Overview

```toml
app = 'emiratiyo-api'
primary_region = 'bom'

[http_service]
  internal_port = 8081
  force_https = true
  auto_stop_machines = true
  auto_start_machines = true
  min_machines_running = 0
```

- **Internal Port**: The Spring Boot app is configured to listen on `8081`.
- **Auto-stop/start**: The instance automatically scales to zero when idle and cold-starts on the next request.
