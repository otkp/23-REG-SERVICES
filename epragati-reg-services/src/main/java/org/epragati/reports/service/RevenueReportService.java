package org.epragati.reports.service;

import java.util.List;

import org.epragati.payment.report.vo.RegReportVO;

public interface RevenueReportService {

	void breakupsGroupByOffice(RegReportVO paymentreportVO);

	List<RegReportVO> getPaymentsReportCount(RegReportVO RegReportVO);

}
