package org.epragati.rta.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.constants.CovCategory;
import org.epragati.constants.MessageKeys;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.CardDispatchDetailsDAO;
import org.epragati.master.dao.MasterCovDAO;
import org.epragati.master.dao.RegServiceDAO;
import org.epragati.master.dao.RegistrationCardPrintDAO;
import org.epragati.master.dao.TaxDetailsDAO;
import org.epragati.master.dto.CardDispatchDetailsDTO;
import org.epragati.master.dto.MasterCovDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.TaxComponentDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.mappers.CardDispatchDetailsMapper;
import org.epragati.master.mappers.RegistrationCardDetailsMapper;
import org.epragati.regservice.RegistrationService;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.rta.service.impl.service.RegistrationCardService;
import org.epragati.rta.vo.CardDispatchDetailsVO;
import org.epragati.rta.vo.RegistrationCardDetailsVO;
import org.epragati.rta.vo.RegistrationCardPrintedVO;
import org.epragati.rta.vo.SmartCardPrintedVO;
import org.epragati.service.notification.MessageTemplate;
import org.epragati.service.notification.NotificationTemplates;
import org.epragati.service.notification.NotificationUtil;
import org.epragati.util.AppMessages;
import org.epragati.util.KeyValue;
import org.epragati.util.StatusRegistration;
import org.epragati.util.payment.ServiceCodeEnum;
import org.epragati.util.payment.ServiceEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

/**
 * 
 * @author saroj.sahoo
 *
 */
@Service
public class RegistrationCardServiceImpl implements RegistrationCardService {

	private static final Logger logger = LoggerFactory.getLogger(RegistrationCardServiceImpl.class);

	@Autowired
	private AppMessages appMessages;

	@Autowired
	private RegistrationCardPrintDAO registrationCardPrintDAO;

	@Autowired
	private RegistrationCardDetailsMapper registrationCardDetailsMapper;

	@Autowired
	private CardDispatchDetailsMapper cardDispatchDetailsMapper;

	@Autowired
	private CardDispatchDetailsDAO cardDispatchDetailsDAO;

	@Autowired
	private TaxDetailsDAO taxDetailsDAO;

	@Autowired
	private RegistrationService registrationService;

	@Autowired
	private MasterCovDAO masterCovDAO;

	@Autowired
	private RegServiceDAO regServiceDAO;
	
	@Autowired
	private NotificationUtil notifications;
	
	@Autowired
	private NotificationTemplates notificationTemplate;

	@Override
	public Optional<RegistrationCardDetailsVO> findRegistrationDetails(String prNo, String officeCode) {
		logger.info("findRegistrationDetails(), Inputs prNo: {}, officeCode: {}", prNo, officeCode);
		List<RegistrationDetailsDTO> registrationDetailsDTOList = registrationCardPrintDAO
				.findFirst10ByPrNoAndOfficeDetailsOfficeCodeAndVehicleType(prNo, officeCode, CovCategory.N.getCode());
		if (!registrationDetailsDTOList.isEmpty()) {
			Optional<RegistrationCardDetailsVO> registrationCardDetailsVO = Optional.empty();
			registrationDetailsDTOList.sort((r1, r2) -> r2.getlUpdate().compareTo(r1.getlUpdate()));
			Optional<RegistrationDetailsDTO> registrationDetailsDTOOpt = registrationDetailsDTOList.stream().findFirst();
			if (checkIsRCRequired(registrationDetailsDTOOpt.get().getClassOfVehicle()) && checkIsServiceIdExists(registrationDetailsDTOOpt.get()).getFirst()) {
				if (checkAllRCValidations(registrationDetailsDTOOpt.get())) {
					return registrationCardDetailsMapper.convertEntity(registrationDetailsDTOOpt);
				}
			}
			return registrationCardDetailsVO;
		}
		logger.warn(appMessages.getLogMessage(MessageKeys.CARDPRINT_GETTINGNULLVALUES_PRNUMBER_OFFICECODE), prNo,
				officeCode);
		return Optional.empty();
	}

	private boolean checkAllRCValidations(RegistrationDetailsDTO registrationDetailsDTO) {
		Long taxAmount = getTaxDetails(registrationDetailsDTO);
		if (taxAmount != 0l || taxAmount == 0l) {
			logger.debug("checkAllRCValidations(), for prNo: {}", registrationDetailsDTO.getPrNo());
			registrationDetailsDTO.setTaxAmount(taxAmount);
			registrationDetailsDTO.getVahanDetails().setManufacturedMonthYear(
					checkManufacturedMonthYear(registrationDetailsDTO.getVahanDetails().getManufacturedMonthYear()));
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public String checkManufacturedMonthYear(String manufacturedMonthYear) {
		if (null != manufacturedMonthYear && !(manufacturedMonthYear.length() == 6)) {
			StringBuilder monthYear = new StringBuilder();
			if ( manufacturedMonthYear.length() == 5) {
				monthYear.append(manufacturedMonthYear.substring(0, 2));
				monthYear.append("2");
				monthYear.append(manufacturedMonthYear.substring(2, 5));
				return monthYear.toString();
			} else if ( manufacturedMonthYear.length() == 4) {
				monthYear.append(manufacturedMonthYear.substring(0, 2));
				monthYear.append("20");
				monthYear.append(manufacturedMonthYear.substring(2, 4));
				return monthYear.toString();
			} else if (manufacturedMonthYear.length() > 10) {
				monthYear.append(manufacturedMonthYear.substring(5, 7));
				monthYear.append(manufacturedMonthYear.substring(0, 4));
				return monthYear.toString();
			}else if ( manufacturedMonthYear.length() ==7 ) {
				if(manufacturedMonthYear.charAt(4)=='-'){
				monthYear.append(manufacturedMonthYear.substring(5, 7));
				monthYear.append(manufacturedMonthYear.substring(0, 4));
				return monthYear.toString();
				}else if(manufacturedMonthYear.charAt(2)=='-'){
					monthYear.append(manufacturedMonthYear.substring(0, 2));
					monthYear.append(manufacturedMonthYear.substring(4, 7));
					return monthYear.toString();
				}
			}
			else {
				return manufacturedMonthYear;
			}
		}
		return manufacturedMonthYear;
	}

	/*private int check15PlusYearsVehicle(RegistrationDetailsDTO registrationDetailsDTO) {
		int vehicleAge = 0;
		if (null != registrationDetailsDTO.getRegistrationValidity().getPrGeneratedDate()) {
			LocalDate prGeneratedDate = registrationDetailsDTO.getRegistrationValidity().getPrGeneratedDate();
			Period period = Period.between(prGeneratedDate, LocalDate.now());
			return period.getYears();
		} else {
			if (null != registrationDetailsDTO.getPrGeneratedDate()) {
				LocalDate prGeneratedDate = registrationDetailsDTO.getPrGeneratedDate().toLocalDate();
				Period period = Period.between(prGeneratedDate, LocalDate.now());
				return period.getYears();
			}
		}
		return vehicleAge;
	}

	private List<OwnerTypeEnum> getTaxExemptedOwnershipType() {
		List<OwnerTypeEnum> ownershipType = new ArrayList<>();
		ownershipType.add(OwnerTypeEnum.Government);
		ownershipType.add(OwnerTypeEnum.POLICE);
		return ownershipType;
	}*/
	
	private boolean checkIsRCRequired(String classOfVehicle){
		if (null != classOfVehicle) {
			MasterCovDTO masterCovDTO = masterCovDAO.findByCovcode(classOfVehicle);
			if(null != masterCovDTO){
				return masterCovDTO.isRequireCard();
			}
		}
		return false;
	}

	private Pair<Boolean, List<Integer>> checkIsServiceIdExists(RegistrationDetailsDTO registrationDetailsDTO) {
		List<Integer> serviceIds = CollectionUtils.isNotEmpty(registrationDetailsDTO.getServiceIds())
				? registrationDetailsDTO.getServiceIds()
				: getServiceIdsFromRegService(registrationDetailsDTO.getPrNo());
		if (!serviceIds.isEmpty()) {
			List<Integer> serviceList = cardNotRequiredServices();
			if (serviceIds.stream().allMatch(id -> serviceList.contains(id))) {
				serviceList.clear();
				return Pair.of(false, serviceIds);
			} else {
				serviceList.clear();
				return Pair.of(true, serviceIds);
			}
		}
		return Pair.of(false, Arrays.asList(ServiceEnum.NEWREG.getId()));
	}

	private List<Integer> getServiceIdsFromRegService(String prNo) {
		List<Integer> serviceIds = new ArrayList<>();
		List<RegServiceDTO> regServiceDTOList = regServiceDAO.findByPrNo(prNo);
		if (!regServiceDTOList.isEmpty()) {
			regServiceDTOList.sort((r1, r2) -> r2.getCreatedDate().compareTo(r2.getCreatedDate()));
			regServiceDTOList.stream().forEach(val -> {
				if (val.getApplicationStatus().equals(StatusRegistration.APPROVED)) {
					serviceIds.addAll(val.getServiceIds());
					return;
				}
			});
			return serviceIds;
		}
		return serviceIds;
	}

	private List<Integer> cardNotRequiredServices() {
		List<Integer> serviceList = new ArrayList<>();
		serviceList.add(ServiceEnum.ISSUEOFNOC.getId());
		serviceList.add(ServiceEnum.AADHARSEEDING.getId());
		serviceList.add(ServiceEnum.TEMPORARYREGISTRATION.getId());
		serviceList.add(ServiceEnum.NEWFC.getId());
		serviceList.add(ServiceEnum.SPNR.getId());
		serviceList.add(ServiceEnum.SPNB.getId());
		serviceList.add(ServiceEnum.BODYBUILDER.getId());
		serviceList.add(ServiceEnum.TRAILER.getId());
		serviceList.add(ServiceEnum.THEFTINTIMATION.getId());
		serviceList.add(ServiceEnum.THEFTREVOCATION.getId());
		serviceList.add(ServiceEnum.CANCELLATIONOFNOC.getId());
		serviceList.add(ServiceEnum.OBJECTION.getId());
		serviceList.add(ServiceEnum.REVOCATION.getId());
		serviceList.add(ServiceEnum.TAXATION.getId());
		serviceList.add(ServiceEnum.NEWPERMIT.getId());
		serviceList.add(ServiceEnum.RENEWALOFPERMIT.getId());
		serviceList.add(ServiceEnum.SURRENDEROFPERMIT.getId());
		serviceList.add(ServiceEnum.TRANSFEROFPERMIT.getId());
		serviceList.add(ServiceEnum.VARIATIONOFPERMIT.getId());
		serviceList.add(ServiceEnum.PERMITCOA.getId());
		serviceList.add(ServiceEnum.RENEWALOFAUTHCARD.getId());
		serviceList.add(ServiceEnum.CANCELOFPERMIT.getId());
		serviceList.add(ServiceEnum.TOSELLER.getId());
		serviceList.add(ServiceEnum.FCLATEFEE.getId());
		serviceList.add(ServiceEnum.ALTDIFFTAX.getId());
		serviceList.add(ServiceEnum.RENEWALFC.getId());
		serviceList.add(ServiceEnum.OTHERSTATIONFC.getId());
		serviceList.add(ServiceEnum.EXTENSIONOFVALIDITY.getId());
		serviceList.add(ServiceEnum.REPLACEMENTOFVEHICLE.getId());
		serviceList.add(ServiceEnum.VEHICLESTOPPAGE.getId());
		serviceList.add(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId());
		serviceList.add(ServiceEnum.RENEWALLATEFEE.getId());
		serviceList.add(ServiceEnum.PERDATAENTRY.getId());
		return serviceList;
	}

	@Override
	public List<RegistrationCardDetailsVO> findRegistrationDetails(LocalDate lUpdateDate, String officeCode) {
		logger.info("findRegistrationDetails(), Inputs lUpdateDate: {}, officeCode: {}", lUpdateDate, officeCode);
		String dateVal = lUpdateDate + "T00:00:00.000Z";
		ZonedDateTime zdt = ZonedDateTime.parse(dateVal);
		LocalDateTime fromTime = zdt.toLocalDateTime();
		dateVal = lUpdateDate + "T23:59:59.999Z";
		ZonedDateTime zdt1 = ZonedDateTime.parse(dateVal);
		LocalDateTime toTime = zdt1.toLocalDateTime();
		return cardDetails(officeCode, fromTime, toTime);
	}

	@Override
	public List<RegistrationCardDetailsVO> findRegistrationDetails(LocalDate fromDate, LocalDate toDate,
			String officeCode) {
		logger.info("findRegistrationDetails(), Inputs fromDate: {}, toDate: {}, officeCode: {}", fromDate, toDate,
				officeCode);
		String dateVal = fromDate + "T00:00:00.000Z";
		ZonedDateTime zdt = ZonedDateTime.parse(dateVal);
		LocalDateTime fromTime = zdt.toLocalDateTime();
		dateVal = toDate + "T23:59:59.999Z";
		ZonedDateTime zdt1 = ZonedDateTime.parse(dateVal);
		LocalDateTime toTime = zdt1.toLocalDateTime();
		return cardDetails(officeCode, fromTime, toTime);
	}

	public List<RegistrationCardDetailsVO> cardDetails(String officeCode, LocalDateTime fromTime,
			LocalDateTime toTime) {
		logger.info("cardDetails(), Inputs officeCode: {}, fromDate: {}, toTime: {}", officeCode, fromTime, toTime);
		List<RegistrationCardDetailsVO> registrationCardDetailsVOlist = new ArrayList<>();
		List<RegistrationDetailsDTO> registrationDetailsDTOList = registrationCardPrintDAO
				.findByOfficeDetailsOfficeCodeAndLUpdateBetweenAndVehicleTypeAndIsCardPrintedFalse(officeCode, fromTime,
						toTime, CovCategory.N.getCode());
		for (RegistrationDetailsDTO registrationDetailsDTO : registrationDetailsDTOList) {
			if (checkIsRCRequired(registrationDetailsDTO.getClassOfVehicle()) && checkIsServiceIdExists(registrationDetailsDTO).getFirst()) {
				if (checkAllRCValidations(registrationDetailsDTO)) {
					logger.debug("cardDetails(), for prNo: {}", registrationDetailsDTO.getPrNo());
					registrationCardDetailsVOlist
							.add(registrationCardDetailsMapper.convertEntity(registrationDetailsDTO));
				}
			}
		}
		registrationDetailsDTOList.clear();
		return registrationCardDetailsVOlist;
	}

	@Override
	public Map<String, String> saveCardPrintedDetails(RegistrationCardPrintedVO registrationCardPrintedVO,
			Optional<UserDTO> userDetails, String officeCode) {
		String remarks = registrationCardPrintedVO.getRemarks();
		if (StringUtils.isEmpty(remarks)) {
			remarks = StringUtils.EMPTY;
		}
		List<String> prNo = registrationCardPrintedVO.getPrNo();
		Map<String, String> map = new HashMap<>();
		for (int i = 0; i < prNo.size(); i++) {
			try {
				Optional<RegistrationCardDetailsVO> registrationCardDetailsVO = findRegistrationDetails(prNo.get(i),
						officeCode);
				if (!registrationCardDetailsVO.isPresent()) {
					logger.info(appMessages.getLogMessage(MessageKeys.CARDPRINT_UPDATIONFAILED_PRNUMBERS), prNo.get(i));
					map.put(prNo.get(i), appMessages.getResponseMessage(MessageKeys.CARDPRINT_NORECORDFOUND));
				} else {
					if (registrationCardDetailsVO.get().isBackendUpdateFlag()) {
						map.put(prNo.get(i), appMessages.getResponseMessage(MessageKeys.CARDPRINT_ALREADY_PRINTED));
					} else {
						List<RegistrationDetailsDTO> registrationDetailsDTOList = registrationCardPrintDAO
								.findFirst10ByPrNoAndOfficeDetailsOfficeCodeAndVehicleType(prNo.get(i), officeCode,
										CovCategory.N.getCode());
						registrationDetailsDTOList.sort((r1, r2) -> r2.getlUpdate().compareTo(r1.getlUpdate()));
						RegistrationDetailsDTO registrationDetailsDTO = registrationDetailsDTOList.stream().findFirst()
								.get();
						Optional<CardDispatchDetailsDTO> cardDispatchDetails = cardDispatchDetailsDAO
								.findByPrNo(registrationDetailsDTO.getPrNo());
						CardDispatchDetailsVO cardDispatchDetailsVO = new CardDispatchDetailsVO();
						if (cardDispatchDetails.isPresent()) {
							getDataFromPreviousRecords(remarks, cardDispatchDetailsVO, cardDispatchDetails);
						} else {
							getDataFromFreshRecords(userDetails, remarks, i, cardDispatchDetailsVO,
									registrationDetailsDTO);
						}
						cardDispatchDetailsVO.setPrNo(registrationDetailsDTO.getPrNo());
						cardDispatchDetailsVO.setOfficeCode(registrationDetailsDTO.getOfficeDetails().getOfficeCode());
						cardDispatchDetailsVO.setRemarks(remarks);
						CardDispatchDetailsDTO cardDispatchDetailsDTO = cardDispatchDetailsMapper
								.convertVO(cardDispatchDetailsVO);
						cardDispatchDetailsDAO.save(cardDispatchDetailsDTO);
						registrationDetailsDTO.setCardPrinted(Boolean.TRUE);
						registrationDetailsDTO.setCardPrintedDate(LocalDateTime.now());
						registrationCardPrintDAO.save(registrationDetailsDTO);
						//Need to send Intimation Alert to Citizen
						notifications.sendNotifications(MessageTemplate.SMART_CARD_PRINT.getId(), registrationDetailsDTO);
					}
				}
			} catch (Exception e) {
				logger.info("saveCardPrintedDetails(), Updation Failed for prNo: {}, Exception is : {}", prNo.get(i),
						e);
				map.put(prNo.get(i), appMessages.getResponseMessage(MessageKeys.CARDPRINT_OFFICE_MISMATCH));
			}

		}
		return map;
	}
	
	private void sendNotifications(Integer templateId, RegistrationDetailsDTO entity) {
		try {
			if (entity != null) {
 				notifications.sendMessageNotification(notificationTemplate::fillTemplate, templateId, entity,
						entity.getApplicantDetails().getContact().getMobile());
			}
		} catch (Exception e) {
			logger.error("Failed to send notifications for template id: {}; {}", templateId, e);
		}
	}

	private void getDataFromFreshRecords(Optional<UserDTO> userDetails, String remarks, int i,
			CardDispatchDetailsVO cardDispatchDetailsVO, RegistrationDetailsDTO registrationDetailsDTO) {
		Map<String, String> noWithRemarks = new HashMap<>();
		Map<String, String> printedByMap = new HashMap<>();
		Map<String, LocalDateTime> printedDateTimeMap = new HashMap<>();
		noWithRemarks.put(String.valueOf(i + 1), remarks);
		printedByMap.put(String.valueOf(i + 1), userDetails.get().getUserId());
		printedDateTimeMap.put(String.valueOf(i + 1), LocalDateTime.now());
		KeyValue<String, Map<String, String>> key = new KeyValue<String, Map<String, String>>();
		key.setKey(registrationDetailsDTO.getApplicationNo());
		key.setValue(noWithRemarks);
		cardDispatchDetailsVO.setApplicationNo(key);
		cardDispatchDetailsVO.setPrintedBy(printedByMap);
		cardDispatchDetailsVO.setPrintedDateTime(printedDateTimeMap);
	}

	private void getDataFromPreviousRecords(String remarks, CardDispatchDetailsVO cardDispatchDetailsVO,
			Optional<CardDispatchDetailsDTO> cardDispatchDetails) {
		KeyValue<String, Map<String, String>> keyValue = cardDispatchDetails.get().getApplicationNo();
		Map<String, String> value = keyValue.getValue();
		String no = Collections.max(value.keySet());
		String maxKey = String.valueOf(Integer.valueOf(no) + 1);
		value.put(maxKey, remarks);
		keyValue.setKey(keyValue.getKey());
		keyValue.setValue(value);
		cardDispatchDetailsVO.setApplicationNo(keyValue);
		Map<String, String> printedByMap = cardDispatchDetails.get().getPrintedBy();
		printedByMap.put(maxKey, printedByMap.get(no));
		cardDispatchDetailsVO.setPrintedBy(printedByMap);
		Map<String, LocalDateTime> printedDateTimeMap = cardDispatchDetails.get().getPrintedDateTime();
		printedDateTimeMap.put(maxKey, LocalDateTime.now());
		cardDispatchDetailsVO.setPrintedDateTime(printedDateTimeMap);
	}

	private Long getTaxDetails(RegistrationDetailsDTO registrationDetailsDTO) {
		logger.debug("getTaxDetails(), for prNo: {}", registrationDetailsDTO.getPrNo());
		Long amount = 0l;
		List<TaxDetailsDTO> listOfPaidTax = null;
		
		listOfPaidTax = taxDetailsDAO.findFirst10ByPrNoAndPaymentPeriodInOrderByCreatedDateDesc(registrationDetailsDTO.getPrNo(), Arrays.asList(ServiceCodeEnum.LIFE_TAX.getCode()));
		if (listOfPaidTax.isEmpty()) {
			listOfPaidTax = taxDetailsDAO
					.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(registrationDetailsDTO.getApplicationNo(), Arrays.asList(ServiceCodeEnum.LIFE_TAX.getCode()));
		}
		if (!listOfPaidTax.isEmpty()) {
			registrationService.updatePaidDateAsCreatedDate(listOfPaidTax);
			return fetchLifeTaxAmount(listOfPaidTax);
		}
		return amount;
	}

	private Long fetchLifeTaxAmount(List<TaxDetailsDTO> listOfPaidTax) {
		Long taxAmount = 0l;
		Long secVehicleTaxAmt = 0l;
		for (TaxDetailsDTO taxDetailsDTO : listOfPaidTax) {
			if (taxDetailsDTO.getSecondVehicleDiffTaxPaid() != null && taxDetailsDTO.getSecondVehicleDiffTaxPaid()) {
				secVehicleTaxAmt = getLifeTaxAmount(taxDetailsDTO);
			} else {
				if (taxAmount == 0l) {
					taxAmount = getLifeTaxAmount(taxDetailsDTO);
				}
			}
		}
		return (taxAmount + secVehicleTaxAmt);
	}

	private Long getLifeTaxAmount(TaxDetailsDTO taxDetailsDTO) {
		Long amount = 0l;
		for (Map<String, TaxComponentDTO> taxMap : taxDetailsDTO.getTaxDetails()) {
			if (taxMap.containsKey(ServiceCodeEnum.LIFE_TAX.getCode())) {
				for (TaxComponentDTO taxComp : taxMap.values()) {
					if (taxComp.getTaxName().equalsIgnoreCase(ServiceCodeEnum.LIFE_TAX.getCode())) {
						if (null != taxDetailsDTO.getTaxAmount()) {
							return taxDetailsDTO.getTaxAmount();
						} else if (null != taxComp.getAmount()) {
							return Long.valueOf(taxComp.getAmount().toString());
						}
					}
				}
			}
		}
		return amount;
	}

	@Override
	public Optional<RegistrationCardDetailsVO> findRegistrationDetailsForSmartCard(String prNo, String officeCode) {
		
		List<RegistrationDetailsDTO> registrationDetailsDTOList = registrationCardPrintDAO
				.findFirst10ByPrNoAndOfficeDetailsOfficeCodeAndVehicleType(prNo, officeCode, CovCategory.N.getCode());
		if (!registrationDetailsDTOList.isEmpty()) {
			Optional<RegistrationCardDetailsVO> registrationCardDetailsVO = Optional.empty();
			registrationDetailsDTOList.sort((r1, r2) -> r2.getlUpdate().compareTo(r1.getlUpdate()));
			Optional<RegistrationDetailsDTO> registrationDetailsDTOOpt = registrationDetailsDTOList.stream().findFirst();
			RegistrationDetailsDTO registrationDetailsDTO = registrationDetailsDTOOpt.get();
			if (!registrationDetailsDTO.isCardPrinted()) {
				logger.debug("RC not yet printed for this prNo: {}", prNo);
				throw new BadRequestException("RC not yet printed for this prNo:" + prNo);
			}
			checkCurrentRegServiceStatus(prNo);
			if (checkIsRCRequired(registrationDetailsDTO.getClassOfVehicle())) {
				registrationDetailsDTO.setServiceIds(checkIsServiceIdExists(registrationDetailsDTO).getSecond());
				if (checkAllRCValidations(registrationDetailsDTO)) {
					registrationCardDetailsVO = registrationCardDetailsMapper.convertEntity(registrationDetailsDTOOpt);
				}
			}
			return registrationCardDetailsVO;
		}
		logger.warn(appMessages.getLogMessage(MessageKeys.CARDPRINT_GETTINGNULLVALUES_PRNUMBER_OFFICECODE), prNo,
				officeCode);
		return Optional.empty();
	}

	private void checkCurrentRegServiceStatus(String prNo) {
		List<RegServiceDTO> regServiceDTOList = regServiceDAO.findByPrNo(prNo);
		if (!regServiceDTOList.isEmpty()) {
			regServiceDTOList.sort((r1, r2) -> r2.getCreatedDate().compareTo(r1.getCreatedDate()));
			Optional<RegServiceDTO> regServiceDTOOpt = regServiceDTOList.stream().findFirst();
			if (regServiceDTOOpt.isPresent()) {
				RegServiceDTO regServiceDTO= regServiceDTOOpt.get();
				List<Integer> serviceList = cardNotRequiredServices();
				if (!serviceList.stream().allMatch(id -> regServiceDTO.getServiceIds().contains(id))) {
					if (!regServiceDTOOpt.get().getApplicationStatus().equals(StatusRegistration.APPROVED)) {
						logger.info("Your {} application is under process, Please wait till final APPROVAL",
								regServiceDTO.getServiceType());
						throw new BadRequestException(regServiceDTO.getServiceType()
								+ " application("+regServiceDTO.getApplicationNo()+") is under process, Please wait till final APPROVAL");
					}
				}
			}
		}
	}

	@Override
	public List<RegistrationCardDetailsVO> findRegistrationDetailsForSmartCardReprint(LocalDate approveDate,
			String officeCode) {
		String dateVal = approveDate + "T00:00:00.000Z";
		ZonedDateTime zdt = ZonedDateTime.parse(dateVal);
		LocalDateTime fromTime = zdt.toLocalDateTime();
		dateVal = approveDate + "T23:59:59.999Z";
		ZonedDateTime zdt1 = ZonedDateTime.parse(dateVal);
		LocalDateTime toTime = zdt1.toLocalDateTime();
		return cardDetailsForSmartCardReprint(officeCode, fromTime, toTime);
	}

	private List<RegistrationCardDetailsVO> cardDetailsForSmartCardReprint(String officeCode, LocalDateTime fromTime,
			LocalDateTime toTime) {
		logger.info("cardDetailsForSmartCardReprint(), Inputs officeCode: {}, fromDate: {}, toTime: {}", officeCode,
				fromTime, toTime);
		List<RegistrationCardDetailsVO> registrationCardDetailsVOlist = new ArrayList<>();
		List<RegistrationDetailsDTO> registrationDetailsDTOList = registrationCardPrintDAO
				.findByOfficeDetailsOfficeCodeAndLUpdateBetweenAndVehicleTypeAndIsCardPrintedTrue(officeCode, fromTime,
						toTime, CovCategory.N.getCode());
		for (RegistrationDetailsDTO registrationDetailsDTO : registrationDetailsDTOList) {
			if (checkIsRCRequired(registrationDetailsDTO.getClassOfVehicle())) {
				registrationDetailsDTO.setServiceIds(checkIsServiceIdExists(registrationDetailsDTO).getSecond());
				if (checkAllRCValidations(registrationDetailsDTO)) {
					logger.info("cardDetailsForSmartCardReprint(), for prNo: {}", registrationDetailsDTO.getPrNo());
					registrationCardDetailsVOlist
							.add(registrationCardDetailsMapper.convertEntity(registrationDetailsDTO));
				}
			}
		}
		registrationDetailsDTOList.clear();
		return registrationCardDetailsVOlist;
	}

	@Override
	public Map<String, String> saveSmartCardRePrintedDetails(RegistrationCardPrintedVO registrationCardPrintedVO,
			Optional<UserDTO> userDetails, String officeCode) {
		List<SmartCardPrintedVO> smartCardPrNos = registrationCardPrintedVO.getSmartCardPrNos();
		Map<String, String> map = new HashMap<>();
		for (int i = 0; i < smartCardPrNos.size(); i++) {
			try {
				Optional<RegistrationCardDetailsVO> registrationCardDetailsVO = findRegistrationDetailsForSmartCard(
						smartCardPrNos.get(i).getPrNo(), officeCode);
				String remarks = smartCardPrNos.get(i).getRemarks();
				if (StringUtils.isEmpty(remarks)) {
					remarks = StringUtils.EMPTY;
				}
				if (!registrationCardDetailsVO.isPresent()) {
					logger.debug(appMessages.getLogMessage(MessageKeys.CARDPRINT_UPDATIONFAILED_PRNUMBERS),
							smartCardPrNos.get(i).getPrNo());
					map.put(smartCardPrNos.get(i).getPrNo(),
							appMessages.getResponseMessage(MessageKeys.CARDPRINT_NORECORDFOUND));
				} else {
					if (!registrationCardDetailsVO.get().isBackendUpdateFlag()) {
						map.put(smartCardPrNos.get(i).getPrNo(), "The card is not Printed");
					} else {
						List<RegistrationDetailsDTO> registrationDetailsDTOList = registrationCardPrintDAO
								.findFirst10ByPrNoAndOfficeDetailsOfficeCodeAndVehicleType(
										smartCardPrNos.get(i).getPrNo(), officeCode, CovCategory.N.getCode());
						registrationDetailsDTOList.sort((r1, r2) -> r2.getlUpdate().compareTo(r1.getlUpdate()));
						RegistrationDetailsDTO registrationDetailsDTO = registrationDetailsDTOList.stream().findFirst().get();
						Optional<CardDispatchDetailsDTO> cardDispatchDetails = cardDispatchDetailsDAO
								.findByPrNo(smartCardPrNos.get(i).getPrNo());
						CardDispatchDetailsVO cardDispatchDetailsVO = new CardDispatchDetailsVO();
						if (cardDispatchDetails.isPresent()) {
							getDataFromPreviousRecords(remarks, cardDispatchDetailsVO, cardDispatchDetails);
						} else {
							getDataFromFreshRecords(userDetails, remarks, i, cardDispatchDetailsVO,
									registrationDetailsDTO);
						}
						cardDispatchDetailsVO.setPrNo(smartCardPrNos.get(i).getPrNo());
						cardDispatchDetailsVO.setOfficeCode(registrationDetailsDTO.getOfficeDetails().getOfficeCode());
						cardDispatchDetailsVO.setRemarks(remarks);
						cardDispatchDetailsVO.setFlagChangedByRTO(true);
						CardDispatchDetailsDTO cardDispatchDetailsDTO = cardDispatchDetailsMapper
								.convertVO(cardDispatchDetailsVO);
						cardDispatchDetailsDAO.save(cardDispatchDetailsDTO);
						registrationDetailsDTO.setServiceIds(registrationCardDetailsVO.get().getServiceIds());
						registrationDetailsDTO.setCardPrinted(Boolean.FALSE);
						//registrationDetailsDTO.setCardPrintedDate(null);
						registrationCardPrintDAO.save(registrationDetailsDTO);
					}
				}
			} catch (Exception e) {
				logger.debug("saveSmartCardRePrintedDetails(), Updation Failed for prNo: {}, Exception is : {}",
						smartCardPrNos.get(i).getPrNo(), e);
				map.put(smartCardPrNos.get(i).getPrNo(),
						appMessages.getResponseMessage(MessageKeys.CARDPRINT_OFFICE_MISMATCH));
			}
		}
		return map;
	}
}
