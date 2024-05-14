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
public class CampaignNameRepository {
private static final Logger logger = Logger.getLogger(CampaignNameRepository.class);
	
	private static CampaignNameRepository mInstance = null;
    private static List<String> mRepository=new ArrayList<>();

    private CampaignNameRepository() {
    }

    public static synchronized CampaignNameRepository getInstance() {                
        if (mInstance == null) {
            mInstance = new CampaignNameRepository();
            makeRepository();
        }
        return mInstance;
    }

    private static void makeRepository() {
        try{
			mRepository = DbHandler.getInstance().fetchCampaignName();			        	           
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