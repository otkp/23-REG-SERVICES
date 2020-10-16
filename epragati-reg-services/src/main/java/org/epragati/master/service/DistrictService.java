package org.epragati.master.service;

import java.util.List;

import org.epragati.master.vo.DistrictVO;

/**
 * @author saroj.sahoo
 *
 */
public interface DistrictService {

	/**
	 * 
	 * @return findAllDistricts
	 */
	List<DistrictVO> findAll();

	/**
	 * 
	 * @param stateId
	 * @return districts By StateId
	 */
	 List<DistrictVO> findBySid(String stateId);

	List<DistrictVO> findDistrictsByUser(String userId);

	List<DistrictVO> getDistrictsForBiLateralTax();
	
	String findDistNameFromInput(List<DistrictVO> districtVOList,Integer distId);

}
