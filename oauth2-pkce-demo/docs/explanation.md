# React + Keycloak PKCE + Spring Boot + Keycloakify (Step by Step)

This project is split into 3 parts:

1. `frontend/` (React SPA): starts login and stores tokens in browser memory.
2. `PKCE-flow/` (Spring Boot API): validates bearer JWT tokens from Keycloak.
3. `keycloak-theme/` (Keycloakify): customizes Keycloak login UI.

---

## 1) Why this architecture

- React is a **public client** (browser app), so it must not use a client secret.
- PKCE protects the authorization code flow in public clients.
- Keycloak handles authentication and token issuance.
- Spring Boot acts as a **resource server** (API), not as the login server.
- API trusts tokens only if they are signed by your Keycloak realm issuer.

---

## 2) How PKCE works here

When user clicks login:

1. React (`keycloak-js`) generates a random `code_verifier`.
2. React computes `code_challenge` from verifier (method `S256`).
3. Browser redirects user to Keycloak login page.
4. After successful login, Keycloak returns an authorization code to React.
5. React exchanges code for tokens using the original `code_verifier`.
6. React gets access token and sends it to Spring Boot in `Authorization: Bearer <token>`.

If attacker steals only the authorization code, it is useless without the original verifier.

---

## 3) Backend implementation (Spring Boot)

Files:

- `PKCE-flow/src/main/java/com/example/PKCE_flow/SecurityConfig.java`
- `PKCE-flow/src/main/resources/application.properties`

What was done:

- Enabled OAuth2 JWT resource server.
- Protected all API routes (except CORS preflight `OPTIONS`).
- Added CORS for React dev origin `http://localhost:5173`.
- Disabled CSRF for stateless token API usage.
- Made issuer configurable:

```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=${KEYCLOAK_ISSUER_URI:http://localhost:8090/realms/oauth2-demo}
```

Why:

- `issuer-uri` ensures JWT `iss` claim matches your realm.
- API only accepts valid tokens from configured Keycloak realm.

---

## 4) Frontend implementation (React)

Files:

- `frontend/src/keycloak.js`
- `frontend/src/App.jsx`
- `frontend/.env.example`

What was done:

- Integrated `keycloak-js`.
- Initialized Keycloak with PKCE `S256`.
- Added login/logout actions.
- Added token refresh before API call (`updateToken(30)`).
- Called protected endpoint `/api/home` with bearer token.

Environment values:

```env
VITE_KEYCLOAK_URL=http://localhost:8090
VITE_KEYCLOAK_REALM=oauth2-demo
VITE_KEYCLOAK_CLIENT_ID=oauth2-pkce-demo
VITE_API_BASE_URL=http://localhost:8080
```

Why:

- Keeps config external and easy to change.
- Uses existing Keycloak client ID you provided.

---

## 5) Keycloak client settings you must have

In Keycloak admin UI for client `oauth2-pkce-demo`:

- Client type: OpenID Connect
- Access type: Public (no client secret)
- Standard flow: Enabled
- PKCE: Enabled/Required with method `S256`
- Valid redirect URIs: `http://localhost:5173/*`
- Web origins: `http://localhost:5173`

If any of these are wrong, login redirect flow will fail.

For registration fields:

- In `Realm settings` -> `User profile`, do not require `firstName` and `lastName` for registration.
- If they are still required on the server side, hiding them in the custom UI will make registration fail with validation errors.
- If you want both username and email as separate fields, keep `registrationEmailAsUsername` disabled.

---

## 6) Keycloakify custom login + register UI

Files:

- `keycloak-theme/src/login/KcPage.tsx`
- `keycloak-theme/src/login/AuthShell.tsx`
- `keycloak-theme/src/login/LoginPage.tsx`
- `keycloak-theme/src/login/RegisterPage.tsx`
- `keycloak-theme/src/login/theme-overrides.css`
- `keycloak-theme/vite.config.ts`

What was done:

- Created a Keycloakify login theme project.
- Replaced default login look with fully custom React pages for `login.ftl` and `register.ftl`.
- Customized register form to show only `username`, `email`, `password`, and `password-confirm`.
- Added a modern split layout: brand panel + form panel on desktop.
- Added mobile responsive layout (single column) for phone/tablet.
- Kept Keycloak security behavior (same Keycloak form actions and validation rules).

Why this is better than simple CSS overrides:

- You control full UX structure, not only colors.
- Login and registration have consistent visual language.
- Still safe: credentials are submitted to Keycloak endpoints (not to custom backend logic).

Build theme:

```bash
cd keycloak-theme
npm install
npm run build-keycloak-theme
```

This creates theme JAR(s) under `keycloak-theme/dist_keycloak/`.

Use the one that matches your Keycloak version:

- `keycloak-theme-for-kc-22-to-25.jar`
- `keycloak-theme-for-kc-all-other-versions.jar`

---

## 7) Install theme into your existing Docker Keycloak

Example (replace container name if needed):

```bash
docker cp keycloak-theme/dist_keycloak/<matching-jar-file>.jar <keycloak-container-name>:/opt/keycloak/providers/
docker restart <keycloak-container-name>
```

Then in Keycloak admin:

1. Go to `Realm Settings` -> `Themes`.
2. Set `Login theme` to `oauth2-pkce-keycloak-theme`.
3. Save.
4. (For register page) Go to `Realm Settings` -> `Login` and enable `User registration`.

Now login and register screens should show your custom modern style.

---

## 8) How to run everything

### Backend

```bash
cd PKCE-flow
./mvnw.cmd spring-boot:run
```

### Frontend

```bash
cd frontend
npm install
npm run dev
```

Open `http://localhost:5173`, click login, then call `/api/home`.

---

## 9) Verification checklist

- Login redirects to Keycloak and returns back to React.
- Access token exists after login.
- `/api/home` returns `Hello <username>`.
- Without token, API returns 401.
- Keycloak login page uses your custom Keycloakify styling.
- Registration link opens a custom register page (not default theme).
- UI is usable on both desktop and mobile widths.

---

## 10) Common mistakes to avoid

- Using confidential client for SPA (wrong for browser app).
- Missing redirect URI or web origin in Keycloak client.
- Using wrong realm or issuer URL in backend.
- Forgetting token refresh before API calls.
- Forgetting to restart Keycloak container after adding theme JAR.
- Expecting register page while `User registration` is disabled in realm settings.

---

## 11) Keycloak admin checklist (username + email + password only)

Use this checklist when first/last name still appears in registration:

1. `Realm settings` -> `Themes` -> set `Login theme` = `oauth2-pkce-keycloak-theme`.
2. `Realm settings` -> `Login` -> enable `User registration`.
3. `Realm settings` -> `User profile` -> open `firstName` and `lastName` attributes.
4. Ensure `firstName` and `lastName` are not required for registration.
5. If you want separate username and email fields, keep `registrationEmailAsUsername` disabled.
6. Rebuild and redeploy the theme JAR, then restart the Keycloak container.
7. Open registration from React again and verify only username, email, password, and confirm password are shown.

---

## 12) Add social login (Google + GitHub)

This project already supports social buttons in the custom login page.
When identity providers are configured in Keycloak, buttons appear automatically.

### Keycloak setup

1. Go to `Identity providers` in realm `oauth2-demo`.
2. Add provider `Google`.
3. Add provider `GitHub`.
4. For each provider, copy the `Redirect URI` shown by Keycloak.
5. In Google/GitHub OAuth app settings, paste that exact URI as callback URL.
6. Copy provider `Client ID` and `Client Secret` back into Keycloak provider settings.
7. Save and enable the providers.

### OAuth app values

- GitHub OAuth App:
  - Homepage URL: `http://localhost:5173`
  - Authorization callback URL: use Keycloak GitHub redirect URI (commonly `http://localhost:8090/realms/oauth2-demo/broker/github/endpoint`)
- Google OAuth Client:
  - Authorized redirect URI: use Keycloak Google redirect URI (commonly `http://localhost:8090/realms/oauth2-demo/broker/google/endpoint`)

### Client settings reminder

For SPA + PKCE client `oauth2-pkce-demo`:

- `Client authentication` must be `Off` (public client).
- `Standard flow` must be `On`.
- Redirect URIs should include `http://localhost:5173/*`.

### Expected result

On Keycloak login and registration pages, you should see social buttons with provider icons:

- `Continue with Google`
- `Continue with GitHub`

On registration page, the social actions are shown as:

- `Sign up with Google`
- `Sign up with GitHub`

These still use Keycloak as the OAuth broker, then return to the same PKCE app flow.

---

## 13) RBAC with Keycloak realm roles

RBAC (Role-Based Access Control) is authorization, not authentication.

- Authentication answers: "Who is the user?"
- Authorization answers: "What is the user allowed to do?"

### Roles used in this project

- `USER`
- `ADMIN`

Spring Boot maps these realm roles from JWT claim `realm_access.roles` to Spring authorities:

- `USER` -> `ROLE_USER`
- `ADMIN` -> `ROLE_ADMIN`

The backend normalizes role names to uppercase before mapping. This means `user`, `User`, and `USER` are treated as the same role.

### Backend endpoint rules

- `/api/home`: any authenticated user
- `/api/user`: requires `USER` or `ADMIN`
- `/api/admin`: requires `ADMIN`

### Keycloak setup for roles

1. In realm `oauth2-demo`, create realm roles `USER` and `ADMIN`.
2. Assign roles to users in `Users` -> `<user>` -> `Role mapping`.
3. Add `USER` into `default-roles-oauth2-demo` so new registered/social users automatically get basic access.

### Frontend behavior

- React reads roles from `tokenParsed.realm_access.roles`.
- UI shows app roles (`USER`, `ADMIN`) and hides Keycloak internal roles like `offline_access` and `uma_authorization`.
- UI can call `/api/home`, `/api/user`, and `/api/admin` to verify RBAC behavior.
- `/api/user` and `/api/admin` buttons are disabled when the current token does not contain required roles.

### Expected API responses

- `401 Unauthorized`: no token or invalid token.
- `403 Forbidden`: valid token, but role is missing.
