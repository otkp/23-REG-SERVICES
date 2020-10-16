package org.epragati.master.service;

import java.util.List;

import org.epragati.master.vo.BloodGroupVO;

/**
 * @author saroj.sahoo
 *
 */
public interface BloodGroupService {

	/**
	 * 
	 * @return findAll BloodGroups
	 */
	List<BloodGroupVO> findAll();
 

	
}
