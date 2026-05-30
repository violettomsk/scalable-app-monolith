# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Quick Reference

**Governance documents (mandatory reading):**
- `.specify/memory/constitution.md` — immutable law; all architecture decisions derive from it
- `AGENTS.md` — how to apply the constitution; the operating loop
- `docs/scaling-roadmap.md` — when to scale (never preemptively)
- `gitflow.md` — branching and commit convention

**Architecture:** Clean/Hexagonal, organized by bounded context. Single deployable (modular monolith) with stateless app tiers.

---

## Dev Environment Setup

**Prerequisites:** Docker, Java 25+ (LTS), Node 20+, Flutter 3.44+, Docker Compose

```bash
# Start backing services (Postgres, Redis, Keycloak)
docker compose up -d

# Backend shell
cd backend && ./gradlew bootRun  # http://localhost:8080

# Web shell
cd web && npm install && npm start  # http://localhost:4200

# Mobile
cd mobile && flutter run
```

---

## Backend (Spring Boot 4.0)

**Language:** Java 25 LTS (Spring Boot 4.0, Spring Framework 7, Spring Security 7)

**Key commands:**
```bash
cd backend

# Build & run
./gradlew bootRun

# Tests (unit + integration)
./gradlew test

# Architecture test (ArchUnit — fails build if dependency rule broken)
./gradlew test -p :architecture

# Single test file or method
./gradlew test --tests MyTest
./gradlew test --tests com.example.catalog.application.*

# Lint & format
./gradlew build

# Health check
curl http://localhost:8080/actuator/health
curl http://localhost:8080/actuator/metrics
```

**Project layout by bounded context:**
```
backend/src/main/java/com/example/{{context}}/
├── domain/          # Entities, value objects, domain rules (framework-free)
├── application/     # Use cases, services, port interfaces
├── adapter/
│   ├── in/         # REST controller, request/response DTOs
│   └── out/        # Persistence (JPA), messaging, external clients
└── [migrations, configs]
```

**Build order (inside-out):** domain → application (ports) → adapters → tests

**Architecture enforcement:** `backend/src/test/java/com/example/architecture/CleanArchitectureTest.java` (ArchUnit). This test **fails the build** if:
- A domain class imports a framework or external dependency
- A dependency points outward (not toward domain)
- Cross-context coupling is detected

**Migrations:** Flyway, committed with the feature. Always reversible.

---

## Web (Angular 21 + Tailwind)

**Language:** TypeScript, Angular 21 (LTS until May 2027), Tailwind v4

**Key commands:**
```bash
cd web

# Install & dev server
npm install
npm start           # http://localhost:4200

# Build for production
npm run build

# Run tests
npm test

# Format & lint
# (Automated as pre-commit or CI gate)
```

**Architecture:**
- **`core/`** — singleton services: BFF/auth, interceptors, guards
- **`shared/`** — reusable Tailwind UI components
- **`features/`** — lazy-loaded per bounded context (e.g., `features/catalog/`, `features/auth/`)
- Standalone components + Angular signals; HTTP via service layer

**Auth pattern (BFF):** Browser holds only `httpOnly`, `Secure`, `SameSite` session cookie. Tokens live server-side (in Spring session). **Never store JWTs in localStorage/sessionStorage.**

---

## Mobile (Flutter 3.44 + Dart 3.12)

**Language:** Dart 3.12, Flutter 3.44

**Key commands:**
```bash
cd mobile

# Dependencies
flutter pub get

# Run on emulator/device
flutter run

# Build APK / IPA
flutter build apk
flutter build ios

# Tests
flutter test

# Format & lint
flutter format .
flutter analyze
```

**Architecture:**
- **Feature-first structure:** `lib/features/{{context}}/{presentation, domain, data}`
- **State:** Riverpod (reactive, testable)
- **Network:** Dio in `data/` layer; UI calls through repository interfaces, never directly to network
- **Auth:** Auth Code + PKCE flow; tokens in platform secure storage (Keychain/Keystore)

---

## Database & Migrations

**System:** PostgreSQL 17+, migrations via Flyway

```bash
# Migrations live in backend/src/main/resources/db/migration/
# Follow naming: V{{version}}__{{description}}.sql
# Example: V1__create_product_table.sql

# New schema change → create reversible migration
# Committed with feature; runs automatically on bootRun
```

**Schema rules:**
- Each bounded context owns its tables; no cross-context joins (compose at app layer)
- All reads writeable for read-replica routing (no implicit read-after-write beyond primary)
- Connection pool is a first-class scaling constraint

---

## Session & Caching

**Session store:** Redis (not in-process memory)
**Cache:** Redis
**Stateless principle:** Any app instance must be safe to kill/replace without losing state. No in-memory session or cache.

---

## Architecture Rules (Constitution)

**You MUST follow these — violations fail the build or gate merges:**

1. **Dependency rule:** domain ← application ← adapter. No inner layer imports outer.
2. **Domain layer:** Framework-free (no Spring, JPA, HTTP, Jackson, SQL).
3. **Organization:** By bounded context, not by layer.
4. **Cross-context:** Published interfaces, application services, or events only — never shared tables or direct repo calls.
5. **Deployment:** Single deployable (modular monolith) until a scaling trigger in `docs/scaling-roadmap.md` is met.
6. **Statelessness:** App instances must be replaceable; no in-process state.
7. **Config:** From environment, never hardcoded.
8. **Auth:** OAuth2/OIDC via Keycloak. Web: BFF pattern (server-side tokens). Mobile: Auth Code + PKCE.
9. **API:** Versioned from day one; DTOs separate from entities.
10. **Tests:** Behavior specified first; architecture tests (ArchUnit) block the build.

**See constitution.md for the full law.**

---

## Operating Loop (Never Skip)

For any non-trivial change:

1. **Specify** — user scenarios, acceptance criteria (tech-free)
2. **Plan** — choose bounded context, run Architecture Gate, resolve violations
3. **Tasks** — decompose into small, testable units
4. **Implement** — test-first, inside-out (domain → application → adapters)
5. **Verify** — run Definition of Done checklist

**Trivial changes** (typo, config) may skip to step 4, but still obey all rules.

**See AGENTS.md for detailed operating instructions.**

---

## Git Workflow (Git Flow)

**Branches:**
- `main` — production only; tagged with versions; protected
- `develop` — integration; receives feature merges; protected
- `feature/*` — new features (branch from develop, PR to develop)
- `bugfix/*` — non-critical fixes (branch from develop, PR to develop)
- `release/*` — release prep (branch from develop, merge to main + develop, tagged)
- `hotfix/*` — critical production fixes (branch from main, merge to main + develop, tagged)

**Commit convention:**
```
<type>(<scope>): <subject>

<body (optional)>
```

Types: `feat`, `fix`, `docs`, `style`, `refactor`, `perf`, `test`, `chore`

Example: `feat(auth): add BFF session endpoint`

**See gitflow.md for detailed workflows.**

---

## Definition of Done

A feature is complete only when **all** are true:

- Spec, plan, and tasks exist and were followed
- ArchUnit (architecture test) is green
- Domain layer is framework-free
- Lives in correct bounded context; no cross-context coupling
- Unit + integration tests pass; critical path has e2e test
- Migrations included and reversible
- Auth: OIDC/BFF or Auth Code + PKCE; inputs validated; secrets not committed
- App tier stateless; config externalized
- Health, metrics, structured logs, trace IDs present
- API versioned; DTOs separate from entities
- Vulnerability scan and lint pass in CI

---

## Key Principles

- **Govern through code, not handwakes.** ArchUnit, migrations, tests enforce rules.
- **Scale lazily.** MVP is modular monolith + Postgres + Keycloak. Only scale when `scaling-roadmap.md` trigger is met.
- **Test-first.** Domain logic with no framework; adapters with integration tests (Testcontainers).
- **Refuse violations.** If a request breaks the constitution, name the article, explain the cost, offer a compliant alternative.
- **Ask once.** One round of clarifying questions upfront beats wrong implementation.
