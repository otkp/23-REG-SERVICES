package org.epragati.master.service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.payments.vo.TransactionDetailVO;
import org.epragati.permits.dto.PermitDetailsDTO;
import org.epragati.permits.dto.PermitRoutesForSCRTDTO;
import org.epragati.permits.dto.PermitValidityDetailsDTO;
import org.epragati.permits.vo.OtherStateTemporaryPermitDetailsVO;
import org.epragati.permits.vo.PermitClassVO;
import org.epragati.permits.vo.PermitDetailsVO;
import org.epragati.permits.vo.PermitDistrictAdjesentDistrictVO;
import org.epragati.permits.vo.PermitGoodsDetailsVO;
import org.epragati.permits.vo.PermitRouteDetailsVO;
import org.epragati.permits.vo.PermitRouteTypeVO;
import org.epragati.permits.vo.PermitRoutesForSCRTVO;
import org.epragati.permits.vo.PermitTypeVO;
import org.epragati.permits.vo.PermitValidationsVO;
import org.epragati.permits.vo.PermitValidityDetailsVO;
import org.epragati.permits.vo.TPDetailsSearchVO;
import org.epragati.permits.vo.TemporaryPermitPassengerDetailsVO;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.vo.ApplicationSearchVO;
import org.epragati.regservice.vo.RegServiceVO;

/**
 * 
 * @author sairam.cheruku
 *
 */
public interface PermitsService {

	/**
	 * Fetching master data of permit class 
	 * 
	 * @param serviceName (From service combinations that is selected by citizen)
	 * 
	 * @return List of Permit class
	 */
	List<PermitClassVO> getPermitClassMasterData(String serviceName);

	/**
	 * 
	 * @param prNo
	 * @param permitType
	 * @return Permit Good Details List
	 */
	List<PermitGoodsDetailsVO> getPermitGoodsDetails(String prNo, String permitType, String classOfVehicle,
			String bodyType);

	/**
	 * This method is used to set the Permits validity
	 * 
	 * @param permitDetailsVO
	 * @return
	 */
	PermitValidityDetailsDTO setPermitValidityDetails(PermitValidityDetailsDTO permitValidityDetailsDTO,
			PermitDetailsDTO permitDetailsDTO, RegServiceDTO regServiceDTO);

	/**
	 * Generation of permit number and Permit details saving
	 * 
	 * @param registrationDetailsDTO
	 */
	void savePermitDetailsForNewPermit(RegServiceDTO registrationDetailsDTO);

	/**
	 * To check weather the permit details with the pr no and permit type and status
	 * 
	 * @param prNo
	 * @return PermitDetailsDTO
	 */
	Optional<PermitDetailsVO> findPermitDetailsByRcNoAndStatus(String prNo);

	/**
	 * This method is used to return the value
	 * 
	 * @param prNo
	 * @return Permit details DTO
	 */
	Optional<List<PermitDetailsDTO>> getListOfPermitDetailsByPrNoAndStatus(String prNo);

	/**
	 * To get permit details based on PR or permit number
	 * 
	 * @param applicationSearchVO
	 * @return
	 */
	Optional<List<PermitDetailsVO>> searchApplicationByPermitOrPrNo(ApplicationSearchVO applicationSearchVO);

	/**
	 * Fetch permit route details
	 * 
	 * @param prNo
	 * @param permitType
	 * @return
	 */
	List<PermitRouteDetailsVO> getPermitsRouteDetailsList(String prNo, String permitType);

	/**
	 * 
	 * @param prNo
	 * @param routeType
	 * @return District VO
	 */
	Optional<PermitDistrictAdjesentDistrictVO> getDistrictBasedOnPrNo(String prNo, String routeType);

	/**
	 * 
	 * @param prNo
	 * @param authorization
	 * @return
	 */

	List<PermitRouteTypeVO> getListOfTypeOfRoute(String prNo, boolean authorization);

	PermitDetailsDTO fetchPermitDetails(String prNo);

	/**
	 * 
	 * @param serviceIds
	 * @return
	 */
	boolean verifyForPermitServices(List<Integer> serviceIds);

	public List<PermitDetailsDTO> saveSurrendOfPermit(List<PermitDetailsVO> permitDetailsVOs);

	/**
	 * This method used to get the records for permits with permit class and prNO
	 * 
	 * @param prNo
	 * @param typeofPermit
	 * @return
	 */
	List<PermitDetailsVO> getListOfPermitRecords(String prNo, String typeofPermit);

	Optional<PermitValidationsVO> getPermitVariationBasedOnCov(String prNo);

	/**
	 * To check weather the permit details with the pr no and pucca permit type and
	 * status active or not
	 * 
	 * @param prNo
	 * @return
	 * 
	 */
	Optional<PermitDetailsVO> findPermitInactiveRecords(String prNo);

	Optional<PermitDetailsVO> findByPrNoOrPermitNoStatusBased(ApplicationSearchVO applicationSearchVO);

	Optional<PermitDetailsVO> vehicleReplaceValidations(String prNo1, String prNo2);

	Optional<PermitDetailsVO> findSecondPermitActiveRecords(String prNo);

	Optional<PermitDetailsVO> findTemporayPermitInactiveRecords(String prNo);

	void checkWithTaxExpairyDays(PermitDetailsDTO dto, String prNo);

	void checExtensionValidtiykWithTaxExpairyDays(PermitDetailsVO permitVO, String prNo);

	void checkForExtensionOfValidity(PermitDetailsVO permitVO);

	/**
	 * To get the permit type
	 * @param prNo
	 * @param permitClass
	 * @param classOfVehicle
	 * @return
	 */
	Optional<List<PermitTypeVO>> getPermitTypeDetails(String prNo, String permitClass, String classOfVehicle);

	/**
	 * 
	 * @param permitClass
	 * @return
	 */
	List<String> getStatesForCounterSignature(String permitClass);

	/**
	 * 
	 * @param prNo
	 * @return
	 */
	List<PermitDetailsVO> findRlStateWiseRecords(String prNo);

	/**
	 * Get No Of months for All India Permit
	 * 
	 * @return
	 */
	List<Integer> getNoOfMonthsForAllIndiaPermit();

	/**
	 * Permit Detail and Tax Details validation for Passengers List
	 * 
	 * @param applicationSearchVO
	 * @return
	 */
	Optional<TPDetailsSearchVO> getPermitdetailsForPassengerList(ApplicationSearchVO applicationSearchVO);

	/**
	 * Save Passenger Details for Temporary Permit
	 * 
	 * @param temporaryPermitPassengerDetailsVO
	 */
	void savePassengerListForTP(TemporaryPermitPassengerDetailsVO temporaryPermitPassengerDetailsVO);

	/**
	 * Updation of Latest pr No in permit_details collection after reassignment
	 * 
	 * @param staginDto
	 */
	void updatePermitDetailsAfterReassignment(RegistrationDetailsDTO staginDto);

	/**
	 * 
	 * @param prNo
	 * @return
	 */
	Optional<OtherStateTemporaryPermitDetailsVO> getOtherStateTPDetails(String prNo);

	void saveOtherStateDetailsForTemporaryPermit(String ApplicationNo, TransactionDetailVO transactionDetailVO);

	/**
	 * To fetch Recommendation Letter Details with PrNo
	 * 
	 * @param prNo
	 * @return
	 */
	List<PermitDetailsDTO> fetchRecommendationLetterDetails(String prNo);

	/**
	 * Giving Dates Dynamically for TP & SP
	 * 
	 * @param tpValidity
	 * @return
	 */
	PermitValidityDetailsVO setDatesForOtheStateTPAndSP(String tpValidity);

	/**
	 * Validations before save
	 * 
	 * @param inputRegServiceVO
	 */
	void doValidateBeforeSaveOfTPandSP(RegServiceVO inputRegServiceVO);

	void saveScrtPermit(RegServiceDTO regServiceDTO);

	/**
	 * Get Tax Types based on covs
	 * @param covCode
	 * @return
	 */
	Set<String> getTaxTypesForTPSP(String covCode);

	List<PermitRoutesForSCRTVO> getRoutesForSCRT(String routes);
	
	String generatePermitNo(String officeCode, String permitNumberCode);


}
