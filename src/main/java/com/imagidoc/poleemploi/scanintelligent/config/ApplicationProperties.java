package com.imagidoc.poleemploi.scanintelligent.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Pole Emploi Scan Intelligent.
 * <p>
 * Properties are configured in the application.yml file. See
 * {@link io.github.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = false)
public class ApplicationProperties {

	private String name;
	private String version;
	private String rootPath;
	private String dn005Url;
	private int targetDpi;
	private String peIdEnvironnement;
	private String peIdUtilisateur;
	private String peNomApplication;

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	public int getTargetDpi() {
		return targetDpi;
	}

	public void setTargetDpi(int targetDpi) {
		this.targetDpi = targetDpi;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDn005Url() {
		return dn005Url;
	}

	public void setDn005Url(String dn005Url) {
		this.dn005Url = dn005Url;
	}

	public String getPeIdEnvironnement() {
		return peIdEnvironnement;
	}

	public void setPeIdEnvironnement(String peIdEnvironnement) {
		this.peIdEnvironnement = peIdEnvironnement;
	}

	public String getPeIdUtilisateur() {
		return peIdUtilisateur;
	}

	public void setPeIdUtilisateur(String peIdUtilisateur) {
		this.peIdUtilisateur = peIdUtilisateur;
	}

	public String getPeNomApplication() {
		return peNomApplication;
	}

	public void setPeNomApplication(String peNomApplication) {
		this.peNomApplication = peNomApplication;
	}
}
