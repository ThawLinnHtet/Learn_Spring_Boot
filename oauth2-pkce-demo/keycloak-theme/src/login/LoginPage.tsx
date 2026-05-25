import { type ReactNode, useState } from "react";
import { kcSanitize } from "keycloakify/lib/kcSanitize";
import { useIsPasswordRevealed } from "keycloakify/tools/useIsPasswordRevealed";
import type { KcContext } from "./KcContext";
import type { I18n } from "./i18n";
import AuthShell from "./AuthShell";
import SocialProviderButtons from "./SocialProviderButtons";

type LoginKcContext = Extract<KcContext, { pageId: "login.ftl" }>;

export default function LoginPage(props: { kcContext: LoginKcContext; i18n: I18n }) {
  const { kcContext, i18n } = props;
  const { msgStr } = i18n;
  const {
    social,
    realm,
    url,
    usernameHidden,
    login,
    auth,
    registrationDisabled,
    messagesPerField
  } = kcContext;

  const [isLoginButtonDisabled, setIsLoginButtonDisabled] = useState(false);
  const usernameError = messagesPerField.existsError("username", "password")
    ? messagesPerField.getFirstError("username", "password")
    : "";

  return (
    <AuthShell
      badge="Trusted Authentication"
      title={msgStr("loginAccountTitle")}
      subtitle="Use your Keycloak account to continue to the application."
      footer={
        realm.password && realm.registrationAllowed && !registrationDisabled ? (
          <p className="auth-foot-copy">
            {msgStr("noAccount")} <a href={url.registrationUrl}>{msgStr("doRegister")}</a>
          </p>
        ) : null
      }
    >
      <form
        id="kc-form-login"
        className="auth-form"
        action={url.loginAction}
        method="post"
        onSubmit={() => {
          setIsLoginButtonDisabled(true);
          return true;
        }}
      >
        {!usernameHidden ? (
          <div className="auth-field">
            <label htmlFor="username" className="auth-label">
              {!realm.loginWithEmailAllowed
                ? msgStr("username")
                : !realm.registrationEmailAsUsername
                  ? msgStr("usernameOrEmail")
                  : msgStr("email")}
            </label>
            <input
              id="username"
              name="username"
              className="auth-input"
              type="text"
              defaultValue={login.username ?? ""}
              autoFocus
              autoComplete="username"
              aria-invalid={messagesPerField.existsError("username", "password")}
            />
          </div>
        ) : null}

        <div className="auth-field">
          <label htmlFor="password" className="auth-label">
            {msgStr("password")}
          </label>
          <PasswordInput passwordInputId="password" i18n={i18n}>
            <input
              id="password"
              className="auth-input"
              name="password"
              type="password"
              autoComplete="current-password"
              aria-invalid={messagesPerField.existsError("username", "password")}
            />
          </PasswordInput>
        </div>

        {usernameError ? (
          <p
            className="auth-error"
            aria-live="polite"
            dangerouslySetInnerHTML={{ __html: kcSanitize(usernameError) }}
          />
        ) : null}

        <div className="auth-row">
          {realm.rememberMe && !usernameHidden ? (
            <label className="auth-check">
              <input id="rememberMe" name="rememberMe" type="checkbox" defaultChecked={!!login.rememberMe} />
              <span>{msgStr("rememberMe")}</span>
            </label>
          ) : (
            <span />
          )}

          {realm.resetPasswordAllowed ? (
            <a className="auth-link" href={url.loginResetCredentialsUrl}>
              {msgStr("doForgotPassword")}
            </a>
          ) : null}
        </div>

        <input type="hidden" id="id-hidden-input" name="credentialId" value={auth.selectedCredential} />
        <button type="submit" className="auth-primary-btn" disabled={isLoginButtonDisabled}>
          {msgStr("doLogIn")}
        </button>
      </form>

      {realm.password ? (
        <SocialProviderButtons
          providers={social?.providers}
          sectionLabel={msgStr("identity-provider-login-label")}
          action="continue"
        />
      ) : null}
    </AuthShell>
  );
}

function PasswordInput(props: { passwordInputId: string; i18n: I18n; children: ReactNode }) {
  const { passwordInputId, i18n, children } = props;
  const { msgStr } = i18n;
  const { isPasswordRevealed, toggleIsPasswordRevealed } = useIsPasswordRevealed({ passwordInputId });

  return (
    <div className="auth-password-wrap">
      {children}
      <button
        type="button"
        className="auth-ghost-btn"
        aria-label={msgStr(isPasswordRevealed ? "hidePassword" : "showPassword")}
        aria-controls={passwordInputId}
        onClick={toggleIsPasswordRevealed}
      >
        {isPasswordRevealed ? msgStr("hidePassword") : msgStr("showPassword")}
      </button>
    </div>
  );
}
