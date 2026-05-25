	package com.example.pure_client;

	import org.springframework.beans.factory.annotation.Value;
	import org.springframework.boot.CommandLineRunner;
	import org.springframework.boot.SpringApplication;
	import org.springframework.boot.autoconfigure.SpringBootApplication;
	import org.springframework.context.annotation.Bean;
	import org.springframework.http.HttpEntity;
	import org.springframework.http.HttpHeaders;
	import org.springframework.http.HttpMethod;
	import org.springframework.security.oauth2.client.*;
	import org.springframework.security.oauth2.client.registration.ClientRegistration;
	import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
	import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository;
	import org.springframework.web.client.RestTemplate;

	@SpringBootApplication
	public class PureClientApplication {

		private final InMemoryClientRegistrationRepository clientRegistrationRepository;

		public PureClientApplication(InMemoryClientRegistrationRepository clientRegistrationRepository) {
			this.clientRegistrationRepository = clientRegistrationRepository;
		}

		public static void main(String[] args) {
			SpringApplication.run(PureClientApplication.class, args);
		}

		@Bean
		public RestTemplate restTemplate(){
			return new RestTemplate();
		}

		@Bean
		public OAuth2AuthorizedClientService oAuth2AuthorizedClientService(ClientRegistrationRepository clientRegistrationRepository){
			return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
		}

		@Bean
		public OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager(
				ClientRegistrationRepository repository,
				OAuth2AuthorizedClientService clientService
		){
			var manager = new AuthorizedClientServiceOAuth2AuthorizedClientManager(repository , clientService);

			OAuth2AuthorizedClientProvider provider = OAuth2AuthorizedClientProviderBuilder.builder()
					.clientCredentials()
					.build();

			manager.setAuthorizedClientProvider(provider);

			return manager;
		}

		@Bean
		public CommandLineRunner runner(
				OAuth2AuthorizedClientManager manager,
				RestTemplate rest,
				@Value("${service2.url}") String service2url
		){
			return args -> {
				var authRequest = OAuth2AuthorizeRequest
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

				System.out.println("Response from service 2" + response.getBody());


			};
		}

	}
