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
public class ItrialBrandRepository {
	private static final Logger logger = Logger.getLogger(ItrialBrandRepository.class);	
	private static ItrialBrandRepository mInstance = null;
    private static Map<String,Integer> mRepository=new HashMap<>();

    private ItrialBrandRepository() {
    }

    public synchronized static ItrialBrandRepository getInstance() {                
        if (mInstance == null) {
            mInstance = new ItrialBrandRepository();
            makeRepository();
        }
        return mInstance;
    }

    private static void makeRepository() {
        try{	
			mRepository = DbHandler.getInstance().fetchBrand();			        	           
            logger.info("Itrial Brand Repository is Successfully Made");     
        }catch(Exception e){           
            logger.error(e);
        }
    }
    
    public static void reLoadRepository() {
    	mRepository = new HashMap<>();
        makeRepository();
    }
    
    public Integer getValue(String key){
        return mRepository.getOrDefault(key, 0);
    }
}
