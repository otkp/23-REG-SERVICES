package org.epragati.motordrivingschool.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.epragati.common.dao.PropertiesDAO;
import org.epragati.common.dto.PropertiesDTO;
import org.epragati.common.vo.UserStatusEnum;
import org.epragati.constants.OfficeType;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.FcDetailsDAO;
import org.epragati.master.dao.MasterUsersDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.TaxDetailsDAO;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dto.FcDetailsDTO;
import org.epragati.master.dto.MasterUsersDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.RolesDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.mappers.FcDetailsMapper;
import org.epragati.master.mappers.OfficeMapper;
import org.epragati.master.mappers.RegistrationDetailsMapper;
import org.epragati.master.vo.FcDetailsVO;
import org.epragati.master.vo.MotorDrivingSchoolVO;
import org.epragati.master.vo.OfficeVO;
import org.epragati.master.vo.RegistrationDetailsVO;
import org.epragati.motordrivingschool.service.MotorDrivingSchoolService;
import org.epragati.permits.dao.PermitDetailsDAO;
import org.epragati.permits.dto.PermitDetailsDTO;
import org.epragati.regservice.vo.PUCDetailsVO;
import org.epragati.util.PermitsEnum;
import org.epragati.util.StatusRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service

public class MotorDrivingSchoolServiceImpl implements MotorDrivingSchoolService {

	private static final Logger logger = LoggerFactory.getLogger(MotorDrivingSchoolServiceImpl.class);

	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;

	@Autowired
	private PermitDetailsDAO permitDetailsDAO;

	@Autowired
	private FcDetailsDAO fcDetailsDAO;

	@Value("${reg.mdsdriver.details.url:http://localhost:8989/reg/getVehicleDetails}")
	private String driverDetailsUrl;

	@Autowired
	private OfficeDAO officeDAO;

	@Autowired
	private TaxDetailsDAO taxDetailsDAO;

	@Autowired
	private MasterUsersDAO masterUsersDAO;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private OfficeMapper officeMapper;

	@SuppressWarnings("rawtypes")
	@Autowired
	private RegistrationDetailsMapper registrationDetailsMapper;

	@Autowired
	private FcDetailsMapper fcDetailsMapper;

	@Autowired
	private PropertiesDAO propertiesDAO;

	@Override
	public List<MotorDrivingSchoolVO> getVehicleDetails(List<String> prNos, String schoolType) {
		long startTime1 = System.currentTimeMillis();

		List<MotorDrivingSchoolVO> vehicleDetails = new ArrayList<>();

		List<String> expectedStatus = Arrays.asList(StatusRegistration.CANCELED.getDescription(),
				StatusRegistration.SUSPEND.getDescription(), StatusRegistration.TheftState.INTIMATIATED.toString(),
				StatusRegistration.TheftState.OBJECTION.toString(), StatusRegistration.TheftState.REVOKED.toString());
		MotorDrivingSchoolVO drivingDetails = new MotorDrivingSchoolVO();
		RegistrationDetailsVO vo = new RegistrationDetailsVO();

		long q1StartTime = System.currentTimeMillis();
		//List<RegistrationDetailsDTO> regDetails = registrationDetailDAO.findByPrNoIn(prNos);
		logger.info("Total time to execute findByPrNoIn :{}", (System.currentTimeMillis() - q1StartTime));

		//List<RegistrationDetailsDTO> regOptional = null;
		//		if (CollectionUtils.isEmpty(regDetails)) {
		//			logger.error("No record found for PrNo: [{}]", prNos);
		//			throw new BadRequestException("No record found for PrNo: " + prNos);
		//		}
		//		regOptional = regDetails.stream().filter(a -> !expectedStatus.contains(a.getApplicationStatus()))
		//				.collect(Collectors.toList());
		q1StartTime = System.currentTimeMillis();
		Optional<PropertiesDTO> propertiesDetails = propertiesDAO
				.findByModule(StatusRegistration.DRIVINGSCHOOL.getDescription());
		logger.info("Total time to execute findByModule :{}", (System.currentTimeMillis() - q1StartTime));
		if(!propertiesDetails.isPresent() ) {
			throw new BadRequestException("No master data found");
		}
		PropertiesDTO propDetails = propertiesDetails.get();
		for (String  prNo : prNos) {
			q1StartTime = System.currentTimeMillis();
			Optional<RegistrationDetailsDTO> optionalRegDoc = registrationDetailDAO.findByPrNo(prNo);
			logger.info("Total time to execute findByPrNo :{}", (System.currentTimeMillis() - q1StartTime));
			if(!optionalRegDoc.isPresent()) {
				throw new BadRequestException("No record found for PrNo: " + prNo);
			}
			RegistrationDetailsDTO registrationDetailsDTO=optionalRegDoc.get();
			if(expectedStatus.contains(registrationDetailsDTO.getApplicationStatus())) {
				throw new BadRequestException("Invalid status : "+registrationDetailsDTO.getApplicationStatus() +", for prNo: " + prNo);
			}
			if (registrationDetailsDTO.getClassOfVehicle() == null)
			{
				throw new BadRequestException("No cov found for prNoS: " + prNos);
			}

			if (!propDetails.getCovs().contains(registrationDetailsDTO.getClassOfVehicle())
					|| registrationDetailsDTO.getVahanDetails().getGvw() < propDetails.getMinGvwLimit())

			{
				throw new BadRequestException("classOfVehicle must be " + propertiesDetails.get().getCovs()
						+ " and GVW should be greater than "+propDetails.getMinGvwLimit());
			}

			drivingDetails.setOwnerName(registrationDetailsDTO.getApplicantDetails().getDisplayName());
			if (registrationDetailsDTO.getPucDetailsDTO() != null) {
				PUCDetailsVO pucDetailsVO = new PUCDetailsVO();
				pucDetailsVO.setValidFrom(registrationDetailsDTO.getPucDetailsDTO().getValidFrom());
				pucDetailsVO.setValidTo(registrationDetailsDTO.getPucDetailsDTO().getValidTo());

			}

			if (!schoolType.equalsIgnoreCase("B")) {
				if (!registrationDetailsDTO.getVehicleType().equalsIgnoreCase(schoolType)) {
					throw new BadRequestException("Vehicle Type mismatch");
				}
			}

			if (!registrationDetailsDTO.getApplicantDetails().getIsAadhaarValidated()) {
				throw new BadRequestException("Your Aadhar is not seeded..pls go for aadhar seeding");
			}

			if (registrationDetailsDTO.getTaxvalidity().isBefore(LocalDate.now())) {
				throw new BadRequestException("Tax Validity Expired....");
			}
			q1StartTime = System.currentTimeMillis();
			Optional<PermitDetailsDTO> listOfPermits = permitDetailsDAO
					.findTopByPrNoInAndPermitStatusOrderByCreatedDateDesc(prNos,
							PermitsEnum.ACTIVE.getDescription());
			logger.info("Total time to execute findTopByPrNoInAndPermitStatusOrderByCreatedDateDesc :{}",
					(System.currentTimeMillis() - q1StartTime));

			if (!listOfPermits.isPresent()) {
				throw new BadRequestException("No permit");
			}
			PermitDetailsDTO permitDetails = listOfPermits.get();
			if (permitDetails.getPermitValidityDetails().getPermitValidTo().isBefore(LocalDate.now())) {
				throw new BadRequestException("Permit Validity Expired");
			}
			registrationDetailsDTO.setPermitDetails(permitDetails);
			drivingDetails.setPermitType(permitDetails.getPermitType().getPermitType());
			drivingDetails.setPermitValidity(permitDetails.getPermitValidityDetails().getPermitValidTo());
			q1StartTime = System.currentTimeMillis();
			Optional<FcDetailsDTO> listOfcDetails = fcDetailsDAO
					.findTopByStatusIsTrueAndPrNoInOrderByCreatedDateDesc(prNos);
			logger.info("Total time to execute findTopByStatusIsTrueAndPrNoInOrderByCreatedDateDesc :{}",
					(System.currentTimeMillis() - q1StartTime));

			if (!listOfcDetails.isPresent()) {
				logger.error("FC details not found: [{}]", prNos);
				throw new BadRequestException("FC details not found:" + prNos);
			}
			FcDetailsDTO fcDetails = listOfcDetails.get();
			if (fcDetails.getFcValidUpto().isBefore(LocalDate.now())) {
				throw new BadRequestException("vehicle Fitness validity Expired..");
			}
			FcDetailsVO fitnessDetails = fcDetailsMapper.convertEntity(fcDetails);
			drivingDetails.setFitnessValidity(fcDetails.getFcValidUpto());
			vo.setFcDetailsVO(fitnessDetails);
			q1StartTime = System.currentTimeMillis();
			Optional<TaxDetailsDTO> taxDetailsList = taxDetailsDAO
					.findTopByApplicationNoOrderByCreatedDateDesc(registrationDetailsDTO.getApplicationNo());
			logger.info("Total time to execute findTopByApplicationNoOrderByCreatedDateDesc :{}",
					(System.currentTimeMillis() - q1StartTime));

			if (!taxDetailsList.isPresent()) {
				logger.error("tax details not found [{}]", prNos);
				throw new BadRequestException("tax details not found. " + prNos);
			}
			TaxDetailsDTO taxDetails = taxDetailsList.get();
			if (taxDetails.getTaxPeriodEnd().isBefore(LocalDate.now())) {
				throw new BadRequestException("tax validity expired");
			}
			registrationDetailsDTO.setTaxvalidity(taxDetails.getTaxPeriodEnd());

			vo = registrationDetailsMapper.convertEntity(registrationDetailsDTO);

			drivingDetails.setRegistrationDetails(vo);
			drivingDetails.setSchoolAddress(null);
			// return drivingDetails;
			vehicleDetails.add(drivingDetails);
		}

		logger.info("Total time to execute getVehicleDetails :{}", (System.currentTimeMillis() - startTime1));

		return vehicleDetails;
	}

	@Override
	public List<MotorDrivingSchoolVO> getMVIDetailsBasedonOffice(String officeCode) {
		long startTime1 = System.currentTimeMillis();
		List<MotorDrivingSchoolVO> vo = new ArrayList<>();
		String role = "MVI";

		long startTime = System.currentTimeMillis();
		List<MasterUsersDTO> masterUsersDTO = masterUsersDAO
				.findByOfficeOfficeCodeAndPrimaryRoleNameOrOfficeOfficeCodeAndAdditionalRoles(officeCode, role,
						officeCode, role);
		logger.info(
				"Total time to execute findByOfficeOfficeCodeAndPrimaryRoleNameOrOfficeOfficeCodeAndAdditionalRoles :{}",
				(System.currentTimeMillis() - startTime));

		if (!masterUsersDTO.isEmpty()) {
			for (MasterUsersDTO usersDto : masterUsersDTO) {
				MotorDrivingSchoolVO mviList = new MotorDrivingSchoolVO();
				mviList.setUserId(usersDto.getUserId());
				mviList.setUsername(usersDto.getUserName());
				mviList.setOfficeCode(usersDto.getOffice().getOfficeCode());
				mviList.setPrimaryRole(usersDto.getPrimaryRole().getName());
				vo.add(mviList);
			}
		}
		logger.info("Total time to execute getMVIDetailsBasedonOffice :{}", (System.currentTimeMillis() - startTime1));

		return vo;
	}

	@Override
	public void userIdGeneration(MotorDrivingSchoolVO vo) {
		long startTime1 = System.currentTimeMillis();
		UserDTO dto = new UserDTO();
		long startTime = System.currentTimeMillis();
		long days = 0;
		
		Optional<UserDTO> optDto = userDAO.findByUserId(vo.getUserId());
		logger.info("Total time to execute findByUserId :{}", (System.currentTimeMillis() - startTime));
if(optDto.isPresent()) {
		if(optDto.get().getValidTo() !=null)
		{
			days = ChronoUnit.DAYS.between(LocalDate.now(), optDto.get().getValidTo());
		}
		if (optDto.get().getValidTo() !=null && optDto.get().getValidTo().isAfter(LocalDate.now()) && days > 31) {
			logger.error("User is already Approved [{}]", optDto.get().getParentId());
			throw new BadRequestException("User is Already Approved By" + optDto.get().getParentId());
		}
		
		else if((optDto.get().getValidTo() !=null && optDto.get().getValidTo().isBefore(LocalDate.now())) || days <= 30)
		{
			

			optDto.get().setValidFrom(vo.getValidFrom());
			optDto.get().setValidTo(vo.getValidTo());
			userDAO.save(optDto.get());
		}
}
else {
		dto.setUserId(vo.getUserId());
		dto.setUserName(vo.getUserId());
		dto.setFirstName(vo.getOwnerName());
		dto.setPassword(vo.getPassword());
		dto.setIsAccountNonLocked(true);
		dto.setMobile(vo.getMobile());
		dto.setEmail(vo.getEmail());
		if (vo.getAadharNo() != null) {
			dto.setAadharNo(vo.getAadharNo());
		}
		if (vo.getOfficeCode() != null) {
			OfficeDTO officeVo = new OfficeDTO();
			officeVo.setOfficeCode(vo.getOfficeCode());
			dto.setOffice(officeVo);
		}
		dto.setCreatedDate(LocalDateTime.now());
		RolesDTO rolesDTO = new RolesDTO();

		rolesDTO.setName(StatusRegistration.DRIVINGSCHOOL.getDescription());

		dto.setPrimaryRole(rolesDTO);
		dto.setAdditionalRoles(Collections.emptyList());
		dto.setIsAccountNonLocked(Boolean.TRUE);
		dto.setUserLevel(1);
		dto.setUserStatus(UserStatusEnum.ACTIVE);
		dto.setPasswordResetRequired(Boolean.TRUE);
		dto.setStatus(Boolean.TRUE);
		dto.setValidFrom(vo.getValidFrom());
		dto.setValidTo(vo.getValidTo());

		userDAO.save(dto);
		logger.info("Total time to execute userIdGeneration :{}", (System.currentTimeMillis() - startTime1));
		}
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> map = new ConcurrentHashMap<>();
		return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	@Override
	public List<OfficeVO> getAllOffices(String officeCode) {
		long startTime1 = System.currentTimeMillis();

		Optional<OfficeDTO> officeDetails = null;
		// officeDetails = officeDAO.findByOfficeCode(officeCode);

		List<String> list = new ArrayList<>();
		list.add(OfficeType.UNI.getCode());
		list.add(OfficeType.MVI.getCode());
		list.add(OfficeType.RTA.getCode());
		long startTime = System.currentTimeMillis();

		officeDetails = officeDAO.findByOfficeCode(officeCode);
		logger.info("Total time to execute findByOfficeCode :{}", (System.currentTimeMillis() - startTime));
		startTime = System.currentTimeMillis();
		List<OfficeDTO> officeDetails1 = officeDAO.findByDistrictAndTypeIn(officeDetails.get().getDistrict(), list);
		logger.info("Total time to execute findByDistrictAndTypeIn :{}", (System.currentTimeMillis() - startTime));
		logger.info("Total time to execute getAllOffices :{}", (System.currentTimeMillis() - startTime1));

		return officeMapper.convertEntity(officeDetails1);
	}

	@Override
	public List<MotorDrivingSchoolVO> getRTAUsers(Set<String> officeCode) {
		long startTime1 = System.currentTimeMillis();
		Optional<OfficeDTO> officeDetails = null;

		List<MotorDrivingSchoolVO> vo = new ArrayList<>();
		long startTime = System.currentTimeMillis();
		officeDetails = officeDAO.findByOfficeCodeIn(officeCode);
		logger.info("Total time to execute findByOfficeCodeIn :{}", (System.currentTimeMillis() - startTime));

		if (!officeDetails.isPresent()) {
			throw new BadRequestException("No record found for the office code");
		}

		MotorDrivingSchoolVO mviList = new MotorDrivingSchoolVO();
		mviList.setReportingOfficeCode(officeDetails.get().getReportingoffice());

		vo.add(mviList);
		logger.info("Total time to execute findByOfficeCodeIn :{}", (System.currentTimeMillis() - startTime1));

		return vo;

	}

}
