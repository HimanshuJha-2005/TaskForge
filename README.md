# TaskForge

![backend](https://github.com/HimanshuJha-2005/TaskForge/actions/workflows/backend.yml/badge.svg)

Team issue and project management platform — **Spring Boot + PostgreSQL + React** — with
membership-gated projects, an enforced issue lifecycle, comments, and a Streams-powered
project dashboard.

## Screenshots

| Projects | Project dashboard | Issue detail |
|---|---|---|
| ![Projects](docs/screenshots/projects.png) | ![Dashboard](docs/screenshots/dashboard.png) | ![Issue](docs/screenshots/issue.png) |

## Features

- **Auth** — registration, login, JWT, BCrypt hashing; roles `ADMIN` / `PROJECT_MANAGER` / `DEVELOPER`
- **Projects** — PM/admin creation with unique keys, member management; members see their projects, admins see all
- **Membership gate** — every project-scoped request checks membership; non-members get 403
- **Issues** — creation, assignment, priorities, full-text search with status/priority/assignee filters and pagination
- **Issue lifecycle** — `OPEN → IN_PROGRESS → IN_REVIEW → RESOLVED → CLOSED` (with rework loops); illegal jumps rejected with 422
- **Comments** — per-issue discussion threads for members
- **Dashboard** — per-project totals, breakdowns by status/priority/assignee and recent issues, aggregated with the Streams API
- **Web app (React)** — projects, project page with dashboard and filters, issue detail with one-click transitions and comments; protected routes, loading/error/empty states, responsive layout

## Tech stack

| Layer | Technologies |
|---|---|
| Backend | Java 21, Spring Boot 3.2 (Web, Security, Data JPA, Validation), JJWT |
| Database | PostgreSQL 16, Flyway migrations, H2 for tests |
| Frontend | React 18, React Router, Vite |
| Build & infra | Maven, Docker Compose, GitHub Actions |

## Quick start

```bash
# Backend tests (no infrastructure needed)
mvn test

# Full stack with Postgres
export APP_JWT_SECRET=$(python3 -c "import secrets,base64;print(base64.b64encode(secrets.token_bytes(32)).decode())")
export DB_PASSWORD=dev-password
docker compose up --build        # API on :8081

# Frontend
cd frontend && npm install && npm run dev   # UI on :5174
```

## API reference

| Method | Endpoint | Auth | Notes |
|---|---|---|---|
| POST | `/api/auth/register`, `/api/auth/login` | — | Returns JWT; 409 on duplicate email |
| GET | `/api/projects` | JWT | Own projects (all for admins) |
| POST | `/api/projects` | PM/Admin | 409 on duplicate key |
| GET / PUT | `/api/projects/{id}` | Member | Project detail and update |
| POST | `/api/projects/{id}/members` | PM/Admin | Add member by email |
| GET | `/api/projects/{id}/dashboard` | Member | Aggregated stats + recent issues |
| GET / POST | `/api/issues?projectId=…` | Member | Search, filters, pagination |
| GET / PUT | `/api/issues/{id}` | Member | Detail and update |
| PATCH | `/api/issues/{id}/status` | Member | 422 on illegal transition |
| GET / POST | `/api/issues/{id}/comments` | Member | Comment thread |

All errors return a uniform JSON body: `{timestamp, status, error, message, path}`.

## Architecture

```
React (Vite, :5174) ──JSON + Bearer JWT──▶ Spring Boot (:8081) ──▶ PostgreSQL (:5433 host)
                                             │  Controller → Service (@Transactional) → Repository
                                             │  ProjectService.requireMember gates every scoped call
                                             └── GlobalExceptionHandler → uniform error JSON
```

## Project structure

```
src/main/java/com/taskforge/
├── auth/        # users, registration, login, JWT
├── project/     # projects, memberships
├── issue/       # issues, lifecycle, comments
├── common/      # global exception handling
├── config/      # security configuration
└── health/      # liveness probe
src/main/resources/db/migration/   # versioned schema
frontend/src/    # pages, auth context, API client
```

## Testing

6 integration tests, run in CI on every push: full issue lifecycle (illegal transition → resolution →
comment → dashboard), role and membership authorization, duplicate keys, search and filters.
`mvn test` needs no external services.

## Limitations & roadmap

Not yet covered: refresh tokens, notifications, file attachments, board view, Testcontainers-based
DB tests. Planned next.
