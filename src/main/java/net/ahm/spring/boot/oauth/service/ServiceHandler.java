package net.ahm.spring.boot.oauth.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServiceHandler {

	@Autowired
	WebClient webClient;

	public String getCMSurveys() throws InterruptedException, ExecutionException {
		String url = "API Endpoint URL";
		Map<String, String> uriMaps = new HashMap<>();
		CompletableFuture<String> bodyToMono = webClient.post().uri(url, uriMaps).retrieve().bodyToMono(String.class)
				.toFuture();
		log.info("Response : " + bodyToMono.get());
		return bodyToMono.get();
	}
}
