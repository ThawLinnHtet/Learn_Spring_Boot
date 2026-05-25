type SocialProvider = {
  loginUrl: string;
  alias: string;
  displayName: string;
};

type SocialProviderButtonsProps = {
  providers: SocialProvider[] | undefined;
  sectionLabel: string;
  action: "continue" | "signup";
};

export default function SocialProviderButtons(props: SocialProviderButtonsProps) {
  const { providers, sectionLabel, action } = props;

  if (!providers?.length) {
    return null;
  }

  return (
    <section className="auth-social" aria-label={sectionLabel}>
      <div className="auth-divider">
        <span>{sectionLabel}</span>
      </div>
      <div className="auth-social-grid">
        {providers.map((provider) => {
          const normalizedAlias = provider.alias.toLowerCase();

          return (
            <a
              key={provider.alias}
              href={provider.loginUrl}
              className="auth-social-btn"
              data-provider={provider.alias}
            >
              <SocialIcon alias={normalizedAlias} />
              <span>{getSocialProviderLabel(normalizedAlias, provider.displayName, action)}</span>
            </a>
          );
        })}
      </div>
    </section>
  );
}

function getSocialProviderLabel(alias: string, displayName: string, action: "continue" | "signup"): string {
  const verb = action === "signup" ? "Sign up with" : "Continue with";

  if (alias === "google") {
    return `${verb} Google`;
  }

  if (alias === "github") {
    return `${verb} GitHub`;
  }

  return `${verb} ${displayName}`;
}

function SocialIcon(props: { alias: string }) {
  const { alias } = props;

  if (alias === "google") {
    return (
      <svg className="auth-social-icon auth-social-icon--google" viewBox="0 0 24 24" aria-hidden="true">
        <path d="M21.35 11.1H12v2.98h5.35c-.23 1.48-1.72 4.35-5.35 4.35a6.04 6.04 0 1 1 0-12.08c2.07 0 3.46.88 4.26 1.64l2.9-2.8C17.3 3.48 14.95 2.5 12 2.5a9.5 9.5 0 1 0 0 19c5.48 0 9.1-3.85 9.1-9.28 0-.62-.07-.94-.15-1.12Z" fill="#4285F4"/>
        <path d="M3.54 7.58 6 9.38A6.03 6.03 0 0 1 12 6.35c2.07 0 3.46.88 4.26 1.64l2.9-2.8C17.3 3.48 14.95 2.5 12 2.5c-3.64 0-6.78 2.08-8.46 5.08Z" fill="#EA4335"/>
        <path d="M12 21.5c2.88 0 5.3-.95 7.08-2.58l-3.27-2.67c-.88.6-2.08 1.18-3.81 1.18A6.04 6.04 0 0 1 6.2 13.3l-2.53 1.95A9.5 9.5 0 0 0 12 21.5Z" fill="#34A853"/>
        <path d="M3.54 7.58A9.4 9.4 0 0 0 2.5 12c0 1.55.37 3.02 1.03 4.25L6.2 13.3a6.1 6.1 0 0 1-.31-1.3c0-.45.1-.88.28-1.29L3.54 7.58Z" fill="#FBBC05"/>
      </svg>
    );
  }

  if (alias === "github") {
    return (
      <svg className="auth-social-icon auth-social-icon--github" viewBox="0 0 24 24" aria-hidden="true">
        <path fill="currentColor" d="M12 2.5a9.5 9.5 0 0 0-3 18.52c.48.1.65-.2.65-.46v-1.6c-2.64.58-3.2-1.27-3.2-1.27-.44-1.09-1.06-1.38-1.06-1.38-.86-.59.07-.58.07-.58.96.07 1.46.97 1.46.97.85 1.45 2.24 1.03 2.78.79.08-.62.34-1.04.62-1.28-2.1-.24-4.3-1.05-4.3-4.66 0-1.03.37-1.88.97-2.54-.1-.24-.42-1.2.1-2.5 0 0 .79-.25 2.58.97a8.9 8.9 0 0 1 4.7 0c1.79-1.22 2.58-.97 2.58-.97.52 1.3.2 2.26.1 2.5.6.66.97 1.51.97 2.54 0 3.62-2.2 4.42-4.3 4.65.35.3.67.9.67 1.84v2.73c0 .26.17.56.66.46A9.5 9.5 0 0 0 12 2.5Z"/>
      </svg>
    );
  }

  return (
    <svg className="auth-social-icon auth-social-icon--default" viewBox="0 0 24 24" aria-hidden="true">
      <circle cx="12" cy="12" r="9" fill="currentColor" />
    </svg>
  );
}
