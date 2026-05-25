# Emiratiyo Investments — Contributing

## Branching Strategy

| Branch | Purpose |
|---|---|
| `main` | Production — deployed to Fly.io |
| `dev` | Integration branch — all PRs target here |
| `feature/<name>` | New features |
| `fix/<name>` | Bug fixes |
| `chore/<name>` | Maintenance (dependencies, cleanup) |

---

## Local Setup

**Prerequisites:** Java 21, Maven 3.9+, PostgreSQL, Gemini API key.

```bash
# 1. Clone the repo
git clone https://github.com/EmiratiyoInvestments/emiratiyo-investments-api.git
cd emiratiyo-investments-api

# 2. Run locally
mvn clean install
mvn spring-boot:run
```

Server starts at `http://localhost:8081`.

Test the API:
```bash
curl -X POST http://localhost:8081/api/v1/internal/emira/analyse \
  -H "X-Internal-Key: your_secret" \
  -H "Content-Type: application/json" \
  -d '{"area":"Dubai Marina","analysisType":"PRICE_FORECAST"}'
```

---

## Code Style

- **Java Records** — Always use `record` for Request/Response DTOs.
- **Lombok** — Use `@Builder`, `@RequiredArgsConstructor`, and `@Slf4j`.
- **Entities vs DTOs** — Never return an Entity directly from a Controller. Map it to a DTO.
- **Async & Non-blocking** — Use `WebClient` for external calls. Use `@Async` for tasks like email dispatch.
- **Naming Conventions**:
    - Entities: `XxxEntity`
    - Requests: `XxxRequest`
    - Responses: `XxxResponse` (or `ApiResponse`)
- **Validation** — All incoming data must be validated using Jakarta annotations.
- **Sanitization** — Use `InputSanitizer` before persisting any user text.

---

## PR Process

1. Branch off `dev`.
2. Keep changes focused and atomic.
3. Ensure `mvn clean install` passes locally.
4. Describe the impact of your changes in the PR description.
5. All merges to `main` happen via `dev`.

---

## Tests

Run tests using:
```bash
mvn test
```
*Note: Ensure you have mock profiles or environment variables set up for tests that interact with external APIs.*
