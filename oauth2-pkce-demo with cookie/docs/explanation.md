# React + Spring Boot + Keycloak PKCE with HttpOnly Cookie (BFF Pattern)

## 1) What changed and why

Before this change, React handled tokens directly (`keycloak-js`) and sent bearer tokens to the API.

Now we switched to a **BFF-style setup**:

- Browser talks to Spring Boot.
- Spring Boot talks to Keycloak for OAuth2/OIDC login.
- Browser stores only session cookie (`JSESSIONID`, HttpOnly), not access token.

Why this is better:

1. **Access tokens are no longer exposed to JavaScript**.
2. Better protection against token theft via XSS.
3. Centralized auth logic in backend (simpler frontend auth code).

PKCE is still used in the authorization code flow.

---

## 2) Architecture in this project

### Frontend (`frontend/`)

- Calls backend with `credentials: "include"`.
- Uses backend auth endpoints:
  - `GET /api/auth/login`
  - `GET /api/auth/register`
  - `GET /api/session`
  - `POST /api/auth/logout`
- No `Authorization: Bearer ...` header anymore.

### Backend (`PKCE-flow/`)

- Uses `oauth2Login()` as OAuth2 client with Keycloak.
- Keeps user logged in with server-side session.
- Uses role checks (`hasRole("USER")`, `hasRole("ADMIN")`).
- Returns session info to frontend through `GET /api/session`.

### Keycloak theme (`keycloak-theme/`)

- Your custom Keycloakify login and register UI remains in use.
- Login/register requests still redirect to Keycloak pages, so your custom UI is preserved.

---

## 3) Important backend implementation details

### File: `PKCE-flow/src/main/java/com/example/PKCE_flow/SecurityConfig.java`

Main updates:

1. **OAuth2 Login enabled**
   - `oauth2Login(...)` is now the main auth entry.
   - On success, user is redirected back to frontend URL.

2. **Session cookie auth (HttpOnly)**
   - Spring session identifies the user on API calls.
   - Browser sends `JSESSIONID` automatically.

3. **CSRF protection enabled**
   - `CookieCsrfTokenRepository.withHttpOnlyFalse()` is used.
   - Browser gets `XSRF-TOKEN` cookie (readable by JS).
   - Frontend sends CSRF token on logout POST (`_csrf` form field).

4. **OIDC global logout enabled**
   - Backend logout now uses OIDC RP-initiated logout.
   - `POST /api/auth/logout` clears Spring session and redirects browser to Keycloak logout endpoint.
   - Keycloak clears SSO session, then redirects back to frontend.

5. **CORS with credentials**
   - Allows frontend origin (`app.frontend-url`, default `http://localhost:5173`).
   - `allowCredentials(true)` is required for cookie-based calls.

6. **API auth behavior**
   - `/api/**` returns `401` when unauthenticated (instead of HTML login redirect).
   - This makes frontend error handling clean.

7. **Role extraction from Keycloak**
   - Access token is decoded and `realm_access.roles` are mapped to Spring roles:
     - `USER` -> `ROLE_USER`
     - `ADMIN` -> `ROLE_ADMIN`
   - Backend endpoint rules:
     - `/api/home`: authenticated
     - `/api/user`: `USER` or `ADMIN`
     - `/api/admin`: `ADMIN`

8. **Custom registration flow endpoint**
   - `GET /api/auth/register` redirects to Spring OAuth2 authorization endpoint with `kc_register=true`.
   - Custom authorization request resolver rewrites Keycloak auth endpoint to Keycloak registrations endpoint.
   - This keeps registration in the same secure OAuth2 flow and still uses your custom Keycloak register page.

### File: `PKCE-flow/src/main/java/com/example/PKCE_flow/AuthController.java`

Provides BFF helper endpoints:

- `GET /api/auth/login` -> redirect to `/oauth2/authorization/keycloak`
- `GET /api/auth/register` -> redirect to register flow
- `GET /api/session` -> returns auth status, username, app roles, CSRF header name, and CSRF parameter name

### File: `PKCE-flow/src/main/java/com/example/PKCE_flow/ApiController.java`

- Switched from `Jwt` principal to authenticated session user (`Authentication`/`OidcUser`).
- Endpoint behavior remains same from business perspective.

### File: `PKCE-flow/src/main/resources/application.properties`

Now contains app-level auth properties:

- `app.frontend-url`
- `app.keycloak.issuer-uri`
- `app.keycloak.client-id`
- session cookie settings (`http-only`, `same-site`, optional `secure` via env)

---

## 4) Frontend implementation details

### File: `frontend/src/App.jsx`

Main changes:

1. Removed direct `keycloak-js` usage.
2. Loads session from backend using `GET /api/session`.
3. Login/register buttons now redirect to backend auth endpoints.
4. API calls use cookies (`credentials: "include"`).
5. Logout sends CSRF token via HTML form POST:
   - Reads `XSRF-TOKEN` from cookie.
   - Sends it as `_csrf` to `POST /api/auth/logout`.
   - Browser then follows redirect to Keycloak logout and returns to frontend.

### File: `frontend/.env.example`

- Frontend now only needs backend base URL:

```env
VITE_API_BASE_URL=http://localhost:8080
```

---

## 5) Keycloak configuration checklist (for BFF)

Client `oauth2-pkce-demo` should have:

1. **Client type**: OpenID Connect
2. **Public client** (no secret) if you keep this local demo setup
3. **Standard flow**: On
4. **PKCE**: Enabled/Required (`S256`)
5. **Valid redirect URIs**:
   - `http://localhost:8080/login/oauth2/code/keycloak`
6. **Web origins**:
   - `+` (recommended) or `http://localhost:8080`
7. **Valid post logout redirect URIs**:
   - `http://localhost:5173/*`

Notes:

- `http://localhost:5173/*` is no longer the OAuth callback in this BFF flow.
- If your previous SPA settings remain, they do not provide auth value in this new flow.

---

## 6) Why CSRF is needed now

In bearer-token SPA style, CSRF is usually less relevant because auth is sent explicitly in headers.

In cookie-based session auth, browser auto-sends cookies, so cross-site requests can carry your session.

That is why CSRF token is enabled now:

- Attacker can force a request, but cannot read your CSRF token cookie value from another site.
- Backend checks `X-XSRF-TOKEN` for state-changing requests.

---

## 7) End-to-end flow (simple)

1. User opens React app.
2. React calls `GET /api/session`.
3. User clicks login -> browser goes to `GET /api/auth/login`.
4. Spring redirects to Keycloak login page (your custom Keycloakify UI).
5. User authenticates.
6. Keycloak redirects to Spring callback (`/login/oauth2/code/keycloak`).
7. Spring creates authenticated session and sets `JSESSIONID` (HttpOnly).
8. Browser returns to frontend.
9. React calls protected APIs with `credentials: include`.
10. Spring authorizes request from session + roles.

---

## 8) Security tradeoff note (junior-friendly)

For production BFF, many teams prefer a **confidential backend client** (with client secret) because the backend can safely hold secrets.

In this project we kept your existing client style to minimize change while introducing BFF behavior.

If you later move to confidential client, frontend code can mostly stay the same because auth already goes through backend.

---

## 9) Files changed in this migration

- `plans/plan.md`
- `PKCE-flow/pom.xml`
- `PKCE-flow/src/main/resources/application.properties`
- `PKCE-flow/src/main/java/com/example/PKCE_flow/SecurityConfig.java`
- `PKCE-flow/src/main/java/com/example/PKCE_flow/AppProperties.java`
- `PKCE-flow/src/main/java/com/example/PKCE_flow/SpaCsrfTokenRequestHandler.java`
- `PKCE-flow/src/main/java/com/example/PKCE_flow/AuthController.java`
- `PKCE-flow/src/main/java/com/example/PKCE_flow/ApiController.java`
- `frontend/src/App.jsx`
- `frontend/.env.example`
- `frontend/src/keycloak.js` (removed)
