/**
 * 
 */
package org.epragati.restGateway;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.epragati.aadhaar.AadhaarDetailsRequestVO;
import org.epragati.aadhaar.AadharDetailsResponseVO;
import org.epragati.aadhaarAPI.AadhaarSourceDTO;
import org.epragati.cfst.vcr.dao.VcrDetailsDto;
import org.epragati.cfstSync.vo.PaymentDetails;
import org.epragati.cfstVcr.vo.TaxPaidVCRDetailsVO;
import org.epragati.cfstVcr.vo.VcrBookingData;
import org.epragati.cfstVcr.vo.VcrInputVo;
import org.epragati.civilsupplies.vo.RationCardDetailsVO;
import org.epragati.common.dto.HsrpDetailDTO;
import org.epragati.common.dto.PanDetailsModel;
import org.epragati.ecv.vo.EngineChassisNOEntity;
import org.epragati.ecv.vo.EngineChassisNoVO;
import org.epragati.elastic.vo.ElasticSecondVehicleSearchVO;
import org.epragati.gstn.vo.GSTNDataVO;
import org.epragati.hsrp.vo.HSRPRequestModel;
import org.epragati.hsrp.vo.HSRPResposeVO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.vo.PanVO;
import org.epragati.master.vo.VCRVahanVehicleDetailsVO;
import org.epragati.master.vo.VahanDetailsVO;
import org.epragati.master.vo.VahanVehicleDetailsVO;
import org.epragati.payment.report.vo.RegReportVO;
import org.epragati.payments.vo.TransactionDetailVO;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.vo.OtherStateVahanVO;
import org.epragati.rta.vo.PrGenerationVO;
import org.epragati.svs.vo.ElasticSecondVehicleResponseVO;
import org.epragati.svs.vo.ResponseListEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

/**
 * @author venkateswarlu.udiga
 *
 */
/* @Service */
public interface RestGateWayService {

	Optional<PanVO> getPanDetails(PanDetailsModel pdModel, String panNumber);

	Optional<AadharDetailsResponseVO> validateAadhaar(AadhaarDetailsRequestVO model, AadhaarSourceDTO aadhaarSourceDTO);

	Optional<VahanDetailsVO> getVahanDetails(String engineNo, String chasisNo, String userId,
			Boolean isRequiredVahanSync);
	
	Optional<VahanDetailsVO> getVahanDetailsUsingEngineNoAndChasisNo(String engineNo, String chasisNo);

	/**
	 * related to hsrp third party call
	 */
	Optional<HSRPResposeVO> callAPIPost(String apiPath, HSRPRequestModel sendData, String contentType);

	/**
	 * Retrieving Second Vehicle Details Based on the application number
	 * 
	 * @param ownerDetailsVO
	 * @return
	 */
	Optional<ResponseListEntity> getSecondVehiclesList(String applicationNo);

	Optional<EngineChassisNOEntity> validateEngineChassisNo(EngineChassisNoVO engineChassisNoVO);

	boolean validateVahanDetailsInStagingRegistrationDetails(String engineNo, String chasisNo);

	/**
	 * 
	 * @param fromDate
	 * @return
	 */
	Optional<List<PaymentDetails>> getRegistrationPayments(LocalDate fromDate, LocalDate toDate, String officeCode);

	Optional<VahanDetailsVO> getVahanDetails(String engineNo, String chasisNo);

	Boolean saveCfstPaymentStatus(String applcationNo, String status);

	Boolean paymentDetailsExcelReport(HttpServletResponse response, String officeCode, LocalDate fromDate,
			LocalDate toDate);

	Optional<ElasticSecondVehicleResponseVO> searchSecondVehicleDocs(
			ElasticSecondVehicleSearchVO elasticSecondVehicleSearchVO);

	VcrBookingData getVcrDetailsCfst(VcrInputVo vcrInputVo);

	Optional<VcrDetailsDto> getVcrDetails(VcrInputVo vcrInputVo);

	Optional<RegServiceDTO> checkDataEntryExits(String prNO);

	Boolean hsrpExcelReport(HttpServletResponse response, Integer catagory, LocalDate fromDate, LocalDate toDate);

	Optional<List<HsrpDetailDTO>> getHsrpPostedList(LocalDate fromDate, LocalDate toDate, Integer catagory);

	Optional<RegistrationDetailsDTO> checkDataEntryExitsInRegDetails(String prNO,Optional<RegServiceDTO> regDatEntry);

	Optional<TransactionDetailVO> getPaymentRequestObjectThroughjRestCall(TransactionDetailVO transactionDetailVO);

	String generatePrNo(PrGenerationVO prGenerationVO);

	TaxPaidVCRDetailsVO getTaxPaidVCRData(String prNo);

	Optional<GSTNDataVO> getGSTNToken(String gstinNo);

	RationCardDetailsVO getRationCardDetails(String aadharNo, String district);

	String generatePaymentReciept();

	Optional<VahanVehicleDetailsVO> getVahanVehicleDetails(String prNo);

	OtherStateVahanVO getVahanVehicleDetailsForOtherState(String prNo);

	VCRVahanVehicleDetailsVO getVahanVehicleDetailsForVcr(String prNo);

	Boolean validateOtherStateNOC(Optional<RegServiceDTO> regDatEntry);

	Map<Object, Map<String, Double>> dlRevenue(RegReportVO paymentreportVO);

	List<RegReportVO> dlRevenueDetailed(RegReportVO paymentreportVO, Pageable page);
}
