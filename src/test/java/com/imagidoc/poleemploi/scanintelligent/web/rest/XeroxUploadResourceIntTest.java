package com.imagidoc.poleemploi.scanintelligent.web.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.fileUpload;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.google.common.io.ByteStreams;
import com.imagidoc.poleemploi.scanintelligent.PoleEmploiScanIntelligentBackendApp;
import com.imagidoc.poleemploi.scanintelligent.config.ApplicationProperties;
import com.imagidoc.poleemploi.scanintelligent.repository.ScanRepository;
import com.imagidoc.poleemploi.scanintelligent.service.XeroxUploadService;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = PoleEmploiScanIntelligentBackendApp.class)
public class XeroxUploadResourceIntTest {
	private static final String POST_URL = "/api/v1/xerox/upload";

	@Autowired
	private ScanRepository scanRepository;

	@Autowired
	private XeroxUploadService uploadService;

	@Autowired
	private ApplicationProperties applicationProperties;

	private MockMvc restAuditMockMvc;

	private boolean exists(String directory, String fileName) throws IOException {
		return scanRepository.getPathFromName(directory, fileName).toFile().exists();
	}

	@Before
	public void setup() throws IOException {
		MockitoAnnotations.initMocks(this);
		XeroxUploadResource uploadResource = new XeroxUploadResource(uploadService);
		this.restAuditMockMvc = MockMvcBuilders.standaloneSetup(uploadResource).build();
		Files.createDirectories(Paths.get(applicationProperties.getRootPath()));
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
	public void deleteFileOK() throws Exception {
		scanRepository.storeFile(null, "833_3350230237_20180119-180803.pdf", getClass().getResourceAsStream("/pdf/833_3350230237_20180119-180803.pdf"));
		MockHttpServletRequestBuilder request = fileUpload(POST_URL)
				.param("theOperation", "DeleteFile")
				.param("destDir", ".")
				.param("destName", "833_3350230237_20180119-180803.pdf")
				.contentType(MediaType.MULTIPART_FORM_DATA);
		restAuditMockMvc.perform(request)
				.andExpect(status().isOk());
		Assert.assertFalse(exists(null, "833_3350230237_20180119-180803.pdf"));
	}

	@Test
	public void makeDirOK() throws Exception {
		MockHttpServletRequestBuilder request = fileUpload(POST_URL)
				.param("theOperation", "MakeDir")
				.param("destDir", "833_3350230237_20180119-180803.lck")
				.contentType(MediaType.MULTIPART_FORM_DATA);
		restAuditMockMvc.perform(request).andExpect(status().isOk());
	}

	public void RemoveDirOk() throws Exception {
		scanRepository.createDirectory("833_3350230237_20180119-180803.lck");
		MockHttpServletRequestBuilder request = fileUpload(POST_URL)
				.param("theOperation", "RemoveDir")
				.param("destDir", "833_3350230237_20180119-180803.lck")
				.contentType(MediaType.MULTIPART_FORM_DATA);
		restAuditMockMvc.perform(request).andExpect(status().isOk());
	}

	@Test
	public void deleteDirContentsOK() throws Exception {
		scanRepository.createDirectory("833_3350230237_20180119-180803.lck");
		MockHttpServletRequestBuilder request = fileUpload(POST_URL)
				.param("theOperation", "DeleteDirContents")
				.param("destDir", "833_3350230237_20180119-180803.lck")
				.param("delDir", "1")
				.contentType(MediaType.MULTIPART_FORM_DATA);
		restAuditMockMvc.perform(request).andExpect(status().isOk());
	}

	@Test
	public void putFileOK() throws Exception {
		scanRepository.storeFile(null, "833_3350230237_20180119-180803.pdf", getClass().getResourceAsStream("/pdf/833_3350230237_20180119-180803.pdf"));
		MockHttpServletRequestBuilder request = fileUpload(POST_URL)
				.file(new MockMultipartFile("sendfile", null, MediaType.MULTIPART_FORM_DATA_VALUE, scanRepository.getFile(null, "833_3350230237_20180119-180803.pdf")))
				.param("theOperation", "PutFile").param("destDir", ".")
				.param("destName", "833_3350230237_20180119-180803.pdf")
				.contentType(MediaType.MULTIPART_FORM_DATA);
		restAuditMockMvc.perform(request)
				.andExpect(status().isOk());
		Assert.assertTrue(exists(null, "833_3350230237_20180119-180803.pdf"));
	}

	@Test
	public void getFileOK() throws Exception {
		byte[] targetContent = ByteStreams.toByteArray(getClass().getResourceAsStream("/pdf/833_3350230237_20180119-180803.pdf"));
		scanRepository.storeFile(null, "833_3350230237_20180119-180803.pdf", new ByteArrayInputStream(targetContent));
		MockHttpServletRequestBuilder request = fileUpload(POST_URL)
				.param("theOperation", "GetFile")
				.param("destDir", ".")
				.param("destName", "833_3350230237_20180119-180803.pdf")
				.contentType(MediaType.MULTIPART_FORM_DATA);
		restAuditMockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().bytes(targetContent));
	}

	@Test
	public void listDirOK() throws Exception {
		String target = "833_3350230237_" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + ".pdf";
		scanRepository.storeFile(null, target, getClass().getResourceAsStream("/pdf/833_3350230237_20180119-180803.pdf"));
		MockHttpServletRequestBuilder request = fileUpload(POST_URL)
				.param("theOperation", "ListDir")
				.param("destDir", ".")
				.contentType(MediaType.MULTIPART_FORM_DATA);
		restAuditMockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().string(target + "\n"));
	}

	@Test
	public void listDirEmptyOK() throws Exception {
		MockHttpServletRequestBuilder request = fileUpload(POST_URL)
				.param("theOperation", "ListDir")
				.param("destDir", ".")
				.contentType(MediaType.MULTIPART_FORM_DATA);
		restAuditMockMvc.perform(request)
				.andExpect(status().isOk())
				.andExpect(content().string(""));
	}

	// Errors

	@Test
	public void noOpKO() throws Exception {
		MockHttpServletRequestBuilder request = fileUpload(POST_URL)
				.contentType(MediaType.MULTIPART_FORM_DATA);
		restAuditMockMvc.perform(request)
				.andExpect(status().isBadRequest());
	}

	@Test
	public void badOpKO() throws Exception {
		MockHttpServletRequestBuilder request = fileUpload(POST_URL)
				.param("theOperation", "Unknown")
				.param("destDir", ".")
				.contentType(MediaType.MULTIPART_FORM_DATA);
		restAuditMockMvc.perform(request)
				.andExpect(status().isBadRequest());
	}
}
