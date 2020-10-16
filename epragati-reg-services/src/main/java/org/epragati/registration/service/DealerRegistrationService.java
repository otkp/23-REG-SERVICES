package org.epragati.registration.service;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.epragati.jwt.JwtUser;
import org.epragati.master.dto.DealerRegDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.vo.DealerRegVO;
import org.epragati.master.vo.MakersVO;
import org.epragati.master.vo.UserVO;
import org.epragati.payments.vo.ClassOfVehiclesVO;
import org.epragati.regservice.vo.ApplicationSearchVO;
import org.springframework.web.multipart.MultipartFile;

public interface DealerRegistrationService {

	/**
	 * Saving of Fresh Delaer Registration Details
	 * 
	 * @param dealerregVO
	 * @param uploadfiles
	 * @return Optional<DealerRegVO>
	 */
	Optional<DealerRegVO> saveDetails(String dealerregVO, MultipartFile[] uploadfiles);

	/**
	 * Initiate Approval Process Method for Fresh Dealer Registration
	 * 
	 * @param dealerRegistrationDTO
	 */

	void initiateApprovalProcessFlow(DealerRegDTO dealerRegistrationDTO);

	/**
	 * Dealer application search
	 * 
	 * @param applicationSearchVO
	 * @return Optional<DealerRegVO>
	 */
	Optional<DealerRegVO> dealerRegistrationService(ApplicationSearchVO applicationSearchVO);

	/**
	 * Suspension Cancellation of Dealer updating
	 * 
	 * @param userDTO
	 * @param dealerRegVO
	 * @param jwtUser
	 * @param httpRequest
	 */
	void saveDealerDetailsinDealerRegistrationCollection(UserDTO userDTO, DealerRegVO dealerRegVO, JwtUser jwtUser,
			HttpServletRequest httpRequest);

	/**
	 * Dealer application Repay
	 * 
	 * @param applicationFormNo
	 * @return
	 */
	Optional<DealerRegVO> doDealerRePay(String applicationFormNo);

	/**
	 * Update Payment status of dealer application
	 * 
	 * @param dealerRegVO
	 */
	void updatePaymentStatusOfDealerRegistrationDetails(DealerRegVO dealerRegVO);

	/**
	 * Get List of Dealers For Parent Child Mapping
	 * 
	 * @return
	 */
	List<UserVO> getListOfDealers();

	/**
	 * Get List of Makers as per class of vehicles
	 * 
	 * @param covs
	 * @return
	 */
	List<MakersVO> getListOfMakers(List<ClassOfVehiclesVO> covs);

	/**
	 * 
	 * @param jwtUser
	 * @return
	 */
	Optional<DealerRegVO> getExistingCovsAndMakers(JwtUser jwtUser);

}
