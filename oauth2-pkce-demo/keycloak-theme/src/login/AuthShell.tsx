import { type ReactNode, useEffect } from "react";

type AuthShellProps = {
  title: string;
  subtitle: string;
  children: ReactNode;
  footer?: ReactNode;
  badge?: string;
};

export default function AuthShell(props: AuthShellProps) {
  const { title, subtitle, children, footer, badge = "Secure Access" } = props;

  useEffect(() => {
    document.body.classList.add("pkce-body");

    return () => {
      document.body.classList.remove("pkce-body");
    };
  }, []);

  return (
    <div className="auth-root">
      <aside className="auth-brand-panel" aria-hidden="true">
        <div className="auth-brand-inner">
          <p className="auth-brand-badge">{badge}</p>
          <h1 className="auth-brand-title">OAuth2 PKCE Platform</h1>
          <p className="auth-brand-copy">
            Fast and secure sign-in for your React and Spring Boot apps with Keycloak.
          </p>
          <ul className="auth-brand-points">
            <li>Authorization code flow with PKCE</li>
            <li>Token-based API protection</li>
            <li>Consistent identity experience</li>
          </ul>
        </div>
      </aside>

      <main className="auth-main-panel">
        <section className="auth-card" role="region" aria-label={title}>
          <header className="auth-card-head">
            <p className="auth-kicker">Identity</p>
            <h2 className="auth-title">{title}</h2>
            <p className="auth-subtitle">{subtitle}</p>
          </header>

          <div className="auth-card-body">{children}</div>

          {footer ? <footer className="auth-card-foot">{footer}</footer> : null}
        </section>
      </main>
    </div>
  );
}
