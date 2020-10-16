package org.epragati.mobile.auth.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.common.dao.PropertiesDAO;
import org.epragati.common.dto.PropertiesDTO;
import org.epragati.constants.MessageKeys;
import org.epragati.dao.enclosure.MobileEnclosuresDAO;
import org.epragati.dto.enclosure.ImageEnclosureDTO;
import org.epragati.dto.enclosure.MobileEnclosuresDTO;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.mappers.UserMapper;
import org.epragati.master.vo.UserVO;
import org.epragati.mobile.auth.dao.AuthenticationOTP_DAO;
import org.epragati.mobile.auth.dao.CitizenAppAuthenticationDAO;
import org.epragati.mobile.auth.dto.AuthenticationOTP_DTO;
import org.epragati.mobile.auth.dto.CitizenAppAuthenticationDTO;
import org.epragati.mobile.auth.mapper.CitizenAppAutenticationMapper;
import org.epragati.mobile.auth.service.CitizenAppAuthenticationService;
import org.epragati.mobile.auth.vo.CitizenAppAuthenticationVO;
import org.epragati.mobile.auth.vo.MobileAppRequestVO;
import org.epragati.registration.service.RegistrationMigrationSolutionsService;
import org.epragati.regservice.impl.RegistrationServiceImpl;
import org.epragati.service.enclosure.mapper.EnclosureImageMapper;
import org.epragati.service.enclosure.mapper.ImageMapper;
import org.epragati.service.files.GridFsClient;
import org.epragati.service.notification.MessageTemplate;
import org.epragati.service.notification.NotificationTemplates;
import org.epragati.service.notification.NotificationUtil;
import org.epragati.util.AppMessages;
import org.epragati.util.MobileEnum;
import org.epragati.util.MobileEnum.Otp;
import org.epragati.util.document.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.mongodb.gridfs.GridFSDBFile;

/**
 * 
 * @author roshan.jugalkishor
 *
 */
@Service
public class CitizenAppAuthenticationServiceImpl implements CitizenAppAuthenticationService {

	private static final Logger logger = LoggerFactory.getLogger(CitizenAppAuthenticationServiceImpl.class);

	@Autowired
	private NotificationUtil notifications;

	@Autowired
	private AppMessages appMessages;

	@Autowired
	private CitizenAppAuthenticationDAO citizenAppAuthenticationDAO;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private AuthenticationOTP_DAO authenticationOTP_DAO;

	@Autowired
	private NotificationTemplates notificationTemplate;

	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;

	@Autowired
	private CitizenAppAutenticationMapper citizenAppAutenticationMapper;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private RegistrationMigrationSolutionsService registrationMigrationSolutionsService;

	@Autowired
	private GridFsClient gridFsClient;

	@Autowired
	private RegistrationServiceImpl registrationServiceImpl;

	@Autowired
	private MobileEnclosuresDAO mobileEnclosuresDAO;

	@Autowired
	private EnclosureImageMapper enclosureImageMapper;

	@Autowired
	private ImageMapper imageMapper;
	
	@Autowired
	private PropertiesDAO propertiesDAO;

	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private	UserMapper userMapper;
	
	/**
	 * Sign UP new user
	 * 
	 */
	@Override
	public void createUser(CitizenAppAuthenticationVO citizenAppAuthenticationVO) {
		logger.info("createUser details are {}", citizenAppAuthenticationVO);
		Optional<CitizenAppAuthenticationDTO> optlAppAuthDto = citizenAppAuthenticationDAO
				.findByAadharNoAndStatus(citizenAppAuthenticationVO.getAadharNo(), MobileEnum.ACTIVE.getDescription());
		if (optlAppAuthDto.isPresent()&&citizenAppAuthenticationVO.getIsdeactivate().equals(Boolean.FALSE)) {
			logger.error("createUser User already exist [{}]", citizenAppAuthenticationVO.getMobileNo());
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.USER_ALREADY_EXIST));
		}
		Optional<AuthenticationOTP_DTO> authenticationOTP_DTO = authenticationOTP_DAO
				.findTopByAadharNoAndDeviceNoAndOtpTypeOrderByOtpGeneratedTimeDesc(
						citizenAppAuthenticationVO.getAadharNo(), citizenAppAuthenticationVO.getDeviceNo(),
						Otp.CITIZENSINGUP.getName());
		if (!authenticationOTP_DTO.isPresent()) {
			logger.error("createUser Failed to save otp details because OTP Details not found");
			throw new BadRequestException("Invalid entry Otp verification not completed, Please try again");
		}
		if (optlAppAuthDto.isPresent()&&citizenAppAuthenticationVO.getIsdeactivate().equals(Boolean.TRUE)) {
			checkValidationForDeactivateDevice(optlAppAuthDto,citizenAppAuthenticationVO);
		}
		citizenAppAuthenticationVO.setPassword(passwordEncoder.encode(citizenAppAuthenticationVO.getPassword()));
		citizenAppAuthenticationVO.setStatus(MobileEnum.ACTIVE.getDescription());
		CitizenAppAuthenticationDTO citizenAppAuthenticationDTO = citizenAppAutenticationMapper
				.convertVO(citizenAppAuthenticationVO);
		citizenAppAuthenticationDTO.setOtpDetails(authenticationOTP_DTO.get());
		citizenAppAuthenticationDTO.setCreatedDate(LocalDateTime.now());
		citizenAppAuthenticationDTO.setlUpdate(LocalDateTime.now());
		citizenAppAuthenticationDAO.save(citizenAppAuthenticationDTO);
	}

	/**
	 * 
	 * 
	 * Login User by citizen mobile app
	 * 
	 */

	@Override
	public CitizenAppAuthenticationVO loginUser(String mobileNo, String password, String deviceNo) {
		logger.info("loginUser()-->> mobile Number {}", mobileNo);

		CitizenAppAuthenticationVO citizenAppAuthenticationVO = new CitizenAppAuthenticationVO();
		Boolean isloginInvalid;
		Optional<CitizenAppAuthenticationDTO> optional = citizenAppAuthenticationDAO.findByMobileNoAndStatus(mobileNo,
				MobileEnum.ACTIVE.getDescription());
		if (!optional.isPresent()) {
			logger.error("loginUser mobile login details not found  {}", mobileNo);
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.MOBILE_NUMBER_NOT_REGISTERED));
		}
		CitizenAppAuthenticationDTO citizenAppAuthenticationDTO = optional.get();
		if (!citizenAppAuthenticationDTO.getMobileNo().equalsIgnoreCase(mobileNo)) {
			logger.error("loginUser Mobile number is not registered {}", mobileNo);
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.MOBILE_NUMBER_NOT_REGISTERED));
		}
		isloginInvalid = validationForDeviceSameOrNot(citizenAppAuthenticationDTO.getDeviceNo(), deviceNo, mobileNo);
		if (!(passwordEncoder.matches(password, citizenAppAuthenticationDTO.getPassword()))) {
			logger.error("loginUser()-->> INVALID PASSWORD {}", password);
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.INVALID_PASSWORD));
		}
		if (isloginInvalid) {
			citizenAppAuthenticationVO.setErrormsg(
					"You are already logged in another device. If you want to continue with this device, you have to register again. The previous session will be cleared.");
			citizenAppAuthenticationVO.setIsdeviceDifferent(Boolean.TRUE);
		} else {
			List<RegistrationDetailsDTO> registrationDetailslist = getRegistrationDetailsByAadharNumberList(
					citizenAppAuthenticationDTO.getAadharNo());
			citizenAppAuthenticationVO = citizenAppAutenticationMapper.limitedDashBoardfields(citizenAppAuthenticationDTO);
			citizenAppAuthenticationVO.setNoOfVehicals(registrationDetailslist.size());
		}
		return citizenAppAuthenticationVO;
	}

	private Boolean validationForDeviceSameOrNot(String deviceNo, String deviceNo2,String MobileNo) {
		if (!deviceNo.equalsIgnoreCase(deviceNo2)) {
			logger.error("loginUser device number not same with mobile number {}" ,MobileNo);
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	/**
	 * 
	 * Generate OTP and send to user mobile number
	 */

	@Override
	public CitizenAppAuthenticationVO verifyOTP(String aadharNo, String deviceNo, String otp){
		logger.info("verifyOTP()--> aadharNumber is {} and otp is {}", aadharNo, otp);
		CitizenAppAuthenticationVO citizenAppAuthenticationVO = null;
		String dlNo = StringUtils.EMPTY;
		Optional<AuthenticationOTP_DTO> otionalAuthOtpDto = authenticationOTP_DAO
				.findTopByAadharNoAndDeviceNoAndOtpTypeOrderByOtpGeneratedTimeDesc(aadharNo, deviceNo,
						Otp.CITIZENSINGUP.getName());
		if (!otionalAuthOtpDto.isPresent()) {
			logger.error("Invalid input ..");
			throw new BadRequestException("Invalid Input");
		}
		AuthenticationOTP_DTO authOtpDto = otionalAuthOtpDto.get();
		validationForOtpExpiredOrNot(authOtpDto);
		Boolean isdevicesame =validationForDeviceSameOrNot(authOtpDto.getDeviceNo(),deviceNo,authOtpDto.getMobileNo());
		if (isdevicesame) {
			throw new BadRequestException(
					"You are trying with another mobile.Please try with same mobile");
		}
		otpVerification(authOtpDto,otp);
		dlNo = getDlNo(aadharNo);
		if (otionalAuthOtpDto.isPresent()) {
				CitizenAppAuthenticationDTO citizenAppAuthenticationDTO = getUserDetailsByAadharNumber(aadharNo);
				citizenAppAuthenticationDTO.setOtpDetails(authOtpDto);
				citizenAppAuthenticationDTO.setDlNo(dlNo);
				citizenAppAuthenticationVO = citizenAppAutenticationMapper.convertEntity(citizenAppAuthenticationDTO);
		}
		return citizenAppAuthenticationVO;
	}

	/**
	 * 
	 * @param aadharNumber
	 * @return
	 */
	public CitizenAppAuthenticationDTO getUserDetailsByAadharNumber(String aadharNo) {
		logger.info("Get User Details By Aadhar Numnber-->>>Aadhar Number is {}", aadharNo);
		List<RegistrationDetailsDTO> registrationDetailslist = getRegistrationDetailsByAadharNumberList(aadharNo);
		Integer noOfVehicals = registrationDetailslist.size();
		RegistrationDetailsDTO registrationDetailsDTO = registrationDetailslist.stream().findFirst().get();
		return citizenAppAutenticationMapper.recordsMapping(registrationDetailsDTO, noOfVehicals);
	}

	/**
	 * 
	 * 
	 * 
	 */
	@Override
	public String verifyOTPforFogotPassword(String mobileNo, String deviceNo, String otp) {
		logger.info("verifyOTPforFogotPassword()-->> Mobile Number {}", mobileNo, " and OTP is {}", otp);
		Optional<AuthenticationOTP_DTO> optionalAuthOtpDto = authenticationOTP_DAO
				.findTopByMobileNoAndDeviceNoAndOtpTypeOrderByOtpGeneratedTimeDesc(mobileNo, deviceNo,
						Otp.FORGOTPASSWORD.getName());
		if (!optionalAuthOtpDto.isPresent()) {
			logger.error("verifyOTPforFogotPassword()-->> OTP not generated for this mobile number {}", mobileNo);
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.OTP_NOT_FOUND));
		}
		Boolean isdevicesame = validationForDeviceSameOrNot(optionalAuthOtpDto.get().getDeviceNo(),deviceNo,optionalAuthOtpDto.get().getMobileNo());
		if (isdevicesame) {
			throw new BadRequestException(
					"You are trying with another mobile.Please try with same mobile"+optionalAuthOtpDto.get().getMobileNo());
		}
		validationForOtpExpiredOrNot(optionalAuthOtpDto.get());
		otpVerification(optionalAuthOtpDto.get(),otp);
		return appMessages.getResponseMessage(MessageKeys.VALID_OTP);
	}

	/**
	 * 
	 */
	@Override
	public String forgotPassword(String password, String mobileNo, String deviceNo) {
		Optional<CitizenAppAuthenticationDTO> optionalDTO = citizenAppAuthenticationDAO
				.findByMobileNoAndDeviceNoAndStatus(mobileNo, deviceNo, MobileEnum.ACTIVE.getDescription());

		logger.info("FORGOT PASSWORD-->Mobile Number {}", mobileNo, " and Password{}", password);

		if (!(optionalDTO.isPresent())) {
			logger.info("FORGOT PASSWORD-->Mobile number not found {}", mobileNo);
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.MOBILE_NUMBER_NOT_REGISTERED));
		}
		CitizenAppAuthenticationDTO citizenAppAuthenticationDTO = optionalDTO.get();
		// password not be null
		Optional<AuthenticationOTP_DTO> optional = authenticationOTP_DAO
				.findTopByMobileNoAndDeviceNoAndOtpTypeOrderByOtpGeneratedTimeDesc(mobileNo, deviceNo,
						Otp.FORGOTPASSWORD.getName());
		// updating password and otp details
		citizenAppAuthenticationDTO.setPassword(passwordEncoder.encode(password));
		citizenAppAuthenticationDTO.setOtpDetails(optional.get());
		citizenAppAuthenticationDTO.setlUpdate(LocalDateTime.now());
		citizenAppAuthenticationDAO.save(citizenAppAuthenticationDTO);
		return appMessages.getResponseMessage(MessageKeys.PASSWORD_UPDATED);
	}

	/**
	 * 
	 */
	@Override
	public Pair<Boolean, String> generateOTPUsingAadharNumber(String aadharNo, String deviceNo) {
		logger.info("Generate OTP Service {}", aadharNo);
		AuthenticationOTP_DTO authenticationOTP_DTO = new AuthenticationOTP_DTO();
		Optional<CitizenAppAuthenticationDTO> optlAppAuthDto = citizenAppAuthenticationDAO
				.findByAadharNoAndDeviceNoAndStatus(aadharNo, deviceNo, MobileEnum.ACTIVE.getDescription());
		if (optlAppAuthDto.isPresent()) {
			logger.error("User already exist [{}]", optlAppAuthDto.get().getMobileNo());
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.USER_ALREADY_EXIST));
		}
		
		RegistrationDetailsDTO registrationDetailsDTO = getRegistrationDetailsByAadharNumber(aadharNo);

		if ((registrationDetailsDTO.getApplicantDetails() == null
				|| registrationDetailsDTO.getApplicantDetails().getContact() == null
				|| StringUtils.isBlank(registrationDetailsDTO.getApplicantDetails().getContact().getMobile()))) {
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.MOBILE_NO_IS_NOT_PRESENT)
					+ registrationDetailsDTO.getPrNo());
		}
		checkValidationForGenrateOtpLimitation(aadharNo,deviceNo,Otp.CITIZENSINGUP.getName());
		String otp = genrateRandomOtp();
		authenticationOTP_DTO.setAadharNo(aadharNo);
		authenticationOTP_DTO.setOtp(otp);
		authenticationOTP_DTO.setDeviceNo(deviceNo);
		authenticationOTP_DTO.setOtpGeneratedTime(LocalDateTime.now());
		authenticationOTP_DTO.setOtpType(Otp.CITIZENSINGUP.getName());
		// displayName validation
		authenticationOTP_DTO.setDisplayName(registrationDetailsDTO.getApplicantDetails().getDisplayName());
		authenticationOTP_DTO.setMobileNo(registrationDetailsDTO.getApplicantDetails().getContact().getMobile());
		logger.info("Generate OTP object {}", authenticationOTP_DTO);
		String result = null;
		try {
			authenticationOTP_DAO.save(authenticationOTP_DTO);
			notifications.sendMessageNotification(notificationTemplate::fillTemplate,
					MessageTemplate.CITIZEN_MOBILE_APP_OTP.getId(), authenticationOTP_DTO,
					authenticationOTP_DTO.getMobileNo());
			logger.info("GenerateOTP() notification sent");
			result = appMessages.getResponseMessage(MessageKeys.OTP_SENT);
		} catch (Exception e) {
			logger.error("Failed to send notifications for template id: {}; {}",
					MessageTemplate.CITIZEN_MOBILE_APP_OTP.getId(), e);
			result = appMessages.getResponseMessage(MessageKeys.OTP_NOT_SENT);
		}
		return Pair.of(Boolean.FALSE, result);
	}

	private String genrateRandomOtp() {
		int otpLength = 6;
		String saltChars = "1234567890";
		String otp = null;
		StringBuffer captchaStrBuffer = new StringBuffer();
		java.util.Random rnd = new java.util.Random();
		while (captchaStrBuffer.length() < otpLength) {
			captchaStrBuffer.append(saltChars.charAt(rnd.nextInt(saltChars.length())));
			logger.info("position [{}]", rnd.nextInt(saltChars.length()));
		}
		logger.info("otp length [{}] ", captchaStrBuffer.length());
		if (captchaStrBuffer.toString().length() < 6) {
			logger.info("captchaStrBuffer [{}]", captchaStrBuffer);
			genrateRandomOtp();
		}
		otp = captchaStrBuffer.toString();
		logger.info("otp  [{}] ", otp);
		return otp;
	}

	/**
	 * 
	 */
	@Override
	public Pair<Boolean, String> generateOTPUsingMobileNumberForgotPassword(String mobileNo, String deviceNo) {
		logger.info("Generate OTP Using Mobile Number-->>>Mobile Number {}", mobileNo);
		AuthenticationOTP_DTO authenticationOTP_DTO = new AuthenticationOTP_DTO();
		Optional<CitizenAppAuthenticationDTO> optlAppAuthDto = citizenAppAuthenticationDAO
				.findByMobileNoAndDeviceNoAndStatus(mobileNo, deviceNo, MobileEnum.ACTIVE.getDescription());
		if (!optlAppAuthDto.isPresent()) {
			logger.error("Generate OTP Using Mobile Number-->>>Mobile Number is not register mobile number [{}]",
					mobileNo);
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.MOBILE_NUMBER_NOT_REGISTERED));
		}
		checkValidationForGenrateOtpLimitation(optlAppAuthDto.get().getAadharNo(),deviceNo,Otp.FORGOTPASSWORD.getName());
		String otp = genrateRandomOtp();
		authenticationOTP_DTO.setOtp(otp);
		authenticationOTP_DTO.setDeviceNo(deviceNo);
		authenticationOTP_DTO.setMobileNo(optlAppAuthDto.get().getMobileNo());
		authenticationOTP_DTO.setOtpGeneratedTime(LocalDateTime.now());
		authenticationOTP_DTO.setOtpType(Otp.FORGOTPASSWORD.getName());
		authenticationOTP_DTO.setDisplayName(optlAppAuthDto.get().getFirstName());
		authenticationOTP_DTO.setAadharNo(optlAppAuthDto.get().getAadharNo());
		String result = StringUtils.EMPTY;
		try {
			authenticationOTP_DAO.save(authenticationOTP_DTO);
			notifications.sendMessageNotification(notificationTemplate::fillTemplate,
					MessageTemplate.CITIZEN_MOBILE_APP_OTP.getId(), authenticationOTP_DTO,
					authenticationOTP_DTO.getMobileNo());
			logger.info("Generated OTP For Forget password notification sent");
			result = appMessages.getResponseMessage(MessageKeys.OTP_SENT);
		} catch (Exception e) {
			logger.error("Failed to send notifications for template id: {}; {}",
					MessageTemplate.CITIZEN_MOBILE_APP_OTP.getId(), e);
			result = appMessages.getResponseMessage(MessageKeys.OTP_NOT_SENT);
		}
		return Pair.of(Boolean.FALSE, result);
	}

	@Override
	public Pair<Boolean, String> CheckValdationBasedOnDeviceNo(String deviceNo) {
		Optional<CitizenAppAuthenticationDTO> citizenAppDto = citizenAppAuthenticationDAO
				.findByDeviceNoAndStatus(deviceNo, MobileEnum.ACTIVE.getDescription());
		if (citizenAppDto.isPresent()) {
			return Pair.of(Boolean.TRUE, "User already Exists");

		}
		return Pair.of(Boolean.FALSE, "User doesn't Exists");
	}

	private void checkValidationForDeactivateDevice(Optional<CitizenAppAuthenticationDTO> optional,CitizenAppAuthenticationVO citizenAppAuthenticationVO) {
		CitizenAppAuthenticationDTO citizenAppAuthenticationDTO = optional.get();
		if (citizenAppAuthenticationVO.getIsdeactivate().equals(Boolean.TRUE)) {
			citizenAppAuthenticationDTO.setStatus(MobileEnum.INACTIVE.getDescription());
			citizenAppAuthenticationDTO.setIsdeactivate(Boolean.TRUE);
			citizenAppAuthenticationDTO.setIsdeactivateDate(LocalDateTime.now());
			citizenAppAuthenticationDTO.setlUpdate(LocalDateTime.now());
			logger.info("Device Deactivated with device No And Mobile No" + citizenAppAuthenticationDTO.getDeviceNo(),
					citizenAppAuthenticationDTO.getMobileNo());
			citizenAppAuthenticationDAO.save(citizenAppAuthenticationDTO);
		}
	}

	@Override
	public String getApplicateImageForMobile(String mobileAppRequestVO, MultipartFile[] multipart) throws IOException {
		CitizenAppAuthenticationDTO citizenAppDto = null;
		MobileAppRequestVO mobileAppReqVO = null;

		Optional<MobileAppRequestVO> inputOptional = registrationServiceImpl.readValue(mobileAppRequestVO,
				MobileAppRequestVO.class);
		if (!inputOptional.isPresent()) {
			throw new BadRequestException("Invalid Inputs.");
		}
		Optional<CitizenAppAuthenticationDTO> optionalDTO = citizenAppAuthenticationDAO
				.findByMobileNoAndDeviceNoAndAadharNoAndStatus(inputOptional.get().getMobileNo(),
						inputOptional.get().getDeviceNo(), inputOptional.get().getAadharNo(),
						MobileEnum.ACTIVE.getDescription());
		if (!optionalDTO.isPresent()) {
			throw new BadRequestException("No Details Found");
		}
		citizenAppDto = optionalDTO.get();
		mobileAppReqVO = inputOptional.get();
		saveImages(mobileAppReqVO, citizenAppDto, multipart);
		return MessageKeys.MESSAGE_SUCCESS;
	}

	private void saveImages(MobileAppRequestVO mobileAppReqVO, CitizenAppAuthenticationDTO citizenAppDto,
			MultipartFile[] multipart) throws IOException {
		MobileEnclosuresDTO dto;
		List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = gridFsClient.convertImages(
				mobileAppReqVO.getImageInput(), citizenAppDto.getAadharNo(), multipart,
				MobileEnum.ACTIVE.getDescription());
		Optional<MobileEnclosuresDTO> enclosuresOptional = mobileEnclosuresDAO.findByAadharNoAndDeviceNoAndMobileNo(
				citizenAppDto.getAadharNo(), citizenAppDto.getDeviceNo(), citizenAppDto.getMobileNo());
		if (enclosuresOptional.isPresent()) {
			dto = enclosuresOptional.get();
			KeyValue<String, List<ImageEnclosureDTO>> keyValue = dto.getEnclosures().stream().findFirst().get();
			enclosuresOptional.get().getEnclosures().removeIf(val -> keyValue.getKey().equals(val.getKey()));
			gridFsClient.removeImages(keyValue.getValue());
			logger.debug("Image Removed : [{}]", enclosuresOptional.get());
			dto = enclosuresOptional.get();
			dto.setEnclosures(enclosures);
			dto.setlUpdate(LocalDateTime.now());
		} else {
			dto = new MobileEnclosuresDTO();
			dto.setDeviceNo(mobileAppReqVO.getDeviceNo());
			dto.setAadharNo(mobileAppReqVO.getAadharNo());
			dto.setMobileNo(mobileAppReqVO.getMobileNo());
			dto.setCreatedDate(LocalDateTime.now());
			dto.setlUpdate(LocalDateTime.now());
			dto.setEnclosures(enclosures);
		}
		mobileEnclosuresDAO.save(dto);
	}

	@Override
	public Optional<GridFSDBFile> getEnclosureOfApplicateImage(String aadharNo, String mobileNo, String deviceNo) {
		Optional<CitizenAppAuthenticationDTO> optionalDTO = citizenAppAuthenticationDAO
				.findByMobileNoAndDeviceNoAndAadharNoAndStatus(mobileNo, deviceNo, aadharNo,
						MobileEnum.ACTIVE.getDescription());
		if (!optionalDTO.isPresent()) {
			throw new BadRequestException("User Not Exits");
		}
		Optional<MobileEnclosuresDTO> enclosuresOptional = mobileEnclosuresDAO
				.findByAadharNoAndDeviceNoAndMobileNo(aadharNo, deviceNo, mobileNo);

		if (!enclosuresOptional.isPresent()) {
			throw new BadRequestException("No Images Found");
		}
		MobileEnclosuresDTO enclosureDTO = enclosuresOptional.get();
		KeyValue<String, List<ImageEnclosureDTO>> encDTO = enclosureDTO.getEnclosures().stream().findFirst().get();
		ImageEnclosureDTO imageDTO = encDTO.getValue().stream().findFirst().get();
		Optional<GridFSDBFile> file = gridFsClient.findFilesInGridFsById(imageDTO.getImageId());
		return file;
	}
	
	private void otpVerification(AuthenticationOTP_DTO authOtpDto, String otp) {
		if(!authOtpDto.getOtp().equalsIgnoreCase(otp)){
			logger.error("Invalid OTP {}", otp);
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.INVALID_OTP));
		}
	}
	
	@Override
	public String getDlNo(String aadharNo){
		Optional<PropertiesDTO> propertiesOptional = propertiesDAO.findByDlUrlsStatusTrue();
		if(!propertiesOptional.isPresent()){
			logger.error("properties file missed in DB for DL urls ..");
			throw new BadRequestException("unable to process your request ,Invalid Input");
		}
		PropertiesDTO properties = propertiesOptional.get();
		String url = properties.getDlUrls().values().toString(); 
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> httpEntity = new HttpEntity<>(aadharNo, headers);
		Map<String, String> uriParams = null;
		if (StringUtils.isNoneBlank(aadharNo)) {
			uriParams = new HashMap<>();
			uriParams.put("aadharNo", aadharNo);
		}
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url.replace("[","").replace("]", "")).queryParam("aadharNo",
				aadharNo);
		try {
		ResponseEntity<String> response = restTemplate.exchange(builder.buildAndExpand(uriParams).toUri(), HttpMethod.GET, httpEntity,
				String.class);
			//response = restTemplate.exchange(urls, HttpMethod.GET, httpEntity, String.class);
		if(StringUtils.isNotBlank(response.getBody())&&response.getBody()!=null){
			return response.getBody();
		}else{
			logger.info("DL Number Not Found with aadharNo No [{}] ",aadharNo);	
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.DL_NUMBER_NOT_FOUND));	
		}
		} catch (Exception e) {
			logger.info("DL Number Not Found with aadharNo No [{}] ",aadharNo,MessageKeys.DL_NUMBER_REQUEST_FAILED);	
		}
		return StringUtils.EMPTY;
	}
	
	private RegistrationDetailsDTO getRegistrationDetailsByAadharNumber(String aadharNo) {
		List<RegistrationDetailsDTO> registrationDetailslist = getRegistrationDetailsByAadharNumberList(aadharNo);
				RegistrationDetailsDTO registrationDetailsDTO = registrationDetailslist.stream().findFirst().get();
		return registrationDetailsDTO;
	}
	
	private void validationForOtpExpiredOrNot(AuthenticationOTP_DTO authOtpDto) {
		long timePeriod = ChronoUnit.MINUTES.between(authOtpDto.getOtpGeneratedTime(),LocalDateTime.now());
		Optional<PropertiesDTO> propertiesOptional = propertiesDAO.findByDlUrlsStatusTrue();
		if(!propertiesOptional.isPresent()){
			logger.error("properties file missed in DB for DL urls ..");
			throw new BadRequestException("unable to process your request ,master data not found");
		}
		if(timePeriod > propertiesOptional.get().getOtpExpiredTime()){
			throw new BadRequestException("Otp Expired Please Try Again");
		}
	}
	
	private void checkValidationForGenrateOtpLimitation(String aadharNo, String deviceNo, String desc) {
		String input1 = LocalDate.now() + "T00:00:00.000Z";
		ZonedDateTime zdt1 = ZonedDateTime.parse(input1);
		LocalDateTime ldt1 = zdt1.toLocalDateTime();
		String input2 = LocalDate.now() + "T23:59:59.999Z";
		ZonedDateTime zdt2 = ZonedDateTime.parse(input2);
		LocalDateTime ldt2 = zdt2.toLocalDateTime();
		List<AuthenticationOTP_DTO> listAuthenticationOtpDto = authenticationOTP_DAO
				.findFirst11ByAadharNoAndDeviceNoAndOtpTypeAndOtpGeneratedTimeBetween(aadharNo, deviceNo, desc,
						ldt1.minusDays(1), ldt2);
		if(!listAuthenticationOtpDto.isEmpty()) {
			Optional<PropertiesDTO> propertiesOptional = propertiesDAO.findByDlUrlsStatusTrue();
			if(!propertiesOptional.isPresent()){
				logger.error("properties file missed in DB for DL urls ..");
				throw new BadRequestException("unable to process your request ,master data not found");
			}
			if(listAuthenticationOtpDto.size() >= propertiesOptional.get().getOtpLimitationPerDay()) {
				throw new BadRequestException("You are tried maximum times please try next day");
			}
		}
	}
	private List<RegistrationDetailsDTO> getRegistrationDetailsByAadharNumberList(String aadharNo) {
		List<RegistrationDetailsDTO> registrationDetailslist = registrationDetailDAO
				.findByApplicantDetailsAadharNoInAndApplicantDetailsIsAadhaarValidatedTrue(aadharNo);
		if (CollectionUtils.isEmpty(registrationDetailslist)) {
			logger.error("Records Not Found for this aadharN [{}]", aadharNo);
			throw new BadRequestException("No Records Found");
		}
		registrationDetailslist = registrationMigrationSolutionsService
				.removeInactiveRecordsToList(registrationDetailslist);
		return registrationDetailslist;
	}

	@Override
	public Optional<UserVO> getUserDetailsByUserNameAndMobile(String username, String mobile) {
		Optional<UserDTO> userOptional = Optional.empty(); 
		Optional<UserVO> userVO = Optional.empty();
		try {
			userOptional = userDAO.findByUserNameAndMobile(username, mobile);
			userVO = userMapper.convertEntity(userOptional); 
		}catch(Exception e) {
			logger.info("Some thing went wrong while retriving userDetails [{}]" , e.getMessage());
		}
		if (!userOptional.isPresent()) {
			logger.info("User is not found [{}]" , username);
			//throw new UsernameNotFoundException(String.format("No user found with username:"+username+"and mobile:"+mobile ));
			throw new UsernameNotFoundException(String.format("Invalid USERNAME/PASSWORD :"+username+"and mobile:"+mobile ));
		}
		logger.debug("User is found {},{}", username,mobile);

		return userVO;
	}
}
