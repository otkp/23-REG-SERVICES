package org.epragati.reports.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.epragati.dealer.vo.TrIssuedReportVO;
import org.epragati.excel.service.CellProps;
import org.epragati.excel.service.ExcelService;
import org.epragati.excel.service.ExcelServiceImpl;
import org.epragati.master.dao.FcDetailsDAO;
import org.epragati.master.dao.RegServiceReportDAO;
import org.epragati.master.dto.FcDetailsDTO;
import org.epragati.master.dto.NonPaymentDetailsDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.payment.report.vo.NonPaymentDetailsVO;
import org.epragati.payment.report.vo.RegReportDuplicateVO;
import org.epragati.payment.report.vo.RegReportVO;
import org.epragati.payment.report.vo.ReportsVO;
import org.epragati.permits.dao.PermitDetailsDAO;
import org.epragati.permits.dto.FitnessReportsDemoVO;
import org.epragati.permits.dto.PermitDetailsDTO;
import org.epragati.regservice.RegistrationService;
import org.epragati.regservice.dto.RegServiceReportDTO;
import org.epragati.regservice.vo.CitizenApplicationSearchResponceVO;
import org.epragati.reports.service.RCCancellationService;
import org.epragati.reports.service.ReportsExcelExportService;
import org.epragati.rta.reports.vo.ActionCountDetailsVO;
import org.epragati.rta.reports.vo.CheckPostReportsVO;
import org.epragati.rta.reports.vo.EODReportVO;
import org.epragati.rta.reports.vo.ReportVO;
import org.epragati.rta.reports.vo.RoleBasedCounts;
import org.epragati.rta.reports.vo.UserWiseEODCount;
import org.epragati.rta.reports.vo.VcrCovAndOffenceBasedReportVO;
import org.epragati.util.DateConverters;
import org.epragati.util.PermitsEnum;
import org.epragati.util.payment.ClassOfVehicleEnum;
import org.epragati.vcr.vo.VcrFinalServiceVO;
import org.epragati.vcr.vo.VcrVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.qos.logback.classic.pattern.DateConverter;

@Service
public class ReportsExcelExportServiceImpl implements ReportsExcelExportService {
	private static final Logger logger = LoggerFactory.getLogger(ReportsExcelExportServiceImpl.class);

	@Autowired
	private RegServiceReportDAO regServiceReportDAO;

	@Autowired
	private PermitDetailsDAO permitDetailsDAO;

	@Autowired
	private FcDetailsDAO fcDetailsDAO;

	@Autowired
	private RegistrationService registrationService;
	
	@Autowired
	private RCCancellationService rcCancellationService;

	@Override
	public void excelReportsForEod(HttpServletResponse response, ActionCountDetailsVO report, EODReportVO eodVO) {
		if (eodVO.isEodCountExcel()) {
			generateExcelForEodCount(response, report);
		} else if (eodVO.isEodSingleCountExcel()) {
			generateExcelForEodSingleCount(response, report);
		} else if (eodVO.isEodDataExcel()) {
			generateExcelForEodList(response, eodVO);
		} else if (eodVO.isEodSingleDataExcel()) {
			generateExcelForEodSingleList(response, eodVO);
		} else if (eodVO.isEodDistCountExcel()) {
			generateExcelForEodDistCount(response, report);
		} else if (eodVO.isEodDistRoleCountExcel()) {
			generateExcelForEodDistRoleCount(response, report);
		} else if (eodVO.isEodDistRoleListExcel()) {
			generateExcelForEodDistRoleList(response, eodVO);
		}
	}

	private void settingValuesForExcel(HttpServletResponse response, String sheetName, XSSFWorkbook wb) {
		XSSFSheet sheet = wb.getSheet(sheetName);
		if (sheet != null) {
			sheet.setDefaultColumnWidth(40);
			XSSFDataFormat df = wb.createDataFormat();
			sheet.getColumnStyle(4).setDataFormat(df.getFormat("0x31"));
		}
		CellStyle style = wb.createCellStyle();// Create style

		XSSFFont font = wb.createFont();// Create font
		font.setBoldweight(XSSFFont.BOLDWEIGHT_NORMAL);// Make font bold
		font.setColor(HSSFColor.BLACK.index);
		style.setFont(font);// set it to bold

		try {
			ServletOutputStream outputStream = null;
			outputStream = response.getOutputStream();
			wb.write(outputStream);
			outputStream.flush();
			outputStream.close();

		} catch (IOException e) {
			logger.error(" excel retriveAllRecords download IOException : {}", e.getMessage());
		}
	}

	public void generateExcelForEodCount(HttpServletResponse response, ActionCountDetailsVO report) {
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "EodCountList");

		String name = "EodCountList";
		String fileName = name + ".xlsx";
		String sheetName = "EodCountList";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		XSSFWorkbook wb = excel.renderData(prepareCellPropsForEodExcelCount(header, report), header, fileName,
				sheetName);

		settingValuesForExcel(response, sheetName, wb);
	}

	private List<List<CellProps>> prepareCellPropsForEodExcelCount(List<String> header, ActionCountDetailsVO report) {
		// TODO Auto-generated method stub
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		List<CellProps> result = new ArrayList<CellProps>();
		for (int i = 0; i < header.size(); i++) {
			CellProps cells = new CellProps();

			switch (i) {
			case 0:
				cells.setFieldValue(String.valueOf(++slNo));
				break;
			case 1:
				if (report.getServiceDesc() != null) {
					cells.setFieldValue(DateConverters.replaceDefaults(report.getServiceDesc()));
				}
				break;
			case 2:
				if (report.getTotalCount() >= 0) {
					cells.setFieldValue(DateConverters.replaceDefaults(report.getTotalCount()));
				}
				break;
			case 3:
				if (report.getApprovedCount() >= 0) {
					cells.setFieldValue(DateConverters.replaceDefaults(report.getApprovedCount()));
				}
				break;
			case 4:
				if (report.getRejectedCount() >= 0) {
					cells.setFieldValue(DateConverters.replaceDefaults(report.getRejectedCount()));
				}
			default:
				break;
			}
			result.add(cells);

		}
		cell.add(result);

		return cell;
	}

	public void generateExcelForEodSingleCount(HttpServletResponse response, ActionCountDetailsVO report) {
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "EodCountList");

		String name = "EodCountList";
		String fileName = name + ".xlsx";
		String sheetName = "EodCountList";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		XSSFWorkbook wb = excel.renderData(prepareCellPropsForEodSingleCount(header, report), header, fileName,
				sheetName);

		settingValuesForExcel(response, sheetName, wb);
	}

	private List<List<CellProps>> prepareCellPropsForEodSingleCount(List<String> header, ActionCountDetailsVO report) {
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		List<CellProps> result = new ArrayList<CellProps>();
		for (int i = 0; i < header.size(); i++) {
			CellProps cells = new CellProps();

			switch (i) {
			case 0:
				cells.setFieldValue(String.valueOf(++slNo));
				break;
			case 1:
				if (report.getServiceDesc() != null) {
					cells.setFieldValue(DateConverters.replaceDefaults(report.getServiceDesc()));
				}
				break;
			case 2:
				if (report.getTotalCount() >= 0) {
					cells.setFieldValue(DateConverters.replaceDefaults(report.getTotalCount()));
				}
				break;
			case 3:
				if (report.getApprovedCount() >= 0) {
					cells.setFieldValue(DateConverters.replaceDefaults(report.getApprovedCount()));
				}
				break;
			case 4:
				if (report.getRejectedCount() >= 0) {
					cells.setFieldValue(DateConverters.replaceDefaults(report.getRejectedCount()));
				}
			default:
				break;
			}
			result.add(cells);

		}
		cell.add(result);

		return cell;
	}

	public void generateExcelForEodList(HttpServletResponse response, EODReportVO eodVO) {
		List<RegServiceReportDTO> reportDTO = regServiceReportDAO
				.findByActionRoleNameAndActionUserNameAndActionStatusInAndOfficeCodeAndActionTimeBetween(
						eodVO.getSelectedRole(), eodVO.getUserName(), eodVO.getStatusList(), eodVO.getOfficeCode(),
						eodVO.getFromDate(), eodVO.getToDate());

		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "EodDataList");

		String name = "EodDataList";
		String fileName = name + ".xlsx";
		String sheetName = "EodDataList";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		XSSFWorkbook wb = excel.renderData(prepareCellPropsForEodDataList(header, reportDTO), header, fileName,
				sheetName);

		settingValuesForExcel(response, sheetName, wb);
	}

	private List<List<CellProps>> prepareCellPropsForEodDataList(List<String> header,
			List<RegServiceReportDTO> reportDTO) {
		// TODO Auto-generated method stub
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();
		if (CollectionUtils.isNotEmpty(reportDTO)) {
			for (RegServiceReportDTO reportDTO1 : reportDTO) {
				List<CellProps> result = new ArrayList<CellProps>();
				for (int i = 0; i < header.size(); i++) {
					CellProps cellpro = new CellProps();
					switch (i) {
					case 0:
						cellpro.setFieldValue(String.valueOf(++slNo));
						break;
					case 1:
						if (reportDTO1.getApplicationNumber() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(reportDTO1.getApplicationNumber()));
						}
						break;
					case 2:
						if (reportDTO1.getPrNumber() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(reportDTO1.getPrNumber()));
						}
						break;
					case 3:
						if (reportDTO1.getClassOfVehicle() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(reportDTO1.getClassOfVehicle()));
						}
						break;
					case 4:
						if (reportDTO1.getCitizenServicesNames() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(reportDTO1.getCitizenServicesNames()));
						}
						break;
					case 5:
						if (reportDTO1.getCreatedDate() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(reportDTO1.getCreatedDate()));
						}
						break;
					case 6:
						if (reportDTO1.getActionTime() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(reportDTO1.getActionTime()));
						}
						break;
					case 7:
						if (reportDTO1.getActionStatus() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(reportDTO1.getActionStatus()));
						}
						break;
					case 8:
						if (reportDTO1.getIpAddress() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(reportDTO1.getIpAddress()));
						}

					default:
						break;
					}
					result.add(cellpro);
				}
				cell.add(result);
			}
		}

		return cell;
	}

	public void generateExcelForEodSingleList(HttpServletResponse response, EODReportVO eodVO) {
		List<RegServiceReportDTO> dto = regServiceReportDAO
				.findByActionRoleNameAndActionUserNameAndOfficeCodeAndActionStatusInAndCitizenServicesNamesAndActionTimeBetween(
						eodVO.getSelectedRole(), eodVO.getUserName(), eodVO.getOfficeCode(), eodVO.getStatusList(),
						eodVO.getServiceName(), eodVO.getFromDate(), eodVO.getToDate());

		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "EodDataList");

		String name = "EodDataList";
		String fileName = name + ".xlsx";
		String sheetName = "EodDataList";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		XSSFWorkbook wb = excel.renderData(prepareCellPropsForEodSingleDataList(header, dto), header, fileName,
				sheetName);

		settingValuesForExcel(response, sheetName, wb);
	}

	private List<List<CellProps>> prepareCellPropsForEodSingleDataList(List<String> header,
			List<RegServiceReportDTO> dto) {
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();
		if (CollectionUtils.isNotEmpty(dto)) {
			for (RegServiceReportDTO dto1 : dto) {
				List<CellProps> result = new ArrayList<CellProps>();
				for (int i = 0; i < header.size(); i++) {
					CellProps cellpro = new CellProps();
					switch (i) {
					case 0:
						cellpro.setFieldValue(String.valueOf(++slNo));
						break;
					case 1:
						if (dto1.getApplicationNumber() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(dto1.getApplicationNumber()));
						}
						break;
					case 2:
						if (dto1.getPrNumber() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(dto1.getPrNumber()));
						}
						break;
					case 3:
						if (dto1.getClassOfVehicle() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(dto1.getClassOfVehicle()));
						}
						break;
					case 4:
						if (dto1.getCitizenServicesNames() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(dto1.getCitizenServicesNames()));
						}
						break;
					case 5:
						if (dto1.getCreatedDate() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(dto1.getCreatedDate()));
						}
						break;
					case 6:
						if (dto1.getActionTime() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(dto1.getActionTime()));
						}
						break;
					case 7:
						if (dto1.getActionStatus() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(dto1.getActionStatus()));
						}
						break;
					case 8:
						if (dto1.getIpAddress() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(dto1.getIpAddress()));
						}

					default:
						break;
					}
					result.add(cellpro);
				}
				cell.add(result);
			}
		}

		return cell;
	}

	public void generateExcelForEodDistCount(HttpServletResponse response, ActionCountDetailsVO report) {

		List<String> header = new ArrayList<>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "EodDistCountList");

		String name = "EodDistCountList";
		String fileName = name + ".xlsx";
		String sheetName = "EodDistCountList";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		XSSFWorkbook wb = excel.renderData(prepareCellPropsForEodDistCount(header, report), header, fileName,
				sheetName);

		settingValuesForExcel(response, sheetName, wb);

	}

	private List<List<CellProps>> prepareCellPropsForEodDistCount(List<String> header, ActionCountDetailsVO report) {
		int slNo = 0;
		boolean track = false;
		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (ActionCountDetailsVO report1 : report.getRolesBasedReport()) {
			track = true;

			// for office
			for (RoleBasedCounts user : report1.getUsersBasedCounts()) {
				List<CellProps> result = new ArrayList<CellProps>();
				// for counts
				for (int i = 0; i < header.size(); i++) {
					CellProps cellpro = new CellProps();
					switch (i) {
					case 0:
						if (track) {
							cellpro.setFieldValue(String.valueOf(++slNo));
							track = false;
							track = true;
						}

						break;

					case 1:
						if (track) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(report1.getOfficeName()));
							track = false;
						}
						break;
					case 2:
						cellpro.setFieldValue(DateConverters.replaceDefaults(user.getRoleName()));
						break;
					case 3:
						cellpro.setFieldValue(DateConverters.replaceDefaults(user.getTotalCount()));
						break;
					case 4:
						cellpro.setFieldValue(DateConverters.replaceDefaults(user.getApprovedCount()));
						break;
					case 5:
						cellpro.setFieldValue(DateConverters.replaceDefaults(user.getRejectedCount()));
					default:
						break;
					}
					result.add(cellpro);
				}
				cell.add(result);
			}

		}

		return cell;

	}

	private void generateExcelForEodDistRoleCount(HttpServletResponse response, ActionCountDetailsVO report) {
		List<String> header = new ArrayList<>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "EodDistRoleCountList");

		String name = "EodDistRoleCountList";
		String fileName = name + ".xlsx";
		String sheetName = "EodDistRoleCountList";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		XSSFWorkbook wb = excel.renderData(prepareCellPropsForEodDistRoleCount(header, report), header, fileName,
				sheetName);

		settingValuesForExcel(response, sheetName, wb);
	}

	private List<List<CellProps>> prepareCellPropsForEodDistRoleCount(List<String> header,
			ActionCountDetailsVO report) {
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (RoleBasedCounts report1 : report.getUsersBasedCounts()) {
			List<CellProps> result = new ArrayList<CellProps>();
			for (int i = 0; i < header.size(); i++) {
				CellProps cellpro = new CellProps();
				switch (i) {
				case 0:
					cellpro.setFieldValue(String.valueOf(++slNo));
					break;

				case 1:
					cellpro.setFieldValue(DateConverters.replaceDefaults(report1.getUserName()));
					break;
				case 2:
					cellpro.setFieldValue(DateConverters.replaceDefaults(report1.getTotalCount()));
					break;
				case 3:
					cellpro.setFieldValue(DateConverters.replaceDefaults(report1.getApprovedCount()));
					break;
				case 4:
					cellpro.setFieldValue(DateConverters.replaceDefaults(report1.getRejectedCount()));
				default:
					break;
				}
				result.add(cellpro);
			}
			cell.add(result);

		}
		return cell;
	}

	private void generateExcelForEodDistRoleList(HttpServletResponse response, EODReportVO eodVO) {
		// TODO Auto-generated method stub
		List<RegServiceReportDTO> reportDTO = regServiceReportDAO
				.findByActionRoleNameAndActionUserNameAndActionStatusInAndOfficeCodeAndActionTimeBetween(
						eodVO.getRoleName(), eodVO.getUserName(), eodVO.getStatusList(), eodVO.getOfficeCode(),
						eodVO.getFromDate(), eodVO.getToDate());

		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "EodDataList");

		String name = "EodDataList";
		String fileName = name + ".xlsx";
		String sheetName = "EodDataList";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		XSSFWorkbook wb = excel.renderData(prepareCellPropsForEodDistList(header, reportDTO), header, fileName,
				sheetName);

		settingValuesForExcel(response, sheetName, wb);
	}

	private List<List<CellProps>> prepareCellPropsForEodDistList(List<String> header,
			List<RegServiceReportDTO> reportDTO) {
		// TODO Auto-generated method stub
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();
		if (CollectionUtils.isNotEmpty(reportDTO)) {
			for (RegServiceReportDTO reportDTO1 : reportDTO) {
				List<CellProps> result = new ArrayList<CellProps>();
				for (int i = 0; i < header.size(); i++) {
					CellProps cellpro = new CellProps();
					switch (i) {
					case 0:
						cellpro.setFieldValue(String.valueOf(++slNo));
						break;
					case 1:
						if (reportDTO1.getApplicationNumber() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(reportDTO1.getApplicationNumber()));
						}
						break;
					case 2:
						if (reportDTO1.getPrNumber() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(reportDTO1.getPrNumber()));
						}
						break;
					case 3:
						if (reportDTO1.getClassOfVehicle() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(reportDTO1.getClassOfVehicle()));
						}
						break;
					case 4:
						if (reportDTO1.getCitizenServicesNames() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(reportDTO1.getCitizenServicesNames()));
						}
						break;
					case 5:
						if (reportDTO1.getCreatedDate() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(reportDTO1.getCreatedDate()));
						}
						break;
					case 6:
						if (reportDTO1.getActionTime() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(reportDTO1.getActionTime()));
						}
						break;
					case 7:
						if (reportDTO1.getActionStatus() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(reportDTO1.getActionStatus()));
						}
						break;
					case 8:
						if (reportDTO1.getIpAddress() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(reportDTO1.getIpAddress()));
						}

					default:
						break;
					}
					result.add(cellpro);
				}
				cell.add(result);
			}
		}

		return cell;
	}

	public void generateExcelForContractCarriageList(TrIssuedReportVO resultList, HttpServletResponse response,
			RegReportVO regVO) {
		// TODO Auto-generated method stub
		List<PermitDetailsDTO> permitsList = permitDetailsDAO
				.findByOfficeCodeInAndClassOfVehicleInAndPermitStatusAndPermitRouteCode(regVO.getOfficeCodes(),
						Arrays.asList(ClassOfVehicleEnum.COCT.getCovCode(), ClassOfVehicleEnum.TOVT.getCovCode()),
						PermitsEnum.ACTIVE.getDescription(), regVO.getRouteCodes(), LocalDate.now());
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "ContractCarriagePermits");

		String name = "ContractCarriagePermits";
		String fileName = name + ".xlsx";
		String sheetName = "ContractCarriagePermits";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		XSSFWorkbook wb = excel.renderData(prepareCellPropsForContractCarriage(header, permitsList), header, fileName,
				sheetName);

		settingValuesForExcel(response, sheetName, wb);

	}

	private List<List<CellProps>> prepareCellPropsForContractCarriage(List<String> header,
			List<PermitDetailsDTO> permitsList) {
		// TODO Auto-generated method stub
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		if (!permitsList.isEmpty()) {
			for (PermitDetailsDTO permitsList1 : permitsList) {
				Optional<FcDetailsDTO> fcDetailsOpt = fcDetailsDAO
						.findByStatusIsTrueAndPrNoOrderByCreatedDateDesc(permitsList1.getPrNo());
				Optional<TaxDetailsDTO> taxDTO = registrationService.getLatestTaxTransaction(permitsList1.getPrNo());

				List<CellProps> result = new ArrayList<CellProps>();
				for (int i = 0; i < header.size(); i++) {
					CellProps cellpro = new CellProps();
					switch (i) {
					case 0:
						cellpro.setFieldValue(String.valueOf(++slNo));
						break;
					case 1:
						if (permitsList1.getPrNo() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(permitsList1.getPrNo()));
						}
						break;
					case 2:
						if (permitsList1.getRdto().getApplicantDetails().getDisplayName() != null) {
							cellpro.setFieldValue(DateConverters
									.replaceDefaults(permitsList1.getRdto().getApplicantDetails().getDisplayName()));
						}
						break;
					case 3:
						if (permitsList1.getRdto().getClassOfVehicleDesc() != null) {
							cellpro.setFieldValue(
									DateConverters.replaceDefaults(permitsList1.getRdto().getClassOfVehicleDesc()));
						}
						break;
					case 4:
						if (permitsList1.getRdto().getVahanDetails().getMakersDesc() != null) {
							cellpro.setFieldValue(DateConverters
									.replaceDefaults(permitsList1.getRdto().getVahanDetails().getMakersDesc()));
						}
						break;
					case 5:
						if (permitsList1.getRdto().getVahanDetails().getChassisNumber() != null) {
							cellpro.setFieldValue(DateConverters
									.replaceDefaults(permitsList1.getRdto().getVahanDetails().getChassisNumber()));
						}
						break;
					case 6:
						if (permitsList1.getRdto().getVahanDetails().getEngineNumber() != null) {
							cellpro.setFieldValue(DateConverters
									.replaceDefaults(permitsList1.getRdto().getVahanDetails().getEngineNumber()));
						}
						break;
					case 7:
						if (taxDTO.isPresent() && taxDTO.get().getTaxPaidDate() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(taxDTO.get().getTaxPaidDate()));
						}
						break;
					case 8:
						if (fcDetailsOpt.isPresent() && fcDetailsOpt.get().getFcValidUpto() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(fcDetailsOpt.get().getFcValidUpto()));
						}
						break;
					case 9:
						if (permitsList1.getPermitValidityDetails().getPermitValidFrom() != null) {
							cellpro.setFieldValue(DateConverters
									.replaceDefaults(permitsList1.getPermitValidityDetails().getPermitValidFrom()));
						}
						break;
					case 10:
						if (permitsList1.getPermitValidityDetails().getPermitValidTo() != null) {
							cellpro.setFieldValue(DateConverters
									.replaceDefaults(permitsList1.getPermitValidityDetails().getPermitValidTo()));
						}
						break;
					case 11:
						if (permitsList1.getPermitType().getDescription() != null) {
							cellpro.setFieldValue(
									DateConverters.replaceDefaults(permitsList1.getPermitType().getDescription()));
						}
						break;
					case 12:
						if (taxDTO.isPresent() && taxDTO.get().getTaxPeriodEnd() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(taxDTO.get().getTaxPeriodEnd()));
						}
						break;
					case 13:
						if (permitsList1.getRdto().getVahanDetails().getSeatingCapacity() != null) {
							cellpro.setFieldValue(DateConverters
									.replaceDefaults(permitsList1.getRdto().getVahanDetails().getSeatingCapacity()));
						}
						break;
					case 14:
						if (permitsList1.getRdto().getApplicantDetails().getPresentAddress().getDoorNo() != null
								&& permitsList1.getRdto().getApplicantDetails().getPresentAddress()
										.getTownOrCity() != null
								&& permitsList1.getRdto().getApplicantDetails().getPresentAddress().getMandal()
										.getMandalName() != null
								&& permitsList1.getRdto().getApplicantDetails().getPresentAddress().getDistrict()
										.getDistrictName() != null
								&& permitsList1.getRdto().getApplicantDetails().getPresentAddress().getState()
										.getStateName() != null) {

							cellpro.setFieldValue(DateConverters.replaceDefaults("DoorNo" + ":"
									+ permitsList1.getRdto().getApplicantDetails().getPresentAddress().getDoorNo() + ","
									+ "Street:" + " "
									+ permitsList1.getRdto().getApplicantDetails().getPresentAddress().getTownOrCity()
									+ "," + "Mandal:" + " "
									+ permitsList1.getRdto().getApplicantDetails().getPresentAddress().getMandal()
											.getMandalName()
									+ "," + "District:" + " "
									+ permitsList1.getRdto().getApplicantDetails().getPresentAddress().getDistrict()
											.getDistrictName()
									+ "," + "State:" + " " + permitsList1.getRdto().getApplicantDetails()
											.getPresentAddress().getState().getStateName()));
						}
					default:
						break;
					}
					result.add(cellpro);
				}
				cell.add(result);
			}

		}
		return cell;
	}

	@Override
	public void getRoadSafetyVcrDistrictCountExcel(HttpServletResponse response, ReportVO report) {
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "RoadSafetyVcrDistrictList");

		String name = "RoadSafetyVcrDistrictList";
		String fileName = name + ".xlsx";
		String sheetName = "RoadSafetyVcrDistrictList";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		XSSFWorkbook wb = excel.renderData(prepareCellPropsForRoadSafetyVcrDistrictCount(header, report), header,
				fileName, sheetName);

		XSSFSheet sheet = wb.getSheet(sheetName);
		sheet.setDefaultColumnWidth(40);
		XSSFDataFormat df = wb.createDataFormat();
		sheet.getColumnStyle(4).setDataFormat(df.getFormat("0x31"));

		CellStyle style = wb.createCellStyle();// Create style

		XSSFFont font = wb.createFont();// Create font
		font.setBoldweight(XSSFFont.BOLDWEIGHT_NORMAL);// Make font bold
		font.setColor(HSSFColor.BLACK.index);
		style.setFont(font);// set it to bold

		try {
			ServletOutputStream outputStream = null;
			outputStream = response.getOutputStream();
			wb.write(outputStream);
			outputStream.flush();
			outputStream.close();

		} catch (IOException e) {
			logger.error(" excel retriveAllRecords download IOException : {}", e.getMessage());
		}

	}

	private List<List<CellProps>> prepareCellPropsForRoadSafetyVcrDistrictCount(List<String> header, ReportVO report) {
		// TODO Auto-generated method stub
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (ReportVO report1 : report.getReport()) {
			if (report1 != null) {
				List<CellProps> result = new ArrayList<CellProps>();
				for (int i = 0; i < header.size(); i++) {
					CellProps cellpro = new CellProps();
					switch (i) {
					case 0:
						cellpro.setFieldValue(String.valueOf(++slNo));
						break;
					case 1:
						if (report1.getDistrictName() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(report1.getDistrictName()));
						}
						break;
					case 2:
						if (report1.getReport().get(i - 2).getOffenceCount() >= 0) {
							cellpro.setFieldValue(
									DateConverters.replaceDefaults(report1.getReport().get(i - 2).getOffenceCount()));
						}
						break;
					case 3:
						if (report1.getReport().get(i - 2).getOffenceCount() >= 0) {
							cellpro.setFieldValue(
									DateConverters.replaceDefaults(report1.getReport().get(i - 2).getOffenceCount()));
						}
					case 4:
						if (report1.getReport().get(i - 2).getOffenceCount() >= 0) {
							cellpro.setFieldValue(
									DateConverters.replaceDefaults(report1.getReport().get(i - 2).getOffenceCount()));
						}
						break;
					case 5:
						if (report1.getReport().get(i - 2).getOffenceCount() >= 0) {
							cellpro.setFieldValue(
									DateConverters.replaceDefaults(report1.getReport().get(i - 2).getOffenceCount()));
						}
						break;
					case 6:
						if (report1.getReport().get(i - 2).getOffenceCount() >= 0) {
							cellpro.setFieldValue(
									DateConverters.replaceDefaults(report1.getReport().get(i - 2).getOffenceCount()));
						}
						break;
					case 7:
						if (report1.getReport().get(i - 2).getOffenceCount() >= 0) {
							cellpro.setFieldValue(
									DateConverters.replaceDefaults(report1.getReport().get(i - 2).getOffenceCount()));
						}
						break;
					case 8:
						if (report1.getReport().get(i - 2).getOffenceCount() >= 0) {
							cellpro.setFieldValue(
									DateConverters.replaceDefaults(report1.getReport().get(i - 2).getOffenceCount()));
						}
						break;
					case 9:
						if (report1.getReport().get(i - 2).getOffenceCount() >= 0) {
							cellpro.setFieldValue(
									DateConverters.replaceDefaults(report1.getReport().get(i - 2).getOffenceCount()));
						}
						break;
					case 10:
						if (report1.getTotalCount() >= 0) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(report1.getTotalCount()));
						}

					default:
						break;
					}
					result.add(cellpro);
				}
				cell.add(result);
			}
		}
		cell.add(prepareMapRowCellToRoadSafetyDistrictCount(header, report));
		return cell;
	}

	private List<CellProps> prepareMapRowCellToRoadSafetyDistrictCount(List<String> header, ReportVO report) {

		List<CellProps> cells = new ArrayList<>();
		for (int i = 0; i < header.size(); i++) {
			CellProps cellpro = new CellProps();
			switch (i) {
			case 0:
				cellpro.setFieldValue(StringUtils.EMPTY);
				break;
			case 1:
				cellpro.setFieldValue("TOTAL");
				break;
			case 2:
				if (report.getTotaloffenceCount().get(i - 2).getOffenceCount() >= 0) {
					cellpro.setFieldValue(
							DateConverters.replaceDefaults(report.getTotaloffenceCount().get(i - 2).getOffenceCount()));
				}
				break;
			case 3:
				if (report.getTotaloffenceCount().get(i - 2).getOffenceCount() >= 0) {
					cellpro.setFieldValue(
							DateConverters.replaceDefaults(report.getTotaloffenceCount().get(i - 2).getOffenceCount()));
				}
				break;
			case 4:
				if (report.getTotaloffenceCount().get(i - 2).getOffenceCount() >= 0) {
					cellpro.setFieldValue(
							DateConverters.replaceDefaults(report.getTotaloffenceCount().get(i - 2).getOffenceCount()));
				}
				break;
			case 5:
				if (report.getTotaloffenceCount().get(i - 2).getOffenceCount() >= 0) {
					cellpro.setFieldValue(
							DateConverters.replaceDefaults(report.getTotaloffenceCount().get(i - 2).getOffenceCount()));
				}
				break;
			case 6:
				if (report.getTotaloffenceCount().get(i - 2).getOffenceCount() >= 0) {
					cellpro.setFieldValue(
							DateConverters.replaceDefaults(report.getTotaloffenceCount().get(i - 2).getOffenceCount()));
				}
				break;
			case 7:
				if (report.getTotaloffenceCount().get(i - 2).getOffenceCount() >= 0) {
					cellpro.setFieldValue(
							DateConverters.replaceDefaults(report.getTotaloffenceCount().get(i - 2).getOffenceCount()));
				}
				break;
			case 8:
				if (report.getTotaloffenceCount().get(i - 2).getOffenceCount() >= 0) {
					cellpro.setFieldValue(
							DateConverters.replaceDefaults(report.getTotaloffenceCount().get(i - 2).getOffenceCount()));
				}
				break;
			case 9:
				if (report.getTotaloffenceCount().get(i - 2).getOffenceCount() >= 0) {
					cellpro.setFieldValue(
							DateConverters.replaceDefaults(report.getTotaloffenceCount().get(i - 2).getOffenceCount()));
				}
				break;
			case 10:
				if (report.getGrandTotal() >= 0) {
					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getGrandTotal()));
				}
			default:
				break;
			}
			cells.add(cellpro);
		}
		return cells;
	}

	@Override
	public void generateExcelForOffenceWiseVcrReport(VcrCovAndOffenceBasedReportVO offenceResult,
			RegReportVO regVo, HttpServletResponse response) {
		
		if(regVo.isOffenceWiseVcrExcelCount()) {
			generateExcelForOffenceWiseVcrCount(offenceResult,response);
		}
		
		if(regVo.isOffenceWiseVcrExcelMviCount()) {
			generateExcelForOffenceWiseVcrMviCount(offenceResult,response);
		}
	}

	private void generateExcelForOffenceWiseVcrMviCount(VcrCovAndOffenceBasedReportVO offenceResult,
			HttpServletResponse response) {
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "OffenceWiseVcrMviCount");

		String name = "OffenceWiseVcrMviCount";
		String fileName = name + ".xlsx";
		String sheetname = "OffenceWiseVcrMviCount"; 

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);
		
		XSSFWorkbook wb = excel.renderData(prepareCellPropsForOffenceWiseVcrMviCountExcel(header, offenceResult), header, fileName,
				sheetname);
		
		settingValuesForExcel(response, sheetname, wb);
		
	}

	private List<List<CellProps>> prepareCellPropsForOffenceWiseVcrMviCountExcel(List<String> header,
			VcrCovAndOffenceBasedReportVO offenceResult) {
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (CheckPostReportsVO report : offenceResult.getMviCovList()) {
			if (report != null) {
				List<CellProps> result = new ArrayList<CellProps>();
				for (int i = 0; i < header.size(); i++) {
					CellProps cellpro = new CellProps();
					switch (i) {
					case 0:
						cellpro.setFieldValue(String.valueOf(++slNo));
						break;
					case 1:
						if(report.getMviName() != null) {
						cellpro.setFieldValue(report.getMviName());
						}
						break;
					case 2:
						if(report.getOffenceName() != null) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getOffenceName()));
						}
						break;
					case 3:
						if(report.getVcrCount() >= 0) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getVcrCount()));
						}
					default:
						break;
					}
					result.add(cellpro);
				}
				cell.add(result);
			}
		}
		return cell;
	}

	private void generateExcelForOffenceWiseVcrCount(VcrCovAndOffenceBasedReportVO offenceResult,
			HttpServletResponse response) {
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "OffenceWiseVcrCount");

		String name = "OffenceWiseVcrCount";
		String fileName = name + ".xlsx";
		String sheetname = "OffenceWiseVcrCount"; 

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);
		
		XSSFWorkbook wb = excel.renderData(prepareCellPropsForOffenceWiseVcrCountExcel(header, offenceResult), header, fileName,
				sheetname);
		
		settingValuesForExcel(response, sheetname, wb);
		
	}

	private List<List<CellProps>> prepareCellPropsForOffenceWiseVcrCountExcel(List<String> header,
			VcrCovAndOffenceBasedReportVO offenceResult) {
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (ReportVO report : offenceResult.getOffencesBasedList()) {
			if (report != null) {
				List<CellProps> result = new ArrayList<CellProps>();
				for (int i = 0; i < header.size(); i++) {
					CellProps cellpro = new CellProps();
					switch (i) {
					case 0:
						cellpro.setFieldValue(String.valueOf(++slNo));
						break;
					case 1:
						if(report.getOffenceDesc() != null) {
						cellpro.setFieldValue(report.getOffenceDesc());
						}
						break;
					case 2:
						if(report.getOffenceCount() >= 0) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getOffenceCount()));
						}
					default:
						break;
					}
					result.add(cellpro);
				}
				cell.add(result);
			}
		}
		return cell;
	}

	@Override
	public void generateExcelForCovWiseVcrReport(VcrCovAndOffenceBasedReportVO covWiseVcrResult,
			RegReportVO regVo, HttpServletResponse response) {

		if(regVo.isCovWiseVcrReportExcelCovCount()) {
			generateCovWiseVcrExcelCount(covWiseVcrResult,response);
		}
		
		if(regVo.isCovWiseVcrReportExcelMviCount()) {
			generateCovWiseVcrExcelMviCount(covWiseVcrResult,response);
		}
	}

	private void generateCovWiseVcrExcelMviCount(VcrCovAndOffenceBasedReportVO covWiseVcrResult,
			HttpServletResponse response) {
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "CovWiseVcrMviCount");

		String name = "CovWiseVcrMviCount";
		String fileName = name + ".xlsx";
		String sheetname = "CovWiseVcrMviCount"; 

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);
		
		XSSFWorkbook wb = excel.renderData(prepareCellPropsForCovWiseVcrMviCountExcel(header, covWiseVcrResult), header, fileName,
				sheetname);
		
		settingValuesForExcel(response, sheetname, wb);
	}

	private List<List<CellProps>> prepareCellPropsForCovWiseVcrMviCountExcel(List<String> header,
			VcrCovAndOffenceBasedReportVO covReport) {
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (CheckPostReportsVO report : covReport.getMviCovList()) {
			if (report != null) {
				List<CellProps> result = new ArrayList<CellProps>();
				for (int i = 0; i < header.size(); i++) {
					CellProps cellpro = new CellProps();
					switch (i) {
					case 0:
						cellpro.setFieldValue(String.valueOf(++slNo));
						break;
					case 1:
						if(report.getMviName() != null) {
						cellpro.setFieldValue(report.getMviName());
						}
						break;
					case 2:
						if(report.getCovDescription() != null && !report.getCovDescription().equalsIgnoreCase("TOTAL")) {
						cellpro.setFieldValue(report.getCovDescription());
						}
						break;
					case 3:
						if(report.getVcrCount() >= 0) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getVcrCount()));
						}
					default:
						break;
					}
					result.add(cellpro);
				}
				cell.add(result);
			}
		}
		return cell;
	}

	private void generateCovWiseVcrExcelCount(VcrCovAndOffenceBasedReportVO covWiseVcrResult,
			HttpServletResponse response) {
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "CovWiseVcrCount");

		String name = "CovWiseVcrCount";
		String fileName = name + ".xlsx";
		String sheetname = "CovWiseVcrCount"; 

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);
		
		XSSFWorkbook wb = excel.renderData(prepareCellPropsForCovWiseVcrCountExcel(header, covWiseVcrResult), header, fileName,
				sheetname);
		
		settingValuesForExcel(response, sheetname, wb);
	}

	private List<List<CellProps>> prepareCellPropsForCovWiseVcrCountExcel(List<String> header,
			VcrCovAndOffenceBasedReportVO covReport) {
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (CheckPostReportsVO report : covReport.getCovBasedList()) {
			if (report != null) {
				List<CellProps> result = new ArrayList<CellProps>();
				for (int i = 0; i < header.size(); i++) {
					CellProps cellpro = new CellProps();
					switch (i) {
					case 0:
						cellpro.setFieldValue(String.valueOf(++slNo));
						break;
					case 1:
						if(report.getCov() != null) {
						cellpro.setFieldValue(report.getCov());
						}
						break;
					case 2:
						if(report.getCovDescription() != null && !report.getCovDescription().equalsIgnoreCase("TOTAL")) {
						cellpro.setFieldValue(report.getCovDescription());
						}
						break;
					case 3:
						if(report.getVcrCount() >= 0) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getVcrCount()));
						}
					default:
						break;
					}
					result.add(cellpro);
				}
				cell.add(result);
			}
		}
		return cell;
	}

	public void generateExcelForPaymentCheckPostReport(List<CheckPostReportsVO> checkpost, RegReportVO paymentReportVO,
			HttpServletResponse response) {
		if(paymentReportVO.isPaymentCheckPostReportOfficeCountExcel()) {
			generateExcelForPaymentCheckPostOfficeCount(checkpost, response);
		}
		
		if(paymentReportVO.isPaymentCheckPostReportMviCountExcel()) {
			generateExcelForPaymentCheckPostMviCount(checkpost, response);
		}
		
	}
	
	public void generateExcelForPaymentCheckPostOfficeCount(List<CheckPostReportsVO> checkpost,
			HttpServletResponse response) {
		
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "PaymentCheckPostReportOfficeCount");

		String name = "PaymentCheckPostReportOfficeCount";
		String fileName = name + ".xlsx";
		String sheetname = "PaymentCheckPostReportOfficeCount";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);

		XSSFWorkbook wb = excel.renderData(
				prepareCellPropsForPaymentCheckPostOfficeCountExcel(header, checkpost), header, fileName,
				sheetname);

		settingValuesForExcel(response, sheetname, wb);

	}

	private List<List<CellProps>> prepareCellPropsForPaymentCheckPostOfficeCountExcel(List<String> header,
			List<CheckPostReportsVO> resultList) {
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (CheckPostReportsVO report : resultList) {
			if (report != null) {
				List<CellProps> result = new ArrayList<CellProps>();
				for (int i = 0; i < header.size(); i++) {
					CellProps cellpro = new CellProps();
					switch (i) {
					case 0:
						cellpro.setFieldValue(String.valueOf(++slNo));
						break;
					case 1:
						if(report.getOfficeName() != null) {
						cellpro.setFieldValue(report.getOfficeName());
						}
						break;
					case 2:
						if(report.getVcrCount() >=0) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getVcrCount()));
						}
						break;
					case 3:
						if(report.getPaidCf() >=0) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getPaidCf()));
						}
						break;
					case 4:
						if(report.getUnPaidCf() >=0) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getUnPaidCf()));
						}
						break;
					case 5:
						if(report.getPermitCount() >=0) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getPermitCount()));
						}
						break;
					case 6:
						if(report.getPermitFee() >=0) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getPermitFee()));
						}
						break;
					case 7:
						if(report.getPermitTax() >=0) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getPermitTax()));
						}
						break;
					case 8:
						if(report.getVoluntaryCount() >=0) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getVoluntaryCount()));
						}
						break;
					case 9:
						if(report.getVoluntaryTax() >=0) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getVoluntaryTax()));
						}
						break;
					case 10:
						if(report.getTotal() >=0) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getTotal()));
						}
					default:
						break;
					}
					result.add(cellpro);
				}
				cell.add(result);
			}
		}
		return cell;
	}

	public void generateExcelForPaymentCheckPostMviCount(List<CheckPostReportsVO> checkpost,
			HttpServletResponse response) {
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "PaymentCheckPostReportMviCount");

		String name = "PaymentCheckPostReportMviCount";
		String fileName = name + ".xlsx";
		String sheetname = "PaymentCheckPostReportMviCount";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);

		XSSFWorkbook wb = excel.renderData(
				prepareCellPropsForPaymentCheckPostMviCountExcel(header, checkpost), header, fileName,
				sheetname);

		settingValuesForExcel(response, sheetname, wb);
		
	}

	private List<List<CellProps>> prepareCellPropsForPaymentCheckPostMviCountExcel(List<String> header,
			List<CheckPostReportsVO> resultList) {
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (CheckPostReportsVO report : resultList.stream().findFirst().get().getTotalSum()) {
			if (report != null) {
				List<CellProps> result = new ArrayList<CellProps>();
				for (int i = 0; i < header.size(); i++) {
					CellProps cellpro = new CellProps();
					switch (i) {
					case 0:
						cellpro.setFieldValue(String.valueOf(++slNo));
						break;
					case 1:
						if(report.getMviName() != null) {
						cellpro.setFieldValue(report.getMviName());
						}
						break;
					case 2:
						if(report.getVcrCount() >=0) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getVcrCount()));
						}
						break;
					case 3:
						if(report.getPaidCf() >=0) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getPaidCf()));
						}
						break;
					case 4:
						if(report.getUnPaidCf() >=0) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getUnPaidCf()));
						}
						break;
					case 5:
						if(report.getPermitCount() >=0) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getPermitCount()));
						}
						break;
					case 6:
						if(report.getPermitFee() >=0) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getPermitFee()));
						}
						break;
					case 7:
						if(report.getPermitTax() >=0) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getPermitTax()));
						}
						break;
					case 8:
						if(report.getVoluntaryCount() >=0) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getVoluntaryCount()));
						}
						break;
					case 9:
						if(report.getVoluntaryTax() >=0) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getVoluntaryTax()));
						}
						break;
					case 10:
						if(report.getTotal() >=0) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getTotal()));
						}
					default:
						break;
					}
					result.add(cellpro);
				}
				cell.add(result);
			}
		}
		return cell;
	}

	@Override
	public void generateNonPaymentReportCountExcel(HttpServletResponse response, Optional<ReportsVO> paymentsReport,
			RegReportVO regReportVO) {
		if (regReportVO.isNonPaymentDistrictwiseExcel()) {
			generateNonPaymentDistrictCountExcel(response, paymentsReport);
		}

		if (regReportVO.isNonPaymentOfficewiseExcel()) {
			generateNonPaymentOfficeCountExcel(response, paymentsReport);
		}
		
	}

	private void generateNonPaymentOfficeCountExcel(HttpServletResponse response, Optional<ReportsVO> paymentsReport) {
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "NonPaymentOfficeWiseList");

		String name = "NonPaymentOfficeWiseList";
		String fileName = name + ".xlsx";
		String sheetname = "NonPaymentOfficeWiseList";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		XSSFWorkbook wb = excel.renderData(prepareCellPropsForNonPaymentReportsOfficeCountExcel(header, paymentsReport), header, fileName, sheetname);

		settingValuesForExcel(response, sheetname, wb);
		
	}

	private List<List<CellProps>> prepareCellPropsForNonPaymentReportsOfficeCountExcel(List<String> header,
			Optional<ReportsVO> resultList) {

		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();


		for (ReportVO report : resultList.get().getReport()) {
			
			List<CellProps> result = new ArrayList<CellProps>();
			
			for (int i = 0; i < header.size(); i++) {
				CellProps cells = new CellProps();

				switch (i) {
				case 0:
					cells.setFieldValue(String.valueOf(++slNo));
					break;

				case 1:
					cells.setFieldValue(report.getOfficeName());
					break;

				case 2:
					cells.setFieldValue(report.getOfficeCode());
					break;

				case 3:
					cells.setFieldValue(DateConverters.replaceDefaults(report.getCount()));
					break;

				default:
					break;
				}
				result.add(cells);

			}
			cell.add(result);
		}
		return cell;
		
	}

	private void generateNonPaymentDistrictCountExcel(HttpServletResponse response,
			Optional<ReportsVO> paymentsReport) {
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "NonPaymentDistrictWiseList");

		String name = "NonPaymentDistrictWiseList";
		String fileName = name + ".xlsx";
		String sheetname = "NonPaymentDistrictWiseList";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		XSSFWorkbook wb = excel.renderData(prepareCellPropsForNonPaymentReportsDistrictCount(header, paymentsReport), header, fileName, sheetname);

		settingValuesForExcel(response, sheetname, wb);
		
	}
	
	
	private List<List<CellProps>> prepareCellPropsForNonPaymentReportsDistrictCount(List<String> header, Optional<ReportsVO> resultList) {

		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (ReportVO report : resultList.get().getReport()) {
			List<CellProps> result = new ArrayList<CellProps>();
			for (int i = 0; i < header.size(); i++) {
				CellProps cellpro = new CellProps();
				switch (i) {
				case 0:
					cellpro.setFieldValue(String.valueOf(++slNo));
					break;

				case 1:
					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getDistrictName()));
					break;

				case 2:
					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getCount()));
					break;

				default:
					break;
				}
				result.add(cellpro);
			}
			cell.add(result);
		}
		return cell;
	}

	@Override
	public void generateNonPaymentReportVehicleDataExcelCount(Optional<ReportsVO> paymentsReport,
			HttpServletResponse response, RegReportVO regReportVO) {
		if (regReportVO.isNonPaymentVehicleWiseExcel()) {
			generateNonPaymentVehicleCountExcel(paymentsReport, response);
		}

		if (regReportVO.isNonPaymentDetailsWiseExcel()) {
			generateNonPaymentDetailsCountExcel(rcCancellationService.getNonPymentDetailsExcle(regReportVO), response,regReportVO);

		}
		
	}

	private void generateNonPaymentDetailsCountExcel(List<NonPaymentDetailsDTO> nonPaymentList, HttpServletResponse response,RegReportVO regReportVO) {
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "NonPaymentDetailsWiseList");

		String name = "NonPaymentDetailsWiseList";
		String fileName = name + ".xlsx";
		String sheetname = "NonPaymentDetailsWiseList";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);
		response.setHeader("Pragma", "public");
		response.setHeader("Cache-Control", "no-store");
		response.addHeader("Cache-Control", "max-age=0");

		XSSFWorkbook wb = excel.renderData(
				prepareCellPropsForNonPaymentReportsDetailsCountExcel(header, nonPaymentList,regReportVO), header, fileName,
				sheetname);

		settingValuesForExcel(response, sheetname, wb);

	}
	
private List<List<CellProps>> prepareCellPropsForNonPaymentReportsDetailsCountExcel(List<String> header, List<NonPaymentDetailsDTO> nonPaymentList,RegReportVO regReportVO) {
		
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (NonPaymentDetailsDTO nonPayDet : nonPaymentList) {
			if(nonPayDet != null) {
			List<CellProps> result = new ArrayList<CellProps>();
			for (int i = 0; i < header.size(); i++) {
				CellProps cells = new CellProps();

				switch (i) {
				case 0:
					cells.setFieldValue(String.valueOf(++slNo));
					break;
				case 1:
					cells.setFieldValue(nonPayDet.getPrNo());
					break;
				case 2:
					cells.setFieldValue(rcCancellationService.setCovWithWeight(nonPayDet.getCov(),regReportVO));
					break;
				case 3:
					cells.setFieldValue(DateConverters.replaceDefaults(nonPayDet.getTaxValidity()));
					break;
				case 4:
					cells.setFieldValue(nonPayDet.getOwnerName());
					break;
				case 5:
					cells.setFieldValue(nonPayDet.getOwnerAddress());
					break;
				case 6:
					cells.setFieldValue(nonPayDet.getMandalName());
					break;
				case 7:
					cells.setFieldValue(nonPayDet.getFinancerName());
					break;
				case 8:
					cells.setFieldValue(nonPayDet.getFinancerAddress());
					break;
				case 9:
					cells.setFieldValue(DateConverters.replaceDefaults(nonPayDet.getFcValidity()));
					break;
				case 10:
					cells.setFieldValue(DateConverters.replaceDefaults(nonPayDet.getPermitValidity()));
					break;
				case 11:
					cells.setFieldValue(DateConverters.replaceDefaults(nonPayDet.getGvw()));
					break;
				case 12:
					cells.setFieldValue(nonPayDet.getMobileNo());
					break;
				default:
					break;
				}
				result.add(cells);

			}
			cell.add(result);
		}
		}
		return cell;
		
	}

	private void generateNonPaymentVehicleCountExcel(Optional<ReportsVO> paymentsReport, HttpServletResponse response) {
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "NonPaymentVehicleWiseList");

		String name = "NonPaymentVehicleWiseList";
		String fileName = name + ".xlsx";
		String sheetname = "NonPaymentVehicleWiseList";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);
		response.setHeader("Pragma", "public");
		response.setHeader("Cache-Control", "no-store");
		response.addHeader("Cache-Control", "max-age=0");

		XSSFWorkbook wb = excel.renderData(
				prepareCellPropsForNonPaymentReportsVehicleCountExcel(header, paymentsReport), header, fileName,
				sheetname);

		settingValuesForExcel(response, sheetname, wb);

	}
	
	private List<List<CellProps>> prepareCellPropsForNonPaymentReportsVehicleCountExcel(List<String> header, Optional<ReportsVO> resultList) {
		
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (ReportVO report : resultList.get().getNonPaymentDetails().stream().findFirst().get().getCovReport()) {
			List<CellProps> result = new ArrayList<CellProps>();
			for (int i = 0; i < header.size(); i++) {
				CellProps cells = new CellProps();

				switch (i) {
				case 0:
					cells.setFieldValue(String.valueOf(++slNo));
					break;
				case 1:
					cells.setFieldValue(report.getCovDesc());
					break;
				case 2:
					cells.setFieldValue(DateConverters.replaceDefaults(report.getCovCount()));
				default:
					break;
				}
				result.add(cells);

			}
			cell.add(result);
		}
		return cell;
		
	}

	@Override
	public void generateNonPaymentReportCovWiseCountExcel(HttpServletResponse response,
			Optional<ReportsVO> paymentsReport, RegReportVO regReportVO) {
		if (regReportVO.isNonPaymentCovMandalWiseWiseExcel()) {
			generateNonPaymentCovMandalCountExcel(paymentsReport, response);
		}
	}

	private void generateNonPaymentCovMandalCountExcel(Optional<ReportsVO> paymentsReport,
			HttpServletResponse response) {
		
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "NonPaymentCovMandalWiseList");

		String name = "NonPaymentCovMandalWiseList";
		String fileName = name + ".xlsx";
		String sheetname = "NonPaymentCovMandalWiseList";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);

		XSSFWorkbook wb = excel.renderData(
				prepareCellPropsForNonPaymentReportsCovMandalCountExcel(header, paymentsReport), header, fileName,
				sheetname);

		settingValuesForExcel(response, sheetname, wb);
	}

	private List<List<CellProps>> prepareCellPropsForNonPaymentReportsCovMandalCountExcel(List<String> header,
			Optional<ReportsVO> resultList) {
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (ReportVO report : resultList.get().getReport()) {
			if(report != null) {
			List<CellProps> result = new ArrayList<CellProps>();
			for (int i = 0; i < header.size(); i++) {
				CellProps cells = new CellProps();

				switch (i) {
				case 0:
					cells.setFieldValue(String.valueOf(++slNo));
					break;
				case 1:
					if(report.getMandalName() != null) {
					cells.setFieldValue(report.getMandalName());
					}
					break;
				case 2:
					if(report.getCount() >= 0) {
					cells.setFieldValue(DateConverters.replaceDefaults(report.getCount()));
					}
				default:
					break;
				}
				result.add(cells);

			}
			cell.add(result);
		}
		}
		return cell;
	}

	@Override
	public void generateExcelForContractCarriage(TrIssuedReportVO resultList, HttpServletResponse response,
			RegReportVO regVO) {
		// TODO Auto-generated method stub
		if (regVO.isContractCarriageExcelCount()) {
			generateExcelForContractCarriageCount(resultList, response, regVO);
		} else if (regVO.isContractCarriageExcelList()) {
			generateExcelForContractCarriageList(resultList, response, regVO);
		}
	}

	private void generateExcelForContractCarriageCount(TrIssuedReportVO resultList, HttpServletResponse response,
			RegReportVO regVO) {

		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "ContractCarriagePermitsCount");

		String name = "ContractCarriagePermitsCount";
		String fileName = name + ".xlsx";
		String sheetName = "ContractCarriagePermitsCount";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

		XSSFWorkbook wb = excel.renderData(prepareCellPropsForContractCarriageCountExcel(header, resultList), header,
				fileName, sheetName);

		settingValuesForExcel(response, sheetName, wb);
	}

	private List<List<CellProps>> prepareCellPropsForContractCarriageCountExcel(List<String> header,
			TrIssuedReportVO resultList) {
		// TODO Auto-generated method stub
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();
		if (resultList != null) {
			for (CitizenApplicationSearchResponceVO resultList1 : resultList.getResultList()) {
				List<CellProps> result = new ArrayList<CellProps>();
				for (int i = 0; i < header.size(); i++) {
					CellProps cells = new CellProps();

					switch (i) {
					case 0:
						cells.setFieldValue(String.valueOf(++slNo));
						break;
					case 1:
						if (resultList1.getPermitType() != null) {
							cells.setFieldValue(DateConverters.replaceDefaults(resultList1.getPermitType()));
						}
						break;
					case 2:
						if (resultList1.getCount() >= 0) {
							cells.setFieldValue(DateConverters.replaceDefaults(resultList1.getCount()));
						}
					default:
						break;
					}
					result.add(cells);

				}
				cell.add(result);
			}

		}
		cell.add(prepareMapRowCellToContractCarriageCount(header, resultList));
		return cell;
	}

	private List<CellProps> prepareMapRowCellToContractCarriageCount(List<String> header, TrIssuedReportVO resultList) {
		// TODO Auto-generated method stub
		List<CellProps> cells = new ArrayList<>();
		for (int i = 0; i < header.size(); i++) {
			CellProps cellpro = new CellProps();
			switch (i) {
			case 0:
				cellpro.setFieldValue("TOTAL");
				break;
			case 1:
				cellpro.setFieldValue(StringUtils.EMPTY);
				break;
			case 2:
				if(resultList.getTotal()>=0) {
				cellpro.setFieldValue(DateConverters.replaceDefaults(resultList.getTotal()));
				}
			default:
				break;
			}
			cells.add(cellpro);
		}
		return cells;
	}

	@Override
	public void generateExcelForEvcrDetailReports(Map<String, Long> regReport, RegReportVO regReportVo,
			HttpServletResponse response) {
		
		if(regReportVo.isEvcrReportExcelCount()) {
			generateEvcrReportDetailExcelCount(regReport,response,regReportVo);
		}
		
	}

	private void generateEvcrReportDetailExcelCount(Map<String, Long> regReport, HttpServletResponse response, RegReportVO regReportVo) {
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "EvcrReportDetail");

		String name = "EvcrReportDetail";
		String fileName = name + ".xlsx";
		String sheetname = "EvcrReportDetail";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);
		
		XSSFWorkbook wb = excel.renderData(prepareCellPropsForEvcrReportDetailExcel(header, regReport, regReportVo), header, fileName,
				sheetname);
		
		settingValuesForExcel(response, sheetname, wb);
	}

	private List<List<CellProps>> prepareCellPropsForEvcrReportDetailExcel(List<String> header, Map<String, Long> resultList,
			RegReportVO regReportVo) {
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (Entry<String, Long> report : resultList.entrySet()) {
			if (report != null) {
				List<CellProps> result = new ArrayList<CellProps>();
				for (int i = 0; i < header.size(); i++) {
					CellProps cellpro = new CellProps();
					switch (i) {
					case 0:
						cellpro.setFieldValue(String.valueOf(++slNo));
						break;
					case 1:
						if (report.getKey() != null) {
							cellpro.setFieldValue(report.getKey());
						}
						break;
					case 2:
						if (report.getValue() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(report.getValue()));
						}
						break;
					default:
						break;
					}
					result.add(cellpro);
				}
				cell.add(result);

			}
		}
		cell.add(mapToRowCellForEvcrDetailReport(header, resultList, regReportVo));
		return cell;
	}

	private List<CellProps> mapToRowCellForEvcrDetailReport(List<String> header, Map<String, Long> resultList,
			RegReportVO regReportVo) {

		List<CellProps> result = new ArrayList<CellProps>();

		for (int i = 0; i < header.size(); i++) {
			CellProps cellpro = new CellProps();
			switch (i) {
			case 0:
				cellpro.setFieldValue(StringUtils.EMPTY);
				break;
			case 1:
				cellpro.setFieldValue("Total");
				break;
			case 2:
				if (regReportVo.getEvcrDetailCount() != null) {
					cellpro.setFieldValue(DateConverters.replaceDefaults(regReportVo.getEvcrDetailCount()));
				}
				break;
			default:
				break;
			}
			result.add(cellpro);
		}
		return result;
	}

	@Override
	public void generateExcelForFitnessData(HttpServletResponse response, List<FitnessReportsDemoVO> fitnessReport,
			RegReportDuplicateVO regVO) {
		
		if(regVO.isFitnessCertIssueExcelCount()) {
			generateExcelForFitnessCertIssue(fitnessReport, response);
		}
		
	}

	private void generateExcelForFitnessCertIssue(List<FitnessReportsDemoVO> fitnessReport,
			HttpServletResponse response) {
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "FitnessCertIssue");

		String name = "FitnessCertIssue";
		String fileName = name + ".xlsx";
		String sheetname = "FitnessCertIssue";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);
		
		XSSFWorkbook wb = excel.renderData(prepareCellPropsForFitnessCertIssueExcel(header, fitnessReport), header, fileName,
				sheetname);
		
		settingValuesForExcel(response, sheetname, wb);
		
	}

	private List<List<CellProps>> prepareCellPropsForFitnessCertIssueExcel(List<String> header,
			List<FitnessReportsDemoVO> resultList) {
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (FitnessReportsDemoVO report : resultList) {
			if (report != null) {
				List<CellProps> result = new ArrayList<CellProps>();
				for (int i = 0; i < header.size(); i++) {
					CellProps cellpro = new CellProps();
					switch (i) {
					case 0:
						cellpro.setFieldValue(String.valueOf(++slNo));
						break;
					case 1:
						if(report.getPrNo() != null) {
						cellpro.setFieldValue(report.getPrNo());
						}
						break;
					case 2:
						if(report.getClassOfVehicle() != null) {
						cellpro.setFieldValue(report.getClassOfVehicle());
						}
						break;
					case 3:
						if(report.getFcIssuedDate() != null) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getFcIssuedDate()));
						}
						break;
					case 4:
						if(report.getFcValidUpto() !=  null) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getFcValidUpto()));
						}
						break;
					case 5:
						if(report.getUserId() != null) {
						cellpro.setFieldValue(report.getUserId());
						}
						break;
					case 6:
						if(report.getFctype() != null) {
						cellpro.setFieldValue(report.getFctype());
						}
					default:
						break;
					}
					result.add(cellpro);
				}
				cell.add(result);
			}
		}
		return cell;
	}

	@Override
	public void generateExcelForEvcrDetailReportsData(List<VcrFinalServiceVO> regReport, RegReportVO regReportVo,
			HttpServletResponse response) {
		if(regReportVo.isEvcrReportDetailDataExcelCount()) {
			generateEvcrReportDetailDataExcelCount(regReport,response,regReportVo);
		}
	}

	private void generateEvcrReportDetailDataExcelCount(List<VcrFinalServiceVO> regReport, HttpServletResponse response,
			RegReportVO regReportVo) {
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "EvcrReportDetailData");

		String name = "EvcrReportDetailData";
		String fileName = name + ".xlsx";
		String sheetname = "EvcrReportDetailData";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);
		
		XSSFWorkbook wb = excel.renderData(prepareCellPropsForEvcrReportDetailDataExcel(header, regReport, regReportVo), header, fileName,
				sheetname);
		
		settingValuesForExcel(response, sheetname, wb);
	}

	private List<List<CellProps>> prepareCellPropsForEvcrReportDetailDataExcel(List<String> header,
			List<VcrFinalServiceVO> resultList, RegReportVO regReportVo) {
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (VcrFinalServiceVO report : resultList) {
			if (report != null) {
				List<CellProps> result = new ArrayList<CellProps>();
				for (int i = 0; i < header.size(); i++) {
					CellProps cellpro = new CellProps();
					switch (i) {
					case 0:
						cellpro.setFieldValue(String.valueOf(++slNo));
						break;
					case 1:
						cellpro.setFieldValue(report.getRegistration().getRegNo());
						break;
					case 2:
						cellpro.setFieldValue(report.getOfficeName());
						break;
					case 3:
						cellpro.setFieldValue(report.getOwnerDetails().getFullName());
						break;
					case 4:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getVcr().getDateOfCheck()));
						break;
					case 5:
						cellpro.setFieldValue(report.getRegistration().getClasssOfVehicle().getCovdescription());
						break;
					case 6:
						cellpro.setFieldValue("Not Paid");
						break;
					case 7:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getCompoundFee()));
					default:
						break;
					}
					result.add(cellpro);
				}
				cell.add(result);

			}
		}
		return cell;
	}

	@Override
	public void generateExcelForVcrPaymentPaidDate(RegReportVO report, HttpServletResponse response, VcrVo reportVO) {
		if(reportVO.isVcrPaidDataReportsExcelList()) {
			generateExcelListForVcrPaidDataReports(report,response);
		}
		
		if(reportVO.isVcrPaidDataReportsVehicleDetailsExcelList()) {
			generateExcelListForVcrPaidDataReportsVehicleDetails(report, response);
		}
	}
	private void generateExcelListForVcrPaidDataReports(RegReportVO report, HttpServletResponse response) {
		List<String> header = new ArrayList<>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "VcrPaymentReportsPaidData");

		String filename = "VcrPaymentReportsPaidData" + ".xlsx";
		String sheetname = "VcrPaymentReportsPaidData";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + filename);
		
		XSSFWorkbook wb = excel.renderData(prepareCellsForVcrPaymentReportsPaidData(header, report), header, filename,
				sheetname);

		settingValuesForExcel(response, sheetname, wb);

	}
	
	private List<List<CellProps>> prepareCellsForVcrPaymentReportsPaidData(List<String> header,
			RegReportVO resultList) {

		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<>();

		for (UserWiseEODCount userWiseEod : resultList.getVcrReport().stream().findFirst().get().getMviCounts()) {

			List<CellProps> result = new ArrayList<CellProps>();
			for (int i = 0; i < header.size(); i++) {
				CellProps cells = new CellProps();

				switch (i) {
				case 0:
					cells.setFieldValue(String.valueOf(++slNo));
					break;
				case 1:
					cells.setFieldValue(userWiseEod.getUserName());
					break;
				case 2:
					cells.setFieldValue(DateConverters.replaceDefaults(userWiseEod.getCount()));
				default:
					break;
				}

				result.add(cells);
			}
			cell.add(result);
		}
		return cell;
	}
	
	private void generateExcelListForVcrPaidDataReportsVehicleDetails(RegReportVO report,
			HttpServletResponse response) {
		ExcelService excel = new ExcelServiceImpl();

		List<String> headers = new ArrayList<>();

		excel.setHeaders(headers, "VcrPaymentReportsPaidDataDetails");

		String filename = "VcrPaymentReportsPaidDataDetails" + ".xlsx";
		String sheetname = "VcrPaymentReportsPaidDataDetails";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename =" + filename);

		XSSFWorkbook wb = excel.renderData(prepareCellsForVcrPaymentReportsPaidDataDetails(headers, report), headers,
				filename, sheetname);

		settingValuesForExcel(response, sheetname, wb);
	}
	
	private List<List<CellProps>> prepareCellsForVcrPaymentReportsPaidDataDetails(List<String> header,
			RegReportVO resultlist) {

		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<>();

		for (VcrFinalServiceVO vcrReport : resultlist.getVcrReport()) {
			if(vcrReport != null) {
			List<CellProps> result = new ArrayList<CellProps>();
			for (int i = 0; i < header.size(); i++) {
				CellProps cells = new CellProps();

				switch (i) {
				case 0:
					cells.setFieldValue(String.valueOf(++slNo));
					break;
				case 1:
					if(vcrReport.getRegistration() != null && vcrReport.getRegistration().getRegNo() != null) {
					cells.setFieldValue(vcrReport.getRegistration().getRegNo());
					}
					break;
				case 2:
					if(vcrReport.getVcr() != null && vcrReport.getVcr().getVcrNumber() != null) {
					cells.setFieldValue(vcrReport.getVcr().getVcrNumber());
					}
					break;
				case 3:
					if(vcrReport.getRegistration() != null && vcrReport.getRegistration().getClasssOfVehicle().getCovdescription() != null) {
					cells.setFieldValue(vcrReport.getRegistration().getClasssOfVehicle().getCovdescription());
					}
					break;
				case 4:
					if(vcrReport.getChallanNo() != null) {
					cells.setFieldValue(DateConverters.replaceDefaults(vcrReport.getChallanNo()));
					}
					break;
				case 5:
					if(vcrReport.getVcr() != null && vcrReport.getVcr().getDateOfCheck() != null) {
					cells.setFieldValue(DateConverters.replaceDefaults(vcrReport.getVcr().getDateOfCheck()));
					}
					break;
				case 6:
					if(vcrReport.getChallanDate() != null) {
					cells.setFieldValue(DateConverters.replaceDefaults(vcrReport.getChallanDate()));
					}
					break;
				case 7:
					if(vcrReport.getActionTaken() != null) {
					cells.setFieldValue(vcrReport.getActionTaken());
					}
					break;
				case 8:
					if(vcrReport.getPaidDate() != null) {
					cells.setFieldValue(DateConverters.replaceDefaults(vcrReport.getPaidDate()));
					}
					break;
				case 9:
					if(vcrReport.getIssuedBy() != null) {
					cells.setFieldValue(vcrReport.getIssuedBy());
					}
					break;
				case 10:
					if(vcrReport.getRecieptNo() != null) {
					cells.setFieldValue(vcrReport.getRecieptNo());
					}
					break;
				case 11:
					if(vcrReport.getVcrStatus() != null) {
					cells.setFieldValue(vcrReport.getVcrStatus());
					}
					break;
				case 12:
					if(vcrReport.getCompoundFee() != null) {
					cells.setFieldValue(DateConverters.replaceDefaults(vcrReport.getCompoundFee()));
					}
					break;
				case 13:
					if(vcrReport.getServiceFee() != null) {
					cells.setFieldValue(DateConverters.replaceDefaults(vcrReport.getServiceFee()));
					}
					break;
				case 14:
					if(vcrReport.getTax() != null) {
					cells.setFieldValue(DateConverters.replaceDefaults(vcrReport.getTax()));
					}
					break;
				case 15:
					if(vcrReport.getTaxArrears() != null) {
					cells.setFieldValue(DateConverters.replaceDefaults(vcrReport.getTaxArrears()));
					}
					break;
				case 16:
					if(vcrReport.getPenalty() != null) {
					cells.setFieldValue(DateConverters.replaceDefaults(vcrReport.getPenalty()));
					}
					break;
				case 17:
					if(vcrReport.getPenaltyArrears() != null) {
					cells.setFieldValue(DateConverters.replaceDefaults(vcrReport.getPenaltyArrears()));
					}
					break;
				case 18:
					if(vcrReport.getFineCollected() != null) {
					cells.setFieldValue(DateConverters.replaceDefaults(vcrReport.getFineCollected()));
					}
					break;
				case 19:
					if(vcrReport.getTotal() >= 0) {
					cells.setFieldValue(DateConverters.replaceDefaults(vcrReport.getTotal()));
					}
					break;
				case 20:
					if(vcrReport.getStatus() != null) {
					cells.setFieldValue(DateConverters.replaceDefaults(vcrReport.getStatus()));
					}
				default:
					break;
				}

				result.add(cells);
			}
			cell.add(result);
		}
		}
		return cell;
	}
}
