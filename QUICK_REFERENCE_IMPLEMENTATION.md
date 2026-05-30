# Quick Reference: Feature Implementation Checklist

> Print this and use it while implementing any feature

---

## 5-Phase Workflow (Copy-Paste to AI)

### Phase 1: SPECIFY (5 min)
```
I want to build: [feature description in user terms]

Ask clarifying questions first (one round only).

Then write a SPEC with:
- User scenarios (what can users do?)
- Acceptance criteria (how do we know it works?)
- Constraints (performance, security, data)

NO code, NO architecture, NO tech detail.
```

### Phase 2: PLAN (10 min)
```
Spec approved: [paste spec]

Propose architecture:
1. Which bounded context?
2. Domain models (entities, value objects)
3. Application services + ports
4. Data layer (repositories, APIs)
5. Presentation/integration layer

Then run Governance Gate (answer all 8):
1. ✅ All dependencies point inward?
2. ✅ Domain framework-free?
3. ✅ One bounded context only?
4. ✅ Still single deployable?
5. ✅ App tier still stateless?
6. ✅ Migration for schema change?
7. ✅ Auth via OAuth2/OIDC?
8. ✅ DTOs separate from entities?

If ANY NO → stop and fix design.
```

### Phase 3: TASKS (5 min)
```
Plan approved: [paste plan]

Decompose into 5-6 independent tasks.

For each task:
- What to build
- Which files/layer
- Acceptance criteria
- Dependencies on other tasks
```

### Phase 4: IMPLEMENT (60-120 min)
```
Task [N]: [task description]

Build test-first, inside-out:
1. Write unit tests (no framework)
2. Implement code
3. Show tests passing

For domain: Zero framework imports.
For adapters: Integration tests with real DB/API.

Report work per layer:
- Domain: [what + tests]
- Application: [what + tests]
- Adapters: [what + tests]
- Tests: [passing count]
```

### Phase 5: VERIFY (10 min)
```
All tasks complete: [summary]

Run Definition of Done:
- [ ] Spec, plan, tasks followed
- [ ] Architecture enforced (ArchUnit green)
- [ ] Domain framework-free
- [ ] Bounded context correct
- [ ] Tests: unit + integration passing
- [ ] Migrations included + reversible
- [ ] Security: authZ, no secrets
- [ ] Stateless tier maintained
- [ ] Observability: logs, metrics
- [ ] API versioned, DTOs separate

Report ✅ for green items.
```

---

## By-Scenario Commands

### Backend Feature (Spring Boot)

```
Bounded context: [domain-name]
Build: [entity name] domain model

Use bounded context: com.example.[domain].{domain,application,adapter.in,adapter.out}

Tests: JUnit 5, no mocks for domain
Adapter tests: Testcontainers + PostgreSQL
```

### Frontend Feature (Angular)

```
Feature: [feature-name]
Use structure: src/app/features/[feature]/{core,shared,feature-name}

Components: Standalone components + signals
State: No mutable state, use signals
API: Service in data/ layer, never call from component

Tests: Jasmine, mock HTTP
```

### Mobile Feature (Flutter)

```
Feature: [feature-name]
Use structure: lib/features/[feature]/{presentation,domain,data}

Domain: No Flutter imports, pure Dart
State: Riverpod providers, no StatefulWidget
API: Dio in data/ layer
Storage: FlutterSecureStorage (not SharedPreferences)

Tests: Unit (domain), integration (data with mock API)
```

---

## Governance Gate: Quick Checklist

Before you code, answer:

```
1. Domain imports framework?  (YES=FAIL, NO=PASS)
2. All dependencies point inward?  (YES=PASS, NO=FAIL)
3. Lives in one bounded context?  (YES=PASS, NO=FAIL)
4. Still single deployable?  (YES=PASS, NO=FAIL)
5. App tier stateless?  (YES=PASS, NO=FAIL)
6. Migration for schema change?  (YES=PASS, NO=FAIL)
7. Auth via OAuth2/OIDC?  (YES=PASS, NO=FAIL)
8. DTOs separate from entities?  (YES=PASS, NO=FAIL)

All 8 PASS? → Proceed
Any FAIL? → Stop, redesign, resubmit
```

---

## Red Flags: Stop & Fix

| Red Flag | Fix |
|----------|-----|
| **Domain imports Spring/JPA/HTTP** | Move to adapter layer |
| **Shared table across contexts** | Publish events instead |
| **In-memory session state** | Use Redis |
| **No tests for logic** | Write tests first |
| **Hardcoded config** | Use environment variables |
| **JWT in localStorage** | Use BFF + httpOnly cookie |
| **Skipped migrations** | Every schema change needs migration |
| **No observability** | Add logs, metrics, traces |
| **Cross-context direct import** | Use events, published ports only |
| **Requests to skip design** | Refuse, explain cost, offer alternative |

---

## Files to Reference While Building

| File | For |
|------|-----|
| `CLAUDE.md` | Dev commands, project structure |
| `constitution.md` | Governance rules, non-negotiable |
| `AGENTS.md` | How agents apply rules |
| `AGENT_PROMPT_TEMPLATES.md` | Copy-paste prompts |
| `IMPLEMENTATION_EXAMPLE_FLUTTER_LOGIN.md` | Detailed walkthrough (this use case) |

---

## Common Mistakes to Avoid

### ❌ Mistakes

1. **Skip spec, jump to code**
   → Always spec first, get approval before planning

2. **Add feature logic to controller/repository**
   → Domain logic goes in domain layer (pure, framework-free)

3. **Forget migrations**
   → Every schema change = Flyway migration

4. **Store tokens in SharedPreferences / localStorage**
   → Use platform secure storage (Keychain/Keystore/Redis)

5. **Hold state in process memory**
   → Use Redis for sessions, database for persistence

6. **Let one context import another's internals**
   → Use published events/interfaces, never direct DB access

7. **Add microservices before trigger is met**
   → Check `scaling-roadmap.md` first

8. **Skip tests**
   → Domain logic tested first, adapters tested with real DB/API

9. **Forget observability**
   → Add logs (JSON with trace_id), metrics, health endpoints

10. **Silently accept violation requests**
    → Refuse, explain which article, propose compliant alternative

### ✅ Do Instead

1. Spec → Plan → Tasks → Implement → Verify (always)
2. Test-first, inside-out (domain first)
3. Framework-free domain layer
4. Migrations with every schema change
5. Secure storage for sensitive data
6. Stateless app tier (state in Redis/DB)
7. Cross-context via events only
8. Tests as first-class citizens
9. Structured logs + metrics from day one
10. Refuse violations gracefully, propose alternatives

---

## Governance Gate Responses to Memorize

### If Domain Imports Framework:

```
FAIL: Article I § 2 violation.
Domain must be framework-free. This import breaks the dependency rule.

Fix: Move this to the adapter layer.
Domain stays pure Dart/Java/TypeScript.
```

### If Cross-Context Import:

```
FAIL: Article I § 4 violation.
Contexts must not reach into each other's internals.

Fix: Publish an event from context A.
Context B listens and syncs its own data.
No shared tables, no direct imports.
```

### If No Tests:

```
FAIL: Article VII § 1 violation.
Behavior must be specified by tests before implementation.

Fix: Write unit tests for domain logic first.
Write integration tests for adapters (with real DB/API).
```

### If In-Memory Session:

```
FAIL: Article III § 1 violation.
App tier must be stateless. Sessions in Redis, not process memory.

Fix: Externalize all state to Redis/database.
Any instance can handle any request.
```

---

## Prompts That Work with Any AI

### "Spec First" Prompt:
```
DO NOT CODE. Write a spec:
- User scenarios (no tech)
- Acceptance criteria
- Constraints

Ask me clarifying questions first.
```

### "Architecture Gate" Prompt:
```
Before any code, answer all 8:
1. Domain framework-free? ✅/❌
2. Dependencies inward? ✅/❌
3. One context only? ✅/❌
4. Single deployable? ✅/❌
5. Stateless tier? ✅/❌
6. Migration for schema? ✅/❌
7. Auth via OAuth2? ✅/❌
8. DTOs separate? ✅/❌

If ANY ❌, STOP and redesign.
```

### "Test-First" Prompt:
```
Task: [task name]

Build test-first:
1. Write unit tests
2. Implement code
3. Show tests passing

For domain: NO framework imports.
```

### "Refuse Violations" Prompt:
```
This request violates [Article X § Y]:
"[quote from constitution.md]"

Why it matters: [consequence]

Compliant alternative: [solution]

Shall we proceed with the alternative?
```

---

## Timing Guide

| Phase | Time | AI Assistance |
|-------|------|---|
| **Spec** | 5-10 min | Ask clarifying questions, write spec |
| **Plan** | 10-15 min | Propose architecture, run gate |
| **Tasks** | 5-10 min | Decompose into 5-6 tasks |
| **Implement** | 60-180 min | Code test-first per task |
| **Verify** | 10 min | Run DoD checklist |
| **TOTAL** | 90-240 min | 1.5-4 hours per feature |

**Smaller features:** 1.5 hours  
**Medium features:** 2-3 hours  
**Large features:** 3-4 hours

---

## AI Tools Quick Pick

| Tool | Cost | Setup | Best For |
|------|------|-------|----------|
| **Gemini 2.0 Flash** | $0.04/feature | 5 min | Start here |
| **Claude Code** | $1-2/feature | 30 min | Teams, coordination |
| **Gemini 1.5 Pro** | $0.25-1/feature | 5 min | Complex work |
| **ChatGPT (GPT-4)** | $3-5/feature | 10 min | Best architecture |

---

## One-Command Workflow

```bash
# 1. Pick an AI (Gemini 2.0 Flash recommended)
# 2. Load this guideline
# 3. Copy this into your AI:

SPEC_PROMPT = "I want to build: [feature]. Ask clarifying questions first."
PLAN_PROMPT = "Spec approved. Propose architecture + run governance gate."
TASKS_PROMPT = "Plan approved. Decompose into 5-6 tasks."
IMPL_PROMPT = "Task [N]: [description]. Build test-first, inside-out."
VERIFY_PROMPT = "All tasks done. Run Definition of Done checklist."

# 4. Follow through all 5 phases
# 5. Done in 1.5-4 hours
```

---

## Success Criteria: Done When...

✅ All 5 phases completed  
✅ Spec approved by user  
✅ Plan passes governance gate (all 8 checks)  
✅ All tasks implemented and tests pass  
✅ DoD checklist all green  
✅ Code pushed to git with descriptive commit message  
✅ Ready for code review / deployment  

You're ready! 🚀

Pick your AI tool, paste the prompts, and follow the 5-phase workflow.

Questions? Refer to `IMPLEMENTATION_EXAMPLE_FLUTTER_LOGIN.md` for detailed walkthrough.
