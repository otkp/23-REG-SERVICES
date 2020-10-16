package org.epragati.master.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.common.dao.PropertiesDAO;
import org.epragati.common.dto.FlowDTO;
import org.epragati.common.dto.PropertiesDTO;
import org.epragati.constants.CovCategory;
import org.epragati.dto.enclosure.ImageEnclosureDTO;
import org.epragati.exception.BadRequestException;
import org.epragati.images.vo.ImageInput;
import org.epragati.master.dao.AlterationDAO;
import org.epragati.master.dao.BodyTypeDAO;
import org.epragati.master.dao.EnclosuresDAO;
import org.epragati.master.dao.MasterCovDAO;
import org.epragati.master.dao.StagingRegistrationDetailsDAO;
import org.epragati.master.dto.BodyTypeDTO;
import org.epragati.master.dto.EnclosuresDTO;
import org.epragati.master.dto.MasterCovDTO;
import org.epragati.master.dto.RoleActionDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.dto.TrailerChassisDetailsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.mappers.BodyTypeMapper;
import org.epragati.master.mappers.EnclosuresMapper;
import org.epragati.master.mappers.MasterCovMapper;
import org.epragati.master.service.CovService;
import org.epragati.master.service.LogMovingService;
import org.epragati.master.service.StagingBodyBuilderService;
import org.epragati.master.service.StagingRegistrationDetailsSerivce;
import org.epragati.master.vo.BodyTypeVO;
import org.epragati.master.vo.MasterCovVO;
import org.epragati.master.vo.OwnerAndVahanInfoVO;
import org.epragati.registration.service.DealerService;
import org.epragati.regservice.dto.AlterationDTO;
import org.epragati.regservice.dto.SlotDetailsDTO;
import org.epragati.regservice.mapper.AlterationMapper;
import org.epragati.regservice.mapper.SlotDetailsMapper;
import org.epragati.regservice.vo.AlterationVO;
import org.epragati.regservice.vo.SlotDetailsVO;
import org.epragati.rta.service.impl.DTOUtilService;
import org.epragati.rta.vo.TrailerChassisDetailsVO;
import org.epragati.sequence.SequenceGenerator;
import org.epragati.service.enclosure.mapper.EnclosureImageMapper;
import org.epragati.service.enclosure.vo.DisplayEnclosures;
import org.epragati.service.enclosure.vo.ImageVO;
import org.epragati.util.RoleEnum;
import org.epragati.util.StatusRegistration;
import org.epragati.util.document.KeyValue;
import org.epragati.util.document.Sequence;
import org.epragati.util.exception.SequenceGenerateException;
import org.epragati.util.payment.ClassOfVehicleEnum;
import org.epragati.util.payment.ServiceCodeEnum;
import org.epragati.util.payment.ServiceEnum;
import org.epragati.vcrImage.dao.VoluntaryTaxDAO;
import org.epragati.vcrImage.dto.VoluntaryTaxDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service class for BodyBuilder app
 * 
 * @author rajesh.vaddi
 *
 */
@Service
public class StagingBodyBuilderServiceImpl implements StagingBodyBuilderService {

	private static final Logger logger = LoggerFactory.getLogger(StagingBodyBuilderServiceImpl.class);

	@Autowired
	private StagingRegistrationDetailsDAO stagingRegistrationDetailsDAO;

	@Autowired
	private MasterCovDAO covDAO;

	@Autowired
	private BodyTypeDAO bodyTypeDAO;

	@Autowired
	private AlterationDAO alterationDAO;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private DTOUtilService dTOUtilService;

	@Autowired
	private AlterationMapper alterationMapper;

	@Autowired
	private EnclosuresDAO enclosuresDAO;

	@Autowired
	private EnclosuresMapper enclosuresMapper;

	@Autowired
	private EnclosureImageMapper enclosureImageMapper;

	@Autowired
	private StagingRegistrationDetailsSerivce stagingRegistrationDetailsSerivce;

	@Autowired
	private SlotDetailsMapper slotDetailsMapper;

	@Autowired
	private SequenceGenerator sequencenGenerator;

	@Autowired
	private CovService covService;

	@Autowired
	private LogMovingService logMovingService;

	@Autowired
	private DealerService dealerService;

	@Autowired
	private VoluntaryTaxDAO voluntaryTaxDAO;
	@Autowired
	private PropertiesDAO propertiesDAO;

	/**
	 * This method fetch owner and vahan details from database and master data to be
	 * used in drop down list in app.
	 * 
	 * @param trNo Temporary Registration number
	 */
	@Override
	public OwnerAndVahanInfoVO findByTrNo(String officecode, String trNo) {
		Boolean isMVIDone = false;
		SlotDetailsVO slotDetailsVO = null;
		Optional<StagingRegistrationDetailsDTO> optionalDto = stagingRegistrationDetailsDAO
				.findByTrNoAndApplicationStatus(trNo, StatusRegistration.SLOTBOOKED.getDescription());
		if (!optionalDto.isPresent()) {
			logger.error("BadRequestException: Registraion Details not Found with Tr Number and Status SlotBooked [{}]",
					trNo);
			throw new BadRequestException("Registraion Details not Found with Tr Number and Status SlotBooked " + trNo);
		}
		// maps owner and vahan details from DTO to VO
		StagingRegistrationDetailsDTO regDTO = optionalDto.get();
		if (!(regDTO.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode())
				|| regDTO.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
				|| regDTO.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.ARVT.getCovCode())
				|| regDTO.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.IVCN.getCovCode())
				|| regDTO.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TTTT.getCovCode())
				|| regDTO.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode()))) {

			logger.error("Vehicle is not chassis or ARVT or IVCN : [{}] ", regDTO.getApplicationNo());
			throw new BadRequestException("Vehicle is not chassis or ARVT or IVCN : [{}] ");

		}
		if (StringUtils
				.isNoneBlank(regDTO.getApplicantDetails().getPresentAddress().getMandal().getMviAddressOfficeCode())) {

			if (regDTO.getVehicleType().equalsIgnoreCase(CovCategory.N.getCode()) && !regDTO.getApplicantDetails()
					.getPresentAddress().getMandal().getNonTransportOffice().equals(officecode)) {
				logger.error("BadRequestException: No Authorization for this office Code [{}]", officecode);
				throw new BadRequestException("No Authorization for this office Code" + officecode);
			} else if (regDTO.getVehicleType().equalsIgnoreCase(CovCategory.T.getCode()) && !regDTO
					.getApplicantDetails().getPresentAddress().getMandal().getTransportOfice().equals(officecode)) {
				logger.error("BadRequestException: No Authorization for this office Code [{}]", officecode);
				throw new BadRequestException("No Authorization for this office Code" + officecode);
			}
		} else {
			if (!regDTO.getApplicantDetails().getPresentAddress().getMandal().getHsrpoffice().equals(officecode)) {
				logger.error("BadRequestException: No Authorization for this office Code [{}]", officecode);
				throw new BadRequestException("No Authorization for this office Code" + officecode);
			}
		}
		if (regDTO.getSlotDetails() == null) {
			logger.error("BadRequestException: Slot Date Not found [{}]", trNo);
			throw new BadRequestException("slot Date Not found");
		}

		if (!regDTO.getSlotDetails().getSlotDate().equals(LocalDate.now())) {
			logger.error("BadRequestException: Today is not Slot Booked Date [{}]", trNo);
			throw new BadRequestException("today is not Slot Booked Date");
		}
		if (regDTO.isMVIDoneForIvcn()) {
			logger.error("BadRequestException: MVI Inspection Done Based on TrNo [{}]", trNo);
			throw new BadRequestException("MVI Inspection Done Based on TrNo" + trNo);
		}
		SlotDetailsDTO slotDetails = regDTO.getSlotDetails();
		if (slotDetails != null) {
			slotDetailsVO = slotDetailsMapper.convertEntity(slotDetails);
		}
		Optional<AlterationDTO> alterationDetails = alterationDAO.findByApplicationNo(regDTO.getApplicationNo());
		if (alterationDetails.isPresent()) {
			logger.error("BadRequestException: MVI Inspection Done Based on TrNo [{}]", trNo);
			throw new BadRequestException("MVI Inspection Done Based on TrNo" + trNo);
		}
		OwnerAndVahanInfoVO vo = new OwnerAndVahanInfoVO();
		vo.setMviDone(isMVIDone);
		vo.setSlotsDetails(slotDetailsVO);
		vo.setApplicationNo(regDTO.getApplicationNo());
		vo.setOwnerName(regDTO.getApplicantDetails().getFirstName());
		vo.setAadhaarNo(regDTO.getApplicantDetails().getAadharNo());
		vo.setOwnerType(regDTO.getOwnerType().toString());
		vo.setMobileNo(regDTO.getApplicantDetails().getContact().getMobile());
		vo.setVehicleType(regDTO.getVehicleType());
		vo.setChassisNumber(regDTO.getVahanDetails().getChassisNumber());
		if (!regDTO.getIsTrailer()) {
			vo.setEngineNumber(regDTO.getVahanDetails().getEngineNumber());
		}
		vo.setClassOfVehicle(regDTO.getClassOfVehicleDesc());
		vo.setMakersName(regDTO.getVahanDetails().getMakersDesc());
		vo.setTrNo(trNo);
		if (regDTO.getVahanDetails() != null && regDTO.getVahanDetails().getMakersModel() != null) {
			vo.setVehicleName(regDTO.getVahanDetails().getMakersModel());
		}
		vo.setCovCode(regDTO.getClassOfVehicle());
		vo.setTrailer(regDTO.getIsTrailer());
		vo.setSeating(regDTO.getVahanDetails().getSeatingCapacity());
		vo.setGvw(regDTO.getVahanDetails().getGvw());
		vo.setUlw(regDTO.getVahanDetails().getUnladenWeight());
		MasterCovDTO cov = covDAO.findByCovcode(regDTO.getClassOfVehicle());
		List<MasterCovDTO> covDtoList = covDAO.findByCategoryAndIsChassisTrue(cov.getCategory());
		List<MasterCovVO> covVOList = covDtoList.stream().map(val -> new MasterCovMapper().convertEntity(val))
				.collect(Collectors.toList());
		if (regDTO.getClassOfVehicle().equals(ClassOfVehicleEnum.CHSN.getCovCode())) {
			covVOList = covVOList.stream().filter(val -> !val.getCovcode().equals(ClassOfVehicleEnum.OBPN.getCovCode()))
					.collect(Collectors.toList());
		} else if (regDTO.getClassOfVehicle().equals(ClassOfVehicleEnum.ARVT.getCovCode())) {
			covVOList = covVOList.stream().filter(val -> val.getCovcode().equals(ClassOfVehicleEnum.ARVT.getCovCode()))
					.collect(Collectors.toList());
		} else if (regDTO.getClassOfVehicle().equals(ClassOfVehicleEnum.IVCN.getCovCode())) {
			Optional<List<MasterCovDTO>> masterCovList = covDAO.findByInvalidCov(Boolean.TRUE);
			if (masterCovList.isPresent() && !masterCovList.get().isEmpty()) {
				covVOList = new MasterCovMapper().convertEntity(masterCovList.get());
			}
		}
		// Fetch Master_BodyType collections
		List<BodyTypeDTO> bodyTypeDTOList = bodyTypeDAO.findAll();
		List<BodyTypeVO> bodyTypeVOList = bodyTypeDTOList.stream().map(val -> new BodyTypeMapper().convertEntity(val))
				.collect(Collectors.toList());
		vo.setBodyTypeList(bodyTypeVOList);
		vo.setCovList(covVOList);
		Optional<VoluntaryTaxDTO> voluntaryTax = voluntaryTaxDAO.findByTrNoOrderByCreatedDateDesc(regDTO.getTrNo());
		if (voluntaryTax.isPresent()) {
			if (voluntaryTax.get().getDateOfCompletion() != null) {
				vo.setDateOfCompletion(voluntaryTax.get().getDateOfCompletion());
				MasterCovDTO covPaidAtBorder = covDAO.findByCovcode(voluntaryTax.get().getClassOfVehicle());
				if (covPaidAtBorder == null) {
					logger.error("class of vehicle not found for  [{}]", voluntaryTax.get().getClassOfVehicle());
					throw new BadRequestException(
							"class of vehicle not found for  [{}]" + voluntaryTax.get().getClassOfVehicle());
				}
				vo.setBorderSelectedCov(new MasterCovMapper().convertEntity(covPaidAtBorder));
				vo.setBorderEnteredulw(voluntaryTax.get().getUlw());
				vo.setBorderEnteredseats(voluntaryTax.get().getSeatingCapacity());
			}
		}
		return vo;
	}

	@Override
	public String getSequenceNo() {

		String number = "";
		try {
			Map<String, String> hMap = new HashMap<>();
			number = sequencenGenerator.getSequence(Sequence.ALTRATION.getSequenceId().toString(), hMap);
		} catch (SequenceGenerateException e) {
			logger.error(" Chanlana SequenceGenerateException  {}", e);

		} catch (Exception e) {
			logger.error(" exception While chalana generation {}", e);

		}
		logger.info("Chalana number ganarated, final number :{}", number);
		return number;
	}

	@Override
	public List<DisplayEnclosures> getEnclosuresforView(String applicationFormNo, String userId, String inputRole) {

		String roles = dTOUtilService.getRole(userId, inputRole);

		Optional<StagingRegistrationDetailsDTO> registrationDetails = stagingRegistrationDetailsSerivce
				.FindbBasedOnApplicationNo(applicationFormNo);
		if (!registrationDetails.isPresent()) {
			logger.error("BadRequestException: Application is not found [{}]", applicationFormNo);
			throw new BadRequestException("Application  is not found. Application no :" + applicationFormNo);
		}
		List<EnclosuresDTO> enclosureDtos = enclosuresDAO.findByServiceID(ServiceEnum.BODYBUILDER.getId());
		enclosureDtos.sort((p1, p2) -> p1.getSlNo().compareTo(p2.getSlNo()));
		List<DisplayEnclosures> rejectedEnclosures = new ArrayList<>();

		if (registrationDetails.get().getEnclosures() == null) {
			logger.error("BadRequestException: Enclosures is not found [{}]",
					registrationDetails.get().getApplicationNo());
			throw new BadRequestException("Enclosures  is not found.");
		}
		List<KeyValue<String, List<ImageEnclosureDTO>>> listOfImages = new ArrayList<>();
		// Rejected images in RTO
		// To do need to change the status check

		listOfImages.addAll(registrationDetails.get().getEnclosures());

		for (KeyValue<String, List<ImageEnclosureDTO>> enclosureKeyValue : listOfImages) {

			boolean statue = false;
			for (EnclosuresDTO enclosures : enclosureDtos) {
				Optional<ImageEnclosureDTO> value = enclosureKeyValue.getValue().stream()
						.filter(dto -> dto.getImageType().equalsIgnoreCase(enclosures.getProof())).findFirst();
				/*
				 * if (value.isPresent()) { if
				 * (!enclosures.getBasedOnRole().stream().anyMatch(role ->
				 * roles.equalsIgnoreCase(role))) { statue = true; } }
				 */
			}

			/*
			 * if (statue) { continue; }
			 */
			Optional<EnclosuresDTO> matchedEnclosure = enclosureDtos.stream()
					.filter(dto -> dto.getProof().equals(enclosureKeyValue.getKey())).findFirst();

			if (!matchedEnclosure.isPresent()) {
				continue;
			}

			// EnclosureType enclosureType =
			// EnclosureType.getEnclosureType(matchedEnclosure.get().getProof());

			List<ImageVO> imagesVO = enclosureImageMapper.convertNewEntity(enclosureKeyValue.getValue());

			rejectedEnclosures.add(new DisplayEnclosures(imagesVO));
		}

		return rejectedEnclosures;
	}

	@Override
	public Optional<AlterationVO> getVehicleAlterationData(String applicationNo) {

		Optional<AlterationDTO> alterationData = alterationDAO.findByApplicationNo(applicationNo);
		if (alterationData.isPresent()) {
			AlterationVO alterationVO = alterationMapper.convertEntity(alterationData.get());
			return Optional.of(alterationVO);
		}
		return Optional.empty();
	}

	/**
	 * This is service method saves alteration details to alteration_details
	 * collection and saves uploaded images to stagging_registration_details
	 * collection
	 * 
	 * @param alterationVO
	 * @throws IOException
	 */
	@Override
	public void saveAlterationDetails(UserDTO user, AlterationVO alterationVO, List<ImageInput> images,
			MultipartFile[] uploadfiles) throws IOException {
		Optional<StagingRegistrationDetailsDTO> optionalDto = stagingRegistrationDetailsDAO
				.findByTrNoAndApplicationStatus(alterationVO.getTrNo(), StatusRegistration.SLOTBOOKED.getDescription());
		if (!optionalDto.isPresent()) {
			throw new BadRequestException("Slot Not Booked for BodyBuilding [{}]" + alterationVO.getTrNo());
		}
		StagingRegistrationDetailsDTO stagingDTO = optionalDto.get();

		if (stagingDTO.getSlotDetails() == null || stagingDTO.getSlotDetails().getSlotDate() == null) {
			throw new BadRequestException("Slot Date Not Found for TrNo" + alterationVO.getTrNo());
		}
		if (!LocalDate.now().equals(stagingDTO.getSlotDetails().getSlotDate())) {
			throw new BadRequestException("Today is Not Slot Booked Date");
		}

		MasterCovDTO covDTO = covDAO.findByCovcode(alterationVO.getCov());
		if (stagingDTO.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.IVCN.getCovCode())
				|| stagingDTO.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TTTT.getCovCode())
				|| stagingDTO.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())) {
			stagingDTO.setMVIDoneForIvcn(
					stagingDTO.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.IVCN.getCovCode()) ? Boolean.TRUE
							: Boolean.FALSE);
			if (alterationVO.getAction() != null && alterationVO.getAction().equals(StatusRegistration.REJECTED)) {
				
				if(stagingDTO.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TTTT.getCovCode())||
						stagingDTO.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())){
					setBodyBuildMVIflowForTrilerRejection(stagingDTO,user);
					stagingDTO.setApplicationStatus(StatusRegistration.TRGENERATED.getDescription());
					stagingDTO.setProcessActionStatus("REJECTED");
					stagingDTO.setRemarks(alterationVO.getRemarks());
					
				}else {
				stagingDTO.setClassOfVehicle(covDTO.getCovcode());
				stagingDTO.setClassOfVehicleDesc(covDTO.getCovdescription());
				stagingDTO.setRemarks(alterationVO.getRemarks());
				stagingDTO.setApplicationStatus(StatusRegistration.IVCNREJECTED.getDescription());
				if (stagingDTO.getVehicleDetails() != null) {
					stagingDTO.getVehicleDetails().setClassOfVehicle(covDTO.getCovcode());
				 }
				}
			} else if (alterationVO.getAction() != null
					&& alterationVO.getAction().equals(StatusRegistration.APPROVED)) {
				stagingDTO.setApplicationStatus(StatusRegistration.TRGENERATED.getDescription());
				stagingDTO.setAutoApprovalInitiatedDate(LocalDate.now());
				setBodyBuildMVIflow(stagingDTO, user);

			}
			logMovingService.moveStagingToLog(stagingDTO.getApplicationNo());
			stagingRegistrationDetailsDAO.save(stagingDTO);
		} else {
			if (alterationVO.getUlw() == null) {
				throw new BadRequestException("ULW is empty");
			}

			if (alterationVO.getDateOfCompletion().isAfter(LocalDate.now())) {
				throw new BadRequestException(
						"future Date is not accepted for Date of Completion " + alterationVO.getDateOfCompletion());
			}
			if (alterationVO.getDateOfCompletion().isBefore(stagingDTO.getTrGeneratedDate().toLocalDate())) {
				throw new BadRequestException("Alteration completion Date should be after TrGenerated Date");
			}
			if (alterationVO.getUlw() <= stagingDTO.getVahanDetails().getUnladenWeight()) {
				throw new BadRequestException("the ULW should be more than the original ULW");
			}

			if (ClassOfVehicleEnum.ARVT.getCovCode().equalsIgnoreCase(alterationVO.getCov())) {
				if (alterationVO.getTrailers() == null || alterationVO.getTrailers().isEmpty()) {
					throw new BadRequestException(
							"please enter Chassis Number for ARVT: " + alterationVO.getApplicationNo());
				}
				if (alterationVO.getTrailers().size() > 3) {
					throw new BadRequestException(
							"chassis limit 3 exceeded for ARVT: " + alterationVO.getApplicationNo());
				}
				Integer gtw = alterationVO.getTrailers().stream().findFirst().get().getGtw();

				for (TrailerChassisDetailsVO trailerDetails : alterationVO.getTrailers()) {
					if (trailerDetails.getUlw() > trailerDetails.getGtw()) {
						throw new BadRequestException(
								"Trailers ULW should not be more than GTW " + alterationVO.getApplicationNo());
					}
					if (trailerDetails.getGtw() > gtw) {
						gtw = trailerDetails.getGtw();
					}
				}
				Integer rlw = stagingDTO.getVahanDetails().getGvw() + gtw;
				Optional<PropertiesDTO> arvtWeight = propertiesDAO.findByArvtWeightFlagTrue();
				if (!arvtWeight.isPresent()) {
					throw new BadRequestException(
							"ARVT weight master data not found" + alterationVO.getApplicationNo());
				}
				if (rlw > arvtWeight.get().getArvtWeight()) {
					throw new BadRequestException(
							"Articulated Vehicle GVW weight and trailer GVW weight should not be greater than: "
									+ arvtWeight.get().getArvtWeight());
				}
			}
			if (ClassOfVehicleEnum.GCRT.getCovCode().equalsIgnoreCase(alterationVO.getCov())) {
				String weightDetails = covService.getWeightTypeDetails(stagingDTO.getVahanDetails().getGvw());
				if (weightDetails.equalsIgnoreCase("HMV")) {
					if (alterationVO.getAxleType() == null || StringUtils.isBlank(alterationVO.getAxleType())) {
						throw new BadRequestException(
								"Axel type missing for GCRT class of vehicle: " + alterationVO.getApplicationNo());
					}
				}
			}

			AlterationDTO alterationDTO = alterationMapper.convertVO(alterationVO);
			alterationDTO.setCovDescription(covDTO.getCovdescription());
			alterationDTO.setMVIDone(true);
			alterationDTO.setId(this.getSequenceNo());
			alterationDTO.setApplicationNo(stagingDTO.getApplicationNo());
			alterationDTO.setVahanDetails(stagingDTO.getVahanDetails());
			alterationDTO.setFromCov(stagingDTO.getClassOfVehicle());
			alterationDTO.setActionBy(user.getUserId());
			alterationDTO.setAlterationDate(LocalDateTime.now());
			if (stagingDTO.getVehicleDetails() != null && stagingDTO.getVehicleDetails().getBodyTypeDesc() != null) {
				alterationDTO.setFrombodyType(stagingDTO.getVehicleDetails().getBodyTypeDesc());
			}
			if (stagingDTO.getVehicleDetails() != null && stagingDTO.getVehicleDetails().getFuelDesc() != null) {
				alterationDTO.setFromFuel(stagingDTO.getVehicleDetails().getFuelDesc());
			}
			if (stagingDTO.getVehicleDetails() != null && stagingDTO.getVehicleDetails().getSeatingCapacity() != null) {
				alterationDTO.setSeating(stagingDTO.getVehicleDetails().getSeatingCapacity());
			}
			alterationDTO.setVehicleTypeFrom(stagingDTO.getVehicleType());
			stagingDTO.setApplicationStatus(StatusRegistration.TAXPENDING.getDescription());
			stagingDTO.getVahanDetails().setTrailerChassisDetailsDTO(alterationDTO.getTrailers());
			if (stagingDTO.getVehicleDetails() != null) {
				stagingDTO.getVehicleDetails().setClassOfVehicle(alterationDTO.getCov());
			}
			stagingDTO.setBodyBuildingDone(true);
			setBodyBuildMVIflow(stagingDTO, user);
			dealerService.saveEnclosures(stagingDTO, images, uploadfiles);
			alterationDAO.save(alterationDTO);
			logMovingService.moveStagingToLog(stagingDTO.getApplicationNo());
		}

	}

	public void setBodyBuildMVIflow(StagingRegistrationDetailsDTO staging, UserDTO user) {

		if (staging.getFlowDetails() != null && !staging.getFlowDetails().getFlowDetails().isEmpty()) {
			if (staging.getFlowDetails().getFlowDetails().get(1) != null) {
				List<RoleActionDTO> roleActionList = staging.getFlowDetails().getFlowDetails().get(1);
				roleActionList.removeIf(val -> val.getRole().equalsIgnoreCase(RoleEnum.MVI.getName()));
				if (CollectionUtils.isEmpty(roleActionList)) {
					staging.getFlowDetails().getFlowDetails().remove(1);
				}

				FlowDTO flowDTO = new FlowDTO();
				flowDTO.setApplicationNo(staging.getApplicationNo());
				flowDTO.setModule(ServiceCodeEnum.REGISTRATION.toString());
				Map<Integer, List<RoleActionDTO>> flowMap = new HashMap<>();
				RoleActionDTO roleAction = new RoleActionDTO();
				roleAction.setAction("APPROVED");
				roleAction.setActionBy(user.getUserId());
				roleAction.setActionTime(LocalDateTime.now());
				roleAction.setApplicatioNo(staging.getApplicationNo());
				roleAction.setRole(RoleEnum.MVI.getName());
				roleAction.setUserId(user.getUserId());
				flowMap.put(1, Arrays.asList(roleAction));
				flowDTO.setFlowDetails(flowMap);

				if (CollectionUtils.isNotEmpty(staging.getFlowDetailsLog())) {

					List<RoleActionDTO> roleActions = staging.getFlowDetailsLog().stream().findFirst().get()
							.getFlowDetails().get(1);
					if (CollectionUtils.isNotEmpty(roleActions)) {
						roleActions.add(roleAction);
					}

				} else {
					List<FlowDTO> flowList = new ArrayList<>();
					flowList.add(flowDTO);
					staging.setFlowDetailsLog(flowList);
				}
			}

		}
	}
		
		

		public void setBodyBuildMVIflowForTrilerRejection(StagingRegistrationDetailsDTO staging, UserDTO user) {

			if (staging.getFlowDetails() != null && !staging.getFlowDetails().getFlowDetails().isEmpty()) {
				if (staging.getFlowDetails().getFlowDetails().get(1) != null) {
					List<RoleActionDTO> roleActionList = staging.getFlowDetails().getFlowDetails().get(1);
					roleActionList.removeIf(val -> val.getRole().equalsIgnoreCase(RoleEnum.MVI.getName()));
					if (CollectionUtils.isEmpty(roleActionList)) {
						staging.getFlowDetails().getFlowDetails().remove(1);
					}

					FlowDTO flowDTO = new FlowDTO();
					flowDTO.setApplicationNo(staging.getApplicationNo());
					flowDTO.setModule(ServiceCodeEnum.REGISTRATION.toString());
					Map<Integer, List<RoleActionDTO>> flowMap = new HashMap<>();
					RoleActionDTO roleAction = new RoleActionDTO();
					roleAction.setAction("REJECTED");
					roleAction.setActionBy(user.getUserId());
					roleAction.setActionTime(LocalDateTime.now());
					roleAction.setApplicatioNo(staging.getApplicationNo());
					roleAction.setRole(RoleEnum.MVI.getName());
					roleAction.setUserId(user.getUserId());
					flowMap.put(1, Arrays.asList(roleAction));
					flowDTO.setFlowDetails(flowMap);

					if (CollectionUtils.isNotEmpty(staging.getFlowDetailsLog())) {

						List<RoleActionDTO> roleActions = staging.getFlowDetailsLog().stream().findFirst().get()
								.getFlowDetails().get(1);
						if (CollectionUtils.isNotEmpty(roleActions)) {
							roleActions.add(roleAction);
						}

					} else {
						List<FlowDTO> flowList = new ArrayList<>();
						flowList.add(flowDTO);
						staging.setFlowDetailsLog(flowList);
					}
				}

			}

	}

}
