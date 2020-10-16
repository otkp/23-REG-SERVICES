package org.epragati.rta.service.impl.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.epragati.financier.vo.FinancierUserUpdateVO;
import org.epragati.images.vo.ImageInput;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dto.ActionDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.RolesDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.vo.UserVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 
 * @author saroj.sahoo
 *
 */
public interface UserManagementService {


	void saveOfficerDetails(UserVO userVO, JwtUser jwtUser, List<ImageInput> imageInput,
			MultipartFile[] uploadfiles, HttpServletRequest request) throws IOException;
	UserVO getOfficerDetails(String userId, JwtUser jwtUser, String role);
	UserDTO findByUserId(String userId);
	UserVO saveChildUser(FinancierUserUpdateVO userVO, String userId);
	Optional<UserVO> getUserDetails(UserVO userVO,JwtUser jwtUser);
	void modifyUserDetails(FinancierUserUpdateVO userVO,JwtUser jwtUser);
	List<UserDTO> getAllUsers(UserVO userVO,String userId);
	boolean validateUserId(String userId);
	List<OfficeDTO> fetchOfficeCode(String officeCode, String role);
	List<RolesDTO> getAllRoles();
	void doAccountLockOrUnlock(UserVO userVO, JwtUser jwtUser, HttpServletRequest request);
	UserVO getCollectionCorrectionUserDetails(String userId,String role);
	List<ActionDTO> getActionItems();
}
 