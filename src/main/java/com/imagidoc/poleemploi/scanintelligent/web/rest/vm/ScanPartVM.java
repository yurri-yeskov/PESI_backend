package com.imagidoc.poleemploi.scanintelligent.web.rest.vm;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class ScanPartVM {

	@NotNull
	private String fileName;

	@Min(1)
	private int page;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getPage() {
		return page;
	}

	public void setPage(int page) {
		this.page = page;
	}

	@Override
	public String toString() {
		return "ScanPartVM{fileName='" + fileName + "', page='" + page + "'}";
	}
}
