package org.epragati.master.service;

import java.util.List;

import org.epragati.master.vo.VillagePostalOfficeVO;
import org.epragati.master.vo.VillageVO;

/**
 * 
 * @author saroj.sahoo
 *
 */

public interface VillageService {

	/**
	 * 
	 * @return findAllVillages
	 */
	List<VillageVO> findAll();
	
	/**
	 * 
	 * @param mandalId
	 * @return VillagesByMandal
	 */
	VillagePostalOfficeVO findByMid(Integer mandalId,Integer districtCode);

	

	List<VillageVO> findByMandalId(Integer mandalId);
}
