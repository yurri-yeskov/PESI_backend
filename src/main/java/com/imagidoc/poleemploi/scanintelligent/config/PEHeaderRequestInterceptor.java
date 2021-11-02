package com.imagidoc.poleemploi.scanintelligent.config;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

public class PEHeaderRequestInterceptor implements ClientHttpRequestInterceptor {

	private ApplicationProperties properties;

	public PEHeaderRequestInterceptor(ApplicationProperties properties) {
		this.properties = properties;
	}

	@Override
	public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
    	request.getHeaders().set("pe-id-environnement", properties.getPeIdEnvironnement());
    	request.getHeaders().set("pe-id-correlation", UUID.randomUUID().toString());
    	request.getHeaders().set("pe-id-utilisateur", properties.getPeIdUtilisateur());
    	request.getHeaders().set("pe-nom-application", properties.getPeNomApplication());
		return execution.execute(request, body);
	}
}
