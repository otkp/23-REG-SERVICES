package com.ecentric.bean;

import java.io.Serializable;
/**
 * @author srikanth.bommasani
 */
public class otpgenration implements Serializable {
	
	private static final long serialVersionUID = 1L;
	private String auth_Status;
	private String auth_reason;
	private String auth_status;
	/**
	 * @return the auth_Status
	 */
	public String getAuth_Status() {
		return auth_Status;
	}
	/**
	 * @param auth_Status the auth_Status to set
	 */
	public void setAuth_Status(String auth_Status) {
		this.auth_Status = auth_Status;
		this.setAuth_status(auth_Status);
	}
	/**
	 * @return the auth_reason
	 */
	public String getAuth_reason() {
		return auth_reason;
	}
	/**
	 * @param auth_reason the auth_reason to set
	 */
	public void setAuth_reason(String auth_reason) {
		this.auth_reason = auth_reason;
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
	
	

}
