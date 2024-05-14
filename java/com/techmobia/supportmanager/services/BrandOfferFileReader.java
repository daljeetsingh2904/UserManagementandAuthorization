/**
 * 
 */
package com.techmobia.supportmanager.services;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;

import com.techmobia.supportmanager.model.OfferCodeUpload;
import com.techmobia.supportmanager.model.OfferCodeValidInput;

/**
 * @author vinay.sethi
 *
 */
public class BrandOfferFileReader implements Runnable {
	
	private static final Logger logger = Logger.getLogger(BrandOfferFileReader.class);
	private String fileName;
	private String filePath;
	private String fileUploadId;
	private OfferCodeUpload offerCodeUpload;
	List<String> combination = new ArrayList<String>();
	StringBuilder output = new StringBuilder();
	private String inputstring;

	public BrandOfferFileReader(String filePath, String fileName, String fileUploadId, OfferCodeUpload offerCodeUpload) {
		this.filePath = filePath;
		this.fileName = fileName;
		this.fileUploadId = fileUploadId;
		this.offerCodeUpload = offerCodeUpload;
	}

	public void run() {
		String fileData = null;
		try {
			DbHandler.getInstance().updateDrcpFileStatus(this.fileUploadId);
			fileData = Utility.readexcelfile(this.filePath, this.fileName, this.fileUploadId);
			logger.info(fileData);

			List<OfferCodeUpload> offerCodeUploads = new ArrayList<>();
			String[] fileDataArr = fileData.split(Constants.DATAPPENDER.replace("*", "\\*"));			
			int numberOfExcelColumn = 0;
			if (offerCodeUpload.getAction().equalsIgnoreCase("addBrandOfferBulk") && offerCodeUpload.getServiceName().equalsIgnoreCase("rurban")) {
				numberOfExcelColumn = 5;
				for (int i = 1; i < fileDataArr.length; i++) {
					String element = fileDataArr[i].replaceAll(",'" + fileUploadId, ",");
					List<String> excelList = Arrays.asList(element.split(","));
					logger.info("excelList Size-->"+excelList.size());
					if (excelList.size() == numberOfExcelColumn) {
						OfferCodeUpload obj = new OfferCodeUpload();
						obj.setServiceName(offerCodeUpload.getServiceName());
						obj.setBrandInput(excelList.get(0));
						obj.setBrandName(URLDecoder.decode(excelList.get(1).replaceAll("_{2,}", "_"), "UTF-8"));
						obj.setSiteName(URLDecoder.decode(excelList.get(2), "UTF-8"));
						obj.setProjectId(excelList.get(3));
						obj.setEmpName(offerCodeUpload.getEmpName());
						offerCodeUploads.add(obj);
					}
				}
				if(offerCodeUploads.size() > 0) {				
					configureBrandFlow2(offerCodeUploads);				
				}else {			
					logger.info("Invalid excel format");
				}
			}else {
				DbHandler.getInstance().addOfferCodeBrand(fileDataArr,this.offerCodeUpload,this.fileUploadId);
			}			
		} catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}
	}

	public void configureBrandFlow2(List<OfferCodeUpload> offerCodeUploads) {
		// ======make all valid input and valid brand
		List<OfferCodeValidInput> offerCodeValidInputs = new ArrayList<>();
		for (int i = 0; i < offerCodeUploads.size(); i++) {
			String trialLimit = DbHandler.getInstance().getTrialLimit(offerCodeUploads.get(i));
			String siteId = DbHandler.getInstance().getSiteIdFromPSM(offerCodeUploads.get(i));
			int index = getExistingIndex(offerCodeValidInputs, offerCodeUploads.get(i).getProjectId());
			OfferCodeValidInput offerCodeValidInput;
			logger.info("configureBrandFlow2-->"+trialLimit+"-->"+siteId+"-->"+index);
			if (index != -1) {
				offerCodeValidInput = offerCodeValidInputs.get(index);
				if (!offerCodeValidInput.getValidInput().contains("@" + offerCodeUploads.get(i).getBrandInput())) {
					offerCodeValidInput.setValidInput(offerCodeValidInput.getValidInput() + "@" + offerCodeUploads.get(i).getBrandInput());
					if (!offerCodeUploads.get(i).getBrandName().contains("_")) {
						if (!offerCodeValidInput.getAllowedBrand().replace("+", "").contains(offerCodeUploads.get(i).getBrandName())) {
							offerCodeValidInput.setBrandCount(Integer.parseInt(offerCodeValidInput.getBrandCount()) + 1 + "");
						}
						if (Integer.parseInt(trialLimit) > 0) {
							if (!offerCodeValidInput.getOfferCount().equalsIgnoreCase("0")) {
								offerCodeValidInput.setOfferCount("1");
							}
						} else {
							offerCodeValidInput.setOfferCount("0");
						}

					} else {
						List<String> temp = Arrays.asList(offerCodeUploads.get(i).getBrandName().split("_"));
						for (int m = 0; m < temp.size(); m++) {
							if (!offerCodeValidInput.getAllowedBrand().replace("+", "").contains(temp.get(m))) {
								offerCodeValidInput.setBrandCount(Integer.parseInt(offerCodeValidInput.getBrandCount()) + 1 + "");
							}
						}
						if (!(temp.size() > Integer.parseInt(trialLimit))) {
							if (!offerCodeValidInput.getOfferCount().equalsIgnoreCase("0")) {
								offerCodeValidInput.setOfferCount("1");
							}
						} else {
							offerCodeValidInput.setOfferCount("0");
						}
					}
					offerCodeValidInput.setAllowedBrand(offerCodeValidInput.getAllowedBrand() + "+"	+ offerCodeUploads.get(i).getBrandName().replace("_", "_"));
					if (!offerCodeValidInput.getSiteId().equalsIgnoreCase("0")) {
						if(offerCodeValidInput.getSiteId().equalsIgnoreCase(siteId)) {
							offerCodeValidInput.setSiteId(siteId);
						}else {
							offerCodeValidInput.setSiteId("0");
						}
					}
					if (offerCodeUploads.get(i).getBrandInput().equalsIgnoreCase("0") && offerCodeUploads.get(i).getBrandName().contains("_")) {
						offerCodeValidInput.setBrandInputStatus("Invalid");
					} else if (offerCodeUploads.get(i).getBrandInput().equalsIgnoreCase("1") && offerCodeUploads.get(i).getBrandName().contains("_")) {
						offerCodeValidInput.setBrandInputStatus("Invalid");
					} else {
						offerCodeValidInput.setBrandInputStatus("Valid");
					}
					if (Integer.parseInt(offerCodeUploads.get(i).getBrandInput()) < 0 || Integer.parseInt(offerCodeUploads.get(i).getBrandInput()) > 9) {
						if (!offerCodeValidInput.getBrandInput().equalsIgnoreCase("Invalid")) {
							offerCodeValidInput.setBrandInput("Invalid");
						}
					} else {
						if (!offerCodeValidInput.getBrandInput().equalsIgnoreCase("Invalid")) {
							offerCodeValidInput.setBrandInput(offerCodeUploads.get(i).getBrandInput());
						}
					}
				}
			} else {
				offerCodeValidInput = new OfferCodeValidInput();
				offerCodeValidInput.setServiceName(offerCodeUploads.get(i).getServiceName());
				offerCodeValidInput.setProjectId(offerCodeUploads.get(i).getProjectId());
				offerCodeValidInput.setValidInput("@" + offerCodeUploads.get(i).getBrandInput());
				offerCodeValidInput.setAllowedBrand(offerCodeUploads.get(i).getBrandName());
				offerCodeValidInput.setSiteId(siteId);
				offerCodeValidInput.setBrandCount(1 + "");
				offerCodeValidInput.setTrialCount(trialLimit);

				if (Integer.parseInt(trialLimit) > 0) {
					offerCodeValidInput.setOfferCount("1");
				} else {
					offerCodeValidInput.setOfferCount("0");
				}
				
				//offerCodeValidInput.setBrandInputStatus("Invalid");

				if (offerCodeUploads.get(i).getBrandInput().equalsIgnoreCase("0") && offerCodeUploads.get(i).getBrandName().contains("_")) {
					offerCodeValidInput.setBrandInputStatus("Invalid");
				} else if (offerCodeUploads.get(i).getBrandInput().equalsIgnoreCase("1") && offerCodeUploads.get(i).getBrandName().contains("_")) {
					offerCodeValidInput.setBrandInputStatus("Invalid");
				} else {
					offerCodeValidInput.setBrandInputStatus("Valid");
				}

				if (Integer.parseInt(offerCodeUploads.get(i).getBrandInput()) < 0
						|| Integer.parseInt(offerCodeUploads.get(i).getBrandInput()) > 9) {
					offerCodeValidInput.setBrandInput("Invalid");
				} else {
					offerCodeValidInput.setBrandInput(offerCodeUploads.get(i).getBrandInput());
				}

				offerCodeValidInput.setEmpId(offerCodeUploads.get(i).getEmpName());

				offerCodeValidInputs.add(offerCodeValidInput);
			}

			if (!offerCodeUploads.get(i).getBrandName().contains("_")) {
				offerCodeUploads.get(i).setOfferCount("1");
			} else {
				List<String> brandList = Arrays.asList(offerCodeUploads.get(i).getBrandName().split("_"));
				offerCodeUploads.get(i).setOfferCount(brandList.size() + "");
			}
		}

		// ======set all valid input and valid brand
		for (int i = 0; i < offerCodeUploads.size(); i++) {
			int index = getExistingIndex(offerCodeValidInputs, offerCodeUploads.get(i).getProjectId());
			if (index != -1) {
				if (offerCodeValidInputs.get(index).getBrandInput().equalsIgnoreCase("Invalid")) {
					offerCodeUploads.get(i).setBrandInput(offerCodeValidInputs.get(index).getBrandInput());
				}
				offerCodeUploads.get(i).setValidInput(offerCodeValidInputs.get(index).getValidInput());
				offerCodeUploads.get(i).setAllowedInput(offerCodeValidInputs.get(index).getValidInput());
				offerCodeUploads.get(i).setAllowedBrand(offerCodeValidInputs.get(index).getAllowedBrand());
				offerCodeUploads.get(i).setSiteId(offerCodeValidInputs.get(index).getSiteId());

				if (Integer.parseInt(offerCodeValidInputs.get(index).getBrandCount()) < Integer
						.parseInt(offerCodeValidInputs.get(index).getTrialCount())) {
					if (!offerCodeValidInputs.get(index).getBrandCount().equalsIgnoreCase("0")) {
						offerCodeValidInputs.get(index).setBrandCount("0");
					}
				}

				offerCodeUploads.get(i).setBrandCount(offerCodeValidInputs.get(index).getBrandCount());
				offerCodeUploads.get(i).setTrialCount(offerCodeValidInputs.get(index).getTrialCount());
				if (offerCodeValidInputs.get(index).getOfferCount().equalsIgnoreCase("0")) {
					offerCodeUploads.get(i).setOfferCount(offerCodeValidInputs.get(index).getOfferCount());
				}
				offerCodeUploads.get(i).setBrandInputStatus(offerCodeValidInputs.get(index).getBrandInputStatus());

			}
		}

		offerCodeUploads = filterValidInput(offerCodeUploads);

		int size = offerCodeValidInputs.size();

		for (int m = 0; m < size; m++) {

			List<String> allbrands = Arrays
					.asList(offerCodeValidInputs.get(m).getAllowedBrand().replace("+", ",").split(","));
			
			logger.info(allbrands);


			String sizePattern = "";
			if (allbrands.size() == 2) {
				sizePattern = "01";
			} else if (allbrands.size() == 3) {
				sizePattern = "012";
			} else if (allbrands.size() == 4) {
				sizePattern = "0123";
			} else if (allbrands.size() == 5) {
				sizePattern = "01234";
			} else if (allbrands.size() == 6) {
				sizePattern = "012345";
			} else if (allbrands.size() == 10) {
				sizePattern = "0123456789";
			}


			if (sizePattern.length() > 0) {
				combination.clear();
				combinations(sizePattern);
			} else {
				combination.clear();
			}

			String compareBrand = "";
			List<String> finalList = new ArrayList<String>();
			for (int z = 0; z < combination.size(); z++) {
				String value = combination.get(z);
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < allbrands.size(); i++) {
					if (value.length() == 1) {
						if (validateValue(allbrands.get(Character.getNumericValue(value.charAt(0))),
								allbrands.get(i))) {
							sb.append("+");
							sb.append(allbrands.get(i));
						}
					} else if (value.length() == 2) {
						if (validateValue(allbrands.get(Character.getNumericValue(value.charAt(0))), allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(1))),
										allbrands.get(i))) {
							sb.append("+");
							sb.append(allbrands.get(i));
						}
					} else if (value.length() == 3) {
						if (validateValue(allbrands.get(Character.getNumericValue(value.charAt(0))), allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(1))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(2))),
										allbrands.get(i))) {
							sb.append("+");
							sb.append(allbrands.get(i));
						}
					} else if (value.length() == 4) {
						if (validateValue(allbrands.get(Character.getNumericValue(value.charAt(0))), allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(1))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(2))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(3))),
										allbrands.get(i))) {
							sb.append("+");
							sb.append(allbrands.get(i));
						}
					} else if (value.length() == 5) {
						if (validateValue(allbrands.get(Character.getNumericValue(value.charAt(0))), allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(1))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(2))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(3))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(4))),
										allbrands.get(i))) {
							sb.append("+");
							sb.append(allbrands.get(i));
						}
					} else if (value.length() == 6) {
						if (validateValue(allbrands.get(Character.getNumericValue(value.charAt(0))), allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(1))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(2))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(3))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(4))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(5))),
										allbrands.get(i))) {
							sb.append("+");
							sb.append(allbrands.get(i));
						}
					} else if (value.length() == 7) {
						if (validateValue(allbrands.get(Character.getNumericValue(value.charAt(0))), allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(1))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(2))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(3))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(4))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(5))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(6))),
										allbrands.get(i))) {
							sb.append("+");
							sb.append(allbrands.get(i));
						}
					} else if (value.length() == 8) {
						if (validateValue(allbrands.get(Character.getNumericValue(value.charAt(0))), allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(1))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(2))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(3))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(4))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(5))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(6))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(7))),
										allbrands.get(i))) {
							sb.append("+");
							sb.append(allbrands.get(i));
						}
					} else if (value.length() == 9) {
						if (validateValue(allbrands.get(Character.getNumericValue(value.charAt(0))), allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(1))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(2))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(3))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(4))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(5))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(6))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(7))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(8))),
										allbrands.get(i))) {
							sb.append("+");
							sb.append(allbrands.get(i));
						}
					} else if (value.length() == 10) {
						if (validateValue(allbrands.get(Character.getNumericValue(value.charAt(0))), allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(1))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(2))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(3))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(4))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(5))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(6))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(7))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(8))),
										allbrands.get(i))
								&& validateValue(allbrands.get(Character.getNumericValue(value.charAt(9))),
										allbrands.get(i))) {
							sb.append("+");
							sb.append(allbrands.get(i));
						}
					}
				}

				if (sb.length() > 0 && !finalList.contains(sb.toString())) {
					finalList.add(sb.toString());
				}
			}

			for (int i = 0; i < finalList.size(); i++) {
				OfferCodeValidInput obj = new OfferCodeValidInput();
				obj.setValidInput(
						makeBrandInput(finalList.get(i), offerCodeUploads, offerCodeValidInputs.get(m).getProjectId()));
				obj.setAllowedBrand(finalList.get(i).substring(1));
				obj.setProjectId(offerCodeValidInputs.get(m).getProjectId());
				obj.setServiceName(offerCodeValidInputs.get(m).getServiceName());
				obj.setSiteId(offerCodeValidInputs.get(m).getSiteId());
				obj.setBrandCount(offerCodeValidInputs.get(m).getBrandCount());
				obj.setTrialCount(offerCodeValidInputs.get(m).getTrialCount());
				obj.setOfferCount(offerCodeValidInputs.get(m).getOfferCount());
				obj.setBrandInputStatus(offerCodeValidInputs.get(m).getBrandInputStatus());
				obj.setBrandInput(offerCodeValidInputs.get(m).getBrandInput());
				obj.setEmpId(offerCodeValidInputs.get(m).getEmpId());
				offerCodeValidInputs.add(obj);
			}
		}
		insertAllowedBrand(offerCodeValidInputs);
		insertValidInput(offerCodeUploads);
		insertAllowedInput(offerCodeUploads);
	}
	
	public boolean validateValue(String first, String second) {
		List<String> wordsOfSecond = Arrays.asList(second.split("_"));
		for (String word : first.split("_")) {
			if (wordsOfSecond.contains(word)) {
				return false;
			}
		}
		return true;
	}

	public String makeBrandInput(String brands, List<OfferCodeUpload> list, String projectId) {
		StringBuilder sb = new StringBuilder();
		brands = brands.substring(1);
		List<String> brandList = Arrays.asList(brands.replace("+", ",").split(","));
		for (int i = 0; i < brandList.size(); i++) {
			sb.append("@");
			sb.append(getBrandInputFromBrand(list, brandList.get(i), projectId));
		}
		return sb.toString();
	}


	public void combinations(final String str) {
		inputstring = str;
		combine();
	}

	public void combine() {
		combine(0);
	}

	public void combine(int start) {
		for (int i = start; i < inputstring.length(); ++i) {
			output.append(inputstring.charAt(i));
			combination.add(output.toString());
			if (i < inputstring.length())
				combine(i + 1);
			output.setLength(output.length() - 1);
		}
	}

	public List<OfferCodeUpload> filterValidInput(List<OfferCodeUpload> offerCodeUploads) {
		for (int i = 0; i < offerCodeUploads.size(); i++) {
			List<OfferCodeUpload> temp = getSameTypeList(offerCodeUploads, offerCodeUploads.get(i).getProjectId());
			for (int j = 0; j < temp.size(); j++) {
				if (temp.get(j).getBrandName().contains(offerCodeUploads.get(i).getBrandName())
						|| offerCodeUploads.get(i).getBrandName().contains(temp.get(j).getBrandName())) {
					String validinput = offerCodeUploads.get(i).getValidInput();
					validinput = validinput.replace("@" + temp.get(j).getBrandInput(), "");
					if (validinput.length() == 0) {
						validinput = "@-1";
					}
					if (offerCodeUploads.get(i).getOfferCount()
							.equalsIgnoreCase(offerCodeUploads.get(i).getTrialCount())) {
						validinput = "@-1";
					}
					offerCodeUploads.get(i).setValidInput(validinput);
				} else {
					if (temp.get(j).getOfferCount().equalsIgnoreCase(offerCodeUploads.get(i).getTrialCount())) {
						String validinput = offerCodeUploads.get(i).getValidInput();
						validinput = validinput.replace("@" + temp.get(j).getBrandInput(), "");
						offerCodeUploads.get(i).setValidInput(validinput);
					}
				}
				if (offerCodeUploads.get(i).getBrandName().contains("_")) {
					List<String> newTemp = Arrays.asList(offerCodeUploads.get(i).getBrandName().split("_"));
					for (int k = 0; k < newTemp.size(); k++) {
						if (offerCodeUploads.get(i).getBrandName().contains(newTemp.get(k))) {
							String validinput = offerCodeUploads.get(i).getValidInput();
							int index = getBrandInputFromIndex(offerCodeUploads, newTemp.get(k),
									offerCodeUploads.get(i).getProjectId());
							if (index != -1) {
								validinput = validinput.replace("@" + offerCodeUploads.get(index).getBrandInput(), "");
							} else {
								validinput = "@-1";
							}
							if (validinput.length() == 0) {
								validinput = "@-1";
							}
							if (offerCodeUploads.get(i).getOfferCount()
									.equalsIgnoreCase(offerCodeUploads.get(i).getTrialCount())) {
								validinput = "@-1";
							}
							offerCodeUploads.get(i).setValidInput(validinput);
						}
					}
				}

				if (offerCodeUploads.get(i).getBrandName().contains("_") && temp.get(j).getBrandName().contains("_")) {
					List<String> temp1 = Arrays.asList(offerCodeUploads.get(i).getBrandName().split("_"));
					List<String> temp2 = Arrays.asList(temp.get(j).getBrandName().split("_"));
					for (int m = 0; m < temp1.size(); m++) {
						for (int n = 0; n < temp2.size(); n++) {
							if (temp1.get(m).contains(temp2.get(n))) {
								String validinput = offerCodeUploads.get(i).getValidInput();
								int index = getBrandInputFromIndex(offerCodeUploads, temp.get(j).getBrandName(),
										offerCodeUploads.get(i).getProjectId());
								if (index != -1) {
									validinput = validinput.replace("@" + offerCodeUploads.get(index).getBrandInput(),
											"");
								} else {
									validinput = "@-1";
								}
								if (validinput.length() == 0) {
									validinput = "@-1";
								}
								if (offerCodeUploads.get(i).getOfferCount()
										.equalsIgnoreCase(offerCodeUploads.get(i).getTrialCount())) {
									validinput = "@-1";
								}
								offerCodeUploads.get(i).setValidInput(validinput);
							}
						}
					}
				}
			}
		}
		return offerCodeUploads;
	}

	public void insertAllowedBrand(List<OfferCodeValidInput> offerCodeValidInputs) {
		boolean isExists = false;
		List<String> projectIds = offerCodeValidInputs.stream().map(OfferCodeValidInput::getProjectId).distinct()
				.collect(Collectors.toList());
		for (int i = 0; i < projectIds.size(); i++) {
			int index = getExistingIndex(offerCodeValidInputs, projectIds.get(i));
			if (!offerCodeValidInputs.get(index).getSiteId().equalsIgnoreCase("0")
					&& !offerCodeValidInputs.get(index).getOfferCount().equalsIgnoreCase("0")
					&& !offerCodeValidInputs.get(index).getBrandCount().equalsIgnoreCase("0")
					&& !offerCodeValidInputs.get(index).getBrandInputStatus().equalsIgnoreCase("invalid")
					&& !offerCodeValidInputs.get(index).getBrandInput().equalsIgnoreCase("invalid")) {
				isExists = DbHandler.getInstance().isAllowedBrandExist(offerCodeValidInputs.get(index));
				if (isExists) {
					DbHandler.getInstance().updateAllowedBrandMasterRecords(offerCodeValidInputs.get(index));
				}
				isExists = DbHandler.getInstance().isBrandExistType2(offerCodeValidInputs.get(index));
				if (isExists) {
					DbHandler.getInstance().updateBrandOfferFileRecordsType2(offerCodeValidInputs.get(index));
				}
			}
		}

		for (int i = 0; i < offerCodeValidInputs.size(); i++) {
			if (!offerCodeValidInputs.get(i).getSiteId().equalsIgnoreCase("0")
					&& !offerCodeValidInputs.get(i).getOfferCount().equalsIgnoreCase("0")
					&& !offerCodeValidInputs.get(i).getBrandCount().equalsIgnoreCase("0")
					&& !offerCodeValidInputs.get(i).getBrandInputStatus().equalsIgnoreCase("invalid")
					&& !offerCodeValidInputs.get(i).getBrandInput().equalsIgnoreCase("invalid")) {
				DbHandler.getInstance().addAllowedBrandMasterRecords(offerCodeValidInputs.get(i));				
			}
		}
	}
	
	public void insertAllowedInput(List<OfferCodeUpload> offerCodeUploads) {
		List<String> projectIds = offerCodeUploads.stream().map(OfferCodeUpload::getProjectId).distinct()
				.collect(Collectors.toList());
		for (int i = 0; i < projectIds.size(); i++) {
			OfferCodeUpload offerCodeUpload = findByProjectId(offerCodeUploads,projectIds.get(i));
			if(!offerCodeUpload.getBrandInputStatus().equalsIgnoreCase("Invalid")
					&& !offerCodeUpload.getBrandInput().equalsIgnoreCase("Invalid")
					&& !offerCodeUpload.getSiteId().equalsIgnoreCase("0")
					&& !offerCodeUpload.getOfferCount().equalsIgnoreCase("0")
					&& !offerCodeUpload.getBrandCount().equalsIgnoreCase("0")
					) {
				DbHandler.getInstance().addDefaultAllowedInputPSM(offerCodeUpload);
			}
		}
	}

	public OfferCodeUpload findByProjectId(Collection<OfferCodeUpload> offerCodeUpload, String projectId) {
	    return offerCodeUpload.stream().filter(offer-> projectId.equals(offer.getProjectId()))
	            .findFirst().orElse(null);
	}

	public void insertValidInput(List<OfferCodeUpload> offerCodeUploads) {
		String status;
		for (int i = 0; i < offerCodeUploads.size(); i++) {
			if (offerCodeUploads.get(i).getBrandInputStatus().equalsIgnoreCase("Invalid")) {
				status = "Invalid Brands";
			} else if ((offerCodeUploads.get(i).getBrandInput().equalsIgnoreCase("Invalid"))) {
				status = "Invalid Brands Input";
			} else if (offerCodeUploads.get(i).getSiteId().equalsIgnoreCase("0")) {
				status = "Invalid Site Name / Site Name not mapped with ProjectId";
			} else if (offerCodeUploads.get(i).getOfferCount().equalsIgnoreCase("0")) {
				status = "Trial count less than offer count";
			} else if (offerCodeUploads.get(i).getBrandCount().equalsIgnoreCase("0")) {
				status = "Brand count less than trial count";
			} else {
				status = "Inserted Successfully";
				DbHandler.getInstance().addBrandOfferFileRecordsType2(offerCodeUploads.get(i),
						offerCodeUploads.get(i).getSiteId());
			}
			DbHandler.getInstance().addOfferCodeFileUploadDetailsType2(offerCodeUploads.get(i), status, fileUploadId);
		}
	}

	public int getExistingIndex(List<OfferCodeValidInput> list, final String projectId) {
		int index = -1;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getProjectId().equalsIgnoreCase(projectId)) {
				index = i;
				break;
			}
		}
		return index;
	}

	public static List<OfferCodeUpload> getSameTypeList(List<OfferCodeUpload> arr, String projectId) {
		List<OfferCodeUpload> indexes = new ArrayList<>();
		for (int index = 0; index < arr.size(); index++) {
			if (arr.get(index).getProjectId().equals(projectId)) {
				OfferCodeUpload obj = new OfferCodeUpload();
				obj.setBrandInput(arr.get(index).getBrandInput());
				obj.setBrandName(arr.get(index).getBrandName());
				obj.setOfferCount(arr.get(index).getOfferCount());
				indexes.add(obj);
			}
		}
		return indexes;
	}

	public int getBrandInputFromIndex(List<OfferCodeUpload> list, String brandName, String projectid) {
		int index = -1;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getBrandName().equalsIgnoreCase(brandName) && list.get(i).getProjectId().equalsIgnoreCase(projectid)) {
				index = i;
				break;
			}
		}
		return index;
	}

	public int getBrandInputFromBrand(List<OfferCodeUpload> list, String brandName, String projectid) {
		int index = -1;
		try {
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i).getBrandName().equalsIgnoreCase(brandName) && list.get(i).getProjectId().equalsIgnoreCase(projectid)) {
					index = Integer.parseInt(list.get(i).getBrandInput());
					break;
				}
			}
		}catch (Exception exception) {
			logger.error(exception + Arrays.asList(exception.getStackTrace()).stream().map(Objects::toString)
					.collect(Collectors.joining("\n")));
		}	
		return index;
	}
}