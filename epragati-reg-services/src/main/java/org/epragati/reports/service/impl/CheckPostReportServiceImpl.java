package org.epragati.reports.service.impl;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.constants.OfficeType;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.MasterCovDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.RegServiceDAO;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dto.MasterCovDTO;
import org.epragati.master.dto.OffenceDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.payment.dto.FeesDTO;
import org.epragati.payment.report.vo.RegReportVO;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.reports.service.CheckPostReportService;
import org.epragati.reports.service.PaymentReportService;
import org.epragati.rta.reports.vo.CheckPostReportsVO;
import org.epragati.rta.reports.vo.CheckpostTotalVO;
import org.epragati.rta.reports.vo.ReportVO;
import org.epragati.rta.reports.vo.VcrCovAndOffenceBasedReportVO;
import org.epragati.tax.vo.TaxTypeEnum.VoluntaryTaxType;
import org.epragati.util.StatusRegistration;
import org.epragati.util.payment.GatewayTypeEnum;
import org.epragati.util.payment.ServiceCodeEnum;
import org.epragati.util.payment.ServiceEnum;
import org.epragati.vcrImage.dao.VcrFinalServiceDAO;
import org.epragati.vcrImage.dao.VoluntaryTaxDAO;
import org.epragati.vcrImage.dto.VcrFinalServiceDTO;
import org.epragati.vcrImage.dto.VoluntaryTaxDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class CheckPostReportServiceImpl implements CheckPostReportService {

	private static final Logger LOGGER = LoggerFactory.getLogger(PaymentReportsServiceImpl.class);

	@Autowired
	private OfficeDAO officeDAO;

	@Autowired
	private PaymentReportService paymentReportService;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private VcrFinalServiceDAO vcrFinalServiceDAO;

	@Autowired
	private VoluntaryTaxDAO voluntaryTaxDAO;
	@Autowired
	private RegServiceDAO permitDdao;

	@Autowired
	private MasterCovDAO masterCovDAO;
	@Autowired
	private UserDAO userDAO;
	
	@Override
	public List<CheckpostTotalVO> checkPostBased(RegReportVO regReportVO) {
		//List<CheckpostTotalVO> totalVo = new ArrayList<>();
		CheckpostTotalVO totalVo = new CheckpostTotalVO();

		if (StringUtils.isNotEmpty(regReportVO.getOfficeCode()) && StringUtils.isEmpty(regReportVO.getMviName())) {
		
			
			Set<String> mviSet = new HashSet<>();
			
			
			
			List<CheckPostReportsVO> vcrCPList = mviBasedVcrAgg(regReportVO.getOfficeCode(), regReportVO);
	
			
			mviSet = vcrCPList.stream().map(vcr -> vcr.getMviName()).collect(Collectors.toSet());
			
			
			
		
	 	List<CheckPostReportsVO> volCPList = mviBasedVoluntaryAgg(regReportVO.getOfficeCode(), regReportVO);
		
		
		mviSet.addAll(volCPList.stream().map(vcr -> vcr.getMviName()).collect(Collectors.toSet()));

			
		List<RegServiceDTO> regSerList = permitAgg(regReportVO, regReportVO.getOfficeCode());
			
			
			Map<String, List<RegServiceDTO>> groupingMap = regSerList.stream()
					.collect(Collectors.groupingBy(RegServiceDTO::getCreatedBy));
			mviSet.addAll(groupingMap.keySet());
			List<CheckPostReportsVO> permitList = new ArrayList<>();
			groupingMap.keySet().stream().forEach(key -> {

				CheckPostReportsVO permitcheckPostVO = new CheckPostReportsVO();
				permitcheckPostVO.setMviName(key);
				setCPPermitFee(groupingMap.get(key), permitcheckPostVO);
				permitList.add(permitcheckPostVO);

			});
			List<CheckPostReportsVO> cpFinalList = cpDetails(regReportVO.getOfficeCode(), mviSet, vcrCPList, volCPList,
					permitList);

			 totalVo = calculateTotals(cpFinalList);
			return Arrays.asList(totalVo);
		}

		else if (StringUtils.isNotEmpty(regReportVO.getMviName())) {
		} else {
			
			List<OfficeDTO> checkPostList = getCheckPostOffice();
			List<String> officeCodesList = checkPostList.stream().map(c -> c.getOfficeCode())
					.collect(Collectors.toList());
			List<CheckPostReportsVO> vcrcheckPostList = vcrAgg(regReportVO, officeCodesList);
			List<CheckPostReportsVO> volcheckPostList = voluntaryAgg(regReportVO, officeCodesList);
			List<CheckPostReportsVO> finalCheckPsotVOList = new ArrayList<>();
			checkPostList.parallelStream().forEach(c -> {
				finalCheckPsotVOList.add(setCheckPostData(vcrcheckPostList, c, volcheckPostList, regReportVO));
			});
			 totalVo = calculateTotals(finalCheckPsotVOList);			
			return Arrays.asList(totalVo);
		}
		return Collections.emptyList();

	}
	
	private CheckpostTotalVO calculateTotals(List<CheckPostReportsVO> finalCheckPsotVOList) {
		CheckpostTotalVO totalVo = new CheckpostTotalVO();
		finalCheckPsotVOList.stream().forEach(x -> {
			totalVo.setTotalVcrCount(totalVo.getTotalVcrCount() + x.getVcrCount());
			totalVo.setTotalCompoundFee(totalVo.getTotalCompoundFee() + x.getCompoundFee());
			totalVo.setTotalVoluntaryTax(totalVo.getTotalVoluntaryTax() + x.getVoluntaryTax());
			totalVo.setTotalVoluntaryCount(totalVo.getTotalVoluntaryCount() + x.getVoluntaryCount());
			totalVo.setTotalPermitTax(totalVo.getTotalPermitTax() + x.getPermitTax());
			totalVo.setTotalPermitCount(totalVo.getTotalPermitCount() + x.getPermitCount());
			totalVo.setTotalPermitFee(totalVo.getTotalPermitFee() + x.getPermitFee());

		});
		totalVo.setCheckPostReportsVO(finalCheckPsotVOList);
		return totalVo;
	}

	public void getCheckPostDetails(RegReportVO regReportVO) {
		LocalDateTime fromDate = paymentReportService.getTimewithDate(regReportVO.getFromDate(), false);
		LocalDateTime toDate = paymentReportService.getTimewithDate(regReportVO.getToDate(), true);
		List<VcrFinalServiceDTO> vcrFinalServiceDTOList = vcrFinalServiceDAO
				.nativeVcrDateOfCheckBetweenAndCreatedByIn(Arrays.asList(regReportVO.getMviName()), fromDate, toDate);
		if (!vcrFinalServiceDTOList.isEmpty()) {

		}
	}

	public void MapVcrDetails(List<VcrFinalServiceDTO> vcrFinalServiceDTOList) {
		vcrFinalServiceDTOList.stream().forEach(vcr -> {
			CheckPostReportsVO cpVO = new CheckPostReportsVO();

		});
	}

	public List<CheckPostReportsVO> cpDetails(String officeCode, Set<String> mviSet, List<CheckPostReportsVO> vcrCPList,
			List<CheckPostReportsVO> volCPList, List<CheckPostReportsVO> permitList) {
		List<CheckPostReportsVO> finalCPList = new ArrayList<>();
		mviSet.stream().forEach(mvi -> {

			CheckPostReportsVO cpVO = new CheckPostReportsVO();
			cpVO.setMviName(mvi);
			cpVO.setOfficeCode(officeCode);
			cpVO.setOfficeName(paymentReportService.getOfficeCodesByOfficeName().get(officeCode));
			Optional<CheckPostReportsVO> vcrCP = vcrCPList.stream().filter(v -> v.getMviName().equals(mvi)).findAny();
			if (vcrCP.isPresent()) {
				CheckPostReportsVO vcr = vcrCP.get();
				cpVO.setCompoundFee(vcr.getCompoundFee());
				cpVO.setVcrCount(vcr.getVcrCount());

			}

			Optional<CheckPostReportsVO> volCP = volCPList.stream().filter(v -> v.getMviName().equals(mvi)).findAny();
			if (volCP.isPresent()) {
				CheckPostReportsVO vol = volCP.get();
				cpVO.setVoluntaryCount(vol.getVoluntaryCount());
				cpVO.setVoluntaryTax(vol.getVoluntaryTax());
			}

			Optional<CheckPostReportsVO> regCP = permitList.stream().filter(v -> v.getMviName().equals(mvi)).findAny();
			if (regCP.isPresent()) {
				CheckPostReportsVO permit = regCP.get();
				cpVO.setPermitCount(permit.getPermitCount());
				cpVO.setPermitFee(permit.getPermitFee());
				cpVO.setPermitTax(permit.getPermitTax());

			}
			finalCPList.add(cpVO);
		});
		return finalCPList;
	}

	public CheckPostReportsVO setCheckPostData(List<CheckPostReportsVO> vcrcheckPostList, OfficeDTO c,
			List<CheckPostReportsVO> volcheckPostList, RegReportVO regReportVO) {

		CheckPostReportsVO checkPostVO = new CheckPostReportsVO();
		checkPostVO.setOfficeCode(c.getOfficeCode());
		String officeName = paymentReportService.getOfficeCodesByOfficeName().get(c.getOfficeCode());
		checkPostVO.setOfficeName(officeName);
		List<RegServiceDTO> regServiceList = permitAgg(regReportVO, c.getOfficeCode());
		setCPPermitFee(regServiceList, checkPostVO);
		Optional<CheckPostReportsVO> vcrVOOptional = vcrcheckPostList.stream()
				.filter(val -> val.getOfficeCode().equals(c.getOfficeCode())).findAny();
		if (vcrVOOptional.isPresent()) {
			CheckPostReportsVO vcrVO = vcrVOOptional.get();
			checkPostVO.setVcrCount(vcrVO.getVcrCount());
			checkPostVO.setCompoundFee(vcrVO.getCompoundFee());

		}
		Optional<CheckPostReportsVO> volVOOptional = volcheckPostList.stream()
				.filter(val -> val.getOfficeCode().equals(c.getOfficeCode())).findAny();
		if (volVOOptional.isPresent()) {
			CheckPostReportsVO voluntaryVO = volVOOptional.get();
			checkPostVO.setVoluntaryCount(voluntaryVO.getVoluntaryCount());
			checkPostVO.setVoluntaryTax(voluntaryVO.getVoluntaryTax());
		}
		return checkPostVO;
	}

	public List<CheckPostReportsVO> vcrAgg(RegReportVO regReportVO, List<String> officeCodes) {
		LocalDateTime fromDate = paymentReportService.getTimewithDate(regReportVO.getFromDate(), false);
		LocalDateTime toDate = paymentReportService.getTimewithDate(regReportVO.getToDate(), true);
		Aggregation agg = newAggregation(
				match(Criteria.where("vcr.dateOfCheck").gte(fromDate).lte(toDate).and("isVcrClosed").is(true)
						.and("officeCode").in(officeCodes)),
				group("officeCode").count().as("vcrCount").sum("offencetotal").as("compoundFee"),
				project("vcrCount", "compoundFee").and("officeCode").previousOperation()

		);
		AggregationResults<CheckPostReportsVO> groupResults = mongoTemplate.aggregate(agg, VcrFinalServiceDTO.class,
				CheckPostReportsVO.class);
		LOGGER.info("check vcr count raw results [{}]", groupResults.getRawResults());
		List<CheckPostReportsVO> result = groupResults.getMappedResults();
		return result;

	}

	public List<RegServiceDTO> permitAgg(RegReportVO regReportVO, String officeCode) {
		LocalDateTime fromDate = paymentReportService.getTimewithDate(regReportVO.getFromDate(), false);
		LocalDateTime toDate = paymentReportService.getTimewithDate(regReportVO.getToDate(), true);

		Criteria criteria1 = Criteria.where("createdDate").gte(fromDate).lte(toDate).and("serviceIds")
				.in(Arrays.asList(164, 165)).and("feeDetails.feeDetails").exists(true).and("applicationStatus")
				.is("APPROVED").and("officeCode").is(officeCode);

		Query query = new Query();
		query.fields().include("officeCode").include("feeDetails.feeDetails").include("createdBy");
		query.addCriteria(criteria1);

		List<RegServiceDTO> reServiceList = mongoTemplate.find(query, RegServiceDTO.class);

		LOGGER.info("check vcr count raw results [{}]", reServiceList.size());

		return reServiceList;

	}

	public void setCPPermitFee(List<RegServiceDTO> reServiceList, CheckPostReportsVO checkPostVO) {

		List<FeesDTO> feeList = reServiceList.parallelStream().map(val -> val.getFeeDetails().getFeeDetails())
				.flatMap(x -> x.stream()).collect(Collectors.toList());

		double permitFee = feeList.stream().filter(fee -> ServiceCodeEnum.permitFeeList().contains(fee.getFeesType()))
				.map(val -> val.getAmount()).mapToDouble(Double::doubleValue).sum();

		double permitTax = feeList.stream().filter(fee -> ServiceCodeEnum.permitTaxList().contains(fee.getFeesType()))
				.map(val -> val.getAmount()).mapToDouble(Double::doubleValue).sum();

		checkPostVO.setPermitCount(reServiceList.size());
		checkPostVO.setPermitFee(permitFee);
		checkPostVO.setPermitTax(permitTax);
	}

	public List<CheckPostReportsVO> voluntaryAgg(RegReportVO regReportVO, List<String> officeCodes) {
		LocalDateTime fromDate = paymentReportService.getTimewithDate(regReportVO.getFromDate(), false);
		LocalDateTime toDate = paymentReportService.getTimewithDate(regReportVO.getToDate(), true);
		Aggregation agg = newAggregation(
				match(Criteria.where("createdDate").gte(fromDate).lte(toDate).and("officeCode").in(officeCodes)),
				group("officeCode").count().as("voluntaryCount").sum("tax").as("voluntaryTax"),
				project("voluntaryCount", "voluntaryTax").and("officeCode").previousOperation()

		);
		AggregationResults<CheckPostReportsVO> groupResults = mongoTemplate.aggregate(agg, VoluntaryTaxDTO.class,
				CheckPostReportsVO.class);
		LOGGER.info("check voluntary count raw results [{}]", groupResults.getRawResults());
		List<CheckPostReportsVO> result = groupResults.getMappedResults();
		return result;
	}

	public List<OfficeDTO> getCheckPostOffice() {
		List<OfficeDTO> officeList = officeDAO.findByTypeInAndIsActive(Arrays.asList(OfficeType.CP.getCode()), true);
		if (CollectionUtils.isEmpty(officeList)) {
			throw new BadRequestException("Check Post Office Not found");
		}

		return officeList;
	}

	public List<CheckPostReportsVO> mviBasedVcrAgg(String officeCode, RegReportVO regReportVO) {
		LocalDateTime fromDate = paymentReportService.getTimewithDate(regReportVO.getFromDate(), false);
		LocalDateTime toDate = paymentReportService.getTimewithDate(regReportVO.getToDate(), true);
		Aggregation agg = newAggregation(
				match(Criteria.where("vcr.dateOfCheck").gte(fromDate).lte(toDate).and("isVcrClosed").is(true)
						.and("officeCode").in(Arrays.asList(officeCode))),
				group("createdBy").count().as("vcrCount").sum("offencetotal").as("compoundFee"),
				project("vcrCount", "compoundFee").and("mviName").previousOperation()

		);
		AggregationResults<CheckPostReportsVO> groupResults = mongoTemplate.aggregate(agg, VcrFinalServiceDTO.class,
				CheckPostReportsVO.class);
		LOGGER.info("check vcr  mVI Based count raw results [{}]", groupResults.getRawResults());
		List<CheckPostReportsVO> result = groupResults.getMappedResults();

		return result;
	}

	public List<CheckPostReportsVO> mviBasedVoluntaryAgg(String officeCode, RegReportVO regReportVO) {
		LocalDateTime fromDate = paymentReportService.getTimewithDate(regReportVO.getFromDate(), false);
		LocalDateTime toDate = paymentReportService.getTimewithDate(regReportVO.getToDate(), true);
		Aggregation agg = newAggregation(
				match(Criteria.where("createdDate").gte(fromDate).lte(toDate).and("officeCode")
						.in(Arrays.asList(officeCode))),
				group("userId").count().as("voluntaryCount").sum("tax").as("voluntaryTax"),
				project("voluntaryCount", "voluntaryTax").and("mviName").previousOperation()

		);
		AggregationResults<CheckPostReportsVO> groupResults = mongoTemplate.aggregate(agg, VoluntaryTaxDTO.class,
				CheckPostReportsVO.class);
		LOGGER.info("check Post MVI Based voluntary count raw results [{}]", groupResults.getRawResults());
		List<CheckPostReportsVO> result = groupResults.getMappedResults();
		return result;
	}

	/**
	 * 3rd page
	 * 
	 * @param permit
	 * @param checkPostVO
	 * @return
	 * @throws Exception
	 * @throws ExecutionException
	 * @throws InterruptedException
	 */
	@Override
	public CheckpostTotalVO report3rdPage(RegReportVO regReportVO, Pageable page) throws Exception {

		CheckPostReportsVO data = new CheckPostReportsVO();
		CheckPostReportsVO total = new CheckPostReportsVO();
		LocalDateTime fromDate = paymentReportService.getTimewithDate(regReportVO.getFromDate(), false);
		LocalDateTime toDate = paymentReportService.getTimewithDate(regReportVO.getToDate(), true);

		data.setPermit(permit(total, fromDate, toDate, regReportVO, page));
		data.setVcr(getVcrData(total, fromDate, toDate, regReportVO, page));
		data.setVoluntary(getVoluntereData(total, fromDate, toDate, regReportVO, page));

		total.setCurrentPage(page.getPageNumber());
		total.setPageSize(page.getPageSize());
		data.setTotalSum(Arrays.asList(total));
		CheckpostTotalVO checkpostTotalVO = new CheckpostTotalVO();
		checkpostTotalVO.setCheckPostReportsVO(Arrays.asList(data));

		return checkpostTotalVO;
	}

	public List<CheckPostReportsVO> getVcrData(CheckPostReportsVO total, LocalDateTime fromDate, LocalDateTime toDate,
			RegReportVO regReportVO, Pageable page) {

		Page<VcrFinalServiceDTO> pageList = vcrFinalServiceDAO
				.findAllByCreatedByAndVcrDateOfCheckBetweenOrderByCreatedDateDesc(regReportVO.getMviName(), fromDate,
						toDate, page.previousOrFirst());

		Double compoundFee = 0.0;
		List<CheckPostReportsVO> listOfVcr = new ArrayList<>();
		if (pageList.hasContent()) {
			for (VcrFinalServiceDTO l : pageList.getContent()) {
				CheckPostReportsVO vo = new CheckPostReportsVO();
				compoundFee = compoundFee + l.getOffencetotal();
				vo.setMviName(l.getCreatedBy());
				vo.setCompoundFee(l.getOffencetotal());
				vo.setAppNo(l.getVcr().getVcrNumber());
				vo.setPrNo(l.getRegistration().getRegNo());

				if (l.getRegistration() != null && l.getRegistration().getClasssOfVehicle() != null
						&& l.getRegistration().getClasssOfVehicle().getCovdescription() != null)
					vo.setCov(l.getRegistration().getClasssOfVehicle().getCovdescription());
				listOfVcr.add(vo);
			}
		} else {
			return Collections.EMPTY_LIST;
		}

		total.setCompoundFee(compoundFee);
		total.setVcrTotalPage(pageList.getTotalPages());
		return listOfVcr;

	}

	public List<CheckPostReportsVO> getVoluntereData(CheckPostReportsVO total, LocalDateTime fromDate,
			LocalDateTime toDate, RegReportVO regReportVO, Pageable page) {
		Double volutereFee = 0.0;

		Page<VoluntaryTaxDTO> listOfVolu = voluntaryTaxDAO
				.findAllByCreatedByAndCreatedDateBetweenOrderByCreatedDateDesc(regReportVO.getMviName(), fromDate,
						toDate, page.previousOrFirst());
		List<CheckPostReportsVO> list = new ArrayList<>();
		if (listOfVolu.hasContent()) {
			for (VoluntaryTaxDTO v : listOfVolu) {
				CheckPostReportsVO vo = new CheckPostReportsVO();
				vo.setVoluntaryTax(v.getTax());
				vo.setAppNo(v.getApplicationNo());
				vo.setPrNo(v.getRegNo());
				vo.setCov(v.getClassOfVehicle());
				volutereFee = volutereFee + v.getTax();
				list.add(vo);
			}
		} else {
			return Collections.EMPTY_LIST;
		}

		total.setVoluntaryTax(volutereFee);
		total.setVoluntaryTotalPage(listOfVolu.getTotalPages());
		return list;

	}

	public List<CheckPostReportsVO> permit(CheckPostReportsVO checkPostVO, LocalDateTime fromDate, LocalDateTime toDate,
			RegReportVO regReportVO, Pageable page) {
		double permitFee = 0.0;
		double permitTax = 0.0;
		Page<RegServiceDTO> permit = permitDdao.findAllByCreatedByAndCreatedDateBetweenOrderByCreatedDateDesc(
				regReportVO.getMviName(), fromDate, toDate, page.previousOrFirst());
		List<CheckPostReportsVO> permitList = new ArrayList<>();
		if (permit.hasContent()) {
			for (RegServiceDTO p : permit.getContent()) {
				CheckPostReportsVO vo = new CheckPostReportsVO();

				setCPPermitFee(Arrays.asList(p), checkPostVO);

				vo.setPermitFee(checkPostVO.getPermitFee());
				vo.setPermitTax(checkPostVO.getPermitTax());
				vo.setAppNo(p.getApplicationNo());
				vo.setPrNo(p.getPrNo());
				if (p.getRegistrationDetails() != null && p.getRegistrationDetails().getVehicleDetails() != null
						&& p.getRegistrationDetails().getVehicleDetails().getVehicleClass() != null)
					vo.setCov(p.getRegistrationDetails().getVehicleDetails().getVehicleClass());
				permitTax = permitTax + checkPostVO.getPermitTax();
				permitFee = permitFee + checkPostVO.getPermitFee();
				permitList.add(vo);
			}
			checkPostVO.setPermitFee(permitFee);
			checkPostVO.setPermitTax(permitTax);
		} else {
			return Collections.EMPTY_LIST;
		}
		checkPostVO.setPermitTotalPage(permit.getTotalPages());
		return permitList;

	}

	@Override
	public List<CheckPostReportsVO> getCheckPostReport(RegReportVO regReportVO) {
		List<OfficeDTO> checkPostList = getCheckPostOffice();
		List<String> officeCodesList = checkPostList.stream().map(c -> c.getOfficeCode())
				.collect(Collectors.toList());
		LocalDateTime fromDate = paymentReportService.getTimewithDate(regReportVO.getFromDate(), false);
		LocalDateTime toDate = paymentReportService.getTimewithDate(regReportVO.getToDate(), true);
		return cpVcrData(officeCodesList,fromDate,toDate, checkPostList);
	}
	
	
	private List<CheckPostReportsVO> cpVcrData(List<String> officeCodes,LocalDateTime fromDate,LocalDateTime toDate,List<OfficeDTO> checkPostList ) {

		List<CheckPostReportsVO> result = new ArrayList<>();
		List<VcrFinalServiceDTO> vcrList = vcrFinalServiceDAO.findFixedAmountsSum(officeCodes, fromDate, toDate);
		List<VoluntaryTaxDTO> listOfVoluntary=voluntaryTaxDAO.findByOfficeCodeAndCreatedDate(officeCodes, fromDate, toDate);
		List<RegServiceDTO>  servicesList = permitDdao.findByCreatedDateGreaterThanEqualAndCreatedDateLessThanEqualAndServiceIdsInAndApplicationStatusAndFeeDetailsIsNotNull
		(fromDate, toDate, Arrays.asList(ServiceEnum.OTHERSTATETEMPORARYPERMIT.getId(),ServiceEnum.OTHERSTATESPECIALPERMIT.getId()), StatusRegistration.APPROVED.getDescription());
		List<String> names = new ArrayList<>();
		if(vcrList!=null && !vcrList.isEmpty()) {
			 names = vcrList.stream().map(VcrFinalServiceDTO::getCreatedBy).collect(Collectors.toList());
		}
		if(listOfVoluntary!=null && !listOfVoluntary.isEmpty()) {
			names.addAll(listOfVoluntary.stream().map(VoluntaryTaxDTO::getUserId).collect(Collectors.toSet())) ;
		}
		if(servicesList!=null && !servicesList.isEmpty()) {
			names.addAll(servicesList.stream().map(RegServiceDTO::getCreatedBy).collect(Collectors.toList()));
		}
		Set<String> set = new HashSet<String>(names);
		List list = new ArrayList(set);
		List<UserDTO>  userDetails = userDAO.nativefindUsers(list);
			for(String office :officeCodes) {
				/*List<UserDTO>  userDetails = userDAO.findByPrimaryRoleNameOrAdditionalRolesNameAndOfficeOfficeCodeNative("MVI", "MVI", office);*/
				CheckPostReportsVO officeReport = new CheckPostReportsVO();
				officeReport.setOfficeCode(office);
				officeReport.setOfficeName(checkPostList.stream().filter(code->code.getOfficeCode().equalsIgnoreCase(office)).collect(Collectors.toList()).stream().findFirst().get().getOfficeName());
				long trotalVcrs=0l;
				double totalPaidCf =0;
				double totalUnPaidCf =0;
				long totalvoluntary=0l;
				double totalvoluntaryTax =0;
				long totalVcrTax=0l;
				long totalServiceFee=0l;
				long totalPermit=0l;
				 double totalPermitFee = 0.0;
				 double totalPermitTax = 0.0;
				List<CheckPostReportsVO> officeWiseVcr = new ArrayList<>();
				for(UserDTO user : userDetails) {
					List<CheckPostReportsVO> mviWiseVcr = new ArrayList<>();
					List<CheckPostReportsVO> mviWiseVoluntary = new ArrayList<>();
					List<CheckPostReportsVO> mviWisePermit = new ArrayList<>();
					CheckPostReportsVO mviVo = new CheckPostReportsVO();
					long vcrCount=0l;
					long voluntaryCount=0l;
					double paidCf =0;
					double unPaidCf =0;
					double voluntaryTax =0;
					boolean getingTheData = false;
					long vcrTax=0l;
					long serviceFee=0l;
					long permitCount=0l;
					double permitFee = 0.0;
					 double permitTax = 0.0;
					/*long total=0l;
					long paidCfTotal=0l;
					long unPaidCfTotal=0l;*/
					
					
					if(vcrList!=null && !vcrList.isEmpty()) {
						List<VcrFinalServiceDTO> userVcrList = vcrList.stream().filter(one->one.getCreatedBy().equalsIgnoreCase(user.getUserId())&&
								one.getOfficeCode().equalsIgnoreCase(office))
								.collect(Collectors.toList());
						if(userVcrList!=null && !userVcrList.isEmpty()) {
							getingTheData=true;
							vcrCount = userVcrList.size();
							for(VcrFinalServiceDTO singleVcr : userVcrList) {
								long singleVcrTax=0l;
								CheckPostReportsVO vcr = new CheckPostReportsVO();
								vcr.setAppNo(singleVcr.getVcr().getVcrNumber());
								if(StringUtils.isNoneBlank(singleVcr.getRegistration().getRegNo())) {
									vcr.setPrNo(singleVcr.getRegistration().getRegNo());
								}else if(StringUtils.isNoneBlank(singleVcr.getRegistration().getTrNo())) {
									vcr.setPrNo(singleVcr.getRegistration().getTrNo());
								}else if(StringUtils.isNoneBlank(singleVcr.getRegistration().getChassisNumber())) {
									vcr.setPrNo(singleVcr.getRegistration().getChassisNumber());
								}
								vcr.setCovDescription(singleVcr.getRegistration().getClasssOfVehicle().getCovdescription());
								vcr.setCov(singleVcr.getRegistration().getClasssOfVehicle().getCovcode());
								if((singleVcr.getDeductionMode()!=null && singleVcr.getDeductionMode())||singleVcr.getRegistration().isOtherState()) {
									if (singleVcr.getTax() != null) {
										singleVcrTax = singleVcrTax+singleVcr.getTax().longValue();
										//vo.setTax(singleVcr.getTax());
									}
									if (singleVcr.getPenalty() != null) {
										singleVcrTax = singleVcrTax+singleVcr.getPenalty();
									}
									if (singleVcr.getServiceFee() != null) {
										vcr.setServiceFee(singleVcr.getServiceFee());
										serviceFee =serviceFee+singleVcr.getServiceFee();
									}
									if (singleVcr.getPenaltyArrears() != null) {
										singleVcrTax = singleVcrTax+singleVcr.getPenaltyArrears();
									}
									if (singleVcr.getTaxArrears() != null) {
										singleVcrTax = singleVcrTax+singleVcr.getTaxArrears().longValue();
									}
									}
								vcr.setVcrTax(singleVcrTax);
								vcrTax = vcrTax+singleVcrTax;
								if (singleVcr.getOffencetotal() != null) {
									if((singleVcr.getIsVcrClosed()!=null && singleVcr.getIsVcrClosed())||(singleVcr.getPaymentType()!=null && 
											(singleVcr.getPaymentType().equalsIgnoreCase(GatewayTypeEnum.CASH.getDescription())||singleVcr.getPaymentType().equalsIgnoreCase(GatewayTypeEnum.CFMS.getDescription())))) {
										vcr.setCompoundFee(singleVcr.getOffencetotal());
										paidCf =paidCf+vcr.getCompoundFee();
									}else {
										vcr.setCompoundFee(singleVcr.getOffencetotal());
										unPaidCf =unPaidCf+vcr.getCompoundFee();
									}
								}
								mviWiseVcr.add(vcr);
							}
						}
					}
					
					if(listOfVoluntary!=null && !listOfVoluntary.isEmpty()) {
						List<VoluntaryTaxDTO> userVoluntaryList = listOfVoluntary.stream().filter(one->one.getUserId().equalsIgnoreCase(user.getUserId())
								&&one.getOfficeCode().equalsIgnoreCase(office))
								.collect(Collectors.toList());
						if(userVoluntaryList!=null && !userVoluntaryList.isEmpty()) {
							getingTheData=true;
							voluntaryCount = userVoluntaryList.size();
							for(VoluntaryTaxDTO Voluntary : userVoluntaryList) {
								CheckPostReportsVO vcr = new CheckPostReportsVO();
								vcr.setAppNo(Voluntary.getApplicationNo());
								if(StringUtils.isNoneBlank(Voluntary.getRegNo())) {
									vcr.setPrNo(Voluntary.getRegNo());
								}else if(StringUtils.isNoneBlank(Voluntary.getTrNo())) {
									vcr.setPrNo(Voluntary.getTrNo());
								}else if(StringUtils.isNoneBlank(Voluntary.getChassisNo())) {
									vcr.setPrNo(Voluntary.getChassisNo());
								}
								MasterCovDTO covDTO = masterCovDAO.findByCovcode(Voluntary.getClassOfVehicle());
								if (covDTO != null) {
									vcr.setCovDescription(covDTO.getCovdescription());
								} else {
									vcr.setCovDescription(Voluntary.getClassOfVehicle());
								}
								vcr.setCov(Voluntary.getClassOfVehicle());
								if(Voluntary.getTax()!=null) {
									voluntaryTax = voluntaryTax+Voluntary.getTax();
								}
								if(Voluntary.getPenalty()!=null) {
									voluntaryTax = voluntaryTax+Voluntary.getPenalty();
								}
								if(Voluntary.getTaxArrears()!=null) {
									voluntaryTax = voluntaryTax+Voluntary.getTaxArrears();
								}
								if(Voluntary.getPenaltyArrears()!=null) {
									voluntaryTax = voluntaryTax+Voluntary.getPenaltyArrears();
								}
								vcr.setVoluntaryTax(voluntaryTax);
								mviWiseVoluntary.add(vcr);
							}
						}
					}
					
					if(servicesList!=null && !servicesList.isEmpty()) {
						List<RegServiceDTO> serviceList = servicesList.stream().filter(one->one.getCreatedBy().equalsIgnoreCase(user.getUserId())
								&&one.getOfficeCode().equalsIgnoreCase(office))
								.collect(Collectors.toList());
						if(serviceList!=null && !serviceList.isEmpty()) {
							getingTheData=true;
							permitCount = serviceList.size();
							for(RegServiceDTO service : serviceList) {
								CheckPostReportsVO vcr = new CheckPostReportsVO();
								vcr.setAppNo(service.getOtherStateTemporaryPermitDetails().getPrimaryPermitDetails().getPermitNo());
									vcr.setPrNo(service.getPrNo());
								MasterCovDTO covDTO = masterCovDAO.findByCovcode(service.getOtherStateTemporaryPermitDetails().getVehicleDetails().getClassOfVehicle());
								if (covDTO != null) {
									vcr.setCovDescription(covDTO.getCovdescription());
								} else {
									vcr.setCovDescription(service.getOtherStateTemporaryPermitDetails().getVehicleDetails().getClassOfVehicle());
								}
								vcr.setCov(service.getOtherStateTemporaryPermitDetails().getVehicleDetails().getClassOfVehicle());
								
								for(FeesDTO one : service.getOtherStateTemporaryPermitDetails().getFeeDetails().getFeeDetails()){
									if(one.getFeesType().equalsIgnoreCase(VoluntaryTaxType.Quarterly.getTaxType())||
											one.getFeesType().equalsIgnoreCase(VoluntaryTaxType.Halfyearly.getTaxType())||
											one.getFeesType().equalsIgnoreCase(VoluntaryTaxType.Annual.getTaxType())||
											one.getFeesType().equalsIgnoreCase(VoluntaryTaxType.SevenDays.getTaxType())||
											one.getFeesType().equalsIgnoreCase(VoluntaryTaxType.ThirtyDays.getTaxType())||
											one.getFeesType().equalsIgnoreCase(VoluntaryTaxType.BorderTax.getTaxType())||
											one.getFeesType().equalsIgnoreCase(VoluntaryTaxType.LifeTax.getTaxType())||
											one.getFeesType().equalsIgnoreCase(VoluntaryTaxType.OneYear.getTaxType())) {
										vcr.setPermitTax(one.getAmount());
										permitTax  = permitTax+one.getAmount();
									}
									if(one.getFeesType().equalsIgnoreCase(ServiceCodeEnum.PERMIT_FEE.getTypeDesc())) {
										vcr.setPermitFee(one.getAmount());
										permitFee  = permitFee+one.getAmount();
									}
									if(one.getFeesType().equalsIgnoreCase(ServiceCodeEnum.PERMIT_SERVICE_FEE.getTypeDesc())) {
										vcr.setServiceFee(one.getAmount().longValue());
										serviceFee  = serviceFee+one.getAmount().longValue();
									}
									if(one.getFeesType().equalsIgnoreCase(ServiceCodeEnum.TAXSERVICEFEE.getTypeDesc())) {
										vcr.setServiceFee((long) (vcr.getServiceFee()+one.getAmount()));
										serviceFee  = serviceFee+one.getAmount().longValue();
									}
									
								}
								
							
								vcr.setPermitTax(permitTax);
								vcr.setPermitFee(permitFee);
								vcr.setServiceFee(serviceFee);
								mviWisePermit.add(vcr);
							}
						}
					}
					
					
					trotalVcrs=trotalVcrs+vcrCount;
					totalPaidCf=totalPaidCf+paidCf;
					totalUnPaidCf = totalUnPaidCf+unPaidCf;
					totalVcrTax = totalVcrTax+vcrTax;
					totalServiceFee = totalServiceFee+serviceFee;
					totalvoluntary = totalvoluntary+voluntaryCount;
					totalvoluntaryTax = totalvoluntaryTax+voluntaryTax;
					totalPermit = totalPermit+permitCount;
					totalPermitFee = totalPermitFee+permitFee;
					totalPermitTax = totalPermitTax+permitTax;
					if(getingTheData) {
					mviVo.setMviName(this.getMviName(user));
					mviVo.setUserId(user.getUserId());
					mviVo.setVcr(mviWiseVcr);
					mviVo.setVoluntary(mviWiseVoluntary);
					mviVo.setPermit(mviWisePermit);
					mviVo.setVcrCount(vcrCount);
					mviVo.setPaidCf(paidCf);
					mviVo.setUnPaidCf(unPaidCf);
					mviVo.setVoluntaryCount(voluntaryCount);
					mviVo.setVoluntaryTax(voluntaryTax);
					mviVo.setPermitCount(permitCount);
					mviVo.setPermitFee(permitFee);
					mviVo.setPermitTax(permitTax);
					officeWiseVcr.add(mviVo);
					}
				}
				
					List<UserDTO>  masterOffices = userDAO.findByUserIdAndOfficeCode(names, office);
					if(masterOffices!=null && !masterOffices.isEmpty()) {
						masterOffices.forEach(id->{
							CheckPostReportsVO mviVo = new CheckPostReportsVO();
							mviVo.setMviName(this.getMviName(id));
							mviVo.setUserId(id.getUserId());
							//mviVo.setVcr(mviWiseVcr);
							mviVo.setVcrCount(0);
							mviVo.setPaidCf(0);
							mviVo.setUnPaidCf(0);
							mviVo.setVoluntaryCount(0);
							officeWiseVcr.add(mviVo);
						});
					}
				officeReport.setVcrCount(trotalVcrs);
				officeReport.setPaidCf(totalPaidCf);
				officeReport.setUnPaidCf(totalUnPaidCf);
				officeReport.setTotalSum(officeWiseVcr);
				officeReport.setVoluntaryCount(totalvoluntary);
				officeReport.setVoluntaryTax(totalvoluntaryTax);
				officeReport.setVcrTax(totalVcrTax);
				officeReport.setServiceFee(totalServiceFee);
				officeReport.setPermitCount(totalPermit);
				officeReport.setPermitFee(totalPermitFee);
				officeReport.setPermitTax(totalPermitTax);
				result.add(officeReport);
			}
			
		

		return result;
	}

	
	private String getMviName(UserDTO userData) {
		String firstName=userData.getFirstName()!=null?userData.getFirstName():"";
		String lastName=userData.getLastName()!=null?userData.getLastName():"";
		String finalName=userData.getUserId()+":"+firstName+" "+lastName;
		return finalName;
	}
	
	/**
	 * 
	 */
	@Override
	public VcrCovAndOffenceBasedReportVO covWisevcrReport(RegReportVO regReportVO) {
		VcrCovAndOffenceBasedReportVO resultVo = new VcrCovAndOffenceBasedReportVO();
		if (CollectionUtils.isNotEmpty(regReportVO.getOfficeCodes()) && StringUtils.isBlank(regReportVO.getCov())) {
			Set<String> covSet = new HashSet<>();
			List<CheckPostReportsVO> covBasedList = covBasedAggregation(regReportVO.getOfficeCodes(), regReportVO);
			covSet = covBasedList.stream().map(vcr -> vcr.getCov()).collect(Collectors.toSet());
			List<CheckPostReportsVO> cpFinalList = vcrDetailsForCovReport(covBasedList, covSet, "cov",
					StringUtils.EMPTY);
			resultVo.setCovBasedList(cpFinalList);

		} else if (CollectionUtils.isNotEmpty(regReportVO.getOfficeCodes())
				&& StringUtils.isNoneBlank(regReportVO.getCov())) {
			Set<String> mviSet = new HashSet<>();
			List<CheckPostReportsVO> mviBasedList = covBasedMviCountAggregation(regReportVO.getOfficeCodes(),
					regReportVO);
			mviSet = mviBasedList.stream().map(vcr -> vcr.getMviName()).collect(Collectors.toSet());
			List<CheckPostReportsVO> cpFinalList = vcrDetailsForCovReport(mviBasedList, mviSet, "mvi",
					regReportVO.getCov());
			resultVo.setMviCovList(cpFinalList);
		}

		else {
			List<OfficeDTO> officesList = officeDAO.findBydistrict(regReportVO.getDistrictId());
			List<String> officeCodesList = officesList.stream().map(c -> c.getOfficeCode())
					.collect(Collectors.toList());
			List<CheckPostReportsVO> vcrcheckPostList = covWiseVCRAgg(regReportVO, officeCodesList);
			List<CheckPostReportsVO> finalCheckPsotVOList = new ArrayList<>();
			officesList.parallelStream().forEach(office -> {
				finalCheckPsotVOList.add(setVCRDataByOfficeWise(vcrcheckPostList, office, regReportVO));
			});
			resultVo.setCovBasedList(finalCheckPsotVOList);
		}
		return resultVo;
	}
	
	@Override
	public VcrCovAndOffenceBasedReportVO covWiseReportForEvcr(RegReportVO regReportVO) {
		VcrCovAndOffenceBasedReportVO resultVo = new VcrCovAndOffenceBasedReportVO();
		if (CollectionUtils.isNotEmpty(regReportVO.getOfficeCodes()) && StringUtils.isBlank(regReportVO.getCov())) {
			Set<String> covSet = new HashSet<>();
			List<CheckPostReportsVO> covBasedList = covBasedAggregationForEvcr(regReportVO.getOfficeCodes(), regReportVO);
			covSet = covBasedList.stream().map(vcr -> vcr.getCov()).collect(Collectors.toSet());
			List<CheckPostReportsVO> cpFinalList = vcrDetailsForCovReport(covBasedList, covSet, "cov",
					StringUtils.EMPTY);
			resultVo.setCovBasedList(cpFinalList);

		} else if (CollectionUtils.isNotEmpty(regReportVO.getOfficeCodes())
				&& StringUtils.isNoneBlank(regReportVO.getCov())) {
			Set<String> mviSet = new HashSet<>();
			List<CheckPostReportsVO> mviBasedList = covBasedMviCountAggregationForEvcr(regReportVO.getOfficeCodes(),
					regReportVO);
			mviSet = mviBasedList.stream().map(vcr -> vcr.getMviName()).collect(Collectors.toSet());
			List<CheckPostReportsVO> cpFinalList = vcrDetailsForCovReport(mviBasedList, mviSet, "mvi",
					regReportVO.getCov());
			resultVo.setMviCovList(cpFinalList);
		}

		else {
			List<OfficeDTO> officesList = officeDAO.findBydistrict(regReportVO.getDistrictId());
			List<String> officeCodesList = officesList.stream().map(c -> c.getOfficeCode())
					.collect(Collectors.toList());
			List<CheckPostReportsVO> vcrcheckPostList = covWiseAggForEvcr(regReportVO, officeCodesList);
			List<CheckPostReportsVO> finalCheckPsotVOList = new ArrayList<>();
			officesList.parallelStream().forEach(office -> {
				finalCheckPsotVOList.add(setVCRDataByOfficeWise(vcrcheckPostList, office, regReportVO));
			});
			resultVo.setCovBasedList(finalCheckPsotVOList);
		}
		return resultVo;
	}

	private List<CheckPostReportsVO> covBasedMviCountAggregation(List<String> officeCodes, RegReportVO regReportVO) {

		LocalDateTime fromDate = paymentReportService.getTimewithDate(regReportVO.getFromDate(), false);
		LocalDateTime toDate = paymentReportService.getTimewithDate(regReportVO.getToDate(), true);
		Aggregation agg = newAggregation(
				match(Criteria.where("vcr.dateOfCheck").gte(fromDate).lte(toDate)
						.and("registration.classsOfVehicle.covcode").is(regReportVO.getCov()).and("officeCode")
						.in(officeCodes)),
				group("createdBy").count().as("vcrCount"), project("vcrCount").and("mviName").previousOperation()

		);
		AggregationResults<CheckPostReportsVO> groupResults = mongoTemplate.aggregate(agg, VcrFinalServiceDTO.class,
				CheckPostReportsVO.class);
		LOGGER.info("check vcr  mVI Based count raw results [{}]", groupResults.getRawResults());
		List<CheckPostReportsVO> result = groupResults.getMappedResults();

		return result;
	}
	
	private List<CheckPostReportsVO> covBasedMviCountAggregationForEvcr(List<String> officeCodes, RegReportVO regReportVO) {

		LocalDateTime fromDate = paymentReportService.getTimewithDate(regReportVO.getFromDate(), false);
		LocalDateTime toDate = paymentReportService.getTimewithDate(regReportVO.getToDate(), true);
		Aggregation agg = newAggregation(
				match(Criteria.where("vcr.dateOfCheck").gte(fromDate).lte(toDate)
						.and("registration.classsOfVehicle.covcode").is(regReportVO.getCov())
						.and("iseVcr").is(true).and("officeCode")
						.in(officeCodes)),
				group("createdBy").count().as("vcrCount"), project("vcrCount").and("mviName").previousOperation()

		);
		AggregationResults<CheckPostReportsVO> groupResults = mongoTemplate.aggregate(agg, VcrFinalServiceDTO.class,
				CheckPostReportsVO.class);
		LOGGER.info("check vcr  mVI Based count raw results [{}]", groupResults.getRawResults());
		List<CheckPostReportsVO> result = groupResults.getMappedResults();

		return result;
	}

	/**
	 * 
	 * @param officeCodes
	 * @param regReportVO
	 * @return
	 */
	private List<CheckPostReportsVO> covBasedAggregation(List<String> officeCodes, RegReportVO regReportVO) {

		LocalDateTime fromDate = paymentReportService.getTimewithDate(regReportVO.getFromDate(), false);
		LocalDateTime toDate = paymentReportService.getTimewithDate(regReportVO.getToDate(), true);
		Aggregation agg = newAggregation(
				match(Criteria.where("vcr.dateOfCheck").gte(fromDate).lte(toDate).and("officeCode").in(officeCodes)),
				group("registration.classsOfVehicle.covcode").count().as("vcrCount"),
				project("vcrCount").and("cov").previousOperation()

		);
		AggregationResults<CheckPostReportsVO> groupResults = mongoTemplate.aggregate(agg, VcrFinalServiceDTO.class,
				CheckPostReportsVO.class);
		LOGGER.info("check vcr  mVI Based count raw results [{}]", groupResults.getRawResults());
		List<CheckPostReportsVO> result = groupResults.getMappedResults();

		return result;
	}
	
	private List<CheckPostReportsVO> covBasedAggregationForEvcr(List<String> officeCodes, RegReportVO regReportVO) {

		LocalDateTime fromDate = paymentReportService.getTimewithDate(regReportVO.getFromDate(), false);
		LocalDateTime toDate = paymentReportService.getTimewithDate(regReportVO.getToDate(), true);
		Aggregation agg = newAggregation(
				match(Criteria.where("vcr.dateOfCheck").gte(fromDate).lte(toDate).and("iseVcr").is(true).and("officeCode").in(officeCodes)),
				group("registration.classsOfVehicle.covcode").count().as("vcrCount"),
				project("vcrCount").and("cov").previousOperation()

		);
		AggregationResults<CheckPostReportsVO> groupResults = mongoTemplate.aggregate(agg, VcrFinalServiceDTO.class,
				CheckPostReportsVO.class);
		LOGGER.info("check vcr  mVI Based count raw results [{}]", groupResults.getRawResults());
		List<CheckPostReportsVO> result = groupResults.getMappedResults();

		return result;
	}

	/**
	 * 
	 * @param vcrcheckPostList
	 * @param officeDTO
	 * @param regReportVO
	 * @return
	 */
	public CheckPostReportsVO setVCRDataByOfficeWise(List<CheckPostReportsVO> vcrcheckPostList, OfficeDTO officeDTO,
			RegReportVO regReportVO) {
		CheckPostReportsVO checkPostVO = new CheckPostReportsVO();
		checkPostVO.setOfficeCode(officeDTO.getOfficeCode());
		String officeName = paymentReportService.getOfficeCodesByOfficeName().get(officeDTO.getOfficeCode());
		checkPostVO.setOfficeName(officeName);

		Optional<CheckPostReportsVO> vcrVOOptional = vcrcheckPostList.stream()
				.filter(val -> val.getOfficeCode().equals(officeDTO.getOfficeCode())).findAny();
		if (vcrVOOptional.isPresent()) {
			CheckPostReportsVO vcrVO = vcrVOOptional.get();
			checkPostVO.setVcrCount(vcrVO.getVcrCount());
		}
		return checkPostVO;
	}

	/**
	 * 
	 * @param regReportVO
	 * @param officeCodes
	 * @return
	 */
	public List<CheckPostReportsVO> covWiseVCRAgg(RegReportVO regReportVO, List<String> officeCodes) {
		LocalDateTime fromDate = paymentReportService.getTimewithDate(regReportVO.getFromDate(), false);
		LocalDateTime toDate = paymentReportService.getTimewithDate(regReportVO.getToDate(), true);
		Aggregation agg = newAggregation(
				match(Criteria.where("vcr.dateOfCheck").gte(fromDate).lte(toDate).and("officeCode").in(officeCodes)),
				group("officeCode").count().as("vcrCount").sum("offencetotal").as("compoundFee"),
				project("vcrCount", "compoundFee").and("officeCode").previousOperation()

		);
		AggregationResults<CheckPostReportsVO> groupResults = mongoTemplate.aggregate(agg, VcrFinalServiceDTO.class,
				CheckPostReportsVO.class);
		LOGGER.info("check vcr count raw results [{}]", groupResults.getRawResults());
		List<CheckPostReportsVO> result = groupResults.getMappedResults();
		return result;

	}
	
	public List<CheckPostReportsVO> covWiseAggForEvcr(RegReportVO regReportVO, List<String> officeCodes) {
		LocalDateTime fromDate = paymentReportService.getTimewithDate(regReportVO.getFromDate(), false);
		LocalDateTime toDate = paymentReportService.getTimewithDate(regReportVO.getToDate(), true);
		Aggregation agg = newAggregation(
				match(Criteria.where("vcr.dateOfCheck").gte(fromDate).lte(toDate).and("iseVcr").is(true).and("officeCode").in(officeCodes)),
				group("officeCode").count().as("vcrCount").sum("offencetotal").as("compoundFee"),
				project("vcrCount", "compoundFee").and("officeCode").previousOperation()

		);
		AggregationResults<CheckPostReportsVO> groupResults = mongoTemplate.aggregate(agg, VcrFinalServiceDTO.class,
				CheckPostReportsVO.class);
		LOGGER.info("check vcr count raw results [{}]", groupResults.getRawResults());
		List<CheckPostReportsVO> result = groupResults.getMappedResults();
		return result;

	}

	/**
	 * 
	 * @param officeCode
	 * @param mviSet
	 * @param vcrCPList
	 * @return
	 */
	private List<CheckPostReportsVO> vcrDetailsForCovReport(List<CheckPostReportsVO> covBasedList, Set<String> covSet,
			String type, String classOfVehicle) {
		List<CheckPostReportsVO> finalCPList = new ArrayList<>();
		if (type.equalsIgnoreCase("cov")) {
			covSet.stream().forEach(cov -> {
				CheckPostReportsVO cpVO = new CheckPostReportsVO();
				cpVO.setCov(cov);
				MasterCovDTO covDTO = masterCovDAO.findByCovcode(cov);
				if (covDTO != null) {
					cpVO.setCovDescription(covDTO.getCovdescription());
				} else {
					cpVO.setCovDescription(cov);
				}
				Optional<CheckPostReportsVO> vcrCP = covBasedList.stream().filter(v -> v.getCov().equals(cov))
						.findAny();
				if (vcrCP.isPresent()) {
					CheckPostReportsVO vcr = vcrCP.get();
					cpVO.setCompoundFee(vcr.getCompoundFee());
					cpVO.setVcrCount(vcr.getVcrCount());
					finalCPList.add(cpVO);
				}
			});
		} else if(type.equalsIgnoreCase("mvi")){
			covSet.stream().forEach(mvi -> {
				CheckPostReportsVO cpVO = new CheckPostReportsVO();
				cpVO.setMviName(getMViFullName(mvi));
				cpVO.setCov(classOfVehicle);
				MasterCovDTO covDTO = masterCovDAO.findByCovcode(classOfVehicle);
				if (covDTO != null) {
					cpVO.setCovDescription(covDTO.getCovdescription());
				} else {
					cpVO.setCovDescription(classOfVehicle);
				}
				Optional<CheckPostReportsVO> vcrCP = covBasedList.stream().filter(v -> v.getMviName().equals(mvi))
						.findAny();
				if (vcrCP.isPresent()) {
					CheckPostReportsVO vcr = vcrCP.get();
					cpVO.setVcrCount(vcr.getVcrCount());
					finalCPList.add(cpVO);
				}
			});
		}else if(type.equalsIgnoreCase("offence")) {

			covSet.stream().forEach(mvi -> {
				CheckPostReportsVO cpVO = new CheckPostReportsVO();
				cpVO.setMviName(getMViFullName(mvi));
				cpVO.setOffenceName(classOfVehicle);
				Optional<CheckPostReportsVO> vcrCP = covBasedList.stream().filter(v -> v.getMviName().equals(mvi))
						.findAny();
				if (vcrCP.isPresent()) {
					CheckPostReportsVO vcr = vcrCP.get();
					cpVO.setVcrCount(vcr.getVcrCount());
					finalCPList.add(cpVO);
				}
			});
		
		}
		finalCPList.stream().sorted(Comparator.comparing(CheckPostReportsVO::getVcrCount).reversed())
				.collect(Collectors.toList());
		List<CheckPostReportsVO> finalSortedList  = setTotals(finalCPList);
		return finalSortedList;
	}

	private String getMViFullName(String mvi) {
		Optional<UserDTO> user = userDAO.findByUserId(mvi);
		StringBuilder sb = new StringBuilder();
		if (user.isPresent()) {
			UserDTO userDTO = user.get();
			if (StringUtils.isNotBlank(userDTO.getFirstName())) {
				sb.append(userDTO.getFirstName()).append(StringUtils.SPACE);
			}
			if (StringUtils.isNotBlank(userDTO.getMiddleName())) {
				sb.append(userDTO.getMiddleName()).append(StringUtils.SPACE);
			}
			if (StringUtils.isNotBlank(userDTO.getLastName())) {
				sb.append(userDTO.getLastName()).append(StringUtils.SPACE);
			}
		} else {
			sb.append(mvi);
		}
		if (StringUtils.isBlank(sb.toString())) {
			sb.append(mvi);
		}
		return sb.toString();
	}

	@Override
	public VcrCovAndOffenceBasedReportVO offenseWisevcrReport(RegReportVO valVO) {

		VcrCovAndOffenceBasedReportVO regReportVO = new VcrCovAndOffenceBasedReportVO();
		List<ReportVO> offenceReportList = new ArrayList<>();
		if (StringUtils.isBlank(valVO.getOffenceName())) {
			LocalDateTime fromDate = paymentReportService.getTimewithDate(valVO.getFromDate(), false);
			LocalDateTime toDate = paymentReportService.getTimewithDate(valVO.getToDate(), true);
			List<String> officeCodeList = valVO.getOfficeCodes();

			List<VcrFinalServiceDTO> vcrList = vcrFinalServiceDAO.findFixedAmountsSum(officeCodeList, fromDate, toDate);
			LOGGER.info("offence Enforcement report  for officeCode [{}]", officeCodeList);

			List<OffenceDTO> offenceList = vcrList.stream().map(map -> map.getOffence().getOffence())
					.flatMap(x -> x.stream()).collect(Collectors.toList());

			offenceList.stream().forEach(val -> {
				if (val.getFixedAmount() == null) {
					val.setFixedAmount(0);
				}
			});
			
			List<OffenceDTO> filteredList = offenceList.stream()
					.filter(val -> val.getOffenceDescription() != null && val.getFixedAmount() != null)
					.collect(Collectors.toList());

			Map<String, Long> offenceDescCount = filteredList.stream()
					.collect(Collectors.groupingBy(OffenceDTO::getOffenceDescription, Collectors.counting()));

			Map<String, Double> offenceAmountSum = filteredList.stream().collect(Collectors.groupingBy(
					OffenceDTO::getOffenceDescription, Collectors.summingDouble(OffenceDTO::getFixedAmount)));
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
				throw new BadRequestException("No offence found for office" + officeCodeList);
			}
			
			List<ReportVO> finalList = setTotalsForVCR(offenceReportList);
			regReportVO.setOffencesBasedList(finalList);
		}else if (StringUtils.isNotBlank(valVO.getOffenceName())) {
			Set<String> mviSet = new HashSet<>();
			List<CheckPostReportsVO> mviBasedList = mviBasedOffenceAggrigation(valVO.getOfficeCodes(),
					valVO);
			mviSet = mviBasedList.stream().map(vcr -> vcr.getMviName()).collect(Collectors.toSet());
			List<CheckPostReportsVO> cpFinalList = vcrDetailsForCovReport(mviBasedList, mviSet, "offence",
					valVO.getOffenceName());
			regReportVO.setMviCovList(cpFinalList);
			
			setTotals(regReportVO.getMviCovList());
		}
		return regReportVO;
	}

	private List<ReportVO> setTotalsForVCR(List<ReportVO> offenceReportList) {
		List<ReportVO> finalList = offenceReportList.stream()
				.sorted(Comparator.comparing(ReportVO::getOffenceCount).reversed()).collect(Collectors.toList());
		ReportVO totalVO = new ReportVO();
		totalVO.setCov("TOTAL");
		totalVO.setMviName("TOTAL");
		totalVO.setOffenceDesc("TOTAL");
		totalVO.setOffenceCount(offenceReportList.stream().collect(Collectors.summingLong(ReportVO::getOffenceCount)));
		finalList.add(totalVO);
		return finalList;
	}

	private List<CheckPostReportsVO> setTotals(List<CheckPostReportsVO> mviCovList) {
		List<CheckPostReportsVO> finalList = mviCovList.stream()
				.sorted(Comparator.comparing(CheckPostReportsVO::getVcrCount).reversed()).collect(Collectors.toList());
		CheckPostReportsVO totalVO = new CheckPostReportsVO();
		totalVO.setCov("TOTAL");
		totalVO.setCovDescription("TOTAL");
		totalVO.setMviName("TOTAL");
		totalVO.setVcrCount(mviCovList.stream().collect(Collectors.summingLong(CheckPostReportsVO::getVcrCount)));
		finalList.add(totalVO);
		return finalList;
	}

	private List<CheckPostReportsVO> mviBasedOffenceAggrigation(List<String> officeCodes, RegReportVO regReportVO) {

		LocalDateTime fromDate = paymentReportService.getTimewithDate(regReportVO.getFromDate(), false);
		LocalDateTime toDate = paymentReportService.getTimewithDate(regReportVO.getToDate(), true);
		Aggregation agg = newAggregation(
				match(Criteria.where("vcr.dateOfCheck").gte(fromDate).lte(toDate)
						.and("offence.offence.offenceDescription").is(regReportVO.getOffenceName()).and("officeCode")
						.in(officeCodes)),
				group("createdBy").count().as("vcrCount"), project("vcrCount").and("mviName").previousOperation()

		);
		AggregationResults<CheckPostReportsVO> groupResults = mongoTemplate.aggregate(agg, VcrFinalServiceDTO.class,
				CheckPostReportsVO.class);
		LOGGER.info("check vcr  mVI Based count raw results [{}]", groupResults.getRawResults());
		List<CheckPostReportsVO> result = groupResults.getMappedResults();

		return result;
	}

	@Override
	public VcrCovAndOffenceBasedReportVO offenseWiseReportForEvcr(RegReportVO valVO) {
		VcrCovAndOffenceBasedReportVO regReportVO = new VcrCovAndOffenceBasedReportVO();
		List<ReportVO> offenceReportList = new ArrayList<>();
		if (StringUtils.isBlank(valVO.getOffenceName())) {
			LocalDateTime fromDate = paymentReportService.getTimewithDate(valVO.getFromDate(), false);
			LocalDateTime toDate = paymentReportService.getTimewithDate(valVO.getToDate(), true);
			List<String> officeCodeList = valVO.getOfficeCodes();

			List<VcrFinalServiceDTO> vcrList = vcrFinalServiceDAO.findFixedAmountsSumBasedOnEvcr(officeCodeList, fromDate, toDate);
			LOGGER.info("offence Enforcement report  for officeCode [{}]", officeCodeList);

			List<OffenceDTO> offenceList = vcrList.stream().map(map -> map.getOffence().getOffence())
					.flatMap(x -> x.stream()).collect(Collectors.toList());

			offenceList.stream().forEach(val -> {
				if (val.getFixedAmount() == null) {
					val.setFixedAmount(0);
				}
			});
			
			List<OffenceDTO> filteredList = offenceList.stream()
					.filter(val -> val.getOffenceDescription() != null && val.getFixedAmount() != null)
					.collect(Collectors.toList());

			Map<String, Long> offenceDescCount = filteredList.stream()
					.collect(Collectors.groupingBy(OffenceDTO::getOffenceDescription, Collectors.counting()));

			Map<String, Double> offenceAmountSum = filteredList.stream().collect(Collectors.groupingBy(
					OffenceDTO::getOffenceDescription, Collectors.summingDouble(OffenceDTO::getFixedAmount)));
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
				throw new BadRequestException("No offence found for office" + officeCodeList);
			}
			
			List<ReportVO> finalList = setTotalsForVCR(offenceReportList);
			regReportVO.setOffencesBasedList(finalList);
		}else if (StringUtils.isNotBlank(valVO.getOffenceName())) {
			Set<String> mviSet = new HashSet<>();
			List<CheckPostReportsVO> mviBasedList = mviBasedOffenceAggrigation(valVO.getOfficeCodes(),
					valVO);
			mviSet = mviBasedList.stream().map(vcr -> vcr.getMviName()).collect(Collectors.toSet());
			List<CheckPostReportsVO> cpFinalList = vcrDetailsForCovReport(mviBasedList, mviSet, "offence",
					valVO.getOffenceName());
			regReportVO.setMviCovList(cpFinalList);
			
			setTotals(regReportVO.getMviCovList());
		}
		return regReportVO;
	}
	
}
