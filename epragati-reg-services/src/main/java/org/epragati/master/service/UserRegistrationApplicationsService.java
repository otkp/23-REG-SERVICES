package org.epragati.master.service;

import java.util.List;

import org.epragati.master.vo.UserRegistrationApplicationsVO;

/**
 * 
 * @author saroj.sahoo
 *
 */
public interface UserRegistrationApplicationsService {

	/**
	 * Save UserRegistrationApplications
	 * @return
	 */
	public void save();

	/**
	 * Find all UserRegistrationApplications
	 * @return
	 */
	public List<UserRegistrationApplicationsVO> findAllUserRegistrationApplications();

	/**
	 * Find UserEnclousersActions Based On applicationno
	 * @param applicationno
	 * @return
	 */
	public UserRegistrationApplicationsVO findUserRegistrationApplicationsByApplicationNo(Integer applicationno);


}
