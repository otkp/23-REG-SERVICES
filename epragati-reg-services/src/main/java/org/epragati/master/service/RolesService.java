package org.epragati.master.service;

import java.util.List;

import org.epragati.master.vo.RolesVO;
/**
 * 
 * @author saikiran.kola
 *
 */
public interface RolesService {
	
	/**
	 * get all Roles Data 
	 * @return
	 */

	public List<RolesVO> getRolesDetails();
	
}
