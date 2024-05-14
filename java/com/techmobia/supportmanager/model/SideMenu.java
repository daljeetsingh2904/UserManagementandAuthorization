package com.techmobia.supportmanager.model;

import java.util.List;
import java.util.Map;

public class SideMenu {

	private String pageName;
	private String pageTitle;
	private String pageLink;
	private String parentStatus;
	private String pageClass;
	private String parentName;
	private String parentId;
	private String readModules;
	private String readWriteModules;
	private String subPageLink;

	public String getReadModules() {
		return readModules;
	}

	public void setReadModules(String readModules) {
		this.readModules = readModules;
	}

	public String getReadWriteModules() {
		return readWriteModules;
	}

	public void setReadWriteModules(String readWriteModules) {
		this.readWriteModules = readWriteModules;
	}

	public String getPageName() {
		return pageName;
	}

	public void setPageName(String pageName) {
		this.pageName = pageName;
	}

	public String getPageTitle() {
		return pageTitle;
	}

	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}

	public String getPageLink() {
		return pageLink;
	}

	public void setPageLink(String pageLink) {
		this.pageLink = pageLink;
	}

	public String getParentStatus() {
		return parentStatus;
	}

	public void setParentStatus(String parentStatus) {
		this.parentStatus = parentStatus;
	}

	public String getPageClass() {
		return pageClass;
	}

	public void setPageClass(String pageClass) {
		this.pageClass = pageClass;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

	public String getParentId() {
		return parentId;
	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
	}

	public String getSubPageLink() {
		return subPageLink;
	}

	public void setSubPageLink(String subPageLink) {
		this.subPageLink = subPageLink;
	}

}
