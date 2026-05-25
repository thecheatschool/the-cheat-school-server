# Emiratiyo Investments — Environment Setup

## Local Configuration

Create a file named `src/main/resources/application-local.properties` (this file is gitignored).

### Server & Infrastructure

| Property | Example Value |
|---|---|
| `server.port` | `8080` |
| `spring.application.name` | `emiratiyo-api` |
| `cors.allowed-origins` | `http://localhost:5173,https://emiratiyo.com` |

### Database (PostgreSQL)

| Property | Example Value |
|---|---|
| `spring.datasource.url` | `jdbc:postgresql://host:5432/neondb` |
| `spring.datasource.username` | `neondb_owner` |
| `spring.datasource.password` | `your_password` |
| `spring.jpa.hibernate.ddl-auto` | `update` |

### Redis Cache (Upstash)

| Property | Example Value |
|---|---|
| `spring.data.redis.host` | `your-redis-host.upstash.io` |
| `spring.data.redis.port` | `6379` |
| `spring.data.redis.password` | `your_redis_password` |
| `spring.data.redis.ssl.enabled` | `true` |

### External API Keys

| Property | Description |
|---|---|
| `em.resend.api.key` | Resend API key for emails |
| `emira.gemini.primary-key` | Google Gemini API key |
| `emira.internal.secret` | Shared secret for `/api/v1/internal/*` |

### Resilience (Circuit Breakers)

| Property | Description |
|---|---|
| `resilience4j.circuitbreaker.instances.emiraGemini.failureRateThreshold` | AI Failure % before trip |
| `resilience4j.circuitbreaker.instances.resendEmail.failureRateThreshold` | Email Failure % before trip |

### Logging

```properties
logging.level.com.emiratiyo.api=INFO
logging.level.com.emiratiyo.api.service=DEBUG
```

---

## Local Setup Steps

1. **Install Java 21** and **Maven 3.9+**.
2. **PostgreSQL**: Ensure you have a local instance or a Neon.tech connection string.
3. **Redis**: Use Upstash (free tier) for a managed Redis instance.
4. **Build & Run**:
   ```bash
   mvn clean install
   mvn spring-boot:run
   ```
