# TCS — Architecture

## Overview

Monolithic Spring Boot application. TCS (The Cheat School) and Emiratiyo (EM) run as namespaced sub-domains within the same deployable JAR.

---

## Layers

| Layer | Package | Responsibility |
|---|---|---|
| Controller | `controller/` | HTTP request handling, input validation, response shaping |
| Service | `service/tcs/` | Business logic — form processing, email dispatch, status tracking |
| Repository | `repository/` | Database access via Spring Data JPA |
| Model | `model/tcs/` | Request DTOs and JPA entity classes |
| Config | `config/` | CORS, rate limiting, async, RabbitMQ |
| Util | `util/` | Input sanitization |
| Exception | `exception/` | Global error handling |

---

## Authentication & Security

- **No Spring Security** — no user sessions or JWT.
- **Rate limiting** — `RateLimitingInterceptor` enforces max 5 POST requests/minute/IP on `/api/contact`. In-memory using `ConcurrentHashMap`.
- **CORS** — centrally configured in `CorsConfig`. Allowlist controlled by `cors.allowed-origins` property.
- **Input sanitization** — all user text fields pass through `InputSanitizer` before persistence.
- **Bean validation** — `@Valid` with Jakarta Validation annotations (`@NotBlank`, `@Email`, `@Pattern`) on all request DTOs.
- **Fallback POST handler** — tolerates mis-labeled `Content-Type` (e.g. `text/plain`) and still parses JSON body via `ObjectMapper`.

---

## Deployment

- **Platform**: Fly.io
- **App name**: `thecheatschool-api`
- **Region**: `bom` (Mumbai)
- **Memory**: 1 GB · 1 shared CPU
- **Port**: `8080` · HTTPS enforced
- **Auto-stop/start**: enabled (scales to zero when idle)
- **Container**: Docker (`Dockerfile` in root)

---

## Database

- **PostgreSQL** via [Neon](https://neon.tech) (serverless, connection pooler enabled)
- **DDL**: `spring.jpa.hibernate.ddl-auto=update`
- Tables auto-created from JPA entities on startup

---

## External Integrations

| Integration | Purpose |
|---|---|
| [Resend](https://resend.com) | Transactional email for TCS contact + notify-me forms |
| Neon PostgreSQL | Persistent storage for form submissions |
| RabbitMQ / CloudAMQP | Optional async email queue (disabled by default via `queue.enabled=false`) |

---

## Notable Patterns

- **Persist-first strategy** — submissions are saved to DB before email is attempted; if email fails, status is set to `EMAIL_FAILED` for manual follow-up.
- **Queue optional** — `@ConditionalOnProperty(name = "queue.enabled", havingValue = "true")` gates all RabbitMQ beans.
- **Async config** — `AsyncConfig` provides a `taskExecutor` bean for non-blocking operations.
- **Global exception handler** — `@RestControllerAdvice` handles `ConstraintViolationException`, `MethodArgumentTypeMismatchException`, and all unhandled exceptions with structured `ApiResponse` bodies.
