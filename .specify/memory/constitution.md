# {{PROJECT_NAME}} — Constitution

> **Status:** Immutable governance. This file is the highest authority for all
> architecture and implementation decisions. Agents and humans read it **before**
> writing any spec, plan, or code. When this document conflicts with a request,
> this document wins — escalate the conflict, do not silently override it.
>
> **How to use:** Place at `.specify/memory/constitution.md`. It is injected into
> every `/specify` and `/plan` template. Pair it with `AGENTS.md` (how agents
> apply these rules) and `scaling-roadmap.md` (when to scale).

Keywords **MUST**, **MUST NOT**, **SHOULD**, **MAY** follow RFC 2119.

---

## Preamble — purpose

The system is built once as a clean, modular monolith and is designed so it can
scale toward very high concurrency **without rewrites**. Every feature is added
in a way that preserves this property. The goal is not to be scalable on day one;
the goal is to never make a decision that *blocks* scaling later.

---

## Article I — Architecture (the dependency rule)

1. The system **MUST** follow Clean / Hexagonal architecture. Dependencies point
   **inward only**: `adapter → application → domain`. Nothing inner may import
   anything outer.
2. The **domain layer MUST be framework-free** — no Spring, JPA, HTTP, Jackson,
   or SQL imports. Pure language + business rules only.
3. Code **MUST** be organized **by feature / bounded context**, not by technical
   layer-first folders. Each context owns its `domain`, `application`,
   `adapter/in`, `adapter/out`.
4. A bounded context **MUST NOT** reach into another context's internals. Cross-
   context communication happens only through published interfaces (ports),
   application services, or events — never shared tables or direct repository calls.
5. The system **MUST** ship as a **single deployable (modular monolith)** until a
   scaling trigger in `scaling-roadmap.md` is met. Microservices **MUST NOT** be
   introduced preemptively (see Article XI).

## Article II — Technology stack (pinned)

Agents **MUST** use this stack and **MUST NOT** substitute frameworks without an
amendment.

| Concern | Technology | Pinned baseline |
|---|---|---|
| Mobile | Flutter / Dart | Flutter 3.44, Dart 3.12 |
| Backend | Java + Spring Boot | Spring Boot 4.0.x, Spring Framework 7, Spring Security 7 |
| Runtime | JDK (LTS only) | Java 25 LTS (min Java 17) |
| Web | Angular + TailwindCSS | Angular 21 (LTS → May 2027), Tailwind v4 |
| Database | PostgreSQL | 17.x |
| Identity | Keycloak (OIDC/OAuth2) | latest stable |
| Cache / session | Redis | 7.x |
| Async / events | Apache Kafka | latest stable (introduced per roadmap) |

- Production **MUST** run on an LTS JDK. Non-LTS Java versions **MUST NOT** be used.
- Versions **MUST** be reviewed quarterly; upgrades are amendments, not silent bumps.

## Article III — Statelessness & 12-factor (scale-readiness)

1. Application instances **MUST** be stateless. No user/session state in process
   memory; sessions and shared state live in Redis.
2. Config **MUST** come from the environment, never hardcoded or committed.
3. Any instance **MUST** be safe to kill and replace at any time. This property is
   what makes horizontal scaling free — it **MUST NOT** be violated for convenience.
4. Backing services (DB, cache, broker) **MUST** be attached via config, treated as
   replaceable resources.

## Article IV — Data

1. PostgreSQL is the default datastore. Each bounded context owns its tables;
   cross-context joins **MUST NOT** be used (compose at the application layer).
2. Schema changes **MUST** go through versioned migrations (Flyway or Liquibase),
   committed with the feature. Manual production schema edits **MUST NOT** happen.
3. All reads **SHOULD** be written so they can later be routed to a read replica
   (no implicit read-after-write assumptions beyond the primary path).
4. Database access **MUST** go through a connection pool. Connection limits are a
   first-class scaling constraint, not an afterthought.

## Article V — Security & authentication

1. AuthN/AuthZ **MUST** use OAuth2 / OIDC via the identity provider (Keycloak).
   Custom auth schemes **MUST NOT** be invented.
2. The **web app MUST use the BFF pattern**: tokens are held server-side; the
   browser receives only an `httpOnly`, `Secure`, `SameSite` session cookie.
   Access/refresh tokens **MUST NOT** be stored in `localStorage` or `sessionStorage`.
3. Mobile **MUST** use Auth Code + PKCE; refresh tokens stored only in platform
   secure storage (Keychain / Keystore).
4. The backend **MUST** act as an OAuth2 resource server validating JWTs, with
   authorization enforced at the use-case boundary.
5. Service-to-service traffic **MUST** use mTLS with a clear CA → intermediate →
   leaf trust chain. Secrets **MUST** live in a vault/secret manager — never in
   source, config files, or images.
6. Every feature **MUST** be checked against **OWASP ASVS**. Input is validated at
   the adapter boundary; output is encoded; rate limiting and a WAF sit at the gateway.

## Article VI — API & contracts

1. Public/external APIs **MUST** be versioned from day one.
2. Inbound and outbound DTOs **MUST** be distinct from domain entities — entities
   **MUST NOT** be serialized directly over the wire.
3. Breaking changes **MUST** be additive-then-deprecate, never silent.

## Article VII — Testing (test-first, enforced)

1. Behavior **MUST** be specified by tests before or alongside implementation.
2. The **dependency rule MUST be enforced automatically** (ArchUnit for the
   backend) so architectural drift fails the build, not code review.
3. Domain logic **MUST** have unit tests with no framework/IO. Adapters get
   integration tests (Testcontainers for DB/broker). A thin end-to-end layer
   covers critical journeys.
4. CI **MUST** gate merges on: tests passing, architecture tests passing,
   dependency/container vulnerability scan, and lint/format.

## Article VIII — Observability & operations

1. Every service **MUST** expose health/readiness/liveness endpoints and metrics.
2. Logs **MUST** be structured (JSON) and carry a correlation/trace ID.
3. Requests **MUST** be traceable end-to-end (OpenTelemetry). You **MUST NOT** ship
   a feature whose failures are invisible in production.

## Article IX — Spec-driven workflow (the loop agents MUST follow)

Every feature **MUST** pass through, in order, with each gate respected:

1. **`/specify`** — the *what & why*. User scenarios, requirements, success
   criteria. **MUST NOT** contain implementation/tech detail.
2. **`/plan`** — the *how*. The plan **MUST** be checked against this constitution;
   any violation is resolved or escalated **before** coding.
3. **`/tasks`** — decompose into small, testable units mapped to bounded contexts.
4. **`/implement`** — execute tasks test-first, honoring all articles.

Code **MUST NOT** be written before an approved spec and plan exist. "Vibe coding"
straight to implementation is prohibited.

## Article X — Feature development procedure (per feature)

For each feature the agent **MUST**:

1. Identify the **bounded context** it belongs to (or justify a new one).
2. Model the **domain** first (entities, value objects, rules) — framework-free.
3. Define **application use cases** and the **ports** they need.
4. Implement **adapters** (in: REST/DTO; out: persistence/messaging/clients) behind
   those ports.
5. Add **migrations**, **tests** (unit + integration), and **observability**.
6. Run the **Definition of Done** checklist before declaring complete.

## Article XI — Anti-patterns the agent MUST refuse

The agent **MUST NOT** do the following, and **MUST** flag the request if asked to:

- Introduce microservices, sharding, or a message broker **before** the relevant
  scaling trigger is met (premature scaling).
- Put business logic in controllers, repositories, or framework classes.
- Let one bounded context import another's internals or share its tables.
- Store JWTs/tokens in browser `localStorage`/`sessionStorage`.
- Add framework imports to the domain layer.
- Hold session state in instance memory.
- Commit secrets, skip migrations, or edit production schema by hand.
- Skip the spec/plan gates and jump to implementation.

When a requested feature cannot be built without violating an article, the agent
**MUST** stop, explain which article is at stake, and propose a compliant
alternative rather than quietly breaking the architecture.

## Article XII — Definition of Done

A feature is done only when **all** are true:

- [ ] Spec, plan, and tasks exist and were followed.
- [ ] Code respects the dependency rule (ArchUnit green).
- [ ] Domain layer is framework-free.
- [ ] Lives in the correct bounded context; no cross-context coupling.
- [ ] Unit + integration tests pass; critical path has an e2e test.
- [ ] Migrations included and reversible.
- [ ] Security: authZ enforced, inputs validated, no secrets, ASVS-checked.
- [ ] App tier still stateless; config externalized.
- [ ] Health, metrics, structured logs, and tracing present.
- [ ] API versioned; DTOs separate from entities.
- [ ] Vulnerability scan and lint pass in CI.

## Article XIII — Amendments

This constitution changes only by explicit amendment: a dated entry below stating
what changed and why. Agents **MUST NOT** treat a one-off instruction in a chat as
an amendment.

| Date | Version | Change | Reason |
|---|---|---|---|
| {{DATE}} | 1.0.0 | Initial ratification | Establish reusable architecture baseline |
