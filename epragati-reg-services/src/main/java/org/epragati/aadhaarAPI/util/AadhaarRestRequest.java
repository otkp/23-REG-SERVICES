package org.epragati.aadhaarAPI.util;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AadhaarRestRequest {

	private String id;
	private String tid;
	private String udc;
	private String ip;
	private String srt;
	private String crt;
	
	@JsonProperty("Skey")
	private String Skey;//--
	
	private String pid;
	
	@JsonProperty("Hmac")
	private String Hmac;//--
	
	private String ci;
	
	private String bt;
	
	private String pincode;
	private String version;
	private String scheme;
	private String department;
	private String service;
	private String consent;
	private String attemptCount;
	private String rdsId;
	private String dpId;
	private String dc;
	private String mi;
	private String mc;
	
	private String rdsVer;
	
	@JsonProperty("IDType")
	private String IDType;//--
	
	@JsonProperty("AllowPDF")
	private String AllowPDF;//--
	
	@JsonProperty("LocalLang")
	private String LocalLang;//--
	
	@JsonProperty("ConsentDesc")
	private String ConsentDesc;//--
	
	@JsonProperty("OTPChannel")
	private String OTPChannel;
	
	private String requesttype; 
	
	private String vercode;
	
	private String oldTid;
	
	private String source;

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
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
	 * @return the udc
	 */
	public String getUdc() {
		return udc;
	}

	/**
	 * @param udc the udc to set
	 */
	public void setUdc(String udc) {
		this.udc = udc;
	}

	/**
	 * @return the ip
	 */
	public String getIp() {
		return ip;
	}

	/**
	 * @param ip the ip to set
	 */
	public void setIp(String ip) {
		this.ip = ip;
	}

	/**
	 * @return the srt
	 */
	public String getSrt() {
		return srt;
	}

	/**
	 * @param srt the srt to set
	 */
	public void setSrt(String srt) {
		this.srt = srt;
	}

	/**
	 * @return the crt
	 */
	public String getCrt() {
		return crt;
	}

	/**
	 * @param crt the crt to set
	 */
	public void setCrt(String crt) {
		this.crt = crt;
	}

	/**
	 * @return the skey
	 */
	public String getSkey() {
		return Skey;
	}

	/**
	 * @param skey the skey to set
	 */
	public void setSkey(String skey) {
		Skey = skey;
	}

	/**
	 * @return the pid
	 */
	public String getPid() {
		return pid;
	}

	/**
	 * @param pid the pid to set
	 */
	public void setPid(String pid) {
		this.pid = pid;
	}

	/**
	 * @return the hmac
	 */
	public String getHmac() {
		return Hmac;
	}

	/**
	 * @param hmac the hmac to set
	 */
	public void setHmac(String hmac) {
		Hmac = hmac;
	}

	/**
	 * @return the ci
	 */
	public String getCi() {
		return ci;
	}

	/**
	 * @param ci the ci to set
	 */
	public void setCi(String ci) {
		this.ci = ci;
	}

	/**
	 * @return the bt
	 */
	public String getBt() {
		return bt;
	}

	/**
	 * @param bt the bt to set
	 */
	public void setBt(String bt) {
		this.bt = bt;
	}

	/**
	 * @return the pincode
	 */
	public String getPincode() {
		return pincode;
	}

	/**
	 * @param pincode the pincode to set
	 */
	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	/**
	 * @return the version
	 */
	public String getVersion() {
		return version;
	}

	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}

	/**
	 * @return the scheme
	 */
	public String getScheme() {
		return scheme;
	}

	/**
	 * @param scheme the scheme to set
	 */
	public void setScheme(String scheme) {
		this.scheme = scheme;
	}

	/**
	 * @return the department
	 */
	public String getDepartment() {
		return department;
	}

	/**
	 * @param department the department to set
	 */
	public void setDepartment(String department) {
		this.department = department;
	}

	/**
	 * @return the service
	 */
	public String getService() {
		return service;
	}

	/**
	 * @param service the service to set
	 */
	public void setService(String service) {
		this.service = service;
	}

	/**
	 * @return the consent
	 */
	public String getConsent() {
		return consent;
	}

	/**
	 * @param consent the consent to set
	 */
	public void setConsent(String consent) {
		this.consent = consent;
	}

	/**
	 * @return the attemptCount
	 */
	public String getAttemptCount() {
		return attemptCount;
	}

	/**
	 * @param attemptCount the attemptCount to set
	 */
	public void setAttemptCount(String attemptCount) {
		this.attemptCount = attemptCount;
	}

	/**
	 * @return the rdsId
	 */
	public String getRdsId() {
		return rdsId;
	}

	/**
	 * @param rdsId the rdsId to set
	 */
	public void setRdsId(String rdsId) {
		this.rdsId = rdsId;
	}

	/**
	 * @return the dpId
	 */
	public String getDpId() {
		return dpId;
	}

	/**
	 * @param dpId the dpId to set
	 */
	public void setDpId(String dpId) {
		this.dpId = dpId;
	}

	/**
	 * @return the dc
	 */
	public String getDc() {
		return dc;
	}

	/**
	 * @param dc the dc to set
	 */
	public void setDc(String dc) {
		this.dc = dc;
	}

	/**
	 * @return the mi
	 */
	public String getMi() {
		return mi;
	}

	/**
	 * @param mi the mi to set
	 */
	public void setMi(String mi) {
		this.mi = mi;
	}

	/**
	 * @return the mc
	 */
	public String getMc() {
		return mc;
	}

	/**
	 * @param mc the mc to set
	 */
	public void setMc(String mc) {
		this.mc = mc;
	}

	/**
	 * @return the rdsVer
	 */
	public String getRdsVer() {
		return rdsVer;
	}

	/**
	 * @param rdsVer the rdsVer to set
	 */
	public void setRdsVer(String rdsVer) {
		this.rdsVer = rdsVer;
	}

	/**
	 * @return the iDType
	 */
	public String getIDType() {
		return IDType;
	}

	/**
	 * @param iDType the iDType to set
	 */
	public void setIDType(String iDType) {
		IDType = iDType;
	}

	/**
	 * @return the allowPDF
	 */
	public String getAllowPDF() {
		return AllowPDF;
	}

	/**
	 * @param allowPDF the allowPDF to set
	 */
	public void setAllowPDF(String allowPDF) {
		AllowPDF = allowPDF;
	}

	/**
	 * @return the localLang
	 */
	public String getLocalLang() {
		return LocalLang;
	}

	/**
	 * @param localLang the localLang to set
	 */
	public void setLocalLang(String localLang) {
		LocalLang = localLang;
	}

	/**
	 * @return the consentDesc
	 */
	public String getConsentDesc() {
		return ConsentDesc;
	}

	/**
	 * @param consentDesc the consentDesc to set
	 */
	public void setConsentDesc(String consentDesc) {
		ConsentDesc = consentDesc;
	}
	

	/**
	 * @return the oTPChannel
	 */
	public String getOTPChannel() {
		return OTPChannel;
	}

	
	public void setOTPChannel(String oTPChannel) {
		OTPChannel = oTPChannel;
	}
	
	

	/**
	 * @return the requesttype
	 */
	public String getRequesttype() {
		return requesttype;
	}

	/**
	 * @param requesttype the requesttype to set
	 */
	public void setRequesttype(String requesttype) {
		this.requesttype = requesttype;
	}
	
	

	/**
	 * @return the vercode
	 */
	public String getVercode() {
		return vercode;
	}

	/**
	 * @param vercode the vercode to set
	 */
	public void setVercode(String vercode) {
		this.vercode = vercode;
	}
	
	

	/**
	 * @return the oldTid
	 */
	public String getOldTid() {
		return oldTid;
	}

	/**
	 * @param oldTid the oldTid to set
	 */
	public void setOldTid(String oldTid) {
		this.oldTid = oldTid;
	}

	
	
	/**
	 * @return the source
	 */
	public String getSource() {
		return source;
	}

	/**
	 * @param source the source to set
	 */
	public void setSource(String source) {
		this.source = source;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "AadhaarRestRequest [" + (id != null ? "id=" + id + ", " : "") + (tid != null ? "tid=" + tid + ", " : "")
				+ (udc != null ? "udc=" + udc + ", " : "") + (ip != null ? "ip=" + ip + ", " : "")
				+ (srt != null ? "srt=" + srt + ", " : "") + (crt != null ? "crt=" + crt + ", " : "")
				+ (Skey != null ? "Skey=" + Skey + ", " : "") + (pid != null ? "pid=" + pid + ", " : "")
				+ (Hmac != null ? "Hmac=" + Hmac + ", " : "") + (ci != null ? "ci=" + ci + ", " : "")
				+ (bt != null ? "bt=" + bt + ", " : "") + (pincode != null ? "pincode=" + pincode + ", " : "")
				+ (version != null ? "version=" + version + ", " : "")
				+ (scheme != null ? "scheme=" + scheme + ", " : "")
				+ (department != null ? "department=" + department + ", " : "")
				+ (service != null ? "service=" + service + ", " : "")
				+ (consent != null ? "consent=" + consent + ", " : "")
				+ (attemptCount != null ? "attemptCount=" + attemptCount + ", " : "")
				+ (rdsId != null ? "rdsId=" + rdsId + ", " : "") + (dpId != null ? "dpId=" + dpId + ", " : "")
				+ (dc != null ? "dc=" + dc + ", " : "") + (mi != null ? "mi=" + mi + ", " : "")
				+ (mc != null ? "mc=" + mc + ", " : "") + (rdsVer != null ? "rdsVer=" + rdsVer + ", " : "")
				+ (IDType != null ? "IDType=" + IDType + ", " : "")
				+ (AllowPDF != null ? "AllowPDF=" + AllowPDF + ", " : "")
				+ (LocalLang != null ? "LocalLang=" + LocalLang + ", " : "")
				+ (ConsentDesc != null ? "ConsentDesc=" + ConsentDesc + ", " : "")
				+ (OTPChannel != null ? "OTPChannel=" + OTPChannel + ", " : "")
				+ (requesttype != null ? "requesttype=" + requesttype + ", " : "")
				+ (vercode != null ? "vercode=" + vercode + ", " : "") + (oldTid != null ? "oldTid=" + oldTid : "")
				+ "]";
	}

}
