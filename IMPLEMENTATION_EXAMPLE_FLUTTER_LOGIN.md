# Implementation Example: Flutter Login Feature

> Step-by-step guide to implement a simple login feature in Flutter using the spec-driven workflow

---

## Overview

This example walks through implementing a basic authentication feature in Flutter using:
- **Architecture:** Hexagonal/Clean (domain → application → presentation/data)
- **Auth method:** OAuth2 Auth Code Flow + PKCE (not username/password stored locally)
- **State management:** Riverpod
- **HTTP client:** Dio
- **Secure storage:** FlutterSecureStorage

**Estimated time:** 2-3 hours (with an AI assistant using this guide)

---

## Step 0: Setup & Prerequisites

### Local Environment Check
```bash
# Verify Flutter is installed
flutter --version

# Verify backend is running
curl http://localhost:8080/actuator/health

# Verify Keycloak is accessible (for OAuth2)
curl http://localhost:8180/auth/realms/master
```

### Project Structure (Before)
```
mobile/
├── lib/
│   ├── main.dart
│   ├── core/          # Shared: network, constants, etc.
│   └── features/      # Feature-first, each owns presentation/domain/data
```

### Project Structure (After)
```
mobile/
├── lib/
│   ├── core/
│   │   ├── network/
│   │   │   └── dio_provider.dart          # HTTP client setup
│   │   ├── storage/
│   │   │   └── secure_storage.dart        # Token storage
│   │   └── constants/
│   │       └── auth_constants.dart
│   └── features/
│       └── auth/
│           ├── presentation/              # UI
│           │   ├── login_page.dart
│           │   └── login_provider.dart    # State (Riverpod)
│           ├── domain/                    # Business logic (framework-free)
│           │   ├── auth_repository.dart   # Interface
│           │   └── user.dart              # Domain models
│           └── data/                      # Implementation
│               ├── auth_repository_impl.dart
│               └── keycloak_auth_api.dart
```

---

## Phase 1: Specify (5-10 minutes)

### What to Tell Your AI:

```
I need to implement a login feature in Flutter for this app.

Requirements:
- User opens the app and sees a login screen
- User taps "Login with Keycloak"
- User is redirected to Keycloak login (browser)
- User enters credentials in Keycloak (NOT in the app)
- User is redirected back to the app with auth code
- App exchanges code for tokens (refresh token stored securely)
- User is logged in and sees the home screen

Tech notes:
- Use Riverpod for state management
- Use Dio for HTTP
- Use FlutterSecureStorage for token storage
- OAuth2 Auth Code + PKCE (not username/password direct)

Ask me clarifying questions before writing the spec.
```

### Expected AI Response (Clarifying Questions):

The AI should ask:
1. **Scope:** Just login, or also logout, auto-refresh, forgot password?
2. **Backend:** Is Keycloak already running? What's the client ID?
3. **UX:** Should we show a loading screen? Handle errors?
4. **Token refresh:** Auto-refresh on expiry, or prompt login?
5. **Data persistence:** Remember login on app restart?

### Your Answers:
```
1. Just login and logout for now
2. Yes, Keycloak is running locally. Client ID = "mobile-app"
3. Yes, loading screen. Show errors as snackbars
4. Auto-refresh on expiry
5. Yes, remember login on app restart
```

### Ask AI to Write Spec:

```
Write a SPEC (no code, no architecture):
- User scenarios (what can users do?)
- Acceptance criteria (how do we know it works?)
- Constraints (performance, security, data)

No implementation detail yet.
```

### Expected Spec Output:

```markdown
# Spec: Mobile Authentication

## User Scenarios
1. As a user, I can tap "Login" to authenticate with Keycloak
2. I am redirected to Keycloak login in a browser
3. After entering credentials, I return to the app logged in
4. I can see my username after login
5. I can tap "Logout" to clear my credentials

## Acceptance Criteria
- [ ] Login button redirects to Keycloak (OAuth2 Auth Code + PKCE)
- [ ] App receives auth code and exchanges it for tokens
- [ ] Refresh token stored in secure storage (not accessible to JavaScript/web)
- [ ] User sees loading indicator during login
- [ ] User sees username after successful login
- [ ] Logout clears tokens from secure storage
- [ ] App auto-refreshes tokens before expiry
- [ ] App restores login state on cold start (if tokens exist)

## Constraints
- Security: No passwords stored locally, no tokens in SharedPreferences
- Performance: Login < 3 seconds, token refresh < 1 second
- Offline: App allows viewing if token still valid
- Keycloak: Client ID = "mobile-app", redirect = "com.example.app://callback"
```

**Approve?** If not, ask AI to adjust and re-read until you agree.

---

## Phase 2: Plan (10-15 minutes)

### Tell AI:

```
Approved spec. Now propose the architecture:

1. Which bounded context does this live in?
2. What domain entities/models do we need?
3. What application services/providers?
4. What data layer (repositories, APIs)?
5. What UI layer (screens, providers)?
6. Any cross-layer concerns (security, networking)?

Then run the Governance Gate:
- Does domain import any Flutter/Dio/SecureStorage? (Should be NO)
- Does each layer import only what it needs?
- Is auth isolated from other features?
- Are secrets NOT stored in SharedPreferences?
- Is the state management (Riverpod) in presentation only?

Run all 8 architecture checks.
```

### Expected Plan Output:

```markdown
# Architecture Plan: Mobile Authentication

## Bounded Context
**Context:** `auth` (new)
- Reason: Authentication is distinct; owns login/logout lifecycle

## Domain Layer (Framework-free)
- **User** (value object): userId, username, email
- **AuthToken** (value object): accessToken, refreshToken, expiryTime
- **AuthRepository** (interface): login(), logout(), refreshToken(), isAuthenticated()

## Application Layer (Riverpod Providers)
- **AuthProvider** (StateNotifier): manages login state
- **AuthService** (Riverpod provider): orchestrates login flow
- **SecureStorageService**: wrapper around FlutterSecureStorage

## Data Layer (Implementation)
- **AuthRepositoryImpl**: implements AuthRepository port
- **KeycloakAuthApi**: REST calls to Keycloak (Dio)
- **SecureTokenStorage**: stores tokens securely

## Presentation Layer
- **LoginPage**: UI screen
- **LoginProvider** (Riverpod): exposes login() and logout() to UI

## GOVERNANCE GATE (8-Point Check)
1. ✅ Domain imports ZERO Flutter, Dio, SecureStorage
2. ✅ Application layer (Riverpod) imports domain + ports only
3. ✅ Data layer implements ports from domain
4. ✅ Presentation layer calls through providers (not direct API)
5. ✅ Auth is one bounded context, no cross-context coupling
6. ✅ Tokens stored in platform secure storage, not SharedPreferences
7. ✅ OAuth2 Auth Code + PKCE (no hardcoded credentials)
8. ✅ All state management in Riverpod, not in mutable widget state

**Result: All checks pass. Ready to implement.**
```

**Approve the plan?** If yes, proceed to Tasks.

---

## Phase 3: Tasks (5-10 minutes)

### Tell AI:

```
Plan approved. Decompose into 5-6 independent tasks:

For each task:
- What to build
- Which layer/file
- Acceptance criteria
- Dependencies

Build order should be: domain → application → data → presentation → tests
```

### Expected Task Decomposition:

```markdown
# Task Decomposition: Mobile Auth

**Task 1: Domain Layer - Auth Models**
- Build: User value object, AuthToken value object, AuthRepository interface
- Files: lib/features/auth/domain/user.dart, auth_token.dart, auth_repository.dart
- No Framework imports
- Acceptance: All value objects immutable, interface is clean
- Dependencies: None

**Task 2: Application Layer - Auth Service**
- Build: AuthService (orchestrates login flow), SecureStorageService
- Files: lib/features/auth/application/auth_service.dart
- Uses domain interfaces
- Acceptance: login() and logout() work with mocked repositories
- Dependencies: Task 1

**Task 3: Data Layer - Keycloak API + Storage**
- Build: KeycloakAuthApi (Dio HTTP calls), SecureTokenStorage
- Files: lib/features/auth/data/keycloak_auth_api.dart, secure_token_storage.dart
- Implements domain repository port
- Acceptance: API calls work, tokens stored securely
- Dependencies: Task 1, 2

**Task 4: Presentation - Login UI + Riverpod State**
- Build: LoginPage, LoginProvider (Riverpod StateNotifier)
- Files: lib/features/auth/presentation/login_page.dart, login_provider.dart
- Calls AuthService through providers
- Acceptance: UI shows loading, handles errors, displays username after login
- Dependencies: Task 2, 3

**Task 5: App Navigation & Auth State**
- Build: Update main.dart to check auth state, conditional routing
- Files: lib/main.dart, lib/features/auth/presentation/auth_gate.dart
- Restores login state on cold start
- Acceptance: App shows login if not authenticated, home if authenticated
- Dependencies: Task 4

**Task 6: Testing & Verification**
- Build: Unit tests (domain), integration tests (data layer with Keycloak mock)
- Files: test/features/auth/...
- Acceptance: All critical paths tested
- Dependencies: All other tasks
```

**Ready to start Task 1?** If yes, proceed to Implementation.

---

## Phase 4: Implementation (Full Walkthrough)

### Task 1: Domain Layer

**Tell AI:**

```
Task 1: Domain Layer - Auth Models

Build test-first (NO Flutter imports, pure Dart):

1. Write unit tests for User, AuthToken, AuthRepository
2. Implement value objects (immutable)
3. Implement repository interface

Show me:
- test/features/auth/domain/user_test.dart (tests)
- test/features/auth/domain/auth_token_test.dart (tests)
- lib/features/auth/domain/user.dart (code)
- lib/features/auth/domain/auth_token.dart (code)
- lib/features/auth/domain/auth_repository.dart (interface)

No Flutter, no Dio, no SecureStorage. Pure domain.
```

**Expected AI Output:**

```dart
// lib/features/auth/domain/user.dart
class User {
  final String id;
  final String username;
  final String email;
  
  const User({
    required this.id,
    required this.username,
    required this.email,
  });
}

// lib/features/auth/domain/auth_token.dart
class AuthToken {
  final String accessToken;
  final String refreshToken;
  final DateTime expiresAt;
  
  const AuthToken({
    required this.accessToken,
    required this.refreshToken,
    required this.expiresAt,
  });
  
  bool get isExpired => DateTime.now().isAfter(expiresAt);
  bool get isExpiringSoon => DateTime.now().add(Duration(minutes: 5)).isAfter(expiresAt);
}

// lib/features/auth/domain/auth_repository.dart
abstract class AuthRepository {
  Future<AuthToken> login(String authCode, String codeVerifier);
  Future<AuthToken> refreshToken(String refreshToken);
  Future<void> logout();
  Future<bool> isAuthenticated();
}
```

**Verify:** No Flutter imports, tests pass. ✅

---

### Task 2: Application Layer

**Tell AI:**

```
Task 2: Application Layer - Auth Service & Providers

Build:
- AuthService: orchestrates login, logout, refresh
- SecureStorageService: wrapper around FlutterSecureStorage

Use domain models + repository interface (mocked in tests).

Show me:
- lib/features/auth/application/auth_service.dart
- lib/features/auth/application/secure_storage_service.dart
- Unit tests with mocked AuthRepository

Use Riverpod providers where needed.
```

**Expected Output:**

```dart
// lib/features/auth/application/auth_service.dart
class AuthService {
  final AuthRepository repository;
  final SecureStorageService secureStorage;
  
  AuthService({
    required this.repository,
    required this.secureStorage,
  });
  
  Future<void> login(String authCode, String codeVerifier) async {
    final token = await repository.login(authCode, codeVerifier);
    await secureStorage.saveToken(token);
  }
  
  Future<void> logout() async {
    await secureStorage.clearToken();
    await repository.logout();
  }
  
  Future<AuthToken?> getStoredToken() => secureStorage.getToken();
  
  Future<void> refreshTokenIfNeeded() async {
    final token = await getStoredToken();
    if (token != null && token.isExpiringSoon) {
      final newToken = await repository.refreshToken(token.refreshToken);
      await secureStorage.saveToken(newToken);
    }
  }
}
```

**Verify:** No Flutter UI imports, tests mock repository. ✅

---

### Task 3: Data Layer

**Tell AI:**

```
Task 3: Data Layer - Keycloak API & Secure Storage

Build:
- KeycloakAuthApi: REST calls to Keycloak
- AuthRepositoryImpl: implements AuthRepository port
- SecureTokenStorage: stores tokens in platform secure storage

Use Dio for HTTP, FlutterSecureStorage for storage.

Show me:
- lib/features/auth/data/keycloak_auth_api.dart
- lib/features/auth/data/auth_repository_impl.dart
- lib/core/storage/secure_token_storage.dart

Include:
- Auth Code + PKCE exchange
- Secure token storage (not SharedPreferences)
- Error handling
- Integration tests with mock Keycloak
```

**Expected Output:**

```dart
// lib/features/auth/data/keycloak_auth_api.dart
class KeycloakAuthApi {
  final Dio dio;
  final String clientId;
  final String redirectUrl;
  
  KeycloakAuthApi({
    required this.dio,
    required this.clientId,
    required this.redirectUrl,
  });
  
  Future<AuthToken> exchangeAuthCode(String authCode, String codeVerifier) async {
    final response = await dio.post(
      'http://localhost:8180/auth/realms/master/protocol/openid-connect/token',
      data: {
        'grant_type': 'authorization_code',
        'client_id': clientId,
        'code': authCode,
        'code_verifier': codeVerifier,  // PKCE
        'redirect_uri': redirectUrl,
      },
    );
    
    return AuthToken(
      accessToken: response.data['access_token'],
      refreshToken: response.data['refresh_token'],
      expiresAt: DateTime.now().add(
        Duration(seconds: response.data['expires_in']),
      ),
    );
  }
}

// lib/core/storage/secure_token_storage.dart
class SecureTokenStorage {
  final FlutterSecureStorage storage = FlutterSecureStorage();
  
  Future<void> saveToken(AuthToken token) async {
    await storage.write(
      key: 'refresh_token',
      value: token.refreshToken,
    );
  }
  
  Future<AuthToken?> getToken() async {
    final refreshToken = await storage.read(key: 'refresh_token');
    if (refreshToken == null) return null;
    // Load full token from secure storage
    // (in production, may need to request new access token)
    return AuthToken(...);
  }
}
```

**Verify:** Uses Dio, SecureStorage (not SharedPreferences), PKCE. ✅

---

### Task 4: Presentation Layer

**Tell AI:**

```
Task 4: Presentation - Login UI + Riverpod State

Build:
- LoginProvider: Riverpod StateNotifier for auth state
- LoginPage: Flutter UI with login button
- Handle loading, errors, success states

Show me:
- lib/features/auth/presentation/login_provider.dart
- lib/features/auth/presentation/login_page.dart

Use Riverpod for state management.
Call AuthService through providers (not direct).
Handle OAuth2 redirect using app_links or uni_links.
```

**Expected Output:**

```dart
// lib/features/auth/presentation/login_provider.dart
class LoginState {
  final bool isLoading;
  final String? error;
  final bool isAuthenticated;
  
  const LoginState({
    this.isLoading = false,
    this.error,
    this.isAuthenticated = false,
  });
}

final loginProvider = StateNotifierProvider<LoginNotifier, LoginState>((ref) {
  return LoginNotifier(ref.read(authServiceProvider));
});

class LoginNotifier extends StateNotifier<LoginState> {
  final AuthService authService;
  
  LoginNotifier(this.authService) : super(const LoginState());
  
  Future<void> handleAuthCallback(String authCode, String codeVerifier) async {
    state = state.copyWith(isLoading: true);
    try {
      await authService.login(authCode, codeVerifier);
      state = const LoginState(isAuthenticated: true);
    } catch (e) {
      state = state.copyWith(error: e.toString(), isLoading: false);
    }
  }
  
  Future<void> logout() async {
    await authService.logout();
    state = const LoginState();
  }
}

// lib/features/auth/presentation/login_page.dart
class LoginPage extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Login')),
      body: Consumer(
        builder: (context, ref, child) {
          final state = ref.watch(loginProvider);
          
          if (state.isLoading) {
            return const Center(child: CircularProgressIndicator());
          }
          
          if (state.error != null) {
            return Center(
              child: Text('Error: ${state.error}'),
            );
          }
          
          return Center(
            child: ElevatedButton(
              onPressed: () => _initiateLogin(context, ref),
              child: const Text('Login with Keycloak'),
            ),
          );
        },
      ),
    );
  }
  
  void _initiateLogin(BuildContext context, WidgetRef ref) {
    // Launch browser to Keycloak login URL with Auth Code + PKCE
    // Listen for redirect back to app
    // Call ref.read(loginProvider.notifier).handleAuthCallback()
  }
}
```

**Verify:** Uses Riverpod, calls AuthService, handles loading/errors. ✅

---

### Task 5: App Navigation

**Tell AI:**

```
Task 5: App Navigation & Auth Gate

Build:
- AuthGate: checks if user is authenticated
- Update main.dart to show LoginPage if not auth, HomePage if auth
- Restore login state on cold start

Show me:
- lib/features/auth/presentation/auth_gate.dart
- Updated lib/main.dart
```

**Expected Output:**

```dart
// lib/main.dart
void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp();

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: const AuthGate(),
    );
  }
}

// lib/features/auth/presentation/auth_gate.dart
class AuthGate extends ConsumerStatefulWidget {
  const AuthGate();

  @override
  ConsumerState<AuthGate> createState() => _AuthGateState();
}

class _AuthGateState extends ConsumerState<AuthGate> {
  @override
  void initState() {
    super.initState();
    _restoreLoginState();
  }

  Future<void> _restoreLoginState() async {
    final authService = ref.read(authServiceProvider);
    final token = await authService.getStoredToken();
    if (token != null && !token.isExpired) {
      ref.read(loginProvider.notifier).state = const LoginState(
        isAuthenticated: true,
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final loginState = ref.watch(loginProvider);
    
    if (loginState.isAuthenticated) {
      return const HomePage();
    } else {
      return const LoginPage();
    }
  }
}
```

**Verify:** Restores auth state on cold start, routes correctly. ✅

---

### Task 6: Testing

**Tell AI:**

```
Task 6: Testing & Verification

Write tests:
- Domain layer: User, AuthToken value object tests
- Data layer: Mock Keycloak, test token exchange
- Presentation: Test login state, button taps

Show me:
- test/features/auth/domain/user_test.dart
- test/features/auth/data/keycloak_auth_api_test.dart
- test/features/auth/presentation/login_provider_test.dart

Use mockito for mocking.
All critical paths tested.
```

**Expected Output:** (Won't paste full test code, but structure should be):
```
✅ User value object tests
✅ AuthToken expiry tests
✅ KeycloakAuthApi token exchange tests
✅ LoginProvider state transitions tests
✅ Integration test: login flow end-to-end
```

---

## Phase 5: Verify (Definition of Done)

### Tell AI:

```
All tasks complete. Run the Definition of Done:

✅ Domain layer: framework-free (no Flutter, Dio, SecureStorage)
✅ Application layer: uses domain models + repository ports
✅ Data layer: implements repository, integrates with Keycloak + SecureStorage
✅ Presentation: uses Riverpod, calls through AuthService
✅ Tests: domain unit, data integration, presentation state
✅ OAuth2: Auth Code + PKCE (not hardcoded credentials)
✅ Security: tokens in platform secure storage, not SharedPreferences
✅ Auth state: restored on cold start
✅ Error handling: loading states, error messages, retry
✅ Bounded context: auth is isolated from other features

Run all checks.
```

### Expected DoD Report:

```markdown
# Definition of Done: Mobile Authentication

✅ **Domain layer is framework-free**
   - Zero Flutter imports (no material, widgets, flutter_secure_storage, dio)
   - Only Dart classes: User, AuthToken, AuthRepository interface
   - All immutable value objects

✅ **Architecture: domain → application → data → presentation**
   - Presentation calls through providers (no direct API calls)
   - Application (AuthService) uses domain models + repository port
   - Data layer implements repository port
   - Clear dependency direction

✅ **OAuth2 Auth Code + PKCE**
   - Auth code exchanged with code_verifier
   - No hardcoded credentials in code
   - Tokens never stored in SharedPreferences

✅ **Secure token storage**
   - Refresh token in FlutterSecureStorage (platform secure)
   - Not in SharedPreferences (accessible to JavaScript)
   - Not in process memory

✅ **State management: Riverpod**
   - LoginProvider manages auth state
   - UI calls through providers (not direct calls)
   - State restored on cold start

✅ **Tests passing**
   - Domain unit tests: 8 tests ✓
   - Data integration tests: 5 tests ✓
   - Presentation state tests: 6 tests ✓
   - Critical path: login → logout → re-login ✓

✅ **Error handling**
   - Loading state during login
   - Error messages on failure
   - Graceful logout

✅ **Bounded context isolation**
   - Auth context doesn't import other features
   - Other features import auth provider, not internals
   - Future integration with catalog/orders is clean

🟢 READY FOR INTEGRATION
```

**Ready to test in the app?**

---

## Full Command: Running Through All Phases with AI

If you want to do this with your chosen AI tool, here's the complete conversation:

### Message 1: Setup
```
[Paste AGENT_PROMPT_TEMPLATES.md system instruction]

I want to build a Flutter login feature. Follow the spec-first workflow.
Context:
- Backend: Spring Boot running on localhost:8080
- Auth provider: Keycloak on localhost:8180
- Client ID: mobile-app
- Keycloak realm: master

Ask clarifying questions first (one round only).
```

### Message 2: Clarifications
```
[Answer the AI's questions]

Now write a SPEC (no code, no architecture):
- User scenarios
- Acceptance criteria
- Constraints

Tech-free, focus on user value.
```

### Message 3: Approval
```
Spec looks good. Now propose the architecture:
1. Bounded context
2. Domain models
3. Application services
4. Data layer (repository, API)
5. Presentation layer

Then run the Governance Gate (8 architecture checks).
```

### Message 4: Approval
```
Plan approved. Decompose into 5-6 tasks.

For each:
- What to build
- Which files
- Acceptance criteria
- Dependencies
```

### Message 5-10: Implementation
```
Task 1: [description from decomposition]

Build test-first:
1. Write unit tests
2. Implement code
3. Show all tests passing

No Flutter imports in domain layer.
```

(Repeat for each task)

### Final Message: Verification
```
All tasks complete. Run the Definition of Done:
[Full checklist from Phase 5]

Report which items are ✅ green.
```

---

## Troubleshooting

### Issue: AI Jumps to Code Before Spec

**Fix:** Remind it:
```
STOP. Do not write code yet. 
Follow the order:
1. Spec (user scenarios + criteria)
2. Architecture plan + governance gate
3. Task decomposition
4. THEN implement test-first

We're still on step 1. Write the spec first.
```

### Issue: AI Adds Flutter Imports to Domain

**Fix:**
```
Domain layer test failed: domain imports 'package:flutter/material.dart'

Fix: Remove all Flutter imports from domain.
Domain should be pure Dart (User, AuthToken, AuthRepository interface only).

Show me the corrected code.
```

### Issue: AI Forgets to Use Auth Code + PKCE

**Fix:**
```
Your code stores the auth code directly. This violates OAuth2 best practices.

Fix: Implement Auth Code + PKCE flow:
1. Generate code_challenge and code_verifier
2. Redirect to Keycloak with code_challenge
3. Keycloak redirects back with auth code
4. Exchange auth code + code_verifier for tokens

Show me the corrected KeycloakAuthApi.
```

### Issue: AI Uses SharedPreferences for Tokens

**Fix:**
```
This violates Article V § 3: tokens must be in platform secure storage.

SharedPreferences is world-readable on Android (accessible to other apps).

Fix: Use FlutterSecureStorage instead (Keychain on iOS, Keystore on Android).

Update secure_token_storage.dart.
```

---

## Next: Integration with Backend

Once Flutter login is done, you'll need to:

1. **Create an auth context in the backend** (Spring Boot)
   - OAuth2 resource server configuration
   - JwtDecoder to validate Keycloak tokens
   - AuthController with /auth/validate endpoint

2. **Connect Flutter login to backend**
   - Call /auth/validate on every app startup
   - Refresh token on server if needed
   - Logout calls /auth/logout on backend

3. **Integrate with other contexts**
   - Catalog context: reads user ID from token
   - Orders context: checks user is authenticated
   - Notifications context: sends emails based on user actions

This follows the same spec → plan → tasks → implement → verify workflow.

---

## Summary

**Using the workflow above with any AI tool:**

1. ✅ **Spec** (5 min) — Define user scenarios, no tech
2. ✅ **Plan** (10 min) — Propose architecture, run governance gate
3. ✅ **Tasks** (5 min) — Break into domain → app → data → presentation
4. ✅ **Implement** (90-120 min) — Code test-first, inside-out
5. ✅ **Verify** (10 min) — Run Definition of Done

**Total: 2-3 hours for a complete, governance-compliant Flutter login feature.**

**Tools recommended:**
- **Claude Code:** For full coordination + testing
- **Gemini 2.0 Flash:** For cost-effective implementation ($0.50-2 per feature)
- **GPT-4:** If you need the absolute best architectural guidance

All three can do this workflow. Pick based on your budget and team size.

Now you're ready. Pick your AI tool and start with **"I want to build a Flutter login feature. Ask clarifying questions first."**

Good luck! 🚀
