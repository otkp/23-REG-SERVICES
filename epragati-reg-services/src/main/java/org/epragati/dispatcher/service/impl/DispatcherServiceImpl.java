package org.epragati.dispatcher.service.impl;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.dom4j.DocumentException;
import org.epragati.constants.DispatchEnum;
import org.epragati.dispatcher.dao.DispatcherSubmissionDAORepo;
import org.epragati.dispatcher.dto.DispatcherSubmissionDTO;
import org.epragati.dispatcher.dto.RecordsDTO;
import org.epragati.dispatcher.mapper.DispatcherMapper;
import org.epragati.dispatcher.service.DispatcherService;
import org.epragati.dispatcher.vo.InputVO;
import org.epragati.dispatcher.vo.RecordsVO;
import org.epragati.dispatcher.vo.UIFormatVO;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.CardDispatchDetailsDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.RegistrationCardPrintDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dto.CardDispatchDetailsDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.util.PermitsEnum;
import org.epragati.util.Status;
import org.epragati.util.StatusRegistration;
import org.epragati.util.payment.ServiceEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.google.zxing.WriterException;

@Service
public class DispatcherServiceImpl implements DispatcherService {

	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;

	@Autowired
	private DispatcherMapper mapper;
	
	@Autowired
	CardDispatchDetailsDAO carddispatchDetailsDAO;

	@Autowired
	private DispatcherSubmissionDAORepo dispatcherSubmissionRepo;
	
	@Autowired
	private OfficeDAO officeDAO;

	private static final Logger logger = LoggerFactory.getLogger(DispatcherServiceImpl.class);

	/**
	 * 
	 * Validation user value is empty or not
	 * 
	 * @param inputVO
	 */
    
	private void doValidations(InputVO inputVO, String token) {

		if (inputVO.getReqType().equals(DispatchEnum.FETCH_BY_PRNO.getReqType())) {
			if (StringUtils.isBlank(inputVO.getPrNo())) {
				throw new BadRequestException("Enter PR Number");
			}
		} else if (inputVO.getReqType().equals(DispatchEnum.FETCH_BY_DATE.getReqType())) {
			if (inputVO.getFromDate() != null && StringUtils.isBlank(inputVO.getFromDate())
					|| inputVO.getToDate() != null && StringUtils.isBlank(inputVO.getToDate())) {
				throw new BadRequestException("Invalid Dates");
			}

		} else if (inputVO.getReqType().equals(DispatchEnum.FETCH_ALL_DETAILS.getReqType())) {

		} else {
			throw new BadRequestException("Invalid Request");
		}
	}

	/**
	 * 
	 * Dispatcher Fetching data as per required type
	 * 
	 */

	@Override
	public UIFormatVO fetchRecords(InputVO inputVO, String token, String officeCode) {

		List<RecordsVO> recordsVOs = new ArrayList<>();
		long noOfRecords=0;
		UIFormatVO uiFormatVO=new UIFormatVO();
		doValidations(inputVO, token);

		List<RegistrationDetailsDTO> listOfRecords = new ArrayList<>();

		if (inputVO.getReqType().equals(DispatchEnum.FETCH_BY_PRNO.getReqType())) {

			logger.info(inputVO.getPrNo() + " office code" + officeCode);
			Optional<RegistrationDetailsDTO> det = registrationDetailDAO
					.findByPrNoAndIsCardPrintedAndIsCardDispatchedAndOfficeDetailsOfficeCode(inputVO.getPrNo(), true,
							false, officeCode);
			if (det.isPresent()) {
				RegistrationDetailsDTO registrationDetailsDTO = det.get();
				RecordsDTO recordsDTO = mapper.recordsMapping(registrationDetailsDTO);
				RecordsVO recordsVO = new RecordsVO();
				BeanUtils.copyProperties(recordsDTO, recordsVO);
				recordsVOs.add(recordsVO);

			} else {
				logger.info("Records Not Found for OfficeCode" + officeCode);
				//logger.error("Records Not Found");
				throw new BadRequestException("Records Not Found for the officeCode: "+officeCode);
			}

		} else if (inputVO.getReqType().equals(DispatchEnum.FETCH_BY_DATE.getReqType())) {

			Map<String, LocalDateTime> dates = mapper.stringToDateConvertor(inputVO.getFromDate(), inputVO.getToDate());

			LocalDateTime fromDate = dates.get("from");
			LocalDateTime toDate = dates.get("to");

			logger.info(fromDate.toString());
			logger.info(toDate.toString());
   
			/*
			 * Pageable pageable = new PageRequest(Integer.parseInt(inputVO.getPageNo()),
			 * Integer.parseInt(inputVO.getSize()), Sort.Direction.ASC, "prGeneratedDate");
			 */
			noOfRecords = registrationDetailDAO
					.countByCardPrintedDateBetweenAndIsCardDispatchedAndIsCardPrintedAndOfficeDetailsOfficeCode(
							fromDate, toDate, false, true,officeCode);
			
			if (noOfRecords != 0) {
				listOfRecords = registrationDetailDAO
						.findByCardPrintedDateBetweenAndIsCardDispatchedAndIsCardPrintedAndOfficeDetailsOfficeCode(
								fromDate, toDate, false, true,officeCode);
				/*if(!listOfRecords.isEmpty()){
					List<String> listOfPrs=listOfRecords.stream().map(p->p.getPrNo()).collect(Collectors.toList());
					
					listOfCardDetails=carddispatchDetailsDAO.findByPrNoIn(listOfPrs);
				}*/
			}
			if (listOfRecords.isEmpty()) {
				//logger.error("Records Not Found");
				throw new BadRequestException("Records Not Found");
			}
			/*permitDto = permitDto.stream()
					.filter(id -> id.getPermitClass().getDescription()
							.equals(PermitsEnum.PermitType.TEMPORARY.getDescription()))
					.collect(Collectors.toList());*/
			
			listOfRecords.stream().forEach(dto -> {

				RecordsDTO recordsDTO = null;
				logger.info(dto.toString());
				if (dto != null)
					recordsDTO = mapper.recordsMapping(dto);
				RecordsVO recordsVO = new RecordsVO();

				if (recordsDTO != null)
					BeanUtils.copyProperties(recordsDTO, recordsVO);

				recordsVOs.add(recordsVO);

			});
			logger.info(listOfRecords.toString());

		} else if (inputVO.getReqType().equals(DispatchEnum.FETCH_ALL_DETAILS.getReqType())) {

			logger.debug("In ALL");
			Pageable pageable = new PageRequest(Integer.parseInt(inputVO.getPageNo()),
					Integer.parseInt(inputVO.getSize()), Sort.Direction.ASC, "prNo");
			
			noOfRecords = registrationDetailDAO.countByIsCardDispatchedAndIsCardPrintedAndOfficeDetailsOfficeCodeAndApplicationStatus(
					false, true, officeCode,StatusRegistration.PRGENERATED.getDescription());
			
			listOfRecords = registrationDetailDAO.findByIsCardDispatchedAndIsCardPrintedAndOfficeDetailsOfficeCodeAndApplicationStatus(
					false, true, officeCode, pageable,StatusRegistration.PRGENERATED.getDescription());
			
			listOfRecords.stream()
			.filter(reg->reg.getApplicationStatus().equalsIgnoreCase(StatusRegistration.PRGENERATED.getDescription()))
			.collect(Collectors.toList());

			if (listOfRecords.isEmpty()) {
				throw new BadRequestException("Records Not Found");
			}
		
			listOfRecords.stream().forEach(dto -> {

				RecordsDTO recordsDTO = null;
				if (dto != null)
					recordsDTO = mapper.recordsMapping(dto);
				RecordsVO recordsVO = new RecordsVO();
				if (recordsDTO != null) {
					BeanUtils.copyProperties(recordsDTO, recordsVO);
					recordsVOs.add(recordsVO);
				}
				
			});
		}
		uiFormatVO.setRecordsVOs(recordsVOs);
		uiFormatVO.setNoOfRecords(noOfRecords);
		
		return uiFormatVO;
	}

	private String DATEFORMAT = "dd-MM-yyyy";
	DateTimeFormatter dateFormatForRegDate = DateTimeFormatter.ofPattern(DATEFORMAT);

	@Override
	public Map<String, Object> getRegistrationDetails(String prNo) {
		DispatcherSubmissionDTO recordsDTO = null;
		Map<String, Object> parameters = new HashMap<>();

		List<DispatcherSubmissionDTO> resultData = dispatcherSubmissionRepo.findByPrNo(prNo);
		if (!(resultData.isEmpty())) {
			recordsDTO = resultData.get(0);
		} else {
			logger.error("No record found");
			throw new BadRequestException("No record found");
		}
		parameters.put("applicationno",
				(recordsDTO.getApplicationNo() == null) ? StringUtils.EMPTY : recordsDTO.getApplicationNo());
		parameters.put("prno", (recordsDTO.getPrNo() == null) ? StringUtils.EMPTY : recordsDTO.getPrNo());
		parameters.put("office", (recordsDTO.getOfficeCode() == null) ? StringUtils.EMPTY : recordsDTO.getOfficeCode());

		parameters.put("name", (recordsDTO.getName() == null) ? StringUtils.EMPTY : recordsDTO.getName());
		parameters.put("emsno", (recordsDTO.getEmsNumber() == null) ? StringUtils.EMPTY : recordsDTO.getEmsNumber());
		parameters.put("posteddate", (recordsDTO.getPostedDate() == null) ? StringUtils.EMPTY
				: recordsDTO.getPostedDate().format(dateFormatForRegDate));
		parameters.put("office",
				(recordsDTO.getDispatchedBy() == null) ? StringUtils.EMPTY : recordsDTO.getDispatchedBy());
		parameters.put("remarks", (recordsDTO.getRemarks() == null) ? StringUtils.EMPTY : recordsDTO.getRemarks());
		return parameters;
	}

	@Override
	public String getFile(String file) {
		return "jasper\\" + file;
	}

	@Override
	public String sendPDF(String qrData) throws FileNotFoundException, DocumentException, WriterException, IOException {

		return null;
	}

	@Override
	public String sendPDFURL(String qrData)
			throws FileNotFoundException, DocumentException, WriterException, IOException {

		return null;
	}
	

}
