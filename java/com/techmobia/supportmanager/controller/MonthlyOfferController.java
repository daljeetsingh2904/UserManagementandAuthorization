package com.techmobia.supportmanager.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.techmobia.supportmanager.model.DataUpload;
import com.techmobia.supportmanager.services.Constants;
import com.techmobia.supportmanager.services.DbHandler;
import com.techmobia.supportmanager.services.PropertyHandler;
import com.techmobia.supportmanager.services.Utility;

/**
 * Servlet implementation class MonthlyOfferController
 */
public class MonthlyOfferController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(MonthlyOfferController.class);
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MonthlyOfferController() {
        super();
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
    @Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		try {
			doPost(request, response);
		}catch(Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString).collect(Collectors.joining("\n")));
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		PropertyConfigurator.configure(Constants.LOG4JFILEPATH);
		DataUpload dataupload=new DataUpload();
		List<String> dataUploadList=new ArrayList<>();
    	int existenceFlag=0;
		try {
			if(ServletFileUpload.isMultipartContent(request)){
				List<FileItem> multiparts = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
                for (FileItem item : multiparts) {
                    if(!item.isFormField()){                    	
                    	dataupload.setFileName(new File(item.getName()).getName().trim().replaceAll("[^a-zA-Z0-9.]", "").replace("\\.", Utility.getCurrentDatetime().replace("T", "")+"."));
                    	dataupload.setFilePath(Constants.MONTHLYOFFERUPLOADIRECTORY+File.separator+Utility.getCurrentDatetime().replace("T", " ").split(" ")[0]+File.separator+dataupload.getSiteId()+File.separator+dataupload.getOfferTypeId());
                    	existenceFlag=Utility.isFileDirectoryExists(dataupload.getFilePath(),dataupload.getFileName());
                    	if(existenceFlag==1) {
                    		dataupload.setUploadFileName(new File(dataupload.getFilePath() + File.separator + dataupload.getFileName()));
                            item.write(dataupload.getUploadFileName());
                    	}else {
                    		logger.info(dataupload.getFileName()+" File Overwritten");
                    		dataupload.setUploadFileName(new File(dataupload.getFilePath() + File.separator + dataupload.getFileName()));
                    		Files.deleteIfExists(Paths.get(dataupload.getFilePath()+"/"+dataupload.getFileName()));
                    		item.write(dataupload.getUploadFileName());
                    	}
                    	DbHandler.getInstance().addPromptDetails(dataupload);
                    }else{
                        String fieldName = item.getFieldName();
                        if(fieldName.equalsIgnoreCase(Constants.MONTHLYOFFERCOLUMNS.split(",")[0])){
                        	dataupload.setOfferTypeId(Integer.parseInt(item.getString().trim()));
                        }else if(fieldName.equalsIgnoreCase("Monthly"+Constants.MONTHLYOFFERCOLUMNS.split(",")[2])){
                        	dataupload.setSiteId(Integer.parseInt(item.getString().trim().split("-")[0]));
                        	dataupload.setSiteName(item.getString().trim().split("-")[1]);
                        }else if(fieldName.equalsIgnoreCase(Constants.MONTHLYOFFERCOLUMNS.split(",")[3])){
                        	dataupload.setFlowtypeId(Integer.parseInt(item.getString().trim()));
                        }else if(fieldName.equalsIgnoreCase(Constants.MONTHLYOFFERCOLUMNS.split(",")[4])){
                        	dataupload.setNumberOfOffers(Integer.parseInt(item.getString().trim()));
                        }else if(fieldName.equalsIgnoreCase(Constants.MONTHLYOFFERCOLUMNS.split(",")[5])){
                        	dataupload.setTotalOfferCount(item.getString().trim());
                        }else if(fieldName.equalsIgnoreCase(Constants.MONTHLYOFFERCOLUMNS.split(",")[6])){
                        	dataupload.setOfferStartDate(item.getString().trim());
                        }else if(fieldName.equalsIgnoreCase(Constants.MONTHLYOFFERCOLUMNS.split(",")[7])){
                        	dataupload.setOfferEndDate(item.getString().trim());
                        }else if(fieldName.equalsIgnoreCase(Constants.MONTHLYOFFERCOLUMNS.split(",")[8])){
                        	dataupload.setOfferCategory(Integer.parseInt(item.getString().trim()));
                        }else if(fieldName.equalsIgnoreCase(Constants.MONTHLYOFFERCOLUMNS.split(",")[9])){
                        	dataupload.setEmpId(Integer.parseInt(item.getString().trim()));
                        }else if(fieldName.startsWith(Constants.MONTHLYOFFEROTHERCOLUMNS.split(",")[0])){
                       		dataupload.setOfferName(dataupload.getOfferName()+","+item.getString().trim());
                        }else if(fieldName.startsWith(Constants.MONTHLYOFFEROTHERCOLUMNS.split(",")[1])){
                        	dataupload.setMessageText(dataupload.getMessageText()+"@@&&"+item.getString().trim());
                        }else if(fieldName.startsWith(Constants.MONTHLYOFFEROTHERCOLUMNS.split(",")[3])){
                        	dataupload.setBrandPrice(dataupload.getBrandPrice()+","+item.getString().trim());
                        }else if(fieldName.startsWith(Constants.MONTHLYOFFEROTHERCOLUMNS.split(",")[4])){
                        	dataupload.setBrandDiscount(dataupload.getBrandDiscount()+","+item.getString().trim());
                        }else if(fieldName.equalsIgnoreCase("action")){
                        	dataupload.setAction(item.getString().trim());
                        }
                    }
                }
                //Utility.moveFiles(dataupload.getFilePath(),dataupload.getFilePath()+File.separator+dataupload.getSiteId()+File.separator+dataupload.getOfferTypeId());
                dataUploadList=DbHandler.getInstance().addMonthlyDataUploadDetails(dataupload);
                logger.info("DataUploadList-->"+dataUploadList.toString());
			}else {
				logger.info("Client File Not Uploaded");
			}			
    	}catch(Exception exception) {		
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString).collect(Collectors.joining("\n")));
		}finally {
			try {				
				response.sendRedirect(request.getHeader("Referer"));
			}catch(Exception exception) {		
				logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString).collect(Collectors.joining("\n")));
			}			
		}
	}
}