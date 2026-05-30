# Agent Team Permissions Guide

> Configure which tools agents can use without permission prompts

---

## Quick Answer: Permissions to Grant

For a **development team** to work efficiently, add this to `.claude/settings.json`:

```json
{
  "permissions": {
    "defaultMode": "acceptEdits",
    "allow": [
      "Read",
      "Edit",
      "Write",
      "Glob",
      "Grep",
      "Bash(cd *)",
      "Bash(git *)",
      "Bash(flutter *)",
      "Bash(npm *)",
      "Bash(./gradlew *)",
      "Bash(javac *)",
      "Bash(dart *)",
      "Bash(ls *)",
      "Bash(mkdir *)",
      "Bash(cat *)",
      "Bash(echo *)",
      "Bash(find *)",
      "Bash(curl *)",
      "Bash(ps *)",
      "Bash(kill *)"
    ]
  }
}
```

This allows agents to:
- ✅ Read any file
- ✅ Edit any file (with warning)
- ✅ Write new files
- ✅ Run development commands (git, gradle, flutter, npm, etc.)
- ❌ Dangerous operations (force push, rm -rf, etc.) still blocked

---

## Why Permissions Matter for Teams

### Without Permissions Setup

```
Agent tries to edit a file
↓
Claude Code prompts: "Allow Edit(src/main/java/...App.java)?" 
↓
Agent pauses, waiting for approval
↓
Every file edit = 1-2 second delay
↓
Feature that should take 2 hours takes 3+ hours
```

**Problem:** Permission prompts block parallel team work

### With Permissions Setup

```
Agent edits a file
↓
Matches "Edit" rule → Allowed automatically
↓
Agent continues without pausing
↓
Parallel work flows smoothly
↓
Feature done in 2 hours as planned
```

**Solution:** Pre-approve safe operations

---

## Permission Configuration by Role

### Architect (Governance Lead)

```json
{
  "permissions": {
    "allow": [
      "Read",              // Read any file (spec, plan, code)
      "Glob",              // Find files by pattern
      "Grep",              // Search code
      "Bash(git log *)",   // Check commit history
      "Bash(git status)",  // Check git status
      "Bash(find *)"       // Find files
    ],
    "deny": [
      "Edit",              // Don't edit code (governance role only)
      "Write",             // Don't write code
      "Bash(rm *)",        // Don't delete
      "Bash(git push *)",  // Don't push yet
      "Bash(git reset *)"  // Don't reset
    ]
  }
}
```

**Architect job:** Review, verify, protect architecture (read-only)

### Backend Developer

```json
{
  "permissions": {
    "allow": [
      "Read",
      "Edit",
      "Write",
      "Glob",
      "Grep",
      "Bash(cd *)",
      "Bash(git *)",
      "Bash(./gradlew *)",     // Gradle build
      "Bash(javac *)",         // Java compiler
      "Bash(mkdir *)",
      "Bash(cat *)",
      "Bash(find *)",
      "Bash(curl *)",
      "Bash(ls *)",
      "Bash(echo *)"
    ],
    "deny": [
      "Bash(rm -rf *)",        // Prevent accidental deletes
      "Bash(git push *)",      // No pushing yet
      "Bash(git reset --hard)" // Prevent data loss
    ]
  }
}
```

**Backend job:** Write code, run tests, commit locally

### Frontend Developer

```json
{
  "permissions": {
    "allow": [
      "Read",
      "Edit",
      "Write",
      "Glob",
      "Grep",
      "Bash(cd *)",
      "Bash(git *)",
      "Bash(npm *)",           // npm commands
      "Bash(flutter *)",       // Flutter/Dart
      "Bash(dart *)",
      "Bash(mkdir *)",
      "Bash(cat *)",
      "Bash(find *)",
      "Bash(curl *)",
      "Bash(ls *)",
      "Bash(echo *)"
    ],
    "deny": [
      "Bash(rm -rf *)",
      "Bash(git push *)",
      "Bash(git reset --hard)"
    ]
  }
}
```

**Frontend job:** Write UI code, run tests, commit locally

---

## Full Permission Settings (All Agents)

Use this in `.claude/settings.json` for your team:

```json
{
  "permissions": {
    "defaultMode": "acceptEdits",
    "allow": [
      "Read",
      "Edit",
      "Write",
      "Glob",
      "Grep",
      "Bash(cd *)",
      "Bash(git status)",
      "Bash(git log *)",
      "Bash(git add *)",
      "Bash(git commit *)",
      "Bash(git branch)",
      "Bash(git checkout *)",
      "Bash(git pull *)",
      "Bash(git diff *)",
      "Bash(git stash *)",
      "Bash(./gradlew *)",
      "Bash(gradle *)",
      "Bash(mvn *)",
      "Bash(npm *)",
      "Bash(yarn *)",
      "Bash(flutter *)",
      "Bash(dart *)",
      "Bash(java *)",
      "Bash(javac *)",
      "Bash(python *)",
      "Bash(pip *)",
      "Bash(ls *)",
      "Bash(pwd)",
      "Bash(mkdir *)",
      "Bash(mkdir -p *)",
      "Bash(cp *)",
      "Bash(cat *)",
      "Bash(echo *)",
      "Bash(find *)",
      "Bash(grep *)",
      "Bash(curl *)",
      "Bash(wget *)",
      "Bash(ps *)",
      "Bash(kill *)",
      "Bash(which *)",
      "Bash(touch *)",
      "Bash(chmod *)",
      "Bash(chown *)"
    ],
    "deny": [
      "Bash(rm -rf *)",
      "Bash(rm -f *)",
      "Bash(git push *)",
      "Bash(git reset --hard *)",
      "Bash(git clean -fd)",
      "Bash(sudo *)",
      "Bash(dd *)"
    ]
  }
}
```

---

## Permission Modes Explained

### Mode 1: `"defaultMode": "default"` (Most Restrictive)

```
Agents encounter ANY tool use → Prompt user for permission
Result: Constant interruptions, but maximum control
Use case: Security-sensitive work, untrusted code
```

### Mode 2: `"defaultMode": "plan"` (Moderate)

```
Agents must enter plan mode before making changes
They can propose edits, you approve the plan
Then they can execute within that plan
Result: You review architecture before coding, but no tool-level prompts
Use case: Complex features where review is critical
```

### Mode 3: `"defaultMode": "acceptEdits"` (Recommended for Teams)

```
Agents can edit/write files without prompts
But destructive operations still blocked
Result: Smooth development, with safety rails
Use case: Development teams, trusted agents
```

**Recommended:** `"acceptEdits"` + explicit `deny` list for dangerous ops

### Mode 4: `"defaultMode": "dontAsk"` (Least Restrictive)

```
Agents can use almost any tool without prompts
Only most dangerous operations (rm -rf, force push, etc.) blocked
Result: Maximum speed, but requires trust
Use case: Experienced teams, internal projects
```

---

## Safe Operations (Pre-Approve)

These are safe to allow without prompts:

| Operation | Permission Rule | Safe? |
|-----------|---|---|
| **Read files** | `Read` | ✅ Yes |
| **Edit files** | `Edit` | ✅ Yes (with defaultMode: acceptEdits) |
| **Write new files** | `Write` | ✅ Yes |
| **Find files** | `Glob`, `Grep` | ✅ Yes |
| **Git status** | `Bash(git status)` | ✅ Yes |
| **Git log** | `Bash(git log *)` | ✅ Yes |
| **Git add** | `Bash(git add *)` | ✅ Yes |
| **Git commit** | `Bash(git commit *)` | ✅ Yes |
| **Build project** | `Bash(./gradlew *)` | ✅ Yes |
| **Run tests** | `Bash(npm test)` | ✅ Yes |
| **Install deps** | `Bash(npm install)` | ✅ Yes |
| **List files** | `Bash(ls *)` | ✅ Yes |
| **Create dir** | `Bash(mkdir *)` | ✅ Yes |
| **Check process** | `Bash(ps *)` | ✅ Yes |

---

## Dangerous Operations (Always Block)

Never pre-approve these:

| Operation | Why Block | Permission |
|-----------|---|---|
| **Force delete** | Can't recover | `Bash(rm -rf *)` ❌ |
| **Force push** | Overwrites remote | `Bash(git push --force *)` ❌ |
| **Hard reset** | Loses commits | `Bash(git reset --hard *)` ❌ |
| **Clean -fd** | Deletes untracked files | `Bash(git clean -fd *)` ❌ |
| **Sudo** | Privilege escalation | `Bash(sudo *)` ❌ |
| **DD** | Block device tool (dangerous) | `Bash(dd *)` ❌ |

---

## Step-by-Step: Configure Permissions

### Step 1: Edit `.claude/settings.json`

```bash
# Location varies by OS:
# macOS/Linux: ~/.claude/settings.json
# Windows: C:\Users\[username]\.claude\settings.json
# Project-specific: .claude/settings.json (this one!)

nano .claude/settings.json
```

### Step 2: Add Permissions Block

Find the `permissions` section, update it:

```json
{
  "permissions": {
    "defaultMode": "acceptEdits",
    "allow": [
      "Read",
      "Edit",
      "Write",
      "Glob",
      "Grep",
      "Bash(cd *)",
      "Bash(git *)",
      "Bash(./gradlew *)",
      "Bash(npm *)",
      "Bash(flutter *)",
      "Bash(ls *)",
      "Bash(mkdir *)",
      "Bash(cat *)"
    ],
    "deny": [
      "Bash(rm -rf *)",
      "Bash(git push *)",
      "Bash(git reset --hard *)"
    ]
  }
}
```

### Step 3: Restart Claude Code

```bash
# Kill any running Claude Code processes
claude stop

# Restart
claude
```

### Step 4: Verify Permissions

Run a test command:
```
Edit a file (should work without prompt)
```

If it prompts, permissions aren't configured correctly. Check `.claude/settings.json` for syntax errors.

---

## Permission Scopes: User vs Project

### User Settings (`~/.claude/settings.json`)

```json
{
  "permissions": {
    "allow": ["Read", "Edit", "Bash(git *)"]
  }
}
```

**Applies to:** All projects globally  
**Use case:** Your personal preferences  
**Override:** Project settings override these

### Project Settings (`.claude/settings.json` in repo)

```json
{
  "permissions": {
    "allow": ["Read", "Edit", "Bash(npm *)", "Bash(./gradlew *)"]
  }
}
```

**Applies to:** Only this project  
**Use case:** Project-specific tools  
**Override:** Takes precedence over user settings  
**Git:** Commit this file (team gets same permissions)

---

## For Your Current Setup

Run this to update your project permissions:

```bash
cat > .claude/settings.json << 'EOF'
{
  "permissions": {
    "defaultMode": "acceptEdits",
    "allow": [
      "Read",
      "Edit",
      "Write",
      "Glob",
      "Grep",
      "Bash(cd *)",
      "Bash(git status)",
      "Bash(git log *)",
      "Bash(git add *)",
      "Bash(git commit *)",
      "Bash(git branch)",
      "Bash(git checkout *)",
      "Bash(git diff *)",
      "Bash(./gradlew *)",
      "Bash(npm *)",
      "Bash(flutter *)",
      "Bash(dart *)",
      "Bash(ls *)",
      "Bash(mkdir *)",
      "Bash(cp *)",
      "Bash(cat *)",
      "Bash(echo *)",
      "Bash(find *)",
      "Bash(curl *)",
      "Bash(ps *)",
      "Bash(which *)",
      "Bash(touch *)"
    ],
    "deny": [
      "Bash(rm -rf *)",
      "Bash(git push *)",
      "Bash(git reset --hard *)"
    ]
  },
  "env": {
    "CLAUDE_CODE_EXPERIMENTAL_AGENT_TEAMS": "1"
  },
  "model": "haiku",
  "teammate": {
    "defaultModel": "sonnet"
  }
}
EOF
```

Then restart Claude Code:
```bash
claude stop
claude
```

---

## Testing Permissions

### Test 1: Edit Should Work (No Prompt)

```
Edit any file
→ Should edit without asking
✅ If no prompt: permissions work
❌ If prompt: permissions not set
```

### Test 2: Read Should Work (No Prompt)

```
Read any file
→ Should read without asking
✅ If no prompt: permissions work
❌ If prompt: permissions not set
```

### Test 3: Git Commands Should Work (No Prompt)

```
Run: git status
→ Should execute without asking
✅ If no prompt: permissions work
❌ If prompt: permissions not set
```

### Test 4: Dangerous Commands Should Block (Prompt Required)

```
Run: git push origin master
→ Should still prompt (blocked by deny list)
✅ If prompt: safety rails work
❌ If no prompt: safety rails failed
```

---

## Permission Rules: Syntax

### Simple Rules (No Wildcards)

```json
"Bash(git status)"      // Exact: only this command
"Bash(ls)"              // Exact: only ls
"Read"                  // Any read operation
```

### Wildcard Rules

```json
"Bash(git *)"           // Any git subcommand
"Bash(npm *)"           // Any npm command
"Bash(./gradlew *)"     // Gradle with any task
"Bash(ls *)"            // ls with any arguments
"Bash(cd *)"            // cd to any directory
```

### Deny Rules (Block Specific)

```json
"Bash(rm -rf *)"        // Block force delete
"Bash(git push *)"      // Block any push
"Bash(sudo *)"          // Block privilege escalation
```

---

## Recommended Setup by Team Size

### Solo Developer

```json
{
  "defaultMode": "acceptEdits",
  "allow": ["Read", "Edit", "Write", "Glob", "Grep", "Bash(*)"],
  "deny": ["Bash(rm -rf *)", "Bash(git push --force *)"]
}
```

### Small Team (3-5 people)

```json
{
  "defaultMode": "acceptEdits",
  "allow": [
    "Read", "Edit", "Write", "Glob", "Grep",
    "Bash(git *)",
    "Bash(./gradlew *)",
    "Bash(npm *)",
    "Bash(flutter *)",
    "Bash(ls *)", "Bash(mkdir *)", "Bash(cat *)"
  ],
  "deny": [
    "Bash(rm -rf *)",
    "Bash(git push *)",
    "Bash(git reset --hard *)"
  ]
}
```

### Enterprise Team (10+ people)

```json
{
  "defaultMode": "plan",
  "allow": [
    "Read", "Edit", "Write", "Glob", "Grep",
    "Bash(git *)",
    "Bash(./gradlew *)",
    "Bash(npm *)",
    "Bash(flutter *)"
  ],
  "deny": [
    "Bash(rm -rf *)",
    "Bash(git push *)",
    "Bash(git reset --hard *)",
    "Bash(sudo *)"
  ],
  "additionalDirectories": ["/var/log"]
}
```

**Mode: `plan`** = Agents must propose architecture changes for review before coding

---

## Troubleshooting Permissions

### Issue: "Permission Denied" Even Though I Allowed It

**Check:**
1. Is `.claude/settings.json` valid JSON? (use `jq . .claude/settings.json`)
2. Did you restart Claude Code after editing?
3. Are you in the project directory? (project settings override user settings)

**Fix:**
```bash
# Verify JSON syntax
jq . .claude/settings.json

# Restart Claude Code
claude stop
claude
```

### Issue: Agents Keep Getting Prompts

**Check:**
- Are the permission rules correct?
- Did you use `"defaultMode": "acceptEdits"`?
- Are the `allow` rules specific enough?

**Fix:**
```bash
# Use less restrictive mode
"defaultMode": "acceptEdits"

# Add broader allow rules
"Bash(git *)"      # Instead of specific git commands
"Bash(npm *)"      # Instead of specific npm commands
```

### Issue: Dangerous Commands Not Blocked

**Check:**
- Are the `deny` rules present?
- Did you restart Claude Code?

**Fix:**
```json
"deny": [
  "Bash(rm -rf *)",
  "Bash(git push *)",
  "Bash(git reset --hard *)"
]
```

---

## Security Best Practices

✅ **DO:**
- ✅ Pre-approve read operations (safe)
- ✅ Pre-approve build commands (npm, gradle, flutter)
- ✅ Pre-approve git commands except push/force operations
- ✅ Block dangerous operations (rm -rf, force push, sudo)
- ✅ Review permissions quarterly
- ✅ Commit `.claude/settings.json` to git (team-wide)

❌ **DON'T:**
- ❌ Use `"defaultMode": "dontAsk"` in untrusted environments
- ❌ Allow `Bash(rm -rf *)`
- ❌ Allow `Bash(git push --force *)`
- ❌ Allow `Bash(sudo *)`
- ❌ Use `defaultMode: "bypassPermissions"`
- ❌ Skip the deny list

---

## Summary: Quick Setup

**For agent teams to work efficiently:**

1. Edit `.claude/settings.json`:
   ```json
   {
     "permissions": {
       "defaultMode": "acceptEdits",
       "allow": ["Read", "Edit", "Write", "Glob", "Grep", "Bash(git *)", "Bash(./gradlew *)", "Bash(npm *)", "Bash(flutter *)"],
       "deny": ["Bash(rm -rf *)", "Bash(git push *)", "Bash(git reset --hard *)"]
     }
   }
   ```

2. Restart Claude Code

3. Test: Edit a file (should work without prompts)

4. Trigger your team (they'll work without permission interruptions)

**Result:** 2-3 hour feature delivery instead of 4+ hours with permission prompts

---

## See Also

- `TEAM_COORDINATION_GUIDE.md` — How to manage agent teams
- `AGENT_OPERATING_PRINCIPLES.md` — How agents operate
- `constitution.md` — Architecture governance (permissions enforce this)
