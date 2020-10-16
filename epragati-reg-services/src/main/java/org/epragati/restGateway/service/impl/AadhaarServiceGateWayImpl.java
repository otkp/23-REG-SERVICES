package org.epragati.restGateway.service.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.epragati.aadhaar.AadhaarDetailsRequestVO;
import org.epragati.aadhaar.AadharDetailsResponseVO;
import org.epragati.aadhaarAPI.AadhaarRestServiceConsumer;
import org.epragati.aadhaarAPI.util.AadhaarConstant;
import org.epragati.aadhaarAPI.util.PojoValidatorUtil;
import org.epragati.aadhar.APIResponse;
import org.epragati.constants.MessageKeys;
import org.epragati.master.dao.AadhaarResponseDAO;
import org.epragati.master.dto.AadhaarDetailsResponseDTO;
import org.epragati.master.mappers.AadhaarDetailsResponseMapper;
import org.epragati.restGateway.AadhaarServiceGateWay;
import org.epragati.util.AppMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AadhaarServiceGateWayImpl implements AadhaarServiceGateWay {


	@Autowired
	private ObjectMapper objectMapper;

	@Value("${reg.aadhaar.source:}")
	private String source;

	@Value("${reg.service.aadhaarToken:}")
	private String aadhaarToken;

	@Value("${reg.service.aadhaarValidationUrl:}")
	private String aadhaarValidationUrl;

	@Autowired
	private AadhaarResponseDAO aadhaarResponseDAO;

	@Autowired
	private AadhaarDetailsResponseMapper aadhaarDetailsResponseMapper;

	@Autowired
	private AppMessages appMessages;

	@Value("${reg.aadhaar.isInternalConn:true}")
	private boolean isInternalConn;

	@Autowired
	private AadhaarRestServiceConsumer aadhaarRestServiceConsumer;
	
	@Value("${apts.request.allow}")
	private String isRequestToApts;

	@Autowired
	private PojoValidatorUtil pojoValidatorUtil;

	private static final Logger logger = LoggerFactory.getLogger(AadhaarServiceGateWayImpl.class);

	@Override
	public Optional<AadharDetailsResponseVO> validateAadhaar(AadhaarDetailsRequestVO model) {
		if(isRequestToApts.equalsIgnoreCase("Y")) {
			AadharDetailsResponseVO consumeAptsSoapApi = aadhaarRestServiceConsumer.consumeAptsSoapApi(model);
			return Optional.ofNullable(consumeAptsSoapApi);
		}else {
		model.setConsentme("Y");
		//		model.setBt("IIR");
		model.setAttemptType("1EA");
		model.setSource(model.getSource() + "_" + source);
		if(isInternalConn){
			AadharDetailsResponseVO aadharDetailsResponseVO=null;
			List<FieldError> fieldErrors = null;
			if (StringUtils.isEmpty(model.getRequestType())) {
				//				fieldErrors = pojoValidatorUtil.doValidation(requestModel, requestModel.getClass().getName());
				//
				//				if (!fieldErrors.isEmpty()) {
				//					return new APIResponse<String>("Validation failed", fieldErrors);
				//				}
				aadharDetailsResponseVO=aadhaarRestServiceConsumer.getAadhaarData(model,null);
			}

			/// --new flow
			//fieldErrors = pojoValidatorUtil.doCustomValidation(requestModel, requestModel.getClass().getName());

			//			if (!CollectionUtils.isEmpty(fieldErrors)) {
			//				logger.error("Validation failed {}", fieldErrors);
			//				return new APIResponse<String>("Validation failed", fieldErrors);
			//			}

			if (AadhaarConstant.RequestType.OPT.getContent().equals(model.getRequestType())) {

				aadharDetailsResponseVO=aadhaarRestServiceConsumer.sendOTPRequest(model);
			} else if (AadhaarConstant.RequestType.EKYC.getContent().equals(model.getRequestType())) {

				aadharDetailsResponseVO=aadhaarRestServiceConsumer.otpAuthentication(model);
			}
			if (null != aadharDetailsResponseVO) {
				saveAadhaarResponce(aadharDetailsResponseVO,model.getUdc());
			}

			return Optional.ofNullable(aadharDetailsResponseVO);

		}else {
			Long startTime = System.currentTimeMillis();
			logger.info(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_AADHAR_ENTRY)+" : "+model.getUid_num());
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);
			headers.set("Authorization", aadhaarToken);
			HttpEntity<AadhaarDetailsRequestVO> httpEntity = new HttpEntity<>(model, headers);
			//RestTemplate restTemplateL = new RestTemplate();

			CloseableHttpClient httpclient = HttpClients.createDefault();
			//httpclient.getConnectionManager().shutdown();
			HttpPost post = new HttpPost(aadhaarValidationUrl);

			String s ="";
			try {
				s = objectMapper.writeValueAsString(model);
			} catch (JsonProcessingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			logger.info(s);
			StringEntity input;
			try {
				input = new StringEntity(s);
				post.setEntity(input);
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			post.addHeader(new BasicHeader(HttpHeaders.CONTENT_TYPE, "application/json"));
			post.addHeader(new BasicHeader(HttpHeaders.AUTHORIZATION, aadhaarToken));
			post.addHeader(new BasicHeader(HttpHeaders.CONNECTION, "close"));

			// TODO Auto-generated catch block

			//org.apache.http.HttpEntity<AadhaarDetailsRequestVO> httpEntityValue = new HttpEntity<>(model);
			//post.setEntity(entity);
			//entity
			//post.setEntity((org.apache.http.HttpEntity) httpEntityValue);


			String result = StringUtils.EMPTY;
			BufferedReader rd = null;
			try {
				CloseableHttpResponse response  = httpclient.execute(post);
				logger.info("Time Taken Complete {}, for UID {}", (System.currentTimeMillis()-startTime), model.getUid_num());
				System.currentTimeMillis();
				rd = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
				String line = "";
				while ((line = rd.readLine()) != null) {
					result = result + line;
				}

			} catch (IOException e) {
				e.printStackTrace();
			}
			finally{
				try{
					IOUtils.closeQuietly(rd);
					httpclient.close();
				}catch(Exception e){
				}
			}


			//ResponseEntity<String> response = aadhaarRestTemplate.exchange(aadhaarValidationUrl, HttpMethod.POST, httpEntity,
			//		String.class);
			//logger.info("Aadhaar Validation Url: [{}] Called ", aadhaarValidationUrl);
			//logger.info(appMessages.getLogMessage(MessageKeys.RESTGATEWAYSERVICEIMPL_AADHAR_RESPONSE),
			//		model.getUid_num()/* response */);

			//if (response == null || StringUtils.isBlank(response.getBody())) {
			//	return Optional.empty();
			//}
			APIResponse<AadharDetailsResponseVO> resultOPt = parseJson(result,
					new TypeReference<APIResponse<AadharDetailsResponseVO>>() {
			});
			if (null != resultOPt && resultOPt.getStatus() && null != resultOPt.getResult()) {
				saveAadhaarResponce(resultOPt.getResult(),model.getUdc());
				return Optional.ofNullable(resultOPt.getResult());
			}
			return Optional.empty();
		}
		}
	}



	private void saveAadhaarResponce(AadharDetailsResponseVO aadharDetailsResponseVO,String udc) {
		if ("SUCCESS".equalsIgnoreCase(aadharDetailsResponseVO.getAuth_status())) {

			AadhaarDetailsResponseDTO aadhaarResponseDTO = aadhaarDetailsResponseMapper
					.convertVO(aadharDetailsResponseVO);
			aadhaarResponseDTO.setDeviceNumber(udc);
			aadhaarResponseDAO.save(aadhaarResponseDTO);

		}
		
	}



	private <T> T parseJson(String value, TypeReference<T> valueTypeRef) {
		try {
			return objectMapper.readValue(value, valueTypeRef);
		} catch (IOException ioe) {
			logger.error(appMessages.getLogMessage(MessageKeys.PARSEJSON_JSONTOOBJECT), ioe);
		}
		return null;
	}
}
