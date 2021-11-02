package com.imagidoc.poleemploi.scanintelligent.web.rest;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.codahale.metrics.annotation.Timed;
import com.imagidoc.poleemploi.scanintelligent.service.ScanService;
import com.imagidoc.poleemploi.scanintelligent.web.rest.vm.ScanInfoVM;
import com.imagidoc.poleemploi.scanintelligent.web.rest.vm.ScanPartVM;
import com.imagidoc.poleemploi.scanintelligent.web.rest.vm.ScanSearchVM;

@Validated
@RestController
@RequestMapping("/api/v1/scan")
public class ScanResource {

    private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ScanResource.class);
    private final ScanService scanService;

    public ScanResource(ScanService scanService) {
        this.scanService = scanService;
    }

    @GetMapping("/search")
    @Timed
    public @ResponseBody ResponseEntity<ScanInfoVM> searchScan(@Valid ScanSearchVM scan) {
    	log.debug("Get page count for {}", scan);
    	ScanInfoVM result = new ScanInfoVM();
    	result.setFileName(scanService.search(scan.getJobId(), scan.getSerial(), scan.getCreationDate()));
    	if (result.getFileName() == null) {
    		log.info("Could not find scan {}", scan);
    		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    	}
    	result.setPageCount(scanService.getPageCount(result.getFileName(), true));
    	if (result.getPageCount() < 0) {
    		log.info("Could not find scan {}", scan);
    		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    	} else if (result.getPageCount() == 0) {
    		log.info("Scan {} is empty", scan);
    	} else {
        	result.setSize(scanService.getSize(result.getFileName()));
    	}
        return new ResponseEntity<>(result, HttpStatus.OK);
    }

    @GetMapping(value = "/part")
    @Timed
    public @ResponseBody ResponseEntity<byte[]> getPage(@Valid ScanPartVM scan) {
    	log.debug("Get page for {}", scan);
    	byte[] pageBytes = scanService.getPage(scan.getFileName(), scan.getPage() - 1);
    	if (pageBytes == null) {
    		log.info("Could not find file {}", scan);
    		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    	}
    	HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_JPEG);
        return new ResponseEntity<>(pageBytes, headers, HttpStatus.OK);
    }

    @GetMapping(value = "/file/{fileName:.+}")
    @Timed
    public @ResponseBody ResponseEntity<byte[]> getFile(@PathVariable("fileName") String fileName) {
    	if (fileName == null || fileName.length() == 0) {
    		log.info("Could not get empty named file");
    		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    	}
    	log.debug("Get file {}", fileName);
    	byte[] pageBytes = scanService.getFile(fileName);
    	if (pageBytes == null || pageBytes.length == 0) {
    		log.info("Could not get file {}", fileName);
    		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    	}
    	HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
    	return new ResponseEntity<>(pageBytes, headers, HttpStatus.OK);
    }

    @DeleteMapping("/file/{fileName:.+}")
    @Timed
    public ResponseEntity<Void> deleteFile(@PathVariable("fileName") String fileName) {
    	if (fileName == null || fileName.length() == 0) {
    		log.info("Could not delete empty named file");
    		return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    	}
    	log.debug("Delete file {}", fileName);
    	scanService.deleteFile(fileName);
    	return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    @PostMapping("/merge")
    @Timed
    public @ResponseBody ResponseEntity<ScanInfoVM> mergeScans(@RequestBody@NotNull String[] scans) {
    	log.debug("Merging {} scans", scans.length);
    	try {
    		ScanInfoVM result = new ScanInfoVM();
        	result.setFileName(scanService.merge(scans));
        	if (result.getFileName() == null) {
        		log.info("Could not find scans to merge");
        		return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        	}
        	result.setPageCount(scanService.getPageCount(result.getFileName(), false));
        	result.setSize(scanService.getSize(result.getFileName()));
        	for (String scan : scans) {
        		scanService.deleteFile(scan);
        	}
            return new ResponseEntity<>(result, HttpStatus.OK);	
    	} catch (Exception e) {
    		log.warn("Could not merge scans", e);
    		return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
    	}
    }
}
