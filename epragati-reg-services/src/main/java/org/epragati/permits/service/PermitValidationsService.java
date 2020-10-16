package org.epragati.permits.service;

import java.util.Optional;

import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.payments.vo.TransactionDetailVO;
import org.epragati.permits.dto.PermitDetailsDTO;
import org.epragati.regservice.dto.CitizenFeeDetailsInput;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.vo.RcValidationVO;
import org.epragati.regservice.vo.RegServiceVO;

public interface PermitValidationsService {

	public void doPermitValidations(RcValidationVO rcValidationVO, RegistrationDetailsDTO registrationDetailsDTO);

	/**
	 * 
	 * @param prNo
	 * @return
	 */
	Optional<PermitDetailsDTO> getPermitDetails(String prNo);

	/**
	 * 
	 * @param prNo
	 * @return
	 */
	public Optional<TaxDetailsDTO> getLatestTaxTransaction(String prNo);

	/**
	 * 
	 * @param dto
	 */
	public void validateIssueOfRecommendationLetterData(PermitDetailsDTO dto, RegServiceDTO regServiceDetails);

	RegServiceDTO permitTOPendingCheck(String prNo);

	/**
	 * For TP/SP Fees
	 * 
	 * @param input
	 * @param prNo
	 */

	void permitvalidateAndSetHomeStateOrOtherState(CitizenFeeDetailsInput input, String prNo,
			TransactionDetailVO transactionDetailVO);

	/**
	 * Validations for Other state Special Permit
	 * 
	 * @param inputRegServiceVO
	 */
	public void validationForOtherStateSpecialPermit(RegServiceVO inputRegServiceVO);

}
