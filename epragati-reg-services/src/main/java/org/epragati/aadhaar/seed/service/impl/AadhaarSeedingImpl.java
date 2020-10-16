package org.epragati.aadhaar.seed.service.impl;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.aadhaar.AadhaarDetailsRequestVO;
import org.epragati.aadhaar.AadharDetailsResponseVO;
import org.epragati.aadhaar.seed.engine.AadharDetilsModel;
import org.epragati.aadhaar.seed.engine.AadharSeedingEngine;
import org.epragati.aadhaar.seed.engine.PersonDetails;
import org.epragati.aadhaar.seed.service.AadharSeeding;
import org.epragati.aadhaarAPI.AadhaarSourceDTO;
import org.epragati.aadhaarseeding.vo.AadhaarSeedDetailsVO;
import org.epragati.aadhaarseeding.vo.AadhaarSeedVO;
import org.epragati.aadhaarseeding.vo.AahaarSeedMatchVO;
import org.epragati.common.dto.aadhaar.seed.AadhaarSeedDTO;
import org.epragati.constants.CovCategory;
import org.epragati.constants.OwnerTypeEnum;
import org.epragati.dto.enclosure.ImageEnclosureDTO;
import org.epragati.exception.BadRequestException;
import org.epragati.images.vo.ImageInput;
import org.epragati.master.dao.AadharSeedingMatrixDAO;
import org.epragati.master.dao.ApplicantDetailsDAO;
import org.epragati.master.dao.ApprovalProcessFlowDAO;
import org.epragati.master.dao.DistrictDAO;
import org.epragati.master.dao.MasterCovDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.RegServiceDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.RegistrationDetailLogDAO;
import org.epragati.master.dto.AadharSeedingMatrixDTO;
import org.epragati.master.dto.ActionDetailsDTO;
import org.epragati.master.dto.ApplicantAddressDTO;
import org.epragati.master.dto.ApplicantDetailsDTO;
import org.epragati.master.dto.ContactDTO;
import org.epragati.master.dto.DistrictDTO;
import org.epragati.master.dto.MasterCovDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.RegistrationDetailsLogDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.mappers.AadhaarDetailsResponseMapper;
import org.epragati.master.mappers.ApplicantAddressMapper;
import org.epragati.master.mappers.RegistrationDetailsMapper;
import org.epragati.master.vo.ApplicantAddressVO;
import org.epragati.master.vo.ApplicantDetailsVO;
import org.epragati.master.vo.RegServiceAadharSeedingInputVO;
import org.epragati.master.vo.RegistrationDetailsVO;
import org.epragati.permits.dao.PermitDetailsDAO;
import org.epragati.permits.dto.PermitDetailsDTO;
import org.epragati.regservice.RegistrationService;
import org.epragati.regservice.dao.AadhaarSeedDAO;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.impl.RegistrationServiceImpl;
import org.epragati.regservice.mapper.AadhaarSeedMapper;
import org.epragati.restGateway.RestGateWayService;
import org.epragati.rta.service.impl.RTAServiceImpl;
import org.epragati.service.files.GridFsClient;
import org.epragati.service.notification.MessageTemplate;
import org.epragati.service.notification.NotificationTemplates;
import org.epragati.service.notification.NotificationUtil;
import org.epragati.util.PermitsEnum;
import org.epragati.util.PermitsEnum.PermitType;
import org.epragati.util.ResponseStatusEnum;
import org.epragati.util.RoleEnum;
import org.epragati.util.Status;
import org.epragati.util.Status.AadhaarSeedStatus;
import org.epragati.util.document.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author sairam.cheruku
 *
 */
@Service
public class AadhaarSeedingImpl implements AadharSeeding {

	private static final Logger logger = LoggerFactory.getLogger(AadhaarSeedingImpl.class);

	@Autowired
	private AadhaarSeedDAO aadhaarSeedDAO;

	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;

	@SuppressWarnings("rawtypes")
	@Autowired
	private RegistrationDetailsMapper regServiceMapper;

	@Autowired
	private RestGateWayService restGateWayService;

	@Autowired
	private AadharSeedingMatrixDAO aadharSeedingMatrixDAO;

	@Autowired
	private ApplicantDetailsDAO applicantDetailsDAO;

	@Autowired
	private NotificationUtil notificationUtil;

	@Autowired
	private NotificationTemplates notificationTemplates;

	@Autowired
	private GridFsClient gridFsClient;

	@Autowired
	private AadhaarSeedMapper aadhaarSeedMapper;

	@Autowired
	private AadharSeeding aadharSeeding;

	@Autowired
	private AadhaarDetailsResponseMapper aadhaarDetailsResponseMapper;

	@Autowired
	private OfficeDAO officeDAO;

	@Autowired
	private DistrictDAO districtDAO;

	@Autowired
	private ApplicantAddressMapper applicantAddressMapper;

	@Autowired
	private MasterCovDAO masterCovDAO;

	@Autowired
	private RegistrationDetailLogDAO regLog;

	@Autowired
	private PermitDetailsDAO permitDetailsDAO;

	@Autowired
	private ApprovalProcessFlowDAO approvalProcessFlowDAO;

	@Autowired
	private RTAServiceImpl rTAServiceImpl;

	@Autowired
	private RegServiceDAO regServiceDAO;
	
	@Autowired
	private RegistrationServiceImpl registrationServiceImpl;

	@Override
	public Optional<AadhaarSeedDetailsVO> processAadhaarSeeding(String prNo, String officeCode,
			AadhaarDetailsRequestVO aadharRequestModel, String mobileNo, String emailId,
			ApplicantAddressVO presentAddress) throws BadRequestException {
		if (prNo != null) {
			prNo = prNo.toUpperCase();
			logger.info("PR no: [{}]", prNo);
		}
		List<AadhaarSeedDTO> aadhaarSeedList = aadhaarSeedDAO.findByPrNo(prNo);

		if (!aadhaarSeedList.isEmpty()) {
			aadhaarSeedList.sort((o1, o2) -> o2.getCreatedDate().compareTo(o1.getCreatedDate()));
			if (aadhaarSeedList.get(0).getStatus().equals(AadhaarSeedStatus.INITIATED)) {
				logger.error("Application is already in-progress");
				throw new BadRequestException("Your Application is already in-progress");
			}
			if (aadhaarSeedList.get(0).getStatus().equals(AadhaarSeedStatus.OPENED)) {
				/*
				 * throw new
				 * BadRequestException("Your Aadhaar Seeding request is already submitted. " +
				 * " Please reach nearest RTA office for further approvals.");
				 */
				aadhaarSeedList.get(0).setStatus(AadhaarSeedStatus.CANCELEDBYSYSTEM);
				aadhaarSeedDAO.save(aadhaarSeedList.get(0));
			}
			if (aadhaarSeedList.get(0).getStatus().equals(AadhaarSeedStatus.AOAPPROVED)
					|| aadhaarSeedList.get(0).getStatus().equals(AadhaarSeedStatus.AOREJECTED)) {
				logger.error("Application is pending at RTO");
				throw new BadRequestException("Your Application Is pending At RTO");
			}
		}

		AadhaarSeedDetailsVO aadhaarSeedDetailsVO = new AadhaarSeedDetailsVO();

		Optional<RegistrationDetailsVO> regDetails = getRegistrationDetailsByAadharSeeding(prNo, presentAddress);

		logger.debug("Geting Registration details");
		aadhaarSeedDetailsVO.setRegistrationDetailsVO(regDetails.get());

		if (regDetails != null && regDetails.get().getApplicantDetails().getIsAadhaarValidated() != null
				&& regDetails.get().getApplicantDetails().getIsAadhaarValidated()) {
			throw new BadRequestException("Applicant Already Aadhaar Seeded");
		}
		if (!regDetails.get().getIsExistMandal()) {
			aadhaarSeedDetailsVO.setRegistrationDetailsVO(regDetails.get());
			return Optional.of(aadhaarSeedDetailsVO);
		}
		AadhaarSourceDTO aadhaarSourceDTO = new AadhaarSourceDTO();
		aadhaarSourceDTO.setPrNo(prNo);
		aadhaarSourceDTO.setService(AadhaarSeedStatus.INITIATED.getDesc());
		Optional<AadharDetailsResponseVO> aadharUserDetailsResponseVO = restGateWayService
				.validateAadhaar(aadharRequestModel, aadhaarSourceDTO);

		if (!aadharUserDetailsResponseVO.isPresent()) {
			throw new BadRequestException("Aadhar details not found");
		}
		if (aadharUserDetailsResponseVO.get().getAuth_status()
				.equals(ResponseStatusEnum.AADHAARRESPONSE.FAILED.getLabel())) {
			throw new BadRequestException(aadharUserDetailsResponseVO.get().getAuth_err_code());
		}

		List<AahaarSeedMatchVO> list = new ArrayList<AahaarSeedMatchVO>();

		Status.AadhaarSeedStatus status = Status.AadhaarSeedStatus.PENDING;
		aadhaarSeedDetailsVO.setCompanyVehicle(false);

		String matchedCount = matchedRecords(regDetails.get(), aadharUserDetailsResponseVO.get(), list);
		if (regDetails.get().getOwnerType() != null
				&& !regDetails.get().getOwnerType().equals(OwnerTypeEnum.Individual)) {
			aadhaarSeedDetailsVO.setCompanyVehicle(true);
			matchedCount = "000000";
		}
		Optional<AadharSeedingMatrixDTO> aadharSeedingMatrix = aadharSeedingMatrixDAO.findByCode(matchedCount);

		if (aadharSeedingMatrix.isPresent()) {
			status = Status.AadhaarSeedStatus.getAadhaarSeedStatus(aadharSeedingMatrix.get().getApprovalStatus());
			if (aadhaarSeedDetailsVO.isCompanyVehicle()) {
				status = Status.AadhaarSeedStatus.PENDING;
			}
		} else {
			logger.warn("Match Code Not Found for: " + matchedCount);
			status = Status.AadhaarSeedStatus.AUTO_REJECTED;
		}

		AadhaarSeedDTO aadhaarSeedDTO = prepareAadharSeedDTO(regDetails.get(), aadharUserDetailsResponseVO.get(),
				status, matchedCount);
		aadhaarSeedDTO.setMatchedDetails(list);
		logger.info("Matching aadhar details and Registration details");

		if (regDetails.get().getVehicleType() == null) {
			if (regDetails.get().getClassOfVehicle() != null) {
				setVehicleType(aadhaarSeedDTO, regDetails.get().getClassOfVehicle());
			} else if (regDetails.get().getVehicleDetails() != null
					&& regDetails.get().getVehicleDetails().getClassOfVehicle() != null) {
				setVehicleType(aadhaarSeedDTO, regDetails.get().getVehicleDetails().getClassOfVehicle());
			} else {
				throw new BadRequestException("Class of Vehicle not available ,So please contact HELP DESK");
			}
		}

		if (emailId != null) {
			aadhaarSeedDTO.setEmail(emailId);
		}
		aadhaarSeedDTO.setMobileNo(mobileNo);
		aadhaarSeedDTO.setOwnerShipType(regDetails.get().getOwnerType());
		if (presentAddress != null) {
			aadhaarSeedDTO.setApplicantAddressDTO(applicantAddressMapper.convertVO(presentAddress));
		}
		if (aadhaarSeedList.size() > 0 && aadhaarSeedList.get(0).getStatus().equals(AadhaarSeedStatus.PENDING)) {
			aadhaarSeedDTO.setId(aadhaarSeedList.get(0).getId());
		}
		aadhaarSeedDTO = aadhaarSeedDAO.save(aadhaarSeedDTO);
		try {
			this.processAadhaarSeeding(aadhaarSeedDTO, aadhaarSeedDTO.getId(), officeCode, StringUtils.EMPTY, status,
					Status.ActionStatus.CITIZEN.toString(), Status.ActionStatus.CITIZEN.toString());
		} catch (Exception e) {
			if (e.getMessage() != null) {
				aadhaarSeedDTO.setStatus(Status.AadhaarSeedStatus.FAILED);
				updateActionLogs(aadhaarSeedDTO, Status.AadhaarSeedStatus.FAILED,
						Status.ActionStatus.CITIZEN.toString(), e.getMessage(), Status.ActionStatus.CITIZEN.toString());
				aadhaarSeedDAO.save(aadhaarSeedDTO);
				throw new BadRequestException("Unable to process request, So please contact HELP DESK");
			}

		}
		Map<String, List<AahaarSeedMatchVO>> obj = new TreeMap<String, List<AahaarSeedMatchVO>>();
		obj.put("details", list);
		if (aadhaarSeedDetailsVO.isCompanyVehicle()) {
			String name = StringUtils.EMPTY;
			for (Entry<String, List<AahaarSeedMatchVO>> objList : obj.entrySet()) {
				for (AahaarSeedMatchVO matchVO : objList.getValue()) {
					if (matchVO.getKey().equals("Name")) {
						name = matchVO.getEkycValue();
						matchVO.setKey("Name(Company/Organization/Govt)");
						matchVO.setEkycValue("");
						if (regDetails.get().getApplicantDetails() != null
								&& regDetails.get().getApplicantDetails().getEntityName() != null) {
							matchVO.setEkycValue(regDetails.get().getApplicantDetails().getEntityName());
						}

					}
					if (matchVO.getKey().equals("Father Name")) {
						matchVO.setKey("Represented By");
						matchVO.setEkycValue(name);
					}

				}

			}

		}
		aadhaarSeedDetailsVO.setMatchedDetails(obj);
		aadhaarSeedDetailsVO.setStatus(status);
		aadhaarSeedDetailsVO.setAadhaarSeedId(aadhaarSeedDTO.getId());
		clearList(aadhaarSeedList);

		return Optional.of(aadhaarSeedDetailsVO);
	}

	@Override
	public void processAadhaarSeeding(AadhaarSeedDTO aadharSeedDetils, String id, String officeCode, String comment,
			AadhaarSeedStatus status, String user, String selectedRole) {

		logger.info("aadhar seeding process starts");
		AadhaarSeedDTO aadharSeedDTO = null;
		if (!StringUtils.isBlank(id)) {
			aadharSeedDTO = aadhaarSeedDAO.findOne(id);
		}
		RegistrationDetailsDTO regServiceDTO = null;
		ApplicantDetailsDTO applicantDTO = null;
		if (aadharSeedDTO == null) {
			throw new BadRequestException("aadharSeed details not found");
		}
		if (selectedRole.equals(RoleEnum.AO.getName()) && aadharSeedDTO.getStatus() != null
				&& (aadharSeedDTO.getStatus().equals(Status.AadhaarSeedStatus.AOAPPROVED)
						|| aadharSeedDTO.getStatus().equals(Status.AadhaarSeedStatus.AOREJECTED))) {
			throw new BadRequestException("Another AO already performed action on this record");
		}
		if (selectedRole.equals(RoleEnum.RTO.getName()) && aadharSeedDTO.getStatus() != null
				&& aadharSeedDTO.getStatus().equals(Status.AadhaarSeedStatus.REJECTED)) {
			throw new BadRequestException("Another RTO rejectd this record");
		}
		aadharSeedDTO.setStatus(status);
		if (selectedRole != null && selectedRole.equals(RoleEnum.AO.getName())) {
			aadharSeedDTO.setAadharNoAO(aadharSeedDetils.getAadharNoAO());
			aadharSeedDTO.setAadharResponseAO(aadharSeedDetils.getAadharResponseAO());
			aadharSeedDTO.setLock(null);
			aadharSeedDTO.setUserId(null);
			logger.info("aadhar seeding lock status and userid removed by userId", user);
		}
		if (selectedRole != null && selectedRole.equals(RoleEnum.RTO.getName())) {
			aadharSeedDTO.setAadharResponseRTO(aadharSeedDetils.getAadharResponseRTO());
			aadharSeedDTO.setAadharNoRTO(aadharSeedDetils.getAadharNoRTO());
			checkValidationForAadharSeddingRecordLockOrNot(aadharSeedDTO, user);
		}
		
		
		
		updateActionLogs(aadharSeedDTO, status, user, comment, selectedRole);
		if (Status.AadhaarSeedStatus.APPROVED.equals(status) || Status.AadhaarSeedStatus.AUTO_APPROVED.equals(status)) {
			final String prNo = aadharSeedDTO.getPrNo();

			synchronized (prNo.intern()) {
				//regServiceDTO = getRegDetails(aadharSeedDTO.getPrNo(), officeCode);
				regServiceDTO = registrationServiceImpl.getRegDetails(prNo, StringUtils.EMPTY, Boolean.FALSE);

				if (null == regServiceDTO.getApplicantDetails()) {
					throw new BadRequestException("Applicant Not Found");
				}

				applicantDTO = regServiceDTO.getApplicantDetails();

				String applicantNo = regServiceDTO.getApplicantDetails().getApplicantNo();

				if (aadharSeedDTO.getApplicantAddressDTO() != null) {
					applicantDTO.setPresentAddress(aadharSeedDTO.getApplicantAddressDTO());
				} else {
					applicantDTO.setPresentAddress(regServiceDTO.getApplicantDetails().getPresentAddress());
				}
					ContactDTO contactDto = new ContactDTO();
					if (aadharSeedDTO.getEmail() != null) {
						contactDto.setEmail(aadharSeedDTO.getEmail());
					}
					if (aadharSeedDTO.getMobileNo() !=null) {
						contactDto.setMobile(aadharSeedDTO.getMobileNo());
					}
					applicantDTO.setContact(contactDto);
					regServiceDTO.getApplicantDetails().setContact(contactDto);
				
				if (aadharSeedDTO.getVehicleType() != null) {
					regServiceDTO.setVehicleType(aadharSeedDTO.getVehicleType());
					regServiceDTO.setClassOfVehicle(aadharSeedDTO.getClassOfVehicle());
				}
				applicantDTO.setAadharNo(aadharSeedDTO.getAadharNo());
				applicantDTO
						.setAadharResponse(aadhaarDetailsResponseMapper.convertVO(aadharSeedDTO.getAadharResponse()));
				applicantDTO.setIsAadhaarValidated(true);

				// TODO: need to implement set prsent addres details to reg detils.
				applicantDTO.setApplicantNo(applicantNo + "_" + regServiceDTO.getApplicationNo());
				regServiceDTO.setApplicantDetails(applicantDTO);
				logger.info("Updating Registration Details ,applicant details and aadhaarSeeding ");
				registrationDetailDAO.save(regServiceDTO);
				applicantDTO.setlUpdate(LocalDateTime.now());
			}
		}
		if (regServiceDTO != null && regServiceDTO.getVehicleType() != null
				&& regServiceDTO.getVehicleType().equals(CovCategory.T.getCode())) {
			Optional<PermitDetailsDTO> permitDetails = permitDetailsDAO
					.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(aadharSeedDTO.getPrNo(),
							PermitType.PRIMARY.getPermitTypeCode(), PermitsEnum.ACTIVE.getDescription());

			if (permitDetails.isPresent()) {
				if (permitDetails.get().getRdto() == null) {
					permitDetails.get().setRdto(regServiceDTO);
				} else {
					permitDetails.get().getRdto().setApplicantDetails(applicantDTO);
				}
				permitDetailsDAO.save(permitDetails.get());
			}
		}
		applicantDetailsDAO.save(applicantDTO);
		aadhaarSeedDAO.save(aadharSeedDTO);
		Integer messageTemplateId = getMessageTemplate(aadharSeedDTO.getStatus());

		try {
			if (messageTemplateId != -1 && aadharSeedDTO.getMobileNo() != null) {
				if (aadharSeedDTO.getEmail() == null) {
					aadharSeedDTO.setEmail("default@rta.com");
				}
				if (regServiceDTO == null) {
					//regServiceDTO = getRegDetails(aadharSeedDTO.getPrNo(), officeCode);
					regServiceDTO = registrationServiceImpl.getRegDetails(aadharSeedDTO.getPrNo(), StringUtils.EMPTY, Boolean.FALSE);
				}
				sendNotifications(regServiceDTO, aadharSeedDTO.getEmail(), aadharSeedDTO.getMobileNo(),
						messageTemplateId);
			}
		} catch (Exception e) {
			logger.info("Exception raised while sending EMAIL/SMS for prNo [{}]", aadharSeedDTO.getPrNo());
		}

	}

	public void updateDistirctDetails(ApplicantDetailsDTO applicantDTO, OfficeDTO officeDetails) {

		Integer districtId = officeDetails.getDistrict();
		if (districtId == null) {
			Optional<OfficeDTO> officeDTO = officeDAO.findByOfficeCode(officeDetails.getOfficeCode());
			if (officeDTO.isPresent()) {
				districtId = officeDTO.get().getDistrict();
			}
		}
		List<DistrictDTO> districtDTO = districtDAO.findByDistrictId(districtId);
		if (districtDTO.isEmpty()) {
			logger.error("Distric not found based on office Given [{}]", officeDetails.getOfficeCode());
			throw new BadRequestException("Distric not found based on office :" + officeDetails.getOfficeCode());
		}
		if (applicantDTO.getPresentAddress() == null) {
			applicantDTO.setPresentAddress(new ApplicantAddressDTO());
		}
		applicantDTO.getPresentAddress().setDistrict(districtDTO.get(0));
		clearList(districtDTO);
	}

	private void sendNotifications(RegistrationDetailsDTO regServiceDTO, String email, String mobileNo,
			Integer templateId) {
		try {
			notificationUtil.sendEmailNotification(notificationTemplates::fillTemplate, templateId, regServiceDTO,
					email);
			notificationUtil.sendMessageNotification(notificationTemplates::fillTemplate, templateId, regServiceDTO,
					mobileNo);
		} catch (IOException e) {
			logger.error("Debug at Failing to send notifications for template id:{}; [{}] ", templateId, e);
			logger.error("Failed to send notifications for template id: {}; {}", templateId, e.getMessage());
		}

	}

	private Integer getMessageTemplate(Status.AadhaarSeedStatus status) {

		if (status == null) {
			return -1;
		}

		if (status.equals(Status.AadhaarSeedStatus.AUTO_APPROVED)) {
			return MessageTemplate.AADHAAR_AUTO_APPROVED.getId();
		}
		if (status.equals(Status.AadhaarSeedStatus.APPROVED)) {
			return MessageTemplate.AADHAAR_APPROVED.getId();
		}
		if (status.equals(Status.AadhaarSeedStatus.INITIATED)) {
			return MessageTemplate.AADHAAR_INITIATED.getId();
		}
		if (status.equals(Status.AadhaarSeedStatus.OPENED)) {
			return MessageTemplate.AADHAAR_OPENED.getId();
		}
		if (status.equals(Status.AadhaarSeedStatus.REJECTED)) {
			return MessageTemplate.AADHAAR_REJECTED.getId();
		}

		return -1;
	}

	/*
	 * private RegistrationDetailsDTO getRegDetails(String prNo, String officeCode)
	 * { Optional<RegistrationDetailsDTO> regDetailsDiffOfficeCode =
	 * registrationDetailDAO.findByPrNo(prNo); if
	 * (!regDetailsDiffOfficeCode.isPresent()) { throw new
	 * BadRequestException("No records found with PrNO: " + prNo + "officecode" +
	 * officeCode); } return regDetailsDiffOfficeCode.get();
	 * 
	 * }
	 */
	private void updateActionLogs(AadhaarSeedDTO aadharSeedDTO, Status.AadhaarSeedStatus status, String userName,
			String comment, String selectedRole) {
		ActionDetailsDTO actionDetailsDTO = new ActionDetailsDTO();
		actionDetailsDTO.setAction(status.getStatus());
		actionDetailsDTO.setActionBy(userName);
		actionDetailsDTO.setReason(comment);
		actionDetailsDTO.setActionDatetime(LocalDateTime.now());
		actionDetailsDTO.setActionByRole(Arrays.asList(selectedRole));

		if (null != aadharSeedDTO) {
			if (aadharSeedDTO.getActionDetails() == null) {
				aadharSeedDTO.setActionDetails(new ArrayList<>());
			}
			aadharSeedDTO.getActionDetails().add(actionDetailsDTO);
		}

	}

	private String matchedRecords(RegistrationDetailsVO registrationDetailsVO, AadharDetailsResponseVO ekycVO,
			List<AahaarSeedMatchVO> list) {
		logger.info("entered into matched records condition");
		PersonDetails ekyc = new PersonDetails();
		ekyc.setAadharNo(String.valueOf(ekycVO.getUid()));
		// address: district name + house_no + Street + villaget/Town/City;
		ekyc.setAddress(replaceWithEmpty(ekycVO.getHouse()) + "," + replaceWithEmpty(ekycVO.getStreet()) + ","
				+ replaceWithEmpty(ekycVO.getVillage_name()));
		ekyc.setDistrict(replaceWithEmpty(ekycVO.getDistrict_name()));
		ekyc.setFatherName(replaceWithEmpty(ekycVO.getCo()));

		// dob not required said by jagan for registration

		/*
		 * try { ekyc.setDob(replaceWithEmpty(DateConverters.covertDateToString(
		 * "dd-MM-yyyy", ekycVO.getDob())));
		 * 
		 * } catch (Exception e) { // TODO Auto-generated catch block
		 * logger.error("Exception while date convertion", e);
		 * ekyc.setDob(replaceWithEmpty(ekycVO.getDob())); }
		 */

		ekyc.setMandal(replaceWithEmpty(ekycVO.getMandal_name()));
		ekyc.setName(replaceWithEmpty(ekycVO.getName()));

		// Registration Details
		PersonDetails reg = new PersonDetails();
		ApplicantDetailsVO applicant = registrationDetailsVO.getApplicantDetails();
		reg.setAadharNo(replaceWithEmpty(applicant.getAadharNo()));

		reg.setAddress(replaceWithEmpty(applicant.getPresentAddress().getDoorNo()) + ","
				+ replaceWithEmpty(applicant.getPresentAddress().getStreetName()) + ","
				+ (applicant.getPresentAddress().getVillage() == null ? ""
						: replaceWithEmpty(applicant.getPresentAddress().getVillage().getVillageName()))

		);
		String district = ((applicant.getPresentAddress().getDistrict() == null) ? ""
				: replaceWithEmpty(applicant.getPresentAddress().getDistrict().getDistrictName()));
		reg.setDistrict(district);
		reg.setFatherName(replaceWithEmpty(registrationDetailsVO.getApplicantDetails().getFatherName()));
		/*
		 * reg.setDob(replaceWithEmpty(
		 * registrationDetailsVO.getApplicantDetails().getDateOfBirth().toString ()));
		 */
		String mandal = ((registrationDetailsVO.getApplicantDetails().getPresentAddress().getMandal() == null) ? ""
				: replaceWithEmpty(
						registrationDetailsVO.getApplicantDetails().getPresentAddress().getMandal().getMandalName()));
		reg.setMandal(mandal);
		reg.setName(replaceWithEmpty(registrationDetailsVO.getApplicantDetails().getFirstName()) + " "
				+ replaceWithEmpty(registrationDetailsVO.getApplicantDetails().getLastName()));

		AadharSeedingEngine seeding = new AadharSeedingEngine();
		String matchCode = seeding.getApprovalStatus(ekyc, reg);
		logger.info("AadharSeeding match code : [{}]", matchCode);
		this.getList(list, ekyc, reg, matchCode);
		/* clearList(list); */
		return matchCode;

	}

	private Character getNumFromCode(int index, String matchCode) {
		return matchCode.charAt(index);
	}

	private List<AahaarSeedMatchVO> getList(List<AahaarSeedMatchVO> list, PersonDetails ekyc, PersonDetails reg,
			String matchCode) {

		// aadharcode+namecode+fathernamecode+dobcode+mandalcode+addresscode+districtCode

		AahaarSeedMatchVO v1 = new AahaarSeedMatchVO();
		v1.setKey("Aadhaar No");
		v1.setDlValue(reg.getAadharNo());
		v1.setEkycValue(ekyc.getAadharNo());
		v1.setMatchState(getNumFromCode(0, matchCode));

		AahaarSeedMatchVO v2 = new AahaarSeedMatchVO();
		v2.setKey("Name");
		v2.setDlValue(reg.getName());
		v2.setEkycValue(ekyc.getName());
		v2.setMatchState(getNumFromCode(1, matchCode));

		AahaarSeedMatchVO v3 = new AahaarSeedMatchVO();
		v3.setKey("Father Name");
		v3.setDlValue(reg.getFatherName());
		v3.setEkycValue(ekyc.getFatherName());
		v3.setMatchState(getNumFromCode(2, matchCode));

		/*
		 * AahaarSeedMatchVO v4 = new AahaarSeedMatchVO(); v4.setKey("Date Of Birth");
		 * v4.setDlValue(reg.getDob()); v4.setEkycValue(ekyc.getDob());
		 * v4.setMatchState(getNumFromCode(3, matchCode));
		 */

		AahaarSeedMatchVO v5 = new AahaarSeedMatchVO();
		v5.setKey("Mandal");
		v5.setDlValue(reg.getMandal());
		v5.setEkycValue(ekyc.getMandal());
		v5.setMatchState(getNumFromCode(3, matchCode));

		AahaarSeedMatchVO v6 = new AahaarSeedMatchVO();
		v6.setKey("Address");
		v6.setDlValue(reg.getAddress());
		v6.setEkycValue(ekyc.getAddress());
		v6.setMatchState(getNumFromCode(4, matchCode));

		AahaarSeedMatchVO v7 = new AahaarSeedMatchVO();
		v7.setKey("District");
		v7.setDlValue(reg.getDistrict());
		v7.setEkycValue(ekyc.getDistrict());
		v7.setMatchState(getNumFromCode(5, matchCode));

		list.add(v1);
		list.add(v2);
		list.add(v3);
		// list.add(v4);
		list.add(v5);
		list.add(v6);
		list.add(v7);

		return list;

	}

	private String replaceWithEmpty(String input) {
		if (StringUtils.isBlank(input))
			input = StringUtils.EMPTY;
		return input;

	}

	/**
	 * 
	 * @param prNo
	 * @param officeCode
	 * @return regServicesDetails
	 */
	// as per sme's & phani query with prno,office code is removed only with
	// prno we fetching data
	private Optional<RegistrationDetailsVO> getRegistrationDetailsByAadharSeeding(String prNo,
			ApplicantAddressVO presentAddress) {
		logger.info("Getting Registration Details based on PrNO [{}]", prNo);
		RegistrationDetailsVO regServicesVo = null;
		Optional<RegistrationDetailsDTO> regDetails = registrationDetailDAO.findByPrNo(prNo);

		if (!regDetails.isPresent()) {
			logger.error("No Records found with [{}]", prNo);
			throw new BadRequestException("No records found with PrNO: " + prNo);
		}
		if (regDetails.get().getOfficeDetails() != null
				&& regDetails.get().getOfficeDetails().getOfficeCode() != null) {
			Optional<OfficeDTO> officeDetails = officeDAO
					.findByOfficeCodeAndIsActiveTrue(regDetails.get().getOfficeDetails().getOfficeCode());
			if (!officeDetails.isPresent()) {
				logger.error("Aadhar Seeding Service is only availble for AP vehicles [{}]", prNo);
				throw new BadRequestException("Aadhaar Seeding Service only available for AP vehicles : " + prNo);
			}
		}
		RegistrationDetailsDTO entry = regDetails.get();
		RegistrationDetailsDTO regDeatils = null;
		try {
			regDeatils = (RegistrationDetailsDTO) entry.clone();
			regDeatils.setMovedSource("AADHAARSEEDING");
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		RegistrationDetailsLogDTO regLogDetails = new RegistrationDetailsLogDTO();
		regLogDetails.setRegiDetails(regDeatils);
		regLogDetails.setLogCreatedDateStr(LocalDateTime.now().toString());
		regLogDetails.setLogCreatedDateTime(LocalDateTime.now());
		regLog.save(regLogDetails);
		boolean isExistMandal = true;
		if (presentAddress == null && (entry.getApplicantDetails().getPresentAddress() == null
				|| entry.getApplicantDetails().getPresentAddress().getDistrict() == null
				|| entry.getApplicantDetails().getPresentAddress().getMandal() == null)) {
			isExistMandal = false;
			updateDistirctDetails(entry.getApplicantDetails(), entry.getOfficeDetails());
		}
		regServicesVo = regServiceMapper.convertEntity(entry);
		regServicesVo.setRegistrationValidity(null);
		regServicesVo.setIsExistMandal(isExistMandal);
		return Optional.ofNullable(regServicesVo);
	}

	private AadhaarSeedDTO prepareAadharSeedDTO(RegistrationDetailsVO regServiceVO,
			AadharDetailsResponseVO aadharUserDetailsResponseVO, Status.AadhaarSeedStatus status, String matchedCount) {
		AadhaarSeedDTO aadhaarSeedDTO = new AadhaarSeedDTO();
		aadhaarSeedDTO.setAadharNo(aadharUserDetailsResponseVO.getUid().toString());
		aadhaarSeedDTO.setAadharResponse(aadharUserDetailsResponseVO);
		aadhaarSeedDTO.setCreatedDate(LocalDateTime.now());
		aadhaarSeedDTO.setPrNo(regServiceVO.getPrNo());
		aadhaarSeedDTO.setDob(regServiceVO.getApplicantDetails().getDateOfBirth());
		aadhaarSeedDTO.setStatus(status);
		aadhaarSeedDTO.setMatchCode(matchedCount);
		aadhaarSeedDTO.setIssuedOfficeCode(regServiceVO.getOfficeDetails().getOfficeCode());
		aadhaarSeedDTO.setEmail(regServiceVO.getApplicantDetails().getContact().getEmail());
		aadhaarSeedDTO.setMobileNo(regServiceVO.getApplicantDetails().getContact().getMobile());
		return aadhaarSeedDTO;
	}

	@Override
	public void updateAadhaarrSeedingEnclosure(RegServiceAadharSeedingInputVO aadharSeedInput,
			MultipartFile[] uploadfiles) {
		if (aadharSeedInput.getAadhaarSeedDetailsVO() == null) {
			logger.warn("AadhaarSeedDetails not found in Request");
			throw new BadRequestException("AadhaarSeedDetails not found in Request");
		}
		AadhaarSeedDTO aadhaarSeedDTO = aadhaarSeedDAO
				.findOne(aadharSeedInput.getAadhaarSeedDetailsVO().getAadhaarSeedId());
		if (aadhaarSeedDTO == null) {
			throw new BadRequestException("Your request not initialized");
		}
		if (!(aadhaarSeedDTO.getStatus().equals(Status.AadhaarSeedStatus.PENDING)
				|| aadhaarSeedDTO.getStatus().equals(Status.AadhaarSeedStatus.AUTO_REJECTED))) {
			throw new BadRequestException("AadhaarSeed Application status should be Pending Stage");
		}
		/*
		 * RegistrationDetailsDTO regServiceDTO =
		 * getRegDetails(aadhaarSeedDTO.getPrNo(),
		 * aadhaarSeedDTO.getIssuedOfficeCode());
		 */
		RegistrationDetailsDTO regServiceDTO = registrationServiceImpl.getRegDetails(aadhaarSeedDTO.getPrNo(), StringUtils.EMPTY, Boolean.FALSE);
		List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = null;
		Status.AadhaarSeedStatus status = aadhaarSeedDTO.getStatus();
		if (!aadharSeedInput.getAadhaarSeedDetailsVO().isMobileUploaded()) {
			status = Status.AadhaarSeedStatus.OPENED;
		}

		if (aadhaarSeedDTO.getStatus().equals(Status.AadhaarSeedStatus.PENDING)
				&& !aadharSeedInput.getAadhaarSeedDetailsVO().isMobileUploaded()) {
			status = Status.AadhaarSeedStatus.INITIATED;
		}
		if (aadharSeedInput.getAadhaarSeedDetailsVO().isMobileUploaded() || aadharSeedInput.isUploadedByBrowser()) {
			enclosures = imageSaveReq(Boolean.TRUE, aadharSeedInput.getImages(), regServiceDTO.getApplicationNo(),
					uploadfiles, status.getStatus());
		} else if (aadhaarSeedDTO.getEnclosures() == null) {
			throw new BadRequestException("enclousers required");
		}

		if (enclosures != null) {
			aadhaarSeedDTO.setEnclosures(enclosures);
		}

		aadhaarSeedDTO.setStatus(status);
		aadhaarSeedDTO.setMobileNo(aadharSeedInput.getAadhaarSeedDetailsVO().getMobileNo());
		aadhaarSeedDTO.setEmail(aadharSeedInput.getAadhaarSeedDetailsVO().getEmail());
		updateActionLogs(aadhaarSeedDTO, status, Status.ActionStatus.CITIZEN.toString(), StringUtils.EMPTY,
				Status.ActionStatus.CITIZEN.toString());
		aadhaarSeedDAO.save(aadhaarSeedDTO);
		clearList(enclosures);
	}

	@Override
	public Optional<AadhaarSeedVO> aadhaarSeedingStatus(AadharDetilsModel input) {

		List<AadhaarSeedDTO> aadhaarSeedList = aadhaarSeedDAO.findByPrNoAndAadharNo(input.getPrNo(),
				input.getAadharNumber());
		if (!aadhaarSeedList.isEmpty()) {
			aadhaarSeedList.sort((o1, o2) -> o2.getCreatedDate().compareTo(o1.getCreatedDate()));
			AadhaarSeedVO aadhaarSeedVO = aadhaarSeedMapper.convertEntity(aadhaarSeedList.get(0));
			aadhaarSeedVO.setStatusDescription(aadhaarSeedVO.getStatus().getDesc());
			if (aadhaarSeedVO.getStatus().equals(Status.AadhaarSeedStatus.REJECTED)
					&& StringUtils.isNotEmpty(aadhaarSeedVO.getComment())) {
				aadhaarSeedVO.setStatusDescription(aadhaarSeedVO.getComment());
			}
			clearList(aadhaarSeedList);
			return Optional.of(aadhaarSeedVO);
		}
		clearList(aadhaarSeedList);
		return Optional.empty();
	}

	@Override
	public Optional<AadhaarSeedVO> getAadhaarSeedDetailsByAadhaarNo(String aadharNo, String prNo, String officeCode,
			String role) {
		// List<AadhaarSeedDTO> aadhaarSeedList =
		// aadhaarSeedDAO.findByAadharNoAndStatus(aadharNo,
		// Status.AadhaarSeedStatus.OPENED);

		if (!role.equals(RoleEnum.AO.name())) {
			logger.error("AO as only Access this service");
			throw new BadRequestException("AO as only Access this service");
		}
		List<AadhaarSeedDTO> aadhaarSeedList = aadhaarSeedDAO.findByStatusAndPrNo(Status.AadhaarSeedStatus.OPENED,
				prNo);
		if (!aadhaarSeedList.isEmpty()) {
			aadhaarSeedList.sort((o1, o2) -> o2.getCreatedDate().compareTo(o1.getCreatedDate()));
			if (!(aadhaarSeedList.get(0).getAadharNo() != null
					&& aadhaarSeedList.get(0).getAadharNo().equals(aadharNo))) {
				logger.error(
						"Given input aadhaar number is not matched with our record.so please provide correct aadhaar number [{}]",
						aadharNo);
				throw new BadRequestException(
						"Given input aadhaar number is not matched with our record.so please provide correct aadhaar number");
			}

			Optional<RegistrationDetailsDTO> registrationDetailsDTO = registrationDetailDAO.findByPrNo(prNo);

			if (aadhaarSeedList.get(0).getIssuedOfficeCode() != null
					&& aadhaarSeedList.get(0).getIssuedOfficeCode().equals(officeCode)
					&& registrationDetailsDTO.get().getOfficeDetails() != null
					&& registrationDetailsDTO.get().getOfficeDetails().getOfficeCode() != null
					&& registrationDetailsDTO.get().getOfficeDetails().getOfficeCode().equals(officeCode)) {
				return Optional.ofNullable(
						getAadhaarSeedVo(aadhaarSeedList.get(0), Boolean.FALSE, registrationDetailsDTO.get()));
			}

			List<RegServiceDTO> listRegDetails = regServiceDAO.findByPrNo(prNo);
			if (!CollectionUtils.isEmpty(listRegDetails)) {
				listRegDetails.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
				RegServiceDTO serviceDTO = listRegDetails.stream().findFirst().get();
				if (aadhaarSeedList.get(0).getCreatedDate().isBefore(serviceDTO.getCreatedDate())
						|| aadhaarSeedList.get(0).getCreatedDate().isEqual(serviceDTO.getCreatedDate())) {
					clearList(aadhaarSeedList);
					return Optional.ofNullable(
							getAadhaarSeedVo(aadhaarSeedList.get(0), Boolean.FALSE, registrationDetailsDTO.get()));
				}
			}
			clearList(aadhaarSeedList);
			return Optional.empty();
			// AadhaarSeedDTO aadhaarSeedDTO= aadhaarSeedList.get(0);
			/*
			 * if(aadhaarSeedDTO.getLock()==null || aadhaarSeedDTO.getLock()==false){ return
			 * Optional.ofNullable(
			 * aadhaarSeedMapper.convertEntity(aadhaarSeedList.get(0))); }else
			 * if(user.equals(aadhaarSeedDTO.getUserId())){ return Optional.ofNullable(
			 * aadhaarSeedMapper.convertEntity(aadhaarSeedList.get(0))); }
			 */
		}
		clearList(aadhaarSeedList);
		return Optional.empty();
	}

	/** RTA Side View Details **/
	@Override
	public Optional<AadhaarSeedVO> getAadhaarSeedDetails(String officeCode, String aadhaarSeedingId, String userId,
			String role) {
		AadhaarSeedDTO entity = aadhaarSeedDAO.findOne(aadhaarSeedingId);
		if (entity != null && (entity.getStatus().equals(Status.AadhaarSeedStatus.INITIATED)
				&& entity.getIssuedOfficeCode().equals(officeCode))) {
			if (entity.getLock() != null && entity.getLock() && entity.getUserId() != null
					&& !entity.getUserId().equalsIgnoreCase(userId)) {
				logger.error("Record already viewed by another So please assign new record [{}]", role);
				throw new BadRequestException(
						"Record already viewed by another+[ " + role + "]So please assign new record");
			}
			Boolean lock = true;
			entity.setLock(lock);
			entity.setUserId(userId);
			aadhaarSeedDAO.save(entity);
			return Optional.ofNullable(getAadhaarSeedVo(entity, Boolean.TRUE, null));
		}
		/*
		 * if(entity!=null &&
		 * (entity.getStatus().equals(Status.AadhaarSeedStatus.OPENED) ||
		 * entity.getIssuedOfficeCode().equals(officeCode)) ){ Boolean lock = true;
		 * entity.setLock(lock); entity.setUserId(userId); aadhaarSeedDAO.save(entity);
		 * return Optional.ofNullable(getAadhaarSeedVo(entity)); }
		 */
		return Optional.empty();
	}

	private AadhaarSeedVO getAadhaarSeedVo(AadhaarSeedDTO aadhaarSeedDTO, Boolean value,
			RegistrationDetailsDTO registrationDetailsDTO) {
		AadhaarSeedVO aadhaarSeedVO = null;
		aadhaarSeedVO = aadhaarSeedMapper.convertEntity(aadhaarSeedDTO);
		if (null != aadhaarSeedVO.getPrNo()) {

			if (value) {
				registrationDetailsDTO = aadharSeeding.getRegistrationDetails(aadhaarSeedVO.getPrNo(),
						aadhaarSeedDTO.getIssuedOfficeCode());
			}

			List<AahaarSeedMatchVO> aahaarSeedMatchlist = new ArrayList<>();
			aadhaarSeedVO.setRegistrationDetailsVO(regServiceMapper.convertEntity(registrationDetailsDTO));
			aadharSeeding.applicantDetailsmatrix(regServiceMapper.convertEntity(registrationDetailsDTO),
					aadhaarSeedVO.getAadharResponse(), aahaarSeedMatchlist);
			if (!aahaarSeedMatchlist.isEmpty() && aadhaarSeedVO != null && aadhaarSeedVO.getOwnerShipType() != null
					&& !aadhaarSeedVO.getOwnerShipType().equals(OwnerTypeEnum.Individual)) {
				String name = StringUtils.EMPTY;
				for (AahaarSeedMatchVO matchVO : aahaarSeedMatchlist) {
					if (matchVO.getKey().equals("Name")) {
						name = matchVO.getEkycValue();
						matchVO.setKey("Name(Company/Organization/Govt)");
						matchVO.setEkycValue("");
						if (registrationDetailsDTO.getApplicantDetails() != null
								&& registrationDetailsDTO.getApplicantDetails().getEntityName() != null) {
							matchVO.setEkycValue(registrationDetailsDTO.getApplicantDetails().getEntityName());
						}

					}
					if (matchVO.getKey().equals("Father Name")) {
						matchVO.setKey("Represented By");
						matchVO.setEkycValue(name);
					}

				}

			}
			aadhaarSeedVO.setAahaarSeedMatchlist(aahaarSeedMatchlist);

		}
		return aadhaarSeedVO;
	}

	@Override
	public RegistrationDetailsDTO getRegistrationDetails(String prNo, String officeCode) {
		Optional<RegistrationDetailsDTO> registrationDetailsDTO = registrationDetailDAO
				.findByOfficeDetailsOfficeCodeAndPrNo(officeCode, prNo);
		if (!registrationDetailsDTO.isPresent()) {
			throw new BadRequestException("PR details not found");
		}
		return registrationDetailsDTO.get();
	}

	@Override
	public String applicantDetailsmatrix(RegistrationDetailsVO regVo, AadharDetailsResponseVO ekycVO,
			List<AahaarSeedMatchVO> list) {
		// TODO Auto-generated method stub
		return matchedRecords(regVo, ekycVO, list);
	}

	@Override
	public void saveAadharSeed(AadhaarSeedVO aadhaarSeedInput, UserDTO userDetails, String user, String selectedRole) {
		if (selectedRole.equals(RoleEnum.AO.name())) {
			if (aadhaarSeedInput == null || aadhaarSeedInput.getAadhaarDetailsRequestVO() == null) {
				logger.error("Aadhaar Authentication is Mandatory");
				throw new BadRequestException("Aadhaar Authentication is Mandatory");
			}
			if (userDetails.getAadharNo() == null) {
				logger.error("AO Aadhar Details Not available");
				throw new BadRequestException("AO Aadhar Details Not available");
			}
			// we are already validate aadhar from third party service
			// Optional<AadharDetailsResponseVO> aadhaarResponseVO = rTAServiceImpl
			// .getAadhaarResponse(aadhaarSeedInput.getAadhaarDetailsRequestVO());
			if (!userDetails.getAadharNo().equals(aadhaarSeedInput.getAadharNoAO())) {
				logger.error("Unauthorized User");
				throw new BadRequestException("Unauthorized User");
			}
		}
		AadhaarSeedDTO aadharSeedDTO = aadhaarSeedMapper.convertVO(aadhaarSeedInput);
		aadharSeeding.processAadhaarSeeding(aadharSeedDTO, aadhaarSeedInput.getId(),
				aadhaarSeedInput.getIssuedOfficeCode(), aadhaarSeedInput.getComment(), aadhaarSeedInput.getStatus(),
				user, selectedRole);
	}

	@Override
	public Optional<AadhaarSeedVO> getAadhaarSeedPendingRecord(String officeCode, String role, String user) {
		List<AadhaarSeedDTO> aadhaarSeedingList = null;
		if (!role.equalsIgnoreCase(RoleEnum.AO.name())) {
			logger.error("AO as only Access this service");
			throw new BadRequestException("AO as only Access this service");
		}
		if (StringUtils.isBlank(user)) {
			logger.error("UserId not available");
			throw new BadRequestException("UserId not available");
		}
		if (role.equalsIgnoreCase(RoleEnum.AO.name())) {
			aadhaarSeedingList = aadhaarSeedDAO.findByIssuedOfficeCodeAndStatusIn(officeCode,
					Arrays.asList(Status.AadhaarSeedStatus.INITIATED));
		}
		if (aadhaarSeedingList.isEmpty()) {
			logger.error("Records not avialble to View");
			throw new BadRequestException("Records not avialble to View");
		}
		for (AadhaarSeedDTO aadhaarSeedDTO : aadhaarSeedingList) {
			if (aadhaarSeedDTO.getUserId() != null && aadhaarSeedDTO.getUserId().equals(user)
					&& aadhaarSeedDTO.getLock()) {
				return Optional.ofNullable(aadhaarSeedMapper.convertEntity(aadhaarSeedDTO));
			}
		}
		for (AadhaarSeedDTO aadhaarSeedDTO : aadhaarSeedingList) {
			if (aadhaarSeedDTO.getUserId() == null && aadhaarSeedDTO.getLock() == null) {
				setLock(aadhaarSeedDTO, user);
				clearList(aadhaarSeedingList);
				return Optional.ofNullable(aadhaarSeedMapper.convertEntity(aadhaarSeedDTO));
			}
			if (aadhaarSeedDTO.getUserId() == null && !aadhaarSeedDTO.getLock()) {
				setLock(aadhaarSeedDTO, user);
				clearList(aadhaarSeedingList);
				return Optional.ofNullable(aadhaarSeedMapper.convertEntity(aadhaarSeedDTO));
			}
			if (aadhaarSeedDTO.getUserId() != null && !aadhaarSeedDTO.getLock()) {
				setLock(aadhaarSeedDTO, user);
				clearList(aadhaarSeedingList);
				return Optional.ofNullable(aadhaarSeedMapper.convertEntity(aadhaarSeedDTO));
			}
		}
		if (CollectionUtils.isNotEmpty(aadhaarSeedingList) && aadhaarSeedingList.get(0).getUserId() != null) {
			logger.error("First Record Locked by [{}],[{}]", role, aadhaarSeedingList.get(0).getUserId());
			clearList(aadhaarSeedingList);
			throw new BadRequestException(
					"First Record Locked by  [ " + role + "]" + aadhaarSeedingList.get(0).getUserId());
		}
		logger.error("Records Locked by another [{}]", role);
		clearList(aadhaarSeedingList);
		throw new BadRequestException("Records Locked by another [ " + role + "]");
	}

	private void setVehicleType(AadhaarSeedDTO aadhaarSeedDTO, String covCode) {
		MasterCovDTO masterDto = masterCovDAO.findByCovcode(covCode);
		if (masterDto != null) {
			aadhaarSeedDTO.setVehicleType(masterDto.getCategory());
			aadhaarSeedDTO.setClassOfVehicle(covCode);
		}
	}

	private void setLock(AadhaarSeedDTO entity, String user) {
		Boolean lock = true;
		entity.setLock(lock);
		entity.setUserId(user);
		aadhaarSeedDAO.save(entity);
	}

	private List<KeyValue<String, List<ImageEnclosureDTO>>> imageSaveReq(boolean saveReq, List<ImageInput> imagesInput,
			String applicationNo, MultipartFile[] uploadfiles, String status) {
		try {
			if (saveReq) {
				if (uploadfiles.length < 3) {
					throw new BadRequestException("Enclouser Required");
				}
				return gridFsClient.convertImages(imagesInput, applicationNo, uploadfiles, status);
			}
		} catch (IOException e) {
			logger.error("Exception{}", e);
			throw new BadRequestException(e.getMessage());
		}
		return null;

	}

	@Override
	public List<AadhaarSeedVO> getAadhaarSeedPendingRecordForRto(String officeCode, String role, String user,
			String status) {
		if (StringUtils.isNotBlank(status)) {
			List<AadhaarSeedVO> AadhaarSeedVOList = new ArrayList<>();
			List<AadhaarSeedDTO> aadhaarSeedingList = aadhaarSeedDAO
					.findFirst10ByIssuedOfficeCodeAndStatusOrderByCreatedDateAsc(officeCode, status);
			if (CollectionUtils.isNotEmpty(aadhaarSeedingList)) {
				aadhaarSeedingList.sort((p2, p1) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
				AadhaarSeedDTO aadhaarSeedDTO = aadhaarSeedingList.stream().findFirst().get();
				// AadhaarSeedDTO aadhaarSeedDTO=aadhaarSeedingList.get(0);
				AadhaarSeedVO aadhaarSeedVO = getAadhaarSeedVo(aadhaarSeedDTO, Boolean.TRUE, null);
				AadhaarSeedVOList.add(aadhaarSeedVO);
				// }
				return AadhaarSeedVOList;
			}
		}
		return Collections.emptyList();
	}

	@Override
	public void saveAadharSeedForRto(AadhaarSeedVO aadhaarSeedInput, UserDTO userDetails, String user,
			String selectedRole,String reqHeader) {
		if (selectedRole.equalsIgnoreCase(RoleEnum.RTO.name())) {
			List<AadhaarSeedStatus> statusList = new ArrayList<>();
			statusList.add(Status.AadhaarSeedStatus.APPROVED);
			statusList.add(Status.AadhaarSeedStatus.REJECTED);
			if (!statusList.contains(aadhaarSeedInput.getStatus())) {
				logger.error("Invalid Status unable to process your request ");
				throw new BadRequestException("Invalid Status unable to process your request ");
			}

			AadhaarSeedDTO aadharSeedDTO = aadhaarSeedMapper.convertVO(aadhaarSeedInput);
			aadharSeeding.processAadhaarSeeding(aadharSeedDTO, aadhaarSeedInput.getId(),
					aadhaarSeedInput.getIssuedOfficeCode(), aadhaarSeedInput.getComment(), aadhaarSeedInput.getStatus(),
					user, selectedRole);
		} else {
			logger.error("This Service Olny For RTO");
			throw new BadRequestException("This Service Olny For RTO");
		}
	}

	private void checkValidationForAadharSeddingRecordLockOrNot(AadhaarSeedDTO aadharSeedDTO, String user) {
		if (aadharSeedDTO.getUserId() != null && aadharSeedDTO.getUserId().equals(user)
				&& aadharSeedDTO.getLock() != null && aadharSeedDTO.getLock()) {
			logger.info("Aadhar seeding application alredy locked with same user userId [{}]", user,
					"And application No[{}]", aadharSeedDTO.getId());
		} else if (aadharSeedDTO.getUserId() != null && aadharSeedDTO.getLock() == null) {
			setLock(aadharSeedDTO, user);
		} else if (aadharSeedDTO.getUserId() == null && aadharSeedDTO.getLock() == null) {
			setLock(aadharSeedDTO, user);
			logger.info("Aadhar seeding application locked with userId [{}]", user, "And application No[{}]",
					aadharSeedDTO.getId());
		} else if (aadharSeedDTO.getUserId() == null && !aadharSeedDTO.getLock()) {
			setLock(aadharSeedDTO, user);
			logger.info("Aadhar seeding application locked with userId [{}]", user, "And application No[{}]",
					aadharSeedDTO.getId());
		} else {
			throw new BadRequestException("Another RTO Processed this record[" + user + "]");
		}
	}

	private void clearList(List<?> list) {
		if (null != list && !list.isEmpty()) {
			list.clear();
		}
	}

}
