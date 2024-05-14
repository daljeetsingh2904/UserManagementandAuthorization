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
public class RetailerNumberFileReader implements Runnable{
	private static final Logger logger = Logger.getLogger(RetailerNumberFileReader.class);

	private String fileName;
	private String filePath;
	private String fileUploadId;
	
	public RetailerNumberFileReader(String filePath,String fileName,String fileUploadId) {
		this.filePath=filePath;
		this.fileName=fileName;
		this.fileUploadId=fileUploadId;
	}
	
	public void run() {
		String fileData=null;
		try {
			DbHandler.getInstance().updateDrcpFileStatus(this.fileUploadId);
			fileData=Utility.readexcelfile(this.filePath, this.fileName, this.fileUploadId);
			DbHandler.getInstance().addRetailerNumberFileRecords(fileData,fileUploadId);
		}catch(Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString).collect(Collectors.joining("\n")));
		}
	}
}