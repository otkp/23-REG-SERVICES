package org.epragati.serviceImpl;

import java.net.ConnectException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.epragati.constants.OwnerTypeEnum;
import org.epragati.master.vo.CfstSyncRegstrationVO;
import org.epragati.rta.vo.TrailerChassisDetailsVO;
import org.epragati.service.CfstSyncService;
import org.epragati.tax.vo.TaxTypeEnum;
import org.epragati.util.CfstStatusTypes;
import org.epragati.util.DateConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.tempuri.OtsiLiveDataSyncToCms1;
import org.tempuri.OtsiLiveDataSyncToCmsSoap;

/**
 * 
 * @author krishnarjun.pampana
 *
 */
@Service
public class CfstServiceImpl implements CfstSyncService {

	private static final Logger logger = LoggerFactory.getLogger(CfstServiceImpl.class);
	
	@Override
	public Map<String, String> saveRegDetails(List<CfstSyncRegstrationVO> registrationDetailsVOList) {

		Map<String, String> savedList = new HashMap<>();
		String vehicleNumber = StringUtils.EMPTY;
		for (CfstSyncRegstrationVO registrationDetailsVO : registrationDetailsVOList) {
			try {
				System.setProperty(LogFactory.FACTORY_PROPERTY, LogFactory.FACTORY_DEFAULT);
				logger.info("Vehicle Pr Number posted to cfst:{}",registrationDetailsVO.getPrNo());
				 vehicleNumber = replaceDefaults(registrationDetailsVO.getPrNo());
				// RegistrationValidity is not available
				String vehicleRegisteredDate = StringUtils.EMPTY;
				String vehicleRegistrationValidUpto = StringUtils.EMPTY;
				String transactionDate = StringUtils.EMPTY;
				if (registrationDetailsVO.getRegistrationValidity() != null) {
					vehicleRegisteredDate = replaceDates(
							registrationDetailsVO.getRegistrationValidity().getPrGeneratedDate());
					transactionDate = replaceDates(
							registrationDetailsVO.getRegistrationValidity().getPrGeneratedDate());
					 vehicleRegistrationValidUpto =
					 replaceDateTime(registrationDetailsVO.getRegistrationValidity().getRegistrationValidity());
				}

				String vehicleIssuePlace = StringUtils.EMPTY;
				if (registrationDetailsVO.getOfficeDetails() != null) {
					vehicleIssuePlace = replaceDefaults(registrationDetailsVO.getOfficeDetails().getOfficeCode());
				}
				String vehicleStatus = replaceDefaults(CfstStatusTypes.A.getCode());
				String remarks = replaceDefaults("");
				String ownerShip = replaceDefaults(registrationDetailsVO.getOwnerType().toString());
				String ownershipType = ownerShip;

				// ApplicantDetails not Available
				String ownerPanNo = StringUtils.EMPTY;
				String ownerFirstName = StringUtils.EMPTY;
				String ownerMiddileName = StringUtils.EMPTY;
				String ownerLastName = StringUtils.EMPTY;
				String ownerAge = StringUtils.EMPTY;
				String ownerFatherName = StringUtils.EMPTY;
				String ownerVoterId = StringUtils.EMPTY;
				String ownerMobileNo = StringUtils.EMPTY;
				String ownerAddress1 = StringUtils.EMPTY;
				String ownerAddress2 = StringUtils.EMPTY;
				String ownerAddress3 = StringUtils.EMPTY;
				String ownerCity = StringUtils.EMPTY;
				String ownerState = StringUtils.EMPTY;
				String ownerPin = StringUtils.EMPTY;
				String taxPaidStateCode = StringUtils.EMPTY;

				if (registrationDetailsVO.getApplicantDetails() != null) {
					ownerFirstName = replaceDefaults(registrationDetailsVO.getApplicantDetails().getFirstName());
					ownerMiddileName = replaceDefaults(registrationDetailsVO.getApplicantDetails().getMiddleName());
					ownerLastName = replaceDefaults(registrationDetailsVO.getApplicantDetails().getLastName());
					ownerAge = replaceDefaults("");
					ownerFatherName = replaceDefaults(registrationDetailsVO.getApplicantDetails().getFatherName());
					if (null != registrationDetailsVO.getPanDetails()) {
						ownerPanNo = replaceDefaults(registrationDetailsVO.getPanDetails().getPanNo());
					}
					ownerVoterId = replaceDefaults("");
					if (registrationDetailsVO.getApplicantDetails().getContact() != null) {
						ownerMobileNo = replaceDefaults(
								registrationDetailsVO.getApplicantDetails().getContact().getMobile());
					}
					if (registrationDetailsVO.getApplicantDetails().getPresentAddress() != null) {
						ownerAddress1 = replaceDefaults(registrationDetailsVO.getApplicantDetails().getPresentAddress()
								.getDoorNo() + ","
								+ registrationDetailsVO.getApplicantDetails().getPresentAddress().getStreetName());
						if (registrationDetailsVO.getApplicantDetails().getPresentAddress().getMandal() != null) {
							ownerAddress2 = replaceDefaults(registrationDetailsVO.getApplicantDetails()
									.getPresentAddress().getMandal().getMandalName());
						}
						if (registrationDetailsVO.getApplicantDetails().getPresentAddress().getDistrict() != null) {
							ownerAddress3 = replaceDefaults(registrationDetailsVO.getApplicantDetails()
									.getPresentAddress().getDistrict().getDistrictName());
						}
						ownerCity = replaceDefaults(
								registrationDetailsVO.getApplicantDetails().getPresentAddress().getTownOrCity());
						ownerState = replaceDefaults(registrationDetailsVO.getApplicantDetails().getPresentAddress()
								.getState().getStateName());
						ownerPin = replaceDefaults(registrationDetailsVO.getApplicantDetails().getPresentAddress()
								.getPostOffice().getPostOfficeCode().toString());
						taxPaidStateCode = replaceDefaults(registrationDetailsVO.getApplicantDetails()
								.getPresentAddress().getState().getStateId());
					}

				}

				String vehicleOldNo = replaceDefaults(registrationDetailsVO.getTrNo());
				String previousRegisteredOfficeName = replaceDefaults();
				String previousRegisteredOfficeState = replaceDefaults();
				String governmentVehicle = CfstStatusTypes.N.getCode();
				if (ownerShip.equalsIgnoreCase(OwnerTypeEnum.Government.toString())
						|| ownerShip.equalsIgnoreCase(OwnerTypeEnum.POLICE.toString())
						|| ownerShip.equalsIgnoreCase(OwnerTypeEnum.Stu.toString())) {
					governmentVehicle = CfstStatusTypes.Y.getCode();
					if (registrationDetailsVO.getApplicantDetails().getEntityName() != null) {
						ownerFirstName = registrationDetailsVO.getApplicantDetails().getEntityName();
						ownerFatherName = replaceDefaults(
								registrationDetailsVO.getApplicantDetails().getRepresentativeName());
					}
				}
				if (ownerShip.equalsIgnoreCase(OwnerTypeEnum.Company.toString())
						|| ownerShip.equalsIgnoreCase(OwnerTypeEnum.Organization.toString())) {
					if (registrationDetailsVO.getApplicantDetails().getEntityName() != null) {
						ownerFirstName = registrationDetailsVO.getApplicantDetails().getEntityName();
						ownerFatherName = replaceDefaults(
								registrationDetailsVO.getApplicantDetails().getRepresentativeName());
					}
				}

				String reservedSpecialNo = CfstStatusTypes.N.getCode();
				if (registrationDetailsVO.getSpecialNumberRequired()) {
					reservedSpecialNo = CfstStatusTypes.Y.getCode();
				}
				String vehicleIsRTC = replaceDefaults(CfstStatusTypes.N.getCode());
				if (ownerShip.equalsIgnoreCase(OwnerTypeEnum.Stu.toString())) {
					vehicleIsRTC = replaceDefaults(CfstStatusTypes.Y.getCode());
				}

				// Insurance Details not available
				String insuranceNo = StringUtils.EMPTY;
				String insuranceCompanyName = StringUtils.EMPTY;
				String insuranceValidFrom = StringUtils.EMPTY;
				String insuranceValidTo = StringUtils.EMPTY;

				if (registrationDetailsVO.getInsuranceDetails() != null) {
					insuranceNo = replaceDefaults(registrationDetailsVO.getInsuranceDetails().getPolicyNumber());
					insuranceCompanyName = replaceDefaults(registrationDetailsVO.getInsuranceDetails().getCompany());
					insuranceValidFrom = replaceDates(registrationDetailsVO.getInsuranceDetails().getValidFrom());
					insuranceValidTo = replaceDates(registrationDetailsVO.getInsuranceDetails().getValidTill());
				}

				// Dealer details not available
				String dealerName = StringUtils.EMPTY;
				String dealerAddress1 = StringUtils.EMPTY;
				String dealerAddress2 = StringUtils.EMPTY;
				String dealerAddress3 = StringUtils.EMPTY;
				String dealerCity = StringUtils.EMPTY;
				String dealerState = StringUtils.EMPTY;
				String dealerAddressPinCode = StringUtils.EMPTY;

				if (registrationDetailsVO.getDealerVO() != null) {
					dealerName = replaceDefaults(CfstStatusTypes.Ina.getCode());
					if (registrationDetailsVO.getDealerVO().getMandal() != null) {
						dealerAddress1 = replaceDefaults(
								registrationDetailsVO.getDealerVO().getMandal().getMandalName());
					}
					if (registrationDetailsVO.getDealerVO().getDistrict() != null) {
						dealerAddress2 = replaceDefaults(
								registrationDetailsVO.getDealerVO().getDistrict().getDistrictName());
					}
					dealerAddress3 = replaceDefaults();
					dealerCity = replaceDefaults(registrationDetailsVO.getDealerVO().getCity());

					if (registrationDetailsVO.getDealerVO().getState() != null) {
						dealerState = replaceDefaults(registrationDetailsVO.getDealerVO().getState().getStateName());
					}
					if (registrationDetailsVO.getDealerVO().getPincode() != null) {
						dealerAddressPinCode = convertIntegerToString(
								registrationDetailsVO.getDealerVO().getPincode().getPostOfficeCode());
					}

				}

				// VahanDetails not Available
				String makerName = StringUtils.EMPTY;
				String makerClass = StringUtils.EMPTY;
				String chassisNo = StringUtils.EMPTY;
				String engineNo = StringUtils.EMPTY;
				String vehicleType = StringUtils.EMPTY;
				String bodyType = StringUtils.EMPTY;
				String wheelBase = StringUtils.EMPTY;
				String fuel = StringUtils.EMPTY;
				String vehicleCC = StringUtils.EMPTY;
				String cylenders = StringUtils.EMPTY;
				String seatingCapacity = StringUtils.EMPTY;
				String driverSeatingCapacity = StringUtils.EMPTY;
				String standingCapacity = StringUtils.EMPTY;
				String color = StringUtils.EMPTY;
				String vehicleIsNewOld = StringUtils.EMPTY;
				String manufactureMonthYear = StringUtils.EMPTY;
				String horsePower = StringUtils.EMPTY;
				String unleadenWeight = StringUtils.EMPTY;
				String growssWeightCertificate = StringUtils.EMPTY;
				String growssWeightRegistrationTime = StringUtils.EMPTY;
				String fronAxelTyreSizes = StringUtils.EMPTY;
				String rearAxelTyreSizes = StringUtils.EMPTY;
				String otherAxelTyreSizes = StringUtils.EMPTY;
				String tandomAxelTyreSizes = StringUtils.EMPTY;
				String fronWeight = StringUtils.EMPTY;
				String rearWeight = StringUtils.EMPTY;
				String otherAxelWeight = StringUtils.EMPTY;
				String tandomAxelWeight = StringUtils.EMPTY;
				String axelType = StringUtils.EMPTY;
				String length = StringUtils.EMPTY;
				String width = StringUtils.EMPTY;
				String height = StringUtils.EMPTY;
				String hangingCapacity = StringUtils.EMPTY;

				String vehicleClass = replaceDefaults(registrationDetailsVO.getClassOfVehicle());

				if (registrationDetailsVO.getVahanDetails() != null) {
					makerName = replaceDefaults(registrationDetailsVO.getVahanDetails().getMakersDesc());
					makerClass = replaceDefaults(registrationDetailsVO.getVahanDetails().getMakersModel());
					chassisNo = replaceDefaults(registrationDetailsVO.getVahanDetails().getChassisNumber());
					engineNo = replaceDefaults(registrationDetailsVO.getVahanDetails().getEngineNumber());
					vehicleType = replaceDefaults(registrationDetailsVO.getVehicleType());
					bodyType = replaceDefaults(registrationDetailsVO.getVahanDetails().getBodyTypeDesc());
					wheelBase = convertIntegerToString(registrationDetailsVO.getVahanDetails().getWheelbase());
					fuel = replaceDefaults(registrationDetailsVO.getVahanDetails().getFuelDesc());
					vehicleCC = replaceDefaults(registrationDetailsVO.getVahanDetails().getCubicCapacity());
					cylenders = replaceDefaults(registrationDetailsVO.getVahanDetails().getNoCyl());
					seatingCapacity = replaceDefaults(registrationDetailsVO.getVahanDetails().getSeatingCapacity());
					driverSeatingCapacity = replaceDefaults();
					standingCapacity = replaceDefaults(registrationDetailsVO.getVahanDetails().getStandCapacity());
					color = replaceDefaults(registrationDetailsVO.getVahanDetails().getColor());
					vehicleIsNewOld = replaceDefaults();
					manufactureMonthYear = replaceDefaults(
							registrationDetailsVO.getVahanDetails().getManufacturedMonthYear());
					horsePower = convertDoubleToString(registrationDetailsVO.getVahanDetails().getEnginePower());
					unleadenWeight = convertIntegerToString(registrationDetailsVO.getVahanDetails().getUnladenWeight());
					if(CollectionUtils.isNotEmpty(registrationDetailsVO.getVahanDetails().getTrailerChassisDetailsVO())){
						TrailerChassisDetailsVO maxGtw = Collections.max(registrationDetailsVO.getVahanDetails().getTrailerChassisDetailsVO(), new TrailerVO());
						registrationDetailsVO.getVahanDetails().setGvw(maxGtw.getGtw()+registrationDetailsVO.getVahanDetails().getGvw());
					}
					growssWeightCertificate = convertIntegerToString(registrationDetailsVO.getVahanDetails().getGvw());// 1
					growssWeightRegistrationTime = convertIntegerToString(
							registrationDetailsVO.getVahanDetails().getGvw());
					/*-1*/ fronAxelTyreSizes = replaceDefaults(
							registrationDetailsVO.getVahanDetails().getFrontAxleDesc());
					rearAxelTyreSizes = replaceDefaults(registrationDetailsVO.getVahanDetails().getRearAxleDesc());
					otherAxelTyreSizes = replaceDefaults(registrationDetailsVO.getVahanDetails().getO1AxleDesc());
					tandomAxelTyreSizes = replaceDefaults(registrationDetailsVO.getVahanDetails().getTandemAxelDescp());
					fronWeight = convertIntegerToString(registrationDetailsVO.getVahanDetails().getFrontAxleWeight());
					rearWeight = convertIntegerToString(registrationDetailsVO.getVahanDetails().getRearAxleWeight());
					otherAxelWeight = convertIntegerToString(registrationDetailsVO.getVahanDetails().getO1AxleWeight());
					tandomAxelWeight = convertIntegerToString(
							registrationDetailsVO.getVahanDetails().getTandemAxelWeight());
					axelType = replaceDefaults();
					length = convertDoubleToString(registrationDetailsVO.getVahanDetails().getLength());
					width = convertDoubleToString(registrationDetailsVO.getVahanDetails().getWidth());
					height = convertDoubleToString(registrationDetailsVO.getVahanDetails().getHeight());
					hangingCapacity = replaceDefaults(); // 0

				}

				String taxPaidOfficeCode = StringUtils.EMPTY;
				String nocOfficeCode = StringUtils.EMPTY;
				if (registrationDetailsVO.getOfficeDetails() != null) {
					taxPaidOfficeCode = replaceDefaults(registrationDetailsVO.getOfficeDetails().getOfficeCode());
					taxPaidStateCode = replaceDefaults(registrationDetailsVO.getOfficeDetails().getOfficeCode());
					nocOfficeCode = replaceDefaults(registrationDetailsVO.getOfficeDetails().getOfficeCode());

				}
				String taxExemption = replaceDefaults(CfstStatusTypes.N.getCode());
				//String taxPaymentPeriod = StringUtils.EMPTY;
				String taxDemandAmount = StringUtils.EMPTY;
				String taxPenaltyAmount = StringUtils.EMPTY;
				String taxCollectedAmount = StringUtils.EMPTY;
				String taxDemandDate = replaceDefaults();
				String taxQuarterStartDate = replaceDefaults();
				String taxValidUpto = StringUtils.EMPTY;
				
			

			    String taxPaymentPeriod = replaceDefaults(registrationDetailsVO.getTaxPaymentPeriod());//
				if (taxPaymentPeriod.equalsIgnoreCase(TaxTypeEnum.LifeTax.getDesc())) {
					taxPaymentPeriod = "LTT";
				}
				if (taxPaymentPeriod.equalsIgnoreCase(TaxTypeEnum.QuarterlyTax.getDesc())) {
					taxPaymentPeriod = "QLY";
				}
				if (taxPaymentPeriod.equalsIgnoreCase(TaxTypeEnum.HalfyearlyTax.getDesc())) {
					taxPaymentPeriod = "HLY";
				}
				if (taxPaymentPeriod.equalsIgnoreCase(TaxTypeEnum.YearlyTax.getDesc())) {
					taxPaymentPeriod = "ANN";
				}
		
				
		
				if (!taxPaymentPeriod.isEmpty()) {
					taxDemandAmount = convertDoubleToString(registrationDetailsVO.getTaxDemandAmount());
					taxPenaltyAmount = replaceDefaults("0.0");
					if(registrationDetailsVO.getTaxPenaltyAmount()!=null){
						taxPenaltyAmount = replaceDefaults(registrationDetailsVO.getTaxPenaltyAmount().toString());
					}
					taxCollectedAmount = convertDoubleToString(registrationDetailsVO.getTaxCollectedAmount());
					if (registrationDetailsVO.getTaxQuarterStartDate() != null) {
					     taxDemandDate=replaceDates(registrationDetailsVO.getTaxQuarterStartDate());
						 taxQuarterStartDate=replaceDates(registrationDetailsVO.getTaxQuarterStartDate());
					}

					taxValidUpto = replaceDates(registrationDetailsVO.getTaxValidUpto());
				}
				
				
				
				// Financier Details not Available
				String hypothecationType = StringUtils.EMPTY;
				String financerName = StringUtils.EMPTY;
				String financeAggrementDate = StringUtils.EMPTY;
				String financerAddress1 = StringUtils.EMPTY;
				String financerAddress2 = StringUtils.EMPTY;
				String financerAddress3 = StringUtils.EMPTY;
				String financerCity = StringUtils.EMPTY;
				String financerState = StringUtils.EMPTY;
				String financerPin = StringUtils.EMPTY;
				String financerFaxNo = StringUtils.EMPTY;
				String financerPhoneNO = StringUtils.EMPTY;
				String financerEmailId = StringUtils.EMPTY;

				if (registrationDetailsVO.getIsFinancier()) {
					if (registrationDetailsVO.getFinanceDetails().getMasterFinanceTypeVO() != null) {
						hypothecationType = replaceDefaults(
								registrationDetailsVO.getFinanceDetails().getMasterFinanceTypeVO().getFinanceType());
					}
					if (registrationDetailsVO.getFinancierVO() != null) {
						financerName = replaceDefaults(registrationDetailsVO.getFinancierVO().getFirstName());
						financeAggrementDate = replaceDates(
								registrationDetailsVO.getFinanceDetails().getAgreementDate());
						if (registrationDetailsVO.getFinancierVO().getMandal() != null) {
							financerAddress1 = replaceDefaults(
									registrationDetailsVO.getFinancierVO().getMandal().getMandalName());
						}
						if (registrationDetailsVO.getFinancierVO().getDistrict() != null) {
							financerAddress2 = replaceDefaults(
									registrationDetailsVO.getFinancierVO().getDistrict().getDistrictName());
						}

						financerAddress3 = replaceDefaults();
						financerCity = replaceDefaults(registrationDetailsVO.getFinancierVO().getCity());
						if (registrationDetailsVO.getFinancierVO().getState() != null) {
							financerState = replaceDefaults(
									registrationDetailsVO.getFinancierVO().getState().getStateName());
						}
						if (registrationDetailsVO.getFinancierVO().getPincode() != null) {
							financerPin = convertIntegerToString(
									registrationDetailsVO.getFinancierVO().getPincode().getPostOfficeCode());
						}
						financerFaxNo = replaceDefaults();
						financerPhoneNO = replaceDefaults(registrationDetailsVO.getFinancierVO().getMobile());
						financerEmailId = replaceDefaults(registrationDetailsVO.getFinancierVO().getEmail());
					}
				}
				String applicationno = replaceDefaults(registrationDetailsVO.getApplicationNo());

				// Fc details not available
				String fcNumber = StringUtils.EMPTY;
				String fcIssuedBy = StringUtils.EMPTY;
				String fcIssuedDate = StringUtils.EMPTY;
				String fcValidFromDate = StringUtils.EMPTY;
				String fcValidToDate = StringUtils.EMPTY;
				String fcApprovedBy = StringUtils.EMPTY;
				String fcChallanNo = StringUtils.EMPTY;

				if (registrationDetailsVO.getFcDetailsVO() != null) {
					fcNumber = replaceDefaults(registrationDetailsVO.getFcDetailsVO().getFcNumber());
					fcIssuedBy = replaceDefaults(registrationDetailsVO.getFcDetailsVO().getOfficeCode());
					 fcIssuedDate=replaceDateTime(registrationDetailsVO.getFcDetailsVO().getFcIssuedDate());
					 fcValidFromDate=replaceDateTime(registrationDetailsVO.getFcDetailsVO().getFcIssuedDate());
					fcValidToDate = replaceDates(registrationDetailsVO.getFcDetailsVO().getFcValidUpto());
					fcApprovedBy = replaceDefaults(registrationDetailsVO.getFcDetailsVO().getInspectedMviName());
					fcChallanNo = replaceDefaults(registrationDetailsVO.getFcDetailsVO().getApplicationNo());
				}

				String permitNo = replaceDefaults();
				String permitClass = replaceDefaults();
				String permitType = replaceDefaults();
				String permitIssueDate = replaceDefaults();
				String permitValidFromDate = replaceDefaults();
				String permitValidToDate = replaceDefaults();
				String authorizationNo = replaceDefaults();
				String authFromDate = replaceDefaults();
				String authToDate = replaceDefaults();
				String routeDescription = replaceDefaults();
				String goodsDescription = replaceDefaults();
				String permitLadenWeight = replaceDefaults();
				String permitRouteType = replaceDefaults();
				String oneDistrictPermit = replaceDefaults();
				String twoDistrictPermit = replaceDefaults();
				if(null!=registrationDetailsVO.getPermitVO()){
				 permitNo = replaceDefaults(registrationDetailsVO.getPermitVO().getPermitNo());
				 if(registrationDetailsVO.getPermitVO().getPermitClass()!=null){
					 permitClass = replaceDefaults(registrationDetailsVO.getPermitVO().getPermitClass().getDescription());
				 }
				 if(registrationDetailsVO.getPermitVO().getPermitType()!=null){
					 
					 permitType = replaceDefaults(registrationDetailsVO.getPermitVO().getPermitType().getDescription());
				 }
				 if(registrationDetailsVO.getPermitVO().getPermitValidityDetailsVO()!=null){
					 permitIssueDate = replaceDefaults();
					 permitValidFromDate = replaceDates(registrationDetailsVO.getPermitVO().getPermitValidityDetailsVO().getPermitValidFrom());
					 permitValidToDate = replaceDates(registrationDetailsVO.getPermitVO().getPermitValidityDetailsVO().getPermitValidTo());
					 authFromDate = replaceDates(registrationDetailsVO.getPermitVO().getPermitValidityDetailsVO().getPermitAuthorizationValidFrom());
					 authToDate = replaceDates(registrationDetailsVO.getPermitVO().getPermitValidityDetailsVO().getPermitAuthorizationValidTo());
				 }
				
				 authorizationNo = replaceDefaults(registrationDetailsVO.getPermitVO().getPermitAuthorizationNo());
				if(registrationDetailsVO.getPermitVO().getGoodDetails()!=null){
					goodsDescription = replaceDefaults(registrationDetailsVO.getPermitVO().getGoodDetails().getPermitAllowedgoods());
				}
				if(registrationDetailsVO.getPermitVO().getRouteDetailsVO().getPermitRouteDetails()!=null){
					routeDescription = replaceDefaults(registrationDetailsVO.getPermitVO().getRouteDetailsVO().getPermitRouteDetails().getDescription());
					if(registrationDetailsVO.getPermitVO().getRouteDetailsVO().getRouteType()!=null){
						permitRouteType = replaceDefaults(registrationDetailsVO.getPermitVO().getRouteDetailsVO().getRouteType().getRouteType());
						if(permitRouteType.equals("One District")){
							 oneDistrictPermit = replaceDefaults(permitRouteType);
						}
						if(permitRouteType.equals("TWO District")){
							twoDistrictPermit = replaceDefaults(permitRouteType);
						}
					}
					
				}
				if(registrationDetailsVO.getVahanDetails().getGvw()!=null && registrationDetailsVO.getVahanDetails().getUnladenWeight()!=null){
					Integer diffPermitLangenWeight =(registrationDetailsVO.getVahanDetails().getGvw())-(registrationDetailsVO.getVahanDetails().getUnladenWeight());
					permitLadenWeight = replaceDefaults(diffPermitLangenWeight.toString().replace("-", StringUtils.EMPTY));
				}
				
				
			}

				OtsiLiveDataSyncToCms1 otsiLiveDataSyncToCms = new OtsiLiveDataSyncToCms1();
				OtsiLiveDataSyncToCmsSoap otsiLiveDataSyncToCmsSoap = otsiLiveDataSyncToCms
						.getOtsiLiveDataSyncToCmsSoap();
				logger.info("Data Posting Started....");
				String output = otsiLiveDataSyncToCmsSoap.otsiLiveDataSyncToCMS(vehicleNumber, vehicleRegisteredDate,
						vehicleRegistrationValidUpto, vehicleIssuePlace, vehicleStatus, remarks, ownershipType,
						ownerFirstName, ownerMiddileName, ownerLastName, ownerAge, ownerFatherName, ownerPanNo,
						ownerVoterId, ownerMobileNo, ownerAddress1, ownerAddress2, ownerAddress3, ownerCity, ownerState,
						ownerPin, vehicleOldNo, previousRegisteredOfficeName, previousRegisteredOfficeState,
						governmentVehicle, reservedSpecialNo, vehicleIsRTC, insuranceNo, insuranceCompanyName,
						insuranceValidFrom, insuranceValidTo, makerName, makerClass, dealerName, dealerAddress1,
						dealerAddress2, dealerAddress3, dealerCity, dealerState, dealerAddressPinCode, vehicleClass,
						chassisNo, engineNo, vehicleType, bodyType, wheelBase, fuel, vehicleCC, cylenders,
						seatingCapacity, driverSeatingCapacity, standingCapacity, color, vehicleIsNewOld,
						manufactureMonthYear, horsePower, unleadenWeight, growssWeightCertificate,
						growssWeightRegistrationTime, fronAxelTyreSizes, rearAxelTyreSizes, otherAxelTyreSizes,
						tandomAxelTyreSizes, fronWeight, rearWeight, otherAxelWeight, tandomAxelWeight, axelType,
						length, width, height, hangingCapacity, taxExemption, taxPaidOfficeCode, taxPaidStateCode,
						taxPaymentPeriod, taxDemandAmount, taxPenaltyAmount, taxCollectedAmount, taxDemandDate,
						taxQuarterStartDate, taxValidUpto, hypothecationType, financerName, financeAggrementDate,
						financerAddress1, financerAddress2, financerAddress3, financerCity, financerState, financerPin,
						financerFaxNo, financerPhoneNO, financerEmailId, nocOfficeCode, applicationno, fcNumber,
						fcIssuedBy, fcIssuedDate, fcValidFromDate, fcValidToDate, fcApprovedBy, fcChallanNo, permitNo,
						permitClass, permitType, permitIssueDate, permitValidFromDate, permitValidToDate,
						authorizationNo, authFromDate, authToDate, routeDescription, goodsDescription,
						permitLadenWeight, permitRouteType, oneDistrictPermit, twoDistrictPermit, transactionDate);
				logger.info("Data Posting ended....result from cfst:{}" , output);
				savedList.put(vehicleNumber, output);
			}
				catch (Exception e) {

					if(e.getCause() instanceof ConnectException){
						logger.error("Exception Occured while posting the data to CSFT : {} ", e.getMessage());
						savedList.put(vehicleNumber,e.getMessage());
						//registrationDetailsVOList.clear();
					}else{
						logger.info("Exception Raised From Cfst Sync : {} ", e.getMessage());
						savedList.put(vehicleNumber,e.getMessage());
						//registrationDetailsVOList.clear();
					}
					// It means, from CFST very oftenly receiving connection time, respective record already saved into CFST
					//logger.info("saved prNo's : {}", savedList.size());
					//return savedList;
				}
			}
		
			registrationDetailsVOList.clear();
			logger.info("saved prNo's : {}", savedList.size());
			return savedList;

		}
		
		private String replaceDefaults() {
			return StringUtils.EMPTY;
		}

	private String replaceDefaults(String input) {

		if (null == input || StringUtils.isBlank(input)) {
			return StringUtils.EMPTY;
		}
		return input;
	}

	private String replaceDates(LocalDate input) {

		if (input != null) {
			return DateConverters.convertCfstSyncLocalDateFormat(input);
		}
		return StringUtils.EMPTY;
	}

	private String replaceDateTime(LocalDateTime input) {

		if (input != null) {
			return DateConverters.convertCfstSyncLocalDateTimeFormat(input);
		}
		return StringUtils.EMPTY;
	}

	private String convertDoubleToString(Double value) {
		if (null != value) {
			return value.toString();
		}
		return StringUtils.EMPTY;

	}

	private String convertIntegerToString(Integer value) {
		if (null != value) {
			return value.toString();
		}
		return StringUtils.EMPTY;
	}
	class TrailerVO implements Comparator<TrailerChassisDetailsVO>{

		@Override
		public int compare(TrailerChassisDetailsVO e1, TrailerChassisDetailsVO e2) {
		    return e1.getGtw().compareTo(e2.getGtw());
		}
		}
}
