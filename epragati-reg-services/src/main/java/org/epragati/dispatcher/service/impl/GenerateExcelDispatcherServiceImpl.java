package org.epragati.dispatcher.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.epragati.dispatcher.dao.DispatcherSubmissionDAORepo;
import org.epragati.dispatcher.dto.DispatcherSubmissionDTO;
import org.epragati.dispatcher.mapper.DispatcherMapper;
import org.epragati.dispatcher.service.GenerateExcelDispatcherService;
import org.epragati.excel.service.CellProps;
import org.epragati.excel.service.ExcelService;
import org.epragati.excel.service.ExcelServiceImpl;
import org.epragati.util.AppMessages;
import org.epragati.util.DateConverters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class GenerateExcelDispatcherServiceImpl implements GenerateExcelDispatcherService {

	private static final Logger logger = LoggerFactory.getLogger(GenerateExcelDispatcherServiceImpl.class);
	
	@Autowired
	AppMessages appMessages;
	@Autowired
	DispatcherMapper mapper;

	@Autowired
	DispatcherSubmissionDAORepo dispatcherSubmissionDAORepo;

	@Override
	public void retriveAllRecords(HttpServletResponse response,String officeCode,String fromDates, String toDates) {
		Map<String, LocalDateTime> dates = mapper.stringToDateConvertor(fromDates, toDates);
		LocalDateTime fromDate = dates.get("from");
		LocalDateTime toDate = dates.get("to");
		List<DispatcherSubmissionDTO> dispatcherSubmissionDTOs = dispatcherSubmissionDAORepo.findByPostedDateBetweenAndOfficeCode(fromDate, toDate,
				officeCode);

		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "EmsReport");

		String name = "EMS_Details";
		String fileName = name + ".xlsx";
		String sheetName = "EMSReport";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		response.setHeader("Pragma", "public");
		response.setHeader("Cache-Control", "no-store");
		response.addHeader("Cache-Control", "max-age=0");

		XSSFWorkbook wb = excel.renderData(prepareCellProps(header, dispatcherSubmissionDTOs), header, fileName,
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
			List<DispatcherSubmissionDTO> dispatcherSubmissionDTOs) {

		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();
		List<CellProps> result = new ArrayList<CellProps>();
		for (DispatcherSubmissionDTO dispatch : dispatcherSubmissionDTOs) {
			for (int i = 0; i < header.size(); i++) {
				CellProps cellpro = new CellProps();
				switch (i) {
				case 0:
					cellpro.setFieldValue(String.valueOf(++slNo));
					break;
				case 1:
					cellpro.setFieldValue(DateConverters.replaceDefaults(dispatch.getApplicationNo()));
					break;

				case 2:
					cellpro.setFieldValue(DateConverters.replaceDefaults(dispatch.getPrNo()));
					break;
				case 3:

					cellpro.setFieldValue(DateConverters.replaceDefaults(dispatch.getOfficeCode()));
					break;

				case 4:

					cellpro.setFieldValue(DateConverters.replaceDefaults(dispatch.getName()));
					break;

				case 5:
					cellpro.setFieldValue(DateConverters.replaceDefaults(dispatch.getEmsNumber()));
					break;

				case 6:
					cellpro.setFieldValue(DateConverters.replaceDefaults(dispatch.getPostedDate()));
					break;

				case 7:
					cellpro.setFieldValue(DateConverters.replaceDefaults(dispatch.getDispatchedBy()));
					break;

				case 8:
					cellpro.setFieldValue(DateConverters.replaceDefaults(dispatch.getMobileNo()));
					break;
				case 9:
					cellpro.setFieldValue(DateConverters.replaceDefaults(dispatch.getPinCode()));
					break;
				case 10:
					cellpro.setFieldValue(DateConverters.replaceDefaults(dispatch.getRemarks()));
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
