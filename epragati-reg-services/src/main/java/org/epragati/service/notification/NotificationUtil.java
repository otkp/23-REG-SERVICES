package org.epragati.service.notification;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;

import org.apache.commons.lang3.StringUtils;
import org.epragati.dispatcher.dto.DispatcherSubmissionDTO;
import org.epragati.master.dto.DealerRegDTO;
import org.epragati.master.dto.FinancierCreateRequestDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.notification.DestinationInfo;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.sn.dto.SpecialNumberDetailsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class NotificationUtil {

	@Autowired
	private MessageSender messageSender;

	// SMS
	@Value("${sms.host}")
	private String smsHost;

	@Value("${sms.password}")
	private String smsPassword;

	@Value("${sms.username}")
	private String smsUsername;

	@Value("${sms.senderid}")
	private String smsSenderId;

	@Value("${sms.securityKey:}")
	private String securityKey;

	// Email
	@Value("${email.host}")
	private String emailHost;

	@Value("${email.port}")
	private String emailPort;

	@Value("${email.from}")
	private String emailFrom;

	@Value("${email.password}")
	private String emailPassword;

	@Value("${email.userName}")
	private String emailUserName;

	@Autowired
	private NotificationTemplates notification;

	public <T> void sendEmailNotification(BiFunction<Integer, T, Map<String, HashMap<String, String>>> fun,
			Integer templateId, T object, String email) throws IOException {
		Map<String, HashMap<String, String>> templateContent = notification.fillTemplate(fun, templateId, object);
		String emailContent = null;
		String emailSubject = null;
		String transactionId = StringUtils.EMPTY;
		for (String key : templateContent.keySet()) {
			if (key == "EMAIL") {
				emailContent = templateContent.get(key).get(NotificationKeyWords.MESSAGE.getValue());
				emailSubject = templateContent.get(key).get(NotificationKeyWords.SUBJECT.getValue());
				transactionId = templateContent.get(key).get(NotificationKeyWords.transactionId.getValue());
			}
		}
		DestinationInfo destInfo = getEmailConfigDetails(templateId, transactionId);
		Map<String, String> parameters = destInfo.getParameters();
		parameters.put("to", email);
		parameters.put(NotificationKeyWords.SUBJECT.getValue(), emailSubject);
		destInfo.setParameters(parameters);
		destInfo.setEmailBody(emailContent);
		messageSender.sendEmailMessage(destInfo);
	}

	public <T> void sendMessageNotification(BiFunction<Integer, T, Map<String, HashMap<String, String>>> fun,
			Integer templateId, T object, String mobileNo) throws IOException {

		Map<String, HashMap<String, String>> templateContent = notification.fillTemplate(fun, templateId, object);

		String smsContent = null;
		String transactionId = StringUtils.EMPTY;
		for (String key : templateContent.keySet()) {
			if (key.equals("SMS")) {
				smsContent = templateContent.get(key).get(NotificationKeyWords.MESSAGE.getValue());
				transactionId = templateContent.get(key).get(NotificationKeyWords.transactionId.getValue());
			}
		}

		DestinationInfo destInfo = getSMSConfigDetails(templateId, transactionId, smsContent);

		Map<String, String> parameters = destInfo.getParameters();

		parameters.put("mobileno", mobileNo);
		parameters.put("content", smsContent);

		destInfo.setParameters(parameters);

		StringBuffer emailBody = new StringBuffer();
		parameters.forEach((key, value) -> {
			emailBody.append(key + "=" + value + "&");
		});

		destInfo.setEmailBody(emailBody.toString());
		destInfo.setParameters(parameters);
		messageSender.sendSMSMessage(destInfo);
	}

	private DestinationInfo getSMSConfigDetails(Integer serviceid, String applicationNumber, String content) {

		DestinationInfo destInfo = new DestinationInfo();
		destInfo.setTransactionId(applicationNumber);
		destInfo.setHost(this.smsHost);
		destInfo.setMessageType("sms");
		destInfo.setTransactionId("1");
		destInfo.setServiceId(String.valueOf(serviceid));

		destInfo.setProtocol("https");

		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("smsservicetype", "singlemsg");
		try {
			parameters.put("password", MD5(this.smsPassword));
		} catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
		}
		parameters.put("username", this.smsUsername);
		parameters.put("senderid", this.smsSenderId);
		String genratedhashKey = hashGenerator(this.smsUsername, this.smsSenderId, content, this.securityKey);
		parameters.put("key", genratedhashKey);

		destInfo.setParameters(parameters);
		return destInfo;
	}

	public String hashGenerator(String userName, String senderId, String content, String secureKey) {

		StringBuffer finalString = new StringBuffer();
		finalString.append(userName.trim()).append(senderId.trim()).append(content.trim()).append(secureKey.trim());
		String hashGen = finalString.toString();
		StringBuffer sb = null;
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-512");
			md.update(hashGen.getBytes());
			byte byteData[] = md.digest();
			// convert the byte to hex format method 1
			sb = new StringBuffer();
			for (int i = 0; i < byteData.length; i++) {
				sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
			}

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	public static String MD5(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
		MessageDigest md;
		md = MessageDigest.getInstance("SHA-1");
		byte[] md5 = new byte[64];
		md.update(text.getBytes("iso-8859-1"), 0, text.length());
		md5 = md.digest();
		return convertedToHex(md5);
	}

	private static String convertedToHex(byte[] data) {
		StringBuffer buf = new StringBuffer();

		for (int i = 0; i < data.length; i++) {
			int halfOfByte = (data[i] >>> 4) & 0x0F;
			int twoHalfBytes = 0;

			do {
				if ((0 <= halfOfByte) && (halfOfByte <= 9)) {
					buf.append((char) ('0' + halfOfByte));
				}

				else {
					buf.append((char) ('a' + (halfOfByte - 10)));
				}

				halfOfByte = data[i] & 0x0F;

			} while (twoHalfBytes++ < 1);
		}
		return buf.toString();
	}

	private DestinationInfo getEmailConfigDetails(Integer serviceid, String applicationNumber) {

		DestinationInfo destInfo = new DestinationInfo();
		destInfo.setHost(this.emailHost);
		destInfo.setTransactionId(applicationNumber);
		destInfo.setPort(this.emailPort);
		destInfo.setProtocol("smtp");
		Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("username", this.emailUserName);
		parameters.put("from", this.emailFrom);
		parameters.put("password", this.emailPassword);
		parameters.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

		destInfo.setParameters(parameters);
		return destInfo;
	}

	public void sendNotifications(Integer templateId, Object object) {
		try {
			if (object != null) {
				String email = null;
				String mobile = null;
				if (object instanceof StagingRegistrationDetailsDTO) {
					StagingRegistrationDetailsDTO stagingRegistrationDetailsDTO = (StagingRegistrationDetailsDTO) object;
					email = stagingRegistrationDetailsDTO.getApplicantDetails().getContact().getEmail();
					mobile = stagingRegistrationDetailsDTO.getApplicantDetails().getContact().getMobile();
				} else if (object instanceof SpecialNumberDetailsDTO) {
					SpecialNumberDetailsDTO specialNumberDetailsDTO = (SpecialNumberDetailsDTO) object;
					email = specialNumberDetailsDTO.getCustomerDetails().getEmailId();
					mobile = specialNumberDetailsDTO.getCustomerDetails().getMobileNo();
				} else if (object instanceof RegServiceDTO) {
					RegServiceDTO regServiceDTO = (RegServiceDTO) object;
					email = regServiceDTO.getRegistrationDetails().getApplicantDetails().getContact().getEmail();
					mobile = regServiceDTO.getRegistrationDetails().getApplicantDetails().getContact().getMobile();
					if (templateId == MessageTemplate.TOW_TOKENCANCEL_FIN_APPROVED.getId()
							|| templateId == MessageTemplate.TOW_TOKENCANCEL_FIN_REJECTED.getId()) {
						mobile = regServiceDTO.getUsersContactDetails().getMobile();
						email = regServiceDTO.getUsersContactDetails().getEmail();
					}
					if (templateId == MessageTemplate.TOW_BUYER.getId()) {
						mobile = regServiceDTO.getBuyerDetails().getBuyerApplicantDetails().getContact().getMobile();
						email = regServiceDTO.getBuyerDetails().getBuyerApplicantDetails().getContact().getEmail();
					}
					if (templateId == MessageTemplate.RCFORFINANCE.getId()) {
						if (StringUtils.isNoneBlank(mobile)) {
							Integer templateIdFrc = MessageTemplate.RCFORFINANCECIT.getId();
							this.sendEmailNotification(notification::fillTemplate, templateIdFrc, object, email);
							this.sendMessageNotification(notification::fillTemplate, templateIdFrc, object, mobile);
						}
						mobile = regServiceDTO.getFreshRcdetails().getFinancerContactDetails().getMobile();
						email = regServiceDTO.getFreshRcdetails().getFinancerContactDetails().getEmail();
					}
					if (templateId == MessageTemplate.RCFORFINANCEFINAPP.getId()) {
						if (StringUtils.isNoneBlank(mobile)) {
							Integer templateIdFrcApp = MessageTemplate.RCFORFINANCECITAPP.getId();
							this.sendEmailNotification(notification::fillTemplate, templateIdFrcApp, object, email);
							this.sendMessageNotification(notification::fillTemplate, templateIdFrcApp, object, mobile);
						}
						mobile = regServiceDTO.getFreshRcdetails().getFinancerContactDetails().getMobile();
						email = regServiceDTO.getFreshRcdetails().getFinancerContactDetails().getEmail();
					}
					if (templateId == MessageTemplate.RCFORFINANCEFORM37FIN.getId()) {
						if (StringUtils.isNoneBlank(mobile)) {
							Integer templateIdFrcForm37 = MessageTemplate.RCFORFINANCEFORM37CIT.getId();
							this.sendEmailNotification(notification::fillTemplate, templateIdFrcForm37, object, email);
							this.sendMessageNotification(notification::fillTemplate, templateIdFrcForm37, object,
									mobile);
						}
						mobile = regServiceDTO.getFreshRcdetails().getFinancerContactDetails().getMobile();
						email = regServiceDTO.getFreshRcdetails().getFinancerContactDetails().getEmail();
					}
					if (templateId == MessageTemplate.FINANCIERNOTMATCH.getId()) {
						if (regServiceDTO.getFreshRcdetails().isAOAssignedToMVI() && StringUtils.isNoneBlank(mobile)) {
							Integer templateIdFrcForm37 = MessageTemplate.VEHICHLENOTREPOSSESED.getId();
							this.sendEmailNotification(notification::fillTemplate, templateIdFrcForm37, object,
									regServiceDTO.getFreshRcdetails().getFinancerContactDetails().getEmail());
							this.sendMessageNotification(notification::fillTemplate, templateIdFrcForm37, object,
									regServiceDTO.getFreshRcdetails().getFinancerContactDetails().getMobile());
						}
						if (!regServiceDTO.getFreshRcdetails().isAOAssignedToMVI()) {
							mobile = regServiceDTO.getFreshRcdetails().getFinancerContactDetails().getMobile();
							email = regServiceDTO.getFreshRcdetails().getFinancerContactDetails().getEmail();
						}
					}
					if (templateId == MessageTemplate.RCFORFINANCEREG.getId()) {
						mobile = regServiceDTO.getFreshRcdetails().getFinancerContactDetails().getMobile();
						email = regServiceDTO.getFreshRcdetails().getFinancerContactDetails().getEmail();
					}
					if (templateId == MessageTemplate.STOPPAGEMVI.getId()) {
						mobile = regServiceDTO.getVehicleStoppageDetails().getMviNumber();
						//email = regServiceDTO.getFreshRcdetails().getFinancerContactDetails().getEmail();
					}
					if (templateId == MessageTemplate.STOPPAGEDTC.getId()) {
						mobile = regServiceDTO.getVehicleStoppageDetails().getDtcNumber();
						//email = regServiceDTO.getFreshRcdetails().getFinancerContactDetails().getEmail();
					}
					if (templateId == MessageTemplate.STOPPAGEAUTOAPPROVEDFORMVI.getId()) {
						mobile = regServiceDTO.getVehicleStoppageDetails().getMviNumber();
						//email = regServiceDTO.getFreshRcdetails().getFinancerContactDetails().getEmail();
					}
					if (templateId == MessageTemplate.STOPPAGEAUTOAPPROVEDFORCITIZEN.getId()) {
						mobile = regServiceDTO.getRegistrationDetails().getApplicantDetails().getContact().getMobile();
						//email = regServiceDTO.getFreshRcdetails().getFinancerContactDetails().getEmail();
					}
				}

				// for Financier After Approval

				else if (object instanceof UserDTO) {
					UserDTO userDto = (UserDTO) object;
					email = userDto.getEmail();
					mobile = userDto.getMobile();
				}
				/**
				 * for Financier During Application Processing (initiated / CCO Rejected / CCO
				 * approved/ AO Rejected / Re Upload)
				 */

				else if (object instanceof FinancierCreateRequestDTO) {
					FinancierCreateRequestDTO userDto = (FinancierCreateRequestDTO) object;
					email = userDto.getEmail();
					mobile = userDto.getMobile();
				}
				
				else if (object instanceof DealerRegDTO) {
					DealerRegDTO userDto = (DealerRegDTO) object;
					email = userDto.getApplicantDetails().getContact().getEmail();
					mobile = userDto.getApplicantDetails().getContact().getMobile();
				}
				
				else if (object instanceof DispatcherSubmissionDTO) {
					DispatcherSubmissionDTO userDto = (DispatcherSubmissionDTO) object;
					//email = userDto.get.getContact().getEmail();
					
					mobile = userDto.getMobileNo();
				}

				this.sendEmailNotification(notification::fillTemplate, templateId, object, email);
				this.sendMessageNotification(notification::fillTemplate, templateId, object, mobile);
			}

		} catch (Exception e) {
			// logger.error("Failed to send notifications for template id: {}; {}",
			// templateId, e);
		}

	}
}