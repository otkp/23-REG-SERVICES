package org.epragati.regservice;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.epragati.constants.OwnerTypeEnum;
import org.epragati.master.dto.MasterPayperiodDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.master.dto.TaxHelper;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.vo.RegServiceVO;
import org.epragati.util.payment.OtherStateApplictionType;
import org.epragati.util.payment.ServiceEnum;
import org.springframework.data.util.Pair;

public interface CitizenTaxService {

	TaxHelper getTaxDetails(String applicationNo, boolean isApplicationFromMvi, boolean isChassesApplication,
			String taxType,boolean isOtherState, String CitizenapplicationNo,List<ServiceEnum> serviceEnum,String permitType,String routeCode,
			Boolean isWeightAlt,String purpose,List<String> listOfVcrs,String oldPrNo,boolean specificVcrPayment );

	OtherStateApplictionType getOtherStateVehicleStatus(RegServiceVO regService);

	Double getOldCovTax(String cov, String seatingCapacity, Integer ulw, Integer gvw, String stateCode,
			String permitcode,String routeCode);

	TaxHelper greenTaxCalculation(String applicationNo,List<ServiceEnum> serviceEnum,List<String> listOfVcrs);
	TaxHelper greenTaxCalculation(String applicationNo,List<ServiceEnum> serviceEnum,RegServiceDTO regServiceDTO);
	boolean checkTaxUpToDateOrNote(boolean isApplicationFromMvi, boolean isChassesApplication,
			RegServiceDTO regServiceDTO, RegistrationDetailsDTO registrationDetails,
			StagingRegistrationDetailsDTO stagingRegistrationDetails, String taxType,boolean vcr);

	Integer getGvwWeightForCitizen(RegistrationDetailsDTO registrationDetails);
	
	LocalDate validity(String taxType);

	Pair<Optional<MasterPayperiodDTO>, Boolean> getPayPeroidForBoth(Optional<MasterPayperiodDTO> Payperiod,
			String seatingCapacity, Integer gvw);

	TaxHelper getLastPaidTax(RegistrationDetailsDTO stagingRegDetails, RegServiceDTO regServiceDTO,
			boolean isApplicationFromMvi, LocalDate currentTaxTill,
			StagingRegistrationDetailsDTO stagingRegistrationDetails, boolean isChassesApplication,
			List<String> taxTypes,boolean isOtherState,boolean vcr);
	
	LocalDate getBilaterTaxUpTo(LocalDate localDate);
	boolean secondVechileMasterData(String cov);

	Pair<String, String> getPermitCode(RegistrationDetailsDTO registrationDetails);

	Integer getUlwWeight(RegistrationDetailsDTO registrationDetails);


	TaxHelper getVoluntaryTax(String regNo, String trNo, String cov, Integer gvwc, Integer ulw, String seats,
			String makersModel, Double invoiceValue, String fuelDesc, LocalDate prGeneratedDate,
			boolean otherStateUnregister, boolean otherStateRegister, boolean unregisteredVehicle,
			OwnerTypeEnum ownerType, boolean firstVehicle, LocalDate dateOfCompletion,String taxType,boolean nocIssued,boolean withTP,LocalDate nocDate,
			LocalDate fcValidity,boolean vehicleHaveAitp);

	TaxHelper getcommenlastTaxPaid(LocalDate currentTaxTill, List<String> taxTypes, String applicationNo,
			List<TaxDetailsDTO> listOfTax);

	Pair<Integer, Integer> getMonthposition(LocalDate date);

	LocalDate getQuarterStatrtDate(Integer indexPosition, Integer quaternNumber, LocalDate dateOfCheck);

	LocalDate calculateTaxUpTo(Integer indexPosition, Integer quaternNumber);

	LocalDate calculateChassisTaxUpTo(Integer indexPosition, Integer quaternNumber, LocalDate date);

	List<TaxDetailsDTO> taxDetails(String applicationNo, List<String> taxType, String prNo);
	
	double calculateAgeOfTheVehicle(LocalDate localDateTime, LocalDate entryDate);

	boolean ownertypecheck(String code, String cov);

	TaxHelper vehicleRevokationTaxCalculation(RegistrationDetailsDTO stagingRegDetails, TaxHelper lastTaxTillDate,
			Double quaterTax, boolean vcr, TaxHelper vcrDetails);

	List<String> taxTypes();
}
