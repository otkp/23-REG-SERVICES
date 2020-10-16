package org.epragati.reports.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.epragati.excel.service.CellProps;
import org.epragati.excel.service.ExcelService;
import org.epragati.excel.service.ExcelServiceImpl;
import org.epragati.master.dao.ShowCauseDetailsDAO;
import org.epragati.master.dto.ShowCauseDetailsDTO;
import org.epragati.master.mappers.ShowCauseDetailsMapper;
import org.epragati.master.mappers.ShowCauseSectionMapper;
import org.epragati.payment.report.vo.ShowCauseReportVo;
import org.epragati.reports.service.ShowCauseService;
import org.epragati.util.DateConverters;
import org.epragati.util.Status;
import org.epragati.vcrImage.dao.VcrFinalServiceDAO;
import org.epragati.vcrImage.mapper.VcrFinalServiceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ShowCauseServiceImpl implements ShowCauseService {
	
	public static final Logger logger=LoggerFactory.getLogger(ShowCauseServiceImpl.class);

	@Autowired
	VcrFinalServiceDAO vcrFinalServiceDAO;

	@Autowired
	ShowCauseDetailsDAO showCauseDetailsDAO;

	@Autowired
	ShowCauseSectionMapper showCauseSectionMapper;

	@Autowired
	ShowCauseDetailsMapper showCauseDetailsMapper;

	@Autowired
	VcrFinalServiceMapper vcrFinalServiceMapper;

	@Override
	public List<ShowCauseReportVo> getShowCauseReport(ShowCauseReportVo input, String officeCode) {

		LocalDateTime fromDate = getTimewithDate(input.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(input.getToDate(), true);
		List<ShowCauseReportVo> output = new ArrayList<>();
		List<ShowCauseDetailsDTO> resultList = showCauseDetailsDAO.findByOfficeCodeAndScIssuedDateBetween(officeCode,
				fromDate, toDate);
		Map<String, List<ShowCauseDetailsDTO>> covWiseMap = resultList.stream()
				.collect(Collectors.groupingBy(ShowCauseDetailsDTO::getCov));
		
		
		covWiseMap.forEach((key, value) -> {
			ShowCauseReportVo vo = new ShowCauseReportVo();
			vo.setCov(key);
			value.forEach(item -> {
				// RC CANCELLED
				if (item.getScStatus().equalsIgnoreCase(Status.ShowCauseEnum.RCCANELLED.getSectionCode())) {
					vo.setRegCancelled(vo.getRegCancelled() + 1);
				} // UNDER SECTION55
				else if (null != item.getScType() && item.getScType().getSectionCode() != null && item.getScType()
						.getSectionCode().equalsIgnoreCase(Status.ShowCauseEnum.UNDERSECTION55.getSectionCode())) {
					vo.setUnderSection55(vo.getUnderSection55() + 1);
				} // MORE THAN 5QRTS
				else if (null != item.getScType() && item.getScType().getSectionCode() != null && item.getScType()
						.getSectionCode().equalsIgnoreCase(Status.ShowCauseEnum.MORETHAN5QRTS.getSectionCode())) {
					vo.setMoreThan5Quarters(vo.getMoreThan5Quarters() + 1);
				} // UNDER RULE12A
				else if (null != item.getScType() && item.getScType().getSectionCode() != null && item.getScType()
						.getSectionCode().equalsIgnoreCase(Status.ShowCauseEnum.UNDERRULE12A.getSectionCode())) {
					vo.setUnderRule12A(vo.getUnderRule12A() + 1);
				} // UNDER RULE6
				else if (null != item.getScType() && item.getScType().getSectionCode() != null && item.getScType()
						.getSectionCode().equalsIgnoreCase(Status.ShowCauseEnum.UNDERRULE6.getSectionCode())) {
					vo.setUnderRule12A(vo.getUnderRule12A() + 1);
				} // UNDER SECTION7A
				else if (null != item.getScType() && item.getScType().getSectionCode() != null && item.getScType()
						.getSectionCode().equalsIgnoreCase(Status.ShowCauseEnum.UNDERSECTION7A.getSectionCode())) {
					vo.setUnderSection7(vo.getUnderSection7() + 1);
				}
				// SHOW CAUSE ISSUED
				vo.setShowCauseIssued(value.size());
				// NON PAYMENT
				//vo.setNonPaymentCount();

				// VEHICLES PAID TAX AFTER SC ISSUED
				vo.setVehiclesPaidTaxAfterScIssued(vo.getNonPaymentCount());

				// TOTAL AMOUNT COLLECTED
				//vo.setTotalAmountCollected(10000);
			});
			output.add(vo);
		});
		return output;
	}

	


	
	


	private LocalDateTime getTimewithDate(LocalDate date, Boolean timeZone) {

		String dateVal = date + "T00:00:00.000Z";
		if (timeZone) {
			dateVal = date + "T23:59:59.999Z";
		}
		ZonedDateTime zdt = ZonedDateTime.parse(dateVal);
		return zdt.toLocalDateTime();
	}
	
	
	private void updateShowCauseDetailsAfterPaymentDone(ShowCauseDetailsDTO dto) {
		
		dto.setPaymentDoneDate(LocalDateTime.now());
		
		dto.setTaxPaidAmount(1000.00);
		
		showCauseDetailsDAO.save(dto);
		
	}


	@Override
	public void generateShowCauseExcelReport(HttpServletResponse response, ShowCauseReportVo input, String officeCode) {
		
		List<ShowCauseReportVo> resultList = getShowCauseReport(input, officeCode);

		List<String> header = new ArrayList<String>();
		
		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "ShowCauseReport");

		String name = "ShowCauseReport";
		String fileName = name + ".xlsx";
		String sheetName = "ShowCauseReport";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		response.setHeader("Pragma", "public");
		response.setHeader("Cache-Control", "no-store");
		response.addHeader("Cache-Control", "max-age=0");

		XSSFWorkbook wb = excel.renderData(prepareCellProps(header, resultList), header, fileName,
				sheetName);

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
			logger.error(" excel retriveAllRecords download IOException : {}",e.getMessage());
		}

		
	}
	
	private List<List<CellProps>> prepareCellProps(List<String> header,
			List<ShowCauseReportVo> resultList) {

		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();
		List<CellProps> result = new ArrayList<CellProps>();
		for (ShowCauseReportVo report : resultList) {
			for (int i = 0; i < header.size(); i++) {
				CellProps cellpro = new CellProps();
				switch (i) {
				case 0:
					cellpro.setFieldValue(String.valueOf(++slNo));
					break;
				case 1:
					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getCov()));
					break;

				case 2:
					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getNonPaymentCount()));
					break;
				case 3:

					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getUnderSection55()));
					break;

				case 4:

					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getMoreThan5Quarters()));
					break;

				case 5:

					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getUnderRule12A()));
					break;

				case 6:

					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getUnderRule6()));
					break;

				case 7:

					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getUnderSection7()));
					break;

				case 8:
					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getShowCauseIssued()));
					break;

				case 9:
					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getRegCancelled()));
					break;
				case 10:
					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getVehiclesPaidTaxAfterScIssued()));
					break;

				case 11:
					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getTotalAmountCollected()));
					break;

				default:
					break;
				}
				result.add(cellpro);
			}
		}
		cell.add(result);
		return cell;
	}

}
