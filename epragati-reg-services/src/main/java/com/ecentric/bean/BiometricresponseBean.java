package com.ecentric.bean;

import java.io.Serializable;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class BiometricresponseBean implements Serializable{

	/**
	 * @author srikanth.bommasani
	 */
	private static final long serialVersionUID = 1L;
	
	protected String auth_status;
	 protected String auth_err_code;
	 protected String auth_date;
	 protected String auth_reason;
	 protected String auth_transaction_code;
	 protected String rrn;
	 protected String phoneNumber;
	 protected String name;
	 protected String gender;
	 protected String dob;
	 protected String UIDAIeKYCTxn;
	 protected String base64file;
	 protected String KSA_KUA_Txn;
	 protected String email;
	 protected String Pincode;
	 protected String Eid;
	 protected String Uid;
	 protected String Careof;
	 protected String street;
	 protected String District;
	 protected String Mandal;
	 protected String Village;

	 protected String BuildingName;
	 protected String District_name;
	 protected String Mandal_name;
	 protected String Village_name;
	 protected String srdhwstxn;
	 protected String statecode;
	 protected String vtc;
	 protected String subdist;
	 protected String po;
	 protected String landmark;
	 protected String house; 
	 protected String co;
	 protected String lc;
	 private UUID uuId;
	 private String orgnlAuth_Status;
	 private String orgnlAuth_ErrorCode;
	 
	public String getAuth_status() {
		return auth_status;
	}

	public void setAuth_status(String auth_status) {
		this.setOrgnlAuth_Status(auth_status);
		if (auth_status.equals("100"))
			auth_status = "SUCCESS";
		else
			auth_status = "FAILED";

		this.auth_status = auth_status;
	}
	public String getAuth_err_code() {
		return auth_err_code;
	}
	public void setAuth_err_code(String auth_err_code) {
		this.setOrgnlAuth_ErrorCode(auth_err_code);/*assigning getAuth_reason to auth_errorCode for apts*/
		this.auth_err_code = auth_err_code;
	}
	public String getAuth_date() {
		return auth_date;
	}
	public void setAuth_date(String auth_date) {
		this.auth_date = auth_date;
	}
	public String getAuth_reason() {
		return auth_reason;
	}
	public void setAuth_reason(String auth_reason) {
		this.auth_reason = auth_reason;
	}
	public String getAuth_transaction_code() {
		return auth_transaction_code;
	}
	public void setAuth_transaction_code(String auth_transaction_code) {
		this.auth_transaction_code = auth_transaction_code;
	}
	public String getRrn() {
		return rrn;
	}
	public void setRrn(String rrn) {
		this.rrn = rrn;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getGender() {
		return gender;
	}
	public void setGender(String gender) {
		this.gender = gender;
	}
	public String getDob() {
		return dob;
	}
	public void setDob(String dob) {
		this.dob = dob;
	}
	public String getUIDAIeKYCTxn() {
		return UIDAIeKYCTxn;
	}
	public void setUIDAIeKYCTxn(String uIDAIeKYCTxn) {
		UIDAIeKYCTxn = uIDAIeKYCTxn;
	}
	public String getBase64file() {
		return base64file;
	}
	public void setBase64file(String base64file) {
		this.base64file = base64file;
	}
	@JsonProperty(value ="KSA_KUA_Txn" )
	public String getKSA_KUA_Txn() {
		return KSA_KUA_Txn;
	}
	
	public void setKSA_KUA_Txn(String KSA_KUA_Txn) {
		this.KSA_KUA_Txn = KSA_KUA_Txn;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPincode() {
		return Pincode;
	}
	public void setPincode(String pincode) {
		Pincode = pincode;
	}
	public String getEid() {
		return Eid;
	}
	public void setEid(String eid) {
		Eid = eid;
	}
	public String getUid() {
		return Uid;
	}
	public void setUid(String uid) {
		Uid = uid;
	}
	public String getCareof() {
		return Careof;
	}
	public void setCareof(String careof) {
		Careof = careof;
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public String getDistrict() {
		return District;
	}
	public void setDistrict(String district) {
		District = district;
	}
	public String getMandal() {
		return Mandal;
	}
	public void setMandal(String mandal) {
		Mandal = mandal;
	}
	public String getVillage() {
		return Village;
	}
	public void setVillage(String village) {
		Village = village;
	}
	public String getBuildingName() {
		return BuildingName;
	}
	public void setBuildingName(String buildingName) {
		BuildingName = buildingName;
	}
	public String getDistrict_name() {
		return District_name;
	}
	public void setDistrict_name(String district_name) {
		District_name = district_name;
	}
	public String getMandal_name() {
		return Mandal_name;
	}
	public void setMandal_name(String mandal_name) {
		Mandal_name = mandal_name;
	}
	public String getVillage_name() {
		return Village_name;
	}
	public void setVillage_name(String village_name) {
		Village_name = village_name;
	}
	public String getSrdhwstxn() {
		return srdhwstxn;
	}
	public void setSrdhwstxn(String srdhwstxn) {
		this.srdhwstxn = srdhwstxn;
	}
	public String getStatecode() {
		return statecode;
	}
	public void setStatecode(String statecode) {
		this.statecode = statecode;
	}
	public String getVtc() {
		return vtc;
	}
	public void setVtc(String vtc) {
		this.vtc = vtc;
	}
	public String getSubdist() {
		return subdist;
	}
	public void setSubdist(String subdist) {
		this.subdist = subdist;
	}
	public String getPo() {
		return po;
	}
	public void setPo(String po) {
		this.po = po;
	}
	public String getLandmark() {
		return landmark;
	}
	public void setLandmark(String landmark) {
		this.landmark = landmark;
	}
	public String getHouse() {
		return house;
	}
	public void setHouse(String house) {
		this.house = house;
	}
	public String getCo() {
		return co;
	}
	public void setCo(String co) {
		this.co = co;
	}
	public String getLc() {
		return lc;
	}
	public void setLc(String lc) {
		this.lc = lc;
	}
	public UUID getUuId() {
		return uuId;
	}
	public void setUuId(UUID uuId) {
		this.uuId = uuId;
	}
	/**
	 * @return the orgnlAuth_Status
	 */
	public String getOrgnlAuth_Status() {
		return orgnlAuth_Status;
	}
	/**
	 * @param orgnlAuth_Status the orgnlAuth_Status to set
	 */
	public void setOrgnlAuth_Status(String orgnlAuth_Status) {
		this.orgnlAuth_Status = orgnlAuth_Status;
	}

	/**
	 * @return the orgnlAuth_ErrorCode
	 */
	public String getOrgnlAuth_ErrorCode() {
		return orgnlAuth_ErrorCode;
	}

	/**
	 * @param orgnlAuth_ErrorCode the orgnlAuth_ErrorCode to set
	 */
	public void setOrgnlAuth_ErrorCode(String orgnlAuth_ErrorCode) {
		this.orgnlAuth_ErrorCode = orgnlAuth_ErrorCode;
	}
	 
	 
	 

}
