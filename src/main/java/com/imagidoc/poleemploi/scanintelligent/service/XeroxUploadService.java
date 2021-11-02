package com.imagidoc.poleemploi.scanintelligent.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import com.imagidoc.poleemploi.scanintelligent.repository.ScanRepository;

/**
 * Service for Xerox printer file management.
 * 
 * @author Aurelien PRALONG
 *
 */
@Service
public class XeroxUploadService {
	private static final List<String> SUPPORTED_FILES = Arrays.asList("pdf"); // "xst", "dat", "txt", "pdf" for full XEROX support
	private static final List<String> SUPPORTED_DIRS = Arrays.asList(); // "lck", "xsm" for full XEROX support

	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(XeroxUploadService.class);
	private final ScanRepository scanRepository;

	public XeroxUploadService(ScanRepository scanRepository) {
		this.scanRepository = scanRepository;
	}

	private boolean isFileSupported(String file) {
		return SUPPORTED_FILES.contains(FilenameUtils.getExtension(file).toLowerCase());
	}

	private boolean isDirSupported(String dir) {
		return SUPPORTED_DIRS.contains(FilenameUtils.getExtension(dir).toLowerCase());
	}

	public void deleteFile(String destDir, String fileName) {
		log.debug("Delete file {}/{}", destDir, fileName);
		if (isFileSupported(fileName)) {
			scanRepository.deleteFile(destDir, fileName);
		}
	}

	public void makeDirectory(String destDir) throws IOException {
		log.debug("Create directory {}", destDir);
		if (isDirSupported(destDir)) {
			scanRepository.createDirectory(destDir);
		}
	}

	public void removeDirectory(String destDir) throws IOException {
		log.debug("Remove directory: {}", destDir);
		if (isDirSupported(destDir)) {
			scanRepository.deleteDirectory(destDir, true);
		}
	}

	public void deleteDirectoryContents(String destDir, boolean deleteDirectory) throws IOException {
		log.debug("Delete directory contents {} / Also delete directory: {}", destDir, deleteDirectory);
		if (isDirSupported(destDir)) {
			scanRepository.deleteDirectory(destDir, deleteDirectory);
		}
	}

	public byte[] getFile(String destDir, String destName) {
		log.debug("Get file {}/{}", destDir, destName);
		byte[] file = null;
		if (isFileSupported(destName)) {
			file = scanRepository.getFile(destDir, destName);
		}
		return file;
	}

	public void saveFiles(String destDir, String destName, InputStream content) throws IOException {
		log.debug("Save file {}/{}", destDir, destName);
		if (isFileSupported(destName)) {
			scanRepository.storeFile(destDir, destName, content);	
		}
	}

	public String listDirectory(String destDir) {
		log.debug("List directory {}", destDir);
		StringBuilder list = new StringBuilder();
		List<Path> dirContent = scanRepository.listDirectory(destDir);
		if (!dirContent.isEmpty()) {
			for (Path elt : dirContent) {
				list.append(elt.getFileName().toString());
				if (elt.toFile().isDirectory()) {
					list.append('/');
				}
				list.append("\n");
			}
		}		
		return list.toString();
	}
}
