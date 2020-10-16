package org.epragati.registration.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.common.vo.UserStatusEnum;
import org.epragati.exception.BadRequestException;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dao.ApprovalProcessFlowDAO;
import org.epragati.master.dao.DealerActionDetailsDAO;
import org.epragati.master.dao.DealerCovDAO;
import org.epragati.master.dao.DealerMakerDAO;
import org.epragati.master.dao.DealerRegDAO;
import org.epragati.master.dao.MakersDAO;
import org.epragati.master.dao.MasterCovDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dto.ActionDetailsDTO;
import org.epragati.master.dto.ApplicantDetailsDTO;
import org.epragati.master.dto.ApprovalProcessFlowDTO;
import org.epragati.master.dto.ContactDTO;
import org.epragati.master.dto.DealerActionDetailsDTO;
import org.epragati.master.dto.DealerCovDTO;
import org.epragati.master.dto.DealerMakerDTO;
import org.epragati.master.dto.DealerRegDTO;
import org.epragati.master.dto.MakersDTO;
import org.epragati.master.dto.MasterCovDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.mappers.DealerRegMapper;
import org.epragati.master.mappers.MakersMapper;
import org.epragati.master.mappers.UserMapper;
import org.epragati.master.vo.DealerRegVO;
import org.epragati.master.vo.MakersVO;
import org.epragati.master.vo.UserVO;
import org.epragati.payments.vo.ClassOfVehiclesVO;
import org.epragati.registration.service.DealerRegistrationService;
import org.epragati.regservice.dto.ActionDetails;
import org.epragati.regservice.vo.ApplicationSearchVO;
import org.epragati.sequence.SequenceGenerator;
import org.epragati.util.RoleEnum;
import org.epragati.util.StatusRegistration;
import org.epragati.util.document.Sequence;
import org.epragati.util.payment.GatewayTypeEnum;
import org.epragati.util.payment.ModuleEnum;
import org.epragati.util.payment.ServiceEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author sairam.cheruku
 *
 */

@Service
public class DealerRegistrationServiceImpl implements DealerRegistrationService {

	private static final Logger logger = LoggerFactory.getLogger(DealerRegistrationServiceImpl.class);

	@Autowired
	private DealerRegDAO dealerDAO;

	@Autowired
	private DealerRegMapper dealerRegMapper;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private SequenceGenerator sequenceGenerator;

	@Autowired
	private ApprovalProcessFlowDAO approvalProcessFlowDAO;

	@Autowired
	private OfficeDAO officeDAO;

	@Autowired
	private DealerActionDetailsDAO dealerActionDetailsDAO;

	@Autowired
	private DealerCovDAO dealerCovDAO;

	@Autowired
	private MasterCovDAO masterCovDAO;
	
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private MakersDAO makersDAO;
	
	@Autowired
	private MakersMapper makersMapper;
	
	@Override
	public Optional<DealerRegVO> saveDetails(String dealerregVOString, MultipartFile[] uploadfiles) {

		if ((dealerregVOString == null)) {
			logger.error("dealerregVO is required.");
			throw new BadRequestException("dealerregVO is required.");
		}

		logger.debug("regServiceVO [{}] ", dealerregVOString);

		if (StringUtils.isBlank(dealerregVOString)) {
			logger.error("regServiceVO is required.");
			throw new BadRequestException("regServiceVO is required.");
		}

		Optional<DealerRegVO> dealerregVOOptional = readValue(dealerregVOString, DealerRegVO.class);

		DealerRegVO dealerregVO = dealerregVOOptional.get();
		DealerRegDTO dealerDTO = dealerRegMapper.convertVO(dealerregVO);
		
		if(dealerDTO.getServiceType().contains(ServiceEnum.DEALERREGISTRATION)) {
			//TODO: Enable below method before push it to production

			//Disabled below validation as per Murthy inputs on 14-10-2019    
			
			//doValidateBeforeFreshDealerRegistration(dealerDTO);
			
			if (dealerregVO.getApplicantDetails() != null && dealerregVO.getApplicantDetails().getPresentAddress() != null
					&& dealerregVO.getApplicantDetails().getPresentAddress().getMandal() != null && StringUtils.isNotBlank(
							dealerregVO.getApplicantDetails().getPresentAddress().getMandal().getNonTransportOffice())) {
				dealerDTO.setOfficeCode(
						dealerregVO.getApplicantDetails().getPresentAddress().getMandal().getNonTransportOffice());
			} else {
				throw new BadRequestException("Madal Details are not avilable");
			}
		}
		else if(dealerDTO.getServiceType().contains(ServiceEnum.DEALERSHIPRENEWAL)) {
			validateAndSetDetailsBeforeRenewal(dealerDTO);
		}
		
		dealerDTO.setOfficeDetails(officeDAO.findByOfficeCode(dealerDTO.getOfficeCode()).get());
		dealerDTO.setApplicationStatus(StatusRegistration.INITIATED);

		Map<String, String> officeCodeMap = new TreeMap<>();
		officeCodeMap.put("officeCode", dealerDTO.getOfficeCode());
		dealerDTO.setApplicationNo(
				sequenceGenerator.getSequence(String.valueOf(Sequence.DEALERAPPNO.getSequenceId()), officeCodeMap));
		updateActionsDetails(dealerDTO);

		dealerDTO.setApplicationStatus(StatusRegistration.PAYMENTPENDING);
		dealerDTO.setCreatedDate(LocalDateTime.now());
		dealerDTO.setPaymentTransactionNo(UUID.randomUUID().toString());
		dealerDAO.save(dealerDTO);

		return Optional.of(dealerRegMapper.convertEntity(dealerDTO));
	}
	
	/**
	 * Disabled below validation as per Murthy inputs on 14-10-2019 with one GSTN number more then one dealer can exists
	 * 
	 * @param dealerDTO
	 */
	@SuppressWarnings("unused")
	private void doValidateBeforeFreshDealerRegistration(DealerRegDTO dealerDTO) {

		Optional<DealerRegDTO> dealerDeatils = dealerDAO
				.findByGstnDataGstinNoOrderByCreatedDateDesc(dealerDTO.getGstnData().getGstinNo());

		if (dealerDeatils.isPresent()) {
			throw new BadRequestException(
					"Dealer already exists with this GSTN number :" + dealerDTO.getGstnData().getGstinNo());
		}

	}

	private void validateAndSetDetailsBeforeRenewal(DealerRegDTO dealerDTO) {
		long days = 0;
		Optional<UserDTO> userDetails = userDAO.findByUserId(dealerDTO.getDealerUserId());
		if (!userDetails.isPresent()) {
			throw new BadRequestException("No records found with this user ID : " + dealerDTO.getDealerUserId());
		}

		if (userDetails.get().getValidTo() != null) {
			days = ChronoUnit.DAYS.between(LocalDate.now(), userDetails.get().getValidTo());
		}
		if (days > 30) {
			String formattedDate = userDetails.get().getValidTo().format(DateTimeFormatter.ofPattern("dd-MMM-yyyy"));
			throw new BadRequestException("Your dealership will expire on " + formattedDate
					+ " Please apply before 30 days of your dealership expiry");
		}

		Optional<DealerRegDTO> dealerRegDTO = dealerDAO
				.findByDealerUserIdOrderByCreatedDate(dealerDTO.getDealerUserId());
		if (dealerRegDTO.isPresent()
				&& !dealerRegDTO.get().getApplicationStatus().equals(StatusRegistration.APPROVED)) {
			throw new BadRequestException("Your application is already processed with the application number"
					+ dealerRegDTO.get().getApplicationNo()
					+ "please check status at https://aprtaregistrations.epragathi.org/#/check-dealership-status");
		}

		ApplicantDetailsDTO applicantDTO = new ApplicantDetailsDTO();

		if (dealerDTO.getDealerAddress() != null && dealerDTO.getDealerAddress().getMandal() != null
				&& StringUtils.isNotBlank(dealerDTO.getDealerAddress().getMandal().getNonTransportOffice())) {
			dealerDTO.setOfficeCode(dealerDTO.getDealerAddress().getMandal().getNonTransportOffice());
		}

		ContactDTO contactDTO = new ContactDTO();
		if (StringUtils.isNotEmpty(userDetails.get().getMobile())
				&& StringUtils.isNotEmpty(userDetails.get().getEmail())) {
			contactDTO.setMobile(userDetails.get().getMobile());
			contactDTO.setEmail(userDetails.get().getEmail());
		}
		if (StringUtils.isNotEmpty(userDetails.get().getFirstname())) {
			applicantDTO.setFirstName(userDetails.get().getFirstname());
		} else if (StringUtils.isNotEmpty(userDetails.get().getFirstName())) {
			applicantDTO.setFirstName(userDetails.get().getFirstName());
		}
		applicantDTO.setContact(contactDTO);
		dealerDTO.setApplicantDetails(applicantDTO);
		dealerDTO.setExistingUserDetails(userDetails.get());
	}

	private DealerRegDTO updateActionsDetails(DealerRegDTO dto) {
		ActionDetailsDTO actionDetailsDTO = new ActionDetailsDTO();

		actionDetailsDTO.setActionBy("Citizen");
		actionDetailsDTO.setActionDatetime(LocalDateTime.now());
		actionDetailsDTO.setAction(dto.getApplicationStatus().getDescription());

		return dto;

	}

	public <T> Optional<T> readValue(String value, Class<T> valueType) {

		try {
			return Optional.of(objectMapper.readValue(value, valueType));
		} catch (IOException ioe) {

			logger.error("Exception occured while converting String to Object", ioe);
			throw new BadRequestException("Please Pass Valid Data.");
		}

	}

	@Override
	public void initiateApprovalProcessFlow(DealerRegDTO dealerRegistrationDTO) {
		if (CollectionUtils.isEmpty(dealerRegistrationDTO.getServiceIds())) {
			logger.error("Service ids not found");
			throw new BadRequestException("Service ids not found.");
		}

		Integer serviceId = getServiceId(dealerRegistrationDTO);
		List<ApprovalProcessFlowDTO> approvalProcessFlowDTO = approvalProcessFlowDAO.findByServiceId(serviceId);

		Integer iterator = (dealerRegistrationDTO.getIterationCount() == null) ? 1
				: dealerRegistrationDTO.getIterationCount() + 1;
		dealerRegistrationDTO.setIterationCount(iterator);
		List<ActionDetails> actionDetailsList = new ArrayList<>();
		approvalProcessFlowDTO.stream().forEach(a -> {

			Boolean isProceed = Boolean.TRUE;
			if (1 != iterator.intValue() && null != a.getHeigherAuthor()) {// check AO status.
				ActionDetails actionDetail = getActionDetailByRole(dealerRegistrationDTO, a.getHeigherAuthor());
				if (!actionDetail.getIsDoneProcess()) {
					actionDetail = getActionDetailByRole(dealerRegistrationDTO, a.getRole());
				}
				if (RoleEnum.CCO.getName().equals(actionDetail.getRole())
						|| StatusRegistration.APPROVED.getDescription().equals(actionDetail.getStatus())) {
					actionDetailsList.add(actionDetail);
					isProceed = false;
				}
			}
			if (isProceed) {
				actionDetailsList.add(new org.epragati.regservice.dto.ActionDetails(a.getRole(),
						ModuleEnum.CITIZEN.getCode(), iterator, Boolean.FALSE, a.getNextIndex(), a.getIndex()));// Default
																												// initial
			}

		});
		Integer currentIndex = actionDetailsList.stream().filter(a -> !a.getIsDoneProcess())
				.sorted((a1, a2) -> a1.getIndex().compareTo(a2.getIndex())).findFirst().get().getIndex();

		dealerRegistrationDTO.setCurrentIndex(currentIndex);
		dealerRegistrationDTO.setActionDetails(actionDetailsList);
		setCurrentRole(dealerRegistrationDTO);

	}

	private Integer getServiceId(DealerRegDTO dealerRegistrationDTO) {

		if (dealerRegistrationDTO.getFlowId() != null) {
			return dealerRegistrationDTO.getFlowId().getId();
		}

		Set<Integer> serviceId = new HashSet<>();
		serviceId.addAll(dealerRegistrationDTO.getServiceIds());
		if (dealerRegistrationDTO.getServiceIds().size() > 1) {
			Optional<ServiceEnum> serviceEnumOpt = ServiceEnum.getContainsMVIRequiredService(serviceId);
			if (serviceEnumOpt.isPresent()) {
				return serviceEnumOpt.get().getId();
			}
		}
		Set<Integer> serviceIds = new HashSet<>();
		dealerRegistrationDTO.getServiceIds().forEach(id -> {
			if (!ServiceEnum.getApprovalNotRequiredService().contains(id)) {
				serviceIds.add(id);
			}
		});
		return serviceIds.iterator().next();
	}

	private ActionDetails getActionDetailByRole(DealerRegDTO dealerRegistrationDTO, String role) {

		Optional<ActionDetails> actionDetailsOpt = dealerRegistrationDTO.getActionDetails().stream()
				.filter(p -> role.equals(p.getRole())).findFirst();
		if (!actionDetailsOpt.isPresent()) {
			logger.error("User role [{}] specific details not found in action detail", role);
			throw new BadRequestException("User role " + role + " specific details not found in actiondetail");
		}
		return actionDetailsOpt.get();
	}

	private void setCurrentRole(DealerRegDTO dealerRegistrationDTO) {
		if (1 == dealerRegistrationDTO.getIterationCount() || null != dealerRegistrationDTO.getCurrentRoles()) {
			dealerRegistrationDTO.setCurrentRoles(dealerRegistrationDTO.getActionDetails().stream()
					.filter(a -> !a.getIsDoneProcess() && dealerRegistrationDTO.getCurrentIndex().equals(a.getIndex()))
					.map(a -> a.getRole()).collect(Collectors.toCollection(LinkedHashSet::new)));
		}
	}

	@Override
	public Optional<DealerRegVO> dealerRegistrationService(ApplicationSearchVO applicationSearchVO) {

		Optional<DealerRegDTO> dealerDTO = null;
		if (StringUtils.isNotEmpty(applicationSearchVO.getApplicationNo())) {
			dealerDTO = dealerDAO.findByApplicationNo(applicationSearchVO.getApplicationNo());
		} else if (StringUtils.isNotEmpty(applicationSearchVO.getGstnNo())) {
			dealerDTO = dealerDAO.findByGstnDataGstinNoOrderByCreatedDateDesc(applicationSearchVO.getGstnNo());
		} else if (StringUtils.isNotEmpty(applicationSearchVO.getUserId())) {
			dealerDTO = dealerDAO.findByDealerUserIdOrderByCreatedDate(applicationSearchVO.getUserId());
		}
		else {
			throw new BadRequestException("Required values are missing");
		}
		if (dealerDTO.isPresent()) {
			return dealerRegMapper.convertEntity(dealerDTO);
		}

		return Optional.empty();
	}

	@Override
	public void saveDealerDetailsinDealerRegistrationCollection(UserDTO userDTO, DealerRegVO dealerRegVO,
			JwtUser jwtUser, HttpServletRequest httpRequest) {
		DealerActionDetailsDTO dto = new DealerActionDetailsDTO();
		dto.setDealerUserId(userDTO.getUserId());
		dto.setApplicationStatus(dealerRegVO.getActionStatus());
		dto.setDealerDetails(userDTO);
		dto.setIpAddress(httpRequest.getRemoteAddr());
		if(dealerRegVO.getActionStatus().equals(StatusRegistration.REVOKED)) {
			savingOfRevocationDetails(userDTO, jwtUser, dto);
		}else {
			if (dealerRegVO.getActionStatus().equals(StatusRegistration.SUSPEND)) {
				dto.setSuspendedBy(jwtUser.getId());
				dto.setSuspendedFrom(dealerRegVO.getSuspendedFrom());
				dto.setSuspendedTo(dealerRegVO.getSuspendedTo());
			} else if (dealerRegVO.getActionStatus().equals(StatusRegistration.CANCELED)) {
				dto.setCancelledBy(jwtUser.getId());
				dto.setCancelledDate(LocalDate.now());
			}
			dto.setOfficeDetails(officeDAO.findByOfficeCode(jwtUser.getOfficeCode()).get());
			dto.setClassOfVehicles(setClassOfVehicles(userDTO));
			dealerActionDetailsDAO.save(dto);
		}
		
	}

	/**
	 * Revocation Details Save
	 * 
	 * @param userDTO
	 * @param jwtUser
	 * @param dto
	 */
	private void savingOfRevocationDetails(UserDTO userDTO, JwtUser jwtUser, DealerActionDetailsDTO dto) {
		Optional<DealerActionDetailsDTO> suspensionRecord = dealerActionDetailsDAO
				.findByDealerUserIdAndApplicationStatusOrderByLUpdateDesc(userDTO.getUserId(),
						StatusRegistration.SUSPEND);
		dto.setApplicationStatus(dto.getApplicationStatus());
		dto.setDealerDetails(userDTO);
		dto.setIpAddress(dto.getIpAddress());
		suspensionRecord.get().setRevokedBy(jwtUser.getId());
		suspensionRecord.get().setRevokedDate(LocalDate.now());
		dealerActionDetailsDAO.save(suspensionRecord.get());
	}

	private List<MasterCovDTO> setClassOfVehicles(UserDTO userDTO) {
		List<DealerCovDTO> covDetails = dealerCovDAO.findByRIdAndStatusTrue(userDTO.getRid());
		List<String> covCodes = new ArrayList<>();
		List<MasterCovDTO> masterCovs = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(covDetails)) {
			covCodes = covDetails.stream().map(val -> val.getCovId()).collect(Collectors.toList());
		}
		if (CollectionUtils.isNotEmpty(covCodes)) {
			masterCovs = masterCovDAO.findByCovcodeInAndDealerCovTrue(covCodes);
		}
		if (CollectionUtils.isNotEmpty(masterCovs)) {
			return masterCovs;
		}
		return null;
	}

	@Override
	public Optional<DealerRegVO> doDealerRePay(String applicationFormNo) {
		Optional<DealerRegDTO> regDTO = dealerDAO.findByApplicationNo(applicationFormNo);
		if (!regDTO.isPresent()) {
			throw new BadRequestException("No records found with this application number : " + applicationFormNo);
		}
		if (!regDTO.get().getApplicationStatus().equals(StatusRegistration.PAYMENTFAILED)) {
			throw new BadRequestException("Invalid Status to do Repay : " + regDTO.get().getApplicationStatus());
		}
		regDTO.get().setGateWayType(GatewayTypeEnum.CFMS.getDescription());
		return dealerRegMapper.convertEntity(regDTO);
	}

	@Override
	public void updatePaymentStatusOfDealerRegistrationDetails(DealerRegVO dealerRegVO) {
		Optional<DealerRegDTO> regDTO = dealerDAO.findByApplicationNo(dealerRegVO.getApplicationNo());
		if (!regDTO.isPresent()) {
			throw new BadRequestException(
					"No records found with this application number : " + dealerRegVO.getApplicationNo());
		}
		regDTO.get().setApplicationStatus(StatusRegistration.PAYMENTPENDING);
		dealerDAO.save(regDTO.get());
	}

	@Override
	public List<UserVO> getListOfDealers() {
		List<UserDTO> userList = userDAO.findByPrimaryRoleNameAndUserStatus(RoleEnum.DEALER.getName(),
				UserStatusEnum.ACTIVE);
		return userMapper.convertEntity(userList);
	}

	@Override
	public List<MakersVO> getListOfMakers(List<ClassOfVehiclesVO> covs) {
		if (CollectionUtils.isNotEmpty(covs)) {
			List<String> classOfVehiclesList = covs.stream().map(val -> val.getCovCode()).collect(Collectors.toList());
		}
		return makersMapper.convertDTOs(makersDAO.findAll());
	}
	
	@Autowired
	private DealerMakerDAO dealerMakerDAO;

	@Override
	public Optional<DealerRegVO> getExistingCovsAndMakers(JwtUser jwtUser) {
		DealerRegVO dealerVO = new DealerRegVO();
		List<Integer> mmIdList = new ArrayList<>();
		List<String> covCodes = new ArrayList<>();
		List<MasterCovDTO> masterCovs = new ArrayList<>();
		List<ClassOfVehiclesVO> covs = new ArrayList<>();
		UserDTO masterUserDTO = userDAO.findByUserIdAndStatusTrue(jwtUser.getId());
		Integer dealerId = masterUserDTO.getRid();
		List<DealerCovDTO> covDetails = dealerCovDAO.findByRIdAndStatusTrue(dealerId);
		List<DealerMakerDTO> dealerMakerDTO = dealerMakerDAO.findByRIdAndStatusTrue(dealerId);

		if (dealerMakerDTO.isEmpty()) {
			logger.error("Dealer maker details is not found with this dealer details [{}]",
					masterUserDTO.getUserName());
			throw new BadRequestException("Maker details is not found with this dealer Details");
		}

		dealerMakerDTO.stream().forEach(d -> mmIdList.add(d.getMmId()));

		List<MakersDTO> makersDetails = makersDAO.findByMidInAndStatusTrue(mmIdList);

		if (makersDetails.isEmpty()) {
			logger.error("Maker details in not present with this MMid[{}]", mmIdList);
			throw new BadRequestException("Maker details in not present with this MMid" + mmIdList);
		}

		List<MakersVO> makerDetailsVO = makersMapper.convertEntity(makersDetails);
		
		if (CollectionUtils.isNotEmpty(covDetails)) {
			covCodes = covDetails.stream().map(val -> val.getCovId()).collect(Collectors.toList());
		}
		if (CollectionUtils.isNotEmpty(covCodes)) {
			masterCovs = masterCovDAO.findByCovcodeInAndDealerCovTrue(covCodes);
		}
		if (CollectionUtils.isNotEmpty(masterCovs)) {
			 covs  = getClassOfVeicles(masterCovs);
		}

		dealerVO.setCovs(covs);
		dealerVO.setMakers(makerDetailsVO);
		return Optional.of(dealerVO);
	}

	private List<ClassOfVehiclesVO> getClassOfVeicles(List<MasterCovDTO> masterCovs) {
		List<ClassOfVehiclesVO> covs = new ArrayList<>();
		masterCovs.stream().forEach(val -> {
			ClassOfVehiclesVO vo = new ClassOfVehiclesVO();
				vo.setCovCode(val.getCovcode());
				vo.setCovdescription(val.getCovdescription());
				covs.add(vo);
		});
		
		return covs;
	}

}
