package com.techmobia.supportmanager.controller;

import java.io.File;
import java.io.IOException;
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
import com.techmobia.supportmanager.services.FileReader;
import com.techmobia.supportmanager.services.Utility;

/**
 * Servlet implementation class FileUploadController
 */
public class FileUploadController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(FileUploadController.class);
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public FileUploadController() {
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
		FileReader filereader;
		DataUpload dataupload=new DataUpload();
		List<String> dataUploadList=new ArrayList<>();
    	int existenceFlag=0;
		try {
			if(ServletFileUpload.isMultipartContent(request)){
				List<FileItem> multiparts = new ServletFileUpload(new DiskFileItemFactory()).parseRequest(request);
                for (FileItem item : multiparts) {
                    if(!item.isFormField()){                    	
                    	dataupload.setFileName(new File(item.getName()).getName().trim().replaceAll("[^a-zA-Z0-9.]", "").replace("\\.", Utility.getCurrentDatetime().toString().replace("T", "")+"."));
                    	dataupload.setFilePath(Constants.UPLOADIRECTORY+File.separator+Utility.getCurrentDatetime().toString().replace("T", " ").split(" ")[0]);
                    	existenceFlag=Utility.isFileDirectoryExists(dataupload.getFilePath(),dataupload.getFileName());
                    	if(existenceFlag==1) {
                    		dataupload.setUploadFileName(new File(dataupload.getFilePath() + File.separator + dataupload.getFileName()));
                            item.write(dataupload.getUploadFileName());
                    	}else {
                    		dataupload.setUploadFileName(new File("NA"));
                    		logger.info(dataupload.getFileName()+" File Already Exists");
                    	}
                    }else{
                        String fieldName = item.getFieldName();
                        if(fieldName.equalsIgnoreCase(Constants.DATAUPLOADCOLUMNS.split(",")[0])){
                        	dataupload.setOfferTypeId(Integer.parseInt(item.getString().trim()));
                        }else if(fieldName.equalsIgnoreCase(Constants.DATAUPLOADCOLUMNS.split(",")[1])){
                        	dataupload.setBranchName(item.getString().trim().split("@")[0]);
                        	dataupload.setBranchCode(item.getString().trim().split("@")[1]);
                        	dataupload.setProjectId(Integer.parseInt(item.getString().trim().split("@")[2]));
                        }else if(fieldName.equalsIgnoreCase(Constants.DATAUPLOADCOLUMNS.split(",")[6])){
                        	dataupload.setEmpId(Integer.parseInt(item.getString().trim()));
                        }else if(fieldName.equalsIgnoreCase(Constants.DATAUPLOADCOLUMNS.split(",")[7])){
                        	dataupload.setTotalOfferCount(item.getString().trim());
                        }
                    }
                }
                dataUploadList=DbHandler.getInstance().addDataUploadDetails(dataupload);
                logger.info(dataUploadList.toString());
                filereader=new FileReader(dataupload.getFilePath(),dataupload.getFileName(),dataUploadList.get(0).split("@")[1]);
                Thread filethread=new Thread(filereader);
                filethread.start();
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