# Emiratiyo Investments — Architecture

## Overview

Emiratiyo Investments (EM) API is a high-performance Spring Boot monolith providing real estate intelligence for Dubai. It features automated contact handling, business setup inquiries, and Emira — an AI-powered property analyst backed by Google Gemini 2.5 Flash.

---

## Layers

| Layer | Package | Responsibility |
|---|---|---|
| Controller | `controller/` | Versioned REST endpoints (`/api/v1/...`) |
| Service | `service/` | Core business logic and external API orchestrations |
| Repository | `repository/` | Spring Data JPA interfaces for database access |
| Entity | `entity/` | JPA models representing the PostgreSQL schema |
| DTO | `dto/` | Immutable Java Records for API request/response contracts |
| Config | `config/` | WebClient, Redis, Security, and Resilience4j configurations |
| Exception | `exception/` | Custom business exceptions and Global Exception Handler |
| Util | `util/` | Sanitization, rate-limiting interceptors, and ID generators |

---

## Auth & Security

- **Internal secret header** — All Emira AI endpoints require the header `X-Internal-Key: <emira.internal.secret>`.
- **Rate limiting** — `Bucket4j` token bucket limits internal AI endpoints to prevent abuse.
- **CORS** — Centrally configured to allow trusted Emiratiyo frontend domains.
- **Input sanitization** — All user-supplied text is sanitized via `InputSanitizer` to prevent XSS and injection attacks.
- **Bean validation** — Strict Jakarta Validation on all incoming DTOs.

---

## Resilience Patterns

- **Circuit Breakers** — Powered by `Resilience4j`.
    - `emiraGemini`: Protects against AI outages. Trips after 3 failures.
    - `resendEmail`: Protects against email delivery failures. Trips after 5 failures.
- **Dual-Key Fallback** — The AI service automatically switches to a backup API key if the primary key fails, ensuring maximum uptime.
- **Reactive HTTP** — All external calls use **Spring WebClient** for non-blocking, resource-efficient communication.

---

## Deployment

- **Platform**: Fly.io
- **App name**: `emiratiyo-api`
- **Region**: `bom` (Mumbai)
- **Port**: `8081` (Internal) · HTTPS enforced externally
- **Runtime**: Java 21 (Docker-based)

---

## Database

- **PostgreSQL** via Neon (serverless)
- Tables: `em_contact_submissions`, `em_business_setup_submissions`, `em_analyses`
- DDL: Managed via Hibernate `ddl-auto=update`

---

## External Integrations

| Integration | Purpose |
|---|---|
| [Resend](https://resend.com) | Transactional email delivery for form submissions |
| [Google Gemini 2.5 Flash](https://ai.google.dev) | AI engine powering market analysis |
| [Upstash Redis](https://upstash.com) | Distributed caching for AI history and rate limiting |
