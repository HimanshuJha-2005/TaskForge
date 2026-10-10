# TaskForge — Team Issue & Project Management Platform

Spring Boot + PostgreSQL backend, React frontend. Projects → members → issues
(full lifecycle) → comments, with a Streams-powered project dashboard.

> Honesty note: no benchmarks, coverage %, or deployment claims — none measured.

## Features

- Auth: register/login/me, JWT, roles ADMIN / PROJECT_MANAGER / DEVELOPER, BCrypt.
- Projects: PM/ADMIN create (unique key), update, add members; members see own projects, ADMIN sees all.
- Issues: member-gated CRUD, assign/unassign, priority, search + status/priority/assignee filters, pagination.
- Lifecycle OPEN → IN_PROGRESS → IN_REVIEW → RESOLVED → CLOSED (rework loops allowed); illegal jumps → 422.
- Comments per issue (members only).
- Dashboard: totals + `groupingBy` status/priority/assignee + 5 recent, via Java Streams.
- Frontend: protected routes, project list/create, project page (dashboard, filters, members), new-issue form,
  issue detail (transition buttons, comments), loading/error/empty states, responsive CSS.

## Architecture

```
React (:5174 → proxy :8081) → SecurityConfig (stateless JWT) → Controllers
  → ProjectService (membership gate) / IssueService (@Transactional) → JPA → PostgreSQL
  → GlobalExceptionHandler → ApiError JSON
```

Membership (`project_members`) is the authorization gate: every project-scoped call checks it; `@PreAuthorize`
restricts project creation to PM/ADMIN. No entities in APIs (records); no logic in controllers.

## Database (V1)

`users` → `projects` (unique `project_key`) → `project_members` (unique pair) · `issues` (status/priority
CHECKs, assignee + reporter FKs) → `comments`. Indexes: membership gate, member projects, issue lists,
assignee queues, comment threads — each with its why in `V1__init.sql`.

## API quick reference

| Method | Path | Auth |
|--------|------|------|
| GET `/api/health` | no | liveness |
| POST `/api/auth/register`, `/login` · GET `/me` | no/no/JWT | 201/200; 409 dup; 401 bad creds |
| GET/POST `/api/projects` | JWT / PM-ADMIN | 403 otherwise; 409 dup key |
| GET/PUT `/api/projects/{id}`, POST `/{id}/members`, GET `/{id}/dashboard` | member | 403 non-member |
| GET/POST `/api/issues?projectId=…` | member | search/filter/page |
| GET/PUT `/api/issues/{id}`, PATCH `/{id}/status` | member | 422 illegal transition |
| GET/POST `/api/issues/{id}/comments` | member | 201 comment |

## Decisions & tradeoffs

- Membership-checked reads (not just writes): prevents cross-team snooping; costs one extra EXISTS query per call.
- Strict linear lifecycle with rework loops (no OPEN→CLOSED skip): matches real review culture; fewer surprise states.
- Streams dashboard in app (not SQL): clear at this scale; move to SQL aggregation when measured slow.
- H2 tests / Postgres runtime (fast `mvn test`; Testcontainers planned).

## Security

Env-only secrets; BCrypt; 1h JWT; server-side role + membership checks; no sensitive logs; no stack traces.
Gaps: no rate limiting/lockout; open role registration (local-dev convenience).

## Testing — 6/6 green (`mvn test`, no infra)

Full lifecycle walk (illegal 422 → OPEN…CLOSED → comment → dashboard), developer-create 403,
non-member 403, duplicate key 409, search/filter. No coverage % claimed.

## Run locally

```powershell
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
$env:Path = "C:\Tools\apache-maven-3.9.9\bin;$env:JAVA_HOME\bin;" + $env:Path
cd trinity-portfolio\taskforge
mvn test
$env:APP_JWT_SECRET = "<base64 32+ bytes>"; $env:DB_PASSWORD = "dev-only-password"
docker compose up --build        # Postgres (:5433) + API (:8080 in container → :8081 host)
cd frontend; npm install; npm run dev   # UI on :5174
```

## Deployment (documented, NOT deployed)

Stateless container + managed Postgres. Not deployed — no claims.

## Limitations → next

No refresh tokens · no notifications · no issue attachments · no sprint/board view (list + filters only) ·
Postman collection pending · Testcontainers planned.

## Interview mapping (selected)

| Q | A |
|---|---|
| How is cross-team access blocked? | `ProjectService.requireMember` + `uq_project_member`; every issue/comment path calls it |
| Why 422 for transitions? | Valid input, illegal current-state move — `IssueTransitionException` |
| Streams usage? | `IssueService.dashboard()` (`groupingBy` ×3, sorted limit) |
| N+1? | Paged JPQL search fetches one page; dashboard loads one project's issues in one query |
| Why membership table vs role-per-project? | Global roles keep the model small; per-project roles planned if needed |
