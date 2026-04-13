# Habit Tracker — Backend

Spring Boot REST API for a gamified habit tracking application. Handles authentication, habit CRUD, daily completion tracking, streak calculation, XP progression, and statistics.

---

## Tech Stack

| Tool | Purpose |
|------|---------|
| Java 21 | Language |
| Spring Boot | Application framework |
| Spring Security | JWT authentication |
| Spring Data JPA | Database access |
| Hibernate | ORM |
| PostgreSQL | Database |
| Maven | Build tool |

---

## Prerequisites

- JDK 21+
- PostgreSQL running on port `5433`

---

## Database Setup

Create the database before starting the application:

```sql
CREATE DATABASE habitdb;
```

Default credentials (configured in `application.properties`):

| Setting | Value |
|---------|-------|
| Host | `localhost` |
| Port | `5433` |
| Database | `habitdb` |
| Username | `postgres` |
| Password | `postgres` |

---

## Environment Variables

| Variable | Required | Description |
|----------|:--------:|-------------|
| `JWT_SECRET` | Yes | Secret key used to sign JWT tokens (min. 32 chars recommended) |

Set it in your shell or create a `.env` file:

```bash
export JWT_SECRET=your-secret-key-here
```

---

## Running

```bash
./mvnw spring-boot:run
```

Server starts on **port 8081** (`http://localhost:8081`).

To build a JAR:

```bash
./mvnw clean package
java -jar target/tracker-*.jar
```

---

## API Reference

### Auth — `/api/v1/auth`

| Method | Path | Auth | Description |
|--------|------|:----:|-------------|
| `POST` | `/api/v1/auth/register` | No | Register a new user |
| `POST` | `/api/v1/auth/login` | No | Login, returns JWT token |
| `GET` | `/api/v1/auth/me` | Yes | Get current user profile |

**Register request body:**
```json
{ "name": "string", "email": "string", "password": "string (min 8 chars)" }
```

**Login request body:**
```json
{ "email": "string", "password": "string" }
```

**Auth response:**
```json
{ "token": "string", "email": "string" }
```

---

### Habits — `/api/v1/habits`

All habit endpoints require `Authorization: Bearer <token>`.

| Method | Path | Description |
|--------|------|-------------|
| `GET` | `/api/v1/habits` | List all habits for the current user |
| `POST` | `/api/v1/habits` | Create a new habit |
| `GET` | `/api/v1/habits/{id}` | Get a single habit |
| `PUT` | `/api/v1/habits/{id}` | Update a habit |
| `DELETE` | `/api/v1/habits/{id}` | Delete a habit |
| `POST` | `/api/v1/habits/{id}/complete` | Mark habit as complete for today |
| `GET` | `/api/v1/habits/dashboard` | Dashboard stats (totals, streak, XP, level) |
| `GET` | `/api/v1/habits/progress/weekly` | Completion counts for the last 7 days |
| `GET` | `/api/v1/habits/progress/heatmap` | Completion counts for the last 365 days |

**Create / Update habit body:**
```json
{ "name": "string", "description": "string (optional)" }
```

**Habit response:**
```json
{
  "id": "uuid",
  "name": "string",
  "description": "string",
  "streak": 0,
  "completedToday": false
}
```

**Dashboard response:**
```json
{
  "totalHabits": 0,
  "completedToday": 0,
  "longestStreak": 0,
  "xp": 0,
  "level": 1
}
```

---

## Architecture

```
AuthController / HabitController   ← HTTP only, no business logic
        ↓
AuthService / HabitService         ← Business logic, XP, streak calculation
        ↓
UserRepository / HabitRepository / HabitEntryRepository   ← Spring Data JPA
        ↓
PostgreSQL
```

**Key patterns:**
- Entities are never exposed directly — DTOs used for all API contracts
- `HabitMapper` / `UserMapper` handle entity ↔ DTO conversion
- `CurrentUserUtil` resolves the authenticated user from the Spring Security context
- `GlobalExceptionHandler` returns structured `ApiErrorResponse` for all errors
- JWT filter (`JwtAuthenticationFilter`) validates tokens on every protected request

---

## XP & Level System

- Each habit completion awards **10 XP**
- `level = xp / 100 + 1`
- XP is stored on the `User` entity; duplicate completions on the same day are idempotent (no double XP)

---

## Known Gaps

| Area | Gap |
|------|-----|
| Tests | Only a context-load smoke test — no unit or integration tests |
| API | No `DELETE /api/v1/habits/{id}/complete` (undo today's completion) |
| API | No token refresh endpoint |
| API | No pagination on `GET /api/v1/habits` |
| API | No achievement / milestone entity or endpoints |
| Config | CORS not explicitly configured (relies on Spring Boot defaults) |
| Config | `JWT_SECRET` has no startup validation — app will fail silently if unset |
