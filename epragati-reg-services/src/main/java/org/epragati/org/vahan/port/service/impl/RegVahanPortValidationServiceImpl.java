package org.epragati.org.vahan.port.service.impl;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.constants.CovCategory;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.DistrictDAO;
import org.epragati.master.dao.FcDetailsDAO;
import org.epragati.master.dao.InsuranceCompanyDAO;
import org.epragati.master.dao.MasterCovDAO;
import org.epragati.master.dao.OwnershipDAO;
import org.epragati.master.dao.RegServiceDAO;
import org.epragati.master.dao.StateDAO;
import org.epragati.master.dao.TaxDetailsDAO;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dto.ApplicantAddressDTO;
import org.epragati.master.dto.ApplicantDetailsDTO;
import org.epragati.master.dto.DistrictDTO;
import org.epragati.master.dto.FcDetailsDTO;
import org.epragati.master.dto.InsuranceCompanyDTO;
import org.epragati.master.dto.MasterCovDTO;
import org.epragati.master.dto.OwnershipDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.StateDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.service.impl.CovServiceImpl;
import org.epragati.org.vahan.port.service.RegVahanPortValidationService;
import org.epragati.payment.dto.PaymentTransactionDTO;
import org.epragati.payments.dao.PaymentTransactionDAO;
import org.epragati.regservice.dao.CombinationServicesDAO;
import org.epragati.regservice.dto.CombinationServicesDTO;
import org.epragati.regservice.dto.NOCDetailsDTO;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.impl.RegistrationServiceImpl;
import org.epragati.rta.service.impl.service.RegistrationCardService;
import org.epragati.util.PermitsEnum;
import org.epragati.util.SourceEnum;
import org.epragati.util.StatusRegistration;
import org.epragati.util.payment.ClassOfVehicleEnum;
import org.epragati.util.payment.PayStatusEnum;
import org.epragati.util.payment.ServiceCodeEnum;
import org.epragati.util.payment.ServiceEnum;
import org.epragati.vahan.port.vo.RegVahanPortVO;
import org.epragati.vahan.sync.dao.VahanSyncDistrictMappingDAO;
import org.epragati.vahan.sync.dao.VahanSyncFuleMappingDAO;
import org.epragati.vahan.sync.dao.VahanSyncVehicleCategoryMappingDAO;
import org.epragati.vahan.sync.dto.VahanSyncDistrictMappingDTO;
import org.epragati.vahan.sync.dto.VahanSyncFuleMappingDTO;
import org.epragati.vahan.sync.dto.VahanSyncVehicleCategoryMappingDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.google.common.base.Splitter;

@Service
public class RegVahanPortValidationServiceImpl implements RegVahanPortValidationService {

	private static final Logger logger = LoggerFactory.getLogger(RegVahanPortValidationServiceImpl.class);

	@Autowired
	private VahanSyncDistrictMappingDAO districtMappingDAO;
	
	@Autowired
	private UserDAO userDAO;

	@Autowired
	private FcDetailsDAO fcDetailsDAO;

	@Autowired
	private TaxDetailsDAO taxDetailsDAO;

	@Autowired
	private RegServiceDAO regServiceDAO;

	@Autowired
	private RegistrationServiceImpl registrationServiceImpl;

	@Autowired
	private PaymentTransactionDAO paymentTransactionDAO;

	@Autowired
	private MasterCovDAO masterCovDAO;

	@Autowired
	private StateDAO stateDAO;

	@Autowired
	private OwnershipDAO ownershipDAO;

	@Autowired
	private InsuranceCompanyDAO insuranceCompanyDAO;

	@Autowired
	private CombinationServicesDAO servicesCombinationsDAO;

	@Autowired
	private VahanSyncVehicleCategoryMappingDAO vahanSyncVehicleCategoryMappingDAO;

	@Autowired
	private CovServiceImpl covServiceImpl;

	@Autowired
	private DistrictDAO districtDAO;

	@Autowired
	private RegistrationCardService registrationCardService;

	@Autowired
	private VahanSyncFuleMappingDAO vahanSyncFuleMappingDAO;

	@Override
	public Pair<RegVahanPortVO, List<String>> validateRegFields(RegistrationDetailsDTO regDto,
			RegVahanPortVO regVahanPortVO, List<String> errors) {
		long startTimeInMilli = System.currentTimeMillis();
		/*
		 * logger.
		 * info("Validations started for VAHANSYNC for REG No, [{}] and Time started, [{}] ms"
		 * , regDto.getPrNo(), startTimeInMilli);
		 */

		ApplicantDetailsDTO applicantDTO = regDto.getApplicantDetails();
		if (StringUtils.isAnyBlank(regDto.getPrNo())) {
			errors.add("prNo Not Found");
		} else {
			// compareFirstTwoCharacter(regDto.getPrNo(), errors);
			regVahanPortVO.setPrNo(regDto.getPrNo());
		}
		if (regDto.getVahanDetails() == null) {
			throw new BadRequestException("Vahan Details Not Found" + regDto.getPrNo());
		}
		if (regDto.getOfficeDetails() == null) {
			throw new BadRequestException("Office Details Not Found" + regDto.getPrNo());
		} else if (StringUtils.isAnyBlank(regDto.getOfficeDetails().getOfficeCode())) {
			errors.add("Office Details Not Found");
		} else {
			regVahanPortVO.setOfficeCode(replaceDefaultsOfficeCode(regDto.getOfficeDetails().getOfficeCode(), errors));
		}

		if (regDto.getPrGeneratedDate() != null) {
			regVahanPortVO.setPrGeneratedDate(regDto.getPrGeneratedDate().toLocalDate());
		} else if (regDto.getRegistrationValidity()!=null && regDto.getRegistrationValidity().getPrGeneratedDate() != null) {
			regVahanPortVO.setPrGeneratedDate(regDto.getRegistrationValidity().getPrGeneratedDate());
		} else {
			errors.add("prNo Generated Not Found");
		}
		if (regDto.getTrGeneratedDate() != null) {
			regVahanPortVO.setTrGeneratedDate(regDto.getTrGeneratedDate().toLocalDate());
		} else if (regDto.getRegistrationValidity()!=null && regDto.getRegistrationValidity().getTrGeneratedDate() != null) {
			regVahanPortVO.setTrGeneratedDate(regDto.getRegistrationValidity().getTrGeneratedDate());
		} else {
			if (regVahanPortVO.getPrGeneratedDate() != null) {
				regVahanPortVO.setTrGeneratedDate(regVahanPortVO.getPrGeneratedDate());
			}
		}
		if (StringUtils.isNotBlank(applicantDTO.getFirstName())) {
			regVahanPortVO.setNameOfOwner(deleteWhiteSpaces(applicantDTO.getFirstName(), 35));
		} else if (StringUtils.isNotBlank(applicantDTO.getDisplayName())) {
			regVahanPortVO.setNameOfOwner(deleteWhiteSpaces(applicantDTO.getDisplayName(), 35));
		} else {
			errors.add("First Name Not Found");
		}
		// as per nic FatherName not there NA
		regVahanPortVO.setFatherName("NA");
		if (StringUtils.isNotBlank(applicantDTO.getFatherName())) {
			regVahanPortVO
					.setFatherName(deleteWhiteSpaces(removeStopWords(applicantDTO.getFatherName().toLowerCase()), 60));
		}
		if (applicantDTO.getPresentAddress() == null) {
			errors.add("Present Address Details Not Found");
		} else if (applicantDTO.getPresentAddress().getDistrict() == null
				|| applicantDTO.getPresentAddress().getDistrict().getDistrictName() == null) {
			errors.add("Present Address District Details Not Found");
		} else if (applicantDTO.getPresentAddress().getState() == null
				|| applicantDTO.getPresentAddress().getState().getStateId() == null) {
			regVahanPortVO.setStateId("AP");
		} /*
			 * else if (applicantDTO.getPresentAddress().getPostOffice() == null||
			 * applicantDTO.getPresentAddress().getPostOffice().getPostOfficeCode() == null)
			 * { errors.add("Present Address PostOffice Details Not Found"); }
			 */ else {
			String[] persentaddress = setAddress(applicantDTO.getPresentAddress());
			regVahanPortVO.setPresentAddress1(persentaddress[0].toString().trim());
			if (persentaddress.length == 3 || persentaddress.length == 2) {
				regVahanPortVO.setPresentAddress2(persentaddress[1].toString().trim());
			}
			if (persentaddress.length == 3) {
				regVahanPortVO.setPresentAddress3(persentaddress[2].toString().trim());
			}
			// as per nic PresentPostOffice pin code not there 0
			regVahanPortVO.setPresentPostOfficeCode(0);
			if (applicantDTO.getPresentAddress().getPostOffice() != null
					&& applicantDTO.getPresentAddress().getPostOffice().getPostOfficeCode() != null) {
				regVahanPortVO
						.setPresentPostOfficeCode(applicantDTO.getPresentAddress().getPostOffice().getPostOfficeCode());
			}
			if (applicantDTO.getPresentAddress() != null && applicantDTO.getPresentAddress().getDistrict() != null
					&& StringUtils.isNotEmpty(applicantDTO.getPresentAddress().getDistrict().getDistrictName())) {
				regVahanPortVO.setPresentDistricName(
						firstLetterCaps(applicantDTO.getPresentAddress().getDistrict().getDistrictName()));
			}
			if (applicantDTO.getPresentAddress() != null && applicantDTO.getPresentAddress().getState() != null
					&& StringUtils.isNotEmpty(applicantDTO.getPresentAddress().getState().getStateName())) {
				regVahanPortVO.setStateId(
						setNicStateCode(applicantDTO.getPresentAddress().getState().getStateName(), errors));
			}
			/*
			 * regVahanPortVO.setPresentDistricCode(
			 * SetDistrictCode(applicantDTO.getPresentAddress().getDistrict().
			 * getDistrictId()));
			 */
			if (applicantDTO.getPresentAddress().getDistrict()!=null && applicantDTO.getPresentAddress().getDistrict().getDistricNicCode() == null) {
				regVahanPortVO.setPresentDistricCode(
						setNicDistrictCode(applicantDTO.getPresentAddress().getDistrict().getDistrictName(), errors));
			} else {
				regVahanPortVO
						.setPresentDistricCode(applicantDTO.getPresentAddress().getDistrict().getDistricNicCode());
			}
		}
		if (StringUtils.isNotBlank(regDto.getClassOfVehicle())) {
			regVahanPortVO.setClassOfVehicle(setNicCovCode(regDto.getClassOfVehicle(), errors));
		} else {
			errors.add("Class Of Vehicle Details Not Found");
		}
		if (applicantDTO.getAadharResponse() == null) {
			errors.add("permant Address Details Not Found ");
		} else {
			String persenteAdd = StringUtils.EMPTY;
			if (applicantDTO.getAadharResponse().getVillage_name() != null) {
				persenteAdd = applicantDTO.getAadharResponse().getVillage_name() + " ";
			}
			if (StringUtils.isNotEmpty(applicantDTO.getAadharResponse().getMandal_name())) {
				persenteAdd = persenteAdd + applicantDTO.getAadharResponse().getMandal_name() + " ";
			}
			if (StringUtils.isNotEmpty(applicantDTO.getAadharResponse().getDistrict_name())) {
				persenteAdd = persenteAdd + applicantDTO.getAadharResponse().getDistrict_name() + " ";
				regVahanPortVO
						.setPermanentDistricName(firstLetterCaps(applicantDTO.getAadharResponse().getDistrict_name()));
			}
			// As per NIC default Permanent state code is AP
			regVahanPortVO.setPermanentstateId("AP");
			if (StringUtils.isNotEmpty(applicantDTO.getAadharResponse().getStatecode())) {
				persenteAdd = persenteAdd + applicantDTO.getAadharResponse().getStatecode() + " ";
				regVahanPortVO.setPermanentstateId(applicantDTO.getAadharResponse().getStatecode());
			}
			regVahanPortVO.setPermanentPostOfficeCode(0);
			if (StringUtils.isNotEmpty(applicantDTO.getAadharResponse().getPincode())) {
				regVahanPortVO.setPermanentPostOfficeCode(
						convertStringToInteger(applicantDTO.getAadharResponse().getPincode(), errors));
			}

			String[] permanentAddress = addressLenghtFixedSize(persenteAdd);
			regVahanPortVO.setPermanentAddress1(permanentAddress[0].toString().trim());
			if (permanentAddress.length == 3 || permanentAddress.length == 2) {
				regVahanPortVO.setPermanentAddress2(permanentAddress[1].toString().trim());
			}
			if (permanentAddress.length == 3) {
				regVahanPortVO.setPermanentAddress3(permanentAddress[2].toString().trim());
			}
		}
		if (StringUtils.isNotBlank(regDto.getVahanDetails().getChassisNumber())) {
			regVahanPortVO.setChassisNumber(regDto.getVahanDetails().getChassisNumber());
		}
		regVahanPortVO.setEngineNumber("NA");
		if (StringUtils.isNotBlank(regDto.getVahanDetails().getEngineNumber())) {
			regVahanPortVO.setEngineNumber(regDto.getVahanDetails().getEngineNumber());
		}
		// as per nic SeatingCapacity not there 0
		regVahanPortVO.setSeatingCapacity(0);
		if (StringUtils.isNotBlank(regDto.getVahanDetails().getSeatingCapacity())) {
			regVahanPortVO
					.setSeatingCapacity(convertStringToInteger(regDto.getVahanDetails().getSeatingCapacity(), errors));
		}
		if (StringUtils.isNotBlank(regDto.getVahanDetails().getBodyTypeDesc())) {
			regVahanPortVO.setBodyTypeDesc(regDto.getVehicleDetails().getBodyTypeDesc());
		}
		if (regDto.getOwnerType() != null) {
			regVahanPortVO.setOwnerType(setOwnerShipTypes(regDto.getOwnerType().name()));
		} else {
			errors.add("OwnerShip Type Not Found ");
		}
		if (regDto.getVahanDetails().getNoCyl() != null) {
			regVahanPortVO.setNoOfCyl(convertStringToInteger(regDto.getVahanDetails().getNoCyl(), errors));
		} else {
			regVahanPortVO.setNoOfCyl(2);
		}
		// as per nic gvw not there 0
		regVahanPortVO.setGvw(0);
		if (regDto.getVahanDetails().getGvw() != null) {
			regVahanPortVO.setGvw(regDto.getVahanDetails().getGvw());
		}
		// as per nic ulw not there 0
		regVahanPortVO.setUlw(0);
		regVahanPortVO.setLadenWeight(0);
		if (regDto.getVahanDetails().getUnladenWeight() != null) {
			regVahanPortVO.setUlw(regDto.getVahanDetails().getUnladenWeight());
			regVahanPortVO.setLadenWeight(regDto.getVahanDetails().getUnladenWeight());
		}

		if (StringUtils.isNotBlank(regDto.getVahanDetails().getColor())
				&& regDto.getVahanDetails().getColor().length() > 20) {
			regVahanPortVO.setColor(deleteWhiteSpaces(regDto.getVahanDetails().getColor(), 20));
		} else if (StringUtils.isNotBlank(regDto.getVahanDetails().getColor())) {
			regVahanPortVO.setColor(regDto.getVahanDetails().getColor());
		} else {
			// as per nic color not there NA
			regVahanPortVO.setColor("NA");
		}

		if (StringUtils.isNotBlank(regDto.getVahanDetails().getManufacturedMonthYear())) {
			String manufacturedMonthYear = registrationCardService
					.checkManufacturedMonthYear(regDto.getVahanDetails().getManufacturedMonthYear());
			regVahanPortVO
					.setManufacturedMonth(convertStringToInteger(StringUtils.left(manufacturedMonthYear, 2), errors));
			regVahanPortVO
					.setManufacturedYear(convertStringToInteger(StringUtils.right(manufacturedMonthYear, 4), errors));
		} else {
			if (regVahanPortVO.getPrGeneratedDate() != null) {
				regVahanPortVO.setManufacturedMonth(regVahanPortVO.getPrGeneratedDate().getMonthValue());
				regVahanPortVO.setManufacturedMonth(regVahanPortVO.getPrGeneratedDate().getYear());
			}
		}
		// as per nic MakersModel not there NA
		regVahanPortVO.setMakersModel("NA");
		if (StringUtils.isNotBlank(regDto.getVahanDetails().getMakersModel())) {
			regVahanPortVO.setMakersModel(StringUtils.deleteWhitespace(regDto.getVahanDetails().getMakersModel()));
		}
		// as per nic MakersDesc not there 9999
		regVahanPortVO.setMakersDesc("9999");
		if (StringUtils.isNotBlank(regDto.getVahanDetails().getMakersDesc())) {
			regVahanPortVO.setMakersDesc(regDto.getVahanDetails().getMakersDesc());
		}
		// as per nic NormsOfVehicle not there 99
		regVahanPortVO.setNormsOfVehicle("99");
		if (StringUtils.isNotBlank(regDto.getVahanDetails().getPollutionNormsDesc())) {
			regVahanPortVO.setNormsOfVehicle(regDto.getVahanDetails().getPollutionNormsDesc());
		}
		// as per nic fuel not there 99
		regVahanPortVO.setFuelType(99);
		if (StringUtils.isNotBlank(regDto.getVahanDetails().getFuelDesc())) {
			regVahanPortVO.setFuelType(setNicFuelCode(regDto.getVahanDetails().getFuelDesc()));
		}

		if (regDto.getDealerDetails() == null || StringUtils.isAnyBlank(regDto.getDealerDetails().getDealerId())) {
			regVahanPortVO.setDealerId("NA");
		} else {
			regVahanPortVO.setDealerId(regDto.getDealerDetails().getDealerId());
		}

		if (regDto.getInvoiceDetails() != null && regDto.getInvoiceDetails().getInvoiceValue() != null) {
			regVahanPortVO.setInvoiceValue(regDto.getInvoiceDetails().getInvoiceValue());
		} else {
			// as per nic InvoiceValue not there 0
			regVahanPortVO.setInvoiceValue(0d);
		}

		if (regDto.getRegistrationValidity() != null
				&& regDto.getRegistrationValidity().getRegistrationValidity() != null) {
			regVahanPortVO
					.setRegistrationValidity(regDto.getRegistrationValidity().getRegistrationValidity().toLocalDate());
			regVahanPortVO
					.setFitnessValidUpto(regDto.getRegistrationValidity().getRegistrationValidity().toLocalDate());
		} else if (setRegValidity(regDto, regVahanPortVO, errors)) {
			// TRTT REG VALIDITY CHECK
		}
		else {
			errors.add("Registration Validity details Not Found ");
		}

		if (regDto.getVahanDetails().getRearAxleDesc() != null && regDto.getVahanDetails().getFrontAxleDesc() != null) {
			regVahanPortVO.setRearAxleDesc(regDto.getVahanDetails().getRearAxleDesc());
			regVahanPortVO.setFrontAxleDesc(regDto.getVahanDetails().getFrontAxleDesc());
		} else {
			regVahanPortVO.setRearAxleDesc(StringUtils.EMPTY);
			regVahanPortVO.setFrontAxleDesc(StringUtils.EMPTY);
		}

		if (regDto.getVahanDetails().getRearAxleWeight() != null
				&& regDto.getVahanDetails().getFrontAxleWeight() != null) {
			regVahanPortVO.setRearAxleWeight(regDto.getVahanDetails().getRearAxleWeight());
			regVahanPortVO.setFrontAxleWeight(regDto.getVahanDetails().getFrontAxleWeight());
		} else {
			regVahanPortVO.setRearAxleWeight(0);
			regVahanPortVO.setFrontAxleWeight(0);
		}
		// as per nic InsuranceCompanyName not there 9999
		regVahanPortVO.setInsuranceCompanyName(9999);
		if (regDto.getInsuranceDetails() != null && StringUtils.isNotEmpty(regDto.getInsuranceDetails().getCompany())) {
			regVahanPortVO.setInsuranceCompanyName(setInsuranceCode(regDto.getInsuranceDetails().getCompany()));
		}
		// as per nic InsurancePolicyType not there 4
		regVahanPortVO.setInsurancePolicyType(4);
		if (regDto.getInsuranceDetails() != null
				&& StringUtils.isNotEmpty(regDto.getInsuranceDetails().getPolicyType())) {
			regVahanPortVO.setInsurancePolicyType(setInsuranceType(regDto.getInsuranceDetails().getPolicyType()));
		}
		// as per nic InsurancePolicyNo not there NA
		regVahanPortVO.setInsurancePolicyNo("NA");
		if (regDto.getInsuranceDetails() != null
				&& StringUtils.isNotEmpty(regDto.getInsuranceDetails().getPolicyNumber())) {
			regVahanPortVO.setInsurancePolicyNo(regDto.getInsuranceDetails().getPolicyNumber());
		}
		if ((regDto.getInsuranceDetails() == null || regDto.getInsuranceDetails().getValidFrom() == null
				|| regDto.getInsuranceDetails().getValidTill() == null)
				&& regVahanPortVO.getPrGeneratedDate() != null) {
			regVahanPortVO.setInsurancevalidFrom(regVahanPortVO.getPrGeneratedDate());
			regVahanPortVO.setInsurancevalidTO(regVahanPortVO.getPrGeneratedDate());
		} else {
			regVahanPortVO.setInsurancevalidFrom(regDto.getInsuranceDetails().getValidFrom());
			regVahanPortVO.setInsurancevalidTO(regDto.getInsuranceDetails().getValidTill());
		}

		if (regDto.getFinanceDetails() != null) {
			Optional<UserDTO> userDTOOpt= Optional.empty();
			if (StringUtils.isNotBlank(regDto.getFinanceDetails().getFinancerName())) {
				regVahanPortVO
						.setFinanceName(StringUtils.deleteWhitespace(regDto.getFinanceDetails().getFinancerName()));
				regVahanPortVO.setIsFinancier(Boolean.TRUE);
			}
			regVahanPortVO.setFinanceType("Hypothecation");
			if (regDto.getFinanceDetails().getFinanceType() != null
					&& StringUtils.isNotEmpty(regDto.getFinanceDetails().getFinanceType().getFinanceType())) {
				regVahanPortVO.setFinanceType(regDto.getFinanceDetails().getFinanceType().getFinanceType());
			} /*
				 * else{ regVahanPortVO.setFinanceType("Hypothecation");
				 * 
				 * }
				 */
			if(regDto.getFinanceDetails().getAgreementDate() !=null) {
			regVahanPortVO.setFinanceAgreementDate(regDto.getFinanceDetails().getAgreementDate());
			}
			if (StringUtils.isNotBlank(regDto.getFinanceDetails().getUserId())) {
				 userDTOOpt =  userDAO.findByUserId(regDto.getFinanceDetails().getUserId());
			}
			if (!userDTOOpt.isPresent()) {
				if (regDto.getFinanceDetails().getDistrict() != null&&StringUtils.isNotEmpty(regDto.getFinanceDetails().getDistrict().getDistrictName())) {
					regVahanPortVO.setFinanceDistricCode(
							finNicDistrictQurey(regDto.getFinanceDetails().getDistrict(),regDto.getFinanceDetails().getState()));
				}
				if (regDto.getFinanceDetails().getState() != null&&StringUtils.isNotEmpty(regDto.getFinanceDetails().getState().getStateName())) {
					regVahanPortVO.setFinancestateId(
							setNicStateCode(regDto.getFinanceDetails().getState().getStateName(), errors));
				}
				if (regDto.getFinanceDetails().getDistrict() != null && regDto.getFinanceDetails().getState() != null
						&& StringUtils.isNotEmpty(regDto.getFinanceDetails().getDistrict().getDistrictName())
						&& StringUtils.isNotEmpty(regDto.getFinanceDetails().getState().getStateId())) {
					regVahanPortVO.setFinanceAddress1(regDto.getFinanceDetails().getDistrict().getDistrictName()
							+ regDto.getFinanceDetails().getState().getStateId());
				}
				regVahanPortVO.setIsFinancier(Boolean.TRUE);
			}
			else if (userDTOOpt.isPresent()) {
				if (StringUtils.isNotBlank(userDTOOpt.get().getInstitutionName())) {
					regVahanPortVO.setFinanceName(StringUtils.deleteWhitespace(userDTOOpt.get().getInstitutionName()));
				}
				else {
					regVahanPortVO.setFinanceName(StringUtils.deleteWhitespace(userDTOOpt.get().getFirstName()));
				}
				regVahanPortVO.setIsFinancier(Boolean.TRUE);
				addFinancierAddress(regVahanPortVO, userDTOOpt, errors);
			} else {
				if (userDTOOpt.isPresent() && StringUtils.isNotEmpty(userDTOOpt.get().getParentId())) {
					Optional<UserDTO> financierParentDetails = userDAO.findByUserId(userDTOOpt.get().getParentId());
					if (financierParentDetails.isPresent()
							&& StringUtils.isNotBlank(userDTOOpt.get().getInstitutionName())) {
						regVahanPortVO
								.setFinanceName(StringUtils.deleteWhitespace(userDTOOpt.get().getInstitutionName()));
						regVahanPortVO.setIsFinancier(Boolean.TRUE);
						addFinancierAddress(regVahanPortVO, financierParentDetails, errors);
					}
				} 
			}
		}

		if (StringUtils.isNotEmpty(regDto.getVehicleType())
						&& regDto.getVehicleType().equals(CovCategory.N.getCode())
						&& regDto.getRegistrationValidity() != null
						&& regDto.getRegistrationValidity().getRegistrationValidity() != null) {
			regVahanPortVO
					.setFitnessValidUpto(regDto.getRegistrationValidity().getRegistrationValidity().toLocalDate());
		}

		if (StringUtils.isNotEmpty(regDto.getVehicleType())
				&& regDto.getVehicleType().equals(CovCategory.T.getCode())) {
			List<FcDetailsDTO> fcDetailsList = fcDetailsDAO
					.findFirst5ByStatusIsTrueAndPrNoOrderByCreatedDateDesc(regDto.getPrNo());
			if (CollectionUtils.isNotEmpty(fcDetailsList)) {
				FcDetailsDTO fcDetailsDTO = fcDetailsList.stream().findFirst().get();
				if (fcDetailsDTO.getInspectedDate() != null) {
					regVahanPortVO.setFitnessinspectedDate(fcDetailsDTO.getInspectedDate().toLocalDate());
				} else {
					regVahanPortVO.setFitnessinspectedDate(fcDetailsDTO.getFcIssuedDate().toLocalDate());
				}
				regVahanPortVO.setFitnessValidUpto(fcDetailsDTO.getFcValidUpto());
				regVahanPortVO.setIsFitness(Boolean.TRUE);
				// regVahanPortVO.setFitnessResult(fcDetailsDTO);
			} else if (regDto.getRegistrationValidity() != null
					&& regDto.getRegistrationValidity().getFcValidity() != null) {
				regVahanPortVO.setFitnessValidUpto(regDto.getRegistrationValidity().getFcValidity());
				if (regVahanPortVO.getPrGeneratedDate() != null) {
					regVahanPortVO.setFitnessinspectedDate(regVahanPortVO.getPrGeneratedDate());
				} else if (regDto.getlUpdate() != null) {
					regVahanPortVO.setFitnessinspectedDate(regDto.getlUpdate().toLocalDate());
				}
				regVahanPortVO.setIsFitness(Boolean.TRUE);
			} else if (regDto.getRegistrationValidity() != null
					&& regDto.getRegistrationValidity().getFcValidity() == null) {
				if (regVahanPortVO.getPrGeneratedDate() != null) {
					regVahanPortVO.setFitnessinspectedDate(regVahanPortVO.getPrGeneratedDate());
					regVahanPortVO.setFitnessValidUpto(regVahanPortVO.getPrGeneratedDate());
				} else if (regDto.getlUpdate() != null) {
					regVahanPortVO.setFitnessinspectedDate(regDto.getlUpdate().toLocalDate());
				}
				regVahanPortVO.setIsFitness(Boolean.TRUE);
			}

		}

		regVahanPortVO.setTowCount(towCount(regDto.getPrNo()));
		getLatestTaxDetails(regVahanPortVO, regDto.getApplicationNo(), regDto.getPrNo(), regDto, errors);
		getPaymentDetails(regVahanPortVO, getApplicationNo(regDto.getPrNo(), regDto.getApplicationNo()), regDto,
				errors);

		if (regDto.getNocDetails() != null) {
			getNocDetails(regVahanPortVO, regDto, errors);
			regVahanPortVO.setNocIssuedOrNot('N');
		} else if (regDto.getActionStatus() != null) {
			regVahanPortVO.setNocIssuedOrNot(status(regDto.getActionStatus()));
		} else {
			regVahanPortVO.setNocIssuedOrNot('A');
		}

		if (regDto.getTheftDetails() != null) {
			if (StringUtils.isNotBlank(regDto.getTheftDetails().getFirNo())
					&& regDto.getTheftDetails().getComplaintDate() != null
					&& StringUtils.isNotBlank(regDto.getTheftDetails().getRemarks())
					&& StringUtils.isNotBlank(regDto.getTheftDetails().getPoliceStationName())) {
				regVahanPortVO.setFirNo(regDto.getTheftDetails().getFirNo());
				regVahanPortVO.setFirDate(regDto.getTheftDetails().getComplaintDate());
				regVahanPortVO.setComplain(StringUtils.left(regDto.getTheftDetails().getRemarks(), 300));
				regVahanPortVO
						.setComplainEnteredBy(deleteWhiteSpaces(regDto.getTheftDetails().getPoliceStationName(), 10));
				regVahanPortVO.setIsTheft(Boolean.TRUE);
			} else {
				getTheftDetails(regVahanPortVO, regDto);
			}
		}
		regVahanPortVO.setVehicleCategory(setVehicleCategory(regDto.getClassOfVehicle(), regVahanPortVO.getGvw(),
				regDto.getVahanDetails().getVehicleClass(), errors));
		if (regDto.getApplicantType() != null && regDto.getApplicantType().equals("OTHERSTATE")) {
			regVahanPortVO.setTypeOfRegistration('O');
		} else {
			regVahanPortVO.setTypeOfRegistration('A');
		}
		regVahanPortVO.setPurposeCode(getRegregServiceCode(regDto.getPrNo()));
		/*
		 * logger.
		 * info("Validations ended for VAHANSYNC for REG No, [{}] and Total Time , [{}] ms"
		 * , regDto.getPrNo(), (System.currentTimeMillis() - startTimeInMilli));
		 */ return Pair.of(regVahanPortVO, errors);
	}

	private Integer setInsuranceType(String policyType) {
		Integer insurancePolicyType = 4;
		switch (policyType) {
		case "Comprehensive":
			insurancePolicyType = 1;
			break;
		case "Third Party":
			insurancePolicyType = 2;
			break;
		default:
			break;
		}
		return insurancePolicyType;
	}

	public String[] setAddress(ApplicantAddressDTO address) {
		StringBuilder sb = new StringBuilder();
		String space = StringUtils.SPACE;
		sb.append(replaceDefaults(address.getDoorNo())).append(space);
		sb.append(replaceDefaults(address.getStreetName())).append(space);
		sb.append(replaceDefaults(address.getTownOrCity())).append(space);
		if (address.getVillage() != null && address.getVillage().getVillageName() != null) {
			sb.append(StringUtils.deleteWhitespace((address.getVillage().getVillageName()))).append(space);
		}
		if (address.getMandal() != null&&address.getMandal().getMandalName()!=null) {
			sb.append(StringUtils.deleteWhitespace((address.getMandal().getMandalName()))).append(space);
		}
		if (address.getDistrict() != null&&address.getDistrict().getDistrictName()!=null) {
			sb.append(replaceDefaults(address.getDistrict().getDistrictName())).append(space);
		}
		if (address.getPostOffice() != null&&address.getPostOffice().getPostOfficeCode()!=null) {
			sb.append(replaceDefaults(address.getPostOffice().getPostOfficeCode().toString())).append(space);
		}
		if(address.getState()!=null&&address.getState().getStateName()!=null) {
			sb.append(replaceDefaults(address.getState().getStateName()));
		}
		return addressLenghtFixedSize(sb.toString());
	}

	private String splitToFixedLenth(String Address) {
		Iterable<String> result = Splitter.fixedLength(35).split(Address);
		return result.toString();

	}

	private String replaceDefaults(String input) {

		if (StringUtils.isBlank(input)) {
			return StringUtils.EMPTY;
		}
		return input;
	}

	private static String firstLetterCaps(String name) {
		String firstLetter = name.substring(0, 1).toUpperCase();
		String restLetters = name.substring(1).toLowerCase();
		return StringUtils.deleteWhitespace(firstLetter + restLetters);
	}

	private Integer convertStringToInteger(String value, List<String> errors) {
		if (null != value) {
			return Integer.parseInt(value);
		}
		errors.add("Unable to convert String To Integer");
		return 0;
	}

	public Integer getRegregServiceCode(String prNo) {
		List<RegServiceDTO> regServiceDTOList = regServiceDAO.findFirst5ByPrNoAndApplicationStatusAndServiceIdsNotNull(
				prNo, StatusRegistration.APPROVED.getDescription());
		if (CollectionUtils.isNotEmpty(regServiceDTOList)) {
			registrationServiceImpl.validateMissingCreatedDate(regServiceDTOList);
			regServiceDTOList.sort((p2, p1) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
			RegServiceDTO regServiceDTO = regServiceDTOList.stream().findFirst().get();
			if(CollectionUtils.isNotEmpty(regServiceDTO.getServiceIds())) {
			Integer serviceId = regServiceDTO.getServiceIds().stream().findFirst().get();
			if (serviceId == 64 && regServiceDTO.getPdtl()!=null&&regServiceDTO.getPdtl().getPermitClass()
					.equals(PermitsEnum.PermitType.PRIMARY.getDescription())) {
				return 26;
			}
			if (serviceId == 64 &&regServiceDTO.getPdtl()!=null&& regServiceDTO.getPdtl().getPermitClass()
					.equals(PermitsEnum.PermitType.TEMPORARY.getDescription())) {
				return 35;
			}
			Optional<CombinationServicesDTO> combinationService = servicesCombinationsDAO.findByServiceId(serviceId);
			if (combinationService.isPresent()) {
				if (combinationService.get().getNicCode() != null) {
					return combinationService.get().getNicCode();
				}
			}
		}
		}
		return 1;
	}

	private void getPaymentDetails(RegVahanPortVO regVahanPortVO, String applicationNo, RegistrationDetailsDTO regDto,
			List<String> errors) {
		List<PaymentTransactionDTO> paymentTransactionList = paymentTransactionDAO
				.findByPayStatusAndApplicationFormRefNum(PayStatusEnum.SUCCESS.getDescription(), applicationNo);
		if (CollectionUtils.isNotEmpty(paymentTransactionList)) {
			PaymentTransactionDTO payments = paymentTransactionList.get(paymentTransactionList.size() - 1);
			if (payments.getResponse() != null&&payments.getResponse().getResponseTime()!=null) {
				regVahanPortVO.setPaymentResponseTime(payments.getResponse().getResponseTime().toLocalDate());
			} else if (payments.getlUpdate() != null) {
				regVahanPortVO.setPaymentResponseTime(payments.getlUpdate().toLocalDate());
			} else {
				regVahanPortVO.setPaymentResponseTime(payments.getCreatedDate().toLocalDate());
			}
			regVahanPortVO.setPaymentCollectedMode("Online");
			if (payments.getBreakPaymentsSave() != null) {
				regVahanPortVO.setTotalFeePay(payments.getBreakPaymentsSave().getGrandTotalFees().longValue());
			} else {
				regVahanPortVO.setTotalFeePay(payments.getFeeDetailsDTO().getTotalFees().longValue());
			}
			regVahanPortVO.setApplicationFormRefNum(payments.getApplicationFormRefNum());
		} else {
			// As per jagan inputs added validation tax details not found
			if ((regDto.getApplicantType() != null && (regDto.getApplicantType().equalsIgnoreCase("WITHINTHESTATE")
					|| regDto.getApplicantType().equalsIgnoreCase("Paper RC")))
					|| (StringUtils.isNotEmpty(regDto.getSource())
							&& (regDto.getSource().equals(SourceEnum.CFST0.getDesc())
									|| regDto.getSource().equals(SourceEnum.CFST1.getDesc())))
					|| (regDto.getMigrationSource() != null
							&& regDto.getMigrationSource().equalsIgnoreCase("ONLINE1.2"))) {
				if (regDto.getCreatedDate() != null || regDto.getlUpdate() != null) {
					regVahanPortVO.setPaymentResponseTime(
							regDto.getCreatedDate() != null ? regDto.getCreatedDate().toLocalDate()
									: regDto.getlUpdate().toLocalDate());
				} else {
					Optional<RegServiceDTO> regService = regServiceDAO.findByApplicationNo(regDto.getApplicationNo());
					if (regService.isPresent()
							&& (regService.get().getCreatedDate() != null || regService.get().getlUpdate() != null)) {
						regVahanPortVO.setPaymentResponseTime(regService.get().getCreatedDate() != null
								? regService.get().getCreatedDate().toLocalDate()
								: regService.get().getlUpdate().toLocalDate());
					} else {
						errors.add("Created Date Not Found");
					}
				}
				regVahanPortVO.setTotalFeePay(0l);
				regVahanPortVO.setPaymentCollectedMode("Online");
				regVahanPortVO.setApplicationFormRefNum(regDto.getApplicationNo());
			} else {
				errors.add("Payment Details Not Found ");
			}
		}
	}

	private void getNocDetails(RegVahanPortVO regVahanPortVO, RegistrationDetailsDTO regDto, List<String> errors) {
		if (regDto.getNocDetails() != null) {
			Optional<RegServiceDTO> regNoc = regServiceDAO
					.findByPrNoAndServiceTypeInAndApplicationStatusOrderByCreatedDateDesc(regDto.getPrNo(),
							Arrays.asList(ServiceEnum.ISSUEOFNOC), StatusRegistration.APPROVED.getDescription());
			if (regNoc.isPresent()) {
				if (regNoc.get().getlUpdate() != null) {
					regVahanPortVO.setNocRtaIssueDate(regNoc.get().getlUpdate().toLocalDate());
				} else {
					regVahanPortVO.setNocRtaIssueDate(regNoc.get().getCreatedDate().toLocalDate());
				}
				regVahanPortVO.setNocApplicatioNo(setNocApplicationNo(regNoc.get()));
			}
			else {
				errors.add("Noc Data Not Found");
			}
			NOCDetailsDTO nocDto = regDto.getNocDetails();
			regVahanPortVO.setNocRtaOfficeName(nocDto.getState());
			regVahanPortVO.setNocRtaOfficeCode(nocDto.getRtaOffice());
			regVahanPortVO.setNocState(setNicStateCode(nocDto.getState(), errors));
			regVahanPortVO.setIsNocIssued(Boolean.TRUE);
		}
	}

	private void getTheftDetails(RegVahanPortVO regVahanPortVO, RegistrationDetailsDTO regDto) {
		List<ServiceEnum> servcieType = new ArrayList<>();
		servcieType.add(ServiceEnum.THEFTINTIMATION);
		servcieType.add(ServiceEnum.OBJECTION);
		Optional<RegServiceDTO> regThet = regServiceDAO
				.findByPrNoAndServiceTypeInAndApplicationStatusOrderByCreatedDateDesc(regDto.getPrNo(), servcieType,
						StatusRegistration.APPROVED.getDescription());
		if (regThet.isPresent()&&regThet.get().getTheftDetails()!=null) {
			regVahanPortVO.setFirNo(regThet.get().getTheftDetails().getFirNo());
			regVahanPortVO.setFirDate(regThet.get().getTheftDetails().getComplaintDate());
			regVahanPortVO.setComplain(StringUtils.left(regThet.get().getTheftDetails().getRemarks(), 300));
			regVahanPortVO.setComplainEnteredBy("NA");
			if (regThet.get().getTheftDetails().getPoliceStationName() != null) {
				regVahanPortVO.setComplainEnteredBy(
						deleteWhiteSpaces((regThet.get().getTheftDetails().getPoliceStationName()).trim(), 10));
			}
			regVahanPortVO.setIsTheft(Boolean.TRUE);
		}
	}

	public void getLatestTaxDetails(RegVahanPortVO regVahanPortVO, String applicationNo, String prNo,
			RegistrationDetailsDTO regDto, List<String> errors) {
		List<String> taxTypes = new ArrayList<>();
		List<TaxDetailsDTO> taxDtoList = null;
		TaxDetailsDTO taxDto = null;
		taxTypes.add(ServiceCodeEnum.QLY_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.HALF_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.YEAR_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.LIFE_TAX.getCode());

		taxDtoList = taxDetailsDAO.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(applicationNo,
				taxTypes);
		if (taxDtoList == null || taxDtoList.isEmpty()) {
			taxDtoList = taxDetailsDAO.findFirst10ByPrNoAndPaymentPeriodInOrderByCreatedDateDesc(prNo, taxTypes);
		}

		if (CollectionUtils.isNotEmpty(taxDtoList)) {

			registrationServiceImpl.updatePaidDateAsCreatedDate(taxDtoList);

			taxDtoList.sort((o1, o2) -> o2.getCreatedDate().compareTo(o1.getCreatedDate()));

			for (TaxDetailsDTO taxDetailsDTO : taxDtoList) {
				if (taxDetailsDTO.getTaxPeriodEnd() != null) {
					taxDto = new TaxDetailsDTO();
					taxDto = taxDetailsDTO;
					break;
				}
			}
			if (taxDto != null) {
				regVahanPortVO.setTaxFromDate(taxDto.getTaxPeriodFrom());
				regVahanPortVO.setTaxValidUpto(taxDto.getTaxPeriodEnd());
				regVahanPortVO.setTotalFee(taxDto.getTaxAmount());
				regVahanPortVO.setTaxFineAmount(0);
				if (taxDto.getTaxPaidDate() != null) {
					regVahanPortVO.setTaxPaidDate(taxDto.getTaxPaidDate());
				} else {
					regVahanPortVO.setTaxPaidDate(taxDto.getCreatedDate().toLocalDate());
				}
				regVahanPortVO.setTaxMode(taxDto.getPaymentPeriod().substring(0, 1).toUpperCase());

			} else {
				// As per jagan inputs added validation tax details not found
				validTaxRecordNotFound(regVahanPortVO);
			}
		} else {
			// As per jagan inputs added validation tax details not found
			validTaxRecordNotFound(regVahanPortVO);
		}
	}

	public void validTaxRecordNotFound(RegVahanPortVO regVahanPortVO) {

		regVahanPortVO.setTaxFromDate(LocalDate.now());
		regVahanPortVO.setTaxValidUpto(LocalDate.now());
		regVahanPortVO.setTotalFee(0l);
		regVahanPortVO.setTaxFineAmount(0);
		regVahanPortVO.setTaxPaidDate(LocalDate.now());
		regVahanPortVO.setTaxMode("L");
	}

	private Integer replaceDefaultsOfficeCode(String input, List<String> errors) {
		String office = input.replace("AP", "");
		return convertStringToInteger(office, errors);
	}

	private Integer towCount(String prNo) {

		long count = regServiceDAO.countByPrNoAndServiceTypeInAndApplicationStatus(prNo,
				Arrays.asList(ServiceEnum.TRANSFEROFOWNERSHIP), StatusRegistration.APPROVED.getDescription()) + 1;
		return (int) count;

	}

	private Integer setNicCovCode(String cov, List<String> errors) {
		MasterCovDTO masterCov = masterCovDAO.findByCovcode(cov);
		if (masterCov != null) {
			return masterCov.getNicCovCode();
		}
		errors.add("Nic CovCode Not Found");
		return 0;
	}

	private String stateCode(String stateName, List<String> errors) {
		Optional<StateDTO> optionalStateDTO = stateDAO.findByStateName(stateName);
		if (optionalStateDTO.isPresent()) {
			return optionalStateDTO.get().getStateId();
		}
		errors.add("State Code Not Found");
		return StringUtils.EMPTY;
	}

	private Integer setOwnerShipTypes(String ownerShipType) {
		Optional<OwnershipDTO> optionalOwnershipDTO = ownershipDAO.findByDescription(ownerShipType);
		if (optionalOwnershipDTO.isPresent()) {
			return optionalOwnershipDTO.get().getNicOwnerCode();
		}
		return 99;
	}

	private Integer setInsuranceCode(String insuranceCompanyName) {
		Optional<InsuranceCompanyDTO> optionalInsuranceCompanyDTO = insuranceCompanyDAO
				.findByInsCompidDescription(insuranceCompanyName);
		if (optionalInsuranceCompanyDTO.isPresent()) {
			return optionalInsuranceCompanyDTO.get().getNicInsuranceCode();
		}
		return 9999;
	}

	private Integer setNicDistrictCode(String districtId, List<String> errors) {
		Optional<DistrictDTO> districtCodeOpt = districtDAO.findByDistrictName(districtId);
		if (districtCodeOpt.isPresent() && districtCodeOpt.get().getDistricNicCode() != null) {
			return districtCodeOpt.get().getDistricNicCode();
		} else {
			errors.add("Nic District code Not Found");
		}
		return 0;
	}

	private String setVehicleCategory(String covCode, Integer gvw, String vehicelClass, List<String> errors) {
		if (StringUtils.isBlank(covCode) && gvw != null) {
			errors.add("Class Of Vehilce is empty gvw is empty");
		}
		String weightType = covServiceImpl.getWeightTypeDetails(gvw);
		Optional<VahanSyncVehicleCategoryMappingDTO> optionalVehicleCategory = vahanSyncVehicleCategoryMappingDAO
				.findByCovCodeInAndWeightTypeInAndStatusIsTrue(Arrays.asList(covCode), Arrays.asList(weightType));
		if (!optionalVehicleCategory.isPresent()) {
			errors.add("No Record Found in master nic Vehicle Category mapping with cov[" + covCode + "]" + "with gvw ["
					+ gvw + "]");
		} else {
			if (optionalVehicleCategory.get().getNicCode().equals("2WIC")) {
				if (vehicelClass.equalsIgnoreCase("L2")) {
					return optionalVehicleCategory.get().getNicCode();
				}
				if (vehicelClass.equalsIgnoreCase("M1") || vehicelClass.equalsIgnoreCase("M2")) {
					return "4WIC";
				}
			}
		}
		return optionalVehicleCategory.isPresent() ? optionalVehicleCategory.get().getNicCode() : "";
	}

	private Character status(String actionStatus) {
		Character status = 'A';
		switch (actionStatus) {
		case "SUSPEND":
			status = 'D';
			break;
		case "CANCELED":
			status = 'C';
			break;
		case "REVOKED":
			status = 'Y';
			break;

		default:
			break;
		}
		return status;
	}

	private String getApplicationNo(String prNo, String applicationNo) {
		List<ServiceEnum> listOfServices = new ArrayList<>();
		List<StatusRegistration> stauslist = new ArrayList<>();
		listOfServices.add(ServiceEnum.PERDATAENTRY);
		listOfServices.add(ServiceEnum.THEFTINTIMATION);
		listOfServices.add(ServiceEnum.THEFTREVOCATION);
		listOfServices.add(ServiceEnum.OBJECTION);
		listOfServices.add(ServiceEnum.VEHICLESTOPPAGE);
		listOfServices.add(ServiceEnum.VEHICLESTOPPAGEREVOKATION);

		stauslist.add(StatusRegistration.PAYMENTDONE);
		stauslist.add(StatusRegistration.APPROVED);
		List<RegServiceDTO> regServicesList = new ArrayList<RegServiceDTO>();
		if (StringUtils.isNotBlank(prNo)) {
			regServicesList = regServiceDAO.findByPrNoAndServiceTypeNotInAndSourceIsNullOrderByCreatedDateDesc(prNo,
					listOfServices);
		}
		if (CollectionUtils.isNotEmpty(regServicesList)) {
			List<RegServiceDTO> filterList = regServicesList.stream()
					.filter(val -> stauslist.contains(val.getApplicationStatus())).collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(filterList)) {
				RegServiceDTO regServiceDTO = filterList.stream().findFirst().get();
				if (StringUtils.isNotEmpty(regServiceDTO.getApplicationNo())
						&& StringUtils.isEmpty(regServiceDTO.getSource())) {
					return regServiceDTO.getApplicationNo();
				}
			}
			return applicationNo;
		} else {
			return applicationNo;
		}
	}

	private void compareFirstTwoCharacter(String prNo, List<String> errors) {
		String prNoFirst = StringUtils.left(prNo, 2);
		if (!prNoFirst.equals("AP")) {
			errors.add("Pr number from data entry and not applied Reassignment of Vehicles Pr number");
		}
	}

	private String deleteWhiteSpaces(String input, Integer value) {
		return StringUtils.left(StringUtils.deleteWhitespace(input), value);

	}

	private String removeStopWords(String input) {
		input = input.replaceAll("s/o|f/o|w/o|c/o|:", "");
		input = input.trim();
		String strinput[] = input.split(" ");
		StringBuilder stringBuilder = new StringBuilder();
		for (String string : strinput) {

			stringBuilder.append(string);
		}
		input = stringBuilder.toString();
		return input;
	}

	private String[] addressLenghtFixedSize(String address) {
		String address1 = StringUtils.EMPTY;
		String address2 = StringUtils.EMPTY;
		String address3 = StringUtils.EMPTY;
		if (StringUtils.isNotEmpty(address)) {
			String strinput[] = address.split(" ");

			for (String string : strinput) {
				if (address1.length() < 50) {
					address1 = setFixedSizeAddress(address1, string);
				}
				if (address1.length() >= 50 && address2.length() < 50) {
					address2 = setFixedSizeAddress(address2, string);
				}
				if (address2.length() >= 50 && address3.length() < 50) {
					address3 = setFixedSizeAddress(address3, string);
				}
			}
		}
		String[] completeaddress = { address1, address2, address3 };
		return completeaddress;
	}

	private String setNicStateCode(String stateName, List<String> errors) {
		Optional<StateDTO> stateOpt = stateCodeQurey(stateName);
		if (stateOpt.isPresent() && StringUtils.isNotEmpty(stateOpt.get().getNicStateCode())) {
			return stateOpt.get().getNicStateCode();
		}
		errors.add("Nic state code not found in matser state");
		return StringUtils.EMPTY;
	}

	private Integer setNicFuelCode(String fuelType) {
		Optional<VahanSyncFuleMappingDTO> fuleMappingOpt = vahanSyncFuleMappingDAO
				.findByFuelTypeAndStatusTrue(fuelType);
		if (fuleMappingOpt.isPresent()) {
			return fuleMappingOpt.get().getFuelCode();
		}
		return 99;
	}

	private String setFixedSizeAddress(String address, String name) {
		address += name + StringUtils.SPACE;
		if (address.length() > 50) {
			address = address.replace(name, StringUtils.EMPTY);
			int stringlen = address.length();
			Integer diff = 50 - stringlen;
			while (diff != 0) {
				address = address + StringUtils.SPACE;
				diff = diff - 1;
				continue;
			}
		}
		return address;
	}

	
	/**
	 * for noc records remove '/' from application no
	 * 
	 * @params regservices dto
	 * @return applicationNo
	 */
	String setNocApplicationNo(RegServiceDTO regNoc) {
		if (StringUtils.isNotEmpty(regNoc.getSource())) {
			String applicationNo = regNoc.getApplicationNo();
			return applicationNo.replace("/", "");
		}
		return regNoc.getApplicationNo();
	}
	
	/**
	 * for TRTT cov's adding RegistrationValidity for the records which are not having RegistrationValidity field
	 * 
	 * @params RegistrationDetailsDTO,RegVahanPortVO,errors
	 * @return returns true when having RegistrationValidity is null PrGeneratedDate is not null ,and returns false when RegistrationValidity is null PrGeneratedDate is null
	 */
	boolean setRegValidity(RegistrationDetailsDTO regDto, RegVahanPortVO regVahanPortVO, List<String> errors) {
		if (regDto.getRegistrationValidity() == null
				|| regDto.getRegistrationValidity().getRegistrationValidity() == null
						&& regDto.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TRTT.getCovCode())
						&& regVahanPortVO.getPrGeneratedDate() != null) {
			regVahanPortVO.setRegistrationValidity(regVahanPortVO.getPrGeneratedDate().plusYears(15));
			regVahanPortVO.setFitnessValidUpto(regVahanPortVO.getRegistrationValidity());
			return true;
		}
		errors.add("Registration Validity details Not Found ");
		return false;
	}
	
	void addFinancierAddress(RegVahanPortVO regVahanPortVO, Optional<UserDTO> financierParentDetails,
			List<String> errors) {
		if (financierParentDetails.isPresent()) {
			regVahanPortVO.setFinanceDistricCode(
					setFinNicDistrictCode(financierParentDetails));
		}
		if (financierParentDetails.get().getState() != null
				&& StringUtils.isNotBlank(financierParentDetails.get().getState().getStateId())) {
			regVahanPortVO.setFinancestateId(setFincStateCode(financierParentDetails.get().getState().getStateName()));
		}
		if (financierParentDetails.get().getDistrict() != null
				&& financierParentDetails.get().getDistrict().getDistrictName() != null
				&& regVahanPortVO.getFinancestateId() != null) {
			regVahanPortVO.setFinanceAddress1(
					financierParentDetails.get().getDistrict().getDistrictName() + regVahanPortVO.getFinancestateId());
		}
	}

	private Integer setFinNicDistrictCode(Optional<UserDTO> finDetOpt) {
		if (finDetOpt.get().getDistrict() != null && StringUtils.isNotBlank(finDetOpt.get().getDistrict().getDistrictName())) {
			return finNicDistrictQurey(finDetOpt.get().getDistrict(),finDetOpt.get().getState());
		}
		return 0;
	}
	
	private String setFincStateCode(String stateName) {
		Optional<StateDTO> stateOpt = stateCodeQurey(stateName);
		if (stateOpt.isPresent() && StringUtils.isNotEmpty(stateOpt.get().getNicStateCode())) {
			return stateOpt.get().getNicStateCode();
		}
		return StringUtils.EMPTY;
	}
	
	private Optional<StateDTO> stateCodeQurey(String stateName) {
		Optional<StateDTO> stateOpt = stateDAO.findByStateNameIgnoreCase(stateName);
		if (stateOpt.isPresent() && StringUtils.isNotEmpty(stateOpt.get().getNicStateCode())) {
			return stateOpt;
		}
		else {
			stateOpt = stateDAO.findByStateNameIgnoreCase(stateName.replaceAll(" ", ""));
			if (stateOpt.isPresent() && StringUtils.isNotEmpty(stateOpt.get().getNicStateCode())) {
			return stateOpt;
			}
		}
		return Optional.empty();
	}
	
	private Integer finNicDistrictQurey(DistrictDTO  districtDto,StateDTO stateDto) {
		if(stateDto!=null&&stateDto.getStateId()!=null) {
			Optional<VahanSyncDistrictMappingDTO> districtCodeOpt = districtMappingDAO
					.findByDistrictNameIgnoreCaseAndStateCode(districtDto.getDistrictName(),
							stateDto.getStateId());
			if (districtCodeOpt.isPresent() && districtCodeOpt.get().getDistrictCode() != null) {
				return districtCodeOpt.get().getDistrictCode();
			}		
		} else if (districtDto != null
				&& StringUtils.isNotBlank(districtDto.getDistrictName())) {
			List<VahanSyncDistrictMappingDTO> finDisList = districtMappingDAO
					.findByDistrictNameIgnoreCase(districtDto.getDistrictName());
			if (!finDisList.isEmpty() && finDisList.size() == 1) {
				return finDisList.get(0).getDistrictCode();
			}
		}
		return 0;
	}
}
