package org.epragati.master.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.codec.binary.Base64;
import org.epragati.common.dao.PropertiesDAO;
import org.epragati.common.dto.PropertiesDTO;
import org.epragati.constants.TransferType;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.DistrictDAO;
import org.epragati.master.dao.FcDetailsDAO;
import org.epragati.master.dao.MasterCovDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.RegServiceDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dto.ApplicantAddressDTO;
import org.epragati.master.dto.ApplicantDetailsDTO;
import org.epragati.master.dto.DistrictDTO;
import org.epragati.master.dto.FcDetailsDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.master.dto.VahanDetailsDTO;
import org.epragati.master.mappers.DistrictMapper;
import org.epragati.master.mappers.MandalMapper;
import org.epragati.master.mappers.MasterCovMapper;
import org.epragati.master.mappers.PermitClassMapper;
import org.epragati.master.mappers.PermitDetailsMapper;
import org.epragati.master.mappers.PermitDistrictMappingMapper;
import org.epragati.master.mappers.PermitGoodsDetailsMapper;
import org.epragati.master.mappers.PermitRouteDetailsMapper;
import org.epragati.master.mappers.PermitTypeMapper;
import org.epragati.master.mappers.PermitValidationsMapper;
import org.epragati.master.mappers.StateMapper;
import org.epragati.master.service.PermitsService;
import org.epragati.master.vo.ApplicantAddressVO;
import org.epragati.master.vo.ApplicantDetailsVO;
import org.epragati.master.vo.DistrictVO;
import org.epragati.master.vo.FcDetailsVO;
import org.epragati.master.vo.InsuranceDetailsVO;
import org.epragati.master.vo.VahanVehicleDetailsVO;
import org.epragati.master.vo.VehicleDetailsVO;
import org.epragati.org.vahan.port.service.VahanSync;
import org.epragati.payment.dto.FeeDetailsDTO;
import org.epragati.payment.dto.PaymentTransactionDTO;
import org.epragati.payments.dao.PaymentTransactionDAO;
import org.epragati.payments.vo.TransactionDetailVO;
import org.epragati.permits.dao.DeathPrNoDAO;
import org.epragati.permits.dao.OfficeWisePermitsAvilabilityDAO;
import org.epragati.permits.dao.OtherStateTemporaryPermitDetailsDAO;
import org.epragati.permits.dao.PermitClassDAO;
import org.epragati.permits.dao.PermitDetailsDAO;
import org.epragati.permits.dao.PermitDistrictMappingDAO;
import org.epragati.permits.dao.PermitGoodsDetailsDAO;
import org.epragati.permits.dao.PermitRouteDetailsDAO;
import org.epragati.permits.dao.PermitRouteTypeDAO;
import org.epragati.permits.dao.PermitRoutesForSCRTDAO;
import org.epragati.permits.dao.PermitTypeDAO;
import org.epragati.permits.dao.PermitValidationsDAO;
import org.epragati.permits.dao.PermitVehicleMappingDAO;
import org.epragati.permits.dao.StateWisePermitsAvailabilityDAO;
import org.epragati.permits.dao.TemporaryPermitPassengerDetailsDAO;
import org.epragati.permits.dto.DeathPrNoDTO;
import org.epragati.permits.dto.OfficeWisePermitsAvilabilityDTO;
import org.epragati.permits.dto.OtherStateTemporaryPermitDetailsDTO;
import org.epragati.permits.dto.PermitClassDTO;
import org.epragati.permits.dto.PermitDetailsDTO;
import org.epragati.permits.dto.PermitDistrictMappingDTO;
import org.epragati.permits.dto.PermitGoodsDetailsDTO;
import org.epragati.permits.dto.PermitRouteDetailsDTO;
import org.epragati.permits.dto.PermitRouteTypeDTO;
import org.epragati.permits.dto.PermitRoutesForSCRTDTO;
import org.epragati.permits.dto.PermitTypeDTO;
import org.epragati.permits.dto.PermitValidationsDTO;
import org.epragati.permits.dto.PermitValidityDetailsDTO;
import org.epragati.permits.dto.PermitVehicleMappingDTO;
import org.epragati.permits.dto.StateWisePermitsAvailabilityDTO;
import org.epragati.permits.dto.TemporaryPermitPassengerDetailsDTO;
import org.epragati.permits.mappers.OtherStateTemporaryPermitDetailsMapper;
import org.epragati.permits.mappers.PermitRouteTypeMapper;
import org.epragati.permits.mappers.TemporaryPermitPassengerDetailsMapper;
import org.epragati.permits.service.PermitValidationsService;
import org.epragati.permits.vo.OtherStateTemporaryPermitDetailsVO;
import org.epragati.permits.vo.PermitClassVO;
import org.epragati.permits.vo.PermitDetailsVO;
import org.epragati.permits.vo.PermitDistrictAdjesentDistrictVO;
import org.epragati.permits.vo.PermitDistrictMappingVO;
import org.epragati.permits.vo.PermitGoodsDetailsVO;
import org.epragati.permits.vo.PermitRouteDetailsVO;
import org.epragati.permits.vo.PermitRouteTypeVO;
import org.epragati.permits.vo.PermitRoutesForSCRTVO;
import org.epragati.permits.vo.PermitTypeVO;
import org.epragati.permits.vo.PermitValidationsVO;
import org.epragati.permits.vo.PermitValidityDetailsVO;
import org.epragati.permits.vo.TPDetailsSearchVO;
import org.epragati.permits.vo.TemporaryPermitPassengerDetailsVO;
import org.epragati.regservice.RegistrationService;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.vo.ApplicationSearchVO;
import org.epragati.regservice.vo.CfstTaxDetailsVO;
import org.epragati.regservice.vo.RegServiceVO;
import org.epragati.restGateway.RestGateWayService;
import org.epragati.rta.service.impl.service.RegistratrionServicesApprovals;
import org.epragati.sequence.SequenceGenerator;
import org.epragati.stagecarriageservice.StageCarriageServices;
import org.epragati.tax.vo.TaxTypeEnum;
import org.epragati.tax.vo.TaxTypeEnum.VoluntaryTaxType;
import org.epragati.util.PermitsEnum;
import org.epragati.util.PermitsEnum.PermitCodes;
import org.epragati.util.PermitsEnum.PermitType;
import org.epragati.util.RoleEnum;
import org.epragati.util.StatusRegistration;
import org.epragati.util.document.Sequence;
import org.epragati.util.payment.ClassOfVehicleEnum;
import org.epragati.util.payment.ServiceEnum;
import org.epragati.util.validators.CommanValidatorForPrNo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author sairam.cheruku
 *
 */
@Service
public class PermitsServiceImpl implements PermitsService {

	public static final Logger logger = LoggerFactory.getLogger(PermitsServiceImpl.class);

	@Autowired
	private PermitClassDAO permitClassDAO;

	@Autowired
	private PermitClassMapper permitClassMapper;

	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;

	@Autowired
	private PermitVehicleMappingDAO permitVehicleMappingDAO;

	@Autowired
	private PermitRoutesForSCRTDAO permitRoutesForSCRTDAO;

	@Autowired
	private PermitTypeDAO permitTypeDAO;

	@Autowired
	private PermitTypeMapper permitTypeMapper;

	@Autowired
	private PermitGoodsDetailsDAO permitGoodsDetailsDAO;

	@Autowired
	private PermitGoodsDetailsMapper permitGoodsDetailsMapper;

	@Autowired
	private PropertiesDAO propertiesDAO;

	@Autowired
	private SequenceGenerator sequencenGenerator;

	@Autowired
	private PermitDetailsDAO permitDetailsDAO;

	@Autowired
	private PermitDetailsMapper permitDetailsMapper;

	@Autowired
	private PermitRouteDetailsDAO permitRouteDetailsDAO;

	@Autowired
	private PermitRouteDetailsMapper permitRouteDetailsMapper;

	@Autowired
	private PermitValidationsDAO permitValidationsDAO;

	@Autowired
	private OfficeWisePermitsAvilabilityDAO officeWisePermitsAvilabilityDAO;

	@Autowired
	private OfficeDAO officeDAO;

	@Autowired
	private DistrictDAO districtDAO;

	@Autowired
	private DistrictMapper districtMapper;

	@Autowired
	private PermitRouteTypeDAO permitRouteTypeDAO;

	@Autowired
	private PermitRouteTypeMapper permitRouteTypeMapper;

	@Autowired
	private PermitDistrictMappingDAO permitDistrictMappingDAO;

	@Autowired
	private PermitDistrictMappingMapper permitDistrictMappingMapper;

	@Autowired
	private PermitValidationsMapper permitValidationsMapper;

	@Autowired
	private RegServiceDAO regServiceDAO;

	@Autowired
	private PermitValidationsService permitValidationsService;

	@Autowired
	private StateWisePermitsAvailabilityDAO stateWisePermitsAvailabilityDAO;

	@Autowired
	private FcDetailsDAO fcDetailsDAO;

	@Autowired
	private RegistrationService registrationService;

	@Autowired
	private TemporaryPermitPassengerDetailsDAO temporaryPermitPassengerDetailsDAO;

	@Autowired
	private TemporaryPermitPassengerDetailsMapper temporaryPermitPassengerDetailsMapper;

	@Autowired
	private DeathPrNoDAO deathPrNoDAO;

	@Autowired
	private OtherStateTemporaryPermitDetailsDAO otherStateTemporaryPermitDetailsDAO;

	@Autowired
	private OtherStateTemporaryPermitDetailsMapper otherStateTemporaryPermitDetailsMapper;

	@Autowired
	private PaymentTransactionDAO paymentTransactionDAO;

	@Autowired
	private VahanSync vahanSync;

	@Autowired
	private StageCarriageServices stageCarriageServices;

	@Autowired
	private RegistratrionServicesApprovals registratrionServicesApprovals;

	@Autowired
	private RestGateWayService restGateWayService;

	@Autowired
	private MasterCovMapper masterCovMapper;

	@Autowired
	private MasterCovDAO masterCovDAO;

	@Autowired
	private MandalMapper mandalMapper;

	@Autowired
	private StateMapper stateMapper;

	@Autowired
	private CommanValidatorForPrNo commanValidatorForPrNo;

	/**
	 * 
	 */
	@Override
	public List<PermitClassVO> getPermitClassMasterData(String serviceName) {
		List<PermitClassVO> permitClass = null;
		List<PermitClassDTO> getPermitDetails = null;

		if (StringUtils.isNotBlank(serviceName)) {
			ServiceEnum serviceValue = ServiceEnum.getServiceEnumByDesc(serviceName);
			if (primaryPermitClassRequiredServices(serviceValue)) {
				getPermitDetails = permitClassDAO.findByStatusTrueAndRequiredInRenewalTrue();
			} else if (serviceValue.equals(ServiceEnum.EXTENSIONOFVALIDITY)) {
				getPermitDetails = permitClassDAO.findByStatusTrueAndReqiredExtensionOfValidityTrue();
			}
			// This condition is useful to get the Recommendation letter combinations
			else if (ServiceEnum.getRLServices().contains(serviceValue.getId())) {
				getPermitDetails = permitClassDAO.findByStatusTrueAndInRlServicesTrue();
			} else {
				getPermitDetails = permitClassDAO.findByStatusTrueAndInRlServicesFalse();
			}

			// ENds here
		} else {
			getPermitDetails = permitClassDAO.findByStatusTrueAndReqiredExtensionOfValidityTrue();
		}

		if (CollectionUtils.isNotEmpty(getPermitDetails)) {
			permitClass = permitClassMapper.convertEntity(getPermitDetails);
		}
		return permitClass;
	}

	private boolean primaryPermitClassRequiredServices(ServiceEnum serviceValue) {
		return serviceValue.equals(ServiceEnum.RENEWALOFPERMIT) || serviceValue.equals(ServiceEnum.TRANSFEROFPERMIT)
				|| serviceValue.equals(ServiceEnum.PERMITCOA) || serviceValue.equals(ServiceEnum.VARIATIONOFPERMIT)
				|| serviceValue.equals(ServiceEnum.REPLACEMENTOFVEHICLE)
				|| serviceValue.equals(ServiceEnum.RENEWALOFAUTHCARD) || serviceValue.equals(ServiceEnum.PERDATAENTRY);
	}

	/**
	 * TO get the permit Type based on permit type based on class of vehicle or pr
	 * number and permit class
	 * 
	 * 
	 */
	@Override
	public Optional<List<PermitTypeVO>> getPermitTypeDetails(String prNo, String permitClass, String classOfVehicle) {
		List<String> permitTypeList = new ArrayList<>();
		List<PermitTypeDTO> permitTypeDTOList = null;
		String cov = StringUtils.EMPTY;
		Optional<RegistrationDetailsDTO> regDetails = null;

		if (StringUtils.isNotBlank(prNo)) {
			regDetails = commonMethodToGetRegDetails(prNo);
		}

		if (StringUtils.isNotBlank(classOfVehicle)) {
			cov = classOfVehicle;
		} else if (StringUtils.isNotBlank(regDetails.get().getClassOfVehicle())) {
			cov = regDetails.get().getClassOfVehicle();
		} else {
			cov = regDetails.get().getVehicleDetails().getClassOfVehicle();
		}
		if (StringUtils.isBlank(cov)) {
			logger.error("Class of vehicle is not present in registaration details:[{}]",
					regDetails.get().getClassOfVehicle());
			throw new BadRequestException("Class of vehicle is not present");
		}

		/**
		 * To get the type of permits based in class of vehicle from
		 * "master_permit_vehiclemapping"
		 */
		List<PermitVehicleMappingDTO> vehicleMappingList = permitVehicleMappingDAO.findByCovAndStatusTrue(cov);

		if (CollectionUtils.isNotEmpty(vehicleMappingList)) {
			permitTypeList = vehicleMappingList.stream().map(type -> type.getPermitType()).collect(Collectors.toList());
		}

		/**
		 * To get the permit type of master_permit_type data based on permit class and
		 * permit type list
		 */
		if (CollectionUtils.isNotEmpty(permitTypeList)) {
			permitTypeDTOList = permitTypeDAO.findByPermitTypeInAndTypeofPermit(permitTypeList, permitClass);
		}

		if (CollectionUtils.isEmpty(permitTypeDTOList)) {
			logger.error("PermitType [{}] with permitClass [{}] is not Available", permitTypeList, permitClass);
			throw new BadRequestException(
					"PermitType [" + permitTypeList + "] with  permitClass[" + permitClass + "] is not Available ");
		}
		return Optional.of(permitTypeMapper.convertEntity(permitTypeDTOList));
	}

	private Optional<RegistrationDetailsDTO> commonMethodToGetRegDetails(String prNo) {
		Optional<RegistrationDetailsDTO> regDetails = registrationDetailDAO.findByPrNo(prNo);
		if (!regDetails.isPresent()) {
			logger.error("Registration Details is not present with this PR.No:[{}]", prNo);
			throw new BadRequestException("Registration Details is not present with this PR.No");
		}
		return regDetails;
	}

	/**
	 * 
	 */
	@Override
	public List<PermitGoodsDetailsVO> getPermitGoodsDetails(String prNo, String permitType, String classOfVehicleValue,
			String bodyTypeValue) {
		String classOfVehicle = StringUtils.EMPTY;
		String bodyType = StringUtils.EMPTY;
		List<PermitGoodsDetailsDTO> permitGoodsList = null;
		boolean bodyTypeRequired = false;

		if (StringUtils.isNotBlank(prNo)) {
			Optional<RegistrationDetailsDTO> regDetails = commonMethodToGetRegDetails(prNo);
			classOfVehicle = regDetails.get().getClassOfVehicle();
			bodyType = regDetails.get().getVehicleDetails().getBodyTypeDesc();
			if (StringUtils.isEmpty(bodyType)) {
				bodyType = regDetails.get().getVahanDetails().getBodyTypeDesc();
			}
		} else {
			classOfVehicle = classOfVehicleValue;
			bodyType = bodyTypeValue;
		}

		/**
		 * To check body type based on class of vehicle in permit_ validations
		 * collection to change the query
		 */
		Optional<PermitValidationsDTO> validationDTO = permitValidationsDAO.findByCovListIn(classOfVehicle);

		if (!validationDTO.isPresent()) {
			logger.error("Validation Details are not present");
			throw new BadRequestException("Validation Details are not present");
		}

		bodyTypeRequired = validationDTO.get().isBodyTypeBasedGoodsDetails();

		if (bodyTypeRequired && StringUtils.isNotBlank(bodyType) && bodyType.equalsIgnoreCase("Tanker")) {
			permitGoodsList = permitGoodsDetailsDAO.findByPermitTypeAndBodyTypeDescIgnoreCase(permitType, bodyType);
		} else {
			permitGoodsList = permitGoodsDetailsDAO.findByPermitTypeAndBodyTypeDescIgnoreCase(permitType,
					StringUtils.EMPTY);
		}
		if (CollectionUtils.isEmpty(permitGoodsList)) {
			logger.error("Goods Details not found");
			throw new BadRequestException("Goods Details not found");
		}
		return permitGoodsDetailsMapper.convertEntity(permitGoodsList);
	}

	@Override
	public void savePermitDetailsForNewPermit(RegServiceDTO regServiceDTO) {
		List<PermitDetailsDTO> permitDetailsList = null;
		PermitDetailsDTO permitDetailsDTO = null;
		if (regServiceDTO.getPdtl() != null) {
			permitDetailsDTO = regServiceDTO.getPdtl();
		}
		List<PermitDetailsDTO> permitDetailsDTOList = regServiceDTO.getPermitDetailsListDTO();
		List<ServiceEnum> serviceIds = regServiceDTO.getServiceIds().stream()
				.map(id -> ServiceEnum.getServiceEnumById(id)).collect(Collectors.toList());
		String prNo = regServiceDTO.getPrNo();
		synchronized (prNo.intern()) {
			for (ServiceEnum serviceEnum : serviceIds) {
				switch (serviceEnum) {
				case NEWPERMIT:
					saveForNewPermit(regServiceDTO, permitDetailsDTO);
					break;
				case RENEWALOFPERMIT:
					saveForRenewalPermit(permitDetailsDTO);
					break;
				case RENEWALOFAUTHCARD:
					saveForRenewalOfAuthCard(permitDetailsDTO);
					break;
				case TRANSFEROFPERMIT:
					RegServiceDTO towDto = permitValidationsService.permitTOPendingCheck(regServiceDTO.getPrNo());
					PermitDetailsDTO dto = fetchPermitDetails(regServiceDTO.getPrNo());
					dto.setRdto(registrationDetailDAO.findByPrNo(regServiceDTO.getPrNo()).get());
					dto.setCreatedDate(LocalDateTime.now());
					dto.setCreatedDateStr(LocalDateTime.now().toString());

					if (towDto.getBuyerDetails() != null
							&& TransferType.DEATH.equals(towDto.getBuyerDetails().getTransferType())) {
						Optional<DeathPrNoDTO> deathPr = deathPrNoDAO.findByPrNoAndStatus(regServiceDTO.getPrNo(),
								Boolean.FALSE);
						if (deathPr.isPresent()) {
							deathPr.get().setStatus(Boolean.TRUE);
							logger.info("Death transfer done for vehicle : {}", regServiceDTO.getPrNo());
							deathPrNoDAO.save(deathPr.get());
						}
					}
					permitDetailsDAO.save(dto);
					logger.info("Transfer Permit Succesfully completed for application number :[{}]",
							regServiceDTO.getApplicationNo());
					break;
				case VARIATIONOFPERMIT:
					saveVariationOfPermit(regServiceDTO, permitDetailsDTO);

					break;
				case PERMITCOA:
					PermitDetailsDTO coaPermit = fetchPermitDetails(regServiceDTO.getPrNo());
					coaPermit.setRdto(registrationDetailDAO.findByPrNo(regServiceDTO.getPrNo()).get());
					coaPermit.setCreatedDate(LocalDateTime.now());
					coaPermit.setCreatedDateStr(LocalDateTime.now().toString());
					permitDetailsDAO.save(coaPermit);
					logger.info("Change of Address for Permit Succesfully completed for application number :[{}]",
							regServiceDTO.getApplicationNo());

					break;
				case SURRENDEROFPERMIT:
					permitDetailsList = saveSurrendOfPermit(permitDetailsMapper.convertEntity(permitDetailsDTOList));

					break;
				case EXTENSIONOFVALIDITY:
					permitDetailsDTO = saveForExtentionOfValidity(regServiceDTO);
					logger.info("your permit has been Extended :[{}]", regServiceDTO.getPrNo());
					break;
				case REPLACEMENTOFVEHICLE:
					permitDetailsDTO = saveForRelacementOfVehicle(regServiceDTO);
					break;
				case ISSUEOFRECOMMENDATIONLETTER:
					permitDetailsDTO = saveForIssueOfRecommendationLetter(regServiceDTO, permitDetailsDTO);
					break;
				case RENEWALOFRECOMMENDATIONLETTER:
					saveForRenewalofRecommendationLetter(permitDetailsDTO);
					break;
				case OTHERSTATETEMPORARYPERMIT:
					saveOtherStateDetailsForTemporaryPermit(regServiceDTO);
					break;
				case OTHERSTATESPECIALPERMIT:
					saveOtherStateDetailsForTemporaryPermit(regServiceDTO);
					break;
				case CHANGEOFADDRESSOFRECOMMENDATIONLETTER:
					saveRecommendationLetterCOADetails(regServiceDTO.getPrNo());
					break;
				case TRANSFEROFRECOMMENDATIONLETTER:
					saveRecommendationLetterTODetails(regServiceDTO.getPrNo());
					break;
				case NEWSTAGECARRIAGEPERMIT:
					savingNewStageCarriagePermit(regServiceDTO, permitDetailsDTO);
					break;

				default:
					break;
				}
			}
			commonMethodSave(regServiceDTO, permitDetailsList, permitDetailsDTO);
		}
	}

	private void savingNewStageCarriagePermit(RegServiceDTO regServiceDTO, PermitDetailsDTO permitDetailsDTO) {
		PermitValidityDetailsDTO permitValidityDetailsDTO = permitDetailsDTO.getPermitValidityDetails() != null
				? permitDetailsDTO.getPermitValidityDetails()
				: new PermitValidityDetailsDTO();
		permitValidityDetailsDTO = setPermitValidityDetails(permitValidityDetailsDTO, permitDetailsDTO, regServiceDTO);
		if (StringUtils.isBlank(permitDetailsDTO.getPermitNo())) {
			permitDetailsDTO.setPermitNo(generatePermitNo(regServiceDTO.getOfficeDetails().getOfficeCode(),
					permitDetailsDTO.getPermitType().getNumberCode()));
		}
		permitDetailsDTO.setCreatedBy(regServiceDTO.getCreatedBy());
		permitDetailsDTO.setPrNo(regServiceDTO.getPrNo());
		permitDetailsDTO.setPermitValidityDetails(permitValidityDetailsDTO);
		permitDetailsDTO.setIsPaymentDone(true);
		permitDetailsDTO.setStageCarriageType(regServiceDTO.getStageCarriageType());
		permitDetailsDTO.setUpdatedBy(regServiceDTO.getUpdatedBy());
		regServiceDTO.setPdtl(permitDetailsDTO);

		Optional<RegistrationDetailsDTO> regDTO = registrationDetailDAO.findByPrNo(regServiceDTO.getPrNo());
		if (regDTO.isPresent()) {
			regDTO.get().setStageCarriageType(regServiceDTO.getStageCarriageType());
		}

		regServiceDAO.save(regServiceDTO);
		permitDetailsDAO.save(permitDetailsDTO);
		registrationDetailDAO.save(regDTO.get());
	}

	private void saveRecommendationLetterTODetails(String prNo) {
		RegServiceDTO towDto = permitValidationsService.permitTOPendingCheck(prNo);
		List<PermitDetailsDTO> recommendationLetterList = fetchRecommendationLetterDetails(prNo);
		recommendationLetterList.stream().forEach(dto -> {
			dto.setRdto(registrationDetailDAO.findByPrNo(prNo).get());
			dto.setCreatedDate(LocalDateTime.now());
			dto.setCreatedDateStr(LocalDateTime.now().toString());
		});

		if (towDto.getBuyerDetails() != null && TransferType.DEATH.equals(towDto.getBuyerDetails().getTransferType())) {
			Optional<DeathPrNoDTO> deathPr = deathPrNoDAO.findByPrNoAndStatus(prNo, Boolean.FALSE);
			if (deathPr.isPresent()) {
				deathPr.get().setStatus(Boolean.TRUE);
				logger.info("Death transfer done for vehicle : {}", prNo);
				deathPrNoDAO.save(deathPr.get());
			}
		}
		permitDetailsDAO.save(recommendationLetterList);
		logger.info("Transfer Permit Succesfully completed for application number :[{}]", prNo);
	}

	private void saveRecommendationLetterCOADetails(String prNo) {
		List<PermitDetailsDTO> recommendationLetterList = fetchRecommendationLetterDetails(prNo);
		recommendationLetterList.stream().forEach(coaPermit -> {
			coaPermit.setRdto(registrationDetailDAO.findByPrNo(prNo).get());
			coaPermit.setCreatedDate(LocalDateTime.now());
			coaPermit.setCreatedDateStr(LocalDateTime.now().toString());
		});
		permitDetailsDAO.save(recommendationLetterList);
		logger.info("Change of Address for Recommedation Letter Succesfully completed for application number :[{}]",
				prNo);

	}

	private RegServiceDTO saveOtherStateDetailsForTemporaryPermit(RegServiceDTO dto) {
		FeeDetailsDTO feeDto = savePaymentDetailsInPermitDocument(dto.getApplicationNo());
		OtherStateTemporaryPermitDetailsDTO osTPDto = dto.getOtherStateTemporaryPermitDetails();
		osTPDto.setPrNo(dto.getPrNo());
		if (osTPDto.getTemporaryPermitDetails() != null) {
			if (dto.getOfficeDetails() != null && StringUtils.isNotBlank(dto.getOfficeDetails().getOfficeCode())) {
				osTPDto.getTemporaryPermitDetails()
						.setPermitNo(generatePermitNo(dto.getOfficeDetails().getOfficeCode(),
								dto.getOtherStateTemporaryPermitDetails().getTemporaryPermitDetails().getPermitType()
										.getNumberCode()));
			} else {
				osTPDto.getTemporaryPermitDetails()
						.setPermitNo(generatePermitNo("APSTA", dto.getOtherStateTemporaryPermitDetails()
								.getTemporaryPermitDetails().getPermitType().getNumberCode()));
			}

		}

		PermitValidityDetailsDTO permitValidityDetailsDTO = new PermitValidityDetailsDTO();
		if (osTPDto.getTemporaryPermitDetails().getRouteDetails() != null
				&& StringUtils.isNotEmpty(osTPDto.getTemporaryPermitDetails().getRouteDetails().getNoOfDays())) {
			if (osTPDto.getTemporaryPermitDetails().getRouteDetails().getNoOfDays().equalsIgnoreCase("7 Days")) {
				permitValidityDetailsDTO.setPermitValidFrom(LocalDate.now());
				permitValidityDetailsDTO.setPermitValidTo(LocalDate.now().plusDays(6));
			} else if (osTPDto.getTemporaryPermitDetails().getRouteDetails().getNoOfDays()
					.equalsIgnoreCase("30 Days")) {
				permitValidityDetailsDTO.setPermitValidFrom(LocalDate.now());
				permitValidityDetailsDTO.setPermitValidTo(LocalDate.now().plusDays(29));
			} else {
				permitValidityDetailsDTO.setPermitValidFrom(
						osTPDto.getTemporaryPermitDetails().getRouteDetails().getForwardRouteDate());
				permitValidityDetailsDTO
						.setPermitValidTo(osTPDto.getTemporaryPermitDetails().getRouteDetails().getReturnRouteDate());
			}
		}
		osTPDto.getTemporaryPermitDetails().setPermitValidityDetails(permitValidityDetailsDTO);

		osTPDto.setApplicationStatus(StatusRegistration.APPROVED);
		osTPDto.setCreatedBy(dto.getCreatedBy());
		osTPDto.setCreatedDate(LocalDateTime.now());
		osTPDto.setCreatedDateStr(LocalDateTime.now().toString());

		dto.setApplicationStatus(StatusRegistration.APPROVED);
		dto.setCurrentIndex(RoleEnum.RTO.getIndex() + 1);
		if (feeDto != null) {
			osTPDto.setFeeDetails(feeDto);
			dto.setFeeDetails(feeDto);
		}
		otherStateTemporaryPermitDetailsDAO.save(osTPDto);
		regServiceDAO.save(dto);
		return dto;
	}

	private void saveForRenewalofRecommendationLetter(PermitDetailsDTO permitDetailsDTO) {
		PermitValidityDetailsDTO permitValidityDetailsDTO = permitDetailsDTO.getPermitValidityDetails();
		if (permitValidityDetailsDTO.getPermitValidFrom().isBefore(LocalDate.now())) {
			permitValidityDetailsDTO.setPermitValidFrom(permitValidityDetailsDTO.getPermitValidFrom());
			permitValidityDetailsDTO
					.setPermitValidTo(permitValidityDetailsDTO.getPermitValidFrom().plusYears(5).minusDays(1));
		} else {
			permitValidityDetailsDTO.setPermitValidFrom(LocalDate.now());
			permitValidityDetailsDTO.setPermitValidTo(LocalDate.now().plusYears(5).minusDays(1));
		}
		permitDetailsDTO.setPermitValidityDetails(permitValidityDetailsDTO);
		permitDetailsDTO.setIsRecommendationLetter(Boolean.TRUE);
	}

	private PermitDetailsDTO saveForIssueOfRecommendationLetter(RegServiceDTO regServiceDTO,
			PermitDetailsDTO permitDetailsDTO) {
		PermitValidityDetailsDTO permitValidityDetailsDTO = new PermitValidityDetailsDTO();
		String permitNumber = null;
		Optional<PermitDetailsDTO> primaryPermitDetails = permitDetailsDAO
				.findByPrNoAndPermitStatusAndPermitTypeTypeofPermitOrderByCreatedDateDesc(regServiceDTO.getPrNo(),
						PermitsEnum.ACTIVE.getDescription(), PermitType.PRIMARY.getPermitTypeCode());

		if (regServiceDTO.getPdtl().getPermitNo() == null) {
			permitNumber = primaryPermitDetails.get().getPermitNo();
			if (StringUtils.isBlank(permitNumber)) {
				logger.error("Problem occured while generating Permit number for PR number ,[{}] ",
						regServiceDTO.getPrNo());
				throw new BadRequestException("Unable to process your request with PR no " + regServiceDTO.getPrNo());
			}
			permitDetailsDTO.setPermitNo(permitNumber);
			permitDetailsDTO.setPermitStatus(PermitsEnum.ACTIVE.getDescription());
			permitDetailsDTO.setPrNo(regServiceDTO.getPrNo());
			permitDetailsDTO.setIsRecommendationLetter(Boolean.TRUE);
			permitDetailsDTO.setRecomendationLetterNo(regServiceDTO.getApplicationNo());
			permitValidityDetailsDTO = setRLValidityDetails(permitValidityDetailsDTO, primaryPermitDetails.get());
			permitDetailsDTO.setPermitValidityDetails(permitValidityDetailsDTO);
			regServiceDTO.setPdtl(permitDetailsDTO);
			if (permitDetailsDTO.getRouteDetails() != null
					&& StringUtils.isNoneBlank(permitDetailsDTO.getRouteDetails().getState())) {
				updateRLAndCSPAvilabilityDetailsByStateWise(permitDetailsDTO.getRouteDetails().getState());
			}

		}
		return permitDetailsDTO;
	}

	private PermitValidityDetailsDTO setRLValidityDetails(PermitValidityDetailsDTO permitValidityDetailsDTO,
			PermitDetailsDTO permitDetails) {

		permitValidityDetailsDTO.setPermitValidFrom(LocalDate.now());
		permitValidityDetailsDTO.setPermitValidTo(permitDetails.getPermitValidityDetails().getPermitValidTo());

		return permitValidityDetailsDTO;

	}

	private void commonMethodSave(RegServiceDTO regServiceDTO, List<PermitDetailsDTO> permitDetailsList,
			PermitDetailsDTO permitDetailsDTO) {
		if (!regServiceDTO.getServiceIds().stream()
				.anyMatch(service -> service.equals(ServiceEnum.OTHERSTATETEMPORARYPERMIT.getId())
						|| service.equals(ServiceEnum.OTHERSTATESPECIALPERMIT.getId())
						|| service.equals(ServiceEnum.OTHERSTATESPECIALPERMIT.getId()))) {

			RegistrationDetailsDTO rDTO = regServiceDTO.getRegistrationDetails();
			if (!CollectionUtils.isEmpty(permitDetailsList)) {
				extensionOfValiditySave(regServiceDTO, permitDetailsList, rDTO);
			} else {
				if (regServiceDTO.getPdtl() != null && StringUtils.isNotBlank(regServiceDTO.getPdtl().getPermitNo())
						&& !regServiceDTO.getServiceIds().stream()
								.anyMatch(val -> ServiceEnum.getRLServices().contains(val))) {
					checkForActivePermitsinRegAndPermitDocumnet(regServiceDTO);
				}
				if (permitDetailsDTO != null) {
					rDTO.setPermitDetails(permitDetailsDTO);
					if (regServiceDTO.getServiceIds() != null && regServiceDTO.getServiceType() != null) {
						permitDetailsDTO.setServiceIds(regServiceDTO.getServiceIds());
						permitDetailsDTO.setServiceType(regServiceDTO.getServiceType());
					}
				}

				if (regServiceDTO.getServiceIds().contains(ServiceEnum.REPLACEMENTOFVEHICLE.getId())) {
					rDTO = setPermitDetailsForVehReplacement(Optional.of(rDTO), permitDetailsDTO, regServiceDTO);
				} else {
					if (permitDetailsDTO.getPermitClass().getCode()
							.equalsIgnoreCase(PermitType.TEMPORARY.getPermitTypeCode())) {
						Optional<PermitDetailsDTO> permitDetailsDTOOptional = setPermitDetailsForRegDetatils(
								regServiceDTO.getPrNo());
						permitDetailsDTOOptional.get().setRdto(null);
						rDTO.setPermitDetails(permitDetailsDTOOptional.get());
					}
					if (permitDetailsDTO.getPermitClass().getCode()
							.equalsIgnoreCase(PermitType.PRIMARY.getPermitTypeCode())) {
						rDTO.setPermitDetails(permitDetailsDTO);
					}
				}

			}
			if (regServiceDTO.getInsuranceDetails() != null) {
				rDTO.setInsuranceDetails(regServiceDTO.getInsuranceDetails());
			}
			if (regServiceDTO.getPucDetails() != null) {
				rDTO.setPucDetailsDTO(regServiceDTO.getPucDetails());
			}
			permitDetailsDTO.setCreatedDateStr(LocalDateTime.now().toString());
			regServiceDTO.setApplicationStatus(StatusRegistration.APPROVED);
			regServiceDTO.setCurrentIndex(RoleEnum.RTO.getIndex() + 1);
			rDTO.setIsvahanSync(Boolean.FALSE);
			rDTO.setIsvahanSyncSkip(Boolean.FALSE);
			permitDetailsDAO.save(permitDetailsDTO);
			regServiceDAO.save(regServiceDTO);
			permitDetailsDTO.setRdto(null);
			vahanSync.commonVahansync(rDTO);
			registrationDetailDAO.save(rDTO);

		}
	}

	private void extensionOfValiditySave(RegServiceDTO regServiceDTO, List<PermitDetailsDTO> permitDetailsList,
			RegistrationDetailsDTO rDTO) {
		PermitDetailsDTO permitDetailsDTO;
		permitDetailsDTO = regServiceDTO.getPermitDetailsListDTO().stream().findFirst().get();
		if (permitDetailsList.stream().findFirst().get().getPermitClass().getCode()
				.equalsIgnoreCase(PermitType.PRIMARY.getPermitTypeCode())) {
			permitDetailsDTO = regServiceDTO.getPermitDetailsListDTO().stream().findFirst().get();
			permitDetailsDTO.setPermitStatus(PermitsEnum.INACTIVE.getDescription());
			permitDetailsDTO.setPermitSurrender(true);
			rDTO.setPermitDetails(permitDetailsDTO);
		}
		if (permitDetailsList.stream().findFirst().get().getPermitClass().getCode()
				.equalsIgnoreCase(PermitType.TEMPORARY.getPermitTypeCode())) {
			Optional<PermitDetailsDTO> permitDetailsDTOOptional = setPermitDetailsForRegDetatils(
					regServiceDTO.getPrNo());
			permitDetailsDTOOptional.get().setRdto(null);
			rDTO.setPermitDetails(permitDetailsDTOOptional.get());
		}
		regServiceDTO.setApplicationStatus(StatusRegistration.APPROVED);
		regServiceDTO.setCurrentIndex(RoleEnum.RTO.getIndex() + 1);
		regServiceDAO.save(regServiceDTO);
		permitDetailsDAO.save(permitDetailsList);
	}

	public RegistrationDetailsDTO setPermitDetailsForVehReplacement(Optional<RegistrationDetailsDTO> regDTO,
			PermitDetailsDTO permitDetailsDTO, RegServiceDTO regServiceDTO) {
		regDTO = registrationDetailDAO.findByPrNo(regServiceDTO.getPdtl().getNonPermitPrNo());
		if (!regDTO.isPresent()) {
			logger.error("Registration Details not found with this PR No [{}]",
					regServiceDTO.getPdtl().getNonPermitPrNo());
			throw new BadRequestException(
					"Registration Details not found with this PR No " + regServiceDTO.getPdtl().getNonPermitPrNo());
		}
		regDTO.get().setPermitDetails(permitDetailsDTO);

		return regDTO.get();
	}

	private void saveVariationOfPermit(RegServiceDTO regServiceDTO, PermitDetailsDTO permitDetailsDTO) {
		Optional<PermitDetailsDTO> permitDetailsOptional = permitDetailsDAO
				.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(regServiceDTO.getPrNo(),
						PermitType.PRIMARY.getPermitTypeCode(), PermitsEnum.ACTIVE.getDescription());
		if (!permitDetailsOptional.isPresent()) {
			throw new BadRequestException("New Permit Record Not found with prNo" + regServiceDTO.getPrNo());
		}
		PermitDetailsDTO permitDetails = permitDetailsOptional.get();
		PermitValidityDetailsDTO permitValidityDetailsDTO = new PermitValidityDetailsDTO();
		permitDetailsDTO.setPermitNo(permitDetailsDTO.getPermitNo());
		permitDetailsDTO.setPermitStatus(PermitsEnum.ACTIVE.getDescription());
		permitDetailsDTO.setPrNo(regServiceDTO.getPrNo());
		if (permitDetails.getPermitValidityDetails() != null
				&& permitDetails.getPermitValidityDetails().getPermitValidFrom() != null) {
			permitValidityDetailsDTO.setPermitValidFrom(permitDetails.getPermitValidityDetails().getPermitValidFrom());
		}
		if (permitDetails.getPermitValidityDetails() != null
				&& permitDetails.getPermitValidityDetails().getPermitValidTo() != null) {
			permitValidityDetailsDTO.setPermitValidTo(permitDetails.getPermitValidityDetails().getPermitValidTo());
		}
		if (permitDetails.getPermitValidityDetails() != null
				&& permitDetails.getPermitValidityDetails().getPermitAuthorizationValidFrom() != null) {
			permitValidityDetailsDTO.setPermitAuthorizationValidFrom(
					permitDetails.getPermitValidityDetails().getPermitAuthorizationValidFrom());
		}

		if (permitDetails.getPermitValidityDetails() != null
				&& permitDetails.getPermitValidityDetails().getPermitAuthorizationValidTo() != null) {
			permitValidityDetailsDTO.setPermitAuthorizationValidTo(
					permitDetails.getPermitValidityDetails().getPermitAuthorizationValidTo());
		}
		if (permitDetailsDTO.getPermitType().isAuthorization()) {
			setAuthorizationDetailsForNatinalPermit(permitDetailsDTO, permitValidityDetailsDTO, regServiceDTO);
		}
		permitDetailsDTO.setPermitValidityDetails(permitValidityDetailsDTO);
		regServiceDTO.setPdtl(permitDetailsDTO);
	}

	private void checkForActivePermitsinRegAndPermitDocumnet(RegServiceDTO regServiceDTO) {
		String permitNo = regServiceDTO.getPdtl().getPermitNo();
		Optional<PermitDetailsDTO> permitsDto = permitDetailsDAO.findByPermitNoAndPermitStatus(permitNo,
				PermitsEnum.ACTIVE.getDescription());
		if (permitsDto.isPresent()) {
			permitsDto.get().setPermitStatus(PermitsEnum.INACTIVE.getDescription());
			permitDetailsDAO.save(permitsDto.get());
		}
	}

	private void saveForNewPermit(RegServiceDTO regServiceDTO, PermitDetailsDTO permitDetailsDTO) {
		PermitValidityDetailsDTO permitValidityDetailsDTO = permitDetailsDTO.getPermitValidityDetails() != null
				? permitDetailsDTO.getPermitValidityDetails()
				: new PermitValidityDetailsDTO();
		String permitNumber = null;
		if (regServiceDTO.getPdtl().getPermitNo() == null) {
			permitNumber = generatePermitNo(regServiceDTO.getOfficeDetails().getOfficeCode(),
					regServiceDTO.getPdtl().getPermitType().getNumberCode());
			if (StringUtils.isBlank(permitNumber)) {
				logger.error("Problem occured while generating Permit number for PR number ,[{}] ",
						regServiceDTO.getPrNo());
				throw new BadRequestException("Unable to process your request with PR no " + regServiceDTO.getPrNo());
			}
			permitDetailsDTO.setPermitNo(permitNumber);
			permitDetailsDTO.setPermitStatus(PermitsEnum.ACTIVE.getDescription());
			permitDetailsDTO.setPrNo(regServiceDTO.getPrNo());
			permitValidityDetailsDTO = setPermitValidityDetails(permitValidityDetailsDTO, permitDetailsDTO,
					regServiceDTO);
			if (permitDetailsDTO.getPermitType().isAuthorization()) {
				setAuthorizationDetailsForNatinalPermit(permitDetailsDTO, permitValidityDetailsDTO, regServiceDTO);
			}
			permitDetailsDTO.setPermitValidityDetails(permitValidityDetailsDTO);
			regServiceDTO.setPdtl(permitDetailsDTO);

			// TODO: Removed office wise validity so this code is not required
			// keep it here for enabled office wise permits
			/*
			 * if (regServiceDTO.getRegistrationDetails().getClassOfVehicle()
			 * .equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())) {
			 * updateOfficeWisePermitDetails(
			 * regServiceDTO.getRegistrationDetails().getOfficeDetails(). getOfficeCode());
			 * }
			 */

			if (permitDetailsDTO.getRouteDetails() != null
					&& StringUtils.isNoneBlank(permitDetailsDTO.getRouteDetails().getState())) {
				updateRLAndCSPAvilabilityDetailsByStateWise(permitDetailsDTO.getRouteDetails().getState());
			}

		}
	}

	@Override
	public String generatePermitNo(String officeCode, String permitNumberCode) {
		String permitNumber;
		Map<String, String> detail = new HashMap<>();
		detail.put("officeCode", officeCode);// regServiceDTO.getOfficeDetails().getOfficeCode()
		detail.put("numberCode", permitNumberCode); // regServiceDTO.getPdtl().getPermitType().getNumberCode()
		permitNumber = sequencenGenerator.getSequence(Sequence.PERMITNUMBER.getSequenceId().toString(), detail);
		return permitNumber;
	}

	/**
	 * Renewal of Authorization card details
	 * 
	 * Multiple conditions added in this method -> For AITP, AITC permit validity
	 * added as per citizen Input. -> If permit is not expired validity will set
	 * after permit expiration time. -> If not From today on wards we will give them
	 * the authorization card validity.
	 * 
	 * @param regServiceDTO
	 * @param permitDetailsDTO
	 */
	private void saveForRenewalOfAuthCard(PermitDetailsDTO permitDetailsDTO) {
		PermitValidityDetailsDTO permitValidityDetailsDTO = permitDetailsDTO.getPermitValidityDetails();

		LocalDate fromDate = null;
		LocalDate toDate = null;

		if (permitValidityDetailsDTO.getNoOfMonths() != null) {
			if (permitValidityDetailsDTO.getPermitAuthorizationValidTo().isAfter(LocalDate.now())) {

				fromDate = permitValidityDetailsDTO.getPermitAuthorizationValidTo();
				toDate = permitValidityDetailsDTO.getPermitAuthorizationValidTo()
						.plusMonths(permitValidityDetailsDTO.getNoOfMonths()).minusDays(1);
			} else {
				fromDate = LocalDate.now();
				toDate = LocalDate.now().plusMonths(permitValidityDetailsDTO.getNoOfMonths()).minusDays(1);
			}

			if ((getMothsValue().contains(toDate.getMonthValue()) && toDate.getDayOfMonth() == 29)
					|| (toDate.isLeapYear() && toDate.getMonthValue() == 2 && toDate.getDayOfMonth() == 28)
					|| (!toDate.isLeapYear() && toDate.getMonthValue() == 2 && toDate.getDayOfMonth() == 27)) {
				toDate.plusDays(1);
			}

		} else {
			fromDate = LocalDate.now();
			toDate = LocalDate.now().plusYears(1).minusDays(1);
		}

		permitValidityDetailsDTO.setPermitAuthorizationValidFrom(fromDate);
		permitValidityDetailsDTO.setPermitAuthorizationValidTo(toDate);
		permitDetailsDTO.setPermitValidityDetails(permitValidityDetailsDTO);

	}

	private static List<Integer> getMothsValue() {

		List<Integer> listOfMonths = Arrays.asList(4, 6, 9, 11);

		return listOfMonths;
	}

	public static List<String> getAllIndiaPermits() {
		return Arrays.asList(PermitCodes.AITP.getPermitCode(), PermitCodes.AITC.getPermitCode());
	}

	/**
	 * Renewal of Permit details
	 * 
	 * Multiple conditions added in this method -> If permit is not expired validity
	 * will set after permit expiration time. -> If not From today on wards we will
	 * give them the permit validity.
	 * 
	 * @param regServiceDTO
	 * @param permitDetailsDTO
	 */
	private void saveForRenewalPermit(PermitDetailsDTO permitDetailsDTO) {

		PermitValidityDetailsDTO permitValidityDetailsDTO = permitDetailsDTO.getPermitValidityDetails();

		if (permitValidityDetailsDTO.getPermitValidTo().isAfter(LocalDate.now())) {
			permitValidityDetailsDTO.setPermitValidFrom(permitValidityDetailsDTO.getPermitValidTo());
			permitValidityDetailsDTO
					.setPermitValidTo(permitValidityDetailsDTO.getPermitValidTo().plusYears(5).minusDays(1));
		} else {
			permitValidityDetailsDTO.setPermitValidFrom(LocalDate.now());
			permitValidityDetailsDTO.setPermitValidTo(LocalDate.now().plusYears(5).minusDays(1));
		}

	}

	@Override
	public PermitValidityDetailsDTO setPermitValidityDetails(PermitValidityDetailsDTO permitValidityDetailsDTO,
			PermitDetailsDTO permitDetailsDTO, RegServiceDTO regServiceDTO) {
		Optional<PropertiesDTO> dto = propertiesDAO.findByStatusTrue();
		@SuppressWarnings("unused")
		Map<String, Integer> validityMasterData = null;
		if (dto.isPresent()) {
			validityMasterData = dto.get().getPermitsValidity();
		}
		if (permitDetailsDTO.getPermitClass().getCode().equals(PermitType.TEMPORARY.getPermitTypeCode())) {
			if (permitDetailsDTO.getRouteDetails() != null
					&& permitDetailsDTO.getRouteDetails().getForwardRouteDate() != null
					&& permitDetailsDTO.getRouteDetails().getReturnRouteDate() != null) {
				permitValidityDetailsDTO.setPermitValidFrom(permitDetailsDTO.getRouteDetails().getForwardRouteDate());
				permitValidityDetailsDTO.setPermitValidTo(permitDetailsDTO.getRouteDetails().getReturnRouteDate());
			}
		} else {
			permitValidityDetailsDTO.setPermitValidFrom(LocalDate.now());
			permitValidityDetailsDTO.setPermitValidTo(LocalDate.now().plusYears(5).minusDays(1));
		}
		return permitValidityDetailsDTO;
	}

	@Override
	public void checkWithTaxExpairyDays(PermitDetailsDTO permitDetailsDTO, String prNo) {
		Optional<TaxDetailsDTO> taxDto = null;
		taxDto = registrationService.getLatestTaxTransaction(prNo);
		LocalDate date1 = permitDetailsDTO.getRouteDetails().getForwardRouteDate();
		LocalDate date2 = permitDetailsDTO.getRouteDetails().getReturnRouteDate();
		Long daysDiff = ChronoUnit.DAYS.between(date1, date2);
		Long daysdiff2 = ChronoUnit.DAYS.between(LocalDate.now(), taxDto.get().getTaxPeriodEnd().plusDays(30));

		if (daysDiff > daysdiff2) {
			throw new BadRequestException("Your tax will expire on [" + taxDto.get().getTaxPeriodEnd()
					+ "] please enter the return date before [" + taxDto.get().getTaxPeriodEnd().plusDays(30) + "]");
		}

	}

	/**
	 * Use this method when ever office wise permits availability enabled in ARKT
	 * vehicles for specific offices
	 * 
	 * @param officeCode
	 */
	@SuppressWarnings("unused")
	private void updateOfficeWisePermitDetails(String officeCode) {
		Optional<OfficeWisePermitsAvilabilityDTO> avilabilityDTO = officeWisePermitsAvilabilityDAO
				.findByOfficeCodeAndStatusTrue(officeCode);
		if (avilabilityDTO.isPresent()) {
			OfficeWisePermitsAvilabilityDTO officeWiseAvilabilityDTO = avilabilityDTO.get();
			officeWiseAvilabilityDTO.setBookedPermits(officeWiseAvilabilityDTO.getBookedPermits() + 1);
			officeWiseAvilabilityDTO.setRemainingPermits(officeWiseAvilabilityDTO.getRemainingPermits() - 1);
			officeWisePermitsAvilabilityDAO.save(officeWiseAvilabilityDTO);
		}

	}

	private void updateRLAndCSPAvilabilityDetailsByStateWise(String state) {
		Optional<StateWisePermitsAvailabilityDTO> avilabilityDTO = stateWisePermitsAvailabilityDAO
				.findByStateNameAndStatusTrueAndIsRecommendationTrue(state);
		if (avilabilityDTO.isPresent()) {
			StateWisePermitsAvailabilityDTO stateWisePermitsAvailabilityDTO = avilabilityDTO.get();
			stateWisePermitsAvailabilityDTO.setIssuedPermits(stateWisePermitsAvailabilityDTO.getIssuedPermits() + 1);
			stateWisePermitsAvailabilityDTO
					.setRemainingPermits(stateWisePermitsAvailabilityDTO.getRemainingPermits() - 1);
			stateWisePermitsAvailabilityDAO.save(stateWisePermitsAvailabilityDTO);
		}

	}

	private void setAuthorizationDetailsForNatinalPermit(PermitDetailsDTO permitDetailsDTO,
			PermitValidityDetailsDTO permitValidityDetailsDTO, RegServiceDTO regServiceDto) {
		Optional<PropertiesDTO> dto = propertiesDAO.findByStatusTrue();

		Map<String, Integer> validityMasterData = null;
		if (dto.isPresent()) {
			validityMasterData = dto.get().getPermitsValidity();
		}
		if (regServiceDto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWPERMIT.getId()))) {
			if (permitValidityDetailsDTO.getNoOfMonths() != null) {
				permitValidityDetailsDTO.setPermitAuthorizationValidFrom(LocalDate.now());
				permitValidityDetailsDTO.setPermitAuthorizationValidTo(
						LocalDate.now().plusMonths(permitValidityDetailsDTO.getNoOfMonths()).minusDays(1));
			} else {
				permitValidityDetailsDTO.setPermitAuthorizationValidFrom(LocalDate.now());
				permitValidityDetailsDTO.setPermitAuthorizationValidTo(
						LocalDate.now().plusYears(validityMasterData.get("permitAuthorizationValidity")).minusDays(1));
			}
		}
		permitDetailsDTO.setPermitValidityDetails(permitValidityDetailsDTO);
		if (StringUtils.isBlank(permitDetailsDTO.getPermitAuthorizationNo())) {
			permitDetailsDTO.setPermitAuthorizationNo(permitDetailsDTO.getPermitNo());
		}
	}

	@Override
	public Optional<PermitDetailsVO> findPermitDetailsByRcNoAndStatus(String prNo) {

		Optional<PermitDetailsDTO> dto = permitDetailsDAO.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(prNo,
				PermitType.PRIMARY.getPermitTypeCode(), PermitsEnum.ACTIVE.getDescription());
		if (!dto.isPresent()) {
			throw new BadRequestException(
					"You are not eligible to apply for the Temprary Permit with this PR no " + prNo);
		}
		return permitDetailsMapper.convertEntity(dto);

	}

	@Override
	public Optional<PermitDetailsVO> findPermitInactiveRecords(String prNo) {

		Optional<PermitDetailsDTO> permitDetailsDTO = permitDetailsDAO
				.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(prNo, PermitType.PRIMARY.getPermitTypeCode(),
						PermitsEnum.ACTIVE.getDescription());
		if (!permitDetailsDTO.isPresent()) {
			throw new BadRequestException("Pucca Permit Details are Inactive Status with " + prNo);
		}

		return permitDetailsMapper.convertEntity(permitDetailsDTO);
	}

	@Override
	public Optional<List<PermitDetailsDTO>> getListOfPermitDetailsByPrNoAndStatus(String prNo) {
		List<PermitDetailsDTO> permitDetailsDTO = permitDetailsDAO.findByPrNoAndPermitStatus(prNo,
				PermitsEnum.ACTIVE.getDescription());
		if (CollectionUtils.isNotEmpty(permitDetailsDTO)) {
			return Optional.of(permitDetailsDTO);
		}
		return Optional.empty();
	}

	@Override
	public Optional<List<PermitDetailsVO>> searchApplicationByPermitOrPrNo(ApplicationSearchVO applicationSearchVO) {
		String prNo = StringUtils.EMPTY;
		if (StringUtils.isNotBlank(applicationSearchVO.getPrNo())) {
			prNo = applicationSearchVO.getPrNo();
		} else {
			Optional<PermitDetailsDTO> permitDetails = permitDetailsDAO.findByPermitNoAndPermitStatus(
					applicationSearchVO.getPermitNo(), PermitsEnum.ACTIVE.getDescription());
			if (permitDetails.isPresent()) {
				prNo = permitDetails.get().getPrNo();
			}
		}
		Optional<RegistrationDetailsDTO> regDetails = registrationDetailDAO.findByPrNo(prNo);
		if (regDetails.isPresent()) {
			if (!regDetails.get().getVahanDetails().getChassisNumber()
					.equalsIgnoreCase(applicationSearchVO.getChassisNo())) {
				logger.error("provided chassis no : [{}], actual chassis no: [{}]", applicationSearchVO.getChassisNo(),
						regDetails.get().getVahanDetails().getChassisNumber());
				throw new BadRequestException("Please provide correct chassis numbers ");
			}
		}
		List<PermitDetailsDTO> permitDetailsDTO = null;
		if (StringUtils.isNoneBlank(applicationSearchVO.getPrNo())) {
			permitDetailsDTO = permitDetailsDAO.findByPrNoAndPermitStatus(applicationSearchVO.getPrNo(),
					PermitsEnum.ACTIVE.getDescription());
		} else if (StringUtils.isNoneBlank(applicationSearchVO.getPermitNo())) {
			permitDetailsDTO = permitDetailsDAO.findByPermitNo(applicationSearchVO.getPermitNo());
		}
		if (CollectionUtils.isEmpty(permitDetailsDTO)) {
			logger.error("Permit details are not present with the Permit No [{}] or with Pr No : [{}]",
					applicationSearchVO.getPermitNo(), applicationSearchVO.getPermitNo());
			throw new BadRequestException("Permit details are not present with the Permit No ["
					+ applicationSearchVO.getPermitNo() + "] or with Pr No : [" + applicationSearchVO.getPrNo() + "]");
		}
		return Optional.of(permitDetailsMapper.convertEntity(permitDetailsDTO));
	}

	@Override
	public Optional<PermitDetailsVO> findByPrNoOrPermitNoStatusBased(ApplicationSearchVO applicationSearchVO) {
		Optional<PermitDetailsDTO> permitDetailsDTO = null;
		if (StringUtils.isNoneBlank(applicationSearchVO.getPrNo())) {
			permitDetailsDTO = permitDetailsDAO.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(
					applicationSearchVO.getPrNo(), PermitType.PRIMARY.getPermitTypeCode(),
					PermitsEnum.ACTIVE.getDescription());
		} else if (StringUtils.isNoneBlank(applicationSearchVO.getPermitNo())) {
			permitDetailsDTO = permitDetailsDAO.findByPermitNoAndPermitTypeTypeofPermitAndPermitStatus(
					applicationSearchVO.getPermitNo(), PermitType.PRIMARY.getPermitTypeCode(),
					PermitsEnum.ACTIVE.getDescription());
		}
		if (!permitDetailsDTO.isPresent()) {
			logger.error("Permit details are not present with the Permit No [{}] or with Pr No : [{}]",
					applicationSearchVO.getPermitNo(), applicationSearchVO.getPermitNo());
			throw new BadRequestException("Permit details are not present with the Permit No ["
					+ applicationSearchVO.getPermitNo() + "] or with Pr No : [" + applicationSearchVO.getPrNo() + "]");
		}
		return Optional.of(permitDetailsMapper.convertEntity(permitDetailsDTO.get()));
	}

	@Override
	public List<PermitRouteDetailsVO> getPermitsRouteDetailsList(String prNo, String permitType) {
		String classOfVehicle = null;
		List<PermitRouteDetailsDTO> permitRouteList = null;
		Optional<RegistrationDetailsDTO> regDetails = commonMethodToGetRegDetails(prNo);
		classOfVehicle = regDetails.get().getClassOfVehicle();
		permitRouteList = permitRouteDetailsDAO.findByCovAndPermitType(classOfVehicle, permitType);
		if (CollectionUtils.isEmpty(permitRouteList)) {
			logger.error("Goods Details not found");
			throw new BadRequestException("Goods Details not found");
		}
		return permitRouteDetailsMapper.convertEntity(permitRouteList);
	}

	@Override
	public Optional<PermitDistrictAdjesentDistrictVO> getDistrictBasedOnPrNo(String prNo, String routeType) {
		String officeCode = null;
		Integer districtId = null;
		PermitDistrictAdjesentDistrictVO permitDistrictAdjesentDistrictVO = new PermitDistrictAdjesentDistrictVO();
		boolean adjesenDistrictRequired = false;
		Optional<DistrictVO> districtVo = null;
		List<PermitDistrictMappingVO> permitDistrictMappingVO = null;
		Optional<RegistrationDetailsDTO> regDetails = commonMethodToGetRegDetails(prNo);
		if (regDetails.get().getOfficeDetails() != null) {
			officeCode = regDetails.get().getOfficeDetails().getOfficeCode();
		}
		Optional<OfficeDTO> officeDTO = officeDAO.findByOfficeCode(officeCode);
		if (officeDTO.isPresent()) {
			districtId = officeDTO.get().getDistrict();
		}
		List<DistrictDTO> districtDTO = districtDAO.findByDistrictId(districtId);
		Optional<DistrictDTO> dto = districtDTO.stream().findFirst();
		if (dto.isPresent()) {
			districtVo = districtMapper.convertEntity(dto);
			permitDistrictAdjesentDistrictVO.setDistictVO(districtVo.get());
		}
		Optional<PermitRouteTypeDTO> permitRouteTypeDTO = permitRouteTypeDAO.findByRouteType(routeType);
		if (permitRouteTypeDTO.isPresent()) {
			adjesenDistrictRequired = permitRouteTypeDTO.get().isShowAdjacentDistrict();
		}
		if (adjesenDistrictRequired) {
			List<PermitDistrictMappingDTO> districtMappinList = permitDistrictMappingDAO.findByDistrictCode(districtId);
			if (CollectionUtils.isNotEmpty(districtMappinList)) {
				permitDistrictMappingVO = permitDistrictMappingMapper.convertEntity(districtMappinList);
				permitDistrictAdjesentDistrictVO.setAdjesentDistrictVO(permitDistrictMappingVO);
			}
		}
		return Optional.of(permitDistrictAdjesentDistrictVO);
	}

	@Override
	public List<PermitRouteTypeVO> getListOfTypeOfRoute(String prNo, boolean authorization) {
		String cov = null;
		List<PermitRouteTypeDTO> permitRouteType = null;
		Optional<RegistrationDetailsDTO> regDetails = commonMethodToGetRegDetails(prNo);
		cov = regDetails.get().getClassOfVehicle();

		if (cov.equalsIgnoreCase(ClassOfVehicleEnum.COCT.getCovCode())
				|| cov.equalsIgnoreCase(ClassOfVehicleEnum.MCPT.getCovCode())
				|| cov.equalsIgnoreCase(ClassOfVehicleEnum.TOVT.getCovCode())) {
			if (authorization) {
				permitRouteType = permitRouteTypeDAO.findByCovAndStatusTrueAndAuthorizationTrue(cov);
			} else {
				permitRouteType = permitRouteTypeDAO.findByCovAndStatusTrue(cov);
			}
		}
		if (CollectionUtils.isEmpty(permitRouteType)) {
			logger.error("No Record Found to Transfer Permit with cov [{}]", cov);
			throw new BadRequestException("No Record Found to Transfer Permit" + cov);
		}
		return permitRouteTypeMapper.convertEntity(permitRouteType);
	}

	@Override
	public PermitDetailsDTO fetchPermitDetails(String prNo) {
		Optional<PermitDetailsDTO> dto = permitDetailsDAO
				.findByPrNoAndPermitStatusAndPermitTypeTypeofPermitOrderByCreatedDateDesc(prNo,
						PermitsEnum.ACTIVE.getDescription(), PermitType.PRIMARY.getPermitTypeCode());
		if (!dto.isPresent()) {
			logger.error("No Record Found to Transfer Permit");
			throw new BadRequestException("No Record Found to Transfer Permit");
		}
		return dto.get();
	}

	@Override
	public List<PermitDetailsDTO> fetchRecommendationLetterDetails(String prNo) {
		List<PermitDetailsDTO> dto = permitDetailsDAO.findByPrNoAndPermitStatusAndIsRecommendationLetterTrue(prNo,
				PermitsEnum.ACTIVE.getDescription());
		if (CollectionUtils.isEmpty(dto)) {
			logger.error("No Record Found to Transfer Permit");
			throw new BadRequestException("No Record Found to Transfer Permit");
		}
		return dto;
	}

	@Override
	public boolean verifyForPermitServices(List<Integer> serviceIds) {
		List<Integer> payVerify = new ArrayList<>();
		payVerify.add(ServiceEnum.NEWPERMIT.getId());
		payVerify.add(ServiceEnum.RENEWALOFPERMIT.getId());
		if (!(serviceIds.contains(ServiceEnum.TRANSFEROFOWNERSHIP.getId())
				|| serviceIds.contains(ServiceEnum.CHANGEOFADDRESS.getId()))) {
			payVerify.add(ServiceEnum.SURRENDEROFPERMIT.getId());
			payVerify.add(ServiceEnum.TRANSFEROFPERMIT.getId());
			payVerify.add(ServiceEnum.PERMITCOA.getId());
			payVerify.add(ServiceEnum.TRANSFEROFRECOMMENDATIONLETTER.getId());
			payVerify.add(ServiceEnum.CHANGEOFADDRESSOFRECOMMENDATIONLETTER.getId());
		}

		payVerify.add(ServiceEnum.VARIATIONOFPERMIT.getId());
		payVerify.add(ServiceEnum.EXTENSIONOFVALIDITY.getId());
		payVerify.add(ServiceEnum.REPLACEMENTOFVEHICLE.getId());
		payVerify.add(ServiceEnum.RENEWALOFAUTHCARD.getId());
		payVerify.add(ServiceEnum.ISSUEOFRECOMMENDATIONLETTER.getId());
		payVerify.add(ServiceEnum.RENEWALOFRECOMMENDATIONLETTER.getId());
		payVerify.add(ServiceEnum.OTHERSTATETEMPORARYPERMIT.getId());
		payVerify.add(ServiceEnum.OTHERSTATESPECIALPERMIT.getId());
		payVerify.add(ServiceEnum.NEWSTAGECARRIAGEPERMIT.getId());
		boolean verifyPaymentIntiation = payVerify.stream().anyMatch(val -> serviceIds.contains(val));
		return verifyPaymentIntiation;
	}

	@Override
	public List<PermitDetailsDTO> saveSurrendOfPermit(List<PermitDetailsVO> permitDetailsVOs) {
		List<String> permitNumList = permitDetailsVOs.stream().map(PermitDetailsVO::getPermitNo)
				.collect(Collectors.toList());
		List<PermitDetailsDTO> permitDetailsDTOList = permitDetailsDAO.findByPermitNoIn(permitNumList);
		permitDetailsDTOList.stream().forEach(u -> {
			u.setPermitStatus(PermitsEnum.INACTIVE.getDescription());
			u.setPermitSurrenderDate(LocalDate.now());
			u.setPermitSurrender(Boolean.TRUE);
		});
		return permitDetailsDTOList;
	}

	@Override
	public List<PermitDetailsVO> getListOfPermitRecords(String prNo, String typeofPermit) {
		List<PermitDetailsDTO> permitDetailsDTO = null;
		if (PermitType.PRIMARY.getPermitTypeCode().equalsIgnoreCase(typeofPermit)) {
			permitDetailsDTO = permitDetailsDAO.findByPrNoAndPermitStatus(prNo, PermitsEnum.ACTIVE.getDescription());
		}
		/* validation for get only temporary permit records */
		if (typeofPermit.equalsIgnoreCase(PermitType.TEMPORARY.getPermitTypeCode())) {
			permitDetailsDTO = permitDetailsDAO.findByPrNoInAndPermitClassCodeAndPermitStatus(Arrays.asList(prNo),
					typeofPermit, PermitsEnum.ACTIVE.getDescription());
		}
		if (CollectionUtils.isEmpty(permitDetailsDTO)) {
			logger.error("Permit details are not Active with the Pr No:[{}]", prNo);
			throw new BadRequestException(" Permit details are not Active with the Pr No " + prNo);
		}
		return permitDetailsMapper.convertEntity(permitDetailsDTO);
	}

	@Override
	public Optional<PermitValidationsVO> getPermitVariationBasedOnCov(String prNo) {

		Optional<RegistrationDetailsDTO> regDetailsOptional = registrationDetailDAO.findByPrNo(prNo);
		if (!regDetailsOptional.isPresent()) {
			throw new BadRequestException("No record found Based on prNo [" + prNo + "]");
		}
		RegistrationDetailsDTO regDTO = regDetailsOptional.get();
		Optional<PermitValidationsDTO> permitValidationOptional = permitValidationsDAO
				.findByCovListIn(regDTO.getClassOfVehicle());
		if (!permitValidationOptional.isPresent()) {
			throw new BadRequestException(
					"No record found in Permit Validations Based on cov [" + regDTO.getClassOfVehicle() + "]");
		}
		PermitValidationsDTO permitValidationDTO = permitValidationOptional.get();
		PermitValidationsVO permitValidationsVO = permitValidationsMapper.variationTypeMapper(permitValidationDTO);
		return Optional.of(permitValidationsVO);
	}

	private PermitDetailsDTO saveForExtentionOfValidity(RegServiceDTO regServiceDTO) {

		Optional<PermitDetailsDTO> permitDetailsOptional = permitDetailsDAO.findByPrNoAndPermitNo(
				regServiceDTO.getPrNo(), regServiceDTO.getPermitDetailsListDTO().get(0).getPermitNo());
		PermitDetailsDTO permitDetails = permitDetailsOptional.get();
		if (permitDetailsOptional.isPresent()) {
			PermitValidityDetailsDTO permitValidityDetailsDTO = permitDetailsOptional.get().getPermitValidityDetails();
			permitValidityDetailsDTO
					.setPermitValidTo(permitValidityDetailsDTO.getPermitValidTo().plusDays(Long.parseLong(regServiceDTO
							.getPermitDetailsListDTO().get(0).getPermitValidityDetails().getExtentionDays())));
			permitDetails.setPermitValidityDetails(permitValidityDetailsDTO);
		}
		return permitDetails;
	}

	@Override
	public Optional<PermitDetailsVO> vehicleReplaceValidations(String permitPrNo, String nonPermitPrNo) {
		Optional<RegistrationDetailsDTO> nonPermitRegOptional = registrationDetailDAO.findByPrNo(nonPermitPrNo);
		Optional<RegistrationDetailsDTO> permitRegOptional = registrationDetailDAO.findByPrNo(permitPrNo);

		PermitDetailsVO permitDetailsVO = new PermitDetailsVO();
		if (!nonPermitRegOptional.isPresent() || !permitRegOptional.isPresent()) {
			logger.error("no record found with prNo :[{}]", permitPrNo);
			throw new BadRequestException("no record found with prNo" + permitPrNo);
		}

		PermitDetailsDTO permitDetailsDTO = fetchPermitDetails(permitPrNo);

		RegistrationDetailsDTO nonPermitDetails = nonPermitRegOptional.get();
		RegistrationDetailsDTO regPermitDetails = permitRegOptional.get();

		VahanDetailsDTO permitVahanDetails = regPermitDetails.getVahanDetails();
		VahanDetailsDTO nonPermitVahanDetails = nonPermitDetails.getVahanDetails();

		ApplicantDetailsDTO permitApplicantDetails = regPermitDetails.getApplicantDetails();
		ApplicantDetailsDTO nonPermitApplicantDetails = nonPermitDetails.getApplicantDetails();

		if (!permitApplicantDetails.getIsAadhaarValidated() && !nonPermitApplicantDetails.getIsAadhaarValidated()) {
			throw new BadRequestException("Please use aadhar seeding service");
		}

		validateRegistrationServicesValidationForNewPrNo(nonPermitPrNo);

		if (!regPermitDetails.getPrNo().equalsIgnoreCase(nonPermitPrNo)) {
			if (StringUtils.isBlank(regPermitDetails.getClassOfVehicle())
					|| StringUtils.isBlank(nonPermitDetails.getClassOfVehicle())) {
				logger.error("Empty Class of Vehicle");
				throw new BadRequestException("Class of Vehicle Not Found");
			}

			// Office Details Validation for two pr numbers
			if (StringUtils.isNotBlank(regPermitDetails.getOfficeDetails().getOfficeCode())
					&& StringUtils.isNotBlank(nonPermitDetails.getOfficeDetails().getOfficeCode())) {
				if (!regPermitDetails.getOfficeDetails().getOfficeCode()
						.equalsIgnoreCase(nonPermitDetails.getOfficeDetails().getOfficeCode())) {
					logger.error("No match found for officeCode with given office codes as [{}] and [{}]",
							regPermitDetails.getOfficeDetails().getOfficeCode(),
							nonPermitDetails.getOfficeDetails().getOfficeCode());
					throw new BadRequestException("No match found for officeCode ");
				}
			} else {
				throw new BadRequestException("Office Details Not Found ");
			}

			// Applicant Details Validation

			if (StringUtils.isBlank(nonPermitApplicantDetails.getAadharNo())
					|| (StringUtils.isBlank(permitApplicantDetails.getAadharNo()))
					|| StringUtils.isBlank(permitApplicantDetails.getFatherName())
					|| StringUtils.isBlank(nonPermitApplicantDetails.getFatherName())
					|| StringUtils.isBlank(permitApplicantDetails.getFirstName())
					|| StringUtils.isBlank(nonPermitApplicantDetails.getFirstName())) {
				logger.error("Empty Applicate Details [{}] [{}]", nonPermitApplicantDetails, permitApplicantDetails);
				throw new BadRequestException("Applicate Details Not Found ");
			}

			if (!permitApplicantDetails.getAadharNo().equals(nonPermitApplicantDetails.getAadharNo())) {
				logger.error("No match found for AadhaarNo: [{}] ,[{}]", permitApplicantDetails.getAadharNo(),
						nonPermitApplicantDetails.getAadharNo());
				throw new BadRequestException("No match found for AadhaarNo ");
			}

			if (!regPermitDetails.getOwnerType().equals(nonPermitDetails.getOwnerType())) {
				logger.error("No match found for Ownership Type : [{}], [{}]", regPermitDetails.getOwnerType(),
						nonPermitDetails.getOwnerType());
				throw new BadRequestException("No match found for Ownership Type");
			}

			// Vahan Details Validation
			if (!regPermitDetails.getClassOfVehicle().equalsIgnoreCase(nonPermitDetails.getClassOfVehicle())) {
				logger.error("No match found for Class of Vehicle:with [{}] and [{}]",
						regPermitDetails.getClassOfVehicle(), nonPermitDetails.getClassOfVehicle());
				throw new BadRequestException("No match found for Class of Vehicle");
			}

			if ((permitVahanDetails.getGvw() > nonPermitVahanDetails.getGvw())) {
				logger.error("Permit vehicle GVW must be less than or Equal NonPermit Vehicle :[{}] > [{}]",
						permitVahanDetails.getGvw(), nonPermitVahanDetails.getGvw());
				throw new BadRequestException("Permit vehicle GVW must be less than or Equal NonPermit Vehicle");
			}

			if ((permitVahanDetails.getUnladenWeight() > nonPermitVahanDetails.getUnladenWeight())) {
				logger.error("Permit vehicle ULW must be less than or Equal NonPermit Vehicle :[{}]> [{}]",
						permitVahanDetails.getUnladenWeight(), nonPermitVahanDetails.getUnladenWeight());
				throw new BadRequestException("Permit vehicle ULW must be less than or Equal NonPermit Vehicle");
			}

			if (Integer.parseInt(permitVahanDetails.getSeatingCapacity()) > Integer
					.parseInt(nonPermitVahanDetails.getSeatingCapacity())) {
				logger.error("Permit vehicle seating  must be less than or Equal NonPermit Vehicle :[{}] > [{}]",
						permitVahanDetails.getSeatingCapacity(), nonPermitVahanDetails.getSeatingCapacity());
				throw new BadRequestException("Permit vehicle seating  must be less than or Equal NonPermit Vehicle");
			}

			if (verifyNameParameter(permitApplicantDetails.getFirstName(),
					nonPermitApplicantDetails.getFirstName()) != 2) {
				logger.error("No match found for Applicants firstName:{}", permitApplicantDetails.getFirstName());
				throw new BadRequestException("No match found for Applicants firstName");
			}

			if (verifyNameParameter(permitApplicantDetails.getFatherName(),
					nonPermitApplicantDetails.getFatherName()) != 2) {
				logger.error("No match found for Applicants fatherName:{}", permitApplicantDetails.getFatherName());
				throw new BadRequestException("No match found for Applicants fatherName");
			}
		}

		permitDetailsDTO.setNonPermitPrNo(nonPermitPrNo);
		permitDetailsVO = permitDetailsMapper.convertEntity(permitDetailsDTO);
		return Optional.of(permitDetailsVO);
	}

	private PermitDetailsDTO saveForRelacementOfVehicle(RegServiceDTO regServiceDTO) {

		PermitDetailsDTO permitDetailsDTO = new PermitDetailsDTO();

		Optional<PermitDetailsDTO> permitDetailsOptional = permitDetailsDAO
				.findByPrNoAndPermitNo(regServiceDTO.getPrNo(), regServiceDTO.getPdtl().getPermitNo());

		PermitDetailsDTO permitDetails = permitDetailsOptional.get();

		if (permitDetailsOptional.isPresent()
				&& (regServiceDTO.getPrNo().equalsIgnoreCase(permitDetailsOptional.get().getPrNo()))) {
			regServiceDTO.getPdtl();
			permitDetails.setPermitStatus(PermitsEnum.INACTIVE.getDescription());
			permitDetails.setIsRelacementOfVehicle(Boolean.TRUE);
			permitDetails.setRelacementDate(LocalDate.now());
			permitDetails.setReplacedToPrNo(regServiceDTO.getPdtl().getNonPermitPrNo());
			permitDetailsDAO.save(permitDetails);
		}

		Optional<RegistrationDetailsDTO> regDetails = commonMethodToGetRegDetails(
				regServiceDTO.getPdtl().getNonPermitPrNo());

		RegistrationDetailsDTO regDetailsDto = regDetails.get();
		permitDetailsDTO = regServiceDTO.getPdtl();
		permitDetailsDTO.setId(null);
		permitDetailsDTO.setPrNo(regServiceDTO.getPdtl().getNonPermitPrNo());
		permitDetailsDTO.setRdto(regDetailsDto);
		permitDetailsDTO.setPermitStatus(PermitsEnum.ACTIVE.getDescription());
		permitDetailsDTO.setCreatedDate(LocalDateTime.now());
		permitDetailsDTO.setCreatedDateStr(LocalDateTime.now().toString());
		return permitDetailsDTO;
	}

	@Override
	public Optional<PermitDetailsVO> findSecondPermitActiveRecords(String prNo) {
		List<PermitDetailsDTO> permitDetailsDTO = permitDetailsDAO.findByPrNo(prNo);
		if (CollectionUtils.isNotEmpty(permitDetailsDTO)) {
			throw new BadRequestException(
					"Permit Details Found ,You are not eligible to apply RelacementOfVehicle " + prNo);
		}
		return Optional.empty();
	}

	@Override
	public Optional<PermitDetailsVO> findTemporayPermitInactiveRecords(String prNo) {

		Optional<PermitDetailsDTO> permitDetailsDTO = permitDetailsDAO
				.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(prNo, PermitType.TEMPORARY.getPermitTypeCode(),
						PermitsEnum.ACTIVE.getDescription());
		if (!permitDetailsDTO.isPresent()) {
			throw new BadRequestException("Temporay Permit Details are Not Found " + prNo);
		}
		LocalDate prValidity = permitDetailsDTO.get().getPermitValidityDetails().getPermitValidTo();
		if (prValidity.isBefore(LocalDate.now())) {
			throw new BadRequestException("Permit validity is expired  " + prNo);
		}
		return permitDetailsMapper.convertEntity(permitDetailsDTO);
	}

	@Override
	public void checExtensionValidtiykWithTaxExpairyDays(PermitDetailsVO permitDetailsVO, String prNo) {
		Optional<TaxDetailsDTO> taxDto = null;
		PermitValidityDetailsVO permitValidityDetailsVO = new PermitValidityDetailsVO();
		taxDto = registrationService.getLatestTaxTransaction(prNo);
		if (!taxDto.isPresent()) {
			throw new BadRequestException("Tax Details are not found for PR no [" + prNo + " ]");
		}
		permitValidityDetailsVO = permitDetailsVO.getPermitValidityDetailsVO();
		permitValidityDetailsVO.setPermitValidTo(permitValidityDetailsVO.getPermitValidTo()
				.plusDays(Long.parseLong(permitDetailsVO.getPermitValidityDetailsVO().getExtentionDays())));
		LocalDate extendays = permitValidityDetailsVO.getPermitValidTo();
		LocalDate taxdays = taxDto.get().getTaxPeriodEnd().plusMonths(1);
		if (taxdays.isBefore(extendays)) {
			throw new BadRequestException("Your tax will expire on [" + taxDto.get().getTaxPeriodEnd()
					+ " You are not eligible to Extented days ");
		}
	}

	private Optional<PermitDetailsDTO> setPermitDetailsForRegDetatils(String prNo) {
		return permitDetailsDAO.findByPrNoAndPermitStatusAndPermitTypeTypeofPermitOrderByCreatedDateDesc(prNo,
				PermitsEnum.ACTIVE.getDescription(), PermitType.PRIMARY.getPermitTypeCode());
	}

	@Override
	public void checkForExtensionOfValidity(PermitDetailsVO permitVO) {
		if (!("07".equalsIgnoreCase(permitVO.getPermitValidityDetailsVO().getExtentionDays()))
				&& !("10".equalsIgnoreCase(permitVO.getPermitValidityDetailsVO().getExtentionDays()))) {
			throw new BadRequestException("Invalid Extension Of Days");

		}
	}

	// As per Murthy Sir inputs added validation to remove spacces and
	// s/o|f/o|w/o|c/o
	public int verifyNameParameter(String perName, String regName) {
		perName = removeStopWords(perName.toLowerCase()).replaceAll("[^a-zA-Z ]+|\\s{2,}", " ").trim();
		regName = removeStopWords(regName.toLowerCase()).replaceAll("[^a-zA-Z ]+|\\s{2,}", " ").trim();
		if (perName.equalsIgnoreCase(regName)) {
			return 2;
		}
		return 0;
	}

	public String removeStopWords(String input) {
		input = input.replaceAll("s/o|f/o|w/o|c/o", "");
		input = input.trim();
		String strinput[] = input.split(" ");
		StringBuilder stringBuilder = new StringBuilder();
		for (String string : strinput) {

			stringBuilder.append(string);
		}
		input = stringBuilder.toString();
		return input;
	}

	@Override
	public List<String> getStatesForCounterSignature(String permitClass) {
		List<StateWisePermitsAvailabilityDTO> states = stateWisePermitsAvailabilityDAO.findByStatusTrue();
		if (CollectionUtils.isEmpty(states)) {
			logger.error("Currently States are not avilable [{}]", states);
			throw new BadRequestException("Currently States are not avilable");
		}
		return states.stream().map(v -> v.getStateName()).collect(Collectors.toList());
	}

	public List<PermitDetailsVO> findRlStateWiseRecords(String prNo) {
		List<PermitDetailsDTO> dtosList = permitDetailsDAO.findByPrNoAndPermitStatusAndIsRecommendationLetterTrue(prNo,
				PermitsEnum.ACTIVE.getDescription());
		List<PermitDetailsDTO> dtosListForUI = new ArrayList<>();
		for (PermitDetailsDTO permitDetailsDTO : dtosList) {
			Long days = ChronoUnit.DAYS.between(permitDetailsDTO.getPermitValidityDetails().getPermitValidTo(),
					LocalDate.now());
			if (days < 15) {
				dtosListForUI.add(permitDetailsDTO);
			}
		}
		if (dtosListForUI.isEmpty()) {
			return Collections.emptyList();
		}
		return permitDetailsMapper.convertEntity(dtosListForUI);
	}

	@Override
	public List<Integer> getNoOfMonthsForAllIndiaPermit() {
		Optional<PropertiesDTO> dtos = propertiesDAO.findByPermitCodeTrue();
		if (!dtos.isPresent()) {
			return Collections.emptyList();
		}
		return dtos.get().getNoOfMonthsForAllIndiaPermit();
	}

	@Override
	public Optional<TPDetailsSearchVO> getPermitdetailsForPassengerList(ApplicationSearchVO applicationSearchVO) {
		if (StringUtils.isBlank(applicationSearchVO.getPrNo())
				&& StringUtils.isBlank(applicationSearchVO.getPermitNo())) {
			logger.error("Empty Pr No or permit No");
			throw new BadRequestException("Pr number or permit number is missing");
		}
		TPDetailsSearchVO vo = new TPDetailsSearchVO();
		Optional<TaxDetailsDTO> taxDto = null;
		Optional<PermitDetailsDTO> primaryPermitDetails = null;
		Optional<PermitDetailsDTO> temporaryPermitDetails = null;
		FcDetailsDTO fcDetails = null;
		List<FcDetailsDTO> fcDetailsOpt = fcDetailsDAO
				.findFirst5ByStatusIsTrueAndPrNoOrderByCreatedDateDesc(applicationSearchVO.getPrNo());
		if (CollectionUtils.isNotEmpty(fcDetailsOpt)) {
			fcDetails = fcDetailsOpt.stream().findFirst().get();
		}
		taxDto = registrationService.getLatestTaxTransaction(applicationSearchVO.getPrNo());
		if (fcDetails != null) {
			vo.setFcNumber(fcDetails.getFcNumber());
			vo.setFcValidUpto(fcDetails.getFcValidUpto());
		}
		if (taxDto.isPresent()) {
			vo.setTaxAmount(taxDto.get().getTaxAmount());
			vo.setTaxValidTill(taxDto.get().getTaxPeriodEnd());
		}
		primaryPermitDetails = permitDetailsDAO
				.findByPrNoAndPermitStatusAndPermitTypeTypeofPermitOrderByCreatedDateDesc(applicationSearchVO.getPrNo(),
						PermitsEnum.ACTIVE.getDescription(), PermitType.PRIMARY.getPermitTypeCode());

		temporaryPermitDetails = permitDetailsDAO
				.findByPrNoAndPermitStatusAndPermitTypeTypeofPermitOrderByCreatedDateDesc(applicationSearchVO.getPrNo(),
						PermitsEnum.ACTIVE.getDescription(), PermitType.TEMPORARY.getPermitTypeCode());
		if (temporaryPermitDetails.isPresent() && LocalDate.now()
				.isAfter(temporaryPermitDetails.get().getPermitValidityDetails().getPermitValidTo())) {
			logger.error("Permit is Expaired please apply for the new permit");
			throw new BadRequestException("Permit is Expaired please apply for the new permit");
		}
		if (primaryPermitDetails.isPresent() && temporaryPermitDetails.isPresent()) {
			vo.setPermitnumber(primaryPermitDetails.get().getPermitNo());
			vo.setPermitValidUpto(primaryPermitDetails.get().getPermitValidityDetails().getPermitValidTo());
			vo.setSeatingCapacity(temporaryPermitDetails.get().getRdto().getVehicleDetails().getSeatingCapacity());
			vo.setPermitId(temporaryPermitDetails.get().getId());
			vo.setTemporaryPermitNumber(temporaryPermitDetails.get().getPermitNo());
		}

		return Optional.of(vo);
	}

	@Override
	public void savePassengerListForTP(TemporaryPermitPassengerDetailsVO temporaryPermitPassengerDetailsVO) {
		TemporaryPermitPassengerDetailsDTO dto = temporaryPermitPassengerDetailsMapper
				.convertVO(temporaryPermitPassengerDetailsVO);
		dto.setCreatedDate(LocalDateTime.now());
		dto.setStatus(PermitsEnum.ACTIVE.getDescription());
		temporaryPermitPassengerDetailsDAO.save(dto);
	}

	@Override
	public void updatePermitDetailsAfterReassignment(RegistrationDetailsDTO registrationDetailsDTO) {
		if (StringUtils.isNotEmpty(registrationDetailsDTO.getOldPrNo())) {
			List<PermitDetailsDTO> permitDetails = permitDetailsDAO.findByPrNoAndPermitStatus(
					registrationDetailsDTO.getOldPrNo(), PermitsEnum.ACTIVE.getDescription());
			List<PermitDetailsDTO> saveList = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(permitDetails)) {
				permitDetails.stream().forEach(pdtl -> {
					if (pdtl.getPermitClass() != null && StringUtils.isNotEmpty(pdtl.getPermitClass().getCode()) && pdtl
							.getPermitClass().getCode().equalsIgnoreCase(PermitType.PRIMARY.getPermitTypeCode())) {
						pdtl.setPrNo(registrationDetailsDTO.getPrNo());
						pdtl.setRdto(registrationDetailsDTO);
						saveList.add(pdtl);
					} else {
						pdtl.setPermitStatus(PermitsEnum.INACTIVE.getDescription());
						saveList.add(pdtl);
					}
				});
				permitDetailsDAO.save(saveList);
			}
		}
	}

	/**
	 * Getting the details for Other state based on Pr No
	 * 
	 * -> Trying to fetch the details from Vahan Initially. -> If data is not
	 * available in Vahan Later we are searching into our application -> Then also
	 * data is not available returning null so he will enter manually all the
	 * details.
	 * 
	 * 
	 */

	@Override
	public Optional<OtherStateTemporaryPermitDetailsVO> getOtherStateTPDetails(String prNo) {

		OtherStateTemporaryPermitDetailsVO vo = new OtherStateTemporaryPermitDetailsVO();
		boolean validatePrno = commanValidatorForPrNo.prNumberValidator(prNo);
		if (!validatePrno) {
			throw new BadRequestException("invalid pr number..!");
		}
		try {
			Optional<VahanVehicleDetailsVO> vahanDetails = restGateWayService.getVahanVehicleDetails(prNo);
			if (vahanDetails.isPresent()) {
				vo = otherStateTemporaryPermitDetailsMapper.convertVahanDataToOtherStateTP(vahanDetails.get(), prNo);
			}
		} catch (Exception e) {
			logger.error("Getting issue while fetching the data from vahan and convertion for PR number [{}]", prNo);
		}

		if (StringUtils.isBlank(vo.getPrNo())) {
			Optional<OtherStateTemporaryPermitDetailsDTO> dto = otherStateTemporaryPermitDetailsDAO
					.findByPrNoOrderByCreatedDateDesc(prNo);
			if (dto.isPresent()) {
				if (dto.get().getTemporaryPermitDetails().getPermitValidityDetails().getPermitValidTo()
						.isAfter(LocalDate.now())) {
					throw new BadRequestException("Your previous permit is not expaired");
				}
				vo = otherStateTemporaryPermitDetailsMapper.convertEntity(dto.get());
				vo.setClassOfVehicleVO(Arrays.asList(masterCovMapper
						.convertEntity(masterCovDAO.findByCovcode(vo.getVehicleDetails().getClassOfVehicle()))));
				return Optional.of(vo);
			}
			if (!dto.isPresent()) {
				try {
					Optional<RegistrationDetailsDTO> regDTO = registrationDetailDAO.findByPrNo(prNo);
					vo = setHomeStateDetailsAtCheckPostServices(prNo, regDTO.get());
				} catch (Exception e) {
					logger.error("Getting issue while fetching the data registration details [{}]", prNo);
				}
				if (vo != null) {
					return Optional.of(vo);
				}
			}
			return Optional.empty();
		}
		return Optional.of(vo);
	}

	@Override
	public void saveOtherStateDetailsForTemporaryPermit(String applicationNo, TransactionDetailVO transactionDetailVO) {
		Optional<RegServiceDTO> regServiceDto = regServiceDAO.findByApplicationNo(applicationNo);

		FeeDetailsDTO feeDto = savePaymentDetailsInPermitDocument(applicationNo);
		if (!regServiceDto.isPresent()) {
			throw new BadRequestException("No records Found");
		}

		RegServiceDTO dto = regServiceDto.get();
		OtherStateTemporaryPermitDetailsDTO osTPDto = dto.getOtherStateTemporaryPermitDetails();
		osTPDto.setPrNo(dto.getPrNo());
		if (osTPDto.getTemporaryPermitDetails() != null && dto.getOfficeDetails() != null
				&& StringUtils.isNotBlank(dto.getOfficeDetails().getOfficeCode())) {
			osTPDto.getTemporaryPermitDetails().setPermitNo(
					generatePermitNo(dto.getOfficeDetails().getOfficeCode(), dto.getOtherStateTemporaryPermitDetails()
							.getTemporaryPermitDetails().getPermitType().getNumberCode()));
		} else {
			osTPDto.getTemporaryPermitDetails()
					.setPermitNo(generatePermitNo("APSTA", dto.getOtherStateTemporaryPermitDetails()
							.getTemporaryPermitDetails().getPermitType().getNumberCode()));
		}

		PermitValidityDetailsDTO permitValidityDetailsDTO = new PermitValidityDetailsDTO();
		if (osTPDto.getTemporaryPermitDetails().getRouteDetails() != null
				&& StringUtils.isNotEmpty(osTPDto.getTemporaryPermitDetails().getRouteDetails().getNoOfDays())) {
			if (osTPDto.getTemporaryPermitDetails().getRouteDetails().getNoOfDays()
					.equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.SevenDays.getDesc())) {
				permitValidityDetailsDTO.setPermitValidFrom(LocalDate.now());
				permitValidityDetailsDTO.setPermitValidTo(LocalDate.now().plusDays(6));
			} else if (osTPDto.getTemporaryPermitDetails().getRouteDetails().getNoOfDays()
					.equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.ThirtyDays.getDesc())) {
				permitValidityDetailsDTO.setPermitValidFrom(LocalDate.now());
				permitValidityDetailsDTO.setPermitValidTo(LocalDate.now().plusDays(29));
			} else {
				permitValidityDetailsDTO.setPermitValidFrom(
						osTPDto.getTemporaryPermitDetails().getRouteDetails().getForwardRouteDate());
				permitValidityDetailsDTO
						.setPermitValidTo(osTPDto.getTemporaryPermitDetails().getRouteDetails().getReturnRouteDate());
				osTPDto.getTemporaryPermitDetails().setPermitValidityDetails(permitValidityDetailsDTO);
			}
		}
		osTPDto.getTemporaryPermitDetails().setPermitValidityDetails(permitValidityDetailsDTO);

		osTPDto.setApplicationStatus(StatusRegistration.APPROVED);
		osTPDto.setCreatedBy(dto.getCreatedBy());
		osTPDto.setCreatedDate(LocalDateTime.now());
		osTPDto.setCreatedDateStr(LocalDateTime.now().toString());

		dto.setApplicationStatus(StatusRegistration.APPROVED);
		dto.setCurrentIndex(RoleEnum.RTO.getIndex() + 1);
		if (feeDto != null) {
			osTPDto.setFeeDetails(feeDto);
			dto.setFeeDetails(feeDto);
		}
		otherStateTemporaryPermitDetailsDAO.save(osTPDto);
		regServiceDAO.save(dto);
		transactionDetailVO.setCreatedDate(dto.getCreatedDate().toLocalDate());
		transactionDetailVO.setCreatedTime(dto.getCreatedDate().toLocalTime());
		transactionDetailVO.setPermitNumber(osTPDto.getTemporaryPermitDetails().getPermitNo());
		transactionDetailVO.setIssuedBy(osTPDto.getCreatedBy());
		transactionDetailVO.setValidFrom(permitValidityDetailsDTO.getPermitValidFrom());
		transactionDetailVO.setValidTo(permitValidityDetailsDTO.getPermitValidTo());
		if (osTPDto.getTemporaryPermitDetails() != null && osTPDto.getTemporaryPermitDetails().getRouteDetails() != null
				&& StringUtils.isNotBlank(osTPDto.getTemporaryPermitDetails().getRouteDetails().getForwardRoute())
				&& StringUtils.isNotBlank(osTPDto.getTemporaryPermitDetails().getRouteDetails().getReturnRoute())) {
			transactionDetailVO
					.setForwardRoute(osTPDto.getTemporaryPermitDetails().getRouteDetails().getForwardRoute());
			transactionDetailVO.setReturnRoute(osTPDto.getTemporaryPermitDetails().getRouteDetails().getReturnRoute());
		}
	}

	/**
	 * Get Latest payment record Based on application Number
	 * 
	 * @param applicationNo
	 * @return
	 */
	private FeeDetailsDTO savePaymentDetailsInPermitDocument(String applicationNo) {

		List<PaymentTransactionDTO> paymentList = paymentTransactionDAO.findByApplicationFormRefNum(applicationNo);
		if (paymentList != null && paymentList.size() > 0) {
			paymentList.sort((o1, o2) -> o2.getRequest().getRequestTime().compareTo(o1.getRequest().getRequestTime()));
			return paymentList.get(0).getFeeDetailsDTO();
		}
		return null;
	}

	@Override
	public PermitValidityDetailsVO setDatesForOtheStateTPAndSP(String tpValidity) {
		PermitValidityDetailsVO vo = new PermitValidityDetailsVO();
		if (tpValidity.equalsIgnoreCase(VoluntaryTaxType.SevenDays.getDesc())) {
			vo.setPermitValidFrom(LocalDate.now());
			vo.setPermitValidTo(LocalDate.now().plusDays(6));
		} else if (tpValidity.equalsIgnoreCase(VoluntaryTaxType.ThirtyDays.getDesc())) {
			vo.setPermitValidFrom(LocalDate.now());
			vo.setPermitValidTo(LocalDate.now().plusDays(29));
		}

		return vo;
	}

	@Override
	public void doValidateBeforeSaveOfTPandSP(RegServiceVO inputRegServiceVO) {
		checkforPendingApplicationsinRegistrationServices(inputRegServiceVO.getPrNo());
		if (inputRegServiceVO.getOtherStateTemporaryPermit() != null) {
			if (inputRegServiceVO.getOtherStateTemporaryPermit().getTaxDetails() != null
					&& inputRegServiceVO.getOtherStateTemporaryPermit().getTaxDetails().getTaxValTo() != null) {
				if (inputRegServiceVO.getOtherStateTemporaryPermit().getTaxDetails().getTaxValTo()
						.isBefore(LocalDate.now())) {
					throw new BadRequestException("Tax Validity is expaired");
				}
			}

			if (inputRegServiceVO.getOtherStateTemporaryPermit().getTaxDetails() != null && inputRegServiceVO
					.getOtherStateTemporaryPermit().getTaxDetails().getOtherStateTaxValUpto() != null) {
				if (inputRegServiceVO.getOtherStateTemporaryPermit().getTaxDetails().getOtherStateTaxValUpto()
						.isBefore(LocalDate.now())) {
					throw new BadRequestException("Tax Validity is expaired");
				}
			}

			if (inputRegServiceVO.getOtherStateTemporaryPermit().getFcDetails() != null
					&& inputRegServiceVO.getOtherStateTemporaryPermit().getFcDetails().getFcValidUpto() != null) {
				if (inputRegServiceVO.getOtherStateTemporaryPermit().getFcDetails().getFcValidUpto()
						.isBefore(LocalDate.now())) {
					throw new BadRequestException("FC Validity is expaired");
				}
			}

			if (inputRegServiceVO.getOtherStateTemporaryPermit().getPrimaryPermitDetails() != null
					&& inputRegServiceVO.getOtherStateTemporaryPermit().getPrimaryPermitDetails()
							.getPermitValidityDetailsVO() != null
					&& inputRegServiceVO.getOtherStateTemporaryPermit().getPrimaryPermitDetails()
							.getPermitValidityDetailsVO().getPermitValidTo() != null) {
				if (inputRegServiceVO.getOtherStateTemporaryPermit().getPrimaryPermitDetails()
						.getPermitValidityDetailsVO().getPermitValidTo().isBefore(LocalDate.now())) {
					throw new BadRequestException("Home State Permit Validity is expaired");
				}
			}
		}
	}

	private void checkforPendingApplicationsinRegistrationServices(String prNo) {
		List<RegServiceDTO> registrationServicesList = regServiceDAO.findByPrNo(prNo);
		if (CollectionUtils.isNotEmpty(registrationServicesList)) {
			registrationServicesList.sort((s1, s2) -> s2.getCreatedDate().compareTo(s1.getCreatedDate()));
			RegServiceDTO regDto = registrationServicesList.stream().findFirst().get();
			List<StatusRegistration> listOfStatusForPAymnets = new ArrayList<>();
			listOfStatusForPAymnets.add(StatusRegistration.CITIZENPAYMENTFAILED);
			listOfStatusForPAymnets.add(StatusRegistration.PAYMENTPENDING);
			if (listOfStatusForPAymnets.contains(regDto.getApplicationStatus())) {
				throw new BadRequestException(
						"Application is in Pending state.Application No: " + regDto.getApplicationNo());
			}
		}
	}

	private void validateRegistrationServicesValidationForNewPrNo(String nonPermitPrNo) {
		List<RegServiceDTO> registrationServicesList = regServiceDAO.findByPrNo(nonPermitPrNo);
		if (CollectionUtils.isNotEmpty(registrationServicesList)) {
			registrationServicesList.sort((s1, s2) -> s2.getCreatedDate().compareTo(s1.getCreatedDate()));
			RegServiceDTO regDto = registrationServicesList.stream().findFirst().get();

			if (!regDto.getServiceType().stream().anyMatch(val -> val.equals(ServiceEnum.ALTERATIONOFVEHICLE))) {
				throw new BadRequestException("You are not eligible to apply for "
						+ ServiceEnum.REPLACEMENTOFVEHICLE.getDesc() + "with " + nonPermitPrNo);
			}

			if (registrationServicesList.size() >= 2) {
				throw new BadRequestException("You are not eligible to apply for "
						+ ServiceEnum.REPLACEMENTOFVEHICLE.getDesc() + "with " + nonPermitPrNo);
			}
		}
	}

	@Override
	public void saveScrtPermit(RegServiceDTO regServiceDTO) {
		PermitDetailsDTO permitDetailsDTO = null;
		if (regServiceDTO.getPdtl() != null) {
			permitDetailsDTO = regServiceDTO.getPdtl();
		} else {

		}
		List<ServiceEnum> serviceIds = regServiceDTO.getServiceIds().stream()
				.map(id -> ServiceEnum.getServiceEnumById(id)).collect(Collectors.toList());
		String prNo = regServiceDTO.getPrNo();
		RegistrationDetailsDTO registrationDetailsDTO = null;
		synchronized (prNo.intern()) {
			for (ServiceEnum serviceEnum : serviceIds) {
				switch (serviceEnum) {

				case STAGECARRIAGERENEWALOFPERMIT:
					registrationDetailsDTO = findRegistrationDetails(regServiceDTO);
					saveForRenewalPermit(permitDetailsDTO);
					break;
				case STAGECARRIAGEREPLACEMENTOFVEHICLE:
					registrationDetailsDTO = findRegistrationDetails(regServiceDTO);
					permitDetailsDTO = saveScrtReplacementOfVehicle(regServiceDTO);
					break;
				default:
					break;
				}
			}
			if (regServiceDTO.getServiceIds() != null && regServiceDTO.getServiceType() != null) {
				permitDetailsDTO.setServiceIds(regServiceDTO.getServiceIds());
				permitDetailsDTO.setServiceType(regServiceDTO.getServiceType());
			}

		}
		regServiceDTO.setApplicationStatus(StatusRegistration.APPROVED);
		regServiceDTO.setCurrentIndex(RoleEnum.RTO.getIndex() + 1);
		regServiceDTO.setCurrentRoles(null);
		if (registrationDetailsDTO != null) {
			registratrionServicesApprovals.updateTaxDetailsInRegCollection(regServiceDTO, registrationDetailsDTO);
			registrationDetailsDTO.setPermitDetails(permitDetailsDTO);
			registrationDetailsDTO.setIsvahanSync(Boolean.FALSE);
			registrationDetailsDTO.setIsvahanSyncSkip(Boolean.FALSE);
			vahanSync.commonVahansync(registrationDetailsDTO);
			registrationDetailDAO.save(registrationDetailsDTO);
		}
		permitDetailsDAO.save(permitDetailsDTO);
		// regServiceDAO.save(regServiceDTO);
		// permitDetailsDTO.setRdto(null);

	}

	private PermitDetailsDTO saveScrtReplacementOfVehicle(RegServiceDTO regServiceDTO) {
		// replacedToPrNo
		stageCarriageServices.validationForScrtReplacementOfVehicle(regServiceDTO.getPrNo(),
				regServiceDTO.getPdtl().getPermitVehiclePrNo());
		return saveRelacementOfVehicleForSCRT(regServiceDTO);
	}

	private PermitDetailsDTO saveRelacementOfVehicleForSCRT(RegServiceDTO regServiceDTO) {

		PermitDetailsDTO permitDetailsDTO = new PermitDetailsDTO();

		Optional<PermitDetailsDTO> permitDetailsOptional = permitDetailsDAO.findByPrNoAndPermitNo(
				regServiceDTO.getPdtl().getPermitVehiclePrNo(), regServiceDTO.getPdtl().getPermitNo());

		PermitDetailsDTO permitDetails = permitDetailsOptional.get();

		if (permitDetailsOptional.isPresent() && (regServiceDTO.getPdtl().getPermitVehiclePrNo()
				.equalsIgnoreCase(permitDetailsOptional.get().getPrNo()))) {
			regServiceDTO.getPdtl();
			permitDetails.setPermitStatus(PermitsEnum.INACTIVE.getDescription());
			permitDetails.setIsRelacementOfVehicle(Boolean.TRUE);
			permitDetails.setRelacementDate(LocalDate.now());
			permitDetails.setReplacedToPrNo(regServiceDTO.getPrNo());
			permitDetailsDAO.save(permitDetails);
		}

		Optional<RegistrationDetailsDTO> regDetails = commonMethodToGetRegDetails(regServiceDTO.getPrNo());

		RegistrationDetailsDTO regDetailsDto = regDetails.get();
		permitDetailsDTO = regServiceDTO.getPdtl();
		permitDetailsDTO.setId(null);
		permitDetailsDTO.setPrNo(regServiceDTO.getPrNo());
		permitDetailsDTO.setRdto(regDetailsDto);
		permitDetailsDTO.setPermitStatus(PermitsEnum.ACTIVE.getDescription());
		permitDetailsDTO.setCreatedDate(LocalDateTime.now());
		permitDetailsDTO.setCreatedDateStr(LocalDateTime.now().toString());
		return permitDetailsDTO;
	}

	private RegistrationDetailsDTO findRegistrationDetails(RegServiceDTO regServiceDTO) {

		RegistrationDetailsDTO dto = null;

		if (regServiceDTO.getRegistrationDetails() != null) {
			dto = registrationDetailDAO.findOne(regServiceDTO.getRegistrationDetails().getApplicationNo());
		}
		if (dto == null) {
			throw new BadRequestException("Registraion Details not found, registration application no: {}"
					+ regServiceDTO.getRegistrationDetails().getApplicationNo());
		}
		return dto;

	}

	private OtherStateTemporaryPermitDetailsVO setHomeStateDetailsAtCheckPostServices(String prNo,
			RegistrationDetailsDTO regDTO) {
		OtherStateTemporaryPermitDetailsVO vo = new OtherStateTemporaryPermitDetailsVO();
		VehicleDetailsVO vehicleDetailsVO = new VehicleDetailsVO();
		ApplicantDetailsVO applicantDetailsVO = new ApplicantDetailsVO();
		InsuranceDetailsVO insurenceDetailsVO = new InsuranceDetailsVO();
		CfstTaxDetailsVO taxVO = new CfstTaxDetailsVO();
		ApplicantAddressVO addressVo = new ApplicantAddressVO();
		StringBuilder address = new StringBuilder();
		FcDetailsVO fcDetailsVO = new FcDetailsVO();
		Optional<TaxDetailsDTO> taxDTO = registrationService.getLatestTaxTransaction(prNo);

		// Tax Details
		if (taxDTO.isPresent()) {
			taxVO.setOtherStateTaxValUpto(taxDTO.get().getTaxPeriodEnd());
			taxVO.setTaxValTo(taxDTO.get().getTaxPeriodEnd());
			vo.setTaxDetails(taxVO);
		}

		// Insurence Details
		if (regDTO.getInsuranceDetails() != null) {
			funPoint(regDTO.getInsuranceDetails().getPolicyNumber(), insurenceDetailsVO::setPolicyNumber);
			funPoint(regDTO.getInsuranceDetails().getValidTill(), insurenceDetailsVO::setValidTill);
			vo.setInsuranceDetails(insurenceDetailsVO);
		}

		// VahanDetails
		if (regDTO.getVahanDetails() != null) {

	        String encodeChassisNumber=Base64.encodeBase64String(regDTO.getVahanDetails().getChassisNumber().getBytes());
			String encodeEngineNumber=Base64.encodeBase64String(regDTO.getVahanDetails().getEngineNumber().getBytes());
			funPoint(regDTO.getVahanDetails().getBodyTypeDesc(), vehicleDetailsVO::setBodyTypeDesc);
			funPoint(encodeChassisNumber, vehicleDetailsVO::setChassisNumber);
			funPoint(encodeEngineNumber, vehicleDetailsVO::setEngineNumber);
			funPoint(regDTO.getVahanDetails().getGvw(), vehicleDetailsVO::setRlw);
			funPoint(regDTO.getVahanDetails().getGvw(), vehicleDetailsVO::setGvw);
			funPoint(regDTO.getVahanDetails().getUnladenWeight(), vehicleDetailsVO::setUlw);
			funPoint(regDTO.getVahanDetails().getSeatingCapacity(), vehicleDetailsVO::setSeatingCapacity);
			String chassisNumber = regDTO.getVahanDetails().getChassisNumber();
			String engineNumber = regDTO.getVahanDetails().getChassisNumber();
			vehicleDetailsVO.setModfiedChassisNumber(chassisNumber != null
					? chassisNumber.length() >= 5 ? chassisNumber.substring(0, chassisNumber.length() - 5) + "*****"
							: chassisNumber.replaceAll(chassisNumber, "****")
					: null);
			vehicleDetailsVO.setModfiedEngineNumber(engineNumber != null
					? engineNumber.length() >= 5 ? engineNumber.substring(0, engineNumber.length() - 5) + "*****"
							: engineNumber.replaceAll(engineNumber, "****")
					: null);
			vo.setVehicleDetails(vehicleDetailsVO);
		}
		// Applicant Details
		funPoint(regDTO.getApplicantDetails().getDisplayName(), applicantDetailsVO::setDisplayName);
		if (null != regDTO.getApplicantDetails().getPresentAddress()) {
			ApplicantAddressDTO applicantAddress = regDTO.getApplicantDetails().getPresentAddress();
			if (StringUtils.isNoneBlank(applicantAddress.getDoorNo())) {
				address.append(applicantAddress.getDoorNo()).append(", ");
			}
			if (StringUtils.isNoneBlank(applicantAddress.getStreetName())) {
				address.append(applicantAddress.getStreetName()).append(", ");
			}
			if (null != applicantAddress.getMandal()) {
				if (StringUtils.isNoneBlank(applicantAddress.getMandal().getMandalName())) {
					address.append(applicantAddress.getMandal().getMandalName()).append(", ");
				}
			}
			if (StringUtils.isNoneBlank(applicantAddress.getDistrict().getDistrictName())) {
				address.append(applicantAddress.getDistrict().getDistrictName()).append(", ");
			}
			if (StringUtils.isNoneBlank(applicantAddress.getState().getStateName())) {
				address.append(applicantAddress.getState().getStateName()).append(" - ");
			}
			if (null != applicantAddress.getPostOffice()
					&& null != applicantAddress.getPostOffice().getPostOfficeCode()) {
				address.append(applicantAddress.getPostOffice().getPostOfficeCode());
			}
		}

		addressVo.setDoorNo(address.toString());
		if (regDTO.getApplicantDetails().getPresentAddress().getMandal() != null) {
			addressVo.setMandal(
					mandalMapper.convertEntity(regDTO.getApplicantDetails().getPresentAddress().getMandal()));
		}
		if (regDTO.getApplicantDetails().getPresentAddress().getDistrict() != null) {
			addressVo.setDistrict(
					districtMapper.convertEntity(regDTO.getApplicantDetails().getPresentAddress().getDistrict()));
		}
		if (regDTO.getApplicantDetails().getPresentAddress().getState() != null) {
			addressVo.setState(stateMapper.convertEntity(regDTO.getApplicantDetails().getPresentAddress().getState()));
		}
		applicantDetailsVO.setPresentAddress(addressVo);
		applicantDetailsVO.setPermanantAddress(addressVo);
		vo.setApplicantDetails(applicantDetailsVO);

		funPoint(prNo, vo::setPrNo);
		funPoint(regDTO.getClassOfVehicle(), vo::setClassOfVehicle);

		// FC DETAILS
		List<FcDetailsDTO> fcDetailsOpt = fcDetailsDAO
				.findFirst5ByStatusIsTrueAndPrNoOrderByCreatedDateDesc(regDTO.getPrNo());
		if (!fcDetailsOpt.isEmpty()) {
			fcDetailsOpt.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
			FcDetailsDTO fcDetails = fcDetailsOpt.stream().findFirst().get();
			if (LocalDate.now().isBefore(fcDetails.getFcValidUpto().plusDays(1))) {
				fcDetailsVO.setFcValidUpto(fcDetails.getFcValidUpto());
			}
			if (StringUtils.isNoneBlank(fcDetails.getFcNumber())) {
				fcDetailsVO.setFcNumber(fcDetails.getFcNumber());
			}
			vo.setFcDetails(fcDetailsVO);
		}
		// Permit Details
		Optional<PermitDetailsDTO> primaryPermitDetails = permitDetailsDAO
				.findByPrNoAndPermitStatusAndPermitTypeTypeofPermitOrderByCreatedDateDesc(regDTO.getPrNo(),
						PermitsEnum.ACTIVE.getDescription(), PermitType.PRIMARY.getPermitTypeCode());
		if (primaryPermitDetails != null && primaryPermitDetails.isPresent()) {
			PermitDetailsVO primaryPermitDetailsVO = new PermitDetailsVO();
			if (StringUtils.isNoneBlank(primaryPermitDetails.get().getPermitNo())) {
				primaryPermitDetailsVO.setPermitNo(primaryPermitDetails.get().getPermitNo());
			}
			if (primaryPermitDetails.get().getPermitValidityDetails() != null
					&& primaryPermitDetails.get().getPermitValidityDetails().getPermitValidTo() != null) {
				PermitValidityDetailsVO permitValidityDetailsVO = new PermitValidityDetailsVO();
				permitValidityDetailsVO
						.setPermitValidTo(primaryPermitDetails.get().getPermitValidityDetails().getPermitValidTo());
				primaryPermitDetailsVO.setPermitValidityDetailsVO(permitValidityDetailsVO);
			}

			vo.setPrimaryPermitDetails(primaryPermitDetailsVO);
		}
		if (StringUtils.isNoneBlank(vo.getClassOfVehicle())) {
			vo.setClassOfVehicleVO(
					Arrays.asList(masterCovMapper.convertEntity(masterCovDAO.findByCovcode(vo.getClassOfVehicle()))));
		}
		return vo;
	}

	public <T> void funPoint(T value, Consumer<T> consumer) {
		if (value != null) {
			consumer.accept(value);
		}
	}

	@Override
	public Set<String> getTaxTypesForTPSP(String covCode) {
		Set<String> taxTypes = new HashSet<>();
		taxTypes.add(TaxTypeEnum.VoluntaryTaxType.SevenDays.getDesc());
		Optional<PermitValidationsDTO> validationDTO = permitValidationsDAO
				.findByTaxType(TaxTypeEnum.VoluntaryTaxType.ThirtyDays.getDesc());
		if (validationDTO.isPresent() && validationDTO.get().getCovList().contains(covCode)) {
			taxTypes.add(TaxTypeEnum.VoluntaryTaxType.ThirtyDays.getDesc());
		}
		return taxTypes;
	}

	@Override
	public List<PermitRoutesForSCRTVO> getRoutesForSCRT(String routes) {

		List<PermitRoutesForSCRTVO> routesList = new ArrayList<>();
		List<PermitRoutesForSCRTDTO> routeList = permitRoutesForSCRTDAO.findAll();

		routeList.stream().forEach(dto -> {
			PermitRoutesForSCRTVO vo = new PermitRoutesForSCRTVO();

			vo.setDistrictCode(dto.getDistrictCode());
			vo.setDistrictName(dto.getDistrictName());
			vo.setMandalCode(dto.getMandalCode());
			vo.setMandalName(dto.getMandalName());
			vo.setOfficeCode(dto.getOfficeCode());
			routesList.add(vo);
		});
		Collections.sort(routesList, (o1, o2) -> (o1.getMandalName().compareTo(o2.getMandalName())));
		return routesList;
	}
}
