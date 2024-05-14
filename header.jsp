<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@include file="/session.jsp"%>
<%@page import="com.techmobia.supportmanager.services.*"%>
<%@page import="com.techmobia.supportmanager.model.UserPermission"%>
<%@page import="com.techmobia.supportmanager.model.SideMenu"%>
<%@page import="com.techmobia.supportmanager.model.SideMenuData"%>


<%@page import="java.util.*"%>
<%@page import="org.apache.commons.lang.*"%>
<!doctype html>
<html class="no-js" lang="en">
<head>
<meta charset="utf-8">
<meta http-equiv="x-ua-compatible" content="ie=edge">
<title>Support Manager</title>
<meta name="description" content="">
<meta name="viewport" content="width=device-width, initial-scale=1">
<!-- favicon
		============================================ -->
<link rel="shortcut icon" type="image/x-icon" href="img/favicon.ico">
<link rel="stylesheet" type="text/css"
	href="css/jquery.datetimepicker.css" />
	
	<!--  -->
	<link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/css/bootstrap.min.css">
	
<!-- Google Fonts
		============================================ -->
<link
	href="https://fonts.googleapis.com/css?family=Roboto:100,300,400,700,900"
	rel="stylesheet">
<!-- Bootstrap CSS
		============================================ -->
<link rel="stylesheet" href="css/bootstrap.min.css">
<!-- Bootstrap CSS
		============================================ -->
<link rel="stylesheet" href="css/font-awesome.min.css">
<!-- owl.carousel CSS
		============================================ -->
<link rel="stylesheet" href="css/owl.carousel.css">
<link rel="stylesheet" href="css/owl.theme.css">
<link rel="stylesheet" href="css/owl.transitions.css">
<!-- animate CSS
		============================================ -->
<link rel="stylesheet" href="css/animate.css">
<!-- normalize CSS
		============================================ -->
<link rel="stylesheet" href="css/normalize.css">
<!-- meanmenu icon CSS
		============================================ -->
<link rel="stylesheet" href="css/meanmenu.min.css">
<!-- main CSS
		============================================ -->
<link rel="stylesheet" href="css/main.css">
<!-- educate icon CSS
		============================================ -->
<link rel="stylesheet" href="css/educate-custon-icon.css">
<!-- morrisjs CSS
		============================================ -->
<link rel="stylesheet" href="css/morrisjs/morris.css">
<!-- mCustomScrollbar CSS
		============================================ -->
<link rel="stylesheet"
	href="css/scrollbar/jquery.mCustomScrollbar.min.css">
<!-- metisMenu CSS
		============================================ -->
<link rel="stylesheet" href="css/metisMenu/metisMenu.min.css">
<link rel="stylesheet" href="css/metisMenu/metisMenu-vertical.css">
<!-- calendar CSS
		============================================ -->
<link rel="stylesheet" href="css/calendar/fullcalendar.min.css">
<link rel="stylesheet" href="css/calendar/fullcalendar.print.min.css">
<!-- x-editor CSS
    ============================================ -->
<link rel="stylesheet" href="css/editor/select2.css">
<link rel="stylesheet" href="css/editor/datetimepicker.css">
<link rel="stylesheet" href="css/editor/bootstrap-editable.css">
<link rel="stylesheet" href="css/editor/x-editor-style.css">
<!-- modals CSS
		============================================ -->
<link rel="stylesheet" href="css/modals.css">
<!-- forms CSS
		============================================ -->
<link rel="stylesheet" href="css/form/all-type-forms.css">
<!-- style CSS
    <!-- normalize CSS
		============================================ -->
<link rel="stylesheet" href="css/data-table/bootstrap-table.css">
<link rel="stylesheet" href="css/data-table/bootstrap-editable.css">
<!-- style CSS
		============================================ -->
<link rel="stylesheet" href="style.css">
<!-- responsive CSS
		============================================ -->
<link rel="stylesheet" href="css/responsive.css">
<!-- modernizr JS
		============================================ -->
<script src="js/vendor/modernizr-2.8.3.min.js"></script>
<link rel="stylesheet"
	href="//code.jquery.com/ui/1.12.1/themes/base/jquery-ui.css">
<link href="http://www.jqueryscript.net/css/jquerysctipttop.css"
	rel="stylesheet" type="text/css">
<link href="css/paging.css" rel="stylesheet" type="text/css" />
<style>
body {
	background-color: #fafafa;
}

.container {
	margin: 150px auto;
	font-family: 'Roboto';
}

table {
	margin: 10px 0;
}
</style>
</head>
<body>
	<!--[if lt IE 8]>
		<p class="browserupgrade">You are using an <strong>outdated</strong> browser. Please <a href="http://browsehappy.com/">upgrade your browser</a> to improve your experience.</p>
	<![endif]-->
	<!-- Start Left menu area -->
	<div class="left-sidebar-pro">
		<nav id="sidebar" class="" aria-label="Menu">
			<div class="sidebar-header">
				<a href="#"><img class="main-logo" src="img/logo/logo.jpg"
					alt="" /></a> <strong><a href="#"><img
						src="img/logo/logo.jpg" alt="" /></a></strong>
			</div>
			<div class="left-custom-menu-adp-wrap comment-scrollbar">
				<nav class="sidebar-nav left-sidebar-menu-pro" aria-label="Menu">
					<ul class="metismenu" id="menu1">
						<%
                        if(session.getAttribute("userType")!=null && session.getAttribute("userType").equals("pampersReport")){
                        	%>
						<li><a title="Pampers Report Data"
							href="reporting_pampersSummary.jsp"><span
								class="mini-sub-pro">Pampers Reporting</span></a></li>
						<%
                        }else if(session.getAttribute("userType")!=null && session.getAttribute("userType").equals("pampersBandhanReport")){
                        	%>
						<li><a title="Pampers Bandhan Report Data"
							href="reporting_pampersBandhanSummary.jsp"><span
								class="mini-sub-pro">Pampers Bandhan Reporting</span></a></li>
						<%
                        }else if(session.getAttribute("userType")!=null && session.getAttribute("userType").equals("mindtreelatching")){
                        	%>
						<li><a title="Mindtree Latching Report Data"
							href="reporting_mindtreeLatchingSummary.jsp"><span
								class="mini-sub-pro">Latching Reporting</span></a></li>
						<%
                        }else if(session.getAttribute("userType")!=null && session.getAttribute("userType").equals("gillettePomeReport")){
                        	%>
						<li><a title="Gillette Pome Report Data"
							href="reporting_gillettePomeData.jsp"><span
								class="mini-sub-pro">Gillette Pome Reporting</span></a></li>
						<%
                        }else{
                        	%>

						<%
    							
    						        String instanceId=session.getAttribute("instanceId").toString();
    						System.out.println("InstanceId is ---->>> "+instanceId+" email id is ---->>> " + session.getAttribute("userEmail"));
    								String userEmail = session.getAttribute("userEmail").toString();
    								String userName = session.getAttribute("userName").toString();
    								SideMenuData sideMenuData = DbHandler.getInstance().fetchSideData(userEmail,instanceId);
    								Map<String, SideMenu> sideMenuDataMap = sideMenuData.getSideMenuDataMap();
    								List<SideMenu> sideMenuDataList = sideMenuData.getSideMenuDataList();
    								String pageLinkList = "";
    								String subPageLinkList = "";
    								for (SideMenu sideMenu : sideMenuDataList) {
    						%>

						<li><a class="has-arrow"
							href="<%=PropertyHandler.getInstance().getValue("success_login_page")%>">
								<span class="educate-icon educate-home icon-wrap"></span> <span
								class="mini-click-non"><%=sideMenu.getParentName()%></span>
						</a> <%
     	pageLinkList += sideMenu.getPageLink() + ",";
     			subPageLinkList += sideMenu.getSubPageLink() + ",";

     			String[] pageNameArr = sideMenu.getPageName().split(",");
     			String[] pageTitleArr = sideMenu.getPageTitle().split(",");
     			String[] pageLinkArr = sideMenu.getPageLink().split(",");

     			for (int i = 0; i < pageNameArr.length; i++) {
     %>
							<ul class="submenu-angle" aria-expanded="true">
								<li><a title=<%=pageTitleArr[i]%> href=<%=pageLinkArr[i]%>><span
										class=<%=sideMenu.getPageClass()%>><%=pageNameArr[i]%></span></a></li>
							</ul> <%
     	}
     		}
     		session.setAttribute("pagelink", pageLinkList);
     		session.setAttribute("subPageLink", subPageLinkList);
     		List<String> pageNames = new ArrayList<>();
     		List<String> pageLinks = new ArrayList<>();
     		for (Map.Entry<String, SideMenu> entry : sideMenuDataMap.entrySet()) {
     			String pageName = entry.getKey(); // Get the page name (key)
     			SideMenu sideMenu = entry.getValue(); // Get the SideMenu object (value)

     			// Store page name and page link in separate lists
     			pageNames.add(pageName);
     			pageLinks.add(sideMenu.getPageLink());

     		}
     		session.setAttribute("pageNameMap", pageNames);
     		session.setAttribute("pageLinkMap", pageLinks);

                        }
	                     %></li>
					</ul>
				</nav>
			</div>
		</nav>
	</div>
	<!-- End Left menu area -->
	<!-- Start Welcome area -->
	<div class="all-content-wrapper">
		<div class="container-fluid">
			<div class="row">
				<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
					<div class="logo-pro">
						<a href="#"><img class="main-logo" src="img/logo/logo.jpg"
							alt="" /></a>
					</div>
				</div>
			</div>
		</div>
		<div class="header-advance-area">
			<div class="header-top-area">
				<div class="container-fluid">
					<div class="row">
						<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
							<div class="header-top-wraper">
								<div class="row">
									<div class="col-lg-1 col-md-0 col-sm-1 col-xs-12">
										<div class="menu-switcher-pro">
											<button type="button" id="sidebarCollapse"
												class="btn bar-button-pro header-drl-controller-btn btn-info navbar-btn">
												<em class="educate-icon educate-nav"></em>
											</button>
										</div>
									</div>
									<div class="col-lg-6 col-md-7 col-sm-6 col-xs-12">
										<div class="header-top-menu tabl-d-n"></div>
									</div>
									<div class="col-lg-5 col-md-5 col-sm-12 col-xs-12">
										<div class="header-right-info">
											<ul class="nav navbar-nav mai-top-nav header-right-menu">
												<li class="nav-item"><a href="#" data-toggle="dropdown"
													role="button" aria-expanded="false"
													class="nav-link dropdown-toggle"> <img
														src="img/product/pro4.jpg" alt="" /> <span
														class="admin-name"><%=WordUtils.capitalize((String) session.getAttribute("userName"))%></span>
														<em class="fa fa-angle-down edu-icon edu-down-arrow"></em>
												</a>
													<ul role="menu"
														class="dropdown-header-top author-log dropdown-menu animated zoomIn">
														<li><a href="logout.jsp"><span
																class="edu-icon edu-locked author-log-ic"></span>Log Out</a>
														</li>
														<li class="nav-item nav-setting-open"><a href="#"
															data-toggle="dropdown" role="button"
															aria-expanded="false" class="nav-link dropdown-toggle"><em
																class="educate-icon educate-menu"></em></a>
															<div role="menu"
																class="admintab-wrap menu-setting-wrap menu-setting-wrap-bg dropdown-menu animated zoomIn">
															</div></li>
													</ul></li>
											</ul>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
			<div class="basic-form-area mg-b-15">
				<div class="container-fluid">
					<div class="row">
						<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
							<div class="sparkline10-list mt-b-30">
								<div class="sparkline10-graph">
									<div class="basic-login-form-ad">
										<div class="row">
											<div class="col-lg-12 col-md-12 col-sm-12 col-xs-12">
												<div class="basic-login-inner inline-basic-form">
													<form method="post" action="RequestController"
														onsubmit="return validate_fields_value('date');">
														<div class="form-group-inner">
															<div class="row">
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-16">
																	<% 
	                                                            	String pageName = request.getRequestURI().split("\\.")[0].split("_")[1].toLowerCase();
																	String fileName = request.getRequestURI().split("\\.")[0]
																			.substring(request.getRequestURI().split("\\.")[0].lastIndexOf('/') + 1);
																	System.out.println("PageName=" + fileName);
	                                                            	String[] defaultPages = PropertyHandler.getInstance().getValue("defaultPages").split(",");

																		String pageLink = session.getAttribute("pagelink").toString();
																		List<String> pageLinkArr = Arrays.asList(pageLink.split(","));

																		//System.out.println("read write is ---->>>> " + session.getAttribute("readwriteValues"));
																		// for storing sub childs
																		String subPageLink = session.getAttribute("subPageLink").toString();
																		List<String> subPageLinkArr = Arrays.asList(subPageLink.split(","));

																		System.out.println(fileName + ".jsp");

																		boolean found = (pageLinkArr.contains(fileName + ".jsp") || subPageLinkArr.contains(fileName + ".jsp")
																				|| Arrays.toString(defaultPages).contains(fileName + ".jsp"));
																		if (!found) {
																	%>
																	<script>
																		window.location.href = "view_errorpage.jsp";
																	</script>

																	<%
																		}

																		if (!fileName.startsWith("add") && !fileName.startsWith("edit") && !fileName.equals("view_userData")
																				&& !fileName.equals("view_home") && !fileName.equals("reporting_pixelCampaigns")) {
																	%>

																	<select name="serviceName"
																		class="form-control basic-ele-mg-b-10 responsive-mg-b-10"
																		id="serviceName" required>
																		<option value="">Select Service</option>
																		<%
				                                						
				                                						String validServices=Utility.verifyData(PropertyHandler.getInstance().getValue(pageName+"_services"));
				                                						List<String> services=ServiceRepository.getInstance().getValue();
				                                						for(String service : services){
				                                							if(validServices.indexOf(","+service.trim().toLowerCase()+",")!=-1){
				                                								%>
																		<option value="<%=service.trim().toLowerCase() %>"><%=service%></option>
																		<%
				                                							}				                                						
				                                						}
				                                					%>
																	</select>
																</div>
																<%
	                                                            if(!pageName.endsWith("gillettepomedata")){
	                                                            	if((pageName.endsWith("data") || pageName.endsWith("summary")) && !pageName.equals("drcpdata") && !pageName.equals("didsummary") && !fileName.equals("view_userData")){
	    	                                                            %>
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-12">
																	<input type="text" name="startDate"
																		placeholder="Start Date" autocomplete="off"
																		id="startdatehourpicker"
																		class="form-control basic-ele-mg-b-10 responsive-mg-b-10" />
																</div>
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-12">
																	<input type="text" name="endDate"
																		placeholder="End Date" autocomplete="off"
																		id="enddatehourpicker"
																		class="form-control basic-ele-mg-b-10 responsive-mg-b-10" />
																</div>
																<%
    	                                                            }
	                                                            	//if(fileName.equalsIgnoreCase("reporting_missCallData")) {
	                                                            	
	                                                            	if (!pageName.equalsIgnoreCase("brandwisedata") && !pageName.equalsIgnoreCase("billingdata")
																			&& pageName.indexOf("summary") == -1 && !pageName.equalsIgnoreCase("didSiteMappingData")
																			&& !pageName.equalsIgnoreCase("encryptDecrypt") && !pageName.equals("tcid")
																			&& !pageName.equals("branch") && !pageName.equals("campaigns")
																			&& !pageName.equals("drcpfile") && !pageName.equals("drcpdata")
																			&& !pageName.equals("retailer") && !pageName.startsWith("edit")) {
	                                                            		
																		
																			/*  if (!pageName.equals("pampersbandhandata") && !pageName.equals("didconfigurations")
																				&& !pageName.equals("didhistory") && !pageName.equals("brandoffers")
																				&& !pageName.equals("offeruploadstatus") && !pageName.equals("brandoffersbulk")
																				&& !fileName.equals("edit_pharmacy") && !pageName.equals("servicemaster")
																				&& (fileName.equals("view_userProfile") || fileName.equals("view_testingMobileNumber")) && !fileName.equals("view_userData")
																				&& !fileName.equals("view_home")) {
																			
																			 */
																			 if(!pageName.equals("pampersbandhandata") && !pageName.equals("didconfigurations") && !pageName.equals("didhistory") && !pageName.equals("brandoffers") && !pageName.equals("offeruploadstatus") && !pageName.equals("brandoffersbulk")){
																			
																		
																			 		
    	                                                            	%>
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-12">
																	<input type="text" name="mobileNumber"
																		placeholder="Mobile Number" autocomplete="off"
																		id="mobileNumber"
																		class="form-control basic-ele-mg-b-10 responsive-mg-b-10" />
																</div>
																<%
																		
																				
																		}
																		//else{
    	                                                            			//System.out.println("idhrrr aa gya");
    	                                                            		//}
    	                                                            	if(pageName.indexOf("obd")!=-1){
                                                                			%>
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-12">
																	<input type="text" name="parameters"
																		placeholder="TCID/Site ID/Retailer Code/Branch Name"
																		autocomplete="off" id="parameters"
																		class="form-control basic-ele-mg-b-10 responsive-mg-b-10" />
																</div>
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-12">
																	<input type="number" min="0" name="OfflineId"
																		placeholder="Offline Id" autocomplete="off"
																		id="OfflineId"
																		class="form-control basic-ele-mg-b-10 responsive-mg-b-10" />
																</div>
																<%
                                                                		}
    	                                                            	if(pageName.indexOf("optin")!=-1){	                                                            		
    	                                                            		%>
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-12">
																	<input type="text" name="parameters"
																		placeholder="TCID/Site ID/Retailer Code/Branch Name"
																		autocomplete="off" id="parameters"
																		class="form-control basic-ele-mg-b-10 responsive-mg-b-10" />
																</div>
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-12">
																	<input type="text" name="couponNumber"
																		placeholder="Coupon Number/Store Code"
																		autocomplete="off" id="couponNumber"
																		class="form-control basic-ele-mg-b-10 responsive-mg-b-10" />
																</div>
																<%
    	                                                           	 	}
    	                                                            	if(pageName.equals("pampersbandhandata")){
    	                                                            		%>
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-12">
																	<input type="text" name="couponNumber"
																		placeholder="Store Code" autocomplete="off"
																		id="couponNumber"
																		class="form-control basic-ele-mg-b-10 responsive-mg-b-10" />
																</div>
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-12">
																	<input type="text" name="tcid" placeholder="Tcid"
																		autocomplete="off" id="tcid"
																		class="form-control basic-ele-mg-b-10 responsive-mg-b-10" />
																</div>
																<%
    	                                                            	}	                                                            		                                                            	
    	                                                            }
    	                                                            if(pageName.indexOf("tcid")!=-1 || !fileName.equals("reporting_pixelCampaigns") && !fileName.equals("view_pixelCampaigns") && !fileName.equals("view_userProfile") && !fileName.equals("reporting_teleCallingData") && !fileName.equals("reporting_popData") && !fileName.equals("view_sms")  && !fileName.equals("fetch_mobilenumber") && !fileName.equals("view_serviceconfiguration") && !fileName.equals("view_branch") && !fileName.equals("view_pharmacy") && !fileName.equals("view_retailer") && !fileName.equals("view_retaileregistration") && !fileName.equals("view_didconfiguration") && !fileName.equals("view_testingMobileNumber") && !fileName.equals("view_email")  && !fileName.startsWith("reporting")){	                                                            		
                                                                		%>
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-12">
																	<input type="text" name="siteName"
																		placeholder="Site Name" autocomplete="off"
																		id="siteName"
																		class="form-control basic-ele-mg-b-10 responsive-mg-b-10" />
																</div>
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-12">
																	<input type="text" name="tcid" placeholder="Tcid"
																		autocomplete="off" id="tcid" style="display: none"
																		class="form-control basic-ele-mg-b-10 responsive-mg-b-10" />
																</div>
																<%
                                                               	 	}
                                                                	if((pageName.indexOf("branch")!=-1 || pageName.indexOf("campaigns")!=-1) && !fileName.equals("reporting_pixelCampaigns") && !fileName.equals("view_pixelCampaigns") && fileName.equals("view_branch")){	                                                            		
                                                                		%>
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-12">
																	<input type="text" name="siteName"
																		placeholder="Site Name" autocomplete="off"
																		id="siteName"
																		class="form-control basic-ele-mg-b-10 responsive-mg-b-10" />
																</div>
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-12">
																	<input type="text" name="projectId"
																		placeholder="Project Id" autocomplete="off"
																		id="projectId"
																		class="form-control basic-ele-mg-b-10 responsive-mg-b-10" />
																</div>
																<%
                                                                		if(pageName.indexOf("campaigns")!=-1 && !fileName.equals("reporting_pixelCampaigns")){
                                                                			%>
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-12">
																	<select name="campaignName"
																		class="form-control basic-ele-mg-b-10 responsive-mg-b-10"
																		id="campaignName">
																		<option value="">Select Campaign Name</option>
																		<%
    					                                							List<String> campaignNamesList=CampaignNameRepository.getInstance().getValue();
    						                                						for(String campaigname : campaignNamesList){
    					                                								%>
																		<option value="<%=campaigname%>"><%=campaigname%></option>
																		<%        						
    						                                						}
    					                                							%>
																	</select>
																</div>
																<%
                                                                		}
                                                               	 	}
                                                                	if(pageName.equals("pharmacy")){
                                                                		%>
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-12">
																	<input type="text" name="couponNumber"
																		placeholder="Store Code" autocomplete="off"
																		id="couponNumber"
																		class="form-control basic-ele-mg-b-10 responsive-mg-b-10" />
																</div>
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-12">
																	<input type="text" name="tcid" placeholder="Tcid"
																		autocomplete="off" id="tcid"
																		class="form-control basic-ele-mg-b-10 responsive-mg-b-10" />
																</div>
																<%
                                                                	}
                                                                	if(pageName.equals("drcpfile") || pageName.equals("drcpdata")){
                                                                		%>
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-12">
																	<select name="OfferTypeId"
																		class="form-control basic-ele-mg-b-10 responsive-mg-b-10"
																		id="OfferTypeId">
																		<option value="">Select OfferType</option>
																		<%	                                						
    					                                						List<String> offertypes=OfferTypeRepository.getInstance().getValue();
    					                                						for(String offertype : offertypes){	                                							
    				                                								%>
																		<option value="<%=offertype.split("-")[0] %>"><%=offertype.split("-")[1]%></option>
																		<%	                                											                                						
    					                                						}
    					                                					%>
																	</select>
																</div>
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-12">
																	<select name="BranchName"
																		class="form-control basic-ele-mg-b-10 responsive-mg-b-10"
																		id="BranchName">
																		<option value="">Select Branch Name</option>
																	</select>
																</div>
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-12">
																	<input type="number" name="FileUploadId"
																		placeholder="FileUploadId" autocomplete="off"
																		id="FileUploadId" min="0"
																		class="form-control basic-ele-mg-b-10 responsive-mg-b-10" />
																</div>
																<%
                                                                	}
                                                                	if(pageName.equals("retailer") || pageName.equals("retaileregistration")){
                                                                		%>
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-12">
																	<input type="number" name="RetailerCode"
																		placeholder="Retailer Code" autocomplete="off"
																		id="RetailerCode" min="0"
																		class="form-control basic-ele-mg-b-10 responsive-mg-b-10" />
																</div>
																<%
                                                                	}
                                                                	if (pageName.equalsIgnoreCase("didhistory")) {
                                                                		%>
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-12">
																	<input type="text" name="callerId"
																		placeholder="Caller Id" autocomplete="off"
																		id="callerId"
																		class="form-control basic-ele-mg-b-10 responsive-mg-b-10" />
																</div>
																<%
                                                                	}

                                                                	if (fileName.equals("view_userProfile")) {
        																%>
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-12">
																	<input type="email" name="emaildropdown"
																		placeholder="Email" autocomplete="off"
																		id="emaildropdown"
																		class="form-control basic-ele-mg-b-10 responsive-mg-b-10" />
																</div>
																<%
        																	}
	                                                            }
	                                                            %>
																<div class="col-lg-2 col-md-2 col-sm-2 col-xs-12">
																	<div class="login-btn-inner">
																		<div class="row">
																			<div class="col-lg-6 col-md-5 col-sm-5 col-xs-12">
																				<input type="hidden" name="action" id="action"
																					value="querycondition" /> <input type="hidden"
																					name="EmpId"
																					value="<%=session.getAttribute("empId") %>" /> <input
																					type="hidden" name="pageName" id="pageName"
																					value="<%=pageName%>" /> <input type="hidden"
																					name="username" id="username"
																					value="<%=session.getAttribute("userName")%>"><input
																					type="hidden" name="readwriteValues"
																					id="readwriteValues"
																					value="<%=session.getAttribute("readwriteValues")%>"><input
																					type="hidden" name="pagelink" id="pagelink"
																					value="<%=session.getAttribute("pagelink")%>">
																				<input type="hidden" name="readValues"
																					id="readValues"
																					value="<%=session.getAttribute("readValues")%>"><input
																					type="hidden" name="fileName" id="fileName"
																					value="<%=fileName%>">
																				<div class="login-horizental lg-hz-mg">
																					<button
																						class="btn btn-sm btn-primary login-submit-cs"
																						type="submit">Submit</button>
																				</div>
																			</div>
																		</div>
																	</div>
																</div>
																<%
																	}
																%>
															</div>
														</div>
													</form>
												</div>
											</div>
										</div>
									</div>
								</div>
							</div>
						</div>
					</div>
				</div>
			</div>
		</div>
		
		
		
		
		<!-- Include Bootstrap CSS and JS -->
<<!-- script src="https://code.jquery.com/jquery-3.2.1.slim.min.js"></script>
<script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.12.9/umd/popper.min.js"></script>
<script src="https://maxcdn.bootstrapcdn.com/bootstrap/4.0.0/js/bootstrap.min.js"></script>
	 -->	
		<script src="https://code.jquery.com/jquery-3.7.0.js"
			integrity="sha256-JlqSTELeR4TLqP0OG9dxM7yDPqX1ox/HfgiSLBj8+kM="
			crossorigin="anonymous"></script>
		<script>
			$(document).ready(function() {
				function toggleTcidVisibility() {
					var service = $('#serviceName').val();
					if (service && service.includes('pampers')) {
						$("#tcid").show();
					} else {
						$("#tcid").hide();
					}
				}
				if ($('#serviceName').length) {

				toggleTcidVisibility();

				$('#serviceName').on('change', toggleTcidVisibility);
				 }
			});
		</script>

		<script src="https://code.jquery.com/jquery-3.6.0.min.js"></script>
		