# Emiratiyo Investments — Architecture

## Overview

Emiratiyo Investments (EM) is a namespaced sub-domain within the shared Spring Boot monolith. It provides real estate intelligence for Dubai through three feature sets: contact form, business setup inquiry, and Emira — an AI-powered property analyst backed by Google Gemini.

---

## Layers

| Layer | Package | Responsibility |
|---|---|---|
| Controller | `controller/` | HTTP request handling for all EM endpoints |
| Service | `service/em/` | Business logic — contact, business setup, Emira AI |
| Repository | `repository/` | Spring Data JPA repositories for EM entities |
| Model | `model/em/` | Request DTOs, JPA entities, Emira analysis types |
| Config | `config/` | Rate limiter (Bucket4j), circuit breaker (Resilience4j), async, CORS |
| Queue | `service/queue/` | Optional async email publishing via RabbitMQ |
| Util | `util/` | Input sanitization |
| Exception | `exception/` | Global exception handler (shared with TCS) |

---

## Auth & Security

- **No Spring Security** — no user sessions or JWT for contact/business endpoints.
- **Internal secret header** — all Emira AI endpoints (`/api/internal/*`) require the header `X-Internal-Key: <emira.internal.secret>`. Unauthorized requests receive an SSE error event or `403 Forbidden`.
- **Rate limiting (Emira)** — `Bucket4j` token bucket limits Emira analyse endpoint to `emira.rate-limit.requests-per-minute` (default: 10 req/min).
- **Circuit breaker (Emira)** — `Resilience4j` wraps Gemini API calls. Trips after 3 consecutive failures; auto-transitions to half-open after 30 seconds.
- **Primary/backup key** — Emira calls Gemini with a primary API key; on failure, retries with a backup key before tripping the circuit breaker.
- **CORS** — centrally configured. Allowed origins include Emiratiyo production domains.
- **Input sanitization** — all user text fields sanitized via `InputSanitizer.sanitize()`.
- **Bean validation** — Jakarta Validation on all request DTOs.

---

## Deployment

- **Platform**: Fly.io (shared with TCS)
- **App name**: `thecheatschool-api`
- **Region**: `bom` (Mumbai)
- **Memory**: 1 GB · 1 shared CPU
- **Port**: `8080` · HTTPS enforced
- **Auto-stop/start**: enabled

---

## Database

- **PostgreSQL** via Neon (serverless)
- EM-specific tables: `em_contact`, `em_business_setup_submission`, `emira_analysis`
- DDL: auto-managed by Hibernate (`ddl-auto=update`)

---

## External Integrations

| Integration | Purpose |
|---|---|
| [Resend](https://resend.com) | Transactional email for EM contact and business setup forms |
| [Google Gemini 2.5 Flash](https://ai.google.dev) | AI engine powering Emira analyst (`generativelanguage.googleapis.com`) |
| Neon PostgreSQL | Persistent storage — contacts, business setup submissions, analysis history |
| RabbitMQ / CloudAMQP | Optional async email queue (disabled by default) |

---

## Notable Patterns

- **Persist-first** — submissions saved before email is sent; failures marked `EMAIL_FAILED` for follow-up.
- **SSE streaming** — Emira analysis streams response word-by-word over Server-Sent Events with a 3-minute timeout.
- **Async analysis** — `EmiraService.analyse()` runs on Spring's `taskExecutor` thread pool (`@Async`).
- **Analysis history** — each successful Emira response is persisted to `emira_analysis` table with area, type, timestamp, and full response text.
- **Dual-key Gemini** — primary key used first; failure triggers automatic retry with backup key before circuit breaker trips.
