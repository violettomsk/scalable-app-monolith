# Agent Team Coordination Guide

> How to spawn and manage multiple Claude agents working together on a feature

---

## Prerequisites

✅ Agent teams enabled: `CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=1` in settings.json  
✅ Claude Code installed: v2.1.32 or later  
✅ Spec + plan approved for the feature  
✅ Tasks decomposed into independent units  

Check version:
```bash
claude --version
```

---

## Quick Start: Launch a Team (3 Steps)

### Step 1: Create a Team

In Claude Code, type:

```
Create an agent team to implement the Flutter login feature.

Spawn 3 teammates:
- "architect": Plans architecture, ensures governance
- "backend-dev": Implements domain + application layers  
- "frontend-dev": Implements presentation + tests

Team name: flutter-auth
Task: Implement OAuth2 login in Flutter following spec-driven workflow
```

### Step 2: Assign Work

Claude will create the team and ask what each agent should work on. Tell it:

```
architect: 
  - Review spec + plan
  - Run governance gate (8-point checklist)
  - Create task list (5-6 independent tasks)
  - Monitor for architecture violations

backend-dev:
  - Task 1: Domain layer (User, AuthToken, AuthRepository)
  - Task 2: Application layer (AuthService, ports)
  - Task 3: Data layer (KeycloakAuthApi, secure storage)

frontend-dev:
  - Task 4: Presentation layer (LoginPage, Riverpod state)
  - Task 5: App navigation (AuthGate, restore state on cold start)
  - Task 6: Tests + verification
```

### Step 3: Monitor Progress

In Claude Code, use:

```
Shift+Down     # Cycle through teammates
Ctrl+T         # Toggle task list
Shift+Down     # Message a specific teammate
```

---

## Full Team Setup: By-Role Guide

### Role 1: Architect (Governance Lead)

**Responsibility:** Ensure architecture stays clean, all governance rules followed

**Initial Prompt:**

```
You are the architecture lead for this team.

RESPONSIBILITIES:
1. Review the approved spec: [paste spec]
2. Review the proposed plan: [paste plan]
3. Run the Governance Gate (8-point checklist)
4. Verify all checks pass
5. Create a task list from the plan (5-6 independent tasks)
6. Monitor all team work for architecture violations
7. Refuse any task that breaks governance rules

If governance gate fails on ANY point:
- Stop the team
- Explain which article is violated
- Propose a fix
- Wait for approval before proceeding

Your job is to protect the architecture.
Ready?
```

**What They Do:**
- Verifies spec/plan before coding starts
- Creates the task breakdown
- Reviews each teammate's work against governance
- Escalates violations immediately
- Manages task dependencies

**Success Metric:** All 8 governance checks pass, no violations

---

### Role 2: Backend Developer

**Responsibility:** Implement domain + application layers (framework-free + test-first)

**Initial Prompt:**

```
You are the backend developer for the Flutter login feature.

RESPONSIBILITIES:
1. Implement Task 1: Domain layer
   - User value object (immutable)
   - AuthToken value object (immutable, with expiry checks)
   - AuthRepository interface (port)
   - Unit tests (JUnit 5 or Dart testing, zero framework imports)

2. Implement Task 2: Application layer
   - AuthService (orchestrates login, logout, refresh)
   - SecureStorageService (token persistence interface)
   - Ports that data layer implements
   - Unit tests with mocked repositories

3. Implement Task 3: Data layer
   - KeycloakAuthApi (Dio HTTP calls, Auth Code + PKCE)
   - AuthRepositoryImpl (implements port)
   - SecureTokenStorage (FlutterSecureStorage wrapper)
   - Integration tests with mock Keycloak

BUILD ORDER: Domain → Application → Data
TEST-FIRST: Write tests before implementation
FRAMEWORK-FREE: Domain layer imports ZERO Flutter/Dio/packages

After each task, report:
- Tests passing: ✅
- Code follows inside-out order: ✅
- Architecture maintained: ✅
- No governance violations: ✅

Ready?
```

**What They Do:**
- Builds domain logic (business rules, no framework)
- Builds application layer (use cases, ports/interfaces)
- Builds data layer (API calls, persistence)
- Writes tests before code
- Ensures domain layer stays pure

**Success Metric:** All tests pass, domain is framework-free

---

### Role 3: Frontend Developer

**Responsibility:** Implement presentation layer + tests + verification

**Initial Prompt:**

```
You are the frontend developer for the Flutter login feature.

RESPONSIBILITIES:
1. Implement Task 4: Presentation layer
   - LoginPage (Flutter UI with login button)
   - LoginProvider (Riverpod StateNotifier for auth state)
   - Handle loading, errors, success states
   - Call AuthService through providers (not direct API)

2. Implement Task 5: App navigation + state restoration
   - AuthGate (checks if user is authenticated)
   - Update main.dart for conditional routing
   - Restore login state on cold start (check SecureStorage)

3. Implement Task 6: Testing + Definition of Done
   - Unit tests for Riverpod state (LoginProvider)
   - Integration tests for login flow end-to-end
   - Run full Definition of Done checklist
   - Verify all 12 DoD items are green

CONSTRAINT: Call AuthService only through Riverpod providers.
Never call Keycloak API directly from UI.

After each task, report:
- Tests passing: ✅
- UI handles all states: ✅
- State restored on cold start: ✅
- Definition of Done: [list ✅ items]

Ready?
```

**What They Do:**
- Builds Flutter UI (login page)
- Manages state with Riverpod
- Handles loading/error states
- Restores authentication state on app restart
- Writes tests for state management
- Runs final verification

**Success Metric:** UI works, all DoD checks pass, feature is done

---

## Team Coordination: Full Conversation Flow

### Message 1: Create Team & Assign Roles

```
Create an agent team to implement the Flutter login feature.

Team name: flutter-auth
Deadline: 3 hours (2-3 hours per estimate)

Spawn 3 teammates:
- architect (reviews governance, manages tasks)
- backend-dev (domain + application + data layers)
- frontend-dev (presentation + tests + verification)

Key resources:
- Spec: [paste spec from Phase 1]
- Plan: [paste plan from Phase 2]
- Task list: [paste tasks from Phase 3]
- Implementation guide: IMPLEMENTATION_EXAMPLE_FLUTTER_LOGIN.md
- Architecture rules: constitution.md + AGENTS.md

All must follow:
1. Spec-first workflow (already done)
2. Governance gate (8-point checklist)
3. Test-first, inside-out
4. Domain framework-free
5. Refuse violations

Ready to spawn team?
```

### Message 2: Task Assignment & Kickoff

```
architect:
  1. Verify spec + plan are sound
  2. Run governance gate (all 8 checks)
  3. Confirm tasks are properly decomposed
  4. Monitor team for violations
  Proceed when gate passes.

backend-dev:
  1. Start with Task 1: Domain layer
     - User, AuthToken value objects
     - AuthRepository interface
     - Unit tests (zero framework imports)
  2. Then Task 2: Application layer
  3. Then Task 3: Data layer
  Build inside-out, test-first.
  Estimate: 60-90 minutes

frontend-dev:
  Wait for backend-dev to finish Task 3, then:
  1. Task 4: LoginPage + LoginProvider (Riverpod)
  2. Task 5: AuthGate + main.dart
  3. Task 6: Tests + Definition of Done
  Estimate: 60-90 minutes

Team: Start now. Report progress every 30 minutes.
Who starts first?
```

### Message 3: Progress Check (30 min)

```
Progress check at 30 minutes:

architect:
  - Governance gate status: ✅ or ❌?
  - Any architecture violations detected?
  - Task list confirmed?

backend-dev:
  - Task 1 progress: [% done]
  - Tests passing: ✅ or ❌?
  - Any blockers?

frontend-dev:
  - Status: [waiting for backend / ready to start / in progress]
  - Blockers: [none / specific blocker]

Report in 2-3 sentences each. Keep going.
```

### Message 4: Final Verification (2 hours)

```
All tasks should be complete or nearly done.

backend-dev:
  Show me: Tasks 1-3 all passing tests

frontend-dev:
  Show me: Tasks 4-6 complete + Definition of Done report

architect:
  Final governance check:
  - All 8 architecture checks: ✅
  - No violations: ✅
  - Ready to merge: ✅

If anything is red (❌), fix now.
If all green (✅), report: READY TO MERGE
```

### Message 5: Completion & Handoff

```
Feature complete?

If YES:
  1. Merge all changes
  2. Push to git
  3. Team can stand down
  
  Next phase: Integration with backend
  (Separate team or same team?)

If NO:
  What's blocking?
  - Architecture issue?
  - Blocker between tasks?
  - Unclear requirements?
  - Technical blocker?
  
  Fix now and report back.
```

---

## During Development: Communication Patterns

### Architecture Violation Detected

**Architect to backend-dev:**
```
I found a violation in Task 2:

File: auth_service.dart
Line: 45
Issue: Imports 'package:flutter_secure_storage'

This violates Article I § 2 (domain framework-free).
SecureStorage should be injected as a port/interface.

Fix: Move SecureStorage import to data layer.
AuthService should receive it as a constructor parameter.

Show me the fix.
```

### Task Dependency Issue

**Frontend-dev to backend-dev:**
```
I'm blocked on Task 4 (presentation layer).

I need:
- AuthService interface (from Task 2)
- Keycloak API configuration (constants)

Are Tasks 1-3 done? Can you show me the AuthService interface?
I'll start LoginProvider while you finish Task 3.
```

### State Coordination

**Any teammate to architect:**
```
I'm creating a new decision point in the architecture:

Question: Should token refresh happen automatically or on-demand?

Options:
A. Auto-refresh when token expires soon (in background)
B. Refresh on-demand when making API calls
C. Prompt user to login when token expires

Recommendation: Option A (cleaner UX)

Does this fit the architecture? Proceed?
```

---

## Managing the Task Board

### View Shared Task List

In Claude Code:
```
Ctrl+T    # Toggle task list panel
```

You'll see:
```
Task 1: Domain layer (backend-dev)         [IN_PROGRESS]
Task 2: Application layer (backend-dev)    [PENDING]
Task 3: Data layer (backend-dev)           [PENDING]
Task 4: Presentation (frontend-dev)        [BLOCKED_BY: Task 3]
Task 5: Navigation (frontend-dev)          [BLOCKED_BY: Task 4]
Task 6: Tests & DoD (frontend-dev)         [BLOCKED_BY: Task 5]
```

### Update Task Status

**Architect (or any teammate):**
```
Task 1 is complete. Mark it done.
This should unblock Task 2 (application layer).
```

Or manually via TaskUpdate (if using integration):
```
TaskUpdate(taskId="1", status="completed")
```

---

## Handling Issues During Development

### If a Task Gets Stuck

**What to do:**
```
Task [N] is stuck on [specific problem].

Team: 
- Architect: Is this a design issue or implementation issue?
- Other devs: Can you help unblock?
- If design issue: Stop and redesign
- If implementation issue: Debug together

Don't work around architecture—fix the design.
```

### If Tests Are Failing

**What to do:**
```
Tests failing in [file]:
- Error: [specific test failure]

Did we violate test-first principle?
- Should we have written tests before implementation?
- Fix: Re-write tests first, then implementation

Show me the fixed tests + code.
```

### If Governance Gate Fails

**What to do:**
```
Governance Gate check [N] failed:
- Check: [what failed]
- Violated article: [Article X § Y]
- Cost: [consequence of violation]

Fix:
1. Redesign the feature
2. Re-run governance gate
3. Proceed only if all 8 pass

Do NOT proceed with violated architecture.
```

---

## Monitoring Strategies

### Quick Check Every 30 Minutes

```
Team status:
- architect: [Governance gate status + any issues]
- backend-dev: [Task progress + blockers]
- frontend-dev: [Task progress + blockers]

Blockers: [List of any issues]
On track: ✅ or ❌?

Keep going.
```

### Mid-Point Assessment (1 Hour)

```
We're halfway through the 2-3 hour estimate.

Progress report:
- backend-dev: Should be finishing Task 2 or starting Task 3
- frontend-dev: Should be ready to start Task 4
- architect: Should have identified any issues by now

If behind: What's blocking? Fix now.
If on track: Keep momentum.
```

### Final Check (2 Hours)

```
We're at the 2-hour mark.

Status:
- backend-dev: Tasks 1-3 all done + tests passing?
- frontend-dev: Tasks 4-6 complete?
- architect: Definition of Done all green?

If done: Merge and push to git
If not: What's left? How much longer?
```

---

## Team Modes: Display & Interaction

### In-Process Mode (Default)

```
# Terminal shows lead + teammates sequentially
Shift+Down      # Cycle to next teammate
Type message    # Send to current teammate
Escape          # Interrupt current teammate
Ctrl+T          # Show task list

# One terminal, multiple agents visible in history
```

### Split-Pane Mode (Recommended for Teams)

```
# Requires: tmux or iTerm2

4-pane layout:
┌─────────────┬─────────────┐
│  Architect  │ Backend-Dev │
├─────────────┼─────────────┤
│ Frontend-Dev│  Shared Tasks│
└─────────────┴─────────────┘

Click pane → type → message that teammate
Ctrl+T → toggle task list overlay

# Can watch all 3 agents work simultaneously
```

**To enable split panes:**
```bash
claude --teammate-mode tmux

# Requires: tmux installed (brew install tmux)
```

---

## End-of-Sprint Checklist

When all tasks are complete:

```
Final verification:

✅ Spec approved (Phase 1)
✅ Plan approved + governance gate passed (Phase 2)
✅ Tasks decomposed (Phase 3)
✅ All tasks implemented + tests passing (Phase 4)
✅ Definition of Done all green (Phase 5)
✅ Code builds without errors
✅ No architecture violations (ArchUnit green if backend)
✅ All tests pass (unit + integration)
✅ Code reviewed for quality
✅ Ready to merge to git
✅ Git commit + push done
✅ Features isolated (no cross-context coupling)

If all ✅ → Feature is DONE and production-ready

If any ❌ → Fix before shipping
```

---

## Example: Real Team Conversation

### Message 1: Kickoff
```
You: "Create a team to build the Flutter login feature.
      Spawn architect, backend-dev, frontend-dev.
      Use spec: [spec]
      Use plan: [plan]
      Use tasks: [tasks]
      
      Architect reviews governance, backend-dev implements layers, 
      frontend-dev implements UI + tests.
      
      Start now."

Claude: [Creates team, assigns agents]
        "Team 'flutter-auth' created.
        - architect: Ready to review spec
        - backend-dev: Ready to implement domain
        - frontend-dev: Waiting for backend-dev
        
        Architect starting review..."
```

### Message 2: Progress Report (30 min)
```
You: "Progress report. Are we on track?"

Claude: "Architect: ✅ Governance gate passed (8/8 checks)
                    Tasks decomposed correctly
                    No violations yet
        
        Backend-dev: Task 1 (domain) 80% done
                    Unit tests for User and AuthToken: ✅
                    Waiting to finish AuthRepository interface
        
        Frontend-dev: ⏳ Waiting for Task 3 completion
                     Starting LoginPage design
        
        Status: ON TRACK (within estimate)"
```

### Message 3: Completion (2 hours)
```
You: "Final status?"

Claude: "✅ ALL TASKS COMPLETE
        
        Backend-dev: ✅ Tasks 1-3 done
                    12 unit tests passing
                    5 integration tests passing
                    Zero framework imports in domain
        
        Frontend-dev: ✅ Tasks 4-6 done
                     LoginPage built
                     Riverpod state management working
                     All 12 DoD items green
        
        Architect: ✅ Final governance check passed
                  ✅ Ready to merge
        
        Estimated build time: 2h 15min
        All tests: ✅ PASSING
        Architecture: ✅ CLEAN
        
        READY FOR GIT PUSH"
```

### Message 4: Merge & Deploy
```
You: "Push to git and stand down team."

Claude: "[Pushes code, creates commit]
        
        Commit: ee38f90
        Branch: master
        Files: 15 files changed, 1200+ insertions
        
        ✅ PUSHED TO GIT
        ✅ TEAM WORK COMPLETE
        
        Next step: Integrate with backend (new team or same team?)"
```

---

## Quick Reference: Team Commands

| Action | Command |
|--------|---------|
| **Create team** | "Create agent team to [task]. Spawn architect, backend-dev, frontend-dev." |
| **Assign work** | "[Agent name]: Do [task 1], then [task 2]." |
| **Check progress** | "Progress report. Status?" |
| **Unblock task** | "[Agent] is blocked on [issue]. Help unblock." |
| **View tasks** | Ctrl+T (toggle task list) |
| **Switch agents** | Shift+Down (cycle through teammates) |
| **Message agent** | Type message → Enter (sends to current agent) |
| **Stop agent** | "Ask [agent] to shut down" |
| **Complete** | "All done? Push to git." |

---

## Success Metrics: How You Know It Worked

✅ **Spec-first:** Spec approved before any coding  
✅ **Architecture:** All 8 governance checks passed  
✅ **Test-first:** Tests written before code  
✅ **Inside-out:** Domain → Application → Adapters  
✅ **Framework-free:** Domain layer has zero framework imports  
✅ **Isolated:** Feature doesn't couple to other contexts  
✅ **Tests:** All unit + integration tests passing  
✅ **DoD:** All 12 Definition of Done items green  
✅ **Performance:** Completed in 2-3 hours  
✅ **Quality:** Code is production-ready  

If all above ✅ → **Feature is DONE and can be shipped**

---

## Troubleshooting

### Team Won't Start
```
Error: "Agents are not responding"

Fix: Check CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS=1 in settings.json
     Check Claude Code version >= 2.1.32
     Restart Claude Code
     Try again
```

### Agents Go Idle
```
Normal behavior: Agents go idle between messages

Fix: Just send them a message (via Shift+Down or direct message)
     They'll resume immediately
     This is expected and OK
```

### Task Dependencies Not Resolving
```
Issue: Task 4 is "BLOCKED_BY Task 3" but Task 3 is done

Fix: Tell the architect to manually unblock:
     "Task 3 is done. Unblock Task 4."
     Or update via TaskUpdate(taskId="4", status="pending")
```

### Architecture Violation Mid-Development
```
Issue: Backend-dev added framework import to domain

Fix: Architect should catch and escalate:
     "Task [N] violates Article I § 2.
      Remove [framework] import and move to adapter layer.
      Show me the fix."
     
     Don't let them proceed with violation.
```

---

## Summary: Full Team Workflow

1. **Prep** (10 min): Spec approved, plan approved, tasks decomposed
2. **Spawn team** (1 min): 3 roles assigned
3. **Execution** (120-180 min): Parallel work on independent tasks
4. **Monitoring** (continuous): Status checks, unblocking issues
5. **Verification** (10 min): Definition of Done checklist
6. **Completion** (2 min): Push to git, team stands down

**Total:** 2-3 hours from start to mergeable code

This is the power of spec-driven, architecture-first development with coordinated teams.

---

## Next: Multi-Team Coordination

Once you have multiple teams:
- **Team 1 (Flutter):** Mobile app features
- **Team 2 (Spring Boot):** Backend context features
- **Team 3 (Angular):** Web app features

Coordinate via:
- Shared task list
- Inter-team messaging
- Governance gate (all teams follow same architecture)
- Event publishing (cross-team communication)

All teams can work in parallel as long as they:
1. Follow spec-first workflow
2. Pass governance gate
3. Don't couple across bounded contexts (use events instead)

Teams can work for weeks in parallel with zero merge conflicts if they maintain architecture isolation.
