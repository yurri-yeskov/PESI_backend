package com.imagidoc.poleemploi.scanintelligent.web.rest;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.imagidoc.poleemploi.scanintelligent.web.rest.util.HeaderUtil;
import com.imagidoc.poleemploi.scanintelligent.web.rest.vm.ClientLogVM;

@Validated
@RestController
@RequestMapping("/api/v1/logs")
public class ClientLogResource {

	private static final String TRACE_FORMAT = "{}";
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ClientLogResource.class);

    @PostMapping
    @Timed
    public ResponseEntity<Void> addDeviceLog(@RequestBody @Valid ClientLogVM deviceLog, HttpServletRequest request) {
    	String targetLevel = deviceLog.getLevel() == null ? "INFO" : deviceLog.getLevel().toUpperCase();
    	String ip = HeaderUtil.extractIp(request.getHeader("X-Forwarded-For"));
    	if (ip == null) {
    		ip = request.getRemoteAddr();
    	}
    	deviceLog.setIp(ip);
    	switch (targetLevel) {
		case "TRACE":
			log.trace(TRACE_FORMAT, deviceLog);
			break;
		case "DEBUG":
			log.debug(TRACE_FORMAT, deviceLog);
			break;
		case "WARN":
			log.warn(TRACE_FORMAT, deviceLog);
			break;
		case "ERROR":
			log.error(TRACE_FORMAT, deviceLog);
			break;
		case "INFO":
		default:
			log.info(TRACE_FORMAT, deviceLog);
			break;
		}
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
