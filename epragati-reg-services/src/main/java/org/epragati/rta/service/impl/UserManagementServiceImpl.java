package org.epragati.rta.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.epragati.aadhaar.AadharDetailsResponseVO;
import org.epragati.common.vo.UserStatusEnum;
import org.epragati.constants.MessageKeys;
import org.epragati.dto.enclosure.ImageEnclosureDTO;
import org.epragati.exception.BadRequestException;
import org.epragati.financier.vo.FinancierUserUpdateVO;
import org.epragati.images.vo.ImageInput;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dao.ActionDAO;
import org.epragati.master.dao.ApplicantDetailsDAO;
import org.epragati.master.dao.DistrictDAO;
import org.epragati.master.dao.FinancierUserLogDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.RolesDAO;
import org.epragati.master.dao.UserCorrectionsDAO;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dao.UserLogDAO;
import org.epragati.master.dao.UserManagementLogsDao;
import org.epragati.master.dto.ActionDTO;
import org.epragati.master.dto.ApplicantDetailsDTO;
import org.epragati.master.dto.DistrictDTO;
import org.epragati.master.dto.FinancierUserLogDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.RolesDTO;
import org.epragati.master.dto.UserCorrectionsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.dto.UserManagementLogsDto;
import org.epragati.master.mappers.ActionMapper;
import org.epragati.master.mappers.UserMapper;
import org.epragati.master.service.FinancierService;
import org.epragati.master.vo.ActionsVO;
import org.epragati.master.vo.RolesVO;
import org.epragati.master.vo.UserVO;
import org.epragati.restGateway.RestGateWayService;
import org.epragati.rta.service.impl.service.UserManagementService;
import org.epragati.sequence.SequenceGenerator;
import org.epragati.service.files.GridFsClient;
import org.epragati.service.notification.MessageTemplate;
import org.epragati.service.notification.NotificationUtil;
import org.epragati.usermanagement.UserManagementActions;
import org.epragati.util.AppMessages;
import org.epragati.util.IPWebUtils;
import org.epragati.util.RoleEnum;
import org.epragati.util.StatusRegistration;
import org.epragati.util.document.KeyValue;
import org.epragati.util.document.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 
 * @author saroj.sahoo
 *
 */
@Service
public class UserManagementServiceImpl implements UserManagementService {

	private static final Logger logger = LoggerFactory.getLogger(UserManagementServiceImpl.class);

	@Autowired
	private UserDAO userDao;

	@Autowired
	private UserLogDAO userLogDAO;

	@Autowired
	private RolesDAO rolesDAO;

	@Autowired
	private OfficeDAO officeDAO;

	@Autowired
	private DistrictDAO districtDAO;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	ActionMapper actionMapper;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private FinancierUserLogDAO financierUserLogDAO;

	@Autowired
	private AppMessages appMessages;

	@Autowired
	private SequenceGenerator sequenceGenerator;

	@Value("${financier.password:}")
	private String financierPassword;

	@Autowired
	private FinancierService financierMasterService;

	@Autowired
	private RestGateWayService restGateWayService;

	@Autowired
	private ApplicantDetailsDAO applicantDetailsDAO;

	@Value("${newofficer.password:}")
	private String newofficerPassword;

	@Autowired
	private NotificationUtil notifications;

	@Autowired
	private RTAServiceImpl rtaServiceImpl;

	@Autowired
	private IPWebUtils webUtils;

	@Autowired
	private UserManagementLogsDao userManagementLogsDao;
	
	@Autowired
	private GridFsClient gridFsClient;
	
	@Autowired
	private UserCorrectionsDAO userCorrectionsDAO;
	
	@Autowired
	private ActionDAO actionDAO;
		
	@Override
	public void saveOfficerDetails(UserVO userVO, JwtUser jwtUser,List<ImageInput> images,
			MultipartFile[] uploadfiles, HttpServletRequest request) throws IOException {
		UserDTO userDTO = null;
		UserDTO oldUserDTO = null;
		if (userVO.getAadhaarDetails() == null) {
			logger.error("Aadhaar Authentication is Mandatory");
			throw new BadRequestException("Aadhaar Authentication is Mandatory");
		}
		Optional<UserDTO> loginUserOpt = userDao.findByUserId(jwtUser.getUsername());
		if (!loginUserOpt.isPresent()) {
			logger.error("Admin Details Not Found");
			throw new BadRequestException("Admin Details Not Found");
		}
		if (loginUserOpt.get().getAadharNo() == null) {
			logger.error("Admin Aadhar Details Not Available");
			throw new BadRequestException("Admin Aadhar Details Not Available");
		}
		Optional<AadharDetailsResponseVO> aadhaarResponseVO = rtaServiceImpl
				.getAadhaarResponse(userVO.getAadhaarDetails());
		if (!loginUserOpt.get().getAadharNo().equals(String.valueOf(aadhaarResponseVO.get().getUid()))) {
			logger.error("Unauthorized User/Aadhaar Mismatch");
			throw new BadRequestException("Unauthorized User/Aadhaar Mismatch");
		}
		Optional<UserDTO> existingUserOpt = userDao.findByUserId(userVO.getUserId());
		if (userVO.isForUpdate()) {
			if (existingUserOpt.isPresent()) {
				String oldPrimaryRole = existingUserOpt.get().getPrimaryRole().getName();

				/*
				 * List<RoleEnum> rtaOfficialRolesList = Arrays.asList(RoleEnum.DTC,
				 * RoleEnum.CCO, RoleEnum.AO, RoleEnum.MVI, RoleEnum.RTO, RoleEnum.DTCIT,
				 * RoleEnum.ADMIN);
				 */
				List<RolesDTO> rtaRolesList = getAllRoles();

				boolean isRtaOfficial = rtaRolesList.stream()
						.anyMatch(role -> role.getName().equalsIgnoreCase(oldPrimaryRole));

				if (!userVO.getPrimaryRole().getName().equals(oldPrimaryRole) && !isRtaOfficial) {
					logger.error("You can not modify primary Role of [{}] ", oldPrimaryRole);
					throw new BadRequestException("You can not modify primary Role of " + oldPrimaryRole);
				}
				if (!CollectionUtils.isEmpty(userVO.getAdditionalRoles())
						| !CollectionUtils.isEmpty(existingUserOpt.get().getAdditionalRoles())) {
					List<RolesDTO> existedAdditionalRoles = existingUserOpt.get().getAdditionalRoles();
					if(!oldPrimaryRole.equalsIgnoreCase("FINANCE")) {
						if (Collections.disjoint(existedAdditionalRoles, rtaRolesList) & !isRtaOfficial) {
							logger.error("You can not modify Additional Roles");
							throw new BadRequestException("You can not modify Additional Roles");
						}
					}
				}
				List<RolesDTO> additionalRolesList = existingUserOpt.get().getAdditionalRoles();
				List<ActionDTO> actionItemsList = existingUserOpt.get().getActionItems();
				oldUserDTO = new UserDTO();
				BeanUtils.copyProperties(existingUserOpt.get(), oldUserDTO);
				userDTO = userMapper.convertVO(existingUserOpt.get(), userVO);
				setRoles(userVO, userDTO);
				Set<RolesDTO> rolesSet = new LinkedHashSet<RolesDTO>();
				additionalRolesList.addAll(userDTO.getAdditionalRoles());
				rolesSet.addAll(additionalRolesList);
				additionalRolesList.clear();
				additionalRolesList.addAll(rolesSet);
				
				Set<ActionDTO> actionItemsSet = new LinkedHashSet<ActionDTO>();
				actionItemsList.addAll(userDTO.getActionItems());
				actionItemsSet.addAll(actionItemsList);
				actionItemsList.clear();
				actionItemsList.addAll(actionItemsSet);
				
				rolesSet.clear();
				userDTO.setAdditionalRoles(additionalRolesList);
				userDTO.setUpdatedBy(jwtUser.getUsername());
				userDTO.setlUpdate(LocalDateTime.now());
			}
		} else {
			if (existingUserOpt.isPresent()) {
				logger.error("Username already Exists!!!");
				throw new BadRequestException("Username already Exists!!!");
			}
			userDTO = userMapper.convertVO(userVO);
			setRoles(userVO, userDTO);
			userDTO.setStatus(true);
			userDTO.setEmpCode(generateEmpCode(userDTO.getOffice().getOfficeCode()));
			userDTO.setUserId(userDTO.getUserId().toUpperCase());
			userDTO.setPassword(newofficerPassword);
			userDTO.setUserLevel(userDTO.getPrimaryRole().getRoleId());
			userDTO.setPasswordResetRequired(true);
			userDTO.setIsAccountNonLocked(true);
			userDTO.setCreatedBy(jwtUser.getUsername());
			userDTO.setCreatedDate(LocalDateTime.now());
			//userDTO.setUserStatus(UserStatusEnum.ACTIVE);
		}
		
		Optional<UserCorrectionsDTO> userCorrectionsData = userCorrectionsDAO.findByUserId(userDTO.getUserId());
		if(!userCorrectionsData.isPresent()) {
			Optional<UserDTO> unmodifiedUserDate = userDao.findByUserId(userDTO.getUserId());
			UserCorrectionsDTO userCorrectionsDTO = userMapper.convertUserCorrectionsDTO(unmodifiedUserDate.get());
			userCorrectionsDAO.save(userCorrectionsDTO);
		}
		userDao.save(userDTO);		
		saveUserCorrections(userDTO, images, uploadfiles);
		
		UserManagementLogsDto umDto = new UserManagementLogsDto();
		umDto.setOldUserDto(oldUserDTO);
		umDto.setUpdatedUserDto(userDTO);
		umDto.setCreatedBy(loginUserOpt.get().getUserId());
		umDto.setCreatedByAadhar(loginUserOpt.get().getAadharNo());
		umDto.setlUpdate(LocalDateTime.now());
		umDto.setIpAddress(userVO.getAadhaarDetails() == null ? webUtils.getClientIp(request)
				: userVO.getAadhaarDetails().getIp());
		umDto.setCreatedDate(LocalDateTime.now());

		if (!userVO.isForUpdate()) {
			umDto.setActionType(UserManagementActions.NEWUSERREGISTRATION);
			userManagementLogsDao.save(umDto);
			notifications.sendNotifications(MessageTemplate.OFFICER_REG.getId(), userDTO);
		} else {
			userLogDAO.save(oldUserDTO);
			umDto.setActionType(UserManagementActions.USERUPDATE);
			userManagementLogsDao.save(umDto);
		}
	}

	private void setRoles(UserVO userVO, UserDTO userDTO) {
		userDTO.getPrimaryRole().setId(String.valueOf(userVO.getPrimaryRole().getRoleId()));
		userDTO.getPrimaryRole().setMenuCodes(null);
		List<RolesDTO> additionalRolesList = new ArrayList<>();
		if (null != userVO.getAdditionalRoles()) {
			if (!userVO.getAdditionalRoles().isEmpty()) {
				for (RolesVO rolesVO : userVO.getAdditionalRoles()) {
					RolesDTO additionalRole = new RolesDTO();
					additionalRole.setId(String.valueOf(rolesVO.getRoleId()));
					additionalRole.setName(rolesVO.getName());
					additionalRole.setRoleId(rolesVO.getRoleId());
					additionalRolesList.add(additionalRole);
				}
			}
		}
		userDTO.setAdditionalRoles(additionalRolesList);
	}

	@Override
	public UserVO getOfficerDetails(String userId, JwtUser jwtUser, String selectedRole) {

		Optional<UserDTO> userDTOOpt = userDao.findByUserId(userId.trim());
		if (!userDTOOpt.isPresent()) {
			logger.debug("User details not found");
			logger.error("Users not Found", userId);
			throw new BadRequestException("Users not Found");
		} else {
			if (userId.equalsIgnoreCase(jwtUser.getUsername())) {
				logger.error("Not allowed to update own details");
				throw new BadRequestException("Not allowed to update own details");
			}
			List<OfficeDTO> officeList = fetchOfficeCode(jwtUser.getOfficeCode(), selectedRole);
			if (officeList.stream()
					.anyMatch(office -> office.getOfficeCode().equals(userDTOOpt.get().getOffice().getOfficeCode()))) {
				return userMapper.convertEntity(userDTOOpt.get());
			} else {
				logger.debug("User not belongs to this Reporting Office");
				logger.error("User not belongs to this Reporting Office", userId);
				throw new BadRequestException("User not belongs to this Reporting Office");
			}
		}
	}

	private StringBuilder generateUserId(String userId, String officeCode, String role) {
		Optional<UserDTO> userDTOOpt = userDao.findByUserId(userId);
		if (!userDTOOpt.isPresent()) {
			logger.debug("Admin Details not Found");
			logger.error("Admin Details not Found", userId);
			throw new BadRequestException("Admin Details not Found");
		}
		Optional<OfficeDTO> officeDTOOpt = officeDAO.findByOfficeCode(userDTOOpt.get().getOffice().getOfficeCode());
		List<DistrictDTO> districtDTOOpt = districtDAO.findByDistrictId(officeDTOOpt.get().getDistrict());
		Optional<RolesDTO> rolesDTO = rolesDAO.findByName(role);
		DistrictDTO district = districtDTOOpt.stream().findFirst().get();
		StringBuilder userIdSeries = new StringBuilder();
		userIdSeries.append(district.getStateId());
		userIdSeries.append(appendZero(Long.valueOf(district.getZonecode()), 2));
		userIdSeries.append(rolesDTO.get().getUserNo());
		Map<String, String> officeCodeMap = new TreeMap<>();
		officeCodeMap.put("officeCode", officeCode);
		officeCodeMap.put("year", String.valueOf(LocalDate.now().getYear()));
		String serialNo = sequenceGenerator.getSequence(String.valueOf(Sequence.USERID.getSequenceId()), officeCodeMap);
		userIdSeries.append(serialNo);
		return userIdSeries;
	}

	private String appendZero(Long number, int length) {
		return String.format("%0" + (length) + "d", number);
	}

	@Override
	public Optional<UserVO> getUserDetails(UserVO userVO, JwtUser jwtUser) {
		Optional<UserDTO> userDTOOpt = null;
		UserVO useVO = new UserVO();
		// convertEntity
		if (jwtUser == null) {

			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.NO_AUTHORIZATION));
		}
		if (!(jwtUser.getPrimaryRole().getName().equals(RoleEnum.FINANCE.getName()))) {
			throw new BadRequestException("user must be a finacier ");
		}
		if (userVO.getMobile() != null) {
			Integer mobileNoSize = userVO.getMobile().trim().length();

			if (mobileNoSize > 12 || mobileNoSize < 10) {
				throw new BadRequestException("Mobile no is too long or too short");
			}
			userDTOOpt = userDao.findByMobileAndParentId(userVO.getMobile().trim(), jwtUser.getUsername());
			if (userDTOOpt == null) {
				throw new BadRequestException("there is no user have this mobile no :" + mobileNoSize);
			}
			useVO = userMapper.getRequiredFieldsForFinacier(userDTOOpt.get());
			return Optional.of(useVO);
		}
		if (userVO.getUserId() != null) {
			userDTOOpt = userDao.findByUserIdAndParentId(userVO.getUserId().trim(), jwtUser.getUsername());
			if (userDTOOpt == null) {
				throw new BadRequestException("user Doesn't exist :" + userVO.getUserId().trim());
			}
			useVO = userMapper.getRequiredFieldsForFinacier(userDTOOpt.get());
			return Optional.of(useVO);
		}
		return null;
	}

	@Override
	public UserDTO findByUserId(String userId) {
		Optional<UserDTO> userOPt = userDao.findByUserId(userId);
		if (!userOPt.isPresent()) {

			throw new BadRequestException("User deatil not found for UserId: " + userId);
		}
		return userOPt.get();
	}

	@Override
	public UserVO saveChildUser(FinancierUserUpdateVO userVO, String userId) {

		Optional<UserDTO> dto = userDao.findByUserId(userId);
		RolesDTO role = dto.get().getPrimaryRole();
		UserDTO parentUserDTO = userDao.findByUserId(userId).get();

		// Logged in user must be parent User
		if (RoleEnum.UserLevel.USER1.getLevel() != parentUserDTO.getUserLevel() && !(parentUserDTO.isParent())) {
			logger.info("User not authorized to do any operations", parentUserDTO.getUserLevel());
			logger.error("You have no provision to acces this service");
			throw new BadRequestException("You have no provision to create a child finacier");
		}
		userVO.setUserLevel(RoleEnum.UserLevel.USER2.getLevel());
		Optional<ApplicantDetailsDTO> applicant = applicantDetailsDAO.findByUidToken(userVO.getUidToken());
		if (!applicant.isPresent()) {
			logger.debug("User does not exists");
			logger.error("User might not be authenticated through AADHAAR Services");
			// User might not be authenticated through AADHAAR Services
			throw new BadRequestException("You have no provisions to continue any more");
		}
		// User must be authenticated through AADHAAR Services on the same day of
		// Registration
		if (applicant.get().getlUpdate().getDayOfMonth() != LocalDateTime.now().getDayOfMonth()) {
			logger.debug("User is not authenticated through aadhaar services ");
			logger.error("You have no provisions to continue any more");
			throw new BadRequestException("You have no provisions to continue any more");
		}

		// if user Already Exists throw User already Exists Exception::
		// Optional<UserDTO> optionalUser =
		// userDao.findByUidToken(userVO.getUidToken());
		Optional<UserDTO> optionalUser = userDao.findByUidTokenAndPrimaryRoleName(userVO.getUidToken(), role.getName());
		if (optionalUser.isPresent()) {
			logger.error("User is already Registered with userId [{}]", optionalUser.get().getUserId());
			throw new BadRequestException("User already exists with userId :- " + optionalUser.get().getUserId()
					+ "  & Primary Role :- " + optionalUser.get().getPrimaryRole().getName());
		}
		applicant.get().setApplicantType(role.getName());
		applicant.get().setUidToken(userVO.getUidToken());
		applicant.get().setlUpdate(LocalDateTime.now());
		applicant.get().setModifiedBy(parentUserDTO.getUserId());

		UserDTO childUser = userMapper.convertVO(userVO);
		childUser.setUserLevel(RoleEnum.UserLevel.USER2.getLevel());
		LocalDateTime time = LocalDateTime.now();
		childUser.setOffice(parentUserDTO.getOffice());
		childUser.setStartDate(userVO.getStartDate());
		childUser.setEndDate(userVO.getEndDate());
		childUser.setPasswordResetRequired(Boolean.TRUE);
		childUser.setPrimaryRole(parentUserDTO.getPrimaryRole());
		childUser.setParentId(parentUserDTO.getUserId());
		childUser.setUidToken(userVO.getUidToken());

		Optional<AadharDetailsResponseVO> aadhaarResponseDetails = Optional.empty();

		// if (null == userVO.getChildAadharResponseVO()) {
		// throw new BadRequestException("please authenticat with financier child
		// user..!");
		// }
		// aadhaarResponseDetails =
		// restGateWayService.validateAadhaar(userVO.getChildAadharResponseVO(), null);
		//
		// if (!(aadhaarResponseDetails.isPresent())) {
		// throw new BadRequestException("aadhar response not getting ...!");
		// }
		// if (!(aadhaarResponseDetails.get().getAuth_status().equals("SUCCESS"))) {
		// logger.error("child finacier aadhar authentication failed...!",
		// aadhaarResponseDetails.get().getUid(),
		// aadhaarResponseDetails.get().getAuth_err_code());
		// throw new
		// BadRequestException(aadhaarResponseDetails.get().getAuth_err_code());
		// }

		childUser.setAadharNo(userVO.getAadharNo());
		childUser.setUserAadhaarAuthTime(LocalDateTime.now());

		childUser.setCreatedDate(time);
		childUser.setCreatedDateStr(time.toString());
		childUser.setlUpdate(time);
		childUser.setCreatedBy(userId);
		// By default Created user must be Active
		childUser.setStatus(true);

		childUser.setStatus(true);
		if (StringUtils.isEmpty(parentUserDTO.getInstitutionName())) {
			childUser.setInstitutionName(parentUserDTO.getFirstName());
		} else {
			childUser.setInstitutionName(parentUserDTO.getInstitutionName());
		}

		childUser.setFirstName(parentUserDTO.getFirstName());
		childUser.setFirstname(parentUserDTO.getFirstname());
		childUser.setIsAccountNonLocked(true);
		childUser.setUserStatus(UserStatusEnum.ACTIVE);
		childUser.setAdditionalRoles(Collections.emptyList());
		if (StatusRegistration.FINANCE.getDescription().equals(parentUserDTO.getPrimaryRole().getName())) {
			// Setting applicant type as Logged in user Role(FINANCIER/DEALER)
			childUser.setUserId(financierMasterService.geneateFinancerApplicationSeries());
			childUser.setPassword(financierPassword);
		}

		if (null == userVO.getParentAadharResponseVO()) {
			throw new BadRequestException("please authenticat with financier parent user..!");
		}
		if (!userVO.getParentAadharResponseVO().getUid_num().equals(parentUserDTO.getAadharNo())) {
			throw new BadRequestException(
					"aadhar number mismatched ...!" + userVO.getParentAadharResponseVO().getUid_num());
		}
		aadhaarResponseDetails = restGateWayService.validateAadhaar(userVO.getParentAadharResponseVO(), null);

		if (!(aadhaarResponseDetails.isPresent())) {
			throw new BadRequestException("aadhar response not getting ...!");
		}
		if (!(aadhaarResponseDetails.get().getAuth_status().equals("SUCCESS"))) {
			logger.error("parent finacier aadhar authentication failed...!", aadhaarResponseDetails.get().getUid(),
					aadhaarResponseDetails.get().getAuth_err_code());
			throw new BadRequestException(aadhaarResponseDetails.get().getAuth_err_code());
		}

		applicantDetailsDAO.save(applicant.get());
		childUser.setUserStatus(UserStatusEnum.ACTIVE);
		childUser = userDao.save(childUser);
		return userMapper.UserAndPassword(childUser);

	}

	@Override
	public void modifyUserDetails(FinancierUserUpdateVO userVO, JwtUser jwtUser) {

		if (userVO.getUserId() == null) {
			throw new BadRequestException("please provide proper user Name :" + userVO.getUserId().trim());
		}

		Optional<UserDTO> userDTO = userDao.findByUserIdAndParentId(userVO.getUserId(), jwtUser.getUsername());

		if (userDTO == null) {
			throw new BadRequestException("user Doesn't exist :" + userVO.getUserId().trim());
		}

		Optional<FinancierUserLogDTO> financierUserLogDTO = Optional
				.of(userMapper.convertEntityFinancierUserLogDTO(userDTO.get()));
		if (jwtUser.getUsername().equals(userVO.getUserId())) {
			throw new BadRequestException("please try with finacier child id  user only ...!");
		}
		if (StringUtils.isEmpty(userVO.getAadharNo()) && StringUtils.isEmpty(userVO.getEmail())
				&& StringUtils.isEmpty(userVO.getMobile()) && userVO.getUserAction() == null
				&& userVO.getFinancierStatus() == null) {
			throw new BadRequestException("please update at least one field ..!");
		}

		if (StringUtils.isEmpty(userVO.getRemarks())) {
			throw new BadRequestException("please specify the remarks ..!");
		}

		if (!(StringUtils.isEmpty(userVO.getEmail()))) {
			userDTO.get().setEmail(userVO.getEmail());
		}
		if (!(StringUtils.isEmpty(userVO.getMobile()))) {
			userDTO.get().setMobile(userVO.getMobile());
		}

		if (userVO.getUserAction() != null) {
			List<ActionDTO> list = new ArrayList<ActionDTO>();
			List<ActionsVO> listvo = userVO.getUserAction();
			for (ActionsVO Actionsvo : listvo) {
				ActionDTO ActionDTO = new ActionDTO();
				ActionDTO.setId(Actionsvo.get_id());
				ActionDTO.setActionName(Actionsvo.getActionName());
				list.add(ActionDTO);
				userDTO.get().setActionItems(list);

			}
		}
		if (userVO.getFinancierStatus() != null && userVO.getFinancierStatus()) {
			userDTO.get().setUserStatus(UserStatusEnum.ACTIVE);
			userDTO.get().setStatus(true);
		}
		if (userVO.getFinancierStatus() != null && !(userVO.getFinancierStatus())) {
			userDTO.get().setUserStatus(UserStatusEnum.INACTIVE);
			userDTO.get().setStatus(false);
		}

		Optional<UserDTO> parentuserDTO = userDao.findByUserId(jwtUser.getUsername());

		if (!(StringUtils.isEmpty(userVO.getAadharNo())) && userVO.getAadharNo() != null) {

			if (null != userVO.getChildAadharResponseVO()) {

				Optional<AadharDetailsResponseVO> aadhaarResponseDetailschild = restGateWayService
						.validateAadhaar(userVO.getChildAadharResponseVO(), null);
				if (!(aadhaarResponseDetailschild.isPresent())) {
					throw new BadRequestException("aadhar response not getting ...!");
				}
				if (!(aadhaarResponseDetailschild.get().getAuth_status().equals("SUCCESS"))) {
					logger.error("child finacier aadhar authentication failed...!",
							aadhaarResponseDetailschild.get().getUid(),
							aadhaarResponseDetailschild.get().getAuth_err_code());
					throw new BadRequestException(
							"Child finacier user aadhar : " + aadhaarResponseDetailschild.get().getAuth_err_code());
				}
				userDTO.get().setAadharNo(userVO.getAadharNo());

			} else {
				throw new BadRequestException("please authenticat with financier child user..!");
			}

		}
		if (null == userVO.getParentAadharResponseVO()) {
			throw new BadRequestException("please authenticat with financier parent user..!");
		}
		if (!userVO.getParentAadharResponseVO().getUid_num().equals(parentuserDTO.get().getAadharNo())) {
			throw new BadRequestException(
					"aadhar number mismatched ...!" + userVO.getParentAadharResponseVO().getUid_num());
		}

		Optional<AadharDetailsResponseVO> aadhaarResponseDetails = restGateWayService
				.validateAadhaar(userVO.getParentAadharResponseVO(), null);

		if (!(aadhaarResponseDetails.isPresent())) {
			throw new BadRequestException("aadhar response not getting ...!");
		}
		if (!(aadhaarResponseDetails.get().getAuth_status().equals("SUCCESS"))) {
			logger.error("parent finacier aadhar authentication failed...!", aadhaarResponseDetails.get().getUid(),
					aadhaarResponseDetails.get().getAuth_err_code());
			throw new BadRequestException(
					"Parent finacier user aadhar : " + aadhaarResponseDetails.get().getAuth_err_code());
		}

		userDao.save(userDTO.get());
		financierUserLogDTO.get().setUpdatedPersonAadhar(aadhaarResponseDetails.get().getUid().toString());
		financierUserLogDTO.get().setUserAadhaarAuthTime(LocalDateTime.now());
		financierUserLogDTO.get().setModifiedDate(LocalDateTime.now());
		financierUserLogDTO.get().setlUpdate(LocalDateTime.now());
		financierUserLogDTO.get().setUpdatedBy(parentuserDTO.get().getUserId());
		financierUserLogDTO.get().setRemarks(userVO.getRemarks());
		financierUserLogDAO.save(financierUserLogDTO.get());

		// userDTO.get().setStartDate(userVO.getStartDate());
		// userDTO.get().setEndDate(userVO.getEndDate());
		// // SuperUser can inactive or Active the child User
		// setActionItems
		// userDTO.get().setIsAccountNonLocked(userVO.isStatus());
		// userDTO.get().setUserDepartment(userVO.getUserDepartment());
		// userDTO.get().setUserDesignation(userVO.getUserDesignation());

	}

	@Override
	public List<UserDTO> getAllUsers(UserVO userVO, String userId) {
		List<UserDTO> usersList = userDao.findAllByParentId(userId);
		if (userVO.getUserId() != null)
			return usersList.stream().
			// filter the element to select only those with particular no
					filter(p -> userVO.getUserId().equals(p.getUserId())).
					// put those filtered elements into a new List.
					collect(Collectors.toList());

		if (userVO.getMobile() != null)
			return usersList.stream().
			// filter the element to select only those with particular no
					filter(p -> userVO.getMobile().equals(p.getMobile())).
					// put those filtered elements into a new List.
					collect(Collectors.toList());
		return usersList;
	}

	@Override
	public boolean validateUserId(String userId) {

		Optional<UserDTO> userOPt = userDao.findByUserId(userId.trim());
		return (userOPt.isPresent()) ? true : false;

	}

	@Override
	public List<OfficeDTO> fetchOfficeCode(String officeCode, String selctedRole) {

		/*
		 * List<OfficeDTO> officeList = officeDAO.findByTypeInAndIsActive(
		 * Arrays.asList(OfficeType.RTA.getCode(), OfficeType.MVI.getCode(),
		 * OfficeType.UNI.getCode()), Boolean.TRUE);
		 */
		List<OfficeDTO> officeList = null;
		Optional<OfficeDTO> officeOpt = officeDAO.findByOfficeCodeAndIsActiveTrue(officeCode);
		if (selctedRole.equals(RoleEnum.RTO.getName())) {
			officeList = officeDAO.findByDistrictAndReportingoffice(officeOpt.get().getDistrict(),
					officeOpt.get().getReportingoffice());
		} else {
			officeList = officeDAO.findBydistrict(officeOpt.get().getDistrict());
		}
		return (officeList != null) ? officeList : Collections.emptyList();
	}

	@Override
	public List<RolesDTO> getAllRoles() {

		List<RolesDTO> rolesList = rolesDAO.findAll();
		rolesList.forEach(role -> {
			role.setRoleId(Float.valueOf(role.getId()).intValue());
		});
		return rolesList.stream().filter(r -> RoleEnum.getOfficersOnly().contains(r.getName())).sorted((r1, r2) -> {
			return Double.valueOf(r1.getId()).compareTo(Double.valueOf(r2.getId()));
		}).collect(Collectors.toList());

	}

	private String generateEmpCode(String officeCode) {

		Optional<OfficeDTO> officeDTOOpt = officeDAO.findByOfficeCode(officeCode);
		List<DistrictDTO> districtDTOOpt = districtDAO.findByDistrictId(officeDTOOpt.get().getDistrict());
		DistrictDTO district = districtDTOOpt.stream().findFirst().get();
		// List<OfficeDTO> officeDTOList = fetchOfficeCode(officeCode);
		StringBuilder userIdSeries = new StringBuilder();
		userIdSeries.append(district.getZonecode());
		Map<String, String> officeCodeMap = new TreeMap<>();
		officeCodeMap.put("officeCode", officeCode);
		officeCodeMap.put("year", String.valueOf(LocalDate.now().getYear()));
		String serialNo = sequenceGenerator.getSequence(String.valueOf(Sequence.USERID.getSequenceId()), officeCodeMap);
		userIdSeries.append(serialNo);
		return userIdSeries.toString();

	}

	@Override
	public void doAccountLockOrUnlock(UserVO userVO, JwtUser jwtUser, HttpServletRequest request) {
		Optional<UserDTO> userDTO = null;
		UserManagementLogsDto userManageDto = new UserManagementLogsDto();
		Optional<UserDTO> userDTOOpt = userDao.findByUserId(jwtUser.getUsername());
		if (!userDTOOpt.isPresent()) {
			logger.error("Admin details not Found");
			throw new BadRequestException("Admin details not Found");
		}
		if (userVO.getIsAccountNonLocked()) {
			userDTO = userDao.findByUserIdAndIsAccountNonLockedFalse(userVO.getUserId());
		} else {
			userDTO = userDao.findByUserIdAndIsAccountNonLockedTrue(userVO.getUserId());
		}
		if (!userDTO.isPresent()) {
			logger.error("No Records Found");
			throw new BadRequestException("No Records Found");
		}
		userDTO.get().setIsAccountNonLocked(userVO.getIsAccountNonLocked());

		if (userVO.getIsAccountNonLocked() == true) {
			userManageDto.setActionType(UserManagementActions.ACTIVE);
			userDTO.get().setUserStatus(UserStatusEnum.ACTIVE);
		} else {
			userManageDto.setActionType(UserManagementActions.DISABLE);
			userDTO.get().setUserStatus(UserStatusEnum.INACTIVE);
		}
		userDTO.get().setIsAccountNonLocked(userVO.getIsAccountNonLocked());
		userDTO.get().setUpdatedBy(jwtUser.getUsername());
		userDTO.get().setlUpdate(LocalDateTime.now());
		userManageDto.setIpAddress(webUtils.getClientIp(request));
		userManageDto.setModifiedOnUserId(userDTO.get().getUserId());
		userManageDto.setCreatedBy(jwtUser.getUsername());
		userManageDto.setCreatedByAadhar(userDTOOpt.get().getAadharNo());
		userManageDto.setCreatedDate(LocalDateTime.now());
		userManageDto.setlUpdate(LocalDateTime.now());
		userManagementLogsDao.save(userManageDto);
		userDao.save(userDTO.get());

	}

	/**
	 * 
	 * @param userId
	 * @param role
	 * @return
	 */
	@Override
	public UserVO getCollectionCorrectionUserDetails(String userId, String role) {
		Optional<UserDTO> userDetails = userDao.findByUserIdAndPrimaryRoleName(userId, role);
		if (!userDetails.isPresent()) {
			logger.error("User Details Not Found");
			throw new BadRequestException("User Details Not Found");
		}
		return userMapper.getRequiredFields(userDetails.get());
	}
	
	
	
	//@Override
	public void saveUserCorrections(UserDTO userDTO, List<ImageInput> images,
			MultipartFile[] uploadfiles) throws IOException {
	
        	for (ImageInput imageInput : images) {
        		List basdedOnRoll = new ArrayList();
        		basdedOnRoll.add("RTO");
        		imageInput.setFileOrder(0);
        		imageInput.setBasedOnRole(basdedOnRoll);
        	}
        	
		UserCorrectionsDTO userCorrectionsDTO = userMapper.convertUserCorrectionsDTO(userDTO);
		List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = gridFsClient.convertImages(images,
				userCorrectionsDTO.getUserId(), uploadfiles, StatusRegistration.INITIATED.getDescription());
		if (userCorrectionsDTO.getEnclosures() == null) {

			userCorrectionsDTO.setEnclosures(enclosures);
		} else {
			for (ImageInput image : images) {
				if (userCorrectionsDTO.getEnclosures().stream()
						.anyMatch(imagetype -> imagetype.getKey().equals(image.getType()))) {

					KeyValue<String, List<ImageEnclosureDTO>> matchedImage = userCorrectionsDTO.getEnclosures()
							.stream().filter(val -> val.getKey().equals(image.getType())).findFirst().get();
					userCorrectionsDTO.getEnclosures().remove(matchedImage);
					gridFsClient.removeImages(matchedImage.getValue());
				}
			}
			userCorrectionsDTO.getEnclosures().addAll(enclosures);
		}
		userCorrectionsDAO.save(userCorrectionsDTO);
	}
	
	@Override
	public List<ActionDTO> getActionItems(){
		List<ActionDTO> actionItemsList = actionDAO.findAll();		
		return actionItemsList;
	}

}
