package org.epragati.reports.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
import org.epragati.common.dao.PropertiesDAO;
import org.epragati.common.dto.CovTypesDTO;
import org.epragati.common.dto.PropertiesDTO;
import org.epragati.constants.CommonConstants;
import org.epragati.constants.CovCategory;
import org.epragati.constants.NationalityEnum;
import org.epragati.excel.service.CellProps;
import org.epragati.excel.service.ExcelService;
import org.epragati.excel.service.ExcelServiceImpl;
import org.epragati.exception.BadRequestException;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dao.DistrictDAO;
import org.epragati.master.dao.MasterCovDAO;
import org.epragati.master.dao.MasterUsersDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.RegistrationDetailLogDAO;
import org.epragati.master.dao.StagingRegistrationDetailsDAO;
import org.epragati.master.dto.DistrictDTO;
import org.epragati.master.dto.MasterCovDTO;
import org.epragati.master.dto.MasterUsersDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.RegistrationDetailsLogDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.service.DistrictService;
import org.epragati.master.service.impl.OfficeServiceImpl;
import org.epragati.master.vo.DistrictVO;
import org.epragati.master.vo.OfficeVO;
import org.epragati.payment.report.vo.InvoiceDetailsReportVo;
import org.epragati.reports.dao.RegistrationCountReportDAO;
import org.epragati.reports.dto.RegistrationCountReportDTO;
import org.epragati.reports.service.RegistrationReportService;
import org.epragati.rta.reports.vo.ReportVO;
import org.epragati.util.DateConverters;
import org.epragati.util.RoleEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.stereotype.Service;

@Service
public class RegistrationReportServiceImpl implements RegistrationReportService {
	
	private static final Logger logger = LoggerFactory.getLogger(RegistrationReportServiceImpl.class);

	@Autowired
	private OfficeDAO officeDAO;

	@Autowired
	private DistrictDAO districtDAO;

	@Autowired
	private PropertiesDAO propertiesDAO;

	@Autowired
	private MasterCovDAO masterCov;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private RegistrationCountReportDAO registrationCountReportDAO;

	private List<DistrictVO> districtVO;

	@Autowired
	private DistrictService districtService;

	@Autowired
	private RegistrationDetailDAO regDetailsDao;

	@Autowired
	private OfficeServiceImpl officeService;

	@Autowired
	private PaymentReportsServiceImpl dateMapper;

	@Autowired
	private MasterUsersDAO masterUserDao;

	@Autowired
	private StagingRegistrationDetailsDAO stagingRegistrationDetailsDAO;

	@Autowired
	private RegistrationDetailLogDAO regDetailsLog;

	@Override
	public List<DistrictVO> getDistricts() {
		List<DistrictDTO> districtList = districtDAO.findByStateId(NationalityEnum.AP.getName());
		if (CollectionUtils.isEmpty(districtList)) {
			throw new BadRequestException("Districts Not Available");
		}
		List<DistrictVO> districts = new ArrayList<>();
		districtList.stream().forEach(action -> {
			DistrictVO vo = new DistrictVO();
			vo.setDistrictId(action.getDistrictId());
			vo.setDistrictName(action.getDistrictName());
			districts.add(vo);
		});
		return districts;
	}

	@Override
	public List<OfficeVO> getOfficeCodes(Integer districtId) {
		List<OfficeDTO> officeDTO = officeDAO.findByDistrictAndTypeIn(districtId, Arrays.asList("RTA"));
		if (CollectionUtils.isEmpty(officeDTO)) {
			throw new BadRequestException("Office Details Not Available");
		}
		List<OfficeVO> officeVO = new ArrayList<>();
		officeDTO.stream().forEach(action -> {
			OfficeVO vo = new OfficeVO();
			vo.setOfficeCode(action.getOfficeCode());
			vo.setOfficeName(action.getOfficeName());
			officeVO.add(vo);
		});
		return officeVO;
	}

	@Override
	public List<String> getVehicleType(String roleType) {
		Optional<PropertiesDTO> propDto = propertiesDAO.findByRoleTypeAndVehicleTypesStatusTrue(roleType);
		if (!propDto.isPresent()) {
			throw new BadRequestException("vehicle types not available for this role");
		}
		return propDto.get().getVehicleTypes().stream().map(e -> e.getType()).collect(Collectors.toList());
	}

	@Override
	public List<CovTypesDTO> getClassOfVehicles(List<String> category) {
		List<MasterCovDTO> dtoList = masterCov.findByCategoryIn(category);
		if (CollectionUtils.isEmpty(dtoList)) {
			throw new BadRequestException("Class of vehicles not available with given inputs");
		}
		List<CovTypesDTO> covDTO = new ArrayList<>();
		dtoList.stream().forEach(action -> {
			CovTypesDTO vo = new CovTypesDTO();
			vo.setCovCode(action.getCovcode());
			vo.setCovDesc(action.getCovdescription());
			covDTO.add(vo);
		});
		return covDTO;
	}

	@Override
	public List<ReportVO> getVehicleStrengthReport(String selectedRole, String districtId, String officeCode,
			String vehicleType, LocalDate countDate, String groupCategory, JwtUser jwtUser) {

		if (CollectionUtils.isEmpty(districtVO)) {
			districtVO = districtService.findBySid(NationalityEnum.AP.getName());
		}

		if (CommonConstants.ALL.equalsIgnoreCase(districtId)) {
			/*
			 * Wil execute for TC &DTC ID , return All distinct wise report.
			 */
			if (!Arrays.asList(RoleEnum.STA.getName(), RoleEnum.DTCIT.getName(), RoleEnum.DTC.getName())
					.contains(selectedRole)) {
				throw new BadRequestException(selectedRole + " is not an authentic role to access the services");

			}
			List<ReportVO> reportVoList = new ArrayList<ReportVO>();
			List<RegistrationCountReportDTO> registrationCountReportList = registrationCountReportDAO
					.findByCountDate(countDate);

			Map<Integer, Long> distWiseCountMap = registrationCountReportList.stream()
					.collect(Collectors.groupingBy(RegistrationCountReportDTO::getDistId,
							Collectors.summingLong(RegistrationCountReportDTO::getCount)));
			distWiseCountMap.entrySet().stream().forEach(map -> {
				ReportVO reportVo = new ReportVO();
				reportVo.setDistrictId(map.getKey());
				reportVo.setDistrictName(districtService.findDistNameFromInput(districtVO, map.getKey()));
				reportVo.setCount(map.getValue());
				reportVo.setOfficeName(officeCode);
				reportVo.setVehicleType(vehicleType);
				reportVoList.add(reportVo);

			});
			if (!reportVoList.isEmpty()) {
				reportVoList.sort((vo1, vo2) -> vo2.getDistrictName().compareTo(vo1.getDistrictName()));
			}
			return reportVoList;

		}
		if (null == districtId) {
			districtId = "0";
		}
		Integer distId = Integer.parseInt(districtId);
		if (CommonConstants.ALL.equalsIgnoreCase(officeCode)) {
			if (!Arrays.asList(RoleEnum.STA.getName(), RoleEnum.DTCIT.getName(), RoleEnum.DTC.getName())
					.contains(selectedRole)) {
				throw new BadRequestException(selectedRole + " is not an authentic role to access the services");
			}
			List<RegistrationCountReportDTO> registrationCountReportList = registrationCountReportDAO
					.findByDistIdAndCountDateOrderByOfficeCode(distId, countDate);
			return getMappedResult(registrationCountReportList, distId);
		}
		if (CommonConstants.ALL.equalsIgnoreCase(vehicleType)) {
			List<RegistrationCountReportDTO> registrationCountReportList = registrationCountReportDAO
					.findByOfficeCodeAndCountDateOrderByOfficeCode(officeCode, countDate);
			return getMappedResult(registrationCountReportList, distId);
		}
		if (groupCategory == null || CommonConstants.ALL.equalsIgnoreCase(groupCategory)) {
			Aggregation agg = registrationCountReportDAO.getGroupWiseCountAggregation(officeCode, vehicleType,
					countDate);
			return mongoTemplate.aggregate(agg, RegistrationCountReportDTO.class, ReportVO.class).getMappedResults();
		}
		Aggregation agg = registrationCountReportDAO.getAllfiledsWiseCountAggregation(officeCode, vehicleType,
				countDate, groupCategory);
		return mongoTemplate.aggregate(agg, RegistrationCountReportDTO.class, ReportVO.class).getMappedResults();
	}

	private List<ReportVO> getMappedResult(List<RegistrationCountReportDTO> registrationCountReportList,
			Integer distId) {

		List<ReportVO> reportVoList = new ArrayList<ReportVO>();
		Map<String, List<RegistrationCountReportDTO>> registrationCountReportMAP = registrationCountReportList.stream()
				.collect(Collectors.groupingBy(RegistrationCountReportDTO::getOfficeName));
		registrationCountReportMAP.entrySet().stream().forEach(map -> {
			ReportVO reportVo = new ReportVO();
			reportVo.setDistrictId(distId);
			reportVo.setDistrictName(districtService.findDistNameFromInput(districtVO, distId));
			reportVo.setTransportCount(0l);
			reportVo.setNonTransportCount(0l);
			map.getValue().stream().forEach(r -> {
				if (CovCategory.T.getCode().equals(r.getVehicleType())) {
					reportVo.setTransportCount(Long.sum(reportVo.getTransportCount(), r.getCount()));
				} else {
					reportVo.setNonTransportCount(Long.sum(reportVo.getNonTransportCount(), r.getCount()));
				}
				if (reportVo.getOfficeName() == null) {
					reportVo.setOfficeCode(r.getOfficeCode());
					reportVo.setOfficeName(r.getOfficeName());
				}
			});
			reportVo.setTotalCount(reportVo.getNonTransportCount() + reportVo.getTransportCount());
			reportVoList.add(reportVo);

		});
		return reportVoList;
	}

	@Override
	public List<InvoiceDetailsReportVo> invoiceDetailsReport(LocalDate fromDate, LocalDate toDate, String officeCode)
			throws Exception {
		List<String> officeCodeList = getOfficeCodeList(officeCode);
		List<InvoiceDetailsReportVo> listOfInvoiceReport = new ArrayList<>();

		mapData(fromDate, toDate, officeCodeList, listOfInvoiceReport);

		Collections.sort(listOfInvoiceReport, Comparator.comparing(InvoiceDetailsReportVo::getDealername));

		return countTotal(listOfInvoiceReport, getSetOfDealer(null, listOfInvoiceReport));
	}

	public List<InvoiceDetailsReportVo> countTotal(List<InvoiceDetailsReportVo> listOfInvoiceReport,
			List<String> dealerName) throws Exception {
		if (CollectionUtils.isEmpty(listOfInvoiceReport))
			return Collections.emptyList();
		List<InvoiceDetailsReportVo> finalList = new ArrayList<>();
		InvoiceDetailsReportVo finalTotal = new InvoiceDetailsReportVo();
		double totalInvoiceAmount = 0.0;
		double totalTaxAmount = 0.0;
		for (String dealer : dealerName) {
			double invoiceAmount = 0.0;
			double taxAmount = 0.0;

			InvoiceDetailsReportVo total = new InvoiceDetailsReportVo();
			for (InvoiceDetailsReportVo rawData : listOfInvoiceReport) {

				if (dealer.equals(rawData.getDealername())) {
					invoiceAmount += rawData.getInvoiceAmount();
					taxAmount += rawData.getTaxAmount();
					finalList.add(rawData);
					rawData.setTotal(rawData.getInvoiceAmount() + rawData.getTaxAmount());
				}
			}
			total.setMakerclass("TOTAL:");
			total.setInvoiceAmount(invoiceAmount);
			total.setTaxAmount(taxAmount);
			total.setTotal(invoiceAmount + taxAmount);
			// finalList.add(total);

			totalInvoiceAmount += invoiceAmount;
			totalTaxAmount += taxAmount;

		}
		finalTotal.setTrNumber("TOTAL");
		finalTotal.setInvoiceAmount(totalInvoiceAmount);
		finalTotal.setTaxAmount(totalTaxAmount);
		finalTotal.setTotal(totalInvoiceAmount + totalTaxAmount);
		finalList.add(finalTotal);

		return finalList;
	}

	public List<InvoiceDetailsReportVo> mapData(LocalDate fromDate, LocalDate toDate, List<String> officeCodeList,
			List<InvoiceDetailsReportVo> listOfInvoiceReport) {

		List<RegistrationDetailsDTO> regDetailsDto = regDetailsDao
				.findByCreatedDateBetween(dateMapper.getTimewithDate(fromDate, false),
						dateMapper.getTimewithDate(toDate, true))
				.stream().filter(r -> r.getTrNo() != null && r.getOfficeDetails() != null
						&& officeCodeList.contains(r.getOfficeDetails().getOfficeCode()))
				.collect(Collectors.toList());
		List<MasterUsersDTO> dealer = masterUserDao.findByUserIdIn(getSetOfDealer(regDetailsDto, null));

		if (!CollectionUtils.isEmpty(regDetailsDto)) {

			for (MasterUsersDTO d : dealer) {
				if (d.getFirstName() != null)
					for (RegistrationDetailsDTO r : regDetailsDto) {
						if (r.getDealerDetails() != null && r.getDealerDetails().getDealerId().equals(d.getUserId())) {
							InvoiceDetailsReportVo invoiceReport = new InvoiceDetailsReportVo();
							invoiceReport.setDealername(d.getFirstName());
							invoiceReport.setTrNumber(r.getTrNo());
							invoiceReport.setTrDate(MapData(r.getTrGeneratedDate()));
							invoiceReport.setTaxType(r.getTaxType());
							invoiceReport.setTaxAmount((r.getTaxAmount() != null) ? r.getTaxAmount() : 0.0);
							if (r.getInvoiceDetails() != null) {
								invoiceReport.setInvoiceAmount(r.getInvoiceDetails().getInvoiceValue());
								invoiceReport.setInvoiceDate(r.getInvoiceDetails().getInvoiceDate());
							}
							if (r.getVehicleDetails() != null) {
								invoiceReport.setClassOfVehicle(r.getVehicleDetails().getClassOfVehicleDesc());
								invoiceReport.setMakerclass(r.getVehicleDetails().getMakersDesc());
								invoiceReport.setMakername(r.getVehicleDetails().getMakersModel());
							}
							listOfInvoiceReport.add(invoiceReport);
						}
					}
			}

		}
		mapStagingData(fromDate, toDate, listOfInvoiceReport, dealer);
		return listOfInvoiceReport;
	}

	public List<StagingRegistrationDetailsDTO> mapStagingData(LocalDate fromDate, LocalDate toDate,
			List<InvoiceDetailsReportVo> listOfInvoiceReport, List<MasterUsersDTO> dealer) {
		List<StagingRegistrationDetailsDTO> statging = stagingRegistrationDetailsDAO
				.findAllByCreatedDateBetween(dateMapper.getTimewithDate(fromDate, false),
						dateMapper.getTimewithDate(toDate, true))
				.stream().filter(r -> r.getTrNo() != null && r.getDealerDetails() != null).collect(Collectors.toList());

		if (!CollectionUtils.isEmpty(statging)) {

			for (MasterUsersDTO d : dealer) {
				if (d.getFirstName() != null)
					for (StagingRegistrationDetailsDTO r : statging) {
						if (r.getDealerDetails().getDealerId().equals(d.getUserId())) {
							InvoiceDetailsReportVo invoiceReport = new InvoiceDetailsReportVo();
							invoiceReport.setDealername(d.getFirstName());
							invoiceReport.setTrNumber(r.getTrNo());
							invoiceReport.setTrDate(MapData(r.getTrGeneratedDate()));
							invoiceReport.setTaxType(r.getTaxType());
							invoiceReport.setTaxAmount((r.getTaxAmount() != null) ? r.getTaxAmount() : 0.0);
							if (r.getInvoiceDetails() != null) {
								invoiceReport.setInvoiceAmount(r.getInvoiceDetails().getInvoiceValue());
								invoiceReport.setInvoiceDate(r.getInvoiceDetails().getInvoiceDate());
							}
							if (r.getVehicleDetails() != null) {
								invoiceReport.setClassOfVehicle(r.getVehicleDetails().getClassOfVehicleDesc());
								invoiceReport.setMakerclass(r.getVehicleDetails().getMakersDesc());
								invoiceReport.setMakername(r.getVehicleDetails().getMakersModel());
							}
							listOfInvoiceReport.add(invoiceReport);
						}
					}
			}

		}
		return statging;
	}

	public LocalDate MapData(LocalDateTime time) {
		return (time == null) ? null : time.toLocalDate();

	}

	/**
	 * getting list Of office Code
	 * 
	 * @param officeCode
	 * @return
	 * @throws Exception
	 */
	public List<String> getOfficeCodeList(String officeCode) throws Exception {
		List<OfficeVO> listOfOfficeCode = officeService
				.getOfficeByDistrictLimited(officeService.getDistrictByofficeCode(officeCode).getDistrict());
		if (CollectionUtils.isEmpty(listOfOfficeCode))
			throw new Exception("district have no office code");
		return listOfOfficeCode.stream().map(r -> r.getOfficeCode()).collect(Collectors.toList());

	}

	/**
	 * getting distinct key for operation
	 * 
	 * @param regDetailsDto
	 * @param invoiceReport
	 * @return
	 */
	public List<String> getSetOfDealer(List<RegistrationDetailsDTO> regDetailsDto,
			List<InvoiceDetailsReportVo> invoiceReport) {
		Map<String, String> map = new HashMap<>();
		List<String> dealerId = new ArrayList<String>();
		if (regDetailsDto != null) {

			List<String> listPrNO = regDetailsDto.stream().filter(r -> r.getDealerDetails() == null)
					.map(f -> f.getPrNo()).collect(Collectors.toList());
			List<RegistrationDetailsLogDTO> list = regDetailsLog.findAllByRegiDetailsPrNoIn(listPrNO).stream()
					.filter(r -> r.getRegiDetails() != null && r.getRegiDetails().getDealerDetails() != null)
					.collect(Collectors.toList());
			Collections.reverse(list);
			listPrNO.forEach(prNo -> {
				regDetailsDto.forEach(r -> {
					if (r.getPrNo().equals(prNo)) {
						List<RegistrationDetailsLogDTO> regDetails = list.stream()
								.filter(regi -> regi.getRegiDetails().getPrNo().equals(prNo))
								.collect(Collectors.toList());
						if (!CollectionUtils.isEmpty(regDetails))
							r.setDealerDetails(list.stream().findFirst().get().getRegiDetails().getDealerDetails());
					}
				});
			});
			regDetailsDto.stream().filter(r -> r.getDealerDetails() != null).collect(Collectors.toList())
					.forEach(r -> map.put(r.getDealerDetails().getDealerId(), ""));

			dealerId.addAll(map.keySet());
		} else {
			invoiceReport.forEach(r -> map.put(r.getDealername(), ""));
			dealerId.addAll(map.keySet());
		}
		return dealerId;
	}

	public List<List<CellProps>> prepareCellProps(List<String> header,
			List<InvoiceDetailsReportVo> listOfInvoiceReport) {

		int slNo = 0;
		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (InvoiceDetailsReportVo regApproved : listOfInvoiceReport) {
			List<CellProps> result = new ArrayList<CellProps>();
			for (int i = 0; i < header.size(); i++) {
				CellProps cellpro = new CellProps();
				switch (i) {
				case 0:
					cellpro.setFieldValue(String.valueOf(++slNo));
					break;
				case 1:
					cellpro.setFieldValue(replaceDefaults(regApproved.getDealername()));
					break;
				case 2:
					cellpro.setFieldValue(replaceDefaults(regApproved.getTrNumber()));
					break;
				case 3:
					cellpro.setFieldValue(dateMapper.getData(regApproved.getTrDate(), null));
					break;
				case 4:
					cellpro.setFieldValue(replaceDefaults(regApproved.getClassOfVehicle()));
					break;
				case 5:
					cellpro.setFieldValue(replaceDefaults(regApproved.getMakername()));
					break;
				case 6:
					cellpro.setFieldValue(replaceDefaults(regApproved.getMakerclass()));
					break;
				case 7:
					cellpro.setFieldValue(dateMapper.getData(regApproved.getInvoiceDate(), null));
					break;
				case 8:
					cellpro.setFieldValue(replaceDefaults(regApproved.getTaxType()));
					break;
				case 9:
					cellpro.setFieldValue(String.valueOf(regApproved.getInvoiceAmount()));
					break;
				case 10:
					cellpro.setFieldValue(String.valueOf(regApproved.getTaxAmount()));
					break;
				case 11:
					cellpro.setFieldValue(String.valueOf(regApproved.getTotal()));
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

	private String replaceDefaults(String input) {
		return (StringUtils.isBlank(input)) ? StringUtils.EMPTY : input;

	}

	/*
	 * @Override public void getExcel(HttpServletResponse response, RegReportVO
	 * regVO, String reportName, String userId, String officeCode, Pageable page)
	 * throws Exception {
	 * 
	 * dateMapper.getExcel(response, regVO, "invoiceReport", userId, officeCode,
	 * page); }
	 */

	@Override
	public void generateVehicleStrengthReportExcel(List<ReportVO> reportVo, HttpServletResponse response) {
	
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "VehicleStrengthReport");

		String name = "VehicleStrengthReport";
		String fileName = name + ".xlsx";
		String sheetname = "VehicleStrengthReport";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);
		
		XSSFWorkbook wb = excel.renderData(prepareCellPropsForVehicleStrengthReportsExcel(header, reportVo), header, fileName,
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

	private List<List<CellProps>> prepareCellPropsForVehicleStrengthReportsExcel(List<String> header, List<ReportVO> resultList) {
		
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(resultList)) {
			for (ReportVO report : resultList) {

				List<CellProps> cells = new ArrayList<>();

					for (int i = 0; i < header.size(); i++) {

						CellProps cellpro = new CellProps();

						switch (i) {
						case 0:
							cellpro.setFieldValue(String.valueOf(++slNo));
							break;
						case 1:
							if(StringUtils.isNotEmpty(report.getDistrictName())) {
							cellpro.setFieldValue(report.getDistrictName());
							}
							break;
						case 2:
							cellpro.setFieldValue(DateConverters.replaceDefaults(report.getCount()));
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

	@Override
	public void generateVehicleStrengthReportOfficeDataExcel(List<ReportVO> reportVo, HttpServletResponse response) {
	
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "VehicleStrengthReportOfficeData");

		String name = "VehicleStrengthReportOfficeData";
		String fileName = name + ".xlsx";
		String sheetname = "VehicleStrengthReportOfficeData";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);
		
		XSSFWorkbook wb = excel.renderData(prepareCellPropsForVehicleStrengthReportsOfficeDataExcel(header, reportVo), header, fileName,
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

	private List<List<CellProps>> prepareCellPropsForVehicleStrengthReportsOfficeDataExcel(List<String> header,
			List<ReportVO> resultList) {
		
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(resultList)) {
			for (ReportVO report : resultList) {

				List<CellProps> cells = new ArrayList<>();

					for (int i = 0; i < header.size(); i++) {

						CellProps cellpro = new CellProps();

						switch (i) {
						case 0:
							cellpro.setFieldValue(String.valueOf(++slNo));
							break;
						case 1:
							if(StringUtils.isNotEmpty(report.getOfficeName())) {
							cellpro.setFieldValue(report.getOfficeName());
							}
							break;
						case 2:
							cellpro.setFieldValue(DateConverters.replaceDefaults(report.getTransportCount()));
							break;
						case 3:
							cellpro.setFieldValue(DateConverters.replaceDefaults(report.getNonTransportCount()));
							break;
						case 4:
							cellpro.setFieldValue(DateConverters.replaceDefaults(report.getTotalCount()));
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

	@Override
	public void generateVehicleStrengthReportTransportDataExcel(List<ReportVO> reportVo, HttpServletResponse response) {
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "VehicleStrengthReportTransportData");

		String name = "VehicleStrengthReportTransportData";
		String fileName = name + ".xlsx";
		String sheetname = "VehicleStrengthReportTransportData";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);
		
		XSSFWorkbook wb = excel.renderData(prepareCellPropsForVehicleStrengthReportsTransportDataExcel(header, reportVo), header, fileName,
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

	private List<List<CellProps>> prepareCellPropsForVehicleStrengthReportsTransportDataExcel(List<String> header,
			List<ReportVO> resultList) {
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(resultList)) {
			for (ReportVO report : resultList) {

				List<CellProps> cells = new ArrayList<>();

					for (int i = 0; i < header.size(); i++) {

						CellProps cellpro = new CellProps();

						switch (i) {
						case 0:
							cellpro.setFieldValue(String.valueOf(++slNo));
							break;
						case 1:
							if(StringUtils.isNotEmpty(report.getGroupCategory())) {
							cellpro.setFieldValue(report.getGroupCategory());
							}
							break;
						case 2:
							cellpro.setFieldValue(DateConverters.replaceDefaults(report.getCount()));
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
