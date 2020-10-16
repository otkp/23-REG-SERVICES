package org.epragati.master.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.aadhaar.AadhaarDetailsRequestVO;
import org.epragati.aadhaar.AadharDetailsResponseVO;
import org.epragati.aadhaarAPI.AadhaarSourceDTO;
import org.epragati.cache.CacheData;
import org.epragati.common.dao.PropertiesDAO;
import org.epragati.common.dto.PropertiesDTO;
import org.epragati.constants.MessageKeys;
import org.epragati.constants.OwnerTypeEnum;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.BloodGroupDAO;
import org.epragati.master.dao.BodyTypeDAO;
import org.epragati.master.dao.CategoryDAO;
import org.epragati.master.dao.CountryDAO;
import org.epragati.master.dao.DealerCovDAO;
import org.epragati.master.dao.DealerMakerDAO;
import org.epragati.master.dao.GateWayDAO;
import org.epragati.master.dao.InsuranceCompanyDAO;
import org.epragati.master.dao.InsuranceTypeDAO;
import org.epragati.master.dao.MakersDAO;
import org.epragati.master.dao.MasterCovDAO;
import org.epragati.master.dao.MasterFinanceTypeDAO;
import org.epragati.master.dao.MasterPayperiodDAO;
import org.epragati.master.dao.MasterVariantDAO;
import org.epragati.master.dao.OwnershipDAO;
import org.epragati.master.dao.QualificationDAO;
import org.epragati.master.dao.TaxTypeDAO;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dto.BloodGroupDTO;
import org.epragati.master.dto.BodyTypeDTO;
import org.epragati.master.dto.CategoryDTO;
import org.epragati.master.dto.CountryDTO;
import org.epragati.master.dto.DealerCovDTO;
import org.epragati.master.dto.DealerMakerDTO;
import org.epragati.master.dto.GateWayDTO;
import org.epragati.master.dto.InsuranceCompanyDTO;
import org.epragati.master.dto.InsuranceTypeDTO;
import org.epragati.master.dto.MakersDTO;
import org.epragati.master.dto.MasterCovDTO;
import org.epragati.master.dto.MasterFinanceTypeDTO;
import org.epragati.master.dto.MasterPayperiodDTO;
import org.epragati.master.dto.MasterVariantDTO;
import org.epragati.master.dto.OwnershipDTO;
import org.epragati.master.dto.QualificationDTO;
import org.epragati.master.dto.TaxTypeDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.mappers.BloodGroupMapper;
import org.epragati.master.mappers.BodyTypeMapper;
import org.epragati.master.mappers.CategoryMapper;
import org.epragati.master.mappers.CountryMapper;
import org.epragati.master.mappers.GateWayMapper;
import org.epragati.master.mappers.InsuranceCompanyMapper;
import org.epragati.master.mappers.InsuranceTypeMapper;
import org.epragati.master.mappers.MakersMapper;
import org.epragati.master.mappers.MasterCovMapper;
import org.epragati.master.mappers.MasterFinanceTypeMapper;
import org.epragati.master.mappers.MasterVaraintMapper;
import org.epragati.master.mappers.OwnershipMapper;
import org.epragati.master.mappers.PropertiesMapper;
import org.epragati.master.mappers.QualificationMapper;
import org.epragati.master.mappers.TaxTypeMapper;
import org.epragati.master.service.InfoService;
import org.epragati.master.vo.BloodGroupVO;
import org.epragati.master.vo.BodyTypeVO;
import org.epragati.master.vo.CategoryVO;
import org.epragati.master.vo.InsuranceCompanyVO;
import org.epragati.master.vo.InsuranceTypeVO;
import org.epragati.master.vo.MakersDetails;
import org.epragati.master.vo.MakersInfoVO;
import org.epragati.master.vo.MakersVO;
import org.epragati.master.vo.MasterCovVO;
import org.epragati.master.vo.MasterFinanceTypeVO;
import org.epragati.master.vo.MasterInfoVO;
import org.epragati.master.vo.MasterVariantVO;
import org.epragati.master.vo.ModelDesVO;
import org.epragati.master.vo.OwnershipVO;
import org.epragati.master.vo.QualificationVO;
import org.epragati.master.vo.TaxTypeVO;
import org.epragati.master.vo.TrailerCOVInfoVO;
import org.epragati.restGateway.RestGateWayService;
import org.epragati.tax.vo.TaxTypeEnum;
import org.epragati.util.CacheEnum;
import org.epragati.util.ResponseStatusEnum;
import org.epragati.util.RoleEnum;
import org.epragati.util.payment.AxleTypeEnum;
import org.epragati.util.payment.ClassOfVehicleEnum;
import org.epragati.util.payment.GatewayTypeEnum;
import org.epragati.util.payment.ServiceEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InfoServiceImpl implements InfoService {

	private static final Logger logger = LoggerFactory.getLogger(InfoServiceImpl.class);

	@Autowired
	private OwnershipDAO ownershipDAO;

	@Autowired
	private QualificationDAO qualificationDAO;

	@Autowired
	private BloodGroupDAO bloodGroupDAO;

	@Autowired
	private BodyTypeDAO bodyTypeDAO;

	@Autowired
	private DealerCovDAO dealerCovDAO;

	@Autowired
	private TaxTypeDAO taxTypeDAO;

	@Autowired
	private InsuranceCompanyDAO insuranceCompanyDAO;

	@Autowired
	private DealerMakerDAO dealerMakerDAO;

	@Autowired
	private UserDAO masterUsersDAO;
	@Autowired
	private MasterPayperiodDAO MpayperiodDAO;

	/*
	 * @Autowired private MasterUsersDAO masterUsersDAO;
	 */

	@Autowired
	private MakersDAO makersDAO;

	@Autowired
	private GateWayDAO gateWayDAO;

	@Autowired
	private CategoryDAO categoryDAO;

	@Autowired
	private CountryDAO masterCountryDAO;

	@Autowired
	private MasterVariantDAO masterVariantDAO;

	@Autowired
	private MasterCovDAO masterCovDAO;

	@Autowired
	private OwnershipMapper ownershipMapper;

	@Autowired
	private QualificationMapper qualificationMapper;

	@Autowired
	private BloodGroupMapper bloodGroupMapper;

	/*
	 * @Autowired private DealerCovMapper dealerCovMapper;
	 */

	@Autowired
	private MakersMapper makersMapper;

	@Autowired
	private BodyTypeMapper bodyTypeMapper;

	@Autowired
	private TaxTypeMapper taxTypeMapper;

	@Autowired
	private InsuranceCompanyMapper insuranceCompanyMapper;

	@Autowired
	private GateWayMapper gateWayMapper;

	@Autowired
	private CategoryMapper categoryMapper;

	@Autowired
	private CountryMapper masterCountryMapper;

	@Autowired
	private MasterVaraintMapper masterVaraintMapper;

	@Autowired
	private InsuranceTypeDAO insuranceTypeDAO;

	@Autowired
	private InsuranceTypeMapper insuranceTypeMapper;

	@Autowired
	private MasterCovMapper masterCovMapper;

	/*
	 * @Autowired private DealerAndVahanMappedCovMapper
	 * dealerAndVahanMappedCovMapper;
	 * 
	 * @Autowired private DealerAndVahanMappedCovDAO dealerAndVahanMappedCovDAO;
	 */

	@Autowired
	private MasterFinanceTypeDAO masterFinanceTypeDAO;

	@Autowired
	private MasterFinanceTypeMapper masterFinanceTypeMapper;

	@Autowired
	private GateWayDAO gatewayDao;

	@Autowired
	private PropertiesDAO propertiesDAO;

	@Autowired
	private PropertiesMapper propertiesMapper;

	@Autowired
	private RestGateWayService restGateWayService;

	@Autowired
	private UserDAO userDAO;

	@SuppressWarnings("unchecked")
	@Override
	public Optional<MasterInfoVO> getMasterInfo(String dealerUserId) {

		MasterInfoVO masterInfoVO = new MasterInfoVO();

		try {
			UserDTO masterUserDTO = masterUsersDAO.findByUserIdAndStatusTrue(dealerUserId);
			String verifiedUser = masterUserDTO.getUserId();
			Integer dealerId = masterUserDTO.getRid();
			List<DealerCovDTO> dealerCovDTO = dealerCovDAO.findByRIdAndStatusTrue(dealerId);
			List<String> covCode = new ArrayList<>();
			dealerCovDTO.stream().forEach(e -> covCode.add(e.getCovId()));
			List<MasterCovDTO> masterCOVDTO = masterCovDAO.findByCovcodeInAndDealerCovTrue(covCode);
			masterInfoVO.setMasterCovVO(masterCovMapper.convertEntity(masterCOVDTO));

			if (verifiedUser.equals(dealerUserId)) {

				for (int i = 0; i <= 8; i++) {
					switch (i) {

					case 0:
						ConcurrentMap<String, Object> ownershipMap = getOwnershipDetails();
						masterInfoVO.setOwnershipVO(ownershipMapper
								.convertDTOs((List<OwnershipDTO>) ownershipMap.get(CacheEnum.OWNERSHIP.getKey())));
						break;

					case 1:
						ConcurrentMap<String, Object> qualifactionMap = getQualificationsDetails();
						masterInfoVO.setQualificationVO(qualificationMapper.convertDTOs(
								(List<QualificationDTO>) qualifactionMap.get(CacheEnum.QUALIFICATION.getKey())));
						break;
					case 2:
						ConcurrentMap<String, Object> bloodGroupMap = getBloodFroupDetails();
						masterInfoVO.setBloodGroupVO(bloodGroupMapper
								.convertDTOs((List<BloodGroupDTO>) bloodGroupMap.get(CacheEnum.BLOODGROUP.getKey())));
						break;
					case 3:
						ConcurrentMap<String, Object> insuranceMap = getInsuranceDetails();
						masterInfoVO.setInsuranceCompanyVO(insuranceCompanyMapper.convertDTOs(
								(List<InsuranceCompanyDTO>) insuranceMap.get(CacheEnum.INSURANCECOMPANY.getKey())));
						break;

					case 4:
						ConcurrentMap<String, Object> gatewayMap = getGatewayDetails();
						masterInfoVO.setGateWayVO(gateWayMapper
								.convertEntity((List<GateWayDTO>) gatewayMap.get(CacheEnum.GATEWAY.getKey())));
						break;
					case 5:
						ConcurrentMap<String, Object> categoryMap = getCategoryDetails();
						masterInfoVO.setCategoryVO(categoryMapper
								.convertEntity((List<CategoryDTO>) categoryMap.get(CacheEnum.CATEGORY.getKey())));
						break;
					case 6:
						ConcurrentMap<String, Object> countryMap = getCountryDetails();
						masterInfoVO.setCountryVO(masterCountryMapper
								.convertDTOs((List<CountryDTO>) countryMap.get(CacheEnum.COUNTRY.getKey())));
						break;

					case 7:
						ConcurrentMap<String, Object> insuranceTypeMap = getInsuranceTypeDetails();
						masterInfoVO.setInsuranceTypeVO(insuranceTypeMapper.convertEntity(
								(List<InsuranceTypeDTO>) insuranceTypeMap.get(CacheEnum.INSURANCETYPE.getKey())));
						break;
					case 8:
						ConcurrentMap<String, Object> financeMap = getFinanceDetails();
						masterInfoVO.setFinanceTypeVO(masterFinanceTypeMapper.convertEntity(
								(List<MasterFinanceTypeDTO>) financeMap.get(CacheEnum.FINANCETYPE.getKey())));
						break;

					default:
						Optional.empty();
					}

				}
			}

		} catch (Exception e) {
			logger.debug("Exception raised while fetching MasterInfo {}", e);

			logger.error("Exception raised while fetching MasterInfo {}", e.getMessage());
			return Optional.empty();

		}
		return Optional.ofNullable(masterInfoVO);
	}

	public ConcurrentMap<String, Object> getOwnershipDetails() {
		if (CacheData.getFromCache(CacheEnum.OWNERSHIP.getKey()) == null) {
			List<OwnershipDTO> ownershipDTO = ownershipDAO.findByStatusTrue();
			CacheData.storeIntoCache(CacheEnum.OWNERSHIP.getKey(), ownershipDTO);
		} else {
			CacheData.getFromCache(CacheEnum.OWNERSHIP.getKey());
		}
		return CacheData.getcacheMap();
	}

	public ConcurrentMap<String, Object> getQualificationsDetails() {
		if (CacheData.getFromCache(CacheEnum.QUALIFICATION.getKey()) == null) {
			List<QualificationDTO> qualificationDTO = qualificationDAO.findByStatusTrue();
			CacheData.storeIntoCache(CacheEnum.QUALIFICATION.getKey(), qualificationDTO);
		} else {
			CacheData.getFromCache(CacheEnum.QUALIFICATION.getKey());
		}
		return CacheData.getcacheMap();
	}

	public ConcurrentMap<String, Object> getBloodFroupDetails() {
		if (CacheData.getFromCache(CacheEnum.BLOODGROUP.getKey()) == null) {
			List<BloodGroupDTO> bloodGroupDTO = bloodGroupDAO.findByActiveTrue();
			CacheData.storeIntoCache(CacheEnum.BLOODGROUP.getKey(), bloodGroupDTO);
		} else {
			CacheData.getFromCache(CacheEnum.BLOODGROUP.getKey());
		}
		return CacheData.getcacheMap();
	}

	public ConcurrentMap<String, Object> getInsuranceDetails() {
		if (CacheData.getFromCache(CacheEnum.INSURANCECOMPANY.getKey()) == null) {
			List<InsuranceCompanyDTO> insuranceCompanyDTO = insuranceCompanyDAO.findByStatusTrue();
			CacheData.storeIntoCache(CacheEnum.INSURANCECOMPANY.getKey(), insuranceCompanyDTO);
		} else {
			CacheData.getFromCache(CacheEnum.INSURANCECOMPANY.getKey());
		}
		return CacheData.getcacheMap();
	}

	public ConcurrentMap<String, Object> getGatewayDetails() {
		if (CacheData.getFromCache(CacheEnum.GATEWAY.getKey()) == null) {
			List<GateWayDTO> gateWayDTOList = gateWayDAO.findByStatusTrue();
			CacheData.storeIntoCache(CacheEnum.GATEWAY.getKey(), gateWayDTOList);
		} else {
			CacheData.getFromCache(CacheEnum.GATEWAY.getKey());
		}
		return CacheData.getcacheMap();
	}

	public ConcurrentMap<String, Object> getCategoryDetails() {
		if (CacheData.getFromCache(CacheEnum.CATEGORY.getKey()) == null) {
			List<CategoryDTO> categoryDTOList = categoryDAO.findByStatusTrue();
			CacheData.storeIntoCache(CacheEnum.CATEGORY.getKey(), categoryDTOList);
		} else {
			CacheData.getFromCache(CacheEnum.CATEGORY.getKey());
		}
		return CacheData.getcacheMap();
	}

	public ConcurrentMap<String, Object> getCountryDetails() {
		if (CacheData.getFromCache(CacheEnum.COUNTRY.getKey()) == null) {
			List<CountryDTO> contryList = masterCountryDAO.findByCountryStatusTrue();
			CacheData.storeIntoCache(CacheEnum.COUNTRY.getKey(), contryList);
		} else {
			CacheData.getFromCache(CacheEnum.COUNTRY.getKey());
		}
		return CacheData.getcacheMap();
	}

	public ConcurrentMap<String, Object> getInsuranceTypeDetails() {
		if (CacheData.getFromCache(CacheEnum.INSURANCETYPE.getKey()) == null) {
			List<InsuranceTypeDTO> insurenceList = insuranceTypeDAO.findAll();
			CacheData.storeIntoCache(CacheEnum.INSURANCETYPE.getKey(), insurenceList);
		} else {
			CacheData.getFromCache(CacheEnum.INSURANCETYPE.getKey());
		}
		return CacheData.getcacheMap();
	}

	public ConcurrentMap<String, Object> getFinanceDetails() {
		if (CacheData.getFromCache(CacheEnum.FINANCETYPE.getKey()) == null) {
			List<MasterFinanceTypeDTO> financeTypeDTO = masterFinanceTypeDAO.findAll();
			CacheData.storeIntoCache(CacheEnum.FINANCETYPE.getKey(), financeTypeDTO);
		} else {
			CacheData.getFromCache(CacheEnum.FINANCETYPE.getKey());
		}
		return CacheData.getcacheMap();
	}

	@Override
	public List<MasterVariantVO> getModelDecs(ModelDesVO modelDesVO) {
		// TODO Auto-generated method stub
		List<MasterVariantVO> masterVariantVO = null;
		Integer mid = Integer.valueOf(modelDesVO.getMmId());
		List<MasterVariantDTO> masterVariantDTO = masterVariantDAO
				.findByMidAndVehicleClassIDAndActiveTrueAndBsAllowedTrue(mid, modelDesVO.getCovCode());
		if (CollectionUtils.isEmpty(masterVariantDTO)) {
			logger.error("Master Class Details Not Found");
			throw new BadRequestException("Master Class Details Not Found");
		}
		masterVariantVO = masterVaraintMapper.convertEntity(masterVariantDTO);
		return masterVariantVO;

	}

	@Override
	public BodyTypeVO getBodyType(String bodyTypeId) {

		Integer bodyType = Integer.valueOf(bodyTypeId);
		BodyTypeDTO bodyTypeDTO = bodyTypeDAO.findByBodyTypeIdAndStatusTrue(bodyType);
		BodyTypeVO bodyTypeVO = null;
		if (null != bodyTypeDTO)
			bodyTypeVO = bodyTypeMapper.convertRequired(bodyTypeDTO);
		return bodyTypeVO;

	}

	@Override
	public Optional<List<InsuranceTypeVO>> insuranceTypes() {

		List<InsuranceTypeDTO> insurenceList = insuranceTypeDAO.findAll();
		return Optional.of(insuranceTypeMapper.convertEntity(insurenceList));

	}

	@Override
	public Optional<List<MasterFinanceTypeVO>> financeTypes() {
		// TODO Auto-generated method stub
		List<MasterFinanceTypeDTO> financeTypeDTO = masterFinanceTypeDAO.findAll();
		return Optional.of(masterFinanceTypeMapper.convertEntity(financeTypeDTO));
	}

	@Override
	public Optional<List<QualificationVO>> qualificationTypes() {
		// TODO Auto-generated method stub
		List<QualificationDTO> qualificationDTO = qualificationDAO.findByStatusTrue();
		return Optional.of(qualificationMapper.convertDTOs(qualificationDTO));

	}

	@Override
	public Optional<List<BloodGroupVO>> bloodGroupTypes() {
		// TODO Auto-generated method stub
		List<BloodGroupDTO> bloodGroupDTO = bloodGroupDAO.findByActiveTrue();
		return Optional.of(bloodGroupMapper.convertDTOs(bloodGroupDTO));
	}

	@Override
	public Optional<List<CategoryVO>> categoryType() {
		// TODO Auto-generated method stub
		List<CategoryDTO> categoryDTOList = categoryDAO.findByStatusTrue();
		return Optional.of(categoryMapper.convertEntity(categoryDTOList));
	}

	@Override
	public Optional<List<TaxTypeVO>> taxType() {
		// TODO Auto-generated method stub
		List<TaxTypeDTO> taxTypeDTOList = taxTypeDAO.findByStatusTrue();
		return Optional.of(taxTypeMapper.convertEntity(taxTypeDTOList));
	}

	@Override
	public Optional<List<OwnershipVO>> ownerType() {
		// TODO Auto-generated method stub
		List<OwnershipDTO> ownershipDTOList = ownershipDAO.findByStatusTrue();
		return Optional.of(ownershipMapper.convertEntity(ownershipDTOList));
	}

	@Override
	public List<MasterCovVO> getTrailerCovs(String userId, String vehicleType) {

		List<String> covCode = new ArrayList<>();
		List<String> trailesCovs = new ArrayList<>();
		trailesCovs.add(ClassOfVehicleEnum.TTTT.getCovCode());
		trailesCovs.add(ClassOfVehicleEnum.TTRN.getCovCode());

		UserDTO masterUserDTO = masterUsersDAO.findByUserIdAndStatusTrue(userId);
		Integer dealerId = masterUserDTO.getRid();
		List<DealerCovDTO> dealerCovDTO = dealerCovDAO.findByCovIdInAndRIdAndStatusTrue(trailesCovs, dealerId);

		if (dealerCovDTO.isEmpty()) {
			logger.error("Dealer maker details is not found with this dealer details [{}]",
					masterUserDTO.getUserName());
			throw new BadRequestException("Dealer Not eligible for trailer Vehicle registration" + dealerId);
		}

		dealerCovDTO.stream().forEach(e -> covCode.add(e.getCovId()));

		List<MasterCovDTO> covDetails = masterCovDAO.findByCovcodeInAndDealerCovTrueAndCategory(covCode, vehicleType);
		List<MasterCovVO> covVo = masterCovMapper.convertEntity(covDetails);
		return covVo;
	}

	@Override
	public Optional<TrailerCOVInfoVO> getTrailerVariantDetails(String userId, String cov) {
		TrailerCOVInfoVO infoVo = new TrailerCOVInfoVO();
		List<Integer> mmIdList = new ArrayList<>();
		UserDTO masterUserDTO = masterUsersDAO.findByUserIdAndStatusTrue(userId);
		Integer dealerId = masterUserDTO.getRid();
		List<DealerMakerDTO> dealerMakerDTO = dealerMakerDAO.findByRIdAndStatusTrue(dealerId);
		if (dealerMakerDTO.isEmpty()) {
			logger.error("Dealer maker details is not found with this dealer details [{}]",
					masterUserDTO.getUserName());
			throw new BadRequestException("Maker details is not found with this dealer Details");
		}

		dealerMakerDTO.stream().forEach(d -> mmIdList.add(d.getMmId()));

		List<MasterVariantDTO> masterVarientDetails = masterVariantDAO.findByMidInAndVehicleClassID(mmIdList, cov);

		if (masterVarientDetails.isEmpty()) {
			logger.error("Varient details in not present with this MMid[{}], and COV [{}]", mmIdList, cov);
			throw new BadRequestException("Varient details in not present with this MMid," + mmIdList + "COV" + cov);
		}

		List<MakersDTO> makersDetails = makersDAO.findByMidInAndStatusTrue(mmIdList);

		if (makersDetails.isEmpty()) {
			logger.error("Maker details in not present with this MMid[{}]", mmIdList);
			throw new BadRequestException("Maker details in not present with this MMid" + mmIdList);
		}

		List<MasterVariantVO> varientDetailsVO = masterVaraintMapper.convertEntity(masterVarientDetails);

		List<MakersVO> makerDetailsVO = makersMapper.convertEntity(makersDetails);

		infoVo.setMasterVariantVO(varientDetailsVO);
		infoVo.setDealerMakerVO(makerDetailsVO);

		return Optional.ofNullable(infoVo);
	}

	@Override
	public GateWayDTO findByGateWayType(GatewayTypeEnum payu) {
		GateWayDTO gatewayValue = gatewayDao.findByGateWayType(payu);
		return gatewayValue;
	}

	@Override
	public Optional<List<InsuranceCompanyVO>> getAllInsuranceCompName() {
		// TODO Auto-generated method stub
		List<InsuranceCompanyDTO> insuranceCompanyDTO = insuranceCompanyDAO.findByStatusTrue();
		return Optional.of(insuranceCompanyMapper.convertDTOs(insuranceCompanyDTO));
	}

	@Override
	public List<OwnershipVO> getOwnerShipTypes() {
		// TODO Auto-generated method stub

		List<String> codeList = new ArrayList<>();
		codeList.add(OwnerTypeEnum.Individual.getCode());
		codeList.add(OwnerTypeEnum.Organization.getCode());
		codeList.add(OwnerTypeEnum.Company.getCode());
		List<OwnershipDTO> ownershipDTOList = ownershipDAO.findByStatusTrueAndCodeIn(codeList);
		if (CollectionUtils.isEmpty(ownershipDTOList)) {
			return Collections.emptyList();
		}

		return ownershipMapper.convertDTOs(ownershipDTOList);
	}

	@Override
	public Optional<List<TaxTypeVO>> taxDataEntryType(String classOfVehicle, String seatingCapacity, String rlw) {
		List<TaxTypeVO> listTaxperiod = new ArrayList<>();
		TaxTypeVO taxtype = new TaxTypeVO();
		Optional<MasterPayperiodDTO> Payperiod = MpayperiodDAO.findByCovcode(classOfVehicle);

		if (!Payperiod.isPresent()) {
			logger.error("No record found in master_payperiod for:  " + classOfVehicle);
			// throw error message
			throw new BadRequestException("No record found in master_payperiod for:  " + classOfVehicle);
		}

		if (ClassOfVehicleEnum.OBPN.getCovCode().equalsIgnoreCase(classOfVehicle)
				&& Payperiod.get().getPayperiod().equalsIgnoreCase("B")) {
			int result = Integer.parseInt(seatingCapacity);
			if (result > 10) {
				Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
			} else {
				Payperiod.get().setPayperiod(TaxTypeEnum.LifeTax.getCode());
			}
		}

		if (ClassOfVehicleEnum.TGVT.getCovCode().equalsIgnoreCase(classOfVehicle)
				&& Payperiod.get().getPayperiod().equalsIgnoreCase("B")) {
			int result = Integer.parseInt(rlw);
			if (result >= 3000) {
				Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
			} else {
				Payperiod.get().setPayperiod(TaxTypeEnum.LifeTax.getCode());
			}
		}
		if (ClassOfVehicleEnum.GCRT.getCovCode().equalsIgnoreCase(classOfVehicle)
				&& Payperiod.get().getPayperiod().equalsIgnoreCase("B")) {
			int result = Integer.parseInt(rlw);
			if (result >= 3000) {
				Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
			} else {
				Payperiod.get().setPayperiod(TaxTypeEnum.LifeTax.getCode());
			}
		}
		if (ClassOfVehicleEnum.ARKT.getCovCode().equalsIgnoreCase(classOfVehicle)
				&& Payperiod.get().getPayperiod().equalsIgnoreCase("B")) {
			int result = Integer.parseInt(rlw);
			if (result >= 3000) {
				Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
			} else {
				Payperiod.get().setPayperiod(TaxTypeEnum.LifeTax.getCode());
			}
		}

		if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.LifeTax.getCode())) {
			// return life tax
			taxtype.setTaxDescription(TaxTypeEnum.LifeTax.getDesc());
			taxtype.setTaxId(TaxTypeEnum.LifeTax.getCode());
			listTaxperiod.add(taxtype);

		} else if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.QuarterlyTax.getCode())) {
			// return life tax
			taxtype.setTaxDescription(TaxTypeEnum.QuarterlyTax.getDesc());
			taxtype.setTaxId(TaxTypeEnum.QuarterlyTax.getCode());
			listTaxperiod.add(taxtype);

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
			if (quaterOne.contains(LocalDate.now().getMonthValue())) {
				// return Q ,h , y
				taxtype.setTaxDescription(TaxTypeEnum.QuarterlyTax.getDesc());
				taxtype.setTaxId(TaxTypeEnum.QuarterlyTax.getCode());
				listTaxperiod.add(taxtype);

				if (!restrictHalfAndYearForChassis(classOfVehicle)) {
					taxtype.setTaxDescription(TaxTypeEnum.HalfyearlyTax.getDesc());
					taxtype.setTaxId(TaxTypeEnum.HalfyearlyTax.getCode());
					listTaxperiod.add(taxtype);
					taxtype.setTaxDescription(TaxTypeEnum.YearlyTax.getDesc());
					taxtype.setTaxId(TaxTypeEnum.YearlyTax.getCode());
					listTaxperiod.add(taxtype);

				}
			} else if (quaterTwo.contains(LocalDate.now().getMonthValue())) {
				// return Q
				taxtype.setTaxDescription(TaxTypeEnum.QuarterlyTax.getDesc());
				taxtype.setTaxId(TaxTypeEnum.QuarterlyTax.getCode());
				listTaxperiod.add(taxtype);

			} else if (quaterThree.contains(LocalDate.now().getMonthValue())) {
				// return Q ,h
				taxtype.setTaxDescription(TaxTypeEnum.QuarterlyTax.getDesc());
				taxtype.setTaxId(TaxTypeEnum.QuarterlyTax.getCode());
				listTaxperiod.add(taxtype);

				if (!restrictHalfAndYearForChassis(classOfVehicle)) {
					taxtype.setTaxDescription(TaxTypeEnum.HalfyearlyTax.getDesc());
					taxtype.setTaxId(TaxTypeEnum.HalfyearlyTax.getCode());
					listTaxperiod.add(taxtype);
				}
			} else if (quaterFour.contains(LocalDate.now().getMonthValue())) {
				// return Q
				taxtype.setTaxDescription(TaxTypeEnum.QuarterlyTax.getDesc());
				taxtype.setTaxId(TaxTypeEnum.QuarterlyTax.getCode());
				listTaxperiod.add(taxtype);
			}
			}

		return Optional.of(listTaxperiod);
	}

	private boolean restrictHalfAndYearForChassis(String cov) {

		if (ClassOfVehicleEnum.CHST.getCovCode().equalsIgnoreCase(cov)
				|| ClassOfVehicleEnum.CHSN.getCovCode().equalsIgnoreCase(cov)
				|| ClassOfVehicleEnum.ARVT.getCovCode().equalsIgnoreCase(cov)) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	@Override
	public MakersInfoVO fetchMasterMakers() {

		MakersInfoVO makersInfoVO = new MakersInfoVO();
		List<MakersDTO> masterDto = makersDAO.findAll();
		if (CollectionUtils.isEmpty(masterDto)) {
			logger.error("No data found in Maker_name status True");
			throw new BadRequestException("No data found in Maker_name status True");
		}
		makersInfoVO.setMakerNames(makersMapper.convertEntity(masterDto));

		List<BodyTypeDTO> listOfBodyTypes = bodyTypeDAO.findByStatusTrue();
		if (CollectionUtils.isEmpty(listOfBodyTypes)) {
			logger.error("No data found in Body type status True");
			throw new BadRequestException("No data found in Body type status True");
		}
		makersInfoVO.setBodyTypes(bodyTypeMapper.convertDTOs(listOfBodyTypes));
		List<MasterCovDTO> masterCovList = masterCovDAO.findAll();
		if (CollectionUtils.isEmpty(masterCovList)) {
			logger.error("No data found in covs status True");
			throw new BadRequestException("No data found in covs status True");
		}
		makersInfoVO.setCovTypes(masterCovMapper.convertDTOs(masterCovList));
		Optional<PropertiesDTO> propDto = propertiesDAO.findByFuelTypeStatusTrue();
		if (!propDto.isPresent()) {
			logger.error("No Data found for Fuel Type");
			throw new BadRequestException("No Data found for Fuel Type");
		}
		makersInfoVO.setFuelType(propertiesMapper.convertRequired(propDto.get()));
		makersInfoVO.setAxleType(AxleTypeEnum.getAxleTypes());
		return makersInfoVO;
	}

	@Override
	public Boolean saveMakerName(String makerName, String officeCode, String userId) {

		List<MakersDTO> masterDto = makersDAO.findAll();
		// AadharSeedingEngine seeding = new AadharSeedingEngine();
		for (MakersDTO makersDTO : masterDto) {
			String value1 = makerName.replaceAll(" ", "");
			String value2 = makersDTO.getMakername().replaceAll(" ", "");
			// Integer value= seeding.stringFullOrNoMatch(value1, value2);
			if (value1.equalsIgnoreCase(value2)) {
				logger.error("Maker Name Already Available :: [{}]", makerName);
				throw new BadRequestException("Maker Name Already Available :: " + makerName);
			}
		}
		MakersDTO makerDto = new MakersDTO();

		synchronized (officeCode.intern()) {
			Optional<MakersDTO> maker = makersDAO.findTop1ByOrderByMidDesc();
			if (!maker.isPresent()) {
				makerDto.setMid(1);
			}
			if (StringUtils.isBlank(userId)) {
				logger.error("UserId not avialable [{}]", userId);
				throw new BadRequestException("UserId not avialable");
			}
			makerDto.setCreatedBy(userId);
			makerDto.setCreatedDate(LocalDateTime.now());
			makerDto.setCreatedDateStr(LocalDateTime.now().toString());
			makerDto.setCreatedOfficeCode(officeCode);
			makerDto.setMakername(makerName.toUpperCase());
			makerDto.setStatus(Boolean.FALSE);
			if (makerDto.getMid() == null) {
				makerDto.setMid(maker.get().getMid() + 1);
			}
			makersDAO.save(makerDto);
		}

		return Boolean.TRUE;

	}

	@Override
	public List<MasterVariantVO> getModelDesc(Integer mid) {
		List<MasterVariantDTO> variantList = masterVariantDAO.findBymid(mid);
		if (CollectionUtils.isNotEmpty(variantList)) {
			return masterVaraintMapper.convertRequiredList(variantList);
		}
		return Collections.emptyList();
	}

	@Override
	public void saveMakersDetails(MakersDetails makersDetails, String officeCode, String userId) {
		Optional<UserDTO> userDetails = userDAO.findByUserId(userId);
		if (!userDetails.isPresent()) {
			logger.error("Unauthorized user [{}]", userId);
			throw new BadRequestException("Unauthorized user");
		}
		if (makersDetails.getAadhaarDetailsRequestVO() == null) {
			logger.error("Aadhaar Authentication is Mandatory");
			throw new BadRequestException("Aadhaar Authentication is Mandatory");
		}
		if (userDetails.get().getAadharNo() == null) {
			logger.error("RTO Aadhar Details Not available");
			throw new BadRequestException("RTO Aadhar Details Not available");
		}
		if (!userDetails.get().getPrimaryRole().getName().equalsIgnoreCase(RoleEnum.RTO.getName())) {
			logger.error("Unauthorized User to do the corrections");
			throw new BadRequestException("Unauthorized User to do the corrections");
		}
		Optional<AadharDetailsResponseVO> aadhaarResponseVO = getAadhaarResponse(
				makersDetails.getAadhaarDetailsRequestVO(), setAadhaarSourceDEtails(userId));
		if (!userDetails.get().getAadharNo().equals(String.valueOf(aadhaarResponseVO.get().getUid()))) {
			logger.error("Unauthorized User");
			throw new BadRequestException("Unauthorized User");
		}
		/**
		 * Optional<AadharDetailsResponseVO> aadhaarResponseVO = getAadhaarResponse(
		 * taxDataVO.getAadhaarDetailsRequestVO()); if
		 * (!userDetails.getAadharNo().equals(String.valueOf(aadhaarResponseVO.get().getUid())))
		 * { throw new BadRequestException("Unauthorized User"); }
		 */
		if (makersDetails.getMasterVariantVO() == null || makersDetails.getMasterVariantVO().getMid() == null
				|| StringUtils.isBlank(makersDetails.getMasterVariantVO().getModelDesc())) {
			logger.error("modelDesc/modelId is manadatory");
			throw new BadRequestException("modelDesc/modelId is manadatory");
		}
		Optional<MakersDTO> makerName = makersDAO.findBymid(makersDetails.getMasterVariantVO().getMid());
		if (!makerName.isPresent()) {
			logger.error("Maker Name not available for model Id : [{}]", makersDetails.getMasterVariantVO().getMid());
			throw new BadRequestException(
					"Maker Name not available for model Id : " + makersDetails.getMasterVariantVO().getMid());
		}
		List<MasterVariantDTO> variantList = masterVariantDAO.findBymid(makersDetails.getMasterVariantVO().getMid());
		// AadharSeedingEngine seeding = new AadharSeedingEngine();
		for (MasterVariantDTO masterVariantDTO : variantList) {
			String value1 = makersDetails.getMasterVariantVO().getModelDesc().replaceAll(" ", "").toLowerCase();
			String value2 = masterVariantDTO.getModelDesc().replaceAll(" ", "").toLowerCase();
			// Integer value= seeding.stringFullOrNoMatch(value1,value2);makerNameValidation
			// Integer value= seeding.makerNameValidation(value1,value2);
			if (value1.equalsIgnoreCase(value2)) {
				logger.error("Maker Class [{}] is Already Available with Id [{}]",
						makersDetails.getMasterVariantVO().getModelDesc(), makersDetails.getMasterVariantVO().getMid());
				throw new BadRequestException("Maker Class " + makersDetails.getMasterVariantVO().getModelDesc()
						+ " is Already Available with Id" + makersDetails.getMasterVariantVO().getMid());
			}
		}

		synchronized (officeCode.intern()) {
			MasterVariantDTO masterDto = masterVaraintMapper.convertVO(makersDetails.getMasterVariantVO());
			Optional<MasterVariantDTO> masterDAO = masterVariantDAO.findTop1ByOrderByVehicleMkSerialNODesc();
			if (!masterDAO.isPresent()) {
				masterDto.setVehicleMkSerialNO(1);
			}
			if (masterDto.getVehicleMkSerialNO() == null) {
				masterDto.setVehicleMkSerialNO(masterDAO.get().getVehicleMkSerialNO());
			}
			masterDto.setCreatedBy(userId);
			masterDto.setCreatedDate(LocalDateTime.now());
			masterDto.setCreatedDateStr(LocalDateTime.now().toString());
			masterDto.setCreatedOfficeCode(officeCode);
			makerName.get().setStatus(Boolean.TRUE);
			makerName.get().setlUpdate(LocalDateTime.now());
			makersDAO.save(makerName.get());
			masterVariantDAO.save(masterDto);
		}

	}

	public AadhaarSourceDTO setAadhaarSourceDEtails(String userId) {
		AadhaarSourceDTO aadhaarSourceDTO = new AadhaarSourceDTO();
		aadhaarSourceDTO.setUser(userId);
		return aadhaarSourceDTO;

	}

	public Optional<AadharDetailsResponseVO> getAadhaarResponse(AadhaarDetailsRequestVO aadhaarDetailsRequestVO,
			AadhaarSourceDTO aadhaarSourceDTO) {
		Optional<AadharDetailsResponseVO> applicantDetailsOptional = restGateWayService
				.validateAadhaar(aadhaarDetailsRequestVO, aadhaarSourceDTO);
		if (!applicantDetailsOptional.isPresent()) {
			logger.error("No data Found for Aadhaar : [{}]", aadhaarDetailsRequestVO.getUid_num());
			throw new BadRequestException(MessageKeys.AADHAAR_RES_NO_DATA);

		}
		if (applicantDetailsOptional.get().getAuth_status()
				.equals(ResponseStatusEnum.AADHAARRESPONSE.FAILED.getLabel())) {
			logger.error("Aadhaar Validation Failed for [{}], Failed Message  : [{}]",
					applicantDetailsOptional.get().getUid(), applicantDetailsOptional.get().getAuth_status());
			throw new BadRequestException(applicantDetailsOptional.get().getAuth_err_code());
		}
		return applicantDetailsOptional;
	}

}
