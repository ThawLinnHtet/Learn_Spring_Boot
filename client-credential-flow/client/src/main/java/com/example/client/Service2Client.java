package com.example.client;


import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class Service2Client {
    private final RestTemplate rest;
    private final OAuth2AuthorizedClientManager manager;

    public Service2Client(RestTemplate rest, OAuth2AuthorizedClientManager manager) {
        this.rest = rest;
        this.manager = manager;
    }

    @Value("${service2.url}")
    String service2url;

    public String fetchData(){
        /*var authRequest = OAuth2AuthorizeRequest
                .withClientRegistrationId("keycloak-client")
                .principal("machine")
                .build();
        var client = manager.authorize(authRequest);

        if (client == null) {
            throw new IllegalStateException("Cannot authorize OAuth2 client. Check client id, secret, token-uri, and grant type.");
        }

        String token = client.getAccessToken().getTokenValue();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);

        var response = rest.exchange(
                service2url + "/data",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        return response.getBody(); */

        @Nullable Authentication auth = SecurityContextHolder.getContext().
                getAuthentication();

        String incomingToken = null;

        if(auth instanceof JwtAuthenticationToken jwtAuthenticationToken){
           incomingToken = jwtAuthenticationToken.getToken().getTokenValue();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(incomingToken);

        var response = rest.exchange(
                service2url + "/data",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                String.class
        );

        return response.getBody();
    }

}
