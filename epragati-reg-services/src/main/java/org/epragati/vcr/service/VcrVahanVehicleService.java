package org.epragati.vcr.service;

import java.util.List;

import org.epragati.master.vo.RegistrationDetailsVO;
import org.epragati.master.vo.VCRVahanVehicleDetailsVO;
import org.epragati.master.vo.VahanVehicleDetailsVO;

public interface VcrVahanVehicleService {

	VCRVahanVehicleDetailsVO convertVahanVehicleToVcr(VahanVehicleDetailsVO vahanVehicleDetailsVO, String prNo);

	Integer convertStringToInteger(String value);

	List<String> setCovs(String covCode,RegistrationDetailsVO registrationDetails);

}
