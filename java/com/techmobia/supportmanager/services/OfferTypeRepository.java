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
public class OfferTypeRepository {
	private static final Logger logger = Logger.getLogger(OfferTypeRepository.class);
	
	private static OfferTypeRepository mInstance = null;
    private static List<String> mRepository=new ArrayList<>();

    private OfferTypeRepository() {
    }

    public static synchronized OfferTypeRepository getInstance() {                
        if (mInstance == null) {
            mInstance = new OfferTypeRepository();
            makeRepository();
        }
        return mInstance;
    }

    private static void makeRepository() {
        try{	
			mRepository = DbHandler.getInstance().fetchOfferType();			        	           
            logger.info("OfferType Repository is Successfully Made");     
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