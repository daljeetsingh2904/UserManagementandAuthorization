package com.techmobia.supportmanager.controller;

import java.awt.Dimension;
import com.techmobia.supportmanager.model.Email;
import com.techmobia.supportmanager.model.Login;
import com.techmobia.supportmanager.model.UserPermission;
import java.io.PrintWriter;

import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.awt.Dimension;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.techmobia.supportmanager.model.Distributor;
import com.techmobia.supportmanager.services.*;

/**
 * Servlet implementation class RequestController
 */
public class RequestController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(RequestController.class);
	boolean executeDid = true;
	private static final String encryptionKey = "techmobiaproject";

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public RequestController() {
		super();
	}

	/**
	 * @see Servlet#destroy()
	 */
	@Override
	public void destroy() {
		String connMysql = null;
		PropertyConfigurator.configure(Constants.LOG4JFILEPATH);
		try {
			connMysql = DbHandler.getInstance().checkClose();
			if (connMysql.equalsIgnoreCase("closed")) {
				logger.info("Db Connection Has Been Successfully Closed for Support Manager Application");
			} else {
				logger.info("Error in Closing Db Connection for Support Manager Application");
			}
		} catch (Exception e) {
			logger.error(e);
		}
	}

	public static int infoBox(String infoMessage, String titleBar) {
		// JOptionPane.showMessageDialog(null, infoMessage, "InfoBox: " + titleBar,
		// JOptionPane.YES_NO_OPTION);
		JPanel panel = new JPanel();
		panel.setSize(new Dimension(250, 100));
		panel.setLayout(null);
		JLabel label2 = new JLabel(infoMessage);
		label2.setVerticalAlignment(SwingConstants.TOP);
		label2.setHorizontalAlignment(SwingConstants.CENTER);
		label2.setBounds(20, 80, 200, 20);
		panel.add(label2);
		UIManager.put("OptionPane.minimumSize", new Dimension(250, 200));
		int res = JOptionPane.showConfirmDialog(null, panel, "Confirmation Box", JOptionPane.YES_NO_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		return res;
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = null;
		String startDate = null;
		String endDate = null;
		String serviceName = null;
		executeDid = true;
		try {
			action = request.getParameter("action");
			switch (action) {
			case "reload":
				PropertyHandler.reload();
				ServiceRepository.reLoadRepository();
				CampaignNameRepository.reLoadRepository();
				//PageNameRepository.reLoadRepository();
				response.getWriter().append(("Property File Reloaded at " + Utility.getCurrentDatetime()));
				logger.info("Property File Reloaded at " + Utility.getCurrentDatetime());
				break;
			case "dailyReport":
				startDate = null == request.getParameter("startDate") ? Utility.yesterdayDate(-1)
						: request.getParameter("startDate");
				endDate = null == request.getParameter("endDate") ? Utility.yesterdayDate(-1)
						: request.getParameter("endDate");
				serviceName = request.getParameter("serviceName");
				DbHandler.getInstance().dailyServiceReports(startDate, endDate, serviceName);
				break;
			case "dailyReportDtc":
				startDate = null == request.getParameter("startDate") ? Utility.yesterdayDate(-1)
						: request.getParameter("startDate");
				endDate = null == request.getParameter("endDate") ? Utility.yesterdayDate(-1)
						: request.getParameter("endDate");
				serviceName = request.getParameter("serviceName");
				DbHandler.getInstance().dailyServiceReportsDtc(startDate, endDate, serviceName);
				break;
			case "dailyReportPampersBandhan":
				startDate = null == request.getParameter("startDate") ? Utility.yesterdayDate(-1)
						: request.getParameter("startDate");
				endDate = null == request.getParameter("endDate") ? Utility.yesterdayDate(-1)
						: request.getParameter("endDate");
				serviceName = request.getParameter("serviceName");
				DbHandler.getInstance().dailyServiceReportsPampers(startDate, endDate, serviceName);
				break;
			case "dailyReport2Days":
				int todayDate = Integer
						.parseInt(Utility.getCurrentDatetime().replace("T", " ").split(" ")[0].split("-")[2]);
				int yesterdayDate = Integer.parseInt(Utility.yesterdayDate(-1).split(" ")[0].split("-")[0]);
				endDate = null == request.getParameter("endDate") ? Utility.yesterdayDate(-1)
						: request.getParameter("endDate");
				if (todayDate % 2 == 1) {
					if (todayDate == 1 && yesterdayDate % 2 == 1) {
						startDate = Utility.yesterdayDate(-1);
					} else {
						startDate = Utility.yesterdayDate(-2);
					}
					serviceName = request.getParameter("serviceName");
					DbHandler.getInstance().dailyServiceReports(startDate, endDate, serviceName);
				} else {
					logger.info("Reports Not Allowed for Today at " + Utility.getCurrentDatetime());
				}
				break;
			case "dtcTracker":
				startDate = null == request.getParameter("startDate") ? Utility.yesterdayDate(-7)
						: request.getParameter("startDate");
				endDate = null == request.getParameter("endDate") ? Utility.yesterdayDate(-1)
						: request.getParameter("endDate");
				Utility.dtcFileTransfer(startDate, endDate);
				break;
			case "trialTracker":
				startDate = null == request.getParameter("startDate") ? Utility.yesterdayDate(-1)
						: request.getParameter("startDate");
				endDate = null == request.getParameter("endDate") ? Utility.yesterdayDate(-1)
						: request.getParameter("endDate");
				String siteName = null == request.getParameter("siteName") ? "NA" : request.getParameter("siteName");
				serviceName = request.getParameter("serviceName");
				DbHandler.getInstance().dailyServiceReportsTrialTracker(startDate, endDate, siteName, serviceName);
				Utility.fileDownload(response, PropertyHandler.getInstance().getValue("report_filepath_trialtracker"),
						PropertyHandler.getInstance().getValue("report_filename_trialtracker")
								.replace("%YESTERDAY_DATE", Utility.yesterdayDate(-1))
								.replace("%SERVICENAME", serviceName.toLowerCase()));
				break;
			case "couponCodeAlert":
				serviceName = request.getParameter("serviceName");
				DbHandler.getInstance().dailyCouponAlertReports(serviceName);
				break;
			case "wsItrialReports":
				startDate = request.getParameter("startDate");
				endDate = null == request.getParameter("endDate") ? Utility.yesterdayDate(-1)
						: request.getParameter("endDate");
				if (null == startDate) {
					if (Utility.getCurrentDatetime().replace("T", " ").split(" ")[0].split("-")[2].equals("01")) {
						startDate = Utility.lastMonth(-1) + "-01";
					} else {
						startDate = Utility.getFirstDateOfMonth().toString();
					}
				}
				DbHandler.getInstance().wsItrialReports(startDate, endDate, "wsitrial");
				break;
			case "popReportsMatrix":
				startDate = request.getParameter("startDate");
				endDate = null == request.getParameter("endDate") ? Utility.yesterdayDate(-1)
						: request.getParameter("endDate");
				if (null == startDate) {
					if (Utility.getCurrentDatetime().replace("T", " ").split(" ")[0].split("-")[2].equals("01")) {
						startDate = Utility.lastMonth(-1) + "-01";
					} else {
						startDate = Utility.getFirstDateOfMonth().toString();
					}
				}
				break;
			case "telecallingReportsMatrix":
				break;

			/**
			 * getTables is used for getting Tablename from databasename and property file
			 * key i.e:transaction_table_pool for deleting tables
			 * 
			 */
			case "getTables":
				PrintWriter out = response.getWriter();
				String databaseName = request.getParameter("database");
				String tablename = request.getParameter("selectedTables"); // for getting values of tablename from view
																			// page
				String[] tableArr = tablename.split(",");
				List<String> tableNames = DbHandler.getInstance().getTableNames(databaseName);

				StringBuilder res = new StringBuilder();
				int count = 0;
				// this loop is for checkbox which will come according to metadata and property
				// file key
				for (String tableValue : tableNames) {
					boolean isChecked = false;
					// This loop is for checked values which will come from view page tablename
					// selected values
					for (String selectedTable : tableArr) {
						if (tableValue.contains(selectedTable)) {
							isChecked = true;
							break;
						}
					}
					String checkedAttribute = isChecked ? "checked" : "";
					String checkboxLabel = "<input type='checkbox' id='checkbox" + count
							+ "' name='tables' class='checkboxclass' value='" + tableValue + "' " + checkedAttribute
							+ ">" + tableValue + "<br>";
					res.append(checkboxLabel);
					count++;
				}

				out.print(res.toString());
				break;

			case "checkEmail":
				out = response.getWriter();
				String email = request.getParameter("email");
				String exist = DbHandler.getInstance().checkEmail(email);
				String resetPassword = Utility.verifyData(request.getParameter("resetUserPassword"));
				if (resetPassword.equalsIgnoreCase("successemail") && exist == "exists") {
					out.print(exist);
					String userNewPassword = Utility.generateRandomPassword(8);
					Utility.sendUserMail(email, userNewPassword);
					DbHandler.getInstance().updatePassword(userNewPassword, email);

				} else {
					out.print(exist);
				}

				break;

			case "passwordWarningMessage":
				out = response.getWriter();
				email = request.getParameter("email");
				String password=request.getParameter("password");
				int message = DbHandler.getInstance().warningMessage(email,password);
				System.out.println("message is "+message);
				out.print(message);
				break;
			
			case "checkOtp":
				 out = response.getWriter();
				 String otp=request.getParameter("otp");
			     String emailId = request.getParameter("email");
			     int validateFlag=DbHandler.getInstance().otpValidateSubmit(emailId, Integer.parseInt(otp));
			     verifyOTPWithLogin(request,validateFlag);
			     out.print(validateFlag);
                 break;
                 
			case "resendOtp":
				 validateFlag=0;
			    out =response.getWriter();
				emailId = request.getParameter("email");
				int otpNumber = Utility.generateOtp(emailId);
				Utility.sendMail("NA", "NA", emailId, "NA", "NA", PropertyHandler.getInstance().getValue("fromOtpMail"),
						"0", "otp", PropertyHandler.getInstance().getValue("otpMailSubject"),
						PropertyHandler.getInstance().getValue("otpMailContent"), 0, "Otp", otpNumber);
				logger.info("validateflag=" + validateFlag);
				out.print(validateFlag);
				break;   
				
			case "smsCampaignDetails":
				out = response.getWriter();
				String campaignName=request.getParameter("campaignName");
				System.out.println("campaignName--->>> "+campaignName);
				long smsCount=DbHandler.getInstance().fetchSmsCampaignCount(campaignName);
	            out.print(smsCount);
				break;	
			case "emailCampaignDetails":
				out = response.getWriter();
			    campaignName=request.getParameter("campaignName");
				System.out.println("campaignName--->>> "+campaignName);
				long emailCount=DbHandler.getInstance().fetchEmailCampaignCount(campaignName);
	            out.print(emailCount);
				break;		
			default:
				doPost(request, response);
				break;
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String action = null;
		PropertyConfigurator.configure(Constants.LOG4JFILEPATH);
		HttpSession session = request.getSession();
		Enumeration<?> enumeration = request.getParameterNames();
		Map<String, Object> modelMap = new HashMap<>();
		String subaction = "";
		String uniqueId = "0";
		executeDid = true;
		int res = 0;
		int isValidDID = Constants.VALID;
		String alertPageName = "";
		int validateFlag = 0;
		String username;
		try {
			while (enumeration.hasMoreElements()) {
				String parameterName = (String) enumeration.nextElement();
				modelMap.put(parameterName, request.getParameter(parameterName));
				logger.info(parameterName + "-->" + request.getParameter(parameterName));
			}
			logger.info("Instance id is ---->>> " + session.getAttribute("instanceId"));
			action = request.getParameter("action");
			switch (action) {
			// case "changepassword":
			// String newpassword = Utility.verifyData(request.getParameter("password"));
			// DbHandler.getInstance().updatePassword(newpassword,
			// Integer.parseInt(session.getAttribute("empId").toString()));
			// response.sendRedirect("logout.jsp");
			// break;
			case "querycondition":
				String queryCondition = "1=1";
				String serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
				String pageName = Utility.verifyData(request.getParameter("pageName"));
				String startDate = Utility.verifyData(request.getParameter("startDate"));
				String endDate = Utility.verifyData(request.getParameter("endDate"));
				String mobileNumber = Utility.verifyData(request.getParameter("mobileNumber"));
				String couponNumber = Utility.verifyData(request.getParameter("couponNumber"));
				String siteName = Utility.verifyData(request.getParameter("siteName"));
				String parameters = Utility.verifyData(request.getParameter("parameters"));
				String reportingFlag = Utility.verifyData(request.getParameter("reportingFlag"));
				String tcid = Utility.verifyData(request.getParameter("tcid"));
				String projectId = Utility.verifyData(request.getParameter("projectId"));
				String offerTypeId = Utility.verifyData(request.getParameter("OfferTypeId"));
				String branchName = Utility.verifyData(request.getParameter("BranchName"));
				String fileuploadId = Utility.verifyData(request.getParameter("FileUploadId"));
				String retailerCode = Utility.verifyData(request.getParameter("RetailerCode"));
				String offlineId = Utility.verifyData(request.getParameter("OfflineId"));
				String queryColumns = Utility
						.verifyData(PropertyHandler.getInstance().getValue(serviceName + "_" + pageName + "_columns"));
				String dateCondition = Utility.verifyData(
						PropertyHandler.getInstance().getValue(serviceName + "_" + pageName + "_datecondition"));
				String did = Utility.verifyData(request.getParameter("callerId"));
				String userEmailId = Utility.verifyData(request.getParameter("emaildropdown"));
				List<String> dbColumns = new ArrayList<>();
				if (!queryColumns.equalsIgnoreCase("NA")) {
					pageName = pageName.toUpperCase();
					for (int i = 0; i < queryColumns.split("@").length; i++) {
						dbColumns.add(queryColumns.split("@")[i]);
					}
					if (!startDate.equals("NA") && !endDate.equals("NA")) {
						if (serviceName.equals("pampersengagement")) {
							queryCondition += " and ed.EngagementDate between '" + startDate + " 00:00:00' and '"
									+ endDate + " 23:59:59'";
						} else {
							queryCondition += " and " + dateCondition.split(">")[0] + ">'" + startDate
									+ " 00:00:00' and " + dateCondition.split(">")[0] + "<'" + endDate + " 23:59:59'";
						}
					}
					if (!mobileNumber.equals("NA") && !serviceName.equalsIgnoreCase("all")) {
						if (!serviceName.equalsIgnoreCase("pamperspremiumcare")
								&& (serviceName.startsWith("pampers") || serviceName.startsWith("itrial"))) {
							queryCondition += " and tm.mobile_number='"
									+ Utility.encrypt(mobileNumber,
											EncryptionValues.valueOf(serviceName.toUpperCase() + Constants.SECRET)
													.getValue(),
											EncryptionValues.valueOf(serviceName.toUpperCase() + "SALT").getValue())
									+ "'";
						}
//else if (pageName.equalsIgnoreCase("testingMobileNumber")) {
//							int mobileNumberId = DbHandler.getInstance().fetchMobileNumberId(serviceName,
//									Utility.encrypt(mobileNumber,
//											EncryptionValues.valueOf(serviceName.toUpperCase() + Constants.SECRET)
//													.getValue(),
//											EncryptionValues.valueOf(serviceName.toUpperCase() + "SALT").getValue()));
//							queryCondition += " and tmm.mobilenumberid=" + mobileNumberId;
//						} 
						else {
							System.out.println("hii 2  ");
							queryCondition += " and tm.MobileNumber='"
									+ Utility.encrypt(mobileNumber,
											EncryptionValues.valueOf(serviceName.toUpperCase() + Constants.SECRET)
													.getValue(),
											EncryptionValues.valueOf(serviceName.toUpperCase() + "SALT").getValue())
									+ "'";
						}
					}

					// JIRA 930

					if (pageName.equalsIgnoreCase("userProfile")) {
						if (!mobileNumber.equals("NA") && !userEmailId.equals("NA")) {
							// Both mobile number and email are provided
							queryCondition += " and ud.mobile_number='" + mobileNumber + "' and ud.email='"
									+ userEmailId + "'";
						} else if (!mobileNumber.equals("NA")) {
							// Only mobile number is provided
							queryCondition += " and ud.mobile_number='" + mobileNumber + "'";
						} else if (!userEmailId.equals("NA")) {
							// Only email is provided
							queryCondition += " and ud.email='" + userEmailId + "'";
						}

					}

					if (!offlineId.equals("NA")) {
						if (serviceName.startsWith("pampers") || serviceName.startsWith("pampersnew")) {
							queryCondition += " and od.smservice_id='" + offlineId + "'";
						} else {
							queryCondition += " and od.OfflineId='" + offlineId + "'";
						}
					}
					if (!siteName.equals("NA")) {
						if (serviceName.equals("pampersbandhan") || serviceName.equals("pampersbandhannew")) {
							queryCondition += " and tsm.site_name='" + siteName + "'";
						} else if (serviceName.startsWith("itrial")) {
							queryCondition += " and sm.site_name='" + siteName + "'";
						} else if (serviceName.startsWith("pampers")) {
							queryCondition += " and sm.projectid_site_name='" + siteName + "'";
						} else if (serviceName.equals("pampersengagement")) {
							queryCondition += " and ed.SiteId=" + siteName;
						} else {
							queryCondition += " and sm.SiteName='" + siteName + "'";
						}
					}
					if (!projectId.equals("NA")) {
						if (serviceName.equals("itrial")) {
							queryCondition += " and bd.project_id=" + projectId;
						} else {
							queryCondition += " and psm.ProjectId=" + projectId;
						}
					}
					if (!tcid.equals("NA")) {
						if (serviceName.equals("pampers") || serviceName.equals("pampersnew")) {
							queryCondition += " and am.tcid='" + tcid + "'";
						} else if (serviceName.equals("pampersbandhan") || serviceName.equals("pampersbandhannew")) {
							queryCondition += " and od.tcid_dtmf='" + tcid + "'";
						}
					}
					if (!did.equals("NA")) {
						if (pageName.equalsIgnoreCase("didhistory")) {
							queryCondition += " and primary_did=" + did;
						}
					}
					if (!couponNumber.equals("NA")
							&& (serviceName.equals("pampersbandhan") || serviceName.equals("pampersbandhannew"))) {
						if (couponNumber.length() == 4) {
							queryCondition += " and sm.store_code='"
									+ Utility.encrypt(couponNumber,
											EncryptionValues.valueOf(serviceName.toUpperCase() + Constants.SECRET)
													.getValue(),
											EncryptionValues.valueOf(serviceName.toUpperCase() + "SALT").getValue())
									+ "'";
						} else {
							queryCondition += " and od.coupon_code='"
									+ Utility.encrypt(couponNumber,
											EncryptionValues.valueOf(serviceName.toUpperCase() + Constants.SECRET)
													.getValue(),
											EncryptionValues.valueOf(serviceName.toUpperCase() + "SALT").getValue())
									+ "'";
						}
					}
					if (serviceName.equals("itrial")) {
						if (!offerTypeId.equals("NA") && pageName.equalsIgnoreCase("drcpfile")) {
							queryCondition += " and OfferTypeId=" + offerTypeId;
						}
						if (!branchName.equals("NA")) {
							if (pageName.equalsIgnoreCase("drcpfile")) {
								queryCondition += " and BranchName=" + branchName.split("@")[0];
							} else {
								queryCondition += " and BranchName='" + DbHandler.getInstance().fetchBranchName(
										branchName.split("@")[0], session.getAttribute("instanceId").toString()) + "'";
							}
						}
						if (!fileuploadId.equals("NA")) {
							queryCondition += " and FileUploadId=" + fileuploadId;
						}
						if (!retailerCode.equals("NA")) {
							queryCondition += " and retailer_code='" + retailerCode + "'";
						}
					}
					if (!parameters.equals("NA")) {
						if (serviceName.startsWith("itrial")) {
							queryCondition += " and (sd.site_name='" + parameters.split("-")[0] + "' or sd.site_id='"
									+ parameters.split("-")[0] + "')";
						} else if (serviceName.startsWith("pampers")) {
							queryCondition += " and (sd.projectid_site_name='" + parameters.split("-")[0] + "')";
						} else {
							queryCondition += " and (sm.SiteName='" + parameters.split("-")[0] + "' or sm.SiteId='"
									+ parameters.split("-")[0] + "')";
						}
						if (serviceName.equals("itrial")) {
							queryCondition += " and (bd.branch_desc='" + parameters.split("-")[1]
									+ "' or bd.branch_code='" + parameters.split("-")[1] + "') and od.retailer_code='"
									+ parameters.split("-")[2] + "'";
						} else {
							if (serviceName.startsWith("pampers")) {
								queryCondition += " and od.tcid_dtmf='" + parameters.split("-")[1] + "'";
							}
						}
					}
					if (pageName.equalsIgnoreCase("brandwisedata")) {
						session.setAttribute(EncryptionValues.valueOf(pageName).getValue(),
								DbHandler.getInstance().fetchBrandData(queryCondition, serviceName,
										EncryptionValues.valueOf(pageName).getValue(), queryColumns, dateCondition));
						session.setAttribute(EncryptionValues.valueOf(pageName).getValue() + Constants.COLUMNS,
								DbHandler.getInstance().fetchBrand());
					} else if (pageName.equalsIgnoreCase("telecallingdata")) {
						TeleCallingDataProcessor processor = new TeleCallingDataProcessor(queryCondition, serviceName,
								pageName, queryColumns, dateCondition, startDate, endDate);
						Thread process = new Thread(processor);
						process.start();
						session.setAttribute(EncryptionValues.valueOf(pageName).getValue(),
								PropertyHandler.getInstance().getValue("teleCallingDataResponse"));
					} else if (pageName.equalsIgnoreCase("telecallingmatrixdata")) {
						TeleCallingDataMatrixProcessor processor = new TeleCallingDataMatrixProcessor(queryCondition,
								serviceName, pageName, queryColumns, dateCondition, startDate, endDate);
						Thread process = new Thread(processor);
						process.start();
					} else if (pageName.equalsIgnoreCase("popdata")) {
						if (reportingFlag.equals("1")) {
							startDate = Utility.lastMonth(-1) + "-01";
							endDate = Utility.getFirstDateOfMonth().toString();
							queryCondition += " and " + dateCondition.split(">")[0] + ">'" + startDate
									+ " 00:00:00' and " + dateCondition.split(">")[0] + "<'" + endDate + " 23:59:59'";
						}
						PopDataProcessor processor = new PopDataProcessor(queryCondition, serviceName, pageName,
								queryColumns, dateCondition, startDate, endDate);
						Thread process = new Thread(processor);
						process.start();
						session.setAttribute(EncryptionValues.valueOf(pageName).getValue(),
								PropertyHandler.getInstance().getValue("popDataResponse"));
					} else if (pageName.equalsIgnoreCase("popmatrixdata")) {
						if (reportingFlag.equals("1")) {
							startDate = Utility.lastMonth(-1) + "-01";
							endDate = Utility.getFirstDateOfMonth().toString();
							queryCondition += " and " + dateCondition.split(">")[0] + ">'" + startDate
									+ " 00:00:00' and " + dateCondition.split(">")[0] + "<'" + endDate + " 23:59:59'";
						}
						PopDataMatrixProcessor processor = new PopDataMatrixProcessor(queryCondition, serviceName,
								pageName, queryColumns, dateCondition, startDate, endDate);
						Thread process = new Thread(processor);
						process.start();
					} else {
						String filePath = PropertyHandler.getInstance().getValue("report_filepath_panel");
						String reportingFileName = PropertyHandler.getInstance().getValue("reportingFileName")
								.replace("%ID", UUID.randomUUID().toString());
						session.removeAttribute("reportingFilename");
						session.setAttribute("reportingFilename", reportingFileName);
						if (pageName.equalsIgnoreCase("pampersbandhandata")
								&& serviceName.startsWith("pampersbandhan")) {
							String empId = session.getAttribute("empId").toString();
							if (startDate.equals("NA")) {
								startDate = Utility.getCurrentDatetime().replace("T", " ").split(" ")[0];
								endDate = Utility.getCurrentDatetime().replace("T", " ").split(" ")[0];
							}
							if (!queryCondition.equals("1=1")) {
								DbHandler.getInstance().insertPampersDashboardRequest(queryCondition, serviceName,
										queryColumns, dateCondition, startDate, endDate, reportingFileName, empId,
										couponNumber, tcid);
								PampersDashboardProcessor dashboardprocessor = new PampersDashboardProcessor(
										queryCondition, serviceName, pageName, queryColumns, dateCondition, filePath,
										reportingFileName, startDate, endDate);
								Thread dashboardprocess = new Thread(dashboardprocessor);
								dashboardprocess.start();
							}
							session.setAttribute(EncryptionValues.valueOf(pageName).getValue(),
									DbHandler.getInstance().fetchBandhanReportingData(queryCondition, serviceName,
											EncryptionValues.valueOf(pageName).getValue(), queryColumns, dateCondition,
											filePath, reportingFileName));
							session.setAttribute(EncryptionValues.valueOf(pageName).getValue() + Constants.COLUMNS,
									dbColumns);
						} else {
							session.setAttribute(EncryptionValues.valueOf(pageName).getValue(),
									DbHandler.getInstance().fetchData(queryCondition, serviceName,
											EncryptionValues.valueOf(pageName).getValue(), queryColumns, dateCondition,
											filePath, reportingFileName,
											session.getAttribute("instanceId").toString()));
							session.setAttribute(EncryptionValues.valueOf(pageName).getValue() + Constants.COLUMNS,
									dbColumns);
						}
					}
					session.setAttribute(EncryptionValues.valueOf(pageName).getValue() + "SERVICE", serviceName);
				} else {
					logger.error("Columns Not Found from Property File for " + pageName);
				}
				break;
			case "autocomplete":
				response.setContentType("application/json");
				response.setCharacterEncoding("UTF-8");
				List<String> retailerList = null;
				List<String> filteredRetailerList;
				serviceName = request.getParameter(Constants.SERVICENAME);
				parameters = request.getParameter("parameters");
				String json = "";
				if (serviceName != null && serviceName.length() > 0) {
					retailerList = DbHandler.getInstance().fetchRetailerData(serviceName, parameters);
					filteredRetailerList = Utility.filterListByTerm(retailerList, parameters.toLowerCase());
					json = Utility.list2Json(filteredRetailerList);
				}
				response.getWriter().write(json);
				break;
			case "data":
				String responseValue = null;
				ArrayList<String> dataList = new ArrayList<>();
				String[] dataArr;
				serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
				String requestValue = Utility.verifyData(request.getParameter("requestValue"));
				String functionType = Utility.verifyData(request.getParameter("functionType"));
				dataArr = requestValue.split(",");
				if (functionType.equals("encrypt")) {
					for (int i = 0; i < dataArr.length; i++) {
						dataList.add(dataArr[i] + "-"
								+ Utility.encrypt(dataArr[i],
										EncryptionValues.valueOf(serviceName + Constants.SECRET).getValue(),
										EncryptionValues.valueOf(serviceName + "SALT").getValue()));
					}
				} else if (functionType.equals("decrypt")) {
					for (int i = 0; i < dataArr.length; i++) {
						if (dataArr[i].length() == 24) {
							responseValue = dataList.toString();
							dataList.add(dataArr[i] + "-"
									+ Utility.decrypt(dataArr[i],
											EncryptionValues.valueOf(serviceName + Constants.SECRET).getValue(),
											EncryptionValues.valueOf(serviceName + "SALT").getValue()));
						} else {
							dataList.add(dataArr[i] + "-" + "Incorrect Data Requested For Decryption");
						}
					}
				}
				responseValue = dataList.toString().replaceAll("\\[", "").replaceAll("\\]", "").replace(" ,", "\n")
						.replace(", ", "\n");
				session.setAttribute("requestValue", requestValue);
				session.setAttribute("responseValue", responseValue);
				break;
			case "fetchMobile":
				responseValue = null;
				dataList = new ArrayList<>();
				String[] mobileArr;
				mobileNumber = "NA";
				serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
				requestValue = Utility.verifyData(request.getParameter("requestValue"));
				functionType = Utility.verifyData(request.getParameter("functionType"));
				mobileArr = requestValue.split(",");
				for (int i = 0; i < mobileArr.length; i++) {
					if (mobileArr[i].length() < 10 || !isNumeric(mobileArr[i])) {
						mobileNumber = DbHandler.getInstance().fetchMobileNumber(serviceName, mobileArr[i]);
					} else {
						mobileNumber = DbHandler.getInstance().fetchStringMobileNumberUniqueId(serviceName,
								Utility.encrypt(mobileArr[i],
										EncryptionValues.valueOf(serviceName.toUpperCase() + Constants.SECRET)
												.getValue(),
										EncryptionValues.valueOf(serviceName.toUpperCase() + "SALT").getValue()));
					}
					if (mobileNumber != null && mobileNumber.length() > 20) {
						mobileNumber = Utility.decrypt(mobileNumber,
								EncryptionValues.valueOf(serviceName.toUpperCase() + Constants.SECRET).getValue(),
								EncryptionValues.valueOf(serviceName.toUpperCase() + "SALT").getValue());
					}
					dataList.add(mobileArr[i] + "-" + mobileNumber);
				}
				responseValue = dataList.toString().replaceAll("\\[", "").replaceAll("\\]", "").replace(" ,", "\n")
						.replace(", ", "\n");
				session.setAttribute("requestValue", requestValue);
				session.setAttribute("responseValue", responseValue);
				break;
			case Constants.DOWNLOADPAMPERSREPORT:
				String reportingFileName = request.getParameter("reportingFileName");
				if (null == reportingFileName) {
					Utility.fileDownload(response, PropertyHandler.getInstance().getValue("report_filepath_panel"),
							session.getAttribute("reportingFilename").toString());
				} else {
					Utility.fileDownload(response, PropertyHandler.getInstance().getValue("report_filepath_panel"),
							reportingFileName);
				}
//					String filepath=PropertyHandler.getInstance().getValue("report_filepath_panel");
//					String filename=session.getAttribute("reportingFilename").toString();
//					String receiverToEmails=PropertyHandler.getInstance().getValue("toemails_telecalling");
//					String receiverCcEmails=PropertyHandler.getInstance().getValue("ccemails_telecalling");
//					String receiverBccEmails=PropertyHandler.getInstance().getValue("bccemails_telecalling");			
//					String from=PropertyHandler.getInstance().getValue("fromemail_panel");
//					String mailSubject=PropertyHandler.getInstance().getValue("mail_subject_panel");
//					Utility.sendMail(filepath,filename,receiverToEmails,receiverCcEmails,receiverBccEmails,from,Utility.yesterdayDate(-1),"panel",mailSubject,"NA",1,"NA");
				break;
			case "enable":
				String userName = session.getAttribute("userName").toString();
				subaction = request.getParameter("subaction");
				uniqueId = request.getParameter("uniqueId");
				serviceName = request.getParameter("serviceName");
				if (serviceName == null) {
					serviceName = (String) session
							.getAttribute(EncryptionValues.valueOf(subaction.toUpperCase()).getValue() + "SERVICE");
				}
				DbHandler.getInstance().enableServices(subaction, serviceName, Integer.parseInt(uniqueId), userName);
				session.removeAttribute(EncryptionValues.valueOf(subaction.toUpperCase()).getValue());
				session.removeAttribute(
						EncryptionValues.valueOf(subaction.toUpperCase()).getValue() + Constants.COLUMNS);
				break;
			case "disable":
				userName = session.getAttribute("userName").toString();
				subaction = request.getParameter("subaction");
				uniqueId = request.getParameter("uniqueId");
				serviceName = request.getParameter("serviceName");

				if (serviceName == null) {
					serviceName = (String) session
							.getAttribute(EncryptionValues.valueOf(subaction.toUpperCase()).getValue() + "SERVICE");
				}
				DbHandler.getInstance().disableServices(subaction, serviceName, Integer.parseInt(uniqueId), userName);

				session.removeAttribute(EncryptionValues.valueOf(subaction.toUpperCase()).getValue());
				session.removeAttribute(
						EncryptionValues.valueOf(subaction.toUpperCase()).getValue() + Constants.COLUMNS);
				break;
			case "edit":
				if (null == request.getParameter("subaction")) {
					mobileNumber = request.getParameter("mobileNumber");
					serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
					if (!mobileNumber.equals("0")) {
						mobileNumber = DbHandler.getInstance().fetchMobileNumber(serviceName, mobileNumber);
					}
					System.out.println("mobile number for view is " + mobileNumber);
					if ((request.getParameter("offerDescription") != null)) {
						session.setAttribute("offerDescription",
								URLEncoder.encode(request.getParameter("offerDescription"), "UTF-8"));
					} else {
						session.setAttribute("offerDescription", "NA");
					}
					session.setAttribute("tcid", request.getParameter("tcid"));
					session.setAttribute("siteId", request.getParameter("siteId"));
					session.setAttribute("uniqueId", request.getParameter("uniqueId"));
					session.setAttribute("mobileNumberId", "0".equalsIgnoreCase(mobileNumber) ? 0
							: Utility.decrypt(mobileNumber,
									EncryptionValues.valueOf(serviceName.toUpperCase() + Constants.SECRET).getValue(),
									EncryptionValues.valueOf(serviceName.toUpperCase() + "SALT").getValue()));
					session.setAttribute("serviceName", serviceName);
					session.setAttribute("projectId", request.getParameter("projectId"));
					session.setAttribute("trialsAllowed", request.getParameter("trialsAllowed"));
				} else {
					serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
					mobileNumber = Utility.encrypt(request.getParameter("mobileNumber"),
							EncryptionValues.valueOf(serviceName.toUpperCase() + Constants.SECRET).getValue(),
							EncryptionValues.valueOf(serviceName.toUpperCase() + "SALT").getValue());
					uniqueId = request.getParameter("uniqueId");
					String siteId = request.getParameter("siteId");
					tcid = request.getParameter("tcid");
					String offerDescription = URLDecoder.decode(request.getParameter("offerDescription"), "UTF-8");
					projectId = Utility.verifyData(request.getParameter("projectId"));
					String trialsAllowed = request.getParameter("trialsAllowed");
					if (serviceName.equals("pampersbandhan") || serviceName.equals("pampersbandhannew")) {
						DbHandler.getInstance().editPharmacy(serviceName, mobileNumber, Integer.parseInt(uniqueId),
								tcid, siteId);
					} else {
						DbHandler.getInstance().editTcid(serviceName, mobileNumber, Integer.parseInt(uniqueId),
								projectId, offerDescription, siteId, trialsAllowed);
					}
					session.removeAttribute("tcid");
					session.removeAttribute("uniqueId");
					session.removeAttribute("mobileNumberId");
					session.removeAttribute("serviceName");
					session.removeAttribute("projectId");
					session.removeAttribute("siteId");
				}
				break;
			case "editClips":
				if (null == request.getParameter("subaction")) {
					serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
					session.setAttribute("uniqueId", request.getParameter("uniqueId"));
					session.setAttribute("offerType", request.getParameter("offerType"));
					session.setAttribute("siteName", request.getParameter("siteName"));
					session.setAttribute("serviceName", serviceName);
				}
				break;
			case "editRetailer":
				serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
				mobileNumber = request.getParameter("swingCode");
				uniqueId = request.getParameter("uniqueId");
				if (null == request.getParameter("subaction")) {
					session.setAttribute("uniqueId", uniqueId);
					session.setAttribute("mobileNumberId", mobileNumber);
					session.setAttribute("serviceName", serviceName);
					session.setAttribute("retailerCode", request.getParameter("retailerCode"));
				} else {
					DbHandler.getInstance().editRetailer(serviceName, mobileNumber, Integer.parseInt(uniqueId));
					session.removeAttribute("uniqueId");
					session.removeAttribute("mobileNumberId");
					session.removeAttribute("serviceName");
					session.removeAttribute("retailerCode");
				}
				break;
			case "editDistributor":
				Distributor distributor = new Distributor();
				serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
				distributor.setDistributorId(Integer.parseInt(request.getParameter("distributorId")));
				distributor.setIvrBranchCode(Integer.parseInt(request.getParameter("ivrBranchCode")));
				distributor.setIvrDistributorCode(Integer.parseInt(request.getParameter("ivrDistributorCode")));
				if (null == request.getParameter("subaction")) {
					session.setAttribute("distributorId", distributor.getDistributorId());
					session.setAttribute("ivrBranchCode", distributor.getIvrBranchCode());
					session.setAttribute("ivrDistributorCode", distributor.getIvrDistributorCode());
					session.setAttribute("serviceName", serviceName);
				} else {
					DbHandler.getInstance().editDistributor(serviceName, distributor);
					session.removeAttribute("distributorId");
					session.removeAttribute("ivrBranchCode");
					session.removeAttribute("ivrDistributorCode");
				}
				break;
			case "editSms":
				if (null == request.getParameter("subaction")) {
					serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
					session.setAttribute("uniqueId", request.getParameter("uniqueId"));
					session.setAttribute("smsCategory", request.getParameter("smsCategory"));
					session.setAttribute("serviceName", serviceName);
				} else {
					serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
					uniqueId = request.getParameter("uniqueId");
					String sms = request.getParameter("sms");
					DbHandler.getInstance().editSms(serviceName, sms, Integer.parseInt(uniqueId));
					session.removeAttribute("uniqueId");
					session.removeAttribute("serviceName");
				}
				break;
			case "editLanguage":
				if (null == request.getParameter("subaction")) {
					serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
					session.setAttribute("uniqueId", request.getParameter("uniqueId"));
					session.setAttribute("mobileNumberId", request.getParameter("mobileNumberId"));
					session.setAttribute("languageId", request.getParameter("languageId"));
					session.setAttribute("ivrLanguageId", request.getParameter("ivrLanguageId"));
					session.setAttribute("serviceName", serviceName);
				} else {
					serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
					uniqueId = request.getParameter("uniqueId");
					String ivrLanguageId = request.getParameter("ivrLanguageId");
					DbHandler.getInstance().editLanguage(serviceName, ivrLanguageId, Integer.parseInt(uniqueId));
					session.removeAttribute("uniqueId");
					session.removeAttribute("serviceName");
				}
				break;
			case "editEmail":
				if (null == request.getParameter("subaction")) {
					serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
					session.setAttribute("uniqueId", request.getParameter("uniqueId"));
					session.setAttribute("reportName", request.getParameter("reportName"));
					session.setAttribute("toEmailAddress", request.getParameter("toEmailAddress"));
					session.setAttribute("ccEmailAddress", request.getParameter("ccEmailAddress"));
					session.setAttribute("bccEmailAddress", request.getParameter("bccEmailAddress"));
					session.setAttribute("serviceName", serviceName);
				} else {
					serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
					uniqueId = request.getParameter("uniqueId");
					String toEmailAddress = request.getParameter("toEmailAddress");
					String ccEmailAddress = request.getParameter("ccEmailAddress");
					String bccEmailAddress = request.getParameter("bccEmailAddress");
					DbHandler.getInstance().editEmail(serviceName, toEmailAddress, ccEmailAddress, bccEmailAddress,
							Integer.parseInt(uniqueId));
					session.removeAttribute("uniqueId");
					session.removeAttribute("serviceName");
				}
				break;

			case "editservicemaster":
				if (null == request.getParameter("subaction")) {
					serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
					serviceName = request.getParameter("serviceName");
					session.setAttribute("serviceName", request.getParameter("serviceName"));
					session.setAttribute("uniqueId", request.getParameter("uniqueId"));
					String databaseName = request.getParameter("databasename");
					if (databaseName.equalsIgnoreCase("NA") || databaseName.equalsIgnoreCase("null")) {
						databaseName = "";
					}
					session.setAttribute("databaseNameOld", databaseName);
					session.setAttribute("databasename", databaseName);
					session.setAttribute("tablename", request.getParameter("tablename"));
					session.setAttribute("schedulerstatus", request.getParameter("schedulerstatus"));
					session.setAttribute("schedulermonths", request.getParameter("schedulermonths"));
				} else {
					String selectedTables[] = request.getParameterValues("tables");
					serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
					uniqueId = request.getParameter("uniqueId");
					String databaseNameOld = request.getParameter("databaseNameOld");
					String databaseName = request.getParameter("databasename");
					String tables = "";
					String selectedTableName = null;
					if (selectedTables != null && selectedTables.length > 0) {
						for (String tableName : selectedTables) {
							tables += tableName + ",";
							int lastIndex = tables.lastIndexOf(",");
							selectedTableName = tables.substring(0, lastIndex) + tables.substring(lastIndex + 1);
						}
					} else {
						selectedTableName = "NA";
					}
					String schedulerStatus = request.getParameter("schedulerstatus");
					String schedulerMonths = request.getParameter("schedulermonths");
					username = Utility.verifyData(request.getParameter("EmpName"));
					DbHandler.getInstance().editServiceMaster(serviceName, databaseName, databaseNameOld,
							selectedTableName, schedulerStatus, schedulerMonths, Integer.parseInt(uniqueId), username);
					session.removeAttribute("uniqueId");
					session.removeAttribute("serviceName");
				}
				break;

			case "editConfiguration":
				if (null == request.getParameter("subaction")) {
					serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
					session.setAttribute("uniqueId", request.getParameter("uniqueId"));
					session.setAttribute("flowId", request.getParameter("flowId"));
					session.setAttribute("couponCodeId", request.getParameter("couponCodeId"));
					session.setAttribute("couponCodeSiteId", request.getParameter("couponCodeSiteId"));
					session.setAttribute("trialsAllowed", request.getParameter("trialsAllowed"));
					session.setAttribute("capDays", request.getParameter("capDays"));
					session.setAttribute("didCappingStatus", request.getParameter("didCappingStatus"));
					session.setAttribute("configurationName", request.getParameter("configurationName"));
					session.setAttribute("serviceName", serviceName);
				} else {
					serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
					uniqueId = request.getParameter("uniqueId");
					int flowId = Integer.parseInt(request.getParameter("flowId"));
					int couponCodeId = Integer.parseInt(request.getParameter("couponCodeId"));
					int couponCodeSiteId = Integer.parseInt(request.getParameter("couponCodeSiteId"));
					int trialsAllowed = Integer.parseInt(request.getParameter("trialsAllowed"));
					int capDays = Integer.parseInt(request.getParameter("capDays"));
					int didCappingStatus = Integer.parseInt(request.getParameter("didCappingStatus"));
					DbHandler.getInstance().editConfiguration(serviceName, flowId, couponCodeId, couponCodeSiteId,
							trialsAllowed, capDays, didCappingStatus, Integer.parseInt(uniqueId));
					session.removeAttribute("uniqueId");
					session.removeAttribute("serviceName");
					session.removeAttribute("flowId");
					session.removeAttribute("couponCodeId");
					session.removeAttribute("couponCodeSiteId");
					session.removeAttribute("trialsAllowed");
					session.removeAttribute("capDays");
					session.removeAttribute("didCappingStatus");
				}
				break;
			case "addTcid":
				serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
				tcid = Utility.verifyData(request.getParameter("Tcid"));
				projectId = Utility.verifyData(request.getParameter("ProjectId"));
				siteName = Utility.verifyData(request.getParameter("SiteName"));
				String name = Utility.verifyData(request.getParameter("Name"));
				mobileNumber = Utility.verifyData(request.getParameter("MobileNumber"));
				mobileNumber = Utility.encrypt(mobileNumber,
						EncryptionValues.valueOf(serviceName.toUpperCase() + Constants.SECRET).getValue(),
						EncryptionValues.valueOf(serviceName.toUpperCase() + "SALT").getValue());
				DbHandler.getInstance().addTcidDetails(serviceName, tcid, projectId, siteName, name, mobileNumber);
				break;
			case "addIpAddress":
				String ipaddress = Utility.verifyData(request.getParameter("ipAddress"));
				String comments = Utility.verifyData(request.getParameter("comments"));
				String empId = Utility.verifyData(request.getParameter("EmpId"));
				DbHandler.getInstance().addIpAddressDetails(ipaddress, comments, empId);
				break;
			case "addTestingMobileNumber":
				serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
				mobileNumber = Utility.verifyData(request.getParameter("mobileNumber"));
				int mobileNumberId = DbHandler.getInstance().fetchMobileNumberUniqueId(serviceName,
						Utility.encrypt(mobileNumber,
								EncryptionValues.valueOf(serviceName.toUpperCase() + Constants.SECRET).getValue(),
								EncryptionValues.valueOf(serviceName.toUpperCase() + "SALT").getValue()));
				comments = Utility.verifyData(request.getParameter("comments"));
				empId = Utility.verifyData(request.getParameter("EmpId"));
				DbHandler.getInstance().addTestingMobileNumberDetails(mobileNumberId, comments, empId, serviceName);
				break;
			case "addonecprequest":
				serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
				startDate = Utility.verifyData(request.getParameter("startDate"));
				endDate = Utility.verifyData(request.getParameter("endDate"));
				String fileType = Utility.verifyData(request.getParameter("fileType"));
				DbHandler.getInstance().addOneCpDetails(serviceName, startDate, endDate, fileType);
				break;
			case "addDidConfigurations":
				serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
				String callerId = Utility.verifyData(request.getParameter("callerId"));
				projectId = DbHandler.getInstance().getProjectId(serviceName) + "";
				siteName = Utility.verifyData(request.getParameter("siteName"));
				String siteId = DbHandler.getInstance().getSiteId(serviceName, siteName) + "";
				String flowType = Utility.verifyData(request.getParameter("flowType"));
				String offerName = Utility.verifyData(request.getParameter("offerName"));
				String remarksValue = Utility.verifyData(request.getParameter("remarksValue"));
				String tcMobileNumber = Utility.verifyData(request.getParameter("tcMobileNumber"));
				String operatorName = Utility.verifyData(request.getParameter("operatorName"));
				String actionStatus = "Added";
				String editFlag = "new";
				username = Utility.verifyData(request.getParameter("EmpName"));
				isValidDID = DbHandler.getInstance().addDidConfiguration(serviceName, callerId, projectId, siteId,
						siteName, flowType, offerName, tcMobileNumber, remarksValue, actionStatus, editFlag, username,
						operatorName);
				alertPageName = "add_didconfigurations";
				break;
			case "editDidConfigurations":
				if (null == request.getParameter("subaction")) {
					serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
					session.setAttribute("uniqueId", request.getParameter("uniqueId"));
					session.setAttribute("callerId", request.getParameter("callerId"));
					session.setAttribute("projectId", request.getParameter("projectId"));
					session.setAttribute("siteName", request.getParameter("siteName"));
					session.setAttribute("flowType", request.getParameter("flowType"));
					session.setAttribute("offerName", request.getParameter("offerName"));
					session.setAttribute("remarksValue", request.getParameter("remarksValue"));
					session.setAttribute("serviceName", serviceName);
				} else {
					serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
					callerId = Utility.verifyData(request.getParameter("callerId"));
					projectId = Utility.verifyData(request.getParameter("projectId"));
					siteName = Utility.verifyData(request.getParameter("siteName"));
					siteId = DbHandler.getInstance().getSiteId(serviceName, siteName) + "";
					flowType = Utility.verifyData(request.getParameter("flowType"));
					offerName = Utility.verifyData(request.getParameter("offerName"));
					remarksValue = Utility.verifyData(request.getParameter("remarksValue"));
					actionStatus = "Updated";
					editFlag = "new";
					username = Utility.verifyData(request.getParameter("EmpName"));
					isValidDID = DbHandler.getInstance().updateDidConfiguration(serviceName, callerId, projectId,
							siteId, siteName, flowType, offerName, remarksValue, actionStatus, editFlag, username);
					session.removeAttribute("uniqueId");
					session.removeAttribute("serviceName");
					alertPageName = "view_didconfigurations";
				}
				break;
			case "revokeDidConfiguration":
				serviceName = Utility.verifyData(request.getParameter(Constants.SERVICENAME));
				callerId = Utility.verifyData(request.getParameter("primaryDid"));
				projectId = Utility.verifyData(request.getParameter("projectId"));
				actionStatus = "Revoked";
				editFlag = "new";
				username = Utility.verifyData(request.getParameter("EmpName"));
				serviceName = Utility.verifyData(request.getParameter("service"));
				isValidDID = DbHandler.getInstance().revokeDidConfiguration(serviceName, callerId, actionStatus,
						projectId, editFlag, username);
				alertPageName = "view_didhistory";
				break;
			case "register":
				subaction = request.getParameter("subaction");
				uniqueId = request.getParameter("uniqueId");
				serviceName = (String) session
						.getAttribute(EncryptionValues.valueOf(subaction.toUpperCase()).getValue() + "SERVICE");
				DbHandler.getInstance().registerServices(subaction, serviceName, Integer.parseInt(uniqueId));
				session.removeAttribute(EncryptionValues.valueOf(subaction.toUpperCase()).getValue());
				session.removeAttribute(
						EncryptionValues.valueOf(subaction.toUpperCase()).getValue() + Constants.COLUMNS);
				break;
			case "validateOtp":
				String otp = request.getParameter("otp");
				String emailId = request.getParameter("emailId");
				validateFlag = DbHandler.getInstance().validateOtp(emailId, Integer.parseInt(otp));
				if (validateFlag==1) {
					DbHandler.getInstance().updateOtp(validateFlag, emailId, Integer.parseInt(otp));
				}
				logger.info("validateCheck=" + validateFlag);
				break;
			 
//			case "resendOtp":
//			//	PrintWriter out =response.getWriter();
//				emailId = request.getParameter("email");
//				int otpNumber = Utility.generateOtp(emailId);
//				Utility.sendMail("NA", "NA", emailId, "NA", "NA", PropertyHandler.getInstance().getValue("fromOtpMail"),
//						"0", "otp", PropertyHandler.getInstance().getValue("otpMailSubject"),
//						PropertyHandler.getInstance().getValue("otpMailContent"), 0, "Otp", otpNumber);
//				logger.info("validateflag=" + validateFlag);
//				//out.print(validateFlag);
//				break;

			// =================JIRA - 930
				
				
			case "addUserProfile":
				userName = Utility.verifyData(request.getParameter("empname"));
				mobileNumber = Utility.verifyData(request.getParameter("userMobileNumber"));
				String email = Utility.verifyData(request.getParameter("emailId"));
				String[] readModules = request.getParameterValues("checkboxRead");
				String[] readWriteModules = request.getParameterValues("checkboxReadWrite");
				DbHandler.getInstance().addUserProfileDetails(userName, mobileNumber, email, readModules,
						readWriteModules);

				break;

			case "editUserProfile":
				if (null == request.getParameter("subaction")) {
					session.setAttribute("uniqueId", request.getParameter("uniqueId"));
					session.setAttribute("empname", request.getParameter("empname"));
					session.setAttribute("userMobileNumber", request.getParameter("userMobileNumber"));
					session.setAttribute("emailId", request.getParameter("emailId"));
					session.setAttribute("checkboxRead", request.getParameter("checkboxRead"));
					session.setAttribute("checkboxReadWrite", request.getParameter("checkboxReadWrite"));
				} else {
					// serviceName =
					// Utility.verifyData(request.getParameter(Constants.SERVICENAME));
					uniqueId = request.getParameter("uniqueId");
					userName = request.getParameter("empname");
					mobileNumber = request.getParameter("userMobileNumber");
					String[] checkboxRead = request.getParameterValues("checkboxRead");
					String[] checkboxReadWrite = request.getParameterValues("checkboxReadWrite");
					DbHandler.getInstance().editUser(userName, mobileNumber, checkboxRead, checkboxReadWrite, uniqueId);
					session.removeAttribute("uniqueId");
//					session.removeAttribute("checkboxReadWrite");
//					session.removeAttribute("checkboxRead");
//					session.removeAttribute("uniqueId");
//					session.removeAttribute("empname");
//					session.removeAttribute("userMobileNumber");
//					session.removeAttribute("emailId");
				}
				break;

			case "enableUserProfile":
				uniqueId = request.getParameter("uniqueId");
				DbHandler.getInstance().enableUserProfile(Integer.parseInt(uniqueId));
				session.removeAttribute("uniqueId");
				break;

			case "disableUserProfile":
				uniqueId = request.getParameter("uniqueId");
				DbHandler.getInstance().disableUserProfile(Integer.parseInt(uniqueId));
				session.removeAttribute("uniqueId");
				break;

			case "viewUserData":
				session.setAttribute("checkboxReadWrite", request.getParameter("checkboxReadWrite"));
				session.setAttribute("checkboxRead", request.getParameter("checkboxRead"));
				response.sendRedirect("view_userData.jsp");
				break;

			case "resetPassword":
				email = request.getParameter("emailId");
				uniqueId = request.getParameter("uniqueId");
				String userNewPassword = Utility.generateRandomPassword(8);
				Utility.sendUserMail(email, userNewPassword);
				DbHandler.getInstance().updatePassword(userNewPassword, email);
				break;
				
				//=======943
			case "addPixelCampaigns":
				String campaignName=request.getParameter("campaignName");
				String smsStatus=request.getParameter("smsStatus");
				String emailStatus=request.getParameter("emailStatus");
				DbHandler.getInstance().addPixelCampaign(campaignName,smsStatus,emailStatus);
				break;
				
			case "editPixelCampaigns":
				if (null == request.getParameter("subaction")) {
					session.setAttribute("campaignName", request.getParameter("campaignName"));
					session.setAttribute("status", request.getParameter("status"));
					session.setAttribute("smsStatus", request.getParameter("smsStatus"));
					session.setAttribute("emailStatus", request.getParameter("emailStatus"));
					session.setAttribute("uniqueId", request.getParameter("uniqueId"));
				} else {
					 campaignName = Utility.verifyData(request.getParameter("campaignName"));
					uniqueId = request.getParameter("uniqueId");
					String status = request.getParameter("status");
				    smsStatus = request.getParameter("smsStatus");
				    emailStatus = request.getParameter("emailStatus");
					DbHandler.getInstance().editCampaign(campaignName,uniqueId,status,smsStatus,emailStatus);
					session.removeAttribute("uniqueId");
					session.removeAttribute("campaignName");
				}
				break;
			default:
				break;
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				if (action != null && !action.equals("autocomplete")) {
					if (null == request.getParameter("type")) {
						if ("edit".equals(action) || "editSms".equals(action) || "editRetailer".equals(action)
								|| "editLanguage".equals(action) || "editEmail".equals(action)
								|| "editDistributor".equals(action) || "editClips".equals(action)
								|| "editConfiguration".equals(action) || "editDidConfigurations".equals(action)
								|| "editservicemaster".equals(action) || "editUserProfile".equals(action) || "editPixelCampaigns".equals(action)) {
							if ("edit".equals(request.getParameter("subaction"))) {
								if (isValidDID == Constants.VALID) {
									response.sendRedirect(request.getHeader("Referer").replace("edit", "view"));
								} else {
									Utility.showDIDAlert(response, isValidDID, alertPageName);
								}
							} else {
								response.sendRedirect(request.getHeader("Referer").replace("view", "edit"));
							}
//						}else if ("validateOtp".equals(action)) {
//								if (validateFlag == 0) {
//									response.sendRedirect(PropertyHandler.getInstance().getValue("failed_login_page"));
//								} else {
//									response.sendRedirect(PropertyHandler.getInstance().getValue("success_login_page"));
//								}
//							
					
							
						} else {
							if (!Constants.DOWNLOADPAMPERSREPORT.equals(action)) {
								if (isValidDID == Constants.VALID) {
									response.sendRedirect(request.getHeader("Referer"));
								} else {
									Utility.showDIDAlert(response, isValidDID, alertPageName);
								}
								// response.sendRedirect(request.getHeader("Referer"));
							}
						}
					} else {
						response.sendRedirect(request.getHeader("Referer").replace("Summary", "Data"));
					}
				}
			} catch (Exception exception) {
				logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
						.collect(Collectors.joining("\n")));
			}
		}
	}

	public static boolean isNumeric(String strNum) {
		if (strNum == null) {
			return false;
		}
		try {
			double d = Double.parseDouble(strNum);
		} catch (NumberFormatException nfe) {
			return false;
		}
		return true;
	}
	
	
	public void verifyOTPWithLogin(HttpServletRequest request,int validateFlag) {
		Login login = (Login) request.getSession().getAttribute("login");
		if(validateFlag==3) {
			request.getSession().setAttribute("empId", login.getEmpId());
			request.getSession().setAttribute("userName", login.getUserName());
			request.getSession().setAttribute("userPassword", login.getUserPassword());
			request.getSession().setAttribute("userMobileNumber", login.getMobileNumber());
			request.getSession().setAttribute("userType", login.getUserType());
			request.getSession().setAttribute("userEmail", login.getUserEmail());
			request.getSession().setAttribute("readwriteValues", login.getReadWriteModules());
			request.getSession().setAttribute("readValues", login.getReadModules());
			request.getSession().setAttribute("instanceId", login.getInstanceId());
		}
	}
}
