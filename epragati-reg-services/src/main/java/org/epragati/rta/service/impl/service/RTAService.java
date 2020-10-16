/**
 * 
 */
package org.epragati.rta.service.impl.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.epragati.aadhaar.AadhaarDetailsRequestVO;
import org.epragati.actions.dto.CorrectionDTO;
import org.epragati.common.vo.PropertiesVO;
import org.epragati.dto.enclosure.TemporaryEnclosuresDTO;
import org.epragati.fa.vo.FinancialAssistanceVO;
import org.epragati.images.vo.InputVO;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dto.BaseRegistrationDetailsDTO;
import org.epragati.master.dto.CollectionCorrectionServiceLogsDTO;
import org.epragati.master.dto.Enclosures;
import org.epragati.master.dto.FeeCorrectionDTO;
import org.epragati.master.dto.FinancierCreateRequestDTO;
import org.epragati.master.dto.LockedDetailsDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.RoleActionDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.vo.BodyTypeVO;
import org.epragati.master.vo.DealerRegVO;
import org.epragati.master.vo.MasterCovVO;
import org.epragati.master.vo.MasterFcQuestionVO;
import org.epragati.master.vo.MasterRcCancellationQuestionsVO;
import org.epragati.master.vo.MasterStoppageQuationsVO;
import org.epragati.master.vo.MasterStoppageRevocationQuationsVO;
import org.epragati.master.vo.RTADashboardVO;
import org.epragati.master.vo.RegistrationDetailsVO;
import org.epragati.master.vo.TaxTypeVO;
import org.epragati.master.vo.UserVO;
import org.epragati.master.vo.VCRVahanVehicleDetailsVO;
import org.epragati.payment.dto.BreakPaymentsSaveDTO;
import org.epragati.payment.report.vo.NonPaymentDetailsVO;
import org.epragati.payment.report.vo.RegReportVO;
import org.epragati.payment.report.vo.ReportsVO;
import org.epragati.permits.vo.PermitSuspCanRevVO;
import org.epragati.rcactions.RCActionRulesVO;
import org.epragati.rcactions.RCActionsVO;
import org.epragati.rcactions.SearchRcRequestVO;
import org.epragati.regservice.mapper.ReportDataVO;
import org.epragati.regservice.mapper.TaxDataVO;
import org.epragati.regservice.vo.ApplicationCancellationVO;
import org.epragati.regservice.vo.ApplicationSearchVO;
import org.epragati.regservice.vo.AuctionDetailsVO;
import org.epragati.regservice.vo.CitizenApplicationSearchResponceVO;
import org.epragati.regservice.vo.CommonFieldsVO;
import org.epragati.regservice.vo.FeeCorrectionVO;
import org.epragati.regservice.vo.LockedDetailsVO;
import org.epragati.regservice.vo.RegServiceVO;
import org.epragati.rta.reports.vo.CitizenSearchReportVO;
import org.epragati.rta.vo.CitizenDashBordDetails;
import org.epragati.rta.vo.CitizenEnclosuresLogsVO;
import org.epragati.rta.vo.CorrectionsVO;
import org.epragati.rta.vo.DashBordDetails;
import org.epragati.rta.vo.RtaActionVO;
import org.epragati.rta.vo.UnlockServiceRecordVO;
import org.epragati.service.enclosure.vo.DisplayEnclosures;
import org.epragati.util.CorrectionEnum;
import org.epragati.util.PermitsEnum.PermitType;
import org.epragati.vcrImage.dto.VcrFinalServiceDTO;
import org.json.JSONException;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author kumaraswamy.asari
 *
 */
public interface RTAService {

	/**
	 * 
	 * @param officeCode
	 * @param userId
	 * @param role
	 * @return
	 */
	DashBordDetails getDashBoard(String officeCode, String userId, String role);

	/**
	 * 
	 * @param officeCode
	 * @param userName
	 * @param role
	 * @param vehicleType
	 * @return
	 */

	List<RegistrationDetailsVO> getPendingList(String officeCode, String userName, String role, String vehicleType);

	/**
	 * 
	 * @param userId
	 * @param actionActionVo
	 * @param role
	 * @throws Exception
	 */
	void doAction(JwtUser jwtUser, RtaActionVO actionActionVo, String role)
			throws Exception;

	/**
	 * 
	 * @param officeCode
	 * @param userId
	 * @param applicationNo
	 * @return
	 */
	RegistrationDetailsVO viewList(String officeCode, String userId, String applicationNo);

	/**
	 * 
	 * @return
	 */
	BaseRegistrationDetailsDTO createBaseRegistrationDetailsDTODummyData();

	/**
	 * 
	 * @param cov
	 * @return
	 */
	Optional<List<MasterCovVO>> findInvalidCovs(String cov);

	/**
	 * 
	 * @param stagingDto
	 * @throws CloneNotSupportedException
	 */
	void assignPR(StagingRegistrationDetailsDTO stagingDto) throws CloneNotSupportedException;

	/**
	 * 
	 * @param officeCode
	 * @param user
	 * @param role
	 * @return dash board details
	 */
	Optional<CitizenDashBordDetails> getCitizenDashBoardDetails(String officeCode, String user, String role);

	/**
	 * 
	 * @param applicationNo
	 * @return
	 */

	List<RoleActionDTO> getImageActions(String applicationNo);

	/**
	 * 
	 * @param officeCode
	 * @param user
	 * @param role
	 * @param service
	 * @param isDataEntry 
	 * @return
	 */
	List<RegServiceVO> getServicesPendingList(String officeCode, String user, String role, String service,String vehicleType,String fc,Boolean isDataEntry);

	/**
	 * 
	 * @param officeCode
	 * @param user
	 * @return
	 */

	Integer getDashBoardCountsForDataEntry(String officeCode, String user);

	/**
	 * 
	 * @param officeCode
	 * @param user
	 * @return
	 */

	List<RegServiceVO> getDashBoardRecordsForDataEntry(String officeCode, String user);

	/**
	 * 
	 * @param officeCode
	 * @param user
	 * @param applicationNo
	 * @return
	 */

	RegServiceVO viewDashBoardRecordsForDataEntry(String officeCode, String user, String applicationNo);

	/**
	 * 
	 */
	
	RegistrationDetailsVO findByTrNo(String trNo);
	
	void updateBodyBuildingDetails(String userId, RtaActionVO actionVo, Optional<UserDTO> userDetails, String role)
			throws Exception;
	
	List<RegistrationDetailsVO> getPendingListForBodyBuilding(String officeCode, String userId, String role);

	/**
	 * 
	 * @param trNo
	 * @return
	 */
	RegistrationDetailsVO findBasedOnTrNo(String trNo);

	DashBordDetails getCountForBB(String officeCode, String userId, String role);

	/**
	 * 
	 * @return
	 */
	Optional<List<TaxTypeVO>> taxType();
	void updateTrailerDetails(String userId, RtaActionVO actionVo, Optional<UserDTO> userDetails, String role)
			throws Exception;
	
	boolean processFlow(StagingRegistrationDetailsDTO stagingDTO, JwtUser jwtUser, String action,
			String applicationNo, String role);

	void updateForDataEntryDetails(String userId, RtaActionVO actionVo, Optional<UserDTO> userDetails,
			String role) throws Exception;

	RegServiceVO getRegDetailsByApplicationNo(String applicationNo);
	
	Optional<RCActionsVO> getSuspensionInformation(SearchRcRequestVO searchRcRequestVO);
	void createSuspensionDetails(RCActionsVO suspensionVO, JwtUser jwtUser,String role);
	
	List<RCActionRulesVO> getAllActionRules();
	
	List<RCActionRulesVO> getActionSectionsBasedOnSource(String sectionName);

	String saveObjectionDetails(RegServiceVO regServiceVO, JwtUser jwtUser);

	RegistrationDetailsVO findBasedOnPrNo(String prNo);

	void saveRevocationDetails(RegServiceVO regServiceVO, JwtUser jwtUser);

	RegistrationDetailsVO getSecondVehicleDetails(UserDTO userDetails, RtaActionVO actionActionVo);

	Boolean releaseSecondVehicle(UserDTO userDetails, RtaActionVO actionActionVo);

	String doActionForFC(RegServiceVO regServiceVO, JwtUser jwtUser, MultipartFile[] uploadfiles) throws IOException;

	Optional<List<MasterFcQuestionVO>> getMVIQuestionFOrFC(String selectedRole);
	/**
	 * permit related
	 */
	Optional<PermitSuspCanRevVO> getPermitSuspensionInformation(SearchRcRequestVO searchRcRequestVO);

	void createPermitSuspensionDetails(PermitSuspCanRevVO suspensionVO, JwtUser userDetails, String role);

	InputVO getEnclousersByServiceId();
	
	Integer getCcoSuspendPending(String officeCode, String id, String role);	
	
	List<RCActionsVO> getPendingListOfSuspend(String officeCode, String user, String role);

	Integer getCcoPermitSuspendCount(String officeCode, String id, String role);

	List<PermitSuspCanRevVO> getPermitPendingListOfSuspend(String officeCode, String user, String role);

	List<BreakPaymentsSaveDTO> fetchBreakPaymentDetails(String applicationNo);

	List<MasterStoppageQuationsVO> getStoppageQuations(String role);

	List<MasterStoppageRevocationQuationsVO> getStoppageRevocationQuations(String role);
	
	TaxDataVO getCorrectionsDataEntryDetails(UserDTO userDetails,String prNo,CorrectionEnum serviceType, PermitType permitType);
	
	RegistrationDetailsVO findBasedOnPrNos(List<String> prNo);
	
	Boolean saveCorrectionsData(UserDTO userDetails,TaxDataVO taxDataVO,CorrectionEnum serviceType) throws CloneNotSupportedException;
	
	TaxDataVO getDiffTaxDetails(UserDTO userDetails,String prNo);

	Boolean saveDiffTax(UserDTO userDetails,TaxDataVO taxDataVO);

	
	
	Integer getFreshRcforFinanceCount(String officeCode, String id, String role);

	List<RegServiceVO> getFreshRcforFinanceList(JwtUser jwtUser);

	TaxDataVO getTaxDetails(UserDTO userDetails, String prNo);
	
	Optional<List<MasterCovVO>> getClassOfVehicle(String cov);

	List<BodyTypeVO> getBodyType();	
	
	void unlockServiceRecord(UnlockServiceRecordVO unlockServiceRecordVO);
	
	void checkValidtionForRtoAadharValiadtion(UserDTO userDetails,AadhaarDetailsRequestVO aadhaarDetailsRequestVO);

	TaxDataVO getCorrectionDetailsForAllServices(UserDTO userDTO, String prNo);
	
	Boolean saveCorrectionsDataForMultipleServices(UserDTO userDetails,TaxDataVO taxDataVO);
	
	Enclosures viewBiLaterailTaxDetails(String officeCode,  String selectedRole);

	DashBordDetails bilateralTaxDetailsCount(String officeCode, String selectedRole);

	String saveBilateralTaxDetails(String userId, RtaActionVO actionVo, Optional<UserDTO> userDetails, String role)
			throws Exception;
/**
 * =======================  getPendingFinancerApplicationsList
 * @param officeCode
 * @param loggedInUser
 * @param role
 * @return
 */
	List<FinancierCreateRequestDTO> getPendingFinancierApplicationsList(String officeCode, String loggedInUser,
			String role);

	Optional<RegServiceVO> secondVehicleValidationsforOtherState(UserDTO userDetails, RtaActionVO actionActionVo);

	Boolean releaseSecondVechicleFortaxexemptions(UserDTO userDetails, RtaActionVO actionActionVo);

	CitizenApplicationSearchResponceVO getVehicleData(ApplicationSearchVO applicationSearchVO,boolean requestFromSearch,UserDTO userDetails);

	void saveFeeCorrection(FeeCorrectionVO vo,UserDTO userDetails);

	DashBordDetails feeCorrectionCount(String officeCode, String selectedRole);

	CitizenApplicationSearchResponceVO getFeeCorrectionPendingDoc(String officeCode, String selectedRole, UserDTO userDetails);
	
	Optional<RTADashboardVO> getCitizenDashBoardMenuDetails(String officeCode, String user, String role);
	
	void processPR(StagingRegistrationDetailsDTO staginDto)throws CloneNotSupportedException;

	void updateStagingRegDetails(StagingRegistrationDetailsDTO staginDto);

	CitizenSearchReportVO getVcrDetailsForCorrections(ApplicationSearchVO applicationSearchVO, UserDTO userDetails);

	void saveOffences(CitizenSearchReportVO vo, UserDTO userDetails,HttpServletRequest request);

	List<CitizenEnclosuresLogsVO> previousIterationDetails(String applicationNo);

	
	PropertiesVO collectionTypedropdown(String role,String module);

	void saveForCollectionCorrections(CorrectionsVO correctionsVO, String user);
	
	CorrectionsVO saveForClientRequestDetails(CorrectionsVO correctionsVO, String user, HttpServletRequest request,Boolean requestType);
	
	void saveForClientResponseDetails(CorrectionsVO correctionsVO, String user, HttpServletResponse response);
	Optional<List<MasterCovVO>> getClassOfVehicleforMVI(String cov);
	
	String saveRcCancellationDetails(RegServiceVO regServiceVO,MultipartFile[] uploadfile, JwtUser jwtUser);
	
	List<UserVO> getMviNames(String applicationNo);

	LockedDetailsDTO setLockedDetails(String userId, String role, Integer iterationNo, String module,
			String applicatioNo);	
	
	List<String> getMviByOffice(String officeCode);

	Optional<CorrectionsVO> getReadOnlyCorrectionDetails(CorrectionsVO vo);

	List<CollectionCorrectionServiceLogsDTO> getDynamicCorrectionFieldsForDTC(Map<String, String> map) throws JSONException;

	Optional<CollectionCorrectionServiceLogsDTO> getCorrectionLogFields(String serviceType, String token);

	List<CorrectionDTO> getDataForEntry();

	void saveDataForEntry(CorrectionDTO dto) throws Exception;
	
	Enclosures getfRCRecordForAOBasedOnprNo(String prNo, String officeCode, String selectedRole, String userId);

	Boolean enableFrom37ForAO(String applcationNo, String userId);
	
	String saveRtoDetailsForShowCause(ApplicationSearchVO vo , String role);
	
	Optional<ReportsVO> getRtoDetilsForShowCauseNo(JwtUser jwtUser,Pageable pagable);
	
	Optional<ReportsVO> getRtoDetilsForShowCauseNoWithMandal(String Mandal,JwtUser jwtUser,Pageable pagable);
	
	Optional<NonPaymentDetailsVO> getRtoDetailsForShowCauseSingle(ApplicationSearchVO applicationSearchVO,JwtUser jwtUser);

	String checkMviNameValidation(String applicationNo, String mviName);

	List<Map<String, String>> getAllVehicleTypes();

	List<RegServiceVO> getStoppagePendingList(String officeCode, String role, String service);

	String saveMviNameInStoppage(String applicationNo, String userId, UserDTO userDetails, String ipAddress, String role);

	List<RegServiceVO> returnStoppageList(String officeCode, String role,Optional<UserDTO> userDetails, String service );
	
	List<RegServiceVO> getRcCancellationPendingListForMvi(String user, String role);
	
	Optional<List<MasterRcCancellationQuestionsVO>> getMVIQuestionForRcCancellation(String selectedRole);

	List<String> getModulesForCollectionCorrection(List<String> user);
	
	String doActionForRcCancellation(RegServiceVO regServiceVO, JwtUser jwtUser, MultipartFile[] uploadfiles) throws IOException;
	
	List<RegServiceVO> getRcCancellationPendingListForAOAndRTO(String officeCode, String role);
	
	String saveRcCancellationDetailsForOtherState(RegServiceVO regServiceVO,MultipartFile[] uploadfile, JwtUser jwtUser);
	
	List<DealerRegVO> dealerReistrationPendingList(String officeCode, String role, String serviceS, UserDTO userDetails);

	List<UserVO> getMVInamesBasedOnMandal(String applicationNo);

	String assignMVIforDealerRegistration(String applicationNo, String userId, UserDTO userDTO, String remoteAddr,
			String selectedRole);

	VcrFinalServiceDTO checkVehicleTrNotGenerated(List<VcrFinalServiceDTO> vcrList);
	
	Optional<UserVO> getDealerDetailsByUserIdOrName(String userId, String dealerName);

	List<String> getListOfDealersByOfficeCode(JwtUser jwtUser);

	void dealerSuspensionAndCancellation(DealerRegVO dealerRegVO, JwtUser jwtUser, HttpServletRequest httpRequest);

	void setClassOfVehiclesForDealer(UserVO userVO);

	List<RegServiceVO> getStageCarriagePendingList(String officeCode, String role, String service);

	CitizenSearchReportVO getStageCarriageByAppNO(JwtUser jwtUser, String applicationNo, String role);
	
	Integer dealerReistrationPendingListCount(String officeCode, String role, String service, UserDTO userDetails);

	List<UserVO> getMviNamesForAuction(String officeCode);

	String saveAuctionDetails(JwtUser jwtUser,String vo, MultipartFile[] uploadfiles, HttpServletRequest request);

	List<AuctionDetailsVO> getAuctionDetailsPendingList(String officeCode, String role, UserDTO userDetails);

	AuctionDetailsVO getAuctionDetailsByAppNo(String role, UserDTO userDetails, String applicationNo);

	VCRVahanVehicleDetailsVO getRegDetailsForAuction(ApplicationSearchVO searchvo);

	String saveAuctionVehicleDetails(JwtUser jwtUser, String stringVo, MultipartFile[] uploadfiles,
			HttpServletRequest request, UserDTO userDetails);

	List<AuctionDetailsVO> getAuctionDetailsPendingListForDTC(String officeCode, String role, UserDTO userDetails);

	String saveAuctionDetailsByDtc(String applicationNo, String role, UserDTO userDetails,
			HttpServletRequest request);

	String getAuctionToken(String prNo, String role, UserDTO userDetails, HttpServletRequest request);

	CommonFieldsVO stoppagecountForApp(String officeCode, String user);

	String saveStoppageRevocation(String applicationNo, UserDTO userDetails, String ipAddress, String role,
			String status);
	
	void doDeemedAction(List<StagingRegistrationDetailsDTO> deemedStagingRegDetlsList);

	ReportDataVO getRegistrationAndTax(RegReportVO regReportVO);
	
	UserVO getFinacierData(String prNo);
	
	UserVO getFinacierUserData(String userId);
	
	

	FinancialAssistanceVO getVehicleDetails(String prNo, String aadharNo,AadhaarDetailsRequestVO aadhaarDetailsRequest);
	
	String uploadDataForFinancialAssistance(String vo,MultipartFile[] uploadFiles,String userId);

	FinancialAssistanceVO getMdoFinancialAssistance(JwtUser jwtUser, String district, String mandal, String village);
	
	String updateFaDetails(FinancialAssistanceVO financialAssistance,String user,MultipartFile[] files);

	Optional<ApplicationCancellationVO> getDetailsForCancellation(String applicationNo, String prNo, JwtUser jwtUser,
			String selectedRole);

	void saveApplicationCancellationDetails(String applicationNo, String prNo, JwtUser jwtUser, String selectedRole,
			HttpServletRequest request, String remarks);

	List<CommonFieldsVO> getListOfFCDocs(JwtUser jwtUser);

	RegServiceVO getTpVtOfPrNo(String prNo);

	void saveFCImagesFromApp(InputVO vo, JwtUser jwtUser, MultipartFile[] uploadfiles) throws IOException;

	List<DisplayEnclosures> getFCMobileUploadImages(String prNo, JwtUser jwtuser);

	List<String> getMviNamesByDistrict(String officeCode);

	Optional<TemporaryEnclosuresDTO> returnTemporaryImages(String applicationNo, JwtUser jwtuser);

	List<TemporaryEnclosuresDTO> returnAllTemporaryImages(String applicationNo);

	Object getLockedDeatilsWithApplicationNo(String applicationNo);

	List<LockedDetailsVO> getLockedDetailsOfficewise(String officeCode, LockedDetailsVO lockVO, Pageable page);

	FeeCorrectionDTO getFeeDoc(String chassisNo, String role);

	boolean validationForFeeParts(FeeCorrectionVO vo, FeeCorrectionDTO olddto, FeeCorrectionDTO newdto,
			UserDTO userDetails, RegistrationDetailsDTO registrationDetails);

	String reuploadFCEnclosures(RegServiceVO regServiceVO, JwtUser jwtUser, MultipartFile[] fcReuploadImgs);

	//void freshRCAODashBoardLOckedDetailsRemove(String officeCode, String user, String role, String service);
		
	
}
