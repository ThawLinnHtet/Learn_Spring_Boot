# Implementation Plan

## Goal
Build authentication using React SPA + Keycloak Authorization Code Flow with PKCE + Spring Boot resource server, and customize Keycloak login UI with Keycloakify.

## Confirmed Local Assumptions
- Keycloak is already running locally in Docker.
- Keycloak realm: `oauth2-demo`.
- Keycloak client ID for SPA: `oauth2-pkce-demo`.
- Keycloak base URL: `http://localhost:8090`.

## Work Steps
1. Update backend security config for JWT resource server + CORS for React dev server.
2. Keep issuer configurable via environment variable.
3. Create React frontend that uses `keycloak-js` with PKCE (`S256`) for login.
4. Add token-aware API calls from React to Spring Boot protected endpoint.
5. Create Keycloakify-based custom login theme project.
6. Add Docker usage instructions to install the built Keycloakify theme into existing Keycloak container.
7. Add junior-friendly explanation in `docs/explanation.md`.

## Verification Steps
- Backend: run tests and package build in `PKCE-flow/`.
- Frontend: install deps and run production build in `frontend/`.
- Theme: install deps and run Keycloakify build in `keycloak-theme/`.
- Manual flow: login from React, redirect to Keycloak custom UI, return to React, call `/api/home` successfully.
