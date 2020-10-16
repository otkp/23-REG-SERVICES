package org.epragati.master.service;

import java.util.List;

import org.epragati.master.vo.UserRegistrationVO;

/**
 * 
 * @author saroj.sahoo
 *
 */
public interface UserRegistrationService {

	/**
	 * Save UserRegistration
	 * @return
	 */
	public void save();

	/**
	 * Find all UserRegistration
	 * @return
	 */
	public List<UserRegistrationVO> findAllUserRegistration();

	/**
	 * Find UserRegistration Based On mandal
	 * @param mandal
	 * @return
	 */
	public UserRegistrationVO findUserRegistrationByMandal(Integer mandal);

}
