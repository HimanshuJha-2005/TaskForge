-- TaskForge V1: users, projects, memberships, issues, comments.
-- Membership is the authorization gate: every project-scoped read checks it.

CREATE TABLE users (
  id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  email         VARCHAR(120) NOT NULL UNIQUE,
  password_hash TEXT         NOT NULL,
  role          VARCHAR(20)  NOT NULL,
  created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
  CONSTRAINT chk_users_role CHECK (role IN ('ADMIN', 'PROJECT_MANAGER', 'DEVELOPER'))
);

CREATE TABLE projects (
  id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  name        VARCHAR(120) NOT NULL,
  project_key VARCHAR(12)  NOT NULL UNIQUE,
  description VARCHAR(2000),
  owner_id    BIGINT NOT NULL REFERENCES users (id),
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE project_members (
  id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  project_id BIGINT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
  user_id    BIGINT NOT NULL REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT uq_project_member UNIQUE (project_id, user_id)
);

CREATE TABLE issues (
  id          BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  project_id  BIGINT NOT NULL REFERENCES projects (id) ON DELETE CASCADE,
  title       VARCHAR(200) NOT NULL,
  description VARCHAR(4000),
  status      VARCHAR(20) NOT NULL DEFAULT 'OPEN',
  priority    VARCHAR(20) NOT NULL,
  assignee_id BIGINT REFERENCES users (id),
  reporter_id BIGINT NOT NULL REFERENCES users (id),
  created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT chk_issues_status CHECK (status IN ('OPEN','IN_PROGRESS','IN_REVIEW','RESOLVED','CLOSED')),
  CONSTRAINT chk_issues_priority CHECK (priority IN ('LOW','MEDIUM','HIGH','CRITICAL'))
);

CREATE TABLE comments (
  id         BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  issue_id   BIGINT NOT NULL REFERENCES issues (id) ON DELETE CASCADE,
  author_id  BIGINT NOT NULL REFERENCES users (id),
  body       VARCHAR(2000) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- Read-path indexes (and why):
-- 1. Membership gate on every project-scoped request.
CREATE INDEX idx_members_project_user ON project_members (project_id, user_id);
CREATE INDEX idx_members_user ON project_members (user_id);
-- 2. Issue lists filtered by project (+ status/priority in memory after page fetch).
CREATE INDEX idx_issues_project ON issues (project_id);
-- 3. Assignee queues: WHERE assignee_id = ?.
CREATE INDEX idx_issues_assignee ON issues (assignee_id);
-- 4. Comment threads per issue.
CREATE INDEX idx_comments_issue ON comments (issue_id);
