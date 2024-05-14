/**
 * 
 */
package com.techmobia.supportmanager.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.spec.KeySpec;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Month;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techmobia.supportmanager.model.Email;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.math.BigInteger;

/**
 * @author vinay.sethi
 *
 */
public class Utility {
	private static final Logger logger = Logger.getLogger(Utility.class);

	/**
	 * Check Value is Blank or Not and Assign a Default Value
	 * 
	 * @return
	 */

	private Utility() {

	}

	public static String generateRandomPassword(int length) {
		return RandomStringUtils.randomAlphanumeric(length);
	}

	public static void zipFileold(String filePath, String fileName, String serviceName) {
		try {
			File file = new File(filePath + fileName);
			String zipFileName = file.getName().replace(file.getName().split("\\.")[1], ".zip");
			logger.info("Zip File Name for " + serviceName + "-->" + filePath + "-->" + fileName + "-->" + zipFileName);
			FileOutputStream fos = new FileOutputStream(filePath + zipFileName);
			ZipOutputStream zos = new ZipOutputStream(fos);
			zos.putNextEntry(new ZipEntry(file.getName()));
			byte[] bytes = Files.readAllBytes(Paths.get(filePath + fileName));
			zos.write(bytes, 0, bytes.length);
			zos.closeEntry();
			zos.close();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
	}

	public static String verifyData(String data) {
		PropertyConfigurator.configure(Constants.LOG4JFILEPATH);
		try {
			if (data != null && !data.equalsIgnoreCase("")) {
				data = data.trim();
			} else {
				data = "NA";
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
		return data;
	}

	/**
	 * Returns Current Date and Time
	 * 
	 * @return
	 */

	public static String getCurrentDatetime() {
		PropertyConfigurator.configure(Constants.LOG4JFILEPATH);
		String currentDatetime = null;
		try {
			currentDatetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis()));
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
		return currentDatetime;
	}

	/**
	 * Encrypt AES 256 the String
	 * 
	 * @return
	 */

	public static String encrypt(String strToEncrypt, String secretKey, String salt) {
		PropertyConfigurator.configure(Constants.LOG4JFILEPATH);
		try {
			byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			IvParameterSpec ivspec = new IvParameterSpec(iv);

			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(), 65536, 256);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKeySpec secretKeySpec = new SecretKeySpec(tmp.getEncoded(), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivspec);
			return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
		return null;
	}

	/**
	 * Decrypt the String
	 * 
	 * @return
	 */

	public static String decrypt(String strToDecrypt, String secretKey, String salt) {
		PropertyConfigurator.configure(Constants.LOG4JFILEPATH);
		try {
			byte[] iv = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
			IvParameterSpec ivspec = new IvParameterSpec(iv);

			SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
			KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(), 65536, 256);
			SecretKey tmp = factory.generateSecret(spec);
			SecretKeySpec secretKeySpec = new SecretKeySpec(tmp.getEncoded(), "AES");

			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivspec);
			return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
		return null;
	}

	public static void sendMail(String filepath, String filename, String receiverToEmails, String receiverCcEmails,
			String receiverBccEmails, String from, String yesterdayDate, String category, String mailSubject,
			String content, int attachmentFlag, String serviceName,int otp) {
		final String username = PropertyHandler.getInstance().getValue("smtp_username");
		final String password = PropertyHandler.getInstance().getValue("smtp_password");
		String host = PropertyHandler.getInstance().getValue("smtp_host");
		String port = PropertyHandler.getInstance().getValue("smtp_port");
		String smtpAuth = PropertyHandler.getInstance().getValue("smtp_auth");
		String smtpStarttlsEnable = PropertyHandler.getInstance().getValue("smtp_starttls.enable");
		Properties props = new Properties();
		props.put("mail.smtp.auth", smtpAuth);
		props.put("mail.smtp.starttls.enable", smtpStarttlsEnable);
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);
		props.put("mail.debug", "true");

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		String[] mailAddressTo = {};
		String[] mailAddressCc = {};
		String[] mailAddressBcc = {};

		try {
			if (receiverToEmails != null && (!"".equalsIgnoreCase(receiverToEmails))) {
				mailAddressTo = receiverToEmails.split(",");
			}
			if (receiverCcEmails != null && (!"".equalsIgnoreCase(receiverCcEmails))) {
				mailAddressCc = receiverCcEmails.split(",");
			}
			if (receiverBccEmails != null && (!"".equalsIgnoreCase(receiverBccEmails))) {
				mailAddressBcc = receiverBccEmails.split(",");
			}
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(from));

			InternetAddress[] mailAddressTO = new InternetAddress[mailAddressTo.length];
			InternetAddress[] mailAddressCC = new InternetAddress[mailAddressCc.length];
			InternetAddress[] mailAddressBCC = new InternetAddress[mailAddressBcc.length];
			for (int i = 0; i < mailAddressTo.length; i++) {
				mailAddressTO[i] = new InternetAddress(mailAddressTo[i]);
			}
			for (int i = 0; i < mailAddressCc.length; i++) {
				mailAddressCC[i] = new InternetAddress(mailAddressCc[i]);
			}
			for (int i = 0; i < mailAddressBcc.length; i++) {
				mailAddressBCC[i] = new InternetAddress(mailAddressBcc[i]);
			}
			// Set To: header field of the header.
			message.setRecipients(Message.RecipientType.TO, mailAddressTO);
			message.setRecipients(Message.RecipientType.CC, mailAddressCC);
			message.setRecipients(Message.RecipientType.BCC, mailAddressBCC);

			// Set Subject: header field
			message.setSubject(mailSubject);

			// Create the message part
			MimeBodyPart messageBodyPartFirst = new MimeBodyPart();

			// Now set the actual message
			logger.info(
					"mail_body_" + category + "-->" + PropertyHandler.getInstance().getValue("mail_body_" + category));
			if (serviceName.equalsIgnoreCase("whisperblitzsms")) {
				messageBodyPartFirst.setContent(PropertyHandler.getInstance().getValue("mail_body_" + category)
						.replace("%YESTERDAY_DATE", yesterdayDate).replace("%CONTENT", content).replace("%SERVICENAME",
								PropertyHandler.getInstance().getValue("whisperblitzsms_servicename")),
						"text/html");
			} else {
				messageBodyPartFirst.setContent(PropertyHandler.getInstance().getValue("mail_body_" + category)
						.replace("%YESTERDAY_DATE", yesterdayDate).replace("%CONTENT", content).replace("%OTP", Integer.toString(otp))
						.replace("%SERVICENAME", serviceName), "text/html");
			}
			// Create a multipar message
			Multipart multipart = new MimeMultipart();

			// Set text message part
			multipart.addBodyPart(messageBodyPartFirst);

			// Part two is attachment

			logger.info("Attachment-->" + filepath + filename);
			if (attachmentFlag == 1) {
				messageBodyPartFirst = new MimeBodyPart();
				DataSource source = new FileDataSource(filepath + "/" + filename);
				messageBodyPartFirst.setDataHandler(new DataHandler(source));
				messageBodyPartFirst.setFileName(filename);
				multipart.addBodyPart(messageBodyPartFirst);
			}

			// Send the complete message parts

			message.setContent(multipart);
			// Send message
			Transport.send(message);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
	}

	public static String yesterdayDate(int days) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, days);
		return dateFormat.format(cal.getTime());
	}

	public static String yesterdayDate(String endDate, int days) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(dateFormat.parse(endDate));
			cal.add(Calendar.DATE, days);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
		return dateFormat.format(cal.getTime());
	}

	public static String lastMonth(int month) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.MONTH, month);
		return dateFormat.format(cal.getTime());
	}

	/**
	 * Returns First Date of Current Month
	 * 
	 * @ServiceParameters
	 * @return
	 */

	public static LocalDate getFirstDateOfMonth() {
		LocalDate todaydate = null;
		try {
			todaydate = LocalDate.now();
			todaydate = todaydate.withDayOfMonth(1);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
		return todaydate;
	}

	public static List<String> filterListByTerm(List<String> list, String term) {
		return list.stream().filter(e -> e.toLowerCase().contains(term)).collect(Collectors.toList());
	}

	public static String list2Json(List<String> list) {
		String json = null;
		try {
			json = new ObjectMapper().writeValueAsString(list);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
		return json;
	}

	public static void zipFile(String filePath, String fileName) {
		try {
			File file = new File(filePath + fileName);
			String zipFileName = file.getName().replace(file.getName().split("\\.")[1], "zip");
			logger.info("Zip File Name-->" + filePath + "-->" + fileName + "-->" + zipFileName);
			FileOutputStream fos = new FileOutputStream(filePath + zipFileName);
			ZipOutputStream zos = new ZipOutputStream(fos);

			zos.putNextEntry(new ZipEntry(file.getName()));

			byte[] bytes = Files.readAllBytes(Paths.get(filePath + fileName));
			zos.write(bytes, 0, bytes.length);
			zos.closeEntry();
			zos.close();

		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
	}

	public static String fileDownload(HttpServletResponse response, String filepath, String filename) {
		try {
			response.setContentType("text/csv");
			// response.setContentType("APPLICATION/OCTET-STREAM");
			response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");
			File file = new File(filepath + filename);
			FileInputStream fileInputStream = new FileInputStream(file);
			ServletOutputStream serout = response.getOutputStream();
			byte[] outputByte = new byte[4096];
			// copy binary contect to output stream
			while (fileInputStream.read(outputByte, 0, 4096) != -1) {
				serout.write(outputByte, 0, 4096);
			}
			fileInputStream.close();
			serout.flush();
			serout.close();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
		return filename;
	}

	public static void downloadFile(HttpServletResponse response, String filepath, String filename) {
		try {
			File file = new File(filepath + filename);
			FileInputStream inputStream = new FileInputStream(file);
			String disposition = "attachment; fileName=\"" + filename + "\"";
			System.out.println("Disposition-->" + disposition);
			response.setContentType("text/csv");
			response.setHeader("Content-Disposition", disposition);
			response.setHeader("content-Length", String.valueOf(stream(inputStream, response.getOutputStream())));
		} catch (Exception e) {
			logger.error("Error occurred while downloading file {}", e);
		}
	}

	private static long stream(InputStream input, OutputStream output) {
		long size = 0;
		try (ReadableByteChannel inputChannel = Channels.newChannel(input);
				WritableByteChannel outputChannel = Channels.newChannel(output)) {
			ByteBuffer buffer = ByteBuffer.allocate(10240);
			while (inputChannel.read(buffer) != -1) {
				buffer.flip();
				size += outputChannel.write(buffer);
				buffer.clear();
			}
			System.out.println("Size=" + size);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
		return size;
	}

	public static String getDateMonth() {
		String dateNow = "";
		try {
			Calendar currentDate = Calendar.getInstance();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy/dd/MMM HH:mm:ss");
			dateNow = formatter.format(currentDate.getTime());
			dateNow = dateNow.substring(5, 11).replace("/", ";").toLowerCase();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return dateNow;
	}

	public static String getCurrentMonth() {
		Month currentMonth = null;
		try {
			LocalDate currentdate = LocalDate.now();
			currentMonth = currentdate.getMonth();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return StringUtils.capitalize(currentMonth.toString().toLowerCase());
	}

	public static int getCurrentYear() {
		int currentYear = 0;
		try {
			LocalDate currentdate = LocalDate.now();
			currentYear = currentdate.getYear();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return currentYear;
	}

	public static int isFileDirectoryExists(String filePath, String fileName) {
		PropertyConfigurator.configure(Constants.LOG4JFILEPATH);
		try {
			File dir = new File(filePath);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			File file = new File(filePath + File.separator + fileName);
			if (!file.exists()) {
				return 1;
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
		return 0;
	}

	public static String readexcelfile(String filePath, String fileName, String fileUploadId) {
		PropertyConfigurator.configure(Constants.LOG4JFILEPATH);
		String fileExtensionName = null;
		Workbook workbook = null;
		Sheet sheet = null;
		File file = null;
		FileInputStream inputstream = null;
		StringBuilder dataBuilder = new StringBuilder();
		try {
			file = new File(filePath + File.separator + fileName);
			inputstream = new FileInputStream(file);
			fileExtensionName = fileName.substring(fileName.indexOf('.'));
			if (fileExtensionName.equals(".xlsx")) {
				workbook = new XSSFWorkbook(inputstream);
			} else if (fileExtensionName.equals(".xls")) {
				workbook = new HSSFWorkbook(inputstream);
			}
			sheet = workbook.getSheetAt(0);
			Iterator<Row> rowIterator = sheet.iterator();
			while (rowIterator.hasNext()) {
				Row row = rowIterator.next();
				Iterator<Cell> cellIterator = row.cellIterator();
				while (cellIterator.hasNext()) {
					Cell cell = cellIterator.next();
					switch (cell.getCellTypeEnum()) {
					case BOOLEAN:
						dataBuilder.append(cell.getBooleanCellValue());
						break;
					case STRING:
						dataBuilder.append(URLEncoder.encode(
								cell.getRichStringCellValue().getString().replaceAll("/[^a-zA-Z0-9._-]/g", ""),
								"UTF-8"));
						break;
					case NUMERIC:
						if (DateUtil.isCellDateFormatted(cell)) {
							SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-mm-dd");
							dataBuilder.append(dateFormat.format(cell.getDateCellValue()));
						} else {
							Double doubleValue = cell.getNumericCellValue();
							BigDecimal bd = new BigDecimal(doubleValue.toString().replaceAll("/[^a-zA-Z0-9._-]/g", ""));
							long lonVal = bd.longValue();
							dataBuilder.append(Long.toString(lonVal).trim());
						}
						break;
					case FORMULA:
						dataBuilder.append(cell.getCellFormula());
						break;
					case BLANK:
						dataBuilder.append("NA");
						break;
					default:
						dataBuilder.append("NA");
					}
					dataBuilder.append(",");
				}
				dataBuilder.append(fileUploadId + Constants.DATAPPENDER);
			}
			inputstream.close();
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
		logger.debug(dataBuilder.toString());
		return dataBuilder.toString();
	}

	public static void sendMail(Email email, String serviceName) {
		final String username = PropertyHandler.getInstance().getValue("smtp_username");
		final String password = PropertyHandler.getInstance().getValue("smtp_password");
		String host = PropertyHandler.getInstance().getValue("smtp_host");
		String port = PropertyHandler.getInstance().getValue("smtp_port");
		String smtpAuth = PropertyHandler.getInstance().getValue("smtp_auth");
		String smtpStarttlsEnable = PropertyHandler.getInstance().getValue("smtp_starttls.enable");
		Properties props = new Properties();
		props.put("mail.smtp.auth", smtpAuth);
		props.put("mail.smtp.starttls.enable", smtpStarttlsEnable);
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.port", port);
		props.put("mail.debug", "true");

		Session session = Session.getInstance(props, new javax.mail.Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});
		String[] mailAddressTo = {};
		String[] mailAddressCc = {};
		String[] mailAddressBcc = {};

		try {
			if (email.getToEmailAddress() != null && (!"".equalsIgnoreCase(email.getToEmailAddress())
					&& !"NA".equalsIgnoreCase(email.getToEmailAddress()))) {
				mailAddressTo = email.getToEmailAddress().split(",");
			}
			if (email.getCcEmailAddress() != null && (!"".equalsIgnoreCase(email.getCcEmailAddress())
					&& !"NA".equalsIgnoreCase(email.getCcEmailAddress()))) {
				mailAddressCc = email.getCcEmailAddress().split(",");
			}
			if (email.getBccEmailAddress() != null && (!"".equalsIgnoreCase(email.getBccEmailAddress())
					&& !"NA".equalsIgnoreCase(email.getBccEmailAddress()))) {
				mailAddressBcc = email.getBccEmailAddress().split(",");
			}
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(email.getFromEmailAddress()));

			InternetAddress[] mailAddressTO = new InternetAddress[mailAddressTo.length];
			InternetAddress[] mailAddressCC = new InternetAddress[mailAddressCc.length];
			InternetAddress[] mailAddressBCC = new InternetAddress[mailAddressBcc.length];
			for (int i = 0; i < mailAddressTo.length; i++) {
				mailAddressTO[i] = new InternetAddress(mailAddressTo[i]);
			}
			for (int i = 0; i < mailAddressCc.length; i++) {
				mailAddressCC[i] = new InternetAddress(mailAddressCc[i]);
			}
			for (int i = 0; i < mailAddressBcc.length; i++) {
				mailAddressBCC[i] = new InternetAddress(mailAddressBcc[i]);
			}
			// Set To: header field of the header.
			message.setRecipients(Message.RecipientType.TO, mailAddressTO);
			message.setRecipients(Message.RecipientType.CC, mailAddressCC);
			message.setRecipients(Message.RecipientType.BCC, mailAddressBCC);

			// Set Subject: header field
			message.setSubject(email.getEmailSubject());

			// Create the message part
			MimeBodyPart messageBodyPartFirst = new MimeBodyPart();

			// Now set the actual message
			messageBodyPartFirst.setContent(email.getEmailBody().replace("%YESTERDAY_DATE", yesterdayDate(-1))
					.replace("%SERVICENAME", serviceName), "text/html");

			// Create a multipar message
			Multipart multipart = new MimeMultipart();

			// Set text message part
			multipart.addBodyPart(messageBodyPartFirst);

			// Part two is attachment

			logger.info("Attachment-->" + email.getFilePath() + email.getFileName());
			if (email.getAttachmentFlag() == 1) {
				messageBodyPartFirst = new MimeBodyPart();
				DataSource source = new FileDataSource(email.getFilePath() + "/" + email.getFileName());
				messageBodyPartFirst.setDataHandler(new DataHandler(source));
				messageBodyPartFirst.setFileName(email.getFileName());
				multipart.addBodyPart(messageBodyPartFirst);
				if (serviceName.equals("wsitrial")) {
					messageBodyPartFirst = new MimeBodyPart();
					source = new FileDataSource(email.getFilePath() + "/Registration_" + email.getFileName());
					messageBodyPartFirst.setDataHandler(new DataHandler(source));
					messageBodyPartFirst.setFileName("Registration_" + email.getFileName());
					multipart.addBodyPart(messageBodyPartFirst);
				}
			}

			// Send the complete message parts

			message.setContent(multipart);
			// Send message
			Transport.send(message);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
	}

	public static void copyFiles(String sourcePath, String destinationPath) {
		File source = new File(sourcePath);
		File dest = new File(destinationPath);
		try {
			FileUtils.copyDirectory(source, dest);
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
	}

	public static void moveFiles(String sourcePath, String destinationPath) {
		Path result = null;
		try {
			result = Files.move(Paths.get(sourcePath), Paths.get(destinationPath));
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
		if (result != null) {
			logger.info(sourcePath + " File moved successfully.");
		} else {
			logger.info(sourcePath + " File movement failed.");
		}
	}
	
	public static void showAlert(HttpServletResponse response, String message, String page) {
		PrintWriter out;
		try {
			out = response.getWriter();
			out.println("<script type=\"text/javascript\">");
			out.println("alert('" + message + "');");
			out.println("location='" + page + ".jsp';");
			out.println("</script>");
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
	}
	
	public static void showDIDAlert(HttpServletResponse response, int category, String page) {
		PrintWriter out;
		String message = PropertyHandler.getInstance().getValue(category+"_message");
		if(message==null && message.length()==0) {
			message = "Something went wrong";
		}
		try {
			out = response.getWriter();
			out.println("<script type=\"text/javascript\">");
			out.println("alert('" + message + "');");
			out.println("location='" + page + ".jsp';");
			out.println("</script>");
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
	}
	
	public static int generateOtp(String emailId) {
		int otp=0;
		int validateFlag=0;
		try {
			otp=DbHandler.getInstance().fetchOtp(emailId);
			validateFlag=DbHandler.getInstance().validateOtp(emailId, otp);
			if(validateFlag==0) {
				otp=(int) (Math.random() * (100000 - 1000)) + 1000;
				DbHandler.getInstance().insertOtp(emailId, otp);
			}
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
		return otp;
	}
	
	public static void dtcFileTransfer(String startDate,String endDate) {
		PropertyConfigurator.configure(PropertyHandler.getInstance().getValue("log4j_filepath"));
		Process process = null;
		StringBuilder summaryoutput = new StringBuilder();
		String todayDate;
		List<String> list = new ArrayList<>();
		Email email=null;
		String[] commands= {};
		try {
			summaryoutput.append("<html> <head> <style> table, th, td {   border: 1px solid black;   border-collapse: collapse; } th, td {   padding: 15px;   text-align: left; } table#t01 {   width: 100%;      background-color: #f1f1c1; } </style> </head> <body> <br><br><table width=\"100%\" cellspacing=\"1\" cellspadding=\"1\" border=\"1\"><tr>");			
		    summaryoutput.append("<th>Date</th>");
		    summaryoutput.append("<th>Program Name</th>");		    
		    summaryoutput.append("<th>Successful Trial</th>");
		    summaryoutput.append("</tr><tr>");
		    logger.info("summaryoutput-->"+summaryoutput.toString());
			todayDate = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));
			String []dataArr=DbHandler.getInstance().getDtcSummary(startDate,endDate).split(",");
			for(int i=0;i<dataArr.length;i++) {
				if(dataArr[i].split("@").length>1) {
					summaryoutput.append("<td>"+dataArr[i].split("@")[0]+ "</td>");
					summaryoutput.append("<td>"+dataArr[i].split("@")[1]+ "</td>");
					summaryoutput.append("<td>"+dataArr[i].split("@")[2]+ "</td>");
					summaryoutput.append("</tr><tr>");
				}else {
					summaryoutput.append("<td>No Records Found</td>");
				}				
			}
			summaryoutput.append("</tr>");
			email=DbHandler.getInstance().getEmailDetails("dtcAlert","cp",startDate,endDate);
			commands = new String[] { "/usr/bin/python", PropertyHandler.getInstance().getValue("script_path_filetransfer")};
			process = Runtime.getRuntime().exec(commands);
			process.waitFor();
			logger.info("Before Replacing Email Body-->"+email.getEmailBody());
			email.setEmailBody(email.getEmailBody().replace("%CONTENT", summaryoutput.toString()).replace("%Date", todayDate));
			email.setEmailSubject(email.getEmailSubject().replace("%Date", todayDate));
			sendMail(email,"dtc");
			summaryoutput.append("</tr><tr><td>Grand Total>");			
			summaryoutput.append("</table></body></html>");						
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		} finally {			
			if (process != null) {
				process.destroy();
			}
			list.clear();
			summaryoutput=null;
		}
	}




	
	
	 public static String encryptmd5(String input) {
		 String md5Hash=null;  
		 try {
	            MessageDigest md = MessageDigest.getInstance("MD5");

	            md.update(input.getBytes());

	            byte[] digest = md.digest();
	            BigInteger bigInt = new BigInteger(1, digest);
	            md5Hash = bigInt.toString(16);

	            while (md5Hash.length() < 32) {
	                md5Hash = "0" + md5Hash;
	            }
               logger.info("MD5 hash is ----->>> "+md5Hash);
	        }  catch (Exception exception) {
				logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
						.collect(Collectors.joining("\n")));
			}
			return md5Hash;
	    }
	 
	 
	 
	 public static String encryptAES(String data, String encryptionKey) {
		 byte[] encryptedData = null;
		 try {
	        SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(), "AES");
	        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
	        cipher.init(Cipher.ENCRYPT_MODE, key);
	        encryptedData = cipher.doFinal(data.getBytes());
	        logger.info("Encrypted data is --->>> "+Base64.getEncoder().encodeToString(encryptedData));
	    } catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
		 return Base64.getEncoder().encodeToString(encryptedData);
	 } 
	 
	 
	 
	 public static String decryptAES(String encryptedData, String encryptionKey) {
		 byte[] decryptedData = null;
		 try {
			 SecretKeySpec key = new SecretKeySpec(encryptionKey.getBytes(), "AES");
		        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		        cipher.init(Cipher.DECRYPT_MODE, key);
		        decryptedData = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
		       logger.info("Decrypted is ---->>> "+new String(decryptedData) );
		 }catch (Exception exception) {
				logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
						.collect(Collectors.joining("\n")));
			}
		 return new String(decryptedData);
		 }
	 
	 
	 public static void sendUserMail(String recipent,String userPassword) {
			final String username = PropertyHandler.getInstance().getValue("smtp_username");
			final String password = PropertyHandler.getInstance().getValue("smtp_password");
			String host = PropertyHandler.getInstance().getValue("smtp_host");
			String port = PropertyHandler.getInstance().getValue("smtp_port");
			String smtpAuth = PropertyHandler.getInstance().getValue("smtp_auth");
			String smtpStarttlsEnable = PropertyHandler.getInstance().getValue("smtp_starttls.enable");
			Properties props = new Properties();
			props.put("mail.smtp.auth", smtpAuth);
			props.put("mail.smtp.starttls.enable", smtpStarttlsEnable);
			props.put("mail.smtp.host", host);
			props.put("mail.smtp.port", port);
			props.put("mail.debug", "true");

			Session session = Session.getInstance(props, new javax.mail.Authenticator() {
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});

			try {
				String senderEmail = "otp@vfirst.com";
				MimeMessage message = new MimeMessage(session);
				message.setFrom(new InternetAddress(senderEmail));

				
				// Set To: header field of the header.
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(recipent));

				// Set Subject: header field
				message.setSubject("Password Mail for User Management");

				// Create a multipar message

				message.setText("Hello Team, \n \nKindly find the updated password PASSWORD \n \nRegards,\nVfirst".replace("PASSWORD", userPassword));
				// Send message
				Transport.send(message);
			} catch (Exception exception) {
				logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
						.collect(Collectors.joining("\n")));
			}
		}
}
