import { useEffect, useMemo, useState } from "react";

const apiBaseUrl = import.meta.env.VITE_API_BASE_URL;

const defaultSession = {
  authenticated: false,
  username: "Guest",
  roles: [],
  loginUrl: "/api/auth/login",
  registerUrl: "/api/auth/register",
  logoutUrl: "/api/auth/logout",
  csrfHeaderName: "X-XSRF-TOKEN",
  csrfParameterName: "_csrf"
};

function getCookieValue(cookieName) {
  const match = document.cookie
    .split("; ")
    .find((cookie) => cookie.startsWith(`${cookieName}=`));

  if (!match) {
    return "";
  }

  return decodeURIComponent(match.slice(cookieName.length + 1));
}

function buildApiUrl(path) {
  return `${apiBaseUrl}${path}`;
}

function App() {
  const [ready, setReady] = useState(false);
  const [session, setSession] = useState(defaultSession);
  const [error, setError] = useState("");
  const [apiMessage, setApiMessage] = useState("");

  async function loadSession() {
    const response = await fetch(buildApiUrl("/api/session"), {
      credentials: "include"
    });

    if (!response.ok) {
      throw new Error(`Session request failed with status ${response.status}`);
    }

    const sessionResponse = await response.json();
    setSession({ ...defaultSession, ...sessionResponse });
  }

  useEffect(() => {
    let mounted = true;

    loadSession()
      .catch(() => {
        if (!mounted) {
          return;
        }
        setError("Failed to load session from backend.");
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

  const authenticated = session.authenticated;
  const username = session.username;
  const roles = session.roles;

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
    window.location.assign(buildApiUrl(session.loginUrl));
  }

  async function register() {
    setError("");
    window.location.assign(buildApiUrl(session.registerUrl));
  }

  async function logout() {
    setError("");

    try {
      const csrfToken = getCookieValue("XSRF-TOKEN");
      if (!csrfToken) {
        throw new Error("Missing CSRF token cookie. Refresh page and try again.");
      }

      const form = document.createElement("form");
      form.method = "POST";
      form.action = buildApiUrl(session.logoutUrl);

      const csrfInput = document.createElement("input");
      csrfInput.type = "hidden";
      csrfInput.name = session.csrfParameterName || "_csrf";
      csrfInput.value = csrfToken;
      form.appendChild(csrfInput);

      document.body.appendChild(form);
      form.submit();
    } catch (authError) {
      setError(getErrorMessage(authError, "Logout failed."));
    }
  }

  async function callApi(path) {
    setApiMessage("");
    setError("");

    try {
      const response = await fetch(buildApiUrl(path), {
        credentials: "include"
      });

      if (!response.ok) {
        if (response.status === 401) {
          throw new Error("Not authenticated. Please login first.");
        }
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
          This app now uses a BFF session cookie. React never stores access tokens
          and calls Spring Boot using HttpOnly cookie authentication.
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
