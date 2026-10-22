# TaskForge

Team issue and project management backend with a React web client. Projects are membership-gated, issues follow an enforced lifecycle, and the dashboard aggregates work per project.

[![backend](https://github.com/HimanshuJha-2005/TaskForge/actions/workflows/backend.yml/badge.svg)](https://github.com/HimanshuJha-2005/TaskForge/actions)

## Features

- Registration and login with JWT and BCrypt. Roles `ADMIN`, `PROJECT_MANAGER`, and `DEVELOPER`.
- Projects with unique keys and member management. Members see their projects; admins see all.
- Membership check on every project-scoped request. Non-members get 403.
- Issues with assignment, priority, full-text search, status/priority/assignee filters, and pagination.
- Issue lifecycle `OPEN -> IN_PROGRESS -> IN_REVIEW -> RESOLVED -> CLOSED`, with rework loops. Illegal transitions return 422.
- Comment threads per issue.
- Per-project dashboard with totals, breakdowns by status, priority, and assignee, plus recent issues.
- React client with protected routes, filters, one-click transitions, and loading, error, and empty states.

## Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot 3.2.5, Spring Security, Spring Data JPA, JJWT |
| Database | PostgreSQL 16, Flyway migrations, H2 for tests |
| Frontend | React 19, React Router 7, Vite |
| Build | Maven, Docker Compose, GitHub Actions |

## Quick start

```bash
# Backend tests (no database needed)
mvn test

# Full stack
export APP_JWT_SECRET=$(python3 -c "import secrets,base64;print(base64.b64encode(secrets.token_bytes(32)).decode())")
export DB_PASSWORD=dev-password
docker compose up --build          # API on :8081, Postgres on :5433

# Frontend
cd frontend && npm install && npm run dev   # UI on :5174
```

## API

| Method | Endpoint | Auth | Notes |
|---|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | none | Returns a JWT; 409 on duplicate email |
| GET | `/api/projects` | JWT | Own projects (all for admins) |
| POST | `/api/projects` | PM/Admin | 409 on duplicate key |
| GET/PUT | `/api/projects/{id}` | Member | Project detail and update |
| POST | `/api/projects/{id}/members` | PM/Admin | Add a member by email |
| GET | `/api/projects/{id}/dashboard` | Member | Aggregates and recent issues |
| GET/POST | `/api/issues?projectId=...` | Member | Search, filters, pagination |
| GET/PUT | `/api/issues/{id}` | Member | Detail and update |
| PATCH | `/api/issues/{id}/status` | Member | 422 on illegal transition |
| GET/POST | `/api/issues/{id}/comments` | Member | Comment thread |

Errors use one shape: `{timestamp, status, error, message, path}`.

## Architecture

```
React (Vite, :5174) -- JSON + Bearer JWT --> Spring Boot (:8081) --> PostgreSQL (:5433 host)
                                               Controller -> Service (@Transactional) -> Repository
                                               ProjectService.requireMember gates scoped calls
                                               GlobalExceptionHandler -> uniform error JSON
```

Project access is checked in the service layer rather than only in controllers. Issue transitions are validated against an explicit state machine, so a client cannot skip states by calling the update endpoint directly.

## Layout

```
src/main/java/com/taskforge/
  auth/       users, registration, login, JWT
  project/    projects, memberships
  issue/      issues, lifecycle, comments
  common/     error handling
  config/     security configuration
  health/     liveness endpoint
src/main/resources/db/migration/   versioned schema
frontend/src/                      pages, auth context, API client
```

## Testing

Six integration tests cover the full issue lifecycle (illegal transition, resolution, comment, dashboard), role and membership authorization, duplicate project keys, and search filters. `mvn test` uses H2 and needs no external services. CI runs the suite on every push.

## Limitations

No refresh tokens, notifications, attachments, or board view yet. The frontend has no automated tests, and database tests run on H2 rather than Testcontainers.

## License

MIT. See [LICENSE](LICENSE).
