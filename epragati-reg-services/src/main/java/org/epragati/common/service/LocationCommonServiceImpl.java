package org.epragati.common.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import org.apache.commons.lang3.StringEscapeUtils;
import org.epragati.common.dao.PrajaasachivalayamDAO;
import org.epragati.common.dto.PrajaasachivalayamDTO;
import org.epragati.common.location.vo.PSResponceVO;
import org.epragati.common.location.vo.PrajasachivalayamInputVO;
import org.epragati.common.location.vo.PrajasachivalayamResponseVO;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.vo.RegServiceVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class LocationCommonServiceImpl implements LocationCommonService {
	 protected static final Logger logger = LoggerFactory.getLogger(LocationCommonServiceImpl.class);
	  
	 @Autowired 
	 private RestTemplate restTemplate;
	 
	 @Value("${reg.service.prajaasachivalayamLoginUrl:}") 
	 private String prajaasachivalayamLoginUrl;
	 
	 @Value("${reg.service.prajaasachivalayamUrl:}")
	 private String prajaasachivalayamUrl;
	 
	 @Autowired 
	 private ObjectMapper objectMapper;
	
	 @Autowired 
	 private PrajaasachivalayamDAO psDAO;
	 
	//sending request to prajasachivalayam and saving in db
		 @Override
		 public Optional<?> prajaasachivalayamLogininfo(Optional<RegServiceVO> voObject, RegServiceVO regServiceDetail) {
			 try {	
					ResponseEntity<String> result = null;
					PrajasachivalayamInputVO psloginDetails = new PrajasachivalayamInputVO();
								
					HttpHeaders headers = new HttpHeaders();
					headers.setContentType(MediaType.APPLICATION_JSON);
					String replaceSpace = voObject.get().getPs_encrypted_data().replace(' ', '+');
					psloginDetails.setENCRYPTED_DATA(replaceSpace);
					psloginDetails.setIV(voObject.get().getPs_iv());
					psloginDetails.setKEY("3fee5395f01bee349feed65629bd442a");
					HttpEntity<PrajasachivalayamInputVO> httpEntity = new HttpEntity<>(psloginDetails, headers);
					Optional<PSResponceVO> psResponceVO = null;
					//String psurl = "http://prajaasachivalayam.ap.gov.in/Services/api/transaction/authenticatedUserDetails";
					try {
						result = restTemplate.exchange(prajaasachivalayamLoginUrl, HttpMethod.POST, httpEntity, String.class);
					} catch (Exception ex) {
							logger.debug("Exception while Rest call. Exception is: [{}]");
							logger.error("Exception while Rest call. Exception is: [{}]");
					}
					//covert to valid string format for assign values to vo using  objectMapper
					String string = result.getBody();
					string = string.replace("\"result\":\"{", "\"result\":{");
					string = string.replace("\"}\"}", "\"}}");
				 			
					psResponceVO = readValue(string, PSResponceVO.class);
					if(psResponceVO.isPresent()) {
						PrajasachivalayamResponseVO resultVO = psResponceVO.get().getResult();
						this.savePrajasachivalayamloginDetails(resultVO,regServiceDetail);
					} else {
						logger.debug("Invalid Data From Rest Call: [{}]");
					}
					
				}catch(Exception e) {
						logger.debug("Exacetion Occured in prajaasachivalayamLogininfo : [{}]");
						logger.error("Exacetion Occured in prajaasachivalayamLogininfo : [{}]");
				}
			return null;
		 }
		 
		 public <T> Optional<T> readValue(String value, Class<T> valueType) {
				String escaped = StringEscapeUtils.unescapeJson(value);
				try {
					return Optional.of(objectMapper.readValue(escaped, valueType));
				} catch (IOException ioe) {
					logger.error("Exception occured while converting String to Object", ioe.getMessage());
				}
				return Optional.empty();
		 }
		 	 
		 private void savePrajasachivalayamloginDetails(PrajasachivalayamResponseVO resultVO,RegServiceVO regServiceDetail){
			 try {
				PrajaasachivalayamDTO psDto=new PrajaasachivalayamDTO();
				psDto.setPs_transaction_id(resultVO.getTxn_id());
				psDto.setDepartment_transaction_id(regServiceDetail.getApplicationNo());
				psDto.setBenficiary_id(regServiceDetail.getAadhaarNo());
				psDto.setStatus_Code(resultVO.getStatus());
				psDto.setPs_userName(resultVO.getUserName());
				psDto.setPs_password(resultVO.getPassword());
				psDto.setApplicationNo(regServiceDetail.getApplicationNo());
				psDto.setStatus_message(regServiceDetail.getApplicationStatus().getDescription());
				psDto.setCreatedDate(LocalDateTime.now());
				Optional<Integer> first = regServiceDetail.getServiceIds().stream().findFirst();
				if(first.isPresent()) {
					psDto.setService_id(first.get());
				}
				psDto.setDept_id("3702");
				psDAO.save(psDto);
			 }catch(Exception e) {
				 logger.error("Exception occured while saving savePrajasachivalayamloginDetails");
			 }
		}

		 //calling after payment transaction PaymentGatewayServiceImpl -- updatePaymentStatus
		 public void prajaasachivalayamApplicationStatus(RegServiceDTO regServiceDTO) {
			
			 try {
				 Optional<PrajaasachivalayamDTO>  psDetails=psDAO.findByApplicationNo(regServiceDTO.getApplicationNo());
					if(psDetails.isPresent()) {
					 	boolean status = false;
						ResponseEntity<String> result = null;
						PrajasachivalayamInputVO psloginDetails = new PrajasachivalayamInputVO();
						HttpHeaders headers = new HttpHeaders();
						headers.setContentType(MediaType.APPLICATION_JSON);
						psloginDetails.setTXN_ID(psDetails.get().getPs_transaction_id());
						psloginDetails.setDEPT_TXN_ID(psDetails.get().getDepartment_transaction_id());
						psloginDetails.setBEN_ID(psDetails.get().getBenficiary_id());
						psloginDetails.setSERVICE_ID(Integer.toString(psDetails.get().getService_id()));
						psloginDetails.setSTATUS_MESSAGE(regServiceDTO.getApplicationStatus().getDescription());
						psloginDetails.setREMARKS("");
						psloginDetails.setDEPT_ID("3702");
						HttpEntity<PrajasachivalayamInputVO> httpEntity = new HttpEntity<>(psloginDetails, headers);
						//String psurl ="http://prajaasachivalayam.ap.gov.in/Services/api/transaction/closingTransaction";
						try {
							result = restTemplate.exchange(prajaasachivalayamUrl, HttpMethod.POST, httpEntity, String.class);
						} catch (Exception ex) {
							logger.debug("Unable get details form PrajaaSachivalayam server. [{}]");
							logger.error("Unable get details form PrajaaSachivalayam server. [{}]");
						}
						
						//covert to valid string format for assign values to vo using objectMapper
						String string = result.getBody();
						if (!string.isEmpty()) {
							 psDetails.get().setStatus_message(regServiceDTO.getApplicationStatus().getDescription());
							 status= true;
							 psDetails.get().setStatus(status); 
							 psDetails.get().setlUpdate(LocalDateTime.now());
							 psDAO.save(psDetails.get());
						}
					}
				}catch(Exception e) {
					logger.debug("Exacetion Occured in prajaasachivalayamApplicationStatus");
					logger.error("Exacetion Occured in prajaasachivalayamApplicationStatus");
				}
					
			}
		 
	 
}
