/**
 * 
 */
package com.techmobia.supportmanager.services;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.techmobia.supportmanager.model.Email;

/**
 * @author vinay.sethi
 *
 */
public class TeleCallingDataMatrixProcessor implements Runnable {
	private static final Logger logger = Logger.getLogger(TeleCallingDataProcessor.class);
	private String queryCondition;
	private String serviceName;
	private String pageName;
	private String queryColumns;
	private String dateCondition;
	private String startDate;
	private String endDate;
	
	public TeleCallingDataMatrixProcessor(String queryCondition,String serviceName,String pageName,String queryColumns,String dateCondition,String startDate,String endDate) {
		this.queryCondition=queryCondition;
		this.serviceName=serviceName;
		this.pageName=pageName;
		this.queryColumns=queryColumns;
		this.dateCondition=dateCondition;
		this.startDate=startDate;
		this.endDate=endDate;
	}
	
	public void run(){
		String password=null;
		try {
			Email email=DbHandler.getInstance().getEmailDetails("TeleCallingMatrix",this.serviceName,this.startDate,this.endDate);
			password=DbHandler.getInstance().fetchTeleCallingDataCsv(this.queryCondition,this.serviceName,EncryptionValues.valueOf(this.pageName).getValue(),this.queryColumns,this.dateCondition,email);
			email.setFileName(email.getFileName().replace("%YESTERDAYDATE", this.dateCondition).replace(email.getFileName().split("\\.")[1],"zip"));
			email.setEmailBody(email.getEmailBody());
			Utility.sendMail(email, this.serviceName);	
			email=DbHandler.getInstance().getEmailDetails("TeleCallingMatrixPassword",this.serviceName,this.startDate,this.endDate);
			email.setEmailBody(email.getEmailBody().replace("%CONTENT", password));
			Utility.sendMail(email, this.serviceName);
			logger.info("Telecalling data password for "+this.serviceName+" is "+password + "-->"+email.getEmailBody());
		}catch(Exception exception) {		
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString).collect(Collectors.joining("\n")));			
		}
	}
}