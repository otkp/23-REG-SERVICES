package org.epragati.reports.service.impl;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.auditService.AuditServiceImpl;
import org.epragati.cfst.dao.CfstNonPaymentSkipRecordsDAO;
import org.epragati.cfst.dto.CfstNonPaymentSkipRecordsDTO;
import org.epragati.constants.CommonConstants;
import org.epragati.constants.CovCategory;
import org.epragati.constants.NationalityEnum;
import org.epragati.constants.OfficeType;
import org.epragati.constants.OwnerTypeEnum;
import org.epragati.constants.Schedulers;
import org.epragati.exception.BadRequestException;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dao.DistrictDAO;
import org.epragati.master.dao.FcDetailsDAO;
import org.epragati.master.dao.MandalDAO;
import org.epragati.master.dao.MasterCovDAO;
import org.epragati.master.dao.MasterPayperiodDAO;
import org.epragati.master.dao.NonPaymentDetailsDAO;
import org.epragati.master.dao.NonPaymentHistroyDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.RegServiceDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.ShowCauseDetailsDAO;
import org.epragati.master.dao.ShowCauseSectionDAO;
import org.epragati.master.dao.TaxDetailsDAO;
import org.epragati.master.dao.WeightDAO;
import org.epragati.master.dto.ApplicantAddressDTO;
import org.epragati.master.dto.ApplicantDetailsDTO;
import org.epragati.master.dto.DistrictDTO;
import org.epragati.master.dto.FcDetailsDTO;
import org.epragati.master.dto.MandalDTO;
import org.epragati.master.dto.MasterCovDTO;
import org.epragati.master.dto.MasterPayperiodDTO;
import org.epragati.master.dto.NonPaymentDetailsDTO;
import org.epragati.master.dto.NonPaymentDetailsHistoryDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.ShowCauseDetailsDTO;
import org.epragati.master.dto.ShowCauseSectionDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.master.dto.TaxHelper;
import org.epragati.master.dto.WeightDTO;
import org.epragati.master.mappers.NonPaymentDetailsMapper;
import org.epragati.master.mappers.ShowCauseDetailsMapper;
import org.epragati.payment.report.vo.NonPaymentDetailsVO;
import org.epragati.payment.report.vo.RCCancellationVO;
import org.epragati.payment.report.vo.RegReportVO;
import org.epragati.payment.report.vo.ReportsVO;
import org.epragati.payment.report.vo.ShowCauseDetailsVO;
import org.epragati.payments.vo.FeeDetailsVO;
import org.epragati.payments.vo.FeesVO;
import org.epragati.permits.dao.PermitDetailsDAO;
import org.epragati.permits.dto.PermitDetailsDTO;
import org.epragati.registration.service.RegistrationMigrationSolutionsService;
import org.epragati.regservice.CitizenTaxService;
import org.epragati.regservice.dto.CitizenFeeDetailsInput;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.vo.ApplicationSearchVO;
import org.epragati.regservice.vo.FeeDetailsResponceVO;
import org.epragati.reports.service.PaymentReportService;
import org.epragati.reports.service.RCCancellationService;
import org.epragati.rta.reports.vo.ReportVO;
import org.epragati.sequence.SequenceGenerator;
import org.epragati.tax.vo.TaxStatusEnum;
import org.epragati.tax.vo.TaxTypeEnum;
import org.epragati.util.PermitsEnum;
import org.epragati.util.PermitsEnum.PermitType;
import org.epragati.util.RoleEnum;
import org.epragati.util.Status;
import org.epragati.util.StatusRegistration;
import org.epragati.util.document.Sequence;
import org.epragati.util.payment.ClassOfVehicleEnum;
import org.epragati.util.payment.ServiceCodeEnum;
import org.epragati.util.payment.ServiceEnum;
import org.epragati.vcr.service.VcrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class RCCancellationServiceImpl implements RCCancellationService {

	private static final Logger logger = LoggerFactory.getLogger(RCCancellationServiceImpl.class);

	@Autowired
	private OfficeDAO officeDAO;

	@Autowired
	private MandalDAO mandalDAO;

	@Autowired
	private TaxDetailsDAO taxDetailsDAO;

	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;

	@Autowired
	private ShowCauseDetailsMapper showCauseDetailsMapper;

	@Autowired
	private RegServiceDAO regServiceDAO;

	@Autowired
	private PermitDetailsDAO permitDetailsDAO;

	@Autowired
	private FcDetailsDAO fcDetailsDAO;

	@Autowired
	private MasterPayperiodDAO masterPayperiodDAO;

	@Autowired
	private MasterCovDAO masterCovDAO;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private VcrService vcrService;

	@Autowired
	private RegistrationMigrationSolutionsService registrationMigrationSolutionsService;

	@Autowired
	private ShowCauseSectionDAO showCauseSectionDAO;

	@Autowired
	private ShowCauseDetailsDAO showCauseDetailsDAO;

	@Autowired
	private SequenceGenerator sequenceGenerator;

	@Autowired
	private NonPaymentDetailsDAO nonPaymentDetailsDAO;

	@Autowired
	private NonPaymentDetailsMapper nonPaymentsDetailsMapper;

	@Autowired
	private RestTemplate restTemplate;

	@Value("${citizen.tax.payment.braekup.url:}")
	private String paymentBraekupUrl;

	@Autowired
	private AuditServiceImpl auditServiceImpl;

	@Autowired
	private CitizenTaxService citizenTaxService;

	@Autowired
	private PaymentReportService paymentReportService;

	@Autowired
	private NonPaymentHistroyDAO nonPaymentHistroyDAO;

	@Autowired
	private DistrictDAO districtDAO;

	@Autowired
	private WeightDAO weightDAO;
	
	@Autowired
	private CfstNonPaymentSkipRecordsDAO cfstNonPaymentSkipRecordsDAO;
	
	static Map<Integer, String> districNameWithCodeMap = new HashMap<>();

	static Map<String, String> covNameWithCodeMap = new HashMap<>();

	static Map<String, Integer> officeCodeNameWithdistricIdMap = new HashMap<>();
	
	@Value("${count:100}")
	private Integer count;

	@PostConstruct
	@Override
	public Map<Integer, String> getDistricIdByDistricName() {
		List<DistrictDTO> districlist = districtDAO.findByStateIdAndStatus(NationalityEnum.AP.getName(), "Y");
		districlist.forEach(val -> {
			districNameWithCodeMap.put(val.getDistrictId(), val.getDistrictName());
		});

		return districNameWithCodeMap;
	}

	@PostConstruct
	@Override
	public Map<String, String> getCovCodeByCovName() {
		List<MasterCovDTO> covlist = masterCovDAO.findAll();
		covlist.forEach(val -> {
			covNameWithCodeMap.put(val.getCovcode(), val.getCovdescription());
		});

		return covNameWithCodeMap;
	}

	@PostConstruct
	@Override
	public Map<String, Integer> getOfficeCodeByDistricId() {
		List<OfficeDTO> officeList = officeDAO.findByTypeInAndIsActive(
				Arrays.asList(OfficeType.RTA.getCode(), OfficeType.UNI.getCode()), Boolean.TRUE);
		officeList.forEach(val -> {
			officeCodeNameWithdistricIdMap.put(val.getOfficeCode(), val.getDistrict());
		});
		return officeCodeNameWithdistricIdMap;
	}

	@Override
	public List<String> getMandalDetails(JwtUser jwtUser) {

		List<String> mandalsList = new ArrayList<>();
		Optional<OfficeDTO> officeOpt = officeDAO.findByOfficeCode(jwtUser.getOfficeCode());
		List<MandalDTO> mandalList = mandalDAO.findByDistrictId(officeOpt.get().getDistrict());
		mandalList.sort((m1, m2) -> m1.getMandalName().compareTo(m2.getMandalName()));
		mandalList.stream().forEach(mandal -> {
			if (mandal.getMandalName() != null) {
				mandalsList.add(mandal.getMandalName());
			}
		});
		return mandalsList;
	}

	@Override
	public List<String> getClassOfVehicles() {
		return getMasterPayPeriod().stream().map(cov -> cov.getCovcode()).collect(Collectors.toList());
	}

	private MasterCovDTO getMasterCov(String covcode) {
		return masterCovDAO.findByCovcode(covcode);
	}

	private String getMasterCovDesc(String covcode) {
		String cov = null;
		MasterCovDTO masterCov = masterCovDAO.findByCovcode(covcode);
		if (null != masterCov) {
			return masterCov.getCovdescription();
		}
		return cov;
	}

	private List<MasterPayperiodDTO> getMasterPayPeriod() {
		List<MasterPayperiodDTO> masterPayperiodList = masterPayperiodDAO
				.findByPayperiodIn(Arrays.asList(TaxTypeEnum.QuarterlyTax.getCode(), TaxTypeEnum.BOTH.getCode()));
		masterPayperiodList.removeIf(val -> val.getCovcode().equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())||
				val.getCovcode().equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())||
				val.getCovcode().equalsIgnoreCase(ClassOfVehicleEnum.TRTT.getCovCode())||
				val.getCovcode().equalsIgnoreCase(ClassOfVehicleEnum.TRCN.getCovCode())||
				val.getCovcode().equalsIgnoreCase(ClassOfVehicleEnum.OBPN.getCovCode()));
		 return masterPayperiodList;
	}
	
	
	private List<String> fetchOfficeCodes(JwtUser jwtUser) {
		List<OfficeDTO> officeDTOList = null;
		Optional<OfficeDTO> officeOptional = officeDAO.findByOfficeCodeAndIsActiveTrue(jwtUser.getOfficeCode());
		if (officeOptional.isPresent()) {
			officeDTOList = officeDAO.findBydistrict(officeOptional.get().getDistrict());
		}
		return officeDTOList.stream().map(office -> office.getOfficeCode()).collect(Collectors.toList());
	}

	@Override
	public Optional<ReportsVO> getNonPaymentsReport(RegReportVO regReportVO, JwtUser jwtUser, Pageable pagable) {
		LocalDate taxPeriodEnd = regReportVO.getQuarterEndDate().minusMonths(regReportVO.getPendingQuarter() * 3);
		Page<TaxDetailsDTO> taxDetailsOpt = null;
		if (taxDetailsOpt.hasContent()) {
			Integer page = 0;
			List<TaxDetailsDTO> taxDetailsList = taxDetailsOpt.getContent();
			Pair<Integer, List<RegReportVO>> nonPaymentTaxReportPair = getNonPaymentsTaxReport(taxDetailsList,
					regReportVO, jwtUser, page);
			if (!nonPaymentTaxReportPair.getSecond().isEmpty()) {
				ReportsVO reportVO = new ReportsVO();
				reportVO.setPaymentReport(nonPaymentTaxReportPair.getSecond());
				reportVO.setPageNo(taxDetailsOpt.getNumber());
				reportVO.setTotalPage(nonPaymentTaxReportPair.getFirst());
				return Optional.of(reportVO);
			} else {
				return Optional.empty();
			}
		}
		return Optional.empty();
	}

	private Pair<Integer, List<RegReportVO>> getNonPaymentsTaxReport(List<TaxDetailsDTO> taxDetailsList,
			RegReportVO regReportVO, JwtUser jwtUser, Integer page) {
		if (!taxDetailsList.isEmpty()) {
			List<RCCancellationVO> nonPaymentVOList = new ArrayList<>();
			for (TaxDetailsDTO taxDetail : taxDetailsList) {
				List<RegistrationDetailsDTO> registrationDetailsList = registrationDetailDAO
						.findByPrNoAndNocDetailsIsNull(taxDetail.getPrNo());
				if (!registrationDetailsList.isEmpty()) {
					registrationDetailsList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
					RegistrationDetailsDTO regDetail = registrationDetailsList.stream().findFirst().get();

					page = getNonPaymenntsDataBasedOnMandal(regReportVO, jwtUser, page, nonPaymentVOList, taxDetail,
							regDetail);
				}
			}
			RegReportVO regReport = new RegReportVO();
			regReport.setNonPaymentReport(nonPaymentVOList);
			return Pair.of(page, Arrays.asList(regReport));

		}
		return Pair.of(page, Collections.emptyList());
	}

	private Integer getNonPaymenntsDataBasedOnMandal(RegReportVO regReportVO, JwtUser jwtUser, Integer page,
			List<RCCancellationVO> nonPaymentVOList, TaxDetailsDTO taxDetail, RegistrationDetailsDTO regDetail) {
		if (null != regDetail.getApplicantDetails().getPresentAddress().getMandal()
				&& null != regDetail.getApplicantDetails().getPresentAddress().getMandal().getMandalName()) {
			RCCancellationVO nonPaymentVO = new RCCancellationVO();
			nonPaymentVO.setTaxValidity(taxDetail.getTaxPeriodEnd());
			nonPaymentVO.setMandalName(regReportVO.getMandalName());
			if (null != regDetail.getRegistrationValidity().getFcValidity()) {
				nonPaymentVO.setFcValidity(regDetail.getRegistrationValidity().getFcValidity());
			}
			if (regReportVO.getMandalName()
					.equals(regDetail.getApplicantDetails().getPresentAddress().getMandal().getMandalName())) {
				page++;
				getRegistrationDetailsData(regDetail, nonPaymentVO);
				getPermitDetailsData(taxDetail, nonPaymentVO);
				nonPaymentVOList.add(nonPaymentVO);
			} else if (regReportVO.getMandalName().equals("ALL")) {
				if (getMandalDetails(jwtUser)
						.contains(regDetail.getApplicantDetails().getPresentAddress().getMandal().getMandalName())) {
					page++;
					getRegistrationDetailsData(regDetail, nonPaymentVO);
					getPermitDetailsData(taxDetail, nonPaymentVO);
					nonPaymentVOList.add(nonPaymentVO);
				}
			}
		}
		return page;
	}

	private void getPermitDetailsData(TaxDetailsDTO taxDetail, RCCancellationVO nonPaymentVO) {
		Optional<PermitDetailsDTO> permitDetailsOpt = permitDetailsDAO
				.findByPrNoAndPermitStatusAndPermitTypeTypeofPermitOrderByCreatedDateDesc(taxDetail.getPrNo(),
						PermitsEnum.ACTIVE.getDescription(), PermitType.PRIMARY.getPermitTypeCode());
		if (permitDetailsOpt.isPresent()) {
			PermitDetailsDTO permitDetails = permitDetailsOpt.get();
			if (null != permitDetails.getPermitValidityDetails()) {
				nonPaymentVO.setPermitValidity(permitDetails.getPermitValidityDetails().getPermitValidTo());
			}
			permitDetailsOpt =null;
		}
	}

	private void getRegistrationDetailsData(RegistrationDetailsDTO regiDetails, RCCancellationVO reportVO) {

		if (StringUtils.isNoneBlank(regiDetails.getPrNo())) {
			reportVO.setPrNo(regiDetails.getPrNo());
		}
		if (StringUtils.isNoneBlank(regiDetails.getClassOfVehicleDesc())) {
			reportVO.setCov(regiDetails.getClassOfVehicleDesc());
		}
		if (null != regiDetails.getApplicantDetails()) {
			ApplicantDetailsDTO applicantDTO = regiDetails.getApplicantDetails();
			if (StringUtils.isNoneBlank(applicantDTO.getDisplayName())
					&& StringUtils.isNoneBlank(applicantDTO.getFatherName())) {
				reportVO.setOwnerName(regiDetails.getApplicantDetails().getDisplayName() + ","
						+ regiDetails.getApplicantDetails().getFatherName());
			}
			if (null != regiDetails.getApplicantDetails().getPresentAddress()) {
				ApplicantAddressDTO applicantAddress = regiDetails.getApplicantDetails().getPresentAddress();
				StringBuilder address = new StringBuilder();
				if (StringUtils.isNoneBlank(applicantAddress.getDoorNo())) {
					address.append(applicantAddress.getDoorNo()).append(", ");
				}
				if (StringUtils.isNoneBlank(applicantAddress.getStreetName())) {
					address.append(applicantAddress.getStreetName()).append(", ");
				}
				if (null != applicantAddress.getMandal()) {
					if (StringUtils.isNoneBlank(applicantAddress.getMandal().getMandalName())) {
						address.append(applicantAddress.getMandal().getMandalName()).append(", ");
					}
				}
				if (StringUtils.isNoneBlank(applicantAddress.getDistrict().getDistrictName())) {
					address.append(applicantAddress.getDistrict().getDistrictName()).append(", ");
				}
				if (StringUtils.isNoneBlank(applicantAddress.getState().getStateName())) {
					address.append(applicantAddress.getState().getStateName()).append(" - ");
				}
				if (null != applicantAddress.getPostOffice()
						&& null != applicantAddress.getPostOffice().getPostOfficeCode()) {
					address.append(applicantAddress.getPostOffice().getPostOfficeCode());
				}
				reportVO.setOwnerAddress(address.toString());
			}
		}
		if (null != regiDetails.getFinanceDetails()) {
			if (StringUtils.isNoneBlank(regiDetails.getFinanceDetails().getFinancerName())) {
				reportVO.setFinancerName(regiDetails.getFinanceDetails().getFinancerName());
				reportVO.setFinancerAddress(regiDetails.getFinanceDetails().getAddress());
			}
		}

		if (null != regiDetails.getRegistrationValidity()) {
			if (null != regiDetails.getRegistrationValidity().getTaxValidity()) {
				reportVO.setTaxValidity(regiDetails.getRegistrationValidity().getTaxValidity());
			}
			if (null != regiDetails.getRegistrationValidity().getFcValidity()) {
				reportVO.setFcValidity(regiDetails.getRegistrationValidity().getFcValidity());
			}
		}

	}

	@Override
	public Optional<ReportsVO> classOfVehicleNonPaymentReportCount(RegReportVO reportVO, JwtUser jwtUser) {
		RegReportVO regReportVO = new RegReportVO();
		List<String> officeCodes = fetchOfficeCodes(jwtUser);
		logger.info("ClassOfVehicleNonPaymentReport count quarterEndDate: [{}] and pendingQuarter :[{}] ",
				regReportVO.getQuarterEndDate(), regReportVO.getPendingQuarter());
		List<ReportVO> reportVOList = getReportCountByCov(reportVO, officeCodes);
		reportVOList.forEach(report -> {
			report.setCovDesc(getMasterCovDesc(report.getCov()));
		});
		regReportVO.setCovReport(reportVOList);
		ReportsVO reportsVO = new ReportsVO();
		reportsVO.setPaymentReport(Arrays.asList(regReportVO));
		return Optional.of(reportsVO);
	}

	private List<ReportVO> getReportCountByCov(RegReportVO reportVO, List<String> officeCodes) {
		LocalDate quarterEndDate = reportVO.getQuarterEndDate().minusMonths(reportVO.getPendingQuarter() * 3);
		LocalDateTime fromDate = vcrService.getTimewithDate(quarterEndDate, false);
		Aggregation agg = newAggregation(
				match(Criteria.where("taxPeriodEnd").lt(fromDate).and("paymentPeriod")
						.is(TaxTypeEnum.QuarterlyTax.getDesc()).and("taxStatus").is(TaxStatusEnum.ACTIVE.getCode())
						.and("classOfVehicle").in(getClassOfVehicles()).and("officeCode").in(officeCodes)),
				group("classOfVehicle").count().as("covCount"), project("covCount").and("cov").previousOperation());
		AggregationResults<ReportVO> groupResults = mongoTemplate.aggregate(agg, TaxDetailsDTO.class, ReportVO.class);
		return groupResults.getMappedResults();
	}

	@Override
	public Optional<ReportsVO> classOfVehicleNonPaymentReport(RegReportVO regReportVO, JwtUser jwtUser,
			Pageable pagable) {
		LocalDate taxPeriodEnd = regReportVO.getQuarterEndDate().minusMonths(regReportVO.getPendingQuarter() * 3);
		Page<TaxDetailsDTO> taxDetailsOpt = null;
		if (StringUtils.isNoneBlank(regReportVO.getCov())) {
			taxDetailsOpt = taxDetailsDAO.findByPaymentPeriodAndTaxPeriodEndLessThanEqualAndTaxStatusAndClassOfVehicle(
					TaxTypeEnum.QuarterlyTax.getDesc(), taxPeriodEnd, TaxStatusEnum.ACTIVE.getCode(),
					regReportVO.getCov(), pagable.previousOrFirst());
		}
		ReportsVO reportVO = new ReportsVO();
		RegReportVO regReport = new RegReportVO();
		reportVO.setPageNo(taxDetailsOpt.getNumber());
		reportVO.setTotalPage(taxDetailsOpt.getTotalPages());
		if (taxDetailsOpt.hasContent()) {
			List<TaxDetailsDTO> taxDetailsList = taxDetailsOpt.getContent();
			Set<String> prNos = fetchPrNos(taxDetailsList);
			List<RegistrationDetailsDTO> registrationDetailsList = registrationDetailDAO
					.findByPrNoInAndNocDetailsIsNull(prNos.stream().collect(Collectors.toList()));
			List<RegistrationDetailsDTO> registrationDetails = registrationMigrationSolutionsService
					.removeInactiveRecordsToList(registrationDetailsList);
			registrationDetails = registrationDetails.stream().filter(dto -> dto.getIsScNoGenerated() == false)
					.collect(Collectors.toList());
			List<RCCancellationVO> cancellationList = new ArrayList<>();
			registrationDetails.stream().forEach(regDetails -> {
				RCCancellationVO rcCancellationVO = new RCCancellationVO();
				cancellationList.add(this.generateRecord(regDetails, rcCancellationVO));
			});
			regReport.setNonPaymentReport(cancellationList);
		}
		reportVO.setPaymentReport(Arrays.asList(regReport));
		return Optional.of(reportVO);
	}

	private RCCancellationVO generateRecord(RegistrationDetailsDTO regDetails, RCCancellationVO rcCancellationVO) {
		this.getRegistrationDetailsData(regDetails, rcCancellationVO);
		rcCancellationVO.setPermitValidity(getPermitValidity(regDetails));
		return rcCancellationVO;
	}

	private Set<String> fetchPrNos(List<TaxDetailsDTO> taxDetailsList) {
		if (CollectionUtils.isNotEmpty(taxDetailsList)) {
			List<TaxDetailsDTO> flisterList = taxDetailsList.stream().filter(val -> val.getPrNo() != null)
					.collect(Collectors.toList());
			return flisterList.stream().map(tax -> tax.getPrNo()).collect(Collectors.toSet());
		}
		return Collections.emptySet();
	}

	private LocalDate getPermitValidity(RegistrationDetailsDTO regDetails) {
		LocalDate permitValidity = null;
		Optional<PermitDetailsDTO> permitDetailsOpt = permitDetailsDAO
				.findByPrNoAndPermitStatusAndPermitTypeTypeofPermitOrderByCreatedDateDesc(regDetails.getPrNo(),
						PermitsEnum.ACTIVE.getDescription(), PermitType.PRIMARY.getPermitTypeCode());
		if (permitDetailsOpt.isPresent() && null != permitDetailsOpt.get().getPermitValidityDetails()) {
			permitValidity = permitDetailsOpt.get().getPermitValidityDetails().getPermitValidTo();
		}
		return permitValidity;
	}

	@Override
	public List<String> getSectionDetails() {

		List<String> showCauseSections = new ArrayList<>();
		List<ShowCauseSectionDTO> showCauseSectionList = showCauseSectionDAO.findByStatusTrue();

		showCauseSectionList.stream().forEach(section -> {
			if (section.getSectionCode() != null) {
				showCauseSections.add(section.getSectionCode());
			}
		});

		return showCauseSections;
	}

	@Override
	public void generateShowCauseNo(ApplicationSearchVO applicationSearchVO, String officeCode, String userId) {

		List<ShowCauseDetailsDTO> showCauseDetailsList = new ArrayList<>();
		List<RegServiceDTO> regServiceList = new ArrayList<>();
		List<NonPaymentDetailsDTO> nonPaymentList = new ArrayList<>();
		Optional<ShowCauseSectionDTO> showCauseSectionDto = showCauseSectionDAO
				.findBySectionCode(applicationSearchVO.getShowCauseSectionVo().getSectionCode());
		if (!showCauseSectionDto.isPresent()) {
			throw new BadRequestException("Section is Missing.Please select a valid section");
		}
		if (CollectionUtils.isNotEmpty(applicationSearchVO.getRcCancellationVOs())) {
			applicationSearchVO.getRcCancellationVOs().stream().forEach(regDto -> {
				Optional<NonPaymentDetailsDTO> nonPaymentdto = nonPaymentDetailsDAO.findByPrNo(regDto.getPrNo());
				Optional<RegistrationDetailsDTO> registrationdto = registrationDetailDAO.findByPrNo(regDto.getPrNo());
				Optional<ShowCauseDetailsDTO> showCausedto = showCauseDetailsDAO.findByPrNo(regDto.getPrNo());
				RegistrationDetailsDTO registrationDto = null;
				if (registrationdto.isPresent()) {
					registrationDto = registrationdto.get();
				}
				if (nonPaymentdto.isPresent()) {
					NonPaymentDetailsDTO nonPaymentDto = nonPaymentdto.get();
					if (!showCausedto.isPresent()) {

						ShowCauseDetailsDTO showCauseDetailsdto = new ShowCauseDetailsDTO();
						RegServiceDTO regServiceDTO = new RegServiceDTO();

						if (StringUtils.isEmpty(showCauseDetailsdto.getScNo())) {
							Map<String, String> officeCodeMap = new TreeMap<>();
							officeCodeMap.put("officeCode", officeCode);
							showCauseDetailsdto.setScNo(sequenceGenerator
									.getSequence(String.valueOf(Sequence.SHOWCAUSENO.getSequenceId()), officeCodeMap));
						}
						showCauseDetailsdto.setPrNo(nonPaymentDto.getPrNo());
						showCauseDetailsdto.setScIssuedDate(LocalDateTime.now());
						showCauseDetailsdto.setOfficeCode(officeCode);
						showCauseDetailsdto.setScIssuedBy(RoleEnum.CCO.getName());
						showCauseDetailsdto.setScIssuedByRole(RoleEnum.CCO.getName());
						showCauseDetailsdto.setScIssuedByName(userId);
						showCauseDetailsdto.setScStatus(Status.ShowCauseStatus.CCOISSUED.getStatus());
						showCauseDetailsdto.setCov(nonPaymentDto.getCov());
						showCauseDetailsdto.setOwnerName(nonPaymentDto.getOwnerName());
						showCauseDetailsdto.setTaxValidity(nonPaymentDto.getTaxValidity());
						showCauseDetailsdto.setOwnerAddress(nonPaymentDto.getOwnerAddress());
						showCauseDetailsdto.setMobileNo(nonPaymentDto.getMobileNo());
						showCauseDetailsdto.setFatherName(nonPaymentDto.getFatherName());
						showCauseDetailsdto
								.setQuarterEndDate(applicationSearchVO.getShowCauseDetailsVO().getQuarterEndDate());
						showCauseDetailsdto
								.setPendingQuarters(applicationSearchVO.getShowCauseDetailsVO().getPendingQuarters());
						if (showCauseSectionDto.get() != null) {
							showCauseDetailsdto.setScType(showCauseSectionDto.get());
						}
						if (StringUtils.isNotEmpty(regDto.getFinancerName())) {
							showCauseDetailsdto.setFinancerName(nonPaymentDto.getFinancerName());
						}
						if (!(null == regDto.getFcValidity())) {
							showCauseDetailsdto.setFcValidity(nonPaymentDto.getFcValidity());
						}
						if(nonPaymentDto.getWeightType()!=null) {
							showCauseDetailsdto.setWeightType(nonPaymentDto.getWeightType());
						}
						if(nonPaymentDto.getGvw()!=null) {
							showCauseDetailsdto.setGvw(nonPaymentDto.getGvw());
						}
						regServiceDTO.setApplicationNo(showCauseDetailsdto.getScNo());
						regServiceDTO.setPrNo(nonPaymentDto.getPrNo());
						regServiceDTO.setAadhaarNo(registrationDto.getApplicantDetails().getAadharNo());
						regServiceDTO.setApplicationStatus(StatusRegistration.CCOISSUED);
						regServiceDTO.setRegistrationDetails(registrationDto);
						regServiceDTO.setOfficeDetails(registrationDto.getOfficeDetails());
						regServiceDTO.setServiceType(Arrays.asList(ServiceEnum.SHOWCAUSENO));
						regServiceDTO
								.setServiceIds(Stream.of(ServiceEnum.SHOWCAUSENO.getId()).collect(Collectors.toSet()));
						regServiceDTO.setShowCauseDetails(showCauseDetailsdto);
						regServiceDTO.setCreatedDate(LocalDateTime.now());
						regServiceDTO.setlUpdate(LocalDateTime.now());
						regServiceDTO.setUpdatedBy(userId);

						if (nonPaymentDto.getIsScNoGenerated() == false) {
							nonPaymentDto.setScNo(showCauseDetailsdto.getScNo());
							nonPaymentDto.setApplicationStatus(StatusRegistration.CCOISSUED);
							nonPaymentDto.setIsScNoIssuedByCco(true);
							nonPaymentDto.setSectionCode(showCauseSectionDto.get().getSectionCode());
							nonPaymentDto.setScNoIssuedOn(LocalDate.now());
							nonPaymentDto
									.setQuarterEndDate(applicationSearchVO.getShowCauseDetailsVO().getQuarterEndDate());
							nonPaymentDto.setPendingQuarters(
									applicationSearchVO.getShowCauseDetailsVO().getPendingQuarters());
							nonPaymentDto.setlUpdate(LocalDateTime.now());
							nonPaymentList.add(nonPaymentDto);
						}
						showCauseDetailsdto.setlUpdate(LocalDateTime.now());
						showCauseDetailsdto.setCreatedDate(LocalDateTime.now());
						showCauseDetailsList.add(showCauseDetailsdto);
						regServiceList.add(regServiceDTO);
					} else {
						logger.error("SC No alredy generated");
						throw new BadRequestException("SC No alredy generated");
					}
				}

			});
			showCauseDetailsDAO.save(showCauseDetailsList);
			regServiceDAO.save(regServiceList);
			nonPaymentDetailsDAO.save(nonPaymentList);
			showCauseDetailsList.clear();
			regServiceList.clear();
			nonPaymentList.clear();
		} else {
			logger.error("Please select atleast one record");
			throw new BadRequestException("Please select atleast one record");
		}
	}

	@Override
	public Optional<ReportsVO> getShowCauseNoDetailsExisting(String officecode, RegReportVO regReportVO,Pageable pagable) {
		Page<ShowCauseDetailsDTO> showCauseDetails = showCauseReportForCovList(regReportVO,officecode,pagable);
		if (showCauseDetails.hasContent()) {
			Integer page = 0;
			List<ShowCauseDetailsDTO> showCauseDetailsDtoList = showCauseDetails.getContent();
			if (CollectionUtils.isNotEmpty(showCauseDetailsDtoList)) {
				Pair<Integer, List<ShowCauseDetailsVO>> showCauseReportPair = getShowCauseReportPair(page,
						showCauseDetailsDtoList,regReportVO);
				ReportsVO reportVO = new ReportsVO();
				reportVO.setShowCauseDetails(showCauseReportPair.getSecond());
				reportVO.setPageNo(showCauseDetails.getNumber());
				reportVO.setTotalPage(showCauseDetails.getTotalPages());
				return Optional.of(reportVO);
			}
		}
		return Optional.empty();
	}


	private void getAddressOfOwner(NonPaymentDetailsDTO nonPaymentDto, RegistrationDetailsDTO regDetails) {
		if (null != regDetails.getApplicantDetails()) {
			ApplicantDetailsDTO applicantDTO = regDetails.getApplicantDetails();
			if (StringUtils.isNoneBlank(applicantDTO.getDisplayName())) {
				nonPaymentDto.setOwnerName(applicantDTO.getDisplayName());
			}
			if (StringUtils.isNotEmpty(applicantDTO.getFatherName())) {
				nonPaymentDto.setFatherName(applicantDTO.getFatherName());
			} else if (StringUtils.isNotEmpty(applicantDTO.getRepresentativeName())) {
				nonPaymentDto.setFatherName(applicantDTO.getRepresentativeName());
			} else {
				nonPaymentDto.setFatherName(StringUtils.EMPTY);
			}
			if (null != regDetails.getApplicantDetails().getPresentAddress()) {
				ApplicantAddressDTO applicantAddress = regDetails.getApplicantDetails().getPresentAddress();
				StringBuilder address = new StringBuilder();
				if (StringUtils.isNoneBlank(applicantAddress.getDoorNo())) {
					address.append(applicantAddress.getDoorNo()).append(", ");
				}
				if (StringUtils.isNoneBlank(applicantAddress.getStreetName())) {
					address.append(applicantAddress.getStreetName()).append(", ");
				}
				if (null != applicantAddress.getMandal()) {
					if (StringUtils.isNoneBlank(applicantAddress.getMandal().getMandalName())) {
						address.append(applicantAddress.getMandal().getMandalName()).append(", ");
						nonPaymentDto.setMandalName(applicantAddress.getMandal().getMandalName());
					}
				}
				if (null != applicantAddress.getDistrict()
						&& null != applicantAddress.getDistrict().getDistrictName()) {
					address.append(applicantAddress.getDistrict().getDistrictName()).append(", ");
				}
				if (StringUtils.isNoneBlank(applicantAddress.getState().getStateName())) {
					address.append(applicantAddress.getState().getStateName()).append(" - ");
				}
				if (null != applicantAddress.getPostOffice()
						&& null != applicantAddress.getPostOffice().getPostOfficeCode()) {
					address.append(applicantAddress.getPostOffice().getPostOfficeCode());
				}
				nonPaymentDto.setOwnerAddress(address.toString());
				if (regDetails.getOfficeDetails() != null
						&& StringUtils.isNotEmpty(regDetails.getOfficeDetails().getOfficeName())) {
					nonPaymentDto.setOfficeName(regDetails.getOfficeDetails().getOfficeName());
				}
				nonPaymentDto.setCov(regDetails.getClassOfVehicle());
				if (StringUtils.isNotEmpty(regDetails.getClassOfVehicleDesc())) {
					nonPaymentDto.setCovDesc(regDetails.getClassOfVehicleDesc());
				}
				if (regDetails.getOfficeDetails() != null
						&& StringUtils.isNotEmpty(regDetails.getOfficeDetails().getOfficeCode())) {
					nonPaymentDto.setOfficeCode(regDetails.getOfficeDetails().getOfficeCode());
					if (nonPaymentDto.getDistrictId() == null) {
						nonPaymentDto.setDistrictId(getOfficeCodeByDistricId().get(nonPaymentDto.getOfficeCode()));
					}
					if (nonPaymentDto.getDistrictId() != null) {
						nonPaymentDto.setDistrictName(getDistricIdByDistricName().get(nonPaymentDto.getDistrictId()));
					}
				}
			}
			if (applicantDTO.getContact() != null && StringUtils.isNotEmpty(applicantDTO.getContact().getMobile())) {
				nonPaymentDto.setMobileNo(applicantDTO.getContact().getMobile());
			}
		}

	}

	private void getPermitDetailsData(RegistrationDetailsDTO regDetails, NonPaymentDetailsDTO nonPaymentDetails) {
		Optional<PermitDetailsDTO> permitDetailsOpt = permitDetailsDAO
				.findByPrNoAndPermitStatusAndPermitTypeTypeofPermitOrderByCreatedDateDesc(regDetails.getPrNo(),
						PermitsEnum.ACTIVE.getDescription(), PermitType.PRIMARY.getPermitTypeCode());
		if (permitDetailsOpt.isPresent()) {
			PermitDetailsDTO permitDetails = permitDetailsOpt.get();
			if (null != permitDetails.getPermitValidityDetails()) {
				nonPaymentDetails.setPermitValidity(permitDetails.getPermitValidityDetails().getPermitValidTo());
				nonPaymentDetails.setPermitNo(permitDetails.getPermitNo());
			}
		}
	}

	@Override
	public Optional<ReportsVO> nonPaymentReportForCovCount(RegReportVO regReportVO, String officeCode) {
		NonPaymentDetailsVO nonPaymentReporstVO = new NonPaymentDetailsVO();
		List<ReportVO> aggregationlist = covCountReport(regReportVO, officeCode);
		if (CollectionUtils.isNotEmpty(aggregationlist)) {
			aggregationlist.stream().forEach(val -> {
				if(StringUtils.isNotBlank(val.getCov())) {
				val.setCovDesc(getCovCodeByCovName().get(val.getCov()));
				}
			});
			this.setGcrtCovDesc(aggregationlist);
			nonPaymentReporstVO.setCovReport(aggregationlist);
			ReportsVO reportsVO = new ReportsVO();
			reportsVO.setNonPaymentDetails(Arrays.asList(nonPaymentReporstVO));
			return Optional.of(reportsVO);
		}
		return Optional.empty();
	}

	private List<ReportVO> getCovCountReport(LocalDate taxPeroidEnd, String officeCode,List<String> covs,String filed) {
		Aggregation agg = newAggregation(
				match(Criteria.where("taxValidity").lt(taxPeroidEnd).and("cov").in(covs)
						.and("officeCode").is(officeCode).and("isScNoIssuedByCco").is(false)),
				group(filed).count().as("covCount"), project("covCount").and(filed).previousOperation());
		AggregationResults<ReportVO> groupResults = mongoTemplate.aggregate(agg, NonPaymentDetailsDTO.class,
				ReportVO.class);
		return groupResults.getMappedResults();

	}

	@Override
	public Optional<ReportsVO> nonPaymentReportForCov(RegReportVO regReportVO, String officeCode, Pageable pagable) {
		Page<NonPaymentDetailsDTO> nonPayment = nonPaymentReportForCovList(regReportVO, officeCode, pagable);
		if (nonPayment.hasContent()) {
			List<NonPaymentDetailsDTO> nonPaymentList = nonPayment.getContent();
			if (CollectionUtils.isNotEmpty(nonPaymentList)) {
				List<NonPaymentDetailsVO> nonPaymentReportPair = getNonpaymentReportPair(nonPaymentList,regReportVO);
				ReportsVO reportVO = new ReportsVO();
				reportVO.setNonPaymentDetails(nonPaymentReportPair);
				reportVO.setPageNo(nonPayment.getNumber());
				reportVO.setTotalPage(nonPayment.getTotalPages());
				return Optional.of(reportVO);
			}
		}
		return Optional.empty();
	}

	private List<NonPaymentDetailsVO> getNonpaymentReportPair(List<NonPaymentDetailsDTO> nonPaymentList,RegReportVO regReportVO) {
		if(!nonPaymentList.isEmpty()) {
			List<NonPaymentDetailsVO> nonPaymentVOList = nonPaymentsDetailsMapper.convertEntityList(nonPaymentList);
			nonPaymentVOList.stream().forEach(val->{
				val.setCov(setCovWithWeight(val.getCov(),regReportVO));
			});
			return nonPaymentVOList;
		} else {
			throw new BadRequestException("No Records Found");
		}
	}

	private void getFeeDetails(NonPaymentDetailsDTO nonPaymentDto, String prNo, RegistrationDetailsDTO regDetails) {
		FeeDetailsVO feeDetailsVO = getTaxFeeDetails(prNo);
		if (feeDetailsVO != null) {
			Double taxArrearsAmount = getAmount(feeDetailsVO, ServiceCodeEnum.TAXARREARS.getTypeDesc());
			Double quarterlyTaxAmount = getAmount(feeDetailsVO, ServiceCodeEnum.QLY_TAX.getCode());
			Double penaltyArrearsAmount = getAmount(feeDetailsVO, ServiceCodeEnum.PENALTYARREARS.getTypeDesc());
			Double penaltyAmount = getAmount(feeDetailsVO, ServiceCodeEnum.PENALTY.getCode());
			nonPaymentDto.setTaxAmount(quarterlyTaxAmount);
			nonPaymentDto.setTaxArrears(taxArrearsAmount);
			nonPaymentDto.setPenaltyArrears(penaltyArrearsAmount);
			nonPaymentDto.setPenaltyAmount(penaltyAmount);
			nonPaymentDto.setTotalFee(taxArrearsAmount + quarterlyTaxAmount + penaltyArrearsAmount + penaltyAmount);
			try {
				getNumberOfQuarters(regDetails, nonPaymentDto);
			} catch (Throwable e) {
				logger.error("getNumberOfQuarters Exception : {}", e);
			}
		}
	}

	private FeeDetailsVO getTaxFeeDetails(String prNo) {
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		headers.add("Accept", "application/json");
		CitizenFeeDetailsInput input = new CitizenFeeDetailsInput();
		input.setServiceIds(Arrays.asList(ServiceEnum.TAXATION.getId()));
		input.setPrNo(prNo);
		input.setTaxType(TaxTypeEnum.QuarterlyTax.getDesc());
		HttpEntity<CitizenFeeDetailsInput> httpEntity = new HttpEntity<>(input, headers);
		try {
			ResponseEntity<String> response = restTemplate.exchange(paymentBraekupUrl, HttpMethod.POST, httpEntity,
					String.class);
			if (response.hasBody() && response.getStatusCode().equals(HttpStatus.OK)) {
				ObjectMapper mapper = new ObjectMapper();
				FeeDetailsResponceVO feeDetailsResponceVO = mapper.readValue(response.getBody(),
						FeeDetailsResponceVO.class);
				return feeDetailsResponceVO.getResult();
			}
		} catch (Exception e) {
			logger.error("An exception occured while getting a record  : {}", e);
		}
		return null;
	}

	private Double getAmount(FeeDetailsVO feeDetailsVO, String taxTypeCode) {
		Set<FeesVO> taxArrears = feeDetailsVO.getFeeDetails().stream()
				.filter(val -> val.getFeesType().equals(taxTypeCode)).collect(Collectors.toSet());
		if (CollectionUtils.isNotEmpty(taxArrears)) {
			FeesVO taxArrearsVo = taxArrears.stream().findFirst().get();
			return taxArrearsVo.getAmount();
		}
		return 0d;
	}

	@Override
	public Optional<NonPaymentDetailsVO> showCausePrint(ApplicationSearchVO applicationSearchVO, String officeCode) {
		Optional<NonPaymentDetailsDTO> nonPaymentDetailsOpt = Optional.empty();
		if (StringUtils.isNoneEmpty(applicationSearchVO.getPrNo())) {
			nonPaymentDetailsOpt = nonPaymentDetailsDAO.findByOfficeCodeAndApplicationStatusInAndPrNo(officeCode,
					Arrays.asList(StatusRegistration.AOAPPROVED, StatusRegistration.RTOAPPROVED),
					applicationSearchVO.getPrNo());
		}
		if (StringUtils.isNoneEmpty(applicationSearchVO.getScNo())) {
			nonPaymentDetailsOpt = nonPaymentDetailsDAO.findByOfficeCodeAndApplicationStatusInAndScNo(officeCode,
					Arrays.asList(StatusRegistration.AOAPPROVED, StatusRegistration.RTOAPPROVED),
					applicationSearchVO.getScNo());
		}
		if (nonPaymentDetailsOpt.isPresent()) {
			return Optional.of(nonPaymentsDetailsMapper.convertFieldsToPrint(nonPaymentDetailsOpt.get()));
		}
		throw new BadRequestException("No details available");
	}

	@Override
	public List<NonPaymentDetailsVO> getShowCauseNoDetailsBetweenDates(NonPaymentDetailsVO nonPaymentDetailsVO,String officeCode) {
		MasterCovDTO masterCovDTO = null;
		LocalDate fromDate = nonPaymentDetailsVO.getFromDate();
		LocalDate toDate = nonPaymentDetailsVO.getToDate();
		masterCovDTO = masterCovDAO.findByCovdescription(nonPaymentDetailsVO.getCov());
		if (masterCovDTO != null && StringUtils.isNotEmpty(masterCovDTO.getCovcode())) {
			Criteria criteria1 = Criteria.where("sectionCode").is(nonPaymentDetailsVO.getSectionCode());
			Criteria criteria2 = Criteria.where("cov").is(masterCovDTO.getCovcode());
			Criteria criteria3 = Criteria.where("scNoIssuedOn").gte(fromDate).lte(toDate);
			Criteria criteria4 = Criteria.where("officeCode").is(officeCode);
			Query query = new Query();
			query.addCriteria(new Criteria().andOperator(criteria1, criteria2, criteria3,criteria4));
			List<NonPaymentDetailsDTO> nonPaymentDetails = mongoTemplate.find(query, NonPaymentDetailsDTO.class);
			if (CollectionUtils.isNotEmpty(nonPaymentDetails)) {
				return nonPaymentsDetailsMapper.convertEntityLimitedList(nonPaymentDetails);
			}
		}
		return Collections.emptyList();
	}

	@Override
	public List<String> getClassOfVehiclesDesc() {

		List<MasterCovDTO> classOfVehicleList = new ArrayList<>();
		for (MasterPayperiodDTO masterPayPeriod : getMasterPayPeriod()) {
			MasterCovDTO masterCovDTO = getMasterCov(masterPayPeriod.getCovcode());
			if (null != masterCovDTO) {
				classOfVehicleList.add(masterCovDTO);
			}
		}
		return classOfVehicleList.stream().map(cov -> cov.getCovdescription()).collect(Collectors.toList());
	}

	private List<String> getAllOfficeCodes() {
		List<OfficeDTO> officeCodelist = officeDAO.findByTypeAndIsActiveNative(Arrays.asList(OfficeType.RTA.getCode(),OfficeType.UNI.getCode()));
		if (CollectionUtils.isNotEmpty(officeCodelist)) {
			List<OfficeDTO> filterlist = officeCodelist.stream().filter(
					o -> !((o.getOfficeCode().contains(CommonConstants.OTHER)) || (o.getOfficeCode().contains("HSRP"))))
					.collect(Collectors.toList());
			return filterlist.stream().map(val -> val.getOfficeCode()).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	private LocalDateTime getTimewithDate(LocalDate date, Boolean timeZone) {

		String dateVal = date + "T00:00:00.000Z";
		if (timeZone) {
			dateVal = date + "T23:59:59.999Z";
		}
		ZonedDateTime zdt = ZonedDateTime.parse(dateVal);
		return zdt.toLocalDateTime();
	}

	private List<String> taxTypes() {
		List<String> taxTypes = new ArrayList<>();
		taxTypes.add(ServiceCodeEnum.QLY_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.HALF_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.YEAR_TAX.getCode());
		return taxTypes;
	}

	private Pair<Integer, List<ShowCauseDetailsVO>> getShowCauseReportPair(Integer page,
			List<ShowCauseDetailsDTO> showCauseList,RegReportVO regReportVO) {
		if (!showCauseList.isEmpty()) {
			List<ShowCauseDetailsVO> showCauseVOList = null;
			showCauseVOList = showCauseDetailsMapper.convertSpecificFieldsList(showCauseList);
			showCauseVOList.forEach(val ->{
				val.setCov(setCovWithWeight(val.getCov(),regReportVO));
			});
			page = showCauseList.size();
			return Pair.of(page, showCauseVOList);
		} else {
			throw new BadRequestException("No Records Found");
		}
	}

	@Override
	public Optional<NonPaymentDetailsVO> showCauseDisplay(ApplicationSearchVO applicationSearchVO, String officeCode) {
		Optional<NonPaymentDetailsDTO> nonPaymentDetailsOpt = Optional.empty();
		if (StringUtils.isNotEmpty(applicationSearchVO.getPrNo())) {
			nonPaymentDetailsOpt = nonPaymentDetailsDAO.findByOfficeCodeAndApplicationStatusInAndPrNo(officeCode,
					Arrays.asList(StatusRegistration.AOAPPROVED, StatusRegistration.RTOAPPROVED),
					applicationSearchVO.getPrNo());
		}
		if (StringUtils.isNotEmpty(applicationSearchVO.getScNo())) {
			nonPaymentDetailsOpt = nonPaymentDetailsDAO.findByOfficeCodeAndApplicationStatusInAndScNo(officeCode,
					Arrays.asList(StatusRegistration.AOAPPROVED, StatusRegistration.RTOAPPROVED),
					applicationSearchVO.getScNo());
		}
		if (nonPaymentDetailsOpt.isPresent()) {
			return Optional.of(nonPaymentsDetailsMapper.convertFieldsToDisplay(nonPaymentDetailsOpt.get()));
		}
		throw new BadRequestException("No details available");
	}

	@Override
	public Optional<ReportsVO> getDistrictCountReport(RegReportVO regReportVO) {
		ReportsVO reportVO = new ReportsVO();
		LocalDate date = getPendingMonthsDate(regReportVO.getQuarterEndDate(), regReportVO.getPendingQuarter());
		List<ReportVO> aggregationlist = getDistrictReport(regReportVO,date);
		if (CollectionUtils.isNotEmpty(aggregationlist)) {
			aggregationlist.stream().forEach(val -> {
				val.setDistrictName(getDistricIdByDistricName().get(val.getDistrictId()));
			});
		}
		reportVO.setReport(aggregationlist);
		return Optional.of(reportVO);
	}

	private void getNumberOfQuarters(RegistrationDetailsDTO regiDetails, NonPaymentDetailsDTO nonPaymentDto) {
		TaxHelper lastTaxTillDate = citizenTaxService.getLastPaidTax(regiDetails, null, false,
				citizenTaxService.validity(TaxTypeEnum.QuarterlyTax.getDesc()), null, false, taxTypes(), false, false);
		if (lastTaxTillDate == null || lastTaxTillDate.getTax() == null || lastTaxTillDate.getValidityTo() == null) {
			throw new BadRequestException("TaxDetails not found");
		}
		if (lastTaxTillDate.isAnypendingQuaters()) {
			long totalMonths = ChronoUnit.MONTHS.between(lastTaxTillDate.getValidityTo(),
					citizenTaxService.validity(TaxTypeEnum.QuarterlyTax.getDesc()));
			Integer months = (int) totalMonths;
			Integer quaters = months / 3;
			nonPaymentDto.setPendingQuarters(quaters);
			Pair<Integer, Integer> indexPosiAndQuarterNo = citizenTaxService.getMonthposition(LocalDate.now());
			if (quaters > 1) {
				// penality arr = 50%
				nonPaymentDto.setPenalityPercentage("50%");
			}
			if (indexPosiAndQuarterNo.getSecond() == 1) {
				// tax penality = 25%
				nonPaymentDto.setTaxPenalityPercentage("25%");
			} else if (indexPosiAndQuarterNo.getSecond() == 2) {
				// tax penality = 50%
				nonPaymentDto.setTaxPenalityPercentage("50%");
			}

		}
	}

	@Override
	public Optional<ReportsVO> getOfficeCountReport(RegReportVO regReportVO, String role, String officeCode) {
		ReportsVO reportVO = new ReportsVO();
		LocalDate date =getPendingMonthsDate(regReportVO.getQuarterEndDate(), regReportVO.getPendingQuarter());
		List<ReportVO> list = getOfficeReport(date,
				getUserDistrict(role, officeCode, regReportVO), regReportVO.getPendingQuarter());
		if (CollectionUtils.isNotEmpty(list)) {
			list.stream().forEach(val -> {
				val.setOfficeName(paymentReportService.getOfficeCodesByOfficeName().get(val.getOfficeCode()));
			});
		}
		reportVO.setReport(list);
		return Optional.of(reportVO);
	}

	private List<ReportVO> aggregationOfficeNonReport(LocalDate taxPeroidEnd, Integer districtId) {
		Aggregation agg = newAggregation(
				match(Criteria.where("taxValidity").lt(taxPeroidEnd).and("isScNoIssuedByCco").is(false)
						.and("districtId").is(districtId).and("cov").in(getClassOfVehicles())),
				group("officeCode").count().as("count"), project("count").and("officeCode").previousOperation());
		AggregationResults<ReportVO> groupResults = mongoTemplate.aggregate(agg, NonPaymentDetailsDTO.class,
				ReportVO.class);
		return groupResults.getMappedResults();
	}

	private Integer getUserDistrict(String role, String officeCode, RegReportVO regReportVO) {
		if (role.equals(RoleEnum.DTC.getName())) {
			Optional<OfficeDTO> officeOpt = officeDAO.findByOfficeCodeAndIsActiveTrue(officeCode);
			if (!officeOpt.isPresent() && officeOpt.get().getDistrict() == null) {
				throw new BadRequestException("District details not found");
			}
			return officeOpt.get().getDistrict();
		}
		return regReportVO.getDistrictId();
	}

	private boolean skipLifeTaxConvertCovs(RegistrationDetailsDTO regDetails) {
		Integer gvw = 0;
		if (StringUtils.isNotEmpty(regDetails.getClassOfVehicle())) {
			if (regDetails.getVahanDetails() != null && regDetails.getVahanDetails().getGvw() != null) {
				gvw = regDetails.getVahanDetails().getGvw();
			}
			if (regDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())
					|| (regDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode())
							&& gvw <= 3000)
					|| regDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())
					|| regDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TRTT.getCovCode())
					|| regDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TRCN.getCovCode())
					|| regDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.OBPN.getCovCode())) {
				return false;
			}
		} else {
			return false;
		}
		return true;
	}

	

	/**
	 * method is used to check vehicle stoppage , suspend , cancellation
	 * @Parameter  RegistrationDetailsDTO
	 * @return true or false
	 * 
	 */
	private boolean skipRecord(RegistrationDetailsDTO regDto) {
		if (regDto.getApplicationStatus().equalsIgnoreCase(StatusRegistration.RCCANCELLED.getDescription())
				|| regDto.isVehicleStoppaged() || regDto.isVehicleStoppageRevoked()
				|| (StringUtils.isNotEmpty(regDto.getActionStatus()) && (regDto.getActionStatus()
						.equalsIgnoreCase(StatusRegistration.INITIATED.getDescription())
						|| regDto.getActionStatus().equalsIgnoreCase(StatusRegistration.SUSPEND.getDescription())))) {
			return false;
		}
		return true;
	}

	@Override
	public void nonPaymentMoveToHistory(TaxDetailsDTO taxDetailsDTO) {
		try {
		Optional<NonPaymentDetailsDTO> nonpaymetOpt =  nonPaymentQurey(taxDetailsDTO);
		if (nonpaymetOpt.isPresent()) {
			String prNo = nonpaymetOpt.get().getPrNo();
			synchronized (prNo.intern()) {
			nonpaymetOpt.get().setTaxPaid(true);
			nonpaymetOpt.get().setTaxPaidDate(LocalDateTime.now());
			NonPaymentDetailsHistoryDTO nonPaymentHistory = nonPaymentsDetailsMapper.convertHistory(nonpaymetOpt.get());
			nonPaymentHistroyDAO.save(nonPaymentHistory);
			nonPaymentDetailsDAO.delete(nonpaymetOpt.get());
			}
		}
		}catch(Throwable e) {
			logger.error("nonPaymentMoveToHistory Exception : {} ",e);
		}
	}

	private void getVehicleDetails(RegistrationDetailsDTO regDto, NonPaymentDetailsDTO nonPaymentDto,
			List<WeightDTO> weightsList) {
		Integer gvw = getGvw(regDto);
		nonPaymentDto.setGvw(gvw);
		nonPaymentDto.setWeightType(weghitType(weightsList, gvw));
	}

	private List<ReportVO> aggregationOfficeNonReportPending(LocalDate taxPeroidEnd, Integer districtId) {
		Aggregation agg = newAggregation(
				match(Criteria.where("taxValidity").is(taxPeroidEnd).and("isScNoIssuedByCco").is(false)
						.and("districtId").is(districtId).and("cov").in(getClassOfVehicles())),
				group("officeCode").count().as("count"), project("count").and("officeCode").previousOperation());
		AggregationResults<ReportVO> groupResults = mongoTemplate.aggregate(agg, NonPaymentDetailsDTO.class,
				ReportVO.class);
		return groupResults.getMappedResults();
	}

	private List<ReportVO> getOfficeReport(LocalDate taxPeroidEnd, Integer districtId, Integer pendingQuarter) {
		List<ReportVO> reportList = null;
		if (pendingQuarter > 0) {
			LocalDate taxEndDate = getPendingQuarterMonthsDate(taxPeroidEnd,pendingQuarter); 
			reportList = aggregationOfficeNonReportPending(taxEndDate, districtId);
		} else {
			reportList = aggregationOfficeNonReport(getPendingMonthsDate(taxPeroidEnd,pendingQuarter), districtId);
		}
		return reportList;
	}

	private List<ReportVO> getDistrictReport(RegReportVO regReportVO , LocalDate date) {
		List<ReportVO> reportList = null;
		if (regReportVO.getPendingQuarter() > 0) {
			LocalDate taxEndDate = getPendingQuarterMonthsDate(regReportVO.getQuarterEndDate(),regReportVO.getPendingQuarter()); 
			reportList=getAggregationDistrictPending(taxEndDate);
		} else {
			reportList = getAggregationDistrict(getPendingMonthsDate(regReportVO.getQuarterEndDate(),regReportVO.getPendingQuarter()));
		}
		return reportList;
	}

	private List<ReportVO> getAggregationDistrictPending(LocalDate taxEndDate) {
		Aggregation agg = newAggregation(
				match(Criteria.where("taxValidity").is(taxEndDate).and("isScNoIssuedByCco").is(false).and("cov")
						.in(getClassOfVehicles())),
				group("districtId").count().as("count"), project("count").and("districtId").previousOperation());
		AggregationResults<ReportVO> groupResults = mongoTemplate.aggregate(agg, NonPaymentDetailsDTO.class,
				ReportVO.class);
		return groupResults.getMappedResults();
	}

	private List<ReportVO> getAggregationDistrict(LocalDate taxEndDate) {
		Aggregation agg = newAggregation(
				match(Criteria.where("taxValidity").lt(taxEndDate).and("isScNoIssuedByCco").is(false).and("cov")
						.in(getClassOfVehicles())),
				group("districtId").count().as("count"), project("count").and("districtId").previousOperation());
		AggregationResults<ReportVO> groupResults = mongoTemplate.aggregate(agg, NonPaymentDetailsDTO.class,
				ReportVO.class);
		return groupResults.getMappedResults();
	}

	private List<ReportVO> covCountReport(RegReportVO regReportVO, String officeCode) {
		List<ReportVO> covList = Collections.emptyList();
		List<ReportVO> gcrtCovList = Collections.emptyList();
		if (regReportVO.getPendingQuarter() > 0) {
			LocalDate taxPeriodEnd = getPendingQuarterMonthsDate(regReportVO.getQuarterEndDate(),
					regReportVO.getPendingQuarter());
			covList = getCovCountReportPending(taxPeriodEnd, officeCode,getClassOfVehicles(),"cov");
			gcrtCovList =  getCovCountReportPending(taxPeriodEnd, officeCode,Arrays.asList(getGcrtCov()),"weightType"); 
					
		} else {
			LocalDate taxPeriodEnd = getPendingMonthsDate(regReportVO.getQuarterEndDate(),
					regReportVO.getPendingQuarter());
			covList = getCovCountReport(taxPeriodEnd, officeCode,getClassOfVehicles(),"cov");
			gcrtCovList = getCovCountReport(taxPeriodEnd, officeCode,Arrays.asList(getGcrtCov()),"weightType");
		}
		return gcrtCovCheck(covList,gcrtCovList);
	}

	private List<ReportVO> getCovCountReportPending(LocalDate taxPeroidEnd, String officeCode,List<String> covs,String filed) {
		Aggregation agg = newAggregation(
				match(Criteria.where("taxValidity").is(taxPeroidEnd).and("cov").in(covs)
						.and("officeCode").is(officeCode).and("isScNoIssuedByCco").is(false)),
				group(filed).count().as("covCount"), project("covCount").and(filed).previousOperation());
		AggregationResults<ReportVO> groupResults = mongoTemplate.aggregate(agg, NonPaymentDetailsDTO.class,
				ReportVO.class);
		return groupResults.getMappedResults();
	}

	private List<ReportVO> gcrtAggPending(LocalDate taxPeroidEnd, String officeCode) {
		Aggregation agg = newAggregation(
				match(Criteria.where("taxValidity").is(taxPeroidEnd).and("cov").is(getGcrtCov())
						.and("officeCode").is(officeCode).and("isScNoIssuedByCco").is(false)),
				group("weightType").count().as("covCount"), project("covCount").and("weightType").previousOperation());
		AggregationResults<ReportVO> groupResults = mongoTemplate.aggregate(agg, NonPaymentDetailsDTO.class,
				ReportVO.class);
		List<ReportVO> gcrtVOList = groupResults.getMappedResults();
		gcrtVOList.stream().forEach(cov ->{
			cov.setCov(getGcrtCov());
		});
		return gcrtVOList;
	}

	private Page<NonPaymentDetailsDTO> nonPaymentReportForCovList(RegReportVO regReportVO, String officeCode,
			Pageable pagable) {
		if (getGcrtCov().equalsIgnoreCase(regReportVO.getCov())) {
			weghitTypeCheck(regReportVO);
			return getNonPaymentOfficeWiseGcrtDetailsQurey(regReportVO, pagable, officeCode,
					regReportVO.getWeightType());
		} else {
			return getNonPaymentOfficeWiseDetailsQurey(regReportVO, pagable, officeCode);
		}
	}

	private LocalDate getPendingMonthsDate(LocalDate date, Integer pendingQuarter) {
		return date.minusMonths(pendingQuarter * 3);
		//return LocalDate.of(taxPeriodEnd.getYear(), taxPeriodEnd.getMonth(), taxPeriodEnd.lengthOfMonth());
	}
	
	
	private LocalDate getQuarterDate(String quater ,Integer year, Integer pendingQuarter) {
		LocalDate date = null;
		checkQuater(quater);
		switch (quater) {
		case "Q1":
			date = LocalDate.of(year, Month.JUNE, 30);
			break;
		case "Q2":
			date = LocalDate.of(year, Month.SEPTEMBER, 30);
			break;
		case "Q3":
			date = LocalDate.of(year, Month.DECEMBER, 31);
			break;
		case "Q4":
			if(pendingQuarter>0){
				date = LocalDate.of(year , Month.MARCH, 31);	
			}else{
				date = LocalDate.of(year + 1, Month.MARCH, 31);	
			}
			break;
		default:
			break;
		}
		return date;
	}
	private void checkQuater(String quater) {
		if(!Arrays.asList("Q1","Q2","Q3","Q4").contains(quater)) {
			throw new BadRequestException("Invalid Quater");
		}
	}
	
	private List<ReportVO> aggregationMandalNonReportPending(LocalDate taxPeroidEnd, String officeCode){
		Aggregation agg = newAggregation(
				match(Criteria.where("taxValidity").lt(taxPeroidEnd).and("isScNoIssuedByCco").is(false).and("officeCode").is(officeCode).and("cov").in(getClassOfVehicles())),
				group("mandalName").count().as("count"), project("count").and("mandalName").previousOperation());
		AggregationResults<ReportVO> groupResults = mongoTemplate.aggregate(agg, NonPaymentDetailsDTO.class, ReportVO.class);
		return groupResults.getMappedResults();
	}
	
	@Override
	public Optional<ReportsVO> getMandalCountReport(RegReportVO regReportVO,String officeCode) {
		ReportsVO reportVO = new ReportsVO();
		List<ReportVO> aggregationlist = getAggregationMandalReport(regReportVO,officeCode);
		if(CollectionUtils.isNotEmpty(aggregationlist)) {
		aggregationlist.stream().forEach(val ->{
			if(StringUtils.isEmpty(val.getMandalName())) {
				val.setMandalName("No Mandal");
			}
		});
		}
		reportVO.setReport(aggregationlist);
		return Optional.of(reportVO);
	}
	
	@Override
	public Optional<ReportsVO> nonPaymentReportMandalWiseForCovCount(RegReportVO regReportVO, String officeCode) {
		NonPaymentDetailsVO nonPaymentReporstVO = new NonPaymentDetailsVO();
		List<ReportVO> aggregationlist = covCountReport(regReportVO,officeCode);
		if(CollectionUtils.isNotEmpty(aggregationlist)) {
		aggregationlist.stream().forEach(val ->{
			val.setCovDesc(getCovCodeByCovName().get(val.getCov()));
		});
		nonPaymentReporstVO.setCovReport(aggregationlist);
		ReportsVO reportsVO = new ReportsVO();
		reportsVO.setNonPaymentDetails(Arrays.asList(nonPaymentReporstVO));
		return Optional.of(reportsVO);
		}
		return Optional.empty();
	}
	
	private List<ReportVO> getMandalWiseCovCountReport(LocalDate taxPeroidEnd, String officeCode,String mandalName,List<String> covs,String filed) {
		Aggregation agg = newAggregation(
				match(Criteria.where("taxValidity").lt(taxPeroidEnd)
						.and("cov").in(covs).and("officeCode").is(officeCode).and("mandalName").is(mandalName).and("isScNoIssuedByCco").is(false)),
				group(filed).count().as("covCount"), project("covCount").and(filed).previousOperation());
		AggregationResults<ReportVO> groupResults = mongoTemplate.aggregate(agg, NonPaymentDetailsDTO.class, ReportVO.class);
		return groupResults.getMappedResults();
	
	}
	
	private List<ReportVO> covMandalWiseCountReport(RegReportVO regReportVO, String officeCode) {
		List<ReportVO> covList = Collections.emptyList();
		List<ReportVO> gcrtCovList = Collections.emptyList();
		if (regReportVO.getMandalName().equalsIgnoreCase("No Mandal")) {
			regReportVO.setMandalName("");
		}
		if(regReportVO.getPendingQuarter()>0) {
			LocalDate taxPeriodEnd = getPendingQuarterMonthsDate(regReportVO.getQuarterEndDate(), regReportVO.getPendingQuarter());
			covList= getMandalWiseCovCountReportPending(taxPeriodEnd, officeCode, regReportVO.getMandalName(),getClassOfVehicles(),"cov");
			gcrtCovList = getMandalWiseCovCountReportPending(taxPeriodEnd, officeCode, regReportVO.getMandalName(),Arrays.asList(getGcrtCov()),"weightType");
		}else {
			LocalDate taxPeriodEnd = getPendingMonthsDate(regReportVO.getQuarterEndDate(), regReportVO.getPendingQuarter());
			covList = getMandalWiseCovCountReport(taxPeriodEnd, officeCode, regReportVO.getMandalName(),getClassOfVehicles(),"cov");
			gcrtCovList = getMandalWiseCovCountReport(taxPeriodEnd, officeCode, regReportVO.getMandalName(),Arrays.asList(getGcrtCov()),"weightType");
		}
		return gcrtCovCheck(covList,gcrtCovList);
	}
	
	@Override
	public Optional<ReportsVO> nonPaymentMandalWiseReportForCovCount(RegReportVO regReportVO, String officeCode) {
		NonPaymentDetailsVO nonPaymentReporstVO = new NonPaymentDetailsVO();
		List<ReportVO> aggregationlist = covMandalWiseCountReport(regReportVO,officeCode);
		if(CollectionUtils.isNotEmpty(aggregationlist)) {
		aggregationlist.stream().forEach(val ->{
			val.setCovDesc(getCovCodeByCovName().get(val.getCov()));
			if(StringUtils.isEmpty(val.getMandalName())) {
				val.setMandalName("No Mandal");
			}
		});
		this.setGcrtCovDesc(aggregationlist);
		nonPaymentReporstVO.setCovReport(aggregationlist);
		ReportsVO reportsVO = new ReportsVO();
		reportsVO.setNonPaymentDetails(Arrays.asList(nonPaymentReporstVO));
		return Optional.of(reportsVO);
		}
		return Optional.empty();
	}
	
	@Override
	public Optional<ReportsVO> nonPaymentMandalWiseReportForCov(RegReportVO regReportVO, String officeCode, Pageable pagable) {
		Page<NonPaymentDetailsDTO> nonPayment = nonPaymentMandalWiseReportForCovList(regReportVO,officeCode,pagable);
		if (nonPayment.hasContent()) {
				List<NonPaymentDetailsDTO> nonPaymentList = nonPayment.getContent();
				if (CollectionUtils.isNotEmpty(nonPaymentList)) {
					List<NonPaymentDetailsVO> nonPaymentReportPair = getNonpaymentReportPair(nonPaymentList,regReportVO);
					ReportsVO reportVO = new ReportsVO();
					reportVO.setNonPaymentDetails(nonPaymentReportPair);
					reportVO.setPageNo(nonPayment.getNumber());
					reportVO.setTotalPage(nonPayment.getTotalPages());
					return Optional.of(reportVO);
				}
		}
		return Optional.empty();
	}
	
	private Page<NonPaymentDetailsDTO> nonPaymentMandalWiseReportForCovList(RegReportVO regReportVO, String officeCode,
			Pageable pagable) {
		if (regReportVO.getMandalName().equalsIgnoreCase("No Mandal")) {
			regReportVO.setMandalName("");
		}
		if(getGcrtCov().equalsIgnoreCase(regReportVO.getCov())) {
			weghitTypeCheck(regReportVO);
			return nonPaymentMandalWiseReportForCovGcrtQurey(regReportVO,officeCode,pagable);
		}else {
			return nonPaymentMandalWiseReportForCovQurey(regReportVO,officeCode,pagable);
		}
		
	}
	
	private LocalDate getPendingQuarterMonthsDate(LocalDate date,Integer pendingQuarter){
		LocalDate taxPeriodEnd= date.minusMonths(pendingQuarter * 3);
		return LocalDate.of(taxPeriodEnd.getYear(), taxPeriodEnd.getMonth(), taxPeriodEnd.lengthOfMonth());
	}
	
	private List<ReportVO> getMandalWiseCovCountReportPending(LocalDate taxPeroidEnd, String officeCode,String mandalName,List<String> covs,String filed) {
		Aggregation agg = newAggregation(
				match(Criteria.where("taxValidity").is(taxPeroidEnd)
						.and("cov").in(covs).and("officeCode").is(officeCode).and("mandalName").is(mandalName).and("isScNoIssuedByCco").is(false)),
				group(filed).count().as("covCount"), project("covCount").and(filed).previousOperation());
		AggregationResults<ReportVO> groupResults = mongoTemplate.aggregate(agg, NonPaymentDetailsDTO.class, ReportVO.class);
		return groupResults.getMappedResults();
	}
	
	private List<ReportVO> aggregationMandalNonReportPendingQuater(LocalDate taxPeroidEnd, String officeCode){
		Aggregation agg = newAggregation(
				match(Criteria.where("taxValidity").is(taxPeroidEnd).and("isScNoIssuedByCco").is(false).and("officeCode").is(officeCode).and("cov").in(getClassOfVehicles())),
				group("mandalName").count().as("count"), project("count").and("mandalName").previousOperation());
		AggregationResults<ReportVO> groupResults = mongoTemplate.aggregate(agg, NonPaymentDetailsDTO.class, ReportVO.class);
		return groupResults.getMappedResults();
	}
	
	private List<ReportVO> getAggregationMandalReport(RegReportVO regReportVO,String officeCode){
		if(regReportVO.getPendingQuarter()>0) {
			LocalDate taxPeriodEnd = getPendingQuarterMonthsDate(regReportVO.getQuarterEndDate(), regReportVO.getPendingQuarter());
			return aggregationMandalNonReportPendingQuater(taxPeriodEnd,officeCode);
		}else {
			LocalDate taxPeriodEnd = getPendingMonthsDate(regReportVO.getQuarterEndDate(),regReportVO.getPendingQuarter());
			return aggregationMandalNonReportPending(taxPeriodEnd,officeCode);
		}
		
	}
	
	@Override
	public void getNonPaymentTaxDetails() {
		List<NonPaymentDetailsDTO> nonPaymentList = new ArrayList<NonPaymentDetailsDTO>();
		List<WeightDTO> weightsList = weightDAO.findAll();
		List<String> cfstSkipList = getCfstRecords();
		LocalDateTime taxPeriodEnd = getTimewithDate(LocalDate.now(), false);
		long stMillseconds = System.currentTimeMillis();
		for (String officeCode : getAllOfficeCodes()) {
		long officeStMillseconds = System.currentTimeMillis();
			for (String cov : getClassOfVehicles()) {
				long covStMillseconds = System.currentTimeMillis();
				long qureyMs = System.currentTimeMillis();
				List<RegistrationDetailsDTO> regIds = registrationDetailDAO
						.findByOfficeDetailsOfficeCodeAndClassOfVehicleAndIsActiveAndApplicationNoNotInNativeWithId(officeCode,cov,checkExistsRecords(officeCode,cov));
			logger.info(
					"findByOfficeDetailsOfficeCodeAndClassOfVehicleAndIsActiveNativeWithId queryExecuted officeCode : {} cov : {} total time in : {}ms size : {} ",
					officeCode, cov, (System.currentTimeMillis() - qureyMs), regIds.size());
				if(!regIds.isEmpty()) {
				List<String> regIdsList = getRegIds(regIds); 
				taxDetails(regIdsList,taxPeriodEnd, weightsList,nonPaymentList,cfstSkipList);
				regIds.clear();
				}
				logger.info("classOfVehilceWise office Code : {}  cov : {} total time in : {}ms ",officeCode,cov,(System.currentTimeMillis()-covStMillseconds));
			}
			logger.info("OfficeWise offcie code : {} and allCovs total time in : {}ms ",officeCode,(System.currentTimeMillis()-officeStMillseconds));
		}
		logger.info("getNonPaymentTaxDetails completed all office and covs with : {} ms",(System.currentTimeMillis()-stMillseconds));
		weightsList.clear();
		cfstSkipList.clear();
	}
	
	
	private void taxDetails(List<String> regIdsList, LocalDateTime date, List<WeightDTO> weightsList,List<NonPaymentDetailsDTO> nonPaymentList,List<String> cfstSkipList) {
		 List<List<String>> splitlist =ListUtils.partition(regIdsList, count);
		for (List<String> sublist : splitlist) {
			if (!sublist.isEmpty()) {
				sublist.parallelStream().forEach(regId -> {
					getTaxDetailsById(regId, date, weightsList, nonPaymentList,cfstSkipList);
				});
				nonPaymentDetailsDAO.save(nonPaymentList);
				nonPaymentList.clear();
			}
			if(splitlist.isEmpty()) {
				break;
			}
		}
		regIdsList.clear();
	
	}

	private List<String> getRegIds(List<RegistrationDetailsDTO> regList) {
		return regList.stream().filter(val -> StringUtils.isNotEmpty(val.getApplicationNo()))
				.collect(Collectors.toList()).stream().map(id -> id.getApplicationNo()).collect(Collectors.toList());
	}
	
	private NonPaymentDetailsDTO setRegDetailsById(String application, List<WeightDTO> weightsList,List<String> cfstSkipList) {
		RegistrationDetailsDTO regDetails = null;
		NonPaymentDetailsDTO nonPaymentDto = null;
		try {
			regDetails = registrationDetailDAO.findOne(application);
			if (regDetails != null && regDetails.getNocDetails() == null && skipLifeTaxConvertCovs(regDetails)
					&& skipRecord(regDetails) && gcrtLifeTaxCheck(regDetails) && stuScrtCheck(regDetails)
					&& cfstRecordSkip(cfstSkipList, regDetails)) {
				nonPaymentDto = new NonPaymentDetailsDTO();
				nonPaymentDto.setPrNo(regDetails.getPrNo());
				nonPaymentDto.setRegApplicationNo(regDetails.getApplicationNo());
				getAddressOfOwner(nonPaymentDto, regDetails);
				if (regDetails.getFinanceDetails() != null) {
					nonPaymentDto.setFinancerName(regDetails.getFinanceDetails().getFinancerName());
					nonPaymentDto.setFinancerAddress(regDetails.getFinanceDetails().getAddress());
				}
				getFeeDetails(nonPaymentDto, regDetails.getPrNo(), regDetails);
				getVehicleDetails(regDetails, nonPaymentDto, weightsList);
				getPermitDetailsData(regDetails, nonPaymentDto);
				if (regDetails.getRegistrationValidity() != null
						&& regDetails.getRegistrationValidity().getFcValidity() != null) {
					nonPaymentDto.setFcValidity(regDetails.getRegistrationValidity().getFcValidity());
				} else if (regDetails.getVehicleType() != null
						&& regDetails.getVehicleType().equals(CovCategory.T.getCode())) {
					Optional<FcDetailsDTO> fcDetailsOpt = fcDetailsDAO
							.findByStatusIsTrueAndPrNoOrderByCreatedDateDesc(regDetails.getPrNo());
					if (fcDetailsOpt.isPresent() && fcDetailsOpt.get().getFcValidUpto() != null) {
						nonPaymentDto.setFcValidity(fcDetailsOpt.get().getFcValidUpto());
						fcDetailsOpt = null;
					}
				}
				regDetails = null;
			}
			return nonPaymentDto;
		} catch (Throwable e) {
			auditServiceImpl.saveErrorTrackLog(Schedulers.NONPAYMENTREPORT.name(), null, application, e.getMessage(),
					Schedulers.NONPAYMENTREPORT.name());
			logger.error("Exception occride with officeCode :{} and appliction no: {}, Exception : {}", application, e);
		}
		return nonPaymentDto;
	}

	private void getTaxDetailsById(String application, LocalDateTime date, List<WeightDTO> weightsList,List<NonPaymentDetailsDTO> nonPaymentList,List<String> cfstSkipList) {
		try {
			Optional<TaxDetailsDTO> taxDetailsOpt = taxDetailsDAO
					.findTopByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(application,
							taxTypes());
			if (taxDetailsOpt.isPresent() && taxDetailsOpt.get().getTaxPeriodEnd() != null
					&& taxDetailsOpt.get().getTaxPeriodEnd().isBefore(date.toLocalDate())) {
				NonPaymentDetailsDTO nonPaymentDto = setRegDetailsById(application, weightsList,cfstSkipList);
				if (nonPaymentDto != null) {
					if (taxDetailsOpt.get().getTaxPeriodEnd() != null) {
						nonPaymentDto.setTaxValidity(taxDetailsOpt.get().getTaxPeriodEnd());
					}
					nonPaymentDto.setCreatedDate(LocalDateTime.now());
					nonPaymentDto.setlUpdate(LocalDateTime.now());
					nonPaymentList.add(nonPaymentDto);
				}
				taxDetailsOpt = null;
			}
		} catch (Throwable e) {
			logger.error("non payment report while getting tax details Exception : {} ", e);
			auditServiceImpl.saveErrorTrackLog(Schedulers.NONPAYMENTREPORT.name(), "", application, e.getMessage(),
					Schedulers.NONPAYMENTREPORT.name());
		}
	}
	
	private String weghitType(List<WeightDTO> weightsList, Integer rlw) {
		Double fromValue = null;
		Double toValue = null;
		for (WeightDTO weightDTO : weightsList) {
			fromValue = weightDTO.getFromvalue();
			toValue = weightDTO.getTovalue();
			if (rlw >= fromValue && rlw <= toValue) {
				 return setWeghitType(weightDTO.getWeighttype());
			}
		}
		return null;
	}

private List<String> checkExistsRecords(String officeCode, String cov) {
	List<NonPaymentDetailsDTO> nonPaymentList = nonPaymentDetailsDAO.findByCovAndOfficeCodeNative(cov, officeCode);
	if(!nonPaymentList.isEmpty()) {
	return nonPaymentList.stream().filter(id -> id.getRegApplicationNo()!=null).collect(Collectors.toList()).stream().map(val -> val.getRegApplicationNo()).collect(Collectors.toList());
	}
	return Collections.emptyList();
}

Optional<NonPaymentDetailsDTO> nonPaymentQurey(TaxDetailsDTO taxDetailsDTO){
	Optional<NonPaymentDetailsDTO> nonpaymetOpt = Optional.empty();
	if(StringUtils.isNotEmpty(taxDetailsDTO.getPrNo())) {
		return nonPaymentDetailsDAO.findByPrNo(taxDetailsDTO.getPrNo());
	}else if(StringUtils.isNotEmpty(taxDetailsDTO.getApplicationNo())) {
		return  nonPaymentDetailsDAO.findByRegApplicationNo(taxDetailsDTO.getApplicationNo());
	}else {
		return nonpaymetOpt;
	}
}

	/**
	 * check gcrt class of vehicle gvw is lees than 3001 and, That vehicle have paid
	 * life those records need to skip
	 * 
	 * @param regDetails
	 * @return {@code false} class of vehicel is GCRT ,if life tax record found and
	 *         gvw is less than 3001, otherwise {@code true}
	 * 
	 * 
	 */

	private boolean gcrtLifeTaxCheck(RegistrationDetailsDTO regDto) {
		if (getGcrtCov().equalsIgnoreCase(regDto.getClassOfVehicle()) && getGvw(regDto) < 3001
				&& lifeTaxQurey(regDto.getApplicationNo())) {
			return false;
		}
		return true;
	}

	/**
	 * method get gvw of vehicle  
	 * @param regDetaisl 
	 * @return {@code value} if vehicle gvw found, otherwise {@code 0} 
	 * 
	 * */	
	private Integer getGvw(RegistrationDetailsDTO regDto) {
		Integer gvw = 0;
		if (regDto.getVahanDetails() != null) {
			if (regDto.getVahanDetails().getGvw() != null) {
				return regDto.getVahanDetails().getGvw();
			} else if (regDto.getVahanDetails().getRearAxleWeight() != null) {
				return regDto.getVahanDetails().getRearAxleWeight();
			}else {
				return gvw;
			}
	}
		return gvw;
	}
	
	/**
	 * get any life tax record based on regApplicationNo
	 * @param regApplicationNo 
	 * @return {@code true} if life tax record found, otherwise {@code false} 
	 * 
	 * */
	private boolean lifeTaxQurey(String applicationNo) {
		Optional<TaxDetailsDTO> taxDetailsOPt = taxDetailsDAO
				.findTopByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(applicationNo,
						Arrays.asList(ServiceCodeEnum.LIFE_TAX.getCode()));
		if(taxDetailsOPt.isPresent()) {
			return true;
		}
		return false;
	}
	
	private Page<ShowCauseDetailsDTO> showCauseReportForCovList(RegReportVO regReportVO,
			String officeCode, Pageable pagable) {
		if(getGcrtCov().equalsIgnoreCase(regReportVO.getCov())) {
			weghitTypeCheck(regReportVO);
			return showCauseReportForGcrtCovQurey(regReportVO,officeCode,pagable);
		}else {
			return showCauseReportForCovQurey(regReportVO,officeCode,pagable);
		}
	}
	/***
	 * OwnerShip type stu and cov scrt 
	 * @param reg Details 
	 * @return {@code true} if ownership type is not stu and cov not a scrt , otherwise {@code false} 
	 *  
	 */
	private boolean stuScrtCheck(RegistrationDetailsDTO regDto) {
		if (regDto.getOwnerType() != null && regDto.getOwnerType().equals(OwnerTypeEnum.Stu)
				&& ClassOfVehicleEnum.SCRT.getCovCode().equalsIgnoreCase(regDto.getClassOfVehicle())) {
			return false;
		}
		return true;
	}
	
	
	private List<NonPaymentDetailsDTO> nonPaymentReportForCovExcelList(RegReportVO regReportVO,
			String officeCode) {
		if(getGcrtCov().equalsIgnoreCase(regReportVO.getCov())) {
			weghitTypeCheck(regReportVO);
			return  nonPaymentReportForCovExcelListGcrtQurey(regReportVO,officeCode);
		}else {
			return nonPaymentReportForCovExcelListQurey(regReportVO ,officeCode);
		}
	}
	
	private List<NonPaymentDetailsDTO> nonPaymentMandalWiseReportForCovExcelList(RegReportVO regReportVO, String officeCode) {
		if (regReportVO.getMandalName().equalsIgnoreCase("No Mandal")) {
			regReportVO.setMandalName("");
		}
		if(regReportVO.getPendingQuarter()>0) {
			LocalDate taxPeriodEnd = getPendingQuarterMonthsDate(regReportVO.getQuarterEndDate(), regReportVO.getPendingQuarter());
			return nonPaymentDetailsDAO.findByOfficeCodeAndTaxValidityEqualsAndCovAndMandalNameAndIsScNoIssuedByCcoFalseNative(
					officeCode, taxPeriodEnd, regReportVO.getCov(), regReportVO.getMandalName());	
		}else {
			LocalDate taxPeriodEnd = getPendingMonthsDate(regReportVO.getQuarterEndDate(), regReportVO.getPendingQuarter());
			return nonPaymentDetailsDAO.findByOfficeCodeAndTaxValidityLessThanAndCovAndMandalNameAndIsScNoIssuedByCcoFalseNative(
					officeCode, taxPeriodEnd, regReportVO.getCov(), regReportVO.getMandalName());	
		}
	}
	@Override
	public List<NonPaymentDetailsDTO> getNonPymentDetailsExcle(RegReportVO regReportVO){
		if(regReportVO.getMandalName()!=null) {
			return nonPaymentMandalWiseReportForCovExcelList(regReportVO,regReportVO.getOfficeCode());
		}else {
			return nonPaymentReportForCovExcelList(regReportVO,regReportVO.getOfficeCode());
		}
	}
	
	private String setWeghitType(String weghitType) {
		String weghit = "";
		switch (weghitType) {
		case "HMV":
			weghit = "HGV";
			break;
		case "MMV":
			weghit = "MGV";
			break;
		case "LMV":
			weghit = "LGV";
			break;
		default:
			break;
		}
		return weghit;
	}
	
	private List<ReportVO> gcrtCovCheck(List<ReportVO> covList, List<ReportVO> gcrtCovList) {
		List<ReportVO> newCovList = new LinkedList<ReportVO>();
		newCovList.addAll(covList);
		List<ReportVO> reportVOList = new LinkedList<ReportVO>();
		
		if (!newCovList.isEmpty()) {
			newCovList.removeIf(val->val.getCov().equals(getGcrtCov()));
		}
		reportVOList.addAll(newCovList);
		if (!gcrtCovList.isEmpty()) {
			gcrtCovList.forEach(cov ->{
				cov.setCov(getGcrtCov());
			});
			reportVOList.addAll(gcrtCovList);
		}
		return reportVOList;
	}
	
	private List<ReportVO> gcrtAgg(LocalDate taxPeroidEnd, String officeCode) {
		Aggregation agg = newAggregation(
				match(Criteria.where("taxValidity").lt(taxPeroidEnd).and("cov").is(getGcrtCov())
						.and("officeCode").is(officeCode).and("isScNoIssuedByCco").is(false)),
				group("weightType").count().as("covCount"), project("covCount").and("weightType").previousOperation());
		AggregationResults<ReportVO> groupResults = mongoTemplate.aggregate(agg, NonPaymentDetailsDTO.class,
				ReportVO.class);
		List<ReportVO> gcrtVOList = groupResults.getMappedResults();
		gcrtVOList.stream().forEach(cov ->{
			cov.setCov(getGcrtCov());
		});
		return gcrtVOList;
	}
	
	private void setGcrtCovDesc(List<ReportVO> reportVOlist) {
		reportVOlist.forEach(cov ->{
			if(StringUtils.isNotBlank(cov.getCov())&&cov.getCov().equalsIgnoreCase(getGcrtCov())) {
				cov.setCovDesc(cov.getCovDesc() + " " + cov.getWeightType());
			}
		});
		
	}
	private Page<NonPaymentDetailsDTO> getNonPaymentOfficeWiseDetailsQurey(RegReportVO regReportVO,Pageable pageable,String officeCode){
		if (regReportVO.getPendingQuarter() > 0) {
			LocalDate taxPeriodEnd = getPendingQuarterMonthsDate(regReportVO.getQuarterEndDate(),regReportVO.getPendingQuarter());
			return nonPaymentDetailsDAO.findByOfficeCodeAndTaxValidityEqualsAndCovAndIsScNoIssuedByCcoFalse(officeCode,
					taxPeriodEnd, regReportVO.getCov(), pageable.previousOrFirst());

		} else {
			LocalDate taxPeriodEnd = getPendingMonthsDate(regReportVO.getQuarterEndDate(),regReportVO.getPendingQuarter());
			return nonPaymentDetailsDAO.findByOfficeCodeAndTaxValidityLessThanAndCovAndIsScNoIssuedByCcoFalse(
					officeCode, taxPeriodEnd, regReportVO.getCov(), pageable.previousOrFirst());
		}
	}
	
	private Page<NonPaymentDetailsDTO> getNonPaymentOfficeWiseGcrtDetailsQurey(RegReportVO regReportVO,Pageable pageable,String officeCode,String weightType){
		if (regReportVO.getPendingQuarter() > 0) {
			LocalDate taxPeriodEnd = getPendingQuarterMonthsDate(regReportVO.getQuarterEndDate(),regReportVO.getPendingQuarter());
			return nonPaymentDetailsDAO.findByOfficeCodeAndTaxValidityEqualsAndCovAndWeightTypeAndIsScNoIssuedByCcoFalse(officeCode,
					taxPeriodEnd, regReportVO.getCov(), weightType,pageable.previousOrFirst());

		} else {
			LocalDate taxPeriodEnd = getPendingMonthsDate(regReportVO.getQuarterEndDate(),regReportVO.getPendingQuarter());
			return nonPaymentDetailsDAO.findByOfficeCodeAndTaxValidityLessThanAndCovAndWeightTypeAndIsScNoIssuedByCcoFalse(
					officeCode, taxPeriodEnd, regReportVO.getCov(), weightType,pageable.previousOrFirst());
		}
	}
	
	private String getGcrtCov() {
		return ClassOfVehicleEnum.GCRT.getCovCode();
	}
	
	private List<NonPaymentDetailsDTO> nonPaymentReportForCovExcelListQurey(RegReportVO regReportVO,
			String officeCode) {
		if (regReportVO.getPendingQuarter() > 0) {
			LocalDate taxPeriodEnd = getPendingQuarterMonthsDate(regReportVO.getQuarterEndDate(),regReportVO.getPendingQuarter());
			return nonPaymentDetailsDAO.findByOfficeCodeAndTaxValidityEqualsAndCovAndIsScNoIssuedByCcoFalseNative(officeCode,
					taxPeriodEnd, regReportVO.getCov());

		} else {
			LocalDate taxPeriodEnd = getPendingMonthsDate(regReportVO.getQuarterEndDate(),regReportVO.getPendingQuarter());
			return nonPaymentDetailsDAO.findByOfficeCodeAndTaxValidityLessThanAndCovAndIsScNoIssuedByCcoFalseNative(
					officeCode, taxPeriodEnd, regReportVO.getCov());
		}
	}
	
	private List<NonPaymentDetailsDTO> nonPaymentReportForCovExcelListGcrtQurey(RegReportVO regReportVO,
			String officeCode) {
		if (regReportVO.getPendingQuarter() > 0) {
			LocalDate taxPeriodEnd = getPendingQuarterMonthsDate(regReportVO.getQuarterEndDate(),regReportVO.getPendingQuarter());
			return nonPaymentDetailsDAO.findByOfficeCodeAndTaxValidityEqualsAndCovAndWeightTypeAndIsScNoIssuedByCcoFalseNative(officeCode,
					taxPeriodEnd, regReportVO.getCov(),regReportVO.getWeightType());

		} else {
			LocalDate taxPeriodEnd = getPendingMonthsDate(regReportVO.getQuarterEndDate(),regReportVO.getPendingQuarter());
			return nonPaymentDetailsDAO.findByOfficeCodeAndTaxValidityLessThanAndCovAndWeightTypeAndIsScNoIssuedByCcoFalseNative(
					officeCode, taxPeriodEnd, regReportVO.getCov(), regReportVO.getWeightType());
		}
	}
	
	private void weghitTypeCheck(RegReportVO regReportVO) {
		if (StringUtils.isBlank(regReportVO.getWeightType())) {
			throw new BadRequestException("WeightType is missig");
		}
	}
	
	private Page<NonPaymentDetailsDTO> nonPaymentMandalWiseReportForCovGcrtQurey(RegReportVO regReportVO, String officeCode,
			Pageable pagable) {
		if(regReportVO.getPendingQuarter()>0) {
			LocalDate taxPeriodEnd = getPendingQuarterMonthsDate(regReportVO.getQuarterEndDate(), regReportVO.getPendingQuarter());
			
			return nonPaymentDetailsDAO.findByOfficeCodeAndTaxValidityEqualsAndCovAndMandalNameAndWeightTypeAndIsScNoIssuedByCcoFalse(
					officeCode, taxPeriodEnd, regReportVO.getCov(), regReportVO.getMandalName(),regReportVO.getWeightType(), pagable.previousOrFirst());	
		}else {
			LocalDate taxPeriodEnd = getPendingMonthsDate(regReportVO.getQuarterEndDate(), regReportVO.getPendingQuarter());
			return nonPaymentDetailsDAO.findByOfficeCodeAndTaxValidityLessThanAndCovAndMandalNameAndWeightTypeAndIsScNoIssuedByCcoFalse(
					officeCode, taxPeriodEnd, regReportVO.getCov(), regReportVO.getMandalName(), regReportVO.getWeightType(),pagable.previousOrFirst());	
		}
	}
	
	private Page<NonPaymentDetailsDTO> nonPaymentMandalWiseReportForCovQurey(RegReportVO regReportVO, String officeCode,
			Pageable pagable) {
		if(regReportVO.getPendingQuarter()>0) {
			LocalDate taxPeriodEnd = getPendingQuarterMonthsDate(regReportVO.getQuarterEndDate(), regReportVO.getPendingQuarter());
			
			return nonPaymentDetailsDAO.findByOfficeCodeAndTaxValidityEqualsAndCovAndMandalNameAndIsScNoIssuedByCcoFalse(
					officeCode, taxPeriodEnd, regReportVO.getCov(), regReportVO.getMandalName(), pagable.previousOrFirst());	
		}else {
			LocalDate taxPeriodEnd = getPendingMonthsDate(regReportVO.getQuarterEndDate(), regReportVO.getPendingQuarter());
			return nonPaymentDetailsDAO.findByOfficeCodeAndTaxValidityLessThanAndCovAndMandalNameAndIsScNoIssuedByCcoFalse(
					officeCode, taxPeriodEnd, regReportVO.getCov(), regReportVO.getMandalName(), pagable.previousOrFirst());	
		}
	}
	@Override
	public String setCovWithWeight(String cov,RegReportVO regReportVO) {
		StringBuilder  sb = new StringBuilder();
		sb.append(getCovCodeByCovName().get(cov));
		if(StringUtils.isNotBlank(cov)&&getGcrtCov().equals(cov)) {
			sb.append(" ").append(regReportVO.getWeightType());
			return sb.toString();
		}
		return sb.toString();
	}
	
	private Page<ShowCauseDetailsDTO> showCauseReportForGcrtCovQurey(RegReportVO regReportVO,
			String officeCode, Pageable pagable) {
		if (regReportVO.getPendingQuarter() > 0) {
			LocalDate taxPeriodEnd = getPendingQuarterMonthsDate(regReportVO.getQuarterEndDate(),regReportVO.getPendingQuarter());
			return showCauseDetailsDAO.findByOfficeCodeAndTaxValidityEqualsAndCovAndWeightTypeAndScStatus(officeCode,
					taxPeriodEnd, regReportVO.getCov(),regReportVO.getWeightType(),Status.ShowCauseStatus.CCOISSUED.getStatus(), pagable.previousOrFirst());

		} else {
			LocalDate taxPeriodEnd = getPendingMonthsDate(regReportVO.getQuarterEndDate(),regReportVO.getPendingQuarter());
			return showCauseDetailsDAO.findByOfficeCodeAndTaxValidityLessThanAndCovAndWeightTypeAndScStatus(
					officeCode, taxPeriodEnd, regReportVO.getCov(),regReportVO.getWeightType(),Status.ShowCauseStatus.CCOISSUED.getStatus(), pagable.previousOrFirst());
		}
	}
	
	private Page<ShowCauseDetailsDTO> showCauseReportForCovQurey(RegReportVO regReportVO,
			String officeCode, Pageable pagable) {
		if (regReportVO.getPendingQuarter() > 0) {
			LocalDate taxPeriodEnd = getPendingQuarterMonthsDate(regReportVO.getQuarterEndDate(),regReportVO.getPendingQuarter());
			return showCauseDetailsDAO.findByOfficeCodeAndTaxValidityEqualsAndCovAndScStatus(officeCode,
					taxPeriodEnd, regReportVO.getCov(),Status.ShowCauseStatus.CCOISSUED.getStatus(), pagable.previousOrFirst());
		} else {
			LocalDate taxPeriodEnd = getPendingMonthsDate(regReportVO.getQuarterEndDate(),regReportVO.getPendingQuarter());
			return showCauseDetailsDAO.findByOfficeCodeAndTaxValidityLessThanAndCovAndScStatus(
					officeCode, taxPeriodEnd, regReportVO.getCov(),Status.ShowCauseStatus.CCOISSUED.getStatus(), pagable.previousOrFirst());
		}
	}
	
	private List<String> getCfstRecords(){
		List<String> cfstskiplist = Collections.emptyList();
		List<CfstNonPaymentSkipRecordsDTO> cfstList = cfstNonPaymentSkipRecordsDAO.findAll();
		if(!cfstList.isEmpty()) {
			cfstskiplist = cfstList.stream().map(val -> val.getPrNo()).collect(Collectors.toList());
		}
		return cfstskiplist;
	}
	
	private boolean cfstRecordSkip(List<String> cfstSkipList, RegistrationDetailsDTO regDto) {
		if (StringUtils.isNotEmpty(regDto.getPrNo()) && cfstSkipList.contains(regDto.getPrNo())) {
			return false;
		}
		return true;
	}
}

