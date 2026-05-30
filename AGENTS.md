# AGENTS.md — {{PROJECT_NAME}}

> Operating instructions for AI coding agents. The **constitution** (`constitution.md`)
> is the law; this file is **how you apply it**. Read both before acting. If they
> conflict, the constitution wins.

## 0. Prime directive

You maintain a clean, modular monolith that is **architected to scale later without
a rewrite**. Your job on every task is to add value *without eroding that property*.
A correct-but-architecture-breaking change is a **failed** change. When in doubt,
preserve the architecture and ask.

## 1. Operating loop (never skip a phase)

For any non-trivial change you **MUST** move through the spec-driven loop:

1. **Specify** — restate the *what & why* as user scenarios + acceptance criteria.
   No tech detail. If the request is ambiguous, ask up front, once.
2. **Plan** — choose the bounded context and the layers you'll touch. Run the
   **Architecture Gate** (§3). Resolve or escalate every violation before coding.
3. **Tasks** — break into small, independently testable units. Each task names the
   layer and context it lives in.
4. **Implement** — test-first, inside-out: domain → application → adapters.
5. **Verify** — run the Definition of Done (constitution Art. XII). Report which
   boxes are green.

Trivial changes (typo, copy, config value) may shortcut to step 4, but still obey
all architecture and security rules.

## 2. Build order for a feature (inside-out)

Always build from the center outward so the framework never leaks inward:

1. **Domain** — entities, value objects, domain services. Framework-free. Unit-tested.
2. **Application** — use-case services + **port interfaces** they depend on.
3. **Adapter (in)** — REST controller + request/response DTOs. Maps DTO ↔ domain.
4. **Adapter (out)** — persistence (JPA repo implementing the port), messaging,
   external clients. Integration-tested with Testcontainers.
5. **Cross-cutting** — migration, authZ check, metrics, structured logs, tracing.

Per-stack placement:

- **Spring Boot:** `com.{{org}}.{{context}}.{domain|application|adapter.in|adapter.out}`.
  Domain has zero Spring/JPA imports. Ports are interfaces in `application`.
- **Flutter:** feature-first folders, each with `presentation/`, `domain/`,
  `data/`. State via Riverpod or BLoC; API via Dio in `data/`. UI never calls the
  network directly — it goes through a repository interface.
- **Angular:** standalone components + signals. `core/` (interceptors, guards, the
  BFF/auth service), `shared/` (Tailwind UI primitives), `features/` (lazy-loaded
  per context). Components are smart/presentational-split; HTTP lives in services.

## 3. Architecture Gate (run during /plan — block on any "no")

- [ ] Does every new dependency point **inward** only?
- [ ] Is the domain layer still **framework-free**?
- [ ] Does this live in **one** bounded context, with no reach into another's internals?
- [ ] Are we still a **single deployable** (no new service/broker/shard unless a
      `scaling-roadmap.md` trigger is met)?
- [ ] Is the app tier still **stateless**? No new in-memory session state?
- [ ] Is there a **migration** for any schema change?
- [ ] Is auth via **OIDC**, web tokens via **BFF** (never browser storage)?
- [ ] Are DTOs **separate** from entities, and is the API **versioned**?

Any unchecked box ⇒ stop, fix the design, or escalate with the specific article cited.

## 4. Roles (use as sub-agents or as hats one agent wears)

- **Spec Author** — turns a request into a clear, tech-free spec with acceptance
  criteria. Asks clarifying questions; does not propose implementation.
- **Architect / Planner** — selects bounded context + layers, runs the Architecture
  Gate, produces the `/plan`. Owns "does this preserve scalability?" Has authority
  to reject a plan.
- **Implementer** — writes code test-first in build order, honoring all articles.
- **Security Reviewer** — checks the feature against OWASP ASVS, authZ at the
  boundary, secret handling, mTLS for service calls, input validation.
- **Verifier** — runs the Definition of Done and CI gates; reports status honestly,
  including failures.

A single agent may play all roles sequentially, but **MUST NOT** let the Implementer
override the Architect or Security Reviewer.

## 5. Scaling discipline

- Default posture: **do not scale anything preemptively.** The MVP is a stateless
  modular monolith + Postgres + Keycloak. That is correct, not incomplete.
- Only advance to the next architecture stage when its **trigger metric** in
  `scaling-roadmap.md` is actually observed in production.
- Because Articles I and III are honored on every feature, each scaling step
  (caching → replicas → async → service extraction → sharding) is **additive**, not
  a rewrite. If a scaling step would require a rewrite, an earlier feature violated
  the constitution — find and fix that, don't paper over it.

## 6. How to refuse / escalate

If a request can't be satisfied without breaking the constitution:

1. Name the exact article at risk.
2. Explain the future cost (what it blocks at scale).
3. Offer a compliant alternative that achieves the user's goal.
4. Proceed only on explicit confirmation, and record it as an amendment if it
   genuinely changes a rule.

Never silently break architecture to satisfy a request faster.

## 7. Communication

- Report what you did per layer/context, which DoD boxes are green, and any
  deferred items with reasons.
- Surface assumptions explicitly. One round of clarifying questions up front beats
  a wrong implementation.
