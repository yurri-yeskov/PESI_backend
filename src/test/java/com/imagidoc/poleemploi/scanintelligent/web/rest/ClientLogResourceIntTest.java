package com.imagidoc.poleemploi.scanintelligent.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.gson.Gson;
import com.imagidoc.poleemploi.scanintelligent.PoleEmploiScanIntelligentBackendApp;
import com.imagidoc.poleemploi.scanintelligent.web.rest.testvm.LogEvent;

/**
 * Test class for the LogsResource REST controller.
 *
 * @see ClientLogResource
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = PoleEmploiScanIntelligentBackendApp.class)
public class ClientLogResourceIntTest {
	private static final String ROOT = "/api/v1/logs";

	private final Gson gson = new Gson();
    private MockMvc restLogsMockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        ClientLogResource logsResource = new ClientLogResource();
        this.restLogsMockMvc = MockMvcBuilders
            .standaloneSetup(logsResource)
            .build();
    }

    @Test
    public void addLogWarn()throws Exception {
    	LogEvent log = new LogEvent();
    	log.setApplication("DE");
    	log.setLevel("WARN");
    	log.setComment("This is a comment");
    	log.setDeviceName("I22322");
    	log.setMac("9c:93:4e:49:b6:5e");
    	log.setSerial("3350230237");
    	log.setSystemSoftware("072.060.165.14201");
    	
    	MockHttpServletRequestBuilder request = post(ROOT)
    			.contentType(MediaType.APPLICATION_JSON)
    			.content(gson.toJson(log, LogEvent.class));
        restLogsMockMvc.perform(request)
            .andExpect(status().isNoContent());
    }

    @Test
    public void addLogDebug()throws Exception {
    	LogEvent log = new LogEvent();
    	log.setApplication("Conseiller");
    	log.setLevel("DEBUG");
    	log.setComment("This is a comment");
    	log.setDeviceName("I22322");
    	log.setMac("9c:93:4e:49:b6:5e");
    	log.setSerial("3350230237");
    	log.setSystemSoftware("072.060.165.14201");
    	
    	MockHttpServletRequestBuilder request = post(ROOT)
    			.contentType(MediaType.APPLICATION_JSON)
    			.content(gson.toJson(log, LogEvent.class));
        restLogsMockMvc.perform(request)
            .andExpect(status().isNoContent());
    }
}
