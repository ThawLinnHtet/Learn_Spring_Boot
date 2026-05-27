package com.example.PKCE_flow;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;

@RestController
public class AuthController {

    @GetMapping("/api/auth/login")
    public RedirectView login() {
        return new RedirectView("/oauth2/authorization/keycloak");
    }

    @GetMapping("/api/auth/register")
    public RedirectView register() {
        return new RedirectView("/oauth2/authorization/keycloak?kc_register=true");
    }

    @GetMapping("/api/session")
    public SessionResponse session(Authentication authentication, CsrfToken csrfToken) {
        if (csrfToken != null) {
            csrfToken.getToken();
        }

        String csrfHeaderName = csrfToken != null ? csrfToken.getHeaderName() : "X-XSRF-TOKEN";
        String csrfParameterName = csrfToken != null ? csrfToken.getParameterName() : "_csrf";

        if (authentication == null || !authentication.isAuthenticated() || authentication instanceof AnonymousAuthenticationToken) {
            return new SessionResponse(false, "Guest", List.of(), "/api/auth/login", "/api/auth/register", "/api/auth/logout", csrfHeaderName, csrfParameterName);
        }

        String username = extractUsername(authentication);
        List<String> appRoles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(authority -> authority.startsWith("ROLE_"))
                .map(authority -> authority.substring(5))
                .filter(role -> role.equals("USER") || role.equals("ADMIN"))
                .sorted()
                .toList();

        return new SessionResponse(true, username, appRoles, "/api/auth/login", "/api/auth/register", "/api/auth/logout", csrfHeaderName, csrfParameterName);
    }

    private String extractUsername(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof OidcUser oidcUser) {
            String preferredUsername = oidcUser.getPreferredUsername();
            if (preferredUsername != null && !preferredUsername.isBlank()) {
                return preferredUsername;
            }
        }

        return authentication.getName();
    }

    public record SessionResponse(
            boolean authenticated,
            String username,
            List<String> roles,
            String loginUrl,
            String registerUrl,
            String logoutUrl,
            String csrfHeaderName,
            String csrfParameterName
    ) {
    }
}
