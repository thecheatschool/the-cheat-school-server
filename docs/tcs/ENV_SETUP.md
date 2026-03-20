# TCS — Environment Setup

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
| `cors.allowed-origins` | Comma-separated list of allowed origins | ✅ | `http://localhost:5173,https://www.thecheatschool.com` |
| `cors.allowed-methods` | Allowed HTTP methods | ✅ | `GET,POST,PUT,DELETE,OPTIONS` |
| `cors.allowed-headers` | Allowed request headers | ✅ | `*` |
| `cors.allow-credentials` | Whether to allow credentials | ✅ | `true` |

### Database

| Variable | Description | Required | Example |
|---|---|---|---|
| `spring.datasource.url` | PostgreSQL JDBC connection URL | ✅ | `jdbc:postgresql://host:5432/db?sslmode=require` |
| `spring.datasource.username` | DB username | ✅ | `neondb_owner` |
| `spring.datasource.password` | DB password 🔒 | ✅ | `your_password_here` |
| `spring.datasource.driver-class-name` | JDBC driver | ✅ | `org.postgresql.Driver` |
| `spring.jpa.hibernate.ddl-auto` | Schema management | ✅ | `update` |
| `spring.jpa.show-sql` | Log SQL statements | ❌ | `true` |

### TCS Email (Resend)

| Variable | Description | Required | Example |
|---|---|---|---|
| `resend.api.key` | Resend API key for TCS emails 🔒 | ✅ | `re_xxxxxxxxxxxx` |
| `contact.recipient.email` | Destination for TCS contact submissions | ✅ | `thecheatschool@gmail.com` |

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
| `resend.api.key` | 🔒 Secret |
| `spring.rabbitmq.uri` | 🔒 Secret |
| `server.port`, `cors.*`, `spring.datasource.url`, log levels | ✅ Safe |

> Never commit `application-local.properties` to version control. It is in `.gitignore`.

---

## Local Setup Steps

```bash
# 1. Clone
git clone https://github.com/EmiratiyoInvestments/emiratiyo-investments-api.git
cd emiratiyo-investments-api

# 2. Create local properties file
touch src/main/resources/application-local.properties
# Populate it with the variables above (get secrets from your team lead)

# 3. Start the application
./mvnw spring-boot:run -Dspring.profiles.active=local

# 4. Verify
curl http://localhost:8080/api/health
```

Expected response:
```json
{
  "status": "success",
  "data": {
    "app": "UP",
    "database": "UP",
    "timestamp": "..."
  }
}
```
