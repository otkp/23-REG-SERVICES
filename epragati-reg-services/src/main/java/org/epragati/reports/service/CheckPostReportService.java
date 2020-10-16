package org.epragati.reports.service;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.epragati.payment.report.vo.RegReportVO;
import org.epragati.rta.reports.vo.CheckPostReportsVO;
import org.epragati.rta.reports.vo.CheckpostTotalVO;
import org.epragati.rta.reports.vo.VcrCovAndOffenceBasedReportVO;
import org.springframework.data.domain.Pageable;

public interface CheckPostReportService {

	List<CheckpostTotalVO> checkPostBased(RegReportVO regReportVO);

	public CheckpostTotalVO report3rdPage(RegReportVO regReportVO,Pageable page) throws Exception;
	
	List<CheckPostReportsVO> getCheckPostReport(RegReportVO regReportVO);
	
	VcrCovAndOffenceBasedReportVO covWisevcrReport(RegReportVO paymentReportVO);

	VcrCovAndOffenceBasedReportVO offenseWisevcrReport(RegReportVO paymentReportVO);

	VcrCovAndOffenceBasedReportVO offenseWiseReportForEvcr(RegReportVO paymentReportVO);

	VcrCovAndOffenceBasedReportVO covWiseReportForEvcr(RegReportVO paymentReportVO);

}
