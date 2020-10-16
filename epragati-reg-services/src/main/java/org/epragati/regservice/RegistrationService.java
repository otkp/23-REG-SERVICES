package org.epragati.regservice;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.epragati.aadhaar.AadhaarDetailsRequestVO;
import org.epragati.aadhaar.AadhaarRequestVO;
import org.epragati.aadhaar.AadharDetailsResponseVO;
import org.epragati.aadhaarAPI.AadhaarSourceDTO;
import org.epragati.cfstVcr.vo.VcrDetailsVO;
import org.epragati.cfstVcr.vo.VcrInputVo;
import org.epragati.exception.RcValidationException;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.master.dto.TaxHelper;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.vo.AadharDropListVO;
import org.epragati.master.vo.AadharReqServiceIdsVO;
import org.epragati.master.vo.ApplicantDetailsVO;
import org.epragati.master.vo.BodyTypeVO;
import org.epragati.master.vo.InsuranceResponseVO;
import org.epragati.master.vo.InsuranceVO;
import org.epragati.master.vo.MasterFeedBackQuestionsVO;
import org.epragati.master.vo.MasterWeightsForAltVO;
import org.epragati.master.vo.OfficeVO;
import org.epragati.master.vo.RegistrationDetailsVO;
import org.epragati.master.vo.SearchVo;
import org.epragati.payments.vo.ClassOfVehiclesVO;
import org.epragati.payments.vo.FeeDetailsVO;
import org.epragati.payments.vo.TransactionDetailVO;
import org.epragati.regservice.dto.CitizenFeeDetailsInput;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.dto.RegServicesFeedBack;
import org.epragati.regservice.vo.AlterationVO;
import org.epragati.regservice.vo.ApplicationSearchVO;
import org.epragati.regservice.vo.CitizenApplicationSearchResponceLimitedVO;
import org.epragati.regservice.vo.CitizenApplicationSearchResponceVO;
import org.epragati.regservice.vo.ClassOfVehicleConversionVO;
import org.epragati.regservice.vo.FuelConversionVO;
import org.epragati.regservice.vo.InputForRePay;
import org.epragati.regservice.vo.MobileApplicationStatusVO;
import org.epragati.regservice.vo.MobileVO;
import org.epragati.regservice.vo.PoliceDepartmentSearchResponceVO;
import org.epragati.regservice.vo.RcValidationVO;
import org.epragati.regservice.vo.RegServiceVO;
import org.epragati.regservice.vo.RegServicesFeedBackVO;
import org.epragati.regservice.vo.SeatConversionVO;
import org.epragati.regservice.vo.TowVO;
import org.epragati.regservice.vo.VcrValidationVo;
import org.epragati.rta.reports.vo.CitizenSearchReportVO;
import org.epragati.tracking.vo.Trackingvo;
import org.epragati.util.RoleEnum;
import org.epragati.util.StatusRegistration;
import org.epragati.util.payment.GatewayTypeEnum;
import org.epragati.vcr.vo.VcrFinalServiceVO;
import org.epragati.vcr.vo.VoluntaryTaxVO;
import org.epragati.vcrImage.dto.VcrFinalServiceDTO;
import org.springframework.data.util.Pair;
import org.springframework.web.multipart.MultipartFile;

/**
 * 
 * @author krishnarjun.pampana
 *
 */
public interface RegistrationService {

	RegServiceVO savingRegistrationServices(String input, MultipartFile[] multipart, String user)
			throws IOException, RcValidationException;

	/**
	 * 
	 * @param rcValidationVO
	 * @return
	 * @throws Exception
	 */
	Optional<RegistrationDetailsVO> getRegistrationDetailByprNoAndAadhaarNo(RcValidationVO rcValidationVO);

	/**
	 * Find registrationDetails based on ApplicationNo
	 * 
	 * @param ApplicationNo
	 * @return
	 */
	Optional<RegServiceVO> findRegistrationDetailsByApplicationNo(String ApplicationNo);

	Optional<RegServiceDTO> findByApplicationNo(String ApplicationNo);

	/**
	 * find registrationDetails based on prNo
	 * 
	 * @param prNo
	 * @return
	 */

	SearchVo searchWithAadharNoAndRc(RcValidationVO rcValidationVO, boolean requestFromSave)
			throws RcValidationException;
	
	SearchVo searchWithOutAadharNoAndRc(RcValidationVO rcValidationVO, boolean requestFromSave)
			throws RcValidationException;
	
	String getAadharNoUsingPrNo(RcValidationVO rcValidationVO);

	RegistrationDetailsDTO getRegDetails(String prNo, String AadharNo, Boolean value);

	TransactionDetailVO getPaymentDetails(RegServiceVO regServiceDetail, Boolean isToPay, String slotDate);

	ClassOfVehiclesVO getClassofVehicle(RegServiceDTO regServiceDetail, RegistrationDetailsDTO regDetails);

	RegServiceDTO getRegServiceDetails(String applicationNo);

	RegServiceVO doRepay(InputForRePay input, UserDTO user, HttpServletRequest request);

	RegServiceVO getRegDetailsForToken(String ApplicationNo);

	TransactionDetailVO getpaymentsForRepay(String applicationNo, String slotDate);

	Optional<RegistrationDetailsVO> findByEngineNo(String EngineNo);

	List<ClassOfVehicleConversionVO> getCovForAlt(String cov, String covType);

	List<BodyTypeVO> getBodyType(String bodyType);

	Optional<RegistrationDetailsVO> findByChassisNoAndEngineNo(String chassisNumber, String engineNumber);

	List<FuelConversionVO> getfuel(String oldFuel, String cov);

	SeatConversionVO getSeats(String cov, String category);

	Optional<RegistrationDetailsVO> getApplicationDetails(ApplicationSearchVO applicationSearchVO);

	StagingRegistrationDetailsDTO getStagingDetailsToken(String applicationNo);

	StagingRegistrationDetailsDTO saveStagingDetails(String slotDate, String applicationNo, boolean ismodifySlot);

	TransactionDetailVO getPaymentDetails(StagingRegistrationDetailsDTO stagingRegistrationDetails);

	StagingRegistrationDetailsDTO getStagingDetailsWithApplicationNo(String applicationNo);

	AlterationVO alterVehicleDetails(String applicationNo);

	RegistrationDetailsVO StagingDetailsForRepay(String applicationNo);

	boolean isTaxOrCessValid(RegistrationDetailsDTO registrationOptional, List<String> taxTypes, LocalDate slotDate);

	// TransactionDetailVO payTaxForTrailers(String
	// applicationNo,GatewayTypeEnum gatewayType);

	// void saveStausforTrailer(String applicationNo);

	// boolean isToPAyTaxForTrailer(String applicationNo, LocalDate slotdate);

	Pair<TransactionDetailVO, Boolean> payTaxForDataEntry(String applicationNo, GatewayTypeEnum gatewayType);

	void saveStausforDateEntry(String applicationNo);

	List<RegServiceDTO> findByChassisNumberAndEngineNumber(String chassisNumber, String engineNumber,
			List<StatusRegistration> applicationStatus);

	RegServiceVO findByRegPrNo(String prNo, String user);

	RegServiceVO getRegServiceDetailsVo(String applicationNo);

	boolean isTopayOtherStateTax(FeeDetailsVO vo);

	RegServiceVO saveTaxForOtherState(String applicationNo);

	void saveCitizenServicesPaymentPending(RegServiceVO regServiceDto, Boolean isRepay);

	RegServiceVO findapplication(String ApplicationNo);

	RegServiceVO findByprNo(String prNo);

	Boolean isToWithSealler(RegServiceVO regServiceDetail);

	RegServiceVO getRegServiceForSearch(ApplicationSearchVO applicationSearchVO);

	/**
	 * 
	 * @param applicationSearchVO getting details from reg services collection
	 * @return Report search vo
	 */
	List<CitizenSearchReportVO> fetchDetailsFromRegistrationServices(ApplicationSearchVO applicationSearchVO);

	/**
	 * 
	 * @param applicationSearchVO getting details from staging and
	 *                            registrationdetails
	 * @return report search vo
	 */
	Optional<CitizenSearchReportVO> fetchDetailsFromStagingAndRegistrationDetails(
			ApplicationSearchVO applicationSearchVO);

	void modifySlot(String slotDate, String applicationNo);

	RegServiceVO getDataForModifySlot(String applicationNo);

	Optional<RegistrationDetailsVO> findByprNoFromRegistrationDetails(String prNo);

	String saveTransactionNo(String applicationNo);

	void alterationValidations(AlterationVO input, RegServiceDTO regServiceDetails, boolean isRequestFormMVI,
			StatusRegistration status);

	RegServiceVO getIssuedNOCData(String applicationNo, String prNo);

	boolean getPUCValidity(String prNo);

	boolean getInsuranceValidity(String prNo);

	public String createTokenForHPA(RegServiceVO regServiceVO);

	boolean regInsuranceValidity(RegistrationDetailsDTO regDeatilsDTO);

	Optional<RegServiceVO> theftObjectionValidation(String prNo);

	RegistrationDetailsVO dotheftObjection(String prNo, String role) throws RcValidationException;

	Optional<RegistrationDetailsVO> applicationSearchForFC(ApplicationSearchVO applicationSearchVO);

	RegServiceDTO saveCitizenServiceDoc(RegServiceVO regServiceVO, RegServiceDTO regServiceDto,
			MultipartFile[] multipart) throws IOException;

	SearchVo applicationSearchForFc(ApplicationSearchVO applicationSearchVO) throws RcValidationException;

	void setMviOfficeDetails(RegServiceDTO regServiceDetails, RegServiceVO regServiceVO);

	Pair<OfficeVO, String> getOffice(Integer mandalId, String vehicleType, String ownerType, String appFormId);

	// RegServiceVO savingRegistrationServicesForFC(String regServiceVO,
	// MultipartFile[] multipart) throws IOException;

	boolean isToPayLateFeeForFC(String applicationNo, String slotDate, Boolean isToPay);

	void getpaymentForRepay(String applicationNo, TransactionDetailVO regServiceDetail, FeeDetailsVO feeDetails);

	boolean isPermitTransferRequired(String mandalId, String prNo);

	Optional<RegistrationDetailsVO> getPermitDetails(String prNo);

	RegistrationDetailsVO applicationSearchForTax(String prNo, String chassisno, Boolean isMobile);

	Boolean regPUCDetailsValidity(RegistrationDetailsDTO registrationDetailsDTO);

	Boolean isTOtokenCanceled(ApplicationSearchVO applicationSearchVO);

	List<String> getTaxTypes(String applicationNo);

	void updatePaidDateAsCreatedDate(List<TaxDetailsDTO> taxDetailsDTO);

	List<CitizenSearchReportVO> fetchDetailsFromRegistrationServicesAtRTA(ApplicationSearchVO applicationSearchVO);

	public List<MobileVO> getDashBoardInfoAndPrNos(String aadharNo);

	public List<MobileApplicationStatusVO> getLatestRecordBasedOnAadharNo(String aadharNo, List<String> prNos);

	List<AadhaarRequestVO> getAdhaarData(String aadharNo);

	List<AadhaarRequestVO> getAllAadhaarData(List<String> aadharNos);

	void freshRcFinanceProcessAtMVI(String rtaActionVO, MultipartFile[] uploadfiles);

	List<CitizenSearchReportVO> fetchDetailsFromRegistrationServicesForMobile(ApplicationSearchVO applicationSearchVO);

	Boolean isOnlineFinance(String userId);

	List<MasterFeedBackQuestionsVO> getFeedBackquestions();

	boolean isFeedBackFormupdated(String applicationNo);

	RegServicesFeedBack saveRegServiceFeedBackFrom(RegServicesFeedBackVO input);

	MasterWeightsForAltVO getweights(String applicationNo);

	boolean isNeedtoAddVariationOfPermit(RegistrationDetailsDTO regServiceDetails);

	Boolean cancelOtherStateApplication(ApplicationSearchVO applicationSearchVO, RoleEnum role, String id);

	RegServiceDTO getPresentAddress(RegServiceVO regServiceVO, RegServiceDTO regServiceDto);

	RegServiceDTO generateHPAToken(RegServiceDTO regSerDTO, String office);

	RegistrationDetailsVO getvehicleDetailsForarvt(String prNo, String chassisNo);

	Optional<CitizenApplicationSearchResponceVO> getApplicationSearchResultForCitizen(
			ApplicationSearchVO applicationSearchVO);

	CitizenApplicationSearchResponceLimitedVO getApplicationSearchResultForCitizenLimtedData(
			Optional<CitizenApplicationSearchResponceVO> citizenVO);

	Optional<RegServiceVO> fetchRegServiceDetails(String applNo);

	List<String> getBiLateralTaxCovs();

	RegServiceVO getvehicleDetailsForBiLateralTax(String prNo, String purpose);

	Boolean saveOtherStateRecord(List<RegServiceDTO> otherStateRecords, Boolean isShedularProcess, Integer days);

	List<CitizenSearchReportVO> getNocPendingApplications(String officeCode);

	VcrDetailsVO verifyOtherStateVCR(VcrValidationVo vcrValidationVo);

	Optional<TaxDetailsDTO> getLatestTaxTransaction(String prNo);

	Boolean checkIsPanrequired(String cov);

	Optional<AadharDetailsResponseVO> getAadhaarResponseService(AadhaarDetailsRequestVO requestModel);

	RegServiceVO saveBilateralTax(String regServiceVO, MultipartFile[] uploadfiles);

	void validationForRlNonadCount(String prNo, String purpose, String recommendationLetterNo, String permitIssuedBy);

	List<ApplicantDetailsVO> activeRepresentative(AadhaarDetailsRequestVO requestModel);

	List<ApplicantDetailsVO> InactiveRepresentative(String parentUidToken, String childAadharNo);

	List<ApplicantDetailsVO> modifyRepresentative(String parentUidToken, String childUidToken,
			AadhaarDetailsRequestVO requestModel);

	boolean isPermitActiveOrNot(String prNo, String vehicleType);

	Optional<AadharDetailsResponseVO> getRepresentativeDetails(String aadhaarNumber);

	AadharDetailsResponseVO getAadharResponse(AadhaarDetailsRequestVO request, AadhaarSourceDTO aadhaarSourceDTO);

	String updateRegServiceDetails(String applicationNo);

	CitizenApplicationSearchResponceVO setRegistrationDetailsIntoResultVO(
			RegistrationDetailsDTO registrationDetailsDTO);

	RegistrationDetailsVO applicationSearchForFeeCorrection(String prNo, String chassisno, Boolean isMobile);

	Long isAllowedForOSTrFancy(LocalDate trValidity);

	CitizenSearchReportVO applicationSearchForVcr(ApplicationSearchVO applicationSearchVO);

	List<VcrFinalServiceDTO> getVcrDetails(List<String> listOfVcrs, boolean requestFromAO,boolean applicationSearchfromMVI);

	RegServiceVO saveVcrDetails(String regServiceVO, MultipartFile[] uploadfiles, JwtUser jwtUser);

	boolean skipAadharValidationForTax();

	void clearTokenRequest();

	Pair<List<VcrFinalServiceDTO>, Integer> getAmount(List<VcrFinalServiceDTO> vcrList, boolean requestFromAO);

	String getTransactionNumber(TransactionDetailVO transactionDetailVO, String applicationNo);

	Optional<InsuranceResponseVO> getInsuranceDetails(String policyNumber);

	String postPRDetails(InsuranceVO vo);

	Boolean postPRDetailsInRegDetals(InsuranceVO vo);

	Boolean confirmationToInsuranceCompany(InsuranceVO vo);

	// RegServiceVO saveOtherStateTPDetails(JwtUser jwtUser, String regServiceVO,
	// MultipartFile[] uploadfiles, HttpServletRequest request);

	void checkonlineVcrDetailsForDataEntry(VcrInputVo vcrInputVo);

	List<String> verifyTheftIntimation(List<String> errors, String prNo, Set<Integer> serviceIds);

	List<String> checkRcsuspendOrcancelled(RegistrationDetailsDTO registrationOptional, List<String> errors);

	List<String> checkObjectionOrTheft(RegistrationDetailsDTO registrationOptional, List<String> errors);

	List<String> verifyNoc(List<String> errors, String prNo, Set<Integer> serviceIds);

	List<String> checkVcrDues(RegistrationDetailsDTO registrationOptional, List<String> errors);

	RegServiceVO saveVoluntaryTax(String voluntaryTaxVO, MultipartFile[] uploadfiles, UserDTO user,
			HttpServletRequest request, boolean requestFromApp);

	RegServiceVO fetchApplicationDetailsForCheckPostServices(ApplicationSearchVO applicationSearchVO);

	Optional<RegServiceVO> rcCancellationValidation(String prNo);

	RegistrationDetailsVO doRcCancellation(String prNo, String role, String officeCode) throws RcValidationException;

	List<VcrFinalServiceDTO> getTotalVcrs(ApplicationSearchVO applicationSearchVO, List<VcrFinalServiceDTO> vcrList);

	void getVcrAmount(ApplicationSearchVO applicationSearchVO, List<VcrFinalServiceDTO> vcrList,
			CitizenSearchReportVO outPut);

	public CitizenSearchReportVO applicationSearchForVcrAfterPayment(ApplicationSearchVO applicationSearchVO);

	public List<VcrFinalServiceDTO> getTotalVcrsAfterPayments(ApplicationSearchVO applicationSearchVO,
			List<VcrFinalServiceDTO> vcrList);

	public List<VcrFinalServiceDTO> getVcrDetailsAfterPayment(List<String> listOfVcrs);
	// As per murthy gaaru inputs commented TR flow for freshRC once murthy gaaru
	// gives approval uncomment code
	/*
	 * RegServiceVO savingRegistrationServicesForFreshRCTrNo(String regServiceVO,
	 * MultipartFile[] multipart, Boolean isTrNo,String user) throws IOException,
	 * RcValidationException;
	 */

	CitizenSearchReportVO applicationSearchForVcrClosed(ApplicationSearchVO applicationSearchVO);

	List<VcrFinalServiceDTO> applicationSearchVcrReleaseOrder(ApplicationSearchVO applicationSearchVO);

	void cancellationOfRegServices(String applicationNumber);

	boolean shouldNotAllowForPayCash(VcrFinalServiceDTO vcdDoc);

	void makingPermitDetailsAsInactive(RegistrationDetailsDTO registrationDetails);

	void savingImagesFromMviApp(String regServiceVO, MultipartFile[] multipart, JwtUser jwtUser);

	List<String> validations(RegistrationDetailsDTO registrationOptional, List<String> errors, String key,
			RcValidationVO rcValidationVO) throws RcValidationException;

	AadhaarSourceDTO setAadhaarSourceDetails(RcValidationVO rcValidationVO);

	SearchVo getSearchResult(RcValidationVO rcValidationVO, RegistrationDetailsDTO registrationDetailsDTO, SearchVo vo);

	RegServiceVO saveStageCarriageServices(String regServiceVO, MultipartFile[] multipart, JwtUser user)
			throws IOException, RcValidationException;

	CitizenFeeDetailsInput getPaymentInputs(TransactionDetailVO regServiceDetail);

	Trackingvo fetchStatusFromRegistrationServices(ApplicationSearchVO applicationSearchVO);

	List<Integer> scrtServices();

	RegServiceDTO returnLatestFcDoc(String prNo);

	void setTaxForPrint(VcrFinalServiceVO vo, TaxHelper taxAndValidity);

	RegServiceVO saveOtherStateTPDetails(JwtUser jwtUser, String regServiceVO, MultipartFile[] uploadfiles,
			HttpServletRequest request, Boolean isRTAWebRequest);

	List<VcrFinalServiceVO> calculateTaxAndTotal(List<VcrFinalServiceDTO> vcrList);

	Pair<Boolean, String> payReceiptDownloadAtCitizen(String applicationNo);

	Optional<CitizenApplicationSearchResponceVO> getVehicleDetailsByPrNoAndAadaharNoForExternalUser(
			ApplicationSearchVO applicationSearchVO);

	Optional<VoluntaryTaxVO> getVoluntaryTaxDetails(ApplicationSearchVO applicationSearchVO,
			HttpServletRequest request);

	Optional<PoliceDepartmentSearchResponceVO> vehicleDetailsSearchWithRegistrationNumber(
			ApplicationSearchVO applicationSearchVO);

	void commonValidationForFc(String user, RegServiceDTO regDTO);

	boolean isallowImagesInapp(String officeCode);

	boolean isFreshStageCarriage(RegServiceVO regServiceDetail);

	RegServiceVO validateAndSaveForFreshStageCarriage(RegServiceVO regServiceDetail);

	Pair<Boolean, String> getEbiddingPaymentDoneOrNot(String trNo, String mobileNo, String selectedPrNo);

	List<AadharDropListVO> aadharDropList(AadharReqServiceIdsVO aadharReqServiceIdsVO);

	RegServiceVO getFCImgsByPrno(String prNo, String user, Boolean isStoppage);

	CitizenSearchReportVO applicationSearchForVcrInMVI(ApplicationSearchVO applicationSearchVO);
	
	Optional<TowVO> towtokendetailsByprNo(String prNo);
	
}
