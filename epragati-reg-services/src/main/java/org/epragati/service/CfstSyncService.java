package org.epragati.service;

import java.util.List;
import java.util.Map;

import org.epragati.master.vo.CfstSyncRegstrationVO;

/**
 * 
 * @author krishnarjun.pampana
 *
 */

public interface CfstSyncService {
	
	/**
	 * 
	 * @param registrationDetailsVOList
	 * @return
	 */
	Map<String, String> saveRegDetails(List<CfstSyncRegstrationVO> registrationDetailsVOList);

}
