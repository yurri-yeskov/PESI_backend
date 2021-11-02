package com.imagidoc.poleemploi.scanintelligent.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.After;
import org.junit.Assert;
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

import com.google.gson.Gson;
import com.imagidoc.poleemploi.scanintelligent.PoleEmploiScanIntelligentBackendApp;
import com.imagidoc.poleemploi.scanintelligent.config.ApplicationProperties;
import com.imagidoc.poleemploi.scanintelligent.repository.ScanRepository;
import com.imagidoc.poleemploi.scanintelligent.service.ScanService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PoleEmploiScanIntelligentBackendApp.class)
public class ScanResourceIntTest {
	private static final String ROOT = "/api/v1/scan";
    private final Gson gson = new Gson();

    @Autowired
    private ScanRepository scanRepository;
    
    @Autowired
    private ScanService scanService;
    
    @Autowired
    private ApplicationProperties applicationProperties;

    private MockMvc restAuditMockMvc;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        
        ScanResource scanResource = new ScanResource(scanService);
        this.restAuditMockMvc = MockMvcBuilders.standaloneSetup(scanResource).build();
    }

    @Before
    public void initTest() throws IOException {
        scanRepository.storeFile(null, "833_3350230237_20180119-180803.pdf", getClass().getResourceAsStream("/pdf/833_3350230237_20180119-180803.pdf"));
        scanRepository.storeFile(null, "855_3350230237_20180205-161719.pdf", getClass().getResourceAsStream("/pdf/855_3350230237_20180205-161719.pdf"));
        scanRepository.storeFile(null, "860_3350230237_20180206-103335.pdf", getClass().getResourceAsStream("/pdf/860_3350230237_20180206-103335.pdf"));
        scanRepository.storeFile(null, "865_3350230237_20180206-165012.pdf", getClass().getResourceAsStream("/pdf/865_3350230237_20180206-165012.pdf"));
        scanRepository.storeFile(null, "992_3350230237_20180417-091524.pdf", getClass().getResourceAsStream("/pdf/992_3350230237_20180417-091524.pdf"));
    }

    @After
    public void cleanTest() throws IOException {
        Files.walkFileTree(Paths.get(applicationProperties.getRootPath()), new SimpleFileVisitor<Path>() {
    	   @Override
    	   public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
    	       Files.delete(file);
    	       return FileVisitResult.CONTINUE;
    	   }

    	   @Override
    	   public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
    	       Files.delete(dir);
    	       return FileVisitResult.CONTINUE;
    	   }
    	});
    }

    @Test
    public void getInfosOK() throws Exception {
    	Calendar cal = Calendar.getInstance();
    	cal.set(Calendar.YEAR, 2018);
    	cal.set(Calendar.MONTH, 0);
    	cal.set(Calendar.DAY_OF_MONTH, 19);
    	cal.set(Calendar.HOUR_OF_DAY, 18);
    	cal.set(Calendar.MINUTE, 8);
    	cal.set(Calendar.SECOND, 3);
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    	formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    	MockHttpServletRequestBuilder request = get(ROOT + "/search")
    			.param("jobId", "833")
    			.param("serial", "3350230237")
    			.param("creationDate", formatter.format(cal.getTime()));
        restAuditMockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.fileName").value("833_3350230237_20180119-180803.pdf"))
            .andExpect(jsonPath("$.pageCount").value("2"))
            .andExpect(jsonPath("$.size").value("115688"));
    }
    
    @Test
    public void getInfosEmpty() throws Exception {
    	Calendar cal = Calendar.getInstance();
    	cal.set(Calendar.YEAR, 2018);
    	cal.set(Calendar.MONTH, 3);
    	cal.set(Calendar.DAY_OF_MONTH, 17);
    	cal.set(Calendar.HOUR_OF_DAY, 9);
    	cal.set(Calendar.MINUTE, 15);
    	cal.set(Calendar.SECOND, 24);
    	SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    	formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
    	MockHttpServletRequestBuilder request = get(ROOT + "/search")
    			.param("jobId", "992")
    			.param("serial", "3350230237")
    			.param("creationDate", formatter.format(cal.getTime()));
        restAuditMockMvc.perform(request)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_UTF8_VALUE))
            .andExpect(jsonPath("$.fileName").value("992_3350230237_20180417-091524.pdf"))
            .andExpect(jsonPath("$.pageCount").value("0"));
    }

    @Test
    public void getPageOK() throws Exception {
    	MockHttpServletRequestBuilder request = get(ROOT + "/part")
    			.param("fileName", "860_3350230237_20180206-103335.pdf")
    			.param("page", "1");
    	restAuditMockMvc.perform(request)
	        .andExpect(status().isOk())
	        .andExpect(content().contentType(MediaType.IMAGE_JPEG));
    }

    @Test
    public void getFileOK() throws Exception {
    	MockHttpServletRequestBuilder request = get(ROOT + "/file/{fileName}", "860_3350230237_20180206-103335.pdf")
    			.accept(MediaType.APPLICATION_PDF);
    	restAuditMockMvc.perform(request)
	        .andExpect(status().isOk())
	        .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }
    
    @Test
    public void deleteFileOK() throws Exception {
    	MockHttpServletRequestBuilder request = delete(ROOT + "/file/{fileName}", "860_3350230237_20180206-103335.pdf");
    	restAuditMockMvc.perform(request)
	        .andExpect(status().isNoContent());
    	Assert.assertFalse(scanRepository.getPathFromName(null, "860_3350230237_20180206-103335.pdf").toFile().exists());
    }
    
    @Test
    public void mergeScansOK() throws Exception {
    	String[] parameters = {
    			"833_3350230237_20180119-180803.pdf",
    			"855_3350230237_20180205-161719.pdf",
    			"860_3350230237_20180206-103335.pdf",
    			"865_3350230237_20180206-165012.pdf"
		};
    	MockHttpServletRequestBuilder request = post(ROOT + "/merge")
    			.contentType(MediaType.APPLICATION_JSON)
    			.content(gson.toJson(parameters));
    	
    	String expectedFileName = "833_3350230237_20180119-180803-merged.pdf";
    	restAuditMockMvc.perform(request)
	        .andExpect(status().isOk())
            .andExpect(jsonPath("$.fileName").value(expectedFileName))
            .andExpect(jsonPath("$.pageCount").value("9"))
            .andExpect(jsonPath("$.size").value("536713"));
    	Assert.assertTrue(scanRepository.getPathFromName(null, expectedFileName).toFile().exists());
    }
}
