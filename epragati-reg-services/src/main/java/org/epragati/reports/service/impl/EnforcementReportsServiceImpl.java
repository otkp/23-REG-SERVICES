package org.epragati.reports.service.impl;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.epragati.excel.service.CellProps;
import org.epragati.excel.service.ExcelService;
import org.epragati.excel.service.ExcelServiceImpl;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dto.OffenceDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.service.OfficeService;
import org.epragati.payment.report.vo.ReportsVO;
import org.epragati.reports.service.EnforcementReports;
import org.epragati.reports.service.PaymentReportService;
import org.epragati.rta.reports.vo.ReportVO;
import org.epragati.vcr.service.VcrService;
import org.epragati.vcr.vo.VcrFinalServiceVO;
import org.epragati.vcr.vo.VcrVo;
import org.epragati.vcrImage.dao.VcrFinalServiceDAO;
import org.epragati.vcrImage.dto.VcrFinalServiceDTO;
import org.epragati.vcrImage.mapper.VcrFinalServiceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

@Service
public class EnforcementReportsServiceImpl implements EnforcementReports {

	private static final Logger logger = LoggerFactory.getLogger(EnforcementReportsServiceImpl.class);

	@Autowired
	private VcrService vcrService;

	@Autowired
	private OfficeService officeService;

	@Autowired
	private PaymentReportService paymentReportService;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private VcrFinalServiceDAO vcrFinalServiceDAO;

	@Autowired
	private VcrFinalServiceMapper vcrFinalServiceMapper;

	@Override
	public List<VcrFinalServiceVO> seizedEnforcementReport(String user, String officeCode, VcrVo vcrVO) {

		LocalDateTime fromDate = vcrService.getTimewithDate(vcrVO.getFromDate(), false);
		LocalDateTime toDate = vcrService.getTimewithDate(vcrVO.getToDate(), true);
		List<String> officeCodeList;
		if (vcrVO.getOfficeCode().equals("ALL")) {
			OfficeDTO officeDTO = officeService.getDistrictByofficeCode(officeCode);
			officeCodeList = paymentReportService.getOfficeCodes().get(officeDTO.getDistrict());
			if (CollectionUtils.isEmpty(officeCodeList)) {
				throw new BadRequestException(
						"No office Found for user: " + user + " District: " + officeDTO.getDistrict());
			}

		} else {
			officeCodeList = Arrays.asList(vcrVO.getOfficeCode());
		}

		if (CollectionUtils.isEmpty(officeCodeList)) {
			throw new BadRequestException("office Not found");
		}
		List<VcrFinalServiceDTO> vcrList = vcrFinalServiceDAO.nativeVcrDateAndOfficeCodeInAndVehicleSeized(officeCodeList,fromDate,
				toDate);
		List<VcrFinalServiceVO> vcrVOList = vcrFinalServiceMapper.seizedMapper(vcrList);
		return vcrVOList;

	}

	@Override
	public ReportsVO offenceEnforcementReport(String user, String officeCode, VcrVo vcrVO) {

		OfficeDTO officeDTO = officeService.getDistrictByofficeCode(officeCode);
		LocalDateTime fromDate = vcrService.getTimewithDate(vcrVO.getFromDate(), false);
		LocalDateTime toDate = vcrService.getTimewithDate(vcrVO.getToDate(), true);
		ReportsVO regReportVO = new ReportsVO();
		if (StringUtils.isEmpty(vcrVO.getOfficeCode())) {
			logger.info("offence Enforcement report  for District");
			List<String> officeCodeList = paymentReportService.getOfficeCodes().get(officeDTO.getDistrict());
			if (CollectionUtils.isEmpty(officeCodeList)) {
				throw new BadRequestException(
						"No office Found for user: " + user + " District: " + officeDTO.getDistrict());
			}
			List<ReportVO> reportList = new ArrayList<>();
			logger.info("offence Enforcement report  for officeList [{}]", officeCodeList);

			List<VcrFinalServiceDTO> vcrOffenceList = vcrFinalServiceDAO.nativeOfficeWiseOffenceCount(officeCodeList,
					fromDate, toDate);

			Map<String, Long> offenceAmountSum = vcrOffenceList.stream()
					.collect(Collectors.groupingBy(VcrFinalServiceDTO::getOfficeCode,
							Collectors.summingLong(vcr -> vcr.getOffence().getOffence().size())));
			
			
			
			convertMap(offenceAmountSum, reportList);
			reportList.stream().forEach(report -> {
				if (paymentReportService.getOfficeCodesByOfficeName().get(report.getOfficeCode()) != null) {
					report.setOfficeName(paymentReportService.getOfficeCodesByOfficeName().get(report.getOfficeCode()));
				}
			});
			regReportVO.setReport(reportList);
			regReportVO.setTotal(
					reportList.stream().map(report -> report.getOffenceCount()).mapToLong(Long::longValue).sum());
		} else {
			List<VcrFinalServiceDTO> vcrList = vcrFinalServiceDAO
					.findFixedAmountsSum(Arrays.asList(vcrVO.getOfficeCode()), fromDate, toDate);
			logger.info("offence Enforcement report  for officeCode [{}]", vcrVO.getOfficeCode());
			List<OffenceDTO> offenceList = vcrList.stream().map(map -> map.getOffence().getOffence())
					.flatMap(x -> x.stream()).collect(Collectors.toList());

			List<OffenceDTO> filteredList = offenceList.stream()
					.filter(val -> val.getOffenceDescription() != null && val.getFixedAmount() != null)
					.collect(Collectors.toList());

			Map<String, Long> offenceDescCount = filteredList.stream()
					.collect(Collectors.groupingBy(OffenceDTO::getOffenceDescription, Collectors.counting()));

			Map<String, Double> offenceAmountSum = filteredList.stream().collect(Collectors.groupingBy(
					OffenceDTO::getOffenceDescription, Collectors.summingDouble(OffenceDTO::getFixedAmount)));
			List<ReportVO> offenceReportList = new ArrayList<>();
			offenceDescCount.keySet().stream().forEach(key -> {
				ReportVO vo = new ReportVO();
				vo.setOffenceDesc(key);
				if (offenceDescCount.get(key) != null) {
					vo.setOffenceCount(offenceDescCount.get(key));
				}
				if (offenceAmountSum.get(key) != null) {
					vo.setAmount(offenceAmountSum.get(key));
				}
				offenceReportList.add(vo);
			});
			if (offenceReportList.isEmpty()) {
				throw new BadRequestException("No offence found for office" + vcrVO.getOfficeCode());
			}
			regReportVO.setReport(offenceReportList);
		}
		return regReportVO;
	}

	public void convertMap(Map<String, Long> offenceAmountSum, List<ReportVO> reportList) {
		offenceAmountSum.keySet().stream().forEach(key -> {
			ReportVO reportVO = new ReportVO();
			reportVO.setOfficeCode(key);
			reportVO.setOffenceCount(offenceAmountSum.get(key));
			reportList.add(reportVO);
		});
	}

	public List<ReportVO> doAggregation(LocalDateTime fromDate, LocalDateTime toDate, List<String> officeCodes,
			String groupField) {
		Aggregation agg = newAggregation(
				match(Criteria.where("vcr.dateOfCheck").gte(fromDate).lte(toDate).and("officeCode").in(officeCodes)
						.and("offence.offence.offenceDescription").exists(true)),
				group("officeCode"),
				project("offenceCount").and("officeCode").previousOperation()

		);
		AggregationResults<ReportVO> groupResults = mongoTemplate.aggregate(agg, VcrFinalServiceDTO.class,
				ReportVO.class);
		List<ReportVO> result = groupResults.getMappedResults();
		return result;
	}


	/*
	 * public Map<String, Long> dooAggregation(LocalDateTime fromDate, LocalDateTime
	 * toDate, List<String> officeCodes, String groupField) {
	 * 
	 * Aggregation agg = newAggregation(
	 * match(Criteria.where("vcr.dateOfCheck").gt(fromDate).lt(toDate).and(
	 * "officeCode").in(officeCodes)
	 * .and("offence.offence.offenceDescription").exists(true).and(
	 * "offence.offence.fixedAmount") .exists(true)), group(groupField,
	 * "offence.offence.fixedAmount").sum("offence.offence.fixedAmount").as(
	 * "fixedAmount"),
	 * project("fixedAmount").and("offenceDesc").previousOperation());
	 * 
	 * AggregationResults<ReportVO> groupResults = mongoTemplate.aggregate(agg,
	 * VcrFinalServiceDTO.class, ReportVO.class); List<ReportVO> result =
	 * groupResults.getMappedResults(); Map<String, Long> map = new HashMap<>();
	 * result.stream().forEach(offence -> offence.getOffenceDesc().forEach(off -> {
	 * if (!map.keySet().contains(off)) { map.put(off, offence.getOffenceCount()); }
	 * else { map.put(off, map.get(off) + offence.getOffenceCount()); } }));
	 * 
	 * return map; }
	 */

	
	public void generateOffenceEnforcementReportExcel(ReportsVO resultlist, HttpServletResponse response) {
		
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "OffenceEnforcementReports");

		String name = "OffenceEnforcementReports";
		String fileName = name + ".xlsx";
		String sheetname = "OffenceEnforcementReports";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);
		
		XSSFWorkbook wb = excel.renderData(prepareCellPropsForOffenceEnforcementReportsDataExcel(header, resultlist), header, fileName,
				sheetname);
		
		XSSFSheet sheet = wb.getSheet(sheetname);
		sheet.setDefaultColumnWidth(40);
		
		XSSFCellStyle style = wb.createCellStyle(); // create style
		XSSFFont font = wb.createFont();
		font.setBoldweight(XSSFFont.BOLDWEIGHT_NORMAL);// Make font bold
		font.setColor(HSSFColor.BLACK.index);
		style.setFont(font);// set it to bold
		
		try {

			ServletOutputStream outputStream = response.getOutputStream();
			wb.write(outputStream);
			outputStream.flush();
			outputStream.close();

		} catch (IOException e) {
			logger.error(" excel retrive AllRecords download IOException : {}", e.getMessage());

		}
		
	}

	private List<List<CellProps>> prepareCellPropsForOffenceEnforcementReportsDataExcel(List<String> header,
			ReportsVO resultlist) {

		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<>();

		for (ReportVO report : resultlist.getReport()) {
			if (report != null) {

				List<CellProps> cells = new ArrayList<>();

				for (int i = 0; i < header.size(); i++) {

					CellProps cellpro = new CellProps();

					switch (i) {
					case 0:
						cellpro.setFieldValue(String.valueOf(++slNo));
						break;
					case 1:
						cellpro.setFieldValue(String.valueOf(report.getOffenceDesc()));
						break;
					case 2:
						cellpro.setFieldValue(String.valueOf(report.getOffenceCount()));
						break;
					case 3:
						cellpro.setFieldValue(String.valueOf(report.getAmount()));
					default:
						break;
					}
					cells.add(cellpro);
				}
				cell.add(cells);
			}
		}
		return cell;
	}
	
}
