package org.epragati.aadhaarAPI;

import java.io.Serializable;
import java.util.UUID;

/**
 * AadharUserDetailsResponseModel for maintain client required  aadhar details 
 * and we sending the object to client as response result.
 * @author naga.pulaparthi
 *
 */
public class AadharUserDetailsResponseModel implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String KSA_KUA_Txn;
	private String auth_err_code;
	private String auth_date;
	private String auth_status;
	private String auth_transaction_code;
	private String base64file;
	private String co;
	private String district;
	private String district_name;
	private String dob;
	private String gender;
	private String house;
	private String lc;
	private String mandal;
	private String mandal_name;
	private String name;
	private String pincode;
	private String po;
	private String statecode;
	private String street;
	private String subdist;
	private Long uid;
	private String village;
	private String village_name;
	private String vtc;
	private boolean isInsideAP;
	private boolean isStateMatched;
	private boolean isDistrictMatched;
	private boolean isMandalMatched;
	private String stateMatchedCode;
	private String districtMatchedCode;
	private String mandalMatchedCode;
	private Integer age;
	private String applicationNumber;
	private String firstName;
	private String lastName;
	private String nationality;
	private String doorNo;
	private String country;
	private String phone;
	private UUID uuId;
	private String email;
	
	private String tid;
	private String uidToken;
	

	public String getAuth_err_code()
	{
		return this.auth_err_code;
	}

	public void setAuth_err_code(String auth_err_code)
	{
		this.auth_err_code = auth_err_code;
	}

	public String getKSA_KUA_Txn()
	{
		return this.KSA_KUA_Txn;
	}

	public void setKSA_KUA_Txn(String kSA_KUA_Txn)
	{
		this.KSA_KUA_Txn = kSA_KUA_Txn;
	}

	public String getAuth_date()
	{
		return this.auth_date;
	}

	public void setAuth_date(String auth_date)
	{
		this.auth_date = auth_date;
	}

	public String getAuth_status()
	{
		return this.auth_status;
	}

	public void setAuth_status(String auth_status)
	{
		this.auth_status = auth_status;
	}

	public String getAuth_transaction_code()
	{
		return this.auth_transaction_code;
	}

	public void setAuth_transaction_code(String auth_transaction_code)
	{
		this.auth_transaction_code = auth_transaction_code;
	}

	public String getBase64file()
	{
		return this.base64file;
	}

	public void setBase64file(String base64file)
	{
		this.base64file = base64file;
	}

	public String getCo()
	{
		return this.co;
	}

	public void setCo(String co)
	{
		this.co = co;
	}

	public String getDistrict()
	{
		return this.district;
	}

	public void setDistrict(String district)
	{
		this.district = district;
	}

	public String getDistrict_name()
	{
		return this.district_name;
	}

	public void setDistrict_name(String district_name)
	{
		this.district_name = district_name;
	}

	public String getDob()
	{
		return this.dob;
	}

	public void setDob(String dob)
	{
		this.dob = dob;
	}

	public String getGender()
	{
		return this.gender;
	}

	public void setGender(String gender)
	{
		this.gender = gender;
	}

	public String getHouse()
	{
		return this.house;
	}

	public void setHouse(String house)
	{
		this.house = house;
	}

	public String getLc()
	{
		return this.lc;
	}

	public void setLc(String lc)
	{
		this.lc = lc;
	}

	public String getMandal()
	{
		return this.mandal;
	}

	public void setMandal(String mandal)
	{
		this.mandal = mandal;
	}

	public String getMandal_name()
	{
		return this.mandal_name;
	}

	public void setMandal_name(String mandal_name)
	{
		this.mandal_name = mandal_name;
	}

	public String getName()
	{
		return this.name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getPincode()
	{
		return this.pincode;
	}

	public void setPincode(String pincode)
	{
		this.pincode = pincode;
	}

	public String getPo()
	{
		return this.po;
	}

	public void setPo(String po)
	{
		this.po = po;
	}

	public String getStatecode()
	{
		return this.statecode;
	}

	public void setStatecode(String statecode)
	{
		this.statecode = statecode;
	}

	public String getStreet()
	{
		return this.street;
	}

	public void setStreet(String street)
	{
		this.street = street;
	}

	public String getSubdist()
	{
		return this.subdist;
	}

	public void setSubdist(String subdist)
	{
		this.subdist = subdist;
	}

	public Long getUid()
	{
		return this.uid;
	}

	public void setUid(Long uid)
	{
		this.uid = uid;
	}

	public String getVillage()
	{
		return this.village;
	}

	public void setVillage(String village)
	{
		this.village = village;
	}

	public String getVillage_name()
	{
		return this.village_name;
	}

	public void setVillage_name(String village_name)
	{
		this.village_name = village_name;
	}

	public String getVtc()
	{
		return this.vtc;
	}

	public void setVtc(String vtc)
	{
		this.vtc = vtc;
	}

	public boolean isInsideAP()
	{
		return this.isInsideAP;
	}

	public void setInsideAP(boolean isInsideAP)
	{
		this.isInsideAP = isInsideAP;
	}

	public boolean isStateMatched()
	{
		return this.isStateMatched;
	}

	public void setStateMatched(boolean isStateMatched)
	{
		this.isStateMatched = isStateMatched;
	}

	public boolean isDistrictMatched()
	{
		return this.isDistrictMatched;
	}

	public void setDistrictMatched(boolean isDistrictMatched)
	{
		this.isDistrictMatched = isDistrictMatched;
	}

	public boolean isMandalMatched()
	{
		return this.isMandalMatched;
	}

	public void setMandalMatched(boolean isMandalMatched)
	{
		this.isMandalMatched = isMandalMatched;
	}

	public String getStateMatchedCode()
	{
		return this.stateMatchedCode;
	}

	public void setStateMatchedCode(String stateMatchedCode)
	{
		this.stateMatchedCode = stateMatchedCode;
	}

	public String getDistrictMatchedCode()
	{
		return this.districtMatchedCode;
	}

	public void setDistrictMatchedCode(String districtMatchedCode)
	{
		this.districtMatchedCode = districtMatchedCode;
	}

	public String getMandalMatchedCode()
	{
		return this.mandalMatchedCode;
	}

	public void setMandalMatchedCode(String mandalMatchedCode)
	{
		this.mandalMatchedCode = mandalMatchedCode;
	}

	public Integer getAge()
	{
		return this.age;
	}

	public void setAge(Integer age)
	{
		this.age = age;
	}

	public String getApplicationNumber()
	{
		return this.applicationNumber;
	}

	public void setApplicationNumber(String applicationNumber)
	{
		this.applicationNumber = applicationNumber;
	}

	public String getFirstName()
	{
		return this.firstName;
	}

	public void setFirstName(String firstName)
	{
		this.firstName = firstName;
	}

	public String getLastName()
	{
		return this.lastName;
	}

	public void setLastName(String lastName)
	{
		this.lastName = lastName;
	}

	public String getNationality()
	{
		return this.nationality;
	}

	public void setNationality(String nationality)
	{
		this.nationality = nationality;
	}

	public String getDoorNo()
	{
		return this.doorNo;
	}

	public void setDoorNo(String doorNo)
	{
		this.doorNo = doorNo;
	}

	public String getCountry()
	{
		return this.country;
	}

	public void setCountry(String country)
	{
		this.country = country;
	}

	public String toString()
	{
		return "AadharModel [KSA_KUA_Txn=" + this.KSA_KUA_Txn + ", auth_err_code=" + this.auth_err_code + ", auth_date=" + this.auth_date + ", auth_status=" + this.auth_status + ", auth_transaction_code=" + this.auth_transaction_code + ", base64file=" + this.base64file + ", co=" + this.co + ", district=" + this.district + ", district_name=" + this.district_name + ", dob=" + this.dob + ", gender=" + this.gender + ", house=" + this.house + ", lc=" + this.lc + ", mandal=" + this.mandal + ", mandal_name=" + this.mandal_name + ", name=" + this.name + ", pincode=" + this.pincode + ", po=" + this.po + ", statecode=" + this.statecode + ", street=" + this.street + ", subdist=" + this.subdist + ", uid=" + this.uid + ", village=" + this.village + ", village_name=" + this.village_name + ", vtc=" + this.vtc + ", isInsideAP=" + this.isInsideAP + ", isStateMatched=" + this.isStateMatched + ", isDistrictMatched=" + this.isDistrictMatched + ", isMandalMatched=" + this.isMandalMatched + "]";
	}

	/**
	 * @return the phone
	 */
	public String getPhone() {
		return phone;
	}

	/**
	 * @param phone the phone to set
	 */
	public void setPhone(String phone) {
		this.phone = phone;
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
	 * @return the email
	 */
	public String getEmail() {
		return email;
	}

	/**
	 * @param email the email to set
	 */
	public void setEmail(String email) {
		this.email = email;
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

	


}