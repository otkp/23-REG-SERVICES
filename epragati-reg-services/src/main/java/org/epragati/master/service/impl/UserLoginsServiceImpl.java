package org.epragati.master.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.epragati.aadhaar.AadhaarDetailsRequestVO;
import org.epragati.aadhaar.AadharDetailsResponseVO;
import org.epragati.exception.BadRequestException;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dao.UserLogDAO;
import org.epragati.master.dao.UserLoginsDAO;
import org.epragati.master.dao.UserManagementLogsDao;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.dto.UserLoginsDTO;
import org.epragati.master.dto.UserManagementLogsDto;
import org.epragati.master.mappers.UserLoginsMapper;
import org.epragati.master.mappers.UserMapper;
import org.epragati.master.service.UserLoginsService;
import org.epragati.master.vo.UserLoginsVO;
import org.epragati.master.vo.UserVO;
import org.epragati.rta.service.impl.RTAServiceImpl;
import org.epragati.service.notification.MessageTemplate;
import org.epragati.service.notification.NotificationUtil;
import org.epragati.usermanagement.UserManagementActions;
import org.epragati.util.AppMessages;
import org.epragati.util.IPWebUtils;
import org.epragati.util.validators.PasswordValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * @author saikiran.kola
 *
 */

@Service
public class UserLoginsServiceImpl implements UserLoginsService {

	private static final Logger logger = LoggerFactory.getLogger(UserLoginsServiceImpl.class);

	@Value("${passwordReset.password:}")
	private String resetPassword;

	@Autowired
	UserLoginsDAO userLoginsDAO;

	@Autowired
	private AppMessages appMessages;

	@Autowired
	UserLoginsMapper userLoginsMapper;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private RTAServiceImpl rtaServiceImpl;

	@Autowired
	private UserLogDAO userLogDAO;

	@Autowired
	private NotificationUtil notifications;

	@Autowired
	private UserManagementLogsDao userManagementLogsDao;

	@Autowired
	private IPWebUtils webUtils;

	@Override
	public List<UserLoginsVO> getUserLoginDetails() {
		// TODO Auto-generated method stub
		/**
		 * get all userLogin data
		 */

		List<UserLoginsDTO> userLoginsList = userLoginsDAO.findAll();
		List<UserLoginsVO> voList = userLoginsMapper.convertEntity(userLoginsList);
		return voList;
	}

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Override
	public String changePassword(UserVO model, String currentUser) {
		PasswordValidator pwdvali=new PasswordValidator();
		
		if (model.getConfirmpassword().equals(model.getNewpassword())) {
			Optional<UserDTO> userdetails = userDAO.findByUserId(currentUser);
			if (userdetails.isPresent()) {
				UserDTO userDto = userdetails.get();
				if (passwordEncoder.matches(model.getPassword(), userDto.getPassword())) {
					if (passwordEncoder.matches(model.getNewpassword(), userDto.getPassword())) {
						logger.error("Error because of Old Password is same as New Password");
						throw new BadRequestException("Old password should not same as new password");
					}
					if(!pwdvali.validate(model.getNewpassword())) {
						logger.warn("password must be contain one small letter,one capital letter,one special character,and length must be 6 to 20");
						logger.error("password must be contain one small letter,one capital letter,one special character,and length must be 6 to 20");
						throw new BadRequestException("password must be contain one small letter,one capital letter,one special character,and length should be 6 to 20");
					}
					userDto.setPassword(passwordEncoder.encode(model.getNewpassword()));
					userDto.setPasswordResetRequired(false);
					userDAO.save(userDto);
				} else {
					logger.error("Error because of Old password is incorrect ");
					throw new BadRequestException("Your old password is incorrect");
				}
				return "Your password changed Sucessfully";
			} else {
				logger.error("User Not found for password changed, Input user Id: {}", currentUser);
				throw new BadRequestException("Password is not changed");

			}
		}
		logger.warn("Your Confirm password  and New password must be same");
		logger.error("Error because of new password and confirm password not equal");
		throw new BadRequestException("Your Confirm password  and New password must be same");
	}

	@Override
	public Optional<UserVO> getUserDetails(String UserId) {

		UserDTO userDTO = userDAO.findByUserIdAndStatusTrue(UserId);
		if (userDTO != null) {
			UserVO userVO = userMapper.convertRequired(userDTO);
			return Optional.of(userVO);
		}

		return Optional.empty();
	}

	@Override
	public Optional<UserVO> getUserForPasswordResetDetails(String userId, String loginOfficerOfficeCode) {

		Optional<UserDTO> userDTOOpt = userDAO.findByUserId(userId.trim());
		if (!userDTOOpt.isPresent()) {
			logger.error("User doesn't Exists [{}]", userId);
			throw new BadRequestException("User doesn't Exists!!!");
		}
		UserDTO userDTO = userDTOOpt.get();
		rtaServiceImpl.validateOfficeCode(loginOfficerOfficeCode, userDTO.getOffice().getOfficeCode());
		return Optional.of(userMapper.userManagementRequiredFields(userDTO));
	}

	@Override
	public void resetPassword(AadhaarDetailsRequestVO aadhaarDetailsRequestVO, String userId, JwtUser jwtUser,
			HttpServletRequest request) {

		if (aadhaarDetailsRequestVO == null) {
			logger.error("Aadhaar Authentication is Mandatory");
			throw new BadRequestException("Aadhaar Authentication is Mandatory");
		}
		Optional<UserDTO> userDTOOpt = userDAO.findByUserId(jwtUser.getUsername());
		if (!userDTOOpt.isPresent()) {
			logger.error("Admin details not Found");
			throw new BadRequestException("Admin details not Found");
		}
		if (userDTOOpt.get().getAadharNo() == null) {
			logger.error("Admin Aadhar Details Not Available");
			throw new BadRequestException("Admin Aadhar Details Not Available");
		}
		if (userDTOOpt.get().getUserId().equalsIgnoreCase(userId)) {
			logger.error("Please Opt Change Password");
			throw new BadRequestException("Please Opt Change Password");
		}

		Optional<AadharDetailsResponseVO> aadhaarResponseVO = rtaServiceImpl
				.getAadhaarResponse(aadhaarDetailsRequestVO);
		if (!userDTOOpt.get().getAadharNo().equals(String.valueOf(aadhaarResponseVO.get().getUid()))) {
			logger.error("Unauthorized User/Aadhaar Mismatch");
			throw new BadRequestException("Unauthorized User/Aadhaar Mismatch");
		}
		Optional<UserDTO> existingUserOpt = userDAO.findByUserId(userId);
		UserDTO existingUser = existingUserOpt.get();
		UserDTO oldUserDTO = new UserDTO();
		BeanUtils.copyProperties(existingUser, oldUserDTO);
		existingUser.setPassword(resetPassword);
		existingUser.setPasswordResetRequired(true);
		existingUser.setUpdatedBy(jwtUser.getUsername());
		existingUser.setlUpdate(LocalDateTime.now());
		userDAO.save(existingUser);

		UserManagementLogsDto userManageDto = new UserManagementLogsDto();

		userManageDto.setCreatedBy(jwtUser.getUsername());
		userManageDto.setCreatedByAadhar(userDTOOpt.get().getAadharNo());
		userManageDto.setModifiedOnUserId(existingUserOpt.get().getUserId());
		userManageDto.setCreatedDate(LocalDateTime.now());
		userManageDto.setlUpdate(LocalDateTime.now());
		userManageDto.setIpAddress(webUtils.getClientIp(request));
		userManageDto.setActionType(UserManagementActions.USERPASSWORDRESET);
		userManagementLogsDao.save(userManageDto);
		userLogDAO.save(oldUserDTO);
		notifications.sendNotifications(MessageTemplate.OFFICER_PWD_RESET.getId(), existingUser);
	}

}
