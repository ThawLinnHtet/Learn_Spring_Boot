import { type ReactElement, Suspense, useLayoutEffect, useMemo, useState } from "react";
import { getKcClsx } from "keycloakify/login/lib/kcClsx";
import { kcSanitize } from "keycloakify/lib/kcSanitize";
import type { UserProfileFormFieldsProps } from "keycloakify/login/UserProfileFormFieldsProps";
import type { ClassKey } from "keycloakify/login";
import type { LazyOrNot } from "keycloakify/tools/LazyOrNot";
import type { KcContext } from "./KcContext";
import type { I18n } from "./i18n";
import AuthShell from "./AuthShell";
import SocialProviderButtons from "./SocialProviderButtons";

type RegisterKcContext = Extract<KcContext, { pageId: "register.ftl" }>;

type RegisterPageProps = {
  kcContext: RegisterKcContext;
  i18n: I18n;
  UserProfileFormFields: LazyOrNot<(props: UserProfileFormFieldsProps) => ReactElement>;
  doMakeUserConfirmPassword: boolean;
  classes?: Partial<Record<ClassKey, string>>;
};

type SocialProvider = {
  loginUrl: string;
  alias: string;
  providerId: string;
  displayName: string;
  iconClasses?: string;
};

declare global {
  interface Window {
    onSubmitRecaptcha?: () => void;
  }
}

export default function RegisterPage(props: RegisterPageProps) {
  const { kcContext, i18n, UserProfileFormFields, doMakeUserConfirmPassword, classes } = props;
  const { msg, msgStr } = i18n;
  const socialProviders = (kcContext as RegisterKcContext & { social?: { providers?: SocialProvider[] } }).social?.providers;
  const {
    url,
    messagesPerField,
    recaptchaRequired,
    recaptchaVisible,
    recaptchaSiteKey,
    recaptchaAction,
    termsAcceptanceRequired
  } = kcContext;

  const { kcClsx } = getKcClsx({ doUseDefaultCss: false, classes });
  const filteredKcContext = useMemo<RegisterKcContext>(() => {
    return {
      ...kcContext,
      realm: {
        ...kcContext.realm,
        registrationEmailAsUsername: true
      },
      profile: {
        ...kcContext.profile,
        attributesByName: filterRegistrationAttributes(kcContext.profile.attributesByName)
      }
    };
  }, [kcContext]);

  const [isFormSubmittable, setIsFormSubmittable] = useState(false);
  const [areTermsAccepted, setAreTermsAccepted] = useState(false);

  useLayoutEffect(() => {
    window.onSubmitRecaptcha = () => {
      const form = document.getElementById("kc-register-form") as HTMLFormElement | null;
      form?.requestSubmit();
    };

    return () => {
      delete window.onSubmitRecaptcha;
    };
  }, []);

  const canSubmit =
    isFormSubmittable && (!termsAcceptanceRequired || areTermsAccepted);

  return (
    <AuthShell
      badge="Account Creation"
      title={msgStr("registerTitle")}
      subtitle="Create your account to access secure PKCE-powered features."
      footer={
        <p className="auth-foot-copy">
          Already have an account? <a href={url.loginUrl}>{msgStr("doLogIn")}</a>
        </p>
      }
    >
      {messagesPerField.exists("global") ? (
        <p
          className="auth-error"
          aria-live="polite"
          dangerouslySetInnerHTML={{ __html: kcSanitize(messagesPerField.get("global")) }}
        />
      ) : null}

      <form
        id="kc-register-form"
        className="auth-form auth-register-form"
        action={url.registrationAction}
        method="post"
      >
        <Suspense>
          <UserProfileFormFields
            kcContext={filteredKcContext}
            i18n={i18n}
            kcClsx={kcClsx}
            onIsFormSubmittableValueChange={setIsFormSubmittable}
            doMakeUserConfirmPassword={doMakeUserConfirmPassword}
          />
        </Suspense>

        {termsAcceptanceRequired ? (
          <div className="auth-terms">
            <p className="auth-terms-title">{msg("termsTitle")}</p>
            <div id="kc-registration-terms-text" className="auth-terms-text">
              {msg("termsText")}
            </div>
            <label className="auth-check auth-check-wide" htmlFor="termsAccepted">
              <input
                type="checkbox"
                id="termsAccepted"
                name="termsAccepted"
                checked={areTermsAccepted}
                onChange={(event) => setAreTermsAccepted(event.target.checked)}
                aria-invalid={messagesPerField.existsError("termsAccepted")}
              />
              <span>{msg("acceptTerms")}</span>
            </label>
            {messagesPerField.existsError("termsAccepted") ? (
              <p
                className="auth-error"
                aria-live="polite"
                dangerouslySetInnerHTML={{ __html: kcSanitize(messagesPerField.get("termsAccepted")) }}
              />
            ) : null}
          </div>
        ) : null}

        {recaptchaRequired && (recaptchaVisible || recaptchaAction === undefined) ? (
          <div className="auth-recaptcha-wrap">
            <div className="g-recaptcha" data-size="compact" data-sitekey={recaptchaSiteKey} data-action={recaptchaAction} />
          </div>
        ) : null}

        {recaptchaRequired && !recaptchaVisible && recaptchaAction !== undefined ? (
          <button
            className="auth-primary-btn g-recaptcha"
            data-sitekey={recaptchaSiteKey}
            data-callback="onSubmitRecaptcha"
            data-action={recaptchaAction}
            type="submit"
            disabled={!canSubmit}
          >
            {msgStr("doRegister")}
          </button>
        ) : (
          <button className="auth-primary-btn" type="submit" disabled={!canSubmit}>
            {msgStr("doRegister")}
          </button>
        )}

        <SocialProviderButtons
          providers={socialProviders}
          sectionLabel={msgStr("identity-provider-login-label")}
          action="signup"
        />
      </form>
    </AuthShell>
  );
}

function filterRegistrationAttributes(attributesByName: RegisterKcContext["profile"]["attributesByName"]) {
  const allowedAttributeNames = new Set(["username", "email", "locale"]);

  return Object.fromEntries(
    Object.entries(attributesByName).filter(([attributeName]) => allowedAttributeNames.has(attributeName))
  );
}
