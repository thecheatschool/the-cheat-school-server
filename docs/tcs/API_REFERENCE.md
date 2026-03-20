# TCS — API Reference

Base URL: `https://thecheatschool-api.fly.dev`

All responses follow the shape:
```json
{
  "status": "success | error",
  "data": ...,
  "message": "..."
}
```

---

## Health

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/health` | App and database liveness check |

**Response includes:** `app`, `database` (UP/DOWN), `databaseCountCheck`, `timestamp`.

---

## Contact Form

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/contact` | Endpoint info |
| `POST` | `/api/contact` | Submit a TCS contact/registration form |
| `GET` | `/api/contact/failed` | List submissions with `EMAIL_FAILED` status |

**Rate limit:** 5 POST requests / minute / IP.

**Request body (`POST /api/contact`):**
```json
{
  "fullName": "John Doe",
  "email": "john@example.com",
  "phoneNumber": "9876543210",
  "college": "IIT Bombay",
  "yearOfStudy": "3rd Year",
  "branch": "Computer Science",
  "hearAboutUs": "Instagram",
  "hearAboutUsOther": ""
}
```

| Field | Type | Required | Validation |
|---|---|---|---|
| `fullName` | string | ✅ | Not blank |
| `email` | string | ✅ | Valid email format |
| `phoneNumber` | string | ✅ | Exactly 10 digits |
| `college` | string | ✅ | Not blank |
| `yearOfStudy` | string | ✅ | Not blank |
| `branch` | string | ✅ | Not blank |
| `hearAboutUs` | string | ✅ | Not blank |
| `hearAboutUsOther` | string | ❌ | Optional |

---

## Notify Me

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/notify-me` | Endpoint info |
| `POST` | `/api/notify-me` | Subscribe to course launch notifications |
| `GET` | `/api/notify-me/failed` | List signups with `EMAIL_FAILED` status |

**Request body (`POST /api/notify-me`):**
```json
{
  "name": "Jane Doe",
  "email": "jane@example.com",
  "phoneNumber": "9123456789"
}
```

| Field | Type | Required | Validation |
|---|---|---|---|
| `name` | string | ✅ | Not blank |
| `email` | string | ✅ | Valid email format |
| `phoneNumber` | string | ✅ | Exactly 10 digits |

> Duplicate emails are deduplicated — existing record is updated rather than creating a new one.
