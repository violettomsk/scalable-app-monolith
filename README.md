# {{PROJECT_NAME}} — reusable scalable app template

A clean, modular-monolith starter designed to scale toward high concurrency
**without a rewrite**. Governance lives in code, so the architecture can't quietly drift.

## Layout
```
.
├── AGENTS.md                 # how AI agents apply the rules
├── .specify/memory/constitution.md   # the law (injected into specs/plans)
├── docs/scaling-roadmap.md   # MVP -> ~1M CCU, trigger-based phases
├── backend/                  # Spring Boot, hexagonal, by bounded context
├── web/                      # Angular 21 + Tailwind (BFF auth)
└── mobile/                   # Flutter (feature-first, clean layers)
```

## Run locally
```
docker compose up -d            # postgres, redis, keycloak
cd backend && ./gradlew bootRun  # http://localhost:8080
cd web && npm i && npm start     # http://localhost:4200
cd mobile && flutter run
```

## How the architecture is enforced
- `backend/.../architecture/CleanArchitectureTest.java` (ArchUnit) **fails the build**
  if the domain imports a framework or a dependency points outward.
- The `catalog` context is the **reference slice**. Create a new bounded context by
  copying it: `domain` (framework-free) -> `application` (use cases + ports) ->
  `adapter/in` (REST + DTOs) -> `adapter/out` (JPA adapter implementing the port).
- Web auth uses the **BFF pattern**: the browser holds only an httpOnly session
  cookie; tokens never touch JavaScript. Stand up the BFF as a server-side component
  (e.g. Spring Cloud Gateway or a dedicated BFF service) exposing `/bff/*`.

## Spec-driven workflow
Run `specify init` (GitHub Spec Kit) to get the official slash commands and templates,
then keep `.specify/memory/constitution.md` as-is — it's injected into every
`/specify` and `/plan`.
