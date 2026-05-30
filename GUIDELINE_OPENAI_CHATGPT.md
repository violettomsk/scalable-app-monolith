# OpenAI ChatGPT/GPT-4/Codex Guideline

> How to use OpenAI's models (ChatGPT, GPT-4, Codex) to develop this project while following governance principles

---

## Quick Assessment

| Aspect | GPT-4 | GPT-4o | GPT-3.5 Turbo | Codex |
|--------|-------|--------|---------------|-------|
| Architecture understanding | ✅ Excellent | ✅ Excellent | ⚠️ Good | ✅ Good |
| Long context | ⚠️ 8K-128K | ✅ 128K | ⚠️ 4K | ⚠️ 8K |
| Code generation | ✅ Excellent | ✅ Excellent | ✅ Good | ✅ Expert |
| Spec-first discipline | ⚠️ Needs prompting | ⚠️ Needs prompting | ❌ Weak | ⚠️ Needs prompting |
| Refusing violations | ⚠️ Sometimes | ⚠️ Sometimes | ❌ Rare | ❌ Rare |
| Following governance | ⚠️ With strong prompt | ⚠️ With strong prompt | ❌ Unreliable | ❌ Unreliable |
| Cost per token | ⚠️ Higher | ✅ Mid | ✅ Lowest | ✅ Mid |

**Recommendation:** GPT-4 or GPT-4o for architecture decisions; Codex for implementation code.

---

## Setup: System Prompt

Use this system prompt for all conversations:

```
You are an expert software architect and engineer building a scalable, 
modular monolith application in Java/Spring Boot (backend), Angular (web), 
and Flutter (mobile).

GOVERNANCE (read carefully):
You MUST follow these principles from AGENT_OPERATING_PRINCIPLES.md:

1. SPEC-FIRST: Never write code before spec and plan exist.
   - Start with user scenarios and acceptance criteria (no tech).
   - Ask clarifying questions once, upfront.
   - Return to user for spec approval before proceeding.

2. ARCHITECTURE FIRST (dependency rule):
   - Domain layer: framework-free, pure business logic
   - Application layer: use cases, port interfaces
   - Adapter layer: REST controllers, repositories, external clients
   - Dependencies point inward ONLY: adapter → app → domain
   - NO cross-cutting framework imports in domain.

3. INSIDE-OUT BUILD ORDER:
   - Write domain tests first (unit, no framework)
   - Implement domain logic (entities, value objects, rules)
   - Define application ports (interfaces)
   - Implement adapters (controllers, repositories)
   - Write integration tests (Testcontainers)
   - Add observability (logs, metrics, tracing)

4. BOUNDED CONTEXTS:
   - Code organized by feature/context, not by layer
   - Each context owns its own domain, application, adapters
   - Cross-context: EVENTS ONLY (never shared tables, never direct DB calls)
   - No reaching into another context's internals

5. TEST-FIRST:
   - Behavior specified by tests before implementation
   - Domain logic: unit tests with no framework
   - Adapters: integration tests (Testcontainers for DB)
   - Critical path: at least one e2e test
   - ArchUnit: enforces dependency rule, fails the build

6. STATELESS DEPLOYMENT:
   - Zero in-process state (sessions, caches)
   - All state in Redis (sessions, distributed cache)
   - Any instance replaceable without data loss
   - 12-factor: config from environment, never hardcoded

7. SINGLE DEPLOYABLE (no premature scaling):
   - Ship as modular monolith (one .jar, one instance group)
   - Microservices only when scaling-roadmap.md trigger is met
   - NOT "we might scale later" → only "we ARE scaling now"
   - If a scaling step would require rewrite, an earlier feature violated the rules

WHEN YOU MUST REFUSE:
If a request breaks any of the above:
1. State which principle is violated: "This breaks [principle X] because [detail]"
2. Explain the future cost: "This blocks [scaling consequence] and [testing consequence]"
3. Propose a compliant alternative: "Instead, [solution that achieves the goal safely]"
4. Wait for confirmation: "Shall I proceed with this alternative?"

STACK (pinned, no substitutes):
- Backend: Java 25 LTS + Spring Boot 4.0.x + Spring Framework 7
- Web: Angular 21 (LTS) + Tailwind CSS v4
- Mobile: Flutter 3.44 + Dart 3.12
- Database: PostgreSQL 17.x + Flyway migrations
- Cache/Session: Redis 7.x
- Identity: Keycloak (OAuth2/OIDC)
- Testing: JUnit 5, Testcontainers, ArchUnit, Mockito
- Build: Gradle (Spring Boot), npm (Angular), Flutter CLI

KEY DOCUMENTS (consult first):
- constitution.md: immutable governance law
- AGENTS.md: how agents apply the constitution
- scaling-roadmap.md: when to scale (check before architecture decisions)
- CLAUDE.md: development commands and project structure
- AGENT_OPERATING_PRINCIPLES.md: these principles

WHEN YOU REPORT WORK:
Show work per layer/context:
  Domain: [entities, value objects, domain rules, unit tests]
  Application: [use cases, port interfaces, application tests]
  Adapter (in): [REST controller, DTOs, request/response mapping]
  Adapter (out): [repository, migrations, external client implementations]
  Observability: [structured logs with trace_id, metrics, health endpoint]
  Tests: [unit, integration, e2e status]
  Definition of Done: [checklist from constitution.md Art. XII]

GOVERNANCE GATE (run before ANY code):
Answer all 8 questions:
  1. ✅ Do all dependencies point inward only?
  2. ✅ Is the domain layer framework-free?
  3. ✅ Does this live in ONE bounded context?
  4. ✅ Are we still a single deployable monolith?
  5. ✅ Is the app tier still stateless?
  6. ✅ Is there a migration for schema changes?
  7. ✅ Is auth via OAuth2/OIDC (no custom auth)?
  8. ✅ Are DTOs separate from entities?

If ANY answer is NO, stop and propose a fix before coding.

You are now ready. Ask me what you want to build.
```

---

## Conversation Flow: OpenAI Model

### Phase 1: Specification (Always Start Here)

**You:**
```
I want to build: [feature description in user terms]

Help me write a spec before any architecture or code.
```

**Expected GPT-4 response:**
```
Great! Let me ask clarifying questions first:

1. [Question about scope, constraints, assumptions]
2. [Question about performance/scale needs]
3. [Question about cross-context dependencies]

Once I understand, here's my proposed SPEC:

# Spec: [Feature Name]

**User Scenarios:**
1. As a [user role], I can [action]
2. As a [user role], I can [action]

**Acceptance Criteria:**
- [ ] [Criterion 1]
- [ ] [Criterion 2]

**Constraints:**
- Performance: [max latency, throughput]
- Security: [auth/authz requirements]
- Scale: [expected load]
- Data retention: [policy]

**Success Metrics:**
- [Metric 1]
- [Metric 2]

Is this correct? Questions or changes?
```

**You:**
```
Looks good. Now create the architecture plan.
```

---

### Phase 2: Architecture Planning

**You:**
```
Spec approved: [paste spec]

Now propose the architecture:
1. Which bounded context (existing or new)?
2. What are the entities/value objects?
3. What ports/interfaces are needed?
4. Any schema changes?
5. Any external integrations?

Then run the Governance Gate (8-point checklist).
```

**Expected GPT-4 response:**
```
# Architecture Plan

**Bounded Context:** [context name] (new/existing)

**Domain Model:**
- Entities: [list with key attributes]
- Value Objects: [list with invariants]
- Domain Rules: [business rules]
- Domain Services: [if any orchestration needed]

**Application Layer:**
- Use Cases: [list of services]
- Port Interfaces: [what domain needs from outside]

**Adapter (in):**
- REST Endpoints: [list with HTTP methods, paths, status codes]
- DTOs: [request and response shapes]

**Adapter (out):**
- Persistence: [tables, JPA entities, repository interface]
- Integrations: [external services, events]
- Migrations: [Flyway V_X__description]

**GOVERNANCE GATE (8-point verification):**
1. ✅ All dependencies point inward (domain ← app ← adapter)
2. ✅ Domain imports ZERO frameworks (no Spring, JPA, HTTP, etc.)
3. ✅ Lives in ONE context: [context name]
4. ✅ Still single deployable monolith (no new services)
5. ✅ App tier stateless (no session caches in memory)
6. ✅ Migration for schema change: V_X__description
7. ✅ Auth via OIDC (no custom auth)
8. ✅ DTOs separate from domain entities

**RESULT:** All checks pass. Ready to implement.

Approve?
```

**You:**
```
Approved. Decompose into tasks.
```

---

### Phase 3: Task Decomposition

**You:**
```
Plan approved. Break into 5-6 independent, testable tasks.

For each task:
- Layer(s) it touches
- What needs to be built
- Acceptance criteria
- Dependencies on other tasks
```

**Expected response:**
```
# Task Decomposition

**Task 1: Domain Layer - [Entity Name] Model**
- Build: Entities, value objects, domain rules
- Tests: Unit tests (no framework)
- Acceptance: [all domain rules verified by tests]
- Depends: None

**Task 2: Application Layer - Use Cases**
- Build: [UseCase1], [UseCase2] services + port interfaces
- Tests: Unit tests (mocked ports)
- Acceptance: [all use cases work with mocked deps]
- Depends: Task 1

**Task 3: Adapter (in) - REST Controllers**
- Build: REST controller, request/response DTOs
- Tests: Unit tests for mapping
- Acceptance: [all endpoints map correctly]
- Depends: Task 2

**Task 4: Adapter (out) - Persistence**
- Build: JPA repository implementing port, Flyway migration
- Tests: Integration tests (Testcontainers PostgreSQL)
- Acceptance: [CRUD operations work end-to-end]
- Depends: Task 1, 2

**Task 5: Integration Tests & E2E**
- Build: Integration tests (Testcontainers), e2e test (HTTP calls)
- Tests: All layers together
- Acceptance: [critical path works end-to-end]
- Depends: Task 3, 4

**Task 6: Observability**
- Build: Structured logs with trace_id, metrics (Micrometer), health endpoint
- Tests: Verify logs/metrics in tests
- Acceptance: [all operations traceable in production]
- Depends: Task 5

Ready to start Task 1?
```

**You:**
```
Start Task 1.
```

---

### Phase 4: Implementation

**You:**
```
Task 1: Domain Layer - [Entity] Model

Build test-first:
1. Write unit tests first (no framework, no DB)
2. Implement entities, value objects, domain rules
3. Show all tests passing

No Spring, no JPA, no HTTP imports. Pure domain logic.
```

**Expected GPT-4 response:**
```
# Task 1: Domain Layer

## Unit Tests (JUnit 5 - no framework)

\`\`\`java
class ProductTest {
    @Test
    void canCreateProductWithValidData() {
        // Arrange
        Money price = Money.of("USD", 99.99);
        
        // Act
        Product product = Product.create("Widget", price, 10);
        
        // Assert
        assertThat(product.getName()).isEqualTo("Widget");
        assertThat(product.getPrice()).isEqualTo(price);
        assertThat(product.getStock()).isEqualTo(10);
    }
    
    @Test
    void cannotCreateProductWithNegativePrice() {
        // Act & Assert
        assertThrows(InvalidPriceException.class, () -> {
            Product.create("Widget", Money.of("USD", -10), 10);
        });
    }
}
\`\`\`

## Domain Implementation

\`\`\`java
public class Product {
    private ProductId id;
    private String name;
    private Money price;
    private Integer stock;
    
    // Domain rules as private methods
    private void validatePrice(Money price) {
        if (price.isNegative()) {
            throw new InvalidPriceException("Price cannot be negative");
        }
    }
    
    // Value methods (no setters, only methods that maintain invariants)
    public void decreaseStock(Integer quantity) {
        if (quantity > stock) {
            throw new InsufficientStockException();
        }
        this.stock -= quantity;
    }
}

public record Money(String currency, BigDecimal amount) {
    public Money {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidPriceException("Amount must be positive");
        }
    }
}
\`\`\`

## Test Results
✅ All 8 unit tests pass
✅ No framework imports
✅ All domain rules enforced

Ready for Task 2 (Application Layer)?
```

---

### Phase 5: Verification & DoD

**You:**
```
Feature complete. Here's what was built: [summary]

Run the Definition of Done checklist (constitution.md Art. XII).

Report:
- [ ] Spec, plan, tasks were followed
- [ ] ArchUnit passes
- [ ] Domain layer framework-free
- [ ] Bounded context correct
- [ ] Tests: unit + integration + e2e
- [ ] Migrations reversible
- [ ] Security: authZ, inputs validated
- [ ] App tier stateless
- [ ] Observability: logs, metrics, traces
- [ ] API versioned, DTOs separate
- [ ] Lint/vulnerability scan pass
```

**Expected response:**
```
# Definition of Done Report

✅ Spec, plan, and tasks exist and were followed
✅ ArchUnit test passes (dependency rule enforced)
✅ Domain layer is framework-free (zero Spring/JPA/HTTP imports)
✅ Lives in catalog context (no cross-context coupling)
✅ Unit tests: 12 tests, all pass. Integration tests: 8 tests, Testcontainers. E2E: 3 critical paths.
✅ Migrations: V5__create_products_table.sql (reversible with migration rollback)
✅ Security: authZ enforced via @Secured on use case boundary; inputs validated in DTO; no secrets committed
✅ App tier stateless (no session cache in memory; uses Redis for distributed cache)
✅ Observability: structured JSON logs with trace_id, Micrometer metrics (products_created_total, product_price_avg), /actuator/health endpoint
✅ API versioned (/api/v1/products); DTOs separate from domain entities
✅ Lint passes (Checkstyle); vulnerability scan: 0 critical, 1 medium (logged as known acceptable)

🟢 READY TO MERGE
```

---

## OpenAI-Specific Challenges & Workarounds

### Challenge 1: GPT-4 Won't Always Refuse Violations

**Problem:**
```
You: "Can we shared the user table across contexts for convenience?"

GPT-4 (bad): "Sure, here's how to share a table [provides code]"
```

**Workaround:**
```
You: "Can we share the user table across contexts?

BEFORE ANSWERING: Check if this violates Article I § 4 or Article IV.
If yes, state which article, explain the consequence, propose a compliant 
alternative. DO NOT PROVIDE THE VIOLATION CODE."

GPT-4 (good): "This violates Article I § 4 (cross-context integrity) and 
Article IV (data isolation). This blocks read replicas and microservice extraction.

Alternative: 
- Auth context owns users table
- Other contexts cache the user_id
- Cross-context: events only
Shall I show this approach?"
```

### Challenge 2: Context Window Limits

**Problem:** Long conversations lose governance context.

**Workaround:**
```
// Every 5-10 messages, reinforce the system prompt:

You: "Continue following the system prompt from the start of this conversation.
[Paste key rules if context seems lost]

Next task: [task description]"

// Or start fresh conversation with: 
You: "[Full system prompt] ... Now, here's where we left off: [summary]"
```

### Challenge 3: GPT-3.5 Turbo Doesn't Follow Complex Rules

**Problem:** GPT-3.5 ignores governance constraints.

**Workaround:** **Upgrade to GPT-4 or GPT-4o for architecture work.** Use GPT-3.5 only for:
- Completing boilerplate code
- Writing tests from specification
- Refactoring within a layer
- Documentation

### Challenge 4: Spec-First Discipline Isn't Automatic

**Problem:** GPT-4 jumps to code without spec approval.

**Workaround:**
```
You: "DO NOT WRITE CODE. First, answer these:
1. Is the spec approved? [Yes/No]
2. Is the architecture plan approved? [Yes/No]
3. Has the Governance Gate passed? [Yes/No]

If all 'Yes', then implement. If any 'No', ask for approval first."

// Repeat this at the start of each implementation task
```

---

## Integration with OpenAI API (Programmatic)

If using OpenAI API directly (not ChatGPT web):

```python
import openai

system_prompt = """
[Full system prompt from above]
"""

messages = [
    {"role": "system", "content": system_prompt},
    {"role": "user", "content": "I want to build: [feature]"}
]

response = openai.ChatCompletion.create(
    model="gpt-4",  # Use gpt-4 or gpt-4o, NOT gpt-3.5-turbo
    messages=messages,
    temperature=0.2,  # Lower = more focused, less creative/tangential
    max_tokens=2000,  # Enforce reasonable response length
    stop=["User:", "---"]  # Stop at conversation boundaries
)

# Always extract spec/plan/tasks, verify against governance
assistant_response = response["choices"][0]["message"]["content"]
print(assistant_response)

# Continue conversation
messages.append({"role": "assistant", "content": assistant_response})
messages.append({"role": "user", "content": "Approved. Next phase: [task]"})
```

---

## Best Practices: OpenAI Models

### ✅ DO

- **Use GPT-4 or GPT-4o for architecture decisions.** GPT-3.5 is unreliable for governance.
- **Reinforce the system prompt every 5-10 messages** or when context seems lost.
- **Ask for spec FIRST, always.** "Before code, write a spec with scenarios and criteria."
- **Run the Governance Gate explicitly** at the start of planning: "Answer all 8 questions."
- **Refuse violations loudly.** "Stop, this violates Article X. Here's why and the compliant alternative."
- **Report work per layer.** "Domain: ..., Application: ..., Adapters: ..., Tests: ..., DoD: ..."
- **Use temperature=0.2 for API** (focused, less tangential).
- **Chunk long conversations.** Start fresh context every 20-30 messages.

### ❌ DON'T

- **Don't use GPT-3.5 for architecture.** It ignores governance constraints.
- **Don't jump to code before spec.** Always spec → plan → tasks → implement.
- **Don't skip the Governance Gate.** "Run the 8-point checklist before coding."
- **Don't accept violation code.** Always refuse and propose compliant alternative.
- **Don't leave context drift unchecked.** Reinforce rules every N messages.
- **Don't mix tools.** If you start with GPT-4, stay with it for the feature (consistency).

---

## Example: Full Feature in OpenAI

**Message 1: Setup**
```
You: [Paste full system prompt from above]

I want to build a feature: [description]
```

**Message 2: Spec**
```
You: "Write a SPEC (no code, no architecture): user scenarios and acceptance criteria.
Ask clarifying questions first."

ChatGPT: [Asks questions, proposes spec]

You: "Approved."
```

**Message 3: Plan**
```
You: "Propose the architecture plan + run Governance Gate (8-point checklist)."

ChatGPT: [Plan + checklist]

You: "Approved."
```

**Message 4: Tasks**
```
You: "Decompose into 5-6 tasks."

ChatGPT: [Tasks]

You: "Ready to start Task 1."
```

**Messages 5-10: Implementation**
```
You: "Task 1: [Domain layer]"
ChatGPT: [Tests, code, report]

You: "Task 2: [Application layer]"
ChatGPT: [Code, report]

...and so on
```

**Final Message: DoD**
```
You: "Run DoD checklist on final implementation."

ChatGPT: [Verification report]

You: "Ready to merge."
```

**Total:** 10-15 messages for a complete feature.

---

## Summary

| Model | Best For | Limitations | Tips |
|-------|----------|---|---|
| GPT-4 | Architecture, spec, planning | Expensive, sometimes doesn't refuse violations | Set temperature=0.2, reinforce system prompt |
| GPT-4o | Balanced: architecture + code | Similar to GPT-4 | Lower cost than GPT-4, similar quality |
| GPT-3.5 Turbo | Boilerplate, tests, docs | Ignores governance, lacks architectural sense | Don't use for design decisions |
| Codex | Code completion in IDE | No architectural sense, no multi-turn reasoning | Use with linters + human review |

**Final recommendation:** Use **GPT-4 or GPT-4o** with the system prompt above. Follow the conversation flow strictly. Reinforce governance rules every few messages. Always spec-first, never code-first.
