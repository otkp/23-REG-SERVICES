package org.epragati.vcr.service.impl;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.group;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.project;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.tomcat.util.codec.binary.Base64;
import org.epragati.cfstVcr.vo.VcrInputVo;
import org.epragati.common.dao.PropertiesDAO;
import org.epragati.common.dto.PropertiesDTO;
import org.epragati.constants.CovCategory;
import org.epragati.constants.FcValidityTypesEnum;
import org.epragati.constants.NationalityEnum;
import org.epragati.dealer.vo.TrIssuedReportVO;
import org.epragati.dto.enclosure.ImageEnclosureDTO;
import org.epragati.excel.service.CellProps;
import org.epragati.excel.service.ExcelService;
import org.epragati.excel.service.ExcelServiceImpl;
import org.epragati.exception.BadRequestException;
import org.epragati.exception.ResourceNotFoudException;
import org.epragati.images.vo.ImageInput;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dao.DistrictDAO;
import org.epragati.master.dao.FcDetailsDAO;
import org.epragati.master.dao.MasterCovDAO;
import org.epragati.master.dao.MasterPayperiodDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.RegServiceDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.StagingRegistrationDetailsDAO;
import org.epragati.master.dao.StateDAO;
import org.epragati.master.dao.TaxDetailsDAO;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dao.VcrGoodsDao;
import org.epragati.master.dto.DistrictDTO;
import org.epragati.master.dto.FcDetailsDTO;
import org.epragati.master.dto.MasterCovDTO;
import org.epragati.master.dto.MasterPayperiodDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.RolesDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.dto.StateDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.dto.VahanDetailsDTO;
import org.epragati.master.dto.VcrGoodsDTO;
import org.epragati.master.mappers.DistrictMapper;
import org.epragati.master.mappers.FcDetailsMapper;
import org.epragati.master.mappers.MasterCovMapper;
import org.epragati.master.mappers.OfficeMapper;
import org.epragati.master.mappers.PermitDetailsMapper;
import org.epragati.master.mappers.PermitUtilizationMapper;
import org.epragati.master.mappers.RegistrationDetailsMapper;
import org.epragati.master.mappers.StagingDetailsMapper;
import org.epragati.master.mappers.UserMapper;
import org.epragati.master.mappers.VCRFcMapper;
import org.epragati.master.mappers.VCRFinalDetailsMapper;
import org.epragati.master.mappers.VCRPermitMapper;
import org.epragati.master.mappers.VCRTaxMapper;
import org.epragati.master.mappers.VcrGoodsMapper;
import org.epragati.master.service.DistrictService;
import org.epragati.master.service.OfficeService;
import org.epragati.master.service.StateService;
import org.epragati.master.vo.ApplicantAddressVO;
import org.epragati.master.vo.ApplicantSearchWithOutIdInput;
import org.epragati.master.vo.DistrictVO;
import org.epragati.master.vo.FcDetailsVO;
import org.epragati.master.vo.MasterCovVO;
import org.epragati.master.vo.OffenceVO;
import org.epragati.master.vo.OfficeVO;
import org.epragati.master.vo.RegistrationDetailsVO;
import org.epragati.master.vo.RegistrationValidityVO;
import org.epragati.master.vo.StateVO;
import org.epragati.master.vo.UserVO;
import org.epragati.master.vo.VCRVahanVehicleDetailsVO;
import org.epragati.master.vo.VcrGoodsVO;
import org.epragati.payment.dto.PaymentTransactionDTO;
import org.epragati.payment.report.vo.RegReportVO;
import org.epragati.payment.report.vo.ReportsVO;
import org.epragati.payments.dao.PaymentTransactionDAO;
import org.epragati.permits.dao.PermitDetailsDAO;
import org.epragati.permits.dto.PermitDetailsDTO;
import org.epragati.permits.dto.PermitUtilizationDTO;
import org.epragati.permits.vo.OtherStateTemporaryPermitDetailsVO;
import org.epragati.permits.vo.PermitDetailsVO;
import org.epragati.permits.vo.PermitUtilizationVO;
import org.epragati.permits.vo.PermitValidityDetailsVO;
import org.epragati.registration.service.RegistrationMigrationSolutionsService;
import org.epragati.regservice.CitizenTaxService;
import org.epragati.regservice.RegistrationService;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.impl.RegistrationServiceImpl;
import org.epragati.regservice.mapper.RegServiceMapper;
import org.epragati.regservice.vo.ApplicationSearchVO;
import org.epragati.regservice.vo.RcValidationVO;
import org.epragati.regservice.vo.RegServiceVO;
import org.epragati.regservice.vo.SpeedGunVO;
import org.epragati.regservice.vo.TaxDetailsVahanVcrVO;
import org.epragati.reports.service.PaymentReportService;
import org.epragati.reports.service.ReportService;
import org.epragati.restGateway.RestGateWayService;
import org.epragati.rta.reports.vo.CitizenSearchReportVO;
import org.epragati.rta.reports.vo.ReportVO;
import org.epragati.rta.reports.vo.UserWiseEODCount;
import org.epragati.rta.service.impl.service.RegistratrionServicesApprovals;
import org.epragati.rta.vo.RtaActionVO;
import org.epragati.service.files.GridFsClient;
import org.epragati.service.notification.MessageTemplate;
import org.epragati.service.notification.NotificationTemplates;
import org.epragati.service.notification.NotificationUtil;
import org.epragati.tax.vo.TaxTypeEnum;
import org.epragati.util.DateConverters;
import org.epragati.util.PermitsEnum;
import org.epragati.util.PermitsEnum.PermitType;
import org.epragati.util.PermitsEnum.RouteType;
import org.epragati.util.RoleEnum;
import org.epragati.util.StatusRegistration;
import org.epragati.util.StatusRegistration.VcrEnum;
import org.epragati.util.document.KeyValue;
import org.epragati.util.payment.ClassOfVehicleEnum;
import org.epragati.util.payment.PayStatusEnum;
import org.epragati.util.payment.ServiceEnum;
import org.epragati.vcr.service.OffenceService;
import org.epragati.vcr.service.VcrService;
import org.epragati.vcr.vo.DriverDetailsVcrVo;
import org.epragati.vcr.vo.FromVcrVo;
import org.epragati.vcr.vo.MasterOffenceSectionsVO;
import org.epragati.vcr.vo.MasterOffendingSectionsVO;
import org.epragati.vcr.vo.MasterPenalSectionsVO;
import org.epragati.vcr.vo.OffenceVcrVO;
import org.epragati.vcr.vo.OwnerDetailsVo;
import org.epragati.vcr.vo.RegistrationVcrVo;
import org.epragati.vcr.vo.ToVcrVO;
import org.epragati.vcr.vo.ValidityDetailsVo;
import org.epragati.vcr.vo.VcrFinalServiceVO;
import org.epragati.vcr.vo.VcrImageVO;
import org.epragati.vcr.vo.VcrVo;
import org.epragati.vcr.vo.VehicleProceedingVO;
import org.epragati.vcrImage.dao.FreezeVehiclsDAO;
import org.epragati.vcrImage.dao.MasterOffenceSectionsDAO;
import org.epragati.vcrImage.dao.MasterOffendingSectionsDAO;
import org.epragati.vcrImage.dao.MasterPenalSectionsDAO;
import org.epragati.vcrImage.dao.SpeedGunVcrDAO;
import org.epragati.vcrImage.dao.VcrDeleteDetailsDAO;
import org.epragati.vcrImage.dao.VcrFinalServiceDAO;
import org.epragati.vcrImage.dao.VcrImageDAO;
import org.epragati.vcrImage.dao.VoluntaryTaxDAO;
import org.epragati.vcrImage.dto.EVcrPrintedDTO;
import org.epragati.vcrImage.dto.FreezeVehiclsDTO;
import org.epragati.vcrImage.dto.MasterOffenceSectionsDTO;
import org.epragati.vcrImage.dto.MasterOffendingSectionsDTO;
import org.epragati.vcrImage.dto.MasterPenalSectionsDTO;
import org.epragati.vcrImage.dto.OtherSectionDTO;
import org.epragati.vcrImage.dto.SpeedGunDTO;
import org.epragati.vcrImage.dto.SpeedGunPrintDTO;
import org.epragati.vcrImage.dto.VcrDeleteDetailsDTO;
import org.epragati.vcrImage.dto.VcrFinalServiceDTO;
import org.epragati.vcrImage.dto.VcrImageDTO;
import org.epragati.vcrImage.dto.VoluntaryTaxDTO;
import org.epragati.vcrImage.mapper.MasterOffenceSectionsMapper;
import org.epragati.vcrImage.mapper.MasterOffendingSectionsMapper;
import org.epragati.vcrImage.mapper.MasterPenalSectionsMapper;
import org.epragati.vcrImage.mapper.VcrFinalServiceMapper;
import org.epragati.vcrImage.mapper.VcrImageMapper;
import org.epragati.vcrImage.mapper.VcrImageResponseMapper;
import org.epragati.vcr_dl.vo.DlDetailsVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;

@Service
public class VcrServiceImpl implements VcrService {
	private static final Logger logger = LoggerFactory.getLogger(VcrServiceImpl.class);

	List<String> officeCodes;
	
	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;
	@Autowired
	private RegistrationDetailsMapper<?> registrationDetailsMapper;
	@Autowired
	private MasterCovDAO masterCovDAO;
	@Autowired
	private MasterCovMapper masterCovMapper;
	@Autowired
	private StateService stateService;
	@Autowired
	private DistrictDAO districtDAO;
	@Autowired
	private DistrictMapper districtMapper;
	@Autowired
	private VcrGoodsDao vcrGoodsDao;
	@Autowired
	private VcrGoodsMapper vcrGoodsMapper;
	@Autowired
	private GridFsClient gridFsClient;
	@Autowired
	private VcrImageMapper vcrImageMapper;
	@Autowired
	private VcrImageDAO vcrImageRepo;
	@Autowired
	private VcrImageResponseMapper vcrImageResponseMapper;
	@Autowired
	private VcrFinalServiceDAO finalServiceDAO;
	@Autowired
	private VcrFinalServiceMapper finalServiceMapper;
	@Autowired
	private RegistrationServiceImpl regService;
	@Autowired
	private StateDAO stateDAO;
	@Autowired
	private PermitDetailsDAO permitDetailsDAO;
	@Autowired
	private PermitDetailsMapper permitDetailsMapper;

	@Autowired
	private FcDetailsDAO fcDAO;
	@Autowired
	private FcDetailsMapper fcDetailsMapper;
	@Autowired
	private TaxDetailsDAO taxDetailsDao;
	@Autowired
	private NotificationUtil notifications;
	@Autowired
	private NotificationTemplates notification;
	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private DistrictService districtService;

	@Autowired
	private OfficeDAO officeDAO;
	@Autowired
	private RegServiceDAO regServiceDAO;
	@Autowired
	private StagingRegistrationDetailsDAO stagingRegistrationDetailsDAO;
	@Autowired
	private PaymentTransactionDAO paymentTransactionDAO;
	@Autowired
	MasterPayperiodDAO masterPayperiodDAO;
	@Autowired
	StagingDetailsMapper stagingRegDetailsMapper;

	@Autowired
	private CitizenTaxService citizenTaxService;
	@Autowired
	private PropertiesDAO propertiesDAO;
	@Autowired
	private RegistrationService registrationService;
	@Autowired
	private VoluntaryTaxDAO voluntaryTaxDAO;
	@Autowired
	private RegistratrionServicesApprovals registratrionServicesApprovals;
	@Autowired
	private OfficeMapper officeMapper;

	@Autowired
	private VCRFinalDetailsMapper vcrMapper;

	@Autowired
	private VCRFcMapper vcrFcMapper;

	@Autowired
	private VCRPermitMapper vcrPermitMapper;

	@Autowired
	private VCRTaxMapper vcrTaxMapper;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private FreezeVehiclsDAO freezeVehiclsDAO;

	@Autowired
	private RegistrationMigrationSolutionsService registrationMigrationSolutionsService;

	@Autowired
	private RegServiceMapper regServiceMapper;

	@Autowired
	private VcrDeleteDetailsDAO vcrDeleteDetailsDAO;

	@Autowired
	private RestGateWayService restGateWayService;

	@Autowired
	private MasterOffenceSectionsDAO masterOffenceSectionsDao;
	@Autowired
	private MasterOffenceSectionsMapper masterOffenceSectionsMapper;
	@Autowired
	private MasterOffendingSectionsDAO masterOffendingSectionsDAO;
	@Autowired
	private MasterOffendingSectionsMapper masterOffendingSectionsMapper;
	@Autowired
	private MasterPenalSectionsDAO masterPenalSectionsDAO;
	@Autowired
	private MasterPenalSectionsMapper masterPenalSectionsMapper;
	@Value("${reg.service.serverUrl:}")
	private String serverURL;
	@Autowired
	private PermitUtilizationMapper permitUtilizationMapper;

	@Autowired
	private SpeedGunVcrDAO speedGunVcrDAO;

	@Autowired
	private OffenceService offenceService;

	private static final String RESOURCE_NOT_FOUND = "Data is not available in database. Please  book case as in other state vehicle and you can enter data manually. ";

	static List<DistrictVO> districtsList = new ArrayList<>();
	static Map<String, String> officeNameWithCodeMap = new HashMap<>();

	@PostConstruct
	public Map<String, String> getOfficeCodesByOfficeName() {
		List<OfficeDTO> offices = officeDAO.findAll();
		offices.forEach(val -> {
			officeNameWithCodeMap.put(val.getOfficeCode(), val.getOfficeName());
		});

		return officeNameWithCodeMap;
	}

	static Map<Integer, List<String>> officeCodesMap = new HashMap<>();
	// @Value("${reg.driver.details.url:https://otsiqa.epragathi.org:9393/dl/getDldetailsByDlNo?dlNo=}")
	@Value("${dl.service.url:null}")
	private String driverDetailsUrl;

	@Value("${dl.getDldetailsByDlNo.service.url:null}")
	private String driverDetailsFromDlUrl;
	@Autowired
	private OfficeService officeService;

	@Autowired
	private PaymentReportService paymentReportService;

	@Autowired
	private ReportService qRCodeService;

	@Override
	public List<MasterCovVO> getAllCovs() {
		List<MasterCovDTO> covsList = masterCovDAO.findAll();
		if (CollectionUtils.isNotEmpty(covsList)) {
			return masterCovMapper.convertEntity(covsList);
		}
		return Collections.emptyList();
	}

	@Override
	public List<StateVO> getAllStates() {
		List<StateVO> statelist = stateService.findByCid(NationalityEnum.IND.getName());
		if (CollectionUtils.isNotEmpty(statelist)) {
			return statelist;
		}
		return Collections.emptyList();

	}

	@Override
	public Optional<RegistrationDetailsVO> getVehicleDetailsByPrNO(String prNo) throws ResourceNotFoudException {

		try {
			Optional<RegistrationDetailsDTO> registrationDetails = registrationDetailDAO.findByPrNo(prNo);
			Optional<RegistrationDetailsVO> vehicleDetails = null;
			if (registrationDetails.isPresent()) {
				logger.debug(" DATA!@@@@@ [{}]", registrationDetails.get());
				vehicleDetails = registrationDetailsMapper.convertVehicleDetails(registrationDetails.get());
			}
			return vehicleDetails;
		} catch (Exception e) {
			logger.debug("EXCEPTION IN SERVICE [{}}]", e);
			logger.error("EXCEPTION IN SERVICE [{}}]", e.getMessage());
			logger.error(RESOURCE_NOT_FOUND);
			throw new ResourceNotFoudException(RESOURCE_NOT_FOUND);
		}

	}

	@Override
	public Optional<RegistrationDetailsVO> getVehicleDetailsByChessisNo(String chessisNo)
			throws ResourceNotFoudException {
		try {
			Optional<RegistrationDetailsDTO> registrationDetails = registrationDetailDAO
					.findByVahanDetailsChassisNumber(chessisNo);
			if (registrationDetails.isPresent())
				return registrationDetailsMapper.convertVehicleDetails(registrationDetails.get());
		} catch (Exception e) {
			logger.debug("EXCEPTION IN SERVICE [{}]", e.getMessage());
			logger.debug(RESOURCE_NOT_FOUND + " [{}] ", e);
			logger.error(RESOURCE_NOT_FOUND + " [{}] ", e.getMessage());
			throw new ResourceNotFoudException(RESOURCE_NOT_FOUND);
		}
		return Optional.empty();

	}

	@Override
	public List<VcrGoodsVO> getAllGoodsDescription() {

		List<VcrGoodsDTO> goodsDetails = vcrGoodsDao.findAll();
		if (CollectionUtils.isNotEmpty(goodsDetails))
			return vcrGoodsMapper.convertEntity(goodsDetails);

		return Collections.emptyList();
	}

	@Override
	public void imageSave(VcrImageVO vcrImeageVO) throws Exception {

		List<ImageInput> imageInput = null;
		if (vcrImeageVO.getImages() != null)
			imageInput = this.getListImage(vcrImeageVO.getImages());
		else {
			logger.error("please pass image property");
			throw new Exception("please pass image property");
		}

		List<KeyValue<String, List<ImageEnclosureDTO>>> enclosure = imageSaveReq(Boolean.TRUE, imageInput,
				vcrImeageVO.getApplicationNo(), vcrImeageVO.getUploadFile(), "DONE");
		logger.debug("enclosure getting after save request [{}]", enclosure);
		VcrImageDTO vcrImageDto = vcrImageMapper.convertVoToDto(vcrImeageVO, enclosure);

		vcrImageRepo.save(vcrImageDto);

	}

	public List<KeyValue<String, List<ImageEnclosureDTO>>> imageSaveReq(boolean saveReq, List<ImageInput> imagesInput,
			String applicationNo, MultipartFile[] uploadfiles, String status) {
		logger.debug("saving Image by gridFs Client");

		try {
			if (saveReq) {
				if (uploadfiles.length > 4) {
					logger.error("Enclouser Required");
					throw new BadRequestException("Enclouser Required");
				}
				return gridFsClient.convertImages(imagesInput, applicationNo, uploadfiles, status);
			}
		} catch (IOException e) {
			logger.debug("Exception [{}]", e);
			logger.error("Bad Request Exception [{}]", e.getMessage());
			throw new BadRequestException(e.getMessage());
		}
		return Collections.emptyList();

	}

	/**
	 * this method is used to swap JSON string to java object.
	 * 
	 * @param imageProperty
	 * @return
	 */
	public List<ImageInput> getListImage(String imageProperty) {

		logger.debug("making Json String to java object getListImage() ");
		JsonElement jsonElement = new JsonParser().parse(imageProperty);
		List<ImageInput> listOfImageInput = new ArrayList<>();

		jsonElement.getAsJsonArray().forEach(json -> {
			ImageInput imageInput = null;
			List<String> listString = null;

			imageInput = new ImageInput();

			imageInput.setType(json.getAsJsonObject().get("type").getAsString());
			imageInput.setFileOrder(json.getAsJsonObject().get("fileOrder").getAsInt());
			imageInput.setPageNo(json.getAsJsonObject().get("pageNo").getAsInt());
			imageInput.setEnclosureName(json.getAsJsonObject().get("enclosureName").getAsString());

			int size = json.getAsJsonObject().get("basedOnRole").getAsJsonArray().size();

			logger.debug("IMG property size is [{}]", size);
			listString = new ArrayList<>();
			for (int index = 0; index < json.getAsJsonObject().get("basedOnRole").getAsJsonArray().size(); index++) {
				listString.add(json.getAsJsonObject().get("basedOnRole").getAsJsonArray().get(index).getAsString());
			}
			imageInput.setBasedOnRole(listString);
			json.getAsJsonObject().get("basedOnRole");
			listOfImageInput.add(imageInput);
		});
		return listOfImageInput;
	}

	/**
	 * this service is used to get image based on Image ID.
	 */
	@Override
	public String getImage(String appImageDocId) throws IOException, ResourceNotFoudException {

		Optional<GridFSDBFile> imageOptional = gridFsClient.findFilesInGridFsById(appImageDocId.trim());
		byte[] bytes64bytes;
		logger.warn(" Getting image from GridFs Client based on appImageId [{}]", imageOptional.get());

		InputStream finput = imageOptional.get().getInputStream();

		bytes64bytes = Base64.encodeBase64(IOUtils.toByteArray(finput));
		final String content = new String(bytes64bytes);
		finput.close();
		return content;
	}

	/**
	 * this service is used to find save details based on vcrNo
	 */
	@Override
	public Optional<Map<String, String>> getSaveDetailsByVcrNO(String vcrNo)
			throws IOException, ResourceNotFoudException {

		Optional<VcrFinalServiceDTO> saveDetails = finalServiceDAO.findByVcrVcrNumber(vcrNo);
		logger.debug("getting details based on vcrNo [{}]", saveDetails);
		Map<String, String> listOfKeyAndImages = new HashMap<>();
		List<KeyValue<String, List<ImageEnclosureDTO>>> listOfImages = new ArrayList<>();
		if (saveDetails.isPresent()) {

			if (saveDetails.get().getSeizedAndDocumentImpounded().getVehicleSeizedDTO() == null
					&& saveDetails.get().getOtherSections().isEmpty()) {
				logger.error("IMGS. not found with this ID:- [{}]", vcrNo, "please try with differnet ID.");
				throw new ResourceNotFoudException(
						"IMGS. not found with this ID:- (" + vcrNo + "), please try with differnet ID.");
			}
			List<OtherSectionDTO> otherSectionDetails = saveDetails.get().getOtherSections();

			if (otherSectionDetails != null && !otherSectionDetails.isEmpty()) {
				otherSectionDetails.forEach(item -> {
					if (item.getEnclosures() != null)
						listOfImages.addAll(item.getEnclosures());
				});
			}
			if (saveDetails.get().getSeizedAndDocumentImpounded().getVehicleSeizedDTO() != null
					&& saveDetails.get().getSeizedAndDocumentImpounded().getVehicleSeizedDTO().getEnclosures() != null)
				listOfImages.add(saveDetails.get().getSeizedAndDocumentImpounded().getVehicleSeizedDTO().getEnclosures()
						.stream().findFirst().get());

			logger.debug("GETTING IMG ID [{}], [{}]", vcrNo, listOfImages);
			if (listOfImages != null && !listOfImages.isEmpty()) {
				/*
				 * logger.error("IMGS. not found with this ID:- [{}]", vcrNo,
				 * " please try with differnet ID."); throw new ResourceNotFoudException(
				 * "IMGS. not found with this ID:- (" + vcrNo +
				 * "), please try with differnet ID.");
				 */

				listOfImages.forEach(imageData -> {

					Optional<ImageEnclosureDTO> optionalImage = imageData.getValue().stream().findFirst();
					if (optionalImage.isPresent()) {
						try {
							listOfKeyAndImages.put(imageData.getKey(), this.getImage(optionalImage.get().getImageId()));
						} catch (IOException e) {
							logger.debug("Error while getting the image [{}]", e);
							logger.error("Error while getting the image [{}]", e.getMessage());

							e.getMessage();
						} catch (ResourceNotFoudException e) {
							logger.debug("Error while getting the image [{}]", e);
							logger.error("Error while getting the image [{}]", e.getMessage());

							e.getMessage();
						}
					}

				});
			}
			return Optional.of(listOfKeyAndImages);
		}
		return Optional.empty();

	}

	@Override
	public VcrFinalServiceVO saveVcrDetails(String finalServiceVO, MultipartFile[] file, JwtUser jwtUser)
			throws ResourceNotFoudException, Exception {
		if (StringUtils.isBlank(finalServiceVO)) {
			logger.error("final service data is required.");
			throw new BadRequestException("final service data is required.");
		}
		Optional<VcrFinalServiceVO> inputOptional = regService.readValue(finalServiceVO, VcrFinalServiceVO.class);

		if (inputOptional.isPresent()) {
			return saveVcrData(inputOptional, jwtUser, file);
		} else {
			logger.error("please pass appropriate JSON format.");
			throw new Exception("please pass appropriate JSON format.");
		}
	}

	private List<String> getListOfStateId(Optional<VcrFinalServiceVO> vcrFinalServiceVO) {

		List<String> stateId = new ArrayList<>();

		if (vcrFinalServiceVO.isPresent()) {
			String regState = vcrFinalServiceVO.get().getRegistration().getState().getStateId().trim();
			String fromState = vcrFinalServiceVO.get().getVehicleProceeding().getFrom().getState().getStateId().trim();
			String toState = vcrFinalServiceVO.get().getVehicleProceeding().getTo().getState().getStateId().trim();
			if (StringUtils.isNotBlank(regState))
				stateId.add(regState);
			if (StringUtils.isNotBlank(fromState))
				stateId.add(fromState);
			if (StringUtils.isNotBlank(toState))
				stateId.add(toState);
		}
		logger.warn("SIZE of LIST are:  [{}]", stateId);
		return stateId;
	}

	/**
	 * send Notifications on Application Fill Success By User(Financier)
	 * 
	 * @param i
	 * @param finReqDto
	 */
	private void sendNotifications(int i, VcrFinalServiceDTO vcrDTO) {
		logger.warn("sendNotifications..");

		logger.warn("processing...... please wait");
		String mobile = vcrDTO.getOwnerDetails().getMobileNo();
		try {

			if (i == 1) {
				notifications.sendMessageNotification(notification::fillTemplate, MessageTemplate.VCR_REG.getId(),
						vcrDTO, mobile);
			} else {
				notifications.sendMessageNotification(notification::fillTemplate, MessageTemplate.EWEBVCR.getId(),
						vcrDTO, mobile);
			}
			logger.warn("MESSAGE SENT to [{}]", mobile);
		} catch (IOException e) {
			logger.warn("Message not sent with mobile Number [{}], for Vcr Number:-[{}]", mobile,
					vcrDTO.getVcr().getVcrNumber());
			logger.error(e.getMessage());
			e.printStackTrace();
		}

	}

	@Override
	public List<DistrictVO> getAllDistrict(String stateId) {
		List<DistrictDTO> dto = districtDAO.findByStateId(stateId);
		if (CollectionUtils.isNotEmpty(dto))
			return districtMapper.convertVoToDTO(dto);
		return Collections.emptyList();
	}

	@Override
	public Optional<VcrFinalServiceDTO> getVcrDetailsByVcrNumber(String vcrNumber) {
		logger.debug("DTO [{}]", vcrNumber);
		Optional<VcrFinalServiceDTO> dto = finalServiceDAO.findByVcrVcrNumber(vcrNumber);

		logger.debug("DTO [{}]", dto.get());
		return dto;
	}

	@Override
	public List<VcrFinalServiceDTO> getVcrDetailsByRegNumber(String regNumber) {

		return finalServiceDAO.findByRegistrationRegNo(regNumber);
	}

	@Override
	public List<VcrFinalServiceVO> getVcrDetailsPageableByRegNumber(String regNumber) {
		ApplicationSearchVO applicationSearchVO = new ApplicationSearchVO();
		applicationSearchVO.setPrNo(regNumber);
		applicationSearchVO.setRequestFromAO(Boolean.TRUE);
		CitizenSearchReportVO outPut = regService.applicationSearchForVcr(applicationSearchVO);
		List<VcrFinalServiceVO> vo = outPut.getVcrList();
		if (CollectionUtils.isNotEmpty(vo)) {
			RegistrationDetailsDTO regDoc = getRegDoc(regNumber);
			if (regDoc != null && regDoc.getOfficeDetails() != null) {
				OfficeVO officeVo = officeMapper.convertEntity(regDoc.getOfficeDetails());
				vo.forEach(id -> {

					id.setOfficeDetails(officeVo);

				});
			}
			return vo;
		} else {
			return Collections.emptyList();
		}

	}

	/**
	 * this service is used for REST (dl_service) call.
	 * 
	 * @throws Exception
	 */
	@Override
	public List<DlDetailsVO> getDriverDetilsByDlNo(String dlNo) throws Exception {

		RestTemplate restTemplate = new RestTemplate();

		ResponseEntity<String> result = null;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<ApplicantSearchWithOutIdInput> httpEntity = new HttpEntity<>(headers);
		logger.debug("calling RestTemplate ....");
		logger.debug("URl [{}][{}]", driverDetailsFromDlUrl, dlNo);
		result = restTemplate.exchange(driverDetailsFromDlUrl + dlNo, HttpMethod.GET, httpEntity, String.class);
		logger.debug("getting response....[{}]", result);
		return this.parseDlDetailsStringToVo(result);
	}

	@Override
	public VehicleDetailsAndPermitDetailsVO getVehicleDetailsAndPeermitDetails(String prNo)
			throws ResourceNotFoudException {
		VehicleDetailsAndPermitDetailsVO responseVo = new VehicleDetailsAndPermitDetailsVO();

		Optional<RegistrationDetailsVO> regDetailsVO = this.getVehicleDetailsByPrNO(prNo);
		Optional<PermitDetailsDTO> permitDto = permitDetailsDAO
				.findByPrNoAndPermitStatusAndPermitTypeTypeofPermitOrderByCreatedDateDesc(regDetailsVO.get().getPrNo(),
						PermitsEnum.ACTIVE.getDescription(), PermitType.PRIMARY.getPermitTypeCode());
		Optional<FcDetailsDTO> fcDetails = fcDAO.findByStatusIsTrueAndPrNoOrderByCreatedDateDesc(prNo);

		logger.debug("setting REG. Details");
		if (regDetailsVO.isPresent()) {
			responseVo.setRegDetailsVO(regDetailsVO.get());

			if (regDetailsVO.get().getApplicationNo() != null) {
				/*
				 * Optional<TaxDetailsDTO> taxDetailsDto = taxDetailsDao
				 * .findByApplicationNoOrderByCreatedDateDesc(regDetailsVO.get().
				 * getApplicationNo());
				 */
				Optional<TaxDetailsDTO> taxDetailsDto = regService
						.getLatestTaxTransaction(regDetailsVO.get().getPrNo());
				if (taxDetailsDto.isPresent())
					responseVo.setTaxDetails(taxDetailsDto.get());

			}
		}

		logger.debug("setting permit Details");
		if (permitDto.isPresent()) {
			Optional<PermitDetailsVO> permitDetailsVOs = permitDetailsMapper.convertEntityDtoToVO(permitDto);
			if (permitDetailsVOs.isPresent())
				responseVo.setPermitDetailsVO(permitDetailsVOs.get());
		}
		logger.debug("setting FC. Details");
		if (fcDetails.isPresent()) {
			responseVo.setFcDetails(fcDetailsMapper.convertFcDetailsToValidityDetails(fcDetails.get()));

		}

		return responseVo;
	}

	public List<DlDetailsVO> parseDlDetailsStringToVo(ResponseEntity<String> data) {

		List<DlDetailsVO> listDlResponse = new ArrayList<>();
		JsonElement jsonElement = new JsonParser().parse(data.getBody());
		JsonArray ar = jsonElement.getAsJsonObject().getAsJsonArray("result");
		ar.getAsJsonArray().forEach(dl -> {
			String element = dl.getAsJsonObject().toString();
			logger.debug("String element [{}]", element);
			Optional<DlDetailsVO> dlDetailsOptional = regService.readValue(element, DlDetailsVO.class);

			if (dlDetailsOptional.isPresent())
				listDlResponse.add(dlDetailsOptional.get());

		});
		if (!CollectionUtils.isEmpty(listDlResponse)) {
			logger.debug("DL DETAILS coming from DL service[{}]", listDlResponse);

		}
		return listDlResponse;

	}

	@Override
	public List<VcrFinalServiceDTO> getVcrDetailsByVcrNumberList(String vcrNumber) {
		List<VcrFinalServiceDTO> dto = finalServiceDAO.findAllByVcrVcrNumber(vcrNumber);
		if (CollectionUtils.isNotEmpty(dto))
			return dto;
		return Collections.emptyList();
	}

	private boolean compareStateID(List<StateDTO> stateDto, String stateId) {
		for (StateDTO stateDTO2 : stateDto) {
			if (stateDTO2.getStateId().equals(stateId))
				return true;
		}
		return false;
	}

	public List<VcrFinalServiceDTO> getVcrDetails(String regNo) {
		return finalServiceDAO.findByRegistrationRegNo(regNo);
	}

	@Override
	public RegReportVO vcrReport(VcrVo vo, JwtUser jwtuser, Pageable page) {
		RegReportVO reportVO = new RegReportVO();
		// vo.getVcrNumber() can be regNo or vcrNo
		if (StringUtils.isNotEmpty(vo.getVcrNumber())) {
			List<VcrFinalServiceVO> vcrDataList = new ArrayList<>();
			logger.info("vcr report based on prNo/vcrNo [{}]", vo.getVcrNumber());
			List<VcrFinalServiceDTO> vcrList = new ArrayList<>();
			Optional<VcrFinalServiceDTO> vcrOptional = finalServiceDAO.findByVcrVcrNumber(vo.getVcrNumber());
			if (vcrOptional.isPresent()) {
				vcrList = Arrays.asList(vcrOptional.get());
				// Commented search with PRNO as discussed with Aparna
				// getVcrDetails(vcrOptional.get().getRegistration().getRegNo());
			} else {
				vcrList = getVcrDetails(vo.getVcrNumber());
			}

			List<VcrFinalServiceDTO> vcrFilterList = filterVcrByStatus(vo, vcrList);
			if (CollectionUtils.isNotEmpty(vcrFilterList)) {
				vcrDataList = finalServiceMapper.convertLimited(vcrFilterList);

			}
			reportVO.setVcrReport(vcrDataList);
		} else {
			return getMviBasedVcrDetails(vo, jwtuser, page);
		}
		return reportVO;

	}

	public List<VcrFinalServiceDTO> filterVcrByStatus(VcrVo vo, List<VcrFinalServiceDTO> vcrList) {
		if (StringUtils.isNotEmpty(vo.getVcrStatus()) && vo.getVcrStatus().equals(VcrEnum.PAID.getDesc())) {
			return vcrList.stream().filter(vcr -> vcr.getIsVcrClosed() != null && vcr.getIsVcrClosed())
					.collect(Collectors.toList());
		} else if (StringUtils.isNotEmpty(vo.getVcrStatus()) && vo.getVcrStatus().equals(VcrEnum.UNPAID.getDesc())) {
			return vcrList.stream().filter(vcr -> vcr.getIsVcrClosed() != null && !vcr.getIsVcrClosed())
					.collect(Collectors.toList());

		} else {
			return vcrList;
		}
	}

	@PostConstruct
	public Map<Integer, List<String>> getOfficeCodes() {

		if (CollectionUtils.isEmpty(districtsList)) {
			districtsList = districtService.findBySid(NationalityEnum.AP.getName());
		}
		districtsList.stream().forEach(dist -> {
			List<OfficeDTO> officeList = officeDAO.findBydistrict(dist.getDistrictId());
			List<String> officeCodes = officeList.stream().map(office -> office.getOfficeCode())
					.collect(Collectors.toList());
			if (!officeCodes.isEmpty()) {
				officeCodesMap.put(dist.getDistrictId(), officeCodes);
			}
		});
		return officeCodesMap;
	}

	public RegReportVO getMviBasedVcrDetails(VcrVo vo, JwtUser jwtuser, Pageable page) {
		if (vo.getFromDate() == null && vo.getToDate() == null) {
			throw new BadRequestException("From Date / To Date is required");
		}
		RegReportVO regReport = new RegReportVO();
		List<String> distinctMVI = new ArrayList<>();
		String officeCode = jwtuser.getOfficeCode();
		if (StringUtils.isEmpty(officeCode)) {
			throw new BadRequestException("Office Code not found");
		}
		if (RoleEnum.roleList().contains(vo.getRole())) {
			if (StringUtils.isNotBlank(vo.getMviName()) && vo.getMviName().equals("ALL")) {
				Optional<OfficeDTO> officeOpt = officeDAO.findByOfficeCode(officeCode);
				if (officeOpt.isPresent()) {
					Integer distId = officeOpt.get().getDistrict();
					distinctMVI = getMVIforDistrict(distId);
				}
			} else if (StringUtils.isNotBlank(vo.getMviName())) {
				distinctMVI = Arrays.asList(vo.getMviName());
			} else {
				distinctMVI = Arrays.asList(jwtuser.getId());
			}
			regReport.setVcrReport(getVcrData(vo, distinctMVI, page));
			if (vo.getPageNo() + 1 == vo.getTotalPage()) {
				List<VcrFinalServiceDTO> vcrDataList = finalServiceDAO.findByPaidDateBetweenAndCreatedByInNative(
						getTimewithDate(vo.getFromDate(), false), getTimewithDate(vo.getToDate(), true), distinctMVI);
				List<VcrFinalServiceVO> grandTotals = finalServiceMapper.convertLimited(vcrDataList);

				Integer cftotal = 0;
				Double taxTotal = 0d;
				Double taxArrears = 0d;
				Long penalityTotal = 0l;
				Long penalityArrearsTotal = 0l;
				Long serviceFeeTotal = 0l;
				Double total = 0d;

				for (VcrFinalServiceVO grandTotal : grandTotals) {
					cftotal = cftotal + grandTotal.getCompoundFee();
					taxTotal = taxTotal + grandTotal.getTax();
					taxArrears = taxArrears + grandTotal.getTaxArrears();
					penalityTotal = penalityTotal + grandTotal.getPenalty();
					penalityArrearsTotal = penalityArrearsTotal + grandTotal.getPenaltyArrears();
					if (grandTotal.getServiceFee() != null) {
						serviceFeeTotal = serviceFeeTotal + grandTotal.getServiceFee();
					}
					total = total + grandTotal.getTotal();
				}
				regReport.setGrandTotal(total);
				regReport.setTaxTotal(taxTotal);
				regReport.setTaxArrears(taxArrears);
				regReport.setCfTotal(cftotal);
				regReport.setPenalityTotal(penalityTotal);
				regReport.setPenalityArrearsTotal(penalityArrearsTotal);
				regReport.setServiceFeeTotal(serviceFeeTotal);
			}

		} else {

			if (StringUtils.isNotBlank(vo.getMviName())) {
				if (vo.getMviName().equals("ALL")) {
					Optional<OfficeDTO> officeOpt = officeDAO.findByOfficeCode(officeCode);
					if (officeOpt.isPresent()) {
						Integer distId = officeOpt.get().getDistrict();
						distinctMVI = getMVIforDistrict(distId);
					} else
						throw new BadRequestException("No office Data found office: " + officeCode);

				} else {
					distinctMVI = Arrays.asList(vo.getMviName());
				}
			} else {
				if ((vo.getType() != null && vo.getType().equalsIgnoreCase("paidDate")
						&& StringUtils.isNotBlank(vo.getOfficeCode()) && !vo.getOfficeCode().equals("ALL")
						&& StringUtils.isNotBlank(vo.getRole()))
						&& (vo.getRole().equalsIgnoreCase(RoleEnum.DTC.getName())
								|| vo.getRole().equalsIgnoreCase(RoleEnum.STA.getName()))) {
					officeCode = vo.getOfficeCode();
				}
				Optional<OfficeDTO> officeOpt = officeDAO.findByOfficeCode(officeCode);
				if (officeOpt.isPresent()) {
					Integer distId = officeOpt.get().getDistrict();
					distinctMVI = getMVIforDistrict(distId);
				} else
					throw new BadRequestException("No office Data found office: " + officeCode);
			}
			regReport.setVcrReport(getVcrData(vo, distinctMVI, page));

		}
		regReport.setPageNo(vo.getPageNo());
		regReport.setTotalPage(vo.getTotalPage());
		List<UserVO> userVOList = mviUserIdsAndNamesMapping(distinctMVI);
		regReport.setMviUsers(userVOList);
		regReport.setMvi(distinctMVI);
		return regReport;
	}

	public List<UserVO> mviUserIdsAndNamesMapping(List<String> distinctMVI) {
		if (!distinctMVI.isEmpty()) {
			List<UserDTO> userDTOList = userDAO.nativefindUsers(distinctMVI);
			return userMapper.userLimitFiledsMapper(userDTOList);
		}
		return Collections.emptyList();

	}

	public List<VcrFinalServiceVO> getVcrData(VcrVo vo, List<String> distinctMVI, Pageable page) {
		getTimewithDate(vo.getFromDate(), false);
		Page<VcrFinalServiceDTO> vcrPage = null;
		List<VcrFinalServiceDTO> vcrList = null;

		if (StringUtils.isNotBlank(vo.getType()) && vo.getType().equalsIgnoreCase("bookedDate")
				&& StringUtils.isNotBlank(vo.getMviName()) && vo.getMviName().equalsIgnoreCase("ALL")) {
			List<UserWiseEODCount> mviWiseCount = getVcrMviWiseCount(getTimewithDate(vo.getFromDate(), false),
					getTimewithDate(vo.getToDate(), true), distinctMVI);
			if (CollectionUtils.isNotEmpty(mviWiseCount)) {
				List<VcrFinalServiceVO> finalVO = new ArrayList<>();
				VcrFinalServiceVO finalDto = new VcrFinalServiceVO();
				finalDto.setMviCounts(mviWiseCount);
				finalVO.add(finalDto);
				return finalVO;
			}
		}

		if (StringUtils.isNotBlank(vo.getType()) && vo.getType().equalsIgnoreCase("paidDate")) {
			if ((/* vo.getType()!=null && vo.getType().equalsIgnoreCase("paidDate")&& */StringUtils.isNotBlank(
					vo.getOfficeCode()) && !vo.getOfficeCode().equals("ALL") && StringUtils.isNotBlank(vo.getRole()))
					&& (vo.getRole().equalsIgnoreCase(RoleEnum.DTC.getName())
							|| vo.getRole().equalsIgnoreCase(RoleEnum.STA.getName()))) {
				vcrList = finalServiceDAO.nativePaidDateBetweenAndCreatedByIn(getTimewithDate(vo.getFromDate(), false),
						getTimewithDate(vo.getToDate(), true), distinctMVI);
			} else {
				vcrPage = finalServiceDAO.findByPaidDateBetweenAndCreatedByIn(getTimewithDate(vo.getFromDate(), false),
						getTimewithDate(vo.getToDate(), true), distinctMVI, page.previousOrFirst());
			}
		} else {
			if (StringUtils.isNotEmpty(vo.getVcrStatus()) && vo.getVcrStatus().equals(VcrEnum.PAID.getDesc())) {

				vcrPage = finalServiceDAO.nativeVcrDateOfCheckBetweenAndCreatedByInAndPaid(distinctMVI,
						getTimewithDate(vo.getFromDate(), false), getTimewithDate(vo.getToDate(), true), true,
						page.previousOrFirst());
			} else if (StringUtils.isNotEmpty(vo.getVcrStatus())
					&& vo.getVcrStatus().equals(VcrEnum.UNPAID.getDesc())) {
				vcrPage = finalServiceDAO.nativeVcrDateOfCheckBetweenAndCreatedByInAndPaid(distinctMVI,
						getTimewithDate(vo.getFromDate(), false), getTimewithDate(vo.getToDate(), true), false,
						page.previousOrFirst());
			} else {
				Pageable pageRequest = new PageRequest(page.getPageNumber(), page.getPageSize(), Sort.Direction.DESC,
						"vcr.dateOfCheck");
				vcrPage = finalServiceDAO.nativeVcrDateOfCheckBetweenAndCreatedByIn(distinctMVI,
						getTimewithDate(vo.getFromDate(), false), getTimewithDate(vo.getToDate(), true),
						pageRequest.previousOrFirst());
			}
		}

		if ((vcrPage != null && vcrPage.hasContent()) || (vcrList != null && !vcrList.isEmpty())) {
			if (vcrPage != null && vcrPage.hasContent()) {
				vcrList = vcrPage.getContent();
				vo.setPageNo(vcrPage.getNumber());
				vo.setTotalPage(vcrPage.getTotalPages());

			}
			if ((vcrList != null && !vcrList.isEmpty() && vo.getType() != null
					&& vo.getType().equalsIgnoreCase("paidDate") && StringUtils.isNotBlank(vo.getOfficeCode())
					&& !vo.getOfficeCode().equals("ALL") && StringUtils.isNotBlank(vo.getRole()))
					&& (vo.getRole().equalsIgnoreCase(RoleEnum.DTC.getName())
							|| vo.getRole().equalsIgnoreCase(RoleEnum.STA.getName()))) {
				if (vcrList != null && !vcrList.isEmpty()) {
					vcrList = vcrList.stream().filter(one -> one.getOfficeCode().equalsIgnoreCase(vo.getOfficeCode()))
							.collect(Collectors.toList());
				}
				if (vcrList != null && !vcrList.isEmpty()) {
					vo.setPageNo(0);
					Double doublePages = Math.ceil(vcrList.size() / 20f);
					vo.setTotalPage(doublePages.longValue());
				}
			}

		}
		if (CollectionUtils.isEmpty(vcrList)) {
			throw new BadRequestException(
					"No Vcr Data Found for Dates B/w " + vo.getFromDate() + " and " + vo.getToDate());
		}

		List<VcrFinalServiceDTO> vcrFilterList = filterVcrByStatus(vo, vcrList);

		if (CollectionUtils.isNotEmpty(vcrFilterList)) {
			List<VcrFinalServiceVO> vcrDataList = finalServiceMapper.convertLimited(vcrFilterList);
			vcrDataList.forEach(item -> {
				if (officeNameWithCodeMap.get(item.getOfficeCode()) != null) {
					item.setOfficeCode(officeNameWithCodeMap.get(item.getOfficeCode()));
				}
			});
			vcrDataList.sort((p1, p2) -> p1.getIssuedBy().compareTo(p2.getIssuedBy()));
			return vcrDataList;
		}
		return Collections.emptyList();
	}

	public List<String> getMVIforDistrict(Integer district) {
		officeCodes = getDistrictOffices(district);
		if (CollectionUtils.isNotEmpty(officeCodes)) {
			DBObject office = new BasicDBObject("officeCode", new BasicDBObject("$in", officeCodes));
			List<String> distinctMVI = mongoTemplate.getCollection("table_vcr_details").distinct("createdBy", office);
			return distinctMVI;
		}
		return Collections.emptyList();
	}

	public List<String> getDistrictOffices(Integer district) {
		return getOfficeCodes().get(district);

	}

	private String getRegApplicationNo(VcrFinalServiceDTO singleVcr) {

		if (singleVcr.getRegistration().isOtherState() && !singleVcr.getRegistration().isUnregisteredVehicle()) {
			List<RegServiceDTO> regServiceList = regServiceDAO.findByprNoAndServiceIdsAndSourceIsNull(
					singleVcr.getRegistration().getRegNo(), ServiceEnum.DATAENTRY.getId());
			if (regServiceList != null && !regServiceList.isEmpty()) {
				regServiceList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
				RegServiceDTO singleReg = regServiceList.stream().findFirst().get();
				if (!singleReg.getApplicationStatus().equals(StatusRegistration.APPROVED)) {
					singleReg.getApplicationNo();
				}
				List<RegServiceDTO> nocList = regServiceDAO.findByprNoAndServiceIdsAndSourceIsNull(
						singleVcr.getRegistration().getRegNo(), ServiceEnum.ISSUEOFNOC.getId());
				if (nocList != null && !nocList.isEmpty()) {
					nocList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
					RegServiceDTO latestNoc = nocList.stream().findFirst().get();
					List<RegServiceDTO> cancelationOfNocList = regServiceDAO.findByprNoAndServiceIdsAndSourceIsNull(
							singleVcr.getRegistration().getRegNo(), ServiceEnum.CANCELLATIONOFNOC.getId());
					if (cancelationOfNocList != null && !cancelationOfNocList.isEmpty()) {
						cancelationOfNocList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
						RegServiceDTO latestCancelationOfNoc = cancelationOfNocList.stream().findFirst().get();
						if (latestNoc.getCreatedDate().isAfter(latestCancelationOfNoc.getCreatedDate())) {
							// treat as no data entry
							return null;
						} else {
							// correct record
							RegistrationDetailsDTO regDoc = getRegDoc(singleVcr.getRegistration().getRegNo());
							if (regDoc != null) {
								return regDoc.getApplicationNo();
							}
						}
					} else {
						// no cancilation of noc treat as no data entry
						return null;
					}
				} else {
					// no noc correct record TODO if application at approvels or sp number pending
					RegistrationDetailsDTO regDoc = getRegDoc(singleVcr.getRegistration().getRegNo());
					if (regDoc != null) {
						return regDoc.getApplicationNo();
					}
				}
			} else {
				// no data entry
				return null;
			}
		} else if (singleVcr.getRegistration().isOtherState() && singleVcr.getRegistration().isUnregisteredVehicle()) {
			if (StringUtils.isNoneBlank(singleVcr.getRegistration().getTrNo())) {
				List<RegServiceDTO> regServiceList = regServiceDAO
						.findByRegistrationDetailsTrNoAndServiceIdsNotNull(singleVcr.getRegistration().getTrNo());
				if (regServiceList != null && !regServiceList.isEmpty()) {
					regServiceList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
					RegServiceDTO otherStateRegService = regServiceList.stream().findFirst().get();
					return otherStateRegService.getApplicationNo();
				}
			} else {
				List<RegServiceDTO> regServiceList = regServiceDAO
						.findByRegistrationDetailsVahanDetailsChassisNumberAndServiceIdsInAndSourceIsNull(
								singleVcr.getRegistration().getChassisNumber(),
								Arrays.asList(ServiceEnum.DATAENTRY.getId()));
				if (regServiceList != null && !regServiceList.isEmpty()) {
					regServiceList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
					RegServiceDTO singleReg = regServiceList.stream().findFirst().get();
					return singleReg.getApplicationNo();
				}
			}
		} else {
			if (singleVcr.getRegistration().isUnregisteredVehicle()) {

				StagingRegistrationDetailsDTO stagingDoc = getStagingDoc(singleVcr);
				if (stagingDoc != null) {
					return stagingDoc.getApplicationNo();
				} else {
					RegistrationDetailsDTO regDoc = getRegDocForFresh(singleVcr);
					if (regDoc != null) {
						singleVcr.getRegistration().setUnregisteredVehicle(Boolean.FALSE);
						singleVcr.getRegistration().setRegNo(regDoc.getPrNo());

						return regDoc.getApplicationNo();
					}
				}

			} else {

				if (StringUtils.isNotBlank(singleVcr.getRegistration().getRegNo())) {
					RegistrationDetailsDTO regDoc = getRegDoc(singleVcr.getRegistration().getRegNo());
					if (regDoc != null) {
						return regDoc.getApplicationNo();
					}
				}
			}
		}
		return null;
	}

	private RegistrationDetailsDTO getRegDoc(String prNo) {

		List<RegistrationDetailsDTO> regList = registrationDetailDAO.findAllByPrNo(prNo);

		if (regList != null && !regList.isEmpty()) {
			Optional<RegistrationDetailsDTO> dto = registrationMigrationSolutionsService
					.removeInactiveRecordsToSongle(regList);
			return dto.get();
		}
		return null;

		/*
		 * if(regList == null || !regList.isPresent()) {
		 * logger.error("No record found in reg for chassisNo :"+singleVcr.
		 * getRegistration().getChassisNumber()); throw new
		 * BadRequestException("No record found in reg for chassisNo :"+singleVcr.
		 * getRegistration().getChassisNumber()); }
		 */
		/*
		 * if (regList.isPresent()) return regList.get(); else { return null; }
		 */

	}

	private StagingRegistrationDetailsDTO getStagingDoc(VcrFinalServiceDTO singleVcr) {
		Optional<StagingRegistrationDetailsDTO> statigingDoc = stagingRegistrationDetailsDAO
				.findByVahanDetailsEngineNumberOrVahanDetailsChassisNumber(
						singleVcr.getRegistration().getEngineNumber(), singleVcr.getRegistration().getChassisNumber());
		if (statigingDoc == null || !statigingDoc.isPresent()) {
			return null;
		}
		List<PaymentTransactionDTO> paymentTransactionDTOList = paymentTransactionDAO
				.findByApplicationFormRefNumAndPayStatus(statigingDoc.get().getApplicationNo(),
						PayStatusEnum.SUCCESS.getDescription());
		if (paymentTransactionDTOList != null && !paymentTransactionDTOList.isEmpty()) {
			return statigingDoc.get();
		}
		return null;
	}

	private RegistrationDetailsDTO getRegDocForFresh(VcrFinalServiceDTO singleVcr) {
		Optional<RegistrationDetailsDTO> regList = registrationDetailDAO
				.findByVahanDetailsChassisNumber(singleVcr.getRegistration().getChassisNumber());
		try {
			if (regList != null && regList.isPresent()) {
				RegistrationDetailsDTO regDto = regList.get();
				return regDto;
			}
		} catch (Exception e) {
			logger.info("Vahan Details Not Found in the DB with ChassisNumber: {} or EngineNumber:{}",
					singleVcr.getRegistration().getChassisNumber(), singleVcr.getRegistration().getEngineNumber());
			return null;
		}
		return null;
	}

	@Override
	public Optional<RegistrationDetailsVO> getVehicleDetailsAndPermitDetailsByTrNumber(String trNo)
			throws ResourceNotFoudException {

		try {
			Optional<RegistrationDetailsDTO> registrationDetails = registrationDetailDAO.findByTrNo(trNo);
			if (registrationDetails.isPresent())
				return registrationDetailsMapper.convertVehicleDetails(registrationDetails.get());
		} catch (Exception e) {
			logger.debug("EXCEPTION IN SERVICE [{}]", e.getMessage());
			throw new ResourceNotFoudException(RESOURCE_NOT_FOUND);
		}
		return Optional.empty();
	}

	@Override
	public VehicleDetailsAndPermitDetailsVO getVehicleDetailsAndPermitDetailsByPrOrTrOrChessisNumber(String prNo,
			String chessisNumber, String trNo) throws ResourceNotFoudException {
		VehicleDetailsAndPermitDetailsVO responseVo = new VehicleDetailsAndPermitDetailsVO();
		Optional<RegistrationDetailsDTO> registrationDetails = Optional.empty();
		Optional<RegistrationDetailsVO> regDetailsVO = Optional.empty();
		try {
			if (StringUtils.isNoneBlank(prNo)) {
				try {
					registrationDetails = registrationDetailDAO.findByPrNo(prNo);
				} catch (Exception e) {
					if (!registrationDetails.isPresent()) {
						return getDetailsBasedOnPrNo(prNo);
					}
				}

			}
			if (StringUtils.isNoneBlank(trNo)) {
				registrationDetails = registrationDetailDAO.findByTrNo(trNo);
				if (!registrationDetails.isPresent()) {
					Optional<StagingRegistrationDetailsDTO> stagingRegistrationDetailswithPr = stagingRegistrationDetailsDAO
							.findByTrNo(trNo);
					// Convert staging reg Details to registrationDetails dto
					registrationDetails = stagingRegDetailsMapper.convertEntity(stagingRegistrationDetailswithPr);
					if (registrationDetails.isPresent()) {
						registrationDetails.get().setPrNo("");
					}

				}
				if (!registrationDetails.isPresent()) {
					return getDetailsBasedOnTrNo(trNo);
				}
			}
			if (StringUtils.isNoneBlank(chessisNumber)) {
				registrationDetails = registrationDetailDAO.findByVahanDetailsChassisNumber(chessisNumber);
				if (!registrationDetails.isPresent()) {

					Optional<StagingRegistrationDetailsDTO> stagingRegDetailsWithChassis = stagingRegistrationDetailsDAO
							.findByVahanDetailsChassisNumber(chessisNumber);

					// Convert staging reg Details to registrationDetails dto
					registrationDetails = stagingRegDetailsMapper.convertEntity(stagingRegDetailsWithChassis);
					if (!registrationDetails.isPresent()) {
						return getDetailsBasedOnChassisNumber(chessisNumber);
					} else {
						registrationDetails.get().setPrNo("");
					}
				}
			}
			if (registrationDetails.isPresent()) {
				Integer gvw = 0;
				if (registrationDetails.get().getVahanDetails() != null
						&& registrationDetails.get().getVahanDetails().getGvw() != null) {
					gvw = citizenTaxService.getGvwWeightForCitizen(registrationDetails.get());
				}
				registrationDetails.get().getVahanDetails().setGvw(gvw);
				Integer ulw = 0;
				if (registrationDetails.get().getVahanDetails() != null
						&& registrationDetails.get().getVahanDetails().getUnladenWeight() != null) {
					ulw = citizenTaxService.getUlwWeight(registrationDetails.get());
				}
				registrationDetails.get().getVahanDetails().setUnladenWeight(ulw);
				regDetailsVO = registrationDetailsMapper.convertVehicleDetails(registrationDetails.get());
				if (registrationDetails.get().getOfficeDetails() == null
						|| StringUtils.isBlank(registrationDetails.get().getOfficeDetails().getOfficeCode())) {
					regDetailsVO.get().setOtherState(Boolean.TRUE);
				} else {
					Optional<OfficeDTO> officeDetails = officeDAO
							.findByOfficeCode(registrationDetails.get().getOfficeDetails().getOfficeCode());
					if (officeDetails.isPresent()) {
						if (officeDetails.get().getIsActive() == null || !officeDetails.get().getIsActive()) {
							regDetailsVO.get().setOtherState(Boolean.TRUE);
						}
					} else {
						regDetailsVO.get().setOtherState(Boolean.TRUE);
					}
				}
			}
			Optional<PermitDetailsDTO> permitDto = permitDetailsDAO.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(
					regDetailsVO.get().getPrNo(), PermitType.PRIMARY.getPermitTypeCode(),
					PermitsEnum.ACTIVE.getDescription());
			Optional<FcDetailsDTO> fcDetails = null;
			if (StringUtils.isNotBlank(regDetailsVO.get().getPrNo())) {
				fcDetails = fcDAO.findByStatusIsTrueAndPrNoOrderByCreatedDateDesc(regDetailsVO.get().getPrNo());
			}

			logger.debug("setting REG. Details");
			List<String> errorMessage = new ArrayList<>();
			if (regDetailsVO.isPresent()) {

				// CHeck for theft
				Set<Integer> set = new HashSet<>();
				set.add(ServiceEnum.VCR.getId());
				regService.verifyTheftIntimation(errorMessage, prNo, set);
				if (registrationDetails.isPresent()) {
					if (registrationDetails.get().isVehicleStoppaged()) {
						DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
						errorMessage.add("Vehicle is under stoppage from :"
								+ registrationDetails.get().getStoppageDate().format(formatter));
					}
					regService.checkRcsuspendOrcancelled(registrationDetails.get(), errorMessage);
					if (registrationDetails.get().getTheftState() != null && (registrationDetails.get().getTheftState()
							.equals(StatusRegistration.TheftState.INTIMATIATED)
							|| registrationDetails.get().getTheftState()
									.equals(StatusRegistration.TheftState.OBJECTION))) {
						errorMessage.add(
								"Theft Intemated/Objected for this vehicle : " + registrationDetails.get().getPrNo());
					}
					regService.verifyNoc(errorMessage, prNo, set);
					regService.checkVcrDues(registrationDetails.get(), errorMessage);
				}

				regDetailsVO.get().setErrorMessage(errorMessage);
				responseVo.setRegDetailsVO(regDetailsVO.get());

				if (regDetailsVO.get().getApplicationNo() != null) {
					/*
					 * Optional<TaxDetailsDTO> taxDetailsDto = taxDetailsDao
					 * .findByApplicationNoOrderByCreatedDateDesc(regDetailsVO.get().
					 * getApplicationNo());
					 */
					Optional<TaxDetailsDTO> taxDetailsDto = regService
							.getLatestTaxTransaction(regDetailsVO.get().getPrNo());
					if (taxDetailsDto.isPresent())
						responseVo.setTaxDetails(taxDetailsDto.get());
					List<VcrFinalServiceDTO> listOfVcrs = finalServiceDAO
							.findByRegistrationRegApplicationNo(regDetailsVO.get().getApplicationNo());
					if (listOfVcrs != null && !listOfVcrs.isEmpty()) {
						Map<Object, List<Object>> citiesIdsToStudentsList = listOfVcrs
								.stream().flatMap(student -> student.getOffence().getOffence().stream()

										.map(state -> new AbstractMap.SimpleEntry<>(student,
												state.getOffenceDescription())))
								.collect(Collectors.groupingBy(Map.Entry::getValue,
										Collectors.mapping(Map.Entry::getKey, Collectors.toList())));
						Map<Object, Integer> finalMap = new LinkedHashMap<>();
						citiesIdsToStudentsList.forEach((k, v) -> finalMap.put(k, v.size()));
						responseVo.setOffenceListAndCount(finalMap);
					}
				}
			}

			logger.debug("setting permit Details");
			if (permitDto != null && permitDto.isPresent()) {
				Optional<PermitDetailsVO> permitDetailsVOs = permitDetailsMapper.convertEntityDtoToVO(permitDto);
				if (permitDetailsVOs.isPresent()) {
					PermitDetailsVO permitDetailsVO = permitDetailsVOs.get();
					setroutCode(regDetailsVO, permitDto, permitDetailsVO);
					responseVo.setPermitDetailsVO(permitDetailsVO);

				}
			}
			logger.debug("setting FC. Details");
			if (fcDetails != null && fcDetails.isPresent()) {
				responseVo.setFcDetails(fcDetailsMapper.convertFcDetailsToValidityDetails(fcDetails.get()));

			}

		} catch (Exception e) {
			logger.debug("EXCEPTION IN SERVICE [{}]", e.getMessage());
			throw new ResourceNotFoudException(RESOURCE_NOT_FOUND);
		}
		if (registrationDetails.isPresent() && regDetailsVO.isPresent() && StatusRegistration.TRGENERATED
				.getDescription().equalsIgnoreCase(registrationDetails.get().getApplicationStatus())) {

			regDetailsVO.get().setPrNo(StringUtils.EMPTY);
			responseVo.setRegDetailsVO(regDetailsVO.get());
		}

		return responseVo;
	}

	private void setroutCode(Optional<RegistrationDetailsVO> regDetailsVO, Optional<PermitDetailsDTO> permitDto,
			PermitDetailsVO permitDetailsVO) {
		if (regDetailsVO != null && StringUtils.isNoneBlank(regDetailsVO.get().getClassOfVehicle())
				&& (regDetailsVO.get().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.COCT.getCovCode())
						|| regDetailsVO.get().getClassOfVehicle()
								.equalsIgnoreCase(ClassOfVehicleEnum.TOVT.getCovCode()))) {
			StringBuilder sb = new StringBuilder();
			boolean flag = false;
			if (permitDto.get().getRouteDetails() != null && permitDto.get().getRouteDetails().getRouteType() != null
					&& StringUtils.isNoneBlank(permitDto.get().getRouteDetails().getRouteType().getRouteCode())) {
				if (permitDto.get().getRouteDetails().getRouteType().getRouteCode().equalsIgnoreCase("O")) {
					flag = true;
					sb.append("One District");
					if (StringUtils.isNoneBlank(permitDto.get().getRouteDetails().getValidFromRoute())) {
						sb.append("(" + permitDto.get().getRouteDetails().getValidFromRoute() + ")");
					}
				} else if (permitDto.get().getRouteDetails().getRouteType().getRouteCode().equalsIgnoreCase("T")) {
					// "TWO District"
					flag = true;
					sb.append("TWO District");
					sb.append("(");
					if (StringUtils.isNoneBlank(permitDto.get().getRouteDetails().getValidFromRoute())) {
						sb.append(permitDto.get().getRouteDetails().getValidFromRoute());
					}
					if (StringUtils.isNoneBlank(permitDto.get().getRouteDetails().getValidToRoute())) {
						sb.append(" - " + permitDto.get().getRouteDetails().getValidToRoute());
					}
					sb.append(")");
				} else if (permitDto.get().getRouteDetails().getRouteType().getRouteCode().equalsIgnoreCase("S")) {
					// "State District"
					flag = true;
					sb.append("State");
					sb.append("(");
					if (StringUtils.isNoneBlank(permitDto.get().getRouteDetails().getValidFromRoute())) {
						sb.append(permitDto.get().getRouteDetails().getValidFromRoute());
					}
					if (StringUtils.isNoneBlank(permitDto.get().getRouteDetails().getValidToRoute())) {
						sb.append(" - " + permitDto.get().getRouteDetails().getValidToRoute());
					}
					sb.append(")");

				} else if (permitDto.get().getRouteDetails().getRouteType().getRouteCode().equalsIgnoreCase("A")) {
					// "All India"
					flag = true;
					sb.append("All India");
					sb.append("(");
					if (StringUtils.isNoneBlank(permitDto.get().getRouteDetails().getValidFromRoute())) {
						sb.append(permitDto.get().getRouteDetails().getValidFromRoute());
					}
					if (StringUtils.isNoneBlank(permitDto.get().getRouteDetails().getValidToRoute())) {
						sb.append(" - " + permitDto.get().getRouteDetails().getValidToRoute());
					}
					sb.append(")");
				}
			}
			if (permitDetailsVO != null && permitDetailsVO.getRouteDetailsVO() != null
					&& permitDetailsVO.getRouteDetailsVO().getPermitRouteDetails() != null && flag) {
				permitDetailsVO.getRouteDetailsVO().getPermitRouteDetails().setDescription(sb.toString());
			}
		}

	}

	@Override
	public LocalDateTime getTimewithDate(LocalDate date, Boolean timeZone) {

		String dateVal = date + "T00:00:00.000Z";
		if (timeZone) {
			dateVal = date + "T23:59:59.999Z";
		}
		ZonedDateTime zdt = ZonedDateTime.parse(dateVal);
		return zdt.toLocalDateTime();
	}

	@Override
	public String getOfficeNameBasedOnOfficeCode(String officeCode) {
		Optional<OfficeDTO> officeDto = Optional.empty();
		String officeName = "";
		try {
			officeDto = officeDAO.findByOfficeCode(officeCode);
			if (officeDto.isPresent()) {
				officeName = officeDto.get().getOfficeName();
			} else {
				logger.info("Office Code Does not exist:" + officeCode);
				throw new BadRequestException("Office Code Does not exist" + officeCode);
			}

		} catch (Exception e) {
			logger.error("Problem While Getting OfficeName with Office Code:{}", officeCode);
		}
		return officeName;
	}

	@Override
	public List<VcrFinalServiceVO> getVcrDetailsByPrOrTrOrChessisNumberOrVcrNumberAndUserName(String prNo,
			String chessisNumber, String trNo, String vcrNumber, String username) {
		List<VcrFinalServiceDTO> VcrFinalServiceDTOs = new ArrayList<>();
		List<VcrFinalServiceVO> vcrFinalServiceVOs = new ArrayList<>();
		if (prNo != "") {
			// VcrFinalServiceDTOs =
			// finalServiceDAO.findFirst10ByRegistrationRegNoAndCreatedByOrderByCreatedDateDesc(prNo,
			// username);
			VcrFinalServiceDTOs = finalServiceDAO.findFirst10ByRegistrationRegNoAndCreatedByOrderByCreatedDateDesc(prNo,
					username);
		}
		if (trNo != "") {
			VcrFinalServiceDTOs = finalServiceDAO.findFirst10ByRegistrationTrNoAndCreatedByOrderByCreatedDateDesc(trNo,
					username);
		}
		if (chessisNumber != "") {
			VcrFinalServiceDTOs = finalServiceDAO
					.findFirst10ByRegistrationChassisNumberAndCreatedByOrderByCreatedDateDesc(chessisNumber, username);
		}
		if (vcrNumber != "") {
			VcrFinalServiceDTOs = finalServiceDAO.findFirst10ByVcrVcrNumberAndCreatedByOrderByCreatedDateDesc(vcrNumber,
					username);
		}

		if (VcrFinalServiceDTOs.isEmpty()) {
			logger.info("VCR Details Not Found for this User:{}", username);
			throw new BadRequestException("VCR Details Not Found for this User:" + username);

		} else {
			vcrFinalServiceVOs = finalServiceMapper.convertEntity(VcrFinalServiceDTOs);
		}

		return vcrFinalServiceVOs;
	}

	@Override
	public Boolean payPeriodBasedonCOV(String cov, String seatingCapacity, String gvw) {
		Optional<MasterPayperiodDTO> Payperiod = masterPayperiodDAO.findByCovcode(cov);

		if (!Payperiod.isPresent()) {
			// throw error message
			logger.error("No record found in master_payperiod for:[{}] " + cov);
			throw new BadRequestException("No record found in master_payperiod for: " + cov);

		}

		if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.BOTH.getCode())) {
			if (Payperiod.get().getCovcode().equalsIgnoreCase(ClassOfVehicleEnum.OBPN.getCovCode())) {
				if (Integer.parseInt(seatingCapacity) > 10) {
					Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
				} else {

					Payperiod.get().setPayperiod(TaxTypeEnum.LifeTax.getCode());
				}
			} else if (Payperiod.get().getCovcode().equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())) {
				if (Integer.parseInt(seatingCapacity) <= 4) {

					Payperiod.get().setPayperiod(TaxTypeEnum.LifeTax.getCode());
				} else {
					Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
				}
			} else if (Payperiod.get().getCovcode().equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode())
					|| Payperiod.get().getCovcode().equalsIgnoreCase(ClassOfVehicleEnum.GCRT.getCovCode())) {
				if (Integer.parseInt(gvw) <= 3000) {

					Payperiod.get().setPayperiod(TaxTypeEnum.LifeTax.getCode());
				} else {
					Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
				}
			}

		}

		if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.LifeTax.getCode())) {
			return true;
		}
		return false;

	}

	@Override
	public RegistrationDetailsVO getDataForFromStaging(VcrInputVo vcrInputVo) {
		Optional<StagingRegistrationDetailsDTO> staging = null;
		String no = "";
		if (StringUtils.isNoneBlank(vcrInputVo.getTrNo())) {
			staging = stagingRegistrationDetailsDAO.findByTrNo(vcrInputVo.getTrNo());
			no = vcrInputVo.getTrNo();
		} else if (StringUtils.isNoneBlank(vcrInputVo.getChassisNo())) {
			staging = stagingRegistrationDetailsDAO.findByVahanDetailsChassisNumber(vcrInputVo.getChassisNo());
			no = vcrInputVo.getChassisNo();
		} else {
			logger.error("Please provide trno/chassisno for voluntary tax");
			throw new BadRequestException("Please provide trno/chassisno for voluntary tax");
		}

		if (staging == null || !staging.isPresent()) {
			if (StringUtils.isNoneBlank(vcrInputVo.getTrNo())) {
				Optional<RegistrationDetailsDTO> regDetails = registrationDetailDAO.findByTrNo(vcrInputVo.getTrNo());
				if (regDetails != null && regDetails.isPresent()) {
					logger.error("Permanent registration done for this vehicle. vehicle number is: "
							+ regDetails.get().getPrNo());
					throw new BadRequestException("Permanent registration done for this vehicle. vehicle number is: "
							+ regDetails.get().getPrNo());
				} else {
					logger.error("No record found for: " + no);
					throw new BadRequestException("No record found for: " + no);
				}
			} else if (StringUtils.isNoneBlank(vcrInputVo.getChassisNo())) {
				Optional<RegistrationDetailsDTO> regDetails = registrationDetailDAO
						.findByVahanDetailsChassisNumber(vcrInputVo.getChassisNo());
				if (regDetails != null && regDetails.isPresent()) {
					logger.error("Permanent registration done for this vehicle. vehicle number is: "
							+ regDetails.get().getPrNo());
					throw new BadRequestException("Permanent registration done for this vehicle. vehicle number is: "
							+ regDetails.get().getPrNo());
				} else {
					logger.error("No record found for: " + no);
					throw new BadRequestException("No record found for: " + no);
				}
			}
		}
		List<PaymentTransactionDTO> paymentTransactionDTOList = paymentTransactionDAO
				.findByApplicationFormRefNumAndPayStatus(staging.get().getApplicationNo(),
						PayStatusEnum.SUCCESS.getDescription());
		if (paymentTransactionDTOList == null || paymentTransactionDTOList.isEmpty()) {
			logger.error("Temporary registration not done for this vehicle. Please raise VCR for this vehicle: " + no);
			throw new BadRequestException(
					"Temporary registration not done for this vehicle. Please raise VCR for this vehicle: " + no);
		}
		if (!(staging.get().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
				|| staging.get().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode()))) {
			logger.error(
					"Only chassis vehicle can pay voluntary tax. Please raise VCR for this vehicle if tax not paid upto date : "
							+ no);
			throw new BadRequestException(
					"Only chassis vehicle can pay voluntary tax. Please raise VCR for this vehicle if tax not paid upto date: "
							+ no);
		}
		Optional<VoluntaryTaxDTO> voluntaryTax = voluntaryTaxDAO
				.findByTrNoOrderByCreatedDateDesc(staging.get().getTrNo());
		if (voluntaryTax.isPresent()) {
			logger.error("Process completed for this vehicle. Tr number is : " + staging.get().getTrNo());
			throw new BadRequestException(
					"Process completed for this vehicle. Tr number is : " + staging.get().getTrNo());
		}
		return registrationDetailsMapper.convertEntity(staging.get());

	}

	@Override
	public List<String> getTaxTypeForVoluntaryTax(String cov, String seatingCapacity, Integer gvw, boolean nocIssued,
			boolean withTP, boolean home, boolean otherStateRegister, boolean vehicleHaveAitp) {
		List<String> list = new ArrayList<>();
		if (StringUtils.isBlank(cov) || StringUtils.isBlank(seatingCapacity) || gvw == null) {
			logger.error("Please provide cov/seatingCapacity/gvw for voluntary tax type");
			throw new BadRequestException("Please provide cov/seatingCapacity/gvw for voluntary tax type");
		}
		if (cov.equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())
				|| (cov.equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode()) && gvw <= 3000)
				|| cov.equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())
				|| cov.equalsIgnoreCase(ClassOfVehicleEnum.TRTT.getCovCode())
				|| cov.equalsIgnoreCase(ClassOfVehicleEnum.TRCN.getCovCode())) {
			logger.error("tax exemption in AP for this class of vehicle: " + cov);
			throw new BadRequestException("tax exemption in AP for this class of vehicle: " + cov);
		}
		Optional<MasterPayperiodDTO> Payperiod = masterPayperiodDAO.findByCovcode(cov);
		if (!Payperiod.isPresent()) {
			logger.error("No record found in pay period for:[{}] " + cov);
			throw new BadRequestException("No record found in master_payperiod for class of vehicle: " + cov);
		}
		MasterCovDTO masterCovDto = masterCovDAO.findByCovcode(cov);
		if (masterCovDto == null) {
			logger.error("No record found master class of vehicle for:[{}] " + cov);
			throw new BadRequestException("No record found master class of vehicle for: " + cov);
		}
		if (masterCovDto.isConstructionVehicle() != null && masterCovDto.isConstructionVehicle() && otherStateRegister
				&& !nocIssued) {
			list.add(TaxTypeEnum.VoluntaryTaxType.OneYear.getDesc());
			list.add(TaxTypeEnum.VoluntaryTaxType.LifeTax.getDesc());
			return list;
		}
		/*
		 * if(!nocIssued &&
		 * (cov.equalsIgnoreCase(ClassOfVehicleEnum.MAXT.getCovCode())||cov.
		 * equalsIgnoreCase(ClassOfVehicleEnum.MTLT.getCovCode()))) { if(withTP) {
		 * list.add(TaxTypeEnum.VoluntaryTaxType.SevenDays.getDesc()); }else {
		 * list.add(TaxTypeEnum.VoluntaryTaxType.Quarterly.getDesc()); }
		 * 
		 * return list; }
		 */
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
		Optional<PropertiesDTO> otherStateQutTax = propertiesDAO
				.findByOtherStateQutTaxCovsInAndOtherStateQutTaxCovsflagTrue(cov);
		if (otherStateQutTax.isPresent()) {
			list.add(TaxTypeEnum.VoluntaryTaxType.Quarterly.getDesc());
			return list;
		}
		if (!nocIssued && !home && !vehicleHaveAitp
				&& (cov.equalsIgnoreCase(ClassOfVehicleEnum.COCT.getCovCode())
						|| cov.equalsIgnoreCase(ClassOfVehicleEnum.TOVT.getCovCode())
						|| cov.equalsIgnoreCase(ClassOfVehicleEnum.MAXT.getCovCode())
						|| cov.equalsIgnoreCase(ClassOfVehicleEnum.MTLT.getCovCode()))) {
			list.add(TaxTypeEnum.VoluntaryTaxType.SevenDays.getDesc());
			return list;
		} else if (!nocIssued && !home && vehicleHaveAitp
				&& (cov.equalsIgnoreCase(ClassOfVehicleEnum.COCT.getCovCode())
						|| cov.equalsIgnoreCase(ClassOfVehicleEnum.TOVT.getCovCode())
						|| cov.equalsIgnoreCase(ClassOfVehicleEnum.MAXT.getCovCode())
						|| cov.equalsIgnoreCase(ClassOfVehicleEnum.MTLT.getCovCode()))) {
			list.add(TaxTypeEnum.VoluntaryTaxType.Quarterly.getDesc());
			if (quaterOne.contains(LocalDate.now().getMonthValue())) {
				list.add(TaxTypeEnum.VoluntaryTaxType.Halfyearly.getDesc());
				list.add(TaxTypeEnum.VoluntaryTaxType.Annual.getDesc());
			} else if (quaterThree.contains(LocalDate.now().getMonthValue())) {
				// return Q ,h
				list.add(TaxTypeEnum.VoluntaryTaxType.Halfyearly.getDesc());
			}
			return list;
		}
		citizenTaxService.getPayPeroidForBoth(Payperiod, seatingCapacity, gvw);
		if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.LifeTax.getCode()) && !nocIssued && !home
				&& cov.equalsIgnoreCase(ClassOfVehicleEnum.GCRT.getCovCode())) {
			Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
		}
		if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.LifeTax.getCode())) {
			Optional<PropertiesDTO> payPeriod = propertiesDAO
					.findByPayperiodAndVoluntaryPayPeriodTrue(TaxTypeEnum.LifeTax.getCode());
			if (!home && !otherStateRegister) {
				list.add(TaxTypeEnum.VoluntaryTaxType.LifeTax.getDesc());
				return list;
			}
			list.addAll(payPeriod.get().getTaxType().keySet());
			if (nocIssued || home) {
				list.remove(TaxTypeEnum.VoluntaryTaxType.BorderTax.getDesc());
			} else {
				list.remove(TaxTypeEnum.VoluntaryTaxType.LifeTax.getDesc());
			}
		} else {
			Optional<PropertiesDTO> payPeriod = propertiesDAO
					.findByPayperiodAndVoluntaryPayPeriodTrue(TaxTypeEnum.QuarterlyTax.getCode());
			Set<String> allList = payPeriod.get().getTaxType().keySet();

			if (nocIssued || (!home && !otherStateRegister)) {
				allList.remove(TaxTypeEnum.VoluntaryTaxType.SevenDays.getDesc());
				allList.remove(TaxTypeEnum.VoluntaryTaxType.ThirtyDays.getDesc());
				// allList.remove(TaxTypeEnum.VoluntaryTaxType.FifteenDays.getDesc());
				allList.remove(TaxTypeEnum.VoluntaryTaxType.BorderTax.getDesc());
				if (quaterTwo.contains(LocalDate.now().getMonthValue())) {
					allList.remove(TaxTypeEnum.VoluntaryTaxType.Halfyearly.getDesc());
					allList.remove(TaxTypeEnum.VoluntaryTaxType.Annual.getDesc());
				} else if (quaterThree.contains(LocalDate.now().getMonthValue())) {
					allList.remove(TaxTypeEnum.VoluntaryTaxType.Annual.getDesc());
				} else if (quaterFour.contains(LocalDate.now().getMonthValue())) {
					allList.remove(TaxTypeEnum.VoluntaryTaxType.Halfyearly.getDesc());
					allList.remove(TaxTypeEnum.VoluntaryTaxType.Annual.getDesc());
				}
			} else {
				if (home) {
					list.add(TaxTypeEnum.VoluntaryTaxType.Quarterly.getDesc());
					return list;
				} else {
					allList.remove(TaxTypeEnum.VoluntaryTaxType.Halfyearly.getDesc());
					allList.remove(TaxTypeEnum.VoluntaryTaxType.Annual.getDesc());
					allList.remove(TaxTypeEnum.VoluntaryTaxType.Quarterly.getDesc());
				}
			}
			list.addAll(allList);
		}
		return list;

	}

	@Override
	public LocalDate getVoluntaryTaxValidity(String taxType, LocalDate prGenerationDate) {
		LocalDate validity = null;
		if (StringUtils.isBlank(taxType)) {
			logger.error("Please select tax type");
			throw new BadRequestException("Please select tax type");
		}
		if (taxType.equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.Quarterly.getDesc())) {
			validity = citizenTaxService.validity(TaxTypeEnum.QuarterlyTax.getDesc());
		} else if (taxType.equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.Halfyearly.getDesc())) {
			validity = citizenTaxService.validity(TaxTypeEnum.QuarterlyTax.getDesc());
			validity = (validity.plusMonths(3));
		} else if (taxType.equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.Annual.getDesc())) {
			validity = citizenTaxService.validity(TaxTypeEnum.QuarterlyTax.getDesc());
			validity = (validity.plusMonths(9));
		} else if (taxType.equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.SevenDays.getDesc())) {
			validity = LocalDate.now().minusDays(1).plusDays(TaxTypeEnum.VoluntaryTaxType.SevenDays.getDays());
		} /*
			 * else if
			 * (taxType.equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.FifteenDays.getDesc())
			 * ) { validity =
			 * LocalDate.now().minusDays(1).plusDays(TaxTypeEnum.VoluntaryTaxType.
			 * FifteenDays.getDays()); }
			 */ else if (taxType.equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.ThirtyDays.getDesc())) {
			validity = LocalDate.now().minusDays(1).plusDays(TaxTypeEnum.VoluntaryTaxType.ThirtyDays.getDays());
		} else if (taxType.equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.BorderTax.getDesc())) {
			validity = LocalDate.now().plusDays(TaxTypeEnum.VoluntaryTaxType.BorderTax.getDays());
		} else if (taxType.equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.LifeTax.getDesc())) {
			if (prGenerationDate == null) {
				logger.error("pr generation date missing for life tax validity calculation");
				throw new BadRequestException("pr generation date missing for life tax validity calculation");
			}
			// prGenerationDate.
			validity = prGenerationDate.minusDays(1).plusYears(12);
		} else if (taxType.equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.OneYear.getDesc())) {
			validity = LocalDate.now().plusDays(TaxTypeEnum.VoluntaryTaxType.OneYear.getDays());
		}

		return validity;

	}

	@Override
	public RegServiceVO saveVoluntaryTax(String voluntaryTaxVO, MultipartFile[] uploadfiles, UserDTO user,
			HttpServletRequest request) {

		return registrationService.saveVoluntaryTax(voluntaryTaxVO, uploadfiles, user, request, Boolean.TRUE);
	}

	private VehicleDetailsAndPermitDetailsVO getDetailsBasedOnChassisNumber(String chassisNumber) {

		List<VcrFinalServiceDTO> dto = finalServiceDAO
				.findFirst2ByRegistrationChassisNumberOrderByCreatedDateDesc(chassisNumber);

		if (CollectionUtils.isNotEmpty(dto)) {
			return mapvcrDetails(dto);
		}
		return null;
	}

	private VehicleDetailsAndPermitDetailsVO getDetailsBasedOnTrNo(String trNo) {

		List<VcrFinalServiceDTO> dto = finalServiceDAO.findFirst2ByRegistrationTrNoOrderByCreatedDateDesc(trNo);

		if (CollectionUtils.isNotEmpty(dto)) {
			return mapvcrDetails(dto);
		}
		return null;
	}

	private VehicleDetailsAndPermitDetailsVO getDetailsBasedOnPrNo(String prNo) {

		List<VcrFinalServiceDTO> dto = finalServiceDAO.findFirst2ByRegistrationRegNoOrderByCreatedDateDesc(prNo);

		if (CollectionUtils.isNotEmpty(dto)) {
			return mapvcrDetails(dto);
		}
		return null;
	}

	private VehicleDetailsAndPermitDetailsVO mapvcrDetails(List<VcrFinalServiceDTO> dto) {
		VehicleDetailsAndPermitDetailsVO responseVo = new VehicleDetailsAndPermitDetailsVO();
		VcrFinalServiceDTO vcrDetails = dto.stream().findFirst().get();
		responseVo.setRegDetailsVO(vcrMapper.convertEntity(vcrDetails));
		responseVo.setFcDetails(vcrFcMapper.convertEntity(vcrDetails));
		responseVo.setPermitDetailsVO(vcrPermitMapper.convertEntity(vcrDetails));
		responseVo.setTaxDetails(vcrTaxMapper.convertEntity(vcrDetails));
		return responseVo;
	}

	/*
	 * @Override public VcrFinalServiceVO amountForPreview(VcrFinalServiceVO vo) {
	 * if (vo != null) { VcrFinalServiceDTO dto = finalServiceMapper.convertVO(vo);
	 * String regApplicationNo = this.getRegApplicationNo(dto);
	 * dto.getRegistration().setRegApplicationNo(regApplicationNo);
	 * List<VcrFinalServiceDTO> vcrList = new ArrayList<>(); ApplicationSearchVO
	 * applicationSearchVO = new ApplicationSearchVO();
	 * applicationSearchVO.setChassisNo(dto.getRegistration().getChassisNumber());
	 * applicationSearchVO.setRequestFromAO(Boolean.TRUE);
	 * dto.setSaveDoc(Boolean.TRUE); CitizenSearchReportVO outPut = new
	 * CitizenSearchReportVO(); vcrList =
	 * regService.getTotalVcrs(applicationSearchVO, vcrList); if (vcrList != null &&
	 * !vcrList.isEmpty()) { vcrList = regService
	 * .getVcrDetails(Arrays.asList(vcrList.stream().findFirst().get().getVcr().
	 * getVcrNumber()), true); vcrList.add(dto); } else { vcrList.add(dto); }
	 * regService.getVcrAmount(applicationSearchVO, vcrList, outPut);
	 * List<VcrFinalServiceVO> listvo = outPut.getVcrList(); VcrFinalServiceVO vcrvo
	 * = new VcrFinalServiceVO(); for (VcrFinalServiceVO vcrVo : listvo) { if
	 * (vcrVo.getSaveDoc() != null && vcrVo.getSaveDoc()) {
	 * dto.setOffencetotal(vcrVo.getOffencetotal()); vcrvo = vcrVo; } }
	 * dto.setSaveDoc(null); return vcrvo; } else {
	 * logger.error("please pass appropriate JSON format."); throw new
	 * BadRequestException("please pass appropriate JSON format."); } }
	 */

	@Override
	public String saveVehiclesFreeze(String prNo, JwtUser jwtuser, boolean freezeVehicle) {
		if (StringUtils.isBlank(prNo)) {
			logger.error("please provide pr number.");
			throw new BadRequestException("please provide pr number.");
		}
		FreezeVehiclsDTO dto = freezeVehiclsDAO.findByPrNoIn(prNo);
		if (freezeVehicle) {
			if (dto != null) {
				logger.error("Vehicle freezed by " + dto.getUserId());
				throw new BadRequestException("Vehicle freezed by " + dto.getUserId());
			}
			FreezeVehiclsDTO userBasedFreeze = freezeVehiclsDAO.findByUserId(jwtuser.getUsername());
			if (userBasedFreeze != null) {
				userBasedFreeze.getPrNo().add(prNo);
				freezeVehiclsDAO.save(userBasedFreeze);
			} else {
				FreezeVehiclsDTO freezeDto = new FreezeVehiclsDTO();
				freezeDto.setUserId(jwtuser.getUsername());
				freezeDto.setPrNo(Arrays.asList(prNo));
				freezeVehiclsDAO.save(freezeDto);
			}
		} else {
			if (dto == null) {
				logger.error("Vehicle not freezed");
				throw new BadRequestException("Vehicle not freezed");
			}
			if (!dto.getUserId().equalsIgnoreCase(jwtuser.getUsername())) {
				logger.error("Vehicle freezed by " + dto.getUserId());
				throw new BadRequestException("Vehicle freezed by " + dto.getUserId());
			}
			FreezeVehiclsDTO userBasedFreeze = freezeVehiclsDAO.findByUserId(jwtuser.getUsername());
			userBasedFreeze.getPrNo().remove(prNo);
			freezeVehiclsDAO.save(userBasedFreeze);
		}
		return "SUCCESS";
	}

	@Override
	public List<String> getVehiclesFreeze(JwtUser jwtuser) {
		FreezeVehiclsDTO userBasedFreeze = freezeVehiclsDAO.findByUserId(jwtuser.getUsername());
		if (userBasedFreeze != null) {
			return userBasedFreeze.getPrNo();
		}
		return null;
	}

	@Override
	public RegServiceVO getRegServicesForApp(RcValidationVO rcValidationVO) {
		if (rcValidationVO.getServiceIds() == null || rcValidationVO.getServiceIds().isEmpty()) {
			logger.error("please provide Service type");
			throw new BadRequestException("please provide Service type");
		}
		List<ServiceEnum> serviceEnums = rcValidationVO.getServiceIds().stream().map(id -> {

			if (id == null) {
				logger.error("ServiceId is null ");
				throw new BadRequestException("Service Id should not be empty");
			}
			ServiceEnum se = ServiceEnum.getServiceEnumById(id);
			if (se == null) {
				logger.error("Servie is null when getByServiceEnumById : [{}]", id);
				throw new BadRequestException("Invalid Service Id + " + id);
			}
			return se;
		}).collect(Collectors.toList());
		List<RegServiceDTO> listofDto = null;
		if (StringUtils.isNoneBlank(rcValidationVO.getPrNo())) {
			listofDto = regServiceDAO.findByPrNoAndServiceTypeIn(rcValidationVO.getPrNo(), serviceEnums);
		} else if (StringUtils.isNoneBlank(rcValidationVO.getTrNo())) {
			listofDto = regServiceDAO.findByTrNoAndServiceTypeIn(rcValidationVO.getTrNo(), serviceEnums);
		} else if (StringUtils.isNoneBlank(rcValidationVO.getChassisNo())) {
			List<Integer> list = new ArrayList<Integer>(rcValidationVO.getServiceIds());
			listofDto = regServiceDAO.findByRegistrationDetailsVahanDetailsChassisNumberAndServiceIdsInAndSourceIsNull(
					rcValidationVO.getTrNo(), list);
		} else {
			logger.error("please provide trNo/prNo/chassisNo");
			throw new BadRequestException("please provide trNo/prNo/chassisNo");
		}
		if (listofDto == null || listofDto.isEmpty()) {
			logger.error("No record found");
			throw new BadRequestException("No record found");
		}

		listofDto.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		return regServiceMapper.convertEntity(listofDto.stream().findFirst().get());
	}

	@Override
	public VcrFinalServiceVO saveVcrDeleteDetails(String finalServiceVO, MultipartFile[] file, JwtUser jwtUser)
			throws Exception {
		if (StringUtils.isBlank(finalServiceVO)) {
			logger.error("final service data is required.");
			throw new BadRequestException("final service data is required.");
		}
		Optional<VcrFinalServiceVO> inputOptional = regService.readValue(finalServiceVO, VcrFinalServiceVO.class);

		if (inputOptional.isPresent()) {
			VcrDeleteDetailsDTO deleteDto = finalServiceMapper.convertVOTODelete(inputOptional.get());
			if (!deleteDto.isDeleteVcrConfirmation()) {
				if (StringUtils.isNoneBlank(deleteDto.getRegistration().getRegNo())) {
					List<VcrFinalServiceDTO> listOfVcrWithPr = finalServiceDAO
							.findFirst10ByCreatedByAndRegistrationRegNoOrderByCreatedDateDesc(jwtUser.getUsername(),
									deleteDto.getRegistration().getRegNo());
					if (this.vacSavedOrNot(listOfVcrWithPr, inputOptional)) {
						return null;
					}
				} else if (StringUtils.isNoneBlank(deleteDto.getRegistration().getTrNo())) {
					List<VcrFinalServiceDTO> listOfVcrWithPr = finalServiceDAO
							.findFirst10ByCreatedByAndRegistrationTrNoOrderByCreatedDateDesc(jwtUser.getUsername(),
									deleteDto.getRegistration().getTrNo());
					if (this.vacSavedOrNot(listOfVcrWithPr, inputOptional)) {
						return null;
					}
				} else {
					List<VcrFinalServiceDTO> listOfVcrWithPr = finalServiceDAO
							.findFirst10ByCreatedByAndRegistrationChassisNumberOrderByCreatedDateDesc(
									jwtUser.getUsername(), deleteDto.getRegistration().getChassisNumber());
					if (this.vacSavedOrNot(listOfVcrWithPr, inputOptional)) {
						return null;
					}
				}
			}
			deleteDto.setCreatedBy(jwtUser.getUsername());
			deleteDto.setCreatedDate(LocalDateTime.now());
			deleteDto.setlUpdate(LocalDateTime.now());
			vcrDeleteDetailsDAO.save(deleteDto);
			return inputOptional.get();

		} else {
			logger.error("please pass appropriate JSON format.");
			throw new Exception("please pass appropriate JSON format.");
		}
	}

	private boolean vacSavedOrNot(List<VcrFinalServiceDTO> listOfVcrWithPr, Optional<VcrFinalServiceVO> inputOptional) {
		if (listOfVcrWithPr != null && listOfVcrWithPr.isEmpty()) {
			VcrFinalServiceDTO latest = listOfVcrWithPr.stream().findFirst().get();
			if (latest.getCreatedDate().toLocalDate().equals(LocalDate.now())
					&& latest.getCreatedDate().getHour() == LocalDateTime.now().getHour()
					&& (latest.getCreatedDate().getMinute() + 15) > LocalDateTime.now().getMinute()) {
				for (OffenceVO offence : inputOptional.get().getOffence().getOffence()) {
					// inputOptional.get().getOffence().getOffence().forEach(offence ->
					if (latest.getOffence().getOffence().stream().anyMatch(
							id -> id.getOffenceDescription().equalsIgnoreCase(offence.getOffenceDescription()))) {
						return Boolean.TRUE;
					}
				}

			}
		}
		return Boolean.FALSE;
	}

	@Override
	public CitizenSearchReportVO periodOfTimeVcrList(ApplicationSearchVO vo, JwtUser jwtUser) {
		CitizenSearchReportVO vcrList = new CitizenSearchReportVO();
		;

		List<VcrFinalServiceDTO> listOfVcrWithPr = finalServiceDAO.findByVcrDateOfCheckBetweenAndCreatedByIn(
				vo.getFrom(), vo.getTo(), Arrays.asList(jwtUser.getUsername()));
		if (listOfVcrWithPr != null && !listOfVcrWithPr.isEmpty()) {
			List<VcrFinalServiceVO> dto = regService.calculateTaxAndTotal(listOfVcrWithPr);
			vcrList.setVcrList(dto);
		}
		return vcrList;
	}

	@Override
	public VCRVahanVehicleDetailsVO getVahanDataForApp(RcValidationVO rcValidationVO) {
		if (rcValidationVO.getServiceIds() == null || rcValidationVO.getServiceIds().isEmpty()) {
			logger.error("please provide Service type");
			throw new BadRequestException("please provide Service type");
		}
		/*
		 * List<ServiceEnum> serviceEnums =
		 * rcValidationVO.getServiceIds().stream().map(id -> {
		 * 
		 * if (id == null) { logger.error("ServiceId is null "); throw new
		 * BadRequestException("Service Id should not be empty"); } ServiceEnum se =
		 * ServiceEnum.getServiceEnumById(id); if (se == null) {
		 * logger.error("Servie is null when getByServiceEnumById : [{}]", id); throw
		 * new BadRequestException("Invalid Service Id + " + id); } return se;
		 * }).collect(Collectors.toList());
		 */
		List<ServiceEnum> serviceEnums = new ArrayList<>();
		serviceEnums.add(ServiceEnum.VOLUNTARYTAX);
		serviceEnums.add(ServiceEnum.OTHERSTATETEMPORARYPERMIT);
		serviceEnums.add(ServiceEnum.OTHERSTATESPECIALPERMIT);
		serviceEnums.add(ServiceEnum.VCR);
		VCRVahanVehicleDetailsVO vCRVahanVehicleDetailsVO = new VCRVahanVehicleDetailsVO();
		List<String> errors = new ArrayList<>();
		RegistrationDetailsDTO registrationOptional = new RegistrationDetailsDTO();
		if (StringUtils.isNoneBlank(rcValidationVO.getPrNo())) {
			try {
				registrationOptional.setPrNo(rcValidationVO.getPrNo());
				registrationService.checkVcrDues(registrationOptional, errors);
				vCRVahanVehicleDetailsVO = restGateWayService.getVahanVehicleDetailsForVcr(rcValidationVO.getPrNo());
			} catch (BadRequestException ex) {

			}
		}
		if (vCRVahanVehicleDetailsVO == null || vCRVahanVehicleDetailsVO.getRegDetailsVO() == null) {
			List<RegServiceDTO> listofDto = null;
			if (StringUtils.isNoneBlank(rcValidationVO.getPrNo())) {
				registrationOptional.setPrNo(rcValidationVO.getPrNo());
				listofDto = regServiceDAO.findByPrNoAndServiceTypeIn(rcValidationVO.getPrNo(), serviceEnums);
			} else if (StringUtils.isNoneBlank(rcValidationVO.getTrNo())) {
				registrationOptional.setTrNo(rcValidationVO.getTrNo());
				listofDto = regServiceDAO.findByTrNoAndServiceTypeIn(rcValidationVO.getTrNo(), serviceEnums);
			} else if (StringUtils.isNoneBlank(rcValidationVO.getChassisNo())) {
				VahanDetailsDTO vahanDetails = new VahanDetailsDTO();
				vahanDetails.setChassisNumber(rcValidationVO.getChassisNo());
				registrationOptional.setVahanDetails(vahanDetails);
				List<Integer> list = new ArrayList<Integer>(rcValidationVO.getServiceIds());
				listofDto = regServiceDAO
						.findByRegistrationDetailsVahanDetailsChassisNumberAndServiceIdsInAndSourceIsNull(
								rcValidationVO.getChassisNo(), list);
			} else {
				logger.error("please provide trNo/prNo/chassisNo");
				throw new BadRequestException("please provide trNo/prNo/chassisNo");
			}
			registrationService.checkVcrDues(registrationOptional, errors);
			if (!errors.isEmpty()) {
				logger.error("[{}]", errors.get(0));
				throw new BadRequestException(errors.get(0));
			}
			if (listofDto != null && !listofDto.isEmpty()) {
				listofDto.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
				RegServiceDTO regServiceDto = listofDto.stream().findFirst().get();
				RegServiceVO regServiceVo = regServiceMapper.convertEntity(regServiceDto);

				if (regServiceVo != null && regServiceVo.getRegistrationDetails() != null) {
					vCRVahanVehicleDetailsVO.setRegDetailsVO(regServiceVo.getRegistrationDetails());
					if (regServiceVo.getVoluntaryTaxDetails() != null) {
						if (regServiceVo.getVoluntaryTaxDetails().getPermitValidUpTo() != null) {
							PermitDetailsVO permitDetailsVO = new PermitDetailsVO();
							if (StringUtils.isNoneBlank(regServiceVo.getVoluntaryTaxDetails().getPermitNo())) {
								permitDetailsVO.setPermitNo(regServiceVo.getVoluntaryTaxDetails().getPermitNo());
							}
							PermitValidityDetailsVO permitValidityDetailsVO = new PermitValidityDetailsVO();
							permitValidityDetailsVO
									.setPermitValidTo(regServiceVo.getVoluntaryTaxDetails().getPermitValidUpTo());
							permitDetailsVO.setPermitValidityDetailsVO(permitValidityDetailsVO);
							vCRVahanVehicleDetailsVO.setPermitDetailsVO(permitDetailsVO);
						}
						if (regServiceVo.getVoluntaryTaxDetails().getFcValidity() != null) {
							FcDetailsVO fcDetails = new FcDetailsVO();
							fcDetails.setFcValidUpto(regServiceVo.getVoluntaryTaxDetails().getFcValidity());
							vCRVahanVehicleDetailsVO.setFcDetails(fcDetails);
						}
						if (regServiceDto.getVoluntaryTaxDetails().getHomeTaxvalidUpto() != null) {
							TaxDetailsVahanVcrVO taxDetails = new TaxDetailsVahanVcrVO();
							taxDetails.setTaxPeriodEnd(regServiceDto.getVoluntaryTaxDetails().getHomeTaxvalidUpto());
							vCRVahanVehicleDetailsVO.setTaxDetails(taxDetails);
						}
						//
						if (regServiceVo.getRegistrationDetails().getVahanDetails() != null
								&& StringUtils.isNoneBlank(regServiceVo.getVoluntaryTaxDetails().getChassisNo())) {
							regServiceVo.getRegistrationDetails().getVahanDetails()
									.setChassisNumber(regServiceVo.getVoluntaryTaxDetails().getChassisNo());
						}
					} else if (regServiceVo.getOtherStateTemporaryPermit() != null) {
						OtherStateTemporaryPermitDetailsVO dto = regServiceVo.getOtherStateTemporaryPermit();
						if (dto.getFcDetails() != null) {
							vCRVahanVehicleDetailsVO.setFcDetails(dto.getFcDetails());
						}
						/*
						 * if (dto.getInsuranceDetails() != null) { //
						 * vo.setInsuranceDetails(insuranceDeatilsMapper.convertEntity(dto.
						 * getInsuranceDetails())); } if (dto.getPucDetails() != null) {
						 * //vo.setPucDetails(pucDetailsMapper.convertEntity(dto.getPucDetails())); }
						 */
						if (dto.getPrimaryPermitDetails() != null) {
							vCRVahanVehicleDetailsVO.setPermitDetailsVO(dto.getPrimaryPermitDetails());
							// vo.setPrimaryPermitDetails(permitDeatilsMapper.convertEntity(dto.getPrimaryPermitDetails()));
						}
						/*
						 * if (dto.getTemporaryPermitDetails() != null) {
						 * //vo.setTemporaryPermitDetails(permitDeatilsMapper.convertEntity(dto.
						 * getTemporaryPermitDetails())); } if (dto.getVehicleDetails() != null) {
						 * vo.setVehicleDetails(vehicleDetailsMapper.convertEntity(dto.getVehicleDetails
						 * ())); }
						 */

						if (dto.getTaxDetails() != null && dto.getTaxDetails().getTaxValTo() != null) {
							TaxDetailsVahanVcrVO taxDetails = new TaxDetailsVahanVcrVO();
							taxDetails.setTaxPeriodEnd(dto.getTaxDetails().getTaxValTo());
							vCRVahanVehicleDetailsVO.setTaxDetails(taxDetails);

						}
					}
					if (regServiceVo.getRegistrationDetails().getPrGeneratedDate() != null) {
						RegistrationValidityVO registrationValidity = new RegistrationValidityVO();
						registrationValidity.setPrGeneratedDate(
								regServiceVo.getRegistrationDetails().getPrGeneratedDate().toLocalDate());
						regServiceVo.getRegistrationDetails().setRegistrationValidity(registrationValidity);
					}

					/*
					 * if( regServiceVo.getRegistrationDetails().getVahanDetails()!=null &&
					 * StringUtils.isNoneBlank(
					 * regServiceVo.getRegistrationDetails().getClassOfVehicle())) {
					 * regServiceVo.getRegistrationDetails().getVahanDetails().setDealerCovType(
					 * Arrays.asList(regServiceVo.getRegistrationDetails().getClassOfVehicle())); }
					 */
					vCRVahanVehicleDetailsVO.setRegDetailsVO(regServiceVo.getRegistrationDetails());
				}

			}
		}
		if (vCRVahanVehicleDetailsVO == null || vCRVahanVehicleDetailsVO.getRegDetailsVO() == null) {
			logger.error("No data found");
			throw new BadRequestException("No data found");
		}
		if (StringUtils.isNoneBlank(rcValidationVO.getPrNo()) && rcValidationVO.getServiceIds() != null
				&& rcValidationVO.getServiceIds().contains(ServiceEnum.VOLUNTARYTAX.getId())
				&& rcValidationVO.isRequestFromAdmin()) {
			Optional<VoluntaryTaxDTO> voluntry = voluntaryTaxDAO
					.findByRegNoOrderByCreatedDateDesc(rcValidationVO.getPrNo());
			if (voluntry.isPresent() && StringUtils.isNoneBlank(voluntry.get().getTaxType())
					&& voluntry.get().getTaxType().equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.Quarterly.getDesc())
					&& voluntry.get().getTaxvalidUpto() != null
					&& StringUtils.isNoneBlank(voluntry.get().getClassOfVehicle())) {
				LocalDate currentTaxValidity = citizenTaxService
						.validity(TaxTypeEnum.VoluntaryTaxType.Quarterly.getDesc());
				if (currentTaxValidity.equals(voluntry.get().getTaxvalidUpto())) {
					Optional<PropertiesDTO> properties = propertiesDAO
							.findByAllowNextQuarterTaxCovsInAndAllowNextQuarterTaxTrue(
									voluntry.get().getClassOfVehicle());
					if (properties.isPresent() && properties.get().getAllowingNextQuarterTaxInTheMonths()
							.contains(LocalDate.now().getMonthValue())) {
						Long days = ChronoUnit.DAYS.between(LocalDate.now(),
								LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth()));
						++days;
						if (days.intValue() <= properties.get().getAllowingNextQuarterTaxBeforeDays()) {
							vCRVahanVehicleDetailsVO.setAllowNextQuarterTax(Boolean.TRUE);
							Pair<Integer, Integer> monthPosition = citizenTaxService
									.getMonthposition(voluntry.get().getTaxvalidUpto().plusDays(1));
							LocalDate nextQuarterValidity = citizenTaxService.calculateChassisTaxUpTo(
									monthPosition.getFirst(), monthPosition.getSecond(),
									voluntry.get().getTaxvalidUpto().plusDays(1));
							vCRVahanVehicleDetailsVO.setTaxvalidUpto(nextQuarterValidity);
						}
					}
				} else if (currentTaxValidity.isBefore(voluntry.get().getTaxvalidUpto())) {
					logger.error("Tax already paid for " + rcValidationVO.getPrNo() + " and validity is: "
							+ voluntry.get().getTaxvalidUpto());
					throw new BadRequestException("Tax already paid for " + rcValidationVO.getPrNo()
							+ " and validity is: " + voluntry.get().getTaxvalidUpto());
				}
			}
		}

		return vCRVahanVehicleDetailsVO;
		// return regServiceMapper.convertEntity(listofDto.stream().findFirst().get());
	}

	@Override
	public List<MasterOffenceSectionsVO> getOffenceSections() {
		List<MasterOffenceSectionsDTO> offenceSectionsList = masterOffenceSectionsDao.findByStatusTrue();
		if (offenceSectionsList != null && !offenceSectionsList.isEmpty()) {
			return masterOffenceSectionsMapper.convertEntity(offenceSectionsList);
		}
		return null;
	}

	@Override
	public List<MasterOffendingSectionsVO> getOffendingSection() {
		List<MasterOffendingSectionsDTO> offendingSectionsList = masterOffendingSectionsDAO.findByStatusTrue();
		if (offendingSectionsList != null && !offendingSectionsList.isEmpty()) {
			return masterOffendingSectionsMapper.convertEntity(offendingSectionsList);
		}
		return null;
	}

	@Override
	public List<MasterPenalSectionsVO> getPenalSections() {
		List<MasterPenalSectionsDTO> penalSectionsList = masterPenalSectionsDAO.findByStatusTrue();
		if (penalSectionsList != null && !penalSectionsList.isEmpty()) {
			return masterPenalSectionsMapper.convertEntity(penalSectionsList);
		}
		return null;
	}

	@Override
	public List<RegReportVO> getCountAndOfficeViceVcrData(VcrVo vo, JwtUser jwtuser) {
		List<RegReportVO> finalReportVo = new ArrayList<>();
		List<VcrFinalServiceDTO> vcrList = this.getVcrList(vo, jwtuser);
		List<DistrictVO> districtVoList = districtService.findBySid(NationalityEnum.AP.getName());
		for (DistrictVO districtVo : districtVoList) {
			RegReportVO reportVo = new RegReportVO();
			reportVo.setDistrictId(districtVo.getDistrictId());
			reportVo.setDistrictName(districtVo.getDistrictName());
			this.commonVcrPaidCountForOfficeVice(districtVo.getDistrictId(), vcrList, finalReportVo, reportVo);
		}
		return finalReportVo;
	}

	private List<VcrFinalServiceDTO> getVcrList(VcrVo vo, JwtUser jwtuser) {
		List<String> distinctMVI = mongoTemplate.getCollection("table_vcr_details").distinct("createdBy");
		List<VcrFinalServiceDTO> vcrList = finalServiceDAO.nativePaidDateBetweenAndCreatedByIn(
				getTimewithDate(vo.getFromDate(), false), getTimewithDate(vo.getToDate(), true), distinctMVI);
		return vcrList;
	}

	private void commonVcrPaidCountForOfficeVice(Integer districtId, List<VcrFinalServiceDTO> vcrList,
			List<RegReportVO> finalReportVo, RegReportVO reportVo) {
		List<OfficeVO> officeVoList = officeService.getOfficeByDistrict(districtId);
		List<ReportVO> officeReportList = new ArrayList<>();
		Double total = 0d;
		for (OfficeVO officeVo : officeVoList) {
			ReportVO officeReport = new ReportVO();
			// System.out.println(officeVo.getOfficeCode());
			officeReport.setOfficeCode(officeVo.getOfficeCode());
			officeReport.setOfficeName(officeVo.getOfficeName());
			long count = vcrList.stream().filter(one -> one.getOfficeCode().equalsIgnoreCase(officeVo.getOfficeCode()))
					.collect(Collectors.counting());
			officeReport.setCount(count);
			officeReportList.add(officeReport);
			total = total + count;
		}
		reportVo.setFeeReport(officeReportList);
		reportVo.setTotal(total);
		finalReportVo.add(reportVo);
	}

	@Override
	public List<RegReportVO> getVcrPaidCountForOfficeVice(VcrVo vo, JwtUser jwtuser) {
		List<RegReportVO> finalReportVo = new ArrayList<>();
		List<VcrFinalServiceDTO> vcrList = this.getVcrList(vo, jwtuser);
		if (vo.getDistrictId() == null) {
			logger.error("Please provide District name");
			throw new BadRequestException("Please provide District name");
		}
		Optional<DistrictDTO> OptonalDist = districtDAO.findBydistrictId(vo.getDistrictId());
		if (OptonalDist == null || !OptonalDist.isPresent()) {
			logger.error("No master District data found for : " + vo.getDistrictId());
			throw new BadRequestException("No master District data found for : " + vo.getDistrictId());
		}
		RegReportVO reportVo = new RegReportVO();
		reportVo.setDistrictId(OptonalDist.get().getDistrictId());
		reportVo.setDistrictName(OptonalDist.get().getDistrictName());
		this.commonVcrPaidCountForOfficeVice(OptonalDist.get().getDistrictId(), vcrList, finalReportVo, reportVo);
		return finalReportVo;
	}

	@Override
	public ReportsVO getDistrictViceVcrCount(VcrVo vo, JwtUser jwtuser) throws Exception {
		if (vo.getFromDate() == null && vo.getToDate() == null) {
			throw new BadRequestException("From Date / To Date is required");
		}

		List<String> distinctMVI = new ArrayList<>();
		// String officeCode = jwtuser.getOfficeCode();
		if (vo.getDistrictId() != null) {
			distinctMVI = getMVIforDistrict(vo.getDistrictId());
		} else if (StringUtils.isNoneBlank(vo.getOfficeCode())) {
			DBObject office = new BasicDBObject("officeCode",
					new BasicDBObject("$in", Arrays.asList(vo.getOfficeCode())));
			distinctMVI = mongoTemplate.getCollection("table_vcr_details").distinct("createdBy", office);
		} else {
			throw new BadRequestException("Office Code or district code not found");
		}
		List<VcrFinalServiceDTO> vcrList = finalServiceDAO.nativeVcrDateOfCheckBetweenAndCreatedByIn(distinctMVI,
				getTimewithDate(vo.getFromDate(), false), getTimewithDate(vo.getToDate(), true));
		if (CollectionUtils.isEmpty(vcrList)) {
			throw new BadRequestException(
					"No Vcr Data Found for Dates B/w " + vo.getFromDate() + " and " + vo.getToDate());
		}
		List<UserDTO> users = null;
		// List<String> listOfOffices =null;
		// if(vo.getDistrictId()!=null) {
		List<String> listOfOffices = this.getDistrictOffices(vo.getDistrictId());
		vcrList = vcrList.stream().filter(
				one -> listOfOffices.stream().anyMatch(office -> office.equalsIgnoreCase(one.getMviOfficeCode())))
				.collect(Collectors.toList());
		users = userDAO.findByPrimaryRoleNameOrAdditionalRolesNameAndOfficeOfficeCodeIn(RoleEnum.MVI.getName(),
				RoleEnum.MVI.getName(), listOfOffices);
		// }

		ReportsVO reportVo = finalServiceMapper.convertDistrictViceVcrCount(vcrList, distinctMVI, users, listOfOffices);
		reportVo.setPageNo(0);
		Double doublePages = Math.ceil(vcrList.size() / 20f);
		reportVo.setTotalPage(doublePages.intValue());
		/*
		 * List<UserVO> userVOList = mviUserIdsAndNamesMapping(distinctMVI);
		 * regReport.setMviUsers(userVOList); regReport.setMvi(distinctMVI);
		 */
		return reportVo;
	}

	@Override
	public void generateDistrictViceVcrCountExcelReport(HttpServletResponse response, ReportsVO result,
			String officeCode) throws Exception {

		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "VcrReport");

		String name = "VcrReport";
		String fileName = name + ".xlsx";
		String sheetName = "VcrReport";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		response.setHeader("Pragma", "public");
		response.setHeader("Cache-Control", "no-store");
		response.addHeader("Cache-Control", "max-age=0");

		XSSFWorkbook wb = excel.renderData(prepareCellPropsForVcrList(header, result), header, fileName, sheetName);

		XSSFSheet sheet = wb.getSheet(sheetName);
		sheet.setDefaultColumnWidth(40);
		XSSFDataFormat df = wb.createDataFormat();
		sheet.getColumnStyle(4).setDataFormat(df.getFormat("0x31"));

		CellStyle style = wb.createCellStyle();// Create style

		XSSFFont font = wb.createFont();// Create font
		font.setBoldweight(XSSFFont.BOLDWEIGHT_NORMAL);// Make font bold
		font.setColor(HSSFColor.BLACK.index);
		style.setFont(font);// set it to bold

		try {
			ServletOutputStream outputStream = null;
			outputStream = response.getOutputStream();
			wb.write(outputStream);
			outputStream.flush();
			outputStream.close();

		} catch (IOException e) {
			logger.error(" excel retriveAllRecords download IOException : {}", e.getMessage());
		}

	}

	private List<List<CellProps>> prepareCellPropsForVcrList(List<String> header, ReportsVO resultList) {

		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (ReportVO report : resultList.getReport()) {

			if (null != report.getVcrList()) {
				List<CellProps> result = new ArrayList<CellProps>();
				for (int i = 0; i < header.size(); i++) {
					CellProps cellpro = new CellProps();
					switch (i) {
					case 0:
						cellpro.setFieldValue(String.valueOf(++slNo));
						break;

					case 1:
						cellpro.setFieldValue((report.getVcrList().stream().findFirst().get().getRegistration() == null)
								? StringUtils.EMPTY
								: (report.getVcrList().stream().findFirst().get().getRegistration()
										.getChassisNumber() != null)
												? report.getVcrList().stream().findFirst().get().getRegistration()
														.getChassisNumber()
												: (report.getVcrList().stream().findFirst().get().getRegistration()
														.getTrNo() != null)
																? report.getVcrList().stream().findFirst().get()
																		.getRegistration().getTrNo()
																: (report.getVcrList().stream().findFirst().get()
																		.getRegistration().getRegNo() != null)
																				? report.getVcrList().stream()
																						.findFirst().get()
																						.getRegistration().getRegNo()
																				: StringUtils.EMPTY);
						break;

					case 2:
						cellpro.setFieldValue(report.getVcrList().stream().findFirst().get().getVcr().getVcrNumber());
						break;

					case 3:
						cellpro.setFieldValue(report.getVcrList().stream().findFirst().get().getRegistration()
								.getClasssOfVehicle().getCovdescription());
						break;

					case 4:
						cellpro.setFieldValue(DateConverters
								.replaceDefaults(report.getVcrList().stream().findFirst().get().getChallanNo()));
						break;

					case 5:
						cellpro.setFieldValue(DateConverters.replaceDefaults(
								report.getVcrList().stream().findFirst().get().getVcr().getDateOfCheck()));
						break;

					case 6:
						cellpro.setFieldValue(DateConverters
								.replaceDefaults(report.getVcrList().stream().findFirst().get().getChallanDate()));
						break;

					case 7:
						cellpro.setFieldValue(report.getVcrList().stream().findFirst().get().getActionTaken());
						break;

					case 8:
						cellpro.setFieldValue(DateConverters
								.replaceDefaults(report.getVcrList().stream().findFirst().get().getPaidDate()));
						break;

					case 9:
						cellpro.setFieldValue(report.getVcrList().stream().findFirst().get().getMviName());
						break;

					case 10:
						cellpro.setFieldValue(report.getVcrList().stream().findFirst().get().getRecieptNo());
						break;

					case 11:
						cellpro.setFieldValue(report.getVcrList().stream().findFirst().get().getVcrStatus());
						break;

					case 12:
						cellpro.setFieldValue(DateConverters
								.replaceDefaults(report.getVcrList().stream().findFirst().get().getServiceFee()));
						break;

					case 13:
						cellpro.setFieldValue(DateConverters
								.replaceDefaults(report.getVcrList().stream().findFirst().get().getCompoundFee()));
						break;

					default:
						break;
					}
					result.add(cellpro);
				}
				cell.add(result);
			}
		}

		return cell;

	}

	@Override
	public void generateExcelListforMVINames(HttpServletResponse response, ReportsVO report) {

		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "VcrReportListMVI");

		String name = "VcrReportListMVI";
		String fileName = name + ".xlsx";
		String sheetName = "VcrReportListMVI";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		response.setHeader("Pragma", "public");
		response.setHeader("Cache-Control", "no-store");
		response.addHeader("Cache-Control", "max-age=0");

		XSSFWorkbook wb = excel.renderData(prepareCellPropsForMviList(header, report), header, fileName, sheetName);

		XSSFSheet sheet = wb.getSheet(sheetName);
		sheet.setDefaultColumnWidth(40);
		XSSFDataFormat df = wb.createDataFormat();
		sheet.getColumnStyle(4).setDataFormat(df.getFormat("0x31"));

		CellStyle style = wb.createCellStyle();// Create style

		XSSFFont font = wb.createFont();// Create font
		font.setBoldweight(XSSFFont.BOLDWEIGHT_NORMAL);// Make font bold
		font.setColor(HSSFColor.BLACK.index);
		style.setFont(font);// set it to bold

		try {
			ServletOutputStream outputStream = null;
			outputStream = response.getOutputStream();
			wb.write(outputStream);
			outputStream.flush();
			outputStream.close();

		} catch (IOException e) {
			logger.error(" excel retriveAllRecords download IOException : {}", e.getMessage());
		}

	}

	private List<List<CellProps>> prepareCellPropsForMviList(List<String> header, ReportsVO resultList) {

		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (ReportVO report : resultList.getReport()) {
			List<CellProps> result = new ArrayList<CellProps>();
			for (int i = 0; i < header.size(); i++) {
				CellProps cellpro = new CellProps();
				switch (i) {
				case 0:
					cellpro.setFieldValue(String.valueOf(++slNo));
					break;

				case 1:
					cellpro.setFieldValue(report.getMviName());
					break;

				case 2:
					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getCount()));
					break;

				case 3:
					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getTotalPiadCf()));
					break;

				case 4:
					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getTotalUnPiadCf()));
					break;

				case 5:
					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getTaxAmount()));
					break;

				case 6:
					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getPenalty()));
					break;

				case 7:
					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getTaxArrears()));
					break;

				case 8:
					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getPenaltyArrears()));
					break;

				case 9:
					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getMviTotalAmount()));
					break;

				default:
					break;
				}
				result.add(cellpro);
			}
			cell.add(result);
		}

		cell.add(mapTotalRowCell(header, resultList));
		return cell;

	}

	public List<CellProps> mapTotalRowCell(List<String> header, ReportsVO report) {

		// int slNo = 0;

		List<CellProps> result = new ArrayList<CellProps>();
		for (int i = 0; i < header.size(); i++) {
			CellProps cellpro = new CellProps();

			switch (i) {

			case 0:
				cellpro.setFieldValue(StringUtils.EMPTY);
				break;

			case 1:
				cellpro.setFieldValue("total");
				break;

			case 2:
				cellpro.setFieldValue(DateConverters.replaceDefaults(report.getTotalVcrCount()));
				break;

			case 3:
				cellpro.setFieldValue(DateConverters.replaceDefaults(report.getTotalPaidCf()));
				break;

			case 4:
				cellpro.setFieldValue(DateConverters.replaceDefaults(report.getTotalUnPaidCf()));
				break;

			case 5:
				cellpro.setFieldValue(DateConverters.replaceDefaults(report.getTotalTax()));
				break;

			case 6:
				cellpro.setFieldValue(DateConverters.replaceDefaults(report.getTotalPenalty()));
				break;

			case 7:
				cellpro.setFieldValue(DateConverters.replaceDefaults(report.getTotalTaxArrears()));
				break;

			case 8:
				cellpro.setFieldValue(DateConverters.replaceDefaults(report.getTotalPenaltyArrears()));
				break;

			case 9:
				cellpro.setFieldValue(DateConverters.replaceDefaults(report.getFinalTotal()));
				break;

			default:
				break;
			}
			result.add(cellpro);
		}
		return result;

	}

	private List<UserWiseEODCount> getVcrMviWiseCount(LocalDateTime fromDate, LocalDateTime toDate,
			List<String> mviNames) {

		Aggregation agg = newAggregation(
				match(Criteria.where("vcr.dateOfCheck").gte(fromDate).lte(toDate).and("createdBy").in(mviNames)),
				group("createdBy").count().as("count"), project("count").and("userName").previousOperation());

		AggregationResults<UserWiseEODCount> groupResults = mongoTemplate.aggregate(agg, VcrFinalServiceDTO.class,
				UserWiseEODCount.class);

		if (!groupResults.getMappedResults().isEmpty()) {
			return groupResults.getMappedResults();
		}
		return Collections.EMPTY_LIST;
	}

	@Override
	public VcrFinalServiceVO amountForPreview(VcrFinalServiceVO vo) {
		if (vo != null) {
			logger.info("vcr amount For Preview request time" + LocalDateTime.now());
			VcrFinalServiceDTO dto = finalServiceMapper.convertVO(vo);
			// String regApplicationNo = this.getRegApplicationNo(dto);
			// dto.getRegistration().setRegApplicationNo(regApplicationNo);
			List<VcrFinalServiceDTO> vcrList = new ArrayList<>();
			ApplicationSearchVO applicationSearchVO = new ApplicationSearchVO();
			if (StringUtils.isNoneBlank(dto.getRegistration().getChassisNumber())) {
				applicationSearchVO.setChassisNo(dto.getRegistration().getChassisNumber());
			}
			if (StringUtils.isNoneBlank(dto.getRegistration().getRegNo())) {
				applicationSearchVO.setPrNo(dto.getRegistration().getRegNo());
			}
			if (StringUtils.isNoneBlank(dto.getRegistration().getTrNo())) {
				applicationSearchVO.setTrNo(dto.getRegistration().getTrNo());
			}
			applicationSearchVO.setRequestFromAO(Boolean.TRUE);
			dto.setSaveDoc(Boolean.TRUE);
			CitizenSearchReportVO outPut = new CitizenSearchReportVO();
			// vcrList = regService.getTotalVcrs(applicationSearchVO, vcrList);
			regService.getTotalVcrsAfterPayments(applicationSearchVO, vcrList);
			/* if (vcrList != null && !vcrList.isEmpty()) { */
			/*
			 * vcrList = regService
			 * .getVcrDetails(Arrays.asList(vcrList.stream().findFirst().get().getVcr().
			 * getVcrNumber()), true);
			 */
			vcrList.add(dto);
			/*
			 * } else { vcrList.add(dto); }
			 */
			regService.getVcrAmount(applicationSearchVO, vcrList, outPut);
			List<VcrFinalServiceVO> listvo = outPut.getVcrList();
			VcrFinalServiceVO vcrvo = new VcrFinalServiceVO();
			for (VcrFinalServiceVO vcrVo : listvo) {
				if (vcrVo.getSaveDoc() != null && vcrVo.getSaveDoc()) {
					dto.setOffencetotal(vcrVo.getOffencetotal());
					vcrvo = vcrVo;
				}
			}
			dto.setSaveDoc(null);
			logger.info("vcr amount For Preview response time" + LocalDateTime.now());
			return vcrvo;
		} else {
			logger.error("please pass appropriate JSON format.");
			throw new BadRequestException("please pass appropriate JSON format.");
		}
	}

	@Override
	public TrIssuedReportVO getEvcrReport(LocalDate fromDate, LocalDate toDate, String vcrNumber, JwtUser jwtUser,
			String reportName, Pageable page) {
		// TODO Auto-generated method stub
		List<VcrFinalServiceVO> finalvo = new ArrayList<>();
		TrIssuedReportVO vo = new TrIssuedReportVO();
		if (StringUtils.isNotBlank(vcrNumber)) {
			Optional<VcrFinalServiceDTO> vcrfinalDto = finalServiceDAO.findByVcrVcrNumber(vcrNumber);
			if (!vcrfinalDto.isPresent()) {
				throw new BadRequestException("vcr details not available");
			}
			finalvo.add(finalServiceMapper.convertEntity(vcrfinalDto.get()));
			vo.setEvcrList(finalvo);
			return vo;
		}
		Pageable pageRequest = new PageRequest(page.getPageNumber(), page.getPageSize(), Sort.Direction.DESC,
				"vcr.dateOfCheck");
		Page<VcrFinalServiceDTO> vcrList = null;
		if (reportName.equalsIgnoreCase("evcr")) {
			vcrList = finalServiceDAO.findByVcrDateOfCheckBetweenAndOfficeCodeAndIseVcr(
					paymentReportService.getTimewithDate(fromDate, Boolean.FALSE),
					paymentReportService.getTimewithDate(toDate, Boolean.TRUE), jwtUser.getOfficeCode(), Boolean.TRUE,
					pageRequest);
		}
		if (reportName.equalsIgnoreCase("speedgun")) {
			vcrList = finalServiceDAO.findByVcrDateOfCheckBetweenAndMviOfficeCodeAndIsSpeedGun(
					paymentReportService.getTimewithDate(fromDate, Boolean.FALSE),
					paymentReportService.getTimewithDate(toDate, Boolean.TRUE), jwtUser.getOfficeCode(), Boolean.TRUE,
					pageRequest);

		}
		if (!(vcrList != null || vcrList.hasContent())) {
			throw new BadRequestException("E-Vcr's/Speed Gun records not found");
		}
		List<VcrFinalServiceDTO> nonPrintedList = new ArrayList<>();
		List<VcrFinalServiceDTO> printedList = new ArrayList<>();
		vcrList.getContent().stream().forEach(action -> {
			if (reportName.equalsIgnoreCase("evcr")) {
				if (action.getIsEvcrPrinted() == null || !action.getIsEvcrPrinted()) {
					nonPrintedList.add(action);
				} else {
					printedList.add(action);
				}
			} else {
				if (action.getIsSpeedGunVcrPrinted() == null || !action.getIsSpeedGunVcrPrinted()) {
					nonPrintedList.add(action);
				} else {
					printedList.add(action);
				}
			}

		});
		vo.setPageNumber(vcrList.getNumber());
		vo.setTotalPageSize(vcrList.getTotalPages());
		List<VcrFinalServiceDTO> finalList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(nonPrintedList)) {
			finalList.addAll(nonPrintedList);
		}
		if (CollectionUtils.isNotEmpty(printedList)) {
			finalList.addAll(printedList);
		}

		setCovDescAndAddress(finalList);
		List<VcrFinalServiceVO> evcrList = finalServiceMapper.convertEntity(finalList);
		for (VcrFinalServiceVO evcrVo : evcrList) {
			String qrCode = StringUtils.EMPTY;
			try {
				qrCode = qRCodeService.sendPDF(getVCRReleaseOrder(evcrVo.getVcr().getVcrNumber()));
				if (qrCode == null)
					qrCode = StringUtils.EMPTY;
			} catch (Exception e) {
				logger.debug(" Exception :[] ", e);
				logger.error(" Exception :[] ", e.getMessage());
			}
			evcrVo.setQrImage(qrCode);
		}

		vo.setEvcrList(evcrList);
		return vo;
	}

	private void setCovDescAndAddress(List<VcrFinalServiceDTO> finalList) {
		// TODO Auto-generated method stub

		finalList.stream().forEach(record -> {
			if (record.getOwnerDetails().getAddress().equals(null)
					|| record.getRegistration().getClasssOfVehicle().getCovdescription() == null) {
				Optional<RegistrationDetailsDTO> regDetailsrecordOptional = registrationDetailDAO
						.findByApplicationNo(record.getRegistration().getRegApplicationNo());
				if (regDetailsrecordOptional.isPresent()) {
					String address = null;
					RegistrationDetailsDTO regRecord = regDetailsrecordOptional.get();
					
					
			
							
							if(regRecord.getApplicantDetails().getPresentAddress().getDoorNo()!=null) {
								address=address+regRecord.getApplicantDetails().getPresentAddress().getDoorNo()+",";
							}
							if(regRecord.getApplicantDetails().getPresentAddress().getVillage()!=null&&regRecord.getApplicantDetails().getPresentAddress().getVillage().getVillageName()!=null) {
								
								address=address+regRecord.getApplicantDetails().getPresentAddress().getVillage().getVillageName()+",";
							}
							if(regRecord.getApplicantDetails().getPresentAddress().getMandal()!=null&&regRecord.getApplicantDetails().getPresentAddress().getMandal().getMandalName()!=null) {
								address=address+regRecord.getApplicantDetails().getPresentAddress().getMandal().getMandalName()+",";
							}
							if(regRecord.getApplicantDetails().getPresentAddress().getDistrict()!=null&&regRecord.getApplicantDetails().getPresentAddress().getDistrict().getDistrictName()!=null) {
								address=address+regRecord.getApplicantDetails().getPresentAddress().getDistrict().getDistrictName()+",";
							}
							if(regRecord.getApplicantDetails().getPresentAddress().getState()!=null&&regRecord.getApplicantDetails().getPresentAddress().getState().getStateName()!=null) {
								address=address+regRecord.getApplicantDetails().getPresentAddress().getState().getStateName()+",";
							}
							if(regRecord.getApplicantDetails().getPresentAddress().getPostOffice()!=null&&regRecord.getApplicantDetails().getPresentAddress().getPostOffice().getPostOfficeCode()!=null) {
								address=address+regRecord.getApplicantDetails().getPresentAddress().getPostOffice().getPostOfficeCode()+".";
							}

					record.getOwnerDetails().setAddress(address);

				}
			}

			if (record.getRegistration().getClasssOfVehicle().getCovdescription() == null) {

				if (record.getRegistration().getRegApplicationNo() != null) {

					Optional<RegistrationDetailsDTO> regDetailsrecordOptional = registrationDetailDAO
							.findByApplicationNo(record.getRegistration().getRegApplicationNo());
					if (regDetailsrecordOptional.isPresent()) {
						RegistrationDetailsDTO regRecord = regDetailsrecordOptional.get();
						MasterCovDTO covDetails = record.getRegistration().getClasssOfVehicle();
						covDetails.setCovdescription(regDetailsrecordOptional.get().getClassOfVehicleDesc());

					}
				}

			}

		});

	}

	@Override
	public void saveEvcrPrintedRecords(JwtUser jwtUser, List<String> printedRecords, String reportName) {
		if (CollectionUtils.isEmpty(printedRecords)) {
			throw new BadRequestException("printed list should not be Empty");
		}
		List<VcrFinalServiceDTO> vcrList = finalServiceDAO.findByIdIn(printedRecords);
		if (CollectionUtils.isEmpty(vcrList)) {
			throw new BadRequestException("Records Not Found");
		}
		vcrList.stream().forEach(action -> {
			if (reportName.equalsIgnoreCase("evcr")) {
				if (!action.getIseVcr()) {
					throw new BadRequestException("Please send selected report name");
				}
				if (action.getIsEvcrPrinted() != null) {
					EVcrPrintedDTO dto = new EVcrPrintedDTO();
					dto.seteVcrPrintedBy(action.geteVcrPrintedBy());
					dto.seteVcrPrintedDate(action.geteVcrPrintedDate());
					dto.setIsEvcrPrinted(action.getIsEvcrPrinted());
					if (CollectionUtils.isNotEmpty(action.getEvcrPrintedLog())) {
						action.getEvcrPrintedLog().add(dto);
					} else {
						List<EVcrPrintedDTO> dtoList = new ArrayList<EVcrPrintedDTO>();
						dtoList.add(dto);
						action.setEvcrPrintedLog(dtoList);
					}

				}
				action.seteVcrPrintedBy(jwtUser.getUsername());
				action.seteVcrPrintedDate(LocalDateTime.now());
				action.setIsEvcrPrinted(Boolean.TRUE);
			}
			if (reportName.equalsIgnoreCase("speedgun")) {
				if (!action.getIsSpeedGun()) {
					throw new BadRequestException("Please send selected report name");
				}
				if (action.getIsSpeedGunVcrPrinted() != null) {
					SpeedGunPrintDTO dto = new SpeedGunPrintDTO();
					dto.setSpeedGunVcrPrintedBy(action.getSpeedGunVcrPrintedBy());
					dto.setSpeedGunVcrPrintedDate(action.getSpeedGunVcrPrintedDate());
					dto.setIsSpeedGunVcrPrinted(action.getIsSpeedGun());
					if (CollectionUtils.isNotEmpty(action.getSpeedGunVcrPrintedLog())) {
						action.getSpeedGunVcrPrintedLog().add(dto);
					} else {
						List<SpeedGunPrintDTO> dtoList = new ArrayList<SpeedGunPrintDTO>();
						dtoList.add(dto);
						action.setSpeedGunVcrPrintedLog(dtoList);
					}
				}
				action.setSpeedGunVcrPrintedBy(jwtUser.getUsername());
				action.setSpeedGunVcrPrintedDate(LocalDateTime.now());
				action.setIsSpeedGunVcrPrinted(Boolean.TRUE);
			}
		});
		finalServiceDAO.save(vcrList);
	}

	public String getVCRReleaseOrder(String applicationNo) {

		return serverURL + "citizenServices/getVcrReport?inputNumber=" + applicationNo;
	}

	@Override
	public void generateVcrPaymentReportsExcelList(List<RegReportVO> report, HttpServletResponse response) {

		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "VcrPaymentReportList");

		String name = "VcrPaymentReportList";
		String fileName = name + ".xlsx";
		String sheetname = "VcrPaymentReportList";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);

		XSSFWorkbook wb = excel.renderData(prepareCellPropsForVcrPaymentReportsExcel(header, report), header, fileName,
				sheetname);

		XSSFSheet sheet = wb.getSheet(sheetname);
		sheet.setDefaultColumnWidth(40);
		XSSFDataFormat df = wb.createDataFormat();
		sheet.getColumnStyle(4).setDataFormat(df.getFormat("0x31"));

		XSSFCellStyle style = wb.createCellStyle(); // create style
		XSSFFont font = wb.createFont();
		font.setBoldweight(XSSFFont.BOLDWEIGHT_NORMAL);// Make font bold
		font.setColor(HSSFColor.BLACK.index);
		style.setFont(font);// set it to bold

		try {

			ServletOutputStream outputStream = response.getOutputStream();
			wb.write(outputStream);
			outputStream.flush();
			outputStream.close();

		} catch (IOException e) {
			logger.error(" excel retrive AllRecords download IOException : {}", e.getMessage());

		}

	}

	private List<List<CellProps>> prepareCellPropsForVcrPaymentReportsExcel(List<String> header,
			List<RegReportVO> resultList) {

		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		for (RegReportVO report : resultList) {

			if (report != null) {
				List<CellProps> result = new ArrayList<CellProps>();
				for (int i = 0; i < header.size(); i++) {
					CellProps cellpro = new CellProps();
					switch (i) {
					case 0:
						cellpro.setFieldValue(String.valueOf(++slNo));
						break;
					case 1:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getDistrictName()));
						break;
					case 2:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report.getTotal()));
					default:
						break;
					}
					result.add(cellpro);
				}
				cell.add(result);
			}
		}
		return cell;
	}

	@Override
	public void generateExcelListForVcrpaymentReportOfficeData(List<RegReportVO> report, HttpServletResponse response) {

		List<String> header = new ArrayList<String>();

		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "VcrPaymentReportListOfficeData");

		String name = "VcrPaymentReportListOfficeData";
		String fileName = name + ".xlsx";
		String sheetname = "VcrPaymentReportListOfficeData";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);

		XSSFWorkbook wb = excel.renderData(prepareCellPropsForVcrPaymentReportsOfficeDataExcel(header, report), header,
				fileName, sheetname);

		XSSFSheet sheet = wb.getSheet(sheetname);
		sheet.setDefaultColumnWidth(40);

		XSSFCellStyle style = wb.createCellStyle(); // create style
		XSSFFont font = wb.createFont();
		font.setBoldweight(XSSFFont.BOLDWEIGHT_NORMAL);// Make font bold
		font.setColor(HSSFColor.BLACK.index);
		style.setFont(font);// set it to bold

		try {

			ServletOutputStream outputStream = response.getOutputStream();
			wb.write(outputStream);
			outputStream.flush();
			outputStream.close();

		} catch (IOException e) {
			logger.error(" excel retrive AllRecords download IOException : {}", e.getMessage());

		}

	}

	private List<List<CellProps>> prepareCellPropsForVcrPaymentReportsOfficeDataExcel(List<String> header,
			List<RegReportVO> resultList) {

		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<List<CellProps>>();

		if (resultList != null && resultList.stream().findFirst().get().getFeeReport() != null) {
			for (ReportVO report : resultList.stream().findFirst().get().getFeeReport()) {

				if (report != null) {
					List<CellProps> result = new ArrayList<CellProps>();
					for (int i = 0; i < header.size(); i++) {
						CellProps cellpro = new CellProps();
						switch (i) {
						case 0:
							cellpro.setFieldValue(String.valueOf(++slNo));
							break;
						case 1:
							cellpro.setFieldValue(DateConverters.replaceDefaults(report.getOfficeName()));
							break;
						case 2:
							cellpro.setFieldValue(DateConverters.replaceDefaults(report.getCount()));
						default:
							break;
						}
						result.add(cellpro);
					}
					cell.add(result);
				}
			}
		}
		return cell;
	}

	@Override
	public ReportVO getRoadSafetyVcrCount(VcrVo vo, JwtUser jwtuser) throws Exception {
		if (vo.getFromDate() == null && vo.getToDate() == null) {
			throw new BadRequestException("From Date / To Date is required");
		}

		List<VcrFinalServiceDTO> vcrList = finalServiceDAO.nativeVcrDateAndIsRoadSafetyIsTrue(
				getTimewithDate(vo.getFromDate(), false), getTimewithDate(vo.getToDate(), true));
		if (CollectionUtils.isEmpty(vcrList)) {
			throw new BadRequestException(
					"No Vcr Data Found for Dates B/w " + vo.getFromDate() + " and " + vo.getToDate());
		}
		// List<DistrictDTO> districtDTO =
		// districtDAO.findByStateId(NationalityEnum.AP.getName());

		ReportVO reportVo = finalServiceMapper.convertRoadSafetyVcrCount(vcrList, vo.getDistrictId());

		return reportVo;
	}

	@Override
	public ReportVO getRoadSafetyVcrDistrictCount(VcrVo vo, JwtUser jwtuser) throws Exception {
		if (vo.getFromDate() == null && vo.getToDate() == null) {
			throw new BadRequestException("From Date / To Date is required");
		}

		List<VcrFinalServiceDTO> vcrList = finalServiceDAO.nativeVcrDateAndIsRoadSafetyIsTrue(
				getTimewithDate(vo.getFromDate(), false), getTimewithDate(vo.getToDate(), true));
		if (CollectionUtils.isEmpty(vcrList)) {
			throw new BadRequestException(
					"No Vcr Data Found for Dates B/w " + vo.getFromDate() + " and " + vo.getToDate());
		}
		List<DistrictDTO> districtDTO = districtDAO.findByStateId(NationalityEnum.AP.getName());

		List<DistrictDTO> finalList = districtDTO.stream()
				.filter(one -> !(one.getDistrictName().equalsIgnoreCase("VISAKHAPATNAM")
						|| one.getDistrictName().equalsIgnoreCase("APSTA")))
				.collect(Collectors.toList());
		ReportVO reportVo = finalServiceMapper.convertRoadSafetyVcrDistrictCount(vcrList, finalList);

		return reportVo;
	}

	@Override
	public void generateExcelForRoadSafetyMviCount(ReportVO report, HttpServletResponse response) {

		List<String> header = new ArrayList<String>();
		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "RoadSafetyMviCount");

		String name = "RoadSafetyMviCount";
		String fileName = name + ".xlsx";
		String sheetname = "RoadSafetyMviCount";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);

		XSSFWorkbook wb = excel.renderData(prepareCellPropsForRoadSafetyMviCount(header, report), header, fileName,
				sheetname);

		XSSFSheet sheet = wb.getSheet(sheetname);
		sheet.setDefaultColumnWidth(40);

		XSSFCellStyle style = wb.createCellStyle(); // create style
		XSSFFont font = wb.createFont();
		font.setBoldweight(XSSFFont.BOLDWEIGHT_NORMAL);// Make font bold
		font.setColor(HSSFColor.BLACK.index);
		style.setFont(font);// set it to bold

		try {

			ServletOutputStream outputStream = response.getOutputStream();
			wb.write(outputStream);
			outputStream.flush();
			outputStream.close();

		} catch (IOException e) {
			logger.error(" excel retrive AllRecords download IOException : {}", e.getMessage());

		}

	}

	private List<List<CellProps>> prepareCellPropsForRoadSafetyMviCount(List<String> header, ReportVO report1) {
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<>();

		for (ReportVO report2 : report1.getReport()) {
			List<CellProps> cells = new ArrayList<>();
			for (int i = 0; i < header.size(); i++) {
				CellProps cellpro = new CellProps();
				switch (i) {
				case 0:
					cellpro.setFieldValue(String.valueOf(++slNo));
					break;
				case 1:
					if (report2.getMviName() != null) {
						cellpro.setFieldValue(String.valueOf(report2.getMviName()));
					}
					break;
				case 2:
					if (report2.getReport().get(i - 2).getOffenceCount() >= 0) {
						cellpro.setFieldValue(
								DateConverters.replaceDefaults(report2.getReport().get(i - 2).getOffenceCount()));
					}
					break;
				case 3:
					if (report2.getReport().get(i - 2).getOffenceCount() >= 0) {
						cellpro.setFieldValue(
								DateConverters.replaceDefaults(report2.getReport().get(i - 2).getOffenceCount()));
					}
					break;
				case 4:
					if (report2.getReport().get(i - 2).getOffenceCount() >= 0) {
						cellpro.setFieldValue(
								DateConverters.replaceDefaults(report2.getReport().get(i - 2).getOffenceCount()));
					}
					break;
				case 5:
					if (report2.getReport().get(i - 2).getOffenceCount() >= 0) {
						cellpro.setFieldValue(
								DateConverters.replaceDefaults(report2.getReport().get(i - 2).getOffenceCount()));
					}
					break;
				case 6:
					if (report2.getReport().get(i - 2).getOffenceCount() >= 0) {
						cellpro.setFieldValue(
								DateConverters.replaceDefaults(report2.getReport().get(i - 2).getOffenceCount()));
					}
					break;
				case 7:
					if (report2.getReport().get(i - 2).getOffenceCount() >= 0) {
						cellpro.setFieldValue(
								DateConverters.replaceDefaults(report2.getReport().get(i - 2).getOffenceCount()));
					}
					break;
				case 8:
					if (report2.getReport().get(i - 2).getOffenceCount() >= 0) {
						cellpro.setFieldValue(
								DateConverters.replaceDefaults(report2.getReport().get(i - 2).getOffenceCount()));
					}
					break;
				case 9:
					if (report2.getReport().get(i - 2).getOffenceCount() >= 0) {
						cellpro.setFieldValue(
								DateConverters.replaceDefaults(report2.getReport().get(i - 2).getOffenceCount()));
					}
					break;
				case 10:
					if (report2.getTotalCount() >= 0) {
						cellpro.setFieldValue(DateConverters.replaceDefaults(report2.getTotalCount()));
					}
				default:
					break;
				}
				cells.add(cellpro);
			}
			cell.add(cells);
		}
		cell.add(prepareMapRowCellToRoadSafetyMviCount(header, report1));
		return cell;
	}

	private List<CellProps> prepareMapRowCellToRoadSafetyMviCount(List<String> header, ReportVO report) {

		List<CellProps> cells = new ArrayList<>();
		for (int i = 0; i < header.size(); i++) {
			CellProps cellpro = new CellProps();
			switch (i) {
			case 0:
				cellpro.setFieldValue(StringUtils.EMPTY);
				break;
			case 1:
				cellpro.setFieldValue("Total");
				break;
			case 2:
				if (report.getTotaloffenceCount().get(i - 2).getOffenceCount() >= 0) {
					cellpro.setFieldValue(
							DateConverters.replaceDefaults(report.getTotaloffenceCount().get(i - 2).getOffenceCount()));
				}
				break;
			case 3:
				if (report.getTotaloffenceCount().get(i - 2).getOffenceCount() >= 0) {
					cellpro.setFieldValue(
							DateConverters.replaceDefaults(report.getTotaloffenceCount().get(i - 2).getOffenceCount()));
				}
				break;
			case 4:
				if (report.getTotaloffenceCount().get(i - 2).getOffenceCount() >= 0) {
					cellpro.setFieldValue(
							DateConverters.replaceDefaults(report.getTotaloffenceCount().get(i - 2).getOffenceCount()));
				}
				break;
			case 5:
				if (report.getTotaloffenceCount().get(i - 2).getOffenceCount() >= 0) {
					cellpro.setFieldValue(
							DateConverters.replaceDefaults(report.getTotaloffenceCount().get(i - 2).getOffenceCount()));
				}
				break;
			case 6:
				if (report.getTotaloffenceCount().get(i - 2).getOffenceCount() >= 0) {
					cellpro.setFieldValue(
							DateConverters.replaceDefaults(report.getTotaloffenceCount().get(i - 2).getOffenceCount()));
				}
				break;
			case 7:
				if (report.getTotaloffenceCount().get(i - 2).getOffenceCount() >= 0) {
					cellpro.setFieldValue(
							DateConverters.replaceDefaults(report.getTotaloffenceCount().get(i - 2).getOffenceCount()));
				}
				break;
			case 8:
				if (report.getTotaloffenceCount().get(i - 2).getOffenceCount() >= 0) {
					cellpro.setFieldValue(
							DateConverters.replaceDefaults(report.getTotaloffenceCount().get(i - 2).getOffenceCount()));
				}
				break;
			case 9:
				if (report.getTotaloffenceCount().get(i - 2).getOffenceCount() >= 0) {
					cellpro.setFieldValue(
							DateConverters.replaceDefaults(report.getTotaloffenceCount().get(i - 2).getOffenceCount()));
				}
				break;
			case 10:
				if (report.getGrandTotal() >= 0) {
					cellpro.setFieldValue(DateConverters.replaceDefaults(report.getGrandTotal()));
				}
			default:
				break;
			}
			cells.add(cellpro);
		}
		return cells;
	}

	@Override
	public void generateExcelForRoadSafetyVcrCount(ReportVO report, HttpServletResponse response) {
		List<String> header = new ArrayList<String>();
		ExcelService excel = new ExcelServiceImpl();

		excel.setHeaders(header, "RoadSafetyVcrCount");

		String name = "RoadSafetyVcrCount";
		String fileName = name + ".xlsx";
		String sheetname = "RoadSafetyVcrCount";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename = " + fileName);

		XSSFWorkbook wb = excel.renderData(prepareCellPropsForRoadSafetyVcrCount(header, report), header, fileName,
				sheetname);

		XSSFSheet sheet = wb.getSheet(sheetname);
		sheet.setDefaultColumnWidth(40);

		XSSFCellStyle style = wb.createCellStyle(); // create style
		XSSFFont font = wb.createFont();
		font.setBoldweight(XSSFFont.BOLDWEIGHT_NORMAL);// Make font bold
		font.setColor(HSSFColor.BLACK.index);
		style.setFont(font);// set it to bold

		try {

			ServletOutputStream outputStream = response.getOutputStream();
			wb.write(outputStream);
			outputStream.flush();
			outputStream.close();

		} catch (IOException e) {
			logger.error(" excel retrive AllRecords download IOException : {}", e.getMessage());

		}
	}

	private List<List<CellProps>> prepareCellPropsForRoadSafetyVcrCount(List<String> header, ReportVO report) {
		int slNo = 0;

		List<List<CellProps>> cell = new ArrayList<>();

		for (ReportVO report2 : report.getReport().stream().findFirst().get().getCcpPermit()) {
			if (report2 != null) {
				List<CellProps> cells = new ArrayList<>();
				for (int i = 0; i < header.size(); i++) {
					CellProps cellpro = new CellProps();
					switch (i) {
					case 0:
						cellpro.setFieldValue(String.valueOf(++slNo));
						break;
					case 1:
						if (report2.getVcrNo() != null) {
							cellpro.setFieldValue(DateConverters.replaceDefaults(report2.getVcrNo()));
						}
						break;
					case 2:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report2.getOffenceDesc()));
						break;
					case 3:
						cellpro.setFieldValue(DateConverters.replaceDefaults(report2.getDateOfCheck()));
					default:
						break;
					}
					cells.add(cellpro);
				}
				cell.add(cells);
			}
		}
		return cell;
	}

	@Override
	public RegServiceVO getTPDataForIncomeAndOutGoing(String prNo) {

		List<PermitDetailsDTO> permitList = permitDetailsDAO.findByPrNoAndPermitStatus(prNo,
				PermitsEnum.ACTIVE.getDescription());
		if (permitList == null || permitList.isEmpty()) {
			logger.error("No permit details found for vehicle number: " + prNo);
			throw new BadRequestException("No permit details found for vehicle number: " + prNo);
		}
		permitList.stream().sorted((p1, p2) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
		List<PermitDetailsDTO> primaryPermit = permitList.stream().filter(
				one -> one.getPermitType().getTypeofPermit().equalsIgnoreCase(PermitType.PRIMARY.getPermitTypeCode()))
				.collect(Collectors.toList());
		if (primaryPermit == null || primaryPermit.isEmpty()) {
			logger.error("Primary permit details found for vehicle number: " + prNo);
			throw new BadRequestException("Primary details found for vehicle number: " + prNo);
		}
		List<PermitDetailsDTO> tPList = new ArrayList<>();
		permitList.stream().forEach(one -> {

			if (one.getPermitType().getTypeofPermit().equalsIgnoreCase(PermitType.TEMPORARY.getPermitTypeCode())
					&& one.getPermitValidityDetails() != null
					&& one.getPermitValidityDetails().getPermitValidTo() != null
					&& (one.getPermitValidityDetails().getPermitValidTo().isAfter(LocalDate.now())
							|| one.getPermitValidityDetails().getPermitValidTo().equals(LocalDate.now()))) {
				tPList.add(one);
			}
		});
		if (tPList == null || tPList.isEmpty()) {
			logger.error("Temporary permit details not found for vehicle number: " + prNo);
			throw new BadRequestException("Temporary permit details not found for vehicle number: " + prNo);
		}
		PermitDetailsVO primaryPermitVo = permitDetailsMapper.convertEntity(primaryPermit.stream().findFirst().get());
		List<PermitDetailsVO> temporaryPermitVoList = permitDetailsMapper.convertListEntity(tPList);
		RegServiceVO result = new RegServiceVO();
		result.setPermitDetailsVO(primaryPermitVo);
		result.setPermitDetailsListVO(temporaryPermitVoList);
		return result;
	}

	@Override
	public PermitUtilizationVO saveTpUtilizationDetails(JwtUser jwtUser, PermitUtilizationVO vo) {

		validationForTpUtilization(vo);
		Optional<PermitDetailsDTO> tpDetails = permitDetailsDAO.findByPermitNoAndPermitTypeTypeofPermitAndPermitStatus(
				vo.getTpno(), PermitType.TEMPORARY.getPermitTypeCode(), PermitsEnum.ACTIVE.getDescription());
		if (tpDetails == null || !tpDetails.isPresent()) {
			logger.error("Temporary details not found for permit no: " + vo.getTpno() + " and permitType is: "
					+ PermitType.TEMPORARY.getDescription());
			throw new BadRequestException("Temporary details not found for permit no: " + vo.getTpno()
					+ " and permitType is: " + PermitType.TEMPORARY.getDescription());
		}
		PermitDetailsDTO dto = tpDetails.get();
		if (!dto.getPrNo().equalsIgnoreCase(vo.getPrNo())) {
			logger.error("Vehicle number " + vo.getPrNo() + " is not belong to this permit number " + "(" + vo.getTpno()
					+ ")");
			throw new BadRequestException("Vehicle number " + vo.getPrNo() + " is not belong to this permit number "
					+ "(" + vo.getTpno() + ")");
		}
		PermitUtilizationDTO utilizationJson = this.setUtilizationDetails(jwtUser, vo);
		if (utilizationJson.getRouteType().equals(RouteType.FORWARD)) {
			if (dto.getRouteDetails().getPermitUtilizationForwardDetails() == null
					|| dto.getRouteDetails().getPermitUtilizationForwardDetails().isEmpty()) {

				dto.getRouteDetails().setPermitUtilizationForwardDetails(Arrays.asList(utilizationJson));
			} else {

				if (dto.getRouteDetails().getPermitUtilizationReturnDetails() != null
						&& !dto.getRouteDetails().getPermitUtilizationReturnDetails().isEmpty()) {
					logger.error(RouteType.RETURN + " details completed for : " + vo.getPrNo());
					throw new BadRequestException(RouteType.RETURN + " details completed for : " + vo.getPrNo());
				}
				dto.getRouteDetails().getPermitUtilizationForwardDetails().add(utilizationJson);
			}
		} else {
			if (dto.getRouteDetails().getPermitUtilizationReturnDetails() == null
					|| dto.getRouteDetails().getPermitUtilizationReturnDetails().isEmpty()) {
				dto.getRouteDetails().setPermitUtilizationReturnDetails(Arrays.asList(utilizationJson));
			} else {
				dto.getRouteDetails().getPermitUtilizationReturnDetails().add(utilizationJson);
			}
		}

		permitDetailsDAO.save(dto);
		PermitUtilizationVO primaryPermitVo = permitUtilizationMapper.convertEntity(utilizationJson);
		return primaryPermitVo;
	}

	private PermitUtilizationDTO setUtilizationDetails(JwtUser jwtUser, PermitUtilizationVO vo) {
		PermitUtilizationDTO utilizationDto = new PermitUtilizationDTO();

		Optional<UserDTO> userDto = userDAO.findByUserId(jwtUser.getUsername());
		utilizationDto.setMviId(userDto.get().getUserId());
		utilizationDto.setMviOfficeCode(userDto.get().getOffice().getOfficeCode());
		Optional<OfficeDTO> office = officeDAO.findByOfficeCode(userDto.get().getOffice().getOfficeCode()); // userDto.get().getOffice().getOfficeCode()
		utilizationDto.setMviOfficeName(office.get().getOfficeName());
		String userName = "";
		if (StringUtils.isNoneBlank(userDto.get().getFirstName())) {
			userName = userName + userDto.get().getFirstName();
		}
		if (StringUtils.isNoneBlank(userDto.get().getLastName())) {
			userName = userName + userDto.get().getLastName();
		}
		utilizationDto.setMviName(userName);
		utilizationDto.setPlace(vo.getPlace());
		utilizationDto.setUtilizationDate(LocalDateTime.now());
		utilizationDto.setRouteType(vo.getRouteType());
		if (StringUtils.isNoneBlank(vo.getRemarks())) {
			utilizationDto.setRemarks(vo.getRemarks());
		}
		return utilizationDto;
	}

	private void validationForTpUtilization(PermitUtilizationVO vo) {
		if (StringUtils.isBlank(vo.getPrNo())) {
			logger.error("Please provide vehicle number");
			throw new BadRequestException("Please provide vehicle number");
		}
		if (StringUtils.isBlank(vo.getTpno())) {
			logger.error("Please provide Temporary permit number for: " + vo.getPrNo());
			throw new BadRequestException("Please provide Temporary permit number for : " + vo.getPrNo());
		}
		if (StringUtils.isBlank(vo.getPlace())) {
			logger.error("Please provide checking place for : " + vo.getPrNo());
			throw new BadRequestException("Please provide checking place for : " + vo.getPrNo());
		}
		if (vo.getRouteType() == null) {
			logger.error("Please provide route type for : " + vo.getPrNo());
			throw new BadRequestException("Please provide route type for : " + vo.getPrNo());
		}
	}

	private VcrFinalServiceVO validationForVCRSave(VcrFinalServiceVO vo) {

		if (vo.getOffence() == null || vo.getOffence().getOffence() == null || vo.getOffence().getOffence().isEmpty()) {
			logger.error("Please select atleast one offence");
			throw new BadRequestException("Please select atleast one offence");
		}

		List<OffenceVO> offenceList = new ArrayList<>();

		for (OffenceVO offenceVo : vo.getOffence().getOffence()) {
			if (offenceList == null || offenceList.isEmpty()) {
				offenceList.add(offenceVo);
			} else {
				if (!offenceList.stream()
						.anyMatch(one -> one.getOffenceDescription().equalsIgnoreCase(offenceVo.getOffenceDescription())
								&& one.getClassOfVehicles().equalsIgnoreCase(offenceVo.getClassOfVehicles()))) {
					offenceList.add(offenceVo);
				}
			}
		}
		vo.getOffence().setOffence(offenceList);
		return vo;
	}

	@Override
	public void saveSpeedGunData(SpeedGunVO speedGunVO) {
		if (StringUtils.isBlank(speedGunVO.getOfficerId())) {
			throw new BadRequestException("officer Id should not be empty");
		}
		if (StringUtils.isBlank(speedGunVO.getImageId())) {
			throw new BadRequestException("Image Id should not be empty");
		}
		if (StringUtils.isBlank(speedGunVO.getVehicleImage())) {
			throw new BadRequestException("Image should not be empty");
		}
		if (StringUtils.isBlank(speedGunVO.getRegistrationNo())) {
			throw new BadRequestException("Registration Number should not be empty");
		}

		Optional<SpeedGunDTO> speedGunDto = speedGunVcrDAO.findByImageIdAndOfficerId(speedGunVO.getImageId(),
				speedGunVO.getOfficerId());
		if (speedGunDto.isPresent()) {
			throw new BadRequestException("Speed Gun Data alreday saved with image Id : " + speedGunVO.getImageId()
					+ " and officer " + speedGunVO.getOfficerId());
		}
		Optional<SpeedGunDTO> dto = speedGunVcrDAO.findByCrtdDtBetweenAndOfficerIdAndRegistrationNo(
				paymentReportService.getTimewithDate(LocalDate.now(), Boolean.FALSE),
				paymentReportService.getTimewithDate(LocalDate.now(), Boolean.TRUE), speedGunVO.getOfficerId(),
				speedGunVO.getRegistrationNo());
		if (dto.isPresent()) {
			throw new BadRequestException("violation data already saved against registration number : "
					+ speedGunVO.getRegistrationNo() + " with officer Id : " + speedGunVO.getOfficerId());
		}
		try {
			SpeedGunDTO speedDto = new SpeedGunDTO();
			speedDto.setCrtdDt(LocalDateTime.now());
			speedDto.setDistance(speedGunVO.getDistance());
			speedDto.setDlNo(speedGunVO.getDlNo());
			speedDto.setDriverName(speedGunVO.getDriverName());
			speedDto.setGunDate(LocalDate.parse(speedGunVO.getGunDate()));
			speedDto.setGunTime(speedGunVO.getGunTime());
			speedDto.setImageId(speedGunVO.getImageId());
			speedDto.setLaserSlno(speedGunVO.getLaserSlno());
			speedDto.setLattitude(speedGunVO.getLattitude());
			speedDto.setLocation(speedGunVO.getLocation());
			speedDto.setLongitude(speedGunVO.getLongitude());
			speedDto.setMobileNo(speedGunVO.getMobileNo());
			speedDto.setOfficer(speedGunVO.getOfficer());
			speedDto.setOfficerId(speedGunVO.getOfficerId());
			speedDto.setOtherOffense(speedGunVO.getOtherOffense());
			speedDto.setOverspeedOffences(speedGunVO.getOverspeedOffences());
			speedDto.setRegistrationNo(speedGunVO.getRegistrationNo());
			speedDto.setSpeed(speedGunVO.getSpeed());
			speedDto.setSpeedZone(speedGunVO.getSpeedZone());
			speedDto.setVehicleImage(speedGunVO.getVehicleImage());
			speedGunVcrDAO.save(speedDto);
		} catch (Exception e) {
			throw new BadRequestException("Mandatory fields should not be empty : " + e.getMessage());
		}
	}

	@Override
	public String generateSpeedGunVcr(String imageId, String officerId) {
		Optional<SpeedGunDTO> speedGunDto = speedGunVcrDAO.findByImageIdAndOfficerId(imageId, officerId);
		if (!speedGunDto.isPresent()) {
			throw new BadRequestException("No Record Found");
		}
		SpeedGunDTO speedGun = speedGunDto.get();
		if (StringUtils.isBlank(speedGun.getOfficerId())) {
			throw new BadRequestException("officer Id should not be empty");
		}
		Optional<UserDTO> userdto = userDAO.findByUserId(speedGun.getOfficerId());
		if (!userdto.isPresent()) {
			throw new BadRequestException("Officer Details Not Found");
		}
		UserDTO dto = userdto.get();
		List<RolesDTO> addtionalRoles = new ArrayList<>();
		if (dto.getAdditionalRoles() == null) {
			addtionalRoles.add(dto.getPrimaryRole());
		} else {
			addtionalRoles.addAll(dto.getAdditionalRoles());
		}
		JwtUser userDetails = new JwtUser(dto.getUserId(), dto.getFirstname(), dto.getLastName(), null,
				dto.getPrimaryRole(), addtionalRoles, dto.getOffice().getOfficeCode(), null, false, false, false, null,
				null);
		if (StringUtils.isBlank(speedGun.getRegistrationNo())) {
			throw new BadRequestException("Registration Number Required");
		}
		VehicleDetailsAndPermitDetailsVO regDetails;
		try {
			regDetails = getVehicleDetailsAndPermitDetailsByPrOrTrOrChessisNumber(speedGun.getRegistrationNo(), null,
					null);
			if (regDetails == null) {
				throw new BadRequestException("Record Not Found");
			}
		} catch (ResourceNotFoudException e) {
			throw new BadRequestException(e.getMessage());
		}

		VcrFinalServiceVO vcrFinalVO = new VcrFinalServiceVO();
		VcrVo vcrVO = new VcrVo(); // vcrVO.setDateOfCheck(speedGunVO.getGunDate());
		vcrVO.setPlaceOfCheck(speedGun.getLocation());
		vcrVO.setLatitude(speedGun.getLattitude());
		vcrVO.setLongitude(speedGun.getLongitude());
		vcrVO.setDateOfCheck(getDateAndTime(speedGun.getGunDate(), speedGun.getGunTime()));
		vcrFinalVO.setVcr(vcrVO);

		if (regDetails.getRegDetailsVO() != null) {
			RegistrationVcrVo regVcrVo = new RegistrationVcrVo();

			regVcrVo.setCov(regDetails.getRegDetailsVO().getClassOfVehicle());
			regVcrVo.setBodyTypeDesc(regDetails.getRegDetailsVO().getVahanDetails().getBodyTypeDesc());
			regVcrVo.setChassisNumber(regDetails.getRegDetailsVO().getVahanDetails().getChassisNumber());
			MasterCovVO vo = new MasterCovVO();
			vo.setCovcode(regDetails.getRegDetailsVO().getClassOfVehicle());
			vo.setCategory(regDetails.getRegDetailsVO().getVehicleType());
			vo.setCovdescription(regDetails.getRegDetailsVO().getClassOfVehicleDesc());
			regVcrVo.setClasssOfVehicle(vo);
			regVcrVo.setEngineNumber(regDetails.getRegDetailsVO().getVahanDetails().getEngineNumber());
			// regVcrVo.setFcValidity(fcValidity);
			regVcrVo.setFirstVehicle(regDetails.getRegDetailsVO().getIsFirstVehicle());
			regVcrVo.setFuelDesc(regDetails.getRegDetailsVO().getVahanDetails().getFuelDesc());
			regVcrVo.setGvw(regDetails.getRegDetailsVO().getVahanDetails().getGvw());
			// regVcrVo.setGvwc(gvwc);
			// regVcrVo.setInvoiceAmmount(regDetails.getRegDetailsVO().getInvoiceDetails().getInvoiceValue());
			regVcrVo.setInvoiceValue(regDetails.getRegDetailsVO().getInvoiceDetails().getInvoiceValue());
			regVcrVo.setMakersModel(regDetails.getRegDetailsVO().getVahanDetails().getMakersModel());
			regVcrVo.setOwnerType(regDetails.getRegDetailsVO().getOwnerType());
			// regVcrVo.setPrGeneratedDate(regDetails.getRegDetailsVO().getPrGeneratedDate());
			regVcrVo.setRegNo(regDetails.getRegDetailsVO().getPrNo());
			regVcrVo.setRegApplicationNo(regDetails.getRegDetailsVO().getApplicationNo());
			// regVcrVo.setSeatingCapacity(regDetails.getRegDetailsVO().getVahanDetails().getSeatingCapacity());
			if (regDetails.getRegDetailsVO().getApplicantDetails().getPresentAddress().getState() != null) {
				regVcrVo.setState(regDetails.getRegDetailsVO().getApplicantDetails().getPresentAddress().getState());

			} else {
				regVcrVo.setState(regDetails.getRegDetailsVO().getApplicantDetails().getPermanantAddress().getState());
			}
			regVcrVo.setTaxType(regDetails.getRegDetailsVO().getTaxType());
			// regVcrVo.setTrGeneratedDate(trGeneratedDate);
			// regVcrVo.setTrNo(trNo);
			if (regDetails.getRegDetailsVO().getVehicleType().equalsIgnoreCase("T")) {
				regVcrVo.setTransport(Boolean.TRUE);
			}

			// regVcrVo.setTrValidity(trValidity);
			regVcrVo.setUlw(regDetails.getRegDetailsVO().getVahanDetails().getUnladenWeight());
			vcrFinalVO.setRegistration(regVcrVo);
			OwnerDetailsVo ownerDetails = new OwnerDetailsVo();
			String address = StringUtils.EMPTY;
			if (regDetails.getRegDetailsVO().getApplicantDetails().getPresentAddress() != null) {
				ApplicantAddressVO addressVo = regDetails.getRegDetailsVO().getApplicantDetails().getPresentAddress();
				if (addressVo.getDoorNo() != null) {
					address = addressVo.getDoorNo();
				}
				if (addressVo.getStreetName() != null) {
					address = address + "," + addressVo.getStreetName();
				}
				if (addressVo.getVillage() != null && addressVo.getVillage().getName() != null) {
					address = address + "," + addressVo.getVillage().getName();
				}
				if (addressVo.getTownOrCity() != null) {
					address = address + "," + addressVo.getTownOrCity();
				}
				if (addressVo.getMandal() != null && addressVo.getMandal().getMandalName() != null) {
					address = address + "," + addressVo.getMandal().getMandalName();
				}
				if (addressVo.getDistrict() != null && addressVo.getDistrict().getDistrictName() != null) {
					address = address + "," + addressVo.getDistrict().getDistrictName();
				}
				if (addressVo.getState() != null && addressVo.getState().getStateName() != null) {
					address = address + "," + addressVo.getState().getStateName();
				}
				if (addressVo.getPostOffice() != null && addressVo.getPostOffice().getPostOfficeCode() != null) {
					address = address + "," + addressVo.getPostOffice().getPostOfficeCode();
				}
				ownerDetails.setAddress(address);
			}
			// ownerDetails.setAddress(regDetails.getRegDetailsVO().getApplicantDetails().getPresentAddress().get);
			ownerDetails.setFullName(regDetails.getRegDetailsVO().getApplicantDetails().getFirstName() + " "
					+ regDetails.getRegDetailsVO().getApplicantDetails().getLastName());
			ownerDetails.setMobileNo(regDetails.getRegDetailsVO().getApplicantDetails().getMobile());
			vcrFinalVO.setOwnerDetails(ownerDetails);
		}
		ValidityDetailsVo validityDetails = new ValidityDetailsVo();
		if (regDetails.fcDetails != null) {
			validityDetails.setFcFrom(regDetails.fcDetails.getFcFrom());
			validityDetails.setFcIssuedDate(regDetails.fcDetails.getFcIssuedDate());
			validityDetails.setFcNumber(regDetails.fcDetails.getFcNumber());
			validityDetails.setFcTo(regDetails.fcDetails.getFcTo());
		}
		if (regDetails.permitDetailsVO != null) {
			validityDetails.setPermitFrom(regDetails.permitDetailsVO.getPermitValidityDetailsVO().getPermitValidFrom());
			validityDetails.setPermitNumber(regDetails.permitDetailsVO.getPermitNo());
			validityDetails.setPermitRouteOrGoods(
					regDetails.permitDetailsVO.getRouteDetailsVO().getPermitRouteDetails().getDescription());
			validityDetails.setPermitTO(regDetails.permitDetailsVO.getPermitValidityDetailsVO().getPermitValidTo());
			validityDetails.setTypeOfPermit(regDetails.permitDetailsVO.getPermitType().getPermitType());
		}
		if (regDetails.getTaxDetails() != null) {
			validityDetails.setTaxPaymentDate(regDetails.getTaxDetails().getTaxPaidDate());
			validityDetails.setTaxValidUpto(regDetails.getTaxDetails().getTaxPeriodEnd());
		}
		vcrFinalVO.setValidityDetails(validityDetails);
		DriverDetailsVcrVo driverVo = new DriverDetailsVcrVo();
		driverVo.setDriverLicense(speedGun.getDlNo());
		driverVo.setFullName(speedGun.getDriverName());
		vcrFinalVO.setDriver(driverVo);
		// vcrFinalVO.setSpeedGunVO(speedGun);
		List<String> offenses = new ArrayList<>();
		if (StringUtils.isNotBlank(speedGun.getOverspeedOffences())) {
			offenses.addAll(Arrays.asList(speedGun.getOverspeedOffences().split("\\s*,\\s*")));
		}
		if (StringUtils.isNotBlank(speedGun.getOtherOffense())) {
			offenses.addAll(Arrays.asList(speedGun.getOtherOffense().split("\\s*,\\s*")));
		}
		List<OffenceVO> offensesList = offenceService.findeListOfOffencesBasedOnCOVandCategoryWeight(
				regDetails.getRegDetailsVO().getClassOfVehicle(), regDetails.getRegDetailsVO().getVehicleType(), " ");
		if (!CollectionUtils.isEmpty(offensesList)) {
			OffenceVcrVO offensesVcr = new OffenceVcrVO();
			List<OffenceVO> vo = new ArrayList<>();
			offensesList.stream().forEach(action -> {
				offenses.stream().forEach(list -> {
					if (action.getOffenceDescription().equalsIgnoreCase(list)) {
						vo.add(action);
					}
				});

			});
			offensesVcr.setOffence(vo);
			vcrFinalVO.setOffence(offensesVcr);
		}
		try {
			FromVcrVo fromVO = new FromVcrVo();
			// fromVO.setDistrict((regDetails.getRegDetailsVO().getApplicantDetails().getPresentAddress().getDistrict().);
			StateVO stateVO = new StateVO();
			stateVO.setStateId(vcrFinalVO.getRegistration().getState().getStateId());
			fromVO.setState(stateVO);
			ToVcrVO toVO = new ToVcrVO();
			// toVO.setDistrict(district);
			toVO.setState(stateVO);
			VehicleProceedingVO vo = new VehicleProceedingVO();
			vo.setFrom(fromVO);
			vo.setTo(toVO);
			vcrFinalVO.setVehicleProceeding(vo);
			vcrFinalVO.setImageId(imageId);
			vcrFinalVO.setOfficerId(officerId);
			vcrFinalVO.setIsSpeedGun(Boolean.TRUE);
			VcrFinalServiceVO vcrNo = saveVcrData(Optional.of(vcrFinalVO), userDetails, null);
			return vcrNo.getVcr().getVcrNumber();
		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());
		}
	}

	private VcrFinalServiceVO saveVcrData(Optional<VcrFinalServiceVO> inputOptional, JwtUser jwtUser,
			MultipartFile[] file) throws ResourceNotFoudException, Exception {

		String no;
		if (StringUtils.isNoneBlank(inputOptional.get().getRegistration().getRegNo())) {
			no = inputOptional.get().getRegistration().getRegNo();

		} else if (StringUtils.isNoneBlank(inputOptional.get().getRegistration().getTrNo())) {
			no = inputOptional.get().getRegistration().getTrNo();
		} else {
			no = inputOptional.get().getRegistration().getChassisNumber();
		}
		synchronized (no.intern()) {
			List<StateDTO> stateDto = stateDAO.findByStateIdIn(getListOfStateId(inputOptional));

			if (inputOptional.get().getRegistration().isOtherState()
					&& !compareStateID(stateDto, inputOptional.get().getRegistration().getState().getStateId())) {

				logger.error("please pass valid State: [{}]",
						inputOptional.get().getRegistration().getState().getStateId());
				throw new ResourceNotFoudException(
						"please pass valid State:-" + inputOptional.get().getRegistration().getState().getStateId());
			}

			/*
			 * if (!compareStateID(stateDto,
			 * inputOptional.get().getVehicleProceeding().getFrom().getState().getStateId().
			 * trim()) || !compareStateID(stateDto,
			 * inputOptional.get().getVehicleProceeding().getTo().getState().getStateId().
			 * trim())) { logger.warn("State not valid under vehicleProcedding"); throw new
			 * ResourceNotFoudException("State not be empty"); }
			 */
			if (!masterCovDAO
					.existsByCovcode(inputOptional.get().getRegistration().getClasssOfVehicle().getCovcode().trim())) {
				logger.error("class of vehicle required");
				throw new ResourceNotFoudException("class of vehicle required");
			}
			List<KeyValue<String, List<ImageEnclosureDTO>>> enclosure = null;

			VcrFinalServiceVO voFinalServiceVO = this.validationForVCRSave(inputOptional.get());
			if (voFinalServiceVO.getImageInput() != null && !voFinalServiceVO.getImageInput().isEmpty()
					&& file.length > 0) {

				if (voFinalServiceVO.getImageInput().size() < 3) {
					voFinalServiceVO.getImageInput().get(0).setFileOrder(0);
					if (voFinalServiceVO.getImageInput().size() == 2) {
						voFinalServiceVO.getImageInput().get(1).setFileOrder(1);
					}
				}
				if (voFinalServiceVO.getIsWebVcr() != null) {
					voFinalServiceVO.getImageInput().get(0).setEnclosureName("image");

					voFinalServiceVO.getImageInput().get(0).setType("otherSection1");

				}
				enclosure = this.imageSaveReq(Boolean.TRUE, voFinalServiceVO.getImageInput(),
						voFinalServiceVO.getRegistration().getRegNo(), file, "DONE");

				if (enclosure.isEmpty()) {
					logger.error("enclosure found as empty");
					throw new Exception("enclosure found as empty");
				}
			}
			VcrFinalServiceDTO dto = finalServiceMapper.convertVoToDto(voFinalServiceVO, enclosure, jwtUser);
			String regApplicationNo = this.getRegApplicationNo(dto);
			dto.getRegistration().setRegApplicationNo(regApplicationNo);
			dto.setMviOfficeCode(jwtUser.getOfficeCode());
			if (dto.getSeizedAndDocumentImpounded() != null
					&& dto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO() != null
					&& dto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().getDateOfSeized() != null) {
				dto.setVehicleSeized(Boolean.TRUE);
			}
			List<VcrFinalServiceDTO> listOfVcrs = null;
			if (StringUtils.isNoneBlank(dto.getRegistration().getRegNo())) {
				listOfVcrs = finalServiceDAO.findFirst10ByCreatedByAndRegistrationRegNoOrderByCreatedDateDesc(
						jwtUser.getUsername(), dto.getRegistration().getRegNo());
			} else if (StringUtils.isNoneBlank(dto.getRegistration().getTrNo())) {
				listOfVcrs = finalServiceDAO.findFirst10ByCreatedByAndRegistrationTrNoOrderByCreatedDateDesc(
						jwtUser.getUsername(), dto.getRegistration().getTrNo());
			} else if (StringUtils.isNoneBlank(dto.getRegistration().getChassisNumber())) {
				listOfVcrs = finalServiceDAO.findFirst10ByCreatedByAndRegistrationChassisNumberOrderByCreatedDateDesc(
						jwtUser.getUsername(), dto.getRegistration().getChassisNumber());
			}
			if (listOfVcrs != null && !listOfVcrs.isEmpty()) {
				for (VcrFinalServiceDTO vcr : listOfVcrs) {
					if (vcr.getCreatedDate().toLocalDate().equals(LocalDate.now())
							&& (vcr.getCreatedDate().plusHours(1).equals(LocalDateTime.now())
									|| vcr.getCreatedDate().plusHours(1).isAfter(LocalDateTime.now()))) {
						voFinalServiceVO.getOffence().getOffence().forEach(offence -> {
							if (vcr.getOffence().getOffence().stream().anyMatch(id -> id.getOffenceDescription()
									.equalsIgnoreCase(offence.getOffenceDescription()))) {
								logger.error(offence.getOffenceDescription()
										+ " is already booked for this vehicle by user: " + jwtUser.getUsername());
								throw new BadRequestException(offence.getOffenceDescription()
										+ " is already booked for this vehicle by user: " + jwtUser.getUsername());
							}
						});

					}
				}
			}
			RtaActionVO actionActionVo = new RtaActionVO();
			voFinalServiceVO.getOffence().getOffence().forEach(offence -> {
				if (offence.getIsRoadSafety() != null && offence.getIsRoadSafety()) {
					dto.setIsRoadSafety(Boolean.TRUE);
				}
				if (offence.isBasedOnCFX()
						&& dto.getRegistration().getClasssOfVehicle().getCategory()
								.equalsIgnoreCase(CovCategory.T.getCode())
						&& !dto.getRegistration().isOtherState()
						&& StringUtils.isNoneBlank(dto.getRegistration().getRegNo())
						&& StringUtils.isNoneBlank(dto.getRegistration().getRegApplicationNo())) {
					dto.setCfxIssued(Boolean.TRUE);
					if (StringUtils.isBlank(offence.getDlNo()) || StringUtils.isBlank(offence.getDriverName())
							|| StringUtils.isBlank(offence.getPlaceOfCheck())
							|| StringUtils.isBlank(offence.getDestination())
							|| StringUtils.isBlank(offence.getMaxSpeed())
							|| StringUtils.isBlank(offence.getDefectsNoticed())) {
						logger.error("Please provide CFX related information");
						throw new BadRequestException("Please provide CFX related information");
					}
					actionActionVo.setDlNumber(offence.getDlNo());
					actionActionVo.setDriverName(offence.getDriverName());
					actionActionVo.setPlaceOfChecking(offence.getPlaceOfCheck());
					actionActionVo.setVcrNumber(dto.getVcr().getVcrNumber());
					actionActionVo.setDestination(offence.getDestination());
					actionActionVo.setMaxSpeed(offence.getMaxSpeed());
					actionActionVo.setDefectComment(offence.getDefectsNoticed());
					actionActionVo.setApplicationNo(dto.getRegistration().getRegApplicationNo());
					actionActionVo.setSelectedRole(RoleEnum.MVI.getName());
					if (offence.isAccident()) {
						actionActionVo.setCfxType(FcValidityTypesEnum.CfxType.ACCIDENT);
					} else {
						actionActionVo.setCfxType(FcValidityTypesEnum.CfxType.CFXENDORES);
					}
					registratrionServicesApprovals.savecfxDetails(actionActionVo, jwtUser);
				}
			});
			List<VcrFinalServiceDTO> vcrList = new ArrayList<>();
			ApplicationSearchVO applicationSearchVO = new ApplicationSearchVO();
			applicationSearchVO.setChassisNo(dto.getRegistration().getChassisNumber());
			applicationSearchVO.setRequestFromAO(Boolean.TRUE);
			dto.setSaveDoc(Boolean.TRUE);
			CitizenSearchReportVO outPut = new CitizenSearchReportVO();
			vcrList = regService.getTotalVcrs(applicationSearchVO, vcrList);

			if (vcrList != null && !vcrList.isEmpty()) {
				vcrList = regService
						.getVcrDetails(Arrays.asList(vcrList.stream().findFirst().get().getVcr().getVcrNumber()), true,false);
				vcrList.add(dto);
			} else {
				vcrList.add(dto);
			}
			regService.getVcrAmount(applicationSearchVO, vcrList, outPut);

			List<VcrFinalServiceVO> listvo = outPut.getVcrList();
			VcrFinalServiceVO vo = new VcrFinalServiceVO();
			for (VcrFinalServiceVO vcrVo : listvo) {
				if (vcrVo.getSaveDoc() != null && vcrVo.getSaveDoc()) {
					dto.setOffencetotal(vcrVo.getOffencetotal());
					vo = vcrVo;
				}
			}
			dto.setSaveDoc(null);
			dto.getVcr().setVcrNumber(finalServiceMapper.getVcrNumber(jwtUser.getOfficeCode()));
			Optional<UserDTO> useDto = userDAO.findByUserId(jwtUser.getUsername());
			dto.setUsername(useDto.get().getFirstName());
			if (useDto.get().getDesignation() != null
					&& StringUtils.isNoneBlank(useDto.get().getDesignation().getDesigName())) {
				dto.setDesignation(useDto.get().getDesignation().getDesigName());
			}
			vo.getVcr().setVcrNumber(dto.getVcr().getVcrNumber());
			if (StringUtils.isNoneBlank(dto.getRegistration().getRegNo()) && !dto.getRegistration().isOtherState()) {
				Sort sort = new Sort(new Sort.Order(Direction.DESC, "lUpdate"));
				List<RegistrationDetailsDTO> regDto = registrationDetailDAO
						.findByPrNoNative(dto.getRegistration().getRegNo(), sort);
				if (CollectionUtils.isNotEmpty(regDto)) {
					if (CollectionUtils.isNotEmpty(regDto)) {
						Optional<OfficeDTO> office = officeDAO
								.findByOfficeCodeAndIsActiveTrue(regDto.get(0).getOfficeDetails().getOfficeCode());
						if (office.isPresent()) {
							dto.setOfficeCode(regDto.get(0).getOfficeDetails().getOfficeCode());
						}
					}
				}
				FreezeVehiclsDTO freezedto = freezeVehiclsDAO.findByPrNoIn(dto.getRegistration().getRegNo());
				if (freezedto != null) {
					freezedto.getPrNo().remove(dto.getRegistration().getRegNo());
					freezeVehiclsDAO.save(freezedto);
				}
			}
			if (voFinalServiceVO.getIsWebVcr() != null) {
				dto.setIseVcr(voFinalServiceVO.getIsWebVcr());
			}
			if (voFinalServiceVO.getIsSpeedGun() != null) {
				/*
				 * Optional<VcrFinalServiceDTO> vcrDto =
				 * finalServiceDAO.findBySpeedGunDTOImageIdAndSpeedGunDTOOfficerId(
				 * voFinalServiceVO.getImageId(),voFinalServiceVO.getOfficerId());
				 * if(vcrDto.isPresent()) { throw new
				 * BadRequestException("vcr already generated"); }
				 */
				Optional<SpeedGunDTO> speedGunDto = speedGunVcrDAO
						.findByImageIdAndOfficerId(voFinalServiceVO.getImageId(), voFinalServiceVO.getOfficerId());
				if (!speedGunDto.isPresent()) {
					throw new BadRequestException("No Record Found");
				}
				speedGunDto.get().setSmsSendDt(LocalDateTime.now());
				speedGunDto.get().setVcrGenDt(LocalDateTime.now());
				if (dto.getVcr() != null) {
					// dto.getVcr().setDateOfCheck(speedGunDto.get().getGunDate());
					dto.getVcr().setLatitude(speedGunDto.get().getLattitude());
					dto.getVcr().setLongitude(speedGunDto.get().getLongitude());
				}
				dto.setSpeedGunDTO(speedGunDto.get());
				dto.setIsSpeedGun(Boolean.TRUE);

			}
			if (StringUtils.isNotBlank(voFinalServiceVO.getSenderMobileNo())) {
				dto.setSenderMobileNo(voFinalServiceVO.getSenderMobileNo());
				dto.setSenderDate(voFinalServiceVO.getSenderDate());
			}

			VcrFinalServiceDTO response = finalServiceDAO.save(dto);
			logger.debug("data saving in to collection by vcrNumber ..[{}]", response.getVcr().getVcrNumber());
			if (response.getIseVcr() == null) {
				sendNotifications(1, response);
			} else {
				sendNotifications(2, response);
			}

			if (regApplicationNo != null && StringUtils.isNoneBlank(dto.getRegistration().getRegNo())) {
				RegistrationDetailsDTO regDoc = getRegDoc(dto.getRegistration().getRegNo());
				if (regDoc != null && regDoc.getOfficeDetails() != null) {
					vo.setOfficeDetails(officeMapper.convertEntity(regDoc.getOfficeDetails()));
				}
			}
			vo.setShouldNotAllowForPayCash(registrationService.shouldNotAllowForPayCash(dto));
			if (dto.getTaxAmountForPrint() != null) {
				regService.setTaxForPrint(vo, dto.getTaxAmountForPrint());
			}
			return vo;
		}

	}

	@Override
	public RegReportVO vcrPaidReport(VcrVo vo, JwtUser jwtUser, Pageable page) {

		return getMviBasedPaidVcrDetails(vo, jwtUser, page);

	}

	private RegReportVO getMviBasedPaidVcrDetails(VcrVo vo, JwtUser jwtUser, Pageable page) {

		if (vo.getFromDate() == null && vo.getToDate() == null) {
			throw new BadRequestException("From Date / To Date is required");
		}
		RegReportVO regReport = new RegReportVO();
		List<String> distinctMVI = new ArrayList<>();
		String officeCode = jwtUser.getOfficeCode();
		if (StringUtils.isEmpty(officeCode)) {
			throw new BadRequestException("Office Code not found");
		}
		if (RoleEnum.roleList().contains(vo.getRole())) {
			if (StringUtils.isNotBlank(vo.getMviName()) && vo.getMviName().equals("ALL")) {
				Optional<OfficeDTO> officeOpt = officeDAO.findByOfficeCode(officeCode);
				if (officeOpt.isPresent()) {
					Integer distId = officeOpt.get().getDistrict();
					distinctMVI = getMVIforDistrict(distId);
				}
			} else if (StringUtils.isNotBlank(vo.getMviName())) {
				distinctMVI = Arrays.asList(vo.getMviName());
			} else {
				distinctMVI = Arrays.asList(jwtUser.getId());
			}
			regReport.setVcrReport(getPaidVcrData(vo, distinctMVI, page));
			if (vo.getPageNo() + 1 == vo.getTotalPage()) {
				List<VcrFinalServiceDTO> vcrDataList = finalServiceDAO.findByPaidDateBetweenAndCreatedByIn(
						getTimewithDate(vo.getFromDate(), false), getTimewithDate(vo.getToDate(), true), distinctMVI);
				List<VcrFinalServiceVO> grandTotals = finalServiceMapper.convertLimited(vcrDataList);

				Integer cftotal = 0;
				Double taxTotal = 0d;
				Double taxArrears = 0d;
				Long penalityTotal = 0l;
				Long penalityArrearsTotal = 0l;
				Long serviceFeeTotal = 0l;
				Double total = 0d;

				for (VcrFinalServiceVO grandTotal : grandTotals) {
					cftotal = cftotal + grandTotal.getCompoundFee();
					taxTotal = taxTotal + grandTotal.getTax();
					taxArrears = taxArrears + grandTotal.getTaxArrears();
					penalityTotal = penalityTotal + grandTotal.getPenalty();
					penalityArrearsTotal = penalityArrearsTotal + grandTotal.getPenaltyArrears();
					if (grandTotal.getServiceFee() != null) {
						serviceFeeTotal = serviceFeeTotal + grandTotal.getServiceFee();
					}
					total = total + grandTotal.getTotal();
				}
				regReport.setGrandTotal(total);
				regReport.setTaxTotal(taxTotal);
				regReport.setTaxArrears(taxArrears);
				regReport.setCfTotal(cftotal);
				regReport.setPenalityTotal(penalityTotal);
				regReport.setPenalityArrearsTotal(penalityArrearsTotal);
				regReport.setServiceFeeTotal(serviceFeeTotal);
			}

		} else {

			if (StringUtils.isNotBlank(vo.getMviName())) {
				if (vo.getMviName().equals("ALL")) {
					Optional<OfficeDTO> officeOpt = officeDAO.findByOfficeCode(officeCode);
					if (officeOpt.isPresent()) {
						Integer distId = officeOpt.get().getDistrict();
						distinctMVI = getMVIforDistrict(distId);
					} else
						throw new BadRequestException("No office Data found office: " + officeCode);
				} else {
					distinctMVI = Arrays.asList(vo.getMviName());
				}
			} else {
				if ((vo.getType() != null && vo.getType().equalsIgnoreCase("paidDate")
						&& StringUtils.isNotBlank(vo.getOfficeCode()) && !vo.getOfficeCode().equals("ALL")
						&& StringUtils.isNotBlank(vo.getRole()))
						&& (vo.getRole().equalsIgnoreCase(RoleEnum.DTC.getName())
								|| vo.getRole().equalsIgnoreCase(RoleEnum.STA.getName()))) {
					officeCode = vo.getOfficeCode();
				}
				Optional<OfficeDTO> officeOpt = officeDAO.findByOfficeCode(officeCode);
				if (officeOpt.isPresent()) {
					Integer distId = officeOpt.get().getDistrict();
					distinctMVI = getMVIforDistrict(distId);
				} else
					throw new BadRequestException("No office Data found office: " + officeCode);
			}
			regReport.setVcrReport(getPaidVcrData(vo, distinctMVI, page));

		}
		regReport.setPageNo(vo.getPageNo());
		regReport.setTotalPage(vo.getTotalPage());
		List<UserVO> userVOList = mviUserIdsAndNamesMapping(distinctMVI);
		regReport.setMviUsers(userVOList);
		regReport.setMvi(distinctMVI);
		return regReport;

	}

	private List<VcrFinalServiceVO> getPaidVcrData(VcrVo vo, List<String> distinctMVI, Pageable page) {
		getTimewithDate(vo.getFromDate(), false);
		Page<VcrFinalServiceDTO> vcrPage = null;
		List<VcrFinalServiceDTO> vcrList = null;
		if (StringUtils.isNotBlank(vo.getType()) && vo.getType().equalsIgnoreCase("paidDate")
				&& StringUtils.isNotBlank(vo.getMviName()) && vo.getMviName().equalsIgnoreCase("ALL")) {
			List<UserWiseEODCount> mviWiseCount = getVcrMviWisePaidCount(getTimewithDate(vo.getFromDate(), false),
					getTimewithDate(vo.getToDate(), true), distinctMVI);
			if (CollectionUtils.isNotEmpty(mviWiseCount)) {
				List<VcrFinalServiceVO> finalVO = new ArrayList<>();
				VcrFinalServiceVO finalDto = new VcrFinalServiceVO();
				Long grandTotal = 0L;
				List<UserWiseEODCount> count = new ArrayList<>();
				for (UserWiseEODCount user : mviWiseCount) {
					grandTotal = grandTotal + user.getCount();
					count.add(user);
				}
				UserWiseEODCount countVo = new UserWiseEODCount();
				countVo.setUserName("TOTAL");
				countVo.setCount(grandTotal);
				count.add(countVo);
				finalDto.setMviCounts(count);
				finalVO.add(finalDto);
				return finalVO;
			}
		} else {
			vcrPage = finalServiceDAO.findByPaidDateBetweenAndCreatedByIn(getTimewithDate(vo.getFromDate(), false),
					getTimewithDate(vo.getToDate(), true), distinctMVI, page.previousOrFirst());
		}

		if ((vcrPage != null && vcrPage.hasContent()) || (vcrList != null && !vcrList.isEmpty())) {
			if (vcrPage != null && vcrPage.hasContent()) {
				vcrList = vcrPage.getContent();
				vo.setPageNo(vcrPage.getNumber());
				vo.setTotalPage(vcrPage.getTotalPages());

			}
			if ((vcrList != null && !vcrList.isEmpty() && vo.getType() != null
					&& vo.getType().equalsIgnoreCase("paidDate") && StringUtils.isNotBlank(vo.getOfficeCode())
					&& !vo.getOfficeCode().equals("ALL"))) {

				if (vcrList != null && !vcrList.isEmpty()) {
					vo.setPageNo(0);
					Double doublePages = Math.ceil(vcrList.size() / 20f);
					vo.setTotalPage(doublePages.longValue());
				}
			}

		}
		if (CollectionUtils.isEmpty(vcrList)) {
			throw new BadRequestException(
					"No Vcr Data Found for Dates B/w " + vo.getFromDate() + " and " + vo.getToDate());
		}

		List<VcrFinalServiceDTO> vcrFilterList = filterVcrByStatus(vo, vcrList);

		if (CollectionUtils.isNotEmpty(vcrFilterList)) {
			List<VcrFinalServiceVO> vcrDataList = finalServiceMapper.convertLimited(vcrFilterList);
			vcrDataList.forEach(item -> {
				if (officeNameWithCodeMap.get(item.getOfficeCode()) != null) {
					item.setOfficeCode(officeNameWithCodeMap.get(item.getOfficeCode()));
				}
			});
			vcrDataList.sort((p1, p2) -> p1.getIssuedBy().compareTo(p2.getIssuedBy()));
			return vcrDataList;
		}
		return Collections.emptyList();
	}

	private List<UserWiseEODCount> getVcrMviWisePaidCount(LocalDateTime fromDate, LocalDateTime toDate,
			List<String> mviNames) {

		Aggregation agg = newAggregation(
				match(Criteria.where("paidDate").gte(fromDate).lte(toDate).and("createdBy").in(mviNames).and("officeCode").in(officeCodes)),
				group("createdBy").count().as("count"), project("count").and("userName").previousOperation());

		AggregationResults<UserWiseEODCount> groupResults = mongoTemplate.aggregate(agg, VcrFinalServiceDTO.class,
				UserWiseEODCount.class);

		if (!groupResults.getMappedResults().isEmpty()) {
			return groupResults.getMappedResults();
		}
		return Collections.EMPTY_LIST;
	}

	public LocalDateTime getDateAndTime(LocalDate date, String time) {
		if (date == null || StringUtils.isBlank(time)) {
			throw new BadRequestException("Date input not available");
		}
		String dateVal = date + "T" + time + ".000Z";

		ZonedDateTime zdt = ZonedDateTime.parse(dateVal);
		return zdt.toLocalDateTime();
	}
}
