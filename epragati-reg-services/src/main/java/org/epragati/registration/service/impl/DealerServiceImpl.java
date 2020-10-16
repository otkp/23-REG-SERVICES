package org.epragati.registration.service.impl;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.sort;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.cfstVcr.vo.VcrInputVo;
import org.epragati.common.dao.DealerDeclarationsDAO;
import org.epragati.common.dao.PresentAddressDropDownDAO;
import org.epragati.common.dao.PropertiesDAO;
import org.epragati.common.dto.DealerDeclarationsDTO;
import org.epragati.common.dto.DeclarationsDTO;
import org.epragati.common.dto.FlowDTO;
import org.epragati.common.dto.PresentAddressDropDownDTO;
import org.epragati.common.dto.PropertiesDTO;
import org.epragati.common.vo.DeclarationsVO;
import org.epragati.common.vo.PropertiesVO;
import org.epragati.common.vo.SpecialNumberDetailsReport;
import org.epragati.constants.CovCategory;
import org.epragati.constants.MessageKeys;
import org.epragati.constants.NationalityEnum;
import org.epragati.constants.OwnerType;
import org.epragati.constants.OwnerTypeEnum;
import org.epragati.constants.StageEnum;
import org.epragati.constants.TransferType;
import org.epragati.dao.enclosure.CitizenEnclosuresDAO;
import org.epragati.dealer.tradecert.DealerTradeCertificateNewMapper;
import org.epragati.dealer.tradecert.DealerTradeCertificateNewVO;
import org.epragati.dealer.tradecert.TradeCertificateDealerDto;
import org.epragati.dealer.tradecert.TradeCertificateDealerVO;
import org.epragati.dealer.tradecert.dao.DealerTradeCertificateHistoryDAO;
import org.epragati.dealer.tradecert.dao.TradeCertificateDealerDAO;
import org.epragati.dealer.tradecert.dto.DealerTradeCertificateHistoryDTO;
import org.epragati.dealer.vo.RequestDetailsVO;
import org.epragati.dealer.vo.ResponseDetailsVO;
import org.epragati.dto.enclosure.CitizenEnclosuresDTO;
import org.epragati.dto.enclosure.ImageEnclosureDTO;
import org.epragati.exception.BadRequestException;
import org.epragati.images.service.ImagesService;
import org.epragati.images.vo.ImageInput;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dao.AadhaarResponseDAO;
import org.epragati.master.dao.ApplicantDetailsDAO;
import org.epragati.master.dao.AutomationMasterDAO;
import org.epragati.master.dao.BharathStageDAO;
import org.epragati.master.dao.BsiiiAllowedDAO;
import org.epragati.master.dao.DealerAndVahanMappedCovDAO;
import org.epragati.master.dao.DealerCovDAO;
import org.epragati.master.dao.DistrictDAO;
import org.epragati.master.dao.EnclosuresDAO;
import org.epragati.master.dao.LogsDAO;
import org.epragati.master.dao.MandalDAO;
import org.epragati.master.dao.MasterCovDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.RegServiceDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.StagingRegistrationDetailsDAO;
import org.epragati.master.dao.StagingRegistrationDetailsHistoryDAO;
import org.epragati.master.dao.TaxDetailsDAO;
import org.epragati.master.dao.TrailerCodesDAO;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dao.VahanResponseDAO;
import org.epragati.master.dto.AadhaarDetailsResponseDTO;
import org.epragati.master.dto.ActionDetailsDTO;
import org.epragati.master.dto.ApplicantDetailsDTO;
import org.epragati.master.dto.AutomationDTO;
import org.epragati.master.dto.BharathStageDTO;
import org.epragati.master.dto.BsiiiAllowedDTO;
import org.epragati.master.dto.DealerAndVahanMappedCovDTO;
import org.epragati.master.dto.DealerCovDTO;
import org.epragati.master.dto.DistrictDTO;
import org.epragati.master.dto.EnclosuresDTO;
import org.epragati.master.dto.FinanceDetailsDTO;
import org.epragati.master.dto.HarvestersDetailsDTO;
import org.epragati.master.dto.LogsDTO;
import org.epragati.master.dto.MandalDTO;
import org.epragati.master.dto.MasterCovDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.RegistrationValidityDTO;
import org.epragati.master.dto.RejectionHistoryDTO;
import org.epragati.master.dto.RoleActionDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.dto.StagingRegistrationDetailsHistoryDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.master.dto.TrailerCodesDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.dto.VahanDetailsDTO;
import org.epragati.master.mappers.ApplicantAddressMapper;
import org.epragati.master.mappers.BloodGroupMapper;
import org.epragati.master.mappers.ContactMapper;
import org.epragati.master.mappers.DealerDetailsMapper;
import org.epragati.master.mappers.EnclosuresMapper;
import org.epragati.master.mappers.FinanceDetailsMapper;
import org.epragati.master.mappers.InsuranceDetailsMapper;
import org.epragati.master.mappers.InvoiceDetailsMapper;
import org.epragati.master.mappers.MasterCovMapper;
import org.epragati.master.mappers.OfficeMapper;
import org.epragati.master.mappers.OwnershipMapper;
import org.epragati.master.mappers.PanDetailsMapper;
import org.epragati.master.mappers.QualificationMapper;
import org.epragati.master.mappers.RegVehicleDetailsMapper;
import org.epragati.master.mappers.RegistrationDetailsMapper;
import org.epragati.master.mappers.RegistrationMapper;
import org.epragati.master.mappers.RejectionHistoryMapper;
import org.epragati.master.mappers.VahanDetailsMapper;
import org.epragati.master.service.DealerMakerService;
import org.epragati.master.service.InfoService;
import org.epragati.master.service.LogMovingService;
import org.epragati.master.service.MakersService;
import org.epragati.master.service.MandalService;
import org.epragati.master.service.PrSeriesService;
import org.epragati.master.service.SlotsService;
import org.epragati.master.vo.ApplicantAddressVO;
import org.epragati.master.vo.ApplicantDetailsVO;
import org.epragati.master.vo.ContactVO;
import org.epragati.master.vo.DashBoardCountVO;
import org.epragati.master.vo.DealerDetailsVO;
import org.epragati.master.vo.DealerMakerVO;
import org.epragati.master.vo.DealerRegVO;
import org.epragati.master.vo.EnclosuresVO;
import org.epragati.master.vo.MakersVO;
import org.epragati.master.vo.MasterCovVO;
import org.epragati.master.vo.OfficeVO;
import org.epragati.master.vo.OwnershipVO;
import org.epragati.master.vo.RegistrationDetailsVO;
import org.epragati.master.vo.RejectionHistoryVO;
import org.epragati.master.vo.TaxTypeVO;
import org.epragati.master.vo.VahanDetailsVO;
import org.epragati.payment.dto.FeesDTO;
import org.epragati.payment.mapper.ApplicantDeatilsMapper;
import org.epragati.payment.report.vo.ReportsVO;
import org.epragati.payments.dao.PaymentFeesDeatailsDAO;
import org.epragati.payments.vo.ClassOfVehiclesVO;
import org.epragati.registration.service.DealerService;
import org.epragati.regservice.RegistrationService;
import org.epragati.regservice.dao.TrailerDetailsDAO;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.dto.SlotDetailsDTO;
import org.epragati.regservice.dto.TrailerDetailsDTO;
import org.epragati.regservice.dto.TrasnferOfOwnerShipDTO;
import org.epragati.regservice.mapper.RegServiceMapper;
import org.epragati.regservice.otherstate.OtherStateVahanService;
import org.epragati.regservice.vo.RegServiceVO;
import org.epragati.reports.service.ReportFiles;
import org.epragati.reports.service.impl.RtaReportServiceImpl;
import org.epragati.restGateway.RestGateWayService;
import org.epragati.rta.service.impl.service.RTAService;
import org.epragati.rta.service.impl.service.RegistratrionServicesApprovals;
import org.epragati.sequence.SequenceGenerator;
import org.epragati.service.enclosure.vo.CitizenImagesInput;
import org.epragati.service.files.GridFsClient;
import org.epragati.service.notification.MessageTemplate;
import org.epragati.service.notification.NotificationUtil;
import org.epragati.sn.vo.SpecialNumberDetailsByTRVO;
import org.epragati.tax.vo.TaxTypeEnum;
import org.epragati.util.AppErrors;
import org.epragati.util.AppMessages;
import org.epragati.util.DateConverters;
import org.epragati.util.QRCodeUtil;
import org.epragati.util.StatusRegistration;
import org.epragati.util.ValidityEnum;
import org.epragati.util.document.KeyValue;
import org.epragati.util.document.Sequence;
import org.epragati.util.payment.ClassOfVehicleEnum;
import org.epragati.util.payment.FinanceTowEnum;
import org.epragati.util.payment.GatewayTypeEnum;
import org.epragati.util.payment.ModuleEnum;
import org.epragati.util.payment.PayStatusEnum;
import org.epragati.util.payment.ServiceEnum;
import org.epragati.vcrImage.dao.VcrFinalServiceDAO;
import org.epragati.vcrImage.dto.OtherVoluntaryTax;
import org.epragati.vcrImage.dto.VcrFinalServiceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class DealerServiceImpl implements DealerService {
	public static final String APP_PAYMENT_TYPE_DUPLICATE = "DUPLICATE_APPLIED";
	private static List<String> dealerStagingDetails = new ArrayList<>();
	private static List<String> intiatedList = new ArrayList<>();
	private static List<String> rejectedList = new ArrayList<>();
	private static List<String> paymentList = new ArrayList<>();
	private static List<String> specialNoList = new ArrayList<>();
	static {
		prepareDealerStagingAppStatus();
		intiatedList();
		rejectedList();
		paymentCount();
		specialNoCount();
	}

	private static final Logger logger = LoggerFactory.getLogger(DealerServiceImpl.class);

	@Autowired
	private AppMessages appMessages;

	@Autowired
	private AppErrors appErrors;

	@Autowired
	private RegistrationDetailsMapper<StagingRegistrationDetailsDTO> registrationDetailsMapper;

	@Autowired
	private ContactMapper contactMapper;

	@Autowired
	private ApplicantAddressMapper applicantAddressMapper;

	@Autowired
	private InsuranceDetailsMapper insuranceMapper;

	@Autowired
	private InvoiceDetailsMapper invoiceDetailsMapper;

	@Autowired
	private OfficeMapper officeMapper;

	@Autowired
	private StagingRegistrationDetailsDAO stagingRegistrationDetailsDAO;

	@Autowired
	private ApplicantDetailsDAO applicantDetailsDAO;

	@Autowired
	private MakersService makersService;

	@Autowired
	private DealerMakerService dealerMakerService;

	@Autowired
	private AadhaarResponseDAO aadhaarResponseDAO;

	@Autowired
	private MandalService mandalService;

	@Autowired
	private PanDetailsMapper panDetailsMapper;

	@Autowired
	private QRCodeUtil qRCodeUtil;

	@Autowired
	private DealerDetailsMapper dealerDetailsMapper;

	@Autowired
	private BloodGroupMapper bloodGroupMapper;

	@Autowired
	private QualificationMapper qualificationMapper;

	@Autowired
	private SequenceGenerator sequenceGenerator;

	@Autowired
	private VahanResponseDAO vahanDAO;

	@Autowired
	private LogsDAO logsDAO;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private DealerCovDAO dealerCovDAO;

	@Autowired
	private DealerAndVahanMappedCovDAO dealerAndVahanMappedCovDAO;

	@Autowired
	private ImagesService magesService;

	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;

	@Autowired
	private RegistrationMapper registrationMapper;

	@Autowired
	private MasterCovDAO masterCovDAO;

	@Autowired
	private BsiiiAllowedDAO bsiiiAllowedDAO;

	@Autowired

	private RejectionHistoryMapper rejectionHistoryMapper;
	@Autowired
	private BharathStageDAO bharathStageDAO;

	@Autowired
	private EnclosuresDAO enclosuresDAO;

	@Autowired
	private NotificationUtil notifications;
	@Autowired
	private StagingRegistrationDetailsHistoryDAO stagingRegistrationDetailsHistoryDAO;

	@Autowired
	private FinanceDetailsMapper financeDetailsMapper;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RegServiceMapper regServiceMapper;

	@Autowired
	private RegServiceDAO regServiceDAO;

	@Autowired
	private EnclosuresMapper userEnclosureMapper;

	@Autowired
	private GridFsClient gridFsClient;

	@Autowired
	private RestGateWayService restGateWayService;

	// @Autowired
	// private StagingRegistrationDetailsDAO stagingDAO;

	@Autowired
	private VahanDetailsMapper vahanMapper;

	@Value("${reg.service.invoiceValueValidation:}")
	private String invoiceValueValidation;

	@Autowired
	private PropertiesDAO propertiesDAO;

	@Autowired
	private TrailerDetailsDAO trailerDetailsDAO;

	@Autowired
	private TrailerCodesDAO trailerCodesDAO;

	@Autowired
	private OfficeDAO officeDAO;

	@Autowired
	private LogMovingService logMovingService;

	@Autowired
	private ApplicantDeatilsMapper applicantDetailsMapper;

	@Autowired
	private RtaReportServiceImpl rtaReportServiceImpl;

	@Autowired
	private RegVehicleDetailsMapper vehicleDetailsMapper;

	@Autowired
	private RegistrationService registrationService;

	@Autowired
	private CitizenEnclosuresDAO citizenEnclosuresDAO;

	@Autowired
	private RegistratrionServicesApprovals registratrionServicesApprovals;

	@Autowired
	private SlotsService slotService;

	@Autowired
	private MandalDAO mandalDAO;

	@Autowired
	private DistrictDAO districtDAO;

	@Autowired
	private MongoTemplate mongoTemplate;
	@Autowired
	private TaxDetailsDAO taxDetailsDAO;
	@Autowired
	private InfoService infoService;

	@Autowired
	private OwnershipMapper ownerShipMapper;

	@Autowired
	private PresentAddressDropDownDAO presentAddressDropDownDAO;

	@Autowired
	private AutomationMasterDAO automationMasterDAO;

	@Autowired
	private RTAService rtaService;

	@Autowired
	private DealerDeclarationsDAO dealerDeclarationsDAO;

	@Autowired
	private OtherStateVahanService otherStateVahanService;

	@Autowired
	private DealerCovDAO dealerCovDao;
	@Autowired
	private PaymentFeesDeatailsDAO feesDao;
	@Autowired
	private TradeCertificateDealerDAO tradeCertDao;
	@Autowired
	private DealerTradeCertificateNewMapper tradeCertMapper;

	@Autowired
	private VcrFinalServiceDAO vcrFinalServiceDAO;

	@Autowired
	private DealerTradeCertificateHistoryDAO dealerTCHistoryDAO;

	@Autowired
	private ResourceLoader resourceLoader;

	@Autowired
	private MasterCovMapper covMapper;

	@Autowired
	private PrSeriesService prSeriesService;

	private static final String TRADE_VALIDITY_STATUS_EXP = "Expire";
	private static final String TRADE_VALIDITY_STATUS_VALID = "Valid";

	@Override
	public Optional<RegistrationDetailsVO> getRegistrationDetailById(String id) {
		Optional<StagingRegistrationDetailsDTO> resultOpt = stagingRegistrationDetailsDAO.findByApplicationNo(id);
		if (resultOpt.isPresent()) {
			logger.debug("Registration Details found for application id : [{}]", id);
			Optional<RegistrationDetailsVO> returnResult = registrationDetailsMapper.convertEntity(resultOpt);
			returnResult.get().setImageDetailsList(magesService.getListOfSupportedEnclosuresForMobile(id));
			return registrationDetailsMapper.convertEntity(resultOpt);
		}
		logger.debug("Registration Details Not found for application id : [{}]", id);
		return Optional.empty();
	}

	public List<DashBoardCountVO> getHostingCount(List<Integer> stageNoList, String dealerId) {

		Aggregation agg = newAggregation(match(Criteria.where("dealerDetails.dealerId").is(dealerId)),
				match(Criteria.where("stageNo").in(stageNoList)), group("applicationStatus").count().as("draftCount"),
				project("draftCount").and("status").previousOperation(), sort(Sort.Direction.DESC, "draftCount")

		);

		// Convert the aggregation result into a List
		AggregationResults<DashBoardCountVO> groupResults = mongoTemplate.aggregate(agg,
				StagingRegistrationDetailsDTO.class, DashBoardCountVO.class);
		List<DashBoardCountVO> result = groupResults.getMappedResults();

		return result;

	}

	@Override
	public Map<String, DashBoardCountVO> getDashBoardDetails(String dealerId) {
		Map<String, DashBoardCountVO> applicationCountMap = new HashMap<>();
		long startTime = System.currentTimeMillis();
		logger.debug("Staging Dash Board Count start dealerId  [{}] at Date [{}] ", dealerId, startTime);
		List<StagingRegistrationDetailsDTO> resultList = stagingRegistrationDetailsDAO
				.findByDealerDetailsDealerId(dealerId);
		logger.debug("Staging Dash Board Count End dealerId  [{}] at Date [{}] , count [{}]", dealerId,
				System.currentTimeMillis() - startTime, resultList.size());
		applicationCountMap.put("Dash_Board_Count", getDashBoardCount(resultList));
		if (!resultList.isEmpty()) {
			applicationCountMap.put("Dash_Board_Count", getDashBoardCount(resultList));
		}

		resultList.clear();

		return applicationCountMap;
	}

	private static List<String> prepareDealerStagingAppStatus() {
		if (CollectionUtils.isEmpty(dealerStagingDetails)) {
			dealerStagingDetails = new ArrayList<>();
			dealerStagingDetails.add(StatusRegistration.TRGENERATED.getDescription());
			dealerStagingDetails.add(StatusRegistration.REUPLOAD.getDescription());
			dealerStagingDetails.add(StatusRegistration.DEALERRESUBMISSION.getDescription());
			dealerStagingDetails.add(StatusRegistration.CHASSISTRGENERATED.getDescription());
			dealerStagingDetails.add(StatusRegistration.CITIZENPAYMENTFAILED.getDescription());
			dealerStagingDetails.add(StatusRegistration.CITIZENPAYMENTPENDING.getDescription());
			dealerStagingDetails.add(StatusRegistration.TAXPAID.getDescription());
			dealerStagingDetails.add(StatusRegistration.SECORINVALIDDONE.getDescription());
			dealerStagingDetails.add(StatusRegistration.SLOTBOOKED.getDescription());
			dealerStagingDetails.add(StatusRegistration.APPROVED.getDescription());
			dealerStagingDetails.add(StatusRegistration.TAXPENDING.getDescription());
			dealerStagingDetails.add(StatusRegistration.IVCNREJECTED.getDescription());
			dealerStagingDetails.add(StatusRegistration.IVCNPAYMENTPENDING.getDescription());
			dealerStagingDetails.add(StatusRegistration.IVCNPAYMENTFAILED.getDescription());
		}
		return dealerStagingDetails;
	}

	public static List<String> paymentCount() {
		if (CollectionUtils.isEmpty(paymentList)) {
			paymentList = new ArrayList<>();
			paymentList.add(StatusRegistration.PAYMENTPENDING.getDescription());
			paymentList.add(StatusRegistration.SECORINVALIDINITIATED.getDescription());
			paymentList.add(StatusRegistration.SECORINVALIDFAILED.getDescription());
			paymentList.add(StatusRegistration.SECORINVALIDPENDING.getDescription());
			paymentList.add(StatusRegistration.PAYMENTFAILED.getDescription());
			paymentList.add(StatusRegistration.IVCNPAYMENTPENDING.getDescription());
			paymentList.add(StatusRegistration.PAYMENTSUCCESS.getDescription());
		}
		return paymentList;
	}

	public static List<String> specialNoCount() {
		if (CollectionUtils.isEmpty(specialNoList)) {
			specialNoList = new ArrayList<>();
			specialNoList.add(StatusRegistration.SPECIALNOPENDING.getDescription());
		}
		return specialNoList;
	}

	public static List<String> intiatedList() {
		if (CollectionUtils.isEmpty(intiatedList)) {
			intiatedList = new ArrayList<>();
			intiatedList.add(StatusRegistration.INITIATED.getDescription());
		}
		return intiatedList;

	}

	public static List<String> rejectedList() {
		if (CollectionUtils.isEmpty(rejectedList)) {
			rejectedList = new ArrayList<>();
			rejectedList.add(StatusRegistration.REJECTED.getDescription());
			rejectedList.add(StatusRegistration.IVCNREJECTED.getDescription());
		}
		return rejectedList;
	}

	private DashBoardCountVO getDashBoardCount(List<StagingRegistrationDetailsDTO> resultList) {
		DashBoardCountVO dashBoardMap = new DashBoardCountVO();

		Map<String, Long> statusCountMap = resultList.stream().filter(val -> val.getApplicationStatus() != null)
				.collect(Collectors.groupingBy(StagingRegistrationDetailsDTO::getApplicationStatus,
						Collectors.counting()));

		long initiatedCount = verifyCount(statusCountMap, intiatedList()).stream().mapToLong(Long::longValue).sum();

		long trGeneratedCount = verifyCount(statusCountMap, prepareDealerStagingAppStatus()).stream()
				.mapToLong(Long::longValue).sum();

		long paymentCount = verifyCount(statusCountMap, paymentCount()).stream().mapToLong(Long::longValue).sum();

		long specialNoCount = verifyCount(statusCountMap, specialNoCount()).stream().mapToLong(Long::longValue).sum();

		long rejectedCount = verifyCount(statusCountMap, rejectedList()).stream().mapToLong(Long::longValue).sum();

		dashBoardMap.setSnCount((int) specialNoCount);
		dashBoardMap.setPaymentPendingCount((int) paymentCount);
		dashBoardMap.setTrGeneratedCount((int) trGeneratedCount);
		dashBoardMap.setDraftCount((int) initiatedCount);
		dashBoardMap.setRejectedCount((int) rejectedCount);
		dashBoardMap.setStatus("Active");

		return dashBoardMap;
	}

	public List<Long> verifyCount(Map<String, Long> covCountMap, List<String> status) {
		List<Long> totalCount = new ArrayList<>();
		status.stream().forEach(s -> {
			if (covCountMap.get(s) != null) {
				totalCount.add(covCountMap.get(s));
			}
		});
		return totalCount;
	}

	@Override
	public List<RegistrationDetailsVO> getRegistrationDetailsByStage(String stageId, String dealerId) {
		List<RegistrationDetailsVO> outPutList = new ArrayList<>();

		long startTime = System.currentTimeMillis();
		logger.debug("Staging Dash Board start dealerId  [{}] at Date In Millis [{}] ", dealerId, startTime);

		List<StagingRegistrationDetailsDTO> resultList = stagingRegistrationDetailsDAO
				.findByDealerDetailsDealerId(dealerId);

		logger.debug("Staging Dash Board end dealerId  [{}] at Execution Time In Millis  [{}] , count [{}]", dealerId,
				System.currentTimeMillis() - startTime, resultList.size());

		if (!resultList.isEmpty()) {
			List<StagingRegistrationDetailsDTO> resultFilterList = resultList.stream()
					.filter(registrationDetail -> registrationDetail.getApplicationStatus().equalsIgnoreCase(stageId))
					.collect(Collectors.toList());

			if (stageId.equalsIgnoreCase(StatusRegistration.REJECTED.getDescription())) {

				resultList.parallelStream().forEach(val -> {
					if (rejectedDashBoardList().contains(val.getApplicationStatus())) {
						resultFilterList.add(val);
					}

				});
			}
			if (stageId.equalsIgnoreCase(StatusRegistration.PAYMENTPENDING.getDescription())) {

				resultList.parallelStream().forEach(val -> {
					if (paymentPendingList().contains(val.getApplicationStatus())) {
						resultFilterList.add(val);
					}

				});

			}
			if (stageId.equalsIgnoreCase(StatusRegistration.TRGENERATED.getDescription())) {

				resultList.parallelStream().forEach(val -> {
					if (trGeneratedList().contains(val.getApplicationStatus())) {
						resultFilterList.add(val);
					}

				});
			}
			return registrationDetailsMapper.dashBoardLimitedFields(resultFilterList);
		}
		resultList.clear();
		return outPutList;

	}

	public List<String> trGeneratedList() {
		return Arrays.asList(StatusRegistration.REUPLOAD.getDescription(),
				StatusRegistration.DEALERRESUBMISSION.getDescription(),
				StatusRegistration.CHASSISTRGENERATED.getDescription(),
				StatusRegistration.CITIZENPAYMENTFAILED.getDescription(),
				StatusRegistration.CITIZENPAYMENTPENDING.getDescription(), StatusRegistration.TAXPAID.getDescription(),
				StatusRegistration.SECORINVALIDDONE.getDescription(), StatusRegistration.SLOTBOOKED.getDescription(),
				StatusRegistration.APPROVED.getDescription(), StatusRegistration.TAXPENDING.getDescription(),
				StatusRegistration.IVCNREJECTED.getDescription(),
				StatusRegistration.IVCNPAYMENTPENDING.getDescription(),
				StatusRegistration.IVCNPAYMENTFAILED.getDescription());
	}

	public List<String> paymentPendingList() {
		return Arrays.asList(StatusRegistration.PAYMENTFAILED.getDescription(),
				StatusRegistration.PAYMENTSUCCESS.getDescription(),
				StatusRegistration.SECORINVALIDPENDING.getDescription(),
				StatusRegistration.SECORINVALIDFAILED.getDescription(),
				StatusRegistration.SECORINVALIDINITIATED.getDescription(),
				StatusRegistration.IVCNPAYMENTPENDING.getDescription(),
				StatusRegistration.SECORINVALIDDONE.getDescription());

	}

	public List<String> rejectedDashBoardList() {
		return Arrays.asList(StatusRegistration.IVCNREJECTED.getDescription());
	}

	@Override
	public RegistrationDetailsVO saveRegistrationDetails(RegistrationDetailsVO registrationDetails) {
		StagingRegistrationDetailsDTO persistData = null;
		if (StringUtils.isEmpty(registrationDetails.getApplicationNo())
				&& registrationDetails.getStageNo() > StageEnum.ONE.getNumber()) {
			logger.error("Exception in SaveRegDetails with application No [{}] ",
					registrationDetails.getApplicationNo());
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.DEALER_UNSUPPORTEDREQUEST));
		}
		if (StringUtils.isNoneEmpty(registrationDetails.getApplicationNo())) {
			Optional<StagingRegistrationDetailsDTO> entity = stagingRegistrationDetailsDAO
					.findByApplicationNo(registrationDetails.getApplicationNo());
			if (!entity.isPresent()) {
				logger.error("registration details nor found with application No:[{}]",
						registrationDetails.getApplicationNo());
				throw new BadRequestException(
						appMessages.getLogMessage(MessageKeys.MESSAGE_FAILED_INCORRECT_PARAMETER));
			}
			if (!entity.get().getApplicationStatus().equalsIgnoreCase(StatusRegistration.INITIATED.getDescription())) {
				logger.error(
						" Exception with application No [{}]  Application might be processed, Please verify in Dashboard",
						registrationDetails.getApplicationNo());
				throw new BadRequestException("Application might be processed, Please verify in Dashboard.");
			}
			persistData = entity.get();
		} else {
			persistData = new StagingRegistrationDetailsDTO();
		}
		persistData.setStageNo(registrationDetails.getStageNo());
		try {
			if (null != registrationDetails.getApplicantDetails().getAadharNo())
				registrationDetails.setAadharNo(registrationDetails.getApplicantDetails().getAadharNo());
			updateRegistrationDetailsBasedOnStageNo(persistData, registrationDetails);
		} catch (Exception e) {
			logger.error("Exception occurred [{}]", e.getMessage());
			throw new BadRequestException(e.getMessage());
		}

		if (null == persistData.getCreatedDate())
			persistData.setCreatedDate(LocalDateTime.now());
		persistData.setApplicationStatus(StatusRegistration.INITIATED.getDescription());
		persistData.setIteration(1);
		persistData.setlUpdate(LocalDateTime.now());
		persistData.setCreatedDateStr(LocalDateTime.now().toString());
		persistData.setReqAuthType(registrationDetails.getReqAuthType());
		persistData.setServiceIds(Arrays.asList(ServiceEnum.NEWREG.getId()));
		if (registrationDetails.getIsTrailer() && persistData.getVahanDetails() != null) {
			registrationDetails.getVahanDetails().setChassisNumber(persistData.getVahanDetails().getChassisNumber());
		}
		logMovingService.moveStagingToLog(persistData.getApplicationNo());
		stagingRegistrationDetailsDAO.save(persistData);
		registrationDetails.setApplicationNo(persistData.getApplicationNo());
		return registrationDetails;
	}

	private void updateRegistrationDetailsBasedOnStageNo(StagingRegistrationDetailsDTO registrationDetailsDTO,
			RegistrationDetailsVO registrationDetailsVO) {
		try {
			if (null != registrationDetailsVO.getDealerDetails()) {
				if (registrationDetailsDTO.getDealerDetails() == null) {
					registrationDetailsDTO
							.setDealerDetails(dealerDetailsMapper.convertVO(registrationDetailsVO.getDealerDetails()));
				}
			}
			if (StageEnum.ONE.getNumber() <= registrationDetailsVO.getStageNo()) {

				saveRegistrationDetailsBasedOnStageNoOne(registrationDetailsDTO, registrationDetailsVO);

			}
			// VCR Check
			this.onlineVcrCheck(registrationDetailsDTO, registrationDetailsVO);
			// startind update process based on stages
			if (null == registrationDetailsDTO) {
				logger.error("Exception Registration Details nor found  ");
				throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.ERROR_REG_DETILSNOTFOUND));
			}
			if (StageEnum.TWO.getNumber() <= registrationDetailsVO.getStageNo()) {

				updateRegistrationDetailsBasedOnStageNoTwo(registrationDetailsDTO, registrationDetailsVO);

			}
			if (StageEnum.THREE.getNumber() <= registrationDetailsVO.getStageNo()) {
				updateRegistrationDetailsBasedOnStageNoThree(registrationDetailsDTO, registrationDetailsVO);

			}
			List<String> ownerTypeList = new ArrayList<>();
			ownerTypeList.add(OwnerTypeEnum.Government.toString());
			ownerTypeList.add(OwnerTypeEnum.Stu.toString());
			ownerTypeList.add(OwnerTypeEnum.POLICE.toString());

			if (StageEnum.FOUR.getNumber() <= registrationDetailsVO.getStageNo()
					&& !ownerTypeList.contains(registrationDetailsVO.getOwnerType().toString())) {
				updateRegistrationDetailsBasedOnStageNoFour(registrationDetailsDTO, registrationDetailsVO);

			}
			if (StageEnum.FIVE.getNumber() <= registrationDetailsVO.getStageNo()) {
				saveDealerDeclartions(registrationDetailsDTO, registrationDetailsVO);
				updateRegistrationDetailsBasedOnStageNoFive(registrationDetailsDTO, registrationDetailsVO);

			}

		} catch (Exception e) {
			logger.error("Exception occurred [{}]", e.getMessage());
			throw new BadRequestException(e.getMessage());
		}
	}

	// First time save
	private void saveRegistrationDetailsBasedOnStageNoOne(StagingRegistrationDetailsDTO registrationDetailsDTO,
			RegistrationDetailsVO registrationDetails) {
		ApplicantDetailsDTO applicantDetailsDTO;
		Optional<ApplicantDetailsDTO> applicantDetailsOption = Optional.empty();
		if (registrationDetails.getApplicantDetails() != null
				&& StringUtils.isNoneBlank(registrationDetails.getApplicantDetails().getApplicantNo())) {
			applicantDetailsOption = applicantDetailsDAO
					.findByApplicantNo(registrationDetails.getApplicantDetails().getApplicantNo());
		}

		if (applicantDetailsOption.isPresent()) {
			applicantDetailsDTO = applicantDetailsOption.get();
		} else {
			applicantDetailsDTO = new ApplicantDetailsDTO();
			applicantDetailsDTO.setApplicantNo(getTransactionId());
		}

		registrationDetailsDTO.setOwnerType(registrationDetails.getOwnerType());
		
		// Added on 260719
				registrationDetailsDTO.setIsduplicateRecord(Boolean.FALSE);
				registrationDetailsDTO.setIsActive(Boolean.TRUE);
		if (registrationDetails.getReqAuthType().equalsIgnoreCase("Without Aadhar")) {
			registrationDetails.getApplicantDetails().getAadharResponse().setUuId(UUID.randomUUID());
			AadhaarDetailsResponseDTO dto=new AadhaarDetailsResponseDTO();
			dto.setUuId(registrationDetails.getApplicantDetails().getAadharResponse().getUuId());
			dto.setUid(Long.valueOf(registrationDetails.getAadharNo()));
			Optional<PropertiesDTO> property=propertiesDAO.findByIsWithoutAadharTrue();
			if(property.isPresent()) {
				dto.setBase64file(property.get().getBase64Image());
			}
			dto.setCo(registrationDetails.getApplicantDetails().getFatherName());
			if (registrationDetails.getApplicantDetails().getAadharResponse().getDistrict_name() != null) {
				dto.setDistrict_name(registrationDetails.getApplicantDetails().getAadharResponse().getDistrict_name());
				dto.setDistrict(registrationDetails.getApplicantDetails().getAadharResponse().getDistrict_name());
			}
			else if(registrationDetails.getApplicantDetails().getAadharResponse().getDistrict() != null){
				dto.setDistrict(registrationDetails.getApplicantDetails().getAadharResponse().getDistrict());
				dto.setDistrict_name(registrationDetails.getApplicantDetails().getAadharResponse().getDistrict());
			}else{
			dto.setDistrict_name(registrationDetails.getApplicantDetails().getPresentAddress().getDistrict().getDistrictName());
			dto.setDistrict(registrationDetails.getApplicantDetails().getPresentAddress().getDistrict().getDistrictName());
			}
			LocalDate dob=registrationDetails.getApplicantDetails().getDateOfBirth();	
			dto.setDob(dob.format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
			dto.setGender(registrationDetails.getApplicantDetails().getGender());
			dto.setHouse(registrationDetails.getApplicantDetails().getAadharResponse().getHouse());
			dto.setLc(registrationDetails.getApplicantDetails().getAadharResponse().getLc());
			dto.setName(registrationDetails.getApplicantDetails().getDisplayName());
			dto.setPincode(registrationDetails.getApplicantDetails().getAadharResponse().getPincode());
			dto.setStatecode(registrationDetails.getApplicantDetails().getAadharResponse().getStatecode());
			dto.setStreet(registrationDetails.getApplicantDetails().getAadharResponse().getStreet());
			dto.setVillage(registrationDetails.getApplicantDetails().getAadharResponse().getVillage());
			dto.setVillage_name(registrationDetails.getApplicantDetails().getAadharResponse().getVillage_name());
			dto.setVtc(registrationDetails.getApplicantDetails().getAadharResponse().getVillage_name());
			dto.setMandal_name(registrationDetails.getApplicantDetails().getAadharResponse().getMandal_name());
			dto.setMandal(registrationDetails.getApplicantDetails().getAadharResponse().getMandal());
			dto.setDeviceNumber("Without Aadhar");
			aadhaarResponseDAO.save(dto);
		}
		// registrationDetailsDTO.setAadharNo(registrationDetails.getAadharNo());
		Optional<AadhaarDetailsResponseDTO> aadhaarDetailsResponseDTOOpt = Optional.empty();
		if (null != registrationDetails.getApplicantDetails().getAadharResponse()
				&& null != registrationDetails.getApplicantDetails().getAadharResponse().getUuId()) {
			aadhaarDetailsResponseDTOOpt = aadhaarResponseDAO
					.findByUuId(registrationDetails.getApplicantDetails().getAadharResponse().getUuId());
			if (!aadhaarDetailsResponseDTOOpt.isPresent()) {
				logger.error("Aadhaar details not found While Saving [{}]",
						registrationDetails.getApplicantDetails().getAadharResponse().getUuId());
				throw new BadRequestException("Aadhaar Details Not Found ");
			}
		} else {
			logger.error("AadhaarDetails Required");
			throw new BadRequestException("Aadhaar Details Required.");
		}
		// ApplicantDetailsDTO applicantDetailsDTO = new ApplicantDetailsDTO();
		// applicantDetailsDTO.setApplicantNo(getTransactionId());

		if (null != registrationDetails.getApplicantDetails().getSameAsAadhar()
				&& registrationDetails.getApplicantDetails().getSameAsAadhar()
				&& !compareAadharResponseDetailsAndApplicantAddressDetails(aadhaarDetailsResponseDTOOpt.get(),
						registrationDetails.getApplicantDetails().getPresentAddress())) {
			logger.error("Dealer addres mismatch");
			throw new BadRequestException(appErrors.getResponseMessage(MessageKeys.DEALER_ADDRESSMISMATCH));
		}

		if (null != registrationDetails.getApplicantDetails().getPresentAddress()) {
			if (null != registrationDetails.getApplicantDetails().getPresentAddress().getState()) {
				List<DistrictDTO> districtList = districtDAO.findByStateId(
						registrationDetails.getApplicantDetails().getPresentAddress().getState().getStateId());
				if (!districtList.isEmpty()) {
					if (!(districtList.stream().anyMatch(district -> district.getStateId().equalsIgnoreCase(
							registrationDetails.getApplicantDetails().getPresentAddress().getState().getStateId())))) {
						String errorMsg = "District ["
								+ registrationDetails.getApplicantDetails().getPresentAddress().getDistrict()
										.getDistrictName()
								+ "] not belongs to " + registrationDetails.getApplicantDetails().getPresentAddress()
										.getState().getStateName()
								+ " State";
						logger.error(errorMsg);
						throw new BadRequestException(errorMsg);
					}
				}
			}
			if (null != registrationDetails.getApplicantDetails().getPresentAddress().getDistrict()) {
				List<MandalDTO> mandalList = mandalDAO.findByDistrictId(
						registrationDetails.getApplicantDetails().getPresentAddress().getDistrict().getDistrictId());
				if (!mandalList.isEmpty()) {
					if (!(mandalList.stream().anyMatch(mandal -> mandal.getDistrictId().equals(registrationDetails
							.getApplicantDetails().getPresentAddress().getMandal().getDistrictId())))) {
						String errorMsg = "Mandal ["
								+ registrationDetails.getApplicantDetails().getPresentAddress().getMandal()
										.getMandalName()
								+ "] not belongs to " + registrationDetails.getApplicantDetails().getPresentAddress()
										.getDistrict().getDistrictName()
								+ " District";
						logger.error(errorMsg);
						throw new BadRequestException(errorMsg);
					}
				}
			}
		}
		if (registrationDetails.getApplicantDetails().getPresentAddressFrom() != null) {
			registrationDetails.getApplicantDetails()
					.setPresentAddressFrom(registrationDetails.getApplicantDetails().getPresentAddressFrom());
		}
		applicantDetailsDTO.setCreatedDate(LocalDateTime.now());
		updateApplicantDetails(registrationDetailsDTO, registrationDetails, applicantDetailsDTO,
				aadhaarDetailsResponseDTOOpt);
		registrationDetailsDTO.setApplicantDetails(applicantDetailsDTO);
		registrationDetails.getApplicantDetails().setApplicantNo(applicantDetailsDTO.getApplicantNo());
		registrationDetailsDTO.setStageNo(StageEnum.ONE.getNumber());
		registrationDetailsDTO.setFlowDetails(getBaseObj());
		if (null == registrationDetailsDTO.getApplicationNo()) {
			Map<String, String> officeCodeMap = new TreeMap<>();
			officeCodeMap.put("officeCode", registrationDetails.getDealerOfficeCode());
			registrationDetailsDTO.setApplicationNo(sequenceGenerator
					.getSequence(String.valueOf(Sequence.REGISTRATIONAPPNO.getSequenceId()), officeCodeMap));
		}
		applicantDetailsDTO.setRegId(registrationDetailsDTO.getApplicationNo());
		applicantDetailsDAO.save(registrationDetailsDTO.getApplicantDetails());
	}

	private void updateApplicantDetails(StagingRegistrationDetailsDTO registrationDetailsDTO,
			RegistrationDetailsVO registrationDetails, ApplicantDetailsDTO applicantDetailsDTO,
			Optional<AadhaarDetailsResponseDTO> aadhaarDetailsResponseDTOOpt) {
		// Applicant details mapping
		if (null != registrationDetails.getApplicantDetails().getContact())
			applicantDetailsDTO
					.setContact(contactMapper.convertVO(registrationDetails.getApplicantDetails().getContact()));
		if (null != registrationDetails.getApplicantDetails().getPresentAddress())
			applicantDetailsDTO.setPresentAddress(
					applicantAddressMapper.convertVO(registrationDetails.getApplicantDetails().getPresentAddress()));

		if (null != registrationDetails.getApplicantDetails().getAadharResponse()
				&& aadhaarDetailsResponseDTOOpt.isPresent()) {
			applicantDetailsDTO.setAadharResponse(aadhaarDetailsResponseDTOOpt.get());
		}
		
		if (null != registrationDetails.getApplicantDetails().getIsAvailablePresentAddrsProof()
				&& registrationDetails.getApplicantDetails().getIsAvailablePresentAddrsProof())
			applicantDetailsDTO.setIsAvailablePresentAddrsProof(
					registrationDetails.getApplicantDetails().getIsAvailablePresentAddrsProof());

		if (null != registrationDetails.getApplicantDetails().getPresentAddrsProofBelongsTo())
			applicantDetailsDTO.setPresentAddrsProofBelongsTo(
					registrationDetails.getApplicantDetails().getPresentAddrsProofBelongsTo());

		if (null != registrationDetails.getApplicantDetails().getNameOfPresentAddrsProofBelongsTo())
			applicantDetailsDTO.setNameOfPresentAddrsProofBelongsTo(
					registrationDetails.getApplicantDetails().getNameOfPresentAddrsProofBelongsTo());

		if (null != registrationDetails.getApplicantDetails().getDisplayName()) {
			applicantDetailsDTO.setDisplayName(registrationDetails.getApplicantDetails().getDisplayName());
		} else if (registrationDetails.getOwnerType().equals(OwnerTypeEnum.Company)
				|| registrationDetails.getOwnerType().equals(OwnerTypeEnum.Organization)
				|| registrationDetails.getOwnerType().equals(OwnerTypeEnum.Government)) {
			applicantDetailsDTO.setDisplayName(registrationDetails.getApplicantDetails().getEntityName());
		}

		if (null != registrationDetails.getApplicantDetails().getBloodGrp())
			applicantDetailsDTO
					.setBloodGrp(bloodGroupMapper.convertVO(registrationDetails.getApplicantDetails().getBloodGrp()));

		if (null != registrationDetails.getApplicantDetails().getQualification())
			applicantDetailsDTO.setQualification(
					qualificationMapper.convertVO(registrationDetails.getApplicantDetails().getQualification()));

		if (null != registrationDetails.getApplicantDetails().getAadharNo()) {
			applicantDetailsDTO.setAadharNo(registrationDetails.getApplicantDetails().getAadharNo());
		}

		if (null != registrationDetails.getApplicantDetails().getIsDifferentlyAbled()) {
			applicantDetailsDTO
					.setIsDifferentlyAbled(registrationDetails.getApplicantDetails().getIsDifferentlyAbled());
		}

		if (null != registrationDetails.getApplicantDetails().getIsAadhaarValidated()) {
			applicantDetailsDTO
					.setIsAadhaarValidated(registrationDetails.getApplicantDetails().getIsAadhaarValidated());
		}
		if (null != registrationDetails.getApplicantDetails().getFatherName()) {
			applicantDetailsDTO.setFatherName(registrationDetails.getApplicantDetails().getFatherName());
		}
		if (null != registrationDetails.getApplicantDetails().getDateOfBirth()) {
			applicantDetailsDTO.setDateOfBirth(registrationDetails.getApplicantDetails().getDateOfBirth());
		}
		if (null != registrationDetails.getApplicantDetails().getFirstName()) {
			applicantDetailsDTO.setFirstName(registrationDetails.getApplicantDetails().getFirstName());
		}
		if (null != registrationDetails.getApplicantDetails().getGender()) {
			applicantDetailsDTO.setGender(registrationDetails.getApplicantDetails().getGender());
		}

		if (null != registrationDetails.getApplicantDetails().getEntityName()) {
			applicantDetailsDTO.setEntityName(registrationDetails.getApplicantDetails().getEntityName());
		}

		if (null != registrationDetails.getApplicantDetails().getRepresentativeName()
				&& null != registrationDetails.getApplicantDetails().getEntityName()) {
			applicantDetailsDTO
					.setRepresentativeName(registrationDetails.getApplicantDetails().getRepresentativeName());
		}

		if (null != registrationDetails.getApplicantDetails().getSameAsAadhar()) {
			applicantDetailsDTO.setSameAsAadhar(registrationDetails.getApplicantDetails().getSameAsAadhar());
		}

		if (null != registrationDetails.getApplicantDetails().getPresentAddressFrom()) {
			applicantDetailsDTO
					.setPresentAddressFrom(registrationDetails.getApplicantDetails().getPresentAddressFrom());
		}

		applicantDetailsDTO.setNationality("INDIAN");
	}

	@Override
	public String getTransactionId() {
		return sequenceGenerator.getSequence(String.valueOf(Sequence.APPLICANT.getSequenceId()), null);
	}

	private void updateRegistrationDetailsBasedOnStageNoTwo(StagingRegistrationDetailsDTO registrationDetailsDTO,
			RegistrationDetailsVO registrationDetails) {
		if (null != registrationDetails.getIsTrailer() && registrationDetails.getIsTrailer()) {
			registrationDetailsDTO = saveTrailerData(registrationDetails.getVahanDetails(), registrationDetails,
					registrationDetailsDTO);
			registrationDetailsDTO.setIsTrailer(registrationDetails.getIsTrailer());

		} else {
			vahanValidation(registrationDetailsDTO, registrationDetails);
		}
		officeDetailsUpdation(registrationDetailsDTO, registrationDetails);
		if (null != registrationDetails.getClassOfVehicleDesc()) {
			registrationDetailsDTO.setClassOfVehicleDesc(registrationDetails.getClassOfVehicleDesc());
		}
		if (null != registrationDetails.getClassOfVehicle()) {

			seatValidationForCov(registrationDetailsDTO, registrationDetails);

			registrationDetailsDTO.setClassOfVehicle(registrationDetails.getClassOfVehicle());

			MasterCovDTO masterCOVDTO = masterCovDAO.findByCovcode(registrationDetails.getClassOfVehicle());

			// BS Validation
			bs3Validation(registrationDetailsDTO, masterCOVDTO);

			if (null != masterCOVDTO) {
				registrationDetailsDTO.setIsPanRequired(masterCOVDTO.getPanrequired());
				registrationDetails.setIsPanRequired(masterCOVDTO.getPanrequired());
			} else {
				logger.debug(appMessages.getLogMessage(MessageKeys.LOG_DEALER_INVALIDCOVTYPE),
						registrationDetails.getClassOfVehicle());
				logger.error(appMessages.getResponseMessage(MessageKeys.DEALER_INVALIDCOVTYPE));
				throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.DEALER_INVALIDCOVTYPE));
			}

		} else

			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.MESSAGE_REG_UNDEFINED));

		registrationDetailsDTO.setStageNo(StageEnum.TWO.getNumber());

	}

	/**
	 * @param registrationDetailsDTO
	 * @param registrationDetails
	 */
	private void seatValidationForCov(StagingRegistrationDetailsDTO registrationDetailsDTO,
			RegistrationDetailsVO registrationDetails) {
		try {
			Optional<PropertiesDTO> propertiesDto = propertiesDAO.findByStatusTrue();
			if (propertiesDto.isPresent()) {
				Map<String, Integer> seatCapCov = propertiesDto.get().getSeatCapacityValidateCOV();
				if (seatCapCov.containsKey(registrationDetails.getClassOfVehicle())
						&& seatCapCov.get(registrationDetails.getClassOfVehicle()) > 0
						&& (Integer.valueOf(registrationDetailsDTO.getVahanDetails().getSeatingCapacity()) > seatCapCov
								.get(registrationDetails.getClassOfVehicle()))) {
					logger.info("Seat Capacity Not applicable for applied COV.[{}]",
							registrationDetails.getApplicationNo());
					throw new BadRequestException("Seat Capacity Not applicable for applied COV.");
				}
			}
		} catch (Exception e) {
			logger.error("Exception [{}] for appNo [{}]", e.getMessage(), registrationDetails.getApplicationNo());
			throw new BadRequestException(e.getMessage());
		}

	}

	/**
	 * @param registrationDetailsDTO
	 * @param registrationDetails
	 */
	private void vahanValidation(StagingRegistrationDetailsDTO registrationDetailsDTO,
			RegistrationDetailsVO registrationDetails) {
		try {
			if (null != registrationDetails.getVahanDetails()) {

				if (validateVahanDetailsInStagingRegistrationDetails(
						registrationDetails.getVahanDetails().getEngineNumber(),
						registrationDetails.getVahanDetails().getChassisNumber(), registrationDetails)
						|| validateVahanDetailsInRegistrationDetails(
								registrationDetails.getVahanDetails().getEngineNumber(),
								registrationDetails.getVahanDetails().getChassisNumber(), registrationDetails)) {

					logger.debug("Input Engine No[{}] or ChasisNo[{}] is Using By Other Vehicle.",
							registrationDetails.getVahanDetails().getEngineNumber(),
							registrationDetails.getVahanDetails().getChassisNumber());
					throw new BadRequestException("Engine No Or Chasis No is Using By Other Vehicle.");

				}

				Optional<VahanDetailsDTO> resultFromDB = validateClassVehicleType(
						registrationDetails.getVahanDetails().getEngineNumber(),
						registrationDetails.getVahanDetails().getChassisNumber(),
						registrationDetails.getDealerDetails().getDealerId());

				if (resultFromDB.isPresent()) {
					registrationDetailsDTO.setVahanDetails(resultFromDB.get());
				} else {
					logger.error(appMessages.getResponseMessage(MessageKeys.DEALER_VAHANDETAILSNOTMAPPED));
					throw new BadRequestException(
							appMessages.getResponseMessage(MessageKeys.DEALER_VAHANDETAILSNOTMAPPED));
				}
				registrationDetailsDTO.getVahanDetails()
						.setDealerSelectedMakerName(registrationDetails.getVahanDetails().getDealerSelectedMakerName());
				registrationDetailsDTO.getVahanDetails().setDealerSelectedMakerClass(
						registrationDetails.getVahanDetails().getDealerSelectedMakerClass());
				registrationDetailsDTO.getVahanDetails()
						.setDealerSelectedBodyType(registrationDetails.getVahanDetails().getDealerSelectedBodyType());
				if (ClassOfVehicleEnum.ATCHN.getCovCode().equalsIgnoreCase(registrationDetails.getClassOfVehicle())
						|| ClassOfVehicleEnum.SPHN.getCovCode()
								.equalsIgnoreCase(registrationDetails.getClassOfVehicle())
						|| ClassOfVehicleEnum.TMRN.getCovCode()
								.equalsIgnoreCase(registrationDetails.getClassOfVehicle())) {
					if (registrationDetails.getVahanDetails().getHarvestersDetails() == null) {
						logger.error(
								appMessages.getResponseMessage(MessageKeys.CLASSOFVEHICLEVALIDATIONFOR_ATCHN_SPHN));
						throw new BadRequestException(
								appMessages.getResponseMessage(MessageKeys.CLASSOFVEHICLEVALIDATIONFOR_ATCHN_SPHN));
					}
					HarvestersDetailsDTO harvestersDetails = new HarvestersDetailsDTO();
					harvestersDetails.setHarvestersMakerClass(
							registrationDetails.getVahanDetails().getHarvestersDetails().getHarvestersMakerClass());
					harvestersDetails.setHarvestersMakerName(
							registrationDetails.getVahanDetails().getHarvestersDetails().getHarvestersMakerName());
					harvestersDetails.setUlw(registrationDetails.getVahanDetails().getHarvestersDetails().getUlw());
					harvestersDetails.setRlw(registrationDetails.getVahanDetails().getHarvestersDetails().getRlw());
					harvestersDetails.setChassisNumber(
							registrationDetails.getVahanDetails().getHarvestersDetails().getChassisNumber());
					registrationDetailsDTO.getVahanDetails().setHarvestersDetails(harvestersDetails);
				}

			}

		} catch (Exception e) {
			logger.error("Exception occured [{}]", e.getMessage());

			throw new BadRequestException(e.getMessage());
		}
	}

	public boolean validateVahanDetailsInStagingRegistrationDetails(String engineNo, String chasisNo,
			RegistrationDetailsVO registrationDetails) {
		Optional<StagingRegistrationDetailsDTO> stagingRegistrationDetailsDTO = stagingRegistrationDetailsDAO
				.findByVahanDetailsEngineNumberAndVahanDetailsChassisNumber(engineNo, chasisNo);

		if (stagingRegistrationDetailsDTO.isPresent() && !registrationDetails.getApplicationNo()
				.equals(stagingRegistrationDetailsDTO.get().getApplicationNo()))
			return true;
		else
			return false;
	}

	public boolean validateVahanDetailsInRegistrationDetails(String engineNo, String chasisNo,
			RegistrationDetailsVO registrationDetails) {
		Optional<RegistrationDetailsDTO> stagingRegistrationDetailsDTO = registrationDetailDAO
				.findTopByVahanDetailsChassisNumberAndVahanDetailsEngineNumberOrderByLUpdateDesc(chasisNo, engineNo);
		if (stagingRegistrationDetailsDTO.isPresent() && !registrationDetails.getApplicationNo()
				.equals(stagingRegistrationDetailsDTO.get().getApplicationNo()))
			return true;
		else
			return false;
	}

	/**
	 * @param registrationDetailsDTO
	 * @param registrationDetailsVO
	 */
	private void officeDetailsUpdation(StagingRegistrationDetailsDTO registrationDetailsDTO,
			RegistrationDetailsVO registrationDetailsVO) {
		try {
			Optional<OfficeVO> officeVO = Optional.empty();
			Integer mandalId = 0;
			if (null != registrationDetailsVO.getApplicantDetails().getPresentAddress().getState()
					&& registrationDetailsVO.getApplicantDetails().getPresentAddress().getState().getStateId()
							.equals("AP")
					&& null != registrationDetailsVO.getApplicantDetails().getPresentAddress().getMandal()) {
				mandalId = registrationDetailsVO.getApplicantDetails().getPresentAddress().getMandal().getMandalCode();
			} else {
				mandalId = 99999;
			}

			if (null != registrationDetailsVO.getVehicleType() && null != registrationDetailsVO.getOwnerType()) {
				if (registrationDetailsVO.getVehicleType().equals(CovCategory.N.getCode())) {
					officeVO = mandalService.getOfficeDetailsByMandal(mandalId, CovCategory.N.getCode(),
							registrationDetailsVO.getOwnerType().toString());
				} else if (registrationDetailsVO.getVehicleType().equals(CovCategory.T.getCode())) {
					officeVO = mandalService.getOfficeDetailsByMandal(mandalId, CovCategory.T.getCode(),
							registrationDetailsVO.getOwnerType().toString());
				} else {
					throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.MESSAGE_REG_UNDEFINED));
				}
				if (!officeVO.isPresent()) {
					throw new BadRequestException(
							appMessages.getResponseMessage(MessageKeys.DEALER_OFFICEDETAILSNOTAVAILABLE));
				} else {
					if (null != registrationDetailsDTO)
						registrationDetailsDTO.setOfficeDetails(officeMapper.convertVO(officeVO.get()));
					registrationDetailsVO.setOfficeDetails(officeVO.get());
				}
			}
			if (null != registrationDetailsDTO) {
				if (null != registrationDetailsVO.getVehicleType()) {
					registrationDetailsDTO.setVehicleType(registrationDetailsVO.getVehicleType());
				}

				if (null != registrationDetailsVO.getIsSecondVehicleDisplayCheck()) {
					registrationDetailsDTO
							.setIsSecondVehicleDisplayCheck(registrationDetailsVO.getIsSecondVehicleDisplayCheck());
				}

				if (null != registrationDetailsVO.getTaxType()) {
					registrationDetailsDTO.setTaxType(registrationDetailsVO.getTaxType());
				}

			}

		} catch (Exception e) {
			throw new BadRequestException("Unable to update office details.");
		}
	}

	/**
	 * @param registrationDetailsDTO
	 * @param masterCOVDTO
	 */
	private void bs3Validation(StagingRegistrationDetailsDTO registrationDetailsDTO, MasterCovDTO masterCOVDTO) {
		try {
			Optional<BsiiiAllowedDTO> bsiiiAllowedDTO = bsiiiAllowedDAO.findByChassisNoAndEngineNo(
					registrationDetailsDTO.getVahanDetails().getChassisNumber(),
					registrationDetailsDTO.getVahanDetails().getEngineNumber());
			Optional<BharathStageDTO> bharathStageDTO = bharathStageDAO
					.findByBharathStage(registrationDetailsDTO.getVahanDetails().getPollutionNormsDesc());
			if ((bsiiiAllowedDTO.isPresent() && !bsiiiAllowedDTO.get().isStatus())) {
				throw new BadRequestException("Applied BS Type is not allowed, Please verify Once.");
			} else if (!bsiiiAllowedDTO.isPresent()
					&& ((null == registrationDetailsDTO.getVahanDetails().getPollutionNormsDesc()
							|| null != registrationDetailsDTO.getVahanDetails().getPollutionNormsDesc())
							&& ((bharathStageDTO.isPresent() && !bharathStageDTO.get().isBsAllowed())
									&& !masterCOVDTO.getBsAllowed()))) {
				throw new BadRequestException("Applied BS Type is not allowed, Please verify Once.");
			}
		} catch (Exception e) {
			logger.error("Applied BS Type is not allowed [{}]", e.getMessage());
			throw new BadRequestException("Applied BS Type is not allowed, Please verify.");
		}
	}

	private void updateRegistrationDetailsBasedOnStageNoThree(StagingRegistrationDetailsDTO registrationDetailsDTO,
			RegistrationDetailsVO registrationDetails) {

		if (null == registrationDetails.getInvoiceDetails()) {
			logger.error(appMessages.getResponseMessage(MessageKeys.ERROR_REG_DETILSNOTFOUND));

			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.ERROR_REG_DETILSNOTFOUND));
		}
		invoiceValidation(registrationDetailsDTO, registrationDetails);

		registrationDetailsDTO
				.setInvoiceDetails(invoiceDetailsMapper.convertVO(registrationDetails.getInvoiceDetails()));

		if (null != registrationDetails.getSpecialNumberRequired())
			registrationDetailsDTO.setSpecialNumberRequired(registrationDetails.getSpecialNumberRequired());

		if ((null != registrationDetails.getIsFirstVehicle() && !registrationDetails.getIsFirstVehicle())
				|| registrationDetailsDTO.getInvoiceDetails().getInvoiceValue() > 1000000) {
			registrationDetailsDTO.setIsFirstVehicle(registrationDetails.getIsFirstVehicle());
		} else {
			registrationDetailsDTO.setIsFirstVehicle(registrationDetails.getIsFirstVehicle());

			secondVehicleSearingStaging(registrationDetailsDTO, registrationDetails);
		}

		if (registrationDetailsDTO.getIsPanRequired()) {
			if (null != registrationDetails.getPanDetails()) {
				registrationDetailsDTO.setPanDetails(panDetailsMapper.convertVO(registrationDetails.getPanDetails()));
			} else {
				logger.error(appMessages.getResponseMessage(MessageKeys.DEALER_PANDETAILSREQUIRED));
				throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.DEALER_PANDETAILSREQUIRED));
			}
		}
		if (null != registrationDetailsDTO.getFinanceDetails()) {
			registrationDetails.setIsFinancier(true);
			registrationDetails
					.setFinanceDetails(financeDetailsMapper.convertEntity(registrationDetailsDTO.getFinanceDetails()));
		}

		registrationDetailsDTO.setStageNo(StageEnum.THREE.getNumber());

	}

	/**
	 * @param registrationDetailsDTO
	 * @param registrationDetails
	 */
	private void invoiceValidation(StagingRegistrationDetailsDTO registrationDetailsDTO,
			RegistrationDetailsVO registrationDetails) {
		try {
			if (null != registrationDetailsDTO.getVahanDetails()
					&& null != registrationDetailsDTO.getVahanDetails().getExShowroomPrice()) {
				if ((registrationDetails.getInvoiceDetails().getInvoiceValue() < registrationDetailsDTO
						.getVahanDetails().getExShowroomPrice())) {
					throw new BadRequestException(
							appErrors.getResponseMessage(MessageKeys.DEALER_PRICESHOULDMORETHANEXSHOWPRICE));
				}
				if (null != registrationDetailsDTO.getVahanDetails().getExShowroomPrice()
						&& registrationDetailsDTO.getVahanDetails().getExShowroomPrice() > 0
						&& (registrationDetails.getInvoiceDetails()
								.getInvoiceValue() > registrationDetailsDTO.getVahanDetails().getExShowroomPrice()
										+ registrationDetailsDTO.getVahanDetails().getExShowroomPrice()
												* Float.valueOf(invoiceValueValidation) / 100)) {
					throw new BadRequestException("Invoice value should not exceed than of " + invoiceValueValidation
							+ "% of exshweroom price  in addtion to  exshweroom price ");
				}
			} else {
				throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.DEALER_VAHANDETAILSREQUIRED));
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new BadRequestException(e.getMessage());
		}
	}

	/**
	 * @param registrationDetailsDTO
	 * @param registrationDetails
	 */
	private void secondVehicleSearingStaging(StagingRegistrationDetailsDTO registrationDetailsDTO,
			RegistrationDetailsVO registrationDetails) {
		registrationDetails.setSecondVehicleMessage(null);
		if (registrationDetails.getOwnerType().equals(OwnerTypeEnum.Individual)) {
			MasterCovDTO masterCOVDTO = masterCovDAO.findByCovcode(registrationDetails.getClassOfVehicle());
			if (null != masterCOVDTO.getIsSecondVehicle() && masterCOVDTO.getIsSecondVehicle()) {
				List<StagingRegistrationDetailsDTO> resultForAadhaar = stagingRegistrationDetailsDAO
						.findByApplicantDetailsAadharNo(registrationDetailsDTO.getApplicantDetails().getAadharNo());
				resultForAadhaar.stream().forEach(reg -> {
					if (reg.getApplicationStatus().equalsIgnoreCase(StatusRegistration.TRGENERATED.getDescription())
							&& reg.getOwnerType().equals(OwnerTypeEnum.Individual)) {
						MasterCovDTO masterCovDTO = masterCovDAO.findByCovcode(reg.getClassOfVehicle());
						if (null != masterCovDTO && null != masterCovDTO.getIsSecondVehicle()
								&& masterCovDTO.getIsSecondVehicle()) {

							registrationDetails.setSecondVehicleMessage(
									"Second Vehicle found for this applicant with applicationNo :"
											+ reg.getApplicationNo());
							logger.warn(
									"Duplicate Registration Deatils found in Staging [{}], Please choose second vehicle.",
									reg.getApplicationNo());

						}
					}
				});
				resultForAadhaar.clear();
			}
		}
	}

	private void updateRegistrationDetailsBasedOnStageNoFour(StagingRegistrationDetailsDTO registrationDetailsDTO,
			RegistrationDetailsVO registrationDetails) {

		if (null == registrationDetails.getInsuranceDetails()) {
			logger.error(appMessages.getResponseMessage(MessageKeys.ERROR_REG_EMPTY));
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.ERROR_REG_EMPTY));
		}
		insuranceValidation(registrationDetails);

		registrationDetails.getInsuranceDetails().setValidTill(registrationDetails.getInsuranceDetails().getValidFrom()
				.plusYears(registrationDetails.getInsuranceDetails().getTenure()).minusDays(1));

		registrationDetailsDTO
				.setInsuranceDetails(insuranceMapper.convertVO(registrationDetails.getInsuranceDetails()));

		if (null != registrationDetails.getIsFinancier())
			registrationDetailsDTO.setIsFinancier(registrationDetails.getIsFinancier());

		if (null != registrationDetails.getInsuranceType())
			registrationDetailsDTO.setInsuranceType(registrationDetails.getInsuranceType());

		registrationDetailsDTO.setStageNo(StageEnum.FOUR.getNumber());

	}

	/**
	 * @param registrationDetails
	 */
	private void insuranceValidation(RegistrationDetailsVO registrationDetails) {
		try {
			List<StagingRegistrationDetailsDTO> resultForInsuarance = stagingRegistrationDetailsDAO
					.findByInsuranceDetailsCompanyAndInsuranceDetailsPolicyNumber(
							registrationDetails.getInsuranceDetails().getCompany(),
							registrationDetails.getInsuranceDetails().getPolicyNumber());

			if (null != resultForInsuarance && !resultForInsuarance.isEmpty()) {
				resultForInsuarance.stream().forEach(stagingRegistrationDetailsDTO -> {
					if (stagingRegistrationDetailsDTO.getInsuranceDetails().getCompany()
							.equals(registrationDetails.getInsuranceDetails().getCompany())
							&& stagingRegistrationDetailsDTO.getInsuranceDetails().getPolicyNumber()
									.equals(registrationDetails.getInsuranceDetails().getPolicyNumber())
							&& !stagingRegistrationDetailsDTO.getApplicationNo()
									.equals(registrationDetails.getApplicationNo())) {
						logger.debug(
								"Insurance Deatils is matching with other application details [{}], Please verify Once",
								stagingRegistrationDetailsDTO.getApplicationNo());
						throw new BadRequestException(
								"Duplicate insurance details." + stagingRegistrationDetailsDTO.getApplicationNo());
					}
				});

			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new BadRequestException(e.getMessage());
		}
	}

	private void updateRegistrationDetailsBasedOnStageNoFive(StagingRegistrationDetailsDTO registrationDetailsDTO,
			RegistrationDetailsVO registrationDetails) {
		if (null == registrationDetailsDTO) {
			logger.error(appMessages.getResponseMessage(MessageKeys.ERROR_REG_UPDATE_FAILED));
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.ERROR_REG_UPDATE_FAILED));
		}

		registrationDetailsDTO.setStageNo(StageEnum.FIVE.getNumber());
		return;

	}

	@Override
	public Optional<RegistrationDetailsVO> getRegistrationDetailByApplicationNo(String appNo) {
		return registrationDetailsMapper.convertEntity(stagingRegistrationDetailsDAO.findByApplicationNo(appNo));
	}

	@Override
	public void doAction(RegistrationDetailsVO registrationDetailsVO) {
		Optional<StagingRegistrationDetailsDTO> opRegistrationDetails = stagingRegistrationDetailsDAO
				.findByApplicationNo(registrationDetailsVO.getApplicationNo());
		if (!opRegistrationDetails.isPresent()) {
			// Throws Error ( Application Number Not Found)
		}

		if (verifyRole()) {
		} else {
		}
		/*
		 * FlowDTO flowDetails = opRegistrationDetails.get().getFlowDetails(); if
		 * (flowDetails == null && flowDetails.getFlowDetails().size() == 0) { // Throws
		 * Error ( Flow Details Not Exist) } Optional<Integer> index =
		 * flowDetails.getFlowDetails().keySet().parallelStream().findFirst();
		 */

		// Optional<FlowDTO> firstFlowDetailsLog =
		// opRegistrationDetails.get().getFlowDetails().get(firstFlowDetailsLog.get().getFlowDetails().size()-1);

		// firstFlowDetailsLog.get().+

		// prepareApprovalsActionsNExecute(opRegistrationDetails);

		// Read Role & Verify, If role not matches throw error saying that
		// unauthorized

		// If role success , read previous actions & Prepare for input to script
		// Engine

		// Verify If if alredy approvals finished for given roles. remove from
		// flow DTO.
		// once all approval are fnished, process for PR generation step

	}

	private boolean verifyRole() {
		return false;
	}

	@Override
	public boolean deleteRegistrationDetailById(String id, String userId) {
		Optional<StagingRegistrationDetailsDTO> appNo = stagingRegistrationDetailsDAO.findByApplicationNo(id);
		if (appNo.isPresent()) {
			StagingRegistrationDetailsDTO dto = appNo.get();

			StagingRegistrationDetailsHistoryDTO stagingHistoryDTO = new StagingRegistrationDetailsHistoryDTO();
			stagingHistoryDTO.setStagingDetails(dto);
			ActionDetailsDTO actionDetailsDTO = new ActionDetailsDTO();
			actionDetailsDTO.setActionBy(userId);
			actionDetailsDTO.setActionDatetime(LocalDateTime.now());
			stagingHistoryDTO.setActionDetailsDTO(actionDetailsDTO);
			stagingRegistrationDetailsHistoryDAO.save(stagingHistoryDTO);
			logMovingService.moveStagingToLog(dto.getApplicationNo());
			stagingRegistrationDetailsDAO.delete(dto);
			logger.debug(appMessages.getLogMessage(MessageKeys.DEALER_REGDETAILSDELETED), id);
			return true;
		} else {
			logger.error(appErrors.getResponseMessage(MessageKeys.DEALER_INVALIDAPPLICATIONNO));
			throw new BadRequestException(appErrors.getResponseMessage(MessageKeys.DEALER_INVALIDAPPLICATIONNO));
		}
	}

	@Override
	public void cancelToken(String applicationNo) {
		Optional<StagingRegistrationDetailsDTO> opRegDetails = getRegistrationDetailsByApplicationNo(applicationNo);

		if (opRegDetails.isPresent()) {
			FinanceDetailsDTO financeDetails = opRegDetails.get().getFinanceDetails();

			if (financeDetails == null) {
				financeDetails = new FinanceDetailsDTO();
			}

			/*
			 * if (StringUtils.isNoneBlank(financeDetails.getToken())) {
			 * logger.warn(appMessages.getLogMessage(MessageKeys.
			 * FINANCE_TOKEN_ALREADY_EXIST )); throw new
			 * BadRequestException(appMessages.getResponseMessage(MessageKeys.
			 * FINANCE_TOKEN_ALREADY_EXIST)); }
			 */

			financeDetails.setToken(StringUtils.EMPTY);
			financeDetails.setQuotationValue(0);

			// financeDetails = null;
			if (null != opRegDetails.get().getFinanceDetails()
					&& null != opRegDetails.get().getFinanceDetails().getStatus()
					&& (opRegDetails.get().getFinanceDetails().getStatus()
							.equals(StatusRegistration.DEALERTOKENAPPROVED.getDescription())
							|| opRegDetails.get().getFinanceDetails().getStatus()
									.equals(StatusRegistration.FINANCIERSANCTIONED.getDescription()))) {
				logger.error(appErrors.getResponseMessage(MessageKeys.DEALER_FINANCIERTAKENDECESSION));
				throw new BadRequestException(appErrors.getResponseMessage(MessageKeys.DEALER_FINANCIERTAKENDECESSION));
			} else {
				opRegDetails.get().setFinanceDetails(null);
				opRegDetails.get().setIsFinancier(false);
			}
			logMovingService.moveStagingToLog(opRegDetails.get().getApplicationNo());
			stagingRegistrationDetailsDAO.save(opRegDetails.get());
		} else {
			logger.warn(appMessages.getLogMessage(MessageKeys.FINANCE_APPLICATION_NOT_FOUND), applicationNo);
			logger.error(appMessages.getResponseMessage(MessageKeys.FINANCE_APPLICATION_NOT_FOUND));
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.FINANCE_APPLICATION_NOT_FOUND));
		}
	}

	private Optional<StagingRegistrationDetailsDTO> getRegistrationDetailsByApplicationNo(String applicationNo) {
		return stagingRegistrationDetailsDAO.findByApplicationNo(applicationNo);
	}

	@Override
	public String createToken(String applicationNo, Integer quotationValue) {

		Optional<StagingRegistrationDetailsDTO> opRegDetails = getRegistrationDetailsByApplicationNo(applicationNo);

		String token = String.valueOf(System.currentTimeMillis());

		if (opRegDetails.isPresent()) {
			FinanceDetailsDTO financeDetails = opRegDetails.get().getFinanceDetails();

			if (financeDetails == null) {
				financeDetails = new FinanceDetailsDTO();
			}

			if (StringUtils.isNoneBlank(financeDetails.getToken())) {
				logger.warn(appMessages.getLogMessage(MessageKeys.FINANCE_TOKEN_ALREADY_EXIST));
				logger.error(appMessages.getResponseMessage(MessageKeys.FINANCE_TOKEN_ALREADY_EXIST));
				throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.FINANCE_TOKEN_ALREADY_EXIST));
			}

			financeDetails.setToken(token);
			financeDetails.setQuotationValue(quotationValue);
			opRegDetails.get().setIsFinancier(true);
			opRegDetails.get().setFinanceDetails(financeDetails);
			logMovingService.moveStagingToLog(opRegDetails.get().getApplicationNo());
			stagingRegistrationDetailsDAO.save(opRegDetails.get());
			notifications.sendNotifications(MessageTemplate.FIN_TOKEN_NO.getId(), opRegDetails.get());
		} else {
			logger.warn(appMessages.getLogMessage(MessageKeys.FINANCE_APPLICATION_NOT_FOUND), applicationNo);
			logger.error(appMessages.getResponseMessage(MessageKeys.FINANCE_APPLICATION_NOT_FOUND));
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.FINANCE_APPLICATION_NOT_FOUND));
		}
		return token;
	}

	private boolean compareAadharResponseDetailsAndApplicantAddressDetails(AadhaarDetailsResponseDTO dto,
			ApplicantAddressVO vo) {
		return (dto.getStatecode().equalsIgnoreCase(vo.getState().getStateName())) && (dto.getDistrict()
				.equalsIgnoreCase(vo.getDistrict().getDistrictName())) /*
																		 * && (dto.getMandal(). equals(vo.getMandal() .
																		 * getMandalName())) && (dto.getVillage().
																		 * equals(vo.getVillage( ). getVillageName()))
																		 */
				&& (dto.getPincode().equals(vo.getPostOffice().getPostOfficeCode().toString()));
	}

	@Override
	public String getQRCode(String applicationNo) throws IOException {
		String qrCode = StringUtils.EMPTY;
		try {
			qrCode = qRCodeUtil.createQRCode(applicationNo);
			if (qrCode == null)
				qrCode = StringUtils.EMPTY;
		} catch (Exception ex) {
			logger.error(MessageKeys.MESSAGE_EXCEPTION, ex.getMessage());
		}
		return qrCode;
	}

	public FlowDTO getBaseObj() {

		FlowDTO flowDto = new FlowDTO();

		Map<Integer, List<RoleActionDTO>> details = new TreeMap<Integer, List<RoleActionDTO>>();

		RoleActionDTO r1 = new RoleActionDTO();
		r1.setRole("CCO");
		// r1.setAction("APPROVED");

		RoleActionDTO r2 = new RoleActionDTO();
		r2.setRole("MVI");
		// r2.setAction("APPROVED");

		ArrayList<RoleActionDTO> al = new ArrayList<RoleActionDTO>();
		al.add(r1);
		al.add(r2);

		RoleActionDTO r3 = new RoleActionDTO();
		r3.setRole("AO");
		// r3.setAction("APPROVED");

		ArrayList<RoleActionDTO> a2 = new ArrayList<RoleActionDTO>();
		a2.add(r3);

		RoleActionDTO r4 = new RoleActionDTO();
		r4.setRole("RTO");
		// r4.setAction("APPROVED");

		ArrayList<RoleActionDTO> a3 = new ArrayList<RoleActionDTO>();
		a3.add(r4);

		details.put(1, al);
		details.put(2, a2);
		details.put(3, a3);

		flowDto.setStatus(true);

		flowDto.setFlowDetails(details);

		return flowDto;
	}

	@Override
	public boolean validateUserValidity(Optional<UserDTO> userDetails) {
		if (userDetails.isPresent() && null != userDetails.get().getPrimaryRole()
				&& userDetails.get().getPrimaryRole().getName().equals("DEALER")
				&& null != userDetails.get().getValidTo()) {
			if (userDetails.get().getValidTo().isAfter(LocalDate.now())
					|| userDetails.get().getValidTo().isEqual(LocalDate.now())) {
				return true;
			}

		}

		return false;
	}

	@Override
	public Map<Integer, List<RoleActionDTO>> updateFlowDetailsForRejection(StagingRegistrationDetailsDTO stagingDTO) {

		List<EnclosuresDTO> enclosureDtos = enclosuresDAO.findByServiceID(ServiceEnum.TEMPORARYREGISTRATION.getId());

		Set<String> rolesList = new TreeSet<>();

		for (KeyValue<String, List<ImageEnclosureDTO>> enclosureKeyValue : stagingDTO.getEnclosures()) {

			boolean statue = false;
			for (EnclosuresDTO enclosures : enclosureDtos) {
				Optional<ImageEnclosureDTO> value = enclosureKeyValue.getValue().stream()
						.filter(dto -> dto.getImageType().equalsIgnoreCase(enclosures.getProof())).findFirst();
				if (value.isPresent()) {
					if (!enclosureKeyValue.getValue().stream().anyMatch(status -> status.getImageStaus()
							.equalsIgnoreCase(StatusRegistration.REUPLOAD.getDescription()))) {
						statue = true;
					}
				}
			}

			if (statue) {
				continue;
			}
			Optional<EnclosuresDTO> matchedEnclosure = enclosureDtos.stream()
					.filter(dto -> dto.getProof().equals(enclosureKeyValue.getKey())).findFirst();

			if (!matchedEnclosure.isPresent()) {
				continue;
			}
			rolesList.addAll(matchedEnclosure.get().getBasedOnRole());

		}

		FlowDTO flowDetailsBaseObj = getBaseObj();

		Map<Integer, List<RoleActionDTO>> finaldetails = new TreeMap<>();

		Map<Integer, List<RoleActionDTO>> detailsMap = flowDetailsBaseObj.getFlowDetails();
		logger.info("Application No [{}] befor remove [{}], ", stagingDTO.getApplicationNo(), detailsMap);
		detailsMap.keySet().forEach(val -> {
			List<RoleActionDTO> data = detailsMap.get(val);
			Iterator<RoleActionDTO> itr = data.iterator();

			List<RoleActionDTO> newdata = new ArrayList<RoleActionDTO>();

			while (itr.hasNext()) {

				RoleActionDTO iter = itr.next();
				if (rolesList.contains(iter.getRole())) {

					if (finaldetails.containsKey(val)) {
						finaldetails.get(val).add(iter);
					} else {
						newdata.add(iter);
						finaldetails.put(val, newdata);
					}
				}

			}
		}

		);
		logger.info("Application No [{}] after remove [{}], ", stagingDTO.getApplicationNo(), finaldetails);
		return finaldetails;
	}

	public void saveRejectedlogs(Map<Integer, List<RoleActionDTO>> map, StagingRegistrationDetailsDTO stagingDTO) {
		LogsDTO log = new LogsDTO();
		List<FlowDTO> flowList = new ArrayList<FlowDTO>();
		FlowDTO flowDetails = new FlowDTO();
		flowDetails.setFlowDetails(map);
		log.setApplicationNo(stagingDTO.getApplicationNo());
		log.setFlowDetails(flowDetails);
		flowList.add(flowDetails);
		log.setFlowDetailsLog(flowList);
		logsDAO.save(log);

	}

	private void validateDealerVehicleTypeAndVahanVehicleType(
			List<DealerAndVahanMappedCovDTO> dealerAndVahanMappedCovDTOList, VahanDetailsDTO vahanDetailsDetails) {

		if (null == vahanDetailsDetails.getDealerCovType()) {
			vahanDetailsDetails.setDealerCovType(new ArrayList<String>());
		}
		// dealerAndVahanMappedCovDTOList.stream().forEach(value ->
		for (DealerAndVahanMappedCovDTO value : dealerAndVahanMappedCovDTOList) {
			if (value.getVahanCovType().equalsIgnoreCase(vahanDetailsDetails.getVehicleClass())) {
				if (Integer.parseInt(vahanDetailsDetails.getSeatingCapacity()) <= 6) {
					if (Arrays.asList(ClassOfVehicleEnum.OBPN.getCovCode(), ClassOfVehicleEnum.OBT.getCovCode())
							.contains(value.getDealerCovType())) {
						continue;
					}
				}
				vahanDetailsDetails.getDealerCovType().add(value.getDealerCovType());
			}
		}

	}

	public Optional<VahanDetailsDTO> validateClassVehicleType(String engineNo, String chasisNo, String userId) {

		List<VahanDetailsDTO> vahanDetailsList = vahanDAO.findByChassisNumberOrEngineNumber(chasisNo, engineNo);

		Optional<VahanDetailsDTO> resultFromDB = Optional.empty();
		if (!vahanDetailsList.isEmpty()) {
			vahanDetailsList = vahanDetailsList.stream().map(VahanDetailsDTO::setCreatedeDateForObj)
					.collect(Collectors.toList());
			vahanDetailsList.sort((v1, v2) -> v2.getCreatedeDate().compareTo(v1.getCreatedeDate()));
			resultFromDB = vahanDetailsList.stream().findFirst();

			VahanDetailsDTO dto = resultFromDB.get();
			if (StringUtils.isEmpty(dto.getManufacturedMonthYear()) || dto.getManufacturedMonthYear().equals("000000")
					|| dto.getManufacturedMonthYear().substring(0, 2).equals("00")
					|| dto.getManufacturedMonthYear().substring(2, 6).equals("0000")) {
				String errorMsg = "Zeros retrieved for Month/Year field from Vahan, please make sure details are updated in Vahan.";
				logger.debug(errorMsg + "chasisNo[{}],engineNo[{}]", chasisNo, engineNo);
				logger.error(errorMsg);
				throw new BadRequestException(errorMsg);
			}

		}
		Optional<UserDTO> userDetails = userDAO.findByUserId(userId);
		if (userDetails.isPresent()) {
			List<DealerCovDTO> dealerCovDTOList = dealerCovDAO.findByRId(userDetails.get().getRid());
			List<String> dealerCovTypeList = new ArrayList<>();
			dealerCovDTOList.forEach(dealerCovType -> {
				dealerCovTypeList.add(dealerCovType.getCovId());
			});
			List<DealerAndVahanMappedCovDTO> dealerAndVahanMappedCovDTOList = dealerAndVahanMappedCovDAO
					.findByDealerCovTypeIn(dealerCovTypeList);
			validateDealerVehicleTypeAndVahanVehicleType(dealerAndVahanMappedCovDTOList, resultFromDB.get());
			dealerCovDTOList.clear();
			dealerAndVahanMappedCovDTOList.clear();

			if (null != resultFromDB.get().getDealerCovType()) {
				logger.info("Validated Vahan Class Type [{}].", resultFromDB.get().getVehicleClass());
				return resultFromDB;
			} else {
				String errorMsg = "Dealer is unsupported vehicle class type, Please verify once.";
				logger.error(errorMsg);
				throw new BadRequestException(errorMsg);
			}

		}
		return resultFromDB;
	}

	@Override
	public void updatePaymentStatusOfApplicant(Optional<StagingRegistrationDetailsDTO> registrationDetails,
			String transactionNo, String status) {
		StagingRegistrationDetailsDTO stagingRegistrationdetails = registrationDetails.get();
		stagingRegistrationdetails.setPaymentTransactionNo(transactionNo);
		stagingRegistrationdetails.setApplicationStatus(status);
		// logMovingService.moveStagingToLog(stagingRegistrationdetails.getApplicationNo());
		// stagingRegistrationDetailsDAO.save(stagingRegistrationdetails);
	}

	@Override
	public void updatePaymentStatusOfSecondOrInvalidTax(Optional<StagingRegistrationDetailsDTO> registrationDetails,
			String transactionNo) {
		StagingRegistrationDetailsDTO stagingRegistrationdetails = registrationDetails.get();
		stagingRegistrationdetails.setPaymentTransactionNo(transactionNo);
		stagingRegistrationdetails.setApplicationStatus(StatusRegistration.SECORINVALIDPENDING.getDescription());
		stagingRegistrationDetailsDAO.save(stagingRegistrationdetails);
	}

	@Override
	public void updatePaymentTransactionNoFoFailedTransactions(
			Optional<StagingRegistrationDetailsDTO> registrationDetails, String transactionNo) {
		StagingRegistrationDetailsDTO stagingRegistrationdetails = registrationDetails.get();
		stagingRegistrationdetails.setPaymentTransactionNo(transactionNo);

	}

	@Override
	public Optional<RegistrationDetailsVO> getRegistrationDetailsBasedOnTroraadhaarNo(RequestDetailsVO requestDetailsVO,
			String userId) {
		// TODO Auto-generated method stub

		if (!StringUtils.isEmpty(requestDetailsVO.getApplicationNo())) {
			RegistrationDetailsDTO registrationDetailsDTO = registrationDetailDAO
					.findByDealerDetailsDealerIdAndApplicationNo(userId, requestDetailsVO.getApplicationNo());
			if (registrationDetailsDTO != null) {
				return Optional.of(registrationMapper.convertEntity(registrationDetailsDTO));
			} else {
				Optional<StagingRegistrationDetailsDTO> stagingOptional = stagingRegistrationDetailsDAO
						.findByDealerDetailsDealerIdAndApplicationNo(userId, requestDetailsVO.getApplicationNo());
				if (stagingOptional.isPresent()) {
					return Optional.of(registrationDetailsMapper.convertEntity(stagingOptional.get()));
				}
			}
			return Optional.empty();
		}
		if (!StringUtils.isEmpty(requestDetailsVO.getTrNo())) {
			RegistrationDetailsDTO registrationDetailsDTO = registrationDetailDAO
					.findByDealerDetailsDealerIdAndTrNo(userId, requestDetailsVO.getTrNo());
			if (registrationDetailsDTO != null) {
				return Optional.of(registrationMapper.convertEntity(registrationDetailsDTO));
			} else {
				Optional<StagingRegistrationDetailsDTO> stagingOptional = stagingRegistrationDetailsDAO
						.findByDealerDetailsDealerIdAndTrNo(userId, requestDetailsVO.getTrNo());
				if (stagingOptional.isPresent()) {
					return Optional.of(registrationDetailsMapper.convertEntity(stagingOptional.get()));
				}
			}
			return Optional.empty();
		}
		if (!StringUtils.isEmpty(requestDetailsVO.getChassisNo())
				&& !StringUtils.isEmpty(requestDetailsVO.getEngineNo())) {
			RegistrationDetailsDTO registrationDetailsDTO = registrationDetailDAO
					.findByDealerDetailsDealerIdAndVahanDetailsChassisNumberAndVahanDetailsEngineNumber(userId,
							requestDetailsVO.getChassisNo(), requestDetailsVO.getEngineNo());
			if (registrationDetailsDTO != null) {
				return Optional.of(registrationMapper.convertEntity(registrationDetailsDTO));
			} else {
				Optional<StagingRegistrationDetailsDTO> stagingOptional = stagingRegistrationDetailsDAO
						.findByDealerDetailsDealerIdAndVahanDetailsChassisNumberAndVahanDetailsEngineNumber(userId,
								requestDetailsVO.getChassisNo(), requestDetailsVO.getEngineNo());
				if (stagingOptional.isPresent()) {
					return Optional.of(registrationDetailsMapper.convertEntity(stagingOptional.get()));
				}
			}
			return Optional.empty();
		} /** returning null if it is not in the any above cases **/
		return null;
	}

	@Override
	public Boolean updateReuploadEnclosureFronDealer(RequestDetailsVO requestDetailsVO, String userId) {

		Optional<StagingRegistrationDetailsDTO> opRegDetails = getRegistrationDetailsByApplicationNo(
				requestDetailsVO.getApplicationNo());

		if (opRegDetails.isPresent() && (opRegDetails.get().getApplicationStatus()
				.equals(StatusRegistration.REUPLOAD.getDescription())
				|| opRegDetails.get().getApplicationStatus().equals(StatusRegistration.REJECTED.getDescription()))) {
			StagingRegistrationDetailsDTO stageDTO = opRegDetails.get();
			FlowDTO flowDetails = new FlowDTO();
			flowDetails.setFlowDetails(updateFlowDetailsForRejection(stageDTO));
			stageDTO.setFlowDetails(flowDetails);
			if (opRegDetails.get().isRejectedByEnclosure() && (null == opRegDetails.get().getRejectionHistory()
					|| ((null == opRegDetails.get().getRejectionHistory().getIsSecondVehicleRejected()
							|| (null != opRegDetails.get().getRejectionHistory().getIsSecondVehicleRejected()
									&& !opRegDetails.get().getRejectionHistory().getIsSecondVehicleRejected()))
							&& (null == opRegDetails.get().getRejectionHistory().getIsInvalidVehicleRejection()
									|| (null != opRegDetails.get().getRejectionHistory().getIsInvalidVehicleRejection()
											&& !opRegDetails.get().getRejectionHistory()
													.getIsInvalidVehicleRejection()))))) {

				stageDTO.setApplicationStatus(StatusRegistration.DEALERRESUBMISSION.getDescription());
				stageDTO.setAutoApprovalInitiatedDate(LocalDate.now());
			}
			if (stageDTO.isIterationFlag()) {
				if (stageDTO.getIteration() != null) {
					stageDTO.setIterationFlag(Boolean.FALSE);
					stageDTO.setIteration(stageDTO.getIteration() + 1);
				} else {
					// For migration data
					stageDTO.setIteration(2);
					stageDTO.setIterationFlag(Boolean.FALSE);
				}
			}
			logMovingService.moveStagingToLog(stageDTO.getApplicationNo());
			stagingRegistrationDetailsDAO.save(stageDTO);
			try {
				if (checkApprovalsNeedorNot(stageDTO)) {
					stageDTO.setApprovedByAutomation(Boolean.TRUE);
					if (stageDTO.getSpecialNumberRequired() && StringUtils.isBlank(stageDTO.getPrNo())) {
						stageDTO.setApplicationStatus(StatusRegistration.SPECIALNOPENDING.getDescription());
						rtaService.updateStagingRegDetails(stageDTO);
					} else if (prSeriesService.isAssignNumberNow()) {
						rtaService.processPR(stageDTO);
						// TODO: Maintain diff status to generate pr's on night schedulers.
					}
				}
			} catch (CloneNotSupportedException e) {
				logger.error("Auto generation failed,Please contact support team");
				throw new BadRequestException("Auto generation failed,Please contact support team");
			}
			return true;

		} else {
			logger.debug("Unable to submit rejection application, Application No:",
					requestDetailsVO.getApplicationNo());
			logger.error("Unable to submit rejection application, Application No: [{}]",
					requestDetailsVO.getApplicationNo());
			throw new BadRequestException(
					"Unable to submit rejection application, Application No:" + requestDetailsVO.getApplicationNo());
		}
	}

	@Override
	public ResponseDetailsVO getSVSStatus(String applicationNo, String userId) {
		Optional<StagingRegistrationDetailsDTO> opRegDetails = getRegistrationDetailsByApplicationNo(applicationNo);

		if (opRegDetails.isPresent()) {
			ResponseDetailsVO responseDetailsVO = new ResponseDetailsVO();
			if (null != opRegDetails.get().getRejectionHistory()
					&& null != opRegDetails.get().getRejectionHistory().getIsSecondVehicleRejected())
				responseDetailsVO.setSecondVehicleRejected(
						opRegDetails.get().getRejectionHistory().getIsSecondVehicleRejected());
			if (null != opRegDetails.get().getRejectionHistory()
					&& null != opRegDetails.get().getRejectionHistory().getIsInvalidVehicleRejection())
				responseDetailsVO.setInvalidVehicleRejection(
						opRegDetails.get().getRejectionHistory().getIsInvalidVehicleRejection());

			opRegDetails.get().getEnclosures().forEach(imageInput -> {
				if (imageInput.getValue().stream().anyMatch(status -> status.getImageStaus()
						.equalsIgnoreCase(StatusRegistration.REUPLOAD.getDescription())
						| status.getImageStaus().equalsIgnoreCase(StatusRegistration.REJECTED.getDescription()))) {
					responseDetailsVO.setRejectedByEnclosure(true);
				}
			});

			if (!responseDetailsVO.isRejectedByEnclosure())
				responseDetailsVO.setRejectedByEnclosure(false);
			return responseDetailsVO;

		} else {
			logger.debug("Unable to fetch rejection application details, Application No:", applicationNo);
			logger.error("Unable to fetch rejection application details, Application No: [{}]", applicationNo);
			throw new BadRequestException(
					"Unable to fetch rejection application details, Application No:" + applicationNo);
		}
	}

	@Override
	public Boolean updateVahanDetails(RequestDetailsVO requestDetailsVO, String userId) {
		Optional<StagingRegistrationDetailsDTO> opRegDetails = getRegistrationDetailsByApplicationNo(
				requestDetailsVO.getApplicationNo());

		if (opRegDetails.isPresent() && null != opRegDetails.get().getVahanDetails()) {
			StagingRegistrationDetailsDTO stageDTO = opRegDetails.get();
			stageDTO.setVahanDetails(null);
			stageDTO.setStageNo(StageEnum.ONE.getNumber());
			logMovingService.moveStagingToLog(stageDTO.getApplicationNo());
			stagingRegistrationDetailsDAO.save(stageDTO);
			return true;

		} else {
			logger.debug("Unable to Update Vahan Details, Application No:", requestDetailsVO.getApplicationNo());
			logger.error("Unable to Update Vahan Details, Application No:[{}]", requestDetailsVO.getApplicationNo());
			throw new BadRequestException(
					"Unable to Update Vahan Details, Application No:" + requestDetailsVO.getApplicationNo());
		}
	}

	@Override
	public Optional<VahanDetailsVO> getCOVByVahanDetails(String engineNo, String chasisNo, String userId) {
		Optional<VahanDetailsDTO> resultFromDB = validateClassVehicleType(engineNo, chasisNo, userId);
		if (resultFromDB.isPresent() && !resultFromDB.get().getDealerCovType().isEmpty()) {
			VahanDetailsVO vahanDetailsVO = new VahanDetailsVO();
			vahanDetailsVO.setDealerCovType(resultFromDB.get().getDealerCovType());
			return Optional.of(vahanDetailsVO);
		} else {
			logger.debug("Unable to fetch COV details.");
			logger.error("Unable to fetch COV details.");
			throw new BadRequestException("Unable to fetch COV details.");
		}
	}
	@Override
	public Optional<VahanDetailsVO> getClassOfVehicleByVahanDetails(String engineNo, String chasisNo) {
		// TODO Auto-generated method stub
		Optional<VahanDetailsDTO> resultFromDB = validateClassVehicle(engineNo, chasisNo);
		if (resultFromDB.isPresent()) {
			List<DealerAndVahanMappedCovDTO> dealerAndVahanMappedCovDTOList = dealerAndVahanMappedCovDAO
					.findByVahanCovType(resultFromDB.get().getVehicleClass());
			List<String> dealerCovTypeList = new ArrayList<>();
			dealerAndVahanMappedCovDTOList.forEach(dealerCovType -> {
				dealerCovTypeList.add(dealerCovType.getDealerCovType());
			});
			VahanDetailsVO vahanDetailsVO = new VahanDetailsVO();
			vahanDetailsVO.setDealerCovType(dealerCovTypeList);
			return Optional.of(vahanDetailsVO);
		} else {
			logger.debug("Unable to fetch COV details.");
			logger.error("Unable to fetch COV details.");
			throw new BadRequestException("Unable to fetch COV details.");
		}
	}
	public Optional<VahanDetailsDTO> validateClassVehicle(String engineNo, String chasisNo) {

		List<VahanDetailsDTO> vahanDetailsList = vahanDAO.findByChassisNumberAndEngineNumber(chasisNo, engineNo);

		Optional<VahanDetailsDTO> resultFromDB = Optional.empty();
		if (vahanDetailsList.isEmpty()) {
			throw new BadRequestException("No Records Found in Vahan Details"); 
		}
		if (!vahanDetailsList.isEmpty()) {
			vahanDetailsList = vahanDetailsList.stream().map(VahanDetailsDTO::setCreatedeDateForObj)
					.collect(Collectors.toList());
			vahanDetailsList.sort((v1, v2) -> v2.getCreatedeDate().compareTo(v1.getCreatedeDate()));
			resultFromDB = vahanDetailsList.stream().findFirst();

			VahanDetailsDTO dto = resultFromDB.get();
			if (StringUtils.isEmpty(dto.getManufacturedMonthYear()) || dto.getManufacturedMonthYear().equals("000000")
					|| dto.getManufacturedMonthYear().substring(0, 2).equals("00")
					|| dto.getManufacturedMonthYear().substring(2, 6).equals("0000")) {
				String errorMsg = "Zeros retrieved for Month/Year field from Vahan, please make sure details are updated in Vahan.";
				logger.debug(errorMsg + "chasisNo[{}],engineNo[{}]", chasisNo, engineNo);
				logger.error(errorMsg);
				throw new BadRequestException(errorMsg);
			}

		}
			if (null != resultFromDB.get().getVehicleClass()) {
				logger.info("Validated Vahan Class Type [{}].", resultFromDB.get().getVehicleClass());
				return resultFromDB;
			} else {
				String errorMsg = "unsupported vehicle class type, Please verify once.";
				logger.error(errorMsg);
				throw new BadRequestException(errorMsg);
			}
	}
	@Override
	public RegServiceVO saveRegServiceForOther(String regServiceVO, MultipartFile[] uploadfiles) {

		if (StringUtils.isBlank(regServiceVO)) {
			logger.error("Input Details are required.");
			throw new BadRequestException("Input Details are required.");
		}
		Optional<RegServiceVO> inputOptional = readValue(regServiceVO, RegServiceVO.class);
		if (!inputOptional.isPresent()) {
			logger.error("Invalid Input Details.");
			throw new BadRequestException("Invalid Input Details.");
		}

		try {
			RegServiceVO inputRegServiceVO = inputOptional.get();

			if (!inputRegServiceVO.getServiceType().stream().anyMatch(id -> id.equals(ServiceEnum.DATAENTRY))
					|| !inputRegServiceVO.getServiceIds().stream()
							.anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))) {
				logger.error("Invalid Service Type.");
				throw new BadRequestException("Invalid Service Type.");
			}
			if (inputRegServiceVO.isGetOtherStateDataFromVahanService()) {
				otherStateVahanService.validationForVahanServices(inputRegServiceVO);
			}
			inputRegServiceVO = this.otherStateValidations(inputRegServiceVO);
			RegServiceDTO regServiceDTO = this.verifyAadharAndVahanDetails(inputRegServiceVO);
			regServiceDTO.setIsTSApplication(inputRegServiceVO.getIsTSApplication());
			updateEnclosureForDataEntry(uploadfiles, inputRegServiceVO, regServiceDTO, null);
			Optional<CitizenEnclosuresDTO> citizenEnclosuresOpt = citizenEnclosuresDAO
					.findByApplicationNoAndServiceIdsIn(regServiceDTO.getApplicationNo(),
							regServiceDTO.getServiceIds());
			if (!citizenEnclosuresOpt.isPresent()) {
				saveCitizenEnclosureDetails(inputRegServiceVO, regServiceDTO, new CitizenEnclosuresDTO());
			} else {
				saveCitizenEnclosureDetails(inputRegServiceVO, regServiceDTO, citizenEnclosuresOpt.get());
			}
			if (regServiceDTO.getPrNo() != null) {
				regServiceDTO.setPrNo(regServiceDTO.getPrNo().replaceAll("\\s", ""));
			}
			if (regServiceDTO.getRegistrationDetails().getPrNo() != null) {
				regServiceDTO.getRegistrationDetails()
						.setPrNo(regServiceDTO.getRegistrationDetails().getPrNo().replaceAll("\\s", ""));
			}
			if (inputRegServiceVO.getServiceType().stream().anyMatch(id -> id.equals(ServiceEnum.HPA))) {
				if (inputRegServiceVO.getRegistrationDetails() != null
						&& inputRegServiceVO.getRegistrationDetails().getIsFinancier()
						&& inputRegServiceVO.getRegistrationDetails().isRegVehicleWithPR()
						|| inputRegServiceVO.getRegistrationDetails() != null
								&& inputRegServiceVO.getRegistrationDetails().getIsFinancier()
								&& inputRegServiceVO.getRegistrationDetails().isRegVehicleWithTR()) {
					if (!inputRegServiceVO.getOtherStateFianContinue()) {
						logger.error("Continue Finance is Mandatory");
						throw new BadRequestException("Continue Finance is Mandatory");
					}
					regServiceDTO.setOtherStateFianContinue(inputRegServiceVO.getOtherStateFianContinue());
				}
				saveFinancierToken(inputRegServiceVO, regServiceDTO);
			}
			if (inputRegServiceVO.getServiceType().stream()
					.anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFOWNERSHIP))) {

				saveBuyerdetails(inputRegServiceVO, regServiceDTO);
			}
			regServiceDTO.setOsNewCombinatonsDataEntry(Boolean.TRUE);
			regServiceDTO = this.reassignmentEnabledOrDisbled(regServiceDTO, inputRegServiceVO);
			if (StringUtils.isNoneEmpty(inputRegServiceVO.getTrNo())) {
				validationRegServiceOtherWithTR(inputRegServiceVO);
				regServiceDTO.setPrNo(null);
				regServiceDTO.getRegistrationDetails().setPrNo(null);
			}
			if (StringUtils.isNoneEmpty(inputRegServiceVO.getPrNo())) {
				validationRegServiceOtherWithPR(inputRegServiceVO);
				regServiceDTO.setTrNo(null);
				regServiceDTO.getRegistrationDetails().setTrNo(null);
			}
			if (!StringUtils.isNoneEmpty(inputRegServiceVO.getTrNo())
					&& !StringUtils.isNoneEmpty(inputRegServiceVO.getPrNo())) {
				regServiceDTO.setPrNo(null);
				regServiceDTO.getRegistrationDetails().setPrNo(null);
				regServiceDTO.setTrNo(null);
				regServiceDTO.getRegistrationDetails().setTrNo(null);
			}
			convertTOuperCase(regServiceDTO);
			if (regServiceDTO.getRegistrationDetails().getIsCentralGovernamentOrDefenceEmployee()
					&& StringUtils.isNoneEmpty(regServiceDTO.getTaxDetails().getTaxType())
					&& regServiceDTO.getTaxDetails().getTaxType().equals(TaxTypeEnum.LifeTax.getDesc())) {
				regServiceDTO.getRegistrationDetails().setTaxPaidByVcr(true);
			}
			if (StringUtils.isNoneEmpty(inputRegServiceVO.getPrNo())
					&& StringUtils.isNoneEmpty(inputRegServiceVO.getPreviousPrNo())
					&& StringUtils.isNoneEmpty(regServiceDTO.getTaxDetails().getTaxType())
					&& regServiceDTO.getTaxDetails().getTaxType().equals(TaxTypeEnum.LifeTax.getDesc())) {
				previouslyPRnumber(regServiceDTO);
			}
			if (StringUtils.isNoneEmpty(inputRegServiceVO.getTaxDetails().getVcrno())
					&& inputRegServiceVO.getVcrDetailsVO() != null) {
				if (inputRegServiceVO.getVcrDetailsVO().getPaymtDate() != null) {
					regServiceDTO.getTaxDetails().setVcrPaymentDate(inputRegServiceVO.getVcrDetailsVO().getPaymtDate());
				}
				if (inputRegServiceVO.getVcrDetailsVO().getBookDate() != null) {
					regServiceDTO.getTaxDetails().setVcrBooKedDate(inputRegServiceVO.getVcrDetailsVO().getBookDate());
				}
			}
			regServiceDAO.save(regServiceDTO);
			if (regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.HPA.getId()))) {
				notifications.sendNotifications(MessageTemplate.REG_FINANCIER_TOKEN_GENERATED.getId(), regServiceDTO);
			}
			if ((regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId())))
					&& (regServiceDTO.getRegistrationDetails() != null)) {
				notifications.sendNotifications(MessageTemplate.REG_FCSLOT.getId(), regServiceDTO);
			}
			return regServiceMapper.convertEntity(regServiceDTO);

		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new BadRequestException(e.getMessage());
		}
	}

	private RegServiceDTO previouslyPRnumber(RegServiceDTO regServiceDTO) {
		List<ServiceEnum> services = new ArrayList<>();
		Optional<RegistrationDetailsDTO> regDetails = null;
		services.add(ServiceEnum.ISSUEOFNOC);
		if (StringUtils.isNotBlank(regServiceDTO.getPreviousPrNo())) {
			List<TaxDetailsDTO> taxDetails = taxDetailsDAO
					.findFirst10ByPrNoOrderByCreatedDateDesc(regServiceDTO.getPreviousPrNo());
			List<RegServiceDTO> regList = regServiceDAO.findByPrNoAndServiceTypeIn(regServiceDTO.getPreviousPrNo(),
					services);
			try {
				regDetails = registrationDetailDAO.findByPrNo(regServiceDTO.getPreviousPrNo());
			} catch (Exception e) {
				logger.info("your previous prNo  does not exits reg Details, prNo:", regServiceDTO.getPreviousPrNo());
			}
			if (CollectionUtils.isNotEmpty(taxDetails) && CollectionUtils.isNotEmpty(regList)) {
				taxDetails = taxDetails.stream().filter(taxDate -> taxDate.getCreatedDate() != null)
						.collect(Collectors.toList());
				regList = regList.stream().filter(regcdate -> regcdate.getCreatedDate() != null)
						.collect(Collectors.toList());
				taxDetails.sort((p2, p1) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
				regList.sort((p2, p1) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
				RegServiceDTO regDTO = regList.stream().findFirst().get();
				TaxDetailsDTO taxDTO = taxDetails.stream().findFirst().get();
				if (StringUtils.isNotBlank(taxDTO.getPaymentPeriod())
						&& TaxTypeEnum.LifeTax.getDesc().equalsIgnoreCase(taxDTO.getPaymentPeriod())
						&& CollectionUtils.isNotEmpty(regDTO.getServiceIds())
						&& regDTO.getServiceIds().contains(ServiceEnum.ISSUEOFNOC.getId()) && regDetails.isPresent()
						&& regDetails.get().getVahanDetails().getEngineNumber()
								.equals(regServiceDTO.getRegistrationDetails().getVahanDetails().getEngineNumber())
						&& regDetails.get().getVahanDetails().getChassisNumber()
								.equals(regServiceDTO.getRegistrationDetails().getVahanDetails().getChassisNumber())) {
					regServiceDTO.getRegistrationDetails().setTaxPaidByVcr(true);
				}
			}
			return regServiceDTO;
		}
		return regServiceDTO;
	}

	private RegServiceVO otherStateValidations(RegServiceVO inputRegServiceVO) {
		if (inputRegServiceVO.getGreenTaxDetails() != null
				&& StringUtils.isNoneEmpty(inputRegServiceVO.getGreenTaxDetails().getGreenTaxAmount())) {
			inputRegServiceVO.getGreenTaxDetails()
					.setTaxAmount(Long.valueOf(inputRegServiceVO.getGreenTaxDetails().getGreenTaxAmount()));
		}
		if (inputRegServiceVO.getRegistrationDetails() == null) {
			throw new BadRequestException("Registration Details not available.");
		}

		if (StringUtils.isNoneEmpty(inputRegServiceVO.getTaxDetails().getVcrno())) {
			inputRegServiceVO.getRegistrationDetails().setTaxPaidByVcr(true);
			inputRegServiceVO.getTaxDetails().setTaxType(null);

		}
		if (StringUtils
				.isNoneEmpty(inputRegServiceVO.getRegistrationDetails().getApplicantDetails().getDisplayName())) {
			inputRegServiceVO.getRegistrationDetails().getApplicantDetails().setDisplayName(
					inputRegServiceVO.getRegistrationDetails().getApplicantDetails().getDisplayName().toUpperCase());
		}
		if (!inputRegServiceVO.getRegistrationDetails().getApplicantType().equalsIgnoreCase("WITHINTHESTATE"))
			inputRegServiceVO.getTaxDetails().setPaymentDAte(LocalDate.now());
		if (StringUtils.isNoneEmpty(inputRegServiceVO.getRegistrationDetails().getPrNo())) {
			inputRegServiceVO.setPrNo(inputRegServiceVO.getRegistrationDetails().getPrNo().toUpperCase());
			inputRegServiceVO.getRegistrationDetails().setRegVehicleWithPR(true);
		}

		if (StringUtils.isNoneEmpty(inputRegServiceVO.getRegistrationDetails().getTrNo())) {
			inputRegServiceVO.setTrNo(inputRegServiceVO.getRegistrationDetails().getTrNo().toUpperCase());
			inputRegServiceVO.getRegistrationDetails().setRegVehicleWithTR(true);
		}

		Optional<PropertiesDTO> propertiesDto = propertiesDAO.findByStatusTrue();
		if (!propertiesDto.isPresent()) {
			throw new BadRequestException("Properties details not found, Please contact to RTA admin");
		}
		if (StringUtils.isNoneBlank(inputRegServiceVO.getRegistrationDetails().getPrNo())
				&& inputRegServiceVO.getnOCDetails() != null
				&& inputRegServiceVO.getnOCDetails().getState().equals(propertiesDto.get().getStateName())) {
			if (isVCRRequiredTS(inputRegServiceVO, propertiesDto.get())) {
				inputRegServiceVO.getRegistrationDetails().setTaxPaidByVcr(true);
				inputRegServiceVO.setIsTSApplication(Boolean.TRUE);
			}
		} else if (isVCRRequired(inputRegServiceVO, propertiesDto.get())) {
			inputRegServiceVO.getRegistrationDetails().setTaxPaidByVcr(true);
		}
		commonValidationForDataEntry(inputRegServiceVO, propertiesDto.get());

		if (StringUtils.isNoneEmpty(inputRegServiceVO.getTrNo())) {
			validationRegServiceOtherWithTR(inputRegServiceVO);
		}
		if (StringUtils.isNoneEmpty(inputRegServiceVO.getPrNo())) {
			validationRegServiceOtherWithPR(inputRegServiceVO);
		}
		return inputRegServiceVO;

	}

	private RegServiceDTO verifyAadharAndVahanDetails(RegServiceVO inputRegServiceVO) {
		Optional<AadhaarDetailsResponseDTO> aadhaarDetailsResponseDTOOpt = Optional.empty();

		if (inputRegServiceVO.getRegistrationDetails().getApplicantDetails() != null) {
			aadhaarDetailsResponseDTOOpt = aadhaarValidationForDataEntry(inputRegServiceVO,
					aadhaarDetailsResponseDTOOpt);
			if (aadhaarDetailsResponseDTOOpt.isPresent()) {
				inputRegServiceVO.setAadhaarNo(aadhaarDetailsResponseDTOOpt.get().getUid().toString());
				inputRegServiceVO.getRegistrationDetails().getApplicantDetails()
						.setAadharNo(aadhaarDetailsResponseDTOOpt.get().getUid().toString());
			} else {
				inputRegServiceVO.setAadhaarNo(inputRegServiceVO.getRegistrationDetails().getApplicantDetails()
						.getAadharResponse().getUid().toString());
				inputRegServiceVO.getRegistrationDetails().getApplicantDetails().setAadharNo(inputRegServiceVO
						.getRegistrationDetails().getApplicantDetails().getAadharResponse().getUid().toString());
			}

		}

		if (inputRegServiceVO.getRegistrationDetails().getVahanDetails() != null
				&& inputRegServiceVO.getRegistrationDetails().getVahanDetails().getManufacturedMonthYear() != null) {
			inputRegServiceVO.getRegistrationDetails().getVahanDetails()
					.setManufacturedMonthYear(getosManufacturedMonthYear(
							inputRegServiceVO.getRegistrationDetails().getVahanDetails().getManufacturedMonthYear()));
		}
		RegServiceDTO regServiceDTO = regServiceMapper.convertVO(inputRegServiceVO);
		if (inputRegServiceVO.getVoluntaryTaxVO() != null) {
			OtherVoluntaryTax voluntaryTax=new OtherVoluntaryTax();
			voluntaryTax.setOwnerName(inputRegServiceVO.getVoluntaryTaxVO().getOwnerName());
			voluntaryTax.setVoluntaryId(inputRegServiceVO.getVoluntaryTaxVO().getVoluntaryId());
			voluntaryTax.setChassisNo(inputRegServiceVO.getVoluntaryTaxVO().getChassisNo());
			voluntaryTax.setState(inputRegServiceVO.getVoluntaryTaxVO().getState());
			voluntaryTax.setTaxType(inputRegServiceVO.getVoluntaryTaxVO().getTaxType());
			voluntaryTax.setTax(inputRegServiceVO.getVoluntaryTaxVO().getTax());
			voluntaryTax.setTaxvalidFrom(inputRegServiceVO.getVoluntaryTaxVO().getTaxvalidFrom());
			voluntaryTax.setTaxvalidUpto(inputRegServiceVO.getVoluntaryTaxVO().getTaxvalidUpto());
			regServiceDTO.setOtherStateVoluntaryTax(voluntaryTax);		
		}
		regServiceDTO.setIsTSApplication(inputRegServiceVO.getIsTSApplication());
		if (regServiceDTO.getRegistrationDetails().isRegVehicleWithTR()) {
			regServiceDTO.getRegistrationDetails()
					.setTrGeneratedDate(inputRegServiceVO.getRegistrationDetails().getTrIssueDate().atStartOfDay());
		}
		officeDetailsUpdation(null, inputRegServiceVO.getRegistrationDetails());
		inputRegServiceVO.setOfficeDetails(inputRegServiceVO.getRegistrationDetails().getOfficeDetails());
		if (aadhaarDetailsResponseDTOOpt.isPresent()
				&& regServiceDTO.getRegistrationDetails().getApplicantDetails() != null) {
			regServiceDTO.getRegistrationDetails().getApplicantDetails()
					.setAadharResponse(aadhaarDetailsResponseDTOOpt.get());
			regServiceDTO.getRegistrationDetails().getApplicantDetails().setIsAadhaarValidated(Boolean.TRUE);

		}

		if (regServiceDTO.getRegistrationDetails().getVahanDetails() != null) {
			regServiceDTO.getRegistrationDetails().getVahanDetails().setIsNonVahanData(true);
		}
		regServiceDTO.setOfficeDetails(
				officeMapper.convertVO(inputRegServiceVO.getRegistrationDetails().getOfficeDetails()));
		regServiceDTO.setOfficeCode(regServiceDTO.getOfficeDetails().getOfficeCode());
		regServiceDTO.getRegistrationDetails().setOfficeDetails(regServiceDTO.getOfficeDetails());
		regServiceDTO.getRegistrationDetails().setVehicleDetails(
				vehicleDetailsMapper.convetVehicleDetailsFromVahanForOther(regServiceDTO.getRegistrationDetails()));

		if (null == regServiceDTO.getApplicationNo()) {
			Map<String, String> officeCodeMap = new TreeMap<>();
			officeCodeMap.put("officeCode",
					inputRegServiceVO.getRegistrationDetails().getOfficeDetails().getOfficeCode());
			regServiceDTO.setApplicationNo(sequenceGenerator
					.getSequence(String.valueOf(Sequence.REGISTRATIONAPPNO.getSequenceId()), officeCodeMap));
		}
		if (null != regServiceDTO.getApplicationNo()) {
			regServiceDTO.getRegistrationDetails().setApplicationNo(regServiceDTO.getApplicationNo());
			regServiceDTO.getRegistrationDetails().getApplicantDetails().setApplicantNo(getTransactionId());
			regServiceDTO.getRegistrationDetails().setDataInsertedByDataEntry(true);
		}

		regServiceDTO.setCreatedDate(LocalDateTime.now());

		regServiceDTO.setApplicationStatus(StatusRegistration.CITIZENSUBMITTED);
		// if
		// (!StringUtils.isNoneEmpty(inputRegServiceVO.getRegistrationDetails().getPrNo()))
		// {
		// regServiceDTO.setFlowId(ServiceEnum.Flow.TOWTODATAENTRY);
		// }
		this.setRegistrationValidityData(regServiceDTO);

		// if (!inputRegServiceVO.getRegistrationDetails().isRegVehicleWithPR()
		// ||
		// inputRegServiceVO.getRegistrationDetails().getApplicantType().equalsIgnoreCase("Paper
		// RC")
		// ||
		// inputRegServiceVO.getRegistrationDetails().getApplicantType().equalsIgnoreCase("WITHINTHESTATE"))
		// {
		registrationService.setMviOfficeDetails(regServiceDTO, inputRegServiceVO);
		String slotTime = slotService.bookSlot(ModuleEnum.REG.toString(), null, regServiceDTO.getMviOfficeCode(),
				inputRegServiceVO.getSlotDetails().getTestSlotDate());
		SlotDetailsDTO dto = new SlotDetailsDTO();
		dto.setSlotTime(slotTime);
		dto.setCreatedDate(LocalDateTime.now());
		dto.setSlotDate(inputRegServiceVO.getSlotDetails().getTestSlotDate());
		regServiceDTO.setSlotDetails(dto);
		if (inputRegServiceVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.CHANGEOFADDRESS.getId()))) {
			regServiceDTO.setFlowId(ServiceEnum.Flow.COATODATAENTRY);
			if (!regServiceDTO.getRegistrationDetails().getOwnerType().toString().equals("POLICE")) {
				regServiceDTO = registrationService.getPresentAddress(inputRegServiceVO, regServiceDTO);
			}
		} else {
			regServiceDTO.setFlowId(ServiceEnum.Flow.TOWTODATAENTRY);
		}

		if (!regServiceDTO.getServiceIds().contains(ServiceEnum.HPA.getId())) {
			registratrionServicesApprovals.initiateApprovalProcessFlow(regServiceDTO);
		}

		// }
		if (!regServiceDTO.getRegistrationDetails().getOwnerType().toString().equals("Government")
				&& !regServiceDTO.getRegistrationDetails().getOwnerType().toString().equals("POLICE")
				&& !regServiceDTO.getRegistrationDetails().getOwnerType().toString().equals("Stu")) {
			this.setInsuranceDetailsData(regServiceDTO);
		}
		if (inputRegServiceVO.getFcDetails() != null && inputRegServiceVO.getFcDetails().getIssuedDate() != null) {
			regServiceDTO.getFcDetails().setOfficeName(inputRegServiceVO.getFcDetails().getIssueState());
			regServiceDTO.getFcDetails().setFcIssuedDate(
					DateConverters.convertLocalDateToLocalDateTime(inputRegServiceVO.getFcDetails().getIssuedDate()));
			regServiceDTO.getFcDetails().setInspectedDate(
					DateConverters.convertLocalDateToLocalDateTime(inputRegServiceVO.getFcDetails().getIssuedDate()));
		}
		// 08-10-2018--add COA
		/*
		 * if ((regServiceDTO.getRegistrationDetails().isRegVehicleWithPR() &&
		 * regServiceDTO.getRegistrationDetails().isTaxPaidByVcr()) ||
		 * inputRegServiceVO.getRegistrationDetails().getApplicantType().
		 * equalsIgnoreCase("WITHINTHESTATE") ||
		 * inputRegServiceVO.getRegistrationDetails().getApplicantType().
		 * equalsIgnoreCase("Paper RC")) {
		 * registratrionServicesApprovals.initiateApprovalProcessFlow( regServiceDTO); }
		 */

		// if
		// (inputRegServiceVO.getRegistrationDetails().getApplicantType().equalsIgnoreCase("Paper
		// RC")
		// ||
		// inputRegServiceVO.getRegistrationDetails().getApplicantType().equalsIgnoreCase("WITHINTHESTATE"))
		// {
		// regServiceDTO.setFlowId(ServiceEnum.Flow.TOWTODATAENTRY);
		// registratrionServicesApprovals.initiateApprovalProcessFlow(regServiceDTO);
		// }
		if (inputRegServiceVO.getRegistrationDetails().getDealerDetails() != null) {
			regServiceDTO.getRegistrationDetails().getDealerDetails()
					.setDealerId(inputRegServiceVO.getRegistrationDetails().getDealerDetails().getDealerName());
		}
		return regServiceDTO;
	}

	private void saveCitizenEnclosureDetails(RegServiceVO inputRegServiceVO, RegServiceDTO regServiceDTO,
			CitizenEnclosuresDTO citizenEnclosuresDTO) {
		citizenEnclosuresDTO.setApplicationNo(regServiceDTO.getApplicationNo());
		citizenEnclosuresDTO.setAadharNo(regServiceDTO.getRegistrationDetails().getApplicantDetails().getAadharNo());
		citizenEnclosuresDTO.setEnclosures(regServiceDTO.getEnclosures());
		citizenEnclosuresDTO.setServiceIds(inputRegServiceVO.getServiceIds());
		if (null != regServiceDTO.getPrNo()) {
			citizenEnclosuresDTO.setPrNo(regServiceDTO.getPrNo());
		}
		citizenEnclosuresDAO.save(citizenEnclosuresDTO);
	}

	private void setInsuranceDetailsData(RegServiceDTO regServiceDTO) {
		if (regServiceDTO.getRegistrationDetails().getInsuranceDetails().getValidFromForOther() != null
				&& regServiceDTO.getRegistrationDetails().getInsuranceDetails().getValidTillForOther() != null) {
			regServiceDTO.getRegistrationDetails().getInsuranceDetails()
					.setValidFrom(regServiceDTO.getRegistrationDetails().getInsuranceDetails().getValidFromForOther());
			regServiceDTO.getRegistrationDetails().getInsuranceDetails()
					.setValidTill(regServiceDTO.getRegistrationDetails().getInsuranceDetails().getValidTillForOther());
			regServiceDTO.setInsuranceDetails(regServiceDTO.getRegistrationDetails().getInsuranceDetails());
		}
	}

	private void setRegistrationValidityData(RegServiceDTO regServiceDTO) {
		RegistrationValidityDTO registrationValidityDTO = new RegistrationValidityDTO();
		if (regServiceDTO.getRegistrationDetails() != null) {
			if (regServiceDTO.getTaxvalidity() != null) {
				registrationValidityDTO.setTaxValidity(regServiceDTO.getTaxvalidity());
			}
			if (regServiceDTO.getRegistrationDetails().getPrIssueDate() != null) {
				registrationValidityDTO.setPrGeneratedDate(regServiceDTO.getRegistrationDetails().getPrIssueDate());
				regServiceDTO.getRegistrationDetails()
						.setPrGeneratedDate(regServiceDTO.getRegistrationDetails().getPrIssueDate().atStartOfDay());
			}
			if (regServiceDTO.getRegistrationDetails().getTrIssueDate() != null) {
				registrationValidityDTO.setTrGeneratedDate(regServiceDTO.getRegistrationDetails().getTrIssueDate());
			}
			if (regServiceDTO.getRegistrationDetails().getPrIssueDate() != null
					&& regServiceDTO.getRegistrationDetails().getVehicleType() != null
					&& !regServiceDTO.getRegistrationDetails().isRegVehicleWithPR()) {
				setVehicleRegistrationValidity(regServiceDTO, registrationValidityDTO);
			}
			if (regServiceDTO.getRegistrationDetails().getPrIssueDate() != null
					&& regServiceDTO.getRegistrationDetails().getVehicleType() != null
					&& regServiceDTO.getRegistrationDetails().isRegVehicleWithPR()) {
				setVehicleRegistrationValidityWithPR(regServiceDTO, registrationValidityDTO);
			}
		}
		regServiceDTO.getRegistrationDetails().setRegistrationValidity(registrationValidityDTO);
	}

	private void setVehicleRegistrationValidity(RegServiceDTO regServiceDTO,
			RegistrationValidityDTO registrationValidityDTO) {
		if (regServiceDTO.getRegistrationDetails().getPrGeneratedDate() != null) {
			// Registration validity for Non Transport Type
			if (regServiceDTO.getRegistrationDetails().getVehicleType().equals(CovCategory.N.getCode())) {
				registrationValidityDTO
						.setRegistrationValidity(regServiceDTO.getRegistrationDetails().getPrGeneratedDate()
								.minusDays(1).plusYears(ValidityEnum.PR_NON_TRANSPORT_VALIDITY.getValidity()));
			}

			// Registration validity for Transport Type
			else if (regServiceDTO.getRegistrationDetails().getVehicleType().equals(CovCategory.T.getCode())) {
				registrationValidityDTO.setRegistrationValidity(regServiceDTO.getRegistrationDetails()
						.getPrGeneratedDate().minusDays(1).plusYears(ValidityEnum.PR_TRANSPORT_VALIDITY.getValidity()));
				registrationValidityDTO
						.setFcValidity(LocalDate.now().minusDays(1).plusYears(ValidityEnum.FCVALIDITY.getValidity()));
			}
		}
	}

	private void setVehicleRegistrationValidityWithPR(RegServiceDTO regServiceDTO,
			RegistrationValidityDTO registrationValidityDTO) {
		if (regServiceDTO.getRegistrationDetails().getPrGeneratedDate() != null) {
			// Registration validity for Non Transport Type
			if (regServiceDTO.getRegistrationDetails().getVehicleType().equals(CovCategory.N.getCode())) {
				registrationValidityDTO.setRegistrationValidity(DateConverters
						.convertLocalDateToLocalDateTime(regServiceDTO.getRegistrationDetails().getPrValidUpto()));
			}

			// Registration validity for Transport Type
			else if (regServiceDTO.getRegistrationDetails().getVehicleType().equals(CovCategory.T.getCode())) {
				registrationValidityDTO.setRegistrationValidity(DateConverters
						.convertLocalDateToLocalDateTime(regServiceDTO.getRegistrationDetails().getPrValidUpto()));
				if (regServiceDTO.getFcDetails() != null)
					registrationValidityDTO.setFcValidity(regServiceDTO.getFcDetails().getFcValidUpto());
			}
		}
	}

	private boolean isVCRRequired(RegServiceVO inputRegServiceVO, PropertiesDTO propertiesDTO) {
		if (StringUtils.isNoneEmpty(inputRegServiceVO.getTaxDetails().getVcrno())
				&& inputRegServiceVO.getTaxDetails().getTaxType() == null
				|| (StringUtils.isNoneBlank(inputRegServiceVO.getRegistrationDetails().getPrNo())
						&& inputRegServiceVO.getnOCDetails() != null
						&& inputRegServiceVO.getnOCDetails().getState().equals(propertiesDTO.getStateName())
						&& inputRegServiceVO.getRegistrationDetails().getPrIssueDate()
								.isBefore(DateConverters.convertStirngTOlocalDate(propertiesDTO.getRegDate())))) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	/**
	 * @param inputRegServiceVO
	 * @param aadhaarDetailsResponseDTOOpt
	 * @return
	 */
	private Optional<AadhaarDetailsResponseDTO> aadhaarValidationForDataEntry(RegServiceVO inputRegServiceVO,
			Optional<AadhaarDetailsResponseDTO> aadhaarDetailsResponseDTOOpt) {
		if (null != inputRegServiceVO.getRegistrationDetails().getApplicantDetails().getAadharResponse()
				&& null != inputRegServiceVO.getRegistrationDetails().getApplicantDetails().getAadharResponse()
						.getUuId()) {
			try {
				aadhaarDetailsResponseDTOOpt = aadhaarResponseDAO.findByUuId(
						inputRegServiceVO.getRegistrationDetails().getApplicantDetails().getAadharResponse().getUuId());

				if (!aadhaarDetailsResponseDTOOpt.isPresent()) {
					logger.info(appMessages.getLogMessage(MessageKeys.DEALER_DETAILSNOTAVAILABLE), inputRegServiceVO
							.getRegistrationDetails().getApplicantDetails().getAadharResponse().getUuId());
					// throw new BadRequestException(
					// appMessages.getResponseMessage(MessageKeys.DEALER_AADHARDETAILSNOTAVAILABLE));
				}
			} catch (Exception e) {
				// TODO: handle exception
			}
		} else {
			throw new BadRequestException("Aadhaar Details Required.");
		}
		return aadhaarDetailsResponseDTOOpt;
	}

	private void commonValidationForDataEntry(RegServiceVO inputRegServiceVO, PropertiesDTO propertiesDTO) {

		vahanValidationInRegServiceForOtherSate(inputRegServiceVO, propertiesDTO);
		try {
			if (null != inputRegServiceVO.getRegistrationDetails().getInsuranceDetails())
				insuranceValidationForOtherState(inputRegServiceVO, propertiesDTO);
		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());

		}

	}

	/**
	 * @param inputRegServiceVO
	 * @param propertiesDTO
	 */
	private void vahanValidationInRegServiceForOtherSate(RegServiceVO inputRegServiceVO, PropertiesDTO propertiesDTO) {
		try {
			if (inputRegServiceVO.getRegistrationDetails() != null && !(inputRegServiceVO.getRegistrationDetails()
					.getClassOfVehicle().equalsIgnoreCase("TTTT")
					|| inputRegServiceVO.getRegistrationDetails().getClassOfVehicle().equalsIgnoreCase("TTRN"))) {
				if (propertiesDTO.getIsVahanValidationInStagingForOtherSate()) {
					vahanValidationInStagingForOtherSate(
							inputRegServiceVO.getRegistrationDetails().getVahanDetails().getEngineNumber(),
							inputRegServiceVO.getRegistrationDetails().getVahanDetails().getChassisNumber());
				}
				if (propertiesDTO.getIsVahanValidationInRegDetailsForOtherSate()) {
					vahanValidationInRegDetailsForOtherSate(inputRegServiceVO);
				}
				if (propertiesDTO.getIsVahanValidationInRegServicesForOtherSate()) {
					vahanValidationInRegServiceForOtherSate(inputRegServiceVO);
				}
			}
		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	@Override
	public void vahanValidationInStagingForOtherSate(String engineNo, String chassisNo) {
		try {
			Optional<StagingRegistrationDetailsDTO> regOpt = stagingRegistrationDetailsDAO
					.findByVahanDetailsEngineNumberOrVahanDetailsChassisNumber(engineNo, chassisNo);

			if (regOpt.isPresent()) {
				throw new BadRequestException("Vahan Details Already exist with Application No:" + "["
						+ regOpt.get().getApplicationNo() + "]");
			}
		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	private void vahanValidationInRegDetailsForOtherSate(RegServiceVO inputRegServiceVO) {
		try {
			Optional<RegistrationDetailsVO> regOpt = registrationService.findByChassisNoAndEngineNo(
					inputRegServiceVO.getRegistrationDetails().getVahanDetails().getChassisNumber(),
					inputRegServiceVO.getRegistrationDetails().getVahanDetails().getEngineNumber());

			if (regOpt.isPresent()) {
				if (regOpt.get().getOfficeDetails() != null
						&& regOpt.get().getOfficeDetails().getOfficeCode() != null) {
					String officeCode = regOpt.get().getOfficeDetails().getOfficeCode();
					Optional<OfficeDTO> office = officeDAO.findByOfficeCodeAndIsActiveTrue(officeCode);
					if (regOpt.isPresent() && office.isPresent()) {
						if (inputRegServiceVO.getRegistrationDetails() != null
								&& inputRegServiceVO.getRegistrationDetails().getPrNo() != null && inputRegServiceVO
										.getRegistrationDetails().getPrNo().equals(regOpt.get().getPrNo())) {
							Optional<RegServiceDTO> regDto = restGateWayService
									.checkDataEntryExits(regOpt.get().getPrNo());
							if (regDto.isPresent()) {
								validateOtherStateNOCDetails(regDto);
							} else {
								throw new BadRequestException("Vahan Details Already exist with Application No:" + "["
										+ regOpt.get().getApplicationNo() + "]");
							}

						} else {
							throw new BadRequestException("Vahan Details Already exist with Application No:" + "["
									+ regOpt.get().getApplicationNo() + "]");
						}

					}
				}
			}
		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	private void vahanValidationInRegServiceForOtherSate(RegServiceVO inputRegServiceVO) {
		try {
			List<StatusRegistration> applicationStatus = new ArrayList<>();
			applicationStatus.add(StatusRegistration.CITIZENSUBMITTED);
			applicationStatus.add(StatusRegistration.PAYMENTDONE);
			applicationStatus.add(StatusRegistration.APPROVED);
			applicationStatus.add(StatusRegistration.OTHERSTATEPAYMENTPENDING);
			List<RegServiceDTO> regOpt = registrationService.findByChassisNumberAndEngineNumber(
					inputRegServiceVO.getRegistrationDetails().getVahanDetails().getChassisNumber(),
					inputRegServiceVO.getRegistrationDetails().getVahanDetails().getEngineNumber(), applicationStatus);

			if (CollectionUtils.isNotEmpty(regOpt)) {
				regOpt.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
				for (RegServiceDTO regOptData : regOpt) {
					Boolean nocValidation = Boolean.TRUE;
					if (inputRegServiceVO.getPrNo() != null && regOptData.getRegistrationDetails() != null
							&& regOptData.getRegistrationDetails().getPrNo() != null
							&& regOptData.getRegistrationDetails().getPrNo().equals(inputRegServiceVO.getPrNo())) {
						/*
						 * Optional<RegServiceDTO> regDto =
						 * restGateWayService.checkDataEntryExits(regOptData. getPrNo());
						 * if(regDto.isPresent()){ validateOtherStateNOCDetails(regDto); }else{ throw
						 * new BadRequestException("Vahan Details Already exist with Application No:" +
						 * "[" +regOptData.getApplicationNo() + "]"); }
						 */
						nocValidation = Boolean.FALSE;
					}
					if (nocValidation) {
						if (regOptData.getRegistrationDetails().isRegVehicleWithPR()
								&& regOptData.getRegistrationDetails().isTaxPaidByVcr()
								&& regOptData.getApplicationStatus().equals(StatusRegistration.CITIZENSUBMITTED)
								&& !inputRegServiceVO.getnOCDetails().getState().equals("Telangana")) {
							throw new BadRequestException("Vahan Details Already exist with Application No:" + "["
									+ regOptData.getApplicationNo() + "]");
						} else if (regOptData.getRegistrationDetails().isRegVehicleWithPR()
								&& regOptData.getApplicationStatus().equals(StatusRegistration.CITIZENSUBMITTED)
								&& regOptData.getRegistrationDetails().getApplicantType()
										.equalsIgnoreCase("WITHINTHESTATE")) {
							throw new BadRequestException("Vahan Details Already exist with Application No:" + "["
									+ regOptData.getApplicationNo() + "]");
						} else if (regOptData.getServiceIds().contains(ServiceEnum.ISSUEOFNOC.getId()) && (regOptData
								.getApplicationStatus().equals(StatusRegistration.OTHERSTATEPAYMENTPENDING)
								|| regOptData.getApplicationStatus().equals(StatusRegistration.APPROVED)
								|| regOptData.getApplicationStatus().equals(StatusRegistration.CITIZENSUBMITTED))) {
							break;
						} else if (regOptData.getApplicationStatus().equals(StatusRegistration.OTHERSTATEPAYMENTPENDING)
								|| regOptData.getApplicationStatus().equals(StatusRegistration.APPROVED)
								|| regOptData.getApplicationStatus().equals(StatusRegistration.CITIZENSUBMITTED)) {
							throw new BadRequestException("Vahan Details Already exist with Application No:" + "["
									+ regOptData.getApplicationNo() + "]");
						}
					}
				}
			}

		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	/**
	 * @param uploadfiles
	 * @param inputRegServiceVO
	 * @param regServiceDTO
	 * @param stagingDetails
	 */
	private void updateEnclosureForDataEntry(MultipartFile[] uploadfiles, RegServiceVO inputRegServiceVO,
			RegServiceDTO regServiceDTO, StagingRegistrationDetailsDTO stagingDetails) {
		try {
			saveImages(inputRegServiceVO, regServiceDTO, uploadfiles);

			/*
			 * if (null != regServiceDTO.getEnclosures()) {
			 * stagingDetails.setEnclosures(regServiceDTO.getEnclosures());
			 * stagingDetails.getApplicantDetails().setEnclosures(regServiceDTO.
			 * getEnclosures()); }
			 */
		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	private void validationRegServiceOtherWithPR(RegServiceVO inputRegServiceVO) {
		// TODO Auto-generated method stub

		if (null == inputRegServiceVO.getPrNo()) {
			throw new BadRequestException("Undefined Field, PR No is Not Optional.");
		}
		prValidatonInRegServiceForDataEntry(inputRegServiceVO);
	}

	/**
	 * @param inputRegServiceVO
	 */
	private void prValidatonInRegServiceForDataEntry(RegServiceVO inputRegServiceVO) {
		try {
			Optional<PropertiesDTO> propertiesDto = propertiesDAO.findByStatusTrue();
			if (propertiesDto.isPresent() && propertiesDto.get().getIsPrValidatonInRegDetailsForDataEntry()) {
				prValidatonInRegDetailsForDataEntry(inputRegServiceVO);
			}

			/*
			 * RegServiceVO regDetails =
			 * registrationService.findByprNo(inputRegServiceVO.getPrNo());
			 * 
			 * if (null != regDetails) { throw new BadRequestException(
			 * "Pr Details Already exist, PR No:" + "[" + regDetails.getApplicationNo() +
			 * "]"); }
			 */

		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	private void prValidatonInRegDetailsForDataEntry(RegServiceVO inputRegServiceVO) {
		try {

			if (inputRegServiceVO.isPreviousPrNoExist()) {
				Optional<RegistrationDetailsDTO> preRegDetails = registrationDetailDAO
						.findByPrNo(inputRegServiceVO.getPreviousPrNo());
				if (preRegDetails.isPresent()) {
					RegistrationDetailsDTO registrationDetailsDTO = preRegDetails.get();
					if (!registrationDetailsDTO.getVahanDetails().getChassisNumber()
							.equals(inputRegServiceVO.getRegistrationDetails().getVahanDetails().getChassisNumber())
							|| !registrationDetailsDTO.getVahanDetails().getEngineNumber().equals(
									inputRegServiceVO.getRegistrationDetails().getVahanDetails().getEngineNumber())) {
						throw new BadRequestException("Vahan Details invalid with previous PR No., PR No:" + "["
								+ inputRegServiceVO.getPreviousPrNo() + "]");
					}
				}
			} else {
				// Optional<RegistrationDetailsDTO> regDetails =
				// registrationDetailDAO
				// .findByPrNo(inputRegServiceVO.getPrNo());
				//
				// if (regDetails.isPresent()) {
				// throw new BadRequestException(
				// "Pr Details Already exist, PR No:" + "[" +
				// regDetails.get().getApplicationNo() + "]");
				// }
			}

		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	/*
	 * private void validationRegServiceOtherWithOutTR(RegServiceVO
	 * inputRegServiceVO) { // TODO Auto-generated method stub
	 * 
	 * String trNo = StringUtils.EMPTY; try { if(null == inputRegServiceVO.getTrNo()
	 * && null == inputRegServiceVO.getRegistrationDetails().getTrNo()){ trNo =
	 * trSeriesService.geneateTrSeries(inputRegServiceVO.getOfficeDetails().
	 * getOfficeDist()); inputRegServiceVO.setTrNo(trNo);
	 * inputRegServiceVO.getRegistrationDetails().setTrNo(trNo); }
	 * 
	 * } catch (Exception e) { throw new BadRequestException(e.getMessage()); }
	 * 
	 * }
	 */

	private void validationRegServiceOtherWithTR(RegServiceVO inputRegServiceVO) {
		// TODO Auto-generated method stub
		if (inputRegServiceVO.isSpecialNoRequired()) {
			registrationService.isAllowedForOSTrFancy(inputRegServiceVO.getRegistrationDetails().getTrIssueDate());
		}
		if (null == inputRegServiceVO.getRegistrationDetails().getInvoiceDetails()) {
			throw new BadRequestException("Undefined Field, Invoice are Not Optional.");
		}
		trValidationInRegServiceForDataEntry(inputRegServiceVO);

	}

	/**
	 * @param inputRegServiceVO
	 */
	private void trValidationInRegServiceForDataEntry(RegServiceVO inputRegServiceVO) {
		try {
			Optional<PropertiesDTO> propertiesDto = propertiesDAO.findByStatusTrue();
			if (propertiesDto.isPresent() && propertiesDto.get().getIstrValidationInStagingForDataEntry()) {
				trValidationInStagingForDataEntry(inputRegServiceVO);
			}

		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	private void trValidationInStagingForDataEntry(RegServiceVO inputRegServiceVO) {
		try {

			Optional<StagingRegistrationDetailsDTO> regDetails = stagingRegistrationDetailsDAO
					.findByTrNo(inputRegServiceVO.getTrNo());

			if (regDetails.isPresent()) {
				throw new BadRequestException(
						"Tr Details Already exist, TR No:" + "[" + inputRegServiceVO.getTrNo() + "]");
			}

		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	public <T> Optional<T> readValue(String value, Class<T> valueType) {

		try {
			return Optional.of(objectMapper.readValue(value, valueType));
		} catch (IOException ioe) {

			logger.error("Exception occured while converting String to Object", ioe);
			throw new BadRequestException("Please Pass Valid Data.");
		}

		// return Optional.empty();
	}

	private void saveImages(RegServiceVO regServiceVO, RegServiceDTO regServiceDTO, MultipartFile[] uploadfiles)
			throws IOException {

		try {
			if (uploadfiles != null && uploadfiles.length > 0) {

				List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = gridFsClient.convertImages(
						regServiceVO.getImageInput(), regServiceDTO.getApplicationNo(), uploadfiles,
						StatusRegistration.INITIATED.getDescription());

				regServiceDTO.setEnclosures(enclosures);

			}
		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());
		}

	}

	/**
	 * @param registrationDetailsDTO
	 * @param registrationDetails
	 */
	/*
	 * private void vahanValidationForOther(StagingRegistrationDetailsDTO
	 * registrationDetailsDTO, RegistrationDetailsVO registrationDetails) { try { if
	 * (null != registrationDetails.getVahanDetails()) { Optional<VahanDetailsDTO>
	 * resultFromDB = validateClassVehicleTypeForOther(
	 * registrationDetails.getVahanDetails().getEngineNumber(),
	 * registrationDetails.getVahanDetails().getChassisNumber(),
	 * registrationDetails.getClassOfVehicle());
	 * 
	 * if (resultFromDB.isPresent()) {
	 * registrationDetailsDTO.setVahanDetails(resultFromDB.get()); } else { throw
	 * new BadRequestException(
	 * appMessages.getResponseMessage(MessageKeys.DEALER_VAHANDETAILSNOTMAPPED)) ; }
	 * registrationDetailsDTO.getVahanDetails()
	 * .setDealerSelectedMakerName(registrationDetails.getVahanDetails().
	 * getDealerSelectedMakerName());
	 * registrationDetailsDTO.getVahanDetails().setDealerSelectedMakerClass(
	 * registrationDetails.getVahanDetails().getDealerSelectedMakerClass());
	 * registrationDetailsDTO.getVahanDetails()
	 * .setDealerSelectedBodyType(registrationDetails.getVahanDetails().
	 * getDealerSelectedBodyType()); }
	 * 
	 * } catch (Exception e) { throw new
	 * BadRequestException(appMessages.getResponseMessage(MessageKeys.
	 * DEALER_VAHANDETAILSNOTMAPPED)); } }
	 * 
	 * public Optional<VahanDetailsDTO> validateClassVehicleTypeForOther(String
	 * engineNo, String chasisNo, String cov) { Optional<VahanDetailsDTO>
	 * resultFromDB; try { resultFromDB =
	 * vahanDAO.findByEngineNumberOrChassisNumber(engineNo, chasisNo); if
	 * (!resultFromDB.isPresent()) { resultFromDB =
	 * vahanMapper.convertVO(restGateWayService.getVahanDetails(engineNo,
	 * chasisNo)); }
	 * 
	 * if (resultFromDB.isPresent()) { List<String> dealerCovTypeList = new
	 * ArrayList<>(); dealerCovTypeList.add(cov);
	 * 
	 * List<DealerAndVahanMappedCovDTO> dealerAndVahanMappedCovDTOList =
	 * dealerAndVahanMappedCovDAO .findByDealerCovTypeIn(dealerCovTypeList);
	 * 
	 * validateDealerVehicleTypeAndVahanVehicleType( dealerAndVahanMappedCovDTOList,
	 * resultFromDB.get());
	 * 
	 * if (null != resultFromDB.get().getDealerCovType()) {
	 * logger.info("Validated Vahan Class Type [{}].",
	 * resultFromDB.get().getVehicleClass()); return resultFromDB; } else { throw
	 * new
	 * BadRequestException("Dealer is unsupported vehicle class type, Please verify once."
	 * ); } } } catch (Exception e) { throw new BadRequestException(e.getMessage());
	 * } return resultFromDB; }
	 */

	@Override
	public void saveImages(String trNo, List<ImageInput> images, MultipartFile[] uploadfiles) throws IOException {
		Optional<StagingRegistrationDetailsDTO> registrationDetails = stagingRegistrationDetailsDAO
				.findByTrNoAndApplicationStatus(trNo, StatusRegistration.SLOTBOOKED.getDescription());
		if (!registrationDetails.isPresent()) {
			logger.error("Record Not found with TrNo [{}]", trNo);
			throw new BadRequestException(
					"Application Based on TrNo  and application Status as SLOTBOOKED is not found." + trNo);
		}
		StagingRegistrationDetailsDTO StagingRegistrationDetails = registrationDetails.get();
		if (StagingRegistrationDetails.getSlotDetails() == null
				|| registrationDetails.get().getSlotDetails().getSlotDate() == null) {
			logger.error("Slot Date Not Found for TrNo [{}]", trNo);
			throw new BadRequestException("Slot Date Not Found for TrNo" + trNo);
		}
		if (!LocalDate.now().equals(StagingRegistrationDetails.getSlotDetails().getSlotDate())) {
			logger.error("Today is Not Slot Booked Date [{}]", trNo);
			throw new BadRequestException("Today is Not Slot Booked Date");
		}
		saveEnclosures(StagingRegistrationDetails, images, uploadfiles);
	}

	@Override
	public void saveEnclosures(StagingRegistrationDetailsDTO registrationDetails, List<ImageInput> images,
			MultipartFile[] uploadfiles) throws IOException {
		List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = gridFsClient.convertImages(images,
				registrationDetails.getApplicationNo(), uploadfiles, StatusRegistration.INITIATED.getDescription());
		if (registrationDetails.getEnclosures() == null) {

			registrationDetails.setEnclosures(enclosures);
		} else {
			for (ImageInput image : images) {
				if (registrationDetails.getEnclosures().stream()
						.anyMatch(imagetype -> imagetype.getKey().equals(image.getType()))) {

					KeyValue<String, List<ImageEnclosureDTO>> matchedImage = registrationDetails.getEnclosures()
							.stream().filter(val -> val.getKey().equals(image.getType())).findFirst().get();
					registrationDetails.getEnclosures().remove(matchedImage);
					gridFsClient.removeImages(matchedImage.getValue());
				}
			}
			registrationDetails.getEnclosures().addAll(enclosures);
		}
		logMovingService.moveStagingToLog(registrationDetails.getApplicationNo());
		stagingRegistrationDetailsDAO.save(registrationDetails);
	}

	public Optional<KeyValue<String, List<ImageEnclosureDTO>>> getImages(
			List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures, ImageInput imageInput) {

		/*
		 * EnclosureType type = EnclosureType.getEnclosureType(imageInput.getType()); if
		 * (type.equals(EnclosureType.NONE)) { return Optional.empty(); }
		 */
		List<EnclosuresDTO> dtos = enclosuresDAO.findByServiceID(ServiceEnum.TEMPORARYREGISTRATION.getId());
		if (dtos.isEmpty()) {
			logger.error("Enclosures  is not found in master. [master_enclosures].");
			throw new BadRequestException("Enclosures  is not found in master_enclosures.");
		}

		logger.info("imageInput [{}]", imageInput.getType());
		for (EnclosuresDTO e : dtos) {
			logger.info("enclosure proof[{}]", e.getProof());
		}
		Optional<EnclosuresDTO> images = dtos.stream()
				.filter(dto -> dto.getProof().equalsIgnoreCase(imageInput.getType())).findFirst();

		if (!images.isPresent()) {
			return Optional.empty();
		}
		return enclosures.stream().filter(e -> images.get().getProof().equalsIgnoreCase(e.getKey())).findFirst();
	}

	public String getMakerName(String userId) {
		String makerName = StringUtils.EMPTY;
		Optional<UserDTO> userDetails = userDAO.findByUserId(userId);
		if (!userDetails.isPresent()) {
			throw new BadRequestException("no user Details found");
		}
		Optional<DealerMakerVO> dealerMakerVO = dealerMakerService.findDealerMakerByrId(userDetails.get().getRid());
		if (!dealerMakerVO.isPresent()) {
			throw new BadRequestException("Dealer maker Details are not present");
		}
		Optional<MakersVO> makersDTO = makersService.findMakerNameByMakerId(dealerMakerVO.get().getMmId());
		if (!makersDTO.isPresent()) {
			throw new BadRequestException("maker Details are not present");
		}
		makerName = makersDTO.get().getMakername();
		return makerName;
	}

	private StagingRegistrationDetailsDTO saveTrailerData(VahanDetailsVO vahanVO,
			RegistrationDetailsVO registrationDetails, StagingRegistrationDetailsDTO stagingDTO) {
		VahanDetailsDTO vahanDetailsDTO;
		Optional<VahanDetailsDTO> vahanDetails = vahanDAO.findByAppNo(stagingDTO.getApplicationNo());
		if (vahanDetails.isPresent()) {
			VahanDetailsDTO vahanDTO = vahanDetails.get();
			// VahanDetailsVO vahanDetailsVO =
			// validationForManufactureMonthYear(vahanVO);

			vahanDetailsDTO = vahanMapper.limitedFieldsVOtoDto(vahanDTO, vahanVO);
		} else {
			VahanDetailsDTO vahan = new VahanDetailsDTO();
			vahan.setAppNo(stagingDTO.getApplicationNo());
			vahanDetailsDTO = vahanMapper.limitedFieldsVOtoDto(vahan, vahanVO);
		}

		if (null != vahanVO.getIsNonVahanData()) {
			vahanDetailsDTO.setIsNonVahanData(vahanVO.getIsNonVahanData());
		}

		stagingDTO.setVahanDetails(vahanDetailsDTO);
		boolean trailerVehicle = trailerVehicleVerification(registrationDetails.getClassOfVehicle());

		if (trailerVehicle) {
			TrailerDetailsDTO result = getTrailerChasisNo(stagingDTO.getDealerDetails().getDealerId(), stagingDTO);
			if (result.getChassisNo().isEmpty()) {
				logger.error(
						"Problem occures while generating the chassis number with delar id[{}], and application Number[{}]",
						stagingDTO.getDealerDetails().getDealerId(), stagingDTO.getApplicationNo());
				throw new BadRequestException("Problem occures while generating the chassis number");
			}
			vahanDetailsDTO.setChassisNumber(result.getChassisNo());
		}

		vahanDetailsDTO.setCreatedeDate(LocalDateTime.now());
		vahanDAO.save(vahanDetailsDTO);
		return stagingDTO;
	}

	private boolean trailerVehicleVerification(String classOfVehicle) {
		List<String> trailesCovs = new ArrayList<>();
		trailesCovs.add(ClassOfVehicleEnum.TTTT.getCovCode());
		trailesCovs.add(ClassOfVehicleEnum.TTRN.getCovCode());
		boolean value = trailesCovs.stream().anyMatch(e -> classOfVehicle.equals(classOfVehicle));
		return value;
	}

	/*
	 * private VahanDetailsVO validationForManufactureMonthYear(VahanDetailsVO
	 * vahanVO) { String manufacturedMonthYear = vahanVO.getManufacturedMonthYear();
	 * int staggingYear = Integer.parseInt(manufacturedMonthYear.substring(2, 6));
	 * int currentYear = LocalDate.now().getYear();
	 * DateConverters.convertStirngToGetMonthAndYear(LocalDate.now().toString()) ;
	 * LocalDate validatedDate2 =
	 * DateConverters.convertStirngToGetMonthAndYear(LocalDate.now().toString()) ;
	 * int dateCheck = validatedDate1.compareTo(date) *
	 * date.compareTo(validatedDate2); if (dateCheck <= 0) { throw new
	 * BadRequestException("Manufacture Month and Year is Exceeds for the Registration"
	 * ); } if (manufacturedMonthYear == null ||
	 * !manufacturedMonthYear.matches("(0?[1-9]|1[012])((19|20)\\d\\d)") ||
	 * !(staggingYear<=currentYear)) return vahanVO; SimpleDateFormat sdf = new
	 * SimpleDateFormat("mmyyyy"); sdf.setLenient(false); try {
	 * sdf.parse(manufacturedMonthYear); return vahanVO; } catch (ParseException ex)
	 * { return vahanVO; }
	 * 
	 * }
	 */

	private boolean validateYearMonth(final String chasisBuildMonthYear) {

		int staggingYear = Integer.parseInt(chasisBuildMonthYear.substring(2, 6));
		int currentYear = LocalDate.now().getYear();
		if (chasisBuildMonthYear == null || !chasisBuildMonthYear.matches("(0?[1-9]|1[012])((19|20)\\d\\d)")
				|| !(staggingYear <= currentYear))
			return false;
		SimpleDateFormat sdf = new SimpleDateFormat("mmyyyy");
		sdf.setLenient(false);
		try {
			sdf.parse(chasisBuildMonthYear);
			return true;
		} catch (ParseException ex) {
			return false;
		}

	}

	@Override
	public TrailerDetailsDTO getTrailerChasisNo(String userId, StagingRegistrationDetailsDTO stageDetails) {

		Optional<UserDTO> userDetails = userDAO.findByUserId(userId);
		TrailerDetailsDTO trailerDetailsDTO = new TrailerDetailsDTO();
		synchronized (userId.intern()) {

			if (!userDetails.isPresent()) {
				logger.debug("UserDetails is Empty.Please verify once for Application[{}]",
						stageDetails.getApplicationNo());
				logger.error("UserDetails is Empty.Please verify once for Application [{}]",
						stageDetails.getApplicationNo());
				throw new BadRequestException("UserDetails is Empty.Please verify once for Application ["
						+ stageDetails.getApplicationNo() + "]");
			}

			Optional<OfficeDTO> office = officeDAO.findByOfficeCode(userDetails.get().getOffice().getOfficeCode());

			Optional<TrailerCodesDTO> trailerCodesDTOOptional = trailerCodesDAO.findByRIdAndDistrictIdAndMakerClass(
					userDetails.get().getRid(), office.get().getDistrict(),
					stageDetails.getVahanDetails().getMakersModel());

			if (!trailerCodesDTOOptional.isPresent()) {
				logger.debug("Unauthorized Dealer to Create Trailer ChassisNo. with the UserId [{}], and MID,[{}]",
						userDetails.get().getUserId(), userDetails.get().getRid());
				logger.error("Unauthorized Dealer to Create Trailer ChassisNo or Invalid Maker Class Choosen");
				throw new BadRequestException(
						"Unauthorized Dealer to Create Trailer ChassisNo or Invalid Maker Class Choosen");
			}

			if (!stageDetails.getVahanDetails().getMakersModel()
					.equalsIgnoreCase(trailerCodesDTOOptional.get().getMakerClass())
					|| trailerCodesDTOOptional.get().getMakerClass() == null
					|| trailerCodesDTOOptional.get().getMakerClass().isEmpty()) {
				logger.debug("Unauthorized Dealer to Create Trailer ChassisNo. with the UserId [{}], and MID,[{}]",
						userDetails.get().getUserId(), userDetails.get().getRid());
				logger.error("Unauthorized Dealer to Create Trailer ChassisNo  for the perticular Maker Class.");
				throw new BadRequestException(
						"Unauthorized Dealer to Create Trailer ChassisNo  for the perticular Maker Class.");
			}

			String chasisBuildYearMonth = stageDetails.getVahanDetails().getManufacturedMonthYear();
			if (!validateYearMonth(chasisBuildYearMonth)) {
				logger.debug("Date format mismatch(MMYYYY) or Future Date selected [{}]", chasisBuildYearMonth);
				logger.error("Date format mismatch(MMYYYY) or future date selected");
				throw new BadRequestException("Date format mismatch(MMYYYY) or future date selected");
			}
			String manufacturedMonth = StringUtils.EMPTY, manufacturedYear = StringUtils.EMPTY,
					manufacturedMonthCode = StringUtils.EMPTY;
			manufacturedMonth = chasisBuildYearMonth.substring(0, 2);
			manufacturedYear = chasisBuildYearMonth.substring(4, 6);
			if (stageDetails.getVahanDetails().getChassisNumber() == null) {
				Optional<PropertiesDTO> propertiesDTO = propertiesDAO.findByStatusTrue();

				Map<String, String> monthCode = propertiesDTO.get().getMonthCode();
				if (monthCode.get(manufacturedMonth) != null) {
					logger.debug("Manufactured Month Code coming [{}]", monthCode.get(manufacturedMonth));
					manufacturedMonthCode = monthCode.get(manufacturedMonth);
				}

				Map<String, String> officeCodeMap = new TreeMap<>();
				officeCodeMap.put("officeCode", userDetails.get().getOffice().getOfficeCode());
				officeCodeMap.put("year", chasisBuildYearMonth.substring(2, 6));
				String serialNo = sequenceGenerator.getSequence(String.valueOf(Sequence.DEALERCHASISNO.getSequenceId()),
						officeCodeMap);
				logger.debug("Serial No for the Trailer Dealer [{}]", serialNo);

				StringBuilder trailerChassisSeries = new StringBuilder();
				trailerChassisSeries.append(trailerCodesDTOOptional.get().getStateCode());
				trailerChassisSeries.append(trailerCodesDTOOptional.get().getDistrictCode());
				trailerChassisSeries.append(trailerCodesDTOOptional.get().getDealerCode());
				trailerChassisSeries.append(manufacturedYear);
				trailerChassisSeries.append(manufacturedMonthCode);
				trailerChassisSeries.append(serialNo);
				trailerChassisSeries.append(trailerCodesDTOOptional.get().getWeightCode());
				trailerChassisSeries.append(trailerCodesDTOOptional.get().getWheelType());
				trailerChassisSeries.append(trailerCodesDTOOptional.get().getTrailerType());

				Optional<TrailerDetailsDTO> trailerDetailsDTOOptional = trailerDetailsDAO
						.findByChassisNo(trailerChassisSeries.toString());
				if (trailerDetailsDTOOptional.isPresent()) {
					logger.debug("Generated ChassisNo already exists [{}]",
							trailerDetailsDTOOptional.get().getChassisNo());
					logger.error("Generated ChassisNo already exists");
					throw new BadRequestException("Generated ChassisNo already exists");
				}
				trailerDetailsDTO.setApplicationNo(stageDetails.getApplicationNo());
				trailerDetailsDTO.setChassisNo(trailerChassisSeries.toString());
				trailerDetailsDTO.setCov(stageDetails.getClassOfVehicle());
				trailerDetailsDTO.setLoginId(userDetails.get().getUserId());
				trailerDetailsDTO.setrId(userDetails.get().getRid());
				trailerDetailsDTO.setStatus("active");
				trailerDetailsDTO.setCreatedDate(LocalDateTime.now());
				trailerDetailsDAO.save(trailerDetailsDTO);
			} else {
				trailerDetailsDTO.setChassisNo(stageDetails.getVahanDetails().getChassisNumber());
			}
		}
		return trailerDetailsDTO;
	}

	@Override
	public SpecialNumberDetailsReport getSpecialNumberDetailsForStagingDetails(
			StagingRegistrationDetailsDTO stagingDetails) {
		SpecialNumberDetailsReport details = new SpecialNumberDetailsReport();

		ApplicantDetailsVO applicantDetailsVO = applicantDetailsMapper
				.convertRequiredEntity(stagingDetails.getApplicantDetails());
		VahanDetailsVO vahanDetailsVO = vahanMapper.convertRequiredEntity(stagingDetails.getVahanDetails());
		List<SpecialNumberDetailsByTRVO> reportVo = rtaReportServiceImpl.getSpecialNumberDetailsByTrNO(stagingDetails);
		details.setApplicantDetails(applicantDetailsVO);
		details.setVahanDetails(vahanDetailsVO);
		details.setReportResponseVO(reportVo);
		return details;
	}

	/**
	 * @param registrationDetails
	 * @param propertiesDTO2
	 */
	private void insuranceValidationForOtherState(RegServiceVO registrationDetails, PropertiesDTO propertiesDTO) {
		try {
			if (propertiesDTO.getIsInsuranceValidationInStagingForOtherState()) {
				insuranceValidationInStagingForOtherState(registrationDetails);
			}
			if (propertiesDTO.getIsInsuranceValidationInRegForOtherState()) {
				insuranceValidationInRegForOtherState(registrationDetails);
			}
			/*
			 * List<RegServiceDTO> resultForInsuarance = regServiceDAO
			 * .findByRegistrationDetailsInsuranceDetailsCompanyAndRegistrationDetailsInsuranceDetailsPolicyNumber(
			 * registrationDetails.getRegistrationDetails().getInsuranceDetails(
			 * ).getCompany (),
			 * registrationDetails.getRegistrationDetails().getInsuranceDetails( ).
			 * getPolicyNumber());
			 * 
			 * if (null != resultForInsuarance && !resultForInsuarance.isEmpty()) {
			 * 
			 * for (RegServiceDTO stagingRegistrationDetailsDTO : resultForInsuarance) { if
			 * (stagingRegistrationDetailsDTO.getRegistrationDetails().
			 * getInsuranceDetails() .getCompany()
			 * .equals(registrationDetails.getRegistrationDetails(). getInsuranceDetails().
			 * getCompany()) && stagingRegistrationDetailsDTO.getRegistrationDetails().
			 * getInsuranceDetails() .getPolicyNumber().equals(registrationDetails.
			 * getRegistrationDetails() .getInsuranceDetails().getPolicyNumber())
			 * 
			 * && !stagingRegistrationDetailsDTO.getApplicationNo()
			 * .equals(registrationDetails.getApplicationNo()) ) { logger.info(
			 * "Insurance Deatils is matching with other application details [{}], Please verify Once"
			 * , stagingRegistrationDetailsDTO.getApplicationNo()); throw new
			 * BadRequestException("Duplicate insurance details [" +
			 * stagingRegistrationDetailsDTO.getApplicationNo() + "]"); } } }
			 */
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new BadRequestException(e.getMessage());
		}
	}

	private void insuranceValidationInStagingForOtherState(RegServiceVO registrationDetails) {
		try {
			List<StagingRegistrationDetailsDTO> resultForInsuarance = stagingRegistrationDetailsDAO
					.findByInsuranceDetailsCompanyAndInsuranceDetailsPolicyNumber(
							registrationDetails.getRegistrationDetails().getInsuranceDetails().getCompany(),
							registrationDetails.getRegistrationDetails().getInsuranceDetails().getPolicyNumber());

			if (null != resultForInsuarance && !resultForInsuarance.isEmpty()) {

				for (StagingRegistrationDetailsDTO stagingRegistrationDetailsDTO : resultForInsuarance) {
					if (stagingRegistrationDetailsDTO.getInsuranceDetails().getCompany()
							.equals(registrationDetails.getRegistrationDetails().getInsuranceDetails().getCompany())
							&& stagingRegistrationDetailsDTO.getInsuranceDetails().getPolicyNumber()
									.equals(registrationDetails.getRegistrationDetails().getInsuranceDetails()
											.getPolicyNumber())
					/*
					 * && !stagingRegistrationDetailsDTO.getApplicationNo()
					 * .equals(registrationDetails.getApplicationNo())
					 */) {
						logger.info(
								"Insurance Deatils is matching with other application details [{}], Please verify Once",
								stagingRegistrationDetailsDTO.getApplicationNo());
						throw new BadRequestException("Duplicate insurance details ["
								+ stagingRegistrationDetailsDTO.getApplicationNo() + "]");
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new BadRequestException(e.getMessage());
		}
	}

	private void insuranceValidationInRegForOtherState(RegServiceVO registrationDetails) {
		try {
			List<RegistrationDetailsDTO> resultForInsuarance = registrationDetailDAO
					.findByInsuranceDetailsCompanyAndInsuranceDetailsPolicyNumber(
							registrationDetails.getRegistrationDetails().getInsuranceDetails().getCompany(),
							registrationDetails.getRegistrationDetails().getInsuranceDetails().getPolicyNumber());

			if (null != resultForInsuarance && !resultForInsuarance.isEmpty()) {

				for (RegistrationDetailsDTO stagingRegistrationDetailsDTO : resultForInsuarance) {
					if (stagingRegistrationDetailsDTO.getInsuranceDetails().getCompany()
							.equals(registrationDetails.getRegistrationDetails().getInsuranceDetails().getCompany())
							&& stagingRegistrationDetailsDTO.getInsuranceDetails().getPolicyNumber()
									.equals(registrationDetails.getRegistrationDetails().getInsuranceDetails()
											.getPolicyNumber())
					/*
					 * && !stagingRegistrationDetailsDTO.getApplicationNo()
					 * .equals(registrationDetails.getApplicationNo())
					 */) {
						logger.info(
								"Insurance Deatils is matching with other application details [{}], Please verify Once",
								stagingRegistrationDetailsDTO.getApplicationNo());
						throw new BadRequestException("Duplicate insurance details ["
								+ stagingRegistrationDetailsDTO.getApplicationNo() + "]");
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new BadRequestException(e.getMessage());
		}
	}

	@Override
	public List<RejectionHistoryVO> viewSecondVehicleData(String applicationNo) {
		List<RejectionHistoryVO> rejectionHistoryVO = new ArrayList<RejectionHistoryVO>();
		List<RejectionHistoryDTO> rejectionList = new ArrayList<RejectionHistoryDTO>();
		List<RejectionHistoryDTO> rejectionHistoryList = new ArrayList<RejectionHistoryDTO>();
		Optional<StagingRegistrationDetailsDTO> stagingOpt = stagingRegistrationDetailsDAO
				.findByApplicationNo(applicationNo);
		if (stagingOpt.isPresent()) {
			StagingRegistrationDetailsDTO stagingDTO = stagingOpt.get();
			RejectionHistoryDTO rejectionDTO = stagingDTO.getRejectionHistory();
			if (rejectionDTO != null && rejectionDTO.getIsSecondVehicleRejected() != null
					&& rejectionDTO.getIsSecondVehicleRejected()) {
				rejectionList.add(rejectionDTO);
			}
			rejectionHistoryList = stagingDTO.getRejectionHistoryLog();
			if (rejectionHistoryList != null && !rejectionHistoryList.isEmpty()) {
				for (RejectionHistoryDTO rejection : rejectionHistoryList) {
					if (rejection.getIsSecondVehicleRejected() != null && rejection.getIsSecondVehicleRejected()) {
						rejectionList.add(rejection);
					}
				}
			}

		}
		rejectionHistoryVO = rejectionHistoryMapper.convertEntity(rejectionList);
		return rejectionHistoryVO;

	}

	@Override
	public DealerDetailsVO saveDealerMgmtDetails(String dealerDetailsVO, MultipartFile[] uploadfiles) {
		if (StringUtils.isBlank(dealerDetailsVO)) {
			logger.error("Input Details are required.");
			throw new BadRequestException("Input Details are required.");
		}
		Optional<DealerDetailsVO> inputOptional = readValue(dealerDetailsVO, DealerDetailsVO.class);
		if (!inputOptional.isPresent()) {
			logger.error("Invalid Input Details.");
			throw new BadRequestException("Invalid Input Details.");
		}

		try {
			/*
			 * DealerDetailsVO inputDealerDetailsVO = inputOptional.get(); if
			 * (!inputDealerDetailsVO.getServiceType().contains(ServiceEnum. DATAENTRY) &&
			 * !inputDealerDetailsVO.getServiceIds().contains(ServiceEnum.
			 * DATAENTRY.getId()) ) { throw new
			 * BadRequestException("Invalid Service Type."); }
			 * //inputDealerDetailsVO=this.otherStateValidations( inputDealerDetailsVO);
			 * DealerDetailsDTO dealerDetailsDTO=this.verifyAadharAndVahanDetails(
			 * inputDealerDetailsVO); //updateEnclosureForDataEntry(uploadfiles,
			 * inputDealerDetailsVO, dealerDetailsDTO, null); Optional<CitizenEnclosuresDTO>
			 * citizenEnclosuresOpt = citizenEnclosuresDAO
			 * .findByApplicationNoAndServiceIdsIn(dealerDetailsDTO. getApplicationNo(),
			 * dealerDetailsDTO.getServiceIds()); if (!citizenEnclosuresOpt.isPresent()) {
			 * saveCitizenEnclosureDetails(inputDealerDetailsVO, dealerDetailsDTO, new
			 * CitizenEnclosuresDTO()); } else {
			 * saveCitizenEnclosureDetails(inputDealerDetailsVO, dealerDetailsDTO,
			 * citizenEnclosuresOpt.get()); }
			 * 
			 * dealerDetailsDAO.save(dealerDetailsDTO); return
			 * dealerDetailsMapper.convertEntity(dealerDetailsDTO);
			 */
			return null;
		} catch (Exception e) {
			logger.error("Exception [{}] occured", e.getMessage());
			throw new BadRequestException(e.getMessage());
		}
	}

	// Other state Telagana 02-06-2014
	@Override
	public Boolean telaganaVechicle(String applicationNo) {
		Optional<PropertiesDTO> propertiesDto = propertiesDAO.findByStatusTrue();
		if (!propertiesDto.isPresent()) {
			logger.error("Properties details not found, Please contact to RTA admin");
			throw new BadRequestException("Properties details not found, Please contact to RTA admin");
		}
		Optional<RegServiceDTO> regService = regServiceDAO.findByApplicationNo(applicationNo);
		if (regService.isPresent()) {
			RegServiceVO inputRegServiceVO = regServiceMapper.convertEntity(regService.get());
			if (isVCRRequiredTS(inputRegServiceVO, propertiesDto.get())) {
				inputRegServiceVO.getRegistrationDetails().setTaxPaidByVcr(true);
				return true;
			}
		}
		return false;

	}

	private boolean isVCRRequiredTS(RegServiceVO inputRegServiceVO, PropertiesDTO propertiesDTO) {
		Boolean taxRequired = false;
		if (inputRegServiceVO.getRegistrationDetails().getVahanDetails() != null
				&& inputRegServiceVO.getRegistrationDetails().getVahanDetails().getVehicleClass() != null
				&& inputRegServiceVO.getRegistrationDetails().getVahanDetails().getSeatingCapacity() != null
				&& inputRegServiceVO.getRegistrationDetails().getVahanDetails().getGvw() != null) {
			Optional<List<TaxTypeVO>> taxTypeVOList = infoService.taxDataEntryType(
					inputRegServiceVO.getRegistrationDetails().getVahanDetails().getVehicleClass(),
					inputRegServiceVO.getRegistrationDetails().getVahanDetails().getSeatingCapacity(),
					inputRegServiceVO.getRegistrationDetails().getVahanDetails().getGvw().toString());
			if (taxTypeVOList.isPresent()) {
				for (TaxTypeVO taxType : taxTypeVOList.get()) {
					// taxTypeVOList.get().stream().forEach(taxType -> {
					if (taxType.getTaxId() != null && taxType.getTaxId().equals(TaxTypeEnum.LifeTax.getCode())) {
						taxRequired = true;
					}
				}
				// });
			}
		}
		if ((StringUtils.isNoneBlank(inputRegServiceVO.getRegistrationDetails().getPrNo())
				&& inputRegServiceVO.getnOCDetails() != null
				&& inputRegServiceVO.getnOCDetails().getState().equals(propertiesDTO.getStateName())
				&& inputRegServiceVO.getRegistrationDetails().getPrIssueDate()
						.isBefore(DateConverters.convertStirngTOlocalDate(propertiesDTO.getRegDate())))) {
			if (inputRegServiceVO.getRegistrationDetails().getPrNo() != null) {
				List<TaxDetailsDTO> taxDetails = taxDetailsDAO
						.findFirst10ByPrNoOrderByCreatedDateDesc(inputRegServiceVO.getRegistrationDetails().getPrNo());
				if (taxDetails.size() > 0 && taxRequired) {
					TaxDetailsDTO taxDetails1 = taxDetails.stream().findFirst().get();
					if (taxDetails1.getPaymentPeriod() != null
							&& taxDetails1.getPaymentPeriod().equalsIgnoreCase("LifeTax"))
						return Boolean.TRUE;
				}

			}
		}
		return Boolean.FALSE;
	}

	/** TOW Details for OtherState **/
	public void saveBuyerdetails(RegServiceVO inputRegServiceVO, RegServiceDTO regServiceDTO) {
		TrasnferOfOwnerShipDTO trasnferOfOwnerShipDTO = new TrasnferOfOwnerShipDTO();
		regServiceDTO = registrationService.getPresentAddress(inputRegServiceVO, regServiceDTO);
		OwnershipVO ownershipVO = new OwnershipVO();
		ownershipVO.setDescription(regServiceDTO.getRegistrationDetails().getOwnerType().toString());
		if (!regServiceDTO.getRegistrationDetails().getOwnerType().equals(OwnerTypeEnum.Individual)) {
			if (inputRegServiceVO.getBasicApplicantDetails().getEntityName() == null) {
				throw new BadRequestException("Entity Name is Mandatory for other than Individual vehicles");
			}
		}
		// regServiceDTO.getBasicApplicantDetails().setApplicantNo(getTransactionId());
		trasnferOfOwnerShipDTO.setBuyerApplicantDetails(
				applicantDetailsMapper.convertVO(inputRegServiceVO.getBasicApplicantDetails()));
		trasnferOfOwnerShipDTO.getBuyerApplicantDetails().setApplicantNo(getTransactionId());
		trasnferOfOwnerShipDTO.getBuyerApplicantDetails()
				.setAadharResponse(regServiceDTO.getRegistrationDetails().getApplicantDetails().getAadharResponse());
		trasnferOfOwnerShipDTO.setTransferType(TransferType.SALE);
		trasnferOfOwnerShipDTO.setBuyer(OwnerType.BUYER);
		trasnferOfOwnerShipDTO.setSeller(OwnerType.SELLER);
		trasnferOfOwnerShipDTO.setOwnerShipType(ownerShipMapper.convertVO(ownershipVO));
		trasnferOfOwnerShipDTO.getBuyerApplicantDetails().setNationality(NationalityEnum.INDIAN.toString());
		trasnferOfOwnerShipDTO.getBuyerApplicantDetails().setIsAadhaarValidated(Boolean.TRUE);
		trasnferOfOwnerShipDTO
				.setBuyerAadhaarNo(regServiceDTO.getRegistrationDetails().getApplicantDetails().getAadharNo());
		trasnferOfOwnerShipDTO.getBuyerApplicantDetails()
				.setAadharNo(regServiceDTO.getRegistrationDetails().getApplicantDetails().getAadharNo());
		trasnferOfOwnerShipDTO.setBuyerUUID(
				regServiceDTO.getRegistrationDetails().getApplicantDetails().getAadharResponse().getUuId());
		trasnferOfOwnerShipDTO.setBuyerServiceType(regServiceDTO.getServiceType());
		regServiceDTO.setBuyerDetails(trasnferOfOwnerShipDTO);
		if (regServiceDTO.getServiceIds().contains(ServiceEnum.HPA.getId())) {
			regServiceDTO.getBuyerDetails().setBuyerFinanceStatus(FinanceTowEnum.getFinanceTowEnumById(3).toString());
		}
		if (inputRegServiceVO.getRegistrationDetails() != null
				&& inputRegServiceVO.getRegistrationDetails().getIsFinancier()
				&& inputRegServiceVO.getOtherStateFianContinue()) {
			if (inputRegServiceVO.getRegistrationDetails() != null
					&& inputRegServiceVO.getRegistrationDetails().getIsFinancier()
					&& inputRegServiceVO.getOtherStateFianContinue()) {
				regServiceDTO.getBuyerDetails()
						.setSellerFinanceStatus(FinanceTowEnum.getFinanceTowEnumById(1).toString());
				regServiceDTO.getBuyerDetails()
						.setBuyerFinanceStatus(FinanceTowEnum.getFinanceTowEnumById(1).toString());
				regServiceDTO.getBuyerDetails()
						.setSellerFinanceType(FinanceTowEnum.getFinanceTowEnumById(7).toString());
			}
		}
		if (regServiceDTO.getServiceIds().contains(ServiceEnum.HPA.getId())) {
			regServiceDTO.getBuyerDetails().setBuyerFinanceStatus(FinanceTowEnum.getFinanceTowEnumById(3).toString());
		}

	}

	/** HPA DETAILS for Other State **/
	public void saveFinancierToken(RegServiceVO inputRegServiceVO, RegServiceDTO regServiceDTO) {
		regServiceDTO = registrationService.generateHPAToken(regServiceDTO, regServiceDTO.getOfficeCode());
	}

	// Reassignment for Other state
	private RegServiceDTO reassignmentEnabledOrDisbled(RegServiceDTO regServiceDTO, RegServiceVO inputRegServiceVO) {
		Optional<PropertiesDTO> propertiesDto = propertiesDAO.findByStatusTrue();
		if (!propertiesDto.isPresent()) {
			throw new BadRequestException("Properties details not found, Please contact to RTA admin");
		}
		PropertiesDTO propertiesDTO = propertiesDto.get();
		if ((StringUtils.isNoneBlank(inputRegServiceVO.getRegistrationDetails().getPrNo())
				&& inputRegServiceVO.getnOCDetails() != null
				&& inputRegServiceVO.getnOCDetails().getState().equals(propertiesDTO.getStateName())
				&& inputRegServiceVO.getRegistrationDetails().getPrIssueDate()
						.isBefore(DateConverters.convertStirngTOlocalDate(propertiesDTO.getRegDate())))) {
			regServiceDTO.getRegistrationDetails().setAllowForReassignment(false);

		} else if (StringUtils.isNoneBlank(inputRegServiceVO.getRegistrationDetails().getPrNo())
				&& inputRegServiceVO.getRegistrationDetails().getApplicantType().equalsIgnoreCase("OTHERSTATE")) {
			regServiceDTO.getRegistrationDetails().setAllowForReassignment(true);
			if (StringUtils.substring(inputRegServiceVO.getRegistrationDetails().getPrNo(), 0, 2)
					.equalsIgnoreCase("AP")) {
				regServiceDTO.getRegistrationDetails().setAllowForReassignment(Boolean.FALSE);
				// srinivas --prNo contains ap no need to apply Reassignment
			}
		} else {
			regServiceDTO.getRegistrationDetails().setAllowForReassignment(false);
		}

		return regServiceDTO;

	}

	private String getosManufacturedMonthYear(String mftDate) {
		StringTokenizer multiTokenizer = new StringTokenizer(mftDate, "-");
		StringBuffer newName = new StringBuffer();
		while (multiTokenizer.hasMoreTokens()) {
			newName.append(multiTokenizer.nextToken().trim());
		}
		return newName.toString();
	}

	private void validateOtherStateNOCDetails(Optional<RegServiceDTO> regDatEntry) {
		if (regDatEntry.get().getApplicationStatus().equals(StatusRegistration.CITIZENSUBMITTED)
				|| regDatEntry.get().getApplicationStatus().equals(StatusRegistration.APPROVED)
				|| regDatEntry.get().getApplicationStatus().equals(StatusRegistration.PAYMENTDONE)) {
			if (!regDatEntry.get().getServiceIds().contains(ServiceEnum.ISSUEOFNOC.getId())) {
				throw new BadRequestException("Vahan Details Already exist with Application No:" + "["
						+ regDatEntry.get().getApplicationNo() + "]");
			}

		}
	}

	private RegServiceDTO convertTOuperCase(RegServiceDTO regServiceDTO) {
		if (StringUtils.isNoneEmpty(regServiceDTO.getPrNo())) {
			regServiceDTO.setPrNo(regServiceDTO.getPrNo().toUpperCase());
			regServiceDTO.getRegistrationDetails()
					.setPrNo(regServiceDTO.getRegistrationDetails().getPrNo().toUpperCase());
		}
		if (StringUtils.isNoneEmpty(regServiceDTO.getRegistrationDetails().getTrNo())) {
			regServiceDTO.getRegistrationDetails()
					.setTrNo(regServiceDTO.getRegistrationDetails().getTrNo().toUpperCase());
		}
		if (StringUtils.isNoneEmpty(regServiceDTO.getRegistrationDetails().getVahanDetails().getEngineNumber())) {
			regServiceDTO.getRegistrationDetails().getVahanDetails().setEngineNumber(
					regServiceDTO.getRegistrationDetails().getVahanDetails().getEngineNumber().toUpperCase());
		}
		if (StringUtils.isNoneEmpty(regServiceDTO.getRegistrationDetails().getVahanDetails().getChassisNumber())) {
			regServiceDTO.getRegistrationDetails().getVahanDetails().setChassisNumber(
					regServiceDTO.getRegistrationDetails().getVahanDetails().getChassisNumber().toUpperCase());
		}
		if (StringUtils.isNoneEmpty(regServiceDTO.getRegistrationDetails().getVehicleDetails().getEngineNumber())) {
			regServiceDTO.getRegistrationDetails().getVehicleDetails().setEngineNumber(
					regServiceDTO.getRegistrationDetails().getVehicleDetails().getEngineNumber().toUpperCase());
		}
		if (StringUtils.isNoneEmpty(regServiceDTO.getRegistrationDetails().getVehicleDetails().getChassisNumber())) {
			regServiceDTO.getRegistrationDetails().getVehicleDetails().setChassisNumber(
					regServiceDTO.getRegistrationDetails().getVehicleDetails().getChassisNumber().toUpperCase());
		}
		return regServiceDTO;

	}

	@Override
	public PropertiesVO getAddressDropDown(String ownerType) {
		Optional<PresentAddressDropDownDTO> addressDropDown = presentAddressDropDownDAO
				.findByOwnerTypeAndAddressDropDownEnabledTrue(ownerType);
		PropertiesVO propVO = new PropertiesVO();
		if (addressDropDown.isPresent()) {
			propVO.setAddressDropdown(addressDropDown.get().getAddressItem());
			return propVO;
		}
		logger.debug("Address Items not available/it configured false in DB");
		propVO.setAddressDropdown(Collections.emptyList());
		return propVO;
	}

	@Override
	public boolean checkApprovalsNeedorNot(StagingRegistrationDetailsDTO stagingDTO) {
		String ownerType = stagingDTO.getOwnerType().toString();
		Optional<AutomationDTO> automationDTO = automationMasterDAO.findByOwnerTypeAndIsOwnerTypeEnabledTrue(ownerType);
		if (!automationDTO.isPresent() || StringUtils.isBlank(stagingDTO.getClassOfVehicle())) {
			return Boolean.FALSE;
		}
		AutomationDTO automation = automationDTO.get();
		switch (stagingDTO.getOwnerType().toString()) {
		case "Individual":
			if (stagingDTO.getVehicleType().equals(CovCategory.N.getCode())
					&& automation.getNonTransportcovs() != null) {
				if (CollectionUtils.isNotEmpty(automation.getNonTransportcovs().getAutoBasedOnConditionCovs())
						&& automation.getNonTransportcovs().getAutoBasedOnConditionCovs()
								.contains(stagingDTO.getClassOfVehicle())) {
					if (stagingDTO.getRejectionHistory() != null
							&& stagingDTO.getRejectionHistory().getIsSecondVehicleRejected() != null
							&& stagingDTO.getRejectionHistory().getIsSecondVehicleRejected()
							&& !stagingDTO.isSecondVehicleTaxPaid()) {
						return Boolean.FALSE;
					}
					if (stagingDTO.getInvoiceDetails().getInvoiceValue() > automationDTO.get().getInvoiceAmount()) {
						return Boolean.TRUE;
					}
					if ((null != stagingDTO.getIsFirstVehicle() && stagingDTO.getIsFirstVehicle())) {
						return Boolean.FALSE;
					}
				}
				return getAutomationNotRequiredForNonTransport(automation, stagingDTO.getClassOfVehicle());
			}
			if (stagingDTO.getVehicleType().equals(CovCategory.T.getCode()) && automation.getTransportCovs() != null) {
				return getAutomationNotRequiredForTransport(automation, stagingDTO.getClassOfVehicle());
			}
			return Boolean.TRUE;
		case "Government":
			return Boolean.FALSE;
		case "Stu":
			return Boolean.FALSE;
		case "Organization":
			return Boolean.FALSE;
		case "POLICE":
			return Boolean.FALSE;
		case "Diplomatic":
			return Boolean.FALSE;
		case "Company":
			return Boolean.FALSE;
		default:
			return Boolean.FALSE;
		}
	}

	private Boolean getAutomationNotRequiredForNonTransport(AutomationDTO automation, String covCode) {
		if (CollectionUtils.isNotEmpty(automation.getNonTransportcovs().getAutomationNotRequiredCovs())
				&& automation.getNonTransportcovs().getAutomationNotRequiredCovs().contains(covCode)) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	private Boolean getAutomationNotRequiredForTransport(AutomationDTO automation, String covCode) {
		if (CollectionUtils.isNotEmpty(automation.getTransportCovs().getAutomationNotRequiredCovs())
				&& automation.getTransportCovs().getAutomationNotRequiredCovs().contains(covCode)) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	private void onlineVcrCheck(StagingRegistrationDetailsDTO registrationDetailsDTO,
			RegistrationDetailsVO registrationDetailsVO) {
		VcrInputVo vcrInputVo = new VcrInputVo();
		if (registrationDetailsDTO != null && registrationDetailsDTO.getVahanDetails() != null
				&& StringUtils.isNoneBlank(registrationDetailsDTO.getVahanDetails().getChassisNumber())) {
			vcrInputVo.setChassisNo(registrationDetailsDTO.getVahanDetails().getChassisNumber());
			// registrationService.checkonlineVcrDetailsForDataEntry(vcrInputVo);
			checkVcrAtDealer(vcrInputVo);
		}
		if (registrationDetailsVO != null && registrationDetailsVO.getVahanDetails() != null
				&& StringUtils.isNoneBlank(registrationDetailsVO.getVahanDetails().getChassisNumber())) {
			vcrInputVo.setChassisNo(registrationDetailsVO.getVahanDetails().getChassisNumber());
			// registrationService.checkonlineVcrDetailsForDataEntry(vcrInputVo);
			checkVcrAtDealer(vcrInputVo);
		}

	}

	private void checkVcrAtDealer(VcrInputVo vcrInputVo) {
		List<VcrFinalServiceDTO> vcrList = vcrFinalServiceDAO
				.findByRegistrationChassisNumberAndIsVcrClosedIsFalse(vcrInputVo.getChassisNo());
		if (vcrList != null && !vcrList.isEmpty()) {
			List<VcrFinalServiceDTO> vcrWithOutCahs = vcrList.stream()
					.filter(paymentDone -> paymentDone.getPaymentType() == null
							|| !paymentDone.getPaymentType().equalsIgnoreCase(GatewayTypeEnum.CASH.getDescription()))
					.collect(Collectors.toList());
			if (vcrWithOutCahs != null && !vcrWithOutCahs.isEmpty()) {
				VcrFinalServiceDTO vcrDto = rtaService.checkVehicleTrNotGenerated(vcrWithOutCahs);
				if (vcrDto != null && vcrDto.getPartiallyClosed() != null && vcrDto.getPartiallyClosed()) {
					if (vcrWithOutCahs.size() == 1) {
						return;
					}
				}
				throw new BadRequestException("VCR details found. VCR number is: "
						+ vcrList.stream().findFirst().get().getVcr().getVcrNumber());
			}
		}
	}

	@Override
	public PropertiesVO getDeclartionDetails() {
		Optional<PresentAddressDropDownDTO> addressDropDown = presentAddressDropDownDAO
				.findByIsDeclarationEnabledTrue();
		PropertiesVO propVO = new PropertiesVO();
		if (addressDropDown.isPresent()) {
			propVO.setDeclarationStart(addressDropDown.get().getDeclarationStart());
			propVO.setDeclarationEnd(addressDropDown.get().getDeclarationEnd());
			List<DeclarationsVO> list = new ArrayList<>();
			for (DeclarationsDTO dto : addressDropDown.get().getDeclartionDetails()) {
				DeclarationsVO vo = new DeclarationsVO();
				vo.setDeclaration(dto.getDeclaration());
				vo.setEnclouserName(dto.getEnclouserName());
				list.add(vo);
			}
			propVO.setDeclartionDetails(list);
			return propVO;
		}
		logger.debug("Declartion details not available/it configured false in DB");
		propVO.setDeclartionDetails(Collections.emptyList());
		return propVO;
	}
	
	/**
	 * Saving Method declarations When Present address type is None in first step of
	 * vehicle registration
	 * 
	 * @param registrationDetailsDTO
	 * @param registrationDetailsVO
	 */

	public void saveDealerDeclartions(StagingRegistrationDetailsDTO registrationDetailsDTO,
			RegistrationDetailsVO registrationDetailsVO) {
		if (registrationDetailsVO.getApplicantDetails() != null
				&& registrationDetailsVO.getApplicantDetails().getPresentAddressFrom() != null
				&& registrationDetailsVO.getApplicantDetails().getPresentAddressFrom().equalsIgnoreCase("NONE")) {

			if (CollectionUtils.isEmpty(registrationDetailsVO.getDeclartionVo())) {
				logger.debug("Enclousers declartions not accepted: {}", registrationDetailsVO.getApplicationNo());
				throw new BadRequestException("Enclousers declartions not accepted");
			}
			DealerDeclarationsDTO dealerDto = new DealerDeclarationsDTO();
			dealerDto.setApplicationNo(registrationDetailsVO.getApplicationNo());
			List<DeclarationsDTO> declartionList = new ArrayList<>();
			registrationDetailsVO.getDeclartionVo().stream().forEach(declarationsVO-> {
				DeclarationsDTO decDto = new DeclarationsDTO();
				decDto.setDeclaration(declarationsVO.getDeclaration());
				decDto.setEnclouserName(declarationsVO.getEnclouserName());
				declartionList.add(decDto);
			});
			dealerDto.setDeclartions(declartionList);
			dealerDto.setCreatedDate(LocalDate.now());
			dealerDeclarationsDAO.save(dealerDto);
		}
	}

	@Override
	public List<EnclosuresVO> getListOfSupportedEnclosers(CitizenImagesInput input) {

		List<EnclosuresDTO> enclosureList;
		Integer serviceId = getEncServiceId(input);

		enclosureList = enclosuresDAO.findByServiceID(serviceId);

		enclosureList.sort((p1, p2) -> p1.getEnclosureId().compareTo(p2.getEnclosureId()));

		if (enclosureList.isEmpty()) {
			logger.error("Enclosueres not found ");
			throw new BadRequestException("No enclosures found based on given Service Id");
		}
		List<EnclosuresVO> inputVO = userEnclosureMapper.convertEntity(enclosureList);
		return inputVO;

	}

	private Integer getEncServiceId(CitizenImagesInput input) {
		if (input.getServiceIds().stream()
				.anyMatch(service -> service.equals(ServiceEnum.DEALERREGISTRATION.getId()))) {
			return ServiceEnum.DEALERREGISTRATION.getId();
		}
		return 0;
	}

	// *****************************************************************************************
	// TRADE CERTIFICATE service....
	// *****************************************************************************************

	/**
	 * this service is used for checking dealer eligibility for Trade CERT.
	 */
	@Override
	public String chkDelerElgibility(String user) throws Exception {
		Optional<UserDTO> userDto = userDAO.findByUserId(user);

		if (!userDto.isPresent() || !userDto.get().getIsAccountNonLocked() || !userDto.get().isStatus()
				|| !userDto.get().getUserStatus().toString().equals("ACTIVE"))
			throw new Exception("your are not eligible for apply trade certificate please contact to your RTO office");
		StringBuilder sb = new StringBuilder();
		if (userDto.isPresent()) {
			if (StringUtils.isNotBlank(userDto.get().getFirstName())) {
				sb.append(userDto.get().getFirstName()).append(StringUtils.SPACE);
			} else if (StringUtils.isNotBlank(userDto.get().getFirstname())) {
				sb.append(userDto.get().getFirstname()).append(StringUtils.SPACE);
			}
			if (StringUtils.isNotBlank(userDto.get().getMiddleName())) {
				sb.append(userDto.get().getMiddleName()).append(StringUtils.SPACE);
			}
			if (StringUtils.isNotBlank(userDto.get().getLastName())) {
				sb.append(userDto.get().getLastName());
			}
		}
		return sb.toString();

	}

	/**
	 * getting COV based on RID.
	 */
	@Override
	public List<MasterCovVO> getCov(String user) {
		Optional<UserDTO> userDto = userDAO.findByUserId(user);
		List<MasterCovDTO> masterCov = null;

		if (userDto.isPresent())
			masterCov = masterCovDAO.findByCovcodeInAndDealerCovTrue(
					dealerCovDao.findByRId(userDto.get().getRid()).stream().filter(distinctByKey(d -> d.getCovId()))
							.map(m -> m.getCovId()).collect(Collectors.toList()));
		return covMapper.convertDTOs(masterCov);
	}

	public static <T> Predicate<T> distinctByKey(Function<? super T, Object> keyExtractor) {
		Map<Object, Boolean> map = new ConcurrentHashMap<>();
		return t -> map.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
	}

	/**
	 * this service is used for getting fee Details for Trade Certificate
	 */
	public static double ammountCount = 0.0;
	public static Boolean chkCov = true;

	@Override
	public DealerTradeCertificateNewVO getFeesForTradeCert(DealerTradeCertificateNewVO tradeVo, Integer serviceId)
			throws Exception {
		List<FeesDTO> feeDto = feesDao.findByServiceId(serviceId);

		Optional<FeesDTO> serviceFee = feeDto.stream().filter(f -> f.getCovCode() != null && f.getFeesType() != null
				&& f.getCovCode().equals("TEST") && f.getFeesType().equals("Service Fee")).findFirst();

		if (!serviceFee.isPresent())
			throw new Exception("service fee not found please add service fee");
		ammountCount = 0.0;
		for (TradeCertificateDealerVO r : tradeVo.getTradeCertificate()) {
			chkCov = true;
			for (FeesDTO f : feeDto) {
				if (r.getCovCode() != null && r.getCovCode().equals(f.getCovcode())) {
					double ammount = (f.getAmount() * r.getNumberOfTradeCertificate() + serviceFee.get().getAmount());
					ammountCount = ammountCount + ammount;
					r.setAmmount(String.valueOf(ammount));
					r.setInitialAmmount(f.getAmount());
					r.setServiceFeeIntial(serviceFee.get().getAmount());
					// r.setServiceFeeTotal(serviceFee.get().getAmount() *
					// r.getNumberOfTradeCertificate());
					chkCov = false;
					break;
				}
			}
			if (chkCov) {
				throw new Exception("COV not found.. ");
			}
		}
		tradeVo.setTotalAmmount(String.valueOf(ammountCount));
		return tradeVo;
	}

	/**
	 * this service is used to apply new Trade Certificate on Dealer Side.
	 */
	@Override
	public DealerRegVO applyNewTradeCertificate(DealerTradeCertificateNewVO tradeVo, JwtUser user) {
		if (CollectionUtils.isNotEmpty(tradeVo.getTradeCertificate())) {
			tradeVo.getTradeCertificate().forEach(val -> {
				if (val.getNumberOfTradeCertificate() <= 0) {
					throw new BadRequestException("Number Of TradeCertificate Should Be One");
				}
			});
		}
		Optional<UserDTO> userDtoOpt = userDAO.findByUserId(user.getId());
		List<TradeCertificateDealerDto> saveDtoList = tradeCertMapper.convertVO(tradeVo, user, userDtoOpt);
		tradeVo.setTcGenId(saveDtoList.stream().findFirst().get().getCommonNumber());
		tradeVo.setPaymentTxId(saveDtoList.stream().findFirst().get().getPaymentTransactionNo());
		tradeCertDao.save(saveDtoList);
		return createPaymentDealerVo(tradeVo, user);
	}

	/**
	 * service for Repay
	 * 
	 * @throws Exception
	 */
	@Override
	public DealerRegVO repayForTc(DealerTradeCertificateNewVO vo, JwtUser user) throws Exception {
		List<TradeCertificateDealerDto> dto = tradeCertDao.findByCommonNumber(vo.getTcGenId());

		if (!CollectionUtils.isEmpty(dto) && (dto.stream().findFirst().get().getPaymentStatus()
				.equals(StatusRegistration.PAYMENTFAILED.getDescription())
				|| dto.stream().findFirst().get().getPaymentStatus()
						.equals(StatusRegistration.PAYMENTPENDING.getDescription()))) {

			vo.setTcGenId(dto.stream().findFirst().get().getCommonNumber());
			vo.setPaymentTxId(dto.stream().findFirst().get().getPaymentTransactionNo());

			vo.setTradeCertificate(tradeCertMapper.getListOfTradeCertificateDealerDto(dto));

		} else {
			throw new Exception("no data with this TC. ids");
		}
		return createPaymentDealerVo(vo, user);
	}

	private static int i = 0;

	public int getCountOfTrade(List<TradeCertificateDealerDto> dto) {

		for (TradeCertificateDealerDto r : dto) {
			i = i + r.getNumberOfTradeCertificate();
		}
		return i;

	}

	public ClassOfVehiclesVO convertClassOfVehiclesVO(TradeCertificateDealerVO vo) {
		ClassOfVehiclesVO cov = new ClassOfVehiclesVO();
		cov.setCode(vo.getCov());
		return cov;

	}

	public DealerRegVO createPaymentDealerVo(DealerTradeCertificateNewVO tradeVo, JwtUser user) {
		DealerRegVO delaer = new DealerRegVO();
		if (!CollectionUtils.isEmpty(tradeVo.getTradeCertificate())) {
			delaer.setTradeCertificate(convertVoMapperTradeCt(tradeVo.getTradeCertificate()));
		}

		delaer.setApplicationNo(tradeVo.getTcGenId());
		delaer.setServiceType(tradeVo.getServiceType());
		delaer.setGateWayType(tradeVo.getGateWayType());
		delaer.setApplicationNo(tradeVo.getTcGenId());
		ApplicantDetailsVO applicant = new ApplicantDetailsVO();
		ContactVO appContact = new ContactVO();
		Optional<UserDTO> userdtoOpt = userDAO.findByUserId(user.getId());
		if (userdtoOpt.isPresent()) {
			if (StringUtils.isNoneBlank(userdtoOpt.get().getMobile())) {
				appContact.setMobile(userdtoOpt.get().getMobile());
			}
			if (StringUtils.isNoneBlank(userdtoOpt.get().getEmail())) {
				appContact.setEmail(userdtoOpt.get().getEmail());
			}
		}
		applicant.setFirstName(user.getFirstname());
		applicant.setContact(appContact);

		delaer.setApplicantDetails(applicant);

		delaer.setPaymentTransactionNo(tradeVo.getPaymentTxId());
		delaer.setOfficeCode(user.getOfficeCode());
		List<ClassOfVehiclesVO> listOfCov = new ArrayList<>();

		/**
		 * comment for change in COV at need..
		 */

		if (tradeVo.getTradeCertificate() != null && !CollectionUtils.isEmpty(tradeVo.getTradeCertificate())) {
			for (TradeCertificateDealerVO cov : tradeVo.getTradeCertificate()) {
				listOfCov.add(convertClassOfVehiclesVO(cov));
				// i = i + cov.getNumberOfTradeCertificate();
			}
		}

		// delaer.setNumberOfTradeCertificateAppl(i);
		delaer.setCovs(listOfCov);
		return delaer;

	}

	/**
	 * updating status after payment done
	 * 
	 * @param dto
	 */
	@Override
	public void getUpdateAfterPaymentSuccess(List<TradeCertificateDealerDto> dtoList, String applicationNo,
			String status) {

		if (CollectionUtils.isNotEmpty(dtoList)) {
			dtoList.forEach(f -> {
				if (f.getTradeCertificateID().equals(applicationNo) || f.getCommonNumber().equals(applicationNo)) {
					updateDateForTradeCertificate(f, status);
				}
			});
			tradeCertDao.save(dtoList);
		}

	}

	/**
	 * updating status after payment done
	 * 
	 * @param dto
	 */
	public void updateDateForTradeCertificate(TradeCertificateDealerDto dto, String status) {
		if (StringUtils.isNoneEmpty(status) && status.equals(PayStatusEnum.SUCCESS.getDescription())) {

			if (dto.getApplicationPaymentType() != null
					&& dto.getApplicationPaymentType().equals(APP_PAYMENT_TYPE_DUPLICATE)) {
				dto.setDownloadStatus(Boolean.FALSE);
			} else {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				dto.setValidFrom(LocalDate.now());
				dto.setTradeValidityStatus(TRADE_VALIDITY_STATUS_VALID);
				dto.setValidTo(LocalDate.parse(
						LocalDate.now().plusDays(364).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")), formatter));
				dto.setPaymentStatus(StatusRegistration.PAYMENTSUCCESS.getDescription());
				dto.setDownloadStatus(Boolean.FALSE);
			}

		} else {
			dto.setPaymentStatus(StatusRegistration.PAYMENTFAILED.getDescription());
		}

	}

	/**
	 * service for getting pagination based on user name and its payment status.
	 * 
	 */
	@Override
	public Optional<ReportsVO> getTradeCertificateDetails(JwtUser user, Pageable pagable, String status) {

		Page<TradeCertificateDealerDto> dtoOpt = (status != null
				&& status.equals(StatusRegistration.PAYMENTSUCCESS.getDescription())
				&& checkValidtiy(user.getUsername(), pagable.getPageNumber()))
						? tradeCertDao.findByUserIdAndPaymentStatusOrderByCreatedDateDesc(user.getUsername(), status,
								pagable.previousOrFirst())
						: (status != null && !status.equals(StatusRegistration.PAYMENTSUCCESS.getDescription()))
								? tradeCertDao.findByUserIdAndPaymentStatusInOrderByCreatedDateDesc(user.getUsername(),
										getListOfPaymentStatusType(), pagable.previousOrFirst())
								: tradeCertDao.findByUserIdOrderByCreatedDateDesc(user.getUsername(),
										pagable.previousOrFirst());

		if (dtoOpt.hasContent()) {
			ReportsVO reportVO = new ReportsVO();
			reportVO.setTradeCertificateDealer(tradeCertMapper.convertListDtoToVo(dtoOpt.getContent()));
			reportVO.setPageNo(pagable.getPageNumber());
			reportVO.setTotalPage(dtoOpt.getTotalPages());
			reportVO.getTradeCertificateDealer().stream().forEach(val -> {
				if (StringUtils.isBlank(val.getCovCode()) && StringUtils.isNotBlank(val.getCov())) {
					val.setCovCode(masterCovDAO.findByCovdescription(val.getCov()).getCovcode());
				}
			});
			return Optional.of(reportVO);

		}
		return Optional.empty();
	}

	private List<String> getListOfPaymentStatusType() {
		List<String> paymentStatus = new ArrayList<>();
		paymentStatus.add(StatusRegistration.PAYMENTFAILED.getDescription());
		paymentStatus.add(StatusRegistration.PAYMENTPENDING.getDescription());
		return paymentStatus;
	}

	/**
	 * checking validity status
	 * 
	 * @param dto
	 */
	@Override
	public void validityChkForTrade(JwtUser user) {
		Optional<TradeCertificateDealerDto> dto = tradeCertDao.findByUserId(user.getUsername());
		if (dto.isPresent()) {
			if (dto.get().getTradeValidityStatus() != null
					&& dto.get().getTradeValidityStatus().equals(TRADE_VALIDITY_STATUS_VALID.toString())) {
				long noOfDaysBetween = ChronoUnit.DAYS.between(LocalDate.now(), dto.get().getValidTo());
				dto.get().setTotalDaysRemins(noOfDaysBetween + 1);
				if (noOfDaysBetween == 0) {
					dto.get().setTradeValidityStatus(TRADE_VALIDITY_STATUS_EXP);
				}
			}
			tradeCertDao.save(dto.get());
		}
	}

	@Override
	public Map<String, Object> generatePdf(String tcGenId, JwtUser user) throws Exception {
		TradeCertificateDealerDto dto = null;
		UserDTO userDTO = null;
		if (StringUtils.isEmpty(tcGenId)) {
			throw new Exception("application Number is Not Found");
		}
		Optional<TradeCertificateDealerDto> tradeCertificateDealerDto = tradeCertDao
				.findByTradeCertificateID(tcGenId);
		if (tradeCertificateDealerDto.isPresent()) {
			dto = tradeCertificateDealerDto.get();

			// IF DUPLCATE TC REQUIRED
			/*
			 * if (dto.isDownloadStatus() == Boolean.TRUE) { throw new
			 * Exception("you have already downloaded please go for duplicate"); }
			 */
		} else {
			throw new BadRequestException("Data not found::" + tcGenId);
		}
		Optional<UserDTO> ustDto = userDAO.findByUserId(dto.getUserId());
		if (ustDto.isPresent()) {
			userDTO = ustDto.get();
		}
		String address = StringUtils.EMPTY;
		if (userDTO != null && userDTO.getDoorNo() != null && userDTO.getStreetName() != null
				&& userDTO.getCity() != null && userDTO.getMandalName() != null && userDTO.getDistrictName() != null) {
			address = (userDTO.getDoorNo() == null ? StringUtils.EMPTY : userDTO.getDoorNo().toUpperCase()) + ", "
					+ (userDTO.getStreetName() == null ? StringUtils.EMPTY : userDTO.getStreetName().toUpperCase())
					+ ", " + (userDTO.getCity() == null ? StringUtils.EMPTY : userDTO.getCity().toUpperCase()) + "\n"
					+ (userDTO.getMandalName() == null ? StringUtils.EMPTY : userDTO.getMandalName().toUpperCase())
					+ ", "
					+ (userDTO.getDistrictName() == null ? StringUtils.EMPTY : userDTO.getDistrictName().toUpperCase());
		}
		Map<String, Object> parameters = new HashMap<>();

		parameters.put("APP_NO", dto.getTradeCertificateID());
		parameters.put("SERIAL_NO", StringUtils.EMPTY);
		parameters.put("NAME", dto.getDealerName());
		parameters.put("SERIAL_NO", dto.getTradeCertificateID());
		parameters.put("VEHICLE_TYPE", dto.getCov());
		parameters.put("EXP_DATE", dto.getValidTo().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
		parameters.put("FEE", dto.getAmmount());
		parameters.put("DATE", dto.getValidFrom().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
		parameters.put("STATE", "AP");
		parameters.put("APP_ADDRESS", address);
		Optional<OfficeDTO> officedtls = officeDAO.findByOfficeCode(user.getOfficeCode());
		if (officedtls.isPresent()) {
			parameters.put("OFFICE_NAME", (officedtls.get().getOfficeName() == null ? StringUtils.EMPTY
					: officedtls.get().getOfficeName().toUpperCase()));
		}
		parameters.put("CURR_DATE", LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")));
		parameters.put("DIGITAL_SIGN", getLogo(ReportFiles.DIGITAL_SIGN));
		parameters.put("QUANTITY", dto.getNumberOfTradeCertificate().toString());

		/**
		 * settnig status for download
		 */

		// IF DUPLCATE TC REQUIRED
		/*
		 * tradeCertificateDealerDto.get().setDownloadStatus(Boolean.TRUE);
		 * tradeCertDao.save(tradeCertificateDealerDto.get());
		 */
		return parameters;
	}

	public String getLogo(String value) {
		String apLogo = "";
		Resource resource = resourceLoader.getResource("classpath:" + "images\\" + value);
		String encodstring = null;
		try {
			encodstring = encodeFileToBase64Binary(resource.getFile());
			return apLogo = encodstring;
		} catch (IOException e) {
			e.printStackTrace();
			return apLogo;
		}
	}

	private static String encodeFileToBase64Binary(File file) {
		String encodedfile = null;
		FileInputStream fileInputStreamReader = null;
		try {
			fileInputStreamReader = new FileInputStream(file);
			byte bytes[] = new byte[(int) file.length()];
			fileInputStreamReader.read(bytes);
			encodedfile = Base64.encodeBase64URLSafeString(bytes);
			return encodedfile;
		} catch (FileNotFoundException e) {
			logger.debug("Exception [{}]", e);
			logger.error("Exception [{}]", e.getMessage());
			e.printStackTrace();
		} catch (IOException e) {
			logger.debug("Exception [{}]", e);
			logger.error("Exception [{}]", e.getMessage());
			e.printStackTrace();
		} finally {
			try {
				fileInputStreamReader.close();
			} catch (IOException e) {
				logger.debug("Exception [{}]", e);
				logger.error("Exception [{}]", e.getMessage());
				e.printStackTrace();
			}
		}
		return encodedfile;
	}

	@Override
	public DealerRegVO applyForRenewalForTradeCt(DealerTradeCertificateNewVO tradeVo, JwtUser user,
			String duplicateApply) {
		Optional<TradeCertificateDealerDto> saveDto = tradeCertDao.findByTradeCertificateID(tradeVo.getTcGenId());
		if (saveDto.isPresent()) {
			if (duplicateApply == null) {
				tradeCertMapper.convertTradeDtoToDto(saveDto, tradeVo, user);
			} else if (duplicateApply.equals(APP_PAYMENT_TYPE_DUPLICATE)) {
				tradeCertMapper.convertTradeDtoToDtoDuplicate(saveDto, tradeVo, user);
			}
			tradeVo.setTradeCertificate(
					tradeCertMapper.getListOfTradeCertificateDealerDto(Arrays.asList(saveDto.get())));
			tradeHistory(saveDto.get());
			tradeCertDao.save(saveDto.get());
		} else {
			throw new BadRequestException("No Record Found");
		}
		return createPaymentDealerVo(tradeVo, user);
	}

	/**
	 * getting Trade CT. id after payment
	 */
	@Override
	public List<String> getTcIdByTcCommon(JwtUser user, String commonId) {
		/**
		 * comman number AP/AP007/TC/098/COMMON
		 */
		List<String> id = new ArrayList<>();
		List<TradeCertificateDealerDto> saveDtoList = tradeCertDao.findByCommonNumber(commonId);

		if (CollectionUtils.isNotEmpty(saveDtoList)) {
			saveDtoList.forEach(f -> {
				if (f.getCommonNumber().equals(commonId.trim())) {
					id.add(f.getTradeCertificateID());
				}
			});
		}
		return id;
	}

	@Override
	public String chkCov(JwtUser user, String cov) {
		Integer tradeCertCount = 0;
		List<TradeCertificateDealerDto> filterList = Collections.emptyList();
		List<TradeCertificateDealerDto> dtoList = tradeCertDao.findByUserIdAndCovAndPaymentStatusIn(user.getUsername(),
				cov, Arrays.asList(StatusRegistration.PAYMENTDONE.getDescription(),
						StatusRegistration.PAYMENTSUCCESS.getDescription()));
		if (CollectionUtils.isNotEmpty(dtoList)) {
			filterList = dtoList.stream().filter(val -> val.getCov().equalsIgnoreCase(cov)
					&& (val.getPaymentStatus().equals(StatusRegistration.PAYMENTDONE.getDescription())
							|| (val.getPaymentStatus().equals(StatusRegistration.PAYMENTSUCCESS.getDescription())
									&& val.getValidTo() != null && val.getValidTo().isAfter(LocalDate.now()))))
					.collect(Collectors.toList());
		}
		if (CollectionUtils.isNotEmpty(filterList)) {
			tradeCertCount = filterList.stream().map(TradeCertificateDealerDto::getNumberOfTradeCertificate)
					.mapToInt(Integer::intValue).sum();
			return tradeCertCount.toString();
		}
		return tradeCertCount.toString();
	}

	private void tradeHistory(TradeCertificateDealerDto tradeDto) {
		DealerTradeCertificateHistoryDTO tradeHistory = new DealerTradeCertificateHistoryDTO();
		tradeHistory.setTradeCertificateDealer(tradeDto);
		tradeHistory.setCreatedDate(LocalDateTime.now());
		tradeHistory.setlUpdate(LocalDateTime.now());
		dealerTCHistoryDAO.save(tradeHistory);
	}

	private boolean checkValidtiy(String userName, int pageNumber) {
		if (pageNumber == 1) {
			List<TradeCertificateDealerDto> dtolist = tradeCertDao.findByUserIdAndTradeValidityStatus(userName,
					TRADE_VALIDITY_STATUS_VALID.toString());
			if (CollectionUtils.isNotEmpty(dtolist)) {
				dtolist.forEach(r -> {
					if (r.getTradeValidityStatus() != null
							&& r.getTradeValidityStatus().equals(TRADE_VALIDITY_STATUS_VALID.toString())) {
						long noOfDaysBetween = ChronoUnit.DAYS.between(LocalDate.now(), r.getValidTo());
						r.setTotalDaysRemins(noOfDaysBetween + 1);
						if (noOfDaysBetween <= 0) {
							r.setTradeValidityStatus(TRADE_VALIDITY_STATUS_EXP);
						}
					}
				});
				tradeCertDao.save(dtolist);
			}
		}
		return Boolean.TRUE;
	}

	/**
	 * service for getting TradeCertificate based on ID.
	 */
	@Override
	public Optional<TradeCertificateDealerVO> getTcDetails(String tcId, JwtUser user) {
		Optional<TradeCertificateDealerDto> dto = tradeCertDao.findByUserIdAndTradeCertificateID(user.getUsername(),
				tcId);
		return (dto.isPresent()) ? Optional.of(tradeCertMapper.convertDtoToVoLimit(dto.get())) : Optional.empty();

	}

	/**
	 * this method is giving map Vo itself
	 * 
	 * @param vo
	 * @return
	 */
	public List<TradeCertificateDealerVO> convertVoMapperTradeCt(List<TradeCertificateDealerVO> vo) {
		List<TradeCertificateDealerVO> listOfTradeCert = new ArrayList<>();
		vo.forEach(r -> {
			TradeCertificateDealerVO v = new TradeCertificateDealerVO();
			BeanUtils.copyProperties(r, v);
			listOfTradeCert.add(v);
		});
		return listOfTradeCert;
	}
}
