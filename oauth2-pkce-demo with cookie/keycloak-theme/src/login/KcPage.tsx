import { Suspense, lazy } from "react";
import type { ClassKey } from "keycloakify/login";
import type { KcContext } from "./KcContext";
import { useI18n } from "./i18n";
import DefaultPage from "keycloakify/login/DefaultPage";
import Template from "keycloakify/login/Template";
import LoginPage from "./LoginPage";
import RegisterPage from "./RegisterPage";
import "./theme-overrides.css";

const UserProfileFormFields = lazy(() => import("keycloakify/login/UserProfileFormFields"));

export default function KcPage(props: { kcContext: KcContext }) {
  const { kcContext } = props;
  const { i18n } = useI18n({ kcContext });

  if (kcContext.pageId === "login.ftl") {
    return <LoginPage kcContext={kcContext} i18n={i18n} />;
  }

  if (kcContext.pageId === "register.ftl") {
    return (
      <RegisterPage
        kcContext={kcContext}
        i18n={i18n}
        UserProfileFormFields={UserProfileFormFields}
        doMakeUserConfirmPassword={true}
        classes={classes}
      />
    );
  }

  return (
    <Suspense>
      <DefaultPage
        kcContext={kcContext}
        i18n={i18n}
        classes={classes}
        Template={Template}
        doUseDefaultCss={false}
        UserProfileFormFields={UserProfileFormFields}
        doMakeUserConfirmPassword={true}
      />
    </Suspense>
  );
}

const classes = {
  kcBodyClass: "pkce-body",
  kcLoginClass: "pkce-login",
  kcFormCardClass: "pkce-card",
  kcHeaderClass: "pkce-header",
  kcFormGroupClass: "auth-field",
  kcLabelClass: "auth-label",
  kcInputClass: "auth-input",
  kcInputErrorMessageClass: "auth-error",
  kcInputWrapperClass: "auth-input-wrap",
  kcFormButtonsClass: "auth-actions",
  kcButtonClass: "auth-btn",
  kcButtonPrimaryClass: "auth-primary-btn",
  kcButtonBlockClass: "auth-btn-block",
  kcButtonLargeClass: "auth-btn-large"
} satisfies { [key in ClassKey]?: string };
