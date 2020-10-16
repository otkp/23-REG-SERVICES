package org.epragati.master.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.epragati.aadhaar.AadharDetailsResponseVO;
import org.epragati.aadhaarAPI.AadhaarSourceDTO;
import org.epragati.common.vo.UserStatusEnum;
import org.epragati.constants.ActionTypeEnum;
import org.epragati.constants.MessageKeys;
import org.epragati.constants.StageEnum;
import org.epragati.constants.TransferType;
import org.epragati.dao.enclosure.FinancierEnclosureDAO;
import org.epragati.dealer.vo.RequestDetailsVO;
import org.epragati.dto.enclosure.FinacierDetailsDTO;
import org.epragati.dto.enclosure.FinancerUploadedDetailsDTO;
import org.epragati.dto.enclosure.FinancierEnclosuresDTO;
import org.epragati.dto.enclosure.ImageEnclosureDTO;
import org.epragati.dto.enclosure.UploadExcelDTO;
import org.epragati.excel.service.CellProps;
import org.epragati.excel.service.ExcelService;
import org.epragati.excel.service.ExcelServiceImpl;
import org.epragati.exception.BadRequestException;
import org.epragati.exception.RcValidationException;
import org.epragati.financier.vo.FinancierActionVO;
import org.epragati.financier.vo.FinancierCreateRequestVO;
import org.epragati.financier.vo.UploadExcelFileVO;
import org.epragati.images.vo.ImageInput;
import org.epragati.images.vo.InputVO;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dao.AadhaarResponseDAO;
import org.epragati.master.dao.ApplicantDetailsDAO;
import org.epragati.master.dao.EnclosuresDAO;
import org.epragati.master.dao.FinanceDetailsDAO;
import org.epragati.master.dao.FinanceSeedDetailsDAO;
import org.epragati.master.dao.FinancierCreateRequestDao;
import org.epragati.master.dao.FinancierSeriesDAO;
import org.epragati.master.dao.FinancierUpldDetailsDAO;
import org.epragati.master.dao.MasterFinanceTypeDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.RegServiceDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.StagingRegistrationDetailsDAO;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dto.AadhaarDetailsResponseDTO;
import org.epragati.master.dto.ActionDTO;
import org.epragati.master.dto.ActionDetailsDTO;
import org.epragati.master.dto.ApplicantDetailsDTO;
import org.epragati.master.dto.EnclosuresDTO;
import org.epragati.master.dto.FinanceDetailsDTO;
import org.epragati.master.dto.FinanceSeedDetailsDTO;
import org.epragati.master.dto.FinancierCreateRequestDTO;
import org.epragati.master.dto.FinancierSeriesDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.RolesDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.mappers.ApplicantAddressMapper;
import org.epragati.master.mappers.EnclosuresMapper;
import org.epragati.master.mappers.FinanceDetailsMapper;
import org.epragati.master.mappers.FinanceSeedDetailsMapper;
import org.epragati.master.mappers.FinancierCreateMapper;
import org.epragati.master.mappers.MasterFinanceTypeMapper;
import org.epragati.master.mappers.RegistrationDetailsMapper;
import org.epragati.master.mappers.UserMapper;
import org.epragati.master.service.FinancierService;
import org.epragati.master.service.LogMovingService;
import org.epragati.master.vo.FinanceDetailsVO;
import org.epragati.master.vo.FinanceSeedDetailsVO;
import org.epragati.master.vo.FinancierDashBoardVO;
import org.epragati.master.vo.MasterFinanceTypeVO;
import org.epragati.master.vo.OfficeVO;
import org.epragati.master.vo.RegistrationDetailsVO;
import org.epragati.master.vo.UserVO;
import org.epragati.regservice.RegistrationService;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.mapper.FreshRCMapper;
import org.epragati.regservice.mapper.RegServiceMapper;
import org.epragati.regservice.mapper.TowMapper;
import org.epragati.regservice.vo.FreshApplicationSearchVO;
import org.epragati.regservice.vo.FreshRcVO;
import org.epragati.regservice.vo.RegServiceVO;
import org.epragati.restGateway.RestGateWayService;
import org.epragati.rta.service.impl.service.RegistratrionServicesApprovals;
import org.epragati.sequence.SequenceGenerator;
import org.epragati.service.enclosure.mapper.EnclosureImageMapper;
import org.epragati.service.enclosure.vo.CitizenImagesInput;
import org.epragati.service.enclosure.vo.EnclosureRejectedVO;
import org.epragati.service.files.GridFsClient;
import org.epragati.service.notification.MessageTemplate;
import org.epragati.service.notification.NotificationUtil;
import org.epragati.util.AppMessages;
import org.epragati.util.DateConverters;
import org.epragati.util.ResponseStatusEnum.AADHAARRESPONSE;
import org.epragati.util.RoleEnum;
import org.epragati.util.Status;
import org.epragati.util.StatusActions;
import org.epragati.util.StatusRegistration;
import org.epragati.util.StatusRegistration.FinancierCreateReqStatus;
import org.epragati.util.document.KeyValue;
import org.epragati.util.document.Sequence;
import org.epragati.util.payment.ServiceEnum;
import org.epragati.util.payment.ServiceEnum.Flow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author krishnarjun.pampana
 *
 */
@Service
public class FinancierServiceImpl implements FinancierService {

	private static final Logger logger = LoggerFactory.getLogger(FinancierServiceImpl.class);

	static final String UNAUTHORIZED_ACTION = "Unauthorized Action";

	@Value("${financier.password:}")
	private String financierPassword;

	@Value("${file.upload-dir:}")
	private String upLoadDir;

	@Autowired
	RegistrationDetailDAO registrationdetailDAO;
	@Autowired
	private FinanceDetailsDAO financeDetailsDAO;

	@SuppressWarnings("rawtypes")
	@Autowired
	private RegistrationDetailsMapper registrationDetailsMapper;

	@Autowired
	private AppMessages appMessages;

	@Autowired
	private UserDAO userDao;

	@Autowired
	private RestGateWayService restGateWayService;

	@Autowired
	private StagingRegistrationDetailsDAO stagingRegistrationDetailsDAO;

	@Autowired
	private MasterFinanceTypeDAO masterFinanceTypeDAO;

	@Autowired
	private MasterFinanceTypeMapper masterFinanceTypeMapper;

	@Autowired
	private FinancierSeriesDAO financierSeriesDAO;

	@Autowired
	private UserMapper userMapper;
	@Autowired
	private OfficeDAO officeDao;

	@Autowired
	private RegistrationService registrationService;

	@Autowired
	private LogMovingService logMovingService;

	@Autowired
	private RegServiceDAO regServiceDAO;

	@Autowired
	private RegServiceMapper regServiceMapper;

	@Autowired
	private RegistratrionServicesApprovals registratrionServicesApprovals;

	@Autowired
	private RegistrationDetailDAO regDetailsDAO;
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private TowMapper towMapper;

	@Autowired
	private Environment applicationProprty;

	@Autowired
	private FinancierUpldDetailsDAO financierUpldDetailsDAO;

	@Autowired
	private FinancierCreateRequestDao finReqDao;

	@Autowired
	private FinancierCreateMapper finCreReqMapper;

	@Autowired
	private GridFsClient gridFsClient;

	@Autowired
	private FinancierEnclosureDAO financierEnclosuresDAO;

	@Autowired
	SequenceGenerator sequenceGenerator;

	@Autowired
	private EnclosuresMapper userEnclosureMapper;
	@Autowired
	private EnclosuresDAO encDao;

	@Autowired
	private NotificationUtil notifications;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private ApplicantDetailsDAO applicantDetailsDao;

	@Autowired
	private EnclosureImageMapper enclosureImageMapper;

	@Autowired
	private AadhaarResponseDAO aadhaarResponseDAO;
	
	@Autowired
	private FreshRCMapper freshRCMapper;
	
	@Autowired
	private ApplicantAddressMapper applicantAddressMapper;
	
	@Autowired
	private FinanceDetailsMapper financeDetailsMapper;
	
	@Autowired
	private RegistrationService regService;
	
	@Autowired
	private FinanceSeedDetailsDAO financeSeedDetailsDAO;
	
	@Autowired
	private FinanceSeedDetailsMapper financeSeedDetailsMapper;
	

	static final String[] EXTENSIONS = new String[] { "xls", "xlsx" };

	@Override
	public void doAction(FinancierActionVO financierActionVO, UserDTO userDTO) {
		final String UNAUTHORZIED_AADHAAR_DETAILS = "Unauthorised Aadhaar Details";
		synchronized (financierActionVO.getApplicationNo().intern()) {
			Optional<AadharDetailsResponseVO> aadhaarResponseDetails = Optional.empty();
			if (!financierActionVO.getAadhaarRequest().getUid_num().equals(userDTO.getAadharNo())) {
				if (StringUtils.isEmpty(userDTO.getAadharNo()) && StringUtils.isNotEmpty(userDTO.getUidToken())) {
					Optional<AadhaarDetailsResponseDTO> aadhaarDTO = aadhaarResponseDAO
							.findByUidToken(userDTO.getUidToken());
					if (!(aadhaarDTO.isPresent() && aadhaarDTO.get().getUid() != null && aadhaarDTO.get().getUid()
							.toString().equals(financierActionVO.getAadhaarRequest().getUid_num()))) {
						logger.error("Un Authorized Aadhaar Details for Aadhar: [{}] with Application  No [{}] ",
								financierActionVO.getAadhaarRequest().getUid_num(),
								financierActionVO.getApplicationNo());
						throw new BadRequestException(
								UNAUTHORZIED_AADHAAR_DETAILS + financierActionVO.getAadhaarRequest().getUid_num());
					}
				} else {
					logger.error("Un Authorized Aadhaar Details for ANO: [{}] with Application  No [{}] ",
							financierActionVO.getAadhaarRequest().getUid_num(), financierActionVO.getApplicationNo());
					throw new BadRequestException(
							UNAUTHORZIED_AADHAAR_DETAILS + financierActionVO.getAadhaarRequest().getUid_num());
				}
			}
			if(!financierActionVO.getReqAuthType().equalsIgnoreCase("Without Aadhar")) {
			AadhaarSourceDTO aadhaarSourceDTO = setAadhaarSourceDetails(financierActionVO);
			if (null != financierActionVO.getAadhaarRequest())
				aadhaarResponseDetails = restGateWayService.validateAadhaar(financierActionVO.getAadhaarRequest(),
						aadhaarSourceDTO);
			if (!aadhaarResponseDetails.isPresent()) {
				logger.error(appMessages.getLogMessage(MessageKeys.FINANCE_TOKEN_AAHDDAR_AUTHENTICATION_NO_SERVICE));
				logger.error("Un Authorized Aadhaar Details for Aadhar NO: [{}] with Application  No [{}] ",
						financierActionVO.getAadhaarRequest().getUid_num(), financierActionVO.getApplicationNo());
				throw new BadRequestException(
						appMessages.getResponseMessage(MessageKeys.FINANCE_TOKEN_AAHDDAR_AUTHENTICATION_NO_SERVICE));
			}
			if (aadhaarResponseDetails.get().getAuth_status().equals(AADHAARRESPONSE.SUCCESS.getLabel())) {
				logger.debug(appMessages.getLogMessage(MessageKeys.FINANCE_TOKEN_AAHDDAR_AUTHENTICATION),
						aadhaarResponseDetails.get().getAuth_err_code());

			} else {
				logger.error(appMessages.getLogMessage(MessageKeys.FINANCE_TOKEN_AAHDDAR_AUTHENTICATION),
						aadhaarResponseDetails.get().getUid(), aadhaarResponseDetails.get().getAuth_err_code());
				logger.error("Exception [{}] for aadhar No:[{}] ", aadhaarResponseDetails.get().getAuth_err_code());
				throw new BadRequestException(aadhaarResponseDetails.get().getAuth_err_code());
			}
			}
			Optional<RegServiceDTO> regServiceDTOoptional = regServiceDAO
					.findByApplicationNo(financierActionVO.getApplicationNo());
			if (!regServiceDTOoptional.isPresent()) {
				logger.error("no record found in registrationServices with appNo [{}]",
						financierActionVO.getApplicationNo());
				throw new BadRequestException(
						"no record found in registrationServices with appNo" + financierActionVO.getApplicationNo());
			}
			RegServiceDTO regServiceDTO = regServiceDTOoptional.get();
			RegistrationDetailsDTO regDetailsDTO = regServiceDTO.getRegistrationDetails();
			FinanceDetailsDTO financeDetailsDTO = new FinanceDetailsDTO();
			financeDetailsDTO.setCreatedDate(LocalDateTime.now());
			financeDetailsDTO.setApplicationNo(financierActionVO.getApplicationNo());
			financeDetailsDTO.setFinancerName(userDTO.getFirstName());
			financeDetailsDTO.setUserId(userDTO.getUserId());
			financeDetailsDTO.setReqAuthType(financierActionVO.getReqAuthType());
			AadhaarDetailsResponseDTO aadhaarDetailsResponseDTO = new AadhaarDetailsResponseDTO();
			if(financierActionVO.getReqAuthType().equalsIgnoreCase("Without Aadhar")) {
				aadhaarDetailsResponseDTO.setUuId(UUID.randomUUID());
				}else {
					aadhaarDetailsResponseDTO.setUuId(aadhaarResponseDetails.get().getUuId());
				}
			financeDetailsDTO.setAadharResponse(aadhaarDetailsResponseDTO);
			if (financierActionVO.getIsHPA()) {
				handleHPA(regServiceDTO, financierActionVO, financeDetailsDTO, userDTO);
			} else {
				handleHPT(regServiceDTO, financierActionVO, financeDetailsDTO, userDTO);
			}
			if (regServiceDTO.getServiceType().contains(ServiceEnum.HPA)
					&& regServiceDTO.getServiceType().contains(ServiceEnum.HIREPURCHASETERMINATION) && financierActionVO
							.getAction().toString().equalsIgnoreCase(StatusRegistration.APPROVED.getDescription())) {
				if (regServiceDTO.getIsHPADone() && regServiceDTO.getIsHPTDone()) {
					registratrionServicesApprovals.initiateApprovalProcessFlow(regServiceDTO);
				}
			}
			if (StatusRegistration.APPROVED.getDescription()
					.equalsIgnoreCase(financierActionVO.getAction().getDescription())) {
				if (!regServiceDTO.getApplicationStatus().equals(StatusRegistration.SELLERCOMPLETED)
						&& regServiceDTO.getServiceIds().contains(ServiceEnum.HPA.getId())
						&& !regServiceDTO.getServiceIds().contains(ServiceEnum.HIREPURCHASETERMINATION.getId())) {
					registratrionServicesApprovals.initiateApprovalProcessFlow(regServiceDTO);
				}
			}

			if (StatusRegistration.APPROVED.getDescription()
					.equalsIgnoreCase(financierActionVO.getAction().getDescription())) {
				if (!regServiceDTO.getApplicationStatus().equals(StatusRegistration.SELLERCOMPLETED)
						&& !regServiceDTO.getServiceIds().contains(ServiceEnum.HPA.getId())
						&& regServiceDTO.getServiceIds().contains(ServiceEnum.HIREPURCHASETERMINATION.getId())) {
					registratrionServicesApprovals.initiateApprovalProcessFlow(regServiceDTO);
				}
			}

			financeDetailsDTO.setLastUpdated(LocalDateTime.now());
			regServiceDTO.setFinanceDetails(financeDetailsDTO);
			logMovingService.moveStagingToLog(regDetailsDTO.getApplicationNo());
			regServiceDAO.save(regServiceDTO);
		}
	}

	public AadhaarSourceDTO setAadhaarSourceDetails(FinancierActionVO financierActionVO) {
		AadhaarSourceDTO aadhaarSourceDTO = new AadhaarSourceDTO();
		aadhaarSourceDTO.setApplicationNo(financierActionVO.getApplicationNo());
		aadhaarSourceDTO.setPrNo(financierActionVO.getPrNo());
		aadhaarSourceDTO.setTrNo(financierActionVO.getTrNo());
		// As Financier Issue Occurred Modified fincancierActionVO.getIsHpa modified to
		// true
		aadhaarSourceDTO.setHPA(true);
		aadhaarSourceDTO.setUser(financierActionVO.getUserId());
		return aadhaarSourceDTO;
	}

	@Override
	public void doFinanceProcess(FinancierActionVO financierActionVO, UserDTO userDTO) {

		if (!financierActionVO.getAadhaarRequest().getUid_num().equals(userDTO.getAadharNo())) {
			logger.error("Unauthorized Aadhaar Details for aadhar No [{}]",
					financierActionVO.getAadhaarRequest().getUid_num());
			throw new BadRequestException(
					"Unauthorised Aadhaar Details :" + financierActionVO.getAadhaarRequest().getUid_num());
		}
		Optional<StagingRegistrationDetailsDTO> regDetails = stagingRegistrationDetailsDAO
				.findByApplicationNo(financierActionVO.getApplicationNo());

		if (!regDetails.isPresent()) {
			logger.error(appMessages.getLogMessage(MessageKeys.FINANCE_APPLICATION_NOT_FOUND),
					financierActionVO.getApplicationNo());
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.FINANCE_APPLICATION_NOT_FOUND));
		}
		FinanceDetailsDTO financeDetailsDTO = regDetails.get().getFinanceDetails();
		Optional<AadharDetailsResponseVO> aadhaarResponseDetails = Optional.empty();
		AadhaarSourceDTO aadhaarSourceDTO = setAadhaarSourceDetails(financierActionVO);
		if(!financierActionVO.getReqAuthType().equalsIgnoreCase("Without Aadhar")) {
		if (null != financierActionVO.getAadhaarRequest())
			aadhaarResponseDetails = restGateWayService.validateAadhaar(financierActionVO.getAadhaarRequest(),
					aadhaarSourceDTO);
		logger.debug("Aadhaar Validation Service called");
		if (aadhaarResponseDetails.isPresent()) {
			if (aadhaarResponseDetails.get().getAuth_status().equals("SUCCESS")) {
				logger.debug(appMessages.getLogMessage(MessageKeys.FINANCE_TOKEN_AAHDDAR_AUTHENTICATION),
						aadhaarResponseDetails.get().getAuth_err_code());
			} else {
				logger.error(appMessages.getLogMessage(MessageKeys.FINANCE_TOKEN_AAHDDAR_AUTHENTICATION),
						aadhaarResponseDetails.get().getUid(), aadhaarResponseDetails.get().getAuth_err_code());
				throw new BadRequestException(aadhaarResponseDetails.get().getAuth_err_code());
			}
		} else {
			logger.error(appMessages.getLogMessage(MessageKeys.FINANCE_TOKEN_AAHDDAR_AUTHENTICATION_NO_SERVICE));
			throw new BadRequestException(
					appMessages.getResponseMessage(MessageKeys.FINANCE_TOKEN_AAHDDAR_AUTHENTICATION_NO_SERVICE));
		}
		}
		if (financeDetailsDTO != null) {
			financeDetailsDTO.setCreatedDate(LocalDateTime.now());
			financeDetailsDTO.setApplicationNo(financierActionVO.getApplicationNo());
			financeDetailsDTO.setFinancerName(userDTO.getFirstName());
			financeDetailsDTO.setUserId(userDTO.getUserId());
			if (financierActionVO.getMasterFinanciervo() != null) {
				financeDetailsDTO
						.setFinanceType(masterFinanceTypeMapper.convertVO(financierActionVO.getMasterFinanciervo()));
			}

			if (financierActionVO.getAction().toString().equalsIgnoreCase("APPROVED")) {
				if (!userDTO.getActionItems().stream()
						.anyMatch(actionItem -> actionItem.getActionName().equals(ActionTypeEnum.HPA.getDesc()))) {
					logger.error(UNAUTHORIZED_ACTION);
					throw new BadRequestException(UNAUTHORIZED_ACTION);
				}
				if (null == financeDetailsDTO.getStatus()) {
					financeDetailsDTO.setStatus(StatusRegistration.FINANCIERSANCTIONED.getDescription());
				}

				financeDetailsDTO.setSanctionedAmount(financierActionVO.getSanctionedAmount());
				regDetails.get().setStageNo(StageEnum.FOUR.getNumber());
				financeDetailsDTO.setAgreementDate(LocalDate.now());
			} else {
				if (!userDTO.getActionItems().stream()
						.anyMatch(actionItem -> actionItem.getActionName().equals(ActionTypeEnum.HAT.getDesc()))) {
					logger.error(UNAUTHORIZED_ACTION);
					throw new BadRequestException(UNAUTHORIZED_ACTION);
				}
				if (financierActionVO.getComments() != null) {
					financeDetailsDTO.setComments(financierActionVO.getComments());
				}
				financeDetailsDTO.setStatus(StatusRegistration.FINANCIERREJECTED.getDescription());
			}
			financeDetailsDTO.setLastUpdated(LocalDateTime.now());
			financeDetailsDTO.setUserId(userDTO.getUserId());
			financeDetailsDTO.setReqAuthType(financierActionVO.getReqAuthType());
			// Need to Write Aadhaar Response
			AadhaarDetailsResponseDTO aadhaarDetailsResponseDTO = new AadhaarDetailsResponseDTO();
			if(financierActionVO.getReqAuthType().equalsIgnoreCase("Without Aadhar")) {
				aadhaarDetailsResponseDTO.setUuId(UUID.randomUUID());
				}else {
					aadhaarDetailsResponseDTO.setUuId(aadhaarResponseDetails.get().getUuId());
				}
			financeDetailsDTO.setAadharResponse(aadhaarDetailsResponseDTO);
		}
		regDetails.get().setFinanceDetails(financeDetailsDTO);
		financeDetailsDAO.save(financeDetailsDTO);
		stagingRegistrationDetailsDAO.save(regDetails.get());
	}

	private void handleHPT(RegServiceDTO regServiceDTO, FinancierActionVO financierActionVO,
			FinanceDetailsDTO financeDetailsDTO, UserDTO userDTO) {
		if (userDTO.getActionItems().stream().anyMatch(data -> data.getActionName().equals(ActionTypeEnum.HAT.getDesc()))) {
			if (regServiceDTO.getIsHPTDone()) {
				logger.error("The application was already apporved by financier with application NO  [{}]",
						regServiceDTO.getApplicationNo());
				throw new BadRequestException("The application was already approved by financier.");
			}
			logger.info("HPT service [{}]", financierActionVO.getApplicationNo());

			if (null == regServiceDTO.getRegistrationDetails().getFinanceDetails().getUserId()) {
				logger.error(appMessages.getLogMessage(MessageKeys.FINANCE_APPLICATION_NOT_FOUND),
						financierActionVO.getApplicationNo());
				throw new BadRequestException(
						appMessages.getResponseMessage(MessageKeys.FINANCE_APPLICATION_NOT_FOUND));
			}
			if (StatusRegistration.APPROVED.getDescription()
					.equalsIgnoreCase(financierActionVO.getAction().getDescription())) {
				financeDetailsDTO.setStatus(StatusRegistration.FINANCIERTERMINATED.getDescription());
				financeDetailsDTO.setTerminateDate(LocalDateTime.now());
				if (regServiceDTO.getFinanceDetails() != null
						&& regServiceDTO.getFinanceDetails().getAgreementDate() != null) {
					financeDetailsDTO.setAgreementDate(regServiceDTO.getFinanceDetails().getAgreementDate());
				}
				if (regServiceDTO.getFinanceDetails() != null
						&& regServiceDTO.getFinanceDetails().getFinanceType() != null) {
					financeDetailsDTO.setFinanceType(regServiceDTO.getFinanceDetails().getFinanceType());
				}
				regServiceDTO.setIsHPTDone(true);
				// Tow with HPT condition
				if (regServiceDTO.getServiceIds().contains(ServiceEnum.TRANSFEROFOWNERSHIP.getId())
						&& regServiceDTO.getServiceIds().contains(ServiceEnum.HIREPURCHASETERMINATION.getId())) {
					if (regServiceDTO.getBuyerDetails() != null
							&& regServiceDTO.getBuyerDetails().getTransferType() != null) {
						if (regServiceDTO.getBuyerDetails().getTransferType().equals(TransferType.SALE)
								&& regServiceDTO.getBuyerDetails().getBuyer() == null) {
							regServiceDTO.setApplicationStatus(StatusRegistration.SELLERCOMPLETED);
						}
					}
				}
			} else {
				if (financierActionVO.getComments() != null) {
					financeDetailsDTO.setComments(financierActionVO.getComments());
				}
				financeDetailsDTO.setStatus(StatusRegistration.REJECTED.getDescription());
				regServiceDTO.setApplicationStatus(StatusRegistration.FINANCIERREJECTED);
			}

		} else {
			logger.error("User do not have permissions to do HPT please check Users ActionItems");
			throw new BadRequestException("You are not authorized to do HPT action");
		}
	}

	private void handleHPA(RegServiceDTO regServiceDTO, FinancierActionVO financeActionVO,
			FinanceDetailsDTO financeDetailsDTO, UserDTO userDTO) {

		if (userDTO.getActionItems().stream().anyMatch(data -> data.getActionName().equals(ActionTypeEnum.HPA.getDesc()))) {
			if (regServiceDTO.getIsHPADone()) {
				logger.error("the application  No : [{}]was already approved by financier ",
						regServiceDTO.getAadhaarNo());
				throw new BadRequestException("The application was already approved by finacier.");
			}
			logger.info("HPA service for application No: [{}]", financeActionVO.getApplicationNo());
			if (userDTO.getActionItems() != null && !userDTO.getActionItems().stream()
					.anyMatch(actionItem -> actionItem.getActionName().equals(ActionTypeEnum.HPA.getDesc()))) {
				logger.error("Unauthorized user Action, Required Action for user is ", ActionTypeEnum.HPA.getDesc());
				throw new BadRequestException(MessageKeys.UNAUTHORIZED_USER);
			}
			if (StatusRegistration.APPROVED.getDescription()
					.equalsIgnoreCase(financeActionVO.getAction().getDescription())) {
				financeDetailsDTO.setStatus(StatusRegistration.FINANCIERSANCTIONED.getDescription());
				financeDetailsDTO.setAgreementDate(LocalDate.now());
				if (financeActionVO.getMasterFinanciervo() != null) {
					financeDetailsDTO
							.setFinanceType(masterFinanceTypeMapper.convertVO(financeActionVO.getMasterFinanciervo()));
				}
				regServiceDTO.setIsHPADone(true);
			} else {
				if (financeActionVO.getComments() != null) {
					financeDetailsDTO.setComments(financeActionVO.getComments());
				}
				financeDetailsDTO.setStatus(StatusRegistration.FINANCIERREJECTED.getDescription());
			}
		} else {
			logger.error("User do not have permissions to do HPA");
			throw new BadRequestException("You do not have permissions to do HPA");
		}
	}

	private List<StagingRegistrationDetailsDTO> getFinacierPendigList(List<String> list, String userId) {
		return stagingRegistrationDetailsDAO.findByFinanceDetailsStatusInAndFinanceDetailsUserId(list, userId);
	}

	/*
	 * private List<RegistrationDetailsDTO> getFinacierPRgeneratedList(String
	 * userId) { return registrationdetailDAO.findByFinanceDetailsUserId(userId); }
	 */
	@Override
	public FinancierDashBoardVO fetchFinancierDashBoard(String userId) {

		List<String> list = new ArrayList<>();
		list.add(StatusRegistration.DEALERTOKENAPPROVED.toString());
		list.add(StatusRegistration.FINANCIERSANCTIONED.toString());
		list.add(StatusRegistration.FINANCIERREJECTED.toString());
		list.add(StatusRegistration.DEALERREJECTED.toString());
		Long startTime = System.currentTimeMillis();
		logger.info("Getting Finance DashBoard Start Dealer [{}] Time :[{}]", userId, startTime);
		List<StagingRegistrationDetailsDTO> registrationList = getFinacierPendigList(list, userId);
		logger.info("Getting Finance DashBoard Start Dealer [{}] Time :[{}], count [{}]", userId,
				System.currentTimeMillis() - startTime, registrationList.size());

		Map<String, Integer> details = new HashMap<>();
		Map<String, Integer> detailsPr = new HashMap<>();
		for (StagingRegistrationDetailsDTO regDetails : registrationList) {

			if (details.containsKey(regDetails.getFinanceDetails().getStatus())) {
				details.put(regDetails.getFinanceDetails().getStatus(),
						details.get(regDetails.getFinanceDetails().getStatus()) + new Integer(1));
			} else
				details.put(regDetails.getFinanceDetails().getStatus(), 1);

		}

		FinancierDashBoardVO financierDashBoardVO = new FinancierDashBoardVO();
		financierDashBoardVO.setSanctioned((details.get(StatusRegistration.FINANCIERSANCTIONED.toString()) == null ? 0
				: details.get(StatusRegistration.FINANCIERSANCTIONED.toString())));
		financierDashBoardVO.setAccepted((details.get(StatusRegistration.DEALERTOKENAPPROVED.toString()) == null ? 0
				: details.get(StatusRegistration.DEALERTOKENAPPROVED.toString())));
		financierDashBoardVO
				.setFinancerRejected((details.get(StatusRegistration.FINANCIERREJECTED.toString()) == null ? 0
						: details.get(StatusRegistration.FINANCIERREJECTED.toString())));
		financierDashBoardVO.setDealerRejected((details.get(StatusRegistration.DEALERREJECTED.toString()) == null ? 0
				: details.get(StatusRegistration.DEALERREJECTED.toString())));
		financierDashBoardVO.setPrGenerated((detailsPr.get(StatusRegistration.PRGENERATED.toString()) == null ? 0
				: detailsPr.get(StatusRegistration.PRGENERATED.toString())));
		financierDashBoardVO.setRejected(0);
		financierDashBoardVO.setFreshRcList(freshRCCount(userId));
		financierDashBoardVO.setFreshRcrejectedList(freshRcRejectedCount(userId));

		return financierDashBoardVO;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<RegistrationDetailsVO> getOwnerDetailsByToken(FinanceDetailsVO financeDetailsVO) {
		Optional<StagingRegistrationDetailsDTO> registrationDetailsDTO = stagingRegistrationDetailsDAO
				.findByFinanceDetailsToken(financeDetailsVO.getToken());
		if (registrationDetailsDTO.isPresent() && null != registrationDetailsDTO.get().getFinanceDetails()
				&& null != registrationDetailsDTO.get().getFinanceDetails().getStatus()) {
			logger.error("Already Action Taken, Please verify once.");
			throw new BadRequestException("Already Action Taken, Please verify once.");
		}
		if (registrationDetailsDTO.isPresent()) {
			Optional<RegistrationDetailsVO> registrationDetailsVO = registrationDetailsMapper
					.convertEntity(registrationDetailsDTO);
			return registrationDetailsVO;
		} else {
			logger.warn(appMessages.getLogMessage(MessageKeys.FINANCE_TOKEN_DETAILS), financeDetailsVO.getToken());
		}
		return Optional.empty();
	}

	@Override
	public Optional<List<RegistrationDetailsVO>> getViewDetails(String type, String userId) {
		if (StringUtils.isBlank(type)) {
			type = StringUtils.EMPTY;
		}
		logger.info(appMessages.getLogMessage(MessageKeys.FINANCE_TOKEN_FILTER_TYPE), type, userId);
		List<String> list = new ArrayList<>();
		list.add(StatusRegistration.DEALERTOKENAPPROVED.toString());
		list.add(StatusRegistration.FINANCIERSANCTIONED.toString());
		list.add(StatusRegistration.FINANCIERREJECTED.toString());
		list.add(StatusRegistration.DEALERREJECTED.toString());
		List<StagingRegistrationDetailsDTO> registrationList = getFinacierPendigList(list, userId);
		logger.info("financier pending records [{}]", registrationList.size());
		List<RegistrationDetailsVO> regDetailsListVo = new ArrayList<>();
		for (StagingRegistrationDetailsDTO regDetails : registrationList) {
			if (type.isEmpty()) {
				regDetailsListVo.add(registrationDetailsMapper.convertEntity(regDetails));
			} else if (list.contains(type)) {
				regDetailsListVo.add(registrationDetailsMapper.limitedFiledsForDashBoard(regDetails));
			}
		}

		return Optional.of(regDetailsListVo);
	}

	@Override
	public List<MasterFinanceTypeVO> findFinanceTypeByStatus(boolean status) {
		return masterFinanceTypeMapper.convertEntity(masterFinanceTypeDAO.findByStatus(status));
	}

	@Override

	public Optional<UserVO> savefinancierDetails(UserVO uservo) {
		UserDTO userDTO = new UserDTO();
		if (uservo.getUserRelation().equals("child")) {
			Optional<UserDTO> dto = userDao.findByUserId(uservo.getParentId());
			if (!dto.isPresent()) {
				logger.error("Parent id does not exists for user: [{}]", uservo.getUserId());
				throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.PARENT_ID_DOES_NOT_EXIST));
			}
		}
		UserDTO user = userDao.findByAadharNo(uservo.getAadharNo());
		if (user != null) {
			logger.error("User already exists with aadhar No: [{}] ", uservo.getAadharNo());
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.AADHAR_NO_ALREADY_EXISTS));
		}
		user = new UserDTO();
		String officeCode = uservo.getMandal().getTransportOfice();
		String officeName = StringUtils.EMPTY;
		if (StringUtils.isBlank(officeCode)) {
			officeCode = uservo.getMandal().getNonTransportOffice();
		}
		Optional<OfficeDTO> opOffice = officeDao.findByOfficeCode(officeCode);
		if (opOffice.isPresent()) {
			officeName = opOffice.get().getOfficeName();
		}
		OfficeVO officeVo = new OfficeVO();
		officeVo.setOfficeCode(officeCode);
		officeVo.setOfficeName(officeName);
		uservo.setOffice(officeVo);
		userDTO = userMapper.convertVO(uservo);
		userDTO.setParentId(uservo.getParentId());
		userDTO.setUserId(geneateFinancerApplicationSeries());
		userDTO.setPassword(financierPassword);
		RolesDTO rolesDTO = new RolesDTO();
		rolesDTO.setName(StatusRegistration.FINANCE.getDescription());
		userDTO.setPrimaryRole(rolesDTO);
		userDTO.setAdditionalRoles(Collections.EMPTY_LIST);
		userDTO.setUserStatus(UserStatusEnum.ACTIVE);
		user = userDao.save(userDTO);
		UserVO vo = userMapper.UserAndPassword(user);
		return Optional.of(vo);
	}

	@Override
	public String geneateFinancerApplicationSeries() {

		List<FinancierSeriesDTO> financierSeriesOptionalDTO;
		StringBuilder financerApplicationSeries = new StringBuilder();

		financierSeriesOptionalDTO = financierSeriesDAO.findAll();
		logger.debug("generateFinancierApplicationSeries Called");

		if (null == financierSeriesOptionalDTO || financierSeriesOptionalDTO.isEmpty()) {
			logger.error(appMessages.getResponseMessage(MessageKeys.FINANCIER_NO_RECORDS_TO_GENERATE_APPLICATION_NO));
			throw new BadRequestException(
					appMessages.getResponseMessage(MessageKeys.FINANCIER_NO_RECORDS_TO_GENERATE_APPLICATION_NO));
		}
		boolean flag = true;
		FinancierSeriesDTO financierSeriesDTO = financierSeriesOptionalDTO.get(0);
		// read CurrentNumber
		Integer currentNumber = financierSeriesDTO.getCurrentNo();
		// read StartFrom
		Integer start = financierSeriesDTO.getStartFrom();
		// read year
		Integer dbYear = financierSeriesDTO.getYear();
		// append series
		financerApplicationSeries.append(financierSeriesDTO.getSeries());
		Integer currentYear = LocalDate.now().getYear();
		// update currentNo
		currentNumber = currentNumber + 1;
		while (flag) {
			if (currentYear > dbYear) {
				financierSeriesDTO.setYear(currentYear);
				currentNumber = start;
				Integer updateYearWithoutTwenty = removeFromYear(currentYear);
				financerApplicationSeries.append(updateYearWithoutTwenty);
			} else {
				Integer updateYearWithoutTwenty = removeFromYear(dbYear);
				financierSeriesDTO.setYear(dbYear);
				financerApplicationSeries.append(updateYearWithoutTwenty);
			}
			flag = false;
			financierSeriesDTO.setCurrentNo(currentNumber);
			// Update with recent Data
			updateCurrentNo(financierSeriesDTO);
			String currentAppNoWithZero = appendZero(currentNumber, 6);
			financerApplicationSeries.append(currentAppNoWithZero);
		}
		return financerApplicationSeries.toString();
	}

	private Integer removeFromYear(Integer year) {
		Integer result = Integer.parseInt(String.valueOf(year).substring(2, 4));
		return result;
	}

	private String appendZero(Integer number, int length) {
		return String.format("%0" + (length) + "d", number);
	}

	@Override
	public FinancierSeriesDTO updateCurrentNo(FinancierSeriesDTO financierSeriesDTO) {
		FinancierSeriesDTO financierSeries = null;
		financierSeries = financierSeriesDAO.save(financierSeriesDTO);
		return financierSeries;
	}

	@Override
	public Optional<RegServiceVO> getDetailsBasedOnengNOChassNO(RequestDetailsVO requestDetailsVO, UserDTO user) {
		if (!StringUtils.isEmpty(requestDetailsVO.getPrNo())) {
			RegServiceDTO registrationDetailsDTO = regServiceDAO
					.findByFinanceDetailsUserIdAndPrNoAndServiceIdsInAndCurrentIndexIsNull(user.getUserId(),
							requestDetailsVO.getPrNo(), ServiceEnum.HIREPURCHASETERMINATION.getId());
			if (registrationDetailsDTO != null) {
				return Optional.of(regServiceMapper.convertEntity(registrationDetailsDTO));
			}
			return Optional.empty();
		}
		if (!StringUtils.isEmpty(requestDetailsVO.getApplicationNo())) {
			RegServiceDTO registrationDetailsDTO = regServiceDAO
					.findByFinanceDetailsUserIdAndApplicationNoAndServiceIdsInAndCurrentIndexIsNull(user.getUserId(),
							requestDetailsVO.getApplicationNo(), ServiceEnum.HIREPURCHASETERMINATION.getId());
			if (registrationDetailsDTO != null) {
				return Optional.of(regServiceMapper.convertEntity(registrationDetailsDTO));
			}
			return Optional.empty();
		}
		if (!StringUtils.isEmpty(requestDetailsVO.getTrNo())) {
			RegServiceDTO registrationDetailsDTO = regServiceDAO
					.findByFinanceDetailsUserIdAndTrNoAndServiceIdsInAndCurrentIndexIsNull(user.getUserId(),
							requestDetailsVO.getTrNo(), ServiceEnum.HIREPURCHASETERMINATION.getId());
			if (registrationDetailsDTO != null) {
				return Optional.of(regServiceMapper.convertEntity(registrationDetailsDTO));
			}
			return Optional.empty();
		}

		/*
		 * if (!StringUtils.isEmpty(requestDetailsVO.getChassisNo()) &&
		 * !StringUtils.isEmpty(requestDetailsVO.getEngineNo())) {
		 * RegistrationDetailsDTO registrationDetailsDTO = registrationdetailDAO
		 * .findByFinanceDetailsUserIdAndVahanDetailsChassisNumberAndVahanDetailsEngineNumber
		 * (userId, requestDetailsVO.getChassisNo(), requestDetailsVO.getEngineNo()); if
		 * (registrationDetailsDTO != null) { return
		 * Optional.of(registrationMapper.convertEntity(registrationDetailsDTO)); }
		 * return Optional.empty(); }
		 *//** returning null if it is not in the any above cases **/
		return null;
	}

	@Override
	public FinancierDashBoardVO fetchFinancierDashBoardDetailsForServices(String userId) {
		// TODO Auto-generated method stub

		Integer hptCount = 0;
		FinancierDashBoardVO financierDashBoardVO = new FinancierDashBoardVO();
		List<String> statusList = hptStatus();
		List<RegServiceDTO> getServicesCount = getHPTServiceCount(userId, statusList);
		if (!getServicesCount.isEmpty()) {
			Optional<UserDTO> users = userDao.findByUserId(userId);
			if(users.isPresent() && users.get().getActionItems().stream().anyMatch(action->action.getActionName().equals(ActionTypeEnum.HAT.getDesc()))) {
			hptCount = getServicesCount.size();
			}
		}
		financierDashBoardVO.setHirepurchaseTerminated(hptCount);

		return financierDashBoardVO;

	}

	/**
	 * NOTE : Observed few aplicationNo of regService and staging are same . To
	 * avoid this calling both regService and staging to pick right one for
	 * freshFinance/HPA .
	 **/

	@Override
	public boolean serviceType(String applicationNo) {
		boolean hpaService = true;
		Optional<RegServiceDTO> regServiceOptional = regServiceDAO.findByApplicationNo(applicationNo);
		if (regServiceOptional.isPresent()) {
			RegServiceDTO regDTO = regServiceOptional.get();
			String appNo = regDTO.getRegistrationDetails().getApplicationNo();
			Optional<RegistrationDetailsDTO> regOptional = registrationdetailDAO.findByApplicationNo(appNo);
			if (regDTO.getServiceIds().contains(ServiceEnum.DATAENTRY.getId())) {
				regOptional = Optional.of(regDTO.getRegistrationDetails());
			}

			if (regOptional.isPresent()) {
				return hpaService;
			} else {
				hpaService = false;
				return hpaService;
			}
		} else {
			Optional<StagingRegistrationDetailsDTO> stagingOptional = stagingRegistrationDetailsDAO
					.findByApplicationNo(applicationNo);
			if (stagingOptional.isPresent()) {
				hpaService = false;
			}
		}

		return hpaService;
	}

	private List<String> getStatusListForRegistrations() {
		List<String> statusList = new ArrayList<>();
		statusList.add(StatusRegistration.FINANCIERSANCTIONED.getDescription());
		statusList.add(StatusRegistration.TOWITHHPTINITIATED.getDescription());
		statusList.add(StatusRegistration.DEALERTOKENAPPROVED.getDescription());

		return statusList;
	}

	private List<String> hptStatus() {
		List<String> statusList = new ArrayList<>();
		statusList.add(StatusRegistration.HPTINITIATED.getDescription());

		return statusList;
	}

	private List<RegServiceDTO> getHPTServiceCount(String userId, List<String> statusList) {
		List<String> childDetailsFilteredList = new ArrayList<>();
		Optional<UserDTO> users = userDao.findByUserId(userId);
		// child financier
		List<StatusRegistration> status = new ArrayList<>();
		status.add(StatusRegistration.REJECTED);
		status.add(StatusRegistration.FINANCIERREJECTED);
		status.add(StatusRegistration.CANCELED);
		if (StringUtils.isNotBlank(users.get().getParentId())) {
			List<UserDTO> childs = userDao.findByParentId(users.get().getParentId());
			if (null != childs) {
				for (UserDTO child : childs) {
					if (null != child.getActionItems()) {
						for (ActionDTO action : child.getActionItems()) {
							if (action != null && action.getActionName().equals(ActionTypeEnum.HAT.getDesc())) {
								childDetailsFilteredList.add(child.getUserId());
							}
						}
					}
				}
			}
			childDetailsFilteredList.add(users.get().getParentId());
			if (!childDetailsFilteredList.isEmpty()) {
				return regServiceDAO
						.findByHptStatusInAndFinanceDetailsUserIdNotNullAndServiceIdsInAndApplicationStatusNotInAndIsHPTDoneFalse(
								statusList, childDetailsFilteredList, ServiceEnum.HIREPURCHASETERMINATION.getId(),
								status);
			}
		}

		// parent financier
		else {
			List<UserDTO> childDetailsList = userDao.findByParentId(userId);
			for (UserDTO userDTO : childDetailsList) {
				childDetailsFilteredList.add(userDTO.getUserId());
			}
			childDetailsFilteredList.add(userId);
			if (!childDetailsFilteredList.isEmpty()) {
				return regServiceDAO
						.findByHptStatusInAndFinanceDetailsUserIdNotNullAndServiceIdsInAndApplicationStatusNotInAndIsHPTDoneFalse(
								statusList, childDetailsFilteredList, ServiceEnum.HIREPURCHASETERMINATION.getId(),
								status);
			}
		}
		return Collections.emptyList();
	}

	public List<String> getFinanceSiblings(UserDTO users, List<String> childDetailsFilteredList) {
		List<UserDTO> childs = userDao.findByParentId(users.getParentId());
		List<String> siblings = childs.stream().map(val -> val.getUserId()).collect(Collectors.toList());
		childDetailsFilteredList.addAll(siblings);
		return childDetailsFilteredList;
	}

	@Override
	public Optional<List<RegServiceVO>> getViewDetailsForServices(String type, String userId) {
		if (StringUtils.isBlank(type)) {
			type = StringUtils.EMPTY;
		}
		logger.info(appMessages.getLogMessage(MessageKeys.FINANCE_TOKEN_FILTER_TYPE), type, userId);
		List<String> list = getStatusListForRegistrations();
		List<String> hptList = hptStatus();

		List<RegServiceDTO> registrationList = getHPTServiceCount(userId, hptList);
		/*
		 * if (registrationList == null || registrationList.isEmpty()) {
		 * registrationList = getFinacierPendigListForServices(list, userId); }
		 */
		List<RegServiceVO> regDetailsListVo = new ArrayList<>();
		for (RegServiceDTO regDetails : registrationList) {
			if (type.isEmpty()) {
				regDetailsListVo.add(regServiceMapper.convertEntity(regDetails));
			} else if (list.contains(type)) {
				regDetailsListVo.add(regServiceMapper.limitedDashBoardfields(regDetails));
			} else if (hptList.contains(type)) {
				regDetailsListVo.add(regServiceMapper.convertEntity(regDetails));
			}
		}
		return Optional.of(regDetailsListVo);
	}

	/*
	 * private List<RegServiceDTO> getFinacierPendigListForServices(List<String>
	 * list, String userId) { return regServiceDAO
	 * .findByHptStatusInAndFinanceDetailsUserIdAndServiceIdsInAndApplicationStatusNotAndIsHPTDoneFalse
	 * (list, userId, ServiceEnum.HIREPURCHASETERMINATION.getId(),
	 * StatusRegistration.REJECTED);
	 * 
	 * }
	 */

	@Override
	public Optional<RegServiceVO> getOwnerDetailsOfSerivicesByApplicationNo(FinanceDetailsVO financeDetailsVO) {
		Optional<RegServiceDTO> regDetails = regServiceDAO
				.findByApplicationNo(financeDetailsVO.getApplicationNo().trim());

		if (regDetails.isPresent()) {
			Optional<RegServiceVO> regServiceVO = regServiceMapper.convertEntity(regDetails);
			return regServiceVO;
		}

		return Optional.empty();
	}

	@Override
	public Optional<RegServiceVO> getOwnerDetailsOfSerivicesByPrNo(String prNo) {
		RegServiceVO regDetails = registrationService.findByprNo(prNo);

		if (null != regDetails) {
			return Optional.of(regDetails);
		}

		return Optional.empty();
	}

	@Override
	public void doActionForServices(FinancierActionVO financierActionVO) {

		Optional<UserDTO> userDetails = userDao.findByUserId(financierActionVO.getUserId());

		if (!userDetails.isPresent()) {
			logger.error("Unauthorized user", financierActionVO.getUserId());
			throw new BadRequestException("Unauthorised User :" + financierActionVO.getUserId());
		}

		if (!financierActionVO.getAadhaarRequest().getUid_num().equals(userDetails.get().getAadharNo())) {
			logger.error("Unauthorized Aadhaar Details [{}]", financierActionVO.getAadhaarRequest().getUid_num());
			throw new BadRequestException(
					"Unauthorised Aadhaar Details :" + financierActionVO.getAadhaarRequest().getUid_num());
		}

		Optional<RegServiceDTO> regDetails = regServiceDAO.findByApplicationNo(financierActionVO.getApplicationNo());

		if (!regDetails.isPresent()) {
			logger.error(appMessages.getLogMessage(MessageKeys.FINANCE_APPLICATION_NOT_FOUND),
					financierActionVO.getApplicationNo());
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.FINANCE_APPLICATION_NOT_FOUND));
		}

		FinanceDetailsDTO financeDetailsDTO = regDetails.get().getRegistrationDetails().getFinanceDetails();

		// TODO validate mulitple aadhaar number(single dealer id may use multiple user)

		Optional<AadharDetailsResponseVO> aadhaarResponseDetails = Optional.empty();
		AadhaarSourceDTO aadhaarSourceDTO = setAadhaarSourceDetails(financierActionVO);

		if (null != financierActionVO.getAadhaarRequest())
			aadhaarResponseDetails = restGateWayService.validateAadhaar(financierActionVO.getAadhaarRequest(),
					aadhaarSourceDTO);

		if (aadhaarResponseDetails.isPresent()) {

			if (aadhaarResponseDetails.get().getAuth_status().equals("SUCCESS")) {
				logger.debug(appMessages.getLogMessage(MessageKeys.FINANCE_TOKEN_AAHDDAR_AUTHENTICATION),
						aadhaarResponseDetails.get().getAuth_err_code());

			} else {
				logger.error(appMessages.getLogMessage(MessageKeys.FINANCE_TOKEN_AAHDDAR_AUTHENTICATION),
						aadhaarResponseDetails.get().getUid(), aadhaarResponseDetails.get().getAuth_err_code());
				throw new BadRequestException(aadhaarResponseDetails.get().getAuth_err_code());
			}
		} else {
			logger.error(appMessages.getLogMessage(MessageKeys.FINANCE_TOKEN_AAHDDAR_AUTHENTICATION_NO_SERVICE));
			throw new BadRequestException(
					appMessages.getResponseMessage(MessageKeys.FINANCE_TOKEN_AAHDDAR_AUTHENTICATION_NO_SERVICE));
		}

		if (financeDetailsDTO != null) {

			// Need to wait
			if (null == financeDetailsDTO.getCreatedDate())
				financeDetailsDTO.setCreatedDate(LocalDateTime.now());
			financeDetailsDTO.setApplicationNo(financierActionVO.getApplicationNo());
			financeDetailsDTO.setFinancerName(userDetails.get().getFirstName());
			financeDetailsDTO.setUserId(financierActionVO.getUserId());
			if (financierActionVO.getMasterFinanciervo() != null) {
				financeDetailsDTO
						.setFinanceType(masterFinanceTypeMapper.convertVO(financierActionVO.getMasterFinanciervo()));
			}

			if (financierActionVO.getAction().toString().equalsIgnoreCase(StatusActions.APPROVED.getStatus())) {
				if (!userDetails.get().getActionItems().stream()
						.anyMatch(actionItem -> actionItem.getActionName().equals(ActionTypeEnum.HPA.getDesc())))
					throw new BadRequestException("Unauthorized user to taken this Action, Please verify once.");
				if (null == financeDetailsDTO.getStatus()) {
					financeDetailsDTO.setStatus(StatusRegistration.FINANCIERAPPROVED.getDescription());
				}
				/*
				 * if(financeDetailsDTO.getStatus().equals(StatusRegistration.
				 * DEALERTOKENAPPROVED.getDescription())){
				 * financeDetailsDTO.setStatus(StatusRegistration.FINANCIERAPPROVED.
				 * getDescription()); }
				 */
				// financeDetailsDTO.setSanctionedAmount(financierActionVO.getSanctionedAmount());

			} else {
				if (!userDetails.get().getActionItems().stream()
						.anyMatch(actionItem -> actionItem.getActionName().equals(ActionTypeEnum.HAT.getDesc()))) {
					logger.error("Unauthorized user Action to do HAT");
					throw new BadRequestException("Unauthorized user to taken this Action, Please verify once.");
				}
				financeDetailsDTO.setStatus(StatusRegistration.FINANCIERREJECTED.getDescription());
			}
			financeDetailsDTO.setLastUpdated(LocalDateTime.now());
			financeDetailsDTO.setUserId(financierActionVO.getUserId());

			// Need to Write Aadhaar Response
			AadhaarDetailsResponseDTO aadhaarDetailsResponseDTO = new AadhaarDetailsResponseDTO();
			aadhaarDetailsResponseDTO.setUuId(aadhaarResponseDetails.get().getUuId());
			financeDetailsDTO.setAadharResponse(aadhaarDetailsResponseDTO);

		}
		regDetails.get().setFinanceDetails(financeDetailsDTO);
		financeDetailsDAO.save(financeDetailsDTO);
		logMovingService.moveStagingToLog(regDetails.get().getApplicationNo());
		regServiceDAO.save(regDetails.get());
	}

	@Override
	public Optional<RegServiceVO> getServiceDetailsByApplicationNumber(String applicationNo) {
		Optional<RegServiceDTO> regDetails = regServiceDAO.findByApplicationNo(applicationNo);

		if (regDetails.isPresent()) {
			Optional<RegServiceVO> regServiceVO = regServiceMapper.convertEntity(regDetails);
			return regServiceVO;
		}

		return Optional.empty();
	}

	@Override
	public Optional<RegServiceVO> getReportsForRegServices(RequestDetailsVO requestDetailsVO, String userId) {

		if (!StringUtils.isEmpty(requestDetailsVO.getApplicationNo())) {
			RegServiceDTO registrationDetailsDTO = regServiceDAO
					.findByRegistrationDetailsFinanceDetailsUserIdAndRegistrationDetailsApplicationNoAndServiceIdsNotIn(
							userId, requestDetailsVO.getApplicationNo(), ServiceEnum.TAXATION.getId());
			if (registrationDetailsDTO != null) {
				return Optional.of(regServiceMapper.convertEntity(registrationDetailsDTO));
			}
			return Optional.empty();
		}
		if (!StringUtils.isEmpty(requestDetailsVO.getTrNo())) {
			RegServiceDTO registrationDetailsDTO = regServiceDAO
					.findByRegistrationDetailsFinanceDetailsUserIdAndRegistrationDetailsTrNoAndServiceIdsNotIn(userId,
							requestDetailsVO.getTrNo(), ServiceEnum.TAXATION.getId());
			if (registrationDetailsDTO != null) {
				return Optional.of(regServiceMapper.convertEntity(registrationDetailsDTO));
			}
			return Optional.empty();
		}
		if (!StringUtils.isEmpty(requestDetailsVO.getPrNo())) {
			RegServiceDTO registrationDetailsDTO = regServiceDAO
					.findByRegistrationDetailsFinanceDetailsUserIdAndRegistrationDetailsPrNoAndServiceIdsNotIn(userId,
							requestDetailsVO.getPrNo(), ServiceEnum.TAXATION.getId());
			if (registrationDetailsDTO != null) {
				return Optional.of(regServiceMapper.convertEntity(registrationDetailsDTO));
			}
			return Optional.empty();
		}
		if (!StringUtils.isEmpty(requestDetailsVO.getChassisNo())
				&& !StringUtils.isEmpty(requestDetailsVO.getEngineNo())) {
			RegServiceDTO registrationDetailsDTO = regServiceDAO
					.findByRegistrationDetailsFinanceDetailsUserIdAndRegistrationDetailsVahanDetailsChassisNumberAndRegistrationDetailsVahanDetailsEngineNumberAndServiceIdsNotIn(
							userId, requestDetailsVO.getChassisNo(), requestDetailsVO.getEngineNo(),
							ServiceEnum.TAXATION.getId());
			if (registrationDetailsDTO != null) {
				return Optional.of(regServiceMapper.convertEntity(registrationDetailsDTO));
			}
			return Optional.empty();
		} /** returning null if it is not in the any above cases **/
		return null;
	}

	@Override
	public Optional<RegistrationDetailsVO> getOwnerDetailsByTokenForRegService(FinanceDetailsVO financeDetailsVO) {
		Optional<RegServiceDTO> registrationDetailsDTO = regServiceDAO.findByToken(financeDetailsVO.getToken());
		if (registrationDetailsDTO.isPresent()) {
			RegServiceDTO regDTO = registrationDetailsDTO.get();
			if (regDTO.getIsHPADone()) {
				logger.error("Already HPA done with this token: [{}]", financeDetailsVO.getToken());
				throw new BadRequestException("Already HPA Done for this token " + financeDetailsVO.getToken());
			}
			if (null != regDTO.getFinanceDetails() && null != regDTO.getFinanceDetails().getStatus()
					&& !regDTO.getIsHPTDone()) {
				logger.error("please dot hpt first to make HPA for application No: [{}]", regDTO.getApplicationNo());
				throw new BadRequestException("Please do HPT first to make HirePurchase Agreement for ApplicationNo :["
						+ regDTO.getApplicationNo() + "]");
			}

			if (!(regDTO.getApplicationStatus().equals(StatusRegistration.PAYMENTDONE)
					|| ((regDTO.getApplicationStatus().equals(StatusRegistration.SLOTBOOKED)
							|| regDTO.getApplicationStatus().equals(StatusRegistration.CITIZENSUBMITTED))
							&& regDTO.getServiceType().contains(ServiceEnum.DATAENTRY)))) {
				throw new BadRequestException(
						"payment is pending for this transaction . Token No :" + financeDetailsVO.getToken());
			}

			RegistrationDetailsVO registrationDetailsVO = new RegistrationDetailsVO();
			Optional<RegServiceVO> regServiceVO = regServiceMapper.convertEntity(registrationDetailsDTO);
			if (regServiceVO.isPresent()) {
				regServiceVO.get().getRegistrationDetails().setApplicationNo(regServiceVO.get().getApplicationNo());
				regServiceVO.get().getRegistrationDetails()
						.setServiceIds(regServiceVO.get().getServiceIds().stream().collect(Collectors.toList()));
				regServiceVO.get().getRegistrationDetails().setServiceType(regServiceVO.get().getServiceType());
				registrationDetailsVO = regServiceVO.get().getRegistrationDetails();
				if (regServiceVO.get().getServiceIds() != null
						&& regServiceVO.get().getServiceIds().contains(ServiceEnum.TRANSFEROFOWNERSHIP.getId())
						&& regDTO.getBuyerDetails() != null) {
					registrationDetailsVO.setBuyerdetails(towMapper.convertEntity(regDTO.getBuyerDetails()));

				}
				registrationDetailsVO.setToken(regServiceVO.get().getToken());
				return Optional.of(registrationDetailsVO);
			}
		}
		return Optional.empty();

	}

	@Override
	public Optional<RegistrationDetailsVO> getFinanceDetailsByPrNo(FinancierActionVO financierActionVO, UserDTO userDTO,
			String user) {
		List<String> errors = new ArrayList<>();
		Optional<RegistrationDetailsDTO> regDetailsOptional = regDetailsDAO.findByPrNo(financierActionVO.getPrNo());
		if (!regDetailsOptional.isPresent()) {
			logger.error("Registration details not found for prNo :[{}]", financierActionVO.getPrNo());
			throw new BadRequestException("RegistrationDetails not found with prNo" + financierActionVO.getPrNo());
		}
		
		RegistrationDetailsDTO regDetailsDTO = regDetailsOptional.get();
		if (regDetailsDTO.getFinanceDetails() == null) {
			logger.error("No finance Details found for prNo/trNo: [{}]", regDetailsDTO.getPrNo());
			throw new BadRequestException("No finance Details with prNo/trNo " +regDetailsDTO.getPrNo());
		}
		checkFinancierValidation(regDetailsDTO, userDTO, financierActionVO, financierActionVO.getPrNo(), user);
		financierAadharValidation(financierActionVO);
		errors = regService.checkVcrDues(regDetailsOptional.get(), errors);
		if (!errors.isEmpty()) {
			throw new BadRequestException("VCR details found with this prNo " + regDetailsOptional.get().getPrNo());
		}
		RegistrationDetailsVO registrationDetailsVO = registrationDetailsMapper.convertEntity(regDetailsDTO);
		return Optional.of(registrationDetailsVO);
	}

	public void financierAadharValidation(FinancierActionVO financierActionVO) {
		logger.debug("in financierAadhaarValidation");
		Optional<AadharDetailsResponseVO> aadhaarResponseDetails = null;
		AadhaarSourceDTO aadhaarSourceDTO = setAadhaarSourceDetails(financierActionVO);
		if (null != financierActionVO.getAadhaarRequest())
			aadhaarResponseDetails = restGateWayService.validateAadhaar(financierActionVO.getAadhaarRequest(),
					aadhaarSourceDTO);
		if (!aadhaarResponseDetails.isPresent()) {
			logger.error("No aadhar details found for [{}]", financierActionVO.getAadhaarRequest().getUid_num());
			logger.error(appMessages.getLogMessage(MessageKeys.FRESH_RC_FOR_FINANCE_AADHAAR_AUTHENTICATION_NO_SERVICE));
			throw new BadRequestException(
					appMessages.getResponseMessage(MessageKeys.FRESH_RC_FOR_FINANCE_AADHAAR_AUTHENTICATION_NO_SERVICE));
		}
		if (aadhaarResponseDetails.get().getAuth_status().equals(AADHAARRESPONSE.SUCCESS.getLabel())) {
			logger.debug(appMessages.getLogMessage(MessageKeys.FINANCE_TOKEN_AAHDDAR_AUTHENTICATION),
					aadhaarResponseDetails.get().getAuth_err_code());
		} else {
			logger.error(appMessages.getLogMessage(MessageKeys.FINANCE_TOKEN_AAHDDAR_AUTHENTICATION),
					aadhaarResponseDetails.get().getUid(), aadhaarResponseDetails.get().getAuth_err_code());
			throw new BadRequestException(aadhaarResponseDetails.get().getAuth_err_code());
		}

	}

	public <T> Optional<T> readValue(String value, Class<T> valueType) {

		try {
			return Optional.of(objectMapper.readValue(value, valueType));
		} catch (IOException ioe) {
			logger.error("Exception occured while converting String to Object", ioe);
		}

		return Optional.empty();
	}

	@Override
	public RegServiceVO doFreshRcForFinance(String regServiceVO, MultipartFile[] filesList, String user)
			throws IOException, RcValidationException {
		return registrationService.savingRegistrationServices(regServiceVO, filesList, user);
	}

	@Override
	public String saveUploadedFiles(UploadExcelFileVO uploadFile, String uploadBy) throws IOException, Exception {

		String mainDir = applicationProprty.getProperty("file.upload-dir");

		String folderName = "TEST.xlsx";

		// checking and creating directory .
		File dir = createAndCheckDirectory(mainDir, folderName);

		// storing text into the file.
		Boolean isMatched = false;
		String directory = null;
		ArrayList<UploadExcelDTO> list = new ArrayList<>();

		// creating folder for storing original image
		dir = new File(dir.getPath());
		dir.mkdirs();
		directory = dir.getAbsolutePath();

		for (MultipartFile file : uploadFile.getExcelFileName()) {

			// get content type of file
			String contentType = file.getContentType();

			logger.info("Found Content Type as [{}]", contentType);

			// get extension of file
			String extension = FilenameUtils.getExtension(file.getOriginalFilename());

			logger.info("Found file extension as [{}]", extension);

			if (extension.equals("xls") || extension.equals("xlsx"))
				isMatched = true;

			Path path = Paths.get(directory + ("." + extension));

			byte[] bytes = file.getBytes();
			Files.write(path, bytes);

			list.add(saveDetails(file, file.getOriginalFilename(), uploadFile));
		}

		if (!isMatched) {
			logger.error("Please chooser Excel file");
			throw new BadRequestException("Please choose excel file");
		}

		list.clear();

		return "Successfully uploaded  " + folderName + ".xlsx";
	}

	private File createAndCheckDirectory(String mainDir, String folderName) {

		File dir1 = new File(mainDir + "//");
		String directory = null;
		// create multiple directories at one time
		File dir = null;

		boolean successful = dir1.mkdirs();

		if (successful) {

			dir = new File(mainDir + "//" + folderName);
			dir.mkdirs();
			// created the directories successfully
			directory = dir.getAbsolutePath() + "\\";
			logger.warn("directories were created successfully [{}]", directory);
		} else {
			// something failed trying to create the directories
			dir = new File(mainDir + "//" + folderName);
			dir.mkdirs();
			directory = dir.getPath() + "\\";
			logger.debug("failed trying to create the directories[{}]", directory);
		}

		return dir;
	}

	@Override
	public UploadExcelDTO saveDetails(MultipartFile file, String folder, UploadExcelFileVO vo) {

		UploadExcelDTO uploadExcelDto = new UploadExcelDTO();
		String name = file.getOriginalFilename();

		for (final String ext : EXTENSIONS) {
			if (name.endsWith("." + ext)) {

				uploadExcelDto.setExtension(ext);
				break;
			}
		}

		return uploadExcelDto;

	}

	/**
	 * Uploading and saving financier info of Vehicle in Collection
	 */
	@Override
	public List<FinancerUploadedDetailsDTO> saveUploadFile(MultipartFile files, JwtUser jwtUser,
			boolean overridePrevRec) {
		List<FinancerUploadedDetailsDTO> dtoList = new ArrayList<FinancerUploadedDetailsDTO>();
		try {

			Optional<UserDTO> userDto = userDao.findByUserId(jwtUser.getUsername());

			logger.info("User Details from JWT user [{}] ", userDto.get());

			String filePath = files.getOriginalFilename();
			boolean flag1 = filePath.toLowerCase().endsWith("xls");
			boolean flag2 = filePath.toLowerCase().endsWith("xlsx");

			if (!(flag1 || flag2)) {
				logger.error("Please upload excel files only ");

				throw new BadRequestException("Please upload Excel files only !! ");

			}
			logger.info("You have choosen [{}] the file to Upload", files.getOriginalFilename());

			// Create Workbook instance for xlsx/xls file input stream
			Workbook workbook = null;
			/*
			 * if (filePath.toLowerCase().endsWith("xlsx")) {
			 * logger.info("You are uploading xlss file"); workbook = new
			 * XSSFWorkbook(files.getInputStream()); } else if
			 * (filePath.toLowerCase().endsWith("xls")) {
			 * logger.info("You are uploading xls file"); workbook = new
			 * HSSFWorkbook(files.getInputStream()); }
			 */

			workbook = new XSSFWorkbook(files.getInputStream());
			// Get the first sheet from the workbook
			workbook.getSheetAt(0).forEach(row -> {
				if (row.getRowNum() != row.getFirstCellNum()) {
					FinancerUploadedDetailsDTO dto = new FinancerUploadedDetailsDTO();
					Cell cell = row.getCell(0);
					if (cell != null) {
						row.forEach(col -> {

							switch (col.getColumnIndex()) {
							case 0:
								// get Rc No from Column 1
								dto.setRcNo(StringUtils.trim(col.getStringCellValue()));
								break;
							case 1:
								// get OwnerName from Column 2
								dto.setOwnersName(StringUtils.trim(col.getStringCellValue()));
								break;
							case 2:
								// get date hpa Date(dd-mm-yyyy) from column3
								Date date = col.getDateCellValue();
								if (date != null) {
									LocalDate lDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
									dto.setHpaDate(lDate);
								}
								break;
							case 3:
								// get FinancierName from Column 3
								dto.setFinancierName(StringUtils.trim(col.getStringCellValue()));
								break;
							case 4:
								// get TypeOfVehicle from Column 4
								dto.setTypeOfVehicle(StringUtils.trim(col.getStringCellValue()));
								break;

							case 5:
								// get TypeOfOwnerShip from Column5
								dto.setTypeOfOwnwerShip(StringUtils.trim(col.getStringCellValue()));
								break;
							default:

							}
						});
						// Checking weather RCNO is already uploaded by the user or Not
						Optional<FinancerUploadedDetailsDTO> dto1 = financierUpldDetailsDAO.findByRcNo(dto.getRcNo());
						logger.info(" Record [{}] Already exist in DB", dto1);

						// if User want to Modify the documents he can proceed to override /modify
						if (dto1.isPresent() && overridePrevRec) {
							FinacierDetailsDTO financeDetails = new FinacierDetailsDTO();
							financeDetails.setActionTakenBy(jwtUser.getUsername());
							financeDetails.setActionTakenTime(LocalDateTime.now());
							financeDetails.setActionTaken("modified");
							financeDetails.setParentFinancierName(userDto.get().getParentId());
							dto1.get().setActionTaken("Updated");
							dto1.get().setFinacierDetails(financeDetails);
							financierUpldDetailsDAO.save(dto1.get());
							logger.debug("Overrided Existing Records");
						}
						// Save the New RC records from financier
						if (!dto1.isPresent()) {
							FinacierDetailsDTO financeDetails = new FinacierDetailsDTO();
							financeDetails.setUserId(jwtUser.getUsername());
							financeDetails.setFinancierName(userDto.get().getFirstName());
							financeDetails.setActionTakenTime(LocalDateTime.now());
							financeDetails.setActionTaken("Created");
							logger.info("New Record  [{}] Saving to DB", dto);
							dto.setFinacierDetails(financeDetails);
							dto.setCreatedDate(LocalDateTime.now());
							dto.setCreatedDateStr(LocalDateTime.now().toString());
							dto.setCreatedBy(jwtUser.getUsername());
							dtoList.add(dto);
						}

					}
				}
			});

		} catch (IOException e) {
			logger.warn("Exception in FinancierServiceImpl Upload File [{{}]", e);
			logger.error("Exception in FinancierServiceImpl Upload File [{{}]", e.getMessage());

		}
		financierUpldDetailsDAO.save(dtoList);
		logger.info("New RC Records uploaded By Financier User [{}]", jwtUser.getUsername());
		return dtoList;

	}

	/**
	 * Logged in Financier Can view the Collections which are Uploaded Order By
	 * Recently Uploaded or Modified By userId ,pageable
	 */
	@Override
	public Map<String, Object> findAllUploadedFinanceDetails(String userId, Pageable pageable) {
		Map<String, Object> result = new HashMap<>();
		Page<FinancerUploadedDetailsDTO> data = null;
		try {
			data = financierUpldDetailsDAO.findByCreatedByOrderByCreatedDateDesc(userId, pageable.previousOrFirst());
		} catch (Exception e) {
			logger.warn("Exception Occurred while Fetching Financier Uploaded Records [{}]", e);
			logger.error("Exception Occurred while Fetching Financier Uploaded Records [{}]", e.getMessage());
		}
		result.put("totalPages", data.getTotalPages());
		result.put("data", data);
		return result;

	}

	/**
	 *********** FinancierCreateRequest
	 * 
	 * @param userVO
	 * @return
	 */

	@Override
	public Optional<FinancierCreateRequestVO> financierCreateRequest(FinancierCreateRequestVO userVO,
			MultipartFile[] uploadfiles) {
		// User must be Aadhaar Authenticated to proceed with Financier Application
		Optional<ApplicantDetailsDTO> optApplicant = applicantDetailsDao.findByUidToken(userVO.getUidToken());
		if (optApplicant.isPresent()) {

			logger.debug("User is Aadhaar Validated");

			FinancierCreateRequestDTO finReqDto = new FinancierCreateRequestDTO();

			if (userVO.getAadharNo() == null || userVO.getAadharNo().trim().length() != 12) {
				logger.warn("invalid Aadhaar No entered [{}]", userVO.getAadharNo());
				logger.error("INVALID UID", userVO.getAadharNo());
				throw new BadRequestException("INVALID UID");
			}

			Optional<FinancierCreateRequestDTO> userFound = finReqDao
					.findByAadharNo(optApplicant.get().getAadharResponse().getUid().toString().trim());
			Optional<FinancierCreateRequestDTO> userFoundPan = finReqDao
					.findByFinancierPanNo(userVO.getFinancierPanNo());
			if (userFound.isPresent() && userFoundPan.isPresent()) {
				logger.warn("user already exits with aadhaar No and PAN Number : ", userVO.getAadharNo());
				logger.error("user already exits with Applications No : " + userFound.get().getFinAppNo());
				throw new BadRequestException(
						"user already exits with Applications No : " + userFound.get().getFinAppNo());
			}
			// set office for financier
			String officeCode = userVO.getMandal().getTransportOfice();
			String officeName = StringUtils.EMPTY;
			if (StringUtils.isBlank(officeCode)) {
				officeCode = userVO.getMandal().getNonTransportOffice();
			}
			Optional<OfficeDTO> opOffice = officeDao.findByOfficeCode(officeCode);
			if (opOffice.isPresent()) {
				officeName = opOffice.get().getOfficeName();
			}
			OfficeVO officeVo = new OfficeVO();
			officeVo.setOfficeCode(officeCode);
			officeVo.setOfficeName(officeName);
			userVO.setOffice(officeVo);
			// convert User VO to FinDTO
			finReqDto = finCreReqMapper.convertUserVOtoFinDTO(userVO);
			// financier firstName is INSTITUTION NAME
			finReqDto.setFirstName(userVO.getInstitutionName());
			// firstname is same as full name in aadhaar
			finReqDto.setFirstname(userVO.getRepresentativeName());
			finReqDto.setMobile(userVO.getMobile());
			finReqDto.setEmail(userVO.getEmail());
			// application status INITIATED
			finReqDto.setApplicationStatus(FinancierCreateReqStatus.INITIATED.getLabel());
			LocalDateTime cDate = LocalDateTime.now();
			finReqDto.setCreatedDate(cDate);
			finReqDto.setModifiedDate(cDate);
			finReqDto.setModifiedDateStr(cDate.toString());
			finReqDto.setlUpdate(cDate);

			// Adding serviceIds for FinancierCreateRequestDTO

			finReqDto.setServiceIds(Stream.of(1001).collect(Collectors.toSet()));

			Map<String, String> officeCodeMap = new TreeMap<>();
			officeCode = officeCode.replace("AP", "FIN");
			officeCodeMap.put("officeCode", officeCode);
			// Sequence Generated for Financier as FIN10200000022018
			finReqDto.setFinAppNo(sequenceGenerator.getSequence(String.valueOf(Sequence.FINANCIERAPPNO.getSequenceId()),
					officeCodeMap));
			// finance Company Details save in DB
			finReqDao.save(finReqDto);
			// modify applicant Type in applicatn_detailsDTO
			optApplicant.get().setApplicantType(StatusRegistration.FINANCE.getDescription());
			optApplicant.get().setIsAadhaarValidated(Boolean.TRUE);
			optApplicant.get().setModifiedBy(userVO.getFirstName());
			optApplicant.get().setModifiedDate(cDate);
			applicantDetailsDao.save(optApplicant.get());
			// Enclosures to be Saved
			FinancierEnclosuresDTO enclosureDTO = new FinancierEnclosuresDTO();
			LocalDateTime currentDate = LocalDateTime.now();
			enclosureDTO.setAadharNo(userVO.getAadharNo());
			enclosureDTO.setCreatedDate(currentDate);
			enclosureDTO.setlUpdate(currentDate);
			enclosureDTO.setModifiedDate(currentDate);
			enclosureDTO.setModifiedDateStr(currentDate.toString());
			// Save financierApplication number in enclosureDTO
			enclosureDTO.setApplicationNo(finReqDto.getFinAppNo());
			finReqDto.setAppNo(finReqDto.getFinAppNo());

			List<EnclosuresDTO> enclosureList = new ArrayList<>();
			Integer serviceId = 1001;

			if (0 == serviceId.intValue()) {
				return Optional.empty();
			}
			enclosureList = encDao.findByServiceID(serviceId);

			enclosureList.sort((p1, p2) -> p1.getEnclosureId().compareTo(p2.getEnclosureId()));

			if (enclosureList.isEmpty()) {
				logger.error("No enclosures found based on given Service Id");
				throw new BadRequestException("No enclosures found based on given Service Id");
			}
			InputVO inputVO = userEnclosureMapper.convertEnclosureDTOtoInputVO(enclosureList);

			userVO.setImageInput(inputVO.getImageInput());

			try {
				saveEnclosures(finReqDto, userVO.getImageInput(), uploadfiles);
				// saveImages(finReqDto.getFinAppNo(), userVO.getImages(), uploadfiles);
			} catch (IOException e) {
				logger.warn("Exception occurred while Enclosures Upload [{ }]", e);
				logger.error("Exception occurred while Enclosures Upload [{ }]", e.getMessage());
			}
			userVO.setId(finReqDto.getFinAppNo());
			// User gets notified by application No
			sendNotifications(1, finReqDto);
			logger.debug("User gets Nofified By SMS/Email");
			return Optional.of(userVO);
		} else
			logger.debug("User is not Aaadhaar Authenticated");
		logger.error("You are not an Aadhaar Authenticated User");
		throw new BadRequestException("You are not an Aadhaar Authenticated User");

	}

	/**
	 * Save Uploaded enclosures
	 * 
	 * @param applicationNo
	 * @param images
	 * @param uploadfiles
	 * @throws IOException
	 */
	public void saveImages(String applicationNo, List<ImageInput> images, MultipartFile[] uploadfiles)
			throws IOException {

		if (CollectionUtils.isNotEmpty(images)) {
			for (MultipartFile file : uploadfiles) {
				long size = file.getSize();
				logger.info("size of uploaded file {} :[{}]", file.getOriginalFilename(), size);
			}

			Optional<FinancierEnclosuresDTO> citizenEnclosuresDTO = financierEnclosuresDAO
					.findByApplicationNo(applicationNo);
			if (!citizenEnclosuresDTO.isPresent()) {
				logger.error("No Enclosures foud for application No: [{}]", applicationNo);
				throw new BadRequestException("No Enclosures foud for application No:" + applicationNo);
			}
			// saveEnclosures(citizenEnclosuresDTO.get(), images, uploadfiles);
		}
	}

	/**
	 * To Save Enclosures uploaded By user with GridFS Client
	 * 
	 * @param enclosureDTO
	 * @param images
	 * @param uploadfiles
	 * @throws IOException
	 */
	private void saveEnclosures(FinancierCreateRequestDTO financierCreateRequestDTO, List<ImageInput> images,
			MultipartFile[] uploadfiles) throws IOException {

		FinancierEnclosuresDTO financierEnclosuresDTO = new FinancierEnclosuresDTO();

		List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = gridFsClient.convertImages(images,
				financierCreateRequestDTO.getAppNo(), uploadfiles, StatusRegistration.INITIATED.getDescription());

		financierEnclosuresDTO.setApplicationNo(financierCreateRequestDTO.getAppNo());
		/* financierEnclosuresDTO.setPrNo(financierCreateRequestDTO.getPrNo()); */
		financierEnclosuresDTO.setAadharNo(financierCreateRequestDTO.getAadharNo());
		financierEnclosuresDTO.setEnclosures(enclosures);
		financierEnclosuresDTO.setServiceIds(Arrays.asList(1001));

		financierEnclosuresDAO.save(financierEnclosuresDTO);

	}

	/**
	 * List Of Enclosures to be uploaded by user to be Approved as Financier
	 */
	@Override
	public Optional<InputVO> getListOfSupportedEnclosers(CitizenImagesInput input) {

		List<EnclosuresDTO> enclosureList;
		Integer serviceId = getEncServiceId(input);

		if (0 == serviceId.intValue()) {
			return Optional.empty();
		}
		enclosureList = encDao.findByServiceID(serviceId);

		enclosureList.sort((p1, p2) -> p1.getEnclosureId().compareTo(p2.getEnclosureId()));

		if (enclosureList.isEmpty()) {
			logger.error("Enclosueres not found ");
			throw new BadRequestException("No enclosures found based on given Service Id");
		}
		InputVO inputVO = userEnclosureMapper.convertEnclosureDTOtoInputVO(enclosureList);
		return Optional.of(inputVO);

	}

	/**
	 * 
	 * @param input
	 * @return
	 */
	private Integer getEncServiceId(CitizenImagesInput input) {
		if (input.getServiceIds().stream().anyMatch(service -> service.equals(ServiceEnum.NEWFINANCIERREGN.getId()))) {
			return ServiceEnum.NEWFINANCIERREGN.getId();
		}
		return 0;
	}

	/**
	 * convert JsonObj To Financier Object
	 */
	@Override
	public Optional<FinancierCreateRequestVO> convertJsonToFinancierObject(String userVO) {
		try {
			return Optional.of(objectMapper.readValue(userVO, FinancierCreateRequestVO.class));
		} catch (IOException ioe) {
			logger.error("Exception occured while converting String to Object", ioe);
		}

		return Optional.empty();
	}

	/**
	 * financierApprovalProcess By CCO or AO
	 */
	@Override
	public Optional<FinancierCreateRequestDTO> financierApprovalProcess(String financierApplicationNo,
			FinancierCreateRequestDTO finReqDto, JwtUser jwtUser, String selectedRole) {
		/*
		 * if (selectedRole.equalsIgnoreCase(jwtUser.getPrimaryRole().getName())) {
		 */
		Optional<FinancierCreateRequestDTO> optFinCreReqDto = null;
		optFinCreReqDto = finReqDao.findByOfficeOfficeCodeAndFinAppNo(jwtUser.getOfficeCode(), financierApplicationNo);

		Optional<FinancierEnclosuresDTO> finEncDtoOpt = financierEnclosuresDAO
				.findByApplicationNo(financierApplicationNo);
		if (optFinCreReqDto.isPresent()) {

			if (finEncDtoOpt.isPresent()) {
				FinancierEnclosuresDTO finEncDto = finEncDtoOpt.get();
				finEncDto.setEnclosures(finReqDto.getFinancierEnclosers().getEnclosures());
				financierEnclosuresDAO.save(finEncDto);
			}

			optFinCreReqDto.get().setApplicationStatus(finReqDto.getApplicationStatus());

			finReqDao.save(optFinCreReqDto.get());

			// if AO approves ===>> Application is APPROVED and role will Be Finance
			// TODO: user has to notified by notification Services like userId and Password
			if (optFinCreReqDto.get().getApplicationStatus()
					.equalsIgnoreCase(FinancierCreateReqStatus.AO_APPROVED.getLabel())) {
				UserDTO financeUserDto = new UserDTO();
				LocalDateTime currentDate = LocalDateTime.now();
				// convert Financier Dto To UserDTO
				financeUserDto = finCreReqMapper.convertFinancierDtoToUserDTO(optFinCreReqDto.get());

				RolesDTO userRole = new RolesDTO();
				userRole.setName(StatusRegistration.FINANCE.getDescription());

				financeUserDto.setPrimaryRole(userRole);
				financeUserDto.setAdditionalRoles(Collections.emptyList());
				financeUserDto.setUserLevel(1);
				financeUserDto.setPasswordResetRequired(Boolean.TRUE);
				String password = RandomStringUtils.randomAlphanumeric(8);
				logger.debug("Password Generated [{}] ", password);
				financeUserDto.setPassword(passwordEncoder.encode(password));
				financeUserDto.setIsAccountNonLocked(true);
				financeUserDto.setParent(true);
				financeUserDto.setStatus(true);
				financeUserDto.setUserStatus(UserStatusEnum.ACTIVE);

				financeUserDto.setUserId(optFinCreReqDto.get().getFinAppNo());
				financeUserDto.setMobile(optFinCreReqDto.get().getMobile());
				financeUserDto.setEmail(optFinCreReqDto.get().getEmail());
				financeUserDto.setCreatedBy(jwtUser.getUsername());
				financeUserDto.setCreatedDate(LocalDateTime.now());
				financeUserDto.setlUpdate(currentDate);
				financeUserDto.setModifiedBy(jwtUser.getUsername());
				financeUserDto.setModifiedDate(currentDate);
			

				// For finance First Name is set as INSTITUTION NAME

				financeUserDto.setFirstName(optFinCreReqDto.get().getInstitutionName());
				// Save Financier Details in Master_Users
				Optional<UserDTO> optDto = userDao.findByUserId(optFinCreReqDto.get().getFinAppNo());

				if (optDto.isPresent()) {
					logger.error("User is already Approved [{}]", optDto.get().getParentId());
					throw new BadRequestException("User is Already Approved By" + optDto.get().getParentId());
				}

				userDao.save(financeUserDto);
				try {
					financeUserDto.setPassword(password);

					// User will get notified after AO Approved with APPLICATION ID & PASSWORD
					sendNotifications(1, financeUserDto);
					logger.debug("User gets Nofified By SMS/Email");
				} catch (Exception e) {
					logger.warn("Error while notification service Call [{}] ", e);
				}
				return optFinCreReqDto;
			}

			sendNotifications(1, optFinCreReqDto.get());
			logger.debug("User gets Nofified By SMS/Email");
			return optFinCreReqDto;
		} else {
			logger.error("No application exixsts with applicationNo [{}]", finReqDto.getFinAppNo());
			throw new BadRequestException("No application exists with app Id" + finReqDto.getFinAppNo());
		}

		/*
		 * } else { throw new
		 * BadRequestException("You are not authorized to use This Servce."); }
		 */
	}

	/**
	 * send Notifications after AO approval Success
	 * 
	 * @param i
	 * @param financeUserDto
	 * @throws IOException
	 */
	private void sendNotifications(int i, UserDTO financeUserDto) throws IOException {

		if (financeUserDto != null) {

			notifications.sendNotifications(MessageTemplate.FIN_REGN_SUCCESS.getId(), financeUserDto);
		}

	}

	/**
	 * send Notifications on Application Fill Success By User(Financier)
	 * 
	 * @param i
	 * @param finReqDto
	 */
	private void sendNotifications(int i, FinancierCreateRequestDTO finReqDto) {

		if (finReqDto != null) {
			// user notified
			notifications.sendNotifications(MessageTemplate.FIN_REGN_STATUS.getId(), finReqDto);
		}
	}

	/**
	 * get FinancierDetailsBy FinAppNo
	 */
	@Override
	public Optional<FinancierCreateRequestDTO> getFinancierDetailsByFinAppNo(FinancierCreateRequestDTO dto) {
		Optional<FinancierCreateRequestDTO> optFinDetails = null;
		if (dto.getFinAppNo() != null) {
			optFinDetails = finReqDao.findByFinAppNo(dto.getFinAppNo().trim());
		} else if (dto.getAadharNo() != null) {
			optFinDetails = finReqDao.findByAadharNo(dto.getAadharNo().trim());
		} else {
			logger.error("Missing application no or aadhaarNo ");
			throw new BadRequestException("Please provide Aadhaar No or Application No");
		}
		if (optFinDetails.isPresent()) {
			optFinDetails.get().setFinancierEnclosers(
					financierEnclosuresDAO.findByApplicationNo(optFinDetails.get().getFinAppNo().trim()).get());
		}
		return optFinDetails;
	}

	/**
	 * update Financier Application by user
	 */
	@Override
	public Optional<FinancierCreateRequestDTO> reUpdateFinancierDetailsByFinAppNo(String appNo,
			MultipartFile[] uploadfiles) {
		Optional<FinancierCreateRequestDTO> optFinDetails = null;
		optFinDetails = finReqDao.findByFinAppNo(appNo.trim());
		// Optional<FinancierEnclosuresDTO> encl = null;
		// encl = finEncDao.findByApplicationNo(appNo.trim());
		// optFinDetails.get().setFinancierEnclosers(encl.get());
		if (optFinDetails.isPresent()) {

			Optional<FinancierEnclosuresDTO> enclosureDTO = financierEnclosuresDAO.findByApplicationNo(appNo.trim());
			if (enclosureDTO.isPresent()) {
				LocalDateTime currentDate = LocalDateTime.now();
				enclosureDTO.get().setAadharNo(optFinDetails.get().getAadharNo());
				/* enclosureDTO.get().setCreatedDate(currentDate); */
				enclosureDTO.get().setlUpdate(currentDate);
				enclosureDTO.get().setModifiedDate(currentDate);
				enclosureDTO.get().setModifiedDateStr(currentDate.toString());
				// Save financierApplication number in enclosureDTO
				enclosureDTO.get().setApplicationNo(appNo);

				optFinDetails.get().setFinancierEnclosers(enclosureDTO.get());
				List<ImageInput> imagesInputsList = new ArrayList<ImageInput>();
				int i = 0;
				List<String> imageNames = new ArrayList<>();
				List<String> typeList = new ArrayList<>();
				optFinDetails.get().getFinancierEnclosers().getEnclosures().forEach(enclosure -> {
					typeList.add(enclosure.getKey());
					enclosure.getValue().forEach(value -> {
						if (value.getImageStaus().equalsIgnoreCase(StatusRegistration.AOREJECTED.getDescription())) {
							imageNames.add(value.getEnclosureName());
						}
					});
				});
				for (MultipartFile uploadfile : uploadfiles) {
					ImageInput inputImage = new ImageInput();
					inputImage.setFileOrder(i);
					inputImage.setPageNo(i);
					inputImage.setEnclosureName(imageNames.get(i));
					inputImage.setType(typeList.get(i));
					// add image to List
					imagesInputsList.add(inputImage);
					i++;
				}
				try {
					uploadEnclosures(optFinDetails.get(), imagesInputsList, uploadfiles);

					/*
					 * List<EnclosuresDTO> enclosureList = new ArrayList<>(); Integer serviceId =
					 * 1001;
					 * 
					 * if (0 == serviceId.intValue()) { return Optional.empty(); } enclosureList =
					 * encDao.findByServiceID(serviceId);
					 * 
					 * enclosureList.sort((p1, p2) ->
					 * p1.getEnclosureId().compareTo(p2.getEnclosureId()));
					 * 
					 * if (enclosureList.isEmpty()) { throw new
					 * BadRequestException("No enclosures found based on given Service Id"); }
					 * InputVO inputVO =
					 * userEnclosureMapper.convertEnclosureDTOtoInputVO(enclosureList);
					 * optFinDetails.get().setImageInput(inputVO.getImageInput());
					 * 
					 * try { saveEnclosures(optFinDetails.get(),
					 * optFinDetails.get().getImageInput(), uploadfiles);
					 */

					// saveImages(finReqDto.getFinAppNo(), userVO.getImages(), uploadfiles);
				} catch (IOException e) {
					logger.warn("Exception occurred while Enclosures Upload [{ }]", e);
					logger.error("Exception occurred while Enclosures Upload [{ }]", e.getMessage());
				}

			}

			return optFinDetails;
		}
		return Optional.empty();
	}

	private Boolean uploadEnclosures(FinancierCreateRequestDTO registrationDetails, List<ImageInput> images,
			MultipartFile[] uploadfiles) throws IOException {

		if (!StatusRegistration.AOREJECTED.getDescription()
				.equalsIgnoreCase(registrationDetails.getApplicationStatus())) {
			logger.error("Application is not rejected status [{}] .", registrationDetails.getApplicationStatus());
			throw new BadRequestException(
					"Application is not rejected status. " + registrationDetails.getApplicationStatus());
		}

		// This code is unable to remove existing image - Musarrat Ansari
		/*
		 * images.forEach(imageInput -> {
		 * 
		 * Optional<KeyValue<String, List<ImageEnclosureDTO>>> pagesOptional =
		 * getImages( registrationDetails.getFinancierEnclosers().getEnclosures(),
		 * imageInput);
		 * 
		 * if (pagesOptional.isPresent()) {
		 * 
		 * if (pagesOptional.get().getValue().stream().anyMatch( status ->
		 * status.getImageStaus().equalsIgnoreCase(StatusRegistration.AOREJECTED.
		 * getDescription())) || pagesOptional.get().getValue().stream().anyMatch(status
		 * -> status.getImageStaus()
		 * .equalsIgnoreCase(StatusRegistration.REUPLOAD.getDescription()))) { //
		 * TODO:Add equals and hashcode for KeyValue
		 * registrationDetails.getFinancierEnclosers().getEnclosures().remove(
		 * pagesOptional.get());
		 * 
		 * gridFsClient.removeImages(pagesOptional.get().getValue()); } } });
		 */

		// Added By Musarrat
		List<KeyValue<String, List<ImageEnclosureDTO>>> list = new ArrayList<>();
		registrationDetails.getFinancierEnclosers().getEnclosures().forEach(enclosure -> {
			enclosure.getValue().forEach(value -> {
				if (!value.getImageStaus().equalsIgnoreCase(StatusRegistration.AOREJECTED.getDescription())) {
					list.add(enclosure);
				}
			});
		});

		registrationDetails.getFinancierEnclosers().setEnclosures(list);
		// Added by Musarrat end

		List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = gridFsClient.convertImages(images,
				registrationDetails.getFinancierEnclosers().getApplicantNo(), uploadfiles,
				StatusRegistration.REUPLOAD.getDescription());

		registrationDetails.getFinancierEnclosers().getEnclosures().addAll(enclosures);

		if (!registrationDetails.getFinancierEnclosers().getEnclosures().stream()
				.anyMatch(valu -> valu.getValue().stream().anyMatch(status -> status.getImageStaus()
						.equalsIgnoreCase(StatusRegistration.AOREJECTED.getDescription())))) {
			/*
			 * if (!checkisSecondOrInvalidReject(registrationDetails)) {
			 * registrationDetails.setApplicationStatus(StatusRegistration.REUPLOAD.
			 * getDescription()); }
			 */
		}
		// logMovingService.moveStagingToLog(registrationDetails.getApplicantNo());
		registrationDetails.setApplicationStatus(StatusRegistration.REUPLOAD.getDescription());
		finReqDao.save(registrationDetails);
		financierEnclosuresDAO.save(registrationDetails.getFinancierEnclosers());
		return false;

	}

	public Optional<KeyValue<String, List<ImageEnclosureDTO>>> getImages(
			List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures, ImageInput imageInput) {
		List<EnclosuresDTO> dtos = encDao.findByServiceID(ServiceEnum.NEWFINANCIERREGN.getId());
		if (dtos.isEmpty()) {
			logger.error("Enclosures  is not found in master. [master_enclosures].");
			throw new BadRequestException("Enclosures  is not found in master_enclosures.");
		}

		logger.info("imageInput [{}]", imageInput.getType());
		for (EnclosuresDTO e : dtos) {
			logger.debug("enclosure proof[{}]", e.getProof());
		}
		Optional<EnclosuresDTO> images = dtos.stream()
				.filter(dto -> dto.getProof().equalsIgnoreCase(imageInput.getType())).findFirst();

		if (!images.isPresent()) {
			return Optional.empty();
		}
		return enclosures.stream().filter(e -> images.get().getProof().equalsIgnoreCase(e.getKey())).findFirst();
	}

	@Override
	public List<EnclosureRejectedVO> getListOfRejectedEnclosures(String applicationNo) {
		List<EnclosureRejectedVO> rejectedEnclosures = new ArrayList<EnclosureRejectedVO>();
		Optional<FinancierCreateRequestDTO> registrationDetails = finReqDao.findByFinAppNo(applicationNo);
		if (!registrationDetails.isPresent()) {
			logger.error("No record found with Application No: [{}].", applicationNo);
			throw new BadRequestException("Application  is not found.");
		}

		Set<Integer> serviceIds = registrationDetails.get().getServiceIds();
		List<EnclosuresDTO> enclosuresList = encDao.findByServiceIDIn(serviceIds);

		if (enclosuresList.isEmpty()) {
			logger.error("Enclosures not found");
			throw new BadRequestException("Enclosures  is not found.");
		}
		if (!registrationDetails.get().getApplicationStatus().contains("REJECTED")) {
			logger.info(" application Status [{}]", registrationDetails.get().getApplicationStatus());
			throw new BadRequestException("Application status is not REJECTED");
		}

		Optional<FinancierEnclosuresDTO> financierEnclosuresOptional = financierEnclosuresDAO
				.findByApplicationNo(applicationNo);
		if (!financierEnclosuresOptional.isPresent()) {
			logger.error("Enclosures not found for application: [{}]", applicationNo);
			throw new BadRequestException("Enclosures not found for  ApplicationNo" + applicationNo);
		}

		for (EnclosuresDTO dto : enclosuresList) {
			financierEnclosuresOptional.get().getEnclosures().stream()
					.forEach(val -> val.getValue().stream().forEach(type -> {
						if (type.getImageType().equalsIgnoreCase(dto.getProof())) {
							logger.info("Image Type found  [{}]", type.getImageType());

						}
					}));
		}
		Set<String> imageType = new HashSet<>();
		if (financierEnclosuresOptional.get().getEnclosures() != null) {
			for (KeyValue<String, List<ImageEnclosureDTO>> keyValue : financierEnclosuresOptional.get()
					.getEnclosures()) {
				List<ImageEnclosureDTO> encValue = keyValue.getValue();
				List<ImageEnclosureDTO> rejectedList = encValue.stream()
						.filter(val -> val.getImageStaus().contains("REJECTED")
						/*
						 * || val.getImageStaus().equalsIgnoreCase(StatusRegistration.REUPLOAD.
						 * getDescription())
						 */).collect(Collectors.toList());

				if (rejectedList.size() > 0 && !imageType.contains(keyValue.getKey())) {
					imageType.add(keyValue.getKey());
					/* uploadStatus = true; */
					rejectedEnclosures
							.add(new EnclosureRejectedVO(keyValue.getValue().stream().findFirst().get().getImageType(),
									enclosureImageMapper.convertNewEntity(rejectedList)));
				}
			}
		} else {
			logger.info("No enclosures found for ApplicationNo[{}]", applicationNo);
			throw new BadRequestException("No Enclosures found for applicationNo " + applicationNo);
		}
		return rejectedEnclosures;
	}

	@Override
	public Boolean rcDetailsExcelReport(HttpServletResponse response, Integer catagory, LocalDate fromDate,
			LocalDate toDate) {

		List<FinancerUploadedDetailsDTO> hsrpOptional1 = financierUpldDetailsDAO.findByCreatedDateBetween(fromDate,
				toDate);
		Collections.sort(hsrpOptional1, new Comparator<FinancerUploadedDetailsDTO>() {
			@Override
			public int compare(FinancerUploadedDetailsDTO p1, FinancerUploadedDetailsDTO p2) {
				return p1.getCreatedDate().compareTo(p2.getCreatedDate());
			}
		});
		if (hsrpOptional1.isEmpty()) {
			return false;
		}
		ExcelService excel = new ExcelServiceImpl();

		List<String> header = new ArrayList<String>();

		excel.setHeaders(header, "hsrpDetails");

		Random rand = new Random();
		int ranNo = rand.nextInt(1000);

		String name = "RcDetailReport" + "" + "_" + ranNo;
		String fileName = name + ".xlsx";
		String sheetName = "VehicleDetails";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		response.setHeader("Pragma", "public");
		response.setHeader("Cache-Control", "no-store");
		response.addHeader("Cache-Control", "max-age=0");

		XSSFWorkbook wb = excel.renderData(prepareHsrpCellProps(header, hsrpOptional1), header, fileName, sheetName);

		try {
			ServletOutputStream outputStream = null;
			outputStream = response.getOutputStream();
			wb.write(outputStream);
			outputStream.flush();
			outputStream.close();

		} catch (IOException e) {
			logger.error("Exception occurred [{}]", e);
		}

		return true;

	}

	private List<List<CellProps>> prepareHsrpCellProps(List<String> header, List<FinancerUploadedDetailsDTO> list) {
		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();
		List<CellProps> result = new ArrayList<CellProps>();
		for (FinancerUploadedDetailsDTO hsrpDetails : list) {
			for (int i = 0; i < header.size(); i++) {
				CellProps cellpro = new CellProps();
				switch (i) {
				case 0:
					cellpro.setFieldValue(hsrpDetails.getFinancierName());
					break;
				case 1:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getCreatedDateStr()));
					break;
				case 2:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getRcNo()));
					break;
				case 3:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getOwnersName()));
					break;
				/*
				 * case 4: cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.
				 * getTransactionNo())); break; case 5:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.
				 * getTransactionDate())); break; case 6:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.
				 * getAuthorizationDate())); break; case 7:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getEngineNo(
				 * ))); break; case 8:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getChassisNo
				 * ())); break; case 9:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getPrNumber(
				 * ))); break; case 10:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getOwnerName
				 * ())); break; case 11:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.
				 * getOwnerAddress())); break; case 12:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.
				 * getOwnerEmailId())); break; case 13:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.
				 * getOwnerPinCode())); break; case 14:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getMobileNo(
				 * ))); break; case 15:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.
				 * getVehicleType())); break; case 16:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getTransType
				 * ())); break; case 17:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.
				 * getVehicleClassType())); break; case 18:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getMfrsName(
				 * ))); break; case 19:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getModelName
				 * ())); break; case 20:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getHsrpFee()
				 * )); break; case 21:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.
				 * getOldNewFlag())); break; case 22:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults("")); break; case 23:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getTimeStamp
				 * ())); break; case 24:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getTrNumber(
				 * ))); break; case 25:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.
				 * getDealerName())); break; case 26:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.
				 * getDealerMail())); break; case 27:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.
				 * getDealerRtoCode())); break;
				 * 
				 * case 28:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getRegDate()
				 * )); break; case 29:
				 * cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getMessage()
				 * )); break;
				 */

				default:
					break;
				}
				result.add(cellpro);
			}
		}
		cell.add(result);
		return cell;

	}

	/**
	 * getChildFinanciersList for logged in parent user
	 * 
	 */
	@Override
	public Map<String, Object> getChildFinanciersList(String loggedInUser, String primaryRole, Pageable pageable) {
		/* List<UserDTO> childs = userDao.findByParentId(loggedInUser); */
		logger.info("getChildFinanciersList");
		Map<String, Object> result = new HashMap<>();
		Page<UserDTO> data = null;
		try {
			data = userDao.findByParentIdAndPrimaryRoleNameOrderByCreatedDateDesc(loggedInUser, primaryRole,
					pageable.previousOrFirst());
			logger.debug("Query execured findByParentIdAndPrimaryRoleNameOrderByCreatedDateDesc");
		} catch (Exception e) {
			logger.warn("Exception Occurred while Fetching Financier Uploaded Records [{}]", e);
			logger.error("Exception Occurred while Fetching Financier Uploaded Records [{}]", e.getMessage());
		}
		result.put("totalPages", data.getTotalPages());
		result.put("data", data);
		return result;
	}

	/**
	 * Parent user can modify child user pwd
	 * 
	 * @param childUserVO
	 */
	@Override
	public Optional<UserDTO> changeChildUserPwd(UserVO childUserVO, String loggedInUser) {
		Optional<UserDTO> childUser = userDao.findByUserId(childUserVO.getUserId());
		if (childUser.isPresent()) {
			if (null != childUser.get().getParentId() && childUser.get().getParentId().equals(loggedInUser)) {
				childUser.get().setPassword(passwordEncoder.encode(childUserVO.getPassword()));
				childUser.get().setPasswordResetRequired(true);
				userDao.save(childUser.get());
			} else {
				logger.error("[{}] is not authorized to change pwd ", loggedInUser);
				throw new BadRequestException("You are not authorized to Change Password");
			}
		} else {
			logger.error("No Child user found with userId", childUserVO.getUserId());
			throw new BadRequestException("No child User found with the userId" + childUserVO.getUserId());

		}
		return childUser;
	}

	@Override
	public Optional<RegistrationDetailsVO> getFinanceDetailsByTrNo(FinancierActionVO financierActionVO,
			UserDTO userDTO,String user) {
		Optional<StagingRegistrationDetailsDTO> stageDetailsOpt = stagingRegistrationDetailsDAO
				.findByTrNoAndApplicationStatus(financierActionVO.getTrNo(),
						StatusRegistration.TRGENERATED.getDescription());
		if (!stageDetailsOpt.isPresent()) {
			logger.error("Registration details not found for trNo :[{}]", financierActionVO.getTrNo());
			throw new BadRequestException("RegistrationDetails not found with trNo" + financierActionVO.getTrNo());
		}
		RegistrationDetailsDTO regDetailsDTO = stageDetailsOpt.get();
		checkFinancierValidation(regDetailsDTO, userDTO, financierActionVO, financierActionVO.getTrNo(),user);
		financierAadharValidation(financierActionVO);
		RegistrationDetailsVO registrationDetailsVO = registrationDetailsMapper.convertEntity(regDetailsDTO);
		return Optional.of(registrationDetailsVO);
	}

	private void checkFinancierValidation(RegistrationDetailsDTO regDetailsDTO, UserDTO userDTO,
			FinancierActionVO financierActionVO, String Number, String user) {

		Optional<UserDTO> masterdata = null;
		List<UserDTO> childusers = null;
		if (regDetailsDTO.getFinanceDetails().getUserId() != null) {
			masterdata = userDao.findByUserId(regDetailsDTO.getFinanceDetails().getUserId());

			if (masterdata.isPresent() && user!= null) {
				childusers = userDao.findByParentId(user);
			}
		}
		if (userDTO.getActionItems() != null && !userDTO.getActionItems().stream()
				.anyMatch(actionItem -> actionItem.getActionName().equals(ActionTypeEnum.HAT.getDesc()))) {
			logger.error("To apply freshRc financier  must have  " + ActionTypeEnum.HAT.getDesc());
			throw new BadRequestException("To apply Fresh rc service financier must have HAT action ");
		}
		if (regDetailsDTO.getFinanceDetails() == null) {
			logger.error("No finance Dtails found for prNo/trNo: [{}]", Number);
			throw new BadRequestException("No finance Details with prNo/trNo " + Number);
		}
		if (!financierActionVO.getChassisNo().equals(regDetailsDTO.getVahanDetails().getChassisNumber())) {
			logger.error("try with Valid chassisNo :[{}]", Number);
			throw new BadRequestException("try with Valid chassisNo :" + Number);
		}

		if (childusers == null &&regDetailsDTO.getFinanceDetails() != null && regDetailsDTO.getFinanceDetails().getUserId() != null &&
				 !regDetailsDTO.getFinanceDetails().getUserId().equals(user) && masterdata.isPresent()) {
			logger.error("login financier not related to respective prNo", Number);
			throw new BadRequestException("login financier not related to respective prNo" + Number);
		}
		if (regDetailsDTO.getFinanceDetails() != null && regDetailsDTO.getFinanceDetails().getUserId() != null
				&& (!regDetailsDTO.getFinanceDetails().getUserId().equals(user) && masterdata.isPresent()
						&& childusers != null && !childusers.stream().anyMatch(
								child -> child.getUserId().equals(regDetailsDTO.getFinanceDetails().getUserId())))) {
			logger.error("login financier not related to respective prNo", Number);
			throw new BadRequestException("login financier not related to respective prNo" + Number);
		}

		if (childusers == null &&regDetailsDTO.getFinanceDetails() != null && regDetailsDTO.getFinanceDetails().getUserId() != null
				&& masterdata.isPresent() && masterdata.get().getUserId() != null
				&& !user.equals(masterdata.get().getUserId())
				&& !regDetailsDTO.getFinanceDetails().getUserId().equals(user)) {
			logger.error("login financier not presented in master data", user);
			throw new BadRequestException("login financier not presented in master data" + user);
		}

		if (!financierActionVO.getAadhaarRequest().getUid_num().equals(userDTO.getAadharNo())) {
			logger.error("Unauthorized aadhar details :[{}]", financierActionVO.getAadhaarRequest().getUid_num());
			throw new BadRequestException(
					"Unauthorised Aadhaar Details :" + financierActionVO.getAadhaarRequest().getUid_num());
		}

	}

	private int freshRCCount(String userId) {
		long financierRecordsCount = regServiceDAO
				.countByFreshRcdetailsFinancerUserIdAndApplicationStatusInAndServiceIds(userId,
						Arrays.asList(StatusRegistration.PAYMENTDONE, StatusRegistration.AOAPPROVED,
								StatusRegistration.MVIAPPROVED, StatusRegistration.AOREJECTED,
								StatusRegistration.MVIREJECTED,StatusRegistration.RTOAPPROVED,StatusRegistration.APPROVED),
						ServiceEnum.RCFORFINANCE.getId());
		return (int) financierRecordsCount;
	}

	// As per murthy gaaru inputs commented TR flow for freshRC once murthy gaaru
	// gives approval uncomment code
	/*
	 * @Override public RegServiceVO doFreshRcForTrNo(String regServiceVO,
	 * MultipartFile[] multipartFile, Boolean isTrNo,String user) throws
	 * IOException, RcValidationException { return
	 * registrationService.savingRegistrationServicesForFreshRCTrNo(regServiceVO,
	 * multipartFile, isTrNo,user); }
	 */

	@Override
	public List<Pair<Boolean, FreshApplicationSearchVO>> reuploadImagesForFreshRc(String user) {
		boolean freshRcFlagStatus = false;
		FreshApplicationSearchVO fRCApplicationSearch = new FreshApplicationSearchVO();
		List<Pair<Boolean, FreshApplicationSearchVO>> list = new ArrayList<Pair<Boolean, FreshApplicationSearchVO>>();
		List<RegServiceDTO> reglist = regServiceDAO
				.findByFreshRcdetailsFinancerUserIdAndApplicationStatusInAndServiceIdsNative(user,
						Arrays.asList(StatusRegistration.REJECTED), ServiceEnum.RCFORFINANCE.getId());
		if (reglist.isEmpty()) {
			logger.error("No application is rejected For FreshRC" + user);
		}

		for (RegServiceDTO regSerDto : reglist) {
			if (regSerDto.isMviDone()) {
				freshRcFlagStatus = false;
				fRCApplicationSearch = freshRCMapper.convertForTerminatDocment(regSerDto);
				list.add(Pair.of(freshRcFlagStatus, fRCApplicationSearch));
			} else {
				freshRcFlagStatus = true;
				fRCApplicationSearch = freshRCMapper.convertForReuploadDocments(regSerDto);
				list.add(Pair.of(freshRcFlagStatus, fRCApplicationSearch));
			}
		}
		return list;
	}

	@Override
	public List<FreshApplicationSearchVO> dispalyRecordOfFreshRc(String user) {
		List<FreshApplicationSearchVO> fRCApplicationSearchlist = new ArrayList<FreshApplicationSearchVO>();
		FreshApplicationSearchVO fRCApplicationSearch = new FreshApplicationSearchVO();
		List<RegServiceDTO> regServiceList = regServiceDAO
				.findByFreshRcdetailsFinancerUserIdAndApplicationStatusInAndServiceIdsNative(user,
						Arrays.asList(StatusRegistration.PAYMENTDONE, StatusRegistration.AOAPPROVED,
								StatusRegistration.MVIAPPROVED, StatusRegistration.AOREJECTED,
								StatusRegistration.MVIREJECTED,StatusRegistration.RTOAPPROVED,StatusRegistration.APPROVED),
						ServiceEnum.RCFORFINANCE.getId());
		if (CollectionUtils.isNotEmpty(regServiceList)) {
			for (RegServiceDTO regSerDto : regServiceList) {
				if (regSerDto.getFlowId() != null && regSerDto.getFlowId().equals(Flow.RCFORFINANCEMVIACTION)) {
					fRCApplicationSearch = freshRCMapper.convertenablefrom37record(regSerDto);
					fRCApplicationSearchlist.add(fRCApplicationSearch);
				} else {
					fRCApplicationSearch = freshRCMapper.convertdisablefrom37record(regSerDto);
					fRCApplicationSearchlist.add(fRCApplicationSearch);
				}
			}
		}
		return fRCApplicationSearchlist;
	}

	private Integer freshRcRejectedCount(String userId) {
		long financierRecordsCount = regServiceDAO
				.countByFreshRcdetailsFinancerUserIdAndApplicationStatusInAndServiceIds(userId,
						Arrays.asList(StatusRegistration.REJECTED), ServiceEnum.RCFORFINANCE.getId());
		return (int) financierRecordsCount;
	}

	@Override
	public List<Map<String, Object>> checkValideFinancier(String userId) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Optional<UserDTO> userDetails = null;
		if (StringUtils.isNotEmpty(userId)) {
			userDetails = userDao.findByUserId(userId);
		} else {
			throw new BadRequestException("User id not found");
		}
		if (userDetails.isPresent()) {
			list = getUserData(userDetails.get());
		} else {
			throw new BadRequestException("Financier details not found");
		}
		return list;
	}

	public List<Map<String, Object>> getUserData(UserDTO userDTO) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<>();

		if (userDTO.getState() == null || userDTO.getDistrict() == null || userDTO.getMandal() == null
				|| userDTO.getVillage() == null || userDTO.getPostOffice() == null) {
			map.put(MessageKeys.FRESHRC_STATUS, MessageKeys.FRESHRC_STATUS_FALSE);
			map.put(MessageKeys.FRESHRC_STATUS_DATA, freshRCMapper.convertFinancierAdressToVo(userDTO));
			list.add(map);
		} else if ((userDTO.getState() != null && userDTO.getState().getStateName().isEmpty())
				|| (userDTO.getDistrict() != null && userDTO.getDistrict().getDistrictName().isEmpty())
				|| (userDTO.getMandal() != null && userDTO.getMandal().getMandalName().isEmpty())
				|| (userDTO.getVillage() != null && userDTO.getVillage().getVillageName().isEmpty())
				|| (userDTO.getPostOffice() != null && userDTO.getPostOffice().getPostOfficeName().isEmpty())) {
			map.put(MessageKeys.FRESHRC_STATUS, MessageKeys.FRESHRC_STATUS_FALSE);
			map.put(MessageKeys.FRESHRC_STATUS_DATA, freshRCMapper.convertFinancierAdressToVo(userDTO));
			list.add(map);
		} else {
			map.put(MessageKeys.FRESHRC_STATUS, MessageKeys.FRESHRC_STATUS_TRUE);
			map.put(MessageKeys.FRESHRC_STATUS_DATA, MessageKeys.FRESHRC_STATUS_NO_DATA);
			list.add(map);
		}
		return list;
	}

	@Override
	public void saveAdressOfFinancier(FreshRcVO vo, String userId) {
		if (StringUtils.isEmpty(userId)) {
			throw new BadRequestException("userId not presented");
		}
		Optional<UserDTO> userDetails = userDao.findByUserId(userId);
		if (userDetails.isPresent()) {
			UserDTO user = freshRCMapper.convertFinancierAdressToDto(userDetails.get(), vo.getFinancerDetails());
			userDao.save(user);
		} else {
			throw new BadRequestException("Financier details not found");
		}

	}

	@Override
	public FinanceSeedDetailsVO getDetailsByFinanceuserId(FinanceSeedDetailsVO financeSeedDetailsVO, JwtUser jwtUser) {
		Optional<RegistrationDetailsDTO> regDetails = null;
		FinanceSeedDetailsVO financeSeedVO = null;
		if (StringUtils.isNoneBlank(financeSeedDetailsVO.getPrNo())
				&& StringUtils.isNoneBlank(financeSeedDetailsVO.getChassisNo())) {
			regDetails = registrationdetailDAO.findByPrNoOrderByCreatedDateDesc(financeSeedDetailsVO.getPrNo());
			if (regDetails.get().getFinanceDetails() == null) {
				throw new BadRequestException("No finance Details Available");
			}
		}
		if (!financeSeedDetailsVO.getChassisNo().equals(regDetails.get().getVahanDetails().getChassisNumber())) {
			throw new BadRequestException("No record found");
		}
		if ((regDetails.get().getFinanceDetails() != null && regDetails.get().getFinanceDetails().getUserId() == null)
				|| regDetails.get().getFinanceDetails() != null
						&& regDetails.get().getFinanceDetails().getUserId() != null
						&& registrationService.isOnlineFinance(regDetails.get().getFinanceDetails().getUserId())) {
			throw new BadRequestException("only offline financiers");
		}
		financeSeedVO = registrationDetailsMapper.convertLimitedFieldsForRegistration(regDetails.get());
		return financeSeedVO;
	}

	@Override
	public void saveVehicleDetails(FinanceSeedDetailsVO financeVO, JwtUser jwtUser) {
		FinanceSeedDetailsDTO financeDTO = new FinanceSeedDetailsDTO();
		Optional<UserDTO> user = userDao.findByUserId(jwtUser.getUsername());
		//Optional<RegistrationDetailsDTO> regDTO=regDetailsDAO.findByPrNo(financeVO.getPrNo());
		
		if (user.isPresent()) {
			financeDTO.setOnlineFinanceDetails(userMapper.getLimitedFieldsForFinaciers(user.get()));
		}
		if (StringUtils.isNotBlank(financeVO.getUserName())) {
			financeDTO.setUserName(financeVO.getUserName());
		}
		if (financeVO.getApplicantAddressVO()!=null) {
			financeDTO.setPresentAddress(applicantAddressMapper.convertVO(financeVO.getApplicantAddressVO()));
		}
		if (StringUtils.isNotBlank(financeVO.getFatherName())) {
			financeDTO.setFatherName(financeVO.getFatherName());
		}
		/*
		 * if (regDTO.isPresent()&&regDTO.get().getFinanceDetails()!=null) {
		 * financeDTO.setOfflineFinanceDetails(regDTO.get().getFinanceDetails()); }
		 */
		if (financeVO.getOfflineFinanceDetails()!=null) {
			financeDTO.setOfflineFinanceDetails(financeDetailsMapper.convertVO(financeVO.getOfflineFinanceDetails()));
		}
		financeDTO.setClassOfVehicle(financeVO.getClassOfVehicle());
		financeDTO.setClassOfVehicleDesc(financeVO.getClassOfVehicleDesc());
		financeDTO.setVehicleType(financeVO.getVehicleType());
		financeDTO.setMakersModel(financeVO.getMakersModel());
		financeDTO.setChassisNo(financeVO.getChassisNo());
		financeDTO.setPrNo(financeVO.getPrNo());
		financeDTO.setStatus(StatusRegistration.SUBMITTED.getDescription());
		financeDTO.setCreatedDate(LocalDateTime.now());
		financeDTO.setCreatedBy(jwtUser.getUsername());
		financeDTO.setOfficeCode(financeVO.getOfficeCode());
		financeSeedDetailsDAO.save(financeDTO);
		
	}

		@Override
		public List<FinanceSeedDetailsVO> getPendingListView(JwtUser jwtUser, String selectedRole) {
			List<FinanceSeedDetailsDTO> financeDetails = new ArrayList<>();
			if (!selectedRole.equalsIgnoreCase(RoleEnum.AO.getName())) {
				throw new BadRequestException("Inavalid Action");
			}
			financeDetails = financeSeedDetailsDAO.findByOfficeCodeAndStatusIn(jwtUser.getOfficeCode(),
					Arrays.asList(StatusRegistration.SUBMITTED.getDescription()));
			if (selectedRole.equalsIgnoreCase(RoleEnum.RTO.getName())) {
				financeDetails = financeSeedDetailsDAO.findByOfficeCodeAndStatusIn(jwtUser.getOfficeCode(), Arrays.asList(
						Status.financeSeedStatus.AOAPPROVED.getStatus(), Status.financeSeedStatus.AOREJECTED.getStatus()));
			}
			return financeSeedDetailsMapper.convertEntity(financeDetails);
		}

		@Override
		public void saveFinancier(FinanceSeedDetailsVO vo, String selectedRole, JwtUser jwtUser) {
			if (!StringUtils.isBlank(vo.getId())) {
				FinanceSeedDetailsDTO dto = financeSeedDetailsDAO.findOne(vo.getId());
			}
			FinanceSeedDetailsDTO financeDTO = financeSeedDetailsMapper.convertVO(vo);
			financierSave(financeDTO,vo.getId(),vo.getStatus(),vo.getComments(),selectedRole,jwtUser);
			
		}

		private void financierSave(FinanceSeedDetailsDTO financeDTO, String id, String status, String comments,
				String selectedRole, JwtUser jwtUser) {
			if (selectedRole.equals(RoleEnum.AO.getName()) && financeDTO.getStatus() != null
					&& (financeDTO.getStatus().equals(Status.financeSeedStatus.AOAPPROVED.getStatus())
							|| financeDTO.getStatus().equals(Status.financeSeedStatus.AOREJECTED.getStatus()))) {
				throw new BadRequestException("Another AO already performed action on this record");
			}
			if (selectedRole.equalsIgnoreCase(RoleEnum.AO.getName())&&status.equalsIgnoreCase(StatusRegistration.APPROVED.getDescription())) {
				financeDTO.setStatus(StatusRegistration.AOAPPROVED.getDescription());
			}
			financeDTO.setStatus(StatusRegistration.AOREJECTED.getDescription());
			if (selectedRole.equalsIgnoreCase(RoleEnum.RTO.getName())&&status.equalsIgnoreCase(StatusRegistration.APPROVED.getDescription())) {
				financeDTO.setStatus(StatusRegistration.APPROVED.getDescription());
				financeDTO.setUserId(jwtUser.getUsername());
			}
			financeDTO.setStatus(StatusRegistration.REJECTED.getDescription());
			financeDTO.setStatus(status);
			financeDTO.setComments(comments);
			updateActionDetails(financeDTO, status, jwtUser, comments, selectedRole);
			financeSeedDetailsDAO.save(financeDTO);
			
		}

		private void updateActionDetails(FinanceSeedDetailsDTO financeDTO, String status, JwtUser jwtUser,
				String comments, String selectedRole) {
			ActionDetailsDTO actionDetailsDTO = new ActionDetailsDTO();
			actionDetailsDTO.setAction(status);
			actionDetailsDTO.setActionBy(jwtUser.getUsername());
			actionDetailsDTO.setReason(comments);
			actionDetailsDTO.setActionDatetime(LocalDateTime.now());
			actionDetailsDTO.setActionByRole(Arrays.asList(selectedRole));

			if (null != financeDTO) {
				if (financeDTO.getActionDetails() == null) {
					financeDTO.setActionDetails(new ArrayList<>());
				}
				financeDTO.getActionDetails().add(actionDetailsDTO);
			}
			
		}
	
}
