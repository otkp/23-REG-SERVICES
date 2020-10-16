package org.epragati.auditService;
import java.time.LocalDateTime;
import java.util.List;

import org.epragati.constants.Schedulers;

public interface AuditLogsService {
	
	void saveScedhuleLogs(Schedulers schedulersType,LocalDateTime startTime,LocalDateTime endTime,
				Boolean isExecuteSucess,String error,List<String> internalErrors);
	
	
	void saveErrorTrackLog(String moduleCode, String prNo, String applicationNo,String error, String context);

}
