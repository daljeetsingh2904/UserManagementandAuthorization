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
public class PopDataMatrixProcessor implements Runnable {
	private static final Logger logger = Logger.getLogger(PopDataProcessor.class);
	private String queryCondition;
	private String serviceName;
	private String pageName;
	private String queryColumns;
	private String dateCondition;
	private String startDate;
	private String endDate;
	
	public PopDataMatrixProcessor(String queryCondition,String serviceName,String pageName,String queryColumns,String dateCondition,String startDate,String endDate) {
		this.queryCondition=queryCondition;
		this.serviceName=serviceName;
		this.pageName=pageName;
		this.queryColumns=queryColumns;
		this.dateCondition=dateCondition;
		this.startDate=startDate;
		this.endDate=endDate;
	}
	
	public void run(){
		String htmlData=null;
		String password = null;
		String data = null;
		String []data1 = null;
		try {
			Email email=DbHandler.getInstance().getEmailDetails("PopReportsExternalMatrix",this.serviceName,this.startDate,this.endDate);
			data=DbHandler.getInstance().popDataReportsCsv(this.queryCondition,this.serviceName,EncryptionValues.valueOf(this.pageName).getValue(),this.queryColumns,this.dateCondition,email);
			logger.info("html data  is "+htmlData);
			data1 = data.split("@@");
			htmlData = data1[0];
			password=data1[1];
			email.setEmailBody(email.getEmailBody().replace("%CONTENT", htmlData));
			Utility.sendMail(email, this.serviceName);
			
			email=DbHandler.getInstance().getEmailDetails("PopReportsInternalMatrix",this.serviceName,this.startDate,this.endDate);
			email.setFileName(email.getFileName().replace(email.getFileName().split("\\.")[1],"zip"));
			email.setEmailBody(email.getEmailBody().replace("%CONTENT", htmlData));
			Utility.sendMail(email, this.serviceName);
			email=DbHandler.getInstance().getEmailDetails("PopReportsInternalMatrixPassword",this.serviceName,this.startDate,this.endDate);
			email.setEmailBody(email.getEmailBody().replace("%CONTENT", password));
			Utility.sendMail(email, this.serviceName);
			logger.info("pop data password For "+this.serviceName+" is "+password + "-->"+email.getEmailBody());
		}catch(Exception exception) {		
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString).collect(Collectors.joining("\n")));			
		}
	}
}