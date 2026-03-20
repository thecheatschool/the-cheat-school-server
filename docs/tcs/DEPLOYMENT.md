# TCS — Deployment

**Platform:** Fly.io
**Live URL:** `https://thecheatschool-api.fly.dev`
**App:** `thecheatschool-api` · Region: `bom` (Mumbai)

---

## Prerequisites

```bash
# Install Fly CLI
curl -L https://fly.io/install.sh | sh

# Authenticate
flyctl auth login --email thecheatschoolcode@gmail.com
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

TCS-specific secrets to set on Fly.io:

| Secret | Description |
|---|---|
| `RESEND_API_KEY` | Resend API key for TCS emails |
| `CONTACT_RECIPIENT_EMAIL` | Destination for TCS contact submissions |
| `SPRING_DATASOURCE_PASSWORD` | PostgreSQL password |

```bash
# View current secrets (names only, values hidden)
flyctl secrets list

# Set a secret
flyctl secrets set RESEND_API_KEY=re_xxxxxxxxxxxx

# Set multiple at once
flyctl secrets set \
  RESEND_API_KEY=re_xxxxxxxxxxxx \
  CONTACT_RECIPIENT_EMAIL=thecheatschool@gmail.com \
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
