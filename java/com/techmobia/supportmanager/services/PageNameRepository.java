package com.techmobia.supportmanager.services;

import java.util.LinkedHashMap;

import org.apache.log4j.Logger;

public class PageNameRepository {
private static final Logger logger = Logger.getLogger(PageNameRepository.class);
	
	private static PageNameRepository minstance = null;
    private static LinkedHashMap<String, String[]> mRepository=new LinkedHashMap<>();

    private PageNameRepository() {
    }

    public synchronized static PageNameRepository getInstance() {
        if (minstance == null) {
        	minstance = new PageNameRepository();
        }
        return minstance;
    }

    private static void makeRepository() {
        try{	
        	
			mRepository = DbHandler.getInstance().fetchChildData();			        	           
            logger.info("Page Name Repository is Successfully Made");     
        }catch(Exception e){           
            logger.error(e);
        }
    }
    
    public static void reLoadRepository() {
    	mRepository = new LinkedHashMap<>();
        makeRepository();
    }
    
    public LinkedHashMap<String, String[]> getValue(){
        return mRepository;
    }
}
