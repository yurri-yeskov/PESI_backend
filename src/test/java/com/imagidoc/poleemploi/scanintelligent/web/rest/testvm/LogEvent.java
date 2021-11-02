package com.imagidoc.poleemploi.scanintelligent.web.rest.testvm;

public class LogEvent {

	private String level;
	private String application;
	private String deviceName;
	private String serial;
	private String systemSoftware;
	private String mac;
	private String comment;

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

    public String getDeviceName() {
		return deviceName;
	}

	public void setDeviceName(String deviceName) {
		this.deviceName = deviceName;
	}

	public String getSerial() {
		return serial;
	}

	public void setSerial(String serial) {
		this.serial = serial;
	}

	public String getSystemSoftware() {
		return systemSoftware;
	}

	public void setSystemSoftware(String systemSoftware) {
		this.systemSoftware = systemSoftware;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
}
