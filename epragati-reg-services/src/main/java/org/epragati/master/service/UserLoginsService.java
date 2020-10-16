package org.epragati.master.service;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.epragati.aadhaar.AadhaarDetailsRequestVO;
import org.epragati.jwt.JwtUser;
import org.epragati.master.vo.UserLoginsVO;
import org.epragati.master.vo.UserVO;

/**]
 * 
 * @author saikiran.kola
 *
 */

public interface UserLoginsService  {
	/**
	 * get all UserLogin data 
	 * @return
	 */
 public List<UserLoginsVO> getUserLoginDetails();

	public String changePassword(UserVO model, String currentUser);
	
	Optional<UserVO> getUserDetails(String UserId);
	public void resetPassword(AadhaarDetailsRequestVO aadhaarDetailsRequestVO, String fetchUserName, JwtUser jwtUser, HttpServletRequest request);
	Optional<UserVO> getUserForPasswordResetDetails(String fetchUserId, String loginOfficerOfficeCode);
}
