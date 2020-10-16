package org.epragati.hsrp.servic;

import org.epragati.common.dto.HsrpDetailDTO;
import org.epragati.hsrp.vo.DataVO;

/**
 * 
 * @author praveen.kuppannagari
 *
 */

public interface IntegrationService {
	/**
	 * used to persist the TR Data in Hsrp_model
	 */
	void createHSRPTRData(DataVO dataVO);
	
	void updatePRData(DataVO dataVO);
	
	HsrpDetailDTO fetchHSRPData(String input, String catagory);
}
