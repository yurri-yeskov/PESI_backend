package com.imagidoc.poleemploi.scanintelligent.web.rest.vm;

import javax.validation.constraints.NotNull;

public class ClientLogVM {

	private String level;
	
	private String application;

	private String deviceName;

	private String serial;

	private String systemSoftware;

	private String ip;

	private String mac;

	@NotNull
	private String comment;

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
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

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
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

	@Override
    public String toString() {
		return "LogVM{"
				+ "application='" + application + '\''
				+ ", deviceName='" + deviceName + '\''
				+ ", serial='" + serial + '\''
				+ ", systemSoftware='" + systemSoftware + '\''
				+ ", ip='" + ip + '\''
				+ ", mac='" + mac + '\''
				+ ", comment='" + comment + '\''
				+ '}';
    }
}
