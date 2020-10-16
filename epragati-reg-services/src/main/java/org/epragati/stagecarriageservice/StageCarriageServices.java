package org.epragati.stagecarriageservice;

import java.io.IOException;
import java.util.List;

import org.epragati.exception.RcValidationException;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.vo.SearchVo;
import org.epragati.regservice.vo.RcValidationVO;
import org.epragati.regservice.vo.RegServiceVO;
import org.epragati.stagecarriageservices.vo.MasterStageCarriageServicesVO;
import org.springframework.data.util.Pair;
import org.springframework.web.multipart.MultipartFile;

public interface StageCarriageServices {

	
	List<MasterStageCarriageServicesVO> getStageCarriageServices();

	SearchVo aadharValidationForStageCarriageServices(RcValidationVO rcValidationVO, boolean requestFromSave)
			throws RcValidationException;

	void validationForSCRTServices(RcValidationVO rcValidationVO, RegistrationDetailsDTO registrationDTO)
			throws RcValidationException;

	RegServiceVO saveStageCarriageServices(String regServiceVO, MultipartFile[] multipart, JwtUser user)
			throws IOException, RcValidationException;

	SearchVo getPermitDetailsForScrt(RcValidationVO rcValidationVO);

	Pair<RegistrationDetailsDTO, RegistrationDetailsDTO> validationForScrtReplacementOfVehicle(String prNo,
			String nonPermitPrNo);

	void updateDetailsForNewStageCarriage(RegServiceVO regServiceDetail);

	List<RegServiceVO> getStageCarriageRecordsInAO();

	RegServiceVO stageCarriageApprovalProcess(RegServiceVO regServiceVO, String role, JwtUser user);

	void saveInFeeCorrections(RegServiceVO list, JwtUser user, String role);
}
