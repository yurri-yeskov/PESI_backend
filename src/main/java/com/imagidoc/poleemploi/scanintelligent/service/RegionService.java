package com.imagidoc.poleemploi.scanintelligent.service;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.imagidoc.poleemploi.scanintelligent.config.ApplicationProperties;
import com.imagidoc.poleemploi.scanintelligent.config.PEHeaderRequestInterceptor;
import com.imagidoc.poleemploi.scanintelligent.web.rest.vm.RegionVM;

@Service
public class RegionService {

	private static final Map<String, RegionVM> REGION_REFERENCE = new HashMap<>();
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegionService.class);

	private final ApplicationProperties properties;
	private final RestTemplate restTemplate;

	private Date expiration = new Date(0);

	public RegionService(ApplicationProperties properties, RestTemplateBuilder restTemplateBuilder) {
    	this.properties = properties;
    	this.restTemplate = restTemplateBuilder.build();
    	ArrayList<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
    	interceptors.add(new PEHeaderRequestInterceptor(properties));
    	restTemplate.setInterceptors(interceptors);
    }

	private synchronized void updateReferences() {
		Date now = new Date();
		if (now.after(expiration)) {
			try {
				log.debug("Region cache is expired, refreshing it");
				REGION_REFERENCE.clear();
				for (RegionVM region : restTemplate.getForObject(properties.getDn005Url(), RegionVM[].class)) {
					REGION_REFERENCE.put(region.getCodeAssedic(), region);
				}
				
				// Update expiration
				Calendar cal = Calendar.getInstance();
				cal.setTime(now);
				cal.add(Calendar.DATE, 1);
				expiration = cal.getTime();
			} catch (RestClientException e) { // Just rethrow
				throw new RuntimeException("Could not update region reference file", e);
			}
		}
	}

	public RegionVM getRegion(String id) {
		updateReferences();
		return REGION_REFERENCE.get(id);
	}

	public Collection<RegionVM> getAll() {
		updateReferences();
		return REGION_REFERENCE.values();
	}

	public RegionVM buildDefaultRegion(String id) {
		RegionVM errorRegion = new RegionVM();
		errorRegion.setCodeAssedic(id);
		errorRegion.setLibelleAssedic("INCONNU");
		return errorRegion;
	}

	public int refreshCache() {
		this.expiration = new Date(0);
		updateReferences();
		return REGION_REFERENCE.size();
	}
}
