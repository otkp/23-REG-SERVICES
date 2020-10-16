package org.epragati.reports.service.impl;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

import java.awt.Desktop.Action;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import javax.annotation.PostConstruct;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFDrawing;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFPicture;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.epragati.aadhaar.VcrHistoryVO;
import org.epragati.aadhaarseeding.vo.AadhaarSeedVO;
import org.epragati.actions.dao.SuspensionDAO;
import org.epragati.actions.dto.RCActionsDTO;
import org.epragati.actions.mapper.SuspensionMapper;
import org.epragati.common.dao.PropertiesDAO;
import org.epragati.common.dto.ExcelHeaders;
import org.epragati.common.dto.PropertiesDTO;
import org.epragati.common.dto.aadhaar.seed.AadhaarSeedDTO;
import org.epragati.common.vo.MviReportVO;
import org.epragati.constants.CommonConstants;
import org.epragati.constants.CovCategory;
import org.epragati.constants.DispatchEnum;
import org.epragati.constants.NationalityEnum;
import org.epragati.dao.enclosure.CitizenEnclosuresDAO;
import org.epragati.dealer.vo.TrIssuedReportVO;
import org.epragati.dispatcher.dao.DispatcherSubmissionDAORepo;
import org.epragati.dispatcher.dto.DispatcherSubmissionDTO;
import org.epragati.dispatcher.mapper.DispatcherMapper;
import org.epragati.dispatcher.vo.DispatcherSubmissionVO;
import org.epragati.dto.enclosure.CitizenEnclosuresDTO;
import org.epragati.dto.enclosure.ImageEnclosureDTO;
import org.epragati.excel.service.CellProps;
import org.epragati.excel.service.ExcelService;
import org.epragati.excel.service.ExcelServiceImpl;
import org.epragati.exception.BadRequestException;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dao.ClassOfVehiclesDAO;
import org.epragati.master.dao.DealerCovDAO;
import org.epragati.master.dao.DistrictDAO;
import org.epragati.master.dao.EnclosuresDAO;
import org.epragati.master.dao.FcDetailsDAO;
import org.epragati.master.dao.MasterCovDAO;
import org.epragati.master.dao.MasterUsersDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.RegServiceDAO;
import org.epragati.master.dao.RegServiceReportDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.StagingRegistrationDetailsDAO;
import org.epragati.master.dao.TaxDetailsDAO;
import org.epragati.master.dao.TrGeneratedReportDAO;
import org.epragati.master.dao.TrIssuedReportMapper;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dao.VillageDAO;
import org.epragati.master.dto.ApplicantAddressDTO;
import org.epragati.master.dto.DealerCovDTO;
import org.epragati.master.dto.DistrictDTO;
import org.epragati.master.dto.EnclosuresDTO;
import org.epragati.master.dto.FcDetailsDTO;
import org.epragati.master.dto.MasterCovDTO;
import org.epragati.master.dto.MasterUsersDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.dto.TaxComponentDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.master.dto.TrGeneratedReportDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.dto.VillageDTO;
import org.epragati.master.mappers.ApplicantAddressMapper;
import org.epragati.master.mappers.DistrictMapper;
import org.epragati.master.mappers.NonPaymentDetailsMapper;
import org.epragati.master.mappers.OfficeMapper;
import org.epragati.master.mappers.RegistrationDetailsMapper;
import org.epragati.master.mappers.StagingDetailsMapper;
import org.epragati.master.mappers.UserMapper;
import org.epragati.master.service.DistrictService;
import org.epragati.master.service.MandalService;
import org.epragati.master.vo.DistrictVO;
import org.epragati.master.vo.FinanceDetailsVO;
import org.epragati.master.vo.MandalVO;
import org.epragati.master.vo.OfficeVO;
import org.epragati.master.vo.UserVO;
import org.epragati.payment.dto.CfstRevenueDTO;
import org.epragati.payment.dto.ClassOfVehiclesDTO;
import org.epragati.payment.dto.FeeDetailsDTO;
import org.epragati.payment.dto.FeesDTO;
import org.epragati.payment.dto.PaymentReportDTO;
import org.epragati.payment.dto.PaymentTransactionDTO;
import org.epragati.payment.dto.RevenueTargetDTO;
import org.epragati.payment.report.vo.InvoiceDetailsReportVo;
import org.epragati.payment.report.vo.MviPerformanceVO;
import org.epragati.payment.report.vo.MviVcrCount;
import org.epragati.payment.report.vo.OsDataEntryFinalVO;
import org.epragati.payment.report.vo.RegReportDuplicateVO;
import org.epragati.payment.report.vo.RegReportVO;
import org.epragati.payment.report.vo.ReportsVO;
import org.epragati.payment.report.vo.TotalVcrCountInDist;
import org.epragati.payments.dao.CfstRevenueDAO;
import org.epragati.payments.dao.PaymentReportDAO;
import org.epragati.payments.dao.PaymentTransactionDAO;
import org.epragati.payments.dao.RevenueTargetDAO;
import org.epragati.payments.vo.BreakPayments;
import org.epragati.permits.dao.PermitDetailsDAO;
import org.epragati.permits.dto.FcReportsDemoDTO;
import org.epragati.permits.dto.FitnessReportsDemoVO;
import org.epragati.permits.dto.PermitDetailsDTO;
import org.epragati.rcactions.RCActionsVO;
import org.epragati.regservice.RegistrationService;
import org.epragati.regservice.dao.AadhaarSeedDAO;
import org.epragati.regservice.dto.ActionDetails;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.dto.RegServiceReportDTO;
import org.epragati.regservice.mapper.AadhaarSeedMapper;
import org.epragati.regservice.mapper.FreshRCMapper;
//import org.epragati.regservice.dto.TaxDetailsDTO;
import org.epragati.regservice.mapper.RegServiceMapper;
import org.epragati.regservice.vo.CitizenApplicationSearchResponceVO;
import org.epragati.regservice.vo.NOCDetailsVO;
import org.epragati.regservice.vo.RegServiceVO;
import org.epragati.regservice.vo.TaxDetailsVO;
import org.epragati.reports.dao.ReportsRoleConfigDAO;
import org.epragati.reports.dao.ReportsUserConfigDAO;
import org.epragati.reports.dto.ReportsRoleConfigDTO;
import org.epragati.reports.dto.ReportsUserConfigDTO;
import org.epragati.reports.excel.ReportNameAndFieldOrderDAO;
import org.epragati.reports.excel.ReportNameAndFieldOrderDTO;
import org.epragati.reports.service.PaymentReportService;
import org.epragati.restGateway.RestGateWayService;
import org.epragati.rta.reports.vo.ActionCountDetailsVO;
import org.epragati.rta.reports.vo.CCOReportVO;
import org.epragati.rta.reports.vo.CitizenEnclosuresVO;
import org.epragati.rta.reports.vo.CitizenEnclosuresVOList;
import org.epragati.rta.reports.vo.DealerReportVO;
import org.epragati.rta.reports.vo.EODReportVO;
import org.epragati.rta.reports.vo.FitnessReportVO;
import org.epragati.rta.reports.vo.FreshRCReportVO;
import org.epragati.rta.reports.vo.PageDataVo;
import org.epragati.rta.reports.vo.PermitHistoryDeatilsVO;
import org.epragati.rta.reports.vo.PermitHistoryVO;
import org.epragati.rta.reports.vo.RegDtlsForMiningVO;
import org.epragati.rta.reports.vo.RegServiceReportVO;
import org.epragati.rta.reports.vo.ReportInputVO;
import org.epragati.rta.reports.vo.ReportVO;
import org.epragati.rta.reports.vo.RevenueFeeVO;
import org.epragati.rta.reports.vo.RoleBasedCounts;
import org.epragati.rta.reports.vo.StagingRejectedListVO;
import org.epragati.rta.reports.vo.StoppageReportVO;
import org.epragati.rta.reports.vo.TrReportTotalsVO;
import org.epragati.rta.reports.vo.UserWiseEODCount;
import org.epragati.rta.reports.vo.UsersDropDownVO;
import org.epragati.rta.reports.vo.VehicleStrengthVO;
import org.epragati.service.enclosure.mapper.EnclosureImageMapper;
import org.epragati.taxreports.mappers.TaxReportMappers;
import org.epragati.util.DateConverters;
import org.epragati.util.PermitsEnum;
import org.epragati.util.PermitsEnum.PermitRouteCodeEnum;
import org.epragati.util.PermitsEnum.PermitType;
import org.epragati.util.RCActionStatus;
import org.epragati.util.ReportConstants;
import org.epragati.util.RoleEnum;
import org.epragati.util.Status;
import org.epragati.util.Status.AadhaarSeedStatus;
import org.epragati.util.StatusRegistration;
import org.epragati.util.SubHeadCodeEnum;
import org.epragati.util.document.KeyValue;
import org.epragati.util.payment.ClassOfVehicleEnum;
import org.epragati.util.payment.GatewayTypeEnum;
import org.epragati.util.payment.ModuleEnum;
import org.epragati.util.payment.PayStatusEnum;
import org.epragati.util.payment.ServiceCodeEnum;
import org.epragati.util.payment.ServiceCodeEnum.PaymentTaxType;
import org.epragati.util.payment.ServiceEnum;
import org.epragati.vcr.vo.AadharSeedingDistWiseVO;
import org.epragati.vcr.vo.AadharSeedingOfficeWiseVO;
import org.epragati.vcr.vo.EnforcementVcrVO;
import org.epragati.vcr.vo.UnpaidVcrListVO;
import org.epragati.vcr.vo.VcrFinalServiceVO;
import org.epragati.vcr.vo.VcrUnpaidCountOfficewiseVo;
import org.epragati.vcr.vo.VcrUnpaidResultVo;
import org.epragati.vcr.vo.VcrVo;
import org.epragati.vcrImage.dao.VcrFinalServiceDAO;
import org.epragati.vcrImage.dto.VcrFinalServiceDTO;
import org.epragati.vcrImage.mapper.VcrFinalServiceMapper;
import org.hibernate.annotations.Where;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;

@Service
public class PaymentReportsServiceImpl implements PaymentReportService {
	private static final Logger logger = LoggerFactory.getLogger(PaymentReportsServiceImpl.class);

	@Autowired
	private PaymentTransactionDAO paymentTransactionDAO;

	@Autowired
	private ClassOfVehiclesDAO classOfVehiclesDAO;

	@Autowired
	private TaxDetailsDAO taxDetailsDAO;

	@Autowired
	private TaxReportMappers taxReportMappers;

	@Autowired
	private DistrictService districtService;

	@Autowired
	private OfficeDAO officeDAO;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private DistrictDAO districtDAO;

	@Autowired
	private DealerCovDAO dealerCovDAO;

	@Autowired
	private PermitDetailsDAO permitDetailsDAO;

	@Autowired
	private StagingRegistrationDetailsDAO stagingRegistrationDetailsDAO;

	@Autowired
	private MasterUsersDAO masterUsersDAO;

	@Autowired
	private ReportsRoleConfigDAO reportsRoleConfigDAO;

	@Autowired
	private ReportsUserConfigDAO reportsUserConfigDAO;

	@Autowired
	private MandalService mandalService;

	@Autowired
	private VillageDAO villageDAO;

	@Autowired
	private SuspensionDAO suspensionDAO;

	@Autowired
	private RegServiceDAO regServiceDAO;

	@Autowired
	private SuspensionMapper suspensionMapper;

	@Autowired
	private RegServiceMapper regServiceMapper;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private VcrFinalServiceDAO finalServiceDAO;

	@Autowired
	private DistrictMapper districtMapper;

	@Autowired
	private CfstRevenueDAO cfstRevenueDAO;

	@Autowired
	private PaymentReportDAO paymentReportDAO;

	@Autowired
	private OfficeMapper officeMapper;

	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;
	@Autowired
	private AadhaarSeedDAO aadhaarSeedDAO;
	
	@Autowired
	private MasterCovDAO masterCovDAO;

	@Autowired
	private VcrFinalServiceMapper vcrFinalServiceMapper;

	@Autowired
	private RegistrationDetailsMapper registrationDetailsMapper;

	@Autowired
	private RevenueTargetDAO revenueTargetDAO;

	@Autowired
	private PropertiesDAO propertiesDAO;
	@Autowired
	private FcDetailsDAO fcDetailsDAO;

	@Autowired
	private RestGateWayService restGateWayService;

	@Autowired
	private EnclosureImageMapper enclosureImageMapper;

	@Autowired
	private NonPaymentDetailsMapper nonPaymentDetailsMapper;

	@Autowired
	private CitizenEnclosuresDAO citizenEnclosuresDAO;

	@Autowired
	private StagingDetailsMapper stagingDetailsMapper;

	@Autowired
	private DispatcherMapper dispatcherMapper;

	@Autowired
	private EnclosuresDAO enclosuresDAO;

	static List<DistrictVO> districtsList = new ArrayList<>();

	static List<MasterCovDTO> covCodesList = new ArrayList<>();

	static List<MandalVO> mandalList = new ArrayList<>();

	static Map<Integer, List<String>> officeCodesMap = new HashMap<>();

	static Map<Integer, List<String>> villageMap = new HashMap<>();

	static Map<String, String> officeNameWithCodeMap = new HashMap<>();

	@Autowired
	private RegServiceReportDAO regServiceReportDAO;
	@Autowired
	private ReportNameAndFieldOrderDAO reportPropertyDao;
	@Autowired
	private EnforcementReportsServiceImpl enfRepSerImp;
	@Autowired
	private RtaReportServiceImpl rtaServiceReport;
	@Autowired
	private AadhaarSeedMapper aadhaarSeedMapper;
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private RegistrationReportServiceImpl regService;

	@Autowired
	private TrGeneratedReportDAO trGeneratedReportDAO;

	@Autowired
	private PaymentReportService paymentReportService;

	@Autowired
	private TrIssuedReportMapper trIssuedReportMapper;

	@Autowired
	private ApplicantAddressMapper addressMapper;

	@Autowired
	private RegistrationService registrationService;

	@Autowired
	private FreshRCMapper freshRCMapper;

	@Autowired
	private DispatcherSubmissionDAORepo dispatcherSubmissionDAORepo;

	@PostConstruct
	@Override
	public Map<String, String> getOfficeCodesByOfficeName() {
		if (officeNameWithCodeMap.isEmpty()) {
			List<OfficeDTO> offices = officeDAO.findAll();
			offices.forEach(val -> {
				officeNameWithCodeMap.put(val.getOfficeCode(), val.getOfficeName());
			});
		}
		return officeNameWithCodeMap;
	}

	@PostConstruct
	@Override
	public Map<Integer, List<String>> getOfficeCodes() {
		if (officeCodesMap.isEmpty()) {
			if (CollectionUtils.isEmpty(districtsList)) {
				districtsList = districtService.findBySid(NationalityEnum.AP.getName());
			}
			districtsList.stream().forEach(dist -> {
				List<OfficeDTO> officeList = officeDAO.findBydistrict(dist.getDistrictId());
				List<String> officeCodes = officeList.stream().map(office -> office.getOfficeCode())
						.collect(Collectors.toList());
				if (!officeCodes.isEmpty()) {
					officeCodesMap.put(dist.getDistrictId(), officeCodes);
				}
			});
		}
		return officeCodesMap;
	}

	public OfficeDTO getDistrictByofficeCode(String officeCode) {
		Optional<OfficeDTO> officeOptional = officeDAO.findByOfficeCode(officeCode);
		if (!officeOptional.isPresent()) {
			throw new BadRequestException("No District found with officeCode :" + officeCode);
		}
		return officeOptional.get();
	}

	@Override
	public List<RegReportVO> PaymentReportData(RegReportVO regReportVO, Pageable pagable) {

		List<DistrictVO> districtsList = districtService.findBySid(NationalityEnum.AP.getName());

		List<RegReportVO> distTransactionsList = new ArrayList<>();

		LocalDate monthTo = regReportVO.getToDate();
		LocalDate monthFrom = regReportVO.getFromDate();
		List<Integer> months = new ArrayList<>();
		while (monthFrom.isBefore(monthTo) || monthFrom.isEqual(monthTo)) {
			months.add(monthFrom.getMonthValue());
			monthFrom = monthFrom.plusMonths(1);
		}

		List<RevenueTargetDTO> revenueTargetList = revenueTargetDAO
				.findByYearAndMonthIn(regReportVO.getFromDate().getYear(), months);

		Map<Object, Map<String, Double>> dlRevenueMap = dlRevenueConfig(regReportVO);

		if (regReportVO.getDistrictId() != null) {
			logger.info("District Based [{}] Report", regReportVO.getDistrictId());

			List<OfficeDTO> officeCodes = officeDAO.findBydistrict(regReportVO.getDistrictId());
			if (CollectionUtils.isNotEmpty(officeCodes)) {
				List<String> officeList = officeCodes.stream().map(off -> off.getOfficeCode())
						.collect(Collectors.toList());
				officeList.stream().forEach(office -> {
					Map<String, Double> map = districtReportDefaults();
					RegReportVO voo = new RegReportVO();
					revenueDistrictData(regReportVO, Arrays.asList(office), voo, map, distTransactionsList,
							regReportVO.getDistrictName(), regReportVO.getDistrictId(), revenueTargetList,
							dlRevenueMap);
				});

			} else {
				throw new BadRequestException("No Data found for District Id" + regReportVO.getDistrictId());
			}
		} else {
			logger.info("State Based Report");

			Set<DistrictVO> districtSet = districtsList.stream().collect(
					Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(DistrictVO::getDistrictId))));

			districtSet.stream().forEach(dist -> {
				logger.info("district " + dist.getDistrictName());
				RegReportVO vo = new RegReportVO();
				Map<String, Double> map = districtReportDefaults();
				List<String> officeCodes = getOfficeCodes().get(dist.getDistrictId());
				String distName = dist.getDistrictName();
				Integer distId = dist.getDistrictId();

				revenueDistrictData(regReportVO, officeCodes, vo, map, distTransactionsList, distName, distId,
						revenueTargetList, dlRevenueMap);
			});
		}

		setGrandTotals(distTransactionsList);
		handleDecimals(distTransactionsList);
		return distTransactionsList;
	}

	public Map<Object, Map<String, Double>> dlRevenueConfig(RegReportVO regReportVO) {
		Map<Object, Map<String, Double>> dlRevenueMap = new HashMap<Object, Map<String, Double>>();

		Optional<PropertiesDTO> propOptional = propertiesDAO.findByReportType("PAYMENTS");
		if (propOptional.isPresent()) {
			PropertiesDTO propDTO = propOptional.get();
			if (propDTO.isEnableDLRevenue()) {
				dlRevenueMap = restGateWayService.dlRevenue(regReportVO);
				if (MapUtils.isNotEmpty(dlRevenueMap)) {
					logger.info("DL revenue Included");
				}

			}

		}
		return dlRevenueMap;
	}

	public void revenueDistrictData(RegReportVO regReportVO, List<String> officeCodes, RegReportVO vo,
			Map<String, Double> map, List<RegReportVO> distTransactionsList, String distName, Integer distId,
			List<RevenueTargetDTO> revenueTargetList, Map<Object, Map<String, Double>> dlRevenueMap) {

		LocalDateTime fromDate = getTimewithDate(regReportVO.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(regReportVO.getToDate(), true);

		List<PaymentReportDTO> paymentList = paymentReportDAO
				.findByResponseResponseTimeBetweenAndPaymentGatewayTypeInAndOfficeCodeInAndPayStatus(fromDate, toDate,
						getGatewayTypes(regReportVO.getGateWayType()), officeCodes,
						regReportVO.getStatus()/* , new PageRequest(0, 20) */);

		List<PaymentReportDTO> cashPaymentList = paymentReportDAO
				.findByCreatedDateBetweenAndPaymentGatewayTypeAndOfficeCodeInAndPayStatus(fromDate, toDate,
						GatewayTypeEnum.CASH.getId(), officeCodes,
						regReportVO.getStatus()/* , new PageRequest(0, 20) */);
		if (CollectionUtils.isNotEmpty(cashPaymentList)) {
			paymentList.addAll(cashPaymentList);
		}
		if (!paymentList.isEmpty()) {
			List<PaymentReportDTO> breakPaymentsList = paymentList.parallelStream()

					.filter(val -> val.getBreakPaymentsSave() != null
							&& !val.getBreakPaymentsSave().getBreakPayments().isEmpty())
					.collect(Collectors.toList());

			breakPaymentsList.stream().forEach(val -> {
				val.getBreakPaymentsSave().getBreakPayments().stream().forEach(breakPay -> {
					Map<String, Double> breakMap = breakPay.getBreakup();
					breakMap.keySet().stream().forEach(feeType -> {

						setDistBreakPayments(feeType, map, breakMap);
					});
				});
			});
			List<PaymentReportDTO> feeDetailsList = paymentList.parallelStream()
					.filter(feeDetails -> feeDetails.getFeeDetailsDTO() != null
							&& !feeDetails.getFeeDetailsDTO().getFeeDetails().isEmpty())
					.collect(Collectors.toList());

			feeDetailsList.stream().forEach(fee -> {
				setDistFeeDetails(fee.getFeeDetailsDTO().getFeeDetails(), map);
			});
			breakPaymentsList.clear();
			feeDetailsList.clear();

		}
		handleSpApplicationFee(paymentList, map);
		processDlRevenue(map, dlRevenueMap, distId, officeCodes, regReportVO);
		processCfstRevenue(map, regReportVO, officeCodes);
		RegReportVO paymentVO = setFeeData(paymentList, vo, map, revenueTargetList, distId);
		paymentVO.setDistrictName(distName);
		paymentVO.setDistrictId(distId);
		setTotals(paymentVO);
		if (regReportVO.getDistrictId() != null && !officeCodes.isEmpty()) {
			String officeCode = officeCodes.stream().findFirst().get();
			paymentVO.setDistrictName(getOfficeCodesByOfficeName().get(officeCode));
			paymentVO.setOfficeCode(officeCode);
		}
		distTransactionsList.add(paymentVO);

	}

	public void setDistFeeDetails(List<FeesDTO> feeList, Map<String, Double> map) {
		feeList.stream().forEach(fee -> {
			if (ServiceCodeEnum.getserviceFee().contains(fee.getFeesType())) {
				map.put(ServiceCodeEnum.revenueReportHeads.SERVICECHARGE.getDesc(),
						map.get(ServiceCodeEnum.revenueReportHeads.SERVICECHARGE.getDesc()) + fee.getAmount());
			} else if (ServiceCodeEnum.getTaxAmounts().contains(fee.getFeesType())) {
				map.put(ServiceCodeEnum.revenueReportHeads.QUARTERLYTAX.getDesc(),
						map.get(ServiceCodeEnum.revenueReportHeads.QUARTERLYTAX.getDesc()) + fee.getAmount());
			} else if (ServiceCodeEnum.getLFee().contains(fee.getFeesType())) {
				map.put(ServiceCodeEnum.revenueReportHeads.LIFETAX.getDesc(),
						map.get(ServiceCodeEnum.revenueReportHeads.LIFETAX.getDesc()) + fee.getAmount());
			} else if (ServiceCodeEnum.getApplicationFee().contains(fee.getFeesType())) {
				map.put(ServiceCodeEnum.revenueReportHeads.FEES.getDesc(),
						map.get(ServiceCodeEnum.revenueReportHeads.FEES.getDesc()) + fee.getAmount());
			} else if (ServiceCodeEnum.deductionHead().contains(fee.getFeesType())) {
				map.put(ServiceCodeEnum.revenueReportHeads.DEDUCTION.getDesc(),
						map.get(ServiceCodeEnum.revenueReportHeads.DEDUCTION.getDesc()) + fee.getAmount());
			}

		});

	}

	public RegReportVO setFeeData(List<PaymentReportDTO> transactionsList, RegReportVO RegReportVO,
			Map<String, Double> map, List<RevenueTargetDTO> revenueTargetList, Integer distId) {
		Map<String, List<RevenueFeeVO>> feeMap = new LinkedHashMap<String, List<RevenueFeeVO>>();
		map.keySet().stream().forEach(key -> {
			RevenueFeeVO reportVO = new RevenueFeeVO();
			reportVO.setFee(map.get(key));
			reportVO.setTarget(setTargets(key, revenueTargetList, RegReportVO, distId));
			List<RevenueFeeVO> revFeeList = new ArrayList<>();
			revFeeList.add(reportVO);
			feeMap.put(key, revFeeList);
		});
		RegReportVO.setFeeDetails(feeMap);
		return RegReportVO;
	}

	public double setTargets(String key, List<RevenueTargetDTO> revenueTargetList, RegReportVO vo, Integer distId) {
		double target = 0;
		if (!revenueTargetList.isEmpty()) {
			List<RevenueTargetDTO> revenueTargetDistList = revenueTargetList.stream()
					.filter(rev -> rev.getDistrictId() == distId).collect(Collectors.toList());
			if (!revenueTargetDistList.isEmpty()) {

				if (key.equalsIgnoreCase(ServiceCodeEnum.revenueReportHeads.QUARTERLYTAX.getDesc())) {
					target = revenueTargetDistList.stream().map(rev -> rev.getQuarterlyTax())
							.mapToDouble(Double::doubleValue).sum();
				} else if (key.equalsIgnoreCase(ServiceCodeEnum.revenueReportHeads.LIFETAX.getDesc())) {
					target = revenueTargetDistList.stream().map(rev -> rev.getLifeTax())
							.mapToDouble(Double::doubleValue).sum();
				} else if (key.equals(ServiceCodeEnum.revenueReportHeads.DEDUCTION.getDesc())) {
					target = revenueTargetDistList.stream().map(rev -> rev.getDetection())
							.mapToDouble(Double::doubleValue).sum();
				} else if (key.equals(ServiceCodeEnum.revenueReportHeads.FEES.getDesc())) {
					target = revenueTargetDistList.stream().map(rev -> rev.getFee()).mapToDouble(Double::doubleValue)
							.sum();
				} else if (key.equals(ServiceCodeEnum.revenueReportHeads.SERVICECHARGE.getDesc())) {
					target = revenueTargetDistList.stream().map(rev -> rev.getServiceCharge())
							.mapToDouble(Double::doubleValue).sum();
				}

			}
		}
		return target;

	}

	public void processCfstRevenue(Map<String, Double> map, RegReportVO regReportVO, List<String> officeCode) {
		LocalDateTime fromDate = getTimewithDate(regReportVO.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(regReportVO.getToDate(), true);
		List<CfstRevenueDTO> cfstRevenueList = cfstRevenueDAO.findByTransactionDateBetweenAndOfficeCodeIn(fromDate,
				toDate, officeCode);

		if (!cfstRevenueList.isEmpty()) {
			Double fee = cfstRevenueList.stream().collect(Collectors.summingDouble(
					val -> val.getLicFee() + val.getFcFee() + val.getRegFee() + val.getPerFee() + val.getOthFee()));

			Double tax = cfstRevenueList.stream()
					.collect(Collectors.summingDouble(val -> val.getqTax() + val.getGreenTax()));

			Double lifetax = cfstRevenueList.stream().collect(Collectors.summingDouble(val -> val.getLifeTax()));

			Double detection = cfstRevenueList.stream().collect(
					Collectors.summingDouble(val -> val.getDetectionLifeTax() + val.getDetectionLifeTaxPenalty()
							+ val.getDetectionQtax() + val.getDetectionQtaxPenalty() + val.getCompoundFee()));

			Double serviceFee = cfstRevenueList.stream().collect(Collectors.summingDouble(val -> val.getServiceFee()));

			map.put(ServiceCodeEnum.revenueReportHeads.QUARTERLYTAX.getDesc(),
					map.get(ServiceCodeEnum.revenueReportHeads.QUARTERLYTAX.getDesc()) + tax);

			map.put(ServiceCodeEnum.revenueReportHeads.LIFETAX.getDesc(),
					map.get(ServiceCodeEnum.revenueReportHeads.LIFETAX.getDesc()) + lifetax);

			map.put(ServiceCodeEnum.revenueReportHeads.SERVICECHARGE.getDesc(),
					map.get(ServiceCodeEnum.revenueReportHeads.SERVICECHARGE.getDesc()) + serviceFee);

			map.put(ServiceCodeEnum.revenueReportHeads.FEES.getDesc(),
					map.get(ServiceCodeEnum.revenueReportHeads.FEES.getDesc()) + fee);

			map.put(ServiceCodeEnum.revenueReportHeads.DEDUCTION.getDesc(),
					map.get(ServiceCodeEnum.revenueReportHeads.DEDUCTION.getDesc()) + detection);

		}

	}

	public void processDlRevenue(Map<String, Double> regRevenueMap, Map<Object, Map<String, Double>> dlRevenueMap,
			Integer distId, List<String> officeCodes, RegReportVO regReportVO) {
		if (MapUtils.isNotEmpty(dlRevenueMap)) {
			Map<String, Double> dlPaymentsMap = null;
			if (regReportVO.getDistrictId() == null) {
				dlPaymentsMap = dlRevenueMap.get(String.valueOf(distId));
			} else {
				if (CollectionUtils.isNotEmpty(officeCodes)) {
					dlPaymentsMap = dlRevenueMap.get(officeCodes.stream().findFirst().get());
				}
			}
			logger.debug("DL Revenue Map Data [{}]", dlPaymentsMap);
			if (MapUtils.isNotEmpty(dlPaymentsMap)) {

				double dlFee = dlPaymentsMap.get(SubHeadCodeEnum.APPLICATION_FEE.getTypeDesc())
						+ dlPaymentsMap.get(SubHeadCodeEnum.TEST_FEE.getTypeDesc())
						+ dlPaymentsMap.get(SubHeadCodeEnum.CARD_FEE.getTypeDesc())
						+ dlPaymentsMap.get(SubHeadCodeEnum.LATE_FEE.getTypeDesc());
				regRevenueMap.put(ServiceCodeEnum.revenueReportHeads.FEES.getDesc(),
						regRevenueMap.get(ServiceCodeEnum.revenueReportHeads.FEES.getDesc()) + dlFee);

				double dlServiceFee = dlPaymentsMap.get(SubHeadCodeEnum.SERVICE_FEE.getTypeDesc())
						+ dlPaymentsMap.get(SubHeadCodeEnum.POSTAL_FEE.getTypeDesc());
				regRevenueMap.put(ServiceCodeEnum.revenueReportHeads.SERVICECHARGE.getDesc(),
						regRevenueMap.get(ServiceCodeEnum.revenueReportHeads.SERVICECHARGE.getDesc()) + dlServiceFee);
			}
		}
	}

	public void handleSpApplicationFee(List<PaymentReportDTO> transactionsList, Map<String, Double> map) {
		List<FeesDTO> spApplication = transactionsList.parallelStream()
				.filter(sp -> sp.getFeeDetailsDTO() != null && !sp.getFeeDetailsDTO().getFeeDetails().isEmpty()
						&& (sp.getModuleCode().equals(ServiceEnum.SPNR.getCode())
								|| sp.getModuleCode().equals(ServiceEnum.SPNB.getCode()))
						&& sp.getPayURefundResponse() != null
						&& StringUtils.isNotEmpty(sp.getPayURefundResponse().getMessage())
						&& !sp.getPayURefundResponse().getMessage().equalsIgnoreCase("Refund Initiated"))
				.map(val -> val.getFeeDetailsDTO().getFeeDetails()).flatMap(x -> x.stream())
				.collect(Collectors.toList());
		if (!spApplication.isEmpty()) {
			double spApplicationFee = spApplication.stream()
					.filter(val -> val.getFeesType() != null && val.getAmount() != null
							&& val.getFeesType().equalsIgnoreCase(ServiceCodeEnum.SP_APPLICATION_FEE.getTypeDesc()))
					.map(FeesDTO::getAmount).mapToDouble(Double::doubleValue).sum();
			map.put(ServiceCodeEnum.revenueReportHeads.FEES.getDesc(),
					map.get(ServiceCodeEnum.revenueReportHeads.FEES.getDesc()) + spApplicationFee);
			logger.info("SP Fee [{}]", spApplicationFee);
		}
	}

	public void setGrandTotals(List<RegReportVO> distTransactionsList) {
		RegReportVO regReport = new RegReportVO();
		regReport.setDistrictName("TOTAL");
		Map<String, List<RevenueFeeVO>> feeMap = new LinkedHashMap<>();
		ServiceCodeEnum.revenueHeadDefaults().stream().forEach(key -> {
			RevenueFeeVO vo = new RevenueFeeVO();
			List<RevenueFeeVO> reportList = distTransactionsList.stream().map(val -> val.getFeeDetails().get(key))
					.flatMap(x -> x.stream()).collect(Collectors.toList());
			double feeTotal = reportList.stream().map(val -> val.getFee()).mapToDouble(Double::doubleValue).sum();
			double targetTotal = reportList.stream().map(val -> val.getTarget()).mapToDouble(Double::doubleValue).sum();
			double achievedTotal = reportList.stream().map(val -> val.getAchieved()).mapToDouble(Double::doubleValue)
					.sum();
			vo.setFee(feeTotal);
			vo.setTarget(targetTotal);
			vo.setAchieved(achievedTotal);
			List<RevenueFeeVO> revenueList = new ArrayList<>();
			revenueList.add(vo);
			feeMap.put(key, revenueList);
		});
		regReport.setFeeDetails(feeMap);
		setTotals(regReport);
		distTransactionsList.add(regReport);

	}

	public void setTotals(RegReportVO paymentVO) {
		List<RevenueFeeVO> voList = new ArrayList<>();
		Map<String, List<RevenueFeeVO>> feeDetailsMap = paymentVO.getFeeDetails();
		if (!feeDetailsMap.isEmpty()) {
			List<RevenueFeeVO> revenueList = feeDetailsMap.keySet().stream().map(val -> feeDetailsMap.get(val))
					.flatMap(x -> x.stream()).collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(revenueList)) {
				double fee = revenueList.stream().map(val -> val.getFee()).mapToDouble(Double::doubleValue).sum();
				double target = revenueList.stream().map(val -> val.getTarget()).mapToDouble(Double::doubleValue).sum();
				double achieved = revenueList.stream().map(val -> val.getAchieved()).mapToDouble(Double::doubleValue)
						.sum();
				RevenueFeeVO vo = new RevenueFeeVO();
				vo.setFee(fee);
				vo.setTarget(target);
				vo.setAchieved(achieved);
				voList.add(vo);
			}
		}
		feeDetailsMap.put("TOTAL", voList);
	}

	public void handleDecimals(List<RegReportVO> distTransactionsList) {
		distTransactionsList.stream().forEach(val -> {
			Map<String, List<RevenueFeeVO>> feeDetailsMap = val.getFeeDetails();
			if (!feeDetailsMap.isEmpty()) {
				feeDetailsMap.keySet().stream().forEach(key -> {
					List<RevenueFeeVO> revenueList = feeDetailsMap.get(key);
					if (CollectionUtils.isNotEmpty(revenueList)) {
						revenueList.stream().forEach(rev -> {
							rev.setFee(convertLakh(rev.getFee()));
							if (rev.getTarget() > 0) {
								rev.setAchieved((rev.getFee() * 100) / rev.getTarget());
							}
							rev.setFee(limitDecimal(rev.getFee()));
							rev.setAchieved(limitDecimal(rev.getAchieved()));
							rev.setTarget(limitDecimal(rev.getTarget()));

						});

					}
				});
			}

		});

	}

	public double convertLakh(double input) {
		return input / 100000;
		// return input;
	}

	public double limitDecimal(double input) {
		try {

			// DecimalFormat df2 = new DecimalFormat("#.##");

			// double d = Double.valueOf(df2.format(input));

			return truncateTo(input, 2);

			// return d;

		} catch (

		Exception e) {
			logger.info("Exception occured for limiting decimal and conversion [{}]  for input [{}]" + e.getMessage(),
					input);

			logger.error("Exception occured for limiting decimal and conversion [{}]" + e.getMessage());
			return 0;
		}
	}

	double truncateTo(double unroundedNumber, int decimalPlaces) {
		int truncatedNumberInt = (int) (unroundedNumber * Math.pow(10, decimalPlaces));
		double truncatedNumber = (double) (truncatedNumberInt / Math.pow(10, decimalPlaces));

		return truncatedNumber;
	}

	@Override
	public Optional<ReportsVO> getpaymenttransactions(RegReportVO RegReportVO, Pageable pagable) {
		LocalDateTime fromDate = getTimewithDate(RegReportVO.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(RegReportVO.getToDate(), true);
		List<RegReportVO> paymentreportList = new ArrayList<>();
		int pageSize = pagable.getPageSize();

		Pageable page = new PageRequest(pagable.getPageNumber(), pageSize / 2);

		Page<PaymentTransactionDTO> reportsOpt = paymentTransactionDAO
				.findByResponseResponseTimeBetweenAndPaymentGatewayTypeInAndOfficeCodeInAndPayStatus(fromDate, toDate,
						getGatewayTypes(RegReportVO.getGateWayType()), Arrays.asList(RegReportVO.getOfficeCode()),
						RegReportVO.getStatus(), page.previousOrFirst());

		if (reportsOpt.hasContent()) {
			List<PaymentTransactionDTO> paymentsList = reportsOpt.getContent();
			paymentreportList = revenueDetailedData(paymentsList, RegReportVO);

		} else {
			page = new PageRequest(pagable.getPageNumber(), pageSize);

		}
		List<RegReportVO> dlRevenueList = restGateWayService.dlRevenueDetailed(RegReportVO, page.previousOrFirst());
		paymentreportList.addAll(dlRevenueList);
		if (!paymentreportList.isEmpty()) {
			ReportsVO reportVO = new ReportsVO();
			reportVO.setPaymentReport(paymentreportList);
			reportVO.setPageNo(reportsOpt.getNumber());
			reportVO.setTotalPage(reportsOpt.getTotalPages());
			return Optional.of(reportVO);
		} else
			return Optional.empty();
	}

	@Override
	public List<RegReportVO> revenueDetailedData(List<PaymentTransactionDTO> paymentsList, RegReportVO RegReportVO) {
		if (!paymentsList.isEmpty()) {
			Map<String, Double> total = new HashMap<>();
			total.put(ServiceCodeEnum.ReportEnum.TOTAL.getCode(), 0.0);
			List<RegReportVO> paymentReportsList = new ArrayList<>();
			paymentsList.stream().forEach(val -> {
				Map<String, Double> map = districtReportDefaults();
				if (val.getBreakPaymentsSave() != null) {
					List<BreakPayments> breakPaymentsList = val.getBreakPaymentsSave().getBreakPayments();
					if (!breakPaymentsList.isEmpty()) {

						breakPaymentsList.stream().forEach(breakPayment -> {
							Map<String, Double> feeMap = breakPayment.getBreakup();

							feeMap.keySet().stream().forEach(feeType -> {
								if (map.get(feeType) != null) {
									setDistBreakPayments(feeType, map, feeMap);
								}
							});
						});
						total.put(ServiceCodeEnum.ReportEnum.TOTAL.getCode(),
								val.getBreakPaymentsSave().getGrandTotalFees());
					}
				}

				else if (val.getFeeDetailsDTO() != null && val.getFeeDetailsDTO().getFeeDetails() != null) {
					setDistFeeDetails(val.getFeeDetailsDTO().getFeeDetails(), map);
					total.put(ServiceCodeEnum.ReportEnum.TOTAL.getCode(), val.getFeeDetailsDTO().getTotalFees());
				}
				RegReportVO vo = new RegReportVO();
				vo.setTransactionDate(val.getResponse().getResponseTime());
				vo.setModule("REG");
				vo.setTotal(total.get(ServiceCodeEnum.ReportEnum.TOTAL.getCode()));
				vo.setTransactionNo(val.getTransactioNo());
				vo.setOfficeCode(val.getOfficeCode());
				vo.setOfficeName(getOfficeCodesByOfficeName().get(val.getOfficeCode()));
				vo.setDistrictName(RegReportVO.getDistrictName());
				vo.setGateWay(setGatewayType(val.getPaymentGatewayType()));
				RegReportVO paymentReport = setReportData(vo, map);
				paymentReportsList.add(paymentReport);
			});
			return paymentReportsList;
		}
		return Collections.emptyList();
	}

	public Map<String, Double> setFeeDefaults() {
		Map<String, Double> map = new LinkedHashMap<>();
		ServiceCodeEnum.getFeeTypes().stream().forEach(val -> {
			map.put(val, 0.0);
		});
		return map;
	}

	public String setGatewayType(Integer gateWay) {
		GatewayTypeEnum gateWays = Arrays.asList(GatewayTypeEnum.values()).stream()
				.filter(val -> val.getId() == gateWay).findAny().orElse(null);
		if (gateWays != null) {
			return gateWays.getDescription();
		}
		return StringUtils.EMPTY;
	}

	@Override
	public List<RegReportVO> getPaymentsReport(List<PaymentTransactionDTO> paymentsList, RegReportVO RegReportVO) {
		if (!paymentsList.isEmpty()) {
			Map<String, Double> total = new HashMap<>();
			total.put(ServiceCodeEnum.ReportEnum.TOTAL.getCode(), 0.0);
			List<RegReportVO> paymentReportsList = new ArrayList<>();
			paymentsList.stream().forEach(val -> {
				Map<String, Double> map = setFeeDefaults();
				if (val.getBreakPaymentsSave() != null) {
					List<BreakPayments> breakPaymentsList = val.getBreakPaymentsSave().getBreakPayments();
					if (!breakPaymentsList.isEmpty()) {
						breakPaymentsList.stream().forEach(breakPayment -> {
							Map<String, Double> feeMap = breakPayment.getBreakup();
							feeMap.keySet().stream().forEach(feeType -> {
								if (map.get(feeType) != null) {
									map.put(feeType, map.get(feeType) + feeMap.get(feeType));
								}
							});
						});
						total.put(ServiceCodeEnum.ReportEnum.TOTAL.getCode(),
								val.getBreakPaymentsSave().getGrandTotalFees());
					}
				}

				else if (val.getFeeDetailsDTO() != null && val.getFeeDetailsDTO().getFeeDetails() != null) {
					getFeeDetails(val, map);
					if (val.getModuleCode().equals(ModuleEnum.SPNB.getCode())
							|| val.getModuleCode().equals(ModuleEnum.SPNR.getCode())) {
						total.put(ServiceCodeEnum.ReportEnum.TOTAL.getCode(),
								map.keySet().stream()
										.filter(key -> !Arrays.asList(ServiceCodeEnum.SPNB_REFUND.getTypeDesc(),
												ServiceCodeEnum.SPNR_REFUND.getTypeDesc()).contains(key))
										.map(v -> map.get(v)).mapToDouble(Double::doubleValue).sum());
					} else {
						total.put(ServiceCodeEnum.ReportEnum.TOTAL.getCode(), val.getFeeDetailsDTO().getTotalFees());
					}
				}
				RegReportVO vo = new RegReportVO();
				vo.setTransactionDate(val.getResponse().getResponseTime());
				vo.setModule("REG");
				vo.setTotal(total.get(ServiceCodeEnum.ReportEnum.TOTAL.getCode()));
				vo.setTransactionNo(val.getTransactioNo());
				vo.setOfficeCode(val.getOfficeCode());
				vo.setDistrictName(RegReportVO.getDistrictName());
				vo.setGateWay(setGatewayType(val.getPaymentGatewayType()));
				RegReportVO paymentReport = setReportData(vo, map);
				paymentReportsList.add(paymentReport);
			});
			return paymentReportsList;
		}
		return Collections.emptyList();
	}

	public void setDistFeeDetails(List<FeesDTO> feeList, Map<String, Double> map, Set<Integer> serviceIds) {
		feeList.stream().forEach(fee -> {
			if (ServiceCodeEnum.getServiceTypeFee().contains(fee.getFeesType())) {
				map.put(ServiceCodeEnum.SERVICE_FEE.getCode(),
						map.get(ServiceCodeEnum.SERVICE_FEE.getCode()) + fee.getAmount());
			} else if (ServiceCodeEnum.getTaxAmounts().contains(fee.getFeesType())) {
				map.put(ServiceCodeEnum.ReportEnum.TAXAMOUNTS.getCode(),
						map.get(ServiceCodeEnum.ReportEnum.TAXAMOUNTS.getCode()) + fee.getAmount());
			} else if (ServiceCodeEnum.getIndiviualFees().contains(fee.getFeesType())) {
				map.put(fee.getFeesType(), map.get(fee.getFeesType()) + fee.getAmount());
			} else if (ServiceCodeEnum.getCessFee().contains(fee.getFeesType())) {
				map.put(ServiceCodeEnum.CESS_FEE.getCode(),
						map.get(ServiceCodeEnum.CESS_FEE.getCode()) + fee.getAmount());
			} else if (ServiceCodeEnum.getLFee().contains(fee.getFeesType())) {
				map.put(ServiceCodeEnum.LIFE_TAX.getCode(),
						map.get(ServiceCodeEnum.LIFE_TAX.getCode()) + fee.getAmount());
			} else if (ServiceCodeEnum.getApplicationFee().contains(fee.getFeesType())
					|| (ServiceCodeEnum.LATE_FEE.getTypeDesc().equals(fee.getFeesType())
							&& serviceIds.contains(ServiceEnum.RENEWAL.getId()))) {
				map.put(ServiceCodeEnum.REGISTRATION.getCode(),
						map.get(ServiceCodeEnum.REGISTRATION.getCode()) + fee.getAmount());
			}
			setPermitFees(fee, map, serviceIds);
		});

	}

	public void spApplicationFee(FeesDTO fee) {
		// fee.get

	}

	public void getFeeDetails(PaymentTransactionDTO val, Map<String, Double> map) {
		List<FeesDTO> feeDetailsList = val.getFeeDetailsDTO().getFeeDetails();
		if (!feeDetailsList.isEmpty()) {
			feeDetailsList.stream().forEach(feeDetails -> {
				if (map.get(feeDetails.getFeesType()) != null) {
					map.put(feeDetails.getFeesType(), map.get(feeDetails.getFeesType()) + feeDetails.getAmount());
				}

				else if (ServiceCodeEnum.getLFee().contains(feeDetails.getFeesType())) {
					map.put(ServiceCodeEnum.LIFE_TAX.getCode(),
							map.get(ServiceCodeEnum.LIFE_TAX.getCode()) + feeDetails.getAmount());
				}

				else if (feeDetails.getFeesType().equals(ServiceCodeEnum.CESS_FEE.getTypeDesc())) {
					map.put(ServiceCodeEnum.CESS_FEE.getCode(),
							map.get(ServiceCodeEnum.CESS_FEE.getCode()) + feeDetails.getAmount());
				} else if (feeDetails.getFeesType().equals(ServiceCodeEnum.SP_APPLICATION_FEE.getTypeDesc())) {
					double refund = val.getFeeDetailsDTO().getRefundAmound();
					map.put(ServiceCodeEnum.REGISTRATION.getCode(),
							map.get(ServiceCodeEnum.REGISTRATION.getCode()) + feeDetails.getAmount() - refund);

					if (val.getModuleCode().equals(ModuleEnum.SPNB.getCode())) {
						map.put(ServiceCodeEnum.SPNB_REFUND.getTypeDesc(),
								map.get(ServiceCodeEnum.SPNB_REFUND.getTypeDesc()) + refund);
					} else if (val.getModuleCode().equals(ModuleEnum.SPNR.getCode())) {
						map.put(ServiceCodeEnum.SPNR_REFUND.getTypeDesc(),
								map.get(ServiceCodeEnum.SPNR_REFUND.getTypeDesc()) + refund);
					}
				}
			});
		}
	}

	@Override
	public RegReportVO setReportData(RegReportVO RegReportVO, Map<String, Double> map) {
		List<ReportVO> feeList = new ArrayList<>();
		map.keySet().stream().forEach(key -> {
			ReportVO feeVO = new ReportVO();
			feeVO.setFeeType(key);
			feeVO.setFee(map.get(key));
			feeList.add(feeVO);
		});
		RegReportVO.setFeeReport(feeList);
		return RegReportVO;
	}

	public void setPermitFees(FeesDTO feeDTO, Map<String, Double> map, Set<Integer> serviceIds) {
		if (ServiceCodeEnum.PERMIT_FEE.getTypeDesc().equals(feeDTO.getFeesType())
				|| ServiceCodeEnum.AUTHORIZATION.getTypeDesc().equals(feeDTO.getFeesType())
				|| (ServiceCodeEnum.LATE_FEE.getTypeDesc().equals(feeDTO.getFeesType())
						&& serviceIds.contains(ServiceEnum.RENEWALOFPERMIT.getId()))) {
			map.put(ServiceCodeEnum.PERMIT_FEE.getTypeDesc(),
					map.get(ServiceCodeEnum.PERMIT_FEE.getTypeDesc()) + feeDTO.getAmount());
		} else if (ServiceCodeEnum.PERMIT_SERVICE_FEE.getTypeDesc().equals(feeDTO.getFeesType())) {
			map.put(feeDTO.getFeesType(), map.get(feeDTO.getFeesType()) + feeDTO.getAmount());
		}
	}

	public Map<String, Double> districtReportDefaults() {
		Map<String, Double> map = new HashMap<>();
		ServiceCodeEnum.districtDefaults().stream().forEach(val -> {
			map.put(val, 0.0);
		});
		return map;

	}

	public void setDistBreakPayments(String feeType, Map<String, Double> map, Map<String, Double> breakMap) {
		if (ServiceCodeEnum.getServiceTypeFee().contains(feeType)) {
			map.put(ServiceCodeEnum.SERVICE_FEE.getCode(),
					map.get(ServiceCodeEnum.SERVICE_FEE.getCode()) + breakMap.get(feeType));
		} else if (ServiceCodeEnum.getApplicationFee().contains(feeType)) {
			map.put(ServiceCodeEnum.REGISTRATION.getCode(),
					map.get(ServiceCodeEnum.REGISTRATION.getCode()) + breakMap.get(feeType));
		} else if (ServiceCodeEnum.getTaxAmounts().contains(feeType)) {
			map.put(ServiceCodeEnum.ReportEnum.TAXAMOUNTS.getCode(),
					map.get(ServiceCodeEnum.ReportEnum.TAXAMOUNTS.getCode()) + breakMap.get(feeType));
		} else if (ServiceCodeEnum.getIndiviualFees().contains(feeType)) {
			map.put(feeType, map.get(feeType) + breakMap.get(feeType));
		} else if (ServiceCodeEnum.getLFee().contains(feeType)) {
			map.put(ServiceCodeEnum.LIFE_TAX.getCode(),
					map.get(ServiceCodeEnum.LIFE_TAX.getCode()) + breakMap.get(feeType));
		} else if (ServiceCodeEnum.getCessFee().contains(feeType)) {
			map.put(ServiceCodeEnum.CESS_FEE.getCode(),
					map.get(ServiceCodeEnum.CESS_FEE.getCode()) + breakMap.get(feeType));
		}
	}

	@Override
	public List<RegReportVO> getDistrictReports(RegReportVO RegReportVO) {
		List<DistrictVO> districtsList = districtService.findBySid(NationalityEnum.AP.getName());
		Set<Integer> distIds = districtsList.stream().map(distMap -> distMap.getDistrictId())
				.collect(Collectors.toSet());

		Set<DistrictVO> districtSet = districtsList.stream().collect(
				Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(DistrictVO::getDistrictName))));

		/*
		 * Set<DistrictVO> districtSet = new HashSet<>(); distIds.stream().forEach(dist
		 * -> { DistrictVO dsitVO = districtsList.stream().filter(di ->
		 * di.getDistrictId() == dist).findFirst().get(); districtSet.add(dsitVO); });
		 */

		List<RegReportVO> distTransactionsList = new ArrayList<>();
		districtSet.stream().forEach(dist -> {
			RegReportVO vo = new RegReportVO();
			Map<String, Double> map = districtReportDefaults();
			List<String> officeCodes = getOfficeCodes().get(dist.getDistrictId());
			LocalDateTime fromDate = getTimewithDate(RegReportVO.getFromDate(), false);
			LocalDateTime toDate = getTimewithDate(RegReportVO.getToDate(), true);

			List<PaymentTransactionDTO> paymentList = paymentTransactionDAO
					.findByResponseResponseTimeBetweenAndPaymentGatewayTypeInAndOfficeCodeInAndPayStatus(fromDate,
							toDate, getGatewayTypes(RegReportVO.getGateWayType()), officeCodes,
							RegReportVO.getStatus()/* , new PageRequest(0, 20) */);
			List<PaymentTransactionDTO> transactionsList = Collections.emptyList();
			if (!paymentList.isEmpty()) {

				transactionsList = paymentList;
				List<PaymentTransactionDTO> breakPaymentsList = transactionsList.stream()
						.filter(val -> val.getBreakPaymentsSave() != null
								&& !val.getBreakPaymentsSave().getBreakPayments().isEmpty())
						.collect(Collectors.toList());

				breakPaymentsList.stream().forEach(val -> {
					val.getBreakPaymentsSave().getBreakPayments().stream().forEach(breakPay -> {
						Map<String, Double> breakMap = breakPay.getBreakup();
						breakMap.keySet().stream().forEach(feeType -> {
							setDistBreakPayments(feeType, map, breakMap);
						});
					});
				});
				List<PaymentTransactionDTO> feeDetailsList = transactionsList.stream()
						.filter(feeDetails -> feeDetails.getFeeDetailsDTO() != null
								&& !feeDetails.getFeeDetailsDTO().getFeeDetails().isEmpty())
						.collect(Collectors.toList());

				feeDetailsList.stream().forEach(fee -> {
					setDistFeeDetails(fee.getFeeDetailsDTO().getFeeDetails(), map, fee.getServiceIds());
				});

			}
			RegReportVO paymentVO = setReportData(vo, map);
			spApplicationFee(transactionsList, paymentVO);
			paymentVO.setDistrictName(dist.getDistrictName());
			if (!transactionsList.isEmpty()) {
				paymentVO.setTotalTransactionCount(transactionsList.size());
			}
			distTransactionsList.add(paymentVO);

		});
		distTransactionsList.stream().forEach(vo -> {
			Double totalFee = vo.getFeeReport().stream().map(v -> v.getFee()).mapToDouble(Double::doubleValue).sum();
			vo.setTotal(totalFee);
		});
		return distTransactionsList;
	}

	public void spApplicationFee(List<PaymentTransactionDTO> transactionsList, RegReportVO paymentVO) {
		List<FeesDTO> spApplication = transactionsList.stream()
				.filter(sp -> sp.getFeeDetailsDTO() != null && !sp.getFeeDetailsDTO().getFeeDetails().isEmpty()
						&& (sp.getModuleCode().equals(ServiceEnum.SPNR.getCode())
								|| sp.getModuleCode().equals(ServiceEnum.SPNB.getCode()))
						&& sp.getPayURefundResponse() != null
						&& StringUtils.isNotEmpty(sp.getPayURefundResponse().getMessage())
						&& !sp.getPayURefundResponse().getMessage().equalsIgnoreCase("Refund Initiated"))
				.map(val -> val.getFeeDetailsDTO().getFeeDetails()).flatMap(x -> x.stream())
				.collect(Collectors.toList());

		double spApplicationFee = spApplication.stream()
				.filter(val -> val.getFeesType() != null && val.getAmount() != null
						&& val.getFeesType().equalsIgnoreCase(ServiceCodeEnum.SP_APPLICATION_FEE.getTypeDesc()))
				.map(FeesDTO::getAmount).mapToDouble(Double::doubleValue).sum();

		ReportVO report = paymentVO.getFeeReport().stream()
				.filter(val -> val.getFeeType().equals(ServiceCodeEnum.REGISTRATION.getCode())).findFirst().get();
		report.setFee(report.getFee() + spApplicationFee);
	}

	@Override
	public RegReportVO vehicleStrengthReport(RegReportVO regReportVO, JwtUser jwtUser) {
		List<VehicleStrengthVO> vehicleStrenghtReport = new ArrayList<>();
		List<ReportVO> covReport = new ArrayList<>();
		if (regReportVO.getDistrictId() != null && StringUtils.isEmpty(regReportVO.getOfficeCode())
				&& regReportVO.getMandalId() == null
				&& regReportVO.getRole().equalsIgnoreCase(RoleEnum.DTC.getName())) {
			vehicleStrenghtReport = getDistBasedCovCount(regReportVO);
			vehicleStrenghtReport.stream().forEach(val -> {
				covReport.addAll(val.getCovReport());
			});
		}

		else if (regReportVO.getDistrictId() == null && StringUtils.isEmpty(regReportVO.getOfficeCode())
				&& regReportVO.getMandalId() == null && StringUtils.isNotEmpty(jwtUser.getOfficeCode())) {
			vehicleStrenghtReport = getDistBasedCovCountForLoginUser(regReportVO, jwtUser);
			vehicleStrenghtReport.stream().forEach(val -> {
				covReport.addAll(val.getCovReport());
			});
		}

		else if (StringUtils.isNotEmpty(regReportVO.getOfficeCode()) && regReportVO.getMandalId() == null
				|| regReportVO.getDistrictId() == null && StringUtils.isNotEmpty(regReportVO.getOfficeCode())) {
			vehicleStrenghtReport = getMandalBasedCovCount(regReportVO);
		}

		else if (regReportVO.getDistrictId() != null && regReportVO.getMandalId() != null) {
			vehicleStrenghtReport = getVillagesBasedCovCount(regReportVO);
		}

		Map<String, Long> covCountMap = covReport.stream().filter(val -> val.getCov() != null)
				.collect(Collectors.groupingBy(ReportVO::getCov, Collectors.summingLong(ReportVO::getCovCount)));
		regReportVO.setVehicleStrength(vehicleStrenghtReport);
		RegReportVO payment = setVehicleStrengthReport(regReportVO, covCountMap);
		return payment;
	}

	public List<VehicleStrengthVO> getDistBasedCovCount(RegReportVO regReportVO) {
		List<VehicleStrengthVO> vehicleStrenght = new ArrayList<>();
		logger.info("vehicle Strength report based on district [{}]", regReportVO.getDistrictId());
		List<String> officeCodes = getOfficeCodes().get(regReportVO.getDistrictId());
		if (CollectionUtils.isEmpty(officeCodes)) {
			throw new BadRequestException("No office found for District [{" + regReportVO.getDistrictId() + "}]");
		}
		List<String> vehicleType = getVehicleType(regReportVO);
		officeCodes.stream().forEach(office -> {
			VehicleStrengthVO vehiceStrenghtVO = new VehicleStrengthVO();
			List<ReportVO> result = doAggregation(regReportVO, vehicleType, ReportConstants.OFFICE_CODE, office);
			vehiceStrenghtVO.setCovReport(result);
			vehiceStrenghtVO.setOfficeCode(office);
			vehicleStrenght.add(vehiceStrenghtVO);
		});
		return vehicleStrenght;
	}

	private List<VehicleStrengthVO> getDistBasedCovCountForLoginUser(RegReportVO regReportVO, JwtUser jwtUser) {
		List<VehicleStrengthVO> vehicleStrenght = new ArrayList<>();
		List<String> officeCodes = Arrays.asList(jwtUser.getOfficeCode());
		if (CollectionUtils.isEmpty(officeCodes)) {
			throw new BadRequestException("No office found for District [{" + regReportVO.getDistrictId() + "}]");
		}
		List<String> vehicleType = getVehicleType(regReportVO);
		officeCodes.stream().forEach(office -> {
			VehicleStrengthVO vehiceStrenghtVO = new VehicleStrengthVO();
			List<ReportVO> result = doAggregation(regReportVO, vehicleType, ReportConstants.OFFICE_CODE, office);
			vehiceStrenghtVO.setCovReport(result);
			vehiceStrenghtVO.setOfficeCode(office);
			vehicleStrenght.add(vehiceStrenghtVO);
		});
		return vehicleStrenght;
	}

	public List<VehicleStrengthVO> getMandalBasedCovCount(RegReportVO regReportVO) {

		if (regReportVO.getDistrictId() == null) {
			regReportVO.setDistrictId(officeDAO.findByOfficeCode(regReportVO.getOfficeCode()).get().getDistrict());
		}

		mandalList = mandalService.getMandalsbasedOnOffice(regReportVO.getDistrictId(), regReportVO.getOfficeCode());

		if (CollectionUtils.isEmpty(mandalList)) {
			throw new BadRequestException("mandals not found for office [{" + regReportVO.getOfficeCode() + "}]");
		}
		logger.info("vehicle Strength report based on mandals for officeCode [{}]", regReportVO.getOfficeCode());
		List<String> vehicleType = getVehicleType(regReportVO);
		List<VehicleStrengthVO> vehicleStrenght = new ArrayList<>();
		mandalList.stream().forEach(mandal -> {
			VehicleStrengthVO vehiceStrenghtVO = new VehicleStrengthVO();
			List<ReportVO> result = doAggregation(regReportVO, vehicleType, ReportConstants.MANDAL_NAME,
					mandal.getMandalName());
			vehiceStrenghtVO.setCovReport(result);
			vehiceStrenghtVO.setMandalName(mandal.getMandalName());
			vehiceStrenghtVO.setMandalId(mandal.getMandalCode());
			vehicleStrenght.add(vehiceStrenghtVO);
		});
		return vehicleStrenght;
	}

	public List<ReportVO> doAggregation(RegReportVO regReportVO, List<String> vehicleType, String type, String value) {
		Aggregation agg = newAggregation(
				match(Criteria.where(ReportConstants.PRGENERATED_DATE).gt(regReportVO.getFromDate())
						.lt(regReportVO.getToDate()).and(type).is(value).and(ReportConstants.VEHICLE_TYPE)
						.in(vehicleType).and(ReportConstants.IS_ACTIVE).ne(false).and(ReportConstants.NOC_DETAILS)
						.exists(false)),
				group(ReportConstants.CLASS_OF_VEH_DESC).count().as("covCount"),
				project("covCount").and("cov").previousOperation()

		);
		AggregationResults<ReportVO> groupResults = mongoTemplate.aggregate(agg, RegistrationDetailsDTO.class,
				ReportVO.class);
		List<ReportVO> result = groupResults.getMappedResults();
		return result;
	}

	public List<VehicleStrengthVO> getVillagesBasedCovCount(RegReportVO RegReportVO) {
		if (RegReportVO.getMandalId() == null) {
			throw new BadRequestException("MandalId is missing");
		}
		List<String> villagesList = villageMap.get(RegReportVO.getMandalId());
		if (CollectionUtils.isEmpty(villagesList)) {
			List<VillageDTO> villages = villageDAO.findByMandalId(RegReportVO.getMandalId());
			if (CollectionUtils.isNotEmpty(villages)) {
				villagesList = villages.stream().map(village -> village.getVillageName()).collect(Collectors.toList());
				villageMap.put(RegReportVO.getMandalId(), villagesList);
			} else {
				throw new BadRequestException("No villages found for mandal [" + RegReportVO.getMandalId() + "]");
			}
		}
		logger.info("vehicle Strength report based on villages for mandal[{}]", RegReportVO.getMandalId());
		List<String> vehicleType = getVehicleType(RegReportVO);
		List<VehicleStrengthVO> vehicleStrenght = new ArrayList<>();
		villagesList.stream().forEach(village -> {
			VehicleStrengthVO vehiceStrenghtVO = new VehicleStrengthVO();
			List<ReportVO> result = doAggregation(RegReportVO, vehicleType, ReportConstants.VILLAGE_NAME, village);
			vehiceStrenghtVO.setCovReport(result);
			vehiceStrenghtVO.setVillageName(village);
			vehicleStrenght.add(vehiceStrenghtVO);
		});
		return vehicleStrenght;
	}

	public List<String> getVehicleType(RegReportVO RegReportVO) {
		List<String> vehicleType = new ArrayList<>();
		if (RegReportVO.getVehicleType().equals("BOTH")) {
			vehicleType.addAll(Arrays.asList(CovCategory.N.getCode(), CovCategory.T.getCode()));
		} else {
			vehicleType.add(RegReportVO.getVehicleType());
		}
		return vehicleType;
	}

	public RegReportVO setVehicleStrengthReport(RegReportVO RegReportVO, Map<String, Long> map) {
		List<ReportVO> covCountList = new ArrayList<>();
		map.keySet().stream().forEach(key -> {
			ReportVO feeVO = new ReportVO();
			feeVO.setCov(key);
			feeVO.setCovCount(map.get(key));
			covCountList.add(feeVO);
		});
		RegReportVO.setCovReport(covCountList);
		return RegReportVO;
	}

	@Override
	public List<RegReportVO> statePermitReport(RegReportVO vo, JwtUser jwtUser) {
		logger.info("state permit count from: [{}] and to :[{}] ", vo.getFromDate(), vo.getToDate());
		List<RegReportVO> regReport = new ArrayList<>();
		LocalDateTime fromDate = getTimewithDate(vo.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(vo.getToDate(), true);
		TreeSet<DistrictVO> districts = getDistricts(jwtUser);
		districts.parallelStream().forEach(dist -> {
			List<String> officeCodes = getOfficeCodes().get(dist.getDistrictId());
			if (CollectionUtils.isNotEmpty(officeCodes)) {
				RegReportVO regReportVO = new RegReportVO();
				List<ReportVO> reportList = permitCountReport(fromDate, toDate, vo, officeCodes);
				List<ReportVO> ccpList = contractCarriageAggregation(fromDate, toDate, vo, officeCodes);
				Optional<ReportVO> ccpOptional = reportList.stream()
						.filter(check -> check.getPermitType().equals("CCP")).findFirst();
				if (ccpOptional.isPresent()) {
					ReportVO ccpreport = ccpOptional.get();
					ccpreport.setCcpPermit(ccpList);
				}
				regReportVO.setCovReport(reportList);
				regReportVO.setDistrictName(dist.getName());
				regReportVO.setDistrictId(dist.getId());
				regReport.add(regReportVO);
			}
		});
		regReport.stream().forEach(report -> {
			Double totalFee = (double) report.getCovReport().stream().map(v -> v.getCount()).mapToLong(Long::longValue)
					.sum();
			report.setTotal(totalFee);
		});

		if (regReport.stream().allMatch(val -> CollectionUtils.isEmpty(val.getCovReport()))) {
			throw new BadRequestException("No Data Found");
		}
		return regReport;
	}

	@Override
	public List<RegReportVO> distPermitReport(RegReportVO vo) {
		logger.info("district permit count for dist [{}]", vo.getDistrictName());
		List<RegReportVO> regReport = new ArrayList<>();
		LocalDateTime fromDate = getTimewithDate(vo.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(vo.getToDate(), true);

		Optional<DistrictDTO> districtOptional = districtDAO.findByDistrictName(vo.getDistrictName());
		if (districtOptional.isPresent()) {
			List<String> officeCodes = getOfficeCodes().get(districtOptional.get().getDistrictId());
			if (CollectionUtils.isNotEmpty(officeCodes)) {
				officeCodes.stream().forEach(office -> {
					RegReportVO regReportVO = new RegReportVO();
					List<ReportVO> reportList = permitCountReport(fromDate, toDate, vo, Arrays.asList(office));
					List<ReportVO> ccpList = contractCarriageAggregation(fromDate, toDate, vo, Arrays.asList(office));
					Optional<ReportVO> ccpOptional = reportList.stream()
							.filter(check -> check.getPermitType().equals("CCP")).findFirst();
					if (ccpOptional.isPresent()) {
						ReportVO ccpreport = ccpOptional.get();
						ccpreport.setCcpPermit(ccpList);
					}
					regReportVO.setOfficeName(getOfficeCodesByOfficeName().get(office));
					regReportVO.setOfficeCode(office);
					regReportVO.setCovReport(reportList);
					regReportVO.setDistrictName(vo.getDistrictName());
					regReportVO.setDistrictId(districtOptional.get().getDistrictId());
					regReport.add(regReportVO);
				});
			}
		}

		return regReport;
	}

	public List<RegReportVO> getPermitDetails(RegReportVO vo, Pageable pageble) {
		LocalDateTime fromDate = getTimewithDate(vo.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(vo.getToDate(), true);

		List<RegReportVO> regReport = new ArrayList<>();
		Page<PermitDetailsDTO> pagebleData = permitDetailsDAO
				.findByCreatedDateBetweenAndPermitTypeDescriptionAndRdtoOfficeDetailsOfficeCode(fromDate, toDate,
						vo.getPermitType(), vo.getOfficeCode(), pageble);

		if (!pagebleData.hasContent()) {
			throw new BadRequestException("No Records found for Permit " + vo.getPermitType());
		}

		List<PermitDetailsDTO> permitOptional = pagebleData.getContent();

		permitOptional.parallelStream().forEach(val -> {
			RegReportVO regReportVo = new RegReportVO();
			regReportVo.setCov(val.getRdto().getClassOfVehicleDesc());
			regReportVo.setPermitType(val.getPermitType().getDescription());
			regReportVo.setPrNo(val.getPrNo());
			if (val.getRdto() != null && val.getRdto().getOfficeDetails() != null
					&& val.getRdto().getOfficeDetails().getOfficeCode() != null) {
				regReportVo.setOfficeName(
						getOfficeCodesByOfficeName().get(val.getRdto().getOfficeDetails().getOfficeCode()));
			}
			regReportVo.setPermitNo(val.getPermitNo());
			regReportVo.setValidFrom(val.getPermitValidityDetails().getPermitValidFrom());
			regReportVo.setValidTo(val.getPermitValidityDetails().getPermitValidTo());
			regReport.add(regReportVo);

		});

		return regReport;

	}

	public TreeSet<DistrictVO> getDistricts(JwtUser jwtUser) {
		if (!CollectionUtils.isEmpty(districtsList)) {
			districtsList = districtService.findDistrictsByUser(jwtUser.getId());
		}

		return districtsList.stream().collect(
				Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(DistrictVO::getDistrictName))));

	}

	public List<ReportVO> permitCountReport(LocalDateTime fromDate, LocalDateTime toDate, RegReportVO vo,
			List<String> officeCodes) {
		Aggregation agg = newAggregation(
				match(Criteria.where("createdDate").gt(fromDate).lt(toDate).and("permitType.description").exists(true)
						.and("rdto.officeDetails.officeCode").in(officeCodes)),
				group("permitType.description").count().as("count"),
				project("count").and("permitType").previousOperation()

		);
		AggregationResults<ReportVO> groupResults = mongoTemplate.aggregate(agg, PermitDetailsDTO.class,
				ReportVO.class);
		List<ReportVO> result = groupResults.getMappedResults();
		return result;
	}

	public List<ReportVO> contractCarriageAggregation(LocalDateTime fromDate, LocalDateTime toDate, RegReportVO vo,
			List<String> officeCodes) {
		Aggregation agg = newAggregation(
				match(Criteria.where("createdDate").gt(fromDate).lt(toDate).and("permitType.description")
						.is("CONTRACT CARRIAGE PERMIT").and("rdto.classOfVehicle").in(Arrays.asList("TOVT", "COCT"))
						.and("rdto.officeDetails.officeCode").in(officeCodes)),
				group("routeDetails.routeType.routeCode").count().as("count"),
				project("count").and("permitType").previousOperation()

		);
		AggregationResults<ReportVO> groupResults = mongoTemplate.aggregate(agg, PermitDetailsDTO.class,
				ReportVO.class);
		List<ReportVO> result = groupResults.getMappedResults();
		return result;
	}

	@Override
	public Boolean paymentDetailsExcel(HttpServletResponse response, RegReportVO RegReportVO) {

		ExcelService excel = new ExcelServiceImpl();
		List<String> header = new ArrayList<String>();
		Random rand = new Random();
		int ranNo = rand.nextInt(1000);

		String name = "Payments_Reg_" + ranNo;
		String fileName = name + ".xlsx";
		String sheetName = "PaymentDetails";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		response.setHeader("Pragma", "public");
		response.setHeader("Cache-Control", "no-store");
		response.addHeader("Cache-Control", "max-age=0");

		XSSFWorkbook wb = null;

		if (StringUtils.isEmpty(RegReportVO.getDistrictName())) {
			List<RegReportVO> reportVO = getDistrictReports(RegReportVO);
			if (CollectionUtils.isEmpty(reportVO)) {
				return false;
			}
			excel.setHeaders(header, "districtReport");
			if (!reportVO.isEmpty()) {
				RegReportVO headerVo = reportVO.get(0);
				for (ReportVO value : headerVo.getFeeReport()) {
					header.add(value.getFeeType());
				}
			}
			/* header.addAll(ServiceCodeEnum.districtDefaults()); */
			wb = excel.renderData(prepareCellProps(header, reportVO, "districtWise"), header, fileName, sheetName);
		} else {
			List<PaymentTransactionDTO> reportsOpt = paymentTransactionDAO
					.findByResponseResponseTimeBetweenAndPaymentGatewayTypeInAndOfficeCodeInAndPayStatus(
							getTimewithDate(RegReportVO.getFromDate(), Boolean.FALSE),
							getTimewithDate(RegReportVO.getToDate(), Boolean.TRUE),
							getGatewayTypes(RegReportVO.getGateWayType()),
							getOfficeCodeList(RegReportVO.getDistrictName()), RegReportVO.getStatus());
			if (reportsOpt.isEmpty()) {
				return false;
			}
			List<RegReportVO> paymentreportList = getPaymentsReport(reportsOpt, RegReportVO);
			Collections.sort(paymentreportList, new Comparator<RegReportVO>() {
				@Override
				public int compare(RegReportVO p1, RegReportVO p2) {
					return p1.getTransactionDate().compareTo(p2.getTransactionDate());
				}
			});
			excel.setHeaders(header, "paymentReport");
			if (!paymentreportList.isEmpty()) {
				RegReportVO headerVo = paymentreportList.get(0);
				for (ReportVO value : headerVo.getFeeReport()) {
					header.add(value.getFeeType());
				}
			}
			/* header.addAll(ServiceCodeEnum.getFeeTypes()); */
			wb = excel.renderData(prepareCellProps(header, paymentreportList, "officeWise"), header, fileName,
					sheetName);
		}

		try {
			ServletOutputStream outputStream = null;
			outputStream = response.getOutputStream();
			wb.write(outputStream);
			outputStream.flush();
			outputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;

	}

	private List<List<CellProps>> prepareCellProps(List<String> header, List<RegReportVO> paymentList,
			String reportName) {

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();
		switch (reportName) {
		case "districtWise":
			cell = getDistrictWiseReport(paymentList);
			break;
		case "officeWise":
			cell = getOfficeWiseReport(paymentList);
			break;
		default:
			break;
		}
		return cell;

	}

	private List<List<CellProps>> getDistrictWiseReport(List<RegReportVO> paymentList) {
		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (RegReportVO payments : paymentList) {
			List<CellProps> result = new ArrayList<CellProps>();
			for (int i = 0; i < 3; i++) {
				CellProps cellpro = new CellProps();
				switch (i) {
				case 0:
					cellpro.setFieldValue(payments.getDistrictName());
					result.add(cellpro);
					break;
				case 1:
					for (ReportVO feeVO : payments.getFeeReport()) {
						CellProps internalCell = new CellProps();
						internalCell.setFieldValue(DateConverters.replaceDefaults(feeVO.getFee()));
						result.add(internalCell);
					}
					break;
				case 2:
					cellpro.setFieldValue(DateConverters.replaceDefaults(payments.getTotal()));
					result.add(cellpro);
					break;
				}

			}
			cell.add(result);
		}
		return cell;

	}

	private List<List<CellProps>> getOfficeWiseReport(List<RegReportVO> paymentList) {
		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (RegReportVO payments : paymentList) {
			List<CellProps> result = new ArrayList<CellProps>();
			for (int i = 0; i < 5; i++) {
				CellProps cellpro = new CellProps();
				switch (i) {
				case 0:
					cellpro.setFieldValue(
							DateConverters.convertCfstSyncLocalDateTimeFormat(payments.getTransactionDate()));
					result.add(cellpro);
					break;
				case 1:
					cellpro.setFieldValue(DateConverters.replaceDefaults(payments.getTransactionNo()));
					result.add(cellpro);
					break;
				case 2:
					cellpro.setFieldValue(payments.getGateWay());
					result.add(cellpro);
					break;
				case 3:
					cellpro.setFieldValue(DateConverters.replaceDefaults(payments.getOfficeCode()));
					result.add(cellpro);
					break;

				case 4:
					for (ReportVO feeVO : payments.getFeeReport()) {
						CellProps internalCell = new CellProps();
						internalCell.setFieldValue(DateConverters.replaceDefaults(feeVO.getFee()));
						result.add(internalCell);
					}
					break;
				}

			}
			cell.add(result);

		}
		return cell;

	}

	@Override
	public LocalDateTime getTimewithDate(LocalDate date, Boolean timeZone) {
		if (date == null) {
			throw new BadRequestException("Date input not available");
		}
		String dateVal = date + "T00:00:00.000Z";
		if (timeZone) {
			dateVal = date + "T23:59:59.999Z";
		}
		ZonedDateTime zdt = ZonedDateTime.parse(dateVal);
		return zdt.toLocalDateTime();
	}

	private List<String> getOfficeCodeList(String districtName) {

		Optional<DistrictDTO> districtOptional = districtDAO.findByDistrictName(districtName);
		if (!districtOptional.isPresent()) {
			throw new BadRequestException("District Not found " + districtName);
		}
		List<OfficeDTO> officeList = officeDAO.findBydistrict(districtOptional.get().getDistrictId());
		return officeList.stream().map(office -> office.getOfficeCode()).collect(Collectors.toList());

	}

	@Override
	public List<Integer> getGatewayTypes(Integer gateWayValue) {
		List<Integer> gateWay = new ArrayList<>();
		if (gateWayValue == 0) {
			gateWay.addAll(Arrays.asList(GatewayTypeEnum.CFMS.getId(), GatewayTypeEnum.PAYTM.getId(),
					GatewayTypeEnum.SBI.getId(), GatewayTypeEnum.PAYU.getId()));
		} else {
			gateWay.add(gateWayValue);
		}
		return gateWay;
	}

	@Override
	public Optional<ReportsRoleConfigDTO> getReportsConfig(String role) {
		Optional<ReportsRoleConfigDTO> reportOptional = reportsRoleConfigDAO.findByType("REPORTS");
		if (reportOptional.isPresent()) {
			return reportOptional;
		}
		return Optional.empty();
	}

	@Override
	public Boolean verfiyRoleAccess(String role, String reportType) {
		Boolean roleAcces = Boolean.FALSE;
		Optional<ReportsRoleConfigDTO> reportConfgOptional = getReportsConfig(role);
		if (reportConfgOptional.isPresent()) {
			ReportsRoleConfigDTO reportConfig = reportConfgOptional.get();
			Map<String, List<String>> reportAccessMap = reportConfig.getReportAccessBy();
			List<String> roles = reportAccessMap.get(reportType);
			if (CollectionUtils.isNotEmpty(roles) && roles.contains(role)) {
				roleAcces = Boolean.TRUE;
			}
		}
		return roleAcces;
	}

	@Override
	public void verifyUserAccess(JwtUser jwtuser, String reportName) {
		Optional<ReportsUserConfigDTO> reportsConfig = reportsUserConfigDAO.findByReportName(reportName);
		if (reportsConfig.isPresent()) {
			if (reportsConfig.get().isRestricted()) {

				if (!jwtuser.getPrimaryRole().getName().equals(CommonConstants.ADMIN)) {
					throw new BadRequestException("No Authorization for userId : [" + jwtuser.getId() + "]");
				}
			}
		} else {
			throw new BadRequestException("Report Configuration Not found");
		}
	}
	/*
	 * @Override public Boolean verifyUserAccess(JwtUser jwtuser, String reportType)
	 * { Boolean reportAccess = Boolean.FALSE; Optional<ReportsUserConfigDTO>
	 * userOptonal = reportsUserConfigDAO.findByRoleAndUserIdAndOfficeCode(
	 * jwtuser.getPrimaryRole().getName(), jwtuser.getId(),
	 * jwtuser.getOfficeCode()); if (userOptonal.isPresent()) { ReportsUserConfigDTO
	 * reportsUserConfigDTO = userOptonal.get(); List<String> reportsIn =
	 * reportsUserConfigDTO.getReportsInclude(); if
	 * (CollectionUtils.isNotEmpty(reportsIn) && reportsIn.contains(reportType)) {
	 * reportAccess = Boolean.TRUE; } if
	 * (CollectionUtils.isNotEmpty(reportsUserConfigDTO.getReportsExclude()) &&
	 * reportsUserConfigDTO.getReportsExclude().contains(reportType)) { throw new
	 * BadRequestException(MessageKeys.UNAUTHORIZED_USER + ":" + jwtuser.getId()); }
	 * }
	 * 
	 * if (reportAccess == Boolean.FALSE) { reportAccess =
	 * verfiyRoleAccess(jwtuser.getPrimaryRole().getName(), reportType); }
	 * 
	 * return reportAccess; }
	 */

	@Override
	public List<RegReportVO> suspensionStateCount(RegReportVO regReportVO, JwtUser jwtUser) {
		Map<Integer, List<String>> officeDistMapping = getOfficeCodes();

		List<DistrictVO> districtsList = districtService.findDistrictsByUser(jwtUser.getId());
		Set<Integer> distIds = districtsList.stream().map(distMap -> distMap.getDistrictId())
				.collect(Collectors.toSet());
		Set<DistrictVO> districtSet = new HashSet<>();
		distIds.stream().forEach(dist -> {
			DistrictVO dsitVO = districtsList.stream().filter(di -> di.getDistrictId() == dist).findFirst().get();
			districtSet.add(dsitVO);
		});
		List<RegReportVO> regReportList = new ArrayList<>();
		districtSet.stream().forEach(dist -> {
			List<String> officeCodes = officeDistMapping.get(dist.getDistrictId());
			if (CollectionUtils.isNotEmpty(officeCodes)) {
				LocalDateTime fromDate = getTimewithDate(regReportVO.getFromDate(), false);
				LocalDateTime toDate = getTimewithDate(regReportVO.getToDate(), true);
				RegReportVO reportVO = new RegReportVO();

				List<ReportVO> reportList = suspensionAggregation(fromDate, toDate, reportVO, officeCodes);
				reportVO.setDistrictName(dist.getDistrictName());
				reportVO.setDistrictId(dist.getDistrictId());
				// reportVO.setOfficeCode(officeCodes);
				reportVO.setCovReport(reportList);
				regReportList.add(reportVO);

			}
		});

		if (regReportList.stream().allMatch(val -> CollectionUtils.isEmpty(val.getCovReport()))) {
			throw new BadRequestException("No Data Found");
		}
		return regReportList;
	}

	@Override
	public List<RegReportVO> suspensionDistCount(RegReportVO regReportVO) {
		List<RegReportVO> regReport = new ArrayList<>();
		LocalDateTime fromDate = getTimewithDate(regReportVO.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(regReportVO.getToDate(), true);
		Optional<DistrictDTO> districtOptional = districtDAO.findByDistrictName(regReportVO.getDistrictName());
		if (districtOptional.isPresent()) {
			List<String> officeCodes = getOfficeCodes().get(districtOptional.get().getDistrictId());
			if (CollectionUtils.isNotEmpty(officeCodes)) {
				officeCodes.stream().forEach(office -> {
					RegReportVO reportVO = new RegReportVO();
					List<ReportVO> reportList = suspensionAggregation(fromDate, toDate, regReportVO,
							Arrays.asList(office));
					reportVO.setDistrictName(districtOptional.get().getDistrictName());
					reportVO.setDistrictId(districtOptional.get().getDistrictId());
					reportVO.setOfficeCode(office);
					reportVO.setOfficeName(getOfficeCodesByOfficeName().get(office));
					if (CollectionUtils.isNotEmpty(reportList)) {
						reportVO.setCovReport(reportList);
					} else {
						reportVO.setCovReport(setZeroCountToSuspencionReport());
					}
					regReport.add(reportVO);
				});
			}
		}
		return regReport;
	}

	private List<ReportVO> setZeroCountToSuspencionReport() {
		List<ReportVO> list = new ArrayList<ReportVO>();

		ReportVO vo = new ReportVO();
		ReportVO vo1 = new ReportVO();
		vo.setActionStatus(Status.RCActionStatus.REVOKED.getStatus());
		vo.setCount(0l);

		vo1.setActionStatus(Status.RCActionStatus.SUSPEND.getStatus());
		vo1.setCount(0l);

		list.add(vo);
		list.add(vo1);
		return list;
	}

	public List<ReportVO> suspensionAggregation(LocalDateTime fromDate, LocalDateTime toDate, RegReportVO vo,
			List<String> officeCodes) {
		Aggregation agg = newAggregation(
				match(Criteria.where("rcActionsDetails.fromDate").gt(fromDate).lt(toDate).and("officeCode")
						.in(officeCodes).and("actionStatus")
						.in(Arrays.asList(Status.RCActionStatus.SUSPEND, Status.RCActionStatus.REVOKED))),
				group("actionStatus").count().as("count"), project("count").and("actionStatus").previousOperation()

		);
		AggregationResults<ReportVO> groupResults = mongoTemplate.aggregate(agg, RCActionsDTO.class, ReportVO.class);
		List<ReportVO> result = groupResults.getMappedResults();
		return result;
	}

	@Override
	public List<RegReportVO> findAllDetails(RegReportVO regReportVO, Pageable page) {
		// RegReportVO regReports = new RegReportVO();
		LocalDateTime fromDate = getTimewithDate(regReportVO.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(regReportVO.getToDate(), true);
		Page<RCActionsDTO> data = suspensionDAO.findByRcActionsDetailsFromDateBetweenAndOfficeCodeAndActionStatusIn(
				regReportVO.getFromDate(), regReportVO.getToDate().plusDays(1), regReportVO.getOfficeCode(),
				getReportStatus(), page);
		List<RCActionsDTO> dlDTO = data.getContent();
		if (dlDTO.isEmpty()) {
			throw new BadRequestException("No records found");
		}

		List<RCActionsVO> RCActionsVOList = suspensionMapper.convertLimited(dlDTO);
		regReportVO.setRcReport(RCActionsVOList);

		return Arrays.asList(regReportVO);

	}

	@Override
	public Optional<List<CCOReportVO>> getCCOApprovedRejectCount(String officeCode, ReportInputVO inputVO) {
		List<CCOReportVO> reportVO = new ArrayList<>();
		LocalDateTime fromDate = getTimewithDate(inputVO.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(inputVO.getToDate(), true);

		List<MasterUsersDTO> masterUsersList = masterUsersDAO
				.findByOfficeOfficeCodeAndPrimaryRoleNameOrOfficeOfficeCodeAndAdditionalRoles(officeCode,
						inputVO.getRole(), officeCode, inputVO.getRole());

		for (MasterUsersDTO masterUsersDTO : masterUsersList) {
			CCOReportVO ccoReportVO = new CCOReportVO();
			String actionBy = masterUsersDTO.getUserId();
			if (masterUsersDTO.getFirstName() != null || masterUsersDTO.getLastName() != null)

			{
				String ccoName = masterUsersDTO.getFirstName() + " " + masterUsersDTO.getLastName();
				ccoReportVO.setName(ccoName);
			}

			/**
			 * approved & Rejected counts
			 */
			Integer approvedCount = regServiceDAO
					.countByOfficeDetailsOfficeCodeAndActionDetailsUserIdAndActionDetailsStatusAndActionDetailsLUpdateTimeBetween(
							officeCode, actionBy, "Approved", fromDate, toDate);
			Integer rejectCount = regServiceDAO
					.countByOfficeDetailsOfficeCodeAndActionDetailsUserIdAndActionDetailsStatusAndActionDetailsLUpdateTimeBetween(
							officeCode, actionBy, "Rejected", fromDate, toDate);

			// Integer approvedCount = approvedList.size();
			ccoReportVO.setApproved(approvedCount);

			ccoReportVO.setRejected(rejectCount);

			ccoReportVO.setTotalcount(approvedCount + rejectCount);

			reportVO.add(ccoReportVO);
		}
		return Optional.of(reportVO);
	}

	@Override
	public List<DealerReportVO> getDealerReport(JwtUser jwtUser, DealerReportVO dealerVO, Pageable page) {
		List<DealerReportVO> dealerReportVOList = new ArrayList<>();
		LocalDateTime fromDate = getTimewithDate(dealerVO.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(dealerVO.getToDate(), true);
		List<RegistrationDetailsDTO> registrationsList = new ArrayList<RegistrationDetailsDTO>();
		List<StagingRegistrationDetailsDTO> stagingList = new ArrayList<StagingRegistrationDetailsDTO>();

		registrationsList = registrationDetailDAO.findByDealerDetailsDealerIdAndTrGeneratedDateBetween(jwtUser.getId(),
				fromDate, toDate, page);

		stagingList = stagingRegistrationDetailsDAO
				.findByDealerDetailsDealerIdAndApplicationStatusNotInAndTrNoNotNullAndTrGeneratedDateBetween(
						jwtUser.getId(), getStatus(), fromDate, toDate, page);

		if (!stagingList.isEmpty()) {
			registrationsList.addAll(stagingList);
		}

		registrationsList.stream().forEach(sl -> {
			DealerReportVO dealerReportVO = new DealerReportVO();
			if (sl.getApplicantDetails() != null && StringUtils.isNotBlank(sl.getApplicantDetails().getDisplayName())) {
				dealerReportVO.setApplicantName(sl.getApplicantDetails().getDisplayName());
			}
			if (StringUtils.isNotBlank(sl.getTrNo())) {
				dealerReportVO.setTrNo(sl.getTrNo());
			}
			if (StringUtils.isNotBlank(sl.getPrNo())
					&& (sl.getPrGeneratedDate() != null || (sl.getRegistrationValidity() != null
							&& sl.getRegistrationValidity().getPrGeneratedDate() != null))) {
				dealerReportVO.setPrNo(sl.getPrNo());
			}
			if (sl.getVahanDetails() != null && StringUtils.isNotBlank(sl.getVahanDetails().getChassisNumber())) {
				dealerReportVO.setChassisNumber(sl.getVahanDetails().getChassisNumber());
			}
			if (sl.getVahanDetails() != null && StringUtils.isNotBlank(sl.getVahanDetails().getEngineNumber())) {
				dealerReportVO.setEngineNumber(sl.getVahanDetails().getEngineNumber());
			}
			if (sl.getVahanDetails() != null && StringUtils.isNotBlank(sl.getVahanDetails().getMakersDesc())) {
				dealerReportVO.setMakersModel(sl.getVahanDetails().getMakersDesc());
			}
			if (StringUtils.isNotBlank(sl.getClassOfVehicleDesc())) {
				dealerReportVO.setClassOfVehicle(sl.getClassOfVehicleDesc());
			}
			if (sl.getFinanceDetails() != null && StringUtils.isNotBlank(sl.getFinanceDetails().getFinancerName())) {
				dealerReportVO.setHypothecatedBy(sl.getFinanceDetails().getFinancerName());
			}
			if (sl.getInvoiceDetails() != null && sl.getInvoiceDetails().getInvoiceValue() != null) {
				dealerReportVO.setInvoiceAmt(sl.getInvoiceDetails().getInvoiceValue());
			}

			if (StringUtils.isNotBlank(sl.getTaxType())) {
				dealerReportVO.setTaxType(sl.getTaxType());
			}
			dealerReportVO.setTaxAmt(sl.getTaxAmount());

			dealerReportVO.setTrGeneratedDate(sl.getTrGeneratedDate());

			dealerReportVOList.add(dealerReportVO);
		});
		registrationsList.clear();
		stagingList.clear();
		return dealerReportVOList;
	}

	public List<String> getStatus() {
		List<String> status = new ArrayList<>();
		status.add(StatusRegistration.PAYMENTFAILED.getDescription());
		status.add(StatusRegistration.INITIATED.getDescription());
		status.add(StatusRegistration.PAYMENTSUCCESS.getDescription());
		return status;

	}

	public List<String> getReportStatus() {
		List<String> status = new ArrayList<>();
		status.add(RCActionStatus.SUSPEND.getDescription());
		status.add(RCActionStatus.REVOKED.getDescription());
		return status;
	}

	@Override
	public void districtvalidate(JwtUser jwtuser, RegReportVO regVO) {
		Optional<OfficeDTO> distId = officeDAO.findByOfficeCode(jwtuser.getOfficeCode());

		List<DistrictDTO> districtDTO = districtDAO.findByDistrictId(distId.get().getDistrict());

		Optional<DistrictDTO> districtDetails = Optional.of(districtDTO.get(0));

		if (!districtDetails.isPresent()) {
			throw new BadRequestException("NO dist found");

		}
		if (StringUtils.isNotBlank(regVO.getDistrictName())
				&& !regVO.getDistrictName().equals(districtDetails.get().getDistrictName())
				&& !CommonConstants.ADMIN.equals(jwtuser.getPrimaryRole().getName())) {
			throw new BadRequestException("This user is not authorized to view the report for selected district");
		}

	}

	@Override
	public void districtvalidate(JwtUser jwtUser, VcrVo reportVO) {

	}

	@Override
	public VcrVo covEnforcementReport(String user, String officeCode, VcrVo vcrVO) {
		OfficeDTO officeDTO = getDistrictByofficeCode(officeCode);
		LocalDateTime fromDate = getTimewithDate(vcrVO.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(vcrVO.getToDate(), true);
		List<EnforcementVcrVO> enforcementList = new ArrayList<>();
		if (StringUtils.isEmpty(vcrVO.getType())) {
			List<String> officeCodesList;
			if (vcrVO.getOfficeCode().equals((DispatchEnum.FETCH_ALL_DETAILS.getReqType()))) {
				officeCodesList = getOfficeCodes().get(officeDTO.getDistrict());
			} else {
				officeCodesList = Arrays.asList(vcrVO.getOfficeCode());
			}
			if (CollectionUtils.isEmpty(officeCodesList)) {
				throw new BadRequestException("No office found for District " + officeDTO.getDistrict());
			}
			logger.info("ENforcement report for [{}]" + officeCodesList);
			List<VcrFinalServiceDTO> vcrList = finalServiceDAO.findByVcrDateOfCheckBetweenAndOfficeCodeIn(fromDate,
					toDate, officeCodesList);
			final Map<String, List<VcrFinalServiceDTO>> officeWiseVcrList = vcrList.stream()
					.collect(Collectors.groupingBy(VcrFinalServiceDTO::getOfficeCode));
			// List<EnforcementVcrVO> enforcementList = new ArrayList<>();
			officeCodesList.stream().forEach(office -> {
				String officeName = getOfficeCodesByOfficeName().get(office);
				List<VcrFinalServiceDTO> officeVcrDataList = officeWiseVcrList.get(office);
				if (CollectionUtils.isNotEmpty(officeVcrDataList)) {
					EnforcementVcrVO enforcementVO = setEnforcementData(officeVcrDataList, officeName, officeCode,
							null);
					enforcementList.add(enforcementVO);
				}
			});
			vcrVO.setEnforcementReport(enforcementList);
			return vcrVO;
		} else if (StringUtils.isNotEmpty(vcrVO.getOfficeCode()) && StringUtils.isNotEmpty(vcrVO.getType())
				&& vcrVO.getType().equalsIgnoreCase("officeWise") && StringUtils.isEmpty(vcrVO.getClassOfVehicle())) {
			logger.info("ENforcement report for selected office [{}]" + vcrVO.getOfficeCode());

			String officeName = getOfficeCodesByOfficeName().get(vcrVO.getOfficeCode());
			List<EnforcementVcrVO> enforcementCovList = enforcementCovWise(vcrVO.getOfficeCode(), officeName, fromDate,
					toDate);
			vcrVO.setEnforcementReport(enforcementCovList);
			return vcrVO;
		}

		else if (StringUtils.isNotEmpty(vcrVO.getOfficeCode()) && StringUtils.isNotEmpty(vcrVO.getType())
				&& vcrVO.getType().equalsIgnoreCase("covWise") && StringUtils.isNotEmpty(vcrVO.getClassOfVehicle())) {
			logger.info("ENforcement report for selected cov [{}]" + vcrVO.getOfficeCode());

			List<EnforcementVcrVO> enforcementDetailsList = covEnforcementDetails(vcrVO.getOfficeCode(),
					vcrVO.getClassOfVehicle(), fromDate, toDate);
			vcrVO.setEnforcementReport(enforcementDetailsList);
		}
		return vcrVO;

	}

	public List<EnforcementVcrVO> enforcementCovWise(String officeCode, String officeName, LocalDateTime fromDate,
			LocalDateTime toDate) {
		List<EnforcementVcrVO> enforcementList = new ArrayList<>();
		List<VcrFinalServiceDTO> vcrList = finalServiceDAO.nativeVcrDateAndOfficeCodeIn(Arrays.asList(officeCode),
				fromDate, toDate);
		final Map<String, List<VcrFinalServiceDTO>> officeWiseVcrList = vcrList.stream()
				.collect(Collectors.groupingBy(val -> val.getRegistration().getClasssOfVehicle().getCovdescription()));

		officeWiseVcrList.keySet().stream().forEach(key -> {
			List<VcrFinalServiceDTO> covVcrList = officeWiseVcrList.get(key);
			EnforcementVcrVO enforcementVO = setEnforcementData(covVcrList, officeName, officeCode, key);
			enforcementList.add(enforcementVO);
		});
		return enforcementList;

	}

	public List<EnforcementVcrVO> covEnforcementDetails(String officeCode, String cov, LocalDateTime fromDate,
			LocalDateTime toDate) {
		List<VcrFinalServiceDTO> vcrList = finalServiceDAO.nativeDateofCheckAndCovDesc(fromDate, toDate,
				Arrays.asList(officeCode), cov);
		List<EnforcementVcrVO> enforcementList = vcrFinalServiceMapper.vcrEnforcementMapper(vcrList);
		return enforcementList;

	}

	public EnforcementVcrVO setEnforcementData(List<VcrFinalServiceDTO> officeVcrDataList, String officeName,
			String officeCode, String classOfVehicle) {
		EnforcementVcrVO enforcementVO = new EnforcementVcrVO();
		Map<Boolean, Long> vcrTypeMap = officeVcrDataList.stream()
				.collect(Collectors.groupingBy(VcrFinalServiceDTO::getIsVcrClosed, Collectors.counting()));
		long vcrClosed = 0;
		long open = 0;
		if (vcrTypeMap.get(true) != null) {
			vcrClosed = vcrTypeMap.get(true);
		}
		if (vcrTypeMap.get(false) != null) {
			open = vcrTypeMap.get(false);
		}
		int offenceTotal = officeVcrDataList.stream().filter(vcr -> vcr.getOffencetotal() != null)
				.map(VcrFinalServiceDTO::getOffencetotal).mapToInt(Integer::intValue).sum();

		double taxAmount = officeVcrDataList.stream().filter(vcr -> vcr.getTax() != null)
				.map(VcrFinalServiceDTO::getTax).mapToDouble(Double::doubleValue).sum();

		double penality = officeVcrDataList.stream().filter(vcr -> vcr.getPenalty() != null)
				.map(VcrFinalServiceDTO::getPenalty).mapToLong(Long::longValue).sum();
		double total = offenceTotal + taxAmount + penality;
		enforcementVO.setOfficeName(officeName);
		enforcementVO.setOfficeCode(officeCode);
		enforcementVO.setVcrsClosed(vcrClosed);
		enforcementVO.setVcrsOpened(open);
		enforcementVO.setClassOfVehicle(classOfVehicle);
		enforcementVO.setCfAmount(offenceTotal);
		enforcementVO.setTaxAmount(taxAmount);
		enforcementVO.setPenality(penality);
		enforcementVO.setTotal(total);
		enforcementVO.setTotalVcrs(enforcementVO.getVcrsClosed() + enforcementVO.getVcrsOpened());
		return enforcementVO;
	}

	@Override
	public List<FinanceDetailsVO> getFinancierReport(JwtUser jwtUser, FinanceDetailsVO financeDetailsVO) {
		List<FinanceDetailsVO> voList = new ArrayList<>();
		LocalDateTime fromDate = getTimewithDate(financeDetailsVO.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(financeDetailsVO.getToDate(), true);
		List<StagingRegistrationDetailsDTO> stagingList = new ArrayList<StagingRegistrationDetailsDTO>();
		List<RegistrationDetailsDTO> registrationsList = new ArrayList<RegistrationDetailsDTO>();

		List<String> users = new ArrayList<>();
		users.add(jwtUser.getId());

		Optional<UserDTO> user = userDAO.findByUserId(jwtUser.getId());

		if (user.isPresent() && user.get().isParent()) {
			List<UserDTO> chaildUsersList = userDAO.findByParentId(jwtUser.getId());
			if (CollectionUtils.isNotEmpty(chaildUsersList)) {
				users.addAll(chaildUsersList.stream().map(val -> val.getUserId()).collect(Collectors.toList()));
			}
		}

		stagingList = stagingRegistrationDetailsDAO
				.findByFinanceDetailsUserIdInAndFinanceDetailsAgreementDateBetween(users, fromDate, toDate);

		registrationsList = registrationDetailDAO
				.findByFinanceDetailsUserIdInAndFinanceDetailsAgreementDateBetween(users, fromDate, toDate);

		if (!stagingList.isEmpty()) {
			registrationsList.addAll(stagingList);
		}

		registrationsList.stream().forEach(val -> {
			FinanceDetailsVO vo = new FinanceDetailsVO();
			if (StringUtils.isNotBlank(val.getTrNo())) {
				vo.setTrNo(val.getTrNo());
			}
			if (StringUtils.isNotBlank(val.getPrNo())
					&& (val.getPrGeneratedDate() != null || (val.getRegistrationValidity() != null
							&& val.getRegistrationValidity().getPrGeneratedDate() != null))) {
				vo.setPrNo(val.getPrNo());
			}

			if (StringUtils.isNotBlank(val.getApplicationNo())) {
				vo.setApplicationNo(val.getApplicationNo());
			}

			if (val.getFinanceDetails() != null && val.getFinanceDetails().getUserId() != null) {
				vo.setUserId(val.getFinanceDetails().getUserId());
			}

			if (val.getFinanceDetails() != null && val.getFinanceDetails().getAgreementDate() != null) {
				vo.setAgreementDate(val.getFinanceDetails().getAgreementDate());
			}

			if (val.getFinanceDetails() != null && val.getFinanceDetails().getSanctionedAmount() != null) {
				vo.setSanctionedAmount(val.getFinanceDetails().getSanctionedAmount());
			}

			if (val.getFinanceDetails() != null && val.getFinanceDetails().getQuotationValue() != null) {
				vo.setQuotationValue(val.getFinanceDetails().getQuotationValue());
			}
			voList.add(vo);

		});
		registrationsList.clear();
		stagingList.clear();
		return voList;

	}

	@Override
	public List<RegReportVO> getData(String userId, String officeCode, RegReportVO regVO, Pageable page) {
		// LocalDateTime fromDate = getTimewithDate(regVO.getFromDate(), false);
		// LocalDateTime toDate = getTimewithDate(regVO.getToDate(), true);
		Page<RCActionsDTO> data = suspensionDAO
				.findByRcActionsDetailsFromDateBetweenAndOfficeCodeAndRcActionsDetailsActionByAndActionStatusIn(
						regVO.getFromDate(), regVO.getToDate().plusDays(1), officeCode, userId, getReportStatus(),
						page);
		List<RCActionsDTO> dlDTO = data.getContent();
		if (dlDTO.isEmpty()) {
			throw new BadRequestException("No records found");
		}

		List<RCActionsVO> RCActionsVOList = suspensionMapper.convertLimited(dlDTO);
		regVO.setRcReport(RCActionsVOList);

		return Arrays.asList(regVO);

	}

	@Override
	public List<RegReportVO> getPermitData(String officeCode, RegReportVO regVO) {
		LocalDateTime fromDate = getTimewithDate(regVO.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(regVO.getToDate(), true);
		List<RegReportVO> regReport = new ArrayList<>();
		if (StringUtils.isNotBlank(officeCode)) {
			RegReportVO regReportVO = new RegReportVO();
			List<ReportVO> reportList = permitCountReport(fromDate, toDate, regVO, Arrays.asList(officeCode));
			List<ReportVO> ccpList = contractCarriageAggregation(fromDate, toDate, regVO, Arrays.asList(officeCode));
			Optional<ReportVO> ccpOptional = reportList.stream().filter(check -> check.getPermitType().equals("CCP"))
					.findFirst();
			if (ccpOptional.isPresent()) {
				ReportVO ccpreport = ccpOptional.get();
				ccpreport.setCcpPermit(ccpList);
			}
			regReportVO.setOfficeName(getOfficeCodesByOfficeName().get(officeCode));
			regReportVO.setOfficeCode(officeCode);
			regReportVO.setCovReport(reportList);
			if (regReportVO.getCovReport().isEmpty()) {
				throw new BadRequestException("no data found");
			}
			// regReportVO.setDistrictName(vo.getDistrictName());
			// regReportVO.setDistrictId(districtOptional.get().getDistrictId());
			regReport.add(regReportVO);

		}

		return regReport;
	}

	@Override
	public List<DistrictVO> getDistByOfc(String officeCode) {
		Optional<OfficeDTO> officeOptional = officeDAO.findByOfficeCode(officeCode);
		if (!officeOptional.isPresent()) {
			throw new BadRequestException("No District found with officeCode :" + officeCode);
		}
		List<DistrictDTO> districtDTO = districtDAO.findByDistrictId(officeOptional.get().getDistrict());

		if (CollectionUtils.isEmpty(districtDTO)) {
			throw new BadRequestException("No dist found");

		}
		List<DistrictVO> dist = districtMapper.convertEntity(districtDTO);
		return dist;
	}

	@Override
	public List<String> getMviForDist(Integer districtId) {
		List<String> officeCodes = getOfficeCodes().get(districtId);
		if (CollectionUtils.isNotEmpty(officeCodes)) {
			DBObject office = new BasicDBObject("officeCode", new BasicDBObject("$in", officeCodes));
			List<String> distinctMVI = mongoTemplate.getCollection("table_vcr_details").distinct("createdBy", office);

			return distinctMVI;
		}
		return Collections.emptyList();
	}

	@Override
	public List<ActionCountDetailsVO> getEodReportCount(JwtUser jwtUser, EODReportVO eodVO) {
		long approvedCount = 0L;
		long rejectedCount = 0L;
		LocalDateTime fromDate = getTimewithDate(eodVO.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(eodVO.getToDate(), true);
		List<ActionCountDetailsVO> actionCountDetailsVOList = new ArrayList<ActionCountDetailsVO>();
		List<String> approvedStatusList = Arrays.asList(StatusRegistration.APPROVED.getDescription(),
				StatusRegistration.MVIAPPROVED.getDescription(), StatusRegistration.RTOAPPROVED.getDescription(),
				StatusRegistration.AOAPPROVED.getDescription());
		List<String> rejectedStatusList = Arrays.asList(StatusRegistration.REJECTED.getDescription(),
				StatusRegistration.MVIREJECTED.getDescription(), StatusRegistration.RTOREJECTED.getDescription(),
				StatusRegistration.AOREJECTED.getDescription());

		List<ServiceEnum> serviceList = Arrays.asList(ServiceEnum.RCFORFINANCE, ServiceEnum.CHANGEOFADDRESS,
				ServiceEnum.ALTERATIONOFVEHICLE, ServiceEnum.DUPLICATE, ServiceEnum.RENEWAL, ServiceEnum.HPA,
				ServiceEnum.TRANSFEROFOWNERSHIP, ServiceEnum.HIREPURCHASETERMINATION, ServiceEnum.REASSIGNMENT,
				ServiceEnum.DATAENTRY, ServiceEnum.CANCELLATIONOFNOC, ServiceEnum.ISSUEOFNOC,
				ServiceEnum.VEHICLESTOPPAGE, ServiceEnum.VEHICLESTOPPAGEREVOKATION, ServiceEnum.THEFTINTIMATION,
				ServiceEnum.THEFTREVOCATION);

		if (Arrays.asList(RoleEnum.CCO.getName(), RoleEnum.MVI.getName(), RoleEnum.AO.getName())
				.contains(eodVO.getSelectedRole())) {

			for (ServiceEnum service : serviceList) {
				ActionCountDetailsVO actionCountDetailsVO = new ActionCountDetailsVO();
				rejectedCount = countCriteria(jwtUser, fromDate, toDate, service, rejectedStatusList, eodVO);
				approvedCount = countCriteria(jwtUser, fromDate, toDate, service, approvedStatusList, eodVO);
				actionCountDetailsVO.setServiceType(service.toString());
				actionCountDetailsVO.setServiceDesc(service.getDesc());
				actionCountDetailsVO.setApprovedCount(approvedCount);
				actionCountDetailsVO.setRejectedCount(rejectedCount);
				actionCountDetailsVO.setTotalCount(approvedCount + rejectedCount);
				actionCountDetailsVOList.add(actionCountDetailsVO);
			}

		} else if (eodVO.getSelectedRole().equals(RoleEnum.RTO.getName())) {

			if (StringUtils.isEmpty(eodVO.getUserId())) {

				List<UserDTO> masterUsersList = userDAO.findByOfficeOfficeCode(jwtUser.getOfficeCode());
				for (UserDTO userDTO : masterUsersList) {
					List<String> roles = userDTO.getAdditionalRoles().stream().map(val -> val.getName())
							.collect(Collectors.toList());
					roles.add(userDTO.getPrimaryRole().getName());

					for (String role : roles) {
						if (Arrays.asList(RoleEnum.CCO.getName(), RoleEnum.MVI.getName(), RoleEnum.AO.getName(),
								RoleEnum.RTO.getName()).contains(role)) {
							ActionCountDetailsVO actionCountDetailsVO = new ActionCountDetailsVO();
							approvedCount = countCriteriaRTOAll(jwtUser, fromDate, toDate, approvedStatusList, userDTO,
									role);
							rejectedCount = countCriteriaRTOAll(jwtUser, fromDate, toDate, rejectedStatusList, userDTO,
									role);
							actionCountDetailsVO.setUserName(userDTO.getFirstname());
							actionCountDetailsVO.setUserID(userDTO.getUserId());
							actionCountDetailsVO.setRole(role);
							actionCountDetailsVO.setApprovedCount(approvedCount);
							actionCountDetailsVO.setRejectedCount(rejectedCount);
							actionCountDetailsVO.setTotalCount(approvedCount + rejectedCount);
							actionCountDetailsVOList.add(actionCountDetailsVO);
						}
					}
				}

			}

			else if (StringUtils.isNotEmpty(eodVO.getUserId())) {

				for (ServiceEnum service : serviceList) {
					ActionCountDetailsVO actionCountDetailsVO = new ActionCountDetailsVO();
					approvedCount = countCriteria(jwtUser, fromDate, toDate, service, approvedStatusList, eodVO);
					rejectedCount = countCriteria(jwtUser, fromDate, toDate, service, rejectedStatusList, eodVO);
					actionCountDetailsVO.setServiceType(service.toString());
					actionCountDetailsVO.setServiceDesc(service.getDesc());
					actionCountDetailsVO.setApprovedCount(approvedCount);
					actionCountDetailsVO.setRejectedCount(rejectedCount);
					actionCountDetailsVO.setTotalCount(approvedCount + rejectedCount);
					actionCountDetailsVOList.add(actionCountDetailsVO);
				}

			}

		}
		return actionCountDetailsVOList;

	}

	private long countCriteriaRTOAll(JwtUser jwtUser, LocalDateTime fromDate, LocalDateTime toDate, List<String> status,
			UserDTO userDTO, String role) {
		Criteria criteria1 = Criteria.where("officeCode").is(jwtUser.getOfficeCode());
		Criteria criteria2 = Criteria.where("actionDetails").elemMatch(Criteria.where("status").in(status).and("role")
				.is(role).and("userId").is(userDTO.getUserId()).and("lUpdate").gte(fromDate).lte(toDate));
		Query query = new Query();
		query.addCriteria(new Criteria().andOperator(criteria1, criteria2));
		long count = mongoTemplate.count(query, RegServiceDTO.class);

		return count;
	}

	private long countCriteria(JwtUser jwtUser, LocalDateTime fromDate, LocalDateTime toDate, ServiceEnum service,
			List<String> status, EODReportVO eodVO) {
		Criteria criteria1 = Criteria.where("officeCode").is(jwtUser.getOfficeCode());
		Criteria criteria2 = Criteria.where("serviceType").in(service);
		Criteria criteria3 = Criteria.where("actionDetails").elemMatch(Criteria.where("status").in(status).and("userId")
				.is(eodVO.getUserId()).and("role").is(eodVO.getRole()).and("lUpdate").gte(fromDate).lte(toDate));

		Query query = new Query();
		query.addCriteria(new Criteria().andOperator(criteria1, criteria2, criteria3));
		long count = mongoTemplate.count(query, RegServiceDTO.class);
		return count;
	}

	@Override
	public PageDataVo getEodReportList(JwtUser jwtUser, EODReportVO eodVO, Pageable pagable)
			throws BadRequestException {

		LocalDateTime fromDate = getTimewithDate(eodVO.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(eodVO.getToDate(), true);
		List<EODReportVO> stagDetls = new ArrayList<>();
		Criteria criteria1 = Criteria.where("officeCode").is(jwtUser.getOfficeCode());
		Criteria criteria2 = Criteria.where("serviceType").in(eodVO.getServiceName());
		Criteria criteria3 = Criteria.where("actionDetails")
				.elemMatch(Criteria.where("status").in(eodVO.getStatusList()).and("userId").is(eodVO.getUserId())
						.and("role").is(eodVO.getRole()).and("lUpdate").gte(fromDate).lte(toDate));

		Query query = new Query();
		query.addCriteria(new Criteria().andOperator(criteria1, criteria2, criteria3));
		long count = mongoTemplate.count(query, RegServiceDTO.class);
		query.limit(pagable.getPageSize());

		query.skip(pagable.getPageSize() * (pagable.getPageNumber() - 1));

		List<RegServiceDTO> registrationsList = mongoTemplate.find(query, RegServiceDTO.class);

		if (!registrationsList.isEmpty()) {
			registrationsList.forEach(source -> {
				Optional<ActionDetails> actionDetailsOpt = source
						.getActionDetails().stream().filter(a -> eodVO.getUserId().equals(a.getUserId())
								&& a.getRole().equals(eodVO.getRole()) && eodVO.getStatusList().contains(a.getStatus()))
						.findFirst();
				if (actionDetailsOpt.isPresent()) {
					ActionDetails actionDetails = actionDetailsOpt.get();
					EODReportVO vo = new EODReportVO();
					vo.setApplicationNo(source.getApplicationNo());
					vo.setPrNo(source.getPrNo());
					vo.setTrNo(source.getTrNo());
					vo.setCreatedDate(source.getCreatedDate());
					vo.setServiceTypes(source.getServiceType());
					vo.setStatus(actionDetails.getStatus());
					vo.setlUpdate(actionDetails.getlUpdate());
					vo.setUserId(actionDetails.getUserId());
					stagDetls.add(vo);
				}

			});
		} else {
			throw new BadRequestException("No Records Found");
		}
		PageDataVo pageDataVo = new PageDataVo();
		pageDataVo.setContent(stagDetls);
		pageDataVo.setPageNo(pagable.getPageNumber());
		pageDataVo.setSize(pagable.getPageSize());
		pageDataVo.setTotalElement(count);
		pageDataVo.setTotalPage(
				registrationsList.size() == 0 ? 1 : (int) Math.ceil((double) count / (double) pagable.getPageSize()));

		return pageDataVo;

	}

	@Override
	public RegServiceVO getAllData(String applicationNo) {
		RegServiceVO regServiceVO = new RegServiceVO();

		Optional<RegServiceDTO> registrationDetailsDTO = regServiceDAO.findByApplicationNo(applicationNo);

		if (registrationDetailsDTO.isPresent()) {

			regServiceVO = regServiceMapper.convertEntity(registrationDetailsDTO.get());
		}
		return regServiceVO;

	}

	@Override
	public ActionCountDetailsVO eodReportForDept(JwtUser jwtUser, EODReportVO eodVO, Pageable pagable) {

		if (StringUtils.isEmpty(eodVO.getSelectedRole()) || StringUtils.isEmpty(eodVO.getName())) {
			logger.debug("Input values not available");
			throw new BadRequestException("Input values not available");
		}
		if (Arrays.asList(RoleEnum.CCO.getName(), RoleEnum.MVI.getName(), RoleEnum.AO.getName())
				.contains(eodVO.getSelectedRole())) {

			return getAllRecordsBasedOnStatus(eodVO.getName(), jwtUser, eodVO, pagable);
		}
		if (RoleEnum.RTO.getName().contains(eodVO.getSelectedRole())
				|| RoleEnum.DTC.getName().contains(eodVO.getSelectedRole())) {
			return getAllRecordsBasedOnStatus(eodVO.getName(), jwtUser, eodVO, pagable);
		}
		if (RoleEnum.STA.getName().contains(eodVO.getSelectedRole())) {
			if (eodVO.getDistrictId() != null || StringUtils.isNotBlank(eodVO.getUserName())) {

				return getAllRecordsBasedOnStatus(eodVO.getName(), jwtUser, eodVO, pagable);
			}
			return getDistrictWiseEodCount(getTimewithDate(eodVO.getFromDate(), false),
					getTimewithDate(eodVO.getToDate(), true));
		}
		throw new BadRequestException("Unauthorized role");
	}

	private List<String> getStatusListBasedOnRole(String role, boolean status) {

		switch (role) {
		case "CCO":
			if (status) {
				return Arrays.asList("APPROVED");
			}
			return Arrays.asList("REJECTED");

		case "MVI":
			if (status) {
				return Arrays.asList("APPROVED", "MVIAPPROVED");
			}
			return Arrays.asList("REJECTED", "MVIREJECTED");

		case "AO":
			if (status) {
				return Arrays.asList("APPROVED", "AOAPPROVED");
			}
			return Arrays.asList("REJECTED", "AOREJECTED");

		case "RTO":
			if (status) {
				return Arrays.asList("APPROVED", "RTOAPPROVED");
			}
			return Arrays.asList("REJECTED", "RTOREJECTED");
		case "ALL":
			if (status) {
				return Arrays.asList("APPROVED", "RTOAPPROVED", "MVIAPPROVED", "AOAPPROVED");
			}
			return Arrays.asList("REJECTED", "RTOREJECTED", "MVIREJECTED", "AOREJECTED");
		default:
			break;
		}
		return Collections.emptyList();
	}

	@Override
	public List<String> getEodReportsDropDown(String roleType, boolean serviceStatus, boolean module) {
		Optional<PropertiesDTO> servicedropDown = Optional.empty();
		List<String> dropDown = new ArrayList<>();
		String msg = "Unautorized role / This role is blocked";
		logger.debug("reports dropdown service started");
		if (module && !serviceStatus) {
			servicedropDown = propertiesDAO.findByEodReportsStatusTrueAndRoleTypeAndModuleCodeStatusTrue(roleType);
			checkDataPreserntOrNot(servicedropDown, msg);
			servicedropDown.get().getModuleCode().forEach(action -> {
				dropDown.add(action.getServiceType());
			});
			return dropDown;
		}
		if (serviceStatus) {
			if (module) {
				servicedropDown = propertiesDAO
						.findByEodReportsStatusTrueAndRoleTypeAndDlCitizenServicesStatusTrue(roleType);
				checkDataPreserntOrNot(servicedropDown, msg);
				servicedropDown.get().getDlCitizenServices().forEach(action -> {
					dropDown.add(action.getServiceName());
				});
				return dropDown;
			}
			servicedropDown = propertiesDAO.findByEodReportsStatusTrueAndRoleTypeAndCitizenServicesStatusTrue(roleType);
			checkDataPreserntOrNot(servicedropDown, msg);
			servicedropDown.get().getCitizenServices().forEach(action -> {
				dropDown.add(action.getServiceName());
			});
		} else {
			servicedropDown = propertiesDAO
					.findByEodReportsStatusTrueAndRoleTypeAndServicesDropDownStatusTrue(roleType);
			checkDataPreserntOrNot(servicedropDown, msg);
			servicedropDown.get().getServicesDropDown().forEach(action -> {
				dropDown.add(action.getServiceType());
			});
		}
		logger.debug("reports dropdown service successfully ended");
		return dropDown;
	}

	private void checkDataPreserntOrNot(Optional<?> service, String errorMsg) {
		if (!service.isPresent()) {
			throw new BadRequestException(errorMsg);
		}
	}

	private ActionCountDetailsVO getAllRecordsBasedOnStatus(String reportName, JwtUser jwtUser, EODReportVO eodVO,
			Pageable pagable) {

		ActionCountDetailsVO actionVO = new ActionCountDetailsVO();
		long approvedCount = 0L;
		long rejectedCount = 0L;
		String officeCode = jwtUser.getOfficeCode();
		if (StringUtils.isNotBlank(eodVO.getOfficeCode())) {
			officeCode = eodVO.getOfficeCode();
		}
		LocalDateTime fromDate = null;
		LocalDateTime toDate = null;
		if (!reportName.equals("SINGLE RECORD")) {
			fromDate = getTimewithDate(eodVO.getFromDate(), false);
			toDate = getTimewithDate(eodVO.getToDate(), true);
		}
		String userId = jwtUser.getId();
		if (StringUtils.isNotEmpty(eodVO.getRoleName())) {
			userId = eodVO.getUserName();
			eodVO.setSelectedRole(eodVO.getRoleName());
		}

		List<RegServiceReportVO> reportVo = Collections.emptyList();
		switch (reportName) {
		case "ALL SERVICES":
			if (eodVO.getSelectedRole().equalsIgnoreCase("ALL")) {
				List<String> roles = getDeptRoles().getRoles().stream().map(p -> p.getRole())
						.collect(Collectors.toList());
				Map<String, String> officeCodes = getAllOfficesNames(officeCode, eodVO.getDistrictId());
				final LocalDateTime startDate = fromDate;
				final LocalDateTime endDate = toDate;
				List<ActionCountDetailsVO> roleBasedCountsList = new ArrayList<ActionCountDetailsVO>();
				officeCodes.forEach((key, value) -> {
					Long approvedValue = 0L;
					List<UserWiseEODCount> approvedOffidceCount = getAllOfficesRoleBasedCounts(startDate, endDate,
							getStatusListBasedOnRole(eodVO.getSelectedRole(), Boolean.TRUE), roles, key);
					List<UserWiseEODCount> rejectedOfficeCount = getAllOfficesRoleBasedCounts(startDate, endDate,
							getStatusListBasedOnRole(eodVO.getSelectedRole(), Boolean.FALSE), roles, key);
					ActionCountDetailsVO roleBasedCounts = new ActionCountDetailsVO();
					List<RoleBasedCounts> rolesList = new ArrayList<RoleBasedCounts>();
					roleBasedCounts.setOfficeCode(key);
					roleBasedCounts.setOfficeName(value);
					approvedOffidceCount.stream().forEach(action -> {
						RoleBasedCounts counts = new RoleBasedCounts();
						counts.setApprovedCount(action.getCount());
						counts.setRejectedCount(approvedValue);
						counts.setTotalCount(action.getCount());
						counts.setRoleName(action.getUserName());
						rolesList.add(counts);
					});
					List<String> approvedRoles = rolesList.stream().map(r -> r.getRoleName())
							.collect(Collectors.toList());
					roles.stream().forEach(action1 -> {
						if (!approvedRoles.contains(action1)) {
							RoleBasedCounts counts = new RoleBasedCounts();
							counts.setApprovedCount(approvedValue);
							counts.setRejectedCount(approvedValue);
							counts.setTotalCount(approvedValue);
							counts.setRoleName(action1);
							rolesList.add(counts);
						}
					});

					rolesList.stream().forEach(action2 -> {
						Optional<UserWiseEODCount> result = rejectedOfficeCount.stream()
								.filter(p -> p.getUserName().equals(action2.getRoleName())).collect(toSingleton());
						if (result.isPresent()) {
							action2.setRejectedCount(result.get().getCount());
							action2.setTotalCount(action2.getApprovedCount() + action2.getRejectedCount());
						}
					});
					roleBasedCounts.setUsersBasedCounts(rolesList);
					roleBasedCountsList.add(roleBasedCounts);
				});
				actionVO.setRolesBasedReport(roleBasedCountsList);
				return actionVO;
			}

			if (userId.equalsIgnoreCase("ALL")) {
				return getEODReportBasedOnUsers(actionVO, approvedCount, jwtUser, eodVO, fromDate, toDate);
			}
			approvedCount = getReportDetails(eodVO.getSelectedRole(), userId,
					getStatusListBasedOnRole(eodVO.getSelectedRole(), Boolean.TRUE), officeCode, fromDate, toDate);
			rejectedCount = getReportDetails(eodVO.getSelectedRole(), userId,
					getStatusListBasedOnRole(eodVO.getSelectedRole(), Boolean.FALSE), officeCode, fromDate, toDate);
			actionVO.setApprovedCount(approvedCount);
			actionVO.setRejectedCount(rejectedCount);
			actionVO.setTotalCount(approvedCount + rejectedCount);
			actionVO.setServiceDesc(eodVO.getName());
			return actionVO;
		case "APPROVED":
			reportVo = getServicesReport(reportVo, eodVO.getSelectedRole(), userId,
					getStatusListBasedOnRole(eodVO.getSelectedRole(), Boolean.TRUE), officeCode, fromDate, toDate,
					pagable);
			actionVO.setRegSerList(reportVo);
			return actionVO;
		case "REJECTED":
			reportVo = getServicesReport(reportVo, eodVO.getSelectedRole(), userId,
					getStatusListBasedOnRole(eodVO.getSelectedRole(), Boolean.FALSE), officeCode, fromDate, toDate,
					pagable);
			actionVO.setRegSerList(reportVo);
			return actionVO;
		case "TOTAL COUNT":
			List<String> status = getStatusListBasedOnRole(eodVO.getSelectedRole(), Boolean.FALSE);
			List<String> approvedStatus = getStatusListBasedOnRole(eodVO.getSelectedRole(), Boolean.TRUE);
			List<String> newList = ListUtils.union(status, approvedStatus);
			reportVo = getServicesReport(reportVo, eodVO.getSelectedRole(), userId, newList, officeCode, fromDate,
					toDate, pagable);
			actionVO.setRegSerList(reportVo);
			return actionVO;
		case "SINGLE SERVICE":
			if (eodVO.getServiceTotal()) {
				approvedCount = regServiceReportDAO
						.countByActionRoleNameAndActionUserNameAndOfficeCodeAndActionStatusInAndCitizenServicesNamesAndActionTimeBetween(
								eodVO.getSelectedRole(), userId, officeCode,
								getStatusListBasedOnRole(eodVO.getSelectedRole(), Boolean.TRUE), eodVO.getServiceName(),
								fromDate, toDate);
				rejectedCount = regServiceReportDAO
						.countByActionRoleNameAndActionUserNameAndOfficeCodeAndActionStatusInAndCitizenServicesNamesAndActionTimeBetween(
								eodVO.getSelectedRole(), userId, officeCode,
								getStatusListBasedOnRole(eodVO.getSelectedRole(), Boolean.FALSE),
								eodVO.getServiceName(), fromDate, toDate);
				actionVO.setApprovedCount(approvedCount);
				actionVO.setRejectedCount(rejectedCount);
				actionVO.setTotalCount(approvedCount + rejectedCount);
				actionVO.setServiceDesc(eodVO.getName());
				return actionVO;
			}
			List<RegServiceReportDTO> dto = regServiceReportDAO
					.findByActionRoleNameAndActionUserNameAndOfficeCodeAndActionStatusInAndCitizenServicesNamesAndActionTimeBetween(
							eodVO.getSelectedRole(), userId, officeCode, eodVO.getStatusList(), eodVO.getServiceName(),
							fromDate, toDate, pagable);
			actionVO.setRegSerList(getReportVO(dto));
			return actionVO;

		default:
			Optional<RegServiceDTO> regSerDTO = regServiceDAO.findByApplicationNo(eodVO.getApplicationNo());
			if (regSerDTO.isPresent()) {
				Optional<RegServiceVO> vo = regServiceMapper.convertEntity(regSerDTO);
				actionVO.setRegSerVO(vo.get());
				return actionVO;
			}
			Optional<StagingRegistrationDetailsDTO> stagingDetails = stagingRegistrationDetailsDAO
					.findByApplicationNo(eodVO.getApplicationNo());
			RegServiceVO regVo = new RegServiceVO();
			if (stagingDetails.isPresent()) {
				regVo.setRegistrationDetails(registrationDetailsMapper.convertEntity(stagingDetails.get()));
				actionVO.setRegSerVO(regVo);
			}
			Optional<RegistrationDetailsDTO> regdetails = registrationDetailDAO
					.findByApplicationNo(eodVO.getApplicationNo());
			if (regdetails.isPresent()) {
				regVo.setRegistrationDetails(registrationDetailsMapper.convertEntity(regdetails.get()));
				actionVO.setRegSerVO(regVo);
			}
			break;
		}
		return actionVO;
	}

	private Integer getReportDetails(String role, String userName, List<String> status, String officeCode,
			LocalDateTime fromDate, LocalDateTime toDate) {

		return regServiceReportDAO
				.countByActionRoleNameAndActionUserNameAndActionStatusInAndOfficeCodeAndActionTimeBetween(role,
						userName, status, officeCode, fromDate, toDate);

	}

	private List<RegServiceReportVO> getServicesReport(List<RegServiceReportVO> reportVo, String role, String userName,
			List<String> status, String officeCode, LocalDateTime fromDate, LocalDateTime toDate, Pageable page) {

		List<RegServiceReportDTO> reportDTO = regServiceReportDAO
				.findByActionRoleNameAndActionUserNameAndActionStatusInAndOfficeCodeAndActionTimeBetween(role, userName,
						status, officeCode, fromDate, toDate, page);
		return getReportVO(reportDTO);

	}

	private List<RegServiceReportVO> getReportVO(List<RegServiceReportDTO> reportDTO) {

		if (CollectionUtils.isEmpty(reportDTO)) {
			throw new BadRequestException("Data not avaialble");
		}
		List<RegServiceReportVO> reportVo = new ArrayList<>();
		reportDTO.stream().forEach(action -> {
			RegServiceReportVO vo = new RegServiceReportVO();
			if (StringUtils.isNoneBlank(action.getCovDesc())) {
				vo.setClassOfVehicle(action.getCovDesc());
			}
			vo.setApplicationNumber(action.getApplicationNumber());
			vo.setCitizenServicesNames(action.getCitizenServicesNames());
			vo.setActionStatus(action.getActionStatus());
			vo.setPrNumber(action.getPrNumber());
			vo.setActionStatus(action.getActionStatus());
			vo.setActionTime(action.getActionTime());
			vo.setCreatedDate(action.getCreatedDate());
			vo.setClassOfVehicle(action.getClassOfVehicle());
			reportVo.add(vo);

		});
		return reportVo;
	}

	@Override
	public List<UsersDropDownVO> getEodRolesList(boolean value, String selectedRole, JwtUser jwtUser) {

		List<UsersDropDownVO> dropDownforallroles = new ArrayList<>();
		if (value && StringUtils.isNotEmpty(selectedRole)) {
			List<UserDTO> users = null;
			if (StringUtils.isNotBlank(jwtUser.getPrimaryRole().getName())
					&& jwtUser.getPrimaryRole().getName().equals("DTC")) {
				List<String> officeCodes = getOfficeCodes(jwtUser.getOfficeCode());
				users = userDAO.findByPrimaryRoleNameOrAdditionalRolesNameAndOfficeOfficeCodeIn(selectedRole,
						selectedRole, officeCodes);
			} else {
				users = userDAO.findByPrimaryRoleNameOrAdditionalRolesNameAndOfficeOfficeCodeNative(selectedRole,
						selectedRole, jwtUser.getOfficeCode());
			}
			if (CollectionUtils.isEmpty(users)) {
				throw new BadRequestException("Dept Users Not Available");
			}
			UsersDropDownVO userVO = new UsersDropDownVO();
			userVO.setUserId("ALL");
			userVO.setUserName("ALL");
			dropDownforallroles.add(userVO);
			users.stream().forEach(action -> {
				UsersDropDownVO vo = new UsersDropDownVO();
				vo.setUserId(action.getUserId());
				vo.setUserName(getValue(action.getFirstName()) + " " + getValue(action.getLastName()));
				dropDownforallroles.add(vo);
			});

			return dropDownforallroles;
		}
		PropertiesDTO dto = getDeptRoles();
		if (jwtUser.getPrimaryRole().getName().equals("DTC")) {
			UsersDropDownVO vo = new UsersDropDownVO();
			vo.setRoleName("ALL");
			dropDownforallroles.add(vo);
		}
		dto.getRoles().stream().forEach(action -> {
			UsersDropDownVO vo = new UsersDropDownVO();
			vo.setRoleName(action.getRole());
			dropDownforallroles.add(vo);
		});
		return dropDownforallroles;

	}

	@Override
	public List<TaxDetailsVO> getTaxReport(String prNo) {
		Optional<RegistrationDetailsDTO> regRecord = registrationDetailDAO.findFirstByPrNoOrderByCreatedDateDesc(prNo);
		if (!regRecord.isPresent()) {
			throw new BadRequestException("No records found for this Pr no:" + prNo);
		}
		String appNo = regRecord.get().getApplicationNo();
		List<TaxDetailsDTO> reportList = taxDetailsDAO.findByApplicationNoOrderByCreatedDateDescnative(appNo);

		if (reportList.isEmpty()) {
			throw new BadRequestException("No records found ");
		}

		List<TaxDetailsVO> taxReportList = new ArrayList<>();
		reportList.stream().forEach(val -> {
			ClassOfVehiclesDTO masterCov = classOfVehiclesDAO.findByCovcode(val.getClassOfVehicle());
			String covDesc = masterCov.getCovdescription();
			if (covDesc.equals(null) || covDesc == null) {
				throw new BadRequestException("No Class Of Vehicle Found in Masters :" + val.getClassOfVehicle());
			}
			List<TaxDetailsVO> taxDetailsList = setTaxDetails(reportList, val, covDesc);

			if (!taxDetailsList.isEmpty()) {
				taxReportList.addAll(taxDetailsList);
			}
		});
		if (taxReportList.isEmpty()) {
			throw new BadRequestException("Tax Data Not found for this PR:" + prNo);
		}
		return taxReportList;

	}

	private List<TaxDetailsVO> setTaxDetails(List<TaxDetailsDTO> reportList, TaxDetailsDTO val, String covDesc) {
		List<Map<String, TaxComponentDTO>> taxDetails = val.getTaxDetails();
		List<TaxDetailsVO> taxReportVO = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(taxDetails)) {
			List<TaxComponentDTO> taxComponentList = taxDetails.stream().map(ta -> ta.values()).flatMap(x -> x.stream())
					.collect(Collectors.toList());
			taxReportVO = taxReportMappers.convertDTO(taxComponentList);
			taxReportVO.stream().forEach(tax -> {
				tax.setCov(covDesc);
			});
		}
		return taxReportVO;

	}

	@Override
	public List<FitnessReportsDemoVO> getFitnessData(JwtUser jwtUser, RegReportDuplicateVO regVO, String actionUserName,
			Pageable page, String officeCode) {
		// List<ClassOfVehiclesDTO> findAll =
		// mongoTemplate.findAll(ClassOfVehiclesDTO.class, "fc_details");
		List<ClassOfVehiclesDTO> findAll2 = classOfVehiclesDAO.findAll();

		LocalDateTime fromDate = getTimewithDate(regVO.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(regVO.getToDate(), true);

		List<FitnessReportsDemoVO> listFitnessReportsDemoVO = new ArrayList<FitnessReportsDemoVO>();
		if (regVO.getActionUserName().equals("ALL")) {
			Criteria criteria3 = Criteria.where("createdDate").gte(fromDate).lte(toDate);
			Criteria criteria1 = Criteria.where("officeCode").is(officeCode);
			Query query = new Query();
			query.addCriteria(new Criteria().andOperator(criteria1, criteria3));
			List<FcReportsDemoDTO> find = mongoTemplate.find(query, FcReportsDemoDTO.class);
			if (!find.isEmpty()) {

				find.stream().forEach(fcReportsDemoDTO -> {
					FitnessReportsDemoVO fitnessReportsDemoVO = new FitnessReportsDemoVO();
					findAll2.stream().forEach(s -> {
						if (s.getCovcode().equals(fcReportsDemoDTO.getClassOfVehicle())) {
							fitnessReportsDemoVO.setClassOfVehicle(s.getCovdescription());
						}
					});
					FitnessReportsDemoVO convertToFcVO = nonPaymentDetailsMapper.convertToFcVO(fcReportsDemoDTO,
							fitnessReportsDemoVO);
					listFitnessReportsDemoVO.add(convertToFcVO);
				});
				return listFitnessReportsDemoVO;
			} else {
				throw new BadRequestException("NO Data Found");
			}

		} else {

			Criteria criteria4 = Criteria.where("createdDate").gte(fromDate).lte(toDate);
			Criteria criteria5 = Criteria.where("userId").is(regVO.getActionUserName());
			Query query1 = new Query();
			Query addCriteria = query1.addCriteria(new Criteria().andOperator(criteria4, criteria5));
			List<FcReportsDemoDTO> singleUser = mongoTemplate.find(addCriteria, FcReportsDemoDTO.class);
			if (!singleUser.isEmpty()) {
				singleUser.stream().forEach(fcReportsDemoDTO -> {
					FitnessReportsDemoVO fitnessReportsDemoVO = new FitnessReportsDemoVO();
					findAll2.stream().forEach(s -> {

						if (s.getCovcode().equals(fcReportsDemoDTO.getClassOfVehicle())) {
							fitnessReportsDemoVO.setClassOfVehicle(s.getCovdescription());
						}
					});
					FitnessReportsDemoVO convertToFcVO = nonPaymentDetailsMapper.convertToFcVO(fcReportsDemoDTO,
							fitnessReportsDemoVO);
					listFitnessReportsDemoVO.add(convertToFcVO);
				});
				return listFitnessReportsDemoVO;
			} else {
				throw new BadRequestException("NO Data Found");
			}
		}

	}

	private String getValue(String value) {
		if (StringUtils.isNotBlank(value)) {
			return value;
		}
		return StringUtils.EMPTY;
	}

	/**
	 * service for generating common excel
	 */
	@Override
	public Boolean getExcel(HttpServletResponse response, RegReportVO regVO, String reportName, String userId,
			String officeCode, List<InvoiceDetailsReportVo> invoiceReport, Pageable page) throws Exception {

		Optional<ReportNameAndFieldOrderDTO> reportPropertyField = reportPropertyDao.findByReportName(reportName);

		ExcelService excel = new ExcelServiceImpl();
		List<String> header = new ArrayList<String>();
		if (!reportPropertyField.isPresent()) {
			excel.setHeaders(header, "InvoiceDetailsReport");
			if (header.isEmpty())
				throw new Exception("Report Not Valid");
			ReportNameAndFieldOrderDTO report = new ReportNameAndFieldOrderDTO();
			report.setReportName(reportName);
			reportPropertyField = Optional.of(report);

		} else {
			setHeaders(header, reportPropertyField.get());
		}
		ReportNameAndFieldOrderDTO reportProperty = reportPropertyField.get();

		String name = reportProperty.getReportName() + new Random().nextInt(1000);
		String fileName = name + ".xlsx";
		String sheetName = reportProperty.getReportName();

		setHttpResponse(response, fileName);
		XSSFWorkbook wb = null;

		wb = excel.renderData(
				getReportDataAndPrepareCell(header, reportName, userId, officeCode, regVO, invoiceReport, page), header,
				fileName, sheetName);
		try {
			ServletOutputStream outputStream = response.getOutputStream();
			wb.write(outputStream);
			outputStream.flush();

			outputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		return true;
	}

	private void setHttpResponse(HttpServletResponse response, String fileName) {
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		response.setHeader("Pragma", "public");
		response.setHeader("Cache-Control", "no-store");
		response.addHeader("Cache-Control", "max-age=0");
	}

	private List<List<CellProps>> getReportDataAndPrepareCell(List<String> header, String reportName, String userId,
			String officeCode, RegReportVO regVO, List<InvoiceDetailsReportVo> list, Pageable page) {

		switch (reportName) {
		case "Suspension Report Status":
			return prepareCellSuspensionReportStatus(header, getData(userId, officeCode, regVO, page));
		case "Offence Wise Enforcement Report":
			return prepareCellForOffenceEnforcement(header,
					enfRepSerImp.offenceEnforcementReport(userId, officeCode, mapVcrVO(regVO)));
		case "Status Reports":
			return prepareCellForstatusReports(header, getCCOApprovedRejectCount(officeCode, mapReportInputVO(regVO)));
		case "InvoiceDetailsReport":
			// rtaServiceReport.eBiddingCovWiseData(regVO.getFromDate(), regVO.getToDate(),
			// regVO.getCov(), regVO.getSeries(), userId);
			return regService.prepareCellProps(header, list);
		default:
			break;
		}
		return null;

	}

	/**
	 * map propertyField as headers.
	 * 
	 * @param headers
	 * @param headerKey
	 * @param key
	 */
	public void setHeaders(List<String> headers, ReportNameAndFieldOrderDTO headerKey) {
		headerKey.getColumnOrderAndName().forEach((k, v) -> {
			headers.add(v);
		});
	}

	private List<List<CellProps>> prepareCellSuspensionReportStatus(List<String> header, List<RegReportVO> data) {

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();
		boolean chkStatus = false;
		for (RCActionsVO vo : data.get(0).getRcReport()) {
			chkStatus = check(vo.getActionStatus().getStatus());
			List<CellProps> result = new ArrayList<CellProps>();
			for (int i = 0; i < header.size(); i++) {
				CellProps cellpro = new CellProps();
				switch (i) {
				case 0:
					cellpro.setFieldValue(vo.getPrNo());
					result.add(cellpro);
					break;
				case 1:
					cellpro.setFieldValue(vo.getSuspensionDetailsVO().getOwnerName());
					result.add(cellpro);
					break;
				case 2:
					cellpro.setFieldValue(
							DateConverters.convertHSRLocalDateFormat(vo.getSuspensionDetailsVO().getRegValidity()));
					result.add(cellpro);
					break;
				case 3:
					cellpro.setFieldValue(vo.getSuspensionDetailsVO().getClassOfVehicle());
					result.add(cellpro);
					break;
				case 4:
					cellpro.setFieldValue(vo.getActionStatus().getStatus());
					result.add(cellpro);
					break;
				case 5:
					cellpro.setFieldValue(
							(chkStatus) ? StringUtils.EMPTY : getData(vo.getSuspensionDetailsVO().getFromDate(), null));
					result.add(cellpro);
					break;
				case 6:
					cellpro.setFieldValue(
							(chkStatus) ? StringUtils.EMPTY : getData(vo.getSuspensionDetailsVO().getToDate(), null));
					result.add(cellpro);
					break;
				case 7:
					cellpro.setFieldValue((chkStatus) ? StringUtils.EMPTY
							: getData(null, vo.getSuspensionDetailsVO().getSuspendedBy()));
					result.add(cellpro);
					break;
				case 8:
					cellpro.setFieldValue(
							(chkStatus) ? getData(null, vo.getSuspensionDetailsVO().getReason()) : StringUtils.EMPTY);
					result.add(cellpro);
					break;

				case 9:
					cellpro.setFieldValue(vo.getSuspensionDetailsVO().getReferenceNumber());
					result.add(cellpro);
					break;
				case 10:
					cellpro.setFieldValue(
							DateConverters.convertLocalDateFormat(vo.getSuspensionDetailsVO().getReferenceDate()));
					result.add(cellpro);
					break;
				case 11:
					cellpro.setFieldValue(vo.getSuspensionDetailsVO().getRevokedBy());
					result.add(cellpro);
					break;
				case 12:
					cellpro.setFieldValue(getData(vo.getSuspensionDetailsVO().getToDate(), null));
					result.add(cellpro);
					break;
				default:
					break;
				}
			}
			cell.add(result);
		}
		return cell;

	}

	public boolean check(String status) {
		return status.equals(Status.RCActionStatus.REVOKED.toString());
	}

	public String getData(LocalDate date, String suspendedBy) {
		return (date == null && suspendedBy == null) ? StringUtils.EMPTY
				: (date != null) ? DateConverters.convertLocalDateFormat(date) : suspendedBy;
	}

	public List<List<CellProps>> prepareCellForOffenceEnforcement(List<String> header, ReportsVO data) {
		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();
		for (ReportVO vo : data.getReport()) {
			List<CellProps> result = new ArrayList<CellProps>();
			for (int i = 0; i < header.size(); i++) {
				CellProps cellpro = new CellProps();
				switch (i) {
				case 0:
					cellpro.setFieldValue(vo.getOffenceDesc());
					result.add(cellpro);
					break;
				case 1:
					cellpro.setFieldValue(getData(null, String.valueOf(vo.getOffenceCount())));
					result.add(cellpro);
					break;
				case 2:
					cellpro.setFieldValue(getData(null, String.valueOf(vo.getAmount())));
					result.add(cellpro);
					break;

				}
				cell.add(result);
			}
		}
		return cell;

	}

	public VcrVo mapVcrVO(RegReportVO reg) {
		VcrVo vo = new VcrVo();
		BeanUtils.copyProperties(reg, vo);
		return vo;

	}

	public ReportInputVO mapReportInputVO(RegReportVO vo) {
		ReportInputVO reportIn = new ReportInputVO();
		BeanUtils.copyProperties(vo, reportIn);
		return reportIn;

	}

	public List<List<CellProps>> prepareCellForstatusReports(List<String> header, Optional<List<CCOReportVO>> data) {
		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();
		for (CCOReportVO vo : data.get()) {
			List<CellProps> result = new ArrayList<CellProps>();
			for (int i = 0; i < header.size(); i++) {
				CellProps cellpro = new CellProps();
				switch (i) {
				case 0:
					cellpro.setFieldValue(getData(null, vo.getName()));
					result.add(cellpro);
					break;
				case 1:
					cellpro.setFieldValue(getData(null, String.valueOf(vo.getApproved())));
					result.add(cellpro);
					break;
				case 2:
					cellpro.setFieldValue(getData(null, String.valueOf(vo.getRejected())));
					result.add(cellpro);
					break;
				case 3:
					cellpro.setFieldValue(getData(null, String.valueOf(vo.getTotalcount())));
					result.add(cellpro);
					break;
				default:
					break;
				}
			}
			cell.add(result);
		}

		return cell;

	}

	@Override
	public Optional<ReportsVO> getaadharSeedIngApprovalsReport(RegReportVO regReportVO, Pageable pagable) {
		LocalDateTime fromDate = getTimewithDate(regReportVO.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(regReportVO.getToDate(), true);
		ReportsVO aadharSeedIngReportsVO = new ReportsVO();
		// Criteria criteria1 =
		// Criteria.where("officeCode").is(regReportVO.getOfficeCode());
		Criteria criteria2 = Criteria.where("createdDate").gte(fromDate).lte(toDate);
		Criteria criteria3 = Criteria.where("status")
				.in(Arrays.asList(Status.AadhaarSeedStatus.AUTO_APPROVED, Status.AadhaarSeedStatus.AUTO_REJECTED,
						Status.AadhaarSeedStatus.APPROVED, Status.AadhaarSeedStatus.REJECTED));
		Query query = new Query();
		query.addCriteria(new Criteria().andOperator(criteria2, criteria3));
		List<AadhaarSeedDTO> aadhaarSeededStatusList = mongoTemplate.find(query, AadhaarSeedDTO.class);
		if (aadhaarSeededStatusList.size() > 0 || aadhaarSeededStatusList != null) {
			aadharSeedIngReportsVO
					.setAadharSeedApprovalsDistrictWise(aadharSeedingApprovalsDistWise(aadhaarSeededStatusList));
			return Optional.of(aadharSeedIngReportsVO);
		}
		return null;

	}

	@SuppressWarnings("unused")
	private List<AadharSeedingDistWiseVO> aadharSeedingApprovalsDistWise(List<AadhaarSeedDTO> aadhaarSeededStatusList) {
		List<AadharSeedingDistWiseVO> distWiseList = new ArrayList<>();
		try {
			for (AadhaarSeedDTO aadhaarSeed : aadhaarSeededStatusList) {
				AadharSeedingDistWiseVO distWise = new AadharSeedingDistWiseVO();
				Optional<OfficeDTO> office = officeDAO.findByOfficeCode(aadhaarSeed.getIssuedOfficeCode());
				Optional<DistrictVO> distDetials = districtMapper
						.convertEntity(districtDAO.findBydistrictId(office.get().getDistrict()));
				if (distDetials.isPresent() && !CollectionUtils.isEmpty(distWiseList) && !distWiseList.isEmpty()
						&& distWiseList.stream().anyMatch(val -> val.getDistrictVO() != null
								&& val.getDistrictVO().getName().equalsIgnoreCase(distDetials.get().getName()))) {
					for (AadharSeedingDistWiseVO sameDist : distWiseList) {// KURNOOL
						if (sameDist.getDistrictVO().getName().equalsIgnoreCase(distDetials.get().getName())) {
							if (sameDist.getAutoApproved() != null
									&& sameDist.getAutoApproved().equals(aadhaarSeed.getStatus().getStatus())) {
								sameDist.setAutoApprovedCount(sameDist.getAutoApprovedCount() + 1);
								// logger.info("Dist Name"+sameDist.getDistrictVO().getName() + "Application
								// Satus:"+aadhaarSeed.getStatus());
							} else if (sameDist.getAutoRejected() != null
									&& sameDist.getAutoRejected().equals(aadhaarSeed.getStatus().getStatus())) {
								sameDist.setAutoRejectedCount(sameDist.getAutoRejectedCount() + 1);
								// logger.info("Dist Name"+sameDist.getDistrictVO().getName() + "Application
								// Satus:"+aadhaarSeed.getStatus());
							} else if (sameDist.getManualApproved() != null
									&& sameDist.getManualApproved().equals(aadhaarSeed.getStatus().getStatus())) {
								sameDist.setManualApprovedCount(sameDist.getManualApprovedCount() + 1);
								// logger.info("Dist Name"+sameDist.getDistrictVO().getName() + "Application
								// Satus:"+aadhaarSeed.getStatus());
							} else if (sameDist.getManualRejected() != null
									&& sameDist.getManualRejected().equals(aadhaarSeed.getStatus().getStatus())) {
								sameDist.setManualRejectedCount(sameDist.getManualRejectedCount() + 1);
								// logger.info("Dist Name"+sameDist.getDistrictVO().getName() + "Application
								// Satus:"+aadhaarSeed.getStatus());
							} else if (aadhaarSeed.getStatus().toString()
									.equals(Status.AadhaarSeedStatus.AUTO_APPROVED.getStatus())) {
								sameDist.setAutoApproved(Status.AadhaarSeedStatus.AUTO_APPROVED.getStatus());
								sameDist.setAutoApprovedCount(1);
								// logger.info("Dist Name"+sameDist.getDistrictVO().getName() + "Application
								// Satus:"+aadhaarSeed.getStatus());
							} else if (aadhaarSeed.getStatus().toString()
									.equals(Status.AadhaarSeedStatus.AUTO_REJECTED.getStatus())) {
								sameDist.setAutoRejected(Status.AadhaarSeedStatus.AUTO_REJECTED.getStatus());
								sameDist.setAutoRejectedCount(1);
								// logger.info("Dist Name"+sameDist.getDistrictVO().getName() + "Application
								// Satus:"+aadhaarSeed.getStatus());
							} else if (aadhaarSeed.getStatus().toString()
									.equals(Status.AadhaarSeedStatus.APPROVED.getStatus())) {
								sameDist.setManualApproved(Status.AadhaarSeedStatus.APPROVED.getStatus());
								sameDist.setManualApprovedCount(1);
								// logger.info("Dist Name"+sameDist.getDistrictVO().getName() + "Application
								// Satus:"+aadhaarSeed.getStatus());
							} else if (aadhaarSeed.getStatus().toString()
									.equals(Status.AadhaarSeedStatus.REJECTED.getStatus())) {
								sameDist.setManualRejected(Status.AadhaarSeedStatus.REJECTED.getStatus());
								sameDist.setManualRejectedCount(1);
								// logger.info("Dist Name"+sameDist.getDistrictVO().getName() + "Application
								// Satus:"+aadhaarSeed.getStatus());
							}

							break;
						}
					}
				}

				else {
					if (aadhaarSeed.getStatus().toString().equals(Status.AadhaarSeedStatus.AUTO_APPROVED.getStatus())) {
						distWise.setAutoApproved(Status.AadhaarSeedStatus.AUTO_APPROVED.getStatus());
						distWise.setAutoApprovedCount(1);
						distWise.setDistrictVO(distDetials.get());
						distWise.setDistrictName(distDetials.get().getDistrictName());
					}
					if (aadhaarSeed.getStatus().toString().equals(Status.AadhaarSeedStatus.AUTO_REJECTED.getStatus())) {
						distWise.setAutoRejected(Status.AadhaarSeedStatus.AUTO_REJECTED.getStatus());
						distWise.setAutoRejectedCount(1);
						distWise.setDistrictVO(distDetials.get());
						distWise.setDistrictName(distDetials.get().getDistrictName());
					}

					if (aadhaarSeed.getStatus().toString().equals(Status.AadhaarSeedStatus.APPROVED.getStatus())) {
						distWise.setManualApproved(Status.AadhaarSeedStatus.APPROVED.getStatus());
						distWise.setManualApprovedCount(1);
						distWise.setDistrictVO(distDetials.get());
						distWise.setDistrictName(distDetials.get().getDistrictName());
					}
					if (aadhaarSeed.getStatus().toString().equals(Status.AadhaarSeedStatus.REJECTED.getStatus())) {
						distWise.setManualRejected(Status.AadhaarSeedStatus.REJECTED.getStatus());
						distWise.setManualRejectedCount(1);
						distWise.setDistrictVO(distDetials.get());
						distWise.setDistrictName(distDetials.get().getDistrictName());
					}
					List<AadharSeedingOfficeWiseVO> officeWiseList = aadharSeedingOfficeWiseCount(
							aadhaarSeededStatusList);
					List<AadharSeedingOfficeWiseVO> distWiseOffices = new ArrayList<>();
					if (!officeWiseList.isEmpty()) {
						for (AadharSeedingOfficeWiseVO offices : officeWiseList) {
							if (offices.getOfficeVO().getDistrict() == distDetials.get().getDistrictId()) {
								distWiseOffices.add(offices);
							}
						}
					}
					distWise.setDistrictOfficeDetils(distWiseOffices);
					if (distWise.getDistrictName() != null)
						distWiseList.add(distWise);
				}
			}
		} catch (Exception e) {
			logger.error("--" + e.getMessage());
		}
		if (distWiseList == null || distWiseList.isEmpty()) {
			throw new BadRequestException("Aadhar Seeding Data not available");
		}
		return distWiseList;
	}

	private List<AadharSeedingOfficeWiseVO> aadharSeedingOfficeWiseCount(List<AadhaarSeedDTO> aadhaarSeededStatusList) {
		List<AadharSeedingOfficeWiseVO> officeWiseList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(aadhaarSeededStatusList)) {
			for (AadhaarSeedDTO seedingApprovals : aadhaarSeededStatusList) {
				AadharSeedingOfficeWiseVO officeCountVo = new AadharSeedingOfficeWiseVO();
				Optional<OfficeDTO> officeDteails = officeDAO.findByOfficeCode(seedingApprovals.getIssuedOfficeCode());
				OfficeVO officeDetails = officeMapper.convertEntity(officeDteails.get());
				if (officeDteails.isPresent() && !CollectionUtils.isEmpty(officeWiseList) && !officeWiseList.isEmpty()
						&& officeWiseList.stream().anyMatch(val -> val.getOfficeVO().getOfficeCode()
								.equalsIgnoreCase(officeDetails.getOfficeCode()))) {
					for (AadharSeedingOfficeWiseVO sameOffice : officeWiseList) {
						if (sameOffice.getOfficeVO().getOfficeCode().equalsIgnoreCase(officeDetails.getOfficeCode())) {
							if (sameOffice.getAutoApproved() != null
									&& sameOffice.getAutoApproved().equals(seedingApprovals.getStatus().getStatus())) {
								sameOffice.setAutoApprovedCount(sameOffice.getAutoApprovedCount() + 1);
							} else if (sameOffice.getAutoRejected() != null
									&& sameOffice.getAutoRejected().equals(seedingApprovals.getStatus().getStatus())) {
								sameOffice.setAutoRejectedCount(sameOffice.getAutoRejectedCount() + 1);
							} else if (sameOffice.getManualApproved() != null && sameOffice.getManualApproved()
									.equals(seedingApprovals.getStatus().getStatus())) {
								sameOffice.setManualApprovedCount(sameOffice.getManualApprovedCount() + 1);
							} else if (sameOffice.getManualRejected() != null && sameOffice.getManualRejected()
									.equals(seedingApprovals.getStatus().getStatus())) {
								sameOffice.setManualRejectedCount(sameOffice.getManualRejectedCount() + 1);
							} else if (seedingApprovals.getStatus().toString()
									.equals(Status.AadhaarSeedStatus.AUTO_APPROVED.getStatus())) {
								sameOffice.setAutoApproved(Status.AadhaarSeedStatus.AUTO_APPROVED.getStatus());
								sameOffice.setAutoApprovedCount(1);
							} else if (seedingApprovals.getStatus().toString()
									.equals(Status.AadhaarSeedStatus.AUTO_REJECTED.getStatus())) {
								sameOffice.setAutoRejected(Status.AadhaarSeedStatus.AUTO_REJECTED.getStatus());
								sameOffice.setAutoRejectedCount(1);
							} else if (seedingApprovals.getStatus().toString()
									.equals(Status.AadhaarSeedStatus.APPROVED.getStatus())) {
								sameOffice.setManualApproved(Status.AadhaarSeedStatus.APPROVED.getStatus());
								sameOffice.setManualApprovedCount(1);

							} else if (seedingApprovals.getStatus().toString()
									.equals(Status.AadhaarSeedStatus.REJECTED.getStatus())) {
								sameOffice.setManualRejected(Status.AadhaarSeedStatus.REJECTED.getStatus());
								sameOffice.setManualRejectedCount(1);
							}
							break;
						}
					}
				}

				else {
					if (seedingApprovals.getStatus().toString()
							.equals(Status.AadhaarSeedStatus.AUTO_APPROVED.getStatus())) {
						officeCountVo.setAutoApproved(Status.AadhaarSeedStatus.AUTO_APPROVED.getStatus());
						officeCountVo.setAutoApprovedCount(1);
						officeCountVo.setOfficeVO(officeDetails);
						officeCountVo.setOfficeName(officeDetails.getOfficeName());
					}
					if (seedingApprovals.getStatus().toString()
							.equals(Status.AadhaarSeedStatus.AUTO_REJECTED.getStatus())) {
						officeCountVo.setAutoRejected(Status.AadhaarSeedStatus.AUTO_REJECTED.getStatus());
						officeCountVo.setAutoRejectedCount(1);
						officeCountVo.setOfficeVO(officeDetails);
						officeCountVo.setOfficeName(officeDetails.getOfficeName());
					}

					if (seedingApprovals.getStatus().toString().equals(Status.AadhaarSeedStatus.APPROVED.getStatus())) {
						officeCountVo.setManualApproved(Status.AadhaarSeedStatus.APPROVED.getStatus());
						officeCountVo.setManualApprovedCount(1);
						officeCountVo.setOfficeVO(officeDetails);
						officeCountVo.setOfficeName(officeDetails.getOfficeName());
					}
					if (seedingApprovals.getStatus().toString().equals(Status.AadhaarSeedStatus.REJECTED.getStatus())) {
						officeCountVo.setManualRejected(Status.AadhaarSeedStatus.REJECTED.getStatus());
						officeCountVo.setManualRejectedCount(1);
						officeCountVo.setOfficeVO(officeDetails);
						officeCountVo.setOfficeName(officeDetails.getOfficeName());
					}

					officeWiseList.add(officeCountVo);
				}

			}
		}
		if (officeWiseList == null || officeWiseList.isEmpty()) {
			throw new BadRequestException("Aadhar Seeding Data not available");
		}
		return officeWiseList;
	}

	@Override
	public Optional<ReportsVO> getaadharSeedIngOfficeViewReport(RegReportVO regReportVO, Pageable pagable) {
		LocalDateTime fromDate = getTimewithDate(regReportVO.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(regReportVO.getToDate(), true);
		ReportsVO aadharSeedIngReportsVO = new ReportsVO();
		Criteria criteria1 = Criteria.where("issuedOfficeCode").is(regReportVO.getOfficeCode());
		Criteria criteria2 = Criteria.where("createdDate").gte(fromDate).lte(toDate);
		Criteria criteria3 = Criteria.where("status")
				.in(Arrays.asList(Status.AadhaarSeedStatus.AUTO_APPROVED, Status.AadhaarSeedStatus.AUTO_REJECTED,
						Status.AadhaarSeedStatus.APPROVED, Status.AadhaarSeedStatus.REJECTED));
		Query query = new Query();
		query.addCriteria(new Criteria().andOperator(criteria1, criteria2, criteria3));
		long count = mongoTemplate.count(query, AadhaarSeedDTO.class);
		query.limit(pagable.getPageSize());
		query.skip(pagable.getPageSize() * (pagable.getPageNumber() - 1));

		List<AadhaarSeedDTO> aadhaarSeededStatusList = mongoTemplate.find(query, AadhaarSeedDTO.class);
		if (aadhaarSeededStatusList.size() > 0 || aadhaarSeededStatusList != null) {
			List<AadhaarSeedVO> seedDetilsList = aadhaarSeedMapper.convertEntity(aadhaarSeededStatusList);
			if (seedDetilsList == null || seedDetilsList.isEmpty()) {
				throw new BadRequestException("Aadhar Seeding Data not available");
			}
			if (!seedDetilsList.isEmpty()) {
				aadharSeedIngReportsVO.setAadharSeedingDetils(seedDetilsList);
				aadharSeedIngReportsVO.setPageNo(pagable.getPageNumber());
				aadharSeedIngReportsVO.setTotalPage(aadhaarSeededStatusList.size() == 0 ? 1
						: (int) Math.ceil((double) count / (double) pagable.getPageSize()));
				return Optional.of(aadharSeedIngReportsVO);
			}
		}
		return null;

	}

	@Override
	public Optional<ReportsVO> getaadharSeedIngApprovalsForDTCReport(RegReportVO regReportVO, Pageable pagable) {
		LocalDateTime fromDate = getTimewithDate(regReportVO.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(regReportVO.getToDate(), true);
		ReportsVO aadharSeedIngReportsVO = new ReportsVO();
		// Criteria criteria1 =
		// Criteria.where("officeCode").is(regReportVO.getOfficeCode());
		Criteria criteria2 = Criteria.where("createdDate").gte(fromDate).lte(toDate);
		Criteria criteria3 = Criteria.where("status")
				.in(Arrays.asList(Status.AadhaarSeedStatus.AUTO_APPROVED, Status.AadhaarSeedStatus.AUTO_REJECTED,
						Status.AadhaarSeedStatus.APPROVED, Status.AadhaarSeedStatus.REJECTED));
		Query query = new Query();
		query.addCriteria(new Criteria().andOperator(criteria2, criteria3));
		List<AadhaarSeedDTO> aadhaarSeededStatusList = mongoTemplate.find(query, AadhaarSeedDTO.class);
		if (CollectionUtils.isNotEmpty(aadhaarSeededStatusList)) {
			aadharSeedIngReportsVO.setAadharSeedApprovalsOfficeWise(
					aadharSeedingOfficeWise(aadhaarSeededStatusList, regReportVO.getOfficeCode()));
			return Optional.of(aadharSeedIngReportsVO);
		}
		return null;
	}

	private List<AadharSeedingOfficeWiseVO> aadharSeedingOfficeWise(List<AadhaarSeedDTO> aadhaarSeededStatusList,
			String officeCode) {
		List<AadharSeedingOfficeWiseVO> officeWiseList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(aadhaarSeededStatusList)) {
			for (AadhaarSeedDTO seedingApprovals : aadhaarSeededStatusList) {
				AadharSeedingOfficeWiseVO officeCountVo = new AadharSeedingOfficeWiseVO();
				Optional<OfficeDTO> officeWithDistId = officeDAO.findByOfficeCode(officeCode);
				Optional<OfficeDTO> officeDteails = officeDAO.findByOfficeCode(seedingApprovals.getIssuedOfficeCode());
				OfficeVO officeDetails = officeMapper.convertEntity(officeDteails.get());
				if (officeDteails.isPresent() && !CollectionUtils.isEmpty(officeWiseList) && !officeWiseList.isEmpty()
						&& officeWiseList.stream().anyMatch(val -> val.getOfficeVO().getOfficeCode()
								.equalsIgnoreCase(officeDetails.getOfficeCode()))) {
					for (AadharSeedingOfficeWiseVO sameOffice : officeWiseList) {
						if (sameOffice.getOfficeVO().getOfficeCode().equalsIgnoreCase(officeDetails.getOfficeCode())) {
							if (sameOffice.getAutoApproved() != null
									&& sameOffice.getAutoApproved().equals(seedingApprovals.getStatus().getStatus())) {
								sameOffice.setAutoApprovedCount(sameOffice.getAutoApprovedCount() + 1);
							} else if (sameOffice.getAutoRejected() != null
									&& sameOffice.getAutoRejected().equals(seedingApprovals.getStatus().getStatus())) {
								sameOffice.setAutoRejectedCount(sameOffice.getAutoRejectedCount() + 1);
							} else if (sameOffice.getManualApproved() != null && sameOffice.getManualApproved()
									.equals(seedingApprovals.getStatus().getStatus())) {
								sameOffice.setManualApprovedCount(sameOffice.getManualApprovedCount() + 1);
							} else if (sameOffice.getManualRejected() != null && sameOffice.getManualRejected()
									.equals(seedingApprovals.getStatus().getStatus())) {
								sameOffice.setManualRejectedCount(sameOffice.getManualRejectedCount() + 1);
							} else if (seedingApprovals.getStatus().toString()
									.equals(Status.AadhaarSeedStatus.AUTO_APPROVED.getStatus())) {
								sameOffice.setAutoApproved(Status.AadhaarSeedStatus.AUTO_APPROVED.getStatus());
								sameOffice.setAutoApprovedCount(1);
							} else if (seedingApprovals.getStatus().toString()
									.equals(Status.AadhaarSeedStatus.AUTO_REJECTED.getStatus())) {
								sameOffice.setAutoRejected(Status.AadhaarSeedStatus.AUTO_REJECTED.getStatus());
								sameOffice.setAutoRejectedCount(1);
							} else if (seedingApprovals.getStatus().toString()
									.equals(Status.AadhaarSeedStatus.APPROVED.getStatus())) {
								sameOffice.setManualApproved(Status.AadhaarSeedStatus.APPROVED.getStatus());
								sameOffice.setManualApprovedCount(1);

							} else if (seedingApprovals.getStatus().toString()
									.equals(Status.AadhaarSeedStatus.REJECTED.getStatus())) {
								sameOffice.setManualRejected(Status.AadhaarSeedStatus.REJECTED.getStatus());
								sameOffice.setManualRejectedCount(1);
							}
							break;
						}
					}
				}

				else {
					if (seedingApprovals.getStatus().toString()
							.equals(Status.AadhaarSeedStatus.AUTO_APPROVED.getStatus())) {
						officeCountVo.setAutoApproved(Status.AadhaarSeedStatus.AUTO_APPROVED.getStatus());
						officeCountVo.setAutoApprovedCount(1);
						officeCountVo.setOfficeVO(officeDetails);
						officeCountVo.setOfficeName(officeDetails.getOfficeName());
					}
					if (seedingApprovals.getStatus().toString()
							.equals(Status.AadhaarSeedStatus.AUTO_REJECTED.getStatus())) {
						officeCountVo.setAutoRejected(Status.AadhaarSeedStatus.AUTO_REJECTED.getStatus());
						officeCountVo.setAutoRejectedCount(1);
						officeCountVo.setOfficeVO(officeDetails);
						officeCountVo.setOfficeName(officeDetails.getOfficeName());
					}

					if (seedingApprovals.getStatus().toString().equals(Status.AadhaarSeedStatus.APPROVED.getStatus())) {
						officeCountVo.setManualApproved(Status.AadhaarSeedStatus.APPROVED.getStatus());
						officeCountVo.setManualApprovedCount(1);
						officeCountVo.setOfficeVO(officeDetails);
						officeCountVo.setOfficeName(officeDetails.getOfficeName());
					}
					if (seedingApprovals.getStatus().toString().equals(Status.AadhaarSeedStatus.REJECTED.getStatus())) {
						officeCountVo.setManualRejected(Status.AadhaarSeedStatus.REJECTED.getStatus());
						officeCountVo.setManualRejectedCount(1);
						officeCountVo.setOfficeVO(officeDetails);
						officeCountVo.setOfficeName(officeDetails.getOfficeName());
					}
					if (officeWithDistId.get().getDistrict() == officeCountVo.getOfficeVO().getDistrict()) {
						officeWiseList.add(officeCountVo);
					}

				}

			}
		}
		if (officeWiseList == null || officeWiseList.isEmpty()) {
			throw new BadRequestException("Aadhar Seeding Data not available");
		}
		return officeWiseList;
	}

	@Override
	public Optional<ReportsVO> getaadharSeedIngAppllicationViewReport(RegReportVO regReportVO, Pageable pagable) {
		// TODO Auto-generated method stub
		return null;
	}

	private List<UserWiseEODCount> getListOfCounts(String role, List<String> status, List<String> userIds,
			String officeCode, LocalDateTime fromDate, LocalDateTime toDate) {

		Aggregation agg = newAggregation(
				match(Criteria.where("actionTime").gte(fromDate).lte(toDate).and("officeCode").is(officeCode)
						.and("actionStatus").in(status).and("actionRoleName").is(role).and("actionUserName")
						.in(userIds)),

				group("actionUserName").count().as("count"), project("count").and("userName").previousOperation());

		AggregationResults<UserWiseEODCount> groupResults = mongoTemplate.aggregate(agg, RegServiceReportDTO.class,
				UserWiseEODCount.class);

		if (!groupResults.getMappedResults().isEmpty()) {
			return groupResults.getMappedResults();
		}
		return Collections.EMPTY_LIST;
	}

	private ActionCountDetailsVO getEODReportBasedOnUsers(ActionCountDetailsVO actionVO, Long approvedCount,
			JwtUser jwtUser, EODReportVO eodVO, LocalDateTime fromDate, LocalDateTime toDate) {
		String officeCode = jwtUser.getOfficeCode();
		if (StringUtils.isNotBlank(eodVO.getOfficeCode())) {
			officeCode = eodVO.getOfficeCode();
		}
		List<UserDTO> users = userDAO.findByPrimaryRoleNameOrAdditionalRolesNameAndOfficeOfficeCodeNative(
				eodVO.getSelectedRole(), eodVO.getSelectedRole(), officeCode);
		if (CollectionUtils.isEmpty(users)) {
			throw new BadRequestException("Dept Users Not Available");
		}
		List<String> usersIds = users.stream().map(p -> p.getUserId()).collect(Collectors.toList());
		List<UserWiseEODCount> approvedCounts = getListOfCounts(eodVO.getSelectedRole(),
				getStatusListBasedOnRole(eodVO.getSelectedRole(), Boolean.TRUE), usersIds, officeCode, fromDate,
				toDate);
		List<UserWiseEODCount> rejecetedCounts = getListOfCounts(eodVO.getSelectedRole(),
				getStatusListBasedOnRole(eodVO.getSelectedRole(), Boolean.FALSE), usersIds, officeCode, fromDate,
				toDate);
		List<RoleBasedCounts> roleBasedCounts = new ArrayList<>();
		for (String userName : usersIds) {
			RoleBasedCounts roleCounts = new RoleBasedCounts();
			roleCounts.setUserId(userName);
			users.stream().forEach(action -> {
				if (action.getUserId().equalsIgnoreCase(userName)) {
					roleCounts.setUserName(getValue(action.getFirstName()) + " " + getValue(action.getLastName()));
				}
			});
			roleCounts.setApprovedCount(approvedCount);
			approvedCounts.stream().forEach(action -> {
				if (action.getUserName().equalsIgnoreCase(userName)) {
					roleCounts.setApprovedCount(action.getCount());
				}
			});
			roleCounts.setRejectedCount(approvedCount);
			rejecetedCounts.stream().forEach(action -> {
				if (action.getUserName().equalsIgnoreCase(userName)) {
					roleCounts.setRejectedCount(action.getCount());
				}
			});
			roleCounts.setTotalCount(roleCounts.getApprovedCount() + roleCounts.getRejectedCount());
			roleBasedCounts.add(roleCounts);
		}
		actionVO.setUsersBasedCounts(roleBasedCounts);
		return actionVO;

	}

	@Override
	public List<UserVO> getMviNameForDist(Integer districtId) {

		List<String> distinctMVI = this.getMviForDist(districtId);
		if (distinctMVI != null && !distinctMVI.isEmpty()) {
			List<UserDTO> listOfUsers = userDAO.nativefindUsers(distinctMVI);
			if (listOfUsers != null && !listOfUsers.isEmpty()) {
				List<UserVO> vo = userMapper.convertEntity(listOfUsers);
				for (UserVO userVO : vo) {
					String firstName = userVO.getFirstName() != null ? userVO.getFirstName() : "";
					String lastName = userVO.getLastName() != null ? userVO.getLastName() : "";
					String finalName = userVO.getUserId() + ":" + firstName + lastName;
					userVO.setFirstName(finalName);
				}
				return vo;
			}
		}

		return Collections.emptyList();
	}

	private List<UserWiseEODCount> getAllOfficesRoleBasedCounts(LocalDateTime fromDate, LocalDateTime toDate,
			List<String> status, List<String> role, String officeCode) {
		Aggregation agg = newAggregation(
				match(Criteria.where("actionTime").gte(fromDate).lte(toDate).and("officeCode").is(officeCode)
						.and("actionStatus").in(status).and("actionRoleName").in(role)),

				group("actionRoleName").count().as("count"), project("count").and("userName").previousOperation());

		AggregationResults<UserWiseEODCount> groupResults = mongoTemplate.aggregate(agg, RegServiceReportDTO.class,
				UserWiseEODCount.class);

		if (!groupResults.getMappedResults().isEmpty()) {
			return groupResults.getMappedResults();
		}
		return Collections.EMPTY_LIST;
	}

	private PropertiesDTO getDeptRoles() {
		Optional<PropertiesDTO> serviceroledropDown = propertiesDAO.findByEodReportsRolesStatusTrueAndRolesStatusTrue();
		checkDataPreserntOrNot(serviceroledropDown, "No Roles in active status");
		PropertiesDTO dto = serviceroledropDown.get();
		if (CollectionUtils.isEmpty(dto.getRoles())) {
			throw new BadRequestException("Roles Not Available");
		}
		return dto;
	}

	private List<String> getOfficeCodes(String officeCode) {
		List<OfficeDTO> officeList = getAllOffices(officeCode);
		return officeList.stream().map(office -> office.getOfficeCode()).collect(Collectors.toList());
	}

	private Map<String, String> getAllOfficesNames(String officeCode, Integer districtId) {
		List<OfficeDTO> officeList = new ArrayList<>();
		if (districtId != null) {
			officeList = officeDAO.findBydistrict(districtId);
		} else {
			officeList = getAllOffices(officeCode);
		}
		return officeList.stream().collect(Collectors.toMap(OfficeDTO::getOfficeCode, OfficeDTO::getOfficeName));
	}

	private List<OfficeDTO> getAllOffices(String officeCode) {
		Optional<OfficeDTO> dao = officeDAO.findByOfficeCode(officeCode);
		checkDataPreserntOrNot(dao, "office details not found");
		return officeDAO.findBydistrict(dao.get().getDistrict());
	}

	public static <T> Collector<T, ?, Optional<T>> toSingleton() {
		return Collectors.collectingAndThen(Collectors.toList(),
				list -> list.size() == 1 ? Optional.of(list.get(0)) : Optional.empty());
	}

	public ActionCountDetailsVO getDistrictWiseEodCount(LocalDateTime startDate, LocalDateTime endDate) {
		List<DistrictDTO> districtList = districtDAO.findByStateIdNative("AP");
		checkListPreserntOrNot(districtList, "District Data Not Availble");
		List<Integer> districtIdList = districtList.stream().map(p -> p.getDistrictId()).collect(Collectors.toList());
		Long approvedCount = 0L;
		List<UserWiseEODCount> approvedOffidceCount = getDistrictWiseCount(startDate, endDate,
				getStatusListBasedOnRole("ALL", Boolean.TRUE), districtIdList);
		List<UserWiseEODCount> rejectedOfficeCount = getDistrictWiseCount(startDate, endDate,
				getStatusListBasedOnRole("ALL", Boolean.FALSE), districtIdList);
		Map<Integer, String> districtNames = districtList.stream()
				.collect(Collectors.toMap(DistrictDTO::getDistrictId, DistrictDTO::getDistrictName));
		List<RoleBasedCounts> rolesList = new ArrayList<>();
		districtNames.forEach((key, value) -> {
			RoleBasedCounts counts = new RoleBasedCounts();
			counts.setDistrictId(key);
			counts.setDistrictName(value);
			counts.setApprovedCount(approvedCount);
			counts.setRejectedCount(approvedCount);
			counts.setTotalCount(approvedCount);

			Optional<UserWiseEODCount> approvedResult = approvedOffidceCount.stream()
					.filter(p -> p.getUserName().equals(key.toString())).collect(toSingleton());
			if (approvedResult.isPresent()) {
				counts.setApprovedCount(approvedResult.get().getCount());
			}
			Optional<UserWiseEODCount> rejecetedResult = rejectedOfficeCount.stream()
					.filter(p -> p.getUserName().equals(key.toString())).collect(toSingleton());
			if (rejecetedResult.isPresent()) {
				counts.setRejectedCount(rejecetedResult.get().getCount());
				counts.setTotalCount(counts.getApprovedCount() + counts.getRejectedCount());
			}
			rolesList.add(counts);
		});
		ActionCountDetailsVO actionCounts = new ActionCountDetailsVO();
		actionCounts.setUsersBasedCounts(rolesList);
		return actionCounts;

	}

	private void checkListPreserntOrNot(List<?> service, String errorMsg) {
		if (CollectionUtils.isEmpty(service)) {
			throw new BadRequestException(errorMsg);
		}
	}

	private List<UserWiseEODCount> getDistrictWiseCount(LocalDateTime fromDate, LocalDateTime toDate,
			List<String> status, List<Integer> districtId) {
		Aggregation agg = newAggregation(
				match(Criteria.where("actionTime").gte(fromDate).lte(toDate).and("actionStatus").in(status)
						.and("districtId").in(districtId)),

				group("districtId").count().as("count"), project("count").and("userName").previousOperation());

		AggregationResults<UserWiseEODCount> groupResults = mongoTemplate.aggregate(agg, RegServiceReportDTO.class,
				UserWiseEODCount.class);

		if (!groupResults.getMappedResults().isEmpty()) {
			return groupResults.getMappedResults();
		}
		return Collections.EMPTY_LIST;
	}

	public void generateExcelForRcSuspensionReports(List<RegReportVO> regReport, HttpServletResponse response) {

		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "RcSuspensionReportsList");

		String name = "RcSuspensionReportsList";
		String fileName = name + ".xlsx";
		String sheetname = "RcSuspensionReportsList";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);

		XSSFWorkbook wb = excel.renderData(prepareCellPropsForRcSuspensionReportsExcel(header, regReport), header,
				fileName, sheetname);

		settingValuesForExcel(response, sheetname, wb);

	}

	private List<List<CellProps>> prepareCellPropsForRcSuspensionReportsExcel(List<String> header,
			List<RegReportVO> resultList) {

		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (ReportVO report : resultList.stream().findFirst().get().getCovReport()) {

			List<CellProps> result = new ArrayList<CellProps>();
			for (int i = 0; i < header.size(); i++) {
				CellProps cellpro = new CellProps();
				switch (i) {
				case 0:
					cellpro.setFieldValue(String.valueOf(++slNo));
					break;
				case 1:
					cellpro.setFieldValue(resultList.stream().findFirst().get().getDistrictName());
					break;
				case 2:
					cellpro.setFieldValue(report.getActionStatus());
					break;
				case 3:
					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getCount()));
				default:
					break;
				}
				result.add(cellpro);
			}
			cell.add(result);
		}
		return cell;
	}

	public void generateExcelForRcSuspensionReportsOfficeData(List<RegReportVO> regReport,
			HttpServletResponse response) {
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "RcSuspensionReportsListOfficeData");

		String name = "RcSuspensionReportsListOfficeData";
		String fileName = name + ".xlsx";
		String sheetname = "RcSuspensionReportsListOfficeData";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);

		XSSFWorkbook wb = excel.renderData(prepareCellPropsForRcSuspensionReportsOfficeDataExcel(header, regReport),
				header, fileName, sheetname);

		settingValuesForExcel(response, sheetname, wb);
	}

	private List<List<CellProps>> prepareCellPropsForRcSuspensionReportsOfficeDataExcel(List<String> header,
			List<RegReportVO> resultList) {

		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (int k = 0; k < resultList.size(); k++) {
			for (ReportVO report : resultList.get(k).getCovReport()) {

				List<CellProps> result = new ArrayList<CellProps>();

				for (int i = 0; i < header.size(); i++) {
					CellProps cellpro = new CellProps();
					switch (i) {
					case 0:
						cellpro.setFieldValue(String.valueOf(++slNo));
						break;
					case 1:
						cellpro.setFieldValue(resultList.get(k).getOfficeName());
						break;
					case 2:
						cellpro.setFieldValue(report.getActionStatus());
						break;
					case 3:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getCount()));
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

	public void generateExcelForRcSuspensionReportsUserData(List<RegReportVO> regReport, HttpServletResponse response) {

		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "RcSuspensionReportsListUserData");

		String name = "RcSuspensionReportsListUserData";
		String fileName = name + ".xlsx";
		String sheetname = "RcSuspensionReportsListUserData";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);

		XSSFWorkbook wb = excel.renderData(prepareCellPropsForRcSuspensionReportsUserDataExcel(header, regReport),
				header, fileName, sheetname);

		settingValuesForExcel(response, sheetname, wb);

	}

	private List<List<CellProps>> prepareCellPropsForRcSuspensionReportsUserDataExcel(List<String> header,
			List<RegReportVO> resultList) {

		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<>();

		if (resultList != null && resultList.stream().findFirst().get().getRcReport() != null) {

			for (RCActionsVO report : resultList.stream().findFirst().get().getRcReport()) {
				if (report != null) {

					List<CellProps> cells = new ArrayList<>();

					for (int i = 0; i < header.size(); i++) {

						CellProps cellpro = new CellProps();

						switch (i) {
						case 0:
							cellpro.setFieldValue(String.valueOf(++slNo));
							break;
						case 1:
							cellpro.setFieldValue(report.getPrNo());
							break;
						case 2:
							cellpro.setFieldValue(report.getSuspensionDetailsVO().getOwnerName());
							break;
						case 3:
							cellpro.setFieldValue(
									DateConverters.replaceDefaults(report.getSuspensionDetailsVO().getRegValidity()));
							break;
						case 4:
							cellpro.setFieldValue(report.getSuspensionDetailsVO().getClassOfVehicle());
							break;
						case 5:
							cellpro.setFieldValue(report.getActionStatus().getStatus());
							break;
						case 6:
							cellpro.setFieldValue(
									DateConverters.replaceDefaults(report.getSuspensionDetailsVO().getFromDate()));
							break;
						case 7:
							cellpro.setFieldValue(
									DateConverters.replaceDefaults(report.getSuspensionDetailsVO().getToDate()));
							break;
						case 8:
							cellpro.setFieldValue(
									DateConverters.replaceDefaults(report.getSuspensionDetailsVO().getSuspendedBy()));
							break;
						case 9:
							cellpro.setFieldValue(report.getSuspensionDetailsVO().getReason());
							break;
						case 10:
							cellpro.setFieldValue(report.getSuspensionDetailsVO().getReferenceNumber());
							break;
						case 11:
							cellpro.setFieldValue(
									DateConverters.replaceDefaults(report.getSuspensionDetailsVO().getReferenceDate()));
							break;
						case 12:
							cellpro.setFieldValue(
									DateConverters.replaceDefaults(report.getSuspensionDetailsVO().getRevokedBy()));
							break;
						case 13:
							cellpro.setFieldValue(DateConverters
									.replaceDefaults(report.getSuspensionDetailsVO().getRevocationDate()));
						default:
							break;
						}
						cells.add(cellpro);
					}

					cell.add(cells);
				}
			}
		}
		return cell;

	}

	@Override
	public TrIssuedReportVO getDealerTrIssuedReport(LocalDate fromDate, LocalDate toDate, JwtUser jwtuser,
			Pageable page) {
		// TODO Auto-generated method stub
		if (!jwtuser.getPrimaryRole().getName().equalsIgnoreCase("DEALER")) {
			throw new BadRequestException("TR Issued Report Only Enabled for Dealers");
		}
		Pageable pageRequest = new PageRequest(page.getPageNumber(), page.getPageSize(), Sort.Direction.DESC,
				"trGeneratedDate");
		Page<TrGeneratedReportDTO> dealerTrIssuedList = trGeneratedReportDAO.findByTrGeneratedDateAndDealerId(
				paymentReportService.getTimewithDate(fromDate, Boolean.FALSE),
				paymentReportService.getTimewithDate(toDate, Boolean.TRUE), jwtuser.getUsername(), pageRequest);
		List<TrGeneratedReportDTO> finalList = new ArrayList<>();
		if (dealerTrIssuedList == null || !dealerTrIssuedList.hasContent()) {
			throw new BadRequestException("No Issued TR's Found");
		}
		finalList = dealerTrIssuedList.getContent();
		TrIssuedReportVO vo = new TrIssuedReportVO();
		vo.setPageNumber(dealerTrIssuedList.getNumber());
		vo.setTotalPageSize(dealerTrIssuedList.getTotalPages());
		vo.setVoList(trIssuedReportMapper.convertEntity(finalList));
		return vo;
	}

	@Override
	public void generateExcelForPermitReports(List<RegReportVO> paymentReportVO, HttpServletResponse response) {

		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "PermitReports");

		String name = "PermitReports";
		String fileName = name + ".xlsx";
		String sheetname = "PermitReports";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);

		XSSFWorkbook wb = excel.renderData(prepareCellPropsForPermitReportsExcel(header, paymentReportVO), header,
				fileName, sheetname);

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

	private List<List<CellProps>> prepareCellPropsForPermitReportsExcel(List<String> header,
			List<RegReportVO> resultList) {

		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(resultList)) {
			for (RegReportVO report : resultList) {

				List<CellProps> cells = new ArrayList<>();

				if (CollectionUtils.isNotEmpty(report.getCovReport())) {

					for (int i = 0; i < header.size(); i++) {

						CellProps cellpro = new CellProps();

						switch (i) {
						case 0:
							cellpro.setFieldValue(String.valueOf(++slNo));
							break;
						case 1:
							if (StringUtils.isNotEmpty(report.getOfficeName())) {
								cellpro.setFieldValue(report.getOfficeName());
							}
							break;
						case 2:
							if (CollectionUtils.isNotEmpty(report.getCovReport())
									&& report.getCovReport().stream().findFirst().get().getPermitType() != null) {
								cellpro.setFieldValue(report.getCovReport().stream().findFirst().get().getPermitType());
							}
							break;
						case 3:
							if (CollectionUtils.isNotEmpty(report.getCovReport())) {
								cellpro.setFieldValue(DateConverters
										.replaceDefaults(report.getCovReport().stream().findFirst().get().getCount()));
							}
						default:
							break;
						}
						cells.add(cellpro);
					}

				}
				cell.add(cells);
			}
		}
		return cell;
	}

	@Override
	public TrReportTotalsVO getTotalOfficeWise(LocalDate fromDate, LocalDate toDate, JwtUser jwtuser,
			String reportName) {
		List<String> officeList = getOffices(jwtuser.getOfficeCode());
		int nTotal = 0;
		int tTotal = 0;
		Long totalNQTax = 0l;
		Long totalNLTax = 0l;
		Long totalTQTax = 0l;
		Long totalTLTax = 0l;
		Long totalTax = 0l;
		TrReportTotalsVO vo = new TrReportTotalsVO();
		List<TrGeneratedReportDTO> trList = new ArrayList<>();
		if (reportName.equalsIgnoreCase("DistrictReport") || reportName.equalsIgnoreCase("DealerReport")) {
			List<TrGeneratedReportDTO> othersList = getOthersData(officeList, fromDate, toDate, null);
			if (CollectionUtils.isNotEmpty(othersList)) {
				trList.addAll(othersList);
			}
		}
		if (reportName.equalsIgnoreCase("DistrictReport")) {
			List<TrGeneratedReportDTO> districtTrList = trGeneratedReportDAO.findByTrGeneratedDateAndOfficeCodeInNative(
					paymentReportService.getTimewithDate(fromDate, Boolean.FALSE),
					paymentReportService.getTimewithDate(toDate, Boolean.TRUE), officeList);
			if (CollectionUtils.isNotEmpty(districtTrList)) {
				trList.addAll(districtTrList);
			}
		}

		Integer n = 0;
		Integer t = 0;
		Long nonTransportQTax = 0l;
		Long nonTransportLTax = 0l;
		Long transportQTax = 0l;
		Long transportLTax = 0l;
		if (CollectionUtils.isEmpty(trList)) {
			throw new BadRequestException("No Records Found");
		}
		for (TrGeneratedReportDTO action : trList) {
			if (action.getVehicleType().equalsIgnoreCase("N")) {
				n++;
				if (action.getTaxType().equalsIgnoreCase("QuarterlyTax")) {
					nonTransportQTax = nonTransportQTax + action.getTaxAmount();
				} else {
					nonTransportLTax = nonTransportLTax + action.getTaxAmount();
				}
			} else {
				t++;
				if (action.getTaxType().equalsIgnoreCase("QuarterlyTax")) {
					transportQTax = transportQTax + action.getTaxAmount();
				} else {
					transportLTax = transportLTax + action.getTaxAmount();
				}
			}
		}
		nTotal = nTotal + n;
		tTotal = tTotal + t;
		totalNQTax = totalNQTax + nonTransportQTax;
		totalNLTax = totalNLTax + nonTransportLTax;
		totalTQTax = totalTQTax + transportQTax;
		totalTLTax = totalTLTax + transportLTax;
		totalTax = totalNQTax + totalNLTax + totalTQTax + totalTLTax;
		vo.setnTotal(nTotal);
		vo.settTotal(tTotal);
		vo.setTotalNLTax(totalNLTax);
		vo.setTotalNQTax(totalNQTax);
		vo.setTotalTLTax(totalTLTax);
		vo.setTotalTQTax(totalTQTax);
		vo.setTotalLtax(totalNLTax + totalTLTax);
		vo.setTotalQTax(totalNQTax + totalTQTax);
		vo.setTotalTax(nTotal + tTotal);
		return vo;
	}

	@Override
	public void generateExcelForPermitReportsData(List<RegReportVO> paymentReportVO, HttpServletResponse response) {

		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "PermitReportsData");

		String name = "PermitReportsData";
		String fileName = name + ".xlsx";
		String sheetname = "PermitReportsData";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);

		XSSFWorkbook wb = excel.renderData(prepareCellPropsForPermitReportsDataExcel(header, paymentReportVO), header,
				fileName, sheetname);

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

	private List<List<CellProps>> prepareCellPropsForPermitReportsDataExcel(List<String> header,
			List<RegReportVO> resultList) {
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(resultList)) {
			for (RegReportVO report : resultList) {

				List<CellProps> cells = new ArrayList<>();

				for (int i = 0; i < header.size(); i++) {

					CellProps cellpro = new CellProps();

					switch (i) {
					case 0:
						cellpro.setFieldValue(String.valueOf(++slNo));
						break;
					case 1:
						cellpro.setFieldValue(report.getOfficeName());
						break;
					case 2:
						cellpro.setFieldValue(report.getPermitType());
						break;
					case 3:
						cellpro.setFieldValue(report.getCov());
						break;
					case 4:
						cellpro.setFieldValue(report.getPermitNo());
						break;
					case 5:
						cellpro.setFieldValue(report.getPrNo());
						break;
					case 6:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getValidFrom()));
						break;
					case 7:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getValidTo()));
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
	public List<StoppageReportVO> fetchVehicleStoppageData(String officeCode, RegReportVO regVO) {
		LocalDateTime fromDate = getTimewithDate(regVO.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(regVO.getToDate(), true);
		List<RegServiceDTO> regStoppageList = null;
		List<StoppageReportVO> stoppageReportVo = new ArrayList<>();
		;
		StoppageReportVO stoppageReportVO = null;
		regStoppageList = regServiceDAO
				.findByOfficeDetailsOfficeCodeAndServiceIdsAndApplicationStatusAndCreatedDateBetweenOrderByCreatedDateDesc(
						officeCode, ServiceEnum.VEHICLESTOPPAGE.getId(), StatusRegistration.INITIATED.toString(),
						fromDate, toDate);
		if (!CollectionUtils.isEmpty(regStoppageList)) {
			for (RegServiceDTO regServiceDTO : regStoppageList) {
				stoppageReportVO = new StoppageReportVO();
				stoppageReportVO.setOfficeName(regServiceDTO.getOfficeDetails().getOfficeName());
				stoppageReportVO.setVehicleNo(regServiceDTO.getPrNo());
				stoppageReportVO.setVehicleStoppageDate(regServiceDTO.getVehicleStoppageDetails().getStoppageDate());
				stoppageReportVO.setStoppageCov(regServiceDTO.getRegistrationDetails().getClassOfVehicleDesc());
				if (regServiceDTO.getRegistrationDetails().getApplicantDetails().getPresentAddress().getDistrict()
						.getDistrictName().equalsIgnoreCase(regServiceDTO.getVehicleStoppageDetails()
								.getVehicleAddressDetails().getDistrict().getDistrictName())) {
					stoppageReportVO.setIsWithinState(Boolean.FALSE);
				}
				stoppageReportVO.setStoppageAddress(
						stoppageAddress(regServiceDTO.getVehicleStoppageDetails().getVehicleAddressDetails()));
				stoppageReportVO.setStoppageReason(regServiceDTO.getVehicleStoppageDetails().getReasonForStoppage());
				stoppageReportVO.setStoppageMviName(
						mviNameByMviofficeCode(regServiceDTO.getVehicleStoppageDetails().getUserId()));
				stoppageReportVo.add(stoppageReportVO);
			}
		} else {
			throw new BadRequestException("No stopagge recored found");
		}

		return stoppageReportVo;
	}

	private String stoppageAddress(ApplicantAddressDTO stoppageAddress) {
		String stoppageAddr = StringUtils.EMPTY;
		stoppageAddr = (stoppageAddress.getDoorNo() == null ? StringUtils.EMPTY : stoppageAddress.getDoorNo()) + ","
				+ (stoppageAddress.getStreetName() == null ? StringUtils.EMPTY : stoppageAddress.getStreetName()) + "\n"
				+ (stoppageAddress.getTownOrCity() == null ? StringUtils.EMPTY : stoppageAddress.getTownOrCity()) + ","
				+ ((stoppageAddress.getDistrict() == null || stoppageAddress.getDistrict().getDistrictName() == null)
						? StringUtils.EMPTY
						: stoppageAddress.getDistrict().getDistrictName());

		return stoppageAddr;
	}

	private String mviNameByMviofficeCode(String userId) {
		MasterUsersDTO masterUsersDto = null;
		String fullName = StringUtils.EMPTY;
		masterUsersDto = masterUsersDAO.findByUserId(userId);
		if (masterUsersDto != null) {
			return fullName = (masterUsersDto.getFirstName() == null ? StringUtils.EMPTY
					: masterUsersDto.getFirstName()) + " "
					+ (masterUsersDto.getMiddleName() == null ? StringUtils.EMPTY : masterUsersDto.getMiddleName())
					+ " " + (masterUsersDto.getLastName() == null ? StringUtils.EMPTY : masterUsersDto.getLastName());
		} else {
			return fullName;
		}

	}

	private List<TrGeneratedReportDTO> getOthersData(List<String> officeCode, LocalDate fromDate, LocalDate toDate,
			List<String> vehicleTypes) {

		return trGeneratedReportDAO.findByTrGeneratedDateAndOfficeCodeNotInNative(
				paymentReportService.getTimewithDate(fromDate, Boolean.FALSE),
				paymentReportService.getTimewithDate(toDate, Boolean.TRUE), officeCode, getUserIds(officeCode));
	}

	private List<String> getOffices(String officeCode) {
		Optional<OfficeDTO> officeDetails = officeDAO.findByOfficeCode(officeCode);
		if (!officeDetails.isPresent()) {
			throw new BadRequestException("Office Details Not Found");
		}
		List<OfficeDTO> officeList = officeDAO.findBydistrictNative(officeDetails.get().getDistrict());
		if (CollectionUtils.isEmpty(officeList)) {
			throw new BadRequestException("Offices Not Found");
		}
		return officeList.stream().map(OfficeDTO::getOfficeCode).collect(Collectors.toList());
	}

	private List<String> getUserIds(List<String> officeCode) {
		List<MasterUsersDTO> userDTOList = masterUsersDAO.findByOfficeOfficeCodeInAndPrimaryRoleName(officeCode,
				RoleEnum.DEALER.getName());
		if (CollectionUtils.isEmpty(userDTOList)) {
			throw new BadRequestException("Dealer Details Not Found");
		}
		return userDTOList.stream().map(MasterUsersDTO::getUserId).collect(Collectors.toList());
	}

	@Override
	public TrIssuedReportVO getTrDetailsBasedOnVehicleType(String vehicleType, JwtUser user, LocalDate fromDate,
			LocalDate toDate, String reportFlag, Pageable page) {
		List<String> vehicleTypes = Arrays.asList("N", "T", "ALL");
		if (StringUtils.isBlank(vehicleType) || !vehicleTypes.contains(vehicleType)
				|| StringUtils.isBlank(user.getOfficeCode())) {
			throw new BadRequestException("Invalid Inputs");
		}
		vehicleTypes = new ArrayList<>();
		if (vehicleType.equalsIgnoreCase("ALL")) {
			vehicleTypes.add("N");
			vehicleTypes.add("T");
		} else {
			vehicleTypes.add(vehicleType);
		}
		List<String> officeCodes = getOffices(user.getOfficeCode());
		List<TrGeneratedReportDTO> trList = new ArrayList<>();
		Integer records = 0;
		Integer pageCount = 0;
		if (reportFlag.equalsIgnoreCase("DistrictReport") || reportFlag.equalsIgnoreCase("DealerReport")) {
			Pageable pageRequest = new PageRequest(page.getPageNumber(), page.getPageSize(), Sort.Direction.DESC,
					"trGeneratedDate");
			Page<TrGeneratedReportDTO> othersDistrictdata = trGeneratedReportDAO
					.findByTrGeneratedDateAndOfficeCodeInAndVehicleTypenInNative(
							paymentReportService.getTimewithDate(fromDate, Boolean.FALSE),
							paymentReportService.getTimewithDate(toDate, Boolean.TRUE), officeCodes, vehicleTypes,
							getUserIds(officeCodes), pageRequest);

			if (othersDistrictdata != null || othersDistrictdata.hasContent()) {
				records = othersDistrictdata.getNumber();
				pageCount = othersDistrictdata.getTotalPages();
				trList.addAll(othersDistrictdata.getContent());
			}

		}
		if (reportFlag.equalsIgnoreCase("DistrictReport")) {
			Pageable pageRequest = new PageRequest(page.getPageNumber() - 1, page.getPageSize(), Sort.Direction.DESC,
					"trGeneratedDate");
			Page<TrGeneratedReportDTO> districtList = trGeneratedReportDAO
					.findByTrGeneratedDateAndOfficeCodeInAndVehicleTypeInNative(
							paymentReportService.getTimewithDate(fromDate, Boolean.FALSE),
							paymentReportService.getTimewithDate(toDate, Boolean.TRUE), officeCodes, vehicleTypes,
							page);
			if (districtList != null || districtList.hasContent()) {
				records = records + districtList.getNumber();
				pageCount = pageCount + districtList.getTotalPages();
				trList.addAll(districtList.getContent());
			}
		}
		if (CollectionUtils.isEmpty(trList)) {
			throw new BadRequestException("No Records Found");
		}
		TrIssuedReportVO vo = new TrIssuedReportVO();
		vo.setPageNumber(records);
		vo.setTotalPageSize(pageCount);
		vo.setVoList(trIssuedReportMapper.convertEntity(trList));
		return vo;
	}

	// for Citizen Enclosures report
	@Override
	public List<CitizenEnclosuresVO> getCitizenServices(CitizenEnclosuresVO citizenEnclosuresVO) {

		List<CitizenEnclosuresVO> regServicesList = new ArrayList<>();
		List<RegServiceDTO> citizenServices = new ArrayList<>();

		if (StringUtils.isNoneBlank(citizenEnclosuresVO.getApplicationNo())) {

			citizenServices = regServiceDAO.findByApplicationNoAndApplicationStatusOrderByCreatedDateDesc(
					citizenEnclosuresVO.getApplicationNo(), StatusRegistration.APPROVED);

		}
		if (StringUtils.isNoneBlank(citizenEnclosuresVO.getPrNo())) {

			citizenServices = regServiceDAO.findByPrNoAndApplicationStatusOrderByCreatedDateDesc(
					citizenEnclosuresVO.getPrNo(), StatusRegistration.APPROVED);

		}
		if (StringUtils.isNoneBlank(citizenEnclosuresVO.getChassisNo())) {

			citizenServices = regServiceDAO
					.findByRegistrationDetailsVahanDetailsChassisNumberAndApplicationStatusOrderByCreatedDateDesc(
							citizenEnclosuresVO.getChassisNo(), StatusRegistration.APPROVED);

		}

		if (CollectionUtils.isEmpty(citizenServices)) {
			throw new BadRequestException("No data fetch for this Criteria......");
		}

		for (RegServiceDTO data : citizenServices) {

			if (!(data.getServiceType().contains(ServiceEnum.TAXATION)
					|| data.getServiceType().contains(ServiceEnum.VCR)
					|| data.getServiceType().contains(ServiceEnum.OTHERSTATESPECIALPERMIT)
					|| data.getServiceType().contains(ServiceEnum.OTHERSTATETEMPORARYPERMIT)
					|| data.getServiceType().contains(ServiceEnum.FEECORRECTION)
					|| data.getServiceType().contains(ServiceEnum.VOLUNTARYTAX)
					|| data.getServiceType().contains(ServiceEnum.RENEWALOFAUTHCARD))) {

				CitizenEnclosuresVO serviceDetails = new CitizenEnclosuresVO();

				if (StringUtils.isNoneBlank(citizenEnclosuresVO.getPrNo())) {

					if (data.getPrNo() != null) {
						serviceDetails.setPrNo(data.getPrNo());
					} else {
						serviceDetails.setPrNo(data.getTrNo());
					}
				}

				if (StringUtils.isNoneBlank(citizenEnclosuresVO.getChassisNo())) {
					serviceDetails.setChassisNo(data.getRegistrationDetails().getVahanDetails().getChassisNumber());
				}

				if (CollectionUtils.isNotEmpty(data.getServiceType())) {
					serviceDetails.setServiceType(data.getServiceType());
				}

				serviceDetails.setApplicationNo(data.getApplicationNo());
				if (data.getCreatedDate() != null)
					serviceDetails.setAppliedDate(data.getCreatedDate().toLocalDate());
				if (data.getlUpdate() != null)
					serviceDetails.setApprovedDate(data.getCreatedDate().toLocalDate());

				regServicesList.add(serviceDetails);
			}
		}

		return regServicesList;
	}

	@Override
	public List<CitizenEnclosuresVO> getCitizenEnclosures(CitizenEnclosuresVO citizenEnclosuresVO) {
		List<CitizenEnclosuresVO> images = null;
		List<CitizenEnclosuresVO> vo = new ArrayList<>();
		Optional<CitizenEnclosuresDTO> enclosuresList = citizenEnclosuresDAO
				.findByApplicationNo(citizenEnclosuresVO.getApplicationNo());
		if (!enclosuresList.isPresent()) {
			throw new BadRequestException(
					"There is no enclosures found with this Application... " + citizenEnclosuresVO.getApplicationNo());
		}
		List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = enclosuresList.get().getEnclosures();

		for (KeyValue<String, List<ImageEnclosureDTO>> dto : enclosures) {
			images = enclosureImageMapper.convertImages(dto.getValue());
			vo.addAll(images);
		}

		return vo;
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

	@Override
	public void generateExcelForRcSuspensionReport(List<RegReportVO> regReport, HttpServletResponse response,
			RegReportVO regReportVO) {
		if (regReportVO.isRcSuspensionReportsExcel()) {
			generateExcelForRcSuspensionReports(regReport, response);
		}

		if (regReportVO.isRcSuspensionReportsOfficeDataExcel()) {
			generateExcelForRcSuspensionReportsOfficeData(regReport, response);
		}

		if (regReportVO.isRcSuspensionReportsUserDataExcel()) {
			generateExcelForRcSuspensionReportsUserData(regReport, response);
		}

	}

	@Override
	public Map<String, Long> fetchEvcrReportDistrictWise(String officeCode, RegReportVO regVO) {
		OfficeDTO officeDto = null;
		List<OfficeDTO> officeDtoList = new ArrayList<>();
		Map<Integer, Long> counting = null;
		Map<String, Long> finalMap = new HashMap<>();
		List<VcrFinalServiceDTO> vcrList = finalServiceDAO.findByVcrDateOfCheckBetweenAndIseVcrNative(
				paymentReportService.getTimewithDate(regVO.getFromDate(), Boolean.FALSE),
				paymentReportService.getTimewithDate(regVO.getToDate(), Boolean.TRUE), Boolean.TRUE);
		if (!(vcrList != null)) {
			throw new BadRequestException("E-Vcr's records not found");
		}
		List<String> officeCodes = vcrList.stream().map(VcrFinalServiceDTO::getOfficeCode).collect(Collectors.toList());
		for (String officeSingle : officeCodes) {
			officeDto = getDistrictByofficeCode(officeSingle);
			officeDtoList.add(officeDto);

		}
		counting = officeDtoList.stream().collect(Collectors.groupingBy(OfficeDTO::getDistrict, Collectors.counting()));
		for (Map.Entry<Integer, Long> entry : counting.entrySet()) {
			finalMap.put(districtDAO.findBydistrictId(entry.getKey()).get().getDistrictName(), entry.getValue());
		}
		return finalMap;
	}

	@Override
	public List<VcrFinalServiceVO> fetchEvcrReportDistrictWiseList(String officeCode, RegReportVO regVO) {
		List<VcrFinalServiceDTO> evcrList = null;
		List<VcrFinalServiceDTO> updatedEvcrList = new ArrayList<>();
		Optional<RegistrationDetailsDTO> registrationDetailsOpt = null;
		RegistrationDetailsDTO registrationDetails = null;
		Optional<DistrictDTO> districtOptional = districtDAO.findByDistrictName(regVO.getDistrictName());
		List<String> officeCodes = getOfficeCodes().get(districtOptional.get().getDistrictId());
		evcrList = finalServiceDAO.nativeVcrDateOfCheckBetweenAndOfficeCodeInAndIseVcr(
				paymentReportService.getTimewithDate(regVO.getFromDate(), Boolean.FALSE),
				paymentReportService.getTimewithDate(regVO.getToDate(), Boolean.TRUE), officeCodes, Boolean.TRUE);
		for (VcrFinalServiceDTO vcrFinalServiceDTO : evcrList) {
			if (!StringUtils.isEmpty(vcrFinalServiceDTO.getRegistration().getRegNo())) {
				registrationDetailsOpt = registrationDetailDAO
						.findByPrNo(vcrFinalServiceDTO.getRegistration().getRegNo());
				if (registrationDetailsOpt != null) {
					registrationDetails = registrationDetailsOpt.get();
					vcrFinalServiceDTO.setOfficeName(registrationDetails.getOfficeDetails().getOfficeName());
				}
			}
			updatedEvcrList.add(vcrFinalServiceDTO);
		}
		return vcrFinalServiceMapper.convertEntity(updatedEvcrList);
	}

	/**
	 * Contract carriage permit report for COCT, TOVT vehicles based on district,
	 * office codes and validity To is grater than or equal to today date.
	 * 
	 * @Condition-1 :- If input is office code we are adding only office code for
	 * report.
	 * 
	 * Condition-2 :- If input is district we are getting all the offices from that
	 * district to generate report
	 * 
	 * Condition-3 :- If input is office code and it contains "ALL" we are getting
	 * all AP offices from to generate report
	 * 
	 * Condition-4 :- If all route codes is empty then we are generating the count
	 * report.
	 * 
	 * @see #groupWiseContractCarriageData(RegReportVO)
	 * 
	 * @param regVO
	 * @exception if no records found with the search criteria throwing custom
	 *               exception
	 * @return TrIssuedReportVO
	 * 
	 */
	@Override
	public TrIssuedReportVO contractCarriagePermitReport(RegReportVO regVO, Pageable page) {
		List<CitizenApplicationSearchResponceVO> resultList = new ArrayList<>();
		if (regVO.getDistrictId() != null) {
			regVO.setOfficeCodes(officeCodesMap.get(regVO.getDistrictId()));
		} else if (regVO.getDistrictId() == null && CollectionUtils.isNotEmpty(regVO.getOfficeCodes())
				&& regVO.getOfficeCodes().contains("ALL")) {
			regVO.setOfficeCodes(officeDAO
					.findByDistrictIn(districtService.findBySid(NationalityEnum.AP.getName()).stream()
							.map(val -> val.getDistrictId()).collect(Collectors.toList()))
					.stream().map(val -> val.getOfficeCode()).collect(Collectors.toList()));
		}

		if (CollectionUtils.isEmpty(regVO.getOfficeCodes())) {
			throw new BadRequestException("Required Parameters are missing");
		}
		if (CollectionUtils.isEmpty(regVO.getRouteCodes())) {
			return groupWiseContractCarriageData(regVO);
		}

		Page<PermitDetailsDTO> permitsList = permitDetailsDAO
				.findByOfficeCodeInAndClassOfVehicleInAndPermitStatusAndPermitRouteCode(regVO.getOfficeCodes(),
						Arrays.asList(ClassOfVehicleEnum.COCT.getCovCode(), ClassOfVehicleEnum.TOVT.getCovCode()),
						PermitsEnum.ACTIVE.getDescription(), regVO.getRouteCodes(), LocalDate.now(), page);
		if (!permitsList.hasContent()) {
			throw new BadRequestException("Records Not found with this office codes" + regVO.getOfficeCodes());
		}
		permitsList.getContent().forEach(val -> {
			CitizenApplicationSearchResponceVO vo = new CitizenApplicationSearchResponceVO();
			funPoint(val.getPermitNo(), vo::setPermitNo);
			funPoint(val.getPrNo(), vo::setRegistraionNumber);
			funPoint(val.getPermitType().getDescription(), vo::setPermitType);
			funPoint(val.getPermitValidityDetails().getPermitValidFrom(), vo::setPermitIssueDate);
			funPoint(val.getPermitValidityDetails().getPermitValidTo(), vo::setPermitValidUpto);

			StringBuilder sb = new StringBuilder();
			if (val.getRouteDetails().getRouteType().getRouteCode()
					.equalsIgnoreCase(PermitRouteCodeEnum.ONEDISTRICT.getCode())) {
				if (StringUtils.isNotEmpty(val.getRouteDetails().getFromDistrict())) {
					sb.append(val.getRouteDetails().getFromDistrict());
				} else if (StringUtils.isNotEmpty(val.getRouteDetails().getForwardRoute())) {
					sb.append(val.getRouteDetails().getForwardRoute());
				} else {
					sb.append("-");
				}
			} else if (val.getRouteDetails().getRouteType().getRouteCode()
					.equalsIgnoreCase(PermitRouteCodeEnum.TWODISTRICT.getCode())) {
				if (StringUtils.isNotEmpty(val.getRouteDetails().getFromDistrict())
						&& StringUtils.isNotEmpty(val.getRouteDetails().getToDistrict())) {
					sb.append(val.getRouteDetails().getFromDistrict());
					sb.append("-");
					sb.append(val.getRouteDetails().getToDistrict());
				} else if (StringUtils.isNotEmpty(val.getRouteDetails().getForwardRoute())) {
					sb.append(val.getRouteDetails().getForwardRoute());
				} else {
					sb.append("-");
				}
			} else if (val.getRouteDetails().getRouteType().getRouteCode()
					.equalsIgnoreCase(PermitRouteCodeEnum.STATE.getCode())) {
				if (val.getRouteDetails() != null && val.getRouteDetails().getPermitRouteDetails() != null
						&& StringUtils.isNotBlank(val.getRouteDetails().getPermitRouteDetails().getDescription())) {
					sb.append(val.getRouteDetails().getPermitRouteDetails().getDescription());
				} else {
					sb.append("ALL ROUTES EXCEPT PROHIBITED IN THE STATE OF ANDHRA PRADESH");
				}

			} else if (val.getRouteDetails().getRouteType().getRouteCode()
					.equalsIgnoreCase(PermitRouteCodeEnum.ALLINDIA.getCode())) {
				if (val.getRouteDetails() != null && val.getRouteDetails().getPermitRouteDetails() != null
						&& StringUtils.isNotBlank(val.getRouteDetails().getPermitRouteDetails().getDescription())) {
					sb.append(val.getRouteDetails().getPermitRouteDetails().getDescription());
				} else {
					sb.append("ALL ROUTES IN INDIA EXCEPT PROHIBITED");
				}
			}
			if (sb != null) {
				funPoint(sb.toString(), vo::setPermitRoute);
			}

			if (!ObjectUtils.isEmpty(val.getRdto())) {
				funPoint(val.getRdto().getClassOfVehicleDesc(), vo::setClassOfVehicle);
				if (!ObjectUtils.isEmpty(val.getRdto().getApplicantDetails())) {
					funPoint(val.getRdto().getApplicantDetails().getDisplayName(), vo::setOwnerName);
					funPoint(val.getRdto().getApplicantDetails().getFatherName(), vo::setFatherName);
					if (val.getRdto().getApplicantDetails().getPresentAddress() != null) {
						vo.setPresentAddress(
								addressMapper.convertEntity(val.getRdto().getApplicantDetails().getPresentAddress()));
					}
				}
				if (!ObjectUtils.isEmpty(val.getRdto().getVahanDetails())) {
					funPoint(val.getRdto().getVahanDetails().getEngineNumber(), vo::setEngineNumber);
					funPoint(val.getRdto().getVahanDetails().getChassisNumber(), vo::setChassisnumber);
					funPoint(val.getRdto().getVahanDetails().getMakersDesc(), vo::setMakerName);
					funPoint(val.getRdto().getVahanDetails().getMakersModel(), vo::setMakerClass);
					funPoint(val.getRdto().getVahanDetails().getSeatingCapacity(), vo::setSeatingCapacity);
				}
				if (!ObjectUtils.isEmpty(val.getRdto().getOfficeDetails())) {
					funPoint(val.getRdto().getOfficeDetails().getOfficeName(), vo::setOfficeName);
				}
			}
			Optional<FcDetailsDTO> fcDetailsOpt = fcDetailsDAO
					.findByStatusIsTrueAndPrNoOrderByCreatedDateDesc(val.getPrNo());
			if (fcDetailsOpt.isPresent()) {
				funPoint(fcDetailsOpt.get().getFcIssuedDate(), vo::setFcValidFrom);
				funPoint(fcDetailsOpt.get().getFcValidUpto(), vo::setFcValidUpto);
			}
			Optional<TaxDetailsDTO> taxDTO = registrationService.getLatestTaxTransaction(val.getPrNo());
			if (taxDTO.isPresent()) {
				funPoint(taxDTO.get().getTaxPaidDate(), vo::setTaxPaidDate);
				funPoint(taxDTO.get().getTaxPeriodEnd(), vo::setTaxValidUpto);
			}
			resultList.add(vo);
		});
		TrIssuedReportVO vo = new TrIssuedReportVO();
		vo.setPageNumber(permitsList.getNumber());
		vo.setTotalPageSize(permitsList.getTotalPages());
		vo.setResultList(resultList);

		return vo;
	}

	/**
	 * This method is used to do the grouping of contract carriage permits of COCT,
	 * TOVT based on their route code
	 * 
	 * @param regVO
	 * @throws if no records found with the search criteria throwing custom
	 *            exception
	 * @return TrIssuedReportVO
	 * 
	 */
	private TrIssuedReportVO groupWiseContractCarriageData(RegReportVO regVO) {
		TrIssuedReportVO reslutVo = new TrIssuedReportVO();
		Aggregation agg = newAggregation(
				match(Criteria.where("permitValidityDetails.permitValidTo").gte(LocalDate.now())
						.and("rdto.classOfVehicle").in(Arrays.asList("TOVT", "COCT"))
						.and("rdto.officeDetails.officeCode").in(regVO.getOfficeCodes()).and("permitStatus")
						.in(PermitsEnum.ACTIVE.getDescription()).and("routeDetails.routeType.routeCode")
						.in(PermitsEnum.getAllRouteCode())),
				group("routeDetails.routeType.routeCode").count().as("count"),
				project("count").and("permitType").previousOperation()

		);
		AggregationResults<ReportVO> groupResults = mongoTemplate.aggregate(agg, PermitDetailsDTO.class,
				ReportVO.class);
		List<ReportVO> result = groupResults.getMappedResults();
		if (CollectionUtils.isEmpty(result)) {
			throw new BadRequestException("Records Not Found with this office codes " + regVO.getOfficeCodes());
		}
		List<CitizenApplicationSearchResponceVO> resultList = new ArrayList<>();
		result.stream().forEach(val -> {
			CitizenApplicationSearchResponceVO vo = new CitizenApplicationSearchResponceVO();
			if (StringUtils.isNotBlank(val.getPermitType())) {
				vo.setPermitType(PermitsEnum.getRouteDesriptionWithCode(val.getPermitType()).toUpperCase());
				vo.setRouteCode(val.getPermitType());
				vo.setCount(val.getCount());
				resultList.add(vo);
			}
		});
		reslutVo.setTotal(resultList.stream().collect(Collectors.summingDouble(val -> val.getCount())));
		resultList.sort((r1, r2) -> r1.getRouteCode().compareTo(r2.getRouteCode()));
		reslutVo.setResultList(resultList);
		return reslutVo;
	}

	public <T> void funPoint(T value, Consumer<T> consumer) {
		if (value != null) {
			consumer.accept(value);
		}
	}

	@Override
	public CitizenEnclosuresVOList getCitizenServicesForQueryScreen(CitizenEnclosuresVO citizenEnclosuresVO) {

		List<CitizenEnclosuresVO> regServicesList = new ArrayList<>();
		List<RegServiceDTO> citizenServices = new ArrayList<>();
		CitizenEnclosuresVOList citizenEnclosuresVOList = new CitizenEnclosuresVOList();
		Optional<StagingRegistrationDetailsDTO> staging = Optional.empty();
		List<CitizenEnclosuresVO> forStaging = null;
		AadhaarSeedDTO aadhaarSeedDTO = null;
		RegistrationDetailsDTO regDtlDTO = null;
		CitizenEnclosuresVO vo = new CitizenEnclosuresVO();
		List<AadhaarSeedStatus> status = new ArrayList<>();
		status.add(AadhaarSeedStatus.AUTO_APPROVED);
		status.add(AadhaarSeedStatus.APPROVED);
		Sort sort = new Sort(Sort.Direction.DESC, "createdDate");

		if (StringUtils.isNoneBlank(citizenEnclosuresVO.getApplicationNo())) {
			staging = stagingRegistrationDetailsDAO
					.findByApplicationNo(citizenEnclosuresVO.getApplicationNo().toUpperCase());
		}

		if (StringUtils.isNoneBlank(citizenEnclosuresVO.getChassisNo())) {
			staging = stagingRegistrationDetailsDAO
					.findByVahanDetailsChassisNumber(citizenEnclosuresVO.getChassisNo().toUpperCase());
		}

		if (StringUtils.isNoneBlank(citizenEnclosuresVO.getTrNo())) {
			staging = stagingRegistrationDetailsDAO.findByTrNo(citizenEnclosuresVO.getTrNo().toUpperCase());
		}
		if (!staging.isPresent()) {
			if (StringUtils.isNoneBlank(citizenEnclosuresVO.getApplicationNo())) {

				citizenServices = regServiceDAO
						.findByApplicationNoIn(citizenEnclosuresVO.getApplicationNo().toUpperCase());
			}
			if (StringUtils.isNoneBlank(citizenEnclosuresVO.getPrNo())) {

				citizenServices = regServiceDAO
						.findByPrNoOrderByCreatedDateDescNative(citizenEnclosuresVO.getPrNo().toUpperCase(), sort);

			}

			if (StringUtils.isNoneBlank(citizenEnclosuresVO.getChassisNo())) {

				citizenServices = regServiceDAO
						.findByRegistrationDetailsVahanDetailsChassisNumberOrderByCreatedDateDesc(
								citizenEnclosuresVO.getChassisNo().toUpperCase());
			}

			/*
			 * for (RegServiceDTO vo : citizenServices) { if (vo.getPrNo() != null) {
			 * aadhaarSeedDTO = aadhaarSeedDAO.findByStatusInAndPrNo(status,
			 * vo.getPrNo()).get();
			 * 
			 * } if (aadhaarSeedDTO != null ) {
			 * citizenEnclosuresVOList.setAadharApprovedDate(aadhaarSeedDTO.getCreatedDate()
			 * .toLocalDate()); citizenEnclosuresVOList.setAdharSeddingStatus(Boolean.TRUE);
			 * 
			 * } else
			 * if(vo.getServiceIds().contains(ServiceEnum.DATAENTRY.getId())&&aadhaarSeedDTO
			 * == null ) {
			 * citizenEnclosuresVOList.setAadharApprovedDate(vo.getlUpdate().toLocalDate());
			 * citizenEnclosuresVOList.setAdharSeddingStatus(Boolean.TRUE); } else
			 * if(vo.getPrNo()!=null) { RegistrationDetailsDTO registrationDetailsDTO =
			 * registrationDetailDAO .findByPrNoOrderByLUpdateDesc(vo.getPrNo()).get(); if
			 * (registrationDetailsDTO.getApplicantDetails().getIsAadhaarValidated()) {
			 * citizenEnclosuresVOList
			 * .setAadharApprovedDate(registrationDetailsDTO.getlUpdate().toLocalDate());
			 * 
			 * citizenEnclosuresVOList.setAdharSeddingStatus(
			 * registrationDetailsDTO.getApplicantDetails().getIsAadhaarValidated()); }else
			 * { citizenEnclosuresVOList.setAdharSeddingStatus(Boolean.FALSE); } }
			 * 
			 * break; }
			 */
			if (citizenEnclosuresVO.getPrNo() != null && !StringUtils.isEmpty(citizenEnclosuresVO.getPrNo())) {
				RegistrationDetailsDTO registDetailsDTO = registrationDetailDAO
						.findByPrNoOrderByLUpdateDesc(citizenEnclosuresVO.getPrNo()).get();
				regDtlDTO = registrationDetailDAO.findByVahanDetailsChassisNumberOrderByLUpdateDesc(
						registDetailsDTO.getVahanDetails().getChassisNumber()).get();
			}
			if (citizenEnclosuresVO.getChassisNo() != null
					&& !StringUtils.isEmpty(citizenEnclosuresVO.getChassisNo())) {
				regDtlDTO = registrationDetailDAO
						.findByVahanDetailsChassisNumberOrderByLUpdateDesc(citizenEnclosuresVO.getChassisNo()).get();
			}
			if (regDtlDTO != null && regDtlDTO.getApplicantDetails().getIsAadhaarValidated()) {
				citizenEnclosuresVOList.setAdharSeddingStatus(regDtlDTO.getApplicantDetails().getIsAadhaarValidated());
			} else {
				citizenEnclosuresVOList.setAdharSeddingStatus(Boolean.FALSE);
			}
			vo.setPrNo(regDtlDTO.getPrNo());
			vo.setTrNo(regDtlDTO.getTrNo());
			vo.setApplStatus(regDtlDTO.getApplicationStatus());
			vo.setApplicationNo(regDtlDTO.getApplicationNo());
			regServicesList.add(vo);

			if (CollectionUtils.isEmpty(citizenServices) && CollectionUtils.isEmpty(regServicesList)) {
				throw new BadRequestException("No Records Found");
			}
			if (!CollectionUtils.isEmpty(citizenServices)) {
				regServicesList = regServiceMapper.citizenServicesList(citizenServices);
			}

			citizenEnclosuresVOList.setCitizenEnclosuresVOList(regServicesList);

			return citizenEnclosuresVOList;
		} else {

			forStaging = stagingDetailsMapper.convertDetails(staging.get());
			for (CitizenEnclosuresVO citivo : forStaging) {
//				citizenEnclosuresVOList.setAadharApprovedDate(vo.getAppliedDate());
				citizenEnclosuresVOList.setAdharSeddingStatus(Boolean.TRUE);

				break;
			}
			citizenEnclosuresVOList.setCitizenEnclosuresVOList(forStaging);
		}
		return citizenEnclosuresVOList;
	}

	@Override
	public void generateExcelForTrDetails(String vehicleType, JwtUser userDetails, LocalDate fromDate, LocalDate toDate,
			HttpServletResponse response, String reportFlag) {
		Map<String, List<String>> map = getOfficeAndVehicleTypes(vehicleType, userDetails);
		List<TrGeneratedReportDTO> voList = getTrDataList(reportFlag, fromDate, toDate, map.get("officeCodes"),
				map.get("vehicleTypes"));

		List<String> header = getPropDTO("trDetailsReport").getHeaders().stream().map(ExcelHeaders::getHeaderName)
				.collect(Collectors.toList());

		ExcelService excel = new ExcelServiceImpl();

		String name = "TrDetailsReport_" + vehicleType;
		String fileName = name + ".xlsx";
		String sheetname = "TrDetailsReport";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);

		XSSFWorkbook wb = null;
		PropertiesDTO dto = null;

		wb = excel.renderData(prepareCellPropsForTrDetailsReport(header, voList), header, fileName, sheetname);

		XSSFSheet sheet = wb.getSheet(sheetname);
		if (sheet != null) {
			sheet.setDefaultColumnWidth(40);
		}

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

	private List<List<CellProps>> prepareCellPropsForTrDetailsReport(List<String> header,
			List<TrGeneratedReportDTO> voList) {
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(voList)) {
			for (TrGeneratedReportDTO report : voList) {

				List<CellProps> cells = new ArrayList<>();

				for (int i = 0; i < header.size(); i++) {

					CellProps cellpro = new CellProps();
					switch (i) {
					case 0:
						cellpro.setFieldValue(String.valueOf(++slNo));
						break;
					case 1:
						cellpro.setFieldValue(
								DateConverters.replaceDefaults(report.getTrGeneratedDate().toLocalDate()));
						break;
					case 2:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getTrNo()));
						break;
					case 3:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getApplicantDisplayName()));
						break;
					case 4:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getClassOfVehicleDesc()));
						break;
					case 5:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getMakersModel()));
						break;
					case 6:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getChassisNumber()));
						break;
					case 7:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getEngineNumber()));
						break;
					case 8:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getManufacturedMonthYear()));
						break;
					case 9:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getInvoiceNo()));
						break;
					case 10:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getInvoiceDate()));
						break;
					case 11:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getInvoiceValue()));
						break;
					case 12:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getTaxType()));
						break;
					case 13:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getTaxAmount()));
						break;
					case 14:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getOfficeName()));
						break;
					case 15:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getDealerId()));
						break;
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

	private Map<String, List<String>> getOfficeAndVehicleTypes(String vehicleType, JwtUser user) {
		List<String> vehicleTypes = Arrays.asList("N", "T", "ALL");
		if (StringUtils.isBlank(vehicleType) || !vehicleTypes.contains(vehicleType)
				|| StringUtils.isBlank(user.getOfficeCode())) {
			throw new BadRequestException("Invalid Inputs");
		}
		vehicleTypes = new ArrayList<>();
		if (vehicleType.equalsIgnoreCase("ALL")) {
			vehicleTypes.add("N");
			vehicleTypes.add("T");
		} else {
			vehicleTypes.add(vehicleType);
		}
		List<String> officeCodes = getOffices(user.getOfficeCode());
		Map<String, List<String>> map = new HashMap<>();
		map.put("vehicleTypes", vehicleTypes);
		map.put("officeCodes", officeCodes);
		return map;
	}

	private List<String> getOffices1(String officeCode) {
		Optional<OfficeDTO> officeDetails = officeDAO.findByOfficeCode(officeCode);
		if (!officeDetails.isPresent()) {
			throw new BadRequestException("Office Details Not Found");
		}
		List<OfficeDTO> officeList = officeDAO.findBydistrictNative(officeDetails.get().getDistrict());
		if (CollectionUtils.isEmpty(officeList)) {
			throw new BadRequestException("Offices Not Found");
		}
		return officeList.stream().map(OfficeDTO::getOfficeCode).collect(Collectors.toList());
	}

	private List<TrGeneratedReportDTO> getTrDataList(String reportFlag, LocalDate fromDate, LocalDate toDate,
			List<String> officeCodes, List<String> vehicleTypes) {
		List<TrGeneratedReportDTO> trList = new ArrayList<>();
		if (reportFlag.equalsIgnoreCase("DistrictReport") || reportFlag.equalsIgnoreCase("DealerReport")) {
			List<TrGeneratedReportDTO> othersDistrictdata = trGeneratedReportDAO
					.findByTrGeneratedDateAndOfficeCodeInAndVehicleTypenInNativeList(
							paymentReportService.getTimewithDate(fromDate, Boolean.FALSE),
							paymentReportService.getTimewithDate(toDate, Boolean.TRUE), officeCodes, vehicleTypes,
							getUserIds(officeCodes));

			if (CollectionUtils.isNotEmpty(othersDistrictdata)) {
				trList.addAll(othersDistrictdata);
			}

		}
		if (reportFlag.equalsIgnoreCase("DistrictReport")) {
			List<TrGeneratedReportDTO> districtList = trGeneratedReportDAO
					.findByTrGeneratedDateAndOfficeCodeInAndVehicleTypeInNativeList(
							paymentReportService.getTimewithDate(fromDate, Boolean.FALSE),
							paymentReportService.getTimewithDate(toDate, Boolean.TRUE), officeCodes, vehicleTypes);
			if (CollectionUtils.isNotEmpty(districtList)) {
				trList.addAll(districtList);
			}
		}
		if (CollectionUtils.isEmpty(trList)) {
			throw new BadRequestException("No Records Found");
		}
		return trList;
	}

	private PropertiesDTO getPropDTO(String name) {
		Optional<PropertiesDTO> dto = propertiesDAO.findByReportName(name);
		if (!dto.isPresent()) {
			throw new BadRequestException("Report Excel Headers Not Available");
		}
		return dto.get();
	}

	private List<String> getUserIds1(List<String> officeCode) {
		List<MasterUsersDTO> userDTOList = masterUsersDAO.findByOfficeOfficeCodeInAndPrimaryRoleName(officeCode,
				RoleEnum.DEALER.getName());
		if (CollectionUtils.isEmpty(userDTOList)) {
			throw new BadRequestException("Dealer Details Not Found");
		}
		return userDTOList.stream().map(MasterUsersDTO::getUserId).collect(Collectors.toList());
	}

	@Override
	public List<FreshRCReportVO> getFreshRCReportVO(String officeCode, RegReportVO regVO) {
		LocalDateTime fromDate = getTimewithDate(regVO.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(regVO.getToDate(), true);
		List<FreshRCReportVO> freshRCReport = new ArrayList<>();
		List<RegServiceDTO> list = null;
		if (StringUtils.isNotEmpty(regVO.getFrcReport())) {
			if (regVO.getFrcReport().equals(StatusRegistration.PENDING.getDescription())) {
				list = regServiceDAO
						.findByOfficeDetailsOfficeCodeAndServiceIdsAndApplicationStatusInAndCreatedDateBetweenOrderByCreatedDateDesc(
								officeCode, ServiceEnum.RCFORFINANCE.getId(),
								Arrays.asList(StatusRegistration.PAYMENTDONE.toString(),
										StatusRegistration.REJECTED.toString(),
										StatusRegistration.AOAPPROVED.toString(),
										StatusRegistration.AOREJECTED.toString(),
										StatusRegistration.RTOAPPROVED.toString(),
										StatusRegistration.RTOREJECTED.toString(),
										StatusRegistration.MVIAPPROVED.toString(),
										StatusRegistration.MVIREJECTED.toString(),
										StatusRegistration.FRESHRCREJECTED.toString()),
								fromDate, toDate);
			} else {
				list = regServiceDAO
						.findByOfficeDetailsOfficeCodeAndServiceIdsAndApplicationStatusInAndCreatedDateBetweenOrderByCreatedDateDesc(
								officeCode, ServiceEnum.RCFORFINANCE.getId(),
								Arrays.asList(StatusRegistration.APPROVED.toString()), fromDate, toDate);
			}
		}
		if (CollectionUtils.isNotEmpty(list)) {
			list.stream().forEach(data -> {
				freshRCReport.add(freshRCMapper.setDtoToVoForFrcReport(data));
			});
		}

		return freshRCReport;
	}

	private List<NOCDetailsVO> getNocDistBasedCovCountForLoginUser(RegReportVO regReportVO, JwtUser jwtUser) {
		List<NOCDetailsVO> nocDetails = new ArrayList<>();
		List<String> officeCodes = Arrays.asList(jwtUser.getOfficeCode());
		if (CollectionUtils.isEmpty(officeCodes)) {
			throw new BadRequestException("No office found for District [{" + regReportVO.getDistrictId() + "}]");
		}
		List<String> roles = Arrays.asList("AO", "RTO");
		List<String> vehicleType = getVehicleType(regReportVO);
		List<String> users = new ArrayList<>();
		users.add(jwtUser.getId());

		Optional<UserDTO> user = userDAO.findByUserId(jwtUser.getId());

		if (user.isPresent() && user.get().isParent()) {
			List<UserDTO> chaildUsersList = userDAO.findByParentId(jwtUser.getId());
			if (CollectionUtils.isNotEmpty(chaildUsersList)) {
				users.addAll(chaildUsersList.stream().map(val -> val.getUserId()).collect(Collectors.toList()));
			}
		}
		officeCodes.stream().forEach(office -> {
			NOCDetailsVO nocDetailsVO = new NOCDetailsVO();
			List<ReportVO> result = doNocAggregation(regReportVO, vehicleType, ReportConstants.OFFICE_CODE, office,
					roles);
			nocDetailsVO.setCovReport(result);
			nocDetailsVO.setOfficeCode(office);
			nocDetails.add(nocDetailsVO);
		});
		return nocDetails;
	}

	public RegReportVO setOsNocReport(RegReportVO RegReportVO, Map<String, Long> map) {
		List<ReportVO> covCountList = new ArrayList<>();
		map.keySet().stream().forEach(key -> {
			ReportVO reportVO = new ReportVO();
			reportVO.setCov(key);
			reportVO.setCovCount(map.get(key));
			covCountList.add(reportVO);
		});
		RegReportVO.setCovReport(covCountList);
		return RegReportVO;
	}

	public List<ReportVO> doNocAggregation(RegReportVO regReportVO, List<String> vehicleType, String type, String value,
			List<String> roles) {
		Aggregation agg = newAggregation(
				match(Criteria.where("actionDetails")
						.elemMatch(Criteria.where("status").is(StatusRegistration.APPROVED.getDescription()).and("role")
								.in(roles).and("lUpdate").gte(regReportVO.getFromDate()).lte(regReportVO.getToDate()))
						.and(type).is(value).and("registrationDetails.vehicleType").in(vehicleType).and("serviceType")
						.is(ServiceEnum.ISSUEOFNOC)),
				group(ReportConstants.REG_CLASS_OF_VEH_DESC).count().as("covCount"),
				project("covCount").and("cov").previousOperation()

		);

		AggregationResults<ReportVO> groupResults = mongoTemplate.aggregate(agg, RegServiceDTO.class, ReportVO.class);
		List<ReportVO> result = groupResults.getMappedResults();
		return result;
	}

	@Override
	public RegReportVO issueOfNocDistrictWiseList(JwtUser jwtUser, RegReportVO regVO) {
		List<NOCDetailsVO> osNocReport = new ArrayList<>();
		List<ReportVO> covReport = new ArrayList<>();
		if (StringUtils.isNotEmpty(jwtUser.getOfficeCode())) {
			osNocReport = getNocDistBasedCovCountForLoginUser(regVO, jwtUser);
			osNocReport.stream().forEach(val -> {
				covReport.addAll(val.getCovReport());
			});
		} else {
			throw new BadRequestException("No office found for District [{" + jwtUser.getOfficeCode() + "}]");
		}
		Map<String, Long> covCountMap = covReport.stream().filter(val -> val.getCov() != null)
				.collect(Collectors.groupingBy(ReportVO::getCov, Collectors.summingLong(ReportVO::getCovCount)));
		regVO.setNocDetailsVO(osNocReport);
		RegReportVO regReportVO = setOsNocReport(regVO, covCountMap);
		return regReportVO;

	}

	@Override
	public List<RegReportVO> fetchIssueOfNocDetails(JwtUser jwtUser, RegReportVO regVO) {
		LocalDate fromDate = regVO.getFromDate();
		LocalDate toDate = regVO.getToDate();
		List<String> roles = Arrays.asList("AO", "RTO");
		List<String> vehicleType = getVehicleType(regVO);
		MasterCovDTO covCode = masterCovDAO.findByCovdescription(regVO.getCov());
		String cov = covCode.getCovcode();
		if (cov != null) {
			Criteria criteria1 = Criteria.where("officeDetails.officeCode").is(jwtUser.getOfficeCode());
			Criteria criteria2 = Criteria.where("registrationDetails.vehicleDetails.classOfVehicle").is(cov);
			Criteria criteria3 = Criteria.where("actionDetails")
					.elemMatch(Criteria.where("status").is(StatusRegistration.APPROVED.getDescription()).and("role")
							.in(roles).and("lUpdate").gte(fromDate).lte(toDate))
					.and("registrationDetails.vehicleType").in(vehicleType).and("serviceType")
					.is(ServiceEnum.ISSUEOFNOC);
			Query query = new Query();
			query.addCriteria(new Criteria().andOperator(criteria1, criteria2, criteria3));
			List<RegServiceDTO> regDetails = mongoTemplate.find(query, RegServiceDTO.class);
			List<RegReportVO> regDetailsVO = new ArrayList<>();
			if (CollectionUtils.isNotEmpty(regDetails)) {
				regDetailsVO = regServiceMapper.convertNocEntityLimitedList(regDetails);
				return regDetailsVO;
			}
		}

		return Collections.emptyList();
	}

	@Override
	public RegReportVO getDistrictwiseDealerOrFinancierDetails(JwtUser jwtUser, RegReportVO regVO, Pageable page) {
		List<UserVO> usersList = new ArrayList<>();
		Page<UserDTO> masterUsersList = userDAO.findByOfficeOfficeCodeAndPrimaryRoleName(jwtUser.getOfficeCode(),
				regVO.getPrimaryRoleName(), page);
		if (!masterUsersList.hasContent()) {
			throw new BadRequestException("Records Not found with this office code" + regVO.getOfficeCodes());
		}
		usersList = userMapper.convertUsersListToVo(masterUsersList.getContent());

		if (regVO.getPrimaryRoleName().equalsIgnoreCase("DEALER")) {
			convertUsersListToVo(usersList);
		}

		RegReportVO regReportVo = new RegReportVO();
		regReportVo.setPageNo(masterUsersList.getNumber());
		regReportVo.setTotalPage(masterUsersList.getTotalPages());
		regReportVo.setResultList(usersList);
		return regReportVo;
	}

	public List<UserVO> convertUsersListToVo(List<UserVO> usersList) {
		return usersList.stream().map(e -> dealerDetailsWithCovToDisplay(e)).collect(Collectors.toList());
	}

	private UserVO dealerDetailsWithCovToDisplay(UserVO userVO) {
		List<DealerCovDTO> covDetails = dealerCovDAO.findByRIdAndStatusTrue(userVO.getRid());
		List<String> covCodes = new ArrayList<>();
		List<String> covCodesDescription = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(covDetails)) {
			covCodes = covDetails.stream().map(val -> val.getCovId()).collect(Collectors.toList());
		}
		if (CollectionUtils.isNotEmpty(covCodes)) {
			List<MasterCovDTO> masterCovs = masterCovDAO.findByCovcodeInAndDealerCovTrue(covCodes);
			covCodesDescription = masterCovs.stream().map(val -> val.getCovdescription()).collect(Collectors.toList());
		}
		if (CollectionUtils.isNotEmpty(covCodesDescription)) {
			userVO.setClassOfVehicles(covCodesDescription);
		}
		return userVO;
	}

	@Override
	public Object getFitnessDetails(FitnessReportVO fitnessReportVO) {

		List<FitnessReportVO> fcDeatils = new ArrayList<>();
		List<FcDetailsDTO> fcRecords = null;

		if (StringUtils.isNoneBlank(fitnessReportVO.getPrNo().toUpperCase())) {
			fcRecords = fcDetailsDAO.findByPrNoInOrderByCreatedDateDesc(fitnessReportVO.getPrNo().toUpperCase());
		}

		if (StringUtils.isNoneBlank(fitnessReportVO.getChassisNo())) {
			fcRecords = fcDetailsDAO
					.findByChassisNoOrderByCreatedDateDesc(fitnessReportVO.getChassisNo().toUpperCase());
		}

		if (fcRecords.isEmpty()) {
			throw new BadRequestException("No records found ");
		}

		for (FcDetailsDTO data : fcRecords) {
			FitnessReportVO vo = new FitnessReportVO();
			if (data.getPrNo() != null)
				vo.setPrNo(data.getPrNo());
			if (data.getChassisNo() != null)
				vo.setChassisNo(data.getChassisNo());
			if (data.getFcIssuedDate() != null)
				vo.setApprovedDate(data.getFcIssuedDate().toLocalDate());
			if (data.getClassOfVehicle() != null) {
				ClassOfVehiclesDTO masterCov = classOfVehiclesDAO.findByCovcode(data.getClassOfVehicle());
				vo.setClassOfVehicle(masterCov.getCovdescription());
			}
			if (data.getFcNumber() != null)
				vo.setFcNumber(data.getFcNumber());
			if (data.getFcValidUpto() != null)
				vo.setFcValidUpto(data.getFcValidUpto());
			if (data.getFcvalidfrom() != null)
				vo.setFcValidFrom(data.getFcvalidfrom());
			else
				vo.setFcValidFrom(data.getInspectedDate().toLocalDate());
			if (data.getOfficeName() != null)
				vo.setOfficeName(data.getOfficeName());
			if (data.isStatus())
				vo.setStatus("Active");
			if (!data.isStatus())
				vo.setStatus("Inactive");
			if (data.getInspectedMviName() != null)
				vo.setApprovedBy(data.getInspectedMviName());
			fcDeatils.add(vo);
		}

		return fcDeatils;
	}

	@Override
	public Object getDetailsForMaining(String prNo) {

		RegDtlsForMiningVO regDtlsForMiningVO = new RegDtlsForMiningVO();

		Optional<FcDetailsDTO> fcDetails = Optional.empty();
		Optional<PermitDetailsDTO> permitDetails = Optional.empty();
		Optional<TaxDetailsDTO> taxDetails = Optional.empty();

		Optional<RegistrationDetailsDTO> regDetails = registrationDetailDAO.findByPrNoOrderByCreatedDateDesc(prNo);
		if (!regDetails.isPresent()) {
			throw new BadRequestException("No record found");
		}
		fcDetails = fcDetailsDAO.findByStatusIsTrueAndPrNoOrderByCreatedDateDesc(prNo);
		if (!fcDetails.isPresent()) {
			FcDetailsDTO value = new FcDetailsDTO();
			// value.add();
			fcDetails = Optional.ofNullable(value);
		}

		permitDetails = permitDetailsDAO.findTopByPrNoInAndPermitStatusOrderByCreatedDateDesc(Arrays.asList(prNo),
				"Active");
		if (!permitDetails.isPresent()) {
			PermitDetailsDTO permitDetailsDTO = new PermitDetailsDTO();
			permitDetails = Optional.ofNullable(permitDetailsDTO);
		}
		taxDetails = taxDetailsDAO.findByApplicationNoOrderByCreatedDateDesc(regDetails.get().getApplicationNo());
		if (!taxDetails.isPresent()) {
			TaxDetailsDTO taxDetailsDTO = new TaxDetailsDTO();

			taxDetails = Optional.ofNullable(taxDetailsDTO);
		}

		regDtlsForMiningVO = registrationDetailsMapper.convertRegDetailsmaining(permitDetails.get(), regDetails.get(),
				fcDetails.get(), taxDetails.get());

		return regDtlsForMiningVO;
	}

	@Override
	public Object getPermitHistory(PermitHistoryDeatilsVO permitHistoryDeatilsVO) {

		// List<PermitDetailsDTO> permitData = null;
		PermitHistoryDeatilsVO PermitHistoryDeatilsVO = null;
		List<PermitHistoryDeatilsVO> primarypermitDatalist = new ArrayList<>();

		List<PermitHistoryDeatilsVO> tempararyPermitDatalist = new ArrayList<>();
		PermitHistoryVO permitHistoryVO = new PermitHistoryVO();

		List<PermitDetailsDTO> permitDetails = null;

		if (StringUtils.isNoneBlank(permitHistoryDeatilsVO.getPrNo())) {

			permitDetails = permitDetailsDAO
					.findByPrNoInOrderByCreatedDateDesc(Arrays.asList(permitHistoryDeatilsVO.getPrNo().toUpperCase()));

			for (PermitDetailsDTO permitDetailsDTO : permitDetails) {

				if (permitDetailsDTO.getPermitClass().getDescription()
						.equalsIgnoreCase(PermitType.PRIMARY.getDescription())) {

					PermitHistoryDeatilsVO = regServiceMapper.primarypermitDetails(permitDetailsDTO);
					primarypermitDatalist.add(PermitHistoryDeatilsVO);
					permitHistoryVO.setPrimaryPermitVO(primarypermitDatalist);
				}

				if (permitDetailsDTO.getPermitClass().getDescription()
						.equalsIgnoreCase(PermitType.TEMPORARY.getDescription())) {

					PermitHistoryDeatilsVO = regServiceMapper.primarypermitDetails(permitDetailsDTO);
					tempararyPermitDatalist.add(PermitHistoryDeatilsVO);
					permitHistoryVO.setTempararyPermitVO(tempararyPermitDatalist);

				}

			}

		}

		if (StringUtils.isNoneBlank(permitHistoryDeatilsVO.getPermitNumber())) {

			permitDetails = permitDetailsDAO
					.findByPermitNoOrderByCreatedDateDesc(permitHistoryDeatilsVO.getPermitNumber().toUpperCase());

			for (PermitDetailsDTO permitDetailsDTO : permitDetails) {

				if (permitDetailsDTO.getPermitClass().getDescription()
						.equalsIgnoreCase(PermitType.PRIMARY.getDescription())) {

					PermitHistoryDeatilsVO = regServiceMapper.primarypermitDetails(permitDetailsDTO);
					primarypermitDatalist.add(PermitHistoryDeatilsVO);
					permitHistoryVO.setPrimaryPermitVO(primarypermitDatalist);
				}

				if (permitDetailsDTO.getPermitClass().getDescription()
						.equalsIgnoreCase(PermitType.TEMPORARY.getDescription())) {

					PermitHistoryDeatilsVO = regServiceMapper.primarypermitDetails(permitDetailsDTO);
					tempararyPermitDatalist.add(PermitHistoryDeatilsVO);
					permitHistoryVO.setTempararyPermitVO(tempararyPermitDatalist);

				}

			}

		}
		if (CollectionUtils.isEmpty(permitDetails)) {
			throw new BadRequestException("No Records found ");
		}

		return permitHistoryVO;
	}

	@Override
	public Object getVcrDetails(VcrHistoryVO vcrHistoryVO) {

		List<VcrFinalServiceDTO> vcrList = new ArrayList<>();
		List<VcrHistoryVO> vcrHistory = new ArrayList<>();

		if (StringUtils.isNoneBlank(vcrHistoryVO.getPrNo())) {
			vcrList = finalServiceDAO.findByRegistrationRegNo(vcrHistoryVO.getPrNo().toUpperCase());
		}
		if (StringUtils.isNoneBlank(vcrHistoryVO.getVcrNumber())) {
			vcrList = finalServiceDAO.findByVcrVcrNumberIgnoreCase(vcrHistoryVO.getVcrNumber());
		}
		if (vcrList.isEmpty()) {
			throw new BadRequestException("No records found ");
		}
		vcrList.stream().forEach(a -> {
			VcrHistoryVO vcrFinalList = regServiceMapper.setVcrDetails(a);
			vcrHistory.add(vcrFinalList);
		});
		return vcrHistory;
	}

	@Override
	public void generateExcelForDispatchFormSubmission(HttpServletResponse response, String fromDate, String toDate,
			String officeCode, String name) {
		// TODO Auto-generated method stub

		LocalDateTime fromDateAndTime = paymentReportService.getTimewithDate(LocalDate.parse(fromDate), false);
		LocalDateTime toDateAndTime = paymentReportService.getTimewithDate(LocalDate.parse(toDate), true);
		List<DispatcherSubmissionDTO> dispatchDetails = Collections.<DispatcherSubmissionDTO>emptyList();
		if (name.equals("DISPATCH-REPORT")) {
			dispatchDetails = dispatcherSubmissionDAORepo.findByPostedDateBetweenAndOfficeCode(fromDateAndTime,
					toDateAndTime, officeCode);
		} else if (name.equals("RE-DISPATCHREPORT")) {
			dispatchDetails = dispatcherSubmissionDAORepo.findByDeliveryDateBetweenAndOfficeCodeOrderByPrNoAsc(
					LocalDate.parse(fromDate).minusDays(1), LocalDate.parse(toDate).plusDays(1), officeCode);
		} else if (name.equals("RETURNCARD-REPORT")) {
			dispatchDetails = dispatcherSubmissionDAORepo.findByReturnDateBetweenAndOfficeCode(
					LocalDate.parse(fromDate).minusDays(1), LocalDate.parse(toDate).plusDays(1), officeCode);
		}

//		ExcelService excel = new ExcelServiceImpl();
		List<DispatcherSubmissionVO> dispatcherList = new ArrayList<DispatcherSubmissionVO>();
		for (DispatcherSubmissionDTO dto : dispatchDetails) {
			DispatcherSubmissionVO dispatcherSubmissionVO = dispatcherMapper.convertEntity(dto);
			dispatcherList.add(dispatcherSubmissionVO);
		}
		ExcelServiceImpl excelServiceImpl = new ExcelServiceImpl();
		List<String> header = new ArrayList<String>();
		excelServiceImpl.setHeaders(header, name);
		String fileName = name + ".xlsx";
		String sheetName = name;
		this.sheetStyle(dispatcherList, header, fileName, officeCode, response, sheetName);
	}

	public void sheetStyle(List<DispatcherSubmissionVO> dispatcherList, List<String> header, String fileName,
			String officeCode, HttpServletResponse response, String sheetName) {

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		response.setHeader("Pragma", "public");
		response.setHeader("Cache-Control", "no-store");
		response.addHeader("Cache-Control", "max-age=0");
		XSSFWorkbook wb = prepareProps(dispatcherList, header, officeCode, fileName, sheetName);
		XSSFSheet sheet = wb.getSheet(sheetName);
		sheet.setDefaultRowHeight((short) 500);
		sheet.setDefaultColumnWidth(20);
		sheet.setColumnWidth(0, 1800);
		sheet.setColumnWidth(5, 7800);
		sheet.setColumnWidth(3, 7800);
		CellStyle style = wb.createCellStyle();// Create style

		XSSFFont font = wb.createFont();// Create font
		font.setBoldweight(XSSFFont.BOLDWEIGHT_NORMAL);// Make font bold
		font.setColor(HSSFColor.BLACK.index);
		style.setFont(font);
		try {
			ServletOutputStream outputStream = null;
			outputStream = response.getOutputStream();
			wb.write(outputStream);
			outputStream.flush();
			outputStream.close();

		} catch (IOException e) {
			logger.error(" dispatch download excle Exception{}:", e.getMessage());
		}
	}

	public XSSFWorkbook prepareProps(List<DispatcherSubmissionVO> dispatchDetails, List<String> header,
			String officeName, String fileName, String sheet1) {
		XSSFWorkbook wb = new XSSFWorkbook();
		XSSFSheet sheet = wb.createSheet(sheet1);
		CellStyle style = wb.createCellStyle();// Create style
		XSSFFont font = wb.createFont();// Create font
		font.setBoldweight(XSSFFont.BOLDWEIGHT_NORMAL);// Make font bold
		font.setColor(HSSFColor.BLACK.index);
		style.setFont(font);

		XSSFRow Row = sheet.createRow(0);
		Cell createCell = Row.createCell(0);
		sheet.addMergedRegion(CellRangeAddress.valueOf("A1:H2"));
		Cell cell1 = sheet.getRow(0).getCell(0);
		Row row1 = sheet.getRow(0);
		row1.setHeightInPoints(45.0f);
		CellStyle createCellStyle = wb.createCellStyle();
		createCellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		Font createFont = wb.createFont();

		createFont.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);
		createFont.setColor(HSSFColor.DARK_BLUE.index);
		createFont.setFontHeight((short) (28.5 * 20));
		createCellStyle.setFont(createFont);
		cell1.setCellStyle(createCellStyle);

		cell1.setCellValue(sheet1);
		Optional<PropertiesDTO> optionalProp = propertiesDAO.findByImageType("excelImages");
		Decoder decoder = Base64.getDecoder();
		byte[] decode1 = decoder.decode(optionalProp.get().getCmImage());
		int addPicture1 = wb.addPicture(decode1, Workbook.PICTURE_TYPE_JPEG);
		XSSFDrawing drawing = (XSSFDrawing) sheet.createDrawingPatriarch();
		XSSFClientAnchor anchor1 = new XSSFClientAnchor();
		anchor1.setRow1(0);
		anchor1.setCol1(1);
		anchor1.setRow2(2);
		anchor1.setCol2(2);
		XSSFPicture my_picture1 = drawing.createPicture(anchor1, addPicture1);

		byte[] decode2 = decoder.decode(optionalProp.get().getMinImage());
		int addPicture2 = wb.addPicture(decode2, Workbook.PICTURE_TYPE_JPEG);
		XSSFClientAnchor anchor2 = new XSSFClientAnchor();
		anchor2.setRow1(0);
		anchor2.setCol1(6);
		anchor2.setRow2(2);
		anchor2.setCol2(7);
		XSSFPicture my_picture2 = drawing.createPicture(anchor2, addPicture2);

		prepareHeaders(sheet, wb, header);

		Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();
		Integer i = 0;
		for (DispatcherSubmissionVO dispDet : dispatchDetails) {
			List<String> listValues = new ArrayList<String>();
			listValues.add(String.valueOf(i + 1));
			listValues.add(dispDet.getPrNo());
			listValues.add(dispDet.getEmsNumber());
			listValues.add(dispDet.getUserName());
			listValues.add(dispDet.getMobileNo());
			listValues.add(dispDet.getDispatchedBy());
			String disDate = StringUtils.EMPTY;
			switch (sheet1) {
			case "DISPATCH-REPORT":
				if (dispDet.getlUpdate() != null) {
					LocalDate localDate = dispDet.getlUpdate().toLocalDate();
					disDate = localDate.format(DateTimeFormatter.ofPattern("dd-MMM-yy"));
					listValues.add(disDate);
					listValues.add(dispDet.getRemarks());
				}
				break;
			case "RE-DISPATCHREPORT":
				if (dispDet.getDeliveryDate() != null) {
					LocalDate localDate = dispDet.getDeliveryDate();
					disDate = localDate.format(DateTimeFormatter.ofPattern("dd-MMM-yy"));
					listValues.add(disDate);
					listValues.add(dispDet.getRemarks());
				}
			case "RETURNCARD-REPORT":
				if (dispDet.getReturnDate() != null) {
					LocalDate localDate = dispDet.getReturnDate();
					disDate = localDate.format(DateTimeFormatter.ofPattern("dd-MMM-yy"));
					listValues.add(disDate);
					listValues.add(dispDet.getReturnReason());
				}
			default:
				break;
			}
//			listValues.add(disDate);
//			listValues.add(dispDet.getRemarks());
			map.put(i, listValues);
			i++;
		}
		int rowNo = 3;
		for (Entry<Integer, List<String>> singleMap : map.entrySet()) {
			XSSFRow row = sheet.createRow(rowNo);
			row.setHeight((short) 500);
			int cellNo = 0;
			List<String> value = singleMap.getValue();
			for (int k = 0; k < header.size(); k++) {
				XSSFCell cell = row.createCell(k);
				row.getCell(k).setCellStyle(style);
				if (k == 5) {
					String name = value.get(cellNo);
					row.getCell(k).setCellValue(name.replace("null", ""));
				} else {
					row.getCell(k).setCellValue(value.get(cellNo));
				}
				cellNo++;
			}
			rowNo++;
		}
		return wb;
	}

	public void prepareHeaders(XSSFSheet sheet, XSSFWorkbook wb, List<String> header) {
		XSSFRow primaryRow = sheet.createRow(2);

		CellStyle style = wb.createCellStyle();// Create style
		XSSFFont font = wb.createFont();// Create font
		font.setBoldweight(XSSFFont.BOLDWEIGHT_BOLD);// Make font bold
		font.setColor(HSSFColor.GREEN.index);
		style.setFont(font);// set it to bold
		style.setAlignment(CellStyle.ALIGN_LEFT);

		for (int c = 0; c <= header.size() - 1; c++) {
			XSSFCell cell = primaryRow.createCell(c);
			primaryRow.getCell(c).setCellStyle(style);
			cell.setCellValue(header.get(c).toUpperCase());
		}

	}

	@Override
	public OsDataEntryFinalVO reportForOtherStateVehiclesDataEntry(String fromDate, String toDate, String officeCode) {
		// TODO Auto-generated method stub
		LocalDateTime fromDateAndTime = paymentReportService.getTimewithDate(LocalDate.parse(fromDate), false);
		LocalDateTime toDateAndTime = paymentReportService.getTimewithDate(LocalDate.parse(toDate), true);
		Set<Integer> serviceIds = new HashSet<Integer>();
		serviceIds.add(ServiceEnum.DATAENTRY.getId());
		List<String> applicationStatus = new ArrayList<String>();
		applicationStatus.add(StatusRegistration.APPROVED.getDescription());
		List<RegServiceDTO> listOfRecords = regServiceDAO
				.findByServiceIdsInAndApplicationStatusInAndApprovedDateBetween(serviceIds, applicationStatus,
						fromDateAndTime, toDateAndTime);
		if (listOfRecords.isEmpty())
			throw new BadRequestException("No Record Found");
//		List<RegServiceDTO> mviAmountList=new ArrayList<RegServiceDTO>();
		List<RegServiceDTO> mviList = listOfRecords.stream().filter(p -> p.getTaxDetails() != null
				&& p.getTaxDetails().getMvi() != null && (!p.getTaxDetails().getMvi().isEmpty()))
				.collect(Collectors.toList());
		List<RegServiceDTO> mviAmountList = listOfRecords.stream()
				.filter(g -> !mviList.stream().anyMatch(d -> d.getApplicationNo().equals(g.getApplicationNo())))
				.collect(Collectors.toList());
//		mviAmountList.addAll(mviList);
//		listOfRecords.removeAll(mviAmountList);
		List<String> ids = new ArrayList<String>();
		mviAmountList.stream().forEach(single -> {
			ids.add(single.getApplicationNo());
		});
		List<String> noTaxRecordFoundList = new ArrayList<String>();
		HashMap<String, Double> hashMap = new HashMap<String, Double>();
		List<PaymentTransactionDTO> paymentList = paymentTransactionDAO.findByApplicationFormRefNumIn(ids);
		if (!CollectionUtils.isEmpty(paymentList)) {
			List<PaymentTransactionDTO> paySuccessList = paymentList.stream()
					.filter(pay -> pay.getPayStatus().equals(PayStatusEnum.SUCCESS.getDescription()))
					.collect(Collectors.toList());
			ids.stream().forEach(id -> {
				if (!paySuccessList.stream().anyMatch(
						c -> c.getApplicationFormRefNum() != null && c.getApplicationFormRefNum().equals(id))) {
					noTaxRecordFoundList.add(id);
				}
			});
			for (PaymentTransactionDTO paymentDTO : paySuccessList) {
				FeeDetailsDTO feeDetailsDTO = paymentDTO.getFeeDetailsDTO();
				List<PaymentTaxType> taxTypes = new ArrayList<PaymentTaxType>();
				Collections.addAll(taxTypes, ServiceCodeEnum.PaymentTaxType.values());
				taxTypes.remove(PaymentTaxType.getTaxTypeEnumByCode("GreenTax"));
				taxTypes.remove(PaymentTaxType.getTaxTypeEnumByCode("CESS"));
				for (FeesDTO fee : feeDetailsDTO.getFeeDetails()) {
					if (taxTypes.stream().anyMatch(t -> t.getDesc().equalsIgnoreCase(fee.getFeesType()))) {
						hashMap.put(paymentDTO.getApplicationFormRefNum(), fee.getAmount());
					}
				}
			}

			if (!hashMap.isEmpty()) {
				Set<String> keySet = hashMap.keySet();
				paySuccessList.stream().forEach(action -> {
					if (!keySet.stream().anyMatch(pred -> pred.equals(action.getApplicationFormRefNum()))) {
						noTaxRecordFoundList.add(action.getApplicationFormRefNum());
					}
				});
				List<RegServiceDTO> collect = mviAmountList.stream().filter(f -> keySet.contains(f.getApplicationNo()))
						.collect(Collectors.toList());
				mviList.addAll(collect);
			}
		}
		OsDataEntryFinalVO convertEntityToVo = dispatcherMapper.convertEntityToVo(mviList, hashMap);
		if (!CollectionUtils.isEmpty(noTaxRecordFoundList)) {
			dispatcherMapper.mappingNonPayTax(noTaxRecordFoundList, mviAmountList, convertEntityToVo);
		}
		return convertEntityToVo;

	}

	@Override
	public RegReportVO getVcrDistrictWiseCount(RegReportVO regReportVO)
			throws InterruptedException, ExecutionException {

		if (regReportVO.getFromDate() == null || regReportVO.getToDate() == null) {

			throw new BadRequestException("From date or To date should not be empty");
		}

		LocalDateTime fromDate = paymentReportService.getTimewithDate(regReportVO.getFromDate(), Boolean.FALSE);
		LocalDateTime toDate = paymentReportService.getTimewithDate(regReportVO.getToDate(), Boolean.TRUE);
		Map<String, Integer> vcrCountinDist = new HashMap<>();

		/*
		 * set thread pool size
		 */

		ExecutorService executor = Executors.newFixedThreadPool(2);
		try {
			List<String> listOfOfficeCodes = getOfficeCodes().get(regReportVO.getDistrictId());

			/*
			 * prepare task-1 get vcr between two booked dates dates
			 */

			Callable<List<VcrFinalServiceDTO>> task1 = () -> {
				List<VcrFinalServiceDTO> totalevcrList1 = finalServiceDAO
						.findByVcrDateOfCheckBetweenAndOfficeCodeIn(fromDate, toDate, listOfOfficeCodes);
				return totalevcrList1;
			};

			/*
			 * prepare task-2 get vcr between two paid dates
			 */
			Callable<List<VcrFinalServiceDTO>> task2 = () -> {

				List<VcrFinalServiceDTO> paidevcrList1 = finalServiceDAO
						.findByIsVcrClosedAndPaidDateBetweenAndOfficeCodeIn(Boolean.TRUE, fromDate, toDate,
								listOfOfficeCodes);
				return paidevcrList1;

			};

			/*
			 * Executing two tasks
			 */
			List<Future<List<VcrFinalServiceDTO>>> future1 = executor.invokeAll(Arrays.asList(task1, task2));

			boolean status = false;

			while (Boolean.TRUE) {
				if (future1.get(0).isDone() && future1.get(1).isDone()) {

					status = true;

					break;
				}
			}
			return getResult(regReportVO, future1, status, fromDate, toDate);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {

			executor.shutdown();
		}
		return null;
	}

	private RegReportVO getResult(RegReportVO regReportVO, List<Future<List<VcrFinalServiceDTO>>> future1,
			Boolean status, LocalDateTime fromDate, LocalDateTime toDate)
			throws InterruptedException, ExecutionException {

		RegReportVO outputVO = new RegReportVO();

		/*
		 * getting results from future object
		 */

		if (status) {

			List<TotalVcrCountInDist> totalVcrCountInDistlist = new ArrayList<>();
			TotalVcrCountInDist totalVcrCountInDist = new TotalVcrCountInDist();

			totalVcrCountInDist.setVcrCount(future1.get(0).get().size());

			totalVcrCountInDist.setPaidVcrCount(future1.get(1).get().size());

			totalVcrCountInDist.setDistrictId(regReportVO.getDistrictId());
			totalVcrCountInDist
					.setDistricName(districtDAO.findBydistrictId(regReportVO.getDistrictId()).get().getDistrictName());

			totalVcrCountInDistlist.add(totalVcrCountInDist);
			outputVO.setTotalVcrCountInDist(totalVcrCountInDistlist);

		}
		return outputVO;
	}

	private List<MviVcrCount> getBookedVcrsbyMvis(List<String> mvis, LocalDateTime fromDate, LocalDateTime toDate,
			RegReportVO outputVO) {

		Aggregation aggr = newAggregation(
				match(Criteria.where("vcr.dateOfCheck").gt(fromDate).lt(toDate).and("createdBy").in(mvis)),
				group("createdBy").count().as("count"), project("count").and("mviName").previousOperation());

		AggregationResults<MviVcrCount> result = mongoTemplate.aggregate(aggr, VcrFinalServiceDTO.class,
				MviVcrCount.class);
		Long totalVccount = 0L;
		List<MviVcrCount> finalResult = result.getMappedResults();
		Long vcrCount = finalResult.stream().mapToLong(a -> a.getCount()).sum();
		outputVO.setTotalvcrscount(vcrCount);
		MviVcrCount mviVcrCount = new MviVcrCount();
		finalResult.stream().forEach(rec -> {

			String fullName = "";
			MasterUsersDTO user = masterUsersDAO.findByUserId(rec.getMviName());

			if (user.getFirstName() != null) {
				fullName = fullName + user.getFirstName();
			}
			if (user.getLastName() != null) {
				fullName = fullName + user.getLastName();
			}

			rec.setFullName(fullName);

		});

		return finalResult;
	}

	@Override
	public RegReportVO getMviVcrCount(RegReportVO regReportVO) throws ExecutionException, InterruptedException {

		Integer bookedvcrGrandTotal = 0;
		Integer paidvcrGrandTotal = 0;
		LocalDateTime fromDate = paymentReportService.getTimewithDate(regReportVO.getFromDate(), Boolean.FALSE);
		LocalDateTime toDate = paymentReportService.getTimewithDate(regReportVO.getToDate(), Boolean.TRUE);
		List<MviPerformanceVO> mviPerformanceVOList = new ArrayList<>();
		RegReportVO outputVO = new RegReportVO();
		List<OfficeDTO> offices = officeDAO.findByDistrict(regReportVO.getDistrictId());

		List<String> listofAllOfficesinDist = offices.stream().map(a -> a.getOfficeCode()).collect(Collectors.toList());
		List<MasterUsersDTO> userDto = masterUsersDAO
				.findByOfficeOfficeCodeInAndPrimaryRoleNameOrOfficeOfficeCodeInAndAdditionalRoles(
						listofAllOfficesinDist, RoleEnum.MVI, listofAllOfficesinDist, RoleEnum.MVI);

		for (MasterUsersDTO dto : userDto) {

			ExecutorService executor = Executors.newFixedThreadPool(2);
			List<Callable<List<VcrFinalServiceDTO>>> listOfTasks = new ArrayList<>();
			try {
				Callable<List<VcrFinalServiceDTO>> task1 = () -> {
					List<VcrFinalServiceDTO> mviVcrBookedList = finalServiceDAO
							.findByVcrDateOfCheckBetweenAndCreatedBy(fromDate, toDate, dto.getUserId());
					return mviVcrBookedList;
				};
				listOfTasks.add(task1);
				Callable<List<VcrFinalServiceDTO>> task2 = () -> {
					List<VcrFinalServiceDTO> mviVcrPaidList = finalServiceDAO
							.findByCreatedByAndIsVcrClosedAndPaidDateBetween(dto.getUserId(), Boolean.TRUE, fromDate,
									toDate);

					return mviVcrPaidList;
				};
				listOfTasks.add(task2);
				List<Future<List<VcrFinalServiceDTO>>> futureList = new ArrayList<>();

				futureList = executor.invokeAll(listOfTasks);

				boolean status = Boolean.FALSE;

				while (Boolean.TRUE) {
					if (futureList.get(0).isDone() && futureList.get(1).isDone()) {

						status = Boolean.TRUE;
						break;
					}

				}

				MviPerformanceVO mviPerformanceVO = getResultFromTasks(futureList, status, dto.getUserId());

				bookedvcrGrandTotal = bookedvcrGrandTotal + mviPerformanceVO.getToatalBookedvcrCount();
				paidvcrGrandTotal = paidvcrGrandTotal + mviPerformanceVO.getToatalPaidVcrCount();

				mviPerformanceVOList.add(mviPerformanceVO);

			} catch (Exception e) {
				logger.error(e.getMessage());
			} finally {
				executor.shutdown();
			}

		}
		outputVO.setGrandTotalofBookedvcrCount(bookedvcrGrandTotal);
		outputVO.setGrandTotalofPaidevcrCount(paidvcrGrandTotal);
		mviPerformanceVOList.sort((a1, a2) -> a1.getMviName().compareToIgnoreCase(a2.getMviName()));
		outputVO.setMviPerformance(mviPerformanceVOList);

		return outputVO;
	}

	private MviPerformanceVO getResultFromTasks(List<Future<List<VcrFinalServiceDTO>>> futureList, boolean status,
			String userid) throws ExecutionException, InterruptedException {

		Integer cfCollected = 0;
		Double taxCollected = 0.0;
		Double taxArrearsCollected = 0.0;
		Long penaltyCollected = 0L;
		Long penaltyArrearsCollected = 0L;
		Long grandTotal = 0L;

		List<VcrFinalServiceDTO> paidVcrList = new ArrayList<>();
		RegReportVO outputVO = new RegReportVO();

		MviPerformanceVO mviPerformanceVO = new MviPerformanceVO();
		mviPerformanceVO.setMviUserId(userid);
		String fullName = "";
		MasterUsersDTO user = masterUsersDAO.findByUserId(userid);

		if (user.getFirstName() != null) {
			fullName = fullName + user.getFirstName() + " ";
		}
		if (user.getLastName() != null) {
			fullName = fullName + user.getLastName();
		}

		fullName = fullName + "::" + userid.toUpperCase();

		mviPerformanceVO.setMviName(fullName);
		List<MviPerformanceVO> MviPerformanceVOlist = new ArrayList<>();
		if (status) {

			for (int i = 0; i <= futureList.size() - 1; i++) {
				if (i == 0) {
					Integer bookedVcrCount = futureList.get(i).get().size();
					mviPerformanceVO.setToatalBookedvcrCount(bookedVcrCount);

				}
				if (i == 1) {
					paidVcrList = futureList.get(1).get().stream().collect(Collectors.toList());
					Integer paidVcrCount = paidVcrList.size();

					for (VcrFinalServiceDTO a : paidVcrList) {
						if (a.getTax() != null) {
							taxCollected = taxCollected + a.getTax();

							grandTotal = (long) (grandTotal + taxCollected);
						}
						if (a.getPenalty() != null) {
							penaltyCollected = penaltyCollected + a.getPenalty();

							grandTotal = (long) (grandTotal + penaltyCollected);
						}
						if (a.getPenaltyArrears() != null) {
							penaltyArrearsCollected = penaltyArrearsCollected + a.getPenaltyArrears();

							grandTotal = (long) (grandTotal + penaltyArrearsCollected);
						}

						if (a.getOffencetotal() != null) {

							cfCollected = cfCollected + a.getOffencetotal();

							grandTotal = (long) (grandTotal + cfCollected);
						}

						if (a.getTaxArrears() != null) {
							taxArrearsCollected = taxArrearsCollected + a.getTaxArrears();
							System.out.println(taxArrearsCollected);

							grandTotal = (long) (grandTotal + taxArrearsCollected);
						}

					}
					mviPerformanceVO.setToatalPaidVcrCount(paidVcrCount);
					mviPerformanceVO.setPenaltyCollected(penaltyCollected);
					mviPerformanceVO.setPenaltyArrearsCollected(penaltyArrearsCollected);
					mviPerformanceVO.setTaxCollected(taxCollected);
					mviPerformanceVO.setTaxArrearsCollected(taxArrearsCollected);
					mviPerformanceVO.setCfCollected(cfCollected);
					mviPerformanceVO.setGrandTotalAmount(grandTotal);

				}
			}

		}
		return mviPerformanceVO;
	}

	@Override
	public RegReportVO getVcrAllDistricts(RegReportVO regReportVO) {

		ExecutorService executor = Executors.newFixedThreadPool(4);

		List<TotalVcrCountInDist> totalVcrCountInDist = new ArrayList<>();
		RegReportVO vo = new RegReportVO();
		List<DistrictDTO> districts = districtDAO.findByStatus("Y");

		int i = 0;
		districts.stream().forEach(record -> {

			vo.setDistrictId(record.getDistrictId());
			vo.setFromDate(regReportVO.getFromDate());
			vo.setToDate(regReportVO.getToDate());

			try {

				RegReportVO eachDistrict = getVcrDistrictWiseCount(vo);

				if (!eachDistrict.equals(null)) {
					eachDistrict.getTotalVcrCountInDist().stream().forEach(a -> {
						totalVcrCountInDist.add(a);

					});

					totalVcrCountInDist.sort((a1, a2) -> a1.getDistricName().compareTo(a2.getDistricName()));

					vo.setTotalVcrCountInDist(totalVcrCountInDist);

				}

			} catch (InterruptedException e) {
				throw new BadRequestException(e.getMessage());

			} catch (ExecutionException e) {
				throw new BadRequestException(e.getMessage());
			}

		});
		return vo;

	}

	@Override
	public List<VcrFinalServiceVO> getPaidVcrListBymviwise(RegReportVO regReportVO) {
		LocalDateTime fromDate = paymentReportService.getTimewithDate(regReportVO.getFromDate(), Boolean.FALSE);
		LocalDateTime toDate = paymentReportService.getTimewithDate(regReportVO.getToDate(), Boolean.TRUE);

		List<VcrFinalServiceDTO> paidVcrsList = finalServiceDAO.findByIsVcrClosedAndPaidDateBetweenAndCreatedBy(
				Boolean.TRUE, fromDate, toDate, regReportVO.getUserId());

		List<VcrFinalServiceVO> outputVO = vcrFinalServiceMapper.convertEntity(paidVcrsList);

		return outputVO;
	}

	/*
	 * For Dealer rejected application list.
	 */

	@Override
	public Object getRejectionList(String officeCode) {

		List<StagingRegistrationDetailsDTO> stagingList = new ArrayList<>();
		List<StagingRejectedListVO> rejectedList = new ArrayList<>();

		stagingList = stagingRegistrationDetailsDAO
				.findByOfficeDetailsOfficeCodeAndApplicationStatusAndVehicleTypeOrderByCreatedDateDesc(officeCode,
						StatusRegistration.REJECTED.getDescription(), "N");
		if (stagingList.isEmpty()) {
			throw new BadRequestException("There is no Rejected applications with this office code");
		}

		stagingList.stream().forEach(x -> {
			if (x.getRejectionHistory() != null && x.getRejectionHistory().getIsSecondVehicleRejected() != null
					&& x.getRejectionHistory().getIsSecondVehicleRejected()) {

				StagingRejectedListVO vo = stagingDetailsMapper.convertHistory(x);
				rejectedList.add(vo);
			}
		});

		return rejectedList;
	}


	@Override
	public VcrUnpaidResultVo getVcrUnpaidedCountOfficewise(RegReportVO regReportVO, JwtUser jwtUser) {
		LocalDateTime fromDate = paymentReportService.getTimewithDate(regReportVO.getFromDate(), Boolean.FALSE);
		LocalDateTime toDate = paymentReportService.getTimewithDate(regReportVO.getToDate(), Boolean.TRUE);
		VcrUnpaidResultVo vcrUnpaidResultVo=new VcrUnpaidResultVo();
		List<VcrUnpaidCountOfficewiseVo> result=new ArrayList<>();
		if(jwtUser!=null)
		{
			OfficeDTO officeDTO = officeDAO.findByOfficeCode(jwtUser.getOfficeCode()).get();
			
			List<String> listOfOfficeCodes = getOfficeCodes().get(officeDTO.getDistrict());
			/*
			 * Aggregation agg = newAggregation(
			 * match(Criteria.where("vcr.dateOfCheck").gt(fromDate)
			 * .lt(toDate).and("mviOfficeCode")
			 * .in(listOfOfficeCodes).and("isVcrClosed").is(false)),
			 * group("mviOfficeCode").count().as("count"),
			 * project("count").and("officeCode").previousOperation());
			 * 
			 * AggregationResults<VcrUnpaidCountOfficewiseVo> groupResults =
			 * mongoTemplate.aggregate(agg, VcrFinalServiceDTO.class,
			 * VcrUnpaidCountOfficewiseVo.class); List<VcrUnpaidCountOfficewiseVo> result =
			 * groupResults.getMappedResults(); for(VcrUnpaidCountOfficewiseVo res: result)
			 * { res.setOfficeName(officeDAO.findByOfficeCode(res.getOfficeCode()).get().
			 * getOfficeName()); }
			 */
			for(String office :listOfOfficeCodes)
			{
				VcrUnpaidCountOfficewiseVo vcrCount=new VcrUnpaidCountOfficewiseVo();
				vcrCount.setOfficeCode(office);
				OfficeDTO officedto = officeDAO.findByOfficeCode(office).get();
				vcrCount.setOfficeName(officedto.getOfficeName());
				List<VcrFinalServiceDTO> listvcr=finalServiceDAO.findByVcrDateOfCheckBetweenAndIsVcrClosedIsFalseAndMviOfficeCode(fromDate, toDate,
						office);
				vcrCount.setCount(listvcr.size());
				result.add(vcrCount);
			}
			vcrUnpaidResultVo.setListVcrUnpaidCountOfficewiseVo(result);
			
		
		}
		return vcrUnpaidResultVo;
	}

	@Override
	public VcrUnpaidResultVo getVcrDetailedListOfficeWise(RegReportVO regReportVO) {
		LocalDateTime fromDate = paymentReportService.getTimewithDate(regReportVO.getFromDate(), Boolean.FALSE);
		LocalDateTime toDate = paymentReportService.getTimewithDate(regReportVO.getToDate(), Boolean.TRUE);
		VcrUnpaidResultVo vcrUnpaidResultVo=new VcrUnpaidResultVo();
		List<UnpaidVcrListVO> unpaidVcrList=new ArrayList<>();
		String officeName=officeDAO.findByOfficeCode(regReportVO.getOfficeCode()).get().getOfficeName();
		List<VcrFinalServiceDTO> listvcrs= finalServiceDAO.findByVcrDateOfCheckBetweenAndIsVcrClosedIsFalseAndMviOfficeCode(fromDate, toDate,
				regReportVO.getOfficeCode());
		for(VcrFinalServiceDTO dto : listvcrs)
		{
			UnpaidVcrListVO unpaidVcrListVO=vcrFinalServiceMapper.convertVcrUnpaidCountOfficewise(dto);
			unpaidVcrListVO.setOfficeName(officeName);
			unpaidVcrList.add(unpaidVcrListVO);
		}
		vcrUnpaidResultVo.setUnpaidVcrListVO(unpaidVcrList);
		return vcrUnpaidResultVo;
	}

	@Override
	public List<StagingRejectedListVO> getStagingPendingReport(String vehicleType, String officeCode) {
		List<StagingRejectedListVO> listStagingpendingData = new ArrayList<>();
		try {
			List<StagingRegistrationDetailsDTO> stagingpendingData = stagingRegistrationDetailsDAO
					.findByOfficeDetailsOfficeCodeAndApplicationStatusInAndSourceIsNullAndVehicleTypeOrderByCreatedDateDesc(
							officeCode, listOfStatus(), vehicleType.toUpperCase());

			if (stagingpendingData.isEmpty() || CollectionUtils.isEmpty(stagingpendingData)) {
				throw new BadRequestException("No Records Found with the office code");
			}

			stagingpendingData.stream().forEach(x -> {
				StagingRejectedListVO vo = stagingDetailsMapper.convertHistory(x);
				listStagingpendingData.add(vo);

			});

		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());
		}
		return listStagingpendingData;
	}
	
	public List<StatusRegistration> listOfStatus() {
		List<StatusRegistration> listOfStatus = new ArrayList<>();
		listOfStatus.add(StatusRegistration.SECORINVALIDPENDING);
		listOfStatus.add(StatusRegistration.SECORINVALIDFAILED);
		listOfStatus.add(StatusRegistration.DEALERRESUBMISSION);
		listOfStatus.add(StatusRegistration.TRGENERATED);
		listOfStatus.add(StatusRegistration.REJECTED);
		listOfStatus.add(StatusRegistration.SECORINVALIDDONE);
		listOfStatus.add(StatusRegistration.CHASSISTRGENERATED);
		listOfStatus.add(StatusRegistration.TAXPENDING);
		listOfStatus.add(StatusRegistration.SLOTBOOKED);
		listOfStatus.add(StatusRegistration.TAXPAID);
		return listOfStatus;
	}
	
	@Override
	public List<StoppageReportVO> fetchVehicleStoppagerevocationData(String officeCode, RegReportVO regVO) {
		LocalDateTime fromDate = getTimewithDate(regVO.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(regVO.getToDate(), true);
		List<RegServiceDTO> regStoppageList = null;
		List<StoppageReportVO> stoppageReportVo = new ArrayList<>();
		;
		StoppageReportVO stoppageReportVO = null;
		regStoppageList = regServiceDAO
				.findByOfficeDetailsOfficeCodeAndServiceIdsAndApplicationStatusNotInAndCreatedDateBetweenOrderByCreatedDateDesc(
						officeCode, ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId(), 
						Arrays.asList(StatusRegistration.APPROVED.toString(),
						StatusRegistration.CANCELED.toString()), 
						fromDate, toDate);
		if (!CollectionUtils.isEmpty(regStoppageList)) {
			for (RegServiceDTO regServiceDTO : regStoppageList) {
				stoppageReportVO = new StoppageReportVO();
				stoppageReportVO.setOfficeName(regServiceDTO.getOfficeDetails().getOfficeName());
				stoppageReportVO.setVehicleNo(regServiceDTO.getPrNo());
				stoppageReportVO.setVehicleStoppageDate(regServiceDTO.getVehicleStoppageDetails().getStoppageDate());
				stoppageReportVO.setStoppageCov(regServiceDTO.getRegistrationDetails().getClassOfVehicleDesc());
				if (regServiceDTO.getRegistrationDetails().getApplicantDetails().getPresentAddress().getDistrict()
						.getDistrictName().equalsIgnoreCase(regServiceDTO.getVehicleStoppageDetails()
								.getVehicleAddressDetails().getDistrict().getDistrictName())) {
					stoppageReportVO.setIsWithinState(Boolean.FALSE);
				}
				stoppageReportVO.setStoppageAddress(
						stoppageAddress(regServiceDTO.getVehicleStoppageDetails().getVehicleAddressDetails()));
				stoppageReportVO.setStoppageReason(regServiceDTO.getVehicleStoppageDetails().getReasonForStoppage());
				stoppageReportVO.setStoppageMviName(
						mviNameByMviofficeCode(regServiceDTO.getVehicleStoppageDetails().getUserId()));
				stoppageReportVo.add(stoppageReportVO);
			}
		} else {
			throw new BadRequestException("No stopagge recored found");
		}

		return stoppageReportVo;
	}

}
