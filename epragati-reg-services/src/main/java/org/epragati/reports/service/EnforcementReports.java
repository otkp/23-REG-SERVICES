package org.epragati.reports.service;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.epragati.payment.report.vo.ReportsVO;
import org.epragati.vcr.vo.VcrFinalServiceVO;
import org.epragati.vcr.vo.VcrVo;

public interface EnforcementReports {

	ReportsVO offenceEnforcementReport(String user, String officeCode, VcrVo vcrVO);

	List<VcrFinalServiceVO> seizedEnforcementReport(String user, String officeCode, VcrVo vcrVO);

	void generateOffenceEnforcementReportExcel(ReportsVO vcrVO, HttpServletResponse response);

}
