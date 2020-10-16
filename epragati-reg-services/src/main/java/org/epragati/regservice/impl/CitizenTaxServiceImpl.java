package org.epragati.regservice.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.aadhaar.seed.service.AadharSeeding;
import org.epragati.common.dao.PropertiesDAO;
import org.epragati.common.dto.PropertiesDTO;
import org.epragati.constants.OwnerTypeEnum;
import org.epragati.constants.PurposeEnum;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.AlterationDAO;
import org.epragati.master.dao.BileteralTaxDAO;
import org.epragati.master.dao.FinalTaxHelperDAO;
import org.epragati.master.dao.MasterAmountSecoundCovsDAO;
import org.epragati.master.dao.MasterGreenTaxDAO;
import org.epragati.master.dao.MasterGreenTaxFuelexcemptionDAO;
import org.epragati.master.dao.MasterNewGoTaxDetailsDAO;
import org.epragati.master.dao.MasterPayperiodDAO;
import org.epragati.master.dao.MasterTaxBasedDAO;
import org.epragati.master.dao.MasterTaxDAO;
import org.epragati.master.dao.MasterTaxExcemptionsDAO;
import org.epragati.master.dao.MasterTaxForVoluntaryDAO;
import org.epragati.master.dao.MasterTaxFuelTypeExcemptionDAO;
import org.epragati.master.dao.MasterWeightsForAltDAO;
import org.epragati.master.dao.RegServiceDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.StagingRegistrationDetailsDAO;
import org.epragati.master.dao.TaxDetailsDAO;
import org.epragati.master.dto.FinalTaxHelper;
import org.epragati.master.dto.InvoiceDetailsDTO;
import org.epragati.master.dto.MasterAmountSecoundCovsDTO;
import org.epragati.master.dto.MasterGreenTax;
import org.epragati.master.dto.MasterGreenTaxFuelexcemption;
import org.epragati.master.dto.MasterNewGoTaxDetails;
import org.epragati.master.dto.MasterPayperiodDTO;
import org.epragati.master.dto.MasterTax;
import org.epragati.master.dto.MasterTaxBased;
import org.epragati.master.dto.MasterTaxExcemptionsDTO;
import org.epragati.master.dto.MasterTaxForVoluntary;
import org.epragati.master.dto.MasterTaxFuelTypeExcemptionDTO;
import org.epragati.master.dto.MasterWeightsForAlt;
import org.epragati.master.dto.OffenceDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.dto.TaxComponentDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.master.dto.TaxHelper;
import org.epragati.master.dto.TrailerChassisDetailsDTO;
import org.epragati.master.dto.VahanDetailsDTO;
import org.epragati.payment.dto.FeeDetailsDTO;
import org.epragati.payment.dto.FeesDTO;
import org.epragati.payment.dto.PaymentTransactionDTO;
import org.epragati.payments.dao.PaymentTransactionDAO;
import org.epragati.payments.vo.BreakPayments;
import org.epragati.permits.dao.PermitDetailsDAO;
import org.epragati.permits.dto.PermitDetailsDTO;
import org.epragati.regservice.CitizenTaxService;
import org.epragati.regservice.RegistrationService;
import org.epragati.regservice.dto.ActionDetails;
import org.epragati.regservice.dto.AlterationDTO;
import org.epragati.regservice.dto.BileteralTaxDTO;
import org.epragati.regservice.dto.CitizenFeeDetailsInput;
import org.epragati.regservice.dto.NOCDetailsDTO;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.mapper.RegServiceMapper;
import org.epragati.regservice.vo.RegServiceVO;
import org.epragati.stagecarriages.dto.MasterStageCarriageTaxDTO;
import org.epragati.stagecarriageservices.dao.MasterStageCarriageTaxDAO;
import org.epragati.tax.service.TaxService;
import org.epragati.tax.vo.TaxCalculationHelper;
import org.epragati.tax.vo.TaxTypeEnum;
import org.epragati.tax.vo.TaxTypeEnum.VoluntaryTaxType;
import org.epragati.util.DateConverters;
import org.epragati.util.PermitsEnum;
import org.epragati.util.PermitsEnum.PermitRouteCodeEnum;
import org.epragati.util.PermitsEnum.PermitType;
import org.epragati.util.RoleEnum;
import org.epragati.util.StatusRegistration;
import org.epragati.util.payment.ClassOfVehicleEnum;
import org.epragati.util.payment.GatewayTypeEnum;
import org.epragati.util.payment.OtherStateApplictionType;
import org.epragati.util.payment.PayStatusEnum;
import org.epragati.util.payment.ServiceCodeEnum;
import org.epragati.util.payment.ServiceEnum;
import org.epragati.vcr.service.VcrService;
import org.epragati.vcrImage.dao.VcrFinalServiceDAO;
import org.epragati.vcrImage.dao.VoluntaryTaxDAO;
import org.epragati.vcrImage.dto.VcrFinalServiceDTO;
import org.epragati.vcrImage.dto.VoluntaryTaxDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
public class CitizenTaxServiceImpl implements CitizenTaxService {

	private static final Logger logger = LoggerFactory.getLogger(CitizenTaxServiceImpl.class);

	@Autowired
	private MasterPayperiodDAO masterPayperiodDAO;

	@Autowired
	private RegServiceDAO regServiceDAO;

	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;

	@Autowired
	private MasterTaxBasedDAO masterTaxBasedDAO;

	@Autowired
	private MasterTaxDAO taxTypeDAO;

	@Autowired
	private TaxDetailsDAO taxDetailsDAO;
	@Autowired
	private StagingRegistrationDetailsDAO stagingRegistrationDetailsDAO;

	@Autowired
	private MasterTaxExcemptionsDAO masterTaxExcemptionsDAO;
	@Autowired
	private PaymentTransactionDAO paymentTransactionDAO;
	@Autowired

	private PropertiesDAO propertiesDAO;

	@Value("${reg.fresh.stateCode}")
	private String stateCode;

	@Value("${reg.fresh.status}")
	private String regStatus;

	@Value("${reg.fresh.vehicle.age}")
	private float freshVehicleAge;

	@Value("${reg.fresh.vehicle.amount}")
	private Integer amount;

	@Value("${reg.fresh.permitcode}")
	private String permitcode;

	@Value("${reg.fresh.reg.otherState}")
	private String otherState;

	@Value("${reg.fresh.reg.lifeTaxCode}")
	private String lifeTaxCode;

	@Value("${reg.fresh.reg.quarterlyCode}")
	private String quarterlyCode;

	@Value("${reg.fresh.reg.bothCode}")
	private String bothCode;

	@Value("${reg.fresh.reg.seatingCapacityCode}")
	private String seatingCapacityCode;

	@Value("${reg.fresh.reg.ulwCode}")
	private String ulwCode;

	@Value("${reg.fresh.reg.rlwCode}")
	private String rlwCode;

	@Value("${reg.fresh.reg.battery}")
	private String battery;
	@Value("${reg.fresh.reg.electric:ELECTRIC}")
	private String electric;

	@Autowired
	private AlterationDAO alterationDao;

	@Autowired
	private MasterAmountSecoundCovsDAO masterAmountSecoundCovsDAO;

	@Autowired
	private MasterTaxFuelTypeExcemptionDAO masterTaxFuelTypeExcemptionDAO;

	@Autowired
	private MasterGreenTaxDAO masterGreenTaxDAO;

	@Autowired
	private RegServiceMapper regServiceMapper;

	@Autowired
	private MasterGreenTaxFuelexcemptionDAO masterGreenTaxFuelexcemptionDAO;

	@Autowired
	private PermitDetailsDAO permitDetailsDAO;

	@Autowired
	private FinalTaxHelperDAO finalTaxHelperDAO;

	@Autowired
	private AadharSeeding aadharSeeding;

	@Autowired
	private MasterNewGoTaxDetailsDAO masterNewGoTaxDetailsDAO;

	@Autowired
	private RegistrationService registrationService;

	@Autowired
	private MasterWeightsForAltDAO masterWeightsForAltDAO;

	@Autowired
	private BileteralTaxDAO bileteralTaxDAO;

	// private Double penality = 0d;

	// private boolean otherStateFlag = true;
	@Autowired
	private VcrFinalServiceDAO vcrFinalServiceDAO;

	@Autowired
	private VcrService vcrService;
	@Autowired
	private VoluntaryTaxDAO voluntaryTaxDAO;
	@Autowired
	private MasterTaxForVoluntaryDAO masterTaxForVoluntaryDAO;
	@Autowired
	private MasterStageCarriageTaxDAO masterStageCarriageTaxDAO;

	@Override
	public TaxHelper getTaxDetails(String applicationNo, boolean isApplicationFromMvi, boolean isChassesApplication,
			String taxType, boolean isOtherState, String CitizenapplicationNo, List<ServiceEnum> serviceEnum,
			String permitTypeCode, String routeCode, Boolean isWeightAlt, String purpose, List<String> listOfVcrs,String oldPrNo,boolean specificVcrPayment) {

		Optional<MasterPayperiodDTO> Payperiod = Optional.empty();
		Optional<MasterTaxBased> taxCalBasedOn = Optional.empty();
		RegServiceDTO regServiceDTO = null;
		RegistrationDetailsDTO registrationDetails = null;
		StagingRegistrationDetailsDTO stagingRegistrationDetails = null;
		String classOfvehicle = null;
		Integer gvw = null;
		TaxHelper quaterTax = null;
		Boolean mtltOrLtct = Boolean.FALSE;
		Boolean noApplication = Boolean.FALSE;
		Boolean considerStaging = Boolean.FALSE;
		Boolean vcr = Boolean.FALSE;
		boolean isUnRegestered = Boolean.FALSE;
		boolean isregestered = Boolean.FALSE;
		Pair<List<VcrFinalServiceDTO>, CitizenFeeDetailsInput> listOfVcrsDetails = null;
		TaxTypeEnum.TaxPayType payTaxType = TaxTypeEnum.TaxPayType.REG;
		if (isApplicationFromMvi || isChassesApplication) {
			payTaxType = TaxTypeEnum.TaxPayType.DIFF;
		}
		Optional<AlterationDTO> alterDetails = Optional.empty();
		if (serviceEnum != null && !serviceEnum.isEmpty()
				&& serviceEnum.stream().anyMatch(type -> type.equals(ServiceEnum.BILLATERALTAX))) {
			return getBilateralTax(CitizenapplicationNo, serviceEnum, taxType, purpose);
		} else if (serviceEnum != null && !serviceEnum.isEmpty()
				&& serviceEnum.stream().anyMatch(type -> type.equals(ServiceEnum.FEECORRECTION))) {
			return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), 0l, 0d, lifTaxValidityCal(), 0l, 0d, payTaxType, "",
					0d,null);
		} else if (serviceEnum != null && !serviceEnum.isEmpty()
				&& serviceEnum.stream().anyMatch(type -> type.equals(ServiceEnum.VCR))) {
			
			listOfVcrsDetails = getVcrDetails(listOfVcrs,specificVcrPayment);
			listOfVcrs = null;
			if (StringUtils.isNoneBlank(taxType) && taxType.equals(ServiceCodeEnum.CESS_FEE.getCode()) &&( 
					listOfVcrsDetails.getFirst().stream().findFirst().get().getRegistration().isOtherState()||
					StringUtils.isBlank(listOfVcrsDetails.getFirst().stream().findFirst().get().getRegistration().getRegApplicationNo()))) {
				
				return null;
			}
			/*
			 * VcrFinalServiceDTO vcrDto =
			 * listOfVcrsDetails.getFirst().stream().findFirst().get();
			 * Optional<RegistrationDetailsDTO> regServiceOptional =
			 * registrationDetailDAO.findByPrNo(vcrDto.getRegistration().getRegNo());
			 * applicationNo = regServiceOptional.get().getApplicationNo();
			 */
			stagingRegistrationDetails = listOfVcrsDetails.getSecond().getStagingRegistrationDetails();
			registrationDetails = listOfVcrsDetails.getSecond().getRegDetails();
			regServiceDTO = listOfVcrsDetails.getSecond().getRegServiceDetails();
			noApplication = listOfVcrsDetails.getSecond().isNoApplication();
			isUnRegestered = listOfVcrsDetails.getSecond().isUnRegestered();
			isregestered = listOfVcrsDetails.getSecond().isRegestered();
			isOtherState = listOfVcrsDetails.getSecond().getIsOtherState();
			if (isOtherState) {
				applicationNo = listOfVcrsDetails.getFirst().stream().findFirst().get().getRegistration()
						.getRegApplicationNo();
			}
			vcr = Boolean.TRUE;
			// TODO need to check cov for play as another cov

			if (!listOfVcrsDetails.getSecond().getIsOtherState() && isUnRegestered
					&& !listOfVcrsDetails.getSecond().isNoApplication()) {

				classOfvehicle = stagingRegistrationDetails.getClassOfVehicle();
				isregestered = Boolean.TRUE;

			}
			VcrFinalServiceDTO vcrDto = listOfVcrsDetails.getFirst().stream().findFirst().get();
			if(vcrDto.getRegistration().isOtherState() && StringUtils.isNoneBlank(vcrDto.getPilledCov())) {
				classOfvehicle = vcrDto.getPilledCov();
			}
			if (classOfvehicle == null) {
				classOfvehicle = vcrDto.getRegistration()
						.getClasssOfVehicle().getCovcode();
			}
			if(vcrDto.getRegistration().isOtherState()) {
				boolean skipTaxForGoodsVeh = Boolean.TRUE;
				if(listOfVcrsDetails.getSecond().isIntrastate()||listOfVcrsDetails.getSecond().isNocIssued()) {
					skipTaxForGoodsVeh=Boolean.FALSE;
				}else {
				for(VcrFinalServiceDTO dto:listOfVcrsDetails.getFirst()) {
					for(OffenceDTO offence:dto.getOffence().getOffence()){
						if(offence.getOffenceDescription().equalsIgnoreCase("Without Payment of Tax") ) {
							skipTaxForGoodsVeh=Boolean.FALSE;
							break;
						}
						}
				}
				}
				if(skipTaxForGoodsVeh) {
					return null;
				}
			}
		}
		if (isChassesApplication) {
			Optional<StagingRegistrationDetailsDTO> stagingOptional = stagingRegistrationDetailsDAO
					.findByApplicationNo(applicationNo);
			if (stagingOptional.isPresent()) {
				stagingRegistrationDetails = stagingOptional.get();
			} else {
				logger.error("No record found in Reg Service for:[{}] " + applicationNo);
				throw new BadRequestException("No record found in Reg Service for:[{}] " + applicationNo);
			}
			// need to call body builder cov
			alterDetails = alterationDao.findByApplicationNo(stagingRegistrationDetails.getApplicationNo());
			if (!alterDetails.isPresent()) {
				throw new BadRequestException(
						"No record found in alteration for: " + stagingRegistrationDetails.getApplicationNo());
			}

			classOfvehicle = alterDetails.get().getCov();
			gvw = stagingRegistrationDetails.getVahanDetails().getGvw();
			Payperiod = masterPayperiodDAO.findByCovcode(alterDetails.get().getCov());
		} else if (isApplicationFromMvi) {

			Optional<RegServiceDTO> regServiceOptional = regServiceDAO.findByApplicationNo(CitizenapplicationNo);
			if (regServiceOptional.isPresent()) {
				regServiceDTO = regServiceOptional.get();
			} else {
				logger.error("No record found in Reg Service for:[{}] " + CitizenapplicationNo);
				throw new BadRequestException("No record found in Reg Service for:[{}] " + CitizenapplicationNo);
			}
			if (regServiceDTO.getAlterationDetails() != null && regServiceDTO.getAlterationDetails().getCov() != null) {

				classOfvehicle = regServiceDTO.getAlterationDetails().getCov();
				Payperiod = masterPayperiodDAO.findByCovcode(regServiceDTO.getAlterationDetails().getCov());
			} else {
				classOfvehicle = regServiceDTO.getRegistrationDetails().getClassOfVehicle();
				Payperiod = masterPayperiodDAO
						.findByCovcode(regServiceDTO.getRegistrationDetails().getClassOfVehicle());
			}
			gvw = regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw();
			if (StringUtils.isNoneBlank(classOfvehicle)
					&& (classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())
							|| (classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode()) && gvw <= 3000)
							|| classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())
							|| classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TRTT.getCovCode())
							|| classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TRCN.getCovCode()))&&
					(regServiceDTO.getAlterationDetails() != null && regServiceDTO.getAlterationDetails().getCov() != null
						&& (regServiceDTO.getRegistrationDetails().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())
								|| (regServiceDTO.getRegistrationDetails().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode()) && 
										regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw() <= 3000)
								|| regServiceDTO.getRegistrationDetails().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())
								|| regServiceDTO.getRegistrationDetails().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TRTT.getCovCode())
								|| regServiceDTO.getRegistrationDetails().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TRCN.getCovCode())))) {
				return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), 0l, 0d, lifTaxValidityCal(), 0l, 0d, payTaxType, "",
						0d,null);
			}
		} else if (isOtherState) {// OTHERSTATE
			Optional<RegServiceDTO> regServiceOptional = regServiceDAO.findByApplicationNo(applicationNo);
			if (!regServiceOptional.isPresent()) {
				logger.error("No record found in Reg Service for:[{}] " + applicationNo);
				throw new BadRequestException("No record found in Reg Service for:[{}] " + applicationNo);
			}
			regServiceDTO = regServiceOptional.get();
			classOfvehicle = regServiceDTO.getRegistrationDetails().getClassOfVehicle();
			gvw = regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw();
			// After MVI
			if ((regServiceDTO.getRegistrationDetails().getClassOfVehicle()
					.equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
					|| regServiceDTO.getRegistrationDetails().getClassOfVehicle()
							.equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode()))
					&& regServiceDTO.isMviDone()) {
				alterDetails = alterationDao.findByApplicationNo(regServiceDTO.getApplicationNo());
				if (!alterDetails.isPresent()) {
					throw new BadRequestException(
							"No record found in alteration details for: " + regServiceDTO.getApplicationNo());
				}

				classOfvehicle = alterDetails.get().getCov();
			}
			Payperiod = masterPayperiodDAO.findByCovcode(classOfvehicle);

		} else if (vcr) {
			if (noApplication) {
				VcrFinalServiceDTO vcrDto = listOfVcrsDetails.getFirst().stream().findFirst().get();
				// TODO need to check cov for play as another cov
				Payperiod = masterPayperiodDAO
						.findByCovcode(classOfvehicle);
			
				if((classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.MTLT.getCovCode())||classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.LTCT.getCovCode())
						||classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.STCT.getCovCode()))&&
						listOfVcrsDetails.getFirst().stream().findFirst().get().getRegistration().isOtherState() && !listOfVcrsDetails.getSecond().isNocIssued()) {
					Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
				}
				gvw = vcrDto.getRegistration().getGvwc();
				if (StringUtils.isNoneBlank(classOfvehicle)
						&& (classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())
								|| (classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode()) && gvw <= 3000)
								|| classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())
								|| classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TRTT.getCovCode())
								|| classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TRCN.getCovCode()))) {
					if(StringUtils.isNoneBlank(listOfVcrsDetails.getSecond().getNewCov())) {
						listOfVcrsDetails.getSecond().setOldCovLife(Boolean.TRUE);
						classOfvehicle = listOfVcrsDetails.getSecond().getNewCov();
					}
				}
				
			} else {
				applicationNo = listOfVcrsDetails.getFirst().stream().findFirst().get().getRegistration().getRegApplicationNo();
				classOfvehicle = listOfVcrsDetails.getSecond().getRegDetails().getClassOfVehicle();
				gvw = listOfVcrsDetails.getSecond().getRegDetails().getVahanDetails().getGvw();
				if(listOfVcrsDetails.getSecond().isVcrChassisTax()) {
					classOfvehicle = listOfVcrsDetails.getSecond().getAlterDetails().getCov();
					gvw =  listOfVcrsDetails.getSecond().getAlterDetails().getGvw();
					isChassesApplication = Boolean.TRUE;
					alterDetails =Optional.of(listOfVcrsDetails.getSecond().getAlterDetails());
					alterDetails.get().setFromSeatingCapacity(listOfVcrsDetails.getSecond().getAlterDetails().getSeating());
				}
				Payperiod = masterPayperiodDAO
						.findByCovcode(classOfvehicle);
				
				/*Pair<Optional<MasterPayperiodDTO>, Boolean> payperiodAndGoStatus = getPayPeroidForBoth(Payperiod,
						listOfVcrsDetails.getSecond().getRegDetails().getVahanDetails().getSeatingCapacity(),
						listOfVcrsDetails.getSecond().getRegDetails().getVahanDetails().getGvw());*/
				if (classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.OBPN.getCovCode())&&StringUtils.isNoneBlank(listOfVcrsDetails.getSecond().getNewCov())) {
					Pair<Optional<MasterPayperiodDTO>, Boolean> payperiodAndGoStatus = getPayPeroidForBoth(Payperiod,
							listOfVcrsDetails.getSecond().getRegDetails().getVahanDetails().getSeatingCapacity(),
							listOfVcrsDetails.getSecond().getRegDetails().getVahanDetails().getGvw());
				}
				if (this.allowForQuarterTax(classOfvehicle)) {
				
					List<TaxDetailsDTO> listOfTaxDetails = taxDetailsDAO
							.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(applicationNo,
									Arrays.asList(TaxTypeEnum.LifeTax.getDesc()));
					if (listOfTaxDetails == null || listOfTaxDetails.isEmpty()) {
						Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
					
						
					}
				if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.LifeTax.getCode())) {
					if (StringUtils.isNoneBlank(listOfVcrsDetails.getSecond().getNewCov())) {
						listOfVcrsDetails.getSecond().setOldCovLife(Boolean.TRUE);
						Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
						classOfvehicle = listOfVcrsDetails.getSecond().getNewCov();
					}
				}
			

			}else if(Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.LifeTax.getCode()) && StringUtils.isNoneBlank(listOfVcrsDetails.getSecond().getNewCov())){
				listOfVcrsDetails.getSecond().setOldCovLife(Boolean.TRUE);
				Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
				classOfvehicle = listOfVcrsDetails.getSecond().getNewCov();
			}
			}
			if (StringUtils.isNoneBlank(classOfvehicle)
					&& (classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())
							|| (classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode()) && gvw <= 3000)
							|| classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())
							|| classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TRTT.getCovCode())
							|| classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TRCN.getCovCode()))) {
				if(StringUtils.isNoneBlank(listOfVcrsDetails.getSecond().getNewCov())) {
					listOfVcrsDetails.getSecond().setOldCovLife(Boolean.TRUE);
					classOfvehicle = listOfVcrsDetails.getSecond().getNewCov();
				}
			}
		} else {
			Optional<RegistrationDetailsDTO> regOptional = registrationDetailDAO.findByApplicationNo(applicationNo);
			if (!regOptional.isPresent()) {
				logger.error("No record found in Reg Service for:[{}] " + applicationNo);
				throw new BadRequestException("No record found in Reg Service for:[{}] " + applicationNo);
			}
			registrationDetails = regOptional.get();
			if(serviceEnum != null && !serviceEnum.isEmpty()
					&& serviceEnum.stream().anyMatch(type -> type.equals(ServiceEnum.STAGECARRIAGEREPLACEMENTOFVEHICLE))) {
				if(StringUtils.isBlank(oldPrNo)) {
					logger.error("Please provide vehicle number for stage carriage permit");
					throw new BadRequestException("Please provide vehicle number for stage carriage permit");
				}
				registrationDetails.setOldPrNo(oldPrNo);
			}
			
			if (registrationDetails.getTrGeneratedDate() == null
					&& registrationDetails.getRegistrationValidity() != null
					&& registrationDetails.getRegistrationValidity().getTrGeneratedDate() != null) {
				registrationDetails.setTrGeneratedDate(
						registrationDetails.getRegistrationValidity().getTrGeneratedDate().atStartOfDay());
			}
			/*
			 * if (registrationDetails.getTrGeneratedDate() == null) { throw new
			 * BadRequestException( "trGenerated Date not found for appNo" +
			 * registrationDetails.getApplicationNo()); }
			 */
			if (StringUtils.isBlank(registrationDetails.getClassOfVehicle())) {
				logger.error("class of vehicle not found for :[{}] " + applicationNo);
				throw new BadRequestException("class of vehicle not found for :" + applicationNo);
			}
			classOfvehicle = registrationDetails.getClassOfVehicle();
			gvw = registrationDetails.getVahanDetails().getGvw();
			Payperiod = masterPayperiodDAO.findByCovcode(registrationDetails.getClassOfVehicle());
			if (this.allowForQuarterTax(classOfvehicle)) {
				/*if (registrationDetails.getRegistrationValidity().getPrGeneratedDate()
						.isBefore(properties.getPrGeneratedDate())) {

				}*/
				List<TaxDetailsDTO> listOfTaxDetails = taxDetailsDAO
						.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(applicationNo,
								Arrays.asList(TaxTypeEnum.LifeTax.getDesc()));
				if (listOfTaxDetails != null && !listOfTaxDetails.isEmpty()) {
					// for (TaxDetailsDTO dto : listOfTaxDetails) {
					// if
					// (registrationDetails.getClassOfVehicle().equalsIgnoreCase(dto.getClassOfVehicle()))
					// {
					TaxHelper taxHelper = returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), 0l, 0d, lifTaxValidityCal(),
							0l, 0d, payTaxType, "", 0d,null);
					listOfTaxDetails.clear();
					return taxHelper;
					// }
					// }
				}
				if (StringUtils.isNoneBlank(taxType) && taxType.equalsIgnoreCase(TaxTypeEnum.LifeTax.getDesc())) {
					mtltOrLtct = Boolean.TRUE;
					// taxType = TaxTypeEnum.QuarterlyTax.getDesc();
					quaterTax = getQuaterTaxForSpecificCase(taxType, registrationDetails, regServiceDTO,
							isApplicationFromMvi, stagingRegistrationDetails, isChassesApplication, isOtherState,
							applicationNo, serviceEnum, permitTypeCode, routeCode, taxCalBasedOn, classOfvehicle,
							payTaxType, alterDetails, isWeightAlt, quaterTax, listOfVcrsDetails, vcr, isregestered);
				} else {
					Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
				}

			}

		}

		if (!Payperiod.isPresent()) {
			// throw error message
			logger.error("No record found in master_payperiod for:[{}] " + applicationNo);
			throw new BadRequestException("No record found in master_payperiod for: " + applicationNo);

		}
		Boolean gostatus = Boolean.FALSE;

		if (!isApplicationFromMvi && StringUtils.isNoneBlank(classOfvehicle)
				&& (classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())
						|| (classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode()) && gvw <= 3000)
						|| classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())
						|| classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TRTT.getCovCode())
						|| classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TRCN.getCovCode()))) {
			return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), 0l, 0d, lifTaxValidityCal(), 0l, 0d, payTaxType, "",
					0d,null);
		}
		if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.BOTH.getCode())) {

			if (isChassesApplication) {
				// need to call body builder cov
				Pair<Optional<MasterPayperiodDTO>, Boolean> payperiodAndGoStatus = getPayPeroidForBoth(Payperiod,
						alterDetails.get().getFromSeatingCapacity(),
						stagingRegistrationDetails.getVahanDetails().getGvw());
				gostatus = payperiodAndGoStatus.getSecond();

			} else if (isApplicationFromMvi) {
				if (regServiceDTO.getAlterationDetails() != null
						&& regServiceDTO.getAlterationDetails().getSeating() != null) {
					// need to chamge gvw
					Pair<Optional<MasterPayperiodDTO>, Boolean> payperiodAndGoStatus = getPayPeroidForBoth(Payperiod,
							regServiceDTO.getAlterationDetails().getSeating(),
							regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw());
					gostatus = payperiodAndGoStatus.getSecond();
				} else {
					// need to chamge gvw
					Pair<Optional<MasterPayperiodDTO>, Boolean> payperiodAndGoStatus = getPayPeroidForBoth(Payperiod,
							regServiceDTO.getRegistrationDetails().getVahanDetails().getSeatingCapacity(),
							regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw());
					gostatus = payperiodAndGoStatus.getSecond();
				}
			} else if (isOtherState) {// OTHERSTATE

				String seats = regServiceDTO.getRegistrationDetails().getVahanDetails().getSeatingCapacity();
				if (alterDetails != null && alterDetails.isPresent()
						&& StringUtils.isNoneBlank(alterDetails.get().getSeating())) {
					seats = alterDetails.get().getSeating();
				}
				getPayPeroidForBoth(Payperiod, seats,
						regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw());

			} else if (vcr) {
				if (noApplication) {
					VcrFinalServiceDTO vcrDto = listOfVcrsDetails.getFirst().stream().findFirst().get();
					gvw=vcrDto.getRegistration().getGvwc();
					if(classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.GCRT.getCovCode())&&
							listOfVcrsDetails.getFirst().stream().findFirst().get().getRegistration().isOtherState()&&gvw <= 3000 && !listOfVcrsDetails.getSecond().isNocIssued()) {
						gvw=3001;
					}
					Pair<Optional<MasterPayperiodDTO>, Boolean> payperiodAndGoStatus = getPayPeroidForBoth(Payperiod,
							String.valueOf(vcrDto.getRegistration().getSeatingCapacity()),
							gvw);
					gostatus = payperiodAndGoStatus.getSecond();
				} else {
					Pair<Optional<MasterPayperiodDTO>, Boolean> payperiodAndGoStatus = getPayPeroidForBoth(Payperiod,
							listOfVcrsDetails.getSecond().getRegDetails().getVahanDetails().getSeatingCapacity(),
							listOfVcrsDetails.getSecond().getRegDetails().getVahanDetails().getGvw());
					gostatus = payperiodAndGoStatus.getSecond();
					// Payperiod =
					// masterPayperiodDAO.findByCovcode(listOfVcrsDetails.getSecond().getRegDetails().getVahanDetails().getSeatingCapacity());

				}
			} else {

				Pair<Optional<MasterPayperiodDTO>, Boolean> payperiodAndGoStatus = getPayPeroidForBoth(Payperiod,
						registrationDetails.getVahanDetails().getSeatingCapacity(),
						registrationDetails.getVahanDetails().getGvw());
				gostatus = payperiodAndGoStatus.getSecond();
			}

		}
		if (gostatus) {
			List<MasterNewGoTaxDetails> newGoDetesDetails = masterNewGoTaxDetailsDAO.findAll();
			if (newGoDetesDetails.isEmpty()) {
				throw new BadRequestException("new records found for new Gov go.");
			}
			MasterNewGoTaxDetails goDetails = newGoDetesDetails.stream().findFirst().get();

			List<TaxDetailsDTO> listOfTaxDetails = taxDetailsDAO
					.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(applicationNo,
							Arrays.asList(TaxTypeEnum.LifeTax.getDesc()));
			if (listOfTaxDetails != null && !listOfTaxDetails.isEmpty()) {
				for (TaxDetailsDTO dto : listOfTaxDetails) {
					if (classOfvehicle.equalsIgnoreCase(dto.getClassOfVehicle())) {
						TaxHelper taxHelper = returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), 0l, 0d,
								lifTaxValidityCal(), 0l, 0d, payTaxType, "", 0d,null);
						listOfTaxDetails.clear();
						return taxHelper;
					}
				}
			}
			LocalDate trGeneratedDate;
			if (isApplicationFromMvi) {
				if (regServiceDTO.getRegistrationDetails().getRegistrationValidity().getTrGeneratedDate() == null) {
					trGeneratedDate = regServiceDTO.getRegistrationDetails().getRegistrationValidity()
							.getPrGeneratedDate();
				} else {
					trGeneratedDate = regServiceDTO.getRegistrationDetails().getRegistrationValidity()
							.getTrGeneratedDate();
				}
			} else if (isChassesApplication) {
				if (stagingRegistrationDetails.getRegistrationValidity().getTrGeneratedDate() != null) {
					trGeneratedDate = stagingRegistrationDetails.getRegistrationValidity().getTrGeneratedDate();
				} else {
					trGeneratedDate = stagingRegistrationDetails.getTrGeneratedDate().toLocalDate();
				}
			} else if (isOtherState) {
				trGeneratedDate = regServiceDTO.getRegistrationDetails().getRegistrationValidity().getTrGeneratedDate();
			} else if (vcr) {
				if(listOfVcrsDetails.getSecond().getTrGeneratedDate()!=null) {
					trGeneratedDate = listOfVcrsDetails.getSecond().getTrGeneratedDate();	
				}else {
					trGeneratedDate = LocalDate.now();
				}
				
			} else {

				if (registrationDetails.getRegistrationValidity().getTrGeneratedDate() != null) {
					trGeneratedDate = registrationDetails.getRegistrationValidity().getTrGeneratedDate();
				} else {
					trGeneratedDate = registrationDetails.getRegistrationValidity().getPrGeneratedDate();
				}

			}
			if (trGeneratedDate.isBefore(goDetails.getTaxEffectFrom())) {
				if (taxType.equalsIgnoreCase(TaxTypeEnum.QuarterlyTax.getDesc())
						|| taxType.equalsIgnoreCase(TaxTypeEnum.HalfyearlyTax.getDesc())
						|| taxType.equalsIgnoreCase(TaxTypeEnum.YearlyTax.getDesc())) {
					Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
				}
				if (goDetails.getOldTaxEffectUpTo().isBefore(LocalDate.now())) {
					if (taxType.equalsIgnoreCase(TaxTypeEnum.QuarterlyTax.getDesc())
							|| taxType.equalsIgnoreCase(TaxTypeEnum.HalfyearlyTax.getDesc())
							|| taxType.equalsIgnoreCase(TaxTypeEnum.YearlyTax.getDesc())) {
						Payperiod.get().setPayperiod(TaxTypeEnum.LifeTax.getCode());
						if(vcr && !listOfVcrsDetails.getFirst().stream().findFirst().get().getRegistration().isOtherState()) {
							taxType = TaxTypeEnum.LifeTax.getDesc();
						}
					}
				}
				// taxType = TaxTypeEnum.LifeTax.getDesc();
				if (StringUtils.isNoneBlank(taxType) && taxType.equalsIgnoreCase(TaxTypeEnum.LifeTax.getDesc())) {
					quaterTax = getQuaterTaxForSpecificCase(taxType, registrationDetails, regServiceDTO,
							isApplicationFromMvi, stagingRegistrationDetails, isChassesApplication, isOtherState,
							applicationNo, serviceEnum, permitTypeCode, routeCode, taxCalBasedOn, classOfvehicle,
							payTaxType, alterDetails, isWeightAlt, quaterTax, listOfVcrsDetails, vcr, isregestered);

					/*
					 * if (taxType.equalsIgnoreCase(TaxTypeEnum.CESS.getDesc())) { return
					 * returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), 0l, 0d, lifTaxValidityCal(),
					 * 0l, 0d, payTaxType, ""); }
					 * 
					 * TaxHelper lastTaxTillDate = getLastPaidTax(registrationDetails,
					 * regServiceDTO, isApplicationFromMvi,
					 * validity(TaxTypeEnum.QuarterlyTax.getDesc()), stagingRegistrationDetails,
					 * isChassesApplication, taxTypes(),isOtherState); if (lastTaxTillDate == null
					 * || lastTaxTillDate.getTax() == null || lastTaxTillDate.getValidityTo() ==hi
					 * null) { throw new BadRequestException("TaxDetails not found"); } if
					 * (lastTaxTillDate.isAnypendingQuaters()) { // taxType =
					 * TaxTypeEnum.QuarterlyTax.getDesc(); quaterTax =
					 * quaterTaxCalculation(applicationNo, isApplicationFromMvi,
					 * isChassesApplication, taxType, isOtherState, serviceEnum, permitTypeCode,
					 * routeCode, taxCalBasedOn, regServiceDTO, registrationDetails,
					 * stagingRegistrationDetails, classOfvehicle, payTaxType, alterDetails,
					 * isWeightAlt); }
					 */}
			}
		}
		switch (TaxTypeEnum.getTaxTypeEnumByCode(Payperiod.get().getPayperiod())) {

		case LifeTax:
			Optional<MasterAmountSecoundCovsDTO> basedOnInvoice = Optional.empty();
			Optional<MasterAmountSecoundCovsDTO> basedOnsecoundVehicle = Optional.empty();
			Double totalLifeTax;
			if (taxType.equalsIgnoreCase(ServiceCodeEnum.CESS_FEE.getCode())) {
				return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), 0l, 0d, LocalDate.now(), 0l, 0d, payTaxType, "",
						0d,null);
			}
			List<MasterAmountSecoundCovsDTO> masterAmountSecoundCovsDTO = masterAmountSecoundCovsDAO.findAll();
			if (masterAmountSecoundCovsDTO.isEmpty()) {
				// TODO:need to Exception throw
			}
			// sdfsdfsdf
			if (isChassesApplication) {
				return chassisTaxCalcultion(isApplicationFromMvi, isChassesApplication, isOtherState, regServiceDTO,
						registrationDetails, stagingRegistrationDetails, vcr, payTaxType, 
						masterAmountSecoundCovsDTO,listOfVcrsDetails);
			} else if (isApplicationFromMvi) {

				boolean isToSkipTak = this.checkIsToPayLifeTaxBefore(
						regServiceDTO.getRegistrationDetails().getApplicationNo(), regServiceDTO.getAlterationDetails(),
						regServiceDTO.getRegistrationDetails().getPrNo());
				if (!isToSkipTak) {
					setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
					return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), 0l, 0d, lifTaxValidityCal(), 0l, 0d,
							payTaxType, "", 0d,null);
				}
				if (ownertypecheck(regServiceDTO.getRegistrationDetails().getOwnerType().getCode(), regServiceDTO.getRegistrationDetails().getClassOfVehicle())) {
					setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
					return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), 0l, 0d, lifTaxValidityCal(), 0l, 0d,
							payTaxType, "", 0d,null);
				}
				Optional<MasterTaxExcemptionsDTO> optionalTaxExcemption = masterTaxExcemptionsDAO
						.findByKeyvalueOrCovcode(
								regServiceDTO.getRegistrationDetails().getVahanDetails().getMakersModel(),
								regServiceDTO.getRegistrationDetails().getClassOfVehicle());
				if (optionalTaxExcemption.isPresent()) {
					//
					setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
					return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(),
							optionalTaxExcemption.get().getTaxvalue().longValue(), 0d, lifTaxValidityCal(), 0l, 0d,
							payTaxType, "", 0d,null);
				}
				// calculate age of vehicle
				double vehicleAge = calculateAgeOfTheVehicle(
						regServiceDTO.getRegistrationDetails().getRegistrationValidity().getPrGeneratedDate(),
						LocalDate.now());
				String cov = regServiceDTO.getAlterationDetails().getCov() != null
						? regServiceDTO.getAlterationDetails().getCov()
						: regServiceDTO.getRegistrationDetails().getClassOfVehicle();
				Optional<MasterTax> OptionalLifeTax = taxTypeDAO
						.findByCovcodeAndOwnershiptypeIgnoreCaseAndStatecodeAndStatusAndToageGreaterThanEqualAndFromageLessThanEqualAndTocostGreaterThanEqualAndFromcostLessThanEqual(
								cov, regServiceDTO.getRegistrationDetails().getOwnerType().getCode(), stateCode,
								regStatus, vehicleAge, vehicleAge,
								regServiceDTO.getRegistrationDetails().getInvoiceDetails().getInvoiceValue(),
								regServiceDTO.getRegistrationDetails().getInvoiceDetails().getInvoiceValue());
				if (!OptionalLifeTax.isPresent()) {
					logger.error("No record found in master_tax for: " + regServiceDTO.getAlterationDetails().getCov()
							+ "and" + regServiceDTO.getRegistrationDetails().getOwnerType());
					// throw error message
					throw new BadRequestException(
							"No record found in master_tax for: " + regServiceDTO.getAlterationDetails().getCov()
									+ "and" + regServiceDTO.getRegistrationDetails().getOwnerType());
				}
				totalLifeTax = (regServiceDTO.getRegistrationDetails().getInvoiceDetails().getInvoiceValue()
						* OptionalLifeTax.get().getPercent() / 100f);
				Pair<Long, Double> lifeTax = finalLifeTaxCalculation(stagingRegistrationDetails, regServiceDTO,
						registrationDetails, totalLifeTax, OptionalLifeTax.get().getPercent(), isApplicationFromMvi,
						isChassesApplication, isOtherState, vcr, false, null);
				setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
				return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), lifeTax.getFirst(), lifeTax.getSecond(),
						lifTaxValidityCal(), 0l, 0d, payTaxType, "", 0d,null);
			} else if (isOtherState) {
				return getOtherStateLifeTax(isApplicationFromMvi, isChassesApplication, isOtherState, regServiceDTO,
						registrationDetails, stagingRegistrationDetails, payTaxType, alterDetails,
						masterAmountSecoundCovsDTO, listOfVcrsDetails, vcr);
			} else if (vcr) {
				/*
				 * if(listOfVcrsDetails.getSecond().getIsOtherState() && !noApplication) {
				 * return getOtherStateLifeTax(isApplicationFromMvi, isChassesApplication,
				 * isOtherState, regServiceDTO, registrationDetails, stagingRegistrationDetails,
				 * payTaxType, alterDetails, masterAmountSecoundCovsDTO,listOfVcrsDetails,vcr);
				 * }else if(listOfVcrsDetails.getSecond().getIsOtherState() && noApplication) {
				 * //no application }else if(!listOfVcrsDetails.getSecond().getIsOtherState() &&
				 * isUnRegestered && !noApplication) {
				 * taxService.getTaxDetails(stagingRegistrationDetails); return
				 * returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(),
				 * stagingRegistrationDetails.getTaxAmount(), 0d, lifTaxValidityCal(), 0l, 0d,
				 * payTaxType, ""); }else if(!listOfVcrsDetails.getSecond().getIsOtherState() &&
				 * isUnRegestered && noApplication) { //no application }else { //registered }
				 */
				if ((listOfVcrsDetails.getSecond().isNoApplication())
						|| (listOfVcrsDetails.getSecond().getIsOtherState()
								&& listOfVcrsDetails.getSecond().isUnRegestered()
								&& listOfVcrsDetails.getSecond().isNoApplication())
						|| (!listOfVcrsDetails.getSecond().getIsOtherState()
								&& listOfVcrsDetails.getSecond().isUnRegestered()
								&& listOfVcrsDetails.getSecond().isNoApplication())) {
					return this.getVcrLifeTax(listOfVcrsDetails);

				}else if(gostatus && !listOfVcrsDetails.getFirst().stream().findFirst().get().getRegistration().isOtherState()) {
					return gcrtOrMtltOrLtctLIfeTxa(applicationNo, regServiceDTO, registrationDetails,
							stagingRegistrationDetails, quaterTax, payTaxType, gostatus);
				}else if(listOfVcrsDetails.getSecond().isVcrChassisTax()) {
					return chassisTaxCalcultion(isApplicationFromMvi, isChassesApplication, isOtherState, regServiceDTO,
							registrationDetails, stagingRegistrationDetails, vcr, payTaxType, 
							masterAmountSecoundCovsDTO,listOfVcrsDetails);
				}

			} else {
				if (gostatus || mtltOrLtct) {

					return gcrtOrMtltOrLtctLIfeTxa(applicationNo, regServiceDTO, registrationDetails,
							stagingRegistrationDetails, quaterTax, payTaxType, gostatus);
				}
			}
			break;

		case QuarterlyTax:
			TaxHelper tax = quaterTaxCalculation(applicationNo, isApplicationFromMvi, isChassesApplication, taxType,
					isOtherState, serviceEnum, permitTypeCode, routeCode, taxCalBasedOn, regServiceDTO,
					registrationDetails, stagingRegistrationDetails, classOfvehicle, payTaxType, alterDetails,
					isWeightAlt, listOfVcrsDetails, vcr, isregestered, false, null);
			return tax;
		// break;
		default:
			break;

		}
		return returnTaxDetails(taxType, 0l, 0d, LocalDate.now(), 0l, 0d, TaxTypeEnum.TaxPayType.REG, "", 0d,null);
	}

	private TaxHelper chassisTaxCalcultion(boolean isApplicationFromMvi, boolean isChassesApplication,
			boolean isOtherState, RegServiceDTO regServiceDTO, RegistrationDetailsDTO registrationDetails,
			StagingRegistrationDetailsDTO stagingRegistrationDetails, Boolean vcr, TaxTypeEnum.TaxPayType payTaxType,
			List<MasterAmountSecoundCovsDTO> masterAmountSecoundCovsDTO,Pair<List<VcrFinalServiceDTO>, CitizenFeeDetailsInput> listOfVcrsDetails) {
		Optional<MasterAmountSecoundCovsDTO> basedOnInvoice;
		Optional<MasterAmountSecoundCovsDTO> basedOnsecoundVehicle;
		Double totalLifeTax;
		Optional<AlterationDTO> alterDetails =Optional.empty();
		if(vcr) {
			stagingRegistrationDetails = listOfVcrsDetails.getSecond().getStagingRegistrationDetails();
			AlterationDTO alt = new AlterationDTO();
			//alt.setCov(stagingRegistrationDetails.getClassOfVehicle());
			alterDetails=Optional.of(listOfVcrsDetails.getSecond().getAlterDetails());
			isChassesApplication=Boolean.TRUE;
		}else {
			alterDetails = alterationDao
					.findByApplicationNo(stagingRegistrationDetails.getApplicationNo());
			if (alterDetails ==null ||  !alterDetails.isPresent()) {
				throw new BadRequestException(
						"No record found in alteration for: " + stagingRegistrationDetails.getApplicationNo());
			}
		}
		

		Optional<MasterTaxExcemptionsDTO> optionalTaxExcemption = masterTaxExcemptionsDAO
				.findByKeyvalueOrCovcode(stagingRegistrationDetails.getVahanDetails().getMakersModel(),
						stagingRegistrationDetails.getClassOfVehicle());
		if (optionalTaxExcemption.isPresent()) {
			setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
			return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(),
					optionalTaxExcemption.get().getTaxvalue().longValue(), 0d, lifTaxValidityCal(), 0l, 0d,
					payTaxType, "", 0d,null);
		}
		String cov =alterDetails.get().getCov();
		basedOnInvoice = masterAmountSecoundCovsDTO.stream()
				.filter(m -> m.getAmountcovcode().contains(cov)).findFirst();

		basedOnsecoundVehicle = masterAmountSecoundCovsDTO.stream()
				.filter(m -> m.getSecondcovcode().contains(cov)).findFirst();

		if (ownertypecheck(stagingRegistrationDetails.getOwnerType().getCode(), alterDetails.get().getCov())) {
			Optional<MasterTax> OptionalLifeTax = taxTypeDAO
					.findByCovcodeAndOwnershiptypeIgnoreCaseAndStatecodeAndStatusAndFromage(
							alterDetails.get().getCov(), stagingRegistrationDetails.getOwnerType().getCode(),
							stateCode, regStatus, freshVehicleAge);
			if (!OptionalLifeTax.isPresent()) {
				logger.error("No record found in master_tax for: " + alterDetails.get().getCov() + "and"
						+ stagingRegistrationDetails.getOwnerType());
				// throw error message
				throw new BadRequestException("No record found in master_tax for: "
						+ alterDetails.get().getCov() + "and" + stagingRegistrationDetails.getOwnerType());
			}

			totalLifeTax = (stagingRegistrationDetails.getInvoiceDetails().getInvoiceValue()
					* OptionalLifeTax.get().getPercent() / 100);
			if (totalLifeTax.equals(Double.valueOf(0))) {
				setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
				return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), 0l, 0d, lifTaxValidityCal(), 0l, 0d,
						payTaxType, "", 0d,null);
			}
			Pair<Long, Double> lifeTax = finalLifeTaxCalculation(stagingRegistrationDetails, regServiceDTO,
					registrationDetails, totalLifeTax, OptionalLifeTax.get().getPercent(), isApplicationFromMvi,
					isChassesApplication, isOtherState, vcr, false, null);
			setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
			return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), lifeTax.getFirst(), lifeTax.getSecond(),
					lifTaxValidityCal(), 0l, 0d, payTaxType, "", 0d,null);

		} else if (stagingRegistrationDetails.getInvoiceDetails().getInvoiceValue() > amount
				&& basedOnInvoice.isPresent() && stagingRegistrationDetails.getOwnerType().getCode()
						.equalsIgnoreCase(OwnerTypeEnum.Individual.getCode())) {

			totalLifeTax = (stagingRegistrationDetails.getInvoiceDetails().getInvoiceValue()
					* basedOnInvoice.get().getTaxpercentinvoice() / 100f);
			Pair<Long, Double> lifeTax = finalLifeTaxCalculation(stagingRegistrationDetails, regServiceDTO,
					registrationDetails, totalLifeTax, basedOnInvoice.get().getTaxpercentinvoice(),
					isApplicationFromMvi, isChassesApplication, isOtherState, vcr, false, null);
			setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
			return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), lifeTax.getFirst(), lifeTax.getSecond(),
					lifTaxValidityCal(), 0l, 0d, payTaxType, "", 0d,null);
		} else if (basedOnsecoundVehicle.isPresent() && !stagingRegistrationDetails.getIsFirstVehicle()) {

			totalLifeTax = (stagingRegistrationDetails.getInvoiceDetails().getInvoiceValue()
					* basedOnsecoundVehicle.get().getSecondvehiclepercent() / 100f);
			Pair<Long, Double> lifeTax = finalLifeTaxCalculation(stagingRegistrationDetails, regServiceDTO,
					registrationDetails, totalLifeTax, basedOnsecoundVehicle.get().getSecondvehiclepercent(),
					isApplicationFromMvi, isChassesApplication, isOtherState, vcr, false, null);
			// stagingRegistrationDetails.setSecondVehicleTaxPaid(Boolean.TRUE);
			setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
			return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), lifeTax.getFirst(), lifeTax.getSecond(),
					lifTaxValidityCal(), 0l, 0d, payTaxType, "", 0d,null);
		} else if (alterDetails.get().getCov().equalsIgnoreCase(ClassOfVehicleEnum.IVCN.getCovCode())) {

			// stagingRegistrationDetailsDAO.save(stagingRegDetails);
			setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
			return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), 0l, 0d, lifTaxValidityCal(), 0l, 0d,
					payTaxType, "", 0d,null);

		} else {
			Optional<MasterTax> OptionalLifeTax = taxTypeDAO
					.findByCovcodeAndOwnershiptypeIgnoreCaseAndStatecodeAndStatusAndFromage(
							alterDetails.get().getCov(), stagingRegistrationDetails.getOwnerType().getCode(),
							stateCode, regStatus, freshVehicleAge);
			if (!OptionalLifeTax.isPresent()) {
				logger.error("No record found in master_tax for: " + alterDetails.get().getCov() + "and"
						+ stagingRegistrationDetails.getOwnerType());
				// throw error message
				throw new BadRequestException("No record found in master_tax for: "
						+ alterDetails.get().getCov() + "and" + stagingRegistrationDetails.getOwnerType());
			}
			totalLifeTax = (stagingRegistrationDetails.getInvoiceDetails().getInvoiceValue()
					* OptionalLifeTax.get().getPercent() / 100f);
			Pair<Long, Double> lifeTax = finalLifeTaxCalculation(stagingRegistrationDetails, regServiceDTO,
					registrationDetails, totalLifeTax, OptionalLifeTax.get().getPercent(), isApplicationFromMvi,
					isChassesApplication, isOtherState, vcr, false, null);
			setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
			return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), lifeTax.getFirst(), lifeTax.getSecond(),
					lifTaxValidityCal(), 0l, 0d, payTaxType, "", 0d,null);
		}
	}

	private TaxHelper gcrtOrMtltOrLtctLIfeTxa(String applicationNo, RegServiceDTO regServiceDTO,
			RegistrationDetailsDTO registrationDetails, StagingRegistrationDetailsDTO stagingRegistrationDetails,
			TaxHelper quaterTax, TaxTypeEnum.TaxPayType payTaxType, Boolean gostatus) {
		Double totalLifeTax;
		double vehicleAge = 0d;

		if (ownertypecheck(registrationDetails.getOwnerType().getCode(), registrationDetails.getClassOfVehicle())) {
			setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
			return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), 0l, 0d, lifTaxValidityCal(), 0l, 0d,
					payTaxType, "", 0d,null);
		}
		Optional<MasterTaxExcemptionsDTO> optionalTaxExcemption = masterTaxExcemptionsDAO
				.findByKeyvalueOrCovcode(registrationDetails.getVahanDetails().getMakersModel(),
						registrationDetails.getClassOfVehicle());
		if (optionalTaxExcemption.isPresent()) {
			//
			setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
			return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(),
					optionalTaxExcemption.get().getTaxvalue().longValue(), 0d,
					lifTaxValidityCalForRegVeh(
							registrationDetails.getRegistrationValidity().getPrGeneratedDate()),
					0l, 0d, payTaxType, "", 0d,null);
		}
		if (gostatus) {
			List<MasterNewGoTaxDetails> newGoDetesDetails = masterNewGoTaxDetailsDAO.findAll();
			if (newGoDetesDetails.isEmpty()) {
				throw new BadRequestException("new records found for new Gov go.");
			}
			MasterNewGoTaxDetails goDetails = newGoDetesDetails.stream().findFirst().get();
			if (registrationDetails.getTrGeneratedDate() == null) {
				registrationDetails.setTrGeneratedDate(
						registrationDetails.getRegistrationValidity().getPrGeneratedDate().atStartOfDay());
			}
			if (goDetails.getTaxEffectFrom()
					.isAfter(registrationDetails.getTrGeneratedDate().toLocalDate())) {
				if (registrationDetails.getRegistrationValidity() == null
						|| registrationDetails.getRegistrationValidity().getPrGeneratedDate() == null) {
					throw new BadRequestException(
							"pr issued  date not found for: " + registrationDetails.getPrNo());
				}
				vehicleAge = calculateAgeOfTheVehicle(
						registrationDetails.getRegistrationValidity().getPrGeneratedDate(),
						LocalDate.now());
			}
		}
		vehicleAge = calculateAgeOfTheVehicle(
				registrationDetails.getRegistrationValidity().getPrGeneratedDate(), LocalDate.now());
		Optional<MasterTax> OptionalLifeTax = taxTypeDAO
				.findByCovcodeAndOwnershiptypeIgnoreCaseAndStatecodeAndStatusAndToageGreaterThanEqualAndFromageLessThanEqual(
						registrationDetails.getClassOfVehicle(),
						registrationDetails.getOwnerType().getCode(), stateCode, regStatus, vehicleAge,
						vehicleAge);
		if (!OptionalLifeTax.isPresent()) {
			logger.error("No record found in master tax for: " + registrationDetails.getClassOfVehicle()
					+ "and" + registrationDetails.getOwnerType());
			// throw error message
			throw new BadRequestException("No record found in master tax for: "
					+ registrationDetails.getClassOfVehicle() + "and" + registrationDetails.getOwnerType());
		}
		double tax = 0d;
		Long penality = 0l;
		Long reoundTaxArrears = 0l;
		Long penalityArrears = 0l;
		if (registrationDetails.getInvoiceDetails() == null) {
			throw new BadRequestException("Invoice Details Not found for appNo" + applicationNo);
		}
		totalLifeTax = (registrationDetails.getInvoiceDetails().getInvoiceValue()
				* OptionalLifeTax.get().getPercent() / 100f);
		if (quaterTax != null) {
			tax = quaterTax.getTaxAmountForPayments();
			penality = quaterTax.getPenalty();
			reoundTaxArrears = quaterTax.getTaxArrearsRound();
			penalityArrears = quaterTax.getPenaltyArrears();
		}
		TaxHelper finalTax = returnTaxForNewGo(TaxTypeEnum.LifeTax.getDesc(), roundUpperTen(totalLifeTax),
				penality.doubleValue(),
				lifTaxValidityCalForRegVeh(
						registrationDetails.getRegistrationValidity().getPrGeneratedDate()),
				reoundTaxArrears, penalityArrears.doubleValue(), payTaxType, "", roundUpperTen(tax));
		setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
		return finalTax;
	}

	private TaxHelper getOtherStateLifeTax(boolean isApplicationFromMvi, boolean isChassesApplication,
			boolean isOtherState, RegServiceDTO regServiceDTO, RegistrationDetailsDTO registrationDetails,
			StagingRegistrationDetailsDTO stagingRegistrationDetails, TaxTypeEnum.TaxPayType payTaxType,
			Optional<AlterationDTO> alterDetails, List<MasterAmountSecoundCovsDTO> masterAmountSecoundCovsDTO,
			Pair<List<VcrFinalServiceDTO>, CitizenFeeDetailsInput> listOfVcrsDetails, boolean vcr) {
		Optional<MasterAmountSecoundCovsDTO> basedOnInvoice;
		Optional<MasterAmountSecoundCovsDTO> basedOnsecoundVehicle;
		Double totalLifeTax;
		// other state
		TaxHelper vcrDetails = vcrIntegration(listOfVcrsDetails, vcr);
		boolean isVehicleSized = false;
		LocalDate sizedDate = null;
		if (vcrDetails != null && vcrDetails.isVehicleSized()) {
			isVehicleSized = vcrDetails.isVehicleSized();
			sizedDate = vcrDetails.getSizedDate();
		}
		if(checkTaxPaidAtVcr(regServiceDTO.getRegistrationDetails().getVahanDetails().getChassisNumber())) {
			return returnTaxDetails(0d, 0d, 0d, 0d, 0d,null);
		}// check other state VoluntaryTax paid 
		if(checkTaxPaidAtVoluntary(regServiceDTO.getRegistrationDetails().getVahanDetails().getChassisNumber())) {
			return returnTaxDetails(0d, 0d, 0d, 0d, 0d,null);
		}
		RegServiceVO vo = regServiceMapper.convertEntity(regServiceDTO);
		if (ownertypecheck(regServiceDTO.getRegistrationDetails().getOwnerType().getCode(), regServiceDTO.getRegistrationDetails().getClassOfVehicle())) {
			Optional<MasterTax> OptionalLifeTax = taxTypeDAO
					.findByCovcodeAndOwnershiptypeIgnoreCaseAndStatecodeAndStatusAndFromage(
							regServiceDTO.getRegistrationDetails().getClassOfVehicle(),
							regServiceDTO.getRegistrationDetails().getOwnerType().getCode(), stateCode, regStatus,
							freshVehicleAge);
			if (!OptionalLifeTax.isPresent()) {
				logger.error("No record found in master_tax for: " + alterDetails.get().getCov() + "and"
						+ regServiceDTO.getRegistrationDetails().getOwnerType());
				// throw error message
				throw new BadRequestException("No record found in master_tax for: " + alterDetails.get().getCov()
						+ "and" + regServiceDTO.getRegistrationDetails().getOwnerType());
			}

			totalLifeTax = (regServiceDTO.getRegistrationDetails().getInvoiceDetails().getInvoiceValue()
					* OptionalLifeTax.get().getPercent() / 100);
			if (totalLifeTax.equals(Double.valueOf(0))) {
				setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
				return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), 0l, 0d, lifTaxValidityCal(), 0l, 0d, payTaxType,
						"", 0d,null);
			}
			Pair<Long, Double> lifeTax = finalLifeTaxCalculation(stagingRegistrationDetails, regServiceDTO,
					registrationDetails, totalLifeTax, OptionalLifeTax.get().getPercent(), isApplicationFromMvi,
					isChassesApplication, isOtherState, vcr, isVehicleSized, sizedDate);
			setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
			return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), lifeTax.getFirst(), lifeTax.getSecond(),
					lifTaxValidityCal(), 0l, 0d, payTaxType, "", 0d,null);

		}
		if ((regServiceDTO.getRegistrationDetails().getClassOfVehicle()
				.equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
				|| regServiceDTO.getRegistrationDetails().getClassOfVehicle()
						.equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode()))
				&& regServiceDTO.isMviDone()) {
			Optional<AlterationDTO> alterDetailsOptionla = alterationDao
					.findByApplicationNo(regServiceDTO.getApplicationNo());
			if (!alterDetailsOptionla.isPresent()) {
				throw new BadRequestException(
						"No record found in alteration document for: " + regServiceDTO.getApplicationNo());
			}

			Optional<MasterTaxExcemptionsDTO> optionalTaxExcemption = masterTaxExcemptionsDAO.findByKeyvalueOrCovcode(
					regServiceDTO.getRegistrationDetails().getVahanDetails().getMakersModel(),
					alterDetails.get().getCov());
			if (optionalTaxExcemption.isPresent()) {
				//
				setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
				return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(),
						optionalTaxExcemption.get().getTaxvalue().longValue(), 0d, lifTaxValidityCal(), 0l, 0d,
						payTaxType, "", 0d,null);
			}
			basedOnInvoice = masterAmountSecoundCovsDTO.stream()
					.filter(m -> m.getAmountcovcode().contains(alterDetailsOptionla.get().getCov())).findFirst();

			basedOnsecoundVehicle = masterAmountSecoundCovsDTO.stream()
					.filter(m -> m.getSecondcovcode().contains(alterDetailsOptionla.get().getCov())).findFirst();

			if (ownertypecheck(regServiceDTO.getRegistrationDetails().getOwnerType().getCode(), alterDetails.get().getCov())) {
				Optional<MasterTax> OptionalLifeTax = taxTypeDAO
						.findByCovcodeAndOwnershiptypeIgnoreCaseAndStatecodeAndStatusAndFromage(
								alterDetails.get().getCov(),
								regServiceDTO.getRegistrationDetails().getOwnerType().getCode(), stateCode, regStatus,
								freshVehicleAge);
				if (!OptionalLifeTax.isPresent()) {
					logger.error("No record found in master_tax for: " + alterDetails.get().getCov() + "and"
							+ regServiceDTO.getRegistrationDetails().getOwnerType());
					// throw error message
					throw new BadRequestException("No record found in master_tax for: " + alterDetails.get().getCov()
							+ "and" + regServiceDTO.getRegistrationDetails().getOwnerType());
				}

				totalLifeTax = (regServiceDTO.getRegistrationDetails().getInvoiceDetails().getInvoiceValue()
						* OptionalLifeTax.get().getPercent() / 100);
				if (totalLifeTax.equals(Double.valueOf(0))) {
					setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
					return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), 0l, 0d, lifTaxValidityCal(), 0l, 0d,
							payTaxType, "", 0d,null);
				}
				Pair<Long, Double> lifeTax = finalLifeTaxCalculation(stagingRegistrationDetails, regServiceDTO,
						registrationDetails, totalLifeTax, OptionalLifeTax.get().getPercent(), isApplicationFromMvi,
						isChassesApplication, isOtherState, vcr, isVehicleSized, sizedDate);
				setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
				return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), lifeTax.getFirst(), lifeTax.getSecond(),
						lifTaxValidityCal(), 0l, 0d, payTaxType, "", 0d,null);

			} else if (regServiceDTO.getRegistrationDetails().getInvoiceDetails().getInvoiceValue() > amount
					&& basedOnInvoice.isPresent() && regServiceDTO.getRegistrationDetails().getOwnerType().getCode()
							.equalsIgnoreCase(OwnerTypeEnum.Individual.getCode())) {

				totalLifeTax = (regServiceDTO.getRegistrationDetails().getInvoiceDetails().getInvoiceValue()
						* basedOnInvoice.get().getTaxpercentinvoice() / 100f);
				Pair<Long, Double> lifeTax = finalLifeTaxCalculation(stagingRegistrationDetails, regServiceDTO,
						registrationDetails, totalLifeTax, basedOnInvoice.get().getTaxpercentinvoice(),
						isApplicationFromMvi, isChassesApplication, isOtherState, vcr, isVehicleSized, sizedDate);
				setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
				return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), lifeTax.getFirst(), lifeTax.getSecond(),
						lifTaxValidityCal(), 0l, 0d, payTaxType, "", 0d,null);
			} else if (basedOnsecoundVehicle.isPresent()
					&& !regServiceDTO.getRegistrationDetails().getIsFirstVehicle()) {

				totalLifeTax = (regServiceDTO.getRegistrationDetails().getInvoiceDetails().getInvoiceValue()
						* basedOnsecoundVehicle.get().getSecondvehiclepercent() / 100f);
				Pair<Long, Double> lifeTax = finalLifeTaxCalculation(stagingRegistrationDetails, regServiceDTO,
						registrationDetails, totalLifeTax, basedOnsecoundVehicle.get().getSecondvehiclepercent(),
						isApplicationFromMvi, isChassesApplication, isOtherState, vcr, isVehicleSized, sizedDate);
				// stagingRegistrationDetails.setSecondVehicleTaxPaid(Boolean.TRUE);
				setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
				return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), lifeTax.getFirst(), lifeTax.getSecond(),
						lifTaxValidityCal(), 0l, 0d, payTaxType, "", 0d,null);
			} else if (alterDetails.get().getCov().equalsIgnoreCase(ClassOfVehicleEnum.IVCN.getCovCode())) {

				// stagingRegistrationDetailsDAO.save(stagingRegDetails);
				setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
				return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), 0l, 0d, lifTaxValidityCal(), 0l, 0d, payTaxType,
						"", 0d,null);

			} else {
				Optional<MasterTax> OptionalLifeTax = taxTypeDAO
						.findByCovcodeAndOwnershiptypeIgnoreCaseAndStatecodeAndStatusAndFromage(
								alterDetails.get().getCov(),
								regServiceDTO.getRegistrationDetails().getOwnerType().getCode(), stateCode, regStatus,
								freshVehicleAge);
				if (!OptionalLifeTax.isPresent()) {
					logger.error("No record found in master_tax for: " + alterDetails.get().getCov() + "and"
							+ regServiceDTO.getRegistrationDetails().getOwnerType());
					// throw error message
					throw new BadRequestException("No record found in master_tax for: " + alterDetails.get().getCov()
							+ "and" + regServiceDTO.getRegistrationDetails().getOwnerType());
				}
				totalLifeTax = (regServiceDTO.getRegistrationDetails().getInvoiceDetails().getInvoiceValue()
						* OptionalLifeTax.get().getPercent() / 100f);
				Pair<Long, Double> lifeTax = finalLifeTaxCalculation(stagingRegistrationDetails, regServiceDTO,
						registrationDetails, totalLifeTax, OptionalLifeTax.get().getPercent(), isApplicationFromMvi,
						isChassesApplication, isOtherState, vcr, isVehicleSized, sizedDate);
				setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
				return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), lifeTax.getFirst(), lifeTax.getSecond(),
						lifTaxValidityCal(), 0l, 0d, payTaxType, "", 0d,null);
			}
		}
		this.checkTaxPaidAtBorder(regServiceDTO);
		OtherStateApplictionType applicationType = getOtherStateVehicleStatus(vo);
		if (Arrays.asList(OtherStateApplictionType.ApplicationNO, OtherStateApplictionType.TrNo)
				.contains(applicationType)) {
			basedOnInvoice = masterAmountSecoundCovsDTO.stream()
					.filter(m -> m.getAmountcovcode().contains(vo.getRegistrationDetails().getClassOfVehicle()))
					.findFirst();
			basedOnsecoundVehicle = masterAmountSecoundCovsDTO.stream()
					.filter(m -> m.getSecondcovcode().contains(vo.getRegistrationDetails().getClassOfVehicle()))
					.findFirst();
			if (!vo.getRegistrationDetails().isTaxPaidByVcr()) {
				if (vo.getRegistrationDetails().getApplicantType() != null
						&& !vo.getRegistrationDetails().getApplicantType().equalsIgnoreCase("withinthestate")) {
					if (regServiceDTO.getRegistrationDetails().getInvoiceDetails().getInvoiceValue() > amount
							&& basedOnInvoice.isPresent() && regServiceDTO.getRegistrationDetails().getOwnerType()
									.getCode().equalsIgnoreCase(OwnerTypeEnum.Individual.getCode())) {

						totalLifeTax = (regServiceDTO.getRegistrationDetails().getInvoiceDetails().getInvoiceValue()
								* basedOnInvoice.get().getTaxpercentinvoice() / 100f);

						Pair<Long, Double> lifeTax = finalLifeTaxCalculation(stagingRegistrationDetails, regServiceDTO,
								registrationDetails, totalLifeTax, basedOnInvoice.get().getTaxpercentinvoice(),
								isApplicationFromMvi, isChassesApplication, isOtherState, vcr, isVehicleSized,
								sizedDate);
						setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
						return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), lifeTax.getFirst(), lifeTax.getSecond(),
								lifTaxValidityCal(), 0l, 0d, payTaxType, "", 0d,null);
					} else if (basedOnsecoundVehicle.isPresent() && !vo.getRegistrationDetails().getIsFirstVehicle()) {

						totalLifeTax = (vo.getRegistrationDetails().getInvoiceDetails().getInvoiceValue()
								* basedOnsecoundVehicle.get().getSecondvehiclepercent() / 100f);
						Pair<Long, Double> lifeTax = finalLifeTaxCalculation(stagingRegistrationDetails, regServiceDTO,
								registrationDetails, totalLifeTax,
								basedOnsecoundVehicle.get().getSecondvehiclepercent(), isApplicationFromMvi,
								isChassesApplication, isOtherState, vcr, isVehicleSized, sizedDate);
						// stagingRegistrationDetails.setSecondVehicleTaxPaid(Boolean.TRUE);
						setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
						return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), lifeTax.getFirst(), lifeTax.getSecond(),
								lifTaxValidityCal(), 0l, 0d, payTaxType, "", 0d,null);
					} else {

						Pair<Long, Double> lifeTax = lifeTaxCalculation(isApplicationFromMvi, isChassesApplication,
								isOtherState, regServiceDTO, registrationDetails, stagingRegistrationDetails, vcr,
								isVehicleSized, sizedDate);
						setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
						return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), lifeTax.getFirst(), lifeTax.getSecond(),
								lifTaxValidityCal(), 0l, 0d, payTaxType, "", 0d,null);
					}
				}
			} else {
				if (!vo.getRegistrationDetails().isTaxPaidByVcr()) {
					if (vo.getRegistrationDetails().getApplicantType() != null
							&& !vo.getRegistrationDetails().getApplicantType().equalsIgnoreCase("withinthestate")) {
						Pair<Long, Double> lifeTax = lifeTaxCalculation(isApplicationFromMvi, isChassesApplication,
								isOtherState, regServiceDTO, registrationDetails, stagingRegistrationDetails, vcr,
								isVehicleSized, sizedDate);
						setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
						return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), lifeTax.getFirst(), lifeTax.getSecond(),
								lifTaxValidityCal(), 0l, 0d, payTaxType, "", 0d,null);
					}
				}
			}

		} else {
			if (!vo.getRegistrationDetails().isTaxPaidByVcr()) {
				if (vo.getRegistrationDetails().getApplicantType() != null
						&& !vo.getRegistrationDetails().getApplicantType().equalsIgnoreCase("withinthestate")) {
					Pair<Long, Double> lifeTax = lifeTaxCalculation(isApplicationFromMvi, isChassesApplication,
							isOtherState, regServiceDTO, registrationDetails, stagingRegistrationDetails, vcr,
							isVehicleSized, sizedDate);
					setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
					return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), lifeTax.getFirst(), lifeTax.getSecond(),
							lifTaxValidityCal(), 0l, 0d, payTaxType, "", 0d,null);
				}
			}
		}
		setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
		return null;
	}

	private TaxHelper quaterTaxCalculation(String applicationNo, boolean isApplicationFromMvi,
			boolean isChassesApplication, String taxType, boolean isOtherState, List<ServiceEnum> serviceEnum,
			String permitTypeCode, String routeCode, Optional<MasterTaxBased> taxCalBasedOn,
			RegServiceDTO regServiceDTO, RegistrationDetailsDTO registrationDetails,
			StagingRegistrationDetailsDTO stagingRegistrationDetails, String classOfvehicle,
			TaxTypeEnum.TaxPayType payTaxType, Optional<AlterationDTO> alterDetails, Boolean isWeightAlt,
			Pair<List<VcrFinalServiceDTO>, CitizenFeeDetailsInput> listOfVcrsDetails, boolean vcr, boolean isregestered,
			boolean voluntaryTax, LocalDate dateOfCompletion) {
		Optional<MasterTax> OptionalTax = null;
		if(listOfVcrsDetails!=null) {
		if(vcr && listOfVcrsDetails.getSecond().isVcrChassisTax()) {
			
			stagingRegistrationDetails = listOfVcrsDetails.getSecond().getStagingRegistrationDetails();
			//stagingRegistrationDetails.setClassOfVehicle(listOfVcrsDetails.getSecond().getStagingRegistrationDetails().getClassOfVehicleDesc());
		
			alterDetails=Optional.of(listOfVcrsDetails.getSecond().getAlterDetails());
			isChassesApplication=Boolean.TRUE;
		}
		}
		if (isChassesApplication) {
			// need to call body builder cov
			Optional<MasterTaxExcemptionsDTO> optionalTaxExcemption = masterTaxExcemptionsDAO.findByKeyvalueOrCovcode(
					stagingRegistrationDetails.getVahanDetails().getMakersModel(),
					stagingRegistrationDetails.getClassOfVehicle());
			if (optionalTaxExcemption.isPresent()) {
				//
				setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
				return returnTaxDetails(taxType, optionalTaxExcemption.get().getTaxvalue().longValue(), 0d,
						validity(taxType), 0l, 0d, payTaxType, "", 0d,null);
			}
			if (ownertypecheck(stagingRegistrationDetails.getOwnerType().getCode(), stagingRegistrationDetails.getClassOfVehicle())) {
				LocalDate TaxTill = validity(taxType);
				setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
				return returnTaxDetails(taxType, 0l, 0d, TaxTill, 0l, 0d, payTaxType, "", 0d,null);

			}
			if (!(stagingRegistrationDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
					|| stagingRegistrationDetails.getClassOfVehicle()
							.equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode())
					|| stagingRegistrationDetails.getClassOfVehicle()
							.equalsIgnoreCase(ClassOfVehicleEnum.ARVT.getCovCode()))) {

				if (checkTaxUpToDateOrNote(isApplicationFromMvi, isChassesApplication, regServiceDTO,
						registrationDetails, stagingRegistrationDetails, taxType, false)) {
					LocalDate TaxTill = validity(taxType);
					setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
					return returnTaxDetails(taxType, 0l, 0d, TaxTill, 0l, 0d, payTaxType, "", 0d,null);
				}
			}
			taxCalBasedOn = masterTaxBasedDAO.findByCovcode(alterDetails.get().getCov());

		} else if (isApplicationFromMvi) {
			if (taxType.equalsIgnoreCase(TaxTypeEnum.LifeTax.getDesc())) {
				taxType = TaxTypeEnum.QuarterlyTax.getDesc();
			}
			Optional<MasterTaxExcemptionsDTO> optionalTaxExcemption = masterTaxExcemptionsDAO.findByKeyvalueOrCovcode(
					regServiceDTO.getRegistrationDetails().getVahanDetails().getMakersModel(),
					regServiceDTO.getRegistrationDetails().getClassOfVehicle());
			if (optionalTaxExcemption.isPresent()) {
				//
				setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
				return returnTaxDetails(taxType, optionalTaxExcemption.get().getTaxvalue().longValue(), 0d,
						validity(taxType), 0l, 0d, payTaxType, "", 0d,null);
			}
			if (ownertypecheck(regServiceDTO.getRegistrationDetails().getOwnerType().getCode(), regServiceDTO.getRegistrationDetails().getClassOfVehicle())) {
				// regServiceDTO.setTaxAmount(0l);//need to retuen
				LocalDate TaxTill = validity(taxType);
				setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
				return returnTaxDetails(taxType, 0l, 0d, TaxTill, 0l, 0d, payTaxType, "", 0d,null);
				// return registrationDetails;
			}
			if (regServiceDTO.getAlterationDetails() != null && regServiceDTO.getAlterationDetails().getCov() != null) {
				classOfvehicle = regServiceDTO.getAlterationDetails().getCov();
				taxCalBasedOn = masterTaxBasedDAO.findByCovcode(regServiceDTO.getAlterationDetails().getCov());
			} else {
				/*
				 * if (checkTaxUpToDateOrNote(isApplicationFromMvi, isChassesApplication,
				 * regServiceDTO, registrationDetails, stagingRegistrationDetails, taxType)) {
				 * LocalDate TaxTill = validity(taxType); return returnTaxDetails(taxType, 0l,
				 * 0d, TaxTill); }
				 */
				classOfvehicle = regServiceDTO.getRegistrationDetails().getClassOfVehicle();
				taxCalBasedOn = masterTaxBasedDAO
						.findByCovcode(regServiceDTO.getRegistrationDetails().getClassOfVehicle());
			}
		} else if (isOtherState) {
			classOfvehicle = regServiceDTO.getRegistrationDetails().getClassOfVehicle();
			if ((regServiceDTO.getRegistrationDetails().getClassOfVehicle()
					.equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
					|| regServiceDTO.getRegistrationDetails().getClassOfVehicle()
							.equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode()))
					&& regServiceDTO.isMviDone()) {

				classOfvehicle = alterDetails.get().getCov();
			}
			if (!regServiceDTO.getRegistrationDetails().isTaxPaidByVcr()) {
				if (ownertypecheck(regServiceDTO.getRegistrationDetails().getOwnerType().getCode(), regServiceDTO.getRegistrationDetails().getClassOfVehicle())) {
					// regServiceDTO.setTaxAmount(0l);//need to retuen
					LocalDate TaxTill = validity(taxType);
					setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
					return returnTaxDetails(taxType, 0l, 0d, TaxTill, 0l, 0d, payTaxType, "", 0d,null);
					// return registrationDetails;
				}

				taxCalBasedOn = masterTaxBasedDAO.findByCovcode(classOfvehicle);
			} else {
				// classOfvehicle = regServiceDTO.getRegistrationDetails().getClassOfVehicle();
				taxCalBasedOn = masterTaxBasedDAO.findByCovcode(classOfvehicle);
			}
		} else if (vcr && !isregestered) {
			if ((listOfVcrsDetails.getSecond().isNoApplication())
					|| (listOfVcrsDetails.getSecond().getIsOtherState()
							&& listOfVcrsDetails.getSecond().isUnRegestered()
							&& listOfVcrsDetails.getSecond().isNoApplication())
					|| (!listOfVcrsDetails.getSecond().getIsOtherState()
							&& listOfVcrsDetails.getSecond().isUnRegestered()
							&& listOfVcrsDetails.getSecond().isNoApplication())) {
				// no application
				VcrFinalServiceDTO vcrDto = listOfVcrsDetails.getFirst().stream().findFirst().get();
				if(vcrDto.getRegistration().isOtherState() && vcrDto.isAnnualTax()){
					Optional<MasterTaxForVoluntary>  vlountaryTax = masterTaxForVoluntaryDAO.findByCovsAndToUlwGreaterThanEqualAndFromUlwLessThanEqualAndStatusTrue(
							vcrDto.getRegistration().getClasssOfVehicle().getCovcode(), vcrDto.getRegistration().getUlw(), vcrDto.getRegistration().getUlw());
					 if(vlountaryTax.isPresent()) {
							long tax = roundUpperTen(vlountaryTax.get().getOneYearTax());
							return returnTaxDetails(TaxTypeEnum.VoluntaryTaxType.OneYear.getTaxType(), tax, 0d,LocalDate.now().plusDays(TaxTypeEnum.VoluntaryTaxType.OneYear.getDays()) , 0l, 0d,
									TaxTypeEnum.TaxPayType.REG, "", 0d,null);
							
						}
				}
				taxCalBasedOn = masterTaxBasedDAO.findByCovcode(classOfvehicle);
			}

		} else {
			if (ownertypecheck(registrationDetails.getOwnerType().getCode(),registrationDetails.getClassOfVehicle())) {
				LocalDate TaxTill = validity(taxType);
				return returnTaxDetails(taxType, 0l, 0d, TaxTill, 0l, 0d, payTaxType, "", 0d,null);
			}
			Optional<MasterTaxExcemptionsDTO> optionalTaxExcemption = masterTaxExcemptionsDAO.findByKeyvalueOrCovcode(
					registrationDetails.getVahanDetails().getMakersModel(), registrationDetails.getClassOfVehicle());
			if (optionalTaxExcemption.isPresent()) {
				//
				setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
				return returnTaxDetails(taxType, optionalTaxExcemption.get().getTaxvalue().longValue(), 0d,
						validity(taxType), 0l, 0d, payTaxType, "", 0d,null);
			}
			// TODO need to skip newPermit and permit variation
			if (serviceEnum == null || serviceEnum.isEmpty()
					|| !serviceEnum.stream()
							.anyMatch(service -> Arrays.asList(ServiceEnum.NEWPERMIT, ServiceEnum.VARIATIONOFPERMIT,ServiceEnum.STAGECARRIAGEREPLACEMENTOFVEHICLE)
									.stream().anyMatch(serviceName -> serviceName.equals(service)))) {
				if (isWeightAlt == null || !isWeightAlt) {
					if (checkTaxUpToDateOrNote(isApplicationFromMvi, isChassesApplication, regServiceDTO,
							registrationDetails, stagingRegistrationDetails, taxType, vcr)) {
						LocalDate TaxTill = validity(taxType);
						return returnTaxDetails(taxType, 0l, 0d, TaxTill, 0l, 0d, payTaxType, "", 0d,null);
					}
				}

			}
			if(vcr&&listOfVcrsDetails.getSecond().isOldCovLife()) {
				classOfvehicle =listOfVcrsDetails.getSecond().getNewCov();
			}else {
				classOfvehicle = registrationDetails.getClassOfVehicle();
			}
			taxCalBasedOn = masterTaxBasedDAO.findByCovcode(classOfvehicle);
		}
		if (!taxCalBasedOn.isPresent()) {
			logger.error("No record found in master_taxbased for: " + classOfvehicle);
			// throw error message
			throw new BadRequestException("No record found in master_taxbased for: " + classOfvehicle);
		}
		if (taxCalBasedOn.get().getBasedon().equalsIgnoreCase(ulwCode)) {
			String permitType = permitcode;
			if (isChassesApplication) {
				// need to call body builder cov
				OptionalTax = getUlwTax(alterDetails.get().getCov(), alterDetails.get().getUlw(), stateCode,
						permitcode);

			} else if (isApplicationFromMvi) {
				if (regServiceDTO.getAlterationDetails() != null) {
					String cov = regServiceDTO.getAlterationDetails().getCov() != null
							? regServiceDTO.getAlterationDetails().getCov()
							: regServiceDTO.getRegistrationDetails().getClassOfVehicle();
					Integer ulw = regServiceDTO.getAlterationDetails().getUlw() != null
							? regServiceDTO.getAlterationDetails().getUlw()
							: regServiceDTO.getRegistrationDetails().getVahanDetails().getUnladenWeight();
					OptionalTax = getUlwTax(cov, ulw, stateCode, permitcode);
				} else {
					OptionalTax = getUlwTax(
							regServiceDTO.getRegistrationDetails().getVehicleDetails().getClassOfVehicle(),
							regServiceDTO.getRegistrationDetails().getVahanDetails().getUnladenWeight(), stateCode, permitcode);
				}

			} else if (isOtherState) {
				Integer ulw = null;
				if (regServiceDTO.getRegistrationDetails().getVahanDetails() != null) {
					ulw = regServiceDTO.getRegistrationDetails().getVahanDetails().getUnladenWeight();
				}
				String staeCode = "OS";
				if ((regServiceDTO.getRegistrationDetails().getClassOfVehicle()
						.equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
						|| regServiceDTO.getRegistrationDetails().getClassOfVehicle()
								.equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode()))
						&& regServiceDTO.isMviDone()) {
					ulw = alterDetails.get().getUlw() != null ? alterDetails.get().getUlw()
							: regServiceDTO.getRegistrationDetails().getVahanDetails().getUnladenWeight();
					staeCode = "AP";
				} else {
					ulw = this.getUlwWeight(regServiceDTO.getRegistrationDetails());
				}
				OptionalTax = getUlwTax(classOfvehicle, ulw, staeCode, permitcode);
			} else if (vcr && !isregestered) {
				if ((listOfVcrsDetails.getSecond().isNoApplication())
						|| (listOfVcrsDetails.getSecond().getIsOtherState()
								&& listOfVcrsDetails.getSecond().isUnRegestered()
								&& listOfVcrsDetails.getSecond().isNoApplication())
						|| (!listOfVcrsDetails.getSecond().getIsOtherState()
								&& listOfVcrsDetails.getSecond().isUnRegestered()
								&& listOfVcrsDetails.getSecond().isNoApplication())) {
					// no application
					OptionalTax = getUlwTax(classOfvehicle,
							listOfVcrsDetails.getFirst().stream().findFirst().get().getRegistration().getUlw(),
							stateCode, permitcode);
				}

			} else {

				// Pair<String, String> permitTypeAndRoutType =
				// getPermitCode(registrationDetails);
				permitType = permitTypeCode;

				// isBelongToPermitService = getAmethodToGetPemritService(serviceEnum);

				Optional<PropertiesDTO> propertiesOptional = propertiesDAO
						.findByCovsInAndPermitCodeTrue(classOfvehicle);
				if (!propertiesOptional.isPresent()) {
					permitTypeCode = permitcode;
					permitType = permitcode;
				}
				Integer ulw = this.getUlwWeight(registrationDetails);
				OptionalTax = getUlwTax(classOfvehicle, ulw, stateCode, permitType);

			}
			if (!OptionalTax.isPresent()) {
				logger.error("No record found in master_tax for: " + applicationNo);
				// throw error message
				throw new BadRequestException("No record found in master_tax for: " + applicationNo);
			}

			TaxCalculationHelper totalTaxAndValidity = quarterlyTaxCalculation(OptionalTax,
					taxCalBasedOn.get().getBasedon(), registrationDetails, regServiceDTO, isApplicationFromMvi,
					stagingRegistrationDetails, isChassesApplication, taxType, classOfvehicle, isOtherState,
					serviceEnum, permitTypeCode, routeCode, null, isWeightAlt, listOfVcrsDetails, vcr, isregestered,
					voluntaryTax, alterDetails);

			// registrationDetails.setTaxAmount(totalQuarterlyTax);
			// return stagingRegDetails;
			payTaxType = totalTaxAndValidity.getTaxPayType();
			if (isApplicationFromMvi || isChassesApplication) {
				payTaxType = TaxTypeEnum.TaxPayType.DIFF;
			}
			setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
			return returnTaxDetails(taxType, totalTaxAndValidity.getReoundTax(), totalTaxAndValidity.getPenality(),
					totalTaxAndValidity.getTaxTill(), totalTaxAndValidity.getReoundTaxArrears(),
					totalTaxAndValidity.getPenalityArrears(), payTaxType, permitType,
					totalTaxAndValidity.getQuaterTax(),totalTaxAndValidity.getLastTaxPaidUpTo());
		} else if (taxCalBasedOn.get().getBasedon().equalsIgnoreCase(rlwCode)) {
			String permitType = permitcode;
			if (isChassesApplication) {
				// need to call body builder cov
				Integer gvw = getGvwWeight(stagingRegistrationDetails.getApplicationNo(),
						stagingRegistrationDetails.getVahanDetails().getGvw(), voluntaryTax);
				OptionalTax = getRlwTax(alterDetails.get().getCov(), gvw, stateCode, permitcode);

			} else if (isApplicationFromMvi) {
				if (regServiceDTO.getAlterationDetails() != null) {
					String cov = regServiceDTO.getAlterationDetails().getCov() != null
							? regServiceDTO.getAlterationDetails().getCov()
							: regServiceDTO.getRegistrationDetails().getClassOfVehicle();
					Integer gvw = getGvwWeightForAlt(regServiceDTO);
					OptionalTax = getRlwTax(cov, gvw, stateCode, permitcode);
				} else {
					// need to chamge gvw
					// need to chek avt weight
					OptionalTax = getRlwTax(
							regServiceDTO.getRegistrationDetails().getVehicleDetails().getClassOfVehicle(),
							regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw(), stateCode, permitcode);
				}

			} else if (isOtherState) {
				Integer gvw = getGvwWeightForCitizen(regServiceDTO.getRegistrationDetails());
				String staeCode = "OS";
				if ((regServiceDTO.getRegistrationDetails().getClassOfVehicle()
						.equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
						|| regServiceDTO.getRegistrationDetails().getClassOfVehicle()
								.equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode()))
						&& regServiceDTO.isMviDone()) {
					gvw = getGvwWeight(regServiceDTO.getApplicationNo(),
							regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw(), voluntaryTax);
					staeCode = "AP";
				}
				OptionalTax = getRlwTax(classOfvehicle, gvw, staeCode, permitcode);

			} else if (vcr && !isregestered) {
				if ((listOfVcrsDetails.getSecond().isNoApplication())
						|| (listOfVcrsDetails.getSecond().getIsOtherState()
								&& listOfVcrsDetails.getSecond().isUnRegestered()
								&& listOfVcrsDetails.getSecond().isNoApplication())
						|| (!listOfVcrsDetails.getSecond().getIsOtherState()
								&& listOfVcrsDetails.getSecond().isUnRegestered()
								&& listOfVcrsDetails.getSecond().isNoApplication())) {
					// no application
					OptionalTax = getRlwTax(classOfvehicle,
							listOfVcrsDetails.getFirst().stream().findFirst().get().getRegistration().getGvwc(),
							stateCode, permitcode);
				}

			} else {
				Pair<String, String> permitTypeAndRoutType = getPermitCode(registrationDetails);
				permitType = permitTypeCode;
				// boolean isBelongToPermitService = false;
				// isBelongToPermitService = getAmethodToGetPemritService(serviceEnum);
				/* if (isBelongToPermitService) { */
				Optional<PropertiesDTO> propertiesOptional = propertiesDAO
						.findByCovsInAndPermitCodeTrue(classOfvehicle);
				if (!propertiesOptional.isPresent()) {
					permitTypeCode = permitcode;
					permitType = permitcode;
				}
				/* } */
				Integer gvw = getGvwWeightForCitizen(registrationDetails);
				OptionalTax = getRlwTax(classOfvehicle, gvw, stateCode, permitType);

			}
			if (!OptionalTax.isPresent()) {
				logger.error("No record found in master_tax for: " + applicationNo);
				// throw error message
				throw new BadRequestException("No record found in master_tax for: " + applicationNo);
			}

			TaxCalculationHelper totalTaxAndValidity = quarterlyTaxCalculation(OptionalTax,
					taxCalBasedOn.get().getBasedon(), registrationDetails, regServiceDTO, isApplicationFromMvi,
					stagingRegistrationDetails, isChassesApplication, taxType, classOfvehicle, isOtherState,
					serviceEnum, permitTypeCode, routeCode, null, isWeightAlt, listOfVcrsDetails, vcr, isregestered,
					voluntaryTax, alterDetails);

			// stagingRegDetails.setTaxAmount(totalQuarterlyTax);
			// stagingRegistrationDetailsDAO.save(stagingRegDetails);
			// return stagingRegDetails;
			payTaxType = totalTaxAndValidity.getTaxPayType();
			if (isApplicationFromMvi || isChassesApplication) {
				payTaxType = TaxTypeEnum.TaxPayType.DIFF;
			}
			setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
			return returnTaxDetails(taxType, totalTaxAndValidity.getReoundTax(), totalTaxAndValidity.getPenality(),
					totalTaxAndValidity.getTaxTill(), totalTaxAndValidity.getReoundTaxArrears(),
					totalTaxAndValidity.getPenalityArrears(), payTaxType, permitType,
					totalTaxAndValidity.getQuaterTax(),totalTaxAndValidity.getLastTaxPaidUpTo());
		} else if (taxCalBasedOn.get().getBasedon().equalsIgnoreCase(seatingCapacityCode)) {
			String permitType = StringUtils.EMPTY;
			if (isChassesApplication) {
				// need to call body builder cov
				OptionalTax = getSeatingCapacityTax(alterDetails.get().getCov(), alterDetails.get().getSeating(),
						stateCode, permitcode, null);

			} else if (isApplicationFromMvi) {
				Pair<String, String> permitTypeAndRoutType = getPermitCode(regServiceDTO.getRegistrationDetails());
				permitType = permitTypeAndRoutType.getFirst();
				routeCode = permitTypeAndRoutType.getSecond();
				if (regServiceDTO.getRegistrationDetails().getClassOfVehicle()
						.equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())) {
					permitType = this.permitcode;
					routeCode = StringUtils.EMPTY;
				}
				Optional<PropertiesDTO> propertiesOptional = propertiesDAO
						.findByCovsInAndPermitCodeTrue(classOfvehicle);
				if (!propertiesOptional.isPresent()) {
					permitTypeCode = permitcode;
					permitType = permitcode;
				}
				if (!regServiceDTO.getRegistrationDetails().getClassOfVehicle().equalsIgnoreCase(classOfvehicle)) {
					permitTypeCode = permitcode;
					permitType = permitcode;
				}
				Optional<PropertiesDTO> propertiesOptionalForObt = propertiesDAO
						.findByCovsInAndObtTaxTrue(regServiceDTO.getRegistrationDetails().getClassOfVehicle());
				
				if (regServiceDTO.getAlterationDetails() != null) {
					String cov = regServiceDTO.getAlterationDetails().getCov() != null
							? regServiceDTO.getAlterationDetails().getCov()
							: regServiceDTO.getRegistrationDetails().getClassOfVehicle();
							if (propertiesOptionalForObt.isPresent() && permitType.equalsIgnoreCase("INA") &&regServiceDTO.getAlterationDetails().getCov() == null && regServiceDTO.getAlterationDetails().getSeating() != null) {
							cov = ClassOfVehicleEnum.OBT.getCovCode();
							}
					String seating = regServiceDTO.getAlterationDetails().getSeating() != null
							? regServiceDTO.getAlterationDetails().getSeating()
							: regServiceDTO.getRegistrationDetails().getVahanDetails().getSeatingCapacity();
					OptionalTax = getSeatingCapacityTax(cov, seating, stateCode, permitType, routeCode);
				} else {
					OptionalTax = getSeatingCapacityTax(
							regServiceDTO.getRegistrationDetails().getVehicleDetails().getClassOfVehicle(),
							regServiceDTO.getRegistrationDetails().getVahanDetails().getSeatingCapacity(), stateCode,
							permitType, routeCode);
				}

			} else if (isOtherState) {
				String staeCode = "OS";
				String seating = regServiceDTO.getRegistrationDetails().getVahanDetails().getSeatingCapacity();
				if ((regServiceDTO.getRegistrationDetails().getClassOfVehicle()
						.equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
						|| regServiceDTO.getRegistrationDetails().getClassOfVehicle()
								.equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode()))
						&& regServiceDTO.isMviDone()) {
					seating = alterDetails.get().getSeating() != null ? alterDetails.get().getSeating()
							: regServiceDTO.getRegistrationDetails().getVahanDetails().getSeatingCapacity();
					staeCode = "AP";
				}
				OptionalTax = getSeatingCapacityTax(classOfvehicle, seating, staeCode, permitcode, null);

			} else if (vcr && !isregestered) {
				if ((listOfVcrsDetails.getSecond().isNoApplication())
						|| (listOfVcrsDetails.getSecond().getIsOtherState()
								&& listOfVcrsDetails.getSecond().isUnRegestered()
								&& listOfVcrsDetails.getSecond().isNoApplication())
						|| (!listOfVcrsDetails.getSecond().getIsOtherState()
								&& listOfVcrsDetails.getSecond().isUnRegestered()
								&& listOfVcrsDetails.getSecond().isNoApplication())) {
					// no application
					String seating = listOfVcrsDetails.getSecond().getSeats() != null ? listOfVcrsDetails.getSecond().getSeats()
							: String.valueOf(listOfVcrsDetails.getFirst()
									.stream().findFirst().get().getRegistration().getSeatingCapacity());
					OptionalTax = getSeatingCapacityTax(classOfvehicle, seating, stateCode, permitcode,
							null);
				}

			} else {
				Pair<String, String> permitTypeAndRoutType = getPermitCode(registrationDetails);
				permitType = permitTypeAndRoutType.getFirst();
				String routeCodes = permitTypeAndRoutType.getSecond();
				String seatingCapacity =registrationDetails.getVehicleDetails().getSeatingCapacity();
				if(vcr&& listOfVcrsDetails.getSecond().isOldCovLife()) {
					permitType = permitcode;
					if(StringUtils.isNoneBlank(listOfVcrsDetails.getSecond().getSeats())) {
						seatingCapacity = listOfVcrsDetails.getSecond().getSeats();
					}
				}
				boolean isBelongToPermitService = false;
				if (registrationDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())) {
					permitType = this.permitcode;
					routeCodes = StringUtils.EMPTY;
				}
				isBelongToPermitService = getAmethodToGetPemritService(serviceEnum);
				if (isBelongToPermitService) {
					Optional<PropertiesDTO> propertiesOptional = propertiesDAO
							.findByCovsInAndPermitCodeTrue(classOfvehicle);
					if (!propertiesOptional.isPresent()) {
						permitTypeCode = permitcode;
						permitType = permitcode;
					}
				}
				OptionalTax = getSeatingCapacityTax(classOfvehicle,
						seatingCapacity, stateCode, permitType,
						routeCodes);
			}

			if (!OptionalTax.isPresent()) {
				// throw error message
				throw new BadRequestException("No record found in master_tax for: " + applicationNo);
			}

			TaxCalculationHelper totalTaxAndValidity = quarterlyTaxCalculation(OptionalTax,
					taxCalBasedOn.get().getBasedon(), registrationDetails, regServiceDTO, isApplicationFromMvi,
					stagingRegistrationDetails, isChassesApplication, taxType, classOfvehicle, isOtherState,
					serviceEnum, permitTypeCode, routeCode, permitType, isWeightAlt, listOfVcrsDetails, vcr,
					isregestered, voluntaryTax, alterDetails);
			// stagingRegDetails.setTaxAmount(totalQuarterlyTax);
			// stagingRegistrationDetailsDAO.save(stagingRegDetails);
			// return stagingRegDetails;
			if (totalTaxAndValidity != null && totalTaxAndValidity.getTaxPayType() != null)
				payTaxType = totalTaxAndValidity.getTaxPayType();
			if (isApplicationFromMvi || isChassesApplication) {
				payTaxType = TaxTypeEnum.TaxPayType.DIFF;
			}
			setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
			return returnTaxDetails(taxType, totalTaxAndValidity.getReoundTax(), totalTaxAndValidity.getPenality(),
					totalTaxAndValidity.getTaxTill(), totalTaxAndValidity.getReoundTaxArrears(),
					totalTaxAndValidity.getPenalityArrears(), payTaxType, permitType,
					totalTaxAndValidity.getQuaterTax(),totalTaxAndValidity.getLastTaxPaidUpTo());
		}
		setObjectAsNull(stagingRegistrationDetails, registrationDetails, regServiceDTO, null, null);
		return returnTaxDetails(taxType, 0l, 0d, null, 0l, 0d, payTaxType, "", 0d,null);
	}

	@Override
	public boolean ownertypecheck(String code, String cov) {
		if (code.equalsIgnoreCase(OwnerTypeEnum.Government.getCode())
				|| code.equalsIgnoreCase(OwnerTypeEnum.POLICE.getCode())
				|| (code.equalsIgnoreCase(OwnerTypeEnum.Stu.getCode()) && cov.equalsIgnoreCase(ClassOfVehicleEnum.OBT.getCovCode()))) {
			return true;

		}
		return false;
	}

	private boolean getAmethodToGetPemritService(List<ServiceEnum> serviceEnum) {
		List<ServiceEnum> payVerify = new ArrayList<>();
		payVerify.add(ServiceEnum.NEWPERMIT);
		payVerify.add(ServiceEnum.RENEWALOFPERMIT);

		payVerify.add(ServiceEnum.VARIATIONOFPERMIT);
		payVerify.add(ServiceEnum.EXTENSIONOFVALIDITY);

		payVerify.add(ServiceEnum.RENEWALOFAUTHCARD);
		boolean verifyPaymentIntiation = payVerify.stream().anyMatch(val -> serviceEnum.contains(val));
		return verifyPaymentIntiation;
	}

	@Override
	public Pair<String, String> getPermitCode(RegistrationDetailsDTO registrationDetails) {
		List<PermitDetailsDTO> listOfPermits = permitDetailsDAO.findByPrNoAndPermitStatus(registrationDetails.getPrNo(),
				PermitsEnum.ACTIVE.getDescription());
		String permitType = permitcode;
		String routeCode = "";
		if (!listOfPermits.isEmpty()) {
			PermitDetailsDTO listOfPermsasits = listOfPermits.stream().filter(type -> type.getPermitType()
					.getTypeofPermit().equalsIgnoreCase(PermitsEnum.PermitType.PRIMARY.getPermitTypeCode())).findAny()
					.get();
			if (listOfPermsasits != null) {
				permitType = listOfPermsasits.getPermitType().getPermitType();
				if ((registrationDetails.getClassOfVehicle().equals(ClassOfVehicleEnum.COCT.getCovCode())
						|| registrationDetails.getClassOfVehicle().equals(ClassOfVehicleEnum.TOVT.getCovCode()))
						&& (listOfPermsasits.getRouteDetails() == null
								|| listOfPermsasits.getRouteDetails().getRouteType() == null || StringUtils
										.isBlank(listOfPermsasits.getRouteDetails().getRouteType().getRouteCode()))) {
					throw new BadRequestException("route code not found for pr: " + registrationDetails.getPrNo());
				}
				if (registrationDetails.getClassOfVehicle().equals(ClassOfVehicleEnum.COCT.getCovCode())
						|| registrationDetails.getClassOfVehicle().equals(ClassOfVehicleEnum.TOVT.getCovCode())) {
					routeCode = listOfPermsasits.getRouteDetails().getRouteType().getRouteCode();
				}

			}
		} else {
			boolean flag = Boolean.TRUE;
			LocalDate taxUpTo = this.validity(ServiceCodeEnum.QLY_TAX.getCode());
			LocalDate quaterStartDate = taxUpTo.minusMonths(3).plusDays(1);
			if (registrationDetails.getClassOfVehicle().equals(ClassOfVehicleEnum.COCT.getCovCode())
					|| registrationDetails.getClassOfVehicle().equals(ClassOfVehicleEnum.TOVT.getCovCode())) {
				List<RegServiceDTO> listOfRegService = regServiceDAO.findByprNoAndServiceIdsAndSourceIsNull(
						registrationDetails.getPrNo(), ServiceEnum.ALTERATIONOFVEHICLE.getId());
				if (listOfRegService != null && !listOfRegService.isEmpty()) {
					listOfRegService.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
					RegServiceDTO dto = listOfRegService.stream().findFirst().get();
					if (dto.getTaxvalidity() != null&&dto.getApplicationStatus().equals(StatusRegistration.APPROVED)) {
						if (dto.getAlterationDetails() != null
								&& (StringUtils.isNoneBlank(dto.getAlterationDetails().getCov())
								&& dto.getAlterationDetails().getCov()
										.equalsIgnoreCase(ClassOfVehicleEnum.COCT.getCovCode()))) {
							if (taxUpTo.equals(dto.getTaxvalidity())
									|| taxUpTo.isBefore(dto.getTaxvalidity())) {
								flag = Boolean.FALSE;
								permitType="CCP";
								routeCode = "O";
							}
						}
					}
				}
			}
			Optional<PermitDetailsDTO> listOfInActivePermits = permitDetailsDAO
					.findByPrNoAndPermitStatusAndPermitTypeTypeofPermitOrderByCreatedDateDesc(
							registrationDetails.getPrNo(), PermitsEnum.INACTIVE.getDescription(),
							PermitType.PRIMARY.getPermitTypeCode());
			if (listOfInActivePermits.isPresent()) {
				if (listOfInActivePermits.get().getPermitSurrenderDate() != null) {
					//boolean flag = Boolean.TRUE;
					
			
					if (listOfInActivePermits.get().getPermitSurrenderDate().isBefore(taxUpTo)
							&& listOfInActivePermits.get().getPermitSurrenderDate().isAfter(quaterStartDate)) {
						permitType = listOfInActivePermits.get().getPermitType().getPermitType();
						if ((registrationDetails.getClassOfVehicle().equals(ClassOfVehicleEnum.COCT.getCovCode())
								|| registrationDetails.getClassOfVehicle().equals(ClassOfVehicleEnum.TOVT.getCovCode()))
								&& (listOfInActivePermits.get().getRouteDetails() == null
										|| listOfInActivePermits.get().getRouteDetails().getRouteType() == null
										|| StringUtils.isBlank(listOfInActivePermits.get().getRouteDetails()
												.getRouteType().getRouteCode()))) {
						if(flag) {
							throw new BadRequestException(
									"route code not found for pr: " + registrationDetails.getPrNo());
						}
						}else if((registrationDetails.getClassOfVehicle().equals(ClassOfVehicleEnum.COCT.getCovCode())
								|| registrationDetails.getClassOfVehicle().equals(ClassOfVehicleEnum.TOVT.getCovCode()))
								&& (listOfInActivePermits.get().getRouteDetails() != null
										|| listOfInActivePermits.get().getRouteDetails().getRouteType() != null
										|| StringUtils.isNoneBlank(listOfInActivePermits.get().getRouteDetails()
												.getRouteType().getRouteCode()))){
							routeCode = listOfInActivePermits.get().getRouteDetails().getRouteType().getRouteCode();
						}
						/*if(flag) {
						if (registrationDetails.getClassOfVehicle().equals(ClassOfVehicleEnum.COCT.getCovCode())
								|| registrationDetails.getClassOfVehicle()
										.equals(ClassOfVehicleEnum.TOVT.getCovCode())) {
							routeCode = listOfInActivePermits.get().getRouteDetails().getRouteType().getRouteCode();
						}
						}else if (registrationDetails.getClassOfVehicle().equals(ClassOfVehicleEnum.COCT.getCovCode())
								|| registrationDetails.getClassOfVehicle()
								.equals(ClassOfVehicleEnum.TOVT.getCovCode())) {
				
							permitType="CCP";
							routeCode = "O";
						}*/
					}
				}
			}
		}
		if(registrationDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.SCRT.getCovCode())) {
			//permitType = permitcode;
		}
		return Pair.of(permitType, routeCode);
	}

	private Pair<Long, Double> lifeTaxCalculation(boolean isApplicationFromMvi, boolean isChassesApplication,
			boolean isOtherState, RegServiceDTO regServiceDTO, RegistrationDetailsDTO registrationDetails,
			StagingRegistrationDetailsDTO stagingRegistrationDetails, boolean vcr, boolean isVehicleSized,
			LocalDate sizedDate) {
		Double totalLifeTax;
		double vehicleAge;
		RegServiceVO vo = regServiceMapper.convertEntity(regServiceDTO);
		OtherStateApplictionType applicationType = getOtherStateVehicleStatus(vo);
		if (Arrays.asList(OtherStateApplictionType.ApplicationNO, OtherStateApplictionType.TrNo)
				.contains(applicationType)) {
			vehicleAge = 0;
		} else {
			LocalDate entryDate = getEarlerDate(regServiceDTO.getnOCDetails().getDateOfEntry(),
					regServiceDTO.getnOCDetails().getIssueDate());
			vehicleAge = calculateAgeOfTheVehicle(regServiceDTO.getRegistrationDetails().getPrIssueDate(), entryDate);

		}
		Optional<MasterTax> OptionalLifeTax = Optional.empty();
		if (regServiceDTO.getRegistrationDetails().getOwnerType().equals(OwnerTypeEnum.Individual) && !(regServiceDTO
				.getRegistrationDetails().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.MCYN.getCovCode())
				|| regServiceDTO.getRegistrationDetails().getClassOfVehicle()
						.equalsIgnoreCase(ClassOfVehicleEnum.IVCN.getCovCode()))) {
			OptionalLifeTax = taxTypeDAO
					.findByCovcodeAndOwnershiptypeIgnoreCaseAndStatecodeAndStatusAndToageGreaterThanEqualAndFromageLessThanEqualAndTocostGreaterThanEqualAndFromcostLessThanEqual(
							regServiceDTO.getRegistrationDetails().getClassOfVehicle(),
							regServiceDTO.getRegistrationDetails().getOwnerType().getCode(), "OS", regStatus,
							vehicleAge, vehicleAge,
							regServiceDTO.getRegistrationDetails().getInvoiceDetails().getInvoiceValue(),
							regServiceDTO.getRegistrationDetails().getInvoiceDetails().getInvoiceValue());

		} else {
			OptionalLifeTax = taxTypeDAO
					.findByCovcodeAndOwnershiptypeIgnoreCaseAndStatecodeAndStatusAndToageGreaterThanEqualAndFromageLessThanEqual(
							regServiceDTO.getRegistrationDetails().getClassOfVehicle(),
							regServiceDTO.getRegistrationDetails().getOwnerType().getCode(), "OS", regStatus,
							vehicleAge, vehicleAge);
		}
		if (!OptionalLifeTax.isPresent()) {
			logger.error(
					"No record found in master tax for " + regServiceDTO.getRegistrationDetails().getClassOfVehicle()
							+ " cov with Ownership type " + regServiceDTO.getRegistrationDetails().getOwnerType());
			// throw error message
			throw new BadRequestException(
					"No record found in master tax for " + regServiceDTO.getRegistrationDetails().getClassOfVehicle()
							+ " cov with Ownership type " + regServiceDTO.getRegistrationDetails().getOwnerType());
		}
		totalLifeTax = (regServiceDTO.getRegistrationDetails().getInvoiceDetails().getInvoiceValue()
				* OptionalLifeTax.get().getPercent() / 100f);
		Pair<Long, Double> lifeTax = finalLifeTaxCalculation(stagingRegistrationDetails, regServiceDTO,
				registrationDetails, totalLifeTax, OptionalLifeTax.get().getPercent(), isApplicationFromMvi,
				isChassesApplication, isOtherState, vcr, isVehicleSized, sizedDate);
		return lifeTax;
	}

	@Override
	public Pair<Optional<MasterPayperiodDTO>, Boolean> getPayPeroidForBoth(Optional<MasterPayperiodDTO> Payperiod,
			String seatingCapacity, Integer gvw) {
		Boolean gostatus = Boolean.FALSE;
		if (Payperiod.get().getCovcode().equalsIgnoreCase(ClassOfVehicleEnum.OBPN.getCovCode())) {
			if (Integer.parseInt(seatingCapacity) > 10) {
				Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
			} else {
				// gostatus = Boolean.TRUE;
				Payperiod.get().setPayperiod(TaxTypeEnum.LifeTax.getCode());
			}
		} else if (Payperiod.get().getCovcode().equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())) {
			if (Integer.parseInt(seatingCapacity) <= 4) {
				gostatus = Boolean.TRUE;
				Payperiod.get().setPayperiod(TaxTypeEnum.LifeTax.getCode());
			} else {
				Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
			}
		} else if (Payperiod.get().getCovcode().equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode())
				|| Payperiod.get().getCovcode().equalsIgnoreCase(ClassOfVehicleEnum.GCRT.getCovCode())) {
			if (gvw <= 3000) {
				gostatus = Boolean.TRUE;
				Payperiod.get().setPayperiod(TaxTypeEnum.LifeTax.getCode());
			} else {
				Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
			}
		}
		return Pair.of(Payperiod, gostatus);
	}

	@Override
	public List<String> taxTypes() {
		List<String> taxTypes = new ArrayList<>();
		taxTypes.add(ServiceCodeEnum.QLY_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.HALF_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.YEAR_TAX.getCode());
		return taxTypes;
	}

	@Override
	public boolean checkTaxUpToDateOrNote(boolean isApplicationFromMvi, boolean isChassesApplication,
			RegServiceDTO regServiceDTO, RegistrationDetailsDTO registrationDetails,
			StagingRegistrationDetailsDTO stagingRegistrationDetails, String taxType, boolean vcr) {
		LocalDate currentTaxValidity = validity(taxType);
		if (vcr) {
			return Boolean.FALSE;
		}
		TaxHelper lastTaxTillDate = getLastPaidTax(registrationDetails, regServiceDTO, isApplicationFromMvi,
				currentTaxValidity, stagingRegistrationDetails, isChassesApplication, taxTypes(), Boolean.FALSE, vcr);
		if (lastTaxTillDate != null && lastTaxTillDate.getTax() != null && lastTaxTillDate.getValidityTo() != null) {
			if (!lastTaxTillDate.isAnypendingQuaters()) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	private Optional<MasterTax> getSeatingCapacityTax(String classOfVehicle, String seatingCapacity, String stateCode,
			String permitcode, String routeCode) {
		Optional<MasterTax> OptionalTax;
		if (StringUtils.isNoneBlank(routeCode)) {
			OptionalTax = taxTypeDAO
					.findByCovcodeAndSeattoGreaterThanEqualAndSeatfromLessThanEqualAndStatecodeAndPermitcodeAndServTypeAndStatus(
							classOfVehicle, Integer.parseInt(seatingCapacity), Integer.parseInt(seatingCapacity),
							stateCode, permitcode, routeCode,regStatus);
		} else {
			OptionalTax = taxTypeDAO
					.findByCovcodeAndSeattoGreaterThanEqualAndSeatfromLessThanEqualAndStatecodeAndPermitcodeAndStatus(
							classOfVehicle, Integer.parseInt(seatingCapacity), Integer.parseInt(seatingCapacity),
							stateCode, permitcode,regStatus);
		}

		return OptionalTax;
	}

	private Optional<MasterTax> getRlwTax(String cov, Integer rlw, String stateCode, String permitType) {
		Optional<MasterTax> OptionalTax;
		if (StringUtils.isBlank(permitType)) {
			permitType = permitcode;
		}
		if (rlw == null || rlw == 0) {
			throw new BadRequestException("GVW weight not found");
		}
		OptionalTax = taxTypeDAO.findByCovcodeAndTorlwGreaterThanEqualAndFromrlwLessThanEqualAndStatecodeAndPermitcodeAndStatus(
				cov, rlw, rlw, stateCode, permitType,regStatus);

		return OptionalTax;
	}

	private Optional<MasterTax> getUlwTax(String cov, Integer ulw, String stateCode, String permitType) {
		Optional<MasterTax> OptionalTax;
		if (StringUtils.isBlank(permitType)) {
			permitType = permitcode;
		}
		if (ulw == null || ulw == 0) {
			throw new BadRequestException("ULW weight not found");
		}
		OptionalTax = taxTypeDAO.findByCovcodeAndToulwGreaterThanEqualAndFromulwLessThanEqualAndStatecodeAndPermitcodeAndStatus(
				cov, ulw, ulw, stateCode, permitType,regStatus);
		return OptionalTax;
	}

	private TaxCalculationHelper quarterlyTaxCalculation(Optional<MasterTax> OptionalTax, String taxBasedon,
			RegistrationDetailsDTO regDetails, RegServiceDTO regServiceDTO, boolean isApplicationFromMvi,
			StagingRegistrationDetailsDTO stagingRegistrationDetails, boolean isChassesApplication, String taxType,
			String classOfvehicle, boolean isOtherState, List<ServiceEnum> serviceEnum, String permitTypeCode,
			String routeCodes, String oldpermitType, Boolean isWeightAlt,
			Pair<List<VcrFinalServiceDTO>, CitizenFeeDetailsInput> listOfVcrsDetails, boolean vcr, boolean isregestered,
			boolean voluntaryTax, Optional<AlterationDTO> alterDetails) {
		/*
		 * if (StringUtils.isEmpty(taxTypes)) {
		 * logger.error("Tax type is missing in staging details [{}]." ,
		 * stagingRegDetails.getApplicationNo()); throw new
		 * BadRequestException("Tax type is missing in staging details: " +
		 * stagingRegDetails.getApplicationNo()); }
		 */
		Double totalTax = 0d;
		Integer quaternNumber = 0;
		Integer indexPosition = 0;
		List<Integer> quaterOne = new ArrayList<>();
		List<Integer> quaterTwo = new ArrayList<>();
		List<Integer> quaterThree = new ArrayList<>();
		List<Integer> quaterFour = new ArrayList<>();
		quaterOne.add(0, 4);
		quaterOne.add(1, 5);
		quaterOne.add(2, 6);
		quaterTwo.add(0, 7);
		quaterTwo.add(1, 8);
		quaterTwo.add(2, 9);
		quaterThree.add(0, 10);
		quaterThree.add(1, 11);
		quaterThree.add(2, 12);
		quaterFour.add(0, 1);
		quaterFour.add(1, 2);
		quaterFour.add(2, 3);

		Double currentquaterTax = 0d;
		Double currentquaterTaxArrears = 0d;
		Double quaterTax = 0d;
		Float tax = null;
		LocalDate Validity = null;
		LocalDate lastTaxPaidUpTo = null;
		Double penalits = 0d;
		Double penalitArrears = 0d;
		Optional<MasterTaxExcemptionsDTO> optionalTaxExcemption = Optional.empty();
		// Optional<AlterationDTO> alterDetails = Optional.empty();
		TaxTypeEnum.TaxPayType payTaxType = TaxTypeEnum.TaxPayType.REG;
		if (isApplicationFromMvi || isChassesApplication) {
			payTaxType = TaxTypeEnum.TaxPayType.DIFF;
		}
		if (isChassesApplication) {
			// need to call body builder cov
			if (!(voluntaryTax || vcr)) {
				alterDetails = alterationDao.findByApplicationNo(stagingRegistrationDetails.getApplicationNo());
				if (!alterDetails.isPresent()) {
					throw new BadRequestException("No record found in alteration document for: "
							+ stagingRegistrationDetails.getApplicationNo());
				}
			}

		}
		if (isOtherState) {
			if ((regServiceDTO.getRegistrationDetails().getClassOfVehicle()
					.equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
					|| regServiceDTO.getRegistrationDetails().getClassOfVehicle()
							.equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode()))
					&& regServiceDTO.isMviDone()) {
				alterDetails = alterationDao.findByApplicationNo(regServiceDTO.getApplicationNo());
				if (!alterDetails.isPresent()) {
					throw new BadRequestException(
							"No record found in alteration document for: " + regServiceDTO.getApplicationNo());
				}
			}
		}
		if (taxBasedon.equalsIgnoreCase(seatingCapacityCode)) {
			if (isChassesApplication) {
				// need to call body builder cov
				tax = (OptionalTax.get().getTaxamount() * (Integer.parseInt(alterDetails.get().getSeating()) - 1));

			} else if (isApplicationFromMvi) {
				permitTypeCode = oldpermitType;
				if (regServiceDTO.getAlterationDetails() != null
						&& regServiceDTO.getAlterationDetails().getSeating() != null) {
					tax = (OptionalTax.get().getTaxamount()
							* (Integer.parseInt(regServiceDTO.getAlterationDetails().getSeating()) - 1));
				} else {
					tax = (OptionalTax.get().getTaxamount() * (Integer.parseInt(
							regServiceDTO.getRegistrationDetails().getVahanDetails().getSeatingCapacity()) - 1));
				}
			} else if (isOtherState) {
				if (alterDetails != null && alterDetails.isPresent()
						&& StringUtils.isNoneBlank(alterDetails.get().getSeating())) {
					tax = (OptionalTax.get().getTaxamount() * (Integer.parseInt(alterDetails.get().getSeating()) - 1));
				} else {
					tax = (OptionalTax.get().getTaxamount() * (Integer.parseInt(
							regServiceDTO.getRegistrationDetails().getVahanDetails().getSeatingCapacity()) - 1));
				}
			} else if (vcr && !isregestered) {
				if ((listOfVcrsDetails.getSecond().isNoApplication())
						|| (listOfVcrsDetails.getSecond().getIsOtherState()
								&& listOfVcrsDetails.getSecond().isUnRegestered()
								&& listOfVcrsDetails.getSecond().isNoApplication())
						|| (!listOfVcrsDetails.getSecond().getIsOtherState()
								&& listOfVcrsDetails.getSecond().isUnRegestered()
								&& listOfVcrsDetails.getSecond().isNoApplication())) {
					// no application
					tax = (OptionalTax.get().getTaxamount() * (listOfVcrsDetails.getFirst().stream().findFirst().get()
							.getRegistration().getSeatingCapacity() - 1));
				}

			} else {
				if ((regDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TOVT.getCovCode())
						|| regDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.COCT.getCovCode()))
						&& StringUtils.isNoneBlank(oldpermitType)
						&& oldpermitType.equalsIgnoreCase(PermitsEnum.PermitCodes.AITP.getPermitCode())) {

					tax = (OptionalTax.get().getTaxamount()
							* (Integer.parseInt(regDetails.getVahanDetails().getSeatingCapacity()) - 1));
				} else {
					tax = (OptionalTax.get().getTaxamount()
							* (Integer.parseInt(regDetails.getVahanDetails().getSeatingCapacity()) - 1));
				}
			}

			TaxCalculationHelper taxAndQuaternNumber = plainTaxCalculation(tax, quaterOne, quaterTwo, quaterThree,
					quaterFour, regDetails, currentquaterTax, regServiceDTO, isApplicationFromMvi,
					stagingRegistrationDetails, isChassesApplication, classOfvehicle, isOtherState, taxType,
					serviceEnum, permitTypeCode, routeCodes, isWeightAlt, listOfVcrsDetails, vcr, isregestered);
			quaterTax = tax.doubleValue();
			if (taxAndQuaternNumber.getCurrentQuaterTax() != null && taxAndQuaternNumber.getCurrentQuaterTax() != 0) {
				payTaxType = TaxTypeEnum.TaxPayType.DIFF;
				quaterTax = taxAndQuaternNumber.getCurrentQuaterTax();
			}

			LocalDate currentTaxValidity = validity(taxType);
			if (taxType.equalsIgnoreCase(ServiceCodeEnum.CESS_FEE.getCode())) {
				Pair<Long, LocalDate> cessAndValidity = cessTaxCalculation(quaterTax, regDetails, currentTaxValidity,
						regServiceDTO, isApplicationFromMvi, stagingRegistrationDetails, isChassesApplication,
						classOfvehicle, isOtherState,listOfVcrsDetails,vcr);
				// getCesFee(tax.doubleValue(), classOfvehicle);
				TaxCalculationHelper taxCalculationHelper = new TaxCalculationHelper();
				// taxCalculationHelper.setQuaterTax(cessAndValidity.getFirst().doubleValue());
				taxCalculationHelper.setReoundTax(cessAndValidity.getFirst());
				taxCalculationHelper.setTaxTill(cessAndValidity.getSecond());
				return taxCalculationHelper;
			}
			currentquaterTax = taxAndQuaternNumber.getQuaterTax();
			quaternNumber = taxAndQuaternNumber.getQuaternNumber();
			indexPosition = taxAndQuaternNumber.getIndexPosition();
			penalits = taxAndQuaternNumber.getPenality();
			penalitArrears = taxAndQuaternNumber.getPenalityArrears();
			currentquaterTaxArrears = taxAndQuaternNumber.getQuaterTaxArrears();
			lastTaxPaidUpTo =taxAndQuaternNumber.getLastTaxPaidUpTo();

			if (isChassesApplication) {
				// need to call body builder cov
				optionalTaxExcemption = getSeatingCapacityExcemptionsTax(alterDetails.get().getCov(),
						alterDetails.get().getSeating(), oldpermitType);

			} else if (isApplicationFromMvi) {
				if (regServiceDTO.getAlterationDetails() != null) {
					String seating = regServiceDTO.getAlterationDetails().getSeating() != null
							? regServiceDTO.getAlterationDetails().getSeating()
							: regServiceDTO.getRegistrationDetails().getVahanDetails().getSeatingCapacity();
					String cov = regServiceDTO.getAlterationDetails().getCov() != null
							? regServiceDTO.getAlterationDetails().getCov()
							: regServiceDTO.getRegistrationDetails().getClassOfVehicle();
					optionalTaxExcemption = getSeatingCapacityExcemptionsTax(cov, seating, oldpermitType);
				} else {

					optionalTaxExcemption = getSeatingCapacityExcemptionsTax(
							regServiceDTO.getRegistrationDetails().getVehicleDetails().getClassOfVehicle(),
							regServiceDTO.getRegistrationDetails().getVahanDetails().getSeatingCapacity(),
							oldpermitType);
				}

			} else {
				if (isOtherState) {

					if (alterDetails != null && alterDetails.isPresent()) {
						String seating = alterDetails.get().getSeating() != null ? alterDetails.get().getSeating()
								: regServiceDTO.getRegistrationDetails().getVahanDetails().getSeatingCapacity();
						String cov = alterDetails.get().getCov();
						optionalTaxExcemption = getSeatingCapacityExcemptionsTax(cov, seating, oldpermitType);
					} else {

						optionalTaxExcemption = getSeatingCapacityExcemptionsTax(
								regServiceDTO.getRegistrationDetails().getVahanDetails().getVehicleClass(),
								regServiceDTO.getRegistrationDetails().getVahanDetails().getSeatingCapacity(),
								oldpermitType);
					}

				} else if (vcr && !isregestered) {
					if ((listOfVcrsDetails.getSecond().isNoApplication())
							|| (listOfVcrsDetails.getSecond().getIsOtherState()
									&& listOfVcrsDetails.getSecond().isUnRegestered()
									&& listOfVcrsDetails.getSecond().isNoApplication())
							|| (!listOfVcrsDetails.getSecond().getIsOtherState()
									&& listOfVcrsDetails.getSecond().isUnRegestered()
									&& listOfVcrsDetails.getSecond().isNoApplication())) {
						// no application
						optionalTaxExcemption = getSeatingCapacityExcemptionsTax(
								listOfVcrsDetails.getFirst().stream().findFirst().get().getRegistration()
										.getClasssOfVehicle().getCovcode(),
								String.valueOf(listOfVcrsDetails.getFirst().stream().findFirst().get().getRegistration()
										.getSeatingCapacity()),
								oldpermitType);
					}

				} else {
					optionalTaxExcemption = getSeatingCapacityExcemptionsTax(regDetails.getClassOfVehicle()!=null?regDetails.getClassOfVehicle():
							regDetails.getVehicleDetails().getClassOfVehicle(),
							regDetails.getVahanDetails().getSeatingCapacity(), oldpermitType);
				}
			}
			if (optionalTaxExcemption.isPresent()
					&& optionalTaxExcemption.get().getValuetype().equalsIgnoreCase(seatingCapacityCode)) {
				Validity = calculateTaxUpTo(indexPosition, quaternNumber);
				TaxHelper taxAndPenality = currentQuaterTaxCalculation(
						optionalTaxExcemption.get().getTaxvalue().doubleValue(), indexPosition, regDetails, Validity,
						regServiceDTO, isApplicationFromMvi, stagingRegistrationDetails, isChassesApplication,
						classOfvehicle, isOtherState, taxType, serviceEnum, permitTypeCode, routeCodes, isWeightAlt,
						listOfVcrsDetails, vcr, isregestered);
				quaterTax = optionalTaxExcemption.get().getTaxvalue().doubleValue();
				penalits = taxAndQuaternNumber.getPenality();
				if (taxAndPenality.getQuaterAmount() != null && taxAndPenality.getQuaterAmount() != 0) {
					payTaxType = TaxTypeEnum.TaxPayType.DIFF;
					quaterTax = taxAndPenality.getQuaterAmount();
				}

				if (taxType.equalsIgnoreCase(ServiceCodeEnum.CESS_FEE.getCode())) {
					Pair<Long, LocalDate> cessAndValidity = cessTaxCalculation(quaterTax, regDetails,
							currentTaxValidity, regServiceDTO, isApplicationFromMvi, stagingRegistrationDetails,
							isChassesApplication, classOfvehicle, isOtherState,listOfVcrsDetails,vcr);
					// getCesFee(optionalTaxExcemption.get().getTaxvalue().doubleValue(),
					// classOfvehicle);
					TaxCalculationHelper taxCalculationHelper = new TaxCalculationHelper();
					taxCalculationHelper.setReoundTax(cessAndValidity.getFirst());
					taxCalculationHelper.setTaxTill(cessAndValidity.getSecond());
					// penalits = taxAndQuaternNumber.getPenality();
					return taxCalculationHelper;
				}
				currentquaterTax = taxAndPenality.getTax();
				currentquaterTaxArrears = taxAndPenality.getTaxArrears();
				penalits = Double.valueOf(taxAndPenality.getPenalty());
				penalitArrears = Double.valueOf(taxAndPenality.getPenaltyArrears());
				lastTaxPaidUpTo =taxAndPenality.getLastTaxPaidUpTo();
			}
		} else if (OptionalTax.get().getIncrementalweight() == 0) {

			TaxCalculationHelper taxAndQuaternNumber = plainTaxCalculation(OptionalTax.get().getTaxamount(), quaterOne,
					quaterTwo, quaterThree, quaterFour, regDetails, currentquaterTax, regServiceDTO,
					isApplicationFromMvi, stagingRegistrationDetails, isChassesApplication, classOfvehicle,
					isOtherState, taxType, serviceEnum, permitTypeCode, routeCodes, isWeightAlt, listOfVcrsDetails, vcr,
					isregestered);
			quaterTax = OptionalTax.get().getTaxamount().doubleValue();
			penalits = taxAndQuaternNumber.getPenality();
			LocalDate currentTaxValidity = validity(taxType);
			if (taxAndQuaternNumber.getCurrentQuaterTax() != null && taxAndQuaternNumber.getCurrentQuaterTax() != 0) {
				payTaxType = TaxTypeEnum.TaxPayType.DIFF;
				quaterTax = taxAndQuaternNumber.getCurrentQuaterTax();
			}
			if (taxType.equalsIgnoreCase(ServiceCodeEnum.CESS_FEE.getCode())) {
				Pair<Long, LocalDate> cessAndValidity = cessTaxCalculation(quaterTax, regDetails, currentTaxValidity,
						regServiceDTO, isApplicationFromMvi, stagingRegistrationDetails, isChassesApplication,
						classOfvehicle, isOtherState,listOfVcrsDetails,vcr);
				// getCesFee(OptionalTax.get().getTaxamount().doubleValue(), classOfvehicle);
				TaxCalculationHelper taxCalculationHelper = new TaxCalculationHelper();
				taxCalculationHelper.setReoundTax(cessAndValidity.getFirst());
				taxCalculationHelper.setTaxTill(cessAndValidity.getSecond());
				penalits = taxAndQuaternNumber.getPenality();
				return taxCalculationHelper;
			}

			currentquaterTax = taxAndQuaternNumber.getQuaterTax();
			quaternNumber = taxAndQuaternNumber.getQuaternNumber();
			indexPosition = taxAndQuaternNumber.getIndexPosition();
			penalits = taxAndQuaternNumber.getPenality();
			currentquaterTaxArrears = taxAndQuaternNumber.getQuaterTaxArrears();
			penalitArrears = taxAndQuaternNumber.getPenalityArrears();
			lastTaxPaidUpTo =taxAndQuaternNumber.getLastTaxPaidUpTo();

		} else {
			if (taxBasedon.equalsIgnoreCase(ulwCode)) {
				LocalDate currentTaxValidity = validity(taxType);
				if (quaterOne.contains(LocalDate.now().getMonthValue())) {
					indexPosition = quaterOne.indexOf(LocalDate.now().getMonthValue());
					quaternNumber = 1;
					Validity = calculateTaxUpTo(indexPosition, quaternNumber);

					TaxCalculationHelper currentAndQuaterTax = ulwQuaterTax(OptionalTax, regDetails, indexPosition,
							currentTaxValidity, regServiceDTO, isApplicationFromMvi, stagingRegistrationDetails,
							isChassesApplication, classOfvehicle, taxType, isOtherState, serviceEnum, permitTypeCode,
							routeCodes, isWeightAlt, listOfVcrsDetails, vcr, isregestered);
					currentquaterTax = currentAndQuaterTax.getQuaterTax();
					quaterTax = currentAndQuaterTax.getCurrentQuaterTax();
					penalits = currentAndQuaterTax.getPenality();
					currentquaterTaxArrears = currentAndQuaterTax.getQuaterTaxArrears();
					penalitArrears = currentAndQuaterTax.getPenalityArrears();
					payTaxType = currentAndQuaterTax.getTaxPayType();
					lastTaxPaidUpTo =currentAndQuaterTax.getLastTaxPaidUpTo();
					// TODO:need to tall all quater

				} else if (quaterTwo.contains(LocalDate.now().getMonthValue())) {
					indexPosition = quaterTwo.indexOf(LocalDate.now().getMonthValue());
					quaternNumber = 2;
					Validity = calculateTaxUpTo(indexPosition, quaternNumber);
					TaxCalculationHelper currentAndQuaterTax = ulwQuaterTax(OptionalTax, regDetails, indexPosition,
							currentTaxValidity, regServiceDTO, isApplicationFromMvi, stagingRegistrationDetails,
							isChassesApplication, classOfvehicle, taxType, isOtherState, serviceEnum, permitTypeCode,
							routeCodes, isWeightAlt, listOfVcrsDetails, vcr, isregestered);
					currentquaterTax = currentAndQuaterTax.getQuaterTax();
					quaterTax = currentAndQuaterTax.getCurrentQuaterTax();
					penalits = currentAndQuaterTax.getPenality();
					currentquaterTaxArrears = currentAndQuaterTax.getQuaterTaxArrears();
					penalitArrears = currentAndQuaterTax.getPenalityArrears();
					payTaxType = currentAndQuaterTax.getTaxPayType();
					lastTaxPaidUpTo =currentAndQuaterTax.getLastTaxPaidUpTo();
				} else if (quaterThree.contains(LocalDate.now().getMonthValue())) {
					indexPosition = quaterThree.indexOf(LocalDate.now().getMonthValue());
					quaternNumber = 3;
					Validity = calculateTaxUpTo(indexPosition, quaternNumber);
					TaxCalculationHelper currentAndQuaterTax = ulwQuaterTax(OptionalTax, regDetails, indexPosition,
							currentTaxValidity, regServiceDTO, isApplicationFromMvi, stagingRegistrationDetails,
							isChassesApplication, classOfvehicle, taxType, isOtherState, serviceEnum, permitTypeCode,
							routeCodes, isWeightAlt, listOfVcrsDetails, vcr, isregestered);
					currentquaterTax = currentAndQuaterTax.getQuaterTax();
					quaterTax = currentAndQuaterTax.getCurrentQuaterTax();
					penalits = currentAndQuaterTax.getPenality();
					currentquaterTaxArrears = currentAndQuaterTax.getQuaterTaxArrears();
					penalitArrears = currentAndQuaterTax.getPenalityArrears();
					payTaxType = currentAndQuaterTax.getTaxPayType();
					lastTaxPaidUpTo =currentAndQuaterTax.getLastTaxPaidUpTo();
				} else if (quaterFour.contains(LocalDate.now().getMonthValue())) {
					indexPosition = quaterFour.indexOf(LocalDate.now().getMonthValue());
					quaternNumber = 4;
					Validity = calculateTaxUpTo(indexPosition, quaternNumber);
					TaxCalculationHelper currentAndQuaterTax = ulwQuaterTax(OptionalTax, regDetails, indexPosition,
							currentTaxValidity, regServiceDTO, isApplicationFromMvi, stagingRegistrationDetails,
							isChassesApplication, classOfvehicle, taxType, isOtherState, serviceEnum, permitTypeCode,
							routeCodes, isWeightAlt, listOfVcrsDetails, vcr, isregestered);
					currentquaterTax = currentAndQuaterTax.getQuaterTax();
					quaterTax = currentAndQuaterTax.getCurrentQuaterTax();
					penalits = currentAndQuaterTax.getPenality();
					currentquaterTaxArrears = currentAndQuaterTax.getQuaterTaxArrears();
					penalitArrears = currentAndQuaterTax.getPenalityArrears();
					payTaxType = currentAndQuaterTax.getTaxPayType();
					lastTaxPaidUpTo =currentAndQuaterTax.getLastTaxPaidUpTo();
				}

			} else if (taxBasedon.equalsIgnoreCase(rlwCode)) {
				LocalDate currentTaxValidity = validity(taxType);
				if (quaterOne.contains(LocalDate.now().getMonthValue())) {
					indexPosition = quaterOne.indexOf(LocalDate.now().getMonthValue());
					quaternNumber = 1;
					TaxCalculationHelper currentAndQuaterTax = rlwQuaterTax(OptionalTax, regDetails, indexPosition,
							currentTaxValidity, regServiceDTO, isApplicationFromMvi, stagingRegistrationDetails,
							isChassesApplication, classOfvehicle, taxType, isOtherState, serviceEnum, permitTypeCode,
							routeCodes, isWeightAlt, listOfVcrsDetails, vcr, isregestered, voluntaryTax);
					currentquaterTax = currentAndQuaterTax.getQuaterTax();
					quaterTax = currentAndQuaterTax.getCurrentQuaterTax();
					penalits = currentAndQuaterTax.getPenality();
					currentquaterTaxArrears = currentAndQuaterTax.getQuaterTaxArrears();
					penalitArrears = currentAndQuaterTax.getPenalityArrears();
					payTaxType = currentAndQuaterTax.getTaxPayType();
					lastTaxPaidUpTo =currentAndQuaterTax.getLastTaxPaidUpTo();
				} else if (quaterTwo.contains(LocalDate.now().getMonthValue())) {
					indexPosition = quaterTwo.indexOf(LocalDate.now().getMonthValue());
					quaternNumber = 2;
					TaxCalculationHelper currentAndQuaterTax = rlwQuaterTax(OptionalTax, regDetails, indexPosition,
							currentTaxValidity, regServiceDTO, isApplicationFromMvi, stagingRegistrationDetails,
							isChassesApplication, classOfvehicle, taxType, isOtherState, serviceEnum, permitTypeCode,
							routeCodes, isWeightAlt, listOfVcrsDetails, vcr, isregestered, voluntaryTax);
					currentquaterTax = currentAndQuaterTax.getQuaterTax();
					quaterTax = currentAndQuaterTax.getCurrentQuaterTax();
					penalits = currentAndQuaterTax.getPenality();
					currentquaterTaxArrears = currentAndQuaterTax.getQuaterTaxArrears();
					penalitArrears = currentAndQuaterTax.getPenalityArrears();
					payTaxType = currentAndQuaterTax.getTaxPayType();
					lastTaxPaidUpTo =currentAndQuaterTax.getLastTaxPaidUpTo();
				} else if (quaterThree.contains(LocalDate.now().getMonthValue())) {
					indexPosition = quaterThree.indexOf(LocalDate.now().getMonthValue());
					quaternNumber = 3;
					TaxCalculationHelper currentAndQuaterTax = rlwQuaterTax(OptionalTax, regDetails, indexPosition,
							currentTaxValidity, regServiceDTO, isApplicationFromMvi, stagingRegistrationDetails,
							isChassesApplication, classOfvehicle, taxType, isOtherState, serviceEnum, permitTypeCode,
							routeCodes, isWeightAlt, listOfVcrsDetails, vcr, isregestered, voluntaryTax);
					currentquaterTax = currentAndQuaterTax.getQuaterTax();
					quaterTax = currentAndQuaterTax.getCurrentQuaterTax();
					penalits = currentAndQuaterTax.getPenality();
					currentquaterTaxArrears = currentAndQuaterTax.getQuaterTaxArrears();
					penalitArrears = currentAndQuaterTax.getPenalityArrears();
					payTaxType = currentAndQuaterTax.getTaxPayType();
					lastTaxPaidUpTo =currentAndQuaterTax.getLastTaxPaidUpTo();
				} else if (quaterFour.contains(LocalDate.now().getMonthValue())) {
					indexPosition = quaterFour.indexOf(LocalDate.now().getMonthValue());
					quaternNumber = 4;
					TaxCalculationHelper currentAndQuaterTax = rlwQuaterTax(OptionalTax, regDetails, indexPosition,
							currentTaxValidity, regServiceDTO, isApplicationFromMvi, stagingRegistrationDetails,
							isChassesApplication, classOfvehicle, taxType, isOtherState, serviceEnum, permitTypeCode,
							routeCodes, isWeightAlt, listOfVcrsDetails, vcr, isregestered, voluntaryTax);
					currentquaterTax = currentAndQuaterTax.getQuaterTax();
					quaterTax = currentAndQuaterTax.getCurrentQuaterTax();
					penalits = currentAndQuaterTax.getPenality();
					currentquaterTaxArrears = currentAndQuaterTax.getQuaterTaxArrears();
					penalitArrears = currentAndQuaterTax.getPenalityArrears();
					payTaxType = currentAndQuaterTax.getTaxPayType();
					lastTaxPaidUpTo =currentAndQuaterTax.getLastTaxPaidUpTo();
				}
			}
		}
		if (taxType.equalsIgnoreCase(ServiceCodeEnum.CESS_FEE.getCode())) {
			Pair<Long, LocalDate> cessAndValidity = cessTaxCalculation(quaterTax, regDetails, Validity, regServiceDTO,
					isApplicationFromMvi, stagingRegistrationDetails, isChassesApplication, classOfvehicle,
					isOtherState,listOfVcrsDetails,vcr);
			// getCesFee(result.doubleValue(), classOfvehicle);
			TaxCalculationHelper taxCalculationHelper = new TaxCalculationHelper();
			taxCalculationHelper.setReoundTax(cessAndValidity.getFirst());
			taxCalculationHelper.setTaxTill(cessAndValidity.getSecond());
			return taxCalculationHelper;
		}
		TaxCalculationHelper taxCalculationHelper = new TaxCalculationHelper();
		if (taxType.equalsIgnoreCase(TaxTypeEnum.QuarterlyTax.getDesc())) {
			LocalDate quarterlyValidity = calculateTaxUpTo(indexPosition, quaternNumber);
			if (isWeightAlt != null && isWeightAlt) {
				TaxHelper lastTaxTillDate = getLastPaidTax(regDetails, regServiceDTO, isApplicationFromMvi,
						quarterlyValidity, stagingRegistrationDetails, isChassesApplication, this.taxTypes(),
						isOtherState, vcr);
				quarterlyValidity = lastTaxTillDate.getValidityTo();
			}
			if(vcr&&listOfVcrsDetails.getFirst().stream().findFirst().get().getRegistration().isOtherState()) {
				if(!listOfVcrsDetails.getSecond().isCalCulateQutTax()) {
				quarterlyValidity = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
				}
			}
			long roundtax = roundUpperTen(currentquaterTax);
			// TaxCalculationHelper taxCalculationHelper = new TaxCalculationHelper();
			taxCalculationHelper.setReoundTax(roundtax);
			taxCalculationHelper.setTaxTill(quarterlyValidity);
			taxCalculationHelper.setPenality(penalits);
			taxCalculationHelper.setReoundTaxArrears(roundUpperTen(currentquaterTaxArrears));
			taxCalculationHelper.setPenalityArrears(penalitArrears);
			taxCalculationHelper.setTaxPayType(payTaxType);
			taxCalculationHelper.setQuaterTax(quaterTax);
			taxCalculationHelper.setLastTaxPaidUpTo(lastTaxPaidUpTo);
			return taxCalculationHelper;

		} else if (taxType.equalsIgnoreCase(TaxTypeEnum.HalfyearlyTax.getDesc())) {
			LocalDate halfyearValidity = calculateTaxUpTo(indexPosition, quaternNumber);
			halfyearValidity = (halfyearValidity.plusMonths(3));
			halfyearValidity = halfyearValidity.withDayOfMonth(halfyearValidity.lengthOfMonth());
			totalTax = quaterTax + currentquaterTax;
			/*
			 * Optional<MasterTaxExcemptionsDTO> excemptionPercentage =
			 * masterTaxExcemptionsDAO .findByKeyvalue(classOfvehicle); if
			 * (excemptionPercentage.isPresent() &&
			 * !excemptionPercentage.get().getValuetype().equalsIgnoreCase(
			 * seatingCapacityCode)) { // check percentage discount double discount =
			 * (totalTax * excemptionPercentage.get().getTaxvalue()) / 100; totalTax =
			 * totalTax - discount; }
			 */
			long roundtax = roundUpperTen(totalTax);
			if (serviceEnum != null && !serviceEnum.isEmpty()
					&& serviceEnum.stream()
							.anyMatch(service -> Arrays.asList(ServiceEnum.NEWPERMIT, ServiceEnum.VARIATIONOFPERMIT)
									.stream().anyMatch(serviceName -> serviceName.equals(service)))) {
				if (currentquaterTax <= 0) {
					roundtax = 0l;
				}
			}
			if (isWeightAlt != null && isWeightAlt) {
				TaxHelper lastTaxTillDate = getLastPaidTax(regDetails, regServiceDTO, isApplicationFromMvi,
						halfyearValidity, stagingRegistrationDetails, isChassesApplication, this.taxTypes(),
						isOtherState, vcr);
				halfyearValidity = lastTaxTillDate.getValidityTo();
				roundtax = roundUpperTen(currentquaterTax);
			}

			taxCalculationHelper.setReoundTax(roundtax);
			taxCalculationHelper.setTaxTill(halfyearValidity);
			taxCalculationHelper.setPenality(penalits);
			taxCalculationHelper.setReoundTaxArrears(roundUpperTen(currentquaterTaxArrears));
			taxCalculationHelper.setPenalityArrears(penalitArrears);
			taxCalculationHelper.setTaxPayType(payTaxType);
			if (isApplicationFromMvi) {
				List<String> listTaxType = this.taxTypes();
				listTaxType.add(ServiceCodeEnum.LIFE_TAX.getCode());
				TaxHelper oldTaxDetails = getIsGreenTaxPending(
						regServiceDTO.getRegistrationDetails().getApplicationNo(), listTaxType, LocalDate.now(),
						regServiceDTO.getRegistrationDetails().getPrNo());
				setObjectAsNull(null, null, null, listTaxType, null);
				taxCalculationHelper.setReoundTax(roundUpperTen(currentquaterTax));
				if (oldTaxDetails.getValidityTo() != null) {
					taxCalculationHelper.setTaxTill(oldTaxDetails.getValidityTo());
				}
			}
			taxCalculationHelper.setQuaterTax(quaterTax);
			return taxCalculationHelper;
		} else if (taxType.equalsIgnoreCase(TaxTypeEnum.YearlyTax.getDesc())) {
			LocalDate yearValidity = calculateTaxUpTo(indexPosition, quaternNumber);
			yearValidity = (yearValidity.plusMonths(9));
			yearValidity = yearValidity.withDayOfMonth(yearValidity.lengthOfMonth());
			totalTax = (quaterTax * 3) + currentquaterTax;
			/*
			 * Optional<MasterTaxExcemptionsDTO> excemptionPercentage =
			 * masterTaxExcemptionsDAO .findByKeyvalue(classOfvehicle); if
			 * (excemptionPercentage.isPresent() &&
			 * !excemptionPercentage.get().getValuetype().equalsIgnoreCase(
			 * seatingCapacityCode)) { // check percentage discount double discount =
			 * (totalTax * excemptionPercentage.get().getTaxvalue()) / 100; totalTax =
			 * totalTax - discount; }
			 */
			long roundtax = roundUpperTen(totalTax);
			if (serviceEnum != null && !serviceEnum.isEmpty()
					&& serviceEnum.stream()
							.anyMatch(service -> Arrays.asList(ServiceEnum.NEWPERMIT, ServiceEnum.VARIATIONOFPERMIT)
									.stream().anyMatch(serviceName -> serviceName.equals(service)))) {
				if (currentquaterTax <= 0) {
					roundtax = 0l;
				}
			}
			if (isWeightAlt != null && isWeightAlt) {
				TaxHelper lastTaxTillDate = getLastPaidTax(regDetails, regServiceDTO, isApplicationFromMvi,
						yearValidity, stagingRegistrationDetails, isChassesApplication, this.taxTypes(), isOtherState,
						vcr);
				yearValidity = lastTaxTillDate.getValidityTo();
				roundtax = roundUpperTen(currentquaterTax);
			}

			// TaxCalculationHelper taxCalculationHelper = new TaxCalculationHelper();
			taxCalculationHelper.setReoundTax(roundtax);
			taxCalculationHelper.setTaxTill(yearValidity);
			taxCalculationHelper.setPenality(penalits);
			taxCalculationHelper.setReoundTaxArrears(roundUpperTen(currentquaterTaxArrears));
			taxCalculationHelper.setPenalityArrears(penalitArrears);
			taxCalculationHelper.setTaxPayType(payTaxType);
			if (isApplicationFromMvi) {
				List<String> listTaxType = this.taxTypes();
				listTaxType.add(ServiceCodeEnum.LIFE_TAX.getCode());
				TaxHelper oldTaxDetails = getIsGreenTaxPending(
						regServiceDTO.getRegistrationDetails().getApplicationNo(), listTaxType, LocalDate.now(),
						regServiceDTO.getRegistrationDetails().getPrNo());
				setObjectAsNull(null, null, null, listTaxType, null);
				taxCalculationHelper.setReoundTax(roundUpperTen(currentquaterTax));
				if (oldTaxDetails.getValidityTo() != null) {
					taxCalculationHelper.setTaxTill(oldTaxDetails.getValidityTo());
				}
			}
			taxCalculationHelper.setQuaterTax(quaterTax);
			return taxCalculationHelper;
		} else if (taxType.equalsIgnoreCase(TaxTypeEnum.LifeTax.getDesc())) {
			LocalDate quarterlyValidity = calculateTaxUpTo(indexPosition, quaternNumber);
			String cov = regServiceDTO != null ? regServiceDTO.getRegistrationDetails().getClassOfVehicle()
					: regDetails.getClassOfVehicle();
			String seats = regServiceDTO != null
					? regServiceDTO.getRegistrationDetails().getVahanDetails().getSeatingCapacity()
					: regDetails.getVahanDetails().getSeatingCapacity();
			Integer gvw = regServiceDTO != null ? regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw()
					: regDetails.getVahanDetails().getGvw();
			if ((cov.equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode()) && Integer.parseInt(seats) <= 4)
					|| (((cov.equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode()))
							|| cov.equalsIgnoreCase(ClassOfVehicleEnum.GCRT.getCovCode())) && gvw <= 3000)
					|| (cov.equalsIgnoreCase(ClassOfVehicleEnum.MTLT.getCovCode()))
					|| (cov.equalsIgnoreCase(ClassOfVehicleEnum.LTCT.getCovCode()))
					|| (cov.equalsIgnoreCase(ClassOfVehicleEnum.STCT.getCovCode()))) {

				long roundtax = roundUpperTen(currentquaterTax);
				// TaxCalculationHelper taxCalculationHelper = new TaxCalculationHelper();
				taxCalculationHelper.setReoundTax(roundtax);
				taxCalculationHelper.setTaxTill(quarterlyValidity);
				taxCalculationHelper.setPenality(penalits);
				taxCalculationHelper.setReoundTaxArrears(roundUpperTen(currentquaterTaxArrears));
				taxCalculationHelper.setPenalityArrears(penalitArrears);
				taxCalculationHelper.setTaxPayType(payTaxType);
				return taxCalculationHelper;
			}
		}

		return taxCalculationHelper;
	}

	private Optional<MasterTaxExcemptionsDTO> getSeatingCapacityExcemptionsTax(String classOfVehicle,
			String seatingCapacity, String permitCode) {
		Optional<MasterTaxExcemptionsDTO> optionalTaxExcemption;
		optionalTaxExcemption = masterTaxExcemptionsDAO.findByKeyvalueAndSeattoGreaterThanEqualAndSeatfromLessThanEqual(
				classOfVehicle, Integer.parseInt(seatingCapacity), Integer.parseInt(seatingCapacity));

		if (optionalTaxExcemption.isPresent()) {

			if (permitCode != null) {
				Optional<PropertiesDTO> optionalProperties = propertiesDAO.findByAllowQuaterTaxTrue();
				PropertiesDTO properties = optionalProperties.get();
				if (properties.getCovs().stream().anyMatch(cov -> cov.equalsIgnoreCase(classOfVehicle))) {
					if (PermitsEnum.PermitCodes.AITC.getPermitCode().equalsIgnoreCase(permitCode)) {
						optionalTaxExcemption.get().setTaxvalue(optionalTaxExcemption.get().getAllIndiataxvalue());
					}
				}
			}
		}
		return optionalTaxExcemption;
	}

	private TaxCalculationHelper ulwQuaterTax(Optional<MasterTax> OptionalTax, RegistrationDetailsDTO stagingRegDetails,
			Integer valu, LocalDate Validity, RegServiceDTO regServiceDTO, boolean isApplicationFromMvi,
			StagingRegistrationDetailsDTO stagingRegistrationDetails, boolean isChassesApplication,
			String classOfvehicle, String taxType, boolean isOtherState, List<ServiceEnum> serviceEnum,
			String permitTypeCode, String routeCode, Boolean isWeightAlt,
			Pair<List<VcrFinalServiceDTO>, CitizenFeeDetailsInput> listOfVcrsDetails, boolean vcr,
			boolean isregestered) {
		Double quaterTax1;
		Float weight = null;
		TaxHelper currentquaterTaxAndPenality = new TaxHelper();
		TaxCalculationHelper taxCalculationHelper = new TaxCalculationHelper();
		TaxTypeEnum.TaxPayType payTaxType = TaxTypeEnum.TaxPayType.REG;
		if (isChassesApplication) {
			// need to call body builder cov
			// need to call body builder cov
			Optional<AlterationDTO> alterDetails = alterationDao
					.findByApplicationNo(stagingRegistrationDetails.getApplicationNo());
			if (!alterDetails.isPresent()) {
				throw new BadRequestException(
						"No record found in master_tax for: " + stagingRegistrationDetails.getApplicationNo());
			}
			weight = (float) ((alterDetails.get().getUlw() - ((OptionalTax.get().getFromulw() - 1)))
					/ OptionalTax.get().getIncrementalweight().doubleValue());

		} else if (isApplicationFromMvi) {
			if (regServiceDTO.getAlterationDetails() != null && regServiceDTO.getAlterationDetails().getUlw() != null) {
				weight = (float) ((regServiceDTO.getAlterationDetails().getUlw()
						- ((OptionalTax.get().getFromulw() - 1)))
						/ OptionalTax.get().getIncrementalweight().doubleValue());
			} else {
				weight = (float) ((regServiceDTO.getRegistrationDetails().getVahanDetails().getUnladenWeight()
						- ((OptionalTax.get().getFromulw() - 1)))
						/ OptionalTax.get().getIncrementalweight().doubleValue());
			}
		} else if (isOtherState) {
			Integer ulw = this.getUlwWeight(regServiceDTO.getRegistrationDetails());
			weight = (float) ((ulw - ((OptionalTax.get().getFromulw() - 1)))
					/ OptionalTax.get().getIncrementalweight().doubleValue());
		} else if (vcr && !isregestered) {
			if ((listOfVcrsDetails.getSecond().isNoApplication())
					|| (listOfVcrsDetails.getSecond().getIsOtherState()
							&& listOfVcrsDetails.getSecond().isUnRegestered()
							&& listOfVcrsDetails.getSecond().isNoApplication())
					|| (!listOfVcrsDetails.getSecond().getIsOtherState()
							&& listOfVcrsDetails.getSecond().isUnRegestered()
							&& listOfVcrsDetails.getSecond().isNoApplication())) {
				// no application
				weight = (float) ((listOfVcrsDetails.getFirst().stream().findFirst().get().getRegistration().getUlw()
						- ((OptionalTax.get().getFromulw() - 1)))
						/ OptionalTax.get().getIncrementalweight().doubleValue());
			}

		} else {
			Integer ulw = this.getUlwWeight(stagingRegDetails);
			weight = (float) ((ulw - ((OptionalTax.get().getFromulw() - 1)))
					/ OptionalTax.get().getIncrementalweight().doubleValue());
		}

		Float result = (float) ((Math.ceil(weight.doubleValue()) * OptionalTax.get().getIncrementalamount())
				+ OptionalTax.get().getTaxamount());
		quaterTax1 = result.doubleValue();
		currentquaterTaxAndPenality = currentQuaterTaxCalculation(result.doubleValue(), valu, stagingRegDetails,
				Validity, regServiceDTO, isApplicationFromMvi, stagingRegistrationDetails, isChassesApplication,
				classOfvehicle, isOtherState, taxType, serviceEnum, permitTypeCode, routeCode, isWeightAlt,
				listOfVcrsDetails, vcr, isregestered);
		if (currentquaterTaxAndPenality.getQuaterAmount() != null
				&& currentquaterTaxAndPenality.getQuaterAmount() != 0) {
			payTaxType = TaxTypeEnum.TaxPayType.DIFF;
			quaterTax1 = currentquaterTaxAndPenality.getQuaterAmount();
		}

		taxCalculationHelper.setQuaterTax(currentquaterTaxAndPenality.getTax());
		taxCalculationHelper.setQuaterTaxArrears(currentquaterTaxAndPenality.getTaxArrears());
		taxCalculationHelper.setCurrentQuaterTax(quaterTax1);
		taxCalculationHelper.setPenality(Double.valueOf(currentquaterTaxAndPenality.getPenalty()));
		taxCalculationHelper.setPenalityArrears(Double.valueOf(currentquaterTaxAndPenality.getPenaltyArrears()));
		taxCalculationHelper.setTaxPayType(payTaxType);
		taxCalculationHelper.setLastTaxPaidUpTo(currentquaterTaxAndPenality.getLastTaxPaidUpTo());
		return taxCalculationHelper;
	}

	private TaxCalculationHelper rlwQuaterTax(Optional<MasterTax> OptionalTax, RegistrationDetailsDTO stagingRegDetails,
			Integer valu, LocalDate Validity, RegServiceDTO regServiceDTO, boolean isApplicationFromMvi,
			StagingRegistrationDetailsDTO stagingRegistrationDetails, boolean isChassesApplication,
			String classOfvehicle, String taxType, boolean isOtherState, List<ServiceEnum> serviceEnum,
			String permitTypeCode, String routeCode, Boolean isWeightAlt,
			Pair<List<VcrFinalServiceDTO>, CitizenFeeDetailsInput> listOfVcrsDetails, boolean vcr, boolean isregestered,
			boolean voluntaryTax) {
		TaxHelper currentquaterTaxAndPenality = new TaxHelper();
		TaxCalculationHelper taxCalculationHelper = new TaxCalculationHelper();
		TaxTypeEnum.TaxPayType payTaxType = TaxTypeEnum.TaxPayType.REG;
		Double quaterTax1;
		Double weight = null;
		if (isChassesApplication) {
			Integer gvw = getGvwWeight(stagingRegistrationDetails.getApplicationNo(),
					stagingRegistrationDetails.getVahanDetails().getGvw(), voluntaryTax);
			// need to call body builder cov
			weight = (double) ((gvw - ((OptionalTax.get().getFromrlw() - 1)))
					/ OptionalTax.get().getIncrementalweight().doubleValue());

		} else if (isApplicationFromMvi) {
			// need to chek avt weight
			// need to chamge gvw
			Integer gvw = getGvwWeightForAlt(regServiceDTO);
			weight = (double) ((gvw - ((OptionalTax.get().getFromrlw() - 1)))
					/ OptionalTax.get().getIncrementalweight().doubleValue());
		} else if (isOtherState) {
			Integer gvw = getGvwWeightForCitizen(regServiceDTO.getRegistrationDetails());
			weight = (double) ((gvw - ((OptionalTax.get().getFromrlw() - 1)))
					/ OptionalTax.get().getIncrementalweight().doubleValue());
		} else if (vcr && !isregestered) {
			if ((listOfVcrsDetails.getSecond().isNoApplication())
					|| (listOfVcrsDetails.getSecond().getIsOtherState()
							&& listOfVcrsDetails.getSecond().isUnRegestered()
							&& listOfVcrsDetails.getSecond().isNoApplication())
					|| (!listOfVcrsDetails.getSecond().getIsOtherState()
							&& listOfVcrsDetails.getSecond().isUnRegestered()
							&& listOfVcrsDetails.getSecond().isNoApplication())) {
				// no application
				weight = (double) ((listOfVcrsDetails.getFirst().stream().findFirst().get().getRegistration().getGvwc()
						- ((OptionalTax.get().getFromrlw() - 1)))
						/ OptionalTax.get().getIncrementalweight().doubleValue());
			}

		} else {
			Integer gvw = getGvwWeightForCitizen(stagingRegDetails);
			weight = (double) ((gvw - ((OptionalTax.get().getFromrlw() - 1)))
					/ OptionalTax.get().getIncrementalweight().doubleValue());
		}

		Double result = ((Math.ceil(weight) * OptionalTax.get().getIncrementalamount())
				+ OptionalTax.get().getTaxamount());
		quaterTax1 = result.doubleValue();

		currentquaterTaxAndPenality = currentQuaterTaxCalculation(result.doubleValue(), valu, stagingRegDetails,
				Validity, regServiceDTO, isApplicationFromMvi, stagingRegistrationDetails, isChassesApplication,
				classOfvehicle, isOtherState, taxType, serviceEnum, permitTypeCode, routeCode, isWeightAlt,
				listOfVcrsDetails, vcr, isregestered);
		if (currentquaterTaxAndPenality.getQuaterAmount() != null
				&& currentquaterTaxAndPenality.getQuaterAmount() != 0) {
			payTaxType = TaxTypeEnum.TaxPayType.DIFF;
			quaterTax1 = currentquaterTaxAndPenality.getQuaterAmount();
		}
		Optional<MasterTaxExcemptionsDTO> excemptionPercentage = masterTaxExcemptionsDAO.findByKeyvalue(classOfvehicle);
		if (excemptionPercentage.isPresent()
				&& !excemptionPercentage.get().getValuetype().equalsIgnoreCase(seatingCapacityCode)) {
			// check percentage discount
			double discount = (result.doubleValue() * excemptionPercentage.get().getTaxvalue()) / 100;
			quaterTax1 = result.doubleValue() - discount;
		}
		taxCalculationHelper.setQuaterTax(currentquaterTaxAndPenality.getTax());
		taxCalculationHelper.setQuaterTaxArrears(currentquaterTaxAndPenality.getTaxArrears());
		taxCalculationHelper.setCurrentQuaterTax(quaterTax1);
		taxCalculationHelper.setPenality(Double.valueOf(currentquaterTaxAndPenality.getPenalty()));
		taxCalculationHelper.setPenalityArrears(Double.valueOf(currentquaterTaxAndPenality.getPenaltyArrears()));
		taxCalculationHelper.setTaxPayType(payTaxType);
		taxCalculationHelper.setLastTaxPaidUpTo(currentquaterTaxAndPenality.getLastTaxPaidUpTo());
		return taxCalculationHelper;
	}

	private TaxCalculationHelper plainTaxCalculation(Float OptionalTax, List<Integer> quaterOne,
			List<Integer> quaterTwo, List<Integer> quaterThree, List<Integer> quaterFour,
			RegistrationDetailsDTO stagingRegDetails, Double currentquaterTax, RegServiceDTO regServiceDTO,
			boolean isApplicationFromMvi, StagingRegistrationDetailsDTO stagingRegistrationDetails,
			boolean isChassesApplication, String classOfvehicle, boolean isOtherState, String taxType,
			List<ServiceEnum> serviceEnum, String permitTypeCode, String routeCode, Boolean isWeightAlt,
			Pair<List<VcrFinalServiceDTO>, CitizenFeeDetailsInput> listOfVcrsDetails, boolean vcr,
			boolean isregestered) {
		TaxCalculationHelper taxCalculationHelper = new TaxCalculationHelper();
		Integer indexPosition = 0;
		Integer quaternNumber = 0;
		LocalDate Validity = null;
		TaxHelper taxAndPenality = new TaxHelper();
		;
		if (quaterOne.contains(LocalDate.now().getMonthValue())) {
			indexPosition = quaterOne.indexOf(LocalDate.now().getMonthValue());
			quaternNumber = 1;
			Validity = calculateTaxUpTo(indexPosition, quaternNumber);
			taxAndPenality = currentQuaterTaxCalculation(OptionalTax.doubleValue(), indexPosition, stagingRegDetails,
					Validity, regServiceDTO, isApplicationFromMvi, stagingRegistrationDetails, isChassesApplication,
					classOfvehicle, isOtherState, taxType, serviceEnum, permitTypeCode, routeCode, isWeightAlt,
					listOfVcrsDetails, vcr, isregestered);
			currentquaterTax = taxAndPenality.getTax();
		} else if (quaterTwo.contains(LocalDate.now().getMonthValue())) {
			indexPosition = quaterTwo.indexOf(LocalDate.now().getMonthValue());
			quaternNumber = 2;
			Validity = calculateTaxUpTo(indexPosition, quaternNumber);
			taxAndPenality = currentQuaterTaxCalculation(OptionalTax.doubleValue(), indexPosition, stagingRegDetails,
					Validity, regServiceDTO, isApplicationFromMvi, stagingRegistrationDetails, isChassesApplication,
					classOfvehicle, isOtherState, taxType, serviceEnum, permitTypeCode, routeCode, isWeightAlt,
					listOfVcrsDetails, vcr, isregestered);
			currentquaterTax = taxAndPenality.getTax();
		} else if (quaterThree.contains(LocalDate.now().getMonthValue())) {
			indexPosition = quaterThree.indexOf(LocalDate.now().getMonthValue());
			quaternNumber = 3;
			Validity = calculateTaxUpTo(indexPosition, quaternNumber);
			taxAndPenality = currentQuaterTaxCalculation(OptionalTax.doubleValue(), indexPosition, stagingRegDetails,
					Validity, regServiceDTO, isApplicationFromMvi, stagingRegistrationDetails, isChassesApplication,
					classOfvehicle, isOtherState, taxType, serviceEnum, permitTypeCode, routeCode, isWeightAlt,
					listOfVcrsDetails, vcr, isregestered);
			currentquaterTax = taxAndPenality.getTax();
		} else if (quaterFour.contains(LocalDate.now().getMonthValue())) {
			indexPosition = quaterFour.indexOf(LocalDate.now().getMonthValue());
			quaternNumber = 4;
			Validity = calculateTaxUpTo(indexPosition, quaternNumber);
			taxAndPenality = currentQuaterTaxCalculation(OptionalTax.doubleValue(), indexPosition, stagingRegDetails,
					Validity, regServiceDTO, isApplicationFromMvi, stagingRegistrationDetails, isChassesApplication,
					classOfvehicle, isOtherState, taxType, serviceEnum, permitTypeCode, routeCode, isWeightAlt,
					listOfVcrsDetails, vcr, isregestered);
			currentquaterTax = taxAndPenality.getTax();
		}
		Optional<MasterTaxExcemptionsDTO> excemptionPercentage = masterTaxExcemptionsDAO.findByKeyvalue(classOfvehicle);
		if (excemptionPercentage.isPresent()
				&& !excemptionPercentage.get().getValuetype().equalsIgnoreCase(seatingCapacityCode)) {
			// check percentage discount
			double discount = (OptionalTax.doubleValue() * excemptionPercentage.get().getTaxvalue()) / 100;
			taxAndPenality.setQuaterAmount( OptionalTax.doubleValue() - discount);
		}
		taxCalculationHelper.setQuaterTax(currentquaterTax);
		taxCalculationHelper.setQuaterTaxArrears(taxAndPenality.getTaxArrears());
		taxCalculationHelper.setIndexPosition(indexPosition);
		taxCalculationHelper.setQuaternNumber(quaternNumber);
		taxCalculationHelper.setTaxTill(Validity);
		taxCalculationHelper.setPenality(Double.valueOf(taxAndPenality.getPenalty()));
		taxCalculationHelper.setPenalityArrears(Double.valueOf(taxAndPenality.getPenaltyArrears()));
		taxCalculationHelper.setCurrentQuaterTax(taxAndPenality.getQuaterAmount());
		taxCalculationHelper.setLastTaxPaidUpTo(taxAndPenality.getLastTaxPaidUpTo());
		return taxCalculationHelper;
	}

	private TaxHelper getChassistax(Double OptionalTax, RegistrationDetailsDTO stagingRegDetails,
			LocalDate currentTaxTill, RegServiceDTO regServiceDTO, boolean isApplicationFromMvi,
			StagingRegistrationDetailsDTO stagingRegistrationDetails, boolean isChassesApplication,
			Pair<List<VcrFinalServiceDTO>, CitizenFeeDetailsInput> listOfVcrsDetails, boolean isOtherState, boolean vcr, boolean voluntaryTax) {
		Long totalMonths = null;
		Double quaterTax = 0d;
		Double taxArr = 0d;
		Double newCovPenality = 0d;
		Optional<AlterationDTO> alterDetails = Optional.empty();
		TaxHelper lastTaxTillDate = null;
		String trNo = null;
		boolean ispaisdThroughVCR = Boolean.FALSE;
		if (isOtherState) {
			if (regServiceDTO != null
					&& (regServiceDTO.getRegistrationDetails().getClassOfVehicle()
							.equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
							|| regServiceDTO.getRegistrationDetails().getClassOfVehicle()
									.equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode()))
					&& regServiceDTO.isMviDone()) {
				trNo = regServiceDTO.getRegistrationDetails().getTrNo();
				alterDetails = alterationDao.findByApplicationNo(regServiceDTO.getApplicationNo());
				if (!alterDetails.isPresent()) {
					throw new BadRequestException(
							"No record found in alteration  for: " + regServiceDTO.getApplicationNo());
				}
				if (regServiceDTO.getRegistrationDetails().isTaxPaidByVcr()) {
					ispaisdThroughVCR = Boolean.TRUE;
					if (regServiceDTO.getTaxDetails() == null || regServiceDTO.getTaxDetails().getPaymentDAte() == null
							|| regServiceDTO.getTaxDetails().getCollectedAmount() == null) {
						throw new BadRequestException(
								"VCR tax details not found  for: " + regServiceDTO.getApplicationNo());
					}
					Pair<Integer, Integer> monthpositionInQuater = getMonthposition(
							regServiceDTO.getTaxDetails().getPaymentDAte());
					LocalDate taxUpTo = calculateChassisTaxUpTo(monthpositionInQuater.getFirst(),
							monthpositionInQuater.getSecond(), regServiceDTO.getTaxDetails().getPaymentDAte());
					if (taxUpTo.isBefore(currentTaxTill)) {
						lastTaxTillDate = addTaxDetails(TaxTypeEnum.QuarterlyTax.getDesc(),
								regServiceDTO.getTaxDetails().getCollectedAmount().doubleValue(), taxUpTo, Boolean.TRUE,
								regServiceDTO.getTaxDetails().getPaymentDAte().atStartOfDay(), Boolean.FALSE);

					} else {
						lastTaxTillDate = addTaxDetails(TaxTypeEnum.QuarterlyTax.getDesc(),
								regServiceDTO.getTaxDetails().getCollectedAmount().doubleValue(), taxUpTo,
								Boolean.FALSE, regServiceDTO.getTaxDetails().getPaymentDAte().atStartOfDay(),
								Boolean.FALSE);
					}
				}
			}
		} else {
			trNo = stagingRegistrationDetails.getTrNo();
			alterDetails = alterationDao.findByApplicationNo(stagingRegistrationDetails.getApplicationNo());
			if (voluntaryTax) {
				if (regServiceDTO != null && regServiceDTO.getAlterationDetails() != null) {
					alterDetails = Optional.of(regServiceDTO.getAlterationDetails());
				}
			}else if(vcr) {
				alterDetails=Optional.of(listOfVcrsDetails.getSecond().getAlterDetails());
			}
			if (!alterDetails.isPresent()) {
				throw new BadRequestException(
						"No record found in alteration  for: " + stagingRegistrationDetails.getApplicationNo());
			}
		}

		List<String> taxTypes = taxTypes();
		if (!ispaisdThroughVCR) {
			lastTaxTillDate = getLastPaidTax(stagingRegDetails, regServiceDTO, isApplicationFromMvi, currentTaxTill,
					stagingRegistrationDetails, isChassesApplication, taxTypes, isOtherState, vcr);
		}

		if (lastTaxTillDate == null || lastTaxTillDate.getTax() == null || lastTaxTillDate.getValidityTo() == null) {
			throw new BadRequestException("TaxDetails not found");
		}
		if (lastTaxTillDate.isAnypendingQuaters()) {
			LocalDate lastTaxTill = lastTaxTillDate.getValidityTo();
			Pair<Integer, Integer> monthpositionInQuater = getMonthposition(alterDetails.get().getDateOfCompletion());
			Pair<Integer, Integer> currentMonthpositionInQuater = getMonthposition(LocalDate.now());
			// localDate = localDate.withDayOfMonth(localDate.getMonth().maxLength());;
			Double oldQuaterTax = getOldQuaterTax(stagingRegDetails, regServiceDTO, isApplicationFromMvi,
					stagingRegistrationDetails, isChassesApplication, isOtherState);
			if (lastTaxTillDate.getTaxPaidThroughVcr() != null && lastTaxTillDate.getTaxPaidThroughVcr()) {
				oldQuaterTax = lastTaxTillDate.getTax();
			}

			Double penalityArrears = 0d;
			Double penalitys = 0d;
			Double oldCovPenalityArrears = 0d;
			Double oldCovTaxArr = 0d;
			Double lastTaxPaidPerMOnth = oldQuaterTax / 3;
			Double currentTaxPerMOnth = OptionalTax / 3;

			LocalDate bodyBuildFirstQuatreTaxUpTo = calculateChassisTaxUpTo(monthpositionInQuater.getFirst(),
					monthpositionInQuater.getSecond(), alterDetails.get().getDateOfCompletion());
			LocalDate chassisTaxUpTo = bodyBuildFirstQuatreTaxUpTo;
			if (!bodyBuildFirstQuatreTaxUpTo.equals(lastTaxTill)) {
				chassisTaxUpTo = bodyBuildFirstQuatreTaxUpTo.minusMonths(3);
			}
			chassisTaxUpTo = chassisTaxUpTo.withDayOfMonth(chassisTaxUpTo.getMonth().maxLength());

			totalMonths = ChronoUnit.MONTHS.between(lastTaxTill.withDayOfMonth(lastTaxTill.getMonth().maxLength()),
					chassisTaxUpTo);
			totalMonths = Math.abs(totalMonths);
			totalMonths = totalMonths + 1;
			double quaters = totalMonths / 3;
			LocalDate taxUpTo = calculateTaxUpTo(currentMonthpositionInQuater.getFirst(),
					currentMonthpositionInQuater.getSecond());
			Long newCovtotalMonths = ChronoUnit.MONTHS.between(chassisTaxUpTo, taxUpTo);
			newCovtotalMonths = newCovtotalMonths + 1;
			double newCovquaters = newCovtotalMonths / 3;

			if (quaters >= 1) {
				Pair<Double, Double> oldCovtaxArrAndPenality = chassisPenalitTax(oldQuaterTax, (quaters), vcr);
				oldCovTaxArr = oldCovtaxArrAndPenality.getFirst();
				oldCovPenalityArrears = oldCovtaxArrAndPenality.getSecond();
			}
			Pair<Double, Integer> totalException = Pair.of(0d, 0);
			if (monthpositionInQuater.getFirst() == 0) {

				if (currentMonthpositionInQuater.getFirst() == 0) {
					quaterTax = OptionalTax;
					totalException = chassExceptionTax(alterDetails, lastTaxTill, monthpositionInQuater, oldQuaterTax,
							lastTaxPaidPerMOnth);
					if (newCovquaters > 1) {
						taxArr = OptionalTax;
					}
				} else if (currentMonthpositionInQuater.getFirst() == 1) {
					totalException = chassExceptionTax(alterDetails, lastTaxTill, monthpositionInQuater, oldQuaterTax,
							lastTaxPaidPerMOnth);
					penalitys = penalitys + ((OptionalTax) * 25) / 100;
					if (vcr) {
						penalitys = penalitys + ((OptionalTax) * 100) / 100;
					}
					quaterTax = OptionalTax;
					if (newCovquaters > 1) {
						taxArr = OptionalTax;
					}

				} else {
					totalException = chassExceptionTax(alterDetails, lastTaxTill, monthpositionInQuater, oldQuaterTax,
							lastTaxPaidPerMOnth);
					penalitys = penalitys + ((OptionalTax) * 50) / 100;
					if (vcr) {
						penalitys = penalitys + ((OptionalTax) * 200) / 100;
					}
					quaterTax = OptionalTax;
					if (newCovquaters > 1) {
						taxArr = OptionalTax;
					}
				}
			} else if (monthpositionInQuater.getFirst() == 1) {

				if (currentMonthpositionInQuater.getFirst() == 0) {
					quaterTax = OptionalTax;
					totalException = chassExceptionTax(alterDetails, lastTaxTill, monthpositionInQuater, oldQuaterTax,
							lastTaxPaidPerMOnth);
					if (newCovquaters > 1) {
						if (totalException.getFirst() > 0) {
							taxArr = OptionalTax;
						} else {
							taxArr = lastTaxPaidPerMOnth + (currentTaxPerMOnth * 2);
						}
					}
				} else if (currentMonthpositionInQuater.getFirst() == 1) {
					totalException = chassExceptionTax(alterDetails, lastTaxTill, monthpositionInQuater, oldQuaterTax,
							lastTaxPaidPerMOnth);

					quaterTax = OptionalTax;
					if (currentMonthpositionInQuater.getSecond() == monthpositionInQuater.getSecond()
							&& alterDetails.get().getDateOfCompletion().getYear() == LocalDate.now().getYear()) {
						quaterTax = lastTaxPaidPerMOnth + (currentTaxPerMOnth * 2);
						;
					}
					penalitys = penalitys + ((quaterTax) * 25) / 100;
					if (vcr) {
						penalitys = penalitys + ((quaterTax) * 100) / 100;
					}
					if (newCovquaters > 1) {
						if (totalException.getFirst() > 0) {
							taxArr = OptionalTax;
						} else {
							taxArr = lastTaxPaidPerMOnth + (currentTaxPerMOnth * 2);
						}
					}
				} else {
					totalException = chassExceptionTax(alterDetails, lastTaxTill, monthpositionInQuater, oldQuaterTax,
							lastTaxPaidPerMOnth);

					quaterTax = OptionalTax;
					if (currentMonthpositionInQuater.getSecond() == monthpositionInQuater.getSecond()
							&& alterDetails.get().getDateOfCompletion().getYear() == LocalDate.now().getYear()) {
						quaterTax = lastTaxPaidPerMOnth + (currentTaxPerMOnth * 2);
						;
					}
					penalitys = penalitys + ((quaterTax) * 50) / 100;
					if (vcr) {
						penalitys = penalitys + ((quaterTax) * 200) / 100;
					}
					if (newCovquaters > 1) {
						if (totalException.getFirst() > 0) {
							taxArr = OptionalTax;
						} else {
							taxArr = lastTaxPaidPerMOnth + (currentTaxPerMOnth * 2);
						}
					}
				}

			} else {

				if (currentMonthpositionInQuater.getFirst() == 0) {
					quaterTax = OptionalTax;
					totalException = chassExceptionTax(alterDetails, lastTaxTill, monthpositionInQuater, oldQuaterTax,
							lastTaxPaidPerMOnth);
					if (newCovquaters > 1) {
						if (totalException.getFirst() > 0) {
							taxArr = OptionalTax;
						} else {
							taxArr = (lastTaxPaidPerMOnth * 2) + (currentTaxPerMOnth);
						}
					}
				} else if (currentMonthpositionInQuater.getFirst() == 1) {
					totalException = chassExceptionTax(alterDetails, lastTaxTill, monthpositionInQuater, oldQuaterTax,
							lastTaxPaidPerMOnth);
					penalitys = penalitys + (OptionalTax * 25) / 100;
					if (vcr) {
						penalitys = penalitys + (OptionalTax * 100) / 100;
					}
					quaterTax = OptionalTax;
					if (newCovquaters > 1) {
						if (totalException.getFirst() > 0) {
							taxArr = OptionalTax;
						} else {
							taxArr = (lastTaxPaidPerMOnth * 2) + (currentTaxPerMOnth);
						}
					}
				} else {
					totalException = chassExceptionTax(alterDetails, lastTaxTill, monthpositionInQuater, oldQuaterTax,
							lastTaxPaidPerMOnth);
					quaterTax = OptionalTax;
					if (currentMonthpositionInQuater.getSecond() == monthpositionInQuater.getSecond()
							&& alterDetails.get().getDateOfCompletion().getYear() == LocalDate.now().getYear()) {
						quaterTax = (lastTaxPaidPerMOnth * 2) + (currentTaxPerMOnth);
					}
					penalitys = penalitys + ((quaterTax) * 50) / 100;
					if (vcr) {
						penalitys = penalitys + ((quaterTax) * 200) / 100;
					}
					if (newCovquaters > 1) {
						if (totalException.getFirst() > 0) {
							taxArr = OptionalTax;
						} else {
							taxArr = (lastTaxPaidPerMOnth * 2) + (currentTaxPerMOnth);
						}
					}
				}

			}

			if (newCovquaters > 2) {
				Pair<Double, Double> taxArrAndPenality = chassisPenalitTax(OptionalTax, (newCovquaters - 2), vcr);
				taxArr = taxArr + taxArrAndPenality.getFirst();
				penalityArrears = penalityArrears + taxArrAndPenality.getSecond();
			}

			Double taxExceptionForChst = 0d;
			if (totalException.getFirst() > 0) {
				taxExceptionForChst = ((currentTaxPerMOnth * totalException.getSecond()) - totalException.getFirst());
				if (taxExceptionForChst < 0) {
					taxExceptionForChst = 0d;
				}
				// quaterTax = quaterTax - totalException.getFirst();
				// quaterTax = quaterTax + (currentTaxPerMOnth * totalException.getSecond());
			}
			TaxHelper currenTax = returnTaxDetails(quaterTax, taxArr + oldCovTaxArr + taxExceptionForChst,
					penalitys + newCovPenality, (taxArr / 2) + (oldCovTaxArr / 2), 0d,lastTaxTill);
			this.checkTaxPaidAtBorderForChassis(currenTax, stagingRegistrationDetails);
			return this.overRideChassisTax(trNo, currenTax);

		} else {
			Pair<Integer, Integer> monthpositionInQuater = getMonthposition(alterDetails.get().getDateOfCompletion());
			LocalDate lastTaxTill = lastTaxTillDate.getValidityTo();
			if (lastTaxTillDate.getValidityTo().getMonthValue() == alterDetails.get().getDateOfCompletion()
					.getMonthValue()) {
				totalMonths = 1l;
			}
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
			String date1 = "01-" + String.valueOf(alterDetails.get().getDateOfCompletion().getMonthValue()) + "-"
					+ String.valueOf(alterDetails.get().getDateOfCompletion().getYear());
			LocalDate localDate = LocalDate.parse(date1, formatter);
			/*
			 * LocalDate newDate =
			 * localDate.withDayOfMonth(localDate.getMonth().maxLength());
			 */

			if (totalMonths == null) {
				totalMonths = ChronoUnit.MONTHS.between(lastTaxTill.withDayOfMonth(lastTaxTill.getMonth().maxLength()),
						localDate);
				totalMonths = Math.abs(totalMonths);
				totalMonths = totalMonths + 1;
			}

			Double oldQuaterTax = getOldQuaterTax(stagingRegDetails, regServiceDTO, isApplicationFromMvi,
					stagingRegistrationDetails, isChassesApplication, isOtherState);
			Double chassisTax = getpreviesQurterBuildTaxForVCRPiad(lastTaxTillDate, monthpositionInQuater, oldQuaterTax,stagingRegistrationDetails,OptionalTax,alterDetails);
			if (lastTaxTillDate.getTaxPaidThroughVcr() != null && lastTaxTillDate.getTaxPaidThroughVcr()) {
				oldQuaterTax = lastTaxTillDate.getTax();
			}
			Double lastTaxPaidPerMOnth = oldQuaterTax / 3 * 1;
			Double currentTaxPerMOnth = OptionalTax / 3 * 1;
			Double totalException = lastTaxPaidPerMOnth * totalMonths;
			if (monthpositionInQuater.getFirst() == 0) {
				quaterTax = (currentTaxPerMOnth * 3) - totalException;
			} else if (monthpositionInQuater.getFirst() == 1) {
				// penality =((oldQuaterTax * 25) / 100);
				quaterTax = (currentTaxPerMOnth * 2) - totalException;
				// quaterTax = quaterTax+oldQuaterTax;
			} else {
				// penality =((oldQuaterTax * 50) / 100);
				quaterTax = (currentTaxPerMOnth) - totalException;
				// quaterTax = quaterTax+oldQuaterTax;
			}
			if(quaterTax<0) {
				quaterTax=chassisTax;
			}
			// return Pair.of(quaterTax, 0d);
			TaxHelper currenTax = returnTaxDetails(quaterTax, 0d, 0d, 0d, 0d,lastTaxTill);
			this.checkTaxPaidAtBorderForChassis(currenTax, stagingRegistrationDetails);
			return this.overRideChassisTax(trNo, currenTax);

		}
	}
	private Double getpreviesQurterBuildTaxForVCRPiad(TaxHelper lastTaxTillDate,
			Pair<Integer, Integer> monthpositionInQuater, Double oldQuaterTax,StagingRegistrationDetailsDTO stagingRegistrationDetails,Double OptionalTax,
			Optional<AlterationDTO> alterDetails) {
		Double quaterTax = 0d;
		if (lastTaxTillDate.getTaxPaidThroughVcr() != null && lastTaxTillDate.getTaxPaidThroughVcr() && stagingRegistrationDetails.getTrGeneratedDate()!=null
				&& alterDetails!=null) {
			//Pair<Integer, Integer> monthpositionInCurrentQuater = getMonthposition(LocalDate.now());
			Pair<Integer, Integer> monthpositionTrGenaration = getMonthposition(stagingRegistrationDetails.getTrGeneratedDate().toLocalDate());
			if(monthpositionTrGenaration.getSecond()== monthpositionInQuater.getSecond() && 
					stagingRegistrationDetails.getTrGeneratedDate().toLocalDate().getYear()==alterDetails.get().getDateOfCompletion().getYear()) {
				Double lastTaxPaidPerMOnth = oldQuaterTax / 3 * 1;
				Double currentTaxPerMOnth = OptionalTax / 3 * 1;
				if (monthpositionInQuater.getFirst() == 0) {
					quaterTax = (currentTaxPerMOnth * 3) - (lastTaxPaidPerMOnth*3);
				} else if (monthpositionInQuater.getFirst() == 1) {
					// penality =((oldQuaterTax * 25) / 100);
					quaterTax = (currentTaxPerMOnth * 2) - (lastTaxPaidPerMOnth*2);
					// quaterTax = quaterTax+oldQuaterTax;
				} else {
					// penality =((oldQuaterTax * 50) / 100);
					quaterTax = (currentTaxPerMOnth) - (lastTaxPaidPerMOnth);
					// quaterTax = quaterTax+oldQuaterTax;
				}
			}
			//oldQuaterTax = lastTaxTillDate.getTax();
			
		}
		if(quaterTax<0) {
			quaterTax=0d;
		}
		return quaterTax;
	}
	public Pair<Double, Integer> chassExceptionTax(Optional<AlterationDTO> alterDetails, LocalDate lastTaxTill,
			Pair<Integer, Integer> monthpositionInQuater, Double oldQuaterTax, Double lastTaxPaidPerMOnth) {
		Double totalException = 0d;
		int months = 0;
		if (lastTaxTill.isAfter(alterDetails.get().getDateOfCompletion())) {
			if (monthpositionInQuater.getFirst() == 0) {
				totalException = oldQuaterTax;
				months = 3;
			} else if (monthpositionInQuater.getFirst() == 1) {
				totalException = lastTaxPaidPerMOnth * 2;
				months = 2;
			} else if (monthpositionInQuater.getFirst() == 2) {
				totalException = lastTaxPaidPerMOnth;
				months = 1;
			}
		}
		return Pair.of(totalException, months);
	}

	private TaxHelper returnTaxDetails(Double tax, Double taxArrears, Double penalty, Double penaltyArrears,
			Double currentTax ,LocalDate lastTaxValidityTo) {
		TaxHelper taxHelper = new TaxHelper();

		taxHelper.setTax(tax);
		taxHelper.setTaxArrears(taxArrears);
		taxHelper.setPenalty(roundUpperTen(penalty));
		taxHelper.setPenaltyArrears(roundUpperTen(penaltyArrears));
		taxHelper.setQuaterAmount(currentTax);
		if(lastTaxValidityTo != null) {
		taxHelper.setLastTaxPaidUpTo(lastTaxValidityTo);
		}
		return taxHelper;
	}

	public Pair<Double, Double> chassisPenalitTax(Double oldQuaterTax, double quaters, boolean vcr) {
		double penality = (((oldQuaterTax * 50) / 100) * (quaters));
		Double taxArr = (oldQuaterTax * (quaters));
		if (vcr) {
			penality = (((oldQuaterTax * 200) / 100) * (quaters));
			taxArr = (oldQuaterTax * (quaters));
		}
		return Pair.of(taxArr, penality);
		// return taxArr;

	}

	private Double calculateOtherStateCurrentQuaterTax(Double OptionalTax, Pair<Integer, Integer> monthpositionInQuater,
			boolean vcr) {
		Double penalityTax = 0d;
		if (monthpositionInQuater.getFirst() == 1) {
			penalityTax = ((OptionalTax * 25) / 100);
			if (vcr) {
				penalityTax = ((OptionalTax * 100) / 100);
			}
		} else if (monthpositionInQuater.getFirst() == 2) {

			penalityTax = ((OptionalTax * 50) / 100);
			if (vcr) {
				penalityTax = ((OptionalTax * 200) / 100);
			}
		}
		return penalityTax;
	}

	private TaxHelper getOtherStateTax(Double OptionalTax, RegistrationDetailsDTO stagingRegDetails,
			LocalDate currentTaxTill, RegServiceDTO regServiceDTO, boolean isApplicationFromMvi,
			StagingRegistrationDetailsDTO stagingRegistrationDetails, boolean isChassesApplication,
			String classOfvehicle, boolean isOtherState,
			Pair<List<VcrFinalServiceDTO>, CitizenFeeDetailsInput> listOfVcrsDetails, boolean vcr,
			boolean isregestered) {
		LocalDate entryDate = null;
		if ((regServiceDTO.getRegistrationDetails().getClassOfVehicle()
				.equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
				|| regServiceDTO.getRegistrationDetails().getClassOfVehicle()
						.equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode()))
				&& regServiceDTO.isMviDone()) {
			return getChassistax(OptionalTax, stagingRegDetails, currentTaxTill, regServiceDTO, isApplicationFromMvi,
					stagingRegistrationDetails, isChassesApplication, null, isOtherState, vcr, false);
		}
		if (regServiceDTO.getnOCDetails() != null) {
			entryDate = getEarlerDate(regServiceDTO.getnOCDetails().getDateOfEntry(),
					regServiceDTO.getnOCDetails().getIssueDate());
		} else {

			if (regServiceDTO.getOsDateofentry() != null) {
				entryDate = regServiceDTO.getOsDateofentry();
			} else {
				entryDate = LocalDate.now();
			}
		}
		boolean taxPaidThroughVcr = Boolean.FALSE;
		if (regServiceDTO.getTaxDetails() != null && StringUtils.isNoneBlank(regServiceDTO.getTaxDetails().getVcrno())
				&& StringUtils.isNoneBlank(regServiceDTO.getTaxDetails().getMvi())
				&& regServiceDTO.getTaxDetails().getPaymentDAte() != null) {
			Pair<Integer, Integer> indexPosiAndQuarterNo = this
					.getMonthposition(regServiceDTO.getTaxDetails().getPaymentDAte());
			LocalDate taxValidityForVcr = this.calculateChassisTaxUpTo(indexPosiAndQuarterNo.getFirst(),
					indexPosiAndQuarterNo.getSecond(), regServiceDTO.getTaxDetails().getPaymentDAte());
			entryDate = taxValidityForVcr;
			taxPaidThroughVcr = Boolean.TRUE;
		}
		return getOtherStatePenalityAndTax(OptionalTax, vcr, entryDate, taxPaidThroughVcr);

	}

	private TaxHelper getOtherStatePenalityAndTax(Double OptionalTax, boolean vcr, LocalDate entryDate,
			boolean taxPaidThroughVcr) {
		Pair<Integer, Integer> entryMonthpositionInQuater = getMonthposition(entryDate);
		Pair<Integer, Integer> currentMonthpositionInQuater = getMonthposition(LocalDate.now());
		Double currentTaxPerMOnth = OptionalTax / 3;

		if (!(entryMonthpositionInQuater.getSecond().equals(currentMonthpositionInQuater.getSecond())
				&& entryDate.getYear() == LocalDate.now().getYear())) {
			Double taxArr = 0d;
			Pair<Double, Double> taxArr2AndPenality = null;
			Double penalityArr = 0d;
			// int monthsToSudtract=0;
			Long totalMonthsForPenality = 0l;

			LocalDate taxStartsFrom = calculateTaxFrom(entryMonthpositionInQuater.getFirst(),
					entryMonthpositionInQuater.getSecond());
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
			String date1 = "01-" + String.valueOf(taxStartsFrom.getMonthValue()) + "-"
					+ String.valueOf(entryDate.getYear());
			LocalDate localDate = LocalDate.parse(date1, formatter);
			LocalDate taxEndDate = calculateTaxUpTo(currentMonthpositionInQuater.getFirst(),
					currentMonthpositionInQuater.getSecond());

			totalMonthsForPenality = ChronoUnit.MONTHS.between(localDate, taxEndDate);
			totalMonthsForPenality = Math.abs(totalMonthsForPenality);
			totalMonthsForPenality = totalMonthsForPenality + 1;
			double Penalityquaters = totalMonthsForPenality / 3d;
			double noOfquaters = Penalityquaters - 2;
			if (!taxPaidThroughVcr) {
				if (entryMonthpositionInQuater.getFirst() == 0) {
					// penalityArr = penalityArr + (((OptionalTax * 50) / 100));
					taxArr = taxArr + (OptionalTax);
				} else if (entryMonthpositionInQuater.getFirst() == 1) {
					// OptionalTax = currentTaxPerMOnth * 2;
					// penalityArr = penalityArr + ((((currentTaxPerMOnth*2) * 50) / 100));
					taxArr = taxArr + (currentTaxPerMOnth * 2);
				} else {
					// OptionalTax = currentTaxPerMOnth;
					// penalityArr = penalityArr + (((currentTaxPerMOnth * 50) / 100));
					taxArr = taxArr + (currentTaxPerMOnth);
				}
				// noOfquaters = Penalityquaters - 2;
			}
			if (noOfquaters > 0) {
				taxArr2AndPenality = chassisPenalitTax(OptionalTax, noOfquaters, vcr);
				taxArr = taxArr + taxArr2AndPenality.getFirst();
				penalityArr = penalityArr + taxArr2AndPenality.getSecond();
			}

			Double currentpenality = calculateOtherStateCurrentQuaterTax(OptionalTax, currentMonthpositionInQuater,
					vcr);
			return returnTaxDetails(OptionalTax, taxArr, currentpenality, penalityArr, 0d,null);

		} else {
			if (entryMonthpositionInQuater.getFirst() == 0) {
				// OptionalTax = OptionalTax;
			} else if (entryMonthpositionInQuater.getFirst() == 1) {
				OptionalTax = currentTaxPerMOnth * 2;
			} else {
				OptionalTax = currentTaxPerMOnth;
			}
			return returnTaxDetails(OptionalTax, 0d, 0d, 0d, 0d,null);
		}
	}

	@Override
	public Pair<Integer, Integer> getMonthposition(LocalDate date) {

		List<Integer> quaterOne = new ArrayList<>();
		List<Integer> quaterTwo = new ArrayList<>();
		List<Integer> quaterThree = new ArrayList<>();
		List<Integer> quaterFour = new ArrayList<>();
		quaterOne.add(0, 4);
		quaterOne.add(1, 5);
		quaterOne.add(2, 6);
		quaterTwo.add(0, 7);
		quaterTwo.add(1, 8);
		quaterTwo.add(2, 9);
		quaterThree.add(0, 10);
		quaterThree.add(1, 11);
		quaterThree.add(2, 12);
		quaterFour.add(0, 1);
		quaterFour.add(1, 2);
		quaterFour.add(2, 3);
		Integer indexPosition = 0;
		Integer quaternNumber = 0;
		if (quaterOne.contains(date.getMonthValue())) {
			quaternNumber = 1;
			indexPosition = quaterOne.indexOf(date.getMonthValue());

		} else if (quaterTwo.contains(date.getMonthValue())) {
			quaternNumber = 2;
			indexPosition = quaterTwo.indexOf(date.getMonthValue());

		} else if (quaterThree.contains(date.getMonthValue())) {
			quaternNumber = 3;
			indexPosition = quaterThree.indexOf(date.getMonthValue());
		} else if (quaterFour.contains(date.getMonthValue())) {
			quaternNumber = 4;
			indexPosition = quaterFour.indexOf(date.getMonthValue());
		}
		return Pair.of(indexPosition, quaternNumber);
	}

	private TaxHelper currentQuaterTaxCalculation(Double OptionalTax, Integer valu,
			RegistrationDetailsDTO stagingRegDetails, LocalDate currentTaxTill, RegServiceDTO regServiceDTO,
			boolean isApplicationFromMvi, StagingRegistrationDetailsDTO stagingRegistrationDetails,
			boolean isChassesApplication, String classOfvehicle, boolean isOtherState, String taxType,
			List<ServiceEnum> serviceEnum, String permitTypeCode, String routeCode, Boolean isWeightAlt,
			Pair<List<VcrFinalServiceDTO>, CitizenFeeDetailsInput> listOfVcrsDetails, boolean vcr,
			boolean isregestered) {
		Double quaterTax = null;
		Double finalquaterTax = OptionalTax;
		Double vcrTax = 0d;
		Long totalMonths = null;
		Double penality = 0d;
		Double taxArrears = 0d;
		Double penaltyArrears = 0d;
		Double newPermitTax = 0d;
		boolean taxPaidThroughVcr = false;
		// List<String> taxTypes = taxTypes();
		Optional<MasterTaxExcemptionsDTO> excemptionPercentage = masterTaxExcemptionsDAO.findByKeyvalue(classOfvehicle);
		if (excemptionPercentage.isPresent()
				&& !excemptionPercentage.get().getValuetype().equalsIgnoreCase(seatingCapacityCode)) {
			// check percentage discount
			double discount = (OptionalTax * excemptionPercentage.get().getTaxvalue()) / 100;
			OptionalTax = OptionalTax - discount;
		}
		TaxHelper vcrDetails = vcrIntegration(listOfVcrsDetails, vcr);
		if (isChassesApplication) {
			boolean voluntaryTax = false;
			if (serviceEnum != null && !serviceEnum.isEmpty()
					&& serviceEnum.stream().anyMatch(type -> type.equals(ServiceEnum.VOLUNTARYTAX))) {
				voluntaryTax = true;
			}
			return getChassistax(OptionalTax, stagingRegDetails, currentTaxTill, regServiceDTO, isApplicationFromMvi,
					stagingRegistrationDetails, isChassesApplication, listOfVcrsDetails, isOtherState, vcr,
					voluntaryTax);

		} else if (isOtherState) {
			if ((regServiceDTO.getRegistrationDetails().getClassOfVehicle()
					.equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
					|| regServiceDTO.getRegistrationDetails().getClassOfVehicle()
							.equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode()))
					&& regServiceDTO.isMviDone()) {

				TaxHelper currenTax = getOtherStateTax(OptionalTax, stagingRegDetails, currentTaxTill, regServiceDTO,
						isApplicationFromMvi, stagingRegistrationDetails, isChassesApplication, classOfvehicle,
						isOtherState, listOfVcrsDetails, vcr, isregestered);
				Pair<String, String> prAndTr = getTrAndPrNo(stagingRegistrationDetails, regServiceDTO,
						stagingRegDetails, null, null, isApplicationFromMvi, isChassesApplication, isOtherState);
				return this.finalOverRideTaxForAllCasses(prAndTr.getFirst(), prAndTr.getSecond(), currenTax);
			}
			this.checkTaxPaidAtBorder(regServiceDTO);
			if (regServiceDTO.getRegistrationDetails().isTaxPaidByVcr()) {
				if (regServiceDTO.getTaxDetails() != null && regServiceDTO.getTaxDetails().getPaymentDAte() != null) {
					Pair<Integer, Integer> indexPosiAndQuarterNo = this
							.getMonthposition(regServiceDTO.getTaxDetails().getPaymentDAte());
					LocalDate taxValidityForVcr = this.calculateChassisTaxUpTo(indexPosiAndQuarterNo.getFirst(),
							indexPosiAndQuarterNo.getSecond(), regServiceDTO.getTaxDetails().getPaymentDAte());
					if (taxValidityForVcr.isBefore(currentTaxTill)) {
						regServiceDTO.getRegistrationDetails().setTaxPaidByVcr(Boolean.FALSE);
					}
				}
			}

			if (!regServiceDTO.getRegistrationDetails().isTaxPaidByVcr()) {
				TaxHelper currenTax = getOtherStateTax(OptionalTax, stagingRegDetails, currentTaxTill, regServiceDTO,
						isApplicationFromMvi, stagingRegistrationDetails, isChassesApplication, classOfvehicle,
						isOtherState, listOfVcrsDetails, vcr, isregestered);
				Pair<String, String> prAndTr = getTrAndPrNo(stagingRegistrationDetails, regServiceDTO,
						stagingRegDetails, null, null, isApplicationFromMvi, isChassesApplication, isOtherState);
				return this.finalOverRideTaxForAllCasses(prAndTr.getFirst(), prAndTr.getSecond(), currenTax);
			}

		} else if (isApplicationFromMvi) {
			TaxHelper currenTax = this.getOldTaxDetailsForAlterVehicle(
					regServiceDTO.getRegistrationDetails().getApplicationNo(), OptionalTax, valu, regServiceDTO,
					taxType, regServiceDTO.getRegistrationDetails().getPrNo(), permitTypeCode, routeCode);
			return this.finalOverRideTaxForAllCasses(regServiceDTO.getPrNo(), null, currenTax);
			/*
			 * if (valu == 0) { quaterTax = OptionalTax; } else if (valu == 1) { quaterTax =
			 * (OptionalTax / 3) * 2; } else { quaterTax = (OptionalTax / 3) * 1; } return
			 * Pair.of(quaterTax, 0d);
			 */

		} else if (vcr && !isregestered) {
			if ((listOfVcrsDetails.getSecond().isNoApplication())
					|| (listOfVcrsDetails.getSecond().getIsOtherState()
							&& listOfVcrsDetails.getSecond().isUnRegestered()
							&& listOfVcrsDetails.getSecond().isNoApplication())
					|| (!listOfVcrsDetails.getSecond().getIsOtherState()
							&& listOfVcrsDetails.getSecond().isUnRegestered()
							&& listOfVcrsDetails.getSecond().isNoApplication())) {
				// no application
				return getVcrTaxForUnRegistered(OptionalTax, valu, listOfVcrsDetails, vcr, isregestered, permitTypeCode,
						routeCode, vcrDetails,classOfvehicle);
			}

		} else {
			TaxHelper lastTaxTillDate = getLastPaidTax(stagingRegDetails, regServiceDTO, isApplicationFromMvi,
					currentTaxTill, stagingRegistrationDetails, isChassesApplication, taxTypes(), isOtherState, vcr);
			if (lastTaxTillDate == null || lastTaxTillDate.getTax() == null
					|| lastTaxTillDate.getValidityTo() == null) {
				throw new BadRequestException("TaxDetails not found");
			}
			if(vcr) {
				vcrDetails.setLastTaxPaidUpTo(lastTaxTillDate.getValidityTo());
			}
			if (vcrDetails.isOldCovLife()) {
				lastTaxTillDate.setValidityTo(vcrDetails.getPlayedAsQuarterEnd());
				lastTaxTillDate.setAnypendingQuaters(Boolean.TRUE);
			}
			Pair<String, String> permitCodeAndRouytCode = this.getPermitCode(stagingRegDetails);
			String oldPermitCode = permitCodeAndRouytCode.getFirst();
			if (stagingRegDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.SCRT.getCovCode())) {
				Double scrtTax = this.getScrtVehicleTax(stagingRegDetails.getPrNo());
				if(scrtTax!=0 && scrtTax>0) {
					OptionalTax = scrtTax;
					oldPermitCode="SCRT";
				}
			}
			newPermitTax = getTaxForNewAndVariationPermitTax(stagingRegDetails, serviceEnum, permitTypeCode,
					newPermitTax, routeCode);
			if (newPermitTax > 0) {
				if (lastTaxTillDate.getTaxPaidThroughVcr() != null && lastTaxTillDate.getTaxPaidThroughVcr()) {
					taxPaidThroughVcr = Boolean.TRUE;
				}
			}
			double vehicleAge = 0d;
			if (stagingRegDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.EIBT.getCovCode())) {
				vehicleAge = calculateAgeOfTheVehicle(stagingRegDetails.getRegistrationValidity().getPrGeneratedDate(),
						LocalDate.now());
			}
			
			Optional<PropertiesDTO> propertiesOptional = propertiesDAO
					.findByCovsInAndObtTaxTrue(stagingRegDetails.getClassOfVehicle());
			if (propertiesOptional.isPresent()) {
				
				if (oldPermitCode.equalsIgnoreCase("INA")) {
					boolean flag = Boolean.TRUE;
					if (stagingRegDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.COCT.getCovCode())) {
						List<RegServiceDTO> listOfRegService = regServiceDAO.findByprNoAndServiceIdsAndSourceIsNull(
								stagingRegDetails.getPrNo(), ServiceEnum.ALTERATIONOFVEHICLE.getId());
						if (listOfRegService != null && !listOfRegService.isEmpty()) {
							listOfRegService.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
							RegServiceDTO dto = listOfRegService.stream().findFirst().get();
							if (dto.getTaxvalidity() != null) {
								if (dto.getAlterationDetails() != null
										&& (StringUtils.isNoneBlank(dto.getAlterationDetails().getCov())
										&& dto.getAlterationDetails().getCov()
												.equalsIgnoreCase(ClassOfVehicleEnum.COCT.getCovCode()))) {
									if (currentTaxTill.equals(dto.getTaxvalidity())
											|| currentTaxTill.isBefore(dto.getTaxvalidity())) {
										flag = Boolean.FALSE;
									}
								}
							}
						}
					}
					if (flag) {
						OptionalTax = getOldCovTax(ClassOfVehicleEnum.OBT.getCovCode(),
								stagingRegDetails.getVahanDetails().getSeatingCapacity(),
								stagingRegDetails.getVahanDetails().getUnladenWeight(),
								stagingRegDetails.getVahanDetails().getGvw(), stateCode, "INA", null);
					}
				}
			}
			if (taxPaidThroughVcr) {
				OptionalTax = lastTaxTillDate.getTax();
				finalquaterTax = lastTaxTillDate.getTax();
			}
			/*
			 * if(vcr) { if(listOfVcrsDetails.getSecond().getVcrTax() != null
			 * &&listOfVcrsDetails.getSecond().getVcrTax()>0) {
			 * vcrTax=listOfVcrsDetails.getSecond().getVcrTax(); } }
			 */
			if (isWeightAlt != null && isWeightAlt) {
				if (lastTaxTillDate.isAnypendingQuaters()) {
					throw new BadRequestException("Tax is pending. Please pay the tax. " + stagingRegDetails.getPrNo());
				}

				if (serviceEnum == null || serviceEnum.isEmpty()) {
					throw new BadRequestException("services type not found. " + stagingRegDetails.getPrNo());
				}
				/*
				 * if(!serviceEnum.stream().anyMatch(id->id.equals(ServiceEnum.
				 * ALTERATIONOFVEHICLE))) { throw new
				 * BadRequestException("For body type alteration please select service as alteratin service. "
				 * +stagingRegDetails.getPrNo()); }
				 */
				Integer gvw = stagingRegDetails.getVahanDetails().getGvw();
				if (stagingRegDetails.isWeightAltDone()) {
					gvw = stagingRegDetails.getVahanDetails().getOldGvw();
				}
				if (stagingRegDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.ARVT.getCovCode())) {
					if (stagingRegDetails.getVahanDetails().getTrailerChassisDetailsDTO() != null
							&& !stagingRegDetails.getVahanDetails().getTrailerChassisDetailsDTO().isEmpty()) {

						Integer gtw = stagingRegDetails.getVahanDetails().getTrailerChassisDetailsDTO().stream()
								.findFirst().get().getGtw();
						for (TrailerChassisDetailsDTO trailerDetails : stagingRegDetails.getVahanDetails()
								.getTrailerChassisDetailsDTO()) {
							if (trailerDetails.getGtw() > gtw) {
								gtw = trailerDetails.getGtw();
							}
						}
						gvw = gvw + gtw;
					}
				}
				Optional<MasterWeightsForAlt> optionalWeigts = masterWeightsForAltDAO
						.findByToGvwGreaterThanEqualAndFromGvwLessThanEqualAndStatusIsTrue(gvw, gvw);
				if (!optionalWeigts.isPresent()) {
					throw new BadRequestException(
							"Vehicle not eligible to change weight: " + stagingRegDetails.getPrNo());
				}
				String classOfVehicle = stagingRegDetails.getClassOfVehicle();
				if (stagingRegDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.EIBT.getCovCode())
						&& vehicleAge > 15) {
					classOfVehicle = ClassOfVehicleEnum.OBT.getCovCode();
					Double oldtax = getOldCovTax(classOfVehicle,
							stagingRegDetails.getVahanDetails().getSeatingCapacity(),
							stagingRegDetails.getVahanDetails().getUnladenWeight(),
							stagingRegDetails.getVahanDetails().getGvw(), stateCode, permitcode, routeCode);
					OptionalTax = oldtax;
					if (taxPaidThroughVcr) {
						OptionalTax = lastTaxTillDate.getTax();
						finalquaterTax = lastTaxTillDate.getTax();
					}

				}
				Double obtTax = getOldCovTax(classOfVehicle, stagingRegDetails.getVahanDetails().getSeatingCapacity(),
						stagingRegDetails.getVahanDetails().getUnladenWeight(), optionalWeigts.get().getGvw(),
						stateCode, permitcode, routeCode);
				newPermitTax = obtTax;
				// OptionalTax = obtTax;
				Pair<Integer, Integer> monthpositionInQuater = getMonthposition(LocalDate.now());
				double taxPerMonthWithNewWeight = obtTax / 3d;
				double taxPerMonthWithOldWeight = OptionalTax / 3d;
				Long newCovtotalMonths = 0l;
				if (!lastTaxTillDate.getValidityTo().equals(currentTaxTill)) {
					newCovtotalMonths = ChronoUnit.MONTHS.between(currentTaxTill, lastTaxTillDate.getValidityTo());
				}
				if (monthpositionInQuater.getFirst() == 0) {
					quaterTax = obtTax - OptionalTax;
					if (newCovtotalMonths != 0) {
						quaterTax = quaterTax + ((taxPerMonthWithNewWeight * newCovtotalMonths)
								- (taxPerMonthWithOldWeight * newCovtotalMonths));
					}

				} else if (monthpositionInQuater.getFirst() == 1) {
					quaterTax = (taxPerMonthWithNewWeight * 2) - (taxPerMonthWithOldWeight * 2);
					if (newCovtotalMonths != 0) {
						quaterTax = quaterTax + ((taxPerMonthWithNewWeight * newCovtotalMonths)
								- (taxPerMonthWithOldWeight * newCovtotalMonths));
					}

				} else {
					quaterTax = (taxPerMonthWithNewWeight) - (taxPerMonthWithOldWeight);
					if (newCovtotalMonths != 0) {
						quaterTax = quaterTax + ((taxPerMonthWithNewWeight * newCovtotalMonths)
								- (taxPerMonthWithOldWeight * newCovtotalMonths));
					}
				}

			} else if (stagingRegDetails.isVehicleStoppageRevoked()) {
				if (stagingRegDetails.getVehicleStoppageRevokedDate() == null) {
					throw new BadRequestException(
							"Vheicle stoppage revokation date not found: " + stagingRegDetails.getPrNo());
				}

				TaxHelper revokedTax = vehicleRevokationTaxCalculation(stagingRegDetails, lastTaxTillDate, OptionalTax,
						vcr, vcrDetails);
				quaterTax = revokedTax.getTax();
				taxArrears = revokedTax.getTaxArrears();
				penality = revokedTax.getPenalty().doubleValue();
				penaltyArrears = revokedTax.getPenaltyArrears().doubleValue();
				newPermitTax = revokedTax.getQuaterAmount();
			} else if (((stagingRegDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())
					&& Integer.parseInt(stagingRegDetails.getVahanDetails().getSeatingCapacity()) <= 4)
					|| ((stagingRegDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode())
							|| stagingRegDetails.getClassOfVehicle()
									.equalsIgnoreCase(ClassOfVehicleEnum.GCRT.getCovCode()))
							&& stagingRegDetails.getVahanDetails().getGvw() <= 3000)
					|| (stagingRegDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.MTLT.getCovCode()))
					|| (stagingRegDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.LTCT.getCovCode()))
					|| (stagingRegDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.STCT.getCovCode())))
					&& taxType.equalsIgnoreCase(TaxTypeEnum.LifeTax.getDesc())) {

				totalMonths = ChronoUnit.MONTHS.between(lastTaxTillDate.getValidityTo(), currentTaxTill);
				totalMonths = Math.abs(totalMonths);
				totalMonths = totalMonths - 3;
				int penalityPercent = 200;
				boolean vcrFlagForDuctionMode = Boolean.TRUE;
				if (vcr) {
					
					if (this.checkTaxdeductionModeOrNotForOldQuarters(vcrDetails)) {
						penalityPercent = 50;
						vcrFlagForDuctionMode = Boolean.FALSE;
					}
				}
				if (vcrDetails.getVcrTax() != null && vcrDetails.getVcrTax() > 0) {
					if (lastTaxTillDate.getValidityTo().isAfter(vcrDetails.getPlayedAsQuarterEnd())) {
						// OptionalTax= vcrDetails.getVcrTax();
						Long oldCvoTaxPenality = ChronoUnit.MONTHS.between(vcrDetails.getPlayedAsQuarterEnd(),
								currentTaxTill);
						oldCvoTaxPenality = Math.abs(oldCvoTaxPenality);
						oldCvoTaxPenality = oldCvoTaxPenality + 1;
						penaltyArrears = (((vcrDetails.getVcrTax() / 3) * oldCvoTaxPenality) * penalityPercent) / 100;
						taxArrears = (vcrDetails.getVcrTax() / 3) * oldCvoTaxPenality;
						totalMonths = ChronoUnit.MONTHS.between(lastTaxTillDate.getValidityTo(),
								vcrDetails.getPlayedAsQuarterEnd());
					} else {
						// totalMonths = ChronoUnit.MONTHS.between(firtQuaterValidity,
						// currentQuaterUpTo);
						OptionalTax = vcrDetails.getVcrTax();
					}
				}

				double totalQuaters = totalMonths / 3;
				double penalityArrearsQuaters = totalQuaters;
				
				if (vcr) {
					penaltyArrears = penaltyArrears + ((((OptionalTax * penalityPercent) / 100) * penalityArrearsQuaters));
					taxArrears = taxArrears + ((OptionalTax * penalityArrearsQuaters));
				} else {
					penaltyArrears = (((OptionalTax * 50) / 100) * penalityArrearsQuaters);
					taxArrears = (OptionalTax * penalityArrearsQuaters);
				}

				double taxPerMonth = OptionalTax / 3d;
				// TODO need to check vehicle sized
				Long unSizedMonths = 0l;
				Pair<Long, Long> unSizedMonthsAndVcrTaxMonths = getUnsizedMonths(stagingRegDetails, vcrDetails,
						unSizedMonths);
				unSizedMonths = unSizedMonthsAndVcrTaxMonths.getFirst();
				if (unSizedMonths > 0) {
					penaltyArrears = ((taxPerMonth * unSizedMonths) * penalityPercent) / 100;
					taxArrears = taxPerMonth * unSizedMonths;
				}
				if (unSizedMonthsAndVcrTaxMonths.getSecond() > 0) {
					taxPerMonth = vcrDetails.getVcrTax() / 3d;
					penaltyArrears = penaltyArrears
							+ (((taxPerMonth * unSizedMonthsAndVcrTaxMonths.getSecond()) * 200) / 100);
					taxArrears = taxArrears + (taxPerMonth * unSizedMonthsAndVcrTaxMonths.getSecond());
				}
				Pair<Integer, Integer> monthpositionInQuater = getMonthposition(LocalDate.now());
				if (monthpositionInQuater.getFirst() == 0) {
					quaterTax = 0d;
				} else if (monthpositionInQuater.getFirst() == 1) {
					penality = (taxPerMonth * 25) / 100;
					if (vcr && vcrFlagForDuctionMode) {
						penality = (taxPerMonth * 100) / 100;
					}
					if (vcrDetails.isVehicleSized()) {
						penality = 0d;
					}
					quaterTax = taxPerMonth;

				} else {
					penality = ((taxPerMonth * 2) * 50) / 100;
					if (vcr) {
						penality = ((taxPerMonth * 2) * penalityPercent) / 100;
					}
					quaterTax = taxPerMonth * 2;
					if (vcrDetails.isVehicleSized()) {
						penality = 0d;
						quaterTax = taxPerMonth;
					}
				}

			} else {
				if (lastTaxTillDate.isAnypendingQuaters()) {
					if (stagingRegDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.EIBT.getCovCode())
							&& vehicleAge > 15) {
						Double obtTax = getOldCovTax(ClassOfVehicleEnum.OBT.getCovCode(),
								stagingRegDetails.getVahanDetails().getSeatingCapacity(),
								stagingRegDetails.getVahanDetails().getUnladenWeight(),
								stagingRegDetails.getVahanDetails().getGvw(), stateCode, permitcode, routeCode);
						Pair<Double, Double> quaterTaxAndPenality = getpendingQuaters(currentTaxTill,
								lastTaxTillDate.getValidityTo(), OptionalTax, finalquaterTax, quaterTax, vcrDetails, 0d,
								0d, true, stagingRegDetails.getRegistrationValidity().getPrGeneratedDate(), obtTax);
						taxArrears = quaterTaxAndPenality.getFirst();
						penaltyArrears = quaterTaxAndPenality.getSecond();
						OptionalTax = obtTax;
						if (taxPaidThroughVcr) {
							OptionalTax = lastTaxTillDate.getTax();
						}
						newPermitTax = obtTax;
						if (vcrDetails != null && vcrDetails.getVcrTax() != null &&vcrDetails.getVcrTax()>0) {
							OptionalTax = vcrDetails.getVcrTax();
							newPermitTax = vcrDetails.getVcrTax();
						}
					} else {
						Pair<Double, Double> quaterTaxAndPenality = getpendingQuaters(currentTaxTill,
								lastTaxTillDate.getValidityTo(), OptionalTax, finalquaterTax, quaterTax, vcrDetails, 0d,
								0d, false, stagingRegDetails.getRegistrationValidity().getPrGeneratedDate(),
								OptionalTax);
						taxArrears = taxArrears + quaterTaxAndPenality.getFirst();
						penaltyArrears = penaltyArrears + quaterTaxAndPenality.getSecond();
					}
					Pair<Double, Double> pairOfTax = getcurrenttaxWithPenality(vcrDetails, newPermitTax, quaterTax,
							penality, OptionalTax, valu);
					quaterTax = pairOfTax.getFirst();
					penality = pairOfTax.getSecond();
					if(vcrDetails!= null && vcrDetails.isOldCovLife()) {
						penality=0d;
						taxArrears = 0d;
						penaltyArrears = 0d;
					}
				} else {
					if(vcrDetails!= null && vcrDetails.getPlayedAsQuarterEnd()!=null) {
						Pair<Integer, Integer> index = getMonthposition(vcrDetails.getPlayedAsQuarterEnd().plusDays(1));
						valu = index.getFirst();
					}
					Pair<Double, Double> pairOfTax = getcurrenttaxWithOutPenality(vcrDetails, newPermitTax,
							finalquaterTax, penality, OptionalTax, valu);
					quaterTax = pairOfTax.getFirst();
					penality = pairOfTax.getSecond();
				}
			}
			if ((quaterTax != null && quaterTax < 1) || quaterTax == null) {
				quaterTax = 0d;
			}
			TaxHelper currenTax = returnTaxDetails(quaterTax, taxArrears, penality, penaltyArrears, newPermitTax,lastTaxTillDate.getValidityTo());
			// if (lastTaxTillDate.isAnypendingQuaters()) {
			currenTax = this.overRideTheTax(OptionalTax, valu, stagingRegDetails, currentTaxTill, regServiceDTO,
					isApplicationFromMvi, stagingRegistrationDetails, isChassesApplication, classOfvehicle,
					isOtherState, taxType, serviceEnum, permitTypeCode, routeCode, lastTaxTillDate, vcrDetails,
					currenTax, isWeightAlt);
			// }
			return currenTax;
		}

		if ((quaterTax != null && quaterTax < 1) || quaterTax == null) {
			quaterTax = 0d;
		}

		return returnTaxDetails(quaterTax, taxArrears, penality, penaltyArrears, newPermitTax,null);

	}

	@Override
	public TaxHelper vehicleRevokationTaxCalculation(RegistrationDetailsDTO stagingRegDetails,
			TaxHelper lastTaxTillDate, Double quaterTax, boolean vcr, TaxHelper vcrDetails) {
		Double penality = 0d;
		Double taxArrears = 0d;
		Double penaltyArrears = 0d;
	
		
		if(this.checkTaxdeductionModeOrNotForOldQuarters(vcrDetails)) {
			vcr=Boolean.FALSE;
		}
		Pair<Integer, Integer> revokedMonthpositionInQuater = getMonthposition(
				stagingRegDetails.getVehicleStoppageRevokedDate());
		LocalDate firstQuaterUpTo = calculateTaxUpTo(revokedMonthpositionInQuater.getFirst(),
				revokedMonthpositionInQuater.getSecond());
		Pair<Integer, Integer> currentMonthpositionInQuater = getMonthposition(LocalDate.now());
		LocalDate currentQuaterUpTo = calculateTaxUpTo(currentMonthpositionInQuater.getFirst(),
				currentMonthpositionInQuater.getSecond());
		double taxPerMonth = quaterTax / 3d;

		/*
		 * vcr write in tax valid date
		 * if(lastTaxTillDate.getValidityTo().plusMonths(1).isAfter(vcrDetails.
		 * getLatestVcrDate())) { vcr = Boolean.FALSE; }
		 */
		Long unSizedMonths = 0l;
		Pair<Long, Long> unSizedMonthsAndVcrTaxMonths = getUnsizedMonths(stagingRegDetails, vcrDetails, unSizedMonths);

		if ((revokedMonthpositionInQuater.getSecond().equals(currentMonthpositionInQuater.getSecond())
				&& stagingRegDetails.getVehicleStoppageRevokedDate().getYear() == LocalDate.now().getYear())) {
			if (revokedMonthpositionInQuater.getFirst() == 0) {
				if (currentMonthpositionInQuater.getFirst() == 1) {
					penality = (quaterTax * 25) / 100;
					if (vcr) {
						penality = (quaterTax * 100) / 100;
					}
				} else if (currentMonthpositionInQuater.getFirst() == 2) {
					penality = (quaterTax * 50) / 100;
					if (vcr) {
						penality = (quaterTax * 200) / 100;
					}
				}
			} else if (revokedMonthpositionInQuater.getFirst() == 1) {
				quaterTax = taxPerMonth * 2;
				if (currentMonthpositionInQuater.getFirst() == 2) {
					penality = (quaterTax * 25) / 100;
					if (vcr) {
						penality = (quaterTax * 100) / 100;
					}
				}
			} else {
				quaterTax = taxPerMonth;
			}
		} else {
			// not in same quater
			if (revokedMonthpositionInQuater.getFirst() == 0) {
				penaltyArrears = (quaterTax * 50) / 100;
				if (vcr) {
					penaltyArrears = (quaterTax * 200) / 100;
				}
				taxArrears = quaterTax;
				if (vcrDetails.isVehicleSized() && unSizedMonths > 0) {
					if (vcr) {
						Pair<Long, Map<Double, Double>> montsAndPenalityArr = getUnSizedMontsAndAmount(unSizedMonths,
								quaterTax, taxPerMonth, 200, 3);
						unSizedMonths = montsAndPenalityArr.getFirst();
						penaltyArrears = montsAndPenalityArr.getSecond().entrySet().iterator().next().getKey();
						taxArrears = montsAndPenalityArr.getSecond().entrySet().iterator().next().getValue();
					} else {
						Pair<Long, Map<Double, Double>> montsAndPenalityArr = getUnSizedMontsAndAmount(unSizedMonths,
								quaterTax, taxPerMonth, 50, 3);
						penaltyArrears = montsAndPenalityArr.getSecond().entrySet().iterator().next().getKey();
						taxArrears = montsAndPenalityArr.getSecond().entrySet().iterator().next().getValue();

					}
				}

			} else if (revokedMonthpositionInQuater.getFirst() == 1) {
				penaltyArrears = ((taxPerMonth * 2) * 50) / 100;
				if (vcr) {
					penaltyArrears = ((taxPerMonth * 2) * 200) / 100;
				}
				taxArrears = taxPerMonth * 2;
				if (vcrDetails.isVehicleSized() && unSizedMonths > 0) {
					if (vcr) {
						Pair<Long, Map<Double, Double>> montsAndPenalityArr = getUnSizedMontsAndAmount(unSizedMonths,
								quaterTax, taxPerMonth, 200, 2);
						unSizedMonths = montsAndPenalityArr.getFirst();
						penaltyArrears = montsAndPenalityArr.getSecond().entrySet().iterator().next().getKey();
						taxArrears = montsAndPenalityArr.getSecond().entrySet().iterator().next().getValue();
					} else {
						Pair<Long, Map<Double, Double>> montsAndPenalityArr = getUnSizedMontsAndAmount(unSizedMonths,
								quaterTax, taxPerMonth, 50, 2);
						penaltyArrears = montsAndPenalityArr.getSecond().entrySet().iterator().next().getKey();
						taxArrears = montsAndPenalityArr.getSecond().entrySet().iterator().next().getValue();

					}
				}
			} else {
				penaltyArrears = (taxPerMonth * 50) / 100;
				if (vcr) {
					penaltyArrears = (taxPerMonth * 200) / 100;
				}
				taxArrears = taxPerMonth;
				if (vcrDetails.isVehicleSized() && unSizedMonths > 0) {
					if (vcr) {
						Pair<Long, Map<Double, Double>> montsAndPenalityArr = getUnSizedMontsAndAmount(unSizedMonths,
								quaterTax, taxPerMonth, 200, 1);
						unSizedMonths = montsAndPenalityArr.getFirst();
						penaltyArrears = montsAndPenalityArr.getSecond().entrySet().iterator().next().getKey();
						taxArrears = montsAndPenalityArr.getSecond().entrySet().iterator().next().getValue();
					} else {
						Pair<Long, Map<Double, Double>> montsAndPenalityArr = getUnSizedMontsAndAmount(unSizedMonths,
								quaterTax, taxPerMonth, 50, 1);
						penaltyArrears = montsAndPenalityArr.getSecond().entrySet().iterator().next().getKey();
						taxArrears = montsAndPenalityArr.getSecond().entrySet().iterator().next().getValue();

					}
				}
			}
			if (vcrDetails.isVehicleSized()) {
				double quaters = unSizedMonths / 3d;
				double quatersRound = Math.ceil(unSizedMonths / 3);
				if (quaters == quatersRound) {
					taxArrears = taxArrears + ((((quaterTax * 200) / 100) * quaters));
					penaltyArrears = penaltyArrears + (quaterTax * quaters);
				} else {
					String numberD = String.valueOf(quaters);
					String number = numberD.substring(numberD.indexOf("."));
					String num = number.substring(1, 4);
					String val = numberD.substring(0, numberD.indexOf("."));
					// Double taxPerMonth = quaterTax / 3d;
					if (num.equalsIgnoreCase("666")) {
						taxArrears = taxArrears + ((((quaterTax * 200) / 100) * Double.valueOf(val)));
						penaltyArrears = penaltyArrears + ((quaterTax * Double.valueOf(val)));
						taxArrears = taxArrears + ((taxPerMonth * 2) * 200 / 100);
						penaltyArrears = penaltyArrears + (taxPerMonth * 2);
					} else {
						taxArrears = taxArrears + ((((quaterTax * 200) / 100) * Double.valueOf(val)));
						penaltyArrears = penaltyArrears + ((quaterTax * Double.valueOf(val)));
						taxArrears = taxArrears + ((taxPerMonth * 1) * 200 / 100);
						penaltyArrears = penaltyArrears + (taxPerMonth * 1);
					}
				}
			} else {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
				String date1 = firstQuaterUpTo.getDayOfMonth() + "-" + firstQuaterUpTo.getMonthValue() + "-"
						+ stagingRegDetails.getVehicleStoppageRevokedDate().getYear();
				LocalDate firtQuaterValidity = LocalDate.parse(date1, formatter);
				Long totalMonthsForPenality = ChronoUnit.MONTHS.between(firtQuaterValidity, currentQuaterUpTo);
				if (vcrDetails.getVcrTax() != null && vcrDetails.getVcrTax() > 0) {
					if (firtQuaterValidity.isAfter(vcrDetails.getPlayedAsQuarterEnd())) {
						Long oldCvoTaxPenality = ChronoUnit.MONTHS.between(firtQuaterValidity,
								vcrDetails.getPlayedAsQuarterEnd());
						oldCvoTaxPenality = Math.abs(oldCvoTaxPenality);
						oldCvoTaxPenality = oldCvoTaxPenality + 1;
						double Penalityquaters = oldCvoTaxPenality / 3d;
						double PenalityQuatersRound = Math.ceil(oldCvoTaxPenality / 3d);
						if (Penalityquaters == PenalityQuatersRound) {
							Pair<Double, Double> taxArr2AndPenality = chassisPenalitTax(quaterTax,
									(Penalityquaters - 1), vcr);
							taxArrears = taxArrears + taxArr2AndPenality.getFirst();
							penaltyArrears = penaltyArrears + taxArr2AndPenality.getSecond();
						}
						totalMonthsForPenality = ChronoUnit.MONTHS.between(vcrDetails.getPlayedAsQuarterEnd(),
								currentQuaterUpTo);
						quaterTax = vcrDetails.getVcrTax();
					} else {
						totalMonthsForPenality = ChronoUnit.MONTHS.between(firtQuaterValidity, currentQuaterUpTo);
						quaterTax = vcrDetails.getVcrTax();
					}
				}
				totalMonthsForPenality = Math.abs(totalMonthsForPenality);
				totalMonthsForPenality = totalMonthsForPenality + 1;
				double Penalityquaters = totalMonthsForPenality / 3d;
				double PenalityQuatersRound = Math.ceil(totalMonthsForPenality / 3d);
				if (Penalityquaters == PenalityQuatersRound) {
					Pair<Double, Double> taxArr2AndPenality = chassisPenalitTax(quaterTax, (Penalityquaters - 1), vcr);
					taxArrears = taxArrears + taxArr2AndPenality.getFirst();
					penaltyArrears = penaltyArrears + taxArr2AndPenality.getSecond();
				}
			}
			if (unSizedMonthsAndVcrTaxMonths.getSecond() > 0) {
				taxPerMonth = vcrDetails.getVcrTax() / 3d;
				quaterTax = vcrDetails.getVcrTax();
				penaltyArrears = penaltyArrears
						+ (((taxPerMonth * unSizedMonthsAndVcrTaxMonths.getSecond()) * 200) / 100);
				taxArrears = taxArrears + (taxPerMonth * unSizedMonthsAndVcrTaxMonths.getSecond());
			}
			if (currentMonthpositionInQuater.getFirst() == 1) {
				penality = ((quaterTax * 25) / 100);
				if (vcr) {
					penality = ((quaterTax * 100) / 100);
				}
				if (vcrDetails.isVehicleSized()) {
					quaterTax = taxPerMonth * 2;
					penality = (((taxPerMonth * 2) * 100) / 100);
				}
			} else if (currentMonthpositionInQuater.getFirst() == 2) {

				penality = ((quaterTax * 50) / 100);
				if (vcr) {
					penality = ((quaterTax * 200) / 100);
				}
				if (vcrDetails.isVehicleSized()) {
					quaterTax = taxPerMonth;
					penality = ((taxPerMonth * 200) / 100);
				}
			}
		}
		quaterTax = quaterTax - (taxPerMonth * stagingRegDetails.getTaxExemMonths());
		return returnTaxDetails(quaterTax, taxArrears, penality, penaltyArrears, 0d,lastTaxTillDate.getValidityTo());

	}

	private Pair<Long, Long> getUnsizedMonths(RegistrationDetailsDTO stagingRegDetails, TaxHelper vcrDetails,
			Long unSizedMonths) {
		Long vcrNewCovMonths = 0l;
		if (vcrDetails.isVehicleSized()) {
			if (stagingRegDetails.getVehicleStoppageRevokedDate()!= null && stagingRegDetails.getVehicleStoppageRevokedDate().isBefore(vcrDetails.getSizedDate())) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
				String date1 = "01-" + String.valueOf(vcrDetails.getSizedDate().getMonthValue()) + "-"
						+ String.valueOf(vcrDetails.getSizedDate().getYear());
				LocalDate sizedmonthStartsDate = LocalDate.parse(date1, formatter);
				String date2 = "01-" + String.valueOf(stagingRegDetails.getVehicleStoppageRevokedDate().getMonthValue())
						+ "-" + String.valueOf(stagingRegDetails.getVehicleStoppageRevokedDate().getYear());
				LocalDate revokdmonthStartsDate = LocalDate.parse(date2, formatter);
				unSizedMonths = ChronoUnit.MONTHS.between(revokdmonthStartsDate, sizedmonthStartsDate);
				unSizedMonths = unSizedMonths + 1;
				if (vcrDetails.getSizedDate().isAfter(vcrDetails.getPlayedAsQuarterEnd())) {
					Long newCvoTaxPenality = ChronoUnit.MONTHS.between(vcrDetails.getPlayedAsQuarterEnd(),
							vcrDetails.getSizedDate());
					newCvoTaxPenality = Math.abs(newCvoTaxPenality);
					newCvoTaxPenality += 1;
					vcrNewCovMonths = newCvoTaxPenality;
					if (newCvoTaxPenality > 0) {
						if (newCvoTaxPenality > unSizedMonths) {
							newCvoTaxPenality = newCvoTaxPenality - unSizedMonths;// set obt as 0
							unSizedMonths = 0l;
						} else {
							unSizedMonths = unSizedMonths - newCvoTaxPenality;
						}
					}
				}
			}
		}

		return Pair.of(unSizedMonths, vcrNewCovMonths);
	}

	private Pair<Double, Double> getcurrenttaxWithPenality(TaxHelper vcrDetails, Double newPermitTax, Double quaterTax,
			Double penality, Double OptionalTax, Integer valu) {
		if (!vcrDetails.isVcr()) {
			// With out VCR
			if (valu == 0) {
				Pair<Double, Double> pairOfTax = getTaxWithOutVcr(quaterTax, penality, OptionalTax, newPermitTax, 3, 0);
				quaterTax = pairOfTax.getFirst();
				penality = pairOfTax.getSecond();
			} else if (valu == 1) {
				Pair<Double, Double> pairOfTax = getTaxWithOutVcr(quaterTax, penality, OptionalTax, newPermitTax, 2,
						25);
				quaterTax = pairOfTax.getFirst();
				penality = pairOfTax.getSecond();
			} else {
				Pair<Double, Double> pairOfTax = getTaxWithOutVcr(quaterTax, penality, OptionalTax, newPermitTax, 1,
						50);
				quaterTax = pairOfTax.getFirst();
				penality = pairOfTax.getSecond();
			}
		} else {
			// With VCR
			if (vcrDetails.getVcrTax() != null && vcrDetails.getVcrTax() > 0 && OptionalTax<=vcrDetails.getVcrTax()) {
				OptionalTax = vcrDetails.getVcrTax();
			}
			if (vcrDetails.isVehicleSized()) {
				// getcurrentSizedTax
				Pair<Integer, Integer> sizedQuarter = this.getMonthposition(vcrDetails.getSizedDate());
				Pair<Integer, Integer> currentQuarter = this.getMonthposition(LocalDate.now());
				if (vcrDetails.getSizedDate().getYear() == LocalDate.now().getYear()
						&& sizedQuarter.getSecond() == currentQuarter.getSecond()) {
					Pair<Double, Double> pairOfTax = this.getcurrentSizedTax(quaterTax, penality, OptionalTax,
							newPermitTax, sizedQuarter.getFirst(),currentQuarter.getFirst(),vcrDetails);
					quaterTax = pairOfTax.getFirst();
					penality = pairOfTax.getSecond();
				} else {
					if (valu == 0) {
						Pair<Double, Double> pairOfTax = getTaxWithVcr(quaterTax, penality, OptionalTax, newPermitTax,
								3, 0, vcrDetails.isVehicleSized());
						quaterTax = pairOfTax.getFirst();
						penality = pairOfTax.getSecond();
					} else if (valu == 1) {
						Pair<Double, Double> pairOfTax = getTaxWithVcr(quaterTax, penality, OptionalTax, newPermitTax,
								2, 0, vcrDetails.isVehicleSized());
						quaterTax = pairOfTax.getFirst();
						penality = pairOfTax.getSecond();
					} else {
						Pair<Double, Double> pairOfTax = getTaxWithVcr(quaterTax, penality, OptionalTax, newPermitTax,
								1, 0, vcrDetails.isVehicleSized());
						quaterTax = pairOfTax.getFirst();
						penality = pairOfTax.getSecond();
					}
				}
			} else {
				if (valu == 0) {
					Pair<Double, Double> pairOfTax = getTaxWithVcr(quaterTax, penality, OptionalTax, newPermitTax, 3, 0,
							vcrDetails.isVehicleSized());
					quaterTax = pairOfTax.getFirst();
					penality = pairOfTax.getSecond();
				} else if (valu == 1) {
					int penalityPercent = 100;
					if(this.checkTaxdeductionModeOrNot(vcrDetails) || checkTaxdeductionModeOrNotForOldQuarters(vcrDetails)) {
						penalityPercent = 25;
					}
					Pair<Double, Double> pairOfTax = getTaxWithVcr(quaterTax, penality, OptionalTax, newPermitTax, 2,
							penalityPercent, vcrDetails.isVehicleSized());
					
					quaterTax = pairOfTax.getFirst();
					penality = pairOfTax.getSecond();
				} else {
					int penalityPercent = 200;
					if(this.checkTaxdeductionModeOrNot(vcrDetails) || checkTaxdeductionModeOrNotForOldQuarters(vcrDetails)) {
						penalityPercent = 50;
					}
					Pair<Double, Double> pairOfTax = getTaxWithVcr(quaterTax, penality, OptionalTax, newPermitTax, 1,
							penalityPercent, vcrDetails.isVehicleSized());
					quaterTax = pairOfTax.getFirst();
					penality = pairOfTax.getSecond();
				}
			}
		}
		return Pair.of(quaterTax, penality);
	}

	private Pair<Double, Double> getTaxWithVcr(Double quaterTax, Double penality, Double OptionalTax,
			Double newPermitTax, int monthPosition, int percent, boolean sized) {
		quaterTax = getTaxWithNewPermit(OptionalTax, newPermitTax, monthPosition);
		// Double quaterTax;
		double oldTaxPerMonth = 0d;
		quaterTax = OptionalTax;
		oldTaxPerMonth = OptionalTax / 3;
		if (monthPosition == 2) {
			if (sized) {
				quaterTax = (oldTaxPerMonth * 2);
			}
		} else if (monthPosition == 1) {
			if (sized) {
				quaterTax = (oldTaxPerMonth * 1);
			}
		}
		penality = ((quaterTax * percent) / 100);
		return Pair.of(quaterTax, penality);
	}

	private Pair<Double, Double> getcurrentSizedTax(Double quaterTax, Double penality, Double OptionalTax,
			Double newPermitTax, int SizedmonthPosition,int currentmonthPosition,TaxHelper vcrDetails) {
		// quaterTax = getTaxWithNewPermit(OptionalTax, newPermitTax, monthPosition);
		// Double quaterTax;
		double oldTaxPerMonth = 0d;
		quaterTax = OptionalTax;
		oldTaxPerMonth = OptionalTax / 3;
		if (SizedmonthPosition == 2) {
			quaterTax = OptionalTax;
			int penalityPercent = 200;
			if(this.checkTaxdeductionModeOrNot(vcrDetails)|| checkTaxdeductionModeOrNotForOldQuarters(vcrDetails)) {
				penalityPercent = 50;
			}
			penality = ((quaterTax * penalityPercent) / 100);
		} else if (SizedmonthPosition == 1) {
			quaterTax = (oldTaxPerMonth * 3);
			int penalityPercent = 100;
			boolean voluntrayMode = this.checkTaxdeductionModeOrNot(vcrDetails);
			if(voluntrayMode || checkTaxdeductionModeOrNotForOldQuarters(vcrDetails)) {
				penalityPercent = 25;
			}
			penality = ((quaterTax * penalityPercent) / 100);
			 if(currentmonthPosition ==2) {
				 penalityPercent = 200;
				 if(voluntrayMode || checkTaxdeductionModeOrNotForOldQuarters(vcrDetails)) {
					 penalityPercent = 50;
				 }
					penality = ((quaterTax * penalityPercent) / 100);
				}

		}else {
			if(currentmonthPosition ==1) {
				quaterTax = (oldTaxPerMonth * 3);
				int penalityPercent = 100;
				if(this.checkTaxdeductionModeOrNot(vcrDetails)|| checkTaxdeductionModeOrNotForOldQuarters(vcrDetails)) {
					penalityPercent = 25;
				}
				penality = ((quaterTax * penalityPercent) / 100);
			}else if(currentmonthPosition ==2) {
				int penalityPercent = 200;
				if(this.checkTaxdeductionModeOrNot(vcrDetails)|| checkTaxdeductionModeOrNotForOldQuarters(vcrDetails)) {
					penalityPercent = 50;
				}
				quaterTax = (oldTaxPerMonth * 2);
				penality = ((quaterTax * penalityPercent) / 100);
			}
		}

		return Pair.of(quaterTax, penality);
	}

	private Pair<Double, Double> getTaxWithOutVcr(Double quaterTax, Double penality, Double OptionalTax,
			Double newPermitTax, int monthPosition, int percent) {
		quaterTax = getTaxWithNewPermit(OptionalTax, newPermitTax, monthPosition);
		penality = ((quaterTax * percent) / 100);
		// quaterTax = OptionalTax;
		return Pair.of(quaterTax, penality);
	}

	private Pair<Double, Double> getTaxWithVcrWithOutPenality(Double quaterTax, Double penality, Double OptionalTax,
			Double newPermitTax, int monthPosition, int percent) {
		quaterTax = getTaxWithNewPermitWitOutPenality(OptionalTax, newPermitTax, monthPosition);
		penality = 0d;
		// quaterTax = OptionalTax;
		return Pair.of(quaterTax, penality);
	}

	private Double getTaxForNewAndVariationPermitTax(RegistrationDetailsDTO stagingRegDetails,
			List<ServiceEnum> serviceEnum, String permitTypeCode, Double newPermitTax, String routeCode) {
		Pair<String, String> permitAndRout = getPermitCode(stagingRegDetails);
		String permitType = permitTypeCode;
		String routeCodes = permitAndRout.getSecond();
		if (serviceEnum != null && !serviceEnum.isEmpty() && ((serviceEnum.stream()
				.anyMatch(service -> Arrays.asList(ServiceEnum.NEWPERMIT, ServiceEnum.VARIATIONOFPERMIT, ServiceEnum.STAGECARRIAGEREPLACEMENTOFVEHICLE).stream()
						.anyMatch(serviceName -> serviceName.equals(service))))
				&& (!serviceEnum.stream().anyMatch(service -> service.equals(ServiceEnum.ALTERATIONOFVEHICLE))))) {

			if ((stagingRegDetails.getClassOfVehicle().equals(ClassOfVehicleEnum.COCT.getCovCode())
					|| stagingRegDetails.getClassOfVehicle().equals(ClassOfVehicleEnum.TOVT.getCovCode()))
					&& StringUtils.isBlank(routeCode)) {
				throw new BadRequestException("rout code not found for pr: " + stagingRegDetails.getPrNo());
			}
			routeCodes = routeCode;
			boolean isBelongToPermitService = false;
			isBelongToPermitService = getAmethodToGetPemritService(serviceEnum);
			if (isBelongToPermitService) {
				Optional<PropertiesDTO> propertiesOptional = propertiesDAO
						.findByCovsInAndPermitCodeTrue(stagingRegDetails.getClassOfVehicle());
				if (!propertiesOptional.isPresent()) {
					permitTypeCode = permitcode;
					permitType = permitcode;
				}
			}
			if (serviceEnum != null && !serviceEnum.isEmpty() && (serviceEnum.stream()
					.anyMatch(service -> service.equals(ServiceEnum.STAGECARRIAGEREPLACEMENTOFVEHICLE)))) {
				permitType="INA";
			}
			Integer gvw = getGvwWeightForCitizen(stagingRegDetails);
			Integer ulw = this.getUlwWeight(stagingRegDetails);
			newPermitTax = getOldCovTax(stagingRegDetails.getClassOfVehicle(),
					stagingRegDetails.getVahanDetails().getSeatingCapacity(), ulw, gvw, stateCode, permitType,
					routeCode);
			if(serviceEnum != null && !serviceEnum.isEmpty()
					&& serviceEnum.stream().anyMatch(type -> type.equals(ServiceEnum.STAGECARRIAGEREPLACEMENTOFVEHICLE))) {
			
				Double scrtTax  = getScrtVehicleTax(stagingRegDetails.getOldPrNo());
				if(scrtTax==0 || scrtTax<0) {
					throw new BadRequestException("Scrt tax not found for : " + stagingRegDetails.getOldPrNo());
				}
				newPermitTax = scrtTax;
			}

		}
		return newPermitTax;
	}

	private TaxHelper vcrIntegration(Pair<List<VcrFinalServiceDTO>, CitizenFeeDetailsInput> listOfVcrsDetails,
			boolean vcr) {
		TaxHelper taxHelper = new TaxHelper();
		boolean vcrFlag = Boolean.FALSE;
		/*
		 * VcrInputVo vcrInputVo = new VcrInputVo(); vcrInputVo.setDocumentType("RC");
		 * vcrInputVo.setRegNo(stagingRegDetails.getPrNo()); VcrBookingData entity =
		 * restGateWayService.getVcrDetailsCfst(vcrInputVo); if (entity != null) { if
		 * (entity.getVcrStatus().equalsIgnoreCase("O")) { vcrFlag = Boolean.TRUE; }
		 * //TODO need to check vehicle sized. }
		 */

		if (vcr) {
			CitizenFeeDetailsInput input = listOfVcrsDetails.getSecond();
			taxHelper.setVehicleSized(input.isVehicleSized());
			taxHelper.setSizedDate(input.getVehicleSizedDate());
			vcrFlag = Boolean.TRUE;
			taxHelper.setOldCovLife(input.isOldCovLife());
			taxHelper.setLatestVcrDate(input.getLatestVcrDate());
			if (listOfVcrsDetails.getSecond().getVcrTax() != null && listOfVcrsDetails.getSecond().getVcrTax() > 0) {
				taxHelper.setVcrTax(listOfVcrsDetails.getSecond().getVcrTax());
				taxHelper.setPlayedAsQuarterEnd(listOfVcrsDetails.getSecond().getPlayedAsQuarterEnd());
			}
		}
		// taxHelper.setVcrTax(50000d);
		// taxHelper.setPlayedAsQuarterEnd(LocalDate.of(2018, 05, 17));
		// taxHelper.setVehicleSized(Boolean.TRUE);
		// taxHelper.setSizedDate(LocalDate.of(2019, 01, 17));
		taxHelper.setVcr(vcrFlag);

		return taxHelper;
	}

	private Double getTaxWithNewPermit(Double OptionalTax, Double newPermitTax, int noOfMonths) {
		Double quaterTax;
		double oldTaxPerMonth = 0d;
		double newTaxPerMonth = 0d;
		quaterTax = OptionalTax;
		if (newPermitTax != null && newPermitTax != 0) {
			oldTaxPerMonth = OptionalTax / 3;
			newTaxPerMonth = newPermitTax / 3;
			if (noOfMonths == 1) {
				quaterTax = (oldTaxPerMonth * 2) + (newTaxPerMonth * 1);
			} else if (noOfMonths == 2) {
				quaterTax = (oldTaxPerMonth * 1) + (newTaxPerMonth * 2);
			} else {
				quaterTax = (oldTaxPerMonth * 0) + (newTaxPerMonth * 3);
			}
			// quaterTax = (oldTaxPerMonth * noOfMonths) - (newTaxPerMonth * noOfMonths);
		}
		return quaterTax;
	}

	private Double getTaxWithNewPermitWitOutPenality(Double OptionalTax, Double newPermitTax, int noOfMonths) {
		Double quaterTax;
		double oldTaxPerMonth = 0d;
		double newTaxPerMonth = 0d;
		quaterTax = OptionalTax;
		if (newPermitTax != null && newPermitTax != 0) {
			oldTaxPerMonth = OptionalTax / 3d;
			newTaxPerMonth = newPermitTax / 3d;
			quaterTax = (newTaxPerMonth * noOfMonths) - (oldTaxPerMonth * noOfMonths);
			return quaterTax;
		}
		return 0d;
	}

	private Pair<Long, LocalDate> cessTaxCalculation(Double OptionalTax, RegistrationDetailsDTO stagingRegDetails,
			LocalDate currentTaxTill, RegServiceDTO regServiceDTO, boolean isApplicationFromMvi,
			StagingRegistrationDetailsDTO stagingRegistrationDetails, boolean isChassesApplication,
			String classOfvehicle, boolean isOtherState,Pair<List<VcrFinalServiceDTO>, CitizenFeeDetailsInput> listOfVcrsDetails, boolean vcr) {
		List<Integer> quaterFour = new ArrayList<>();
		LocalDate endDate = null;
		quaterFour.add(0, 1);
		quaterFour.add(1, 2);
		quaterFour.add(2, 3);
		if (isOtherState) {
			return Pair.of(0l, LocalDate.now());
		}
		List<String> list = taxTypes();
		list.add(ServiceCodeEnum.CESS_FEE.getCode());
		TaxHelper lastTax = getLastPaidTax(stagingRegDetails, regServiceDTO, isApplicationFromMvi,
				validity(ServiceCodeEnum.CESS_FEE.getCode()), stagingRegistrationDetails, isChassesApplication, list,
				isOtherState, Boolean.FALSE);
		setObjectAsNull(null, null, null, list, null);
	/*	if ((lastTax != null && lastTax.getValidityTo() != null)) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
			String date1 = "31-03-2019";
			LocalDate cessEndDate = LocalDate.parse(date1, formatter);
			String date2 = "30-06-2018";
			LocalDate taxValidity = LocalDate.parse(date2, formatter);
			if (!isChassesApplication) {
				if ((lastTax.getValidityTo().equals(cessEndDate) || lastTax.getValidityTo().isBefore(cessEndDate))
						&& (taxValidity.equals(lastTax.getValidityTo())
								|| taxValidity.isBefore(lastTax.getValidityTo()))) {
					return Pair.of(0l, LocalDate.now());
				}
			}
		}*/
		if (quaterFour.contains(LocalDate.now().getMonthValue())) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
			String date1 = "31-03-" + String.valueOf(LocalDate.now().getYear());
			endDate = LocalDate.parse(date1, formatter);
		} else {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
			String date1 = "31-03-" + String.valueOf(LocalDate.now().plusYears(1).getYear());
			endDate = LocalDate.parse(date1, formatter);
		}
		TaxHelper lastTaxTillDate = getLastPaidTax(stagingRegDetails, regServiceDTO, isApplicationFromMvi, endDate,
				stagingRegistrationDetails, isChassesApplication, Arrays.asList(ServiceCodeEnum.CESS_FEE.getCode()),
				isOtherState, Boolean.FALSE);
		/*
		 * if ((lastTaxTillDate == null || lastTaxTillDate.getAmount() == null)&&
		 * !isApplicationFromMvi) { throw new
		 * BadRequestException("TaxDetails not found"); }
		 */
		if ((lastTaxTillDate == null || lastTaxTillDate.getTax() == null)) {
			Pair<Long, LocalDate> cessfeesAndValidity = getCesFee(OptionalTax, classOfvehicle, null, classOfvehicle,
					isChassesApplication,listOfVcrsDetails,vcr);
			return cessfeesAndValidity;
		}
		if (lastTaxTillDate.isAnypendingQuaters()) {
			// cal cess
			String applicatioNo = StringUtils.EMPTY;
			if (isApplicationFromMvi) {
				applicatioNo = regServiceDTO.getApplicationNo();
			} else {
				applicatioNo = stagingRegistrationDetails != null ? stagingRegistrationDetails.getApplicationNo()
						: stagingRegDetails.getApplicationNo();
			}
			// LocalDate lastTaxDate = getCessDetails(lastTaxTillDate, applicatioNo);
			Pair<Long, LocalDate> cessfeesAndValidity = getCesFee(OptionalTax, classOfvehicle, null, classOfvehicle,
					isChassesApplication,listOfVcrsDetails,vcr);
			return cessfeesAndValidity;
		} else {
			return Pair.of(0l, LocalDate.now());
		}

	}

	private Pair<Double, Double> getpendingQuaters(LocalDate currentQuater, LocalDate lastTaxTillDate,
			Double OptionalTax, Double finalquaterTax, Double quaterTax, TaxHelper vcrDetails,
			Double excemtionpenalityArrears, Double excemtionTaxArrears, boolean isEibtVehicle,
			LocalDate prGeneratedDate, Double obtOptionalTax) {
		Double penality = 0d;
		finalquaterTax = 0d;
		Long totalUnSizedMonths = 0l;
		Long totalMonths = ChronoUnit.MONTHS.between(lastTaxTillDate.withDayOfMonth(1), currentQuater.withDayOfMonth(1));
		totalMonths = Math.abs(totalMonths);
		// totalMonths = totalMonths ;
		double TotalQuaters = totalMonths / 3d;
		// TotalQuaters = Math.ceil(TotalQuaters);
		double penalityArrearsQuaters = TotalQuaters - 1;
		if(penalityArrearsQuaters<0) {
			penalityArrearsQuaters=0d;
		}
		double penalityArrearsQuatersRound = Math.ceil(penalityArrearsQuaters);
		double taxArrearsQuaters = TotalQuaters - 1;
		if (excemtionpenalityArrears != 0) {
			penalityArrearsQuaters = penalityArrearsQuaters - excemtionpenalityArrears.intValue();
		}
		if (excemtionTaxArrears != 0) {
			taxArrearsQuaters = taxArrearsQuaters - excemtionTaxArrears.intValue();
		}
		if (isEibtVehicle) {
			return getPenalityForEibtAgeIsGreaterThan15(vcrDetails, prGeneratedDate, lastTaxTillDate, currentQuater,
					OptionalTax, obtOptionalTax);
		}

		if (vcrDetails.isVcr()) {
			Long vcrNewCovMonths = 0l;
			// TODO need to check suspend
			int penalityPercent = 200;
			boolean vcrFlagForDuctionMode = vcrDetails.isVcr();
			if(this.checkTaxdeductionModeOrNotForOldQuarters(vcrDetails)) {
				penalityPercent = 50;
				vcrFlagForDuctionMode = Boolean.FALSE;
			}
			if (vcrDetails.isVehicleSized()) {
				Pair<Integer, Integer> postionForCurrent = this.getMonthposition(currentQuater);
				Pair<Integer, Integer> postionForSized = this.getMonthposition(vcrDetails.getSizedDate());
				//LocalDate quarterSatart  = this.getQuarterStatrtDate(postion.getFirst(), postion.getSecond(), currentQuater);
				if(vcrDetails.getSizedDate().getYear()!=currentQuater.getYear()||postionForCurrent.getSecond() != postionForSized.getSecond()) {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
					String date1 = "01-" + String.valueOf(vcrDetails.getSizedDate().getMonthValue()) + "-"
							+ String.valueOf(vcrDetails.getSizedDate().getYear());
					LocalDate localDate = LocalDate.parse(date1, formatter);
					Long sizedMonths = ChronoUnit.MONTHS.between(localDate, LocalDate.now());
					sizedMonths = sizedMonths - 1;
					if (sizedMonths < 0) {
						sizedMonths = 0l;
					}
					if(vcrDetails.getSizedDate().isAfter(lastTaxTillDate)) {
						sizedMonths-=1;
					}
					 totalUnSizedMonths = (totalMonths - 3) - sizedMonths;
					if(totalUnSizedMonths<0) {
						totalUnSizedMonths=0l;
					}
					
					penalityArrearsQuaters = totalUnSizedMonths / 3d;
					penalityArrearsQuatersRound = Math.ceil(totalUnSizedMonths / 3d);
				}
			
				if (vcrDetails.getVcrTax() != null && vcrDetails.getVcrTax() > 0&& OptionalTax<=vcrDetails.getVcrTax()) {
					Pair<Integer, Integer>  sizedIndex = this.getMonthposition(vcrDetails.getSizedDate());
					Pair<Integer, Integer>  currentIndex = this.getMonthposition(LocalDate.now());
					if (vcrDetails.getSizedDate().isAfter(vcrDetails.getPlayedAsQuarterEnd())
							&&!(sizedIndex.getSecond()==currentIndex.getSecond() &&sizedIndex.getFirst()==currentIndex.getFirst()&& vcrDetails.getSizedDate().getYear()==LocalDate.now().getYear())) {
						
						Long newCvoTaxPenality = ChronoUnit.MONTHS.between(vcrDetails.getPlayedAsQuarterEnd(),
								vcrDetails.getSizedDate());
						newCvoTaxPenality = Math.abs(newCvoTaxPenality);
						newCvoTaxPenality += 1;
						vcrNewCovMonths = newCvoTaxPenality;
						if (newCvoTaxPenality > 0) {
							if (newCvoTaxPenality > totalUnSizedMonths) {
								newCvoTaxPenality = newCvoTaxPenality - totalUnSizedMonths;// set obt as 0
								totalUnSizedMonths = 0l;
							} else {
								totalUnSizedMonths = totalUnSizedMonths - newCvoTaxPenality;
							}
						}
					}
				}
				if (vcrNewCovMonths != 0 && vcrNewCovMonths > 3) {
					double Penalityquaters = vcrNewCovMonths / 3d;
					if(Penalityquaters<0) {
						Penalityquaters=0;
					}
					double PenalityQuatersRound = Math.ceil(vcrNewCovMonths / 3d);
					if (Penalityquaters == PenalityQuatersRound) {
						Pair<Double, Double> taxArr2AndPenality = chassisPenalitTax(vcrDetails.getVcrTax(),
								(Penalityquaters), vcrFlagForDuctionMode);
						finalquaterTax = finalquaterTax + taxArr2AndPenality.getFirst();
						penality = penality + taxArr2AndPenality.getSecond();
					} else {
						String numberD = String.valueOf(Penalityquaters);
						String number = numberD.substring(numberD.indexOf("."));
						String num = number.substring(1, 4);
						String val = numberD.substring(0, numberD.indexOf("."));
						Double taxPerMonth = vcrDetails.getVcrTax() / 3d;
						if (num.equalsIgnoreCase("666")) {
							penality = (((vcrDetails.getVcrTax() * penalityPercent) / 100) * Double.valueOf(val));
							finalquaterTax = (vcrDetails.getVcrTax() * Double.valueOf(val));
							penality = penality + ((taxPerMonth * 2) * penalityPercent / 100);
							finalquaterTax = finalquaterTax + (taxPerMonth * 2);
						} else {
							penality = (((vcrDetails.getVcrTax() * penalityPercent) / 100) * Double.valueOf(val));
							finalquaterTax = (vcrDetails.getVcrTax() * Double.valueOf(val));
							penality = penality + ((taxPerMonth * 1) * penalityPercent / 100);
							finalquaterTax = finalquaterTax + (taxPerMonth * 1);
						}
					}
				}
				
				if(penalityArrearsQuaters<0) {
					penalityArrearsQuaters=0;
				}
			
				if (penalityArrearsQuaters == penalityArrearsQuatersRound) {
					penality = penality + ((((OptionalTax * penalityPercent) / 100) * penalityArrearsQuaters));
					finalquaterTax = finalquaterTax + ((OptionalTax * penalityArrearsQuaters));
				} else {
					String numberD = String.valueOf(penalityArrearsQuaters);
					String number = numberD.substring(numberD.indexOf("."));
					String num = number.substring(1, 4);
					String val = numberD.substring(0, numberD.indexOf("."));
					Double taxPerMonth = OptionalTax / 3d;
					if (num.equalsIgnoreCase("666")) {
						penality = penality + ((((OptionalTax * penalityPercent) / 100) * Double.valueOf(val)));
						finalquaterTax = finalquaterTax + ((OptionalTax * Double.valueOf(val)));
						penality = penality + ((taxPerMonth * 2) * penalityPercent / 100);
						finalquaterTax = finalquaterTax + (taxPerMonth * 2);
					} else {
						penality = penality + ((((OptionalTax * penalityPercent) / 100) * Double.valueOf(val)));
						finalquaterTax = finalquaterTax + ((OptionalTax * Double.valueOf(val)));
						penality = penality + ((taxPerMonth * 1) * penalityPercent / 100);
						finalquaterTax = finalquaterTax + (taxPerMonth * 1);
					}
				}

			} else {
				if (vcrDetails.getVcrTax() != null && vcrDetails.getVcrTax() > 0&& OptionalTax<=vcrDetails.getVcrTax()) {
					if (vcrDetails.getPlayedAsQuarterEnd().isAfter(lastTaxTillDate)) {
						Long newCvoTaxPenality = ChronoUnit.MONTHS.between(vcrDetails.getPlayedAsQuarterEnd(),
								currentQuater);
						newCvoTaxPenality = Math.abs(newCvoTaxPenality);
						newCvoTaxPenality += 1;
						newCvoTaxPenality = newCvoTaxPenality - 3;
						double Penalityquaters = newCvoTaxPenality / 3d;
						double PenalityQuatersRound = Math.ceil(newCvoTaxPenality / 3d);
						if (Penalityquaters == PenalityQuatersRound) {
							Pair<Double, Double> taxArr2AndPenality = chassisPenalitTax(vcrDetails.getVcrTax(),
									(Penalityquaters), vcrFlagForDuctionMode);
							finalquaterTax = finalquaterTax + taxArr2AndPenality.getFirst();
							penality = penality + taxArr2AndPenality.getSecond();
						} else {
							String numberD = String.valueOf(Penalityquaters);
							String number = numberD.substring(numberD.indexOf("."));
							String num = number.substring(1, 4);
							String val = numberD.substring(0, numberD.indexOf("."));
							Double taxPerMonth = vcrDetails.getVcrTax() / 3d;
							if (num.equalsIgnoreCase("666")) {
								penality = (((vcrDetails.getVcrTax() * penalityPercent) / 100) * Double.valueOf(val));
								finalquaterTax = (vcrDetails.getVcrTax() * Double.valueOf(val));
								penality = penality + ((taxPerMonth * 2) * penalityPercent / 100);
								finalquaterTax = finalquaterTax + (taxPerMonth * 2);
							} else {
								penality = (((vcrDetails.getVcrTax() * penalityPercent) / 100) * Double.valueOf(val));
								finalquaterTax = (vcrDetails.getVcrTax() * Double.valueOf(val));
								penality = penality + ((taxPerMonth * 1) * penalityPercent / 100);
								finalquaterTax = finalquaterTax + (taxPerMonth * 1);
							}
						}
						totalMonths = ChronoUnit.MONTHS.between(lastTaxTillDate, vcrDetails.getPlayedAsQuarterEnd());
						double quaters = totalMonths / 3d;
						penalityArrearsQuaters = quaters;
						penalityArrearsQuatersRound = Math.ceil(quaters);
					} else {
						OptionalTax = vcrDetails.getVcrTax();
					}
				}

				if (penalityArrearsQuaters == penalityArrearsQuatersRound) {
					penality = penality + ((((OptionalTax * penalityPercent) / 100) * penalityArrearsQuaters));
					finalquaterTax = finalquaterTax + ((OptionalTax * penalityArrearsQuaters));
				} else {
					String numberD = String.valueOf(penalityArrearsQuaters);
					String number = numberD.substring(numberD.indexOf("."));
					String num = number.substring(1, 4);
					String val = numberD.substring(0, numberD.indexOf("."));
					Double taxPerMonth = OptionalTax / 3d;
					if (num.equalsIgnoreCase("666")) {
						penality = penality + ((((OptionalTax * penalityPercent) / 100) * Double.valueOf(val)));
						finalquaterTax = finalquaterTax + ((OptionalTax * Double.valueOf(val)));
						penality = penality + ((taxPerMonth * 2) * penalityPercent / 100);
						finalquaterTax = finalquaterTax + (taxPerMonth * 2);
					} else {
						penality = penality + ((((OptionalTax * penalityPercent) / 100) * Double.valueOf(val)));
						finalquaterTax = finalquaterTax + ((OptionalTax * Double.valueOf(val)));
						penality = penality + ((taxPerMonth * 1) * penalityPercent / 100);
						finalquaterTax = finalquaterTax + (taxPerMonth * 1);
					}
				}
			}
		} else {

			penality = (((OptionalTax * 50) / 100) * penalityArrearsQuaters);
			finalquaterTax = (OptionalTax * taxArrearsQuaters);
		}
		return Pair.of(finalquaterTax, penality);
	}

	private Pair<Double, Double> getPenalityForEibtAgeIsGreaterThan15(TaxHelper vcrDetails, LocalDate prGeneratedDate,
			LocalDate lastTaxTillDate, LocalDate currentQuater, Double eibtOptionalTax, Double obtOptionalTax) {
		LocalDate dateOfTheVehicle = prGeneratedDate.plusYears(15);

		Long obtMonths = 0l;
		Long eibtMonths = 0l;
		Double penality = 0d;
		Double finalquaterTax = 0d;
		Long vcrNewCovMonths = 0l;
		if (lastTaxTillDate.isBefore(dateOfTheVehicle)) {
			eibtMonths = ChronoUnit.MONTHS.between(lastTaxTillDate, dateOfTheVehicle);
			obtMonths = ChronoUnit.MONTHS.between(dateOfTheVehicle, currentQuater);
			obtMonths = obtMonths + 1;
		} else {
			obtMonths = ChronoUnit.MONTHS.between(lastTaxTillDate, currentQuater);
		}
		eibtMonths = Math.abs(eibtMonths);
		obtMonths = Math.abs(obtMonths);
		obtMonths = obtMonths - 3;
		if (vcrDetails.getVcrTax() != null && vcrDetails.getVcrTax() > 0) {
			if (vcrDetails.getPlayedAsQuarterEnd().isAfter(lastTaxTillDate)
					|| vcrDetails.getPlayedAsQuarterEnd().equals(lastTaxTillDate)) {
				Long newCvoTaxPenality = ChronoUnit.MONTHS.between(vcrDetails.getPlayedAsQuarterEnd(), currentQuater);
				newCvoTaxPenality = Math.abs(newCvoTaxPenality);
				newCvoTaxPenality += 1;
				newCvoTaxPenality = newCvoTaxPenality - 3;
				vcrNewCovMonths = newCvoTaxPenality;
				if (newCvoTaxPenality > 0) {
					if (newCvoTaxPenality > obtMonths) {
						newCvoTaxPenality = newCvoTaxPenality - obtMonths;// set obt as 0
						obtMonths = 0l;
						if (newCvoTaxPenality > eibtMonths) {
							newCvoTaxPenality = newCvoTaxPenality - eibtMonths;
							eibtMonths = 0l;
						} else {
							eibtMonths = eibtMonths - newCvoTaxPenality;
						}

					} else {
						obtMonths = obtMonths - newCvoTaxPenality;
					}
				}
			} else {
				vcrNewCovMonths = ChronoUnit.MONTHS.between(lastTaxTillDate, currentQuater);
				vcrNewCovMonths = vcrNewCovMonths - 3;
				obtMonths = 0l;
				eibtMonths = 0l;
			}
		}

		double obtTotalQuaters = obtMonths / 3d;
		double obttTotalquatersRound = Math.ceil(obtMonths / 3);

		double eibtTotalQuaters = eibtMonths / 3d;
		double eibtTotalquatersRound = Math.ceil(eibtMonths / 3);

		if (vcrDetails.isVehicleSized()) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
			String date1 = "01-" + String.valueOf(vcrDetails.getSizedDate().getMonthValue()) + "-"
					+ String.valueOf(vcrDetails.getSizedDate().getYear());
			LocalDate localDate = LocalDate.parse(date1, formatter);
			Long sizedMonthsForEibt = 0l;
			Long sizedMonthsForObt = 0l;

			if (lastTaxTillDate.isBefore(dateOfTheVehicle)) {
				if (dateOfTheVehicle.isBefore(localDate)) {
					sizedMonthsForEibt = ChronoUnit.MONTHS.between(lastTaxTillDate, dateOfTheVehicle);
					sizedMonthsForObt = ChronoUnit.MONTHS.between(dateOfTheVehicle, localDate);
				} else {
					sizedMonthsForObt = ChronoUnit.MONTHS.between(localDate, LocalDate.now());
				}
			} else {
				sizedMonthsForObt = ChronoUnit.MONTHS.between(localDate, LocalDate.now());
			}
			sizedMonthsForObt = sizedMonthsForObt + 2;
			if (vcrDetails.getVcrTax() != null && vcrDetails.getVcrTax() > 0) {
			if (vcrDetails.getSizedDate().isAfter(vcrDetails.getPlayedAsQuarterEnd())) {
				Long newCvoTaxPenality = ChronoUnit.MONTHS.between(vcrDetails.getPlayedAsQuarterEnd(),
						vcrDetails.getSizedDate());
				newCvoTaxPenality = Math.abs(newCvoTaxPenality);
				newCvoTaxPenality += 1;
				vcrNewCovMonths = newCvoTaxPenality;
				if (newCvoTaxPenality > 0) {
					if (newCvoTaxPenality > sizedMonthsForObt) {
						newCvoTaxPenality = newCvoTaxPenality - sizedMonthsForObt;// set obt as 0
						sizedMonthsForObt = 0l;
						if (newCvoTaxPenality > sizedMonthsForEibt) {
							newCvoTaxPenality = newCvoTaxPenality - sizedMonthsForEibt;
							sizedMonthsForEibt = 0l;
						} else {
							sizedMonthsForEibt = sizedMonthsForEibt - newCvoTaxPenality;
						}

					} else {
						sizedMonthsForObt = sizedMonthsForObt - newCvoTaxPenality;
					}
				}

			}
		}
			if (sizedMonthsForEibt < 0) {
				sizedMonthsForEibt = 0l;
			}
			if (obtMonths < 0) {
				obtMonths = 0l;
			}
			// Long totalUnSizedMonthseibt = (sizedMonthsForEibt - 3) - sizedMonthsForEibt;

			if (sizedMonthsForEibt != 0 && sizedMonthsForEibt > 0) {
				double eibtquaters = sizedMonthsForEibt / 3d;
				double eibtquatersRound = Math.ceil(sizedMonthsForEibt / 3);
				if (eibtquaters == eibtquatersRound) {
					penality = (((eibtOptionalTax * 200) / 100) * eibtquaters);
					finalquaterTax = (eibtOptionalTax * eibtquaters);
				} else {
					String numberD = String.valueOf(eibtquaters);
					String number = numberD.substring(numberD.indexOf("."));
					String num = number.substring(1, 4);
					String val = numberD.substring(0, numberD.indexOf("."));
					Double taxPerMonth = eibtOptionalTax / 3d;
					if (num.equalsIgnoreCase("666")) {
						penality = (((eibtOptionalTax * 200) / 100) * Double.valueOf(val));
						finalquaterTax = (eibtOptionalTax * Double.valueOf(val));
						penality = penality + ((taxPerMonth * 2) * 200 / 100);
						finalquaterTax = finalquaterTax + (taxPerMonth * 2);
					} else {
						penality = (((eibtOptionalTax * 200) / 100) * Double.valueOf(val));
						finalquaterTax = (eibtOptionalTax * Double.valueOf(val));
						penality = penality + ((taxPerMonth * 1) * 200 / 100);
						finalquaterTax = finalquaterTax + (taxPerMonth * 1);
					}
				}
			}
			if (sizedMonthsForObt != 0 && sizedMonthsForObt > 0) {
				double obttquaters = obtMonths / 3d;
				double obttquatersRound = Math.ceil(obtMonths / 3);
				if (obttquaters == obttquatersRound) {
					penality = penality + (((obtOptionalTax * 200) / 100) * obttquaters);
					finalquaterTax = finalquaterTax + (obtOptionalTax * obttquaters);
				} else {
					String numberD = String.valueOf(obttquaters);
					String number = numberD.substring(numberD.indexOf("."));
					String num = number.substring(1, 4);
					String val = numberD.substring(0, numberD.indexOf("."));
					Double taxPerMonth = obtOptionalTax / 3d;
					if (num.equalsIgnoreCase("666")) {
						penality = penality + ((((obtOptionalTax * 200) / 100) * Double.valueOf(val)));
						finalquaterTax = finalquaterTax + ((obtOptionalTax * Double.valueOf(val)));
						penality = penality + ((taxPerMonth * 2) * 200 / 100);
						finalquaterTax = finalquaterTax + (finalquaterTax + (taxPerMonth * 2));
					} else {
						penality = penality + ((((obtOptionalTax * 200) / 100) * Double.valueOf(val)));
						finalquaterTax = finalquaterTax + ((obtOptionalTax * Double.valueOf(val)));
						penality = penality + ((taxPerMonth * 1) * 200 / 100);
						finalquaterTax = finalquaterTax + (taxPerMonth * 1);
					}
				}
			}
			if (vcrNewCovMonths != 0 && vcrNewCovMonths > 0) {
				double eibtquaters = vcrNewCovMonths / 3d;
				double eibtquatersRound = Math.ceil(vcrNewCovMonths / 3);
				if (eibtquaters == eibtquatersRound) {
					penality = penality + ((((vcrDetails.getVcrTax() * 200) / 100) * eibtquaters));
					finalquaterTax = finalquaterTax + ((vcrDetails.getVcrTax() * eibtquaters));
				} else {
					String numberD = String.valueOf(eibtquaters);
					String number = numberD.substring(numberD.indexOf("."));
					String num = number.substring(1, 4);
					String val = numberD.substring(0, numberD.indexOf("."));
					Double taxPerMonth = vcrDetails.getVcrTax() / 3d;
					if (num.equalsIgnoreCase("666")) {
						penality = penality + ((((vcrDetails.getVcrTax() * 200) / 100) * Double.valueOf(val)));
						finalquaterTax = finalquaterTax + ((vcrDetails.getVcrTax() * Double.valueOf(val)));
						penality = penality + ((taxPerMonth * 2) * 200 / 100);
						finalquaterTax = finalquaterTax + (taxPerMonth * 2);
					} else {
						penality = penality + ((((vcrDetails.getVcrTax() * 200) / 100) * Double.valueOf(val)));
						finalquaterTax = finalquaterTax + ((vcrDetails.getVcrTax() * Double.valueOf(val)));
						penality = penality + ((taxPerMonth * 1) * 200 / 100);
						finalquaterTax = finalquaterTax + (taxPerMonth * 1);
					}
				}
			}
		} else {
			if (vcrDetails.isVcr()) {
				if (eibtTotalQuaters == eibtTotalquatersRound) {
					penality = (((eibtOptionalTax * 200) / 100) * eibtTotalQuaters);
					finalquaterTax = (eibtOptionalTax * eibtTotalQuaters);
				} else {
					String numberD = String.valueOf(eibtTotalQuaters);
					String number = numberD.substring(numberD.indexOf("."));
					String num = number.substring(1, 4);
					String val = numberD.substring(0, numberD.indexOf("."));
					Double taxPerMonth = eibtOptionalTax / 3d;
					if (num.equalsIgnoreCase("666")) {
						penality = (((eibtOptionalTax * 200) / 100) * Double.valueOf(val));
						finalquaterTax = (eibtOptionalTax * Double.valueOf(val));
						penality = penality + ((taxPerMonth * 2) * 200 / 100);
						finalquaterTax = finalquaterTax + (taxPerMonth * 2);
					} else {
						penality = (((eibtOptionalTax * 200) / 100) * Double.valueOf(val));
						finalquaterTax = (eibtOptionalTax * Double.valueOf(val));
						penality = penality + ((taxPerMonth * 1) * 200 / 100);
						finalquaterTax = finalquaterTax + (taxPerMonth * 1);
					}
				}

				if (obtMonths != 0 && obtMonths > 0) {

					if (obttTotalquatersRound == obtTotalQuaters) {
						penality = penality + (((obtOptionalTax * 200) / 100) * obtTotalQuaters);
						finalquaterTax = finalquaterTax + (obtOptionalTax * obtTotalQuaters);
					} else {
						String numberD = String.valueOf(obtTotalQuaters);
						String number = numberD.substring(numberD.indexOf("."));
						String num = number.substring(1, 4);
						String val = numberD.substring(0, numberD.indexOf("."));
						Double taxPerMonth = obtOptionalTax / 3d;
						if (num.equalsIgnoreCase("666")) {
							penality = penality + ((((obtOptionalTax * 200) / 100) * Double.valueOf(val)));
							finalquaterTax = finalquaterTax + ((obtOptionalTax * Double.valueOf(val)));
							penality = penality + ((taxPerMonth * 2) * 200 / 100);
							finalquaterTax = finalquaterTax + (taxPerMonth * 2);
						} else {
							penality = penality + ((((obtOptionalTax * 200) / 100) * Double.valueOf(val)));
							finalquaterTax = finalquaterTax + ((obtOptionalTax * Double.valueOf(val)));
							penality = penality + ((taxPerMonth * 1) * 200 / 100);
							finalquaterTax = finalquaterTax + (taxPerMonth * 1);
						}
					}
				}
			} else {
				if (eibtTotalQuaters == eibtTotalquatersRound) {
					penality = (((eibtOptionalTax * 50) / 100) * eibtTotalQuaters);
					finalquaterTax = (eibtOptionalTax * eibtTotalQuaters);
				} else {
					String numberD = String.valueOf(eibtTotalQuaters);
					String number = numberD.substring(numberD.indexOf("."));
					String num = number.substring(1, 4);
					String val = numberD.substring(0, numberD.indexOf("."));
					Double taxPerMonth = eibtOptionalTax / 3d;
					if (num.equalsIgnoreCase("666")) {
						penality = (((eibtOptionalTax * 50) / 100) * Double.valueOf(val));
						finalquaterTax = (eibtOptionalTax * Double.valueOf(val));
						penality = penality + ((taxPerMonth * 2) * 50 / 100);
						finalquaterTax = finalquaterTax + (taxPerMonth * 2);
					} else {
						penality = (((eibtOptionalTax * 50) / 100) * Double.valueOf(val));
						finalquaterTax = (eibtOptionalTax * Double.valueOf(val));
						penality = penality + ((taxPerMonth * 1) * 50 / 100);
						finalquaterTax = finalquaterTax + (taxPerMonth * 1);
					}
				}

				if (obtMonths != 0 && obtMonths > 0) {

					if (obttTotalquatersRound == obtTotalQuaters) {
						penality = penality + (((obtOptionalTax * 50) / 100) * obtTotalQuaters);
						finalquaterTax = finalquaterTax + (obtOptionalTax * obtTotalQuaters);
					} else {
						String numberD = String.valueOf(obtTotalQuaters);
						String number = numberD.substring(numberD.indexOf("."));
						String num = number.substring(1, 4);
						String val = numberD.substring(0, numberD.indexOf("."));
						Double taxPerMonth = obtOptionalTax / 3d;
						if (num.equalsIgnoreCase("666")) {
							penality = penality + ((((obtOptionalTax * 50) / 100) * Double.valueOf(val)));
							finalquaterTax = finalquaterTax + ((obtOptionalTax * Double.valueOf(val)));
							penality = penality + ((taxPerMonth * 2) * 50 / 100);
							finalquaterTax = finalquaterTax + (taxPerMonth * 2);
						} else {
							penality = penality + ((((obtOptionalTax * 50) / 100) * Double.valueOf(val)));
							finalquaterTax = finalquaterTax + ((obtOptionalTax * Double.valueOf(val)));
							penality = penality + ((taxPerMonth * 1) * 50 / 100);
							finalquaterTax = finalquaterTax + (taxPerMonth * 1);
						}
					}
				}
			}
			if (vcrNewCovMonths != 0 && vcrNewCovMonths > 0) {
				double eibtquaters = vcrNewCovMonths / 3d;
				double eibtquatersRound = Math.ceil(vcrNewCovMonths / 3);
				if (eibtquaters == eibtquatersRound) {
					penality = penality + ((((vcrDetails.getVcrTax() * 200) / 100) * eibtquaters));
					finalquaterTax = finalquaterTax + ((vcrDetails.getVcrTax() * eibtquaters));
				} else {
					String numberD = String.valueOf(eibtquaters);
					String number = numberD.substring(numberD.indexOf("."));
					String num = number.substring(1, 4);
					String val = numberD.substring(0, numberD.indexOf("."));
					Double taxPerMonth = vcrDetails.getVcrTax() / 3d;
					if (num.equalsIgnoreCase("666")) {
						penality = penality + ((((vcrDetails.getVcrTax() * 200) / 100) * Double.valueOf(val)));
						finalquaterTax = finalquaterTax + ((vcrDetails.getVcrTax() * Double.valueOf(val)));
						penality = penality + ((taxPerMonth * 2) * 200 / 100);
						finalquaterTax = finalquaterTax + (taxPerMonth * 2);
					} else {
						penality = penality + ((((vcrDetails.getVcrTax() * 200) / 100) * Double.valueOf(val)));
						finalquaterTax = finalquaterTax + ((vcrDetails.getVcrTax() * Double.valueOf(val)));
						penality = penality + ((taxPerMonth * 1) * 200 / 100);
						finalquaterTax = finalquaterTax + (taxPerMonth * 1);
					}
				}
			}
		}
		return Pair.of(finalquaterTax, penality);
	}

	@Override
	public TaxHelper getLastPaidTax(RegistrationDetailsDTO stagingRegDetails, RegServiceDTO regServiceDTO,
			boolean isApplicationFromMvi, LocalDate currentTaxTill,
			StagingRegistrationDetailsDTO stagingRegistrationDetails, boolean isChassesApplication,
			List<String> taxTypes, boolean isOtherState, boolean vcr) {
		if (vcr) {
			taxTypes.add(TaxTypeEnum.LifeTax.getDesc());
		}
		if (stagingRegDetails != null) {
			Optional<MasterTaxExcemptionsDTO> optionalTaxExcemption = masterTaxExcemptionsDAO
					.findByKeyvalue(stagingRegDetails.getVahanDetails().getMakersModel());
			if (optionalTaxExcemption.isPresent()) {
				return this.addTaxDetails(TaxTypeEnum.QuarterlyTax.getDesc(), 0d,
						validity(TaxTypeEnum.QuarterlyTax.getDesc()), Boolean.FALSE, LocalDateTime.now(),
						Boolean.FALSE);
			}
		}

		String applicationNo = StringUtils.EMPTY;
		if (isApplicationFromMvi) {
			applicationNo = regServiceDTO.getApplicationNo();
		} else if (isOtherState) {
			if (regServiceDTO != null
					&& (regServiceDTO.getRegistrationDetails().getClassOfVehicle()
							.equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
							|| regServiceDTO.getRegistrationDetails().getClassOfVehicle()
									.equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode()))
					&& regServiceDTO.isMviDone()) {
				applicationNo = regServiceDTO.getApplicationNo();
			}

		} else {
			applicationNo = stagingRegDetails != null ? stagingRegDetails.getApplicationNo()
					: stagingRegistrationDetails.getApplicationNo();
		}
		List<TaxDetailsDTO> listOfTax = getTaxDetails(stagingRegDetails, taxTypes, regServiceDTO, isApplicationFromMvi,
				stagingRegistrationDetails, isChassesApplication, isOtherState);
		if (listOfTax.isEmpty() && ((!taxTypes.contains(ServiceCodeEnum.CESS_FEE.getCode())) || vcr)) {
			setObjectAsNull(null, null, null, taxTypes, listOfTax);
			logger.error("TaxDetails not found: [{}]", applicationNo);
			throw new BadRequestException("TaxDetails not found:" + applicationNo);
		}
		if (listOfTax.isEmpty()) {
			setObjectAsNull(null, null, null, taxTypes, listOfTax);
			return this.addTaxDetails(null, null, null, Boolean.FALSE, null, Boolean.FALSE);
		}
		return getcommenlastTaxPaid(currentTaxTill, taxTypes, applicationNo, listOfTax);
	}

	@Override
	public TaxHelper getcommenlastTaxPaid(LocalDate currentTaxTill, List<String> taxTypes, String applicationNo,
			List<TaxDetailsDTO> listOfTax) {
		listOfTax.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		TaxDetailsDTO dto = listOfTax.stream().findFirst().get();
		if (dto.getTaxDetails() == null) {
			logger.error("TaxDetails not found: [{}]", applicationNo);
			throw new BadRequestException("TaxDetails not found:" + applicationNo);
		}
		for (Map<String, TaxComponentDTO> map : dto.getTaxDetails()) {

			for (Entry<String, TaxComponentDTO> entry : map.entrySet()) {
				if (taxTypes.stream().anyMatch(key -> key.equalsIgnoreCase(entry.getKey()))) {
					if (entry.getKey().equalsIgnoreCase(ServiceCodeEnum.CESS_FEE.getCode())) {
						if (entry.getValue().getValidityTo() == null) {
							setObjectAsNull(null, null, null, taxTypes, listOfTax);
							return this.addTaxDetails(entry.getKey(), entry.getValue().getAmount(),
									entry.getValue().getValidityTo(), Boolean.TRUE, entry.getValue().getPaidDate(),
									dto.getTaxPaidThroughVcr());

						} else if (entry.getValue().getValidityTo().isBefore(currentTaxTill)) {
							setObjectAsNull(null, null, null, taxTypes, listOfTax);
							return this.addTaxDetails(entry.getKey(), entry.getValue().getAmount(),
									entry.getValue().getValidityTo(), Boolean.TRUE, entry.getValue().getPaidDate(),
									dto.getTaxPaidThroughVcr());
						} else {
							setObjectAsNull(null, null, null, taxTypes, listOfTax);
							return this.addTaxDetails(entry.getKey(), entry.getValue().getAmount(),
									entry.getValue().getValidityTo(), Boolean.FALSE, entry.getValue().getPaidDate(),
									dto.getTaxPaidThroughVcr());
						}
					} else {
						if (entry.getValue().getValidityTo().isBefore(currentTaxTill)) {
							setObjectAsNull(null, null, null, taxTypes, listOfTax);
							return this.addTaxDetails(entry.getKey(), entry.getValue().getAmount(),
									entry.getValue().getValidityTo(), Boolean.TRUE, entry.getValue().getPaidDate(),
									dto.getTaxPaidThroughVcr());

						} else if (entry.getValue().getValidityTo().equals(currentTaxTill)
								|| entry.getValue().getValidityTo().isAfter(currentTaxTill)) {
							if (dto.getTaxPaidThroughVcr() != null && dto.getTaxPaidThroughVcr()) {
								if (listOfTax.size() > 1) {
									TaxDetailsDTO latestSeconddto = listOfTax.get(1);
									if (latestSeconddto.getTaxPeriodEnd() != null && latestSeconddto.getTaxPeriodEnd()
											.equals(entry.getValue().getValidityTo())) {
										entry.getValue().setAmount(
												entry.getValue().getAmount() + latestSeconddto.getTaxAmount());
									}
								}
							}
							setObjectAsNull(null, null, null, taxTypes, listOfTax);
							return this.addTaxDetails(entry.getKey(), entry.getValue().getAmount(),
									entry.getValue().getValidityTo(), Boolean.FALSE, entry.getValue().getPaidDate(),
									dto.getTaxPaidThroughVcr());
						}
					}
				}
			}
		}
		return null;
	}

	private TaxHelper addTaxDetails(String taxType, Double taxAmount, LocalDate validityTo, boolean isAnypendingQuaters,
			LocalDateTime taxPaidDate, Boolean taxPaidThroughVcr) {

		TaxHelper tax = new TaxHelper();
		tax.setTaxName(taxType);
		tax.setTax(taxAmount);
		tax.setValidityTo(validityTo);
		tax.setTaxPaidDate(taxPaidDate);
		tax.setAnypendingQuaters(isAnypendingQuaters);
		if (!isAnypendingQuaters) {
			tax.setTaxPaidThroughVcr(taxPaidThroughVcr);
		}
		return tax;
	}

	private List<TaxDetailsDTO> getTaxDetails(RegistrationDetailsDTO registrationOptional, List<String> taxType,
			RegServiceDTO regServiceDTO, boolean isApplicationFromMvi,
			StagingRegistrationDetailsDTO stagingRegistrationDetails, boolean isChassesApplication,
			boolean isOtherState) {

		// List<TaxDetailsDTO> listOfTaxDetails = new ArrayList<>();
		// List<TaxDetailsDTO> listOfPaidTax = new ArrayList<>();
		String prNo = null;
		String trNo = null;
		String applicationNo = null;
		if (isChassesApplication) {
			trNo = stagingRegistrationDetails.getTrNo();
			applicationNo = stagingRegistrationDetails.getApplicationNo();
			/*
			 * listOfPaidTax = taxDetailsDAO.findByApplicationNoAndTrNoAndTaxStatus(
			 * stagingRegistrationDetails.getApplicationNo(),
			 * trNo,TaxStatusEnum.ACTIVE.getCode());
			 */
		} else if (isApplicationFromMvi) {
			prNo = regServiceDTO.getPrNo();
			applicationNo = regServiceDTO.getRegistrationDetails().getApplicationNo();
			// listOfPaidTax =
			// taxDetailsDAO.findByPrNoAndTaxStatus(prNo,TaxStatusEnum.ACTIVE.getCode());

		} else if (isOtherState) {
			if (regServiceDTO != null
					&& (regServiceDTO.getRegistrationDetails().getClassOfVehicle()
							.equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
							|| regServiceDTO.getRegistrationDetails().getClassOfVehicle()
									.equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode()))
					&& regServiceDTO.isMviDone()) {
				applicationNo = regServiceDTO.getRegistrationDetails().getApplicationNo();
			}
		} else {
			prNo = registrationOptional.getPrNo();
			applicationNo = registrationOptional.getApplicationNo();
			// listOfPaidTax
			// =taxDetailsDAO.findByPrNoAndTaxStatus(prNo,TaxStatusEnum.ACTIVE.getCode());
		}

		return taxDetails(applicationNo, taxType, prNo);
	}

	@Override
	public LocalDate calculateTaxUpTo(Integer indexPosition, Integer quaternNumber) {
		if (quaternNumber == 1) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
			String date1 = "01-04-" + String.valueOf(LocalDate.now().getYear());
			return validity(indexPosition, formatter, date1);
		} else if (quaternNumber == 2) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
			String date1 = "01-07-" + String.valueOf(LocalDate.now().getYear());
			return validity(indexPosition, formatter, date1);
		} else if (quaternNumber == 3) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
			String date1 = "01-10-" + String.valueOf(LocalDate.now().getYear());
			return validity(indexPosition, formatter, date1);
		} else if (quaternNumber == 4) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
			String date1 = "01-01-" + String.valueOf(LocalDate.now().getYear());
			return validity(indexPosition, formatter, date1);
		}
		return null;
	}

	@Override
	public LocalDate validity(String taxType) {
		List<Integer> quaterOne = new ArrayList<>();
		List<Integer> quaterTwo = new ArrayList<>();
		List<Integer> quaterThree = new ArrayList<>();
		List<Integer> quaterFour = new ArrayList<>();
		quaterOne.add(0, 4);
		quaterOne.add(1, 5);
		quaterOne.add(2, 6);
		quaterTwo.add(0, 7);
		quaterTwo.add(1, 8);
		quaterTwo.add(2, 9);
		quaterThree.add(0, 10);
		quaterThree.add(1, 11);
		quaterThree.add(2, 12);
		quaterFour.add(0, 1);
		quaterFour.add(1, 2);
		quaterFour.add(2, 3);
		Integer indexPosition = 0;
		Integer quaternNumber = 0;
		LocalDate Validity = null;
		if (quaterOne.contains(LocalDate.now().getMonthValue())) {
			indexPosition = quaterOne.indexOf(LocalDate.now().getMonthValue());
			quaternNumber = 1;
			Validity = calculateTaxUpTo(indexPosition, quaternNumber);
		} else if (quaterTwo.contains(LocalDate.now().getMonthValue())) {
			indexPosition = quaterTwo.indexOf(LocalDate.now().getMonthValue());
			quaternNumber = 2;
			Validity = calculateTaxUpTo(indexPosition, quaternNumber);
		} else if (quaterThree.contains(LocalDate.now().getMonthValue())) {
			indexPosition = quaterThree.indexOf(LocalDate.now().getMonthValue());
			quaternNumber = 3;
			Validity = calculateTaxUpTo(indexPosition, quaternNumber);
		} else if (quaterFour.contains(LocalDate.now().getMonthValue())) {
			indexPosition = quaterFour.indexOf(LocalDate.now().getMonthValue());
			quaternNumber = 4;
			Validity = calculateTaxUpTo(indexPosition, quaternNumber);
		}
		/*
		 * if (taxType.equalsIgnoreCase(TaxTypeEnum.HalfyearlyTax.getDesc())) { return
		 * Validity.plusMonths(3); } else if
		 * (taxType.equalsIgnoreCase(TaxTypeEnum.HalfyearlyTax.getDesc())) { return
		 * Validity.plusMonths(9); } else {
		 * 
		 * }
		 */
		return Validity;
	}

	public LocalDate validity(Integer indexPosition, DateTimeFormatter formatter, String date1) {
		LocalDate localDate = LocalDate.parse(date1, formatter);
		LocalDate newDate = localDate.withDayOfMonth(localDate.getMonth().maxLength());
		LocalDate newDate1 = newDate.plusMonths(2);
		return newDate1.withDayOfMonth(newDate1.getMonth().maxLength());
	}

	public long roundUpperTen(Double totalTax) {
		if ((totalTax % 10f) == 0) {
			return (int) Math.round(totalTax);
		} else {
			int taxIntValue = totalTax.intValue();
			if (taxIntValue % 10 == 9) {
				Double tax = Math.ceil(totalTax);
				if ((tax % 10f) != 0) {
					tax = tax + 1;
				}
				return tax.longValue();
			} else {
				return ((Math.round(totalTax) / 10) * 10 + 10);
			}
		}
	}

	private Pair<Long, LocalDate> getCesFee(Double quarterTax, String cov, LocalDate cessLastvalidity,
			String classOfvehicle, boolean isChassesApplication,Pair<List<VcrFinalServiceDTO>, CitizenFeeDetailsInput> listOfVcrsDetails, boolean vcr) {
		List<Integer> quaterFour = new ArrayList<>();
		quaterFour.add(0, 1);
		quaterFour.add(1, 2);
		quaterFour.add(2, 3);
		LocalDate endDate = LocalDate.now();
		Long cessFee = null;
		Long monthsBetween = null;
		if(vcr&&listOfVcrsDetails!=null && listOfVcrsDetails.getSecond()!=null && StringUtils.isNoneBlank(listOfVcrsDetails.getSecond().getNewCov()) 
				&& listOfVcrsDetails.getSecond().getVcrTax() !=null && quarterTax<=listOfVcrsDetails.getSecond().getVcrTax()) {
			cov = listOfVcrsDetails.getSecond().getNewCov();
			quarterTax = listOfVcrsDetails.getSecond().getVcrTax();
		}
		Optional<MasterTaxExcemptionsDTO> excemptionPercentage = Optional.empty();
		Optional<MasterTaxExcemptionsDTO> optionalTaxExcemption = masterTaxExcemptionsDAO.findByKeyvalue(cov);
		if (optionalTaxExcemption.isPresent()
				&& optionalTaxExcemption.get().getValuetype().equalsIgnoreCase(TaxTypeEnum.CESS.getCode())) {
			cessFee = optionalTaxExcemption.get().getTaxvalue().longValue();
			return Pair.of(cessFee, endDate);
		} else {
			excemptionPercentage = masterTaxExcemptionsDAO.findByKeyvalue(classOfvehicle);

			Double discount = (double) ((quarterTax * 4) / 12);
			if (quaterFour.contains(LocalDate.now().getMonthValue())) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
				String date1 = "31-03-" + String.valueOf(LocalDate.now().getYear());
				endDate = LocalDate.parse(date1, formatter);
				if (cessLastvalidity == null) {

					monthsBetween = Long.parseLong(String.valueOf(12));
					if (isChassesApplication) {
						int currentMonth = LocalDate.now().getMonthValue();
						int monthUpTo = endDate.getMonthValue();
						monthsBetween = Long.parseLong(String.valueOf((monthUpTo - currentMonth)));
						monthsBetween = monthsBetween + 1;
					}

					// long monthsBetween = ChronoUnit.MONTHS.between(LocalDate.now(), endDate);
					// monthsBetween = monthsBetween + 1;
				} else {
					monthsBetween = ChronoUnit.MONTHS
							.between(cessLastvalidity.withDayOfMonth(cessLastvalidity.getMonth().maxLength()), endDate);
				}
				Double totalCesFee = (discount * monthsBetween) * 10 / 100;
				if (excemptionPercentage.isPresent()
						&& !excemptionPercentage.get().getValuetype().equalsIgnoreCase(seatingCapacityCode)) {
					// check percentage discount
					double cessdiscount = (totalCesFee * excemptionPercentage.get().getTaxvalue()) / 100;
					totalCesFee = totalCesFee - cessdiscount;
				}
				if (totalCesFee > 1500) {
					cessFee = 1500l;
					return Pair.of(cessFee, endDate);
				} else {
					cessFee = roundUpperTen(totalCesFee);
					return Pair.of(cessFee, endDate);
				}

			} else {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
				String date1 = "31-03-" + String.valueOf(LocalDate.now().plusYears(1).getYear());
				endDate = LocalDate.parse(date1, formatter);
				if (cessLastvalidity == null) {
					monthsBetween = Long.parseLong(String.valueOf(12));
					if (isChassesApplication) {
						int currentMonth = LocalDate.now().getMonthValue();
						monthsBetween = Long.parseLong(String.valueOf((12 - currentMonth) + 1));// year end month and
																								// add current month.
						monthsBetween = monthsBetween + endDate.getMonthValue();
					}
				} else {
					monthsBetween = ChronoUnit.MONTHS
							.between(cessLastvalidity.withDayOfMonth(cessLastvalidity.getMonth().maxLength()), endDate);
				}
				Double totalCesFee = (discount * monthsBetween) * 10 / 100;
				if (excemptionPercentage.isPresent()
						&& !excemptionPercentage.get().getValuetype().equalsIgnoreCase(seatingCapacityCode)) {
					// check percentage discount
					double cessdiscount = (totalCesFee * excemptionPercentage.get().getTaxvalue()) / 100;
					totalCesFee = totalCesFee - cessdiscount;
				}
				if (totalCesFee > 1500) {
					cessFee = 1500l;
					return Pair.of(cessFee, endDate);
				} else {
					cessFee = roundUpperTen(totalCesFee);
					return Pair.of(cessFee, endDate);
				}
			}

		}
	}

	private Pair<Long, Double> finalLifeTaxCalculation(StagingRegistrationDetailsDTO stagingRegDetails,
			RegServiceDTO regServiceDTO, RegistrationDetailsDTO registrationOptional, Double totalLifeTax,
			Float Percent, boolean isApplicationFromMvi, boolean isChassesApplication, boolean isOtherState,
			boolean vcr, boolean isVehicleSized, LocalDate sizedDate) {
		Double penality = 0d;
		List<MasterTaxFuelTypeExcemptionDTO> list = masterTaxFuelTypeExcemptionDAO.findAll();
		Pair<String, String> prAndTr = getTrAndPrNo(stagingRegDetails, regServiceDTO, registrationOptional,
				totalLifeTax, Percent, isApplicationFromMvi, isChassesApplication, isOtherState);
		TaxHelper overRideTax = finalOverRideTaxForAllCasses(prAndTr.getFirst(), prAndTr.getSecond(), null);
		if (overRideTax != null && overRideTax.getTax() != null && overRideTax.getTax() != 0) {
			totalLifeTax = overRideTax.getTax();
			penality = overRideTax.getPenalty().doubleValue();
		}
		if (isChassesApplication) {
			if (stagingRegDetails.getOfficeDetails() == null
					|| stagingRegDetails.getOfficeDetails().getOfficeCode() == null) {
				logger.error("office details missing [{}].", stagingRegDetails.getApplicationNo());
				throw new BadRequestException("office details missing. " + stagingRegDetails.getApplicationNo());
			}
			if (list.stream().anyMatch(type -> type.getFuelType().stream()
					.anyMatch(fuel -> fuel.equalsIgnoreCase(stagingRegDetails.getVahanDetails().getFuelDesc())))) {
				totalLifeTax = batteryDiscount(stagingRegDetails.getInvoiceDetails().getInvoiceValue(), totalLifeTax,
						Percent, list, stagingRegDetails.getVahanDetails().getFuelDesc());

				long tax = roundUpperTen(totalLifeTax);
				return Pair.of(tax, 0d);

			} else {
				long tax = roundUpperTen(totalLifeTax);
				return Pair.of(tax, 0d);

			}
		} else if (isApplicationFromMvi) {
			if (regServiceDTO.getOfficeDetails() == null || regServiceDTO.getOfficeDetails().getOfficeCode() == null) {
				logger.error("office details missing [{}].", regServiceDTO.getApplicationNo());
				throw new BadRequestException("office details missing. " + regServiceDTO.getApplicationNo());
			}

			long tax = roundUpperTen(totalLifeTax);
			return Pair.of(tax, 0d);

		} else if (isOtherState) {
			Double penalty = 0d;
			RegServiceVO vo = regServiceMapper.convertEntity(regServiceDTO);
			if (vo.getOsSecondVechicleFoundRTO() != null && vo.getOsSecondVechicleFoundRTO()
					&& vo.getRegistrationDetails() != null && !vo.getRegistrationDetails().isRegVehicleWithPR()) {
				Double paidTax = 0.0;
				List<PaymentTransactionDTO> paymentTransactionDTOList = paymentTransactionDAO
						.findByApplicationFormRefNumAndPayStatus(vo.getApplicationNo(),
								PayStatusEnum.SUCCESS.getDescription());
				paymentTransactionDTOList
						.sort((p1, p2) -> p2.getRequest().getRequestTime().compareTo(p1.getRequest().getRequestTime()));
				if (paymentTransactionDTOList.size() > 0
						&& paymentTransactionDTOList.get(0).getFeeDetailsDTO() != null) {
					List<FeesDTO> paymentTransactionDTO = paymentTransactionDTOList.get(0).getFeeDetailsDTO()
							.getFeeDetails();

					for (FeesDTO bpsave : paymentTransactionDTO) {
						if (bpsave.getFeesType() != null
								&& bpsave.getFeesType().equals(ServiceCodeEnum.LIFE_TAX.getCode())
								|| bpsave.getFeesType().equals(ServiceCodeEnum.LIFE_TAX.getTypeDesc())) {
							paidTax += bpsave.getAmount();
						} else {
							if (bpsave.getFeesType() != null
									&& bpsave.getFeesType().equals(ServiceCodeEnum.QLY_TAX.getCode())
									|| bpsave.getFeesType().equals(ServiceCodeEnum.QLY_TAX.getTypeDesc())) {
								paidTax += bpsave.getAmount();
							} else if (bpsave.getFeesType() != null
									&& bpsave.getFeesType().equals(ServiceCodeEnum.HALF_TAX.getCode())
									|| bpsave.getFeesType().equals(ServiceCodeEnum.HALF_TAX.getTypeDesc())) {
								paidTax += bpsave.getAmount();// HALF_TAX
							} else if (bpsave.getFeesType() != null
									&& bpsave.getFeesType().equals(ServiceCodeEnum.YEAR_TAX.getCode())) {
								paidTax += bpsave.getAmount();// YEAR_TAX
							}
						}
					}

				}
				paidTax = totalLifeTax - paidTax;
				if (totalLifeTax > paidTax) {
					vo.setOsSecondVechicleFoundRTO(Boolean.FALSE);
					return Pair.of(roundUpperTen(paidTax), 0d);
				} else {
					vo.setOsSecondVechicleFoundRTO(Boolean.FALSE);
					return Pair.of(roundUpperTen(paidTax), 0d);
				}
			}
			OtherStateApplictionType applicationType = getOtherStateVehicleStatus(vo);
			if (OtherStateApplictionType.ApplicationNO.equals(applicationType)) {
				if (list.stream().anyMatch(type -> type.getFuelType().stream().anyMatch(fuel -> fuel
						.equalsIgnoreCase(regServiceDTO.getRegistrationDetails().getVahanDetails().getFuelDesc())))) {
					totalLifeTax = batteryDiscount(
							regServiceDTO.getRegistrationDetails().getInvoiceDetails().getInvoiceValue(), totalLifeTax,
							Percent, list, regServiceDTO.getRegistrationDetails().getVahanDetails().getFuelDesc());

					long tax = roundUpperTen(totalLifeTax);
					return Pair.of(tax, 0d);

				} else {
					long tax = roundUpperTen(totalLifeTax);
					return Pair.of(tax, 0d);

				}
			} else if (OtherStateApplictionType.TrNo.equals(applicationType)) {
				// VCR details is yes no need to collected taxi
				if (!regServiceDTO.getRegistrationDetails().isTaxPaidByVcr()) {
					if (list.stream()
							.anyMatch(type -> type.getFuelType().stream().anyMatch(fuel -> fuel.equalsIgnoreCase(
									regServiceDTO.getRegistrationDetails().getVahanDetails().getFuelDesc())))) {
						totalLifeTax = batteryDiscount(
								regServiceDTO.getRegistrationDetails().getInvoiceDetails().getInvoiceValue(),
								totalLifeTax, Percent, list,
								regServiceDTO.getRegistrationDetails().getVahanDetails().getFuelDesc());
						penality = otherStateNeedToPayPenality(
								regServiceDTO.getRegistrationDetails().getTrGeneratedDate().toLocalDate(), totalLifeTax,
								vcr, isVehicleSized, sizedDate);
						long tax = roundUpperTen(totalLifeTax);
						return Pair.of(tax, penality);

					} else {

						if (vo.getRegistrationDetails().getTrIssueDate() != null
								&& regServiceDTO.getRegistrationDetails().getTrGeneratedDate() == null) {
							regServiceDTO.getRegistrationDetails().setTrGeneratedDate(DateConverters
									.convertLocalDateToLocalDateTime(vo.getRegistrationDetails().getTrIssueDate()));
						}
						penality = otherStateNeedToPayPenality(
								regServiceDTO.getRegistrationDetails().getTrGeneratedDate().toLocalDate(), totalLifeTax,
								vcr, isVehicleSized, sizedDate);
						long tax = roundUpperTen(totalLifeTax);
						return Pair.of(tax, penality);
					}
				} else {
					if ((regServiceDTO.getRegistrationDetails().getClassOfVehicle()
							.equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
							|| regServiceDTO.getRegistrationDetails().getClassOfVehicle()
									.equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode()))
							&& regServiceDTO.isMviDone()) {

						if (list.stream()
								.anyMatch(type -> type.getFuelType().stream().anyMatch(fuel -> fuel.equalsIgnoreCase(
										regServiceDTO.getRegistrationDetails().getVahanDetails().getFuelDesc())))) {
							totalLifeTax = batteryDiscount(
									regServiceDTO.getRegistrationDetails().getInvoiceDetails().getInvoiceValue(),
									totalLifeTax, Percent, list,
									regServiceDTO.getRegistrationDetails().getVahanDetails().getFuelDesc());

							long tax = roundUpperTen(totalLifeTax);
							return Pair.of(tax, 0d);

						} else {

							if (vo.getRegistrationDetails().getTrIssueDate() != null
									&& regServiceDTO.getRegistrationDetails().getTrGeneratedDate() == null) {
								regServiceDTO.getRegistrationDetails().setTrGeneratedDate(DateConverters
										.convertLocalDateToLocalDateTime(vo.getRegistrationDetails().getTrIssueDate()));
							}

							long tax = roundUpperTen(totalLifeTax);
							return Pair.of(tax, 0d);
						}
					}
				}
			} else {
				LocalDate entryDate = getEarlerDate(regServiceDTO.getnOCDetails().getDateOfEntry(),
						regServiceDTO.getnOCDetails().getIssueDate());
				penality = otherStateNeedToPayPenality(entryDate, totalLifeTax, vcr, isVehicleSized, sizedDate);
				long tax = roundUpperTen(totalLifeTax);
				return Pair.of(tax, penality);

			}

		}
		// return ;
		return Pair.of(0L, penality);

	}

	private Double otherStateNeedToPayPenality(LocalDate otherStateDate, Double tax, boolean vcr,
			boolean isVehicleSized, LocalDate sizedDate) {
		Double penality = 0d;
		/*
		 * Pair<Integer, Integer> trMonthpositionInQuater =
		 * getMonthposition(otherStateDate);
		 */
		/*
		 * LocalDate firstQuaterUpTo =
		 * calculateTaxUpTo(trMonthpositionInQuater.getFirst(),
		 * trMonthpositionInQuater.getSecond());
		 */
		// Pair<Integer, Integer> toDayMonthpositionInQuater =
		// getMonthposition(LocalDate.now());
		if (otherStateDate.getMonthValue() == LocalDate.now().getMonthValue()
				&& otherStateDate.getYear() == LocalDate.now().getYear()) {
			// return false;
		} else {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
			String date1 = "01-" + String.valueOf(LocalDate.now().getMonthValue()) + "-"
					+ String.valueOf(LocalDate.now().getYear());
			LocalDate localDate = LocalDate.parse(date1, formatter);
			Long totalMonths = ChronoUnit.MONTHS.between(otherStateDate, localDate);
			totalMonths = totalMonths + 1;
			penality = tax * 1 / 100;
			if (vcr) {
				penality = tax * 2 / 100;
			}
			if (isVehicleSized) {
				String sizedStartDate = "01-" + String.valueOf(sizedDate.getMonthValue()) + "-"
						+ String.valueOf(sizedDate.getYear());
				localDate = LocalDate.parse(sizedStartDate, formatter);
				totalMonths = ChronoUnit.MONTHS.between(otherStateDate, localDate);
				totalMonths = totalMonths + 1;
				if (totalMonths <= 0) {
					totalMonths = 0l;
				}
			}
			penality = penality * totalMonths;
		}
		if (penality > tax) {
			penality = tax;
		}
		return penality;

	}

	private Double batteryDiscount(Double invoiceValue, Double totalTax, Float Percent,
			List<MasterTaxFuelTypeExcemptionDTO> list, String fuelDesc) {

		MasterTaxFuelTypeExcemptionDTO dto = list.stream().findFirst().get();
		Float discount = (Percent / 12f) * dto.getNoOfYears().get(fuelDesc);
		return ((invoiceValue * discount) / 100f);
	}

	public LocalDate lifTaxValidityCal() {

		return LocalDate.now().minusDays(1).plusYears(12);

	}

	public LocalDate lifTaxValidityCalForRegVeh(LocalDate prGeneratedDate) {

		return prGeneratedDate.minusDays(1).plusYears(12);

	}

	@Override
	public Double getOldCovTax(String cov, String seatingCapacity, Integer ulw, Integer gvw, String stateCode,
			String permitcode, String routeCode) {
		Double totalQuarterlyTax = 0d;
		Optional<MasterTaxBased> taxCalBasedOn = masterTaxBasedDAO.findByCovcode(cov);
		if (!taxCalBasedOn.isPresent()) {
			logger.error("No record found in master_taxbased for: " + cov);
			// throw error message
			throw new BadRequestException("No record found in master_taxbased for: " + cov);
		}
		if (taxCalBasedOn.get().getBasedon().equalsIgnoreCase(ulwCode)) {
			Optional<MasterTax> OptionalTax = taxTypeDAO
					.findByCovcodeAndToulwGreaterThanEqualAndFromulwLessThanEqualAndStatecodeAndPermitcodeAndStatus(cov, ulw,
							ulw, stateCode, permitcode,regStatus);

			if (!OptionalTax.isPresent()) {
				logger.error("No record found in master_tax for: " + cov + "and ULW: " + ulw);
				// throw error message
				throw new BadRequestException("No record found in master_tax for: " + cov + "and ULW: " + ulw);
			}

			totalQuarterlyTax = quarterlyTaxCalculation(OptionalTax, taxCalBasedOn.get().getBasedon(), seatingCapacity,
					cov, ulw, gvw, permitcode);
			return totalQuarterlyTax;
		} else if (taxCalBasedOn.get().getBasedon().equalsIgnoreCase(rlwCode)) {
			Optional<MasterTax> OptionalTax = taxTypeDAO
					.findByCovcodeAndTorlwGreaterThanEqualAndFromrlwLessThanEqualAndStatecodeAndPermitcodeAndStatus(cov, gvw,
							gvw, stateCode, permitcode,regStatus);
			if (!OptionalTax.isPresent()) {
				logger.error("No record found in master_tax for: " + cov + "and rLW: " + gvw);
				// throw error message
				throw new BadRequestException("No record found in master_tax for: " + cov + "and rLW: " + gvw);
			}

			totalQuarterlyTax = quarterlyTaxCalculation(OptionalTax, taxCalBasedOn.get().getBasedon(), seatingCapacity,
					cov, ulw, gvw, permitcode);

		} else if (taxCalBasedOn.get().getBasedon().equalsIgnoreCase(seatingCapacityCode)) {

			Optional<MasterTax> OptionalTax = Optional.empty();
			if (StringUtils.isNoneBlank(routeCode)) {
				OptionalTax = taxTypeDAO
						.findByCovcodeAndSeattoGreaterThanEqualAndSeatfromLessThanEqualAndStatecodeAndPermitcodeAndServTypeAndStatus(
								cov, Integer.parseInt(seatingCapacity), Integer.parseInt(seatingCapacity), stateCode,
								permitcode, routeCode,regStatus);
			} else {
				OptionalTax = taxTypeDAO
						.findByCovcodeAndSeattoGreaterThanEqualAndSeatfromLessThanEqualAndStatecodeAndPermitcodeAndStatus(cov,
								Integer.parseInt(seatingCapacity), Integer.parseInt(seatingCapacity), stateCode,
								permitcode,regStatus);
			}
			if (!OptionalTax.isPresent()) {
				// throw error message
				throw new BadRequestException(
						"No record found in master_tax for: " + cov + "and seatingCapacity: " + seatingCapacity);
			}

			totalQuarterlyTax = quarterlyTaxCalculation(OptionalTax, taxCalBasedOn.get().getBasedon(), seatingCapacity,
					cov, ulw, gvw, permitcode);
		}
		Optional<MasterTaxExcemptionsDTO> excemptionPercentage = masterTaxExcemptionsDAO.findByKeyvalue(cov);
		if (excemptionPercentage.isPresent()
				&& !excemptionPercentage.get().getValuetype().equalsIgnoreCase(seatingCapacityCode)) {
			// check percentage discount
			double discount = (totalQuarterlyTax * excemptionPercentage.get().getTaxvalue()) / 100;
			totalQuarterlyTax = totalQuarterlyTax - discount;
		}
		return totalQuarterlyTax;
	}

	private Double getOldQuaterTax(RegistrationDetailsDTO stagingRegDetails, RegServiceDTO regServiceDTO,
			boolean isApplicationFromMvi, StagingRegistrationDetailsDTO stagingRegistrationDetails,
			boolean isChassesApplication, boolean isOtherState) {
		Double OldQuaterTax = null;
		String cov = null;
		if (isChassesApplication) {
			OldQuaterTax = getOldCovTax(stagingRegistrationDetails.getClassOfVehicle(),
					stagingRegistrationDetails.getVahanDetails().getSeatingCapacity(),
					stagingRegistrationDetails.getVahanDetails().getUnladenWeight(),
					stagingRegistrationDetails.getVahanDetails().getGvw(), stateCode, permitcode, null);
			cov = stagingRegistrationDetails.getClassOfVehicle();
		} else if (isApplicationFromMvi) {
			OldQuaterTax = getOldCovTax(regServiceDTO.getRegistrationDetails().getVehicleDetails().getClassOfVehicle(),
					regServiceDTO.getRegistrationDetails().getVahanDetails().getSeatingCapacity(),
					regServiceDTO.getRegistrationDetails().getVahanDetails().getUnladenWeight(),
					regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw(), stateCode, permitcode, null);
			cov = regServiceDTO.getRegistrationDetails().getVehicleDetails().getClassOfVehicle();
		} else if (isOtherState) {
			if (regServiceDTO != null
					&& (regServiceDTO.getRegistrationDetails().getClassOfVehicle()
							.equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
							|| regServiceDTO.getRegistrationDetails().getClassOfVehicle()
									.equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode()))
					&& regServiceDTO.isMviDone()) {
				OldQuaterTax = getOldCovTax(
						regServiceDTO.getRegistrationDetails().getVehicleDetails().getClassOfVehicle(),
						regServiceDTO.getRegistrationDetails().getVahanDetails().getSeatingCapacity(),
						regServiceDTO.getRegistrationDetails().getVahanDetails().getUnladenWeight(),
						regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw(), stateCode, permitcode,
						null);
				cov = regServiceDTO.getRegistrationDetails().getVehicleDetails().getClassOfVehicle();
			}

		} else {
			cov = stagingRegDetails.getClassOfVehicle()!=null?stagingRegDetails.getClassOfVehicle():
				stagingRegDetails.getVehicleDetails().getClassOfVehicle();
			OldQuaterTax = getOldCovTax(cov,
					stagingRegDetails.getVahanDetails().getSeatingCapacity(),
					stagingRegDetails.getVahanDetails().getUnladenWeight(), stagingRegDetails.getVahanDetails().getGvw(),
					stateCode, permitcode, null);
			//cov = stagingRegDetails.getVehicleDetails().getClassOfVehicle();
		}

		return OldQuaterTax;
	}

	private Double quarterlyTaxCalculation(Optional<MasterTax> OptionalTax, String taxBasedon, String seatingCapacity,
			String cov, Integer ulw, Integer gvw, String permitcode) {

		if (taxBasedon.equalsIgnoreCase(seatingCapacityCode)) {

			Double quatertax = null;
			Float tax = (OptionalTax.get().getTaxamount() * (Integer.parseInt(seatingCapacity) - 1));
			if ((OptionalTax.get().getCovcode().equalsIgnoreCase(ClassOfVehicleEnum.COCT.getCovCode())
					|| OptionalTax.get().getCovcode().equalsIgnoreCase(ClassOfVehicleEnum.TOVT.getCovCode()))
					&& StringUtils.isNoneBlank(permitcode)
					&& permitcode.equalsIgnoreCase(PermitsEnum.PermitCodes.AITP.getPermitCode())) {
				tax = (OptionalTax.get().getTaxamount() * (Integer.parseInt(seatingCapacity) - 1));
			}

			quatertax = tax.doubleValue();
			Optional<MasterTaxExcemptionsDTO> optionalTaxExcemption = getSeatingCapacityExcemptionsTax(cov,
					seatingCapacity, permitcode);
			if (optionalTaxExcemption.isPresent()
					&& optionalTaxExcemption.get().getValuetype().equalsIgnoreCase(seatingCapacityCode)) {

				quatertax = optionalTaxExcemption.get().getTaxvalue().doubleValue();

			}
			return quatertax;
		} else if (OptionalTax.get().getIncrementalweight() == 0) {

			Double quatertax = OptionalTax.get().getTaxamount().doubleValue();
			return quatertax;

		} else {
			if (taxBasedon.equalsIgnoreCase(ulwCode)) {
				Float weight = (float) ((ulw - ((OptionalTax.get().getFromulw() - 1)))
						/ OptionalTax.get().getIncrementalweight().doubleValue());
				Float result = (float) ((Math.ceil(weight.doubleValue()) * OptionalTax.get().getIncrementalamount())
						+ OptionalTax.get().getTaxamount());
				Double quatertax = result.doubleValue();
				return quatertax;
			} else if (taxBasedon.equalsIgnoreCase(rlwCode)) {
				Double weight = (double) ((gvw - ((OptionalTax.get().getFromrlw() - 1)))
						/ OptionalTax.get().getIncrementalweight().doubleValue());
				Double result = ((Math.ceil(weight) * OptionalTax.get().getIncrementalamount())
						+ OptionalTax.get().getTaxamount());
				Double quatertax = result.doubleValue();
				return quatertax;
			}
		}
		return null;

	}

	private TaxHelper returnTaxDetails(String taxType, Long tax, Double penality, LocalDate taxTill,
			Long reoundTaxArrears, Double penalityArrears, TaxTypeEnum.TaxPayType payTaxType, String permitType,
			Double quarterTax,LocalDate lastTaxPaidUpTo) {
		TaxHelper taxHelper = new TaxHelper();
		taxHelper.setTaxName(taxType);
		taxHelper.setValidityTo(taxTill);
		taxHelper.setTaxAmountForPayments(tax);
		if (penality != null) {
			taxHelper.setPenalty(roundUpperTen(penality));
		}
		if (penality != null) {
			taxHelper.setPenaltyArrears(roundUpperTen(penalityArrears));
		}
		if (penalityArrears != null) {
			taxHelper.setTaxArrearsRound(reoundTaxArrears);
		}
		taxHelper.setTaxPayType(payTaxType);
		taxHelper.setPermitType(permitType);
		taxHelper.setTax(quarterTax);
		taxHelper.setLastTaxPaidUpTo(lastTaxPaidUpTo);
		return taxHelper;

	}

	private TaxHelper returnTaxForNewGo(String taxType, Long tax, Double penality, LocalDate taxTill,
			Long reoundTaxArrears, Double penalityArrears, TaxTypeEnum.TaxPayType payTaxType, String permitType,
			Long quaterTaxNewGotax) {
		TaxHelper taxHelper = new TaxHelper();
		taxHelper.setTaxName(taxType);
		taxHelper.setValidityTo(taxTill);
		taxHelper.setTaxAmountForPayments(tax);
		if (penality != null) {
			taxHelper.setPenalty(roundUpperTen(penality));
		}
		if (penality != null) {
			taxHelper.setPenaltyArrears(roundUpperTen(penalityArrears));
		}
		if (penalityArrears != null) {
			taxHelper.setTaxArrearsRound(reoundTaxArrears);
		}
		taxHelper.setTaxPayType(payTaxType);
		taxHelper.setPermitType(permitType);
		if (quaterTaxNewGotax != null) {
			taxHelper.setQuaterTaxForNewGo(quaterTaxNewGotax);
		}
		return taxHelper;

	}

	private Integer getGvwWeight(String applicationNo, Integer gvw, boolean voluntaryTax) {
		Integer rlw = null;
		if (voluntaryTax) {
			return gvw;
		}
		Optional<AlterationDTO> alterDetails = alterationDao.findByApplicationNo(applicationNo);
		if (!alterDetails.isPresent()) {
			throw new BadRequestException("No record found in alter collection for: " + applicationNo);
		}

		if (ClassOfVehicleEnum.ARVT.getCovCode().equalsIgnoreCase(alterDetails.get().getCov())) {

			if (alterDetails.get().getTrailers().isEmpty()) {
				throw new BadRequestException(
						"Trailers Details not found in Alteration collection for(ARVT) : " + applicationNo);
			}
			Integer gtw = alterDetails.get().getTrailers().stream().findFirst().get().getGtw();
			for (TrailerChassisDetailsDTO trailerDetails : alterDetails.get().getTrailers()) {
				if (trailerDetails.getGtw() > gtw) {
					gtw = trailerDetails.getGtw();
				}
			}
			rlw = gvw + gtw;
			return rlw;
		} else {
			rlw = gvw;
			return rlw;
		}

	}

	@Override
	public double calculateAgeOfTheVehicle(LocalDate localDateTime, LocalDate entryDate) {

		double yeare = localDateTime.until(entryDate, ChronoUnit.DAYS) / 365.2425f;
		yeare = Math.abs(yeare);
		yeare = Math.ceil(yeare);
		return yeare;

	}

	private LocalDate getEarlerDate(LocalDate dateOfEnter, LocalDate nocIssueDate) {
		LocalDate entryDate;
		if (!dateOfEnter.isAfter(nocIssueDate)) {
			entryDate = dateOfEnter;

		} else {
			entryDate = nocIssueDate;
		}
		return entryDate;
	}

	@Override
	public OtherStateApplictionType getOtherStateVehicleStatus(RegServiceVO regService) {
		OtherStateApplictionType applicationType = null;
		if (!regService.getRegistrationDetails().isRegVehicleWithPR()
				&& !regService.getRegistrationDetails().isRegVehicleWithTR()) {
			// application no
			applicationType = OtherStateApplictionType.ApplicationNO;
		} else if (regService.getRegistrationDetails().isRegVehicleWithTR()
				&& !regService.getRegistrationDetails().isRegVehicleWithPR()) {
			// TR no
			applicationType = OtherStateApplictionType.TrNo;
		} else {
			// PrNO
			applicationType = OtherStateApplictionType.PrNo;
		}
		return applicationType;
	}

	@Override
	public TaxHelper greenTaxCalculation(String applicationNo, List<ServiceEnum> serviceEnum,List<String> listOfVcrs) {
		if (serviceEnum != null && !serviceEnum.isEmpty()) {
			if (serviceEnum.stream().anyMatch(type -> type.equals(ServiceEnum.BILLATERALTAX))
					|| serviceEnum.stream().anyMatch(type -> type.equals(ServiceEnum.FEECORRECTION))) {
				return null;
			}
		}
		if ( serviceEnum.stream().anyMatch(type -> type.equals(ServiceEnum.VCR))) {
			List<VcrFinalServiceDTO> vcrList = getVcrDocs(listOfVcrs);
			vcrList.sort((p1, p2) -> p2.getVcr().getDateOfCheck().compareTo(p1.getVcr().getDateOfCheck()));
			VcrFinalServiceDTO singleVcr = vcrList.stream().findFirst().get();
			if (!StringUtils.isNoneBlank(singleVcr.getRegistration().getRegApplicationNo())||singleVcr.getRegistration().isUnregisteredVehicle()) {
				return null;
					}
			applicationNo=singleVcr.getRegistration().getRegApplicationNo();
			
		}
		Optional<RegistrationDetailsDTO> regOptional = registrationDetailDAO.findByApplicationNo(applicationNo);
		if (!regOptional.isPresent()) {
			logger.error("No record found in Reg Service for:[{}] " + applicationNo);
			throw new BadRequestException("No record found in Reg Service for:[{}] " + applicationNo);
		}
		RegistrationDetailsDTO regDTO = regOptional.get();
		Optional<MasterGreenTax> masterGreenTax = masterGreenTaxDAO.findByCovcode(regDTO.getClassOfVehicle());
		if (!masterGreenTax.isPresent()) {
			logger.error("No record found in MasterGreenTax for:[{}] " + applicationNo);
			throw new BadRequestException("No record found in MasterGreenTax for:[{}] " + applicationNo);
		}
		// TODO fuel exception
		Optional<MasterGreenTaxFuelexcemption> fuelException = masterGreenTaxFuelexcemptionDAO
				.findByFuelTypeIn(regDTO.getVahanDetails().getFuelDesc());
		if (fuelException.isPresent()) {
			return returnTaxDetails(ServiceCodeEnum.GREEN_TAX.getCode(), 0l, 0d, LocalDate.now(), 0l, 0d,
					TaxTypeEnum.TaxPayType.REG, "", 0d,null);
		}

		Pair<Long, LocalDate> taxAndValid = Pair.of(0l, LocalDate.now());
		if (regDTO.getPrGeneratedDate() == null && regDTO.getRegistrationValidity() != null
				&& regDTO.getRegistrationValidity().getPrGeneratedDate() != null) {
			regDTO.setPrGeneratedDate(regDTO.getRegistrationValidity().getPrGeneratedDate().atStartOfDay());
		}

		if (LocalDateTime.now()
				.isAfter(regDTO.getPrGeneratedDate().plusYears(masterGreenTax.get().getAgeOfVehicle().longValue()))) {
			TaxHelper taxHelper = getIsGreenTaxPending(applicationNo,
					Arrays.asList(ServiceCodeEnum.GREEN_TAX.getCode()), LocalDate.now(), regOptional.get().getPrNo());

			if (taxHelper == null || taxHelper.getTax() == null) {
				taxAndValid = finalGreenTaxCal(masterGreenTax, regDTO.getPrGeneratedDate()
						.plusYears(masterGreenTax.get().getAgeOfVehicle().longValue()).toLocalDate());
			} else if (taxHelper.isAnypendingQuaters()) {
				// TaxHelper taxHelper = new TaxHelper();
				LocalDate taxUpTo = getGreenTaxDetails(taxHelper, applicationNo);

				taxAndValid = finalGreenTaxCal(masterGreenTax, taxUpTo);

			}
			return returnTaxDetails(ServiceCodeEnum.GREEN_TAX.getCode(), taxAndValid.getFirst(), 0d,
					taxAndValid.getSecond(), 0l, 0d, TaxTypeEnum.TaxPayType.REG, "", 0d,null);
		} else if (serviceEnum != null && !serviceEnum.isEmpty() && serviceEnum.contains(ServiceEnum.RENEWAL)) {

			return returnTaxDetails(ServiceCodeEnum.GREEN_TAX.getCode(),
					masterGreenTax.get().getTaxamount().longValue(), 0d,
					regDTO.getRegistrationValidity().getRegistrationValidity().toLocalDate().minusDays(1).plusYears(
							masterGreenTax.get().getAgeOfIncrement()),
					0l, 0d, TaxTypeEnum.TaxPayType.REG, "", 0d,null);
		}
		return null;

	}

	private Pair<Long, LocalDate> finalGreenTaxCal(Optional<MasterGreenTax> masterGreenTax, LocalDate taxUpTo) {
		LocalDate date = taxUpTo;
		int incrementCount = 0;
		boolean status = Boolean.TRUE;
		do {

			date = date.plusYears(masterGreenTax.get().getAgeOfIncrement());
			if (date.isAfter(LocalDate.now())) {
				status = Boolean.FALSE;
			} else {
				incrementCount = incrementCount + 1;
			}
		} while (status);

		incrementCount = incrementCount + 1;

		Long taxAmount = masterGreenTax.get().getTaxamount().longValue() * incrementCount;
		return Pair.of(taxAmount, date);

	}

	private LocalDate getGreenTaxDetails(TaxHelper taxHelper, String applicationNo) {
		// TaxHelper taxHelper = new TaxHelper();
		List<RegServiceDTO> listofRegService = regServiceDAO.findByRegistrationDetailsApplicationNoAndServiceIds(
				applicationNo, ServiceEnum.ALTERATIONOFVEHICLE.getId());
		if (listofRegService.isEmpty()) {
			return taxHelper.getValidityTo();
		}
		listofRegService.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		// RegServiceDTO regCitizenDto = listofRegService.stream().findFirst().get();

		for (RegServiceDTO dto : listofRegService) {
			if (dto.getAlterationDetails().getVehicleTypeFrom() != null
					&& dto.getAlterationDetails().getVehicleTypeTo() != null) {
				if (dto.getAlterationDetails().getVehicleTypeFrom() != dto.getAlterationDetails().getVehicleTypeTo()) {
					if (dto.getSlotDetails().getSlotDate().isAfter(taxHelper.getValidityTo())) {
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
						String date1 = String.valueOf(taxHelper.getValidityTo().getDayOfMonth()) + "-"
								+ String.valueOf(taxHelper.getValidityTo().getMonthValue()) + "-"
								+ String.valueOf(dto.getSlotDetails().getSlotDate().getYear());
						LocalDate localDate = LocalDate.parse(date1, formatter);
						return localDate;
					} else {
						return taxHelper.getValidityTo();
					}

				}
			}
		}
		return taxHelper.getValidityTo();

	}

	private TaxHelper getIsGreenTaxPending(String applicationNo, List<String> taxType, LocalDate currentTaxTill,
			String prNo) {

		List<TaxDetailsDTO> taxDetailsList = getGreenTaxDetails(applicationNo, taxType, prNo);
		if (taxDetailsList == null || taxDetailsList.isEmpty()) {
			return this.addTaxDetails(null, null, null, Boolean.TRUE, null, Boolean.FALSE);
		}
		taxDetailsList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		TaxDetailsDTO dto = taxDetailsList.stream().findFirst().get();
		if (dto.getTaxDetails() == null) {
			logger.error("TaxDetails not found: [{}]", applicationNo);
			throw new BadRequestException("TaxDetails not found:" + applicationNo);
		}
		for (Map<String, TaxComponentDTO> map : dto.getTaxDetails()) {

			for (Entry<String, TaxComponentDTO> entry : map.entrySet()) {
				if (taxType.stream().anyMatch(key -> key.equalsIgnoreCase(entry.getKey()))) {

					if (entry.getValue().getValidityTo().isBefore(currentTaxTill)) {
						return this.addTaxDetails(entry.getKey(), entry.getValue().getAmount(),
								entry.getValue().getValidityTo(), Boolean.TRUE, entry.getValue().getPaidDate(),
								dto.getTaxPaidThroughVcr());

					} else if (entry.getValue().getValidityTo().equals(currentTaxTill)
							|| entry.getValue().getValidityTo().isAfter(currentTaxTill)) {
						return this.addTaxDetails(entry.getKey(), entry.getValue().getAmount(),
								entry.getValue().getValidityTo(), Boolean.FALSE, entry.getValue().getPaidDate(),
								dto.getTaxPaidThroughVcr());
					}

				}
			}
		}
		return null;
	}

	@Override
	public List<TaxDetailsDTO> taxDetails(String applicationNo, List<String> taxType, String prNo) {
		List<TaxDetailsDTO> listOfTaxDetails = new ArrayList<>();
		List<TaxDetailsDTO> taxDetailsList = new ArrayList<>();
		if (taxType.contains(ServiceCodeEnum.CESS_FEE.getCode())) {
			taxDetailsList = taxDetailsDAO.findFirst50ByApplicationNoOrderByCreatedDateDesc(applicationNo);
		} else {
			taxDetailsList = taxDetailsDAO
					.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(applicationNo, taxType);
		}
		if (taxDetailsList.isEmpty()) {
			logger.error("TaxDetails not found: [{}]", applicationNo);
			throw new BadRequestException("TaxDetails not found:" + applicationNo);
		}
		registrationService.updatePaidDateAsCreatedDate(taxDetailsList);
		taxDetailsList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		for (String type : taxType) {
			for (TaxDetailsDTO taxDetails : taxDetailsList) {
				if (taxDetails.getTaxDetails() == null) {
					logger.error("TaxDetails map not found: [{}]", applicationNo);
					throw new BadRequestException("TaxDetails map not found:" + applicationNo);
				}
				if (taxDetails.getTaxDetails().stream().anyMatch(key -> key.keySet().contains(type))) {
					listOfTaxDetails.add(taxDetails);
					if (taxDetails.getTaxPaidThroughVcr() != null && taxDetails.getTaxPaidThroughVcr()) {
						continue;
					}
					break;
				}
			}
		}
		taxDetailsList.clear();
		return listOfTaxDetails;
	}

	private List<TaxDetailsDTO> getGreenTaxDetails(String applicationNo, List<String> taxType, String prNo) {
		List<TaxDetailsDTO> listOfTaxDetails = new ArrayList<>();
		// TODO need to modify DB query
		List<TaxDetailsDTO> taxDetailsList = taxDetailsDAO
				.findFirst50ByApplicationNoOrderByCreatedDateDesc(applicationNo);
		if (taxDetailsList.isEmpty()) {
			logger.error("TaxDetails not found: [{}]", applicationNo);
			throw new BadRequestException("TaxDetails not found:" + applicationNo);
		}
		registrationService.updatePaidDateAsCreatedDate(taxDetailsList);
		taxDetailsList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		for (String type : taxType) {
			for (TaxDetailsDTO taxDetails : taxDetailsList) {
				if (taxDetails.getTaxDetails() == null) {
					logger.error("TaxDetails not found: [{}]", applicationNo);
					throw new BadRequestException("TaxDetails not found:" + applicationNo);
				}
				if (taxDetails.getTaxDetails().stream().anyMatch(key -> key.keySet().contains(type))) {
					listOfTaxDetails.add(taxDetails);
					break;
				}
			}
		}
		taxDetailsList.clear();
		return listOfTaxDetails;
	}

	private TaxHelper getOldTaxDetailsForAlterVehicle(String applicationNo, Double OptionalTax, Integer valu,
			RegServiceDTO regServiceDTO, String taxType, String prNo, String permitTypeCode, String routeCode) {
		Double penalityTax = 0d;
		Double quaterTax = 0d;
		List<String> listTaxType = this.taxTypes();
		String oldPermitTypeCode = this.permitcode;
		permitTypeCode = this.permitcode;
		routeCode = StringUtils.EMPTY;
		Pair<String, String> permitTypeAndRoutType = Pair.of(permitTypeCode, routeCode);
		listTaxType.add(ServiceCodeEnum.LIFE_TAX.getCode());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
		String date1 = "01-" + String.valueOf(LocalDate.now().getMonthValue()) + "-"
				+ String.valueOf(LocalDate.now().getYear());
		LocalDate localDate = LocalDate.parse(date1, formatter);
		TaxHelper oldTaxDetails = getIsGreenTaxPending(applicationNo, listTaxType, LocalDate.now(), prNo);
		String  classOfvehicle= regServiceDTO.getRegistrationDetails().getClassOfVehicle();
		if (oldTaxDetails.isAnypendingQuaters()
				&& !oldTaxDetails.getTaxName().equalsIgnoreCase(ServiceCodeEnum.LIFE_TAX.getCode())) {
			
				//classOfvehicle = regServiceDTO.getAlterationDetails().getCov();
				
			
				if (regServiceDTO.getAlterationDetails() != null && regServiceDTO.getAlterationDetails().getCov() != null
						&& (classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())
								|| (classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode()) && 
										regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw() <= 3000)
								|| classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())
								|| classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TRTT.getCovCode())
								|| classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TRCN.getCovCode()))
						&& !(regServiceDTO.getAlterationDetails().getCov().equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())
								|| (regServiceDTO.getAlterationDetails().getCov().equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode()) && 
										regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw() <= 3000)
								|| regServiceDTO.getAlterationDetails().getCov().equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())
								|| regServiceDTO.getAlterationDetails().getCov().equalsIgnoreCase(ClassOfVehicleEnum.TRTT.getCovCode())
								|| regServiceDTO.getAlterationDetails().getCov().equalsIgnoreCase(ClassOfVehicleEnum.TRCN.getCovCode()))) {
					LocalDate mviApprovedDate = getMviApprovedDate(regServiceDTO);
					return this.getOtherStatePenalityAndTax(OptionalTax, false, mviApprovedDate, false);
				}else if(regServiceDTO.getAlterationDetails() != null && regServiceDTO.getAlterationDetails().getCov() != null
						&& !(classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())
								|| (classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode()) && 
										regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw() <= 3000)
								|| classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())
								|| classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TRTT.getCovCode())
								|| classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TRCN.getCovCode()))
						&& (regServiceDTO.getAlterationDetails().getCov().equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())
								|| (regServiceDTO.getAlterationDetails().getCov().equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode()) && 
										regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw() <= 3000)
								|| regServiceDTO.getAlterationDetails().getCov().equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())
								|| regServiceDTO.getAlterationDetails().getCov().equalsIgnoreCase(ClassOfVehicleEnum.TRTT.getCovCode())
								|| regServiceDTO.getAlterationDetails().getCov().equalsIgnoreCase(ClassOfVehicleEnum.TRCN.getCovCode()))){
					LocalDate mviApprovedDate = getMviApprovedDate(regServiceDTO);
				return this.getNonexemptionCovTax(regServiceDTO,OptionalTax, oldTaxDetails.getValidityTo(), mviApprovedDate);
					
				}else {
			Long totalMonthsForPenality = ChronoUnit.MONTHS.between(oldTaxDetails.getValidityTo(), localDate);
			totalMonthsForPenality = Math.abs(totalMonthsForPenality);
			totalMonthsForPenality = totalMonthsForPenality + 1;
			double Penalityquaters = totalMonthsForPenality / 3d;
			double PenalityQuatersRound = Math.ceil(totalMonthsForPenality / 3);
			PenalityQuatersRound = PenalityQuatersRound - 1;
			Pair<Double, Double> taxArrAndPenality = chassisPenalitTax(OptionalTax, (PenalityQuatersRound), false);
			if (valu == 0) {
				// quaterTax = OptionalTax;
			} else if (valu == 1) {
				penalityTax = ((OptionalTax * 25) / 100);

			} else {
				penalityTax = ((OptionalTax * 50) / 100);

			}
			// quaterTax = taxArrAndPenality.getFirst() + OptionalTax;
			// penalityTax = penalityTax + taxArrAndPenality.getSecond();
			// return Pair.of(quaterTax, penalityTax);
			setObjectAsNull(null, null, null, listTaxType, null);
			return returnTaxDetails(OptionalTax, taxArrAndPenality.getFirst(), penalityTax,
					taxArrAndPenality.getSecond(), 0d,oldTaxDetails.getValidityTo());
				}
		} else {
			if(regServiceDTO.getAlterationDetails() != null && regServiceDTO.getAlterationDetails().getCov() != null
					&& !(classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())
							|| (classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode()) && 
									regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw() <= 3000)
							|| classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())
							|| classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TRTT.getCovCode())
							|| classOfvehicle.equalsIgnoreCase(ClassOfVehicleEnum.TRCN.getCovCode()))
					&& (regServiceDTO.getAlterationDetails().getCov().equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())
							|| (regServiceDTO.getAlterationDetails().getCov().equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode()) && 
									regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw() <= 3000)
							|| regServiceDTO.getAlterationDetails().getCov().equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())
							|| regServiceDTO.getAlterationDetails().getCov().equalsIgnoreCase(ClassOfVehicleEnum.TRTT.getCovCode())
							|| regServiceDTO.getAlterationDetails().getCov().equalsIgnoreCase(ClassOfVehicleEnum.TRCN.getCovCode()))){
				return returnTaxDetails(quaterTax, 0d, 0d, 0d, 0d,this.validity(taxType));
			}
			if (!oldTaxDetails.getTaxName().equalsIgnoreCase(ServiceCodeEnum.LIFE_TAX.getCode())) {
				Optional<MasterTaxBased> taxCalBasedOn = masterTaxBasedDAO
						.findByCovcode(regServiceDTO.getRegistrationDetails().getClassOfVehicle());
				if (!taxCalBasedOn.isPresent()) {
					logger.error("No record found in master_taxbased for: "
							+ regServiceDTO.getRegistrationDetails().getClassOfVehicle());
					// throw error message
					throw new BadRequestException("No record found in master_taxbased for: "
							+ regServiceDTO.getRegistrationDetails().getClassOfVehicle());
				}
				permitTypeAndRoutType = getPermitCode(regServiceDTO.getRegistrationDetails());
				oldPermitTypeCode = permitTypeAndRoutType.getFirst();
				if (taxCalBasedOn.get().getBasedon().equalsIgnoreCase("S")) {

					permitTypeCode = permitTypeAndRoutType.getFirst();
					routeCode = permitTypeAndRoutType.getSecond();
					if (regServiceDTO.getRegistrationDetails().getClassOfVehicle()
							.equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())) {
						permitTypeCode = this.permitcode;
						routeCode = StringUtils.EMPTY;
					}
					Optional<PropertiesDTO> propertiesOptional = propertiesDAO
							.findByCovsInAndPermitCodeTrue(regServiceDTO.getRegistrationDetails().getClassOfVehicle());
					if (!propertiesOptional.isPresent()) {
						permitTypeCode = permitcode;
						routeCode = StringUtils.EMPTY;
					}
				}
				Long totalMonths = ChronoUnit.MONTHS.between(localDate, oldTaxDetails.getValidityTo());
				totalMonths = totalMonths + 1;
				Double OldQuaterTax = getOldCovTax(regServiceDTO.getRegistrationDetails().getClassOfVehicle(),
						regServiceDTO.getRegistrationDetails().getVahanDetails().getSeatingCapacity(),
						regServiceDTO.getRegistrationDetails().getVahanDetails().getUnladenWeight(),
						regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw(), stateCode, permitTypeCode,
						routeCode);
				Optional<PropertiesDTO> propertiesOptionalForObt = propertiesDAO
						.findByCovsInAndObtTaxTrue(regServiceDTO.getRegistrationDetails().getClassOfVehicle());
				if (propertiesOptionalForObt.isPresent()) {
					// Pair<String, String> permitCodeAndRouytCode =
					// this.getPermitCode(stagingRegDetails);
					if (oldPermitTypeCode.equalsIgnoreCase("INA")) {
						boolean flag = Boolean.TRUE;
						if (regServiceDTO.getRegistrationDetails().getClassOfVehicle()
								.equalsIgnoreCase(ClassOfVehicleEnum.COCT.getCovCode())) {
							List<RegServiceDTO> listOfRegService = regServiceDAO.findByprNoAndServiceIdsAndSourceIsNull(
									regServiceDTO.getRegistrationDetails().getPrNo(),
									ServiceEnum.ALTERATIONOFVEHICLE.getId());
							if (listOfRegService != null && !listOfRegService.isEmpty()) {
								listOfRegService.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
								RegServiceDTO dto = listOfRegService.stream().findFirst().get();
								if (dto.getTaxvalidity() != null
										&& dto.getApplicationStatus().equals(StatusRegistration.APPROVED)) {
									if (dto.getAlterationDetails() != null
											&& (StringUtils.isNoneBlank(dto.getAlterationDetails().getCov())
											&& dto.getAlterationDetails().getCov()
													.equalsIgnoreCase(ClassOfVehicleEnum.COCT.getCovCode()))) {
										if (validity(TaxTypeEnum.QuarterlyTax.getDesc()).equals(dto.getTaxvalidity())
												|| validity(TaxTypeEnum.QuarterlyTax.getDesc())
														.isBefore(dto.getTaxvalidity())) {
											flag = Boolean.FALSE;
										}
									}
								}
							}
						}
						if (flag) {
							OldQuaterTax = getOldCovTax(ClassOfVehicleEnum.OBT.getCovCode(),
									regServiceDTO.getRegistrationDetails().getVahanDetails().getSeatingCapacity(),
									regServiceDTO.getRegistrationDetails().getVahanDetails().getUnladenWeight(),
									regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw(), stateCode, "INA",
									null);
						}
					}
				}
				if (oldTaxDetails.getTaxPaidThroughVcr() != null && oldTaxDetails.getTaxPaidThroughVcr()) {
					OldQuaterTax = oldTaxDetails.getTax();
				}
				Double oldQuaterTaxPerMonth = OldQuaterTax / 3;
				Double newQuaterTaxPerMonth = OptionalTax / 3;
				Double newTax = newQuaterTaxPerMonth * totalMonths;
				Double oldTax = oldQuaterTaxPerMonth * totalMonths;
				newTax = newTax - oldTax;
				if (newTax < 1) {
					newTax = 0d;
				}
				taxType = oldTaxDetails.getTaxName();
				// return Pair.of(newTax, 0d);
				setObjectAsNull(null, null, null, listTaxType, null);
				return returnTaxDetails(newTax, 0d, 0d, 0d, 0d,oldTaxDetails.getValidityTo());
			} else {
				if (valu == 0) {
					quaterTax = OptionalTax;
				} else if (valu == 1) {
					quaterTax = (OptionalTax / 3) * 2;
				} else {
					quaterTax = (OptionalTax / 3) * 1;
				}
				// return Pair.of(quaterTax, 0d);
				setObjectAsNull(null, null, null, listTaxType, null);
				return returnTaxDetails(quaterTax, 0d, 0d, 0d, 0d,oldTaxDetails.getValidityTo());
			}
		}

	}

	private LocalDate getMviApprovedDate(RegServiceDTO regServiceDTO) {
		LocalDate mviApprovedDate = LocalDate.now();
		if(regServiceDTO.getAlterationDetails().getlUpdate()!=null) {
			mviApprovedDate = regServiceDTO.getAlterationDetails().getlUpdate().toLocalDate();
		}else {
			Optional<ActionDetails> actionDetailsOpt = regServiceDTO.getActionDetails().stream()
					.filter(p -> RoleEnum.MVI.getName().equals(p.getRole())).findFirst();
			if (!actionDetailsOpt.isPresent()) {
				logger.error("User role [{}] specific details not found in action detail", RoleEnum.MVI.getName());
				throw new BadRequestException("User role " + RoleEnum.MVI.getName() + " specific details not found in actiondetail");
			}
			if(actionDetailsOpt.get().getlUpdate()!=null) {
				mviApprovedDate = actionDetailsOpt.get().getlUpdate().toLocalDate();
			}
		}
		return mviApprovedDate;
	}

	private LocalDate getCessDetails(TaxHelper taxHelper, String applicationNo) {
		// TaxHelper taxHelper = new TaxHelper();
		List<RegServiceDTO> listofRegService = regServiceDAO.findByRegistrationDetailsApplicationNoAndServiceIds(
				applicationNo, ServiceEnum.ALTERATIONOFVEHICLE.getId());
		if (listofRegService.isEmpty()) {
			return taxHelper.getValidityTo();
		}
		listofRegService.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		// RegServiceDTO regCitizenDto = listofRegService.stream().findFirst().get();

		for (RegServiceDTO dto : listofRegService) {
			if (dto.getAlterationDetails().getVehicleTypeFrom() != null
					&& dto.getAlterationDetails().getVehicleTypeTo() != null) {
				if (dto.getAlterationDetails().getVehicleTypeFrom() == "N"
						&& dto.getAlterationDetails().getVehicleTypeTo() == "T") {
					if (dto.getSlotDetails().getSlotDate().isAfter(taxHelper.getValidityTo())) {
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
						String date1 = String.valueOf(taxHelper.getValidityTo().getDayOfMonth()) + "-"
								+ String.valueOf(taxHelper.getValidityTo().getMonthValue()) + "-"
								+ String.valueOf(dto.getSlotDetails().getSlotDate().getYear());
						LocalDate localDate = LocalDate.parse(date1, formatter);
						return localDate;
					} else {
						return taxHelper.getValidityTo();
					}

				}
			}
		}
		return null;

	}

	private boolean checkIsToPayLifeTaxBefore(String applicationNo, AlterationDTO altDto, String prNo) {
		List<TaxDetailsDTO> listTax = taxDetailsDAO.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(
				applicationNo, Arrays.asList(TaxTypeEnum.LifeTax.getDesc()));
		if (listTax.isEmpty()) {
			return Boolean.TRUE;

		}
		if (altDto.getFromCov() != null && altDto.getCov() != null
				&& altDto.getFromCov().equalsIgnoreCase(ClassOfVehicleEnum.IVCN.getCovCode())) {
			boolean flag = Boolean.FALSE;
			for (TaxDetailsDTO dto : listTax) {
				if (getListofTwoWeelerNotTransCovs().stream()
						.anyMatch(covs -> covs.equalsIgnoreCase(dto.getClassOfVehicle()))) {
					flag = Boolean.TRUE;
				}
			}
			if (!flag) {
				listTax.clear();
				return Boolean.TRUE;// true;
			}
		}
		for (TaxDetailsDTO dto : listTax) {
			for (Map<String, TaxComponentDTO> map : dto.getTaxDetails()) {
				for (Entry<String, TaxComponentDTO> entry : map.entrySet()) {
					if (TaxTypeEnum.LifeTax.getDesc().equalsIgnoreCase(entry.getKey())) {
						listTax.clear();
						return Boolean.FALSE;
					}
				}
			}
		}
		listTax.clear();
		return true;
	}

	public List<String> getListofTwoWeelerNotTransCovs() {
		List<String> list = new ArrayList<>();
		list.add(ClassOfVehicleEnum.MCYN.getCovCode());
		list.add(ClassOfVehicleEnum.MMCN.getCovCode());
		return list;
	}

	@Override
	public Integer getGvwWeightForCitizen(RegistrationDetailsDTO registrationDetails) {
		Integer rlw = null;

		if (ClassOfVehicleEnum.ARVT.getCovCode().equalsIgnoreCase(registrationDetails.getClassOfVehicle())) {
			Integer gtw = 0;
			if (registrationDetails.getVahanDetails() == null
					|| registrationDetails.getVahanDetails().getTrailerChassisDetailsDTO() == null) {
				// removed for migration data as per murthy sir input.
				// throw new BadRequestException("trailer details missed for ARVT : " +
				// registrationDetails.getPrNo());
			} else {
				gtw = registrationDetails.getVahanDetails().getTrailerChassisDetailsDTO().stream().findFirst().get()
						.getGtw();
				for (TrailerChassisDetailsDTO trailerDetails : registrationDetails.getVahanDetails()
						.getTrailerChassisDetailsDTO()) {
					if (trailerDetails.getGtw() > gtw) {
						gtw = trailerDetails.getGtw();
					}
				}
			}

			rlw = registrationDetails.getVahanDetails().getGvw() + gtw;
			return rlw;
		} else {
			if (registrationDetails.getVahanDetails() == null) {
				throw new BadRequestException("vehicle Details not found for: " + registrationDetails.getPrNo());
			}
			if (registrationDetails.getVahanDetails() != null
					&& registrationDetails.getVahanDetails().getGvw() == null) {
				throw new BadRequestException("vehicle GVW not found for: " + registrationDetails.getPrNo());
			}

			return registrationDetails.getVahanDetails().getGvw();
		}

	}

	private Pair<Double, Double> getcurrenttaxWithOutPenality(TaxHelper vcrDetails, Double newPermitTax,
			Double quaterTax, Double penality, Double OptionalTax, Integer valu) {
		if (!vcrDetails.isVcr()) {
			// With out VCR
			if (valu == 0) {
				Pair<Double, Double> pairOfTax = getTaxWithVcrWithOutPenality(quaterTax, penality, OptionalTax,
						newPermitTax, 3, 0);
				quaterTax = pairOfTax.getFirst();
				penality = pairOfTax.getSecond();
			} else if (valu == 1) {
				Pair<Double, Double> pairOfTax = getTaxWithVcrWithOutPenality(quaterTax, penality, OptionalTax,
						newPermitTax, 2, 25);
				quaterTax = pairOfTax.getFirst();
				penality = pairOfTax.getSecond();
			} else {
				Pair<Double, Double> pairOfTax = getTaxWithVcrWithOutPenality(quaterTax, penality, OptionalTax,
						newPermitTax, 1, 50);
				quaterTax = pairOfTax.getFirst();
				penality = pairOfTax.getSecond();
			}
		} else {
			// With VCR
			if (valu == 0) {
				Pair<Double, Double> pairOfTax = getTaxWithVcr(quaterTax, penality, OptionalTax, newPermitTax, 3, 0,
						vcrDetails);
				quaterTax = pairOfTax.getFirst();
				penality = pairOfTax.getSecond();
			} else if (valu == 1) {
				Pair<Double, Double> pairOfTax = getTaxWithVcr(quaterTax, penality, OptionalTax, newPermitTax, 2, 100,
						vcrDetails);
				quaterTax = pairOfTax.getFirst();
				penality = pairOfTax.getSecond();
			} else {
				Pair<Double, Double> pairOfTax = getTaxWithVcr(quaterTax, penality, OptionalTax, newPermitTax, 1, 200,
						vcrDetails);
				quaterTax = pairOfTax.getFirst();
				penality = pairOfTax.getSecond();
			}
		}
		return Pair.of(quaterTax, penality);
	}

	// Other state Green tax
	@Override
	public TaxHelper greenTaxCalculation(String applicationNo, List<ServiceEnum> serviceEnum, RegServiceDTO regDTO) {
		Optional<MasterGreenTax> masterGreenTax = masterGreenTaxDAO
				.findByCovcode(regDTO.getRegistrationDetails().getVahanDetails().getVehicleClass());
		if (!masterGreenTax.isPresent()) {
			logger.error("No record found in MasterGreenTax for:[{}] " + applicationNo);
			throw new BadRequestException("No record found in MasterGreenTax for:[{}] " + applicationNo);
		}
		// TODO fuel exception
		Optional<MasterGreenTaxFuelexcemption> fuelException = masterGreenTaxFuelexcemptionDAO
				.findByFuelTypeIn(regDTO.getRegistrationDetails().getVahanDetails().getFuelDesc());
		if (fuelException.isPresent()) {
			return returnTaxDetails(ServiceCodeEnum.GREEN_TAX.getCode(), 0l, 0d, LocalDate.now(), 0l, 0d,
					TaxTypeEnum.TaxPayType.REG, "", 0d,null);
		}

		Pair<Long, LocalDate> taxAndValid = Pair.of(0l, LocalDate.now());
		if (LocalDate.now().isAfter(regDTO.getRegistrationDetails().getPrIssueDate()
				.plusYears(masterGreenTax.get().getAgeOfVehicle().longValue()))) {
			TaxHelper taxHelper = null;
			// getIsGreenTaxPending(applicationNo,
			// Arrays.asList(ServiceCodeEnum.GREEN_TAX.getCode()), LocalDate.now());

			if (taxHelper == null || taxHelper.getTax() == null) {
				taxAndValid = finalGreenTaxCal(masterGreenTax, regDTO.getRegistrationDetails().getPrIssueDate()
						.plusYears(masterGreenTax.get().getAgeOfVehicle().longValue()));
			} else if (taxHelper.isAnypendingQuaters()) {
				// TaxHelper taxHelper = new TaxHelper();
				LocalDate taxUpTo = getGreenTaxDetails(taxHelper, applicationNo);

				taxAndValid = finalGreenTaxCal(masterGreenTax, taxUpTo);

			}
			return returnTaxDetails(ServiceCodeEnum.GREEN_TAX.getCode(), taxAndValid.getFirst(), 0d,
					taxAndValid.getSecond(), 0l, 0d, TaxTypeEnum.TaxPayType.REG, "", 0d,null);
		}
		return null;

	}

	private TaxHelper overRideTheTax(Double OptionalTax, Integer valu, RegistrationDetailsDTO stagingRegDetails,
			LocalDate currentTaxTill, RegServiceDTO regServiceDTO, boolean isApplicationFromMvi,
			StagingRegistrationDetailsDTO stagingRegistrationDetails, boolean isChassesApplication,
			String classOfvehicle, boolean isOtherState, String taxType, List<ServiceEnum> serviceEnum,
			String permitTypeCode, String routeCode, TaxHelper lastTaxTillDate, TaxHelper vcrDetails,
			TaxHelper currenTax, boolean isWeightAlt) {

		Optional<FinalTaxHelper> optionalPrDocument = finalTaxHelperDAO
				.findByPrNoInAndStatusIsTrue(stagingRegDetails.getPrNo());
		if (optionalPrDocument.isPresent()) {
			return this.finalOverRideTax(stagingRegDetails, optionalPrDocument.get(), OptionalTax, currentTaxTill,
					lastTaxTillDate, vcrDetails, currenTax, valu);
		} else {
			if (serviceEnum != null && serviceEnum.stream().anyMatch(id -> id.equals(ServiceEnum.TAXATION))) {
				if (stagingRegDetails != null && stagingRegDetails.isWeightAltDone() && isWeightAlt) {
					Optional<FinalTaxHelper> optionaDocument = finalTaxHelperDAO
							.findByPrNoInAndWeightAltIsTrue(stagingRegDetails.getPrNo());
					if (optionaDocument.isPresent()) {
						return this.finalOverRideTax(stagingRegDetails, optionaDocument.get(), OptionalTax,
								currentTaxTill, lastTaxTillDate, vcrDetails, currenTax, valu);
					}
				}
			}
			Integer districCode = null;
			if (stagingRegDetails.getApplicantDetails().getPresentAddress() == null
					|| stagingRegDetails.getApplicantDetails().getPresentAddress().getDistrict() == null
					|| stagingRegDetails.getApplicantDetails().getPresentAddress().getDistrict()
							.getDistricCode() == null) {
				aadharSeeding.updateDistirctDetails(stagingRegDetails.getApplicantDetails(),
						stagingRegDetails.getOfficeDetails());

			} else {
				districCode = stagingRegDetails.getApplicantDetails().getPresentAddress().getDistrict()
						.getDistricCode();
			}
			Optional<FinalTaxHelper> optionalDistrictDocument = finalTaxHelperDAO
					.findByDistricCodeInAndCovInAndStatusIsTrue(districCode, stagingRegDetails.getClassOfVehicle());
			if (optionalDistrictDocument.isPresent()) {
				return this.finalOverRideTax(stagingRegDetails, optionalDistrictDocument.get(), OptionalTax,
						currentTaxTill, lastTaxTillDate, vcrDetails, currenTax, valu);
			} else {
				Optional<FinalTaxHelper> optionalOfficeDocument = finalTaxHelperDAO
						.findByOfficeCodeInAndCovInAndStatusIsTrue(stagingRegDetails.getOfficeDetails().getOfficeCode(),
								stagingRegDetails.getClassOfVehicle());
				if (optionalOfficeDocument.isPresent()) {
					return this.finalOverRideTax(stagingRegDetails, optionalOfficeDocument.get(), OptionalTax,
							currentTaxTill, lastTaxTillDate, vcrDetails, currenTax, valu);
				}
			}
		}
		return currenTax;

	}

	private TaxHelper finalOverRideTax(RegistrationDetailsDTO registrationDetails, FinalTaxHelper finalTaxHelper,
			Double OptionalTax, LocalDate currentTaxTill, TaxHelper lastTaxTillDate, TaxHelper vcrDetails,
			TaxHelper currenTax, Integer valu) {
		Double taxArrears = 0d;
		Double penaltyArrears = 0d;
		Double penalty = 0d;
		Double tax = 0d;
		double discount = 0d;
		Double finalquaterTax = OptionalTax;
		switch (finalTaxHelper.getExcemptionsType()) {
		case QUATER:
			Pair<Double, Double> quaterTaxAndPenality = getpendingQuaters(currentTaxTill,
					lastTaxTillDate.getValidityTo(), OptionalTax, finalquaterTax, OptionalTax, vcrDetails,
					finalTaxHelper.getTaxModeDetails().get(TaxTypeEnum.TaxModule.PENALTYARREARS),
					finalTaxHelper.getTaxModeDetails().get(TaxTypeEnum.TaxModule.TAXARREARS), false, null, OptionalTax);
			taxArrears = quaterTaxAndPenality.getFirst();
			penaltyArrears = quaterTaxAndPenality.getSecond();
			penalty = currenTax.getPenalty().doubleValue();
			tax = currenTax.getTax().doubleValue();
			break;
		case PERCENTAGE:
			discount = ((currenTax.getTaxArrears()
					* finalTaxHelper.getTaxModeDetails().get(TaxTypeEnum.TaxModule.TAXARREARS)) / 100);
			taxArrears = currenTax.getTaxArrears() - discount;
			discount = ((currenTax.getPenaltyArrears()
					* finalTaxHelper.getTaxModeDetails().get(TaxTypeEnum.TaxModule.PENALTYARREARS)) / 100);
			penaltyArrears = currenTax.getPenaltyArrears() - discount;
			discount = ((currenTax.getPenalty() * finalTaxHelper.getTaxModeDetails().get(TaxTypeEnum.TaxModule.PENALTY))
					/ 100);
			penalty = currenTax.getPenalty() - discount;
			discount = ((currenTax.getTax() * finalTaxHelper.getTaxModeDetails().get(TaxTypeEnum.TaxModule.TAX)) / 100);
			tax = currenTax.getTax() - discount;
			break;
		case DIRECTTAXAMOUNT:
			taxArrears = finalTaxHelper.getTaxModeDetails().get(TaxTypeEnum.TaxModule.TAXARREARS);
			penaltyArrears = finalTaxHelper.getTaxModeDetails().get(TaxTypeEnum.TaxModule.PENALTYARREARS);
			penalty = finalTaxHelper.getTaxModeDetails().get(TaxTypeEnum.TaxModule.PENALTY);
			tax = finalTaxHelper.getTaxModeDetails().get(TaxTypeEnum.TaxModule.TAX);
			break;
		case COVANDPERMIT:
			Integer gvw = getGvwWeightForCitizen(registrationDetails);
			Integer ulw = this.getUlwWeight(registrationDetails);
			Double newTax = this.getOldCovTax(finalTaxHelper.getCov().stream().findFirst().get(),
					registrationDetails.getVahanDetails().getSeatingCapacity(), ulw, gvw, stateCode,
					finalTaxHelper.getPermitCode(), null);

			Pair<Double, Double> quaterTaxAndPenalitywithNewTax = getpendingQuaters(currentTaxTill,
					lastTaxTillDate.getValidityTo(), newTax, finalquaterTax, newTax, vcrDetails,
					finalTaxHelper.getTaxModeDetails().get(TaxTypeEnum.TaxModule.PENALTYARREARS),
					finalTaxHelper.getTaxModeDetails().get(TaxTypeEnum.TaxModule.TAXARREARS), false, null, OptionalTax);
			taxArrears = quaterTaxAndPenalitywithNewTax.getFirst();
			penaltyArrears = quaterTaxAndPenalitywithNewTax.getSecond();
			Pair<Double, Double> pairOfTax = getcurrenttaxWithPenality(vcrDetails, null, newTax, penalty, OptionalTax,
					valu);
			tax = pairOfTax.getFirst();
			penalty = pairOfTax.getSecond();

			break;

		default:

			break;
		}
		if (tax == null) {
			tax = currenTax.getTax().doubleValue();
		}
		if (penalty == null) {
			penalty = currenTax.getPenalty().doubleValue();
		}
		if (taxArrears == null) {
			taxArrears = currenTax.getTaxArrears();
		}
		if (penaltyArrears == null) {
			penaltyArrears = currenTax.getPenaltyArrears().doubleValue();
		}
		return returnTaxDetails(tax, taxArrears, penalty, penaltyArrears, null,currenTax.getLastTaxPaidUpTo());
	}

	public LocalDate calculateTaxFrom(Integer indexPosition, Integer quaternNumber) {
		if (quaternNumber == 1) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
			String date1 = "01-04-" + String.valueOf(LocalDate.now().getYear());
			return LocalDate.parse(date1, formatter);
		} else if (quaternNumber == 2) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
			String date1 = "01-07-" + String.valueOf(LocalDate.now().getYear());
			return LocalDate.parse(date1, formatter);
		} else if (quaternNumber == 3) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
			String date1 = "01-10-" + String.valueOf(LocalDate.now().getYear());
			return LocalDate.parse(date1, formatter);
		} else if (quaternNumber == 4) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
			String date1 = "01-01-" + String.valueOf(LocalDate.now().getYear());
			return LocalDate.parse(date1, formatter);
		}
		return null;
	}

	@Override
	public LocalDate calculateChassisTaxUpTo(Integer indexPosition, Integer quaternNumber, LocalDate date) {
		if (quaternNumber == 1) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
			String date1 = "01-04-" + String.valueOf(date.getYear());
			return validity(indexPosition, formatter, date1);
		} else if (quaternNumber == 2) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
			String date1 = "01-07-" + String.valueOf(date.getYear());
			return validity(indexPosition, formatter, date1);
		} else if (quaternNumber == 3) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
			String date1 = "01-10-" + String.valueOf(date.getYear());
			return validity(indexPosition, formatter, date1);
		} else if (quaternNumber == 4) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
			String date1 = "01-01-" + String.valueOf(date.getYear());
			return validity(indexPosition, formatter, date1);
		}
		return null;
	}

	private TaxHelper overRideChassisTax(String trNo, TaxHelper currenTax) {

		Optional<FinalTaxHelper> optionalPrDocument = finalTaxHelperDAO.findBytrNoInAndStatusIsTrue(trNo);
		if (optionalPrDocument.isPresent()) {
			return this.finalOverRideChassisTax(optionalPrDocument.get(), currenTax);
		}
		return currenTax;

	}

	private TaxHelper finalOverRideChassisTax(FinalTaxHelper finalTaxHelper, TaxHelper currenTax) {
		Double taxArrears = 0d;
		Double penaltyArrears = 0d;
		Double penalty = 0d;
		Double tax = 0d;
		double discount = 0d;
		switch (finalTaxHelper.getExcemptionsType()) {
		case DIRECTTAXAMOUNT:
			taxArrears = finalTaxHelper.getTaxModeDetails().get(TaxTypeEnum.TaxModule.TAXARREARS);
			penaltyArrears = finalTaxHelper.getTaxModeDetails().get(TaxTypeEnum.TaxModule.PENALTYARREARS);
			penalty = finalTaxHelper.getTaxModeDetails().get(TaxTypeEnum.TaxModule.PENALTY);
			tax = finalTaxHelper.getTaxModeDetails().get(TaxTypeEnum.TaxModule.TAX);
			break;

		default:
			break;
		}
		if (tax == null) {
			tax = currenTax.getTax().doubleValue();
		}
		if (penalty == null) {
			penalty = currenTax.getPenalty().doubleValue();
		}
		if (taxArrears == null) {
			taxArrears = currenTax.getTaxArrears();
		}
		if (penaltyArrears == null) {
			penaltyArrears = currenTax.getPenaltyArrears().doubleValue();
		}
		return returnTaxDetails(tax, taxArrears, penalty, penaltyArrears, null,currenTax.getLastTaxPaidUpTo());
	}

	private Integer getGvwWeightForAlt(RegServiceDTO regServiceDTO) {
		Integer rlw = null;
		Integer gtw = 0;
		if (regServiceDTO.getAlterationDetails().getCov() != null && ClassOfVehicleEnum.ARVT.getCovCode()
				.equalsIgnoreCase(regServiceDTO.getAlterationDetails().getCov())) {
			if (regServiceDTO.getAlterationDetails().getTrailers() == null
					|| regServiceDTO.getAlterationDetails().getTrailers().isEmpty()) {
				throw new BadRequestException("Trailers Details not found in Alteration collection for(ARVT) : "
						+ regServiceDTO.getApplicationNo());
			}
			gtw = regServiceDTO.getAlterationDetails().getTrailers().stream().findFirst().get().getGtw();
			for (TrailerChassisDetailsDTO trailerDetails : regServiceDTO.getAlterationDetails().getTrailers()) {
				if (trailerDetails.getGtw() > gtw) {
					gtw = trailerDetails.getGtw();
				}
			}
			rlw = regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw() + gtw;
			return rlw;
		} else {
			rlw = regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw() + gtw;
			return rlw;
		}

	}

	private TaxHelper getBilateralTax(String applicationNo, List<ServiceEnum> serviceEnums, String taxType,
			String purpose) {
		if (StringUtils.isNoneBlank(taxType) && taxType.equals(ServiceCodeEnum.CESS_FEE.getCode())) {
			return null;
		}
		if (StringUtils.isBlank(purpose)) {
			throw new BadRequestException("Please select purpose:" + applicationNo);
		}

		Optional<RegServiceDTO> regServiceOptional = Optional.empty();
		regServiceOptional = regServiceDAO.findByApplicationNo(applicationNo);
		if (!regServiceOptional.isPresent()) {
			// getOldpaidBilaterTax(applicationNo, listOfTaxDetails, regServiceOptional);
			List<RegServiceDTO> regServicelist = regServiceDAO.findByPrNo(applicationNo);
			if ((regServicelist == null || regServicelist.isEmpty())
					&& !purpose.equalsIgnoreCase(PurposeEnum.FRESH.getCode())) {
				throw new BadRequestException("application not found. Please select Fresh: " + applicationNo);
			}
			regServicelist.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
			regServiceOptional = regServicelist.stream().findFirst();
		}
		LocalDate currentTaxTill = getBilaterTaxUpTo(LocalDate.now());
		if (!purpose.equalsIgnoreCase(PurposeEnum.FRESH.getCode())) {
			Optional<BileteralTaxDTO> bilateraDetailsOptional = bileteralTaxDAO
					.findByPrNoAndStatusIsTrue(regServiceOptional.get().getPrNo());
			if (!bilateraDetailsOptional.isPresent()) {
				throw new BadRequestException("No active records found for : " + regServiceOptional.get().getPrNo());
			}
			BileteralTaxDTO bilateralDto = bilateraDetailsOptional.get();
			if (bilateralDto.getValidityTo().isBefore(currentTaxTill)) {
				if (purpose.equalsIgnoreCase(PurposeEnum.RENEWAL.getCode())) {
					throw new BadRequestException("tax paid up to date");
				}
				if (purpose.equalsIgnoreCase(PurposeEnum.TRANSFER.getCode())
						|| purpose.equalsIgnoreCase(PurposeEnum.VEHICLEREPLACE.getCode())) {
					if (bilateralDto.getValidityTo().isBefore(LocalDate.now())) {
						throw new BadRequestException("Validity completed. Please select fresh or renewal for: "
								+ regServiceOptional.get().getPrNo());
					}
				}

			}
		}
		if (purpose.equalsIgnoreCase(PurposeEnum.FRESH.getCode())
				|| purpose.equalsIgnoreCase(PurposeEnum.RENEWAL.getCode())) {
			return getBilaterTaxForFresh(purpose);
		}
		return returnTaxDetails(TaxTypeEnum.QuarterlyTax.getDesc(), 0l, 0d, currentTaxTill, 0l, 0d,
				TaxTypeEnum.TaxPayType.REG, "", 0d,null);
	}

	@Override
	public LocalDate getBilaterTaxUpTo(LocalDate localDate) {
		List<Integer> quaterFour = new ArrayList<>();
		quaterFour.add(0, 1);
		quaterFour.add(1, 2);
		quaterFour.add(2, 3);
		LocalDate currentTaxTill = null;
		if (quaterFour.contains(localDate.getMonthValue())) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
			String date1 = "31-03-" + String.valueOf(LocalDate.now().getYear());
			currentTaxTill = LocalDate.parse(date1, formatter);

		} else {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
			String date1 = "31-03-" + String.valueOf(localDate.plusYears(1).getYear());
			currentTaxTill = LocalDate.parse(date1, formatter);
		}
		return currentTaxTill;
	}

	private TaxHelper getBilaterTaxForFresh(String purpose) {
		// Long tax = 0l;
		LocalDate currentTaxTill = getBilaterTaxUpTo(LocalDate.now());
		Long monthsBetween = ChronoUnit.MONTHS.between(LocalDate.now(), currentTaxTill);
		monthsBetween = monthsBetween + 1;
		double tax = (5000 / 12d) * monthsBetween;
		Long roundTax = roundUpperTen(tax);
		return returnTaxDetails(TaxTypeEnum.QuarterlyTax.getDesc(), roundTax, 0d, currentTaxTill, 0l, 0d,
				TaxTypeEnum.TaxPayType.REG, "", 0d,null);
	}

	private TaxHelper getBilaterTax(String purpose, LocalDate validityTo, LocalDate currentTaxTill) {
		if (purpose.equalsIgnoreCase("FRESH")) {
			return getBilaterTaxForFresh(purpose);
		} else {
			// TODO need to cal tax for renewal.
			return getBilaterTaxForFresh(purpose);
		}
	}

	private TaxHelper getQuaterTaxForSpecificCase(String taxType, RegistrationDetailsDTO registrationDetails,
			RegServiceDTO regServiceDTO, boolean isApplicationFromMvi,
			StagingRegistrationDetailsDTO stagingRegistrationDetails, boolean isChassesApplication,
			boolean isOtherState, String applicationNo, List<ServiceEnum> serviceEnum, String permitTypeCode,
			String routeCode, Optional<MasterTaxBased> taxCalBasedOn, String classOfvehicle,
			TaxTypeEnum.TaxPayType payTaxType, Optional<AlterationDTO> alterDetails, boolean isWeightAlt,
			TaxHelper quaterTax, Pair<List<VcrFinalServiceDTO>, CitizenFeeDetailsInput> listOfVcrsDetails, boolean vcr,
			boolean isregestered) {

		if (taxType.equalsIgnoreCase(TaxTypeEnum.CESS.getDesc())) {
			TaxHelper cessTaxHelper = returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), 0l, 0d, lifTaxValidityCal(), 0l,
					0d, payTaxType, "", 0d,null);
			quaterTax = cessTaxHelper;
			return quaterTax;
		}

		TaxHelper lastTaxTillDate = getLastPaidTax(registrationDetails, regServiceDTO, isApplicationFromMvi,
				validity(TaxTypeEnum.QuarterlyTax.getDesc()), stagingRegistrationDetails, isChassesApplication,
				taxTypes(), isOtherState, vcr);
		if (lastTaxTillDate == null || lastTaxTillDate.getTax() == null || lastTaxTillDate.getValidityTo() == null) {
			throw new BadRequestException("TaxDetails not found");
		}
		if (lastTaxTillDate.isAnypendingQuaters()) {
			// taxType = TaxTypeEnum.QuarterlyTax.getDesc();
			quaterTax = quaterTaxCalculation(applicationNo, isApplicationFromMvi, isChassesApplication, taxType,
					isOtherState, serviceEnum, permitTypeCode, routeCode, taxCalBasedOn, regServiceDTO,
					registrationDetails, stagingRegistrationDetails, classOfvehicle, payTaxType, alterDetails,
					isWeightAlt, listOfVcrsDetails, vcr, isregestered, false, null);
		}
		return quaterTax;

	}

	@Override
	public boolean secondVechileMasterData(String cov) {
		Optional<MasterAmountSecoundCovsDTO> basedOnsecoundVehicle = Optional.empty();
		List<MasterAmountSecoundCovsDTO> masterAmountSecoundCovsDTO = masterAmountSecoundCovsDAO.findAll();
		if (masterAmountSecoundCovsDTO.isEmpty()) {
			// TODO:need to Exception throw
		}
		basedOnsecoundVehicle = masterAmountSecoundCovsDTO.stream().filter(m -> m.getSecondcovcode().contains(cov))
				.findFirst();
		if (basedOnsecoundVehicle.isPresent()) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;

	}

	private TaxHelper finalOverRideTaxForAllCasses(String prNo, String trNo, TaxHelper currenTax) {
		Optional<FinalTaxHelper> optionalPrDocument = Optional.empty();
		if (StringUtils.isNoneBlank(trNo)) {
			optionalPrDocument = finalTaxHelperDAO.findBytrNoInAndStatusIsTrue(trNo);
		} else if (StringUtils.isNoneBlank(prNo)) {
			optionalPrDocument = finalTaxHelperDAO.findByPrNoInAndStatusIsTrue(prNo);
		}
		if (optionalPrDocument.isPresent()) {
			if (currenTax != null) {
				currenTax = finalOverRideChassisTax(optionalPrDocument.get(), currenTax);
			} else {
				currenTax = this.finalOverRideTaxForAllCasses(optionalPrDocument.get());
			}

		}
		return currenTax;

	}

	private TaxHelper finalOverRideTaxForAllCasses(FinalTaxHelper finalTaxHelper) {
		Double taxArrears = 0d;
		Double penaltyArrears = 0d;
		Double penalty = 0d;
		Double tax = 0d;
		double discount = 0d;
		switch (finalTaxHelper.getExcemptionsType()) {
		case DIRECTTAXAMOUNT:
			taxArrears = finalTaxHelper.getTaxModeDetails().get(TaxTypeEnum.TaxModule.TAXARREARS);
			penaltyArrears = finalTaxHelper.getTaxModeDetails().get(TaxTypeEnum.TaxModule.PENALTYARREARS);
			penalty = finalTaxHelper.getTaxModeDetails().get(TaxTypeEnum.TaxModule.PENALTY);
			tax = finalTaxHelper.getTaxModeDetails().get(TaxTypeEnum.TaxModule.TAX);
			break;

		default:
			break;
		}
		if (tax == null) {
			tax = 0d;
		}
		if (penalty == null) {
			penalty = 0d;
		}
		if (taxArrears == null) {
			taxArrears = 0d;
		}
		if (penaltyArrears == null) {
			penaltyArrears = 0d;
		}

		return returnTaxDetails(tax, taxArrears, penalty, penaltyArrears, null,null);
	}

	private Pair<String, String> getTrAndPrNo(StagingRegistrationDetailsDTO stagingRegDetails,
			RegServiceDTO regServiceDTO, RegistrationDetailsDTO registrationOptional, Double totalLifeTax,
			Float Percent, boolean isApplicationFromMvi, boolean isChassesApplication, boolean isOtherState) {
		String prNo = StringUtils.EMPTY;
		String trNo = StringUtils.EMPTY;
		if (isChassesApplication) {
			trNo = stagingRegDetails.getTrNo();
		} else if (isApplicationFromMvi) {
			prNo = regServiceDTO.getPrNo();
		} else if (isOtherState) {
			RegServiceVO vo = regServiceMapper.convertEntity(regServiceDTO);
			OtherStateApplictionType applicationType = getOtherStateVehicleStatus(vo);
			if (OtherStateApplictionType.TrNo.equals(applicationType)) {
				trNo = regServiceDTO.getRegistrationDetails().getTrNo();
			} else if (OtherStateApplictionType.PrNo.equals(applicationType)) {
				prNo = regServiceDTO.getPrNo();
			} else {
				trNo = regServiceDTO.getApplicationNo();
			}
		} else {
			prNo = registrationOptional.getPrNo();
		}

		return Pair.of(prNo, trNo);
	}

	// @Override
	public Pair<List<VcrFinalServiceDTO>, CitizenFeeDetailsInput> getVcrDetails(List<String> listOfVcrs,boolean specificVcrPayment) {
		CitizenFeeDetailsInput input = new CitizenFeeDetailsInput();
		
		String permit = permitcode;
		Integer seats;
		String routeCode = null;
		Integer ulw;
		Integer gvw;
		LocalDate playedAsDate = null;
		if(specificVcrPayment)
		{
			input.setSpecificVcrPayment(specificVcrPayment);
		}
		List<VcrFinalServiceDTO> vcrList = registrationService.getVcrDetails(listOfVcrs,false,specificVcrPayment);
		if (vcrList == null || vcrList.isEmpty()) {
			logger.error("No record found for vcr..[{}]");
			throw new BadRequestException("No record found for vcr : ");
		}
		boolean isOtherSate = Boolean.FALSE;
		boolean isUnregistered = Boolean.FALSE;
		List<VcrFinalServiceDTO> allVcrList = null;
		
		vcrList.sort((p1, p2) -> p2.getVcr().getDateOfCheck().compareTo(p1.getVcr().getDateOfCheck()));
		VcrFinalServiceDTO singleVcr = vcrList.stream().findFirst().get();
		input.setLatestVcrDate(singleVcr.getVcr().getDateOfCheck().toLocalDate());
		if(!specificVcrPayment) {
		if (StringUtils.isNoneBlank(singleVcr.getRegistration().getRegApplicationNo())) {
			allVcrList = vcrFinalServiceDAO.findByRegistrationRegApplicationNoAndIsVcrClosedIsFalse(
					singleVcr.getRegistration().getRegApplicationNo());
			allVcrList = allVcrList.stream()
			.filter(paymentDone -> paymentDone.getPaymentType() == null
					|| !paymentDone.getPaymentType().equalsIgnoreCase(GatewayTypeEnum.CASH.getDescription()))
			.collect(Collectors.toList());
		} else {
			if (StringUtils.isBlank(singleVcr.getRegistration().getChassisNumber())) {
				logger.error("Chassis number not found for vcr..[{}]" + singleVcr.getVcr().getVcrNumber());
				throw new BadRequestException(
						"Chassis number not found for vcr..[{}]" + singleVcr.getVcr().getVcrNumber());
			}
			allVcrList = vcrFinalServiceDAO.findByRegistrationChassisNumberAndIsVcrClosedIsFalse(
					singleVcr.getRegistration().getChassisNumber());
			allVcrList = allVcrList.stream()
					.filter(paymentDone -> paymentDone.getPaymentType() == null
							|| !paymentDone.getPaymentType().equalsIgnoreCase(GatewayTypeEnum.CASH.getDescription()))
					.collect(Collectors.toList());
		}
		}else {
			allVcrList=	vcrFinalServiceDAO.findByVcrVcrNumberIgnoreCaseAndIsVcrClosedIsFalse(singleVcr.getVcr().getVcrNumber());
			allVcrList = allVcrList.stream()
					.filter(paymentDone -> paymentDone.getPaymentType() == null
							|| !paymentDone.getPaymentType().equalsIgnoreCase(GatewayTypeEnum.CASH.getDescription()))
					.collect(Collectors.toList());

		}
		for (VcrFinalServiceDTO vcrDto : allVcrList) {
			if (vcrDto.getRegistration().isOtherState()) {
				isOtherSate = Boolean.TRUE;
			}
			if (vcrDto.getRegistration().isUnregisteredVehicle()) {
				isUnregistered = Boolean.TRUE;
			}
			if(vcrDto.getRegistration().getNocIssued()!=null && vcrDto.getRegistration().getNocIssued()) {
				input.setNocIssued(Boolean.TRUE);
				if(vcrDto.getRegistration().getTaxCalculationDateForQuarterlyTax()== null&&vcrDto.getRegistration().getTaxCalculationDateForLifeTax()==null) {
					logger.error("State entry date or noc issued date missed for..[{}]" + vcrDto.getVcr().getVcrNumber());
					throw new BadRequestException("State entry date or noc issued date missed for..[{}]" + vcrDto.getVcr().getVcrNumber());
				}
				LocalDate nocDate = vcrDto.getRegistration().getTaxCalculationDateForQuarterlyTax()!=null?vcrDto.getRegistration().getTaxCalculationDateForQuarterlyTax():
					vcrDto.getRegistration().getTaxCalculationDateForLifeTax();
				input.setNocDate(nocDate);
			}
			for(OffenceDTO offenceVo:vcrDto.getOffence().getOffence()) {
				if(offenceVo.getIntrastate()!=null && offenceVo.getIntrastate()) {
					if(input.getIntrastateVcrDate()==null) {
						input.setIntrastateVcrDate(vcrDto.getVcr().getDateOfCheck());
					}
					input.setIntrastate( Boolean.TRUE);
				}
				
				
			}
		}
		boolean vehicleSized = Boolean.FALSE;
		LocalDate vehicleSizedDate = null;

		List<VcrFinalServiceDTO> vcrDtosList = vcrList.stream()
				.filter(sizedDetails -> sizedDetails.getSeizedAndDocumentImpounded() != null
						&& sizedDetails.getSeizedAndDocumentImpounded().getVehicleSeizedDTO() != null
						&& sizedDetails.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().getDateOfSeized() != null)
				.collect(Collectors.toList());
		if (vcrDtosList != null && !vcrDtosList.isEmpty()) {
			vcrDtosList.sort((p1, p2) -> p1.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().getDateOfSeized()
					.compareTo(p2.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().getDateOfSeized()));
			vehicleSized = Boolean.TRUE;
			vehicleSizedDate = vcrDtosList.stream().findFirst().get().getSeizedAndDocumentImpounded()
					.getVehicleSeizedDTO().getDateOfSeized();
		}

		input.setVehicleSized(vehicleSized);
		input.setVehicleSizedDate(vehicleSizedDate);
		if (isOtherSate) {
			if (StringUtils.isNoneBlank(singleVcr.getRegistration().getRegApplicationNo())) {
				Optional<RegServiceDTO> regService = regServiceDAO
						.findByApplicationNo(singleVcr.getRegistration().getRegApplicationNo());
				if (!regService.isPresent()) {
					logger.error("No record found in registration service for: "
							+ singleVcr.getRegistration().getRegApplicationNo());
					throw new BadRequestException("No record found in registration service for: "
							+ singleVcr.getRegistration().getRegApplicationNo());
				}
				RegServiceDTO serviceDto = regService.get();
				input.setRegServiceDetails(serviceDto);
				input.setRegDetails(serviceDto.getRegistrationDetails());
				ulw = serviceDto.getRegistrationDetails().getVahanDetails().getUnladenWeight();
				gvw = serviceDto.getRegistrationDetails().getVahanDetails().getGvw();
				this.getLastPaidTaxForVcr(null, serviceDto, null, isOtherSate, isUnregistered, input, vcrList);
			} else {
				// TODO set trgenerated date
				if (singleVcr.getRegistration().isUnregisteredVehicle()||singleVcr.getRegistration().isOtherStateUnregister()) {
					input.setUnRegestered(Boolean.TRUE);
				}
				input.setNoApplication(Boolean.TRUE);
				ulw = singleVcr.getRegistration().getUlw();
				gvw = singleVcr.getRegistration().getGvwc();
			}

		} else {
			if (singleVcr.getRegistration().isUnregisteredVehicle()) {
				input.setUnRegestered(Boolean.TRUE);
				if (StringUtils.isNoneBlank(singleVcr.getRegistration().getRegApplicationNo())) {
					Optional<StagingRegistrationDetailsDTO> statigingDoc = stagingRegistrationDetailsDAO
							.findByApplicationNo(singleVcr.getRegistration().getRegApplicationNo());
					if (statigingDoc.isPresent()) {
						input.setConsiderStaging(Boolean.TRUE);
						input.setStagingRegistrationDetails(statigingDoc.get());
						ulw = statigingDoc.get().getVahanDetails().getUnladenWeight();
						gvw = statigingDoc.get().getVahanDetails().getGvw();
						if(StringUtils.isNoneBlank(statigingDoc.get().getClassOfVehicle())) {
							if((statigingDoc.get().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode())
									||statigingDoc.get().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode()))) {
								
								List<VcrFinalServiceDTO> newCovDetails = vcrList.stream()
										.filter(single -> !single.getRegistration().getClasssOfVehicle().getCovcode().
												equalsIgnoreCase(statigingDoc.get().getClassOfVehicle()))
										.collect(Collectors.toList());
								if (newCovDetails != null && !newCovDetails.isEmpty()) {
									input.setVcrChassisTax(Boolean.TRUE);
									newCovDetails.sort((p1, p2) -> p1.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().getDateOfSeized()
											.compareTo(p2.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().getDateOfSeized()));
									VcrFinalServiceDTO  newCovDetail = newCovDetails.stream().findFirst().get();
									ulw = newCovDetail.getRegistration().getUlw();
									seats = newCovDetail.getRegistration().getSeatingCapacity();
									//statigingDoc.get().getVahanDetails().setUnladenWeight(ulw);
									//statigingDoc.get().setClassOfVehicle(newCovDetail.getRegistration().getClasssOfVehicle().getCovcode());
									AlterationDTO alt = new AlterationDTO();
									alt.setCov(newCovDetail.getRegistration().getClasssOfVehicle().getCovcode());
									alt.setUlw(newCovDetail.getRegistration().getUlw());
									alt.setGvw(statigingDoc.get().getVahanDetails().getGvw());
									alt.setSeating(statigingDoc.get().getVahanDetails().getSeatingCapacity());
									if(newCovDetail.getRegistration().getDateOfCompletion()==null) {
										logger.error("Body build date missing for: "
												+ singleVcr.getRegistration().getRegApplicationNo());
										throw new BadRequestException("Body build date missing for: "
												+ singleVcr.getRegistration().getRegApplicationNo());
									}
									alt.setDateOfCompletion(newCovDetail.getRegistration().getDateOfCompletion());
									if(statigingDoc.get().getInvoiceDetails()!=null && statigingDoc.get().getInvoiceDetails().getInvoiceValue()!=null) {
										if(newCovDetail.getRegistration().getInvoiceAmmount()!=null&&newCovDetail.getRegistration().getInvoiceAmmount()>
										statigingDoc.get().getInvoiceDetails().getInvoiceValue()) {
											statigingDoc.get().getInvoiceDetails().setInvoiceValue(newCovDetail.getRegistration().getInvoiceAmmount().doubleValue());
										}
									}
									if(newCovDetail.getRegistration().getSeatingCapacity()!=null&&newCovDetail.getRegistration().getSeatingCapacity()>0) {
										alt.setSeating(String.valueOf(newCovDetail.getRegistration().getSeatingCapacity()));
									}
									//gvw = statigingDoc.get().getVahanDetails().getGvw();
									input.setAlterDetails(alt);
								}
							}
							
						}
						
						input.setRegDetails(statigingDoc.get());
						this.getLastPaidTaxForVcr(null, null, statigingDoc.get(), isOtherSate, isUnregistered, input,
								vcrList);
					} else {
						Optional<RegistrationDetailsDTO> regDoc = registrationDetailDAO
								.findByApplicationNo(singleVcr.getRegistration().getRegApplicationNo());
						if (regDoc == null || !regDoc.isPresent()) {
							logger.error("No record found in registration details for: "
									+ singleVcr.getRegistration().getRegApplicationNo());
							throw new BadRequestException("No record found in registration details for: "
									+ singleVcr.getRegistration().getRegApplicationNo());
						}
						input.setUnRegestered(Boolean.FALSE);
						input.setRegestered(Boolean.TRUE);
						input.setRegDetails(regDoc.get());
						ulw = regDoc.get().getVahanDetails().getUnladenWeight();
						gvw = regDoc.get().getVahanDetails().getGvw();
						this.getLastPaidTaxForVcr(regDoc.get(), null, null, isOtherSate, isUnregistered, input,
								vcrList);
					}
				} else {
					// TODO set trgenerated date
					input.setNoApplication(Boolean.TRUE);
					ulw = singleVcr.getRegistration().getUlw();
					gvw = singleVcr.getRegistration().getGvwc();
				}

			} else {
				if (StringUtils.isNoneBlank(singleVcr.getRegistration().getRegApplicationNo())) {
					Optional<RegistrationDetailsDTO> regDoc = registrationDetailDAO
							.findByApplicationNo(singleVcr.getRegistration().getRegApplicationNo());
					if (regDoc == null || !regDoc.isPresent()) {
						logger.error("No record found in registration details for: "
								+ singleVcr.getRegistration().getRegApplicationNo());
						throw new BadRequestException("No record found in registration details for: "
								+ singleVcr.getRegistration().getRegApplicationNo());
					}
					input.setRegestered(Boolean.TRUE);
					input.setRegDetails(regDoc.get());
					ulw = regDoc.get().getVahanDetails().getUnladenWeight();
					gvw = regDoc.get().getVahanDetails().getGvw();
					this.getLastPaidTaxForVcr(regDoc.get(), null, null, isOtherSate, isUnregistered, input, vcrList);
				} else {
					// NOT executable code
					// logger.error("No record found in registration details ");
					// throw new BadRequestException("No record found in registration details for");
					input.setNoApplication(Boolean.TRUE);
					ulw = singleVcr.getRegistration().getUlw();
					gvw = singleVcr.getRegistration().getGvwc();
				}
			}
		}
		Double finalTax = 0d;
		Double totalQuarterlyTax = 0d;
		for (VcrFinalServiceDTO vcrDto : vcrList) {
			seats = vcrDto.getRegistration().getSeatingCapacity();
			
			if (StringUtils.isNoneBlank(vcrDto.getPilledCov())) {
				if (playedAsDate == null) {
					playedAsDate = vcrDto.getVcr().getDateOfCheck().toLocalDate();
					Pair<Integer, Integer> revokedMonthpositionInQuater = getMonthposition(
							vcrDto.getVcr().getDateOfCheck().toLocalDate());
					LocalDate firstQuaterstarts = getQuarterStatrtDate(revokedMonthpositionInQuater.getFirst(),
							revokedMonthpositionInQuater.getSecond(), vcrDto.getVcr().getDateOfCheck().toLocalDate());
					playedAsDate = firstQuaterstarts.minusDays(1);
				}
				//Integer seatingCapacity = vcrDto.getRegistration().getSeatingCapacity();
				if (vcrDto.getPilledCov().equalsIgnoreCase(ClassOfVehicleEnum.SCRT.getCovCode())
						&& vcrDto.getPilledSeatings() != null && vcrDto.getPilledSeatings() > 0
						&& vcrDto.getPilledSeatings() > vcrDto.getRegistration().getSeatingCapacity()
						&& (vcrDto.getRegistration().getClasssOfVehicle().getCovcode()
								.equalsIgnoreCase(ClassOfVehicleEnum.MTLT.getCovCode())
								|| vcrDto.getRegistration().getClasssOfVehicle().getCovcode()
										.equalsIgnoreCase(ClassOfVehicleEnum.STCT.getCovCode())
								|| vcrDto.getRegistration().getClasssOfVehicle().getCovcode()
										.equalsIgnoreCase(ClassOfVehicleEnum.LTCT.getCovCode())
								|| vcrDto.getRegistration().getClasssOfVehicle().getCovcode()
										.equalsIgnoreCase(ClassOfVehicleEnum.MCRN.getCovCode()))) {
					seats = vcrDto.getPilledSeatings();
				}
				if(vcrDto.getPilledSeatings()!= null && vcrDto.getPilledSeatings()>0&&vcrDto.getPilledSeatings()>seats) {
					seats = vcrDto.getPilledSeatings();
				}
				Optional<PropertiesDTO> properties = propertiesDAO
						.findByCovAndPlayedAsCovAndSeattoGreaterThanEqualAndSeatfromLessThanEqualAndVcrTaxTrue(
								vcrDto.getRegistration().getClasssOfVehicle().getCovcode(), vcrDto.getPilledCov(),
								seats, seats);
				if (properties.isPresent()) {
					totalQuarterlyTax = (vcrDto.getRegistration().getSeatingCapacity()-1) * properties.get().getTax();
				} else {
					if ((vcrDto.getPilledCov().equals(ClassOfVehicleEnum.COCT.getCovCode())
							|| vcrDto.getPilledCov().equals(ClassOfVehicleEnum.TOVT.getCovCode()))
							&& (vcrDto.getRegistration().getClasssOfVehicle().getCovcode()
									.equalsIgnoreCase(ClassOfVehicleEnum.EIBT.getCovCode())
									|| vcrDto.getRegistration().getClasssOfVehicle().getCovcode()
											.equalsIgnoreCase(ClassOfVehicleEnum.PSVT.getCovCode()))) {
						routeCode = vcrDto.getPilledRouteCode();
						permit = PermitsEnum.PermitCodes.CCP.getPermitCode();
						if(vcrDto.getRegistration().isOtherState()) {
							routeCode = "A";
							permit = "AITP";
						}else {
						if (StringUtils.isBlank(vcrDto.getPilledRouteCode())) {
							throw new BadRequestException("route code not found for chassis no: "
									+ vcrDto.getRegistration().getChassisNumber());
						}
						}
						
					
					}
					if ((vcrDto.getPilledCov().equals(ClassOfVehicleEnum.COCT.getCovCode())
							|| vcrDto.getPilledCov().equals(ClassOfVehicleEnum.TOVT.getCovCode()))
							&& (vcrDto.getRegistration().getClasssOfVehicle().getCovcode()
									.equalsIgnoreCase(ClassOfVehicleEnum.COCT.getCovCode())
									|| vcrDto.getRegistration().getClasssOfVehicle().getCovcode()
											.equalsIgnoreCase(ClassOfVehicleEnum.TOVT.getCovCode()))) {
						if (StringUtils.isBlank(vcrDto.getPilledRouteCode())) {
							throw new BadRequestException("route code not found for chassis no: "
									+ vcrDto.getRegistration().getChassisNumber());
						}
						routeCode = vcrDto.getPilledRouteCode();
						permit = PermitsEnum.PermitCodes.CCP.getPermitCode();
						if(routeCode.equalsIgnoreCase(PermitRouteCodeEnum.ALLINDIA.getCode())) {
							permit = PermitsEnum.PermitCodes.AITP.getPermitCode();
						}
						
					}
					totalQuarterlyTax = this.getOldCovTax(vcrDto.getPilledCov(), String.valueOf(seats), ulw, gvw,
							stateCode, permit, routeCode);
				}
				if (totalQuarterlyTax > finalTax) {
					input.setNewCov(vcrDto.getPilledCov());
					input.setRouteCode(routeCode);
					finalTax = totalQuarterlyTax;
					if(seats!=null) {
						input.setSeats(seats.toString());
					}
				}
			}else {
				if ((StringUtils.isNoneBlank(vcrDto.getRegistration().getRegApplicationNo())&& !vcrDto.getRegistration().isOtherState()
						&& !vcrDto.getRegistration().isUnregisteredVehicle())&&(vcrDto.getRegistration().getClasssOfVehicle().getCovcode().equals(ClassOfVehicleEnum.COCT.getCovCode())
								|| vcrDto.getRegistration().getClasssOfVehicle().getCovcode().equals(ClassOfVehicleEnum.TOVT.getCovCode()))) {
					for (OffenceDTO offence : vcrDto.getOffence().getOffence()) {
						//TODO need to change OffenceDescription
						if(offence.getOffenceDescription().equalsIgnoreCase("Without Permit/Violation")) {
							if(StringUtils.isNoneBlank(offence.getPermitType())) {//
								if(offence.getPermitType().equalsIgnoreCase("A")) {
									permit = PermitsEnum.PermitCodes.AITP.getPermitCode();
								}else {
									routeCode = offence.getPermitType();
									permit = PermitsEnum.PermitCodes.CCP.getPermitCode();
								}
								if (playedAsDate == null) {
									playedAsDate = vcrDto.getVcr().getDateOfCheck().toLocalDate();
									Pair<Integer, Integer> revokedMonthpositionInQuater = getMonthposition(
											vcrDto.getVcr().getDateOfCheck().toLocalDate());
									LocalDate firstQuaterstarts = getQuarterStatrtDate(revokedMonthpositionInQuater.getFirst(),
											revokedMonthpositionInQuater.getSecond(), vcrDto.getVcr().getDateOfCheck().toLocalDate());
									playedAsDate = firstQuaterstarts.minusDays(1);
								}
								totalQuarterlyTax = this.getOldCovTax(vcrDto.getRegistration().getClasssOfVehicle().getCovcode(), String.valueOf(seats), ulw, gvw,
										stateCode, permit, routeCode);
							}
						}
					}
				}
				if (totalQuarterlyTax > finalTax) {
					input.setNewCov(vcrDto.getRegistration().getClasssOfVehicle().getCovcode());
					input.setRouteCode(routeCode);
					finalTax = totalQuarterlyTax;
				}
			}

		}
		input.setVcrTax(finalTax);
		input.setPlayedAsQuarterEnd(playedAsDate);
		if (isOtherSate && input.getIsOtherState()) {
			input.setOtherStateVehicle(true);
		}
		return Pair.of(allVcrList, input);
	}

	public CitizenFeeDetailsInput getLastPaidTaxForVcr(RegistrationDetailsDTO stagingRegDetails,
			RegServiceDTO regServiceDTO, StagingRegistrationDetailsDTO stagingRegistrationDetails, boolean isOtherState,
			boolean unregistered, CitizenFeeDetailsInput input, List<VcrFinalServiceDTO> vcrList) {
		String makersModel = null;
		String applicationNo = StringUtils.EMPTY;
		String classOfvehicle = null;
		String seatingCapacity;
		Integer gvw;
		LocalDate trGeneratedDate;
		List<String> taxTypes = new ArrayList<>();
		vcrList.stream().forEach(single -> {
			if (StringUtils.isNoneBlank(single.getPilledCov())) {
				input.setTaxPending(Boolean.TRUE);
			}

		});
		if (input.isTaxPending()) {
			return input;
		}
		LocalDate currentTaxTill = validity(TaxTypeEnum.QuarterlyTax.getDesc());
		if (regServiceDTO != null) {
			applicationNo = regServiceDTO.getApplicationNo();
			makersModel = regServiceDTO.getRegistrationDetails().getVahanDetails().getMakersModel();
			classOfvehicle = regServiceDTO.getRegistrationDetails().getClassOfVehicle();
			seatingCapacity = regServiceDTO.getRegistrationDetails().getVahanDetails().getSeatingCapacity();
			gvw = regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw();
			if (regServiceDTO.getRegistrationDetails().getRegistrationValidity().getTrGeneratedDate() == null) {
				trGeneratedDate = regServiceDTO.getRegistrationDetails().getRegistrationValidity().getPrGeneratedDate();
			} else {
				trGeneratedDate = regServiceDTO.getRegistrationDetails().getRegistrationValidity().getTrGeneratedDate();
			}
		} else if (stagingRegDetails != null) {
			applicationNo = stagingRegDetails.getApplicationNo();
			makersModel = stagingRegDetails.getVahanDetails().getMakersModel();
			classOfvehicle = stagingRegDetails.getClassOfVehicle();
			seatingCapacity = stagingRegDetails.getVahanDetails().getSeatingCapacity();
			gvw = stagingRegDetails.getVahanDetails().getGvw();
			if (stagingRegDetails.getRegistrationValidity().getTrGeneratedDate() != null) {
				trGeneratedDate = stagingRegDetails.getRegistrationValidity().getTrGeneratedDate();
			} else {
				trGeneratedDate = stagingRegDetails.getRegistrationValidity().getPrGeneratedDate();
			}
		} else {
			applicationNo = stagingRegistrationDetails.getApplicationNo();
			makersModel = stagingRegistrationDetails.getVahanDetails().getMakersModel();
			classOfvehicle = stagingRegistrationDetails.getClassOfVehicle();
			seatingCapacity = stagingRegistrationDetails.getVahanDetails().getSeatingCapacity();
			gvw = stagingRegistrationDetails.getVahanDetails().getGvw();
			if (stagingRegistrationDetails.getRegistrationValidity().getTrGeneratedDate() != null) {
				trGeneratedDate = stagingRegistrationDetails.getRegistrationValidity().getTrGeneratedDate();
			} else {
				trGeneratedDate = stagingRegistrationDetails.getTrGeneratedDate().toLocalDate();
			}
		}
		input.setTrGeneratedDate(trGeneratedDate);
		if (makersModel != null) {
			Optional<MasterTaxExcemptionsDTO> optionalTaxExcemption = masterTaxExcemptionsDAO
					.findByKeyvalue(makersModel);
			if (optionalTaxExcemption.isPresent()) {
				/*
				 * return this.addTaxDetails(TaxTypeEnum.QuarterlyTax.getDesc(), 0d,
				 * validity(TaxTypeEnum.QuarterlyTax.getDesc()), Boolean.FALSE,
				 * LocalDateTime.now());
				 */
				input.setTaxPending(Boolean.FALSE);
				return input;
			}
		}

		if (classOfvehicle == null) {
			logger.error("class of vehicle not found for : " + applicationNo);
			throw new BadRequestException("class of vehicle not found for : " + applicationNo);
		}
		Optional<MasterPayperiodDTO> Payperiod = masterPayperiodDAO.findByCovcode(classOfvehicle);
		if (!Payperiod.isPresent()) {
			logger.error("No master pay period for : " + classOfvehicle);
			throw new BadRequestException("No master pay period for : " + classOfvehicle);
		}
		if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.BOTH.getCode())) {
			Pair<Optional<MasterPayperiodDTO>, Boolean> payperiodAndGoStatus = getPayPeroidForBoth(Payperiod,
					seatingCapacity, gvw);
			// boolean gostatus = payperiodAndGoStatus.getSecond();
		}
		// TODO need to check offence for played as other cov

		if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.LifeTax.getCode())) {
			taxTypes.add(TaxTypeEnum.LifeTax.getDesc());
		} else {
			taxTypes.add(TaxTypeEnum.QuarterlyTax.getDesc());
			taxTypes.add(TaxTypeEnum.HalfyearlyTax.getDesc());
			taxTypes.add(TaxTypeEnum.YearlyTax.getDesc());
		}
		List<TaxDetailsDTO> listOfTax = this.lastPaidTaxForVcr(applicationNo, taxTypes, null);
		if (listOfTax != null && !listOfTax.isEmpty()) {
			if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.LifeTax.getCode())) {
				input.setTaxPending(Boolean.FALSE);
				return input;
			}
			listOfTax.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
			TaxDetailsDTO dto = listOfTax.stream().findFirst().get();
			if (dto.getTaxDetails() == null) {
				logger.error("TaxDetails map not found: [{}]", applicationNo);
				throw new BadRequestException("TaxDetails map not found:" + applicationNo);
			}
			for (Map<String, TaxComponentDTO> map : dto.getTaxDetails()) {

				for (Entry<String, TaxComponentDTO> entry : map.entrySet()) {
					if (taxTypes.stream().anyMatch(key -> key.equalsIgnoreCase(entry.getKey()))) {

						if (entry.getValue().getValidityTo().isBefore(currentTaxTill)) {
							input.setTaxPending(Boolean.TRUE);
							return input;

						} else if (entry.getValue().getValidityTo().equals(currentTaxTill)
								|| entry.getValue().getValidityTo().isAfter(currentTaxTill)) {
							input.setTaxPending(Boolean.FALSE);
							return input;
						}

					}
				}
			}
		} else {
			input.setTaxPending(Boolean.TRUE);
			if (isOtherState) {
				input.setIsOtherState(Boolean.TRUE);
			}
			return input;
		}
		return null;
	}

	private List<TaxDetailsDTO> lastPaidTaxForVcr(String applicationNo, List<String> taxType, String prNo) {
		List<TaxDetailsDTO> listOfTaxDetails = new ArrayList<>();
		List<TaxDetailsDTO> taxDetailsList = new ArrayList<>();

		taxDetailsList = taxDetailsDAO.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(applicationNo,
				taxType);

		if (!taxDetailsList.isEmpty()) {

			registrationService.updatePaidDateAsCreatedDate(taxDetailsList);
			taxDetailsList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
			for (String type : taxType) {
				for (TaxDetailsDTO taxDetails : taxDetailsList) {
					if (taxDetails.getTaxDetails() == null) {
						logger.error("TaxDetails map not found: [{}]", applicationNo);
						throw new BadRequestException("TaxDetails map not found:" + applicationNo);
					}
					if (taxDetails.getTaxDetails().stream().anyMatch(key -> key.keySet().contains(type))) {
						listOfTaxDetails.add(taxDetails);
						break;
					}
				}
			}
			taxDetailsList.clear();
			return listOfTaxDetails;
		}
		return null;
	}

	private TaxHelper getVcrTaxForUnRegistered(Double OptionalTax, Integer valu,
			Pair<List<VcrFinalServiceDTO>, CitizenFeeDetailsInput> listOfVcrsDetails, boolean vcr, boolean isregestered,
			String permitTypeCode, String routeCode, TaxHelper vcrDetails,String classOfvehicle) {
		Double penalityTax = 0d;
		if (listOfVcrsDetails.getSecond().getIsOtherState()) {
			return returnTaxDetails(OptionalTax / 3, 0d, OptionalTax / 3, 0d, 0d,null);
		} else {
			if(listOfVcrsDetails.getSecond().isNocIssued()) {
				return getOtherStatePenalityAndTax(OptionalTax, vcr, listOfVcrsDetails.getSecond().getNocDate(), false);
			}
			List<String> listTaxType = this.taxTypes();
			LocalDate taxValidity = this.validity(TaxTypeEnum.QuarterlyTax.getDesc());
			permitTypeCode = this.permitcode;
			routeCode = StringUtils.EMPTY;
			listTaxType.add(ServiceCodeEnum.LIFE_TAX.getCode());
			List<VcrFinalServiceDTO> listOfVcrs = listOfVcrsDetails.getFirst();
			listOfVcrs.sort((p1, p2) -> p1.getVcr().getDateOfCheck().compareTo(p2.getVcr().getDateOfCheck()));
			VcrFinalServiceDTO vcrDto = listOfVcrs.stream().findFirst().get();
			List<TaxDetailsDTO> listOfTax = taxDetailsDAO
					.findFirst10ByChassisNoOrderByCreatedDateDesc(vcrDto.getRegistration().getChassisNumber());
			if (listOfTax != null && !listOfTax.isEmpty()) {
				List<TaxDetailsDTO> vcrTax = listOfTax.stream()
						.filter(tax -> tax.getTaxPaidThroughVcr() != null && tax.getTaxPaidThroughVcr())
						.collect(Collectors.toList());
				if (vcrTax != null && !vcrTax.isEmpty()) {
					List<TaxDetailsDTO> lifTax = vcrTax.stream()
							.filter(tax -> tax.getPaymentPeriod() != null
									&& tax.getPaymentPeriod().equalsIgnoreCase(TaxTypeEnum.QuarterlyTax.getDesc()))
							.collect(Collectors.toList());
					if (lifTax != null && !lifTax.isEmpty()) {
						TaxHelper taxhelper = getcommenlastTaxPaid(taxValidity, listTaxType, null, lifTax);
						if (!taxhelper.isAnypendingQuaters()) {
							return returnTaxDetails(0d, 0d, penalityTax, 0d, 0d,null);
						} else {
							List<TaxDetailsDTO> taxList = listOfTax.stream()
									.filter(tax -> tax.getPaymentPeriod() != null && tax.getPaymentPeriod()
											.equalsIgnoreCase(TaxTypeEnum.QuarterlyTax.getDesc()))
									.collect(Collectors.toList());
							LocalDate taxPeriodDate = taxList.stream().findFirst().get().getTaxPeriodEnd();
							//vcrDto.getVcr().setDateOfCheck(taxPeriodDate.atStartOfDay());
						}
					}
				}
			}
			if ((vcrDto.getRegistration().getClasssOfVehicle().getCovcode()
							.equalsIgnoreCase(ClassOfVehicleEnum.COCT.getCovCode())
							|| vcrDto.getRegistration().getClasssOfVehicle().getCovcode()
									.equalsIgnoreCase(ClassOfVehicleEnum.TOVT.getCovCode())|| vcrDto.getRegistration().getClasssOfVehicle().getCovcode()
									.equalsIgnoreCase(ClassOfVehicleEnum.MAXT.getCovCode()))) {
				if(vcrDto.isVehicleHaveAitp()) {
				listOfVcrsDetails.getSecond().setCalCulateQutTax(Boolean.TRUE);
				//routeCode = "A";
				String permit = PermitsEnum.PermitCodes.AITP.getPermitCode();
				OptionalTax = this.getOldCovTax(vcrDto.getRegistration().getClasssOfVehicle().getCovcode(), String.valueOf(vcrDto.getRegistration().getSeatingCapacity()), vcrDto.getRegistration().getUlw(), vcrDto.getRegistration().getGvwc(),
						stateCode, permit, null);
				}else {
					routeCode = "S";
					String permit = PermitsEnum.PermitCodes.CCP.getPermitCode();
					OptionalTax = this.getOldCovTax(vcrDto.getRegistration().getClasssOfVehicle().getCovcode(), String.valueOf(vcrDto.getRegistration().getSeatingCapacity()), vcrDto.getRegistration().getUlw(), vcrDto.getRegistration().getGvwc(),
							stateCode, permit, routeCode );
				}
			}
			Pair<Integer, Integer> revokedMonthpositionInQuater = getMonthposition(
					vcrDto.getVcr().getDateOfCheck().toLocalDate());
			LocalDate firstQuaterstarts = getQuarterStatrtDate(revokedMonthpositionInQuater.getFirst(),
					revokedMonthpositionInQuater.getSecond(), vcrDto.getVcr().getDateOfCheck().toLocalDate());
			LocalDate taxUpTo = validity(ServiceCodeEnum.QLY_TAX.getCode());
			Long totalMonthsTax = ChronoUnit.MONTHS.between(firstQuaterstarts, taxUpTo);
			// TaxHelper oldTaxDetails = getIsGreenTaxPending(applicationNo, listTaxType,
			// LocalDate.now(), prNo);

			totalMonthsTax = Math.abs(totalMonthsTax);
			totalMonthsTax = totalMonthsTax + 1;
			totalMonthsTax = totalMonthsTax - 3;
			// double Penalityquaters = totalMonthsTax / 3d;
			// double PenalityQuatersRound = Math.ceil(totalMonthsTax / 3d);
			// PenalityQuatersRound = PenalityQuatersRound - 1;
			Pair<Double, Double> taxArrAndPenality =Pair.of(0d, 0d);
			if(!vcrDto.getRegistration().isOtherState()) {
			 taxArrAndPenality = vcrPenality(OptionalTax, totalMonthsTax,
					listOfVcrsDetails.getSecond().isVehicleSized(), listOfVcrsDetails.getSecond().getVehicleSizedDate(),
					vcrDetails, firstQuaterstarts);
			}
			
			Optional<PropertiesDTO> otherStateQutTax =	propertiesDAO.findByOtherStateQutTaxCovsInAndOtherStateQutTaxCovsflagTrue(classOfvehicle);
			if(otherStateQutTax.isPresent()) {
				listOfVcrsDetails.getSecond().setCalCulateQutTax(Boolean.TRUE);
			}
			if(vcrDto.getRegistration().isOtherState()) {
			Optional<PropertiesDTO> aitpTax =	propertiesDAO.findByOtherStateAitpTaxTrue();
			if(aitpTax.isPresent()&& aitpTax.get().getCovAndVoluntaryTax().containsKey(classOfvehicle)) {
				Double seats = vcrDto.getRegistration().getSeatingCapacity().doubleValue();
				if( vcrDto.getRegistration().getClasssOfVehicle().getCovcode()
						.equalsIgnoreCase(ClassOfVehicleEnum.MTLT.getCovCode())||vcrDto.getRegistration().getClasssOfVehicle().getCovcode()
						.equalsIgnoreCase(ClassOfVehicleEnum.LTCT.getCovCode())||vcrDto.getRegistration().getClasssOfVehicle().getCovcode()
						.equalsIgnoreCase(ClassOfVehicleEnum.STCT.getCovCode())) {
					seats=1d;
				}
				if(vcrDto.isVehicleHaveAitp()) {
					listOfVcrsDetails.getSecond().setCalCulateQutTax(Boolean.TRUE);
					
				OptionalTax = aitpTax.get().getCovAndAitpTax().get(classOfvehicle) *seats ;
				}else {
					OptionalTax = aitpTax.get().getCovAndVoluntaryTax().get(classOfvehicle) * seats;
				}
			}
			}
			if (vcrDetails.getVcrTax() != null && vcrDetails.getVcrTax() > 0 && OptionalTax<=vcrDetails.getVcrTax()) {
				OptionalTax = vcrDetails.getVcrTax();
			}
			boolean intrastate= Boolean.FALSE;
			if(listOfVcrsDetails.getSecond().isIntrastate()||listOfVcrsDetails.getSecond().isNocIssued()) {
				if(listOfVcrsDetails.getSecond().isIntrastate()) {
					if(listOfVcrsDetails.getSecond().getIntrastateVcrDate()!=null) {
						 revokedMonthpositionInQuater = getMonthposition(
								 listOfVcrsDetails.getSecond().getIntrastateVcrDate().toLocalDate());
					}
					valu = revokedMonthpositionInQuater.getFirst();
				}
				intrastate = Boolean.TRUE;
				listOfVcrsDetails.getSecond().setCalCulateQutTax(Boolean.TRUE);
			}
			if (listOfVcrsDetails.getSecond().isVehicleSized()) {
				if (valu == 0) {
					if(vcrDto.getRegistration().isOtherState()) {
						if(vcrDto.isVehicleHaveAitp()|| otherStateQutTax.isPresent() ||intrastate) {
							 penalityTax = ((OptionalTax * 50) / 100);
						}else {
							if(!otherStateQutTax.isPresent()) {
							OptionalTax = OptionalTax/3;
							 penalityTax = OptionalTax;//((OptionalTax * 50) / 100);
							}
						}
					}else {
						Pair<Double, Double> pairOfTax = getTaxWithVcr(OptionalTax, penalityTax, OptionalTax, null, 3, 0,
								listOfVcrsDetails.getSecond().isVehicleSized());
						OptionalTax = pairOfTax.getFirst();
						penalityTax = pairOfTax.getSecond();
					}
					
					
				} else if (valu == 1) {
					if(vcrDto.getRegistration().isOtherState()) {
						if(vcrDto.isVehicleHaveAitp()|| otherStateQutTax.isPresent() ||intrastate) {
							 penalityTax = ((OptionalTax * 100) / 100);
						}else {
							
							OptionalTax = OptionalTax/3;
							 penalityTax = OptionalTax;//((OptionalTax * 50) / 100);
							
						}
					}else {
						Pair<Double, Double> pairOfTax = getTaxWithVcr(OptionalTax, penalityTax, OptionalTax, null, 2, 100,
								listOfVcrsDetails.getSecond().isVehicleSized());
						OptionalTax = pairOfTax.getFirst();
						penalityTax = pairOfTax.getSecond();
					}
				
				} else {
					if(vcrDto.getRegistration().isOtherState()) {
						if(vcrDto.isVehicleHaveAitp()|| otherStateQutTax.isPresent() ||intrastate) {
							 penalityTax = ((OptionalTax * 200) / 100);
						}else {
							OptionalTax = OptionalTax/3;
							 penalityTax = OptionalTax;//((OptionalTax * 50) / 100);
						}
					}else {
						Pair<Double, Double> pairOfTax = getTaxWithVcr(OptionalTax, penalityTax, OptionalTax, null, 1, 200,
								listOfVcrsDetails.getSecond().isVehicleSized());
						OptionalTax = pairOfTax.getFirst();
						penalityTax = pairOfTax.getSecond();
					}
					
				}
			} else {
				if (valu == 0) {
					if(vcrDto.getRegistration().isOtherState()) {
						if(vcrDto.isVehicleHaveAitp()|| otherStateQutTax.isPresent() ||intrastate) {
							 penalityTax = ((OptionalTax * 50) / 100);
						}else {
							if(!otherStateQutTax.isPresent()) {
							OptionalTax = OptionalTax/3;
							 penalityTax = OptionalTax;//((OptionalTax * 50) / 100);
							}
						}
					}
				} else if (valu == 1) {
					if(vcrDto.getRegistration().isOtherState()) {
						if(vcrDto.isVehicleHaveAitp()|| otherStateQutTax.isPresent() ||intrastate) {
							 penalityTax = ((OptionalTax * 100) / 100);
						}else {
							OptionalTax = OptionalTax/3;
							 penalityTax = OptionalTax;//((OptionalTax * 50) / 100);
						}
					}else {
						 penalityTax = ((OptionalTax * 100) / 100);
					}
				

				} else {
					if(vcrDto.getRegistration().isOtherState()) {
						if(vcrDto.isVehicleHaveAitp()|| otherStateQutTax.isPresent() ||intrastate) {
							 penalityTax = ((OptionalTax * 200) / 100);
						}else {
							OptionalTax = OptionalTax/3;
							 penalityTax = OptionalTax;//((OptionalTax * 50) / 100);
							}
					}else {
						 penalityTax = ((OptionalTax * 200) / 100);
					}
				

				}
			}
			if(otherStateQutTax.isPresent()) 
				// penalityTax = 0d;
			
			setObjectAsNull(null, null, null, listTaxType, null);
			return returnTaxDetails(OptionalTax, taxArrAndPenality.getFirst(), penalityTax,
					taxArrAndPenality.getSecond(), 0d,null);
		}

	}

	@Override
	public LocalDate getQuarterStatrtDate(Integer indexPosition, Integer quaternNumber, LocalDate dateOfCheck) {
		LocalDate localDate = null;
		if (quaternNumber == 1) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
			String date1 = "01-04-" + String.valueOf(dateOfCheck.getYear());
			localDate = LocalDate.parse(date1, formatter);
		} else if (quaternNumber == 2) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
			String date1 = "01-07-" + String.valueOf(dateOfCheck.getYear());
			localDate = LocalDate.parse(date1, formatter);
		} else if (quaternNumber == 3) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
			String date1 = "01-10-" + String.valueOf(dateOfCheck.getYear());
			localDate = LocalDate.parse(date1, formatter);
		} else if (quaternNumber == 4) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
			String date1 = "01-01-" + String.valueOf(dateOfCheck.getYear());
			localDate = LocalDate.parse(date1, formatter);
		}
		return localDate;
	}

	public Pair<Double, Double> vcrPenality(Double OptionalTax, Long totalMonths, boolean vehicleSized,
			LocalDate sizedDate, TaxHelper vcrDetails, LocalDate firstQuaterstarts) {
		Double penality = 0d;
		Double finalquaterTax = 0d;
		Long vcrNewCovMonths = 0l;
		if (vehicleSized) {
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
			String date1 = "01-" + String.valueOf(sizedDate.getMonthValue()) + "-"
					+ String.valueOf(sizedDate.getYear());
			LocalDate localDate = LocalDate.parse(date1, formatter);
			Long sizedMonths = ChronoUnit.MONTHS.between(localDate, LocalDate.now());
			sizedMonths = sizedMonths - 1;
			if (sizedMonths < 0) {
				sizedMonths = 0l;
			}
			Long totalUnSizedMonths = (totalMonths - 3) - sizedMonths;

			if (vcrDetails.getVcrTax() != null && vcrDetails.getVcrTax() > 0 && OptionalTax<=vcrDetails.getVcrTax()) {
				if (vcrDetails.getSizedDate().isAfter(vcrDetails.getPlayedAsQuarterEnd())) {
					Long newCvoTaxPenality = ChronoUnit.MONTHS.between(vcrDetails.getPlayedAsQuarterEnd(),
							vcrDetails.getSizedDate());
					newCvoTaxPenality = Math.abs(newCvoTaxPenality);
					newCvoTaxPenality += 1;
					vcrNewCovMonths = newCvoTaxPenality;
					if (newCvoTaxPenality > 0) {
						if (newCvoTaxPenality > totalUnSizedMonths) {
							newCvoTaxPenality = newCvoTaxPenality - totalUnSizedMonths;// set obt as 0
							totalUnSizedMonths = 0l;
						} else {
							totalUnSizedMonths = totalUnSizedMonths - newCvoTaxPenality;
						}
					}
				}
			}
			double quaters = totalUnSizedMonths / 3d;
			double quatersRound = Math.ceil(totalUnSizedMonths / 3);
			if (quaters == quatersRound) {
				penality = (((OptionalTax * 200) / 100) * quaters);
				finalquaterTax = (OptionalTax * quaters);
			} else {
				String numberD = String.valueOf(quaters);
				String number = numberD.substring(numberD.indexOf("."));
				String num = number.substring(1, 4);
				String val = numberD.substring(0, numberD.indexOf("."));
				Double taxPerMonth = OptionalTax / 3d;
				if (num.equalsIgnoreCase("666")) {
					penality = (((OptionalTax * 200) / 100) * Double.valueOf(val));
					finalquaterTax = (OptionalTax * Double.valueOf(val));
					penality = penality + ((taxPerMonth * 2) * 200 / 100);
					finalquaterTax = finalquaterTax + (taxPerMonth * 2);
				} else {
					penality = (((OptionalTax * 200) / 100) * Double.valueOf(val));
					finalquaterTax = (OptionalTax * Double.valueOf(val));
					penality = penality + ((taxPerMonth * 1) * 200 / 100);
					finalquaterTax = finalquaterTax + (taxPerMonth * 1);
				}
			}

			if (vcrNewCovMonths != 0 && vcrNewCovMonths > 0) {
				double Penalityquaters = vcrNewCovMonths / 3d;
				double PenalityQuatersRound = Math.ceil(vcrNewCovMonths / 3d);
				if (Penalityquaters == PenalityQuatersRound) {
					Pair<Double, Double> taxArr2AndPenality = chassisPenalitTax(vcrDetails.getVcrTax(),
							(Penalityquaters), vcrDetails.isVcr());
					finalquaterTax = finalquaterTax + taxArr2AndPenality.getFirst();
					penality = penality + taxArr2AndPenality.getSecond();
				} else {
					String numberD = String.valueOf(Penalityquaters);
					String number = numberD.substring(numberD.indexOf("."));
					String num = number.substring(1, 4);
					String val = numberD.substring(0, numberD.indexOf("."));
					Double taxPerMonth = vcrDetails.getVcrTax() / 3d;
					if (num.equalsIgnoreCase("666")) {
						penality = penality + ((((vcrDetails.getVcrTax() * 200) / 100) * Double.valueOf(val)));
						finalquaterTax = finalquaterTax + ((vcrDetails.getVcrTax() * Double.valueOf(val)));
						penality = penality + ((taxPerMonth * 2) * 200 / 100);
						finalquaterTax = finalquaterTax + (taxPerMonth * 2);
					} else {
						penality = penality + ((((vcrDetails.getVcrTax() * 200) / 100) * Double.valueOf(val)));
						finalquaterTax = finalquaterTax + ((vcrDetails.getVcrTax() * Double.valueOf(val)));
						penality = penality + ((taxPerMonth * 1) * 200 / 100);
						finalquaterTax = finalquaterTax + (taxPerMonth * 1);
					}
				}
			}
		} else {
			if (vcrDetails.getVcrTax() != null && vcrDetails.getVcrTax() > 0 && OptionalTax<=vcrDetails.getVcrTax()) {
				if (vcrDetails.getPlayedAsQuarterEnd().isAfter(validity(""))) {
					Long newCvoTaxPenality = ChronoUnit.MONTHS.between(vcrDetails.getPlayedAsQuarterEnd(),
							validity(""));
					newCvoTaxPenality = Math.abs(newCvoTaxPenality);
					newCvoTaxPenality += 1;
					newCvoTaxPenality = newCvoTaxPenality - 3;
					double Penalityquaters = newCvoTaxPenality / 3d;
					double PenalityQuatersRound = Math.ceil(newCvoTaxPenality / 3d);
					if (Penalityquaters == PenalityQuatersRound) {
						Pair<Double, Double> taxArr2AndPenality = chassisPenalitTax(vcrDetails.getVcrTax(),
								(Penalityquaters), vcrDetails.isVcr());
						finalquaterTax = finalquaterTax + taxArr2AndPenality.getFirst();
						penality = penality + taxArr2AndPenality.getSecond();
					} else {
						String numberD = String.valueOf(Penalityquaters);
						String number = numberD.substring(numberD.indexOf("."));
						String num = number.substring(1, 4);
						String val = numberD.substring(0, numberD.indexOf("."));
						Double taxPerMonth = vcrDetails.getVcrTax() / 3d;
						if (num.equalsIgnoreCase("666")) {
							penality = (((vcrDetails.getVcrTax() * 200) / 100) * Double.valueOf(val));
							finalquaterTax = (vcrDetails.getVcrTax() * Double.valueOf(val));
							penality = penality + ((taxPerMonth * 2) * 200 / 100);
							finalquaterTax = finalquaterTax + (taxPerMonth * 2);
						} else {
							penality = (((vcrDetails.getVcrTax() * 200) / 100) * Double.valueOf(val));
							finalquaterTax = (vcrDetails.getVcrTax() * Double.valueOf(val));
							penality = penality + ((taxPerMonth * 1) * 200 / 100);
							finalquaterTax = finalquaterTax + (taxPerMonth * 1);
						}
					}
					totalMonths = ChronoUnit.MONTHS.between(firstQuaterstarts, vcrDetails.getPlayedAsQuarterEnd());
					totalMonths = totalMonths + 3;
				} else {
					OptionalTax = vcrDetails.getVcrTax();
				}
			}
			penality = (((OptionalTax * 200) / 100) * (totalMonths - 3));
			finalquaterTax = (OptionalTax * (totalMonths - 3));
			return Pair.of(finalquaterTax, penality);
			// return taxArr;
		}
		return Pair.of(finalquaterTax, penality);
	}

	private void setObjectAsNull(StagingRegistrationDetailsDTO stagingRegistrationDetailsDTO,
			RegistrationDetailsDTO registrationDetailsDTO, RegServiceDTO regServiceDTO, List<String> list,
			List<TaxDetailsDTO> listOfTax) {
		if (stagingRegistrationDetailsDTO != null) {
			stagingRegistrationDetailsDTO = null;
		}
		if (registrationDetailsDTO != null) {
			registrationDetailsDTO = null;
		}
		if (regServiceDTO != null) {
			regServiceDTO = null;
		}
		if (list != null) {
			list = null;
		}
		if (listOfTax != null) {
			listOfTax = null;
		}
	}

	private Pair<Long, Map<Double, Double>> getUnSizedMontsAndAmount(Long unSizedMonths, Double quarterTax,
			Double taxPerMonths, int percentage, int months) {
		Double penaltyArrears = 0d;
		Double taxArrears = 0d;
		Map<Double, Double> arrearsAndPenality = new HashMap<>();
		if (months > 0) {

			penaltyArrears = ((taxPerMonths * unSizedMonths) * percentage) / 100;
			unSizedMonths -= months;
			taxArrears = taxPerMonths * unSizedMonths;
		}
		arrearsAndPenality.put(penaltyArrears, taxArrears);
		return Pair.of(unSizedMonths, arrearsAndPenality);
	}

	private Pair<Double, Double> getTaxWithVcr(Double quaterTax, Double penality, Double OptionalTax,
			Double newPermitTax, int monthPosition, int percent, TaxHelper vcrDetails) {
		quaterTax = getTaxWithplayasCov(OptionalTax, vcrDetails, monthPosition);
		penality = 0d;
		// quaterTax = OptionalTax;
		return Pair.of(quaterTax, penality);
	}

	private Double getTaxWithplayasCov(Double OptionalTax, TaxHelper vcrDetails, int noOfMonths) {
		Double quaterTax;
		double oldTaxPerMonth = 0d;
		double newTaxPerMonth = 0d;
		quaterTax = OptionalTax;
		if (vcrDetails.getVcrTax() != null && vcrDetails.getVcrTax() > 0) {
			oldTaxPerMonth = OptionalTax / 3d;
			newTaxPerMonth = vcrDetails.getVcrTax() / 3d;
			quaterTax = (newTaxPerMonth * noOfMonths) - (oldTaxPerMonth * noOfMonths);
			return quaterTax;
		}
		return 0d;
	}

	private TaxHelper getVcrLifeTax(Pair<List<VcrFinalServiceDTO>, CitizenFeeDetailsInput> listOfVcrsDetails) {

		Double penality = 0d;
		Double totalLifeTax = 0d;
		LocalDate taxValidity = lifTaxValidityCal();
		/*
		 * if(listOfVcrsDetails.getSecond().isOtherStateVehicle()&&listOfVcrsDetails.
		 * getSecond().isUnRegestered()) {
		 * 
		 * }else if(listOfVcrsDetails.getSecond().isOtherStateVehicle() &&
		 * !listOfVcrsDetails.getSecond().isUnRegestered()) {
		 * 
		 * }else { //TODO with in the state. }
		 */
		// TaxHelper vcrDteails = this.vcrIntegration(listOfVcrsDetails, true);
		VcrFinalServiceDTO vcrDto = listOfVcrsDetails.getFirst().stream().findFirst().get();
		String cov = vcrDto.getRegistration().getClasssOfVehicle().getCovcode();
		if (vcrDto.getRegistration().getOwnerType() == null) {
			logger.error("Owner type missing for: " + vcrDto.getVcr().getVcrNumber());
			throw new BadRequestException("Owner type missing for: " + vcrDto.getVcr().getVcrNumber());
		}
		String ownerType = vcrDto.getRegistration().getOwnerType().getCode();
		if(checkTaxPaidAtVcr(vcrDto.getRegistration().getChassisNumber())) {
			return returnTaxDetails(0d, 0d, 0d, 0d, 0d,null);
		}
		if (listOfVcrsDetails.getSecond().isUnRegestered()) {
			Optional<MasterTax> OptionalLifeTax = taxTypeDAO
					.findByCovcodeAndOwnershiptypeIgnoreCaseAndStatecodeAndStatusAndFromage(cov, ownerType, stateCode,
							regStatus, freshVehicleAge);
			if (!OptionalLifeTax.isPresent()) {
				logger.error("No record found in master_tax for: " + cov + "and" + ownerType);
				throw new BadRequestException("No record found in master_tax for: " + cov + "and" + ownerType);
			}
			if (vcrDto.getRegistration().getInvoiceAmmount() == null
					|| vcrDto.getRegistration().getInvoiceAmmount() <= 0) {
				logger.error("Invoice amount not found for: " + vcrDto.getVcr().getVcrNumber());
				throw new BadRequestException("Invoice amount not found for: " + vcrDto.getVcr().getVcrNumber());
			}
			Optional<MasterAmountSecoundCovsDTO> basedOnInvoice;
			Optional<MasterAmountSecoundCovsDTO> basedOnsecoundVehicle;
			List<MasterAmountSecoundCovsDTO> masterAmountSecoundCovsDTO = masterAmountSecoundCovsDAO.findAll();
			basedOnInvoice = masterAmountSecoundCovsDTO.stream()
					.filter(m -> m.getAmountcovcode().contains(cov))
					.findFirst();
			basedOnsecoundVehicle = masterAmountSecoundCovsDTO.stream()
					.filter(m -> m.getSecondcovcode().contains(cov))
					.findFirst();
			
			
					if (vcrDto.getRegistration().getInvoiceAmmount() > amount
							&& basedOnInvoice.isPresent() && ownerType.equalsIgnoreCase(OwnerTypeEnum.Individual.getCode())) {

						totalLifeTax = (vcrDto.getRegistration().getInvoiceAmmount()
								* basedOnInvoice.get().getTaxpercentinvoice() / 100d);

					
					} else if (basedOnsecoundVehicle.isPresent() && vcrDto.getRegistration().getFirstVehicle()!= null && !vcrDto.getRegistration().getFirstVehicle()) {

						totalLifeTax = (vcrDto.getRegistration().getInvoiceAmmount()
								* basedOnsecoundVehicle.get().getSecondvehiclepercent() / 100d);
					
					} else {

						totalLifeTax = (vcrDto.getRegistration().getInvoiceAmmount() * OptionalLifeTax.get().getPercent() / 100d);
					}
				
			

		
			if (totalLifeTax==0f) {
				/// setObjectAsNull(stagingRegistrationDetails, registrationDetails,
				/// regServiceDTO,null,null);
				return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), 0l, 0d, taxValidity, 0l, 0d,
						TaxTypeEnum.TaxPayType.REG, "", 0d,null);
			}
			if (vcrDto.getRegistration().getTaxCalculationDateForLifeTax() == null) {
				logger.error("Tax calculation date  not found for: " + vcrDto.getVcr().getVcrNumber());
				throw new BadRequestException("Tax calculation date  not found for: " + vcrDto.getVcr().getVcrNumber());
			}

			List<MasterTaxFuelTypeExcemptionDTO> list = masterTaxFuelTypeExcemptionDAO.findAll();
			if (list.stream().anyMatch(type -> type.getFuelType().stream()
					.anyMatch(fuel -> fuel.equalsIgnoreCase(vcrDto.getRegistration().getFuelDesc())))) {
			//	Double doubleResult = Double.valueOf(Float.valueOf(totalLifeTax).toString()).doubleValue();
				totalLifeTax = batteryDiscount(vcrDto.getRegistration().getInvoiceAmmount().doubleValue(), totalLifeTax,
						OptionalLifeTax.get().getPercent(), list, vcrDto.getRegistration().getFuelDesc());

			}
		} else {
			if(vcrDto.getRegistration().isOtherState() && vcrDto.isAnnualTax()){
				Optional<MasterTaxForVoluntary>  vlountaryTax = masterTaxForVoluntaryDAO.findByCovsAndToUlwGreaterThanEqualAndFromUlwLessThanEqualAndStatusTrue(
						cov, vcrDto.getRegistration().getUlw(), vcrDto.getRegistration().getUlw());
				 if(vlountaryTax.isPresent()) {

						long tax = roundUpperTen(vlountaryTax.get().getOneYearTax());
						return returnTaxDetails(TaxTypeEnum.VoluntaryTaxType.OneYear.getTaxType(), tax, 0d,LocalDate.now().plusDays(TaxTypeEnum.VoluntaryTaxType.OneYear.getDays()) , 0l, 0d,
								TaxTypeEnum.TaxPayType.REG, "", 0d,null);
						
					}
			}
			if (vcrDto.getRegistration().getPrGeneratedDate() == null) {
				logger.error("Vehicle registered date  not found for: " + vcrDto.getVcr().getVcrNumber());
				throw new BadRequestException(
						"Vehicle registered date  not found for: " + vcrDto.getVcr().getVcrNumber());
			}
			double vehicleAge = calculateAgeOfTheVehicle(vcrDto.getRegistration().getPrGeneratedDate(),
					LocalDate.now());
			Optional<MasterTax> OptionalLifeTax = taxTypeDAO
					.findByCovcodeAndOwnershiptypeIgnoreCaseAndStatecodeAndStatusAndToageGreaterThanEqualAndFromageLessThanEqualAndTocostGreaterThanEqualAndFromcostLessThanEqual(
							cov, ownerType, stateCode, regStatus, vehicleAge, vehicleAge,
							vcrDto.getRegistration().getInvoiceAmmount().doubleValue(),
							vcrDto.getRegistration().getInvoiceAmmount().doubleValue());
			if (!OptionalLifeTax.isPresent()) {
				logger.error("No record found in master_tax for: " + cov + "and" + ownerType);
				throw new BadRequestException("No record found in master_tax for: " + cov + "and" + ownerType);
			}
			totalLifeTax = (vcrDto.getRegistration().getInvoiceAmmount().doubleValue()
					* OptionalLifeTax.get().getPercent() / 100f);
			taxValidity = lifTaxValidityCalForRegVeh(vcrDto.getRegistration().getPrGeneratedDate());
		}
		penality = otherStateNeedToPayPenality(vcrDto.getRegistration().getTaxCalculationDateForLifeTax(), totalLifeTax,
				true, listOfVcrsDetails.getSecond().isVehicleSized(),
				listOfVcrsDetails.getSecond().getVehicleSizedDate());

		long tax = roundUpperTen(totalLifeTax);
		return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), tax, penality, taxValidity, 0l, 0d,
				TaxTypeEnum.TaxPayType.REG, "", 0d,null);

	}

	private boolean checkTaxPaidAtVcr(String  chassisNo) {
		List<TaxDetailsDTO> listOfTax = taxDetailsDAO
				.findFirst10ByChassisNoOrderByCreatedDateDesc(chassisNo);
		if (listOfTax != null && !listOfTax.isEmpty()) {
			List<TaxDetailsDTO> vcrTax = listOfTax.stream()
					.filter(tax -> tax.getTaxPaidThroughVcr() != null && tax.getTaxPaidThroughVcr())
					.collect(Collectors.toList());
			if (vcrTax != null && !vcrTax.isEmpty()) {
				List<TaxDetailsDTO> lifTax = vcrTax.stream()
						.filter(tax -> tax.getPaymentPeriod() != null
								&& tax.getPaymentPeriod().equalsIgnoreCase(TaxTypeEnum.LifeTax.getDesc()))
						.collect(Collectors.toList());
				if (lifTax != null && !lifTax.isEmpty()) {

					return true;

				}
			}
		}
		return false;
	}

	private boolean checkTaxPaidAtVoluntary(String chassisNo) {
		List<VoluntaryTaxDTO> voluntaryTaxDTOList = null;
		if (CollectionUtils.isEmpty(voluntaryTaxDTOList)) {
			voluntaryTaxDTOList = voluntaryTaxDAO.findFirst2ByChassisNoOrderByCreatedDateDesc(chassisNo);
		}
		if (voluntaryTaxDTOList != null && !voluntaryTaxDTOList.isEmpty()) {

			List<VoluntaryTaxDTO> voluntaryTax = voluntaryTaxDTOList.stream().filter(
					tax -> tax.getTaxType() != null && tax.getTaxType().equalsIgnoreCase(TaxTypeEnum.LifeTax.getDesc()))
					.collect(Collectors.toList());
			if (voluntaryTax != null && !voluntaryTax.isEmpty()) {

				return true;
			}

		}
		return false;
	}
	

	@Override
	public Integer getUlwWeight(RegistrationDetailsDTO registrationDetails) {
		Integer ulw = registrationDetails.getVahanDetails().getUnladenWeight();

		if (ClassOfVehicleEnum.ATCHN.getCovCode().equalsIgnoreCase(registrationDetails.getClassOfVehicle())) {
			if (registrationDetails.getVahanDetails() != null
					&& registrationDetails.getVahanDetails().getHarvestersDetails() != null
					&& registrationDetails.getVahanDetails().getHarvestersDetails().getUlw() != null) {
				ulw = registrationDetails.getVahanDetails().getHarvestersDetails().getUlw()
						+ registrationDetails.getVahanDetails().getUnladenWeight();
			}
			return ulw;
		} else if (ClassOfVehicleEnum.SPHN.getCovCode().equalsIgnoreCase(registrationDetails.getClassOfVehicle())) {
			if (registrationDetails.getVahanDetails() != null
					&& registrationDetails.getVahanDetails().getHarvestersDetails() != null
					&& registrationDetails.getVahanDetails().getHarvestersDetails().getUlw() != null) {
				ulw = registrationDetails.getVahanDetails().getHarvestersDetails().getUlw()
						+ registrationDetails.getVahanDetails().getUnladenWeight();
			}
			return ulw;
		}else if (ClassOfVehicleEnum.TMRN.getCovCode().equalsIgnoreCase(registrationDetails.getClassOfVehicle())) {
			if (registrationDetails.getVahanDetails() != null
					&& registrationDetails.getVahanDetails().getHarvestersDetails() != null
					&& registrationDetails.getVahanDetails().getHarvestersDetails().getUlw() != null) {
				ulw = registrationDetails.getVahanDetails().getHarvestersDetails().getUlw()
						+ registrationDetails.getVahanDetails().getUnladenWeight();
			}
			return ulw;
		}
		return ulw;
	}

	@Override
	public TaxHelper getVoluntaryTax(String prNo, String trNo, String cov, Integer gvw, Integer ulw, String seats,
			String makersModel, Double invoiceValue, String fuelDesc, LocalDate prGenarationDate,
			boolean otherStateUnregister, boolean otherStateRegister, boolean unregisteredVehicle,
			OwnerTypeEnum ownerType, boolean isFirstVehicle, LocalDate dateOfCompletion, String taxType,
			boolean nocIssued, boolean withTP, LocalDate nocDate, LocalDate fcValidity, boolean vehicleHaveAitp) {
		TaxHelper tax = new TaxHelper();
		if (StringUtils.isBlank(cov) || StringUtils.isBlank(seats)
				|| gvw == null/* ||StringUtils.isBlank(makersModel) */) {
			logger.error("Please provide cov/seats/gvw/makersModel");
			throw new BadRequestException("Please provide cov/seats/gvw/makersModel");
		}
		if ((cov.equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())
				|| (cov.equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode()) && gvw <= 3000)
				|| cov.equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())
				|| cov.equalsIgnoreCase(ClassOfVehicleEnum.TRTT.getCovCode())
				|| cov.equalsIgnoreCase(ClassOfVehicleEnum.TRCN.getCovCode()))) {
			return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), 0l, 0d, lifTaxValidityCal(), 0l, 0d,
					TaxTypeEnum.TaxPayType.REG, "", 0d,null);
		}
		if ((cov.equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
				|| cov.equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode())) && unregisteredVehicle) {
			logger.error(
					"Please select another class of vehicle ,which is body build class of vehicle.Selected cov is: ",
					cov);
			throw new BadRequestException(
					"Please select another class of vehicle ,which is body build.Selected cov is: " + cov);
		}
		if (StringUtils.isNoneBlank(makersModel)) {
			Optional<MasterTaxExcemptionsDTO> optionalTaxExcemption = masterTaxExcemptionsDAO
					.findByKeyvalueOrCovcode(makersModel, cov);
			if (optionalTaxExcemption.isPresent()) {
				return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(),
						optionalTaxExcemption.get().getTaxvalue().longValue(), 0d, lifTaxValidityCal(), 0l, 0d,
						TaxTypeEnum.TaxPayType.REG, "", 0d,null);
			}
		}
		if (StringUtils.isBlank(taxType)) {
			logger.error("Please select tax type");
			throw new BadRequestException("Please select tax type");
		}
		LocalDate taxValidity = vcrService.getVoluntaryTaxValidity(taxType, prGenarationDate);
		tax.setValidityTo(taxValidity);
		if (!nocIssued && !unregisteredVehicle) {
			Optional<PropertiesDTO> propertiesOptional = propertiesDAO
					.findByPayperiodAndVoluntaryPayPeriodTrue(TaxTypeEnum.QuarterlyTax.getCode());
			PropertiesDTO properties = propertiesOptional.get();
			if (properties.getCovAndVoluntaryTax().containsKey(cov)) {
				Double seatsTax = 0d;
				if (vehicleHaveAitp) {
					tax.setTaxPayType(TaxTypeEnum.TaxPayType.REG);
					tax.setPermitType(PermitsEnum.PermitCodes.AITP.getPermitCode());
					Pair<Integer, Integer> montsPosition = this.getMonthposition(LocalDate.now());
					Double OptionalTax = properties.getCovAndAitpTax().get(cov) * (Double.parseDouble(seats)-1);
					//Double currentTaxPerMOnth = OptionalTax / 3d;
					if (montsPosition.getFirst() == 0) {
						// OptionalTax = OptionalTax;
					} else if (montsPosition.getFirst() == 1) {
						//OptionalTax = currentTaxPerMOnth * 2;
					} else {
						//OptionalTax = currentTaxPerMOnth;
					}
					if(cov.equalsIgnoreCase(ClassOfVehicleEnum.MTLT.getCovCode())) {
						OptionalTax = properties.getCovAndAitpTax().get(cov).doubleValue();
					}
					if(taxType.equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.Halfyearly.getDesc())) {
						OptionalTax = OptionalTax*2d;
					}else if(taxType.equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.Annual.getDesc())) {
						OptionalTax = OptionalTax*4d;
					}
					tax.setTaxAmountForPayments(this.roundUpperTen(OptionalTax));
					tax.setTaxName(TaxTypeEnum.VoluntaryTaxType.getType(taxType));
					return tax;
				} else {
					tax.setTaxPayType(TaxTypeEnum.TaxPayType.REG);
					tax.setPermitType(permitcode);
					if(cov.equalsIgnoreCase(ClassOfVehicleEnum.MTLT.getCovCode())) {
						seatsTax = properties.getCovAndVoluntaryTax().get(cov).doubleValue();
					}else {
					 seatsTax = (properties.getCovAndVoluntaryTax().get(cov)* (Double.parseDouble(seats)-1));
					}
					tax.setTaxAmountForPayments(seatsTax.longValue());
					tax.setTaxName(TaxTypeEnum.VoluntaryTaxType.getType(taxType));
					return tax;
				}
			}
		}
	
			if(otherStateRegister && taxType.equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.OneYear.getDesc())){
				Optional<MasterTaxForVoluntary>  vlountaryTax = masterTaxForVoluntaryDAO.findByCovsAndToUlwGreaterThanEqualAndFromUlwLessThanEqualAndStatusTrue(cov, ulw, ulw);
				 if(vlountaryTax.isPresent()) {
						tax.setTaxPayType(TaxTypeEnum.TaxPayType.REG);
						tax.setPermitType(permitcode);
						tax.setTaxName(TaxTypeEnum.VoluntaryTaxType.getType(taxType));
						tax.setTaxAmountForPayments(vlountaryTax.get().getOneYearTax().longValue());
						return tax;
					}
			}
			
		
		Optional<MasterPayperiodDTO> Payperiod = masterPayperiodDAO.findByCovcode(cov);

		if (!Payperiod.isPresent()) {
			// throw error message
			logger.error("No record found in master_payperiod for:[{}] " + cov);
			throw new BadRequestException("No record found in master_payperiod for: " + cov);

		}
		if(!nocIssued && !unregisteredVehicle && (cov.equalsIgnoreCase(ClassOfVehicleEnum.MTLT.getCovCode())||cov.equalsIgnoreCase(ClassOfVehicleEnum.LTCT.getCovCode())
				||cov.equalsIgnoreCase(ClassOfVehicleEnum.STCT.getCovCode()))) {
			Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
		}
		if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.BOTH.getCode())) {
			getPayPeroidForBoth(Payperiod, seats, gvw);
		}
		if(Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.LifeTax.getCode()) && !nocIssued && !unregisteredVehicle && cov.equalsIgnoreCase(ClassOfVehicleEnum.GCRT.getCovCode())) {
			Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
		}
		RegServiceDTO regServiceDTO = new RegServiceDTO();
		RegistrationDetailsDTO regDto = new RegistrationDetailsDTO();
		VahanDetailsDTO vahanDto = new VahanDetailsDTO();
		InvoiceDetailsDTO invoice = new InvoiceDetailsDTO();
		NOCDetailsDTO nocDto = new NOCDetailsDTO();
		vahanDto.setGvw(gvw);
		vahanDto.setUnladenWeight(ulw);
		vahanDto.setSeatingCapacity(seats);
		vahanDto.setFuelDesc(fuelDesc);
		vahanDto.setMakersModel(makersModel);
		regDto.setVahanDetails(vahanDto);
		regDto.setClassOfVehicle(cov);
		invoice.setInvoiceValue(invoiceValue);
		regDto.setInvoiceDetails(invoice);
		if (ownerType == null) {
			regDto.setOwnerType(OwnerTypeEnum.Individual);
		} else {
			regDto.setOwnerType(ownerType);
		}
		regDto.setRegVehicleWithPR(otherStateRegister);
		regDto.setRegVehicleWithTR(otherStateUnregister);
		regDto.setClassOfVehicle(cov);
		regDto.setPrIssueDate(prGenarationDate);
		regDto.setIsFirstVehicle(isFirstVehicle);
		regServiceDTO.setRegistrationDetails(regDto);
		nocDto.setIssueDate(LocalDate.now());
		if (nocIssued) {
			if (nocDate == null) {
				logger.error("Please provide noc date ");
				throw new BadRequestException("Please provide noc date ");
			}
			nocDto.setIssueDate(nocDate);
		}

		nocDto.setDateOfEntry(LocalDate.now());
		regServiceDTO.setnOCDetails(nocDto);
		regServiceDTO.setPrNo(prNo);
		regServiceDTO.getRegistrationDetails().setTrNo(trNo);
		switch (TaxTypeEnum.getTaxTypeEnumByCode(Payperiod.get().getPayperiod())) {

		case LifeTax:

			List<MasterAmountSecoundCovsDTO> masterAmountSecoundCovsDTO = masterAmountSecoundCovsDAO.findAll();
			if (otherStateRegister || otherStateUnregister) {
				if(otherStateRegister && taxType.equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.OneYear.getDesc())){
					Optional<MasterTaxForVoluntary>  vlountaryTax = masterTaxForVoluntaryDAO.findByCovsAndToUlwGreaterThanEqualAndFromUlwLessThanEqualAndStatusTrue(cov, ulw, ulw);
					 if(vlountaryTax.isPresent()) {
							tax.setTaxPayType(TaxTypeEnum.TaxPayType.REG);
							tax.setPermitType(permitcode);
							tax.setTaxName(TaxTypeEnum.VoluntaryTaxType.getType(taxType));
							tax.setTaxAmountForPayments(vlountaryTax.get().getOneYearTax().longValue());
							return tax;
							
							
						}
				}
				regDto.setApplicantType("OTHERSTATE");
				tax = getOtherStateLifeTax(false, false, true, regServiceDTO, null, null, TaxTypeEnum.TaxPayType.REG,
						null, masterAmountSecoundCovsDTO, null, false);
			} else if (unregisteredVehicle) {
				if (StringUtils.isBlank(trNo)) {
					logger.error("Please provide trNo");
					throw new BadRequestException("Please provide trNo");
				}
				Optional<StagingRegistrationDetailsDTO> optionalStaging = stagingRegistrationDetailsDAO
						.findByTrNo(trNo);
				if (!optionalStaging.isPresent()) {
					logger.error("No records found for tr: " + trNo);
					throw new BadRequestException("No records found for tr: " + trNo);
				}
				if (!(optionalStaging.get().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
						|| optionalStaging.get().getClassOfVehicle()
								.equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode()))) {
					logger.error(
							"Only chassis vehicle can pay voluntary tax. Please raise VCR for this vehicle if tax not paid upto date : "
									+ trNo);
					throw new BadRequestException(
							"Only chassis vehicle can pay voluntary tax. Please raise VCR for this vehicle if tax not paid upto date: "
									+ trNo);
				}
				Optional<MasterTax> OptionalLifeTax = taxTypeDAO
						.findByCovcodeAndOwnershiptypeIgnoreCaseAndStatecodeAndStatusAndFromage(cov,
								optionalStaging.get().getOwnerType().getCode(), stateCode, regStatus, freshVehicleAge);
				if (!OptionalLifeTax.isPresent()) {
					logger.error(
							"No record found in master_tax for: " + cov + "and" + optionalStaging.get().getOwnerType());
					// throw error message
					throw new BadRequestException(
							"No record found in master_tax for: " + cov + "and" + optionalStaging.get().getOwnerType());
				}
				Double totalLifeTax = ((optionalStaging.get().getInvoiceDetails().getInvoiceValue()
						* OptionalLifeTax.get().getPercent()) / 100f);
				Pair<Long, Double> taxAndpenality = this.finalLifeTaxCalculation(optionalStaging.get(), regServiceDTO,
						regDto, totalLifeTax, OptionalLifeTax.get().getPercent(), false, true, false, false, false,
						null);
				tax = returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), taxAndpenality.getFirst(),
						taxAndpenality.getSecond(), lifTaxValidityCal(), 0l, 0d, TaxTypeEnum.TaxPayType.REG, "", 0d,null);
			} else {
				logger.error("Voluntary tax can pay other sate or home chassis vehicls");
				throw new BadRequestException("Voluntary tax can pay other sate or home chassis vehicls");
			}

			break;
		case QuarterlyTax:
			 Double penality=0d;
			 Long reoundTaxArrears=0l;
			 Double penalityArrears=0d;
			Optional<MasterTaxBased> taxCalBasedOn = Optional.empty();
			if (otherStateRegister || otherStateUnregister) {
				regDto.setApplicantType("OTHERSTATE");
				if(otherStateRegister&& (taxType.equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.SevenDays.getDesc())||
						taxType.equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.ThirtyDays.getDesc()))) {
					Optional<MasterTaxForVoluntary>  vlountaryTax =Optional.empty();
					taxCalBasedOn = masterTaxBasedDAO.findByCovcode(cov);
				if (taxCalBasedOn.get().getBasedon().equalsIgnoreCase(rlwCode)) {
					  vlountaryTax = masterTaxForVoluntaryDAO.findByCovsAndToGvwGreaterThanEqualAndFromGvwLessThanEqualAndStatusTrue(cov, gvw, gvw);
				}else if (taxCalBasedOn.get().getBasedon().equalsIgnoreCase(ulwCode))  {
					  vlountaryTax = masterTaxForVoluntaryDAO.findByCovsAndToUlwGreaterThanEqualAndFromUlwLessThanEqualAndStatusTrue(cov, ulw, ulw);
				}else if (taxCalBasedOn.get().getBasedon().equalsIgnoreCase(seatingCapacityCode))  {
					  vlountaryTax = masterTaxForVoluntaryDAO.findByCovsAndToSeatsGreaterThanEqualAndFromSeatsLessThanEqualAndStatusTrue(cov, seats, seats);
				}
					if(vlountaryTax.isPresent()) {
						tax.setTaxPayType(TaxTypeEnum.TaxPayType.REG);
						tax.setPermitType(permitcode);
						tax.setTaxName(TaxTypeEnum.VoluntaryTaxType.getType(taxType));
						if(taxType.equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.SevenDays.getDesc())) {
							tax.setTaxAmountForPayments(vlountaryTax.get().getSevenDaysTax().longValue());
							return tax;
						}else if(taxType.equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.ThirtyDays.getDesc())){
							tax.setTaxAmountForPayments(vlountaryTax.get().getThirtyDaysTax().longValue());
							return tax;
						}else if(taxType.equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.OneYear.getDesc())){
							tax.setTaxAmountForPayments(vlountaryTax.get().getOneYearTax().longValue());
							return tax;
						}
						
					}
				}
				Double otherStateTax = this.getOldCovTax(cov, seats, ulw, gvw, "AP", permitcode, null);
				long finalTax  = this.roundUpperTen(otherStateTax);
				if(nocIssued) {
					/*if(nocDate==null) {
						logger.error("Please provide noc date");
						throw new BadRequestException("Please provide noc date");
					}*/
					LocalDate entryDate = LocalDate.now();
						entryDate = getEarlerDate(entryDate,nocDate);
						TaxHelper nocTax = getOtherStatePenalityAndTax(otherStateTax, false, entryDate, false);
						finalTax  = this.roundUpperTen(nocTax.getTax());
						//otherStateTax = nocTax.getTax();
						penality = nocTax.getPenalty().doubleValue();
						reoundTaxArrears = nocTax.getTaxArrears().longValue();
						penalityArrears = nocTax.getPenaltyArrears().doubleValue();
						
				}
				
				
				
				/*Optional<PropertiesDTO> otherStateQutTax =	propertiesDAO.findByOtherStateQutTaxCovsInAndOtherStateQutTaxCovsflagTrue(cov);
				if(otherStateQutTax.isPresent()) {
			//	if (this.allowForQuarterTax(cov)) {
					Pair<Integer, Integer>  monthPosition = this.getMonthposition(LocalDate.now());
					Double slabTax =otherStateTax;
					if (monthPosition.getFirst() == 1) {
						slabTax = (slabTax / 3) * 2;
					} else if (monthPosition.getFirst() == 2) {
						slabTax = (slabTax / 3) * 1;
					}
					finalTax = this.roundUpperTen(slabTax);
				}*/
				return returnTaxDetails( TaxTypeEnum.QuarterlyTax.getDesc(), finalTax, penality,
						this.validity(""), reoundTaxArrears,
						penalityArrears, TaxTypeEnum.TaxPayType.REG, permitcode,
						otherStateTax,null);
				/*tax = this.quaterTaxCalculation(null, false, false, TaxTypeEnum.QuarterlyTax.getDesc(), true,
						Arrays.asList(ServiceEnum.VOLUNTARYTAX), permitcode, null, taxCalBasedOn, regServiceDTO, regDto,
						null, cov, TaxTypeEnum.TaxPayType.REG, null, false, null, false, false, false, null);*/

			} else if (unregisteredVehicle) {
				if (StringUtils.isBlank(trNo)) {
					logger.error("Please provide trNo");
					throw new BadRequestException("Please provide trNo");
				}
				Optional<StagingRegistrationDetailsDTO> optionalStaging = stagingRegistrationDetailsDAO
						.findByTrNo(trNo);
				if (!optionalStaging.isPresent()) {
					logger.error("No records found for tr: " + trNo);
					throw new BadRequestException("No records found for tr: " + trNo);
				}
				if (!(optionalStaging.get().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
						|| optionalStaging.get().getClassOfVehicle()
								.equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode()))) {
					logger.error(
							"Only chassis vehicle can pay voluntary tax. Please raise VCR for this vehicle if tax not paid upto date : "
									+ trNo);
					throw new BadRequestException(
							"Only chassis vehicle can pay voluntary tax. Please raise VCR for this vehicle if tax not paid upto date: "
									+ trNo);
				}
				AlterationDTO alterDetails = new AlterationDTO();
				alterDetails.setCov(cov);
				alterDetails.setGvw(gvw);
				alterDetails.setSeating(seats);
				alterDetails.setUlw(ulw);
				alterDetails.setDateOfCompletion(dateOfCompletion);
				regServiceDTO.setAlterationDetails(alterDetails);
				tax = this.quaterTaxCalculation(null, false, true, TaxTypeEnum.QuarterlyTax.getDesc(), false,
						Arrays.asList(ServiceEnum.VOLUNTARYTAX), permitcode, null, taxCalBasedOn, regServiceDTO, regDto,
						optionalStaging.get(), cov, TaxTypeEnum.TaxPayType.REG, Optional.of(alterDetails), false, null,
						false, false, true, LocalDate.now());
			} else {

			}
			// return tax;
			break;
		default:
			break;

		}
		tax.setValidityTo(taxValidity);
		if (TaxTypeEnum.VoluntaryTaxType.BorderTax.getDesc().equalsIgnoreCase(taxType)) {
			tax.setTaxAmountForPayments(240l);
			tax.setTaxName(TaxTypeEnum.VoluntaryTaxType.BorderTax.getTaxType());
		} else {
			if (tax != null && tax.getTax() != null && tax.getTax() > 0) {
				tax.setTaxName(TaxTypeEnum.VoluntaryTaxType.getType(taxType));
				if (!(taxType.equalsIgnoreCase(VoluntaryTaxType.Quarterly.getDesc())
						|| taxType.equalsIgnoreCase(VoluntaryTaxType.Halfyearly.getDesc())
						|| taxType.equalsIgnoreCase(VoluntaryTaxType.Annual.getDesc()))) {
					if ((taxType.equalsIgnoreCase(VoluntaryTaxType.SevenDays.getDesc())
							//|| taxType.equalsIgnoreCase(VoluntaryTaxType.FifteenDays.getDesc())
							|| taxType.equalsIgnoreCase(VoluntaryTaxType.ThirtyDays.getDesc())) && otherStateRegister) {
						if (fcValidity == null) {
							logger.error("Please provide fc validity for: " + prNo);
							throw new BadRequestException("Please provide fc validity for: " + prNo);
						}
						if (fcValidity.isBefore(LocalDate.now())) {
							logger.error("Fc validity expired for: " + prNo);
							throw new BadRequestException("Fc validity expired for: " + prNo);
						}
						if (fcValidity.isBefore(taxValidity)) {
							tax.setValidityTo(fcValidity);
						}
					}
					Optional<PropertiesDTO> payPeriod = propertiesDAO.findByVoluntaryPayPeriodTrue();
					Integer days = payPeriod.get().getTaxType().get(taxType);
					Double taxPerMonth = tax.getTax() / 3d;
					Double taxPerDay = taxPerMonth / 30d;
					tax.setTaxAmountForPayments(this.roundUpperTen(days * taxPerDay));

				}
			}
		}
		return tax;

	}

	private void checkTaxPaidAtBorder(RegServiceDTO regServiceDTO) {
		Optional<VoluntaryTaxDTO> voluntaryTax = null;
		RegServiceVO vo = regServiceMapper.convertEntity(regServiceDTO);
		OtherStateApplictionType applicationType = getOtherStateVehicleStatus(vo);
		if (OtherStateApplictionType.TrNo.equals(applicationType)) {
			voluntaryTax = voluntaryTaxDAO
					.findByTrNoOrderByCreatedDateDesc(regServiceDTO.getRegistrationDetails().getTrNo());
		} else if (OtherStateApplictionType.PrNo.equals(applicationType)) {
			voluntaryTax = voluntaryTaxDAO.findByRegNoOrderByCreatedDateDesc(regServiceDTO.getPrNo());
		}

		if (voluntaryTax != null && voluntaryTax.isPresent() && StringUtils.isNoneBlank(voluntaryTax.get().getTaxType())
				&&(voluntaryTax.get().getTaxType().equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.Quarterly.getDesc())
						||voluntaryTax.get().getTaxType().equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.Halfyearly.getDesc())
						||voluntaryTax.get().getTaxType().equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.Annual.getDesc())
						||voluntaryTax.get().getTaxType().equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.LifeTax.getDesc()))) {
			if (voluntaryTax.get().getTax() != null && voluntaryTax.get().getTaxvalidUpto() != null) {
				if (regServiceDTO.getRegistrationDetails().isTaxPaidByVcr()) {
					if (regServiceDTO.getTaxDetails() != null
							&& regServiceDTO.getTaxDetails().getPaymentDAte() != null) {
						Pair<Integer, Integer> indexPosiAndQuarterNo = this
								.getMonthposition(regServiceDTO.getTaxDetails().getPaymentDAte());
						LocalDate taxValidityForVcr = this.calculateChassisTaxUpTo(indexPosiAndQuarterNo.getFirst(),
								indexPosiAndQuarterNo.getSecond(), regServiceDTO.getTaxDetails().getPaymentDAte());
						if (taxValidityForVcr.isBefore(voluntaryTax.get().getTaxvalidUpto())) {
							regServiceDTO.getTaxDetails().setPaymentDAte(voluntaryTax.get().getTaxvalidUpto());
							regServiceDTO.getTaxDetails().setTaxAmount(voluntaryTax.get().getTax().longValue());
						}
					}
				} else {
					regServiceDTO.getRegistrationDetails().setTaxPaidByVcr(Boolean.TRUE);
					org.epragati.regservice.dto.TaxDetailsDTO taxDto = new org.epragati.regservice.dto.TaxDetailsDTO();
					taxDto.setPaymentDAte(voluntaryTax.get().getTaxvalidUpto());
					taxDto.setTaxAmount(voluntaryTax.get().getTax().longValue());
					regServiceDTO.setTaxDetails(taxDto);
				}
			}
		}
	}

	private void checkTaxPaidAtBorderForChassis(TaxHelper currenTax,
			StagingRegistrationDetailsDTO stagingRegistrationDetails) {

		Optional<VoluntaryTaxDTO> voluntaryTax = voluntaryTaxDAO
				.findByTrNoOrderByCreatedDateDesc(stagingRegistrationDetails.getTrNo());

		if (currenTax.getValidityTo() == null) {
			currenTax.setValidityTo(this.validity(TaxTypeEnum.QuarterlyTax.getDesc()));
		}
		if (voluntaryTax != null && voluntaryTax.isPresent()) {
			if (voluntaryTax.get().getTax() != null && voluntaryTax.get().getTaxvalidUpto() != null) {
				if (currenTax.getValidityTo().equals(voluntaryTax.get().getTaxvalidUpto())) {

					if (currenTax.getTax() > voluntaryTax.get().getTax()) {
						double totalTax = currenTax.getTax() - voluntaryTax.get().getTax();
						currenTax.setTax(totalTax);
						if (currenTax.getPenalty() != null && voluntaryTax.get().getPenalty() != null
								&& currenTax.getPenalty() > voluntaryTax.get().getPenalty()) {
							currenTax.setPenalty(currenTax.getPenalty() - voluntaryTax.get().getPenalty());
						}
					} else {
						currenTax.setPenalty(0l);
						currenTax.setTax(0d);
					}
				} /*
					 * else { if(currenTax.getTax()>voluntaryTax.get().getTax()) { double totalTax =
					 * voluntaryTax.get().getTax()-currenTax.getTax();
					 * currenTax.setPenalty(currenTax.getPenalty()+this.roundUpperTen(totalTax)); }
					 * }
					 */

			}
			if (voluntaryTax.get().getPenaltyArrears() != null) {
				if (currenTax.getPenaltyArrears() > voluntaryTax.get().getPenaltyArrears()) {
					double totalTax = currenTax.getPenaltyArrears() - voluntaryTax.get().getPenaltyArrears();
					currenTax.setPenaltyArrears(this.roundUpperTen(totalTax));
				} else {
					currenTax.setPenaltyArrears(0l);
				}
			}
			if (voluntaryTax.get().getTaxArrears() != null) {
				if (currenTax.getTaxArrears() > voluntaryTax.get().getTaxArrears()) {
					double totalTax = currenTax.getTaxArrears() - voluntaryTax.get().getTaxArrears();
					currenTax.setTaxArrears(totalTax);
				} else {
					currenTax.setTaxArrears(0d);
				}
			}
		}
	}
	private List<VcrFinalServiceDTO> getVcrDocs(List<String> listOfVcrs) {
		List<VcrFinalServiceDTO> vcrList = vcrFinalServiceDAO.findByVcrVcrNumberInAndIsVcrClosedIsFalse(listOfVcrs);
		if (vcrList == null || vcrList.isEmpty()) {
			logger.error("No record found for vcr..[{}]");
			throw new BadRequestException("No record found for vcr : ");
		}
		return vcrList;
	}
	
	private boolean allowForQuarterTax(String covs) {
		Optional<PropertiesDTO> optionalProperties = propertiesDAO.findByAllowQuaterTaxTrue();
		PropertiesDTO properties = optionalProperties.get();
		if (properties.getCovs().stream().anyMatch(cov->cov.equalsIgnoreCase(covs))) {
			return true;
		}
		return false;
	}
	private boolean checkTaxdeductionModeOrNot(TaxHelper vcrDetails) {
		
		LocalDate currentTaxValidity = validity("");
		Pair<Integer, Integer> monthsPosition = this.getMonthposition(currentTaxValidity);
		LocalDate quarterStartDate = this.getQuarterStatrtDate(monthsPosition.getFirst(), monthsPosition.getSecond(), currentTaxValidity);
	
		if(vcrDetails.getLastTaxPaidUpTo() !=null && vcrDetails.getLastTaxPaidUpTo().equals(quarterStartDate.minusDays(1)) && vcrDetails.getLatestVcrDate()!=null) {
			Pair<Integer, Integer> monthsPositionForVcr = this.getMonthposition(vcrDetails.getLatestVcrDate());
			if(monthsPositionForVcr.getFirst()==0) {
			return true;	
			}
		}
		return false;
	}
	
	public Double getScrtVehicleTax(String prNo) {
		Optional<MasterStageCarriageTaxDTO>  scrtTaxDto = masterStageCarriageTaxDAO.findByPrNoAndStatusIsTrue(prNo);
		if(scrtTaxDto.isPresent()) {
			return scrtTaxDto.get().getTax();
		}
		return 0d;
	}
	private TaxHelper getNonexemptionCovTax(RegServiceDTO regServiceDTO,Double OptionalTax, LocalDate taxValidity, LocalDate approveDate) {
		Pair<Integer, Integer> approveDateMonthpositionInQuater = getMonthposition(approveDate);
		Pair<Integer, Integer> taxValidityMonthpositionInQuater = getMonthposition(taxValidity);
		
		
		if (!(approveDateMonthpositionInQuater.getSecond().equals(taxValidityMonthpositionInQuater.getSecond())
				&& approveDate.getYear() == taxValidity.getYear())) {
			OptionalTax = getOldCovTax(
					regServiceDTO.getRegistrationDetails().getClassOfVehicle(),
					regServiceDTO.getRegistrationDetails().getVahanDetails().getSeatingCapacity(),
					regServiceDTO.getRegistrationDetails().getVahanDetails().getUnladenWeight(),
					regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw(), stateCode, permitcode,
					null);
			Double currentTaxPerMOnth = OptionalTax / 3;
			Double taxArr = 0d;
			Pair<Double, Double> taxArr2AndPenality = null;
			Double penalityArr = 0d;
			LocalDate approvedQuaterSatertSDate = this.getQuarterStatrtDate(approveDateMonthpositionInQuater.getFirst(), approveDateMonthpositionInQuater.getSecond(), approveDate);
			Long totalMonthsForPenality = ChronoUnit.MONTHS.between(taxValidity, approvedQuaterSatertSDate);
			totalMonthsForPenality = Math.abs(totalMonthsForPenality);
		//	totalMonthsForPenality = totalMonthsForPenality + 1;
			double Penalityquaters = totalMonthsForPenality / 3d;
			if (Penalityquaters > 0) {
				taxArr2AndPenality = chassisPenalitTax(OptionalTax, Penalityquaters, false);
				taxArr = taxArr + taxArr2AndPenality.getFirst();
				penalityArr = penalityArr + taxArr2AndPenality.getSecond();
			}
			if (approveDateMonthpositionInQuater.getFirst() == 0) {
				OptionalTax =  (currentTaxPerMOnth);
			} else if (approveDateMonthpositionInQuater.getFirst() == 1) {
				OptionalTax =  (currentTaxPerMOnth * 2);
			}/* else {
				OptionalTax =  (OptionalTax);
			}*/
			return returnTaxDetails(OptionalTax, taxArr, 0d, penalityArr, OptionalTax,null);
		}
		return returnTaxDetails(TaxTypeEnum.LifeTax.getDesc(), 0l, 0d, lifTaxValidityCal(), 0l, 0d, null, "",
				0d,null);
	}
	
	
	
	private boolean checkTaxdeductionModeOrNotForOldQuarters(TaxHelper vcrDetails) {
		
		Pair<Integer, Integer> montPosition = this.getMonthposition(vcrDetails.getLatestVcrDate());
		LocalDate nextQuarterEnd = vcrDetails.getLastTaxPaidUpTo().plusDays(1);
		Pair<Integer, Integer> montPositionnextQuarterEnd = this.getMonthposition(nextQuarterEnd);
		nextQuarterEnd = this.calculateChassisTaxUpTo(montPositionnextQuarterEnd.getFirst(), montPositionnextQuarterEnd.getSecond(), nextQuarterEnd);
		if ((vcrDetails.getLatestVcrDate().isAfter(vcrDetails.getLastTaxPaidUpTo()) &&  montPosition.getFirst()!=0)||
				vcrDetails.getLatestVcrDate().isAfter(nextQuarterEnd)) {
		
			//regServiceDTO.setDeductionMode(Boolean.TRUE);
			return false;
		}
		return true;
	
	}
}