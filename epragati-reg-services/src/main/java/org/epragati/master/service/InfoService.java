package org.epragati.master.service;

import java.util.List;
import java.util.Optional;

import org.epragati.master.dto.GateWayDTO;
import org.epragati.master.vo.BloodGroupVO;
import org.epragati.master.vo.BodyTypeVO;
import org.epragati.master.vo.CategoryVO;
import org.epragati.master.vo.InsuranceCompanyVO;
import org.epragati.master.vo.InsuranceTypeVO;
import org.epragati.master.vo.MakersDetails;
import org.epragati.master.vo.MakersInfoVO;
import org.epragati.master.vo.MasterCovVO;
import org.epragati.master.vo.MasterFinanceTypeVO;
import org.epragati.master.vo.MasterInfoVO;
import org.epragati.master.vo.MasterVariantVO;
import org.epragati.master.vo.ModelDesVO;
import org.epragati.master.vo.OwnershipVO;
import org.epragati.master.vo.QualificationVO;
import org.epragati.master.vo.TaxTypeVO;
import org.epragati.master.vo.TrailerCOVInfoVO;
import org.epragati.util.payment.GatewayTypeEnum;

public interface InfoService {
	/**
	 * 
	 * @param dealerUserId
	 * @return
	 */
	Optional<MasterInfoVO> getMasterInfo(String dealerUserId);
	
	/**
	 * 
	 * @param modelDesVO
	 * @return
	 */
	List<MasterVariantVO> getModelDecs(ModelDesVO modelDesVO);
	
	BodyTypeVO getBodyType(String bodyTypeId);
	
	Optional<List<InsuranceTypeVO>> insuranceTypes();
	 
	Optional<List<MasterFinanceTypeVO>> financeTypes();
	
	Optional<List<QualificationVO>> qualificationTypes();
	
	Optional<List<BloodGroupVO>> bloodGroupTypes();
	
	Optional<List<CategoryVO>> categoryType();

	Optional<List<TaxTypeVO>> taxType();
	
	Optional<List<OwnershipVO>> ownerType();

	List<MasterCovVO> getTrailerCovs(String userId, String vehicleType);

	Optional<TrailerCOVInfoVO> getTrailerVariantDetails(String userId, String cov);

	GateWayDTO findByGateWayType(GatewayTypeEnum payu);

	Optional<List<InsuranceCompanyVO>> getAllInsuranceCompName();

	List<OwnershipVO> getOwnerShipTypes();
	Optional<List<TaxTypeVO>> taxDataEntryType(String covcode,String seatingCapacity,String rlw);

	MakersInfoVO fetchMasterMakers();

	Boolean saveMakerName(String makerName, String officeCode, String userId);
	
	List<MasterVariantVO> getModelDesc(Integer mid);

	void saveMakersDetails(MakersDetails makersDetails,String officeCode, String userId);
}
