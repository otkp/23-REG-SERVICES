package org.epragati.aadhaarAPI;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.epragati.aadhaar.AadhaarDetailsRequestVO;
import org.epragati.aadhaar.AadharDetailsResponseVO;
import org.epragati.aadhaarAPI.DAO.AadhaarTransactionBackUpDAO;
import org.epragati.aadhaarAPI.DAO.AadhaarTransactionDAO;
import org.epragati.aadhaarAPI.DTO.AadhaarTransactionDTO;
import org.epragati.aadhaarAPI.mapper.AadhaarDetailsMapper;
import org.epragati.aadhaarAPI.util.APIResponse;
import org.epragati.aadhaarAPI.util.AadhaarConstant;
import org.epragati.aadhaarAPI.util.AadhaarRestRequest;
import org.epragati.aadhaarAPI.util.AadhaarRestResponse;
import org.epragati.aadhaarAPI.util.DateUtil;
import org.epragati.aadhaarAPI.util.EKYCData;
import org.epragati.aadhaarAPI.util.PIDData;
import org.epragati.aadhaarAPI.util.ResponseStatusEnum;
import org.epragati.aadhaarAPI.util.ResponseType;
import org.epragati.aadhar.MacIdUtil;
import org.epragati.apts.aadhaar.ConsumeAptsAadhaarService;
import org.epragati.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.ecentric.bean.BiometricresponseBean;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author naga.pulaparthi
 *
 */
@Service
@EnableScheduling
public class AadhaarRestServiceConsumerImpl implements AadhaarRestServiceConsumer {

	@Value("${rta.aadhar.service.endpoint:}")
	private String eKycUrl;

	@Value("${rta.aadhar.service.otp.request:}")
	private String otpReqUrl;

	@Value("${rta.aadhar.service.otp.auth:}")
	private String otpAuthUrl;

	@Value("${rta.aadhar.service.biometrictype:}")
	private String biometricType4TCS;// E-KYC or OTP

	@Value("${rta.aadhar.service.version:}")
	private String version4TCS;

	@Value("${rta.aadhar.service.departement:}")
	private String departement;

	@Value("${rta.aadhar.service.scheme:}")
	private String scheme;

	@Value("${rta.aadhar.service.service:}")
	private String service4TCS;

	@Value("${isForUnitTest:false}")
	private boolean isForQA;

	@Value("${rta.aadhar.service.consent:}")
	private String consentDesc;

	@Value("${opt.cer.file.path:}")
	private String otpCerFilePath;

	// private String filePath=classLoader.getResource(otpCerFilePath).getFile();

	@Value("${otp.auth.wadh:}")
	private String otpAuthWadh;

	@Autowired
	private MacIdUtil macIdUtil;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private AadhaarTransactionDAO aadhaarTransactionDAO;

	@Autowired
	private AadhaarTransactionBackUpDAO aadhaarTransactionBackUpDAO;

	@Autowired
	AadhaarDetailsMapper mapper;
	
	@Autowired
	private ConsumeAptsAadhaarService consumeAptsAadhaarService;
	
	@Autowired
	private AptsAadhaarResponseMapper aptsAadhaarResponseMapper;

	@Autowired
	private static final Logger logger = LoggerFactory.getLogger(AadhaarRestServiceConsumerImpl.class);

	private final static String prepend = "UKC:";
	
	@Value("${apts.request.allow}")
	private String isAptsRequest;

	//	@Override
//	public AadharDetailsResponseVO getAadhaarData(AadhaarDetailsRequestVO req, AadhaarSourceDTO aadhaarSourceDTO) {
//
//		req.setTid(getTid(req.getUid_num(), prepend));// time stamp +uid
//		return sendResposeToClients(cosumeServe(createRequest(req), eKycUrl, aadhaarSourceDTO));
//
//	}
	//disabling apt online aadhar rest services
//	@Override
//	public AadharDetailsResponseVO getAadhaarData(AadhaarDetailsRequestVO req,AadhaarSourceDTO aadhaarSourceDTO) {
//		HttpHeaders headers = new HttpHeaders();
//		headers.setContentType(MediaType.APPLICATION_JSON);
//		headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
//		HttpEntity<AadhaarDetailsRequestVO> httpEntity = new HttpEntity<>(req, headers);
//		ResponseEntity<String> exchange = restTemplate.exchange(eKycUrl, HttpMethod.POST, httpEntity, String.class);
//		AadharDetailsResponseVO aadharDetailsResponseVO=null;
//		try {
//			APIResponse<AadharDetailsResponseVO> resultOPt = parseJson(exchange.getBody(),
//					new TypeReference<APIResponse<AadharDetailsResponseVO>>() {
//					});
//			 aadharDetailsResponseVO = resultOPt.getResult();
//		} catch (Exception e) {
//			aadharDetailsResponseVO = new AadharDetailsResponseVO();
//			aadharDetailsResponseVO.setAuth_status(ResponseStatusEnum.AADHAARRESPONSE.FAILED.getResponseStatus());
//			aadharDetailsResponseVO.setAuth_err_code(e.getMessage());
//			logger.error("Exception while converting from Aadhar responce to RTA object: {}", e);
//		}
//		return aadharDetailsResponseVO;
//	}
	private <T> T parseJson(String value, TypeReference<T> valueTypeRef) {
		try {
			return objectMapper.readValue(value, valueTypeRef);
		} catch (IOException ioe) {
			logger.error("Exception occured while parsing the json ", ioe);
		}
		return null;
	}
	@Override
	public AadharDetailsResponseVO getAadhaarData(AadhaarDetailsRequestVO req, AadhaarSourceDTO aadhaarSourceDTO) {
		if(isAptsRequest.equalsIgnoreCase("Y")) {
			return this.consumeAptsSoapApi(req);
		}else {
		req.setTid(getTid(req.getUid_num(), prepend));// time stamp +uid
		return sendResposeToClients(cosumeServe(createRequest(req), eKycUrl, aadhaarSourceDTO));
		}
	}

//	@Override
//	public AadharDetailsResponseVO sendOTPRequest(AadhaarDetailsRequestVO req) {
//		req.setBt(AadhaarConstant.BiometricType.OPT.getContent());
//		req.setService(AadhaarConstant.ServiceType.OPT.getContent());
//		// req.setRequestType("OTP");
//		req.setOtpChannel(AadhaarConstant.OtpChannel.MOBILE.getContent());
//		req.setAttemptType(AadhaarConstant.AttemptType.OPT.getContent());
//		req.setTid(getTid(req.getUid_num()));
//		AadhaarSourceDTO aadhaarSourceDTO = new AadhaarSourceDTO();
//		aadhaarSourceDTO.setService(req.getService());
//		//return sendResposeToClients(cosumeServe(createRequest(req), otpReqUrl,aadhaarSourceDTO));
//		return getAadhaarData(req,null);
//	}
	
	@Override
	public AadharDetailsResponseVO sendOTPRequest(AadhaarDetailsRequestVO req) {
		if(isAptsRequest.equalsIgnoreCase("Y")) {
			return this.consumeAptsSoapApi(req);
		}else {
		req.setBt(AadhaarConstant.BiometricType.OPT.getContent());
		req.setService(AadhaarConstant.ServiceType.OPT.getContent());
		// req.setRequestType("OTP");
		req.setOtpChannel(AadhaarConstant.OtpChannel.MOBILE.getContent());
		req.setAttemptType(AadhaarConstant.AttemptType.OPT.getContent());
		req.setTid(getTid(req.getUid_num()));
		AadhaarSourceDTO aadhaarSourceDTO = new AadhaarSourceDTO();
		aadhaarSourceDTO.setService(req.getService());
		return sendResposeToClients(cosumeServe(createRequest(req), otpReqUrl,aadhaarSourceDTO));
		}
	}

//	@Override
//	public AadharDetailsResponseVO otpAuthentication(AadhaarDetailsRequestVO req) {
		//		req.setBt(AadhaarConstant.BiometricType.EKYCOTP.getContent());
//		req.setService(AadhaarConstant.ServiceType.EKYC.getContent());
//		// req.setRequestType("EKYC");
//		req.setOtpChannel(AadhaarConstant.OtpChannel.MOBILE.getContent());
//		req.setAttemptType(AadhaarConstant.AttemptType.EKAYCOPT.getContent());
//		req.setTid(getTid(req.getUid_num(), prepend));// time stamp +uid
//
//		PIDData pidata = new PIDData();
//		ClassLoader classLoader = getClass().getClassLoader();
//		pidata.setPIDData(req.getVercode(), otpAuthWadh, classLoader.getResource(otpCerFilePath).getFile(),
//				version4TCS);
//		PIDData.PIDParams pidParam = pidata.getPidValues();
//		req.setEncSessionKey(pidParam.getSkey());
//		req.setCi(pidParam.getCi());
//		req.setEncHmac(pidParam.getHmac());
//		req.setEncryptedPid(pidParam.getPid());
//		req.setOldTid(concatPrepend(prepend, req.getOldTid()));
//
//		req.getEncryptedPid();
//		AadhaarSourceDTO aadhaarSourceDTO = new AadhaarSourceDTO();
//		aadhaarSourceDTO.setPidData(pidata);
//		aadhaarSourceDTO.setService(req.getService());
//		sendResposeToClients(cosumeServe(createRequest(req), otpAuthUrl, aadhaarSourceDTO));
		//return sendResposeToClients(cosumeServe(createRequest(req), otpAuthUrl, aadhaarSourceDTO));
//		return getAadhaarData(req,null);
//	}

	@Override
	public AadharDetailsResponseVO otpAuthentication(AadhaarDetailsRequestVO req) {
		if(isAptsRequest.equalsIgnoreCase("Y")) {
			return this.consumeAptsSoapApi(req);
		}else {
		req.setBt(AadhaarConstant.BiometricType.EKYCOTP.getContent());
		req.setService(AadhaarConstant.ServiceType.EKYC.getContent());
		// req.setRequestType("EKYC");
		req.setOtpChannel(AadhaarConstant.OtpChannel.MOBILE.getContent());
		req.setAttemptType(AadhaarConstant.AttemptType.EKAYCOPT.getContent());
		req.setTid(getTid(req.getUid_num(), prepend));// time stamp +uid
		PIDData pidata = new PIDData();
		ClassLoader classLoader = getClass().getClassLoader();
		pidata.setPIDData(req.getVercode(), otpAuthWadh, classLoader.getResource(otpCerFilePath).getFile(),
				version4TCS);
		PIDData.PIDParams pidParam = pidata.getPidValues();
		req.setEncSessionKey(pidParam.getSkey());
		req.setCi(pidParam.getCi());
		req.setEncHmac(pidParam.getHmac());
		req.setEncryptedPid(pidParam.getPid());
		req.setOldTid(concatPrepend(prepend, req.getOldTid()));

		req.getEncryptedPid();
		AadhaarSourceDTO aadhaarSourceDTO = new AadhaarSourceDTO();
		aadhaarSourceDTO.setPidData(pidata);
		aadhaarSourceDTO.setService(req.getService());
		return sendResposeToClients(cosumeServe(createRequest(req), otpAuthUrl, aadhaarSourceDTO));
		}
		}
	private void saveLogs(AadhaarRestRequest aadhaarRestRequest, AadhaarRestResponse aadhaarRestResponse, String url,
			AadhaarSourceDTO aadhaarSourceDTO) {

		AadhaarTransactionDTO aadhaarTransactionDTO = new AadhaarTransactionDTO();
		try {
			aadhaarTransactionDTO.setUuId(UUID.randomUUID());
			aadhaarTransactionDTO.setAadhaarRestRequest(aadhaarRestRequest);
			// aadhaarTransactionDTO.setAadhaarResponse(aadhaarRestResponse.toString());
			aadhaarTransactionDTO.setResponse(aadhaarRestResponse);
			aadhaarTransactionDTO.setCreatedDate(LocalDateTime.now());
			aadhaarTransactionDTO.setUrl(url);
			aadhaarTransactionDTO.setAadhaarSourceDTO(aadhaarSourceDTO);
			aadhaarTransactionDAO.save(aadhaarTransactionDTO);
			aadhaarRestResponse.setUuId(aadhaarTransactionDTO.getUuId());
			// aadhaarTransactionDTO.setSource(aadhaarRestRequest.getSource());

			// logger.info("Save Success : AadharNo : {} , id : {}",
			// aadhaarTransactionDTO.getAadhaarRequest().getReqUid(),aadhaarTransactionDTO.getId());

		} catch (Exception e) {
			logger.error("Save Failed : {} , Exception : {}, Cause : {}",
					aadhaarTransactionDTO.getAadhaarRestRequest().getId(), e.getMessage(), e.getCause().getMessage());
		}

	}

	private AadhaarRestRequest createRequest(AadhaarDetailsRequestVO aadhaarDetailsRequestModel) {

		/*
		 * if (StringUtils.isEmpty(aadhaarDetailsRequestModel.getIp())) { // TODO: IP
		 * need to capture from request object
		 * aadhaarDetailsRequestModel.setIp(request.getRemoteAddr());
		 * logger.info("Client IP is", aadhaarDetailsRequestModel.getIp()); } else {
		 * aadhaarDetailsRequestModel.setIp("10.80.1.92"); }
		 */

		// logger.info("Request is sent from IP [{}] ",
		// aadhaarDetailsRequestModel.getIp());
		if (StringUtils.isEmpty(aadhaarDetailsRequestModel.getIp())) {
			// logger.info("ip is set to default 10.80.1.92");
			aadhaarDetailsRequestModel.setIp("10.80.1.92");
		}
		logger.debug("Captured IP [{}], for : {} ", aadhaarDetailsRequestModel.getIp(),
				aadhaarDetailsRequestModel.getUid_num());
		if (StringUtils.isEmpty(aadhaarDetailsRequestModel.getPincode())) {
			aadhaarDetailsRequestModel.setPincode("500072");// any pin number is required.
		}
		aadhaarDetailsRequestModel.setConsentme("Y");
		aadhaarDetailsRequestModel.setConsentDesc(consentDesc);

		if (StringUtils.isEmpty(aadhaarDetailsRequestModel.getUdc())) {
			Optional<String> macIdOptional = macIdUtil.getMacId(isForQA);

			if (!macIdOptional.isPresent()) {
				throw new BadRequestException("Unable to getting macid value is empty.");
			}
			aadhaarDetailsRequestModel.setUdc(macIdOptional.get());
		}
		AadhaarRestRequest aadhaarRestRequest = new AadhaarRestRequest();

		if (StringUtils.isEmpty(aadhaarDetailsRequestModel.getSource())) {
			aadhaarRestRequest.setSource("DL");
		} else {
			aadhaarRestRequest.setSource(aadhaarDetailsRequestModel.getSource());
		}

		aadhaarRestRequest.setAllowPDF(aadhaarDetailsRequestModel.getAllowPDF());
		aadhaarRestRequest.setAttemptCount(aadhaarDetailsRequestModel.getAttemptType());

		aadhaarRestRequest.setCi(aadhaarDetailsRequestModel.getCi()); // ci-- st--ci
		aadhaarRestRequest.setConsent(aadhaarDetailsRequestModel.getConsentme());
		aadhaarRestRequest.setConsentDesc(aadhaarDetailsRequestModel.getConsentDesc());
		aadhaarRestRequest.setDc(aadhaarDetailsRequestModel.getDc());

		aadhaarRestRequest.setDpId(aadhaarDetailsRequestModel.getDpId());
		aadhaarRestRequest.setHmac(aadhaarDetailsRequestModel.getEncHmac());
		aadhaarRestRequest.setId(aadhaarDetailsRequestModel.getUid_num());

		aadhaarRestRequest.setIp(aadhaarDetailsRequestModel.getIp());// currently NA;
		aadhaarRestRequest.setLocalLang(aadhaarDetailsRequestModel.getLocalLang());// --
		aadhaarRestRequest.setMc(aadhaarDetailsRequestModel.getMc());
		aadhaarRestRequest.setMi(aadhaarDetailsRequestModel.getMi());
		aadhaarRestRequest.setPid(aadhaarDetailsRequestModel.getEncryptedPid());

		// New Modification for AADHAAR for resolving duplicate Request (563)
		logger.info("Internal Duplicate Request Check For ANO [{}]", aadhaarDetailsRequestModel.getUid_num());
		if (null != aadhaarDetailsRequestModel.getEncryptedPid()) {
			Optional<AadhaarTransactionDTO> aadharDetailsByPid = aadhaarTransactionDAO

					.findByAadhaarRestRequestPidOrderByCreatedDateDesc(aadhaarDetailsRequestModel.getEncryptedPid());

			if (aadharDetailsByPid.isPresent()) {
				logger.error("Duplicate Request pid Already Exists id: [{}]", aadhaarDetailsRequestModel.getUid_num());
				throw new BadRequestException(
						"Duplicate Request Found For Aadhar No :" + aadhaarDetailsRequestModel.getUid_num());
			}
		}

		aadhaarRestRequest.setPincode(aadhaarDetailsRequestModel.getPincode());
		aadhaarRestRequest.setRdsId(aadhaarDetailsRequestModel.getRdsId());

		aadhaarRestRequest.setSkey(aadhaarDetailsRequestModel.getEncSessionKey());

		aadhaarRestRequest.setSrt(getDate());// today Date

		if (null != aadhaarDetailsRequestModel.getRequestType()
				&& aadhaarDetailsRequestModel.getRequestType().equals("OTP")) {
			//aadhaarRestRequest.setCrt(aadhaarDetailsRequestModel.getCrt());
			aadhaarRestRequest.setCrt(getcrtDate());
			// logger.info("CRT DATE FROM UI IS [{}]", aadhaarDetailsRequestModel.getCrt());
		} else {
			aadhaarRestRequest.setCrt(getcrtDate());// getcrtDate
			// logger.info("CRT DATE FROM BE IS [{}]", aadhaarRestRequest.getCrt());
		}
		// logger.info("CRT DATE FROM UI IS [{}] AND CRT FROM BACKEND IS [{}]",
		// aadhaarDetailsRequestModel.getCrt(),
		// getcrtDate());

		aadhaarRestRequest.setTid(aadhaarDetailsRequestModel.getTid());

		aadhaarRestRequest.setUdc(aadhaarDetailsRequestModel.getUdc());// macID divce number
		aadhaarRestRequest.setRdsVer(aadhaarDetailsRequestModel.getRdsVer());// rdsver from rd service

		// based on selection.
		if (aadhaarDetailsRequestModel.getIdType() == null) {
			aadhaarRestRequest.setIDType("A");
		} else {
			aadhaarRestRequest.setIDType(aadhaarDetailsRequestModel.getIdType());
		}

		if (StringUtils.isBlank(aadhaarDetailsRequestModel.getBt())) {
			aadhaarRestRequest.setBt(biometricType4TCS);
		} else {
			aadhaarRestRequest.setBt(aadhaarDetailsRequestModel.getBt());
		}

		if (StringUtils.isBlank(aadhaarDetailsRequestModel.getService())) {
			aadhaarRestRequest.setService(service4TCS);
		} else {
			aadhaarRestRequest.setService(aadhaarDetailsRequestModel.getService());
		}

		// configured based properties.
		aadhaarRestRequest.setDepartment(departement);
		aadhaarRestRequest.setScheme(scheme);

		aadhaarRestRequest.setVersion(version4TCS);
		if (null != aadhaarDetailsRequestModel.getRequestType()) {
			aadhaarRestRequest.setRequesttype(aadhaarDetailsRequestModel.getRequestType());
		}
		aadhaarRestRequest.setVercode(aadhaarDetailsRequestModel.getVercode());
		aadhaarRestRequest.setOTPChannel(aadhaarDetailsRequestModel.getOtpChannel());
		aadhaarRestRequest.setOldTid(aadhaarDetailsRequestModel.getOldTid());

		return aadhaarRestRequest;

	}

	private AadhaarRestResponse cosumeServe(AadhaarRestRequest aadhaarRestRequest, final String url,
			AadhaarSourceDTO aadhaarSourceDTO) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<AadhaarRestRequest> httpEntity = new HttpEntity<>(aadhaarRestRequest, headers);

		long startTime = System.currentTimeMillis();
		logger.debug("request sending to : {}, id: {}", url, aadhaarRestRequest.getId());
		ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
		logger.debug("Response received for : {}, After :{}ms time :", aadhaarRestRequest.getId(),
				(System.currentTimeMillis() - startTime));
		if (responseEntity == null || StringUtils.isBlank(responseEntity.getBody())) {
			throw new BadRequestException("No Responce from Aadhaar Server");
		}
		AadhaarRestResponse aadhaarRestResponse = null;
		try {
			aadhaarRestResponse = objectMapper.readValue(responseEntity.getBody(), AadhaarRestResponse.class);
		} catch (Exception e) {
			aadhaarRestResponse = new AadhaarRestResponse();
			aadhaarRestResponse.setAuth_status(ResponseStatusEnum.AADHAARRESPONSE.FAILED.getResponseStatus());
			aadhaarRestResponse.setAuth_err_code(e.getMessage());
			logger.error("Exception while converting from Aadhar responce to RTA object: {}", e);
		}
		saveLogs(aadhaarRestRequest, aadhaarRestResponse, url, aadhaarSourceDTO);
		logger.debug("Response from aadhaar : {} for UID/VID :{}", aadhaarRestResponse.getReturnMessage(),
				aadhaarRestRequest.getId());
		return aadhaarRestResponse;
	}

	private AadharDetailsResponseVO sendResposeToClients(AadhaarRestResponse aadhaarRestResponse) {
		AadharDetailsResponseVO am = new AadharDetailsResponseVO();
		am.setKSA_KUA_Txn("-");
		am.setAuth_date("-");
		am.setAuth_transaction_code("-");
		if (!ResponseType.SUCCESS.getLabel().equalsIgnoreCase(aadhaarRestResponse.getReturnMessage())) {
			aadhaarRestResponse.setAuth_status(ResponseStatusEnum.AADHAARRESPONSE.FAILED.getResponseStatus());
			aadhaarRestResponse.setAuth_err_code(aadhaarRestResponse.getReturnMessage());
		} else {
			aadhaarRestResponse.setAuth_status(ResponseStatusEnum.AADHAARRESPONSE.SUCCESS.getResponseStatus());
			if (null != aadhaarRestResponse.getEkycData()) {
				EKYCData ekycData = aadhaarRestResponse.getEkycData();
				am.setCo(ekycData.getCo());
				am.setDistrict(ekycData.getDist());
				am.setDistrict_name(ekycData.getDist());
				am.setDob(ekycData.getDob());
				am.setGender(ekycData.getGender());
				am.setHouse(ekycData.getHouse());
				am.setLc(ekycData.getLoc());
				am.setMandal(ekycData.getSubdist());
				am.setMandal_name(ekycData.getSubdist());
				am.setName(ekycData.getName());
				am.setPincode(ekycData.getPc());
				am.setPo(ekycData.getPo());
				am.setStatecode(ekycData.getState());
				am.setStreet(ekycData.getStreet());
				am.setSubdist(ekycData.getSubdist());
				am.setBase64file(ekycData.getPhoto());
				am.setUid(Long.valueOf(ekycData.getUid()));
				am.setVillage(ekycData.getVtc());
				am.setVillage_name(ekycData.getVtc());
				am.setVtc(ekycData.getVtc());
				am.setPhone(ekycData.getPhone());
				am.setEmail(ekycData.getEmail());
			}
		}
		am.setUuId(aadhaarRestResponse.getUuId());
		am.setAuth_status(aadhaarRestResponse.getAuth_status());
		am.setAuth_err_code(aadhaarRestResponse.getAuth_err_code());
		am.setTid(aadhaarRestResponse.getTid());
		am.setUidToken(aadhaarRestResponse.getUidToken());

		return am;
	}

	private String getTid(final String uid_num, String prepend) {
		return prepend + System.currentTimeMillis() + uid_num;
	}

	private String getTid(final String uid_num) {
		return System.currentTimeMillis() + uid_num;
	}

	private String concatPrepend(String prepend, String str) {
		return prepend + str;
	}

	private String getDate() {

		return DateUtil.getDate(DateUtil.DATE_PATTERN_MISSEC, "IST", new Date());

	}

	private String getcrtDate() {

		return DateUtil.getDate(DateUtil.CRT_DATE_PATTERN, "IST", new Date());

	}

	@Override
	// @Transactional
	// @Scheduled(cron = "${DL.aadhaar.logs.backup:null}")
	public List<AadhaarTransactionDTO> getAllAadhaarLogs() {
//		logger.info("No of Records before update: [{}]", aadhaarTransactionBackUpDAO.count());
//		List<AadhaarTransactionDTO> aadhaarTransactionDTOs = new ArrayList<AadhaarTransactionDTO>();
//
//		aadhaarTransactionDTOs = aadhaarTransactionDAO.findByCreatedDateBefore(LocalDateTime.now().minusDays(1));
//		logger.info("No of Records found : [{}]", aadhaarTransactionDTOs.size());
//		aadhaarTransactionBackUpDAO.insert(mapper.convertEntity(aadhaarTransactionDTOs));
//		logger.info("No of Records Inserted into BackUp: [{}] ", aadhaarTransactionDTOs.size());
//		aadhaarTransactionDAO.delete(aadhaarTransactionDTOs);
//		logger.info("No of Records Deleted : [{}]", aadhaarTransactionDTOs.size());
//		logger.info("Updated count in BackUp: [{}]", aadhaarTransactionBackUpDAO.count());

//		return aadhaarTransactionDTOs;
		return null;

	}
	@Override
	public AadharDetailsResponseVO consumeAptsSoapApi(AadhaarDetailsRequestVO aadhaarDetailsRequestModel) {
		// TODO Auto-generated method stub
		try {
			org.epragati.aadhar.APIResponse<BiometricresponseBean> apiResponse = consumeAptsAadhaarService
					.cosumeAptsAaadhaarResponse(aadhaarDetailsRequestModel);

			if (apiResponse.getStatus()) {
				if (!"SUCCESS".equalsIgnoreCase(apiResponse.getResult().getAuth_status())) {
					logger.error("Aadhaar Response failure [{}]", apiResponse.getResult().getAuth_err_code());
					throw new BadRequestException(apiResponse.getResult().getAuth_err_code());
				}
				Optional<BiometricresponseBean> ofNullable = Optional.ofNullable(apiResponse.getResult());
				BiometricresponseBean aadhaarDetailsResponseDTO = ofNullable.get();
				return aptsAadhaarResponseMapper.convertEntity(aadhaarDetailsResponseDTO);
			} else {
				logger.error("error while consuming apts api"+apiResponse.getMessage());
				throw new BadRequestException(apiResponse.getMessage());
			}
			}catch(Exception e) {
				logger.error("error while consuming apts api"+e.getMessage());
				throw new BadRequestException(e.getMessage());
			}
	}

}
