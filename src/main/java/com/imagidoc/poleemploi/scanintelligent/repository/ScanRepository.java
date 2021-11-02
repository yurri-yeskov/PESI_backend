package com.imagidoc.poleemploi.scanintelligent.repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Date;
import java.util.List;

/**
 * Base scan files access. All files are expected to have a
 * {jobId}_{serial}_{yyyyMMdd-HHmmss} structure.
 * 
 * @author Aurelien PRALONG
 *
 */
public interface ScanRepository {

	/**
	 * Returns Path for given scan.
	 * 
	 * @param jobId        job to retrieve.
	 * @param serial       printer serial
	 * @param creationDate creation date. Must be hour precise !
	 * @param extension    file extension
	 * @return matching Path, or null if not found
	 */
	Path getFilePath(String jobId, String serial, Date creationDate, String extension);

	/**
	 * Returns full path from name.
	 * 
	 * @param directory (optional) directory to search against
	 * @param fileName  file name ({jobId}_{serial}_{yyyyMMdd-HHmmss})
	 * @return full path
	 */
	Path getPathFromName(String directory, String fileName);

	/**
	 * Returns file content.
	 * 
	 * @param directory (optional) directory to search against
	 * @param fileName  file name ({jobId}_{serial}_{yyyyMMdd-HHmmss})
	 * @return file content
	 */
	byte[] getFile(String directory, String fileName);

	/**
	 * Writes a new file to disk.
	 * 
	 * @param directory top directory
	 * @param fileName  format 865_3350230237_20180206-165012.pdf, aka
	 *                  jobId_serial_yyyyMMdd-HHmmss.pdf
	 * @param content   file content
	 * @throws IOException when file cannot be created
	 */
	void storeFile(String directory, String fileName, InputStream content) throws IOException;

	/**
	 * Deletes given file.
	 * 
	 * @param directory (optional) directory to search against
	 * @param fileName  file name ({jobId}_{serial}_{yyyyMMdd-HHmmss})
	 */
	void deleteFile(String directory, String fileName);

	/**
	 * Creates a directory.
	 * 
	 * @param directory directory to create ({jobId}_{serial}_{yyyyMMdd-HHmmss})
	 * @throws IOException if a problem occurs
	 */
	void createDirectory(String directory) throws IOException;

	/**
	 * Deletes a directory.
	 * 
	 * @param directory  directory to create ({jobId}_{serial}_{yyyyMMdd-HHmmss})
	 * @param deleteRoot
	 * @throws IOException
	 */
	void deleteDirectory(String directory, boolean deleteRoot) throws IOException;

	/**
	 * Lists directory content.
	 * 
	 * @param directory directory to create ({jobId}_{serial}_{yyyyMMdd-HHmmss})
	 * @return directory contents
	 */
	List<Path> listDirectory(String directory);
}
