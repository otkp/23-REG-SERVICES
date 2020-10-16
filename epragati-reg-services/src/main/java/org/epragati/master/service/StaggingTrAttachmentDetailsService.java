package org.epragati.master.service;

import java.util.List;
import java.util.Optional;

import org.epragati.master.vo.StaggingTrAttachmentDetailsVO;
import org.epragati.master.vo.EnclosuresVO;

/**
 * 
 * @author saroj.sahoo
 *
 */
public interface StaggingTrAttachmentDetailsService {
	
	/**
	 * Save StaggingTrAttachmentDetails
	 * @return 
	 */
	public Integer save(Optional<StaggingTrAttachmentDetailsVO> staggingTrAttachmentDetailsVO);
	
	/**
	 * get all StaggingTrAttachmentDetails details
	 * 
	 * @return
	 */
	public List<StaggingTrAttachmentDetailsVO> findAllUserEnclosure();
	
	/**
	 * Find UserEnclosure by enId
	 * @param enId
	 * @return 
	 */
	public Optional<EnclosuresVO> findUserEnclosureBasedOnenId(Integer enId);
	

}
