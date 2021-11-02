package com.imagidoc.poleemploi.scanintelligent.service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import javax.imageio.ImageIO;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.stereotype.Service;

import com.google.common.io.Files;
import com.imagidoc.poleemploi.scanintelligent.config.ApplicationProperties;
import com.imagidoc.poleemploi.scanintelligent.repository.ScanRepository;

/**
 * Service for PDF retrieval.
 * 
 * @author Aurelien PRALONG
 *
 */
@Service
public class ScanService {

	private static final int EMPTY_SIZE_WARNING = 10000;
	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(ScanService.class);

	private final ScanRepository scanRepository;
	private final ApplicationProperties properties;

	public ScanService(ScanRepository scanRepository, ApplicationProperties properties) {
		this.scanRepository = scanRepository;
		this.properties = properties;
	}

	public int getPageCount(String fileName, boolean checkEmpty) {
		int pageCount = -1;
		Path path = scanRepository.getPathFromName(null, fileName);
		boolean shouldDelete = false;
		if (path != null) {
			try (PDDocument document = PDDocument.load(path.toFile())) {
				pageCount = document.getNumberOfPages();
				if (checkEmpty && pageCount == 1 && java.nio.file.Files.size(path) <= EMPTY_SIZE_WARNING) {
					log.info("Detected empty file for file {}", path);
					pageCount = 0;
					shouldDelete = true;
				}
			} catch (Exception render) {
				log.warn("Could not retrieve page count for file {}", path, render);
			}
		} else {
			log.warn("Could not find matching file for {}", fileName);
		}
		if (shouldDelete) {
			try {
				java.nio.file.Files.delete(path);
			} catch (IOException e) {
				log.warn("Could not delete file {}", path, e);
			}
		}
		return pageCount;
	}

	public long getSize(String fileName) {
		long size = -1;
		Path path = scanRepository.getPathFromName(null, fileName);
		if (path != null) {
			try {
				size = java.nio.file.Files.size(path);
			} catch (Exception e) {
				log.warn("Could not get size", e);
			}
		}
		return size;
	}

	public byte[] getPage(String fileName, int page) {
		byte[] result = null;
		Path path = scanRepository.getPathFromName(null, fileName);
		if (path != null) {
			try (PDDocument document = PDDocument.load(path.toFile())) {
				PDFRenderer pdfRenderer = new PDFRenderer(document);
				int pageCount = document.getNumberOfPages();
				if (page >= 0 && page < pageCount) {
					BufferedImage bim = pdfRenderer.renderImageWithDPI(page, properties.getTargetDpi());
					ByteArrayOutputStream imageContent = new ByteArrayOutputStream();
					ImageIO.write(bim, "jpg", imageContent);
					result = imageContent.toByteArray();
				} else {
					log.info("Page outside range : {} / {}", page, pageCount);
				}
			} catch (Exception ignored) {
				log.warn("Could not render page", ignored);
			}
		}
		return result;
	}

	public String search(String jobId, String serial, Date creationDate) {
		Path found = scanRepository.getFilePath(jobId, serial, creationDate, ".pdf");
		return found == null ? null : found.getFileName().toString();
	}

	public byte[] getFile(String fileName) {
		return scanRepository.getFile(null, fileName);
	}

	public void deleteFile(String fileName) {
		scanRepository.deleteFile(null, fileName);
	}

	public String merge(String[] scans) throws IOException {
		String result = null;
		Path mergedFile = null;
		PDFMergerUtility merger = new PDFMergerUtility();
		for (String scan : scans) {
			Path path = scanRepository.getPathFromName(null, scan);
			if (path != null && path.toFile().exists()) {
				if (mergedFile == null) {
					mergedFile = Paths.get(path.getParent().toString(),
							Files.getNameWithoutExtension(path.getFileName().toString()) + "-merged.pdf");
				}
				merger.addSource(path.toFile());
			}
		}
		if (mergedFile != null) {
			merger.setDestinationFileName(mergedFile.toString());
			merger.mergeDocuments(null);
			result = mergedFile.getFileName().toString();
		}
		return result;
	}
}
