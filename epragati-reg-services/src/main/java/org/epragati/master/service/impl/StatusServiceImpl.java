package org.epragati.master.service.impl;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.Optional;

import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.SiteDownDAO;
import org.epragati.master.dao.StatusDAO;
import org.epragati.master.dto.SiteDownDTO;
import org.epragati.master.dto.StatusDTO;
import org.epragati.master.mappers.StatusMapper;
import org.epragati.master.service.StatusService;
import org.epragati.master.vo.StatusVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StatusServiceImpl implements StatusService {
	private static final Logger logger = LoggerFactory.getLogger(DealerMakerServiceImpl.class);
	@Autowired
	StatusDAO statusDAO;

	@Autowired
	StatusMapper statusMapper;
	
	@Autowired
	private SiteDownDAO siteDownDAO;

	@Override
	public List<StatusVO> getStatusDetails() {

		/**
		 * get all MasterStatus details
		 */

		List<StatusDTO> StatusList = statusDAO.findAll();
		List<StatusVO> voList = statusMapper.convertEntity(StatusList);
		// TODO Auto-generated method stub
		return voList;
	}

	/**
	 * get all Status details based on Id and Status
	 */
	@Override
	public Optional<StatusVO> getStatusBysIdAndStatus(Integer sId, Integer status) {
		// TODO Auto-generated method stub
		Optional<StatusDTO> statusOptional = statusDAO.findBysIdAndStatus(sId, status);
		if (statusOptional.isPresent()) {
			logger.debug("master status data based on sid and status is available");
			StatusVO statusVO = statusMapper.convertEntity(statusOptional.get());
			return Optional.of(statusVO);
		}
		logger.error("no data is available for MasterStatus based on id and status");
		throw new BadRequestException("no data is available for MasterStatus based on id and status");
	}

	/**
	 * get Status details Based on CreatedDate
	 */
	@Override
	public Optional<StatusVO> findStatusDetailsByCreateDate(Timestamp createddate) {
		// TODO Auto-generated method stub
		Optional<StatusDTO> statusOptional = statusDAO.findBycreatedDate(createddate);
		if (statusOptional.isPresent()) {
			logger.debug("Master status data basaed on created date is available");
			StatusVO stausVO = statusMapper.convertEntity(statusOptional.get());
			return Optional.of(stausVO);
		}
		logger.error("no data is available for Master Status based on created date");
		throw new BadRequestException("no data is available for Master Status based on created date");
	}

	private boolean isWithinRange(LocalDateTime currentDateTime, LocalDateTime effectiveFrom, LocalDateTime toDate) {
		logger.debug("Inside Date Range Check");
		return !(currentDateTime.isBefore(effectiveFrom) || currentDateTime.isAfter(toDate));
	}
	
	@Override
	public Optional<SiteDownDTO> fetchSiteDownInfo(LocalDateTime currentDateTime) {
		Optional<SiteDownDTO> siteDownDTOOptional = siteDownDAO.findByStatusTrue();
		if(siteDownDTOOptional.isPresent()){
			boolean status = isWithinRange(currentDateTime, siteDownDTOOptional.get().getEffectiveFrom(), siteDownDTOOptional.get().getToDate());
			if(status) {
				logger.info("Status coming from Date Range Check is {[]}", status);
				if(siteDownDTOOptional.get().getToDate().isBefore(currentDateTime)) {
					logger.info("Current Date and toDate Range Check is {[]}, {[]}", currentDateTime, siteDownDTOOptional.get().getToDate());
					SiteDownDTO siteDownDTO = new SiteDownDTO();
					siteDownDTO.setStatus(Boolean.FALSE);
					siteDownDAO.save(siteDownDTO);
				}
				return siteDownDTOOptional;
			} else {
				return Optional.empty();
			}
		}
		logger.error("No Messages to Display");
		throw new BadRequestException("No Messages to Display");
		
	}
	
}