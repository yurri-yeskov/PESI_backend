package com.imagidoc.poleemploi.scanintelligent.web.rest;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.imagidoc.poleemploi.scanintelligent.PoleEmploiScanIntelligentBackendApp;
import com.imagidoc.poleemploi.scanintelligent.web.rest.vm.RegionVM;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PoleEmploiScanIntelligentBackendApp.class, RegionResourceIntTest.TestConfig.class })
@AutoConfigureMockMvc
public class RegionResourceIntTest {

	@TestConfiguration
    static class TestConfig {

        @Bean
        public RestTemplateBuilder restTemplateBuilder() {
        	RestTemplateBuilder restTemplateBuilder = mock(RestTemplateBuilder.class);
            RestTemplate restTemplate = mock(RestTemplate.class);

    		try (InputStream is = getClass().getResourceAsStream("/regions.json")) {
                RegionVM[] allRegions = new Gson().fromJson(new InputStreamReader(is), RegionVM[].class);
                
        		given(restTemplate.getForObject("http://localhost-test/nomenclature/unite/v1/regionsASSEDIC", RegionVM[].class))
        			.willReturn(allRegions);
        		when(restTemplateBuilder.build()).thenReturn(restTemplate);
    		} catch (Exception e) {
    			throw new RuntimeException(e);
			}
            return restTemplateBuilder;
        }        
    }

	private static final String ROOT = "/api/v1/region";

	@Autowired
    private MockMvc restLogsMockMvc;

    @Test
	public void getAllRegionsOK() throws Exception {
    	MockHttpServletRequestBuilder request = get(ROOT + "/");
        restLogsMockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(36)))
            .andExpect(jsonPath("$[0].codeAssedic").value("070"))
            .andExpect(jsonPath("$[0].libelleAssedic").value("SAINT-PIERRE ET MIQUELON"));
    }

    @Test
	public void checkSyntaxAllAlphaKO() throws Exception {
    	MockHttpServletRequestBuilder request = get(ROOT + "/ABC");
        restLogsMockMvc.perform(request)
            .andExpect(status().isBadRequest());
    }
    
    @Test
	public void checkSyntaxLength2KO() throws Exception {
    	MockHttpServletRequestBuilder request = get(ROOT + "/12");
        restLogsMockMvc.perform(request)
            .andExpect(status().isBadRequest());
    }

    @Test
	public void checkSyntaxLength4KO() throws Exception {
    	MockHttpServletRequestBuilder request = get(ROOT + "/1234");
        restLogsMockMvc.perform(request)
            .andExpect(status().isBadRequest());
    }

    @Test
    public void checkUnknownRegionKO() throws Exception {
    	MockHttpServletRequestBuilder request = get(ROOT + "/666");
        restLogsMockMvc.perform(request)
            .andExpect(status().isNotFound());
    }

    @Test
    public void checkValidRegionOK() throws Exception {
    	MockHttpServletRequestBuilder request = get(ROOT + "/012");
        restLogsMockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.codeAssedic").value("012"))
            .andExpect(jsonPath("$.libelleAssedic").value("LIMOUSIN"));
    }

    @Test
    public void checkRefreshCacheOK() throws Exception {
    	MockHttpServletRequestBuilder request1 = post(ROOT + "/refreshCache");
    	restLogsMockMvc.perform(request1)
    		.andExpect(status().isOk())
    		.andExpect(content().string("36"));
    }
}
