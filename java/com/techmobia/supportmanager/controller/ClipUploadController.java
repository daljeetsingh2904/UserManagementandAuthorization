package com.techmobia.supportmanager.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
import com.techmobia.supportmanager.services.Utility;

/**
 * Servlet implementation class ClipUploadController
 */
public class ClipUploadController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(ClipUploadController.class);
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public ClipUploadController() {
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
                    }else{
                        String fieldName = item.getFieldName();
                        if(fieldName.equalsIgnoreCase("uniqueId")){
                        	dataupload.setDataId(Integer.parseInt(item.getString().trim()));
                        }else if(fieldName.equalsIgnoreCase(Constants.MONTHLYOFFERCOLUMNS.split(",")[9])){
                        	dataupload.setEmpId(Integer.parseInt(item.getString().trim()));
                        }else if(fieldName.startsWith("comments")){
                        	dataupload.setComments(dataupload.getBrandDiscount()+","+item.getString().trim());
                        }else if(fieldName.equalsIgnoreCase("action")){
                        	dataupload.setAction(item.getString().trim());
                        }else if(fieldName.equalsIgnoreCase(Constants.MONTHLYOFFERCOLUMNS.split(",")[0])){
                        	dataupload.setOfferTypeId("Traditional".equalsIgnoreCase(item.getString().trim())?1:2);
                        }else if(fieldName.equalsIgnoreCase(Constants.MONTHLYOFFERCOLUMNS.split(",")[2])){
                        	dataupload.setSiteName(item.getString().trim());
                        	dataupload.setSiteId(DbHandler.getInstance().getSiteId("itrial",dataupload.getSiteName()));
                        }
                    }
                }
                //Utility.moveFiles(dataupload.getFilePath(),dataupload.getFilePath()+File.separator+dataupload.getSiteId()+File.separator+dataupload.getOfferTypeId());
                DbHandler.getInstance().updateMonthlyClipDetails(dataupload);
			}else {
				logger.info("Client File Not Uploaded");
			}			
    	}catch(Exception exception) {		
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString).collect(Collectors.joining("\n")));
		}finally {
			try {				
				response.sendRedirect(request.getHeader("Referer").replace("edit", "view"));
			}catch(Exception exception) {		
				logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString).collect(Collectors.joining("\n")));
			}			
		}
	}
}