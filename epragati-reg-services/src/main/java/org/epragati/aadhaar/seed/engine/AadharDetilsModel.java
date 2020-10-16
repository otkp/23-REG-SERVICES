package org.epragati.aadhaar.seed.engine;

import org.epragati.aadhaar.AadhaarDetailsRequestVO;
import org.epragati.master.vo.ApplicantAddressVO;


/**
 * @author sairam.cheruku
 *
 */
public class AadharDetilsModel {

	private String prNo;
	
	private String officeCode;
	
	private AadhaarDetailsRequestVO model;
	
	private String aadharNumber;
	
	private String mobileNo;
	
	private String emailId;
	
	private ApplicantAddressVO presentAddress;
	
	/**
	 * @return the presentAddress
	 */
	public ApplicantAddressVO getPresentAddress() {
		return presentAddress;
	}

	/**
	 * @param presentAddress the presentAddress to set
	 */
	public void setPresentAddress(ApplicantAddressVO presentAddress) {
		this.presentAddress = presentAddress;
	}

	/**
	 * @return the mobileNo
	 */
	public String getMobileNo() {
		return mobileNo;
	}

	/**
	 * @param mobileNo the mobileNo to set
	 */
	public void setMobileNo(String mobileNo) {
		this.mobileNo = mobileNo;
	}

	/**
	 * @return the emailId
	 */
	public String getEmailId() {
		return emailId;
	}

	/**
	 * @param emailId the emailId to set
	 */
	public void setEmailId(String emailId) {
		this.emailId = emailId;
	}

	public String getAadharNumber() {
		return aadharNumber;
	}

	public void setAadharNumber(String aadharNumber) {
		this.aadharNumber = aadharNumber;
	}

	/**
	 * @return the prNo
	 */
	public String getPrNo() {
		return prNo;
	}

	/**
	 * @param prNo the prNo to set
	 */
	public void setPrNo(String prNo) {
		this.prNo = prNo;
	}

	/**
	 * @return the officeCode
	 */
	public String getOfficeCode() {
		return officeCode;
	}

	/**
	 * @param officeCode the officeCode to set
	 */
	public void setOfficeCode(String officeCode) {
		this.officeCode = officeCode;
	}

	/**
	 * @return the model
	 */
	public AadhaarDetailsRequestVO getModel() {
		return model;
	}

	/**
	 * @param model the model to set
	 */
	public void setModel(AadhaarDetailsRequestVO model) {
		this.model = model;
	}
	
	

}
