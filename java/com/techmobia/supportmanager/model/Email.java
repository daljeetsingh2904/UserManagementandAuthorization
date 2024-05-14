/**
 * 
 */
package com.techmobia.supportmanager.model;

/**
 * @author vinay.sethi
 *
 */
public class Email {
	private String bccEmailAddress;
	private String ccEmailAddress;
	private String filePath;
	private String fileName;
	private String fromEmailAddress;
	private String emailBody;
	private String emailSubject;
	private String toEmailAddress;
	private int attachmentFlag;
	/**
	 * @return the bccEmailAddress
	 */
	public String getBccEmailAddress() {
		return bccEmailAddress;
	}
	/**
	 * @param bccEmailAddress the bccEmailAddress to set
	 */
	public void setBccEmailAddress(String bccEmailAddress) {
		this.bccEmailAddress = bccEmailAddress;
	}
	/**
	 * @return the ccEmailAddress
	 */
	public String getCcEmailAddress() {
		return ccEmailAddress;
	}
	/**
	 * @param ccEmailAddress the ccEmailAddress to set
	 */
	public void setCcEmailAddress(String ccEmailAddress) {
		this.ccEmailAddress = ccEmailAddress;
	}
	/**
	 * @return the filePath
	 */
	public String getFilePath() {
		return filePath;
	}
	/**
	 * @param filePath the filePath to set
	 */
	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}
	/**
	 * @return the fileName
	 */
	public String getFileName() {
		return fileName;
	}
	/**
	 * @param fileName the fileName to set
	 */
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	/**
	 * @return the fromEmailAddress
	 */
	public String getFromEmailAddress() {
		return fromEmailAddress;
	}
	/**
	 * @param fromEmailAddress the fromEmailAddress to set
	 */
	public void setFromEmailAddress(String fromEmailAddress) {
		this.fromEmailAddress = fromEmailAddress;
	}
	/**
	 * @return the emailBody
	 */
	public String getEmailBody() {
		return emailBody;
	}
	/**
	 * @param emailBody the emailBody to set
	 */
	public void setEmailBody(String emailBody) {
		this.emailBody = emailBody;
	}
	/**
	 * @return the emailSubject
	 */
	public String getEmailSubject() {
		return emailSubject;
	}
	/**
	 * @param emailSubject the emailSubject to set
	 */
	public void setEmailSubject(String emailSubject) {
		this.emailSubject = emailSubject;
	}
	/**
	 * @return the toEmailAddress
	 */
	public String getToEmailAddress() {
		return toEmailAddress;
	}
	/**
	 * @param toEmailAddress the toEmailAddress to set
	 */
	public void setToEmailAddress(String toEmailAddress) {
		this.toEmailAddress = toEmailAddress;
	}
	/**
	 * @return the attachmentFlag
	 */
	public int getAttachmentFlag() {
		return attachmentFlag;
	}
	/**
	 * @param attachmentFlag the attachmentFlag to set
	 */
	public void setAttachmentFlag(int attachmentFlag) {
		this.attachmentFlag = attachmentFlag;
	}
}