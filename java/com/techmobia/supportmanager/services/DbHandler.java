/**
 * 
 */
package com.techmobia.supportmanager.services;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.poifs.crypt.EncryptionInfo;
import org.apache.poi.poifs.crypt.EncryptionMode;
import org.apache.poi.poifs.crypt.Encryptor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.opencsv.CSVWriter;
import com.techmobia.supportmanager.model.DataUpload;
import com.techmobia.supportmanager.model.Distributor;
import com.techmobia.supportmanager.model.Email;
import com.techmobia.supportmanager.model.Login;
import com.techmobia.supportmanager.model.OfferCodeUpload;
import com.techmobia.supportmanager.model.OfferCodeValidInput;
import com.techmobia.supportmanager.model.SideMenu;
import com.techmobia.supportmanager.model.SideMenuData;
import com.techmobia.supportmanager.model.UserPermission;
import javax.crypto.spec.IvParameterSpec;

/**
 * @author vinay.sethi
 *
 */
public class DbHandler {
	Connection conn = null;
	private static DbHandler mInstance = null;
	private static final Logger logger = Logger.getLogger(DbHandler.class);
	private static final String ALPHA_NUMERIC_STRING = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        private static final String encryptionKey = "techmobiaproject";
	static {
		if (mInstance == null) {
			mInstance = new DbHandler();
		}
	}

	public static DbHandler getInstance() {
		return mInstance;
	}

	private DbHandler() {
		PropertyConfigurator.configure(Constants.LOG4JFILEPATH);
		String username = null;
		String password = null;
		String hostname = null;
		try {
			Class.forName(PropertyHandler.getInstance().getValue("class_driver"));
			username = PropertyHandler.getInstance().getValue("username_mysql_db");
			password = PropertyHandler.getInstance().getValue("password_mysql_db");
			hostname = PropertyHandler.getInstance().getValue("hostname_mysql_db");
			conn = DriverManager.getConnection(hostname, username, password);
		} catch (Exception e) {
			logger.error(e);
		} finally {
			username = null;
			password = null;
			hostname = null;
		}
	}

	private Connection getConnection() {
		PropertyConfigurator.configure(Constants.LOG4JFILEPATH);
		String username = null;
		String password = null;
		String hostname = null;
		try {
			Class.forName(PropertyHandler.getInstance().getValue("class_driver"));
			username = PropertyHandler.getInstance().getValue("username_mysql_db");
			password = PropertyHandler.getInstance().getValue("password_mysql_db");
			hostname = PropertyHandler.getInstance().getValue("hostname_mysql_db");
			conn = DriverManager.getConnection(hostname, username, password);
		} catch (Exception e) {
			logger.error(e);
		} finally {
			username = null;
			password = null;
			hostname = null;
		}
		return conn;
	}

	/**
	 * Closes Db Connection
	 * 
	 * @return
	 */

	public String checkClose() {
		PropertyConfigurator.configure(Constants.LOG4JFILEPATH);
		try {
			conn.close();
			conn = null;
			return "closed";
		} catch (Exception e) {
			logger.error(e);
		}
		return "failure";
	}

	/**
	 * Validate Login Credentials
	 * 
	 * @return
	 */

	public Login validateLogin(String email, String password) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean found;
		Login login = new Login();
		Connection con = null;
		try {
			con = this.getConnection();
			query = "select empid,username,password,mobile_number,email,usertype_id,ReadModules,ReadWriteModules from _tbl_userprofile_details where email=? and password=? and status=1";
			pstmt = con.prepareStatement(query);
			logger.info(query + "-->" + email + "-->" + password);
			pstmt.setString(1, email);
			pstmt.setString(2, password);
			rs = pstmt.executeQuery();
			found = rs.next();
			if (found) {
				login.setEmpId(rs.getInt("empid"));
				login.setUserName(rs.getString("username"));
				login.setUserPassword(rs.getString("password"));
				login.setMobileNumber(rs.getString("mobile_number"));
				login.setUserEmail(rs.getString("email"));
				login.setReadModules(rs.getString("ReadModules"));
				login.setReadWriteModules(rs.getString("ReadWriteModules"));
                               login.setUserType(UserTypeRepository.getInstance().getValue(rs.getInt("usertype_id")));
			}
			logger.debug(login);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			query = null;
			email = null;
			password = null;
			try {
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return login;
	}

	/**
	 * Validate Ip Address
	 * 
	 * @return
	 */

	public int validateIpAddress(String clientIp) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		boolean found;
		int validateCounter = 0;
		Connection con = null;
		try {
			con = this.getConnection();
			query = "select count(*) as count from _tbl_ipaddress_master where IpAddress=? and Status=1";
			pstmt = con.prepareStatement(query);
			logger.info(query + "-->" + clientIp);
			pstmt.setString(1, clientIp);
			rs = pstmt.executeQuery();
			found = rs.next();
			if (found) {
				validateCounter = rs.getInt("count");
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			query = null;
			try {
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return validateCounter;
	}

	/**
	 * Fetch Service Name From Master Table
	 * 
	 * @return
	 */

	public List<String> fetchServices() {
		String query = null;
		List<String> dbRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = "select service_name from _tbl_servicename_master where status=1";
			logger.info(query);
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dbRecords.add(rs.getString("service_name"));
			}
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return dbRecords;
	}

	/**
	 * Fetch Offer Type From Master Table
	 * 
	 * @return
	 */

	public List<String> fetchOfferType() {
		String query = null;
		List<String> dbRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = "select offertype_id,type_description from itrial_v3._tbl_offertype_details where process_status=1";
			logger.info(query);
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dbRecords.add(rs.getInt("offertype_id") + "-" + rs.getString("type_description"));
			}
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return dbRecords;
	}

	/**
	 * Fetch Branch Name From Master Table
	 * 
	 * @return
	 */

	public String fetchWsSiteDetails() {
		String query = null;
		List<String> dbRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = "select SiteId,SiteName from wsitrial_application._tbl_site_master where Status=1";
			logger.info(query);
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dbRecords.add(rs.getInt("SiteId") + "@" + rs.getString("SiteName"));
			}
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return dbRecords.toString().replace("[", "").replace("]", "").replace(" ,", ",").replace(", ", ",");
	}

	/**
	 * Fetch Branch Name From Master Table
	 * 
	 * @return
	 */

	public List<String> fetchBranchName(int offertype) {
		String query = null;
		List<String> dbRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = "select branch_id,branch_code,project_id,branch_name from itrial_v3._tbl_branch_details where offertype_id=? and process_status=1";
			logger.info(query);
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, offertype);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dbRecords.add(rs.getInt("branch_id") + "@" + rs.getString("branch_code") + "@"
						+ rs.getString("project_id") + "-" + rs.getString("branch_name"));
			}
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return dbRecords;
	}

	/**
	 * Fetch ProjectId Name From Master Table
	 * 
	 * @return
	 */

	public List<String> fetchProjectId(int siteId) {
		String query = null;
		List<String> dbRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = "select project_id,projectid_site_name from pampers_application_v2._tbl_projectsite_mapping_master where site_id=? and status=1";
			logger.info(query);
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, siteId);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dbRecords.add(rs.getInt("project_id") + "@" + rs.getString("projectid_site_name"));
			}
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return dbRecords;
	}

	/**
	 * Fetch Branch Name From Master Table
	 * 
	 * @return
	 */

	public String fetchBranchName(String branch_id,String instanceId) {
		String query = null;
		String branchName = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = "select branch_name from itrial_v3._tbl_branch_details where branch_id=? and process_status=1";
			logger.info(query+" instance id is --->> "+instanceId);
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, branch_id);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				branchName = rs.getString("branch_name");
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return branchName;
	}

	/**
	 * Fetch Branch Name From Master Table
	 * 
	 * @return
	 */

	public String getBranchDetails() {
		String query = null;
		List<String> dbRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = "select BranchId,BranchName from wsitrial_application._tbl_branch_master where Status=1";
			logger.info(query);
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dbRecords.add(rs.getInt("BranchId") + "@" + rs.getString("BranchName"));
			}
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		logger.info(dbRecords.toString());
		return dbRecords.toString().replace("[", "").replace("]", "").replace(" ,", ",").replace(", ", ",");
	}

	/**
	 * Fetch Branch Name From Master Table
	 * 
	 * @return
	 */

	public String getLanguageDetails() {
		String query = null;
		List<String> dbRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = "select LanguageId,LanguageName from wsitrial_application._tbl_language_master where Status=1";
			logger.info(query);
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dbRecords.add(rs.getInt("LanguageId") + "@" + rs.getString("LanguageName"));
			}
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		logger.info(dbRecords.toString());
		return dbRecords.toString().replace("[", "").replace("]", "").replace(" ,", ",").replace(", ", ",");
	}

	/**
	 * Fetch Campaign Name From Master Table
	 * 
	 * @return
	 */

	public List<String> fetchCampaignName() {
		String query = null;
		List<String> dbRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = "select description from h2h_v3._tbl_project_site_mapping";
			logger.info(query);
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dbRecords.add(rs.getString("description"));
			}
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return dbRecords;
	}

	/**
	 * Fetch Site Name From Master Table
	 * 
	 * @return
	 */

	public List<String> fetchSiteNames(String serviceName) {
		String query = null;
		List<String> dbRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.getConnection();
			if (serviceName.equals("h2hold")) {
				query = "select site_name from h2h_v3._tbl_project_site_mapping";
			} else if (serviceName.equals("pampers")) {
				query = "select site_id,site_name from pampers_application_v2._tbl_site_master where status=1";
			} else if (serviceName.equals("itrial")) {
				query = "select site_id,site_name from itrial_v3._tbl_site_details where process_status=1";
			} else {
				query = PropertyHandler.getInstance().getValue(serviceName + "_get_site_name");
			}
			logger.info(query);
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				if (serviceName.equals("h2hold")) {
					dbRecords.add(rs.getString("site_name"));
				} else if (serviceName.equals("pampers") || serviceName.equals("itrial")) {
					dbRecords.add(rs.getString("site_id") + "@" + rs.getString("site_name"));
				} else {
					dbRecords.add(rs.getString("SiteName"));
				}
			}
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return dbRecords;
	}

	/**
	 * Fetch Site ID From Master Table
	 * 
	 * @return
	 */

	public int getSiteId(String serviceName, String siteName) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		int siteId = 0;
		try {
			con = this.getConnection();
			if (serviceName.equals("pampers")) {
				query = "select site_id from pampers_application_v2._tbl_site_master where status=1 and site_name=?";
			} else if (serviceName.equals("h2h")) {
				query = "select SiteId as site_id from h2h_application._tbl_site_master where Status=1 and SiteName=?";
			} else if (serviceName.equals("itrial")) {
				query = "select site_id from itrial_v3._tbl_site_details where process_status=1 and site_name=?";
			} else {
				query = PropertyHandler.getInstance().getValue(serviceName + "_get_site_id");
			}
			logger.info(query + "-->" + siteName);
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, siteName);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				siteId = rs.getInt("site_id");
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return siteId;
	}

	/**
	 * Validate ProjectId & SiteId Mapping
	 * 
	 * @return
	 */

	public int vaidateProjectSiteMapping(String serviceName, int projectId, int siteId) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		int validateCounter = 0;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(serviceName + "_validateProjectId");
			logger.info(query + "-->" + projectId);
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, projectId);
			pstmt.setInt(2, siteId);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				validateCounter = rs.getInt("validateCounter");
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return validateCounter;
	}

	/**
	 * Fetch UserType Details From Master Table
	 * 
	 * @return
	 */

	public Map<Integer, String> fetchUserType() {
		String query = null;
		HashMap<Integer, String> dbRecords = new HashMap<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = "select usertype_id,usertype_description from _tbl_usertype_master where status=1";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dbRecords.put(rs.getInt("usertype_id"), rs.getString("usertype_description"));
			}
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		logger.debug(query + "-->" + dbRecords);
		return dbRecords;
	}

	/**
	 * Fetch Pharmacy Details From Master Table
	 * 
	 * @return
	 */

	public Map<String, Integer> getTotalPharmacyStores() {
		String query = null;
		HashMap<String, Integer> dbRecords = new HashMap<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue("getTotalPharmacyStoresQuery");
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dbRecords.put(rs.getString("projectid_site_name"), rs.getInt("totalStores"));
			}
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		logger.debug(query + "-->" + dbRecords);
		return dbRecords;
	}

	/**
	 * Fetch Brand From Master Table
	 * 
	 * @return
	 */

	public Map<String, Integer> fetchBrand() {
		String query = null;
		HashMap<String, Integer> dbRecords = new HashMap<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = "select brand_id,brand_name from itrial_digital_v3._tbl_brand_details where status=1";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dbRecords.put(rs.getString("brand_name"), rs.getInt("brand_id"));
			}
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		// logger.debug(query + "-->" + dbRecords);
		return dbRecords;
	}

	/**
	 * Insert File Data Into Table
	 * 
	 * @return
	 */

	public void addFileRecords(String fileData, String fileUploadId) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		String[] fileDataArr;
		int insertedRows = 0;
		try {
			con = this.getConnection();
			fileDataArr = fileData.split(Constants.DATAPPENDER.replace("*", "\\*"));
			for (int i = 1; i < fileDataArr.length; i++) {
				query = "insert ignore into _tbl_drcpdata_details (BranchName,IteName,DseName,RouteNo,RetailerCode,SwingCode,RetailerName,Address,MobileNumber,Channel,LeapCode,FileUploadId) values ('"
						+ fileDataArr[i].replace("+", " ").replace(",", "','") + "')";
				logger.debug("Drcp Start DrcpData Insert Query-->" + query + "-->" + fileUploadId);
				pstmt = con.prepareStatement(query);
				pstmt.executeUpdate();
			}
			query = "update _tbl_fileupload_details set Status=3,UpdateDate=now() where FileUploadId=?";
			logger.info("Drcp Update Finish DrcpData Insert Query-->" + query + "-->" + fileUploadId);
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, Integer.parseInt(fileUploadId));
			pstmt.executeUpdate();
			query = "update " + PropertyHandler.getInstance().getValue("drcpdatabase")
					+ "._tbl_retailer_details a, _tbl_fileupload_details b, _tbl_drcpdata_details c set a.process_status=0,a.modifiedate=now() where a.project_id=b.ProjectId and a.branch_code=b.BranchCode and b.FileUploadId=?";
			logger.info("Drcp Update Retailer Status Query-->" + query + "-->" + fileUploadId);
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, Integer.parseInt(fileUploadId));
			pstmt.executeUpdate();
			query = "update " + PropertyHandler.getInstance().getValue("drcpdatabase")
					+ "._tbl_retailer_registration_details a, _tbl_fileupload_details b, _tbl_drcpdata_details c set a.process_status=0,a.modifiedate=now() where a.project_id=b.ProjectId and a.branch_code=b.BranchCode and b.FileUploadId=?";
			logger.info("Drcp Update Retailer Registration Status Query-->" + query + "-->" + fileUploadId);
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, Integer.parseInt(fileUploadId));
			pstmt.executeUpdate();
			query = "update " + PropertyHandler.getInstance().getValue("drcpdatabase")
					+ "._tbl_retailer_registration_details a, _tbl_fileupload_details b, _tbl_drcpdata_details c set a.ite_name=c.IteName,c.DrcpStatus=1,a.process_status=1,a.modifiedate=now() where a.project_id=b.ProjectId and a.branch_code=b.BranchCode and c.DrcpStatus=0 and a.retailer_code=c.RetailerCode and b.FileUploadId=?";
			logger.info("Drcp Update Retailer Registration Existing Data Query-->" + query + "-->" + fileUploadId);
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, Integer.parseInt(fileUploadId));
			pstmt.executeUpdate();
			query = "update " + PropertyHandler.getInstance().getValue("drcpdatabase")
					+ "._tbl_retailer_details a, _tbl_fileupload_details b, _tbl_drcpdata_details c set a.ite_name=c.IteName,a.ite_code=substr(c.IteName,-1),a.swing_code=c.SwingCode,a.route_no=c.RouteNo,c.DrcpStatus=2,a.process_status=2,a.modifiedate=now() where a.project_id=b.ProjectId and a.branch_code=b.BranchCode and c.DrcpStatus=1 and a.retailer_code=c.RetailerCode and b.FileUploadId=?";
			logger.info("Drcp Update Retailer Registration Existing Data Query-->" + query + "-->" + fileUploadId);
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, Integer.parseInt(fileUploadId));
			pstmt.executeUpdate();
			query = "update " + PropertyHandler.getInstance().getValue("drcpdatabase")
					+ "._tbl_retailer_details a, _tbl_fileupload_details b, _tbl_drcpdata_details c set a.ite_name=c.IteName,a.ite_code=substr(c.IteName,-1),a.swing_code=c.SwingCode,a.route_no=c.RouteNo,c.DrcpStatus=3,a.process_status=1,a.modifiedate=now() where a.project_id=b.ProjectId and a.branch_code=b.BranchCode and c.DrcpStatus=0 and a.retailer_code=c.RetailerCode and a.process_status=0 and b.FileUploadId=?";
			logger.info("Drcp Update Retailer Existing Data Query-->" + query + "-->" + fileUploadId);
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, Integer.parseInt(fileUploadId));
			pstmt.executeUpdate();
			query = "insert ignore into " + PropertyHandler.getInstance().getValue("drcpdatabase")
					+ "._tbl_retailer_details (project_id,branch_code,ite_name,ite_code,retailer_code,swing_code,retailer_name,mobile_number,retailer_address,route_no,dse_name,channel_desc) select b.ProjectId,b.BranchCode,a.IteName,substr(IteName,-1),a.RetailerCode,a.SwingCode,a.RetailerName,a.MobileNumber,a.Address,a.RouteNo,a.DseName,a.Channel from _tbl_drcpdata_details a inner join _tbl_fileupload_details b where a.FileUploadId=b.FileUploadId and a.DrcpStatus=0 and b.FileUploadId=?";
			logger.info("Drcp Insert Missing DrcpData Data Query-->" + query + "-->" + fileUploadId);
			pstmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			pstmt.setInt(1, Integer.parseInt(fileUploadId));
			pstmt.executeUpdate();
			rs = pstmt.getGeneratedKeys();
			while (rs.next()) {
				insertedRows = rs.getInt(1);
			}
			query = "update " + PropertyHandler.getInstance().getValue("drcpdatabase")
					+ "._tbl_retailer_details a, _tbl_fileupload_details b, _tbl_drcpdata_details c set a.total_offer_count=b.TotalOfferCount where a.project_id=b.ProjectId and a.branch_code=b.BranchCode and c.DrcpStatus=0 and a.retailer_code=c.RetailerCode and a.total_offer_count is NULL and b.FileUploadId=?";
			logger.info("Drcp Update DrcpData Status Query-->" + query + "-->" + fileUploadId + "-->" + insertedRows);
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, Integer.parseInt(fileUploadId));
			pstmt.executeUpdate();
			query = "update _tbl_drcpdata_details set DrcpStatus=4,UpdateDate=now() where DrcpStatus=0 and FileUploadId=?";
			logger.info("Drcp Update DrcpData Status Query-->" + query + "-->" + fileUploadId);
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, Integer.parseInt(fileUploadId));
			pstmt.executeUpdate();
			query = "update _tbl_fileupload_details set Status=4,UpdateDate=now() where FileUploadId=?";
			logger.info("Drcp Update Final FileUpload Status Query-->" + query + "-->" + fileUploadId);
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, Integer.parseInt(fileUploadId));
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
			try {
				query = "update _tbl_drcpdata_details set DrcpStatus=-1,UpdateDate=now() where FileUploadId=?";
				logger.info("Drcp Update DrcpData Status Query-->" + query + "-->" + fileUploadId);
				pstmt = con.prepareStatement(query);
				pstmt.setInt(1, Integer.parseInt(fileUploadId));
				pstmt.executeUpdate();
			} catch (Exception catchexception) {
				logger.error(query + "-->" + catchexception + Arrays.asList(exception.getStackTrace()).stream()
						.map(Objects::toString).collect(Collectors.joining("\n")));
			}
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	/**
	 * Insert Offer Data Into Table
	 * 
	 * @return
	 */

	public void addOfferFileRecords(String fileData, String action, String fileUploadId) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		String[] fileDataArr;
		try {
			con = this.getConnection();
			fileDataArr = fileData.split(Constants.DATAPPENDER.replace("*", "\\*"));
			for (int i = 0; i < fileDataArr.length; i++) {
				if (action.equalsIgnoreCase("addMessages")) {
					query = "insert ignore into _tbl_offermessages_details (MessageKey,MessageText,FileUploadId) values ('"
							+ fileDataArr[i].replace(",", "','") + "')";
				} else if (action.equalsIgnoreCase("addOffer")) {
					query = "insert ignore into _tbl_offercode_details (offer_code,offer_code_ref,offer_description,offertype_id,site_id,brand_id,brand_price,brand_discount,offer_startdate,offer_endate,FileUploadId) values ('"
							+ fileDataArr[i].replace(",", "','") + "')";
				} else {
					query = "insert ignore into _tbl_offerprompts_details (PromptName,FileUploadId) values ('"
							+ fileDataArr[i].replace(",", "','") + "')";
				}
				logger.debug("Monthly Offer Data Insert Query-->" + query + "-->" + fileUploadId);
				pstmt = con.prepareStatement(query);
				pstmt.executeUpdate();
			}
			query = "update _tbl_fileupload_details set Status=3,UpdateDate=now() where FileUploadId=?";
			logger.info("Monthly Offer FileUpload Status Query-->" + query + "-->" + fileUploadId);
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, Integer.parseInt(fileUploadId));
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	/**
	 * Insert Offer Data Into Table
	 * 
	 * @return
	 */

	public void addOneCpDetails(String serviceName, String startDate, String endDate, String fileType) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		try {
			con = this.getConnection();
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(serviceName.toLowerCase() + "_insert_onecp");
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, serviceName);
			pstmt.setString(2, startDate);
			pstmt.setString(3, endDate + " 23:59:59");
			pstmt.setString(4, fileType);
			logger.info("C360 file Insert Query is " + query + "-->" + serviceName + "-->" + startDate + "-->" + endDate
					+ "-->" + fileType);
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	/**
	 * Insert Retailer Number File Data Into Table
	 * 
	 * @return
	 */

	public void addRetailerNumberFileRecords(String fileData, String fileUploadId) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		ResultSet rs1 = null;
		String[] fileDataArr;
		String mobileNumber = null;
		String branchCode = null;
		String retailerCode = null;
		int counter = 0;
		int status = 0;
		String mobileNumberUniqueId = "NA";
		try {
			con = this.getConnection();
			fileDataArr = fileData.split(Constants.DATAPPENDER.replace("*", "\\*"));
			for (int i = 1; i < fileDataArr.length; i++) {
				query = "insert ignore into itrial_v3._tbl_retailernumber_details (MobileNumber,BranchCode,RetailerCode,SiteId,FileUploadId) values ('"
						+ fileDataArr[i].replace(",", "','") + "')";
				logger.debug("Retailer Number Update Insert Query-->" + query + "-->" + fileUploadId);
				pstmt = con.prepareStatement(query);
				pstmt.executeUpdate();
			}
			query = "select RetailerId,MobileNumber,BranchCode,RetailerCode,SiteId from itrial_v3._tbl_retailernumber_details where Status=0";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				mobileNumber = rs.getString("MobileNumber");
				branchCode = rs.getString("BranchCode");
				retailerCode = rs.getString("RetailerCode");
				query = "update itrial_v3._tbl_retailernumber_details set Status=1,UpdateDate=now() where RetailerId=?";
				logger.debug("Retailer Number Update Insert Query-->" + query + "-->" + rs.getInt("RetailerId"));
				pstmt = con.prepareStatement(query);
				pstmt.setInt(1, rs.getInt("RetailerId"));
				pstmt.executeUpdate();
				query = "select count(*) as count from itrial_v3._tbl_retailer_registration_details where branch_code=? and retailer_code=? and process_status=1";
				logger.debug(
						"Retailer Number Update Insert Query-->" + query + "-->" + branchCode + "-->" + retailerCode);
				pstmt = con.prepareStatement(query);
				pstmt.setString(1, branchCode);
				pstmt.setString(2, retailerCode);
				rs1 = pstmt.executeQuery();
				while (rs1.next()) {
					counter = rs1.getInt("count");
				}
				if (counter > 0) {
					mobileNumberUniqueId = generate_alphanumeric(mobileNumber, 10);
					query = "update itrial_v3._tbl_retailer_registration_details set mobile_number=? where branch_code=? and retailer_code=? and process_status=1";
					logger.info("Retailer Number Update Insert Query-->" + query + "-->" + branchCode + "-->"
							+ retailerCode + "-->" + mobileNumberUniqueId);
					pstmt = con.prepareStatement(query);
					pstmt.setString(1, mobileNumberUniqueId);
					pstmt.setString(2, branchCode);
					pstmt.setString(3, retailerCode);
					status = 2;
				} else {
					query = PropertyHandler.getInstance()
							.getValue("update_retailernumber_query_" + rs.getInt("SiteId"));
					pstmt = con.prepareStatement(query);
					pstmt.setString(1, mobileNumber);
					status = 3;
				}
				pstmt.executeUpdate();
				query = "update itrial_v3._tbl_retailernumber_details set Status=?,UpdateDate=now() where RetailerId=?";
				pstmt = con.prepareStatement(query);
				pstmt.setInt(1, status);
				pstmt.setInt(2, rs.getInt("RetailerId"));
				pstmt.executeUpdate();
			}
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	/**
	 * Insert Distributor File Data Into Table
	 * 
	 * @return
	 */

	public void addDistributorFileRecords(String fileData, String fileUploadId) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		String[] fileDataArr;
		int insertedRows = 0;
		try {
			con = this.getConnection();
			fileDataArr = fileData.split(Constants.DATAPPENDER.replace("*", "\\*"));
			for (int i = 1; i < fileDataArr.length; i++) {
				query = "insert ignore into wsitrial_application._tbl_distributor_details (StateId,SiteId,BranchCode,BranchId,WholeSalerName,WholeSalerCode,IvrBranchCode,IvrDistributorCode,FileUploadId) values ('"
						+ fileDataArr[i].replace(",", "','") + "')";
				logger.debug("Drcp Start DrcpData Insert Query-->" + query + "-->" + fileUploadId);
				pstmt = con.prepareStatement(query);
				pstmt.executeUpdate();
			}
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void updateDrcpFileStatus(String fileUploadId) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = "update _tbl_fileupload_details set Status=2,UpdateDate=now() where FileUploadId=?";
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, Integer.parseInt(fileUploadId));
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public List<String> addDataUploadDetails(DataUpload dataupload) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		StringBuilder dataBuilder = new StringBuilder();
		List<String> insertedRows = new ArrayList<>();
		ResultSet rs = null;
		try {
			con = this.getConnection();
			query = "insert ignore into _tbl_fileupload_details (" + Constants.DATAUPLOADCOLUMNS
					+ ") values (?,?,?,?,?,?,?,?,?)";
			pstmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			pstmt.setInt(1, dataupload.getOfferTypeId());
			pstmt.setString(2, dataupload.getBranchName());
			pstmt.setString(3, dataupload.getBranchCode());
			pstmt.setInt(4, dataupload.getProjectId());
			pstmt.setString(5, dataupload.getFileName());
			pstmt.setString(6, dataupload.getUploadFileName().toString());
			pstmt.setString(7, Integer.toString(dataupload.getEmpId()));
			pstmt.setString(8, dataupload.getTotalOfferCount());
			pstmt.setString(9, Integer.toString(dataupload.getFlowtypeId()));
			dataBuilder.append(pstmt.executeUpdate());
			dataBuilder.append("@");
			rs = pstmt.getGeneratedKeys();
			while (rs.next()) {
				dataBuilder.append(rs.getInt(1));
			}
			insertedRows.add(dataBuilder.toString());
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return insertedRows;
	}

	public List<String> addMonthlyDataUploadDetails(DataUpload dataupload) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		StringBuilder dataBuilder = new StringBuilder();
		List<String> insertedRows = new ArrayList<>();
		ResultSet rs = null;
		String[] messageTextArr;
		String[] offerNameArr;
		String[] brandPriceArr;
		String[] brandDiscountArr;
		int offerCode = 0;
		try {
			con = this.getConnection();
			query = "insert ignore into " + PropertyHandler.getInstance().getValue("monthlyOfferDatabaseName")
					+ "._tbl_monthlydata_details (" + Constants.MONTHLYOFFERCOLUMNS + ") values (?,?,?,?,?,?,?,?,?,?)";
			logger.info("Monthly Data Insert Query-->" + query + "-->" + dataupload.getOfferTypeId() + "-->"
					+ dataupload.getSiteId() + "-->" + dataupload.getSiteName() + "-->" + dataupload.getFlowtypeId()
					+ "-->" + dataupload.getNumberOfOffers() + "-->" + dataupload.getTotalOfferCount() + "-->"
					+ dataupload.getEmpId());
			pstmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
			pstmt.setInt(1, dataupload.getOfferTypeId());
			pstmt.setInt(2, dataupload.getSiteId());
			pstmt.setString(3, dataupload.getSiteName());
			pstmt.setString(4, Integer.toString(dataupload.getFlowtypeId()));
			pstmt.setString(5, Integer.toString(dataupload.getNumberOfOffers()));
			pstmt.setString(6, dataupload.getTotalOfferCount());
			pstmt.setString(7, dataupload.getOfferStartDate());
			pstmt.setString(8, dataupload.getOfferEndDate() + " 23:59:59");
			pstmt.setInt(9, dataupload.getOfferCategory());
			pstmt.setString(10, Integer.toString(dataupload.getEmpId()));
			dataBuilder.append(pstmt.executeUpdate());
			dataBuilder.append("@");
			rs = pstmt.getGeneratedKeys();
			while (rs.next()) {
				dataBuilder.append(rs.getInt(1));
			}
			insertedRows.add(dataBuilder.toString());
			query = "update " + PropertyHandler.getInstance().getValue("monthlyOfferDatabaseName")
					+ "._tbl_promptdata_details set DataId=? where DataId=0";
			logger.info("Monthly Data Update Query-->" + query + "-->" + dataBuilder.toString().split("@")[1]);
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, dataBuilder.toString().split("@")[1]);
			pstmt.executeUpdate();
			messageTextArr = dataupload.getMessageText().split("@@&&");
			offerNameArr = dataupload.getOfferName().split(",");
			brandPriceArr = dataupload.getBrandPrice().split(",");
			brandDiscountArr = dataupload.getBrandDiscount().split(",");
			query = "update " + PropertyHandler.getInstance().getValue("monthlyOfferDatabaseName")
					+ "._tbl_messagetext_details set Status=0,UpdateDate=now() where SiteId=? and OfferTypeId=? and Status=1";
			logger.info("Monthly Data Update Query-->" + query + "-->" + dataupload.getSiteId() + "-->"
					+ dataupload.getOfferTypeId());
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, dataupload.getSiteId());
			pstmt.setInt(2, dataupload.getOfferTypeId());
			pstmt.executeUpdate();
			query = "insert ignore into " + PropertyHandler.getInstance().getValue("monthlyOfferDatabaseName")
					+ "._tbl_messagetext_details (OfferNumber,MessageText,SiteId,OfferTypeId,DataId) values (?,?,?,?,?)";
			logger.info("Message Text Insert Query-->" + query + "-->" + dataupload.getNumberOfOffers() + "-->"
					+ dataupload.getMessageText() + "-->" + dataBuilder.toString().split("@")[1]);
			pstmt = con.prepareStatement(query);
			for (int i = 1; i <= dataupload.getNumberOfOffers(); i++) {
				pstmt.setInt(1, i);
				pstmt.setString(2,
						messageTextArr[i].replace("%OFFERNAME" + i, offerNameArr[i])
								.replace("%BRANDPRICE" + i, brandPriceArr[i])
								.replace("%BRANDISCOUNT" + i, brandDiscountArr[i]));
				pstmt.setInt(3, dataupload.getSiteId());
				pstmt.setInt(4, dataupload.getOfferTypeId());
				pstmt.setString(5, dataBuilder.toString().split("@")[1]);
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			query = "insert ignore into " + PropertyHandler.getInstance().getValue("monthlyOfferDatabaseName")
					+ "._tbl_offername_details (OfferNumber,OfferName,BrandPrice,BrandDiscount,OfferStartDate,OfferEndDate,DataId) values (?,?,?,?,?,?,?)";
			logger.info("Offer Name Insert Query-->" + query + "-->" + dataupload.getNumberOfOffers() + "-->"
					+ dataupload.getOfferName() + "-->" + dataupload.getBrandPrice() + "-->"
					+ dataupload.getBrandDiscount() + "-->" + dataBuilder.toString().split("@")[1]);
			pstmt = con.prepareStatement(query);
			for (int i = 1; i <= dataupload.getNumberOfOffers(); i++) {
				pstmt.setInt(1, i);
				pstmt.setString(2, offerNameArr[i]);
				pstmt.setString(3, brandPriceArr[i]);
				pstmt.setString(4, brandDiscountArr[i]);
				pstmt.setString(5, dataupload.getOfferStartDate());
				pstmt.setString(6, dataupload.getOfferEndDate() + " 23:59:59");
				pstmt.setString(7, dataBuilder.toString().split("@")[1]);
				pstmt.addBatch();
			}
			pstmt.executeBatch();
			query = "update " + PropertyHandler.getInstance().getValue("monthlyOfferDatabaseName")
					+ "._tbl_branch_details set offer_category=?,flowtype_id=?,modifiedate=now() where site_id=? and offertype_id=?";
			logger.info("Update Branch Query-->" + query + "-->" + dataupload.getOfferCategory() + "-->"
					+ dataupload.getFlowtypeId() + "-->" + dataupload.getSiteId() + "-->"
					+ dataupload.getOfferTypeId());
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, dataupload.getOfferCategory());
			pstmt.setInt(2, dataupload.getFlowtypeId());
			pstmt.setInt(3, dataupload.getSiteId());
			pstmt.setInt(4, dataupload.getOfferTypeId());
			pstmt.executeUpdate();
			query = "update " + PropertyHandler.getInstance().getValue("monthlyOfferDatabaseName")
					+ "._tbl_retailer_details set total_offer_count=?,modifiedate=now() where project_id in (select project_id from "
					+ PropertyHandler.getInstance().getValue("monthlyOfferDatabaseName")
					+ "._tbl_branch_details where offer_category=? and flowtype_id=?)";
			logger.info("Update Retailer Details Query-->" + query + "-->" + dataupload.getTotalOfferCount() + "-->"
					+ dataupload.getOfferCategory() + "-->" + dataupload.getFlowtypeId());
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, dataupload.getTotalOfferCount());
			pstmt.setInt(2, dataupload.getOfferCategory());
			pstmt.setInt(3, dataupload.getFlowtypeId());
			pstmt.executeUpdate();
			query = "update " + PropertyHandler.getInstance().getValue("monthlyOfferDatabaseName")
					+ "._tbl_retailer_registration_details set total_offer_count=?,modifiedate=now() where project_id  in (select project_id from "
					+ PropertyHandler.getInstance().getValue("monthlyOfferDatabaseName")
					+ "._tbl_branch_details where offer_category=? and flowtype_id=?)";
			logger.info(
					"Update Retailer Registration Details Query-->" + query + "-->" + dataupload.getTotalOfferCount()
							+ "-->" + dataupload.getOfferCategory() + "-->" + dataupload.getFlowtypeId());
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, dataupload.getTotalOfferCount());
			pstmt.setInt(2, dataupload.getOfferCategory());
			pstmt.setInt(3, dataupload.getFlowtypeId());
			pstmt.executeUpdate();
			query = "update " + PropertyHandler.getInstance().getValue("monthlyOfferDatabaseName")
					+ "._tbl_offercode_details set process_status=0 where process_status=1 and offertype_id=? and site_id=?";
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, dataupload.getOfferTypeId());
			pstmt.setInt(2, dataupload.getSiteId());
			logger.info("Update Offer Code Query-->" + query + "-->" + dataupload.getOfferTypeId() + "-->"
					+ dataupload.getSiteId());
			pstmt.executeUpdate();
			query = "select max(offer_code_ref) as offer_code_ref from "
					+ PropertyHandler.getInstance().getValue("monthlyOfferDatabaseName") + "._tbl_offercode_details";
			logger.info("Fetch Offer Code Reference Query-->" + query);
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				offerCode = rs.getInt("offer_code_ref");
			}
			query = "insert ignore into " + PropertyHandler.getInstance().getValue("monthlyOfferDatabaseName")
					+ "._tbl_offercode_details (offer_code,offer_code_ref,offer_description,offertype_id,site_id,brand_id,brand_price,brand_discount,offer_startdate,offer_endate) values (?,?,?,?,?,?,?,?,?,?)";
			pstmt = con.prepareStatement(query);
			for (int i = 1; i <= dataupload.getNumberOfOffers(); i++) {
				logger.info("Insert Offer Code Details Query-->" + query + "-->" + i + "-->" + offerCode + "-->"
						+ offerNameArr[i] + "-->" + dataupload.getOfferTypeId() + "-->" + dataupload.getSiteId() + "-->"
						+ brandPriceArr[i] + "-->" + brandDiscountArr[i] + "-->" + dataupload.getOfferStartDate()
						+ "-->" + dataupload.getOfferEndDate());
				pstmt.setInt(1, i);
				pstmt.setInt(2, offerCode + 1);
				pstmt.setString(3, offerNameArr[i]);
				pstmt.setInt(4, dataupload.getOfferTypeId());
				pstmt.setInt(5, dataupload.getSiteId());
				pstmt.setString(6, getBrandId(offerNameArr[i]));
				pstmt.setString(7, brandPriceArr[i]);
				pstmt.setString(8, brandDiscountArr[i]);
				pstmt.setString(9, dataupload.getOfferStartDate());
				pstmt.setString(10, dataupload.getOfferEndDate() + " 23:59:59");
				pstmt.addBatch();
			}
			pstmt.executeBatch();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return insertedRows;
	}

	public void updateMonthlyClipDetails(DataUpload dataupload) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = "update " + PropertyHandler.getInstance().getValue("monthlyOfferDatabaseName")
					+ "._tbl_promptdata_details set FilePath=?,FileName=?,UploadedFileName=?,EmpId=?,Comments=?,UpdateDate=now() where PromptId=?";
			logger.info("Monthly Data Clip Update Query-->" + query + "-->" + dataupload.getFilePath() + "-->"
					+ dataupload.getFileName() + "-->" + dataupload.getUploadFileName().toString() + "-->"
					+ dataupload.getEmpId() + "-->" + dataupload.getComments() + "-->" + dataupload.getDataId());
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, dataupload.getFilePath());
			pstmt.setString(2, dataupload.getFileName());
			pstmt.setString(3, dataupload.getUploadFileName().toString());
			pstmt.setInt(4, dataupload.getEmpId());
			pstmt.setString(5, dataupload.getComments());
			pstmt.setInt(6, dataupload.getDataId());
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	private String getBrandId(String offerName) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		String[] offerNameArr;
		StringBuilder brandId = new StringBuilder();
		int counter = 0;
		try {
			con = this.getConnection();
			offerNameArr = offerName.split("\\+");
			for (int i = 0; i < offerNameArr.length; i++) {
				query = PropertyHandler.getInstance().getValue("fetchBrandQuery");
				logger.info(query + "-->" + offerNameArr[i]);
				pstmt = con.prepareStatement(query);
				pstmt.setString(1, offerNameArr[i]);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					counter = 1;
					brandId.append(rs.getString("BrandId") + "-");
				}
			}
			if (counter == 0) {
				brandId.append("NA-NA-");
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception ex) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + ex + Arrays.asList(ex.getStackTrace())
						.stream().map(Objects::toString).collect(Collectors.joining("\n")));
			}
		}
		return brandId.substring(0, brandId.length() - 1);
	}

	public void addPromptDetails(DataUpload dataupload) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = "insert ignore into " + PropertyHandler.getInstance().getValue("monthlyOfferDatabaseName")
					+ "._tbl_promptdata_details (FilePath,FileName,UploadedFileName) values (?,?,?)";
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, dataupload.getFilePath());
			pstmt.setString(2, dataupload.getFileName());
			pstmt.setString(3, dataupload.getUploadFileName().toString());
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	/**
	 * Change Password of Admin in table
	 * 
	 * @return
	 */

	public void updatePassword(String newPassword, String email) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = "update _tbl_userprofile_details set password=?,updateDate=now(),PasswordExpiryDate=NOW() + INTERVAL 45 DAY where email=?";
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, newPassword);
			pstmt.setString(2, email);
			pstmt.executeUpdate();
                       logger.info("Query --->>> "+query+" and password is ---->>> "+newPassword+" and email is-->>"+email);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public List<String> fetchData(String queryCondition, String serviceName, String category, String queryColumns,
			String dateCondition, String filePath, String reportingFileName,String instanceId) {
		String query = null;
		List<String> dbRecords = new ArrayList<>();
		List<String> panelRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String[] columnArr;
		String[] reportHeaders;
		String tableName = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_tablename");
		String dbCondition = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_dbcondition");
		Connection con = null;
		StringBuilder headerNames = new StringBuilder();
		int dataCounter = 0;
		try {
			logger.info("Instance id is "+instanceId+"-->"+filePath + "-->" + reportingFileName + "-->" + dateCondition);
			CSVWriter writer = new CSVWriter(new FileWriter(filePath + reportingFileName));
			if (category.equals("pampersdatabackend") || category.equals("pampersnewdatabackend")
					|| category.equals("pampersdata") || category.equals("pampersnewdata")) {
				if (serviceName.equalsIgnoreCase("pampersoffline")) {
					reportHeaders = new String[] { "Mobile Number", "Mo Sms Id", "Mo RequestTime", "Site Name",
							"Tcid Input", "Tcid Status", "Optin Input", "Optin Status" };
				} else if (serviceName.equalsIgnoreCase("pampersapi")) {
					reportHeaders = new String[] { "Mobile Number", "Mo API Id", "Mo RequestTime", "Site Name",
							"Tcid Input", "Tcid Status", "Optin Input", "Optin Status" };
				} else {
					reportHeaders = new String[] { "Mobile Number", "Tcid Input", "Tcid Status", "Optin Input",
							"Optin Status", "Retry Number", "Misscall Time", "CallAttemptedTS", "Connected", "StartTS",
							"Call EndTime", "Site Name" };
				}
			} else {
				columnArr = queryColumns.split("@");
				for (int i = 0; i < columnArr.length; i++) {
					if (columnArr[i].indexOf(" as ") == -1) {
						if (columnArr[i].indexOf("\\.") != -1) {
							headerNames.append(
									PropertyHandler.getInstance().getValue(columnArr[i].split("\\.")[1] + "_name")
											+ ",");
						} else {
							headerNames.append(PropertyHandler.getInstance().getValue(columnArr[i] + "_name") + ",");
						}
					} else {
						headerNames.append(
								PropertyHandler.getInstance().getValue(columnArr[i].split(" as ")[1] + "_name") + ",");
					}
				}
				reportHeaders = headerNames.deleteCharAt(headerNames.lastIndexOf(",")).toString().split(",");
				logger.info("Instance id is "+instanceId+"-->"+"Report Headers-->" + reportHeaders);
			}
			writer.writeNext(reportHeaders);
			con = this.getConnection();
			if (queryCondition.equals("1=1")) {
				query = "select " + queryColumns.replace("@", ",") + " from " + tableName + " where " + dateCondition
						+ " " + dbCondition;
			} else {
				query = "select " + queryColumns.replace("@", ",") + " from " + tableName + " where " + queryCondition
						+ " " + dbCondition;
			}
			logger.info("Instance id is "+instanceId+"-->"+query);
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dataCounter = dataCounter + 1;
				columnArr = queryColumns.split("@");
				for (int i = 0; i < columnArr.length; i++) {
					if (columnArr[i].indexOf(" as ") == -1) {
						if (columnArr[i].indexOf("\\.") != -1) {
							if (dataCounter < Integer
									.parseInt(PropertyHandler.getInstance().getValue("dataLimitCounter"))) {
								dbRecords.add(rs.getString(columnArr[i].split("\\.")[1]));
							}
							panelRecords.add(rs.getString(columnArr[i].split("\\.")[1]));
						} else {
							if (dataCounter < Integer
									.parseInt(PropertyHandler.getInstance().getValue("dataLimitCounter"))) {
								dbRecords.add(rs.getString(columnArr[i]));
							}
							if (columnArr[i].split("\\.").length > 1
									&& columnArr[i].split("\\.")[1].equals("store_code")
									&& (category.equalsIgnoreCase("pampersbandhandata")
											|| category.equalsIgnoreCase("pampersbandhannewdata"))) {
								panelRecords.add(Utility.decrypt(rs.getString(columnArr[i].split("\\.")[1]),
										"p@mp$rS@05062020", "png@ppl!210620@)"));
							} else {
								panelRecords.add(rs.getString(columnArr[i]));
							}
						}
					} else {
						if (dataCounter < Integer
								.parseInt(PropertyHandler.getInstance().getValue("dataLimitCounter"))) {
							dbRecords.add(rs.getString(columnArr[i].split(" as ")[1]));
						}
						panelRecords.add(rs.getString(columnArr[i].split(" as ")[1]));
					}
				}
				writer.writeNext(panelRecords.toArray(new String[panelRecords.size()]));
				panelRecords.clear();
			}
			// writer.writeAll(rs, true);
			writer.flush();
			writer.close();
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				columnArr = null;
				dataCounter = 0;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return dbRecords;
	}

	public List<String> fetchBandhanReportingData(String queryCondition, String serviceName, String category,
			String queryColumns, String dateCondition, String filePath, String reportingFileName) {
		String query = null;
		List<String> dbRecords = new ArrayList<>();
		List<String> panelRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String[] columnArr;
		String tableName = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_tablename");
		String dbCondition = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_dbcondition");
		Connection con = null;
		int dataCounter = 0;
		try {
			logger.info(filePath + "-->" + reportingFileName);
			CSVWriter writer = new CSVWriter(new FileWriter(filePath + reportingFileName));
			if (category.equals("pampersdatabackend") || category.equals("pampersnewdatabackend")) {
				String[] reportHeaders = { "Mobile Number", "Tcid Input", "Tcid Status", "Optin Input", "Optin Status",
						"Retry Number", "Misscall Time", "CallAttemptedTS", "Connected", "StartTS", "Call EndTime",
						"Site Name" };
				writer.writeNext(reportHeaders);
			}
			con = this.getConnection();
			if (queryCondition.equals("1=1")) {
				query = "select " + queryColumns.replace("@", ",") + " from " + tableName + " where " + dateCondition
						+ " " + dbCondition;
			} else {
				query = "select " + queryColumns.replace("@", ",") + " from " + tableName + " where "
						+ queryCondition.replace("CreateDate", "StartDate").replace(">", ">=")
								.replace("sm.store_code", "store_code").replace("od.tcid_dtmf", "tcid_dtmf")
						+ " " + dbCondition;
			}
			logger.info(query);
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dataCounter = dataCounter + 1;
				columnArr = queryColumns.split("@");
				for (int i = 0; i < columnArr.length; i++) {
					if (columnArr[i].indexOf(" as ") == -1) {
						if (columnArr[i].indexOf("\\.") != -1) {
							if (dataCounter < Integer
									.parseInt(PropertyHandler.getInstance().getValue("dataLimitCounter"))) {
								dbRecords.add(rs.getString(columnArr[i].split("\\.")[1]));
							}
							panelRecords.add(rs.getString(columnArr[i].split("\\.")[1]));
						} else {
							if (dataCounter < Integer
									.parseInt(PropertyHandler.getInstance().getValue("dataLimitCounter"))) {
								dbRecords.add(rs.getString(columnArr[i]));
							}
							if (columnArr[i].split("\\.").length > 1
									&& columnArr[i].split("\\.")[1].equals("store_code")
									&& (category.equalsIgnoreCase("pampersbandhandata")
											|| category.equalsIgnoreCase("pampersbandhannewdata"))) {
								panelRecords.add(Utility.decrypt(rs.getString(columnArr[i].split("\\.")[1]),
										"p@mp$rS@05062020", "png@ppl!210620@)"));
							} else {
								panelRecords.add(rs.getString(columnArr[i]));
							}
						}
					} else {
						if (dataCounter < Integer
								.parseInt(PropertyHandler.getInstance().getValue("dataLimitCounter"))) {
							dbRecords.add(rs.getString(columnArr[i].split(" as ")[1]));
						}
						panelRecords.add(rs.getString(columnArr[i].split(" as ")[1]));
					}
				}
				writer.writeNext(panelRecords.toArray(new String[panelRecords.size()]));
				panelRecords.clear();
			}
			writer.flush();
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				columnArr = null;
				dataCounter = 0;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return dbRecords;
	}

	public List<String> fetchMindtreeData(String queryCondition, String serviceName, String category,
			String queryColumns, String dateCondition, String filePath, String reportingFileName) {
		String query = null;
		List<String> dbRecords = new ArrayList<>();
		List<String> panelRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String[] columnArr;
		String tableName = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_tablename");
		String dbCondition = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_dbcondition");
		Connection con = null;
		int dataCounter = 0;
		try {
			CSVWriter writer = new CSVWriter(new FileWriter(filePath + reportingFileName));
			String[] reportHeaders = { "Sno", "ApartyMNo", "BpartyMNo", "ApartyCallStatus", "BpartyCallStatus",
					"CallDuration", "Muid", "CallAttempt", "CounsellorCode", "CampaignId" };
			writer.writeNext(reportHeaders);
			con = this.getConnection();
			if (queryCondition.equals("1=1")) {
				query = "select " + queryColumns.replace("@", ",") + " from " + tableName + " where " + dateCondition
						+ " " + dbCondition;
			} else {
				query = "select " + queryColumns.replace("@", ",") + " from " + tableName + " where " + queryCondition
						+ " " + dbCondition;
			}
			logger.info(query);
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dataCounter = dataCounter + 1;
				panelRecords.add(Integer.toString(dataCounter));
				columnArr = queryColumns.split("@");
				for (int i = 0; i < columnArr.length; i++) {
					if (columnArr[i].indexOf(" as ") == -1) {
						if (columnArr[i].indexOf("\\.") != -1) {
							if (dataCounter < Integer
									.parseInt(PropertyHandler.getInstance().getValue("dataLimitCounter"))) {
								if (columnArr[i].equals("ApartyMNo")) {
									dbRecords.add(rs.getString(Utility.decrypt(columnArr[i].split("\\.")[1],
											"sm$GeN@06072020", "sMs@ppl!)%)620@)")));
								} else if (columnArr[i].equals("BpartyMNo")) {
									dbRecords.add(rs.getString(
											fetchMobileNumber(Integer.parseInt(columnArr[i].split("\\.")[1]))));
								} else {
									dbRecords.add(rs.getString(columnArr[i].split("\\.")[1]));
								}
							}
							if (columnArr[i].equals("ApartyMNo")) {
								panelRecords.add(rs.getString(Utility.decrypt(columnArr[i].split("\\.")[1],
										"sm$GeN@06072020", "sMs@ppl!)%)620@)")));
							} else if (columnArr[i].equals("BpartyMNo")) {
								panelRecords.add(rs
										.getString(fetchMobileNumber(Integer.parseInt(columnArr[i].split("\\.")[1]))));
							} else {
								panelRecords.add(rs.getString(columnArr[i].split("\\.")[1]));
							}
						} else {
							if (dataCounter < Integer
									.parseInt(PropertyHandler.getInstance().getValue("dataLimitCounter"))) {
								dbRecords.add(rs.getString(columnArr[i]));
							}
							panelRecords.add(rs.getString(columnArr[i]));
						}
					} else {
						if (dataCounter < Integer
								.parseInt(PropertyHandler.getInstance().getValue("dataLimitCounter"))) {
							dbRecords.add(rs.getString(columnArr[i].split(" as ")[1]));
						}
						panelRecords.add(rs.getString(columnArr[i].split(" as ")[1]));
					}
				}
				writer.writeNext(panelRecords.toArray(new String[panelRecords.size()]));
				panelRecords.clear();
			}
			writer.flush();
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				columnArr = null;
				dataCounter = 0;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return dbRecords;
	}

	public Map<String, Integer> fetchBrandData(String queryCondition, String serviceName, String category,
			String queryColumns, String dateCondition) {
		String query = null;
		String brandName = null;
		HashMap<String, Integer> dbRecords = new HashMap<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String[] columnArr;
		Connection con = null;
		String tableName = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_tablename");
		String dbCondition = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_dbcondition");
		try {
			con = this.getConnection();
			if (queryCondition.equals("1=1")) {
				query = "select " + queryColumns.replace("@", ",") + " from " + tableName + " where " + dateCondition
						+ " and " + dbCondition;
			} else {
				query = "select " + queryColumns.replace("@", ",") + " from " + tableName + " where " + queryCondition
						+ " and " + dbCondition;
			}
			logger.info(query + "-->" + queryCondition + "-->" + dateCondition);
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				brandName = rs.getString("brand_name");
				columnArr = brandName.split("\\+");
				for (int i = 0; i < columnArr.length; i++) {
					// logger.info("Column
					// Value="+columnArr[i]+"-->"+rs.getInt("total_trials")+"-->"+dbRecords.getOrDefault(columnArr[i],
					// 0));
					// logger.info("Optin Column
					// Value="+columnArr[i]+"-->"+rs.getInt("total_optin")+"-->"+dbRecords.getOrDefault(columnArr[i]+"_optin",
					// 0));
					dbRecords.put(columnArr[i], dbRecords.getOrDefault(columnArr[i], 0) + rs.getInt("total_trials"));
					dbRecords.put(columnArr[i] + "_optin",
							dbRecords.getOrDefault(columnArr[i] + "_optin", 0) + rs.getInt("total_optin"));
				}
			}
			logger.debug(dbRecords.toString());
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				columnArr = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception ex) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + ex + Arrays.asList(ex.getStackTrace())
						.stream().map(Objects::toString).collect(Collectors.joining("\n")));
			}
		}
		return dbRecords;
	}

	public void fetchTeleCallingData_14032022(String queryCondition, String serviceName, String category,
			String queryColumns, String dateCondition, Email email) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String[] columnArr;
		String[] reportHeaders = PropertyHandler.getInstance().getValue(serviceName + "_teleCallingHeaders").split(",");
		Sheet sheet = null;
		int rowNum = 1;
		Workbook workbook = null;
		CreationHelper createHelper = null;
		Font headerFont = null;
		CellStyle headerCellStyle = null;
		Row headerRow = null;
		Map<String, String> decryptedMobileNumber = new HashMap<>();
		String tableName = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_tablename");
		String dbCondition = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_dbcondition");
		Connection con = null;
		try {
			con = this.getConnection();
			// Create a Workbook
			workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file
			/*
			 * CreationHelper helps us create instances of various things like DataFormat,
			 * Hyperlink, RichTextString etc, in a format (HSSF, XSSF) independent way
			 */
			createHelper = workbook.getCreationHelper();
			// Create a Sheet
			sheet = workbook.createSheet("teleCallingData");
			// Create a Font for styling header cells
			headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 14);
			headerFont.setColor(IndexedColors.RED.getIndex());
			// Create a CellStyle with the font
			headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setFont(headerFont);
			// Create a Row
			headerRow = sheet.createRow(0);
			// Create cells
			for (int j = 0; j < reportHeaders.length; j++) {
				Cell cell = headerRow.createCell(j);
				cell.setCellValue(reportHeaders[j]);
				cell.setCellStyle(headerCellStyle);
			}
			if (queryCondition.equals("1=1")) {
				query = "select " + queryColumns.replace("@", ",") + " from " + tableName + " where " + dateCondition
						+ " " + dbCondition;
			} else {
				query = "select " + queryColumns.replace("@", ",") + " from " + tableName + " where " + queryCondition
						+ " " + dbCondition;
			}
			logger.info(query);
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				Row row = sheet.createRow(rowNum++);
				columnArr = queryColumns.split("@");
				for (int i = 0; i < columnArr.length; i++) {
					// logger.info("Column Value="+columnArr[i]);
					if (columnArr[i].indexOf(" as ") == -1) {
						if (columnArr[i].indexOf('.') != -1) {
							if (columnArr[i].split("\\.")[1].equalsIgnoreCase("mobile_number")
									|| columnArr[i].split("\\.")[1].equalsIgnoreCase("MobileNumber")) {
								logger.info("Column Value=" + columnArr[i] + "-->"
										+ rs.getString(columnArr[i].split("\\.")[1]));
								if (decryptedMobileNumber.getOrDefault(rs.getString(columnArr[i].split("\\.")[1]), "NA")
										.equals("NA")) {
									decryptedMobileNumber.put(rs.getString(columnArr[i].split("\\.")[1]),
											Utility.decrypt(rs.getString(columnArr[i].split("\\.")[1]), EncryptionValues
													.valueOf(serviceName.toUpperCase() + Constants.SECRET).getValue(),
													EncryptionValues.valueOf(serviceName.toUpperCase() + "SALT")
															.getValue()));
									row.createCell(i).setCellValue(
											decryptedMobileNumber.get(rs.getString(columnArr[i].split("\\.")[1])));
								} else {
									row.createCell(i).setCellValue(
											decryptedMobileNumber.get(rs.getString(columnArr[i].split("\\.")[1])));
								}
							} else {
								row.createCell(i).setCellValue(rs.getString(columnArr[i].split("\\.")[1]));
							}
						} else {
							row.createCell(i).setCellValue(rs.getString(columnArr[i]));
						}
					} else {
						row.createCell(i).setCellValue(rs.getString(columnArr[i].split(" as ")[1]));
					}
				}
			}
			logger.info(email.getFilePath() + email.getFileName());
			for (int j = 0; j < reportHeaders.length; j++) {
				sheet.autoSizeColumn(j);
			}
			// Write the output to a file
			FileOutputStream fileOut = new FileOutputStream(
					email.getFilePath() + email.getFileName().replace("%SERVICENAME", serviceName));
			workbook.write(fileOut);
			fileOut.close();
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception ex) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + ex + Arrays.asList(ex.getStackTrace())
						.stream().map(Objects::toString).collect(Collectors.joining("\n")));
			}
		}
	}

	public void fetchTeleCallingData(String queryCondition, String serviceName, String category, String queryColumns,
			String dateCondition, Email email) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		List<String> panelRecords = new ArrayList<>();
		String[] columnArr;
		String[] reportHeaders = PropertyHandler.getInstance().getValue(serviceName + "_teleCallingHeaders").split(",");
		Map<String, String> decryptedMobileNumber = new HashMap<>();
		String tableName = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_tablename");
		String dbCondition = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_dbcondition");
		Connection con = null;
		CSVWriter writer = null;
		try {
			writer = new CSVWriter(new FileWriter(email.getFilePath() + email.getFileName().replace("%SERVICENAME",
					null == PropertyHandler.getInstance().getValue(serviceName + "_servicename") ? serviceName
							: PropertyHandler.getInstance().getValue(serviceName + "_servicename").replace(" ", "")
									.toLowerCase())));
			writer.writeNext(reportHeaders);
			con = this.getConnection();
			if (queryCondition.equals("1=1")) {
				query = "select " + queryColumns.replace("@", ",") + " from " + tableName + " where " + dateCondition
						+ " " + dbCondition;
			} else {
				query = "select " + queryColumns.replace("@", ",") + " from " + tableName + " where " + queryCondition
						+ " " + dbCondition;
			}
			logger.info(query);
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				columnArr = queryColumns.split("@");
				for (int i = 0; i < columnArr.length; i++) {
					// logger.info("Column Value="+columnArr[i]);
					if (columnArr[i].indexOf(" as ") == -1) {
						if (columnArr[i].indexOf('.') != -1) {
							if (columnArr[i].split("\\.")[1].equalsIgnoreCase("mobile_number")
									|| columnArr[i].split("\\.")[1].equalsIgnoreCase("MobileNumber")) {
								logger.info("Column Value=" + columnArr[i] + "-->"
										+ rs.getString(columnArr[i].split("\\.")[1]));
								if (decryptedMobileNumber.getOrDefault(rs.getString(columnArr[i].split("\\.")[1]), "NA")
										.equals("NA")) {
									decryptedMobileNumber.put(rs.getString(columnArr[i].split("\\.")[1]),
											Utility.decrypt(rs.getString(columnArr[i].split("\\.")[1]), EncryptionValues
													.valueOf(serviceName.toUpperCase() + Constants.SECRET).getValue(),
													EncryptionValues.valueOf(serviceName.toUpperCase() + "SALT")
															.getValue()));
									panelRecords
											.add(decryptedMobileNumber.get(rs.getString(columnArr[i].split("\\.")[1])));
								} else {
									panelRecords
											.add(decryptedMobileNumber.get(rs.getString(columnArr[i].split("\\.")[1])));
								}
							} else {
								panelRecords.add(rs.getString(columnArr[i].split("\\.")[1]));
							}
						} else {
							panelRecords.add(rs.getString(columnArr[i]));
						}
					} else {
						panelRecords.add(rs.getString(columnArr[i].split(" as ")[1]));
					}
				}
				writer.writeNext(panelRecords.toArray(new String[panelRecords.size()]));
				panelRecords.clear();
			}
			writer.flush();
			writer.close();
			logger.info(email.getFilePath() + email.getFileName());
			Utility.zipFile(email.getFilePath(), email.getFileName().replace("%SERVICENAME", serviceName));
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception ex) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + ex + Arrays.asList(ex.getStackTrace())
						.stream().map(Objects::toString).collect(Collectors.joining("\n")));
			}
		}
	}

	public String fetchTeleCallingDataCsv(String queryCondition, String serviceName, String category,
			String queryColumns, String dateCondition, Email email) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String[] columnArr;
		String[] reportHeaders = PropertyHandler.getInstance().getValue(serviceName + "_teleCallingHeaders").split(",");
		Sheet sheet = null;
		int rowNum = 1;
		// Workbook workbook = null;
		CreationHelper createHelper = null;
		Font headerFont = null;
		CellStyle headerCellStyle = null;
		Row headerRow = null;
		Map<String, String> decryptedMobileNumber = new HashMap<>();
		String tableName = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_tablename");
		String dbCondition = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_dbcondition");
		Connection con = null;
		String password = null;
		int passwordLength = Integer.parseInt(PropertyHandler.getInstance().getValue("passwordlength"));
		try {
			con = this.getConnection();
			password = serviceName + Utility.generateRandomPassword(passwordLength).toString();
//			if (Biff8EncryptionKey.getCurrentUserPassword() != null)
//				logger.info("get passwword ye ha " + Biff8EncryptionKey.getCurrentUserPassword());
//			Biff8EncryptionKey.setCurrentUserPassword(password);
			XSSFWorkbook workbook = new XSSFWorkbook();
			createHelper = workbook.getCreationHelper();
			sheet = workbook.createSheet("teleCallingData");
			headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 14);
			headerFont.setColor(IndexedColors.RED.getIndex());
			headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setFont(headerFont);
			headerRow = sheet.createRow(0);
			for (int j = 0; j < reportHeaders.length; j++) {
				Cell cell = headerRow.createCell(j);
				cell.setCellValue(reportHeaders[j]);
				cell.setCellStyle(headerCellStyle);
			}
			if (queryCondition.equals("1=1")) {
				query = "select " + queryColumns.replace("@", ",") + " from " + tableName + " where " + dateCondition
						+ " " + dbCondition;
			} else {
				query = "select " + queryColumns.replace("@", ",") + " from " + tableName + " where " + queryCondition
						+ " " + dbCondition;
			}
			logger.info(query);
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				Row row = sheet.createRow(rowNum++);
				columnArr = queryColumns.split("@");
				for (int i = 0; i < columnArr.length; i++) {
					if (columnArr[i].indexOf(" as ") == -1) {
						if (columnArr[i].indexOf('.') != -1) {
							if (columnArr[i].split("\\.")[1].equalsIgnoreCase("mobile_number")
									|| columnArr[i].split("\\.")[1].equalsIgnoreCase("MobileNumber")) {
								logger.info("Column Value=" + columnArr[i] + "-->"
										+ rs.getString(columnArr[i].split("\\.")[1]));
								if (decryptedMobileNumber.getOrDefault(rs.getString(columnArr[i].split("\\.")[1]), "NA")
										.equals("NA")) {
									decryptedMobileNumber.put(rs.getString(columnArr[i].split("\\.")[1]),
											Utility.decrypt(rs.getString(columnArr[i].split("\\.")[1]), EncryptionValues
													.valueOf(serviceName.toUpperCase() + Constants.SECRET).getValue(),
													EncryptionValues.valueOf(serviceName.toUpperCase() + "SALT")
															.getValue()));
									row.createCell(i).setCellValue(
											decryptedMobileNumber.get(rs.getString(columnArr[i].split("\\.")[1])));
								} else {
									row.createCell(i).setCellValue(
											decryptedMobileNumber.get(rs.getString(columnArr[i].split("\\.")[1])));
								}
							} else {
								row.createCell(i).setCellValue(rs.getString(columnArr[i].split("\\.")[1]));
							}
						} else {
							row.createCell(i).setCellValue(rs.getString(columnArr[i]));
						}
					} else {
						row.createCell(i).setCellValue(rs.getString(columnArr[i].split(" as ")[1]));
					}
				}
			}
			logger.info(email.getFilePath() + email.getFileName());
			for (int j = 0; j < reportHeaders.length; j++) {
				sheet.autoSizeColumn(j);
			}
			FileOutputStream fileOut = new FileOutputStream(
					email.getFilePath() + email.getFileName().replace("%SERVICENAME", serviceName));
			workbook.write(fileOut);
			fileOut.close();
			workbook.close();
			POIFSFileSystem fs = new POIFSFileSystem();
			EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
			Encryptor encryptor = info.getEncryptor();
			encryptor.confirmPassword(password);
			OPCPackage opc = OPCPackage.open(email.getFilePath() + email.getFileName(), PackageAccess.READ_WRITE);
			OutputStream os = encryptor.getDataStream(fs);
			opc.save(os);
			FileOutputStream fos = new FileOutputStream(email.getFilePath() + email.getFileName());
			fs.writeFilesystem(fos);
			Utility.zipFile(email.getFilePath(), email.getFileName().replace("%SERVICENAME", serviceName));
			logger.info(password);
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception ex) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + ex + Arrays.asList(ex.getStackTrace())
						.stream().map(Objects::toString).collect(Collectors.joining("\n")));
			}
		}
		return password;
	}

	public List<String> fetchRetailerData(String serviceName, String parameters) {
		String query = null;
		List<String> dbRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String[] paramarray;
		Connection con = null;
		try {
			con = this.getConnection();
			paramarray = parameters.split("-");
			switch (serviceName) {
			case "itrial":
				query = "select CONCAT(sd.site_name,'-',bd.branch_desc,'-',rrd.retailer_code) as paramName,CONCAT(sd.site_id,'-',bd.branch_code,'-',rrd.retailer_code) as paramId from itrial_v3._tbl_site_details sd inner join itrial_v3._tbl_branch_details bd on sd.site_id=bd.site_id inner join itrial_v3._tbl_retailer_registration_details rrd on (bd.branch_code=rrd.branch_code and bd.project_id=rrd.project_id) where sd.process_status=1 and bd.process_status=1 and rrd.process_status=1 and (sd.site_name like '%"
						+ parameters + "%' or bd.branch_desc like '%" + parameters + "%' or rrd.retailer_code like '%"
						+ parameters + "%')";
				break;
			case "pampers":
				query = "select CONCAT(sd.site_name,'-',rrd.tcid) as paramName,CONCAT(sd.site_id,'-',rrd.tcid) as paramId from pampers_application._tbl_site_master sd inner join pampers_application._tbl_retailer_details rrd on (sd.site_id=rrd.site_id) where sd.status=1 and rrd.status=1 and (sd.site_name like '%"
						+ parameters + "%' or rrd.tcid like '%" + parameters + "%')";
				break;
			case "pampersbandhan":
				query = "select CONCAT(sd.site_name,'-',rrd.tcid) as paramName,CONCAT(sd.site_id,'-',rrd.tcid) as paramId from pampers_application._tbl_site_master sd inner join pampers_application._tbl_retailer_details rrd on (sd.site_id=rrd.site_id) where sd.status=1 and rrd.status=1 and (sd.site_name like '%"
						+ parameters + "%' or rrd.tcid like '%" + parameters + "%')";
				break;
			case "pampersnew":
				query = "select CONCAT(sd.site_name,'-',rrd.tcid) as paramName,CONCAT(sd.site_id,'-',rrd.tcid) as paramId from pampers_application_v2._tbl_site_master sd inner join pampers_application_v2._tbl_retailer_details rrd on (sd.site_id=rrd.site_id) where sd.status=1 and rrd.status=1 and (sd.site_name like '%"
						+ parameters + "%' or rrd.tcid like '%" + parameters + "%')";
				break;
			case "pampersbandhannew":
				query = "select CONCAT(sd.site_name,'-',rrd.tcid) as paramName,CONCAT(sd.site_id,'-',rrd.tcid) as paramId from pampers_application_v2._tbl_site_master sd inner join pampers_application_v2._tbl_retailer_details rrd on (sd.site_id=rrd.site_id) where sd.status=1 and rrd.status=1 and (sd.site_name like '%"
						+ parameters + "%' or rrd.tcid like '%" + parameters + "%')";
				break;
			default:
				break;
			}
			logger.info(query);
			if (query != null && query.length() > 0) {
				pstmt = con.prepareStatement(query);
				rs = pstmt.executeQuery();
				while (rs.next()) {
					dbRecords.add(rs.getString("paramName"));
					dbRecords.add(rs.getString("paramId"));
				}
			}
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				paramarray = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return dbRecords;
	}

	private String getCircleOperator(String mobileNumberId, String mobileNumber, String type, Connection con) {
		String response = null;
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		try {
			query = "select " + type
					+ " from rsplapplication_new._tbl_rsplrequest_details where MobilenumberId=? and Action='rsplmcRequest' and "
					+ type + " not in ('NA') order by 1 desc limit 1";
			logger.info(query + "-->" + mobileNumberId);
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, mobileNumberId);
			rs = pstmt.executeQuery();
			boolean found = rs.next();
			if (found) {
				response = rs.getString(type);
			} else {
				query = "select " + type.toLowerCase()
						+ " from rsplapplication_new._tbl_circleoperator_master where mobileseries=?";
				logger.info(query + "-->" + mobileNumber.substring(0, 5));
				pstmt = con.prepareStatement(query);
				pstmt.setString(1, "91" + mobileNumber.substring(0, 5));
				rs = pstmt.executeQuery();
				found = rs.next();
				if (found) {
					response = rs.getString(type.toLowerCase());
				} else {
					response = "NA";
				}
			}
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
			} catch (Exception ex) {
				logger.error(query + "-->" + ex + Arrays.asList(ex.getStackTrace()).stream().map(Objects::toString)
						.collect(Collectors.joining("\n")));
			}
		}
		return response;
	}

	private static Sheet getNewSheet(String sheetName, Workbook workbook, String[] headers) {
		Sheet sheet = workbook.createSheet(sheetName);
		Font headerFont = null;
		CellStyle headerCellStyle = null;
		Row headerRow = null;
		// Create a Font for styling header cells
		headerFont = workbook.createFont();
		headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short) 14);
		headerFont.setColor(IndexedColors.RED.getIndex());
		// Create a CellStyle with the font
		headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFont(headerFont);
		// Create a Row
		headerRow = sheet.createRow(0);
		// Create cells
		for (int j = 0; j < headers.length; j++) {
			Cell cell = headerRow.createCell(j);
			cell.setCellValue(headers[j]);
			cell.setCellStyle(headerCellStyle);
		}
		return sheet;
	}

	public static String getValue(String str) {
		List<String> list = Arrays.asList(str.split(","));
		String str1 = list.get(list.size() - 1);
		return str1;
	}

	public static String getStatusCodeValue(String statusCode) {

		return PropertyHandler.getInstance()
				.getValue(PropertyHandler.getInstance().getValue(statusCode + "_" + "code") + "_responseMessage");

	}

	public static HashMap<String, Integer> getDetails(String tableName, String groupBy, String action,
			String responseCode, Connection con) {
		String query = null;
		if (action.equals("No") && tableName.equals("rsplapplication_new._tbl_rsplrequest_details tr")) {
			query = "select date(RequestReceivedTime) as date,count(*) as number from " + tableName + " " + groupBy;

		} else if (action != null && tableName.equals("rsplapplication_new._tbl_rsplrequest_details tr")) {
			query = "select date(RequestReceivedTime) as date,count(*) as number from " + tableName
					+ " where tr.Action='" + action + "' and ResponseCode = '" + responseCode + "' " + groupBy;

		} else if (tableName.equals("rsplapplication_new._tbl_validreferralcode_details tr")
				&& action.equals("referrerCount")) {
			query = "select date(CreateDate) as date,count(distinct(tr.ReferralMobileNumberId)) as number from "
					+ tableName + " " + groupBy;

		} else if (tableName.equals("rsplapplication_new._tbl_validreferralcode_details tr")
				&& action.equals("referryCount")) {
			query = "select date(CreateDate) as date,count(distinct(tr.MobileNumberId)) as number from " + tableName
					+ " " + groupBy;

		} else if (tableName.equals("rsplapplication_new._tbl_validreferralcode_details tr")) {

			query = "select date(CreateDate) as date,SUM(tr." + action + ") as number from " + tableName + " "
					+ groupBy;
		}

		LinkedHashMap<String, Integer> result = new LinkedHashMap<String, Integer>();
		PreparedStatement prestmt = null;
		ResultSet resultSet = null;
		try {
			logger.info(query);
			prestmt = con.prepareStatement(query);
			resultSet = prestmt.executeQuery();
			while (resultSet.next()) {
				result.put(resultSet.getString("date"), resultSet.getInt("number"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				query = null;
				if (prestmt != null) {
					prestmt.close();
					prestmt = null;
				}
				if (resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
			} catch (Exception ex) {
				logger.error(query + "-->" + ex + Arrays.asList(ex.getStackTrace()).stream().map(Objects::toString)
						.collect(Collectors.joining("\n")));
			}
		}
		return result;
	}

	public String fetchMobileNumber(String serviceName, String mobileNumberId) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		String mobileNumber = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(serviceName + "_fetch_mobilenumber");
			logger.info(serviceName + "-->" + query + "-->" + mobileNumberId);
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, mobileNumberId);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				mobileNumber = rs.getString("mobile_number");
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception ex) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + ex + Arrays.asList(ex.getStackTrace())
						.stream().map(Objects::toString).collect(Collectors.joining("\n")));
			}
		}
		return mobileNumber;
	}

	public String fetchMobileNumber(int mobileNumberId) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		String mobileNumber = null;
		try {
			con = this.getConnection();
			query = "select mobile_number from api_application._tbl_mobilenumber_details where mobilenumber_id=? and status=1";
			logger.info(query);
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, Integer.toString(mobileNumberId));
			rs = pstmt.executeQuery();
			while (rs.next()) {
				mobileNumber = Utility.decrypt(rs.getString("mobile_number"), "sm$GeN@06072020", "sMs@ppl!)%)620@)");
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception ex) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + ex + Arrays.asList(ex.getStackTrace())
						.stream().map(Objects::toString).collect(Collectors.joining("\n")));
			}
		}
		return mobileNumber;
	}

	public void enableServices(String category, String serviceName, int uniqueId, String userName) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(serviceName.toLowerCase() + "_" + category + "_enable_query");
			logger.info("Enable Service Query-->" + query + "-->" + uniqueId);
			pstmt = con.prepareStatement(query);
			if (category.equalsIgnoreCase("servicemaster")) {
				pstmt.setString(1, userName);
				pstmt.setInt(2, uniqueId);
			} else {
				pstmt.setInt(1, uniqueId);
			}
			pstmt.executeUpdate();
			if (category.equalsIgnoreCase("servicemaster")) {
				ServiceRepository.reLoadRepository();
			} else {
				reloadConfiguration(serviceName);
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void registerServices(String category, String serviceName, int uniqueId) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_register_query");
			logger.info("Register Service Query-->" + query + "-->" + uniqueId);
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, uniqueId);
			pstmt.executeUpdate();
			reloadConfiguration(serviceName);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void disableServices(String category, String serviceName, int uniqueId, String userName) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(serviceName.toLowerCase() + "_" + category + "_disable_query");
			logger.info("Disable Service Query-->" + query + "-->" + uniqueId + " ---->>> " + userName);
			pstmt = con.prepareStatement(query);
			if (category.equalsIgnoreCase("servicemaster")) {
				pstmt.setString(1, userName);
				pstmt.setInt(2, uniqueId);
			} else {
				pstmt.setInt(1, uniqueId);
			}
			pstmt.executeUpdate();
			if (category.equalsIgnoreCase("servicemaster")) {
				ServiceRepository.reLoadRepository();
			} else {
				reloadConfiguration(serviceName);
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void editTcid(String serviceName, String mobileNumber, int uniqueId, String projectId,
			String offerDescription, String siteId, String trialsAllowed) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		int mobileNumberId = 0;
		try {
			mobileNumberId = fetchMobileNumberUniqueId(serviceName, mobileNumber);
			offerDescription = offerDescription.toLowerCase().replaceAll(" +", "").replaceAll("[^a-zA-Z0-9]", "@");
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(serviceName + "_update_tcid");
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, mobileNumberId);
			logger.info("Tcid Update Query for " + serviceName + "-->" + query + "-->" + mobileNumber + "-->"
					+ mobileNumberId + "-->" + uniqueId);
			if (serviceName.equals("pampersnew")) {
				pstmt.setInt(2, Integer.parseInt(projectId));
				pstmt.setString(3, offerDescription);
				pstmt.setInt(4, uniqueId);
			} else {
				pstmt.setString(2, offerDescription);
				pstmt.setInt(3, Integer.parseInt(trialsAllowed));
				pstmt.setInt(4, uniqueId);
			}
			pstmt.executeUpdate();
			addBrandConfiguration(serviceName, projectId, siteId, offerDescription);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	private void addBrandConfiguration(String serviceName, String projectId, String siteId, String offerDescription) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		try {
			con = this.getConnection();
			if (serviceName.equalsIgnoreCase("cp") || serviceName.equalsIgnoreCase("socp")
					|| serviceName.equalsIgnoreCase("venussolo") || serviceName.equalsIgnoreCase("olaysolo")
					|| serviceName.equalsIgnoreCase("multibrandsampling") || serviceName.equalsIgnoreCase("travelkit")
					|| serviceName.equalsIgnoreCase("hth")) {
				query = PropertyHandler.getInstance().getValue(serviceName + "_deactivate_offer");
				logger.info("Deactivate Offer Query-->" + query + "-->" + projectId);
				pstmt = con.prepareStatement(query);
				pstmt.setInt(1, Integer.parseInt(projectId));
				pstmt.executeUpdate();
				String[] offerNameArr = offerDescription.split("@");
				for (int i = 0; i < offerNameArr.length; i++) {
					query = PropertyHandler.getInstance().getValue(serviceName + "_add_brand_offer_insert_query");
					logger.info("Add New Offer Query-->" + query + "-->" + (i + 1) + "-->" + offerNameArr[i] + "-->"
							+ siteId + "-->" + projectId);
					pstmt = con.prepareStatement(query);
					pstmt.setInt(1, (i + 1));
					pstmt.setString(2, offerNameArr[i]);
					pstmt.setInt(3, Integer.parseInt(siteId));
					pstmt.setInt(4, Integer.parseInt(projectId));
					// pstmt.setString(5, Utility.getFirstDateOfMonth().toString().replace("T", "
					// "));
					// pstmt.setString(6, Utility.getCurrentDatetime().toString().replace("T", "
					// "));
					pstmt.executeUpdate();
				}
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void editRetailer(String serviceName, String mobileNumber, int uniqueId) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(serviceName + "_update_retailer");
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, mobileNumber);
			logger.info("Retailer Update Query for " + serviceName + "-->" + query + "-->" + mobileNumber + "-->"
					+ uniqueId);
			pstmt.setInt(2, uniqueId);
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void editConfiguration(String serviceName, int flowId, int couponCodeId, int couponCodeSiteId,
			int trialsAllowed, int capDays, int didCappingStatus, int uniqueId) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(serviceName + "_update_serviceconfiguration");
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, flowId);
			pstmt.setInt(2, couponCodeId);
			pstmt.setInt(3, couponCodeSiteId);
			pstmt.setInt(4, trialsAllowed);
			pstmt.setInt(5, capDays);
			pstmt.setInt(6, didCappingStatus);
			pstmt.setInt(7, uniqueId);
			pstmt.executeUpdate();
			logger.info("Configuration Update Query for " + serviceName + "-->" + query + "-->" + flowId + "-->"
					+ couponCodeId + "-->" + couponCodeSiteId + "-->" + trialsAllowed + "-->" + capDays + "-->"
					+ uniqueId);
			reloadConfiguration(serviceName);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void reloadConfiguration(String serviceName) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		try {
			if (!serviceName.toLowerCase().startsWith("pampers")) {
				query = PropertyHandler.getInstance().getValue(serviceName + "_reload_serviceconfiguration");
				con = this.getConnection();
				pstmt = con.prepareStatement(query);
				pstmt.setString(1, serviceName);
				logger.info("Reload Update Query for " + serviceName + "-->" + query);
				pstmt.executeUpdate();
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void editDistributor(String serviceName, Distributor distributor) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(serviceName + "_update_distributor");
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, distributor.getIvrBranchCode());
			pstmt.setInt(2, distributor.getIvrDistributorCode());
			pstmt.setInt(3, distributor.getDistributorId());
			logger.info(
					"Retailer Update Query for " + serviceName + "-->" + query + "-->" + distributor.getIvrBranchCode()
							+ "-->" + distributor.getIvrDistributorCode() + "-->" + distributor.getDistributorId());
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void editSms(String serviceName, String smsText, int uniqueId) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(serviceName + "_update_smsText");
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, smsText);
			logger.info("Sms Update Query for " + serviceName + "-->" + query + "-->" + uniqueId);
			pstmt.setInt(2, uniqueId);
			pstmt.executeUpdate();
			reloadConfiguration(serviceName);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void editLanguage(String serviceName, String languageId, int uniqueId) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(serviceName + "_update_languageId");
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, languageId);
			logger.info(
					"Language Update Query for " + serviceName + "-->" + query + "-->" + languageId + "-->" + uniqueId);
			pstmt.setInt(2, uniqueId);
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void editEmail(String serviceName, String toEmailAddress, String ccEmailAddress, String bccEmailAddress,
			int uniqueId) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(serviceName + "_update_email");
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, toEmailAddress);
			pstmt.setString(2, ccEmailAddress);
			pstmt.setString(3, bccEmailAddress);
			pstmt.setInt(4, uniqueId);
			logger.info("Email Update Query for " + serviceName + "-->" + query + "-->" + toEmailAddress + "-->"
					+ ccEmailAddress + "-->" + bccEmailAddress + "-->" + uniqueId);
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void editPharmacy(String serviceName, String mobileNumber, int uniqueId, String tcid, String siteId) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		int mobileNumberId = 0;
		try {
			mobileNumberId = fetchMobileNumberUniqueId(serviceName, mobileNumber);
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(serviceName + "_update_query");
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, mobileNumberId);
			pstmt.setInt(2, Integer.parseInt(tcid));
			pstmt.setInt(3, Integer.parseInt(siteId));
			pstmt.setInt(4, uniqueId);
			logger.info("Pharmacy Update Query for " + serviceName + "-->" + query + "-->" + mobileNumber + "-->"
					+ mobileNumberId + "-->" + uniqueId + "-->" + tcid + "-->" + siteId);
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public static boolean isAlphaNumeric(String str) {
		Matcher m = null;
		try {
			String regex = "^(?=.*[a-zA-Z])(?=.*[0-9])[A-Za-z0-9]+$";
			Pattern p = Pattern.compile(regex);
			if (str == null) {
				return false;
			}
			m = p.matcher(str);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
			return false;
		}
		return m.matches();
	}

	public String popDataReports(String queryCondition, String serviceName, String category, String queryColumns,
			String dateCondition, Email email) {
		String query = null;
		List<String> dbRecords = new ArrayList<>();
		List<String> panelRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int totalCalls = 0;
		int totalAnsweredCalls = 0;
		int totalUnansweredCalls = 0;
		int totalTrials = 0;
		int totalUniqueTrials = 0;
		int totalOptin = 0;
		int totalAnsweredCallsRoundtwo = 0;
		int totalCallsRoundtwodaytwo = 0;
		int totalUniqueStoreId = 0;
		int totalSmsTrials = 0;
		int totalUniqueCalls = 0;
		int totalRegret = 0;
		int totalExpired = 0;
		int totalPharmacyStores = 0;
		int totalSmsDelivery = 0;
		int totalTtsDelivery = 0;
		int totalBrand1 = 0;
		int totalBrand2 = 0;
		int totalBrand3 = 0;
		int totalBrand4 = 0;
		int totalBrand5 = 0;
		int totalBrand6 = 0;
		int totalBrand7 = 0;
		int totalBrand8 = 0;
		int totalBrand9 = 0;
		String[] columnArr;
		String tableName = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_tablename");
		String dbCondition = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_dbcondition");
		String[] reportHeaders = PropertyHandler.getInstance().getValue(serviceName + "_popHeaders").split(",");
		String[] summaryHeaders = PropertyHandler.getInstance().getValue(serviceName + "_popSummaryHeaders").split(",");
		String htmlData = "<html> <head> <style> table, th, td {   border: 1px solid black;   border-collapse: collapse; } th, td {   padding: 15px;   text-align: left; } table#t01 {   width: 100%;      background-color: #f1f1c1; } </style> </head> <body> <br><br><table width=\"100%\" cellspacing=\"1\" cellspadding=\"1\" border=\"1\"><tr>";
		Connection con = null;
		int dataCounter = 0;
		CSVWriter writer = null;
		Map<String, Integer> totalStores = new HashMap<>();
		try {
			totalStores = getTotalPharmacyStores();
			writer = new CSVWriter(new FileWriter(email.getFilePath() + email.getFileName().replace("%SERVICENAME",
					null == PropertyHandler.getInstance().getValue(serviceName + "_servicename") ? serviceName
							: PropertyHandler.getInstance().getValue(serviceName + "_servicename").replace(" ", "")
									.toLowerCase())));
			writer.writeNext(reportHeaders);
			con = this.getConnection();
			if (queryCondition.equals("1=1")) {
				query = "select " + queryColumns.replace("@", ",") + " from " + tableName + " where " + dateCondition
						+ " " + dbCondition;
			} else {
				query = "select " + queryColumns.replace("@", ",") + " from " + tableName + " where " + queryCondition
						+ " " + dbCondition;
			}
			logger.info(query);
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dataCounter = dataCounter + 1;
				columnArr = queryColumns.split("@");
				for (int i = 0; i < columnArr.length; i++) {
					if (columnArr[i].indexOf(" as ") == -1) {
						if (columnArr[i].indexOf("\\.") != -1) {
							panelRecords.add(rs.getString(columnArr[i].split("\\.")[1]));
						} else {
							panelRecords.add(rs.getString(columnArr[i]));
						}
					} else {
						panelRecords.add(rs.getString(columnArr[i].split(" as ")[1]));
					}
				}
				writer.writeNext(panelRecords.toArray(new String[panelRecords.size()]));
				panelRecords.clear();
			}
			writer.flush();
			writer.close();
			if (serviceName.equalsIgnoreCase("whisperblitzsms") || serviceName.equalsIgnoreCase("whispersampling")) {
				Utility.zipFile(email.getFilePath(), email.getFileName().replace("%SERVICENAME", PropertyHandler
						.getInstance().getValue("whisperblitzsms_servicename").replace(" ", "").toLowerCase()));
			} else {
				Utility.zipFile(email.getFilePath(), email.getFileName().replace("%SERVICENAME", serviceName));
			}
			logger.debug(dbRecords);
			for (int i = 0; i < summaryHeaders.length; i++) {
				htmlData += "<th>" + summaryHeaders[i] + "</th>";
			}
			if (serviceName.equals("pampersbandhan") || serviceName.equals("pampersbandhannew")
					|| serviceName.equals("wsitrial") || serviceName.equals("gillettepome")) {
				tableName = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_summarytablename");
				dateCondition = PropertyHandler.getInstance()
						.getValue(serviceName + "_" + category + "_summarydatecondition");
				queryCondition = queryCondition.replace("coupon_redemptionDate", "createDate");
			}
			dbCondition = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_summarydbcondition");
			queryColumns = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_summarycolumns");
			if (queryCondition.equals("1=1")) {
				query = "select " + queryColumns.replace("@", ",") + " from " + tableName + " where " + dateCondition
						+ " " + dbCondition;
			} else {
				query = "select " + queryColumns.replace("@", ",") + " from " + tableName + " where " + queryCondition
						+ " " + dbCondition;
			}
			pstmt = con.prepareStatement(query);
			logger.info("Summary Query-->" + query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				htmlData += "<tr>";
				if (serviceName.equals("pampersbandhan") || serviceName.equals("pampersbandhannew")) {
					totalCalls += rs.getInt("totalRedemptionRequest");
					totalUnansweredCalls += rs.getInt("totalValidRequest");
					totalTrials += rs.getInt("totalInvalidRequest");
					totalUniqueTrials += rs.getInt("totalStatusRequest");
					totalUniqueStoreId += rs.getInt("totalUniqueStoreId");
					totalPharmacyStores += totalStores.getOrDefault(rs.getString("site_name"), 0);
					htmlData += "<td>" + rs.getString("site_name") + "</td><td>" + rs.getString("brandName")
							+ "</td><td>" + rs.getInt("totalRedemptionRequest") + "</td><td>"
							+ rs.getInt("totalValidRequest") + "</td><td>" + rs.getInt("totalInvalidRequest")
							+ "</td><td>" + rs.getInt("totalStatusRequest") + "</td><td>"
							+ rs.getInt("totalUniqueStoreId") + "</td><td>"
							+ totalStores.getOrDefault(rs.getString("site_name"), 0) + "</td>";
				} else if (serviceName.equals("wheels")) {
					totalCalls += rs.getInt("totalCalls");
					totalTrials += rs.getInt("totalTrials");
					totalUniqueTrials += rs.getInt("totalUniqueTrials");
					totalOptin += rs.getInt("totalOptIn");
					htmlData += "<td>" + rs.getString("site_name") + "</td><td>" + rs.getString("CallerId")
							+ "</td><td>" + rs.getString("brandName") + "</td><td>" + rs.getInt("totalCalls")
							+ "</td><td>" + rs.getInt("totalTrials") + "</td><td>" + rs.getInt("totalUniqueTrials")
							+ "</td><td>" + rs.getInt("totalOptin") + "</td>";
				} else if (serviceName.equals("pampersengagement")) {
					totalCalls += rs.getInt("total_calls_roundone");
					totalTrials += rs.getInt("total_answered_calls_roundone");
					totalUniqueTrials += rs.getInt("total_calls_roundonedaytwo");
					totalOptin += rs.getInt("total_calls_roundtwo");
					totalAnsweredCallsRoundtwo += rs.getInt("total_answered_calls_roundtwo");
					totalCallsRoundtwodaytwo += rs.getInt("total_calls_roundtwodaytwo");
					htmlData += "<td>" + rs.getString("brandName") + "</td><td>" + rs.getString("EngagementDate")
							+ "</td><td>" + rs.getString("SiteName") + "</td><td>" + rs.getInt("total_calls_roundone")
							+ "</td><td>" + rs.getInt("total_answered_calls_roundone") + "</td><td>"
							+ rs.getInt("total_calls_roundonedaytwo") + "</td><td>" + rs.getInt("total_calls_roundtwo")
							+ "</td><td>" + rs.getInt("total_answered_calls_roundtwo") + "</td><td>"
							+ rs.getInt("total_calls_roundtwodaytwo") + "</td>";
				} else if (serviceName.equals("wsitrial")) {
					totalCalls += rs.getInt("total_registered_distributor");
					totalTrials += rs.getInt("total_active_distributor");
					totalUniqueTrials += rs.getInt("total_calls_trials");
					totalOptin += rs.getInt("unique_calls_trials");
					totalAnsweredCallsRoundtwo += rs.getInt("total_optin");
					htmlData += "<td>" + rs.getString("BranchName") + "</td><td>" + rs.getString("CallerId")
							+ "</td><td>" + rs.getInt("total_registered_distributor") + "</td><td>"
							+ rs.getInt("total_active_distributor") + "</td><td>" + rs.getInt("total_calls_trials")
							+ "</td><td>" + rs.getInt("unique_calls_trials") + "</td><td>" + rs.getInt("total_optin")
							+ "</td>";
				} else if (serviceName.equals("pamperspremiumcare") || serviceName.equals("whisperkgis")) {
					totalCalls += rs.getInt("totalCalls");
					totalTrials += rs.getInt("totalTrials");
					totalRegret += rs.getInt("totalRegret");
					totalExpired += rs.getInt("totalExpired");
					htmlData += "<td>" + rs.getString("CreateDate") + "</td><td>" + rs.getString("CallerId")
							+ "</td><td>" + rs.getString("brandName") + "</td><td>" + rs.getInt("totalCalls")
							+ "</td><td>" + rs.getInt("totalTrials") + "</td><td>" + rs.getInt("totalRegret")
							+ "</td><td>" + rs.getInt("totalExpired") + "</td>";
				} else if (serviceName.equals("pampersoffline") || serviceName.equals("pampersapi")) {
					totalCalls += rs.getInt("totalCalls");
					totalTrials += rs.getInt("totalTrials");
					totalUniqueTrials += rs.getInt("totalUniqueTrials");
					totalOptin += rs.getInt("totalOptIn");
					htmlData += "<td>" + rs.getString("site_name") + "</td><td>" + rs.getString("CallerId")
							+ "</td><td>" + rs.getString("brandName") + "</td><td>" + rs.getInt("totalCalls")
							+ "</td><td>" + rs.getInt("totalTrials") + "</td><td>" + rs.getInt("totalOptIn") + "</td>";
				} else {
					totalCalls += rs.getInt("totalCalls");
					totalAnsweredCalls += rs.getInt("totalAnsweredCalls");
					totalUnansweredCalls += rs.getInt("totalUnAnsweredCalls");
					totalTrials += rs.getInt("totalTrials");
					totalUniqueTrials += rs.getInt("totalUniqueTrials");
					totalOptin += rs.getInt("totalOptIn");
					System.out.println("servicename is ================================== "+serviceName);
					if (!serviceName.startsWith("pampers")) {
						totalRegret += rs.getInt("totalRegret");
						if (serviceName.equalsIgnoreCase("gillettepome")) {
							totalUniqueCalls += rs.getInt("totalUniqueCalls");
							htmlData += "<td>" + rs.getString("site_name") + "</td><td>" + rs.getString("CallerId")
									+ "</td><td>" + rs.getString("brandName") + "</td><td>" + rs.getInt("totalCalls")
									+ "</td><td>" + rs.getInt("totalUniqueCalls") + "</td><td>"
									+ rs.getInt("totalAnsweredCalls") + "</td><td>" + rs.getInt("totalUnAnsweredCalls")
									+ "</td><td>" + rs.getInt("totalRegret") + "</td><td>" + rs.getInt("totalTrials")
									+ "</td><td>" + rs.getInt("totalUniqueTrials") + "</td><td>"
									+ rs.getInt("totalOptIn") + "</td>";
						} else if (serviceName.equalsIgnoreCase("cp") || serviceName.equalsIgnoreCase("socp")
								|| serviceName.equalsIgnoreCase("hth")
								|| serviceName.equalsIgnoreCase("multibrandsampling")
								|| serviceName.equalsIgnoreCase("venussolo") || serviceName.equalsIgnoreCase("olaysolo")
								|| serviceName.equalsIgnoreCase("travelkit") || serviceName.equalsIgnoreCase("rurban") || serviceName.equalsIgnoreCase("pilot")) {
							System.out.println("servicename is ----->>>>> "+serviceName);
							totalUniqueCalls += rs.getInt("totalUniqueCalls");
							totalBrand1 += rs.getInt("brand1");
							totalBrand2 += rs.getInt("brand2");
							totalBrand3 += rs.getInt("brand3");
							totalBrand4 += rs.getInt("brand4");
							totalBrand5 += rs.getInt("brand5");
							totalBrand6 += rs.getInt("brand6");
							totalBrand7 += rs.getInt("brand7");
							totalBrand8 += rs.getInt("brand8");
							totalBrand9 += rs.getInt("brand9");
							htmlData += "<td>" + rs.getString("site_name") + "</td><td>" + rs.getString("CallerId")
									+ "</td><td>" + rs.getString("brand1") + "</td><td>" + rs.getString("brand2")
									+ "</td><td>" + rs.getString("brand3") + "</td><td>" + rs.getString("brand4")
									+ "</td><td>" + rs.getString("brand5") + "</td><td>" + rs.getString("brand6")
									+ "</td><td>" + rs.getString("brand7") + "</td><td>" + rs.getString("brand8")
									+ "</td><td>" + rs.getString("brand9") + "</td><td>" + rs.getInt("totalCalls")
									+ "</td><td>" + rs.getInt("totalUniqueCalls") + "</td><td>"
									+ rs.getInt("totalAnsweredCalls") + "</td><td>" + rs.getInt("totalUnAnsweredCalls")
									+ "</td><td>" + rs.getInt("totalTrials") + "</td><td>"
									+ rs.getInt("totalUniqueTrials") + "</td><td>" + rs.getInt("totalRegret")
									+ "</td><td>" + rs.getInt("totalOptIn") + "</td>";
						} else if (serviceName.equalsIgnoreCase("cpoffline")
								|| serviceName.equalsIgnoreCase("socpoffline")
								|| serviceName.equalsIgnoreCase("hthoffline")
								|| serviceName.equalsIgnoreCase("multibrandsamplingoffline")
								|| serviceName.equalsIgnoreCase("venussolooffline")
								|| serviceName.equalsIgnoreCase("olaysolooffline")
								|| serviceName.equalsIgnoreCase("rurbanoffline")) {
							totalBrand1 += rs.getInt("brand1");
							totalBrand2 += rs.getInt("brand2");
							totalBrand3 += rs.getInt("brand3");
							totalBrand4 += rs.getInt("brand4");
							totalBrand5 += rs.getInt("brand5");
							totalBrand6 += rs.getInt("brand6");
							totalBrand7 += rs.getInt("brand7");
							totalBrand8 += rs.getInt("brand8");
							totalBrand9 += rs.getInt("brand9");
							htmlData += "<td>" + rs.getString("site_name") + "</td><td>" + rs.getString("CallerId")
									+ "</td><td>" + rs.getString("brand1") + "</td><td>" + rs.getString("brand2")
									+ "</td><td>" + rs.getString("brand3") + "</td><td>" + rs.getString("brand4")
									+ "</td><td>" + rs.getString("brand5") + "</td><td>" + rs.getString("brand6")
									+ "</td><td>" + rs.getString("brand7") + "</td><td>" + rs.getString("brand8")
									+ "</td><td>" + rs.getString("brand9") + "</td><td>" + rs.getInt("totalCalls")
									+ "</td><td>" + rs.getInt("totalTrials") + "</td><td>"
									+ rs.getInt("totalUniqueTrials") + "</td><td>" + rs.getInt("totalRegret")
									+ "</td><td>" + rs.getInt("totalOptIn") + "</td>";
						} else {
							totalSmsDelivery += rs.getInt("TotalSmsDelivery");
							totalTtsDelivery += rs.getInt("TotalTtsDelivery");
							htmlData += "<td>" + rs.getString("site_name") + "</td><td>" + rs.getString("CallerId")
									+ "</td><td>" + rs.getString("brandName") + "</td><td>" + rs.getInt("totalCalls")
									+ "</td><td>" + rs.getInt("totalTrials") + "</td><td>"
									+ rs.getInt("totalUniqueTrials") + "</td><td>" + rs.getInt("totalRegret")
									+ "</td><td>" + rs.getInt("totalOptIn") + "</td><td>"
									+ rs.getInt("totalSmsDelivery") + "</td><td>" + rs.getInt("totalTtsDelivery")
									+ "</td>";
						}
					} else {
						// totalSmsTrials+=rs.getInt("totalSmsTrials");
						htmlData += "<td>" + rs.getString("site_name") + "</td><td>" + rs.getString("CallerId")
								+ "</td><td>" + rs.getString("brandName") + "</td><td>" + rs.getInt("totalCalls")
								+ "</td><td>" + rs.getInt("totalAnsweredCalls") + "</td><td>"
								+ rs.getInt("totalUnAnsweredCalls") + "</td><td>" + rs.getInt("totalTrials")
								+ "</td><td>" + rs.getInt("totalOptIn") + "</td>";
					}
				}
				htmlData += "</tr>";
			}
			if (serviceName.equals("pampersbandhan") || serviceName.equals("pampersbandhannew")) {
				htmlData += "<tr><td>Grand Total</td><td></td><td>" + totalCalls + "</td><td>" + totalUnansweredCalls
						+ "</td><td>" + totalTrials + "</td><td>" + totalUniqueTrials + "</td><td>" + totalUniqueStoreId
						+ "</td><td>" + totalPharmacyStores + "</td></tr></table></body></html>";
			} else if (serviceName.equals("wheels")) {
				htmlData += "<tr><td>Grand Total</td><td></td><td></td><td>" + totalCalls + "</td><td>" + totalTrials
						+ "</td><td>" + totalUniqueTrials + "</td><td>" + totalOptin
						+ "</td></tr></table></body></html>";
			} else if (serviceName.equals("pampersengagement")) {
				htmlData += "<tr><td>Grand Total</td><td></td><td></td><td>" + totalCalls + "</td><td>" + totalTrials
						+ "</td><td>" + totalUniqueTrials + "</td><td>" + totalOptin + "</td><td>"
						+ totalAnsweredCallsRoundtwo + "</td><td>" + totalCallsRoundtwodaytwo
						+ "</td></tr></table></body></html>";
			} else if (serviceName.equals("wsitrial")) {
				htmlData += "<tr><td>Grand Total</td><td>" + totalCalls + "</td><td>" + totalTrials + "</td><td>"
						+ totalUniqueTrials + "</td><td>" + totalOptin + "</td><td>" + totalAnsweredCallsRoundtwo
						+ "</td></tr></table></body></html>";
			} else if (serviceName.equals("pamperspremiumcare") || serviceName.equals("whisperkgis")) {
				htmlData += "<tr><td>Grand Total</td><td></td><td></td><td>" + totalCalls + "</td><td>" + totalTrials
						+ "</td><td>" + totalRegret + "</td><td>" + totalExpired + "</td></tr></table></body></html>";
			} else if (serviceName.equals("pampersoffline") || serviceName.equals("pampersapi")) {
				htmlData += "<tr><td>Grand Total</td><td></td><td></td><td>" + totalCalls + "</td><td>" + totalTrials
						+ "</td><td>" + totalOptin + "</td></tr></table></body></html>";
			} else {
				if (!serviceName.startsWith("pampers")) {
					if (serviceName.equalsIgnoreCase("gillettepome")) {
						htmlData += "<tr><td>Grand Total</td><td></td><td></td><td>" + totalCalls + "</td><td>"
								+ totalUniqueCalls + "</td><td>" + totalAnsweredCalls + "</td><td>"
								+ totalUnansweredCalls + "</td><td>" + totalTrials + "</td><td>" + totalUniqueTrials
								+ "</td><td>" + totalRegret + "</td><td>" + totalOptin
								+ "</td></tr></table></body></html>";
					} else if (serviceName.equalsIgnoreCase("cp") || serviceName.equalsIgnoreCase("socp")
							|| serviceName.equalsIgnoreCase("hth") || serviceName.equalsIgnoreCase("multibrandsampling")
							|| serviceName.equalsIgnoreCase("venussolo") || serviceName.equalsIgnoreCase("olaysolo")
							|| serviceName.equalsIgnoreCase("rurban") || serviceName.equalsIgnoreCase("travelkit") || serviceName.equalsIgnoreCase("pilot")) {
						htmlData += "<tr><td>Grand Total</td><td></td><td>" + totalBrand1 + "</td><td>" + totalBrand2
								+ "</td><td>" + totalBrand3 + "</td><td>" + totalBrand4 + "</td><td>" + totalBrand5
								+ "</td><td>" + totalBrand6 + "</td><td>" + totalBrand7 + "</td><td>" + totalBrand8
								+ "</td><td>" + totalBrand9 + "</td><td>" + totalCalls + "</td><td>" + totalUniqueCalls
								+ "</td><td>" + totalAnsweredCalls + "</td><td>" + totalUnansweredCalls + "</td><td>"
								+ totalTrials + "</td><td>" + totalUniqueTrials + "</td><td>" + totalRegret
								+ "</td><td>" + totalOptin + "</td></tr></table></body></html>";
					} else if (serviceName.equalsIgnoreCase("cpoffline") || serviceName.equalsIgnoreCase("socpoffline")
							|| serviceName.equalsIgnoreCase("hthoffline")
							|| serviceName.equalsIgnoreCase("multibrandsamplingoffline")
							|| serviceName.equalsIgnoreCase("venussolooffline")
							|| serviceName.equalsIgnoreCase("olaysolooffline")
							|| serviceName.equalsIgnoreCase("rurbanoffline")) {
						htmlData += "<tr><td>Grand Total</td><td></td><td>" + totalBrand1 + "</td><td>" + totalBrand2
								+ "</td><td>" + totalBrand3 + "</td><td>" + totalBrand4 + "</td><td>" + totalBrand5
								+ "</td><td>" + totalBrand6 + "</td><td>" + totalBrand7 + "</td><td>" + totalBrand8
								+ "</td><td>" + totalBrand9 + "</td><td>" + totalCalls + "</td><td>" + totalTrials
								+ "</td><td>" + totalUniqueTrials + "</td><td>" + totalRegret + "</td><td>" + totalOptin
								+ "</td></tr></table></body></html>";
					} else {
						htmlData += "<tr><td>Grand Total</td><td></td><td></td><td>" + totalCalls + "</td><td>"
								+ totalTrials + "</td><td>" + totalUniqueTrials + "</td><td>" + totalRegret
								+ "</td><td>" + totalOptin + "</td><td>" + totalSmsDelivery + "</td><td>"
								+ totalTtsDelivery + "</td></tr></table></body></html>";
					}
				} else {
					htmlData += "<tr><td>Grand Total</td><td></td><td></td><td>" + totalCalls + "</td><td>"
							+ totalAnsweredCalls + "</td><td>" + totalUnansweredCalls + "</td><td>" + totalTrials
							+ "</td><td>" + totalOptin + "</td></tr></table></body></html>";
				}
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				columnArr = null;
				dataCounter = 0;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return htmlData;
	}

	public String popDataReportsCsv(String queryCondition, String serviceName, String category, String queryColumns,
			String dateCondition, Email email) {
		String query = null;
		List<String> dbRecords = new ArrayList<>();
		List<String> panelRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		int totalCalls = 0;
		int totalAnsweredCalls = 0;
		int totalUnansweredCalls = 0;
		int totalTrials = 0;
		int totalUniqueTrials = 0;
		int totalOptin = 0;
		int totalAnsweredCallsRoundtwo = 0;
		int totalCallsRoundtwodaytwo = 0;
		int totalUniqueStoreId = 0;
		int totalSmsTrials = 0;
		int totalUniqueCalls = 0;
		int totalRegret = 0;
		int totalExpired = 0;
		int totalPharmacyStores = 0;
		int totalSmsDelivery = 0;
		int totalTtsDelivery = 0;
		int totalBrand1 = 0;
		int totalBrand2 = 0;
		int totalBrand3 = 0;
		int totalBrand4 = 0;
		int totalBrand5 = 0;
		int totalBrand6 = 0;
		int totalBrand7 = 0;
		int totalBrand8 = 0;
		int totalBrand9 = 0;
		String[] columnArr;
		String tableName = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_tablename");
		String dbCondition = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_dbcondition");
		String[] reportHeaders = PropertyHandler.getInstance().getValue(serviceName + "_popHeaders").split(",");
		String[] summaryHeaders = PropertyHandler.getInstance().getValue(serviceName + "_popSummaryHeaders").split(",");
		String htmlData = "<html> <head> <style> table, th, td {   border: 1px solid black;   border-collapse: collapse; } th, td {   padding: 15px;   text-align: left; } table#t01 {   width: 100%;      background-color: #f1f1c1; } </style> </head> <body> <br><br><table width=\"100%\" cellspacing=\"1\" cellspadding=\"1\" border=\"1\"><tr>";
		Connection con = null;
		int dataCounter = 0;
		CSVWriter writer = null;
		Map<String, Integer> totalStores = new HashMap<>();
		int passwordLength = Integer.parseInt(PropertyHandler.getInstance().getValue("passwordlength"));
		String password = null;
		CreationHelper createHelper = null;
		Font headerFont = null;
		CellStyle headerCellStyle = null;
		Row headerRow = null;
		Sheet sheet = null;
		int rowNum = 1;
		try {
			totalStores = getTotalPharmacyStores();
			con = this.getConnection();
			XSSFWorkbook workbook = new XSSFWorkbook(); // new HSSFWorkbook() for generating `.xls` file
			createHelper = workbook.getCreationHelper();
			sheet = workbook.createSheet("PopData");
			headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 14);
			headerFont.setColor(IndexedColors.RED.getIndex());
			headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setFont(headerFont);
			headerRow = sheet.createRow(0);
			logger.info("Header row is " + headerRow);
			for (int j = 0; j < reportHeaders.length; j++) {
				Cell cell = headerRow.createCell(j);
				cell.setCellValue(reportHeaders[j]);
				cell.setCellStyle(headerCellStyle);
			}
			if (queryCondition.equals("1=1")) {
				query = "select " + queryColumns.replace("@", ",") + " from " + tableName + " where " + dateCondition
						+ " " + dbCondition;
			} else {
				query = "select " + queryColumns.replace("@", ",") + " from " + tableName + " where " + queryCondition
						+ " " + dbCondition;
			}
			logger.info(query);
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				// dataCounter = dataCounter + 1;
				Row row = sheet.createRow(rowNum++);
				columnArr = queryColumns.split("@");
				for (int i = 0; i < columnArr.length; i++) {
					Cell cell = row.createCell(i);
					if (columnArr[i].indexOf(" as ") == -1) {
						if (columnArr[i].indexOf("\\.") != -1) {
							// panelRecords.add(rs.getString(columnArr[i].split("\\.")[1]));
							cell.setCellValue(rs.getString(columnArr[i].split("\\.")[1]));
						} else {
							// panelRecords.add(rs.getString(columnArr[i]));
							cell.setCellValue(rs.getString(columnArr[i]));
						}
					} else {
						// panelRecords.add(rs.getString(columnArr[i].split(" as ")[1]));
						cell.setCellValue(rs.getString(columnArr[i].split(" as ")[1]));
					}
				}
				// writer.writeNext(panelRecords.toArray(new String[panelRecords.size()]));
//				panelRecords.clear();
			}
//			writer.flush();
//			writer.close();
			logger.info("Email Data-->" + email.getFileName() + "-->" + email.getFilePath());
			FileOutputStream fileOut = new FileOutputStream(
					email.getFilePath() + email.getFileName().replace("%SERVICENAME", "NA"));
			panelRecords.clear();
			password = serviceName + Utility.generateRandomPassword(passwordLength).toString();

			// Biff8EncryptionKey.setCurrentUserPassword(password);
			workbook.write(fileOut);

			fileOut.close();
			workbook.close();
			POIFSFileSystem fs = new POIFSFileSystem();
			EncryptionInfo info = new EncryptionInfo(EncryptionMode.agile);
			Encryptor encryptor = info.getEncryptor();
			encryptor.confirmPassword(password);
			OPCPackage opc = OPCPackage.open(email.getFilePath() + email.getFileName(), PackageAccess.READ_WRITE);
			OutputStream os = encryptor.getDataStream(fs);
			opc.save(os);
			FileOutputStream fos = new FileOutputStream(email.getFilePath() + email.getFileName());
			fs.writeFilesystem(fos);
			if (serviceName.equalsIgnoreCase("whisperblitzsms") || serviceName.equalsIgnoreCase("whispersampling")) {
				Utility.zipFile(email.getFilePath(), email.getFileName().replace("%SERVICENAME", PropertyHandler
						.getInstance().getValue("whisperblitzsms_servicename").replace(" ", "").toLowerCase()));
			} else {
				Utility.zipFile(email.getFilePath(), email.getFileName().replace("%SERVICENAME", serviceName));
			}
			logger.debug(dbRecords);
			for (int i = 0; i < summaryHeaders.length; i++) {
				htmlData += "<th>" + summaryHeaders[i] + "</th>";
			}
			if (serviceName.equals("pampersbandhan") || serviceName.equals("pampersbandhannew")
					|| serviceName.equals("wsitrial") || serviceName.equals("gillettepome")) {
				tableName = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_summarytablename");
				dateCondition = PropertyHandler.getInstance()
						.getValue(serviceName + "_" + category + "_summarydatecondition");
				queryCondition = queryCondition.replace("coupon_redemptionDate", "createDate");
			}
			dbCondition = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_summarydbcondition");
			queryColumns = PropertyHandler.getInstance().getValue(serviceName + "_" + category + "_summarycolumns");
			if (queryCondition.equals("1=1")) {
				query = "select " + queryColumns.replace("@", ",") + " from " + tableName + " where " + dateCondition
						+ " " + dbCondition;
			} else {
				query = "select " + queryColumns.replace("@", ",") + " from " + tableName + " where " + queryCondition
						+ " " + dbCondition;
			}
			pstmt = con.prepareStatement(query);
			logger.info("Summary Query-->" + query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				htmlData += "<tr>";
				if (serviceName.equals("pampersbandhan") || serviceName.equals("pampersbandhannew")) {
					totalCalls += rs.getInt("totalRedemptionRequest");
					totalUnansweredCalls += rs.getInt("totalValidRequest");
					totalTrials += rs.getInt("totalInvalidRequest");
					totalUniqueTrials += rs.getInt("totalStatusRequest");
					totalUniqueStoreId += rs.getInt("totalUniqueStoreId");
					totalPharmacyStores += totalStores.getOrDefault(rs.getString("site_name"), 0);
					htmlData += "<td>" + rs.getString("site_name") + "</td><td>" + rs.getString("brandName")
							+ "</td><td>" + rs.getInt("totalRedemptionRequest") + "</td><td>"
							+ rs.getInt("totalValidRequest") + "</td><td>" + rs.getInt("totalInvalidRequest")
							+ "</td><td>" + rs.getInt("totalStatusRequest") + "</td><td>"
							+ rs.getInt("totalUniqueStoreId") + "</td><td>"
							+ totalStores.getOrDefault(rs.getString("site_name"), 0) + "</td>";
				} else if (serviceName.equals("wheels")) {
					totalCalls += rs.getInt("totalCalls");
					totalTrials += rs.getInt("totalTrials");
					totalUniqueTrials += rs.getInt("totalUniqueTrials");
					totalOptin += rs.getInt("totalOptIn");
					htmlData += "<td>" + rs.getString("site_name") + "</td><td>" + rs.getString("CallerId")
							+ "</td><td>" + rs.getString("brandName") + "</td><td>" + rs.getInt("totalCalls")
							+ "</td><td>" + rs.getInt("totalTrials") + "</td><td>" + rs.getInt("totalUniqueTrials")
							+ "</td><td>" + rs.getInt("totalOptin") + "</td>";
				} else if (serviceName.equals("pampersengagement")) {
					totalCalls += rs.getInt("total_calls_roundone");
					totalTrials += rs.getInt("total_answered_calls_roundone");
					totalUniqueTrials += rs.getInt("total_calls_roundonedaytwo");
					totalOptin += rs.getInt("total_calls_roundtwo");
					totalAnsweredCallsRoundtwo += rs.getInt("total_answered_calls_roundtwo");
					totalCallsRoundtwodaytwo += rs.getInt("total_calls_roundtwodaytwo");
					htmlData += "<td>" + rs.getString("brandName") + "</td><td>" + rs.getString("EngagementDate")
							+ "</td><td>" + rs.getString("SiteName") + "</td><td>" + rs.getInt("total_calls_roundone")
							+ "</td><td>" + rs.getInt("total_answered_calls_roundone") + "</td><td>"
							+ rs.getInt("total_calls_roundonedaytwo") + "</td><td>" + rs.getInt("total_calls_roundtwo")
							+ "</td><td>" + rs.getInt("total_answered_calls_roundtwo") + "</td><td>"
							+ rs.getInt("total_calls_roundtwodaytwo") + "</td>";
				} else if (serviceName.equals("wsitrial")) {
					totalCalls += rs.getInt("total_registered_distributor");
					totalTrials += rs.getInt("total_active_distributor");
					totalUniqueTrials += rs.getInt("total_calls_trials");
					totalOptin += rs.getInt("unique_calls_trials");
					totalAnsweredCallsRoundtwo += rs.getInt("total_optin");
					htmlData += "<td>" + rs.getString("BranchName") + "</td><td>" + rs.getString("CallerId")
							+ "</td><td>" + rs.getInt("total_registered_distributor") + "</td><td>"
							+ rs.getInt("total_active_distributor") + "</td><td>" + rs.getInt("total_calls_trials")
							+ "</td><td>" + rs.getInt("unique_calls_trials") + "</td><td>" + rs.getInt("total_optin")
							+ "</td>";
				} else if (serviceName.equals("pamperspremiumcare") || serviceName.equals("whisperkgis")) {
					totalCalls += rs.getInt("totalCalls");
					totalTrials += rs.getInt("totalTrials");
					totalRegret += rs.getInt("totalRegret");
					totalExpired += rs.getInt("totalExpired");
					htmlData += "<td>" + rs.getString("CreateDate") + "</td><td>" + rs.getString("CallerId")
							+ "</td><td>" + rs.getString("brandName") + "</td><td>" + rs.getInt("totalCalls")
							+ "</td><td>" + rs.getInt("totalTrials") + "</td><td>" + rs.getInt("totalRegret")
							+ "</td><td>" + rs.getInt("totalExpired") + "</td>";
				} else if (serviceName.equals("pampersoffline") || serviceName.equals("pampersapi")) {
					totalCalls += rs.getInt("totalCalls");
					totalTrials += rs.getInt("totalTrials");
					totalUniqueTrials += rs.getInt("totalUniqueTrials");
					totalOptin += rs.getInt("totalOptIn");
					htmlData += "<td>" + rs.getString("site_name") + "</td><td>" + rs.getString("CallerId")
							+ "</td><td>" + rs.getString("brandName") + "</td><td>" + rs.getInt("totalCalls")
							+ "</td><td>" + rs.getInt("totalTrials") + "</td><td>" + rs.getInt("totalOptIn") + "</td>";
				} else {
					totalCalls += rs.getInt("totalCalls");
					totalAnsweredCalls += rs.getInt("totalAnsweredCalls");
					totalUnansweredCalls += rs.getInt("totalUnAnsweredCalls");
					totalTrials += rs.getInt("totalTrials");
					totalUniqueTrials += rs.getInt("totalUniqueTrials");
					totalOptin += rs.getInt("totalOptIn");
					if (!serviceName.startsWith("pampers")) {
						totalRegret += rs.getInt("totalRegret");
						if (serviceName.equalsIgnoreCase("gillettepome")) {
							totalUniqueCalls += rs.getInt("totalUniqueCalls");
							htmlData += "<td>" + rs.getString("site_name") + "</td><td>" + rs.getString("CallerId")
									+ "</td><td>" + rs.getString("brandName") + "</td><td>" + rs.getInt("totalCalls")
									+ "</td><td>" + rs.getInt("totalUniqueCalls") + "</td><td>"
									+ rs.getInt("totalAnsweredCalls") + "</td><td>" + rs.getInt("totalUnAnsweredCalls")
									+ "</td><td>" + rs.getInt("totalRegret") + "</td><td>" + rs.getInt("totalTrials")
									+ "</td><td>" + rs.getInt("totalUniqueTrials") + "</td><td>"
									+ rs.getInt("totalOptIn") + "</td>";
						} else if (serviceName.equalsIgnoreCase("cp") || serviceName.equalsIgnoreCase("socp")
								|| serviceName.equalsIgnoreCase("hth")
								|| serviceName.equalsIgnoreCase("multibrandsampling")
								|| serviceName.equalsIgnoreCase("venussolo") || serviceName.equalsIgnoreCase("olaysolo")
								|| serviceName.equalsIgnoreCase("rurban")) {
							totalUniqueCalls += rs.getInt("totalUniqueCalls");
							totalBrand1 += rs.getInt("brand1");
							totalBrand2 += rs.getInt("brand2");
							totalBrand3 += rs.getInt("brand3");
							totalBrand4 += rs.getInt("brand4");
							totalBrand5 += rs.getInt("brand5");
							totalBrand6 += rs.getInt("brand6");
							totalBrand7 += rs.getInt("brand7");
							totalBrand8 += rs.getInt("brand8");
							totalBrand9 += rs.getInt("brand9");
							htmlData += "<td>" + rs.getString("site_name") + "</td><td>" + rs.getString("CallerId")
									+ "</td><td>" + rs.getString("brand1") + "</td><td>" + rs.getString("brand2")
									+ "</td><td>" + rs.getString("brand3") + "</td><td>" + rs.getString("brand4")
									+ "</td><td>" + rs.getString("brand5") + "</td><td>" + rs.getString("brand6")
									+ "</td><td>" + rs.getString("brand7") + "</td><td>" + rs.getString("brand8")
									+ "</td><td>" + rs.getString("brand9") + "</td><td>" + rs.getInt("totalCalls")
									+ "</td><td>" + rs.getInt("totalUniqueCalls") + "</td><td>"
									+ rs.getInt("totalAnsweredCalls") + "</td><td>" + rs.getInt("totalUnAnsweredCalls")
									+ "</td><td>" + rs.getInt("totalTrials") + "</td><td>"
									+ rs.getInt("totalUniqueTrials") + "</td><td>" + rs.getInt("totalRegret")
									+ "</td><td>" + rs.getInt("totalOptIn") + "</td>";
						} else {
							totalSmsDelivery += rs.getInt("TotalSmsDelivery");
							totalTtsDelivery += rs.getInt("TotalTtsDelivery");
							htmlData += "<td>" + rs.getString("site_name") + "</td><td>" + rs.getString("CallerId")
									+ "</td><td>" + rs.getString("brandName") + "</td><td>" + rs.getInt("totalCalls")
									+ "</td><td>" + rs.getInt("totalTrials") + "</td><td>"
									+ rs.getInt("totalUniqueTrials") + "</td><td>" + rs.getInt("totalRegret")
									+ "</td><td>" + rs.getInt("totalOptIn") + "</td><td>"
									+ rs.getInt("totalSmsDelivery") + "</td><td>" + rs.getInt("totalTtsDelivery")
									+ "</td>";
						}
					} else {
						// totalSmsTrials+=rs.getInt("totalSmsTrials");
						htmlData += "<td>" + rs.getString("site_name") + "</td><td>" + rs.getString("CallerId")
								+ "</td><td>" + rs.getString("brandName") + "</td><td>" + rs.getInt("totalCalls")
								+ "</td><td>" + rs.getInt("totalAnsweredCalls") + "</td><td>"
								+ rs.getInt("totalUnAnsweredCalls") + "</td><td>" + rs.getInt("totalTrials")
								+ "</td><td>" + rs.getInt("totalOptIn") + "</td>";
					}
				}
				htmlData += "</tr>";
			}
			if (serviceName.equals("pampersbandhan") || serviceName.equals("pampersbandhannew")) {
				htmlData += "<tr><td>Grand Total</td><td></td><td>" + totalCalls + "</td><td>" + totalUnansweredCalls
						+ "</td><td>" + totalTrials + "</td><td>" + totalUniqueTrials + "</td><td>" + totalUniqueStoreId
						+ "</td><td>" + totalPharmacyStores + "</td></tr></table></body></html>";
			} else if (serviceName.equals("wheels")) {
				htmlData += "<tr><td>Grand Total</td><td></td><td></td><td>" + totalCalls + "</td><td>" + totalTrials
						+ "</td><td>" + totalUniqueTrials + "</td><td>" + totalOptin
						+ "</td></tr></table></body></html>";
			} else if (serviceName.equals("pampersengagement")) {
				htmlData += "<tr><td>Grand Total</td><td></td><td></td><td>" + totalCalls + "</td><td>" + totalTrials
						+ "</td><td>" + totalUniqueTrials + "</td><td>" + totalOptin + "</td><td>"
						+ totalAnsweredCallsRoundtwo + "</td><td>" + totalCallsRoundtwodaytwo
						+ "</td></tr></table></body></html>";
			} else if (serviceName.equals("wsitrial")) {
				htmlData += "<tr><td>Grand Total</td><td>" + totalCalls + "</td><td>" + totalTrials + "</td><td>"
						+ totalUniqueTrials + "</td><td>" + totalOptin + "</td><td>" + totalAnsweredCallsRoundtwo
						+ "</td></tr></table></body></html>";
			} else if (serviceName.equals("pamperspremiumcare") || serviceName.equals("whisperkgis")) {
				htmlData += "<tr><td>Grand Total</td><td></td><td></td><td>" + totalCalls + "</td><td>" + totalTrials
						+ "</td><td>" + totalRegret + "</td><td>" + totalExpired + "</td></tr></table></body></html>";
			} else if (serviceName.equals("pampersoffline") || serviceName.equals("pampersapi")) {
				htmlData += "<tr><td>Grand Total</td><td></td><td></td><td>" + totalCalls + "</td><td>" + totalTrials
						+ "</td><td>" + totalOptin + "</td></tr></table></body></html>";
			} else {
				if (!serviceName.startsWith("pampers")) {
					if (serviceName.equalsIgnoreCase("gillettepome")) {
						htmlData += "<tr><td>Grand Total</td><td></td><td></td><td>" + totalCalls + "</td><td>"
								+ totalUniqueCalls + "</td><td>" + totalAnsweredCalls + "</td><td>"
								+ totalUnansweredCalls + "</td><td>" + totalTrials + "</td><td>" + totalUniqueTrials
								+ "</td><td>" + totalRegret + "</td><td>" + totalOptin
								+ "</td></tr></table></body></html>";
					} else if (serviceName.equalsIgnoreCase("cp") || serviceName.equalsIgnoreCase("socp")
							|| serviceName.equalsIgnoreCase("hth") || serviceName.equalsIgnoreCase("multibrandsampling")
							|| serviceName.equalsIgnoreCase("venussolo") || serviceName.equalsIgnoreCase("olaysolo")
							|| serviceName.equalsIgnoreCase("rurban")) {
						htmlData += "<tr><td>Grand Total</td><td></td><td>" + totalBrand1 + "</td><td>" + totalBrand2
								+ "</td><td>" + totalBrand3 + "</td><td>" + totalBrand4 + "</td><td>" + totalBrand5
								+ "</td><td>" + totalBrand6 + "</td><td>" + totalBrand7 + "</td><td>" + totalBrand8
								+ "</td><td>" + totalBrand9 + "</td><td>" + totalCalls + "</td><td>" + totalUniqueCalls
								+ "</td><td>" + totalAnsweredCalls + "</td><td>" + totalUnansweredCalls + "</td><td>"
								+ totalTrials + "</td><td>" + totalUniqueTrials + "</td><td>" + totalRegret
								+ "</td><td>" + totalOptin + "</td></tr></table></body></html>";
					} else {
						htmlData += "<tr><td>Grand Total</td><td></td><td></td><td>" + totalCalls + "</td><td>"
								+ totalTrials + "</td><td>" + totalUniqueTrials + "</td><td>" + totalRegret
								+ "</td><td>" + totalOptin + "</td><td>" + totalSmsDelivery + "</td><td>"
								+ totalTtsDelivery + "</td></tr></table></body></html>";
					}
				} else {
					htmlData += "<tr><td>Grand Total</td><td></td><td></td><td>" + totalCalls + "</td><td>"
							+ totalAnsweredCalls + "</td><td>" + totalUnansweredCalls + "</td><td>" + totalTrials
							+ "</td><td>" + totalOptin + "</td></tr></table></body></html>";
				}
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				columnArr = null;
				dataCounter = 0;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return htmlData + "@@" + password;
	}

	public int fetchMobileNumberUniqueId(String serviceName, String mobileNumber) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		Connection con = null;
		int mobileNumberId = 0;
		try {
			query = PropertyHandler.getInstance().getValue(serviceName + "_fetch_mobilenumberId");
			con = this.getConnection();
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, mobileNumber);
			logger.info("Mobile Number UniqueId Query-->" + query + "-->" + mobileNumber);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				mobileNumberId = resultSet.getInt("mobilenumber_id");
			}
			if (mobileNumberId == 0) {
				query = PropertyHandler.getInstance().getValue(serviceName + "_insert_mobilenumberId");
				pstmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
				pstmt.setString(1, mobileNumber);
				pstmt.executeUpdate();
				resultSet = pstmt.getGeneratedKeys();
				while (resultSet.next()) {
					mobileNumberId = resultSet.getInt(1);
				}
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return mobileNumberId;
	}

       public int fetchMobileNumberId(String serviceName, String mobileNumber) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		Connection con = null;
		int mobileNumberId = 0;
		try {
			query = PropertyHandler.getInstance().getValue(serviceName + "_fetch_mobilenumberId");
			con = this.getConnection();
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, mobileNumber);
			logger.info("Mobile Number UniqueId Query-->" + query + "-->" + mobileNumber);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				mobileNumberId = resultSet.getInt("mobilenumber_id");
			}

		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return mobileNumberId;
	}


       public int fetchStatus(String serviceName, int status) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		Connection con = null;
		int mobileNumberId = 0;
		try {
			query = PropertyHandler.getInstance().getValue(serviceName + "_tcid_columns");
			con = this.getConnection();
			pstmt = con.prepareStatement(query);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				System.out.println("Status is ====>>> " + status);
				int statusValue = resultSet.getInt("status");
				if (statusValue == 0) {
					status = 0;
				} else if (statusValue == 1) {
					status = 1;
				} else {
					status = 2;
				}
			}

		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return status;
	}


	public String fetchStringMobileNumberUniqueId(String serviceName, String mobileNumber) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		Connection con = null;
		String mobileNumberId = "NA";
		try {
			query = PropertyHandler.getInstance().getValue(serviceName + "_fetch_mobilenumberId");
			con = this.getConnection();
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, mobileNumber);
			logger.info("Mobile Number UniqueId Query-->" + query + "-->" + mobileNumber);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				mobileNumberId = resultSet.getString("mobile_number_uniqueid");
			}
			if (mobileNumberId.equalsIgnoreCase("NA")) {
				mobileNumberId = generate_alphanumeric(10, serviceName);
				query = PropertyHandler.getInstance().getValue(serviceName + "_insert_mobilenumberId");
				pstmt = con.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
				logger.info("Mobile Number UniqueId Insert Query-->" + query + "-->" + mobileNumber);
				pstmt.setString(1, mobileNumber);
				pstmt.setString(2, mobileNumberId);
				pstmt.executeUpdate();
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return mobileNumberId;
	}

	/**
	 * Generate Unique Id of Mobile Number
	 * 
	 * @return
	 */

	private String generate_alphanumeric(int count, String serviceName) {
		StringBuilder builder = new StringBuilder();
		String mobile_number_uniqueid = null;
		int validation = 0;
		try {
			while (count-- != 0) {
				int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
				builder.append(ALPHA_NUMERIC_STRING.charAt(character));
			}
			validation = validate_mobilenumber_uniqueid(builder.toString(), serviceName);
			if (validation > 0) {
				builder = null;
				generate_alphanumeric(count, serviceName);
			}
			mobile_number_uniqueid = builder.toString();
			logger.info(mobile_number_uniqueid);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			count = 0;
			builder = null;
		}
		return mobile_number_uniqueid;
	}

	/**
	 * Check if Unique Id exists for Mobile Number
	 * 
	 * @return
	 */

	public int validate_mobilenumber_uniqueid(String uniqueid, String serviceName) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		int count = 0;
		try {
			query = PropertyHandler.getInstance().getValue(serviceName + "_count_mobilenumberId");
			logger.info(query + "--" + uniqueid);
			con = this.getConnection();
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, uniqueid);
			rs = pstmt.executeQuery();
			boolean found = rs.next();
			if (found) {
				count = rs.getInt("count");
			}
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
			return 2;
		} finally {
			try {
				query = null;
				uniqueid = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception exception) {
				logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
						.map(Objects::toString).collect(Collectors.joining("\n")));
			}
		}
		return count;
	}

	/**
	 * Check if Tcid Exists
	 * 
	 * @return
	 */

	public int validateTcid(int siteName, int projectId, int tcid) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		int count = 0;
		try {
			query = "select count(*) as count from pampers_application_v2._tbl_retailer_details where site_id=? and tcid=? and status=1";
			logger.info(query + "--" + siteName + "-->" + projectId + "-->" + tcid);
			con = this.getConnection();
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, siteName);
			pstmt.setInt(2, tcid);
			rs = pstmt.executeQuery();
			boolean found = rs.next();
			if (found) {
				count = rs.getInt("count");
			}
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
			return 2;
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception exception) {
				logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
						.map(Objects::toString).collect(Collectors.joining("\n")));
			}
		}
		return count;
	}

	public void addTcidDetails(String serviceName, String tcid, String projectId, String siteName, String name,
			String mobileNumber) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		int mobileNumberId = 0;
		try {
			mobileNumberId = fetchMobileNumberUniqueId(serviceName, mobileNumber);
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(serviceName + "_insert_tcid");
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, tcid);
			pstmt.setString(2, projectId);
			pstmt.setString(3, siteName);
			pstmt.setString(4, name);
			pstmt.setInt(5, mobileNumberId);
			logger.info("Tcid Insert Query for " + serviceName + "-->" + query + "-->" + tcid + "-->" + projectId
					+ "-->" + siteName + "-->" + name + "-->" + mobileNumber);
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void addIpAddressDetails(String ipaddress, String comments, String empId) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue("insert_ipaddress");
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, ipaddress);
			pstmt.setString(2, comments);
			pstmt.setString(3, empId);
			logger.info("IpAddress Insert Query for " + query + "-->" + ipaddress);
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void addTestingMobileNumberDetails(int mobileNumber, String comments, String empId, String serviceName) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(serviceName.toLowerCase() + "_insert_testingMobileNumber");
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, mobileNumber);
			pstmt.setString(2, comments);
			pstmt.setString(3, empId);
			logger.info("TestingMobileNumber Insert Query for " + query + "-->" + mobileNumber + "-->" + comments
					+ "-->" + empId + "-->" + serviceName);
			pstmt.executeUpdate();
			reloadConfiguration(serviceName);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public int whisperInductionCouponAlert(String siteId) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		int alertFlag = -1;
		try {
			con = this.getConnection();
			query = "select if((count(*)-ifnull(sum(if(Status=2,1,0)),0))>=(count(*)*0.6),0,1) as AlertStatus from whisperinduction_application._tbl_couponcode_master where SiteId=?";
			pstmt = con.prepareStatement(query);
			logger.info(query + "-->" + siteId);
			pstmt.setString(1, siteId);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				alertFlag = rs.getInt("AlertStatus");
			}
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception ex) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + ex + Arrays.asList(ex.getStackTrace())
						.stream().map(Objects::toString).collect(Collectors.joining("\n")));
			}
		}
		return alertFlag;
	}

	public int venusSoloCouponAlert() {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		int alertFlag = -1;
		try {
			con = this.getConnection();
			query = "select if((count(*)-ifnull(sum(if(Status=2,1,0)),0))>=(count(*)*0.6),0,1) as AlertStatus from venussolo_application._tbl_couponcode_master where SiteId=?";
			pstmt = con.prepareStatement(query);
			logger.info(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				alertFlag = rs.getInt("AlertStatus");
			}
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception ex) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + ex + Arrays.asList(ex.getStackTrace())
						.stream().map(Objects::toString).collect(Collectors.joining("\n")));
			}
		}
		return alertFlag;
	}

	public int multiBrandSamplingCouponAlert() {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		int alertFlag = -1;
		try {
			con = this.getConnection();
			query = "select if((count(*)-ifnull(sum(if(Status=2,1,0)),0))>=(count(*)*0.6),0,1) as AlertStatus from multibrandsampling_application._tbl_couponcode_master where SiteId=?";
			pstmt = con.prepareStatement(query);
			logger.info(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				alertFlag = rs.getInt("AlertStatus");
			}
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception ex) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + ex + Arrays.asList(ex.getStackTrace())
						.stream().map(Objects::toString).collect(Collectors.joining("\n")));
			}
		}
		return alertFlag;
	}

	public int travelKitCouponAlert() {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		int alertFlag = -1;
		try {
			con = this.getConnection();
			query = "select if((count(*)-ifnull(sum(if(Status=2,1,0)),0))>=(count(*)*0.6),0,1) as AlertStatus from travelkit_application._tbl_couponcode_master where SiteId=?";
			pstmt = con.prepareStatement(query);
			logger.info(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				alertFlag = rs.getInt("AlertStatus");
			}
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception ex) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + ex + Arrays.asList(ex.getStackTrace())
						.stream().map(Objects::toString).collect(Collectors.joining("\n")));
			}
		}
		return alertFlag;
	}

	public void wsItrialReports(String startDate, String endDate, String serviceName) {
		String query = null;
		List<String> panelRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		ResultSet rss = null;
		String branchName = null;
		String htmlData = "NA";
		CSVWriter writer = null;
		// int branchId=0;
		// int branchCode=0;
		int siteId = 0;
		String[] summaryHeaders = PropertyHandler.getInstance().getValue(serviceName + "_reportSummaryHeaders")
				.split(",");
		Connection con = null;
		try {
			con = this.getConnection();
			// query="select bm.BranchCode,bm.BranchId,bm.BranchName,bm.SiteId,sm.SiteName
			// from wsitrial_application._tbl_branch_master bm inner join
			// wsitrial_application._tbl_site_master sm on bm.SiteId=sm.SiteId where
			// bm.Status=1";
			query = "select SiteId,SiteName from wsitrial_application._tbl_site_master where SiteId not in (4) and Status=1";
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			logger.info("WsItrial Report First Query-->" + query);
			while (rs.next()) {
				// branchId=rs.getInt("BranchId");
				// branchName=rs.getString("BranchName");
				// branchCode=rs.getInt("BranchCode");
				siteId = rs.getInt("SiteId");
				Email email = DbHandler.getInstance().getEmailDetails(rs.getString("SiteName"), "wsitrial", startDate,
						endDate);
				htmlData = "<html> <head> <style> table, th, td {   border: 1px solid black;   border-collapse: collapse; } th, td {   padding: 15px;   text-align: left; } table#t01 {   width: 100%;      background-color: #f1f1c1; } </style> </head> <body> <br><br><table width=\"100%\" cellspacing=\"1\" cellspadding=\"1\" border=\"1\"><tr>";
				writer = new CSVWriter(new FileWriter(email.getFilePath() + email.getFileName()));
				// writer.writeNext(reportHeaders);
				for (int j = 0; j < summaryHeaders.length; j++) {
					htmlData += "<th>" + summaryHeaders[j] + "</th>";
				}
				query = "select tbm.BranchName, od.MobileNumberId as MobileNumber,od.CallAnswerTime as CallAnswerTime,md.CallerId as MissedCallNumber,If(od.BranchCodeStatus=1,tdd.BranchCode,'NA') as BranchCode,If(od.BranchCodeStatus=1,'Valid','Invalid') as BranchCodeStatus,If(od.DistributorCodeStatus=1,tdd.WholeSalerCode,'NA') as WholeSalerCode,If(od.DistributorCodeStatus=1,'Valid','Invalid') as WholeSalerCodeStatus,od.OptinDtmf,If(od.OptinStatus=1,'Valid','Invalid') as OptinStatus from wsitrial_application._tbl_misscall_details md inner join wsitrial_application._tbl_projectsite_mapping_master tpmm on md.ProjectId=tpmm.ProjectId inner join wsitrial_application._tbl_obdcall_details od on md.MissCallId=od.MissCallId left join wsitrial_application._tbl_distributor_details tdd on (tdd.Status=2 and od.SiteId=tdd.SiteId and od.BranchCode=tdd.IvrBranchCode and od.DistributorCode=tdd.IvrDistributorCode) inner join wsitrial_application._tbl_branch_master tbm on od.BranchCode=tbm.BranchCode where (md.CreateDate>? and md.CreateDate<?) and tdd.SiteId=? and md.ProjectId>1 group by 2,od.CallSendTime order by od.CallSendTime";
				pstmt = con.prepareStatement(query);
				pstmt.setString(1, startDate);
				pstmt.setString(2, endDate + " 23:59:59");
				pstmt.setInt(3, siteId);
				logger.info("WsItrial Report Second Query-->" + query + "-->" + startDate + "-->" + endDate + "-->"
						+ siteId);
				rss = pstmt.executeQuery();
				writer.writeAll(rss, true);
				rss = null;
				writer.flush();
				writer.close();
				writer = new CSVWriter(new FileWriter(email.getFilePath() + "Registration_" + email.getFileName()));
				query = "select tbm.BranchName as BranchName, od.MobileNumberId as MobileNumber,od.CallAnswerTime as CallAnswerTime,md.CallerId as MissedCallNumber,If(od.BranchCodeStatus=1,tdd.BranchCode,'NA') as BranchCode,If(od.DistributorCodeStatus=1,tdd.WholeSalerCode,'NA') as WholeSalerCode,If(od.DistributorCodeStatus=1,'Valid','Invalid') as WholeSalerCodeStatus from wsitrial_application._tbl_misscall_details md inner join wsitrial_application._tbl_projectsite_mapping_master tpmm on md.ProjectId=tpmm.ProjectId inner join wsitrial_application._tbl_obdcall_details od on md.MissCallId=od.MissCallId left join wsitrial_application._tbl_distributor_details tdd on (tdd.Status=2 and od.SiteId=tdd.SiteId) inner join wsitrial_application._tbl_branch_master tbm on od.BranchCode=tbm.BranchCode where tdd.SiteId=? and md.ProjectId=1 group by 2,od.CallSendTime order by od.CallSendTime";
				pstmt = con.prepareStatement(query);
				// pstmt.setString(1, startDate);
				// pstmt.setString(2, endDate);
				pstmt.setInt(1, siteId);
				logger.info("WsItrial Report Third Query-->" + query + "-->" + startDate + "-->" + endDate + "-->"
						+ siteId);
				rss = pstmt.executeQuery();
				writer.writeAll(rss, true);
				writer.flush();
				writer.close();
				rss = null;
				query = "select tbm.BranchName,IFNULL(sum(case when md.TrialStatus=1 then 1 else 0 end),0) as total_registered_distributor,count(distinct(case when md.TrialStatus=2 then od.DistributorCode end)) as total_active_distributor,IFNULL(sum(case when md.TrialStatus=2 then 1 else 0 end),0) as total_calls_trials,count(distinct(case when md.TrialStatus=2 then md.MobileNumber end)) as unique_calls_trials,IFNULL(sum(case when od.OptinStatus=1 then 1 else 0 end),0) as total_optin from wsitrial_application._tbl_misscall_details md inner join wsitrial_application._tbl_obdcall_details od on md.MissCallId=od.MissCallId inner join wsitrial_application._tbl_branch_master tbm on (od.BranchCode=tbm.BranchCode and od.BranchCodeStatus=1) where (md.CreateDate>? and md.CreateDate<?) and od.SiteId=? group by od.BranchCode";
				pstmt = con.prepareStatement(query);
				pstmt.setString(1, startDate);
				pstmt.setString(2, endDate + " 23:59:59");
				pstmt.setInt(3, siteId);
				logger.info("WsItrial Report Fourth Query-->" + query + "-->" + startDate + "-->" + endDate + "-->"
						+ siteId);
				rss = pstmt.executeQuery();
				while (rss.next()) {
					htmlData += "<tr>";
					htmlData += "<td>" + rs.getString("SiteName") + "</td><td>" + rss.getString("BranchName")
							+ "</td><td>" + rss.getInt("total_registered_distributor") + "</td><td>"
							+ rss.getInt("total_active_distributor") + "</td><td>" + rss.getInt("total_calls_trials")
							+ "</td><td>" + rss.getInt("unique_calls_trials") + "</td><td>" + rss.getInt("total_optin")
							+ "</td>";
					htmlData += "</tr>";
				}
				Utility.zipFile(email.getFilePath(), email.getFileName());
				logger.debug(htmlData);
				email.setEmailBody(email.getEmailBody().replace("%CONTENT", htmlData));
				Utility.sendMail(email, "wsitrial");
				htmlData = "NA";
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				panelRecords.clear();
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void dailyServiceReportsTrialTracker(String startDate, String endDate, String siteName, String serviceName) {
		String query = null;
		List<String> panelRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String htmlData = "NA";
		CSVWriter writer = null;
		Connection con = null;
		try {
			con = this.getConnection();
			Email email = DbHandler.getInstance().getEmailDetails("Daily", serviceName, startDate, endDate);
			writer = new CSVWriter(new FileWriter(email.getFilePath() + email.getFileName()));
			query = PropertyHandler.getInstance()
					.getValue(serviceName.toLowerCase() + "_fetchDailyServiceTrialTrackerQuery");
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, startDate);
			pstmt.setString(2, endDate + " 23:59:59");
			pstmt.setString(3, siteName);
			rs = pstmt.executeQuery();
			writer.writeAll(rs, true);
			writer.flush();
			writer.close();
			logger.info(WordUtils.capitalize(serviceName) + " Trial Tracker Report Query-->" + query + "-->" + startDate
					+ "-->" + endDate + "-->" + email.getFilePath() + "-->" + email.getFileName());
			Utility.zipFile(email.getFilePath(), email.getFileName());
			email.setEmailBody(email.getEmailBody().replace("%CONTENT", htmlData));
			Utility.sendMail(email, serviceName);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				panelRecords.clear();
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void dailyServiceReports(String startDate, String endDate, String serviceName) {
		String query = null;
		List<String> panelRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String htmlData = "NA";
		CSVWriter writer = null;
		Connection con = null;
		try {
			con = this.getConnection();
			Email email = DbHandler.getInstance().getEmailDetails("Daily", serviceName, startDate, endDate);
			writer = new CSVWriter(new FileWriter(email.getFilePath() + email.getFileName()));
			query = PropertyHandler.getInstance().getValue(serviceName.toLowerCase() + "_fetchDailyServiceQuery");
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, startDate);
			pstmt.setString(2, endDate + " 23:59:59");
			rs = pstmt.executeQuery();
			writer.writeAll(rs, true);
			writer.flush();
			writer.close();
			logger.info(WordUtils.capitalize(serviceName) + " Daily Report Query-->" + query + "-->" + startDate + "-->"
					+ endDate + "-->" + email.getFilePath() + "-->" + email.getFileName());
			Utility.zipFile(email.getFilePath(), email.getFileName());
			email.setEmailBody(email.getEmailBody().replace("%CONTENT", htmlData));
			Utility.sendMail(email, serviceName);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				panelRecords.clear();
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void dailyCouponAlertReports(String serviceName) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet resultSet = null;
		Connection con = null;
		int alertFlag = 0;
		int couponCount = 0;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(serviceName.toLowerCase() + "_fetchCouponAlertServiceQuery");
			pstmt = con.prepareStatement(query);
			logger.info(query);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				alertFlag = resultSet.getInt("AlertStatus");
			}
			if (alertFlag > 0) {
				query = PropertyHandler.getInstance()
						.getValue(serviceName.toLowerCase() + "_fetchCouponCountServiceQuery");
				pstmt = con.prepareStatement(query);
				logger.info(query);
				resultSet = pstmt.executeQuery();
				while (resultSet.next()) {
					couponCount = resultSet.getInt("CouponCount");
				}
				Email email = DbHandler.getInstance().getEmailDetails("DailyCouponAlert", serviceName, "NA", "NA");
				email.setEmailBody(email.getEmailBody().replace("%CONTENT", Integer.toString(couponCount)));
				Utility.sendMail(email, serviceName);
			} else {
				logger.info("More Than 50% Coupon Codes Left For " + WordUtils.capitalize(serviceName));
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (resultSet != null) {
					resultSet.close();
					resultSet = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public Email getEmailDetails(String reportName, String serviceName, String startDate, String endDate) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Email email = new Email();
		Connection con = null;
		try {
			query = PropertyHandler.getInstance().getValue(serviceName.toLowerCase() + "_fetchEmailQuery");
			con = this.getConnection();
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, reportName + "Reports");
			logger.info(query + "-->" + reportName + "Reports");
			rs = pstmt.executeQuery();
			while (rs.next()) {
				email.setBccEmailAddress(rs.getString("BccEmailAddress"));
				email.setCcEmailAddress(rs.getString("CcEmailAddress"));
				email.setFromEmailAddress(rs.getString("FromEmailAddress"));
				email.setFilePath(rs.getString("FilePath"));
				email.setFileName(rs.getString("FileName").replace("%SITENAME", reportName)
						.replace("%YESTERDAYDATE", endDate).replace("%SERVICENAME", serviceName));
				email.setEmailBody(rs.getString("EmailBody").replace("%SERVICENAME", serviceName)
						.replace("%YESTERDAYDATE", endDate).replace("%MONTH", Utility.getCurrentMonth())
						.replace("%YEAR", Integer.toString(Utility.getCurrentYear())));
				email.setEmailSubject(rs.getString("EmailSubject").replace("%SERVICENAME", serviceName.toUpperCase())
						.replace("%YESTERDAYDATE", endDate).replace("%MONTH", Utility.getCurrentMonth())
						.replace("%YEAR", Integer.toString(Utility.getCurrentYear()))
						.replace("%YESTERDAY_DATE", startDate.split(" ")[0] + " and " + endDate));
				email.setToEmailAddress(rs.getString("ToEmailAddress"));
				email.setAttachmentFlag(rs.getInt("AttachmentFlag"));
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return email;
	}

	/**
	 * Generate Unique Id of Mobile Number
	 * 
	 * @return
	 */

	private String generate_alphanumeric(String mobile_number, int count) {
		StringBuilder builder = new StringBuilder();
		String mobile_number_uniqueid = null;
		int validation = 0;
		try {
			mobile_number_uniqueid = fetch_mobilenumber_uniqueid(mobile_number);
			if (mobile_number_uniqueid.equalsIgnoreCase("NA")) {
				while (count-- != 0) {
					int character = (int) (Math.random() * ALPHA_NUMERIC_STRING.length());
					builder.append(ALPHA_NUMERIC_STRING.charAt(character));
				}
				validation = validate_mobilenumber_uniqueid(builder.toString());
				// System.out.println("validation=" + validation);
				if (validation == 1) {
					builder = null;
					generate_alphanumeric(mobile_number, count);
				} else {
					insert_mobile_number_uniqueid(mobile_number, builder.toString());
				}
				mobile_number_uniqueid = builder.toString();
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			count = 0;
			mobile_number = null;
			builder = null;
		}
		return mobile_number_uniqueid;
	}

	/**
	 * Check if Unique Id exists for Mobile Number
	 * 
	 * @return
	 */

	public int validate_mobilenumber_uniqueid(String mobile_number_uniqueid) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		int count = 0;
		try {
			query = "select count(*) as count from itrial_v3._tbl_mobilenumber_details where mobile_number_uniqueid=?";
			logger.info(query + "-->" + mobile_number_uniqueid);
			con = this.getConnection();
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, mobile_number_uniqueid);
			rs = pstmt.executeQuery();
			boolean found = rs.next();
			if (found) {
				count = rs.getInt("count");
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
			return 2;
		} finally {
			try {
				query = null;
				mobile_number_uniqueid = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error(e + Arrays.asList(e.getStackTrace()).stream().map(Objects::toString)
						.collect(Collectors.joining("\n")));
			}
		}
		return count;
	}

	/**
	 * Fetch UniqueID of Mobile Number
	 * 
	 * @return
	 */
	public String fetch_mobilenumber_uniqueid(String mobile_number) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		String mobile_number_uniqueid = "NA";
		try {
			query = "select mobile_number_uniqueid from itrial_v3._tbl_mobilenumber_details where (mobile_number=? or mobile_number=?)";
			// loggerhandler.infolog(query+"-->"+mobile_number);
			con = this.getConnection();
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, Utility.encrypt(mobile_number, "!tri@l)(032020", "png@ppl!c@tions"));
			pstmt.setString(2, mobile_number);
			rs = pstmt.executeQuery();
			boolean found = rs.next();
			if (found) {
				mobile_number_uniqueid = rs.getString("mobile_number_uniqueid");
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
			return "failure";
		} finally {
			try {
				query = null;
				mobile_number = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				logger.error(e + Arrays.asList(e.getStackTrace()).stream().map(Objects::toString)
						.collect(Collectors.joining("\n")));
			}
		}
		return mobile_number_uniqueid;
	}

	/**
	 * Insert Unique Id For Mobile Number
	 * 
	 * @return
	 */
	public int insert_mobile_number_uniqueid(String mobile_number, String uniqueid) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		try {
			query = "insert into itrial_v3._tbl_mobilenumber_details (mobile_number,mobile_number_uniqueid) values (?,?)";
			con = this.getConnection();
			pstmt = con.prepareStatement(query);
			if (mobile_number.length() > 12) {
				logger.error("Duplicate Entry Found For Encryption for " + mobile_number + "-->" + uniqueid);
			} else {
				pstmt.setString(1, Utility.encrypt(mobile_number, "!tri@l)(032020", "png@ppl!c@tions"));
				pstmt.setString(2, uniqueid);
				pstmt.executeUpdate();
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				uniqueid = null;
				mobile_number = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e + Arrays.asList(e.getStackTrace()).stream().map(Objects::toString)
						.collect(Collectors.joining("\n")));
				return 0;
			}
		}
		return 1;
	}

	public int insertPampersDashboardRequest(String queryCondition, String serviceName, String queryColumns,
			String dateCondition, String startDate, String endDate, String reportingFileName, String userType,
			String storeCode, String tcid) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		try {
			query = PropertyHandler.getInstance().getValue("insertPampersBandhanReportingQuery");
			con = this.getConnection();
			logger.info(query + "-->" + queryCondition + "-->" + serviceName + "-->" + queryColumns + "-->"
					+ dateCondition + "-->" + startDate + "-->" + endDate + "-->" + userType);
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, queryCondition);
			pstmt.setString(2, serviceName);
			pstmt.setString(3, queryColumns);
			pstmt.setString(4, dateCondition);
			pstmt.setString(5, Utility.encrypt(storeCode, "p@mp$rS@05062020", "png@ppl!210620@)"));
			pstmt.setString(6, tcid);
			pstmt.setString(7, startDate);
			pstmt.setString(8, endDate);
			pstmt.setString(9, reportingFileName);
			pstmt.setString(10, userType);
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e + Arrays.asList(e.getStackTrace()).stream().map(Objects::toString)
						.collect(Collectors.joining("\n")));
				return 0;
			}
		}
		return 1;
	}

	public int updatePampersDashboardRequest(String startDate, String endDate, String serviceName) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		try {
			query = PropertyHandler.getInstance().getValue("updatePampersBandhanReportingQuery");
			con = this.getConnection();
			logger.info(query + "-->" + startDate + "-->" + serviceName + "-->" + endDate);
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, startDate + " 00:00:00");
			pstmt.setString(2, endDate + " 00:00:00");
			pstmt.setString(3, serviceName);
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error(e + Arrays.asList(e.getStackTrace()).stream().map(Objects::toString)
						.collect(Collectors.joining("\n")));
				return 0;
			}
		}
		return 1;
	}

	public void dailyServiceReportsPampers(String startDate, String endDate, String serviceName) {
		String query = null;
		List<String> panelRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String[] columnArr;
		String dateCondition = PropertyHandler.getInstance().getValue(serviceName + "_dailyReport_dateCondition");
		String columns = PropertyHandler.getInstance().getValue(serviceName + "_dailyReport_columns");
		String tableName = PropertyHandler.getInstance().getValue(serviceName + "_" + "dailyReports_tableName");
		String dbCondition = PropertyHandler.getInstance().getValue(serviceName + "_dailyReport_dbCondition");
		String[] reportHeaders = PropertyHandler.getInstance().getValue(serviceName + "_dailyReportHeaders").split(",");
		Connection con = null;
		int dataCounter = 0;
		CSVWriter writer = null;
		String htmlData = "NA";
		try {

			logger.info(
					"Start Date is " + startDate + " End Date is " + endDate + " and Service Name is " + serviceName);
			Email email = DbHandler.getInstance().getEmailDetails("DailyPampersBandhan", serviceName, startDate,
					endDate);
			writer = new CSVWriter(new FileWriter(email.getFilePath() + email.getFileName()));
			writer.writeNext(reportHeaders);
			con = this.getConnection();
			query = "select " + columns.replace("@", ",") + " from " + tableName + " where " + dateCondition + " and "
					+ dbCondition;
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, startDate);
			pstmt.setString(2, endDate + " 23:59:59");
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dataCounter = dataCounter + 1;
				// logger.info("data counter is "+dataCounter);
				columnArr = columns.split("@");
				// logger.info("the column arr is "+Arrays.deepToString(columnArr));
				for (int i = 0; i < columnArr.length; i++) {
					if (columnArr[i].endsWith("store_code")) {
						panelRecords.add(
								Utility.decrypt(rs.getString("store_code"), "p@mp$rS@05062020", "png@ppl!210620@)"));
					} else {
						if (columnArr[i].indexOf(" as ") == -1) {
							if (columnArr[i].indexOf("\\.") != -1) {
								panelRecords.add(rs.getString(columnArr[i].split("\\.")[1]));
							} else {
								panelRecords.add(rs.getString(columnArr[i]));
							}
						} else {
							panelRecords.add(rs.getString(columnArr[i].split(" as ")[1]));
						}
					}
				}
				writer.writeNext(panelRecords.toArray(new String[panelRecords.size()]));

				panelRecords.clear();
			}
			writer.flush();
			writer.close();
			logger.info(WordUtils.capitalize(serviceName) + " Daily Report Query-->" + query + "-->" + startDate + "-->"
					+ endDate + "-->" + email.getFilePath() + "-->" + email.getFileName());
			Utility.zipFile(email.getFilePath(), email.getFileName());
			email.setEmailBody(email.getEmailBody().replace("%CONTENT", htmlData));
			Utility.sendMail(email, serviceName);

		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				columnArr = null;
				dataCounter = 0;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public List<String> fetchCallerIds() {
		String query = null;
		List<String> dbRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue("get_callerid_master_data");
			logger.info("get caller id master data---->" + query);
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dbRecords.add(rs.getString("callerId"));
			}
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return dbRecords;
	}

	public int getProjectId(String serviceName) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		int projectId = 0;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(serviceName + "_get_project_id");
			logger.info("query for get project id----->" + query);
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				projectId = rs.getInt("ProjectId");
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return projectId;
	}

	public List<String> fetchMissCallDidRecord(String dbName) {
		String query = null;
		List<String> dbRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue("get_did_configuration_misscall_config");
			logger.info("get did configuration from misscall config---->" + query + "----->" + dbName);
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, dbName);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dbRecords.add(rs.getString("tablename"));
				dbRecords.add(rs.getString("column_names"));
				dbRecords.add(rs.getString("column_values"));
			}
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return dbRecords;
	}

	public List<String> fetchDidHistoryRecord(String serviceName, String callerId, String editFlag) {
		String query = null;
		List<String> dbRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue("fetch_did_history_support_manager");
			logger.info("query for get history record----->" + serviceName + "--->" + query + "-->" + callerId + "-->"
					+ editFlag);
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, serviceName);
			pstmt.setString(2, callerId);
			pstmt.setString(3, editFlag);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dbRecords.add(rs.getString("serviceName"));
				dbRecords.add(rs.getString("callerId"));
				dbRecords.add(rs.getString("projectId"));
				dbRecords.add(rs.getString("siteId"));
				dbRecords.add(rs.getString("siteName"));
				dbRecords.add(rs.getString("flowType"));
				dbRecords.add(rs.getString("offerName"));
				dbRecords.add(rs.getString("remarks"));
				dbRecords.add(rs.getString("dbname"));
				dbRecords.add(rs.getString("tablename"));
				dbRecords.add(rs.getString("column_names"));
				dbRecords.add(rs.getString("column_values"));
			}
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return dbRecords;
	}

	public int addDidConfiguration(String serviceName, String callerId, String projectId, String siteId,
			String siteName, String flowType, String offerName, String mobileNumber, String remarksValue,
			String actionStatus, String editFlag, String username, String operatorName) {
		try {
			String dbName = fetchServiceDatabaseName("database_name", serviceName);
			if (dbName == null || dbName.length() == 0) {
				return Constants.INVALID_DBNAME;
			}
			List<String> dbRecords = fetchMissCallDidRecord(dbName);
			if (dbRecords.size() > 0) {
				int uniqueServiceMappingId = addDidConfigurationServiceMapping(serviceName, callerId, projectId, siteId,
						offerName, mobileNumber);
				if (uniqueServiceMappingId != -1) {
					int uniqueMissCallConfigId = addDidConfigurationMissCallConfig(callerId, projectId, siteId,
							flowType, dbName, dbRecords.get(0), dbRecords.get(1), dbRecords.get(2), operatorName);
					if (uniqueMissCallConfigId != -1) {
						addDidConfigurationSupportManager(serviceName, callerId, projectId, siteId, siteName, flowType,
								offerName, remarksValue, actionStatus, editFlag, dbName, dbRecords.get(0),
								dbRecords.get(1), dbRecords.get(2), username);
						updateCallerIdStatus(callerId, "1");
						return Constants.VALID;
					} else {
						updateDidConfigurationServiceMapping(serviceName, "0", "NA", "0", "0", "", "0",
								uniqueServiceMappingId + "");
						return Constants.ERROR_MISS_CALL_CONFIG;
					}
				} else {
					return Constants.ERROR_PROJECT_SITE_MAPPING;
				}
			} else {
				return Constants.NO_DID_CONFIGURE;
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
			return Constants.INVALID;
		}
	}

	public int updateDidConfiguration(String serviceName, String callerId, String projectId, String siteId,
			String siteName, String flowType, String offerName, String remarksValue, String actionStatus,
			String editFlag, String username) {
		try {
			String dbName = fetchServiceDatabaseName("database_name", serviceName);
			if (dbName == null || dbName.length() == 0) {
				return Constants.INVALID_DBNAME;
			}
			List<String> dbRecords = fetchMissCallDidRecord(dbName);
			if (dbRecords.size() > 0) {
				List<String> resetRecords = fetchServiveMappingRecords(serviceName, projectId);

				int uniqueServiceMappingId = updateDidConfigurationServiceMapping(serviceName, resetRecords.get(1),
						resetRecords.get(2), siteId, resetRecords.get(4), offerName, resetRecords.get(6),
						resetRecords.get(0));

				if (uniqueServiceMappingId != -1) {
					int uniqueMissCallConfigId = updateDidConfigurationMissCallConfig(callerId, siteId, flowType);

					if (uniqueMissCallConfigId != -1) {
						updateEditStatus(serviceName, callerId);
						addDidConfigurationSupportManager(serviceName, callerId, projectId, siteId, siteName, flowType,
								offerName, remarksValue, actionStatus, editFlag, dbName, dbRecords.get(0),
								dbRecords.get(1), dbRecords.get(2), username);
						return Constants.VALID;
					} else {

						updateDidConfigurationServiceMapping(serviceName, resetRecords.get(1), resetRecords.get(2),
								resetRecords.get(3), resetRecords.get(4), resetRecords.get(5), resetRecords.get(6),
								resetRecords.get(0));
						return Constants.ERROR_MISS_CALL_CONFIG;
					}
				} else {
					return Constants.ERROR_PROJECT_SITE_MAPPING;
				}
			} else {
				return Constants.NO_DID_CONFIGURE;
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
			return Constants.INVALID;
		}
	}

	public int revokeDidConfiguration(String serviceName, String callerId, String actionStatus, String projectId,
			String editFlag, String username) {
		try {
			List<String> dbRecords = fetchDidHistoryRecord(serviceName, callerId, editFlag);
			if (dbRecords.size() > 0) {
				List<String> resetRecords = fetchServiveMappingRecords(serviceName, projectId);

				int uniqueServiceMappingId = updateDidConfigurationServiceMapping(serviceName, "0", "NA", "0", "0", "",
						"0", resetRecords.get(0));
				if (uniqueServiceMappingId != -1) {
					editFlag = "old";
					updateEditStatus(serviceName, callerId);
					addDidConfigurationSupportManager(dbRecords.get(0), dbRecords.get(1), dbRecords.get(2),
							dbRecords.get(3), dbRecords.get(4), dbRecords.get(5), dbRecords.get(6), dbRecords.get(7),
							actionStatus, editFlag, dbRecords.get(8), dbRecords.get(9), dbRecords.get(10),
							dbRecords.get(11), username);
					backupDidConfigurationMissCallConfig(callerId);
					deleteDidConfigurationMissCallConfig(callerId);
					updateCallerIdStatus(callerId, "0");
					return Constants.VALID;
				} else {
					return Constants.ERROR_PROJECT_SITE_MAPPING;
				}
			} else {
				return Constants.INVALID;
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
			return Constants.INVALID;
		}
	}

	public int addDidConfigurationSupportManager(String serviceName, String callerId, String projectId, String siteId,
			String siteName, String flowType, String offerName, String remarksValue, String actionStatus,
			String editFlag, String dbName, String tableName, String columnName, String columnValue, String username) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		ResultSet rs = null;
		int uniqueId = -1;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue("insert_did_configuration_supportmanager");

			pstmt = con.prepareStatement(query, new String[] { "id" });
			pstmt.setString(1, serviceName);
			pstmt.setString(2, callerId);
			pstmt.setString(3, projectId);
			pstmt.setString(4, siteId);
			pstmt.setString(5, siteName);
			pstmt.setString(6, flowType);
			pstmt.setString(7, offerName);
			pstmt.setString(8, remarksValue);
			pstmt.setString(9, actionStatus);
			pstmt.setString(10, editFlag);
			pstmt.setString(11, dbName);
			pstmt.setString(12, tableName);
			pstmt.setString(13, columnName);
			pstmt.setString(14, columnValue);
			pstmt.setString(15, username);
			logger.info("insert did configuration on support manager----> " + "---->" + query + "--->" + serviceName
					+ "--->" + callerId + "--->" + projectId + "--->" + siteId + "--->" + siteName + "--->" + flowType
					+ "--->" + offerName + "--->" + remarksValue + "--->" + actionStatus + "--->" + editFlag + "--->"
					+ dbName + "--->" + tableName + "--->" + columnName + "--->" + columnValue + "----->" + username);
			pstmt.executeUpdate();
			rs = pstmt.getGeneratedKeys();
			if (rs.next()) {
				uniqueId = rs.getInt(1);
			}
		} catch (Exception exception) {
			uniqueId = -1;
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return uniqueId;
	}

	public void updateEditStatus(String serviceName, String callerId) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue("update_did_configuration_supportmanager");
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, serviceName);
			pstmt.setString(2, callerId);
			pstmt.executeUpdate();
			logger.info("Query for update flag in history----> " + serviceName + "-->" + query + "---->" + callerId);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public int addDidConfigurationServiceMapping(String serviceName, String callerId, String projectId, String siteId,
			String offerName, String mobileNumber) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		ResultSet rs = null;
		int uniqueId = -1;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(serviceName + "_insert_didconfiguration_service_mapping");
			pstmt = con.prepareStatement(query, new String[] { "MappingId" });
			pstmt.setString(1, callerId);
			pstmt.setString(2, projectId);
			pstmt.setString(3, siteId);
			pstmt.setString(4, offerName);
			pstmt.setInt(5,
					fetchMobileNumberUniqueId(serviceName,
							Utility.encrypt(mobileNumber,
									EncryptionValues.valueOf(serviceName.toUpperCase() + Constants.SECRET).getValue(),
									EncryptionValues.valueOf(serviceName.toUpperCase() + "SALT").getValue())));
			logger.info("insert did configuraton on service mapping ---->" + serviceName + "----->" + query + "----->"
					+ callerId + "----->" + projectId + "----->" + siteId + "----->" + offerName);
			pstmt.executeUpdate();
			rs = pstmt.getGeneratedKeys();
			if (rs.next()) {
				uniqueId = rs.getInt(1);
			}
			addBrandConfiguration(serviceName, projectId, siteId, offerName);
		} catch (Exception exception) {
			uniqueId = -1;
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return uniqueId;
	}

	public int addDidConfigurationMissCallConfig(String callerId, String projectId, String siteId, String flowType,
			String dbName, String tableName, String columnName, String columnValue, String operatorName) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		ResultSet rs = null;
		int uniqueId = -1;
		String primaryOperator = "NA";
		String secondryOperator = "NA";
		List<String> operatorNameList = Arrays
				.asList(PropertyHandler.getInstance().getValue("did_operator_name").split(","));
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue("insert_did_configuration_misscall_config");
			pstmt = con.prepareStatement(query, new String[] { "id" });
			pstmt.setString(1, callerId);
			pstmt.setString(2, callerId);
			pstmt.setString(3, callerId);
			pstmt.setString(4, projectId);
			pstmt.setString(5, siteId);
			pstmt.setString(6, flowType);
			pstmt.setString(7, dbName);
			pstmt.setString(8, tableName);
			pstmt.setString(9, columnName);
			pstmt.setString(10, columnValue);
			if (operatorNameList != null && operatorNameList.size() == 2) {
				if (operatorNameList.indexOf(operatorName) == 0) {
					primaryOperator = operatorNameList.get(0);
					secondryOperator = operatorNameList.get(1);
				} else {
					primaryOperator = operatorNameList.get(1);
					secondryOperator = operatorNameList.get(0);
				}
			} 
			pstmt.setString(11, primaryOperator);
			pstmt.setString(12, secondryOperator);
			logger.info("insert did configuration misscall config ---->" + query + "---->" + callerId + "---->"
					+ projectId + "---->" + siteId + "---->" + flowType + "---->" + dbName + "---->" + tableName
					+ "---->" + columnName + "----->" + columnValue + "----->" + primaryOperator + "----->"
					+ secondryOperator);
			pstmt.executeUpdate();
			rs = pstmt.getGeneratedKeys();
			if (rs.next()) {
				uniqueId = rs.getInt(1);
			}
		} catch (Exception exception) {
			uniqueId = -1;
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return uniqueId;
	}

	public int updateDidConfigurationServiceMapping(String serviceName, String tcidMobileNumberId, String callerId,
			String siteId, String projectId, String offerName, String status, String uniqueServiceMappingId) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		int uniqueId = -1;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(serviceName + "_all_update_did_configuration_mapping");
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, tcidMobileNumberId);
			pstmt.setString(2, callerId);
			pstmt.setString(3, siteId);
			pstmt.setString(4, projectId);
			pstmt.setString(5, offerName);
			pstmt.setString(6, status);
			pstmt.setString(7, uniqueServiceMappingId);
			logger.info("update did configuration service mapping ----->" + serviceName + "-->" + query + "----->"
					+ tcidMobileNumberId + "----->" + callerId + "----->" + siteId + "----->" + projectId + "----->"
					+ offerName + "----->" + status + "----->" + uniqueServiceMappingId);
			pstmt.executeUpdate();
			uniqueId = pstmt.executeUpdate();
			if (uniqueId == 0) {
				uniqueId = -1;
			}
		} catch (Exception exception) {
			uniqueId = -1;
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return uniqueId;
	}

	public int updateDidConfigurationMissCallConfig(String callerId, String siteId, String flowType) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		int uniqueId = -1;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue("update_did_configuration_misscall_config");
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, siteId);
			pstmt.setString(2, flowType);
			pstmt.setString(3, callerId);
			logger.info("update did configuration misscall config---->" + query + "----->" + callerId + "----->"
					+ siteId + "----->" + flowType);
			uniqueId = pstmt.executeUpdate();
			if (uniqueId == 0) {
				uniqueId = -1;
			}
		} catch (Exception exception) {
			uniqueId = -1;
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return uniqueId;
	}

	public void updateCallerIdStatus(String callerId, String assignedStatus) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue("update_caller_id_status_supportmanager");
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, assignedStatus);
			pstmt.setString(2, callerId);
			logger.info(
					"update caller id status supportmanager -->" + query + "-->" + assignedStatus + "-->" + callerId);
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void backupDidConfigurationMissCallConfig(String callerId) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue("backup_did_configuration_misscall_config");
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, callerId);
			logger.info("backup deleted did configuration miss call config---->" + "-->" + query + "--->" + callerId);
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void deleteDidConfigurationMissCallConfig(String callerId) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue("delete_did_confuguration_misscall_config");
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, callerId);
			logger.info("delete did configuration misscall config---->" + "-->" + query + "---->" + callerId);
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public String fetchServiceDatabaseName(String key, String value) {
		String query = null;
		String service_dbname = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.getConnection();
			if (key.equalsIgnoreCase("service_name")) {
				query = "select service_name from _tbl_servicename_master where database_name = ? and status=1";
			} else if (key.equalsIgnoreCase("database_name")) {
				query = "select database_name from _tbl_servicename_master where service_name = ? and status=1";
			}
			logger.info("query for get dbname/service name from supportmanager---->" + query + "--->" + key + "--->"
					+ value);
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, value);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				service_dbname = rs.getString(key);
			}
			logger.debug(service_dbname);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return service_dbname;
	}

	public List<String> fetchCallerMasterCount() {
		String query = null;
		List<String> dbRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = "select " + PropertyHandler.getInstance().getValue("get_callerid_master_count_columns") + " from "
					+ PropertyHandler.getInstance().getValue("get_callerid_master_count_tablename") + " where "
					+ PropertyHandler.getInstance().getValue("get_callerid_master_count_datecondition") + " "
					+ PropertyHandler.getInstance().getValue("get_callerid_master_count_dbcondition");

			query = query.replace('@', ',');
			logger.info("fetch caller id master count---->" + query);
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dbRecords.add(rs.getString("total_did"));
				dbRecords.add(rs.getString("used_total_did"));
				dbRecords.add(rs.getString("free_did"));
				dbRecords.add(rs.getString("free_after_90days"));
			}
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return dbRecords;
	}

	public List<String> fetchServiveMappingRecords(String serviceName, String projectId) {
		String query = null;
		List<String> dbRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(serviceName + "_fetch_service_mapping_records");
			logger.info("fetch service mapping records----->" + serviceName + "--->" + query + "-->" + projectId);
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, projectId);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dbRecords.add(rs.getString("MappingId"));
				dbRecords.add(rs.getString("TcidMobileNumberId"));
				dbRecords.add(rs.getString("ServiceNumber"));
				dbRecords.add(rs.getString("SiteId"));
				dbRecords.add(rs.getString("ProjectId"));
				dbRecords.add(rs.getString("StoreName"));
				dbRecords.add(rs.getString("Status"));
			}
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return dbRecords;
	}

	public void clearFileUploadDetails(List<OfferCodeUpload> offerCodeUploads) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance()
					.getValue(offerCodeUploads.get(0).getServiceName() + "_clear_offer_code_file_upload_query");
			logger.info("Insert Query-->" + query);
			pstmt = con.prepareStatement(query);
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public String isMappingExist(OfferCodeUpload offerCodeUpload) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		String brandName = "";
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance()
					.getValue(offerCodeUpload.getServiceName() + "_is_mapping_exist_query");
			logger.info("Insert Query-->" + query + "--->" + offerCodeUpload.getBrandName() + "--->"
					+ offerCodeUpload.getProjectId());
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, offerCodeUpload.getProjectId());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				brandName = rs.getString("StoreName");
			}
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return brandName;
	}

	public boolean isBrandExist(OfferCodeUpload offerCodeUpload) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(offerCodeUpload.getServiceName() + "_is_brand_exist_query");
			logger.info("select Query-->" + query + "--->" + offerCodeUpload.getBrandName() + "--->"
					+ offerCodeUpload.getProjectId());
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, offerCodeUpload.getBrandName());
			pstmt.setString(2, offerCodeUpload.getProjectId());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				String result = rs.getString("result");
				if (!result.equalsIgnoreCase("0")) {
					return true;
				}
			}
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return false;
	}

	public List<String> fetchServiceProjectId(String serviceName, String siteName) {
		String query = null;
		List<String> dbRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(serviceName + "_get_service_project_id");
			logger.info(query);
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, siteName);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dbRecords.add(rs.getString("ProjectId"));
			}
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return dbRecords;
	}

	public void addOfferCodeFileUploadDetailsType2(OfferCodeUpload offerCodeUpload, String status,
			String fileUploadId) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance()
					.getValue(offerCodeUpload.getServiceName() + "_add_offer_code_file_upload_insert_query_type2");
			logger.info("Insert Query-->" + query + "-->" + offerCodeUpload.getServiceName() + "-->"
					+ offerCodeUpload.getBrandInput() + "-->" + offerCodeUpload.getBrandName() + "-->"
					+ offerCodeUpload.getSiteName() + "-->" + offerCodeUpload.getProjectId() + "-->"
					+ offerCodeUpload.getValidInput().substring(1) + "-->" + offerCodeUpload.getOfferCount() + "-->"
					+ status + "-->" + fileUploadId + "-->" + offerCodeUpload.getEmpName());
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, offerCodeUpload.getServiceName());
			pstmt.setString(2, offerCodeUpload.getBrandInput());
			pstmt.setString(3, offerCodeUpload.getBrandName());
			pstmt.setString(4, offerCodeUpload.getSiteName());
			pstmt.setString(5, offerCodeUpload.getProjectId());
			pstmt.setString(6, offerCodeUpload.getValidInput().substring(1));
			pstmt.setString(7, offerCodeUpload.getOfferCount());
			pstmt.setString(8, status);
			pstmt.setString(9, fileUploadId);
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void updateBrandOfferFileRecordsType2(OfferCodeValidInput offerCodeValidInput) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance()
					.getValue(offerCodeValidInput.getServiceName() + "_update_brand_offer_insert_query_type2");
			logger.info("Update Query-->" + query + "-->" + offerCodeValidInput.getProjectId() + "-->"
					+ offerCodeValidInput.getEmpId());
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, offerCodeValidInput.getProjectId());
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void addBrandOfferFileRecordsType2(OfferCodeUpload offerCodeUpload, String siteId) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance()
					.getValue(offerCodeUpload.getServiceName() + "_add_brand_offer_insert_query_type2");
			logger.info("Insert Query-->" + query + "-->" + offerCodeUpload.getServiceName() + "-->"
					+ offerCodeUpload.getBrandInput() + "-->" + offerCodeUpload.getBrandName() + "-->"
					+ offerCodeUpload.getSiteName() + "-->" + siteId + "-->" + offerCodeUpload.getProjectId() + "-->"
					+ offerCodeUpload.getValidInput().substring(1) + "-->" + offerCodeUpload.getOfferCount() + "-->"
					+ offerCodeUpload.getEmpName());
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, offerCodeUpload.getBrandInput());
			pstmt.setString(2, offerCodeUpload.getBrandName());
			pstmt.setString(3, offerCodeUpload.getSiteName());
			pstmt.setString(4, siteId);
			pstmt.setString(5, offerCodeUpload.getProjectId());
			pstmt.setString(6, offerCodeUpload.getValidInput().substring(1));
			pstmt.setString(7, offerCodeUpload.getOfferCount());
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public boolean isBrandExistType2(OfferCodeValidInput offerCodeValidInput) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance()
					.getValue(offerCodeValidInput.getServiceName() + "_is_brand_exist_query_type2");
			logger.info("select Query-->" + query + "--->" + offerCodeValidInput.getProjectId());
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, offerCodeValidInput.getProjectId());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				String result = rs.getString("result");
				if (!result.equalsIgnoreCase("0")) {
					return true;
				}
			}
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return false;
	}

	public boolean isAllowedBrandExist(OfferCodeValidInput offerCodeValidInput) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance()
					.getValue(offerCodeValidInput.getServiceName() + "_is_allowed_brand_exist_query");
			logger.info("select Query-->" + query + "--->" + offerCodeValidInput.getServiceName() + "--->"
					+ offerCodeValidInput.getProjectId());
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, offerCodeValidInput.getProjectId());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				String result = rs.getString("result");
				if (!result.equalsIgnoreCase("0")) {
					return true;
				}
			}
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return false;
	}

	public void updateAllowedBrandMasterRecords(OfferCodeValidInput offerCodeValidInput) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance()
					.getValue(offerCodeValidInput.getServiceName() + "_update_allowed_brand_query");
			logger.info("Update Query-->" + query + "-->" + offerCodeValidInput.getProjectId() + "-->" + "-->"
					+ offerCodeValidInput.getEmpId());
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, offerCodeValidInput.getProjectId());
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void addAllowedBrandMasterRecords(OfferCodeValidInput offerCodeValidInput) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance()
					.getValue(offerCodeValidInput.getServiceName() + "_add_allowed_brand_query");
			logger.info("Insert Query-->" + query + "-->" + offerCodeValidInput.getProjectId() + "-->"
					+ offerCodeValidInput.getValidInput().substring(1) + "-->" + offerCodeValidInput.getAllowedBrand()
					+ "-->" + offerCodeValidInput.getEmpId());
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, offerCodeValidInput.getProjectId());
			pstmt.setString(2, offerCodeValidInput.getValidInput().substring(1));
			pstmt.setString(3, offerCodeValidInput.getAllowedBrand().replace("_", ""));
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

       public void editServiceMaster(String serviceName, String databaseName, String databaseNameOld, String tableName,
			String schedulerStatus, String schedulerDays, int uniqueId, String username) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		try {
			con = this.getConnection();
			if (Integer.parseInt(schedulerStatus) == 1) {
				query = PropertyHandler.getInstance().getValue(serviceName.toLowerCase() + "_enable_servicemaster");
				pstmt = con.prepareStatement(query);
				pstmt.setString(1, databaseName);
				pstmt.setString(2, tableName);
				pstmt.setString(3, schedulerStatus);
				pstmt.setString(4, schedulerDays);
				pstmt.setString(5, username);
				pstmt.setInt(6, uniqueId);
				logger.info("Service Update Query for scheduler status 1 is  " + serviceName + "-->" + query + "-->"
						+ databaseName + "-->" + tableName + "-->" + schedulerStatus + "-->" + schedulerDays + "-->"
						+ username + " ---->" + uniqueId);
				pstmt.executeUpdate();

			} else {
				if (!databaseNameOld.equalsIgnoreCase("")) {
					query = PropertyHandler.getInstance()
							.getValue(serviceName.toLowerCase() + "_disable_servicemaster");
					pstmt = con.prepareStatement(query);
					pstmt.setString(1, schedulerStatus);
					pstmt.setString(2, username);
					pstmt.setInt(3, uniqueId);
					logger.info("Service Update Query for scheduler status 1 is  " + serviceName + "-->" + query + "-->"
							+ "-->" + schedulerStatus + "-->" + username + " ---->" + uniqueId);
					pstmt.executeUpdate();
				} else {
					query = PropertyHandler.getInstance()
							.getValue(serviceName.toLowerCase() + "_db_disable_servicemaster");
					pstmt = con.prepareStatement(query);
					pstmt.setString(1, schedulerStatus);
					pstmt.setString(2, username);
					pstmt.setString(3, databaseName);
					pstmt.setInt(4, uniqueId);
					logger.info("Service Update Query for scheduler status 1 is  " + serviceName + "-->" + query + "-->"
							+ "-->" + schedulerStatus + "-->" + username + "--->>" + databaseName + " ---->"
							+ uniqueId);
					pstmt.executeUpdate();
				}
			}

		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}



              /**
	 * 
	 * @param databaseName get Table name from databasename
	 */
	public List<String> getTableNames(String databaseName) {
		List<String> tableNames = new ArrayList<>();
		Connection con = null;
		String query = null;
		PreparedStatement statement;
		ResultSet resultSet = null;
		String tableName = null;
		String databaseKeyValues[];
		try {
			databaseKeyValues = PropertyHandler.getInstance().getValue("transaction_table_pool").split(",");
			con = this.getConnection();
			query = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = ?";
			statement = con.prepareStatement(query);
			statement.setString(1, databaseName);
			resultSet = statement.executeQuery();
			while (resultSet.next()) {
				tableName = resultSet.getString("TABLE_NAME");
				for (String databaseValue : databaseKeyValues) {
					if (databaseValue.equals(tableName)) {
						tableNames.add(tableName);
					}
				}
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
		return tableNames;
	}


	public String getTrialLimit(OfferCodeUpload offerCodeUpload) {
		String query = null;
		String trial_limit = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(offerCodeUpload.getServiceName() + "_get_trial_limit_query");
			logger.info("select Query-->" + query + "--->" + offerCodeUpload.getProjectId());
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, offerCodeUpload.getProjectId());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				trial_limit = rs.getString("trial_limit");
			}
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return trial_limit;
	}

	public int getTrialLimit(OfferCodeUpload offerCodeUpload, String projectId, String key) {
		String query = null;
		int trialLimit = 0;
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(offerCodeUpload.getServiceName() + key);
			logger.info("select Query-->" + query + "--->" + offerCodeUpload.getProjectId());
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, Integer.parseInt(projectId));
			rs = pstmt.executeQuery();
			while (rs.next()) {
				trialLimit = rs.getInt("trialLimit");
			}
			if (trialLimit == 0) {
				trialLimit = getTrialLimit(offerCodeUpload, projectId, "_get_trial_limit_serviceconfiguration_query");
			}
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return trialLimit;
	}

	public void addDefaultAllowedInputPSM22052023(OfferCodeValidInput offerCodeValidInput) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance()
					.getValue(offerCodeValidInput.getServiceName() + "_add_default_allowed_brand_input_query");
			logger.info("Update Query-->" + query + "-->" + offerCodeValidInput.getValidInput().substring(1) + "-->"
					+ offerCodeValidInput.getProjectId() + "-->" + offerCodeValidInput.getEmpId());
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, offerCodeValidInput.getValidInput().substring(1));
			pstmt.setString(2, offerCodeValidInput.getProjectId());
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void addDefaultAllowedInputPSM(OfferCodeUpload offerCodeUpload) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance()
					.getValue(offerCodeUpload.getServiceName() + "_add_default_allowed_brand_input_query");
			logger.info("Update Query-->" + query + "-->" + offerCodeUpload.getAllowedInput().substring(1) + "-->"
					+ offerCodeUpload.getProjectId() + "-->" + offerCodeUpload.getEmpName());
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, offerCodeUpload.getAllowedInput().substring(1));
			pstmt.setString(2, offerCodeUpload.getProjectId());
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public String getSiteIdFromPSM(OfferCodeUpload offerCodeUpload) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		String site_id = "0";
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(offerCodeUpload.getServiceName() + "_get_siteid_from_psm");
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, offerCodeUpload.getProjectId());
			pstmt.setString(2, offerCodeUpload.getSiteName());
			logger.info("select Query-->" + query + "--->" + offerCodeUpload.getProjectId() + "--->"
					+ offerCodeUpload.getSiteName());
			rs = pstmt.executeQuery();
			while (rs.next()) {
				site_id = rs.getString("site_id");
			}
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return site_id;
	}

	public void dailyServiceReportsDtc(String startDate, String endDate, String serviceName) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		String[] reportHeaders = PropertyHandler.getInstance().getValue(serviceName + "_dailyReportDtcHeaders")
				.split(",");
		logger.info("ReportHeaders-->" + PropertyHandler.getInstance().getValue(serviceName + "_dailyReportDtcHeaders")
				+ "-->" + reportHeaders.length);
		Sheet sheet = null;
		int rowNum = 1;
		Workbook workbook = null;
		CreationHelper createHelper = null;
		Font headerFont = null;
		CellStyle headerCellStyle = null;
		Row headerRow = null;
		Connection con = null;
		String[] brandNameArr;
		int recordsCount = 0;
		String dtcFileName;
		try {
			logger.info("FilePath-->" + PropertyHandler.getInstance().getValue("dtcReportFilePath")
					+ PropertyHandler.getInstance().getValue(serviceName + "_dtcReportFileName"));
			dtcFileName = PropertyHandler.getInstance().getValue(serviceName + "_dtcReportFileName")
					.replace("%DATETIME", Utility.getCurrentDatetime().replace(":", "-"));
			con = this.getConnection();
			workbook = new XSSFWorkbook();
			createHelper = workbook.getCreationHelper();
			sheet = workbook.createSheet("DailyReport");
			// Create a Font for styling header cells
			headerFont = workbook.createFont();
			headerFont.setBold(true);
			headerFont.setFontHeightInPoints((short) 14);
			headerFont.setColor(IndexedColors.RED.getIndex());
			// Create a CellStyle with the font
			headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setFont(headerFont);
			// Create a Row
			headerRow = sheet.createRow(0);
			// Create cells
			for (int j = 0; j < reportHeaders.length; j++) {
				Cell cell = headerRow.createCell(j);
				cell.setCellValue(reportHeaders[j]);
				cell.setCellStyle(headerCellStyle);
			}
			query = PropertyHandler.getInstance().getValue(serviceName + "_dailyReportDtcQuery");
			logger.info(query + "-->" + startDate + "-->" + endDate);
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, startDate + " 00:00:00");
			pstmt.setString(2, endDate + " 23:59:59");
			rs = pstmt.executeQuery();
			while (rs.next()) {
				recordsCount = recordsCount + 1;
				// logger.info("MissCallTime-->"+rs.getString("MissCallTime")+"-->"+rs.getString("BrandName"));
				brandNameArr = rs.getString("BrandName").replace("+", "_").split("_");
				for (int i = 0; i < brandNameArr.length; i++) {
					Row row = sheet.createRow(rowNum++);
					row.createCell(0).setCellValue(rs.getString("MobileNumber"));
					row.createCell(1).setCellValue(rs.getString("MissCallTime").split("\\.")[0]);
					row.createCell(2).setCellValue(rs.getString("CallerId"));
					row.createCell(3).setCellValue(rs.getString("SiteName"));
					row.createCell(4).setCellValue(brandNameArr[i]);
					row.createCell(5).setCellValue(rs.getString("ChassisName"));
					row.createCell(6).setCellValue(rs.getString("OptinStatus"));
				}
			}
			for (int j = 0; j < reportHeaders.length; j++) {
				sheet.autoSizeColumn(j);
			}
			FileOutputStream fileOut = new FileOutputStream(
					PropertyHandler.getInstance().getValue("dtcReportFilePath") + dtcFileName);
			workbook.write(fileOut);
			fileOut.close();
			query = "insert ignore into _tbl_dtcreportsummary_details(SummaryDate,ServiceName,TrialsCount,FileName) values (?,?,?,?)";
			logger.info("Insert Summary Query-->" + query + "-->"
					+ Utility.getCurrentDatetime().replace("T", " ").split(" ")[0] + "-->" + serviceName + "-->"
					+ recordsCount);
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, Utility.getCurrentDatetime().replace("T", " ").split(" ")[0]);
			pstmt.setString(2, serviceName);
			pstmt.setInt(3, recordsCount);
			pstmt.setString(4, dtcFileName);
			pstmt.executeUpdate();
			recordsCount = 0;
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception ex) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + ex + Arrays.asList(ex.getStackTrace())
						.stream().map(Objects::toString).collect(Collectors.joining("\n")));
			}
		}
	}

	public void addOfferCodeBrand(String[] fileDataArr, OfferCodeUpload offerCodeUpload, String fileUploadId) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		ResultSet rs = null;
		String brandName = "NA";
		String[] brandNameArr;
		int siteId = 0;
		int projectId = 0;
		int validateProjectId = 0;
		int recordsCounter = 0;
		int batchSize = 20;
		String uploadStatus = "NA";
		String siteName = "NA";
		try {
			System.out.println("fileDataArr.length--->>>>> is "+fileDataArr.length);
			con = this.getConnection();
			for (int i = 1; i < fileDataArr.length; i++) {
				String element = fileDataArr[i].replaceAll(",'" + fileUploadId, ",");
				List<String> excelList = Arrays.asList(element.split(","));
				logger.info("Excel Data-->" + excelList.get(0) + "-->" + excelList.get(1) + "-->" + excelList.get(2));
				brandName = URLDecoder.decode(excelList.get(0).replaceAll("_{2,}", "_"), "UTF-8");
				brandNameArr = brandName.split("@");
				System.out.println("============== Site name is "+siteName+" projectId --->> "+projectId+" siteId--->> "+siteId);
				siteName = URLDecoder.decode(excelList.get(1), "UTF-8");
				projectId = Integer.parseInt(excelList.get(2));
				siteId = getSiteId(offerCodeUpload.getServiceName(), siteName);
				validateProjectId = vaidateProjectSiteMapping(offerCodeUpload.getServiceName(), projectId, siteId);
				logger.info("Brand Details For ServiceName-->" + offerCodeUpload.getServiceName() + "-->" + brandName
						+ "-->" + brandNameArr.length);
				if (siteId == 0) {
					uploadStatus = "Invalid Site Name / Site Name not mapped with ProjectId";
				} else if (validateProjectId == 0) {
					uploadStatus = "Site Name not mapped with ProjectId";
				} else {
					uploadStatus = "Inserted Successfully";
				}
				query = PropertyHandler.getInstance()
						.getValue(offerCodeUpload.getServiceName() + "_add_offer_code_file_upload_insert_query_type2");
				pstmt = con.prepareStatement(query);
				for (int j = 0; j < brandNameArr.length; j++) {
					pstmt.setString(1, offerCodeUpload.getServiceName());
					pstmt.setInt(2, (j + 1));
					pstmt.setString(3, brandNameArr[j].toLowerCase());
					pstmt.setString(4, siteName);
					pstmt.setInt(5, siteId);
					pstmt.setInt(6, projectId);
					pstmt.setInt(7, brandNameArr[j].split("_").length);
					pstmt.setString(8, uploadStatus);
					pstmt.setString(9, fileUploadId);
					pstmt.addBatch();
					if (++recordsCounter % batchSize == 0) {
						pstmt.executeBatch();
					}
				}
				pstmt.executeBatch();
				query = PropertyHandler.getInstance().getValue(offerCodeUpload.getServiceName() + "_update_ivrflow");
				pstmt = con.prepareStatement(query);
				System.out.println("offerCodeUpload.getServiceName()--->>>> "+offerCodeUpload.getServiceName()+" brandName.toLowerCase()--->>> "+brandName.toLowerCase()+" projectId--->> "+projectId);
				pstmt.setString(1, brandName.toLowerCase());
				pstmt.setInt(2, projectId);
				pstmt.executeUpdate();
				addBrandConfiguration(offerCodeUpload.getServiceName(), Integer.toString(projectId),
						Integer.toString(siteId), brandName.toLowerCase());
			}
		} catch (Exception exception) {
			logger.error(query + "-->" + exception + Arrays.asList(exception.getStackTrace()).stream()
					.map(Objects::toString).collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	/**
	 * Fetch Site ID From Master Table
	 * 
	 * @return
	 */

	public int getFlowId(String serviceName, String brandName) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		int flowId = 0;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue(serviceName + "_get_flowId");
			logger.info(query + "-->" + brandName);
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, brandName);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				flowId = rs.getInt("flowId");
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return flowId;
	}

	public int insertOtp(String emailId, int otp) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		int validateFlag = 0;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue("insertotp_query");
			logger.info(query + "-->" + emailId + "-->" + otp);
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, emailId);
			pstmt.setInt(2, otp);
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return validateFlag;
	}

	public int fetchOtp(String emailId) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		int otp = 0;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue("fetchOtp_query");
			logger.info(query + "-->" + emailId);
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, emailId);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				otp = rs.getInt("otp");
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return otp;
	}

	public int otpValidateSubmit(String emailId, int otp) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		int existCheck = 0;
		int validityCheck=0;
		int validateOtp=0;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue("checkOtp_query");
			logger.info(query + "-->" + emailId + "-->" + otp);
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, emailId);
			pstmt.setInt(2, otp);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				existCheck = rs.getInt("count");
				validityCheck=rs.getInt("validity");
			}
			if(existCheck==0 && validityCheck==0) {
				validateOtp=1;   //Invalid Otp
			}else if(existCheck==1 && validityCheck==0) {
				validateOtp=2;    //Expired Otp
			}else if(existCheck==1 && validityCheck==1 ) {
				validateOtp=3;  //Success
			}
			if (validateOtp ==2) {
				query = PropertyHandler.getInstance().getValue("updateOtp_expired_query");
				logger.info(query + "-->" + emailId + "-->" + otp);
				pstmt = con.prepareStatement(query);
				pstmt.setString(1, emailId);
				pstmt.setInt(2, otp);
				pstmt.executeUpdate();
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return validateOtp;
	}

	
	public int validateOtp(String emailId, int otp) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		int validateFlag = 0;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue("validateOtp_query");
			logger.info(query + "-->" + emailId + "-->" + otp);
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, emailId);
			pstmt.setInt(2, otp);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				validateFlag = rs.getInt("count");
			}
			if (validateFlag == 0) {
				query = PropertyHandler.getInstance().getValue("updateOtp_expired_query");
				logger.info(query + "-->" + emailId + "-->" + otp);
				pstmt = con.prepareStatement(query);
				pstmt.setString(1, emailId);
				pstmt.setInt(2, otp);
				pstmt.executeUpdate();
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return validateFlag;
	}
	
	
	public void updateOtp(int validateFlag, String emailId, int otp) {
		String query = null;
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue("updateOtp_valid_query");
			logger.info(query + "-->" + emailId + "-->" + otp);
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, emailId);
			pstmt.setInt(2, otp);
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public String getDtcSummary(String startDate, String endDate) {
		String query = null;
		List<String> dbRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = "select SummaryDate,ServiceName,TrialsCount from _tbl_dtcreportsummary_details where (SummaryDate>=? and SummaryDate<=?) and Status=1";
			logger.info(query + "-->" + startDate + "-->" + endDate);
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, startDate);
			pstmt.setString(2, endDate);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dbRecords.add(rs.getString("SummaryDate") + "@" + rs.getString("ServiceName") + "@"
						+ rs.getInt("TrialsCount"));
			}
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return dbRecords.toString().replace("[", "").replace("]", "").replace(" ,", ",").replace(", ", ",");
	}




// =====================================JIRA 930=========================

	public void addUserProfileDetails(String userName, String mobileNumber, String email,
			String[] readModules, String[] readWriteModules) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		String read = "";
		String readWrite = "";
		String readModule = null;
		String readWriteModule = null;
		String password =null;
		try {
			password = Utility.generateRandomPassword(8);
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue("insertUserProfile");
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, userName);
			pstmt.setString(2, mobileNumber);
			pstmt.setString(3, email);
			pstmt.setString(4, password);
			if (readModules != null) {
				for (String moduleRead : readModules) {
					read += moduleRead + ",";
					int lastIndex = read.lastIndexOf(",");
					readModule = read.substring(0, lastIndex) + read.substring(lastIndex + 1);
					pstmt.setString(5, readModule);
				}
			} else {
				pstmt.setString(5, "NA");
			}
			if (readWriteModules != null) {
				for (String moduleReadWrite : readWriteModules) {
					readWrite += moduleReadWrite + ",";
					int lastIndex = readWrite.lastIndexOf(",");
					readWriteModule = readWrite.substring(0, lastIndex) + readWrite.substring(lastIndex + 1);
					pstmt.setString(6, readWriteModule);
				}
			} else {
				pstmt.setString(6, "NA");
			}
			logger.info("User Profile Insert Query for " + query + "-->" + userName + mobileNumber + "-->" + email
					+ "-->" + password);
			logger.info("Read module is --->>> "+readModule);
			logger.info("Read Write Module is --->>>> "+readWriteModule);
			pstmt.executeUpdate();
			Utility.sendUserMail(email, password);
			
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void editUser(String userName, String mobileNumber, String[] checkboxRead,
			String[] checkboxReadWrite, String uniqueId) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		String read = "";
		String readWrite = "";
		String readModule = null;
		String readWriteModule = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue("updateUser");
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, mobileNumber);
//			if (checkboxRead != null) {
//				for (String moduleRead : checkboxRead) {
//					read += moduleRead + ",";
//					int lastIndex = read.lastIndexOf(",");
//					readModule = read.substring(0, lastIndex) + read.substring(lastIndex + 1);
//					System.out.println("read module is --->>> "+readModule);
//					pstmt.setString(4, readModule);
//				}
//			} else {
//				pstmt.setString(4, "NA");
//			}
//			if (checkboxReadWrite != null) {
//				for (String moduleReadWrite : checkboxReadWrite) {
//					readWrite += moduleReadWrite + ",";
//					int lastIndex = readWrite.lastIndexOf(",");
//					readWriteModule = readWrite.substring(0, lastIndex) + readWrite.substring(lastIndex + 1);
//					System.out.println("read & write module is ====>>> "+readWriteModule);
//					pstmt.setString(5, readWriteModule);
//				}
//			} else {
//				pstmt.setString(5, "NA");
//			}
			 if (checkboxRead != null) {
	                read = String.join(",", checkboxRead);
	            } else {
	                read = "NA";
	            }

	            if (checkboxReadWrite != null) {
	                readWrite = String.join(",", checkboxReadWrite);
	            } else {
	                readWrite = "NA";
	            }
	            pstmt.setString(2, read);
	            pstmt.setString(3, readWrite);
			pstmt.setString(4, read+","+readWrite);
			pstmt.setString(5, uniqueId);
			logger.info("User Management Update Query for " + mobileNumber + "-->" + query + "-->" 
					 + uniqueId);
			logger.info("Read module is --->>> "+readModule);
			logger.info("Read Write Module is --->>>> "+readWriteModule);
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

//	public List<String> fetchSideMenuParentData() {
//		PreparedStatement pstmt = null;
//		String query = null;
//		Connection con = null;
//		ResultSet resultSet = null;
//		ArrayList<String> parentList = new ArrayList<>();
//		try {
//			con = this.getConnection();
//			query = "select ParentName,ParentId from _tbl_sidemenuparent_details where status=1";
//			pstmt = con.prepareStatement(query);
//			resultSet = pstmt.executeQuery();
//			while (resultSet.next()) {
//				parentList.add(resultSet.getString("ParentName"));
//			}
//			logger.debug(parentList);
//
//		} catch (Exception exception) {
//			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
//					.collect(Collectors.joining("\n")));
//		} finally {
//			try {
//				query = null;
//				if (pstmt != null) {
//					pstmt.close();
//					pstmt = null;
//				}
//				if (con != null) {
//					con.close();
//				}
//			} catch (Exception e) {
//				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
//			}
//		}
//		return parentList;
//	}

//	public Map<String,HashMap<String,SideMenu>>  fetchSideData(String userEmail) {
//		PreparedStatement pstmt = null;
//		String query = null;
//		Connection con = null;
//		ResultSet resultSet = null;
//		HashMap<String,SideMenu> sideMenuDataList = new HashMap<>();
//		HashMap<String,SideMenu> nameLinkMap=new HashMap<>();
//		SideMenu sideData = null;
//		try {
//			con = this.getConnection();
//			query = "SELECT spd.parentname,GROUP_CONCAT(DISTINCT sd.PageName) as PageName,GROUP_CONCAT(DISTINCT sd.PageTitle) as PageTitle,GROUP_CONCAT(DISTINCT sd.PageLink) as PageLink,sd.Class FROM _tbl_sidemenu_details sd INNER JOIN _tbl_userprofile_details ud ON FIND_IN_SET(sd.pagename, ud.readmodules) > 0 OR FIND_IN_SET(sd.pagename, ud.readwritemodules) > 0 inner join _tbl_sidemenuparent_details spd on sd.parentid=spd.parentid WHERE sd.status = 1 and email=? group by 1 order by spd.parentid";
//			pstmt = con.prepareStatement(query);
//			pstmt.setString(1, userEmail);
//			resultSet = pstmt.executeQuery();
//			logger.info("Query is --->>> "+query+" and user email is --->> "+userEmail);
//			while (resultSet.next()) {
//			    sideData = new SideMenu();
//				sideData.setParentName(resultSet.getString("parentname"));
//				sideData.setPageName(resultSet.getString("PageName"));
//				sideData.setPageTitle(resultSet.getString("PageTitle"));
//				sideData.setPageLink(resultSet.getString("PageLink"));
//				sideData.setPageClass(resultSet.getString("Class"));
//				
//				
//			}
//			sideMenuDataList.put("sideMenuData", sideData);
//			nameLinkMap.put("pageLinkData", sideData);		
//			
//		} catch (Exception exception) {
//			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
//					.collect(Collectors.joining("\n")));
//		} finally {
//			try {
//				query = null;
//				if (pstmt != null) {
//					pstmt.close();
//					pstmt = null;
//				}
//				if (con != null) {
//					con.close();
//				}
//			} catch (Exception e) {
//				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
//			}
//		}
//		Map<String, HashMap<String,SideMenu>> result = new HashMap<>();
//	    result.put("sideMenuData", sideMenuDataList);
//	    result.put("pageLinkData", nameLinkMap);
//	    return result;
//	}

//	

	public SideMenuData fetchSideData(String userEmail,String instanceId) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		ResultSet resultSet = null;
		List<SideMenu> sideMenuDataList = new ArrayList<>();
		HashMap<String, SideMenu> pageLinkMap = new HashMap<>();
		try {
			con = this.getConnection();
			query = "SELECT spd.parentname,GROUP_CONCAT(sd.PageName order by sd.id) as PageName,GROUP_CONCAT(sd.PageTitle order by id) as PageTitle,GROUP_CONCAT(sd.PageLink order by sd.id) as PageLink,sd.Class,GROUP_CONCAT(sd.subpagelink order by sd.id) as SubPageLink FROM _tbl_sidemenu_details sd INNER JOIN _tbl_userprofile_details ud ON FIND_IN_SET(sd.pagename, ud.readmodules) > 0 OR FIND_IN_SET(sd.pagename, ud.readwritemodules) > 0 inner join _tbl_sidemenuparent_details spd on sd.parentid=spd.parentid WHERE sd.status = 1 and email=? group by 1 order by spd.parentid";
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, userEmail);
			resultSet = pstmt.executeQuery();
			logger.info("InstanceId is --->>> "+instanceId+"  Query is --->>> " + query + " and useremail is  is --->> " + userEmail);
			while (resultSet.next()) {
				SideMenu sideData = new SideMenu();
				sideData.setParentName(resultSet.getString("parentname"));
				sideData.setPageName(resultSet.getString("PageName"));
				sideData.setPageTitle(resultSet.getString("PageTitle"));
				sideData.setPageLink(resultSet.getString("PageLink"));
				sideData.setPageClass(resultSet.getString("Class"));
				sideData.setSubPageLink(resultSet.getString("SubPageLink"));
				sideMenuDataList.add(sideData);
				pageLinkMap.put(sideData.getPageName(), sideData);
			}

		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		SideMenuData sideMenuData = new SideMenuData();
		sideMenuData.setSideMenuDataMap(pageLinkMap);
		sideMenuData.setSideMenuDataList(sideMenuDataList);

		return sideMenuData;
	}

//	public boolean checkPermission(String userName, String pageName) {
//		PreparedStatement pstmt = null;
//		String query = null;
//		Connection con = null;
//		ResultSet resultSet = null;
//		int count = 0;
//		boolean hasPermission = false;
//		try {
//			String pageData = PropertyHandler.getInstance().getValue(pageName);
//			con = this.getConnection();
//			query = "SELECT count(readwritemodules) as count FROM _tbl_userprofile_details WHERE FIND_IN_SET(?, readwritemodules) > 0 and username=? ";
//			pstmt = con.prepareStatement(query);
//			pstmt.setString(1, pageData);
//			pstmt.setString(2, userName);
//			resultSet = pstmt.executeQuery();
//			while (resultSet.next()) {
//				count = resultSet.getInt("count");
//				if (count > 0) {
//					hasPermission = true;
//				}
//			}
//			logger.info("query is --->>> " + query + " page data is ---->>> " + pageData + " and username is --->>> "
//					+ userName);
//		} catch (Exception exception) {
//			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
//					.collect(Collectors.joining("\n")));
//		} finally {
//			try {
//				query = null;
//				if (pstmt != null) {
//					pstmt.close();
//					pstmt = null;
//				}
//				if (con != null) {
//					con.close();
//				}
//			} catch (Exception e) {
//				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
//			}
//		}
//		return hasPermission;
//	}

//	public HashMap<String,String[]> fetchChildData(String parentName) {
//		PreparedStatement pstmt = null;
//		String query = null;
//		Connection con = null;
//		ResultSet resultSet = null;
//		HashMap<String,String[]> pageNameMap=new HashMap<>();
//		try {
//			con = this.getConnection();
//			query = "select sd.pagename as pagename from _tbl_sidemenu_details sd inner join _tbl_sidemenuparent_details spd on spd.parentid=sd.parentid where spd.parentname=?";
//			pstmt = con.prepareStatement(query);
//			pstmt.setString(1, parentName);
//			resultSet = pstmt.executeQuery();
//			 List<String> childPageNames = new ArrayList<>();
//			while (resultSet.next()) {
//				childPageNames.add(resultSet.getString("pagename"));
//			}
//			String[] childArray = childPageNames.toArray(new String[0]);
//			 pageNameMap.put(parentName, childArray);
//
//		} catch (Exception exception) {
//			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
//					.collect(Collectors.joining("\n")));
//		} finally {
//			try {
//				query = null;
//				if (pstmt != null) {
//					pstmt.close();
//					pstmt = null;
//				}
//				if (con != null) {
//					con.close();
//				}
//			} catch (Exception e) {
//				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
//			}
//		}
//		return pageNameMap;
//	}

	public LinkedHashMap<String, String[]> fetchChildData() {
		LinkedHashMap<String, String[]> pageNameMap = new LinkedHashMap<>();
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		ResultSet resultSet = null;
		try {
			con = this.getConnection();

			query = "select group_concat(sd.pagename) as pagename,spd.parentname from _tbl_sidemenu_details sd inner join _tbl_sidemenuparent_details spd on spd.parentid=sd.parentid where spd.status=1 group by 2 ORDER BY spd.parentid";
			pstmt = con.prepareStatement(query);
			resultSet = pstmt.executeQuery();

			while (resultSet.next()) {
				String parentName = resultSet.getString("parentname");
				String pagename = resultSet.getString("pagename");
				String[] childArray = pagename.split(",");
				pageNameMap.put(parentName, childArray);
			}
			pstmt.close();

		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + "-->" + e);
			}
		}
		return pageNameMap;
	}

	public boolean isChildSelected(List<String> preSelected, String parent) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		ResultSet resultSet = null;
		String pageName = null;
		boolean found = false;
		try {
            if(!parent.equals("Reports") && !parent.equals("Service Config")) {
			con = this.getConnection();
			query = "select group_concat(pagename) as PageName from _tbl_sidemenu_details sd inner join _tbl_sidemenuparent_details spd on sd.parentid=spd.parentid where spd.parentname=?";
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, parent);
			resultSet = pstmt.executeQuery();
			logger.info("query is --->>> " + query + " parent name is ---->>> " + parent);

			while (resultSet.next()) {
				pageName = resultSet.getString("PageName");
			}
			found = Arrays.stream(pageName.split(",")).anyMatch(preSelected::contains);
            }
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return found;

	}

	public void enableUserProfile(int uniqueId) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue("enable_user");
			logger.info("Enable User Query-->" + query + "-->" + uniqueId);
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, uniqueId);
			pstmt.executeUpdate();
			UserTypeRepository.reLoadRepository();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}

	public void disableUserProfile(int uniqueId) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = PropertyHandler.getInstance().getValue("disable_user");
			logger.info("Disable User Query-->" + query + "-->" + uniqueId);
			pstmt = con.prepareStatement(query);
			pstmt.setInt(1, uniqueId);
			pstmt.executeUpdate();
			UserTypeRepository.reLoadRepository();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
	}
	

	public String checkEmail(String email) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		ResultSet resultSet=null;
		int count=0;
		String exist=null;
		try {
			con = this.getConnection();
			query = "select count(*) as count from _tbl_userprofile_details where email=? and status=1";
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, email);
			resultSet=pstmt.executeQuery();
		    while(resultSet.next()) {
		    	count=resultSet.getInt("count");
		    	if(count==1) {
		    		exist="exists";
		    	}else {
		    		exist="not exists";
		    	}
		    }
			
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return exist;
		
	}
	
	
	public int warningMessage(String email,String password) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		ResultSet resultSet=null;
		String passwordExists="";
		String user="";
		String passwordStatus="";
		int validate =0;
		try {
			con = this.getConnection();
			//query = "select password from _tbl_userprofile_details where DATEDIFF(NOW(), passwordexpirydate) >45 and email=?";
			query="SELECT password,CASE WHEN DATEDIFF(NOW(), passwordexpirydate) > 45 THEN 'Expired' ELSE 'Not Expired' END AS password_status,CASE WHEN status=1 then 'Active' ELSE 'Inactive' END AS user FROM _tbl_userprofile_details WHERE email = ? and password = ?";
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, email);
			pstmt.setString(2, password);
			resultSet=pstmt.executeQuery();
		    while(resultSet.next()) {
		    	passwordExists=resultSet.getString("password");
		    	user=resultSet.getString("user");
		    	passwordStatus=resultSet.getString("password_status");
		    }
		    System.out.println("passwordExists-->>"+passwordExists+" user --->> "+user +" passwordStatus --->> "+passwordStatus);
			if(user.equalsIgnoreCase("Inactive")) {
				System.out.println("Inactive User");
				validate=0;
			}else if(passwordExists.equalsIgnoreCase("")) {
				System.out.println("if 1");
				validate=1;
			}else if(passwordStatus.equalsIgnoreCase("Expired")) {
				System.out.println("if 2");
				validate=2;
			}else if(user.equalsIgnoreCase("Active") && !password.equalsIgnoreCase("") && passwordStatus.equalsIgnoreCase("Not Expired")){
				System.out.println("if success");
				validate=3;
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		//return passwordExists+"@"+passwordStatus+"@"+user;
	     return  validate;
	}
	
	public int checkEmailExists(String email) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		ResultSet resultSet=null;
		int emailExists=0;
		try {
			con = this.getConnection();
			query = "select count(*) as count from _tbl_userprofile_details where email=?";
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, email);
			resultSet=pstmt.executeQuery();
		    while(resultSet.next()) {
		    	emailExists=resultSet.getInt("count");
		    	
		    }
			
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return emailExists;
	

	}

	
	
	public long fetchSmsCampaignCount(String serviceName) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		ResultSet resultSet = null;
		long smsCount=0;
		long emailCount=0;
		try {
			con = this.getConnection();
			query = "select count(*) as SmsCount from pixeldb._tbl_pixelmsg_details where servicename=?";
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, serviceName);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				smsCount=resultSet.getInt("SmsCount");
			}
			logger.info("Query is "+query+ " and sms count is "+smsCount);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return smsCount;
		}
	
	
	public long fetchEmailCampaignCount(String serviceName) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		ResultSet resultSet = null;
		long emailCount=0;
		try {
			con = this.getConnection();
			query = "select count(*) as EmailCount from pixeldb._tbl_pixelemail_details where servicename=?";
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, serviceName);
			resultSet = pstmt.executeQuery();
			while (resultSet.next()) {
				emailCount=resultSet.getInt("EmailCount");
			}
			logger.info("Query is "+query+" and email count is "+emailCount);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return emailCount;
		}
	
	
	public List<String> fetchCampaigns() {
		String query = null;
		List<String> dbRecords = new ArrayList<>();
		PreparedStatement pstmt = null;
		ResultSet rs = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = "select CampaignName from pixeldb._tbl_campaign_master where status=1";
			logger.info(query);
			pstmt = con.prepareStatement(query);
			rs = pstmt.executeQuery();
			while (rs.next()) {
				dbRecords.add(rs.getString("CampaignName"));
			}
			logger.debug(dbRecords);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		return dbRecords;
	}

	public void addPixelCampaign(String campaignName,String smsStatus, String emailStatus) {
		String query = null;
		PreparedStatement pstmt = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = "Insert ignore into pixeldb._tbl_campaign_master(campaignName,smsStatus,emailStatus) values(?,?,?)";
			pstmt = con.prepareStatement(query);
			logger.info(query);
			pstmt.setString(1, campaignName);
			pstmt.setString(2, smsStatus);
			pstmt.setString(3, emailStatus);
			pstmt.executeUpdate();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}

		
	}

	public void editCampaign(String campaignName, String uniqueId, String status, String smsStatus,
			String emailStatus) {
		PreparedStatement pstmt = null;
		String query = null;
		Connection con = null;
		try {
			con = this.getConnection();
			query = "Update pixeldb._tbl_campaign_master set status=?,SmsStatus=?,EmailStatus=?,UpdateDate=now() where CampaignId=?";
			pstmt = con.prepareStatement(query);
			pstmt.setString(1, status);
			pstmt.setString(2, smsStatus);
			pstmt.setString(3, emailStatus);
			pstmt.setString(4, uniqueId);
			pstmt.executeUpdate();
			logger.info("Campaign Update Query for " + campaignName + "-->" + query + "-->" + status + "-->"
					+ smsStatus + "-->" + emailStatus + "-->" 
					+ uniqueId);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {
			try {
				query = null;
				if (pstmt != null) {
					pstmt.close();
					pstmt = null;
				}
				if (con != null) {
					con.close();
				}
			} catch (Exception e) {
				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
			}
		}
		
	}

	
	
//	public void updatePasswordAfter30Days(String empId) {
//		PreparedStatement pstmt = null;
//		String query = null;
//		Connection con = null;
//		try {
//			con = this.getConnection();
//			query = "update _tbl_userprofile_details set password='defaultpassword' where DATEDIFF(NOW(), if(updatedate is null,createdate,updatedate)) >30 and userid=?";
//			pstmt = con.prepareStatement(query);
//			pstmt.setString(1, empId);
//			pstmt.executeUpdate();
//			logger.info("Query --->>> "+query+" and password is ---->>> "+newPassword+" and userid is-->>"+empId);
//		} catch (Exception exception) {
//			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
//					.collect(Collectors.joining("\n")));
//		} finally {
//			try {
//				query = null;
//				if (pstmt != null) {
//					pstmt.close();
//					pstmt = null;
//				}
//				if (con != null) {
//					con.close();
//				}
//			} catch (Exception e) {
//				logger.error(Constants.FINALLYEXCEPTION + query + "-->" + e);
//			}
//		}
//	}

}
