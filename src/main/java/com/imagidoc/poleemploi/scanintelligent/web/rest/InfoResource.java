package com.imagidoc.poleemploi.scanintelligent.web.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.imagidoc.poleemploi.scanintelligent.config.ApplicationProperties;
import com.imagidoc.poleemploi.scanintelligent.web.rest.vm.InformationsVM;

@Validated
@RestController
@RequestMapping("/api/v1")
public class InfoResource {

    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(InfoResource.class);
	private final ApplicationProperties properties;

    public InfoResource(ApplicationProperties properties) {
		this.properties = properties;
	}

    @GetMapping("/informations")
    @Timed
    public @ResponseBody ResponseEntity<InformationsVM> getVersion() {
    	log.debug("Get application informations");
    	InformationsVM vm = new InformationsVM();
    	vm.setName(properties.getName());
    	vm.setVersion(properties.getVersion());
        return new ResponseEntity<>(vm, HttpStatus.OK);
    }
}
