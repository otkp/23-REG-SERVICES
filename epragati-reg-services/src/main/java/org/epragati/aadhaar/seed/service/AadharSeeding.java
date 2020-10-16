package org.epragati.aadhaar.seed.service;

import java.util.List;
import java.util.Optional;

import org.epragati.aadhaar.AadhaarDetailsRequestVO;
import org.epragati.aadhaar.AadharDetailsResponseVO;
import org.epragati.aadhaar.seed.engine.AadharDetilsModel;
import org.epragati.aadhaarseeding.vo.AadhaarSeedDetailsVO;
import org.epragati.aadhaarseeding.vo.AadhaarSeedVO;
import org.epragati.aadhaarseeding.vo.AahaarSeedMatchVO;
import org.epragati.common.dto.aadhaar.seed.AadhaarSeedDTO;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dto.ApplicantDetailsDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.vo.ApplicantAddressVO;
import org.epragati.master.vo.RegServiceAadharSeedingInputVO;
import org.epragati.master.vo.RegistrationDetailsVO;
import org.epragati.util.Status;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author sairam.cheruku
 *
 */
public interface AadharSeeding {

	/**
	 * Aadhar Seeding Process
	 * 
	 * @param prNo
	 * @param officeCode
	 * @param model
	 * @return
	 * @throws BadRequestException
	 */
	Optional<AadhaarSeedDetailsVO> processAadhaarSeeding(String prNo, String officeCode, AadhaarDetailsRequestVO model,
			String mobileNo, String emailId, ApplicantAddressVO presentAddress) throws BadRequestException;

	/**
	 * 
	 * @param aadharSeedDTO
	 * @param id
	 * @param officeCode
	 * @param comment
	 * @param status
	 * @param user
	 */
	void processAadhaarSeeding(AadhaarSeedDTO aadharSeedDTO, String id, String officeCode, String comment,
			Status.AadhaarSeedStatus status, String user, String selectedRole);

	void updateAadhaarrSeedingEnclosure(RegServiceAadharSeedingInputVO aadhaarseding, MultipartFile[] uploadfiles);

	Optional<AadhaarSeedVO> aadhaarSeedingStatus(AadharDetilsModel input);

	// Optional<AadhaarSeedVO> getAadhaarSeedDetailsByAadhaarNo(String aadharNo);

	Optional<AadhaarSeedVO> getAadhaarSeedDetails(String officeCode, String aadhaarSeedingId, String userId,
			String role);

	RegistrationDetailsDTO getRegistrationDetails(String prNo, String officeCode);

	String applicantDetailsmatrix(RegistrationDetailsVO regVo, AadharDetailsResponseVO ekycVO,
			List<AahaarSeedMatchVO> list);

	void saveAadharSeed(AadhaarSeedVO aadhaarSeedInput, UserDTO userDetails, String currentUser, String selectedRole);

	Optional<AadhaarSeedVO> getAadhaarSeedPendingRecord(String officeCode, String role, String user);

	Optional<AadhaarSeedVO> getAadhaarSeedDetailsByAadhaarNo(String aadharNo, String prNo, String officeCode,
			String role);

	void updateDistirctDetails(ApplicantDetailsDTO applicantDTO, OfficeDTO officeDetails);

	List<AadhaarSeedVO> getAadhaarSeedPendingRecordForRto(String officeCode, String selectedRole, String user,
			String status);

	void saveAadharSeedForRto(AadhaarSeedVO aadhaarSeedInput, UserDTO userDetails, String user, String selectedRole,
			String reqHeader);

}