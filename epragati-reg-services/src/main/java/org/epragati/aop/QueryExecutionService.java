package org.epragati.aop;

import java.time.LocalDateTime;

import org.epragati.jwt.JwtUser;
import org.epragati.master.dao.MasterCovDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.RegServiceReportDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.TrGeneratedReportDAO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.dto.TrGeneratedReportDTO;
import org.epragati.master.mappers.RegistrationDetailsMapper;
import org.epragati.regservice.dto.ActionDetails;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.dto.RegServiceReportDTO;
import org.epragati.regservice.mapper.RegDetailsReportMapper;
import org.epragati.regservice.mapper.RegServiceReportMapper;
import org.epragati.reports.dao.RegDetailsReportDAO;
import org.epragati.reports.dao.RegistrationCountReportDAO;
import org.epragati.reports.dto.RegDetailsReportDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * 
 * @author krishnarjun.pampana
 *
 */

@Service
public class QueryExecutionService {

	@Autowired
	private RegServiceReportMapper regServiceReportMapper;

	@Autowired
	private RegServiceReportDAO regServiceReportDAO;

	@Autowired
	private RegDetailsReportDAO regDetailsReportDAO;

	@Autowired
	private RegDetailsReportMapper regDetailsReportMapper;

	@Autowired
	private RegistrationCountReportDAO registrationCountReportDAO;

	@Autowired
	private OfficeDAO officeDAO;

	@Autowired
	private MasterCovDAO masterCovDAO;

	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;

	@Autowired
	private RegistrationDetailsMapper registrationDetailsMapper;

	@Autowired
	private TrGeneratedReportDAO trGeneratedReportDAO;

	/*
	 * private static Logger log =
	 * LoggerFactory.getLogger(QueryExecutionService.class);
	 * 
	 * @Autowired private QueryExceutorDAO queryExceutorDAO;
	 * 
	 * @Async("threadPoolTaskExecutor") public void
	 * saveQueryExecutionDetails(QueryExecutionDetailsDTO exceutionTime) throws
	 * InterruptedException { log.info(Thread.currentThread().getName());
	 * queryExceutorDAO.save(exceutionTime); return ; }
	 */
	@Async("threadPoolTaskExecutor")
	public void saveServicesReport(RegServiceDTO regServiceDTO, ActionDetails actionDetail) {
		try {
			RegServiceReportDTO dto = regServiceReportMapper.converDto(regServiceDTO);
			dto.setActionRoleName(actionDetail.getRole());
			dto.setActionStatus(actionDetail.getStatus());
			dto.setActionTime(actionDetail.getlUpdate());
			dto.setActionUserName(actionDetail.getUserId());
			regServiceReportDAO.save(dto);
		} catch (Exception e) {

		}
		return;
	}

	@Async("threadPoolTaskExecutor")
	public void saveRegDetailsReport(RegistrationDetailsDTO regDto) {
		try {
			RegDetailsReportDTO dto = regDetailsReportMapper.convertEntity(regDto);
			regDetailsReportDAO.save(dto);
		} catch (Exception e) {

		}
		return;
	}

	@Async("threadPoolTaskExecutor")
	public void saveRegDetailsEodReport(StagingRegistrationDetailsDTO stagingDTO, JwtUser jwtUser, String action,
			LocalDateTime currentTime, String role) {
		try {
			RegServiceReportDTO dto = regServiceReportMapper.convertRegToEodReport(stagingDTO);
			dto.setActionRoleName(role);
			dto.setActionStatus(action);
			dto.setActionTime(currentTime);
			dto.setActionUserName(jwtUser.getId());
			dto.setModule("REG");
			regServiceReportDAO.save(dto);

		} catch (Exception e) {
		}
		return;
	}

	@Async("threadPoolTaskExecutor")
	public void saveTrReportSave(StagingRegistrationDetailsDTO staging) {
		try {
			TrGeneratedReportDTO trgenratedReportDTO = registrationDetailsMapper.convertTrgenratedReportDTO(staging);
			trGeneratedReportDAO.save(trgenratedReportDTO);
		} catch (Exception e) {

		}
		return;
	}

}
