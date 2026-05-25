# Emiratiyo Investments — API Reference

Base URL: `http://localhost:8081` (Local) / `https://emiratiyo-api.fly.dev` (Production)

---

## Standard Response Format

All API endpoints return a standardized JSON envelope:

```json
{
  "status": "success | error",
  "data": { ... },
  "message": "Optional descriptive message"
}
```

---

## Contact Form

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/contact` | Submit a contact message |
| `GET` | `/api/v1/contact/failed` | List submissions with `EMAIL_FAILED` status |

**Request Body (`POST /api/v1/contact`):**
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
| `POST` | `/api/v1/business-setup` | Submit a business setup inquiry |
| `GET` | `/api/v1/business-setup/failed` | List submissions with `EMAIL_FAILED` status |

**Request Body (`POST /api/v1/business-setup`):**
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

> All `/api/v1/internal/*` endpoints require the header `X-Internal-Key: <secret>`. 

### Analyse

Runs a non-blocking AI property analysis using Gemini 2.5 Flash.

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/v1/internal/emira/analyse` | Run a property analysis |

**Headers:**
```
X-Internal-Key: <emira.internal.secret>
Content-Type: application/json
```

**Request Body:**
```json
{
  "area": "Dubai Marina",
  "analysisType": "PRICE_FORECAST",
  "marketContext": "Recent transaction data: ...",
  "additionalContext": "Looking for 2BR apartments"
}
```

**Response Data (`data` field):**
A formatted string containing the AI's full market analysis, structured by the requested `analysisType`.

---

### History

Access the audit trail of previous AI analyses.

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/v1/internal/emira/history` | List analysis history (lightweight) |
| `GET` | `/api/v1/internal/emira/history/{id}` | Get full analysis details |

**History Item Schema:**
```json
{
  "id": 123,
  "area": "Dubai Marina",
  "analysisType": "PRICE_FORECAST",
  "createdAt": "2024-05-25T10:00:00"
}
```
