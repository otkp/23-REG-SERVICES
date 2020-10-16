package org.epragati.vcr.service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.epragati.cfstVcr.vo.VcrInputVo;
import org.epragati.dealer.vo.TrIssuedReportVO;
import org.epragati.exception.ResourceNotFoudException;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.vo.DistrictVO;
import org.epragati.master.vo.MasterCovVO;
import org.epragati.master.vo.RegistrationDetailsVO;
import org.epragati.master.vo.StateVO;
import org.epragati.master.vo.VCRVahanVehicleDetailsVO;
import org.epragati.master.vo.VcrGoodsVO;
import org.epragati.payment.report.vo.RegReportVO;
import org.epragati.payment.report.vo.ReportsVO;
import org.epragati.permits.vo.PermitUtilizationVO;
import org.epragati.regservice.vo.ApplicationSearchVO;
import org.epragati.regservice.vo.RcValidationVO;
import org.epragati.regservice.vo.RegServiceVO;
import org.epragati.regservice.vo.SpeedGunVO;
import org.epragati.rta.reports.vo.CitizenSearchReportVO;
import org.epragati.rta.reports.vo.ReportVO;
import org.epragati.vcr.service.impl.VehicleDetailsAndPermitDetailsVO;
import org.epragati.vcr.vo.MasterOffenceSectionsVO;
import org.epragati.vcr.vo.MasterOffendingSectionsVO;
import org.epragati.vcr.vo.MasterPenalSectionsVO;
import org.epragati.vcr.vo.VcrFinalServiceVO;
import org.epragati.vcr.vo.VcrImageVO;
import org.epragati.vcr.vo.VcrVo;
import org.epragati.vcrImage.dto.VcrFinalServiceDTO;
import org.epragati.vcr_dl.vo.DlDetailsVO;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

public interface VcrService {

	List<MasterCovVO> getAllCovs();

	List<StateVO> getAllStates();

	List<DistrictVO> getAllDistrict(String stateId);

	/**
	 * this service is used for getting vehicleDetails based on prNo.
	 * 
	 * @param prNo
	 * @return
	 */
	public Optional<RegistrationDetailsVO> getVehicleDetailsByPrNO(String prNo) throws ResourceNotFoudException;

	/**
	 * @throws ResourceNotFoudException 
	 * 
	 */
	public Optional<RegistrationDetailsVO> getVehicleDetailsByChessisNo(String chessisNo) throws ResourceNotFoudException;

	/**
	 * 
	 * @return
	 */
	public List<VcrGoodsVO> getAllGoodsDescription();

	/**
	 * this service is used to save image
	 * 
	 * @param vcrImeageVO
	 * @return
	 */
	public void imageSave(VcrImageVO vcrImeageVO) throws Exception;

	public Optional<Map<String, String>> getSaveDetailsByVcrNO(String vcrNo) throws IOException, ResourceNotFoudException;

	public String getImage(String appImageDocId) throws IOException, ResourceNotFoudException;

	public VcrFinalServiceVO saveVcrDetails(String finalServiceVO, MultipartFile[] file, JwtUser jwtUser)
			throws ResourceNotFoudException, Exception;

	public Optional<VcrFinalServiceDTO> getVcrDetailsByVcrNumber(String vcrNumber);

	public List<VcrFinalServiceDTO> getVcrDetailsByRegNumber(String regNumber);

	public List<VcrFinalServiceVO> getVcrDetailsPageableByRegNumber(String regNumber);

	List<DlDetailsVO> getDriverDetilsByDlNo(String dlNo)
			throws JsonParseException, JsonMappingException, IOException, Exception;

	VehicleDetailsAndPermitDetailsVO getVehicleDetailsAndPeermitDetails(String prNo) throws ResourceNotFoudException;

	public List<VcrFinalServiceDTO> getVcrDetailsByVcrNumberList(String vcrNumber);

	
	RegReportVO vcrReport(VcrVo vo, JwtUser jwtuser, Pageable page);

	Optional<RegistrationDetailsVO> getVehicleDetailsAndPermitDetailsByTrNumber(String trNo) throws ResourceNotFoudException;

	VehicleDetailsAndPermitDetailsVO getVehicleDetailsAndPermitDetailsByPrOrTrOrChessisNumber(String prNo,
			String chessisNumber, String trNo) throws ResourceNotFoudException;

	String getOfficeNameBasedOnOfficeCode(String officeCode);
	
	List<VcrFinalServiceVO> getVcrDetailsByPrOrTrOrChessisNumberOrVcrNumberAndUserName(String prNo,
			String chessisNumber, String trNo, String vcrNumber, String username);
	
	Boolean payPeriodBasedonCOV(String cov, String seatingCapacity, String gvw);

	

	LocalDateTime getTimewithDate(LocalDate date, Boolean timeZone);

	RegistrationDetailsVO getDataForFromStaging(VcrInputVo vcrInputVo);

	List<String> getTaxTypeForVoluntaryTax(String cov, String seatingCapacity, Integer gvw,boolean nocIssuedRC,boolean withTP,boolean home,boolean otherStateRegister
			,boolean vehicleHaveAitp);

	LocalDate getVoluntaryTaxValidity(String taxType, LocalDate prGenerationDate);

	RegServiceVO saveVoluntaryTax(String voluntaryTaxVO, MultipartFile[] uploadfiles, UserDTO user,HttpServletRequest request);

	VcrFinalServiceVO amountForPreview(VcrFinalServiceVO vo);

	String saveVehiclesFreeze(String prNo, JwtUser jwtuser, boolean freezeVehicle);

	List<String> getVehiclesFreeze(JwtUser jwtuser);

	RegServiceVO getRegServicesForApp(RcValidationVO rcValidationVO);
	
	VcrFinalServiceVO saveVcrDeleteDetails(String finalServiceVO, MultipartFile[] file, JwtUser jwtUser)
			throws Exception;
	
	CitizenSearchReportVO periodOfTimeVcrList(ApplicationSearchVO vo, JwtUser jwtUser);
	VCRVahanVehicleDetailsVO getVahanDataForApp(RcValidationVO rcValidationVO);

	List<MasterOffenceSectionsVO> getOffenceSections();

	List<MasterOffendingSectionsVO> getOffendingSection();

	List<MasterPenalSectionsVO> getPenalSections();

	List<RegReportVO> getCountAndOfficeViceVcrData(VcrVo vo, JwtUser jwtuser);

	List<RegReportVO> getVcrPaidCountForOfficeVice(VcrVo vo, JwtUser jwtuser);

	ReportsVO getDistrictViceVcrCount(VcrVo vo, JwtUser jwtuser) throws Exception;

	void generateDistrictViceVcrCountExcelReport(HttpServletResponse response, ReportsVO report, String officeCode) throws Exception;

	void generateExcelListforMVINames(HttpServletResponse response, ReportsVO report) throws Exception;

	TrIssuedReportVO getEvcrReport(LocalDate fromDate, LocalDate toDate, String vcrNumber, JwtUser jwtUser,String reportName,
			Pageable page);

	void saveEvcrPrintedRecords(JwtUser jwtUser, List<String> printedRecords,String reportName);

	void generateVcrPaymentReportsExcelList(List<RegReportVO> report, HttpServletResponse response);

	void generateExcelListForVcrpaymentReportOfficeData(List<RegReportVO> report, HttpServletResponse response);

	ReportVO getRoadSafetyVcrCount(VcrVo vo, JwtUser jwtuser) throws Exception;

	ReportVO getRoadSafetyVcrDistrictCount(VcrVo vo, JwtUser jwtuser) throws Exception;

	void generateExcelForRoadSafetyMviCount(ReportVO report, HttpServletResponse response);

	void generateExcelForRoadSafetyVcrCount(ReportVO report, HttpServletResponse response);

	RegServiceVO getTPDataForIncomeAndOutGoing(String prNo);

	PermitUtilizationVO saveTpUtilizationDetails(JwtUser jwtUser, PermitUtilizationVO vo);
	
    void saveSpeedGunData(SpeedGunVO speedGunVO);
 	
	String generateSpeedGunVcr(String imageId ,String officeId); 

	RegReportVO vcrPaidReport(VcrVo reportVO, JwtUser jwtUser, Pageable page);

	
}
