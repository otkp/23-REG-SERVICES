package org.epragati.aadhaarAPI;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.epragati.aadhaar.AadhaarDetailsRequestVO;
import org.epragati.aadhaar.AadharDetailsResponseVO;
import org.epragati.aadhaarAPI.DTO.AadhaarTransactionDTO;

public interface AadhaarRestServiceConsumer  {
	
	AadharDetailsResponseVO getAadhaarData(AadhaarDetailsRequestVO aadhaarDetailsRequestModel, AadhaarSourceDTO aadhaarSourceDTO);
	 
	AadharDetailsResponseVO sendOTPRequest(AadhaarDetailsRequestVO aadhaarDetailsRequestModel);
	 
	AadharDetailsResponseVO otpAuthentication(AadhaarDetailsRequestVO aadhaarDetailsRequestModel);

	List<AadhaarTransactionDTO> getAllAadhaarLogs();
	
	AadharDetailsResponseVO consumeAptsSoapApi(AadhaarDetailsRequestVO aadhaarDetailsRequestModel);

}
