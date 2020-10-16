package org.epragati.vcr.service.impl;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.constants.CovCategory;
import org.epragati.constants.OwnerTypeEnum;
import org.epragati.master.dao.MasterCovDAO;
import org.epragati.master.dao.OwnershipDAO;
import org.epragati.master.dto.MasterCovDTO;
import org.epragati.master.dto.OwnershipDTO;
import org.epragati.master.vo.ApplicantAddressVO;
import org.epragati.master.vo.ApplicantDetailsVO;
import org.epragati.master.vo.ContactVO;
import org.epragati.master.vo.FcDetailsVO;
import org.epragati.master.vo.InvoiceDetailsVO;
import org.epragati.master.vo.OfficeVO;
import org.epragati.master.vo.RegistrationDetailsVO;
import org.epragati.master.vo.RegistrationValidityVO;
import org.epragati.master.vo.StateVO;
import org.epragati.master.vo.VCRVahanVehicleDetailsVO;
import org.epragati.master.vo.VahanDetailsVO;
import org.epragati.master.vo.VahanVehicleDetailsVO;
import org.epragati.permits.vo.PermitDetailsVO;
import org.epragati.permits.vo.PermitRouteDetailsVO;
import org.epragati.permits.vo.PermitValidityDetailsVO;
import org.epragati.permits.vo.RouteDetailsVO;
import org.epragati.regservice.otherstate.OtherStateVahanService;
import org.epragati.regservice.vo.TaxDetailsVahanVcrVO;
import org.epragati.util.DateConverters;
import org.epragati.util.payment.ModuleEnum;
import org.epragati.vcr.service.VcrVahanVehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class VcrVahanVehicleServiceImpl implements VcrVahanVehicleService {

	@Autowired
	private OtherStateVahanService otherStateVahanService;

	@Autowired
	private OwnershipDAO ownershipDAO;

	@Autowired
	private MasterCovDAO masterCovDAO;

	@Override
	public VCRVahanVehicleDetailsVO convertVahanVehicleToVcr(VahanVehicleDetailsVO vo, String prNo) {
		VCRVahanVehicleDetailsVO vcrVahanVehicleDetails = new VCRVahanVehicleDetailsVO();
		vcrVahanVehicleDetails.setRegDetailsVO(setRegistionDetails(vo));
		vcrVahanVehicleDetails.setTaxDetails(setTaxDetails(vo, prNo));
		vcrVahanVehicleDetails.setPermitDetailsVO(setPermitDetails(vo));
		vcrVahanVehicleDetails.setFcDetails(setFcDetails(vo));
		return vcrVahanVehicleDetails;
	}

	private RegistrationDetailsVO setRegistionDetails(VahanVehicleDetailsVO vo) {
		RegistrationDetailsVO registrationDetails = new RegistrationDetailsVO();
		registrationDetails.setApplicantDetails(setApplicantDetails(vo));
		registrationDetails.setVahanDetails(setVahanDetails(vo, registrationDetails));
		registrationDetails.setInsuranceDetails(otherStateVahanService.setInsuranceDetails(vo));
		registrationDetails.setInvoiceDetails(setInvoiceDetails(vo));
		registrationDetails.setRegistrationValidity(setRegValidity(vo));
		registrationDetails.setOfficeDetails(setOfficeDetails(vo));
		if (StringUtils.isNotEmpty(vo.getVehicleClass()) && !vo.getVehicleClass().equals("NA")) {
			registrationDetails.setClassOfVehicle(vo.getVehicleClass());
		}
		if (StringUtils.isNotEmpty(vo.getOwnerCd())) {
			registrationDetails.setOwnerType(setOwnership(vo.getOwnerCd()));
		}
		return registrationDetails;
	}

	private ApplicantDetailsVO setApplicantDetails(VahanVehicleDetailsVO vo) {
		ApplicantDetailsVO applicantDetails = new ApplicantDetailsVO();
		if (StringUtils.isNotEmpty(vo.getVehicleOwnerName()) && !vo.getVehicleOwnerName().equals("NA")) {
			applicantDetails.setDisplayName(vo.getVehicleOwnerName());
			applicantDetails.setFirstName(vo.getVehicleOwnerName());
		} else {
			applicantDetails.setDisplayName(replaceDefaults(vo.getVehicleOwnerName()));
			applicantDetails.setFirstName(replaceDefaults(vo.getVehicleOwnerName()));
		}
		if (StringUtils.isNotEmpty(vo.getGender()) && !vo.getGender().equals("NA")) {
			applicantDetails.setGender(vo.getGender());
		} else {
			applicantDetails.setGender(replaceDefaults(vo.getGender()));
		}
		if (StringUtils.isNotEmpty(vo.getDateOfBirth()) && !vo.getDateOfBirth().equals("NA")) {
			applicantDetails.setDateOfBirth(DateConverters.convertStirngTOlocalDate(vo.getDateOfBirth()));
		}
		applicantDetails.setPermanantAddress(setPermanantAddress(vo));
		applicantDetails.setContact(setContactDetails(vo));
		return applicantDetails;
	}

	private String replaceDefaults(String input) {

		if (StringUtils.isBlank(input) || input.equals("NA")) {
			return StringUtils.EMPTY;
		}
		return input;
	}

	private ContactVO setContactDetails(VahanVehicleDetailsVO vo) {
		ContactVO contactVO = new ContactVO();
		if (StringUtils.isNotEmpty(vo.getMobileNumber()) && !vo.getMobileNumber().equals("NA")) {
			contactVO.setMobile(vo.getMobileNumber());
		} else {
			contactVO.setMobile(replaceDefaults(vo.getMobileNumber()));
		}
		if (StringUtils.isNotEmpty(vo.getEmailId()) && !vo.getEmailId().equals("NA")) {
			contactVO.setEmail(vo.getEmailId());
		} else {
			contactVO.setMobile(replaceDefaults(vo.getEmailId()));
		}
		return contactVO;
	}

	private ApplicantAddressVO setPermanantAddress(VahanVehicleDetailsVO vo) {
		ApplicantAddressVO permanantAddress = new ApplicantAddressVO();
		if (StringUtils.isNotEmpty(vo.getVehicleOwnerAddress()) && !vo.getVehicleOwnerAddress().equals("NA")) {
			permanantAddress.setDoorNo(vo.getVehicleOwnerAddress());
		}
		if (StringUtils.isNotEmpty(vo.getDistrictName()) && !vo.getDistrictName().equals("NA")) {
			permanantAddress.setOtherDistrict(vo.getDistrictName());
		} else {
			permanantAddress.setOtherDistrict(replaceDefaults(vo.getDistrictName()));
		}
		if (StringUtils.isNotEmpty(vo.getStateCode())) {
			Optional<StateVO> stateOpt = otherStateVahanService.setStateDetails(vo.getStateCode());
			if (stateOpt.isPresent()) {
				permanantAddress.setState(stateOpt.get());
			} else {
				permanantAddress.setOtherState(vo.getStateCode());
			}
		}
		return permanantAddress;
	}

	private VahanDetailsVO setVahanDetails(VahanVehicleDetailsVO vo, RegistrationDetailsVO registrationDetails) {
		VahanDetailsVO vahanDetails = new VahanDetailsVO();
		if (StringUtils.isNotEmpty(vo.getChasisNo()) && !vo.getChasisNo().equals("NA")) {
			vahanDetails.setChassisNumber(vo.getChasisNo());
		}
		if (StringUtils.isNoneEmpty(vo.getEngineNo()) && !vo.getEngineNo().equals("NA")) {
			vahanDetails.setEngineNumber(vo.getEngineNo());
		}
		if (StringUtils.isNotEmpty(vo.getVehicleColor()) && vo.getVehicleColor().equals("NA")) {
			vahanDetails.setColor(vo.getVehicleColor());
		}
		if (StringUtils.isNotEmpty(vo.getManuMonth()) && StringUtils.isNotEmpty(vo.getManuYear())) {
			StringBuilder manuMonthYear = new StringBuilder();
			String manuMonth = StringUtils.EMPTY;
			if (vo.getManuMonth().length() < 2) {
				manuMonth = "0" + vo.getManuMonth();
			} else {
				manuMonth = vo.getManuMonth();
			}
			manuMonthYear.append(manuMonth).append(vo.getManuYear());
			vahanDetails.setManufacturedMonthYear(manuMonthYear.toString());
		}
		if (StringUtils.isNotEmpty(vo.getVehicleModel()) && vo.getVehicleModel().equals("NA")) {
			vahanDetails.setMakersModel(vo.getVehicleModel());
		}
		if (vo.getNoOfSeat() != null) {
			vahanDetails.setSeatingCapacity(vo.getNoOfSeat().toString());
		}
		if (StringUtils.isNotEmpty(vo.getFrontAxlDesc()) && !vo.getFrontAxlDesc().equals("NA")) {
			vahanDetails.setFrontAxleDesc(vo.getFrontAxlDesc());
		}
		if (StringUtils.isNotEmpty(vo.getRearAxleDesc()) && vo.getRearAxleDesc().equals("NA")) {
			vahanDetails.setRearAxleDesc(vo.getRearAxleDesc());
		}
		if (vo.getFaxleWeight() != null) {
			vahanDetails.setFrontAxleWeight(vo.getFaxleWeight());
		}
		if (vo.getRaxleWeight() != null) {
			vahanDetails.setRearAxleWeight(vo.getRaxleWeight());
		}
		if (StringUtils.isNotEmpty(vo.getGvw()) && vo.getGvw().equals("NA")) {
			vahanDetails.setGvw(convertStringToInteger(vo.getGvw()));
		}
		if (StringUtils.isNotEmpty(vo.getMakerName()) && vo.getMakerName().equals("NA")) {
			vahanDetails.setMakersDesc(vo.getMakerName());
		}
		if (StringUtils.isNotEmpty(vo.getNormsDesc()) && !vo.getNormsDesc().equals("NA")) {
			vahanDetails.setPollutionNormsDesc(vo.getNormsDesc());
		}
		if (vo.getUlw() != null) {
			vahanDetails.setUnladenWeight(vo.getUlw());
		}
		if (vo.getNoOfAxle() != null) {
			vahanDetails.setNoCyl(vo.getNoOfAxle().toString());
		}
		if (StringUtils.isNotEmpty(vo.getVehicleClass()) && !vo.getVehicleClass().equals("NA")) {
			vahanDetails.setDealerCovType(setCovs(vo.getVehicleClass(), registrationDetails));
		}
		if (StringUtils.isNotEmpty(vo.getBodyType()) && !vo.getBodyType().equals("NA")) {
			vahanDetails.setBodyTypeDesc(vo.getBodyType());
		}
		if (StringUtils.isNotEmpty(vo.getFuelDesc()) && !vo.getFuelDesc().equals("NA")) {
			vahanDetails.setFuelDesc(vo.getFuelDesc());
		}
		if (vo.getCubicCap() != null) {
			vahanDetails.setCubicCapacity(vo.getCubicCap().toString());
		}
		if (StringUtils.isNotEmpty(vo.getTandemAxleDesc()) && vo.getTandemAxleDesc().equals("NA")) {
			vahanDetails.setTandemAxelDescp(vo.getTandemAxleDesc());
		}
		if (StringUtils.isNotEmpty(vo.getTandamAxleWeight()) && vo.getTandamAxleWeight().equals("NA")) {
			vahanDetails.setTandemAxelWeight(convertStringToInteger(vo.getTandamAxleWeight()));
		}
		return vahanDetails;
	}

	@Override
	public Integer convertStringToInteger(String value) {
		if (null != value && !value.equals("NA")) {
			return Integer.parseInt(value);
		}
		return 0;
	}

	private InvoiceDetailsVO setInvoiceDetails(VahanVehicleDetailsVO vo) {
		InvoiceDetailsVO invoiceDetailsVO = new InvoiceDetailsVO();
		if (StringUtils.isNotEmpty(vo.getInvoiceDate()) && !vo.getInvoiceDate().equals("NA")) {
			invoiceDetailsVO.setInvoiceDateForOther(DateConverters.convertStirngTOlocalDate(vo.getInvoiceDate()));
		}
		if (StringUtils.isNotEmpty(vo.getInvoiceNumber()) && !vo.getInvoiceDate().equals("NA")) {
			invoiceDetailsVO.setInvoiceNo(vo.getInvoiceNumber());
		}
		if (vo.getSaleAmout() != null) {
			invoiceDetailsVO.setInvoiceValue(vo.getSaleAmout());
		}
		if (StringUtils.isNotEmpty(vo.getVehicleRegDate())) {
			invoiceDetailsVO.setPurchaseDateForOther(DateConverters.convertStirngTODate(vo.getVehicleRegDate()));
		}
		return invoiceDetailsVO;
	}

	private RegistrationValidityVO setRegValidity(VahanVehicleDetailsVO vo) {
		RegistrationValidityVO registrationValidityVO = new RegistrationValidityVO();
		if (StringUtils.isNotEmpty(vo.getVehicleRegDate())) {
			registrationValidityVO.setPrGeneratedDate(DateConverters.convertStirngTODate(vo.getVehicleRegDate()));
		}
		if (StringUtils.isNotEmpty(vo.getValidityOfFitnessCeritificate())) {
			registrationValidityVO
			.setRegistrationValidity(getTimewithDate(vo.getValidityOfFitnessCeritificate(), false));

		}
		return registrationValidityVO;
	}

	private TaxDetailsVahanVcrVO setTaxDetails(VahanVehicleDetailsVO vo, String prNo) {
		TaxDetailsVahanVcrVO taxDetails = new TaxDetailsVahanVcrVO();
		taxDetails.setPrNo(prNo);
		taxDetails.setModule(ModuleEnum.REG.getCode());
		taxDetails.setApplicationNo(replaceDefaults(vo.getTaxPaidReceiptNo()));
		if (StringUtils.isNotEmpty(vo.getTaxPaidReceiptNo()) && !vo.getTaxPaidReceiptNo().equals("NA")) {
			taxDetails.setApplicationNo(vo.getTaxPaidReceiptNo());
		}
		if (StringUtils.isNotEmpty(vo.getTaxPaidOnDate()) && !vo.getTaxPaidOnDate().equals("NA")) {
			String[] taxEndDateArray = vo.getTaxPaidOnDate().split(" ");
			taxDetails.setTaxPeriodFrom(DateConverters.convertStirngTODate(taxEndDateArray[0]));
		}
		if (StringUtils.isNotEmpty(vo.getTaxPaidUpto()) && !vo.getTaxPaidUpto().equals("NA")) {
			taxDetails.setTaxPeriodEnd(DateConverters.convertStirngTODate(vo.getTaxPaidUpto()));
		}
		if (StringUtils.isNotEmpty(vo.getTaxPaidDepositedAt()) && !vo.getTaxPaidDepositedAt().equals("NA")) {
			taxDetails.setOfficeCode(vo.getTaxPaidDepositedAt());
		}
		if (vo.getSaleAmout() != null) {
			taxDetails.setInvoiceValue(vo.getSaleAmout());
		}
		if (StringUtils.isNotEmpty(vo.getStateCode()) && !vo.getStateCode().equals("NA")) {
			taxDetails.setStateCode(vo.getStateCode());
		}
		return taxDetails;
	}

	private LocalDateTime getTimewithDate(String date, Boolean timeZone) {
		String dateVal = date + "T00:00:00.000Z";
		if (timeZone) {
			dateVal = date + "T23:59:59.999Z";
		}
		ZonedDateTime zdt = ZonedDateTime.parse(dateVal);
		return zdt.toLocalDateTime();
	}

	private OfficeVO setOfficeDetails(VahanVehicleDetailsVO vo) {
		OfficeVO office = new OfficeVO();
		if (StringUtils.isNotEmpty(vo.getTaxPaidDepositedAt()) && !vo.getRegistaringAuthorityName().equals("NA")) {
			office.setOffice(vo.getTaxPaidDepositedAt());
		}
		if (StringUtils.isNotEmpty(vo.getRegistaringAuthorityName())
				&& !vo.getRegistaringAuthorityName().equals("NA")) {
			office.setOfficeName(vo.getRegistaringAuthorityName());
		}
		return office;
	}

	private OwnerTypeEnum setOwnership(String ownerTypr) {
		OwnerTypeEnum ownerTypeEnum = null;
		if (StringUtils.isNotEmpty(ownerTypr)) {
			Optional<OwnershipDTO> ownershipOpt = ownershipDAO.findByNicCodes(ownerTypr);
			if (ownershipOpt.isPresent()) {
				if (ownershipOpt.get().getNicOwnerCode() != null) {
					switch (ownershipOpt.get().getNicOwnerCode()) {
					case 1:
						ownerTypeEnum = OwnerTypeEnum.Individual;
						break;
					case 3:
						ownerTypeEnum = OwnerTypeEnum.Company;
						break;
					case 5:
						ownerTypeEnum = OwnerTypeEnum.Government;
						break;
					case 9:
						ownerTypeEnum = OwnerTypeEnum.Organization;
						break;
					case 12:
						ownerTypeEnum = OwnerTypeEnum.Diplomatic;
						break;
					case 15:
						ownerTypeEnum = OwnerTypeEnum.POLICE;
						break;
					case 16:
						ownerTypeEnum = OwnerTypeEnum.Stu;
						break;
					default:
						break;
					}
				}
			}
		}
		return ownerTypeEnum;
	}

	@Override
	public List<String> setCovs(String covCode, RegistrationDetailsVO registrationDetails) {
		List<MasterCovDTO> covList = masterCovDAO.findByNicCovCode(convertStringToInteger(covCode));
		if (CollectionUtils.isNotEmpty(covList)) {
			if (covList.stream().allMatch(t -> t.getCategory().equals(CovCategory.T.getCode()))) {
				registrationDetails.setVehicleType(CovCategory.T.getCode());
			} else if (covList.stream().allMatch(n -> n.getCategory().equals(CovCategory.N.getCode()))) {
				registrationDetails.setVehicleType(CovCategory.N.getCode());
			} else {
				registrationDetails.setVehicleType(StringUtils.EMPTY);
			}
			return covList.stream().map(val -> val.getCovcode()).collect(Collectors.toList());
		}
		return Collections.emptyList();
	}

	private PermitDetailsVO setPermitDetails(VahanVehicleDetailsVO vo) {
		PermitDetailsVO permitDetails = null;
		if (vo.getPermitNo() != null || vo.getRouteName() != null || vo.getRouteName() != null
				|| vo.getPermitValidity() != null || vo.getPermitIssuedByOffice() != null) {
			permitDetails = new PermitDetailsVO();
		}
		if (StringUtils.isNotEmpty(vo.getPermitNo()) && !vo.getPermitNo().equals("NA")) {
			permitDetails.setPermitNo(vo.getPermitNo());
		}
		if (StringUtils.isNoneEmpty(vo.getRouteName()) && !vo.getRouteName().equals("NA")) {
			RouteDetailsVO routeDetails = new RouteDetailsVO();
			routeDetails.setPermitRouteDetails(setPermitRouteDetails(vo));
			permitDetails.setRouteDetailsVO(routeDetails);
		}
		if (StringUtils.isNoneEmpty(vo.getPermitValidity()) && !vo.getPermitValidity().equals("NA")) {
			PermitValidityDetailsVO permitValidityDetails = new PermitValidityDetailsVO();
			String[] permitValidToArray = vo.getPermitValidity().split(" ");
			permitValidityDetails.setPermitValidTo(DateConverters.convertStirngTODate(permitValidToArray[0]));
			permitDetails.setPermitValidityDetailsVO(permitValidityDetails);
		}
		return permitDetails;
	}

	private PermitRouteDetailsVO setPermitRouteDetails(VahanVehicleDetailsVO vo) {
		PermitRouteDetailsVO permitRouteDetails = new PermitRouteDetailsVO();
		if (StringUtils.isNoneEmpty(vo.getRouteName()) && !vo.getRouteName().equals("NA")) {
			permitRouteDetails.setDescription(vo.getRouteName());
		}
		if (StringUtils.isNoneEmpty(vo.getViaRoute()) && !vo.getViaRoute().equals("NA")) {
			permitRouteDetails.setDescription(vo.getViaRoute());
		}
		return permitRouteDetails;
	}

	private FcDetailsVO setFcDetails(VahanVehicleDetailsVO vo) {
		FcDetailsVO fcDetails = new FcDetailsVO();
		if (StringUtils.isNoneEmpty(vo.getFitnessNumber()) && !vo.getFitnessNumber().equals("NA")) {
			fcDetails.setFcNumber(vo.getFitnessNumber());
		}
		if (StringUtils.isNoneEmpty(vo.getValidityOfFitnessCeritificate())
				&& !vo.getValidityOfFitnessCeritificate().equals("NA")) {
			fcDetails.setFcValidUpto(DateConverters.convertStirngTODate(vo.getValidityOfFitnessCeritificate()));
		}
		return fcDetails;
	}
}
