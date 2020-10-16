package org.epragati.rta.service.impl.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.epragati.master.dto.UserDTO;
import org.epragati.rta.vo.RegistrationCardDetailsVO;
import org.epragati.rta.vo.RegistrationCardPrintedVO;

/**
 * 
 * @author saroj.sahoo
 *
 */
public interface RegistrationCardService {

	/**
	 * 
	 * @param prNo
	 * @param officeCode
	 * @return
	 */
	Optional<RegistrationCardDetailsVO> findRegistrationDetails(String prNo, String officeCode);

	/**
	 * 
	 * @param trGeneratedDate
	 * @param officeCode
	 * @return
	 */
	List<RegistrationCardDetailsVO> findRegistrationDetails(LocalDate prGeneratedDate, String officeCode);

	/**
	 * 
	 * @param fromDate
	 * @param toDate
	 * @param officeCode
	 * @return
	 */
	List<RegistrationCardDetailsVO> findRegistrationDetails(LocalDate fromDate, LocalDate toDate, String officeCode);

	/**
	 * 
	 * @param prNo
	 * @param userDetails
	 * @param officeCode
	 * @return
	 */
	Map<String, String> saveCardPrintedDetails(RegistrationCardPrintedVO registrationCardPrintedVO, Optional<UserDTO> userDetails, String officeCode);
	
	
	/**
	 * 
	 * @param prNo
	 * @param officeCode
	 * @return
	 */
	Optional<RegistrationCardDetailsVO> findRegistrationDetailsForSmartCard(String prNo, String officeCode);
	
	/**
	 * 
	 * @param prGeneratedDate
	 * @param officeCode
	 * @return
	 */
	List<RegistrationCardDetailsVO> findRegistrationDetailsForSmartCardReprint(LocalDate appraveDate, String officeCode);

	/**
	 * 
	 * @param registrationCardPrintedVO
	 * @param userDetails
	 * @param officeCode
	 * @return
	 */
	Map<String, String> saveSmartCardRePrintedDetails(RegistrationCardPrintedVO registrationCardPrintedVO,
			Optional<UserDTO> userDetails, String officeCode);

	String checkManufacturedMonthYear(String manufacturedMonthYear);


}
