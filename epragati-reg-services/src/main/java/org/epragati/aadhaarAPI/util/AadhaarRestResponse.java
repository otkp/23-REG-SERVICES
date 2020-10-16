package org.epragati.aadhaarAPI.util;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AadhaarRestResponse {

	@JsonProperty(value= "EKYCData")
	private EKYCData ekycData;

	@JsonProperty(value= "TID")
	private String tid;

	@JsonProperty(value= "ReturnCode")
	private String returnCode;

	@JsonProperty(value= "UIDToken")
	private String uidToken;

	@JsonProperty(value= "ReturnMessage")
	private String returnMessage;
	
	private UUID uuId;
	
	private String auth_err_code;
	
	private String auth_status;

	/**
	 * @return the ekycData
	 */
	public EKYCData getEkycData() {
		return ekycData;
	}

	/**
	 * @param ekycData the ekycData to set
	 */
	public void setEkycData(EKYCData ekycData) {
		this.ekycData = ekycData;
	}

	/**
	 * @return the tid
	 */
	public String getTid() {
		return tid;
	}

	/**
	 * @param tid the tid to set
	 */
	public void setTid(String tid) {
		this.tid = tid;
	}

	/**
	 * @return the returnCode
	 */
	public String getReturnCode() {
		return returnCode;
	}

	/**
	 * @param returnCode the returnCode to set
	 */
	public void setReturnCode(String returnCode) {
		this.returnCode = returnCode;
	}

	/**
	 * @return the uidToken
	 */
	public String getUidToken() {
		return uidToken;
	}

	/**
	 * @param uidToken the uidToken to set
	 */
	public void setUidToken(String uidToken) {
		this.uidToken = uidToken;
	}

	/**
	 * @return the returnMessage
	 */
	public String getReturnMessage() {
		return returnMessage;
	}

	/**
	 * @param returnMessage the returnMessage to set
	 */
	public void setReturnMessage(String returnMessage) {
		this.returnMessage = returnMessage;
	}
	

	/**
	 * @return the uuId
	 */
	public UUID getUuId() {
		return uuId;
	}

	/**
	 * @param uuId the uuId to set
	 */
	public void setUuId(UUID uuId) {
		this.uuId = uuId;
	}
	

	/**
	 * @return the auth_err_code
	 */
	public String getAuth_err_code() {
		return auth_err_code;
	}

	/**
	 * @param auth_err_code the auth_err_code to set
	 */
	public void setAuth_err_code(String auth_err_code) {
		this.auth_err_code = auth_err_code;
	}

	/**
	 * @return the auth_status
	 */
	public String getAuth_status() {
		return auth_status;
	}

	/**
	 * @param auth_status the auth_status to set
	 */
	public void setAuth_status(String auth_status) {
		this.auth_status = auth_status;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AadhaarRestResponse [" + (ekycData != null ? "ekycData=" + ekycData + ", " : "")
				+ (tid != null ? "tid=" + tid + ", " : "")
				+ (returnCode != null ? "returnCode=" + returnCode + ", " : "")
				+ (uidToken != null ? "uidToken=" + uidToken + ", " : "")
				+ (returnMessage != null ? "returnMessage=" + returnMessage : "") + "]";
	}
	
}
