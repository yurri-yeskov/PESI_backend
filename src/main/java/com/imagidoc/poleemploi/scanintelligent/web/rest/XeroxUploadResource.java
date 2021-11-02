package com.imagidoc.poleemploi.scanintelligent.web.rest;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.codahale.metrics.annotation.Timed;
import com.imagidoc.poleemploi.scanintelligent.service.XeroxUploadService;

/**
 * Xerox printer HTTP transfer implementation.
 * @author Aurelien PRALONG
 *
 */
@RestController
@RequestMapping("/api/v1/xerox/upload")
public class XeroxUploadResource {

	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(XeroxUploadResource.class);
	private final XeroxUploadService uploadService;

    public XeroxUploadResource(XeroxUploadService uploadService) {
        this.uploadService = uploadService;
    }
    
    private boolean isNullOrEmpty(String source) {
    	return source == null || source.length() == 0;
    }

	@PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Timed
	public void handleScannerRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String destDir = request.getParameter("destDir");
		String destName = request.getParameter("destName");
		String delDir = request.getParameter("delDir");
		String theOperation = request.getParameter("theOperation");
		
		MultipartFile sendFile = null;
		if (request instanceof MultipartHttpServletRequest) {
			sendFile = ((MultipartHttpServletRequest)request).getFile("sendfile");
		}
		
		// Clean paths
		if (destDir != null) {
			destDir = destDir.replace("../", "").replace("..", "");
		}
		
		log.debug("Received new scanner operation : theOperation={}, destDir={}, destName={}, delDir={}",
				theOperation, destDir, destName, delDir);
		if (isNullOrEmpty(theOperation)) {
			response.setStatus(HttpStatus.BAD_REQUEST.value());
			return;
		}
		try {
			byte[] responseBytes = null;
			switch (theOperation) {
			case "DeleteFile":
				uploadService.deleteFile(destDir, destName);
				break;
			case "MakeDir":
				uploadService.makeDirectory(destDir);
				break;
			case "RemoveDir":
				uploadService.removeDirectory(destDir);
				break;
			case "DeleteDirContents":
				uploadService.deleteDirectoryContents(destDir,  "1".equals(delDir));
				break;
			case "GetFile":
				responseBytes = uploadService.getFile(destDir, destName);
				if (responseBytes == null || responseBytes.length == 0) {
					responseBytes = "XRXBADNAME".getBytes();
				}
				break;
			case "PutFile":
				if (sendFile == null) {
					log.error("Could not get sent file for PutFile");
					responseBytes = "XRXERROR".getBytes();
				} else {
					try (InputStream is = sendFile.getInputStream()) {
						uploadService.saveFiles(destDir, destName, is);
					}
				}
				break;
			case "ListDir":
				String list = uploadService.listDirectory(destDir);
				if (!isNullOrEmpty(list)) {
					responseBytes = list.getBytes();
				}
				break;
			default:
				log.warn("Could not process operation {}", theOperation);
				response.setStatus(HttpStatus.BAD_REQUEST.value());
				return;
			}
			
			if (responseBytes != null) {
				log.debug("Writing {} bytes", responseBytes.length);
				response.setContentLength(responseBytes.length);
				ServletOutputStream outStream = response.getOutputStream();
				outStream.write(responseBytes);
				outStream.flush();
				outStream.close();
			}
			response.setStatus(HttpStatus.OK.value());
		} catch (Exception e) {
			log.warn("Could not handle request", e);
			response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
		}
	}
}
