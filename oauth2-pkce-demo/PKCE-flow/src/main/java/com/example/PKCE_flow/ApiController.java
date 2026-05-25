package com.example.PKCE_flow;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ApiController {

    @GetMapping("/api/home")
    public String home(@AuthenticationPrincipal Jwt jwt) {
        return "Hello " + getUsername(jwt);
    }

    @GetMapping("/api/user")
    public String userEndpoint(@AuthenticationPrincipal Jwt jwt) {
        return "User endpoint access granted to " + getUsername(jwt);
    }

    @GetMapping("/api/admin")
    public String adminEndpoint(@AuthenticationPrincipal Jwt jwt) {
        return "Admin endpoint access granted to " + getUsername(jwt);
    }

    private String getUsername(Jwt jwt) {
        String preferredUsername = jwt.getClaim("preferred_username");
        return preferredUsername != null ? preferredUsername : "Unknown";
    }
}
