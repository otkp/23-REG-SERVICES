package org.epragati.regservice.otherstate.impl;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.constants.CovCategory;
import org.epragati.constants.NationalityEnum;
import org.epragati.constants.OwnerTypeEnum;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.CountryDAO;
import org.epragati.master.dao.InsuranceCompanyDAO;
import org.epragati.master.dao.InsuranceTypeDAO;
import org.epragati.master.dao.MakersDAO;
import org.epragati.master.dao.MasterCovDAO;
import org.epragati.master.dao.OtherStateVahanResponseDAO;
import org.epragati.master.dao.OwnershipDAO;
import org.epragati.master.dao.StateDAO;
import org.epragati.master.dto.ApplicantDetailsDTO;
import org.epragati.master.dto.CountryDTO;
import org.epragati.master.dto.FinanceDetailsDTO;
import org.epragati.master.dto.InsuranceCompanyDTO;
import org.epragati.master.dto.InsuranceTypeDTO;
import org.epragati.master.dto.InvoiceDetailsDTO;
import org.epragati.master.dto.MakersDTO;
import org.epragati.master.dto.MasterCovDTO;
import org.epragati.master.dto.OtherStateVahanResponseDTO;
import org.epragati.master.dto.OwnershipDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.StateDTO;
import org.epragati.master.dto.VahanDetailsDTO;
import org.epragati.master.mappers.ContactMapper;
import org.epragati.master.mappers.CountryMapper;
import org.epragati.master.mappers.InsuranceCompanyMapper;
import org.epragati.master.mappers.InsuranceTypeMapper;
import org.epragati.master.mappers.MakersMapper;
import org.epragati.master.mappers.OwnershipMapper;
import org.epragati.master.mappers.StateMapper;
import org.epragati.master.vo.ApplicantAddressVO;
import org.epragati.master.vo.ApplicantDetailsVO;
import org.epragati.master.vo.CountryVO;
import org.epragati.master.vo.FinanceDetailsVO;
import org.epragati.master.vo.InsuranceCompanyVO;
import org.epragati.master.vo.InsuranceDetailsVO;
import org.epragati.master.vo.InsuranceTypeVO;
import org.epragati.master.vo.InvoiceDetailsVO;
import org.epragati.master.vo.MakersVO;
import org.epragati.master.vo.MasterVariantVO;
import org.epragati.master.vo.OwnershipVO;
import org.epragati.master.vo.RegistrationDetailsVO;
import org.epragati.master.vo.RegistrationValidityVO;
import org.epragati.master.vo.StateVO;
import org.epragati.master.vo.VahanDetailsVO;
import org.epragati.master.vo.VahanVehicleDetailsVO;
import org.epragati.payment.mapper.ClassOfVehiclesMapper;
import org.epragati.payments.vo.ClassOfVehiclesVO;
import org.epragati.regservice.dto.NOCDetailsDTO;
import org.epragati.regservice.otherstate.OtherStateVahanService;
import org.epragati.regservice.vo.NOCDetailsVO;
import org.epragati.regservice.vo.OtherStateCovVO;
import org.epragati.regservice.vo.OtherStateRegVO;
import org.epragati.regservice.vo.OtherStateVahanVO;
import org.epragati.regservice.vo.PUCDetailsVO;
import org.epragati.regservice.vo.RegServiceVO;
import org.epragati.util.DateConverters;
import org.epragati.vcr.service.VcrVahanVehicleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
public class OtherStateVahanServiceImpl implements OtherStateVahanService{

	@Autowired
	private StateDAO stateDAO;
	
	@Autowired 
	private StateMapper stateMapper;
	
	@Autowired
	private ContactMapper contactMapper;
	
	@Autowired
	private InsuranceTypeDAO insuranceTypeDAO;
	
	@Autowired
	private OwnershipDAO ownershipDAO;
	
	@Autowired
	private OwnershipMapper ownershipMapper;
	
	@Autowired
	private CountryDAO countryDAO;
	
	@Autowired
	private CountryMapper countryMapper;
	
	@Autowired
	private VcrVahanVehicleService vcrVahanVehicleService;
	
	@Autowired
	private MasterCovDAO masterCovDAO;
	
	@Autowired
	private ClassOfVehiclesMapper classOfVehiclesMapper;
	
	@Autowired
	private MakersDAO makersDAO;
	
	@Autowired
	private MakersMapper makersMapper;
	
	@Autowired
	private InsuranceTypeMapper insuranceTypeMapper;
	
	@Autowired
	private InsuranceCompanyDAO insuranceCompanyDAO; 
	
	@Autowired
	private InsuranceCompanyMapper insuranceCompanyMapper;
	
	@Autowired
	private OtherStateVahanResponseDAO otherStateVahanResponseDAO;
	
	@Override
	public Pair<OtherStateVahanVO, List<String>>convertVahanVehicleToOtherState(VahanVehicleDetailsVO vo, String prNo) {
		OtherStateVahanVO otherStateVO = new OtherStateVahanVO();
		
		otherStateVO.setOtherStateRegVO(setRegDetails(vo,prNo));
		otherStateVO.setOtherStateCovVO(setCovMakers(vo,otherStateVO.getOtherStateRegVO().getRegistrationDetails()));
			otherStateVO.getOtherStateRegVO().setGetOtherStateDataFromVahanService(true);
		if (otherStateVO.getOtherStateCovVO() != null && otherStateVO.getOtherStateCovVO().getOwnerType() != null
				&& otherStateVO.getOtherStateRegVO() != null
				&& otherStateVO.getOtherStateRegVO().getRegistrationDetails() != null
				&& otherStateVO.getOtherStateRegVO().getRegistrationDetails().getApplicantDetails() != null) {
			setRepresentativeName(otherStateVO.getOtherStateCovVO().getOwnerType(),otherStateVO.getOtherStateRegVO().getRegistrationDetails().getApplicantDetails());
		}
		return Pair.of(otherStateVO, Collections.emptyList());
	}
	
	ApplicantDetailsVO setApplicateDetails(VahanVehicleDetailsVO vo ){
		ApplicantDetailsVO applicantDetailsVO = new ApplicantDetailsVO();
		if(StringUtils.isNotEmpty(vo.getDateOfBirth())&&!vo.getDateOfBirth().equalsIgnoreCase("NA")&&!vo.getDateOfBirth().equalsIgnoreCase("N/A")){
			applicantDetailsVO.setDateOfBirth(DateConverters.convertStirngTOlocalDate(vo.getDateOfBirth()));
		}
		if(StringUtils.isNotEmpty(vo.getGender())&&!vo.getDateOfBirth().equalsIgnoreCase("NA")&&vo.getDateOfBirth().equalsIgnoreCase("N/A")){
			applicantDetailsVO.setGender(vo.getGender());
		}
		if(StringUtils.isNotEmpty(vo.getVehicleOwnerName())&&!vo.getVehicleOwnerName().equalsIgnoreCase("NA")&&!vo.getVehicleOwnerName().equalsIgnoreCase("N/A")){
			applicantDetailsVO.setFirstName(vo.getVehicleOwnerName());
		}if(StringUtils.isNotEmpty(vo.getVehicleOwnerName())&&vo.getVehicleOwnerName().length()<=21){
			applicantDetailsVO.setDisplayName(vo.getVehicleOwnerName());
		}
		
		if(StringUtils.isNotEmpty(vo.getFatherName())&&!vo.getFatherName().equalsIgnoreCase("NA")&&!vo.getFatherName().equalsIgnoreCase("N/A")){
			applicantDetailsVO.setFatherName(vo.getFatherName());
		}
		applicantDetailsVO.setPermanantAddress(setPermanantAddressDetails(vo));
		applicantDetailsVO.setPresentAddress(setPresentAddressDetails());
		applicantDetailsVO.setContact(contactMapper.convertVahanfildes(vo));
		applicantDetailsVO.setNationality(NationalityEnum.INDIAN.getName());
		return applicantDetailsVO;
	}
	
	private VahanDetailsVO setVehicleDetails(VahanVehicleDetailsVO vo){
		VahanDetailsVO vahanDetailsVO = new VahanDetailsVO();
		if(StringUtils.isNotEmpty(vo.getChasisNo())&&!vo.getChasisNo().equalsIgnoreCase("NA")&&!vo.getChasisNo().equalsIgnoreCase("N/A")){
			vahanDetailsVO.setChassisNumber(vo.getChasisNo());
		}
		if(StringUtils.isNotEmpty(vo.getEngineNo())&&!vo.getEngineNo().equalsIgnoreCase("NA")&&!vo.getEngineNo().equalsIgnoreCase("N/A")){
			vahanDetailsVO.setEngineNumber(vo.getEngineNo());
		}
		if(vo.getGvw()!=null&&!vo.getGvw().equalsIgnoreCase("NA")&&!vo.getGvw().equalsIgnoreCase("N/A")){
			vahanDetailsVO.setGvw(vcrVahanVehicleService.convertStringToInteger(vo.getGvw()));
			vahanDetailsVO.setRearAxleWeight(vcrVahanVehicleService.convertStringToInteger(vo.getGvw()));
		}
		if (vo.getUlw() != null&&!String.valueOf(vo.getUlw()).equalsIgnoreCase("NA")&&!String.valueOf(vo.getUlw()).equalsIgnoreCase("N/A")&&!vo.getUlw().equals(0)) {
			vahanDetailsVO.setUnladenWeight(vo.getUlw());
		} else if (vo.getuLW() != null&&!String.valueOf(vo.getuLW()).equalsIgnoreCase("NA")&&!String.valueOf(vo.getuLW()).equalsIgnoreCase("N/A")&&!vo.getuLW().equals(0)) {
			vahanDetailsVO.setUnladenWeight(vo.getuLW());
		}
		if(vo.getNoOfSeat()!=null&&!String.valueOf(vo.getNoOfSeat()).equalsIgnoreCase("NA")&&!String.valueOf(vo.getNoOfSeat()).equalsIgnoreCase("N/A")
				&&!vo.getNoOfSeat().equals(0)){
			vahanDetailsVO.setSeatingCapacity(vo.getNoOfSeat().toString());
		}
		if(vo.getVehicleModel()!=null&&!vo.getVehicleModel().equalsIgnoreCase("NA")&&!vo.getVehicleModel().equalsIgnoreCase("N/A")){
			vahanDetailsVO.setMakersModel(vo.getVehicleModel());
		}
		if(vo.getMakerName()!=null&&!vo.getMakerName().equalsIgnoreCase("NA")&&!vo.getMakerName().equalsIgnoreCase("N/A")){
			vahanDetailsVO.setMakersDesc(vo.getMakerName());
		}
		if (StringUtils.isNotEmpty(vo.getManuMonth()) && !vo.getManuMonth().equalsIgnoreCase("NA")&&!vo.getManuMonth().equalsIgnoreCase("N/A")
				&& !vo.getManuMonth().equalsIgnoreCase("0") && !!vo.getManuMonth().equalsIgnoreCase("00")
				&& StringUtils.isNotEmpty(vo.getManuYear()) && !vo.getManuYear().equalsIgnoreCase("NA")&&!vo.getManuYear().equalsIgnoreCase("N/A")&&!vo.getManuYear().equalsIgnoreCase("0000")) {
			StringBuilder manuMonthYear = new StringBuilder();
			String manuMonth = StringUtils.EMPTY;
			if (vo.getManuMonth().length() < 2) {
				manuMonth = "0" + vo.getManuMonth();
			} else {
				manuMonth = vo.getManuMonth();
			}
			manuMonthYear.append(manuMonth).append("-").append(vo.getManuYear());
			vahanDetailsVO.setManufacturedMonthYear(manuMonthYear.toString());
		}
		if(StringUtils.isNotEmpty(vo.getVehicleColor())&&!vo.getVehicleColor().equalsIgnoreCase("NA")&&!vo.getVehicleColor().equalsIgnoreCase("N/A")){
			vahanDetailsVO.setColor(vo.getVehicleColor());
		}
		if (vo.getWheelBase() != null && !String.valueOf(vo.getWheelBase()).equalsIgnoreCase("NA")&&!String.valueOf(vo.getWheelBase()).equalsIgnoreCase("N/A") && !vo.getWheelBase().equals(0)) {
			vahanDetailsVO.setWheelbase(vo.getWheelBase());
		}
		if(vo.getCubicCap()!=null&&!String.valueOf(vo.getCubicCap()).equalsIgnoreCase("NA")&&!String.valueOf(vo.getCubicCap()).equalsIgnoreCase("N/A")){
			vahanDetailsVO.setCubicCapacity(vo.getCubicCap().toString());
		}
		if (StringUtils.isNotEmpty(vo.getBodyType()) && !vo.getBodyType().equalsIgnoreCase("NA")&&!vo.getBodyType().equalsIgnoreCase("N/A")) {
			vahanDetailsVO.setBodyTypeDesc(vo.getBodyType());
		}
		if (StringUtils.isNotEmpty(vo.getFuelDesc()) && !vo.getFuelDesc().equalsIgnoreCase("NA")&&!vo.getFuelDesc().equalsIgnoreCase("N/A")) {
			vahanDetailsVO.setFuelDesc(vo.getFuelDesc());
		}
		return vahanDetailsVO;
	}
	
	private ApplicantAddressVO setPermanantAddressDetails(VahanVehicleDetailsVO vo){
		ApplicantAddressVO applicantAddressVO = new ApplicantAddressVO();
		applicantAddressVO.setOtherCountry(NationalityEnum.INDIAN.getName());
		applicantAddressVO.setCountry(setCountryCode().get());
		if(StringUtils.isNotEmpty(vo.getDistrictName())&& !vo.getDistrictName().equalsIgnoreCase("NA")&&!vo.getDistrictName().equalsIgnoreCase("N/A")){
			applicantAddressVO.setOtherDistrict(vo.getDistrictName());
		}
		if (StringUtils.isNotEmpty(vo.getStateCode())&& !vo.getStateCode().equalsIgnoreCase("NA")&&!vo.getStateCode().equalsIgnoreCase("N/A")) {
			Optional<StateVO> stateOpt = setStateDetails(vo.getStateCode());
			if (stateOpt.isPresent()) {
				applicantAddressVO.setState(stateOpt.get());
			}else{
				applicantAddressVO.setOtherState(vo.getStateCode());
			}
		}
		if (StringUtils.isNotBlank(vo.getVehicleOwnerAddress())&&!vo.getVehicleOwnerAddress().equalsIgnoreCase("NA")&&!vo.getVehicleOwnerAddress().equalsIgnoreCase("N/A")) {
			applicantAddressVO.setDoorNo(vo.getVehicleOwnerAddress());
		}
		return applicantAddressVO;
	}
	
	private ApplicantAddressVO setPresentAddressDetails(){
		ApplicantAddressVO applicantAddressVO = new ApplicantAddressVO();
		applicantAddressVO.setCountry(setCountryCode().get());
		applicantAddressVO.setState(setStateDetails("AP").get());
		return applicantAddressVO;
	}
	
	
	
	@Override
	public InsuranceDetailsVO setInsuranceDetails(VahanVehicleDetailsVO vo){
		LocalDate insFromDate = null;
		LocalDate insToDate = null;
		Optional<InsuranceTypeVO>  insuranceTypeOpt = Optional.empty(); 
		InsuranceDetailsVO insuranceDetailsVO = new InsuranceDetailsVO();
		if(StringUtils.isNotEmpty(vo.getVehicleInsuranceCompanyName())){
		insuranceDetailsVO.setCompany(vo.getVehicleInsuranceCompanyName());
		}
		if(insuranceTypeOpt.isPresent()){
			insuranceTypeOpt = setInsPolicyType(vo.getInsType());
		insuranceDetailsVO.setPolicyType(insuranceTypeOpt.get().getInsDescription());
		}
		if(StringUtils.isNotEmpty(vo.getInsFrom())&&!vo.getInsFrom().equalsIgnoreCase("NA")&&!vo.getInsFrom().equalsIgnoreCase("N/A")){
			insFromDate=DateConverters.convertStirngTODate(vo.getInsFrom());
			insuranceDetailsVO.setValidFromForOther(insFromDate);
		}
		if(StringUtils.isNotEmpty(vo.getValidityOfInsurance())&&!vo.getValidityOfInsurance().equalsIgnoreCase("NA")&&!vo.getValidityOfInsurance().equalsIgnoreCase("N/A")){
		insToDate= DateConverters.convertStirngTODate(vo.getValidityOfInsurance());
		insuranceDetailsVO.setValidTillForOther(insToDate);
		}
		insuranceDetailsVO.setValidFromForOther(insFromDate);
		insuranceDetailsVO.setValidTillForOther(insToDate);
		if(StringUtils.isNotEmpty(vo.getPolicyNo())&&!vo.getPolicyNo().equalsIgnoreCase("NA")&&!vo.getPolicyNo().equalsIgnoreCase("N/A")){
			insuranceDetailsVO.setPolicyNumber(vo.getPolicyNo());
		}
		if(insToDate!=null&&insFromDate!=null){
		long numberOfYears = ChronoUnit.YEARS.between(insFromDate,insToDate.plusDays(1));
		Integer tenure = (int) (numberOfYears);
		insuranceDetailsVO.setTenure(tenure);
		}
		return insuranceDetailsVO;
	}
	
	private Optional<InsuranceTypeVO> setInsPolicyType(String nicInsCode){
		Optional<InsuranceTypeDTO> insuranceTypeOpt = insuranceTypeDAO.findByNicCode(Integer.parseInt(nicInsCode));
		if(insuranceTypeOpt.isPresent()){
			return Optional.of(insuranceTypeMapper.convertEntity(insuranceTypeOpt.get()));
		}
		return Optional.empty();
	}
	
	private NOCDetailsVO setNocDetails(VahanVehicleDetailsVO vo){
		NOCDetailsVO nOCDetailsVO = new NOCDetailsVO();
		if(StringUtils.isNotEmpty(vo.getStateTo())&&!vo.getStateTo().equalsIgnoreCase("NA")&&!vo.getStateTo().equalsIgnoreCase("N/A")){
		nOCDetailsVO.setState(setNocStateName(vo.getStateTo()));
		}
		if(StringUtils.isNotEmpty(vo.getNocDate())&&!vo.getNocDate().equalsIgnoreCase("NA")&&!vo.getNocDate().equalsIgnoreCase("N/A")){
			nOCDetailsVO.setIssueDate(DateConverters.convertStirngTODate(vo.getNocDate()));
		}
		if(StringUtils.isNotEmpty(vo.getRegistaringAuthorityName())&&!vo.getRegistaringAuthorityName().equalsIgnoreCase("NA")&&!vo.getRegistaringAuthorityName().equalsIgnoreCase("N/A")){
			nOCDetailsVO.setRtaOffice(vo.getRegistaringAuthorityName());
		}
		if(StringUtils.isNotEmpty(vo.getOfficeTo())&&!vo.getOfficeTo().equalsIgnoreCase("NA")&&!vo.getOfficeTo().equalsIgnoreCase("NA")){
			nOCDetailsVO.setnOCIssuedRtaOffice(vo.getOfficeTo());
		}
		return nOCDetailsVO;
	}
	
	private FinanceDetailsVO setFinancierDetails(VahanVehicleDetailsVO vo,RegistrationDetailsVO registrationDetails){
		FinanceDetailsVO financeDetailsVO = new FinanceDetailsVO();
		if (StringUtils.isNotEmpty(vo.getFinancerState())&&!vo.getFinancerState().equalsIgnoreCase("NA")&&!vo.getFinancerState().equalsIgnoreCase("N/A")) {
			Optional<StateVO> stateOpt = setStateDetails(vo.getFinancerState());
			if (stateOpt.isPresent()&&StringUtils.isNotEmpty(stateOpt.get().getStateName())) {
				financeDetailsVO.setOtherStateState(stateOpt.get().getStateName());
			} else {
				financeDetailsVO.setOtherStateState(vo.getFinancerState());
			}
		}
		if(StringUtils.isNotEmpty(vo.getFinancerDist())&&!vo.getFinancerDist().equalsIgnoreCase("NA")&&!vo.getFinancerDist().equalsIgnoreCase("N/A")){
			financeDetailsVO.setOtherStateDistrict(vo.getFinancerDist());
		}
		if(StringUtils.isNotEmpty(vo.getFinancerName())&&!vo.getFinancerName().equalsIgnoreCase("NA")&&!vo.getFinancerName().equalsIgnoreCase("N/A")){
			financeDetailsVO.setFinancerName(vo.getFinancerName());
		}
		if(StringUtils.isNotEmpty(vo.getFinancerAdd())&&!vo.getFinancerAdd().equalsIgnoreCase("NA")&&!vo.getFinancerAdd().equalsIgnoreCase("N/A")){
			financeDetailsVO.setCity(vo.getFinancerAdd());
			financeDetailsVO.setOtherStateMandal(vo.getFinancerAdd());
		}
		registrationDetails.setIsFinancier(true);
		return financeDetailsVO;
	}
	
	private RegistrationDetailsVO setRegistrationDetails(VahanVehicleDetailsVO vo,String prNo){
		RegistrationDetailsVO registrationDetailsVO = new RegistrationDetailsVO();
		registrationDetailsVO.setPrNo(prNo);
		if(StringUtils.isNotEmpty(vo.getRegistaringAuthorityName())&&!vo.getRegistaringAuthorityName().equalsIgnoreCase("NA")&&!vo.getRegistaringAuthorityName().equalsIgnoreCase("N/A")){
			registrationDetailsVO.setPrIssuingAuthority(vo.getRegistaringAuthorityName());
		}
		registrationDetailsVO.setVahanDetails(setVehicleDetails(vo));
		registrationDetailsVO.setInvoiceDetails(setInvoiceDetails(vo));
		registrationDetailsVO.setApplicantDetails(setApplicateDetails(vo));
		registrationDetailsVO.setRegistrationValidity(setRegValidity(vo,registrationDetailsVO));
		if(checkVadationForInsurance(vo)){
		registrationDetailsVO.setInsuranceDetails(setInsuranceDetails(vo));
		}
		if (StringUtils.isNotEmpty(vo.getFinancerName())) {
			registrationDetailsVO.setFinanceDetails(setFinancierDetails(vo, registrationDetailsVO));
		}
		return registrationDetailsVO;
	}
	
	private InvoiceDetailsVO setInvoiceDetails(VahanVehicleDetailsVO vo){
		InvoiceDetailsVO invoiceDetailsVO = new InvoiceDetailsVO();
		if(StringUtils.isNotEmpty(vo.getInvoiceDate())&&!vo.getInvoiceDate().equalsIgnoreCase("NA")&&!vo.getInvoiceDate().equalsIgnoreCase("N/A")){
			invoiceDetailsVO.setInvoiceDateForOther(DateConverters.convertStirngTOlocalDate(vo.getInvoiceDate()));
		} else if (StringUtils.isNotEmpty(vo.getVehicleRegDate())&&!vo.getVehicleRegDate().equalsIgnoreCase("NA")&&!vo.getVehicleRegDate().equalsIgnoreCase("N/A")){
			invoiceDetailsVO.setInvoiceDateForOther(DateConverters.convertStirngTODate(vo.getVehicleRegDate()));
		}
		if(StringUtils.isNotEmpty(vo.getInvoiceNumber())&&!vo.getInvoiceNumber().equalsIgnoreCase("NA")&&!vo.getInvoiceNumber().equalsIgnoreCase("N/A")){
			invoiceDetailsVO.setInvoiceNo(vo.getInvoiceNumber());
		}
		if(vo.getSaleAmout()!=null&&!String.valueOf(vo.getSaleAmout()).equalsIgnoreCase("NA")&&!String.valueOf(vo.getSaleAmout()).equalsIgnoreCase("N/A")&&!vo.getSaleAmout().equals(0)){
			invoiceDetailsVO.setInvoiceValue(vo.getSaleAmout());
		}
		if(StringUtils.isNotEmpty(vo.getVehicleRegDate())&&!vo.getVehicleRegDate().equalsIgnoreCase("NA")&&!vo.getVehicleRegDate().equalsIgnoreCase("N/A")){
			invoiceDetailsVO.setPurchaseDateForOther(DateConverters.convertStirngTODate(vo.getVehicleRegDate()));
		}
		return invoiceDetailsVO;
	} 
	
	private RegistrationValidityVO setRegValidity(VahanVehicleDetailsVO vo,RegistrationDetailsVO registrationDetailsVO){
		
		RegistrationValidityVO registrationValidityVO = new RegistrationValidityVO();
		if(StringUtils.isNotEmpty(vo.getVehicleRegDate())&&!vo.getVehicleRegDate().equalsIgnoreCase("NA")&&!vo.getVehicleRegDate().equalsIgnoreCase("N/A")){
		registrationValidityVO.setPrGeneratedDate(DateConverters.convertStirngTODate(vo.getVehicleRegDate()));
		registrationDetailsVO.setPrIssueDate(DateConverters.convertStirngTODate(vo.getVehicleRegDate()));
		}
		if(StringUtils.isNotEmpty(vo.getValidityOfFitnessCeritificate())&&!vo.getValidityOfFitnessCeritificate().equalsIgnoreCase("NA")&&!vo.getValidityOfFitnessCeritificate().equalsIgnoreCase("N/A")){
			registrationDetailsVO.setPrValidUpto(DateConverters.convertStirngTODate(vo.getValidityOfFitnessCeritificate()));
			}
		return registrationValidityVO;
	}
	private Optional<OwnershipVO> setOwnership(VahanVehicleDetailsVO vo){
		if(StringUtils.isNotEmpty(vo.getOwnerCd())&&!vo.getOwnerCd().equalsIgnoreCase("NA")&&!vo.getOwnerCd().equalsIgnoreCase("N/A")){
		Optional<OwnershipDTO> ownershipOpt = ownershipDAO.findByNicCodes(vo.getOwnerCd()); 
		if(ownershipOpt.isPresent()){
			return Optional.of(ownershipMapper.convertRequired(ownershipOpt.get()));
		}
		}
		return Optional.empty();
	} 
	
	private String setNocStateName(String stateTo){
		Optional<StateDTO> stateOpt = stateDAO.findByNicStateCode(stateTo);
		if(stateOpt.isPresent()&&StringUtils.isNotEmpty(stateOpt.get().getStateName())){
			return stateOpt.get().getStateName();
		}
		return StringUtils.EMPTY;
	}
	
	@Override
	public Optional<StateVO> setStateDetails(String stateCode){
		Optional<StateDTO> stateOpt = stateDAO.findByNicStateCode(stateCode);
		if(stateOpt.isPresent()){
			return stateMapper.convertEntity(stateOpt);
		}
		return Optional.empty();
	}
	
	private Optional<CountryVO> setCountryCode(){
		Optional<CountryDTO> countryOpt = countryDAO.findByCountryCode("IND");
		if(countryOpt.isPresent()){
			return Optional.of(countryMapper.convertRequired(countryOpt.get()));
		}
		return Optional.empty();
	}
	private Optional<PUCDetailsVO> setPucDetails(VahanVehicleDetailsVO vo){
		PUCDetailsVO pUCDetailsVO = new PUCDetailsVO();
		if(StringUtils.isNotEmpty(vo.getValidityOfPollutionCertificate())){
			pUCDetailsVO.setValidTo(dateCovertion(vo.getValidityOfPollutionCertificate()));
		}
		return Optional.empty();
	}
	
	
	@Override
	public List<ClassOfVehiclesVO> setCovs(String covCode,RegistrationDetailsVO registrationDetailsVO){
		List<MasterCovDTO> covList = masterCovDAO.findByNicCovCode(vcrVahanVehicleService.convertStringToInteger(covCode));
		if(CollectionUtils.isNotEmpty(covList)){
			if(covList.stream().allMatch(t -> t.getCategory().equals(CovCategory.T.getCode()))){
				registrationDetailsVO.setVehicleType(CovCategory.T.getCode());
			}else if (covList.stream().allMatch(n -> n.getCategory().equals(CovCategory.N.getCode()))){
				registrationDetailsVO.setVehicleType(CovCategory.N.getCode());
			}else {
				registrationDetailsVO.setVehicleType(StringUtils.EMPTY);
			}
			return classOfVehiclesMapper.convertOtherStateVOList(covList);
		}
		return Collections.emptyList();
	}
	
	private MakersVO setMakersName(String makerName){
		MakersVO makersVO = new MakersVO();
		Optional<MakersDTO> makserDtoOpt = makersDAO.findByMakername(makerName);
		if(makserDtoOpt.isPresent()){
			return makersMapper.convertEntity(makserDtoOpt.get());
		}else{
		makersVO.setMakername(makerName);
		makersVO.setMmId(0);
		}
		return makersVO;
	}
	
	private MasterVariantVO setmakerClass(VahanVehicleDetailsVO vo){
		MasterVariantVO masterVariantVO = new MasterVariantVO();
		if(vo.getGvw()!=null&&!vo.getGvw().equalsIgnoreCase("NA")&&!vo.getGvw().equalsIgnoreCase("N/A")){
			masterVariantVO.setRlw(vcrVahanVehicleService.convertStringToInteger(vo.getGvw()));
		}
		if (vo.getUlw() != null&&!String.valueOf(vo.getUlw()).equalsIgnoreCase("NA")&&!String.valueOf(vo.getUlw()).equalsIgnoreCase("N/A")&&!vo.getUlw().equals(0)) {
			masterVariantVO.setUlw(vo.getUlw());
		} else if (vo.getuLW() != null&&!String.valueOf(vo.getuLW()).equalsIgnoreCase("NA")&&!String.valueOf(vo.getuLW()).equalsIgnoreCase("N/A")&&!vo.getuLW().equals(0)) {
			masterVariantVO.setUlw(vo.getuLW());
		}
		if(vo.getNoOfSeat()!=null&&!String.valueOf(vo.getNoOfSeat()).equalsIgnoreCase("NA")&&!String.valueOf(vo.getNoOfSeat()).equalsIgnoreCase("N/A")&&!vo.getNoOfSeat().equals(0)){
			masterVariantVO.setSeatCapacity(vo.getNoOfSeat());
		}
		if(vo.getCubicCap()!=null&&!String.valueOf(vo.getCubicCap()).equalsIgnoreCase("NA")&&!String.valueOf(vo.getCubicCap()).equalsIgnoreCase("N/A")){
			masterVariantVO.setCc(vo.getCubicCap());
		}
		if(StringUtils.isNotEmpty(vo.getFuelDesc())&&!vo.getFuelDesc().equalsIgnoreCase("NA")&&!vo.getFuelDesc().equalsIgnoreCase("N/A")){
			masterVariantVO.setFuel(vo.getFuelDesc());
		}
		if(StringUtils.isNotEmpty(vo.getVehicleModel())&&!vo.getVehicleModel().equalsIgnoreCase("NA")&&!vo.getVehicleModel().equalsIgnoreCase("N/A")){
			masterVariantVO.setModelDesc(vo.getVehicleModel());
		}
		if(vo.getWheelBase()!=null&&!String.valueOf(vo.getWheelBase()).equalsIgnoreCase("NA")&&!String.valueOf(vo.getWheelBase()).equalsIgnoreCase("N/A")&&!vo.getWheelBase().equals(0)){
			masterVariantVO.setWheelBase(vo.getWheelBase().toString());
		}
		return masterVariantVO;
	}
	
	OtherStateCovVO setCovMakers(VahanVehicleDetailsVO vo, RegistrationDetailsVO registrationDetailsVO) {
		OtherStateCovVO otherStateCovVO = new OtherStateCovVO();
		Optional<OwnershipVO> ownerOpt = setOwnership(vo);
		if(ownerOpt.isPresent()){
		otherStateCovVO.setOwnerType(ownerOpt.get());
		}
		if (StringUtils.isNotEmpty(vo.getVehicleClass()) && !vo.getVehicleClass().equalsIgnoreCase("NA")&& !vo.getVehicleClass().equalsIgnoreCase("N/A")) {
			otherStateCovVO.setClassOfVehicle(setCovs(validationForOSOBT(vo.getVehicleClass()), registrationDetailsVO));
		}
		if (StringUtils.isNotEmpty(vo.getMakerName()) && !vo.getMakerName().equalsIgnoreCase("NA")&&!vo.getMakerName().equalsIgnoreCase("N/A")) {
			otherStateCovVO.setOperator(setMakersName(vo.getMakerName()));
		}
		otherStateCovVO.setMakerClass(setmakerClass(vo));
		if(checkVadationForInsurance(vo)){
		otherStateCovVO.setPolicyType(setInsurance(registrationDetailsVO.getInsuranceDetails(),vo.getInsType()));
		Optional<InsuranceCompanyVO>  InsuranceCompanyOpt = Optional.empty(); 
		InsuranceCompanyOpt = setInsuranceCompanyName(vo.getVehicleInsuranceCompanyName());
		if(InsuranceCompanyOpt.isPresent()){
			otherStateCovVO.setCompany(InsuranceCompanyOpt.get());
		}
		}
		if (StringUtils.isNotEmpty(vo.getStateTo())&&!vo.getStateTo().equalsIgnoreCase("NA")&&!vo.getStateTo().equalsIgnoreCase("N/A")) {
			Optional<StateVO> stateOpt = setStateDetails(vo.getStateTo());
			if (stateOpt.isPresent()) {
				otherStateCovVO.setNocState(stateOpt.get());
			}
		}
		return otherStateCovVO;
	}
	
	OtherStateRegVO setRegDetails(VahanVehicleDetailsVO vo,String prNo){
		OtherStateRegVO otherStateRegVO = new OtherStateRegVO();
		otherStateRegVO.setPrNo(prNo);
		Optional<PUCDetailsVO> pucDetailsVOOpt = Optional.empty();
		otherStateRegVO.setRegistrationDetails(setRegistrationDetails(vo, prNo));
		otherStateRegVO.setnOCDetails(setNocDetails(vo));
		pucDetailsVOOpt = setPucDetails(vo);
		if(pucDetailsVOOpt.isPresent()){
			otherStateRegVO.setPucDetails(setPucDetails(vo).get());
		}
		return otherStateRegVO;
	}
	
	InsuranceTypeVO setInsurance(InsuranceDetailsVO insuranceDetailsVO,String InsPolicyType){
		InsuranceTypeVO insuranceTypeVO = null;
		if(StringUtils.isNotEmpty(InsPolicyType)){
		Optional<InsuranceTypeVO>  insuranceTypeOpt = Optional.empty(); 
		insuranceTypeOpt = setInsPolicyType(InsPolicyType);
		if(insuranceTypeOpt.isPresent()){
			insuranceTypeVO = insuranceTypeOpt.get();
			if(insuranceDetailsVO.getTenure()!=null){
				insuranceTypeVO.setTenure(Arrays.asList(insuranceDetailsVO.getTenure()));
			}
		}
		}
		return insuranceTypeVO;
	}
	Optional<InsuranceCompanyVO> setInsuranceCompanyName(String insCompanyname){
		if(StringUtils.isNotEmpty(insCompanyname)){
			Optional<InsuranceCompanyDTO>  insuranceCompanyOpt =insuranceCompanyDAO.findByInsCompidDescription(insCompanyname);
			if(insuranceCompanyOpt.isPresent()){
				return Optional.of(insuranceCompanyMapper.convertEntity(insuranceCompanyOpt.get()));
			}else{
				InsuranceCompanyVO insCompanynamevo = new InsuranceCompanyVO();
				insCompanynamevo.setInsCompidDescription(insCompanyname);
				insCompanynamevo.setInsCompId("0");
				return Optional.of(insCompanynamevo);
			}
		}
		return Optional.empty();
		
	}
	@Override
	public void validationForVahanServices(RegServiceVO regServiceVO) {
		List<String> errors = new ArrayList<>();
		Optional<OtherStateVahanResponseDTO> dtoOpt = otherStateVahanResponseDAO
				.findByOtherStateVahanRegDtoPrNoOrderByLUpdateDesc(regServiceVO.getPrNo());
		if (dtoOpt.isPresent()) {
			RegistrationDetailsDTO regDto = dtoOpt.get().getOtherStateVahanRegDto().getRegistrationDetails();
			if (regDto != null) {
				validationForRegDetails(regDto, regServiceVO.getRegistrationDetails(), errors);
			} else {
				throw new BadRequestException("Vahan Details Not Found");
			}

			if (dtoOpt.get().getOtherStateVahanRegDto().getnOCDetails() != null
					&& regServiceVO.getnOCDetails() != null) {
				validationForNocDetails(dtoOpt.get().getOtherStateVahanRegDto().getnOCDetails(),
						regServiceVO.getnOCDetails(), errors);
			}
			validationForCOV(dtoOpt.get().getOtherStateVahanCovMakerDto().getClassOfVehicle(),
					regServiceVO.getRegistrationDetails(), errors);
		} else {
			throw new BadRequestException("Vahan Details Not Found");
		}
		if(CollectionUtils.isNotEmpty(errors)){
			throw new BadRequestException(errors.toString());
		}
	}

	private void validationForRegDetails(RegistrationDetailsDTO regDto, RegistrationDetailsVO regVO,
			List<String> errors) {
		if (regDto.getApplicantDetails() != null && regVO.getApplicantDetails() != null) {
			validationForApplicateDetails(regDto.getApplicantDetails(), regVO.getApplicantDetails(), errors);
		}
		if (regDto.getIsFinancier() && regDto.getFinanceDetails() != null && regVO.getIsFinancier()
				&& regVO.getFinanceDetails() != null) {
			validationForFinanceDetails(regDto.getFinanceDetails(), regVO.getFinanceDetails(), errors);
		}
		if (regDto.getInvoiceDetails() != null && regVO.getInvoiceDetails() != null) {
			validationForInvoiceDetails(regDto.getInvoiceDetails(), regVO.getInvoiceDetails(), errors);
		}
		if (regDto.getVahanDetails() != null && regVO.getVahanDetails() != null) {
			validationForVahanDetails(regDto.getVahanDetails(), regVO.getVahanDetails(), errors);
		}
		if (regDto.getPrIssuingAuthority() != null && regVO.getPrIssuingAuthority() != null
				&& !regDto.getPrIssuingAuthority().equals(regVO.getPrIssuingAuthority())) {
			errors.add("registaringAuthority Name Missmatched");
		}
		if (regDto.getPrIssueDate() != null && regVO.getPrIssueDate() != null
				&& !regDto.getPrIssueDate().equals(regVO.getPrIssueDate())) {
			errors.add("registration Date Missmatched");
		}
		if (regDto.getPrValidUpto() != null && regVO.getPrValidUpto() != null
				&& !regDto.getPrValidUpto().equals(regVO.getPrValidUpto())) {
			errors.add("registration ValidUpto Missmatched");
		}
	}

	private void validationForApplicateDetails(ApplicantDetailsDTO appDto, ApplicantDetailsVO appVO,
			List<String> errors) {
		if (StringUtils.isNotEmpty(appDto.getDisplayName()) && StringUtils.isNotEmpty(appVO.getDisplayName())
				&& !appDto.getDisplayName().equalsIgnoreCase(appVO.getDisplayName())) {
			errors.add("Dispaly Name Missmatched");
		}
		if (StringUtils.isNotEmpty(appDto.getFirstName())
				&& !appDto.getFirstName().equalsIgnoreCase(appVO.getFirstName())) {
			errors.add("Name Missmatched");
		}
		if (StringUtils.isNotEmpty(appDto.getFatherName())
				&& !appDto.getFatherName().equalsIgnoreCase(appVO.getFatherName())) {
			errors.add("Father Name Missmatched");
		}
		if (appVO.getPermanantAddress() != null && appDto.getOtherStateAddress() != null
				&& StringUtils.isNotEmpty(appDto.getOtherStateAddress().getOtherDistrict())
				&& !appVO.getPermanantAddress().getOtherDistrict()
				.equalsIgnoreCase(appDto.getOtherStateAddress().getOtherDistrict())) {
			errors.add("Permanant District Missmatched");
		}
		if (appVO.getPermanantAddress() != null
				&& StringUtils.isNotEmpty(appVO.getPermanantAddress().getState().getStateName())
				&& appDto.getOtherStateAddress() != null && appDto.getOtherStateAddress().getState() != null
				&& StringUtils.isNotEmpty(appDto.getOtherStateAddress().getState().getStateName())
				&& !appDto.getOtherStateAddress().getState().getStateName()
				.equalsIgnoreCase(appVO.getPermanantAddress().getState().getStateName())) {
			errors.add("Permanant State Missmatched");
		}
		if (appDto.getOtherStateAddress() != null && StringUtils.isNotEmpty(appDto.getOtherStateAddress().getDoorNo())
				&& appVO.getPermanantAddress() != null
				&& StringUtils.isNotEmpty(appVO.getPermanantAddress().getDoorNo()) && !appVO.getPermanantAddress()
				.getDoorNo().equalsIgnoreCase(appDto.getOtherStateAddress().getDoorNo())) {
			errors.add("Permanant Address Missmatched");
		}
	}

	private void validationForFinanceDetails(FinanceDetailsDTO financeDTO, FinanceDetailsVO financeVO,
			List<String> errors) {
		if (!financeDTO.getFinancerName().equals(financeVO.getFinancerName())) {
			errors.add("Financer Name Missmatched");
		}
		if (financeDTO.getDistrict() != null && financeVO.getDistrict() != null
				&& StringUtils.isNotEmpty(financeDTO.getDistrict().getDistrictName())
				&& StringUtils.isNotEmpty(financeVO.getDistrict().getDistrictName()) && !financeDTO.getDistrict()
				.getDistrictName().equalsIgnoreCase(financeVO.getDistrict().getDistrictName())) {
			errors.add("Financer District Name Missmatched");
		}
		if (financeDTO.getState() != null && financeVO.getState() != null
				&& StringUtils.isNotEmpty(financeDTO.getState().getStateName())
				&& StringUtils.isNoneEmpty(financeVO.getState().getStateName())
				&& !financeDTO.getState().getStateName().equalsIgnoreCase(financeVO.getState().getStateName())) {
			errors.add("Financer State Name Missmatched");
		}
	}

	private void validationForNocDetails(NOCDetailsDTO nocDTO, NOCDetailsVO nocVO, List<String> errors) {
		if (nocDTO.getState() != null && nocVO.getState() != null
				&& !nocDTO.getState().equalsIgnoreCase(nocVO.getState())) {
			errors.add("Noc State Name Missmatched");
		}
		if (nocDTO.getRtaOffice() != null && nocVO.getRtaOffice() != null
				&& !nocDTO.getRtaOffice().equalsIgnoreCase(nocVO.getRtaOffice())) {
			errors.add("Noc RTA Office Name Missmatched");
		}
		if (nocDTO.getnOCIssuedRtaOffice() != null && nocVO.getnOCIssuedRtaOffice() != null
				&& !nocDTO.getnOCIssuedRtaOffice().equalsIgnoreCase(nocVO.getnOCIssuedRtaOffice())) {
			errors.add("Noc Issued Rta Office Name Missmatched");
		}
		if (nocDTO.getIssueDate() != null && nocVO.getIssueDate() != null
				&& !nocDTO.getIssueDate().equals(nocVO.getIssueDate())) {
			errors.add("Noc Issued Date Missmatched");
		}
	}

	private void validationForInvoiceDetails(InvoiceDetailsDTO invoiceDTO, InvoiceDetailsVO invoiceVO,
			List<String> errors) {
		if (invoiceDTO.getInvoiceNo() != null && invoiceVO.getInvoiceNo() != null
				&& !invoiceDTO.getInvoiceNo().equalsIgnoreCase(invoiceVO.getInvoiceNo())) {
			errors.add("Invoice Number Missmatched");
		}
		// as per jagan input commit the validation 
		/*if (invoiceDTO.getInvoiceValue() != null && invoiceVO.getInvoiceValue() != null
				&& !invoiceDTO.getInvoiceValue().equals(invoiceVO.getInvoiceValue())) {
			errors.add("Invoice Amount Missmatched");
		}*/
		if (invoiceDTO.getPurchaseDateForOther() != null && invoiceVO.getPurchaseDateForOther() != null
				&& !invoiceDTO.getPurchaseDateForOther().equals(invoiceVO.getPurchaseDateForOther())) {
			errors.add("Purchase Date Missmatched");
		}
		if (invoiceDTO.getInvoiceDateForOther() != null && invoiceVO.getInvoiceDateForOther() != null
				&& !invoiceDTO.getInvoiceDateForOther().equals(invoiceVO.getInvoiceDateForOther())) {
			errors.add("Invoice Date Missmatched");
		}
	}

	private void validationForVahanDetails(VahanDetailsDTO vahanDTO, VahanDetailsVO vahanVO, List<String> errors) {
		if (StringUtils.isNotEmpty(vahanDTO.getChassisNumber()) && StringUtils.isNotEmpty(vahanVO.getChassisNumber())
				&& !vahanDTO.getChassisNumber().equalsIgnoreCase(vahanVO.getChassisNumber())) {
			errors.add("Chassis Number Missmatched");
		}
		if (StringUtils.isNotEmpty(vahanDTO.getEngineNumber()) && StringUtils.isNotEmpty(vahanVO.getEngineNumber())
				&& !vahanDTO.getEngineNumber().equalsIgnoreCase(vahanVO.getEngineNumber())) {
			errors.add("Engine Number Missmatched");
		}
		if (StringUtils.isNotEmpty(vahanDTO.getColor()) && StringUtils.isNotEmpty(vahanVO.getColor())
				&& !vahanDTO.getColor().equalsIgnoreCase(vahanVO.getColor())) {
			errors.add("Color Missmatched");
		}
		if (StringUtils.isNotEmpty(vahanDTO.getCubicCapacity()) && StringUtils.isNotEmpty(vahanVO.getCubicCapacity())
				&& !vahanDTO.getCubicCapacity().equalsIgnoreCase(vahanVO.getCubicCapacity())) {
			errors.add("Cubic Capacity Missmatched");
		}
		if (StringUtils.isNotEmpty(vahanDTO.getBodyTypeDesc()) && StringUtils.isNotEmpty(vahanVO.getBodyTypeDesc())
				&& !vahanDTO.getBodyTypeDesc().equalsIgnoreCase(vahanVO.getBodyTypeDesc())) {
			errors.add("BodyTyp Missmatched");
		}
		if (StringUtils.isNotEmpty(vahanDTO.getMakersModel()) && StringUtils.isNotEmpty(vahanVO.getMakersModel())
				&& !vahanDTO.getMakersModel().equalsIgnoreCase(vahanVO.getMakersModel())) {
			errors.add("Makers Model Missmatched");
		}
		if (StringUtils.isNotEmpty(vahanDTO.getSeatingCapacity())
				&& StringUtils.isNotEmpty(vahanVO.getSeatingCapacity())
				&& !vahanDTO.getSeatingCapacity().equalsIgnoreCase(vahanVO.getSeatingCapacity())) {
			errors.add("Seating Capacity Missmatched");
		}
		if (StringUtils.isNotEmpty(vahanDTO.getFuelDesc()) && StringUtils.isNotEmpty(vahanVO.getFuelDesc())
				&& !vahanDTO.getFuelDesc().equalsIgnoreCase(vahanVO.getFuelDesc())) {
			errors.add("Fuel Missmatched");
		}
		if (vahanDTO.getGvw() != null && vahanVO.getGvw() != null && !vahanDTO.getGvw().equals(vahanVO.getGvw())) {
			errors.add("Gvw Missmatched");
		}
		if (vahanDTO.getRearAxleWeight() != null && vahanVO.getRearAxleWeight() != null
				&& !vahanDTO.getRearAxleWeight().equals(vahanVO.getRearAxleWeight())) {
			errors.add("Gvw Missmatched");
		}
		if (vahanDTO.getUnladenWeight() != null && vahanVO.getUnladenWeight() != null
				&& !vahanDTO.getUnladenWeight().equals(vahanVO.getUnladenWeight())) {
			errors.add("ULW Missmatched");
		}
		if (vahanDTO.getWheelbase() != null && vahanVO.getWheelbase() != null
				&& !vahanDTO.getWheelbase().equals(vahanVO.getWheelbase())) {
			errors.add("Wheel base Missmatched");
		}
		if (vahanDTO.getManufacturedMonthYear() != null && vahanVO.getManufacturedMonthYear() != null
				&& !vahanDTO.getManufacturedMonthYear().equals(vahanVO.getManufacturedMonthYear())) {
			errors.add("Manufactured MonthYear Missmatched");
		}
		if (StringUtils.isNotEmpty(vahanDTO.getMakersDesc()) && StringUtils.isNotEmpty(vahanVO.getMakersDesc())
				&& !vahanDTO.getMakersDesc().equalsIgnoreCase(vahanVO.getMakersDesc())) {
			errors.add("Makers Name Missmatched");
		}
	}

	private void validationForCOV(List<MasterCovDTO> covList, RegistrationDetailsVO regVO, List<String> errors) {
		if (CollectionUtils.isNotEmpty(covList) && regVO.getClassOfVehicle() != null) {
			List<String> covs = covList.stream().map(cov -> cov.getCovcode()).collect(Collectors.toList());
			if (!covs.contains(regVO.getClassOfVehicle())) {
				errors.add("ClassOfVehicle Missmatched");
			}
		}
	}
	private void setRepresentativeName(OwnershipVO ownershipVO, ApplicantDetailsVO applicantDetailsVO){
		List<String> ownerShipList = new ArrayList<>();
		ownerShipList.add(OwnerTypeEnum.Company.name());
		ownerShipList.add(OwnerTypeEnum.Government.name());
		ownerShipList.add(OwnerTypeEnum.Stu.name());
		ownerShipList.add(OwnerTypeEnum.Organization.name());
		ownerShipList.add(OwnerTypeEnum.POLICE.name());
		if(ownerShipList.contains(ownershipVO.getDescription())&&applicantDetailsVO.getFirstName()!=null){
			applicantDetailsVO.setEntityName(applicantDetailsVO.getFirstName());
		}
	}

	private boolean checkVadationForInsurance(VahanVehicleDetailsVO vo) {
		if (StringUtils.isNotEmpty(vo.getVehicleInsuranceCompanyName())
				&& !vo.getVehicleInsuranceCompanyName().equalsIgnoreCase("NA")&&!vo.getVehicleInsuranceCompanyName().equalsIgnoreCase("N/A")) {
			if (StringUtils.isNotEmpty(vo.getOwnerCd()) && !vo.getOwnerCd().equalsIgnoreCase("NA")&&!vo.getOwnerCd().equalsIgnoreCase("N/A")) {
				Optional<OwnershipDTO> ownershipOpt = ownershipDAO.findByNicCodes(vo.getOwnerCd());
				if (ownershipOpt.isPresent() && StringUtils.isNotEmpty(ownershipOpt.get().getDescription()) && Arrays
						.asList(OwnerTypeEnum.Government.name(), OwnerTypeEnum.Stu.name(), OwnerTypeEnum.POLICE.name())
						.contains(ownershipOpt.get().getDescription())) {
					return false;
				}
			}
		}
		return true;
	}
	//As per srinivas sir input nic code 73,69,75
	private String validationForOSOBT(String covCode){
		if(covCode.equals("73")||covCode.equals("69")||covCode.equals("75")){
			return "86";
		}
		return covCode;
		
	}

	LocalDate dateCovertion(String date) {
		LocalDate localdate = null;
		try {
			localdate = DateConverters.convertStirngTOlocalDate(date);
		} catch (Exception e) {
			localdate = DateConverters.convertStirngTODate(date);
		}
		return localdate;
	}
}
