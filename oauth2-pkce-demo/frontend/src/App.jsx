import { useEffect, useMemo, useState } from "react";
import keycloak, { initKeycloak } from "./keycloak";

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;

function App() {
  const [ready, setReady] = useState(false);
  const [authenticated, setAuthenticated] = useState(false);
  const [error, setError] = useState("");
  const [apiMessage, setApiMessage] = useState("");

  useEffect(() => {
    let mounted = true;

    initKeycloak()
      .then((isAuthenticated) => {
        if (!mounted) {
          return;
        }
        setAuthenticated(isAuthenticated);
      })
      .catch(() => {
        if (!mounted) {
          return;
        }
        setError("Failed to initialize Keycloak. Check your env values.");
      })
      .finally(() => {
        if (mounted) {
          setReady(true);
        }
      });

    return () => {
      mounted = false;
    };
  }, []);

  const username = useMemo(() => {
    return keycloak.tokenParsed?.preferred_username ?? "Guest";
  }, [authenticated]);

  const roles = useMemo(() => {
    const parsedRoles = keycloak.tokenParsed?.realm_access?.roles;
    return Array.isArray(parsedRoles) ? parsedRoles : [];
  }, [authenticated]);

  const appRoles = useMemo(() => {
    return [...new Set(
      roles
        .filter((role) => typeof role === "string")
        .map((role) => role.trim().toUpperCase())
        .filter((role) => role === "USER" || role === "ADMIN")
    )];
  }, [roles]);

  const hasUserAccess = appRoles.includes("USER") || appRoles.includes("ADMIN");
  const hasAdminAccess = appRoles.includes("ADMIN");

  function getErrorMessage(error, fallbackMessage) {
    if (error instanceof Error && error.message) {
      return error.message;
    }

    return fallbackMessage;
  }

  async function login() {
    setError("");

    try {
      await keycloak.login({ redirectUri: window.location.origin });
    } catch (authError) {
      setError(getErrorMessage(authError, "Login redirect failed."));
    }
  }

  async function register() {
    setError("");

    try {
      await keycloak.register({ redirectUri: window.location.origin });
    } catch (authError) {
      setError(getErrorMessage(authError, "Registration redirect failed."));
    }
  }

  async function logout() {
    await keycloak.logout({ redirectUri: window.location.origin });
  }

  async function callApi(path) {
    setApiMessage("");
    setError("");

    try {
      await keycloak.updateToken(30);

      const response = await fetch(`${apiBaseUrl}${path}`, {
        headers: {
          Authorization: `Bearer ${keycloak.token}`
        }
      });

      if (!response.ok) {
        if (response.status === 403) {
          throw new Error("Access denied: you are logged in but missing the required role.");
        }
        throw new Error(`Request failed with status ${response.status}`);
      }

      const text = await response.text();
      setApiMessage(`${path}: ${text}`);
    } catch (requestError) {
      setError(getErrorMessage(requestError, `Request to ${path} failed.`));
    }
  }

  if (!ready) {
    return <main className="app">Initializing authentication...</main>;
  }

  return (
    <main className="app">
      <section className="card">
        <h1>React + Keycloak PKCE Demo</h1>
        <p className="hint">
          This SPA uses Authorization Code Flow with PKCE and calls a protected
          Spring Boot API.
        </p>

        <p>
          <strong>Status:</strong> {authenticated ? "Authenticated" : "Not authenticated"}
        </p>
        <p>
          <strong>User:</strong> {username}
        </p>
        <p>
          <strong>App roles:</strong> {appRoles.length ? appRoles.join(", ") : "(none)"}
        </p>

        <div className="actions">
          {!authenticated ? (
            <>
              <button onClick={login}>Login with Keycloak</button>
              <button className="secondary" onClick={register}>
                Register with Keycloak
              </button>
            </>
          ) : (
            <>
              <button onClick={() => callApi("/api/home")}>Call /api/home</button>
              <button
                className="secondary"
                onClick={() => callApi("/api/user")}
                disabled={!hasUserAccess}
                title={hasUserAccess ? "" : "Requires USER or ADMIN role"}
              >
                Call /api/user (USER or ADMIN)
              </button>
              <button
                className="secondary"
                onClick={() => callApi("/api/admin")}
                disabled={!hasAdminAccess}
                title={hasAdminAccess ? "" : "Requires ADMIN role"}
              >
                Call /api/admin (ADMIN only)
              </button>
              <button className="secondary" onClick={logout}>
                Logout
              </button>
            </>
          )}
        </div>

        {apiMessage ? <p className="ok">API response: {apiMessage}</p> : null}
        {error ? <p className="error">{error}</p> : null}
      </section>
    </main>
  );
}

export default App;
