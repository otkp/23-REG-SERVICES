package org.epragati.vcr.service;

import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.epragati.payment.report.vo.RegReportVO;
import org.epragati.payment.report.vo.ReportsVO;
import org.epragati.regservice.vo.ApplicationSearchVO;
import org.epragati.rta.reports.vo.UsersDropDownVO;
import org.springframework.data.domain.Pageable;

public interface VcrNonPaymentReport {

	Optional<ReportsVO> vcrNonPaymentReport(RegReportVO regReportVO, Pageable pagable);

	void generateShowCauseNoForVcr(ApplicationSearchVO applicationSearchVO, String officeCode, String user,String role);

	Optional<ReportsVO> getShowCauseNoDetailsExistingForVcr(RegReportVO regReportVO, Pageable pagable);

	List<UsersDropDownVO> getVcrNonPaymentRolesDropDown(String officeCode);

	Optional<ReportsVO> getRegServicesAutoapprovalsDetails(RegReportVO regReportVO, Pageable pagable);

	Optional<ReportsVO> getRegOfficeAndDistAutoapprovalsDetails(RegReportVO regReportVO, Pageable pagable);

	Optional<ReportsVO> getExcelReportByAutoapprovals( HttpServletResponse response,RegReportVO regReportVO, Pageable pagable);

}
