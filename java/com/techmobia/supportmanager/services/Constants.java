/**
 * 
 */
package com.techmobia.supportmanager.services;

/**
 * @author vinay.sethi
 *
 */
public class Constants {
	
	private Constants() {		
	}
	
	public static final int VALID=100;
	public static final int INVALID=101;
	public static final int INVALID_DBNAME=102;
	public static final int NO_DID_CONFIGURE=103;
	public static final int ERROR_PROJECT_SITE_MAPPING=104;
	public static final int ERROR_MISS_CALL_CONFIG=105;
	public static final String LOG4JFILEPATH=PropertyHandler.getInstance().getValue("log4j_filepath");
	public static final String OFFERCODEUPLOADCOLUMNS = "serviceName,brandInput,brandName,siteName,projectId,startDate,endDate,status";
	public static final String UPLOADIRECTORYOFFERCODE = PropertyHandler.getInstance().getValue("uploadedOfferCodeFilePath");
	public static final String CDRDATA="cdrData";
	public static final String CDRCOLUMNS="cdrColumns";
	public static final String BILLINGDATA="billingData";
	public static final String BILLINGCOLUMNS="billingColumns";
	public static final String DATAPPENDER = "@&*@";
	public static final String DATAUPLOADCOLUMNS = "OfferTypeId,BranchName,BranchCode,ProjectId,FileName,UploadedFileName,EmpId,TotalOfferCount,FlowTypeId";
	public static final String MONTHLYOFFERCOLUMNS = "OfferTypeSiteId,SiteId,SiteName,FlowTypeId,NumberofOffers,TotalOfferCount,OfferStartDate,OfferEndDate,OfferCategory,EmployeeId";
	public static final String MONTHLYOFFEROTHERCOLUMNS="OfferName,MessageText,FileName,BrandPrice,BrandDiscount";
	public static final String TELECALLINGDATA="teleCallingData";
	public static final String TELECALLINGCOLUMNS="teleCallingColumns";	
	public static final String BRANDDATA="itrialBrandWiseData";
	public static final String BRANDCOLUMNS="itrialBrandWiseColumns";
	public static final String MISSCALLDATA="missCallData";
	public static final String MISSCALLCOLUMNS="missCallColumns";
	public static final String UPLOADIRECTORY = PropertyHandler.getInstance().getValue("uploadedFilePath");
	public static final String MONTHLYOFFERUPLOADIRECTORY = PropertyHandler.getInstance().getValue("uploadMonthlyOfferFilePath");
	public static final String OBDCALLDATA="obdCallData";
	public static final String OBDCALLCOLUMNS="obdCallColumns";
	public static final String SMSDATA="smsData";
	public static final String SMSCOLUMNS="smsColumns";
	public static final String OPTINDATA="optinData";
	public static final String OPTINCOLUMNS="optinColumns";
	public static final String FINALLYEXCEPTION="Finally block-->";
	public static final String COLUMNS="COLUMNS";
	public static final String SERVICENAME="serviceName";
	public static final String SECRET="SECRET";
	public static final String DOWNLOADPAMPERSREPORT="downloadpampersreport";
}