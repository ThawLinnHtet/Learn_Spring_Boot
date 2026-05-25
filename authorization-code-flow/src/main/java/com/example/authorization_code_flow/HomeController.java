package com.example.authorization_code_flow;

import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.annotation.RegisteredOAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home(OAuth2AuthenticationToken token,
    @RegisteredOAuth2AuthorizedClient("oauth2-authorization-code-flow")
    OAuth2AuthorizedClient client){
        String name = token.getPrincipal().getAttribute("name");
        String email = token.getPrincipal().getAttribute("email");
        String accessToken = client.getAccessToken().getTokenValue();
        return "Hello welcome " + name + "!." + "This is your email " + email + accessToken;
    }
}
