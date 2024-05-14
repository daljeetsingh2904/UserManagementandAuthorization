	package com.techmobia.supportmanager.controller;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.techmobia.supportmanager.services.*;
import com.techmobia.supportmanager.model.*;

/**
 * Servlet implementation class LoginController
 */
public class LoginController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private static final Logger logger = Logger.getLogger(LoginController.class);

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public LoginController() {
		super();
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		try {
			response.getWriter().append("Served at: ").append(request.getContextPath());
		} catch (Exception e) {
			logger.error(e);
		}
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse
	 *      response)
	 */


	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		HttpSession session = request.getSession();
		PropertyConfigurator.configure(Constants.LOG4JFILEPATH);
		//String userName = null;
               String email=null;
		String password = null;
		Login login = new Login();
		try {
			email = Utility.verifyData(request.getParameter("email"));
			password = Utility.verifyData(request.getParameter("password"));
			String remoteAddr = request.getHeader("X-FORWARDED-FOR");
			if (remoteAddr == null || "".equals(remoteAddr)) {
				remoteAddr = request.getRemoteAddr();
			}
			System.out.println("Remote=" + remoteAddr);
			/*
			 * int validateIpAddress=DbHandler.getInstance().validateIpAddress(remoteAddr);
			 * if(validateIpAddress==0) {
			 * logger.info("Ip Address Not Whitelisted for UserName-->"+userName);
			 * response.sendRedirect(PropertyHandler.getInstance().getValue(
			 * "failed_login_page")); }else {
			 */
			login = DbHandler.getInstance().validateLogin(email, password);
			if (null == login) {
				response.sendRedirect(PropertyHandler.getInstance().getValue("failed_login_page"));
			} else {
				logger.info(login.getEmpId() + "-->" + login.getUserName() + "-->" + login.getUserPassword() + "-->"
						+ login.getMobileNumber() + "-->" + login.getUserEmail() + "-->" + login.getUserType());
				
                                 logger.info("Current Login User is "+login.getUserEmail());    
                     long instanceId = System.currentTimeMillis();
                    
                     logger.info("Current Login User Instance Id is "+instanceId);
                               session.setAttribute("empId", login.getEmpId());
				session.setAttribute("userName", login.getUserName());
				session.setAttribute("userPassword", login.getUserPassword());
				session.setAttribute("userMobileNumber", login.getMobileNumber());
				session.setAttribute("userType", login.getUserType());
				session.setAttribute("userEmail", login.getUserEmail());
                               session.setAttribute("readwriteValues", login.getReadWriteModules());
    				session.setAttribute("readValues", login.getReadModules());
    				session.setAttribute("instanceId", instanceId);
    				
    				login.setInstanceId(Long.toString(instanceId));
    				session.setAttribute("login", login);
				if (login.getUserType() != null) {
					if (login.getUserType().equals("pampersReport")) {
						response.sendRedirect("reporting_pampersSummary.jsp");
					} else if (login.getUserType().equals("pampersBandhanReport")) {
						response.sendRedirect("reporting_pampersBandhanSummary.jsp");
					} else if (login.getUserType().equals("mindtreelatching")) {
						response.sendRedirect("reporting_mindtreeLatchingSummary.jsp");
					} else if (login.getUserType().equals("gillettePomeReport")) {
						response.sendRedirect("reporting_gillettePomeData.jsp");
					} else {
						//response.sendRedirect(PropertyHandler.getInstance().getValue("success_login_page"));
						System.out.println("===========swag daljeet=====");
						if(login.getUserEmail().indexOf("vfirst.com")!=-1) {
    						int otp=Utility.generateOtp(login.getUserEmail());
    						Utility.sendMail("NA", "NA", login.getUserEmail(), "NA", "NA", PropertyHandler.getInstance().getValue("fromOtpMail"), "0", "otp", PropertyHandler.getInstance().getValue("otpMailSubject"), PropertyHandler.getInstance().getValue("otpMailContent"), 0, "Otp",otp);
    						response.sendRedirect(PropertyHandler.getInstance().getValue("success_otp_page"));
    					}else {
    						response.sendRedirect(PropertyHandler.getInstance().getValue("success_login_page"));
    					}
					}
				} else {
					response.sendRedirect(request.getHeader("Referer"));
				}
			}
			// }
		} catch (Exception e) {
			e.printStackTrace();
			logger.error(e);
			try {
				response.sendRedirect(request.getHeader("Referer"));
			} catch (Exception ex) {
				logger.error(ex);
			}
		}
	}
}
