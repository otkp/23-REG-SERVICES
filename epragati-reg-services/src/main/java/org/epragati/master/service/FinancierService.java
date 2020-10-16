package org.epragati.master.service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.epragati.dealer.vo.RequestDetailsVO;
import org.epragati.dto.enclosure.FinancerUploadedDetailsDTO;
import org.epragati.dto.enclosure.UploadExcelDTO;
import org.epragati.exception.RcValidationException;
import org.epragati.financier.vo.FinancierActionVO;
import org.epragati.financier.vo.FinancierCreateRequestVO;
import org.epragati.financier.vo.UploadExcelFileVO;
import org.epragati.images.vo.InputVO;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dto.FinancierCreateRequestDTO;
import org.epragati.master.dto.FinancierSeriesDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.vo.FinanceDetailsVO;
import org.epragati.master.vo.FinanceSeedDetailsVO;
import org.epragati.master.vo.FinancierDashBoardVO;
import org.epragati.master.vo.MasterFinanceTypeVO;
import org.epragati.master.vo.RegistrationDetailsVO;
import org.epragati.master.vo.UserVO;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.vo.FreshApplicationSearchVO;
import org.epragati.regservice.vo.FreshRcVO;
import org.epragati.regservice.vo.RegServiceVO;
import org.epragati.service.enclosure.vo.CitizenImagesInput;
import org.epragati.service.enclosure.vo.EnclosureRejectedVO;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.web.multipart.MultipartFile;

/**
 * 
 * @author krishnarjun.pampana
 *
 */
public interface FinancierService {

	/**
	 * 
	 * @return
	 */
	FinancierDashBoardVO fetchFinancierDashBoard(String usderId);

	/**
	 * 
	 * @param financeDetailsVO
	 * @return
	 */
	Optional<RegistrationDetailsVO> getOwnerDetailsByToken(FinanceDetailsVO financeDetailsVO);

	/**
	 * 
	 * @param id
	 * @return
	 */
	Optional<List<RegistrationDetailsVO>> getViewDetails(String type, String userId);

	void doAction(FinancierActionVO financierActionVO, UserDTO userDTO);

	/**
	 * 
	 * @param financeType by status
	 * @return
	 */
	List<MasterFinanceTypeVO> findFinanceTypeByStatus(boolean status);

	Optional<UserVO> savefinancierDetails(UserVO uservo);

	/**
	 * 
	 * @return
	 */
	String geneateFinancerApplicationSeries();

	/**
	 * 
	 * @param financierSeriesDTO
	 * @return
	 */
	FinancierSeriesDTO updateCurrentNo(FinancierSeriesDTO financierSeriesDTO);

	Optional<RegServiceVO> getDetailsBasedOnengNOChassNO(RequestDetailsVO requestDetailsVO, UserDTO user);

	/**
	 * 
	 * @param userId
	 * @return dashboardcount
	 */
	FinancierDashBoardVO fetchFinancierDashBoardDetailsForServices(String userId);

	/**
	 * 
	 * @param type
	 * @param userId
	 * @return services List
	 */
	Optional<List<RegServiceVO>> getViewDetailsForServices(String type, String userId);

	/**
	 * @param financeDetailsVO
	 * @return Details of Application NO
	 */

	Optional<RegServiceVO> getOwnerDetailsOfSerivicesByApplicationNo(FinanceDetailsVO financeDetailsVO);

	/**
	 * 
	 * @param financierActionVO
	 */
	void doActionForServices(FinancierActionVO financierActionVO);

	/**
	 * 
	 * @param applicationNo
	 * @return
	 */
	Optional<RegServiceVO> getServiceDetailsByApplicationNumber(String applicationNo);

	/**
	 * 
	 * @param requestDetailsVO
	 * @param userId
	 * @return Reg service list
	 */
	Optional<RegServiceVO> getReportsForRegServices(RequestDetailsVO requestDetailsVO, String userId);

	Optional<RegistrationDetailsVO> getOwnerDetailsByTokenForRegService(FinanceDetailsVO financeDetailsVO);

	Optional<RegServiceVO> getOwnerDetailsOfSerivicesByPrNo(String prNo);

	void doFinanceProcess(FinancierActionVO financierActionVO, UserDTO userDTO);

	boolean serviceType(String applicationNo);

	Optional<RegistrationDetailsVO> getFinanceDetailsByPrNo(FinancierActionVO financierActionVO, UserDTO userDTO,String user);

	RegServiceVO doFreshRcForFinance(String regServiceVO, MultipartFile[] uploadfiles, String user)
			throws IOException, RcValidationException;

	public String saveUploadedFiles(UploadExcelFileVO uploadFile, String uploadBy) throws IOException, Exception;

	public UploadExcelDTO saveDetails(MultipartFile file, String folder, UploadExcelFileVO vo);

	public List<FinancerUploadedDetailsDTO> saveUploadFile(MultipartFile excelFile, JwtUser jwtUser,
			boolean overridePrevRec);

	Map<String, Object> findAllUploadedFinanceDetails(String jwtUser, Pageable pageable);

	/**
	 * ==================== Financier Create Request
	 * 
	 * @param userVO
	 * @param uploadfiles
	 * @return
	 */
	Optional<FinancierCreateRequestVO> financierCreateRequest(FinancierCreateRequestVO userVO,
			MultipartFile[] uploadfiles);

	/**
	 * getList Of Supported Enclosers
	 * 
	 * @param input
	 * @return
	 */
	Optional<InputVO> getListOfSupportedEnclosers(CitizenImagesInput input);

	/**
	 * convertJson To Financier Object
	 * 
	 * @param userVO
	 * @return
	 */
	Optional<FinancierCreateRequestVO> convertJsonToFinancierObject(String userVO);

	/**
	 * update FinancierApp Status Approve/Reject
	 * 
	 * @param finAppNo
	 * @param finDTO
	 * @param jwtUser
	 * @param selectedRole
	 * @return
	 */
	Optional<FinancierCreateRequestDTO> financierApprovalProcess(String finAppNo, FinancierCreateRequestDTO finDTO,
			JwtUser jwtUser, String selectedRole);

	/**
	 * get Financier Details By FinAppNo
	 * 
	 * @param dto
	 * @return
	 */
	Optional<FinancierCreateRequestDTO> getFinancierDetailsByFinAppNo(FinancierCreateRequestDTO dto);

	/**
	 * re Update Financier Details By FinAppNo
	 * 
	 * @param financierCreateRequestVO
	 * @param uploadfiles
	 * @return
	 */
	Optional<FinancierCreateRequestDTO> reUpdateFinancierDetailsByFinAppNo(String appNo, MultipartFile[] uploadfiles);

	/**
	 * getListOfRejectedEnclosures
	 * 
	 * @param applicationNo
	 * @return
	 */
	List<EnclosureRejectedVO> getListOfRejectedEnclosures(String applicationNo);

	/**
	 * 
	 * @param response
	 * @param catagory
	 * @param fromDate
	 * @param toDate
	 * @return
	 */
	Boolean rcDetailsExcelReport(HttpServletResponse response, Integer catagory, LocalDate fromDate, LocalDate toDate);

	/**
	 * 
	 * @param loggedInUser
	 * @param pageable
	 * @return
	 */
	Map<String, Object> getChildFinanciersList(String loggedInUser, String primaryRole, Pageable pageable);

	/**
	 * Parent user can modify child user pwd
	 * 
	 * @param childUserVO
	 * @param loggedInUser
	 * @return
	 */
	Optional<UserDTO> changeChildUserPwd(UserVO childUserVO, String loggedInUser);

	Optional<RegistrationDetailsVO> getFinanceDetailsByTrNo(FinancierActionVO financierActionVO, UserDTO userDTO,String user);

	// As per murthy gaaru inputs commented TR flow for freshRC once murthy gaaru
	// gives approval uncomment code
	/*
	 * RegServiceVO doFreshRcForTrNo(String regServiceVO, MultipartFile[]
	 * multipartFile, Boolean isTrNo,String user) throws IOException,
	 * RcValidationException;
	 */

	List<Pair<Boolean, FreshApplicationSearchVO>> reuploadImagesForFreshRc(String user);

	List<FreshApplicationSearchVO> dispalyRecordOfFreshRc(String user);
	List<Map<String, Object>> checkValideFinancier(String name);
	void saveAdressOfFinancier(FreshRcVO vo,String userId);

	FinanceSeedDetailsVO getDetailsByFinanceuserId(FinanceSeedDetailsVO financeSeedDetailsVO, JwtUser jwtUser);

	void saveVehicleDetails(FinanceSeedDetailsVO financeVO, JwtUser jwtUser);

	List<FinanceSeedDetailsVO> getPendingListView(JwtUser jwtUser, String selectedRole);

	void saveFinancier(FinanceSeedDetailsVO vo, String selectedRole, JwtUser jwtUser);


}
