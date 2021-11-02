package com.imagidoc.poleemploi.scanintelligent.repository;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Stream;

import org.springframework.stereotype.Repository;

import com.imagidoc.poleemploi.scanintelligent.config.ApplicationProperties;

@Repository
public class FsScanRepository implements ScanRepository {

	private final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(FsScanRepository.class);
	private final ApplicationProperties properties;

	public FsScanRepository(ApplicationProperties properties) {
		this.properties = properties;
	}

	private static String get2Digits(int number) {
		return number < 10 ? "0" + number : String.valueOf(number);
	}

	private static boolean isCurrentDirectory(String directory) {
		return directory == null || directory.length() == 0 || directory.equals(".") || directory.equals("./");
	}

	@Override
	public Path getFilePath(String jobId, String serial, Date creationDate, String extension) {
		Path fileName = null;
		Calendar cal = Calendar.getInstance();
		cal.setTime(creationDate);
		Path targetDir = Paths.get(properties.getRootPath(), get2Digits(cal.get(Calendar.MONTH) + 1),
				get2Digits(cal.get(Calendar.DAY_OF_MONTH)), get2Digits(cal.get(Calendar.HOUR_OF_DAY)));
		if (targetDir.toFile().exists()) {
			try {
				ScanPatternFileVisitor visitor = new ScanPatternFileVisitor(jobId, serial,
						new SimpleDateFormat("yyyyMMdd").format(creationDate), extension);
				Files.walkFileTree(targetDir, EnumSet.noneOf(FileVisitOption.class), 1, visitor);
				fileName = visitor.foundFile;
			} catch (IOException e) {
				log.warn("Could not search file", e);
			}
		} else {
			log.debug("Directory {} does not exist", targetDir);
		}
		return fileName;
	}

	@Override
	public byte[] getFile(String directory, String fileName) {
		byte[] content = null;
		Path target = getPathFromName(directory, fileName);
		if (target != null && target.toFile().exists()) {
			try {
				content = Files.readAllBytes(target);
			} catch (IOException e) {
				log.warn("Could not read {}", fileName);
			}
		} else {
			log.debug("{} does not exist", fileName);
		}
		return content;
	}

	@Override
	public Path getPathFromName(String directory, String fileName) {
		// Expected file format : 865_3350230237_20180206-165012.pdf, aka
		// jobId_serial_yyyyMMdd-HHmmss.pdf
		Path targetPath = null;
		String[] fileParts = fileName.split("_");
		if (fileParts.length == 3) {
			String datePart = fileParts[2];
			if (datePart.length() >= 15) {
				try {
					Date parsed = new SimpleDateFormat("yyyyMMdd-HHmmss").parse(datePart.substring(0, 15));
					Calendar cal = Calendar.getInstance();
					cal.setTime(parsed);
					String month = get2Digits(cal.get(Calendar.MONTH) + 1);
					String day = get2Digits(cal.get(Calendar.DAY_OF_MONTH));
					String hour = get2Digits(cal.get(Calendar.HOUR_OF_DAY));
					targetPath = isCurrentDirectory(directory)
							? Paths.get(properties.getRootPath(), month, day, hour, fileName)
							: Paths.get(properties.getRootPath(), directory, month, day, hour, fileName);
				} catch (Exception e) {
					log.warn("Could not convert file name {} : invalid date format", fileName, e);
				}
			} else {
				log.warn("Could not convert file name {} : invalid date format (too short)", fileName);
			}
		} else {
			log.warn("Could not convert file name {} : invalid general format", fileName);
		}
		return targetPath;
	}

	@Override
	public void storeFile(String directory, String fileName, InputStream content) throws IOException {
		Path targetPath = getPathFromName(directory, fileName);
		if (targetPath != null) {
			if (!targetPath.getParent().toFile().exists()) {
				Files.createDirectories(targetPath.getParent());
			}
			Files.copy(content, targetPath, StandardCopyOption.REPLACE_EXISTING);
			log.debug("Written {}", fileName);
		} else {
			throw new IOException("File does not exist : " + fileName);
		}
	}

	@Override
	public void deleteFile(String directory, String fileName) {
		Path targetPath = getPathFromName(directory, fileName);
		if (targetPath != null && targetPath.toFile().exists()) {
			try {
				Files.delete(targetPath);
				log.debug("Deleted file {}", fileName);
			} catch (IOException e) {
				log.warn("deleteFile : could not delete {}", fileName, e);
			}
		}
	}

	@Override
	public void createDirectory(String directory) throws IOException {
		if (!isCurrentDirectory(directory)) {
			Path path = getPathFromName(null, directory);
			if (path != null && !path.toFile().exists()) {
				Files.createDirectories(path);
			}
		}
	}

	@Override
	public void deleteDirectory(String directory, boolean deleteRoot) throws IOException {
		Path path = getPathFromName(null, directory);
		if (path != null && path.toFile().exists()) {
			try {
				Files.delete(path);
				log.debug("Deleted directory {}", directory);
			} catch (DirectoryNotEmptyException dne) {
				log.info("Directory not empty {} : wait a bit", directory);
				try {
					Thread.sleep(500);
					try (Stream<Path> sp = Files.walk(path, 1)) {
						sp.filter(tmp -> tmp.toFile().isFile()).forEach(tmp -> {
							try {
								Files.delete(tmp);
							} catch (IOException ex) {
								log.debug("deleteDirectory : Could not delete file {}", tmp);
							}
						});
					}
					Files.delete(path);
				} catch (Exception e) {
					log.warn("Could not delete directory {}", directory, e);
				}
			} catch (IOException ioe) {
				log.warn("Could not delete directory {}", directory, ioe);
			}
		}
	}

	@Override
	public List<Path> listDirectory(String directory) {
		final List<Path> results = new ArrayList<>();
		Path path;
		if (isCurrentDirectory(directory)) {
			path = getPathFromName(null, "jobId_serial_" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()))
					.getParent();
		} else {
			path = getPathFromName(null, directory);
		}
		if (path != null && path.toFile().exists()) {
			try {
				Files.walkFileTree(path, EnumSet.noneOf(FileVisitOption.class), 1, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
						results.add(file);
						return FileVisitResult.CONTINUE;
					}
				});
			} catch (IOException e) {
				log.info("Could not list directory", e);
			}
		}
		return results;
	}

	private class ScanPatternFileVisitor extends SimpleFileVisitor<Path> {
		private Path foundFile;
		private final String pattern;
		private final String extension;

		public ScanPatternFileVisitor(String jobId, String serial, String searchDate, String extension) {
			this.pattern = jobId + "_" + serial + "_" + searchDate;
			this.extension = extension;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
			FileVisitResult found = FileVisitResult.CONTINUE;
			String tmp = file.getFileName().toString().toLowerCase();
			if (tmp.startsWith(pattern) && (extension == null || tmp.endsWith(extension))) {
				foundFile = file;
				found = FileVisitResult.TERMINATE;
			}
			return found;
		}
	}
}
