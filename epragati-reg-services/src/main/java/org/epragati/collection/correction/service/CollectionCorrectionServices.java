package org.epragati.collection.correction.service;

import java.util.List;
import java.util.Map;

import org.epragati.common.vo.CorrectionVO;
import org.epragati.rta.vo.CorrectionsVO;
/**
 * 
 * @author krishnarjun.pampana
 *
 */
public interface CollectionCorrectionServices {
    
	/**
	 * 
	 * @param collectionCorrectionVO
	 * @return 
	 */
	Map<String, List<CorrectionVO>> getRegistrationDetails(CorrectionsVO collectionCorrectionVO);

}
