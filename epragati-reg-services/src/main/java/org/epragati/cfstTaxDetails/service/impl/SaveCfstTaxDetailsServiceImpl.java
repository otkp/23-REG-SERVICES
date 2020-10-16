package org.epragati.cfstTaxDetails.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.epragati.auditService.AuditLogsService;
import org.epragati.cfstTaxDetails.service.SaveCfstTaxDetailsService;
import org.epragati.constants.Schedulers;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.TaxDetailsDAO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.TaxComponentDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.regservice.RegistrationService;
import org.epragati.regservice.vo.CfstTaxDetailsVO;
import org.epragati.reports.service.RCCancellationService;
import org.epragati.tax.vo.TaxStatusEnum;
import org.epragati.tax.vo.TaxTypeEnum;
import org.epragati.util.payment.ModuleEnum;
import org.epragati.util.payment.ServiceCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
public class SaveCfstTaxDetailsServiceImpl implements SaveCfstTaxDetailsService{

	private static final Logger logger = LoggerFactory.getLogger(SaveCfstTaxDetailsServiceImpl.class);

	@Autowired
	private TaxDetailsDAO taxDetailsDAO;

	@Autowired
	private AuditLogsService auditLogsService;

	@Autowired
	private RegistrationService registrationService;

	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;
	
	@Autowired
	private RCCancellationService rcCancellationService;

	@Value("${reg.cfst.tax.authString:eyJhbGfhgjdEPRAGAthixMiJ9.eRTAU0xFU0hvbSAIRCFSTUxMz00OTkHHRTAXFHJjhsdfghsdMRVIiLCJle}")
	private String localAuthString;
	@Override
	public Pair<String,String> saveCfstTaxDetails(CfstTaxDetailsVO cfstTaxDetailsVO,String authString,HttpServletRequest request) {

		Boolean isExecuteSucess=true;
		LocalDateTime startTime=LocalDateTime.now();

		logger.info("start to save tax details", cfstTaxDetailsVO.getVehicleNo());

		synchronized (cfstTaxDetailsVO.getVehicleNo().intern()) {
			
			cfstTaxDetailsVO.setIpAddress(request.getRemoteAddr());
			if(!authString.equals(localAuthString)) {
				logger.error("Please provide correct authorization code : [{}] ", cfstTaxDetailsVO.getVehicleNo());
				auditLogsService.saveScedhuleLogs(Schedulers.CFSTTAXSCHEDULER, startTime, LocalDateTime.now(), isExecuteSucess, "Please provide correct authorization code : "+ cfstTaxDetailsVO.getVehicleNo(),
						Arrays.asList(request.getRemoteAddr()));
				throw new BadRequestException("Please provide correct authorization code: "+ cfstTaxDetailsVO.getVehicleNo());
			}
			RegistrationDetailsDTO regDto =null;
			List<RegistrationDetailsDTO> registrationDetailsDTOs = registrationDetailDAO.findByPrNoIn(Arrays.asList(cfstTaxDetailsVO.getVehicleNo().toUpperCase()));
			if(registrationDetailsDTOs.isEmpty()) {
				cfstTaxDetailsVO.setApplicationNotFound(Boolean.TRUE);
			}else {
				boolean lastUpdatedFieldNotFound = Boolean.FALSE;
				for(RegistrationDetailsDTO dto : registrationDetailsDTOs) {
					if(dto.getlUpdate() == null) {
						lastUpdatedFieldNotFound = Boolean.TRUE;
					}
				}
				if(lastUpdatedFieldNotFound) {
					cfstTaxDetailsVO.setApplicationNotFound(Boolean.TRUE);
				}else {
					registrationDetailsDTOs.sort((r1,r2)->r2.getlUpdate().compareTo(r1.getlUpdate()));
					regDto = registrationDetailsDTOs.stream().findFirst().get();
				}
			}

			Pair<String,String> prNoAndApplicationNo = saveCitizenTaxDetails( cfstTaxDetailsVO, regDto);

			auditLogsService.saveScedhuleLogs(Schedulers.CFSTTAXSCHEDULER, startTime, LocalDateTime.now(), isExecuteSucess, StringUtils.EMPTY, 
					Arrays.asList(request.getRemoteAddr()));

			registrationDetailsDTOs.clear();
			return prNoAndApplicationNo;

		}
	}


	public Pair<String,String> saveCitizenTaxDetails(CfstTaxDetailsVO cfstTaxDetailsVO,RegistrationDetailsDTO regDto) {
		// need to update pr and second vehicel tax and flag
		TaxDetailsDTO dto = new TaxDetailsDTO();
		if (cfstTaxDetailsVO.getTaxAmt() == null 
				&& cfstTaxDetailsVO.getCessFee() == null 
				&& cfstTaxDetailsVO.getGreenTaxAmount() == null) {
			logger.error("Amount not found from cfst");
			throw new BadRequestException("Amount not found from cfst: "+ cfstTaxDetailsVO.getVehicleNo().toUpperCase());
		}

		List<Map<String, TaxComponentDTO>> taxDetails = new ArrayList<>();

		if(cfstTaxDetailsVO.getApplicationNotFound() != null && cfstTaxDetailsVO.getApplicationNotFound()) {
			dto.setApplicationNotFound(cfstTaxDetailsVO.getApplicationNotFound());
		}else {
			dto.setApplicationNo(regDto.getApplicationNo());
			if(regDto.getOwnerType() != null) {
				dto.setCovCategory(regDto.getOwnerType());
			}
			if(StringUtils.isNoneBlank(regDto.getTrNo())) {
				dto.setTrNo(regDto.getTrNo());
			}
		}

		dto.setModule(ModuleEnum.REG.getCode());
		dto.setPaymentPeriod(TaxTypeEnum.CfstTaxType.getdesc(cfstTaxDetailsVO.getTaxPaymentPrd().getCode()));


		dto.setPrNo(cfstTaxDetailsVO.getVehicleNo().toUpperCase());


		if (cfstTaxDetailsVO.getPayTaxType() != null) {
			dto.setPayTaxType(cfstTaxDetailsVO.getPayTaxType());
		}
		// Tax
		// TODO : Change to Enum
		if (cfstTaxDetailsVO.getTaxAmt() != null ) {

			if(cfstTaxDetailsVO.getTaxAmt()<0) {
				logger.error("Tax amount should be greater than or equal zero: [{}]",cfstTaxDetailsVO.getTaxAmt());
				throw new BadRequestException("Tax amount should be greater than or equal zero: "+ cfstTaxDetailsVO.getTaxAmt());
			}
			dto.setTaxAmount(cfstTaxDetailsVO.getTaxAmt());
			if(cfstTaxDetailsVO.getTaxValTo() == null) {
				logger.error("Tax valid Tup to not found from cfst");
				throw new BadRequestException("Tax valid Tup to not found from cfst: "+ cfstTaxDetailsVO.getVehicleNo());
			}
			if(cfstTaxDetailsVO.getTaxValfrom() == null) {
				logger.error("Tax valid from not found from cfst");
				throw new BadRequestException("Tax valid from not found from cfst: "+ cfstTaxDetailsVO.getVehicleNo());
			}
			dto.setTaxPeriodFrom(cfstTaxDetailsVO.getTaxValfrom());
			dto.setTaxPeriodEnd(cfstTaxDetailsVO.getTaxValTo());
			Double taxArrears = 0d;
			Long penalty = 0l;
			Long penaltyArrears = 0l;
			if (cfstTaxDetailsVO.getTaxArrears() != null)
				taxArrears = Double.valueOf(cfstTaxDetailsVO.getTaxArrears().toString());
			if (cfstTaxDetailsVO.getPenaltyAmt() != null)
				penalty = cfstTaxDetailsVO.getPenaltyAmt();
			if (cfstTaxDetailsVO.getPenaltyArrears() != null)
				penaltyArrears = cfstTaxDetailsVO.getPenaltyArrears();

			this.addTaxDetails(TaxTypeEnum.CfstTaxType.getdesc(cfstTaxDetailsVO.getTaxPaymentPrd().getCode()), Double.valueOf(cfstTaxDetailsVO.getTaxAmt().toString()),
					cfstTaxDetailsVO.getTaxPaymentDt().atStartOfDay(), cfstTaxDetailsVO.getTaxValTo(), cfstTaxDetailsVO.getTaxValfrom(), taxDetails, penalty,
					Double.valueOf(taxArrears), penaltyArrears);


		}

		if(StringUtils.isNoneBlank(cfstTaxDetailsVO.getPermitType())) {
			dto.setPermitType(cfstTaxDetailsVO.getPermitType());
		}


		if (cfstTaxDetailsVO.getCessFee() != null ) {
			if(cfstTaxDetailsVO.getCessPeriodEnd() == null) {
				logger.error("cess Period end not found from cfst");
				throw new BadRequestException("cess Period end not found from cfst: "+ cfstTaxDetailsVO.getVehicleNo());
			}
			if(cfstTaxDetailsVO.getCessPeriodFrom() == null) {
				logger.error("cess Period  from not found from cfst");
				throw new BadRequestException("cess Period  from not found from cfst: "+ cfstTaxDetailsVO.getVehicleNo());
			}
			if(cfstTaxDetailsVO.getCessFee()<0) {
				logger.error("Cess amount should be greater than or equal zero");
				throw new BadRequestException("Cess amount should be greater than or equal zero: "+ cfstTaxDetailsVO.getCessFee());
			}
			dto.setCessFee(cfstTaxDetailsVO.getCessFee() );
			dto.setCessPeriodEnd(cfstTaxDetailsVO.getCessPeriodEnd());
			dto.setCessPeriodFrom(cfstTaxDetailsVO.getCessPeriodFrom());

			this.addTaxDetails(ServiceCodeEnum.CESS_FEE.getCode(),
					Double.valueOf(cfstTaxDetailsVO.getCessFee().toString()),cfstTaxDetailsVO.getTaxPaymentDt().atStartOfDay(),
					cfstTaxDetailsVO.getCessPeriodEnd(), cfstTaxDetailsVO.getCessPeriodFrom(), taxDetails, null, null, null);
		}
		if (cfstTaxDetailsVO.getGreenTaxAmount() != null ) {

			if(cfstTaxDetailsVO.getGreenTaxPeriodEnd() == null) {
				throw new BadRequestException("green tax Period end not  found from cfst: "+ cfstTaxDetailsVO.getVehicleNo());
			}
			if(cfstTaxDetailsVO.getGreenTaxPeriodFrom() == null) {
				throw new BadRequestException("green tax Period from not found from cfst: "+ cfstTaxDetailsVO.getVehicleNo());
			}
			if(cfstTaxDetailsVO.getGreenTaxAmount()<0) {
				throw new BadRequestException("Green tax amount should be greater than or equal zero: "+ cfstTaxDetailsVO.getGreenTaxAmount());
			}
			dto.setGreenTaxAmount(cfstTaxDetailsVO.getGreenTaxAmount());
			dto.setGreenTaxPeriodEnd(cfstTaxDetailsVO.getGreenTaxPeriodEnd());
			dto.setGreenTaxPeriodFrom(cfstTaxDetailsVO.getGreenTaxPeriodFrom());
			this.addTaxDetails(ServiceCodeEnum.GREEN_TAX.getCode(),
					Double.valueOf(cfstTaxDetailsVO.getGreenTaxAmount().toString()), cfstTaxDetailsVO.getTaxPaymentDt().atStartOfDay(),
					cfstTaxDetailsVO.getGreenTaxPeriodEnd(), cfstTaxDetailsVO.getGreenTaxPeriodFrom(), taxDetails, null, null, null);
		}
		dto.setCreatedDate(cfstTaxDetailsVO.getTaxPaymentDt().atStartOfDay());
		dto.setTaxPaidDate(cfstTaxDetailsVO.getTaxPaymentDt());

		dto.setClassOfVehicle(cfstTaxDetailsVO.getClassOfVehicle());

		dto.setCfstApplicationNo(cfstTaxDetailsVO.getApplicationNo());
		dto.setTaxDetails(taxDetails);
		dto.setTaxAmount(cfstTaxDetailsVO.getCollectedAmt());
		dto.setServiceFee(cfstTaxDetailsVO.getServiceFee());
		dto.setSource("CFST");
		dto.setTaxStatus(TaxStatusEnum.ACTIVE.getCode());
		dto.setRemarks("");
		dto.setOfficeCode(cfstTaxDetailsVO.getTaxPaidOffCd());
		dto.setStateCode("AP");
		dto.setIpAddress(cfstTaxDetailsVO.getIpAddress());
		taxDetailsDAO.save(dto);
		rcCancellationService.nonPaymentMoveToHistory(dto);
		return Pair.of(cfstTaxDetailsVO.getVehicleNo(), cfstTaxDetailsVO.getApplicationNo());
	}

	private void addTaxDetails(String taxType, Double taxAmount, LocalDateTime paidDate, LocalDate validityTo,
			LocalDate validityFrom, List<Map<String, TaxComponentDTO>> list, Long penalty, Double taxArrears,
			Long penaltyArrears) {

		Map<String, TaxComponentDTO> taxMap = new HashMap<>();

		TaxComponentDTO tax = new TaxComponentDTO();
		tax.setTaxName(taxType);
		tax.setAmount(taxAmount);
		tax.setPaidDate(paidDate);
		tax.setValidityFrom(validityFrom);
		tax.setValidityTo(validityTo);
		if (penalty != null && penalty != 0) {
			tax.setPenalty(penalty);
		}
		if (penaltyArrears != null && penaltyArrears != 0) {
			tax.setPenaltyArrears(penaltyArrears);
		}
		if (taxArrears != null && taxArrears != 0) {
			tax.setTaxArrears(taxArrears);
		}
		taxMap.put(taxType, tax);
		list.add(taxMap);

	}
}
