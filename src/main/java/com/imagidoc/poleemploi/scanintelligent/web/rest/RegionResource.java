package com.imagidoc.poleemploi.scanintelligent.web.rest;

import java.util.Collection;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.imagidoc.poleemploi.scanintelligent.service.RegionService;
import com.imagidoc.poleemploi.scanintelligent.web.rest.vm.RegionVM;

@RestController
@RequestMapping("/api/v1/region")
@Validated
public class RegionResource {

	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(RegionResource.class);
	private final RegionService regionService;

	public RegionResource(RegionService regionService) {
		this.regionService = regionService;
	}
	
	@GetMapping("/")
    @Timed
	public ResponseEntity<Collection<RegionVM>> getAllRegions() {
		return new ResponseEntity<>(regionService.getAll(), HttpStatus.OK);
	}

	@GetMapping("/{id}")
    @Timed
	public ResponseEntity<RegionVM> getRegion(@PathVariable(required = true) @Size(min = 3, max = 3) @Pattern(regexp = "[0-9]{3}") @Valid String id) {
		RegionVM vm = regionService.getRegion(id);
		if (vm == null) {
			log.debug("Could not find region {}", id);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(vm, HttpStatus.OK);
	}

	@PostMapping("/refreshCache")
	@Timed
	public ResponseEntity<Integer> refreshCache() {
		return new ResponseEntity<>(regionService.refreshCache(), HttpStatus.OK);
	}
}
