package com.imagidoc.poleemploi.scanintelligent.web.rest;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

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
import com.imagidoc.poleemploi.scanintelligent.config.ApplicationProperties;
import com.imagidoc.poleemploi.scanintelligent.service.RegionService;
import com.imagidoc.poleemploi.scanintelligent.web.rest.vm.StructureVM;

@RestController
@RequestMapping("/api/v1/conseiller")
@Validated
public class ConseillerResource {

	public static final String CSV_DIR = "ldap";
	public static final String CSV_FILE = "LDAPSCANRCONS.csv";

	private static final Map<String, StructureVM> USER_REFERENCE = new HashMap<>();

	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ConseillerResource.class);
	private final ApplicationProperties properties;
	private final RegionService regionService;
	private Date expiration = new Date(0);

	public ConseillerResource(ApplicationProperties properties, RegionService regionService) {
		this.properties = properties;
		this.regionService = regionService;
	}

	private String normalizeRegion(String region) {
		return region.length() > 2 ? region : "0" + region;
	}

	private void parseLine(String line)  {
		String[] elements = line.split(";");
		if (elements.length >= 3) {
			StructureVM structure = new StructureVM();
			structure.setCode(elements[1]);
			String region = normalizeRegion(elements[2]);
			structure.setRegion(regionService.getRegion(region));
			if (structure.getRegion() == null) {
				log.warn("Could not retrieve region {} label", region);
				structure.setRegion(regionService.buildDefaultRegion(region));
			}
			USER_REFERENCE.put(elements[0].toUpperCase(), structure);
		}
	}

	private synchronized void updateReferences() {
		Date now = new Date();
		if (now.after(expiration)) {
			log.debug("User cache is expired, refreshing it");
			File targetCsv = Paths.get(properties.getRootPath(), CSV_DIR, CSV_FILE).toFile();
			if (targetCsv.exists()) {
				try (InputStream csvStream = new FileInputStream(targetCsv)) {
					Scanner scanner = new Scanner(csvStream);
					USER_REFERENCE.clear();
					while (scanner.hasNext()) {
						parseLine(scanner.nextLine());
					}
					scanner.close();
					
					// Do not allow failures
					Calendar cal = Calendar.getInstance();
					cal.setTime(now);
					cal.add(Calendar.DATE, 1);
					expiration = cal.getTime();
				} catch (IOException e) { // Just rethrow
					throw new RuntimeException("Could not update conseiller reference file", e);
				}
			} else { // Just rethrow
				throw new RuntimeException("Could not find reference CSV");
			}
		}
	}

	@GetMapping("/{id}")
    @Timed
	public ResponseEntity<StructureVM> getStructure(@PathVariable(required = true) @Size(min = 8, max = 8) @Pattern(regexp = "[a-zA-Z]{4}[0-9]{4}") @Valid String id) {
		updateReferences();
		StructureVM vm = USER_REFERENCE.get(id.toUpperCase());
		if (vm == null) {
			log.debug("Could not find user {}", id);
			return new ResponseEntity<>(HttpStatus.NOT_FOUND);
		}
		return new ResponseEntity<>(vm, HttpStatus.OK);
	}

	@PostMapping("/refreshCache")
	@Timed
	public ResponseEntity<Integer> refreshCache() {
		this.expiration = new Date(0);
		updateReferences();
		return new ResponseEntity<>(USER_REFERENCE.size(), HttpStatus.OK);
	}
}
