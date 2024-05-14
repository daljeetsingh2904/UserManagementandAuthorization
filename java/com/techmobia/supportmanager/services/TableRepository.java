package com.techmobia.supportmanager.services;

import java.util.ArrayList;

import java.util.List;


import org.apache.log4j.Logger;


public class TableRepository {
private static final Logger logger = Logger.getLogger(TableRepository.class);
	
	private static TableRepository mInstance = null;
    private static List<String> mRepository=new ArrayList<>();

    private TableRepository() {
    }

    public static synchronized TableRepository getInstance(String databaseName) {                
        if (mInstance == null) {
            mInstance = new TableRepository();
            makeRepository(databaseName);
        }
        return mInstance;
    }

    private static void makeRepository(String databaseName) {
        try{	
			mRepository = DbHandler.getInstance().getTableNames(databaseName);			        	           
            logger.info("Table Repository is Successfully Made");     
        }catch(Exception e){           
            logger.error(e);
        }
    }
    
    public static void reLoadRepository(String databaseName) {
    	mRepository = new ArrayList<>();
        makeRepository(databaseName);
    }
    
    public List<String> getValue(){
        return mRepository;
    }
}
