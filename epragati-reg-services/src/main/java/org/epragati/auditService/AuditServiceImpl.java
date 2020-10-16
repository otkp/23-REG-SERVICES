package org.epragati.auditService;

import java.time.LocalDateTime;
import java.util.List;

import org.epragati.common.dao.ErrorTrackLogDAO;
import org.epragati.common.dao.SchedulerLogsDAO;
import org.epragati.common.dto.ErrorTrackLogDTO;
import org.epragati.common.dto.SchedulerLogsDTO;
import org.epragati.constants.Schedulers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuditServiceImpl implements AuditLogsService{


@Autowired
private SchedulerLogsDAO schedulerLogsDAO;

@Autowired
private ErrorTrackLogDAO errorTrackLogDAO;

	@Override
	public void saveScedhuleLogs(Schedulers schedulersType, LocalDateTime startTime, LocalDateTime endTime,
			Boolean isExecuteSucess, String error, List<String> internalErrors) {
			
			SchedulerLogsDTO schedulerLogsDTO= new SchedulerLogsDTO();
			schedulerLogsDTO.setName(schedulersType);
			schedulerLogsDTO.setStartTime(startTime);
			schedulerLogsDTO.setEndTime(endTime);
			schedulerLogsDTO.setIsExecuteSucess(isExecuteSucess);
			schedulerLogsDTO.setErrorMessage(error);
			schedulerLogsDTO.setInternalErrors(internalErrors);		
			schedulerLogsDAO.save(schedulerLogsDTO);

	}
	
	@Override
		public void saveErrorTrackLog(String moduleCode, String prNo, String applicationNo, String error, String context) {
			
			ErrorTrackLogDTO errorTrackLog= new ErrorTrackLogDTO();
			
			errorTrackLog.setApplicationNo(applicationNo);
			errorTrackLog.setCreatedDate(LocalDateTime.now());
			errorTrackLog.setError(error);
			errorTrackLog.setPrNo(prNo);
			errorTrackLog.setContext(context);
			errorTrackLog.setModuleCode(moduleCode);
			
			errorTrackLogDAO.save(errorTrackLog);
			
		}
}
