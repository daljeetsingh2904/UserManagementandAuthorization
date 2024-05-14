/**
 * 
 */
package com.techmobia.supportmanager.services;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;

/**
 * @author vinay.sethi
 *
 */
public class PampersDashboardProcessor implements Runnable {
	private static final Logger logger = Logger.getLogger(PampersDashboardProcessor.class);
	private String queryCondition;
	private String serviceName;
	private String pageName;
	private String queryColumns;
	private String dateCondition;
	private String filePath;
	private String reportingFileName;
	private String startDate;
	private String endDate;
	
	public PampersDashboardProcessor(String queryCondition,String serviceName,String pageName,String queryColumns,String dateCondition,String filePath,String reportingFileName,String startDate,String endDate) {
		this.queryCondition=queryCondition;
		this.serviceName=serviceName;
		this.pageName=pageName.toLowerCase();
		this.queryColumns=queryColumns;
		this.dateCondition=dateCondition;
		this.filePath=filePath;
		this.reportingFileName=reportingFileName;
		this.startDate=startDate;
		this.endDate=endDate;
	}
	
	public void run(){
String instanceId="NA";
		try {
			logger.info(this.serviceName+"_"+this.pageName+"backend_columns-->"+this.serviceName+"_"+this.pageName+"backend_datecondition");
			this.queryColumns=Utility.verifyData(PropertyHandler.getInstance().getValue(this.serviceName+"_"+this.pageName+"backend_columns"));
            this.dateCondition=Utility.verifyData(PropertyHandler.getInstance().getValue(this.serviceName+"_"+this.pageName+"backend_datecondition"));
			DbHandler.getInstance().fetchData(this.queryCondition.replace("trd.CreateDate", "od.coupon_redemptionDate"),this.serviceName,this.pageName+"backend",this.queryColumns,this.dateCondition,this.filePath,this.reportingFileName,"NA");
			DbHandler.getInstance().updatePampersDashboardRequest(this.startDate,this.endDate,this.serviceName);
			delete(7,".csv",this.filePath);
		}catch(Exception exception) {		
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString).collect(Collectors.joining("\n")));			
		}
	}
	
	public void delete(long days, String fileExtension,String dirPath) {
		 
        File folder = new File(dirPath);
 
        if (folder.exists()) {
 
            File[] listFiles = folder.listFiles();
 
            long eligibleForDeletion = System.currentTimeMillis() -
                (days * 24 * 60 * 60 * 1000);
 
            for (File listFile: listFiles) { 
                if (listFile.getName().endsWith(fileExtension) && listFile.lastModified() < eligibleForDeletion) { 
                    if (!listFile.delete()) { 
                        logger.info("Sorry Unable to Delete Files.."); 
                    }
                }
            }
        }
    }
}
