package org.epragati.tax.serviceImp;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.epragati.constants.CovCategory;
import org.epragati.constants.OwnerTypeEnum;
import org.epragati.exception.BadRequestException;
import org.epragati.images.serviceImp.ImagesServiceImpl;
import org.epragati.master.dao.MasterAmountSecoundCovsDAO;
import org.epragati.master.dao.MasterOtherStateTaxDAO;
import org.epragati.master.dao.MasterPayperiodDAO;
import org.epragati.master.dao.MasterTaxBasedDAO;
import org.epragati.master.dao.MasterTaxDAO;
import org.epragati.master.dao.MasterTaxExcemptionsDAO;
import org.epragati.master.dao.MasterTaxFuelTypeExcemptionDAO;
import org.epragati.master.dao.StagingRegistrationDetailsDAO;
import org.epragati.master.dao.TaxDetailsDAO;
import org.epragati.master.dto.MasterAmountSecoundCovsDTO;
import org.epragati.master.dto.MasterOtherStateTaxDTO;
import org.epragati.master.dto.MasterPayperiodDTO;
import org.epragati.master.dto.MasterTax;
import org.epragati.master.dto.MasterTaxBased;
import org.epragati.master.dto.MasterTaxExcemptionsDTO;
import org.epragati.master.dto.MasterTaxFuelTypeExcemptionDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.payment.dto.PaymentTransactionDTO;
import org.epragati.payments.dao.PaymentTransactionDAO;
import org.epragati.payments.vo.BreakPayments;
import org.epragati.regservice.CitizenTaxService;
import org.epragati.tax.service.TaxService;
import org.epragati.tax.vo.TaxCalculationHelper;
import org.epragati.tax.vo.TaxTypeEnum;
import org.epragati.util.payment.ClassOfVehicleEnum;
import org.epragati.util.payment.PayStatusEnum;
import org.epragati.util.payment.ServiceCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class TaxServiceImp implements TaxService {

	private static final Logger logger = LoggerFactory.getLogger(TaxServiceImp.class);
	
	@Autowired
	private MasterTaxBasedDAO masterTaxBasedDAO;

	@Autowired
	private MasterPayperiodDAO masterPayperiodDAO;

	@Autowired
	private MasterTaxDAO taxTypeDAO;

	@Autowired
	private MasterTaxExcemptionsDAO masterTaxExcemptionsDAO;

	@Autowired
	private MasterOtherStateTaxDAO masterOtherStateTaxDAO;

	@Autowired
	private MasterAmountSecoundCovsDAO masterAmountSecoundCovsDAO;

	@Autowired
	private PaymentTransactionDAO paymentTransactionDAO;
	
	@Autowired
	private MasterTaxFuelTypeExcemptionDAO masterTaxFuelTypeExcemptionDAO;

	/*
	 * @Autowired private StagingRegistrationDetailsDAO
	 * stagingRegistrationDetailsDAO;
	 */
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
	private TaxDetailsDAO taxDetailsDAO;
	
	@Autowired
	private CitizenTaxService citizenTaxService;

	@Override
	public StagingRegistrationDetailsDTO getTaxDetails(StagingRegistrationDetailsDTO stagingRegDetails) {

		Optional<MasterTaxExcemptionsDTO> optionalTaxExcemption = masterTaxExcemptionsDAO.findByKeyvalueOrCovcode(
				stagingRegDetails.getVahanDetails().getMakersModel(), stagingRegDetails.getClassOfVehicle());
		if (optionalTaxExcemption.isPresent()) {
			stagingRegDetails.setTaxAmount(optionalTaxExcemption.get().getTaxvalue().longValue());
			// stagingRegistrationDetailsDAO.save(stagingRegDetails);
			return stagingRegDetails;
		}
		if(stagingRegDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())
				||(stagingRegDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode())&&stagingRegDetails.getVahanDetails().getGvw()<=3000)
				||stagingRegDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())
				|| stagingRegDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TRTT.getCovCode())
				||stagingRegDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TRCN.getCovCode())) {
			lifTaxValidityCal(stagingRegDetails);
			stagingRegDetails.setTaxAmount(0l);
	// stagingRegistrationDetailsDAO.save(stagingRegDetails);
	return stagingRegDetails;
		}

		Optional<MasterPayperiodDTO> Payperiod = masterPayperiodDAO
				.findByCovcode(stagingRegDetails.getClassOfVehicle());

		if (!Payperiod.isPresent()) {
			// throw error message
			logger.error("No record found in master_payperiod for: " + stagingRegDetails.getClassOfVehicle());
			throw new BadRequestException(
					"No record found in master_payperiod for: " + stagingRegDetails.getClassOfVehicle());

		}
	

		if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.BOTH.getCode())) {
			if(stagingRegDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.OBPN.getCovCode())) {
			if (Integer.parseInt(stagingRegDetails.getVahanDetails().getSeatingCapacity()) > 10) {
				Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
			} else {
				Payperiod.get().setPayperiod(TaxTypeEnum.LifeTax.getCode());
			}
			}else if(stagingRegDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())) {
				if (Integer.parseInt(stagingRegDetails.getVahanDetails().getSeatingCapacity()) <=4) {
					Payperiod.get().setPayperiod(TaxTypeEnum.LifeTax.getCode());
				} else {
					Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
				}
			}else if(stagingRegDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode())||
					stagingRegDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.GCRT.getCovCode())) {
				if (stagingRegDetails.getVahanDetails().getGvw()<=3000) {
					Payperiod.get().setPayperiod(TaxTypeEnum.LifeTax.getCode());
				} else {
					Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
				}
			}
		}
		switch (TaxTypeEnum.getTaxTypeEnumByCode(Payperiod.get().getPayperiod())) {

		case LifeTax:
			Double totalLifeTax;
			List<TaxDetailsDTO> vcrLifTax =null;
			List<MasterAmountSecoundCovsDTO> masterAmountSecoundCovsDTO = masterAmountSecoundCovsDAO.findAll();
			if (masterAmountSecoundCovsDTO.isEmpty()) {
				// TODO:need to Exception throw
			}

			Optional<MasterAmountSecoundCovsDTO> basedOnInvoice = masterAmountSecoundCovsDTO.stream()
					.filter(m -> m.getAmountcovcode().contains(stagingRegDetails.getClassOfVehicle())).findFirst();

			Optional<MasterAmountSecoundCovsDTO> basedOnsecoundVehicle = masterAmountSecoundCovsDTO.stream()
					.filter(m -> m.getSecondcovcode().contains(stagingRegDetails.getClassOfVehicle())).findFirst();
			List<TaxDetailsDTO>  listOfTax = taxDetailsDAO.findFirst10ByChassisNoOrderByCreatedDateDesc(stagingRegDetails.getVahanDetails().getChassisNumber());
			if(listOfTax !=null && !listOfTax.isEmpty()) {
				List<TaxDetailsDTO> vcrTax = listOfTax.stream().filter(tax->tax.getTaxPaidThroughVcr()!=null && tax.getTaxPaidThroughVcr()).collect(Collectors.toList());
				if(vcrTax!=null && !vcrTax.isEmpty()) {
					 vcrLifTax = vcrTax.stream().filter(tax->tax.getPaymentPeriod()!=null && tax.getPaymentPeriod().equalsIgnoreCase(TaxTypeEnum.LifeTax.getDesc())).collect(Collectors.toList());
					if(vcrLifTax!=null && !vcrLifTax.isEmpty()) {
						if (stagingRegDetails.getRejectionHistory() == null) {
							stagingRegDetails.setTaxAmount(0l);
							return stagingRegDetails;
						}
					}
				}
			}
			
			if (ownertypecheck(stagingRegDetails.getOwnerType().getCode(), stagingRegDetails.getClassOfVehicle())) {
				Optional<MasterTax> OptionalLifeTax = taxTypeDAO
						.findByCovcodeAndOwnershiptypeIgnoreCaseAndStatecodeAndStatusAndFromage(
								stagingRegDetails.getClassOfVehicle(), stagingRegDetails.getOwnerType().getCode(),
								stateCode, regStatus, freshVehicleAge);
				if (!OptionalLifeTax.isPresent()) {
					logger.error("No record found in master_tax for: "
							+ stagingRegDetails.getClassOfVehicle() + "and" + stagingRegDetails.getOwnerType());
					// throw error message
					throw new BadRequestException("No record found in master_tax for: "
							+ stagingRegDetails.getClassOfVehicle() + "and" + stagingRegDetails.getOwnerType());
				}

				totalLifeTax = (stagingRegDetails.getInvoiceDetails().getInvoiceValue()
						* OptionalLifeTax.get().getPercent() / 100);
				if (totalLifeTax.equals(Double.valueOf(0))) {
					stagingRegDetails.setTaxAmount(0l);
					// stagingRegistrationDetailsDAO.save(stagingRegDetails);
					return stagingRegDetails;
				}
				finalLifeTaxCalculation(stagingRegDetails, totalLifeTax, OptionalLifeTax.get().getPercent(),vcrLifTax);

			} else if (stagingRegDetails.getInvoiceDetails().getInvoiceValue() > amount && basedOnInvoice.isPresent()
					&& stagingRegDetails.getOwnerType().getCode()
							.equalsIgnoreCase(OwnerTypeEnum.Individual.getCode())) {

				totalLifeTax = (stagingRegDetails.getInvoiceDetails().getInvoiceValue()
						* basedOnInvoice.get().getTaxpercentinvoice() / 100f);
				finalLifeTaxCalculation(stagingRegDetails, totalLifeTax, basedOnInvoice.get().getTaxpercentinvoice(),vcrLifTax);
			} else if (basedOnsecoundVehicle.isPresent() && !stagingRegDetails.getIsFirstVehicle()) {

				totalLifeTax = (stagingRegDetails.getInvoiceDetails().getInvoiceValue()
						* basedOnsecoundVehicle.get().getSecondvehiclepercent() / 100f);
				finalLifeTaxCalculation(stagingRegDetails, totalLifeTax,
						basedOnsecoundVehicle.get().getSecondvehiclepercent(),vcrLifTax);
				stagingRegDetails.setSecondVehicleTaxPaid(Boolean.TRUE);
			} else if (stagingRegDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.IVCN.getCovCode()) && 
					!stagingRegDetails.getOfficeDetails().getOfficeCode().equalsIgnoreCase(otherState)) {
				Optional<MasterTax> OptionalLifeTax = taxTypeDAO
						.findByCovcodeAndOwnershiptypeIgnoreCaseAndStatecodeAndStatusAndFromage(
								stagingRegDetails.getClassOfVehicle(), stagingRegDetails.getOwnerType().getCode(),
								stateCode, regStatus, freshVehicleAge);
				if (!OptionalLifeTax.isPresent()) {
					logger.error("No record found in master_tax for: "
							+ stagingRegDetails.getClassOfVehicle() + "and" + stagingRegDetails.getOwnerType());
					// throw error message
					throw new BadRequestException("No record found in master_tax for: "
							+ stagingRegDetails.getClassOfVehicle() + "and" + stagingRegDetails.getOwnerType());
				}
				totalLifeTax = (OptionalLifeTax.get().getTaxamount().doubleValue());
				lifTaxValidityCal(stagingRegDetails);
				stagingRegDetails.setTaxAmount(roundUpperTen(totalLifeTax));
				//stagingRegDetails.setTaxAmount(0l);
				// stagingRegistrationDetailsDAO.save(stagingRegDetails);
				return stagingRegDetails;

			} else if (stagingRegDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.IVCN.getCovCode()) && 
					stagingRegDetails.getOfficeDetails().getOfficeCode().equalsIgnoreCase(otherState)) {
				Optional<MasterTax> OptionalLifeTax = Optional.empty();
				if(stagingRegDetails.getVahanDetails().getVehicleClass().equalsIgnoreCase("M1")) {
					OptionalLifeTax = taxTypeDAO
							.findByCovcodeAndOwnershiptypeIgnoreCaseAndStatecodeAndStatusAndFromage(
									ClassOfVehicleEnum.MCRN.getCovCode(), stagingRegDetails.getOwnerType().getCode(),
									stateCode, regStatus, freshVehicleAge);
				}else {
					 OptionalLifeTax = taxTypeDAO
							.findByCovcodeAndOwnershiptypeIgnoreCaseAndStatecodeAndStatusAndFromage(
									ClassOfVehicleEnum.MCYN.getCovCode(), stagingRegDetails.getOwnerType().getCode(),
									stateCode, regStatus, freshVehicleAge);
				}
				
				if (!OptionalLifeTax.isPresent()) {
					logger.error("No record found in master_tax for: "
							+ stagingRegDetails.getClassOfVehicle() + "and" + stagingRegDetails.getOwnerType());
					// throw error message
					throw new BadRequestException("No record found in master_tax for: "
							+ stagingRegDetails.getClassOfVehicle() + "and" + stagingRegDetails.getOwnerType());
				}
				totalLifeTax = (stagingRegDetails.getInvoiceDetails().getInvoiceValue()
						* OptionalLifeTax.get().getPercent() / 100f);
				finalLifeTaxCalculation(stagingRegDetails, totalLifeTax, OptionalLifeTax.get().getPercent(),vcrLifTax);

			} else {
				Optional<MasterTax> OptionalLifeTax = taxTypeDAO
						.findByCovcodeAndOwnershiptypeIgnoreCaseAndStatecodeAndStatusAndFromage(
								stagingRegDetails.getClassOfVehicle(), stagingRegDetails.getOwnerType().getCode(),
								stateCode, regStatus, freshVehicleAge);
				if (!OptionalLifeTax.isPresent()) {
					logger.error("No record found in master_tax for: "
							+ stagingRegDetails.getClassOfVehicle() + "and" + stagingRegDetails.getOwnerType());
					// throw error message
					throw new BadRequestException("No record found in master_tax for: "
							+ stagingRegDetails.getClassOfVehicle() + "and" + stagingRegDetails.getOwnerType());
				}
				if(ClassOfVehicleEnum.ERKT.getCovCode().equalsIgnoreCase(stagingRegDetails.getClassOfVehicle())||
						ClassOfVehicleEnum.ECRT.getCovCode().equalsIgnoreCase(stagingRegDetails.getClassOfVehicle())) {
					totalLifeTax = (OptionalLifeTax.get().getTaxamount().doubleValue());
					//lifTaxValidityCal(stagingRegDetails);
					//stagingRegDetails.setTaxAmount(roundUpperTen(totalLifeTax));
					//return stagingRegDetails;
				}else {
				totalLifeTax = (stagingRegDetails.getInvoiceDetails().getInvoiceValue()
						* OptionalLifeTax.get().getPercent() / 100f);
				}
				finalLifeTaxCalculation(stagingRegDetails, totalLifeTax, OptionalLifeTax.get().getPercent(),vcrLifTax);
			}

			break;

		case QuarterlyTax:
			if (ownertypecheck(stagingRegDetails.getOwnerType().getCode(), stagingRegDetails.getClassOfVehicle())) {
				stagingRegDetails.setTaxAmount(0l);
				// stagingRegistrationDetailsDAO.save(stagingRegDetails);
				return stagingRegDetails;
			}
			List<TaxDetailsDTO>  listOfQuarterTax = taxDetailsDAO.findFirst10ByChassisNoOrderByCreatedDateDesc(stagingRegDetails.getVahanDetails().getChassisNumber());
			if(listOfQuarterTax !=null && !listOfQuarterTax.isEmpty()) {
				List<TaxDetailsDTO> vcrTax = listOfQuarterTax.stream().filter(tax->tax.getTaxPaidThroughVcr()!=null && tax.getTaxPaidThroughVcr()).collect(Collectors.toList());
				if(vcrTax!=null && !vcrTax.isEmpty()) {
					List<TaxDetailsDTO> lifTax = vcrTax.stream().filter(tax->tax.getPaymentPeriod()!=null && tax.getPaymentPeriod().equalsIgnoreCase(TaxTypeEnum.QuarterlyTax.getDesc())).collect(Collectors.toList());
					if(lifTax!=null && !lifTax.isEmpty()) {
						TaxDetailsDTO taxDto = lifTax.stream().findFirst().get();
						if (taxDto.getTaxPeriodEnd().isAfter(citizenTaxService.validity(TaxTypeEnum.QuarterlyTax.getDesc()))||
								taxDto.getTaxPeriodEnd().equals(citizenTaxService.validity(TaxTypeEnum.QuarterlyTax.getDesc()))) {
							stagingRegDetails.setTaxAmount(0l);
							return stagingRegDetails;
						}
					}
				}
			}
			Long totalQuarterlyTax;

			Optional<MasterTaxBased> taxCalBasedOn = masterTaxBasedDAO
					.findByCovcode(stagingRegDetails.getClassOfVehicle());
			if (!taxCalBasedOn.isPresent()) {
				logger.error("No record found in master_taxbased for: " + stagingRegDetails.getClassOfVehicle());
				// throw error message
				throw new BadRequestException(
						"No record found in master_taxbased for: " + stagingRegDetails.getClassOfVehicle());
			}
			Optional<MasterTax> OptionalUlwTax =Optional.empty();
			if (taxCalBasedOn.get().getBasedon().equalsIgnoreCase(ulwCode)) {
				if(ClassOfVehicleEnum.ATCHN.getCovCode().equalsIgnoreCase(stagingRegDetails.getClassOfVehicle())
						||ClassOfVehicleEnum.SPHN.getCovCode().equalsIgnoreCase(stagingRegDetails.getClassOfVehicle())
						||ClassOfVehicleEnum.TMRN.getCovCode().equalsIgnoreCase(stagingRegDetails.getClassOfVehicle())) {
					
					Integer ulw = stagingRegDetails.getVahanDetails().getUnladenWeight() + stagingRegDetails.getVahanDetails().getHarvestersDetails().getUlw();
					OptionalUlwTax = taxTypeDAO
							.findByCovcodeAndToulwGreaterThanEqualAndFromulwLessThanEqualAndStatecodeAndPermitcodeAndStatus(
									stagingRegDetails.getClassOfVehicle(),ulw,ulw, stateCode, permitcode,regStatus);
				}else {
					OptionalUlwTax = taxTypeDAO
						.findByCovcodeAndToulwGreaterThanEqualAndFromulwLessThanEqualAndStatecodeAndPermitcodeAndStatus(
								stagingRegDetails.getClassOfVehicle(),
								stagingRegDetails.getVahanDetails().getUnladenWeight(),
								stagingRegDetails.getVahanDetails().getUnladenWeight(), stateCode, permitcode,regStatus);
				}

				if (!OptionalUlwTax.isPresent()) {
					logger.error("No record found in master_tax for: " + stagingRegDetails.getClassOfVehicle() + "and ULW: "
							+ stagingRegDetails.getVahanDetails().getUnladenWeight());
					// throw error message
					throw new BadRequestException(
							"No record found in master_tax for: " + stagingRegDetails.getClassOfVehicle() + "and ULW: "
									+ stagingRegDetails.getVahanDetails().getUnladenWeight());
				}

				totalQuarterlyTax = quarterlyTaxCalculation(stagingRegDetails.getTaxType(), OptionalUlwTax,
						taxCalBasedOn.get().getBasedon(), stagingRegDetails);

				stagingRegDetails.setTaxAmount(totalQuarterlyTax);
				// stagingRegistrationDetailsDAO.save(stagingRegDetails);
				return stagingRegDetails;
			} else if (taxCalBasedOn.get().getBasedon().equalsIgnoreCase(rlwCode)) {
				Optional<MasterTax> OptionalTax = taxTypeDAO
						.findByCovcodeAndTorlwGreaterThanEqualAndFromrlwLessThanEqualAndStatecodeAndPermitcodeAndStatus(
								stagingRegDetails.getClassOfVehicle(), stagingRegDetails.getVahanDetails().getGvw(),
								stagingRegDetails.getVahanDetails().getGvw(), stateCode, permitcode,regStatus);
				if (!OptionalTax.isPresent()) {
					logger.error("No record found in master_tax for: " + stagingRegDetails.getClassOfVehicle() + "and rLW: "
							+ stagingRegDetails.getVahanDetails().getGvw());
					// throw error message
					throw new BadRequestException(
							"No record found in master_tax for: " + stagingRegDetails.getClassOfVehicle() + "and rLW: "
									+ stagingRegDetails.getVahanDetails().getGvw());
				}

				totalQuarterlyTax = quarterlyTaxCalculation(stagingRegDetails.getTaxType(), OptionalTax,
						taxCalBasedOn.get().getBasedon(), stagingRegDetails);

				stagingRegDetails.setTaxAmount(totalQuarterlyTax);
				// stagingRegistrationDetailsDAO.save(stagingRegDetails);
				return stagingRegDetails;

			} else if (taxCalBasedOn.get().getBasedon().equalsIgnoreCase(seatingCapacityCode)) {
				Optional<MasterTax> OptionalTax = taxTypeDAO
						.findByCovcodeAndSeattoGreaterThanEqualAndSeatfromLessThanEqualAndStatecodeAndPermitcodeAndStatus(
								stagingRegDetails.getClassOfVehicle(),
								Integer.parseInt(stagingRegDetails.getVahanDetails().getSeatingCapacity()),
								Integer.parseInt(stagingRegDetails.getVahanDetails().getSeatingCapacity()), stateCode,
								permitcode,regStatus);
				if (!OptionalTax.isPresent()) {
					// throw error message
					logger.error("No record found in master_tax for: "+stagingRegDetails.getClassOfVehicle() + "and seatingCapacity: "
							+ stagingRegDetails.getVahanDetails().getSeatingCapacity());
					throw new BadRequestException("No record found in master_tax for: "
							+ stagingRegDetails.getClassOfVehicle() + "and seatingCapacity: "
							+ stagingRegDetails.getVahanDetails().getSeatingCapacity());
				}

				totalQuarterlyTax = quarterlyTaxCalculation(stagingRegDetails.getTaxType(), OptionalTax,
						taxCalBasedOn.get().getBasedon(), stagingRegDetails);
				stagingRegDetails.setTaxAmount(totalQuarterlyTax);
				// stagingRegistrationDetailsDAO.save(stagingRegDetails);
				return stagingRegDetails;
			}

			break;
		default:
			break;
		}
		return stagingRegDetails;
	}

	private void finalLifeTaxCalculation(StagingRegistrationDetailsDTO stagingRegDetails, Double totalLifeTax,
			Float Percent,List<TaxDetailsDTO> lifTax) {
		List<MasterTaxFuelTypeExcemptionDTO> list = masterTaxFuelTypeExcemptionDAO.findAll();
		Double payAmount = 0d;
		if (stagingRegDetails.getOfficeDetails() == null
				|| stagingRegDetails.getOfficeDetails().getOfficeCode() == null) {
			logger.error("office details missing [{}]." , stagingRegDetails.getApplicationNo());
			throw new BadRequestException("office details missing. "+stagingRegDetails.getApplicationNo());
		}
		if (stagingRegDetails.getOfficeDetails().getOfficeCode().equalsIgnoreCase(otherState)) {
			lifTaxValidityCal(stagingRegDetails);
			if (list.stream().anyMatch(type->type.getFuelType().stream().anyMatch(fuel->fuel.equalsIgnoreCase(stagingRegDetails.getVahanDetails().getFuelDesc())))) {
				totalLifeTax = batteryDiscount(stagingRegDetails, totalLifeTax, Percent,list);
				if(totalLifeTax == 0) {
					lifTaxValidityCal(stagingRegDetails);
					stagingRegDetails.setTaxAmount(0l);
					return;
				}
			}else {
			stagingRegDetails.setTaxAmount(roundUpperTen(otherStateTaxCalculation(stagingRegDetails, totalLifeTax)));
			}
		} else if (list.stream().anyMatch(type->type.getFuelType().stream().anyMatch(fuel->fuel.equalsIgnoreCase(stagingRegDetails.getVahanDetails().getFuelDesc())))) {
		/*	if(!(ClassOfVehicleEnum.ERKT.getCovCode().equalsIgnoreCase(stagingRegDetails.getClassOfVehicle())||
					ClassOfVehicleEnum.ECRT.getCovCode().equalsIgnoreCase(stagingRegDetails.getClassOfVehicle()))) {*/
			totalLifeTax = batteryDiscount(stagingRegDetails, totalLifeTax, Percent,list);
			if(totalLifeTax == 0) {
				lifTaxValidityCal(stagingRegDetails);
				stagingRegDetails.setTaxAmount(0l);
				return;
			}
			
			/*}*/
			if (stagingRegDetails.getRejectionHistory() != null) {
				if ((stagingRegDetails.getRejectionHistory().getIsSecondVehicleRejected() != null
						&& stagingRegDetails.getRejectionHistory().getIsSecondVehicleRejected())
						|| (stagingRegDetails.getRejectionHistory().getIsInvalidVehicleRejection() != null
								&& stagingRegDetails.getRejectionHistory().getIsInvalidVehicleRejection())) {
					Double paidTax = 0.0;
					List<PaymentTransactionDTO> paymentTransactionDTOList = paymentTransactionDAO
							.findByApplicationFormRefNumAndPayStatus(stagingRegDetails.getApplicationNo(),
									PayStatusEnum.SUCCESS.getDescription());
					paymentTransactionDTOList.sort(
							(p1, p2) -> p2.getRequest().getRequestTime().compareTo(p1.getRequest().getRequestTime()));
					if (paymentTransactionDTOList.size() > 0
							&& paymentTransactionDTOList.get(0).getBreakPaymentsSave() != null) {
						PaymentTransactionDTO paymentTransactionDTO = paymentTransactionDTOList.get(0);

						for (BreakPayments bpsave : paymentTransactionDTO.getBreakPaymentsSave().getBreakPayments()) {
							if (bpsave.getBreakup() != null
									&& bpsave.getBreakup().get(ServiceCodeEnum.LIFE_TAX.getCode()) != null) {
								paidTax += bpsave.getBreakup().get(ServiceCodeEnum.LIFE_TAX.getCode());
							} else {
								if (bpsave.getBreakup() != null
										&& bpsave.getBreakup().get(ServiceCodeEnum.QLY_TAX.getCode()) != null) {
									paidTax += bpsave.getBreakup().get(ServiceCodeEnum.QLY_TAX.getCode());
								} else if (bpsave.getBreakup() != null
										&& bpsave.getBreakup().get(ServiceCodeEnum.HALF_TAX.getCode()) != null) {
									paidTax += bpsave.getBreakup().get(ServiceCodeEnum.HALF_TAX.getCode());
								} else if (bpsave.getBreakup() != null
										&& bpsave.getBreakup().get(ServiceCodeEnum.YEAR_TAX.getCode()) != null) {
									paidTax += bpsave.getBreakup().get(ServiceCodeEnum.YEAR_TAX.getCode());
								}
							}
						}

					}
					if(lifTax!=null &&!lifTax.isEmpty() && lifTax.stream().findFirst().get().getTaxAmount() !=null && paidTax==0) {
						paidTax = lifTax.stream().findFirst().get().getTaxAmount().doubleValue();
					}
					payAmount = totalLifeTax - paidTax;
					if (totalLifeTax > paidTax) {
						if ((stagingRegDetails.getRejectionHistory().getIsSecondVehicleRejected() != null
								&& stagingRegDetails.getRejectionHistory().getIsSecondVehicleRejected())) {
						stagingRegDetails.setSecondVehicleTaxPaid(Boolean.TRUE);
						}
						stagingRegDetails.setTaxAmount(roundUpperTen(payAmount));
					} else {
						if ((stagingRegDetails.getRejectionHistory().getIsSecondVehicleRejected() != null
								&& stagingRegDetails.getRejectionHistory().getIsSecondVehicleRejected())) {
						stagingRegDetails.setSecondVehicleTaxPaid(Boolean.TRUE);
						}
						stagingRegDetails.setTaxAmount(0l);
					}
				}
			} else {
				lifTaxValidityCal(stagingRegDetails);
				stagingRegDetails.setTaxAmount(roundUpperTen(totalLifeTax));
			}
		} else {
			Double paidTax = 0.0;
			if (stagingRegDetails.getRejectionHistory() != null) {
				if ((stagingRegDetails.getRejectionHistory().getIsSecondVehicleRejected() != null
						&& stagingRegDetails.getRejectionHistory().getIsSecondVehicleRejected())
						|| (stagingRegDetails.getRejectionHistory().getIsInvalidVehicleRejection() != null
								&& stagingRegDetails.getRejectionHistory().getIsInvalidVehicleRejection())) {
					List<PaymentTransactionDTO> paymentTransactionDTOList = paymentTransactionDAO
							.findByApplicationFormRefNumAndPayStatus(stagingRegDetails.getApplicationNo(),
									PayStatusEnum.SUCCESS.getDescription());
					paymentTransactionDTOList.sort(
							(p1, p2) -> p2.getRequest().getRequestTime().compareTo(p1.getRequest().getRequestTime()));
					if (paymentTransactionDTOList.size() > 0
							&& paymentTransactionDTOList.get(0).getBreakPaymentsSave() != null) {
						PaymentTransactionDTO paymentTransactionDTO = paymentTransactionDTOList.get(0);

						for (BreakPayments bpsave : paymentTransactionDTO.getBreakPaymentsSave().getBreakPayments()) {
							if (bpsave.getBreakup() != null
									&& bpsave.getBreakup().get(ServiceCodeEnum.LIFE_TAX.getCode()) != null) {
								paidTax += bpsave.getBreakup().get(ServiceCodeEnum.LIFE_TAX.getCode());
							} else {
								if (bpsave.getBreakup() != null
										&& bpsave.getBreakup().get(ServiceCodeEnum.QLY_TAX.getCode()) != null) {
									paidTax += bpsave.getBreakup().get(ServiceCodeEnum.QLY_TAX.getCode());
								} else if (bpsave.getBreakup() != null
										&& bpsave.getBreakup().get(ServiceCodeEnum.HALF_TAX.getCode()) != null) {
									paidTax += bpsave.getBreakup().get(ServiceCodeEnum.HALF_TAX.getCode());
								} else if (bpsave.getBreakup() != null
										&& bpsave.getBreakup().get(ServiceCodeEnum.YEAR_TAX.getCode()) != null) {
									paidTax += bpsave.getBreakup().get(ServiceCodeEnum.YEAR_TAX.getCode());
								}
							}
						}

					}
					if(lifTax!=null &&!lifTax.isEmpty() && lifTax.stream().findFirst().get().getTaxAmount() !=null && paidTax==0) {
						paidTax = lifTax.stream().findFirst().get().getTaxAmount().doubleValue();
					}
					payAmount = totalLifeTax - paidTax;
					if (totalLifeTax > paidTax) {
						if ((stagingRegDetails.getRejectionHistory().getIsSecondVehicleRejected() != null
								&& stagingRegDetails.getRejectionHistory().getIsSecondVehicleRejected())) {
						stagingRegDetails.setSecondVehicleTaxPaid(Boolean.TRUE);
						}
						stagingRegDetails.setTaxAmount(roundUpperTen(payAmount));
					} else {
						if ((stagingRegDetails.getRejectionHistory().getIsSecondVehicleRejected() != null
								&& stagingRegDetails.getRejectionHistory().getIsSecondVehicleRejected())) {
						stagingRegDetails.setSecondVehicleTaxPaid(Boolean.TRUE);
						}
						stagingRegDetails.setTaxAmount(0l);
					}
				}
			} else {
				lifTaxValidityCal(stagingRegDetails);
				stagingRegDetails.setTaxAmount(roundUpperTen(totalLifeTax));
			}
		}
	}

	// other state tax
	private double otherStateTaxCalculation(StagingRegistrationDetailsDTO stagingRegDetails, Double totalTax) {
		Optional<MasterOtherStateTaxDTO> masterOtherStateTax =Optional.empty();
		int ulw=0;
		if(ClassOfVehicleEnum.ATCHN.getCovCode().equalsIgnoreCase(stagingRegDetails.getClassOfVehicle())
				||ClassOfVehicleEnum.SPHN.getCovCode().equalsIgnoreCase(stagingRegDetails.getClassOfVehicle())
				||ClassOfVehicleEnum.TMRN.getCovCode().equalsIgnoreCase(stagingRegDetails.getClassOfVehicle())) {
			
			 ulw = stagingRegDetails.getVahanDetails().getUnladenWeight() + stagingRegDetails.getVahanDetails().getHarvestersDetails().getUlw();
			masterOtherStateTax = masterOtherStateTaxDAO
					.findByCovcodeInAndUlwtoGreaterThanEqualAndUlwfromLessThanEqualAndMonthsIn(
							stagingRegDetails.getClassOfVehicle(), ulw,ulw, LocalDate.now().getMonthValue());
			//ulw=stagingRegDetails.getVahanDetails().getHarvestersDetails().getUlw();
		}else {
		masterOtherStateTax = masterOtherStateTaxDAO
				.findByCovcodeInAndUlwtoGreaterThanEqualAndUlwfromLessThanEqualAndMonthsIn(
						stagingRegDetails.getClassOfVehicle(), stagingRegDetails.getVahanDetails().getUnladenWeight(),
						stagingRegDetails.getVahanDetails().getUnladenWeight(), LocalDate.now().getMonthValue());
		ulw=stagingRegDetails.getVahanDetails().getUnladenWeight();
		}
		if (masterOtherStateTax.isPresent()) {
			// based on weight 4000-8002=valu/250*80
			if (stagingRegDetails.getVahanDetails().getUnladenWeight() > 4000) {
				Double result = 0d;
				//int ulw = stagingRegDetails.getVahanDetails().getUnladenWeight();ss
				Float weight = (ulw - 4000f) / 250f;
				result = (double) ((Math.ceil(weight.doubleValue()) * 80) + masterOtherStateTax.get().getAmount());
				return result;
			}
			return masterOtherStateTax.get().getAmount();
		} else {
			return totalTax / 8;
		}
	}

	private void lifTaxValidityCal(StagingRegistrationDetailsDTO stagingRegDetails) {
		
		stagingRegDetails.setTaxvalidity(LocalDate.now().minusDays(1).plusYears(12));

	}

	private Long quarterlyTaxCalculation(String taxType, Optional<MasterTax> OptionalTax, String taxBasedon,
			StagingRegistrationDetailsDTO stagingRegDetails) {
		if (StringUtils.isEmpty(taxType)) {
			logger.error("Tax type is missing in staging details [{}]." , stagingRegDetails.getApplicationNo());
			throw new BadRequestException("Tax type is missing in staging details: " +  stagingRegDetails.getApplicationNo());
		}
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
		Double quaterTax = 0d;
		if (taxBasedon.equalsIgnoreCase(seatingCapacityCode)) {
			Float tax = (OptionalTax.get().getTaxamount()
					* (Integer.parseInt(stagingRegDetails.getVahanDetails().getSeatingCapacity()) - 1));
			TaxCalculationHelper taxAndQuaternNumber = plainTaxCalculation(tax, quaterOne, quaterTwo, quaterThree,
					quaterFour, currentquaterTax);
			quaterTax = tax.doubleValue();
			getCesFee(tax.doubleValue(), stagingRegDetails);
			currentquaterTax = taxAndQuaternNumber.getQuaterTax();
			quaternNumber = taxAndQuaternNumber.getQuaternNumber();
			indexPosition = taxAndQuaternNumber.getIndexPosition();
			Optional<MasterTaxExcemptionsDTO> optionalTaxExcemption = masterTaxExcemptionsDAO
					.findByKeyvalueAndSeattoGreaterThanEqualAndSeatfromLessThanEqual(
							stagingRegDetails.getClassOfVehicle(),
							Integer.parseInt(stagingRegDetails.getVahanDetails().getSeatingCapacity()),
							Integer.parseInt(stagingRegDetails.getVahanDetails().getSeatingCapacity()));
			if (optionalTaxExcemption.isPresent()
					&& optionalTaxExcemption.get().getValuetype().equalsIgnoreCase(seatingCapacityCode)) {
				TaxCalculationHelper taxAndQuaternNumberForException = plainTaxCalculation(
						optionalTaxExcemption.get().getTaxvalue(), quaterOne, quaterTwo, quaterThree, quaterFour,
						currentquaterTax);
				quaterTax = optionalTaxExcemption.get().getTaxvalue().doubleValue();
				getCesFee(optionalTaxExcemption.get().getTaxvalue().doubleValue(), stagingRegDetails);
				currentquaterTax = taxAndQuaternNumberForException.getQuaterTax();
			}
		} else if (OptionalTax.get().getIncrementalweight() == 0) {

			TaxCalculationHelper taxAndQuaternNumber = plainTaxCalculation(OptionalTax.get().getTaxamount(), quaterOne,
					quaterTwo, quaterThree, quaterFour, currentquaterTax);
			quaterTax = OptionalTax.get().getTaxamount().doubleValue();
			getCesFee(OptionalTax.get().getTaxamount().doubleValue(), stagingRegDetails);
			currentquaterTax = taxAndQuaternNumber.getQuaterTax();
			quaternNumber = taxAndQuaternNumber.getQuaternNumber();
			indexPosition = taxAndQuaternNumber.getIndexPosition();

		} else {
			if (taxBasedon.equalsIgnoreCase(ulwCode)) {

				if (quaterOne.contains(LocalDate.now().getMonthValue())) {
					indexPosition = quaterOne.indexOf(LocalDate.now().getMonthValue());
					quaternNumber = 1;
					Pair<Double, Double> currentAndQuaterTax = ulwQuaterTax(OptionalTax, stagingRegDetails,
							indexPosition);
					currentquaterTax = currentAndQuaterTax.getFirst();
					quaterTax = currentAndQuaterTax.getSecond();
				} else if (quaterTwo.contains(LocalDate.now().getMonthValue())) {
					indexPosition = quaterTwo.indexOf(LocalDate.now().getMonthValue());
					quaternNumber = 2;
					Pair<Double, Double> currentAndQuaterTax = ulwQuaterTax(OptionalTax, stagingRegDetails,
							indexPosition);
					currentquaterTax = currentAndQuaterTax.getFirst();
					quaterTax = currentAndQuaterTax.getSecond();
				} else if (quaterThree.contains(LocalDate.now().getMonthValue())) {
					indexPosition = quaterThree.indexOf(LocalDate.now().getMonthValue());
					quaternNumber = 3;
					Pair<Double, Double> currentAndQuaterTax = ulwQuaterTax(OptionalTax, stagingRegDetails,
							indexPosition);
					currentquaterTax = currentAndQuaterTax.getFirst();
					quaterTax = currentAndQuaterTax.getSecond();
				} else if (quaterFour.contains(LocalDate.now().getMonthValue())) {
					indexPosition = quaterFour.indexOf(LocalDate.now().getMonthValue());
					quaternNumber = 4;
					Pair<Double, Double> currentAndQuaterTax = ulwQuaterTax(OptionalTax, stagingRegDetails,
							indexPosition);
					currentquaterTax = currentAndQuaterTax.getFirst();
					quaterTax = currentAndQuaterTax.getSecond();
				}
			} else if (taxBasedon.equalsIgnoreCase(rlwCode)) {
				if (quaterOne.contains(LocalDate.now().getMonthValue())) {
					indexPosition = quaterOne.indexOf(LocalDate.now().getMonthValue());
					quaternNumber = 1;
					Pair<Double, Double> currentAndQuaterTax = rlwQuaterTax(OptionalTax, stagingRegDetails,
							indexPosition);
					currentquaterTax = currentAndQuaterTax.getFirst();
					quaterTax = currentAndQuaterTax.getSecond();
				} else if (quaterTwo.contains(LocalDate.now().getMonthValue())) {
					indexPosition = quaterTwo.indexOf(LocalDate.now().getMonthValue());
					quaternNumber = 2;
					Pair<Double, Double> currentAndQuaterTax = rlwQuaterTax(OptionalTax, stagingRegDetails,
							indexPosition);
					currentquaterTax = currentAndQuaterTax.getFirst();
					quaterTax = currentAndQuaterTax.getSecond();
				} else if (quaterThree.contains(LocalDate.now().getMonthValue())) {
					indexPosition = quaterThree.indexOf(LocalDate.now().getMonthValue());
					quaternNumber = 3;
					Pair<Double, Double> currentAndQuaterTax = rlwQuaterTax(OptionalTax, stagingRegDetails,
							indexPosition);
					currentquaterTax = currentAndQuaterTax.getFirst();
					quaterTax = currentAndQuaterTax.getSecond();
				} else if (quaterFour.contains(LocalDate.now().getMonthValue())) {
					indexPosition = quaterFour.indexOf(LocalDate.now().getMonthValue());
					quaternNumber = 4;
					Pair<Double, Double> currentAndQuaterTax = rlwQuaterTax(OptionalTax, stagingRegDetails,
							indexPosition);
					currentquaterTax = currentAndQuaterTax.getFirst();
					quaterTax = currentAndQuaterTax.getSecond();
				}
			}
		}

		if (taxType.equalsIgnoreCase(TaxTypeEnum.QuarterlyTax.getDesc())) {
			LocalDate quarterlyValidity = calculateTaxUpTo(indexPosition, quaternNumber);
			stagingRegDetails.setTaxvalidity(quarterlyValidity);
			Optional<MasterTaxExcemptionsDTO> excemptionPercentage = masterTaxExcemptionsDAO
					.findByKeyvalue(stagingRegDetails.getClassOfVehicle());
			if (excemptionPercentage.isPresent()
					&& !excemptionPercentage.get().getValuetype().equalsIgnoreCase(seatingCapacityCode)) {
				// check percentage discount
				double discount = (currentquaterTax * excemptionPercentage.get().getTaxvalue()) / 100;
				currentquaterTax = currentquaterTax - discount;
			}
			// getCesFee(quaterTax, stagingRegDetails);
			return roundUpperTen(currentquaterTax);

		} else if (taxType.equalsIgnoreCase(TaxTypeEnum.HalfyearlyTax.getDesc())) {
			LocalDate halfyearValidity = calculateTaxUpTo(indexPosition, quaternNumber);
			stagingRegDetails.setTaxvalidity(halfyearValidity.plusMonths(3));
			totalTax = quaterTax + currentquaterTax;
			Optional<MasterTaxExcemptionsDTO> excemptionPercentage = masterTaxExcemptionsDAO
					.findByKeyvalue(stagingRegDetails.getClassOfVehicle());
			if (excemptionPercentage.isPresent()
					&& !excemptionPercentage.get().getValuetype().equalsIgnoreCase(seatingCapacityCode)) {
				// check percentage discount
				double discount = (totalTax * excemptionPercentage.get().getTaxvalue()) / 100;
				totalTax = totalTax - discount;
			}
			// getCesFee(quaterTax, stagingRegDetails);
			return roundUpperTen(totalTax);
		} else if (taxType.equalsIgnoreCase(TaxTypeEnum.YearlyTax.getDesc())) {
			LocalDate yearValidity = calculateTaxUpTo(indexPosition, quaternNumber);
			stagingRegDetails.setTaxvalidity(yearValidity.plusMonths(9));
			totalTax = (quaterTax * 3) + currentquaterTax;
			Optional<MasterTaxExcemptionsDTO> excemptionPercentage = masterTaxExcemptionsDAO
					.findByKeyvalue(stagingRegDetails.getClassOfVehicle());
			if (excemptionPercentage.isPresent()
					&& !excemptionPercentage.get().getValuetype().equalsIgnoreCase(seatingCapacityCode)) {
				// check percentage discount
				double discount = (totalTax * excemptionPercentage.get().getTaxvalue()) / 100;
				totalTax = totalTax - discount;
			}
			// getCesFee(quaterTax, stagingRegDetails);
			return roundUpperTen(totalTax);
		}
		return null;
	}

	private TaxCalculationHelper plainTaxCalculation(Float OptionalTax, List<Integer> quaterOne,
			List<Integer> quaterTwo, List<Integer> quaterThree, List<Integer> quaterFour, Double currentquaterTax) {
		TaxCalculationHelper taxCalculationHelper = new TaxCalculationHelper();
		Integer indexPosition = 0;
		Integer quaternNumber = 0;
		if (quaterOne.contains(LocalDate.now().getMonthValue())) {
			indexPosition = quaterOne.indexOf(LocalDate.now().getMonthValue());
			quaternNumber = 1;
			currentquaterTax = currentQuaterTaxCalculation(OptionalTax.doubleValue(), indexPosition);
		} else if (quaterTwo.contains(LocalDate.now().getMonthValue())) {
			indexPosition = quaterTwo.indexOf(LocalDate.now().getMonthValue());
			quaternNumber = 2;
			currentquaterTax = currentQuaterTaxCalculation(OptionalTax.doubleValue(), indexPosition);
		} else if (quaterThree.contains(LocalDate.now().getMonthValue())) {
			indexPosition = quaterThree.indexOf(LocalDate.now().getMonthValue());
			quaternNumber = 3;
			currentquaterTax = currentQuaterTaxCalculation(OptionalTax.doubleValue(), indexPosition);
		} else if (quaterFour.contains(LocalDate.now().getMonthValue())) {
			indexPosition = quaterFour.indexOf(LocalDate.now().getMonthValue());
			quaternNumber = 4;
			currentquaterTax = currentQuaterTaxCalculation(OptionalTax.doubleValue(), indexPosition);
		}
		taxCalculationHelper.setQuaterTax(currentquaterTax);
		taxCalculationHelper.setIndexPosition(indexPosition);
		taxCalculationHelper.setQuaternNumber(quaternNumber);
		return taxCalculationHelper;
	}

	private LocalDate calculateTaxUpTo(Integer indexPosition, Integer quaternNumber) {
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

	private LocalDate validity(Integer indexPosition, DateTimeFormatter formatter, String date1) {
		LocalDate localDate = LocalDate.parse(date1, formatter);
		LocalDate newDate = localDate.withDayOfMonth(localDate.getMonth().maxLength());
		LocalDate newDate1 = newDate.plusMonths(2);
		return newDate1.withDayOfMonth(newDate1.getMonth().maxLength());
	}

	private Pair<Double, Double> ulwQuaterTax(Optional<MasterTax> OptionalTax,
			StagingRegistrationDetailsDTO stagingRegDetails, Integer valu) {
		Double currentquaterTax;
		Double quaterTax1;
		int ulw = 0;
		ulw=stagingRegDetails.getVahanDetails().getUnladenWeight();
		if(ClassOfVehicleEnum.ATCHN.getCovCode().equalsIgnoreCase(stagingRegDetails.getClassOfVehicle())
				||ClassOfVehicleEnum.SPHN.getCovCode().equalsIgnoreCase(stagingRegDetails.getClassOfVehicle())
				||ClassOfVehicleEnum.TMRN.getCovCode().equalsIgnoreCase(stagingRegDetails.getClassOfVehicle())) {
			 ulw = stagingRegDetails.getVahanDetails().getUnladenWeight() + stagingRegDetails.getVahanDetails().getHarvestersDetails().getUlw();
		}
		Float weight = (float) ((ulw
				- ((OptionalTax.get().getFromulw() - 1))) / OptionalTax.get().getIncrementalweight().doubleValue());
		Float result = (float) ((Math.ceil(weight.doubleValue()) * OptionalTax.get().getIncrementalamount())
				+ OptionalTax.get().getTaxamount());
		quaterTax1 = result.doubleValue();
		getCesFee(result.doubleValue(), stagingRegDetails);
		currentquaterTax = currentQuaterTaxCalculation(result.doubleValue(), valu);
		return Pair.of(currentquaterTax, quaterTax1);
	}

	private Pair<Double, Double> rlwQuaterTax(Optional<MasterTax> OptionalTax,
			StagingRegistrationDetailsDTO stagingRegDetails, Integer valu) {
		Double currentquaterTax;
		Double quaterTax1;
		Double weight = (double) ((stagingRegDetails.getVahanDetails().getGvw()
				- ((OptionalTax.get().getFromrlw() - 1))) / OptionalTax.get().getIncrementalweight().doubleValue());
		Double result = ((Math.ceil(weight) * OptionalTax.get().getIncrementalamount())
				+ OptionalTax.get().getTaxamount());
		quaterTax1 = result.doubleValue();
		getCesFee(result.doubleValue(), stagingRegDetails);
		currentquaterTax = currentQuaterTaxCalculation(result.doubleValue(), valu);
		return Pair.of(currentquaterTax, quaterTax1);
	}

	private Double currentQuaterTaxCalculation(Double OptionalTax, Integer valu) {
		Double quaterTax;
		if (valu == 0) {
			quaterTax = OptionalTax;
		} else if (valu == 1) {
			quaterTax = (OptionalTax / 3) * 2;
		} else {
			quaterTax = (OptionalTax / 3) * 1;
		}
		return quaterTax;
	}

	private long roundUpperTen(Double totalTax) {
		if ((totalTax % 10f) == 0) {
			return (int) Math.round(totalTax);
		} else {
			int taxIntValue = totalTax.intValue();
			if (taxIntValue % 10 == 9) {
				Double tax = Math.ceil(totalTax);
				if((tax % 10f) != 0) {
					tax = tax+1;
				}
				return tax.longValue();
			} else {
				return ((Math.round(totalTax) / 10) * 10 + 10);
			}
		}
	}

	private Double batteryDiscount(StagingRegistrationDetailsDTO stagingRegDetails, Double totalTax, Float Percent,List<MasterTaxFuelTypeExcemptionDTO> list) {
		MasterTaxFuelTypeExcemptionDTO dto = list.stream().findFirst().get();
		Float discount = (Percent / 12f) * dto.getNoOfYears().get(stagingRegDetails.getVahanDetails().getFuelDesc());
		return ((stagingRegDetails.getInvoiceDetails().getInvoiceValue() * discount) / 100f);
	}

	private void getCesFee(Double quarterTax, StagingRegistrationDetailsDTO stagingRegDetails) {
		List<Integer> quaterFour = new ArrayList<>();
		quaterFour.add(0, 1);
		quaterFour.add(1, 2);
		quaterFour.add(2, 3);

		Optional<MasterTaxExcemptionsDTO> optionalTaxExcemption = masterTaxExcemptionsDAO
				.findByKeyvalue(stagingRegDetails.getClassOfVehicle());
		if (optionalTaxExcemption.isPresent()
				&& optionalTaxExcemption.get().getValuetype().equalsIgnoreCase(TaxTypeEnum.CESS.getCode())) {
			stagingRegDetails.setCesFee(optionalTaxExcemption.get().getTaxvalue().longValue());
		} else {
			Optional<MasterTaxExcemptionsDTO> excemptionPercentage = masterTaxExcemptionsDAO
					.findByKeyvalue(stagingRegDetails.getClassOfVehicle());
			if (excemptionPercentage.isPresent()
					&& !excemptionPercentage.get().getValuetype().equalsIgnoreCase(seatingCapacityCode)) {
				// check percentage discount
				double discount = (quarterTax * excemptionPercentage.get().getTaxvalue()) / 100;
				quarterTax = quarterTax - discount;
			}
			Double discount = (double) ((quarterTax * 4) / 12);
			if (quaterFour.contains(LocalDate.now().getMonthValue())) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
				String date1 = "31-03-" + String.valueOf(LocalDate.now().getYear());
				LocalDate endDate = LocalDate.parse(date1, formatter);
				stagingRegDetails.setCesValidity(endDate);
				int currentMonth = LocalDate.now().getMonthValue();
				int monthUpTo = endDate.getMonthValue();
				int monthsBetween = monthUpTo - currentMonth;
				// long monthsBetween = ChronoUnit.MONTHS.between(LocalDate.now(), endDate);
				monthsBetween = monthsBetween + 1;
				Double totalCesFee = (discount * monthsBetween) * 10 / 100;
				if (totalCesFee > 1500) {
					stagingRegDetails.setCesFee(1500l);
				} else {
					stagingRegDetails.setCesFee(roundUpperTen(totalCesFee));
				}

			} else {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
				String date1 = "31-03-" + String.valueOf(LocalDate.now().plusYears(1).getYear());
				LocalDate endDate = LocalDate.parse(date1, formatter);
				stagingRegDetails.setCesValidity(endDate);
				int currentMonth = LocalDate.now().getMonthValue();
				// int monthUpTo = 12;
				int monthsBetween = (12 - currentMonth) + 1;// year end month and add current month.
				// long monthsBetween = ChronoUnit.MONTHS.between(LocalDate.now(), endDate);
				monthsBetween = monthsBetween + endDate.getMonthValue();
				Double totalCesFee = (discount * monthsBetween) * 10 / 100;
				if (totalCesFee > 1500) {
					stagingRegDetails.setCesFee(1500l);
				} else {
					stagingRegDetails.setCesFee(roundUpperTen(totalCesFee));
				}
			}

		}
	}

	@Override
	public List<String> getTaxTypes(String State, String classOfVehicle, Integer seatingCapacity,Integer gvw) {

		List<String> listTaxperiod = new ArrayList<>();
		Optional<MasterPayperiodDTO> Payperiod = masterPayperiodDAO.findByCovcode(classOfVehicle);

		if (!Payperiod.isPresent()) {
			logger.error("No record found in master_payperiod for:  " + classOfVehicle);
			// throw error message
			throw new BadRequestException("No record found in master_payperiod for:  " + classOfVehicle);
		}

		if (Payperiod.get().getPayperiod().equalsIgnoreCase("B")) {
			if(Payperiod.get().getCovcode().equalsIgnoreCase(ClassOfVehicleEnum.OBPN.getCovCode())) {
				if (seatingCapacity > 10) {
					Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
				} else {
					Payperiod.get().setPayperiod(TaxTypeEnum.LifeTax.getCode());
				}
				}else if(Payperiod.get().getCovcode().equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())) {
				
					if (seatingCapacity <=4) {
						
						Payperiod.get().setPayperiod(TaxTypeEnum.LifeTax.getCode());
					} else {
						Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
					}
				}else if(Payperiod.get().getCovcode().equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode())||
						Payperiod.get().getCovcode().equalsIgnoreCase(ClassOfVehicleEnum.GCRT.getCovCode())) {
					if(gvw == null ) {
						logger.error("GVW not found for : [{}]",classOfVehicle);
						throw new BadRequestException("GVW not found for :  " + classOfVehicle);
					}
					if (gvw<=3000) {
						
						Payperiod.get().setPayperiod(TaxTypeEnum.LifeTax.getCode());
					} else {
						Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
					}
				}
		}

		if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.LifeTax.getCode())) {
			// return life tax
			listTaxperiod.add(TaxTypeEnum.LifeTax.getDesc());

		} else {
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
			if (State.equalsIgnoreCase(otherState)) {
				// return Q
				listTaxperiod.add(TaxTypeEnum.QuarterlyTax.getDesc());

			} else {
				if (quaterOne.contains(LocalDate.now().getMonthValue())) {
					// return Q ,h , y
					listTaxperiod.add(TaxTypeEnum.QuarterlyTax.getDesc());
					if(!restrictHalfAndYearForChassis(classOfVehicle)) {
					listTaxperiod.add(TaxTypeEnum.HalfyearlyTax.getDesc());
					listTaxperiod.add(TaxTypeEnum.YearlyTax.getDesc());
					}
				} else if (quaterTwo.contains(LocalDate.now().getMonthValue())) {
					// return Q
					listTaxperiod.add(TaxTypeEnum.QuarterlyTax.getDesc());
				} else if (quaterThree.contains(LocalDate.now().getMonthValue())) {
					// return Q ,h
					listTaxperiod.add(TaxTypeEnum.QuarterlyTax.getDesc());
					if(!restrictHalfAndYearForChassis(classOfVehicle)) {
					listTaxperiod.add(TaxTypeEnum.HalfyearlyTax.getDesc());
					}
				} else if (quaterFour.contains(LocalDate.now().getMonthValue())) {
					// return Q
					listTaxperiod.add(TaxTypeEnum.QuarterlyTax.getDesc());
				}
			}
		}
		return listTaxperiod;
	}
	private boolean restrictHalfAndYearForChassis(String cov) {
		
		if(ClassOfVehicleEnum.CHST.getCovCode().equalsIgnoreCase(cov)
				|| ClassOfVehicleEnum.CHSN.getCovCode().equalsIgnoreCase(cov)
				|| ClassOfVehicleEnum.ARVT.getCovCode().equalsIgnoreCase(cov)) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
	
	private boolean ownertypecheck(String code, String cov) {
		if (code.equalsIgnoreCase(OwnerTypeEnum.Government.getCode())
				|| code.equalsIgnoreCase(OwnerTypeEnum.POLICE.getCode())
				|| (code.equalsIgnoreCase(OwnerTypeEnum.Stu.getCode()) && cov.equalsIgnoreCase(ClassOfVehicleEnum.OBT.getCovCode()))) {
			return true;

		}
		return false;
	}
}
