package com.techmobia.supportmanager.model;

public class UserPermission {

	private String userName;
	private String password;
	private String emailId;
	private String mobileNumber;
	private String readModules;
	private String readWriteModules;
	private String userId;

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmailId() {
		return emailId;
	}

	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

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

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}
}
