/**
 * 
 */
package com.techmobia.supportmanager.services;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * @author vinay.sethi
 *
 */
public class UserTypeRepository {
	
	private static final Logger logger = Logger.getLogger(UserTypeRepository.class);
	
	private static UserTypeRepository mInstance = null;
    private static Map<Integer,String> mRepository=new HashMap<>();

    private UserTypeRepository() {
    }

    public synchronized static UserTypeRepository getInstance() {                
        if (mInstance == null) {
            mInstance = new UserTypeRepository();
            makeRepository();
        }
        return mInstance;
    }

    private static void makeRepository() {
        try{	
			mRepository = DbHandler.getInstance().fetchUserType();			        	           
            logger.info("UserType Repository is Successfully Made");     
        }catch(Exception e){           
            logger.error(e);
        }
    }
    
    public static void reLoadRepository() {
    	mRepository = new HashMap<>();
        makeRepository();
    }
    
    public String getValue(int key){
        return mRepository.getOrDefault(key, "Not Applicable");
    }
}
