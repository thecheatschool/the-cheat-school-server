# Emiratiyo Investments — API Reference

Base URL: `https://thecheatschool-api.fly.dev`

All standard responses:
```json
{
  "status": "success | error",
  "data": ...,
  "message": "..."
}
```

Emira AI endpoints respond with **Server-Sent Events (SSE)**, not JSON.

---

## Contact Form

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/em/contact` | Endpoint info |
| `POST` | `/api/em/contact` | Submit a contact message |
| `GET` | `/api/em/contact/failed` | List submissions with `EMAIL_FAILED` status |

**Request body (`POST /api/em/contact`):**
```json
{
  "name": "Ahmed Al Mansouri",
  "phone": "+971501234567",
  "email": "ahmed@example.com",
  "message": "I'm interested in off-plan properties in JVC."
}
```

| Field | Type | Required | Validation |
|---|---|---|---|
| `name` | string | ✅ | Not blank |
| `phone` | string | ✅ | 7–20 chars, digits/+/()-/ space |
| `email` | string | ✅ | Valid email format |
| `message` | string | ❌ | Optional |

---

## Business Setup

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/em/business-setup` | Endpoint info |
| `POST` | `/api/em/business-setup` | Submit a business setup inquiry |
| `GET` | `/api/em/business-setup/failed` | List submissions with `EMAIL_FAILED` status |

**Request body (`POST /api/em/business-setup`):**
```json
{
  "fullName": "Sarah Johnson",
  "email": "sarah@company.com",
  "mobileNumber": "+447700900123",
  "countryOfResidence": "United Kingdom"
}
```

| Field | Type | Required | Validation |
|---|---|---|---|
| `fullName` | string | ✅ | Not blank |
| `email` | string | ✅ | Valid email format |
| `mobileNumber` | string | ✅ | 7–20 chars, digits/+/()-/ space |
| `countryOfResidence` | string | ✅ | Not blank |

---

## Emira AI Analyst (Internal)

> All `/api/internal/*` endpoints require the header `X-Internal-Key: <secret>`. Unauthorized requests return an SSE error event.

### Analyse

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/internal/analyse` | Run an AI property analysis (SSE stream) |

**Headers:**
```
X-Internal-Key: <emira.internal.secret>
Content-Type: application/json
```

**Request body:**
```json
{
  "area": "Dubai Marina",
  "analysisType": "PRICE_FORECAST",
  "marketContext": "Recent transaction data: ...",
  "additionalContext": "Looking for 2BR apartments"
}
```

| Field | Type | Required | Values |
|---|---|---|---|
| `area` | string | ✅ | Any Dubai area name |
| `analysisType` | enum | ✅ | `PRICE_FORECAST`, `RENTAL_YIELD`, `GROWTH_DRIVERS`, `RISK_ASSESSMENT`, `MARKET_PULSE` |
| `marketContext` | string | ✅ | Market data to pass to AI |
| `additionalContext` | string | ❌ | Optional user context |

**Response:** SSE stream. Each event contains a text chunk. Final event completes the stream.

**Rate limit:** `emira.rate-limit.requests-per-minute` (default: 10 req/min).

---

### Analysis History

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/internal/history` | List all past analyses (id, area, type, createdAt) |
| `GET` | `/api/internal/history/{id}` | Get full analysis by ID |
| `DELETE` | `/api/internal/history/{id}` | Delete an analysis record |

**Headers:** `X-Internal-Key: <secret>` required on all history endpoints.
