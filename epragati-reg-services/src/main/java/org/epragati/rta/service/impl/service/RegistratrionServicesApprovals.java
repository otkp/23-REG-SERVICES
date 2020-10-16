package org.epragati.rta.service.impl.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.epragati.common.dto.aadhaar.seed.AadhaarSeedDTO;
import org.epragati.dto.enclosure.CitizenEnclosuresDTO;
import org.epragati.images.vo.InputVO;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dto.ApprovalProcessFlowDTO;
import org.epragati.master.dto.DealerRegDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.vo.MasterStoppageQuationsVO;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.vo.FreshApplicationSearchVO;
import org.epragati.regservice.vo.RegServiceVO;
import org.epragati.regservice.vo.VehicleStoppageMVIReportVO;
import org.epragati.rta.vo.EnclosuresVO;
import org.epragati.rta.vo.FreshRCActionVO;
import org.epragati.rta.vo.FreshRcReassignedMVIVO;
import org.epragati.rta.vo.RtaActionVO;
import org.epragati.service.enclosure.vo.CitizenImagesInput;
import org.epragati.util.StatusRegistration;
import org.springframework.web.multipart.MultipartFile;

/**
 * 
 * @author naga.pulaparthi
 *
 */

public interface RegistratrionServicesApprovals {
	
	/**
	 * 
	 * @param jwtUser
	 * @param actionActionVo
	 * @param role
	 * @throws Exception
	 */
	void approvalProcess(JwtUser jwtUser, RtaActionVO actionActionVo, String role,MultipartFile[] uploadfiles);

	/**
	 * 
	 * @param regServiceDTO
	 * @param iterator
	 */
	void initiateApprovalProcessFlow(RegServiceDTO regServiceDTO);
	
	/**
	 * 
	 * @param regServiceDTO
	 * @param role
	 */
	void incrmentIndex(RegServiceDTO regServiceDTO, String role);

	Optional<InputVO> getListOfSupportedEnclosers(CitizenImagesInput input);

	void saveMviActions(RtaActionVO vo, MultipartFile[] uploadfiles, JwtUser jwtUser);
	
	Optional<RegServiceVO> getServicesDetailsByApplicationNO(JwtUser jwtUser, String applicationNo,String role);

	RegServiceVO getVehicleDetails(String prNo, JwtUser jwtUser, String role);

	String savecfxDetails(RtaActionVO actionActionVo, JwtUser jwtUser);
	
	 ApprovalProcessFlowDTO getApprovalProcessFlowDTO(String role, Integer serviceId);
	 
	 ApprovalProcessFlowDTO getApprovalProcessFlowDTOForLock(String role, Integer serviceId);
	 void  missingFcDetails();

	void saveOtherStateData(RegServiceDTO regServiceDTO, RegistrationDetailsDTO registrationDetailsDTO);
	
	public Optional<RegServiceVO> getServicesDetailsByAppNO(JwtUser jwtUser, String applicationNo,
			String role);

	void updateApplicationNOInTaxDetails();

	RegServiceDTO saveBilateralTaxDetails(String userId, RtaActionVO actionVo, Optional<UserDTO> userDetails,
			String role) throws Exception;

	void updateMviOfficeDetails(RegServiceDTO regServiceDTO);

	RegServiceVO getVehicleDetails(String prNo, JwtUser jwtUser, String role, String aadharNo);

	void moveActionsDetailsToActionDetailsLogs(RegServiceDTO regServiceDTO);

	void updatesVehicleStoppageNewFlow(RtaActionVO reportVo, JwtUser jwtUser, String ipAddress, String role);

	List<VehicleStoppageMVIReportVO> getPendingQuarters(RegServiceDTO stagingDetailsDTO);

	Long daysLeftForAutoapproved(RegServiceDTO stagingDetailsDTO);


	void saveMviReportCommon(String jwtUser, List<VehicleStoppageMVIReportVO> report, RegServiceDTO stagingDetailsDTO,
			RegistrationDetailsDTO registrationDetailsDTO, boolean autoApprove,
			List<VehicleStoppageMVIReportVO> pendingReports, List<MasterStoppageQuationsVO> stoppageQuations);

	void updatesVehicleStoppageRevokationNewFolw(RegServiceDTO regServiceDTO,
			RegistrationDetailsDTO registrationDetailsDTO);

	Optional<InputVO> getListOfSupportedEnclosersForApp(RtaActionVO input, JwtUser jwtUser);

	List<InputVO> returnCurrentQuarterImagesForStoppage(String applicationNo, JwtUser jwtUser);

	void dealerApprovalProcess(JwtUser jwtUser, RtaActionVO rtaActionVO, String role, MultipartFile[] uploadfiles);

	StatusRegistration updateEnclosures(String role, List<EnclosuresVO> enclosureList,
			CitizenEnclosuresDTO citizenEnclosures);

	void saveScrtServices(RtaActionVO reportVo, JwtUser jwtUser, String ipAddress, String role);

	void updateTaxDetailsInRegCollection(RegServiceDTO regServiceDTO, RegistrationDetailsDTO staginDto);
	
	void prGenerationFromRegService(RegServiceDTO regServiceDTO);

	void updateRenewalofDealerShipDetails(DealerRegDTO dealerRegDTO);
	
	void reApproveServRecod(RegServiceDTO regServiceDTO,String role);

	void doActionAutoForServices(List<RegServiceDTO> regserviceslist);

	void mapStoppageRevocationImages(CitizenEnclosuresDTO enclosresDto, List<InputVO> map);

	FreshRCActionVO frcFinancierValidation(String role, RtaActionVO rtaActionVO);
	
	List<RegServiceVO> frcMviListData(String  service,String selectedRole,String user,String officeCode);

	List<RegServiceVO> frcRTOListData(String service, String selectedRole, String userId, String officeCode);

	FreshApplicationSearchVO downloadForm37AtAODashBoard(String prNo,String officeCode);

	List<RegServiceVO> frcAORecordsDisplay(String service, String selectedRole, String officeCode, String id,
			String status);
	Optional<FreshRcReassignedMVIVO> getFrcServiceReassignDetails(JwtUser jwtUser, String applicationNo, String role, String prNo);

	String reAssignMVIforFreshRC(FreshRcReassignedMVIVO freshRcReassignedMVIVO,JwtUser jwtUser);

	RegServiceVO withOutAadharListData(String prNo,String selectedRole, String userId, String officeCode);
}
