package com.imagidoc.poleemploi.scanintelligent.web.rest;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.junit.Before;
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
import com.imagidoc.poleemploi.scanintelligent.config.ApplicationProperties;
import com.imagidoc.poleemploi.scanintelligent.web.rest.vm.RegionVM;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = { PoleEmploiScanIntelligentBackendApp.class, ConseillerResourceIntTest.TestConfig.class })
@AutoConfigureMockMvc
public class ConseillerResourceIntTest {

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

	private static final String ROOT = "/api/v1/conseiller";

	@Autowired
    private MockMvc restLogsMockMvc;

	@Autowired
	private ApplicationProperties properties;

	@Before
    public void setup() throws IOException {
		Path targetCsvDir = Paths.get(properties.getRootPath(), ConseillerResource.CSV_DIR);
		Files.createDirectories(targetCsvDir);
		try (InputStream is = getClass().getResourceAsStream("/" + ConseillerResource.CSV_FILE)) {
			Files.copy(is, targetCsvDir.resolve(ConseillerResource.CSV_FILE), StandardCopyOption.REPLACE_EXISTING);	
		}
    }

    @Test
	public void checkParamMandatory() throws Exception {
    	MockHttpServletRequestBuilder request = get(ROOT + "/");
        restLogsMockMvc.perform(request)
            .andExpect(status().isNotFound());
    }

    @Test
	public void checkSyntaxAllAlphaKO() throws Exception {
    	MockHttpServletRequestBuilder request = get(ROOT + "/ABCDEFGH");
        restLogsMockMvc.perform(request)
            .andExpect(status().isBadRequest());
    }

    @Test
	public void checkSyntaxAllNumKO() throws Exception {
    	MockHttpServletRequestBuilder request = get(ROOT + "/12345678");
        restLogsMockMvc.perform(request)
            .andExpect(status().isBadRequest());
    }

    @Test
	public void checkSyntaxLength9KO() throws Exception {
    	MockHttpServletRequestBuilder request = get(ROOT + "/ABCD12345");
        restLogsMockMvc.perform(request)
            .andExpect(status().isBadRequest());
    }

    @Test
	public void checkSyntaxLength7KO() throws Exception {
    	MockHttpServletRequestBuilder request = get(ROOT + "/ABCD123");
        restLogsMockMvc.perform(request)
            .andExpect(status().isBadRequest());
    }

    @Test
    public void checkUnknownUserKO() throws Exception {
    	MockHttpServletRequestBuilder request = get(ROOT + "/ABCD1234");
        restLogsMockMvc.perform(request)
            .andExpect(status().isNotFound());
    }

    @Test
    public void checkValidUserOK() throws Exception {
    	MockHttpServletRequestBuilder request = get(ROOT + "/IJDE9240");
        restLogsMockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value("51002"))
            .andExpect(jsonPath("$.region.codeAssedic").value("051"))
            .andExpect(jsonPath("$.region.libelleAssedic").value("CHAMPAGNE ARDENNE"));
    }

    @Test
    public void checkValidUserCasingIgnoredOK() throws Exception {
    	String id = "/irde7750";
    	String resultCode = "45054";
    	String resultCodeAssedic = "035";
    	String resultLibelleAssedic = "REGION CENTRE";
    	
    	// Upper case
    	MockHttpServletRequestBuilder request1 = get(ROOT + id.toUpperCase());
        restLogsMockMvc.perform(request1)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(resultCode))
            .andExpect(jsonPath("$.region.codeAssedic").value(resultCodeAssedic))
            .andExpect(jsonPath("$.region.libelleAssedic").value(resultLibelleAssedic));
        
        // Lower case
        MockHttpServletRequestBuilder request2 = get(ROOT + id);
        restLogsMockMvc.perform(request2)
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(resultCode))
            .andExpect(jsonPath("$.region.codeAssedic").value(resultCodeAssedic))
            .andExpect(jsonPath("$.region.libelleAssedic").value(resultLibelleAssedic));
    }

    @Test
    public void checkRefreshCacheOK() throws Exception {
    	MockHttpServletRequestBuilder request1 = post(ROOT + "/refreshCache");
    	restLogsMockMvc.perform(request1)
    		.andExpect(status().isOk())
    		.andExpect(content().string("58403"));
    }
}
