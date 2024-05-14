package com.techmobia.supportmanager.model;

import java.util.List;
import java.util.Map;

public class SideMenuData {
  
	private Map<String, SideMenu> sideMenuDataMap;
    private List<SideMenu> sideMenuDataList;

    // Constructors, getters, and setters for the two fields
    // Define getters and setters for the two fields
    public Map<String, SideMenu> getSideMenuDataMap() {
        return sideMenuDataMap;
    }

    public void setSideMenuDataMap(Map<String, SideMenu> sideMenuDataMap) {
        this.sideMenuDataMap = sideMenuDataMap;
    }

    public List<SideMenu> getSideMenuDataList() {
        return sideMenuDataList;
    }

    public void setSideMenuDataList(List<SideMenu> sideMenuDataList) {
        this.sideMenuDataList = sideMenuDataList;
    }
}

