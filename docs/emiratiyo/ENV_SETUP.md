# Emiratiyo Investments — Environment Setup

## Environment Variables

All configuration lives in `src/main/resources/application-local.properties`. This file is gitignored and must be created locally.

### Server

| Variable | Description | Required | Example |
|---|---|---|---|
| `server.port` | HTTP port the app binds to | ✅ | `8080` |
| `server.address` | Bind address | ✅ | `0.0.0.0` |
| `spring.application.name` | Application name for logs | ✅ | `thecheatschoolserver` |

### CORS

| Variable | Description | Required | Example |
|---|---|---|---|
| `cors.allowed-origins` | Comma-separated allowed origins | ✅ | `http://localhost:5173,https://emiratiyo.com` |
| `cors.allowed-methods` | Allowed HTTP methods | ✅ | `GET,POST,PUT,DELETE,OPTIONS` |
| `cors.allowed-headers` | Allowed request headers | ✅ | `*` |
| `cors.allow-credentials` | Allow credentials | ✅ | `true` |

### Database

| Variable | Description | Required | Example |
|---|---|---|---|
| `spring.datasource.url` | PostgreSQL JDBC connection URL | ✅ | `jdbc:postgresql://host:5432/db?sslmode=require` |
| `spring.datasource.username` | DB username | ✅ | `neondb_owner` |
| `spring.datasource.password` | DB password 🔒 | ✅ | `your_password_here` |
| `spring.datasource.driver-class-name` | JDBC driver | ✅ | `org.postgresql.Driver` |
| `spring.jpa.hibernate.ddl-auto` | Schema management | ✅ | `update` |
| `spring.jpa.show-sql` | Log SQL statements | ❌ | `true` |

### EM Email (Resend)

| Variable | Description | Required | Example |
|---|---|---|---|
| `em.resend.api.key` | Resend API key for EM emails 🔒 | ✅ | `re_xxxxxxxxxxxx` |
| `em.contact.recipient.email` | Destination for EM contact/business submissions | ✅ | `algaarigroup@gmail.com` |
| `em.contact.from.email` | Sender address for EM emails | ✅ | `onboarding@resend.dev` |

### Emira AI (Google Gemini)

| Variable | Description | Required | Example |
|---|---|---|---|
| `emira.gemini.primary-key` | Primary Gemini API key 🔒 | ✅ | `AIzaSy...` |
| `emira.gemini.backup-key` | Backup Gemini API key 🔒 | ✅ | `AIzaSy...` |
| `emira.internal.secret` | Shared secret for `X-Internal-Key` header 🔒 | ✅ | `49352` |
| `emira.rate-limit.requests-per-minute` | Max Emira requests per minute (Bucket4j) | ❌ | `10` |

### Resilience4j (Circuit Breaker for Emira)

| Variable | Description | Required | Example |
|---|---|---|---|
| `resilience4j.circuitbreaker.instances.emiraGemini.slidingWindowSize` | Window size for failure tracking | ❌ | `3` |
| `resilience4j.circuitbreaker.instances.emiraGemini.minimumNumberOfCalls` | Min calls before tripping | ❌ | `3` |
| `resilience4j.circuitbreaker.instances.emiraGemini.waitDurationInOpenState` | Open-state wait before half-open | ❌ | `30s` |
| `resilience4j.circuitbreaker.instances.emiraGemini.failureRateThreshold` | Failure % to trip breaker | ❌ | `100` |
| `resilience4j.circuitbreaker.instances.emiraGemini.automaticTransitionFromOpenToHalfOpenEnabled` | Auto-transition to half-open | ❌ | `true` |

### Queue (RabbitMQ — optional)

| Variable | Description | Required | Example |
|---|---|---|---|
| `queue.enabled` | Enable async email queue | ❌ | `false` |
| `spring.rabbitmq.uri` | CloudAMQP connection URI 🔒 | Only if `queue.enabled=true` | `amqps://user:pass@host/vhost` |

### Logging

| Variable | Description | Required | Example |
|---|---|---|---|
| `logging.level.com.thecheatschool.server` | Root log level | ❌ | `INFO` |
| `logging.level.com.thecheatschool.server.service` | Service layer log level | ❌ | `DEBUG` |

---

## Secrets vs Safe to Share

| Variable | Secret? |
|---|---|
| `spring.datasource.password` | 🔒 Secret |
| `em.resend.api.key` | 🔒 Secret |
| `emira.gemini.primary-key` | 🔒 Secret |
| `emira.gemini.backup-key` | 🔒 Secret |
| `emira.internal.secret` | 🔒 Secret |
| `spring.rabbitmq.uri` | 🔒 Secret |
| `server.*`, `cors.*`, `spring.datasource.url`, log levels, rate-limit, circuit breaker config | ✅ Safe |

> Never commit `application-local.properties` to version control. It is in `.gitignore`.

---

## Local Setup Steps

```bash
# 1. Clone
git clone https://github.com/EmiratiyoInvestments/emiratiyo-investments-api.git
cd emiratiyo-investments-api

# 2. Create local properties file
touch src/main/resources/application-local.properties
# Populate with the variables above (get secrets from your team lead)

# 3. Start the application
./mvnw spring-boot:run -Dspring.profiles.active=local

# 4. Verify app is up
curl http://localhost:8080/api/health

# 5. Test Emira AI (SSE)
curl -X POST http://localhost:8080/api/internal/analyse \
  -H "X-Internal-Key: <your_secret>" \
  -H "Content-Type: application/json" \
  -d '{"area":"Dubai Marina","analysisType":"MARKET_PULSE","marketContext":"High transaction volume in Q4 2024"}' \
  --no-buffer
```
