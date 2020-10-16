
package org.tempuri;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="vehicleNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vehicleRegisteredDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vehicleRegistrationValidUpto" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vehicleIssuePlace" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vehicleStatus" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="remarks" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ownershipType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ownerFirstName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ownerMiddileName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ownerLastName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ownerAge" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ownerFatherName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ownerPanNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ownerVoterId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ownerMobileNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ownerAddress1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ownerAddress2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ownerAddress3" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ownerCity" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ownerState" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="ownerPin" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vehicleOldNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="previousRegisteredOfficeName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="previousRegisteredOfficeState" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="governmentVehicle" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="reservedSpecialNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vehicleIsRTC" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="insuranceNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="insuranceCompanyName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="insuranceValidFrom" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="insuranceValidTo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="makerName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="makerClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dealerName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dealerAddress1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dealerAddress2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dealerAddress3" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dealerCity" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dealerState" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dealerAddressPinCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vehicleClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="chassisNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="engineNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vehicleType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="bodyType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="wheelBase" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fuel" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vehicleCC" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="cylenders" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="seatingCapacity" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="driverSeatingCapacity" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="standingCapacity" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="color" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="vehicleIsNewOld" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="manufactureMonthYear" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="horsePower" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="unleadenWeight" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="growssWeightCertificate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="growssWeightRegistrationTime" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fronAxelTyreSizes" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rearAxelTyreSizes" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="otherAxelTyreSizes" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tandomAxelTyreSizes" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="fronWeight" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rearWeight" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="otherAxelWeight" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tandomAxelWeight" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="axelType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="length" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="width" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="height" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="hangingCapacity" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="taxExemption" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="taxPaidOfficeCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="taxPaidStateCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="taxPaymentPeriod" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="taxDemandAmount" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="taxPenaltyAmount" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="taxCollectedAmount" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="taxDemandDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="taxQuarterStartDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="taxValidUpto" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="hypothecationType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="financerName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="financeAggrementDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="financerAddress1" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="financerAddress2" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="financerAddress3" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="financerCity" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="financerState" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="financerPin" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="financerFaxNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="financerPhoneNO" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="financerEmailId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="nocOfficeCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="APPLICATIONNO" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FcNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FcIssuedBy" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FcIssuedDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FcValidFromDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FcValidToDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FcApprovedBy" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="FcChallanNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PermitNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PermitClass" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PermitType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PermitIssueDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PermitValidFromDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PermitValidToDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AuthorizationNo" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AuthFromDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="AuthToDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="RouteDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="GoodsDescription" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PermitLadenWeight" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="PermitRouteType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="OneDistrictPermit" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TwoDistrictPermit" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TransactionDate" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "vehicleNumber",
    "vehicleRegisteredDate",
    "vehicleRegistrationValidUpto",
    "vehicleIssuePlace",
    "vehicleStatus",
    "remarks",
    "ownershipType",
    "ownerFirstName",
    "ownerMiddileName",
    "ownerLastName",
    "ownerAge",
    "ownerFatherName",
    "ownerPanNo",
    "ownerVoterId",
    "ownerMobileNo",
    "ownerAddress1",
    "ownerAddress2",
    "ownerAddress3",
    "ownerCity",
    "ownerState",
    "ownerPin",
    "vehicleOldNo",
    "previousRegisteredOfficeName",
    "previousRegisteredOfficeState",
    "governmentVehicle",
    "reservedSpecialNo",
    "vehicleIsRTC",
    "insuranceNo",
    "insuranceCompanyName",
    "insuranceValidFrom",
    "insuranceValidTo",
    "makerName",
    "makerClass",
    "dealerName",
    "dealerAddress1",
    "dealerAddress2",
    "dealerAddress3",
    "dealerCity",
    "dealerState",
    "dealerAddressPinCode",
    "vehicleClass",
    "chassisNo",
    "engineNo",
    "vehicleType",
    "bodyType",
    "wheelBase",
    "fuel",
    "vehicleCC",
    "cylenders",
    "seatingCapacity",
    "driverSeatingCapacity",
    "standingCapacity",
    "color",
    "vehicleIsNewOld",
    "manufactureMonthYear",
    "horsePower",
    "unleadenWeight",
    "growssWeightCertificate",
    "growssWeightRegistrationTime",
    "fronAxelTyreSizes",
    "rearAxelTyreSizes",
    "otherAxelTyreSizes",
    "tandomAxelTyreSizes",
    "fronWeight",
    "rearWeight",
    "otherAxelWeight",
    "tandomAxelWeight",
    "axelType",
    "length",
    "width",
    "height",
    "hangingCapacity",
    "taxExemption",
    "taxPaidOfficeCode",
    "taxPaidStateCode",
    "taxPaymentPeriod",
    "taxDemandAmount",
    "taxPenaltyAmount",
    "taxCollectedAmount",
    "taxDemandDate",
    "taxQuarterStartDate",
    "taxValidUpto",
    "hypothecationType",
    "financerName",
    "financeAggrementDate",
    "financerAddress1",
    "financerAddress2",
    "financerAddress3",
    "financerCity",
    "financerState",
    "financerPin",
    "financerFaxNo",
    "financerPhoneNO",
    "financerEmailId",
    "nocOfficeCode",
    "applicationno",
    "fcNumber",
    "fcIssuedBy",
    "fcIssuedDate",
    "fcValidFromDate",
    "fcValidToDate",
    "fcApprovedBy",
    "fcChallanNo",
    "permitNo",
    "permitClass",
    "permitType",
    "permitIssueDate",
    "permitValidFromDate",
    "permitValidToDate",
    "authorizationNo",
    "authFromDate",
    "authToDate",
    "routeDescription",
    "goodsDescription",
    "permitLadenWeight",
    "permitRouteType",
    "oneDistrictPermit",
    "twoDistrictPermit",
    "transactionDate"
})
@XmlRootElement(name = "OtsiLiveDataSyncToCMS")
public class OtsiLiveDataSyncToCMS {

    protected String vehicleNumber;
    protected String vehicleRegisteredDate;
    protected String vehicleRegistrationValidUpto;
    protected String vehicleIssuePlace;
    protected String vehicleStatus;
    protected String remarks;
    protected String ownershipType;
    protected String ownerFirstName;
    protected String ownerMiddileName;
    protected String ownerLastName;
    protected String ownerAge;
    protected String ownerFatherName;
    protected String ownerPanNo;
    protected String ownerVoterId;
    protected String ownerMobileNo;
    protected String ownerAddress1;
    protected String ownerAddress2;
    protected String ownerAddress3;
    protected String ownerCity;
    protected String ownerState;
    protected String ownerPin;
    protected String vehicleOldNo;
    protected String previousRegisteredOfficeName;
    protected String previousRegisteredOfficeState;
    protected String governmentVehicle;
    protected String reservedSpecialNo;
    protected String vehicleIsRTC;
    protected String insuranceNo;
    protected String insuranceCompanyName;
    protected String insuranceValidFrom;
    protected String insuranceValidTo;
    protected String makerName;
    protected String makerClass;
    protected String dealerName;
    protected String dealerAddress1;
    protected String dealerAddress2;
    protected String dealerAddress3;
    protected String dealerCity;
    protected String dealerState;
    protected String dealerAddressPinCode;
    protected String vehicleClass;
    protected String chassisNo;
    protected String engineNo;
    protected String vehicleType;
    protected String bodyType;
    protected String wheelBase;
    protected String fuel;
    protected String vehicleCC;
    protected String cylenders;
    protected String seatingCapacity;
    protected String driverSeatingCapacity;
    protected String standingCapacity;
    protected String color;
    protected String vehicleIsNewOld;
    protected String manufactureMonthYear;
    protected String horsePower;
    protected String unleadenWeight;
    protected String growssWeightCertificate;
    protected String growssWeightRegistrationTime;
    protected String fronAxelTyreSizes;
    protected String rearAxelTyreSizes;
    protected String otherAxelTyreSizes;
    protected String tandomAxelTyreSizes;
    protected String fronWeight;
    protected String rearWeight;
    protected String otherAxelWeight;
    protected String tandomAxelWeight;
    protected String axelType;
    protected String length;
    protected String width;
    protected String height;
    protected String hangingCapacity;
    protected String taxExemption;
    protected String taxPaidOfficeCode;
    protected String taxPaidStateCode;
    protected String taxPaymentPeriod;
    protected String taxDemandAmount;
    protected String taxPenaltyAmount;
    protected String taxCollectedAmount;
    protected String taxDemandDate;
    protected String taxQuarterStartDate;
    protected String taxValidUpto;
    protected String hypothecationType;
    protected String financerName;
    protected String financeAggrementDate;
    protected String financerAddress1;
    protected String financerAddress2;
    protected String financerAddress3;
    protected String financerCity;
    protected String financerState;
    protected String financerPin;
    protected String financerFaxNo;
    protected String financerPhoneNO;
    protected String financerEmailId;
    protected String nocOfficeCode;
    @XmlElement(name = "APPLICATIONNO")
    protected String applicationno;
    @XmlElement(name = "FcNumber")
    protected String fcNumber;
    @XmlElement(name = "FcIssuedBy")
    protected String fcIssuedBy;
    @XmlElement(name = "FcIssuedDate")
    protected String fcIssuedDate;
    @XmlElement(name = "FcValidFromDate")
    protected String fcValidFromDate;
    @XmlElement(name = "FcValidToDate")
    protected String fcValidToDate;
    @XmlElement(name = "FcApprovedBy")
    protected String fcApprovedBy;
    @XmlElement(name = "FcChallanNo")
    protected String fcChallanNo;
    @XmlElement(name = "PermitNo")
    protected String permitNo;
    @XmlElement(name = "PermitClass")
    protected String permitClass;
    @XmlElement(name = "PermitType")
    protected String permitType;
    @XmlElement(name = "PermitIssueDate")
    protected String permitIssueDate;
    @XmlElement(name = "PermitValidFromDate")
    protected String permitValidFromDate;
    @XmlElement(name = "PermitValidToDate")
    protected String permitValidToDate;
    @XmlElement(name = "AuthorizationNo")
    protected String authorizationNo;
    @XmlElement(name = "AuthFromDate")
    protected String authFromDate;
    @XmlElement(name = "AuthToDate")
    protected String authToDate;
    @XmlElement(name = "RouteDescription")
    protected String routeDescription;
    @XmlElement(name = "GoodsDescription")
    protected String goodsDescription;
    @XmlElement(name = "PermitLadenWeight")
    protected String permitLadenWeight;
    @XmlElement(name = "PermitRouteType")
    protected String permitRouteType;
    @XmlElement(name = "OneDistrictPermit")
    protected String oneDistrictPermit;
    @XmlElement(name = "TwoDistrictPermit")
    protected String twoDistrictPermit;
    @XmlElement(name = "TransactionDate")
    protected String transactionDate;

    /**
     * Gets the value of the vehicleNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVehicleNumber() {
        return vehicleNumber;
    }

    /**
     * Sets the value of the vehicleNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVehicleNumber(String value) {
        this.vehicleNumber = value;
    }

    /**
     * Gets the value of the vehicleRegisteredDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVehicleRegisteredDate() {
        return vehicleRegisteredDate;
    }

    /**
     * Sets the value of the vehicleRegisteredDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVehicleRegisteredDate(String value) {
        this.vehicleRegisteredDate = value;
    }

    /**
     * Gets the value of the vehicleRegistrationValidUpto property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVehicleRegistrationValidUpto() {
        return vehicleRegistrationValidUpto;
    }

    /**
     * Sets the value of the vehicleRegistrationValidUpto property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVehicleRegistrationValidUpto(String value) {
        this.vehicleRegistrationValidUpto = value;
    }

    /**
     * Gets the value of the vehicleIssuePlace property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVehicleIssuePlace() {
        return vehicleIssuePlace;
    }

    /**
     * Sets the value of the vehicleIssuePlace property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVehicleIssuePlace(String value) {
        this.vehicleIssuePlace = value;
    }

    /**
     * Gets the value of the vehicleStatus property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVehicleStatus() {
        return vehicleStatus;
    }

    /**
     * Sets the value of the vehicleStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVehicleStatus(String value) {
        this.vehicleStatus = value;
    }

    /**
     * Gets the value of the remarks property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRemarks() {
        return remarks;
    }

    /**
     * Sets the value of the remarks property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRemarks(String value) {
        this.remarks = value;
    }

    /**
     * Gets the value of the ownershipType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwnershipType() {
        return ownershipType;
    }

    /**
     * Sets the value of the ownershipType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwnershipType(String value) {
        this.ownershipType = value;
    }

    /**
     * Gets the value of the ownerFirstName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwnerFirstName() {
        return ownerFirstName;
    }

    /**
     * Sets the value of the ownerFirstName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwnerFirstName(String value) {
        this.ownerFirstName = value;
    }

    /**
     * Gets the value of the ownerMiddileName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwnerMiddileName() {
        return ownerMiddileName;
    }

    /**
     * Sets the value of the ownerMiddileName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwnerMiddileName(String value) {
        this.ownerMiddileName = value;
    }

    /**
     * Gets the value of the ownerLastName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwnerLastName() {
        return ownerLastName;
    }

    /**
     * Sets the value of the ownerLastName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwnerLastName(String value) {
        this.ownerLastName = value;
    }

    /**
     * Gets the value of the ownerAge property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwnerAge() {
        return ownerAge;
    }

    /**
     * Sets the value of the ownerAge property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwnerAge(String value) {
        this.ownerAge = value;
    }

    /**
     * Gets the value of the ownerFatherName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwnerFatherName() {
        return ownerFatherName;
    }

    /**
     * Sets the value of the ownerFatherName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwnerFatherName(String value) {
        this.ownerFatherName = value;
    }

    /**
     * Gets the value of the ownerPanNo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwnerPanNo() {
        return ownerPanNo;
    }

    /**
     * Sets the value of the ownerPanNo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwnerPanNo(String value) {
        this.ownerPanNo = value;
    }

    /**
     * Gets the value of the ownerVoterId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwnerVoterId() {
        return ownerVoterId;
    }

    /**
     * Sets the value of the ownerVoterId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwnerVoterId(String value) {
        this.ownerVoterId = value;
    }

    /**
     * Gets the value of the ownerMobileNo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwnerMobileNo() {
        return ownerMobileNo;
    }

    /**
     * Sets the value of the ownerMobileNo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwnerMobileNo(String value) {
        this.ownerMobileNo = value;
    }

    /**
     * Gets the value of the ownerAddress1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwnerAddress1() {
        return ownerAddress1;
    }

    /**
     * Sets the value of the ownerAddress1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwnerAddress1(String value) {
        this.ownerAddress1 = value;
    }

    /**
     * Gets the value of the ownerAddress2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwnerAddress2() {
        return ownerAddress2;
    }

    /**
     * Sets the value of the ownerAddress2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwnerAddress2(String value) {
        this.ownerAddress2 = value;
    }

    /**
     * Gets the value of the ownerAddress3 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwnerAddress3() {
        return ownerAddress3;
    }

    /**
     * Sets the value of the ownerAddress3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwnerAddress3(String value) {
        this.ownerAddress3 = value;
    }

    /**
     * Gets the value of the ownerCity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwnerCity() {
        return ownerCity;
    }

    /**
     * Sets the value of the ownerCity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwnerCity(String value) {
        this.ownerCity = value;
    }

    /**
     * Gets the value of the ownerState property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwnerState() {
        return ownerState;
    }

    /**
     * Sets the value of the ownerState property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwnerState(String value) {
        this.ownerState = value;
    }

    /**
     * Gets the value of the ownerPin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOwnerPin() {
        return ownerPin;
    }

    /**
     * Sets the value of the ownerPin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOwnerPin(String value) {
        this.ownerPin = value;
    }

    /**
     * Gets the value of the vehicleOldNo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVehicleOldNo() {
        return vehicleOldNo;
    }

    /**
     * Sets the value of the vehicleOldNo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVehicleOldNo(String value) {
        this.vehicleOldNo = value;
    }

    /**
     * Gets the value of the previousRegisteredOfficeName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreviousRegisteredOfficeName() {
        return previousRegisteredOfficeName;
    }

    /**
     * Sets the value of the previousRegisteredOfficeName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreviousRegisteredOfficeName(String value) {
        this.previousRegisteredOfficeName = value;
    }

    /**
     * Gets the value of the previousRegisteredOfficeState property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPreviousRegisteredOfficeState() {
        return previousRegisteredOfficeState;
    }

    /**
     * Sets the value of the previousRegisteredOfficeState property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPreviousRegisteredOfficeState(String value) {
        this.previousRegisteredOfficeState = value;
    }

    /**
     * Gets the value of the governmentVehicle property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGovernmentVehicle() {
        return governmentVehicle;
    }

    /**
     * Sets the value of the governmentVehicle property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGovernmentVehicle(String value) {
        this.governmentVehicle = value;
    }

    /**
     * Gets the value of the reservedSpecialNo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReservedSpecialNo() {
        return reservedSpecialNo;
    }

    /**
     * Sets the value of the reservedSpecialNo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReservedSpecialNo(String value) {
        this.reservedSpecialNo = value;
    }

    /**
     * Gets the value of the vehicleIsRTC property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVehicleIsRTC() {
        return vehicleIsRTC;
    }

    /**
     * Sets the value of the vehicleIsRTC property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVehicleIsRTC(String value) {
        this.vehicleIsRTC = value;
    }

    /**
     * Gets the value of the insuranceNo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInsuranceNo() {
        return insuranceNo;
    }

    /**
     * Sets the value of the insuranceNo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInsuranceNo(String value) {
        this.insuranceNo = value;
    }

    /**
     * Gets the value of the insuranceCompanyName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInsuranceCompanyName() {
        return insuranceCompanyName;
    }

    /**
     * Sets the value of the insuranceCompanyName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInsuranceCompanyName(String value) {
        this.insuranceCompanyName = value;
    }

    /**
     * Gets the value of the insuranceValidFrom property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInsuranceValidFrom() {
        return insuranceValidFrom;
    }

    /**
     * Sets the value of the insuranceValidFrom property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInsuranceValidFrom(String value) {
        this.insuranceValidFrom = value;
    }

    /**
     * Gets the value of the insuranceValidTo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getInsuranceValidTo() {
        return insuranceValidTo;
    }

    /**
     * Sets the value of the insuranceValidTo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInsuranceValidTo(String value) {
        this.insuranceValidTo = value;
    }

    /**
     * Gets the value of the makerName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMakerName() {
        return makerName;
    }

    /**
     * Sets the value of the makerName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMakerName(String value) {
        this.makerName = value;
    }

    /**
     * Gets the value of the makerClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMakerClass() {
        return makerClass;
    }

    /**
     * Sets the value of the makerClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMakerClass(String value) {
        this.makerClass = value;
    }

    /**
     * Gets the value of the dealerName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDealerName() {
        return dealerName;
    }

    /**
     * Sets the value of the dealerName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDealerName(String value) {
        this.dealerName = value;
    }

    /**
     * Gets the value of the dealerAddress1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDealerAddress1() {
        return dealerAddress1;
    }

    /**
     * Sets the value of the dealerAddress1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDealerAddress1(String value) {
        this.dealerAddress1 = value;
    }

    /**
     * Gets the value of the dealerAddress2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDealerAddress2() {
        return dealerAddress2;
    }

    /**
     * Sets the value of the dealerAddress2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDealerAddress2(String value) {
        this.dealerAddress2 = value;
    }

    /**
     * Gets the value of the dealerAddress3 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDealerAddress3() {
        return dealerAddress3;
    }

    /**
     * Sets the value of the dealerAddress3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDealerAddress3(String value) {
        this.dealerAddress3 = value;
    }

    /**
     * Gets the value of the dealerCity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDealerCity() {
        return dealerCity;
    }

    /**
     * Sets the value of the dealerCity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDealerCity(String value) {
        this.dealerCity = value;
    }

    /**
     * Gets the value of the dealerState property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDealerState() {
        return dealerState;
    }

    /**
     * Sets the value of the dealerState property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDealerState(String value) {
        this.dealerState = value;
    }

    /**
     * Gets the value of the dealerAddressPinCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDealerAddressPinCode() {
        return dealerAddressPinCode;
    }

    /**
     * Sets the value of the dealerAddressPinCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDealerAddressPinCode(String value) {
        this.dealerAddressPinCode = value;
    }

    /**
     * Gets the value of the vehicleClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVehicleClass() {
        return vehicleClass;
    }

    /**
     * Sets the value of the vehicleClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVehicleClass(String value) {
        this.vehicleClass = value;
    }

    /**
     * Gets the value of the chassisNo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getChassisNo() {
        return chassisNo;
    }

    /**
     * Sets the value of the chassisNo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setChassisNo(String value) {
        this.chassisNo = value;
    }

    /**
     * Gets the value of the engineNo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEngineNo() {
        return engineNo;
    }

    /**
     * Sets the value of the engineNo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEngineNo(String value) {
        this.engineNo = value;
    }

    /**
     * Gets the value of the vehicleType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVehicleType() {
        return vehicleType;
    }

    /**
     * Sets the value of the vehicleType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVehicleType(String value) {
        this.vehicleType = value;
    }

    /**
     * Gets the value of the bodyType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBodyType() {
        return bodyType;
    }

    /**
     * Sets the value of the bodyType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBodyType(String value) {
        this.bodyType = value;
    }

    /**
     * Gets the value of the wheelBase property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWheelBase() {
        return wheelBase;
    }

    /**
     * Sets the value of the wheelBase property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWheelBase(String value) {
        this.wheelBase = value;
    }

    /**
     * Gets the value of the fuel property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFuel() {
        return fuel;
    }

    /**
     * Sets the value of the fuel property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFuel(String value) {
        this.fuel = value;
    }

    /**
     * Gets the value of the vehicleCC property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVehicleCC() {
        return vehicleCC;
    }

    /**
     * Sets the value of the vehicleCC property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVehicleCC(String value) {
        this.vehicleCC = value;
    }

    /**
     * Gets the value of the cylenders property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCylenders() {
        return cylenders;
    }

    /**
     * Sets the value of the cylenders property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCylenders(String value) {
        this.cylenders = value;
    }

    /**
     * Gets the value of the seatingCapacity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSeatingCapacity() {
        return seatingCapacity;
    }

    /**
     * Sets the value of the seatingCapacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSeatingCapacity(String value) {
        this.seatingCapacity = value;
    }

    /**
     * Gets the value of the driverSeatingCapacity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDriverSeatingCapacity() {
        return driverSeatingCapacity;
    }

    /**
     * Sets the value of the driverSeatingCapacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDriverSeatingCapacity(String value) {
        this.driverSeatingCapacity = value;
    }

    /**
     * Gets the value of the standingCapacity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStandingCapacity() {
        return standingCapacity;
    }

    /**
     * Sets the value of the standingCapacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStandingCapacity(String value) {
        this.standingCapacity = value;
    }

    /**
     * Gets the value of the color property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getColor() {
        return color;
    }

    /**
     * Sets the value of the color property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setColor(String value) {
        this.color = value;
    }

    /**
     * Gets the value of the vehicleIsNewOld property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVehicleIsNewOld() {
        return vehicleIsNewOld;
    }

    /**
     * Sets the value of the vehicleIsNewOld property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVehicleIsNewOld(String value) {
        this.vehicleIsNewOld = value;
    }

    /**
     * Gets the value of the manufactureMonthYear property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getManufactureMonthYear() {
        return manufactureMonthYear;
    }

    /**
     * Sets the value of the manufactureMonthYear property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setManufactureMonthYear(String value) {
        this.manufactureMonthYear = value;
    }

    /**
     * Gets the value of the horsePower property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHorsePower() {
        return horsePower;
    }

    /**
     * Sets the value of the horsePower property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHorsePower(String value) {
        this.horsePower = value;
    }

    /**
     * Gets the value of the unleadenWeight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUnleadenWeight() {
        return unleadenWeight;
    }

    /**
     * Sets the value of the unleadenWeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUnleadenWeight(String value) {
        this.unleadenWeight = value;
    }

    /**
     * Gets the value of the growssWeightCertificate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGrowssWeightCertificate() {
        return growssWeightCertificate;
    }

    /**
     * Sets the value of the growssWeightCertificate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGrowssWeightCertificate(String value) {
        this.growssWeightCertificate = value;
    }

    /**
     * Gets the value of the growssWeightRegistrationTime property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGrowssWeightRegistrationTime() {
        return growssWeightRegistrationTime;
    }

    /**
     * Sets the value of the growssWeightRegistrationTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGrowssWeightRegistrationTime(String value) {
        this.growssWeightRegistrationTime = value;
    }

    /**
     * Gets the value of the fronAxelTyreSizes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFronAxelTyreSizes() {
        return fronAxelTyreSizes;
    }

    /**
     * Sets the value of the fronAxelTyreSizes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFronAxelTyreSizes(String value) {
        this.fronAxelTyreSizes = value;
    }

    /**
     * Gets the value of the rearAxelTyreSizes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRearAxelTyreSizes() {
        return rearAxelTyreSizes;
    }

    /**
     * Sets the value of the rearAxelTyreSizes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRearAxelTyreSizes(String value) {
        this.rearAxelTyreSizes = value;
    }

    /**
     * Gets the value of the otherAxelTyreSizes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOtherAxelTyreSizes() {
        return otherAxelTyreSizes;
    }

    /**
     * Sets the value of the otherAxelTyreSizes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOtherAxelTyreSizes(String value) {
        this.otherAxelTyreSizes = value;
    }

    /**
     * Gets the value of the tandomAxelTyreSizes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTandomAxelTyreSizes() {
        return tandomAxelTyreSizes;
    }

    /**
     * Sets the value of the tandomAxelTyreSizes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTandomAxelTyreSizes(String value) {
        this.tandomAxelTyreSizes = value;
    }

    /**
     * Gets the value of the fronWeight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFronWeight() {
        return fronWeight;
    }

    /**
     * Sets the value of the fronWeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFronWeight(String value) {
        this.fronWeight = value;
    }

    /**
     * Gets the value of the rearWeight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRearWeight() {
        return rearWeight;
    }

    /**
     * Sets the value of the rearWeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRearWeight(String value) {
        this.rearWeight = value;
    }

    /**
     * Gets the value of the otherAxelWeight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOtherAxelWeight() {
        return otherAxelWeight;
    }

    /**
     * Sets the value of the otherAxelWeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOtherAxelWeight(String value) {
        this.otherAxelWeight = value;
    }

    /**
     * Gets the value of the tandomAxelWeight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTandomAxelWeight() {
        return tandomAxelWeight;
    }

    /**
     * Sets the value of the tandomAxelWeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTandomAxelWeight(String value) {
        this.tandomAxelWeight = value;
    }

    /**
     * Gets the value of the axelType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAxelType() {
        return axelType;
    }

    /**
     * Sets the value of the axelType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAxelType(String value) {
        this.axelType = value;
    }

    /**
     * Gets the value of the length property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLength() {
        return length;
    }

    /**
     * Sets the value of the length property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLength(String value) {
        this.length = value;
    }

    /**
     * Gets the value of the width property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWidth(String value) {
        this.width = value;
    }

    /**
     * Gets the value of the height property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHeight() {
        return height;
    }

    /**
     * Sets the value of the height property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHeight(String value) {
        this.height = value;
    }

    /**
     * Gets the value of the hangingCapacity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHangingCapacity() {
        return hangingCapacity;
    }

    /**
     * Sets the value of the hangingCapacity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHangingCapacity(String value) {
        this.hangingCapacity = value;
    }

    /**
     * Gets the value of the taxExemption property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaxExemption() {
        return taxExemption;
    }

    /**
     * Sets the value of the taxExemption property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaxExemption(String value) {
        this.taxExemption = value;
    }

    /**
     * Gets the value of the taxPaidOfficeCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaxPaidOfficeCode() {
        return taxPaidOfficeCode;
    }

    /**
     * Sets the value of the taxPaidOfficeCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaxPaidOfficeCode(String value) {
        this.taxPaidOfficeCode = value;
    }

    /**
     * Gets the value of the taxPaidStateCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaxPaidStateCode() {
        return taxPaidStateCode;
    }

    /**
     * Sets the value of the taxPaidStateCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaxPaidStateCode(String value) {
        this.taxPaidStateCode = value;
    }

    /**
     * Gets the value of the taxPaymentPeriod property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaxPaymentPeriod() {
        return taxPaymentPeriod;
    }

    /**
     * Sets the value of the taxPaymentPeriod property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaxPaymentPeriod(String value) {
        this.taxPaymentPeriod = value;
    }

    /**
     * Gets the value of the taxDemandAmount property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaxDemandAmount() {
        return taxDemandAmount;
    }

    /**
     * Sets the value of the taxDemandAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaxDemandAmount(String value) {
        this.taxDemandAmount = value;
    }

    /**
     * Gets the value of the taxPenaltyAmount property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaxPenaltyAmount() {
        return taxPenaltyAmount;
    }

    /**
     * Sets the value of the taxPenaltyAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaxPenaltyAmount(String value) {
        this.taxPenaltyAmount = value;
    }

    /**
     * Gets the value of the taxCollectedAmount property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaxCollectedAmount() {
        return taxCollectedAmount;
    }

    /**
     * Sets the value of the taxCollectedAmount property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaxCollectedAmount(String value) {
        this.taxCollectedAmount = value;
    }

    /**
     * Gets the value of the taxDemandDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaxDemandDate() {
        return taxDemandDate;
    }

    /**
     * Sets the value of the taxDemandDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaxDemandDate(String value) {
        this.taxDemandDate = value;
    }

    /**
     * Gets the value of the taxQuarterStartDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaxQuarterStartDate() {
        return taxQuarterStartDate;
    }

    /**
     * Sets the value of the taxQuarterStartDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaxQuarterStartDate(String value) {
        this.taxQuarterStartDate = value;
    }

    /**
     * Gets the value of the taxValidUpto property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTaxValidUpto() {
        return taxValidUpto;
    }

    /**
     * Sets the value of the taxValidUpto property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTaxValidUpto(String value) {
        this.taxValidUpto = value;
    }

    /**
     * Gets the value of the hypothecationType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getHypothecationType() {
        return hypothecationType;
    }

    /**
     * Sets the value of the hypothecationType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setHypothecationType(String value) {
        this.hypothecationType = value;
    }

    /**
     * Gets the value of the financerName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFinancerName() {
        return financerName;
    }

    /**
     * Sets the value of the financerName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFinancerName(String value) {
        this.financerName = value;
    }

    /**
     * Gets the value of the financeAggrementDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFinanceAggrementDate() {
        return financeAggrementDate;
    }

    /**
     * Sets the value of the financeAggrementDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFinanceAggrementDate(String value) {
        this.financeAggrementDate = value;
    }

    /**
     * Gets the value of the financerAddress1 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFinancerAddress1() {
        return financerAddress1;
    }

    /**
     * Sets the value of the financerAddress1 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFinancerAddress1(String value) {
        this.financerAddress1 = value;
    }

    /**
     * Gets the value of the financerAddress2 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFinancerAddress2() {
        return financerAddress2;
    }

    /**
     * Sets the value of the financerAddress2 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFinancerAddress2(String value) {
        this.financerAddress2 = value;
    }

    /**
     * Gets the value of the financerAddress3 property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFinancerAddress3() {
        return financerAddress3;
    }

    /**
     * Sets the value of the financerAddress3 property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFinancerAddress3(String value) {
        this.financerAddress3 = value;
    }

    /**
     * Gets the value of the financerCity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFinancerCity() {
        return financerCity;
    }

    /**
     * Sets the value of the financerCity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFinancerCity(String value) {
        this.financerCity = value;
    }

    /**
     * Gets the value of the financerState property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFinancerState() {
        return financerState;
    }

    /**
     * Sets the value of the financerState property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFinancerState(String value) {
        this.financerState = value;
    }

    /**
     * Gets the value of the financerPin property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFinancerPin() {
        return financerPin;
    }

    /**
     * Sets the value of the financerPin property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFinancerPin(String value) {
        this.financerPin = value;
    }

    /**
     * Gets the value of the financerFaxNo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFinancerFaxNo() {
        return financerFaxNo;
    }

    /**
     * Sets the value of the financerFaxNo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFinancerFaxNo(String value) {
        this.financerFaxNo = value;
    }

    /**
     * Gets the value of the financerPhoneNO property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFinancerPhoneNO() {
        return financerPhoneNO;
    }

    /**
     * Sets the value of the financerPhoneNO property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFinancerPhoneNO(String value) {
        this.financerPhoneNO = value;
    }

    /**
     * Gets the value of the financerEmailId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFinancerEmailId() {
        return financerEmailId;
    }

    /**
     * Sets the value of the financerEmailId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFinancerEmailId(String value) {
        this.financerEmailId = value;
    }

    /**
     * Gets the value of the nocOfficeCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNocOfficeCode() {
        return nocOfficeCode;
    }

    /**
     * Sets the value of the nocOfficeCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNocOfficeCode(String value) {
        this.nocOfficeCode = value;
    }

    /**
     * Gets the value of the applicationno property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAPPLICATIONNO() {
        return applicationno;
    }

    /**
     * Sets the value of the applicationno property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAPPLICATIONNO(String value) {
        this.applicationno = value;
    }

    /**
     * Gets the value of the fcNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFcNumber() {
        return fcNumber;
    }

    /**
     * Sets the value of the fcNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFcNumber(String value) {
        this.fcNumber = value;
    }

    /**
     * Gets the value of the fcIssuedBy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFcIssuedBy() {
        return fcIssuedBy;
    }

    /**
     * Sets the value of the fcIssuedBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFcIssuedBy(String value) {
        this.fcIssuedBy = value;
    }

    /**
     * Gets the value of the fcIssuedDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFcIssuedDate() {
        return fcIssuedDate;
    }

    /**
     * Sets the value of the fcIssuedDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFcIssuedDate(String value) {
        this.fcIssuedDate = value;
    }

    /**
     * Gets the value of the fcValidFromDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFcValidFromDate() {
        return fcValidFromDate;
    }

    /**
     * Sets the value of the fcValidFromDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFcValidFromDate(String value) {
        this.fcValidFromDate = value;
    }

    /**
     * Gets the value of the fcValidToDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFcValidToDate() {
        return fcValidToDate;
    }

    /**
     * Sets the value of the fcValidToDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFcValidToDate(String value) {
        this.fcValidToDate = value;
    }

    /**
     * Gets the value of the fcApprovedBy property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFcApprovedBy() {
        return fcApprovedBy;
    }

    /**
     * Sets the value of the fcApprovedBy property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFcApprovedBy(String value) {
        this.fcApprovedBy = value;
    }

    /**
     * Gets the value of the fcChallanNo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFcChallanNo() {
        return fcChallanNo;
    }

    /**
     * Sets the value of the fcChallanNo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFcChallanNo(String value) {
        this.fcChallanNo = value;
    }

    /**
     * Gets the value of the permitNo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPermitNo() {
        return permitNo;
    }

    /**
     * Sets the value of the permitNo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPermitNo(String value) {
        this.permitNo = value;
    }

    /**
     * Gets the value of the permitClass property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPermitClass() {
        return permitClass;
    }

    /**
     * Sets the value of the permitClass property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPermitClass(String value) {
        this.permitClass = value;
    }

    /**
     * Gets the value of the permitType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPermitType() {
        return permitType;
    }

    /**
     * Sets the value of the permitType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPermitType(String value) {
        this.permitType = value;
    }

    /**
     * Gets the value of the permitIssueDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPermitIssueDate() {
        return permitIssueDate;
    }

    /**
     * Sets the value of the permitIssueDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPermitIssueDate(String value) {
        this.permitIssueDate = value;
    }

    /**
     * Gets the value of the permitValidFromDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPermitValidFromDate() {
        return permitValidFromDate;
    }

    /**
     * Sets the value of the permitValidFromDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPermitValidFromDate(String value) {
        this.permitValidFromDate = value;
    }

    /**
     * Gets the value of the permitValidToDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPermitValidToDate() {
        return permitValidToDate;
    }

    /**
     * Sets the value of the permitValidToDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPermitValidToDate(String value) {
        this.permitValidToDate = value;
    }

    /**
     * Gets the value of the authorizationNo property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthorizationNo() {
        return authorizationNo;
    }

    /**
     * Sets the value of the authorizationNo property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthorizationNo(String value) {
        this.authorizationNo = value;
    }

    /**
     * Gets the value of the authFromDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthFromDate() {
        return authFromDate;
    }

    /**
     * Sets the value of the authFromDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthFromDate(String value) {
        this.authFromDate = value;
    }

    /**
     * Gets the value of the authToDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAuthToDate() {
        return authToDate;
    }

    /**
     * Sets the value of the authToDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAuthToDate(String value) {
        this.authToDate = value;
    }

    /**
     * Gets the value of the routeDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRouteDescription() {
        return routeDescription;
    }

    /**
     * Sets the value of the routeDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRouteDescription(String value) {
        this.routeDescription = value;
    }

    /**
     * Gets the value of the goodsDescription property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getGoodsDescription() {
        return goodsDescription;
    }

    /**
     * Sets the value of the goodsDescription property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGoodsDescription(String value) {
        this.goodsDescription = value;
    }

    /**
     * Gets the value of the permitLadenWeight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPermitLadenWeight() {
        return permitLadenWeight;
    }

    /**
     * Sets the value of the permitLadenWeight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPermitLadenWeight(String value) {
        this.permitLadenWeight = value;
    }

    /**
     * Gets the value of the permitRouteType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPermitRouteType() {
        return permitRouteType;
    }

    /**
     * Sets the value of the permitRouteType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPermitRouteType(String value) {
        this.permitRouteType = value;
    }

    /**
     * Gets the value of the oneDistrictPermit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getOneDistrictPermit() {
        return oneDistrictPermit;
    }

    /**
     * Sets the value of the oneDistrictPermit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setOneDistrictPermit(String value) {
        this.oneDistrictPermit = value;
    }

    /**
     * Gets the value of the twoDistrictPermit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTwoDistrictPermit() {
        return twoDistrictPermit;
    }

    /**
     * Sets the value of the twoDistrictPermit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTwoDistrictPermit(String value) {
        this.twoDistrictPermit = value;
    }

    /**
     * Gets the value of the transactionDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTransactionDate() {
        return transactionDate;
    }

    /**
     * Sets the value of the transactionDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTransactionDate(String value) {
        this.transactionDate = value;
    }

}
