package org.epragati.reports.service.impl;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.unwind;

import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.epragati.constants.CommonConstants;
import org.epragati.constants.NationalityEnum;
import org.epragati.excel.service.CellProps;
import org.epragati.excel.service.ExcelService;
import org.epragati.excel.service.ExcelServiceImpl;
import org.epragati.exception.BadRequestException;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dao.DistrictDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dto.DistrictDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.service.OfficeService;
import org.epragati.master.vo.OfficeVO;
import org.epragati.payment.dto.BreakPaymentsSaveDTO;
import org.epragati.payment.dto.FeesDTO;
import org.epragati.payment.dto.PaymentTransactionDTO;
import org.epragati.payment.mapper.PaymentTransactionMapper;
import org.epragati.payment.report.vo.RegReportVO;
import org.epragati.payments.dao.PaymentTransactionDAO;
import org.epragati.payments.vo.BreakPayments;
import org.epragati.reports.service.RtaReportService;
import org.epragati.restGateway.service.impl.RestGateWayServiceImpl;
import org.epragati.rta.reports.vo.CheckPostReportsVO;
import org.epragati.rta.reports.vo.DetectionVO;
import org.epragati.rta.reports.vo.DistrictReportVO;
import org.epragati.rta.reports.vo.DistrictWiseReportVO;
import org.epragati.rta.reports.vo.FeeReportVO;
import org.epragati.rta.reports.vo.LifeTaxVO;
import org.epragati.rta.reports.vo.OfficePaymentVO;
import org.epragati.rta.reports.vo.QuarterTaxVO;
import org.epragati.rta.reports.vo.ReportFeeVO;
import org.epragati.rta.reports.vo.ReportTotalsVo;
import org.epragati.rta.reports.vo.RevenueReportVO;
import org.epragati.rta.reports.vo.SubReportVO;
import org.epragati.rta.vo.ReportResponseVO;
import org.epragati.rta.vo.ReportVO;
import org.epragati.sn.dao.SpecialNumberDetailsDAO;
import org.epragati.sn.dto.SpecialNumberDetailsDTO;
import org.epragati.sn.mappers.SpecialNumberDetailsMapper;
import org.epragati.sn.vo.SNReportResultVO;
import org.epragati.sn.vo.SPNumberPaymentDetailsVO;
import org.epragati.sn.vo.SpecialNumberDetailsByTRVO;
import org.epragati.util.BidStatus;
import org.epragati.util.DateConverters;
import org.epragati.util.FeeTypeDetails;
import org.epragati.util.payment.GatewayTypeEnum;
import org.epragati.util.payment.PayStatusEnum;
import org.epragati.util.payment.ServiceCodeEnum;
import org.epragati.vcrImage.dto.VcrFinalServiceDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

@Service
public class RtaReportServiceImpl implements RtaReportService {

	private static final Logger logger = LoggerFactory.getLogger(RtaReportServiceImpl.class);

	@Autowired
	private SpecialNumberDetailsDAO specialNumberDetailsDAO;

	@Autowired
	private PaymentTransactionDAO paymentTransactionDAO;

	@Autowired
	private SpecialNumberDetailsMapper specialNumberDetailsMapper;

	@Autowired
	private OfficeDAO officeDAO;

	@Autowired
	private PaymentTransactionMapper paymentTransactionMapper;

	@Autowired
	private DistrictDAO districtDAO;

	@Autowired
	private OfficeService officeService;

	@Autowired
	private RestGateWayServiceImpl restGateWayServiceImpl;
	
	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public List<ReportResponseVO> getEbidingDetails(ReportVO reportVO, JwtUser user) {

		Optional<OfficeDTO> officeDetails = officeDAO.findByOfficeCode(user.getOfficeCode());

		if (!officeDetails.isPresent()) {
			logger.error("Office Details not found with the given User Id :[{}] and Office Code[{}]",
					user.getUsername(), user.getOfficeCode());
			throw new BadRequestException("Office Details not found with the given User Id");
		}

		String officeNumberSeries = officeDetails.get().getOfficeNumberSeries();

		List<SpecialNumberDetailsDTO> spBidDetails = null;
		if (StringUtils.isNotBlank(reportVO.getPrNo())) {
			spBidDetails = specialNumberDetailsDAO.findBySelectedPrSeriesAndVehicleDetailsRtaOfficeOfficeNumberSeries(
					reportVO.getPrNo(), officeNumberSeries);
		}
		if (reportVO.getFromDate() != null) {
			List<LocalDateTime> dateList = DateConverters.convertDateToLocalDateTime(reportVO.getFromDate(),
					reportVO.getToDate());
			spBidDetails = specialNumberDetailsDAO.findByCreatedDateBetweenAndVehicleDetailsRtaOfficeOfficeNumberSeries(
					dateList.stream().findFirst().get(), dateList.get(1), officeNumberSeries);
		}

		return getBidDetails(spBidDetails);
	}

	public static String replaceDefaults(String input) {

		if (StringUtils.isBlank(input) || input == null) {
			return StringUtils.EMPTY;
		}
		return input;
	}

	public static Double replaceDefaults(Double value) {

		if (value == null) {
			return 0.0;
		}
		return value;
	}

	private Optional<PaymentTransactionDTO> getPaymentStatus(String transactionNo) {
		Optional<PaymentTransactionDTO> payStatus = paymentTransactionDAO.findByTransactioNo(transactionNo);
		if (payStatus.isPresent()) {
			return payStatus;
		}
		return Optional.empty();
	}

	private List<ReportResponseVO> getBidDetails(List<SpecialNumberDetailsDTO> spBidDetails) {

		if (CollectionUtils.isEmpty(spBidDetails)) {
			return Collections.emptyList();
		}
		List<ReportResponseVO> listReportVo = new ArrayList<>();
		

		for (SpecialNumberDetailsDTO specialNumberDetailsDTO : spBidDetails) {
			String value = StringUtils.EMPTY;
			Double spFee = 0.0;
			Double bidFinFee = 0.0;
			ReportResponseVO responseVo = new ReportResponseVO();
			responseVo.setDate(specialNumberDetailsDTO.getCreatedDate().toLocalDate());

			if (specialNumberDetailsDTO.getVehicleDetails() != null) {
				responseVo.setApplicationNo(
						replaceDefaults(specialNumberDetailsDTO.getVehicleDetails().getApplicationNumber()));
				responseVo.setTrNo(replaceDefaults(specialNumberDetailsDTO.getVehicleDetails().getTrNumber()));
			}

			if (specialNumberDetailsDTO.getCustomerDetails() != null) {
				responseVo
				.setApplicantName(replaceDefaults(specialNumberDetailsDTO.getCustomerDetails().getFirstName()));
				responseVo.setMobileNo(replaceDefaults(specialNumberDetailsDTO.getCustomerDetails().getMobileNo()));
			}

			responseVo.setPrNo(specialNumberDetailsDTO.getSelectedPrSeries());
			responseVo.setFinalStatus(replaceDefaults(specialNumberDetailsDTO.getBidStatus().toString()));

			responseVo.setSpRegRefId(value);
			responseVo.setAmount(0.0);
			responseVo.setRegPaymentStatus(value);
			responseVo.setSpRefStatus(value);

			// SpecialNumberFeeDetails
			if (specialNumberDetailsDTO.getSpecialNumberFeeDetails() != null) {

				responseVo.setSpRegRefId(
						replaceDefaults(specialNumberDetailsDTO.getSpecialNumberFeeDetails().getRefundId()));

				if (specialNumberDetailsDTO.getSpecialNumberFeeDetails().getTransactionNo() != null) {

					Optional<PaymentTransactionDTO> payStatus = getPaymentStatus(
							specialNumberDetailsDTO.getSpecialNumberFeeDetails().getTransactionNo());
					if (payStatus.isPresent()) {
						responseVo.setRegPaymentStatus(payStatus.get().getPayStatus());
						if (payStatus.get().getPayURefundResponse() != null) {
							responseVo.setSpRefStatus(
									replaceDefaults(payStatus.get().getPayURefundResponse().getMessage()));
						}
					}
				}

				spFee = replaceDefaults(specialNumberDetailsDTO.getSpecialNumberFeeDetails().getTotalAmount());
			}

			responseVo.setBidPaymentStatus(value);
			responseVo.setBidAmountRefId(value);
			responseVo.setBidRefStatus(value);

			// BidFinalDetails
			if (specialNumberDetailsDTO.getBidFinalDetails() != null) {

				responseVo
				.setBidAmountRefId(replaceDefaults(specialNumberDetailsDTO.getBidFinalDetails().getRefundId()));

				if (specialNumberDetailsDTO.getBidFinalDetails().getTransactionNo() != null) {
					Optional<PaymentTransactionDTO> payStatus = getPaymentStatus(
							specialNumberDetailsDTO.getBidFinalDetails().getTransactionNo());
					if (payStatus.isPresent()) {
						responseVo.setBidPaymentStatus(payStatus.get().getPayStatus());
						if (payStatus.get().getPayURefundResponse() != null) {
							responseVo.setBidRefStatus(
									replaceDefaults(payStatus.get().getPayURefundResponse().getMessage()));
						}
					}
				}

				bidFinFee = replaceDefaults(specialNumberDetailsDTO.getBidFinalDetails().getBidAmount());
			}

			responseVo.setSpRegAmount(spFee);
			responseVo.setBidAmount(bidFinFee);
			responseVo.setAmount(spFee + bidFinFee);
			listReportVo.add(responseVo);
		}
		return listReportVo;

	}

	public List<SpecialNumberDetailsByTRVO> getSpecialNumberDetailsByTrNO(
			StagingRegistrationDetailsDTO registrationDetails) {
		List<SpecialNumberDetailsByTRVO> voList = new ArrayList<>();
		List<SpecialNumberDetailsDTO> specialNumberDetailsDTO = specialNumberDetailsDAO
				.findByVehicleDetailsTrNumber(registrationDetails.getTrNo());
		specialNumberDetailsDTO.stream().forEach(dto -> {
			SpecialNumberDetailsByTRVO specialNumberDetailsByTRVO = new SpecialNumberDetailsByTRVO();
			specialNumberDetailsByTRVO.setSpecialNumber(dto.getSelectedPrSeries());
			specialNumberDetailsByTRVO.setStatus(dto.getBidStatus().getDescription());
			specialNumberDetailsByTRVO.setDate(dto.getCreatedDate().toLocalDate());
			specialNumberDetailsByTRVO.setId(dto.getSpecialNumberAppId());
			voList.add(specialNumberDetailsByTRVO);
		});
		return voList;
	}

	@Override
	public ReportResponseVO getSpecialNumberDetailsById(String id) {
		ReportResponseVO reportVo = new ReportResponseVO();
		Optional<SpecialNumberDetailsDTO> specialNumberDetails = specialNumberDetailsDAO.findBySpecialNumberAppId(id);

		if (!specialNumberDetails.isPresent()) {
			Optional.empty();
		}
		reportVo = specialNumberDetailsMapper.converLimitedFieldsDTOtoVO(specialNumberDetails.get());
		return reportVo;
	}

	@Override
	public SPNumberPaymentDetailsVO spNumberPaymentDetails(String paymentId) {
		SPNumberPaymentDetailsVO spNumberDetailsVo = new SPNumberPaymentDetailsVO();
		Optional<PaymentTransactionDTO> paymentDetails = paymentTransactionDAO.findByTransactioNo(paymentId);
		if (!paymentDetails.isPresent()) {
			Optional.empty();
		}
		spNumberDetailsVo = paymentTransactionMapper.converLimitedFieldsOfPaymentForSpNumber(paymentDetails.get());
		return spNumberDetailsVo;
	}

	@Override
	public RevenueReportVO getDistrictWiseReport(LocalDate fromDate, LocalDate toDate) {

		List<DistrictWiseReportVO> districtWiseReportVO = new ArrayList<>();
		List<LocalDateTime> localDateTimes = DateConverters.convertDateToLocalDateTime(fromDate, toDate);
		Optional<List<PaymentTransactionDTO>> paymetDetails = paymentTransactionDAO.findByResponseResponseTimeBetweenAndPayStatus(localDateTimes.stream().findFirst().get(), localDateTimes.get(1), PayStatusEnum.SUCCESS.getDescription());
		RevenueReportVO revenueReportVO = new RevenueReportVO();
		revenueReportVO.setDistrictWiseVOList(Collections.emptyList());
		if(CollectionUtils.isEmpty(paymetDetails.get())){
			return revenueReportVO;
		}
		List<DistrictDTO> districtList = districtDAO.findByStateId(NationalityEnum.AP.getName());
		if(CollectionUtils.isEmpty(districtList)){
			return revenueReportVO;
		}
		int i=0;
		Double quarterGrandTotal =0.0;
		Double lifeGrandTotal =0.0;
		Double feeGrandTotal =0.0;
		Double serviceGrandTotal =0.0;
		//Double detectionGrandTotal =0.0;
		for (DistrictDTO district : districtList) {
			DistrictWiseReportVO vo = new DistrictWiseReportVO();

			SubReportVO subServiceCharge = null;
			SubReportVO fee = null;
			SubReportVO lTaxVo = null;
			SubReportVO qTaxVo = null;
			SubReportVO detVo= null;
			SubReportVO total = null;

			Optional<DistrictDTO> disOptional = districtDAO.findByDistrictName(district.getDistrictName());
			if (disOptional.isPresent()) {

				subServiceCharge = new SubReportVO();
				fee = new SubReportVO();
				lTaxVo = new SubReportVO();
				qTaxVo = new SubReportVO();
				detVo= new  SubReportVO();
				total = new SubReportVO();
				DistrictDTO districtDTO = disOptional.get();
				List<OfficeDTO> officeList = officeDAO.findBydistrict(districtDTO.getDistrictId());
				logger.info("office Codes list [{}]",officeList);
				Double serviceCharge = 0.0;
				Double applicationFee = 0.0;
				Double qTax = 0.0;
				Double lTax = 0.0;
				for (OfficeDTO officeCode : officeList) {
					List<PaymentTransactionDTO> officeWiseList =paymetDetails.get().stream().filter(e-> e.getOfficeCode()==null || e.getOfficeCode().equalsIgnoreCase(officeCode.getOfficeCode()) || e.getOfficeCode().equalsIgnoreCase("OTHER")).collect(Collectors.toList());
					for (PaymentTransactionDTO paymentDTO : officeWiseList) {
						FeeReportVO feeReportVO = getAllFees(paymentDTO,officeCode.getOfficeCode());
						serviceCharge = serviceCharge+feeReportVO.getServiceFee();
						lTax = lTax+feeReportVO.getLifeTax();
						qTax = qTax+feeReportVO.getQuarterlyTax();
						applicationFee = applicationFee+feeReportVO.getFee();
					}
				}
				subServiceCharge.setCollected(decimalPoints(getAmountInLakhs(serviceCharge)));
				serviceGrandTotal = decimalPoints(serviceGrandTotal+decimalPoints(getAmountInLakhs(serviceCharge)));
				lTaxVo.setCollected(decimalPoints(getAmountInLakhs(lTax)));
				lifeGrandTotal = decimalPoints(lifeGrandTotal+decimalPoints(getAmountInLakhs(lTax)));
				qTaxVo.setCollected(decimalPoints(getAmountInLakhs(qTax)));
				quarterGrandTotal = decimalPoints(quarterGrandTotal+decimalPoints(getAmountInLakhs(qTax)));
				fee.setCollected(decimalPoints(getAmountInLakhs(applicationFee)));
				feeGrandTotal = decimalPoints(feeGrandTotal+decimalPoints(getAmountInLakhs(applicationFee)));
				total.setCollected(decimalPoints(decimalPoints(getAmountInLakhs(serviceCharge))+decimalPoints(getAmountInLakhs(lTax))+decimalPoints(getAmountInLakhs(qTax))+decimalPoints(getAmountInLakhs(applicationFee))));
			}
			vo.setServicecharge(subServiceCharge);
			vo.setFee(fee);
			vo.setDistrictName(district.getDistrictName());
			vo.setDistrictId(district.getDistrictId());
			vo.setQuarterlyTax(qTaxVo);
			vo.setLifeTax(lTaxVo);
			vo.setDetection(detVo);
			vo.setTotal(total);
			if(i++ == districtList.size()-1){

				ReportTotalsVo reportTotalsVo = new ReportTotalsVo();
				reportTotalsVo.setDetectionGrandTotal(0.0);
				reportTotalsVo.setFeeGrandTotal(decimalPoints(feeGrandTotal));
				reportTotalsVo.setLifeGrandTotal(decimalPoints(lifeGrandTotal));
				reportTotalsVo.setQuaterGrandTotal(decimalPoints(quarterGrandTotal));
				reportTotalsVo.setServiceGrandTotal(decimalPoints(serviceGrandTotal));
				reportTotalsVo.setTotalGrandTotal(decimalPoints(decimalPoints(feeGrandTotal)+decimalPoints(lifeGrandTotal)+decimalPoints(quarterGrandTotal)+decimalPoints(serviceGrandTotal)));

				//vo.setGrandTotals(reportTotalsVo);
				revenueReportVO.setReportTotalsVo(reportTotalsVo);
			}
			districtWiseReportVO.add(vo);
		}
		revenueReportVO.setDistrictWiseVOList(districtWiseReportVO);
		return revenueReportVO;

	}

	@Override
	public RevenueReportVO getDistrictReport(String districtId,LocalDate fromDate, LocalDate toDate) {
		// TODO Auto-generated method stub
		List<LocalDateTime> localDateTimes = DateConverters.convertDateToLocalDateTime(fromDate, toDate);
		List<OfficeVO> officeVo  = officeService.getOfficeByDistrict(Integer.parseInt(districtId));
		RevenueReportVO revenueReportVO = new RevenueReportVO();
		revenueReportVO.setDistrictReportVO(Collections.emptyList());
		if(CollectionUtils.isEmpty(officeVo)){
			return revenueReportVO;
		}
		List<DistrictReportVO> districtReport = new ArrayList<>();

		Optional<List<PaymentTransactionDTO>> paymentTransactionList = paymentTransactionDAO.findByResponseResponseTimeBetweenAndPayStatus(localDateTimes.stream().findFirst().get(), localDateTimes.get(1), PayStatusEnum.SUCCESS.getDescription());
		int i = 0;
		Double detectionGrandTotal=0.0;
		Double feeGrandTotal=0.0;
		Double lifeGrandTotal=0.0;
		Double quaterGrandTotal=0.0;
		Double serviceGrandTotal=0.0;
		for(OfficeVO officeVO : officeVo){

			List<PaymentTransactionDTO> officeWiseList = paymentTransactionList.get().stream().filter(e-> e.getOfficeCode()==null || e.getOfficeCode().equalsIgnoreCase(officeVO.getOfficeCode()) || e.getOfficeCode().equalsIgnoreCase("OTHER")).collect(Collectors.toList());

			DistrictReportVO districtReportVO = new DistrictReportVO();
			districtReportVO.setOfficeName(officeVO.getOfficeName());
			districtReportVO.setOfficeCode(officeVO.getOfficeCode());
			Double quarterlyTax = 0.0;
			Double lifeTax = 0.0;
			Double regFee = 0.0;
			Double serviceFee=0.0;

			Double fitnessServiceFee = 0.0;
			Double fitnessFee = 0.0;
			Double postalRegFee=0.0;
			Double regApplicationFee =0.0;
			Double regServiceFee = 0.0;

			for(PaymentTransactionDTO paymentTransactionDTO :officeWiseList){

				FeeReportVO feeReportVO= getAllFees(paymentTransactionDTO,officeVO.getOfficeCode());
				
				//To dispaly Total fees
				quarterlyTax=quarterlyTax+feeReportVO.getQuarterlyTax();
				lifeTax = lifeTax+feeReportVO.getLifeTax();
				regFee = regFee+feeReportVO.getFee();
				serviceFee=serviceFee+feeReportVO.getServiceFee();

				//To display individual fees
				fitnessFee= fitnessFee+feeReportVO.getFitnessFee();
				fitnessServiceFee=fitnessServiceFee+feeReportVO.getRegFitnessServieceFee();
				postalRegFee=postalRegFee+feeReportVO.getRegPostalFee();
				regApplicationFee = regApplicationFee+feeReportVO.getRegistrationFee();
				regServiceFee = regServiceFee+feeReportVO.getRegServiceFee();

			}


			Double officeGrandTotal=0.0;
			//To dispaly QuarterTax fee
			QuarterTaxVO quarterTaxVO = new QuarterTaxVO();
			quarterTaxVO.setQuarterlyTax(decimalPoints(getAmountInLakhs(quarterlyTax)));
			quarterTaxVO.setQuarterlyTaxPenality(0.0);
			quarterTaxVO.setTotal(decimalPoints(getAmountInLakhs(quarterlyTax)));
			districtReportVO.setQuarterlyTax(quarterTaxVO);

			//office wise total displaying purpose
			officeGrandTotal = officeGrandTotal+decimalPoints(getAmountInLakhs(quarterlyTax));
			//fee wise total displaying purpose
			quaterGrandTotal = quaterGrandTotal+decimalPoints(getAmountInLakhs(quarterlyTax));

			//To dispaly LifeTax fee
			LifeTaxVO lifeTaxVO = new LifeTaxVO();
			lifeTaxVO.setLifeTax(decimalPoints(getAmountInLakhs(lifeTax)));
			lifeTaxVO.setLifeTaxPenality(0.0);
			lifeTaxVO.setTotal(decimalPoints(getAmountInLakhs(lifeTax)));
			districtReportVO.setLifeTax(lifeTaxVO);
			officeGrandTotal = officeGrandTotal+decimalPoints(getAmountInLakhs(lifeTax));
			lifeGrandTotal = lifeGrandTotal+decimalPoints(getAmountInLakhs(lifeTax));


			ReportFeeVO reportFeeVO = new ReportFeeVO();
			reportFeeVO.setFitness(decimalPoints(getAmountInLakhs(fitnessFee)));
			reportFeeVO.setGreenTax(0.0);
			//ToDO Need to Fetch data from the third party war 
			reportFeeVO.setLicense(0.0);
			reportFeeVO.setOther(0.0);
			//ToDO Need to Fetch data from the third party war 
			reportFeeVO.setPermit(0.0);
			reportFeeVO.setRegistration(decimalPoints(getAmountInLakhs(regApplicationFee)));
			//reportFeeVO.setTotal(decimalPoints(decimalPoints(getAmountInLakhs(fitnessFee))+ decimalPoints(getAmountInLakhs(regApplicationFee))));
			reportFeeVO.setTotal(decimalPoints(getAmountInLakhs(regFee)));
			//officeGrandTotal = decimalPoints(officeGrandTotal+decimalPoints(getAmountInLakhs(fitnessFee))+ decimalPoints(getAmountInLakhs(regApplicationFee))+decimalPoints(getAmountInLakhs(postalRegFee)));
			officeGrandTotal = officeGrandTotal +decimalPoints(getAmountInLakhs(regFee));
			districtReportVO.setFee(reportFeeVO);

			feeGrandTotal=feeGrandTotal+decimalPoints(getAmountInLakhs(regFee));


			ReportFeeVO serviecFees = new ReportFeeVO();
			serviecFees.setRegistration(decimalPoints(getAmountInLakhs(regServiceFee)));
			serviecFees.setFitness(decimalPoints(getAmountInLakhs(fitnessServiceFee)));
			//ToDO Need to Fetch data from the third party war 
			serviecFees.setLicense(0.0);
			serviecFees.setOther(0.0);
			//ToDO Need to Fetch data from the third party war 
			serviecFees.setPermit(0.0);
			//serviecFees.setTotal(decimalPoints(decimalPoints(getAmountInLakhs(regServiceFee))+decimalPoints(getAmountInLakhs(fitnessServiceFee))));
			serviecFees.setTotal(decimalPoints(getAmountInLakhs(serviceFee)));
			districtReportVO.setServiceCharges(serviecFees);

			//officeGrandTotal =decimalPoints( officeGrandTotal+decimalPoints(decimalPoints(getAmountInLakhs(regServiceFee))+decimalPoints(getAmountInLakhs(fitnessServiceFee))));
			officeGrandTotal= officeGrandTotal+decimalPoints(getAmountInLakhs(serviceFee));
			//Service grand total
			serviceGrandTotal= serviceGrandTotal+decimalPoints(getAmountInLakhs(serviceFee));


			ReportFeeVO postalFees = new ReportFeeVO();
			postalFees.setRegistration(decimalPoints(getAmountInLakhs(postalRegFee)));
			postalFees.setLicense(0.0);
			postalFees.setTotal(decimalPoints(getAmountInLakhs(postalRegFee)));

			//feeGrandTotal=feeGrandTotal+getAmountInLakhs(postalRegFee);

			districtReportVO.setPostalFee(postalFees);

			DetectionVO detectionVO = new DetectionVO();
			detectionVO.setCompoundingFee(0.0);
			detectionVO.setDetectionTax(0.0);
			detectionVO.setTotal(0.0);
			detectionGrandTotal = detectionGrandTotal+0.0;
			districtReportVO.setDetection(detectionVO);

			districtReportVO.setOfficeGrandTotal(decimalPoints(officeGrandTotal));

			if(i++ == officeVo.size() - 1){
				// Last iteration
				ReportTotalsVo reportTotalsVo = new ReportTotalsVo();
				reportTotalsVo.setDetectionGrandTotal(decimalPoints(detectionGrandTotal));
				reportTotalsVo.setFeeGrandTotal(decimalPoints(feeGrandTotal));
				reportTotalsVo.setLifeGrandTotal(decimalPoints(lifeGrandTotal));
				reportTotalsVo.setQuaterGrandTotal(decimalPoints(quaterGrandTotal));
				reportTotalsVo.setServiceGrandTotal(decimalPoints(serviceGrandTotal));
				reportTotalsVo.setTotalGrandTotal(decimalPoints(detectionGrandTotal+decimalPoints(feeGrandTotal)+decimalPoints(lifeGrandTotal)+decimalPoints(quaterGrandTotal)+decimalPoints(serviceGrandTotal)));
				revenueReportVO.setReportTotalsVo(reportTotalsVo);
				//districtReportVO.setGrandTotals(reportTotalsVo);
			}

			districtReport.add(districtReportVO);
		}
		revenueReportVO.setDistrictReportVO(districtReport);
		return revenueReportVO;
	}

	@Override
	public RevenueReportVO getOfficewisepPayments(String officeCode,LocalDate fromDate, LocalDate toDate) {
		// TODO Auto-generated method stub
		List<LocalDateTime> localDateTimes = DateConverters.convertDateToLocalDateTime(fromDate,toDate);

		Optional<List<PaymentTransactionDTO>> paymentTransactionList = paymentTransactionDAO.findByResponseResponseTimeBetweenAndPayStatus(localDateTimes.stream().findFirst().get(), localDateTimes.get(1), PayStatusEnum.SUCCESS.getDescription());
		RevenueReportVO revenueReportVO = new RevenueReportVO();
		revenueReportVO.setDistrictReportVO(Collections.emptyList());
		if(CollectionUtils.isEmpty(paymentTransactionList.get())){
			return revenueReportVO;
		}
		List<OfficePaymentVO> officePaymentsList = new ArrayList<>();
		List<Integer> paymentIds = paymentTransactionList.get().stream().map(e -> e.getPaymentGatewayType()).collect(Collectors.toList());
		List<Integer> paymentIdList = paymentIds.stream().distinct().collect(Collectors.toList());

		Double quarterlyTax=0.0;
		Double lifeTax = 0.0;
		Double fee = 0.0;
		Double serviceFee = 0.0;
		Double compundingFee=0.0;
		Double total = 0.0;

		for(int i=0;i<paymentIdList.size();i++){
			final int j= i;
			List<PaymentTransactionDTO> payModeList = paymentTransactionList.get().stream().filter(p->p.getPaymentGatewayType().equals(paymentIdList.get(j))).collect(Collectors.toList());

			List<PaymentTransactionDTO> officeWisePayModeList = payModeList.stream().filter(e-> e.getOfficeCode()==null || e.getOfficeCode().equalsIgnoreCase(officeCode) || e.getOfficeCode().equalsIgnoreCase("OTHER")).collect(Collectors.toList());
			OfficePaymentVO officePaymentVo = new OfficePaymentVO();
			officePaymentVo.setPaymentMode(GatewayTypeEnum.getGatewayTypeEnumById(paymentIdList.get(j)).toString());
			officePaymentVo.setGatewayId(paymentIdList.get(j));
			String amountType = "lakhs";
			OfficePaymentVO officePaymentVO = getAllFeeDetails(officePaymentVo,officeWisePayModeList,amountType,officeCode);

			quarterlyTax = quarterlyTax+officePaymentVO.getQuarterlyTax();
			lifeTax = lifeTax+officePaymentVO.getLifeTax();
			fee = fee+officePaymentVO.getFee();
			serviceFee = serviceFee+officePaymentVO.getServiceFee();	
			compundingFee = compundingFee+officePaymentVO.getCompundingFee();	
			total = total + decimalPoints(officePaymentVO.getQuarterlyTax())+decimalPoints(officePaymentVO.getLifeTax())+decimalPoints(officePaymentVO.getFee())+decimalPoints(officePaymentVO.getServiceFee())+decimalPoints(officePaymentVO.getCompundingFee());
			if(i==paymentIdList.size()-1){
				ReportTotalsVo reportTotalsVo = new ReportTotalsVo();
				reportTotalsVo.setQuaterGrandTotal(decimalPoints(quarterlyTax));
				reportTotalsVo.setLifeGrandTotal(decimalPoints(lifeTax));
				reportTotalsVo.setFeeGrandTotal(decimalPoints(fee));
				reportTotalsVo.setServiceGrandTotal(decimalPoints(serviceFee));
				reportTotalsVo.setDetectionGrandTotal(decimalPoints(compundingFee));
				reportTotalsVo.setTotalGrandTotal(decimalPoints(total));
				//officePaymentVO.setGrandTotals(reportTotalsVo);
				revenueReportVO.setReportTotalsVo(reportTotalsVo);
			}
			officePaymentsList.add(officePaymentVO);
		}
		revenueReportVO.setOfficeVOList(officePaymentsList);
		return revenueReportVO;
	}

	private Double getAmountInLakhs(Double amount){
		if(amount>0.0){
			return amount/100000;
		}
		return 0.0;

	}

	public List<String> getDistrictName(String officeCode) {
		List<DistrictDTO> districtList;
		List<String> districtNames = null;
		Optional<OfficeDTO> officeOptional = officeDAO.findByOfficeCode(officeCode);
		if (officeOptional.isPresent()) {
			OfficeDTO officeDTO = officeOptional.get();
			districtList = districtDAO.findByDistrictId(officeDTO.getDistrict());
			if (districtList.size() > 0) {
				districtNames = districtList.stream().map(val -> val.getDistrictName()).collect(Collectors.toList());
			}

		}
		return districtNames;

	}

	@Override
	public RevenueReportVO getDateWiseReports(String gateWayId,String officeCode, LocalDate fromDate, LocalDate toDate) {

		List<LocalDateTime> localDateTimes = DateConverters.convertDateToLocalDateTime(fromDate,toDate);
		// TODO Auto-generated method stub
		Integer payId = Integer.parseInt(gateWayId);
		Optional<List<PaymentTransactionDTO>> paymentTransactionList = paymentTransactionDAO.findByResponseResponseTimeBetweenAndPayStatusAndPaymentGatewayType(localDateTimes.stream().findFirst().get(), localDateTimes.get(1), PayStatusEnum.SUCCESS.getDescription(),payId);
		RevenueReportVO revenueReportVO = new RevenueReportVO();
		revenueReportVO.setDistrictReportVO(Collections.emptyList());
		if(CollectionUtils.isEmpty(paymentTransactionList.get())){
			return revenueReportVO;
		}
		List<OfficePaymentVO> officePaymentsList = new ArrayList<>();
		Double quarterlyTax=0.0;
		Double lifeTax = 0.0;
		Double fee = 0.0;
		Double serviceFee = 0.0;
		Double compundingFee=0.0;
		Double total = 0.0;
		while (fromDate.isBefore(toDate) || fromDate.equals(toDate)) {
			LocalDate reportDate = fromDate;
			// && p.getPaymentGatewayType().equals(gateWayId)
			List<PaymentTransactionDTO> payModeList = paymentTransactionList.get().stream().filter(p->p.getResponse().getResponseTime().toLocalDate().equals(reportDate)).collect(Collectors.toList());
			List<PaymentTransactionDTO> officeWisePayModeList = payModeList.stream().filter(e-> e.getOfficeCode()==null || e.getOfficeCode().equalsIgnoreCase(officeCode) || e.getOfficeCode().equalsIgnoreCase("OTHER")).collect(Collectors.toList());
			OfficePaymentVO officePaymentVo = new OfficePaymentVO();
			officePaymentVo.setDate(reportDate);
			officePaymentVo.setGatewayId(payId);
			String amountType = "lakhs";
			OfficePaymentVO	officePaymentVO = getAllFeeDetails(officePaymentVo,officeWisePayModeList,amountType,officeCode);
			quarterlyTax = quarterlyTax+officePaymentVO.getQuarterlyTax();
			lifeTax = lifeTax+officePaymentVO.getLifeTax();
			fee = fee+officePaymentVO.getFee();
			serviceFee = serviceFee+officePaymentVO.getServiceFee();	
			compundingFee = compundingFee+officePaymentVO.getCompundingFee();	
			total = total + decimalPoints(officePaymentVO.getQuarterlyTax())+decimalPoints(officePaymentVO.getLifeTax())+decimalPoints(officePaymentVO.getFee())+decimalPoints(officePaymentVO.getServiceFee())+decimalPoints(officePaymentVO.getCompundingFee());
			if(fromDate.equals(toDate)){
				ReportTotalsVo reportTotalsVo = new ReportTotalsVo();
				reportTotalsVo.setQuaterGrandTotal(decimalPoints(quarterlyTax));
				reportTotalsVo.setLifeGrandTotal(decimalPoints(lifeTax));
				reportTotalsVo.setFeeGrandTotal(decimalPoints(fee));
				reportTotalsVo.setServiceGrandTotal(decimalPoints(serviceFee));
				reportTotalsVo.setDetectionGrandTotal(decimalPoints(compundingFee));
				reportTotalsVo.setTotalGrandTotal(decimalPoints(total));
				//officePaymentVO.setGrandTotals(reportTotalsVo);
				revenueReportVO.setReportTotalsVo(reportTotalsVo);
			}
			officePaymentsList.add(officePaymentVO);
			fromDate = fromDate.plusDays(1);
		}
		revenueReportVO.setOfficeVOList(officePaymentsList);
		return revenueReportVO;
	}


	private OfficePaymentVO getAllFeeDetails(OfficePaymentVO officePaymentVO,List<PaymentTransactionDTO> payModeList,String amountType,String officeCode){

		//FeeReportVO feeReportVO = new FeeReportVO();
		Double quarterlyTax = 0.0;
		Double lifeTax = 0.0;
		Double fee = 0.0;
		Double serviceFee = 0.0;
		Double compundingFee = 0.0;
		for(PaymentTransactionDTO paymentTransactionDTO : payModeList){

			FeeReportVO feeReportVO= getAllFees(paymentTransactionDTO,officeCode);
			quarterlyTax=quarterlyTax+feeReportVO.getQuarterlyTax();
			lifeTax = lifeTax+feeReportVO.getLifeTax();
			fee = fee+feeReportVO.getFee();
			serviceFee = serviceFee+feeReportVO.getServiceFee();
			compundingFee=compundingFee+feeReportVO.getCompundingFee();
		}
		officePaymentVO.setOfficeCode(officeCode);
		officePaymentVO.setQuarterlyTax(quarterlyTax); 
		officePaymentVO.setLifeTax(lifeTax );
		officePaymentVO.setServiceFee(serviceFee);
		officePaymentVO.setFee(fee);
		officePaymentVO.setCompundingFee(compundingFee);
		officePaymentVO.setTotal(quarterlyTax+lifeTax+ serviceFee +fee +compundingFee);

		if(amountType.equalsIgnoreCase("lakhs")){
			officePaymentVO.setQuarterlyTax(decimalPoints(getAmountInLakhs(quarterlyTax)));
			officePaymentVO.setLifeTax(decimalPoints(getAmountInLakhs(lifeTax)));
			officePaymentVO.setServiceFee(decimalPoints(getAmountInLakhs(serviceFee)));
			officePaymentVO.setFee(decimalPoints(getAmountInLakhs(fee)));
			officePaymentVO.setCompundingFee(decimalPoints(getAmountInLakhs(compundingFee)));
			officePaymentVO.setTotal(decimalPoints(decimalPoints(getAmountInLakhs(quarterlyTax))+decimalPoints(getAmountInLakhs(lifeTax))+decimalPoints(getAmountInLakhs(serviceFee))+decimalPoints(getAmountInLakhs(fee))+decimalPoints(getAmountInLakhs(compundingFee))));
		}
		return officePaymentVO;

	}

	@Override
	public List<OfficePaymentVO> getApplicationWiseReport(String gateWayId, String officeCode, LocalDate fromDate,
			LocalDate toDate) {
		// TODO Auto-generated method stub

		List<LocalDateTime> localDateTimes = DateConverters.convertDateToLocalDateTime(fromDate,toDate);
		// TODO Auto-generated method stub
		Integer payId = Integer.parseInt(gateWayId);

		Optional<List<PaymentTransactionDTO>> paymentTransactionList = paymentTransactionDAO.findByResponseResponseTimeBetweenAndPayStatus(localDateTimes.stream().findFirst().get(), localDateTimes.get(1), PayStatusEnum.SUCCESS.getDescription());
		if(CollectionUtils.isEmpty(paymentTransactionList.get())){
			return Collections.emptyList();
		}
		List<OfficePaymentVO> officePaymentsList = new ArrayList<>();
		List<PaymentTransactionDTO> payModeList = paymentTransactionList.get().stream().filter(p->p.getPaymentGatewayType().equals(payId)).collect(Collectors.toList());
		List<PaymentTransactionDTO> officeWisePayModeList = payModeList.stream().filter(e-> e.getOfficeCode()==null || e.getOfficeCode().equalsIgnoreCase(officeCode) || e.getOfficeCode().equalsIgnoreCase("OTHER")).collect(Collectors.toList());
		for(PaymentTransactionDTO paymentTransactionDTO : officeWisePayModeList){
			OfficePaymentVO officeVO= new OfficePaymentVO();
			FeeReportVO  feeReportVO = getAllFees(paymentTransactionDTO,officeCode);
			if(feeReportVO.getQuarterlyTax()!=0.0 || feeReportVO.getLifeTax()!=0.0 || feeReportVO.getServiceFee()!=0.0 || feeReportVO.getFee()!=0.0 || feeReportVO.getCompundingFee()!=0.0){
				officeVO.setApplicationNo(paymentTransactionDTO.getApplicationFormRefNum());
				officeVO.setQuarterlyTax(feeReportVO.getQuarterlyTax());
				officeVO.setLifeTax(feeReportVO.getLifeTax());
				officeVO.setServiceFee(feeReportVO.getServiceFee());
				officeVO.setFee(feeReportVO.getFee());
				officeVO.setCompundingFee(feeReportVO.getCompundingFee());
				officeVO.setTotal(feeReportVO.getQuarterlyTax()+feeReportVO.getLifeTax()+feeReportVO.getServiceFee()+feeReportVO.getFee()+feeReportVO.getCompundingFee());
				officePaymentsList.add(officeVO);
			}
		}
		return officePaymentsList;
	}



	private FeeReportVO getAllFees(PaymentTransactionDTO paymentTransactionDTO,String officeCode){

		Double quarterlyTax = 0.0;
		Double lifeTax = 0.0;
		Double fee = 0.0;
		Double serviceFee = 0.0;
		Double compundingFee = 0.0;




		FeeReportVO feeReportVO = new FeeReportVO();
		feeReportVO.setCompundingFee(compundingFee);
		feeReportVO.setFee(fee);
		feeReportVO.setLifeTax(lifeTax);
		feeReportVO.setQuarterlyTax(quarterlyTax);
		feeReportVO.setServiceFee(serviceFee);
		String refNumber = paymentTransactionDTO.getApplicationFormRefNum();
		//office code is OTHER we need to display dealer office code said by murthy(there is no Enum for OTHER)
		if(paymentTransactionDTO.getOfficeCode()!=null && paymentTransactionDTO.getOfficeCode().equalsIgnoreCase("OTHER")){
			if(!officeCode.equalsIgnoreCase(restGateWayServiceImpl.officeCodeValidation(officeCode,refNumber))){
				return feeReportVO;
			}
		}

		Boolean paymentToShow = false;
		if(paymentTransactionDTO.getModuleCode()!=null && paymentTransactionDTO.getModuleCode().equalsIgnoreCase("SPNR") ){
			Optional<SpecialNumberDetailsDTO> SpecialNumberDetails = specialNumberDetailsDAO.findBySpecialNumberAppId(refNumber);
			if(SpecialNumberDetails.isPresent()){
				String  bidStatus = SpecialNumberDetails.get().getBidStatus().toString();
				if(bidStatus.equals(BidStatus.BIDWIN.toString()) ||
						bidStatus.equals(BidStatus.BIDABSENT.toString() )){
					if(SpecialNumberDetails.get().getVehicleDetails().getRtaOffice().getOfficeCode()!=null){

						if(!officeCode.equalsIgnoreCase(SpecialNumberDetails.get().getVehicleDetails().getRtaOffice().getOfficeCode())){
							return feeReportVO;
						}
						if(SpecialNumberDetails.get().getSpecialNumberFeeDetails().getServicesAmount()!=null){
							serviceFee = serviceFee+SpecialNumberDetails.get().getSpecialNumberFeeDetails().getServicesAmount();
							feeReportVO.setRegServiceFee(serviceFee);
						}
						fee = fee+SpecialNumberDetails.get().getSpecialNumberFeeDetails().getApplicationAmount();
						if(SpecialNumberDetails.get().getBidFinalDetails()!=null){
							fee = fee+ SpecialNumberDetails.get().getBidFinalDetails().getBidAmount();
						}
					}else{
						return feeReportVO;
					}
				}else if(SpecialNumberDetails.get().getVehicleDetails().getRtaOffice().getOfficeCode()!=null){

					if(!officeCode.equalsIgnoreCase(SpecialNumberDetails.get().getVehicleDetails().getRtaOffice().getOfficeCode())){
						return feeReportVO;
					}
					if(SpecialNumberDetails.get().getSpecialNumberFeeDetails().getServicesAmount()!=null){
						serviceFee = serviceFee+ SpecialNumberDetails.get().getSpecialNumberFeeDetails().getServicesAmount();
						feeReportVO.setRegServiceFee(serviceFee);
					}

				}

				if(bidStatus.equals(BidStatus.FINALPAYMENTFAILURE.toString()) ||
						bidStatus.equals(BidStatus.FINALPAYMENTPENDING.toString()) ||
						bidStatus.equals(BidStatus.SPPAYMENTFAILURE.toString())||
						bidStatus.equals(BidStatus.SPPAYMENTPENDING.toString())){
					paymentToShow = true;
				}
			}
			feeReportVO.setRegistrationFee(fee);
		}else if(paymentTransactionDTO.getBreakPaymentsSave()!=null){
			BreakPaymentsSaveDTO breakPaymentsSaveDTO = paymentTransactionDTO.getBreakPaymentsSave();
			List<BreakPayments> breakPaymentsList = breakPaymentsSaveDTO.getBreakPayments();
			for(BreakPayments breakPayments : breakPaymentsList){

				Map<String,Double> breakUp = null;
				// Temporary Registration 
				if( breakPayments.getFeeType().equalsIgnoreCase(FeeTypeDetails.REGVALUE)){
					breakUp = breakPayments.getBreakup();
					for(Map.Entry<String, Double> entry : breakUp.entrySet()){
						if(entry.getKey().equalsIgnoreCase(ServiceCodeEnum.SERVICE_FEE.getTypeDesc())){
							serviceFee = serviceFee + entry.getValue();
						}
						if(entry.getKey().equalsIgnoreCase(ServiceCodeEnum.REGISTRATION.getTypeDesc())){
							fee = fee + entry.getValue();

						}
					}
				}

				// PR Registration 
				if( breakPayments.getFeeType().equalsIgnoreCase(FeeTypeDetails.FRESH)){
					breakUp = breakPayments.getBreakup();
					for(Map.Entry<String, Double> entry : breakUp.entrySet()){
						if(entry.getKey().equalsIgnoreCase(ServiceCodeEnum.SERVICE_FEE.getTypeDesc())){
							serviceFee = serviceFee + entry.getValue();

						}
						if(entry.getKey().equalsIgnoreCase(ServiceCodeEnum.REGISTRATION.getTypeDesc())){
							fee = fee + entry.getValue();

						}
						if(entry.getKey().equalsIgnoreCase(ServiceCodeEnum.POSTAL_FEE.getTypeDesc())){
							fee = fee + entry.getValue();
							//this feeReportVO is display individual report
							feeReportVO.setRegPostalFee( entry.getValue());
						}
						if(entry.getKey().equalsIgnoreCase(ServiceCodeEnum.CARD.getTypeDesc())){
							fee = fee + entry.getValue();
						}
					}
				}
				//Fc Fees
				if(breakPayments.getFeeType().equalsIgnoreCase(FeeTypeDetails.NEW)){
					breakUp = breakPayments.getBreakup();
					for(Map.Entry<String, Double> entry : breakUp.entrySet()){
						if(entry.getKey().equalsIgnoreCase(ServiceCodeEnum.SERVICE_FEE.getTypeDesc())){
							serviceFee = serviceFee + entry.getValue();
							feeReportVO.setRegFitnessServieceFee(entry.getValue());
						}
						if(entry.getKey().equalsIgnoreCase(ServiceCodeEnum.REGISTRATION.getTypeDesc())){
							fee = fee + entry.getValue();
							feeReportVO.setFitnessFee(entry.getValue());
						}
					}
				}

				// TAX Fees
				if( breakPayments.getFeeType().equalsIgnoreCase(ServiceCodeEnum.LIFE_TAX.getCode())){
					breakUp = breakPayments.getBreakup();
					for(Map.Entry<String, Double> entry : breakUp.entrySet()){
						if(entry.getKey().equalsIgnoreCase(ServiceCodeEnum.LIFE_TAX.getCode())){
							lifeTax = lifeTax + entry.getValue();
						}

					}
				}
				if( breakPayments.getFeeType().equalsIgnoreCase(ServiceCodeEnum.QLY_TAX.getCode())){

					breakUp = breakPayments.getBreakup();
					for(Map.Entry<String, Double> entry : breakUp.entrySet()){
						if(entry.getKey().equalsIgnoreCase(ServiceCodeEnum.QLY_TAX.getCode())){
							quarterlyTax = quarterlyTax + entry.getValue();
						}

					}
				}
				//HPA Fee
				if( breakPayments.getFeeType().equalsIgnoreCase(FeeTypeDetails.HPAFEE)){

					breakUp = breakPayments.getBreakup();
					for(Map.Entry<String, Double> entry : breakUp.entrySet()){
						if(entry.getKey().equalsIgnoreCase(ServiceCodeEnum.REGISTRATION.getTypeDesc())){
							fee = fee + entry.getValue();
						}

					}
				}
			}
			feeReportVO.setRegistrationFee(fee);
			feeReportVO.setRegServiceFee(serviceFee);
		}else if(paymentTransactionDTO.getFeeDetailsDTO()!=null){
			List<FeesDTO> feeDetailsDTO = paymentTransactionDTO.getFeeDetailsDTO().getFeeDetails();
			for(FeesDTO feesDTO:feeDetailsDTO){
				if(feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.SERVICE_FEE.getTypeDesc())){
					serviceFee = serviceFee + feesDTO.getAmount();
					feeReportVO.setRegServiceFee(serviceFee);
				}
				if(feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.REGISTRATION.getTypeDesc())){
					fee = fee + feesDTO.getAmount();
				}
				if(feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.POSTAL_FEE.getTypeDesc())){
					fee = fee + feesDTO.getAmount();
					feeReportVO.setRegPostalFee(feesDTO.getAmount());
				}
				if(feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.CARD.getTypeDesc())){
					fee = fee + feesDTO.getAmount();
				}
				if(feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.LIFE_TAX.getCode())){
					lifeTax = lifeTax + feesDTO.getAmount();
				}
				if(feesDTO.getFeesType().equalsIgnoreCase(ServiceCodeEnum.QLY_TAX.getCode())){
					quarterlyTax = quarterlyTax + feesDTO.getAmount();
				}
			}
			feeReportVO.setRegistrationFee(fee);
		}

		if(!paymentToShow){
			feeReportVO.setCompundingFee(compundingFee);
			feeReportVO.setFee(fee);
			feeReportVO.setLifeTax(lifeTax);
			feeReportVO.setQuarterlyTax(quarterlyTax);
			feeReportVO.setServiceFee(serviceFee);
			return feeReportVO;
		}
		return feeReportVO;
	}

	private Double decimalPoints(Double value){
		if(value>0){
			return Double.parseDouble(new DecimalFormat("##.###").format(value));
		}
		return value;

	}
	
	@Override
	public List<SNReportResultVO> eBiddingCovWiseData(LocalDate fromDate, LocalDate toDate, String cov, String series,
			JwtUser user) {
		List<ResultVo> results=getBiddersCountCovwise(fromDate,toDate,cov);
		if(CommonConstants.ALL.equals(cov)) {
			List<SpecialNumberDetailsDTO> specialNativeList= specialNumberDetailsDAO.findByAllCovsDataNative(fromDate.atStartOfDay(), toDate.atTime(23, 59, 59,999),BidStatus.SPPAYMENTDONE.getDescription());
			return convertSpecialNumbetDetails(specialNativeList,cov,results);
		}
		List<SpecialNumberDetailsDTO> specialNativeList= specialNumberDetailsDAO.findBySpecificCovsDataNative(cov,BidStatus.SPPAYMENTDONE.getDescription(),fromDate.atStartOfDay(), toDate.atTime(23, 59, 59,999));
		return convertSpecialNumbetDetails(specialNativeList,cov,results);
	}


	private List<ResultVo>  getBiddersCountCovwise(LocalDate fromDate, LocalDate toDate, String cov) {
		
		Criteria criteria = Criteria.where("createdDate").gte(fromDate.atStartOfDay()).lte(toDate.atTime(23, 59, 59, 999));
		if(!CommonConstants.ALL.equals(cov)) {
		 criteria =criteria.andOperator(Criteria.where("vehicleDetails.classOfVehicle.covcode").is(cov));
		}
		
		Aggregation agg = newAggregation(unwind("vehicleDetails.classOfVehicle"),
				match(criteria),
				group("vehicleDetails.classOfVehicle.covcode").count().as("count"), 
				project("count").and("covcode").previousOperation()

		);
		AggregationResults<ResultVo> groupResults = mongoTemplate.aggregate(agg, SpecialNumberDetailsDTO.class, ResultVo.class);
		return groupResults.getMappedResults();
	}

	private List<SNReportResultVO> convertSpecialNumbetDetails(List<SpecialNumberDetailsDTO> specialNativeList, String cov,
			List<ResultVo> results) {
		Map<String, List<SpecialNumberDetailsDTO>> specialNumberDtlMap = specialNativeList.stream().collect(Collectors.groupingBy(s->s.getVehicleDetails().getClassOfVehicle().getCovcode()));
		List<SNReportResultVO> output=new ArrayList<>();
		SNReportResultVO totalReportResultVO=new SNReportResultVO();
		specialNumberDtlMap.entrySet().stream().forEach(entry->{
			if(CollectionUtils.isEmpty(entry.getValue())) {
				throw new BadRequestException("No Result Found");
			}
			SNReportResultVO snReportResultVO = new SNReportResultVO();
			snReportResultVO.setCovCode(entry.getKey());
			snReportResultVO.setCovDesc(entry.getValue().get(0).getVehicleDetails().getClassOfVehicle().getCovdescription());
			entry.getValue().stream().forEach(s->{
				snReportResultVO.setNumberRegisterAmount(snReportResultVO.getNumberRegisterAmount()+s.getSpecialNumberFeeDetails().getTotalAmount());
				totalReportResultVO.setNumberRegisterAmount(totalReportResultVO.getNumberRegisterAmount()+s.getSpecialNumberFeeDetails().getTotalAmount());
				if(s.getSpecialNumberFeeDetails().getIsRefundDone()) {
					snReportResultVO.setNumberRefundAmount(snReportResultVO.getNumberRefundAmount()+s.getSpecialNumberFeeDetails().getApplicationAmount());
					totalReportResultVO.setNumberRefundAmount(totalReportResultVO.getNumberRefundAmount()+s.getSpecialNumberFeeDetails().getApplicationAmount());
				}
				if(s.getActionsDetailsLog().stream().anyMatch(a->BidStatus.FINALPAYMENTDONE.getDescription().equals(a.getAction()))) {
					snReportResultVO.setBidAmount(snReportResultVO.getBidAmount()+s.getBidFinalDetails().getBidAmount());
					totalReportResultVO.setBidAmount(totalReportResultVO.getBidAmount()+s.getBidFinalDetails().getBidAmount());
					if(s.getBidFinalDetails().getIsRefundDone()) {
						snReportResultVO.setBidRefundAmount(snReportResultVO.getBidRefundAmount()+s.getBidFinalDetails().getBidAmount());
						totalReportResultVO.setBidRefundAmount(totalReportResultVO.getBidRefundAmount()+s.getBidFinalDetails().getBidAmount());
					}
				}
				s=null;
			});
			entry.getValue().clear();
			snReportResultVO.setTotalAmount(snReportResultVO.getNumberRegisterAmount()+snReportResultVO.getBidAmount()-snReportResultVO.getNumberRefundAmount()-snReportResultVO.getBidRefundAmount());
			totalReportResultVO.setTotalAmount(totalReportResultVO.getTotalAmount()+snReportResultVO.getTotalAmount());
			Optional<ResultVo> resultVoOpt=results.stream().filter(r->entry.getKey().equals(r.getCovcode())).findFirst();
			if(resultVoOpt.isPresent()) {
				snReportResultVO.setNoOfBidders(resultVoOpt.get().getCount());
				totalReportResultVO.setNoOfBidders(totalReportResultVO.getNoOfBidders()+resultVoOpt.get().getCount());
			}
			output.add(snReportResultVO);
		});
		totalReportResultVO.setCovDesc(CommonConstants.TOTAL);
		totalReportResultVO.setCovCode(CommonConstants.TOTAL);
		if(CommonConstants.ALL.equals(cov)) {
			output.add(totalReportResultVO);
		}
		specialNumberDtlMap.clear();
		return output;
	}

	
	@Override
	public void regServiceReportDataSync() {
		
	}
	@SuppressWarnings("unused")
	private class ResultVo{
		
		private String covcode;
		private Integer count;
		private String offices;
		private Integer totalBidFee;
		private Integer totalServiceFee;
		private Integer feeAmountforSN;
		public Integer getTotalBidFee() {
			return totalBidFee;
		}
		public void setTotalBidFee(Integer totalBidFee) {
			this.totalBidFee = totalBidFee;
		}
		public Integer getTotalServiceFee() {
			return totalServiceFee;
		}
		public void setTotalServiceFee(Integer totalServiceFee) {
			this.totalServiceFee = totalServiceFee;
		}
		
		public Integer getFeeAmountforSN() {
			return feeAmountforSN;
		}
		public void setFeeAmountforSN(Integer feeAmountforSN) {
			this.feeAmountforSN = feeAmountforSN;
		}
		public String getOffices() {
			return offices;
		}
		public void setOffices(String offices) {
			this.offices = offices;
		}
		/**
		 * @return the covcode
		 */
		public String getCovcode() {
			return covcode;
		}
		/**
		 * @param covcode the covcode to set
		 */
		public void setCovcode(String covcode) {
			this.covcode = covcode;
		}
		/**
		 * @return the count
		 */
		public Integer getCount() {
			return count;
		}
		/**
		 * @param count the count to set
		 */
		public void setCount(Integer count) {
			this.count = count;
		}
		
		
		
		
	}
	
	@Override
	public List<SNReportResultVO> eBiddingOfficeWiseData(LocalDate fromDate, LocalDate toDate, String officeCode) {
		List<SNReportResultVO> listOfSNRwithOffice = new ArrayList<>();
		OfficeDTO officeDTO = officeService.getDistrictByofficeCode(officeCode);
		List<OfficeVO> officeVOList = officeService.getOfficeByDistrictLimited(officeDTO.getDistrict());
		listOfSNRwithOffice = this.setOfficeNameToSNR(officeVOList);
		List<String> offices = officeVOList.stream().map(office -> office.getOfficeCode()).collect(Collectors.toList());
		List<ResultVo> results = this.getBiddersCountAndFeesOfficeWise(fromDate, toDate, offices);
		listOfSNRwithOffice = this.setSNDataToReport(listOfSNRwithOffice, results);
		return listOfSNRwithOffice;
	}

	public List<SNReportResultVO> setOfficeNameToSNR(List<OfficeVO> officeVOList) {

		List<SNReportResultVO> list = new ArrayList<>();
		officeVOList.forEach(office -> {
			SNReportResultVO sNReportResultVO = new SNReportResultVO();
			sNReportResultVO.setOfficeName(office.getOfficeName());
			sNReportResultVO.setOfficeCode(office.getOfficeCode());
			list.add(sNReportResultVO);
		});

		return list;

	}

	public List<SNReportResultVO> setSNDataToReport(List<SNReportResultVO> listOfSNRwithOffice,
			List<ResultVo> results) {
		listOfSNRwithOffice.forEach(dataSn -> {
			results.forEach(data -> {
				if (dataSn.getOfficeCode() != null && dataSn.getOfficeCode().equals(data.getOffices())) {
					if (data.getTotalServiceFee() != null && data.getFeeAmountforSN() != null && data.getCount() != null
							&& data.getTotalBidFee() != null) {
						dataSn.setNoOfBidders(data.getCount());
						dataSn.setBidAmount(data.getTotalBidFee());
						dataSn.setFeeAmomuntforSN(data.getFeeAmountforSN());
						dataSn.setServiceFee(data.getTotalServiceFee());
						dataSn.setTotalAmount(
								data.getTotalBidFee() + data.getFeeAmountforSN() + data.getTotalServiceFee());

					}
				}
			});
		});

		SNReportResultVO sNReportResultVO = new SNReportResultVO();
		sNReportResultVO.setOfficeCode(CommonConstants.TOTAL);
		sNReportResultVO.setOfficeName(CommonConstants.TOTAL);
		listOfSNRwithOffice.forEach(fee -> {
			sNReportResultVO.setNoOfBidders(sNReportResultVO.getNoOfBidders() + fee.getNoOfBidders());
			sNReportResultVO.setServiceFee(sNReportResultVO.getServiceFee() + fee.getServiceFee());
			sNReportResultVO.setBidAmount(sNReportResultVO.getBidAmount() + fee.getBidAmount());
			sNReportResultVO.setFeeAmomuntforSN(sNReportResultVO.getFeeAmomuntforSN() + fee.getFeeAmomuntforSN());
			sNReportResultVO.setTotalAmount(sNReportResultVO.getTotalAmount() + fee.getTotalAmount());
		});
		listOfSNRwithOffice.add(sNReportResultVO);
		return listOfSNRwithOffice;
	}

	private List<ResultVo> getBiddersCountAndFeesOfficeWise(LocalDate fromDate, LocalDate toDate,
			List<String> offices) {
		List<String> bidStatusList = new ArrayList<String>();
		bidStatusList.add(BidStatus.BIDWIN.getDescription());
		bidStatusList.add(BidStatus.BIDABSENT.getDescription());
		Aggregation agg = newAggregation(
				match(Criteria.where("createdDate").gte(fromDate.atStartOfDay()).lt(toDate.atTime(23, 59, 59, 999))
						.and("bidStatus").in(bidStatusList).and("vehicleDetails.rtaOffice.officeCode").in(offices)),
				group("vehicleDetails.rtaOffice.officeCode").count().as("count")
						.sum("specialNumberFeeDetails.bidFeeMaster.specialNumFee").as("feeAmountforSN")
						.sum("bidFinalDetails.bidAmountNumber").as("totalBidFee")
						.sum("specialNumberFeeDetails.servicesAmount").as("totalServiceFee"),
				project("count", "feeAmountforSN", "totalBidFee", "totalServiceFee").and("offices").previousOperation()

		);

		AggregationResults<ResultVo> groupResults = mongoTemplate.aggregate(agg, SpecialNumberDetailsDTO.class,
				ResultVo.class);
		return groupResults.getMappedResults();
	}

	@Override
	public void generateExcelForEbiddingReports(List<SNReportResultVO> ebidReport, HttpServletResponse response) {
		
		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "E-BiddingReportsList");

		String name = "E-BiddingReportsList";
		String fileName = name + ".xlsx";
		String sheetname = "E-BiddingReportsList";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);
		
		XSSFWorkbook wb = excel.renderData(prepareCellPropsForEbiddingReportsExcel(header, ebidReport), header, fileName,
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

	private List<List<CellProps>> prepareCellPropsForEbiddingReportsExcel(List<String> header,
			List<SNReportResultVO> resultList) {

		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (SNReportResultVO report : resultList) {

			if (report != null) {
				List<CellProps> result = new ArrayList<CellProps>();
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
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getNoOfBidders()));
						break;
					case 3:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getFeeAmomuntforSN()));
						break;
					case 4:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getBidAmount()));
						break;
					case 5:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getServiceFee()));
						break;
					case 6:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getTotalAmount()));
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
}