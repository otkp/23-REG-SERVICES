package org.epragati.reports.service;

import java.time.LocalDate;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.epragati.common.dto.CovTypesDTO;
import org.epragati.jwt.JwtUser;
import org.epragati.master.vo.DistrictVO;
import org.epragati.master.vo.OfficeVO;
import org.epragati.payment.report.vo.InvoiceDetailsReportVo;
import org.epragati.rta.reports.vo.ReportVO;

public interface RegistrationReportService {

	List<DistrictVO> getDistricts();

	List<OfficeVO> getOfficeCodes(Integer districtId);

	List<String> getVehicleType(String roleType);

	List<CovTypesDTO> getClassOfVehicles(List<String> category);

	List<ReportVO> getVehicleStrengthReport(String selectedRole, String districtId, String officeCode,
			String vehicleType, LocalDate countDate, String groupCategory, JwtUser jwtUser);

	public List<InvoiceDetailsReportVo> invoiceDetailsReport(LocalDate fromDate, LocalDate toDate, String user)
			throws Exception;

	void generateVehicleStrengthReportExcel(List<ReportVO> reportVo, HttpServletResponse response);

	void generateVehicleStrengthReportOfficeDataExcel(List<ReportVO> reportVo, HttpServletResponse response);

	void generateVehicleStrengthReportTransportDataExcel(List<ReportVO> reportVo, HttpServletResponse response);
	
}