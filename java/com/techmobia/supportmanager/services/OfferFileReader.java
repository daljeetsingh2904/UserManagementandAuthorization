/**
 * 
 */
package com.techmobia.supportmanager.services;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

/**
 * @author vinay.sethi
 *
 */
public class OfferFileReader implements Runnable{
	private static final Logger logger = Logger.getLogger(OfferFileReader.class);
	
	private String action;
	private String fileName;
	private String filePath;
	private String fileUploadId;
	
	public OfferFileReader(String filePath,String fileName, String action,String fileUploadId) {
		this.action=action;
		this.filePath=filePath;
		this.fileName=fileName;
		this.fileUploadId=fileUploadId;
	}
	
	public void run() {
		String fileData=null;
		try {
			logger.info("Action-->"+this.action);
			if(!this.action.equalsIgnoreCase("addPrompts")) {
				fileData=Utility.readexcelfile(this.filePath, this.fileName, this.fileUploadId);
			}else {
				fileData=this.fileName+","+fileUploadId+Constants.DATAPPENDER;
			}
			DbHandler.getInstance().addOfferFileRecords(fileData,this.action,this.fileUploadId);
		}catch(Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString).collect(Collectors.joining("\n")));
		}
	}
}