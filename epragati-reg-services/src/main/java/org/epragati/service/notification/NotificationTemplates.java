package org.epragati.service.notification;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.BiFunction;

import org.apache.commons.lang3.StringUtils;
import org.epragati.common.dao.NotificationTemplateDAO;
import org.epragati.common.dto.NotificationTemplateDTO;
import org.epragati.constants.TransferType;
import org.epragati.dispatcher.dto.DispatcherSubmissionDTO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dto.DealerRegDTO;
import org.epragati.master.dto.FinancierCreateRequestDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.mobile.auth.dto.AuthenticationOTP_DTO;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.sn.dto.SpecialNumberDetailsDTO;
import org.epragati.util.DateConverters;
import org.epragati.util.payment.ServiceEnum;
import org.epragati.vcrImage.dto.VcrFinalServiceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NotificationTemplates {

	private static final Logger LOG = LoggerFactory.getLogger(NotificationTemplates.class);

	@Autowired
	private NotificationTemplateDAO notificationTemplateDAO;

	// @Autowired
	// private BidDetailsService bidDetailsService;

	static final String EMAIL = "EMAIL";
	static final String SMS = "SMS";

	private Map<String, HashMap<String, String>> getTemplate(Integer templateId) {

		Map<String, HashMap<String, String>> content = new TreeMap<>();

		Optional<NotificationTemplateDTO> notificationTemplate = notificationTemplateDAO.findBytemplateId(templateId);
		if (notificationTemplate.isPresent()) {

			content.put(SMS, notificationTemplate.get().getConfigDetails().get(SMS));
			content.put(EMAIL, notificationTemplate.get().getConfigDetails().get(EMAIL));

		} else
			return null;

		return content;
	}

	public <T> Map<String, HashMap<String, String>> fillTemplate(
			BiFunction<Integer, T, Map<String, HashMap<String, String>>> fun, Integer serviceId, T object) {
		return fun.apply(serviceId, object);
	}

	public Map<String, HashMap<String, String>> fillTemplate(Integer templateId, Object object) {

		try {

			StagingRegistrationDetailsDTO stagingRegistrationDetailsDTO = null;
			SpecialNumberDetailsDTO specialNumberDetailsDTO = null;
			RegServiceDTO regServiceDTO = null;
			AuthenticationOTP_DTO authenticationOTP_DTO = null;
			// For Financier AO Approved
			UserDTO userDTO = null;
			// For financier Application Status
			FinancierCreateRequestDTO finDto = null;
			// For VCR IMPL.
			VcrFinalServiceDTO vcrImpl = null;
			
			DealerRegDTO dealerRegDTO = null;
			
			DispatcherSubmissionDTO dispatcherSubmissionDTO = null;

			if (object instanceof StagingRegistrationDetailsDTO) {
				stagingRegistrationDetailsDTO = (StagingRegistrationDetailsDTO) object;
			} else if (object instanceof SpecialNumberDetailsDTO) {
				specialNumberDetailsDTO = (SpecialNumberDetailsDTO) object;
			} else if (object instanceof RegServiceDTO) {
				regServiceDTO = (RegServiceDTO) object;
			} else if (object instanceof AuthenticationOTP_DTO) {
				authenticationOTP_DTO = (AuthenticationOTP_DTO) object;
			}
			
			// For Financier Application Approval
			else if (object instanceof UserDTO) {
				userDTO = (UserDTO) object;
			}

			// For Financier Application Status
			else if (object instanceof FinancierCreateRequestDTO) {
				finDto = (FinancierCreateRequestDTO) object;
			} else if (object instanceof VcrFinalServiceDTO) {
				vcrImpl = (VcrFinalServiceDTO) object;
			}
			else if (object instanceof DealerRegDTO) {
				dealerRegDTO = (DealerRegDTO) object;
			}
			
			else if (object instanceof DispatcherSubmissionDTO) {
				dispatcherSubmissionDTO = (DispatcherSubmissionDTO) object;
			}

			Map<String, HashMap<String, String>> template = this.getTemplate(templateId);

			if (template.isEmpty()) {
				LOG.error(" No templated found");
				return null;
			}

			String sms = template.get(SMS).get(NotificationKeyWords.MESSAGE.getValue());
			String email = template.get(EMAIL).get(NotificationKeyWords.MESSAGE.getValue());
			String subject = template.get(EMAIL).get(NotificationKeyWords.SUBJECT.getValue());
			String transctionId = StringUtils.EMPTY;

			if (templateId.equals(MessageTemplate.NEW_REG_TR.getId())) {

				sms = sms.replaceAll("@@TRNO@@", stagingRegistrationDetailsDTO.getTrNo());
				sms = sms.replaceAll("@@Citizen@@",
						stagingRegistrationDetailsDTO.getApplicantDetails().getAadharResponse().getName());
				email = email.replaceAll("@@TRNO@@", stagingRegistrationDetailsDTO.getTrNo());
				email = email.replaceAll("@@Citizen@@",
						stagingRegistrationDetailsDTO.getApplicantDetails().getAadharResponse().getName());
				transctionId = stagingRegistrationDetailsDTO.getApplicationNo();
			}
			/**
			 * Template For Financier After Approval Process SUCCESS BY AO
			 */
			if (templateId.equals(MessageTemplate.FIN_REGN_SUCCESS.getId())) {

				String userId = userDTO.getUserId();
				if (userId == null)
					userId = "";
				String password = userDTO.getPassword();
				if (password == null)
					password = " ";
				String userName = userDTO.getRepresentativeName();
				if (userName == null)
					userName = "Citizen";

				sms = sms.replaceAll("@@APPNO@@", userId);
				sms = sms.replaceAll("@@Citizen@@", userName);
				sms = sms.replaceAll("@@PASSWORD@@", password);
				email = email.replaceAll("@@APPNO@@", userId);
				email = email.replaceAll("@@Citizen@@", userName);
				email = email.replaceAll("@@PASSWORD@@", password);
				LOG.info("Processed Template [{}] in NotificationTemplates::",
						MessageTemplate.FIN_REGN_SUCCESS.getId());
			}
			/**
			 *
			 * Template For Financier STATUS
			 */

			if (templateId.equals(MessageTemplate.FIN_REGN_STATUS.getId())) {

				String applStatus = finDto.getApplicationStatus();
				if (applStatus == null)
					applStatus = "";

				String userName = finDto.getFirstName();
				if (userName == null)
					userName = finDto.getRepresentativeName();
				if (userName == null)
					userName = userDTO.getUserName();

				String applicationNo = finDto.getFinAppNo();
				if (applicationNo == null)
					applicationNo = "";

				sms = sms.replaceAll("@@Citizen@@", userName);
				sms = sms.replaceAll("@@APPNO@@", applicationNo);
				sms = sms.replaceAll("@@APPLICATION-STATUS@@", applStatus);
				sms = sms.replaceAll("INITIATED", "SUCCESSFULLY APPLIED FOR FINANCIER REGISTRATION");
				sms = sms.replaceAll("CCO APPROVED", "APPROVED BY CCO, PENDING AT AO LEVEL");
				sms = sms.replaceAll("CCO REJECTED", "PENDING AT AO LEVEL");
				sms = sms.replaceAll("AO REJECTED", "REJECTED BY AO, PLEASE CHECK STATUS IN PORTAL");
				sms = sms.replaceAll("AO APPROVED",
						"SUCCESSFULLY APPROVED, YOUR USERNAME AND PASSWORD WILL BE SENT TO MOBILE/EMAIL");

				email = email.replaceAll("@@Citizen@@", userName);
				email = email.replaceAll("@@APPNO@@", applicationNo);
				email = email.replaceAll("@@APPLICATION-STATUS@@", applStatus);
				email = email.replaceAll("INITIATED", "SUCCESSFULLY APPLIED FOR FINANCIER REGISTRATION");
				email = email.replaceAll("CCO APPROVED", "APPROVED BY CCO, PENDING AT AO LEVEL");
				email = email.replaceAll("CCO REJECTED", "PENDING AT AO LEVEL");
				email = email.replaceAll("AO REJECTED", "REJECTED BY AO, PLEASE CHECK STATUS IN PORTAL");
				email = email.replaceAll("AO APPROVED",
						"SUCCESSFULLY APPROVED, YOUR USERNAME AND PASSWORD WILL BE SENT TO MOBILE/EMAIL");

				LOG.info("Processed Template [{}] in NotificationTemplates::", MessageTemplate.FIN_REGN_STATUS.getId());
			}
			if (templateId.equals(MessageTemplate.OFFICER_REG.getId())) {
				sms = sms.replaceAll("@@Citizen@@", userDTO.getFirstName());
				sms = sms.replaceAll("@@USERNAME@@", userDTO.getUserId());
				// sms = sms.replaceAll("@@PASSWORD@@", userDTO.getPassword());
				email = email.replaceAll("@@Citizen@@", userDTO.getFirstName());
				email = email.replaceAll("@@USERNAME@@", userDTO.getUserId());
				// email = email.replaceAll("@@PASSWORD@@", userDTO.getPassword());
			}

			if (templateId.equals(MessageTemplate.NEW_REG_PR.getId())
					|| templateId.equals(MessageTemplate.SMART_CARD_PRINT.getId())) {

				sms = sms.replaceAll("@@PRNO@@", stagingRegistrationDetailsDTO.getPrNo());
				sms = sms.replaceAll("@@Citizen@@",
						stagingRegistrationDetailsDTO.getApplicantDetails().getAadharResponse().getName());
				email = email.replaceAll("@@PRNO@@", stagingRegistrationDetailsDTO.getPrNo());
				email = email.replaceAll("@@Citizen@@",
						stagingRegistrationDetailsDTO.getApplicantDetails().getAadharResponse().getName());
				transctionId = stagingRegistrationDetailsDTO.getApplicationNo();

			}
			if (templateId.equals(MessageTemplate.SP_NUM_PASSCODE.getId())) {
				sms = sms.replaceAll("@@TRNO@@", specialNumberDetailsDTO.getVehicleDetails().getTrNumber());
				// sms = sms.replaceAll("@@Citizen@@",
				// specialNumberDetailsDto.getCustomerDetails().getFirstName());
				sms = sms.replaceAll("@@PASSCODE@@", specialNumberDetailsDTO.getPasscode());

				email = email.replaceAll("@@TRNO@@", specialNumberDetailsDTO.getVehicleDetails().getTrNumber());
				// email = email.replaceAll("@@Citizen@@",
				// specialNumberDetailsDto.getCustomerDetails().getFirstName());
				email = email.replaceAll("@@PASSCODE@@", specialNumberDetailsDTO.getPasscode());
				transctionId = specialNumberDetailsDTO.getSpecialNumberAppId();
			}
			if (templateId.equals(MessageTemplate.FIN_TOKEN_NO.getId())) {
				sms = sms.replaceAll("@@TOKEN_NO@@", stagingRegistrationDetailsDTO.getFinanceDetails().getToken());
				sms = sms.replaceAll("@@APPLICATION_NO@@", stagingRegistrationDetailsDTO.getApplicationNo());
				email = email.replaceAll("@@TOKEN_NO@@", stagingRegistrationDetailsDTO.getFinanceDetails().getToken());
				email = email.replaceAll("@@APPLICATION_NO@@", stagingRegistrationDetailsDTO.getApplicationNo());
				transctionId = stagingRegistrationDetailsDTO.getApplicationNo();
			}
			if (templateId.equals(MessageTemplate.SP_BID_INTIMATION.getId())) {

				sms = sms.replaceAll("@@Citizen@@", specialNumberDetailsDTO.getCustomerDetails().getFirstName());
				sms = sms.replaceAll("@@APPLICATION_NO@@",
						specialNumberDetailsDTO.getVehicleDetails().getApplicationNumber());
				email = email.replaceAll("@@Citizen@@", specialNumberDetailsDTO.getCustomerDetails().getFirstName());
				email = email.replaceAll("@@APPLICATION_NO@@",
						specialNumberDetailsDTO.getVehicleDetails().getApplicationNumber());
				transctionId = specialNumberDetailsDTO.getVehicleDetails().getApplicationNumber();
			}

			if (templateId == MessageTemplate.AADHAAR_APPROVED.getId()
					|| templateId == MessageTemplate.AADHAAR_AUTO_APPROVED.getId()
					|| templateId == MessageTemplate.AADHAAR_INITIATED.getId()
					|| templateId == MessageTemplate.AADHAAR_OPENED.getId()
					|| templateId == MessageTemplate.AADHAAR_REJECTED.getId()) {
				RegistrationDetailsDTO regDetails = (RegistrationDetailsDTO) object;
				sms = sms.replace("@@User@@", regDetails.getApplicantDetails().getDisplayName());
				email = email.replace("@@User@@", regDetails.getApplicantDetails().getDisplayName());
			}

			if (templateId.equals(MessageTemplate.NEW_REG_PR_REJECTED.getId())) {

				sms = sms.replaceAll("@@TRNO@@", stagingRegistrationDetailsDTO.getTrNo());
				sms = sms.replaceAll("@@Citizen@@",
						stagingRegistrationDetailsDTO.getApplicantDetails().getAadharResponse().getName());
				email = email.replaceAll("@@TRNO@@", stagingRegistrationDetailsDTO.getTrNo());
				email = email.replaceAll("@@Citizen@@",
						stagingRegistrationDetailsDTO.getApplicantDetails().getAadharResponse().getName());
				transctionId = stagingRegistrationDetailsDTO.getApplicationNo();
			}
			if (templateId.equals(MessageTemplate.SLOTBOOKED.getId())) {
				sms = sms.replaceAll("@@Citizen@@",
						stagingRegistrationDetailsDTO.getApplicantDetails().getAadharResponse().getName());
				sms = sms.replaceAll("@@Date@@",
						DateConverters
								.convertLocalDateFormat(stagingRegistrationDetailsDTO.getSlotDetails().getSlotDate())
								.toString());
				sms = sms.replaceAll("@@Time@@", stagingRegistrationDetailsDTO.getSlotDetails().getSlotTime().trim());
				sms = sms.replaceAll("@@APPLICATION N0@@", stagingRegistrationDetailsDTO.getApplicationNo());
				sms = sms.replaceAll("@@Office Address@@", this.addressData(stagingRegistrationDetailsDTO));

				email = email.replaceAll("@@Citizen@@",
						stagingRegistrationDetailsDTO.getApplicantDetails().getAadharResponse().getName());
				email = email.replaceAll("@@Date@@",
						DateConverters
								.convertLocalDateFormat(stagingRegistrationDetailsDTO.getSlotDetails().getSlotDate())
								.toString());
				email = email.replaceAll("@@Time@@", stagingRegistrationDetailsDTO.getSlotDetails().getSlotTime());
				email = email.replaceAll("@@APPLICATION N0@@", stagingRegistrationDetailsDTO.getApplicationNo());
				email = email.replaceAll("@@Office Address@@", this.addressData(stagingRegistrationDetailsDTO));
				transctionId = stagingRegistrationDetailsDTO.getApplicationNo();
			}

			if (templateId.equals(MessageTemplate.SLOTMODIFICATION.getId())) {
				sms = sms.replaceAll("@@Citizen@@",
						stagingRegistrationDetailsDTO.getApplicantDetails().getAadharResponse().getName());
				sms = sms.replaceAll("@@Date@@",
						DateConverters
								.convertLocalDateFormat(stagingRegistrationDetailsDTO.getSlotDetails().getSlotDate())
								.toString());
				sms = sms.replaceAll("@@Time@@", stagingRegistrationDetailsDTO.getSlotDetails().getSlotTime());
				sms = sms.replaceAll("@@APPLICATION N0@@", stagingRegistrationDetailsDTO.getApplicationNo());
				sms = sms.replaceAll("@@Office Address@@", this.addressData(stagingRegistrationDetailsDTO));

				email = email.replaceAll("@@Citizen@@",
						stagingRegistrationDetailsDTO.getApplicantDetails().getAadharResponse().getName());
				email = email.replaceAll("@@Date@@",
						DateConverters
								.convertLocalDateFormat(stagingRegistrationDetailsDTO.getSlotDetails().getSlotDate())
								.toString());
				email = email.replaceAll("@@Time@@", stagingRegistrationDetailsDTO.getSlotDetails().getSlotTime());
				email = email.replaceAll("@@APPLICATION N0@@", stagingRegistrationDetailsDTO.getApplicationNo());
				email = email.replaceAll("@@Office Address@@", this.addressData(stagingRegistrationDetailsDTO));
				transctionId = stagingRegistrationDetailsDTO.getApplicationNo();
			}
			List<Integer> bidTemplateIds = Arrays.asList(MessageTemplate.SP_BID_WIN.getId(),
					MessageTemplate.SP_BID_LOOSE.getId(), MessageTemplate.SP_BID_TIE.getId(),
					MessageTemplate.SP_BID_LIMITEXCEED.getId(), MessageTemplate.SP_NUM_PASSCODE.getId(),
					MessageTemplate.SP_BID_INTIMATION.getId());
			if (bidTemplateIds.contains(templateId)) {
				sms = sms.replaceAll("@@Citizen@@", specialNumberDetailsDTO.getCustomerDetails().getFirstName());
				email = email.replaceAll("@@Citizen@@", specialNumberDetailsDTO.getCustomerDetails().getFirstName());
				sms = sms.replaceAll("@@REG_NO@@", specialNumberDetailsDTO.getSelectedPrSeries());
				email = email.replaceAll("@@REG_NO@@", specialNumberDetailsDTO.getSelectedPrSeries());

			}
			// regarding suspension,cancellation and revocation
			if (templateId.equals(MessageTemplate.RC_REVOKATION.getId())) {

				sms = sms.replace("@@User@@", stagingRegistrationDetailsDTO.getApplicantDetails().getDisplayName());
				sms = sms.replace("@@RC@@", stagingRegistrationDetailsDTO.getPrNo());
				email = email.replace("@@User@@", stagingRegistrationDetailsDTO.getApplicantDetails().getDisplayName());
				email = email.replace("@@RC@@", stagingRegistrationDetailsDTO.getPrNo());

			}
			if (templateId.equals(MessageTemplate.RC_CANCELLATION.getId())) {
				sms = sms.replace("@@User@@", regServiceDTO.getRegistrationDetails().getApplicantDetails().getDisplayName());
				sms = sms.replace("@@RC@@", regServiceDTO.getPrNo());
				email = email.replace("@@User@@", regServiceDTO.getRegistrationDetails().getApplicantDetails().getDisplayName());
				email = email.replace("@@RC@@", regServiceDTO.getPrNo());
			}
			if (templateId.equals(MessageTemplate.RC_SUSPENSION.getId())) {
				sms = sms.replace("@@User@@", stagingRegistrationDetailsDTO.getApplicantDetails().getDisplayName());
				sms = sms.replace("@@RC@@", stagingRegistrationDetailsDTO.getPrNo());
				email = email.replace("@@User@@", stagingRegistrationDetailsDTO.getApplicantDetails().getDisplayName());
				email = email.replace("@@RC@@", stagingRegistrationDetailsDTO.getPrNo());
			}
			// permit related suspension,cancellation and revocation
			if (templateId.equals(MessageTemplate.P_REVOKATION.getId())) {

				sms = sms.replace("@@User@@", stagingRegistrationDetailsDTO.getApplicantDetails().getDisplayName());
				sms = sms.replace("@@RC@@", stagingRegistrationDetailsDTO.getPrNo());
				email = email.replace("@@User@@", stagingRegistrationDetailsDTO.getApplicantDetails().getDisplayName());
				email = email.replace("@@RC@@", stagingRegistrationDetailsDTO.getPrNo());

			}
			if (templateId.equals(MessageTemplate.P_CANCELLATION.getId())) {
				sms = sms.replace("@@User@@", stagingRegistrationDetailsDTO.getApplicantDetails().getDisplayName());
				sms = sms.replace("@@RC@@", stagingRegistrationDetailsDTO.getPrNo());
				email = email.replace("@@User@@", stagingRegistrationDetailsDTO.getApplicantDetails().getDisplayName());
				email = email.replace("@@RC@@", stagingRegistrationDetailsDTO.getPrNo());
			}
			if (templateId.equals(MessageTemplate.P_SUSPENSION.getId())) {
				sms = sms.replace("@@User@@", stagingRegistrationDetailsDTO.getApplicantDetails().getDisplayName());
				sms = sms.replace("@@RC@@", stagingRegistrationDetailsDTO.getPrNo());
				email = email.replace("@@User@@", stagingRegistrationDetailsDTO.getApplicantDetails().getDisplayName());
				email = email.replace("@@RC@@", stagingRegistrationDetailsDTO.getPrNo());
			}
			if (templateId.equals(MessageTemplate.CITIZEN_MOBILE_APP_OTP.getId())) {
				sms = sms.replace("@@User@@", authenticationOTP_DTO.getDisplayName());
				sms = sms.replace("@@OTP@@", String.valueOf(authenticationOTP_DTO.getOtp()));
				transctionId = authenticationOTP_DTO.getAadharNo();
			}
			// Regarding FC Slot Book
			if (templateId.equals(MessageTemplate.REG_FCSLOT.getId())) {
				sms = sms.replaceAll("@@Citizen@@",
						regServiceDTO.getRegistrationDetails().getApplicantDetails().getDisplayName());
				sms = sms.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().get(0).toString());
				sms = sms.replaceAll("@@Date@@",
						DateConverters.convertLocalDateFormat(regServiceDTO.getSlotDetails().getSlotDate()).toString());
				sms = sms.replaceAll("@@Time@@", regServiceDTO.getSlotDetails().getSlotTime());
				sms = sms.replaceAll("@@APPLICATION N0@@", regServiceDTO.getApplicationNo());
				sms = sms.replaceAll("@@Office Address@@", this.officeAddress(regServiceDTO));

				email = email.replaceAll("@@Citizen@@",
						regServiceDTO.getRegistrationDetails().getApplicantDetails().getDisplayName());
				email = email.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().get(0).toString());
				email = email.replaceAll("@@Date@@",
						DateConverters.convertLocalDateFormat(regServiceDTO.getSlotDetails().getSlotDate()).toString());
				email = email.replaceAll("@@Time@@", regServiceDTO.getSlotDetails().getSlotTime());
				email = email.replaceAll("@@APPLICATION N0@@", regServiceDTO.getApplicationNo());
				email = email.replaceAll("@@Office Address@@", this.officeAddress(regServiceDTO));
				transctionId = regServiceDTO.getApplicationNo();
			}
			// Regarding REG Service Updation
			List<Integer> regServiceTemplateIds = Arrays.asList(MessageTemplate.REG_APPROVAL.getId(),
					MessageTemplate.REG_REJECTED.getId(), MessageTemplate.REG_TAXPENDING.getId(),
					MessageTemplate.REG_REUPLOAD.getId(), MessageTemplate.REG_FC.getId());
			if (regServiceTemplateIds.contains(templateId)) {
				sms = sms.replaceAll("@@Citizen@@",
						regServiceDTO.getRegistrationDetails().getApplicantDetails().getDisplayName());
				sms = sms.replaceAll("@@PRNO@@", regServiceDTO.getPrNo());
				sms = sms.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().get(0).toString());
				sms = sms.replaceAll("@@APPLICATION N0@@", regServiceDTO.getApplicationNo());
				email = email.replaceAll("@@Citizen@@",
						regServiceDTO.getRegistrationDetails().getApplicantDetails().getDisplayName());
				email = email.replaceAll("@@PRNO@@", regServiceDTO.getPrNo());
				email = email.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().get(0).toString());
				email = email.replaceAll("@@APPLICATION N0@@", regServiceDTO.getApplicationNo());
			}
			if (templateId.equals(MessageTemplate.TOW_TOKENCANCEL_FIN_APPROVED.getId())
					|| templateId.equals(MessageTemplate.TOW_TOKENCANCEL_FIN_APPROVED.getId())) {
				sms = sms.replaceAll("@@User@@", regServiceDTO.getFinanceDetails().getFinancerName());
				sms = sms.replaceAll("@@TokenNo@@", regServiceDTO.getBuyerDetails().getTokenNo());

				email = email.replaceAll("@@User@@", regServiceDTO.getFinanceDetails().getFinancerName());
				email = email.replaceAll("@@TokenNo@@", regServiceDTO.getBuyerDetails().getTokenNo());

			}

			if (templateId.equals(MessageTemplate.TOW_TOKEN_GENERATED.getId())) {
				Map<String, String> content = getMessageContent(sms, email, regServiceDTO);
				sms = content.get("sms");
				email = content.get("email");
			}
			if (templateId.equals(MessageTemplate.TOW_BUYER.getId())) {
				Map<String, String> content = getMessageContent(sms, email, regServiceDTO);
				sms = content.get("sms");
				email = content.get("email");
			}
			if (templateId.equals(MessageTemplate.THEFT_INTIMATION.getId())) {
				Map<String, String> content = getMessageContent(sms, email, regServiceDTO);
				sms = content.get("sms");
				email = content.get("email");
			}

			if (templateId.equals(MessageTemplate.THEFT_REVOCATION.getId())) {
				Map<String, String> content = getMessageContent(sms, email, regServiceDTO);
				sms = content.get("sms");
				email = content.get("email");
			}

			if (templateId.equals(MessageTemplate.NOC_ISSUED.getId())) {
				Map<String, String> content = getMessageContent(sms, email, regServiceDTO);
				sms = content.get("sms");
				email = content.get("email");
			}

			if (templateId.equals(MessageTemplate.NOC_CANCELLED.getId())) {
				Map<String, String> content = getMessageContent(sms, email, regServiceDTO);
				sms = content.get("sms");
				email = content.get("email");
			}

			if (templateId.equals(MessageTemplate.HPA_TOKEN_GENERATED.getId())) {
				Map<String, String> content = getMessageContent(sms, email, regServiceDTO);
				sms = content.get("sms");
				email = content.get("email");
			}

			if (templateId.equals(MessageTemplate.HPT.getId())) {
				Map<String, String> content = getMessageContent(sms, email, regServiceDTO);
				sms = content.get("sms");
				email = content.get("email");
			}

			if (templateId.equals(MessageTemplate.RENWAL_RC.getId())) {
				Map<String, String> content = getMessageContent(sms, email, regServiceDTO);
				sms = content.get("sms");
				email = content.get("email");
			}

			if (templateId.equals(MessageTemplate.ALTERATION_VEHICLE.getId())) {
				Map<String, String> content = getMessageContent(sms, email, regServiceDTO);
				sms = content.get("sms");
				email = content.get("email");
			}

			if (templateId.equals(MessageTemplate.CHANGE_OF_ADDRESS.getId())) {

				Map<String, String> content = getMessageContent(sms, email, regServiceDTO);
				sms = content.get("sms");
				email = content.get("email");
			}

			if (templateId.equals(MessageTemplate.HPT.getId())) {
				Map<String, String> content = getMessageContent(sms, email, regServiceDTO);
				sms = content.get("sms");
				email = content.get("email");
			}

			if (templateId.equals(MessageTemplate.DUPLICATE_RC.getId())) {
				Map<String, String> content = getMessageContent(sms, email, regServiceDTO);
				sms = content.get("sms");
				email = content.get("email");
			}

			if (templateId.equals(MessageTemplate.NEW_FC.getId())) {
				Map<String, String> content = getMessageContent(sms, email, regServiceDTO);
				sms = content.get("sms");
				email = content.get("email");
			}

			if (templateId.equals(MessageTemplate.PERMIT_NEW.getId())) {
				Map<String, String> content = getMessageContent(sms, email, regServiceDTO);
				sms = content.get("sms");
				email = content.get("email");
			}

			if (templateId.equals(MessageTemplate.RENEWAL_PERMIT.getId())) {
				Map<String, String> content = getMessageContent(sms, email, regServiceDTO);
				sms = content.get("sms");
				email = content.get("email");
			}

			if (templateId.equals(MessageTemplate.SURRENDER_PERMIT.getId())) {
				Map<String, String> content = getMessageContent(sms, email, regServiceDTO);
				sms = content.get("sms");
				email = content.get("email");
			}

			if (templateId.equals(MessageTemplate.TRANSFER_PERMIT.getId())) {
				Map<String, String> content = getMessageContent(sms, email, regServiceDTO);
				sms = content.get("sms");
				email = content.get("email");
			}

			if (templateId.equals(MessageTemplate.PERMIT_COA.getId())) {
				Map<String, String> content = getMessageContent(sms, email, regServiceDTO);
				sms = content.get("sms");
				email = content.get("email");
			}

			if (templateId.equals(MessageTemplate.DUPLICATE_RC.getId())) {
				Map<String, String> content = getMessageContent(sms, email, regServiceDTO);
				sms = content.get("sms");
				email = content.get("email");
			}

			if (templateId.equals(MessageTemplate.VARIATION_PERMIT.getId())) {
				Map<String, String> content = getMessageContent(sms, email, regServiceDTO);
				sms = content.get("sms");
				email = content.get("email");
			}
			if (templateId.equals(MessageTemplate.RENEWAL_AUTH_CARD.getId())) {
				Map<String, String> content = getMessageContent(sms, email, regServiceDTO);
				sms = content.get("sms");
				email = content.get("email");
			}
			if (templateId.equals(MessageTemplate.TOW_TOKEN_CANCELED.getId())) {
				sms = sms.replaceAll("@@User@@",
						regServiceDTO.getRegistrationDetails().getApplicantDetails().getDisplayName());
				sms = sms.replaceAll("@@TokenNo@@", regServiceDTO.getBuyerDetails().getTokenNo());

				email = email.replaceAll("@@User@@",
						regServiceDTO.getRegistrationDetails().getApplicantDetails().getDisplayName());
				email = email.replaceAll("@@TokenNo@@", regServiceDTO.getBuyerDetails().getTokenNo());
			}
			if (templateId.equals(MessageTemplate.REG_OTHERSTATEVEHICLEWITHPR.getId())) {
				sms = sms.replaceAll("@@Citizen@@",
						regServiceDTO.getRegistrationDetails().getApplicantDetails().getDisplayName());
				sms = sms.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().toString());

				email = email.replaceAll("@@Citizen@@",
						regServiceDTO.getRegistrationDetails().getApplicantDetails().getDisplayName());
				email = email.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().toString());
				sms = sms.replaceAll("@@APPLICATION N0@@", regServiceDTO.getApplicationNo());

				email = email.replaceAll("@@APPLICATION N0@@", regServiceDTO.getApplicationNo());
			}
			if (templateId.equals(MessageTemplate.REG_OSSECONDVEHICLEPAYMENTPENDING.getId())) {
				sms = sms.replaceAll("@@Citizen@@",
						regServiceDTO.getRegistrationDetails().getApplicantDetails().getDisplayName());
				sms = sms.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().toString());

				email = email.replaceAll("@@Citizen@@",
						regServiceDTO.getRegistrationDetails().getApplicantDetails().getDisplayName());
				email = email.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().toString());
				sms = sms.replaceAll("@@APPLICATION N0@@", regServiceDTO.getApplicationNo());

				email = email.replaceAll("@@APPLICATION N0@@", regServiceDTO.getApplicationNo());
			}
			if (templateId.equals(MessageTemplate.REG_OSSECONDVEHICLEPAYMENTDONE.getId())) {
				sms = sms.replaceAll("@@Citizen@@",
						regServiceDTO.getRegistrationDetails().getApplicantDetails().getDisplayName());
				sms = sms.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().toString());

				email = email.replaceAll("@@Citizen@@",
						regServiceDTO.getRegistrationDetails().getApplicantDetails().getDisplayName());
				email = email.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().toString());
				sms = sms.replaceAll("@@APPLICATION N0@@", regServiceDTO.getApplicationNo());

				email = email.replaceAll("@@APPLICATION N0@@", regServiceDTO.getApplicationNo());
			}
			if (templateId.equals(MessageTemplate.REG_OTHERSTATEPAYMENTPENDING.getId())) {
				sms = sms.replaceAll("@@Citizen@@",
						regServiceDTO.getRegistrationDetails().getApplicantDetails().getDisplayName());
				sms = sms.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().toString());

				email = email.replaceAll("@@Citizen@@",
						regServiceDTO.getRegistrationDetails().getApplicantDetails().getDisplayName());
				email = email.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().toString());
				sms = sms.replaceAll("@@APPLICATION N0@@", regServiceDTO.getApplicationNo());

				email = email.replaceAll("@@APPLICATION N0@@", regServiceDTO.getApplicationNo());
			}

			if (templateId.equals(MessageTemplate.REG_FINANCIER_TOKEN_GENERATED.getId())) {
				sms = sms.replaceAll("@@Citizen@@",
						regServiceDTO.getRegistrationDetails().getApplicantDetails().getDisplayName());
				sms = sms.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().toString());
				sms = sms.replaceAll("@@APPLICATION N0@@", regServiceDTO.getApplicationNo());
				sms = sms.replaceAll("@@FINANCIER TOKEN NO@@", regServiceDTO.getToken());
				email = email.replaceAll("@@Citizen@@",
						regServiceDTO.getRegistrationDetails().getApplicantDetails().getDisplayName());
				email = email.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().toString());
				email = email.replaceAll("@@APPLICATION N0@@", regServiceDTO.getApplicationNo());
				email = email.replaceAll("@@FINANCIER TOKEN NO@@", regServiceDTO.getToken());
			}
			if (templateId.equals(MessageTemplate.VCR_REG.getId())) {
				sms = sms.replaceAll("@@User@@", vcrImpl.getOwnerDetails().getFullName());
				sms = sms.replace("@@AppNo@@", vcrImpl.getVcr().getVcrNumber());
			}
			if (templateId.equals(MessageTemplate.EWEBVCR.getId())) {
				//sms = sms.replaceAll("@@User@@", vcrImpl.getOwnerDetails().getFullName());
				if(vcrImpl.getRegistration()!=null &&  vcrImpl.getRegistration().getRegNo()!=null) {}
				sms = sms.replace("@@prNo@@",vcrImpl.getRegistration().getRegNo());
			}
			if (templateId.equals(MessageTemplate.RCFORFINANCE.getId())) {
				sms = sms.replaceAll("@@User@@", regServiceDTO.getFreshRcdetails().getFinancerUserId());
				sms = sms.replaceAll("@@serviceNames@@", regServiceDTO.getServiceType().toString());
				sms = sms.replaceAll("@@AppNo@@", regServiceDTO.getApplicationNo());
				email = email.replaceAll("@@User@@", regServiceDTO.getFreshRcdetails().getFinancerUserId());
				email = email.replaceAll("@@serviceNames@@", regServiceDTO.getServiceType().toString());
				email = email.replaceAll("@@AppNo@@", regServiceDTO.getApplicationNo());
			}
			if (templateId.equals(MessageTemplate.RCFORFINANCECIT.getId())) {
				sms = sms.replaceAll("@@User@@",
						regServiceDTO.getRegistrationDetails().getApplicantDetails().getFirstName());
				email = email.replaceAll("@@User@@",
						regServiceDTO.getRegistrationDetails().getApplicantDetails().getFirstName());
			}
			if (templateId.equals(MessageTemplate.RCFORFINANCECITAPP.getId())) {
				sms = sms.replaceAll("@@User@@",
						regServiceDTO.getRegistrationDetails().getApplicantDetails().getFirstName());
				email = email.replaceAll("@@User@@",
						regServiceDTO.getRegistrationDetails().getApplicantDetails().getFirstName());
			}
			if (templateId.equals(MessageTemplate.RCFORFINANCEFINAPP.getId())) {
				sms = sms.replaceAll("@@User@@", regServiceDTO.getFreshRcdetails().getFinancerUserId());
				email = email.replaceAll("@@User@@", regServiceDTO.getFreshRcdetails().getFinancerUserId());
			}
			if (templateId.equals(MessageTemplate.RCFORFINANCEFORM37CIT.getId())) {
				sms = sms.replaceAll("@@Citizen@@",
						regServiceDTO.getRegistrationDetails().getApplicantDetails().getFirstName());
				sms = sms.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().toString());
				sms = sms.replaceAll("@@prNo@@", regServiceDTO.getPrNo().toString());
				email = email.replaceAll("@@Citizen@@",
						regServiceDTO.getRegistrationDetails().getApplicantDetails().getFirstName());
				email = email.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().get(0).toString());
				email = email.replaceAll("@@prNo@@", regServiceDTO.getPrNo().toString());
			}
			if (templateId.equals(MessageTemplate.RCFORFINANCEFORM37FIN.getId())) {
				sms = sms.replaceAll("@@Citizen@@", regServiceDTO.getFreshRcdetails().getFinancerUserId());
				sms = sms.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().toString());
				sms = sms.replaceAll("@@prNo@@", regServiceDTO.getPrNo().toString());
				sms = sms.replaceAll("@@APPLICATION No@@", regServiceDTO.getApplicationNo());
				email = email.replaceAll("@@Citizen@@", regServiceDTO.getFreshRcdetails().getFinancerUserId());
				email = email.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().get(0).toString());
				email = email.replaceAll("@@APPLICATION No@@", regServiceDTO.getApplicationNo());
				email = email.replaceAll("@@prNo@@", regServiceDTO.getPrNo().toString());
			}


			if (templateId.equals(MessageTemplate.RCFORFINANCEREG.getId())) {
				sms = sms.replaceAll("@@Citizen@@",
						regServiceDTO.getFreshRcdetails().getFinancerUserId());
				sms = sms.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().toString());
				sms = sms.replaceAll("@@APPLICATION No@@", regServiceDTO.getApplicationNo());
				email = email.replaceAll("@@Citizen@@",
						regServiceDTO.getFreshRcdetails().getFinancerUserId());
				email = email.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().get(0).toString());
				email = email.replaceAll("@@APPLICATION No@@", regServiceDTO.getApplicationNo());
			}
			if (templateId.equals(MessageTemplate.FINANCIERNOTMATCH.getId())) {
				sms = sms.replaceAll("@@Citizen@@",
						regServiceDTO.getFreshRcdetails().getFinancerUserId());
				sms = sms.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().toString());
				sms = sms.replaceAll("@@APPLICATION No@@", regServiceDTO.getApplicationNo());
				sms = sms.replaceAll("@@prNo@@", regServiceDTO.getPrNo().toString());
				email = email.replaceAll("@@Citizen@@",
						regServiceDTO.getFreshRcdetails().getFinancerUserId());
				email = email.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().get(0).toString());
				email = email.replaceAll("@@APPLICATION No@@", regServiceDTO.getApplicationNo());
				email = email.replaceAll("@@PRNO@@", regServiceDTO.getPrNo());
			}
			if (templateId.equals(MessageTemplate.VEHICHLENOTREPOSSESED.getId())) {
				sms = sms.replaceAll("@@Citizen@@",
						regServiceDTO.getFreshRcdetails().getFinancerUserId());
				sms = sms.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().toString());
				sms = sms.replaceAll("@@APPLICATION No@@", regServiceDTO.getApplicationNo());
				sms = sms.replaceAll("@@prNo@@", regServiceDTO.getPrNo().toString());
				email = email.replaceAll("@@Citizen@@",
						regServiceDTO.getFreshRcdetails().getFinancerUserId());
				email = email.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().get(0).toString());
				email = email.replaceAll("@@APPLICATION No@@", regServiceDTO.getApplicationNo());
				email = email.replaceAll("@@PRNO@@", regServiceDTO.getPrNo());
			}
			if (templateId.equals(MessageTemplate.STOPPAGEMVI.getId())) {
				sms = sms.replaceAll("@@Citizen@@",
						regServiceDTO.getVehicleStoppageDetails().getUserId());
				sms = sms.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().toString());
				sms = sms.replaceAll("@@APPLICATION No@@", regServiceDTO.getApplicationNo());
				email = email.replaceAll("@@Citizen@@",
						regServiceDTO.getVehicleStoppageDetails().getUserId());
				email = email.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().get(0).toString());
				email = email.replaceAll("@@APPLICATION No@@", regServiceDTO.getApplicationNo());
			}
			if (templateId.equals(MessageTemplate.STOPPAGEDTC.getId())) {
				sms = sms.replaceAll("@@Citizen@@",
						regServiceDTO.getVehicleStoppageDetails().getUserId());
				sms = sms.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().toString());
				sms = sms.replaceAll("@@APPLICATION No@@", regServiceDTO.getApplicationNo());
				email = email.replaceAll("@@Citizen@@",
						regServiceDTO.getVehicleStoppageDetails().getUserId());
				email = email.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().get(0).toString());
				email = email.replaceAll("@@APPLICATION No@@", regServiceDTO.getApplicationNo());
			}
			if (templateId.equals(MessageTemplate.STOPPAGEAUTOAPPROVEDFORCITIZEN.getId())) {
				sms = sms.replaceAll("@@Citizen@@",
						regServiceDTO.getVehicleStoppageDetails().getUserId());
				sms = sms.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().toString());
				sms = sms.replaceAll("@@APPLICATION No@@", regServiceDTO.getApplicationNo());
				email = email.replaceAll("@@Citizen@@",
						regServiceDTO.getVehicleStoppageDetails().getUserId());
				email = email.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().get(0).toString());
				email = email.replaceAll("@@APPLICATION No@@", regServiceDTO.getApplicationNo());
			}
			if (templateId.equals(MessageTemplate.STOPPAGEAUTOAPPROVEDFORMVI.getId())) {
				sms = sms.replaceAll("@@Citizen@@",
						regServiceDTO.getVehicleStoppageDetails().getUserId());
				sms = sms.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().toString());
				sms = sms.replaceAll("@@APPLICATION No@@", regServiceDTO.getApplicationNo());
				email = email.replaceAll("@@Citizen@@",
						regServiceDTO.getVehicleStoppageDetails().getUserId());
				email = email.replaceAll("@@ServiceType@@", regServiceDTO.getServiceType().get(0).toString());
				email = email.replaceAll("@@APPLICATION No@@", regServiceDTO.getApplicationNo());
			}
			
			if (templateId.equals(MessageTemplate.DELERSHIP_APPROVED.getId())) {
				sms = sms.replaceAll("@@Citizen@@",
						dealerRegDTO.getApplicantDetails().getFirstName());
				sms = sms.replaceAll("@@ServiceType@@", dealerRegDTO.getServiceType().toString());
				sms = sms.replaceAll("@@APPLICATION No@@", dealerRegDTO.getApplicationNo());
				email = email.replaceAll("@@Citizen@@",
						dealerRegDTO.getApplicantDetails().getFirstName());
				email = email.replaceAll("@@ServiceType@@", dealerRegDTO.getServiceType().toString());
				email = email.replaceAll("@@APPLICATION No@@", dealerRegDTO.getApplicationNo());
			}
			
			if (templateId.equals(MessageTemplate.DELERSHIP_REJECTED.getId())) {
				sms = sms.replaceAll("@@Citizen@@",
						dealerRegDTO.getApplicantDetails().getFirstName());
				sms = sms.replaceAll("@@ServiceType@@", dealerRegDTO.getServiceType().toString());
				sms = sms.replaceAll("@@APPLICATION No@@", dealerRegDTO.getApplicationNo());
				email = email.replaceAll("@@Citizen@@",
						dealerRegDTO.getApplicantDetails().getFirstName());
				email = email.replaceAll("@@ServiceType@@", dealerRegDTO.getServiceType().toString());
				email = email.replaceAll("@@APPLICATION No@@", dealerRegDTO.getApplicationNo());
			}
			
			if (templateId.equals(MessageTemplate.CARDDISPATCH_EMS.getId())) {
				sms = sms.replaceAll("@@Citizen@@",
						dispatcherSubmissionDTO.getName());
				sms = sms.replaceAll("@@EMSNO@@", dispatcherSubmissionDTO.getEmsNumber());
				email = email.replaceAll("@@Citizen@@",
						dispatcherSubmissionDTO.getName());
				email = email.replaceAll("@@EMSNO@@", dispatcherSubmissionDTO.getEmsNumber());
			}
			
			template.get(SMS).put(NotificationKeyWords.transactionId.getValue(), transctionId);
			template.get(SMS).put(NotificationKeyWords.MESSAGE.getValue(), sms);
			template.get(EMAIL).put(NotificationKeyWords.MESSAGE.getValue(), email);
			template.get(EMAIL).put(NotificationKeyWords.transactionId.getValue(), transctionId);
			template.get(EMAIL).put(NotificationKeyWords.SUBJECT.getValue(), subject);
			return template;
		} catch (Exception e) {
			LOG.error("{}" + e);
			return null;
		}

	}

	@Autowired
	private OfficeDAO officeDAO;

	private String addressData(StagingRegistrationDetailsDTO regDetails) {
		String address = null;
		if (regDetails.getApplicantDetails() != null && regDetails.getApplicantDetails().getPresentAddress() != null
				&& regDetails.getApplicantDetails().getPresentAddress().getMandal() != null
				&& regDetails.getApplicantDetails().getPresentAddress().getMandal().getHsrpoffice() != null) {
			Optional<OfficeDTO> officeDetailsOptional = officeDAO
					.findByOfficeCode(regDetails.getApplicantDetails().getPresentAddress().getMandal().getHsrpoffice());
			if (officeDetailsOptional.isPresent()) {
				address = officeDetailsOptional.get().getOfficeName() + officeDetailsOptional.get().getOfficeAddress1();
			}
		}
		return address;
	}

	private String officeAddress(RegServiceDTO regServiceDTO) {
		String address = null;
		if (regServiceDTO.getMviOfficeDetails() != null && regServiceDTO.getMviOfficeDetails().getOfficeName() != null
				&& regServiceDTO.getMviOfficeDetails().getOfficeAddress1() != null) {
			address = regServiceDTO.getMviOfficeDetails().getOfficeName()
					+ regServiceDTO.getMviOfficeDetails().getOfficeAddress1();
		}
		return address;

	}

	public <T> Map<String, HashMap<String, String>> dlTemplate(
			BiFunction<Integer, T, Map<String, HashMap<String, String>>> fun, Integer serviceId, T object) {
		return fun.apply(serviceId, object);
	}

	private Map<String, String> getMessageContent(String sms, String email, RegServiceDTO regServiceDTO) {
		Map<String, String> content = new HashMap<>();
		if (regServiceDTO.getRegistrationDetails() != null
				&& regServiceDTO.getRegistrationDetails().getApplicantDetails() != null) {
			sms = sms.replaceAll("@@User@@",
					regServiceDTO.getRegistrationDetails().getApplicantDetails().getDisplayName());
			String appNo = StringUtils.EMPTY;
			if (regServiceDTO.getToken() != null) {
				appNo = regServiceDTO.getApplicationNo() + " and token no : " + regServiceDTO.getToken();
			} else if (regServiceDTO.getBuyerDetails() != null
					&& regServiceDTO.getBuyerDetails().getTransferType().equals(TransferType.SALE)
					&& regServiceDTO.getBuyerDetails().getBuyer() == null) {
				appNo = regServiceDTO.getApplicationNo() + " & token no : "
						+ regServiceDTO.getBuyerDetails().getTokenNo();
			} else {
				appNo = regServiceDTO.getApplicationNo();
			}

			sms = sms.replaceAll("@@serviceNames@@", regServiceDTO.getServiceType().toString());
			sms = sms.replaceAll("@@AppNo@@", appNo);
			email = email.replaceAll("@@User@@",
					regServiceDTO.getRegistrationDetails().getApplicantDetails().getDisplayName());

			email = email.replaceAll("@@serviceNames@@", regServiceDTO.getServiceType().toString());
			email = email.replaceAll("@@AppNo@@", appNo);
			if (regServiceDTO.getServiceIds().contains(ServiceEnum.TRANSFEROFOWNERSHIP.getId())
					&& regServiceDTO.getBuyerDetails() != null && regServiceDTO.getBuyerDetails().getBuyer() != null) {
				sms = sms.replaceAll("@@serviceNames@@",
						regServiceDTO.getBuyerDetails().getBuyerServiceType().toString());
				email = email.replaceAll("@@serviceNames@@",
						regServiceDTO.getBuyerDetails().getBuyerServiceType().toString());
			}
			content.put("sms", sms);
			content.put("email", email);
		}

		return content;
	}

}
