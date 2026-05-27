# Implementation Plan

## Goal
Convert the current React + Keycloak PKCE + Spring Boot setup into a BFF-style architecture that uses HttpOnly session cookies, keeps PKCE, preserves custom Keycloakify login/register pages, and applies CSRF protection.

## Confirmed Local Assumptions
- Keycloak is already running locally in Docker.
- Keycloak realm: `oauth2-demo`.
- Keycloak client ID for SPA: `oauth2-pkce-demo`.
- Keycloak base URL: `http://localhost:8090`.

## Work Steps
1. Convert Spring Boot from pure JWT resource server to OAuth2 Login BFF with session-based authentication.
2. Configure Spring OAuth2 client against Keycloak using Authorization Code flow with PKCE.
3. Keep role-based authorization (`USER`/`ADMIN`) by extracting roles from OIDC tokens in Spring Security.
4. Enable CORS with credentials for React dev origin and enable CSRF with cookie token repository.
5. Add BFF endpoints for session status, login redirect, register redirect, and logout.
6. Refactor React to remove browser-held bearer tokens and use cookie-based API calls (`credentials: include`).
7. Preserve current Keycloakify custom login/register UI by continuing to authenticate through Keycloak pages.
8. Update junior-friendly explanation in `docs/explanation.md` for BFF, HttpOnly cookies, PKCE, and CSRF.

## Verification Steps
- Backend: run tests and package build in `PKCE-flow/`.
- Frontend: install deps and run production build in `frontend/`.
- Manual flow: open React app, click login, authenticate on custom Keycloak UI, return to React, call protected API without bearer token.
- Cookie check: ensure browser stores `JSESSIONID` as HttpOnly and uses it for API calls.
- CSRF check: verify logout/state-changing requests require valid `X-XSRF-TOKEN`.
