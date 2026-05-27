package com.example.PKCE_flow;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

    @GetMapping("/api/home")
    public String home(Authentication authentication) {
        return "Hello " + getUsername(authentication);
    }

    @GetMapping("/api/user")
    public String userEndpoint(Authentication authentication) {
        return "User endpoint access granted to " + getUsername(authentication);
    }

    @GetMapping("/api/admin")
    public String adminEndpoint(Authentication authentication) {
        return "Admin endpoint access granted to " + getUsername(authentication);
    }

    private String getUsername(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "Unknown";
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof OidcUser oidcUser) {
            String preferredUsername = oidcUser.getPreferredUsername();
            if (preferredUsername != null && !preferredUsername.isBlank()) {
                return preferredUsername;
            }
        }

        return authentication.getName();
    }
}
