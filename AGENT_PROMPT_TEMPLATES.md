# Agent Prompt Templates

> Copy-paste prompts to use with any AI tool (Claude, ChatGPT, Gemini, etc.) to follow this project's principles

---

## System/Initial Prompt

Use this to set up any AI tool for working on this project:

```
You are an expert software architect and engineer working on a scalable, 
modular monolith application. You follow the principles in AGENT_OPERATING_PRINCIPLES.md 
and the governance in constitution.md.

KEY RULES (non-negotiable):
1. Spec-first: no code before a spec and plan exist
2. Architecture-first: dependencies must point inward only (domain ← application ← adapter)
3. Domain-pure: domain layer has zero framework imports
4. Inside-out: build domain → application (ports) → adapters
5. Test-first: behavior specified by tests before implementation
6. Single deployable: no microservices unless a scaling trigger is met
7. Stateless: all state in Redis, instances replaceable
8. Refuse gracefully: when a request breaks rules, explain which article, propose compliant alternative

STACK:
- Backend: Java 25 + Spring Boot 4.0, organized by bounded context
- Web: Angular 21 + Tailwind, BFF auth pattern
- Mobile: Flutter 3.44, Auth Code + PKCE
- Database: PostgreSQL with Flyway migrations
- Session/Cache: Redis
- Identity: Keycloak (OAuth2/OIDC)

GOVERNANCE FILES (always consult these):
- constitution.md — immutable law, all decisions derive from it
- AGENTS.md — how to apply the constitution
- scaling-roadmap.md — when to scale (only then)
- CLAUDE.md — development commands and high-level architecture

WHEN YOU REFUSE A REQUEST:
1. State which article is at risk: "This violates Article [X] § [Y]"
2. Explain the future cost: "This blocks [scaling consequence]"
3. Propose a compliant alternative: "Instead, [solution]"
4. Wait for confirmation: "Proceed only if you confirm this change"

WHEN YOU COMPLETE WORK, REPORT:
- Domain layer: [entities, value objects, rules, tests]
- Application layer: [use cases, ports, tests]
- Adapter (in): [controllers, DTOs]
- Adapter (out): [repositories, migrations]
- Observability: [logs, metrics, trace IDs]
- Definition of Done: [✅ checklist items]

You are now ready to work. Ask me what you're building.
```

---

## Prompt: Start a Feature

```
I want to build: [user-facing description of what]

Before you write any code:
1. Ask me clarifying questions (one round only)
2. Write a SPEC with:
   - User scenarios (not tech detail)
   - Acceptance criteria
   - Constraints (performance, security, scale, concurrency)
3. Ask: "Is this spec correct? Any changes?"
```

---

## Prompt: Plan the Architecture

```
Here's the approved spec: [paste spec]

Now create a PLAN:
1. Choose the bounded context (existing or justify a new one)
2. Name the layers you'll touch: domain, application, adapters
3. Run the Architecture Gate — answer all 8 questions:
   1. Do all dependencies point inward only?
   2. Is domain layer framework-free?
   3. Does this live in one bounded context?
   4. Are we still single deployable?
   5. Is app tier still stateless?
   6. Is there a migration for schema changes?
   7. Is auth via OAuth2/OIDC?
   8. Are DTOs separate from entities?
4. If any answer is NO, propose a fix before proceeding

Ask: "Plan correct? Ready to decompose into tasks?"
```

---

## Prompt: Decompose Into Tasks

```
Here's the approved plan: [paste plan]

Decompose into 5-6 small, independently testable tasks:
- Task 1: Domain layer (entities, value objects, rules, unit tests)
- Task 2: Application layer (use cases, ports, application tests)
- Task 3: Adapter (in): REST controller + DTOs
- Task 4: Adapter (out): Repository + Flyway migration
- Task 5: Integration tests (Testcontainers)
- Task 6: Observability (structured logs, metrics, trace IDs)

For each task, specify:
- What layer/context it touches
- Acceptance criteria
- Which other tasks it depends on

Ask: "Ready to start implementing?"
```

---

## Prompt: Implement a Task

```
Task: [task description from above]

Build this task test-first:
1. Write domain tests first (no framework)
2. Write integration tests for adapters (Testcontainers)
3. Implement the code
4. Include migrations if schema changes
5. Add observability (logs, metrics)

Build order for this task:
  Domain → Application → Adapter (in) → Adapter (out) → Tests → Observability

After implementation, report:
  [Domain layer work + tests]
  [Application layer work + tests]
  [Adapter work]
  [Migrations]
  [Observability]
```

---

## Prompt: Verify Definition of Done

```
Here's the completed feature work: [paste summary]

Verify against Definition of Done (constitution.md Art. XII):

- [ ] Spec, plan, and tasks exist and were followed
- [ ] ArchUnit passes (dependency rule enforced)
- [ ] Domain layer is framework-free
- [ ] Lives in correct bounded context; no cross-context coupling
- [ ] Unit + integration tests pass; critical path has e2e test
- [ ] Migrations included and reversible
- [ ] Security: authZ enforced, inputs validated, no secrets
- [ ] App tier stateless; config externalized
- [ ] Health, metrics, structured logs, trace IDs present
- [ ] API versioned; DTOs separate from entities
- [ ] Vulnerability scan and lint pass

Report which boxes are ✅ green and which need work.
```

---

## Prompt: When Architecture Is Challenged

```
Request: [the request that breaks rules]

This request violates Article [X] § [Y]: [quote the rule]

Why this matters:
- [consequence for scaling]
- [consequence for testing]
- [consequence for operations]

Compliant alternative:
[describe the alternative that achieves the goal within constraints]

Shall I proceed with the alternative instead?
```

---

## Prompt: Debug Architecture Issues

```
We're seeing [problem: slow queries / tight coupling / hard to test / etc.]

Which article(s) might be violated?
- Article I: Dependency rule (are dependencies pointing outward?)
- Article II: Tech stack pinned (are we using right versions?)
- Article III: Statelessness (is state in process memory?)
- Article IV: Data isolation (are contexts sharing tables?)
- Article V: Auth (is auth via OIDC?)
- etc.

Diagnose which article is at stake, and propose a fix that honors the constitution.
```

---

## Prompt: Evaluate a Pull Request

```
Here's a PR description: [paste]
Here's the code diff: [paste or describe]

Check against governance:
1. Does this follow the spec/plan?
2. Run the Architecture Gate (8 checks)
3. Is domain layer framework-free?
4. Are tests present (unit + integration)?
5. Is it organized by bounded context?
6. Are migrations included and reversible?
7. Are there observability features (logs, metrics)?
8. Is the DoD checklist complete?

Report:
- ✅ What passes
- ❌ What fails
- 🔴 What blocks merge
```

---

## Prompt: When Time Pressure Hits

```
We're under time pressure. The request is: [what]

I know we need to skip things. What's the MINIMAL correct implementation?
- What can we skip safely? (nothing in domain/tests)
- What must we do? (domain logic, tests, migrations, auth)
- What can we defer? (advanced observability, optimization, cosmetics)

Show me the minimal compliant solution, not the fastest incorrect one.
```

---

## Prompt: Cross-Team Coordination

Use this when multiple agents are working together (with or without Claude Code teams):

```
We have [number] agents working on different contexts: [list contexts]

SHARED RULES (all must follow):
- Domain layer MUST be framework-free in ALL contexts
- Cross-context communication: events only (never shared tables)
- All contexts use same dependency rule (domain ← app ← adapter)
- All instances stateless (state in Redis)
- All contexts have tests + migrations + observability

COORDINATION:
- Agent A owns [context A]
- Agent B owns [context B]
- When contexts touch, send events (never direct DB calls)
- Report work per context at end

Ready to start?
```

---

## Prompt: Migrate Legacy Code

Use this when integrating older code:

```
I have [description of legacy code] that doesn't follow the architecture.

Steps to migrate safely:
1. Don't refactor everything at once
2. Create a new bounded context with proper architecture
3. Migrate data gradually (dual-write pattern)
4. Remove old code only after new code is stable
5. Run ArchUnit to verify dependency rule

Show me a plan to:
- Create the new context (proper architecture)
- Migrate data safely
- Deprecate the old code
- Verify the dependency rule with ArchUnit

This is longer but safer than a big rewrite.
```

---

## Prompt: Handle a Scaling Trigger

Use this when metrics hit a scaling threshold from `scaling-roadmap.md`:

```
We've hit a scaling trigger: [metric exceeds threshold]

Scaling roadmap says: [the next step: caching / read replica / async / extraction]

Before implementing, let's check:
1. Are all existing features already stateless? (check Article III)
2. Are all contexts isolated? (check Article IV)
3. Can this step be additive (not a rewrite)?

If yes to all three, proceed with [scaling step].
If no to any, fix the violation first, then scale.

Show me the compliant scaling plan.
```

---

## Prompt: Refuse a Violation (Template You Can Send)

```
I asked for: [the request]

I cannot do this because it violates Article [X] § [Y]:
"[quote from constitution.md]"

Why this matters:
- [consequence 1]
- [consequence 2]
- [future cost if we ignore it]

Compliant alternative:
[alternative that achieves the goal within constraints]

I'm ready to proceed with this alternative. Approve?
```

---

## Usage Tips

### Tip 1: Load Context Once, Reuse

```
// First message in a conversation:
[Paste AGENT_OPERATING_PRINCIPLES.md or constitution.md]
"Use these rules for the entire conversation."

// All follow-up messages just use the templates above
// The AI remembers the context
```

### Tip 2: Phrase Spec Requests Clearly

❌ Bad: "Add caching"
✅ Good: "Users are waiting 500ms for product queries. What's the root cause? Before adding anything, can we diagnose if it's query complexity, N+1, or just load?"

### Tip 3: Ask for Spec FIRST

❌ Bad: "Implement user profiles"
✅ Good: "I need user profiles. Before code, write a spec: who uses it, what can they do, what are constraints?"

### Tip 4: Run the Architecture Gate Out Loud

❌ Bad: "OK, let's implement"
✅ Good: "Before we code, let's verify the architecture plan:
  - [ ] All dependencies inward? 
  - [ ] Domain framework-free?
  - [ ] One bounded context?
  - [ ] Single deployable still?
  - [ ] Stateless app?
  - [ ] Migration for schema?
  - [ ] Auth via OAuth?
  - [ ] DTOs separate?
  
  All green? Then let's proceed."

### Tip 5: Report Work Per Layer

❌ Bad: "Done. 200 lines of code, 5 test files."
✅ Good: "
  Domain: Product entity, ProductRepository port, 3 domain rules, 12 unit tests.
  Application: SaveProductUseCase, port interfaces, 8 unit tests.
  Adapter (in): ProductRestController, SaveProductRequest/Response DTOs.
  Adapter (out): ProductJpaRepository, V5__create_products_table.sql.
  Observability: structured logs with trace_id, product_saved metric.
  DoD: ✅ all checks pass.
"

---

## Quick Reference

| Need | Prompt Template |
|------|---|
| Start a feature | "I want to build: [what]. Ask clarifying questions, then write spec." |
| Plan the architecture | "Approved spec: [spec]. Create plan + run Architecture Gate." |
| Decompose into tasks | "Approved plan: [plan]. Decompose into 5-6 tasks." |
| Implement a task | "Task: [task]. Build test-first, inside-out." |
| Verify DoD | "Completed work: [summary]. Check against Definition of Done." |
| Refuse a violation | "This request violates Article [X]. Here's why and the compliant alternative." |
| Evaluate a PR | "PR description: [PR]. Check against governance: Architecture Gate, tests, migrations, DoD." |
| Debug a problem | "We're seeing [problem]. Which article is violated? Propose a fix." |
| Scale safely | "Scaling trigger: [metric]. Check statelessness, isolation, additivity." |

---

## Final Note

These templates work with **any AI tool**: Claude, ChatGPT, Gemini, Copilot, or custom models. The tool doesn't matter. **Following the principles does.**

Load the governance rules once, use the templates above, and any AI will produce architecture-first, test-first, scalable code.
