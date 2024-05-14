/**
 * 
 */
package com.techmobia.supportmanager.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * @author vinay.sethi
 *
 */
public class ServiceRepository {
	private static final Logger logger = Logger.getLogger(ServiceRepository.class);
	
	private static ServiceRepository mInstance = null;
    private static List<String> mRepository=new ArrayList<>();

    private ServiceRepository() {
    }

    public static synchronized ServiceRepository getInstance() {                
        if (mInstance == null) {
            mInstance = new ServiceRepository();
            makeRepository();
        }
        return mInstance;
    }

    private static void makeRepository() {
        try{	
			mRepository = DbHandler.getInstance().fetchServices();			        	           
            logger.info("Service Repository is Successfully Made");     
        }catch(Exception e){           
            logger.error(e);
        }
    }
    
    public static void reLoadRepository() {
    	mRepository = new ArrayList<>();
        makeRepository();
    }
    
    public List<String> getValue(){
        return mRepository;
    }
}