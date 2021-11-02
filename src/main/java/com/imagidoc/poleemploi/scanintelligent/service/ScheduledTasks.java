package com.imagidoc.poleemploi.scanintelligent.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.imagidoc.poleemploi.scanintelligent.web.rest.ClientLogResource;

@Component
public class ScheduledTasks {

	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ScheduledTasks.class);
	private final org.slf4j.Logger logClient = org.slf4j.LoggerFactory.getLogger(ClientLogResource.class);

	@Scheduled(cron = "0 0 1 * * ?")
    public void startNewLogDay() {
        log.info("New day started");
        logClient.info("New day started");
    }
}
