package org.epragati.regservice.otherstate;

import java.util.List;
import java.util.Optional;

import org.epragati.master.vo.InsuranceDetailsVO;
import org.epragati.master.vo.RegistrationDetailsVO;
import org.epragati.master.vo.StateVO;
import org.epragati.master.vo.VahanVehicleDetailsVO;
import org.epragati.payments.vo.ClassOfVehiclesVO;
import org.epragati.regservice.vo.OtherStateVahanVO;
import org.epragati.regservice.vo.RegServiceVO;
import org.springframework.data.util.Pair;

public interface OtherStateVahanService {

	Pair<OtherStateVahanVO, List<String>> convertVahanVehicleToOtherState(VahanVehicleDetailsVO vo,String prNo);

	InsuranceDetailsVO setInsuranceDetails(VahanVehicleDetailsVO vo);

	Optional<StateVO> setStateDetails(String stateCode);

	List<ClassOfVehiclesVO> setCovs(String covCode,RegistrationDetailsVO registrationDetailsVO);

	void validationForVahanServices(RegServiceVO regServiceVO);
}
