package net.ahm.spring.boot.oauth.config;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ClientHttpConnector;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.InMemoryReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.InMemoryReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.web.reactive.function.client.WebClient;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import reactor.netty.http.client.HttpClient;
import reactor.netty.tcp.ProxyProvider;

@Configuration
public class ConfluentKafkaConfig {

	@Bean
	ReactiveClientRegistrationRepository getRegistration() {
		String scope = "scope";
		String[] scopes = scope.split(":");
		ClientRegistration registration = ClientRegistration.withRegistrationId("client-api").tokenUri("token url")
				.clientId("clientid").clientSecret("secret")
				.authorizationGrantType(AuthorizationGrantType.CLIENT_CREDENTIALS).scope(Arrays.asList(scopes)).build();
		return new InMemoryReactiveClientRegistrationRepository(registration);
	}

	@Bean
	WebClient webClient(ReactiveClientRegistrationRepository clientRegistrations) {

		InMemoryReactiveOAuth2AuthorizedClientService authorizedClientService = new InMemoryReactiveOAuth2AuthorizedClientService(
				clientRegistrations);
		ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2FilterFunction = new ServerOAuth2AuthorizedClientExchangeFilterFunction(
				new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrations,
						authorizedClientService));

		oauth2FilterFunction.setDefaultClientRegistrationId("client-api");

		HttpClient httpClient = HttpClient.create()
				.tcpConfiguration(tcpClient -> tcpClient.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
						.doOnConnected(conn -> conn.addHandlerLast(new ReadTimeoutHandler(2000, TimeUnit.MILLISECONDS)))
						.proxy(proxy -> proxy.type(ProxyProvider.Proxy.HTTP).host("yourproxyhost").port(8080)));

		ClientHttpConnector connector = new ReactorClientHttpConnector(httpClient);
		return WebClient.builder().clientConnector(connector).filter(oauth2FilterFunction).build();
	}

}
