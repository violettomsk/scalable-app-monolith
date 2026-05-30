# Agent Operating Principles

> Guidelines for any AI agent (Claude, GPT, Gemini, or other) to operate effectively within this project

This document describes the principles, values, and operating patterns that guide intelligent work on this codebase. It is independent of any specific AI tool or model—it captures the *way of thinking* that produces reliable, scalable, maintainable code.

---

## Core Values

### 1. Architecture is Governance, Not Suggestion

- **Rules are enforced in code**, not handwaves. ArchUnit fails the build; migrations are versioned; tests gate merges.
- When a rule is violated, **the build fails**, not the code review. This prevents drift.
- **Refuse to break rules for speed.** A fast violation creates technical debt that compounds. A slow correct approach scales.

### 2. Understand Before Building

- **Read the constitution first.** Every request is evaluated against `constitution.md`, `AGENTS.md`, and `scaling-roadmap.md`.
- If a request conflicts with governance, **name the exact article at risk** and propose a compliant alternative.
- **Ask clarifying questions once, upfront.** One round of questions beats a wrong implementation.
- **Never silently override architecture to satisfy a request faster.**

### 3. Test-First, Inside-Out

- **Behavior is specified by tests before or alongside implementation.** Domain logic has unit tests with no framework. Adapters get integration tests.
- **Build from the center outward:** domain → application (ports) → adapters → tests.
- **The dependency rule is non-negotiable:** domain imports nothing outer; application imports only ports; adapters implement ports.

### 4. Scale Lazily, Never Preemptively

- The MVP is correct: stateless modular monolith + PostgreSQL + Keycloak.
- **Scale only when a trigger in `scaling-roadmap.md` is actually observed in production.**
- Each scaling step (caching → replicas → async → service extraction) is additive, not a rewrite.
- If a scaling step would require a rewrite, an earlier feature violated the constitution—find and fix that, don't paper over it.

### 5. Observability is Non-Negotiable

- **Every feature must be traceable end-to-end.** Structured logs, trace IDs, health endpoints, metrics.
- **You MUST NOT ship a feature whose failures are invisible in production.**
- Configuration externalizes; state lives in Redis; instances are replaceable.

---

## Operating Loop (Never Skip)

For any non-trivial change, move through these gates in order:

### 1. **Specify** — The *what & why*

- User scenarios and acceptance criteria.
- **NO tech detail.** ("Add login" not "implement JWT with refresh tokens")
- **Resolve ambiguity once:** ask clarifying questions before proceeding.
- Output: Clear spec document or conversation summary.

### 2. **Plan** — The *how*

- Choose the bounded context (existing or justify a new one).
- Name the layers touched (domain, application, adapters).
- **Run the Architecture Gate** (see below).
- Resolve every violation before proceeding.
- Output: Implementation plan checked against governance.

### 3. **Tasks** — Decompose into units

- Small, independently testable units.
- Each task names its layer and context.
- 5–6 tasks per person/agent keeps productivity high without context thrashing.
- Output: Task list with clear acceptance criteria.

### 4. **Implement** — Build test-first

- Domain logic first (framework-free).
- Application layer (ports and use cases).
- Adapters (controllers, repositories, clients).
- Migrations, tests, observability.
- Output: Code, tests, migrations following build order.

### 5. **Verify** — Definition of Done

Run the checklist (see constitution.md Art. XII):
- [ ] Spec, plan, and tasks exist and were followed
- [ ] ArchUnit passes (dependency rule enforced)
- [ ] Domain layer is framework-free
- [ ] Lives in correct bounded context; no cross-context coupling
- [ ] Unit + integration tests; critical path has e2e test
- [ ] Migrations included and reversible
- [ ] Security: authZ enforced, inputs validated, no secrets
- [ ] App tier stateless; config externalized
- [ ] Health, metrics, structured logs, trace IDs present
- [ ] API versioned; DTOs separate from entities
- [ ] Vulnerability scan and lint pass

---

## Architecture Gate (Block on Any "No")

Answer these before coding:

1. **Dependency rule:** Does every new dependency point inward only?
2. **Domain purity:** Is the domain layer still framework-free?
3. **Bounded context:** Does this live in ONE context with no reach into another's internals?
4. **Single deployable:** Are we still a single modular monolith? (No new service/broker unless a scaling trigger is met.)
5. **Statelessness:** Is the app tier still stateless? No new in-memory session state?
6. **Migrations:** Is there a migration for any schema change?
7. **Auth:** Via OIDC? Web tokens in BFF (never browser storage)?
8. **API contracts:** DTOs separate from entities? API versioned?

**Any unchecked box = stop, fix the design, or escalate with the specific article cited.**

---

## How to Refuse / Escalate

When a request cannot be satisfied without breaking the constitution:

1. **Name the exact article at risk.**
   - Bad: "This violates the architecture."
   - Good: "This requires shared tables across contexts, which violates Article I § 4 (cross-context integrity) and Article IV (data isolation)."

2. **Explain the future cost.**
   - Bad: "It's against the rules."
   - Good: "Shared tables block read replicas (Article IV § 3) and microservice extraction (scaling-roadmap). Fixing this later requires a migration affecting two services."

3. **Offer a compliant alternative.**
   - "Instead of sharing a table, publish an event from Context A; Context B consumes it and maintains its own view. This preserves isolation and enables both replicas and later extraction."

4. **Proceed only on explicit confirmation**, and record it as an amendment if it genuinely changes a rule.

---

## Key Patterns (Do's and Don'ts)

### ✅ DO

- **Read the constitution before coding.** Every request is checked against it.
- **Ask clarifying questions upfront, once.** "Is this a public API?" "Does this need real-time sync?"
- **Build inside-out:** domain (unit tests) → application (ports) → adapters (integration tests) → cross-cutting (observability).
- **Write migrations with features.** Schema changes are versioned and reversible.
- **Make architecture violations fail the build.** Use ArchUnit, lint, vulnerability scans as gates.
- **Stateless instances.** Kill and replace any instance without losing state. Sessions in Redis.
- **Test-first.** Write tests before or alongside implementation.
- **Refuse violations gracefully.** Name the article, explain the cost, offer a compliant alternative.
- **Surface assumptions.** "I'm assuming this is a read-heavy query; if it's mixed, we'll need a different strategy."
- **Report what you did per layer/context.** "Domain logic in catalog context: 3 entities + 2 value objects + 5 unit tests. Application layer: 2 use cases + 1 port. Adapters: 1 REST controller + 1 JPA repository."

### ❌ DON'T

- **Skip the spec/plan gates.** "Vibe coding" straight to implementation is prohibited.
- **Let domains import frameworks.** Spring, JPA, HTTP, SQL have no place in domain logic.
- **Share tables across contexts.** Compose at the application layer.
- **Hold state in process memory.** Use Redis for sessions.
- **Commit secrets.** Environment variables, vault, never source.
- **Hardcode config.** Everything comes from environment.
- **Skip migrations.** Schema changes must be versioned and reversible.
- **Store JWTs in localStorage/sessionStorage.** Browser holds only an httpOnly cookie; tokens live server-side.
- **Introduce microservices before a scaling trigger is met.** The MVP is a modular monolith.
- **Silently break architecture to satisfy a request faster.** Escalate, don't override.

---

## Communication Style

### When Reporting Work

```
✅ Good:
  Backend domain layer: 3 entities (Product, Inventory, Reservation), 2 domain services (ReservationPolicy, InventoryAllocator), 12 unit tests—all framework-free.
  
  Application layer: 2 use cases (ReserveProduct, CancelReservation), 2 port interfaces (InventoryPort, NotificationPort).
  
  Adapters (in): REST controller (POST /products/{id}/reserve), DTO (ReserveRequest, ReserveResponse).
  
  Adapters (out): JPA repository implementing InventoryPort (Testcontainers integration test), SMTP adapter implementing NotificationPort (mocked in unit tests).
  
  Migrations: V3__create_reservation_table.sql (reversible), V4__add_status_enum.sql.
  
  Observability: structured logs with trace_id, health endpoint, metrics (reservations_total, reservation_duration).
  
  DoD: ✅ all checks green.

❌ Bad:
  "Finished the feature. Tests pass. Ready to merge."
```

### When Blocking

```
✅ Good:
  This feature requires sharing the product table across catalog and reservation contexts. This violates Article I § 4 (cross-context integrity) and Article IV (data isolation per context). This blocks read replicas (Article IV § 3) and microservice extraction (scaling-roadmap).
  
  Compliant alternative: Reservation context owns its own product view. On CreateProduct event from catalog, reservation context writes a ProductSnapshot to its own table. ReservationQuery joins against local snapshot, not the catalog table.

❌ Bad:
  "Can't do this, it violates the architecture. Let's just share the table and move on."
```

---

## Stack-Specific Patterns

### Spring Boot (Backend)

- Package by bounded context: `com.example.catalog.{domain,application,adapter.in,adapter.out}`
- Domain: no Spring imports. Just entities, value objects, domain services.
- Application: services with `@UseCase`, port interfaces (no `@Repository` in domain).
- Adapters (in): `@RestController`, DTOs, `@Service` orchestrators.
- Adapters (out): `@Repository` implementing ports; JPA entities separate from domain.
- Migrations: Flyway, `V{version}__{description}.sql`, reversible.
- Tests: JUnit 5, Testcontainers for DB/broker, ArchUnit for dependency rule.

### Angular (Frontend)

- Structure: `core/` (singletons: auth, interceptors), `shared/` (Tailwind UI primitives), `features/` (lazy-loaded per context).
- Auth: BFF pattern. Browser holds `httpOnly` session cookie only. Tokens never in `localStorage`.
- HTTP: Services in `data/` layer; components call through services.
- State: Signals or Riverpod (testable, no magic).
- Components: smart/presentational split; UI never calls network directly.

### Flutter (Mobile)

- Structure: `features/{context}/{presentation,domain,data}`.
- Auth: Auth Code + PKCE; tokens in platform secure storage (Keychain/Keystore).
- State: Riverpod or BLoC; providers are interfaces.
- Network: Dio in `data/` layer; UI calls through repositories.
- Domain: business logic, no framework.

---

## Red Flags (Stop and Escalate)

If you encounter any of these, **stop and ask for clarification:**

1. **Shared tables across contexts.** → Violates Article I § 4, Article IV. Offer event-based alternative.
2. **Framework imports in domain.** → Violates Article I § 2. Refactor immediately.
3. **In-process session state.** → Violates Article III § 1. Use Redis.
4. **Request to skip migrations.** → Violates Article IV § 2. Migrations are mandatory.
5. **Request to store JWTs in localStorage.** → Violates Article V § 2. Use BFF + httpOnly cookie.
6. **"Let's just add a microservice."** → Violates Article I § 5 unless a scaling trigger is met. Check `scaling-roadmap.md`.
7. **"Let's hardcode this config value."** → Violates Article III § 2. Use environment variables.
8. **"Ship a feature without tests."** → Violates Article VII § 1. No feature is done without tests.
9. **"No time for observability."** → Violates Article VIII. You MUST NOT ship a feature whose failures are invisible in production.

---

## Working with This Project

### As a Human

1. **Read `constitution.md` and `AGENTS.md` first.** They are the law.
2. **Use `/specify`, `/plan`, `/tasks` from Spec-Kit** to move through gates.
3. **Run the Architecture Gate** during planning. Block on violations.
4. **Use ArchUnit, tests, and CI as enforcers**, not code review.
5. **Refuse to skip phases** even when pressured for speed.

### As an AI Agent (Claude or Other)

1. **Load and internalize `constitution.md`, `AGENTS.md`, `scaling-roadmap.md`, and `CLAUDE.md`.**
2. **Evaluate every request against these documents.**
3. **Move through the spec/plan/tasks/implement/verify loop** for non-trivial changes.
4. **Run the Architecture Gate** before coding.
5. **Report work per layer/context** with DoD verification.
6. **Refuse violations gracefully:** name the article, explain the cost, propose a compliant alternative.
7. **Ask clarifying questions once, upfront.**
8. **Never silently override architecture** to satisfy a request faster.

---

## Scalability Mindset

- **Default posture:** Do not scale anything preemptively.
- **MVP correctness:** Stateless modular monolith + Postgres + Keycloak is correct, not incomplete.
- **Scaling trigger:** Check `scaling-roadmap.md`. Only advance when a metric is actually observed.
- **Additive, not rewrite:** Each scaling step (caching, replicas, async, extraction) is additive because Articles I and III are honored on every feature.
- **If a scaling step would require a rewrite:** An earlier feature violated the constitution. Find and fix that.

---

## Summary

This project succeeds because:

1. **Rules are in code**, not handwaves.
2. **Architecture is preserved on every feature.**
3. **Work moves through gates:** spec → plan → tasks → implement → verify.
4. **Test-first, inside-out build order.**
5. **Stateless, horizontally scalable from day one.**
6. **Violations are refused gracefully**, not silently accepted.

Whether you are Claude, ChatGPT, Gemini, or a human developer, **follow these principles**. Your output will be reliable, scalable, and maintainable.

---

## Questions and Clarifications

If an instruction conflicts with these principles:
1. Name the exact principle at stake.
2. Explain why the conflict matters (future cost, scaling risk, security issue).
3. Propose a compliant alternative.
4. **Proceed only on explicit confirmation.**

This protects the codebase and keeps all work aligned with the constitution.
