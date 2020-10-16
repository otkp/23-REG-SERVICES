package org.epragati.master.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.epragati.master.dao.RegServiceDAO;
import org.epragati.master.dao.RegServiceLogDAO;
import org.epragati.master.dao.StagingRegistrationDetailsDAO;
import org.epragati.master.dao.StagingRegistrationDetailsHistoryLogDAO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.dto.StagingRegistrationDetailsHistoryLogDto;
import org.epragati.master.service.LogMovingService;
import org.epragati.payment.dto.PaymentTransactionDTO;
import org.epragati.payment.dto.PaymentTransactionLogDTO;
import org.epragati.payments.dao.PaymentTransactionDAO;
import org.epragati.payments.dao.PaymentTransactionLogDAO;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.dto.RegServiceLogsDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LogMovingServiceImpl implements LogMovingService{

	private static final Logger logger = LoggerFactory.getLogger(LogMovingServiceImpl.class);
	
	@Autowired
	private StagingRegistrationDetailsDAO stagingRegistrationDetails;
	@Autowired
	private StagingRegistrationDetailsHistoryLogDAO stagingHistoryLogDAO;
	@Autowired
	private PaymentTransactionDAO paymentTransactionDAO;
	@Autowired
	private PaymentTransactionLogDAO paymentTransactionLogDAO;
	@Autowired
	private RegServiceLogDAO regServiceLogDAO;
	@Autowired
	private RegServiceDAO regServiceDAO;
	
	@Override
	public void moveStagingToLog(String applicationNo) {
		
		 Optional<StagingRegistrationDetailsDTO> stagingOptional = stagingRegistrationDetails.findByApplicationNo(applicationNo);
		 if(stagingOptional.isPresent()) {
			 StagingRegistrationDetailsHistoryLogDto dto = new StagingRegistrationDetailsHistoryLogDto();
			 dto.setStagingDetails(stagingOptional.get());
			 dto.setModifiedDate(LocalDateTime.now());
			 stagingHistoryLogDAO.save(dto);
		 }
		
	}

	@Override
	public void movePaymnetsToLog(String applicationNo) {
		
		List<PaymentTransactionDTO> listOfPayments = paymentTransactionDAO.findByApplicationFormRefNum(applicationNo);
		if(listOfPayments != null && !listOfPayments.isEmpty()) {
			listOfPayments.sort((p1,p2)->p2.getRequest().getRequestTime().compareTo(p1.getRequest().getRequestTime()));
			PaymentTransactionLogDTO dto = new PaymentTransactionLogDTO();
			dto.setPaymentTransactionDTO(listOfPayments.stream().findFirst().get());
			 dto.setModifiedDate(LocalDateTime.now());
			paymentTransactionLogDAO.save(dto);
		}
	}
	
	@Override
	public void moveRegServiceToLog(String applicationNo) {
		
		 Optional<RegServiceDTO> regServiceOptional = regServiceDAO.findByApplicationNo(applicationNo);
		 if(regServiceOptional.isPresent()) {
			 RegServiceLogsDTO dto = new RegServiceLogsDTO();
			 dto.setModifiedDate(LocalDateTime.now());
			 dto.setRgServDetails(regServiceOptional.get());
			 regServiceLogDAO.save(dto);
		 }
		
	}
}
