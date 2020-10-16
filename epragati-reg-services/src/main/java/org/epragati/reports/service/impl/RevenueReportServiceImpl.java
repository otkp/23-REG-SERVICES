package org.epragati.reports.service.impl;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.map.HashedMap;
import org.epragati.constants.NationalityEnum;
import org.epragati.exception.BadRequestException;
import org.epragati.master.service.DistrictService;
import org.epragati.master.vo.DistrictVO;
import org.epragati.payment.dto.AssemblePaymentsDTO;
import org.epragati.payment.dto.PaymentTransactionDTO;
import org.epragati.payment.report.vo.RegReportVO;
import org.epragati.payments.dao.AssemblePaymentsDAO;
import org.epragati.reports.service.PaymentReportService;
import org.epragati.reports.service.RevenueReportService;
import org.epragati.util.payment.GatewayTypeEnum;
import org.epragati.util.payment.ServiceCodeEnum;
import org.epragati.util.payment.ServiceEnum;
import org.epragati.vcr.service.VcrService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

/**
 * 
 * @author saikiran.kola
 *
 */

@Service
public class RevenueReportServiceImpl implements RevenueReportService {

	private static final Logger logger = LoggerFactory.getLogger(RevenueReportServiceImpl.class);

	@Autowired
	private VcrService vcrService;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	PaymentReportService paymentReportService;

	@Autowired
	private AssemblePaymentsDAO assemblePaymentsDAO;

	@Autowired
	private DistrictService districtService;

	public void generatePaymentBreakups() {

		// paymentTransactionDAO

	}

	public void breakupsGroupByCov() {

	}

	public List<LocalDate> selectedMonths() {

		List<LocalDate> selectedMonths = new ArrayList<>();
		selectedMonths.add(LocalDate.of(2019, 01, 01));
		selectedMonths.add(LocalDate.of(2019, 02, 01));
		selectedMonths.add(LocalDate.of(2019, 03, 01));
		selectedMonths.add(LocalDate.of(2019, 04, 01));
		// selectedMonths.add(LocalDate.of(2019, 05, 01));

		return selectedMonths;
	}

	public Map<Integer, List<String>> getAllDistricts() {

		Map<Integer, List<String>> districtOfficeMap = paymentReportService.getOfficeCodes();

		Map<Integer, List<String>> collectMap = districtOfficeMap.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
		return collectMap;
	}

	@Override
	public void breakupsGroupByOffice(RegReportVO paymentreportVO) {
		getAllDistricts().keySet().stream().forEach(key -> {

			logger.info("District Initiated [{}]", key);
			List<String> officeList = getAllDistricts().get(key);
			List<AssemblePaymentsDTO> districtFee = new ArrayList<AssemblePaymentsDTO>();
			if (CollectionUtils.isNotEmpty(officeList)) {
				officeList.stream().forEach(office -> {
					logger.info("office initiated [{}]", office);
					List<AssemblePaymentsDTO> officeDetails = getBreakupsOfficeWise(office, paymentreportVO);
					districtFee.addAll(officeDetails);
					logger.info("office completed [{}]", office);

				});
			}
			Map<String, Double> sumMap = districtFee.stream().filter(val -> val.getFeesType() != null)
					.collect(Collectors.groupingBy(AssemblePaymentsDTO::getFeesType,
							Collectors.summingDouble(AssemblePaymentsDTO::getAmount)));
			logger.info("District [{}] total breakups [{}] for District [{}]", key, sumMap, key);

		});

	}

	public LocalDateTime getDates(LocalDate date) {
		return vcrService.getTimewithDate(date, false);

	}

	public List<AssemblePaymentsDTO> processPayment(LocalDate date, String officeCode) {
		List<AssemblePaymentsDTO> assemblePaymentsList = new ArrayList<>();
		distinctGateWays().stream().forEach(gateway -> {

			// logger.info("gateway type [{}]", gateway);
			LocalDateTime fromJan = vcrService.getTimewithDate(date, false);
			LocalDateTime toJan = vcrService.getTimewithDate(date, true);
			List<PaymentTransactionDTO> paymentList = getPaymentsMap(fromJan, toJan, officeCode, gateway);
			Map<String, Double> map = new HashedMap<>();
			processBreakUp(paymentList, map);
			/*
			 * if (!paymentList.isEmpty()) {
			 * logger.info("Breakup found for date [{}] gateway [{}] officeCode [{}]", date,
			 * gateway, officeCode); }
			 */
			/*
			 * logger.
			 * info("BreakUp Payments Map [{}] for officeCode [{}] fromDate [{}] toDate [{}]  gateway [{}]"
			 * , map, officeCode, fromJan, toJan, gateway);
			 */
			processFeeDetails(fromJan, toJan, officeCode, map, gateway);
			handleLateFee(fromJan, toJan, officeCode, map, gateway);
			handleSpFee(fromJan, toJan, officeCode, map, gateway);
			handleDeductionMode(fromJan, toJan, officeCode, map, gateway);
			// logger.info("Final Map for officeCode [{}] ,[{}] gateway [{}]", officeCode,
			// map, gateway);

			if (!map.isEmpty()) {
				map.keySet().stream().forEach(key -> {

					saveAssemblePayments(key, map, officeCode, gateway, assemblePaymentsList, date);
				});

			}
			// logger.info("total Break [{}] for office code [{}] date[{}] gateway[{}]",
			// map, officeCode, date, gateway);

		});
		// logger.info("All gatewaysBreakup [{}] for officeCode[{}] for Date [{}]",
		// feeDetailsList, officeCode, date);
		return assemblePaymentsList;
	}

	public void saveAssemblePayments(String key, Map<String, Double> map, String officeCode, Integer gateway,
			List<AssemblePaymentsDTO> assemblePaymentsList, LocalDate date) {

		AssemblePaymentsDTO assembleDTO = new AssemblePaymentsDTO();
		assembleDTO.setFeesType(key);
		assembleDTO.setAmount(map.get(key));
		assembleDTO.setPaymentGateway(gateway);
		assembleDTO.setOfficeCode(officeCode);
		assembleDTO.setDate(date);
		assembleDTO.setCreatedDate(LocalDateTime.now());
		assembleDTO.setSource("group");
		assembleDTO.setType(ServiceEnum.NEWREG.toString());
		assemblePaymentsList.add(assembleDTO);
		assemblePaymentsDAO.save(assembleDTO);

	}

	public List<AssemblePaymentsDTO> getBreakupsOfficeWise(String officeCode, RegReportVO paymentreportVO) {
		if (paymentreportVO.getFromDate() == null || paymentreportVO.getToDate() == null) {
			throw new BadRequestException("fromDate/ToDate is missing");
		}
		logger.info("fromDate [{}]  ToDate [{}]", paymentreportVO.getFromDate(), paymentreportVO.getToDate());
		List<AssemblePaymentsDTO> officeFeeDetails = new ArrayList<>();
		LocalDate fromdate = paymentreportVO.getFromDate();
		LocalDate toDate = paymentreportVO.getToDate();
		// LocalDate date = LocalDate.of(2019, 01, 01);
		// while (date.isBefore(LocalDate.of(2019, 04, 30)) ||
		// date.equals(LocalDate.of(2019, 04, 30))) {

		while (fromdate.isBefore(toDate) || fromdate.equals(toDate)) {

			// logger.info("Date start[{}]", date);
			List<AssemblePaymentsDTO> feeDetailsList = processPayment(fromdate, officeCode);

			if (CollectionUtils.isNotEmpty(feeDetailsList)) {
				officeFeeDetails.addAll(feeDetailsList);
			}
			// logger.info("Date end[{}]", date);
			fromdate = fromdate.plusDays(1);
		}
		// logger.info("office [{}] complete FeeDetails [{}]", officeCode,
		// officeFeeDetails);
		return officeFeeDetails;

	}

	public void processBreakUp(List<PaymentTransactionDTO> paymentList, Map<String, Double> map) {
		if (!paymentList.isEmpty()) {
			List<PaymentTransactionDTO> breakPaymentsList = paymentList.stream()
					.filter(val -> val.getBreakPaymentsSave() != null
							&& !val.getBreakPaymentsSave().getBreakPayments().isEmpty())
					.collect(Collectors.toList());

			breakPaymentsList.stream().forEach(val -> {
				val.getBreakPaymentsSave().getBreakPayments().stream()
						.filter(breakMap -> !breakMap.getBreakup().isEmpty()
								&& !breakMap.getBreakup().keySet().contains(null)
								&& !breakMap.getBreakup().values().contains(null))
						.forEach(breakPay -> {
							Map<String, Double> breakMap = breakPay.getBreakup();
							breakMap.keySet().stream().forEach(feeType -> {
								if (map.containsKey(feeType)) {
									map.put(feeType, map.get(feeType) + breakMap.get(feeType));
								} else {
									map.put(feeType, breakMap.get(feeType));
								}

							});
						});
			});

		}

	}

	public List<Integer> distinctGateWays() {

		return Arrays.asList(GatewayTypeEnum.values()).stream().filter(val -> !val.equals(GatewayTypeEnum.CASH))
				.map(val -> val.getId()).collect(Collectors.toList());
	}

	public List<PaymentTransactionDTO> getPaymentsMap(LocalDateTime fromJan, LocalDateTime toJan, String officeCode,
			Integer gateway) {

		Criteria criteria = new Criteria();
		criteria = Criteria.where("response.responseTime").gte(fromJan).lte(toJan).and("officeCode").is(officeCode)
				.and("breakPaymentsSave.breakPayments.breakup").exists(true).and("payStatus").is("success")
				.and("paymentGatewayType").is(gateway);

		Query query = new Query(criteria);
		query.fields().include("breakPaymentsSave.breakPayments");
		return mongoTemplate.find(query, PaymentTransactionDTO.class);

		/*
		 * codeList.stream().forEach(feeType -> { Aggregation agg =
		 * newAggregation(unwind("BreakPaymentsSave.breakPayments"),
		 * match(Criteria.where("response.responseTime").gte(fromJan).lte(toJan).and(
		 * "officeCode")
		 * .is(officeCode).and("BreakPaymentsSave.breakPayments.breakup").exists(true).
		 * and("payStatus") .is("success")),
		 * group("BreakPaymentsSave.breakPayments.breakup." + feeType)
		 * .sum("BreakPaymentsSave.breakPayments.breakup." + feeType).as("total"),
		 * project("total").and(feeType).previousOperation());
		 * AggregationResults<RegReportVO> groupResults = mongoTemplate.aggregate(agg,
		 * PaymentTransactionDTO.class, RegReportVO.class);
		 * 
		 * breakupOfficeList.addAll(groupResults.getMappedResults()); });
		 */
		// return breakupOfficeList;
	}

	public void processFeeDetails(LocalDateTime fromJan, LocalDateTime toJan, String officeCode,
			Map<String, Double> map, Integer gateway) {

		Aggregation agg = newAggregation(unwind("feeDetailsDTO.feeDetails"),
				match(Criteria.where("response.responseTime").gte(fromJan).lte(toJan).and("serviceIds")
						.nin(Arrays.asList(ServiceEnum.OTHERSTATESPECIALPERMIT.getId(),
								ServiceEnum.OTHERSTATETEMPORARYPERMIT.getId(), ServiceEnum.VCR.getId()))
						.and("officeCode").is(officeCode).and("feeDetailsDTO.feeDetails.feesType").exists(true)
						.and("payStatus").is("success").and("paymentGatewayType").is(gateway)),
				group("feeDetailsDTO.feeDetails.feesType").sum("feeDetailsDTO.feeDetails.amount").as("total"),
				project("total").and("fee").previousOperation());

		AggregationResults<RegReportVO> groupResults = mongoTemplate.aggregate(agg, PaymentTransactionDTO.class,
				RegReportVO.class);
		// logger.info("group result for officeCode [{}] , feeDetails [{}]", officeCode,
		// groupResults.getRawResults());

		List<RegReportVO> feeDetailsList = groupResults.getMappedResults();
		List<RegReportVO> filteredList = feeDetailsList.stream()
				.filter(feeDetails -> feeDetails.getTotal() != null && feeDetails.getTotal() > 0)
				.collect(Collectors.toList());

		if (CollectionUtils.isNotEmpty(filteredList)) {
			filteredList.stream().forEach(fee -> {
				if (map.containsKey(fee.getFee())) {
					map.put(fee.getFee(), map.get(fee.getFee()) + fee.getTotal());
				} else {
					map.put(fee.getFee(), fee.getTotal());
				}

			});
		}
		/*
		 * logger.
		 * info("FeeDetails Map [{}] for officeCode [{}] fromDate [{}] toDate [{}]  gateway [{}]"
		 * , map, officeCode, fromJan, toJan, gateway);
		 */

	}

	public void handleDeductionMode(LocalDateTime fromJan, LocalDateTime toJan, String officeCode,
			Map<String, Double> map, Integer gateway) {
		String deduction = " Deduction";
		Aggregation agg = newAggregation(unwind("feeDetailsDTO.feeDetails"),
				match(Criteria.where("response.responseTime").gte(fromJan).lte(toJan).and("serviceIds")
						.in(Arrays.asList(ServiceEnum.OTHERSTATESPECIALPERMIT.getId(),
								ServiceEnum.OTHERSTATETEMPORARYPERMIT.getId(), ServiceEnum.VCR.getId()))
						.and("officeCode").is(officeCode).and("feeDetailsDTO.feeDetails.feesType").exists(true)
						.and("payStatus").is("success").and("paymentGatewayType").is(gateway)),
				group("feeDetailsDTO.feeDetails.feesType").sum("feeDetailsDTO.feeDetails.amount").as("total"),
				project("total").and("fee").previousOperation());

		AggregationResults<RegReportVO> groupResults = mongoTemplate.aggregate(agg, PaymentTransactionDTO.class,
				RegReportVO.class);
		// logger.info(" Deduction feeDetails [{}] ", groupResults.getRawResults());

		List<RegReportVO> feeDetailsList = groupResults.getMappedResults();

		feeDetailsList.stream().forEach(fee -> {
			if (fee.getTotal() != null && fee.getTotal() > 0) {

				if (map.containsKey(fee.getFee() + deduction)) {
					map.put(fee.getFee() + deduction, map.get(fee.getFee() + deduction) + fee.getTotal());
				} else {
					map.put(fee.getFee() + deduction, fee.getTotal());
				}

			}
		});
	}

	public void handleLateFee(LocalDateTime fromJan, LocalDateTime toJan, String officeCode, Map<String, Double> map,
			Integer gateway) {
		Aggregation agg = newAggregation(unwind("feeDetailsDTO.feeDetails"),
				match(Criteria.where("response.responseTime").gte(fromJan).lte(toJan).and("officeCode").is(officeCode)
						.and("feeDetailsDTO.feeDetails.feesType").is("Late Fee").and("payStatus").is("success")
						.and("paymentGatewayType").is(gateway)),
				group("serviceIds").sum("feeDetailsDTO.feeDetails.amount").as("total"),
				project("total").and("serviceId").previousOperation());
		// Query query = new Query(criteria);
		// query.fields().include("feeDetailsDTO").include("serviceIds");
		AggregationResults<RegReportVO> groupResults = mongoTemplate.aggregate(agg, PaymentTransactionDTO.class,
				RegReportVO.class);
		// logger.info(" Late Fee RAW Result [{}] ", groupResults.getRawResults());

		if (!groupResults.getMappedResults().isEmpty()) {
			// logger.info(" Late Fee Mapped Result [{}] ",
			// groupResults.getMappedResults());

			List<RegReportVO> lateFeeList = groupResults.getMappedResults();
			double renewalLateFee = lateFeeList.stream()
					.filter(fee -> fee.getTotal() != null && fee.getServiceId() != null
							&& fee.getServiceId().contains(ServiceEnum.RENEWAL.getId()))
					.map(val -> val.getTotal()).mapToDouble(Double::doubleValue).sum();
			double renewalPermitLateFee = lateFeeList.stream()
					.filter(fee -> fee.getTotal() != null && fee.getServiceId() != null
							&& fee.getServiceId().contains(ServiceEnum.RENEWALOFPERMIT.getId()))
					.map(val -> val.getTotal()).mapToDouble(Double::doubleValue).sum();
			map.put(ServiceCodeEnum.lateFeeEnum.PERMITRENEWALLATEFEE.getDesc(), renewalPermitLateFee);
			map.put(ServiceCodeEnum.lateFeeEnum.RENEWALLATEFEE.getDesc(), renewalLateFee);

		}

	}

	public void handleSpFee(LocalDateTime fromJan, LocalDateTime toJan, String officeCode, Map<String, Double> map,
			Integer gateway) {

		Aggregation agg = newAggregation(
				match(Criteria.where("response.responseTime").gte(fromJan).lte(toJan).and("officeCode").is(officeCode)
						.and("feeDetailsDTO.feeDetails.feesType").exists(true).and("payStatus").is("success")
						.and("paymentGatewayType").is(gateway).and("moduleCode")
						.in(Arrays.asList(ServiceEnum.SPNR.getCode(), ServiceEnum.SPNB.getCode()))
						.and("payURefundResponse").exists(true).and("payURefundResponse.message")
						.in(Arrays.asList("Refund Initiated", "Refund Initiated by manually"))),

				group("officeCode").sum("feeDetailsDTO.refundAmound").as("total"),
				project("total").and("officeCode").previousOperation());

		AggregationResults<RegReportVO> groupResults = mongoTemplate.aggregate(agg, PaymentTransactionDTO.class,
				RegReportVO.class);

		if (!groupResults.getMappedResults().isEmpty()) {
			logger.info(" SP Application Fee RAW Result [{}] ", groupResults.getRawResults());

			List<RegReportVO> spFeeResult = groupResults.getMappedResults();

			Optional<RegReportVO> spFeeOpt = spFeeResult.stream()
					.filter(sp -> sp.getOfficeCode() != null && sp.getTotal() != null && sp.getTotal() > 0).findFirst();
			if (spFeeOpt.isPresent()) {
				RegReportVO regVO = spFeeOpt.get();
				if (map.get(ServiceEnum.spRefundEnum.SPREFUNDAMOUNT.getDesc()) != null) {
					map.put(ServiceEnum.spRefundEnum.SPREFUNDAMOUNT.getDesc(),
							map.get(ServiceEnum.spRefundEnum.SPREFUNDAMOUNT.getDesc()) + regVO.getTotal());
				} else {
					map.put(ServiceEnum.spRefundEnum.SPREFUNDAMOUNT.getDesc(), regVO.getTotal());
				}
			}
		}
	}

	public Map<String, Double> districtReportDefaults() {
		Map<String, Double> map = new HashMap<>();
		ServiceCodeEnum.districtDefaults().stream().forEach(val -> {
			map.put(val, 0.0);
		});
		return map;

	}

	@Override
	public List<RegReportVO> getPaymentsReportCount(RegReportVO RegReportVO) {
		List<DistrictVO> districtsList = districtService.findBySid(NationalityEnum.AP.getName());
		Set<DistrictVO> districtSet = districtsList.stream().collect(
				Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(DistrictVO::getDistrictName))));
		List<RegReportVO> distTransactionsList = new ArrayList<>();
		districtSet.stream().forEach(dist -> {

			RegReportVO vo = new RegReportVO();
			Map<String, Double> map = districtReportDefaults();
			List<String> officeCodes = paymentReportService.getOfficeCodes().get(dist.getDistrictId());

			LocalDateTime fromDate = paymentReportService.getTimewithDate(RegReportVO.getFromDate(), false);
			LocalDateTime toDate = paymentReportService.getTimewithDate(RegReportVO.getToDate(), true);
			logger.info("District [{}] office Codes [{}]  gateway [{}]", dist, officeCodes,
					paymentReportService.getGatewayTypes(RegReportVO.getGateWayType()));
			Aggregation agg = newAggregation(
					match(Criteria.where("date").gte(fromDate).lte(toDate).and("officeCode").in(officeCodes)
							.and("PaymentGateway")
							.in(paymentReportService.getGatewayTypes(RegReportVO.getGateWayType()))),

					group("feesType").sum("amount").as("total"), project("total").and("fee").previousOperation());

			AggregationResults<RegReportVO> groupResults = mongoTemplate.aggregate(agg, AssemblePaymentsDTO.class,
					RegReportVO.class);
			List<RegReportVO> reportsList = groupResults.getMappedResults();
			logger.info("agg [{}] payments RAW [{}]", agg, groupResults.getRawResults());

			logger.info("payments vo [{}]", groupResults.getMappedResults());

			if (CollectionUtils.isNotEmpty(reportsList)) {
				reportsList.stream().forEach(val -> {
					mapRevenueHeads(val.getFee(), map, val.getTotal());
				});
			}

			logger.info("final map [{}]", map);
			RegReportVO paymentVO = paymentReportService.setReportData(vo, map);
			paymentVO.setDistrictName(dist.getDistrictName());
			distTransactionsList.add(paymentVO);

		});
		distTransactionsList.stream().forEach(vo -> {
			Double totalFee = vo.getFeeReport().stream().filter(fee -> fee.getFee() != null && fee.getFeeType() != null)
					.map(v -> v.getFee()).mapToDouble(Double::doubleValue).sum();
			vo.setTotal(totalFee);
		});

		if (distTransactionsList.stream().allMatch(result -> result.getTotal() == 0)) {
			throw new BadRequestException("No Data Found for Dates fromDate  " + RegReportVO.getFromDate()
					+ " +To Date " + RegReportVO.getToDate());
		}
		return distTransactionsList;
	}

	public void mapRevenueHeads(String feeType, Map<String, Double> map, double amount) {
		if (ServiceCodeEnum.getServiceTypeFee().contains(feeType)) {
			map.put(ServiceCodeEnum.SERVICE_FEE.getCode(), map.get(ServiceCodeEnum.SERVICE_FEE.getCode()) + amount);
		} else if (ServiceCodeEnum.getApplicationFee().contains(feeType)) {
			map.put(ServiceCodeEnum.REGISTRATION.getCode(), map.get(ServiceCodeEnum.REGISTRATION.getCode()) + amount);
		} else if (ServiceCodeEnum.getTaxAmounts().contains(feeType)) {
			map.put(ServiceCodeEnum.ReportEnum.TAXAMOUNTS.getCode(),
					map.get(ServiceCodeEnum.ReportEnum.TAXAMOUNTS.getCode()) + amount);
		}

		else if (ServiceCodeEnum.getLFee().contains(feeType)) {
			map.put(ServiceCodeEnum.LIFE_TAX.getCode(), map.get(ServiceCodeEnum.LIFE_TAX.getCode()) + amount);
		} else if (ServiceCodeEnum.deductionList().contains(feeType)) {
			map.put(ServiceCodeEnum.DEDUCTIONAX.getCode(), map.get(ServiceCodeEnum.DEDUCTIONAX.getCode()) + amount);
		}
		/*
		 * else if (ServiceCodeEnum.cfHeadList().contains(feeType)) {
		 * map.put(ServiceCodeEnum.COMPOUNDING_FEE.getTypeDesc(),
		 * map.get(ServiceCodeEnum.COMPOUNDING_FEE.getTypeDesc()) + amount); } else if
		 * (ServiceCodeEnum.permitFeeHead().contains(feeType)) {
		 * map.put(ServiceCodeEnum.PERMIT_FEE.getTypeDesc(),
		 * map.get(ServiceCodeEnum.PERMIT_FEE.getTypeDesc()) + amount); } else if
		 * (ServiceCodeEnum.PERMIT_SERVICE_FEE.getTypeDesc().equals(feeType)) {
		 * map.put(ServiceCodeEnum.PERMIT_SERVICE_FEE.getTypeDesc(),
		 * map.get(ServiceCodeEnum.PERMIT_SERVICE_FEE.getTypeDesc()) + amount); }
		 */

	}

}
