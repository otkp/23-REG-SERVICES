package org.epragati.restGateway.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.epragati.aadhaar.AadhaarDetailsRequestVO;
import org.epragati.aadhaar.AadharDetailsResponseVO;
import org.epragati.aadhaarAPI.AadhaarRestServiceConsumer;
import org.epragati.aadhaarAPI.AadhaarSourceDTO;
import org.epragati.aadhaarAPI.util.AadhaarConstant;
import org.epragati.aadhar.APIResponse;
import org.epragati.cfst.vcr.dao.VcrDetailsDAO;
import org.epragati.cfst.vcr.dao.VcrDetailsDto;
import org.epragati.cfst.vcr.dao.VcrDetailsMapper;
import org.epragati.cfstSync.vo.PaymentDetails;
import org.epragati.cfstVcr.vo.ResponseVcrEntity;
import org.epragati.cfstVcr.vo.ResponseVcrTaxEntity;
import org.epragati.cfstVcr.vo.TaxPaidVCRDetailsVO;
import org.epragati.cfstVcr.vo.VcrBookingData;
import org.epragati.cfstVcr.vo.VcrInputVo;
import org.epragati.civilsupplies.dao.RationCardDetailsDAO;
import org.epragati.civilsupplies.dao.RationCardDistrictDAO;
import org.epragati.civilsupplies.dto.CardMemberDetails;
import org.epragati.civilsupplies.dto.RationCardDetailsDTO;
import org.epragati.civilsupplies.dto.RationCardDistrictDTO;
import org.epragati.civilsupplies.vo.CardMemberDetailsVO;
import org.epragati.civilsupplies.vo.RationCardDetailsVO;
import org.epragati.common.dto.HsrpDetailDTO;
import org.epragati.common.dto.PanDetailsModel;
import org.epragati.common.vo.UserStatusEnum;
import org.epragati.constants.MessageKeys;
import org.epragati.ecv.vo.EngineChassisNOEntity;
import org.epragati.ecv.vo.EngineChassisNoVO;
import org.epragati.elastic.vo.ElasticSecondVehicleSearchVO;
import org.epragati.excel.service.CellProps;
import org.epragati.excel.service.ExcelService;
import org.epragati.excel.service.ExcelServiceImpl;
import org.epragati.exception.BadRequestException;
import org.epragati.gstn.dao.GSTNDataDAO;
import org.epragati.gstn.dto.GSTNConfig;
import org.epragati.gstn.dto.GSTNDataDTO;
import org.epragati.gstn.dto.GSTNResponse;
import org.epragati.gstn.mapper.GSTNDataMapper;
import org.epragati.gstn.vo.GSTNDataVO;
import org.epragati.hsrp.dao.HSRPDetailDAO;
import org.epragati.hsrp.vo.HSRPRequestModel;
import org.epragati.hsrp.vo.HSRPResposeVO;
import org.epragati.master.dao.AadhaarResponseDAO;
import org.epragati.master.dao.DealerAndVahanMappedCovDAO;
import org.epragati.master.dao.DealerCovDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.OtherStateVahanResponseDAO;
import org.epragati.master.dao.PanResponseDAO;
import org.epragati.master.dao.RegServiceDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.StagingRegistrationDetailsDAO;
import org.epragati.master.dao.TaxDetailsDAO;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dao.VahanResponseDAO;
import org.epragati.master.dto.AadhaarDetailsResponseDTO;
import org.epragati.master.dto.DealerAndVahanMappedCovDTO;
import org.epragati.master.dto.DealerCovDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.PanDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.dto.VahanDetailsDTO;
import org.epragati.master.mappers.AadhaarDetailsResponseMapper;
import org.epragati.master.mappers.PanMapper;
import org.epragati.master.mappers.VahanDetailsMapper;
import org.epragati.master.service.LogMovingService;
import org.epragati.master.service.MandalService;
import org.epragati.master.vo.MandalVO;
import org.epragati.master.vo.PanResponseVO;
import org.epragati.master.vo.PanVO;
import org.epragati.master.vo.VCRVahanVehicleDetailsVO;
import org.epragati.master.vo.VahanDetailsVO;
import org.epragati.master.vo.VahanResponseVO;
import org.epragati.master.vo.VahanVehicleDetailsVO;
import org.epragati.master.vo.VahanVehicleResponseVO;
import org.epragati.payment.dto.BreakPaymentsSaveDTO;
import org.epragati.payment.dto.FeesDTO;
import org.epragati.payment.dto.PaymentTransactionDTO;
import org.epragati.payment.report.vo.RegReportVO;
import org.epragati.payments.dao.PaymentTransactionDAO;
import org.epragati.payments.vo.BreakPayments;
import org.epragati.payments.vo.TransactionDetailVO;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.mapper.OtherStateVahanResponseMapper;
import org.epragati.regservice.otherstate.OtherStateVahanService;
import org.epragati.regservice.vo.OtherStateVahanVO;
import org.epragati.restGateway.RestGateWayService;
import org.epragati.rta.vo.PrGenerationVO;
import org.epragati.sn.dao.SpecialNumberDetailsDAO;
import org.epragati.sn.dto.SpecialNumberDetailsDTO;
import org.epragati.svs.vo.ElasticSecondVehicleResponseVO;
import org.epragati.svs.vo.OwnerDetailsVO;
import org.epragati.svs.vo.ResponseListEntity;
import org.epragati.util.AppMessages;
import org.epragati.util.BidStatus;
import org.epragati.util.CfstStatusTypes;
import org.epragati.util.DateConverters;
import org.epragati.util.EncryptDecryptUtil;
import org.epragati.util.FeeTypeDetails;
import org.epragati.util.GateWayResponse;
import org.epragati.util.StatusRegistration;
import org.epragati.util.payment.PayStatusEnum;
import org.epragati.util.payment.ServiceCodeEnum;
import org.epragati.util.payment.ServiceEnum;
import org.epragati.vcr.service.VcrVahanVehicleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RestGateWayServiceImpl implements RestGateWayService {

	@Autowired
	private RestTemplate restTemplate;

	@Value("${reg.aadhaar.source:}")
	private String source;
	@Value("${reg.service.panValidationUrl:}")
	private String panValidationUrl;

	@Value("${reg.service.aadhaarToken:}")
	private String aadhaarToken;

	@Value("${reg.service.aadhaarValidationUrl:}")
	private String aadhaarValidationUrl;

	@Value("${reg.service.vahanUrl:}")
	private String vahanUrl;

	@Value("${rta.pan.token}")
	private String panToken;

	@Value("${reg.service.secondVehicleSearchUrl}")
	private String secondVehicleSearchUrl;

	@Value("${reg.service.engineChassisNovalidationUrl}")
	private String engineChassisNoUrl;

	@Value("${hsrp.post.tr.records.url}")
	private String POST_TR_HSRP_URL;

	@Value("${hsrp.contenttype}")
	private String contenttype;

	@Value("${hsrp.securitykey}")
	private String securitykey;

	@Value("${hsrp.post.pr.records.url}")
	private String POST_PR_HSRP_URL;

	@Value("${reg.service.cfstSyncUrl:}")
	private String cfstSyncUrl;

	@Value("${elasticSearch.secondVehicle.url:}")
	private String elasticSearchUrl;

	@Value("${reg.service.vcrDetailsFromCfstUrl:}")
	private String vcrDetailsFromCfstUrl;

	@Value("${common.service.cash.payment.reciept.sequence:}")
	private String cashpaymentSeqNo;

	@Value("${dl.reports.revenue:}")
	private String dlRevenueReportUrl;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private PanMapper panMapper;

	@Autowired
	private PanResponseDAO panDao;

	@Autowired
	private VahanResponseDAO vahanDAO;

	@Autowired
	private StagingRegistrationDetailsDAO stagingRegistrationDetailsDAO;

	@Autowired
	private VahanDetailsMapper vahanMapper;

	@Autowired
	private AppMessages appMessages;

	@Autowired
	private AadhaarResponseDAO aadhaarResponseDAO;

	@Autowired
	private AadhaarDetailsResponseMapper aadhaarDetailsResponseMapper;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private DealerCovDAO dealerCovDAO;

	@Autowired
	private DealerAndVahanMappedCovDAO dealerAndVahanMappedCovDAO;

	@Autowired
	private PaymentTransactionDAO paymentTransactionDAO;

	/** cfst sync autowired **/

	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;

	@Autowired
	private SpecialNumberDetailsDAO specialNumberDetailsDAO;

	@Autowired
	private RestGateWayService restGateWayService;

	@Autowired
	private LogMovingService logMovingService;

	@Autowired
	private VcrDetailsDAO vcrDetailsDAO;

	@Autowired
	private VcrDetailsMapper vcrDetailsMapper;

	@Autowired
	private RegServiceDAO regServiceDAO;

	@Autowired
	private HSRPDetailDAO hsrpDetailDAO;

	@Autowired
	private TaxDetailsDAO taxDetailsDAO;

	@Autowired
	private OfficeDAO officeDAO;

	@Value("${reg.dealer.paymentObject.url:}")
	private String paymentUrl;

	@Value("${reg.dealer.prGeneration.url:}")
	private String prGenerationUrl;

	@Value("${gstn.auth:}")
	private String gstnToken;

	@Value("${gstn.token.url:}")
	private String gstnTokenUrl;

	@Value("${gstn.dataposting.url:}")
	private String gstnDatapostingUrl;

	@Autowired
	private GSTNDataDAO gstnDataDAO;

	@Autowired
	private GSTNDataMapper gstnDataMapper;

	@Value("${reg.service.civilSuppliesUrl:}")
	private String rationCardUrl;

	@Autowired
	private RationCardDetailsDAO rationCardDetailsDAO;

	@Autowired
	private RationCardDistrictDAO rationCardDistrictDAO;

	@Value("${reg.service.civilSupplies.authToken:}")
	private String accessToken;

	@Autowired
	private MandalService mandalService;

	@Value("${reg.aadhaar.isInternalConn:true}")
	private boolean isInternalConn;

	@Autowired
	private AadhaarRestServiceConsumer aadhaarRestServiceConsumer;

	@Autowired
	private EncryptDecryptUtil encryptDecryptUtil;

	@Value("${reg.service.vehicle.vahanUrl:}")
	private String vahanVehicleUrl;

	@Value("${dl.revenue.detailed.reports:}")
	private String dlRevenueDetailedReportUrl;

	@Autowired
	private OtherStateVahanService otherStateVahanService;

	@Autowired
	private OtherStateVahanResponseDAO otherStateVahanResponseDAO;

	@Autowired
	private OtherStateVahanResponseMapper otherStateVahanResponseMapper;

	@Autowired
	private VcrVahanVehicleService vcrVahanVehicleService;
	
	@Value("${apts.request.allow}")
	private String isRequestToApts;


	private static final Logger logger = LoggerFactory.getLogger(RestGateWayServiceImpl.class);

	/**
	 * Verify Aadhar Details
	 */
	@Override
	public Optional<AadharDetailsResponseVO> validateAadhaar(AadhaarDetailsRequestVO model,
			AadhaarSourceDTO aadhaarSourceDTO) {
		if(isRequestToApts.equalsIgnoreCase("Y")) {
			AadharDetailsResponseVO consumeAptsSoapApi = aadhaarRestServiceConsumer.consumeAptsSoapApi(model);
			return Optional.ofNullable(consumeAptsSoapApi);
		}else {
		model.setConsentme("Y");
		// model.setBt("IIR");
		model.setAttemptType("1EA");
		//model.setSource(model.getSource() + "_" + source);
		logger.debug("The source for AAdhaar is [{}]", source);
		// logger.info(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_AADHAR_ENTRY));
		if (isInternalConn) {
			AadharDetailsResponseVO aadharDetailsResponseVO = null;
			// List<FieldError> fieldErrors = null;
			if (StringUtils.isEmpty(model.getRequestType())) {
//				fieldErrors = pojoValidatorUtil.doValidation(requestModel, requestModel.getClass().getName());
//
//				if (!fieldErrors.isEmpty()) {
//					return new APIResponse<String>("Validation failed", fieldErrors);
//				}
				aadharDetailsResponseVO = aadhaarRestServiceConsumer.getAadhaarData(model, aadhaarSourceDTO);
			}

			/// --new flow
			// fieldErrors = pojoValidatorUtil.doCustomValidation(requestModel,
			/// requestModel.getClass().getName());

			// if (!CollectionUtils.isEmpty(fieldErrors)) {
			// logger.error("Validation failed {}", fieldErrors);
			// return new APIResponse<String>("Validation failed", fieldErrors);
			// }

			if (AadhaarConstant.RequestType.OPT.getContent().equals(model.getRequestType())) {

				aadharDetailsResponseVO = aadhaarRestServiceConsumer.sendOTPRequest(model);
			} else if (AadhaarConstant.RequestType.EKYC.getContent().equals(model.getRequestType())) {

				aadharDetailsResponseVO = aadhaarRestServiceConsumer.otpAuthentication(model);
			}
			if (null != aadharDetailsResponseVO) {
				saveAadhaarResponce(aadharDetailsResponseVO, model.getUdc());
				if (model.getIsDeptLogin() && aadharDetailsResponseVO.getUid() != null) {
					setUserAuthTime(model.getUserId(), aadharDetailsResponseVO.getUid().toString());
				}
			}

			return Optional.ofNullable(aadharDetailsResponseVO);

		} else {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", aadhaarToken);
			HttpEntity<AadhaarDetailsRequestVO> httpEntity = new HttpEntity<>(model, headers);
			// RestTemplate restTemplateL = new RestTemplate();
			ResponseEntity<String> response = restTemplate.exchange(aadhaarValidationUrl, HttpMethod.POST, httpEntity,
					String.class);
			logger.info("Aadhaar Validation Url: [{}] Called ", aadhaarValidationUrl);
			logger.info(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_AADHAR_RESPONSE),
					model.getUid_num()/* response */);

			if (response == null || StringUtils.isBlank(response.getBody())) {
				return Optional.empty();
			}
			APIResponse<AadharDetailsResponseVO> resultOPt = parseJson(response.getBody(),
					new TypeReference<APIResponse<AadharDetailsResponseVO>>() {
					});
			if (null != resultOPt && resultOPt.getStatus() && null != resultOPt.getResult()) {
				if ("SUCCESS".equalsIgnoreCase(resultOPt.getResult().getAuth_status())) {

					AadhaarDetailsResponseDTO aadhaarResponseDTO = aadhaarDetailsResponseMapper
							.convertVO(resultOPt.getResult());
					aadhaarResponseDTO.setDeviceNumber(model.getUdc());
					aadhaarResponseDAO.save(aadhaarResponseDTO);
					if (model.getIsDeptLogin()) {
						setUserAuthTime(model.getUserId(), aadhaarResponseDTO.getUid().toString());
					}

				} // else {
					// logger.error("Response from aadhaar Error found as",
					// resultOPt.getResult().getAuth_err_code());
					// throw new BadRequestException(resultOPt.getResult().getAuth_err_code());
					// }
				return Optional.ofNullable(resultOPt.getResult());
			}
			return Optional.empty();
		}
		}
	}

	@Override
	public Map<Object, Map<String, Double>> dlRevenue(RegReportVO paymentreportVO) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<RegReportVO> httpEntity = new HttpEntity<>(paymentreportVO, headers);
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(dlRevenueReportUrl, HttpMethod.POST, httpEntity, String.class);
			if (response != null && response.hasBody()) {

				GateWayResponse<Map<Object, Map<String, Double>>> paymentsResponse = parseJson(response.getBody(),
						new TypeReference<GateWayResponse<Map<Object, Map<String, Double>>>>() {
						});
				return paymentsResponse.getResult();

				// logger.info("Map " + map);

			}
		} catch (Exception ex) {
			logger.error("Exception occured while getting DL Revenue Data [{}]", ex.getMessage());

		}
		return Collections.emptyMap();

	}

	@Override
	public List<RegReportVO> dlRevenueDetailed(RegReportVO paymentreportVO, Pageable page) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<RegReportVO> httpEntity = new HttpEntity<>(paymentreportVO, headers);
		ResponseEntity<String> response = null;
		try {
			UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(dlRevenueDetailedReportUrl)
					.queryParam("page", page.getPageNumber()).queryParam("size", page.getPageSize());
			response = restTemplate.exchange(builder.buildAndExpand().toUri(), HttpMethod.POST, httpEntity,
					String.class);
			if (response != null && response.hasBody()) {

				GateWayResponse<List<RegReportVO>> paymentsResponse = parseJson(response.getBody(),
						new TypeReference<GateWayResponse<List<RegReportVO>>>() {
						});
				if (paymentsResponse != null) {
					return paymentsResponse.getResult();
				}

			}
		} catch (Exception ex) {
			logger.error("Exception occured while getting DL Revenue Detailed Data [{}]", ex.getMessage());

		}
		return Collections.emptyList();

	}

	@Override
	public Optional<PanVO> getPanDetails(PanDetailsModel pdModel, String panNumber) {

		logger.info(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_ENTRY));

		Optional<PanDTO> resultFromDB = panDao.findByPanNumber(panNumber);
		if (resultFromDB.isPresent()) {
			logger.info(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_SUCCESS), panNumber);
			return panMapper.convertEntity(resultFromDB);
		}

		Map<String, String> uriParams = null;
		PanDetailsModel p1dModel = null;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", panToken);
		headers.add("Accept", "application/json");

		if (StringUtils.isNoneBlank(panNumber)) {
			uriParams = new HashMap<>();
			uriParams.put("panNumber", panNumber);
		} else {
			return Optional.empty();
		}
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(panValidationUrl).queryParam("panNumber",
				panNumber);
		logger.info(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_URL), builder.toUriString());
		HttpEntity<PanDetailsModel> entity = new HttpEntity<>(p1dModel, headers);
		ResponseEntity<String> result = null;
		try {
			result = restTemplate.exchange(builder.buildAndExpand(uriParams).toUri(), HttpMethod.GET, entity,
					String.class);
			logger.info(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_RESULT), result);
			PanResponseVO resultOpt = objectMapper.readValue(result.getBody(), PanResponseVO.class);
			if (null == resultOpt) {
				return Optional.empty();
			}
			if (!resultOpt.getStatus()) {
				throw new BadRequestException(resultOpt.getMessage());
			}
			PanDTO panDTO = panMapper.convertVO(resultOpt.getResult());
			panDTO.setCreatedDate(LocalDateTime.now());
			panDao.save(panDTO);
			return Optional.ofNullable(resultOpt.getResult());

		} catch (HttpClientErrorException httpClientErrorException) {
			logger.debug(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_HTTPCLIENTERROR),
					httpClientErrorException);
			logger.error(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_HTTPCLIENTERROR),
					httpClientErrorException.getMessage());
			return Optional.empty();
		} catch (IOException e) {
			logger.debug(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_PARSEREXCEPTION), e);
			logger.error(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_PARSEREXCEPTION),
					e.getMessage());
			throw new BadRequestException(appMessages.getLogMessage(MessageKeys.PAN_JSONPARSEREXCEPTION));
		}

	}

	@Override
	public Optional<VahanDetailsVO> getVahanDetails(String engineNo, String chasisNo, String userId,
			Boolean isRequiredVahanSync) {

		if (!isRequiredVahanSync) {
			List<VahanDetailsDTO> resultFromDB = vahanDAO.findByChassisNumberOrEngineNumber(chasisNo, engineNo);

			if (!resultFromDB.isEmpty()) {
				resultFromDB = resultFromDB.stream().map(VahanDetailsDTO::setCreatedeDateForObj)
						.collect(Collectors.toList());
				resultFromDB.sort((v1, v2) -> v2.getCreatedeDate().compareTo(v1.getCreatedeDate()));

				for (VahanDetailsDTO vahanDetailsDTO : resultFromDB) {
					if (vahanDetailsDTO.getEngineNumber().equals(engineNo)
							&& vahanDetailsDTO.getChassisNumber().equals(chasisNo)) {
						if (validateClassVehicleType(Optional.of(vahanDetailsDTO), engineNo, chasisNo, userId)
								.isPresent()) {
							logger.info(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_VAHAN_DETAILS),
									engineNo, chasisNo);
							resultFromDB.clear();
							return Optional.of(vahanMapper.convertEntity(vahanDetailsDTO));
						} else {
							logger.error("Dealer is unsupported vehicle class type, Please verify once.");
							throw new BadRequestException(
									"Dealer is unsupported vehicle class type, Please verify once.");
						}

					}
				}
			}
			resultFromDB.clear();
		}
		Map<String, String> uriParams = null;
		logger.info(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_VAHAN_CALL));
		if (StringUtils.isEmpty(engineNo) || StringUtils.isEmpty(chasisNo)) {
			logger.error(appMessages.getLogMessage(MessageKeys.VAHAN_UNAUTHORIZEDREQ));
			throw new BadRequestException(appMessages.getLogMessage(MessageKeys.VAHAN_UNAUTHORIZEDREQ));
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		if (StringUtils.isNoneBlank(engineNo)) {
			uriParams = new HashMap<>();
			uriParams.put("engineNo", engineNo);
			uriParams.put("chasisNo", chasisNo);
		} else {
			return Optional.empty();
		}
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(vahanUrl).queryParam("engineNo", engineNo)
				.queryParam("chasisNo", chasisNo);
		HttpEntity<PanDetailsModel> entity = new HttpEntity<>(headers);
		ResponseEntity<String> result = null;
		try {
			result = restTemplate.exchange(builder.buildAndExpand(uriParams).toUri(), HttpMethod.POST, entity,
					String.class);
		} catch (HttpClientErrorException httpClientErrorException) {

			logger.debug(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_HTTPCLIENTERROR),
					httpClientErrorException);
			logger.error(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_HTTPCLIENTERROR),
					httpClientErrorException.getMessage());
			return Optional.empty();

		} catch (Exception e) {

			logger.debug(appMessages.getLogMessage(MessageKeys.VAHAN_RESULTNOTAVAILABLE), e);
			logger.error(appMessages.getLogMessage(MessageKeys.VAHAN_RESULTNOTAVAILABLE), e.getMessage());

			return Optional.empty();

		}

		if (result == null || StringUtils.isBlank(result.getBody())) {
			return Optional.empty();
		}
		try {
			Optional<VahanResponseVO> resultOpt = Optional
					.ofNullable(objectMapper.readValue(result.getBody(), VahanResponseVO.class));
			if (resultOpt.isPresent()) {
				if (!resultOpt.get().getResult().getResponseType().equals("SUCCESS")) {
					logger.error(resultOpt.get().getResult().getErrorDesc());
					throw new BadRequestException(resultOpt.get().getResult().getErrorDesc());
				}
				VahanDetailsDTO vahanDetailsDTO = vahanMapper.convertVO(resultOpt.get().getResult().getResponseModel());
				/*
				 * if(vahanDetailsDTO.getExShowroomPrice()== null ||
				 * vahanDetailsDTO.getExShowroomPrice() == 0) {
				 * logger.info("Exshowroom price is zero [{}, {}]", engineNo, chasisNo); throw
				 * new BadRequestException(
				 * "Exshowroom price is incorrect.Please update Exshowroom price in vahan details."
				 * + "ExShowroom price is: "+vahanDetailsDTO.getExShowroomPrice()); }
				 */
				vahanDetailsDTO.setCreatedeDate(LocalDateTime.now());
				vahanDAO.save(vahanDetailsDTO);
				Optional<VahanDetailsDTO> validationResult = validateClassVehicleType(Optional.of(vahanDetailsDTO),
						engineNo, chasisNo, userId);
				return Optional.of(vahanMapper.convertEntity(validationResult.get()));
			}
			// return
			// Optional.of(resultOpt.get().getResult().getResponseModel());
		} catch (IOException e) {
			logger.debug("Error while fetching the Engine [{}]/ Chasis No [{}] & Exception is [{}], ", engineNo,
					chasisNo, e);
			logger.error("Error while fetching the Engine [{}]/ Chasis No [{}] & Exception is [{}], ", engineNo,
					chasisNo, e.getMessage());
		}
		return null;
	}
	@Override
	public Optional<VahanDetailsVO> getVahanDetailsUsingEngineNoAndChasisNo(String engineNo, String chasisNo) {

		Map<String, String> uriParams = null;
		logger.info(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_VAHAN_CALL));
		if (StringUtils.isEmpty(engineNo) || StringUtils.isEmpty(chasisNo)) {
			logger.error(appMessages.getLogMessage(MessageKeys.VAHAN_UNAUTHORIZEDREQ));
			throw new BadRequestException(appMessages.getLogMessage(MessageKeys.VAHAN_UNAUTHORIZEDREQ));
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		if (StringUtils.isNoneBlank(engineNo)) {
			uriParams = new HashMap<>();
			uriParams.put("engineNo", engineNo);
			uriParams.put("chasisNo", chasisNo);
		} else {
			return Optional.empty();
		}
		logger.info("before UriComponentsBuilder.fromUriString(vahanUrl).");
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(vahanUrl).queryParam("engineNo", engineNo)
				.queryParam("chasisNo", chasisNo);
		logger.info("after UriComponentsBuilder.fromUriString(vahanUrl).");
		HttpEntity<PanDetailsModel> entity = new HttpEntity<>(headers);
		ResponseEntity<String> result = null;
		try {
			logger.info("before restTemplate.exchange");
			result = restTemplate.exchange(builder.buildAndExpand(uriParams).toUri(), HttpMethod.POST, entity,
					String.class);
			logger.info("after restTemplate.exchange");
		} catch (HttpClientErrorException httpClientErrorException) {

			logger.debug(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_HTTPCLIENTERROR),
					httpClientErrorException);
			logger.error(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_HTTPCLIENTERROR),
					httpClientErrorException.getMessage());
			return Optional.empty();

		} catch (Exception e) {

			logger.debug(appMessages.getLogMessage(MessageKeys.VAHAN_RESULTNOTAVAILABLE), e);
			logger.error(appMessages.getLogMessage(MessageKeys.VAHAN_RESULTNOTAVAILABLE), e.getMessage());

			return Optional.empty();

		}

		if (result == null || StringUtils.isBlank(result.getBody())) {
			return Optional.empty();
		}
		try {
			Optional<VahanResponseVO> resultOpt = Optional
					.ofNullable(objectMapper.readValue(result.getBody(), VahanResponseVO.class));
			if (resultOpt.isPresent()) {
				if (!resultOpt.get().getResult().getResponseType().equals("SUCCESS")) {
					logger.error(resultOpt.get().getResult().getErrorDesc());
					throw new BadRequestException(resultOpt.get().getResult().getErrorDesc());
				}
				VahanDetailsDTO vahanDetailsDTO = vahanMapper.convertVO(resultOpt.get().getResult().getResponseModel());
				/*
				 * if(vahanDetailsDTO.getExShowroomPrice()== null ||
				 * vahanDetailsDTO.getExShowroomPrice() == 0) {
				 * logger.info("Exshowroom price is zero [{}, {}]", engineNo, chasisNo); throw
				 * new BadRequestException(
				 * "Exshowroom price is incorrect.Please update Exshowroom price in vahan details."
				 * + "ExShowroom price is: "+vahanDetailsDTO.getExShowroomPrice()); }
				 */
				vahanDetailsDTO.setCreatedeDate(LocalDateTime.now());
				Optional<VahanDetailsDTO> validationResult = Optional.of(vahanDetailsDTO);
				vahanDAO.save(vahanDetailsDTO);
				return Optional.of(vahanMapper.convertEntity(validationResult.get()));
			}
			// return
			// Optional.of(resultOpt.get().getResult().getResponseModel());
		} catch (IOException e) {
			logger.debug("Error while fetching the Engine [{}]/ Chasis No [{}] & Exception is [{}], ", engineNo,
					chasisNo, e);
			logger.error("Error while fetching the Engine [{}]/ Chasis No [{}] & Exception is [{}], ", engineNo,
					chasisNo, e.getMessage());
		}
		return null;
	}

	@Override
	public Optional<ResponseListEntity> getSecondVehiclesList(String applicationNo) {

		OwnerDetailsVO ownerDetailsVO = new OwnerDetailsVO();

		Optional<StagingRegistrationDetailsDTO> registrationDetailsDTO = stagingRegistrationDetailsDAO
				.findByApplicationNo(applicationNo);

		if (!registrationDetailsDTO.isPresent()) {
			return Optional.empty();
		}

		if (registrationDetailsDTO.get().getApplicantDetails().getAadharResponse() != null) {
			String displayName = registrationDetailsDTO.get().getApplicantDetails().getAadharResponse().getName();

			Map<String, String> lastFirstName = nameSplit(displayName);

			ownerDetailsVO.setFirstName(lastFirstName.get("firstName"));
			ownerDetailsVO
					.setFatherName(registrationDetailsDTO.get().getApplicantDetails().getAadharResponse().getCo());
			if (StringUtils.isBlank(registrationDetailsDTO.get().getApplicantDetails().getAadharResponse().getCo())) {
				ownerDetailsVO.setFatherName(registrationDetailsDTO.get().getApplicantDetails().getFatherName());
			}
			ownerDetailsVO.setLastName(lastFirstName.get("lastName"));
			String dobAadhaar = registrationDetailsDTO.get().getApplicantDetails().getAadharResponse().getDob();
			ownerDetailsVO.setDob(registrationDetailsDTO.get().getApplicantDetails().getAadharResponse().getDob());
			if (dobAadhaar.length() <= 4) {
				if (registrationDetailsDTO.get().getApplicantDetails().getDateOfBirth() != null) {
					ownerDetailsVO.setDob(DateConverters.convertLocalDateFormat(
							registrationDetailsDTO.get().getApplicantDetails().getDateOfBirth()));
				}
			}

		} else {
			// srinivas and murthy sir said that if data not available send null
			// as a string
			String displayName = DateConverters
					.replaceDefaults(registrationDetailsDTO.get().getApplicantDetails().getDisplayName());

			Map<String, String> lastFirstName = nameSplit(displayName);
			ownerDetailsVO.setFirstName(lastFirstName.get("firstName"));
			ownerDetailsVO.setLastName(lastFirstName.get("lastName"));
			ownerDetailsVO.setFatherName(
					DateConverters.replaceDefaults(registrationDetailsDTO.get().getApplicantDetails().getFatherName()));
			ownerDetailsVO.setDob("null");
			if (registrationDetailsDTO.get().getApplicantDetails().getDateOfBirth() != null) {
				ownerDetailsVO.setDob(DateConverters
						.convertLocalDateFormat(registrationDetailsDTO.get().getApplicantDetails().getDateOfBirth()));
			}
		}

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<OwnerDetailsVO> httpEntity = new HttpEntity<>(ownerDetailsVO, headers);
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(secondVehicleSearchUrl, HttpMethod.POST, httpEntity, String.class);
		} catch (HttpClientErrorException httpClientErrorException) {

			logger.debug(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_HTTPCLIENTERROR),
					httpClientErrorException);
			logger.error(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_HTTPCLIENTERROR),
					httpClientErrorException.getMessage());
			return Optional.empty();

		} catch (Exception e) {
			logger.debug(appMessages.getLogMessage(MessageKeys.SVS_RESULTNOTAVAILABLE), e);
			logger.error(appMessages.getLogMessage(MessageKeys.SVS_RESULTNOTAVAILABLE), e.getMessage());

			return Optional.empty();
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			ResponseListEntity secondVehicleSearchVO = mapper.readValue(response.getBody(), ResponseListEntity.class);
			return Optional.of(secondVehicleSearchVO);
		} catch (Exception e) {
			logger.debug(appMessages.getLogMessage(MessageKeys.SVS_RESULTNOTAVAILABLE), e);
			logger.error(appMessages.getLogMessage(MessageKeys.SVS_RESULTNOTAVAILABLE), e.getMessage());

			return Optional.empty();
		}
	}

	public <T> Optional<T> readValue(String value, Class<T> valueType) {

		try {
			return Optional.of(objectMapper.readValue(value, valueType));
		} catch (IOException ioe) {
			logger.debug(appMessages.getLogMessage(MessageKeys.READVALUE_STRINGTOOBJECT), ioe);
			logger.error(appMessages.getLogMessage(MessageKeys.READVALUE_STRINGTOOBJECT), ioe.getMessage());

		}

		return Optional.empty();
	}

	private <T> T parseJson(String value, TypeReference<T> valueTypeRef) {
		try {
			return objectMapper.readValue(value, valueTypeRef);
		} catch (IOException ioe) {
			logger.debug(appMessages.getLogMessage(MessageKeys.PARSEJSON_JSONTOOBJECT), ioe);
			logger.error(appMessages.getLogMessage(MessageKeys.PARSEJSON_JSONTOOBJECT), ioe.getMessage());

		}
		return null;
	}

	@Override
	public Optional<HSRPResposeVO> callAPIPost(String apiPath, HSRPRequestModel sendData, String contentType)
			throws RuntimeException {

		ResponseEntity<String> responseEntity = null;
		// sendData.setAuthorizationRefNo();TO-DO
		HttpHeaders headers = new HttpHeaders();
		headers.set("Content-Type", contentType);
		try {
			HttpEntity<HSRPRequestModel> entity = new HttpEntity<HSRPRequestModel>(sendData, headers);
			long startTimeInMilli = System.currentTimeMillis();
			logger.info("Time before API call  {}", startTimeInMilli);
			responseEntity = restTemplate.exchange(apiPath, HttpMethod.POST, entity, String.class);
			logger.info("Time taken for API call {}",
					+(System.currentTimeMillis() - startTimeInMilli) + " milliseconds");
			if (responseEntity.getStatusCode() == HttpStatus.OK) {
				String response = responseEntity.getBody();
				ObjectMapper objectMapper = new ObjectMapper();
				JsonNode node = objectMapper.readValue(response, JsonNode.class);
				JsonNode object = node.get(0);
				HSRPResposeVO hsrpResposeVO = new HSRPResposeVO();
				hsrpResposeVO.setStatus(object.get("Status").asInt());
				hsrpResposeVO.setMessage(object.get("Message").textValue());
				return Optional.of(hsrpResposeVO);
			}
		} catch (Exception e) {
			logger.error("Exception while hsrp data posting : {}", e);
		}
		return Optional.empty();
	}

	@Override
	public Optional<EngineChassisNOEntity> validateEngineChassisNo(EngineChassisNoVO engineChassisNoVO) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<EngineChassisNoVO> httpEntity = new HttpEntity<>(engineChassisNoVO, headers);
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(engineChassisNoUrl, HttpMethod.POST, httpEntity, String.class);

		} catch (HttpClientErrorException httpClientErrorException) {

			logger.debug(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_HTTPCLIENTERROR),
					httpClientErrorException);
			logger.error(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_HTTPCLIENTERROR),
					httpClientErrorException.getMessage());
			return Optional.empty();

		} catch (Exception e) {
			logger.debug(appMessages.getLogMessage(MessageKeys.SVS_RESULTNOTAVAILABLE), e);
			logger.error(appMessages.getLogMessage(MessageKeys.SVS_RESULTNOTAVAILABLE), e.getMessage());

			return Optional.empty();
		}
		try {
			ObjectMapper mapper = new ObjectMapper();
			EngineChassisNOEntity engineChassisNOEntity = mapper.readValue(response.getBody(),
					EngineChassisNOEntity.class);
			return Optional.of(engineChassisNOEntity);
		} catch (Exception e) {
			logger.debug(appMessages.getLogMessage(MessageKeys.SVS_RESULTNOTAVAILABLE), e);
			logger.error(appMessages.getLogMessage(MessageKeys.SVS_RESULTNOTAVAILABLE), e.getMessage());

			return Optional.empty();
		}
	}

	@Override
	public boolean validateVahanDetailsInStagingRegistrationDetails(String engineNo, String chasisNo) {
		Optional<StagingRegistrationDetailsDTO> stagingRegistrationDetailsDTO = stagingRegistrationDetailsDAO
				.findByVahanDetailsEngineNumberOrVahanDetailsChassisNumber(engineNo, chasisNo);
		return stagingRegistrationDetailsDTO.isPresent();
	}

	private void validateDealerVehicleTypeAndVahanVehicleType(
			List<DealerAndVahanMappedCovDTO> dealerAndVahanMappedCovDTOList, VahanDetailsDTO vahanDetailsDetails) {
		if (null == vahanDetailsDetails.getDealerCovType()) {
			vahanDetailsDetails.setDealerCovType(new ArrayList<String>());
		}
		dealerAndVahanMappedCovDTOList.stream().forEach(value -> {
			if (value.getVahanCovType().equalsIgnoreCase(vahanDetailsDetails.getVehicleClass())) {
				vahanDetailsDetails.getDealerCovType().add(value.getDealerCovType());
			}
		});
	}

	public Optional<VahanDetailsDTO> validateClassVehicleType(Optional<VahanDetailsDTO> vahanDetailsOpt,
			String engineNo, String chasisNo, String userId) {
		// Optional<VahanDetailsDTO> resultFromDB =
		// vahanDAO.findByEngineNumberOrChassisNumber(engineNo, chasisNo);

		Optional<UserDTO> userDetails = userDAO.findByUserId(userId);
		if (userDetails.isPresent()) {
			List<DealerCovDTO> dealerCovDTOList = dealerCovDAO.findByRId(userDetails.get().getRid());
			List<String> dealerCovTypeList = new ArrayList<>();

			dealerCovDTOList.forEach(dealerCovType -> {
				dealerCovTypeList.add(dealerCovType.getCovId());
			});

			List<DealerAndVahanMappedCovDTO> dealerAndVahanMappedCovDTOList = dealerAndVahanMappedCovDAO
					.findByDealerCovTypeIn(dealerCovTypeList);
			validateDealerVehicleTypeAndVahanVehicleType(dealerAndVahanMappedCovDTOList, vahanDetailsOpt.get());
			if (null != vahanDetailsOpt.get().getDealerCovType()) {
				logger.info("Validated Vahan Class Type [{}].", vahanDetailsOpt.get().getVehicleClass());
				return vahanDetailsOpt;
			} else {
				logger.error("Dealer is unsupported vehicle class type, Please verify once.");
				throw new BadRequestException("Dealer is unsupported vehicle class type, Please verify once.");
			}
		}
		logger.info("Vahan Details :[{}]", vahanDetailsOpt.get());
		return vahanDetailsOpt;
	}

	@Override
	public Optional<List<PaymentDetails>> getRegistrationPayments(LocalDate fromDate, LocalDate toDate,
			String offiCode) {

		List<LocalDateTime> localDateTimeList = DateConverters.convertDateToLocalDateTime(fromDate, toDate);
		String status = PayStatusEnum.SUCCESS.getDescription();
		Optional<List<PaymentTransactionDTO>> paymentTransactionList = paymentTransactionDAO
				.findByResponseResponseTimeBetweenAndPayStatus(localDateTimeList.get(0), localDateTimeList.get(1),
						status);
		List<PaymentDetails> paymentDetails = new ArrayList<>();
		if (!StringUtils.isBlank(offiCode)) {
			if (paymentTransactionList.isPresent()) {

				for (PaymentTransactionDTO paymentTransactionDTO : paymentTransactionList.get()) {
					String serviceType = "excelReport";
					PaymentDetails payments = getFeeDetails(paymentTransactionDTO, serviceType);
					if (payments != null && payments.getOfficeCode() != null
							&& payments.getOfficeCode().equalsIgnoreCase(offiCode)) {
						paymentDetails.add(payments);
					}

				}
				HashSet<Object> seen = new HashSet<>();
				paymentDetails.removeIf(e -> !seen.add(e.getTransactionNo()));

				return Optional.of(paymentDetails);
			}

		} else {
			if (paymentTransactionList.isPresent()) {

				for (PaymentTransactionDTO paymentTransactionDTO : paymentTransactionList.get()) {
					Boolean cfstStatus = paymentTransactionDTO.isCfstSync();
					if (cfstStatus == null || !cfstStatus) {
						String serviceType = "revenueReport";
						PaymentDetails payments = getFeeDetails(paymentTransactionDTO, serviceType);
						if (payments != null) {
							paymentDetails.add(payments);
						}
					}
				}
				HashSet<Object> seen = new HashSet<>();
				paymentDetails.removeIf(e -> !seen.add(e.getTransactionNo()));

				return Optional.of(paymentDetails);

			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<VahanDetailsVO> getVahanDetails(String engineNo, String chasisNo) {

		Map<String, String> uriParams = null;
		logger.info(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_VAHAN_CALL));
		if (StringUtils.isEmpty(engineNo) || StringUtils.isEmpty(chasisNo)) {
			throw new BadRequestException(appMessages.getLogMessage(MessageKeys.VAHAN_UNAUTHORIZEDREQ));
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		if (StringUtils.isNoneBlank(engineNo)) {
			uriParams = new HashMap<>();
			uriParams.put("engineNo", engineNo);
			uriParams.put("chasisNo", chasisNo);
		} else {
			return Optional.empty();
		}
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(vahanUrl).queryParam("engineNo", engineNo)
				.queryParam("chasisNo", chasisNo);
		HttpEntity<PanDetailsModel> entity = new HttpEntity<>(headers);
		ResponseEntity<String> result = null;
		try {
			result = restTemplate.exchange(builder.buildAndExpand(uriParams).toUri(), HttpMethod.POST, entity,
					String.class);
		} catch (HttpClientErrorException httpClientErrorException) {

			logger.debug(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_HTTPCLIENTERROR),
					httpClientErrorException);
			logger.error(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_HTTPCLIENTERROR),
					httpClientErrorException.getMessage());
			return Optional.empty();

		} catch (Exception e) {

			logger.info(appMessages.getLogMessage(MessageKeys.VAHAN_RESULTNOTAVAILABLE), e.getMessage());
			return Optional.empty();

		}

		if (result == null || StringUtils.isBlank(result.getBody())) {
			return Optional.empty();
		}
		try {
			Optional<VahanResponseVO> resultOpt = Optional
					.ofNullable(objectMapper.readValue(result.getBody(), VahanResponseVO.class));
			if (resultOpt.isPresent()) {
				if (!resultOpt.get().getResult().getResponseType().equals("SUCCESS")) {
					throw new BadRequestException(resultOpt.get().getResult().getErrorDesc());
				}
				VahanDetailsDTO vahanDetailsDTO = vahanMapper.convertVO(resultOpt.get().getResult().getResponseModel());
				vahanDAO.save(vahanDetailsDTO);
				return Optional.of(vahanMapper.convertEntity(vahanDetailsDTO));
			}

		} catch (IOException e) {
			logger.error("Error while fetching the Engine [{}]/ Chasis No [{}] & Exception is [{}], ", engineNo,
					chasisNo, e);
		}
		return null;
	}

	@Override
	public Boolean saveCfstPaymentStatus(String applcationNo, String status) {
		// TODO Auto-generated method stub
		Boolean updateStatus = false;
		if (status.equalsIgnoreCase(CfstStatusTypes.Y.getCode())) {
			updateStatus = true;
		}
		String payStatus = PayStatusEnum.SUCCESS.getDescription();
		List<PaymentTransactionDTO> paymentTransactionDTOlist = paymentTransactionDAO
				.findByPayStatusAndApplicationFormRefNum(payStatus, applcationNo);
		if (!paymentTransactionDTOlist.isEmpty()) {
			for (PaymentTransactionDTO paymentTransaction : paymentTransactionDTOlist) {
				Boolean cfstStatus = paymentTransaction.isCfstSync();
				if (cfstStatus == null || !cfstStatus) {
					paymentTransaction.setCfstSync(updateStatus);
					logMovingService.movePaymnetsToLog(paymentTransaction.getApplicationFormRefNum());
					paymentTransactionDAO.save(paymentTransaction);
				}
			}
			return true;
		}
		return false;
	}

	public String officeCodeValidation(String officeCode, String refNumber) {

		if (officeCode != null) {
			if (officeCode.equalsIgnoreCase("OTHER")) {

				Optional<RegistrationDetailsDTO> registrationDTO = registrationDetailDAO.findByApplicationNo(refNumber);
				if (registrationDTO.isPresent()) {

					Optional<UserDTO> userDTO = userDAO
							.findByUserId(registrationDTO.get().getDealerDetails().getDealerId());
					if (userDTO.isPresent()) {
						return userDTO.get().getOffice().getOfficeCode();
					}
				}
			}
		}
		return officeCode;

	}

	private Map<String, String> nameSplit(String displayName) {

		String name = StringUtils.EMPTY;
		Map<String, String> nameMap = new HashMap<String, String>();
		nameMap.put("lastName", StringUtils.EMPTY);
		nameMap.put("firstName", StringUtils.EMPTY);
		if (!displayName.isEmpty() || displayName != null) {
			StringTokenizer tokens = new StringTokenizer(displayName, " ");
			String[] splited = new String[tokens.countTokens()];
			int index = 0;
			for (; index <= splited.length - 1; index++) {
				splited[index] = tokens.nextToken();
				if (index == 0) {
					nameMap.put("lastName", splited[0]);
				} else {
					name = name + splited[index] + " ";
					nameMap.put("firstName", name);
				}

			}
		}
		return nameMap;

	}

	@Override
	public Boolean paymentDetailsExcelReport(HttpServletResponse response, String officeCode, LocalDate fromDate,
			LocalDate toDate) {
		Optional<List<PaymentDetails>> paymentList = restGateWayService.getRegistrationPayments(fromDate, toDate,
				officeCode);
		Collections.sort(paymentList.get(), new Comparator<PaymentDetails>() {
			@Override
			public int compare(PaymentDetails p1, PaymentDetails p2) {
				return p1.getTransactionDate().compareTo(p2.getTransactionDate());
			}
		});
		if (paymentList.get().isEmpty()) {
			return false;
		}
		ExcelService excel = new ExcelServiceImpl();

		List<String> header = new ArrayList<String>();

		excel.setHeaders(header, "regPayments");

		Random rand = new Random();
		int ranNo = rand.nextInt(1000);

		String name = "Payments_Reg_" + officeCode + "_" + ranNo;
		String fileName = name + ".xlsx";
		String sheetName = "PaymentDetails";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		response.setHeader("Pragma", "public");
		response.setHeader("Cache-Control", "no-store");
		response.addHeader("Cache-Control", "max-age=0");

		XSSFWorkbook wb = excel.renderData(prepareCellProps(header, paymentList.get()), header, fileName, sheetName);

		try {
			ServletOutputStream outputStream = null;
			outputStream = response.getOutputStream();
			wb.write(outputStream);
			outputStream.flush();
			outputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;

	}

	private List<List<CellProps>> prepareCellProps(List<String> header, List<PaymentDetails> paymentList) {

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();
		List<CellProps> result = new ArrayList<CellProps>();
		for (PaymentDetails payments : paymentList) {
			for (int i = 0; i < header.size(); i++) {
				CellProps cellpro = new CellProps();
				switch (i) {
				case 0:
					cellpro.setFieldValue(payments.getTransactionDate());
					break;
				case 1:
					cellpro.setFieldValue(DateConverters.replaceDefaults(payments.getOfficeCode()));
					break;
				case 2:
					cellpro.setFieldValue(DateConverters.replaceDefaults(payments.getReferanceNo()));
					break;
				case 3:
					cellpro.setFieldValue(DateConverters.replaceDefaults(payments.getTransactionNo()));
					break;
				case 4:
					String serviceNames = "";
					cellpro.setFieldValue(serviceNames);
					Set<Integer> ids = payments.getServiceNames();
					if (!CollectionUtils.isEmpty(ids)) {
						Iterator<Integer> iterator = ids.iterator();
						while (iterator.hasNext()) {
							serviceNames = serviceNames + ""
									+ ServiceEnum.getServiceEnumById(iterator.next()).toString() + ",";
						}
						cellpro.setFieldValue(DateConverters.replaceDefaults(removeLastChar(serviceNames)));
					}
					break;
				case 5:
					cellpro.setFieldValue(DateConverters.replaceDefaults(payments.getTrApplicationFee()));
					break;
				case 6:
					cellpro.setFieldValue(DateConverters.replaceDefaults(payments.getTrServiceFee()));
					break;
				case 7:
					cellpro.setFieldValue(DateConverters.replaceDefaults(payments.getPrApplicationFee()));
					break;
				case 8:
					cellpro.setFieldValue(DateConverters.replaceDefaults(payments.getPrServiceFee()));
					break;
				case 9:
					cellpro.setFieldValue(DateConverters.replaceDefaults(payments.getPrPostalFee()));
					break;
				case 10:
					cellpro.setFieldValue(DateConverters.replaceDefaults(payments.getPrCardFee()));
					break;
				case 11:
					cellpro.setFieldValue(DateConverters.replaceDefaults(payments.getHpaApplicationFee()));
					break;
				case 12:
					cellpro.setFieldValue(DateConverters.replaceDefaults(payments.getTaxType()));
					break;
				case 13:
					cellpro.setFieldValue(DateConverters.replaceDefaults(payments.getTaxAmount()));
					break;
				case 14:
					cellpro.setFieldValue(DateConverters.replaceDefaults(payments.getCessFee()));
					break;
				case 15:
					cellpro.setFieldValue(DateConverters.replaceDefaults(payments.getBidAmount()));
					break;
				case 16:
					cellpro.setFieldValue(DateConverters.replaceDefaults(payments.getFcApplicationFee()));
					break;
				case 17:
					cellpro.setFieldValue(DateConverters.replaceDefaults(payments.getFcServiceFee()));
					break;
				case 18:
					cellpro.setFieldValue(DateConverters.replaceDefaults(payments.getHsrpFee()));
					break;
				default:
					break;
				}
				result.add(cellpro);
			}
		}
		cell.add(result);
		return cell;

	}

	@Override
	public Optional<ElasticSecondVehicleResponseVO> searchSecondVehicleDocs(
			ElasticSecondVehicleSearchVO elasticSecondVehicleSearchVO) {

		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<ElasticSecondVehicleSearchVO> httpEntity = new HttpEntity<>(elasticSecondVehicleSearchVO, headers);
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(elasticSearchUrl, HttpMethod.POST, httpEntity, String.class);
			logger.info("elastic Search url [{}]", elasticSearchUrl);
			if (StringUtils.isBlank(response.getBody())) {
				logger.error("invalid request or response is not available from elastic");
				throw new BadRequestException("invalid request or response is not available from elastic ");
			}
		} catch (HttpClientErrorException httpClientErrorException) {

			logger.debug(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_HTTPCLIENTERROR),
					httpClientErrorException);
			logger.error(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_HTTPCLIENTERROR),
					httpClientErrorException.getMessage());
			return Optional.empty();

		} catch (Exception e) {
			logger.debug("exception [{}]", e);
			logger.error("exception [{}]", e.getMessage());

			return Optional.empty();
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			ElasticSecondVehicleResponseVO secondVehicleSearchVO = mapper.readValue(response.getBody(),
					ElasticSecondVehicleResponseVO.class);
			return Optional.of(secondVehicleSearchVO);
		} catch (Exception e) {
			logger.debug(appMessages.getLogMessage(MessageKeys.SVS_RESULTNOTAVAILABLE), e);
			logger.error(appMessages.getLogMessage(MessageKeys.SVS_RESULTNOTAVAILABLE), e.getMessage());

			return Optional.empty();
		}

	}

	private PaymentDetails getFeeDetails(PaymentTransactionDTO paymentTransactionDTO, String serviceType) {
		Boolean paymentToShow = false;

		PaymentDetails payments = new PaymentDetails();
		String refNumber = paymentTransactionDTO.getApplicationFormRefNum();
		String officeCode = paymentTransactionDTO.getOfficeCode();

		payments.setReferanceNo(refNumber);
		payments.setOfficeCode(officeCode);

		// office code is OTHER we need to display dealer office code said by
		// murthy(there is no Enum for OTHER)
		if (paymentTransactionDTO.getOfficeCode() != null && "OTHER".equalsIgnoreCase(payments.getOfficeCode())) {
			payments.setOfficeCode(officeCodeValidation(officeCode, refNumber));
		}
		payments.setTransactionNo(paymentTransactionDTO.getTransactioNo());

		if (paymentTransactionDTO.getResponse().getResponseTime() != null) {
			LocalDate date = paymentTransactionDTO.getResponse().getResponseTime().toLocalDate();
			payments.setTransactionDate(DateConverters.convertLocalDateFormat(date));
		}
		if (serviceType.equalsIgnoreCase("excelReport")) {
			payments.setServiceNames(paymentTransactionDTO.getServiceIds());
		}
		payments.setServiceId(
				paymentTransactionDTO.getServiceIds().stream().map(Object::toString).collect(Collectors.joining(",")));
		if (payments.getServiceId().equalsIgnoreCase(ServiceEnum.SPNR.getId().toString())) {
			Optional<SpecialNumberDetailsDTO> specialNumberDetails = specialNumberDetailsDAO
					.findBySpecialNumberAppId(payments.getReferanceNo());
			if (specialNumberDetails.isPresent()) {
				BidStatus bidStatus = specialNumberDetails.get().getBidStatus();
				if (bidStatus.equals(BidStatus.BIDWIN) || bidStatus.equals(BidStatus.BIDABSENT)) {
					if (specialNumberDetails.get().getSpecialNumberFeeDetails().getServicesAmount() != null) {
						payments.setPrServiceFee(
								specialNumberDetails.get().getSpecialNumberFeeDetails().getServicesAmount());
					}

					if (specialNumberDetails.get().getVehicleDetails().getRtaOffice().getOfficeCode() != null) {
						payments.setOfficeCode(
								specialNumberDetails.get().getVehicleDetails().getRtaOffice().getOfficeCode());
					}
					payments.setPrApplicationFee(
							specialNumberDetails.get().getSpecialNumberFeeDetails().getApplicationAmount());
					if (!BidStatus.BIDABSENT.equals(bidStatus)
							&& specialNumberDetails.get().getBidFinalDetails() != null) {
						payments.setBidAmount(specialNumberDetails.get().getBidFinalDetails().getBidAmount());
					}
				} else {
					if (specialNumberDetails.get().getSpecialNumberFeeDetails().getServicesAmount() != null) {
						payments.setPrServiceFee(
								specialNumberDetails.get().getSpecialNumberFeeDetails().getServicesAmount());
					}
					if (specialNumberDetails.get().getVehicleDetails().getRtaOffice().getOfficeCode() != null) {
						payments.setOfficeCode(
								specialNumberDetails.get().getVehicleDetails().getRtaOffice().getOfficeCode());

						if (paymentTransactionDTO.getOfficeCode() != null) {
							if (payments.getOfficeCode().equalsIgnoreCase("OTHER")) {
								payments.setOfficeCode(officeCodeValidation(officeCode, refNumber));
							}
						}
					}
				}
				if (bidStatus.equals(BidStatus.FINALPAYMENTFAILURE) || bidStatus.equals(BidStatus.FINALPAYMENTPENDING)
						|| bidStatus.equals(BidStatus.SPPAYMENTFAILURE)
						|| bidStatus.equals(BidStatus.SPPAYMENTPENDING)) {
					paymentToShow = true;
				}
			}
		}
		if (paymentTransactionDTO.getBreakPaymentsSave() != null) {
			BreakPaymentsSaveDTO breakPaymentsSaveDTO = paymentTransactionDTO.getBreakPaymentsSave();
			List<BreakPayments> breakPaymentsList = breakPaymentsSaveDTO.getBreakPayments();

			for (BreakPayments breakPayments : breakPaymentsList) {
				Map<String, Double> breakUp = null;
				// Temporary Registration
				if (breakPayments.getFeeType().equalsIgnoreCase(FeeTypeDetails.REGVALUE)) {
					breakUp = breakPayments.getBreakup();
					for (Map.Entry<String, Double> entry : breakUp.entrySet()) {
						if (entry.getKey().equalsIgnoreCase(ServiceCodeEnum.SERVICE_FEE.getTypeDesc())) {
							payments.setTrServiceFee(entry.getValue());
						}
						if (entry.getKey().equalsIgnoreCase(ServiceCodeEnum.REGISTRATION.getTypeDesc())) {
							payments.setTrApplicationFee(entry.getValue());
						}
					}
				}
				// Fc Fees
				if (breakPayments.getFeeType().equalsIgnoreCase(FeeTypeDetails.NEW)) {
					breakUp = breakPayments.getBreakup();
					for (Map.Entry<String, Double> entry : breakUp.entrySet()) {
						if (entry.getKey().equalsIgnoreCase(ServiceCodeEnum.SERVICE_FEE.getTypeDesc())) {
							payments.setFcServiceFee(entry.getValue());
						}
						if (entry.getKey().equalsIgnoreCase(ServiceCodeEnum.REGISTRATION.getTypeDesc())) {
							payments.setFcApplicationFee(entry.getValue());
						}
					}
				}
				// PR Registration
				if (breakPayments.getFeeType().equalsIgnoreCase(FeeTypeDetails.FRESH)) {
					breakUp = breakPayments.getBreakup();
					for (Map.Entry<String, Double> entry : breakUp.entrySet()) {
						if (entry.getKey().equalsIgnoreCase(ServiceCodeEnum.SERVICE_FEE.getTypeDesc())) {
							payments.setPrServiceFee(entry.getValue());
						}
						if (entry.getKey().equalsIgnoreCase(ServiceCodeEnum.REGISTRATION.getTypeDesc())) {
							payments.setPrApplicationFee(entry.getValue());
						}
						if (entry.getKey().equalsIgnoreCase(ServiceCodeEnum.POSTAL_FEE.getTypeDesc())) {
							payments.setPrPostalFee(entry.getValue());
						}
						if (entry.getKey().equalsIgnoreCase(ServiceCodeEnum.CARD.getTypeDesc())) {
							payments.setPrCardFee(entry.getValue());
						}
						if (serviceType.equalsIgnoreCase("excelReport")) {
							if (entry.getKey().equalsIgnoreCase(ServiceCodeEnum.HSRP_FEE.getTypeDesc())) {
								payments.setHsrpFee(entry.getValue());
							}
						}

					}
				}

				// TAX Fees
				if (breakPayments.getFeeType().equalsIgnoreCase(ServiceCodeEnum.LIFE_TAX.getCode())) {
					payments.setTaxType(breakPayments.getFeeType());
					breakUp = breakPayments.getBreakup();
					for (Map.Entry<String, Double> entry : breakUp.entrySet()) {
						if (entry.getKey().equalsIgnoreCase(ServiceCodeEnum.LIFE_TAX.getCode())) {
							payments.setTaxAmount(entry.getValue());
						}

					}
				}
				if (breakPayments.getFeeType().equalsIgnoreCase(ServiceCodeEnum.QLY_TAX.getCode())) {
					payments.setTaxType(breakPayments.getFeeType());
					breakUp = breakPayments.getBreakup();
					for (Map.Entry<String, Double> entry : breakUp.entrySet()) {
						if (entry.getKey().equalsIgnoreCase(ServiceCodeEnum.QLY_TAX.getCode())) {
							payments.setTaxAmount(entry.getValue());
						}

					}
				}
				if (breakPayments.getFeeType().equalsIgnoreCase(ServiceCodeEnum.HALF_TAX.getCode())) {
					payments.setTaxType(breakPayments.getFeeType());
					breakUp = breakPayments.getBreakup();
					for (Map.Entry<String, Double> entry : breakUp.entrySet()) {
						if (entry.getKey().equalsIgnoreCase(ServiceCodeEnum.HALF_TAX.getCode())) {
							payments.setTaxAmount(entry.getValue());
						}

					}
				}
				if (breakPayments.getFeeType().equalsIgnoreCase(ServiceCodeEnum.YEAR_TAX.getCode())) {
					payments.setTaxType(breakPayments.getFeeType());
					breakUp = breakPayments.getBreakup();
					for (Map.Entry<String, Double> entry : breakUp.entrySet()) {
						if (entry.getKey().equalsIgnoreCase(ServiceCodeEnum.YEAR_TAX.getCode())) {
							payments.setTaxAmount(entry.getValue());
						}

					}
				}

				// HPA Fee
				if (breakPayments.getFeeType().equalsIgnoreCase(FeeTypeDetails.HPAFEE)) {

					breakUp = breakPayments.getBreakup();
					for (Map.Entry<String, Double> entry : breakUp.entrySet()) {
						if (entry.getKey().equalsIgnoreCase(ServiceCodeEnum.REGISTRATION.getTypeDesc())) {
							payments.setHpaApplicationFee(entry.getValue());
						}

					}
				}

				// Cess fee

				if (breakPayments.getFeeType().equalsIgnoreCase(ServiceCodeEnum.CESS_FEE.getCode())
						|| breakPayments.getFeeType().equalsIgnoreCase(ServiceCodeEnum.CESS_FEE.getTypeDesc())) {

					breakUp = breakPayments.getBreakup();
					for (Map.Entry<String, Double> entry : breakUp.entrySet()) {
						if (entry.getKey().equalsIgnoreCase(ServiceCodeEnum.CESS_FEE.getCode())) {
							payments.setCessFee(entry.getValue());
						}

					}
				}

			}
		}
		if (paymentTransactionDTO.getFeeDetailsDTO() != null) {
			List<FeesDTO> feeDetailsDTO = paymentTransactionDTO.getFeeDetailsDTO().getFeeDetails();
			Double tax = 0.0;
			Double appFee = 0.0;
			Double serviceFee = 0.0;

			for (FeesDTO feesDTO : feeDetailsDTO) {
				if (feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.SERVICE_FEE.getTypeDesc())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.FITNESS_SERVICE_FEE.getTypeDesc())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.FITNESS_SERVICE_FEE.getCode())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.TAXSERVICEFEE.getCode())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.TAXSERVICEFEE.getTypeDesc())) {
					// payments.setPrServiceFee(feesDTO.getAmount());
					serviceFee = serviceFee + feesDTO.getAmount();
				}
				if (feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.REGISTRATION.getTypeDesc())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.FITNESS_FEE.getTypeDesc())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.FITNESS_FEE.getCode())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.FC_LATE_FEE.getTypeDesc())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.FC_LATE_FEE.getCode()) ||

						feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.LATE_FEE.getTypeDesc())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.LATE_FEE.getCode())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.GREEN_TAX.getTypeDesc())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.GREEN_TAX.getCode())) {
					// payments.setPrApplicationFee(feesDTO.getAmount());
					appFee = appFee + feesDTO.getAmount();
				}
				if (feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.AUTHORIZATION.getTypeDesc())) {
					payments.setAuthorizationFee(feesDTO.getAmount());
				}
				if (feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.POSTAL_FEE.getTypeDesc())) {
					payments.setPrPostalFee(feesDTO.getAmount());
				}
				if (feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.CARD.getTypeDesc())) {
					payments.setPrCardFee(feesDTO.getAmount());
				}
				if (feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.LIFE_TAX.getCode())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.LIFE_TAX.getTypeDesc())) {
					payments.setTaxType(feesDTO.getFeesType());
					// payments.setTaxAmount(feesDTO.getAmount());
					tax = tax + feesDTO.getAmount();
				}

				if (feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.QLY_TAX.getCode())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.QLY_TAX.getTypeDesc())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.HALF_TAX.getCode())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.HALF_TAX.getTypeDesc())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.YEAR_TAX.getCode())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.YEAR_TAX.getTypeDesc())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.TAXARREARS.getCode())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.TAXARREARS.getTypeDesc())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.PENALTY.getCode())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.PENALTY.getTypeDesc())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.PENALTYARREARS.getCode())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.PENALTYARREARS.getTypeDesc())) {
					payments.setTaxType(feesDTO.getFeesType());
					// payments.setTaxAmount(feesDTO.getAmount());
					tax = tax + feesDTO.getAmount();
				}
				if (feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.CESS_FEE.getCode())
						|| feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.CESS_FEE.getTypeDesc())) {
					payments.setCessFee(feesDTO.getAmount());
				}
				if (tax > 0) {
					payments.setTaxAmount(tax);
				}
				if (serviceFee > 0) {
					payments.setPrServiceFee(serviceFee);
				}
				if (appFee > 0) {
					payments.setPrApplicationFee(appFee);
				}
				if ((payments.getTaxType() == null || payments.getTaxType().isEmpty())
						&& (paymentTransactionDTO.getModuleCode() != null
								&& paymentTransactionDTO.getModuleCode().equalsIgnoreCase("CITIZEN"))) {
					payments.setTaxType("CITIZEN");
				}
			}
		}
		if (!ServiceEnum.SPNB.getId().toString().equalsIgnoreCase(payments.getServiceId()) && !paymentToShow) {
			return payments;
		}
		return null;
	}

	public String removeLastChar(String s) {
		if (s == null || s.length() == 0) {
			return s;
		}
		return s.substring(0, s.length() - 1);
	}

	@Override
	public VcrBookingData getVcrDetailsCfst(VcrInputVo vcrInputVo) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<VcrInputVo> httpEntity = new HttpEntity<>(vcrInputVo, headers);
		ResponseEntity<String> response = null;
		VcrBookingData vcrBookdataReturn = null;
		try {
			response = restTemplate.exchange(vcrDetailsFromCfstUrl + "/getVcrDetails", HttpMethod.POST, httpEntity,
					String.class);

		} catch (HttpClientErrorException httpClientErrorException) {

			logger.error(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_HTTPCLIENTERROR),
					httpClientErrorException.getMessage());

		} catch (Exception e) {
			logger.info(appMessages.getLogMessage(MessageKeys.SVS_RESULTNOTAVAILABLE), e.getMessage());

		}
		try {
			ObjectMapper mapper = new ObjectMapper();
			ResponseVcrEntity vcrDetails = mapper.readValue(response.getBody(), ResponseVcrEntity.class);
			if (vcrDetails.getResult() != null) {
				List<VcrBookingData> vcrList = vcrDetails.getResult();
				for (VcrBookingData vcrBookdata : vcrList) {
					vcrBookdataReturn = vcrBookdata;
					vcrDetailsDAO.save(vcrDetailsMapper.convertVO(vcrBookdata));
				}
			}

			return vcrBookdataReturn;
		} catch (Exception e) {
			logger.info(appMessages.getLogMessage(MessageKeys.SVS_RESULTNOTAVAILABLE), e.getMessage());
			return null;
		}

	}

	@Override
	public Optional<VcrDetailsDto> getVcrDetails(VcrInputVo vcrInputVo) {
		Optional<VcrDetailsDto> vcrdetails = null;
		if (StringUtils.isNoneBlank(vcrInputVo.getRegNo()) && StringUtils.isNoneBlank(vcrInputVo.getVcrNum())) {
			vcrdetails = vcrDetailsDAO.findByRegNoAndVcrNum(vcrInputVo.getRegNo(), vcrInputVo.getVcrNum());
		}
		return vcrdetails;

	}

	@Override
	public Optional<RegServiceDTO> checkDataEntryExits(String prNO) {
		List<ServiceEnum> services = new ArrayList<>();
		services.add(ServiceEnum.DATAENTRY);
		services.add(ServiceEnum.ISSUEOFNOC);
		List<RegServiceDTO> regList = regServiceDAO.findByPrNoAndServiceTypeIn(prNO, services);
		if (!regList.isEmpty()) {
			regList.sort((p2, p1) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
			RegServiceDTO regDTO = regList.stream().findFirst().get();
			return Optional.of(regDTO);
		}
		return Optional.empty();
	}

	@Override
	public Optional<RegistrationDetailsDTO> checkDataEntryExitsInRegDetails(String prNO,
			Optional<RegServiceDTO> regDatEntry) {
		List<ServiceEnum> services = new ArrayList<>();
		services.add(ServiceEnum.DATAENTRY);
		List<RegistrationDetailsDTO> regList = registrationDetailDAO.findByPrNoAndNocDetailsIsNull(prNO);
		if (!regList.isEmpty()) {
			regList.sort((p2, p1) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
			RegistrationDetailsDTO regDTO = regList.stream().findFirst().get();
			if (regDTO.getOfficeDetails() == null) {
				throw new BadRequestException("Office details not found");
			}
			String officeCode = regDTO.getOfficeDetails().getOfficeCode();
			Optional<OfficeDTO> office = officeDAO.findByOfficeCodeAndIsActiveTrue(officeCode);
			if (!office.isPresent()) {
				regDTO = null;
			}
			if (office.isPresent() && regDatEntry.isPresent()) {
				Boolean value = validateOtherStateNOC(regDatEntry);
				if (value) {
					throw new BadRequestException(
							"Application with same PRNumber " + prNO + " has already been submitted.");
				}
				// throw new BadRequestException("We are not belongs to this office");
			}
			return Optional.of(regDTO);
		}
		return Optional.empty();
	}

	@Override
	public Boolean hsrpExcelReport(HttpServletResponse response, Integer catagory, LocalDate fromDate,
			LocalDate toDate) {
		Optional<List<HsrpDetailDTO>> hsrpPostList = restGateWayService.getHsrpPostedList(fromDate, toDate, catagory);
		Collections.sort(hsrpPostList.get(), new Comparator<HsrpDetailDTO>() {
			@Override
			public int compare(HsrpDetailDTO p1, HsrpDetailDTO p2) {
				return p1.getCreatedDate().compareTo(p2.getCreatedDate());
			}
		});
		if (hsrpPostList.get().isEmpty()) {
			return false;
		}
		ExcelService excel = new ExcelServiceImpl();

		List<String> header = new ArrayList<String>();

		excel.setHeaders(header, "hsrpDetails");

		Random rand = new Random();
		int ranNo = rand.nextInt(1000);

		String name = "Payments_Reg_" + "" + "_" + ranNo;
		String fileName = name + ".xlsx";
		String sheetName = "HsrpPostedDetails";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		response.setHeader("Pragma", "public");
		response.setHeader("Cache-Control", "no-store");
		response.addHeader("Cache-Control", "max-age=0");

		XSSFWorkbook wb = excel.renderData(prepareHsrpCellProps(header, hsrpPostList.get()), header, fileName,
				sheetName);

		try {
			ServletOutputStream outputStream = null;
			outputStream = response.getOutputStream();
			wb.write(outputStream);
			outputStream.flush();
			outputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;

	}

	private List<List<CellProps>> prepareHsrpCellProps(List<String> header, List<HsrpDetailDTO> list) {
		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();
		List<CellProps> result = new ArrayList<CellProps>();
		for (HsrpDetailDTO hsrpDetails : list) {
			for (int i = 0; i < header.size(); i++) {
				CellProps cellpro = new CellProps();
				switch (i) {
				case 0:
					cellpro.setFieldValue(hsrpDetails.getAuthorizationRefNo());
					break;
				case 1:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getRtoCode()));
					break;
				case 2:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getRtoName()));
					break;
				case 3:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getAffixationCenterCode()));
					break;
				case 4:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getTransactionNo()));
					break;
				case 5:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getTransactionDate()));
					break;
				case 6:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getAuthorizationDate()));
					break;
				case 7:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getEngineNo()));
					break;
				case 8:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getChassisNo()));
					break;
				case 9:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getPrNumber()));
					break;
				case 10:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getOwnerName()));
					break;
				case 11:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getOwnerAddress()));
					break;
				case 12:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getOwnerEmailId()));
					break;
				case 13:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getOwnerPinCode()));
					break;
				case 14:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getMobileNo()));
					break;
				case 15:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getVehicleType()));
					break;
				case 16:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getTransType()));
					break;
				case 17:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getVehicleClassType()));
					break;
				case 18:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getMfrsName()));
					break;
				case 19:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getModelName()));
					break;
				case 20:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getHsrpFee()));
					break;
				case 21:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getOldNewFlag()));
					break;
				case 22:
					cellpro.setFieldValue(DateConverters.replaceDefaults(""));
					break;
				case 23:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getTimeStamp()));
					break;
				case 24:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getTrNumber()));
					break;
				case 25:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getDealerName()));
					break;
				case 26:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getDealerMail()));
					break;
				case 27:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getDealerRtoCode()));
					break;

				case 28:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getRegDate()));
					break;
				case 29:
					cellpro.setFieldValue(DateConverters.replaceDefaults(hsrpDetails.getMessage()));
					break;

				default:
					break;
				}
				result.add(cellpro);
			}
		}
		cell.add(result);
		return cell;

	}

	@Override
	public Optional<List<HsrpDetailDTO>> getHsrpPostedList(LocalDate fromDate, LocalDate toDate, Integer catagory) {
		List<LocalDateTime> localDateTimeList = DateConverters.convertDateToLocalDateTime(fromDate, toDate);

		Optional<List<HsrpDetailDTO>> hsrpPostedList = hsrpDetailDAO.findByHsrpStatusAndCreatedDateBetween(catagory,
				localDateTimeList.get(0), localDateTimeList.get(1));
		List<HsrpDetailDTO> hsrpDetails = new ArrayList<>();

		if (hsrpPostedList.isPresent()) {

			for (HsrpDetailDTO hsrpDetailsDTO : hsrpPostedList.get()) {
				hsrpDetails.add(hsrpDetailsDTO);
			}
			return Optional.of(hsrpDetails);
		}
		return Optional.empty();
	}

	private Optional<TaxDetailsDTO> getTaxDetails(String applicationNo) {
		List<TaxDetailsDTO> taxDetailsList = taxDetailsDAO
				.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(applicationNo, this.taxTypes());
		if (!taxDetailsList.isEmpty()) {
			TaxDetailsDTO dto = new TaxDetailsDTO();
			taxDetailsList.sort((p1, p2) -> p1.getTaxPeriodEnd().compareTo(p2.getTaxPeriodEnd()));
			dto = taxDetailsList.get(taxDetailsList.size() - 1);
			taxDetailsList.clear();
			return Optional.of(dto);
		}
		return Optional.empty();
	}

	private List<String> taxTypes() {
		List<String> taxTypes = new ArrayList<>();
		taxTypes.add(ServiceCodeEnum.QLY_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.HALF_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.YEAR_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.LIFE_TAX.getCode());
		return taxTypes;
	}

	@Override
	public Optional<TransactionDetailVO> getPaymentRequestObjectThroughjRestCall(
			TransactionDetailVO transactionDetailVO) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<TransactionDetailVO> httpEntity = new HttpEntity<>(transactionDetailVO, headers);
		// RestTemplate restTemplateL = new RestTemplate();
		ResponseEntity<String> response = restTemplate.exchange(paymentUrl, HttpMethod.POST, httpEntity, String.class);
		if (response.hasBody()) {
			GateWayResponse<TransactionDetailVO> inputOptional = parseJson(response.getBody(),
					new TypeReference<GateWayResponse<TransactionDetailVO>>() {
					});
			if (!inputOptional.getStatus()) {
				logger.error(inputOptional.getMessage());
				throw new BadRequestException(inputOptional.getMessage());
			}
			return Optional.of((TransactionDetailVO) inputOptional.getResult());
		}
		return Optional.empty();
	}

	@Override
	public String generatePrNo(PrGenerationVO prGenerationVO) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<PrGenerationVO> httpEntity = new HttpEntity<>(prGenerationVO, headers);
			// RestTemplate restTemplateL = new RestTemplate();
			logger.info("PR Number Generation URL Initiated Successfully");
			ResponseEntity<String> response = restTemplate.exchange(prGenerationUrl + "generatePrNo", HttpMethod.POST,
					httpEntity, String.class);
			if (response.hasBody()) {
				GateWayResponse<String> inputOptional = parseJson(response.getBody(),
						new TypeReference<GateWayResponse<String>>() {
						});
				if (!inputOptional.getStatus()) {
					if (StringUtils.isEmpty(inputOptional.getMessage())) {
						throw new BadRequestException(inputOptional.getResult());
					}
					throw new BadRequestException(inputOptional.getMessage());
				}
				return inputOptional.getResult();
			}
		} catch (Exception e) {
			logger.info("Issue Raised in common service war :: " + e.getMessage());
			throw new BadRequestException(e.getMessage());
		}
		return null;
	}

	@Override
	public TaxPaidVCRDetailsVO getTaxPaidVCRData(String prNo) {
		if (StringUtils.isEmpty(prNo)) {
			throw new BadRequestException("PR number not available to check vcr data");
		}
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		HttpEntity entity = new HttpEntity(headers);
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(vcrDetailsFromCfstUrl + "/getVcrTaxPaidDeatils?regnPrNo=" + prNo,
					HttpMethod.GET, entity, String.class);
		} catch (HttpClientErrorException httpClientErrorException) {

			logger.error(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_HTTPCLIENTERROR),
					httpClientErrorException.getMessage());

		} catch (Exception e) {
			logger.info(appMessages.getLogMessage(MessageKeys.SVS_RESULTNOTAVAILABLE), e.getMessage());

		}
		try {
			ObjectMapper mapper = new ObjectMapper();
			ResponseVcrTaxEntity vcrDetails = mapper.readValue(response.getBody(), ResponseVcrTaxEntity.class);
			TaxPaidVCRDetailsVO vcrData = null;
			if (vcrDetails.getHttpStatus().equals("false")) {
				throw new BadRequestException(
						"VCR details not found with the given VCR number. Please wait until your VCR is updated in the system or contact RTA office");
			}
			if (vcrDetails.getResult() != null) {
				vcrData = vcrDetails.getResult();
			}
			return vcrData;
		} catch (Exception e) {
			logger.info(appMessages.getLogMessage(MessageKeys.SVS_RESULTNOTAVAILABLE), e.getMessage());
			throw new BadRequestException("RESPONSE NOT RECEIVED FROM SERVER/ERROR RECEIVED WHILE GETTING VCR DETAILS");
		}

	}

	@Override
	public Optional<GSTNDataVO> getGSTNToken(String gstinNo) {

		ResponseEntity<String> response = null;
		/*
		 * Optional<GSTNDataDTO> gstnDataDTOOpt =
		 * gstnDataDAO.findByGstinNoAndStatusAndLastUpdatedDate(gstinNo.trim(),
		 * "Active", LocalDate.now());
		 */
		Optional<GSTNDataDTO> gstnDataDTOOpt = gstnDataDAO.findByGstinNoAndStatusOrderByLastUpdatedDateDesc(gstinNo.trim(), "Active");
		
		if (gstnDataDTOOpt.isPresent()) {
			logger.info("Data exists in Local DB for Gstin No : [{}]", gstinNo);
			return gstnDataMapper.convertEntity(gstnDataDTOOpt);
		}
		logger.debug("Inside getGSTNToken() Generate Gstin Application Token");
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", gstnToken);
		headers.add("Accept", "application/json");
		try {

			HttpEntity<GSTNConfig> httpEntity = new HttpEntity<>(new GSTNConfig(), headers);
			response = restTemplate.exchange(gstnTokenUrl, HttpMethod.GET, httpEntity, String.class);
			if (null == response || StringUtils.isBlank(response.getBody())) {
				return Optional.empty();
			}
			logger.info("Generate Gstin Application Token Url: [{}] Called Successfully ", gstnTokenUrl);

		} catch (HttpClientErrorException httpClientErrorException) {
			logger.debug("RestGatewayServiceImpl getGSTNToken() GSTN HttpClientError : [{}]", httpClientErrorException);
			logger.error("RestGatewayServiceImpl getGSTNToken() GSTN HttpClientError : [{}]",
					httpClientErrorException.getMessage());
			return Optional.empty();
		} catch (Exception exception) {
			logger.debug("RestGatewayServiceImpl getGSTNToken() GSTN Exception : [{}]", exception);
			logger.error("RestGatewayServiceImpl getGSTNToken() GSTN Exception : [{}]", exception.getMessage());
			return Optional.empty();
		}
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			Optional<GSTNConfig> gstnConfigOpt = Optional
					.ofNullable(objectMapper.readValue(response.getBody(), GSTNConfig.class));
			if (gstnConfigOpt.isPresent()) {
				GSTNConfig gstnConfig = gstnConfigOpt.get();
				if (!gstnConfig.getStatusCode().equals("1")) {
					logger.debug("Exception occured while generating Token");
					logger.error("Exception occured while generating Token");
					throw new BadRequestException(
							"Exception occured while generating Token for Gstin No :" + gstnConfig.getGstin());
				}
				gstnConfig.setGstin(gstinNo);
				return this.getOrganisationDataByGstnNumber(gstnConfig);
			}
			return Optional.empty();
		} catch (Exception e) {
			logger.debug("GSTN No Result Available {}", e);
			logger.error("GSTN No Result Available {}", e.getMessage());
			throw new BadRequestException("No Record Found");
		}
	}

	public Optional<GSTNDataVO> getOrganisationDataByGstnNumber(GSTNConfig gstnConfig) {
		ResponseEntity<String> response = null;
		Optional<GSTNDataVO> gstnDataVOOpt = null;
		if (null == gstnConfig) {
			logger.error("GSTIN Number not available to fetch the Data");
			throw new BadRequestException("GSTIN Number not available to fetch the Data");
		}
		logger.info("Inside getOrganisationDataByGstnNumber() based on GSTIN Number: [{}}", gstnConfig.getGstin());
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Authorization", gstnToken);
		headers.add("Accept", "application/json");
		HttpEntity<GSTNConfig> httpEntity = new HttpEntity<>(gstnConfig, headers);

		try {

			response = restTemplate.exchange(gstnDatapostingUrl, HttpMethod.POST, httpEntity, String.class);
			logger.info("Gstn DataPosting Url: [{}] Called Successfully", gstnDatapostingUrl);
			if (null == response || StringUtils.isBlank(response.getBody())) {
				return Optional.empty();
			}

		} catch (HttpClientErrorException httpClientErrorException) {
			logger.debug("RestGatewayServiceImpl GSTN getOrganisationDataByGstnNumber() HttpClientError : [{}]",
					httpClientErrorException);
			logger.error("RestGatewayServiceImpl GSTN getOrganisationDataByGstnNumber() HttpClientError : [{}]",
					httpClientErrorException.getMessage());
		} catch (Exception e) {
			logger.debug("RestGatewayServiceImpl GSTN getOrganisationDataByGstnNumber() Exception : [{}]", e);
			logger.error("RestGatewayServiceImpl GSTN getOrganisationDataByGstnNumber() Exception : [{}]",
					e.getMessage());
		}

		try {
			ObjectMapper mapper = new ObjectMapper();
			Optional<GSTNResponse> gstnResponse = Optional
					.ofNullable(mapper.readValue(response.getBody(), GSTNResponse.class));
			if (gstnResponse.isPresent()) {
				if (!gstnResponse.get().getStatus_cd().equals("1")) {
					logger.info("No Record Found with the given GSTIN No : [{}]", gstnConfig.getGstin());
					logger.error("No Record Found with the given GSTIN No : [{}]", gstnConfig.getGstin());
					throw new BadRequestException("No Record Found with the given GSTIN No : " + gstnConfig.getGstin());
				}
				String decodedString = encryptDecryptUtil.decrypt(gstnResponse.get().getData().getBytes());
				gstnDataVOOpt = Optional.ofNullable(mapper.readValue(decodedString, GSTNDataVO.class));
				if (gstnDataVOOpt.isPresent()) {
					gstnDataDAO.save(gstnDataMapper.convertVO(gstnDataVOOpt.get()));
					logger.info("GSTIN Number: [{}} data saved Successfully", gstnConfig.getGstin());
					logger.info("Outside getOrganisationDataByGstnNumber() based on GSTIN Number: [{}}",
							gstnConfig.getGstin());
					return gstnDataVOOpt;
				}
				return Optional.empty();
			} else {
				return Optional.empty();
			}
		} catch (Exception e) {
			logger.debug("GSTN No Result Available {}", e);
			logger.error("GSTN No Result Available {}", e.getMessage());
			throw new BadRequestException("No Record Found");
		}
	}

	@Override
	public RationCardDetailsVO getRationCardDetails(String aadharNo, String district) {
		if (StringUtils.isEmpty(aadharNo)) {
			logger.error("aadhaar number not available to check ration card data");
			throw new BadRequestException("aadhaar number not available to check ration card data");
		}
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		headers.set("Authorization", "Bearer " + accessToken);
		HttpEntity entity = new HttpEntity(headers);
		ResponseEntity<String> response = null;
		RationCardDistrictDTO districtCode = null;
		try {
			districtCode = getRationDistrictCode(district);
			response = restTemplate.exchange(rationCardUrl + "rationcard?UID=" + aadharNo + "&districtcode="
					+ districtCode.getRationCardDistrictCode(), HttpMethod.POST, entity, String.class);
		} catch (HttpClientErrorException httpClientErrorException) {
			logger.debug(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_HTTPCLIENTERROR),
					httpClientErrorException);
			logger.error(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_HTTPCLIENTERROR),
					httpClientErrorException.getMessage());

		} catch (Exception e) {
			logger.debug(appMessages.getLogMessage(MessageKeys.SVS_RESULTNOTAVAILABLE), e);
			logger.error(appMessages.getLogMessage(MessageKeys.SVS_RESULTNOTAVAILABLE), e.getMessage());

		}
		try {
			if (response.getStatusCodeValue() != 200) {
				logger.error("RESPONSE NOT RECEIVED FROM SERVER/ERROR RECEIVED WHILE GETTING RATION CARD DETAILS");
				throw new BadRequestException(
						"RESPONSE NOT RECEIVED FROM SERVER/ERROR RECEIVED WHILE GETTING RATION CARD DETAILS");
			}
			ObjectMapper mapper = new ObjectMapper();
			RationCardDetailsDTO rationCardDetails = mapper.readValue(response.getBody(), RationCardDetailsDTO.class);
			rationCardDetails.setAadhaarNo(aadharNo);
			rationCardDetailsDAO.save(rationCardDetails);
			RationCardDetailsVO vo = new RationCardDetailsVO();
			vo.setDistrict(
					replaceUpperCase(rationCardDetails.getRationCardDetails().getCarddetails().getDistrictName()));
			vo.setDistrictId(districtCode.getDistrictId());
			vo.setDoorNo(rationCardDetails.getRationCardDetails().getCarddetails().getDoorNo());
			vo.setMandal(replaceUpperCase(rationCardDetails.getRationCardDetails().getCarddetails().getOfficeName()));
			if (!vo.getMandal().isEmpty()) {
				List<MandalVO> mandallist = mandalService.findByDid(districtCode.getDistrictId());
				mandallist.stream().forEach(madalVo -> {
					String val = madalVo.getMandalName();
					if (val.equalsIgnoreCase(
							rationCardDetails.getRationCardDetails().getCarddetails().getOfficeName())) {
						vo.setMandalCode(madalVo.getMandalCode());
						vo.setMandal(madalVo.getMandalName());
					}
				});
			   List<CardMemberDetailsVO> familyMembers = new ArrayList<CardMemberDetailsVO>();
				if((rationCardDetails.getRationCardDetails().getMemberdetails()!=null)&&(!rationCardDetails.getRationCardDetails().getMemberdetails().isEmpty())) {
					List<CardMemberDetails> memberdetails = rationCardDetails.getRationCardDetails().getMemberdetails();
					memberdetails.forEach(member->{
						CardMemberDetailsVO cardMemberDetailsVO = new CardMemberDetailsVO();
					BeanUtils.copyProperties(member, cardMemberDetailsVO);	
					familyMembers.add(cardMemberDetailsVO);
						
					});
					vo.setFamilyMembers(familyMembers);
				}
				vo.setHouseHoldCardNo(rationCardDetails.getRationCardDetails().getCarddetails().getHouseHoldCardNo());
			}

			vo.setVillage(replaceUpperCase(rationCardDetails.getRationCardDetails().getCarddetails().getVillageName()));
			vo.setStreet(replaceUpperCase(rationCardDetails.getRationCardDetails().getCarddetails().getAddress()));
			vo.setPincode(StringUtils.EMPTY);
			
			return vo;
		} catch (Exception e) {
			logger.debug(appMessages.getLogMessage(MessageKeys.SVS_RESULTNOTAVAILABLE), e);
			logger.error(appMessages.getLogMessage(MessageKeys.SVS_RESULTNOTAVAILABLE), e.getMessage());

			throw new BadRequestException(
					"RESPONSE NOT RECEIVED FROM SERVER/ERROR RECEIVED WHILE GETTING RATION CARD DETAILS");
		}

	}

	/** Method to fetch rationcard district codes from DB **/
	public RationCardDistrictDTO getRationDistrictCode(String districtName) {
		if (StringUtils.isNotEmpty(districtName)) {
			districtName = districtName.replaceAll("\\s", "");
			Optional<RationCardDistrictDTO> rcDistrictCode = rationCardDistrictDAO
					.findByAadhaarDistrict(districtName.toLowerCase());
			return rcDistrictCode.get();
		}
		return null;
	}

	public String replaceUpperCase(String value) {
		if (StringUtils.isNotEmpty(value)) {
			return value.toUpperCase();
		}
		return StringUtils.EMPTY;

	}

	private void saveAadhaarResponce(AadharDetailsResponseVO aadharDetailsResponseVO, String udc) {
		if ("SUCCESS".equalsIgnoreCase(aadharDetailsResponseVO.getAuth_status())) {

			AadhaarDetailsResponseDTO aadhaarResponseDTO = aadhaarDetailsResponseMapper
					.convertVO(aadharDetailsResponseVO);
			aadhaarResponseDTO.setDeviceNumber(udc);
			aadhaarResponseDAO.save(aadhaarResponseDTO);

		}

	}

	@Override
	public String generatePaymentReciept() {
		try {
			if (StringUtils.isEmpty(cashpaymentSeqNo)) {
				throw new BadRequestException("cash payment sequence url missing");
			}
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			HttpEntity<PrGenerationVO> httpEntity = new HttpEntity<>(headers);

			ResponseEntity<String> response = restTemplate.exchange(cashpaymentSeqNo, HttpMethod.GET, httpEntity,
					String.class);
			if (response.hasBody()) {
				GateWayResponse<String> inputOptional = parseJson(response.getBody(),
						new TypeReference<GateWayResponse<String>>() {
						});
				if (!inputOptional.getStatus()) {
					if (StringUtils.isEmpty(inputOptional.getMessage())) {
						throw new BadRequestException(inputOptional.getResult());
					}
					throw new BadRequestException(inputOptional.getMessage());
				}
				return inputOptional.getResult();
			}
		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());
		}
		return null;
	}

	@Override
	public Optional<VahanVehicleDetailsVO> getVahanVehicleDetails(String prNo) {

		Map<String, String> uriParams = null;
		logger.info(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_VAHAN_CALL));
		if (StringUtils.isEmpty(prNo)) {
			throw new BadRequestException(appMessages.getLogMessage(MessageKeys.VAHAN_UNAUTHORIZEDREQ));
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.set("Accept", "application/json");
		if (StringUtils.isNoneBlank(prNo)) {
			uriParams = new HashMap<>();
			uriParams.put("prNo", prNo);
		} else {
			return Optional.empty();
		}
		UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(vahanVehicleUrl).queryParam("prNo", prNo);
		HttpEntity<String> entity = new HttpEntity<>(headers);
		ResponseEntity<String> result = null;
		try {
			result = restTemplate.exchange(builder.buildAndExpand(uriParams).toUri(), HttpMethod.GET, entity,
					String.class);
		} catch (HttpClientErrorException httpClientErrorException) {

			logger.debug(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_HTTPCLIENTERROR),
					httpClientErrorException);
			logger.error(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_PAN_HTTPCLIENTERROR),
					httpClientErrorException.getMessage());
			return Optional.empty();

		} catch (Exception e) {

			logger.info(appMessages.getLogMessage(MessageKeys.VAHAN_RESULTNOTAVAILABLE), e.getMessage());
			return Optional.empty();

		}

		if (result == null || StringUtils.isBlank(result.getBody())) {
			return Optional.empty();
		}
		try {
			Optional<VahanVehicleResponseVO> resultOpt = Optional
					.ofNullable(objectMapper.readValue(result.getBody(), VahanVehicleResponseVO.class));
			if (resultOpt.isPresent()) {
				if (resultOpt.get().getHttpStatus().equals(HttpStatus.BAD_REQUEST.name())
						|| resultOpt.get().getHttpStatus().equals(HttpStatus.SERVICE_UNAVAILABLE.name())) {
					throw new BadRequestException(resultOpt.get().getMessage());
				}
				return Optional.of(resultOpt.get().getResult());
			}
			return Optional.empty();
		} catch (IOException e) {
			logger.error("Error while fetching the prNo No [{}] & Exception is [{}], ", prNo, e.getMessage());
		}
		return Optional.empty();
	}

	@Override
	public OtherStateVahanVO getVahanVehicleDetailsForOtherState(String prNo) {
		Pair<OtherStateVahanVO, List<String>> result = null;
		try {
			result = otherStateVahanService.convertVahanVehicleToOtherState(getVahanVehicleDetails(prNo).get(), prNo);
			if (CollectionUtils.isEmpty(result.getSecond())) {
				otherStateVahanResponseDAO.save(otherStateVahanResponseMapper.convertVO(result.getFirst()));
				return result.getFirst();
			}
		} catch (Exception e) {
			if (!e.getMessage().equalsIgnoreCase("Data Not Found")) {
				throw new BadRequestException(e.getMessage());
			}
		}
		return null;
	}

	@Override
	public VCRVahanVehicleDetailsVO getVahanVehicleDetailsForVcr(String prNo) {
		return vcrVahanVehicleService.convertVahanVehicleToVcr(getVahanVehicleDetails(prNo).get(), prNo);
	}

	private void setUserAuthTime(String userId, String aadhaarNumber) {
		Optional<UserDTO> userLoad = userDAO.findByUserIdAndUserStatus(userId, UserStatusEnum.ACTIVE);
		if (!userLoad.isPresent()) {
			throw new BadRequestException("Unauthorized user");
		}
		if (StringUtils.isEmpty(userLoad.get().getAadharNo()) || !aadhaarNumber.equals(userLoad.get().getAadharNo())) {
			throw new BadRequestException("Unauthorized user");
		}
		userLoad.get().setUserAadhaarAuthTime(LocalDateTime.now());
		userDAO.save(userLoad.get());
	}

	@Override
	public Boolean validateOtherStateNOC(Optional<RegServiceDTO> regDatEntry) {
		if (regDatEntry.get().getApplicationStatus().equals(StatusRegistration.CITIZENSUBMITTED)
				|| regDatEntry.get().getApplicationStatus().equals(StatusRegistration.APPROVED)
				|| regDatEntry.get().getApplicationStatus().equals(StatusRegistration.SLOTBOOKED)
				|| regDatEntry.get().getApplicationStatus().equals(StatusRegistration.OTHERSTATEPAYMENTPENDING)
				|| regDatEntry.get().getApplicationStatus().equals(StatusRegistration.PAYMENTDONE)) {
			if (!regDatEntry.get().getServiceIds().contains(ServiceEnum.ISSUEOFNOC.getId())) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}
}
