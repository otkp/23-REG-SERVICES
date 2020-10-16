package org.epragati.reports.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import javax.servlet.http.HttpServletResponse;

import org.epragati.aadhaar.VcrHistoryVO;
import org.epragati.common.vo.MviReportVO;
import org.epragati.dealer.vo.TrIssuedReportVO;
import org.epragati.exception.BadRequestException;
import org.epragati.jwt.JwtUser;
import org.epragati.master.vo.DistrictVO;
import org.epragati.master.vo.FinanceDetailsVO;
import org.epragati.master.vo.UserVO;
import org.epragati.payment.dto.PaymentTransactionDTO;
import org.epragati.payment.report.vo.InvoiceDetailsReportVo;
import org.epragati.payment.report.vo.OsDataEntryFinalVO;
import org.epragati.payment.report.vo.RegReportDuplicateVO;
import org.epragati.payment.report.vo.RegReportVO;
import org.epragati.payment.report.vo.ReportsVO;
import org.epragati.permits.dto.FitnessReportsDemoVO;
import org.epragati.regservice.vo.RegServiceVO;
import org.epragati.regservice.vo.TaxDetailsVO;
import org.epragati.reports.dto.ReportsRoleConfigDTO;
import org.epragati.rta.reports.vo.ActionCountDetailsVO;
import org.epragati.rta.reports.vo.CCOReportVO;
import org.epragati.rta.reports.vo.CitizenEnclosuresVO;
import org.epragati.rta.reports.vo.DealerReportVO;
import org.epragati.rta.reports.vo.EODReportVO;
import org.epragati.rta.reports.vo.FitnessReportVO;
import org.epragati.rta.reports.vo.FreshRCReportVO;
import org.epragati.rta.reports.vo.PageDataVo;
import org.epragati.rta.reports.vo.PermitHistoryDeatilsVO;
import org.epragati.rta.reports.vo.ReportInputVO;
import org.epragati.rta.reports.vo.StagingRejectedListVO;
import org.epragati.rta.reports.vo.StoppageReportVO;
import org.epragati.rta.reports.vo.TrReportTotalsVO;
import org.epragati.rta.reports.vo.UsersDropDownVO;
import org.epragati.vcr.vo.VcrFinalServiceVO;
import org.epragati.vcr.vo.VcrUnpaidResultVo;
import org.epragati.vcr.vo.VcrVo;
import org.springframework.data.domain.Pageable;

public interface PaymentReportService {

	/**
	 * 
	 * @param pagable
	 * @return
	 */

	List<RegReportVO> getDistrictReports(RegReportVO paymentreportVO);

	Optional<ReportsVO> getpaymenttransactions(RegReportVO paymentReportVO, Pageable pagable);

	Boolean paymentDetailsExcel(HttpServletResponse response, RegReportVO paymentReportVO);

	List<RegReportVO> getPaymentsReport(List<PaymentTransactionDTO> paymentsList, RegReportVO paymentReportVO);

	Optional<ReportsRoleConfigDTO> getReportsConfig(String role);

	Boolean verfiyRoleAccess(String role, String reportType);

	void verifyUserAccess(JwtUser jwtuser, String reportType);

	// RegReportVO vehicleStrengthReport(RegReportVO paymentReportVO);

	List<RegReportVO> statePermitReport(RegReportVO vo, JwtUser jwtUser);

	List<RegReportVO> distPermitReport(RegReportVO vo);

	// List<RegReportVO> suspensionStateCount(RegReportVO regReportVO);

	// Map<String, Object> findAllDetails(RegReportVO regReportVO, Pageable page);

	Optional<List<CCOReportVO>> getCCOApprovedRejectCount(String officeCode, ReportInputVO inputVO);

	List<RegReportVO> suspensionDistCount(RegReportVO regReportVO);

	List<DealerReportVO> getDealerReport(JwtUser jwtUser, DealerReportVO dealerVO, Pageable page);

	List<RegReportVO> findAllDetails(RegReportVO regReportVO, Pageable page);

	List<RegReportVO> getPermitDetails(RegReportVO paymentreportVO, Pageable pageble);

	void districtvalidate(JwtUser jwtuser, RegReportVO regVO);

	void districtvalidate(JwtUser jwtUser, VcrVo reportVO);

	LocalDateTime getTimewithDate(LocalDate date, Boolean timeZone);

	VcrVo covEnforcementReport(String user, String officeCode, VcrVo vcrVO);

	Map<Integer, List<String>> getOfficeCodes();

	Map<String, String> getOfficeCodesByOfficeName();

	List<FinanceDetailsVO> getFinancierReport(JwtUser jwtUser, FinanceDetailsVO financeDetailsVO);

	List<RegReportVO> suspensionStateCount(RegReportVO regReportVO, JwtUser jwtUser);

	List<RegReportVO> getData(String userId, String officeCode, RegReportVO regVO, Pageable page);

	List<RegReportVO> getPermitData(String officeCode, RegReportVO regVO);

	List<DistrictVO> getDistByOfc(String officeCode);

	List<String> getMviForDist(Integer districtId);

	PageDataVo getEodReportList(JwtUser jwtUser, EODReportVO eodVO, Pageable pagable) throws BadRequestException;

	RegServiceVO getAllData(String applicationNo);

	List<ActionCountDetailsVO> getEodReportCount(JwtUser jwtUser, EODReportVO eodVO);

	ActionCountDetailsVO eodReportForDept(JwtUser jwtUser, EODReportVO eodVO, Pageable pagable);

	List<String> getEodReportsDropDown(String roleType, boolean serviceStatus, boolean module);

	RegReportVO vehicleStrengthReport(RegReportVO RegReportVO, JwtUser jwtUser);

	RegReportVO setReportData(RegReportVO RegReportVO, Map<String, Double> map);

	List<Integer> getGatewayTypes(Integer gateWayValue);

	List<UsersDropDownVO> getEodRolesList(boolean value, String selectedRole, JwtUser jwtUser);

	List<TaxDetailsVO> getTaxReport(String prNo);

	List<FitnessReportsDemoVO> getFitnessData(JwtUser jwtUser, RegReportDuplicateVO regVO, String actionUserName,
			Pageable page, String officeCode);

	List<RegReportVO> PaymentReportData(RegReportVO regReportVO, Pageable pagable);

	List<RegReportVO> revenueDetailedData(List<PaymentTransactionDTO> paymentsList, RegReportVO RegReportVO);
	public Boolean getExcel(HttpServletResponse response, RegReportVO regVO, String reportName, String userId,
			String officeCode, List<InvoiceDetailsReportVo> invoiceReport, Pageable page) throws Exception;

	Optional<ReportsVO> getaadharSeedIngApprovalsReport(RegReportVO regReportVO, Pageable pagable);

	Optional<ReportsVO> getaadharSeedIngOfficeViewReport(RegReportVO regReportVO, Pageable pagable);

	Optional<ReportsVO> getaadharSeedIngApprovalsForDTCReport(RegReportVO regReportVO, Pageable pagable);

	Optional<ReportsVO> getaadharSeedIngAppllicationViewReport(RegReportVO regReportVO, Pageable pagable);

	List<UserVO> getMviNameForDist(Integer districtId);
	
	TrIssuedReportVO getDealerTrIssuedReport(LocalDate fromDate,LocalDate toDate,JwtUser jwtuser,Pageable page);

	void generateExcelForPermitReports(List<RegReportVO> paymentReportVO, HttpServletResponse response);
	
	void generateExcelForPermitReportsData(List<RegReportVO> paymentReportVO, HttpServletResponse response);
	
	List<StoppageReportVO> fetchVehicleStoppageData(String officeCode, RegReportVO regVO);
	
	TrReportTotalsVO getTotalOfficeWise(LocalDate fromDate, LocalDate toDate, JwtUser jwtuser,String reportName);
	
	TrIssuedReportVO getTrDetailsBasedOnVehicleType(String vehicleType, JwtUser userDetails, LocalDate fromDate,
			LocalDate toDate,String reportFlag,Pageable page);

	List<CitizenEnclosuresVO> getCitizenEnclosures(CitizenEnclosuresVO citizenEnclosuresVO);

	List<CitizenEnclosuresVO> getCitizenServices(CitizenEnclosuresVO citizenEnclosuresVO);

	void generateExcelForRcSuspensionReport(List<RegReportVO> regReport, HttpServletResponse response,
			RegReportVO regReportVO);
	
	Map<String, Long> fetchEvcrReportDistrictWise(String officeCode, RegReportVO regVO);

	List<VcrFinalServiceVO> fetchEvcrReportDistrictWiseList(String officeCode, RegReportVO regVO);
	
	TrIssuedReportVO contractCarriagePermitReport(RegReportVO regVO, Pageable page);

	Object getCitizenServicesForQueryScreen(CitizenEnclosuresVO citizenEnclosuresVO);

	
	void generateExcelForTrDetails(String vehicleType, JwtUser userDetails, LocalDate fromDate, LocalDate toDate,
			HttpServletResponse response, String reportFlag);
	
	List<FreshRCReportVO> getFreshRCReportVO(String officeCode, RegReportVO regVO);

	RegReportVO issueOfNocDistrictWiseList(JwtUser jwtUser, RegReportVO regVO);

	List<RegReportVO> fetchIssueOfNocDetails(JwtUser jwtUser, RegReportVO regVO);

	RegReportVO getDistrictwiseDealerOrFinancierDetails(JwtUser jwtUser, RegReportVO regVO, Pageable page);

	Object getFitnessDetails(FitnessReportVO fitnessReportVO);

	Object getDetailsForMaining(String upperCase);

	Object getPermitHistory(PermitHistoryDeatilsVO permitHistoryDeatilsVO);
	
	OsDataEntryFinalVO reportForOtherStateVehiclesDataEntry(String fromDate,String toDate,String officeCode);
	
	void generateExcelForDispatchFormSubmission(HttpServletResponse response, String fromDate, String toDate,
			String officeCode, String name);

	Object getVcrDetails(VcrHistoryVO vcrHistoryVO);

	RegReportVO getVcrDistrictWiseCount(RegReportVO regReportVO) throws ExecutionException, InterruptedException;

	RegReportVO getVcrAllDistricts(RegReportVO regReportVO);
	
	RegReportVO getMviVcrCount(RegReportVO regReportVO) throws InterruptedException, ExecutionException;

	List<VcrFinalServiceVO> getPaidVcrListBymviwise(RegReportVO regReportVO);

	Object getRejectionList(String upperCase);
	VcrUnpaidResultVo getVcrUnpaidedCountOfficewise(RegReportVO regReportVO,JwtUser jwtUser);
	VcrUnpaidResultVo getVcrDetailedListOfficeWise(RegReportVO regReportVO);

	List<StagingRejectedListVO> getStagingPendingReport(String upperCase, String officeCode);

	List<StoppageReportVO> fetchVehicleStoppagerevocationData(String officeCode, RegReportVO regVO);
}
