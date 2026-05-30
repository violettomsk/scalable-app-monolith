# web (Angular 21 + Tailwind)

Standalone components + signals. `core/` = auth/BFF + interceptors, `shared/` = UI
primitives, `features/*` = lazy-loaded per bounded context.

**Auth (BFF pattern):** the browser holds only an httpOnly session cookie. Tokens
live server-side in the BFF. Never store tokens in localStorage. The BFF exposes
`/bff/login`, `/bff/logout`, `/bff/me` and proxies `/api/*` with the access token
attached server-side. Run `ng add tailwindcss` to wire Tailwind v4.
