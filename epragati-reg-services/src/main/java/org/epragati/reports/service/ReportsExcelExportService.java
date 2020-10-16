package org.epragati.reports.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.epragati.dealer.vo.TrIssuedReportVO;
import org.epragati.payment.report.vo.RegReportDuplicateVO;
import org.epragati.payment.report.vo.RegReportVO;
import org.epragati.payment.report.vo.ReportsVO;
import org.epragati.permits.dto.FitnessReportsDemoVO;
import org.epragati.rta.reports.vo.ActionCountDetailsVO;
import org.epragati.rta.reports.vo.CheckPostReportsVO;
import org.epragati.rta.reports.vo.EODReportVO;
import org.epragati.rta.reports.vo.ReportVO;
import org.epragati.rta.reports.vo.VcrCovAndOffenceBasedReportVO;
import org.epragati.vcr.vo.VcrFinalServiceVO;
import org.epragati.vcr.vo.VcrVo;

public interface ReportsExcelExportService {

	void excelReportsForEod(HttpServletResponse response, ActionCountDetailsVO report, EODReportVO eodVO);

	void generateExcelForContractCarriage(TrIssuedReportVO resultList, HttpServletResponse response, RegReportVO regVO);
	
	void getRoadSafetyVcrDistrictCountExcel(HttpServletResponse response, ReportVO report);

	void generateExcelForOffenceWiseVcrReport(VcrCovAndOffenceBasedReportVO offenceResult, RegReportVO paymentReportVO,
			HttpServletResponse response);

	void generateExcelForCovWiseVcrReport(VcrCovAndOffenceBasedReportVO covWiseVcrResult, RegReportVO paymentReportVO,
			HttpServletResponse response);

	void generateExcelForPaymentCheckPostReport(List<CheckPostReportsVO> checkpost, RegReportVO paymentReportVO,
			HttpServletResponse response);

	void generateNonPaymentReportCountExcel(HttpServletResponse response, Optional<ReportsVO> paymentsReport,
			RegReportVO regReportVO);

	void generateNonPaymentReportVehicleDataExcelCount(Optional<ReportsVO> paymentsReport, HttpServletResponse response,
			RegReportVO regReportVO);

	void generateNonPaymentReportCovWiseCountExcel(HttpServletResponse response, Optional<ReportsVO> paymentsReport,
			RegReportVO regReportVO);

	void generateExcelForEvcrDetailReports(Map<String, Long> regReport, RegReportVO regVO, HttpServletResponse response);

	void generateExcelForFitnessData(HttpServletResponse response, List<FitnessReportsDemoVO> fitnessReport,
			RegReportDuplicateVO regVO);

	void generateExcelForEvcrDetailReportsData(List<VcrFinalServiceVO> regReport, RegReportVO regVO,
			HttpServletResponse response);

	void generateExcelForVcrPaymentPaidDate(RegReportVO report, HttpServletResponse response, VcrVo reportVO);
	
}
