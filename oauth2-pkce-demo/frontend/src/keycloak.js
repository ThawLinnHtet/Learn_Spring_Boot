import Keycloak from "keycloak-js";

const keycloak = new Keycloak({
  url: import.meta.env.VITE_KEYCLOAK_URL,
  realm: import.meta.env.VITE_KEYCLOAK_REALM,
  clientId: import.meta.env.VITE_KEYCLOAK_CLIENT_ID
});

let initPromise;

export async function initKeycloak() {
  if (initPromise) {
    return initPromise;
  }

  initPromise = keycloak
    .init({
      onLoad: "check-sso",
      pkceMethod: "S256",
      checkLoginIframe: false
    })
    .catch((error) => {
      initPromise = undefined;
      throw error;
    });

  return initPromise;
}

export default keycloak;
