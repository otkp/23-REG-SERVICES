package org.epragati.mobile.auth.service;

import java.io.IOException;
import java.util.Optional;

import org.epragati.master.vo.UserVO;
import org.epragati.mobile.auth.dto.CitizenAppAuthenticationDTO;
import org.epragati.mobile.auth.vo.CitizenAppAuthenticationVO;
import org.epragati.mobile.auth.vo.MobileAppRequestVO;
import org.epragati.service.enclosure.vo.ImageVO;
import org.springframework.data.util.Pair;
import org.springframework.web.multipart.MultipartFile;

import com.mongodb.gridfs.GridFSDBFile;

/***
 * 
 * 
 * @author roshan.jugalkishor
 *
 */
public interface CitizenAppAuthenticationService {

	/**
	 * 
	 * 
	 * @param citizenAppAuthenticationVO
	 * @return
	 */
	public void createUser(CitizenAppAuthenticationVO citizenAppAuthenticationVO);

	/**
	 * 
	 * 
	 * @param mobileNo
	 * @param password
	 * @return
	 */
	public CitizenAppAuthenticationVO loginUser(String mobileNo, String password,String deviceNo);

	/**
	 * 
	 * 
	 * @param aadharNumber
	 * @param otp
	 * @return
	 * @throws Exception 
	 */

	public CitizenAppAuthenticationVO verifyOTP(String aadharNo,String deviceNo, String otp) throws Exception;

	/**
	 * 
	 * 
	 * @param mobileNumber
	 * @return
	 */
	public String forgotPassword(String password, String mobileNo,String deviceNo);

	/**
	 * 
	 * 
	 * @param mobileNumber
	 * @param otp
	 * @return
	 */
	public String verifyOTPforFogotPassword(String mobileNo,String deviceNo, String otp);

	/**
	 * 
	 * @param aadharNumber
	 * @return
	 */
	public Pair<Boolean, String> generateOTPUsingAadharNumber(String aadharNo,String deviceNo);

	/**
	 * 
	 * @param mobileNumber
	 * @return
	 */
	public Pair<Boolean, String> generateOTPUsingMobileNumberForgotPassword(String mobileNo,String deviceNo);

	public Pair<Boolean, String> CheckValdationBasedOnDeviceNo(String deviceNo);

	public String getApplicateImageForMobile(String MobileAppRequestVO, MultipartFile[] uploadfiles) throws IOException;

	public Optional<GridFSDBFile> getEnclosureOfApplicateImage(String aadharNo, String mobileNo, String deviceNo);

	public String getDlNo(String aadharNo);

	public Optional<UserVO> getUserDetailsByUserNameAndMobile(String username, String mobile);

}
