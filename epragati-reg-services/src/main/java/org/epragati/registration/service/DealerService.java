package org.epragati.registration.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.epragati.common.vo.PropertiesVO;
import org.epragati.common.vo.SpecialNumberDetailsReport;
import org.epragati.dealer.tradecert.DealerTradeCertificateNewVO;
import org.epragati.dealer.tradecert.TradeCertificateDealerDto;
import org.epragati.dealer.tradecert.TradeCertificateDealerVO;
import org.epragati.dealer.vo.RequestDetailsVO;
import org.epragati.dealer.vo.ResponseDetailsVO;
import org.epragati.images.vo.ImageInput;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dto.RoleActionDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.vo.DashBoardCountVO;
import org.epragati.master.vo.DealerDetailsVO;
import org.epragati.master.vo.DealerRegVO;
import org.epragati.master.vo.EnclosuresVO;
import org.epragati.master.vo.MasterCovVO;
import org.epragati.master.vo.RegistrationDetailsVO;
import org.epragati.master.vo.RejectionHistoryVO;
import org.epragati.master.vo.VahanDetailsVO;
import org.epragati.payment.report.vo.ReportsVO;
import org.epragati.payments.vo.FeesVO;
import org.epragati.regservice.dto.TrailerDetailsDTO;
import org.epragati.regservice.vo.RegServiceVO;
import org.epragati.service.enclosure.vo.CitizenImagesInput;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.google.zxing.NotFoundException;

public interface DealerService {

	Optional<RegistrationDetailsVO> getRegistrationDetailById(String id);

	Map<String, DashBoardCountVO> getDashBoardDetails(String dealerId);

	List<RegistrationDetailsVO> getRegistrationDetailsByStage(String stageId, String dealerId);

	RegistrationDetailsVO saveRegistrationDetails(RegistrationDetailsVO registrationDetails);

	boolean deleteRegistrationDetailById(String id, String userId);

	Optional<RegistrationDetailsVO> getRegistrationDetailByApplicationNo(String appNo);

	void doAction(RegistrationDetailsVO registrationDetailsVO);

	public Map<Integer, List<RoleActionDTO>> updateFlowDetailsForRejection(StagingRegistrationDetailsDTO stagingDTO);

	/**
	 * 
	 * @param applicationNo
	 * @return
	 */
	String createToken(String applicationNo, Integer quotationValue);

	/**
	 * 
	 * @param applicationNo
	 * @return
	 */
	void cancelToken(String applicationNo);

	/**
	 * 
	 * @param applicationNo
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws NotFoundException
	 */
	String getQRCode(String applicationNo) throws FileNotFoundException, IOException, NotFoundException;

	boolean validateUserValidity(Optional<UserDTO> userDetails);

	void updatePaymentStatusOfApplicant(Optional<StagingRegistrationDetailsDTO> registrationDetails,
			String transactionNo, String status);

	Optional<RegistrationDetailsVO> getRegistrationDetailsBasedOnTroraadhaarNo(RequestDetailsVO requestDetailsVO,
			String userId);

	void updatePaymentStatusOfSecondOrInvalidTax(Optional<StagingRegistrationDetailsDTO> registrationDetails,
			String transactionNo);

	Boolean updateReuploadEnclosureFronDealer(RequestDetailsVO requestDetailsVO, String userId);

	ResponseDetailsVO getSVSStatus(String applicationNo, String userId);

	Boolean updateVahanDetails(RequestDetailsVO requestDetailsVO, String userId);

	Optional<VahanDetailsVO> getCOVByVahanDetails(String engineNo, String chasisNo, String userId);
	
	Optional<VahanDetailsVO> getClassOfVehicleByVahanDetails(String engineNo, String chasisNo);

	RegServiceVO saveRegServiceForOther(String regServiceVO, MultipartFile[] uploadfiles);

	TrailerDetailsDTO getTrailerChasisNo(String userId, StagingRegistrationDetailsDTO stagingDTO);

	void saveImages(String trNo, List<ImageInput> images, MultipartFile[] uploadfiles) throws IOException;

	SpecialNumberDetailsReport getSpecialNumberDetailsForStagingDetails(StagingRegistrationDetailsDTO stagingDetails);

	String getTransactionId();

	List<RejectionHistoryVO> viewSecondVehicleData(String applicationNo);

	void updatePaymentTransactionNoFoFailedTransactions(Optional<StagingRegistrationDetailsDTO> registrationDetails,
			String transactionNo);

	DealerDetailsVO saveDealerMgmtDetails(String dealerDetailsVO, MultipartFile[] uploadfiles);

	Boolean telaganaVechicle(String applicationNo);

	void vahanValidationInStagingForOtherSate(String engineNo, String chassisNo);

	PropertiesVO getAddressDropDown(String ownerType);

	boolean checkApprovalsNeedorNot(StagingRegistrationDetailsDTO stagingDTO);

	void saveEnclosures(StagingRegistrationDetailsDTO registrationDetails, List<ImageInput> images,
			MultipartFile[] uploadfiles) throws IOException;

	PropertiesVO getDeclartionDetails();

	List<EnclosuresVO> getListOfSupportedEnclosers(CitizenImagesInput input);

	// *****************************************************************************************
	// TRADE CERTIFICATE service
	// *****************************************************************************************

	public String chkDelerElgibility(String user) throws Exception;

	/**
	 * service for getting COV based on rid user.
	 * 
	 * @param user
	 * @return
	 */
	public List<MasterCovVO> getCov(String user);

	public DealerTradeCertificateNewVO getFeesForTradeCert(DealerTradeCertificateNewVO tradeVo,Integer serviceId) throws Exception;

	/**
	 * service for apply new Trade certificate.
	 * 
	 * @param vo
	 * @param user
	 * @return
	 */
	public DealerRegVO applyNewTradeCertificate(DealerTradeCertificateNewVO vo, JwtUser user);

	public void getUpdateAfterPaymentSuccess(List<TradeCertificateDealerDto> dto, String applicationNo, String status);

	/**
	 * service for getting list of trade certificate associated with the Dealer.
	 * 
	 * @param user
	 * @return
	 */
	public Optional<ReportsVO> getTradeCertificateDetails(JwtUser user, Pageable pagable, String status);

	/**
	 * service for generating pdf. once payment success.
	 * 
	 * @param tradeVo
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public Map<String, Object> generatePdf(String tcGenId, JwtUser user) throws Exception;

	/**
	 * service for getting trade certificate id based of common Trade Ct. id after
	 * payment success.
	 * 
	 * @param user
	 * @param commonId
	 * @return
	 */
	public List<String> getTcIdByTcCommon(JwtUser user, String commonId);

	/**
	 * apply for renewal trade Ct.
	 * 
	 * @param vo
	 * @param user
	 * @return
	 */

	public DealerRegVO applyForRenewalForTradeCt(DealerTradeCertificateNewVO vo, JwtUser user,String duplicateApply);

	/**
	 * service for checking days
	 * 
	 * @param user
	 */
	public void validityChkForTrade(JwtUser user);

	public String chkCov(JwtUser user, String cov);

	/**
	 * Service for repay
	 * 
	 * @param vo
	 * @param user
	 * @return
	 * @throws Exception
	 */
	public DealerRegVO repayForTc(DealerTradeCertificateNewVO vo, JwtUser user) throws Exception;

	public Optional<TradeCertificateDealerVO> getTcDetails(String id, JwtUser user);

}