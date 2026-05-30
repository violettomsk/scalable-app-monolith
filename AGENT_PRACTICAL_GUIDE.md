# Practical Guide: Using This Project with Any AI Tool

> How to work with Claude, ChatGPT, Gemini, Copilot, or any LLM while following this project's principles

---

## Quick Start: Any AI Tool

### 1. Before You Start

Copy this context into your AI tool's message:

```
You are working on a scalable app architecture project.
Key files:
- constitution.md — immutable governance rules
- AGENTS.md — how to apply the constitution
- scaling-roadmap.md — when to scale
- CLAUDE.md — development commands and architecture
- AGENT_OPERATING_PRINCIPLES.md — these principles

Rules you MUST follow:
1. Spec-first: No code before a spec and plan exist.
2. Inside-out: domain → application → adapters
3. Test-first: behavior specified before implementation
4. Single deployable: no microservices unless scaling-roadmap trigger is met
5. Stateless instances: all state in Redis
6. If a request breaks these rules, refuse it and propose a compliant alternative.

Stack: Java 25 + Spring Boot 4.0 (backend), Angular 21 + Tailwind (web), Flutter 3.44 (mobile).
```

### 2. Conversation Structure

**Start with `/specify`:**
```
User: "I need users to be able to save favorites."

You (AI): 
  "Before coding, let me clarify:
   - Is this a per-user or per-session feature?
   - Do favorites persist across devices?
   - Do we need real-time sync?
   
   Here's the spec I'm proposing:
   [Write spec with scenarios, not tech detail]"

User approves spec.
```

**Then `/plan`:**
```
You (AI):
  "Architecture decision:
   - Bounded context: 'favorites' (new)
   - Domain: Favorite entity, FavoriteRepository port
   - Application: SaveFavoriteUseCase, RemoveFavoriteUseCase
   - Adapters: FavoritesRestController, FavoriteJpaRepository
   
   Architecture Gate:
   ✅ All dependencies point inward
   ✅ Domain is framework-free
   ✅ Lives in one context
   ✅ Still single deployable
   ✅ Stateless app tier
   ✅ Migration needed for schema
   ✅ Auth via OAuth2
   ✅ DTOs separate from entities
   
   Proceed? [Yes/No]"

User confirms.
```

**Then `/tasks`:**
```
Task 1: Domain layer - Favorite entity, value objects, domain rules
Task 2: Application layer - SaveFavoriteUseCase, RemoveFavoriteUseCase ports
Task 3: Adapter (in) - FavoritesRestController + DTOs
Task 4: Adapter (out) - FavoriteJpaRepository + migration
Task 5: Integration tests + migration testing
Task 6: Observability - logs, metrics, trace IDs
```

**Then implement + verify DoD.**

---

## Tool-Specific Examples

### Claude Code (Recommended)

**Advantages:**
- Agent teams coordinate multiple specialists
- Shared task list with auto-coordination
- Integrated with CLAUDE.md, Spec-Kit
- `/specify`, `/plan`, `/tasks` commands

**Example workflow:**
```bash
# In Claude Code terminal
/specify

# Claude enters spec mode, returns a spec doc
# You approve or give feedback

/plan

# Claude creates a plan, runs Architecture Gate
# You confirm or ask for changes

/tasks

# Claude decomposes into tasks
# Tasks appear in shared list

# Claude Code spawns teammates or works alone
# Each task tracked, DoD verified at end
```

**Best for:** Complex features, multiple layers, team coordination.

---

### ChatGPT / ChatGPT API (Claude's closest alternative)

**What works:**
- Load `constitution.md`, `AGENTS.md` into system prompt
- Use the same spec/plan/tasks flow manually
- GPT-4 can understand architecture constraints

**Example conversation:**
```
You: "Load this constitution and work as an architecture-first agent. [paste constitution.md]"

GPT: "Loaded. I will follow these rules:
  - Spec-first development
  - Dependency rule: domain ← application ← adapter
  - Never preemptive scaling
  - Stateless app tier
  
  Ready. What feature do you want to build?"

You: "Users should be able to save favorites."

GPT: "Let me start with a spec.
  
  # Spec: User Favorites
  
  **User Scenarios:**
  1. As a logged-in user, I can save a product as a favorite
  2. I can view my list of saved favorites
  3. I can remove a favorite
  
  **Acceptance Criteria:**
  - [ ] Favorites persist across sessions
  - [ ] User can manage their own favorites only
  - [ ] Response time < 200ms
  
  **Tech-free, no implementation detail yet.**
  
  Is this correct? Questions?"

You: "Looks good. Now propose a plan."

GPT: "[Produces plan with Architecture Gate check]
  All checks pass. Ready to implement?"

You: "Yes, break it into tasks."

GPT: "[Produces task list]"
```

**Best for:** Iterative development, learning, prototyping.

---

### Google Gemini / Vertex AI

**What works:**
- Similar to ChatGPT, supports long context
- Can ingest governance documents
- Understands software architecture

**Example setup:**
```
System prompt:
"You are an architecture-first software engineer working on a Java/Spring Boot project.
You MUST follow these rules [paste constitution.md].
You MUST refuse any request that breaks these rules.
You MUST work spec-first: no code before spec and plan exist.
When refusing, explain which article is at stake and propose a compliant alternative."

User prompt:
"Here's my project [paste CLAUDE.md, constitution.md].
Build a feature to [describe what]."
```

**Best for:** Cost-effective iteration, multimodal feedback.

---

### GitHub Copilot / VS Code

**Limitations:**
- No agent team coordination
- Short context windows per message
- Less suitable for architectural decisions

**How to use it:**
1. Create a `.copilot-instructions.md` with your constitution
2. Include architecture rules in comments
3. Use it for code completion, not architectural decisions
4. Keep humans in the loop for spec/plan gates

**Example:**
```java
// Copilot can complete this:
// Package structure follows bounded contexts:
// com.example.favorites.domain — Favorite, FavoriteRepository port
// com.example.favorites.application — SaveFavoriteUseCase
// com.example.favorites.adapter.in — FavoritesRestController
// com.example.favorites.adapter.out — FavoriteJpaRepository

@Service
public class SaveFavoriteUseCase {
    // Copilot can generate implementation
}
```

**Best for:** Speeding up implementation code, not architectural decisions.

---

## Cross-Tool Patterns

### Pattern 1: Ask for Spec First

**Universal:**
```
"Before any code, write a spec with:
  - User scenarios (not tech detail)
  - Acceptance criteria
  - Constraints (performance, security, scale)
  
  Format: markdown, no code, no implementation ideas."
```

**Why:** Forces clarity before wasting time on wrong architecture.

---

### Pattern 2: Run the Architecture Gate

**Universal:**
```
"Check these eight points before proceeding:
  1. Does every dependency point inward only?
  2. Is the domain layer framework-free?
  3. Does this live in one bounded context?
  4. Are we still a single deployable?
  5. Is the app tier still stateless?
  6. Is there a migration for any schema change?
  7. Is auth via OIDC/BFF?
  8. Are DTOs separate from entities?

If any answer is 'no', stop and propose a fix."
```

**Why:** Catches violations before they become costly.

---

### Pattern 3: Report Work Per Layer

**Universal:**
```
"When you finish, report:
  - Domain layer: [entities, value objects, rules, unit tests]
  - Application layer: [use cases, ports, tests]
  - Adapter (in): [controllers, DTOs]
  - Adapter (out): [repositories, migrations]
  - Observability: [logs, metrics, traces]
  - DoD: [checklist results]"
```

**Why:** Forces visibility, prevents hidden complexity.

---

### Pattern 4: Refuse Violations Gracefully

**Universal:**
```
"If the request breaks a rule:
  1. Say 'I can't do that because [Article X, Rule Y].'
  2. Explain the cost: 'This blocks [consequence].'
  3. Propose an alternative: 'Instead, [compliant solution].'
  4. Wait for confirmation: 'Proceed only if this is acceptable.'"
```

**Why:** Protects architecture instead of eroding it.

---

## Real-World Examples

### Example 1: "Add Caching"

**Bad approach (any tool):**
```
You: "Add caching to speed up product queries."

AI: [Adds Redis cache everywhere, no thought]
```

**Good approach:**
```
You: "Product queries are slow (see metrics). What's the bottleneck?
      Before adding cache, let's measure:
      - Query latency now?
      - Hit rate if cached?
      - Cache invalidation strategy?"

AI: "The query joins 5 tables. Latency is 500ms. 
     A read replica would fix this better than cache.
     
     But Article IV § 3 says all reads must be routable to read replicas.
     Let's refactor the query first, then add a replica.
     
     Step 1: Spec the read model.
     Step 2: Plan the read replica rollout (check scaling-roadmap).
     Step 3: Implement."

You: "Good. Let's start with the spec."
```

**Why:** Prevents premature scaling, follows the roadmap.

---

### Example 2: "Share a Table"

**Bad approach:**
```
You: "Users in the auth context and favorites context both need user_id.
      Let's share the users table."

AI: [Implements shared table]
```

**Good approach:**
```
You: "Users in auth and favorites both need user_id. 
      What's the cleanest way?"

AI: "Sharing a table violates Article I § 4 (cross-context integrity) 
     and Article IV (data isolation).
     
     Compliant alternative:
     - Auth context owns users table (public interface: user_id, username, email)
     - Favorites context owns favorites table with user_id (no FK to auth)
     - On CreateUser event, favorites context caches the user_id
     - Favorites only reads what it cached, never joins to auth
     
     This preserves isolation and enables read replicas + microservice extraction.
     Shall we proceed?"

You: "Yes, start with the spec."
```

**Why:** Maintains architecture, unblocks future scaling.

---

### Example 3: "No Time for Tests"

**Bad approach:**
```
You: "We're under time pressure. Can we ship without tests?"

AI: "Sure, I'll ship untested code."
```

**Good approach:**
```
You: "We're under time pressure. Can we ship without tests?"

AI: "No. Article VII § 1: 'Behavior MUST be specified by tests.'
     This isn't optional—ArchUnit will fail the build otherwise.
     
     Quick alternative: fast unit tests for domain, 
     mocked integration tests for adapters.
     This is actually faster than writing bad code and debugging later.
     
     Let me show you the minimal test suite for this feature."

You: "OK, let's do it right."
```

**Why:** Tests are cheaper than debugging in production.

---

## Checklist: Preparing Any AI Tool

- [ ] Copy `constitution.md` into system prompt / context
- [ ] Copy `AGENTS.md` into system prompt / context
- [ ] Explain the spec/plan/tasks/implement/verify loop
- [ ] Show the Architecture Gate checklist
- [ ] Explain how to refuse violations (name article, explain cost, offer alternative)
- [ ] Give examples of good vs bad patterns (see above)
- [ ] Set expectations: "No code before spec and plan exist"
- [ ] Point to stack-specific patterns (Spring Boot, Angular, Flutter)
- [ ] Show how to report work (per layer, with DoD verification)

---

## Summary

**With Claude Code:**
- Use agent teams for coordination
- Use `/specify`, `/plan`, `/tasks` commands
- Leverage shared task lists

**With ChatGPT, Gemini, Copilot:**
- Load governance into system prompt
- Use the same spec/plan/tasks flow manually
- Keep humans in the loop for architecture gate
- Report work per layer with DoD verification

**Universal (all tools):**
- Spec-first, never code before spec
- Run Architecture Gate before coding
- Test-first, inside-out build order
- Refuse violations gracefully
- Report work per layer

**The tool doesn't matter.** The principles do. Follow them, and any AI—Claude, GPT, Gemini—will produce reliable, scalable code.
