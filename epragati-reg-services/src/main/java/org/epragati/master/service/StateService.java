package org.epragati.master.service;

import java.util.List;

import org.epragati.master.vo.StateVO;

/**
 * @author sairam.cheruku
 *
 */
public interface StateService {

	/**
	 * 
	 * @return AllStates
	 */
	List<StateVO> findAll();
	
	/**
	 * 
	 * @param countryId
	 * @return StatesList By county
	 */
	List<StateVO> findByCid(String countryId);

	List<StateVO> getStatesForBiLateralTax();

}
