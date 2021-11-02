package com.imagidoc.poleemploi.scanintelligent.web.rest.vm;

import java.util.Date;

import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

public class ScanSearchVM {

	@NotNull
	private String jobId;

	@NotNull
	private String serial;

	@DateTimeFormat(iso = ISO.DATE_TIME)
	@NotNull
	private Date creationDate;

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public String getJobId() {
		return jobId;
	}

	public void setJobId(String jobId) {
		this.jobId = jobId;
	}

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	@Override
	public String toString() {
		return "ScanSearchVM{jobId='" + jobId + "', serial='" + serial + "', creationDate='" + creationDate + "'}";
	}
}
