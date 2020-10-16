package org.epragati.reports.service;

import java.time.LocalDate;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.epragati.jwt.JwtUser;
import org.epragati.rta.reports.vo.OfficePaymentVO;
import org.epragati.rta.reports.vo.RevenueReportVO;
import org.epragati.rta.vo.ReportResponseVO;
import org.epragati.rta.vo.ReportVO;
import org.epragati.sn.vo.SNReportResultVO;
import org.epragati.sn.vo.SPNumberPaymentDetailsVO;

public interface RtaReportService {

	List<ReportResponseVO> getEbidingDetails(ReportVO reportVO, JwtUser user);

	ReportResponseVO getSpecialNumberDetailsById(String id);

	SPNumberPaymentDetailsVO spNumberPaymentDetails(String paymentId);
	/**
	 *  
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	RevenueReportVO getDistrictWiseReport(LocalDate fromDate,LocalDate toDate); 
	/**
	 * 
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	RevenueReportVO getDistrictReport(String districtId,LocalDate fromDate,LocalDate toDate);
	/**
	 * 
	 * @param officeCode
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	RevenueReportVO getOfficewisepPayments(String officeCode,LocalDate fromDate,LocalDate toDate);
	
	RevenueReportVO getDateWiseReports(String gateWayId,String officeCode,LocalDate fromDate,LocalDate toDate);
	
	List<OfficePaymentVO> getApplicationWiseReport(String gateWayId,String officeCode,LocalDate fromDate,LocalDate toDate);
	
	void regServiceReportDataSync();
	
	List<SNReportResultVO> eBiddingCovWiseData(LocalDate fromDate,LocalDate toDate,String cov,String series, JwtUser user);

	List<SNReportResultVO> eBiddingOfficeWiseData(LocalDate fromDate, LocalDate toDate, String officeCode);

	void generateExcelForEbiddingReports(List<SNReportResultVO> ebidReport, HttpServletResponse response);
}
