# Git Flow Branching Strategy

This document outlines the Git Flow branching model used in this project to manage code development, releases, and hotfixes in an organized and scalable way.

## Overview

Git Flow is a branching model that provides a robust framework for managing releases and features in larger projects. It uses multiple branch types for different purposes, ensuring code stability and organized development workflows.

## Main Branches

### `main` (Production)
- **Purpose**: Production-ready code only
- **Stability**: Highly stable, tagged with version numbers
- **Protection**: Should be protected; all merges require pull request review
- **Source**: Only receives merges from `release/` and `hotfix/` branches
- **Deploy**: Automatically deployed to production

### `develop` (Integration)
- **Purpose**: Main development branch where features integrate
- **Stability**: Stable but may contain unreleased features
- **Protection**: Should be protected; all merges require pull request review
- **Source**: Receives merges from `feature/`, `release/`, and `bugfix/` branches
- **Deploy**: Can be deployed to staging/QA environment

## Supporting Branches

### Feature Branches (`feature/*`)

Used for developing new features or enhancements.

**Naming Convention**: `feature/TICKET-ID-description` or `feature/short-description`
- Example: `feature/PROJ-123-user-authentication`

**Workflow**:
```bash
# Create feature branch from develop
git checkout develop
git pull origin develop
git checkout -b feature/PROJ-123-user-authentication

# Work on feature
git add .
git commit -m "feat: implement user authentication"

# Push to remote
git push origin feature/PROJ-123-user-authentication

# Create Pull Request to develop
# After review and approval, merge via PR
```

**Deletion**: Delete feature branch after merging to develop

### Release Branches (`release/*`)

Used to prepare a new production release.

**Naming Convention**: `release/VERSION` or `release/v1.2.0`
- Example: `release/v1.2.0`

**Workflow**:
```bash
# Create release branch from develop
git checkout develop
git pull origin develop
git checkout -b release/v1.2.0

# Fix bugs, update version numbers, prepare release notes
git add .
git commit -m "chore: prepare release v1.2.0"

# Push to remote
git push origin release/v1.2.0

# Create Pull Request to main
# After approval, merge to main with version tag
git checkout main
git merge --no-ff release/v1.2.0
git tag -a v1.2.0 -m "Release version 1.2.0"
git push origin main --tags

# Also merge back to develop
git checkout develop
git merge --no-ff release/v1.2.0
git push origin develop

# Delete release branch
git branch -d release/v1.2.0
git push origin --delete release/v1.2.0
```

**Usage**: 
- Only bug fixes, version number updates, and release-related metadata
- No new features on release branches
- Typically lasts a few days to a week

### Hotfix Branches (`hotfix/*`)

Used for critical bug fixes in production.

**Naming Convention**: `hotfix/VERSION-description` or `hotfix/v1.2.1-critical-bug`
- Example: `hotfix/v1.2.1-payment-fix`

**Workflow**:
```bash
# Create hotfix branch from main
git checkout main
git pull origin main
git checkout -b hotfix/v1.2.1-payment-fix

# Fix the bug
git add .
git commit -m "fix: resolve payment processing error"

# Push to remote
git push origin hotfix/v1.2.1-payment-fix

# Create Pull Request to main
# After approval, merge to main
git checkout main
git merge --no-ff hotfix/v1.2.1-payment-fix
git tag -a v1.2.1 -m "Hotfix version 1.2.1"
git push origin main --tags

# Also merge to develop to prevent regression
git checkout develop
git merge --no-ff hotfix/v1.2.1-payment-fix
git push origin develop

# Delete hotfix branch
git branch -d hotfix/v1.2.1-payment-fix
git push origin --delete hotfix/v1.2.1-payment-fix
```

**Usage**:
- Critical production bugs only
- No new features
- Created directly from main for urgent fixes

### Bugfix Branches (`bugfix/*`)

Used for non-critical bug fixes during development.

**Naming Convention**: `bugfix/TICKET-ID-description` or `bugfix/short-description`
- Example: `bugfix/PROJ-456-login-validation`

**Workflow**:
```bash
# Create bugfix branch from develop
git checkout develop
git pull origin develop
git checkout -b bugfix/PROJ-456-login-validation

# Fix the bug
git add .
git commit -m "fix: correct login validation logic"

# Push to remote
git push origin bugfix/PROJ-456-login-validation

# Create Pull Request to develop
# After review and approval, merge via PR
```

**Deletion**: Delete bugfix branch after merging to develop

## Commit Message Convention

Follow conventional commits for clarity:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types**:
- `feat`: A new feature
- `fix`: A bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, missing semicolons, etc.)
- `refactor`: Code refactoring
- `perf`: Performance improvements
- `test`: Adding or updating tests
- `chore`: Build process, dependencies, tooling

**Examples**:
```
feat(auth): add OAuth2 integration
fix(payment): correct transaction rounding error
docs(readme): update installation instructions
refactor(api): simplify request handler
```

## Branch Naming Rules

1. Use lowercase letters
2. Use hyphens to separate words (no spaces or underscores)
3. Keep names concise but descriptive
4. Include ticket/issue ID if applicable
5. Avoid special characters

**Valid**: `feature/PROJ-123-user-profile`, `hotfix/v1.2.1-security`
**Invalid**: `Feature_PROJ_123_User_Profile`, `bugfix/fix the thing`, `my-awesome-feature`

## Pull Request Guidelines

1. **Description**: Clearly describe what changes are included and why
2. **Linked Issues**: Reference related issues or tickets
3. **Testing**: Confirm testing was performed
4. **Reviewers**: Assign appropriate reviewers
5. **Branch Protection**: Ensure all CI checks pass before merging
6. **Squash vs Merge**: Use `--no-ff` for feature/release/hotfix branches to preserve history

## Developer Workflow Example

### Feature Development
```bash
# 1. Start new feature
git checkout develop
git pull origin develop
git checkout -b feature/PROJ-789-search-functionality

# 2. Make commits as you work
git add src/search.js
git commit -m "feat(search): implement basic search"

# 3. Push and create PR
git push origin feature/PROJ-789-search-functionality
# Create PR on GitHub/GitLab pointing to develop

# 4. After approval, merge via PR interface

# 5. Clean up locally
git checkout develop
git pull origin develop
git branch -d feature/PROJ-789-search-functionality
```

### Release Process
```bash
# 1. Create release branch
git checkout -b release/v2.0.0 develop

# 2. Update version numbers
# 3. Fix any last-minute bugs
# 4. Create and merge PR to main

# 5. Tag the release
git tag -a v2.0.0 -m "Release v2.0.0"
git push origin main --tags

# 6. Merge back to develop
git merge --no-ff release/v2.0.0 develop
git push origin develop

# 7. Clean up
git branch -d release/v2.0.0
```

### Emergency Hotfix
```bash
# 1. Create hotfix from main
git checkout -b hotfix/v2.0.1-critical-bug main

# 2. Fix the issue
git commit -m "fix: resolve critical production issue"

# 3. Merge to main and tag
git checkout main
git merge --no-ff hotfix/v2.0.1-critical-bug
git tag -a v2.0.1 -m "Hotfix v2.0.1"
git push origin main --tags

# 4. Merge to develop to prevent regression
git checkout develop
git merge --no-ff hotfix/v2.0.1-critical-bug
git push origin develop

# 5. Clean up
git branch -d hotfix/v2.0.1-critical-bug
```

## Branch Protection Rules

Recommended settings for `main` and `develop`:

- ✅ Require pull request reviews (at least 1)
- ✅ Require status checks to pass (CI/CD)
- ✅ Require branches to be up to date
- ✅ Dismiss stale pull request approvals
- ✅ Restrict who can push to matching branches
- ✅ Require code owners review
- ✅ Require conversation resolution before merging

## Useful Git Flow Commands

```bash
# See all branches
git branch -a

# Delete local branch
git branch -d branch-name

# Delete remote branch
git push origin --delete branch-name

# Force delete local branch
git branch -D branch-name

# Rename branch
git branch -m old-name new-name

# See commit history for a branch
git log branch-name

# Compare branches
git diff main develop
git log main..develop  # commits in develop not in main
```

## Tips for Developer Agents

1. **Always pull latest** before creating a new branch from develop or main
2. **Keep branches short-lived** - merge within 1-2 weeks
3. **Rebase or merge cleanly** - use `--no-ff` flag to preserve branch history
4. **Squash if needed** - for feature branches with many small commits
5. **Test thoroughly** before creating PR
6. **Write clear commit messages** - future you will thank present you
7. **Communicate** - mention in PR if blocking other work or blocked by other work
8. **Clean up** - always delete branches after merging
9. **Version tags** - always tag releases on main branch
10. **Follow conventions** - consistency makes the codebase easier to navigate

## References

- [Original Git Flow Model](https://nvie.com/posts/a-successful-git-branching-model/)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [GitHub Flow Documentation](https://guides.github.com/introduction/flow/)
