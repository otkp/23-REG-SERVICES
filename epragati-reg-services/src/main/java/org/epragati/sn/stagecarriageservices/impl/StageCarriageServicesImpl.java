package org.epragati.sn.stagecarriageservices.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.aadhaar.AadharDetailsResponseVO;
import org.epragati.aadhaarAPI.AadhaarSourceDTO;
import org.epragati.exception.BadRequestException;
import org.epragati.exception.RcValidationException;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dao.FeeCorrectionDAO;
import org.epragati.master.dao.RegServiceDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dto.ActionDetailsDTO;
import org.epragati.master.dto.FeeCorrectionDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.mappers.PermitDetailsMapper;
import org.epragati.master.mappers.RegistrationDetailsMapper;
import org.epragati.master.vo.SearchVo;
import org.epragati.permits.dao.PermitDetailsDAO;
import org.epragati.permits.dto.PermitDetailsDTO;
import org.epragati.permits.vo.PermitDetailsVO;
import org.epragati.regservice.RegistrationService;
import org.epragati.regservice.dto.ActionDetails;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.mapper.FeeCorrectionMapper;
import org.epragati.regservice.mapper.RegServiceMapper;
import org.epragati.regservice.vo.FeeCorrectionVO;
import org.epragati.regservice.vo.RcValidationVO;
import org.epragati.regservice.vo.RegServiceVO;
import org.epragati.rta.service.impl.service.RTAService;
import org.epragati.stagecarriages.dto.MasterStageCarriagesServicesDTO;
import org.epragati.stagecarriageservice.StageCarriageServices;
import org.epragati.stagecarriageservices.dao.StageCarriageServicesDAO;
import org.epragati.stagecarriageservices.mapper.StageCarriageServicesMapper;
import org.epragati.stagecarriageservices.vo.MasterStageCarriageServicesVO;
import org.epragati.util.FeeTypeDetails;
import org.epragati.util.PermitsEnum;
import org.epragati.util.RoleEnum;
import org.epragati.util.PermitsEnum.PermitType;
import org.epragati.util.StatusRegistration;
import org.epragati.util.payment.ClassOfVehicleEnum;
import org.epragati.util.payment.ServiceCodeEnum;
import org.epragati.util.payment.ServiceEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class StageCarriageServicesImpl implements StageCarriageServices {

	private static final Logger logger = LoggerFactory.getLogger(StageCarriageServicesImpl.class);

	@Autowired
	private StageCarriageServicesDAO stageCarriageServicesDAO;

	@Autowired
	private StageCarriageServicesMapper stageCarriageServicesMapper;

	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;

	@Autowired
	private RegistrationService registrationService;

	@Autowired
	private PermitDetailsDAO permitDetailsDAO;

	@Autowired
	private PermitDetailsMapper permitDetailsMapper;

	@Autowired
	private RegistrationDetailsMapper<?> registrationDetailsMapper;

	@Autowired
	private RegServiceDAO regServiceDAO;

	@Autowired
	private RegServiceMapper regServiceMapper;

	@Autowired
	private FeeCorrectionDAO feeCorrectionDAO;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private RTAService rtaService;

	@Autowired
	private FeeCorrectionMapper feeCorrectionMapper;
	
	@Override
	public List<MasterStageCarriageServicesVO> getStageCarriageServices() {
		List<MasterStageCarriagesServicesDTO> listOfDto = stageCarriageServicesDAO.findByStatusTrue();
		return stageCarriageServicesMapper.convertEntity(listOfDto);
	}

	@Override
	public SearchVo aadharValidationForStageCarriageServices(RcValidationVO rcValidationVO, boolean requestFromSave)
			throws RcValidationException {
		Optional<RegistrationDetailsDTO> registrationOptional = Optional.empty();
		if (CollectionUtils.isEmpty(rcValidationVO.getServiceIds())) {
			throw new BadRequestException("Please select services.");
		}

		if (StringUtils.isNoneBlank(rcValidationVO.getPrNo())) {
			registrationOptional = registrationDetailDAO.findByPrNo(rcValidationVO.getPrNo());
		}

		if (!registrationOptional.isPresent()) {
			logger.error("No record found. [{}] ", rcValidationVO.getPrNo());
			throw new BadRequestException("No record found.Pr no: " + rcValidationVO.getPrNo());
		}

		if (!registrationOptional.get().getApplicantDetails().getIsAadhaarValidated()
				|| registrationOptional.get().getApplicantDetails().getAadharNo() == null) {
			logger.error("Please select aadhar seeding service to Seed your aadhar number");
			throw new BadRequestException("Please select aadhar seeding service to Seed your aadhar number");
		}
		if (!registrationOptional.get().getApplicantDetails().getAadharNo()
				.equalsIgnoreCase(rcValidationVO.getAadharNo())) {

			logger.error("Please give correct aadhar number...");
			throw new BadRequestException("Please give correct aadhar number...");

		}
		try {
			this.validationForSCRTServices(rcValidationVO, registrationOptional.get());
		} catch (RcValidationException e) {
			logger.error("Exception in RcValidation [{}] ", e);
			throw new RcValidationException(e.getErrors());
		}

		SearchVo searchVo = new SearchVo();
		AadhaarSourceDTO aadhaarSourceDTO = registrationService.setAadhaarSourceDetails(rcValidationVO);
		AadharDetailsResponseVO aadharResponse = registrationService
				.getAadharResponse(rcValidationVO.getAadhaarDetailsRequestVO(), aadhaarSourceDTO);

		return registrationService.getSearchResult(rcValidationVO, registrationOptional.get(), searchVo);

		// return null;
	}

	@Override
	public RegServiceVO saveStageCarriageServices(String regServiceVO, MultipartFile[] multipart, JwtUser user)
			throws IOException, RcValidationException {

		return registrationService.saveStageCarriageServices(regServiceVO, multipart, user);
	}

	@Override
	public void validationForSCRTServices(RcValidationVO rcValidationVO, RegistrationDetailsDTO registrationDTO)
			throws RcValidationException {
		List<String> errors = new ArrayList<>();
		Optional<MasterStageCarriagesServicesDTO> listOfValidations = stageCarriageServicesDAO
				.findByServiceIdIn(rcValidationVO.getServiceIds());

		listOfValidations.get().getValidation().stream().forEach(data -> {
			try {
				registrationService.validations(registrationDTO, errors, data, rcValidationVO);
			} catch (Exception e) {
				throw new BadRequestException(e.getMessage());
			}
		});

		if (!errors.isEmpty()) {
			logger.error(" Validation Failed  : [{}]", errors);
			try {
				logger.error("[{}]", errors);
				throw new RcValidationException(errors);
			} catch (Exception e) {
				logger.error("[{}]", errors, e);
				throw new RcValidationException(errors, e);
			}
		}
	}

	@Override
	public SearchVo getPermitDetailsForScrt(RcValidationVO rcValidationVO) {
		PermitDetailsVO vo = null;
		Optional<PermitDetailsDTO> dto = null;
		SearchVo result = new SearchVo();
		if (StringUtils.isBlank(rcValidationVO.getPrNo())) {
			logger.error("Please provide pr number...");
			throw new BadRequestException("Please provide pr number...");
		}
		if (rcValidationVO.getServiceIds() == null) {
			logger.error("Please provide service type for pr no: " + rcValidationVO.getPrNo());
			throw new BadRequestException("Please provide service type for pr no: " + rcValidationVO.getPrNo());
		}
		if (rcValidationVO.getServiceIds().stream()
				.anyMatch(id -> id.equals(ServiceEnum.STAGECARRIAGERENEWALOFPERMIT.getId()))) {
			dto = permitDetailsDAO.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(rcValidationVO.getPrNo(),
					PermitType.PRIMARY.getPermitTypeCode(), PermitsEnum.ACTIVE.getDescription());
			if (!dto.isPresent()) {
				logger.error("Permit details not found for pr number: " + rcValidationVO.getPrNo());
				throw new BadRequestException("Permit details not found for pr number: " + rcValidationVO.getPrNo());
			}
			// TODO need to validate permit type SCRT or not
			if (dto.get().getPermitValidityDetails() == null
					|| dto.get().getPermitValidityDetails().getPermitValidTo() == null) {
				logger.error("Permit validitys not found for pr number: " + rcValidationVO.getPrNo());
				throw new BadRequestException("Permit validitys not found for pr number: " + rcValidationVO.getPrNo());
			}
			if (dto.get().getPermitValidityDetails().getPermitValidTo().isAfter(LocalDate.now())) {
				logger.error("Please renewal permit after expire: " + rcValidationVO.getPrNo());
				throw new BadRequestException("Please renewal permit after expire: " + rcValidationVO.getPrNo());
			}
		} else if (rcValidationVO.getServiceIds().stream()
				.anyMatch(id -> id.equals(ServiceEnum.STAGECARRIAGEREPLACEMENTOFVEHICLE.getId()))) {
			Pair<RegistrationDetailsDTO, RegistrationDetailsDTO> regDetails = this
					.validationForScrtReplacementOfVehicle(rcValidationVO.getPrNo(),
							rcValidationVO.getPermitVehiclePrNo());
			dto = permitDetailsDAO.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(
					rcValidationVO.getPermitVehiclePrNo(), PermitType.PRIMARY.getPermitTypeCode(),
					PermitsEnum.ACTIVE.getDescription());
			if (!dto.isPresent()) {
				logger.error("Permit details not found for pr number: " + rcValidationVO.getPrNo());
				throw new BadRequestException("Permit details not found for pr number: " + rcValidationVO.getPrNo());
			}
			// TODO need to validate permit type SCRT or not
			result.setRegistrationDetails(registrationDetailsMapper.convertEntity(regDetails.getSecond()));

		}
		if (dto != null) {
			Optional<PermitDetailsVO> permitDetailsVO = permitDetailsMapper.convertEntity(dto);
			vo = permitDetailsVO.get();
		}

		result.setPermitDetailsVO(vo);
		return result;
	}

	@Override
	public Pair<RegistrationDetailsDTO, RegistrationDetailsDTO> validationForScrtReplacementOfVehicle(String prNo,
			String permitVehiclePrNo) {
		if (StringUtils.isBlank(prNo)) {
			logger.error("Please provide vehicle number for which permit will replace...");
			throw new BadRequestException("Please provide vehicle number for which permit will replace...");
		}
		Optional<RegistrationDetailsDTO> regDetailsFornonPermit = registrationDetailDAO.findByPrNo(prNo);

		if (!regDetailsFornonPermit.isPresent()) {
			logger.error("No record found. [{}] ", prNo);
			throw new BadRequestException("No record found.Pr no: " + prNo);
		}
		if (regDetailsFornonPermit.get().getApplicantDetails().getIsAadhaarValidated() == null
				|| !regDetailsFornonPermit.get().getApplicantDetails().getIsAadhaarValidated()) {
			logger.error("Please seed aadhar to vehicle number [{}] ", prNo);
			throw new BadRequestException("Please seed aadhar to vehicle number  " + prNo);
		}
		Optional<RegistrationDetailsDTO> regdetails = registrationDetailDAO.findByPrNo(permitVehiclePrNo);

		if (!regdetails.isPresent()) {
			logger.error("No record found. [{}] ", permitVehiclePrNo);
			throw new BadRequestException("No record found.Pr no: " + permitVehiclePrNo);
		}
		if (!regDetailsFornonPermit.get().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.SCRT.getCovCode())) {
			logger.error("Only stage carriage vehicle can transfer the permit: ", prNo);
			throw new BadRequestException("Only stage carriage vehicle can transfer the permit: " + prNo);
		}
		if (!regdetails.get().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.SCRT.getCovCode())) {
			logger.error("Only stage carriage vehicle can transfer the permit: ", permitVehiclePrNo);
			throw new BadRequestException("Only stage carriage vehicle can transfer the permit: " + permitVehiclePrNo);
		}
		if (!regDetailsFornonPermit.get().getApplicantDetails().getAadharNo()
				.equalsIgnoreCase(regdetails.get().getApplicantDetails().getAadharNo())) {
			logger.error("Same aadhar holder can replace the permit for vehicle numbers: ", permitVehiclePrNo, " and ",
					prNo);
			throw new BadRequestException("Same aadhar holder can replace the permit for vehicle numbers: "
					+ permitVehiclePrNo + " and " + prNo);
		}
		Optional<PermitDetailsDTO> dto = permitDetailsDAO.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(prNo,
				PermitType.PRIMARY.getPermitTypeCode(), PermitsEnum.ACTIVE.getDescription());
		if (dto.isPresent()) {
			logger.error("This vehicle have pucca permit. Plesse surrender the permit for pr number: " + prNo);
			throw new BadRequestException(
					"This vehicle have pucca permit. Plesse surrender the permit for pr number: " + prNo);
		}
		return Pair.of(regdetails.get(), regDetailsFornonPermit.get());
	}

	@Override
	public void updateDetailsForNewStageCarriage(RegServiceVO regServiceDetail) {
		Optional<RegServiceDTO> dto = registrationService.findByApplicationNo(regServiceDetail.getApplicationNo());
		if (dto.isPresent()) {
			dto.get().setApplicationStatus(StatusRegistration.CCOAPPROVED);
			regServiceDAO.save(dto.get());
		}
	}

	@Override
	public List<RegServiceVO> getStageCarriageRecordsInAO() {
		List<RegServiceDTO> listOfStageCarriages = regServiceDAO.findByServiceIdsAndApplicationStatusIn(
				ServiceEnum.NEWSTAGECARRIAGEPERMIT.getId(), Arrays.asList(StatusRegistration.CCOAPPROVED));
		if (listOfStageCarriages.isEmpty()) {
			throw new BadRequestException("Records not found");
		}
		return regServiceMapper.convertEntity(listOfStageCarriages);
	}

	@Override
	public RegServiceVO stageCarriageApprovalProcess(RegServiceVO regServiceVO, String role, JwtUser user) {
		Optional<RegServiceDTO> regDTO = regServiceDAO.findByApplicationNo(regServiceVO.getApplicationNo());
		RegServiceDTO regServiceDTO = regDTO.get();
		List<ActionDetails> actionDetailsList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(regServiceDTO.getActionDetails())) {
			actionDetailsList = regServiceDTO.getActionDetails();
		}

		ActionDetails actionDetails = new ActionDetails();
		actionDetails.setRole(role);
		actionDetails.setUserId(user.getId());
		actionDetails.setStatus(regServiceVO.getStatus());
		actionDetails.setCreatedBy(user.getId());
		actionDetails.setCreatedDate(LocalDateTime.now());
		actionDetailsList.add(actionDetails);
		regServiceDTO.setActionDetails(actionDetailsList);

		regServiceDTO.setApplicationStatus(
				regServiceVO.getStatus().equalsIgnoreCase(StatusRegistration.APPROVED.getDescription())
						? StatusRegistration.INITIATED
						: StatusRegistration.CANCELED);

		regServiceDTO.setCurrentIndex(RoleEnum.RTO.getIndex() + 1);
		regServiceDTO.setUpdatedBy(user.getId());
		regServiceDTO.setlUpdate(LocalDateTime.now());

		if(regServiceVO.getPermitDetailsVO() != null) {
			regServiceDTO.setPdtl(permitDetailsMapper.convertVO(regServiceVO.getPermitDetailsVO()));
		}
		regServiceDAO.save(regServiceDTO);

		return regServiceMapper.convertEntity(regServiceDTO);
	}

	@Override
	public void saveInFeeCorrections(RegServiceVO regVO, JwtUser user, String role) {
		Optional<UserDTO> userDTO = userDAO.findByUserId(user.getId());
		UserDTO userDetails = userDTO.get();
		Optional<RegistrationDetailsDTO> registrationDetails = registrationDetailDAO.findByPrNo(regVO.getPrNo());
		;
		FeeCorrectionVO vo = new FeeCorrectionVO();
		addFeeCorrectionDetailsForVo(regVO, vo, role);
		FeeCorrectionDTO dto = feeCorrectionMapper.convertVO(vo);

		dto.setApplicationNo(registrationDetails.get().getApplicationNo());
		dto.setChassisNo(registrationDetails.get().getVahanDetails().getChassisNumber());
		FeeCorrectionDTO feeDto = rtaService.getFeeDoc(registrationDetails.get().getVahanDetails().getChassisNumber(),
				role);
		ActionDetailsDTO actions = new ActionDetailsDTO();
		actions.setActionBy(userDetails.getUserId());
		actions.setActionDatetime(LocalDateTime.now());
		actions.setActionByRole(Arrays.asList(role));

		rtaService.validationForFeeParts(vo, feeDto, dto, userDetails, registrationDetails.get());
		if (feeDto != null) {
			if (CollectionUtils.isNotEmpty(feeDto.getActiondetails())) {
				logger.error("action details missing for: " + feeDto.getId());
				throw new BadRequestException("action details missing for: " + feeDto.getId());
			}
			feeDto.getActiondetails().add(actions);
			dto = feeDto;
		} else {
			dto.setActiondetails(Arrays.asList(actions));
		}
		dto.setStatus(true);
		dto.setApproved(Boolean.TRUE);
		dto.setStageCarriage(Boolean.TRUE);
		feeCorrectionDAO.save(dto);

		Optional<RegServiceDTO> regServiceDTO = regServiceDAO.findByApplicationNo(regVO.getApplicationNo());
		if (regServiceDTO.isPresent()) {

			List<ServiceEnum> servicesList = new ArrayList<>();
			servicesList.addAll(regServiceDTO.get().getServiceType());
			servicesList.add(ServiceEnum.FEECORRECTION);

			Set<Integer> servicesIds = new HashSet<>();
			servicesIds.addAll(regServiceDTO.get().getServiceIds());
			servicesIds.add(ServiceEnum.FEECORRECTION.getId());

			regServiceDTO.get().setServiceType(servicesList);
			regServiceDTO.get().setServiceIds(servicesIds);

			regServiceDTO.get().setApplicationStatus(StatusRegistration.CITIZENPAYMENTPENDING);
			regServiceDAO.save(regServiceDTO.get());
		}
	}

	private void addFeeCorrectionDetailsForVo(RegServiceVO regVO, FeeCorrectionVO vo, String role) {

		vo.setPrNo(regVO.getPrNo());
		vo.setFeeDetails(Arrays.asList(regVO.getFeeDetails()));
		vo.setOfficeCode(regVO.getRegistrationDetails().getOfficeDetails().getOfficeCode());
		vo.setRole(role);
		vo.setRemarks(regVO.getPrNo() + " Stage carriage fee details");
		regVO.getFeeDetails().getFeeDetails().stream().forEach(val -> {
			if (val.getFeesType().equalsIgnoreCase(ServiceCodeEnum.PERMIT_SERVICE_FEE.getTypeDesc())) {
				vo.setServiceFee(val.getAmount());
			}
			if (val.getFeesType().equalsIgnoreCase(ServiceCodeEnum.PERMIT_FEE.getTypeDesc())) {
				vo.setPermitApplicationFee(val.getAmount());
			}
			if (val.getFeesType().equalsIgnoreCase(ServiceCodeEnum.REGISTRATION.getTypeDesc())) {
				vo.setApplicationFee(val.getAmount());
			}
			if (val.getFeesType().equalsIgnoreCase(ServiceCodeEnum.SERVICE_FEE.getTypeDesc())) {
				vo.setServiceFee(val.getAmount());
			}
		});
	}
}
