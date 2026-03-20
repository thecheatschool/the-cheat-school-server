# Emiratiyo Investments — Deployment

**Platform:** Fly.io
**Live URL:** `https://thecheatschool-api.fly.dev`
**App:** `thecheatschool-api` · Region: `bom` (Mumbai)

---

## Prerequisites

```bash
# Install Fly CLI
curl -L https://fly.io/install.sh | sh

# Authenticate
flyctl auth login --email [EMAIL_ADDRESS]
```

---

## Deploy

```bash
# From repo root
flyctl deploy
```

Fly.io builds the Docker image from `Dockerfile` in the root and deploys it.

---

## Logs

```bash
flyctl logs
flyctl logs --instance <instance-id>   # tail a specific machine
```

---

## Secrets

EM-specific secrets to set on Fly.io:

| Secret | Description |
|---|---|
| `EM_RESEND_API_KEY` | Resend API key for EM emails |
| `EM_CONTACT_RECIPIENT_EMAIL` | Destination for EM contact/business submissions |
| `EMIRA_GEMINI_PRIMARY_KEY` | Primary Google Gemini API key |
| `EMIRA_GEMINI_BACKUP_KEY` | Backup Google Gemini API key |
| `EMIRA_INTERNAL_SECRET` | Shared secret for `X-Internal-Key` header |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL password |

```bash
# View current secrets (names only, values hidden)
flyctl secrets list

# Set a secret
flyctl secrets set EMIRA_GEMINI_PRIMARY_KEY=AIzaSy...

# Set all EM secrets at once
flyctl secrets set \
  EM_RESEND_API_KEY=re_xxxxxxxxxxxx \
  EM_CONTACT_RECIPIENT_EMAIL=algaarigroup@gmail.com \
  EMIRA_GEMINI_PRIMARY_KEY=AIzaSy... \
  EMIRA_GEMINI_BACKUP_KEY=AIzaSy... \
  EMIRA_INTERNAL_SECRET=your_secret \
  SPRING_DATASOURCE_PASSWORD=your_password
```

> Secrets are injected as environment variables at runtime. Map them to the property names in `application-local.properties` via your `fly.toml` or Spring property binding.

---

## `fly.toml` Reference

```toml
app = 'thecheatschool-api'
primary_region = 'bom'

[build]

[http_service]
  internal_port = 8080
  force_https = true
  auto_stop_machines = true
  auto_start_machines = true
  min_machines_running = 0
  processes = ['app']

[[vm]]
  memory = '1gb'
  cpu_kind = 'shared'
  cpus = 1
```

- `auto_stop_machines = true` — scales to zero when idle.
- `auto_start_machines = true` — cold-starts on incoming request.
- `force_https = true` — HTTP redirects to HTTPS automatically.

---

## Rollback

```bash
# List recent releases
flyctl releases list

# Roll back to a specific version
flyctl deploy --image <image-ref-from-releases-list>
```

Or redeploy from a previous Git commit:
```bash
git checkout <previous-commit>
flyctl deploy
```
