# Emiratiyo Investments — Contributing

## Branching Strategy

| Branch | Purpose |
|---|---|
| `main` | Production — deployed to Fly.io |
| `dev` | Integration branch — all PRs target here |
| `feature/<name>` | New features |
| `fix/<name>` | Bug fixes |
| `chore/<name>` | Maintenance (deps, cleanup, config) |

---

## Local Setup

**Prerequisites:** Java 17, Maven 3.8+, a running PostgreSQL instance (or Neon connection string), Gemini API key.

```bash
# 1. Clone the repo
git clone https://github.com/EmiratiyoInvestments/emiratiyo-investments-api.git
cd emiratiyo-investments-api

# 2. Configure local properties
cp src/main/resources/application-local.properties.example src/main/resources/application-local.properties
# Fill in all required values — see docs/emiratiyo/ENV_SETUP.md

# 3. Run locally
./mvnw spring-boot:run -Dspring.profiles.active=local
```

Server starts at `http://localhost:8080`.

Test Emira locally:
```bash
curl -X POST http://localhost:8080/api/internal/analyse \
  -H "X-Internal-Key: 49352" \
  -H "Content-Type: application/json" \
  -d '{"area":"Dubai Marina","analysisType":"PRICE_FORECAST","marketContext":"Sample data"}' \
  --no-buffer
```

---

## PR Process

1. Branch off `dev` — never commit directly to `main`.
2. Keep PRs focused — one concern per PR.
3. PR title format: `[TCS|EM|SHARED] Short description`
4. Describe what changed and why in the PR body.
5. All PRs require at least one review before merge.
6. Squash merge into `dev`. Merge commits into `main`.

---

## Code Style

- **Lombok** — use `@Data`, `@RequiredArgsConstructor`, `@Slf4j` consistently.
- **Naming** — services: `XxxService`, controllers: `XxxController`, repos: `XxxRepository`. EM-specific classes prefixed with `EM` or named `Emira*`.
- **Validation** — all request DTOs must use Jakarta Validation annotations (`@NotBlank`, `@Email`, `@Pattern`).
- **Logging** — use `log.info` for normal flow, `log.warn` for recoverable issues, `log.error` for failures. Never log full email addresses — use `maskEmail()` pattern.
- **Input sanitization** — all user-supplied text must pass through `InputSanitizer.sanitize()` before persisting.
- **No raw SQL** — use Spring Data JPA repository methods only.
- **SSE / async** — Emira analysis must remain `@Async` on `taskExecutor`. Never block the request thread.
- **Secrets** — never hardcode API keys. Always inject via `@Value` from properties.

---

## Tests

Currently no automated test suite beyond `spring-boot-starter-test` on the classpath.

To run the placeholder test:
```bash
./mvnw test
```

> When adding features, add corresponding unit tests under `src/test/java/`. For Emira, mock the Gemini HTTP call in tests.
