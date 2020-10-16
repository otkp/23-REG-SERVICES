package org.epragati.common.service;

import java.time.LocalDate;
/**
 * 
 * @author krishnarjun.pampana
 *
 */
public interface NumberGenerationService {
	/**
	 * 
	 * @param trDistrictId
	 * @return
	 */
	String getTrGeneratedSeries(Integer trDistrictId);
	
	void prGenerationScheduler();
	
	void validateRequest(String authToken, String ip);
	
	void caluclateRegistrationCount(LocalDate countDatefromUI);	
	
}
