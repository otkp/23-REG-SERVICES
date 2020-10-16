package org.epragati.restGateway;

import java.util.Optional;

import org.epragati.aadhaar.AadhaarDetailsRequestVO;
import org.epragati.aadhaar.AadharDetailsResponseVO;

public interface AadhaarServiceGateWay {
	
	public Optional<AadharDetailsResponseVO> validateAadhaar(AadhaarDetailsRequestVO model) ;

}
