package org.epragati.rta.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.epragati.common.dao.ActionDetailsDAO;
import org.epragati.common.dao.FlowDAO;
import org.epragati.common.dto.FlowDTO;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.FcDetailsDAO;
import org.epragati.master.dao.FinanceDetailsDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.RegistrationDetailLogDAO;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dto.ActionDetailsDTO;
import org.epragati.master.dto.FinanceDetailsDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.RegistrationDetailsLogDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.org.vahan.port.service.VahanSync;
import org.epragati.vahan.sync.dao.VahanSyncDAO;
import org.epragati.vahan.sync.mapper.VahanSyncMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DTOUtilService {

	private static final Logger logger = LoggerFactory.getLogger(DTOUtilService.class);

	@Autowired
	private ActionDetailsDAO actionDetailsDAO;

	@Autowired
	private RegistrationDetailDAO registrationDetailsDao;

	@Autowired
	private FinanceDetailsDAO financeDetailsDAO;

	@Autowired
	private FlowDAO flowDAO;

	@Autowired
	private UserDAO userDao;
	
	@Autowired
	private FcDetailsDAO fcDetailsDao;
	
	@Autowired
	private RegistrationDetailLogDAO registrationDetailLogDAO;
	
	@Autowired
	private VahanSync vahanSync;
	
	public void moveActionDetails(ActionDetailsDTO actionDetails, String applicatioNo) {
		if (actionDetails != null) {
			logger.debug("applicationNo [{}] / Action Details Success ", applicatioNo);
			actionDetailsDAO.save(actionDetails);
		} else {
			logger.debug("applicationNo [{}] / Action Details Failed ", applicatioNo);
		}
	}

	public void moveActionDetails(List<ActionDetailsDTO> actionDetails, String applicatioNo) {
		if (actionDetails != null) {
			logger.debug("applicationNo [{}] / Action Details List Success ", applicatioNo);
			actionDetailsDAO.save(actionDetails);
		} else {
			logger.debug("applicationNo [{}] / Action Details List Failed ", applicatioNo);
		}
	}

	public void saveFinanceDetails(FinanceDetailsDTO financeDetailsDTO, String applicatioNo) {

		if (financeDetailsDTO != null) {
			logger.debug("applicationNo [{}] / Finance Details Success ", applicatioNo);
			financeDetailsDAO.save(financeDetailsDTO);
		} else {
			logger.debug("applicationNo [{}] / Finance Details Failed ",  applicatioNo);
		}

	}

	public void moveFlowDetails(FlowDTO flowDto, String applicatioNo) {
		if (flowDto != null) {
			logger.debug("applicationNo [{}] / FLow Details Success ", applicatioNo);
			flowDAO.save(flowDto);
		} else {
			logger.debug("applicationNo [{}] / FLow Details Failed ", applicatioNo);
		}
	}

	public void moveFlowDetails(List<FlowDTO> flowDto, String applicatioNo) {
		if (flowDto != null) {
			logger.debug("applicationNo [{}] / FLow Details List Success ");
			flowDAO.save(flowDto.stream().findFirst().get());
		} else {
			logger.debug("applicationNo [{}] / FLow Details List Failed ", applicatioNo);
		}
	}

	public void moveStagingDetails(StagingRegistrationDetailsDTO stagingDetails, String applicatioNo) {
		if (stagingDetails != null) {
			logger.debug("applicationNo [{}] / Registration Details Success ", applicatioNo);
			registrationDetailsDao.save(stagingDetails);
			vahanSync.commonVahansync(stagingDetails);
			
		} else
			logger.debug("applicationNo [{}] / Registration Details Failed ", applicatioNo);
	}
	
	public void moveToRegistrationLogsWithChecks(RegistrationDetailsDTO registrationDetailsDTO) {
/*		Optional<RegistrationDetailsLogDTO> registrationDetailsLogOptinal=
				registrationDetailLogDAO.findTopByRegiDetailsApplicationNoOrderByLogCreatedDateTimeDesc(registrationDetailsDTO.getApplicationNo());
		if(registrationDetailsLogOptinal.isPresent() 
				&& registrationDetailsLogOptinal.get().getRegiDetails()!=null
				&& registrationDetailsLogOptinal.get().getRegiDetails().getServiceIds() !=null && registrationDetailsLogOptinal.get().getRegiDetails().getServiceIds().contains(ServiceEnum.NEWREG.getId())) {
				return;
		}*/
		moveToRegistrationLogs(registrationDetailsDTO);
	}
	
	public void moveToRegistrationLogs(RegistrationDetailsDTO registrationDetailsDTO) {
		LocalDateTime now = LocalDateTime.now();
		RegistrationDetailsLogDTO registrationDetailsLog = new RegistrationDetailsLogDTO();
		registrationDetailsLog.setRegiDetails(registrationDetailsDTO);
		registrationDetailsLog.setLogCreatedDateStr(now.toString());
		registrationDetailsLog.setLogCreatedDateTime(now);
		registrationDetailsDTO.setlUpdate(now);
		registrationDetailLogDAO.save(registrationDetailsLog);
	}
	
	public String getRole(String userId, String inputRole) {
		Optional<UserDTO> userDdetails =  userDao.findByUserId(userId);
		if(!userDdetails.isPresent()) {
			logger.error("User  is not found.");
			throw new BadRequestException("User  is not found.");
		}
		List<String> list = new ArrayList<>();
		list.add(userDdetails.get().getPrimaryRole().getName());
		if(!(!userDdetails.get().getAdditionalRoles().isEmpty() && userDdetails.get().getAdditionalRoles().stream().anyMatch(name->name.getName().equalsIgnoreCase(inputRole)) 
				|| userDdetails.get().getPrimaryRole().getName().equalsIgnoreCase(inputRole))) {
			logger.error("Invalid user.");
			throw new BadRequestException("Invalid user.");
		}
		
		return inputRole;
	}
}
