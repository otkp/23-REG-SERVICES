package org.epragati.permits.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.constants.CovCategory;
import org.epragati.constants.OwnerTypeEnum;
import org.epragati.constants.TransferType;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.FcDetailsDAO;
import org.epragati.master.dao.RegServiceDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.TaxDetailsDAO;
import org.epragati.master.dto.FcDetailsDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.master.dto.VehicleDetailsDTO;
import org.epragati.master.service.PermitsService;
import org.epragati.payments.vo.TransactionDetailVO;
import org.epragati.permits.dao.DeathPrNoDAO;
import org.epragati.permits.dao.OfficeWisePermitsAvilabilityDAO;
import org.epragati.permits.dao.PermitDetailsDAO;
import org.epragati.permits.dao.PermitMandalExemptionDAO;
import org.epragati.permits.dao.PermitValidationsDAO;
import org.epragati.permits.dao.PermitVehicleMappingDAO;
import org.epragati.permits.dao.StateWisePermitsAvailabilityDAO;
import org.epragati.permits.dto.CovValidationDTO;
import org.epragati.permits.dto.DeathPrNoDTO;
import org.epragati.permits.dto.OfficeWisePermitsAvilabilityDTO;
import org.epragati.permits.dto.PermitDetailsDTO;
import org.epragati.permits.dto.PermitMandalExemptionDTO;
import org.epragati.permits.dto.PermitValidationsDTO;
import org.epragati.permits.dto.PermitValidityDetailsDTO;
import org.epragati.permits.dto.PermitVehicleMappingDTO;
import org.epragati.permits.dto.StateWisePermitsAvailabilityDTO;
import org.epragati.permits.service.PermitValidationsService;
import org.epragati.regservice.CitizenTaxService;
import org.epragati.regservice.RegistrationService;
import org.epragati.regservice.dto.CitizenFeeDetailsInput;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.vo.RcValidationVO;
import org.epragati.regservice.vo.RegServiceVO;
import org.epragati.util.PermitsEnum;
import org.epragati.util.PermitsEnum.PermitType;
import org.epragati.util.Status.permitSuspCanRevStatus;
import org.epragati.util.StatusRegistration;
import org.epragati.util.payment.ClassOfVehicleEnum;
import org.epragati.util.payment.ServiceCodeEnum;
import org.epragati.util.payment.ServiceEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PermitValidationsServiceImpl implements PermitValidationsService {

	private static final Logger logger = LoggerFactory.getLogger(PermitValidationsServiceImpl.class);

	@Autowired
	private PermitValidationsDAO permitValidationsDAO;

	@Autowired
	private FcDetailsDAO fcDetailsDAO;

	@Autowired
	private PermitsService permitsService;

	@Autowired
	private OfficeWisePermitsAvilabilityDAO officeWisePermitsAvilabilityDAO;

	@Autowired
	private PermitDetailsDAO permitDetailsDAO;

	@Autowired
	private RegServiceDAO regServiceDAO;

	@Autowired
	private TaxDetailsDAO taxDetailsDAO;

	@Autowired
	private PermitMandalExemptionDAO permitMandalExemptionDAO;

	@Autowired
	private RegistrationService registrationService;

	@Autowired
	private StateWisePermitsAvailabilityDAO stateWisePermitsAvailabilityDAO;

	@Autowired
	private DeathPrNoDAO deathPrNoDAO;

	@Autowired
	private PermitVehicleMappingDAO permitVehicleMappingDAO;
	
	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;
	
	@Autowired
	private CitizenTaxService citizenTaxService;

	@Override
	public void doPermitValidations(RcValidationVO rcValidationVO, RegistrationDetailsDTO registrationDetailsDTO) {
		String cov = null;

		if (StringUtils.isNotBlank(cov)) {
			cov = registrationDetailsDTO.getClassOfVehicle();
		} else {
			cov = registrationDetailsDTO.getVehicleDetails().getClassOfVehicle();
		}

		Optional<PermitValidationsDTO> validationDTO = permitValidationsDAO.findByCovListIn(cov);

		if (!validationDTO.isPresent()) {
			throw new BadRequestException(cov + " Class of vehicle not eligible for permit");
		}

		if (validationDTO.isPresent() && validationDTO.get().getValidations() == null) {
			throw new BadRequestException(cov + " Class of vehicle not eligible for permit");
		}

		if (registrationDetailsDTO.getVehicleType().equalsIgnoreCase(CovCategory.N.getCode())) {
			logger.error("Permits are not allowed for Non-Transpot vehicles with this PR.No:[{}]",
					registrationDetailsDTO.getPrNo());
			throw new BadRequestException("Permits are not allowed for Non-Transpot vehicles");
		}
		permitTOPendingCheck(rcValidationVO.getPrNo());
		checkForSuspendRecords(rcValidationVO);

		if (rcValidationVO.getPermitClassVO() != null
				|| rcValidationVO.getServiceIds().stream().anyMatch(val -> ServiceEnum.getRLServices().contains(val))) {
			if (StringUtils.isNotBlank(rcValidationVO.getChassisNo())) {
				if (null != rcValidationVO.getChassisNo() && !rcValidationVO.getChassisNo()
						.equalsIgnoreCase(registrationDetailsDTO.getVahanDetails().getChassisNumber())) {
					throw new BadRequestException("please enter correct Chassis Number");
				}
			}
			if (rcValidationVO.getPermitClassVO() != null && rcValidationVO.getPermitClassVO().getCode()
					.equalsIgnoreCase(PermitType.TEMPORARY.getPermitTypeCode())) {
				checkForPaccaPermitDetailsForTemporaryPermit(rcValidationVO);
			}
		}

		Optional<PermitValidationsDTO> permitValidationsOpt = permitValidationsDAO.findByCovListIn(cov);
		if (permitValidationsOpt.isPresent()) {
			VehicleDetailsDTO vehicleDetailsDTO = registrationDetailsDTO.getVehicleDetails();
			checkValidity(registrationDetailsDTO, rcValidationVO);
			CovValidationDTO covValidations = permitValidationsOpt.get().getValidations();
			if (covValidations != null && covValidations.getWeight() != null) {
				performWeightValidationsForPermits(covValidations, vehicleDetailsDTO, cov);
			} else if (covValidations != null && covValidations.getSeatingCapacityFrom() != null) {
				performSeatingCapacityValidationsForPermits(covValidations, vehicleDetailsDTO);
			} else if (covValidations != null && covValidations.getOwnerType() != null) {
				performOwnerTypeValidationsForPermits(covValidations, registrationDetailsDTO);
			}
		}
		if (null != rcValidationVO.getServiceIds() && rcValidationVO.getServiceIds().stream()
				.anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFPERMIT.getId()))) {

			permitTransferValidations(registrationDetailsDTO, rcValidationVO);

		}
		if (null != rcValidationVO.getServiceIds()
				&& rcValidationVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.PERMITCOA.getId()))) {

			permitChangeOfAddress(registrationDetailsDTO);
		}

		if (null != rcValidationVO.getServiceIds() && rcValidationVO.getServiceIds().stream()
				.anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFRECOMMENDATIONLETTER.getId()))) {

			permitRecommendationLetterTransferValidations(registrationDetailsDTO, rcValidationVO);

		}

		if (null != rcValidationVO.getServiceIds() && rcValidationVO.getServiceIds().stream()
				.anyMatch(id -> id.equals(ServiceEnum.CHANGEOFADDRESSOFRECOMMENDATIONLETTER.getId()))) {

			permitRecommendationLetterCOAValidations(registrationDetailsDTO, rcValidationVO);

		}

		if (rcValidationVO.getServiceIds().stream().anyMatch(val -> ServiceEnum.getRLServices().contains(val))) {
			List<PermitVehicleMappingDTO> vehicleMappingList = permitVehicleMappingDAO
					.findByPermitTypeAndStatusTrue("CSP");
			List<String> covs = vehicleMappingList.stream().map(val -> val.getCov()).collect(Collectors.toList());
			if (!covs.contains(cov)) {
				throw new BadRequestException("This Class of Vehicle" + cov + " is not eligible to take"
						+ ServiceEnum.RENEWALOFRECOMMENDATIONLETTER.getDesc());
			}
			checkForPaccaPermitDetailsForTemporaryPermit(rcValidationVO);
		}
		checkForVehicleWiseValidations(registrationDetailsDTO, rcValidationVO);
		checkForPaccaPermitDetailsStatusValidtion(rcValidationVO);
	}
	
	private void permitRecommendationLetterCOAValidations(RegistrationDetailsDTO registrationDetailsDTO,
			RcValidationVO rcValidationVO) {
		RegServiceDTO regService = getLatestRecord(registrationDetailsDTO.getPrNo(), ServiceEnum.CHANGEOFADDRESS.getId());

		validateSameOffice(registrationDetailsDTO, regService);
		List<PermitDetailsDTO> recommendationLetterList = permitsService.fetchRecommendationLetterDetails(registrationDetailsDTO.getPrNo());
		
		recommendationLetterList.stream().forEach(val -> {
			validateOfficeOnRegistrationAndPermitDetails(registrationDetailsDTO, val.getRdto());
		});
		
		
	}

	/**
	 * Doing recommendation letter details validation
	 * @param regDetails
	 * @param rcValidationVO
	 */

	private void permitRecommendationLetterTransferValidations(RegistrationDetailsDTO regDetails,
			RcValidationVO rcValidationVO) {

		RegServiceDTO regService = permitTOPendingCheck(rcValidationVO.getPrNo());
		
		if (regService == null) {
			throw new BadRequestException(
					"Transfer of ownership completed application only allowed for Permit Transfer  : "
							+ regDetails.getPrNo());
		}
		
		List<PermitDetailsDTO> recommendationLetterList = permitsService.fetchRecommendationLetterDetails(regDetails.getPrNo());
		
		recommendationLetterList.stream().forEach(permitDetailsDTO -> {
			Boolean isNotDeath = Boolean.FALSE;
			if (regService.getBuyerDetails() != null
					&& TransferType.DEATH.equals(regService.getBuyerDetails().getTransferType())) {
				Optional<DeathPrNoDTO> deathPr = deathPrNoDAO.findByPrNoAndStatus(rcValidationVO.getPrNo(), isNotDeath);
				if (deathPr.isPresent()) {
					isNotDeath = Boolean.TRUE;
				}
			}
			if (!isNotDeath) {
				if (permitDetailsDTO.getRdto() != null
						&& permitDetailsDTO.getRdto().getApplicantDetails() != null
						&& permitDetailsDTO.getRdto().getApplicantDetails().getAadharNo() != null) {
					if (regDetails.getApplicantDetails() != null && regDetails.getApplicantDetails().getAadharNo() != null
							&& permitDetailsDTO.getRdto().getApplicantDetails().getAadharNo()
									.equals(regDetails.getApplicantDetails().getAadharNo())) {
						throw new BadRequestException(
								"Permit Transfer Already Done on this prNo : " + regDetails.getPrNo() + " by Buyer");
					}
					if (!permitDetailsDTO.getRdto().getApplicantDetails().getAadharNo()
							.equals(rcValidationVO.getAadharNo())) {
						throw new BadRequestException("Only Seller has the provision to apply Permit Transfer Service");
					}
				}
			}

			if (null == regService.getBuyerDetails()) {
				throw new BadRequestException("Buyer details not available");
			}
			if (!regService.getBuyerDetails().getBuyerAadhaarNo().equals(regDetails.getApplicantDetails().getAadharNo())) {
				throw new BadRequestException("Please give correct Aadhaar Number : " + regDetails.getPrNo());
			}
			validateSameOffice(regDetails, regService);
			
		});
		
	}

	private void checkForSuspendRecords(RcValidationVO rcValidationVO) {
		List<String> statusList = new ArrayList<>();
		statusList.add(permitSuspCanRevStatus.SUSPEND.getStatus());
		statusList.add(permitSuspCanRevStatus.INITIATED.getStatus());
		Optional<PermitDetailsDTO> dto = permitDetailsDAO.findByPermitStatusInAndPrNo(statusList, rcValidationVO.PrNo);
		if (dto.isPresent()) {
			throw new BadRequestException("This PR no is not eligible to take permit services");
		}
	}

	/**
	 * This method is used to check for the
	 * 
	 * @param registrationDetailsDTO
	 * @param permitValidationsOpt
	 */
	private void checkForVehicleWiseValidations(RegistrationDetailsDTO registrationDetailsDTO,
			RcValidationVO rcValidationVO) {

		LocalDateTime date = registrationDetailsDTO.getPrGeneratedDate();

		if (date == null) {
			throw new BadRequestException(
					"Vehicle validity details are not present with this RC no [ " + rcValidationVO.getPrNo() + "]");
		}

		if (registrationDetailsDTO.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.EIBT.getCovCode())) {
			if (date.toLocalDate().plusYears(15).isBefore(LocalDate.now())&& !rcValidationVO.getServiceIds().stream().anyMatch(id -> 
			id.equals(ServiceEnum.SURRENDEROFPERMIT.getId()))) {
				throw new BadRequestException(
						"Age of the vehicle is more than 15 years, " + ClassOfVehicleEnum.EIBT.getCovCode()
								+ " not eligeble to take permit: " + rcValidationVO.getPrNo());
			}
		}

		if (registrationDetailsDTO.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())
				&& !registrationDetailsDTO.getVahanDetails().getFuelDesc().equalsIgnoreCase("CNG")
				&& null != rcValidationVO.getServiceIds()
				&& rcValidationVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWPERMIT.getId()))) {
			// verifyPermitsisAvilableForOffice(registrationDetailsDTO.getOfficeDetails().getOfficeCode());
			verifyPermitsisAvilableForMandal(
					registrationDetailsDTO.getApplicantDetails().getPresentAddress().getMandal().getMandalCode(),
					rcValidationVO.getPrNo());
		}
	}

	/**
	 * Mandal wise permits availability check for ARKT vehicles
	 * 
	 * @param MandalCode
	 */
	private void verifyPermitsisAvilableForMandal(Integer mandalCode, String prNo) {
		if (mandalCode == null) {
			throw new BadRequestException("We are unable to fetch the madal details with this RC no +[ " + prNo + "]");
		}
		Optional<PermitMandalExemptionDTO> dto = permitMandalExemptionDAO.findByMandalCodeAndStatusTrue(mandalCode);
		if (dto.isPresent()) {
			throw new BadRequestException("Mandal Details Not Found For ARKT permit with RTA Office Region +[ "
					+ dto.get().getMandalName() + "]");
		}
	}

	/**
	 * Office wise permits availability check for ARKT vehicles
	 * 
	 * @param officeCode
	 */
	private void verifyPermitsisAvilableForOffice(String officeCode) {
		Optional<OfficeWisePermitsAvilabilityDTO> dto = officeWisePermitsAvilabilityDAO
				.findByOfficeCodeAndStatusTrue(officeCode);
		if (!dto.isPresent()) {
			logger.error("Office details not found for found for ARKT permits with this office code , [{}]",
					officeCode);
			throw new BadRequestException("Office is not present to issue permits +[ " + officeCode + "]");
		}
		Integer avilablePermits = dto.get().getRemainingPermits();
		if (avilablePermits <= 0) {
			logger.error("Permits limit is exceeded at this office for ARKT cov with this office code , [{}]",
					officeCode);
			throw new BadRequestException("Permits limit is exceeded at this office +[ " + officeCode + "]");
		}
	}

	/**
	 * To perform weight validations
	 * 
	 * @param covValidations
	 * @param vehicleDetailsDTO
	 * @param cov
	 */
	private void performWeightValidationsForPermits(CovValidationDTO covValidations,
			VehicleDetailsDTO vehicleDetailsDTO, String cov) {
		if (vehicleDetailsDTO.getRlw() < covValidations.getWeight()) {
			logger.error("Weight should be greater than 3000 for cov [{}]", cov);
			throw new BadRequestException("Weight should be greater than 3000 for cov [" + cov + " ]");
		}
	}

	/**
	 * To perform seating capacity validations
	 * 
	 * @param covValidations
	 * @param vehicleDetailsDTO
	 */

	private void performSeatingCapacityValidationsForPermits(CovValidationDTO covValidations,
			VehicleDetailsDTO vehicleDetailsDTO) {
		if (Integer.valueOf(vehicleDetailsDTO.getSeatingCapacity()) < covValidations.getSeatingCapacityFrom()
				&& Integer.valueOf(vehicleDetailsDTO.getSeatingCapacity()) > covValidations.getSeatingCapacityFrom()) {
			logger.error("Seating capacity should be B/W ,[{}] and [{}] ", covValidations.getSeatingCapacityFrom(),
					covValidations.getSeatingCapacityTo());
			throw new BadRequestException("Seating capacity should be B/w " + covValidations.getSeatingCapacityFrom()
					+ "and " + covValidations.getSeatingCapacityTo());
		}
	}

	/**
	 * To perform owner type validations
	 * 
	 * @param covValidations
	 * @param registrationDetailsDTO
	 */
	private void performOwnerTypeValidationsForPermits(CovValidationDTO covValidations,
			RegistrationDetailsDTO registrationDetailsDTO) {
		List<OwnerTypeEnum> ownerTypeList = covValidations.getOwnerType();
		boolean verifyOwner = ownerTypeList.stream()
				.anyMatch(type -> type.equals(registrationDetailsDTO.getOwnerType()));
		if (!verifyOwner) {
			logger.error("Owner type is not allowed to get the permit, [{}]", registrationDetailsDTO.getOwnerType());
			throw new BadRequestException(
					"Owner type is not allowed to get the permit [" + registrationDetailsDTO.getOwnerType() + "]");
		}

	}

	/**
	 * This method is used to check for the PUCCA permit exists or not to take
	 * TEMP permit
	 * 
	 * @param rcValidationVO
	 */
	private void checkForPaccaPermitDetailsForTemporaryPermit(RcValidationVO rcValidationVO) {

		Optional<PermitDetailsDTO> dto = permitDetailsDAO.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(
				rcValidationVO.getPrNo(), PermitType.PRIMARY.getPermitTypeCode(), PermitsEnum.ACTIVE.getDescription());
		if (!dto.isPresent()) {
			throw new BadRequestException(
					"You are not eligible to apply for the Temporary Permit , Pucca Permit Details are Not Found with this PR no "
							+ rcValidationVO.getPrNo());
		}

		if (LocalDate.now().isAfter(dto.get().getPermitValidityDetails().getPermitValidTo())) {
			throw new BadRequestException("Your permit is expaired " + rcValidationVO.getPrNo());
		}
	}

	/**
	 * This method is used to check for the PUCCA permit active or not
	 * 
	 * checkForPaccaPermitDetailsStatusValidtion
	 * 
	 * @param rcValidationVO
	 */
	private void checkForPaccaPermitDetailsStatusValidtion(RcValidationVO rcValidationVO) {

		if (rcValidationVO.getPermitClassVO() == null
				&& rcValidationVO.getServiceIds().contains(ServiceEnum.SURRENDEROFPERMIT.getId())) {
			permitTOPendingCheck(rcValidationVO.getPrNo());
			permitsService.findPermitInactiveRecords(rcValidationVO.getPrNo());
		} else if (rcValidationVO.getPermitClassVO() != null
				&& (rcValidationVO.getServiceIds().contains(ServiceEnum.SURRENDEROFPERMIT.getId())
						|| rcValidationVO.getServiceIds().contains(ServiceEnum.EXTENSIONOFVALIDITY.getId()))
				&& rcValidationVO.getPermitClassVO().getCode().contains(PermitType.TEMPORARY.getPermitTypeCode())) {
			permitsService.findTemporayPermitInactiveRecords(rcValidationVO.getPrNo());
		}
	}

	/**
	 * This is used to validate the dates and FC and Tax expiration details
	 * 
	 * @param regDTO
	 * @param rcValidationVO
	 */
	public void checkValidity(RegistrationDetailsDTO regDTO, RcValidationVO rcValidationVO) {
		Optional<TaxDetailsDTO> taxDto = null;
		performPermitValidityCheckForPuccaAndTemporaryPermit(rcValidationVO);
		taxDto = registrationService.getLatestTaxTransaction(rcValidationVO.getPrNo());
		VehicleDetailsDTO vehicleDetailsDTO = regDTO.getVehicleDetails();
		List<FcDetailsDTO> fcDetailsOpt = fcDetailsDAO
				.findFirst5ByStatusIsTrueAndPrNoOrderByCreatedDateDesc(regDTO.getPrNo());
		boolean skipFc= Boolean.FALSE;
		if(StringUtils.isNoneBlank(regDTO.getClassOfVehicle())) {
				if(regDTO.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.EIBT.getCovCode())&& rcValidationVO.getServiceIds().stream().anyMatch(id -> 
				id.equals(ServiceEnum.SURRENDEROFPERMIT.getId()))) {
					double vehicleAge = 0d;
					if(regDTO.getPrGeneratedDate()!=null) {
						 vehicleAge = citizenTaxService.calculateAgeOfTheVehicle(regDTO.getPrGeneratedDate().toLocalDate(),
								LocalDate.now());
					}else if(regDTO.getRegistrationValidity()!=null &&  regDTO.getRegistrationValidity().getPrGeneratedDate()!=null) {
						 vehicleAge = citizenTaxService.calculateAgeOfTheVehicle(regDTO.getRegistrationValidity().getPrGeneratedDate(),
								LocalDate.now());
					}
					if(vehicleAge > 15) {
						skipFc= Boolean.TRUE;
					}
				}
		}
		if(!skipFc) {
		if (fcDetailsOpt.isEmpty()) {
			throw new BadRequestException(
					"FC details not found for application No [ " + regDTO.getApplicationNo() + " ]");
		}
		fcDetailsOpt.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		FcDetailsDTO fcDetails = fcDetailsOpt.stream().findFirst().get();
		if (!LocalDate.now().isBefore(fcDetails.getFcValidUpto().plusDays(1))) {
			throw new BadRequestException("Fc Validity expired [ " + fcDetails.getFcValidUpto() + " ]");
		}
		}
		// Commented as per the discussion Srinivas sir
		if (rcValidationVO.getPermitClassVO() != null
				|| rcValidationVO.getServiceIds().stream().anyMatch(val -> ServiceEnum.getRLServices().contains(val))) {

			if (!taxDto.isPresent()) {
				throw new BadRequestException(
						"Tax Details are not found for PR no [" + rcValidationVO.getPrNo() + " ]");
			}

			if (!LocalDate.now().isBefore(taxDto.get().getTaxPeriodEnd().plusMonths(1))) {
				throw new BadRequestException("Tax Validity expired [ " + taxDto.get().getTaxPeriodEnd() + " ]");
			}
		}

		if (vehicleDetailsDTO.getUlw() == null || vehicleDetailsDTO.getRlw() == null) {
			throw new BadRequestException(
					"UlW and RLW are mandatory for applicationNo [" + regDTO.getApplicationNo() + " ]");
		}
		if (vehicleDetailsDTO.getUlw() != null && vehicleDetailsDTO.getRlw() == null
				&& vehicleDetailsDTO.getRlw() <= vehicleDetailsDTO.getUlw()) {
			throw new BadRequestException("GVW should be higher than ULW [" + regDTO.getApplicationNo() + " ]");
		}
	}

	private void performPermitValidityCheckForPuccaAndTemporaryPermit(RcValidationVO rcValidationVO) {
		Optional<PermitDetailsDTO> permitDetailsDTO = Optional.empty();
		String prNo = rcValidationVO.getPrNo();
		Long transportValidDays = 0l;
		List<Integer> serviceEnum = new ArrayList<Integer>();
		serviceEnum.add(ServiceEnum.NEWPERMIT.getId());
		if (!serviceEnum.stream().anyMatch(id -> rcValidationVO.getServiceIds().contains(id))) {
			if (!StringUtils.isEmpty(rcValidationVO.getPrNo())) {
				permitDetailsDTO = getPermitDetails(prNo);
			}
			if (!permitDetailsDTO.isPresent()
					&& !(rcValidationVO.getServiceIds().contains(ServiceEnum.VARIATIONOFPERMIT.getId()))
					&& !(rcValidationVO.getServiceIds().contains(ServiceEnum.SURRENDEROFPERMIT.getId()))
					&& !(rcValidationVO.getServiceIds().contains(ServiceEnum.PERDATAENTRY.getId()))) {
				throw new BadRequestException("permit Details Not Found for prNo " + prNo);
			}

			if (rcValidationVO.getServiceIds().contains(ServiceEnum.VARIATIONOFPERMIT.getId())) {
				permitVariationValidation(rcValidationVO);
			}
			if (rcValidationVO.getServiceIds().contains(ServiceEnum.PERDATAENTRY.getId())) {
				PermitDataentryValidation(rcValidationVO);
			}
			if (rcValidationVO.getServiceIds().contains(ServiceEnum.REPLACEMENTOFVEHICLE.getId())) {
				permitRepacementValidation(rcValidationVO);
			}

			if (rcValidationVO.getServiceIds().stream()
					.anyMatch(id -> id.equals(ServiceEnum.RENEWALOFPERMIT.getId()))) {
				transportValidDays = ChronoUnit.DAYS.between(LocalDate.now().minusDays(1),
						permitDetailsDTO.get().getPermitValidityDetails().getPermitValidTo().plusDays(1));
				if (transportValidDays >= 30 && !permitDetailsDTO.get().getPermitType().getPermitType().equalsIgnoreCase("NP")) {
					throw new BadRequestException(
							"You are not eligible to apply for Renewal Of Permit, Permit validity not expired");
				}
				
				else if (transportValidDays >= 90 && permitDetailsDTO.get().getPermitType().getPermitType().equalsIgnoreCase("NP")) {
					throw new BadRequestException(
							"You are not eligible to apply for Renewal Of Permit, Permit validity not expired");
				}
			}

			if (rcValidationVO.getServiceIds().stream()
					.anyMatch(id -> id.equals(ServiceEnum.RENEWALOFAUTHCARD.getId()))) {
				String permitType = permitDetailsDTO.get().getPermitType().getPermitType();
				if (permitType.equals("AITP") || permitType.equals("AITC") || permitType.equals("NP")) {
					if (permitDetailsDTO.get().getPermitValidityDetails().getPermitValidTo()
							.isBefore(LocalDate.now())) {
						throw new BadRequestException("Permit Validity is Expired");
					}

					transportValidDays = ChronoUnit.DAYS.between(LocalDate.now().minusDays(1), permitDetailsDTO.get()
							.getPermitValidityDetails().getPermitAuthorizationValidTo().plusDays(1));
					if (transportValidDays > 30
							&& !permitDetailsDTO.get().getPermitType().getPermitType().equalsIgnoreCase("NP")) {
						throw new BadRequestException(
								"You are not eligible to apply for Permit Renewal Authorization, Permit Authorization validity not expired");
					}
				} else {
					throw new BadRequestException(
							"Your permit type(" + permitType + ") is not eligible for Authorization");
				}
			}

			if (rcValidationVO.getServiceIds().stream()
					.anyMatch(id -> id.equals(ServiceEnum.RENEWALOFRECOMMENDATIONLETTER.getId()))) {
				transportValidDays = ChronoUnit.DAYS.between(LocalDate.now().minusDays(1),
						permitDetailsDTO.get().getPermitValidityDetails().getPermitValidTo().plusDays(1));
				if (transportValidDays >= 15) {
					throw new BadRequestException(
							"You are not eligible to apply for Renewal of Recommendation Letter, RL validity not expired");
				}
			} 

			else {
				/* Validation for get valid temporary record */
				if (!rcValidationVO.getServiceIds().stream()
						.anyMatch(id -> id.equals(ServiceEnum.SURRENDEROFPERMIT.getId()))) {
					permitDetailsDTO = permitDetailsDAO.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(prNo,
							PermitType.TEMPORARY.getPermitTypeCode(), PermitsEnum.ACTIVE.getDescription());
				}
			}
		} else

		{
			permitDetailsDTO = rcValidationVO.getPermitClassVO() != null
					? permitDetailsDAO.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(prNo,
							PermitType.TEMPORARY.getPermitTypeCode(), PermitsEnum.ACTIVE.getDescription())
					: permitDetailsDAO.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(prNo,
							PermitType.PRIMARY.getPermitTypeCode(), PermitsEnum.ACTIVE.getDescription());
			if (null != rcValidationVO.getServiceIds()) {
				if (!rcValidationVO.getServiceIds().stream()
						.anyMatch(id -> ServiceEnum.getPermitRelatedServiceIdsValidations().contains(id))) {

					if (rcValidationVO.getPermitClassVO() == null && permitDetailsDTO.isPresent()) {
						if (LocalDate.now()
								.isBefore(permitDetailsDTO.get().getPermitValidityDetails().getPermitValidTo())) {
							throw new BadRequestException("Please cancel or surrender your permit");
						}
						if (permitDetailsDTO.isPresent() && permitDetailsDTO.get().getPermitClass().getDescription()
								.equalsIgnoreCase(PermitType.PRIMARY.getDescription())) {
							throw new BadRequestException("Please cancel or surrender your permit");
						}
					}
				}
			}
		}

	}

	private void permitTransferValidations(RegistrationDetailsDTO regDetails, RcValidationVO rcValidationVO) {

		RegServiceDTO regService = permitTOPendingCheck(rcValidationVO.getPrNo());
		if (regService == null) {
			throw new BadRequestException(
					"Transfer of ownership completed application only allowed for Permit Transfer  : "
							+ regDetails.getPrNo());
		}
		Optional<PermitDetailsDTO> permitDetailsDTO = getPermitDetails(regDetails.getPrNo());
		if (!permitDetailsDTO.isPresent()) {
			throw new BadRequestException(
					"Permite is InActive (or) not availablr for this PRNo: " + regDetails.getPrNo());
		}
		Boolean isNotDeath = Boolean.FALSE;
		if (regService.getBuyerDetails() != null
				&& TransferType.DEATH.equals(regService.getBuyerDetails().getTransferType())) {
			Optional<DeathPrNoDTO> deathPr = deathPrNoDAO.findByPrNoAndStatus(rcValidationVO.getPrNo(), isNotDeath);
			if (deathPr.isPresent()) {
				isNotDeath = Boolean.TRUE;
				/*deathPr.get().setStatus(Boolean.TRUE);
				deathPrNoDAO.save(deathPr.get());*/
			}
		}
		if (!isNotDeath) {
			if (permitDetailsDTO.get().getRdto() != null
					&& permitDetailsDTO.get().getRdto().getApplicantDetails() != null
					&& permitDetailsDTO.get().getRdto().getApplicantDetails().getAadharNo() != null) {
				if (regDetails.getApplicantDetails() != null && regDetails.getApplicantDetails().getAadharNo() != null
						&& permitDetailsDTO.get().getRdto().getApplicantDetails().getAadharNo()
								.equals(regDetails.getApplicantDetails().getAadharNo())) {
					throw new BadRequestException(
							"Permit Transfer Already Done on this prNo : " + regDetails.getPrNo() + " by Buyer");
				}
				if (!permitDetailsDTO.get().getRdto().getApplicantDetails().getAadharNo()
						.equals(rcValidationVO.getAadharNo())) {
					throw new BadRequestException("Only Seller has the provision to apply Permit Transfer Service");
				}
			}
		}

		if (null == regService.getBuyerDetails()) {
			throw new BadRequestException("Buyer details not available");
		}
		if (!regService.getBuyerDetails().getBuyerAadhaarNo().equals(regDetails.getApplicantDetails().getAadharNo())) {
			throw new BadRequestException("Please give correct Aadhaar Number : " + regDetails.getPrNo());
		}
		validateSameOffice(regDetails, regService);
	}

	private void permitChangeOfAddress(RegistrationDetailsDTO regDetails) {
		RegServiceDTO regService = getLatestRecord(regDetails.getPrNo(), ServiceEnum.CHANGEOFADDRESS.getId());

		validateSameOffice(regDetails, regService);
		getPermitDetails(regDetails.getPrNo());
	}

	private void validateSameOffice(RegistrationDetailsDTO regDetails, RegServiceDTO regService) {
		if (!regService.getOfficeCode().equals(regDetails.getOfficeDetails().getOfficeCode()) || !regService
				.getOfficeCode().equals(regService.getRegistrationDetails().getOfficeDetails().getOfficeCode())) {
			throw new BadRequestException("With in the office only permit transfer/COA service available");
		}
	}
	
	private void validateOfficeOnRegistrationAndPermitDetails(RegistrationDetailsDTO regDetails, RegistrationDetailsDTO permitRegistrationDetails) {
		if (!permitRegistrationDetails.getOfficeDetails().getOfficeCode().equals(regDetails.getOfficeDetails().getOfficeCode())) {
			throw new BadRequestException("With in the office only permit transfer/COA service available");
		}
	}

	private RegServiceDTO getLatestRecord(String prNo, Integer serviceId) {
		ServiceEnum serviceType = ServiceEnum.getServiceEnumById(serviceId);
		List<RegServiceDTO> regServiceDTO = regServiceDAO.findByPrNoAndServiceTypeIn(prNo, Arrays.asList(serviceType));
		if (CollectionUtils.isEmpty(regServiceDTO)) {
			throw new BadRequestException(
					"Citizen not done " + ServiceEnum.getServiceEnumById(serviceId) + " service on PrNo : " + prNo);
		}
		regServiceDTO.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));

		RegServiceDTO regService = regServiceDTO.stream().findFirst().get();
		return regService;
	}

	@Override
	public Optional<PermitDetailsDTO> getPermitDetails(String prNo) {
		return permitDetailsDAO.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(prNo,
				PermitType.PRIMARY.getPermitTypeCode(), PermitsEnum.ACTIVE.getDescription());
	}

	private Optional<PermitDetailsDTO> getNewPermitDetailsByPermitNo(String permitNo) {
		return permitDetailsDAO.findByPermitNoAndServiceIdsInAndPermitTypeTypeofPermit(permitNo,
				ServiceEnum.NEWPERMIT.getId(), PermitType.PRIMARY.getPermitTypeCode());
	}

	public void permitVariationValidation(RcValidationVO rcValidationVO) {
		Optional<PermitDetailsDTO> permitDetialsOptional = Optional.empty();
		if (!StringUtils.isEmpty(rcValidationVO.getPrNo())) {
			permitDetialsOptional = getPermitDetails(rcValidationVO.getPrNo());
		} else if (!StringUtils.isEmpty(rcValidationVO.getPermitNo())) {
			permitDetialsOptional = getNewPermitDetailsByPermitNo(rcValidationVO.getPermitNo());
		}
		if (!permitDetialsOptional.isPresent()) {
			throw new BadRequestException(
					"new permit Details not found for permitNo [{}]" + rcValidationVO.getPermitNo());
		}
		PermitDetailsDTO permitDTO = permitDetialsOptional.get();
		if (permitDTO.getPermitValidityDetails().getPermitValidTo().isBefore(LocalDate.now())) {
			throw new BadRequestException("New Permit Validity is Expired");

		}
	}

	@Override
	public RegServiceDTO permitTOPendingCheck(String prNo) {
		RegServiceDTO regService = getLatestPendingRecord(prNo, ServiceEnum.TRANSFEROFOWNERSHIP.getId());
		List<StatusRegistration> status = new ArrayList<>();
		status.add(StatusRegistration.CANCELED);
		status.add(StatusRegistration.APPROVED);
		if (null != regService && null != regService.getApplicationStatus()
				&& !status.contains(regService.getApplicationStatus())) {
			logger.error("TOW service is pending on this application :[{}]",regService.getApplicationNo());
			throw new BadRequestException(
					"TOW service is pending on this application : " + regService.getApplicationNo());
		}
		return regService;
	}

	private RegServiceDTO getLatestPendingRecord(String prNo, Integer serviceId) {

		ServiceEnum serviceType = ServiceEnum.getServiceEnumById(serviceId);
		List<RegServiceDTO> regServiceDTO = regServiceDAO.findByPrNoAndServiceTypeIn(prNo, Arrays.asList(serviceType));
		if (CollectionUtils.isEmpty(regServiceDTO)) {
			return null;
		}
		regServiceDTO.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));

		RegServiceDTO regService = regServiceDTO.stream().findFirst().get();
		return regService;
	}

	@Override
	public Optional<TaxDetailsDTO> getLatestTaxTransaction(String prNo) {
		List<String> taxTypes = new ArrayList<>();
		taxTypes.add(ServiceCodeEnum.QLY_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.HALF_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.YEAR_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.LIFE_TAX.getCode());
		List<TaxDetailsDTO> taxsList = taxDetailsDAO.findFirst10ByPrNoAndPaymentPeriodInOrderByCreatedDateDesc(prNo,
				taxTypes);
		if (CollectionUtils.isNotEmpty(taxsList)) {
			TaxDetailsDTO taxDto = new TaxDetailsDTO();
			registrationService.updatePaidDateAsCreatedDate(taxsList);
			taxsList.sort((o1, o2) -> o2.getCreatedDate().compareTo(o1.getCreatedDate()));
			for (TaxDetailsDTO taxDetailsDTO : taxsList) {
				if (taxDetailsDTO.getTaxPeriodEnd() != null) {
					taxDto = taxDetailsDTO;
					break;
				}
			}
			taxsList.clear();
			return Optional.of(taxDto);

		}
		return Optional.empty();
	}

	private void permitRepacementValidation(RcValidationVO rcValidationVO) {
		Optional<PermitDetailsDTO> permitDetialsOptional = Optional.empty();
		Optional<PermitDetailsDTO> permitDetialsTempOptional = Optional.empty();
		PermitValidityDetailsDTO permitValidityDetailsDTO = new PermitValidityDetailsDTO();

		if (!StringUtils.isEmpty(rcValidationVO.getPrNo())) {
			permitDetialsOptional = permitDetailsDAO.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(
					rcValidationVO.getPrNo(), PermitType.PRIMARY.getPermitTypeCode(),
					PermitsEnum.ACTIVE.getDescription());
			permitDetialsTempOptional = permitDetailsDAO.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(
					rcValidationVO.getPrNo(), PermitType.TEMPORARY.getPermitTypeCode(),
					PermitsEnum.ACTIVE.getDescription());
			if (!permitDetialsOptional.isPresent()) {
				throw new BadRequestException("Permit Details Not Found  : " + rcValidationVO.getPrNo());
			}
			if (permitDetialsTempOptional.isPresent()) {
				throw new BadRequestException(
						"Please Surrender You are Temporary Permit  : " + rcValidationVO.getPrNo());
			}
			permitValidityDetailsDTO = permitDetialsOptional.get().getPermitValidityDetails();
			if (permitValidityDetailsDTO == null) {
				throw new BadRequestException("Permit validity Details Not Found  : " + rcValidationVO.getPrNo());

			}
			if (permitValidityDetailsDTO.getPermitValidTo() == null) {
				throw new BadRequestException("permit Validity Not found for prNo " + rcValidationVO.getPrNo());
			}
			LocalDate permitValidTo = permitValidityDetailsDTO.getPermitValidTo();
			LocalDate permitValidAuthTo = permitValidityDetailsDTO.getPermitAuthorizationValidTo();
			if (permitValidTo.isBefore(LocalDate.now())) {
				throw new BadRequestException("Permit validity is expired   : " + rcValidationVO.getPrNo());
			}
			if (permitValidAuthTo != null && permitValidAuthTo.isBefore(LocalDate.now())) {
				throw new BadRequestException(
						"permit authorization validity is expired   : " + rcValidationVO.getPrNo());
			}

		}

	}

	private void PermitDataentryValidation(RcValidationVO rcValidationVO) {
		Optional<PermitDetailsDTO> permitDetialsOptional = Optional.empty();
		if (!StringUtils.isEmpty(rcValidationVO.getPrNo())) {
			permitDetialsOptional = permitDetailsDAO.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(
					rcValidationVO.getPrNo(), PermitType.PRIMARY.getPermitTypeCode(),
					PermitsEnum.ACTIVE.getDescription());
			
			if (permitDetialsOptional.isPresent()) {
				throw new BadRequestException(
						"Permit Details Found , You are not eligible to apply for Permit Data Entry "
								+ rcValidationVO.getPrNo());
			}
			

		}
		/*Optional<PermitDetailsDTO> permitDetialsValidation = Optional.empty();
		if (!StringUtils.isEmpty(rcValidationVO.getPrNo())) {
			permitDetialsValidation = permitDetailsDAO.findByPrNoAndPermitNoAndPermitTypePermitType(
					rcValidationVO.getPrNo(),rcValidationVO.getPermitNo(), PermitType.PRIMARY.getPermitTypeCode());
			if (permitDetialsValidation.isPresent()) {
				throw new BadRequestException(
						"Permit Number already exists,You are not eligible to apply for Permit Data Entry"
								+ rcValidationVO.getPermitNo());
			}
		}*/
		
	}

	@Override
	public void validateIssueOfRecommendationLetterData(PermitDetailsDTO dto, RegServiceDTO regServiceDetails) {
		Optional<StateWisePermitsAvailabilityDTO> statesValidation = stateWisePermitsAvailabilityDAO
				.findByStateNameAndStatusTrueAndIsRecommendationTrue(dto.getRouteDetails().getState());
		if (statesValidation.isPresent() && statesValidation.get().getRemainingPermits() <= 0) {
			throw new BadRequestException("Limit Exceeded for "
					+ dto.getRouteDetails().getState() + " State");
		}
		Optional<PermitDetailsDTO> permitDetails = permitDetailsDAO
				.findByPrNoAndPermitTypePermitTypeAndPermitStatusAndRouteDetailsStateOrderByCreatedDateDesc(regServiceDetails.getPrNo(),
						dto.getPermitType().getPermitType(), PermitsEnum.ACTIVE.getDescription(), dto.getRouteDetails().getState());
		if (permitDetails.isPresent()) {
			throw new BadRequestException("Permit already exists with this PrNo " + dto.getPrNo() + " State "
					+ dto.getRouteDetails().getState());
		}

	}
	
	@Override
	public void permitvalidateAndSetHomeStateOrOtherState(CitizenFeeDetailsInput input,
			String prNo, TransactionDetailVO transactionDetailVO) {
		try {
			Optional<RegistrationDetailsDTO> regDetails = registrationDetailDAO.findByPrNo(prNo);
			Optional<TaxDetailsDTO> taxDto = null;
			if (regDetails.isPresent()) {
				taxDto = getLatestTaxTransaction(prNo);
				if (taxDto.isPresent()) {
					if (LocalDate.now().isBefore(taxDto.get().getTaxPeriodEnd().plusMonths(1))) {
						if (input != null) {
							input.setSkipTaxForTPSP(Boolean.TRUE);
						}
						if (transactionDetailVO != null) {
							transactionDetailVO.setSkipTaxForTPSP(Boolean.TRUE);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Exception occured while processing the request", e.getMessage());
		}
	}

	@Override
	public void validationForOtherStateSpecialPermit(RegServiceVO inputRegServiceVO) {
		if (inputRegServiceVO.getServiceIds().contains(ServiceEnum.OTHERSTATESPECIALPERMIT.getId())) {
			Optional<PermitValidationsDTO> validations = permitValidationsDAO.findByOtherStateSpecialPermitTrue();
			if (validations.isPresent()) {
				final String cov = inputRegServiceVO.getOtherStateTemporaryPermit().getVehicleDetails()
						.getClassOfVehicle();
				if (!validations.get().getCovList().contains(cov)) {
					throw new BadRequestException(cov + "is not eligible to apply for the SPECIAL PERMIT");
				}
			}
		}
	}

}
