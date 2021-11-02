package com.imagidoc.poleemploi.scanintelligent.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.imagidoc.poleemploi.scanintelligent.PoleEmploiScanIntelligentBackendApp;
import com.imagidoc.poleemploi.scanintelligent.config.ApplicationProperties;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PoleEmploiScanIntelligentBackendApp.class)
public class InfoResourceIntTest {

	private static final String ROOT = "/api/v1";

	@Autowired
	private ApplicationProperties settings;
	private MockMvc restLogsMockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        InfoResource infoResource = new InfoResource(settings);
        this.restLogsMockMvc = MockMvcBuilders
            .standaloneSetup(infoResource)
            .build();
    }

    @Test
    public void getVersionOK()throws Exception {
    	MockHttpServletRequestBuilder request = get(ROOT + "/informations");
        restLogsMockMvc.perform(request)
        	.andExpect(status().isOk())
        	.andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.name").value(settings.getName()))
            .andExpect(jsonPath("$.version").value(settings.getVersion()));
    }
}
