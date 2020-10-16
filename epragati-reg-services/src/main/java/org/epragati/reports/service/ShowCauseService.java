package org.epragati.reports.service;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.epragati.payment.report.vo.ShowCauseReportVo;

public interface ShowCauseService {
	public List<ShowCauseReportVo> getShowCauseReport(ShowCauseReportVo input, String officeCode);

	void generateShowCauseExcelReport(HttpServletResponse response, ShowCauseReportVo input, String officeCode);
}
