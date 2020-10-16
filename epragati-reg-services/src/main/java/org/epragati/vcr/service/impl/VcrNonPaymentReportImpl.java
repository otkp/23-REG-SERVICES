package org.epragati.vcr.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.epragati.excel.service.CellProps;
import org.epragati.excel.service.ExcelService;
import org.epragati.excel.service.ExcelServiceImpl;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.DistrictDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.StagingRegServiceDetailsAutoApprovalLogDAO;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.StagingRegServiceDetailsAutoApprovalDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.mappers.DistrictMapper;
import org.epragati.master.mappers.OfficeMapper;
import org.epragati.master.vo.DistrictVO;
import org.epragati.master.vo.OfficeVO;
import org.epragati.payment.report.vo.AutoApprovalsDistrictWise;
import org.epragati.payment.report.vo.RegReportVO;
import org.epragati.payment.report.vo.ReportsVO;
import org.epragati.regservice.vo.ApplicationSearchVO;
import org.epragati.regservice.vo.AutoApprovalsOfficeWiseVO;
import org.epragati.regservice.vo.StagingRegServiceAutoApprovalsVO;
import org.epragati.reports.service.RCCancellationService;
import org.epragati.rta.reports.vo.UsersDropDownVO;
import org.epragati.sequence.SequenceGenerator;
import org.epragati.util.RoleEnum;
import org.epragati.util.Status;
import org.epragati.util.document.Sequence;
import org.epragati.util.payment.ServiceEnum;
import org.epragati.vcr.service.VcrNonPaymentReport;
import org.epragati.vcrImage.dao.VcrFinalServiceDAO;
import org.epragati.vcrImage.dto.VcrFinalServiceDTO;
import org.epragati.vcrImage.mapper.StagingRegServiceAutoApprovalsMapper;
import org.epragati.vcrImage.mapper.VcrFinalServiceMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
@Service
public class VcrNonPaymentReportImpl implements VcrNonPaymentReport{
	
	private static final Logger logger = LoggerFactory.getLogger(VcrNonPaymentReportImpl.class);
	
	
	@Autowired
	VcrFinalServiceDAO vcrFinalServiceDAO;
	
	@Autowired
	RCCancellationService rCCancellationService;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private VcrFinalServiceMapper vcrFinalServiceMapper;
	
	@Autowired
	private SequenceGenerator sequenceGenerator;
	@Autowired
	private StagingRegServiceDetailsAutoApprovalLogDAO regServicesAutoApprovalLogDAO;
	@Autowired
	private StagingRegServiceAutoApprovalsMapper serviceAutoApprovalsMapper;
	@Autowired
	private UserDAO userDAO;
	@Autowired
	private OfficeDAO officeDAO;
	@Autowired
	private OfficeMapper officeMapper;
	@Autowired
	private DistrictMapper districtMapper;
	@Autowired
	private DistrictDAO districtDAO;
	@Override
	public Optional<ReportsVO> vcrNonPaymentReport(RegReportVO regReportVO, Pageable pagable) {
		
			LocalDateTime fromDate = getTimewithDate(regReportVO.getFromDate(), false);
			LocalDateTime toDate = getTimewithDate(regReportVO.getToDate(), true);

			Page<VcrFinalServiceDTO> vcrNonPayment = vcrFinalServiceDAO
					.findByIsVcrClosedFalseAndPaymentDoneFalseAndIsScVcrNoGeneratedFalseAndOfficeCodeAndCreatedByAndCreatedDateBetween(regReportVO.getOfficeCode(), regReportVO.getUserId(),fromDate, toDate,
							pagable.previousOrFirst());
			if (vcrNonPayment.hasContent()) {
				List<VcrFinalServiceDTO> vcrNonPaymentList = vcrNonPayment.getContent();
				if (CollectionUtils.isNotEmpty(vcrNonPaymentList)) {
					ReportsVO reportVO = new ReportsVO();
					reportVO.setvCRNonPaymentDetailsVO(
							vcrFinalServiceMapper.convertEntityLimitedList(vcrNonPaymentList));
					reportVO.setPageNo(vcrNonPayment.getNumber());
					reportVO.setTotalPage(vcrNonPayment.getTotalPages());
					return Optional.of(reportVO);
				}
			}
			return Optional.empty();
	}

	private LocalDateTime getTimewithDate(LocalDate date, Boolean timeZone) {

		String dateVal = date + "T00:00:00.000Z";
		if (timeZone) {
			dateVal = date + "T23:59:59.999Z";
		}
		ZonedDateTime zdt = ZonedDateTime.parse(dateVal);
		return zdt.toLocalDateTime();
	}

	@Override
	public void generateShowCauseNoForVcr(ApplicationSearchVO applicationSearchVO, String officeCode, String user,String role) {
		if (CollectionUtils.isNotEmpty(applicationSearchVO.getvCRNonPaymentDetailsVOs())) {
			List<String> vrNosList = applicationSearchVO.getvCRNonPaymentDetailsVOs().stream()
					.map(vcr -> vcr.getVcrNo()).collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(vrNosList)) {
				List<VcrFinalServiceDTO> vcrlist = vcrFinalServiceDAO
						.findByVcrVcrNumberInAndPaymentDoneFalseAndIsScVcrNoGeneratedFalse(vrNosList);
				if (CollectionUtils.isNotEmpty(vcrlist)) {
					vcrlist.forEach(val -> {
						val.setScVcrNoIssuedOn(LocalDate.now());
						val.setIsScVcrNoGenerated(Boolean.TRUE);
						val.setIsScNoVcrIssuedRole(role);
						val.setIsScNoVcrIssuedBy(user);
						val.setlUpdate(LocalDateTime.now());
						val.setVcrNonApplicationStatus(Status.ShowCauseStatus.SHOWCAUSEISSUED.getStatus());
						if (StringUtils.isEmpty(val.getIsScNoVcr())) {
							Map<String, String> officeCodeMap = new TreeMap<>();
							officeCodeMap.put("officeCode", officeCode);
							val.setIsScNoVcr(sequenceGenerator.getSequence(
									String.valueOf(Sequence.SHOWCAUSENOFORVCR.getSequenceId()), officeCodeMap));
						}

					});
				}
				vcrFinalServiceDAO.save(vcrlist);
				vcrlist.clear();
			}
		}
	}

	@Override
	public Optional<ReportsVO> getShowCauseNoDetailsExistingForVcr(RegReportVO regReportVO, Pageable pagable) {
		LocalDateTime fromDate = getTimewithDate(regReportVO.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(regReportVO.getToDate(), true);

		Page<VcrFinalServiceDTO> vcrNonPayment = vcrFinalServiceDAO
				.findByIsVcrClosedFalseAndPaymentDoneFalseAndOfficeCodeAndCreatedByAndIsScVcrNoGeneratedTrueAndCreatedDateBetween(regReportVO.getOfficeCode(), regReportVO.getUserId(),fromDate, toDate,
						pagable.previousOrFirst());
		if (vcrNonPayment.hasContent()) {
			List<VcrFinalServiceDTO> vcrNonPaymentList = vcrNonPayment.getContent();
			if (CollectionUtils.isNotEmpty(vcrNonPaymentList)) {
				ReportsVO reportVO = new ReportsVO();
				reportVO.setvCRNonPaymentDetailsVO(
						vcrFinalServiceMapper.convertEntityLimitedList(vcrNonPaymentList));
				reportVO.setPageNo(vcrNonPayment.getNumber());
				reportVO.setTotalPage(vcrNonPayment.getTotalPages());
				return Optional.of(reportVO);
			}
		}
		return Optional.empty();
	}

	@Override
	public List<UsersDropDownVO> getVcrNonPaymentRolesDropDown(String officeCode) {
		List<UsersDropDownVO> dropDownforallroles = new ArrayList<>();
		if (StringUtils.isNotEmpty(officeCode)) {
			List<UserDTO> users = null;
				users = userDAO.findByPrimaryRoleNameInOrAdditionalRolesNameInAndOfficeOfficeCodeNative(
						Arrays.asList(RoleEnum.MVI.getName(),RoleEnum.RTO.getName()), Arrays.asList(RoleEnum.MVI.getName(),RoleEnum.RTO.getName()), officeCode);
			
			if(CollectionUtils.isEmpty(users)) {
				throw new BadRequestException("Dept Users Not Available");
			}
			users.stream().forEach(action->{
				UsersDropDownVO  vo = new UsersDropDownVO();
				vo.setUserId(action.getUserId());
				vo.setUserName(getValue(action.getFirstName())+" "+getValue(action.getLastName()));
				dropDownforallroles.add(vo);
			});
			
			
		}
		return dropDownforallroles;
	}
	private String getValue(String value) {
		if (StringUtils.isNotBlank(value)) {
			return value;
		}
		return StringUtils.EMPTY;
	}

	@Override
	public Optional<ReportsVO> getRegServicesAutoapprovalsDetails(RegReportVO regReportVO, Pageable pagable) {
		List<StagingRegServiceAutoApprovalsVO> autoApprovalsList = null;
		ReportsVO autoApprovalsReportsVO = new ReportsVO();
		if (regReportVO.getFromDate() != null && regReportVO.getToDate() != null
				&& regReportVO.getOfficeCode() != null) {
			LocalDate fromDate = getLocalDate(regReportVO.getFromDate(), false);
			LocalDate toDate = getLocalDate(regReportVO.getToDate(), false);
			Criteria criteria1 = Criteria.where("officeCode").is(regReportVO.getOfficeCode());
			Criteria criteria2 = Criteria.where("autoApprovalsDate").gte(fromDate).lte(toDate);
			Query query = new Query();
			query.addCriteria(new Criteria().andOperator(criteria1, criteria2));
			long count = mongoTemplate.count(query, StagingRegServiceDetailsAutoApprovalDTO.class);
			query.limit(pagable.getPageSize());
			query.skip(pagable.getPageSize() * (pagable.getPageNumber() - 1));
			List<StagingRegServiceDetailsAutoApprovalDTO> regautoApprovalsList = mongoTemplate.find(query,
					StagingRegServiceDetailsAutoApprovalDTO.class);
			if (regautoApprovalsList == null || regautoApprovalsList.isEmpty()) {
				throw new BadRequestException(" Record not found");
			}
			if (!regautoApprovalsList.isEmpty()) {
				autoApprovalsList = serviceAutoApprovalsMapper.convertEntity(regautoApprovalsList);
				autoApprovalsReportsVO.setAutoApprovalsList(autoApprovalsList);
				autoApprovalsReportsVO.setTotalPage(regautoApprovalsList.size() == 0 ? 1
						: (int) Math.ceil((double) count / (double) pagable.getPageSize()));
				autoApprovalsReportsVO.setPageNo(pagable.getPageNumber());
				return Optional.of(autoApprovalsReportsVO);
			}

		}
		return null;
	}
	private LocalDate getLocalDate(LocalDate date, Boolean timeZone) {
		String dateVal = date + "T23:59:59.999Z";
		if (timeZone) {
			dateVal = date + "T23:59:59.999Z";
		}
		ZonedDateTime zdt = ZonedDateTime.parse(dateVal);
		return zdt.toLocalDate();
	}

	@Override
	public Optional<ReportsVO> getRegOfficeAndDistAutoapprovalsDetails(RegReportVO regReportVO, Pageable pagable) {
		Page<StagingRegServiceDetailsAutoApprovalDTO> regautoApprovals = null;
		List<StagingRegServiceDetailsAutoApprovalDTO> regautoApprovalsList = null;
		List<StagingRegServiceAutoApprovalsVO> autoApprovalsList = null;
		ReportsVO autoApprovalsReportsVO = new ReportsVO();
		if (StringUtils.isNotEmpty(regReportVO.getOfficeCode()) && regReportVO.getFromDate() != null
				&& regReportVO.getToDate() != null && StringUtils.isEmpty(regReportVO.getRole())) {
			LocalDate fromDate = getLocalDate(regReportVO.getFromDate(), false);
			LocalDate toDate = getLocalDate(regReportVO.getToDate(), false);
			Criteria criteria1 = Criteria.where("officeCode").is(regReportVO.getOfficeCode());
			Criteria criteria2 = Criteria.where("autoApprovalsDate").gte(fromDate).lte(toDate);
			Query query = new Query();
			query.addCriteria(new Criteria().andOperator(criteria1, criteria2));
			long count = mongoTemplate.count(query, StagingRegServiceDetailsAutoApprovalDTO.class);
			query.limit(pagable.getPageSize());
			query.skip(pagable.getPageSize() * (pagable.getPageNumber() - 1));
			regautoApprovalsList = mongoTemplate.find(query, StagingRegServiceDetailsAutoApprovalDTO.class);
			if (regautoApprovalsList == null || regautoApprovalsList.isEmpty()) {
				throw new BadRequestException(" Record not found");
			}
			if (!regautoApprovalsList.isEmpty()) {
				autoApprovalsList = serviceAutoApprovalsMapper.convertEntity(regautoApprovalsList);
				autoApprovalsReportsVO.setAutoApprovalsList(autoApprovalsList);
				autoApprovalsReportsVO.setTotalPage(regautoApprovalsList.size() == 0 ? 1
						: (int) Math.ceil((double) count / (double) pagable.getPageSize()));
				autoApprovalsReportsVO.setPageNo(pagable.getPageNumber());
				return Optional.of(autoApprovalsReportsVO);
			}

		}
		if (regReportVO.getFromDate() != null && regReportVO.getToDate() != null
				&& StringUtils.isEmpty(regReportVO.getRole())) {
			LocalDate fromDate = getLocalDate(regReportVO.getFromDate(), false);
			LocalDate toDate = getLocalDate(regReportVO.getToDate(), false);
			Criteria criteria2 = Criteria.where("autoApprovalsDate").gte(fromDate).lte(toDate);
			Query query = new Query();
			query.addCriteria(new Criteria().andOperator(criteria2));
			regautoApprovalsList = mongoTemplate.find(query, StagingRegServiceDetailsAutoApprovalDTO.class);
			if (regautoApprovalsList == null || regautoApprovalsList.isEmpty()) {
				throw new BadRequestException(" Record not found");
			}
			if (!regautoApprovalsList.isEmpty()) {
				autoApprovalsList = serviceAutoApprovalsMapper.convertEntity(regautoApprovalsList);
				autoApprovalsReportsVO.setAutoApprovalsDistrictWise(autoApprovalsDistWiseCount(autoApprovalsList));
				return Optional.of(autoApprovalsReportsVO);
			}
		}
		if (regReportVO.getFromDate() != null && regReportVO.getToDate() != null
				&& StringUtils.isNotEmpty(regReportVO.getRole())) {
			LocalDate fromDate = getLocalDate(regReportVO.getFromDate(), false);
			LocalDate toDate = getLocalDate(regReportVO.getToDate(), false);
			Criteria criteria2 = Criteria.where("autoApprovalsDate").gte(fromDate).lte(toDate);
			Query query = new Query();
			query.addCriteria(new Criteria().andOperator(criteria2));
			regautoApprovalsList = mongoTemplate.find(query, StagingRegServiceDetailsAutoApprovalDTO.class);
			if (regautoApprovalsList == null || regautoApprovalsList.isEmpty()) {
				throw new BadRequestException(" Record not found");
			}
			if (!regautoApprovalsList.isEmpty()) {
				autoApprovalsList = serviceAutoApprovalsMapper.convertEntity(regautoApprovalsList);
				autoApprovalsReportsVO.setAutoApprovalsOfficeWise(
						autoApprovalsOfficeWise(autoApprovalsList, regReportVO.getOfficeCode()));
				return Optional.of(autoApprovalsReportsVO);
			}
		}

		return null;
	}

	private List<AutoApprovalsOfficeWiseVO> autoApprovalsOfficeWiseCount(
			List<StagingRegServiceAutoApprovalsVO> autoApprovalsList) {
		List<AutoApprovalsOfficeWiseVO> officeWiseList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(autoApprovalsList)) {
			for (StagingRegServiceAutoApprovalsVO autoApprovals : autoApprovalsList) {
				AutoApprovalsOfficeWiseVO officeCountVo = new AutoApprovalsOfficeWiseVO();
				Optional<OfficeDTO> officeDteails = officeDAO.findByOfficeCode(autoApprovals.getOfficeCode());
				OfficeVO officeDetails = officeMapper.convertEntity(officeDteails.get());
				if (officeDteails.isPresent() && !CollectionUtils.isEmpty(officeWiseList) && !officeWiseList.isEmpty()
						&& officeWiseList.stream().anyMatch(val -> val.getOfficeName().getOfficeCode()
								.equalsIgnoreCase(officeDetails.getOfficeCode()))) {
					for (AutoApprovalsOfficeWiseVO sameOffice : officeWiseList) {
						if (sameOffice.getOfficeName().getOfficeCode()
								.equalsIgnoreCase(officeDetails.getOfficeCode())) {
							sameOffice.setOfficeWiseCount(sameOffice.getOfficeWiseCount() + 1);
							if (!sameOffice.getServiceName().contains(autoApprovals.getServiceName()))
								sameOffice.getServiceName()
										.addAll(new HashSet<ServiceEnum>(autoApprovals.getServiceName()));
							sameOffice.getServiceNameDesc().addAll(new HashSet<String>(autoApprovals.getServiceNameDesc()));
							break;
						}
					}
				}

				else {
					officeCountVo.setOfficeName(officeDetails);
					officeCountVo.setOfficeWiseCount(1);
					officeCountVo.setServiceName(new HashSet<ServiceEnum>(autoApprovals.getServiceName()));
					officeCountVo.setServiceNameDesc(new HashSet<String>(autoApprovals.getServiceNameDesc()));
					officeWiseList.add(officeCountVo);
				}

			}
		}
		if (officeWiseList == null || officeWiseList.isEmpty()) {
			throw new BadRequestException("Auto approval Data not available");
		}
		return officeWiseList;
	}

	private List<AutoApprovalsDistrictWise> autoApprovalsDistWiseCount(
			List<StagingRegServiceAutoApprovalsVO> autoApprovalsList) {
		List<AutoApprovalsDistrictWise> distWiseList = new ArrayList<>();
		for (StagingRegServiceAutoApprovalsVO autoApprovals : autoApprovalsList) {
			AutoApprovalsDistrictWise distWise = new AutoApprovalsDistrictWise();
			Optional<DistrictVO> distDetials = districtMapper
					.convertEntity(districtDAO.findByDistrictName(autoApprovals.getDistrictName()));
		//	System.out.println("---"+autoApprovals.getDistrictName());
			if (distDetials.isPresent() && !CollectionUtils.isEmpty(distWiseList) && !distWiseList.isEmpty()
					&& distWiseList.stream().anyMatch(
							val -> val.getDistrict().getName().equalsIgnoreCase(distDetials.get().getName()))) {
				for (AutoApprovalsDistrictWise sameDist : distWiseList) {
					if (sameDist.getDistrict().getName().equalsIgnoreCase(distDetials.get().getName())) {
						sameDist.setDistrictWiseCount(sameDist.getDistrictWiseCount() + 1);
						if (!sameDist.getDistrictserviceName().contains(autoApprovals.getServiceName()))
							sameDist.getDistrictserviceName()
									.addAll(new HashSet<ServiceEnum>(autoApprovals.getServiceName()));
						sameDist.getServiceNameDesc().addAll(new HashSet<String>(autoApprovals.getServiceNameDesc()));
						break;
					}
				}
			}

			else {
				if(distDetials.isPresent())
				distWise.setDistrict(distDetials.get());
				distWise.setDistrictWiseCount(1);
				distWise.setDistrictserviceName(new HashSet<ServiceEnum>(autoApprovals.getServiceName()));
				distWise.setServiceNameDesc(new HashSet<String>(autoApprovals.getServiceNameDesc()));
				List<AutoApprovalsOfficeWiseVO> officeWiseList = autoApprovalsOfficeWiseCount(autoApprovalsList);
				List<AutoApprovalsOfficeWiseVO> distWiseOffices = new ArrayList<>();
				if (!officeWiseList.isEmpty()) {
					for (AutoApprovalsOfficeWiseVO offices : officeWiseList) {
						if (offices.getOfficeName().getDistrict() == autoApprovals.getDistrictId()) {
							distWiseOffices.add(offices);
						}
					}
				}
				distWise.setDistrictOfficeDetils(distWiseOffices);
				distWiseList.add(distWise);
			}
		}
		if (distWiseList == null || distWiseList.isEmpty()) {
			throw new BadRequestException("Auto approval Data not available");
		}
		return distWiseList;
	}

	@Override
	public Optional<ReportsVO> getExcelReportByAutoapprovals(HttpServletResponse response, RegReportVO regReportVO,
			Pageable pagable) {
		LocalDate fromDate = getLocalDate(regReportVO.getFromDate(), false);
		LocalDate toDate = getLocalDate(regReportVO.getToDate(), false);
		List<StagingRegServiceDetailsAutoApprovalDTO> regautoApprovalsList = regServicesAutoApprovalLogDAO
				.findByAutoApprovalsDateBetweenAndOfficeCode(fromDate, toDate, regReportVO.getOfficeCode());

		if (regautoApprovalsList == null || regautoApprovalsList.isEmpty()) {
			throw new BadRequestException("Auto approval Data not available");
		}

		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "AutoApprovalReport");

		String name = "AutoApprovalReport";
		String fileName = name + ".xlsx";
		String sheetName = "AutoApprovalReport";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		response.setHeader("Pragma", "public");
		response.setHeader("Cache-Control", "no-store");
		response.addHeader("Cache-Control", "max-age=0");

		XSSFWorkbook wb = excel.renderData(prepareCellProps(header, regautoApprovalsList), header, fileName, sheetName);

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
			logger.error(" dispatch download excle Exception{}:", e.getMessage());
		}
		return null;
	}

	private List<List<CellProps>> prepareCellProps(List<String> header,
			List<StagingRegServiceDetailsAutoApprovalDTO> llrautoApprovalsList) {

		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();
		List<CellProps> result = new ArrayList<CellProps>();
		for (StagingRegServiceDetailsAutoApprovalDTO regApproved : llrautoApprovalsList) {
			for (int i = 0; i < header.size(); i++) {
				CellProps cellpro = new CellProps();
				switch (i) {
				case 0:
					cellpro.setFieldValue(String.valueOf(++slNo));
					break;
				case 1:
					cellpro.setFieldValue(replaceDefaults(regApproved.getApplicationNo()));
					break;
				case 2:
					if (regApproved.getPrNo() != null) {
						cellpro.setFieldValue(replaceDefaults(regApproved.getPrNo()));
					} else if (regApproved.getTrNo() != null) {
						cellpro.setFieldValue(replaceDefaults(regApproved.getTrNo()));
					} else {
						cellpro.setFieldValue(replaceDefaults(regApproved.getTrNo()));
					}
					break;
				case 3:
					String servicesTypes = null;
					if (regApproved.getServiceName() != null && !regApproved.getServiceName().isEmpty()) {
						if (regApproved.getServiceName().size() == 1) {
							for (ServiceEnum serviceType : regApproved.getServiceName()) {
								servicesTypes = serviceType.getDesc();
							}
						} else {
							for (ServiceEnum serviceType : regApproved.getServiceName()) {
								servicesTypes = serviceType.getDesc() + ",";
							}
						}
					}
					cellpro.setFieldValue(replaceDefaults(servicesTypes));
					break;
				case 4:
					if (regApproved.getCreatedDate() != null)
						cellpro.setFieldValue(convertDateToString(regApproved.getCreatedDate()));
					break;
				case 5:
					if (regApproved.getAutoApprovalsDate() != null)
						cellpro.setFieldValue(convertLocalDateFormat(regApproved.getAutoApprovalsDate()));
					break;
				case 6:

					cellpro.setFieldValue(replaceDefaults(regApproved.getRoleType().toString()));
					break;
				case 7:

					cellpro.setFieldValue("Approved By System");
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

	private String replaceDefaults(String input) {

		if (StringUtils.isBlank(input)) {
			return StringUtils.EMPTY;
		}
		return input;
	}

	public String convertDateToString(LocalDateTime date) {
		// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy
		// HH:mm:ss");
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		return date.format(formatter);
	}

	public static String convertLocalDateFormat(LocalDate date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		return date.format(formatter);
	}
	private List<AutoApprovalsOfficeWiseVO> autoApprovalsOfficeWise(
			List<StagingRegServiceAutoApprovalsVO> autoApprovalsList,String officeCode) {
		List<AutoApprovalsOfficeWiseVO> officeWiseList = new ArrayList<>();
		Optional<OfficeDTO> officeWithDistId = officeDAO.findByOfficeCode(officeCode);
		if (CollectionUtils.isNotEmpty(autoApprovalsList)) {
			for (StagingRegServiceAutoApprovalsVO autoApprovals : autoApprovalsList) {
				AutoApprovalsOfficeWiseVO officeCountVo = new AutoApprovalsOfficeWiseVO();
				Optional<OfficeDTO> officeDteails = officeDAO.findByOfficeCode(autoApprovals.getOfficeCode());
				OfficeVO officeDetails = officeMapper.convertEntity(officeDteails.get());
				if (officeDteails.isPresent() && !CollectionUtils.isEmpty(officeWiseList) && !officeWiseList.isEmpty()
						&& officeWiseList.stream().anyMatch(val -> val.getOfficeName().getOfficeCode()
								.equalsIgnoreCase(officeDetails.getOfficeCode()))) {
					for (AutoApprovalsOfficeWiseVO sameOffice : officeWiseList) {
						if (sameOffice.getOfficeName().getOfficeCode()
								.equalsIgnoreCase(officeDetails.getOfficeCode())) {
							sameOffice.setOfficeWiseCount(sameOffice.getOfficeWiseCount() + 1);
							if (!sameOffice.getServiceName().contains(autoApprovals.getServiceName()))
								sameOffice.getServiceName()
										.addAll(new HashSet<ServiceEnum>(autoApprovals.getServiceName()));
							sameOffice.getServiceNameDesc().addAll(new HashSet<String>(autoApprovals.getServiceNameDesc()));
							break;
						}
					}
				}

				else {
					officeCountVo.setOfficeName(officeDetails);
					officeCountVo.setOfficeWiseCount(1);
					officeCountVo.setServiceName(new HashSet<ServiceEnum>(autoApprovals.getServiceName()));
					officeCountVo.setServiceNameDesc(new HashSet<String>(autoApprovals.getServiceNameDesc()));
					if (officeWithDistId.get().getDistrict() == autoApprovals.getDistrictId()) {
						officeWiseList.add(officeCountVo);
					}
					
				}

			}
		}
		if (officeWiseList == null || officeWiseList.isEmpty()) {
			throw new BadRequestException("Auto approval Data not available");
		}
		return officeWiseList;
	}
}
