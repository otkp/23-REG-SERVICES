package org.epragati.master.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.epragati.master.dto.SiteDownDTO;
import org.epragati.master.vo.StatusVO;
/**
 * 
 * @author saikiran.kola
 *
 */

public interface StatusService  {
 /**
  * get all Status Details
  * @return
  */
	
	public List<StatusVO> getStatusDetails();
	/**
	 * find Status data based on Id and Status 
	 * @param sId
	 * @param Status
	 * @return
	 */
	
	public Optional<StatusVO> getStatusBysIdAndStatus(Integer sId,Integer Status);
	
	/**
	 * find Status data based on Created Date
	 * @param createddate
	 * @return
	 */
	
	public Optional<StatusVO> findStatusDetailsByCreateDate(Timestamp createddate);
	
	/**
	 * 
	 * @param currentDateTime
	 * @return
	 */
	public Optional<SiteDownDTO> fetchSiteDownInfo(LocalDateTime currentDateTime);
}
