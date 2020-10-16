package org.epragati.master.service;

import java.util.List;
import java.util.Optional;

import org.epragati.master.dto.UserEnclousersActionsDTO;
import org.epragati.master.vo.UserEnclousersActionsVO;

/**
 * 
 * @author saroj.sahoo
 *
 */
public interface UserEnclousersActionsService {

	/**
	 * Save UserEnclousersActions
	 * @return
	 */
	public Integer save(Optional<UserEnclousersActionsVO> userEnclousersActionsVO);

	/**
	 * Find all UserEnclousersActions
	 * @return
	 */
	public List<UserEnclousersActionsVO> findAllUserEnclousersActions();

	/**
	 * Find UserEnclousersActions Based On role
	 * @param role
	 * @return
	 */
	public Optional<UserEnclousersActionsVO> findUserEnclousersActionsByRole(Integer role);

	

	

}
