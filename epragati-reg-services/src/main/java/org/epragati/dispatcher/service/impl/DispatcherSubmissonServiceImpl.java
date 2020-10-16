package org.epragati.dispatcher.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.epragati.common.dao.PropertiesDAO;
import org.epragati.common.dto.PropertiesDTO;
import org.epragati.constants.DispatchEnum;
import org.epragati.constants.DispatchEnum.DispatchCardReasonEnum;
import org.epragati.constants.OfficeType;
import org.epragati.dispatcher.dao.DispatcherSubmissionDAORepo;
import org.epragati.dispatcher.dto.DispatcherSubmissionDTO;
import org.epragati.dispatcher.mapper.DispatcherMapper;
import org.epragati.dispatcher.service.DispatcherSubmissionService;
import org.epragati.dispatcher.vo.DispatcherReportVO;
import org.epragati.dispatcher.vo.DispatcherSubmissionVO;
import org.epragati.dispatcher.vo.FormDetailsVo;
import org.epragati.exception.BadRequestException;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dao.CardDispatchDetailsDAO;
import org.epragati.master.dao.DistrictDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dto.CardDispatchDetailsDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.mappers.OfficeMapper;
import org.epragati.master.mappers.UserMapper;
import org.epragati.master.vo.OfficeVO;
import org.epragati.master.vo.UserVO;
import org.epragati.service.notification.MessageTemplate;
import org.epragati.service.notification.NotificationUtil;
import org.epragati.util.payment.ServiceEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class DispatcherSubmissonServiceImpl implements DispatcherSubmissionService {

	@Autowired
	DispatcherMapper mapper;

	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;

	@Autowired
	private DispatcherSubmissionDAORepo dispatcherSubmissionRepo;

	@Autowired
	private PropertiesDAO propertiesDAO;

	@Autowired
	private CardDispatchDetailsDAO cardDispatchDetailsDAO;

	@Autowired
	private NotificationUtil notifications;
	
	@Autowired
	private OfficeDAO officeDAO;
	
	@Autowired
	private DistrictDAO districtDAO;

	static Logger log = Logger.getLogger(DispatcherSubmissonServiceImpl.class.getName());
	
	
	

	@Override
	public String insertDetails(List<DispatcherSubmissionVO> dispatcherFormSubmissionDispatherVOs) {
		log.debug(dispatcherFormSubmissionDispatherVOs + " before mapper call from service VO");
		String result = null;
		List<DispatcherSubmissionDTO> disDTO = mapper.convertVO(dispatcherFormSubmissionDispatherVOs);

		log.debug(disDTO + " after mapper call from service DTO type");

		// DispatcherSubMapperAndServiceUtil dispatcherSubmissionUtil = new
		// DispatcherSubMapperAndServiceUtil();

		List<String> applicatioNumberUpdateSucc = new ArrayList<>();
		List<String> emsDuplicateList = new ArrayList<>();

		if (disDTO == null || disDTO.isEmpty()) {
			//log.error("At least complete one record");
			throw new BadRequestException("At least complete one record");
		}

		disDTO.forEach(dto -> {
			Optional<RegistrationDetailsDTO> regDTO = registrationDetailDAO.findByApplicationNo(dto.getApplicationNo());
			if (regDTO.isPresent()) {
				RegistrationDetailsDTO registrationDetailsDTO = regDTO.get();
				DispatcherSubmissionDTO dtoTemp = dispatcherSubmissionRepo.findByEmsNumber(dto.getEmsNumber());
				if (dtoTemp == null) {
					registrationDetailsDTO.setCardDispatched(true);
					registrationDetailsDTO.setDispatcherFormSubmissionDTO(dto);
					registrationDetailDAO.save(registrationDetailsDTO);
					dto.setCreatedDate(LocalDateTime.now());
					if (registrationDetailsDTO.getApplicantDetails().getPresentAddress() != null
							&& registrationDetailsDTO.getApplicantDetails().getPresentAddress().getPostOffice() != null
							&& registrationDetailsDTO.getApplicantDetails().getPresentAddress().getPostOffice()
									.getPostOfficeCode() != null) {
						dto.setPinCode(registrationDetailsDTO.getApplicantDetails().getPresentAddress().getPostOffice()
								.getPostOfficeCode().toString());
					}
					if (registrationDetailsDTO.getApplicantDetails() != null
							&& registrationDetailsDTO.getApplicantDetails().getContact() != null
							&& StringUtils.isNotBlank(
									registrationDetailsDTO.getApplicantDetails().getContact().getMobile())) {
						dto.setMobileNo(registrationDetailsDTO.getApplicantDetails().getContact().getMobile());
					}
					Optional<CardDispatchDetailsDTO> cardDTO = cardDispatchDetailsDAO.findByPrNo(dto.getPrNo());
					if (cardDTO.isPresent()) {

						CardDispatchDetailsDTO cardDispatchDTO = cardDTO.get();

						dto.setCardPrintedDate(cardDispatchDTO.getPrintedDateTime());
						dto.setCardPrintedBy(cardDispatchDTO.getPrintedBy());
						/*
						 * LocalDateTime printedDate =
						 * cardDispatchDTO.getPrintedDateTime().get(cardDispatchDTO.getPrintedBy().size(
						 * )); dto.setCardPrintedDate(cardDispatchDTO.getPrintedDateTime().get(
						 * cardDispatchDTO.getPrintedBy().size()-1));
						 */
					}
					notifications.sendNotifications(MessageTemplate.CARDDISPATCH_EMS.getId(), dto);
					dispatcherSubmissionRepo.save(dto);
					applicatioNumberUpdateSucc.add(dto.getPrNo());
				} else {
					emsDuplicateList.add(dto.getPrNo());
				}
			}
		});

		if (!(emsDuplicateList.isEmpty()) && !(applicatioNumberUpdateSucc.isEmpty())) {
			return result = applicatioNumberUpdateSucc.size()
					+ ":Records Dispatched Successfully and already EMS number is used for others please verify ems number of this PrNo:"
					+ emsDuplicateList.toString();
		}

		if (!(emsDuplicateList.isEmpty())) {
			return result = "Already EMS number is used for others please verify ems number of this PrNo:"
					+ emsDuplicateList.toString();
		}

		return result = applicatioNumberUpdateSucc.size() + ":Records Dispatched Successfully";

		// return dipatchValidation(dispatcherSubmissionUtil);

	}

	/**
	 * this service is used for retrieving details from dispatcher_details document
	 * for report creation
	 * 
	 * @author akhtar_masud
	 * @return
	 */

	@Override
	public DispatcherReportVO getDispatcherDetailsForReport(FormDetailsVo vo, String officeCode, Pageable pagable) {
		// TODO Auto-generated method stub
		long noOfRecords = 0;
		Page<DispatcherSubmissionDTO> dispatcherData = null;
		DispatcherReportVO dispatcherReportVO = new DispatcherReportVO();
		List<DispatcherSubmissionDTO> despatcherDto = new ArrayList<DispatcherSubmissionDTO>();
		Pageable pagable1 = new PageRequest(pagable.getPageNumber()-1, pagable.getPageSize());
		if (vo.getRegistrationType().equals(DispatchEnum.FETCH_BY_PRNO.getReqType())) {
			despatcherDto = dispatcherSubmissionRepo.findByPrNoAndOfficeCode(vo.getApplicationNo(), officeCode);
		} else if (vo.getRegistrationType().equals(DispatchEnum.FETCH_ALL_DETAILS.getReqType())) {
			despatcherDto = dispatcherSubmissionRepo.findByOfficeCode(officeCode);
		} else if (vo.getRegistrationType().equals(DispatchEnum.FETCH_BY_DATE.getReqType())) {
			Map<String, LocalDateTime> dates = mapper.stringToDateConvertor(vo.getFromDate(), vo.getToDate());
			LocalDateTime fromDate = dates.get("from");
			LocalDateTime toDate = dates.get("to");
			noOfRecords = dispatcherSubmissionRepo.countByPostedDateBetweenAndOfficeCode(fromDate, toDate, officeCode);

			if (noOfRecords != 0) {
				dispatcherData = dispatcherSubmissionRepo.findByPostedDateBetweenAndOfficeCode(fromDate, toDate,
						officeCode,pagable1);
			}
			if (dispatcherData == null) {
				//log.error("Records Not Found");
				throw new BadRequestException("Records Not Found");
			}
			despatcherDto = dispatcherData.getContent();
			dispatcherReportVO.setPageNo(dispatcherData.getNumber());
			dispatcherReportVO.setTotalPages(dispatcherData.getTotalPages());

		}
		//List<DispatcherSubmissionVO> dispatcherVO = mapper.convertEntity(despatcherDto);
		dispatcherReportVO.setDispatcherSubmissionVOList(mapper.convertEntity(despatcherDto));
		return dispatcherReportVO;

	}

	/**
	 * Dispatcher Validation for form submission after submiting data preparing
	 * messages
	 * 
	 * @param dispatcherUtil
	 * @return
	 */
	/*
	 * public GateWayResponse<?> dipatchValidation(DispatcherSubMapperAndServiceUtil
	 * dispatcherUtil) {
	 * 
	 * boolean flag=false; List<String> nullRecords = null;
	 * 
	 * if
	 * (!(DispatcherSubMapperAndServiceUtil.getAssignNullApplicationNo().isEmpty()))
	 * { nullRecords =
	 * DispatcherSubMapperAndServiceUtil.getAssignNullApplicationNo();
	 * 
	 * flag=true; }
	 * 
	 * if (dispatcherUtil != null) {
	 * 
	 * if (flag && dispatcherUtil.getEmsDuplicate() == null &&
	 * dispatcherUtil.getApplicatioNumberUpdateSucc() == null) {
	 * 
	 * return new GateWayResponse<>(HttpStatus.OK,
	 * dispatcherUtil.getApplicatioNumberUpdateSucc().size() +
	 * ": Records Successfully Dispatched" + dispatcherUtil.getEmsDuplicate() +
	 * ": EMS Duplicate found try with different EMS" + st +
	 * ": records are not save");
	 * 
	 * return new GateWayResponse<>(HttpStatus.OK, "No Records Dispatched");
	 * 
	 * } else if (flag &&
	 * !(dispatcherUtil.getApplicatioNumberUpdateSucc().isEmpty())) return new
	 * GateWayResponse<>(HttpStatus.OK,
	 * dispatcherUtil.getApplicatioNumberUpdateSucc().size() +
	 * ": Successfully Dispatched " + nullRecords + ": Records are not saved");
	 * 
	 * else if (flag && !dispatcherUtil.getEmsDuplicate().contains("1"))
	 * 
	 * return new GateWayResponse<>(HttpStatus.NOT_ACCEPTABLE,
	 * dispatcherUtil.getEmsDuplicate(), ":Already exist EMS number" + nullRecords +
	 * ": records are not save");
	 * 
	 * else if (flag) throw new BadRequestException("At least complete one record");
	 * 
	 * else if (!(dispatcherUtil.getApplicatioNumberUpdateSucc().isEmpty())) return
	 * new GateWayResponse<>(HttpStatus.OK,
	 * dispatcherUtil.getApplicatioNumberUpdateSucc().size() +
	 * ":Records Successfully Dispatched");
	 * 
	 * else return new GateWayResponse<>(HttpStatus.IM_USED,
	 * dispatcherUtil.getEmsDuplicate(), ": Already EMS number is exist");
	 * 
	 * } else { return new GateWayResponse<>(HttpStatus.BAD_REQUEST,
	 * "INTERNAL_ERROR"); } }
	 */

	@Override
	public List<String> getAllCardReasons() {
		PropertiesDTO result = propertiesDAO.findByCardReturnReasonsExistsTrue();
		return result.getCardReturnReasons();

	}

	@Override
	public DispatcherSubmissionVO getDetailsByregNo(String regNo) {
		DispatcherSubmissionVO vo = null;
		//Optional<DispatcherSubmissionDTO> dto = dispatcherSubmissionRepo.findByPrNoOrderByLUpdateDesc(regNo);
		Optional<DispatcherSubmissionDTO> dto = dispatcherSubmissionRepo.findByPrNoOrderByIdDesc(regNo);
		if (!dto.isPresent()) {
			throw new BadRequestException("No data found with this prNo: "+regNo);
		}
		vo = mapper.convertEntity(dto.get());
		if (dto.get().getReturnDate() != null || dto.get().getDeliveryDate() != null) {
			vo.setReturnDate(dto.get().getReturnDate());
			vo.setDeliveryDate(dto.get().getDeliveryDate());
			vo.setIsDateEdit(Boolean.TRUE);
		}

		return vo;
	}

	@Override
	public DispatcherSubmissionVO saveDetails(DispatcherSubmissionVO vo) {
		DispatcherSubmissionVO dispatcherSubmissionVO = null;
        //Optional<DispatcherSubmissionDTO> dto = dispatcherSubmissionRepo.findByPrNoOrderByLUpdateDesc(vo.getPrNo());
		Optional<DispatcherSubmissionDTO> dto = dispatcherSubmissionRepo.findByPrNoOrderByIdDesc(vo.getPrNo());
		DispatcherSubmissionDTO dispatcherSubmissionDTO = dto.get();
		Optional<RegistrationDetailsDTO> reg = registrationDetailDAO.findByPrNo(vo.getPrNo());
		if (vo.getDispatch().equals(DispatchEnum.RETURN)) {
			dispatcherSubmissionDTO.setReturnDate(vo.getReturnDate());
			dispatcherSubmissionDTO.setReturnReason(vo.getReturnReason());
			if (reg.isPresent()) {
				notifications.sendNotifications(ServiceEnum.CARDRETURN.getId(), reg.get());
			}
		} else if (vo.getDispatch().equals(DispatchEnum.DISPATCH)) {
			dispatcherSubmissionDTO.setDeliveryDate(vo.getDeliveryDate());
			dispatcherSubmissionDTO.setRemarks(vo.getRemarks());
			if (reg.isPresent()) {
				notifications.sendNotifications(ServiceEnum.CARDREDISPATCH.getId(), reg.get());
			}
		}
		dispatcherSubmissionDTO.setlUpdate(LocalDateTime.now());
		

		dispatcherSubmissionRepo.save(dispatcherSubmissionDTO);
		dispatcherSubmissionDTO.setIsDateEdit(true);
		dispatcherSubmissionVO = mapper.convertEntity(dispatcherSubmissionDTO);
		return dispatcherSubmissionVO;

	}

	@Override
	public DispatcherReportVO getDetails(DispatcherSubmissionVO dispatcherSubmissionVO, JwtUser jwtUser,Pageable pagable) {
		List<DispatcherSubmissionVO> voList = new ArrayList<>();
		LocalDateTime fromDate = dispatcherSubmissionVO.getFromDate().atStartOfDay();
		LocalDateTime toDate = dispatcherSubmissionVO.getToDate().atTime(23, 59, 59, 999);
		
		//LocalDateTime fromDate = getTimewithDate(dispatcherSubmissionVO.getFromDate(), false);
		//LocalDateTime toDate = getTimewithDate(dispatcherSubmissionVO.getToDate(), true);
		Page<DispatcherSubmissionDTO> dispatcherData = null;
		DispatcherReportVO dispatcherReportVO = new DispatcherReportVO(); 
		List<DispatcherSubmissionDTO> dtoList = new ArrayList<DispatcherSubmissionDTO>();
		Pageable pagable1 = new PageRequest(pagable.getPageNumber()-1, pagable.getPageSize());	
		if (dispatcherSubmissionVO.getDispatch().equals(DispatchEnum.DISPATCHDATE)) {
			dispatcherData = dispatcherSubmissionRepo.findByDeliveryDateBetweenAndOfficeCodeOrderByPrNoAsc(fromDate, toDate,
					jwtUser.getOfficeCode(),pagable1);
		} else if (dispatcherSubmissionVO.getDispatch().equals(DispatchEnum.RETURNDATE)) {
			//TODO:records are getting displayed including re-dispatched cards data.
			/*
			 * dispatcherData =
			 * dispatcherSubmissionRepo.findByReturnDateBetweenAndOfficeCodeOrderByPrNoAsc(
			 * fromDate.plusDays(1), toDate, jwtUser.getOfficeCode(),pagable1);
			 */
			
			dispatcherData = dispatcherSubmissionRepo
					.findByReturnDateBetweenAndRemarksAndReturnReasonInAndOfficeCodeOrderByPrNoAsc(fromDate.plusDays(1),
							toDate, DispatchEnum.DISPATCHED.getReqType(),
							Arrays.asList(DispatchCardReasonEnum.ADDRESSNOTTALLIED.getDescription(), DispatchCardReasonEnum.DOORLOCKED.getDescription(), DispatchCardReasonEnum.RECEIVERNOTPRESENTATHOME.getDescription()),
							jwtUser.getOfficeCode(),pagable1);
		}

		else if (dispatcherSubmissionVO.getDispatch().equals(DispatchEnum.CARDPRINTEDDATE)) {
			dispatcherData = dispatcherSubmissionRepo.findByCardPrintedDateBetweenAndOfficeCodeOrderByPrNoAsc(fromDate, toDate,
					jwtUser.getOfficeCode(),pagable1);
		}
		if (!dispatcherData.hasContent()) {
			throw new BadRequestException("No record found");
		}
		
		
		dtoList = dispatcherData.getContent();
		dtoList.stream().forEach(sl -> {
			DispatcherSubmissionVO vo = new DispatcherSubmissionVO();

			if (StringUtils.isNotBlank(sl.getEmsNumber())) {
				vo.setEmsNumber(sl.getEmsNumber());
			}
			if (StringUtils.isNotBlank(sl.getMobileNo())) {
				vo.setMobileNo(sl.getMobileNo());
			}
			if (StringUtils.isNotBlank(sl.getName())) {
				vo.setUserName(sl.getName());
			}
			if (StringUtils.isNotBlank(sl.getPrNo())) {
				vo.setPrNo(sl.getPrNo());
			}
			if (StringUtils.isNotBlank(sl.getRemarks())) {
				vo.setRemarks(sl.getRemarks());
			}
			if (StringUtils.isNoneBlank(sl.getDispatchedBy())) {
				vo.setDispatchedBy(sl.getDispatchedBy());
			}
			vo.setDeliveryDate(sl.getDeliveryDate());
			vo.setReturnDate(sl.getReturnDate());
			voList.add(vo);
		});
		
		dispatcherReportVO.setDispatcherSubmissionVOList(voList);
		dispatcherReportVO.setPageNo(dispatcherData.getNumber());
		dispatcherReportVO.setTotalPages(dispatcherData.getTotalPages());
		return dispatcherReportVO;
	}

	private LocalDateTime getTimewithDate(LocalDate date, boolean timeZone) {
		String dateVal = date + "T00:00:00.000Z";
		if (timeZone) {
			dateVal = date + "T23:59:59.999Z";
		}
		ZonedDateTime zdt = ZonedDateTime.parse(dateVal);
		return zdt.toLocalDateTime();
	}

	@Override
	public Optional<UserVO> getUserDetails(String userId, String officeCode) {
		Optional<UserDTO> dto = null;
		Optional<UserVO> vo =null;
		dto = userDAO.findByUserIdAndOfficeOfficeCode(userId, officeCode);
		vo = userMapper.convertEntity(dto);
		if(dto.isPresent() && !dto.get().getIsCardDispatchUser()) {
			vo.get().setIsCardDispatchUser(Boolean.FALSE);
		}
		return vo;
	}

	
	
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private UserMapper userMapper;
	/*
	 * @Override public List<DispatcherFormSubmissionDTO>
	 * getDispatcherDetailsByDate(SeFormDetailsVo vo) {
	 * 
	 * DateTimeFormatter DATEFORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
	 * LocalDate fromDate = LocalDate.parse(vo.getFromDate(), DATEFORMATTER);
	 * LocalDate toDate = LocalDate.parse(vo.getToDate(), DATEFORMATTER);
	 * 
	 * LocalDateTime fromDate1 = LocalDateTime.of(fromDate,
	 * LocalDateTime.now().toLocalTime()); LocalDateTime toDate1 =
	 * LocalDateTime.of(toDate, LocalDateTime.now().toLocalTime());
	 * 
	 * DispatcherFormSubmissionDTO dispatcherFormSubmissionDTO=new
	 * DispatcherFormSubmissionDTO();
	 * 
	 * List<RegistrationDetailsDTO> reg = registrationDetailDAO.findAll();
	 * List<DispatcherFormSubmissionDTO> listdispatcher=new
	 * ArrayList<DispatcherFormSubmissionDTO>();
	 * 
	 * reg.forEach(dto -> { if (dto.getIsCardDispatched() == true) {
	 * 
	 * listdispatcher.add(dispatcherSubmissionRepo.findByPostedDateBetween(
	 * fromDate1, toDate1));
	 * 
	 * } });
	 * 
	 * return listdispatcher;
	 * 
	 * }
	 */
	/*
	 * public List<DispatcherSubmissionDTO>
	 * getDispatcherDetailsByDate(LocalDateTimeConverterDTO dto) {
	 * 
	 * List<DispatcherSubmissionDTO> dispatcherFormSubmissionDTO = new
	 * ArrayList<DispatcherSubmissionDTO>();
	 * 
	 * dispatcherFormSubmissionDTO
	 * .addAll(dispatcherSubmissionRepo.findByPostedDateBetween(dto.getFromDate(),
	 * dto.getToDate()));
	 * 
	 * return dispatcherFormSubmissionDTO;
	 * 
	 * }
	 */

	@Autowired
	private OfficeMapper officeMapper;
	
	@Override
	public List<OfficeVO> getMviForDist(String officeCode) {
		// List<UserDTO> userList = new ArrayList<>();
		Optional<OfficeDTO> officeOptional = officeDAO.findByOfficeCode(officeCode);
		List<OfficeDTO> officeList = officeDAO.findByDistrictAndTypeIn(officeOptional.get().getDistrict(),
				Arrays.asList(OfficeType.MVI.getCode()));
		if (CollectionUtils.isEmpty(officeList)) {
			  throw new BadRequestException("MVIOfficeDetails not found");
		}
			  
		return officeMapper.convertEntity(officeList);

	}
}
