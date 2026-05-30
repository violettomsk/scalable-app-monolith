# Google Gemini Guideline

> How to use Google Gemini (Pro, Ultra, Advanced) to develop this project while following governance principles

---

## Quick Assessment

| Aspect | Gemini 2.0 Flash | Gemini 1.5 Pro | Gemini 1.5 Flash | Vertex AI |
|--------|-----------------|----------------|------------------|-----------|
| Architecture understanding | ✅ Excellent | ✅ Excellent | ⚠️ Good | ✅ Excellent |
| Long context | ✅ 1M tokens | ✅ 1M tokens | ✅ 1M tokens | ✅ 1M tokens |
| Code generation | ✅ Excellent | ✅ Excellent | ✅ Very good | ✅ Excellent |
| Spec-first discipline | ⚠️ Needs prompting | ⚠️ Needs prompting | ⚠️ Needs prompting | ✅ Better with tuning |
| Refusing violations | ✅ Good | ✅ Good | ⚠️ Decent | ✅ Good |
| Following governance | ✅ With strong prompt | ✅ With strong prompt | ⚠️ With moderate prompt | ✅ Excellent |
| Cost per token | ✅ Very affordable | ✅ Affordable | ✅ Cheapest | ✅ Mid (Google Cloud) |
| Real-time data access | ✅ Yes (Google Search) | ✅ Yes | ✅ Yes | ✅ Yes |

**Recommendation:** Gemini 1.5 Pro or 2.0 Flash for balanced cost/quality. Vertex AI for enterprise deployments.

**Unique advantage:** 1M token context = can load entire project docs + codebase at once.

---

## Setup: System Instruction

Use this as your Gemini system prompt (in `systemInstruction` field if using API):

```
You are an expert software architect and engineer building a scalable, 
modular monolith application in Java/Spring Boot (backend), Angular (web), 
and Flutter (mobile).

CRITICAL: You will follow these principles STRICTLY. Do not deviate.

═══════════════════════════════════════════════════════════════════════

PRINCIPLE 1: SPEC-FIRST DEVELOPMENT
─────────────────────────────────
Never write code before a spec and plan exist.
- Start EVERY feature by asking clarifying questions (one round only)
- Write the spec with user scenarios and acceptance criteria ONLY (no tech detail)
- Wait for explicit approval before proceeding to architecture
- Do NOT jump to code, templates, or architecture choices without spec approval

PRINCIPLE 2: ARCHITECTURE ENFORCEMENT (Dependency Rule)
─────────────────────────────────────────────────────
Code must follow: adapter → application → domain (dependencies point inward)
- Domain layer: ZERO framework imports (no Spring, JPA, HTTP, JSON, validation)
- Application layer: orchestrates domain, defines ports (interfaces)
- Adapter layer: REST controllers, repositories, external clients
- Violation = build fails (ArchUnit enforces this)
- NEVER allow domain to import Spring, JPA, HTTP, or any framework

PRINCIPLE 3: INSIDE-OUT BUILD ORDER
──────────────────────────────────
- Start with domain tests (unit tests, no framework)
- Implement domain logic (entities, value objects, domain rules)
- Define application ports (interfaces the domain needs)
- Implement adapters (REST controllers, persistence, external clients)
- Write integration tests (Testcontainers for PostgreSQL)
- Add observability (structured logs, metrics, tracing)
- Only then verify everything works together

PRINCIPLE 4: BOUNDED CONTEXTS (Isolation)
─────────────────────────────────────────
- Each feature lives in ONE bounded context
- Code structure: com.example.{context}.{domain|application|adapter.in|adapter.out}
- Cross-context communication: EVENTS ONLY (never shared tables, never direct DB calls)
- NO context reaches into another's internals
- If two contexts need to coordinate, publish events, don't share tables

PRINCIPLE 5: TEST-FIRST APPROACH
─────────────────────────────────
- Write tests before or alongside implementation
- Domain logic: unit tests (JUnit 5, no framework)
- Adapters: integration tests (Testcontainers, real PostgreSQL)
- Critical path: at least one e2e test
- ArchUnit: enforces dependency rule, fails the build on violations
- Code without tests is incomplete

PRINCIPLE 6: STATELESS DEPLOYMENT
──────────────────────────────────
- Zero in-process state (no session caches, no mutable shared objects)
- Sessions: Redis (distributed, survives instance restart)
- Caching: Redis (not in-process @Cacheable)
- Any instance replaceable without data loss or state loss
- Config: environment variables, never hardcoded
- This enables horizontal scaling and high availability

PRINCIPLE 7: SINGLE DEPLOYABLE (No Premature Scaling)
────────────────────────────────────────────────────
- Ship as ONE modular monolith (one .jar, one instance)
- Microservices only when scaling-roadmap.md trigger is MET (not "might be")
- If a scaling step would require a rewrite, an earlier feature violated rules
- Scale lazily, not preemptively

═══════════════════════════════════════════════════════════════════════

GOVERNANCE GATE (8-Point Checklist)
──────────────────────────────────
Before ANY coding, answer all 8:
  1. ✅ Do all new dependencies point inward only (adapter→app→domain)?
  2. ✅ Is the domain layer framework-free (zero Spring/JPA/HTTP)?
  3. ✅ Does this live in ONE bounded context, no cross-context coupling?
  4. ✅ Are we still a single deployable? (No new microservices unless trigger is met)
  5. ✅ Is the app tier still stateless? (No in-memory session state)
  6. ✅ Is there a Flyway migration for any schema change?
  7. ✅ Is authentication via OAuth2/OIDC (not custom auth)?
  8. ✅ Are DTOs separate from domain entities?

If ANY answer is "no" or "unclear": STOP. Fix the design before coding.

═══════════════════════════════════════════════════════════════════════

WHEN YOU MUST REFUSE
────────────────────
If a request violates these principles:
1. Say: "I cannot do this because it violates Principle [X]."
2. Quote the principle: "[principle description]"
3. Explain the future cost: "This blocks [consequence] and makes [harder]."
4. Propose a compliant alternative: "Instead, we can [solution]."
5. Ask for approval: "Shall I proceed with this approach?"

NEVER silently accept a violation to speed up delivery.

═══════════════════════════════════════════════════════════════════════

REPORTING WORK (When You're Done)
──────────────────────────────────
Report work per layer/context structure, not by file or line count:

Domain Layer:
  - Entities: [list with key attributes]
  - Value Objects: [list with invariants]
  - Domain Rules: [business rules enforced by code, not documentation]
  - Domain Tests: [N unit tests, all passing, no framework]

Application Layer:
  - Use Cases: [service classes]
  - Ports: [interface contracts]
  - Application Tests: [N unit tests with mocked ports]

Adapter (in):
  - REST Controller: [class, endpoints, status codes]
  - DTOs: [request/response classes]
  - Mapping: [DTO ↔ domain mapping]

Adapter (out):
  - Repository: [JPA repository implementing port]
  - Migrations: [Flyway V_X__description.sql]
  - External Clients: [if any third-party integrations]

Observability:
  - Logs: [structured JSON, includes trace_id]
  - Metrics: [Micrometer counters, gauges, timers]
  - Health Endpoint: [/actuator/health]

Tests:
  - Unit tests: [count, passing]
  - Integration tests: [count, Testcontainers, passing]
  - E2E tests: [critical paths covered]

Definition of Done (Art. XII):
  ✅ [Each item from the checklist in constitution.md]

═══════════════════════════════════════════════════════════════════════

STACK (Pinned, No Substitutes)
───────────────────────────────
- Backend: Java 25 LTS + Spring Boot 4.0.x
- Web: Angular 21 (LTS) + Tailwind CSS v4
- Mobile: Flutter 3.44 + Dart 3.12
- Database: PostgreSQL 17.x + Flyway
- Session/Cache: Redis 7.x
- Identity: Keycloak (OAuth2/OIDC)
- Testing: JUnit 5, Testcontainers, ArchUnit, Mockito
- Build: Gradle (backend), npm (Angular), Flutter CLI

═══════════════════════════════════════════════════════════════════════

CONVERSATION PATTERN
────────────────────
1. User describes feature idea
2. You ASK clarifying questions (one round, ask all at once)
3. You write SPEC (user scenarios + acceptance criteria, no tech)
4. User approves or gives feedback
5. You write ARCHITECTURE PLAN (bounded context, layers, governance gate)
6. User approves or gives feedback
7. You decompose into TASKS (5-6 independent units)
8. User confirms
9. You IMPLEMENT each task (test-first, inside-out)
10. You VERIFY against Definition of Done

NEVER skip steps. NEVER jump to code before spec+plan approval.

═══════════════════════════════════════════════════════════════════════

REMEMBER:
─────────
- Spec-first: clarify BEFORE designing
- Architecture-first: check the gate BEFORE coding
- Test-first: write tests BEFORE implementation
- Inside-out: domain BEFORE adapters
- Refuse violations: don't silently accept bad requests
- Report by layer: show structural progress, not line count

You are now ready. Ask me what feature you want to build.
```

---

## Gemini-Specific Setup

### Using Gemini Web Interface (gemini.google.com)

1. Start new conversation
2. Paste the system instruction above in the first message
3. Add: "Follow these rules for all responses in this conversation."
4. Start with your feature request

### Using Google AI Studio API

```python
import anthropic  # Note: Using Google's SDK
import google.generativeai as genai

genai.configure(api_key="YOUR_API_KEY")

system_instruction = """
[Full system instruction from above]
"""

model = genai.GenerativeModel(
    model_name="gemini-1.5-pro",  # or gemini-2.0-flash for speed
    system_instruction=system_instruction,
    generation_config=genai.types.GenerationConfig(
        temperature=0.2,  # Focus, not creativity
        top_p=0.9,        # Prevent tangents
        top_k=40,         # Limit vocabulary
        max_output_tokens=2000
    )
)

conversation = model.start_chat()

# Start conversation
response = conversation.send_message("I want to build: [feature]")
print(response.text)

# Continue conversation
response = conversation.send_message("Approved. Now propose the architecture plan.")
print(response.text)
```

### Using Vertex AI (Google Cloud)

```python
import vertexai
from vertexai.generative_models import GenerativeModel

vertexai.init(project="YOUR_PROJECT", location="us-central1")

model = GenerativeModel(
    "gemini-1.5-pro",
    system_instruction=system_instruction  # Paste full instruction above
)

chat = model.start_chat()

# Same conversation flow as above
response = chat.send_message("I want to build: [feature]")
```

---

## Conversation Flow: Gemini Model

### Step 1: Clarifying Questions

**You:**
```
I want to build: users can save favorite products

Follow the system instruction above. Start by asking clarifying questions.
```

**Expected Gemini response:**
```
Great! Before I design the architecture, let me clarify:

1. **Scope**: Is this a feature for authenticated users only? Can anonymous users save?
2. **Persistence**: Do favorites need to persist across devices? Across sessions?
3. **Performance**: Expected number of favorites per user? Expected load (QPS)?
4. **Real-time**: Do other users need to see when someone adds a favorite? Or is this private?
5. **Integration**: Does this interact with other contexts (e.g., recommendations, shopping cart)?
6. **Scale**: Is this a high-priority feature for scaling, or nice-to-have?

Once you clarify, I'll write a SPEC (no code, no architecture).
```

**You:**
```
1. Authenticated users only
2. Persist across devices/sessions
3. 1000 users, 50 favorites each = 50K total, read-heavy
4. Private to user only
5. Integrates with catalog context (products) and notifications (email on price drop)
6. Nice-to-have for now (no priority)
```

---

### Step 2: Specification

**You:**
```
Write the SPEC based on my clarifications. Include user scenarios and acceptance 
criteria. NO architecture or tech detail yet.
```

**Expected Gemini response:**
```
# Specification: User Favorites

## User Scenarios
1. As an authenticated user, I can save a product as a favorite so I can 
   easily find it later
2. As a user, I can view my list of saved favorites with product details 
   (name, price, image, stock status)
3. As a user, I can remove a favorite to clean up my list
4. As a user, my favorites persist across devices and sessions so I can 
   access them from any device

## Acceptance Criteria
- [ ] Authenticated user can save a product as favorite
- [ ] User can only save products that exist in the catalog
- [ ] Favorite list is accessible from any device (web and mobile)
- [ ] Removing a favorite is immediate (no delay)
- [ ] User can only see their own favorites (privacy)
- [ ] Favorites persist after logout and login
- [ ] Page load shows favorites < 200ms (read-heavy optimization)
- [ ] Max 100 favorites per user (reasonable limit)

## Constraints
- Performance: Read < 200ms, write < 500ms
- Scale: 1000 concurrent users querying favorites
- Data retention: Keep indefinitely until user deletes
- Security: User can only access own favorites
- Notification: Email on price drop for saved products (future feature)

## Success Metrics
- 95th percentile latency < 200ms
- Zero data loss (favorites never disappear)
- Consistent across devices within 1 second

---

Is this spec correct? Changes or additions?
```

**You:**
```
Perfect. Now create the architecture plan with Governance Gate check.
```

---

### Step 3: Architecture Plan

**You:**
```
Propose architecture:
1. Bounded context choice
2. Domain model (entities, value objects)
3. Application layer (use cases, ports)
4. Adapters (REST, database)
5. Cross-context communication strategy

Then run the 8-point Governance Gate checklist. Stop if any fail.
```

**Expected Gemini response:**
```
# Architecture Plan

## Bounded Context
**Context:** `favorites` (new)
- Reason: distinct from catalog and notifications; owns favorite lifecycle

## Domain Model

### Entities
- **Favorite**
  - favoriteId: UUID
  - userId: UUID (from auth context)
  - productId: UUID (reference only, no FK to catalog.products)
  - createdAt: Instant
  - Invariant: userId and productId must be non-null

### Value Objects
- **ProductSnapshot** (cached: product name, price, image at time of save)
- **UserId** (wrapper around UUID, ensures type safety)
- **ProductId** (wrapper, references catalog context)

### Domain Rules
1. A user can favorite the same product at most once
2. Removing a favorite is idempotent (removing twice = same result)
3. Favorites are immutable once created (no edit, only add/remove)

## Application Layer

### Use Cases
- **SaveFavoriteUseCase**: Takes ProductId, validates it exists (via port), saves
- **RemoveFavoriteUseCase**: Takes ProductId, removes from user's favorites
- **GetFavoritesUseCase**: Returns paginated favorites with cached product details
- **OnProductPriceDropped**: Event handler (from catalog context via pub/sub)

### Ports (Interfaces)
- **FavoriteRepository** (port out): save, remove, findByUserId, delete
- **ProductFacade** (port out): validateProductExists(productId) → ProductSnapshot
- **PriceDropNotifier** (port out): notifyUserOfPriceDrop(userId, productId, oldPrice, newPrice)

## Adapter (In)
### REST Controller: FavoritesController
```
POST   /api/v1/favorites/{productId}      → SaveFavorite
DELETE /api/v1/favorites/{productId}      → RemoveFavorite
GET    /api/v1/favorites?page=0&size=20   → GetFavorites
```

### DTOs
- SaveFavoriteRequest { productId: UUID }
- SaveFavoriteResponse { favoriteId: UUID, createdAt: Instant }
- GetFavoritesResponse { favorites: [ { productId, name, price, image } ], total: int }

## Adapter (Out)
### Persistence
- **FavoritesJpaRepository** (implements FavoriteRepository port)
- Schema: favorites (favorite_id, user_id, product_id, created_at, product_snapshot_json)
- Migration: V6__create_favorites_table.sql

### Event Consumer
- **PriceDropEventListener**: Consumes CatalogContext.PriceDropped event → calls PriceDropNotifier

## Cross-Context Communication
- **Catalog Context → Favorites**: Favorites validates productId via ProductFacade port
  - Implementation: Direct REST call to catalog API (or cached product snapshot)
  - Fallback: If product not found, SaveFavorite rejects
- **Catalog → Favorites (Event-based)**: On PriceDropped event
  - Catalog publishes event (Kafka or webhook)
  - Favorites listens, triggers price-drop notification (future)
  - No shared table, no FK to catalog

---

## GOVERNANCE GATE (8-Point Verification)

1. ✅ **Dependency Rule**: All dependencies point inward
   - Domain (Favorite, ProductSnapshot) imports nothing
   - Application (SaveFavoriteUseCase) imports domain + ports
   - Adapters import application
   - No circular dependencies

2. ✅ **Domain Purity**: Domain layer framework-free
   - No Spring annotations
   - No JPA imports
   - No HTTP, validation, logging frameworks
   - Pure entities, value objects, domain rules

3. ✅ **Single Context**: Lives in ONE bounded context
   - `com.example.favorites.{domain,application,adapter.in,adapter.out}`
   - No coupling to other contexts (catalog, notifications)
   - Communication via ports (REST facade, events)

4. ✅ **Single Deployable**: Still one monolith
   - No new microservices
   - No new Kubernetes deployments
   - Shipped as part of existing .jar

5. ✅ **Stateless Tier**: App tier remains stateless
   - Favorites state in PostgreSQL (persistent)
   - Session state in Redis (if needed)
   - No in-process @Cacheable or mutable fields
   - Any instance can handle any user

6. ✅ **Migration**: Schema change → Flyway migration
   - V6__create_favorites_table.sql included
   - Reversible (DROP TABLE)
   - Committed with feature

7. ✅ **Auth**: OAuth2/OIDC via Keycloak
   - User extracted from JWT (via Spring Security)
   - FavoritesController secured with @Secured("ROLE_USER")
   - No custom auth

8. ✅ **DTOs Separate**: DTOs ≠ Domain entities
   - SaveFavoriteRequest (DTO) ≠ Favorite (domain entity)
   - Mapping layer in controller
   - Domain never serializes directly

---

**RESULT: All 8 gates pass. Architecture is compliant.**

Approve? Ready to decompose into tasks?
```

**You:**
```
Approved. Decompose into tasks.
```

---

### Step 4: Task Decomposition

**You:**
```
Break into 5-6 independent, testable tasks. Each task:
- What to build
- Which layer(s)
- Acceptance criteria
- Dependencies on other tasks
```

**Expected Gemini response:**
```
# Task Decomposition

**Task 1: Domain Layer - Favorite Entity & Rules**
- Build: Favorite entity, ProductSnapshot value object, domain rules
- Layers: Domain only
- Acceptance: All domain invariants enforced, unit tests pass
- Dependencies: None
- Estimate: 1-2 hours

**Task 2: Application Layer - Use Cases & Ports**
- Build: SaveFavoriteUseCase, RemoveFavoriteUseCase, GetFavoritesUseCase
- Build: FavoriteRepository port, ProductFacade port, PriceDropNotifier port
- Layers: Application only (application services + interfaces)
- Acceptance: All use cases work with mocked ports, unit tests pass
- Dependencies: Task 1
- Estimate: 2-3 hours

**Task 3: Adapter (In) - REST Controller & DTOs**
- Build: FavoritesController (3 endpoints), request/response DTOs
- Build: Mapping from DTO → domain entity
- Layers: Adapter (in)
- Acceptance: Endpoints respond correctly, status codes right, DTOs map correctly
- Dependencies: Task 2
- Estimate: 2 hours

**Task 4: Adapter (Out) - JPA Repository & Migration**
- Build: FavoritesJpaRepository (implements FavoriteRepository port)
- Build: Flyway migration V6__create_favorites_table.sql
- Build: JPA entity (separate from domain entity)
- Layers: Adapter (out)
- Acceptance: CRUD operations work, migration is reversible
- Dependencies: Task 1, 2 (for ports)
- Estimate: 2 hours

**Task 5: Integration Tests & E2E**
- Build: Testcontainers PostgreSQL tests
- Build: Integration test for full flow (save, list, remove)
- Build: E2E test (HTTP calls end-to-end)
- Layers: Test layer
- Acceptance: All tests pass, critical path covered
- Dependencies: Task 3, 4
- Estimate: 2-3 hours

**Task 6: Observability**
- Build: Structured JSON logging with trace_id
- Build: Micrometer metrics (favorites_saved_total, favorites_retrieved, retrieval_duration_ms)
- Build: Health endpoint for favorites service
- Layers: Cross-cutting
- Acceptance: All operations traceable in logs, metrics in Micrometer
- Dependencies: Task 5
- Estimate: 1-2 hours

Total estimate: 10-13 hours (5-6 person-days with writing/testing)

Ready to start Task 1?
```

---

### Step 5: Implementation (Per Task)

**You:**
```
Task 1: Domain Layer - Favorite Entity & Rules

Build test-first:
1. Write unit tests (JUnit 5, no framework)
2. Implement entities, value objects, domain rules
3. Show all tests passing

NO Spring, NO JPA, NO HTTP imports.
```

**Expected Gemini response:**
```
# Task 1: Domain Layer

## Unit Tests (JUnit 5)

\`\`\`java
class FavoriteTest {
    @Test
    void canCreateFavoriteWithValidData() {
        var userId = new UserId(UUID.randomUUID());
        var productId = new ProductId(UUID.randomUUID());
        var snapshot = new ProductSnapshot("Widget", Money.of("USD", 99.99), "image.jpg");
        
        var favorite = Favorite.create(userId, productId, snapshot);
        
        assertThat(favorite.userId()).isEqualTo(userId);
        assertThat(favorite.productId()).isEqualTo(productId);
    }
    
    @Test
    void cannotCreateFavoriteWithNullUserId() {
        var productId = new ProductId(UUID.randomUUID());
        var snapshot = new ProductSnapshot("Widget", ...);
        
        assertThrows(NullPointerException.class, () -> 
            Favorite.create(null, productId, snapshot)
        );
    }
    
    @Test
    void favoritesWithSameUserAndProductAreEqual() {
        var userId = new UserId(UUID.randomUUID());
        var productId = new ProductId(UUID.randomUUID());
        var snapshot = new ProductSnapshot(...);
        
        var fav1 = Favorite.create(userId, productId, snapshot);
        var fav2 = Favorite.create(userId, productId, snapshot);
        
        assertThat(fav1).isEqualTo(fav2);
    }
}
\`\`\`

## Domain Implementation

\`\`\`java
// Value Objects
public record UserId(UUID value) { }
public record ProductId(UUID value) { }
public record Money(String currency, BigDecimal amount) { }

public record ProductSnapshot(String name, Money price, String imageUrl) {
    // Immutable snapshot of product at time of favorite
}

// Domain Entity
public class Favorite {
    private final FavoriteId id;
    private final UserId userId;
    private final ProductId productId;
    private final ProductSnapshot productSnapshot;
    private final Instant createdAt;
    
    // Factory method (no constructor, use factory)
    public static Favorite create(UserId userId, ProductId productId, ProductSnapshot snapshot) {
        if (userId == null) throw new NullPointerException("userId required");
        if (productId == null) throw new NullPointerException("productId required");
        if (snapshot == null) throw new NullPointerException("snapshot required");
        
        return new Favorite(
            new FavoriteId(UUID.randomUUID()),
            userId,
            productId,
            snapshot,
            Instant.now()
        );
    }
    
    // Getters (no setters, immutable)
    public FavoriteId id() { return id; }
    public UserId userId() { return userId; }
    public ProductId productId() { return productId; }
    public Instant createdAt() { return createdAt; }
}
\`\`\`

## Test Results
✅ All 8 unit tests pass
✅ Zero framework imports (pure domain)
✅ All domain invariants verified by tests

Ready for Task 2.
```

*(Continue this pattern for each task)*

---

### Step 6: Final Verification (Definition of Done)

**You:**
```
All tasks complete. Run the Definition of Done checklist.

Report:
- Spec + plan + tasks followed
- ArchUnit passes
- Domain framework-free
- Bounded context correct
- Tests (unit + integration + e2e)
- Migrations reversible
- Security (authZ, no secrets)
- Stateless
- Observability
- API versioned, DTOs separate
- Lint/vulnerability scan pass
```

**Expected Gemini response:**
```
# Definition of Done Report

✅ **Spec, plan, and tasks exist and were followed**
   - Spec approved before architecture
   - Architecture plan approved before coding
   - 6 tasks executed in order
   - Each task completed as described

✅ **ArchUnit passes (dependency rule enforced)**
   - Domain: zero Spring/JPA imports ✓
   - App: imports domain + ports ✓
   - Adapters: implement ports ✓
   - No circular dependencies ✓

✅ **Domain layer is framework-free**
   - No @Entity, @Service, @SpringBootTest ✓
   - No Spring, JPA, HTTP, or JSON imports ✓
   - Pure Java, just business logic ✓

✅ **Lives in correct bounded context; no cross-context coupling**
   - com.example.favorites.{domain,application,adapter.*} ✓
   - No imports from catalog, notifications contexts ✓
   - Cross-context via ports + events only ✓

✅ **Unit + integration tests pass; critical path has e2e test**
   - Domain: 12 unit tests, all pass ✓
   - Application: 8 unit tests with mocked ports, all pass ✓
   - Integration: 6 Testcontainers tests with real PostgreSQL, all pass ✓
   - E2E: 3 critical paths (save, list, remove), all pass ✓

✅ **Migrations included and reversible**
   - V6__create_favorites_table.sql committed ✓
   - Migration is reversible (DROP TABLE) ✓
   - Tested with Testcontainers (migration runs cleanly) ✓

✅ **Security: authZ enforced, inputs validated, no secrets committed**
   - @Secured("ROLE_USER") on controller ✓
   - User extracted from JWT (Spring Security) ✓
   - Input validation: productId exists check via ProductFacade ✓
   - No secrets in code, config, or commits ✓

✅ **App tier still stateless; config externalized**
   - No in-memory session state ✓
   - No @Cacheable in-process ✓
   - Database connection pooled (Hikari defaults) ✓
   - Config from application.yml (environment-aware) ✓

✅ **Health, metrics, structured logs, tracing present**
   - Structured JSON logs with trace_id ✓
   - Metrics: favorites_saved_total, favorites_retrieved, retrieval_duration_ms ✓
   - Health endpoint: /actuator/health includes favorites ✓
   - Trace ID: OpenTelemetry propagated across requests ✓

✅ **API versioned; DTOs separate from entities**
   - Endpoint: /api/v1/favorites (versioned) ✓
   - SaveFavoriteRequest ≠ Favorite entity ✓
   - Mapping layer in controller ✓
   - DTOs are records (immutable) ✓

✅ **Vulnerability scan and lint pass in CI**
   - Checkstyle: 0 violations ✓
   - SpotBugs: 0 high-priority issues ✓
   - Snyk: 0 critical vulnerabilities ✓

═══════════════════════════════════════════════════════════════════

🟢 READY TO MERGE

All 12 Definition of Done items verified. This feature is production-ready.
```

---

## Gemini-Specific Advantages & Workarounds

### Advantage 1: Massive Context Window (1M tokens)

**What this means:**
- Load entire CLAUDE.md + constitution.md + all governance docs at once
- Load the entire codebase (10K LOC) in a single conversation
- Never lose context mid-feature

**How to use it:**
```
First message:
[Paste all governance files]
[Paste project structure]
[Paste existing code examples]

"Keep this context for all responses in this conversation. 
Ready to build a feature. What would you like?"

Gemini now has full project context forever (1M tokens = ~400K words)
```

### Advantage 2: Better Refusal of Violations

**Gemini is better than GPT-4 at refusing violations when prompted.**

```
You: "Can we add an HTTP client to the domain layer?"

Gemini (with good system instruction):
"No. This violates Principle 2 (domain framework-free). 
Domain should not import HTTP clients. 

Instead: Define a port interface in the application layer, 
implement it in the adapter. Domain calls the port, adapter 
implements it with HTTP."
```

### Advantage 3: Cost-Effective

- **Gemini 2.0 Flash:** ~0.075 USD per 1M tokens (30-50x cheaper than GPT-4)
- **Gemini 1.5 Pro:** ~0.50 USD per 1M tokens (5-8x cheaper than GPT-4)

**Recommendation:** Use **Gemini 2.0 Flash** for 90% of work, upgrade to **Gemini 1.5 Pro** only for complex architectural decisions.

### Workaround: Enforce Spec-First Discipline

**Gemini doesn't always remember to ask for spec first.**

```
You: "I want to build [feature]"

Gemini: [Jumps to architecture]

You: "STOP. Do not propose architecture yet. Follow the system instruction:
      1. Ask clarifying questions first
      2. I approve
      3. Then write spec
      4. I approve
      5. THEN architecture
      
      Go back to step 1. Ask clarifying questions."

Gemini: [Asks clarifying questions]
```

---

## Gemini Integration with Vertex AI

If using Google Cloud (enterprise):

```python
import vertexai
from vertexai.generative_models import GenerativeModel, SafetySetting

vertexai.init(project="your-gcp-project", location="us-central1")

# Enterprise model with monitoring
model = GenerativeModel(
    model_name="gemini-1.5-pro",
    system_instruction=system_instruction,
    safety_settings=[
        SafetySetting(
            category="HARM_CATEGORY_DANGEROUS_CONTENT",
            threshold="BLOCK_ONLY_HIGH",
        ),
    ],
    generation_config={
        "temperature": 0.2,
        "top_p": 0.9,
        "max_output_tokens": 2000,
    }
)

chat = model.start_chat()

# Use same conversation flow
response = chat.send_message("I want to build: [feature]")
print(response.text)
```

---

## Best Practices: Gemini

### ✅ DO

- **Load full governance context upfront** (1M token advantage)
- **Use Gemini 2.0 Flash for speed** (90% of tasks)
- **Ask for spec explicitly** ("Before code, ask clarifying questions then write spec")
- **Use temperature=0.2** (focused, consistent)
- **Checkpoint every 5-10 messages** ("Confirm we're still on track with these principles...")
- **Let Gemini refuse violations** (it's better at this than GPT-4)
- **Use Vertex AI for enterprise** (monitoring, audit logs, better governance)

### ❌ DON'T

- **Start a new conversation for each task** (load full context once, reuse)
- **Use default temperature** (too creative for architecture)
- **Assume Gemini remembers all rules** (reinforce every 5-10 messages)
- **Mix governance principles across tasks** (be consistent)
- **Ignore the 1M token advantage** (use it to load full codebase)

---

## Complete Example: Gemini Conversation

**Message 1: Load Context**
```
[Paste all governance files]

"Use these principles for the entire conversation. Temperature=0.2, focused responses only.
Ready to build a feature."
```

**Message 2: Feature Request**
```
"I want to build: users can save favorite products"
```

**Gemini:** [Asks clarifying questions]

**Message 3: Clarifications**
```
"[Your answers to the questions]"
```

**Gemini:** [Writes spec]

**Message 4: Approval**
```
"Spec approved. Now the architecture plan with Governance Gate check."
```

**Gemini:** [Architecture plan + all 8 gates]

**Message 5: Approval**
```
"Plan approved. Decompose into tasks."
```

**Gemini:** [6 tasks]

**Messages 6-11: Implementation**
```
"Task 1: [description]"
Gemini: [Code + report]

"Task 2: ..."
...etc
```

**Final Message: Verification**
```
"All tasks complete. Run DoD checklist."
```

**Gemini:** [Full verification report]

**Total:** 12-15 messages for a complete, verified feature.

---

## Summary

| Aspect | Gemini 2.0 Flash | Gemini 1.5 Pro | Verdict |
|--------|-----------------|----------------|---------|
| Architecture | ✅ Excellent | ✅ Excellent | Both good |
| Code quality | ✅ Excellent | ✅ Excellent | Both excellent |
| Governance adherence | ✅ Good with instruction | ✅ Better | Gemini better than GPT-4 |
| Long context | ✅ 1M tokens | ✅ 1M tokens | Major advantage |
| Cost | ✅ Cheapest | ✅ Very cheap | 30-50x cheaper than GPT-4 |
| Ease of setup | ✅ Same as OpenAI | ✅ Same | Easy |

**Recommendation:** **Use Gemini.** It's cheaper, refuses violations better, and has massive context. For this project, Gemini 1.5 Pro is the sweet spot (cost + quality + governance).
