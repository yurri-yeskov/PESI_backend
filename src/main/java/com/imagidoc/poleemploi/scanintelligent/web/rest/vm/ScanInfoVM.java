package com.imagidoc.poleemploi.scanintelligent.web.rest.vm;

public class ScanInfoVM {

	private String fileName;
	private int pageCount;
	private long size;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String name) {
		this.fileName = name;
	}

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}
}
