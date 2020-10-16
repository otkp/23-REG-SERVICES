package org.epragati.reports.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpResponse;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dto.NonPaymentDetailsDTO;
import org.epragati.master.dto.ShowCauseDetailsDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.payment.report.vo.NonPaymentDetailsVO;
import org.epragati.payment.report.vo.RegReportVO;
import org.epragati.payment.report.vo.ReportsVO;
import org.epragati.payment.report.vo.ShowCauseDetailsVO;
import org.epragati.regservice.vo.ApplicationSearchVO;
import org.epragati.rta.reports.vo.ReportVO;
import org.springframework.data.domain.Pageable;

/**
 * 
 * @author saroj.sahoo
 *
 */
public interface RCCancellationService {

	List<String> getMandalDetails(JwtUser jwtUser);
	
	List<String> getClassOfVehicles();
	
	Optional<ReportsVO> getNonPaymentsReport(RegReportVO regReportVO, JwtUser jwtUser, Pageable pagable);
	
	Optional<ReportsVO> classOfVehicleNonPaymentReportCount(RegReportVO regReportVO, JwtUser jwtUser);

	Optional<ReportsVO> classOfVehicleNonPaymentReport(RegReportVO regReportVO, JwtUser jwtUser, Pageable pagable);


	List<String> getSectionDetails();

	
	void generateShowCauseNo(ApplicationSearchVO applicationSearchVO, String officeCode,String userId);
	
	Optional<ReportsVO> getShowCauseNoDetailsExisting(String officecode,RegReportVO regReportVO,Pageable pagable);

	Optional<ReportsVO> nonPaymentReportForCovCount(RegReportVO regReportVO, String officeCode);

	Optional<ReportsVO> nonPaymentReportForCov(RegReportVO regReportVO, String officeCode, Pageable pagable);
	
	Optional<NonPaymentDetailsVO> showCausePrint(ApplicationSearchVO applicationSearchVO,String officeCode);

	List<NonPaymentDetailsVO> getShowCauseNoDetailsBetweenDates(NonPaymentDetailsVO nonPaymentDetailsVO,String officeCode);

	List<String> getClassOfVehiclesDesc();

	Optional<NonPaymentDetailsVO> showCauseDisplay(ApplicationSearchVO applicationSearchVO, String officeCode);

	Optional<ReportsVO> getDistrictCountReport(RegReportVO regReportVO);

	Optional<ReportsVO> getOfficeCountReport(RegReportVO regReportVO,String role,String userId);

	Map<Integer, String> getDistricIdByDistricName();

	Map<String, Integer> getOfficeCodeByDistricId();

	Map<String, String> getCovCodeByCovName();

	void nonPaymentMoveToHistory(TaxDetailsDTO taxDetailsDTO);

	Optional<ReportsVO> getMandalCountReport(RegReportVO regReportVO, String officeCode);

	Optional<ReportsVO> nonPaymentReportMandalWiseForCovCount(RegReportVO regReportVO, String officeCode);

	Optional<ReportsVO> nonPaymentMandalWiseReportForCovCount(RegReportVO regReportVO, String officeCode);

	Optional<ReportsVO> nonPaymentMandalWiseReportForCov(RegReportVO regReportVO, String officeCode, Pageable pagable);

	void getNonPaymentTaxDetails();

	List<NonPaymentDetailsDTO> getNonPymentDetailsExcle(RegReportVO regReportVO);

	String setCovWithWeight(String cov, RegReportVO regReportVO);

}
