package org.epragati.aadhaarAPI;

import java.util.List;
import java.util.Set;

import org.epragati.aadhaarAPI.util.PIDData;
import org.epragati.common.dto.BaseEntity;
import org.epragati.util.payment.ServiceEnum;

public class AadhaarSourceDTO extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String prNo;

	private String trNo;

	private List<ServiceEnum> serviceType;

	private Set<Integer> serviceIds;

	private String role;

	private String user;

	private String applicationNo;

	private String collectionName;

	private boolean isHPA;

	private PIDData pidData;

	private String service;

	private String aadhaarNo;

	private String status;

	private String reqTrackNo;

	public String getReqTrackNo() {
		return reqTrackNo;
	}

	public void setReqTrackNo(String reqTrackNo) {
		this.reqTrackNo = reqTrackNo;
	}

	public String getAadhaarNo() {
		return aadhaarNo;
	}

	public void setAadhaarNo(String aadhaarNo) {
		this.aadhaarNo = aadhaarNo;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public boolean isHPA() {
		return isHPA;
	}

	public PIDData getPidData() {
		return pidData;
	}

	public void setPidData(PIDData pidData) {
		this.pidData = pidData;
	}

	public void setHPA(boolean isHPA) {
		this.isHPA = isHPA;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getApplicationNo() {
		return applicationNo;
	}

	public void setApplicationNo(String applicationNo) {
		this.applicationNo = applicationNo;
	}

	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}

	public String getPrNo() {
		return prNo;
	}

	public void setPrNo(String prNo) {
		this.prNo = prNo;
	}

	public String getTrNo() {
		return trNo;
	}

	public void setTrNo(String trNo) {
		this.trNo = trNo;
	}

	public List<ServiceEnum> getServiceType() {
		return serviceType;
	}

	public void setServiceType(List<ServiceEnum> serviceType) {
		this.serviceType = serviceType;
	}

	public Set<Integer> getServiceIds() {
		return serviceIds;
	}

	public void setServiceIds(Set<Integer> serviceIds) {
		this.serviceIds = serviceIds;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
