
package org.epragati.regservice.impl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.aadhaar.AadhaarDetailsRequestVO;
import org.epragati.aadhaar.AadhaarRequestVO;
import org.epragati.aadhaar.AadharDetailsResponseVO;
import org.epragati.aadhaarAPI.AadhaarSourceDTO;
import org.epragati.aadhaarAPI.AptsAadhaarResponseMapper;
import org.epragati.actions.dao.SuspensionDAO;
import org.epragati.actions.dto.RCActionsDTO;
import org.epragati.availabilitycofig.dao.ServiceAvailabilityConfigDAO;
import org.epragati.availabilitycofig.dto.ServiceAvailabilityConfigDTO;
import org.epragati.cfst.vcr.dao.VcrDetailsDAO;
import org.epragati.cfst.vcr.dao.VcrDetailsDto;
import org.epragati.cfst.vcr.dao.VcrTaxPaidMapper;
import org.epragati.cfstVcr.vo.TaxPaidVCRDetailsVO;
import org.epragati.cfstVcr.vo.VcrBookingData;
import org.epragati.cfstVcr.vo.VcrDetailsVO;
import org.epragati.cfstVcr.vo.VcrInputVo;
import org.epragati.cfstVcr.vo.VcrTaxDetails;
import org.epragati.common.dao.ErrorTrackLogDAO;
import org.epragati.common.dao.PropertiesDAO;
import org.epragati.common.dto.ErrorTrackLogDTO;
import org.epragati.common.dto.PropertiesDTO;
import org.epragati.constants.AadhaarConstants;
import org.epragati.constants.AlterationTypeEnum;
import org.epragati.constants.CovCategory;
import org.epragati.constants.EnclosureType;
import org.epragati.constants.MessageKeys;
import org.epragati.constants.NationalityEnum;
import org.epragati.constants.OwnerType;
import org.epragati.constants.OwnerTypeEnum;
import org.epragati.constants.PurposeEnum;
import org.epragati.constants.TransferType;
import org.epragati.dao.enclosure.CitizenEnclosuresDAO;
import org.epragati.dispatcher.dao.DispatcherSubmissionDAORepo;
import org.epragati.dispatcher.dto.DispatcherSubmissionDTO;
import org.epragati.dto.enclosure.CitizenEnclosuresDTO;
import org.epragati.dto.enclosure.ImageActionsDTO;
import org.epragati.dto.enclosure.ImageEnclosureDTO;
import org.epragati.exception.BadRequestException;
import org.epragati.exception.RcValidationException;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dao.AadhaarResponseDAO;
import org.epragati.master.dao.AadharDropDAO;
import org.epragati.master.dao.AdhaarFamilyDAO;
import org.epragati.master.dao.AlterationDAO;
import org.epragati.master.dao.ApplicantDetailsDAO;
import org.epragati.master.dao.AuctionDetailsDAO;
import org.epragati.master.dao.BileteralTaxDAO;
import org.epragati.master.dao.BodyTypeDAO;
import org.epragati.master.dao.ClassOfVehicleConversionDAO;
import org.epragati.master.dao.CountryDAO;
import org.epragati.master.dao.DealerRegDAO;
import org.epragati.master.dao.DistrictDAO;
import org.epragati.master.dao.EductaionInstituteVehicleDetailsDao;
import org.epragati.master.dao.EnclosuresDAO;
import org.epragati.master.dao.FcDetailsDAO;
import org.epragati.master.dao.FeeCorrectionDAO;
import org.epragati.master.dao.FinalTaxHelperDAO;
import org.epragati.master.dao.FuelConversionDAO;
import org.epragati.master.dao.GeneratedPrDetailsDAO;
import org.epragati.master.dao.MandalDAO;
import org.epragati.master.dao.MasterCovDAO;
import org.epragati.master.dao.MasterFeedBackQuestionsDAO;
import org.epragati.master.dao.MasterNewGoTaxDetailsDAO;
import org.epragati.master.dao.MasterPayperiodDAO;
import org.epragati.master.dao.MasterQuaterPeriodDAO;
import org.epragati.master.dao.MasterTaxExcemptionsDAO;
import org.epragati.master.dao.MasterUsersDAO;
import org.epragati.master.dao.MasterWeightsForAltDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.OfficeSlotsAvailabilityDAO;
import org.epragati.master.dao.RegServiceApprovedDAO;
import org.epragati.master.dao.RegServiceDAO;
import org.epragati.master.dao.RegServicesFeedBackDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.SeatConversionDAO;
import org.epragati.master.dao.ServicesDAO;
import org.epragati.master.dao.ServicesInvalidDetailsDAO;
import org.epragati.master.dao.StagingRegistrationDetailsDAO;
import org.epragati.master.dao.StateDAO;
import org.epragati.master.dao.TaxDetailsDAO;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dao.VehicleStoppageDetailsDAO;
import org.epragati.master.dto.AadhaarDetailsResponseDTO;
import org.epragati.master.dto.AadharDropListDTO;
import org.epragati.master.dto.AdhaarFamilyDTO;
import org.epragati.master.dto.ApplicantDetailsDTO;
import org.epragati.master.dto.AuctionDetailsDTO;
import org.epragati.master.dto.AuctionVehicleDetailsDTO;
import org.epragati.master.dto.BodyTypeDTO;
import org.epragati.master.dto.ContactDTO;
import org.epragati.master.dto.CountryDTO;
import org.epragati.master.dto.DealerRegDTO;
import org.epragati.master.dto.DistrictDTO;
import org.epragati.master.dto.EductaionInstituteVehicleDetailsDto;
import org.epragati.master.dto.EnclosuresDTO;
import org.epragati.master.dto.FcDetailsDTO;
import org.epragati.master.dto.FeeCorrectionDTO;
import org.epragati.master.dto.FinalTaxHelper;
import org.epragati.master.dto.GeneratedPrDetailsDTO;
import org.epragati.master.dto.InsuranceDetailsDTO;
import org.epragati.master.dto.InvoiceDetailsDTO;
import org.epragati.master.dto.MandalDTO;
import org.epragati.master.dto.MasterCovDTO;
import org.epragati.master.dto.MasterFeedBackQuestionsDTO;
import org.epragati.master.dto.MasterNewGoTaxDetails;
import org.epragati.master.dto.MasterPayperiodDTO;
import org.epragati.master.dto.MasterQuaterPeriodDTO;
import org.epragati.master.dto.MasterTaxExcemptionsDTO;
import org.epragati.master.dto.MasterUsersDTO;
import org.epragati.master.dto.MasterWeightsForAlt;
import org.epragati.master.dto.MemberDetails;
import org.epragati.master.dto.OffenceDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.ServicesDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.dto.StateDTO;
import org.epragati.master.dto.TaxComponentDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.master.dto.TaxHelper;
import org.epragati.master.dto.TrailerChassisDetailsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.dto.VahanDetailsDTO;
import org.epragati.master.dto.VillageDTO;
import org.epragati.master.mappers.AadhaarDetailsResponseMapper;
import org.epragati.master.mappers.AadharDropDownMapper;
import org.epragati.master.mappers.ApplicantAddressMapper;
import org.epragati.master.mappers.BodyTypeMapper;
import org.epragati.master.mappers.DistrictMapper;
import org.epragati.master.mappers.FcDetailsMapper;
import org.epragati.master.mappers.InsuranceDetailsMapper;
import org.epragati.master.mappers.MandalMapper;
import org.epragati.master.mappers.MasterFeedBackQuestionsMapper;
import org.epragati.master.mappers.MasterWeightsForAltMapper;
import org.epragati.master.mappers.OfficeMapper;
import org.epragati.master.mappers.OwnershipMapper;
import org.epragati.master.mappers.PermitDetailsMapper;
import org.epragati.master.mappers.RCCancellationMapper;
import org.epragati.master.mappers.RegistrationDetailsMapper;
import org.epragati.master.mappers.RepresentativeMapper;
import org.epragati.master.mappers.UserMapper;
import org.epragati.master.service.CovService;
import org.epragati.master.service.LogMovingService;
import org.epragati.master.service.MandalService;
import org.epragati.master.service.PermitsService;
import org.epragati.master.service.SlotsService;
import org.epragati.master.service.StagingBodyBuilderService;
import org.epragati.master.vo.AadharDropListVO;
import org.epragati.master.vo.AadharReqServiceIdsVO;
import org.epragati.master.vo.ApplicantAddressVO;
import org.epragati.master.vo.ApplicantDetailsVO;
import org.epragati.master.vo.BodyTypeVO;
import org.epragati.master.vo.FinanceDetailsVO;
import org.epragati.master.vo.InsuranceDetailsVO;
import org.epragati.master.vo.InsuranceResponseVO;
import org.epragati.master.vo.InsuranceVO;
import org.epragati.master.vo.MasterFeedBackQuestionsVO;
import org.epragati.master.vo.MasterWeightsForAltVO;
import org.epragati.master.vo.OfficeVO;
import org.epragati.master.vo.OwnershipVO;
import org.epragati.master.vo.RegistrationDetailsVO;
import org.epragati.master.vo.RepresentativeVO;
import org.epragati.master.vo.SearchVo;
import org.epragati.master.vo.UserVO;
import org.epragati.payment.dto.FeeDetailsDTO;
import org.epragati.payment.dto.PaymentTransactionDTO;
import org.epragati.payment.mapper.ApplicantDeatilsMapper;
import org.epragati.payment.mapper.FeeDetailsMapper;
import org.epragati.payments.dao.PaymentTransactionDAO;
import org.epragati.payments.vo.ClassOfVehiclesVO;
import org.epragati.payments.vo.FeeDetailsVO;
import org.epragati.payments.vo.FeesVO;
import org.epragati.payments.vo.TransactionDetailVO;
import org.epragati.permits.dao.PermitDetailsDAO;
import org.epragati.permits.dao.PermitMandalExemptionDAO;
import org.epragati.permits.dao.PermitTypeDAO;
import org.epragati.permits.dto.PermitDetailsDTO;
import org.epragati.permits.dto.PermitMandalExemptionDTO;
import org.epragati.permits.dto.PermitTypeDTO;
import org.epragati.permits.mappers.OtherStateTemporaryPermitDetailsMapper;
import org.epragati.permits.service.PermitValidationsService;
import org.epragati.permits.vo.PermitDetailsVO;
import org.epragati.registration.service.DealerService;
import org.epragati.registration.service.RegistrationMigrationSolutionsService;
import org.epragati.regservice.CitizenTaxService;
import org.epragati.regservice.RegistrationService;
import org.epragati.regservice.dao.CombinationServicesDAO;
import org.epragati.regservice.dao.RepresentativeDAO;
import org.epragati.regservice.dao.RepresentativeLogDAO;
import org.epragati.regservice.dao.TokenAuthenticationDAO;
import org.epragati.regservice.dto.ActionDetails;
import org.epragati.regservice.dto.AlterationDTO;
import org.epragati.regservice.dto.ApplicationStatusDetails;
import org.epragati.regservice.dto.BileteralTaxDTO;
import org.epragati.regservice.dto.CitizenFeeDetailsInput;
import org.epragati.regservice.dto.ClassOfVehicleConversion;
import org.epragati.regservice.dto.CombinationServicesDTO;
import org.epragati.regservice.dto.FuelConversion;
import org.epragati.regservice.dto.NOCDetailsDTO;
import org.epragati.regservice.dto.OfficeSlotsAvailabilityDTO;
import org.epragati.regservice.dto.PUCDetailsDTO;
import org.epragati.regservice.dto.RegServiceApprovedDTO;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.dto.RegServicesFeedBack;
import org.epragati.regservice.dto.RepresentativeDTO;
import org.epragati.regservice.dto.RepresentativeLogDTO;
import org.epragati.regservice.dto.SeatConversion;
import org.epragati.regservice.dto.ServicesInvalidDetailsDTO;
import org.epragati.regservice.dto.SlotDetailsDTO;
import org.epragati.regservice.dto.TheftVehicleDetailsDTO;
import org.epragati.regservice.dto.TokenAuthenticationDTO;
import org.epragati.regservice.dto.TrasnferOfOwnerShipDTO;
import org.epragati.regservice.dto.VehicleStoppageDetailsDTO;
import org.epragati.regservice.mapper.ActionDetailMapper;
import org.epragati.regservice.mapper.AlterationMapper;
import org.epragati.regservice.mapper.BileteralTaxMapper;
import org.epragati.regservice.mapper.ClassOfVehicleConversionMapper;
import org.epragati.regservice.mapper.DuplicateDetailsMapper;
import org.epragati.regservice.mapper.FreshRCMapper;
import org.epragati.regservice.mapper.FuelConversionMapper;
import org.epragati.regservice.mapper.LegalHiresMapper;
import org.epragati.regservice.mapper.NOCDetailsMapper;
import org.epragati.regservice.mapper.PUCDetailsMapper;
import org.epragati.regservice.mapper.PoliceClearanceCertificateMapper;
import org.epragati.regservice.mapper.RegServiceMapper;
import org.epragati.regservice.mapper.RegServicesFeedBackMapper;
import org.epragati.regservice.mapper.SeatConversionMapper;
import org.epragati.regservice.mapper.SlotDetailsMapper;
import org.epragati.regservice.mapper.TheftVehcileDetailsMapper;
import org.epragati.regservice.mapper.TowMapper;
import org.epragati.regservice.mapper.VehicleStoppageDetailsMapper;
import org.epragati.regservice.vo.AlterationVO;
import org.epragati.regservice.vo.ApplicationSearchVO;
import org.epragati.regservice.vo.CitizenApplicationSearchResponceLimitedVO;
import org.epragati.regservice.vo.CitizenApplicationSearchResponceVO;
import org.epragati.regservice.vo.ClassOfVehicleConversionVO;
import org.epragati.regservice.vo.FuelConversionVO;
import org.epragati.regservice.vo.InputForRePay;
import org.epragati.regservice.vo.LegalHiresVO;
import org.epragati.regservice.vo.MobileApplicationStatusVO;
import org.epragati.regservice.vo.MobileVO;
import org.epragati.regservice.vo.PUCDetailsVO;
import org.epragati.regservice.vo.PoliceDepartmentSearchResponceVO;
import org.epragati.regservice.vo.RcValidationVO;
import org.epragati.regservice.vo.RegServiceVO;
import org.epragati.regservice.vo.RegServicesFeedBackVO;
import org.epragati.regservice.vo.SeatConversionVO;
import org.epragati.regservice.vo.TheftVehicleDetailsVO;
import org.epragati.regservice.vo.TowVO;
import org.epragati.regservice.vo.VcrValidationVo;
import org.epragati.regservice.vo.VehicleStoppageMVIReportVO;
import org.epragati.restGateway.RestGateWayService;
import org.epragati.rta.reports.vo.CitizenSearchReportVO;
import org.epragati.rta.service.impl.service.RTAService;
import org.epragati.rta.service.impl.service.RegistratrionServicesApprovals;
import org.epragati.rta.vo.TrailerChassisDetailsVO;
import org.epragati.sequence.SequenceGenerator;
import org.epragati.service.enclosure.mapper.EnclosuresLogMapper;
import org.epragati.service.enclosure.vo.ImageVO;
import org.epragati.service.files.GridFsClient;
import org.epragati.service.notification.MessageTemplate;
import org.epragati.service.notification.NotificationUtil;
import org.epragati.sn.dao.SpecialNumberDetailsDAO;
import org.epragati.sn.dto.SpecialNumberDetailsDTO;
import org.epragati.sn.numberseries.dao.PRPoolDAO;
import org.epragati.sn.numberseries.dto.PRPoolDTO;
import org.epragati.stagecarriageservice.StageCarriageServices;
import org.epragati.tax.vo.TaxTypeEnum;
import org.epragati.tax.vo.TaxTypeEnum.ExcemptionsType;
import org.epragati.tracking.vo.Trackingvo;
import org.epragati.util.AppErrors;
import org.epragati.util.AppMessages;
import org.epragati.util.ApplicantTypeEnum;
import org.epragati.util.BidStatus;
import org.epragati.util.DateConverters;
import org.epragati.util.MandalServiceEnum;
import org.epragati.util.NumberPoolStatus;
import org.epragati.util.PermitsEnum;
import org.epragati.util.PermitsEnum.PermitType;
import org.epragati.util.ResponseStatusEnum;
import org.epragati.util.RoleEnum;
import org.epragati.util.ServicesValidations;
import org.epragati.util.SourceEnum;
import org.epragati.util.Status;
import org.epragati.util.StatusRegistration;
import org.epragati.util.StatusRegistration.TheftState;
import org.epragati.util.TowTokenStatus;
import org.epragati.util.document.KeyValue;
import org.epragati.util.document.Sequence;
import org.epragati.util.payment.ClassOfVehicleEnum;
import org.epragati.util.payment.FinanceTowEnum;
import org.epragati.util.payment.GatewayTypeEnum;
import org.epragati.util.payment.ModuleEnum;
import org.epragati.util.payment.ModuleEnum.RegCombinationsModuleEnum;
import org.epragati.util.payment.OtherStateApplictionType;
import org.epragati.util.payment.PayStatusEnum;
import org.epragati.util.payment.ServiceCodeEnum;
import org.epragati.util.payment.ServiceEnum;
import org.epragati.vcr.vo.RegistrationVcrVo;
import org.epragati.vcr.vo.VcrFinalServiceVO;
import org.epragati.vcr.vo.VoluntaryTaxVO;
import org.epragati.vcrImage.dao.FreezeVehiclsDAO;
import org.epragati.vcrImage.dao.VcrFinalServiceDAO;
import org.epragati.vcrImage.dao.VoluntaryTaxDAO;
import org.epragati.vcrImage.dto.FreezeVehiclsDTO;
import org.epragati.vcrImage.dto.VcrFinalServiceDTO;
import org.epragati.vcrImage.dto.VoluntaryTaxDTO;
import org.epragati.vcrImage.mapper.VcrFinalServiceMapper;
import org.epragati.vcrImage.mapper.VoluntaryTaxMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 *
 * @author krishnarjun.pampana
 *
 */
@Service
public class RegistrationServiceImpl implements RegistrationService {

	private static final Logger logger = LoggerFactory.getLogger(RegistrationServiceImpl.class);
	@Autowired
	private DispatcherSubmissionDAORepo dispatcherDAO;

	@Autowired
	private FcDetailsDAO fcDetailsDAO;

	@Autowired
	private FcDetailsMapper fcDetailsMapper;

	@Autowired
	private AppMessages appMessages;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private RegServiceMapper regServiceMapper;

	@Autowired
	private ApplicantDeatilsMapper applicantDeatilsMapper;

	@Autowired
	private OfficeMapper officeMapper;

	@Autowired
	private ApplicantAddressMapper applicantAddressMapper;

	@Autowired
	private SlotDetailsMapper slotDetailsMapper;

	@Autowired
	private RegServiceDAO regServiceDAO;

	@Autowired
	private ServiceAvailabilityConfigDAO serviceAvailabilityConfigDAO;

	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;

	@SuppressWarnings("rawtypes")
	@Autowired
	private RegistrationDetailsMapper registrationDetailsMapper;

	@Autowired
	private RestGateWayService restGateWayService;

	@Autowired
	private ApplicantAddressMapper addressMapper;

	@Autowired
	private GridFsClient gridFsClient;

	@Autowired
	private CombinationServicesDAO combinationServicesDAO;

	@Autowired
	private CovService covService;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private PaymentTransactionDAO paymentTransactionDAO;

	@Autowired
	private ClassOfVehicleConversionDAO classOfVehicleConversionDAO;

	@Autowired
	private ClassOfVehicleConversionMapper classOfVehicleConversionMapper;

	@Autowired
	private BodyTypeDAO bodyTypeDAO;

	@Autowired
	private BodyTypeMapper bodyTypeMapper;

	@Autowired
	private TaxDetailsDAO taxDetailsDAO;

	@Autowired
	private AlterationMapper alterationMapper;

	@Autowired
	private DuplicateDetailsMapper duplicateDetailsMapper;

	@Autowired
	private SequenceGenerator sequenceGenerator;

	@Autowired
	private FuelConversionDAO fuelConversionDAO;

	@Autowired
	private FuelConversionMapper fuelConversionMapper;

	@Autowired
	private SeatConversionDAO seatConversionDAO;

	@Autowired
	private SeatConversionMapper seatConversionMapper;

	@Autowired
	private MandalService mandalService;

	@Autowired
	private TowMapper towMapper;

	@Autowired
	private StagingRegistrationDetailsDAO stagingRegistrationDetailsDAO;

	@Autowired
	private AlterationDAO alterationDAO;

	@Autowired
	private UserMapper userMapper;

	@Autowired
	private SlotsService slotService;

	@Autowired
	private OfficeSlotsAvailabilityDAO officeSlotAvailabilityDao;

	@Autowired
	private NotificationUtil notifications;

	@Autowired
	private OfficeDAO officeDAO;

	@Autowired
	private LogMovingService logMovingService;

	@Autowired
	private MasterPayperiodDAO masterPayperiodDAO;

	@Autowired
	private RegistratrionServicesApprovals registratrionServicesApprovals;

	@Autowired
	private StagingBodyBuilderService stagingBodyBuilderService;

	@Autowired
	private CitizenEnclosuresDAO citizenEnclosuresDAO;

	@Value("${reg.fresh.puc.validity.inmonths:6}")
	private Long pucValidityInMonths;

	@Autowired
	private NOCDetailsMapper nocDetailsMapper;

	@Value("${reg.service.vcrDetailsFromCfstUrl}")
	private String vcrDetailsFromCfstUrl;

	@Autowired
	private InsuranceDetailsMapper insuranceDetailsMapper;

	@Autowired
	private PUCDetailsMapper pucDetailsMapper;

	@Autowired
	private DealerService dealerService;

	@Autowired
	private AadhaarResponseDAO aadhaarResponseDAO;

	@Autowired
	private AadhaarDetailsResponseMapper aadhaarDetailsResponseMapper;

	@Autowired
	private OwnershipMapper ownerShipMapper;

	@Autowired
	private PermitsService permitsService;

	@Autowired
	private MandalDAO mandalDAO;

	@Autowired
	private OfficeMapper officeMappar;

	@Autowired
	private TheftVehcileDetailsMapper theftDetailsMapper;

	@Autowired
	private PermitDetailsMapper permitDetailsMapper;

	@Autowired
	private PermitValidationsService permitValidationsService;

	@Autowired
	private CitizenTaxService citizenTaxService;

	@Autowired
	private PoliceClearanceCertificateMapper policeClearanceCertificateMapper;

	@Autowired
	private LegalHiresMapper legalHiresMapper;

	@Autowired
	private RegServiceApprovedDAO regServiceApprovedDAO;

	@Autowired
	private PermitDetailsDAO permitDetailsDAO;

	@Autowired
	private MandalMapper mandalMapper;

	@Autowired
	private PropertiesDAO propertiesDAO;

	@Autowired
	private VehicleStoppageDetailsMapper taxStoppageDetailsMapper;

	@Autowired
	private PermitTypeDAO permitTypeDAO;

	@Autowired
	private MasterUsersDAO masterUsersDAO;

	@Autowired
	private MasterNewGoTaxDetailsDAO masterNewGoTaxDetailsDAO;

	@Autowired
	private MasterTaxExcemptionsDAO masterTaxExcemptionsDAO;

	@Autowired
	private RegistrationMigrationSolutionsService registrationMigrationSolutionsService;

	@Autowired
	private ActionDetailMapper actionDetailMapper;

	@Autowired
	private MasterFeedBackQuestionsDAO masterFeedBackQuestionsDAO;

	@Autowired
	private MasterFeedBackQuestionsMapper masterFeedBackQuestionsMapper;

	@Autowired
	private RegServicesFeedBackDAO regServicesFeedBackDAO;

	@Autowired
	private RegServicesFeedBackMapper regServicesFeedBackMapper;

	@Autowired
	private EnclosuresLogMapper enclosuresLogMapper;

	@Autowired
	AppErrors appErrors;

	@Autowired
	private AdhaarFamilyDAO adhaarFamilyDAO;

	@Autowired
	private PermitMandalExemptionDAO permitMandalExemptionDAO;

	@Autowired
	private MasterWeightsForAltDAO masterWeightsForAltDAO;

	@Autowired
	private MasterWeightsForAltMapper masterWeightsForAltMapper;

	@Autowired
	private DistrictDAO districtDAO;

	@Autowired
	private DistrictMapper districtMapper;

	@Autowired
	private FinalTaxHelperDAO finalTaxHelperDAO;

	@Autowired
	private MasterCovDAO masterCovDAO;

	@Autowired
	private VcrTaxPaidMapper vcrTaxPaidMapper;

	@Autowired
	private VcrDetailsDAO vcrDetailsDAO;

	@Autowired
	private ErrorTrackLogDAO errorTrackLogDAO;

	@Autowired
	private MasterQuaterPeriodDAO masterQuaterPeriodDAO;

	@Autowired
	private ApplicantDetailsDAO applicantDetailsDAO;

	@Autowired
	private BileteralTaxMapper bileteralTaxMapper;

	@Autowired
	private BileteralTaxDAO bileteralTaxDAO;

	@Autowired
	private RTAService rtaService;

	@Autowired
	private RepresentativeDAO representativeDAO;

	@Autowired
	private RepresentativeMapper representativeMapper;

	@Autowired
	private RepresentativeLogDAO representativeLogDAO;

	@Autowired
	private EductaionInstituteVehicleDetailsDao eductaionInstituteVehicleDetailsDao;

	@Autowired
	private VcrFinalServiceDAO vcrFinalServiceDAO;

	@Autowired
	private VcrFinalServiceMapper vcrFinalServiceMapper;

	@Autowired
	private FeeCorrectionDAO feeCorrectionDAO;

	@Autowired
	private TokenAuthenticationDAO tokenAuthenticationDAO;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	private RCCancellationMapper rcCancellationMapper;

	@Autowired
	private OtherStateTemporaryPermitDetailsMapper otherStateTemporaryPermitDetailsMapper;

	@Autowired
	private VoluntaryTaxMapper voluntaryTaxMapper;

	@Autowired
	private StateDAO stateDao;

	@Autowired
	private CountryDAO countryDao;

	@Autowired
	private VehicleStoppageDetailsDAO vehicleStoppageDetailsDAO;

	@Autowired
	private DealerRegDAO dealerRegDAO;

	@Autowired
	private FreezeVehiclsDAO freezeVehiclsDAO;

	@Autowired
	private StageCarriageServices stageCarriageServices;

	@Autowired
	private AuctionDetailsDAO auctionDetailsDAO;

	@Autowired
	private ServicesInvalidDetailsDAO servicesInvalidDetailsDAO;

	@Autowired
	private SuspensionDAO suspensionDAO;

	@Autowired
	private VoluntaryTaxDAO voluntaryTaxDAO;

	@Autowired
	private FreshRCMapper freshRCMapper;

	@Autowired
	private SpecialNumberDetailsDAO specialNumberDetailsDAO;

	@Autowired
	private ServicesDAO servicesDAO;

	@Autowired
	private AadharDropDAO aadharDropDAO;

	@Autowired
	private AadharDropDownMapper aadharDropDownMapper;

	@Autowired
	private EnclosuresDAO enclosureDAO;
	
	@Autowired
	private AptsAadhaarResponseMapper aptsAadhaarResponseMapper;

	@Override
	public Optional<RegistrationDetailsVO> getRegistrationDetailByprNoAndAadhaarNo(RcValidationVO rcValidationVO) {

		Optional<RegistrationDetailsDTO> registrationDetailsDTO = registrationDetailDAO
				.findByPrNo(rcValidationVO.getPrNo());
		if (!registrationDetailsDTO.isPresent()) {
			logger.warn(" No Registration Details Records found for prNo [{}]", rcValidationVO.getPrNo());
			throw new BadRequestException("No Record Found");
		}
		if (StringUtils.isNoneBlank(rcValidationVO.getAadhaarDetailsRequestVO().getUid_num())
				|| registrationDetailsDTO.get().getApplicantDetails() == null
				|| !rcValidationVO.getAadhaarDetailsRequestVO().getUid_num()
						.equals(registrationDetailsDTO.get().getApplicantDetails().getAadharNo())) {
			logger.warn(" No applicant details found for PRno[{}]", rcValidationVO.getPrNo());
			throw new BadRequestException("No Application Details found with PR No: " + rcValidationVO.getPrNo()
					+ " and aadhaar No: " + rcValidationVO.getAadhaarDetailsRequestVO().getUid_num());
		}
		if (registrationDetailsDTO.get().getApplicantDetails().getIsAadhaarValidated()) {

			Optional<AadharDetailsResponseVO> applicantDetailsOptional = restGateWayService.validateAadhaar(
					rcValidationVO.getAadhaarDetailsRequestVO(), setAadhaarSourceDetails(rcValidationVO));

			if (!applicantDetailsOptional.isPresent()) {
				throw new BadRequestException(MessageKeys.AADHAAR_RES_NO_DATA);

			}
			if (applicantDetailsOptional.get().getAuth_status()
					.equals(ResponseStatusEnum.AADHAARRESPONSE.FAILED.getLabel())) {

				throw new BadRequestException(applicantDetailsOptional.get().getAuth_err_code());
			}

			return Optional.of(registrationDetailsMapper.convertEntity(registrationDetailsDTO.get()));
		}
		logger.error("User is not aadhaar Validated with aadhaar No [{}]",
				registrationDetailsDTO.get().getApplicantDetails().getAadharNo());
		throw new BadRequestException("Please do aadhaar seed service");

	}

	@Override
	public RegServiceVO savingRegistrationServices(String regServiceVO, MultipartFile[] multipart, String user)
			throws IOException, RcValidationException {

		logger.debug("regServiceVO [{}] ", regServiceVO);

		if (StringUtils.isBlank(regServiceVO)) {
			logger.error("regServiceVO is required.");
			throw new BadRequestException("regServiceVO is required.");
		}

		Optional<RegServiceVO> inputOptional = readValue(regServiceVO, RegServiceVO.class);

		if (!inputOptional.isPresent()) {
			logger.error(" Problem in reading json String to RegServiceVO [{}]");
			throw new BadRequestException("Invalid Inputs.");
		}

		if (inputOptional.get().getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId()))) {
			inputOptional.get().setFinancierUserId(user);
		}
		Optional<RegistrationDetailsDTO> registrationOptional = registrationDetailDAO
				.findByPrNo(inputOptional.get().getPrNo());
		if (!registrationOptional.isPresent()) {
			logger.error("No record found. with PRNo [{}] ", inputOptional.get().getPrNo());
			throw new BadRequestException("No record found.Pr no: " + inputOptional.get().getPrNo());
		}
		// call createCitizenDetails
		return createCitizenDetails(inputOptional.get(), multipart);

	}

	public RegServiceVO createCitizenDetails(RegServiceVO regServiceVO, MultipartFile[] multipart)
			throws IOException, RcValidationException {

		final String prNo = regServiceVO.getPrNo();

		synchronized (prNo.intern()) {

			// save based on serviceIds use switch
			Optional<KeyValue<RegServiceDTO, RegistrationDetailsDTO>> citizenObjectsOptional = Optional.empty();
			List<ServiceEnum> serviceIds = regServiceVO.getServiceIds().stream()
					.map(id -> ServiceEnum.getServiceEnumById(id)).collect(Collectors.toList());

			for (ServiceEnum se : serviceIds) {
				switch (se) {
				case CHANGEOFADDRESS:

					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO, this::doCoa);
					break;

				case ALTERATIONOFVEHICLE:

					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO, this::doAlt);
					break;

				case REASSIGNMENT:

					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO, this::doReAssign);
					break;

				case DUPLICATE:

					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO, this::doDuplicate);
					break;
				case TRANSFEROFOWNERSHIP:

					if (null != regServiceVO.getTowDetails() && regServiceVO.getTowDetails().getBuyer() != null
							&& regServiceVO.getTowDetails().getBuyer().equals(OwnerType.BUYER)
							|| null != regServiceVO.getTowDetails().getTransferType()
									&& regServiceVO.getTowDetails().getTransferType().equals(TransferType.DEATH)) {

						return saveBuyerDoc(regServiceVO, multipart,
								getTowBuyerCombinationServices(citizenObjectsOptional, regServiceVO));
					}
					if (multipart != null && multipart.length == 0) {
						throw new BadRequestException("No images found");
					}
					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO, this::doTow);
					break;
				case RENEWAL:

					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO, this::doRenewal);
					break;

				case ISSUEOFNOC:

					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO, this::doNoc);
					break;

				case NEWPERMIT:
					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO, this::doNewPermit);
					break;

				case VARIATIONOFPERMIT:
					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO,
							this::doPermitVariation);
					break;

				case THEFTINTIMATION:
					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO,
							this::doTheftIntimation);
					break;

				case THEFTREVOCATION:
					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO,
							this::doTheftRevocation);
					break;

				case RCFORFINANCE:
					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO,
							this::doRcForFinance);
					break;

				case CANCELLATIONOFNOC:

					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO,
							this::doCancellationOfNoc);
					break;
				case RENEWALOFPERMIT:
					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO,
							this::doRenewalOfPermit);
					break;
				case HPA:
					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO, this::doHGA);
					break;
				case HIREPURCHASETERMINATION:
					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO, this::doHPT);
					break;
				case TRANSFEROFPERMIT:
					if (serviceIds.contains(ServiceEnum.TRANSFEROFOWNERSHIP)) {
						break;
					}
					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO, this::doNewPermit);
					break;
				case PERMITCOA:
					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO, this::doNewPermit);
					break;

				case NEWFC:
					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO, this::doNewFC);
					break;
				case RENEWALOFAUTHCARD:
					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO,
							this::doRenewalOfAuthCard);
					break;

				case SURRENDEROFPERMIT:
					if (serviceIds.contains(ServiceEnum.TRANSFEROFOWNERSHIP)
							|| serviceIds.contains(ServiceEnum.CHANGEOFADDRESS)) {
						break;
					}
					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO,
							this::doCancelPermit);
					break;
				case EXTENSIONOFVALIDITY:
					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO,
							this::doExtensionOfValidity);
					break;
				case REPLACEMENTOFVEHICLE:
					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO,
							this::doReplacementOfVehicle);
					break;
				case TAXATION:

					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO, this::doDiffTAx);
					break;

				case VEHICLESTOPPAGE:

					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO,
							this::doVehicleStoppage);
					break;
				case VEHICLESTOPPAGEREVOKATION:

					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO,
							this::doVehicleStoppageRevokation);
					break;

				case PERDATAENTRY:
					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO,
							this::doPermitDataEntry);
					break;
				case ISSUEOFRECOMMENDATIONLETTER:
					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO,
							this::doIssueOfRecommendationetter);
					break;
				case RENEWALOFRECOMMENDATIONLETTER:
					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO,
							this::doRenewalOfRecommendationetter);
					break;
				case FEECORRECTION:
					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO,
							this::validationForFeeCorrection);
					break;
				case RCCANCELLATION:

					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO,
							this::doRcCancellation);
					break;
				case CHANGEOFADDRESSOFRECOMMENDATIONLETTER:
					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO, this::doNewPermit);
					break;
				case TRANSFEROFRECOMMENDATIONLETTER:
					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO, this::doNewPermit);
					break;
				default:
					break;
				}
			}

			if (!regServiceVO.getServiceIds().stream()
					.anyMatch(id -> (id.equals(ServiceEnum.PERMITCOA.getId())
							|| id.equals(ServiceEnum.TRANSFEROFPERMIT.getId())
							|| id.equals(ServiceEnum.CHANGEOFADDRESSOFRECOMMENDATIONLETTER.getId())
							|| id.equals(ServiceEnum.TRANSFEROFRECOMMENDATIONLETTER.getId())))) {
				InsuranceDetailsVO insurenceDetails = doValidateAndSaveInsurenceDetails(regServiceVO,
						citizenObjectsOptional.get().getKey());
				PUCDetailsVO pucDetails = doValidateAndSavePUCDetails(regServiceVO,
						citizenObjectsOptional.get().getKey());
				if (insurenceDetails != null) {
					citizenObjectsOptional.get().getKey()
							.setInsuranceDetails(insuranceDetailsMapper.convertVO(insurenceDetails));

					// citizenObjectsOptional.get().getValue().setInsuranceDetails(insuranceDetailsMapper.convertVO(insurenceDetails));

				}
				if (pucDetails != null) {
					citizenObjectsOptional.get().getKey().setPucDetails(pucDetailsMapper.convertVO(pucDetails));
				}
			}
			// validations
			Pair<Boolean, RegServiceDTO> pendingDetailsAndStatus = checkPendingStatus(regServiceVO.getPrNo(),
					regServiceVO.getServiceIds(), citizenObjectsOptional.get().getKey());
			if (pendingDetailsAndStatus.getFirst()) {
				throw new BadRequestException("Application is in Pending state.Application No: "
						+ pendingDetailsAndStatus.getSecond().getApplicationNo());
			}
			RcValidationVO rcValidationVO = this.addfieldsForValidatons(citizenObjectsOptional.get().getKey());
			try {
				
					if(regServiceVO.getTransactionType() != null) {
						searchWithOutAadharNoAndRc(rcValidationVO, Boolean.TRUE);
					}else {
					searchWithAadharNoAndRc(rcValidationVO, Boolean.TRUE);
				}
				
				
			} catch (RcValidationException e) {
				logger.error("Exception in RcValidation [{}] ", e);
				throw new RcValidationException(e.getErrors());
			}
			OfficeVO vo = null;
			/*
			 * if (null != regServiceVO.getNewOfficeDetails() &&
			 * regServiceVO.getNewOfficeDetails().getOfficeCode() != null) { vo =
			 * officeMapper .convertEntity(officeDAO.findByOfficeCode(regServiceVO.
			 * getNewOfficeDetails(). getOfficeCode())) .get(); } else {
			 */
			vo = getOfficeDetails(regServiceVO, citizenObjectsOptional.get().getKey());
			/* } */
			citizenObjectsOptional.get().getKey().setOfficeDetails(officeMapper.convertVO(vo));
			citizenObjectsOptional.get().getKey().setOfficeCode(vo.getOfficeCode());
			Map<String, String> officeCodeMap = new TreeMap<>();
			officeCodeMap.put("officeCode", citizenObjectsOptional.get().getKey().getOfficeCode());
			citizenObjectsOptional.get().getKey().setApplicationNo(sequenceGenerator
					.getSequence(String.valueOf(Sequence.REGISTRATIONAPPNO.getSequenceId()), officeCodeMap));
			if (regServiceVO.getSlotDetails() != null && regServiceVO.getSlotDetails().getTestSlotDate() != null
					&& !rcValidationVO.getServiceIds().stream()
							.anyMatch(id -> ServiceEnum.getPermitServices().contains(id))) {
				citizenObjectsOptional.get().getKey()
						.setSlotDetails(slotDetailsMapper.convertVO(regServiceVO.getSlotDetails()));
			}
			if (regServiceVO.getServiceIds().stream()
					.anyMatch(service -> service.equals(ServiceEnum.ALTERATIONOFVEHICLE.getId()))
					|| regServiceVO.getServiceIds().stream()
							.anyMatch(service -> service.equals(ServiceEnum.RENEWAL.getId()))
					|| regServiceVO.getServiceIds().stream()
							.anyMatch(service -> service.equals(ServiceEnum.NEWFC.getId()))
					|| regServiceVO.getServiceIds().stream()
							.anyMatch(service -> service.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))
					|| regServiceVO.getServiceIds().stream()
							.anyMatch(service -> service.equals(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId()))) {
				setMviOfficeDetails(citizenObjectsOptional.get().getKey(), regServiceVO);
			}
			if (regServiceVO.getServiceIds().stream()
					.anyMatch(service -> service.equals(ServiceEnum.RCCANCELLATION.getId()))) {
				rcCancellationProcess(citizenObjectsOptional.get(), regServiceVO);
			}
			saveRegistrationDetailsDoc(regServiceVO, citizenObjectsOptional.get());
			saveCitizenServiceDoc(regServiceVO, citizenObjectsOptional.get().getKey(), multipart);
			RegServiceVO regVo = regServiceMapper.convertEntity(citizenObjectsOptional.get().getKey());
			return regVo;
		}
	}

	private RegistrationDetailsDTO saveRegistrationDetailsDoc(RegServiceVO input,
			KeyValue<RegServiceDTO, RegistrationDetailsDTO> keyValue) {
		RegistrationDetailsDTO regDetails = keyValue.getValue();
		RegServiceDTO regServiceDTO = keyValue.getKey();

		registrationDetailDAO.save(regDetails);

		if (regDetails.getApplicantDetails().getPresentAddress().getMandal() == null) {
			if (input.getPresentAddress() != null && input.getPresentAddress().getMandal() != null) {
				regServiceDTO.getRegistrationDetails().getApplicantDetails().getPresentAddress()
						.setMandal(mandalMapper.convertVO(input.getPresentAddress().getMandal()));
			}
		}
		if (input.getRepresnatativeAadhaar() != null
				&& !regDetails.getApplicantDetails().getAadharNo().equals(input.getRepresnatativeAadhaar())) {
			RepresentativeDTO repDTO = validateRepresentative(input.getRepresnatativeAadhaar());
			regServiceDTO.setRepresentativeUidToken(repDTO.getChildToken());
		}
		if(input.getTransactionType()!=null) {
			regServiceDTO.setTransactionType(input.getTransactionType());
		}
		/*
		 * if (regDetails.getApplicantDetails().getPresentAddress().getMandal() == null)
		 * { regDetails.getApplicantDetails().getPresentAddress()
		 * .setMandal(mandalMapper.convertVO(input.getPresentAddress().getMandal ())); }
		 * if (regDetails.getApplicantDetails().getPresentAddress().getDistrict() ==
		 * null) { regDetails.getApplicantDetails().getPresentAddress()
		 * .setDistrict(districtMapper.convertVO(input.getPresentAddress().
		 * getDistrict() )); }
		 */

		return regDetails;

	}

	private RcValidationVO addfieldsForValidatons(RegServiceDTO dto) {
		RcValidationVO rcValidationVO = new RcValidationVO();
		rcValidationVO.setPrNo(dto.getPrNo());
		rcValidationVO.setAadharNo(dto.getAadhaarNo());
		rcValidationVO.setServiceIds(dto.getServiceIds());
		if (dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.ALTERATIONOFVEHICLE.getId()))) {
			// Iterator<Integer> intIds = dto.getServiceIds().iterator();
			// List<Integer> serviceIdsInt =dto.getServiceIds();
			Set<Integer> serviceIds = new TreeSet<>();
			Iterator<Integer> intIds = dto.getServiceIds().iterator();
			while (intIds.hasNext()) {
				Integer singleId = intIds.next();
				if (singleId.equals(ServiceEnum.REASSIGNMENT.getId())) {
					continue;
				} else if (singleId.equals(ServiceEnum.VARIATIONOFPERMIT.getId())) {
					continue;
				}
				serviceIds.add(singleId);
				// intIds.remove();
			}
			rcValidationVO.setServiceIds(serviceIds);
		}
		if (dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId()))) {
			if (dto.getBuyerDetails().getSeller() != null) {
				rcValidationVO.setOwnerType(dto.getBuyerDetails().getSeller());
			}
			if (dto.getBuyerDetails() != null && dto.getBuyerDetails().getTransferType() != null) {
				rcValidationVO.setTransferType(dto.getBuyerDetails().getTransferType());
			}

		}
		return rcValidationVO;
	}

	/**
	 *
	 * @param regServiceVO
	 * @param regServiceDetails
	 * @return pucdetailsVo
	 */

	private PUCDetailsVO doValidateAndSavePUCDetails(RegServiceVO regServiceVO, RegServiceDTO regServiceDetails) {
		PUCDetailsVO pucVO = new PUCDetailsVO();
		PUCDetailsDTO pucDetails = null;
		if (regServiceVO.getPucDetails() == null
				&& regServiceDetails.getRegistrationDetails().getPucDetailsDTO() != null) {
			pucDetails = regServiceDetails.getRegistrationDetails().getPucDetailsDTO();
			pucVO = pucDetailsMapper.convertEntity(pucDetails);
		} else {
			pucVO = regServiceVO.getPucDetails();
		}
		return pucVO;
	}

	/**
	 *
	 * @param regServiceVO
	 * @param regServiceDetails
	 * @return InsyrencedetailsVo
	 */

	private InsuranceDetailsVO doValidateAndSaveInsurenceDetails(RegServiceVO regServiceVO,
			RegServiceDTO regServiceDetails) {
		InsuranceDetailsVO vo = new InsuranceDetailsVO();
		InsuranceDetailsDTO insuranceDetails = null;
		if (regServiceVO.getInsuranceDetailsVo() == null
				&& regServiceDetails.getRegistrationDetails().getInsuranceDetails() != null) {
			insuranceDetails = regServiceDetails.getRegistrationDetails().getInsuranceDetails();
			vo = insuranceDetailsMapper.convertEntity(insuranceDetails);
		} else if (regServiceVO.getInsuranceDetailsVo() != null
				&& regServiceVO.getInsuranceDetailsVo().getTenure() != null) {
			Long tenure = regServiceVO.getInsuranceDetailsVo().getTenure().longValue();
			LocalDate validTo = regServiceVO.getInsuranceDetailsVo().getValidTill().plusDays(1);
			LocalDate validFrom = regServiceVO.getInsuranceDetailsVo().getValidFrom();
			Long years = ChronoUnit.YEARS.between(validFrom, validTo);
			if (years != tenure) {
				throw new BadRequestException("Please enter valid Insurence details");
			}
			vo = regServiceVO.getInsuranceDetailsVo();
		}

		return vo;
	}

	public void saveImages(RegServiceVO regServiceVO, RegServiceDTO regServiceDTO, MultipartFile[] uploadfiles)
			throws IOException {

		if (uploadfiles != null && uploadfiles.length == 0) {
			throw new BadRequestException("No images found");
		}
		boolean isNonToApplication = true;
		boolean buyerInsurance = false;
		boolean buyerPUC = false;
		CitizenEnclosuresDTO dto = null;
		List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = null;

		if (null != regServiceDTO.getServiceIds() && regServiceDTO.getServiceIds().size() == 1
				&& (regServiceDTO.getServiceIds().contains(ServiceEnum.NEWFC.getId())
						|| regServiceDTO.getServiceIds().contains(ServiceEnum.RENEWALFC.getId())
						|| regServiceDTO.getServiceIds().contains(ServiceEnum.OTHERSTATIONFC.getId()))
				|| regServiceDTO.getServiceIds().contains(ServiceEnum.VEHICLESTOPPAGE.getId())) {
			enclosures = gridFsClient.convertImages(regServiceVO.getImageInput(), regServiceDTO.getApplicationNo(),
					uploadfiles, StatusRegistration.APPROVED.getDescription());
			if (regServiceDTO.getServiceIds().contains(ServiceEnum.VEHICLESTOPPAGE.getId())) {
				Optional<CitizenEnclosuresDTO> toDetails = citizenEnclosuresDAO
						.findByApplicationNo(regServiceDTO.getApplicationNo());
				if (toDetails.isPresent()) {
					dto = toDetails.get();
					List<KeyValue<String, List<ImageEnclosureDTO>>> enclousersTo = dto.getEnclosures();
					enclosures.addAll(enclousersTo);
					isNonToApplication = false;
				}

			}
			/*
			 * for (KeyValue<String, List<ImageEnclosureDTO>> key : enclosures) { if
			 * (key.getKey().equalsIgnoreCase(EnclosureType.CONTRACTCOPY.getValue())) {
			 * key.getValue().stream().findFirst().get()
			 * .setImageStaus(StatusRegistration.INITIATED.getDescription()); } }
			 */
		} else {
			enclosures = gridFsClient.convertImages(regServiceVO.getImageInput(), regServiceDTO.getApplicationNo(),
					uploadfiles, StatusRegistration.INITIATED.getDescription());
		}

		if (null != regServiceDTO.getServiceIds()
				&& regServiceDTO.getServiceIds().contains(ServiceEnum.TRANSFEROFOWNERSHIP.getId())
				&& regServiceDTO.getBuyerDetails() != null && regServiceDTO.getBuyerDetails().getBuyer() != null
				&& regServiceDTO.getBuyerDetails().getTransferType() != null
				&& !regServiceDTO.getBuyerDetails().getTransferType().equals(TransferType.AUCTION)) {

			Optional<CitizenEnclosuresDTO> toDetails = citizenEnclosuresDAO
					.findByApplicationNo(regServiceDTO.getApplicationNo());
			dto = toDetails.get();

			for (KeyValue<String, List<ImageEnclosureDTO>> enclousersMap : enclosures) {

				if (enclousersMap.getKey().equalsIgnoreCase(EnclosureType.Insurance.getValue())) {
					buyerInsurance = true;
				}
				if (enclousersMap.getKey().equalsIgnoreCase(EnclosureType.PUC.getValue())) {
					buyerPUC = true;
				}
			}

			List<KeyValue<String, List<ImageEnclosureDTO>>> enclousersTo = dto.getEnclosures();
			for (KeyValue<String, List<ImageEnclosureDTO>> enclousersMap : enclousersTo) {

				if (enclousersMap.getKey().equalsIgnoreCase(EnclosureType.Insurance.getValue()) && buyerInsurance) {
					enclousersMap.setKey("seller Insurance");
				}
				if (enclousersMap.getKey().equalsIgnoreCase(EnclosureType.PUC.getValue()) && buyerPUC) {
					enclousersMap.setKey("seller PUC");
				}

			}
			enclosures.addAll(enclousersTo);

			isNonToApplication = false;

		}

		if (isNonToApplication) {
			dto = new CitizenEnclosuresDTO();
		}
		dto.setApplicationNo(regServiceDTO.getApplicationNo());
		dto.setPrNo(regServiceDTO.getPrNo());
		dto.setAadharNo(regServiceDTO.getAadhaarNo());
		dto.setEnclosures(enclosures);
		dto.setServiceIds(regServiceDTO.getServiceIds());
		if (regServiceDTO.getServiceIds() != null && !regServiceDTO.getServiceIds().stream()
				.anyMatch(id -> id.equals(ServiceEnum.BILLATERALTAX.getId()))) {
			dto.setApplicantNo(regServiceDTO.getRegistrationDetails().getApplicantDetails().getApplicantNo());
		}

		citizenEnclosuresDAO.save(dto);

	}

	private void doIssueOfRecommendationetter(RegServiceVO input,
			KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {
		if (!(input.getServiceIds().contains(ServiceEnum.CHANGEOFADDRESS.getId())
				|| input.getServiceIds().contains(ServiceEnum.TRANSFEROFOWNERSHIP.getId()))) {
			RegServiceDTO regServiceDetails = citizenObjects.getKey();
			PermitDetailsDTO dto = permitDetailsMapper.convertVO(input.getPermitDetailsVO());
			if (input.getServiceIds().contains(ServiceEnum.ISSUEOFRECOMMENDATIONLETTER.getId())) {
				validateIssueOfRecommendationLetterData(dto, regServiceDetails);
			}
			regServiceDetails.setPdtl(dto);
		}

	}

	private void doRenewalOfRecommendationetter(RegServiceVO input,
			KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {
		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		PermitDetailsDTO dto = permitDetailsMapper.convertVO(input.getPermitDetailsVO());
		regServiceDetails.setPdtl(dto);
	}

	private void validateIssueOfRecommendationLetterData(PermitDetailsDTO dto, RegServiceDTO regServiceDetails) {
		permitValidationsService.validateIssueOfRecommendationLetterData(dto, regServiceDetails);

	}

	/**
	 *
	 * @param input
	 * @param citizenObjects
	 */
	private void doNewPermit(RegServiceVO input, KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {
		if (!(input.getServiceIds().contains(ServiceEnum.CHANGEOFADDRESS.getId())
				|| input.getServiceIds().contains(ServiceEnum.TRANSFEROFOWNERSHIP.getId()))) {
			RegServiceDTO regServiceDetails = citizenObjects.getKey();
			PermitDetailsDTO dto = permitDetailsMapper.convertVO(input.getPermitDetailsVO());
			if (dto.getPermitClass().getCode().equalsIgnoreCase(PermitType.TEMPORARY.getPermitTypeCode())) {
				LocalDate validFrom = dto.getRouteDetails().getForwardRouteDate();
				LocalDate validTo = dto.getRouteDetails().getReturnRouteDate();
				Long days = ChronoUnit.DAYS.between(validFrom, validTo);
				PermitTypeDTO permitType = permitTypeDAO.findByPermitType(dto.getPermitType().getPermitType());
				if (days.intValue() > permitType.getValidityPeriod()) {
					throw new BadRequestException("Please enter valid dates to apply Temporary Permit");
				}
				permitsService.checkWithTaxExpairyDays(dto, regServiceDetails.getPrNo());
			}
			regServiceDetails.setPdtl(dto);
		}

	}

	/**
	 *
	 * @param input
	 * @param citizenObjects
	 */
	private void doCancelPermit(RegServiceVO input, KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {
		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		doCheckPermitsAreExistOrNot(input.getPrNo(), input.getPermitDetailsListVO());
		List<PermitDetailsDTO> dto = permitDetailsMapper.convertVO(input.getPermitDetailsListVO());
		regServiceDetails.setPermitDetailsListDTO(dto);
	}

	private void doCheckPermitsAreExistOrNot(String prNo, List<PermitDetailsVO> permitDetailsListVO) {
		List<String> permitNumList = permitDetailsListVO.stream().map(PermitDetailsVO::getPermitNo)
				.collect(Collectors.toList());
		List<PermitDetailsDTO> permitDetailsDTOList = permitDetailsDAO.findByPrNoAndPermitStatus(prNo,
				PermitsEnum.ACTIVE.getDescription());

		List<String> permitNoList = permitDetailsDTOList.stream().map(PermitDetailsDTO::getPermitNo)
				.collect(Collectors.toList());
		for (String permitNo : permitNumList) {
			if (!permitNoList.contains(permitNo)) {
				throw new BadRequestException("Permit Details Not found with permit no :" + permitNo);
			}
		}

	}

	private void doPermitVariation(RegServiceVO input, KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {
		RegServiceDTO regServiceDTO = citizenObjects.getKey();
		PermitDetailsDTO dto = permitDetailsMapper.convertVO(input.getPermitDetailsVO());
		regServiceDTO.setPdtl(dto);

	}

	/*
	 * private void doPermitTransfer(RegServiceVO input, KeyValue<RegServiceDTO,
	 * RegistrationDetailsDTO> citizenObjects) {
	 *
	 * RegServiceDTO regServiceDetails = citizenObjects.getKey(); PermitDetailsDTO
	 * dto = permitDetailsMapper.convertVO(input.getPermitDetailsVO());
	 * regServiceDetails.setPermitDetails(dto);
	 *
	 * }
	 */

	private void doRenewalOfAuthCard(RegServiceVO input,
			KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {
		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		if (regServiceDetails.getPdtl() != null) {
			regServiceDetails.setPermitDetailsLog(Arrays.asList(regServiceDetails.getPdtl()));
		}
		PermitDetailsDTO dto = permitDetailsMapper.convertVO(input.getPermitDetailsVO());
		regServiceDetails.setPdtl(dto);
	}

	/**
	 *
	 * @param input
	 * @param citizenObjects
	 */
	private void doRenewalOfPermit(RegServiceVO input, KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {
		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		Optional<PermitDetailsDTO> permitDetailsDTOOptional = permitValidationsService
				.getPermitDetails(input.getPrNo());
		if (permitDetailsDTOOptional.isPresent()) {
			checkvalidationRenewalOfPermitValidity(permitDetailsDTOOptional.get(), input);
		} else {
			throw new BadRequestException("permit Details Not Found for prNo " + input.getPrNo());
		}
		PermitDetailsDTO dto = permitDetailsMapper.convertVO(input.getPermitDetailsVO());
		regServiceDetails.setPdtl(dto);
	}

	private Optional<KeyValue<RegServiceDTO, RegistrationDetailsDTO>> doCitizenService(
			Optional<KeyValue<RegServiceDTO, RegistrationDetailsDTO>> dlObjectsOptional, RegServiceVO input,
			BiConsumer<RegServiceVO, KeyValue<RegServiceDTO, RegistrationDetailsDTO>> dlServiceFun) {

		KeyValue<RegServiceDTO, RegistrationDetailsDTO> dlObjects;

		if (dlObjectsOptional.isPresent()) {

			dlObjects = dlObjectsOptional.get();
		} else {
			dlObjects = getBasicApplicationDetails(input);
		}

		dlServiceFun.accept(input, dlObjects);

		return Optional.of(dlObjects);
	}

	private void saveRegDocument(RegServiceDTO regServiceDetail) {

		if (regServiceDetail.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))) {
			regServiceDetail.getVehicleStoppageDetails().setApplicationNo(regServiceDetail.getApplicationNo());
			regServiceDetail.getVehicleStoppageDetails().setMviOfficeCode(regServiceDetail.getMviOfficeCode());
			regServiceDetail.getVehicleStoppageDetails().setOfficeCode(regServiceDetail.getOfficeCode());
			regServiceDetail.getVehicleStoppageDetails().setStaus(Boolean.TRUE);
			regServiceDetail.setApplicationStatus(StatusRegistration.INITIATED);
			vehicleStoppageDetailsDAO.save(regServiceDetail.getVehicleStoppageDetails());
		} else if (regServiceDetail.getServiceIds().stream()
				.anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId()))) {
			RegServiceDTO dto = this.getLatestStoppageDoc(regServiceDetail.getRegistrationDetails());
			dto.setLockedDetails(null);
			dto.getVehicleStoppageDetails().setStaus(Boolean.FALSE);
			dto.setCurrentIndex(4);
			dto.setCurrentRoles(null);
			dto.setApplicationStatus(StatusRegistration.APPROVED);
			dto.setlUpdate(LocalDateTime.now());
			regServiceDAO.save(dto);
			vehicleStoppageDetailsDAO.save(dto.getVehicleStoppageDetails());
			regServiceDetail.getVehicleStoppageDetails().setStoppageApplicationNo(dto.getApplicationNo());
			regServiceDetail.setApplicationStatus(StatusRegistration.INITIATED);
		} else

		if (regServiceDetail.getServiceIds().stream()
				.anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId()))) {
			if (regServiceDetail.getBuyerDetails() != null && regServiceDetail.getBuyerDetails().getSeller() != null) {
				if (regServiceDetail.getBuyerDetails().getBuyer() == null
						&& !(regServiceDetail.getServiceIds().contains(ServiceEnum.RENEWAL.getId())
								|| regServiceDetail.getServiceIds().contains(ServiceEnum.DUPLICATE.getId()))) {
					regServiceDetail.setApplicationStatus(StatusRegistration.SELLERCOMPLETED);
					if (null != regServiceDetail.getRegistrationDetails().getFinanceDetails()
							&& regServiceDetail.getRegistrationDetails().getFinanceDetails().getUserId() != null
							&& !(regServiceDetail.getServiceIds().contains(ServiceEnum.RENEWAL.getId())
									|| regServiceDetail.getServiceIds().contains(ServiceEnum.DUPLICATE.getId()))) {
						regServiceDetail.setApplicationStatus(StatusRegistration.SELLERCOMPLETED);
						MasterUsersDTO userDTO = masterUsersDAO.findByUserId(
								regServiceDetail.getRegistrationDetails().getFinanceDetails().getUserId());
						if (userDTO != null) {
							regServiceDetail.setApplicationStatus(StatusRegistration.TOWITHHPTINITIATED);
						}
					}
				} else {
					regServiceDetail.setApplicationStatus(StatusRegistration.INITIATED);
				}
			} else {
				regServiceDetail.setApplicationStatus(StatusRegistration.INITIATED);
			}
		}

		else if (regServiceDetail.getServiceIds().stream().anyMatch(service -> service.equals(ServiceEnum.VCR.getId()))
				&& regServiceDetail.getGatewayType() != null
				&& regServiceDetail.getGatewayType().equals(GatewayTypeEnum.CASH.getDescription())) {
			regServiceDetail.setApplicationStatus(StatusRegistration.INITIATED);
		} else if (regServiceDetail.getServiceIds().stream()
				.anyMatch(service -> service.equals(ServiceEnum.RCCANCELLATION.getId()))) {
			regServiceDetail.setApplicationStatus(StatusRegistration.APPROVED);
		} else if (regServiceDetail.getServiceIds().stream()
				.anyMatch(service -> service.equals(ServiceEnum.NEWSTAGECARRIAGEPERMIT.getId()))) {
			regServiceDetail.setApplicationStatus(StatusRegistration.CCOAPPROVED);
		} else {
			regServiceDetail.setApplicationStatus(StatusRegistration.INITIATED);
		}
		regServiceDetail.setCreatedDate(LocalDateTime.now());
		regServiceDetail.setCreatedDateStr(LocalDateTime.now().toString());
		regServiceDetail.setPaymentTransactionNo(UUID.randomUUID().toString());
		List<ServiceEnum> serviceEnums = regServiceDetail.getServiceIds().stream()
				.map(id -> ServiceEnum.getServiceEnumById(id)).collect(Collectors.toList());
		regServiceDetail.setServiceType(serviceEnums);

		if (StringUtils.isNoneBlank(regServiceDetail.getMviOfficeCode()) && regServiceDetail.getSlotDetails() != null
				&& regServiceDetail.getSlotDetails().getSlotDate() != null) {
			String slotTime = slotService.bookSlot(ModuleEnum.REG.toString(), null, regServiceDetail.getMviOfficeCode(),
					regServiceDetail.getSlotDetails().getSlotDate());
			regServiceDetail.getSlotDetails().setSlotTime(slotTime);
			regServiceDetail.getSlotDetails().setCreatedDate(LocalDateTime.now());

		}
		if (regServiceDetail.getRegistrationDetails() != null
				&& regServiceDetail.getRegistrationDetails().getApplicantDetails() != null
				&& regServiceDetail.getRegistrationDetails().getApplicantDetails().getAadharResponse() != null) {
			regServiceDetail.getRegistrationDetails().getApplicantDetails().getAadharResponse().setBase64file(null);
		}
	}

	private KeyValue<RegServiceDTO, RegistrationDetailsDTO> getBasicApplicationDetails(RegServiceVO input) {

		Optional<RegistrationDetailsDTO> registrationOptional = registrationDetailDAO.findByPrNo(input.getPrNo());
		if (!registrationOptional.isPresent()) {
			throw new BadRequestException("No  Application Details found. " + input.getPrNo());
		}
		if (StringUtils.isNoneBlank(input.getAadhaarNo()) && (registrationOptional.get().getApplicantDetails() == null
				|| !input.getAadhaarNo().equals(registrationOptional.get().getApplicantDetails().getAadharNo()))) {
			throw new BadRequestException("No  Application Details found with PR No: " + input.getPrNo()
					+ " and aadhaar No: " + input.getAadhaarNo());
		}

		RegistrationDetailsDTO registrationDetailsDTO = registrationOptional.get();
		boolean skipAadharvalidation = Boolean.FALSE;
		if (!registrationDetailsDTO.getApplicantDetails().getIsAadhaarValidated()) {

			skipAadharvalidation = Boolean.TRUE;
			// if aadharvalidation false checking aadharNo correct or not
			
				if (input.getTransactionType()!= null) {
					this.getAadharNoValidation(registrationDetailsDTO, input);
				
			}
		}
		if ((input.getServiceIds() != null
				&& input.getServiceIds().stream().anyMatch(service -> service.equals(ServiceEnum.RCFORFINANCE.getId())))
				&& skipAadharvalidation) {
			skipAadharvalidation = Boolean.FALSE;

		}
		if ((input.getServiceIds() != null
				&& input.getServiceIds().stream().anyMatch(service -> service.equals(ServiceEnum.RCFORFINANCE.getId())))
				&& registrationOptional.get().getApplicantDetails() != null
				&& registrationOptional.get().getApplicantDetails().getPresentAddress() != null
				&& (registrationOptional.get().getApplicantDetails().getPresentAddress().getMandal() == null
						|| registrationOptional.get().getApplicantDetails().getPresentAddress()
								.getDistrict() == null)) {
			freshRCMapper.convertPresentAddressVoToDto(input, registrationDetailsDTO);
		}
		if ((input.getServiceIds().stream().anyMatch(is -> is.equals(ServiceEnum.TAXATION.getId())))) {
			if (skipAadharValidationForTax()) {
				skipAadharvalidation = Boolean.FALSE;
			}
		}

		if (skipAadharvalidation) {
			if (!(input.getTowDetails() != null
					&& input.getTowDetails().getTransferType().equals(TransferType.DEATH))) {
				if(input.getTransactionType()==null) {
				throw new BadRequestException("Please select aadhar seeding service for Seed your aadhar number");
				}
			}
		}
		if (!(input.getServiceIds().stream().anyMatch(is -> is.equals(ServiceEnum.TAXATION.getId()))
				|| input.getServiceIds().stream().anyMatch(is -> is.equals(ServiceEnum.FEECORRECTION.getId()))
				|| input.getServiceIds().stream().anyMatch(is -> is.equals(ServiceEnum.RCFORFINANCE.getId()))
				|| input.getServiceIds().stream()
						.anyMatch(val -> ServiceEnum.getStageCarriagePermitServicesList().contains(val)))) {
			if (input.getContactDetails() == null || StringUtils.isBlank(input.getContactDetails().getMobile())) {
				if (!(input.getServiceIds().stream()
						.anyMatch(val -> ServiceEnum.getPermitRelatedServiceIds().contains(val))))
					logger.error("Contact detials missing  [{}]", input.getPrNo());
				throw new BadRequestException("Contact detials are missing.Pr no: " + input.getPrNo());
			}
		}
		RegServiceDTO regServiceDetails = registrationDetailsMapper.createNew(registrationDetailsDTO, input);
		return new KeyValue<>(regServiceDetails, registrationDetailsDTO);
	}

	private void doCoa(RegServiceVO input, KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {
		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		regServiceDetails.setPresentAdderss(addressMapper.convertVO(input.getPresentAddress()));

		if (input.getPresentAddress().getMandal() == null) {

			throw new BadRequestException("Office Code is not found.");
		}
		if (StringUtils.isNotBlank(input.getPresentAddress().getOtherVillage())) {
			VillageDTO village = new VillageDTO();
			village.setMandalId(input.getPresentAddress().getMandal().getMandalCode());
			village.setVillageName(input.getPresentAddress().getOtherVillage());
			regServiceDetails.getPresentAdderss().setVillage(village);
		}
		if (input.getCoaPermitSameOfficeStatus() != null) {
			regServiceDetails.setCitizenCOAPermitStatus(input.getCoaPermitSameOfficeStatus());
			if (input.getPermitDetailsVO() != null) {
				regServiceDetails.setPdtl(permitDetailsMapper.convertVO(input.getPermitDetailsVO()));
			} else {
				logger.info("permit details not available for pr No [{}]", input.getAadhaarNo());
			}
			regServiceDetails.setPdtl(permitDetailsMapper.convertVO(input.getPermitDetailsVO()));
		}
		if (input.getCoaRecommendationLetterSameOfficeStatus() != null) {
			regServiceDetails
					.setCitizenCOARecommendationLetterStatus(input.getCoaRecommendationLetterSameOfficeStatus());
		}
		regServiceDetails.setIsSameAsAadhaar(input.getIsSameAsAadhaar());
	}

	private void doAlt(RegServiceVO input, KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {
		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		alterationValidations(input.getAlterationVO(), regServiceDetails, Boolean.FALSE, null);
		if (input.getPresentAddress() != null) {
			regServiceDetails.setPresentAdderss(addressMapper.convertVO(input.getPresentAddress()));
		}
		regServiceDetails.setAlterationDetails(alterationMapper.convertVO(input.getAlterationVO()));

		regServiceDetails.setSpecialNoRequired(input.getAlterationVO().isSpecialNoRequired());

		if (regServiceDetails.getAlterationDetails().getVehicleTypeFrom() != null
				&& regServiceDetails.getAlterationDetails().getVehicleTypeTo() != null
				&& !(regServiceDetails.getAlterationDetails().getVehicleTypeFrom()
						.equalsIgnoreCase(regServiceDetails.getAlterationDetails().getVehicleTypeTo()))) {
			regServiceDetails.getServiceIds().add(ServiceEnum.REASSIGNMENT.getId());
		}
		if (input.getAlterationVO().getAlterationService() == null
				|| input.getAlterationVO().getAlterationService().isEmpty()) {
			logger.error("Please select alteration service types: ", regServiceDetails.getPrNo());
			throw new BadRequestException("Please select alteration service types: " + regServiceDetails.getPrNo());
		}
		if (input.getAlterationVO().getAlterationService().stream()
				.anyMatch(vehicleType -> vehicleType.equals(AlterationTypeEnum.WEIGHT))) {
			if (regServiceDetails.getServiceIds().size() > 1) {
				logger.error("For the weight type alteration only select Alteration of vehicle Service : ",
						regServiceDetails.getPrNo());
				throw new BadRequestException(
						"For the weight type alteration only select Alteration of vehicle Service : "
								+ regServiceDetails.getPrNo());
			}
			Integer gvw = regServiceDetails.getRegistrationDetails().getVahanDetails().getGvw();
			if (regServiceDetails.getRegistrationDetails().getClassOfVehicle()
					.equalsIgnoreCase(ClassOfVehicleEnum.ARVT.getCovCode())) {
				if (regServiceDetails.getRegistrationDetails().getVahanDetails().getTrailerChassisDetailsDTO() != null
						&& !regServiceDetails.getRegistrationDetails().getVahanDetails().getTrailerChassisDetailsDTO()
								.isEmpty()) {

					Integer gtw = regServiceDetails.getRegistrationDetails().getVahanDetails()
							.getTrailerChassisDetailsDTO().stream().findFirst().get().getGtw();
					for (TrailerChassisDetailsDTO trailerDetails : regServiceDetails.getRegistrationDetails()
							.getVahanDetails().getTrailerChassisDetailsDTO()) {
						if (trailerDetails.getGtw() > gtw) {
							gtw = trailerDetails.getGtw();
						}
					}
					gvw = gvw + gtw;
				}
			}
			Optional<MasterWeightsForAlt> optionalWeigts = masterWeightsForAltDAO
					.findByToGvwGreaterThanEqualAndFromGvwLessThanEqualAndStatusIsTrue(gvw, gvw);
			if (!optionalWeigts.isPresent()) {
				throw new BadRequestException("Vehicle not eligible to change weight: " + regServiceDetails.getPrNo());
			}
			regServiceDetails.getAlterationDetails().setGvw(optionalWeigts.get().getGvw());
			if (isNeedtoAddVariationOfPermit(regServiceDetails.getRegistrationDetails())) {
				regServiceDetails.getServiceIds().add(ServiceEnum.VARIATIONOFPERMIT.getId());
			}
		} else {
			if (this.isPermitActiveOrNot(regServiceDetails.getPrNo(),
					regServiceDetails.getRegistrationDetails().getVehicleType())) {
				if (!input.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.SURRENDEROFPERMIT.getId()))) {
					regServiceDetails.getServiceIds().add(ServiceEnum.VARIATIONOFPERMIT.getId());
				}
			}
		}

		if (regServiceDetails.getAlterationDetails().getCov() != null
				&& StringUtils.isNoneBlank(regServiceDetails.getAlterationDetails().getCov())) {
			if (regServiceDetails.getAlterationDetails().getCov()
					.equalsIgnoreCase(ClassOfVehicleEnum.EIBT.getCovCode())) {
				if (regServiceDetails.getRegistrationDetails().getPrGeneratedDate().toLocalDate().plusYears(15)
						.isBefore(LocalDate.now())) {
					logger.error("Age of the vehicle is more than 15 years, " + ClassOfVehicleEnum.EIBT.getCovCode()
							+ " not eligeble to alter: ", regServiceDetails.getPrNo());
					throw new BadRequestException(
							"Age of the vehicle is more than 15 years, " + ClassOfVehicleEnum.EIBT.getCovCode()
									+ " not eligeble to alter: " + regServiceDetails.getPrNo());
				}
			}
		}
	}

	@Override
	public void alterationValidations(AlterationVO input, RegServiceDTO regServiceDetails, boolean isRequestFormMVI,
			StatusRegistration status) {
		if (!(isRequestFormMVI && status != null && !status.equals(StatusRegistration.APPROVED))) {
			if (input.getAlterationService() != null && !input.getAlterationService().isEmpty()) {
				if (input.getAlterationService().stream()
						.anyMatch(vehicleType -> vehicleType.equals(AlterationTypeEnum.VEHICLE))) {
					alterVehicleTypeValidations(input, regServiceDetails);
				}
				if (input.getAlterationService().stream()
						.anyMatch(vehicleType -> vehicleType.equals(AlterationTypeEnum.FUEL))) {
					alterFuelTypeValidations(input, regServiceDetails);
				}
				if (input.getAlterationService().stream()
						.anyMatch(vehicleType -> vehicleType.equals(AlterationTypeEnum.BODY))) {
					alterBodyTypeValidations(input, regServiceDetails);
				}
				if (input.getAlterationService().stream()
						.anyMatch(vehicleType -> vehicleType.equals(AlterationTypeEnum.SEATING))) {
					alterSeatTypeValidations(input, regServiceDetails);
				}
				if (input.getAlterationService().stream()
						.anyMatch(vehicleType -> vehicleType.equals(AlterationTypeEnum.WEIGHT))) {
					alterWeightValidations(input, regServiceDetails);
				}
			}
		}
	}

	private void alterSeatTypeValidations(AlterationVO input, RegServiceDTO regServiceDetails) {
		if (StringUtils.isBlank(input.getFromSeatingCapacity())) {
			logger.error("From Seatting capacity  missing: ", regServiceDetails.getPrNo());
			throw new BadRequestException("From Seatting capacity  missing: " + regServiceDetails.getPrNo());
		}
		if (StringUtils.isBlank(input.getSeating())) {
			logger.error("please enter seats capacity: ", regServiceDetails.getPrNo());
			throw new BadRequestException("please enter seats capacity: " + regServiceDetails.getPrNo());
		}
		String cov = input.getCov() != null ? input.getCov()
				: regServiceDetails.getRegistrationDetails().getClassOfVehicle();
		String category = input.getVehicleTypeTo() != null ? input.getVehicleTypeTo()
				: regServiceDetails.getRegistrationDetails().getVehicleType();
		SeatConversionVO vo = this.getSeats(cov, category);
		if (!(Integer.parseInt(input.getSeating()) <= Integer.parseInt(vo.getSeatTo())
				&& Integer.parseInt(input.getSeating()) >= Integer.parseInt(vo.getSeatFrom()))) {
			logger.error("please enter seats capacity with in the range: [{}]  , [{}]", vo.getSeatFrom(),
					vo.getSeatFrom());
			throw new BadRequestException(
					"please enter seats capacity with in the range: " + vo.getSeatFrom() + " and " + vo.getSeatFrom());
		}
	}

	private void alterWeightValidations(AlterationVO input, RegServiceDTO regServiceDetails) {

		if (input.getAlterationService().size() > 1) {
			logger.error("please select only weight type alteration");
			throw new BadRequestException("please select only weight type alteration");
		}
		Optional<RegistrationDetailsDTO> registrationOptional = registrationDetailDAO
				.findByPrNo(regServiceDetails.getPrNo());
		if (!registrationOptional.isPresent()) {
			logger.error("No record found. [{}]", regServiceDetails.getPrNo());
			throw new BadRequestException("No record found.application no: " + regServiceDetails.getPrNo());
		}
		RegistrationDetailsDTO regDto = registrationOptional.get();
		this.validationForWeightAlt(regDto);
	}

	private void alterBodyTypeValidations(AlterationVO input, RegServiceDTO regServiceDetails) {
		if (StringUtils.isBlank(input.getBodyType())) {
			logger.error("Please select body type : ", regServiceDetails.getPrNo());
			throw new BadRequestException("Please select body type: " + regServiceDetails.getPrNo());
		}
		if (StringUtils.isBlank(input.getFrombodyType())) {
			logger.error("From body type missing: ", regServiceDetails.getPrNo());
			throw new BadRequestException("From body type missing: " + regServiceDetails.getPrNo());
		}
		if (!input.getFrombodyType()
				.equalsIgnoreCase(regServiceDetails.getRegistrationDetails().getVahanDetails().getBodyTypeDesc())) {
			logger.error("Body type missmatching: ", regServiceDetails.getPrNo());
			throw new BadRequestException("Body type missmatching: " + regServiceDetails.getPrNo());
		}
		if (input.getUlw() == null) {
			logger.error("Please enter ulw weight: ", regServiceDetails.getPrNo());
			throw new BadRequestException("Please enter ulw weight: " + regServiceDetails.getPrNo());
		}
		if (regServiceDetails.getRegistrationDetails().getVahanDetails().getGvw() != null) {
			if (input.getUlw() > regServiceDetails.getRegistrationDetails().getVahanDetails().getGvw()) {
				logger.error("Please enter ulw weight less than RLW: ",
						regServiceDetails.getRegistrationDetails().getVahanDetails().getGvw());
				throw new BadRequestException("Please enter ulw weight less than RLW : "
						+ regServiceDetails.getRegistrationDetails().getVahanDetails().getGvw());
			}
		}

		List<BodyTypeVO> bodyTypeVo = this
				.getBodyType(regServiceDetails.getRegistrationDetails().getVahanDetails().getBodyTypeDesc());
		boolean flag = Boolean.FALSE;
		for (BodyTypeVO vo : bodyTypeVo) {
			if (vo.getBodyType().equalsIgnoreCase(input.getBodyType())) {
				flag = Boolean.TRUE;
			}
		}
		if (!flag) {
			logger.error("Body type miss matched from master : ", regServiceDetails.getPrNo());
			throw new BadRequestException("Body type miss matched from master : " + regServiceDetails.getPrNo());
		}
	}

	private void alterFuelTypeValidations(AlterationVO input, RegServiceDTO regServiceDetails) {
		if (StringUtils.isBlank(input.getFromFuel())) {
			logger.error("From fuel missing : ", regServiceDetails.getPrNo());
			throw new BadRequestException("From fuel missing: " + regServiceDetails.getPrNo());
		}
		if (StringUtils.isBlank(input.getFuel())) {
			logger.error("fuel missing : ", regServiceDetails.getPrNo());
			throw new BadRequestException("fuel missing: " + regServiceDetails.getPrNo());
		}
		String cov = input.getCov() != null ? input.getCov()
				: regServiceDetails.getRegistrationDetails().getClassOfVehicle();
		List<FuelConversionVO> fuelVo = this
				.getfuel(regServiceDetails.getRegistrationDetails().getVahanDetails().getFuelDesc(), cov);
		boolean flag = Boolean.FALSE;
		for (FuelConversionVO vo : fuelVo) {
			if (vo.getFuel().equalsIgnoreCase(input.getFuel())) {
				flag = Boolean.TRUE;
			}
		}
		if (!flag) {
			logger.error("fuel miss matched from master : ", regServiceDetails.getPrNo());
			throw new BadRequestException("fuel miss matched from master : " + regServiceDetails.getPrNo());
		}
		if (StringUtils.isBlank(input.getGasKitNo())) {
			logger.error("Please enter gasGit number : ", regServiceDetails.getPrNo());
			throw new BadRequestException("Please enter gasGit number : " + regServiceDetails.getPrNo());
		}
		if (StringUtils.isBlank(input.getAgencyDetails())) {
			logger.error("Please enter agency details : ", regServiceDetails.getPrNo());
			throw new BadRequestException("Please enter agency details : " + regServiceDetails.getPrNo());
		}
		if (input.getGasKitValidity() == null) {
			logger.error("Please enter gas git validity : ", regServiceDetails.getPrNo());
			throw new BadRequestException("Please enter gas git validity : " + regServiceDetails.getPrNo());
		}
		if (input.getGasKitValidity().isEqual(LocalDate.now()) || input.getGasKitValidity().isBefore(LocalDate.now())) {
			logger.error("Please enter gas git validity feature date : ", regServiceDetails.getPrNo());
			throw new BadRequestException(
					"Please enter gas git validity feature date : " + regServiceDetails.getPrNo());
		}

	}

	private void alterVehicleTypeValidations(AlterationVO input, RegServiceDTO regServiceDetails) {
		if (StringUtils.isBlank(input.getVehicleTypeFrom()) || StringUtils.isBlank(input.getVehicleTypeTo())) {
			logger.error("Please select vehicle category: ", regServiceDetails.getPrNo());
			throw new BadRequestException("Please select vehicle category: " + regServiceDetails.getPrNo());
		}
		if (StringUtils.isBlank(input.getFromCov()) || StringUtils.isBlank(input.getCov())) {
			logger.error("Please select vehicle : ", regServiceDetails.getPrNo());
			throw new BadRequestException("Please select vehicle: " + regServiceDetails.getPrNo());
		}
		if (!StringUtils.isBlank(input.getFromCov()) && !input.getFromCov()
				.equalsIgnoreCase(regServiceDetails.getRegistrationDetails().getClassOfVehicle())) {
			logger.error("Old class of vehicle not matched in registration details : ", regServiceDetails.getPrNo());
			throw new BadRequestException(
					"Old class of vehicle not matched in registration details: " + regServiceDetails.getPrNo());
		}
		if (this.isPermitActiveOrNot(regServiceDetails.getPrNo(),
				regServiceDetails.getRegistrationDetails().getVehicleType())) {
			logger.error("Please surrender vehicle  permit : ", regServiceDetails.getPrNo());
			throw new BadRequestException("Please surrender vehicle permit: " + regServiceDetails.getPrNo());
		}
		if (StringUtils.isNoneBlank(input.getCov())
				&& input.getCov().equalsIgnoreCase(ClassOfVehicleEnum.EIBT.getCovCode())
				&& !regServiceDetails.getRegistrationDetails().getOwnerType().equals(OwnerTypeEnum.Organization)) {
			logger.error("For eib alteration only organization can do: ", regServiceDetails.getPrNo());
			throw new BadRequestException(
					"For eib alteration only organization can do: " + regServiceDetails.getPrNo());
		}
		if (!StringUtils.isBlank(input.getCov()) && !StringUtils.isBlank(input.getVehicleTypeTo())) {
			if (input.getFromCov().equalsIgnoreCase(ClassOfVehicleEnum.IVCN.getCovCode())) {
				List<RegServiceApprovedDTO> listOfDtos = regServiceApprovedDAO
						.findByRegistrationDetailsApplicationNoAndServiceIds(
								regServiceDetails.getRegistrationDetails().getApplicationNo(),
								ServiceEnum.ALTERATIONOFVEHICLE.getId());
				if (!listOfDtos.isEmpty()) {
					boolean flag = Boolean.FALSE;
					for (RegServiceApprovedDTO dto : listOfDtos) {
						if (getListOfTwoWeelerCovs().stream().anyMatch(
								cov -> cov.equalsIgnoreCase(dto.getRegistrationDetails().getClassOfVehicle()))) {
							flag = Boolean.TRUE;
						}
					}
					if (flag && !getListOfTwoWeelerCovs().stream()
							.anyMatch(cov -> cov.equalsIgnoreCase(input.getCov()))) {
						logger.error("Please select proper class of vehicle : ", regServiceDetails.getPrNo());
						throw new BadRequestException(
								"Please select proper class of vehicle: " + regServiceDetails.getPrNo());
					} else if (!flag && getListOfTwoWeelerCovs().stream()
							.anyMatch(cov -> cov.equalsIgnoreCase(input.getCov()))) {
						logger.error("Please select proper class of vehicle : ", regServiceDetails.getPrNo());
						throw new BadRequestException(
								"Please select proper class of vehicle: " + regServiceDetails.getPrNo());
					}
				}
			}
			Optional<ClassOfVehicleConversion> covConversion = classOfVehicleConversionDAO
					.findByNewCovAndNewCategoryAndCovAndCategory(input.getCov(), input.getVehicleTypeTo(),
							input.getFromCov(), input.getVehicleTypeFrom());
			if (!covConversion.isPresent()) {
				logger.error("no records found in master conversion document : ", regServiceDetails.getPrNo());
				throw new BadRequestException(
						"no records found in master conversion document: " + regServiceDetails.getPrNo());
			}

			if (!covConversion.get().getNewCov().equalsIgnoreCase(input.getCov())) {
				logger.error("please selectg new class of vehicle : ", regServiceDetails.getPrNo());
				throw new BadRequestException("please selectg new class of vehicle: " + regServiceDetails.getPrNo());

			}
			Optional<SeatConversion> optionalSeat = seatConversionDAO.findByCovAndCategory(input.getCov(),
					input.getVehicleTypeTo());

			if (optionalSeat.isPresent()) {
				SeatConversion vo = optionalSeat.get();
				if (StringUtils.isNoneBlank(vo.getSeatFrom()) && StringUtils.isNoneBlank(vo.getSeatTo())) {
					String seats = regServiceDetails.getRegistrationDetails().getVahanDetails().getSeatingCapacity();
					if (input.getAlterationService().stream()
							.anyMatch(service -> service.equals(AlterationTypeEnum.SEATING))) {
						seats = input.getSeating();
					}
					if (!(Integer.parseInt(seats) <= Integer.parseInt(vo.getSeatTo())
							&& Integer.parseInt(seats) >= Integer.parseInt(vo.getSeatFrom()))) {
						logger.error("please enter seats capacity with in the range: ", vo.getSeatFrom(), " and ",
								vo.getSeatTo());
						throw new BadRequestException("please enter seats capacity with in the range: "
								+ vo.getSeatFrom() + " and " + vo.getSeatTo());
					}
				}
			}
			// SeatConversionVO vo = this.getSeats(input.getCov(),
			// input.getVehicleTypeTo());

		}
	}

	private List<String> getListOfTwoWeelerCovs() {
		List<String> list = new ArrayList<>();
		list.add(ClassOfVehicleEnum.MCYN.getCovCode());
		list.add(ClassOfVehicleEnum.MMCN.getCovCode());
		list.add(ClassOfVehicleEnum.MCPT.getCovCode());
		return list;

	}

	@Override
	public void setMviOfficeDetails(RegServiceDTO regServiceDetails, RegServiceVO regServiceVO) {
		Pair<OfficeVO, String> officeVO = null;
		/*
		 * Optional<MandalDTO> optionalMandalDTO =
		 * getMandal(regServiceDetails.getRegistrationDetails()
		 * .getApplicantDetails().getPresentAddress().getMandal().getMandalCode( ));
		 */
		if ((regServiceDetails.getServiceIds().stream().anyMatch(service -> service.equals(ServiceEnum.NEWFC.getId()))
				|| regServiceDetails.getServiceIds().stream()
						.anyMatch(service -> service.equals(ServiceEnum.RENEWALFC.getId()))
				|| regServiceDetails.getServiceIds().stream()
						.anyMatch(service -> service.equals(ServiceEnum.OTHERSTATIONFC.getId())))
				&& regServiceDetails.getServiceIds().size() == 1 && null != regServiceVO.getNewOfficeDetails()
				&& regServiceVO.getNewOfficeDetails().getOfficeCode() != null) {
			// regServiceDetails.setMviOfficeDetails(regServiceDetails.getOfficeDetails());
			// regServiceDetails.setMviOfficeCode(regServiceDetails.getOfficeDetails().getOfficeCode());
			regServiceDetails.setMviOfficeDetails(
					officeDAO.findByOfficeCode(regServiceVO.getNewOfficeDetails().getOfficeCode()).get());
			regServiceDetails.setMviOfficeCode(regServiceDetails.getMviOfficeDetails().getOfficeCode());
		} else if (regServiceDetails.getServiceIds().stream()
				.anyMatch(service -> service.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))
				|| regServiceDetails.getServiceIds().stream()
						.anyMatch(service -> service.equals(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId()))) {

			Integer mandalCode = regServiceVO.getPresentAddress().getMandal().getMandalCode();
			officeVO = this.getOffice(mandalCode, regServiceDetails.getRegistrationDetails().getVehicleType(),
					regServiceDetails.getRegistrationDetails().getOwnerType().toString(), StringUtils.EMPTY);
			if (StringUtils.isBlank(officeVO.getFirst().getReportingoffice())) {
				logger.error("reporting office not found for office:  ", officeVO.getSecond());
				throw new BadRequestException("reporting office not found for office:  " + officeVO.getSecond());
			}
			regServiceDetails.setDtcOfficeCode(officeVO.getFirst().getReportingoffice());

		} else {
			Integer mandalCode = 0;
			if (!regServiceDetails.getServiceIds().stream()
					.anyMatch(service -> service.equals(ServiceEnum.DATAENTRY.getId()))) {

				if (regServiceDetails.getRegistrationDetails().getApplicantDetails().getPresentAddress()
						.getMandal() == null
						|| regServiceDetails.getRegistrationDetails().getApplicantDetails().getPresentAddress()
								.getMandal().getMandalCode() == null) {
					if (regServiceDetails.getPresentAdderss() != null
							&& regServiceDetails.getPresentAdderss().getMandal() != null
							&& regServiceDetails.getPresentAdderss().getMandal().getMandalCode() != null) {
						mandalCode = regServiceDetails.getPresentAdderss().getMandal().getMandalCode();
					} else {
						throw new BadRequestException("Please select mandal details  : " + regServiceDetails.getPrNo());
					}

				} else {

					mandalCode = regServiceDetails.getRegistrationDetails().getApplicantDetails().getPresentAddress()
							.getMandal().getMandalCode();
				}
			}
			if (regServiceDetails.getServiceIds().stream()
					.anyMatch(service -> service.equals(ServiceEnum.DATAENTRY.getId()))) {
				mandalCode = regServiceDetails.getRegistrationDetails().getApplicantDetails().getPresentAddress()
						.getMandal().getMandalCode();
			}
			String vehicleType = regServiceDetails.getRegistrationDetails().getVehicleType();
			if (regServiceDetails.getServiceIds().stream()
					.anyMatch(id -> id.equals(ServiceEnum.ALTERATIONOFVEHICLE.getId()))
					&& StringUtils.isNoneBlank(regServiceDetails.getAlterationDetails().getVehicleTypeTo())) {
				vehicleType = regServiceDetails.getAlterationDetails().getVehicleTypeTo();
			}
			boolean addressValidation = false;
			if (!(regServiceDetails.getServiceIds().contains(ServiceEnum.RENEWAL.getId())
					&& regServiceDetails.getServiceIds().contains(ServiceEnum.TRANSFEROFOWNERSHIP.getId()))) {
				addressValidation = true;
				if (regServiceDetails.getServiceIds().stream()
						.anyMatch(id -> (id.equals(ServiceEnum.CHANGEOFADDRESS.getId())
								|| id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId())))) {

					if (regServiceDetails.getPresentAdderss() == null) {
						logger.error("pressent address missed in reg service   : ", regServiceDetails.getPrNo());
						throw new BadRequestException(
								"pressent address missed in reg service  : " + regServiceDetails.getPrNo());
					}
					officeVO = this.getOffice(regServiceDetails.getPresentAdderss().getMandal().getMandalCode(),
							vehicleType, regServiceDetails.getRegistrationDetails().getOwnerType().toString(),
							StringUtils.EMPTY);
					// mandalCode =
					// regServiceDetails.getPresentAdderss().getMandal().getMandalCode();
				} else {
					if (mandalCode != null) {
						officeVO = this.getOffice(mandalCode, vehicleType,
								regServiceDetails.getRegistrationDetails().getOwnerType().toString(),
								StringUtils.EMPTY);
						/*
						 * mandalCode = regServiceDetails.getRegistrationDetails().
						 * getApplicantDetails(). getPresentAddress() .getMandal().getMandalCode();
						 */
					}
				}
			}
			if (!addressValidation) {
				officeVO = this.getOffice(mandalCode, vehicleType,
						regServiceDetails.getRegistrationDetails().getOwnerType().toString(), StringUtils.EMPTY);
				/*
				 * mandalCode = regServiceDetails.getRegistrationDetails().
				 * getApplicantDetails(). getPresentAddress() .getMandal().getMandalCode();
				 */
			}

		}
		if (officeVO != null) {
			regServiceDetails.setMviOfficeDetails(officeMapper.convertVO(officeVO.getFirst()));
			// regServiceDetails.setMviOfficeCode(regServiceDetails.getMviOfficeDetails().getOfficeCode());
			regServiceDetails.setMviOfficeCode(officeVO.getSecond());
		}
	}

	private OfficeVO getOfficeDetails(RegServiceVO input, RegServiceDTO regServiceDetails) {
		Optional<OfficeVO> officeDetails = Optional.empty();
		if (input.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.TAXATION.getId()))) {

			if (skipAadharValidationForTax()) {
				return officeMappar.convertEntity(regServiceDetails.getRegistrationDetails().getOfficeDetails());
			}
		}
		if (input.getAlterationVO() != null && input.getAlterationVO().getVehicleTypeTo() != null) {
			if (input.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.CHANGEOFADDRESS.getId()))) {
				officeDetails = mandalService.getOfficeDetailsByMandal(
						input.getPresentAddress().getMandal().getMandalCode(),
						input.getAlterationVO().getVehicleTypeTo());
			} else {
				officeDetails = mandalService.getOfficeDetailsByMandal(regServiceDetails.getRegistrationDetails()
						.getApplicantDetails().getPresentAddress().getMandal().getMandalCode(),
						input.getAlterationVO().getVehicleTypeTo());
			}
		} else if (input.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.CHANGEOFADDRESS.getId()))) {
			officeDetails = mandalService.getOfficeDetailsByMandal(
					input.getPresentAddress().getMandal().getMandalCode(),
					regServiceDetails.getRegistrationDetails().getVehicleType());
		} else if (input.getServiceIds().stream().anyMatch(id -> scrtServices().contains(id))) {
			OfficeVO vo = new OfficeVO();
			vo.setOfficeCode("APSTA");
			officeDetails = Optional.of(vo);

		} else {
			if (!input.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.CHANGEOFADDRESS.getId()))) {
				if (regServiceDetails.getRegistrationDetails().getApplicantDetails().getPresentAddress()
						.getMandal() == null) {
					officeDetails = mandalService.getOfficeDetailsByMandal(
							input.getPresentAddress().getMandal().getMandalCode(),
							regServiceDetails.getRegistrationDetails().getVehicleType());
				} else {
					officeDetails = mandalService.getOfficeDetailsByMandal(
							regServiceDetails.getRegistrationDetails().getApplicantDetails().getPresentAddress()
									.getMandal().getMandalCode(),
							regServiceDetails.getRegistrationDetails().getVehicleType());
				}
			}
		}
		if (!officeDetails.isPresent()) {
			logger.error("office details not found for  : ", regServiceDetails.getPrNo());
			throw new BadRequestException("office details not found for  : " + regServiceDetails.getPrNo());
		}
		return officeDetails.get();
	}

	@Override
	public Pair<OfficeVO, String> getOffice(Integer mandalId, String vehicleType, String ownerType, String appFormId) {
		logger.debug("calling getOfficeDetailsByMandal mandalCode:" + mandalId);

		Optional<OfficeVO> officeVO = Optional.empty();
		String mviOfficeCode = StringUtils.EMPTY;
		if (StringUtils.isNoneBlank(appFormId)) {
			RegServiceDTO dto = this.getRegServiceDetails(appFormId);
			if ((dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWFC.getId()))
					|| dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RENEWALFC.getId()))
					|| dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.OTHERSTATIONFC.getId())))
					&& dto.getServiceIds().size() == 1 && dto.getFitnessOtherStation()) {
				if (dto.isAllowFcForOtherStation()) {
					officeVO = officeMappar.convertEntity(Optional.of(dto.getMviOfficeDetails()));
					return Pair.of(officeVO.get(), officeVO.get().getOfficeCode());
				} else {
					return getOfficeDetailsBasedOnMandal(mandalId, vehicleType, mviOfficeCode);
				}

			}
		}
		return getOfficeDetailsBasedOnMandal(mandalId, vehicleType, mviOfficeCode);
	}

	private Pair<OfficeVO, String> getOfficeDetailsBasedOnMandal(Integer mandalId, String vehicleType,
			String mviOfficeCode) {
		Optional<OfficeVO> officeVO;
		Optional<MandalDTO> optionalMandalDTO = getMandal(mandalId);
		if (StringUtils.isBlank(optionalMandalDTO.get().getMviAddressOfficeCode())) {
			officeVO = mandalService.getOfficeDetailsByMandal(mandalId, MandalServiceEnum.h.toString());
			mviOfficeCode = optionalMandalDTO.get().getHsrpoffice();
		} else {
			Optional<OfficeDTO> optionalOfficeDTO = officeDAO
					.findByOfficeCode(optionalMandalDTO.get().getMviAddressOfficeCode());
			if (!optionalOfficeDTO.isPresent()) {
				logger.error("office details not found for  : ", optionalMandalDTO.get().getMviAddressOfficeCode());
				throw new BadRequestException(
						"office details not found for  : " + optionalMandalDTO.get().getMviAddressOfficeCode());
			}

			if (vehicleType.equalsIgnoreCase(CovCategory.N.getCode())) {
				mviOfficeCode = optionalMandalDTO.get().getNonTransportOffice();
			} else if (vehicleType.equalsIgnoreCase(CovCategory.T.getCode())) {
				mviOfficeCode = optionalMandalDTO.get().getTransportOfice();
			}
			officeVO = officeMappar.convertEntity(optionalOfficeDTO);
		}
		return Pair.of(officeVO.get(), mviOfficeCode);
	}

	private Optional<MandalDTO> getMandal(Integer mandalId) {
		Optional<MandalDTO> optionalMandalDTO = mandalDAO.findByMandalCode(mandalId);
		if (!optionalMandalDTO.isPresent()) {
			throw new BadRequestException("No Details found with given madal");
		}
		return optionalMandalDTO;
	}

	private void doDuplicate(RegServiceVO input, KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {
		if (input.getDuplicateDetailsVO() != null && input.getDuplicateDetailsVO().getFirDate() != null) {
			if (input.getDuplicateDetailsVO().getFirDate().atStartOfDay()
					.isBefore(citizenObjects.getValue().getPrGeneratedDate())) {
				throw new BadRequestException(
						"FIR date should be greater than prGeneratedDate for prNo :" + input.getPrNo());
			}
		}

		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		regServiceDetails.setDuplicateDetails(duplicateDetailsMapper.convertVO(input.getDuplicateDetailsVO()));
		/*
		 * if(regServiceDetails.getOfficeCode()== null &&
		 * StringUtils.isEmpty(regServiceDetails.getOfficeCode()) &&
		 * regServiceDetails.getOfficeDetails()==null) { if
		 * (citizenObjects.getValue().getVehicleType().equalsIgnoreCase( CovCategory.N.
		 * getCode())) { coaNonTransportOfficeDetails(input, regServiceDetails); } else
		 * { coaTransportOfficeDetails(input, regServiceDetails); } }
		 */

	}
	/*
	 * private void coaOfficeDetails(RegServiceVO input, RegServiceDTO
	 * regServiceDetails) { OfficeVO officeDetails =
	 * this.getOffice(input.getPresentAddress().getMandal().getMandalCode() ,
	 * regServiceDetails.getRegistrationDetails().getVehicleType(),
	 * regServiceDetails .getRegistrationDetails().getOwnerType().toString());
	 * regServiceDetails.setOfficeDetails(officeDetails);
	 * regServiceDetails.setOfficeCode(officeDetails.getOfficeCode()); }
	 */

	/*
	 * private void coaNonTransportOfficeDetails(RegServiceVO input, RegServiceDTO
	 * regServiceDetails) { OfficeDTO officeDetails =
	 * this.getOffice(input.getPresentAddress().getMandal().
	 * getNonTransportOffice()) ; regServiceDetails.setOfficeDetails(officeDetails);
	 * regServiceDetails.setOfficeCode(officeDetails.getOfficeCode()); }
	 */

	/*
	 * private void OfficeDetails(RegServiceDTO regServiceDetails) { OfficeDTO
	 * officeDetails =
	 * this.getOffice(citizenObjects.getValue().getApplicantDetails().
	 * getPresentAddress() .getMandal().getNonTransportOffice());
	 * regServiceDetails.setOfficeDetails(officeDetails);
	 * regServiceDetails.setOfficeCode(officeDetails.getOfficeCode()); }
	 */

	/*
	 * private void transportOfficeDetails(KeyValue<RegServiceDTO,
	 * RegistrationDetailsDTO> citizenObjects, RegServiceDTO regServiceDetails) {
	 * OfficeDTO officeDetails = this.getOffice(
	 * citizenObjects.getValue().getApplicantDetails().getPresentAddress().
	 * getMandal ().getTransportOfice());
	 * regServiceDetails.setOfficeDetails(officeDetails);
	 * regServiceDetails.setOfficeCode(officeDetails.getOfficeCode()); }
	 */

	/*
	 * public OfficeDTO getOffice(String officeCode) {
	 *
	 * Optional<OfficeDTO> officeDtoOptional =
	 * officeDAO.findByOfficeCode(officeCode);
	 *
	 * if (!officeDtoOptional.isPresent()) {
	 *
	 * throw new BadRequestException("Not able to found Office details by code:" +
	 * officeCode); }
	 *
	 * return officeDtoOptional.get();
	 *
	 * }
	 */

	private void doTow(RegServiceVO input, KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {

		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		RegistrationDetailsDTO regDTO = citizenObjects.getValue();
		if (input.getTowDetails() != null) {

			if (input.getTowDetails().getSeller() != null) {
				String tokenNo = String.valueOf(System.currentTimeMillis());
				LocalDateTime tokenNoGeneratedTime = LocalDateTime.now();
				if (input.getToEnclouserStatus() != null) {
					input.getTowDetails().setSellerFinanceStatus(input.getToEnclouserStatus());
				}
				regServiceDetails.setBuyerDetails(towMapper.convertVO(input.getTowDetails()));
				// remove this after online financier implementation
				if (input.getToEnclouserStatus() != null) {
					regServiceDetails.getBuyerDetails().setSellerFinanceStatus(
							FinanceTowEnum.getFinanceTowEnumById(input.getToEnclouserStatus()).toString());
				}
				Boolean value = input.getServiceIds().stream()
						.anyMatch(id -> id.equals(ServiceEnum.HIREPURCHASETERMINATION.getId()));
				if (regDTO.getFinanceDetails() != null && !value) {
					throw new BadRequestException("Please do HPT service");

				}
				if (value) {
					regServiceDetails.getBuyerDetails()
							.setSellerFinanceStatus(FinanceTowEnum.getFinanceTowEnumById(2).toString());
				}
				if (null != input.getIsContinueFinancier()
						&& input.getIsContinueFinancier() == FinanceTowEnum.CONTINUEWITHFINANCE.getId()) {
					regServiceDetails.getBuyerDetails()
							.setSellerFinanceStatus(FinanceTowEnum.getFinanceTowEnumById(1).toString());
					regServiceDetails.getBuyerDetails().setSellerFinanceType(
							FinanceTowEnum.getFinanceTowEnumById(input.getFinancierType()).toString());
					if (input.getFinancierType() == FinanceTowEnum.ONLINE.getId()) {
						regServiceDetails.getBuyerDetails()
								.setSellerFinancierId(regDTO.getFinanceDetails().getUserId());
					}

				}
				if (input.getSellerPermitStatus() != null) {
					regServiceDetails.getBuyerDetails().setSellerPermitStatus(input.getSellerPermitStatus());
					if (input.getSellerPermitStatus().equals(TransferType.permitTranfer.PERMITTRANSFER)) {
						List<PermitDetailsDTO> permitDto = temporaryPermitValidation(input.getPrNo());
						if (!CollectionUtils.isEmpty(permitDto)) {
							permitDto = permitDto.stream()
									.filter(id -> id.getPermitClass().getDescription()
											.equals(PermitsEnum.PermitType.TEMPORARY.getDescription()))
									.collect(Collectors.toList());
							if (!CollectionUtils.isEmpty(permitDto)) {
								for (PermitDetailsDTO tempararyPermitDetails : permitDto) {
									if (tempararyPermitDetails.getPermitStatus()
											.equals(PermitsEnum.ACTIVE.getDescription())
											&& tempararyPermitDetails.getPermitValidityDetails().getPermitValidTo()
													.isAfter(LocalDate.now())) {
										throw new BadRequestException(
												"Please Cancel all your temporary permits before applying TOW on PRNO:"
														+ input.getPrNo());
									}
								}
							}
						}
					}

				}

				regServiceDetails.getBuyerDetails()
						.setSellerRecommedationLetterStatus(input.getSellerRecommendationLetterStatus());
				if (input.getSellerRecommendationLetterStatus() != null && input.getSellerRecommendationLetterStatus()
						.equals(TransferType.permitTranfer.RECOMMENDATIONLETTERSURRENDER)) {
					List<PermitDetailsDTO> permitDto = temporaryPermitValidation(input.getPrNo());
					if (!CollectionUtils.isEmpty(permitDto)) {
						permitDto = permitDto.stream()
								.filter(id -> id.getPermitClass().getDescription()
										.equals(PermitsEnum.PermitType.COUNTER_SIGNATURE.getDescription()))
								.collect(Collectors.toList());
						if (!CollectionUtils.isEmpty(permitDto)) {
							for (PermitDetailsDTO tempararyPermitDetails : permitDto) {
								if (tempararyPermitDetails.getPermitStatus().equals(PermitsEnum.ACTIVE.getDescription())
										&& tempararyPermitDetails.getPermitValidityDetails().getPermitValidTo()
												.isAfter(LocalDate.now())) {
									throw new BadRequestException(
											"Please Cancel all your temporary permits before applying TOW on PRNO:"
													+ input.getPrNo());
								}
							}
						}
					}
				}
				regServiceDetails.getBuyerDetails().setTokenNo(tokenNo);
				regServiceDetails.getBuyerDetails().setTokenNoGeneratedTime(tokenNoGeneratedTime);
				regServiceDetails.getBuyerDetails().setTokenStatus(Boolean.TRUE);
			}
		} else {
			throw new BadRequestException(appMessages.getResponseMessage("Tow details not found:" + input.getPrNo()));

		}

	}

	private void doRenewal(RegServiceVO input, KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {

		RegistrationDetailsDTO regDetails = citizenObjects.getValue();
		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		if (input.getPresentAddress() != null) {
			regServiceDetails.setPresentAdderss(addressMapper.convertVO(input.getPresentAddress()));
		}
		List<String> errors = new ArrayList<>();

		checkCovForRenewal(regDetails, errors);
		if (!errors.isEmpty()) {
			logger.error("Application not eligible for renewal: ", regDetails.getPrNo());
			throw new BadRequestException("Application not eligible for renewal: " + regDetails.getPrNo());
		}
		// setMviOfficeDetails(regServiceDetails);
	}

	private void doTheftIntimation(RegServiceVO input, KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {
		if (input.getTheftDetails() != null && input.getTheftDetails().getFirYear() != null) {
			if (input.getTheftDetails().getFirYear() > LocalDate.now().getYear()) {
				throw new BadRequestException("FirYear is Future Date");
			}
			if (input.getTheftDetails().getComplaintDate().getYear() > LocalDate.now().getYear()) {
				throw new BadRequestException("Complaint Date Year is Future Date");
			}
		}

		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		TheftVehicleDetailsVO theftDetailsVO = input.getTheftDetails();
		TheftVehicleDetailsDTO theftDetailsDTO = theftDetailsMapper.convertVO(theftDetailsVO);
		regServiceDetails.setTheftDetails(theftDetailsDTO);
		registratrionServicesApprovals.initiateApprovalProcessFlow(regServiceDetails);

	}

	private void doTheftRevocation(RegServiceVO input, KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {
		// RegServiceDTO regServiceDetails = citizenObjects.getKey();'
		List<RegServiceDTO> regList = regServiceDAO.findByPrNoAndServiceTypeNotIn(input.getPrNo(),
				ServiceEnum.getServiceEnumById(ServiceEnum.TAXATION.getId()));
		if (!regList.isEmpty()) {
			regList.sort((p2, p1) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
			RegServiceDTO regDTO = regList.stream().findFirst().get();
			if (regDTO.getTheftDetails() == null) {
				throw new BadRequestException("application Based on PrNo is not THEFT INTIMATED" + input.getPrNo());
			}
			TheftVehicleDetailsVO theftVO = input.getTheftDetails();
			if (theftVO != null) {
				TheftVehicleDetailsDTO theftDTO = regDTO.getTheftDetails();
				theftDTO.setRevocationRemarks(theftVO.getRevocationRemarks());
				citizenObjects.getKey().setTheftDetails(theftDTO);
			}
			registratrionServicesApprovals.initiateApprovalProcessFlow(citizenObjects.getKey());

		}
	}

	private void doRcForFinance(RegServiceVO input, KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {

		RegServiceDTO regServiceDTO = citizenObjects.getKey();
		RegistrationDetailsDTO registrationDTO = citizenObjects.getValue();
		String officeCode = StringUtils.EMPTY;
		Optional<UserDTO> userOptional = userDAO.findByUserId(input.getFinancierUserId());
		if (!userOptional.isPresent()) {
			throw new BadRequestException("Financier details not found Please Sign Up");
		}
		officeCode = getMviOfficeCode(input, registrationDTO.getVehicleType());
		regServiceDTO.setMviOfficeCode(officeCode);
		Optional<OfficeDTO> officeDTOOpt = officeDAO.findByOfficeCode(officeCode);
		if (!officeDTOOpt.isPresent()) {
			throw new BadRequestException("Master Office Details Not Found wiht prNo" + input.getPrNo());

		}
		Optional<StateDTO> stateDTOOpt = stateDao
				.findByStateName(input.getFreshRc().getYardAddress().getState().getStateName());
		if (!stateDTOOpt.isPresent()) {
			throw new BadRequestException("state details not found with statename"
					+ input.getFreshRc().getYardAddress().getState().getStateName());
		}
		Optional<CountryDTO> countryDtoOpt = countryDao.findByCountryCode(NationalityEnum.IND.name());
		if (!countryDtoOpt.isPresent()) {
			throw new BadRequestException("country details not found with Countryname "
					+ input.getFreshRc().getYardAddress().getCountry().getCountryName());
		}
		regServiceMapper.freshRcforFinanceMapper(input, userOptional.get(), regServiceDTO, officeDTOOpt.get());
		regServiceDTO.getFreshRcdetails().getYardAddress().setState(stateDTOOpt.get());
		regServiceDTO.getFreshRcdetails().getYardAddress().setCountry(countryDtoOpt.get());

	}

	private void doNoc(RegServiceVO input, KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {
		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		RegistrationDetailsDTO registrationDTO = citizenObjects.getValue();
		if (input.getnOCDetails() != null && input.getnOCDetails().getPoliceClearanceDetails() != null
				&& input.getnOCDetails().getPoliceClearanceDetails().getIssuedDate().atStartOfDay()
						.isBefore(registrationDTO.getPrGeneratedDate())) {
			throw new BadRequestException("Issued Date should be greater than prGeneratedDate :" + input.getPrNo());
		}

		if (registrationDTO.getPrGeneratedDate() != null) {
			Period period = Period.between(registrationDTO.getRegistrationValidity().getPrGeneratedDate(),
					LocalDate.now());
			String vehicleAge = period.getYears() + " Years " + period.getMonths() + " Months";
			input.getnOCDetails().setPeriodOfStay(vehicleAge);
		}
		if (!validateDateFormat(input)) {
			throw new BadRequestException("Please provide valid Date Format as mentioned!");
		}

		if (null != input.getnOCDetails()) {
			regServiceDetails.setnOCDetails(nocDetailsMapper.convertVO(input.getnOCDetails()));
		}
	}

	private void doCancellationOfNoc(RegServiceVO input,
			KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {
		RegServiceDTO regServiceDetails = citizenObjects.getKey();

		RegistrationDetailsDTO registrationDetails = citizenObjects.getValue();
		if (registrationDetails.getNocDetails() != null) {
			if (input.getnOCDetails().getState() == null) {
				input.getnOCDetails().setState(registrationDetails.getNocDetails().getState());
			}
			if (input.getnOCDetails().getDistrict() == null) {
				input.getnOCDetails().setDistrict(registrationDetails.getNocDetails().getDistrict());
			}
			if (input.getnOCDetails().getRtaOffice() == null) {
				input.getnOCDetails().setRtaOffice(registrationDetails.getNocDetails().getRtaOffice());
			}
			if (input.getnOCDetails().getPoliceClearanceDetails() == null) {
				input.getnOCDetails().setPoliceClearanceDetails(policeClearanceCertificateMapper
						.convertEntity(registrationDetails.getNocDetails().getPoliceClearanceDetails()));
			}
		}

		if (null != input.getnOCDetails()) {
			regServiceDetails.setnOCDetails(nocDetailsMapper.convertVO(input.getnOCDetails()));
		}

	}

	private boolean validateDateFormat(RegServiceVO input) {
		LocalDate pucValidFrom = null;
		LocalDate pucValidTill = null;
		LocalDate pccIssueDate = null;
		if (input.getPucDetails() != null) {
			pucValidFrom = input.getPucDetails().getValidFrom();
			pucValidTill = input.getPucDetails().getValidTo();
			if (!pucValidFrom.isBefore(LocalDate.now())) {
				throw new BadRequestException("PUC ValidFrom Date must not exceed Today's Date");
			}
			if (!pucValidTill.isAfter(pucValidFrom)) {
				throw new BadRequestException("Puc ValidTill Date must be greater than ValidFrom Date");
			}
		}
		if (input.getnOCDetails().getPoliceClearanceDetails() != null) {
			pccIssueDate = input.getnOCDetails().getPoliceClearanceDetails().getIssuedDate();
			if (pccIssueDate.isAfter(LocalDate.now())) {
				throw new BadRequestException("Police Clearance Issued Date must not exceed Today's Date");
			}
		}

		return true;
	}

	private void doRcCancellation(RegServiceVO input, KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {
		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		if (null == input.getRcCancellation()) {
			throw new BadRequestException("Cancellation Details not coming prNo : [{}] " + input.getPrNo());
		}
		if (null != input.getRcCancellation()) {
			regServiceDetails.setRcCancellation(rcCancellationMapper.convertVO(input.getRcCancellation()));
		}
	}
	
	public void getAadharNoValidation(RegistrationDetailsDTO registrationDetailsDTO,RegServiceVO input) {
	
	if(registrationDetailsDTO.getApplicantDetails()== null) {
		throw new BadRequestException("Application Details Not Found "+registrationDetailsDTO.getPrNo());
	}
	if(registrationDetailsDTO.getApplicantDetails().getAadharNo() == null) {
		throw new BadRequestException("AadharNo Not Found " + registrationDetailsDTO.getPrNo());
	}
	if(!registrationDetailsDTO.getApplicantDetails().getAadharNo().equals(input.getAadhaarNo())) {
		throw new BadRequestException("AadharNo not matched/please give correct aadharNo "+input.getAadhaarNo());
	}
	}
	@Override
	public String getAadharNoUsingPrNo(RcValidationVO rcValidationVO) {
		Optional<RegistrationDetailsDTO> registrationOptional = Optional.empty();
	if (StringUtils.isNoneBlank(rcValidationVO.getPrNo())) {
		registrationOptional = registrationDetailDAO.findByPrNo(rcValidationVO.getPrNo());
	}
	if(!registrationOptional.isPresent()) {
		logger.error("No record found. [{}] ", rcValidationVO.getPrNo());
		throw new BadRequestException("No Record Found for this PrNo "+rcValidationVO.getPrNo());
	}
	if(registrationOptional.get().getApplicantDetails()== null) {
		throw new BadRequestException("Application Details Not Found for this PrNo "+rcValidationVO.getPrNo());
	}
	if(registrationOptional.get().getApplicantDetails().getAadharNo() == null) {
		throw new BadRequestException("AadharNo Not Found for this PrNo "+rcValidationVO.getPrNo());
	}
	return registrationOptional.get().getApplicantDetails().getAadharNo();
	}
	
	// TODO: Need to remove below method After Application Stable.
	private void checkServiceAvailabultyTOOffice(OfficeDTO officeDTO) {

		Optional<ServiceAvailabilityConfigDTO> serviceAvailabilityConfigOption = serviceAvailabilityConfigDAO
				.findByOfficeCodesIn(officeDTO.getOfficeCode());
		if (!serviceAvailabilityConfigOption.isPresent()) {
			throw new BadRequestException(
					"The services are currently not available for the office : " + officeDTO.getOfficeName());
		}

	}
	@Override
	public SearchVo searchWithOutAadharNoAndRc(RcValidationVO rcValidationVO, boolean requestFromSave)
			throws RcValidationException {
		Optional<RegistrationDetailsDTO> registrationOptional = Optional.empty();
		if (CollectionUtils.isEmpty(rcValidationVO.getServiceIds())) {
			throw new BadRequestException("Please select services.");
		}

		boolean isTemporaryPermit = false;
		if (null != rcValidationVO.getPermitClassVO()) {
			isTemporaryPermit = rcValidationVO.getPermitClassVO().getCode().equalsIgnoreCase("S");
		}

		if (StringUtils.isNoneBlank(rcValidationVO.getPrNo())) {
			registrationOptional = registrationDetailDAO.findByPrNo(rcValidationVO.getPrNo());
		}
		if (!registrationOptional.isPresent()
				&& !rcValidationVO.getServiceIds().contains(ServiceEnum.VARIATIONOFPERMIT.getId())) {
			logger.error("No record found. [{}] ", rcValidationVO.getPrNo());
			throw new BadRequestException("No record found.Pr no: " + rcValidationVO.getPrNo());
		}

		if (rcValidationVO.getServiceIds().contains(ServiceEnum.VARIATIONOFPERMIT.getId())
				&& !StringUtils.isEmpty(rcValidationVO.getPermitNo())) {
			registrationOptional = registrationDetailDAO.findByPermitDetailsPermitNo(rcValidationVO.getPermitNo());
			setPrNo(rcValidationVO);
		}

		if (!registrationOptional.isPresent()) {
			logger.error("No record found. [{}] ", rcValidationVO.getPrNo());
			throw new BadRequestException("No record found.Pr no: " + rcValidationVO.getPrNo());
		}
		// comment after completion of existing data entry applications
		if (registrationOptional.get().getServiceIds() != null) {
			if (registrationOptional.get().getServiceIds().stream()
					.anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))) {
				if (!(rcValidationVO.getServiceIds().contains(ServiceEnum.CHANGEOFADDRESS.getId())
						|| rcValidationVO.getServiceIds().contains(ServiceEnum.TRANSFEROFOWNERSHIP.getId()))) {
					if (registrationOptional.get().isRegVehicleWithPR()
							&& !registrationOptional.get().getApplicantType().equalsIgnoreCase("Paper RC")) {
						if (!(registrationOptional.get().getServiceIds().stream()
								.anyMatch(id -> id.equals(ServiceEnum.CHANGEOFADDRESS.getId()))
								|| registrationOptional.get().getServiceIds().stream()
										.anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId())))) {
							logger.error("Application is not eligible for this service. [{}] ",
									rcValidationVO.getPrNo());
							throw new BadRequestException(
									"Application is not eligible for this service Please apply change of address  or transfer of ownership");
						}
					}
				}
			}
		}

		// TODO: Need to remove below method After Application Stable.
		Boolean isRepresentative = Boolean.FALSE;
		Boolean skipAadharCheck = Boolean.FALSE;
		checkServiceAvailabultyTOOffice(registrationOptional.get().getOfficeDetails()); /////////
		if (!(rcValidationVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWFC.getId()))
				|| (rcValidationVO.getServiceIds().stream()
						.anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId()))))) {
			skipAadharCheck = Boolean.TRUE;

		}
		if (rcValidationVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.TAXATION.getId()))) {

			if (skipAadharValidationForTax()) {
				skipAadharCheck = Boolean.FALSE;
			}
		}
		//not working for  NEWFC and RCFORFINANCE and TAXATION
		if (skipAadharCheck) {
			if (rcValidationVO.getPermitClassVO() == null) {
				if (!(rcValidationVO.getOwnerType() != null && rcValidationVO.getOwnerType().equals(OwnerType.BUYER))) {
					if (!(rcValidationVO.getOwnerType() != null && rcValidationVO.getTransferType() != null
							&& rcValidationVO.getTransferType().equals(TransferType.DEATH))) {
						if (registrationOptional.get().getApplicantDetails().getAadharNo() == null) {
							logger.error("aadhar number not found");
							throw new BadRequestException(
									"aadhar number not found");
						}
						if (!registrationOptional.get().getApplicantDetails().getAadharNo()
								.equalsIgnoreCase(rcValidationVO.getAadharNo())) {
							logger.error("Please Enter correct aadhar number...");
							throw new BadRequestException("Please Enter correct aadhar number...");
						}
						if (!registrationOptional.get().getApplicantDetails().getAadharNo()
								.equalsIgnoreCase(rcValidationVO.getAadharNo())
								&& !rcValidationVO.getServiceIds().stream()
										.anyMatch(id -> (id.equals(ServiceEnum.TRANSFEROFPERMIT.getId())
												|| id.equals(ServiceEnum.TRANSFEROFRECOMMENDATIONLETTER.getId())))) {
							RepresentativeDTO represDto = validateRepresentative(rcValidationVO.getAadharNo());
							if (represDto == null) {
								logger.error("Please give correct aadhar number...");
								throw new BadRequestException("Please give correct aadhar number...");
							}
							if (!represDto.getApplicationIds()
									.contains(registrationOptional.get().getApplicationNo())) {
								logger.error("Please give correct aadhar number...");
								throw new BadRequestException("Please give correct aadhar number...");
							}
							isRepresentative = Boolean.TRUE;
						}
					}
				}
			}
		}

		if (RegCombinationsModuleEnum.REG_PERMITS.getModuleCode().equalsIgnoreCase(rcValidationVO.getModule())
				|| RegCombinationsModuleEnum.RECOMMENDATION_LETTER.getModuleCode()
						.equalsIgnoreCase(rcValidationVO.getModule())) {
			permitValidationsService.doPermitValidations(rcValidationVO, registrationOptional.get());
		}

		if (rcValidationVO.getServiceIds().stream()
				.anyMatch(id -> id.equals(ServiceEnum.HIREPURCHASETERMINATION.getId()))) {
			if (null == registrationOptional.get().getFinanceDetails()) {
				logger.error("Application is not eligible for this service. [{}] ", rcValidationVO.getPrNo());
				throw new BadRequestException("FinanceDetails Not Found for Pr no: " + rcValidationVO.getPrNo());
			}
		}

		if (rcValidationVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.HPA.getId()))
				&& !rcValidationVO.getServiceIds().stream()
						.anyMatch(id -> id.equals(ServiceEnum.HIREPURCHASETERMINATION.getId()))) {
			Boolean flag = true;
			if (registrationOptional.get().getFinanceDetails() != null) {
				if (rcValidationVO.getOwnerType() != null && rcValidationVO.getOwnerType().equals(OwnerType.BUYER)) {
					flag = false;
				}
				if (flag) {
					logger.error("Finance is Already Available Please apply HPT for prNo: " + rcValidationVO.getPrNo());
					throw new BadRequestException(
							"Finance is Already Available Please apply HPT for prNo: " + rcValidationVO.getPrNo());
				}
			}
		}

		Boolean aadhaarValidationRequired = true;
		if (registrationOptional.get().isDataInsertedByDataEntry() || isTemporaryPermit
				|| rcValidationVO.getIsMobile().equals(true)) {
			boolean isOtherStateData = true;
			if (rcValidationVO.getIsMobile().equals(true) || isTemporaryPermit) {
				aadhaarValidationRequired = false;
			}

			// TODO:Remove below code of reassignment validation
			if (rcValidationVO.getServiceIds().stream()
					.anyMatch(id -> id.equals(ServiceEnum.REASSIGNMENT.getId()) && isOtherStateData)) {
				List<String> errors = new ArrayList<>();
				validationForReassignment(rcValidationVO, errors);
			}
		}
		skipAadharCheck = Boolean.FALSE;
		if (rcValidationVO.getOwnerType() == null
		/*
		 * && !registrationOptional.get().getApplicantDetails().getIsAadhaarValidated()
		 */) {
			skipAadharCheck = Boolean.TRUE;
		}
		if (rcValidationVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.TAXATION.getId()))) {

			if (skipAadharValidationForTax()) {
				skipAadharCheck = Boolean.FALSE;
			}
		}

		if (rcValidationVO.getServiceIds() != null
				&& rcValidationVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId()))
				&& skipAadharCheck) {

			skipAadharCheck = Boolean.FALSE;

		}
		/*
		 * if (skipAadharCheck) { logger.
		 * error("Please select aadhar seeding service to Seed your aadhar number");
		 * throw new
		 * BadRequestException("Please select aadhar seeding service to Seed your aadhar number"
		 * ); }
		 */
		if (rcValidationVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId()))
				&& registrationOptional.get().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.EIBT.getCovCode())
				&& rcValidationVO.getOwnerType() != null && rcValidationVO.getOwnerType().equals(OwnerType.BUYER)
				&& registrationOptional.get().getOwnerType() != null
				&& !rcValidationVO.getOwnerShipType().equals(OwnerTypeEnum.Organization)) {
			logger.error(
					"For EIB vehicle, do not do transfer of owner other than organization.Do vehicle alteration to omini bus or select owner type Organization");
			throw new BadRequestException(
					"For EIB vehicle, do not do transfer of owner other than organization.Do vehicle alteration to omini bus or select owner type Organization");
		}
		Set<String> validations = new HashSet<String>();
		if (null != rcValidationVO.getTransferType() && TransferType.DEATH.equals(rcValidationVO.getTransferType())) {
			if ((registrationOptional.get().getApplicantDetails().getAadharNo() != null
					&& registrationOptional.get().getApplicantDetails().getAadharNo()
							.equalsIgnoreCase(rcValidationVO.getAadharNo()))) {
				//rcValidationVO.getAadhaarDetailsRequestVO().getUid_num()
				logger.error("Aadhaar number entered is same as applicant");
				throw new BadRequestException("Aadhaar number entered is same as applicant");
			}
			/*
			 * if(rcValidationVO.getServiceIds().contains(ServiceEnum.RENEWAL. getId()) &&
			 * rcValidationVO.getServiceIds().contains(ServiceEnum.
			 * ALTERATIONOFVEHICLE.getId ())){ throw new
			 * BadRequestException("RENEWAL + TOW + ALTERATION OF VEHICLE Combinations  " );
			 * }
			 */
			if (null != rcValidationVO.getChassisNo() && !rcValidationVO.getChassisNo()
					.equalsIgnoreCase((registrationOptional.get().getVahanDetails().getChassisNumber()))) {
				logger.error("please enter valid Chassis Number");
				throw new BadRequestException("please enter valid Chassis Number");
			}
		}
		Boolean isOtherState = Boolean.TRUE;
		if (registrationOptional.get().isDataInsertedByDataEntry() && registrationOptional.get().getServiceIds() != null
				&& !(registrationOptional.get().getServiceIds().stream()
						.anyMatch(id -> id.equals(ServiceEnum.CHANGEOFADDRESS.getId()))
						|| registrationOptional.get().getServiceIds().stream()
								.anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId())))) {
			isOtherState = Boolean.FALSE;
		}

		if (isOtherState && rcValidationVO.getOwnerType() != null
				&& !rcValidationVO.getOwnerType().equals(OwnerType.SELLER)) {
			rcValidationVO.setIsToSlotBookRequired(false);
			if (registrationOptional.get().getApplicantDetails().getIsAadhaarValidated()|| !registrationOptional.get().getApplicantDetails().getIsAadhaarValidated()) {
				
				if(!registrationOptional.get().getApplicantDetails().getAadharNo().equals(rcValidationVO.getAadharNo())) {
					logger.error("Please give correct aadhar number...");
					throw new BadRequestException("Please give correct aadhar number...");
				}
				
				if (null != rcValidationVO.getTransferType()
						&& TransferType.SALE.equals(rcValidationVO.getTransferType())) {
					if (!(registrationOptional.get().getApplicantDetails().getAadharNo() != null
							&& registrationOptional.get().getApplicantDetails().getAadharNo()
									.equalsIgnoreCase(rcValidationVO.getAadharNo()))) {
						//rcValidationVO.getAadhaarDetailsRequestVO().getUid_num()
						// for other services
						if (!(rcValidationVO.getServiceIds().stream()
								.anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId()))
								&& rcValidationVO.getOwnerType() != null
								&& rcValidationVO.getOwnerType().equals(OwnerType.BUYER))) {
							logger.error("Please give correct aadhar number...");
							throw new BadRequestException("Please give correct aadhar number...");
						}

					}

					if ((registrationOptional.get().getApplicantDetails().getAadharNo() != null
							&& registrationOptional.get().getApplicantDetails().getAadharNo()
									.equalsIgnoreCase(rcValidationVO.getAadharNo()))) {
						//rcValidationVO.getAadhaarDetailsRequestVO().getUid_num()
						if (rcValidationVO.getServiceIds().stream()
								.anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId()))
								&& rcValidationVO.getOwnerType() != null
								&& rcValidationVO.getOwnerType().equals(OwnerType.BUYER)
								&& registrationOptional.get().getOwnerType() != null && rcValidationVO
										.getOwnerShipType().equals(registrationOptional.get().getOwnerType())) {
							logger.error("Buyer and Seller should not be the same person");
							throw new BadRequestException("Buyer and Seller should not be the same person");
						}

					}

				}
			} else {
				logger.error("Please select aadhar seeding service to Seed your aadhar number");
				throw new BadRequestException("Please select aadhar seeding service to Seed your aadhar number");
			}
		}
		// validations based on services..

		List<CombinationServicesDTO> listOfValidations = combinationServicesDAO
				.findByServiceIdIn(rcValidationVO.getServiceIds());
		if ((!listOfValidations.isEmpty() && null != listOfValidations.get(0).getCombinationServices()
				&& !listOfValidations.get(0).getCombinationServices().isEmpty())
				|| (rcValidationVO.getServiceIds().stream()
						.anyMatch(val -> ServiceEnum.getPermitRelatedServiceIds().contains(val)))) {
			listOfValidations.parallelStream().forEach(

					data -> {
						validations.addAll(data.getValidation());
						validations.addAll(data.getTransportValidation());
					});

			List<String> errors = new ArrayList<>();

			for (Iterator<String> i = validations.iterator(); i.hasNext();) {
				String item = i.next();
				if (rcValidationVO.getServiceIds().contains(ServiceEnum.RENEWAL.getId())) {
					if (rcValidationVO.getServiceIds().contains(ServiceEnum.TRANSFEROFOWNERSHIP.getId())
							&& rcValidationVO.getServiceIds().contains(ServiceEnum.RENEWAL.getId())
							&& rcValidationVO.getTransferType() != null
							&& rcValidationVO.getTransferType().equals(TransferType.SALE)) {

						logger.error("TOW + RENEWAL only allowed for Death case");
						throw new BadRequestException("TOW + RENEWAL only allowed for Death case");
					}
					if (item.equalsIgnoreCase(ServicesValidations.PRVALIDITY.getCode())) {
						i.remove();
					}

				}
			}
			RegistrationDetailsDTO registrationDTO = registrationOptional.get();
			validations.stream().forEach(data -> {
				try {
					validations(registrationDTO, errors, data, rcValidationVO);
				} catch (Exception e) {
					throw new BadRequestException(e.getMessage());
				}
			});

			if (!errors.isEmpty()) {
				logger.error(" Validation Failed  : [{}]", errors);
				try {
					logger.error("[{}]", errors);
					throw new RcValidationException(errors);
				} catch (Exception e) {
					logger.error("[{}]", errors, e);
					throw new RcValidationException(errors, e);
				}
			}
		}
		if (rcValidationVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWFC.getId()))
				|| rcValidationVO.getServiceIds().stream()
						.anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))
				|| rcValidationVO.getServiceIds().stream()
						.anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId()))) {
			List<String> errors = new ArrayList<>();
			validations.addAll(listOfValidations.get(0).getValidation());
			validations.addAll(listOfValidations.get(0).getTransportValidation());
			RegistrationDetailsDTO registrationDTO = registrationOptional.get();
			validations.stream().forEach(data -> {
				try {
					validations(registrationDTO, errors, data, rcValidationVO);
				} catch (Exception e) {
					throw new BadRequestException(e.getMessage());
				}

			});
			if (!errors.isEmpty()) {
				logger.error(" Validation Failed  : [{}]", errors);
				try {
					throw new RcValidationException(errors);
				} catch (Exception e) {
					throw new RcValidationException(errors, e);
				}
			}
			aadhaarValidationRequired = false;
			/*
			 * if (rcValidationVO.getServiceIds().stream().anyMatch(id ->
			 * id.equals(ServiceEnum.VEHICLESTOPPAGE.getId())) ||
			 * rcValidationVO.getServiceIds().stream() .anyMatch(id ->
			 * id.equals(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId()))) {
			 * aadhaarValidationRequired = true; }
			 */
			rcValidationVO.setAadharNo(registrationOptional.get().getApplicantDetails().getAadharNo());
		}
		SearchVo searchVo = new SearchVo();
		if (!requestFromSave) {
			AadharDetailsResponseVO aadharResponse = null;
			AadhaarSourceDTO aadhaarSourceDTO = setAadhaarSourceDetails(rcValidationVO);
				if (aadhaarValidationRequired) {
					//aadharResponse = getAadharResponse(rcValidationVO.getAadhaarDetailsRequestVO(), aadhaarSourceDTO);
					searchVo.setIsRepresentative(isRepresentative);
					if (rcValidationVO.getOwnerType() != null && rcValidationVO.getOwnerType().equals(OwnerType.BUYER)
							|| null != rcValidationVO.getTransferType()
									&& rcValidationVO.getTransferType().equals(TransferType.DEATH)) {
						aadharResponse.setUid_num(rcValidationVO.getAadharNo());
						rcValidationVO.setBuyerAadhaarResponse(aadharResponse);
					}
					if ((registrationOptional.get().getApplicantDetails().getPresentAddress().getMandal() == null
							|| registrationOptional.get().getApplicantDetails().getPresentAddress()
									.getDistrict() == null)) {
						searchVo.setIsMandalNotExist(Boolean.TRUE);
						Integer distId = getDistrictId(registrationOptional.get().getOfficeDetails().getOfficeCode());
						searchVo.setDistrictId(distId);
					}
				
			}
			if (rcValidationVO.getServiceIds().contains(ServiceEnum.CHANGEOFADDRESS.getId())
					&& rcValidationVO.getIsMobile().equals(Boolean.FALSE)) {
				registrationOptional.get().getApplicantDetails()
						.setAadharResponse(null);//aadhaarDetailsResponseMapper.convertVO(aadharResponse)
			}
			searchVo.setTrasactionType("WithOutAadhar");
			return getSearchResult(rcValidationVO, registrationOptional.get(), searchVo);
		}
		return null;
	}
	
	@Override
	public SearchVo searchWithAadharNoAndRc(RcValidationVO rcValidationVO, boolean requestFromSave)
			throws RcValidationException {
		Optional<RegistrationDetailsDTO> registrationOptional = Optional.empty();
		if (CollectionUtils.isEmpty(rcValidationVO.getServiceIds())) {
			throw new BadRequestException("Please select services.");
		}

		boolean isTemporaryPermit = false;
		if (null != rcValidationVO.getPermitClassVO()) {
			isTemporaryPermit = rcValidationVO.getPermitClassVO().getCode().equalsIgnoreCase("S");
		}

		if (StringUtils.isNoneBlank(rcValidationVO.getPrNo())) {
			registrationOptional = registrationDetailDAO.findByPrNo(rcValidationVO.getPrNo());
		}
		if (!registrationOptional.isPresent()
				&& !rcValidationVO.getServiceIds().contains(ServiceEnum.VARIATIONOFPERMIT.getId())) {
			logger.error("No record found. [{}] ", rcValidationVO.getPrNo());
			throw new BadRequestException("No record found.Pr no: " + rcValidationVO.getPrNo());
		}

		if (rcValidationVO.getServiceIds().contains(ServiceEnum.VARIATIONOFPERMIT.getId())
				&& !StringUtils.isEmpty(rcValidationVO.getPermitNo())) {
			registrationOptional = registrationDetailDAO.findByPermitDetailsPermitNo(rcValidationVO.getPermitNo());
			setPrNo(rcValidationVO);
		}

		if (!registrationOptional.isPresent()) {
			logger.error("No record found. [{}] ", rcValidationVO.getPrNo());
			throw new BadRequestException("No record found.Pr no: " + rcValidationVO.getPrNo());
		}
		// comment after completion of existing data entry applications
		if (registrationOptional.get().getServiceIds() != null) {
			if (registrationOptional.get().getServiceIds().stream()
					.anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))) {
				if (!(rcValidationVO.getServiceIds().contains(ServiceEnum.CHANGEOFADDRESS.getId())
						|| rcValidationVO.getServiceIds().contains(ServiceEnum.TRANSFEROFOWNERSHIP.getId()))) {
					if (registrationOptional.get().isRegVehicleWithPR()
							&& !registrationOptional.get().getApplicantType().equalsIgnoreCase("Paper RC")) {
						if (!(registrationOptional.get().getServiceIds().stream()
								.anyMatch(id -> id.equals(ServiceEnum.CHANGEOFADDRESS.getId()))
								|| registrationOptional.get().getServiceIds().stream()
										.anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId())))) {
							logger.error("Application is not eligible for this service. [{}] ",
									rcValidationVO.getPrNo());
							throw new BadRequestException(
									"Application is not eligible for this service Please apply change of address  or transfer of ownership");
						}
					}
				}
			}
		}

		// TODO: Need to remove below method After Application Stable.
		Boolean isRepresentative = Boolean.FALSE;
		Boolean skipAadharCheck = Boolean.FALSE;
		checkServiceAvailabultyTOOffice(registrationOptional.get().getOfficeDetails());
		if (!(rcValidationVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWFC.getId()))
				|| (rcValidationVO.getServiceIds().stream()
						.anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId()))))) {
			skipAadharCheck = Boolean.TRUE;

		}
		if (rcValidationVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.TAXATION.getId()))) {

			if (skipAadharValidationForTax()) {
				skipAadharCheck = Boolean.FALSE;
			}
		}

		if (skipAadharCheck) {
			if (rcValidationVO.getPermitClassVO() == null) {
				if (!(rcValidationVO.getOwnerType() != null && rcValidationVO.getOwnerType().equals(OwnerType.BUYER))) {
					if (!(rcValidationVO.getOwnerType() != null && rcValidationVO.getTransferType() != null
							&& rcValidationVO.getTransferType().equals(TransferType.DEATH))) {
						if (!registrationOptional.get().getApplicantDetails().getIsAadhaarValidated()
								|| registrationOptional.get().getApplicantDetails().getAadharNo() == null) {
							logger.error("Please select aadhar seeding service to Seed your aadhar number");
							throw new BadRequestException(
									"Please select aadhar seeding service to Seed your aadhar number");
						}
						if (!registrationOptional.get().getApplicantDetails().getAadharNo()
								.equalsIgnoreCase(rcValidationVO.getAadharNo())
								&& !rcValidationVO.getServiceIds().stream()
										.anyMatch(id -> (id.equals(ServiceEnum.TRANSFEROFPERMIT.getId())
												|| id.equals(ServiceEnum.TRANSFEROFRECOMMENDATIONLETTER.getId())))) {
							RepresentativeDTO represDto = validateRepresentative(rcValidationVO.getAadharNo());
							if (represDto == null) {
								logger.error("Please give correct aadhar number...");
								throw new BadRequestException("Please give correct aadhar number...");
							}
							if (!represDto.getApplicationIds()
									.contains(registrationOptional.get().getApplicationNo())) {
								logger.error("Please give correct aadhar number...");
								throw new BadRequestException("Please give correct aadhar number...");
							}
							isRepresentative = Boolean.TRUE;
						}
					}
				}
			}
		}

		if (RegCombinationsModuleEnum.REG_PERMITS.getModuleCode().equalsIgnoreCase(rcValidationVO.getModule())
				|| RegCombinationsModuleEnum.RECOMMENDATION_LETTER.getModuleCode()
						.equalsIgnoreCase(rcValidationVO.getModule())) {
			permitValidationsService.doPermitValidations(rcValidationVO, registrationOptional.get());
		}

		if (rcValidationVO.getServiceIds().stream()
				.anyMatch(id -> id.equals(ServiceEnum.HIREPURCHASETERMINATION.getId()))) {
			if (null == registrationOptional.get().getFinanceDetails()) {
				logger.error("Application is not eligible for this service. [{}] ", rcValidationVO.getPrNo());
				throw new BadRequestException("FinanceDetails Not Found for Pr no: " + rcValidationVO.getPrNo());
			}
		}

		if (rcValidationVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.HPA.getId()))
				&& !rcValidationVO.getServiceIds().stream()
						.anyMatch(id -> id.equals(ServiceEnum.HIREPURCHASETERMINATION.getId()))) {
			Boolean flag = true;
			if (registrationOptional.get().getFinanceDetails() != null) {
				if (rcValidationVO.getOwnerType() != null && rcValidationVO.getOwnerType().equals(OwnerType.BUYER)) {
					flag = false;
				}
				if (flag) {
					logger.error("Finance is Already Available Please apply HPT for prNo: " + rcValidationVO.getPrNo());
					throw new BadRequestException(
							"Finance is Already Available Please apply HPT for prNo: " + rcValidationVO.getPrNo());
				}
			}
		}

		Boolean aadhaarValidationRequired = true;
		if (registrationOptional.get().isDataInsertedByDataEntry() || isTemporaryPermit
				|| rcValidationVO.getIsMobile().equals(true)) {
			boolean isOtherStateData = true;
			if (/*
				 * rcValidationVO.getOwnerType() != null &&
				 * rcValidationVO.getOwnerType().equals(OwnerType.SELLER) ||
				 */ rcValidationVO.getIsMobile().equals(true) || isTemporaryPermit) {
				aadhaarValidationRequired = false;
			}

			// TODO:Remove below code of reassignment validation
			if (rcValidationVO.getServiceIds().stream()
					.anyMatch(id -> id.equals(ServiceEnum.REASSIGNMENT.getId()) && isOtherStateData)) {
				List<String> errors = new ArrayList<>();
				validationForReassignment(rcValidationVO, errors);
			}
		}
		skipAadharCheck = Boolean.FALSE;
		if (rcValidationVO.getOwnerType() == null
				&& !registrationOptional.get().getApplicantDetails().getIsAadhaarValidated()) {
			skipAadharCheck = Boolean.TRUE;
		}
		if (rcValidationVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.TAXATION.getId()))) {

			if (skipAadharValidationForTax()) {
				skipAadharCheck = Boolean.FALSE;
			}
		}

		if (rcValidationVO.getServiceIds() != null
				&& rcValidationVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId()))
				&& skipAadharCheck) {

			skipAadharCheck = Boolean.FALSE;

		}
		if (skipAadharCheck) {
			logger.error("Please select aadhar seeding service to Seed your aadhar number");
			throw new BadRequestException("Please select aadhar seeding service to Seed your aadhar number");
		}
		if (rcValidationVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId()))
				&& registrationOptional.get().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.EIBT.getCovCode())
				&& rcValidationVO.getOwnerType() != null && rcValidationVO.getOwnerType().equals(OwnerType.BUYER)
				&& registrationOptional.get().getOwnerType() != null
				&& !rcValidationVO.getOwnerShipType().equals(OwnerTypeEnum.Organization)) {
			logger.error(
					"For EIB vehicle, do not do transfer of owner other than organization.Do vehicle alteration to omini bus or select owner type Organization");
			throw new BadRequestException(
					"For EIB vehicle, do not do transfer of owner other than organization.Do vehicle alteration to omini bus or select owner type Organization");
		}
		Set<String> validations = new HashSet<String>();
		if (null != rcValidationVO.getTransferType() && TransferType.DEATH.equals(rcValidationVO.getTransferType())) {
			if ((registrationOptional.get().getApplicantDetails().getAadharNo() != null
					&& registrationOptional.get().getApplicantDetails().getAadharNo()
							.equalsIgnoreCase(rcValidationVO.getAadhaarDetailsRequestVO().getUid_num()))) {
				logger.error("Aadhaar number entered is same as applicant");
				throw new BadRequestException("Aadhaar number entered is same as applicant");
			}
			/*
			 * if(rcValidationVO.getServiceIds().contains(ServiceEnum.RENEWAL. getId()) &&
			 * rcValidationVO.getServiceIds().contains(ServiceEnum.
			 * ALTERATIONOFVEHICLE.getId ())){ throw new
			 * BadRequestException("RENEWAL + TOW + ALTERATION OF VEHICLE Combinations  " );
			 * }
			 */
			if (null != rcValidationVO.getChassisNo() && !rcValidationVO.getChassisNo()
					.equalsIgnoreCase((registrationOptional.get().getVahanDetails().getChassisNumber()))) {
				logger.error("please enter valid Chassis Number");
				throw new BadRequestException("please enter valid Chassis Number");
			}
		}
		Boolean isOtherState = Boolean.TRUE;
		if (registrationOptional.get().isDataInsertedByDataEntry() && registrationOptional.get().getServiceIds() != null
				&& !(registrationOptional.get().getServiceIds().stream()
						.anyMatch(id -> id.equals(ServiceEnum.CHANGEOFADDRESS.getId()))
						|| registrationOptional.get().getServiceIds().stream()
								.anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId())))) {
			isOtherState = Boolean.FALSE;
		}

		if (isOtherState && rcValidationVO.getOwnerType() != null
				&& !rcValidationVO.getOwnerType().equals(OwnerType.SELLER)) {
			rcValidationVO.setIsToSlotBookRequired(false);
			if (registrationOptional.get().getApplicantDetails().getIsAadhaarValidated()) {

				if (null != rcValidationVO.getTransferType()
						&& TransferType.SALE.equals(rcValidationVO.getTransferType())) {
					if (!(registrationOptional.get().getApplicantDetails().getAadharNo() != null
							&& registrationOptional.get().getApplicantDetails().getAadharNo()
									.equalsIgnoreCase(rcValidationVO.getAadhaarDetailsRequestVO().getUid_num()))) {
						// for other services
						if (!(rcValidationVO.getServiceIds().stream()
								.anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId()))
								&& rcValidationVO.getOwnerType() != null
								&& rcValidationVO.getOwnerType().equals(OwnerType.BUYER))) {
							logger.error("Please give correct aadhar number...");
							throw new BadRequestException("Please give correct aadhar number...");
						}

					}

					if ((registrationOptional.get().getApplicantDetails().getAadharNo() != null
							&& registrationOptional.get().getApplicantDetails().getAadharNo()
									.equalsIgnoreCase(rcValidationVO.getAadhaarDetailsRequestVO().getUid_num()))) {
						if (rcValidationVO.getServiceIds().stream()
								.anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId()))
								&& rcValidationVO.getOwnerType() != null
								&& rcValidationVO.getOwnerType().equals(OwnerType.BUYER)
								&& registrationOptional.get().getOwnerType() != null && rcValidationVO
										.getOwnerShipType().equals(registrationOptional.get().getOwnerType())) {
							logger.error("Buyer and Seller should not be the same person");
							throw new BadRequestException("Buyer and Seller should not be the same person");
						}

					}

				}
			} else {
				logger.error("Please select aadhar seeding service to Seed your aadhar number");
				throw new BadRequestException("Please select aadhar seeding service to Seed your aadhar number");
			}
		}
		// validations based on services..

		List<CombinationServicesDTO> listOfValidations = combinationServicesDAO
				.findByServiceIdIn(rcValidationVO.getServiceIds());
		if ((!listOfValidations.isEmpty() && null != listOfValidations.get(0).getCombinationServices()
				&& !listOfValidations.get(0).getCombinationServices().isEmpty())
				|| (rcValidationVO.getServiceIds().stream()
						.anyMatch(val -> ServiceEnum.getPermitRelatedServiceIds().contains(val)))) {
			listOfValidations.parallelStream().forEach(

					data -> {
						validations.addAll(data.getValidation());
						validations.addAll(data.getTransportValidation());
					});

			List<String> errors = new ArrayList<>();

			for (Iterator<String> i = validations.iterator(); i.hasNext();) {
				String item = i.next();
				if (rcValidationVO.getServiceIds().contains(ServiceEnum.RENEWAL.getId())) {
					if (rcValidationVO.getServiceIds().contains(ServiceEnum.TRANSFEROFOWNERSHIP.getId())
							&& rcValidationVO.getServiceIds().contains(ServiceEnum.RENEWAL.getId())
							&& rcValidationVO.getTransferType() != null
							&& rcValidationVO.getTransferType().equals(TransferType.SALE)) {

						logger.error("TOW + RENEWAL only allowed for Death case");
						throw new BadRequestException("TOW + RENEWAL only allowed for Death case");
					}
					if (item.equalsIgnoreCase(ServicesValidations.PRVALIDITY.getCode())) {
						i.remove();
					}

				}
			}
			RegistrationDetailsDTO registrationDTO = registrationOptional.get();
			validations.stream().forEach(data -> {
				try {
					validations(registrationDTO, errors, data, rcValidationVO);
				} catch (Exception e) {
					throw new BadRequestException(e.getMessage());
				}
			});

			if (!errors.isEmpty()) {
				logger.error(" Validation Failed  : [{}]", errors);
				try {
					logger.error("[{}]", errors);
					throw new RcValidationException(errors);
				} catch (Exception e) {
					logger.error("[{}]", errors, e);
					throw new RcValidationException(errors, e);
				}
			}
		}
		if (rcValidationVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWFC.getId()))
				|| rcValidationVO.getServiceIds().stream()
						.anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))
				|| rcValidationVO.getServiceIds().stream()
						.anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId()))) {
			List<String> errors = new ArrayList<>();
			validations.addAll(listOfValidations.get(0).getValidation());
			validations.addAll(listOfValidations.get(0).getTransportValidation());
			RegistrationDetailsDTO registrationDTO = registrationOptional.get();
			validations.stream().forEach(data -> {
				try {
					validations(registrationDTO, errors, data, rcValidationVO);
				} catch (Exception e) {
					throw new BadRequestException(e.getMessage());
				}

			});
			if (!errors.isEmpty()) {
				logger.error(" Validation Failed  : [{}]", errors);
				try {
					throw new RcValidationException(errors);
				} catch (Exception e) {
					throw new RcValidationException(errors, e);
				}
			}
			aadhaarValidationRequired = false;
			/*
			 * if (rcValidationVO.getServiceIds().stream().anyMatch(id ->
			 * id.equals(ServiceEnum.VEHICLESTOPPAGE.getId())) ||
			 * rcValidationVO.getServiceIds().stream() .anyMatch(id ->
			 * id.equals(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId()))) {
			 * aadhaarValidationRequired = true; }
			 */
			rcValidationVO.setAadharNo(registrationOptional.get().getApplicantDetails().getAadharNo());
		}
		SearchVo searchVo = new SearchVo();
		if (!requestFromSave) {
			AadharDetailsResponseVO aadharResponse = null;
			AadhaarSourceDTO aadhaarSourceDTO = setAadhaarSourceDetails(rcValidationVO);
				if (aadhaarValidationRequired) {
					aadharResponse = getAadharResponse(rcValidationVO.getAadhaarDetailsRequestVO(), aadhaarSourceDTO);
					searchVo.setIsRepresentative(isRepresentative);
					if (rcValidationVO.getOwnerType() != null && rcValidationVO.getOwnerType().equals(OwnerType.BUYER)
							|| null != rcValidationVO.getTransferType()
									&& rcValidationVO.getTransferType().equals(TransferType.DEATH)) {
						rcValidationVO.setBuyerAadhaarResponse(aadharResponse);
					}
					if ((registrationOptional.get().getApplicantDetails().getPresentAddress().getMandal() == null
							|| registrationOptional.get().getApplicantDetails().getPresentAddress()
									.getDistrict() == null)) {
						searchVo.setIsMandalNotExist(Boolean.TRUE);
						Integer distId = getDistrictId(registrationOptional.get().getOfficeDetails().getOfficeCode());
						searchVo.setDistrictId(distId);
					}
				
			}
			if (/*
				 * rcValidationVO.getServiceIds().stream().anyMatch(id ->
				 * id.equals(ServiceEnum.VEHICLESTOPPAGE.getId())) ||
				 * rcValidationVO.getServiceIds().stream() .anyMatch(id ->
				 * id.equals(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId())) ||
				 */ rcValidationVO.getServiceIds().contains(ServiceEnum.CHANGEOFADDRESS.getId())
					&& rcValidationVO.getIsMobile().equals(Boolean.FALSE)) {
				registrationOptional.get().getApplicantDetails()
						.setAadharResponse(aadhaarDetailsResponseMapper.convertVO(aadharResponse));
			}
			return getSearchResult(rcValidationVO, registrationOptional.get(), searchVo);
		}
		return null;
	}

	@Override
	public AadhaarSourceDTO setAadhaarSourceDetails(RcValidationVO rcValidationVO) {
		AadhaarSourceDTO aadhaarSourceDTO = new AadhaarSourceDTO();
		aadhaarSourceDTO.setPrNo(rcValidationVO.getPrNo());
		aadhaarSourceDTO.setServiceIds(rcValidationVO.getServiceIds());
		aadhaarSourceDTO.setAadhaarNo(rcValidationVO.getAadharNo());
		return aadhaarSourceDTO;
	}

	private Integer getDistrictId(String officeCode) {
		Optional<OfficeDTO> officeOptional = officeDAO.findByOfficeCode(officeCode);
		if (!officeOptional.isPresent()) {
			logger.error("Office Details not found for officeCode " + officeCode);
			throw new BadRequestException("Office Details not found for officeCode " + officeCode);
		}
		return officeOptional.get().getDistrict();
	}

	// This method is not required because we realised in Every office
	// TODO: Need to remove below method After Application Stable.
	/*
	 * private void checkServiceAvailabultyTOOffice(OfficeDTO officeDTO) {
	 * 
	 * Optional<ServiceAvailabilityConfigDTO> serviceAvailabilityConfigOption =
	 * serviceAvailabilityConfigDAO .findByOfficeCodesIn(officeDTO.getOfficeCode());
	 * if (!serviceAvailabilityConfigOption.isPresent()) { throw new
	 * BadRequestException(
	 * "The services are currently not available for the office : " +
	 * officeDTO.getOfficeName()); }
	 * 
	 * }
	 */

	/**
	 * @param rcValidationVO
	 */

	private void validationForReassignment(RcValidationVO rcValidationVO, List<String> errors) {
		List<RegServiceDTO> listRegDetails = regServiceDAO.findByPrNoAndServiceTypeNotIn(rcValidationVO.getPrNo(),
				ServiceEnum.getServiceEnumById(ServiceEnum.TAXATION.getId()));
		if (!listRegDetails.isEmpty()) {
			List<StatusRegistration> listOfStatus = new ArrayList<>();
			listOfStatus.add(StatusRegistration.APPROVED);
			listRegDetails.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
			RegServiceDTO regDetails = listRegDetails.stream().findFirst().get();

			// listRegDetails.stream().forEach(regDetails -> {
			if (regDetails.getServiceIds().contains(ServiceEnum.ISSUEOFNOC.getId())) {
				/***
				 * Below condition only for non migration data, as of now we are restricting non
				 * migration data , we will change the below logic after noc data migrate from
				 * old data to our DB.
				 */
				final String stateName = "Andhra Pradesh";// Temp variable
				if (regDetails.getnOCDetails() != null && !stateName.equals(regDetails.getnOCDetails())) {
					logger.error("Please cancel NOC to avail this service. Pr No : [{}] ", rcValidationVO.getPrNo());
					errors.add("Please cancel NOC to avail this service.Pr no: " + rcValidationVO.getPrNo());
				}
			}

			/*
			 * if (!regDetails.getServiceIds().stream().anyMatch(id ->
			 * id.equals(ServiceEnum.CHANGEOFADDRESS.getId())) &&
			 * !regDetails.getServiceIds().stream() .anyMatch(id ->
			 * id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId()))) { logger.
			 * error("Before Re-Assigment, Have to apply COA/TO. Pr No : [{}] ",
			 * rcValidationVO.getPrNo());
			 * errors.add("Before Re-Assigment, Have to apply COA/TOW.Pr no: " +
			 * rcValidationVO.getPrNo()); }
			 */

			if ((regDetails.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.CHANGEOFADDRESS.getId()))
					|| regDetails.getServiceIds().stream()
							.anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId())))
					&& !listOfStatus.stream().anyMatch(status -> status.equals(regDetails.getApplicationStatus()))) {
				logger.error("Can Not process Request, Application is pending in RTA Office. [{}] ",
						rcValidationVO.getPrNo());
				errors.add("Can Not process Request, Application is pending in RTA Office.Pr no: "
						+ rcValidationVO.getPrNo());
			}
			// });
		}
	}

	@Override
	public SearchVo getSearchResult(RcValidationVO rcValidationVO, RegistrationDetailsDTO registrationDetailsDTO,
			SearchVo vo) {

		// SearchVo vo = new SearchVo();

		RegistrationDetailsVO registrationDetailsVO = registrationDetailsMapper.convertEntity(registrationDetailsDTO);

		if (registrationDetailsDTO.getPrGeneratedDate() != null) {
			if (registrationDetailsDTO.getRegistrationValidity().getPrGeneratedDate() != null) {
				Period period = Period.between(registrationDetailsDTO.getRegistrationValidity().getPrGeneratedDate(),
						LocalDate.now());
				String vehicleAge = period.getYears() + " Years " + period.getMonths() + " Months";
				registrationDetailsVO.setVehicleAge(vehicleAge);
			}

		}

		if (registrationDetailsVO.getFinanceDetails() != null) {
			FinanceDetailsVO financeVO = appendFinancerAddress(registrationDetailsVO.getFinanceDetails(),
					registrationDetailsDTO);
			registrationDetailsVO.setFinanceDetails(financeVO);

		}

		if (registrationDetailsDTO.getRegistrationValidity() != null
				&& registrationDetailsDTO.getRegistrationValidity().getPrGeneratedDate() != null
				&& registrationDetailsVO.getPrGeneratedDate() == null) {
			registrationDetailsVO.setPrGeneratedDate(
					registrationDetailsDTO.getRegistrationValidity().getPrGeneratedDate().atStartOfDay());
		} else if (registrationDetailsDTO.getRegistrationValidity().getPrGeneratedDate() == null) {
			if (registrationDetailsDTO.getRegistrationValidity().getRegistrationValidity() != null) {
				if (registrationDetailsDTO.getVehicleType().equals(CovCategory.T.getCode())) {
					registrationDetailsVO.setPrGeneratedDate(
							registrationDetailsDTO.getRegistrationValidity().getRegistrationValidity().minusYears(2));
				} else {
					registrationDetailsVO.setPrGeneratedDate(
							registrationDetailsDTO.getRegistrationValidity().getRegistrationValidity().minusYears(15));
				}
			}
		}
		if (registrationDetailsVO.getPucDetailsVO() == null) {
			PUCDetailsVO pucVo = new PUCDetailsVO();
			pucVo.setValidFrom(registrationDetailsVO.getPrGeneratedDate().toLocalDate());
			pucVo.setValidTo(registrationDetailsVO.getPrGeneratedDate().minusDays(1).plusYears(1).toLocalDate());
			registrationDetailsVO.setPucDetailsVO(pucVo);
		}
		Optional<MasterPayperiodDTO> masterPayPeridOptional = masterPayperiodDAO
				.findByCovcode(registrationDetailsDTO.getClassOfVehicle());
		if (masterPayPeridOptional.isPresent()) {
			List<TaxDetailsDTO> taxList = taxDetailsDAO
					.findFirst10ByApplicationNoOrderByCreatedDateDesc(registrationDetailsDTO.getApplicationNo());
			if (taxList.size() > 0) {
				this.updatePaidDateAsCreatedDate(taxList);
				if (!CollectionUtils.isEmpty(taxList)) {
					for (TaxDetailsDTO taxDetailsDTO : taxList) {
						if (taxDetailsDTO.getCreatedDate() == null) {
							throw new BadRequestException("Tax Dates Not Availble");
						}
					}
					taxList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
					Map<String, TaxComponentDTO> taxComponent = Collections.emptyMap();
					for (TaxDetailsDTO taxDetailsDTO : taxList) {
						List<Map<String, TaxComponentDTO>> taxComponentList = taxDetailsDTO.getTaxDetails().stream()
								.filter(taxDetails -> taxDetails.containsKey(TaxTypeEnum.LifeTax.getDesc())
										|| taxDetails.containsKey(TaxTypeEnum.HalfyearlyTax.getDesc())
										|| taxDetails.containsKey(TaxTypeEnum.QuarterlyTax.getDesc())
										|| taxDetails.containsKey(TaxTypeEnum.YearlyTax.getDesc()))
								.collect(Collectors.toList());

						if (!taxComponentList.isEmpty()) {
							taxComponent = taxComponentList.stream().findFirst().get();
							break;
						}
					}

					if (!taxComponent.isEmpty()) {
						String taxType = taxComponent.keySet().stream().findFirst().get();
						if (taxComponent.get(taxType).getValidityTo() == null) {
							Optional<MasterTaxExcemptionsDTO> optionalTaxExcemption = masterTaxExcemptionsDAO
									.findByKeyvalue(registrationDetailsDTO.getVahanDetails().getMakersModel());
							if (optionalTaxExcemption.isPresent()
									|| registrationDetailsVO.getOwnerType().equals(OwnerTypeEnum.Government)
									|| registrationDetailsVO.getOwnerType().equals(OwnerTypeEnum.POLICE)
									|| registrationDetailsDTO.getClassOfVehicle()
											.equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())
									|| (registrationDetailsDTO.getClassOfVehicle()
											.equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode())
											&& registrationDetailsDTO.getVahanDetails().getGvw() <= 3000)
									|| registrationDetailsDTO.getClassOfVehicle()
											.equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())
									|| registrationDetailsDTO.getClassOfVehicle()
											.equalsIgnoreCase(ClassOfVehicleEnum.TRTT.getCovCode())
									|| registrationDetailsDTO.getClassOfVehicle()
											.equalsIgnoreCase(ClassOfVehicleEnum.TRCN.getCovCode())) {
								registrationDetailsVO
										.setTaxvalidity(citizenTaxService.validity(TaxTypeEnum.QuarterlyTax.getDesc()));
							} else {
								throw new BadRequestException(
										"tax validity not found for RegistrationNo :" + rcValidationVO.getPrNo());
							}
						} else {
							registrationDetailsVO.setTaxvalidity(taxComponent.get(taxType).getValidityTo());
						}

						registrationDetailsVO.setTaxPaidDate(taxComponent.get(taxType).getPaidDate().toLocalDate());
						Pair<List<String>, Boolean> listTaxperiod = getPayTaxType(
								registrationDetailsDTO.getApplicationNo());

						if (listTaxperiod.getSecond()) {
							registrationDetailsVO.setIsToPayTax(Boolean.FALSE);
						} else {
							if (!citizenTaxService.checkTaxUpToDateOrNote(Boolean.FALSE, Boolean.FALSE, null,
									registrationDetailsDTO, null, ServiceCodeEnum.QLY_TAX.getCode(), Boolean.FALSE)) {
								registrationDetailsVO.setIsToPayTax(Boolean.FALSE);
							}
						}
						/*
						 * if (!taxType.equalsIgnoreCase(TaxTypeEnum.LifeTax. getDesc())) { if
						 * (taxComponent.get(taxType).getValidityTo().isBefore( LocalDate.now())) {
						 * registrationDetailsVO.setIsToPayTax(Boolean.TRUE); Pair<List<String>,
						 * Boolean> listTaxperiod = getPayTaxType(
						 * registrationDetailsDTO.getApplicationNo()); if (!listTaxperiod.getSecond()) {
						 * registrationDetailsVO.setListOfTaxTypes(listTaxperiod .getFirst()); } }
						 * 
						 * }
						 */
					}
				}
				taxList.clear();
			}
		} else {
			logger.info("master payperiod is not present for cov [{}]", registrationDetailsDTO.getClassOfVehicle());
			throw new BadRequestException(
					"master payperiod is not present for cov " + registrationDetailsDTO.getClassOfVehicle());
		}
		Boolean fcValidationRequired = Boolean.TRUE;
		if (rcValidationVO.getTransferType() != null && (rcValidationVO.getTransferType().equals(TransferType.DEATH)
				|| rcValidationVO.getTransferType().equals(TransferType.AUCTION))) {
			fcValidationRequired = Boolean.FALSE;
		}
		if (registrationDetailsDTO != null && registrationDetailsDTO.getVehicleType().equals(CovCategory.T.getCode())

				&& (!registrationDetailsDTO.getClassOfVehicle().equals("TRTT")) && fcValidationRequired) {
			List<FcDetailsDTO> fcDetailsOptional = fcDetailsDAO
					.findFirst5ByStatusIsTrueAndPrNoOrderByCreatedDateDesc(registrationDetailsDTO.getPrNo());
			if (!fcDetailsOptional.isEmpty()) {
				FcDetailsDTO fcDetailsDTO = fcDetailsOptional.stream().findFirst().get();
				registrationDetailsVO.setFcDetailsVO(fcDetailsMapper.convertEntity(fcDetailsDTO));
			} else {
				if (!ServiceEnum.fcValidationRequired(rcValidationVO.getServiceIds())) {
					LocalDate fcValidity = null;
					if (registrationDetailsDTO.getRegistrationValidity() != null) {
						if (registrationDetailsDTO.getRegistrationValidity().getFcValidity() == null) {
							List<FcDetailsDTO> fcDetailsList = fcDetailsDAO
									.findFirst5ByStatusIsTrueAndPrNoOrderByCreatedDateDesc(
											registrationDetailsDTO.getPrNo());
							if (fcDetailsList.isEmpty()) {
								throw new BadRequestException("No FC details found for prNo: "
										+ registrationDetailsDTO.getPrNo() + ". Please apply for new FC");
							}
							FcDetailsDTO fcDetailsDTO = fcDetailsList.stream().findFirst().get();
							if (fcDetailsDTO.getFcValidUpto() == null) {
								throw new BadRequestException(
										"Fc ValidUpto not found for prNo :" + registrationDetailsDTO.getPrNo());
							}
							fcValidity = fcDetailsDTO.getFcValidUpto();
							registrationDetailsDTO.getRegistrationValidity().setFcValidity(fcValidity);
						} else {
							fcValidity = registrationDetailsDTO.getRegistrationValidity().getFcValidity();
						}
					}
					if (fcValidity == null) {
						throw new BadRequestException(
								"fcDetails found not for applicationNo" + registrationDetailsDTO.getApplicationNo());
					}
					if (fcValidity.isBefore(LocalDate.now()))
						throw new BadRequestException("No FC details found for prNo: "
								+ registrationDetailsDTO.getPrNo() + ". Please apply for new FC");
				}
			}
		}
		Optional<PermitDetailsDTO> dto = permitDetailsDAO.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(
				rcValidationVO.getPrNo(), PermitType.PRIMARY.getPermitTypeCode(), PermitsEnum.ACTIVE.getDescription());
		Optional<PermitDetailsVO> permitDetailsVO = null;
		if (dto.isPresent()) {
			permitDetailsVO = permitDetailsMapper.convertEntity(dto);
			if (rcValidationVO.getServiceIds().stream().anyMatch(s -> (s.equals(ServiceEnum.RENEWALOFAUTHCARD.getId())))
					&& dto.get().getPermitType() != null && PermitsEnum.getAllIndiaPermitCode().stream()
							.anyMatch(v -> v.equalsIgnoreCase(dto.get().getPermitType().getPermitType()))) {
				permitDetailsVO.get().setIsMonthsInRenewal(Boolean.TRUE);
			}
			vo.setPermitDetailsVO(permitDetailsVO.get());
		}
		/*
		 * if (rcValidationVO.getServiceIds().stream().anyMatch(s ->
		 * (s.equals(ServiceEnum.RENEWALOFPERMIT.getId()) ||
		 * s.equals(ServiceEnum.EXTENSIONOFVALIDITY.getId())))) { permitDetailsVO =
		 * permitsService.findPermitDetailsByRcNoAndStatus(rcValidationVO.getPrNo());
		 * vo.setPermitDetailsVO(permitDetailsVO.get()); }
		 */
		if (rcValidationVO.getServiceIds().stream().anyMatch(s -> (s.equals(ServiceEnum.VARIATIONOFPERMIT.getId())))) {
			ApplicationSearchVO applicationSearchVO = new ApplicationSearchVO();
			if (!StringUtils.isEmpty(rcValidationVO.getPrNo())) {
				applicationSearchVO.setPrNo(rcValidationVO.getPrNo());
			} else if (!StringUtils.isEmpty(rcValidationVO.getPermitNo())) {
				applicationSearchVO.setPermitNo(rcValidationVO.getPermitNo());
			}
			Optional<PermitDetailsVO> permitDetailsOptional = permitsService
					.findByPrNoOrPermitNoStatusBased(applicationSearchVO);
			if (permitDetailsOptional.isPresent())
				vo.setPermitDetailsVO(permitDetailsOptional.get());
		}

		vo.setIsInsuranceExpired(regInsuranceValidity(registrationDetailsDTO));
		vo.setIsPucExpired(regPUCDetailsValidity(registrationDetailsDTO));

		if (rcValidationVO.getOwnerType() != null) {
			List<Integer> financeStatus = new ArrayList<>();
			if (OwnerType.BUYER.equals(rcValidationVO.getOwnerType()) && rcValidationVO.getIsToSlotBookRequired()) {
				isDataEntry(registrationDetailsVO, vo);
				// registrationDetailsVO.setTOWSlotRequried(rcValidationVO.getIsToSlotBookRequired());
			}

			if (TransferType.DEATH.equals(rcValidationVO.getTransferType())) {
				registrationDetailsVO.getApplicantDetails().setAadharResponse(rcValidationVO.getBuyerAadhaarResponse());
				if (registrationDetailsVO.getApplicantDetails() == null) {
					throw new BadRequestException("Applicant details are not found,So please contact help desk");
				}
				if (registrationDetailsVO.getApplicantDetails().getPresentAddress() == null
						|| registrationDetailsVO.getApplicantDetails().getPresentAddress().getDistrict() == null
						|| registrationDetailsVO.getApplicantDetails().getPresentAddress().getDistrict()
								.getDistrictId() == null) {
					updateMissedData(registrationDetailsVO);
				}
			}
			if (OwnerType.BUYER.equals(rcValidationVO.getOwnerType())
					&& !(TransferType.DEATH.equals(rcValidationVO.getTransferType())
							|| TransferType.AUCTION.equals(rcValidationVO.getTransferType()))) {
				vo.setIsInsuranceExpired(!getInsuranceValidity(rcValidationVO.getPrNo()));
				vo.setIsPucExpired(!getPUCValidity(rcValidationVO.getPrNo()));
				// Null Pointer handled in the getLatestRecord() method
				RegServiceDTO regServiceDto = getLatestRecord(rcValidationVO.PrNo);
				registrationDetailsVO.getApplicantDetails().setAadharResponse(rcValidationVO.getBuyerAadhaarResponse());

				if (regServiceDto.getBuyerDetails() != null
						&& regServiceDto.getBuyerDetails().getSellerPermitStatus() != null
						&& regServiceDto.getBuyerDetails().getSellerPermitStatus()
								.equals(TransferType.permitTranfer.PERMITTRANSFER)) {

					vo.setSellerPermitStatus(true);
				} else {
					vo.setPermitDetailsVO(null);
				}

				if (regServiceDto.getBuyerDetails() != null
						&& regServiceDto.getBuyerDetails().getSellerRecommedationLetterStatus() != null
						&& regServiceDto.getBuyerDetails().getSellerRecommedationLetterStatus()
								.equals(TransferType.permitTranfer.RECOMMENDATIONLETTERTRANSFER)) {

					vo.setSellerRecommedationLetterStatus(true);
				}

				// financeStatus.add(FinanceTowEnum.NEWFINANCE.getId());
				if (regServiceDto.getBuyerDetails() != null
						&& regServiceDto.getBuyerDetails().getSellerFinanceStatus() != null
						&& regServiceDto.getBuyerDetails().getSellerFinanceStatus()
								.equalsIgnoreCase(FinanceTowEnum.CONTINUEWITHFINANCE.toString())) {
					financeStatus.add(FinanceTowEnum.CONTINUEWITHFINANCE.getId());
					financeStatus.add(FinanceTowEnum.OFFLINE.getId());
					if (regServiceDto.getBuyerDetails().getSellerFinanceType()
							.equals(FinanceTowEnum.ONLINE.toString())) {
						financeStatus.remove(new Integer(FinanceTowEnum.OFFLINE.getId()));
						financeStatus.add(FinanceTowEnum.ONLINE.getId());
					}
					registrationDetailsVO.setFinance(Boolean.TRUE);
					registrationDetailsVO.setToFinanceStatus(financeStatus);
				}

			}

			if (OwnerType.SELLER.equals(rcValidationVO.getOwnerType())) {
				vo.setIsTaxPending(checkIsTaxPending(registrationDetailsDTO));
				/*
				 * null != registrationDetailsDTO.getIsFinancier() &&
				 * registrationDetailsDTO.getIsFinancier()&&
				 */
				if (null != registrationDetailsDTO.getFinanceDetails()) {
					if (rcValidationVO.getTransferType().equals(TransferType.DEATH) && !rcValidationVO.getServiceIds()
							.stream().anyMatch(id -> (id.equals(ServiceEnum.HIREPURCHASETERMINATION.getId())))) {

						throw new BadRequestException(
								"Vehicle is under finance so please apply HPT / HPT+TOW Combination");

					}
					if (registrationDetailsDTO.getFinanceDetails().getUserId() != null) {
						financeStatus.add(FinanceTowEnum.OFFLINE.getId());
						MasterUsersDTO userDTO = masterUsersDAO
								.findByUserId(registrationDetailsDTO.getFinanceDetails().getUserId());
						if (userDTO != null) {
							financeStatus.remove(new Integer(FinanceTowEnum.OFFLINE.getId()));
							financeStatus.add(FinanceTowEnum.ONLINE.getId());
						}
					} else {
						financeStatus.add(FinanceTowEnum.OFFLINE.getId());
					}
					financeStatus.add(FinanceTowEnum.CONTINUEWITHFINANCE.getId());
					financeStatus.add(FinanceTowEnum.TERMINATE.getId());
					registrationDetailsVO.setFinance(Boolean.TRUE);
					registrationDetailsVO.setToFinanceStatus(financeStatus);
				}
			}

			if (registrationDetailsDTO.getVehicleType().equalsIgnoreCase("T")
					/*
					 * && TransferType.SALE.equals(rcValidationVO.getTransferType() )
					 */
					&& rcValidationVO.getOwnerType().equals(OwnerType.SELLER)) {
				this.getPermitStatus(vo, rcValidationVO.getPrNo());
				this.getRecommendationLetterDetails(vo, rcValidationVO.getPrNo());
			}
		}
		// Permit Validation in COA

		if (registrationDetailsDTO.getVehicleType().equalsIgnoreCase("T")
				&& rcValidationVO.getServiceIds().contains(ServiceEnum.CHANGEOFADDRESSOFRECOMMENDATIONLETTER.getId())
				|| rcValidationVO.getServiceIds().contains(ServiceEnum.TRANSFEROFRECOMMENDATIONLETTER.getId())
				|| rcValidationVO.getServiceIds().contains(ServiceEnum.CHANGEOFADDRESS.getId())) {
			this.getPermitStatus(vo, rcValidationVO.getPrNo());
			this.getRecommendationLetterDetails(vo, rcValidationVO.getPrNo());
		}

		// adding Theft Intimation Details while Revocation
		if (rcValidationVO.getServiceIds().contains(ServiceEnum.THEFTREVOCATION.getId())) {

			List<RegServiceDTO> regList = regServiceDAO.findByPrNoAndServiceTypeNotIn(rcValidationVO.getPrNo(),
					ServiceEnum.getServiceEnumById(ServiceEnum.TAXATION.getId()));

			if (!regList.isEmpty()) {
				regList.sort((p2, p1) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
				RegServiceDTO regDTO = regList.stream().findFirst().get();
				if (regDTO.getTheftDetails() != null) {
					TheftVehicleDetailsVO regVO = theftDetailsMapper.convertEntity(regDTO.getTheftDetails());
					registrationDetailsVO.setTheftDetails(regVO);
				}
			}

		}
		if (rcValidationVO.getServiceIds().contains(ServiceEnum.RCCANCELLATION.getId())) {
			List<String> errors = new ArrayList<>();
			if (registrationDetailsDTO.getIsFinancier() || registrationDetailsDTO.getFinanceDetails() != null) {
				throw new BadRequestException(
						"RC cannot be cancelled for a financed vehicle " + registrationDetailsDTO.getPrNo());
			}
			if (CollectionUtils.isNotEmpty(validationForRC(registrationDetailsDTO, errors))) {
				throw new BadRequestException(errors.toString());
			}
		}

		vo.setRegistrationDetails(registrationDetailsVO);

		return vo;
	}

	private void getRecommendationLetterDetails(SearchVo vo, String prNo) {

		this.getPermitStatus(vo, prNo);
		if (vo.getIsValidPermit()) {
			Optional<PermitDetailsDTO> permitDto = fetchRecommendationLetterDetails(prNo);
			if (permitDto.isPresent()
					&& permitDto.get().getPermitValidityDetails().getPermitValidTo().isAfter(LocalDate.now())) {
				vo.setIsRecommendationLetter(true);
				vo.setRecoomendationLetterCode(permitDto.get().getPermitType().getPermitType());
			}
		}

	}

	private Optional<PermitDetailsDTO> fetchRecommendationLetterDetails(String prNo) {

		return permitDetailsDAO.findByPrNoAndPermitStatusAndPermitTypeTypeofPermitOrderByCreatedDateDesc(prNo,
				PermitsEnum.ACTIVE.getDescription(), PermitType.COUNTER_SIGNATURE.getPermitTypeCode());
	}

	private boolean checkIsTaxPending(RegistrationDetailsDTO registrationDetailsDTO) {
		boolean status = Boolean.FALSE;
		if (registrationDetailsDTO.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())
				|| (registrationDetailsDTO.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode())
						&& registrationDetailsDTO.getVahanDetails().getGvw() <= 3000)
				|| registrationDetailsDTO.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())
				|| registrationDetailsDTO.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TRTT.getCovCode())
				|| registrationDetailsDTO.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TRCN.getCovCode())) {
			return Boolean.FALSE;
		}
		Optional<MasterPayperiodDTO> Payperiod = masterPayperiodDAO
				.findByCovcode(registrationDetailsDTO.getClassOfVehicle());
		if (!Payperiod.isPresent()) {
			// throw error message
			logger.error("No record found in master_payperiod for:[{}] " + registrationDetailsDTO.getApplicationNo());
			throw new BadRequestException(
					"No record found in master_payperiod for: " + registrationDetailsDTO.getApplicationNo());

		}
		Pair<List<String>, Boolean> listTaxperiod = getPayTaxType(registrationDetailsDTO.getApplicationNo());

		if (listTaxperiod.getSecond()) {
			status = Boolean.FALSE;
		} else {
			if (!citizenTaxService.checkTaxUpToDateOrNote(Boolean.FALSE, Boolean.FALSE, null, registrationDetailsDTO,
					null, ServiceCodeEnum.QLY_TAX.getCode(), Boolean.FALSE)) {
				status = Boolean.TRUE;
			}
		}
		// Green tax
		TaxHelper greenTaxAndValidity = citizenTaxService.greenTaxCalculation(registrationDetailsDTO.getApplicationNo(),
				Arrays.asList(ServiceEnum.TEMPORARYREGISTRATION), new ArrayList<>());
		if (greenTaxAndValidity != null && greenTaxAndValidity.getTaxAmountForPayments() != 0) {
			status = Boolean.TRUE;
		}
		return status;
	}

	// To validate the aadhaar number of citizen with aadhaar response
	@Override
	public AadharDetailsResponseVO getAadharResponse(AadhaarDetailsRequestVO request,
			AadhaarSourceDTO aadhaarSourceDTO) {

		Optional<AadharDetailsResponseVO> applicantDetailsOptional = restGateWayService.validateAadhaar(request,
				aadhaarSourceDTO);

		if (!applicantDetailsOptional.isPresent()) {
			logger.error("No data Found for Aadhaar : [{}]", request.getUid_num());
			throw new BadRequestException(MessageKeys.AADHAAR_RES_NO_DATA);

		}
		if (applicantDetailsOptional.get().getAuth_status()
				.equals(ResponseStatusEnum.AADHAARRESPONSE.FAILED.getLabel())) {
			logger.error("Aadhaar Validation Failed for [{}], Failed Message  : [{}]",
					applicantDetailsOptional.get().getUid(), applicantDetailsOptional.get().getAuth_status());
			throw new BadRequestException(applicantDetailsOptional.get().getAuth_err_code());
		}

		return applicantDetailsOptional.get();
	}

	@Override
	public RegServiceVO getRegServiceDetailsVo(String applicationNo) {

		RegServiceDTO dto = this.getRegServiceDetails(applicationNo);
		RegServiceVO vo = regServiceMapper.convertEntity(dto);
		return vo;
	}

	@Override
	public List<String> validations(RegistrationDetailsDTO registrationOptional, List<String> errors, String key,
			RcValidationVO rcValidationVO) throws RcValidationException {

		if (key.equalsIgnoreCase(ServicesValidations.THEFTINTIMATION.getCode())) {
			verifyTheftIntimation(errors, rcValidationVO.getPrNo(), rcValidationVO.getServiceIds());
		}

		if (key.equalsIgnoreCase(ServicesValidations.THEFTREVOCATION.getCode())) {
			verifyTheftRevocation(registrationOptional, errors, rcValidationVO);
		}
		if (key.equalsIgnoreCase(ServicesValidations.NOC.getCode())) {
			verifyNoc(errors, rcValidationVO.getPrNo(), rcValidationVO.getServiceIds());
		}

		if (key.equalsIgnoreCase(ServicesValidations.CNOC.getCode())) {
			verifyCancellationOfNoc(registrationOptional, errors, rcValidationVO);
		}

		if (key.equalsIgnoreCase(ServicesValidations.INSURANCE.getCode())) {
			verifyInsurance(registrationOptional, errors);
		}

		if (key.equalsIgnoreCase(ServicesValidations.PUC.getCode())) {
			checkPuc(registrationOptional, errors);
		}

		if (key.equalsIgnoreCase(ServicesValidations.PRVALIDITY.getCode())) {
			checkPrValidity(registrationOptional, errors, rcValidationVO);
		}

		if (key.equalsIgnoreCase(ServicesValidations.SUSPENDORCANCEL.getCode())) {
			checkRcsuspendOrcancelled(registrationOptional, errors);
		}
		if (key.equalsIgnoreCase(ServicesValidations.VCR.getCode())) {
			checkVcrDues(registrationOptional, errors);
		}

		if (key.equalsIgnoreCase(ServicesValidations.CCTNS.getCode())) {
			checkCctnsDues(registrationOptional, errors);
		}

		if (key.equalsIgnoreCase(ServicesValidations.THEFT.getCode())) {
			checkObjectionOrTheft(registrationOptional, errors);
		}
		if (key.equalsIgnoreCase(ServicesValidations.GREENTAX.getCode())) {
			checkGreenTax(registrationOptional, errors);
		}
		if (key.equalsIgnoreCase(ServicesValidations.TAX.getCode())) {
			if (checkIsTaxPending(registrationOptional)) {
				errors.add("Tax or Green Tax pending for this vehicle <" + registrationOptional.getPrNo()
						+ "> Please pay tax before proceeding with this service ");
			}
			/*
			 * if (checkTaxDues(registrationOptional, LocalDate.now())) {
			 * errors.add("Tax pending for this vehicle <"+ registrationOptional.getPrNo()
			 * +"> Please pay tax before proceeding with this service " ); }
			 */
		}
		if (key.equalsIgnoreCase(ServicesValidations.FC.getCode())) {
			Boolean fcValidationRequired = Boolean.TRUE;
			if (registrationOptional.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.EIBT.getCovCode())) {
				if (registrationOptional.getPrGeneratedDate() == null) {
					throw new BadRequestException("Vehicle validity details are not present with this RC no [ "
							+ rcValidationVO.getPrNo() + "]");
				}
				if (registrationOptional.getPrGeneratedDate().toLocalDate().plusYears(15).isBefore(LocalDate.now())
						&& rcValidationVO.getServiceIds().stream()
								.anyMatch(id -> id.equals(ServiceEnum.SURRENDEROFPERMIT.getId()))) {
					fcValidationRequired = Boolean.FALSE;
				}
			}
			if (rcValidationVO.getTransferType() != null
					&& rcValidationVO.getTransferType().equals(TransferType.DEATH)) {
				fcValidationRequired = Boolean.FALSE;
			}
			if (rcValidationVO.getServiceIds().contains(ServiceEnum.TRANSFEROFOWNERSHIP.getId())
					&& rcValidationVO.getOwnerType() != null && rcValidationVO.getOwnerType().equals(OwnerType.BUYER)) {
				fcValidationRequired = Boolean.FALSE;
			}
			if (!rcValidationVO.getServiceIds().contains(ServiceEnum.NEWFC.getId())) {
				if (registrationOptional.getServiceIds() != null && registrationOptional.getServiceIds().stream()
						.anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))) {
					fcValidationRequired = Boolean.FALSE;
				}
				if (fcValidationRequired) {
					checkFc(registrationOptional, errors);
				}

			}
		}

		if (key.equalsIgnoreCase(ServicesValidations.PENDINGTRANSACTIONS.getCode())) {
			checkAnyPendingTransactions(registrationOptional, errors, rcValidationVO.getServiceIds());
		}
		if (key.equalsIgnoreCase(ServicesValidations.PERMIT.getCode())) {
			checkPermit(registrationOptional, errors);
		}
		if (key.equalsIgnoreCase(ServicesValidations.UNDERJUDICIAL.getCode())) {
			checkUnderJudicial(registrationOptional, errors);
		}
		if (key.equalsIgnoreCase(ServicesValidations.RENEWALVALIDATION.getCode())) {
			checkCovForRenewal(registrationOptional, errors);
		}
		if (key.equalsIgnoreCase(ServicesValidations.TOWVALIDATIONS.getCode())) {
			towValidations(registrationOptional, errors, rcValidationVO);
		}
		if (key.equalsIgnoreCase(ServicesValidations.REASSIGNMENTVALIDATION.getCode())) {
			checkToAllowReassignmentOrNot(registrationOptional, errors, rcValidationVO);
		}
		if (key.equalsIgnoreCase(ServicesValidations.FCVALIDATIONS.getCode())) {
			fcValidations(registrationOptional, errors);
		}
		if (key.equalsIgnoreCase(ServicesValidations.VEHICLESTOPPAGE.getCode())) {
			vehicleStoppageValidation(registrationOptional, errors);
		}
		if (key.equalsIgnoreCase(ServicesValidations.ISVEHICLESTOPPED.getCode())) {
			isVehicleStopped(registrationOptional, errors);
		}
		if (key.equalsIgnoreCase(ServicesValidations.VEHICLESTOPPAGEREVOKED.getCode())) {
			vehicleStoppageRevokationValidation(registrationOptional, errors);
		}
		if (key.equalsIgnoreCase(ServicesValidations.ISVEHICLESTOPPAGEREVOKED.getCode())) {
			isVehicleStoppageRevoked(registrationOptional, errors);
		}
		if (key.equalsIgnoreCase(ServicesValidations.ISANYFEEPENDING.getCode())) {
			isAnyFeePending(registrationOptional, errors);
		}
		if (key.equalsIgnoreCase(ServicesValidations.RCFORFINANCE.getCode())) {
			freshRCValidations(registrationOptional, errors);
		}
		if (key.equalsIgnoreCase(ServicesValidations.RCCANCELLATION.getCode())) {
			validationForRcCancellation(registrationOptional, errors);
		}
		if (key.equalsIgnoreCase(ServicesValidations.SCRTRENEWALOFPERMIT.getCode())) {
			validationForScrtRenewalOfPermit(registrationOptional, errors);
		}
		if (key.equalsIgnoreCase(ServicesValidations.SCRTREPLACEMENTOFVEHICLET.getCode())) {
			validationForScrtReplacementOfVehicle(registrationOptional, errors);
		}
		return errors;

	}

	public List<String> vehicleStoppageValidation(RegistrationDetailsDTO registrationOptional, List<String> errors) {

		if (checkIsVehicleunderLifeTax(registrationOptional)) {
			errors.add("RC Number entered doesn't belongs to quater tax  Category : " + registrationOptional.getPrNo());
		}
		Optional<PropertiesDTO> stoppageMaster = propertiesDAO.findByStoppageDaysStatusTrue();
		if (!stoppageMaster.get().getMonthsForStoppage().stream()
				.anyMatch(month -> month.equals(LocalDate.now().getMonthValue()))) {
			errors.add("There is no provision to apply Vehicle stoppage in this month   : "
					+ registrationOptional.getPrNo());
		}
		return errors;
	}

	public List<String> isVehicleStopped(RegistrationDetailsDTO registrationOptional, List<String> errors) {

		if (registrationOptional.isVehicleStoppaged()) {
			errors.add("Vehicle is stopped, Please revoke the vehicle : " + registrationOptional.getPrNo());
		}

		return errors;
	}

	public List<String> vehicleStoppageRevokationValidation(RegistrationDetailsDTO registrationOptional,
			List<String> errors) {

		if (!registrationOptional.isVehicleStoppaged()) {
			errors.add("Please apply vehicle stoppage before apply vehicle stoppage revokation : "
					+ registrationOptional.getPrNo());
			return errors;
		}
		Optional<RegServiceDTO> listOfResServiceDetails = regServiceDAO
				.findByPrNoAndServiceTypeInAndApplicationStatusOrderByCreatedDateDesc(registrationOptional.getPrNo(),
						Arrays.asList(ServiceEnum.VEHICLESTOPPAGE), StatusRegistration.INITIATED.getDescription());
		if (listOfResServiceDetails != null && listOfResServiceDetails.isPresent()) {
			RegServiceDTO dto = listOfResServiceDetails.get();
			List<VehicleStoppageMVIReportVO> reports = registratrionServicesApprovals.getPendingQuarters(dto);
			if (reports != null && !reports.isEmpty()) {
				errors.add("Stoppage inspection pending at MVI level : " + registrationOptional.getPrNo());
			}
		}
		return errors;
	}

	public List<String> isVehicleStoppageRevoked(RegistrationDetailsDTO registrationOptional, List<String> errors) {

		if (registrationOptional.isVehicleStoppageRevoked()) {
			errors.add("Please pay the differntial tax : " + registrationOptional.getPrNo());
		}

		return errors;
	}

	public List<String> isAnyFeePending(RegistrationDetailsDTO registrationOptional, List<String> errors) {

		Optional<FeeCorrectionDTO> feeCorrection = feeCorrectionDAO
				.findByChassisNoAndStatusIsTrue(registrationOptional.getVahanDetails().getChassisNumber());
		if (feeCorrection.isPresent() && feeCorrection.get().isApproved()) {
			errors.add("Please pay difference fee : " + registrationOptional.getPrNo());
		}

		return errors;
	}

	private List<TaxDetailsDTO> getTaxDetails(RegistrationDetailsDTO registrationOptional, List<String> taxType) {

		List<TaxDetailsDTO> listOfTaxDetails = new ArrayList<>();
		List<TaxDetailsDTO> listOfGreenTax = taxDetailsDAO
				.findFirst10ByApplicationNoOrderByCreatedDateDesc(registrationOptional.getApplicationNo());
		if (listOfGreenTax.isEmpty()) {
			logger.error("TaxDetails not found: [{}]", registrationOptional.getPrNo());
			throw new BadRequestException("TaxDetails not found:" + registrationOptional.getPrNo());
		}
		this.updatePaidDateAsCreatedDate(listOfGreenTax);

		listOfGreenTax.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		for (String type : taxType) {
			for (TaxDetailsDTO taxDetails : listOfGreenTax) {
				if (taxDetails.getTaxDetails() == null) {
					logger.error("TaxDetails not found: [{}]", registrationOptional.getPrNo());
					throw new BadRequestException("TaxDetails not found:" + registrationOptional.getPrNo());
				}
				if (taxDetails.getTaxDetails().stream().anyMatch(key -> key.keySet().contains(type))) {
					listOfTaxDetails.add(taxDetails);
					break;
				}
			}
		}
		listOfGreenTax.clear();
		return listOfTaxDetails;
	}

	public List<String> checkGreenTax(RegistrationDetailsDTO registrationOptional, List<String> errors) {

		// checkNonTransportGreenTax(15 years) Validity and
		// checkTransportGreenTax(7
		// years)
		// for non transport every 5 years
		// for transport every year
		//
		/*
		 * if (registrationOptional.getVehicleType().equalsIgnoreCase(CovCategory.N
		 * .getCode ())) { LocalDate currentDateMinusNonTranValidity = LocalDate.now()
		 * .minusYears(ValidityEnum.GreenTax_NON_TRANSPORT_VALIDITY.getValidity( )).
		 * minusDays(1); if
		 * (registrationOptional.getRegistrationValidity().getPrGeneratedDate()
		 * .isBefore(currentDateMinusNonTranValidity)) {
		 *
		 * List<TaxDetailsDTO> listOfGreenTax = getTaxDetails(registrationOptional,
		 * Arrays.asList(ServiceCodeEnum.GREEN_TAX.getCode()));
		 *
		 * if(listOfGreenTax==null || listOfGreenTax.isEmpty()) {
		 * errors.add("Please pay Green tax: " + registrationOptional.getPrNo()); return
		 * errors; } TaxDetailsDTO dto = listOfGreenTax.stream().findFirst().get();
		 *
		 *
		 * for (Map<String, TaxComponentDTO> map : dto.getTaxDetails()) {
		 *
		 * for (Entry<String, TaxComponentDTO> entry : map.entrySet()) { if
		 * (entry.getKey().equalsIgnoreCase(ServiceCodeEnum.GREEN_TAX.getCode()) ) { if
		 * (entry.getValue().getValidityTo().isBefore(LocalDate.now())) {
		 * errors.add("Please pay Green tax: " + registrationOptional.getPrNo()); } } }
		 * }
		 *
		 * }
		 *
		 * } else if
		 * (registrationOptional.getVehicleType().equalsIgnoreCase(CovCategory.T
		 * .getCode ())) { Long tarnsportValiddays = ChronoUnit.DAYS
		 * .between(registrationOptional.getRegistrationValidity().
		 * getPrGeneratedDate(), LocalDate.now()); LocalDate
		 * currentDateMinusTranValidity = LocalDate.now()
		 * .minusYears(ValidityEnum.GreenTax__TRANSPORT_VALIDITY.getValidity()).
		 * minusDays(1); if
		 * (registrationOptional.getRegistrationValidity().getPrGeneratedDate()
		 * .isBefore(currentDateMinusTranValidity)) {
		 *
		 * List<TaxDetailsDTO> listOfGreenTax = getTaxDetails(registrationOptional,
		 * Arrays.asList(ServiceCodeEnum.GREEN_TAX.getCode())); if(listOfGreenTax==null
		 * || listOfGreenTax.isEmpty()) { errors.add("Please pay Green tax: " +
		 * registrationOptional.getPrNo()); return errors; } TaxDetailsDTO dto =
		 * listOfGreenTax.stream().findFirst().get();
		 *
		 * for (Map<String, TaxComponentDTO> map : dto.getTaxDetails()) {
		 *
		 * for (Entry<String, TaxComponentDTO> entry : map.entrySet()) { if
		 * (entry.getKey().equalsIgnoreCase(ServiceCodeEnum.GREEN_TAX.getCode()) ) { if
		 * (entry.getValue().getValidityTo().isBefore(LocalDate.now())) {
		 * errors.add("Please pay Green tax"); } } } }
		 *
		 * } }
		 */

		return errors;
	}

	public List<String> checkPrValidity(RegistrationDetailsDTO registrationOptional, List<String> errors,
			RcValidationVO rcValidationVO) {
		Boolean fcValidationRequired = Boolean.TRUE;
		if ((rcValidationVO.getTransferType() != null && rcValidationVO.getTransferType().equals(TransferType.DEATH))
				|| (rcValidationVO.getServiceIds().contains(ServiceEnum.TRANSFEROFOWNERSHIP.getId())
						&& rcValidationVO.getOwnerType().equals(OwnerType.BUYER))) {
			fcValidationRequired = Boolean.FALSE;
		}
		if (ServiceEnum.fcValidationRequired(rcValidationVO.getServiceIds())) {
			fcValidationRequired = Boolean.FALSE;
		}
		if (registrationOptional.getVehicleType().equalsIgnoreCase(CovCategory.N.getCode())) {
			if (registrationOptional.getRegistrationValidity().getRegistrationValidity()
					.isBefore(LocalDateTime.now())) {
				if (rcValidationVO.getTransferType() != null
						&& rcValidationVO.getTransferType().equals(TransferType.SALE)) {
					throw new BadRequestException(
							"Please Renewal the RC before doing Transfer of Ownership(only Death case, RENEWAL + TOW combination allowed) : "
									+ registrationOptional.getPrNo());
				}

				errors.add("Please Renewal the RC: " + registrationOptional.getPrNo());
			}
		} else if (registrationOptional.getVehicleType().equalsIgnoreCase(CovCategory.T.getCode())
				&& !registrationOptional.getClassOfVehicle().equals("TRTT")
				&& !rcValidationVO.getServiceIds().contains(ServiceEnum.NEWFC.getId())
				&& !rcValidationVO.getServiceIds().contains(ServiceEnum.ALTERATIONOFVEHICLE.getId())
				&& !rcValidationVO.getServiceIds().contains(ServiceEnum.CANCELLATIONOFNOC.getId())
				&& fcValidationRequired) {
			LocalDate fcValidity = null;
			if (registrationOptional.getRegistrationValidity().getFcValidity() == null) {
				List<FcDetailsDTO> fcDetailsList = fcDetailsDAO
						.findFirst5ByStatusIsTrueAndPrNoOrderByCreatedDateDesc(registrationOptional.getPrNo());
				if (fcDetailsList.isEmpty()) {
					throw new BadRequestException("No FC details found for prNo: " + registrationOptional.getPrNo()
							+ ". Please apply for new FC");
				}
				FcDetailsDTO fcDetailsDTO = fcDetailsList.stream().findFirst().get();
				if (fcDetailsDTO.getFcValidUpto() == null) {
					throw new BadRequestException("Fc validUpto not found for prNo :" + registrationOptional.getPrNo());
				}
				fcValidity = fcDetailsDTO.getFcValidUpto();
				registrationOptional.getRegistrationValidity().setFcValidity(fcValidity);
			} else {
				fcValidity = registrationOptional.getRegistrationValidity().getFcValidity();
			}
			if (fcValidity != null && fcValidity.isBefore(LocalDate.now())
					&& !registrationOptional.getClassOfVehicle().equals("TRTT")
					&& registrationOptional.getServiceIds() != null && !registrationOptional.getServiceIds().stream()
							.anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))) {
				errors.add("No FC details found for prNo: " + registrationOptional.getPrNo()
						+ " Please apply for new FC ");
			}
		}

		return errors;
	}

	@Override
	public List<String> checkRcsuspendOrcancelled(RegistrationDetailsDTO registrationOptional, List<String> errors) {

		if (checkStatusForSuspendOrcancelled(registrationOptional)) {
			errors.add("Your Rc is Suspend " + registrationOptional.getPrNo());
		}
		return errors;
	}

	private boolean checkStatusForSuspendOrcancelled(RegistrationDetailsDTO registrationOptional) {
		List<String> listOfStatus = new ArrayList<>();
		listOfStatus.add(Status.RCActionStatus.SUSPEND.toString());
		listOfStatus.add(Status.RCActionStatus.CANCELED.toString());
		listOfStatus.add(Status.RCActionStatus.INITIATED.toString());
		if (StringUtils.isNoneBlank(registrationOptional.getActionStatus())) {
			if (listOfStatus.stream()
					.anyMatch(status -> status.equalsIgnoreCase(registrationOptional.getActionStatus()))) {
				return Boolean.TRUE;
			} else {
				return Boolean.FALSE;
			}
		}
		return Boolean.FALSE;

	}

	public boolean checkIsVehicleFinanced(RegistrationDetailsDTO registrationOptional) {

		return false;
	}

	public boolean checkTaxDues(RegistrationDetailsDTO registrationOptional, LocalDate date) {

		boolean statue = Boolean.FALSE;

		List<String> taxTypes = new ArrayList<>();
		taxTypes.add(ServiceCodeEnum.QLY_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.HALF_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.YEAR_TAX.getCode());

		List<TaxDetailsDTO> listOfGreenTax = getTaxDetails(registrationOptional, taxTypes);
		listOfGreenTax.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		TaxDetailsDTO dto = listOfGreenTax.stream().findFirst().get();

		for (Map<String, TaxComponentDTO> map : dto.getTaxDetails()) {

			for (Entry<String, TaxComponentDTO> entry : map.entrySet()) {
				if (taxTypes.stream().anyMatch(key -> key.equalsIgnoreCase(entry.getKey()))) {
					if (entry.getValue().getValidityTo().isBefore(date)) {
						statue = Boolean.TRUE;
					}
				}
			}
			// }

		}

		return statue;
	}

	@Override
	public List<String> checkVcrDues(RegistrationDetailsDTO registrationOptional, List<String> errors) {
		VcrInputVo vcrInputVo = new VcrInputVo();
		vcrInputVo.setDocumentType("RC");
		vcrInputVo.setRegNo(registrationOptional.getPrNo());
		if (restGateWayService.getVcrDetailsCfst(vcrInputVo) != null) {
			VcrBookingData entity = restGateWayService.getVcrDetailsCfst(vcrInputVo);
			if (entity != null) {
				if (entity.getVcrStatus().equalsIgnoreCase("O")) {
					errors.add("You have offline(CFST) VCR with VCR number " + entity.getVcrNum()
							+ "in legacy system . Please contact RTA office for payment");
				}
			}
		}

		if (StringUtils.isNoneBlank(registrationOptional.getApplicationNo())) {
			vcrInputVo.setApplicationNo(registrationOptional.getApplicationNo());
		}
		if (StringUtils.isNoneBlank(registrationOptional.getPrNo())) {
			vcrInputVo.setRegNo(registrationOptional.getPrNo());
		}
		if (StringUtils.isNoneBlank(registrationOptional.getTrNo())) {
			vcrInputVo.setTrNo(registrationOptional.getTrNo());
		}
		if (registrationOptional.getVahanDetails() != null
				&& StringUtils.isNoneBlank(registrationOptional.getVahanDetails().getChassisNumber())) {
			vcrInputVo.setChassisNo(registrationOptional.getVahanDetails().getChassisNumber());
		}
		List<VcrFinalServiceDTO> vcrList = this.getOnlineVcrData(vcrInputVo);
		if (vcrList != null && !vcrList.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			vcrList.stream().forEach(val -> {
				if (val.getVcr() != null && StringUtils.isNotBlank(val.getVcr().getVcrNumber())
						&& val.getCreatedDate() != null) {
					sb.append("VCR number ");
					sb.append(val.getVcr().getVcrNumber() + " - "
							+ DateConverters.convertLocalDateFormat(val.getCreatedDate()) + ",");
				}
			});
			sb.replace(sb.lastIndexOf(","), sb.lastIndexOf(",") + 1, " "); // for remove the "," at the last
			errors.add("VCR details found with this Registration number " + registrationOptional.getPrNo() + " ("
					+ sb.toString() + " ) ");
		} else {

			FreezeVehiclsDTO dto = freezeVehiclsDAO.findByPrNoIn(registrationOptional.getPrNo());
			if (dto != null) {
				errors.add(
						"Sorry... You are not allowed for further services as your vehicle is caught by Motor Vehicle Inspector and vehicle challan report in under processing....");
			}

		}

		return errors;
	}

	public List<String> checkCctnsDues(RegistrationDetailsDTO registrationOptional, List<String> errors) {

		// Is there any CCTNS fees payment dues
		// CCTNS fees payment

		return errors;
	}

	@Override
	public List<String> checkObjectionOrTheft(RegistrationDetailsDTO registrationOptional, List<String> errors) {

		//Is there any objection/theft on this Vehicle
		// Theft/Objection found on this Vehicle
		if (registrationOptional.getTheftState() != null
				&& (registrationOptional.getTheftState().equals(StatusRegistration.TheftState.INTIMATIATED)
						|| registrationOptional.getTheftState().equals(StatusRegistration.TheftState.OBJECTION))) {
			errors.add("Theft intimated/Objected plese revoke the theft : " + registrationOptional.getPrNo());
		}

		return errors;
	}

	public List<String> checkFc(RegistrationDetailsDTO registrationOptional, List<String> errors) {
		// Only in case of Transport Vehicle
		// Fitness for the Vehicle is expired, please Renew Fitness of your
		// vehicle
		if (registrationOptional.getVehicleType().equalsIgnoreCase(CovCategory.T.getCode())
				&& !registrationOptional.getClassOfVehicle().equals("TRTT")) {
			LocalDate fcValidity;
			// if (registrationOptional.getRegistrationValidity().getFcValidity() == null) {
			List<FcDetailsDTO> fcDetailsList = fcDetailsDAO
					.findFirst5ByStatusIsTrueAndPrNoOrderByCreatedDateDesc(registrationOptional.getPrNo());
			if (fcDetailsList.isEmpty()) {
				throw new BadRequestException("No FC details found for prNo: " + registrationOptional.getPrNo()
						+ ". Please apply for new FC");
			}
			FcDetailsDTO fcDetailsDTO = fcDetailsList.stream().findFirst().get();
			if (fcDetailsDTO.getFcValidUpto() == null) {
				throw new BadRequestException("Fc validUpto not found for prNo :" + registrationOptional.getPrNo());
			}
			fcValidity = fcDetailsDTO.getFcValidUpto();
			registrationOptional.getRegistrationValidity().setFcValidity(fcValidity);
			/*
			 * } else { fcValidity =
			 * registrationOptional.getRegistrationValidity().getFcValidity(); }
			 */
			if (fcValidity.isBefore(LocalDate.now())) {
				errors.add("FC expired for prNo:" + registrationOptional.getPrNo() + " Please apply for renewal of FC");
			}
			if (registrationOptional.isCfxIssued()) {
				errors.add("CFX issued by MVI. Please renewal the FC: " + registrationOptional.getPrNo());
			}
		}
		if (registrationOptional.isCfxIssued()) {
			errors.add("CFX issued by MVI. Please renewal the FC: " + registrationOptional.getPrNo());
		}
		return errors;
	}

	public List<String> fcValidations(RegistrationDetailsDTO registrationOptional, List<String> errors) {

		if (registrationOptional.getVehicleType().equalsIgnoreCase(CovCategory.N.getCode())) {
			errors.add("RC Number entered doesn't belongs to Transport Category : " + registrationOptional.getPrNo());
		} else {
			if (!registrationOptional.isCfxIssued()) {
				LocalDate fcValidity = null;
				/*
				 * if (registrationOptional.getRegistrationValidity() != null &&
				 * registrationOptional.getRegistrationValidity().getFcValidity() != null) {
				 * fcValidity = registrationOptional.getRegistrationValidity().getFcValidity();
				 * 
				 * } else {
				 */
				List<FcDetailsDTO> fcDetailsList = fcDetailsDAO
						.findFirst5ByStatusIsTrueAndPrNoOrderByCreatedDateDesc(registrationOptional.getPrNo());
				if (!fcDetailsList.isEmpty()) {
					FcDetailsDTO fcDetailsDTO = fcDetailsList.stream().findFirst().get();
					if (fcDetailsDTO.getFcValidUpto() != null) {
						fcValidity = fcDetailsDTO.getFcValidUpto();
						registrationOptional.getRegistrationValidity().setFcValidity(fcValidity);

					}
				}

				// }
				if (fcValidity != null) {
					Optional<PropertiesDTO> optionalPropertie = propertiesDAO.findByModule(ModuleEnum.FC.getCode());
					Long tarnsportValiddays = ChronoUnit.DAYS.between(LocalDate.now().minusDays(1), fcValidity);
					if (!(tarnsportValiddays <= optionalPropertie.get().getFcValidity())) {
						logger.error("Please apply " + optionalPropertie.get().getFcValidity()
								+ " days before the expiry of FC. PR No: [{}]", registrationOptional.getPrNo());
						errors.add("Please apply " + optionalPropertie.get().getFcValidity()
								+ " days before the expiry of FC. PR No:" + registrationOptional.getPrNo());
					}
				}
			}
		}

		if (registrationOptional.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.EIBT.getCovCode())) {
			this.calculateEibtVehicleAge(registrationOptional, errors);
		}
		return errors;
	}

	@Override
	public List<String> verifyNoc(List<String> errors, String prNo, Set<Integer> serviceIds) {

		// verify if there is any NOC issued against the vehicle or not NOC is
		// issued
		// you cannot do any transaction,
		// please cancel the issued NOC first
		if (prNo != null) {
			/*
			 * List<RegServiceDTO> regServiceList =
			 * regServiceDAO.findByPrNoAndServiceTypeNotInAndSourceIsNull(prNo,
			 * Arrays.asList(ServiceEnum.TAXATION));
			 */
			List<RegServiceDTO> regServiceList = regServiceDAO.findByPrNoAndServiceTypeNotIn(prNo,
					ServiceEnum.getServiceEnumById(ServiceEnum.TAXATION.getId()));

			if (!CollectionUtils.isEmpty(regServiceList)) {
				validateMissingCreatedDate(regServiceList);
				regServiceList.sort((p2, p1) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
				RegServiceDTO regServiceDTO = regServiceList.stream().findFirst().get();
				if (regServiceDTO.getServiceIds() != null && !regServiceDTO.getServiceIds().isEmpty()) {
					if (!serviceIds.contains(ServiceEnum.CANCELLATIONOFNOC.getId())) {
						if (regServiceDTO.getServiceIds().contains(ServiceEnum.ISSUEOFNOC.getId())) {
							if (serviceIds.contains(ServiceEnum.TRANSFEROFOWNERSHIP.getId())) {
								// Buyer conditions added. To ignore this error
								// msg
								// in the TOW and NOC
								// combination at buyer end.
								if (regServiceDTO.getBuyerDetails() != null
										&& regServiceDTO.getBuyerDetails().getBuyer() != null) {
									// combination at buyer end.
									logger.error("Please cancel NOC to avail any services for prNo : [{}]", prNo);
									errors.add("Please cancel NOC to avail any services for prNo : " + prNo);
								}
							} else {
								logger.error("Please cancel NOC to avail any services for prNo : [{}]", prNo);
								errors.add("Please cancel NOC to avail any services for prNo : " + prNo);
							}
						}
					}
				}
			}
		}
		return errors;

	}

	@Override
	public List<String> verifyTheftIntimation(List<String> errors, String prNo, Set<Integer> serviceIds) {
		if (prNo != null) {

			List<RegServiceDTO> regServiceList = regServiceDAO.findByPrNoAndServiceTypeIn(prNo,
					ServiceEnum.getTheftRelatedServices());

			if (!regServiceList.isEmpty()) {
				regServiceList.sort((p2, p1) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
				RegServiceDTO regServiceDTO = regServiceList.stream().findFirst().get();

				if (regServiceDTO.getServiceIds().contains(ServiceEnum.THEFTINTIMATION.getId()) && regServiceDTO
						.getApplicationStatus().toString().equals(StatusRegistration.INITIATED.toString())) {
					errors.add("THEFT INTIMATION is already in open state with applicationNo"
							+ regServiceDTO.getApplicationNo() + " prNo :" + prNo);
				}

				if (regServiceDTO.getServiceIds().contains(ServiceEnum.THEFTREVOCATION.getId()) && regServiceDTO
						.getApplicationStatus().toString().equals(StatusRegistration.INITIATED.toString())) {
					errors.add("THEFT REVOCATION is already in open state with applicationNo"
							+ regServiceDTO.getApplicationNo() + "  prNo : " + prNo);
				}

				if (regServiceDTO.getServiceIds().contains(ServiceEnum.OBJECTION.getId()) && regServiceDTO
						.getApplicationStatus().toString().equals(StatusRegistration.INITIATED.toString())) {
					errors.add("THEFT OBJECTION is already in open state with applicationNo "
							+ regServiceDTO.getApplicationNo() + "  prNo : " + prNo);
				}

				if (regServiceDTO.getServiceIds().contains(ServiceEnum.REVOCATION.getId()) && regServiceDTO
						.getApplicationStatus().toString().equals(StatusRegistration.INITIATED.toString())) {
					errors.add("THEFT REVOCATION is already in open state with applicationNo  for prNo : "
							+ regServiceDTO.getApplicationNo() + " " + prNo);
				}

				if (!serviceIds.contains(ServiceEnum.THEFTREVOCATION.getId())
						&& regServiceDTO.getServiceIds().contains(ServiceEnum.THEFTINTIMATION.getId()) && !regServiceDTO
								.getApplicationStatus().toString().equals((StatusRegistration.REJECTED.toString()))) {
					logger.error(
							" THEFT is INITIATED/PENDING for approval. Please  appply for THEFT REVOCATE to avail any services for prNo : [{}]",
							prNo);
					errors.add(
							"THEFT is INITIATED/PENDING for approval. Please  appply for THEFT REVOCATE to avail any services for prNo  : "
									+ prNo);
				}

				if (!regServiceDTO.getServiceIds().isEmpty()) {
					if (serviceIds.contains(ServiceEnum.THEFTINTIMATION.getId())
							&& regServiceDTO.getServiceIds().contains(ServiceEnum.THEFTINTIMATION.getId())
							&& !regServiceDTO.getApplicationStatus().toString()
									.equals(StatusRegistration.REJECTED.getDescription())) {
						logger.error(
								" Already applied for THEFT INTIMATION, please THEFT REVOCATE to avail any services for prNo : [{}]",
								prNo);
						errors.add(
								"Already applied for THEFT INTIMATION, please THEFT REVOCATE to avail any services for prNo : "
										+ prNo);
					}

				}
			}

		}
		return errors;

	}

	private List<String> verifyTheftRevocation(RegistrationDetailsDTO registrationOptional, List<String> errors,
			RcValidationVO rcValidationVO) {
		if (rcValidationVO.getServiceIds().contains(ServiceEnum.THEFTREVOCATION.getId())) {
			List<RegServiceDTO> regServiceList = regServiceDAO.findByPrNoAndServiceTypeNotIn(rcValidationVO.getPrNo(),
					ServiceEnum.getServiceEnumById(ServiceEnum.TAXATION.getId()));

			if (regServiceList.isEmpty()) {
				errors.add(" PLEASE APPLY THEFT INTIMATION or THEFT OBJECTION to REVOCATE for prNo:"
						+ rcValidationVO.getPrNo());
			}
			if (!regServiceList.isEmpty()) {
				regServiceList.sort((p2, p1) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
				List<RegServiceDTO> filterList = filterServiceIdsNotEmpty(regServiceList);
				if (!CollectionUtils.isEmpty(filterList)) {
					RegServiceDTO regServiceDTO = filterList.stream().findFirst().get();
					if (!(regServiceDTO.getServiceType().contains(ServiceEnum.THEFTINTIMATION))) {
						errors.add(" PLEASE APPLY THEFT INTIMATION or THEFT OBJECTION to REVOCATE for applicationNo "
								+ regServiceDTO.getApplicationNo() + "prNO" + rcValidationVO.getPrNo());
					}

					if (!regServiceDTO.getApplicationStatus().toString()
							.equals(StatusRegistration.APPROVED.getDescription())) {
						errors.add(" THEFT INTIMATION/OBJECTION is pending for approvals "
								+ regServiceDTO.getApplicationNo() + "prNO" + rcValidationVO.getPrNo());
					}
				} else {
					errors.add(" PLEASE APPLY THEFT INTIMATION or THEFT OBJECTION to REVOCATE for prNo:"
							+ rcValidationVO.getPrNo());
				}
			}
		}
		return errors;
	}

	public List<RegServiceDTO> filterServiceIdsNotEmpty(List<RegServiceDTO> regServiceList) {
		return regServiceList.stream()
				.filter(val -> val.getSource() == null
						|| (val.getSource() != null
								&& (val.getSource().equals(SourceEnum.CFST0.getDesc())
										|| val.getSource().equals(SourceEnum.CFST1.getDesc()))
								&& val.getServiceIds() != null))
				.collect(Collectors.toList());
	}

	public List<String> verifyCancellationOfNoc(RegistrationDetailsDTO registrationOptional, List<String> errors,
			RcValidationVO rcValidationVO) {

		// Noc can be cancelled only if Noc is issued

		if (rcValidationVO.getPrNo() != null) {
			List<RegServiceDTO> regServiceList = regServiceDAO.findByPrNoAndServiceTypeNotIn(rcValidationVO.getPrNo(),
					ServiceEnum.getServiceEnumById(ServiceEnum.TAXATION.getId()));

			if (!regServiceList.isEmpty()) {
				regServiceList.sort((p2, p1) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
				RegServiceDTO regServiceDTO = regServiceList.stream().findFirst().get();
				if (rcValidationVO.getServiceIds().contains(ServiceEnum.CANCELLATIONOFNOC.getId())
						&& regServiceDTO.getServiceIds().contains(ServiceEnum.CANCELLATIONOFNOC.getId())) {
					logger.error("You have already Cancelled your NOC for prNo : [{}]", rcValidationVO.getPrNo());
					errors.add("You have already Cancelled your NOC for prNo : " + rcValidationVO.getPrNo());
				}

				if (rcValidationVO.getServiceIds().contains(ServiceEnum.CANCELLATIONOFNOC.getId())
						&& !rcValidationVO.getServiceIds().contains(ServiceEnum.RENEWAL.getId())) {

					if (registrationOptional.getRegistrationValidity().getRegistrationValidity()
							.isBefore(LocalDateTime.now())
							&& registrationOptional.getVehicleType().equalsIgnoreCase(CovCategory.N.getCode())) {
						logger.error("RC is Expired Please Select RENEWAL also for PrNo: [{}]",
								rcValidationVO.getPrNo());
						errors.add("RC is Expired Please Select RENEWAL also for PrNo: " + rcValidationVO.getPrNo());
					}
				}

				if (rcValidationVO.getServiceIds().contains(ServiceEnum.CANCELLATIONOFNOC.getId())
						&& !regServiceDTO.getServiceIds().contains(ServiceEnum.ISSUEOFNOC.getId())) {
					logger.error("You have not yet Issued NOC for prNo : [{}]", rcValidationVO.getPrNo());
					errors.add("You have not yet Issued NOC for prNo : " + rcValidationVO.getPrNo());
				}

			} else {
				if (rcValidationVO.getServiceIds().contains(ServiceEnum.CANCELLATIONOFNOC.getId())) {
					logger.error("You have not yet Issued NOC for prNo : [{}]", rcValidationVO.getPrNo());
					errors.add("You have not yet Issued NOC for prNo : " + rcValidationVO.getPrNo());
				}
			}

		}
		return errors;
	}

	public List<String> checkAnyPendingTransactions(RegistrationDetailsDTO registrationOptional, List<String> errors,
			Set<Integer> serviceIds) {

		// verify any pending transactions are there for that particular Vehicle
		// Transaction is under process for (Service Name) against the
		// application
		// number (Application Number).
		Pair<Boolean, RegServiceDTO> pendingDetailsAndStatus = checkPendingStatus(registrationOptional.getPrNo(),
				serviceIds, null);
		if (pendingDetailsAndStatus.getFirst()) {
			errors.add("You have already applied for " + pendingDetailsAndStatus.getSecond().getServiceType()
					+ " and is in progress Application No: " + pendingDetailsAndStatus.getSecond().getApplicationNo()
					+ ". Please verify the status at registration search / application search");
		}

		return errors;
	}

	public List<String> checkPermit(RegistrationDetailsDTO registrationDetails, List<String> errors) {

		// In case of Transport Vehicle Permit for the Vehicle is expired, please renew
		// your permit.
		if (null != registrationDetails.getVehicleType()
				&& registrationDetails.getVehicleType().equalsIgnoreCase(CovCategory.T.getCode())) {
			Optional<PermitDetailsDTO> primaryPermitDetailsOpt = permitDetailsDAO
					.findByPrNoAndPermitStatusAndPermitTypeTypeofPermitOrderByCreatedDateDesc(
							registrationDetails.getPrNo(), PermitsEnum.ACTIVE.getDescription(),
							PermitType.PRIMARY.getPermitTypeCode());
			if (primaryPermitDetailsOpt.isPresent()) {
				errors.add("Please apply Surrender/Cancel of Permit to avail NOC Service");
			}
		}

		return errors;
	}

	public List<String> checkPuc(RegistrationDetailsDTO registrationOptional, List<String> errors) {

		// verify if the PUC is valid or not
		// PUC is expired please renew PUC 

		return errors;
	}

	public List<String> checkUnderJudicial(RegistrationDetailsDTO registrationOptional, List<String> errors) {

		// verify whether the vehicle is under judicial custody or not
		// Vehicle is under judicial custody, transaction cannot be allowed

		return errors;
	}

	public List<String> verifyInsurance(RegistrationDetailsDTO registrationOptional, List<String> errors) {

		// must verify Insurance details from IIB
		// Insurance is expired please renew Insurance

		return errors;
	}

	public List<String> checkCovForRenewal(RegistrationDetailsDTO registrationOptional, List<String> errors) {

		// renewal will allow only non transport validity will be every 5 years
		// should allow 30 days before his rc expired date

		if (!registrationOptional.getVehicleType().equalsIgnoreCase(CovCategory.N.getCode())) {
			logger.error("Only NonTransport vehicle can apply renewal: [{}]", registrationOptional.getPrNo());
			errors.add("Only NonTransport vehicle can apply renewal: " + registrationOptional.getPrNo());
		} else {
			Long tarnsportValiddays = ChronoUnit.DAYS.between(LocalDate.now().minusDays(1),
					registrationOptional.getRegistrationValidity().getRegistrationValidity().plusDays(1));
			if (!(tarnsportValiddays <= 60)) {
				logger.error("Application not eligible for renewal: [{}]", registrationOptional.getPrNo());
				errors.add("Application not eligible for renewal:" + registrationOptional.getPrNo());
			}
		}

		return errors;
	}

	private boolean getApplicationOpenOrNorForTow(RegServiceDTO regServiceDto) {

		List<StatusRegistration> listOfStatus = new ArrayList<>();
		listOfStatus.add(StatusRegistration.CANCELED);
		listOfStatus.add(StatusRegistration.APPROVED);

		return listOfStatus.stream().anyMatch(status -> status.equals(regServiceDto.getApplicationStatus()));

	}

	public List<String> towValidations(RegistrationDetailsDTO registrationOptional, List<String> errors,
			RcValidationVO rcValidationVO) {

		if (null == rcValidationVO.getOwnerType()) {
			logger.error("OwnerType not found: [{}]", rcValidationVO.getPrNo());
			errors.add("OwnerType not found: " + rcValidationVO.getPrNo());

		}
		Optional<PropertiesDTO> expireDays = propertiesDAO.findByEnableTowConditionsTrue();
		if (rcValidationVO.getOwnerType().equals(OwnerType.SELLER)) {
			if (expireDays.isPresent() && expireDays.get().isCovCodeStatus()
					&& registrationOptional.getClassOfVehicle() != null
					&& expireDays.get().getCovToBlock().contains(registrationOptional.getClassOfVehicle())) {
				logger.error(registrationOptional.getClassOfVehicle() + " : cov restricted for tow service");
				throw new BadRequestException(registrationOptional.getClassOfVehicle()
						+ " : class of vehicle restricted for transfer of ownership service");
			}
			/*
			 * if (null != registrationOptional.getIsFinancier() &&
			 * registrationOptional.getIsFinancier() && null !=
			 * registrationOptional.getFinanceDetails()) {
			 * if(registrationOptional.getFinanceDetails().getUserId()!=null){
			 * 
			 * }else{
			 * 
			 * } if (!validateServiceIds(ServiceEnum.HIREPURCHASETERMINATION.getId(),
			 * rcValidationVO.getServiceIds())) { throw new BadRequestException(
			 * "Vehicle is under finance Apply TOW and HPT combination serviec/HPT service"
			 * ); }
			 * 
			 * }
			 */

			if (TransferType.SALE.equals(rcValidationVO.getTransferType())
					&& validateServiceIds(ServiceEnum.HPA.getId(), rcValidationVO.getServiceIds())
			/*
			 * || (TransferType.DEATH.equals(rcValidationVO.getTransferType()) &&
			 * validateServiceIds(ServiceEnum.HPA.getId(), rcValidationVO.getServiceIds()))
			 */) {
				throw new BadRequestException("HPA service not available for Seller");
			}
			if (rcValidationVO.getServiceIds().stream().anyMatch(
					id -> (id == ServiceEnum.ALTERATIONOFVEHICLE.getId()) || id == ServiceEnum.ISSUEOFNOC.getId())
					&& !TransferType.DEATH.equals(rcValidationVO.getTransferType())) {
				throw new BadRequestException(
						"Only Buyer have provision to apply services : Alteration of vehicle/Issue of NOC");

			}
			if (TransferType.DEATH.equals(rcValidationVO.getTransferType())) {
				return errors;
			}

			if (!registrationOptional.getApplicantDetails().getAadharNo()
					.equalsIgnoreCase(rcValidationVO.getAadharNo())) {
				throw new BadRequestException("Please give correct aadhar number...");
			}

			List<RegServiceDTO> listRegDetails = regServiceDAO.findByPrNoAndServiceTypeNotInAndCreatedDateNotNull(
					rcValidationVO.getPrNo(), ServiceEnum.getServiceEnumById(ServiceEnum.TAXATION.getId()));
			if (!listRegDetails.isEmpty()) {
				listRegDetails.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
				RegServiceDTO regServiceDto = listRegDetails.stream().findFirst().get();
				if (regServiceDto.getBuyerDetails() != null) {
					if (!getApplicationOpenOrNorForTow(regServiceDto)) {
						logger.error("Application is open state: [{}]", regServiceDto.getApplicationNo());
						errors.add("Application is open state: " + regServiceDto.getApplicationNo());
					}

				}
			}

		} else if (rcValidationVO.getOwnerType().equals(OwnerType.BUYER)) {

			if (rcValidationVO.getServiceIds().stream()
					.anyMatch(id -> (id == ServiceEnum.RENEWAL.getId()) || id == ServiceEnum.DUPLICATE.getId())) {
				throw new BadRequestException("Only Seller have provision to apply services : Renewal/Duplicate");

			}

			if (rcValidationVO.getServiceIds().contains(ServiceEnum.HIREPURCHASETERMINATION.getId())
					&& rcValidationVO.getServiceIds().contains(ServiceEnum.TRANSFEROFOWNERSHIP.getId())
					&& rcValidationVO.getTransferType() != null
					&& !rcValidationVO.getTransferType().equals(TransferType.DEATH)) {
				throw new BadRequestException(
						" Hire Purchase Termination not eligible at Buyer" + rcValidationVO.getTransferType());
			}
			if (!rcValidationVO.getTransferType().equals(TransferType.AUCTION)) {
				/*
				 * List<RegServiceDTO> listRegDetails =
				 * regServiceDAO.findByPrNoAndServiceTypeNotInAndCreatedDateNotNull(
				 * rcValidationVO.getPrNo(),
				 * ServiceEnum.getServiceEnumById(ServiceEnum.TAXATION.getId()));
				 */

				List<RegServiceDTO> listRegDetails = regServiceDAO
						.findByPrNoAndServiceTypeNotInAndApplicationStatusNotInAndCreatedDateNotNull(
								rcValidationVO.getPrNo(), ServiceEnum.getServiceEnumById(ServiceEnum.TAXATION.getId()),
								Arrays.asList(StatusRegistration.APPROVED.getDescription()));

				if (listRegDetails.isEmpty()) {
					logger.error("First complete Seller side: [{}]", registrationOptional.getPrNo());
					errors.add("First complete Seller side: " + registrationOptional.getPrNo());
				} else {

					listRegDetails.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
					RegServiceDTO regServiceDto = listRegDetails.stream().findFirst().get();
					if (regServiceDto.getApplicationStatus() != null
							&& regServiceDto.getApplicationStatus().equals(StatusRegistration.CANCELED)) {
						throw new BadRequestException("Seller Token Canceled");
					}
					if (regServiceDto.getApplicationStatus() != null
							&& regServiceDto.getApplicationStatus().equals(StatusRegistration.TOWITHHPTINITIATED)) {
						throw new BadRequestException("Application Pending at Financier Approvals");
					}
					if (regServiceDto.getBuyerDetails() != null) {
						if (!regServiceDto.getApplicationStatus().equals(StatusRegistration.SELLERCOMPLETED)) {
							logger.error("First complete Seller side: [{}]", regServiceDto.getApplicationNo());
							errors.add("HPT Process in progress/Complete seller process: "
									+ regServiceDto.getApplicationNo());
						} else if (regServiceDto.getBuyerDetails().getTokenNo()
								.equalsIgnoreCase(rcValidationVO.getToken())) {
							if (!regServiceDto.getBuyerDetails().isTokenStatus()) {
								logger.error("Token already used: [{}]", regServiceDto.getApplicationNo());
								errors.add("Token already used: " + regServiceDto.getApplicationNo());

							}

							long days = ChronoUnit.DAYS.between(
									regServiceDto.getBuyerDetails().getTokenNoGeneratedTime().toLocalDate(),
									LocalDate.now());
							if (expireDays.isPresent() && expireDays.get().isEnableTowConditions()) {
								if (days > expireDays.get().getTokenExpireDays()) {
									logger.error("Token is Expired: [{}]", regServiceDto.getApplicationNo());
									ApplicationSearchVO applicationSearchVO = new ApplicationSearchVO();
									applicationSearchVO.setApplicationNo(regServiceDto.getApplicationNo());
									applicationSearchVO.setTokenNo(regServiceDto.getBuyerDetails().getTokenNo());
									this.isTOtokenCanceled(applicationSearchVO);
									errors.add("Token is Expired : " + regServiceDto.getApplicationNo());
								}
							}
						} else {
							logger.error("Token miss matched.Please enter valid token: [{}]",
									regServiceDto.getApplicationNo());
							errors.add("Token miss matched.Please enter valid token : "
									+ regServiceDto.getApplicationNo());

						}
					} else {
						logger.error("Tow details not found: [{}]", regServiceDto.getApplicationNo());
						errors.add("Tow details not found: " + regServiceDto.getApplicationNo());
					}
				}
			}
		}

		return errors;
	}

	public <T> Optional<T> readValue(String value, Class<T> valueType) {

		try {
			return Optional.of(objectMapper.readValue(value, valueType));
		} catch (IOException ioe) {
			logger.debug("Exception occured while converting String to Object [{}]", ioe);

			logger.error("Exception occured while converting String to Object [{}]", ioe.getMessage());
		}

		return Optional.empty();
	}

	public void validateMissingCreatedDate(List<RegServiceDTO> regServiceList) {
		int dayVal = 1;
		Month month = Month.JANUARY;
		if (regServiceList.stream().allMatch(date -> date.getCreatedDate() == null)) {
			throw new BadRequestException(
					"Exceoption occured: " + appErrors.getResponseMessage(MessageKeys.CREATED_DATE_MISSING));
		}
		for (RegServiceDTO regService : regServiceList) {
			if (regService.getCreatedDate() == null
					&& (regService.getSource().equalsIgnoreCase(SourceEnum.CFST0.getDesc())
							|| regService.getSource().equalsIgnoreCase(SourceEnum.CFST1.getDesc()))) {
				if (dayVal > 28) {
					dayVal = 1;
					month = month.plus(1);
				}
				LocalDateTime date = LocalDateTime.of(1982, month, dayVal, 10, 10);
				regService.setCreatedDate(date);
				dayVal = dayVal + 1;
			}
		}

	}

	private Pair<Boolean, RegServiceDTO> checkPendingStatus(String prNo, Set<Integer> serviceIds,
			RegServiceDTO newServiceDetails) {
		List<ServiceEnum> list = new ArrayList<>();
		list.add(ServiceEnum.TAXATION);
		if (serviceIds.stream().anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId()))) {
			list.add(ServiceEnum.VEHICLESTOPPAGE);
		}
		List<RegServiceDTO> listOfResServiceDetails = regServiceDAO.findByPrNoAndServiceTypeNotInAndSourceIsNull(prNo,
				list);
		return commonForPendinfApplicationStatus(serviceIds, newServiceDetails, listOfResServiceDetails);
	}

	private Pair<Boolean, RegServiceDTO> commonForPendinfApplicationStatus(Set<Integer> serviceIds,
			RegServiceDTO newServiceDetails, List<RegServiceDTO> listOfResServiceDetails) {

		for (RegServiceDTO regServiceDTO : listOfResServiceDetails) {
			Optional<RegistrationDetailsDTO> registrationOptional = registrationDetailDAO
					.findByPrNo(regServiceDTO.getPrNo());
			if (regServiceDTO.getServiceType().contains(ServiceEnum.FEECORRECTION)
					&& registrationOptional.get().getClassOfVehicle() == null) {
				return Pair.of(Boolean.FALSE, new RegServiceDTO());
			}
		}

		if (listOfResServiceDetails.isEmpty()) {
			return Pair.of(Boolean.FALSE, new RegServiceDTO());
		} else {
			List<RegServiceDTO> filterList = sortByDate(listOfResServiceDetails);
			if (filterList == null || filterList.isEmpty()) {
				RegServiceDTO dto = new RegServiceDTO();
				return Pair.of(Boolean.FALSE, dto);
			}
			if (checkPendingApplicationStatus(filterList.stream().findFirst().get(), serviceIds, newServiceDetails)) {
				return Pair.of(Boolean.FALSE, filterList.stream().findFirst().get());
			} else {
				return Pair.of(Boolean.TRUE, filterList.stream().findFirst().get());
			}
		}
	}

	private List<RegServiceDTO> sortByDate(List<RegServiceDTO> listOfResServiceDetails) {
		validateMissingCreatedDate(listOfResServiceDetails);
		listOfResServiceDetails.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		List<RegServiceDTO> filterList = listOfResServiceDetails.stream()
				.filter(val -> val.getSource() == null
						|| (val.getSource() != null
								&& (val.getSource().equals(SourceEnum.CFST0.getDesc())
										|| val.getSource().equals(SourceEnum.CFST1.getDesc()))
								&& val.getServiceIds() != null))
				.collect(Collectors.toList());
		return filterList;
	}

	private boolean checkPendingApplicationStatus(RegServiceDTO regServiceDto, Set<Integer> serviceIds,
			RegServiceDTO newServiceDetails) {
		/*
		 * if (regServiceDto.getServiceIds() == null || regServiceDto.getServiceType()
		 * == null) { throw new
		 * BadRequestException("serviceIds or ServiceTypes not found for prNo :" +
		 * regServiceDto.getPrNo()); }
		 */
		List<StatusRegistration> listOfStatus = new ArrayList<>();
		listOfStatus.add(StatusRegistration.APPROVED);
		listOfStatus.add(StatusRegistration.CANCELED);
		listOfStatus.add(StatusRegistration.FINANCIERREJECTED);
		if (serviceIds.stream().anyMatch(id -> id.equals(ServiceEnum.TAXATION.getId()))) {
			List<RegServiceDTO> listOfRegService = regServiceDAO
					.findByRegistrationDetailsApplicationNoAndServiceIdsAndSourceIsNull(
							regServiceDto.getRegistrationDetails().getApplicationNo(), ServiceEnum.TAXATION.getId());
			if (!listOfRegService.isEmpty()) {
				listOfRegService.sort((s1, s2) -> s2.getCreatedDate().compareTo(s1.getCreatedDate()));
				RegServiceDTO regDto = listOfRegService.stream().findFirst().get();
				List<StatusRegistration> listOfStatusForPAymnets = new ArrayList<>();
				listOfStatusForPAymnets.add(StatusRegistration.CITIZENPAYMENTFAILED);
				listOfStatusForPAymnets.add(StatusRegistration.PAYMENTPENDING);
				if (listOfStatusForPAymnets.contains(regDto.getApplicationStatus())) {
					return false;
				}
				return true;

			} else {
				return true;
			}
		}
		if (newServiceDetails != null && newServiceDetails.getServiceIds() != null && newServiceDetails.getServiceIds()
				.stream().anyMatch(id -> id.equals(ServiceEnum.ALTERATIONOFVEHICLE.getId()))) {

			if (newServiceDetails.getAlterationDetails() != null
					&& newServiceDetails.getAlterationDetails().getAlterationService() != null
					&& newServiceDetails.getAlterationDetails().getAlterationService().stream()
							.anyMatch(type -> type.equals(AlterationTypeEnum.WEIGHT))) {
				if (regServiceDto.getServiceIds().stream()
						.anyMatch(id -> id.equals(ServiceEnum.ALTERATIONOFVEHICLE.getId()))
						&& regServiceDto.getAlterationDetails() != null
						&& regServiceDto.getAlterationDetails().getAlterationService() != null
						&& regServiceDto.getAlterationDetails().getAlterationService().stream()
								.anyMatch(type -> type.equals(AlterationTypeEnum.WEIGHT))) {
					return false;
				}
			}
		}
		if (regServiceDto.getBuyerDetails() != null && regServiceDto.getBuyerDetails().getSeller() != null
				&& regServiceDto.getBuyerDetails().getBuyer() == null) {
			if (listOfStatus.contains(regServiceDto.getApplicationStatus())) {
				return true;
			}
			listOfStatus.add(StatusRegistration.SELLERCOMPLETED);
			if (serviceIds.stream().anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId()))
					&& StatusRegistration.SELLERCOMPLETED.equals(regServiceDto.getApplicationStatus())) {
				return true;
			}

			if ((!serviceIds.stream().anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId()))
					|| serviceIds.size() > 1)) {
				return false;
			}

		}
		if ((regServiceDto.getServiceType().contains(ServiceEnum.THEFTINTIMATION)
				|| regServiceDto.getServiceType().contains(ServiceEnum.VEHICLESTOPPAGE)
				|| regServiceDto.getServiceType().contains(ServiceEnum.VEHICLESTOPPAGEREVOKATION))
				&& regServiceDto.getApplicationStatus().toString()
						.equals(StatusRegistration.REJECTED.getDescription())) {
			return true;
		}
		if (regServiceDto.getServiceType().contains(ServiceEnum.OBJECTION)
				&& regServiceDto.getApplicationStatus().equals(StatusRegistration.AOREJECTED)) {
			return true;
		}
		if (regServiceDto.getServiceIds() != null
				&& regServiceDto.getServiceIds().contains(ServiceEnum.SHOWCAUSENO.getId())) {
			return true;
		}
		if (regServiceDto.getServiceIds() != null
				&& regServiceDto.getServiceIds().contains(ServiceEnum.RCFORFINANCE.getId())
				&& regServiceDto.getApplicationStatus() != null
				&& ((regServiceDto.getApplicationStatus().equals(StatusRegistration.FRESHRCREJECTED))
						|| (regServiceDto.getApplicationStatus().equals(StatusRegistration.REJECTED)
								&& regServiceDto.getFlowId() != null
								&& regServiceDto.getFlowId().equals(ServiceEnum.Flow.RCFORFINANCEMVIACTION))
						|| (regServiceDto.getApplicationStatus().equals(StatusRegistration.CANCELED)))) {
			return true;
		}
		return listOfStatus.stream().anyMatch(status -> status.equals(regServiceDto.getApplicationStatus()));
	}

	@Override
	public Optional<RegServiceVO> findRegistrationDetailsByApplicationNo(String applicationNo) {
		Optional<RegServiceDTO> regOptional = regServiceDAO.findByApplicationNo(applicationNo);
		if (regOptional.isPresent()) {
			RegServiceDTO regDTO = regOptional.get();
			RegServiceVO regVO = regServiceMapper.limiteddtoToVo(regDTO);
			return Optional.of(regVO);
		}
		return Optional.empty();
	}

	@Override
	public Optional<RegServiceDTO> findByApplicationNo(String ApplicationNo) {
		Optional<RegServiceDTO> regOptional = regServiceDAO.findByApplicationNo(ApplicationNo);// AP0050002233018
		if (regOptional.isPresent()) {
			RegServiceDTO regDTO = regOptional.get();

			return Optional.of(regDTO);
		}
		return Optional.empty();
	}

	@Override
	public RegServiceVO findapplication(String ApplicationNo) {
		Optional<RegServiceDTO> regOptional = this.findByApplicationNo(ApplicationNo);
		if (!regOptional.isPresent()) {
			logger.error("No record found. [{}],[{}] ", ApplicationNo);
			throw new BadRequestException("No record found with this Application number" + ApplicationNo);
		}

		return regServiceMapper.convertEntity(regOptional.get());
	}

	@Override
	public RegServiceVO findByRegPrNo(String prNo, String user) {

		/*
		 * if (!userDetails.get().getAadharNo().equalsIgnoreCase(aadharNo)) {
		 * logger.error("you are not authorized to access this application "); throw new
		 * BadRequestException("you are not authorized to access this application"); }
		 */
		RegServiceDTO regDTO = this.returnLatestFcDoc(prNo);
		if (!LocalDate.now().equals(regDTO.getSlotDetails().getSlotDate()) && !regDTO.getRegistrationDetails()
				.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.EIBT.getCovCode())) {
			logger.error("today is not Slot Booked Date");
			throw new BadRequestException("today is not Slot Booked Date");
		}
		commonValidationForFc(user, regDTO);
		if (!isApprovedRTO(regDTO)) {
			logger.error("Approval Pending at RTO.Pr no:", regDTO.getPrNo());
			throw new BadRequestException("Approval Pending From RTO.Pr no: " + regDTO.getPrNo());

		}

		RegServiceVO vo = regServiceMapper.convertEntity(regDTO);
		if (isallowImagesInapp(regDTO.getMviOfficeCode())) {
			vo.setAllowFcImagesInApp(Boolean.TRUE);
		}
		Optional<FcDetailsDTO> fcDoc = fcDetailsDAO.findByStatusIsTrueAndPrNoOrderByCreatedDateDesc(prNo);
		if (fcDoc != null && fcDoc.isPresent() && fcDoc.get().getFcValidUpto() != null) {
			if (vo.getRegistrationDetails() != null && vo.getRegistrationDetails().getRegistrationValidity() != null) {
				vo.getRegistrationDetails().getRegistrationValidity().setFcValidity(fcDoc.get().getFcValidUpto());
			}
		}
		vo.setIsFor1Years(Boolean.TRUE);
		if (regDTO.getRegistrationDetails().getRegistrationValidity().getFcValidity() == null) {
			vo.setIsFor2Years(Boolean.TRUE);
		}
		Optional<PropertiesDTO> optionalPropertie = propertiesDAO.findByModule(ModuleEnum.FC.toString());
		if (optionalPropertie.isPresent()) {
			if (optionalPropertie.get().getListOfficesForGati().stream()
					.anyMatch(office -> office.equalsIgnoreCase(regDTO.getMviOfficeCode()))) {
				vo.setIsFor6Months(Boolean.TRUE);
			}
		}

		return vo;
	}

	@Override
	public void commonValidationForFc(String user, RegServiceDTO regDTO) {
		Optional<UserDTO> userDetails = userDAO.findByUserId(user);
		if (!userDetails.isPresent()) {
			logger.error("user details not found. [{}] ", user);
			throw new BadRequestException("No record found. " + user);
		}
		if (!((regDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWFC.getId()))
				|| regDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RENEWALFC.getId()))
				|| regDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.OTHERSTATIONFC.getId())))
				&& regDTO.getServiceIds().size() == 1)) {
			logger.error("Application not belong to fitness service.");
			throw new BadRequestException("Application not belong to fitness service.");
		}
		if (!userDetails.get().getOffice().getOfficeCode().equalsIgnoreCase(regDTO.getMviOfficeCode())) {
			logger.error("No Authorization for this office Code", regDTO.getMviOfficeCode());
			if (regDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.OTHERSTATIONFC.getId()))) {
				logger.error("This PR is applied for OtherStationFC [{}]", regDTO.getMviOfficeCode());
				throw new BadRequestException("This PR is applied for OtherStationFC " + regDTO.getMviOfficeCode());
			} else {
				logger.error("No Authorization for this office Code [{}]", regDTO.getMviOfficeCode());
				throw new BadRequestException("No Authorization for this office Code" + regDTO.getMviOfficeCode());
			}
		}

		if (regDTO.getApplicationStatus().equals(StatusRegistration.APPROVED)
				|| regDTO.getApplicationStatus().equals(StatusRegistration.REJECTED)
				|| regDTO.getApplicationStatus().equals(StatusRegistration.MVIREJECTED)) {

			logger.error("Application process completed.Pr no:", regDTO.getPrNo(), " and Application status is: ",
					regDTO.getApplicationStatus());
			throw new BadRequestException("Application process completed.Pr no: " + regDTO.getPrNo()
					+ " and Application status is: " + regDTO.getApplicationStatus());
		}
		if (regDTO.getApplicationStatus().equals(StatusRegistration.CITIZENPAYMENTFAILED)
				|| regDTO.getApplicationStatus().equals(StatusRegistration.PAYMENTPENDING)
				|| regDTO.getApplicationStatus().equals(StatusRegistration.PAYMENTFAILED)) {
			logger.error("Please pay the fee.Pr no:", regDTO.getPrNo(), " and Application status is: ",
					regDTO.getApplicationStatus());
			throw new BadRequestException("Please pay the fee.Pr no: " + regDTO.getPrNo()
					+ " and Application status is: " + regDTO.getApplicationStatus());
		}
	}

	@Override
	public boolean isallowImagesInapp(String officeCode) {

		Optional<PropertiesDTO> imagesInput = propertiesDAO.findByAllowFcImagesTrue();
		if (imagesInput.get().getOfficeCodes().contains(officeCode)) {
			return true;
		}
		return false;
	}

	private boolean isApprovedRTO(RegServiceDTO regDTO) {
		Optional<ActionDetails> actionDetailsOpt = regDTO.getActionDetails().stream()
				.filter(p -> RoleEnum.RTO.getName().equals(p.getRole())).findFirst();
		if (actionDetailsOpt.isPresent() && !actionDetailsOpt.get().getIsDoneProcess()) {
			return Boolean.FALSE;
		}
		return Boolean.TRUE;
	}

	@Override
	public RegistrationDetailsDTO getRegDetails(String prNo, String aadharNo, Boolean value) {

		Optional<RegistrationDetailsDTO> registrationOptional = registrationDetailDAO.findByPrNo(prNo);

		if (!registrationOptional.isPresent()) {
			logger.error("No record found. [{}],[{}] ", prNo);
			throw new BadRequestException("No record found.Pr no: " + prNo);
		}
		if (value) {
			if (aadharNo == null || registrationOptional.get().getApplicantDetails() == null
					|| !aadharNo.equals(registrationOptional.get().getApplicantDetails().getAadharNo())) {
				throw new BadRequestException(
						"No  Application Details found with PR No: " + prNo + " and aadhaar No: " + aadharNo);
			}
		}
		return registrationOptional.get();

	}

	@Override
	public RegServiceVO getRegDetailsForToken(String ApplicationNo) {
		// TODO Auto-generated method stub
		Optional<RegServiceDTO> regOptional = this.findByApplicationNo(ApplicationNo);
		if (regOptional.isPresent()) {
			if (regOptional.get().getServiceIds().stream()
					.anyMatch(id -> id.equals(ServiceEnum.BILLATERALTAX.getId()))) {
				regOptional.get().setFeeDetails(this.getpaymentsForToken(ApplicationNo));

			} else {

				if (!(regOptional.get().getServiceIds().stream()
						.anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId())
								&& regOptional.get().getApplicationStatus().equals(StatusRegistration.SELLERCOMPLETED)
								|| regOptional.get().getApplicationStatus()
										.equals(StatusRegistration.TOWITHHPTINITIATED)))) {
					if (!regOptional.get().getServiceIds().stream()
							.anyMatch(id -> id.equals(ServiceEnum.THEFTINTIMATION.getId()))
							&& !regOptional.get().getServiceIds().stream()
									.anyMatch(id -> id.equals(ServiceEnum.RCCANCELLATION.getId()))
							&& !regOptional.get().getServiceIds().stream()
									.anyMatch(id -> id.equals(ServiceEnum.THEFTREVOCATION.getId()))
							&& !regOptional.get().getServiceIds().stream()
									.anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))
							&& !regOptional.get().getServiceIds().stream()
									.anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId()))
							&& !regOptional.get().getServiceIds().stream()
									.anyMatch(id -> id.equals(ServiceEnum.PERDATAENTRY.getId()))
							&& !(regOptional.get().getServiceIds().stream()
									.anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))
									&& regOptional.get().getApplicationStatus()
											.equals(StatusRegistration.CITIZENSUBMITTED))
							&& !(regOptional.get().getServiceIds().stream()
									.anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))
									&& regOptional.get().getApplicationStatus()
											.equals(StatusRegistration.SLOTBOOKED))) {
						if (regOptional.get().getRegistrationDetails() != null
								&& !(StringUtils.isNoneEmpty(regOptional.get().getRegistrationDetails().getPrNo())
										&& regOptional.get().getRegistrationDetails().isTaxPaidByVcr())) {
							if (regOptional.get().getRegistrationDetails().getApplicantType() == null || !regOptional
									.get().getRegistrationDetails().getApplicantType().equals("WITHINTHESTATE"))
								regOptional.get().setFeeDetails(this.getpaymentsForToken(ApplicationNo));

						} else {
							regOptional.get().setFeeDetails(this.getpaymentsForToken(ApplicationNo));
						}
					}
				}
			}

			return regServiceMapper.convertEntity(regOptional.get());
		} else {
			logger.error("No record found. [{}],[{}] ", ApplicationNo);
			throw new BadRequestException("No record found.Application no: " + ApplicationNo);
		}
	}

	@Override
	public TransactionDetailVO getPaymentDetails(RegServiceVO regServiceDetail, Boolean isToPay, String slotDate) {

		TransactionDetailVO transactionDetailVO = new TransactionDetailVO();
		String weightDetails = null;
		ClassOfVehiclesVO covDetails = null;
		boolean isCalculateFc = false;
		if(regServiceDetail.getSpecificVcrPayment()!=null)
		{
			transactionDetailVO.setSpecificVcrPayment(regServiceDetail.getSpecificVcrPayment());
		}
		if (isToPay) {
			GatewayTypeEnum payGatewayTypeEnum = GatewayTypeEnum
					.getGatewayTypeEnumByDesc(regServiceDetail.getGatewayType());
			transactionDetailVO.setGatewayTypeEnum(payGatewayTypeEnum);
		}

		transactionDetailVO.setServiceEnumList(regServiceDetail.getServiceType());

		if (regServiceDetail.getServiceType() != null && regServiceDetail.getServiceType().stream()
				.anyMatch(type -> type.equals(ServiceEnum.BILLATERALTAX))) {
			transactionDetailVO
					.setGatewayType(GatewayTypeEnum.getGatewayTypeEnumByDesc(regServiceDetail.getGatewayType()));
			transactionDetailVO.setFormNumber(regServiceDetail.getApplicationNo());
			transactionDetailVO.setCovs(Arrays.asList(
					covService.findByCovdescription(regServiceDetail.getBileteralTaxDetails().getClassOfVehicle())));
			transactionDetailVO.setPhone(regServiceDetail.getBileteralTaxDetails().getContactNo());
			transactionDetailVO.setEmail(StringUtils.EMPTY);
			transactionDetailVO.setModule(ModuleEnum.CITIZEN.getCode());
			transactionDetailVO.setFirstName(regServiceDetail.getBileteralTaxDetails().getOwnerName());
			transactionDetailVO
					.setTxnid(getTransactionNumber(transactionDetailVO, regServiceDetail.getApplicationNo()));
			transactionDetailVO.setPaymentTransactionNo(regServiceDetail.getPaymentTransactionNo());
			transactionDetailVO.setServiceId(regServiceDetail.getServiceIds());
			transactionDetailVO.setPurpose(regServiceDetail.getBileteralTaxDetails().getPurpose());
			transactionDetailVO.setOfficeCode(regServiceDetail.getOfficeCode());
		} else if (regServiceDetail.getServiceType() != null
				&& regServiceDetail.getServiceType().stream().anyMatch(type -> type.equals(ServiceEnum.VCR))) {

			transactionDetailVO
					.setGatewayType(GatewayTypeEnum.getGatewayTypeEnumByDesc(regServiceDetail.getGatewayType()));
			transactionDetailVO.setFormNumber(regServiceDetail.getApplicationNo());
			transactionDetailVO.setCovs(Arrays
					.asList(covService.findByCovCode(regServiceDetail.getRegistrationDetails().getClassOfVehicle())));
			transactionDetailVO.setPhone(StringUtils.EMPTY);
			transactionDetailVO.setEmail(StringUtils.EMPTY);
			transactionDetailVO.setModule(ModuleEnum.CITIZEN.getCode());
			transactionDetailVO
					.setFirstName(regServiceDetail.getRegistrationDetails().getApplicantDetails().getFirstName());
			transactionDetailVO
					.setTxnid(getTransactionNumber(transactionDetailVO, regServiceDetail.getApplicationNo()));
			transactionDetailVO.setPaymentTransactionNo(regServiceDetail.getPaymentTransactionNo());
			transactionDetailVO.setServiceId(regServiceDetail.getServiceIds());
			transactionDetailVO.setListOfVcrs(regServiceDetail.getVcrNosList());
			transactionDetailVO.setOfficeCode(regServiceDetail.getOfficeCode());
			

			if (isToPay) {
				transactionDetailVO.setRequestToPay(Boolean.TRUE);
			}

		} else if (regServiceDetail.getServiceType() != null
				&& regServiceDetail.getServiceType().stream().anyMatch(type -> type.equals(ServiceEnum.VOLUNTARYTAX))) {

			transactionDetailVO
					.setGatewayType(GatewayTypeEnum.getGatewayTypeEnumByDesc(regServiceDetail.getGatewayType()));
			transactionDetailVO.setFormNumber(regServiceDetail.getApplicationNo());
			transactionDetailVO.setCovs(Arrays
					.asList(covService.findByCovCode(regServiceDetail.getRegistrationDetails().getClassOfVehicle())));
			transactionDetailVO.setPhone(StringUtils.EMPTY);
			transactionDetailVO.setEmail(StringUtils.EMPTY);
			transactionDetailVO.setModule(ModuleEnum.CITIZEN.getCode());
			transactionDetailVO
					.setFirstName(regServiceDetail.getRegistrationDetails().getApplicantDetails().getFirstName());
			transactionDetailVO
					.setTxnid(getTransactionNumber(transactionDetailVO, regServiceDetail.getApplicationNo()));
			transactionDetailVO.setPaymentTransactionNo(regServiceDetail.getPaymentTransactionNo());
			transactionDetailVO.setServiceId(regServiceDetail.getServiceIds());
			// transactionDetailVO.setListOfVcrs(regServiceDetail.getVcrNosList());
			RegistrationVcrVo voluntyInputs = new RegistrationVcrVo();
			voluntyInputs.setRegNo(regServiceDetail.getPrNo());
			voluntyInputs.setTrNo(regServiceDetail.getRegistrationDetails().getTrNo());
			voluntyInputs.setCov(regServiceDetail.getRegistrationDetails().getClassOfVehicle());
			voluntyInputs.setGvwc(regServiceDetail.getRegistrationDetails().getVahanDetails().getGvw());
			voluntyInputs.setUlw(regServiceDetail.getRegistrationDetails().getVahanDetails().getUnladenWeight());
			voluntyInputs.setSeats(regServiceDetail.getRegistrationDetails().getVahanDetails().getSeatingCapacity());
			voluntyInputs.setMakersModel(regServiceDetail.getRegistrationDetails().getVahanDetails().getMakersModel());
			voluntyInputs
					.setInvoiceValue(regServiceDetail.getRegistrationDetails().getInvoiceDetails().getInvoiceValue());
			voluntyInputs.setFuelDesc(regServiceDetail.getRegistrationDetails().getVahanDetails().getFuelDesc());
			voluntyInputs.setNocIssued(regServiceDetail.getVoluntaryTaxDetails().isNocIssued());
			voluntyInputs.setWithTP(regServiceDetail.getVoluntaryTaxDetails().isWithTP());
			voluntyInputs.setVehicleHaveAitp(regServiceDetail.getVoluntaryTaxDetails().isVehicleHaveAitp());
			if (regServiceDetail.getnOCDetails() != null && regServiceDetail.getnOCDetails().getIssueDate() != null) {
				voluntyInputs.setNocDate(regServiceDetail.getnOCDetails().getIssueDate());
			}

			if (regServiceDetail.getRegistrationDetails().getOwnerType() != null) {
				voluntyInputs.setOwnerType(regServiceDetail.getRegistrationDetails().getOwnerType());
			}

			if (regServiceDetail != null && regServiceDetail.getRegistrationDetails() != null
					&& regServiceDetail.getRegistrationDetails().getPrGeneratedDate() != null) {
				voluntyInputs.setPrGeneratedDate(
						regServiceDetail.getRegistrationDetails().getPrGeneratedDate().toLocalDate());
			}

			if (StringUtils.isNoneBlank(regServiceDetail.getRegistrationDetails().getApplicantType())) {
				if (regServiceDetail.getRegistrationDetails().isRegVehicleWithPR()) {
					voluntyInputs.setOtherStateRegister(true);
				} else {
					voluntyInputs.setOtherStateUnregister(true);
				}
			} else {
				voluntyInputs.setUnregisteredVehicle(true);
			}
			if (regServiceDetail.getAlterationVO() != null
					&& regServiceDetail.getAlterationVO().getDateOfCompletion() != null) {
				voluntyInputs.setDateOfCompletion(regServiceDetail.getAlterationVO().getDateOfCompletion());
			}
			// voluntyInputs.setDateOfCompletion(regServiceDetail.getDateOfCompletion());
			voluntyInputs.setFirstVehicle(regServiceDetail.getRegistrationDetails().getIsFirstVehicle());
			transactionDetailVO.setOfficeCode(regServiceDetail.getOfficeCode());
			voluntyInputs.setTaxType(regServiceDetail.getVoluntaryTaxDetails().getTaxType());
			voluntyInputs.setFirstVehicle(regServiceDetail.getRegistrationDetails().getIsFirstVehicle());
			voluntyInputs.setFcValidity(regServiceDetail.getVoluntaryTaxDetails().getFcValidity());
			transactionDetailVO.setInput(voluntyInputs);
			if (isToPay) {
				transactionDetailVO.setRequestToPay(Boolean.TRUE);
			}

		} else if (regServiceDetail.getServiceType() != null && regServiceDetail.getServiceType().stream()
				.anyMatch(type -> type.equals(ServiceEnum.OTHERSTATETEMPORARYPERMIT)
						|| type.equals(ServiceEnum.OTHERSTATESPECIALPERMIT))) {
			transactionDetailVO
					.setGatewayType(GatewayTypeEnum.getGatewayTypeEnumByDesc(regServiceDetail.getGatewayType()));
			transactionDetailVO.setFormNumber(regServiceDetail.getApplicationNo());
			transactionDetailVO.setCovs(Arrays.asList(covService.findByCovCode(
					regServiceDetail.getOtherStateTemporaryPermit().getVehicleDetails().getClassOfVehicle())));
			transactionDetailVO.setPhone(StringUtils.EMPTY);
			transactionDetailVO.setEmail(StringUtils.EMPTY);
			transactionDetailVO.setModule(ModuleEnum.CITIZEN.getCode());
			transactionDetailVO.setFirstName(
					regServiceDetail.getOtherStateTemporaryPermit().getApplicantDetails().getDisplayName());
			transactionDetailVO
					.setTxnid(getTransactionNumber(transactionDetailVO, regServiceDetail.getApplicationNo()));
			transactionDetailVO.setPaymentTransactionNo(regServiceDetail.getPaymentTransactionNo());
			transactionDetailVO.setServiceId(regServiceDetail.getServiceIds());
			transactionDetailVO.setSeatingCapacity(
					regServiceDetail.getOtherStateTemporaryPermit().getVehicleDetails().getSeatingCapacity());
			// transactionDetailVO.setPurpose(regServiceDetail.getBileteralTaxDetails().getPurpose());
			transactionDetailVO.setWeightType(covService.getWeightTypeDetails(
					regServiceDetail.getOtherStateTemporaryPermit().getVehicleDetails().getRlw()));
			transactionDetailVO.setPermitType(regServiceDetail.getOtherStateTemporaryPermit()
					.getTemporaryPermitDetails().getPermitType().getPermitType());
			transactionDetailVO.setOfficeCode(regServiceDetail.getOfficeCode());

			RegistrationVcrVo voluntyInputs = new RegistrationVcrVo();
			voluntyInputs.setRegNo(regServiceDetail.getPrNo());
			voluntyInputs
					.setCov(regServiceDetail.getOtherStateTemporaryPermit().getVehicleDetails().getClassOfVehicle());
			voluntyInputs.setGvwc(regServiceDetail.getOtherStateTemporaryPermit().getVehicleDetails().getRlw());
			voluntyInputs.setUlw(regServiceDetail.getOtherStateTemporaryPermit().getVehicleDetails().getUlw());
			voluntyInputs
					.setSeats(regServiceDetail.getOtherStateTemporaryPermit().getVehicleDetails().getSeatingCapacity());

			// voluntyInputs.setWithTP(regServiceDetail.getVoluntaryTaxDetails().isWithTP());

			/*
			 * if (regServiceDetail != null && regServiceDetail.getRegistrationDetails() !=
			 * null && regServiceDetail.getRegistrationDetails().getPrGeneratedDate() !=
			 * null) { voluntyInputs.setPrGeneratedDate(
			 * regServiceDetail.getRegistrationDetails().getPrGeneratedDate().toLocalDate())
			 * ; }
			 */
			voluntyInputs.setOtherStateRegister(true);
			voluntyInputs.setOtherStateUnregister(false);
			// voluntyInputs.setFirstVehicle(regServiceDetail.getRegistrationDetails().getIsFirstVehicle());
			transactionDetailVO.setOfficeCode(regServiceDetail.getOfficeCode());
			voluntyInputs
					.setFcValidity(regServiceDetail.getOtherStateTemporaryPermit().getFcDetails().getFcValidUpto());
			voluntyInputs.setTaxType(regServiceDetail.getOtherStateTemporaryPermit().getTemporaryPermitDetails()
					.getRouteDetailsVO().getNoOfDays());
			// voluntyInputs.setFirstVehicle(regServiceDetail.getRegistrationDetails().getIsFirstVehicle());
			transactionDetailVO.setInput(voluntyInputs);
			permitValidationsService.permitvalidateAndSetHomeStateOrOtherState(null, regServiceDetail.getPrNo(),
					transactionDetailVO);
			/*
			 * if (isToPay) { transactionDetailVO.setRequestToPay(Boolean.TRUE); }
			 */} /*
					 * else if (regServiceDetail.getServiceType() != null &&
					 * regServiceDetail.getServiceType().stream().anyMatch(type ->
					 * type.equals(ServiceEnum.STAGECARRIAGEREPLACEMENTOFVEHICLE))) {
					 * 
					 * 
					 * 
					 * }
					 */else {
			List<String> errors = new ArrayList<>();
			this.checkVcrDues(registrationDetailsMapper.convertVO(regServiceDetail.getRegistrationDetails()), errors);
			if (!errors.isEmpty()) {
				logger.error("[{}]", errors.get(0));
				throw new BadRequestException(errors.get(0));
			}

			if (regServiceDetail.getServiceType() != null && regServiceDetail.getServiceType().stream()
					.anyMatch(type -> type.equals(ServiceEnum.STAGECARRIAGEREPLACEMENTOFVEHICLE))) {
				transactionDetailVO.setPermitVehiclePrNo(regServiceDetail.getPermitDetailsVO().getPrNo());
			}

			if (StringUtils
					.isNotBlank(regServiceDetail.getRegistrationDetails().getApplicantDetails().getFirstName())) {
				transactionDetailVO
						.setFirstName(regServiceDetail.getRegistrationDetails().getApplicantDetails().getFirstName());
			} else {
				transactionDetailVO
						.setFirstName(regServiceDetail.getRegistrationDetails().getApplicantDetails().getDisplayName());
			}

			if (StringUtils.isNoneBlank(
					regServiceDetail.getRegistrationDetails().getApplicantDetails().getContact().getEmail())) {
				transactionDetailVO.setEmail(
						regServiceDetail.getRegistrationDetails().getApplicantDetails().getContact().getEmail());
			} else if (regServiceDetail.getContactDetails() != null
					&& StringUtils.isNoneBlank(regServiceDetail.getContactDetails().getEmail())) {
				transactionDetailVO.setEmail(regServiceDetail.getContactDetails().getEmail());
			} else {
				transactionDetailVO.setEmail(StringUtils.EMPTY);
			}

			if (StringUtils.isNoneBlank(
					regServiceDetail.getRegistrationDetails().getApplicantDetails().getContact().getPhone())) {
				transactionDetailVO.setPhone(
						regServiceDetail.getRegistrationDetails().getApplicantDetails().getContact().getMobile());
			} else {
				if (regServiceDetail.getContactDetails() != null) {
					transactionDetailVO.setPhone(regServiceDetail.getContactDetails().getMobile());
				} else {
					transactionDetailVO.setPhone(StringUtils.EMPTY);
				}

			}
			if (StringUtils.isNoneBlank(regServiceDetail.getTaxType())) {
				transactionDetailVO.setTaxType(regServiceDetail.getTaxType());
			} else {
				transactionDetailVO.setTaxType(regServiceDetail.getRegistrationDetails().getTaxType());
			}
			transactionDetailVO.setModule(ModuleEnum.CITIZEN.getCode());
			transactionDetailVO.setOwnerType(regServiceDetail.getRegistrationDetails().getOwnerType());
			List<ServiceEnum> serviceIds = regServiceDetail.getServiceType();
			if (regServiceDetail.getServiceIds().stream()
					.anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId()))) {
				if (null != regServiceDetail.getTowDetails() && null != regServiceDetail.getTowDetails().getSeller()
						&& null == regServiceDetail.getTowDetails().getBuyer()
						&& regServiceDetail.getTowDetails().getTransferType() != null
						&& regServiceDetail.getTowDetails().getTransferType().equals(TransferType.SALE)) {
					Collections.replaceAll(serviceIds, ServiceEnum.TRANSFEROFOWNERSHIP, ServiceEnum.TOSELLER);

				} else if (!regServiceDetail.getTowDetails().getTransferType().equals(TransferType.DEATH)) {
					if (regServiceDetail.getTowDetails().getBuyerServices() != null)
						serviceIds = regServiceDetail.getTowDetails().getBuyerServices();
				}

			}

			transactionDetailVO.setServiceEnumList(serviceIds);
			transactionDetailVO.setServiceId(regServiceDetail.getServiceIds());
			transactionDetailVO.setCov(regServiceDetail.getRegistrationDetails().getClassOfVehicle());
			transactionDetailVO.setVehicleType(regServiceDetail.getRegistrationDetails().getVehicleType());
			transactionDetailVO.setFormNumber(regServiceDetail.getApplicationNo());
			transactionDetailVO.setRegApplicationNo(regServiceDetail.getRegistrationDetails().getApplicationNo());
			if (isToPay) {
				transactionDetailVO
						.setGatewayType(GatewayTypeEnum.getGatewayTypeEnumByDesc(regServiceDetail.getGatewayType()));
			}
			if (regServiceDetail.getAlterationVO() != null
					&& regServiceDetail.getAlterationVO().getVehicleTypeTo() != null
					&& regServiceDetail.getAlterationVO().getCov() != null && regServiceDetail.getServiceIds().stream()
							.anyMatch(service -> service.equals(ServiceEnum.ALTERATIONOFVEHICLE.getId()))) {
				if (!regServiceDetail.getServiceIds().stream()
						.anyMatch(service -> service.equals(ServiceEnum.REASSIGNMENT.getId()))) {
					String cov = regServiceDetail.getAlterationVO().getCov() != null
							? regServiceDetail.getAlterationVO().getCov()
							: regServiceDetail.getRegistrationDetails().getClassOfVehicle();
					// covDetails = covService.findByCovCode(cov);
					String vehicleType = regServiceDetail.getAlterationVO().getVehicleTypeTo() != null
							? regServiceDetail.getAlterationVO().getVehicleTypeTo()
							: regServiceDetail.getRegistrationDetails().getVehicleType();
					Optional<ClassOfVehicleConversion> classOfVehicle = classOfVehicleConversionDAO
							.findByNewCovAndNewCategoryAndCovAndCategory(cov, vehicleType,
									regServiceDetail.getRegistrationDetails().getClassOfVehicle(),
									regServiceDetail.getRegistrationDetails().getVehicleType());
					if (classOfVehicle.isPresent()) {
						if (classOfVehicle.get().isFcFee()) {
							isCalculateFc = true;
						}
					}
					if (regServiceDetail.getAlterationVO().getVehicleTypeTo().equalsIgnoreCase(CovCategory.T.getCode())
							&& regServiceDetail.getRegistrationDetails().getVehicleType()
									.equalsIgnoreCase(CovCategory.T.getCode())) {
						transactionDetailVO.setSlotDate(regServiceDetail.getSlotDetails().getTestSlotDate());
						if (StringUtils.isNoneBlank(slotDate)) {
							DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy");
							LocalDate slotDates = LocalDate.parse(slotDate, df);
							transactionDetailVO.setSlotDate(slotDates);
							boolean status = isToPayLateFeeForFC(regServiceDetail.getApplicationNo(), slotDate,
									isToPay);
							if (!status) {
								transactionDetailVO.setToPayLateFeeForFC(Boolean.FALSE);
							}
						}
						if (regServiceDetail.isPaidPyamentsForFC()) {
							isCalculateFc = false;
							transactionDetailVO.getServiceEnumList().clear();
							transactionDetailVO.getServiceEnumList().add(ServiceEnum.FCLATEFEE);
							// transactionDetailVO.setServiceEnumList(Arrays.asList(ServiceEnum.DIFFTAX));
							transactionDetailVO
									.setServiceId(Stream.of(ServiceEnum.FCLATEFEE.getId()).collect(Collectors.toSet()));

						}
					}
				}
				/*
				 * Optional<ClassOfVehicleConversion> classOfVehicle =
				 * classOfVehicleConversionDAO .findByNewCovAndNewCategoryAndCovAndCategory(
				 * regServiceDetail. getAlterationVO ().getCov(),
				 * regServiceDetail.getAlterationVO().getVehicleTypeTo(),
				 * regServiceDetail.getRegistrationDetails().getClassOfVehicle() ,
				 * regServiceDetail.getRegistrationDetails().getVehicleType()); if
				 * (classOfVehicle.isPresent() &&
				 * !regServiceDetail.getAlterationVO().isMVIDone()) { if
				 * (classOfVehicle.get().isFcFee()) { isCalculateFc = true; } }
				 */

				// covDetails =
				// covService.findByCovCode(regServiceDetail.getAlterationVO().getCov());
				List<ServiceEnum> serviceList = new ArrayList<>();
				Iterator<ServiceEnum> it = serviceIds.iterator();
				while (it.hasNext()) {
					ServiceEnum a = it.next();
					if (a.equals(ServiceEnum.REASSIGNMENT))
						continue;
					serviceList.add(a);
					// it.remove();
				}
				transactionDetailVO.setServiceEnumList(serviceList);
				Set<Integer> serviceIdsInt = new TreeSet<>();
				Iterator<Integer> intIds = regServiceDetail.getServiceIds().iterator();
				while (intIds.hasNext()) {
					Integer singleId = intIds.next();
					if (singleId.equals(ServiceEnum.REASSIGNMENT.getId()))
						continue;
					serviceIdsInt.add(singleId);
					// intIds.remove();
				}
				transactionDetailVO.setServiceId(serviceIdsInt);
			}
			if (regServiceDetail.getServiceIds().stream()
					.anyMatch(service -> service.equals(ServiceEnum.ALTERATIONOFVEHICLE.getId())
							&& regServiceDetail.getAlterationVO().isMVIDone())) {
				transactionDetailVO.setModule(ModuleEnum.ALTERVEHICLE.getCode());
				transactionDetailVO.setRtoRejectedIvcn(Boolean.TRUE);
				transactionDetailVO.getServiceEnumList().clear();
				transactionDetailVO.getServiceEnumList().add(ServiceEnum.ALTDIFFTAX);
				// transactionDetailVO.setServiceEnumList(Arrays.asList(ServiceEnum.DIFFTAX));
				transactionDetailVO.setServiceId(Stream.of(ServiceEnum.ALTDIFFTAX.getId()).collect(Collectors.toSet()));
				if (regServiceDetail.getAlterationVO().getVehicleTypeFrom() != null
						&& !(regServiceDetail.getAlterationVO().getVehicleTypeFrom()
								.equalsIgnoreCase(regServiceDetail.getAlterationVO().getVehicleTypeTo()))) {
					List<ServiceEnum> list = new ArrayList<>();
					list.addAll(regServiceDetail.getServiceType());
					transactionDetailVO.getServiceEnumList().clear();
					transactionDetailVO.getServiceEnumList().add(ServiceEnum.REASSIGNMENT);
					transactionDetailVO
							.setServiceId(Stream.of(ServiceEnum.REASSIGNMENT.getId()).collect(Collectors.toSet()));
				}
				if (regServiceDetail.getAlterationVO() != null
						&& StringUtils.isNoneBlank(regServiceDetail.getAlterationVO().getCov())) {
					covDetails = covService.findByCovCode(regServiceDetail.getAlterationVO().getCov());
				}
				Integer ulw = regServiceDetail.getAlterationVO().getUlw() != null
						? regServiceDetail.getAlterationVO().getUlw()
						: regServiceDetail.getRegistrationDetails().getVehicleDetails().getUlw();
				regServiceDetail.getRegistrationDetails().getVehicleDetails().setUlw(ulw);
				transactionDetailVO.setRegApplicationNo(regServiceDetail.getRegistrationDetails().getApplicationNo());

			}
			if (regServiceDetail.getServiceType().stream()
					.anyMatch(type -> type.equals(ServiceEnum.ALTERATIONOFVEHICLE))) {
				if (regServiceDetail.getAlterationVO().getAlterationService().stream()
						.anyMatch(type -> type.equals(AlterationTypeEnum.WEIGHT))) {
					Optional<RegistrationDetailsDTO> optionalReg = registrationDetailDAO
							.findByPrNo(regServiceDetail.getPrNo());
					if (!optionalReg.isPresent()) {
						logger.error("no records found for pr no: " + regServiceDetail.getPrNo());
						throw new BadRequestException("no records found for pr no: " + regServiceDetail.getPrNo());
					}
					this.validationForWeightAlt(optionalReg.get());
					transactionDetailVO.setIsWeightAlt(Boolean.TRUE);
				}
			}
			if (regServiceDetail.getServiceType().stream().anyMatch(type -> type.equals(ServiceEnum.TAXATION))) {
				if (regServiceDetail.isWeightAlt()) {

					transactionDetailVO.setIsWeightAlt(Boolean.TRUE);
				}
			}
			weightDetails = getWeight(regServiceDetail);

			if (covDetails == null) {
				if (!StringUtils.isEmpty(regServiceDetail.getRegistrationDetails().getClassOfVehicle())) {
					covDetails = covService
							.findByCovCode(regServiceDetail.getRegistrationDetails().getClassOfVehicle());
				} else {
					covDetails = covService.findByCovdescription(
							regServiceDetail.getRegistrationDetails().getClassOfVehicleDesc().toUpperCase());
				}
			}

			if (regServiceDetail.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))
					&& regServiceDetail.getRegistrationDetails().isDataInsertedByDataEntry()) {
				OtherStateApplictionType applicationType = citizenTaxService
						.getOtherStateVehicleStatus(regServiceDetail);
				if (regServiceDetail.getRegistrationDetails().getApplicantType() != null && regServiceDetail
						.getRegistrationDetails().getApplicantType().equalsIgnoreCase("WITHINTHESTATE")) {
					transactionDetailVO.getServiceEnumList().clear();
					transactionDetailVO.getServiceEnumList().add(ServiceEnum.FR);
					transactionDetailVO.setServiceId(Stream.of(ServiceEnum.FR.getId()).collect(Collectors.toSet()));
					if (regServiceDetail.getRegistrationDetails().getVehicleType()
							.equalsIgnoreCase(CovCategory.T.getCode())) {
						transactionDetailVO.getServiceEnumList().add(ServiceEnum.NEWFC);
						transactionDetailVO
								.setServiceId(Stream.of(ServiceEnum.NEWFC.getId()).collect(Collectors.toSet()));
						// serviceEnums.add(ServiceEnum.NEWFC);
					}
				} else if (regServiceDetail.getRegistrationDetails().getIsFirstVehicle() != null
						&& !regServiceDetail.getRegistrationDetails().getIsFirstVehicle()
						&& regServiceDetail.getOsSecondVechicleFoundRTO() != null
						&& regServiceDetail.getOsSecondVechicleFoundRTO()) {
					transactionDetailVO.getServiceEnumList().clear();
					transactionDetailVO.getServiceEnumList().add(ServiceEnum.DATAENTRY);
					transactionDetailVO
							.setServiceId(Stream.of(ServiceEnum.DATAENTRY.getId()).collect(Collectors.toSet()));
				}

				else if (OtherStateApplictionType.ApplicationNO.equals(applicationType)) {
					// serviceEnums.add(ServiceEnum.FR);
					// serviceEnums.add(ServiceEnum.TEMPORARYREGISTRATION);
					transactionDetailVO.getServiceEnumList().clear();
					transactionDetailVO.getServiceEnumList().add(ServiceEnum.FR);
					transactionDetailVO.getServiceEnumList().add(ServiceEnum.TEMPORARYREGISTRATION);
					transactionDetailVO.setServiceId(Stream.of(ServiceEnum.FR.getId()).collect(Collectors.toSet()));
					transactionDetailVO.setServiceId(
							Stream.of(ServiceEnum.TEMPORARYREGISTRATION.getId()).collect(Collectors.toSet()));
					if (regServiceDetail.getRegistrationDetails().getVehicleType()
							.equalsIgnoreCase(CovCategory.T.getCode())) {
						transactionDetailVO.getServiceEnumList().add(ServiceEnum.NEWFC);
						transactionDetailVO
								.setServiceId(Stream.of(ServiceEnum.NEWFC.getId()).collect(Collectors.toSet()));
						// serviceEnums.add(ServiceEnum.NEWFC);
					}
				} else if (OtherStateApplictionType.TrNo.equals(applicationType)) {
					// serviceEnums.add(ServiceEnum.FR);
					if ((regServiceDetail.getRegistrationDetails().getClassOfVehicle()
							.equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
							|| regServiceDetail.getRegistrationDetails().getClassOfVehicle()
									.equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode()))
							&& regServiceDetail.isMviDone()) {
						AlterationVO vo = alterVehicleDetails(regServiceDetail.getApplicationNo());
						covDetails = covService.findByCovCode(vo.getCov());
						transactionDetailVO.setModule(ModuleEnum.ALTERVEHICLE.getCode());
					}
					transactionDetailVO.getServiceEnumList().add(ServiceEnum.FR);
					transactionDetailVO.setServiceId(Stream.of(ServiceEnum.FR.getId()).collect(Collectors.toSet()));
					if (regServiceDetail.getRegistrationDetails().getVehicleType()
							.equalsIgnoreCase(CovCategory.T.getCode())) {
						// serviceEnums.add(ServiceEnum.NEWFC);
						transactionDetailVO.getServiceEnumList().add(ServiceEnum.NEWFC);
						transactionDetailVO
								.setServiceId(Stream.of(ServiceEnum.NEWFC.getId()).collect(Collectors.toSet()));
					}
				}
			}
			if ((regServiceDetail.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWFC.getId()))
					|| regServiceDetail.getServiceIds().stream()
							.anyMatch(id -> id.equals(ServiceEnum.RENEWALFC.getId()))
					|| regServiceDetail.getServiceIds().stream()
							.anyMatch(id -> id.equals(ServiceEnum.OTHERSTATIONFC.getId())))
					&& regServiceDetail.getServiceIds().size() == 1) {
				if (regServiceDetail.getSlotDetails() == null
						|| regServiceDetail.getSlotDetails().getTestSlotDate() == null) {
					logger.error("slot deatsil missing in: " + regServiceDetail.getApplicationNo());
					throw new BadRequestException("slot deatsil missing in: " + regServiceDetail.getApplicationNo());
				}
				if (regServiceDetail.isPaidPyamentsForFC()) {
					transactionDetailVO.getServiceEnumList().clear();
					transactionDetailVO.getServiceEnumList().add(ServiceEnum.FCLATEFEE);
					// transactionDetailVO.setServiceEnumList(Arrays.asList(ServiceEnum.DIFFTAX));
					transactionDetailVO
							.setServiceId(Stream.of(ServiceEnum.FCLATEFEE.getId()).collect(Collectors.toSet()));
				}
				transactionDetailVO.setSlotDate(regServiceDetail.getSlotDetails().getTestSlotDate());
				if (StringUtils.isNoneBlank(slotDate)) {
					DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy");
					LocalDate slotDates = LocalDate.parse(slotDate, df);
					transactionDetailVO.setSlotDate(slotDates);
					boolean status = isToPayLateFeeForFC(regServiceDetail.getApplicationNo(), slotDate, isToPay);
					if (!status) {
						transactionDetailVO.setToPayLateFeeForFC(Boolean.FALSE);
					}
				}
			}
			if ((regServiceDetail.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RENEWAL.getId())))) {
				if (regServiceDetail.getSlotDetails() == null
						|| regServiceDetail.getSlotDetails().getTestSlotDate() == null) {
					logger.error("slot deatsil missing in: " + regServiceDetail.getApplicationNo());
					throw new BadRequestException("slot deatsil missing in: " + regServiceDetail.getApplicationNo());
				}
				if (regServiceDetail.isPaidPyamentsForRenewal()) {
					transactionDetailVO.getServiceEnumList().clear();
					transactionDetailVO.setServiceEnumList(Arrays.asList(ServiceEnum.RENEWALLATEFEE));
					// transactionDetailVO.setServiceEnumList(Arrays.asList(ServiceEnum.DIFFTAX));
					transactionDetailVO
							.setServiceId(Stream.of(ServiceEnum.RENEWALLATEFEE.getId()).collect(Collectors.toSet()));
				}
				transactionDetailVO.setSlotDate(regServiceDetail.getSlotDetails().getTestSlotDate());
				if (StringUtils.isNoneBlank(slotDate)) {
					DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy");
					LocalDate slotDates = LocalDate.parse(slotDate, df);
					transactionDetailVO.setSlotDate(slotDates);
					boolean status = isToPayLateFeeForRenewal(regServiceDetail.getApplicationNo(), slotDate);
					if (!status) {
						transactionDetailVO.setToPayLateFeeForRenewal(Boolean.FALSE);
					}
				}

			}
			if (regServiceDetail.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))
					&& regServiceDetail.getRegistrationDetails().isDataInsertedByDataEntry()) {
				transactionDetailVO
						.setOtherState(regServiceDetail.getRegistrationDetails().isDataInsertedByDataEntry());
			} else {
				transactionDetailVO.setOtherState(false);
			}
			transactionDetailVO.setCalculateFc(isCalculateFc);
			transactionDetailVO.setWeightType(weightDetails);
			transactionDetailVO.setCovs(Arrays.asList(covDetails));
			transactionDetailVO.setOfficeCode(regServiceDetail.getOfficeCode());
			if (isToPay) {
				transactionDetailVO.setRequestToPay(Boolean.TRUE);
			}
			transactionDetailVO.setPaymentTransactionNo(regServiceDetail.getPaymentTransactionNo());
			if (regServiceDetail.getPermitDetailsVO() != null) {
				transactionDetailVO
						.setPermitType(regServiceDetail.getPermitDetailsVO().getPermitType().getPermitType());
				if (regServiceDetail.getPermitDetailsVO().getRouteDetailsVO() != null
						&& regServiceDetail.getPermitDetailsVO().getRouteDetailsVO().getRouteType() != null
						&& regServiceDetail.getPermitDetailsVO().getRouteDetailsVO().getRouteType()
								.getRouteCode() != null)
					transactionDetailVO.setRouteCode(
							regServiceDetail.getPermitDetailsVO().getRouteDetailsVO().getRouteType().getRouteCode());
			}
			if (isToPay) {
				transactionDetailVO
						.setTxnid(getTransactionNumber(transactionDetailVO, regServiceDetail.getApplicationNo()));
			}
			if (regServiceDetail.getRegistrationDetails().getVahanDetails() != null) {
				transactionDetailVO.setSeatingCapacity(
						regServiceDetail.getRegistrationDetails().getVahanDetails().getSeatingCapacity());
			}

			if (regServiceDetail.getServiceIds().stream()
					.anyMatch(val -> ServiceEnum.getPermitRelatedServiceIds().contains(val))
					&& StringUtils.isBlank(transactionDetailVO.getPermitType())
					&& StringUtils.isNotBlank(regServiceDetail.getPrNo())) {
				Optional<PermitDetailsDTO> permitDetails = permitDetailsDAO
						.findByPrNoAndPermitStatusAndPermitTypeTypeofPermitOrderByCreatedDateDesc(
								regServiceDetail.getPrNo(), PermitsEnum.ACTIVE.getDescription(),
								PermitType.PRIMARY.getPermitTypeCode());

				if (permitDetails.isPresent()) {
					transactionDetailVO.setPermitType(permitDetails.get().getPermitType().getPermitType());
				}

			}
		}
		
		
		
		return transactionDetailVO;

	}

	private String getWeight(RegServiceVO regServiceDetail) {
		String weightDetails;
		/*
		 * if (regServiceDetail.getServiceIds().stream() .anyMatch(service ->
		 * service.equals(ServiceEnum.ALTERATIONOFVEHICLE.getId()))) { if
		 * ((regServiceDetail.getAlterationVO().getVehicleTypeTo() != null &&
		 * regServiceDetail.getAlterationVO()
		 * .getVehicleTypeTo().equalsIgnoreCase(CovCategory.T.getCode()))) {
		 *
		 * weightDetails = covService
		 * .getWeightTypeDetails(regServiceDetail.getRegistrationDetails().
		 * getVehicleDetails().getRlw()); } else if
		 * ((regServiceDetail.getAlterationVO().getVehicleTypeTo() != null &&
		 * regServiceDetail
		 * .getAlterationVO().getVehicleTypeTo().equalsIgnoreCase(CovCategory.N.
		 * getCode( )))) { weightDetails = getUlwWeghtForAltVehicle(regServiceDetail); }
		 * else { if (regServiceDetail.getRegistrationDetails().getVehicleType()
		 * .equalsIgnoreCase(CovCategory.T.getCode())) {
		 *
		 * weightDetails = covService.getWeightTypeDetails(
		 * regServiceDetail.getRegistrationDetails().getVehicleDetails().getRlw( )); }
		 * else { weightDetails = getUlwWeghtForAltVehicle(regServiceDetail); } }
		 *
		 * } else {
		 */

		if (regServiceDetail.getRegistrationDetails().getVehicleType().equalsIgnoreCase(CovCategory.T.getCode())) {

			weightDetails = covService.getWeightTypeDetails(citizenTaxService.getGvwWeightForCitizen(
					registrationDetailsMapper.convertVOForOtherState(regServiceDetail.getRegistrationDetails())));
		} else {
			weightDetails = covService.getWeightTypeDetails(citizenTaxService.getUlwWeight(
					registrationDetailsMapper.convertVOForOtherState(regServiceDetail.getRegistrationDetails())));
		}
		/* } */
		return weightDetails;
	}

	@Override
	public ClassOfVehiclesVO getClassofVehicle(RegServiceDTO regServiceDetail, RegistrationDetailsDTO regDetails) {

		ClassOfVehiclesVO covDetails = covService.findByCovCode(regDetails.getClassOfVehicle());

		if (covDetails == null) {
			throw new BadRequestException("Class of vehicle not found.." + regDetails.getClassOfVehicle() + "Pr no : "
					+ regDetails.getPrNo());
		}
		return covDetails;

	}

	@Override
	public RegServiceDTO getRegServiceDetails(String applicationNo) {
		Optional<RegServiceDTO> regServiceDTO = regServiceDAO.findByApplicationNo(applicationNo);
		if (!regServiceDTO.isPresent()) {
			logger.error("Application not found [{}]", applicationNo);
			throw new BadRequestException("Application not found [{}]" + applicationNo);
		}
		return regServiceDTO.get();
	}

	@Override
	public RegServiceVO doRepay(InputForRePay input, UserDTO user, HttpServletRequest request) {

		RegServiceDTO regDetails = this.getRegServiceDetails(input.getApplicationNo());

		// status check
		if (paymentStatusCheckForRepay(regDetails)) {
			logger.error("Invalid request for application no [{}] " + input.getApplicationNo());
			throw new BadRequestException("Invalid request for application no : " + input.getApplicationNo());
		}

		if (regDetails.getSlotDetails() != null) {
			if (!regDetails.getServiceType().stream().anyMatch(service -> service.equals(ServiceEnum.DATAENTRY))) {
				if (regDetails.getServiceType().stream()
						.anyMatch(service -> service.equals(ServiceEnum.ALTERATIONOFVEHICLE))) {
					if (regDetails.getAlterationDetails() != null && !regDetails.getAlterationDetails().isMVIDone()) {
						if (input.getSoltDate() == null) {
							throw new BadRequestException("Slot date is required.");
						}
					}
				} else if (input.getSoltDate() == null) {
					logger.error("Slot date is required.");
					throw new BadRequestException("Slot date is required.");
				}
			}
			// slotLogMapper.putDlSlotLog(appStatusDetails);
			/*
			 * if(regDetails.getSlotDetailsLog() != null) {
			 * regDetails.getSlotDetailsLog().add(regDetails.getSlotDetails()); }else {
			 * regDetails.setSlotDetailsLog(Arrays.asList(regDetails. getSlotDetails())); }
			 * regDetails.getSlotDetails().setCreatedDate(LocalDateTime.now());
			 * regDetails.getSlotDetails().setSlotDate(input.getSoltDate());
			 */
			if (regDetails.getSlotDetails().getSlotDate().equals(input.getSoltDate())) {
				if (regDetails.getSlotDetails().getPaymentStatus() == null
						|| !regDetails.getSlotDetails().getPaymentStatus().equals(StatusRegistration.PAYMENTFAILED)) {
					logger.error("Slot date should not be same as previous slot date.");
					throw new BadRequestException("Slot date should not be same as previous slot date.");
				}

			}
			if (input.getSoltDate() != null)
				try {
					commonModifySlot(input.getSoltDate(), regDetails);
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			// slotBookingService.setLock(appStatusDetails);
		}
		// regDetails.setApplicationStatus(StatusRegistration.PAYMENTPENDING);
		if (StringUtils.isNoneBlank(input.getGateWayType())) {
			regDetails.setGatewayType(input.getGateWayType());
		}
		if (regDetails.getServiceIds() != null && !regDetails.getServiceIds().isEmpty()
				&& regDetails.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VOLUNTARYTAX.getId()))) {
			String ip = "";
			if (request != null) {
				ip = request.getRemoteAddr();
			}
			regDetails.getVoluntaryTaxDetails()
					.setGatewayType(GatewayTypeEnum.getGatewayTypeEnumByDesc(input.getGateWayType()));
			this.setActions(regDetails.getVoluntaryTaxDetails(), user, ip);
		}
		regDetails.setPaymentTransactionNo(UUID.randomUUID().toString());
		RegServiceDTO dto = regServiceDAO.save(regDetails);
		RegServiceVO vo = regServiceMapper.convertEntity(dto);
		return vo;
	}

	private boolean paymentStatusCheckForRepay(RegServiceDTO regDetails) {
		logger.debug("Check payment status for Re-pay");
		if ((regDetails.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWFC.getId()))
				|| regDetails.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RENEWALFC.getId()))
				|| regDetails.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.OTHERSTATIONFC.getId())))
				&& regDetails.getServiceIds().size() == 1) {
			return false;
		}
		if ((regDetails.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RENEWAL.getId())))) {
			return false;
		}
		if ((regDetails.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.ALTERATIONOFVEHICLE.getId())))) {
			/*
			 * if
			 * (StringUtils.isNoneBlank(regDetails.getAlterationDetails().getVehicleTypeTo()
			 * ) && regDetails.getAlterationDetails().getVehicleTypeTo().equalsIgnoreCase(
			 * CovCategory.T.getCode()) &&
			 * regDetails.getRegistrationDetails().getVehicleType().equalsIgnoreCase(
			 * CovCategory.T.getCode())) {
			 */
			return false;
			/* } */

		}

		if ((regDetails.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VOLUNTARYTAX.getId())))
				&& regDetails.getApplicationStatus().equals(StatusRegistration.INITIATED)) {

			return false;

		}
		if (regDetails.getApplicationStatus().equals(StatusRegistration.CITIZENPAYMENTFAILED)
				|| regDetails.getApplicationStatus().equals(StatusRegistration.TAXPENDING)
				|| regDetails.getApplicationStatus().equals(StatusRegistration.OTHERSTATEPAYMENTPENDING)
				|| regDetails.getApplicationStatus().equals(StatusRegistration.REUPLOAD)
				|| regDetails.getApplicationStatus().equals(StatusRegistration.REJECTED)) {
			logger.debug("Payment status [{}] " + "return true", regDetails.getApplicationStatus());
			return false;
		}

		logger.debug("Payment status [{}] " + "return false", regDetails.getApplicationStatus());
		return true;

	}

	@Override
	public TransactionDetailVO getpaymentsForRepay(String applicationNo, String slotDate) {

		Optional<RegServiceDTO> regOptional = this.findByApplicationNo(applicationNo);
		if (!regOptional.isPresent()) {
			logger.error("No record found. [{}],[{}] ", applicationNo);
			throw new BadRequestException("No record found.Application no: " + applicationNo);
		}
		if (paymentStatusCheckForRepay(regOptional.get())) {
			logger.error("Invalid request for application no [{}] " + regOptional.get().getApplicationNo());
			throw new BadRequestException(
					"Invalid request for application no : " + regOptional.get().getApplicationNo());
		}
		RegServiceVO regvo = regServiceMapper.convertEntity(regOptional.get());
		TransactionDetailVO transactionDetails = this.getPaymentDetails(regvo, Boolean.FALSE, slotDate);

		return transactionDetails;
	}

	@Override
	public void getpaymentForRepay(String applicationNo, TransactionDetailVO regServiceDetail,
			FeeDetailsVO feeDetails) {
		feeDetails.setServiceIds(regServiceDetail.getServiceId());
		RegServiceVO vo = this.findapplication(applicationNo);
		if (vo.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.ALTERATIONOFVEHICLE.getId()))) {
			if (vo.getAlterationVO().isMVIDone() || vo.getAlterationVO().getAlterationService().stream()
					.anyMatch(type -> type.equals(AlterationTypeEnum.WEIGHT))) {
				feeDetails.setIsSkipSlot(Boolean.TRUE);
			}
		}
		if (!regServiceDetail.getServiceId().stream().anyMatch(id -> id.equals(ServiceEnum.BILLATERALTAX.getId()))) {
			if (vo.getRegistrationDetails() != null && vo.getRegistrationDetails().getApplicantDetails() != null
					&& vo.getRegistrationDetails().getApplicantDetails().getPresentAddress() != null
					&& vo.getRegistrationDetails().getApplicantDetails().getPresentAddress().getMandal() != null
					&& vo.getRegistrationDetails().getApplicantDetails().getPresentAddress().getMandal()
							.getMandalCode() != null) {
				feeDetails.setMandalCode(vo.getRegistrationDetails().getApplicantDetails().getPresentAddress()
						.getMandal().getMandalCode());
			}

			feeDetails.setOwnerType(vo.getRegistrationDetails().getOwnerType());
			feeDetails.setVehicleType(vo.getRegistrationDetails().getVehicleType());
		}

		feeDetails.setToPayLateFeeForFC(regServiceDetail.isToPayLateFeeForFC());
		feeDetails.setToPayLateFeeForRenewal(regServiceDetail.isToPayLateFeeForRenewal());
		if ((vo.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWFC.getId()))
				|| vo.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RENEWALFC.getId()))
				|| vo.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.OTHERSTATIONFC.getId()))
				|| vo.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.FCLATEFEE.getId())))
				&& vo.getServiceIds().size() == 1) {
			if (vo.getSlotDetails() != null) {
				feeDetails.setSlotDetailsVO(vo.getSlotDetails());
				if (feeDetails.getTotalFees() != null && feeDetails.getTotalFees() == 0.0) {
					feeDetails.setToPayLateFeeForFC(Boolean.FALSE);
				}
			}
		}
		if ((vo.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RENEWAL.getId())))
				|| vo.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RENEWALLATEFEE.getId()))) {
			if (vo.getSlotDetails() != null) {
				feeDetails.setSlotDetailsVO(vo.getSlotDetails());
				if (feeDetails.getTotalFees() != null && feeDetails.getTotalFees() == 0.0) {
					feeDetails.setToPayLateFeeForRenewal(Boolean.FALSE);
				}
			}
		}
		if ((vo.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.ALTERATIONOFVEHICLE.getId())))
				|| vo.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.FCLATEFEE.getId()))) {
			if (vo.isPaidPyamentsForFC()) {
				feeDetails.setToPayLateFeeForFC(Boolean.FALSE);
			}
			if (StringUtils.isNoneBlank(vo.getAlterationVO().getVehicleTypeTo())
					&& vo.getAlterationVO().getVehicleTypeTo().equalsIgnoreCase(CovCategory.T.getCode())
					&& vo.getRegistrationDetails().getVehicleType().equalsIgnoreCase(CovCategory.T.getCode())) {
				Optional<ClassOfVehicleConversion> classOfVehicle = classOfVehicleConversionDAO
						.findByNewCovAndNewCategoryAndCovAndCategory(vo.getAlterationVO().getCov(),
								vo.getAlterationVO().getVehicleTypeTo(),
								vo.getRegistrationDetails().getClassOfVehicle(),
								vo.getRegistrationDetails().getVehicleType());
				if (classOfVehicle.isPresent()) {
					if (classOfVehicle.get().isFcFee()) {
						feeDetails.setToPayLateFeeForFC(Boolean.TRUE);
					}
				}

			}
			if (vo.getSlotDetails() != null) {
				feeDetails.setSlotDetailsVO(vo.getSlotDetails());
				if (feeDetails.getTotalFees() != null && feeDetails.getTotalFees() == 0.0) {
					feeDetails.setToPayLateFeeForFC(Boolean.FALSE);
				}
			}

		}
		if ((vo.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId())))) {
			feeDetails.setIsSkipSlot(Boolean.TRUE);
			if (vo.getSlotDetails() != null) {
				feeDetails.setSlotDetailsVO(vo.getSlotDetails());
				feeDetails.setIsSkipSlot(Boolean.FALSE);
			}
			if (vo.isMviDone()) {
				feeDetails.setIsSkipSlot(Boolean.TRUE);
			}
		}

	}

	private FeeDetailsDTO getpaymentsForToken(String applicationNo) {

		List<PaymentTransactionDTO> paymentTransactionDTOList = null;

		paymentTransactionDTOList = paymentTransactionDAO
				.findByPayStatusAndApplicationFormRefNum(PayStatusEnum.SUCCESS.getDescription(), applicationNo);
		if (paymentTransactionDTOList.isEmpty()) {
			/*
			 * paymentTransactionDTOList = paymentTransactionDAO.
			 * findByApplicationFormRefNumAndPayStatusAndModuleCode( applicationNo,
			 * PayStatusEnum.SUCCESS.getDescription(), ModuleEnum.CITIZEN.getCode());
			 */
			logger.error("Payment not found [{}] ", applicationNo);
			throw new BadRequestException("Payment not found [{}] " + applicationNo);
		}

		paymentTransactionDTOList
				.sort((p1, p2) -> p2.getRequest().getRequestTime().compareTo(p1.getRequest().getRequestTime()));
		if (paymentTransactionDTOList.size() > 0 && paymentTransactionDTOList.get(0).getFeeDetailsDTO() != null) {
			PaymentTransactionDTO paymentTransactionDTO = paymentTransactionDTOList.get(0);

			return paymentTransactionDTO.getFeeDetailsDTO();

		} else {
			logger.error("Payment not found [{}] ", applicationNo);
			throw new BadRequestException("Payment not found [{}] " + applicationNo);
		}

	}

	@Override
	public List<ClassOfVehicleConversionVO> getCovForAlt(String cov, String newCategory) {

		List<ClassOfVehicleConversion> covOptional = classOfVehicleConversionDAO.findByCovAndNewCategory(cov,
				newCategory);

		if (!covOptional.isEmpty()) {
			List<ClassOfVehicleConversion> list = covOptional.stream().distinct().collect(Collectors.toList());// new
																												// ArrayList<ClassOfVehicleConversion>();
			return classOfVehicleConversionMapper.convertEntity(list);
		} else {
			logger.error("No data found in master_conversion for [{}],[{}] ", cov, newCategory);
			throw new BadRequestException(
					"Apllicatin not eligible to apply this category for :  " + cov + " and Type: " + newCategory);
		}
	}

	@Override
	public List<FuelConversionVO> getfuel(String oldFuel, String cov) {

		List<FuelConversion> listFuel = fuelConversionDAO.findByoldFuelAndCovIn(oldFuel, cov);
		// List<FuelConversion> listFuel =
		// fuelConversionDAO.findByoldFuel(oldFuel);

		if (!listFuel.isEmpty()) {
			return fuelConversionMapper.convertEntity(listFuel);
		} else {
			logger.error("\"Not applicable for Fuel conversion.[{}] ", listFuel);
			throw new BadRequestException("Not applicable for Fuel conversion.  " + listFuel);
		}
	}

	@Override
	public SeatConversionVO getSeats(String cov, String category) {

		Optional<SeatConversion> optionalSeat = seatConversionDAO.findByCovAndCategory(cov, category);

		if (optionalSeat.isPresent()) {
			return seatConversionMapper.convertEntity(optionalSeat.get());
		} else {
			logger.error("Not applicable for Seats conversion.[{}] ", cov);
			throw new BadRequestException("Not applicable for Seats conversion. cov:  " + cov);
		}
	}

	@Override
	public List<BodyTypeVO> getBodyType(String bodyType) {

		List<BodyTypeDTO> listOfBodyTypes = bodyTypeDAO.findByStatusTrue();

		if (listOfBodyTypes.isEmpty()) {
			logger.error("No data found in master_bodytype status True");
			throw new BadRequestException("No data found in master_bodytype status True");
		} else {
			return bodyTypeMapper
					.convertDTOs(listOfBodyTypes.stream().filter(body -> !body.getBodyType().equalsIgnoreCase(bodyType)

					).collect(Collectors.toList()));
		}
	}

	@Override
	public Optional<RegistrationDetailsVO> findByEngineNo(String EngineNo) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Optional<RegistrationDetailsVO> findByChassisNoAndEngineNo(String chassisNumber, String engineNumber) {

		Optional<RegistrationDetailsDTO> regOptional = registrationDetailDAO
				.findByVahanDetailsChassisNumberAndVahanDetailsEngineNumberAndNocDetailsIsNull(chassisNumber,
						engineNumber);
		if (regOptional.isPresent()) {
			RegistrationDetailsDTO dto = regOptional.get();
			RegistrationDetailsVO vo = registrationDetailsMapper.limitedDtoToVo(dto);
			return Optional.of(vo);

		}

		return Optional.empty();
	}

	private Optional<RegistrationDetailsVO> findByTrNo(String trNo) {
		Optional<StagingRegistrationDetailsDTO> regOptional = stagingRegistrationDetailsDAO.findByTrNo(trNo);
		if (regOptional.isPresent()) {
			RegistrationDetailsDTO registrationDetailsDTO = regOptional.get();
			RegistrationDetailsVO registrationDetailsVO = registrationDetailsMapper
					.limitedDtoToVo(registrationDetailsDTO);
			return Optional.of(registrationDetailsVO);
		}
		return Optional.empty();

	}

	public boolean isInRole(String role) {

		// role get ...

		return true;
	}

	public void duplicate(RegServiceDTO regDTO) {

	}

	public void renewal() {

	}

	public void coa() {

	}

	public boolean validateSlot(String applicationNo) {
		Optional<RegServiceDTO> regServiceOptional = regServiceDAO.findByApplicationNo(applicationNo);
		if (regServiceOptional.isPresent()) {
			RegServiceDTO regDTO = regServiceOptional.get();
			LocalDate slotDate = regDTO.getSlotDetails().getSlotDate();
			if (slotDate.equals(LocalDate.now())) {
				return true;
			}
			return false;
		} else {
			throw new BadRequestException("slot booked date is not today");
		}
	}

	@Override
	public Optional<RegistrationDetailsVO> getApplicationDetails(ApplicationSearchVO applicationSearchVO) {

		logger.info("getApplicationDetails start,Inputs: {}", applicationSearchVO);
		if (ModuleEnum.BODYBUILDER.equals(applicationSearchVO.getModule())) {
			return getBodyBuildDetails(applicationSearchVO.getTrNo(), applicationSearchVO.getMobileNo());
		}
		if (ModuleEnum.REG.equals(applicationSearchVO.getModule())) {
			if (applicationSearchVO.getApplicationNo() != null) {
				return findStagingDetailsByApplicationNo(applicationSearchVO.getApplicationNo());
			}
			if (applicationSearchVO.getTrNo() != null) {
				return findByTrNo(applicationSearchVO.getTrNo());
			}
		}

		return Optional.empty();

	}

	@Override
	public RegServiceVO getRegServiceForSearch(ApplicationSearchVO applicationSearchVO) {

		if (applicationSearchVO.getApplicationNo() != null) {
			RegServiceDTO dto = getRegServiceDetails(applicationSearchVO.getApplicationNo());
			RegServiceVO vo = regServiceMapper.convertEntity(dto);
			return vo;
		}
		return null;

	}

	private Optional<RegistrationDetailsVO> findStagingDetailsByApplicationNo(String applicationNo) {
		StagingRegistrationDetailsDTO stagingDTO = stagingRegistrationDetailsDAO.findOne(applicationNo);
		if (stagingDTO != null) {
			return Optional.of(registrationDetailsMapper.convertEntity(stagingDTO));
		}
		return Optional.empty();
	}

	private Optional<RegistrationDetailsVO> getBodyBuildDetails(String trNo, String mobileNo) {
		Optional<StagingRegistrationDetailsDTO> stagingDetailsOptional = stagingRegistrationDetailsDAO
				.findByTrNoAndApplicantDetailsContactMobile(trNo, mobileNo);
		if (!stagingDetailsOptional.isPresent()) {
			throw new BadRequestException("No record found for trNo: " + trNo);
		}
		List<String> listOfStatuses = bodyBuilderStatusList();
		if (!listOfStatuses.stream()
				.anyMatch(status -> status.equalsIgnoreCase(stagingDetailsOptional.get().getApplicationStatus()))) {
			logger.error("Invalid status for bodyBuild service {} ", trNo);
			throw new BadRequestException("Invalid status for bodyBuild service " + trNo);
		}
		if (!(stagingDetailsOptional.get().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode())
				|| stagingDetailsOptional.get().getClassOfVehicle()
						.equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
				|| stagingDetailsOptional.get().getClassOfVehicle()
						.equalsIgnoreCase(ClassOfVehicleEnum.ARVT.getCovCode())
				|| stagingDetailsOptional.get().getClassOfVehicle()
						.equalsIgnoreCase(ClassOfVehicleEnum.IVCN.getCovCode())
				||stagingDetailsOptional.get().getClassOfVehicle()
				.equalsIgnoreCase(ClassOfVehicleEnum.TTTT.getCovCode())	
				||stagingDetailsOptional.get().getClassOfVehicle()
				.equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())
				)) {
			logger.error("Vehicle is not chassis or ARVT or IVCN : [{}] ",
					stagingDetailsOptional.get().getApplicationNo());
			throw new BadRequestException("Vehicle is not chassis or ARVT or IVCN");

		}
		List<String> errors = new ArrayList<>();
		this.checkVcrDues(stagingDetailsOptional.get(), errors);
		if (!errors.isEmpty()) {
			logger.error("[{}]", errors.get(0));
			throw new BadRequestException(errors.get(0));
		}
		if (stagingDetailsOptional.get().getApplicantDetails() != null
				&& stagingDetailsOptional.get().getApplicantDetails().getPresentAddress() != null
				&& stagingDetailsOptional.get().getApplicantDetails().getPresentAddress().getMandal() != null
				&& stagingDetailsOptional.get().getApplicantDetails().getPresentAddress().getMandal()
						.getHsrpoffice() != null) {
			Optional<OfficeDTO> officeDetailsOptional = Optional.empty();
			if (StringUtils.isNoneBlank(stagingDetailsOptional.get().getApplicantDetails().getPresentAddress()
					.getMandal().getMviAddressOfficeCode())) {
				officeDetailsOptional = officeDAO.findByOfficeCode(stagingDetailsOptional.get().getApplicantDetails()
						.getPresentAddress().getMandal().getMviAddressOfficeCode());
			} else {
				officeDetailsOptional = officeDAO.findByOfficeCode(stagingDetailsOptional.get().getApplicantDetails()
						.getPresentAddress().getMandal().getHsrpoffice());
			}
			if (!officeDetailsOptional.isPresent()) {
				throw new BadRequestException("No record found in office details for applicationNo: "
						+ stagingDetailsOptional.get().getApplicationNo());
			}
			stagingDetailsOptional.get().setOfficeDetails(officeDetailsOptional.get());
		}
		return Optional.of(registrationDetailsMapper.convertEntity(stagingDetailsOptional.get()));
	}

	private List<String> bodyBuilderStatusList() {
		List<String> listOfStatuses = new ArrayList<>();
		listOfStatuses.add(StatusRegistration.CHASSISTRGENERATED.getDescription());
		listOfStatuses.add(StatusRegistration.SLOTBOOKED.getDescription());
		listOfStatuses.add(StatusRegistration.ABSENT.getDescription());
		listOfStatuses.add(StatusRegistration.TAXPENDING.getDescription());
		listOfStatuses.add(StatusRegistration.CITIZENPAYMENTFAILED.getDescription());
		listOfStatuses.add(StatusRegistration.CITIZENPAYMENTPENDING.getDescription());
		listOfStatuses.add(StatusRegistration.REJECTED.getDescription());
		listOfStatuses.add(StatusRegistration.PAYMENTPENDING.getDescription());
		return listOfStatuses;
	}

	@Override
	public StagingRegistrationDetailsDTO getStagingDetailsWithApplicationNo(String applicationNo) {

		Optional<StagingRegistrationDetailsDTO> stagingDetailsOptional = stagingRegistrationDetailsDAO
				.findByApplicationNo(applicationNo);
		if (!stagingDetailsOptional.isPresent()) {
			logger.error("No record found for applicationNo: " + applicationNo);
			throw new BadRequestException("No record found for applicationNo: " + applicationNo);
		}
		return stagingDetailsOptional.get();
	}

	@Override
	public StagingRegistrationDetailsDTO getStagingDetailsToken(String applicationNo) {

		Optional<StagingRegistrationDetailsDTO> stagingDetailsOptional = stagingRegistrationDetailsDAO
				.findByApplicationNo(applicationNo);
		if (!stagingDetailsOptional.isPresent()) {
			logger.error("No record found for applicationNo: " + applicationNo);
			throw new BadRequestException("No record found for applicationNo: " + applicationNo);
		}

		StagingRegistrationDetailsDTO regDetails = stagingDetailsOptional.get();

		if (!(regDetails.getApplicationStatus().equalsIgnoreCase(StatusRegistration.CHASSISTRGENERATED.getDescription())
				|| regDetails.getApplicationStatus().equalsIgnoreCase(StatusRegistration.SLOTBOOKED.getDescription())
				|| regDetails.getApplicationStatus().equalsIgnoreCase(StatusRegistration.ABSENT.getDescription())
				|| regDetails.getApplicationStatus().equalsIgnoreCase(StatusRegistration.REJECTED.getDescription()))) {
			regDetails.setFeeDetails(this.getpaymentsForToken(applicationNo));
			regDetails.setSlotDetails(null);
		} else {
			if (regDetails.getApplicantDetails() != null && regDetails.getApplicantDetails().getPresentAddress() != null
					&& regDetails.getApplicantDetails().getPresentAddress().getMandal() != null
					&& regDetails.getApplicantDetails().getPresentAddress().getMandal().getHsrpoffice() != null) {
				Optional<OfficeDTO> officeDetailsOptional = Optional.empty();
				if (StringUtils.isNoneBlank(stagingDetailsOptional.get().getApplicantDetails().getPresentAddress()
						.getMandal().getMviAddressOfficeCode())) {
					officeDetailsOptional = officeDAO.findByOfficeCode(stagingDetailsOptional.get()
							.getApplicantDetails().getPresentAddress().getMandal().getMviAddressOfficeCode());
				} else {
					officeDetailsOptional = officeDAO.findByOfficeCode(
							regDetails.getApplicantDetails().getPresentAddress().getMandal().getHsrpoffice());
				}
				if (!officeDetailsOptional.isPresent()) {
					logger.error("No record found in office details for applicationNo: " + applicationNo);
					throw new BadRequestException(
							"No record found in office details for applicationNo: " + applicationNo);
				}
				regDetails.setOfficeDetails(officeDetailsOptional.get());
			}
		}

		return regDetails;
	}

	@Override
	public RegistrationDetailsVO StagingDetailsForRepay(String applicationNo) {

		Optional<StagingRegistrationDetailsDTO> stagingDetailsOptional = stagingRegistrationDetailsDAO
				.findByApplicationNo(applicationNo);
		if (!stagingDetailsOptional.isPresent()) {
			logger.error("No record found for applicationNo: " + applicationNo);
			throw new BadRequestException("No record found for applicationNo: " + applicationNo);
		}

		StagingRegistrationDetailsDTO regDetails = stagingDetailsOptional.get();

		RegistrationDetailsVO vo = registrationDetailsMapper.convertEntity(regDetails);

		return vo;
	}

	@Override
	public AlterationVO alterVehicleDetails(String applicationNo) {

		Optional<AlterationDTO> alterOptional = alterationDAO.findByApplicationNo(applicationNo);
		if (!alterOptional.isPresent()) {
			logger.error("Alteration details not found , Application No : [{}] ", applicationNo);
			throw new BadRequestException("Alteration details not found: " + applicationNo);
		}
		AlterationVO vo = alterationMapper.convertEntity(alterOptional.get());
		return vo;
	}

	@Override
	public StagingRegistrationDetailsDTO saveStagingDetails(String slotDate, String applicationNo,
			boolean ismodifySlot) {

		synchronized (applicationNo.intern()) {

			Optional<StagingRegistrationDetailsDTO> stagingoptional = stagingRegistrationDetailsDAO
					.findByApplicationNo(applicationNo);
			if (!stagingoptional.isPresent()) {
				logger.error("Failed to save Staging Details for BodyBuilder , Application No : [{}] ", applicationNo);
				throw new BadRequestException("No record found in stagingRegistration Details for: " + applicationNo);
			}
			// TODO: check class of vehicle.
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
			LocalDate localDate = LocalDate.parse(slotDate, formatter);

			SlotDetailsDTO slotDetails = new SlotDetailsDTO();

			List<String> list = new ArrayList<>();

			list.add(StatusRegistration.CHASSISTRGENERATED.getDescription());
			// list.add(StatusRegistration.TRAILERTRGENERATED.getDescription());
			list.add(StatusRegistration.SLOTBOOKED.getDescription());
			list.add(StatusRegistration.ABSENT.getDescription());
			list.add(StatusRegistration.REJECTED.getDescription());

			StagingRegistrationDetailsDTO stagingDetails = stagingoptional.get();

			if (!list.contains(stagingDetails.getApplicationStatus())) {
				logger.error("Application Status Is Invalid for Application No : [{}] ", applicationNo);
				throw new BadRequestException("Application Status Is Invalid ");
			}
			if (!(stagingDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode())
					|| stagingDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
					|| stagingDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.ARVT.getCovCode())
					|| stagingDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.IVCN.getCovCode())||
					stagingDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TTTT.getCovCode())||
					stagingDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode()))) {

				logger.error("Vehicle is not chassis or ARVT or IVCN : [{}] ", stagingDetails.getApplicationNo());
				throw new BadRequestException("Vehicle is not chassis or ARVT or IVCN");

			}
			String officeCode;
			if (StringUtils.isNoneBlank(
					stagingDetails.getApplicantDetails().getPresentAddress().getMandal().getMviAddressOfficeCode())) {
				officeCode = stagingDetails.getApplicantDetails().getPresentAddress().getMandal()
						.getMviAddressOfficeCode();
			} else {
				officeCode = stagingDetails.getApplicantDetails().getPresentAddress().getMandal().getHsrpoffice();
			}
			String sloTime;
			if (ismodifySlot) {
				if (!(stagingDetails.getApplicationStatus()
						.equalsIgnoreCase(StatusRegistration.SLOTBOOKED.getDescription())
						|| stagingDetails.getApplicationStatus()
								.equalsIgnoreCase(StatusRegistration.ABSENT.getDescription())
						|| stagingDetails.getApplicationStatus()
								.equalsIgnoreCase(StatusRegistration.REJECTED.getDescription()))) {
					logger.error("Application Status Is Invalid for modify slot Application No : [{}] ", applicationNo);
					throw new BadRequestException("Application Status Is Invalid for modify slot");
				}
				if (stagingDetails.getSlotDetails() == null) {
					logger.error("slot details not found Application No : [{}] ", applicationNo);
					throw new BadRequestException("slot details not found");
				}
				if (stagingDetails.getSlotDetails().getSlotDate().equals(localDate)) {
					logger.error("slot should not same as previous slot day.Application No : [{}] ", applicationNo);
					throw new BadRequestException("slot should not same as previous slot day");
				}
				sloTime = slotService.modifySlot(ModuleEnum.REG.toString(), null, officeCode,
						stagingDetails.getSlotDetails().getSlotDate(), localDate);

			} else {
				sloTime = slotService.bookSlot(ModuleEnum.REG.toString(), null, officeCode, localDate);
				// stagingDetails.setIteration((stagingDetails.getIteration()==null)?1:stagingDetails.getIteration()+1);
			}
			slotDetails.setSlotTime(sloTime);

			if (stagingDetails.getSlotDetails() == null) {

				slotDetails.setCreatedDate(LocalDateTime.now());
				slotDetails.setSlotDate(localDate);
				stagingDetails.setSlotDetails(slotDetails);

			} else {

				if (stagingDetails.getSlotDetailsLog() == null) {
					stagingDetails.setSlotDetailsLog(new ArrayList<>());
				}
				stagingDetails.getSlotDetailsLog().add(stagingDetails.getSlotDetails());

				slotDetails.setCreatedDate(LocalDateTime.now());
				slotDetails.setSlotDate(localDate);
				stagingDetails.setSlotDetails(slotDetails);

			}
			/*
			 * if (stagingDetails.getClassOfVehicle().equalsIgnoreCase(
			 * ClassOfVehicleEnum.TTTT.getCovCode()) ||
			 * stagingDetails.getClassOfVehicle().equalsIgnoreCase(
			 * ClassOfVehicleEnum.TTRN.getCovCode())) { if
			 * (this.isToPAyTaxForTrailer(stagingDetails.getApplicationNo() , localDate)) {
			 * stagingDetails.setApplicationStatus(StatusRegistration.TAXPENDING
			 * .getDescription()); }else {
			 * stagingDetails.setApplicationStatus(StatusRegistration.SLOTBOOKED
			 * .getDescription()); } } else {
			 *
			 * stagingDetails.setApplicationStatus(StatusRegistration.SLOTBOOKED
			 * .getDescription()); }
			 */
			stagingDetails.setApplicationStatus(StatusRegistration.SLOTBOOKED.getDescription());
			if (stagingDetails.isIterationFlag()) {
				if (stagingDetails.getIteration() != null) {
					stagingDetails.setIterationFlag(Boolean.FALSE);
					stagingDetails.setIteration(stagingDetails.getIteration() + 1);
				} else {
					// For migration data
					stagingDetails.setIteration(2);
					stagingDetails.setIterationFlag(Boolean.FALSE);
				}
			}
			stagingDetails.setTaxAmount(null);
			stagingDetails.setTaxvalidity(null);
			stagingDetails.setCesFee(null);
			stagingDetails.setCesValidity(null);

			logMovingService.moveStagingToLog(stagingDetails.getApplicationNo());
			stagingRegistrationDetailsDAO.save(stagingDetails);
			Integer templateId = 0;
			if (ismodifySlot) {
				templateId = MessageTemplate.SLOTMODIFICATION.getId();
			} else {
				templateId = MessageTemplate.SLOTBOOKED.getId();
			}
			notifications.sendNotifications(templateId, stagingDetails);
			logger.info("SlotModfication  :Notification Processed  [{}] ", stagingDetails.getApplicationNo());

			return stagingDetails;
		}
	}

	public OfficeSlotsAvailabilityDTO getOfficeLlrAvailability(Integer moduleId) {
		logger.debug("get the days,hours and restestallowed days");
		Optional<OfficeSlotsAvailabilityDTO> officeAvblHoursOptional = officeSlotAvailabilityDao
				.findByModuleId(moduleId);

		if (officeAvblHoursOptional.isPresent()) {
			return officeAvblHoursOptional.get();

		} else {
			logger.info("OfficeSlotsAvailability not found for moduke id :", moduleId);

			throw new BadRequestException("OfficeSlotsAvailability not found for moduke id :" + moduleId);
		}
	}

	@Override
	public TransactionDetailVO getPaymentDetails(StagingRegistrationDetailsDTO stagingRegistrationDetails) {

		TransactionDetailVO transactionDetailVO = new TransactionDetailVO();
		// need to call body builder cov
		Optional<AlterationDTO> alterDetails = alterationDAO
				.findByApplicationNo(stagingRegistrationDetails.getApplicationNo());
		if (!alterDetails.isPresent()) {
			logger.error("No record found in alter Details for: " + stagingRegistrationDetails.getApplicationNo());
			throw new BadRequestException(
					"No record found in alter Details for: " + stagingRegistrationDetails.getApplicationNo());
		}

		String weightDetails = null;
		ClassOfVehiclesVO covDetails = null;
		boolean isCalculateFc = false;
		Integer rlw = null;
		GatewayTypeEnum payGatewayTypeEnum = GatewayTypeEnum
				.getGatewayTypeEnumByDesc(stagingRegistrationDetails.getGatewayTypeEnum());
		transactionDetailVO.setGatewayTypeEnum(payGatewayTypeEnum);
		// transactionDetailVO.setCovs(Arrays.asList(covDetails));\
		if (!ClassOfVehicleEnum.ARVT.getCovCode().equalsIgnoreCase(stagingRegistrationDetails.getClassOfVehicle())) {
			transactionDetailVO.getServiceEnumList().add(ServiceEnum.FR);
			transactionDetailVO.setServiceId(Stream.of(ServiceEnum.FR.getId()).collect(Collectors.toSet()));

		}
		transactionDetailVO.getServiceEnumList().add(ServiceEnum.BODYBUILDER);
		// transactionDetailVO.setWeightType(weightDetails);
		transactionDetailVO.setFirstName(stagingRegistrationDetails.getApplicantDetails().getDisplayName());
		transactionDetailVO.setEmail(stagingRegistrationDetails.getApplicantDetails().getContact().getEmail());
		transactionDetailVO.setPhone(stagingRegistrationDetails.getApplicantDetails().getContact().getMobile());
		transactionDetailVO.setModule(ModuleEnum.BODYBUILDER.getCode());
		transactionDetailVO.setOwnerType(stagingRegistrationDetails.getOwnerType());
		// List<Integer> serviceIds =
		// regServiceDetail.getServiceType().stream().map(id->id.getId()).collect(Collectors.toList());

		transactionDetailVO.setCov(alterDetails.get().getCov());
		transactionDetailVO.setVehicleType(stagingRegistrationDetails.getVehicleType());
		transactionDetailVO.setFormNumber(stagingRegistrationDetails.getApplicationNo());
		transactionDetailVO.setGatewayType(
				GatewayTypeEnum.getGatewayTypeEnumByDesc(stagingRegistrationDetails.getGatewayTypeEnum()));

		if (stagingRegistrationDetails.getVehicleType().equalsIgnoreCase(CovCategory.T.getCode())) {
			if (ClassOfVehicleEnum.ARVT.getCovCode().equalsIgnoreCase(stagingRegistrationDetails.getClassOfVehicle())) {
				if (alterDetails.get().getTrailers().isEmpty()) {
					logger.error("Trailers Details not found in Alteration collection for(ARVT) : "
							+ stagingRegistrationDetails.getApplicationNo());
					throw new BadRequestException("Trailers Details not found in Alteration collection for(ARVT) : "
							+ stagingRegistrationDetails.getApplicationNo());
				}
				Integer gtw = alterDetails.get().getTrailers().stream().findFirst().get().getGtw();
				for (TrailerChassisDetailsDTO trailerDetails : alterDetails.get().getTrailers()) {
					if (trailerDetails.getGtw() > gtw) {
						gtw = trailerDetails.getGtw();
					}
				}
				rlw = stagingRegistrationDetails.getVahanDetails().getGvw() + gtw;
			} else {
				rlw = stagingRegistrationDetails.getVahanDetails().getGvw();
			}
			weightDetails = covService.getWeightTypeDetails(rlw);
			// weightDetails =
			// covService.getWeightTypeDetails(stagingRegistrationDetails.getVahanDetails().getGvw());
		} else {
			weightDetails = covService.getWeightTypeDetails(alterDetails.get().getUlw());
		}

		covDetails = covService.findByCovCode(alterDetails.get().getCov());

		transactionDetailVO.setCalculateFc(isCalculateFc);
		transactionDetailVO.setWeightType(weightDetails);
		transactionDetailVO.setCovs(Arrays.asList(covDetails));
		transactionDetailVO.setOfficeCode(stagingRegistrationDetails.getOfficeDetails().getOfficeCode());
		transactionDetailVO.setChassesVehicle(Boolean.TRUE);
		transactionDetailVO.setPaymentTransactionNo(stagingRegistrationDetails.getPaymentTransactionNo());
		transactionDetailVO
				.setTxnid(getTransactionNumber(transactionDetailVO, stagingRegistrationDetails.getApplicationNo()));

		return transactionDetailVO;

	}

	/*
	 * @Override public TransactionDetailVO payTaxForTrailers(String applicationNo,
	 * GatewayTypeEnum gatewayType) {
	 *
	 * Optional<StagingRegistrationDetailsDTO> stagingDetailsOptional =
	 * stagingRegistrationDetailsDAO .findByApplicationNo(applicationNo); if
	 * (!stagingDetailsOptional.isPresent()) { throw new
	 * BadRequestException("No record found for applicationNo: " + applicationNo); }
	 *
	 * StagingRegistrationDetailsDTO regDetails = stagingDetailsOptional.get();
	 * regDetails.setGatewayTypeEnum(gatewayType.getDescription());
	 *
	 * boolean isCessNeedToPAy = Boolean.FALSE; boolean isTaxNeedToPAy =
	 * Boolean.FALSE; // RegistrationDetailsVO vo = //
	 * stagingRegistrationDetailsMapper.convertEntity(regDetails);
	 * List<TaxDetailsDTO> listTaxDetails =
	 * taxDetailsDAO.findByApplicationNoAndTrNoAndTaxStatus(regDetails.
	 * getApplicationNo(), regDetails.getTrNo(),TaxStatusEnum.ACTIVE.getCode()); if
	 * (listTaxDetails.isEmpty()) { throw new
	 * BadRequestException("tax details not found: " + applicationNo); }
	 *
	 * if ((regDetails.getApplicationStatus().equalsIgnoreCase(StatusRegistration.
	 * TAXPENDING.getDescription()))) {
	 *
	 * if (isTaxOrCessValid(regDetails, Arrays.asList(regDetails.getTaxType()),
	 * regDetails.getSlotDetails().getSlotDate())) {
	 *
	 * isTaxNeedToPAy = Boolean.TRUE; } if (isTaxOrCessValid(regDetails,
	 * Arrays.asList(ServiceCodeEnum.CESS_FEE.getCode()),
	 * regDetails.getSlotDetails().getSlotDate())) { isCessNeedToPAy = Boolean.TRUE;
	 * }
	 *
	 * if (!(isTaxNeedToPAy || isCessNeedToPAy)) {
	 * logger.error("No need to pay Tax : [{}] ", applicationNo); throw new
	 * BadRequestException("No need to pay Tax."); }
	 *
	 * return prepareRequestForTrailser(regDetails); } else {
	 * logger.error("Application Status Is Invalid Application No : [{}] ",
	 * applicationNo); throw new
	 * BadRequestException("Application Status Is Invalid."); }
	 *
	 * }
	 */
	/*
	 * private TransactionDetailVO
	 * prepareRequestForTrailser(StagingRegistrationDetailsDTO
	 * stagingRegistrationDetails) { TransactionDetailVO transactionDetailVO = new
	 * TransactionDetailVO(); String weightDetails = null; ClassOfVehiclesVO
	 * covDetails = null; boolean isCalculateFc = false;
	 *
	 * GatewayTypeEnum payGatewayTypeEnum = GatewayTypeEnum
	 * .getGatewayTypeEnumByDesc(stagingRegistrationDetails.getGatewayTypeEnum() );
	 * transactionDetailVO.setGatewayTypeEnum(payGatewayTypeEnum); //
	 * transactionDetailVO.setCovs(Arrays.asList(covDetails));
	 * transactionDetailVO.getServiceEnumList().add(ServiceEnum.TRAILER); //
	 * transactionDetailVO.setWeightType(weightDetails);
	 * transactionDetailVO.setFirstName(stagingRegistrationDetails.
	 * getApplicantDetails().getDisplayName());
	 * transactionDetailVO.setEmail(stagingRegistrationDetails.
	 * getApplicantDetails().getContact().getEmail());
	 * transactionDetailVO.setPhone(stagingRegistrationDetails.
	 * getApplicantDetails().getContact().getMobile());
	 * transactionDetailVO.setModule(ModuleEnum.TRAILER.getCode());
	 * transactionDetailVO.setOwnerType(stagingRegistrationDetails.getOwnerType( ));
	 * // List<Integer> serviceIds = //
	 * regServiceDetail.getServiceType().stream().map(id->id.getId()).collect(
	 * Collectors.toList());
	 *
	 * transactionDetailVO.setServiceId(Arrays.asList(ServiceEnum.TRAILER.getId(
	 * )));
	 * transactionDetailVO.setCov(stagingRegistrationDetails.getClassOfVehicle() );
	 * transactionDetailVO.setVehicleType(stagingRegistrationDetails.
	 * getVehicleType());
	 * transactionDetailVO.setFormNumber(stagingRegistrationDetails.
	 * getApplicationNo()); transactionDetailVO.setGatewayType(
	 * GatewayTypeEnum.getGatewayTypeEnumByDesc(stagingRegistrationDetails.
	 * getGatewayTypeEnum())); transactionDetailVO.setCalculateFc(isCalculateFc);
	 * transactionDetailVO.setWeightType(weightDetails); covDetails =
	 * covService.findByCovCode(stagingRegistrationDetails.getClassOfVehicle());
	 * transactionDetailVO.setCovs(Arrays.asList(covDetails));
	 * transactionDetailVO.setOfficeCode(stagingRegistrationDetails.
	 * getOfficeDetails().getOfficeCode());
	 * transactionDetailVO.setTrailerVehicle(Boolean.TRUE); return
	 * transactionDetailVO; }
	 */
	@Override
	public boolean isTaxOrCessValid(RegistrationDetailsDTO registrationOptional, List<String> taxTypes,
			LocalDate slotDate) {

		List<TaxDetailsDTO> listOfGreenTax = getTaxDetails(registrationOptional, taxTypes);
		listOfGreenTax.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		TaxDetailsDTO dto = listOfGreenTax.stream().findFirst().get();

		for (Map<String, TaxComponentDTO> map : dto.getTaxDetails()) {

			for (Entry<String, TaxComponentDTO> entry : map.entrySet()) {
				if (taxTypes.stream().anyMatch(key -> key.equalsIgnoreCase(entry.getKey()))) {
					if (entry.getValue().getValidityTo() == null) {
						return Boolean.TRUE;
					}
					if (entry.getValue().getValidityTo().isBefore(slotDate)) {
						return Boolean.TRUE;
					}
				}
			}
		}
		return Boolean.FALSE;
	}

	/*
	 * @Override public void saveStausforTrailer(String applicationNo) {
	 * StagingRegistrationDetailsDTO dto =
	 * this.getStagingDetailsWithApplicationNo(applicationNo);
	 * dto.setApplicationStatus(StatusRegistration.PAYMENTPENDING.getDescription
	 * ()); logMovingService.moveStagingToLog(dto.getApplicationNo());
	 * stagingRegistrationDetailsDAO.save(dto); }
	 */

	@Override
	public void saveStausforDateEntry(String applicationNo) {
		RegServiceDTO dto = this.getRegServiceDetails(applicationNo);
		dto.setApplicationStatus(StatusRegistration.PAYMENTPENDING);
		regServiceDAO.save(dto);
	}

	/*
	 * @Override public boolean isToPAyTaxForTrailer(String applicationNo,LocalDate
	 * slotdate) { boolean isCessNeedToPAy = Boolean.FALSE; boolean isTaxNeedToPAy =
	 * Boolean.FALSE; LocalDate slotBookedDate = null; StagingRegistrationDetailsDTO
	 * regDetails = this.getStagingDetailsWithApplicationNo(applicationNo);
	 *
	 * if(regDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.
	 * CHSN.getCovCode())
	 * ||regDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.CHST
	 * .getCovCode())) { return Boolean.FALSE; } if(slotdate != null){
	 * slotBookedDate = slotdate; }else{ slotBookedDate =
	 * regDetails.getSlotDetails().getSlotDate(); } if (isTaxOrCessValid(regDetails,
	 * Arrays.asList(regDetails.getTaxType()), slotBookedDate)) {
	 *
	 * isTaxNeedToPAy = Boolean.TRUE; } if (isTaxOrCessValid(regDetails,
	 * Arrays.asList(ServiceCodeEnum.CESS_FEE.getCode()), slotBookedDate)) {
	 * isCessNeedToPAy = Boolean.TRUE; }
	 *
	 * if (isTaxNeedToPAy || isCessNeedToPAy) { return Boolean.TRUE; } else { return
	 * Boolean.FALSE; } }
	 */

	@Override
	public Pair<TransactionDetailVO, Boolean> payTaxForDataEntry(String applicationNo, GatewayTypeEnum gatewayType) {

		Optional<RegServiceDTO> stagingDetailsOptional = regServiceDAO.findByApplicationNo(applicationNo);

		if (!stagingDetailsOptional.isPresent()) {
			logger.error("No record found for applicationNo: " + applicationNo);
			throw new BadRequestException("No record found for applicationNo: " + applicationNo);
		}

		RegServiceDTO regDetails = stagingDetailsOptional.get();
		// regDetails.getRegistrationDetails().isTaxPaidByVcr()
		TransactionDetailVO vo = prepareRequestForDataEntry(regDetails, gatewayType);
		return Pair.of(vo, regDetails.getRegistrationDetails().isTaxPaidByVcr());
	}

	@Override
	public String saveTransactionNo(String applicationNo) {
		RegServiceDTO dto = this.getRegServiceDetails(applicationNo);
		dto.setPaymentTransactionNo(UUID.randomUUID().toString());
		regServiceDAO.save(dto);
		return dto.getPaymentTransactionNo();
	}

	private TransactionDetailVO prepareRequestForDataEntry(RegServiceDTO regDetails, GatewayTypeEnum gateWayType) {
		TransactionDetailVO transactionDetailVO = new TransactionDetailVO();
		String weightDetails = null;
		ClassOfVehiclesVO covDetails = null;
		boolean isCalculateFc = false;

		transactionDetailVO.setGatewayTypeEnum(gateWayType);
		transactionDetailVO.getServiceEnumList().add(ServiceEnum.DATAENTRY);
		transactionDetailVO.setFirstName(regDetails.getRegistrationDetails().getApplicantDetails().getFirstName());
		transactionDetailVO.setEmail(regDetails.getRegistrationDetails().getApplicantDetails().getContact().getEmail());
		transactionDetailVO
				.setPhone(regDetails.getRegistrationDetails().getApplicantDetails().getContact().getMobile());
		transactionDetailVO.setModule(ModuleEnum.CITIZEN.getCode());
		transactionDetailVO.setOwnerType(regDetails.getRegistrationDetails().getOwnerType());
		Set<Integer> serviceIdsList = new TreeSet<>();
		serviceIdsList.add(ServiceEnum.DATAENTRY.getId());
		transactionDetailVO.setServiceId(serviceIdsList);
		transactionDetailVO.setCov(regDetails.getRegistrationDetails().getClassOfVehicle());
		transactionDetailVO.setVehicleType(regDetails.getRegistrationDetails().getVehicleType());
		transactionDetailVO.setFormNumber(regDetails.getApplicationNo());
		transactionDetailVO.setGatewayType(gateWayType);
		transactionDetailVO.setCalculateFc(isCalculateFc);
		transactionDetailVO.setTxnid(getTransactionNumber(transactionDetailVO, regDetails.getApplicationNo()));
		RegServiceVO vo = regServiceMapper.convertEntity(regDetails);
		OtherStateApplictionType applicationType = this.getOtherStateVehicleStatus(vo);
		if (OtherStateApplictionType.ApplicationNO.equals(applicationType)) {
			serviceIdsList = new TreeSet<>();
			serviceIdsList.add(ServiceEnum.FR.getId());
			serviceIdsList.add(ServiceEnum.TEMPORARYREGISTRATION.getId());
			transactionDetailVO.setServiceId(serviceIdsList);
			transactionDetailVO.getServiceEnumList().add(ServiceEnum.FR);
			transactionDetailVO.getServiceEnumList().add(ServiceEnum.TEMPORARYREGISTRATION);
			if (regDetails.getRegistrationDetails().getVehicleType().equalsIgnoreCase(CovCategory.T.getCode())) {
				transactionDetailVO.getServiceEnumList().add(ServiceEnum.NEWFC);
				transactionDetailVO.getServiceId().add(ServiceEnum.NEWFC.getId());
			}
		} else if (OtherStateApplictionType.TrNo.equals(applicationType)) {
			serviceIdsList = new TreeSet<>();
			serviceIdsList.add(ServiceEnum.FR.getId());
			transactionDetailVO.setServiceId(serviceIdsList);
			transactionDetailVO.getServiceEnumList().add(ServiceEnum.FR);
			if (regDetails.getRegistrationDetails().getVehicleType().equalsIgnoreCase(CovCategory.T.getCode())) {
				transactionDetailVO.getServiceEnumList().add(ServiceEnum.NEWFC);
				transactionDetailVO.getServiceId().add(ServiceEnum.NEWFC.getId());
			}
		}

		if (regDetails.getRegistrationDetails().getVehicleType().equalsIgnoreCase(CovCategory.T.getCode())) {
			weightDetails = covService
					.getWeightTypeDetails(regDetails.getRegistrationDetails().getVehicleDetails().getRlw());
		} else {
			weightDetails = covService
					.getWeightTypeDetails(regDetails.getRegistrationDetails().getVehicleDetails().getUlw());
		}
		transactionDetailVO.setWeightType(weightDetails);
		covDetails = covService.findByCovCode(regDetails.getRegistrationDetails().getClassOfVehicle());
		transactionDetailVO.setCovs(Arrays.asList(covDetails));
		transactionDetailVO.setOfficeCode(regDetails.getRegistrationDetails().getOfficeDetails().getOfficeCode());
		transactionDetailVO.setOtherState(Boolean.TRUE);
		transactionDetailVO.setRequestToPay(Boolean.TRUE);
		transactionDetailVO.setRegApplicationNo(regDetails.getApplicationNo());

		return transactionDetailVO;
	}

	@Override
	public List<RegServiceDTO> findByChassisNumberAndEngineNumber(String chassisNumber, String engineNumber,
			List<StatusRegistration> applicationStatus) {
		List<Integer> servicesIds = new ArrayList<>();
		servicesIds.add(ServiceEnum.DATAENTRY.getId());
		servicesIds.add(ServiceEnum.ISSUEOFNOC.getId());
		List<RegServiceDTO> regServiceDTO = regServiceDAO
				.findByRegistrationDetailsVahanDetailsChassisNumberAndRegistrationDetailsVahanDetailsEngineNumberAndApplicationStatusInAndServiceIdsIn(
						chassisNumber, engineNumber, applicationStatus, servicesIds);
		return regServiceDTO;
	}

	@Override
	public boolean isTopayOtherStateTax(FeeDetailsVO vo) {
		List<String> listTaxType = new ArrayList<>();
		listTaxType.add(ServiceCodeEnum.QLY_TAX.getCode());
		listTaxType.add(ServiceCodeEnum.HALF_TAX.getCode());
		listTaxType.add(ServiceCodeEnum.YEAR_TAX.getCode());
		if (vo.getFeeDetails().isEmpty()) {
			return Boolean.TRUE;
		}
		for (FeesVO feeVo : vo.getFeeDetails()) {
			if (listTaxType.stream().anyMatch(taxType -> taxType.equalsIgnoreCase(feeVo.getFeesType()))) {
				if (feeVo.getAmount() > 0) {
					return Boolean.TRUE;
				}
			}
		}

		return Boolean.FALSE;
	}

	@Override
	public RegServiceVO saveTaxForOtherState(String applicationNo) {
		RegServiceVO vo = this.getRegServiceDetailsVo(applicationNo);

		if (vo.getTaxDetails() != null) {
			vo.setTaxAmount(vo.getTaxDetails().getTaxAmount());
			vo.setTaxvalidity(vo.getTaxDetails().getPaymentDAte());
			vo.setTaxType(getTaxPeriod(vo.getRegistrationDetails().getClassOfVehicle(),
					vo.getRegistrationDetails().getVahanDetails().getSeatingCapacity()));
		}
		if (vo.getGreenTaxDetails() != null) {
			vo.setGreenTaxAmount(vo.getGreenTaxDetails().getTaxAmount());
			vo.setGreenTaxvalidity(vo.getGreenTaxDetails().getValidUpto());
		}
		return vo;
	}

	private String getTaxPeriod(String cov, String seatingCapacity) {
		Optional<MasterPayperiodDTO> Payperiod = masterPayperiodDAO.findByCovcode(cov);

		if (!Payperiod.isPresent()) {
			// throw error message
			logger.error("No record found in master_payperiod for: " + cov);
			throw new BadRequestException("No record found in master_payperiod for: " + cov);

		}

		if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.BOTH.getCode())) {
			if (Integer.parseInt(seatingCapacity) > 10) {
				Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
			} else {
				Payperiod.get().setPayperiod(TaxTypeEnum.LifeTax.getCode());
			}
		}
		if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.QuarterlyTax.getCode())) {
			return TaxTypeEnum.QuarterlyTax.getDesc();
		} else {
			return TaxTypeEnum.LifeTax.getDesc();
		}
	}

	public OtherStateApplictionType getOtherStateVehicleStatus(RegServiceVO regService) {
		OtherStateApplictionType applicationType = null;
		if (!regService.getRegistrationDetails().isRegVehicleWithPR()
				&& !regService.getRegistrationDetails().isRegVehicleWithTR()) {
			// application no
			applicationType = OtherStateApplictionType.ApplicationNO;
		} else if (regService.getRegistrationDetails().isRegVehicleWithTR()
				&& !regService.getRegistrationDetails().isRegVehicleWithPR()) {
			// TR no
			applicationType = OtherStateApplictionType.TrNo;
		} else {
			// PrNO
			applicationType = OtherStateApplictionType.PrNo;
		}
		return applicationType;
	}

	private void saveCitizenServices(RegServiceDTO dto) {
		/*
		 * if (!dto.getServiceIds().stream().anyMatch(id ->
		 * id.equals(ServiceEnum.NEWPERMIT.getId()))) {
		 * registratrionServicesApprovals.initiateApprovalProcessFlow(dto); }
		 */
		// RegServiceDTO dto = regServiceMapper.convertVO(regServiceVO);
		regServiceDAO.save(dto);
		/*
		 * if(dto.getServiceIds().stream().anyMatch(id->id.equals(ServiceEnum.
		 * TRANSFEROFOWNERSHIP.getId()))){
		 * notifications.sendNotifications(MessageTemplate.TOW_TOKEN_GENERATED. getId(),
		 * dto); }
		 */
		if (dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RCCANCELLATION.getId()))) {
			notifications.sendNotifications(MessageTemplate.RC_CANCELLATION.getId(), dto);
		}
	}

	@Override
	public void saveCitizenServicesPaymentPending(RegServiceVO regServiceVO, Boolean isRepay) {
		Optional<RegServiceDTO> dto = regServiceDAO.findByApplicationNo(regServiceVO.getApplicationNo());
		if (!dto.isPresent()) {
			logger.error("No record found in reg service for: " + regServiceVO.getApplicationNo());
			throw new BadRequestException("No record found in reg service for: " + regServiceVO.getApplicationNo());
		}
		RegServiceDTO regDto = dto.get();
		if (isRepay) {
			if (!((regDto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWFC.getId()))
					|| regDto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RENEWALFC.getId()))
					|| regDto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.OTHERSTATIONFC.getId()))
					|| regDto.getServiceIds().stream()
							.anyMatch(id -> id.equals(ServiceEnum.ALTERATIONOFVEHICLE.getId()))
					|| regDto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RENEWAL.getId()))))) {
				if (!(regDto.getApplicationStatus().equals(StatusRegistration.CITIZENPAYMENTFAILED)
						|| regDto.getApplicationStatus().equals(StatusRegistration.TAXPENDING)
						|| regDto.getApplicationStatus().equals(StatusRegistration.OTHERSTATEPAYMENTPENDING)
						|| regDto.getApplicationStatus().equals(StatusRegistration.REUPLOAD)
						|| regDto.getApplicationStatus().equals(StatusRegistration.REJECTED))) {
					logger.error("Invalid status to repay: " + regDto.getApplicationNo());
					throw new BadRequestException("Invalid status to repay: " + regDto.getApplicationNo());
				}
			}
		}
		regDto.setApplicationStatus(StatusRegistration.PAYMENTPENDING);
		if (regDto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.ALTERATIONOFVEHICLE.getId()))
				&& !isRepay) {
			String altId = stagingBodyBuilderService.getSequenceNo();
			regDto.getAlterationDetails().setId(altId);
			regDto.getAlterationDetails().setApplicationNo(regDto.getApplicationNo());
			regDto.getAlterationDetails().setOldPrNo(regDto.getPrNo());
			alterationDAO.save(regDto.getAlterationDetails());
		}

		if (regDto.getServiceIds().contains(ServiceEnum.NEWSTAGECARRIAGEPERMIT.getId())) {
			PermitDetailsDTO pdtl = regDto.getPdtl();
			pdtl.setRdto(registrationDetailDAO.findByPrNo(regDto.getPrNo()).get());
			pdtl.setIsPaymentDone(false);
			pdtl.setPermitStatus(PermitsEnum.ACTIVE.getDescription());
			pdtl.setServiceIds(regDto.getServiceIds());
			pdtl.setServiceType(regDto.getServiceType());
			pdtl.setCreatedDate(LocalDateTime.now());
			pdtl.setCreatedBy(regDto.getCreatedBy());
			permitDetailsDAO.save(pdtl);
		}
		this.saveCitizenServices(regDto);
	}

	@Override

	public RegServiceVO findByprNo(String prNo) {
		// TODO Auto-generated method stub
		List<RegServiceDTO> regList = regServiceDAO.findByPrNoAndServiceTypeNotIn(prNo,
				ServiceEnum.getServiceEnumById(ServiceEnum.TAXATION.getId()));
		if (regList.isEmpty()) {
			logger.error("No record found. [{}],[{}] ", prNo);
			// throw new BadRequestException("No record found.Pr no: " + prNo );
			return null;
		}
		regList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		RegServiceDTO regDTO = regList.stream().findFirst().get();
		RegServiceVO vo = regServiceMapper.convertEntity(regDTO);

		return vo;
	}

	@Override
	public Optional<RegServiceVO> theftObjectionValidation(String prNo) {
		RegServiceVO vo = new RegServiceVO();
		List<RegServiceDTO> regList = regServiceDAO.findByPrNoAndServiceTypeIn(prNo,
				ServiceEnum.getTheftRelatedServices());
		if (!regList.isEmpty()) {
			regList.sort((p2, p1) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
			RegServiceDTO regServiceDTO = regList.stream().findFirst().get();
			vo = regServiceMapper.convertEntity(regServiceDTO);
			return Optional.of(vo);
		}
		return Optional.empty();
	}

	@Override
	public RegistrationDetailsVO dotheftObjection(String prNo, String role) throws RcValidationException {
		RegistrationDetailsVO registrationDetailsVO = new RegistrationDetailsVO();
		List<String> errors = new ArrayList<String>();

		Optional<RegServiceVO> regServiceDetails = theftObjectionValidation(prNo);
		if (regServiceDetails.isPresent()) {
			RegServiceVO regServiceVO = regServiceDetails.get();
			if (role.equals(RoleEnum.RTO.getName())) {
				if (!(regServiceVO.getServiceIds().contains(ServiceEnum.OBJECTION.getId())
						|| regServiceVO.getServiceIds().contains(ServiceEnum.THEFTINTIMATION.getId()))) {
					logger.error("Vehicle not applied for Theft Objection [{}] Please apply Theft Objection", prNo);
					throw new BadRequestException(
							"Vehicle[" + prNo + "] not applied for Theft Objection, Please apply Theft Objection");
				}

				if (regServiceVO.getServiceIds().contains(ServiceEnum.OBJECTION.getId()) && regServiceVO
						.getApplicationStatus().toString().equals(StatusRegistration.REJECTED.getDescription())) {
					logger.error("THEFT OBJECTION is REJECTED  for prNo [{}] Please re-apply", prNo);
					throw new BadRequestException("THEFT OBJECTION is REJECTED  for prNo " + prNo + " Please Re-apply");
				}

				if (regServiceVO.getServiceIds().contains(ServiceEnum.OBJECTION.getId())
						&& regServiceVO.getTheftDetails() != null && regServiceVO.getTheftDetails().getStatus() != null
						&& regServiceVO.getTheftDetails().getStatus().equals(TheftState.REVOKED.toString())) {
					logger.error("Already REVOKED please apply THEFT OBJECTION for prNo: [{}] ", prNo);
					throw new BadRequestException("Already REVOKED please apply THEFT OBJECTION for prNo: " + prNo);
				}

			}
			if (regServiceVO.getServiceType().contains(ServiceEnum.THEFTINTIMATION) && regServiceVO
					.getApplicationStatus().toString().equals(StatusRegistration.INITIATED.getDescription())) {
				logger.error("THEFT INTIMATION is already open for prNo [{}]", prNo);
				throw new BadRequestException("THEFT INTIMATION is already open for prNo " + prNo);
			}
			if (regServiceVO.getServiceType().contains(ServiceEnum.THEFTREVOCATION) && regServiceVO
					.getApplicationStatus().toString().equals(StatusRegistration.INITIATED.getDescription())) {
				logger.error("THEFT REVOCATION is already open for prNo [{}]", prNo);
				throw new BadRequestException("THEFT REVOCATION is already open for prNo " + prNo);
			}
			if (regServiceVO.getServiceType().contains(ServiceEnum.OBJECTION) && regServiceVO.getApplicationStatus()
					.toString().equals(StatusRegistration.INITIATED.getDescription())) {
				logger.error("THEFT OBJECTION is already open for prNo [{}]", prNo);
				throw new BadRequestException("THEFT OBJECTION is already open for prNo " + prNo);
			}
			if (regServiceVO.getServiceType().contains(ServiceEnum.REVOCATION) && regServiceVO.getApplicationStatus()
					.toString().equals(StatusRegistration.INITIATED.getDescription())) {
				logger.error("THEFT REVOCATION is already open for prNo [{}]", prNo);
				throw new BadRequestException("THEFT REVOCATION is already open for prNo " + prNo);
			}
			registrationDetailsVO = getRegDetails(role, prNo, errors);
		} else {
			registrationDetailsVO = getRegDetails(role, prNo, errors);
		}

		return registrationDetailsVO;

	}

	public RegistrationDetailsVO getRegDetails(String role, String prNo, List<String> errors) {
		RegistrationDetailsVO registrationDetailsVO = new RegistrationDetailsVO();
		List<RegServiceDTO> regServiceList = regServiceDAO.findByPrNoAndServiceTypeNotIn(prNo,
				ServiceEnum.getServiceEnumById(ServiceEnum.TAXATION.getId()));
		Optional<RegistrationDetailsDTO> regDetailsOptional = registrationDetailDAO.findByPrNo(prNo);
		if (!regServiceList.isEmpty()) {
			regServiceList.sort((p2, p1) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
			RegServiceDTO regServiceDTO = regServiceList.stream().findFirst().get();
			if (regServiceDTO.getServiceIds().contains(ServiceEnum.ISSUEOFNOC.getId()) && regServiceDTO
					.getApplicationStatus().toString().equals(StatusRegistration.APPROVED.getDescription())) {

				logger.error("Please cancel NOC to avail this services for prNo : [{}]", prNo);
				throw new BadRequestException("Please cancel NOC to avail this services for prNo : " + prNo);

			}
			if (regServiceDTO.getServiceIds().contains(ServiceEnum.ISSUEOFNOC.getId()) && !regServiceDTO
					.getApplicationStatus().toString().equals(StatusRegistration.APPROVED.getDescription())) {

				logger.error("Your NOC application is pending in RTA side for prNo : [{}]", prNo);
				throw new BadRequestException("Your NOC application is pending for approval prNo : " + prNo

						+ " Please go to Citizen Portal Registration application search to know our application Status");

			}
			if (role.equals(RoleEnum.RTO.getName()) && !(regServiceDTO.getServiceType().contains(ServiceEnum.OBJECTION)

					|| regServiceDTO.getServiceType().contains(ServiceEnum.THEFTINTIMATION))) {
				logger.error("Vehicle not applied for Theft Objection [{}] Please apply Theft Objection", prNo);

				throw new BadRequestException(
						"Vehicle[" + prNo + "] not applied for Theft Objection, Please apply Theft Objection");

			}
			if (regServiceDTO.getServiceType().contains(ServiceEnum.THEFTINTIMATION) && regServiceDTO
					.getApplicationStatus().toString().equals(StatusRegistration.APPROVED.getDescription())) {

				throw new BadRequestException("THEFT INTIMATION is approved please THEFT REVOCATE prNo" + prNo);
			}
		} else {
			if (role.equals(RoleEnum.RTO.getName())) {
				throw new BadRequestException("Please Apply for THEFT INTIMATION/OBJECTION for REVOCATION");

			}
		}
		if (regDetailsOptional.isPresent()) {
			RegistrationDetailsDTO regDTO = regDetailsOptional.get();
			registrationDetailsVO = registrationDetailsMapper.convertEntity(regDTO);
		} else {
			throw new BadRequestException("No record found based on prNo in registrationDetails: " + prNo);
		}
		return registrationDetailsVO;
	}

	@Override
	public Boolean isToWithSealler(RegServiceVO regServiceVO) {

		try {
			RegistrationDetailsDTO dto = registrationDetailsMapper
					.convertVOForOtherState(regServiceVO.getRegistrationDetails());
			if (regServiceVO.getTowDetails() != null && regServiceVO.getTowDetails().getSeller() != null
					&& regServiceVO.getTowDetails().getBuyer() == null) {
				if (regServiceVO.getServiceIds().contains(ServiceEnum.RENEWAL.getId())
						|| regServiceVO.getServiceIds().contains(ServiceEnum.DUPLICATE.getId())
						|| regServiceVO.getServiceIds().contains(ServiceEnum.SURRENDEROFPERMIT.getId())) {
					return Boolean.FALSE;
				}

				if (checkIsTaxPending(dto)) {
					return Boolean.FALSE;
				} else {
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;
		} catch (Exception e) {
			ErrorTrackLogDTO exceptionlog = new ErrorTrackLogDTO();
			exceptionlog.setCreatedDate(LocalDateTime.now());
			exceptionlog.setError(e.getMessage());
			exceptionlog.setIsFixed(Boolean.FALSE);
			exceptionlog.setModuleCode(regServiceVO.getServiceType().get(0).getCode());
			exceptionlog.setPrNo(regServiceVO.getPrNo());
			exceptionlog.setApplicationNo(regServiceVO.getApplicationNo());
			exceptionlog.setContext(StatusRegistration.PAYMENTFAILED.name());
			errorTrackLogDAO.save(exceptionlog);

			return Boolean.FALSE;
		}
	}

	private void doReAssign(RegServiceVO input, KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {

		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		// regServiceDetails.setAlterationDetails(alterationMapper.convertVO(input.getAlterationVO()));

		// getOfficeDetails(input, citizenObjects, regServiceDetails);
		if (regServiceDetails != null && regServiceDetails.getRegistrationDetails() != null
				&& StringUtils.isNoneBlank(regServiceDetails.getRegistrationDetails().getVehicleType())
				&& regServiceDetails.getRegistrationDetails().getVehicleType()
						.equalsIgnoreCase(CovCategory.T.getCode())) {
			regServiceDetails.setNeedToUpdatePrNoInFc(Boolean.TRUE);
		}

	}

	/*
	 * @Override public TransactionDetailVO alterVehicleDiffTaxPay(String
	 * applicationNo,GatewayTypeEnum gatewayType) {
	 *
	 * RegServiceDTO dto = this.getRegServiceDetails(applicationNo);
	 * if(!dto.getApplicationStatus().equals(StatusRegistration.TAXPENDING)) {
	 * logger.error("Invalid status to pay tax : " + dto.getPrNo()); throw new
	 * BadRequestException("Invalid status to pay tax : " + dto.getPrNo()); }
	 * RegServiceVO vo = regServiceMapper.convertEntity(dto); return
	 * getPaymentDetailsForalterVehicleDiffTax(vo,gatewayType); }
	 *
	 * private TransactionDetailVO
	 * getPaymentDetailsForalterVehicleDiffTax(RegServiceVO
	 * regServiceDetail,GatewayTypeEnum gatewayType) {
	 *
	 * TransactionDetailVO transactionDetailVO = new TransactionDetailVO(); String
	 * weightDetails = null; ClassOfVehiclesVO covDetails = null; boolean
	 * isCalculateFc = false; transactionDetailVO.setGatewayTypeEnum(gatewayType);
	 * transactionDetailVO.setServiceEnumList(regServiceDetail.getServiceType()) ;
	 * transactionDetailVO .setFirstName(regServiceDetail.getRegistrationDetails().
	 * getApplicantDetails() .getDisplayName()); transactionDetailVO
	 * .setEmail(regServiceDetail.getRegistrationDetails().getApplicantDetails() .
	 * getContact().getEmail()); transactionDetailVO
	 * .setPhone(regServiceDetail.getRegistrationDetails().getApplicantDetails() .
	 * getContact().getMobile());
	 * transactionDetailVO.setModule(ModuleEnum.CITIZEN.getCode());
	 * transactionDetailVO.setOwnerType(regServiceDetail.getRegistrationDetails( ).
	 * getOwnerType()); // List<Integer> serviceIds = //
	 * regServiceDetail.getServiceType().stream().map(id->id.getId()).collect(
	 * Collectors.toList()); List<ServiceEnum> serviceIds =
	 * regServiceDetail.getServiceIds().stream() .map(id ->
	 * ServiceEnum.getServiceEnumById(id)).collect(Collectors.toList());
	 * transactionDetailVO.setServiceEnumList(serviceIds);
	 * transactionDetailVO.setServiceId(regServiceDetail.getServiceIds());
	 * transactionDetailVO.setCov(regServiceDetail.getRegistrationDetails().
	 * getClassOfVehicle()); transactionDetailVO.setVehicleType(regServiceDetail.
	 * getRegistrationDetails(). getVehicleType());
	 * transactionDetailVO.setFormNumber(regServiceDetail.getApplicationNo());
	 * transactionDetailVO.setRegApplicationNo(regServiceDetail.
	 * getRegistrationDetails().getApplicationNo());
	 * transactionDetailVO.setGatewayType(GatewayTypeEnum. getGatewayTypeEnumByDesc(
	 * regServiceDetail.getGatewayType()));
	 *
	 * Optional<ClassOfVehicleConversion> classOfVehicle =
	 * classOfVehicleConversionDAO.findByNewCovAndNewCategory(
	 * regServiceDetail.getAlterationVO().getCov(),
	 * regServiceDetail.getAlterationVO().getVehicleTypeTo()); if
	 * (classOfVehicle.isPresent()) { if (classOfVehicle.get().isFcFee()) {
	 * isCalculateFc = true; } } covDetails =
	 * covService.findByCovCode(regServiceDetail.getAlterationVO().getCov()); if
	 * (regServiceDetail.getRegistrationDetails().getVehicleDetails() == null ||
	 * regServiceDetail.getRegistrationDetails().getVehicleDetails().getRlw() ==
	 * null) { logger.error("VehicleDetails not  found. [{}],[{}] ",
	 * regServiceDetail.getApplicationNo()); throw new
	 * BadRequestException("VehicleDetails not  found :.  " +
	 * regServiceDetail.getApplicationNo()); } weightDetails =
	 * getWeight(regServiceDetail);
	 * transactionDetailVO.setCalculateFc(isCalculateFc);
	 * transactionDetailVO.setWeightType(weightDetails);
	 * transactionDetailVO.setCovs(Arrays.asList(covDetails));
	 * transactionDetailVO.setOfficeCode(regServiceDetail.getOfficeCode());
	 * transactionDetailVO.setRequestToPay(Boolean.TRUE);
	 *
	 * return transactionDetailVO;
	 *
	 * }
	 */
	// To get the latest record from the registration based on the PR no
	public RegServiceDTO getLatestRecord(String prNo) {
		if (StringUtils.isNotBlank(prNo)) {
			List<RegServiceDTO> listRegDetails = regServiceDAO.findByPrNoAndServiceTypeNotIn(prNo,
					ServiceEnum.getServiceEnumById(ServiceEnum.TAXATION.getId()));

			if (!CollectionUtils.isEmpty(listRegDetails)) {
				List<RegServiceDTO> regFilterList = filterServiceIdsNotEmpty(listRegDetails);
				if (!CollectionUtils.isEmpty(regFilterList)) {
					regFilterList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
					return regFilterList.stream().findFirst().get();
				}
				throw new BadRequestException("Records not availble for PrNo : " + prNo);
			}
			throw new BadRequestException("Records not availble for PrNo : " + prNo);
		}
		throw new BadRequestException("Records not availble for PrNo  : " + prNo);
	}

	/*
	 * private void sellerAcceptance(RcValidationVO rcValidationVO){
	 *
	 * //RegistrationDetailsDTO registrationDetailsDTO =
	 * getRegDetails(rcValidationVO.getPrNo(),rcValidationVO.getAadharNo());
	 * RegServiceDTO regServiceDTO = getLatestRecord(rcValidationVO.getPrNo());
	 * if(regServiceDTO.getRegistrationDetails().getApplicantDetails().
	 * getAadharNo() .equals(rcValidationVO.getAadharNo())){ AadharDetailsResponseVO
	 * aadharResponse =
	 * getAadharResponse(rcValidationVO.getAadhaarDetailsRequestVO());
	 * if(!rcValidationVO.getAadharNo().equals(aadharResponse.getUid().toString(
	 * ))){ throw new BadRequestException("Finger print Mismached"); }
	 * if(regServiceDTO.getTowDto()!=null &&
	 * regServiceDTO.getTowDto().isTokenStatus() ){
	 * if(regServiceDTO.getTowDto().getTokenNo().equals(rcValidationVO.getToken(
	 * ))){ TowDTO toDTO = regServiceDTO.getTowDto();
	 * toDTO.setSellerDecision(rcValidationVO.getSellerDecision());
	 * regServiceDTO.setTowDto(toDTO); regServiceDAO.save(regServiceDTO); } } }else{
	 * throw new BadRequestException("Aadhaar Number And PrNo Mismatched"); }
	 *
	 *
	 * }
	 */

	@Override
	public List<CitizenSearchReportVO> fetchDetailsFromRegistrationServices(ApplicationSearchVO applicationSearchVO) {
		List<CitizenSearchReportVO> searchReport = new ArrayList<>();
		boolean isToEnableTokenCancel = false;
		List<RegServiceDTO> servicesList = null;
		if (StringUtils.isBlank(applicationSearchVO.getChassisNo())) {
			logger.error("Please provide chassis numbers ," + applicationSearchVO);
			throw new BadRequestException("Please provide chassis numbers ," + applicationSearchVO);
		}
		if (applicationSearchVO.getApplicationNo() != null
				&& StringUtils.isNoneBlank(applicationSearchVO.getApplicationNo())) {
			servicesList = regServiceDAO.findByApplicationNoIn(applicationSearchVO.getApplicationNo());
		}

		if (applicationSearchVO.getTrNo() != null && StringUtils.isNoneBlank(applicationSearchVO.getTrNo())) {
			servicesList = regServiceDAO
					.findByRegistrationDetailsTrNoAndServiceIdsNotNull(applicationSearchVO.getTrNo());
		}

		if (applicationSearchVO.getPrNo() != null && StringUtils.isNoneBlank(applicationSearchVO.getPrNo())) {
			servicesList = regServiceDAO.findByPrNo(applicationSearchVO.getPrNo());
		}

		List<RegServiceDTO> regServiceList = getRegistrationServicesLatestRecordBasedOnServicesList(servicesList,
				applicationSearchVO.getChassisNo());
		if ((applicationSearchVO.getPrNo() != null && StringUtils.isNoneBlank(applicationSearchVO.getPrNo()))
				|| (applicationSearchVO.getTrNo() != null && StringUtils.isNoneBlank(applicationSearchVO.getTrNo()))) {
			ArrayList<RegServiceDTO> arrayList = new ArrayList<RegServiceDTO>();
			RegServiceDTO dto = regServiceList.stream().findFirst().get();
			if (dto.getApplicationStatus().equals(StatusRegistration.APPROVED)) {
				regServiceList.forEach(s -> {
					List<String> pendingStatus = StatusRegistration.PendingStatus();
					if (pendingStatus.contains(s.getApplicationStatus().toString())) {
						arrayList.add(s);
					}

				});
			}

			if (!arrayList.isEmpty()) {
				arrayList.sort((s1, s2) -> s2.getCreatedDate().compareTo(s1.getCreatedDate()));
				RegServiceDTO regServiceDTO = arrayList.stream().findFirst().get();
				regServiceList.clear();
				regServiceList.add(regServiceDTO);
			} else {
				regServiceList = Arrays.asList(regServiceList.stream().findFirst().get());
			}
		}
		if (!regServiceList.isEmpty()) {
			for (RegServiceDTO regServiceDTO : regServiceList) {
				if (!(regServiceDTO.getFlowId() != null
						&& regServiceDTO.getFlowId().equals(ServiceEnum.Flow.RCCANCELLATIONCCO))) {
					if (null != regServiceDTO.getBuyerDetails()) {
						if (null != regServiceDTO.getBuyerDetails().getBuyerApplicantDetails()) {
							regServiceDTO.getRegistrationDetails().getApplicantDetails().setFatherName(
									regServiceDTO.getBuyerDetails().getBuyerApplicantDetails().getFatherName());
							regServiceDTO.getRegistrationDetails().getApplicantDetails().setFirstName(
									regServiceDTO.getBuyerDetails().getBuyerApplicantDetails().getFirstName());
						}
						if (TowTokenStatus.validateStatus(regServiceDTO.getApplicationStatus().toString())) {
							isToEnableTokenCancel = true;
						}
					}

					CitizenSearchReportVO vo = regServiceMapper.convertSpecificFieldsForCtizenSearch(regServiceDTO);
					if (isToEnableTokenCancel) {
						vo.setTokenCancelRequired(true);
					}
					vo.setApplicationStatus(regServiceDTO.getApplicationStatus());
					freshRcValidationForStatus(regServiceDTO, vo);
					Optional<RegServicesFeedBack> serviceOptionalFeedBackForm = regServicesFeedBackDAO
							.findByApplicationNo(regServiceDTO.getApplicationNo());
					if (serviceOptionalFeedBackForm.isPresent()) {
						vo.setFeedBackFormsubmited(Boolean.TRUE);
					}

					if ((regServiceDTO.getServiceIds().contains(ServiceEnum.HPA.getId())
							|| regServiceDTO.getServiceIds().contains(ServiceEnum.HIREPURCHASETERMINATION.getId()))
							&& (regServiceDTO.getCurrentIndex() != null
									&& RoleEnum.RTO.getIndex() + 1 != regServiceDTO.getCurrentIndex().intValue()
									&& regServiceDTO.getFinanceDetails() != null
									&& regServiceDTO.getFinanceDetails().getStatus() != null)) {
						vo.setApplicationStatus(regServiceDTO.getApplicationStatus());
					}
					if (regServiceDTO.getServiceIds().contains(ServiceEnum.OTHERSTATIONFC.getId())
							&& regServiceDTO.getApplicationStatus().equals(StatusRegistration.MVIREJECTED)
							&& regServiceDTO.isAllowFcForOtherStation()) {
						vo.setNeedToEnableSecondButton(Boolean.TRUE);
						vo.setSecondButtonName("Inspect At Home Office");
					}
					if (regServiceDTO.getServiceIds().contains(ServiceEnum.VEHICLESTOPPAGE.getId())) {
						if (regServiceDTO.getVehicleStoppageDetails() != null
								&& regServiceDTO.getVehicleStoppageDetails().getMviReport() != null
								&& !regServiceDTO.getVehicleStoppageDetails().getMviReport().isEmpty()) {
							regServiceDTO.getActionDetails().stream()
									.filter(p -> RoleEnum.MVI.getName().equals(p.getRole())).findFirst().get()
									.setStatus(StatusRegistration.APPROVED.getDescription());

						}
					}
					if (StringUtils.isNoneBlank(regServiceDTO.getToken())) {
						vo.setToken(regServiceDTO.getToken());
					}
					Optional<CitizenEnclosuresDTO> imgDetails = citizenEnclosuresDAO
							.findByApplicationNo(applicationSearchVO.getApplicationNo());
					if (imgDetails.isPresent()) {
						CitizenEnclosuresDTO enclosuresImg = imgDetails.get();
						List<KeyValue<String, List<ImageEnclosureDTO>>> enclousersTo = enclosuresImg.getEnclosures();
						List<KeyValue<EnclosureType, List<ImageVO>>> enclosuresInputList = enclosuresLogMapper
								.convertNewEnclosures(enclousersTo);
						vo.setImageList(enclosuresInputList);

					}
					if (regServiceDTO.getActionDetails() != null)
						vo.setActionDetailsList(actionDetailMapper.convertEntity(regServiceDTO.getActionDetails()));

					String transactionNumber = getTransactionDetais(regServiceDTO.getPaymentTransactionNo(),
							regServiceDTO.getApplicationNo());
					if (StringUtils.isNoneBlank(transactionNumber)) {
						vo.setTransactionNumber(transactionNumber);
					}
					if (null != regServiceDTO.getServiceIds()
							&& regServiceDTO.getServiceIds().contains(ServiceEnum.DATAENTRY.getId())) {
						Optional<FeeCorrectionDTO> feeCorrection = feeCorrectionDAO.findByChassisNoAndStatusIsTrue(
								regServiceDTO.getRegistrationDetails().getVahanDetails().getChassisNumber());
						if (feeCorrection.isPresent() && feeCorrection.get().isApproved()
								&& feeCorrection.get().isStatus()) {
							throw new BadRequestException(
									"Please pay difference fee or fee corrections approvals is pending ");
						} else if (feeCorrection.isPresent() && feeCorrection.get().isApproved()
								&& !feeCorrection.get().isStatus()) {
							vo.setDemandNoticeverified(Boolean.TRUE);
						} else if (feeCorrection.isPresent() && !feeCorrection.get().isApproved()
								&& feeCorrection.get().isStatus()) {
							throw new BadRequestException(
									"Please pay difference fee or fee corrections approvals is pending");
						}
						if (null != regServiceDTO.getServiceIds()
								&& regServiceDTO.getServiceIds().contains(ServiceEnum.CHANGEOFADDRESS.getId())
								|| null != regServiceDTO.getServiceIds() && regServiceDTO.getServiceIds()
										.contains(ServiceEnum.TRANSFEROFOWNERSHIP.getId())) {
							vo.setOtherSateCOVTOIsdone(Boolean.TRUE);

							if (regServiceDTO.getOtherStateNOCStatus() != null) {
								vo.setOtherStateNOCStatus(regServiceDTO.getOtherStateNOCStatus());
							}
						}
						if (regServiceDTO.getRegistrationDetails().isRegVehicleWithTR()) {
							vo.setOtherStateNOCStatus(StatusRegistration.APPROVED);
						} else if (!regServiceDTO.getRegistrationDetails().isRegVehicleWithTR()
								&& !regServiceDTO.getRegistrationDetails().isRegVehicleWithPR()) {
							vo.setOtherStateNOCStatus(StatusRegistration.APPROVED);
						}
						vo.setOsPaymentStatus(isApprovedCCOandMVI(regServiceDTO));
						vo.setOsSecondVechicleFoundRTO(regServiceDTO.getOsSecondVechicleFoundRTO());
						vo.setIsPRNoRequiredosSVRejected(regServiceDTO.getIsPRNoRequiredosSVRejected());
					}
					if (null != regServiceDTO.getServiceIds()
							&& regServiceDTO.getServiceIds().contains(ServiceEnum.RCFORFINANCE.getId())) {
						if (regServiceDTO.getFreshRcdetails() != null
								&& regServiceDTO.getFreshRcdetails().getFinancerDetails() != null
								&& StringUtils.isNotEmpty(
										regServiceDTO.getFreshRcdetails().getFinancerDetails().getFirstName())) {
							vo.setApplicantName(regServiceDTO.getFreshRcdetails().getFinancerDetails().getFirstName());
						}
					}
					searchReport.add(vo);
				}
			}
		}
		return searchReport;
	}

	/**
	 * Get Bank Transaction number by Using payment Transaction Number
	 * 
	 * @param paymentTransactionNo
	 * @return
	 */
	private String getTransactionDetais(String paymentTransactionNo, String applicationNumber) {
		Optional<PaymentTransactionDTO> dto = paymentTransactionDAO
				.findByApplicationFormRefNumAndPaymentTransactionNo(applicationNumber, paymentTransactionNo);
		if (dto.isPresent()) {
			return dto.get().getTransactioNo();
		}
		return StringUtils.EMPTY;
	}

	/**
	 * This method is used to fetch the latest record from reg services collection
	 *
	 * @param servicesList
	 * @return
	 */
	private List<RegServiceDTO> getRegistrationServicesLatestRecordBasedOnServicesList(List<RegServiceDTO> servicesList,
			String chassisNo) {
		List<RegServiceDTO> list = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(servicesList)) {
			List<RegServiceDTO> filterList = servicesList.stream().filter(val -> val.getSource() == null)
					.collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(filterList)) {
				filterList.sort((s1, s2) -> s2.getCreatedDate().compareTo(s1.getCreatedDate()));
				RegServiceDTO dto = filterList.stream().findFirst().get();
				if (!dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.BILLATERALTAX.getId()))) {
					if (dto.getRegistrationDetails() == null || dto.getRegistrationDetails().getVahanDetails() == null
							|| StringUtils.isBlank(dto.getRegistrationDetails().getVahanDetails().getChassisNumber())) {
						throw new BadRequestException(
								"Registration details or vahan details not found in citizen document");
					}

					if (!dto.getRegistrationDetails().getVahanDetails().getChassisNumber()
							.equalsIgnoreCase(chassisNo)) {
						logger.info("provided chassis no : [{}], actual chassis no: [{}]", chassisNo,
								dto.getRegistrationDetails().getVahanDetails().getChassisNumber());
						throw new BadRequestException("Please provide correct chassis numbers ");
					}
				} else {
					if (!dto.getBileteralTaxDetails().getChassisNumber().equalsIgnoreCase(chassisNo)) {
						logger.info("provided chassis no : [{}], actual chassis no: [{}]", chassisNo,
								dto.getRegistrationDetails().getVahanDetails().getChassisNumber());
						throw new BadRequestException("Please provide correct chassis numbers ");
					}
				}
				return filterList;
			} else {
				throw new BadRequestException("To download RC copy, Please do any online transaction");
			}
		}
		return list;
	}

	@Override
	public Optional<CitizenSearchReportVO> fetchDetailsFromStagingAndRegistrationDetails(
			ApplicationSearchVO applicationSearchVO) {
		CitizenSearchReportVO searchReportVo = null;
		Optional<StagingRegistrationDetailsDTO> optionalStagingDTO = null;
		Optional<RegistrationDetailsDTO> registrationDTO = null;
		if (StringUtils.isNotBlank(applicationSearchVO.getApplicationNo())) {
			registrationDTO = registrationDetailDAO.findByApplicationNo(applicationSearchVO.getApplicationNo());
		}

		if (StringUtils.isNotBlank(applicationSearchVO.getTrNo())) {
			registrationDTO = registrationDetailDAO.findByTrNo(applicationSearchVO.getTrNo());
		}

		if (StringUtils.isNotBlank(applicationSearchVO.getPrNo())) {
			registrationDTO = registrationDetailDAO.findByPrNo(applicationSearchVO.getPrNo());
		}

		if (registrationDTO.isPresent()) {
			searchReportVo = registrationDetailsMapper
					.convertSpecificFieldsForCitizenSearchForRegistration(registrationDTO.get(), applicationSearchVO);
			if (ObjectUtils.isEmpty(searchReportVo)) {
				String transactionNumber = getTransactionDetais(registrationDTO.get().getPaymentTransactionNo(),
						registrationDTO.get().getApplicationNo());
				if (StringUtils.isNoneBlank(transactionNumber)) {
					searchReportVo.setTransactionNumber(transactionNumber);
				}
				return Optional.of(searchReportVo);
			}
		} else {

			if (StringUtils.isNotBlank(applicationSearchVO.getApplicationNo())) {
				optionalStagingDTO = stagingRegistrationDetailsDAO
						.findByApplicationNo(applicationSearchVO.getApplicationNo());
			}
			if (StringUtils.isNotBlank(applicationSearchVO.getTrNo())) {
				optionalStagingDTO = stagingRegistrationDetailsDAO.findByTrNo(applicationSearchVO.getTrNo());
			}
			if (optionalStagingDTO.isPresent()) {
				CitizenSearchReportVO searchReportVo1 = registrationDetailsMapper
						.convertSpecificFieldsForCitizenSearch(optionalStagingDTO.get(), applicationSearchVO);
				String transactionNumber = getTransactionDetais(optionalStagingDTO.get().getPaymentTransactionNo(),
						optionalStagingDTO.get().getApplicationNo());
				if (StringUtils.isNoneBlank(transactionNumber)) {
					searchReportVo1.setTransactionNumber(transactionNumber);
				}
				return Optional.of(searchReportVo1);
			}
		}

		return Optional.empty();
	}

	private RegServiceVO saveBuyerDoc(RegServiceVO regServiceVO, MultipartFile[] multipart,
			Optional<KeyValue<RegServiceDTO, RegistrationDetailsDTO>> citizenObjectsOptional)
			throws IOException, RcValidationException {
		RegServiceDTO regServiceDto = null;
		Optional<RegistrationDetailsDTO> registrationOptional = registrationDetailDAO
				.findByPrNo(regServiceVO.getPrNo());

		if (!registrationOptional.isPresent()) {
			logger.error("No record found. [{}] ", regServiceVO.getPrNo());
			throw new BadRequestException("No record found.Pr no: " + regServiceVO.getPrNo());
		}
		// validations
		RcValidationVO rcValidationVO = null;
		if (regServiceVO.getTowDetails().getTransferType().equals(TransferType.DEATH)) {
			if (multipart != null && multipart.length == 0) {
				throw new BadRequestException("No images found");
			}
			rcValidationVO = getValidationVO(regServiceVO);
			rcValidationVO.setOwnerType(regServiceVO.getTowDetails().getSeller());
			rcValidationVO.setChassisNo(regServiceVO.getTowDetails().getChassisNo());
		} else {
			rcValidationVO = getValidationVO(regServiceVO);
			rcValidationVO.setOwnerType(regServiceVO.getTowDetails().getBuyer());
			rcValidationVO.setToken(regServiceVO.getTowDetails().getToken());
			rcValidationVO.setOwnerShipType(
					OwnerTypeEnum.getOwnerType(regServiceVO.getTowDetails().getOwnershipType().getCode()));
		}
		try {
			searchWithAadharNoAndRc(rcValidationVO, Boolean.TRUE);
		} catch (RcValidationException e) {
			throw new RcValidationException(e.getErrors());
		}
		regServiceDto = saveDeathDoc(regServiceDto, regServiceVO, registrationOptional.get());
		if (!(TransferType.DEATH.equals(regServiceVO.getTowDetails().getTransferType())
				|| TransferType.AUCTION.equals(regServiceVO.getTowDetails().getTransferType()))) {
			regServiceDto = getLatestRecord(regServiceVO.getPrNo());
			if (regServiceDto.getBuyerDetails().getSellerPermitStatus() != null
					&& TransferType.permitTranfer.PERMITTRANSFER
							.equals(regServiceDto.getBuyerDetails().getSellerPermitStatus())) {
				if (regServiceVO.getPresentAddress().getMandal() != null
						&& regServiceVO.getPresentAddress().getMandal().getMandalCode() != null) {
					OfficeVO vo = getOfficeDetails(regServiceVO.getPresentAddress().getMandal().getMandalCode(), "T");
					if (!registrationOptional.get().getOfficeDetails().getOfficeCode().equals(vo.getOfficeCode())) {
						throw new BadRequestException(
								"Permit Transfer is only available for same office,so please cancel token or select same office");
					}
					if (registrationOptional.get().getClassOfVehicle()
							.equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())
							&& !registrationOptional.get().getVahanDetails().getFuelDesc().equalsIgnoreCase("CNG")) {
						if (registrationOptional.get().getApplicantDetails().getPresentAddress().getMandal() == null
								|| registrationOptional.get().getApplicantDetails().getPresentAddress().getMandal()
										.getMandalCode() == null) {
							throw new BadRequestException("2015 :: Mandal code not available in Seller Deatails");
						}
						if (!registrationOptional.get().getApplicantDetails().getPresentAddress().getMandal()
								.getMandalCode().equals(regServiceVO.getPresentAddress().getMandal().getMandalCode())) {
							permitMandalExemption(regServiceVO.getPresentAddress().getMandal().getMandalCode(),
									registrationOptional.get().getVahanDetails().getFuelDesc());
						}
					}
				} else {
					throw new BadRequestException("2015 :: Mandal code not available in Buyer Deatails");
				}
				if (!regServiceVO.isBuyerPermitStatus()) {
					throw new BadRequestException(
							"seller Transfered permit, so it is mandatory to buyer select transfer of permit");
				}
			}
			buyerValidation(regServiceDto, regServiceVO);
			getPresentAddress(regServiceVO, regServiceDto);
			regServiceDto.getBuyerDetails()
					.setOwnerShipType((ownerShipMapper.convertVO(regServiceVO.getTowDetails().getOwnershipType())));
		}
		if (null == regServiceDto.getBuyerDetails()) {
			throw new BadRequestException("2015 :: Buyer Details not available");
		}
		if (regServiceVO.getInsuranceDetailsVo() != null) {
			regServiceDto.setInsuranceDetails(
					insuranceDetailsMapper.convertVO(doValidateAndSaveInsurenceDetails(regServiceVO, regServiceDto)));
		}
		if (regServiceVO.getPucDetails() != null) {
			regServiceDto.setPucDetails(
					pucDetailsMapper.convertVO(doValidateAndSavePUCDetails(regServiceVO, regServiceDto)));
		}

		List<ServiceEnum> buyerServiceType = new ArrayList<>();
		if (citizenObjectsOptional.isPresent()) {
			RegServiceDTO regServiceDtoCombination = citizenObjectsOptional.get().getKey();

			if (regServiceDtoCombination.getnOCDetails() != null) {
				regServiceDto.getServiceIds().add(ServiceEnum.ISSUEOFNOC.getId());
				buyerServiceType.add(ServiceEnum.ISSUEOFNOC);
				regServiceDto.setnOCDetails(regServiceDtoCombination.getnOCDetails());
			}
			if (regServiceDtoCombination.getAlterationDetails() != null
					|| regServiceDto.getRegistrationDetails().isDataInsertedByDataEntry()
					|| regServiceVO.getServiceIds().contains(ServiceEnum.RENEWAL.getId())) {
				setMviOfficeDetails(regServiceDto, regServiceVO);
				if (regServiceDtoCombination.getAlterationDetails() != null) {
					regServiceDto.getServiceIds().add(ServiceEnum.ALTERATIONOFVEHICLE.getId());
					buyerServiceType.add(ServiceEnum.ALTERATIONOFVEHICLE);
					regServiceDto.setAlterationDetails(regServiceDtoCombination.getAlterationDetails());

					if (regServiceDtoCombination.getServiceIds().contains(ServiceEnum.REASSIGNMENT.getId())) {
						regServiceDto.getServiceIds().add(ServiceEnum.REASSIGNMENT.getId());
					}
				}
				if (regServiceVO.getServiceIds().contains(ServiceEnum.RENEWAL.getId())) {
					regServiceDto.getServiceIds().add(ServiceEnum.RENEWAL.getId());
				}
			}
			if (regServiceDtoCombination.getDuplicateDetails() != null) {
				regServiceDto.setDuplicateDetails(regServiceDtoCombination.getDuplicateDetails());
				regServiceDto.getServiceIds().add(ServiceEnum.DUPLICATE.getId());
			}
			if (regServiceDtoCombination.getFinanceDetails() != null) {
				regServiceDto.setFinanceDetails(regServiceDtoCombination.getFinanceDetails());
				regServiceDto.getServiceIds().add(ServiceEnum.HIREPURCHASETERMINATION.getId());
				if (regServiceDtoCombination.getIsHPTDone() != null && regServiceDtoCombination.getIsHPTDone()) {
					regServiceDto.setIsHPTDone(regServiceDtoCombination.getIsHPTDone());
				}

			}

			if (validateServiceIds(ServiceEnum.HPA.getId(), regServiceVO.getServiceIds())) {
				buyerServiceType.add(ServiceEnum.HPA);
				regServiceDto.getBuyerDetails().setBuyerFinanceStatus(FinanceTowEnum.NEWFINANCE.toString());
				regServiceDto.getServiceIds().add(ServiceEnum.HPA.getId());
				regServiceDto.setToken(regServiceDtoCombination.getToken());
				regServiceDto.setTokenGeneratedDate(regServiceDtoCombination.getTokenGeneratedDate());
			}

		}

		if ((regServiceDto.getBuyerDetails().getSellerFinanceStatus() != null && regServiceDto.getBuyerDetails()
				.getSellerFinanceStatus().equals(FinanceTowEnum.CONTINUEWITHFINANCE.toString()))) {

			if (regServiceVO.getIsContinueFinancier() == null
					|| regServiceVO.getIsContinueFinancier() != FinanceTowEnum.CONTINUEWITHFINANCE.getId()) {
				throw new BadRequestException(
						"Seller selected continue financier so pls continue financier / cancel the TOW token");
			}
			if (!validateServiceIds(ServiceEnum.HPA.getId(), regServiceVO.getServiceIds())) {
				throw new BadRequestException("Please continue the HPA service");
			}
			regServiceDto.getBuyerDetails().setBuyerFinanceStatus(FinanceTowEnum.CONTINUEWITHFINANCE.toString());

		}
		buyerServiceType.add(ServiceEnum.TRANSFEROFOWNERSHIP);
		regServiceDto.getBuyerDetails().setBuyerServiceType(buyerServiceType);
		if (regServiceVO.isBuyerPermitStatus()) {
			regServiceDto.getBuyerDetails().setBuyerPermitStatus(TransferType.permitTranfer.PERMITTRANSFER);
			buyerServiceType.add(ServiceEnum.TRANSFEROFPERMIT);
			regServiceDto.getServiceIds().add(ServiceEnum.TRANSFEROFPERMIT.getId());
			regServiceDto.getServiceType().add(ServiceEnum.TRANSFEROFPERMIT);
			regServiceVO.getServiceIds().add(ServiceEnum.TRANSFEROFPERMIT.getId());
			if (regServiceVO.getPermitDetailsVO() == null) {
				logger.info("Permit Details not sending from UI :[{}]", regServiceVO.getPrNo());
				throw new BadRequestException("Permit Details not available");
			}
			regServiceDto.setPdtl(permitDetailsMapper.convertVO(regServiceVO.getPermitDetailsVO()));

		}

		if (regServiceVO.isRecommendationLetterbuyerStatus()) {
			buyerServiceType.add(ServiceEnum.TRANSFEROFRECOMMENDATIONLETTER);
			regServiceDto.getBuyerDetails()
					.setBuyerRecommedationLetterStatus(TransferType.permitTranfer.RECOMMENDATIONLETTERTRANSFER);
			regServiceDto.getServiceIds().add(ServiceEnum.TRANSFEROFRECOMMENDATIONLETTER.getId());
			regServiceDto.getServiceType().add(ServiceEnum.TRANSFEROFRECOMMENDATIONLETTER);
			regServiceVO.getServiceIds().add(ServiceEnum.TRANSFEROFPERMIT.getId());
		}
		regServiceDto.setServiceIds(regServiceDto.getServiceIds().stream().distinct().collect(Collectors.toSet()));
		regServiceDto.getBuyerDetails().setBuyerUUID(regServiceVO.getTowDetails().getBuyerUUID());

		if (regServiceVO.getSlotDetails() != null && regServiceVO.getSlotDetails().getTestSlotDate() != null) {
			regServiceDto.setSlotDetails(slotDetailsMapper.convertVO(regServiceVO.getSlotDetails()));
		}
		if (regServiceVO.getBasicApplicantDetails() == null) {
			throw new BadRequestException("Basic Information Not Available");
		}
		regServiceVO.getBasicApplicantDetails().setApplicantNo(dealerService.getTransactionId());
		regServiceVO.getBasicApplicantDetails().setPresentAddress(regServiceVO.getPresentAddress());
		Optional<AadhaarDetailsResponseDTO> aadhaarDetailsResponseDTO = getAadhaarDetails(regServiceVO);
		if (aadhaarDetailsResponseDTO.isPresent()) {
			regServiceVO.getBasicApplicantDetails()
					.setAadharResponse(aadhaarDetailsResponseMapper.convertEntity(aadhaarDetailsResponseDTO.get()));
			Long uid = regServiceVO.getBasicApplicantDetails().getAadharResponse().getUid();
			regServiceVO.getBasicApplicantDetails().setAadharNo(uid.toString());
			regServiceDto.getBuyerDetails().setBuyerAadhaarNo(uid.toString());
			regServiceDto.getBuyerDetails().setIsMobileService(regServiceVO.getTowDetails().getIsMobileService());
			regServiceDto.getBuyerDetails()
					.setBuyerUUID(regServiceVO.getBasicApplicantDetails().getAadharResponse().getUuId());
			regServiceVO.getBasicApplicantDetails().setIsAadhaarValidated(Boolean.TRUE);
		}
		regServiceVO.getBasicApplicantDetails().setNationality(NationalityEnum.INDIAN.toString());
		ApplicantDetailsDTO applicantDto = applicantDeatilsMapper.convertVO(regServiceVO.getBasicApplicantDetails());
		regServiceDto.getBuyerDetails().setBuyerApplicantDetails(applicantDto);
		if (regServiceDto.getBuyerDetails().getTokenNoGeneratedTime() != null) {
			regServiceDto.setCreatedDate(regServiceDto.getBuyerDetails().getTokenNoGeneratedTime());
			regServiceDto.setCreatedDateStr(regServiceDto.getBuyerDetails().getTokenNoGeneratedTime().toString());
			regServiceDto.setlUpdate(LocalDateTime.now());
		}
		return regServiceMapper.convertEntity(saveCitizenServiceDoc(regServiceVO, regServiceDto, multipart));
	}

	private void commonModifySlot(LocalDate localDate, RegServiceDTO regDetails) throws UnknownHostException {
		SlotDetailsDTO slotDetails = new SlotDetailsDTO();
		String officeCode = regDetails.getMviOfficeCode();
		if (StringUtils.isNoneBlank(regDetails.getOldMviOfficeCode())) {
			officeCode = regDetails.getOldMviOfficeCode();
			regDetails.setOldMviOfficeCode(null);
		}
		String sloTime = slotService.modifySlot(ModuleEnum.REG.toString(), null, officeCode,
				regDetails.getSlotDetails().getSlotDate(), localDate);

		slotDetails.setSlotTime(sloTime);

		if (regDetails.getSlotDetails() == null) {

			slotDetails.setCreatedDate(LocalDateTime.now());
			slotDetails.setSlotDate(localDate);
			slotDetails.setIpAddress(InetAddress.getLocalHost().getHostAddress().trim());
			regDetails.setSlotDetails(slotDetails);

		} else {

			if (regDetails.getSlotDetailsLog() == null) {
				regDetails.setSlotDetailsLog(new ArrayList<>());
			}
			regDetails.getSlotDetailsLog().add(regDetails.getSlotDetails());

			slotDetails.setCreatedDate(LocalDateTime.now());
			slotDetails.setSlotDate(localDate);
			slotDetails.setIpAddress(InetAddress.getLocalHost().getHostAddress().trim());
			regDetails.setSlotDetails(slotDetails);

		}

		if (Stream
				.of(ServiceEnum.NEWFC.getId(), ServiceEnum.RENEWALFC.getId(), ServiceEnum.OTHERSTATIONFC.getId(),
						ServiceEnum.DATAENTRY.getId())
				.collect(Collectors.toSet()).contains(regDetails.getServiceIds().iterator().next())) {
			Integer templateId = MessageTemplate.REG_FCSLOT.getId();
			notifications.sendNotifications(templateId, regDetails);
			logger.info("SlotModfication  :Notification Processed  [{}] ", regDetails.getApplicationNo());
		} else {
			Integer templateId = MessageTemplate.SLOTMODIFICATION.getId();
			notifications.sendNotifications(templateId, regDetails);
			logger.info("SlotModfication  :Notification Processed  [{}] ", regDetails.getApplicationNo());
		}

	}

	@Override
	public RegServiceVO getDataForModifySlot(String applicationNo) {
		// TODO Auto-generated method stub
		Optional<RegServiceDTO> regOptional = this.findByApplicationNo(applicationNo);
		if (regOptional.isPresent()) {
			RegServiceDTO dto = regOptional.get();
			if (dto.getMviOfficeDetails() == null || dto.getMviOfficeCode() == null) {
				logger.error("No MVI office details in applicationNo: " + applicationNo);
				throw new BadRequestException("No MVI office details in applicationNo: " + applicationNo);
			}
			dto.setOfficeDetails(dto.getMviOfficeDetails());
			dto.getOfficeDetails().setOfficeCode(dto.getMviOfficeCode());

			RegServiceVO vo = regServiceMapper.convertEntity(dto);
			if ((dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWFC.getId()))
					|| dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RENEWALFC.getId()))
					|| dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.OTHERSTATIONFC.getId())))
					&& dto.getServiceIds().size() == 1) {
				if (vo.getReInspectionDate() != null) {
					vo.setReInspectionDate(LocalDate.now());
				}
			}
			return vo;
		} else {
			logger.error("No record found. [{}],[{}] ", applicationNo);
			throw new BadRequestException("No record found.Application no: " + applicationNo);
		}
	}

	public FinanceDetailsVO appendFinancerAddress(FinanceDetailsVO financeVO,
			RegistrationDetailsDTO registrationDetailsDTO) {

		Optional<UserDTO> userDtoFinancier = userDAO
				.findByUserId(registrationDetailsDTO.getFinanceDetails().getUserId());
		if (userDtoFinancier.isPresent()) {
			StringBuilder address = new StringBuilder();
			UserVO financierVO = userMapper.requiredFields(userDtoFinancier.get());
			if (financierVO.getMandal() != null && !StringUtils.isBlank(financierVO.getMandal().getMandalName())) {
				address.append(financierVO.getMandal().getMandalName());
			}
			address.append(",");
			if (financierVO.getDistrict() != null
					&& !StringUtils.isBlank(financierVO.getDistrict().getDistrictName())) {
				address.append(financierVO.getDistrict().getDistrictName());
			}
			address.append(",");
			if (financierVO.getState() != null && !StringUtils.isBlank(financierVO.getState().getStateName())) {
				address.append(financierVO.getState().getStateName());
			}

			financeVO.setAddress(address.toString());
			// registrationDetails.setFinancierVO(financierVO);
		}
		return financeVO;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<RegistrationDetailsVO> findByprNoFromRegistrationDetails(String prNo) {
		Optional<RegistrationDetailsDTO> registrationDetailsDTO = registrationDetailDAO.findByPrNo(prNo);
		if (registrationDetailsDTO.isPresent()) {
			return registrationDetailsMapper.convertEntity(registrationDetailsDTO);
		}
		return Optional.empty();
	}

	@Override
	public void modifySlot(String slotDate, String applicationNo) {

		synchronized (applicationNo.intern()) {

			Optional<RegServiceDTO> stagingoptional = regServiceDAO.findByApplicationNo(applicationNo);
			if (!stagingoptional.isPresent()) {
				logger.error("No regcords found in citizen collection , Application No : [{}] ", applicationNo);
				throw new BadRequestException("No record found in citizen Details for: " + applicationNo);
			}
			// TODO: check class of vehicle.
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
			LocalDate localDate = LocalDate.parse(slotDate, formatter);

			List<String> list = new ArrayList<>();

			list.add(StatusRegistration.CITIZENPAYMENTFAILED.getDescription());
			// list.add(StatusRegistration.TRAILERTRGENERATED.getDescription());
			list.add(StatusRegistration.SLOTBOOKED.getDescription());
			list.add(StatusRegistration.ABSENT.getDescription());
			list.add(StatusRegistration.MVIREJECTED.getDescription());
			list.add(StatusRegistration.PAYMENTDONE.getDescription());
			list.add(StatusRegistration.REJECTED.getDescription());
			list.add(StatusRegistration.CITIZENSUBMITTED.getDescription());
			list.add(StatusRegistration.RTOREJECTED.getDescription());
			list.add(StatusRegistration.RTOAPPROVED.getDescription());

			RegServiceDTO regDetails = stagingoptional.get();
			if ((regDetails.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWFC.getId()))
					|| regDetails.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RENEWALFC.getId()))
					|| regDetails.getServiceIds().stream()
							.anyMatch(id -> id.equals(ServiceEnum.OTHERSTATIONFC.getId())))
					&& regDetails.getServiceIds().size() == 1) {
				if (regDetails.isCfxIssued()) {
					if (localDate.isBefore(regDetails.getReInspectionDate())) {
						logger.error("Please apply on or after  : [{}] ", regDetails.getReInspectionDate(),
								"this date");
						throw new BadRequestException(
								"Please apply on or after  :" + regDetails.getReInspectionDate() + " this date");
					}
				}
			}
			if (!list.stream()
					.anyMatch(status -> status.equalsIgnoreCase(regDetails.getApplicationStatus().toString()))) {
				logger.error("Application Status Is Invalid for Application No : [{}] ", applicationNo);
				throw new BadRequestException("Application Status Is Invalid ");
			}
			if (regDetails.getApplicationStatus().equals(StatusRegistration.MVIREJECTED)
					|| regDetails.getApplicationStatus().equals(StatusRegistration.RTOREJECTED)) {
				if ((regDetails.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.OTHERSTATIONFC.getId())))
						&& (regDetails.isCfxIssued() || !regDetails.isAllowFcForOtherStation())) {
					ServiceEnum.Flow flowId = regDetails.getFlowId();
					regDetails.setFlowId(null);
					Set<Integer> serviceIds = new TreeSet<>();
					Set<Integer> originalServiceIds = regDetails.getServiceIds();
					serviceIds.add(ServiceEnum.RENEWALFC.getId());
					regDetails.setServiceIds(serviceIds);
					registratrionServicesApprovals.initiateApprovalProcessFlow(regDetails);
					regDetails.setServiceIds(originalServiceIds);
					regDetails.setFlowId(flowId);
				} else {
					registratrionServicesApprovals.initiateApprovalProcessFlow(regDetails);
				}
			}
			if (regDetails.getSlotDetails().getSlotDate().equals(localDate)) {
				if (regDetails.getSlotDetails().getPaymentStatus() == null
						|| !regDetails.getSlotDetails().getPaymentStatus().equals(StatusRegistration.PAYMENTFAILED)) {
					logger.error("Slot date should not be same as previous slot date.");
					throw new BadRequestException("Slot date should not be same as previous slot date.");
				}

			}
			try {
				commonModifySlot(localDate, regDetails);
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			regDetails.setApplicationStatus(StatusRegistration.SLOTBOOKED);
			regServiceDAO.save(regDetails);

		}

	}

	public List<String> checkToAllowReassignmentOrNot(RegistrationDetailsDTO registrationOptional, List<String> errors,
			RcValidationVO rcValidationVO) throws RcValidationException {

		if (!registrationOptional.isAllowForReassignment()) {
			logger.error("Application not eligible for Reassignment : [{}]", registrationOptional.getPrNo());
			errors.add("Application not eligible for Reassignment : " + registrationOptional.getPrNo());
		}
		try {
			validationForReassignment(rcValidationVO, errors);
		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());
		}
		if (!errors.isEmpty()) {
			logger.error(" Validation Failed  : [{}]", errors);
			throw new BadRequestException(errors.toString());
		}

		return errors;
	}

	@Override
	public RegServiceDTO saveCitizenServiceDoc(RegServiceVO regServiceVO, RegServiceDTO regServiceDto,
			MultipartFile[] multipart) throws IOException {
		if (multipart.length > 0) {
			saveImages(regServiceVO, regServiceDto, multipart);
		} else {
			if (regServiceDto.getServiceIds().stream().anyMatch(type -> listFoServicesForImages(regServiceVO).stream()
					.anyMatch(serviceType -> serviceType.equals(type)))) {
				throw new BadRequestException("Please upload required images: " + regServiceDto.getApplicationNo());
			}
		}
		saveRegDocument(regServiceDto);
		if (StringUtils.isNotBlank(regServiceVO.getGatewayType())) {
			regServiceDto.setGatewayType(regServiceVO.getGatewayType());
		}
		this.saveCitizenServices(regServiceDto);
		return regServiceDto;
	}

	private RegServiceDTO buyerValidation(RegServiceDTO regServiceDto, RegServiceVO regServiceVO) {

		if (regServiceDto.getBuyerDetails() == null) {
			throw new BadRequestException(
					appMessages.getResponseMessage("Tow details not found: " + regServiceDto.getApplicationNo()));
		}

		if (!regServiceDto.getBuyerDetails().getTokenNo().equalsIgnoreCase(regServiceVO.getTowDetails().getToken())) {
			throw new BadRequestException(appMessages.getResponseMessage(
					"Token miss matched.Please enter valid token..: " + regServiceDto.getApplicationNo()));
		}
		if (!regServiceDto.getBuyerDetails().isTokenStatus()) {
			throw new BadRequestException(
					appMessages.getResponseMessage("Token already used: " + regServiceDto.getApplicationNo()));
		}
		regServiceDto.getBuyerDetails().setBuyer(regServiceVO.getTowDetails().getBuyer());
		regServiceDto.getBuyerDetails().setTokenUsedTime(LocalDateTime.now());
		regServiceDto.getBuyerDetails().setTokenStatus(Boolean.FALSE);

		return regServiceDto;
	}

	@Override
	public RegServiceVO getIssuedNOCData(String applicationNo, String prNo) {
		Optional<RegServiceDTO> regServiceDTOOpt = regServiceDAO.findByApplicationNoAndPrNo(applicationNo, prNo);
		if (!regServiceDTOOpt.isPresent()) {
			logger.error("NOC Details not Found Application No [{}]", applicationNo);
			throw new BadRequestException("NOC Details not Found Application No " + applicationNo);
		}
		return regServiceMapper.convertDtoToVoForNOC(regServiceDTOOpt.get());
	}

	private OfficeVO getOfficeDetails(Integer mandalId, String vehicleType) {

		Optional<OfficeVO> officeDetails = mandalService.getOfficeDetailsByMandal(mandalId, vehicleType);
		if (!officeDetails.isPresent()) {
			logger.error("office details not found for  : ", mandalId);
			throw new BadRequestException("office details not found for  : " + mandalId);
		}
		return officeDetails.get();
	}

	@Override
	public RegServiceDTO getPresentAddress(RegServiceVO regServiceVO, RegServiceDTO regServiceDto) {
		if (regServiceVO.getPresentAddress() == null) {
			throw new BadRequestException("Provide Present Address");
		}
		regServiceDto.setPresentAdderss(applicantAddressMapper.convertVO(regServiceVO.getPresentAddress()));
		OfficeVO vo = getOfficeDetails(regServiceVO.getPresentAddress().getMandal().getMandalCode(),
				regServiceDto.getRegistrationDetails().getVehicleType());
		regServiceDto.setOfficeDetails(officeMapper.convertVO(vo));
		regServiceDto.setOfficeCode(vo.getOfficeCode());
		return regServiceDto;

	}

	private RegServiceDTO getApplicationId(RegServiceDTO regServiceDto) {
		Map<String, String> officeCodeMap = new TreeMap<>();
		officeCodeMap.put("officeCode", regServiceDto.getOfficeCode());
		regServiceDto.setApplicationNo(sequenceGenerator
				.getSequence(String.valueOf(Sequence.REGISTRATIONAPPNO.getSequenceId()), officeCodeMap));
		return regServiceDto;

	}

	@Override
	public boolean getInsuranceValidity(String prNo) {
		RegServiceDTO regServiceDTO = getLatestRecord(prNo);
		if (null != regServiceDTO.getInsuranceDetails() && null != regServiceDTO.getInsuranceDetails().getValidTill()
				&& LocalDate.now().isBefore(regServiceDTO.getInsuranceDetails().getValidTill())) {
			return true;
		}
		return false;

	}

	@Override
	public boolean getPUCValidity(String prNo) {
		RegServiceDTO regServiceDTO = getLatestRecord(prNo);
		if (null != regServiceDTO.getPucDetails() && null != regServiceDTO.getPucDetails().getValidTo()
				&& LocalDate.now().isBefore(regServiceDTO.getPucDetails().getValidTo())) {
			return true;
		}
		if (null == regServiceDTO.getPucDetails()) {
			return !regPUCDetailsValidity(regServiceDTO.getRegistrationDetails());
		}
		return false;

	}

	@Override
	public String createTokenForHPA(RegServiceVO regServiceVO) {

		Optional<RegistrationDetailsDTO> optionalDto = registrationDetailDAO.findByPrNo(regServiceVO.getPrNo());

		String token = String.valueOf(System.currentTimeMillis());

		if (optionalDto.isPresent()) {
			/*
			 * FinanceDetailsDTO financeDetails = optionalDto.get().getFinanceDetails();
			 *
			 * if (financeDetails == null) { financeDetails = new FinanceDetailsDTO(); }
			 *
			 * if (StringUtils.isNoneBlank(financeDetails.getToken())) {
			 * logger.warn(appMessages.getLogMessage(MessageKeys.
			 * FINANCE_TOKEN_ALREADY_EXIST )); throw new
			 * BadRequestException(appMessages.getResponseMessage(MessageKeys.
			 * FINANCE_TOKEN_ALREADY_EXIST)); }
			 *
			 * financeDetails.setToken(token);
			 */
			// regServiceDAO.save(regServiceDTOOptional.get());
		} else {
			logger.warn("PR No not Exist", regServiceVO.getPrNo());
			throw new BadRequestException("PR No not Exist" + regServiceVO.getPrNo());
		}
		return token;

	}

	private RegServiceDTO saveDeathDoc(RegServiceDTO regServiceDto, RegServiceVO regServiceVO,
			RegistrationDetailsDTO registrationOptional) {

		if (TransferType.DEATH.equals(regServiceVO.getTowDetails().getTransferType())) {
			/*
			 * if(!registrationOptional.getOwnerType().equals(OwnerTypeEnum. Individual.
			 * toString())){ throw new
			 * BadRequestException("TOW for Death available,only for Individual vehicles" +
			 * regServiceVO.getPrNo()); }
			 */
			int i = 2;
			for (LegalHiresVO legalHire : regServiceVO.getTowDetails().getLegalHires()) {
				regServiceDto = new RegServiceDTO();
				TrasnferOfOwnerShipDTO trasnferOfOwnerShip = new TrasnferOfOwnerShipDTO();
				List<LegalHiresVO> voList = new ArrayList<>();
				legalHire.setSlNo(i++);
				voList.add(legalHire);
				trasnferOfOwnerShip.setLegalHire(legalHiresMapper.convertVO(voList));
				voList.clear();
				regServiceDto.setRegistrationDetails(registrationOptional);
				regServiceDto.setBuyerDetails(trasnferOfOwnerShip);
				regServiceDto.setCreatedDate(LocalDateTime.now());
				regServiceDto.setCreatedDateStr(LocalDateTime.now().toString());
				getPresentAddress(regServiceVO, regServiceDto);
				getApplicationId(regServiceDto);
				regServiceDAO.save(regServiceDto);
				// saveCitizenServices(regServiceDto);
			}

			regServiceDto = new RegServiceDTO();

			regServiceDto.setRegistrationDetails(registrationOptional);
			TrasnferOfOwnerShipDTO trasnferOfOwnerShipDTO = new TrasnferOfOwnerShipDTO();
			if (null != regServiceVO.getTowDetails().getLegalHires()) {
				trasnferOfOwnerShipDTO
						.setLegalHire(legalHiresMapper.convertVO(regServiceVO.getTowDetails().getLegalHires()));
			}
			getPresentAddress(regServiceVO, regServiceDto);
			getApplicationId(regServiceDto);
			OwnershipVO ownershipVO = new OwnershipVO();
			ownershipVO.setCode(OwnerTypeEnum.Individual.getCode());
			ownershipVO.setDescription(OwnerTypeEnum.Individual.toString());
			trasnferOfOwnerShipDTO.setTransferType(regServiceVO.getTowDetails().getTransferType());
			trasnferOfOwnerShipDTO.setBuyer(regServiceVO.getTowDetails().getBuyer());
			trasnferOfOwnerShipDTO.setOwnerShipType(ownerShipMapper.convertVO(ownershipVO));
			regServiceVO.getBasicApplicantDetails().setNationality(NationalityEnum.INDIAN.toString());
			regServiceDto.setBuyerDetails(trasnferOfOwnerShipDTO);
			regServiceDto.setRegistrationDetails(registrationOptional);
			if (registrationOptional.getApplicantDetails().getPresentAddress() != null
					&& (registrationOptional.getApplicantDetails().getPresentAddress().getMandal() == null
							|| registrationOptional.getApplicantDetails().getPresentAddress().getMandal()
									.getMandalCode() == null)) {
				registrationOptional.getApplicantDetails().getPresentAddress()
						.setMandal(mandalMapper.convertVO(regServiceVO.getPresentAddress().getMandal()));
				registrationOptional.getApplicantDetails().getPresentAddress().setIsDeathMandal(Boolean.TRUE);
			}
			regServiceDto.setServiceIds(regServiceVO.getServiceIds());
			if (regServiceVO.getServiceIds().contains(ServiceEnum.TRANSFEROFPERMIT.getId())
					&& regServiceVO.getSellerPermitStatus() != null
					&& regServiceVO.getSellerPermitStatus().equals(TransferType.permitTranfer.PERMITTRANSFER)) {
				regServiceDto.getBuyerDetails().setSellerPermitStatus(TransferType.permitTranfer.PERMITTRANSFER);
				regServiceDto.getBuyerDetails().setBuyerPermitStatus(TransferType.permitTranfer.PERMITTRANSFER);
			}
			if (regServiceVO.getServiceIds().contains(ServiceEnum.SURRENDEROFPERMIT.getId())
					&& regServiceVO.getSellerPermitStatus() != null
					&& regServiceVO.getSellerPermitStatus().equals(TransferType.permitTranfer.PERMITSURRENDER)) {
				regServiceDto.getBuyerDetails().setSellerPermitStatus(TransferType.permitTranfer.PERMITSURRENDER);
			}
			if (regServiceVO.getSellerRecommendationLetterStatus() != null
					&& regServiceVO.getSellerRecommendationLetterStatus()
							.equals(TransferType.permitTranfer.RECOMMENDATIONLETTERTRANSFER)) {
				regServiceDto.getBuyerDetails()
						.setSellerRecommedationLetterStatus(TransferType.permitTranfer.RECOMMENDATIONLETTERTRANSFER);
				regServiceDto.getBuyerDetails()
						.setBuyerRecommedationLetterStatus(TransferType.permitTranfer.RECOMMENDATIONLETTERTRANSFER);
			}
			if (regServiceVO.getSellerRecommendationLetterStatus() != null
					&& regServiceVO.getSellerRecommendationLetterStatus()
							.equals(TransferType.permitTranfer.RECOMMENDATIONLETTERSURRENDER)) {
				regServiceDto.getBuyerDetails()
						.setSellerRecommedationLetterStatus(TransferType.permitTranfer.RECOMMENDATIONLETTERSURRENDER);
			}
			regServiceDto.setPrNo(regServiceVO.getPrNo());
		} else if (TransferType.AUCTION.equals(regServiceVO.getTowDetails().getTransferType())) {
			regServiceDto = new RegServiceDTO();
			regServiceDto = registrationDetailsMapper.createNew(registrationOptional, regServiceVO);
			regServiceDto.setRegistrationDetails(registrationOptional);

			getPresentAddress(regServiceVO, regServiceDto);
			getApplicationId(regServiceDto);
			TrasnferOfOwnerShipDTO trasnferOfOwnerShipDTO = new TrasnferOfOwnerShipDTO();
			trasnferOfOwnerShipDTO.setTransferType(regServiceVO.getTowDetails().getTransferType());
			trasnferOfOwnerShipDTO.setBuyer(regServiceVO.getTowDetails().getBuyer());
			trasnferOfOwnerShipDTO
					.setOwnerShipType((ownerShipMapper.convertVO(regServiceVO.getTowDetails().getOwnershipType())));
			regServiceVO.getBasicApplicantDetails().setNationality(NationalityEnum.INDIAN.toString());
			regServiceDto.setBuyerDetails(trasnferOfOwnerShipDTO);
			regServiceDto.setRegistrationDetails(registrationOptional);
		}
		return regServiceDto;

	}

	private void doHGA(RegServiceVO input, KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {

		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		RegistrationDetailsDTO regDetails = citizenObjects.getValue();
		final String office = regDetails.getOfficeDetails().getOfficeCode();
		regServiceDetails = generateHPAToken(regServiceDetails, office);
	}

	@Override
	public RegServiceDTO generateHPAToken(RegServiceDTO regSerDTO, String office) {
		String token = StringUtils.EMPTY;
		synchronized (office.intern()) {
			token = office.replaceAll("[a-zA-Z]", "") + String.valueOf(System.currentTimeMillis());
			regSerDTO.setToken(token);
			regSerDTO.setTokenGeneratedDate(LocalDateTime.now());

		}
		return regSerDTO;
	}

	@Override
	public Boolean isOnlineFinance(String userId) {
		Boolean isOnline = false;
		MasterUsersDTO userDTO = masterUsersDAO.findByUserId(userId);
		if (userDTO != null) {
			isOnline = true;
		}
		return isOnline;
	}

	private void doHPT(RegServiceVO input, KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {
		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		RegistrationDetailsDTO regDetails = citizenObjects.getValue();
		if (input.getCitizenPayAcceptance() == null || !input.getCitizenPayAcceptance()) {
			if (input.getServiceIds().contains(ServiceEnum.HIREPURCHASETERMINATION.getId())
					&& (input.getServiceIds().contains(ServiceEnum.SURRENDEROFPERMIT.getId())
							|| input.getServiceIds().contains(ServiceEnum.DUPLICATE.getId())
							|| input.getServiceIds().contains(ServiceEnum.TOSELLER.getId())
							|| (input.getTowDetails() != null
									&& input.getTowDetails().getTransferType().equals(TransferType.DEATH)))) {
				throw new BadRequestException("citizen acceptance is mandatory for payment");
			}
		}
		regServiceDetails.setCitizenPayAcceptance(input.getCitizenPayAcceptance());
		if (regDetails.getFinanceDetails() != null) {
			regServiceDetails.setFinanceDetails(regDetails.getFinanceDetails());
		}
		// for offline finance HPT financier approval is not required but RTA
		// approvals
		// is required
		// offline
		if ((regDetails.getFinanceDetails() != null && regDetails.getFinanceDetails().getUserId() == null)
				|| regDetails.getFinanceDetails() != null && regDetails.getFinanceDetails().getUserId() != null
						&& !isOnlineFinance(regDetails.getFinanceDetails().getUserId())) {
			regServiceDetails.setIsHPTDone(true);
		}
		if (regServiceDetails.getFinanceDetails() != null) {
			regServiceDetails.getFinanceDetails().setTerminateDate(LocalDateTime.now());
		}

		if (regServiceDetails.getServiceIds() != null
				&& regServiceDetails.getServiceIds().contains(ServiceEnum.HIREPURCHASETERMINATION.getId())
				&& regServiceDetails.getServiceIds().contains(ServiceEnum.TRANSFEROFOWNERSHIP.getId())
				&& !regServiceDetails.getServiceIds().contains(ServiceEnum.HPA.getId())
				&& regServiceDetails.getFinanceDetails() != null
				&& regServiceDetails.getFinanceDetails().getUserId() != null
				&& isOnlineFinance(regServiceDetails.getFinanceDetails().getUserId())) {
			regServiceDetails.setHptStatus(StatusRegistration.HPTINITIATED.getDescription());

		}

	}

	@Override
	public boolean regInsuranceValidity(RegistrationDetailsDTO regDeatilsDTO) {
		List<OwnerTypeEnum> skipOwnerTypes = new ArrayList<>();
		skipOwnerTypes.add(OwnerTypeEnum.Government);
		skipOwnerTypes.add(OwnerTypeEnum.POLICE);
		skipOwnerTypes.add(OwnerTypeEnum.Stu);
		if (null != regDeatilsDTO.getOwnerType() && skipOwnerTypes.contains(regDeatilsDTO.getOwnerType())) {
			return false;
		}
		if (null != regDeatilsDTO.getInsuranceDetails() && null != regDeatilsDTO.getInsuranceDetails().getValidTill()
				&& LocalDate.now().isBefore(regDeatilsDTO.getInsuranceDetails().getValidTill())) {
			return false;
		}

		return true;
	}

	/*
	 * public boolean regPUCValidity(RegistrationDetailsDTO regDeatilsDTO) {
	 * RegServiceDTO regServiceDTO = getLatestRecord(prNo); if (null !=
	 * regServiceDTO.getPucDetails() && null !=
	 * regServiceDTO.getPucDetails().getValidTo() &&
	 * LocalDate.now().isBefore(regServiceDTO.getPucDetails().getValidTo())) {
	 * return true; } return false;
	 *
	 * }
	 */
	@Override
	public Boolean regPUCDetailsValidity(RegistrationDetailsDTO registrationDetailsDTO) {
		int noOfMonths = 0;
		int vehicleAgeinMonths = 0;

		if (null != registrationDetailsDTO.getRegistrationValidity().getPrGeneratedDate()) {
			LocalDate prValidFrom = registrationDetailsDTO.getRegistrationValidity().getPrGeneratedDate();
			Period period = Period.between(prValidFrom, LocalDate.now());
			int vehicleAgeYears = period.getYears();
			noOfMonths = vehicleAgeYears * 12;
			vehicleAgeinMonths = period.getMonths() + noOfMonths;
		}
		if (vehicleAgeinMonths < 12) {
			return false;
		}
		if (null != registrationDetailsDTO.getPucDetailsDTO()
				&& null != registrationDetailsDTO.getPucDetailsDTO().getValidTo()
				&& LocalDate.now().isBefore(registrationDetailsDTO.getPucDetailsDTO().getValidTo())) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Optional<RegistrationDetailsVO> applicationSearchForFC(ApplicationSearchVO applicationSearchVO) {
		Optional<RegistrationDetailsVO> result = null;
		Optional<RegistrationDetailsDTO> output = null;
		if (StringUtils.isBlank(applicationSearchVO.getChassisNo())) {
			throw new BadRequestException("Please provide correct chassis numbers ");
		}
		if (null != applicationSearchVO.getPrNo()) {
			output = registrationDetailDAO.findByPrNo(applicationSearchVO.getPrNo());
			if (output.isPresent() && output.get().getVehicleType().equals("T"))
				result = registrationDetailsMapper.convertEntity(output);
			else
				throw new BadRequestException("Please provide Valid Pr Number.");
		}
		return result;
	}

	private void doNewFC(RegServiceVO input, KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {

		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		if (null != input.getReasonForChangeOffice()) {
			regServiceDetails.setReasonForChangeOffice(input.getReasonForChangeOffice());
		}
		List<FcDetailsDTO> listOfFc = fcDetailsDAO
				.findFirst5ByStatusIsTrueAndPrNoOrderByCreatedDateDesc(citizenObjects.getValue().getPrNo());
		if (!listOfFc.isEmpty()) {
			regServiceDetails.setServiceIds(Stream.of(ServiceEnum.RENEWALFC.getId()).collect(Collectors.toSet()));
			regServiceDetails.setServiceType(Arrays.asList(ServiceEnum.RENEWALFC));
		}

		if (null != input.getReasonForChangeOffice() && input.getNewOfficeDetails() != null
				&& input.getNewOfficeDetails().getOfficeCode() != null) {
			regServiceDetails.setFitnessOtherStation(Boolean.TRUE);
			regServiceDetails.setFlowId(ServiceEnum.Flow.NEWFCWITHOTHER);
			regServiceDetails.setAllowFcForOtherStation(Boolean.TRUE);
			regServiceDetails.setServiceIds(Stream.of(ServiceEnum.OTHERSTATIONFC.getId()).collect(Collectors.toSet()));
			regServiceDetails.setServiceType(Arrays.asList(ServiceEnum.OTHERSTATIONFC));
		}

	}

	private Optional<PermitDetailsDTO> fetchPermitDetails(String prNo) {

		return permitDetailsDAO.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(prNo,
				PermitType.PRIMARY.getPermitTypeCode(), PermitsEnum.ACTIVE.getDescription());
	}

	private List<PermitDetailsDTO> temporaryPermitValidation(String prNo) {
		return permitDetailsDAO.findByPrNoAndPermitStatus(prNo, PermitsEnum.ACTIVE.getDescription());
	}

	private void isDataEntry(RegistrationDetailsVO registrationDetails, SearchVo vo) {
		// After Data Entry citizen will apply only TO/COA
		if (null != registrationDetails.getServiceIds()
				&& registrationDetails.getServiceIds().contains(ServiceEnum.CHANGEOFADDRESS.getId())) {
			registrationDetails.setTOWSlotRequried(false);
		}
		// for otherstate applications no need MVI Inspection
		registrationDetails.setTOWSlotRequried(false);

		// get latest record from the regservixce for dataentryand check
		// validity
		getLatestRecord(registrationDetails.getPrNo());

	}

	private Boolean validateServiceIds(Integer serviceId, Set<Integer> rcValidationVO) {
		return rcValidationVO.stream().anyMatch(id -> id.equals(serviceId));
	}

	@Override
	public SearchVo applicationSearchForFc(ApplicationSearchVO applicationSearchVO) throws RcValidationException {
		Integer mandalCode = null;
		RcValidationVO rcValidationVO = new RcValidationVO();
		rcValidationVO.setPrNo(applicationSearchVO.getPrNo());
		rcValidationVO.setServiceIds(Stream.of(ServiceEnum.NEWFC.getId()).collect(Collectors.toSet()));

		Optional<RegistrationDetailsDTO> registrationDetailsDTO = registrationDetailDAO
				.findByPrNo(applicationSearchVO.getPrNo());
		if (!registrationDetailsDTO.isPresent()) {
			logger.error("No records found in registration details: " + applicationSearchVO.getPrNo());
			throw new BadRequestException("No records found in registration details: " + applicationSearchVO.getPrNo());
		}
		RegistrationDetailsDTO dto = registrationDetailsDTO.get();
		if (dto.getVahanDetails() == null || StringUtils.isBlank(dto.getVahanDetails().getChassisNumber())) {
			logger.error("Registration details or vahan details not found in citizen document. "
					+ applicationSearchVO.getPrNo());
			throw new BadRequestException("Registration details or vahan details not found in citizen document. "
					+ applicationSearchVO.getPrNo());
		}
		if (!dto.getVahanDetails().getChassisNumber().equalsIgnoreCase(applicationSearchVO.getChassisNo())) {
			logger.error("provided chassis no : [{}], actual chassis no: [{}]", applicationSearchVO.getChassisNo(),
					dto.getVahanDetails().getChassisNumber());
			throw new BadRequestException("Please provide correct chassis numbers ");
		}
		if (!dto.getApplicantDetails().getIsAadhaarValidated() || dto.getApplicantDetails().getAadharNo() == null) {
			logger.error(
					"Please select aadhar seeding service to Seed your aadhar number." + applicationSearchVO.getPrNo());
			throw new BadRequestException(
					"Please select aadhar seeding service to Seed your aadhar number." + applicationSearchVO.getPrNo());
		}
		if (dto.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.EIBT.getCovCode())) {
			Optional<EductaionInstituteVehicleDetailsDto> optionalEibt = eductaionInstituteVehicleDetailsDao
					.findByprNo(dto.getPrNo());
			if (!optionalEibt.isPresent()) {
				logger.error("Please register education bus at: https://aprtaregistrations.epragathi.org/#/signup. "
						+ applicationSearchVO.getPrNo());
				throw new BadRequestException(
						"Please register education bus at: https://aprtaregistrations.epragathi.org/#/signup. "
								+ applicationSearchVO.getPrNo());
			}
			EductaionInstituteVehicleDetailsDto eibDto = optionalEibt.get();
			if (eibDto.getStudentDetails() == null || eibDto.getStudentDetails().isEmpty()
					|| eibDto.getEnclosures() == null || eibDto.getEnclosures().isEmpty()) {
				logger.error("Please update student details and enclosures. " + applicationSearchVO.getPrNo());
				throw new BadRequestException(
						"Please update student details and enclosures. " + applicationSearchVO.getPrNo());
			} else if (eibDto.isTowDone()) {
				logger.error("Transfer of Ownership done for this vehicle .Please register the vehicle. "
						+ applicationSearchVO.getPrNo());
				throw new BadRequestException(
						"Transfer of Ownership done for this vehicle .Please register the vehicle. "
								+ applicationSearchVO.getPrNo());
			}
		}
		SearchVo result = null;
		result = this.searchWithAadharNoAndRc(rcValidationVO, Boolean.FALSE);

		if (null == result) {
			logger.error("RC Number is not exists:" + applicationSearchVO.getPrNo());
			throw new BadRequestException("RC Number is not exists:" + applicationSearchVO.getPrNo());
		}
		if (result.getRegistrationDetails().getApplicantDetails().getPresentAddress().getMandal() == null
				&& applicationSearchVO.getMandal() == null) {
			result.setIsMandalNotExist(true);
			return result;
		}

		if (result.getRegistrationDetails().getApplicantDetails().getPresentAddress().getMandal() != null
				&& result.getRegistrationDetails().getApplicantDetails().getPresentAddress().getMandal()
						.getMandalCode() != null) {
			mandalCode = result.getRegistrationDetails().getApplicantDetails().getPresentAddress().getMandal()
					.getMandalCode();
		} else {
			mandalCode = applicationSearchVO.getMandal().getMandalCode();
		}

		if (mandalCode == null) {
			logger.error("Mandal not Found for prNo" + mandalCode);
			throw new BadRequestException("Mandal not Found for prNo" + mandalCode);
		}

		Pair<OfficeVO, String> vo = this.getOffice(mandalCode, result.getRegistrationDetails().getVehicleType(),
				result.getRegistrationDetails().getOwnerType().toString(), StringUtils.EMPTY);
		result.getRegistrationDetails().setOfficeDetails(vo.getFirst());
		return result;
	}

	@Override
	public boolean isToPayLateFeeForFC(String applicationNo, String slotDate, Boolean isToPay) {

		Optional<RegServiceDTO> optionalRegDto = regServiceDAO.findByApplicationNo(applicationNo);
		if (!optionalRegDto.isPresent()) {
			logger.error("No records found for application No: " + applicationNo);
			throw new BadRequestException("No records found for application No: " + applicationNo);
		}
		RegServiceDTO dto = optionalRegDto.get();
		if (dto.getSlotDetails() == null) {
			logger.error("slot details not found for application No: " + applicationNo);
			throw new BadRequestException("slot details not found for application No: " + applicationNo);
		}
		if (dto.isCfxIssued()) {
			return Boolean.FALSE;
		}
		if (!dto.isPaidPyamentsForFC()) {
			return Boolean.TRUE;
		}
		DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		LocalDate localDate = LocalDate.parse(slotDate, df);
		LocalDate fcValidity = null;
		if (dto.getRegistrationDetails().getRegistrationValidity().getFcValidity() != null) {
			fcValidity = dto.getRegistrationDetails().getRegistrationValidity().getFcValidity();
		} else {
			List<FcDetailsDTO> listOfDetails = fcDetailsDAO
					.findFirst5ByStatusIsTrueAndPrNoOrderByCreatedDateDesc(dto.getPrNo());
			if (listOfDetails != null && !listOfDetails.isEmpty()) {
				FcDetailsDTO fcDto = listOfDetails.stream().findFirst().get();
				if (fcDto.getFcValidUpto() == null) {
					logger.error("FC validty missing for : " + dto.getPrNo());
					throw new BadRequestException("FC validty missing for : " + dto.getPrNo());
				}
				fcValidity = fcDto.getFcValidUpto();
			}
		}
		if (fcValidity != null && fcValidity.isBefore(localDate)) {
			if (dto.getSlotDetailsLog() == null) {
				if (dto.getSlotDetails().getPaymentStatus() != null
						&& dto.getSlotDetails().getPaymentStatus().equals(StatusRegistration.PAYMENTFAILED)) {
					return Boolean.TRUE;
				}
				if (localDate.isAfter(dto.getSlotDetails().getSlotDate())) {
					return Boolean.TRUE;
				}

			}
			LocalDate privesSlotDate = null;

			if (dto.getSlotDetailsLog() == null) {
				if (!isToPay) {
					if (dto.getSlotDetails().getPaymentStatus() == null
							|| !dto.getSlotDetails().getPaymentStatus().equals(StatusRegistration.PAYMENTFAILED)) {
						privesSlotDate = dto.getSlotDetails().getSlotDate();
					}
				}
			}
			if (dto.getSlotDetailsLog() != null) {

				if (isToPay) {
					privesSlotDate = getOldSlotDate(dto, privesSlotDate);
				} else {
					if (dto.getSlotDetails().getPaymentStatus() == null
							|| !dto.getSlotDetails().getPaymentStatus().equals(StatusRegistration.PAYMENTFAILED)) {
						privesSlotDate = dto.getSlotDetails().getSlotDate();
					}
					privesSlotDate = getOldSlotDate(dto, privesSlotDate);

				}

				if (localDate.isAfter(privesSlotDate) || localDate.equals(privesSlotDate)) {
					return Boolean.TRUE;
				}
			}
		}
		return Boolean.FALSE;
	}

	private LocalDate getOldSlotDate(RegServiceDTO dto, LocalDate privesSlotDate1) {
		if (privesSlotDate1 == null) {
			for (SlotDetailsDTO slotDto : dto.getSlotDetailsLog()) {
				if (slotDto.getPaymentStatus() == null
						|| !slotDto.getPaymentStatus().equals(StatusRegistration.PAYMENTFAILED)) {
					privesSlotDate1 = slotDto.getSlotDate();
				}
			}
		}
		if (privesSlotDate1 != null) {
			for (SlotDetailsDTO slots : dto.getSlotDetailsLog()) {
				if (!privesSlotDate1.isAfter(slots.getSlotDate())) {
					if (slots.getPaymentStatus() != null
							&& slots.getPaymentStatus().equals(StatusRegistration.PAYMENTFAILED)) {
						continue;
					}
					privesSlotDate1 = slots.getSlotDate();
				}
			}
		}
		if (privesSlotDate1 == null) {
			if (dto.getRegistrationDetails().getRegistrationValidity().getFcValidity() == null) {
				List<FcDetailsDTO> fcDetailsList = fcDetailsDAO
						.findFirst5ByStatusIsTrueAndPrNoOrderByCreatedDateDesc(dto.getPrNo());
				if (fcDetailsList.isEmpty()) {
					return null;
				}
				FcDetailsDTO fcDetailsDTO = fcDetailsList.stream().findFirst().get();
				if (fcDetailsDTO.getFcValidUpto() == null) {
					throw new BadRequestException("Fc validUpto not found for prNo :" + dto.getPrNo());
				}
				dto.getRegistrationDetails().getRegistrationValidity().setFcValidity(fcDetailsDTO.getFcValidUpto());
			}
			privesSlotDate1 = dto.getRegistrationDetails().getRegistrationValidity().getFcValidity();
		}
		return privesSlotDate1;
	}

	public String getTransactionNumber(TransactionDetailVO transactionDetailVO, String applicationNo) {
		String uuid = java.util.UUID.randomUUID().toString();

		if (GatewayTypeEnum.CFMS.equals(transactionDetailVO.getGatewayTypeEnum())
				&& StringUtils.isNotBlank(applicationNo)) {
			return ModuleEnum.REG + "_" + transactionDetailVO.getModule() + "_" + applicationNo + "_"
					+ convertingStringToLocalDateTime(LocalDateTime.now());
		} else if (GatewayTypeEnum.CFMS.equals(transactionDetailVO.getGatewayTypeEnum())
				&& StringUtils.isBlank(applicationNo)) {
			return ModuleEnum.REG + "_" + transactionDetailVO.getModule() + "_"
					+ convertingStringToLocalDateTime(LocalDateTime.now());
		}
		return uuid;

	}

	public String convertingStringToLocalDateTime(LocalDateTime date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("ddMMyyHHmmssSSS");
		return date.format(formatter);
	}

	@Override
	public boolean isPermitTransferRequired(String mandalId, String prNo) {
		String vehicleType = "T";
		OfficeVO vo = getOfficeDetails(Integer.parseInt(mandalId), vehicleType);
		RegServiceDTO regService = getLatestRecordToTrnasferPermit(prNo);
		if (regService == null) {
			Optional<RegistrationDetailsDTO> registrationDetailsDTO = registrationDetailDAO.findByPrNo(prNo);
			if (registrationDetailsDTO.isPresent()) {
				regService = new RegServiceDTO();
				regService.setRegistrationDetails(registrationDetailsDTO.get());
			}
		}
		if (regService != null && regService.getRegistrationDetails().getOfficeDetails() != null
				&& regService.getRegistrationDetails().getOfficeDetails().getOfficeCode().equals(vo.getOfficeCode())) {
			return true;
		}
		return false;

	}

	private void doExtensionOfValidity(RegServiceVO input,
			KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {
		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		List<PermitDetailsVO> vo = input.getPermitDetailsListVO();
		if (CollectionUtils.isEmpty(vo)) {

		}
		PermitDetailsVO permitVO = vo.get(0);
		permitsService.checkForExtensionOfValidity(permitVO);
		permitsService.checExtensionValidtiykWithTaxExpairyDays(permitVO, regServiceDetails.getPrNo());
		List<PermitDetailsDTO> dto = permitDetailsMapper.convertVO(input.getPermitDetailsListVO());
		regServiceDetails.setPermitDetailsListDTO(dto);
	}

	private Optional<KeyValue<RegServiceDTO, RegistrationDetailsDTO>> getTowBuyerCombinationServices(
			Optional<KeyValue<RegServiceDTO, RegistrationDetailsDTO>> citizenObjectsOptional,
			RegServiceVO regServiceVO) {
		Set<Integer> serviceIds = regServiceVO.getServiceIds();
		if (serviceIds.size() == 1) {
			return citizenObjectsOptional;
		}

		if (serviceIds.contains(ServiceEnum.ISSUEOFNOC.getId())) {
			citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO, this::doNoc);
		}
		if (serviceIds.contains(ServiceEnum.ALTERATIONOFVEHICLE.getId())) {
			citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO, this::doAlt);
		}
		if (serviceIds.contains(ServiceEnum.HPA.getId())) {
			citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO, this::doHGA);
		}
		if (regServiceVO.getTowDetails().getTransferType().equals(TransferType.DEATH)) {
			if (serviceIds.contains(ServiceEnum.DUPLICATE.getId())) {
				citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO, this::doDuplicate);
			}
			if (serviceIds.contains(ServiceEnum.HIREPURCHASETERMINATION.getId())) {
				citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO, this::doHPT);
			}
			if (serviceIds.contains(ServiceEnum.RENEWAL.getId())) {
				citizenObjectsOptional = doCitizenService(citizenObjectsOptional, regServiceVO, this::doRenewal);
			}
		}
		return citizenObjectsOptional;
	}

	private void doReplacementOfVehicle(RegServiceVO input,
			KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {
		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		PermitDetailsDTO dto = permitDetailsMapper.convertVO(input.getPermitDetailsVO());
		regServiceDetails.setPdtl(dto);
	}

	@Override
	public Optional<RegistrationDetailsVO> getPermitDetails(String prNo) {
		Optional<RegistrationDetailsDTO> registrationDetailsDTO = registrationDetailDAO.findByPrNo(prNo);
		if (!registrationDetailsDTO.isPresent()) {
			throw new BadRequestException("No Record Found With PrNo " + prNo);
		}
		if (!registrationDetailsDTO.get().getVehicleType().equals(CovCategory.T.getCode())) {
			throw new BadRequestException("Invalid Vehicle Type" + prNo);
		}
		permitsService.findSecondPermitActiveRecords(prNo);
		Boolean taxpending = checkIsTaxPending(registrationDetailsDTO.get());
		if (taxpending) {
			throw new BadRequestException("Tax pending on this vehicle " + prNo);
		}
		return Optional.of(registrationDetailsMapper.convertEntity(registrationDetailsDTO.get()));
	}

	@Override
	public RegistrationDetailsVO applicationSearchForTax(String prNo, String chassisno, Boolean isMobile) {

		Optional<RegistrationDetailsDTO> regDetails = registrationDetailDAO.findByPrNo(prNo);
		if (!regDetails.isPresent()) {
			logger.error("No record found for prNo..[{}]", prNo);
			throw new BadRequestException("No record found for prNo: " + prNo);
		}
		RegistrationDetailsDTO dto = regDetails.get();

		if (!skipAadharValidationForTax()) {
			if (!dto.getApplicantDetails().getIsAadhaarValidated() || dto.getApplicantDetails().getAadharNo() == null) {
				logger.error("Please select aadhar seeding service to Seed your aadhar number");
				throw new BadRequestException("Please select aadhar seeding service to Seed your aadhar number");
			}
		}
		if (isMobile.equals(Boolean.FALSE)) {
			if (StringUtils.isBlank(chassisno)) {
				logger.error("Please provide correct chassis numbers ");
				throw new BadRequestException("Please provide correct chassis numbers ");
			}
			if (!dto.getVahanDetails().getChassisNumber().equalsIgnoreCase(chassisno)) {
				logger.error("provided chassis no : [{}], actual chassis no: [{}]", chassisno,
						dto.getVahanDetails().getChassisNumber());
				throw new BadRequestException("Please enter correct chassis numbers ");
			}
		}
		Optional<MasterPayperiodDTO> Payperiod = masterPayperiodDAO.findByCovcode(dto.getClassOfVehicle());
		if (!Payperiod.isPresent()) {
			logger.error("No record found in pay period for:[{}] " + dto.getClassOfVehicle());
			throw new BadRequestException(
					"No record found in master_payperiod for class of vehicle: " + dto.getClassOfVehicle());
		}

		/*
		 * if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.BOTH.
		 * getCode())) { if
		 * (Integer.parseInt(dto.getVahanDetails().getSeatingCapacity()) > 10) {
		 * Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode()); } else {
		 * Payperiod.get().setPayperiod(TaxTypeEnum.LifeTax.getCode()); } }
		 */
		Pair<Optional<MasterPayperiodDTO>, Boolean> payperiodAndGoStatus = citizenTaxService.getPayPeroidForBoth(
				Payperiod, dto.getVahanDetails().getSeatingCapacity(), dto.getVahanDetails().getGvw());
		if (payperiodAndGoStatus.getSecond()) {
			List<TaxDetailsDTO> listOfTaxDetails = taxDetailsDAO
					.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(dto.getApplicationNo(),
							Arrays.asList(TaxTypeEnum.LifeTax.getDesc()));
			if (!listOfTaxDetails.isEmpty()) {
				for (TaxDetailsDTO taxdto : listOfTaxDetails) {
					if (dto.getClassOfVehicle().equalsIgnoreCase(taxdto.getClassOfVehicle())) {
						listOfTaxDetails.clear();
						logger.error("Vehicle is not eligible to pay tax : " + dto.getPrNo());
						throw new BadRequestException("Vehicle is not eligible to pay tax : " + dto.getPrNo());
					}
				}
				listOfTaxDetails.clear();
			}
		} else {
			Optional<PropertiesDTO> optionalProperties = propertiesDAO.findByAllowQuaterTaxTrue();
			PropertiesDTO properties = optionalProperties.get();
			if (properties.getCovs().stream().anyMatch(cov -> cov.equalsIgnoreCase(dto.getClassOfVehicle()))) {
				// if
				// (dto.getRegistrationValidity().getPrGeneratedDate().isBefore(properties.getPrGeneratedDate()))
				// {
				List<TaxDetailsDTO> listOfTaxDetails = taxDetailsDAO
						.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(dto.getApplicationNo(),
								Arrays.asList(TaxTypeEnum.LifeTax.getDesc()));
				if (listOfTaxDetails != null && !listOfTaxDetails.isEmpty()) {
					// for (TaxDetailsDTO taxdto : listOfTaxDetails) {
					// if (dto.getClassOfVehicle().equalsIgnoreCase(taxdto.getClassOfVehicle())) {
					logger.error("Vehicle is not eligible to pay tax : " + dto.getPrNo());
					throw new BadRequestException("Vehicle is not eligible to pay tax : " + dto.getPrNo());
					// }
					// }
				}
				Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());
				// }
			}
			if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.LifeTax.getCode())) {
				logger.error("Vehicle is not eligible to pay tax : " + dto.getPrNo());
				throw new BadRequestException("Vehicle is not eligible to pay tax : " + dto.getPrNo());
			}
		}
		// validations like vehicle suspend or not .
		List<String> errors = new ArrayList<>();
		Set<Integer> serviceIds = new HashSet<>();
		serviceIds.add(ServiceEnum.TAXATION.getId());
		this.checkVcrDues(dto, errors);
		this.verifyNoc(errors, prNo, serviceIds);
		if (!errors.isEmpty()) {
			logger.error("[{}]", errors.get(0));
			throw new BadRequestException(errors.get(0));
		}
		this.isVehicleStopped(dto, errors);
		if (errors != null && !errors.isEmpty()) {
			logger.error(errors.stream().findFirst().get());
			throw new BadRequestException(errors.stream().findFirst().get());
		}
		List<RegServiceDTO> listOfRegService = regServiceDAO
				.findByRegistrationDetailsApplicationNoAndServiceIdsAndSourceIsNull(dto.getApplicationNo(),
						ServiceEnum.TAXATION.getId());
		if (!listOfRegService.isEmpty()) {
			listOfRegService.sort((s1, s2) -> s2.getCreatedDate().compareTo(s1.getCreatedDate()));
			RegServiceDTO regDto = listOfRegService.stream().findFirst().get();
			List<StatusRegistration> listOfStatus = new ArrayList<>();
			listOfStatus.add(StatusRegistration.CITIZENPAYMENTFAILED);
			listOfStatus.add(StatusRegistration.PAYMENTPENDING);
			if (listOfStatus.contains(regDto.getApplicationStatus())) {
				logger.error("Application is in progress. Application No: " + regDto.getApplicationNo()
						+ ". Please verify the status of the application in registration search");
				throw new BadRequestException("Application is in progress. Application No: " + regDto.getApplicationNo()
						+ ". Please verify the status of the application in registration search");
			}

		}
		dto.getApplicantDetails().setAadharResponse(null);
		RegistrationDetailsVO regVo = registrationDetailsMapper.convertEntity(dto);
		if (regVo.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.ARVT.getCovCode())) {
			if (regVo.getVahanDetails() != null && regVo.getVahanDetails().getTrailerChassisDetailsVO() != null) {
				Integer gtw = regVo.getVahanDetails().getTrailerChassisDetailsVO().stream().findFirst().get().getGtw();
				for (TrailerChassisDetailsVO trailerDetails : regVo.getVahanDetails().getTrailerChassisDetailsVO()) {
					if (trailerDetails.getGtw() > gtw) {
						gtw = trailerDetails.getGtw();
					}
				}
				Integer rlw = regVo.getVahanDetails().getGvw() + gtw;
				regVo.getVahanDetails().setGvw(rlw);
			}
		}
		RcValidationVO rcValidationVO = new RcValidationVO();
		SearchVo vo = new SearchVo();
		rcValidationVO.setServiceIds(Stream.of(ServiceEnum.TAXATION.getId()).collect(Collectors.toSet()));
		rcValidationVO.setPrNo(regVo.getPrNo());
		getSearchResult(rcValidationVO, dto, vo);
		if (vo.getPermitDetailsVO() != null) {
			regVo.setPermitDetailsVO(vo.getPermitDetailsVO());
		}
		if (vo.getRegistrationDetails().getTaxPaidDate() != null) {
			regVo.setTaxPaidDate(vo.getRegistrationDetails().getTaxPaidDate());
		}
		if (vo.getRegistrationDetails().getTaxvalidity() != null) {
			regVo.setTaxvalidity(vo.getRegistrationDetails().getTaxvalidity());
		}
		return regVo;
	}

	private void doDiffTAx(RegServiceVO input, KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {

		RegistrationDetailsDTO regDetails = citizenObjects.getValue();
		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		if (input.getPresentAddress() != null) {
			regServiceDetails.setPresentAdderss(addressMapper.convertVO(input.getPresentAddress()));
		}
		if (input.isWeightAlt()) {
			regServiceDetails.setWeightAlt(input.isWeightAlt());
			regDetails.getVahanDetails().setGvw(regDetails.getVahanDetails().getOldGvw());
			Integer gvw = regDetails.getVahanDetails().getGvw();
			if (regDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.ARVT.getCovCode())) {
				if (regDetails.getVahanDetails().getTrailerChassisDetailsDTO() != null
						&& !regDetails.getVahanDetails().getTrailerChassisDetailsDTO().isEmpty()) {

					Integer gtw = regDetails.getVahanDetails().getTrailerChassisDetailsDTO().stream().findFirst().get()
							.getGtw();
					for (TrailerChassisDetailsDTO trailerDetails : regDetails.getVahanDetails()
							.getTrailerChassisDetailsDTO()) {
						if (trailerDetails.getGtw() > gtw) {
							gtw = trailerDetails.getGtw();
						}
					}
					gvw = gvw + gtw;
				}
			}
			// TODO get the weight type form master
			Optional<MasterWeightsForAlt> optionalWeigts = masterWeightsForAltDAO
					.findByToGvwGreaterThanEqualAndFromGvwLessThanEqualAndStatusIsTrue(gvw, gvw);
			if (!optionalWeigts.isPresent()) {
				throw new BadRequestException("Vehicle not eligible to change weight: " + regDetails.getPrNo());
			}
			regServiceDetails.setGvw(optionalWeigts.get().getGvw());
		}
		List<String> errors = new ArrayList<>();

		checkVcrDues(regDetails, errors);
		if (!errors.isEmpty()) {
			logger.error(errors.get(0));
			throw new BadRequestException(errors.get(0));
		}
		// setMviOfficeDetails(regServiceDetails);
	}

	private void doVehicleStoppage(RegServiceVO input, KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {

		RegistrationDetailsDTO regDetails = citizenObjects.getValue();
		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		if (input.getVehicleStoppageDetailsVO() == null || input.getVehicleStoppageDetailsVO().getStoppageDate() == null
				|| StringUtils.isBlank(input.getVehicleStoppageDetailsVO().getReasonForStoppage())
				|| input.getPresentAddress() == null) {
			throw new BadRequestException("stoppage details not found" + regDetails.getPrNo());
		}

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-M-yyyy");

		String date1 = "01-" + String.valueOf(LocalDate.now().getMonthValue()) + "-"
				+ String.valueOf(LocalDate.now().getYear());
		LocalDate montstartinDate = LocalDate.parse(date1, formatter);
		if (checkIsTaxPending(regDetails)) {
			throw new BadRequestException("Please pay the tax: " + regDetails.getPrNo());
		}
		if (input.getVehicleStoppageDetailsVO().getStoppageDate().isBefore(montstartinDate)) {
			throw new BadRequestException(
					"please give stopage date with in the month and not future date: " + regDetails.getPrNo());
		}
		/*
		 * if(checkTaxDues(regDetails,input.getVehicleStoppageDetailsVO().
		 * getStoppageDate())) { throw new
		 * BadRequestException("Please give stopage date with in the tax paid  duration: "
		 * + regDetails.getPrNo()); }
		 */
		if (input.getVehicleStoppageDetailsVO().getStoppageDate().isAfter(LocalDate.now())) {
			throw new BadRequestException("Vehicle stopage date should not be future date: " + regDetails.getPrNo());
		}
		regServiceDetails
				.setVehicleStoppageDetails(taxStoppageDetailsMapper.convertVO(input.getVehicleStoppageDetailsVO()));
		regServiceDetails.getVehicleStoppageDetails()
				.setVehicleAddressDetails(addressMapper.convertVO(input.getPresentAddress()));
		regServiceDetails.getVehicleStoppageDetails().setPrNo(regDetails.getPrNo());
		regServiceDetails.getVehicleStoppageDetails().setRegApplicationNo(regDetails.getApplicationNo());
		registratrionServicesApprovals.initiateApprovalProcessFlow(regServiceDetails);
		// setMviOfficeDetails(regServiceDetails);
	}

	public void doVehicleStoppageRevokation(RegServiceVO input,
			KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {

		RegistrationDetailsDTO regDetails = citizenObjects.getValue();
		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		RegServiceDTO dto = getLatestStoppageDoc(regDetails);
		VehicleStoppageDetailsDTO stoppageDto = dto.getVehicleStoppageDetails();
		// stoppageDto.setActions(null);
		stoppageDto.setRtoCompleted(null);
		regServiceDetails.setVehicleStoppageDetails(stoppageDto);
		regServiceDetails.getVehicleStoppageDetails().setStoppageRevpkationDate(LocalDate.now());
		/*
		 * if(dto.getLockedDetails()!=null && !dto.getLockedDetails().isEmpty()) {
		 * regServiceDetails.setLockedDetails(dto.getLockedDetails()); }
		 */
		RegServiceVO vo = regServiceMapper.convertEntity(dto);
		if (vo.getVehicleStoppageDetailsVO() != null
				&& vo.getVehicleStoppageDetailsVO().getVehicleAddressDetails() != null) {
			input.setPresentAddress(vo.getVehicleStoppageDetailsVO().getVehicleAddressDetails());
		}

		regServiceDetails.setApplicationStatus(StatusRegistration.INITIATED);
		registratrionServicesApprovals.initiateApprovalProcessFlow(regServiceDetails);
		// registratrionServicesApprovals.updatesVehicleStoppageRevokationNewFolw(regServiceDetails,
		// regDetails);
		// registratrionServicesApprovals.initiateApprovalProcessFlow(regServiceDetails);
		// setMviOfficeDetails(regServiceDetails);
	}

	private RegServiceDTO getLatestStoppageDoc(RegistrationDetailsDTO regDetails) {
		List<RegServiceDTO> listOfRegServices = regServiceDAO.findByRegistrationDetailsApplicationNoAndServiceIdsIn(
				regDetails.getApplicationNo(), Arrays.asList(ServiceEnum.VEHICLESTOPPAGE.getId()));
		if (listOfRegServices.isEmpty()) {
			throw new BadRequestException("Please apply vehvicle stoppage before revokation: " + regDetails.getPrNo());
		}
		listOfRegServices.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		RegServiceDTO dto = listOfRegServices.stream().findFirst().get();
		return dto;
	}

	private void getPermitStatus(SearchVo vo, String prNo) {
		Optional<PermitDetailsDTO> permitDto = fetchPermitDetails(prNo);
		if (permitDto.isPresent()) {
			vo.setPermitDetailsVO(permitDetailsMapper.convertEntity(permitDto.get()));
			vo.setIsValidPermit(true);

		}
		if (permitDto.isPresent()
				&& permitDto.get().getPermitValidityDetails().getPermitValidTo().isBefore(LocalDate.now())) {
			vo.setIsValidPermit(false);
			vo.setIsPermitExpired(true);
		}
	}

	private void setPrNo(RcValidationVO rcValidationVO) {
		List<PermitDetailsDTO> permitList = permitDetailsDAO.findByPermitNo(rcValidationVO.getPermitNo());
		if (!permitList.isEmpty()) {
			permitList.sort((d2, d1) -> d1.getCreatedDate().compareTo(d2.getCreatedDate()));
			rcValidationVO.setPrNo(permitList.stream().findFirst().get().getPrNo());
		} else {
			throw new BadRequestException("permit Details not found fo permitNo [{}]" + rcValidationVO.getPermitNo());
		}
	}

	/*
	 * private void setInsurancePucDetails(){ InsuranceDetailsVO insurenceDetails =
	 * doValidateAndSaveInsurenceDetails(regServiceVO,
	 * citizenObjectsOptional.get().getKey()); PUCDetailsVO pucDetails =
	 * doValidateAndSavePUCDetails(regServiceVO,
	 * citizenObjectsOptional.get().getKey()); if (insurenceDetails != null) {
	 * citizenObjectsOptional.get().getKey()
	 * .setInsuranceDetails(insuranceDetailsMapper.convertVO(insurenceDetails)); }
	 * if (pucDetails != null) {
	 * citizenObjectsOptional.get().getKey().setPucDetails(pucDetailsMapper.
	 * convertVO(pucDetails)); } }
	 */
	private boolean checkIsVehicleunderLifeTax(RegistrationDetailsDTO dto) {
		if (dto.getVehicleType().equalsIgnoreCase(CovCategory.T.getCode())) {
			return Boolean.FALSE;
		} else {
			Optional<MasterPayperiodDTO> Payperiod = masterPayperiodDAO.findByCovcode(dto.getClassOfVehicle());
			if (!Payperiod.isPresent()) {
				logger.error("No record found in pay period for:[{}] " + dto.getClassOfVehicle());
				throw new BadRequestException(
						"No record found in master_payperiod for class of vehicle: " + dto.getClassOfVehicle());
			}

			Pair<Optional<MasterPayperiodDTO>, Boolean> payperiodAndGoStatus = citizenTaxService.getPayPeroidForBoth(
					Payperiod, dto.getVahanDetails().getSeatingCapacity(), dto.getVahanDetails().getGvw());
			if (payperiodAndGoStatus.getFirst().get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.LifeTax.getCode())) {
				return Boolean.TRUE;
			}
		}
		return Boolean.TRUE;
	}

	@Override
	public Boolean isTOtokenCanceled(ApplicationSearchVO applicationSearchVO) {

		if (applicationSearchVO == null || applicationSearchVO.getApplicationNo() == null
				|| applicationSearchVO.getTokenNo() == null) {
			throw new BadRequestException("Invalid Inputs ");
		}
		Optional<RegServiceDTO> regDTO = regServiceDAO.findByApplicationNoAndBuyerDetailsTokenNo(
				applicationSearchVO.getApplicationNo(), applicationSearchVO.getTokenNo());
		if (!regDTO.isPresent()) {
			throw new BadRequestException(
					"No Record Found for applcation No : " + applicationSearchVO.getApplicationNo());
		}
		regDTO.get().setApplicationStatus(StatusRegistration.CANCELED);
		regDTO.get().getBuyerDetails().setIsTokenCanceledBySeller(Boolean.TRUE);
		regDTO.get().getBuyerDetails().setTokenCanceledTime(LocalDateTime.now());
		regServiceDAO.save(regDTO.get());
		notifications.sendNotifications(MessageTemplate.TOW_TOKEN_CANCELED.getId(), regDTO.get());
		if (regDTO.get().getFinanceDetails() != null && regDTO.get().getFinanceDetails().getUserId() != null) {
			MasterUsersDTO masterDto = masterUsersDAO
					.findByUserIdAndStatusTrue(regDTO.get().getFinanceDetails().getUserId());
			if (masterDto != null && (masterDto.getMobile() != null || masterDto.getEmail() != null)) {
				ContactDTO contactDto = new ContactDTO();
				contactDto.setMobile(masterDto.getMobile());
				contactDto.setEmail(masterDto.getEmail());
				regDTO.get().setUsersContactDetails(contactDto);
				if (regDTO.get().getIsHPTDone()) {
					notifications.sendNotifications(MessageTemplate.TOW_TOKENCANCEL_FIN_APPROVED.getId(), regDTO.get());
				} else if (regDTO.get().getFinanceDetails().getStatus() != null
						&& regDTO.get().getFinanceDetails().getStatus().equals(StatusRegistration.REJECTED)) {
					notifications.sendNotifications(MessageTemplate.TOW_TOKENCANCEL_FIN_REJECTED.getId(), regDTO.get());
				}
			}
		}
		return true;

	}

	@Override
	public List<String> getTaxTypes(String applicationNo) {
		Pair<List<String>, Boolean> listTaxperiod = getPayTaxType(applicationNo);

		if (listTaxperiod.getSecond()) {
			throw new BadRequestException("vehicle is not eligible to pay tax : " + applicationNo);
		}
		return listTaxperiod.getFirst();
	}

	private Pair<List<String>, Boolean> getPayTaxType(String applicationNo) {
		Boolean gostatus = Boolean.FALSE;
		Boolean taxPayStatus = Boolean.FALSE;
		Boolean skipQTax = Boolean.FALSE;
		Optional<RegistrationDetailsDTO> regOptionalDto = registrationDetailDAO.findByApplicationNo(applicationNo);
		if (!regOptionalDto.isPresent()) {
			logger.error("No record found in registration details:  " + applicationNo);
			// throw error message
			throw new BadRequestException("No record found in registration details for:  " + applicationNo);
		}
		RegistrationDetailsDTO dto = regOptionalDto.get();
		List<String> listTaxperiod = new ArrayList<>();
		Optional<MasterPayperiodDTO> Payperiod = masterPayperiodDAO.findByCovcode(dto.getClassOfVehicle());

		if (!Payperiod.isPresent()) {
			logger.error("No record found in master_payperiod for:  " + dto.getClassOfVehicle());
			// throw error message
			throw new BadRequestException("No record found in master_payperiod for:  " + dto.getClassOfVehicle());
		}

		if (Payperiod.get().getPayperiod().equalsIgnoreCase("B")) {
			if (dto.getVahanDetails().getGvw() == null) {
				logger.error("vehicle gvw weight not found for:  " + applicationNo);
				// throw error message
				throw new BadRequestException("vehicle gvw weight not found for:  " + applicationNo);
			}
			Pair<Optional<MasterPayperiodDTO>, Boolean> payperiodAndGoStatus = citizenTaxService.getPayPeroidForBoth(
					Payperiod, dto.getVahanDetails().getSeatingCapacity(), dto.getVahanDetails().getGvw());
			gostatus = payperiodAndGoStatus.getSecond();
			if (payperiodAndGoStatus.getSecond()) {
				List<TaxDetailsDTO> listOfTaxDetails = taxDetailsDAO
						.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(dto.getApplicationNo(),
								Arrays.asList(TaxTypeEnum.LifeTax.getDesc()));
				if (!listOfTaxDetails.isEmpty()) {
					for (TaxDetailsDTO taxdto : listOfTaxDetails) {
						if (dto.getClassOfVehicle().equalsIgnoreCase(taxdto.getClassOfVehicle())) {
							taxPayStatus = Boolean.TRUE;

						}
					}
					listOfTaxDetails.clear();
				}
			}
			if (payperiodAndGoStatus.getFirst().get().getCovcode()
					.equalsIgnoreCase(ClassOfVehicleEnum.OBPN.getCovCode())
					&& payperiodAndGoStatus.getFirst().get().getPayperiod().equalsIgnoreCase("L")) {
				taxPayStatus = Boolean.TRUE;
			}
		} else {
			Optional<PropertiesDTO> optionalProperties = propertiesDAO.findByAllowQuaterTaxTrue();
			PropertiesDTO properties = optionalProperties.get();
			if (properties.getCovs().stream().anyMatch(cov -> cov.equalsIgnoreCase(dto.getClassOfVehicle()))) {
				if (!dto.getRegistrationValidity().getPrGeneratedDate().isBefore(properties.getPrGeneratedDate())) {
					skipQTax = Boolean.TRUE;

				}

				List<TaxDetailsDTO> listOfTaxDetails = taxDetailsDAO
						.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(dto.getApplicationNo(),
								Arrays.asList(TaxTypeEnum.LifeTax.getDesc()));
				if (listOfTaxDetails != null && !listOfTaxDetails.isEmpty()) {
					for (TaxDetailsDTO taxdto : listOfTaxDetails) {
						if (dto.getClassOfVehicle().equalsIgnoreCase(taxdto.getClassOfVehicle())) {
							taxPayStatus = Boolean.TRUE;
						}
					}
				}
				listTaxperiod.add(TaxTypeEnum.LifeTax.getDesc());
				Payperiod.get().setPayperiod(TaxTypeEnum.QuarterlyTax.getCode());

			}
			if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.LifeTax.getCode())) {

				taxPayStatus = Boolean.TRUE;

			}
		}
		if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.LifeTax.getCode())) {
			// return life tax
			listTaxperiod.add(TaxTypeEnum.LifeTax.getDesc());

		}
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
			if (!skipQTax) {
				listTaxperiod.add(TaxTypeEnum.QuarterlyTax.getDesc());
				listTaxperiod.add(TaxTypeEnum.HalfyearlyTax.getDesc());
				listTaxperiod.add(TaxTypeEnum.YearlyTax.getDesc());
			}

		} else if (quaterTwo.contains(LocalDate.now().getMonthValue())) {
			// return Q
			if (!skipQTax) {
				listTaxperiod.add(TaxTypeEnum.QuarterlyTax.getDesc());
			}
		} else if (quaterThree.contains(LocalDate.now().getMonthValue())) {
			// return Q ,h
			if (!skipQTax) {
				listTaxperiod.add(TaxTypeEnum.QuarterlyTax.getDesc());
				listTaxperiod.add(TaxTypeEnum.HalfyearlyTax.getDesc());
			}

		} else if (quaterFour.contains(LocalDate.now().getMonthValue())) {
			// return Q
			if (!skipQTax) {
				listTaxperiod.add(TaxTypeEnum.QuarterlyTax.getDesc());
			}
		}

		if (gostatus) {
			List<MasterNewGoTaxDetails> newGoDetails = masterNewGoTaxDetailsDAO.findAll();
			if (newGoDetails.isEmpty()) {
				throw new BadRequestException("new Gov go detils not found");
			}
			LocalDate trGeneratedDate = dto.getRegistrationValidity().getPrGeneratedDate();
			if (dto.getRegistrationValidity().getTrGeneratedDate() != null) {
				trGeneratedDate = dto.getRegistrationValidity().getTrGeneratedDate();
			}
			MasterNewGoTaxDetails goDetails = newGoDetails.stream().findFirst().get();
			if (goDetails.getOldTaxEffectUpTo().isBefore(LocalDate.now())) {
				listTaxperiod = new ArrayList<>();
				listTaxperiod.add(TaxTypeEnum.LifeTax.getDesc());
			} else if (trGeneratedDate.isEqual(goDetails.getTaxEffectFrom())
					|| trGeneratedDate.isAfter(goDetails.getTaxEffectFrom())) {
				listTaxperiod = new ArrayList<>();
				listTaxperiod.add(TaxTypeEnum.LifeTax.getDesc());
			}
		}
		return Pair.of(listTaxperiod, taxPayStatus);
	}

	private RegServiceDTO getLatestRecordToTrnasferPermit(String prNo) {
		if (StringUtils.isNotBlank(prNo)) {
			List<RegServiceDTO> listRegDetails = regServiceDAO.findByPrNo(prNo);
			if (!CollectionUtils.isEmpty(listRegDetails)) {
				listRegDetails.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
				return listRegDetails.stream().findFirst().get();
			}
		}
		return null;
	}

	private boolean isToPayLateFeeForRenewal(String applicationNo, String slotDate) {

		Optional<RegServiceDTO> optionalRegDto = regServiceDAO.findByApplicationNo(applicationNo);
		if (!optionalRegDto.isPresent()) {
			throw new BadRequestException("No records found for application No: " + applicationNo);
		}
		RegServiceDTO dto = optionalRegDto.get();
		if (dto.getSlotDetails() == null) {
			throw new BadRequestException("slot details not found for application No: " + applicationNo);
		}

		if (!dto.isPaidPyamentsForRenewal()) {
			return Boolean.TRUE;
		}
		DateTimeFormatter df = DateTimeFormatter.ofPattern("dd-MM-yyyy");
		LocalDate localDate = LocalDate.parse(slotDate, df);
		if (dto.getRegistrationDetails().getRegistrationValidity().getRegistrationValidity() != null
				&& dto.getRegistrationDetails().getRegistrationValidity().getRegistrationValidity().toLocalDate()
						.isBefore(localDate)) {
			if (dto.getSlotDetailsLog() == null) {
				if (dto.getSlotDetails().getPaymentStatus() != null
						&& dto.getSlotDetails().getPaymentStatus().equals(StatusRegistration.PAYMENTFAILED)) {
					return Boolean.TRUE;
				}
				if (localDate.isAfter(dto.getSlotDetails().getSlotDate())) {
					if (localDate.getMonthValue() != dto.getSlotDetails().getSlotDate().getMonthValue()) {
						return Boolean.TRUE;
					}
				}
			}
			LocalDate privesSlotDate = localDate;
			if (dto.getSlotDetailsLog() != null) {
				if (dto.getSlotDetails().getPaymentStatus() == null
						|| !dto.getSlotDetails().getPaymentStatus().equals(StatusRegistration.PAYMENTFAILED)) {
					privesSlotDate = dto.getSlotDetails().getSlotDate();
				}
				for (SlotDetailsDTO slots : dto.getSlotDetailsLog()) {
					if (!privesSlotDate.isAfter(slots.getSlotDate())) {
						if (slots.getPaymentStatus() != null
								&& slots.getPaymentStatus().equals(StatusRegistration.PAYMENTFAILED)) {
							continue;
						}
						privesSlotDate = slots.getSlotDate();
					}
				}
				if (localDate.isAfter(privesSlotDate) || localDate.equals(privesSlotDate)) {
					if (localDate.getMonthValue() != privesSlotDate.getMonthValue()) {
						return Boolean.TRUE;
					}
				}
			}
		}
		return Boolean.FALSE;
	}

	@Override
	public void updatePaidDateAsCreatedDate(List<TaxDetailsDTO> taxDetailsDTO) {

		if (taxDetailsDTO == null || taxDetailsDTO.isEmpty()) {
			logger.error("tax details not found");
			throw new BadRequestException("tax details not found");
		}
		for (TaxDetailsDTO dto : taxDetailsDTO) {
			if (dto.getCreatedDate() == null) {
				if (dto.getTaxPaidDate() == null) {
					logger.error("tax paid date missing for [{}]", dto.getPrNo());
					throw new BadRequestException("tax paid date missing for: " + dto.getPrNo());
				} else {
					dto.setCreatedDate(dto.getTaxPaidDate().atStartOfDay());
				}
			}

		}
	}

	/**
	 * Application search at RTA end
	 */
	@Override
	public List<CitizenSearchReportVO> fetchDetailsFromRegistrationServicesAtRTA(
			ApplicationSearchVO applicationSearchVO) {
		List<CitizenSearchReportVO> searchReport = new ArrayList<>();
		// CitizenSearchReportVO vo = null;
		boolean isToEnableTokenCancel = false;
		List<RegServiceDTO> servicesList = null;
		/*
		 * if (StringUtils.isBlank(applicationSearchVO.getChassisNo())) { throw new
		 * BadRequestException("Please provide last 5 digits chassis numbers "); } if
		 * (StringUtils.isNoneBlank(applicationSearchVO.getChassisNo()) &&
		 * applicationSearchVO.getChassisNo().length() != 5) { throw new
		 * BadRequestException("Please provide last 5 digits chassis numbers "); }
		 */
		if (applicationSearchVO.getApplicationNo() != null
				&& StringUtils.isNoneBlank(applicationSearchVO.getApplicationNo())) {
			servicesList = regServiceDAO.findByApplicationNoIn(applicationSearchVO.getApplicationNo());
		}

		if (applicationSearchVO.getTrNo() != null && StringUtils.isNoneBlank(applicationSearchVO.getTrNo())) {
			servicesList = regServiceDAO
					.findByRegistrationDetailsTrNoAndServiceIdsNotNull(applicationSearchVO.getTrNo());
		}

		if (applicationSearchVO.getPrNo() != null && StringUtils.isNoneBlank(applicationSearchVO.getPrNo())) {
			servicesList = regServiceDAO.findByPrNo(applicationSearchVO.getPrNo());
		}

		List<RegServiceDTO> regServiceList = getRegistrationServicesLatestRecordBasedOnServicesListAtAdmin(
				servicesList);
		if (!regServiceList.isEmpty()) {
			for (RegServiceDTO regServiceDTO : regServiceList) {
				if (null != regServiceDTO.getBuyerDetails()) {
					if (null != regServiceDTO.getBuyerDetails().getBuyerApplicantDetails()) {
						regServiceDTO.getRegistrationDetails().getApplicantDetails().setFatherName(
								regServiceDTO.getBuyerDetails().getBuyerApplicantDetails().getFatherName());
						regServiceDTO.getRegistrationDetails().getApplicantDetails().setFirstName(
								regServiceDTO.getBuyerDetails().getBuyerApplicantDetails().getFirstName());
					}
					if (TowTokenStatus.validateStatus(regServiceDTO.getApplicationStatus().toString())) {
						isToEnableTokenCancel = true;
					}
				}
				CitizenSearchReportVO vo = regServiceMapper.convertSpecificFieldsForCtizenSearch(regServiceDTO);
				if (isToEnableTokenCancel) {
					vo.setTokenCancelRequired(true);
				}
				vo.setApplicationStatus(regServiceDTO.getApplicationStatus());
				if ((regServiceDTO.getServiceIds().contains(ServiceEnum.HPA.getId())
						|| regServiceDTO.getServiceIds().contains(ServiceEnum.HIREPURCHASETERMINATION.getId()))
						&& (regServiceDTO.getCurrentIndex() != null
								&& RoleEnum.RTO.getIndex() + 1 != regServiceDTO.getCurrentIndex().intValue()
								&& regServiceDTO.getFinanceDetails() != null
								&& regServiceDTO.getFinanceDetails().getStatus() != null)) {
					vo.setApplicationStatus(regServiceDTO.getApplicationStatus());
				}
				if (StringUtils.isNoneBlank(regServiceDTO.getToken())) {
					vo.setToken(regServiceDTO.getToken());
				}
				searchReport.add(vo);
			}

		}

		return searchReport;
	}

	private List<RegServiceDTO> getRegistrationServicesLatestRecordBasedOnServicesListAtAdmin(
			List<RegServiceDTO> servicesList) {
		List<RegServiceDTO> list = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(servicesList) && servicesList.size() > 0) {
			servicesList.sort((s1, s2) -> s2.getCreatedDate().compareTo(s1.getCreatedDate()));
			RegServiceDTO dto = servicesList.stream().findFirst().get();
			if (dto.getRegistrationDetails() == null || dto.getRegistrationDetails().getVahanDetails() == null
					|| StringUtils.isBlank(dto.getRegistrationDetails().getVahanDetails().getChassisNumber())) {
				throw new BadRequestException("Registration details or vahan details not found in citizen document");
			}
			if (dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.TAXATION.getId()))) {
				RegServiceDTO secondDto = servicesList.get(servicesList.size() - 1);
				if (!secondDto.getApplicationNo().equalsIgnoreCase(dto.getApplicationNo())) {
					list.add(secondDto);
				}

			}
			list.add(dto);
		}
		return list;
	}

	private void doPermitDataEntry(RegServiceVO input, KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {

		RegistrationDetailsDTO regDetails = citizenObjects.getValue();
		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		PermitDetailsVO permitDetails = input.getPermitDetailsVO();
		if (permitDetails == null) {
			throw new BadRequestException("Permit Details Not Found: " + regDetails.getPrNo());
		}
		if (input.getPermitDetailsVO().getPermitValidityDetailsVO() == null) {
			throw new BadRequestException("Permit valid Details Not Found: " + regDetails.getPrNo());
		}
		LocalDate permitvalidFrom = permitDetails.getPermitValidityDetailsVO().getPermitValidFrom();
		LocalDate permitvalidTO = permitDetails.getPermitValidityDetailsVO().getPermitValidTo();
		if (permitvalidFrom == null || permitvalidTO == null) {
			throw new BadRequestException("Permit valid from and valid to details not found: " + regDetails.getPrNo());
		}
		permitDataEntryDateValidtion(permitvalidFrom, permitvalidTO, input.getPrNo());
		if (input.getPermitDetailsVO().getPermitNo() == null) {
			throw new BadRequestException("Permit Number Not Found: " + regDetails.getPrNo());
		} else {
			List<PermitDetailsDTO> permitnumCheck = permitDetailsDAO.findByPermitNo(permitDetails.getPermitNo());
			if (!permitnumCheck.isEmpty()) {

				logger.error("Already permit exists with this permit number [" + permitDetails.getPermitNo() + "]");
				throw new BadRequestException(
						"Already permit exists with this permit number [" + permitDetails.getPermitNo() + "]");

			}
		}
		PermitDetailsDTO dto = permitDetailsMapper.convertVO(permitDetails);
		regServiceDetails.setPdtl(dto);

		registratrionServicesApprovals.initiateApprovalProcessFlow(regServiceDetails);

	}

	private void permitDataEntryDateValidtion(LocalDate permitvalidFrom, LocalDate permitvalidTO, String prNo) {
		if (permitvalidFrom.plusYears(5).minusDays(1).equals(permitvalidTO)) {
			throw new BadRequestException("Please Enter Valid Details From and To : " + prNo);
		}
	}

	@Override
	public List<MobileVO> getDashBoardInfoAndPrNos(String aadharNo) {
		List<MobileVO> mobileVOList = new ArrayList<MobileVO>();
		RegistrationDetailsDTO latestExpired = null;
		PermitDetailsDTO latestPermitExpired = null;
		List<RegistrationDetailsDTO> registrationDetailslist = registrationDetailDAO
				.findByApplicantDetailsAadharNoInAndApplicantDetailsIsAadhaarValidatedTrue(aadharNo);
		registrationDetailslist = registrationMigrationSolutionsService
				.removeInactiveRecordsToList(registrationDetailslist);
		if (CollectionUtils.isEmpty(registrationDetailslist)) {
			throw new BadRequestException("No Records Found");
		}

		registrationDetailslist.stream().forEach(val -> {
			MobileVO mobileVO = new MobileVO();
			mobileVO.setPrNo(val.getPrNo());
			mobileVO.setCov(val.getClassOfVehicle());
			mobileVO.setSeatingCapacity(val.getVahanDetails().getSeatingCapacity());
			mobileVO.setGvw(val.getVahanDetails().getGvw());
			mobileVO.setApplicationNo(val.getApplicationNo());
			// mobileVO.setCov(val.getClassOfVehicle());
			List<VcrFinalServiceDTO> vcrList = vcrFinalServiceDAO
					.findByRegistrationRegNoAndIsVcrClosedIsFalse(val.getPrNo());
			if (vcrList != null && !vcrList.isEmpty()) {
				List<String> vcrNos = new ArrayList<String>();
				vcrList.stream().forEach(one -> {
					vcrNos.add(one.getVcr().getVcrNumber());
				});
				mobileVO.setVcrNos(vcrNos);
			}
			mobileVOList.add(mobileVO);
		});
		List<RegistrationDetailsDTO> expiredPrList = registrationDetailslist.stream()
				.filter(val -> val.getVehicleType().equals(CovCategory.N.getCode())
						&& (val.getRegistrationValidity().getRegistrationValidity().isBefore(LocalDateTime.now())
								|| val.getRegistrationValidity().getRegistrationValidity()
										.isBefore(LocalDateTime.now().plusDays(60))))
				.collect(Collectors.toList());
		List<String> rcPrNos = expiredPrList.stream().map(val -> val.getPrNo()).collect(Collectors.toList());
		if (!expiredPrList.isEmpty()) {
			expiredPrList.sort((p1, p2) -> p1.getRegistrationValidity().getRegistrationValidity()
					.compareTo(p2.getRegistrationValidity().getRegistrationValidity()));
			latestExpired = expiredPrList.stream().findFirst().get();
			setExpire(rcPrNos, mobileVOList, latestExpired);
		}
		if (!registrationDetailslist.isEmpty()) {
			List<String> taxPrNos = registrationDetailslist.stream().map(val -> val.getPrNo())
					.collect(Collectors.toList());
			setNotEligibleForTax(taxPrNos, mobileVOList);
		}
		List<RegistrationDetailsDTO> transportlist = registrationDetailslist.stream()
				.filter(val -> val.getVehicleType().equals(CovCategory.T.getCode())).collect(Collectors.toList());
		List<String> transportPrNos = transportlist.stream().map(val -> val.getPrNo()).collect(Collectors.toList());
		if (!transportlist.isEmpty()) {
			List<PermitDetailsDTO> permitlist = permitDetailsDAO.findByPrNoInAndPermitClassCodeAndPermitStatus(
					transportPrNos, PermitType.PRIMARY.getPermitTypeCode(), PermitsEnum.ACTIVE.getDescription());
			List<PermitDetailsDTO> permitexplist = permitlist.stream()
					.filter(val -> val.getPermitValidityDetails().getPermitValidTo().isBefore(LocalDate.now())
							|| val.getPermitValidityDetails().getPermitValidTo().isBefore(LocalDate.now().plusDays(30)))
					.collect(Collectors.toList());
			if (!permitexplist.isEmpty()) {
				List<String> permitPrNos = permitexplist.stream().map(val -> val.getPrNo())
						.collect(Collectors.toList());
				permitexplist.sort((p1, p2) -> p1.getPermitValidityDetails().getPermitValidTo()
						.compareTo(p2.getPermitValidityDetails().getPermitValidTo()));
				latestPermitExpired = permitexplist.stream().findFirst().get();
				setPermitExpire(permitPrNos, mobileVOList, latestPermitExpired);
			}
		}
		return mobileVOList;
	}

	public List<MobileVO> setExpire(List<String> rcPrNos, List<MobileVO> mobileVOList,
			RegistrationDetailsDTO latestExpired) {
		mobileVOList.stream().forEach(val -> {
			if (rcPrNos.contains(val.getPrNo())) {
				val.setRcExpried(Boolean.TRUE);
			}
			if (val.getPrNo().equals(latestExpired.getPrNo())) {
				long days = 0;
				val.setRcExpriedFirst(Boolean.TRUE);
				days = ChronoUnit.DAYS.between(LocalDate.now().minusDays(1),
						latestExpired.getRegistrationValidity().getRegistrationValidity());
				if (days <= 0) {
					days = 0;
				}
				val.setrCExpriedDays((int) days);
			}
		});
		return mobileVOList;
	}

	public List<MobileVO> setPermitExpire(List<String> permitPrNos, List<MobileVO> mobileVOList,
			PermitDetailsDTO latestPermitExpired) {
		mobileVOList.stream().forEach(val -> {
			if (permitPrNos.contains(val.getPrNo())) {
				val.setPermitExpried(Boolean.TRUE);
			}
			if (val.getPrNo().equals(latestPermitExpired.getPrNo())) {
				long days = 0;
				val.setPermitExpriedFirst(Boolean.TRUE);
				days = ChronoUnit.DAYS.between(LocalDate.now().minusDays(1),
						latestPermitExpired.getPermitValidityDetails().getPermitValidTo());
				if (days <= 0) {
					days = 0;
				}
				val.setPermitExpriedDays((int) days);
			}
		});
		return mobileVOList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AadhaarRequestVO> getAdhaarData(String adhaarNo) {
		List<RegistrationDetailsDTO> regList = registrationDetailDAO
				.findByApplicantDetailsAadharNoInAndApplicantDetailsIsAadhaarValidatedTrue(adhaarNo);
		if (regList.isEmpty()) {
			throw new BadRequestException("No records found with this Aadhaar Number " + adhaarNo);
		}
		// return registrationDetailsMapper.aadharRelatedData(regList);
		return registrationDetailsMapper.aadharRelatedDataForEPrgathi(regList);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<AadhaarRequestVO> getAllAadhaarData(List<String> aadhaarNos) {

		List<RegistrationDetailsDTO> regList = registrationDetailDAO
				.findByApplicantDetailsAadharNoInAndApplicantDetailsIsAadhaarValidatedTrue(aadhaarNos);
		List<AadhaarRequestVO> aadharVos = null;
		if (!regList.isEmpty()) {
			aadharVos = registrationDetailsMapper.aadharRelatedData(regList);
			List<MemberDetails> memberDetails = new ArrayList<>();
			aadharVos.forEach(m -> {
				MemberDetails member = new MemberDetails();
				BeanUtils.copyProperties(m, member);
				memberDetails.add(member);
			});
			AdhaarFamilyDTO adhaarFamilyDTO = new AdhaarFamilyDTO();
			adhaarFamilyDTO.setAadhaarNos(aadhaarNos);
			adhaarFamilyDTO.setMemberDetails(memberDetails);
			adhaarFamilyDTO.setCreatedDate(LocalDateTime.now());
			adhaarFamilyDAO.save(adhaarFamilyDTO);
		}
		return aadharVos;
	}

	@Override
	public void freshRcFinanceProcessAtMVI(String regServiceVOString, MultipartFile[] uploadfiles) {
		logger.debug("regServiceVO [{}] ", regServiceVOString);
		if (StringUtils.isBlank(regServiceVOString)) {
			logger.error("regServiceVO is required.");
			throw new BadRequestException("regServiceVO is required.");
		}

		Optional<RegServiceVO> inputOptional = readValue(regServiceVOString, RegServiceVO.class);

		if (!inputOptional.isPresent()) {
			logger.error("Invalid Inputs.");
			throw new BadRequestException("Invalid Inputs.");
		}
		Optional<RegServiceDTO> regServiceDTO = regServiceDAO
				.findByApplicationNo(inputOptional.get().getApplicationNo());

		RegServiceDTO regServicesDto = regServiceDTO.get();
		try {
			saveCitizenServiceDoc(inputOptional.get(), regServicesDto, uploadfiles);
		} catch (IOException e) {
			e.printStackTrace();
		}
		regServiceDAO.save(regServicesDto);

	}

	// As per jagan inputs change the query
	@Override
	public List<MobileApplicationStatusVO> getLatestRecordBasedOnAadharNo(String aadharNo, List<String> prNos) {
		List<MobileApplicationStatusVO> mobileVOList = new ArrayList<MobileApplicationStatusVO>();
		List<RegServiceDTO> regSerDtoList = new ArrayList<RegServiceDTO>();
		List<RegServiceDTO> regServiceDtoList = regServiceDAO
				.findFirst20ByPrNoInAndAadhaarNoOrderByCreatedDateDesc(prNos, aadharNo);
		if (!regServiceDtoList.isEmpty()) {
			List<RegServiceDTO> approvedlist = regServiceDtoList.stream().filter(val -> val.getApplicationStatus()
					.getDescription().equals(StatusRegistration.APPROVED.getDescription()))
					.collect(Collectors.toList());
			if (!approvedlist.isEmpty()) {
				validateMissingCreatedDate(approvedlist);
				approvedlist.sort((p2, p1) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
				approvedlist = approvedlist.stream().limit(5).collect(Collectors.toList());
				regSerDtoList.addAll(approvedlist);
			}
			List<RegServiceDTO> nonApprovedlist = regServiceDtoList.stream().filter(val -> (!val.getApplicationStatus()
					.getDescription().equals(StatusRegistration.APPROVED.getDescription())))
					.collect(Collectors.toList());
			regSerDtoList.addAll(nonApprovedlist);
			if (!regSerDtoList.isEmpty()) {
				setLatestrecords(mobileVOList, regSerDtoList);
			}
		} else {
			throw new BadRequestException("No records found");
		}
		return mobileVOList;
	}

	@Override
	public List<CitizenSearchReportVO> fetchDetailsFromRegistrationServicesForMobile(
			ApplicationSearchVO applicationSearchVO) {
		List<CitizenSearchReportVO> searchReport = new ArrayList<>();
		Optional<RegServiceDTO> optionalregService = null;
		if (applicationSearchVO.getApplicationNo() != null
				&& StringUtils.isNoneBlank(applicationSearchVO.getApplicationNo())) {
			optionalregService = regServiceDAO.findByApplicationNo(applicationSearchVO.getApplicationNo());
		}
		if (optionalregService.isPresent()) {
			RegServiceDTO regServiceDTO = optionalregService.get();
			CitizenSearchReportVO vo = regServiceMapper.convertSpecificFieldsForCtizenSearchForMobile(regServiceDTO);
			vo.setApplicationStatus(regServiceDTO.getApplicationStatus());
			if ((regServiceDTO.getServiceIds().contains(ServiceEnum.HPA.getId())
					|| regServiceDTO.getServiceIds().contains(ServiceEnum.HIREPURCHASETERMINATION.getId()))
					&& (regServiceDTO.getCurrentIndex() != null
							&& RoleEnum.RTO.getIndex() + 1 != regServiceDTO.getCurrentIndex().intValue()
							&& regServiceDTO.getFinanceDetails() != null
							&& regServiceDTO.getFinanceDetails().getStatus() != null)) {
				vo.setApplicationStatus(regServiceDTO.getApplicationStatus());
			}
			if (StringUtils.isNoneBlank(regServiceDTO.getToken())) {
				vo.setToken(regServiceDTO.getToken());
			}
			if (regServiceDTO.getActionDetails() != null)
				vo.setActionDetailsList(actionDetailMapper.convertEntity(regServiceDTO.getActionDetails()));

			searchReport.add(vo);
		}
		return searchReport;
	}

	private RcValidationVO getValidationVO(RegServiceVO regServiceVO) {
		RcValidationVO rcValidationVO = new RcValidationVO();
		rcValidationVO.setAadharNo(regServiceVO.getTowDetails().getBuyerAadhaarNo().toString());
		rcValidationVO.setPrNo(regServiceVO.getPrNo());
		rcValidationVO.setTransferType(regServiceVO.getTowDetails().getTransferType());
		rcValidationVO.setServiceIds(regServiceVO.getServiceIds());
		AadhaarDetailsRequestVO aadhaarDetailsRequestVO = new AadhaarDetailsRequestVO();
		aadhaarDetailsRequestVO.setUid_num(regServiceVO.getTowDetails().getBuyerAadhaarNo().toString());
		rcValidationVO.setAadhaarDetailsRequestVO(aadhaarDetailsRequestVO);
		return rcValidationVO;
	}

	private List<MobileApplicationStatusVO> setLatestrecords(List<MobileApplicationStatusVO> mobileVOList,
			List<RegServiceDTO> regSerDtoList) {
		regSerDtoList.stream().forEach(val -> {
			MobileApplicationStatusVO mobileApplicationStatusVO = new MobileApplicationStatusVO();
			mobileApplicationStatusVO.setPrNo(val.getPrNo());
			mobileApplicationStatusVO.setApplicationNo(val.getApplicationNo());
			mobileApplicationStatusVO.setApplicationStatus(val.getApplicationStatus().getDescription());
			mobileApplicationStatusVO.setCreatedDate(val.getCreatedDate());
			mobileApplicationStatusVO
					.setServiceType(val.getServiceType().stream().map(p -> p.getDesc()).collect(Collectors.toList()));
			mobileVOList.add(mobileApplicationStatusVO);
		});
		return mobileVOList;
	}

	public List<MobileVO> setNotEligibleForTax(List<String> taxPrNos, List<MobileVO> mobileVOList) {
		mobileVOList.stream().forEach(val -> {
			Optional<MasterPayperiodDTO> Payperiod = masterPayperiodDAO.findByCovcode(val.getCov());
			if (!Payperiod.isPresent()) {
				logger.error("No record found in pay period for:[{}] " + val.getCov());
				throw new BadRequestException(
						"No record found in master_payperiod for class of vehicle: " + val.getCov());
			}
			Pair<Optional<MasterPayperiodDTO>, Boolean> payperiodAndGoStatus = citizenTaxService
					.getPayPeroidForBoth(Payperiod, val.getSeatingCapacity(), val.getGvw());
			if (payperiodAndGoStatus.getSecond()) {
				List<TaxDetailsDTO> listOfTaxDetails = taxDetailsDAO
						.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(val.getApplicationNo(),
								Arrays.asList(TaxTypeEnum.LifeTax.getDesc()));
				if (!listOfTaxDetails.isEmpty()) {
					for (TaxDetailsDTO taxdto : listOfTaxDetails) {
						if (val.getCov().equalsIgnoreCase(taxdto.getClassOfVehicle())) {
							val.setNotEligibleForTax(Boolean.TRUE);
						}
					}
					listOfTaxDetails.clear();
				}
			} else {
				if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.LifeTax.getCode())) {
					val.setNotEligibleForTax(Boolean.TRUE);
				}
			}
			val.setApplicationNo(null);
			// val.setCov(null);
			val.setGvw(null);
			val.setSeatingCapacity(null);
		});
		return mobileVOList;
	}

	@Override
	public List<MasterFeedBackQuestionsVO> getFeedBackquestions() {

		List<MasterFeedBackQuestionsDTO> listOfQuestions = masterFeedBackQuestionsDAO.findAll();
		if (listOfQuestions == null || listOfQuestions.isEmpty()) {
			logger.error("No master Data for feedback form");
			throw new BadRequestException("No master Data for feedback form");
		}

		return masterFeedBackQuestionsMapper.convertEntity(listOfQuestions);
	}

	@Override
	public boolean isFeedBackFormupdated(String applicationNo) {

		Optional<RegServicesFeedBack> serviceOptionalFeedBackForm = regServicesFeedBackDAO
				.findByApplicationNo(applicationNo);
		if (serviceOptionalFeedBackForm.isPresent()) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	@Override
	public RegServicesFeedBack saveRegServiceFeedBackFrom(RegServicesFeedBackVO input) {

		if (StringUtils.isBlank(input.getApplicationNo())) {
			logger.error("application number not found from UI");
			throw new BadRequestException("application number not found from UI");
		}
		Optional<RegServicesFeedBack> serviceOptionalFeedBackForm = regServicesFeedBackDAO
				.findByApplicationNo(input.getApplicationNo());
		if (serviceOptionalFeedBackForm.isPresent()) {
			logger.error("form already submited");
			throw new BadRequestException("form already submited");
		}

		Optional<RegServiceDTO> regServicDto = regServiceDAO.findByApplicationNo(input.getApplicationNo());
		if (regServicDto.isPresent()) {
			input.setServices(regServicDto.get().getServiceType());
		}
		/*
		 * if(input.getServices() == null || input.getServices().isEmpty()) { throw new
		 * BadRequestException("services  not found from UI"); }
		 */
		if (input.getQuestions() == null || input.getQuestions().isEmpty()) {
			logger.error("questions and answers  not found from UI");
			throw new BadRequestException("questions and answers  not found from UI");
		}
		for (MasterFeedBackQuestionsVO vo : input.getQuestions()) {
			if (StringUtils.isBlank(vo.getAnswer())) {
				logger.error("Please enter valid feedback");
				throw new BadRequestException("Please enter valid feedback");
			}
		}
		RegServicesFeedBack dto = regServicesFeedBackMapper.convertVO(input);
		return regServicesFeedBackDAO.save(dto);
	}

	private void permitMandalExemption(Integer mandalCode, String fuelType) {
		Optional<PermitMandalExemptionDTO> dto = permitMandalExemptionDAO.findByMandalCodeAndStatusTrue(mandalCode);
		if (dto.isPresent()) {
			throw new BadRequestException("Permit Transfer for ARKT COV is not allowed for fuel type : " + fuelType
					+ "in " + dto.get().getMandalName() + " mandal");
		}
	}

	@Override
	public MasterWeightsForAltVO getweights(String applicationNo) {

		Optional<RegistrationDetailsDTO> registrationOptional = registrationDetailDAO
				.findByApplicationNo(applicationNo);

		if (!registrationOptional.isPresent()) {
			logger.error("No record found. [{}]", applicationNo);
			throw new BadRequestException("No record found.application no: " + applicationNo);
		}
		RegistrationDetailsDTO regDto = registrationOptional.get();
		Optional<MasterWeightsForAlt> optionalWeigts = validationForWeightAlt(regDto);
		MasterWeightsForAltVO vo = masterWeightsForAltMapper.convertEntity(optionalWeigts.get());
		return vo;

	}

	private Optional<MasterWeightsForAlt> validationForWeightAlt(RegistrationDetailsDTO regDto) {
		if (StringUtils.isBlank(regDto.getVehicleType())) {
			throw new BadRequestException("Vehicle type missing in reg document: " + regDto.getPrNo());
		}
		if (!regDto.getVehicleType().equalsIgnoreCase(CovCategory.T.getCode())) {
			throw new BadRequestException(
					"Only transport vehicle can apply weight type alteration: " + regDto.getPrNo());
		}
		if (regDto.isWeightAltDone()) {
			throw new BadRequestException("weight alteration already completed for this PR : " + regDto.getPrNo());
		}
		if (StringUtils.isBlank(regDto.getVahanDetails().getBodyTypeDesc())) {
			throw new BadRequestException("Vehicle body type missing for this PR : " + regDto.getPrNo());
		}
		// AS per UO note we are not checking body type
		/*
		 * if (regDto.getVahanDetails().getBodyTypeDesc().equalsIgnoreCase("TANKER")) {
		 * throw new BadRequestException(
		 * "Vehicle body type is tanker.Vehicle is not allow for weight alteration: " +
		 * regDto.getPrNo()); }
		 */
		Optional<PropertiesDTO> optionnalProperty = propertiesDAO.findByWeightAltTrue();
		if (!optionnalProperty.isPresent()) {
			throw new BadRequestException("No master covs for weight alt");
		}
		if (!optionnalProperty.get().getCovs().stream()
				.anyMatch(cov -> cov.equalsIgnoreCase(regDto.getClassOfVehicle()))) {
			throw new BadRequestException("class of vehicle not eligible to alter weight");
		}
		Integer gvw = regDto.getVahanDetails().getGvw();
		if (regDto.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.ARVT.getCovCode())) {
			if (regDto.getVahanDetails().getTrailerChassisDetailsDTO() != null
					&& !regDto.getVahanDetails().getTrailerChassisDetailsDTO().isEmpty()) {

				Integer gtw = regDto.getVahanDetails().getTrailerChassisDetailsDTO().stream().findFirst().get()
						.getGtw();
				for (TrailerChassisDetailsDTO trailerDetails : regDto.getVahanDetails().getTrailerChassisDetailsDTO()) {
					if (trailerDetails.getGtw() > gtw) {
						gtw = trailerDetails.getGtw();
					}
				}
				gvw = gvw + gtw;
			}
		}
		// TODO get the weight type form master
		Optional<MasterWeightsForAlt> optionalWeigts = masterWeightsForAltDAO
				.findByToGvwGreaterThanEqualAndFromGvwLessThanEqualAndStatusIsTrue(gvw, gvw);
		if (!optionalWeigts.isPresent()) {
			throw new BadRequestException("Vehicle not eligible to change weight: " + regDto.getPrNo());
		}
		// check tax pending or not
		LocalDate taxTill = citizenTaxService.validity(TaxTypeEnum.QuarterlyTax.getDesc());

		TaxHelper lastTaxTillDate = citizenTaxService.getLastPaidTax(regDto, null, false, taxTill, null, false,
				listOfQuaterTaxPayType(), false, false);
		if (lastTaxTillDate == null || lastTaxTillDate.getTax() == null || lastTaxTillDate.getValidityTo() == null) {
			throw new BadRequestException("TaxDetails not found");
		}
		if (lastTaxTillDate.isAnypendingQuaters()) {
			throw new BadRequestException("Tax pending. Please pay the tax for : " + regDto.getPrNo());
		}
		return optionalWeigts;
	}

	private List<String> listOfQuaterTaxPayType() {
		List<String> list = new ArrayList<>();
		list.add(TaxTypeEnum.QuarterlyTax.getDesc());
		list.add(TaxTypeEnum.HalfyearlyTax.getDesc());
		list.add(TaxTypeEnum.YearlyTax.getDesc());
		return list;
	}

	@Override
	public boolean isNeedtoAddVariationOfPermit(RegistrationDetailsDTO regServiceDetails) {
		List<PermitDetailsDTO> listOfPermits = permitDetailsDAO.findByPrNoInAndPermitClassCodeAndPermitStatus(
				Arrays.asList(regServiceDetails.getPrNo()), PermitsEnum.PermitType.PRIMARY.getPermitTypeCode(),
				PermitsEnum.ACTIVE.getDescription());
		List<PermitDetailsDTO> list = new ArrayList<>();
		if (!listOfPermits.isEmpty()) {
			for (PermitDetailsDTO permits : listOfPermits) {
				if (permits.getCreatedDate() != null) {
					list.add(permits);
				}
			}
			list.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
			PermitDetailsDTO permitsDtos = list.stream().findFirst().get();
			if (permitsDtos.getPermitValidityDetails() == null
					|| permitsDtos.getPermitValidityDetails().getPermitValidTo() == null) {
				logger.error("permit validitys missing in permit details : ", regServiceDetails.getPrNo());
				throw new BadRequestException(
						"permit validitys missing in permit details : " + regServiceDetails.getPrNo());

			}
			if (permitsDtos.getPermitValidityDetails().getPermitValidTo().isAfter(LocalDate.now())) {
				return true;
			}
		}
		return false;
	}

	/** only for Death Case in TOW **/
	private void updateMissedData(RegistrationDetailsVO registrationDetailsVO) {

		Integer districtId = registrationDetailsVO.getOfficeDetails().getOfficeDist();
		if (districtId == null) {
			Optional<OfficeDTO> officeDTO = officeDAO
					.findByOfficeCode(registrationDetailsVO.getOfficeDetails().getOfficeCode());
			if (officeDTO.isPresent()) {
				districtId = officeDTO.get().getDistrict();
			}
		}
		List<DistrictDTO> districtDTO = districtDAO.findByDistrictId(districtId);
		if (districtDTO.isEmpty()) {
			throw new BadRequestException(
					"Distric not found based on office :" + registrationDetailsVO.getOfficeDetails().getOfficeCode());
		}

		if (registrationDetailsVO.getApplicantDetails().getPresentAddress() == null) {
			registrationDetailsVO.getApplicantDetails().setPresentAddress(new ApplicantAddressVO());
		}
		registrationDetailsVO.getApplicantDetails().getPresentAddress()
				.setDistrict(districtMapper.districtLimitedFields(districtDTO.get(0)));

	}

	private List<Integer> listFoServicesForImages(RegServiceVO regServiceVO) {
		List<Integer> list = new ArrayList<>();

		list.add(ServiceEnum.CHANGEOFADDRESS.getId());
		// list.add(ServiceEnum.TRANSFEROFOWNERSHIP.getId());
		list.add(ServiceEnum.ISSUEOFNOC.getId());
		list.add(ServiceEnum.NEWFC.getId());
		list.add(ServiceEnum.RENEWALFC.getId());
		list.add(ServiceEnum.OTHERSTATIONFC.getId());
		list.add(ServiceEnum.DUPLICATE.getId());
		list.add(ServiceEnum.RENEWAL.getId());
		list.add(ServiceEnum.PERDATAENTRY.getId());
		list.add(ServiceEnum.HPA.getId());
		if (regServiceVO.getServiceIds().contains(ServiceEnum.TRANSFEROFOWNERSHIP.getId())
				&& regServiceVO.getTowDetails() != null && regServiceVO.getTowDetails().getBuyer() != null
				&& regServiceVO.getTowDetails().getTransferType().equals(TransferType.SALE)) {
			list.remove(ServiceEnum.HPA.getId());
		}
		if (regServiceVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.ALTERATIONOFVEHICLE.getId()))) {
			if (!regServiceVO.getAlterationVO().getAlterationService().stream()
					.anyMatch(vehicleType -> vehicleType.equals(AlterationTypeEnum.WEIGHT))) {
				list.add(ServiceEnum.ALTERATIONOFVEHICLE.getId());
			}
		}
		return list;
	}

	@Override
	public Boolean cancelOtherStateApplication(ApplicationSearchVO applicationSearchVO, RoleEnum role, String id) {
		if (!Arrays.asList(StatusRegistration.CANCELED, StatusRegistration.APPROVED)
				.contains(applicationSearchVO.getOtherStateNocStatus())) {
			logger.error("Invalid approval status.Status should be either,[{}] or [{}]", StatusRegistration.CANCELED,
					StatusRegistration.APPROVED);
			throw new BadRequestException("Invalid approval status.Status should be either"
					+ StatusRegistration.CANCELED + " or " + StatusRegistration.APPROVED);
		}
		if (applicationSearchVO.getOtherStateNocStatus() == null) {
			logger.error("Status is mandatory to NOC approval/cancel");
			throw new BadRequestException("Status is mandatory to NOC approval/cancel");
		}
		RegServiceDTO dto = null;
		if (applicationSearchVO.getApplicationNo() != null) {
			dto = regServiceDAO.findOne(applicationSearchVO.getApplicationNo());
		}
		if (applicationSearchVO.getPrNo() != null) {
			List<RegServiceDTO> regSerRecords = regServiceDAO.findByPrNo(applicationSearchVO.getPrNo());
			if (CollectionUtils.isNotEmpty(regSerRecords)) {
				regSerRecords.sort((s1, s2) -> s2.getCreatedDate().compareTo(s1.getCreatedDate()));
				dto = regSerRecords.stream().findFirst().get();
				if (dto.getServiceIds() != null && dto.getServiceIds().contains(ServiceEnum.TAXATION.getId())) {
					dto = regSerRecords.get(1);
				}
			}
		}
		if (dto == null) {
			logger.error("No record with  prNo [{}]", applicationSearchVO.getPrNo());
			throw new BadRequestException("No Record with Provided Input");
		}
		if (dto.getOtherStateNOCStatus() == null) {
			logger.error("This application is not belongs to other state[{}]");
			throw new BadRequestException(
					"This application not belongs to OTHER STATE / Existing record with previous flow");
		}

		if (dto.getServiceIds() != null && dto.getServiceIds().contains(ServiceEnum.DATAENTRY.getId())
				&& dto.getApplicationStatus().equals(StatusRegistration.APPROVED)
				&& dto.getOtherStateNOCStatus().equals(StatusRegistration.NOCVERIFICATIONPENDING)) {
			if (dto.getRegistrationDetails() != null
					&& (dto.getRegistrationDetails().getApplicantType().equals("OTHERSTATE")
							|| dto.getRegistrationDetails().getApplicantType().equals("MILITARY"))
					&& dto.getRegistrationDetails().isRegVehicleWithPR()) {
				dto.setApplicationStatus(applicationSearchVO.getOtherStateNocStatus());
				dto.setOtherStateNOCStatus(applicationSearchVO.getOtherStateNocStatus());
				setOtherStateApplicationDetails(dto, id, applicationSearchVO.getReasonToCancel(), role.getName());
				if (StatusRegistration.APPROVED.equals(applicationSearchVO.getOtherStateNocStatus())) {
					List<RegServiceDTO> regSerRecords = new ArrayList<>();
					regSerRecords.add(dto);
					return saveOtherStateRecord(regSerRecords, Boolean.FALSE, 0);
				}
				regServiceDAO.save(dto);
				return Boolean.TRUE;
			}
			logger.error("this service only for registered vehicle with pr");
			throw new BadRequestException("this service only for REGISTERED VEHICLE WITH PR ");
		}
		logger.error("Already action performed on the record/not belongs to other state");
		throw new BadRequestException("Already action performed on the record/not belongs to other state");
	}

	@Override
	public Boolean saveOtherStateRecord(List<RegServiceDTO> otherStateRecords, Boolean isShedularProcess,
			Integer days) {

		if (isShedularProcess) {
			/*
			 * List<RegServiceDTO> regSerDTO = new ArrayList<>() ;
			 * List<RegistrationDetailsDTO> regDetailsDTO= new ArrayList<>();
			 */

			for (RegServiceDTO regSerDetails : otherStateRecords) {
				try {
					String role = "SCHEDULAR";
					String reason = "NOC Verification date exceeded more than " + days
							+ "days,so schedular moved this record";
					regSerDetails.setApplicationStatus(StatusRegistration.APPROVED);
					regSerDetails.setOtherStateNOCStatus(StatusRegistration.APPROVED);
					setOtherStateApplicationDetails(regSerDetails, role, reason, role);
					registratrionServicesApprovals.saveOtherStateData(regSerDetails,
							regSerDetails.getRegistrationDetails());
				} catch (Exception e) {
					logger.info("Saving failed for the record {}", regSerDetails.getPrNo());
				}
				/*
				 * regSerDTO.add(regSerDetails);
				 * regDetailsDTO.add(regSerDetails.getRegistrationDetails());
				 */
			}

			/*
			 * regServiceDAO.save(regSerDTO); registrationDetailDAO.save(regDetailsDTO);
			 */
			return Boolean.TRUE;

		}
		/*
		 * regServiceDAO.save(otherStateRecords.get(0));
		 * registrationDetailDAO.save(otherStateRecords.get(0).
		 * getRegistrationDetails());
		 */
		registratrionServicesApprovals.saveOtherStateData(otherStateRecords.get(0),
				otherStateRecords.get(0).getRegistrationDetails());
		return Boolean.TRUE;

	}

	private void setOtherStateApplicationDetails(RegServiceDTO dto, String id, String reason, String role) {
		dto.setCurrentIndex(RoleEnum.RTO.getIndex() + 1);
		ApplicationStatusDetails otherStateNocDetails = new ApplicationStatusDetails();
		otherStateNocDetails.setActionBy(id);
		if (StringUtils.isEmpty(reason)) {
			logger.error("Rto Comments are missing");
			throw new BadRequestException("RTO Comments Mandatory");
		}
		otherStateNocDetails.setComments(reason);
		otherStateNocDetails.setActionTime(LocalDateTime.now());
		otherStateNocDetails.setRole(role);
		dto.setOtherStateNOCActionDetails(otherStateNocDetails);
	}

	@Override
	public RegistrationDetailsVO getvehicleDetailsForarvt(String prNo, String chassisNo) {

		Optional<RegistrationDetailsDTO> registrationOptional = registrationDetailDAO.findByPrNo(prNo);

		if (!registrationOptional.isPresent()) {
			logger.error("No record found in regDetails with pr No. [{}]", prNo);
			throw new BadRequestException("No record found.pr no: " + prNo);
		}
		RegistrationDetailsDTO regDto = registrationOptional.get();
		if (!regDto.getVahanDetails().getChassisNumber().equalsIgnoreCase(chassisNo)) {
			logger.error("please provide correct chassis no[{}] for prNumber [{}]", chassisNo, prNo);
			throw new BadRequestException("please provide correct chassis number: " + prNo);
		}
		if (!regDto.isWeightAltDone()) {
			logger.error("Weight alteration not done for this vehiclce [{}]", prNo);
			throw new BadRequestException(
					"weight alteration not done for this vehicle. please apply weight alteration: " + prNo);
		}
		if (!regDto.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.ARVT.getCovCode())) {
			logger.error("Only Articulated Vehicles can apply this service [{}]", prNo);
			throw new BadRequestException("Only Articulated Vehicles can apply this service: " + prNo);
		}
		List<RegServiceDTO> listOfRegService = regServiceDAO
				.findByRegistrationDetailsApplicationNoAndServiceIdsAndSourceIsNull(regDto.getApplicationNo(),
						ServiceEnum.TAXATION.getId());
		if (!listOfRegService.isEmpty()) {
			listOfRegService.sort((s1, s2) -> s2.getCreatedDate().compareTo(s1.getCreatedDate()));
			RegServiceDTO regSerDto = listOfRegService.stream().findFirst().get();
			List<StatusRegistration> listOfStatus = new ArrayList<>();
			listOfStatus.add(StatusRegistration.CITIZENPAYMENTFAILED);
			listOfStatus.add(StatusRegistration.PAYMENTPENDING);
			if (listOfStatus.contains(regSerDto.getApplicationStatus())) {
				logger.error("Application [{}] is in progress.[{}]", regSerDto.getApplicationNo());
				throw new BadRequestException(
						"Application is in progress. Application No: " + regSerDto.getApplicationNo()
								+ ". Please verify the status of the application in registration search");
			}

		}
		regDto.setWeightAltDone(Boolean.FALSE);
		Integer changedgvw = regDto.getVahanDetails().getGvw();
		regDto.getVahanDetails().setGvw(regDto.getVahanDetails().getOldGvw());
		Optional<MasterWeightsForAlt> optionalWeigts = validationForWeightAlt(regDto);
		Integer gvw = regDto.getVahanDetails().getGvw();
		if (regDto.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.ARVT.getCovCode())) {
			if (regDto.getVahanDetails().getTrailerChassisDetailsDTO() != null
					&& !regDto.getVahanDetails().getTrailerChassisDetailsDTO().isEmpty()) {

				Integer gtw = regDto.getVahanDetails().getTrailerChassisDetailsDTO().stream().findFirst().get()
						.getGtw();
				for (TrailerChassisDetailsDTO trailerDetails : regDto.getVahanDetails().getTrailerChassisDetailsDTO()) {
					if (trailerDetails.getGtw() > gtw) {
						gtw = trailerDetails.getGtw();
					}
				}
				gvw = gvw + gtw;
			}
		}
		List<RegServiceDTO> listOfServices = regServiceDAO
				.findByRegistrationDetailsApplicationNoAndServiceIdsAndSourceIsNull(regDto.getApplicationNo(),
						ServiceEnum.ALTERATIONOFVEHICLE.getId());
		if (listOfServices == null || listOfServices.isEmpty()) {
			logger.error("weight alteration not done for this vehicle. please apply weight alteration: [{}]", prNo);
			throw new BadRequestException(
					"weight alteration not done for this vehicle. please apply weight alteration: " + prNo);
		}
		RegServiceDTO regSrevice = null;
		listOfServices.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		for (RegServiceDTO dto : listOfServices) {
			if (dto.getAlterationDetails().getAlterationService().stream()
					.anyMatch(type -> type.equals(AlterationTypeEnum.WEIGHT))) {
				regSrevice = dto;
			}
		}
		if (regSrevice == null) {
			logger.error("weight alteration document not found from service: [{}]", prNo);
			throw new BadRequestException("weight alteration document not found from service: " + prNo);
		}
		if (optionalWeigts.get().getGvw().equals(regSrevice.getAlterationDetails().getGvw())) {
			throw new BadRequestException("Tax paid for difference of waight. no need to tax for this pr: " + prNo);
		}
		RegistrationDetailsVO vo = registrationDetailsMapper.convertEntity(regDto);
		vo.getVahanDetails().setGvw(gvw);
		vo.setRlw(optionalWeigts.get().getGvw());
		Integer oldPaidWeight = changedgvw - regDto.getVahanDetails().getOldGvw();
		Integer newPaidWeight = optionalWeigts.get().getGvw() - gvw;
		Integer needToPayWeight = newPaidWeight - oldPaidWeight;
		needToPayWeight = needToPayWeight / 250;
		Long tax = roundUpperTen((Math.ceil(needToPayWeight) * 69.3));
		if (tax <= 0) {
			logger.error("Tax paid for diff weight, No need tax for this pr No[{}]", prNo);
			throw new BadRequestException("Tax paid for difference of waight. no need to tax for this pr: " + prNo);
		}

		Optional<FinalTaxHelper> mastertaxHelper = finalTaxHelperDAO.findByPrNoInAndWeightAltIsTrue(prNo);
		if (!mastertaxHelper.isPresent()) {
			TaxHelper lastTaxTillDate = citizenTaxService.getLastPaidTax(regDto, null, false,
					citizenTaxService.validity(TaxTypeEnum.QuarterlyTax.getDesc()), null, false,
					listOfQuaterTaxPayType(), false, false);
			if (lastTaxTillDate == null || lastTaxTillDate.getTax() == null
					|| lastTaxTillDate.getValidityTo() == null) {
				logger.error("Tax Details not found");
				throw new BadRequestException("TaxDetails not found");
			}
			if (lastTaxTillDate.getTaxName().equalsIgnoreCase(TaxTypeEnum.HalfyearlyTax.getDesc())) {
				tax = tax * 2;
			} else if (lastTaxTillDate.getTaxName().equalsIgnoreCase(TaxTypeEnum.YearlyTax.getDesc())) {
				tax = tax * 4;
			}
			FinalTaxHelper taxHelper = new FinalTaxHelper();
			taxHelper.setPrNo(Arrays.asList(regDto.getPrNo()));
			taxHelper.setWeightAlt(Boolean.TRUE);
			taxHelper.setExcemptionsType(ExcemptionsType.DIRECTTAXAMOUNT);
			Map<TaxTypeEnum.TaxModule, Double> map = new HashMap<>();
			map.put(TaxTypeEnum.TaxModule.TAX, tax.doubleValue());
			taxHelper.setTaxModeDetails(map);
			finalTaxHelperDAO.save(taxHelper);
		}
		return vo;

	}

	private long roundUpperTen(Double totalTax) {
		if ((totalTax % 10f) == 0) {
			return (int) Math.round(totalTax);
		} else {
			int taxIntValue = totalTax.intValue();
			if (taxIntValue % 10 == 9) {
				Double tax = Math.ceil(totalTax);
				if ((tax % 10f) != 0) {
					tax = tax + 1;
				}
				return tax.longValue();
			} else {
				return ((Math.round(totalTax) / 10) * 10 + 10);
			}
		}
	}

	@Override
	public Optional<CitizenApplicationSearchResponceVO> getApplicationSearchResultForCitizen(
			ApplicationSearchVO applicationSearchVO) {
		CitizenApplicationSearchResponceVO resultVo = null;
		CitizenSearchReportVO vcrVO = null;
		Optional<RegistrationDetailsDTO> registrationDetails = null;
		Optional<StagingRegistrationDetailsDTO> stagingRegistrationDetails = null;
		if (StringUtils.isNoneBlank(applicationSearchVO.getPrNo())) {
			registrationDetails = registrationDetailDAO.findByPrNo(applicationSearchVO.getPrNo().toUpperCase());
		}
		if (StringUtils.isNoneBlank(applicationSearchVO.getEngineNo())) {
			registrationDetails = registrationDetailDAO.findByVahanDetailsEngineNumberOrderByCreatedDateDesc(
					applicationSearchVO.getEngineNo().toUpperCase());
		}
		if (StringUtils.isNoneBlank(applicationSearchVO.getChassisNo())) {
			registrationDetails = registrationDetailDAO.findByVahanDetailsChassisNumberOrderByCreatedDateDesc(
					applicationSearchVO.getChassisNo().toUpperCase());
		}
		if (StringUtils.isNoneBlank(applicationSearchVO.getTrNo())) {
			registrationDetails = registrationDetailDAO.findByTrNo(applicationSearchVO.getTrNo().toUpperCase());
			if (!registrationDetails.isPresent()) {
				stagingRegistrationDetails = stagingRegistrationDetailsDAO
						.findByTrNo(applicationSearchVO.getTrNo().toUpperCase());
				if (stagingRegistrationDetails.isPresent()) {
					registrationDetails = Optional.of(stagingRegistrationDetails.get());
				}
			}
		}
		if (!registrationDetails.isPresent()) {
			logger.error("No record found with prNO ", applicationSearchVO.getPrNo());
			throw new BadRequestException("No record found with this PR NO: " + applicationSearchVO.getPrNo());
		}
		resultVo = setRegistrationDetailsIntoResultVO(registrationDetails.get());
		try {
			vcrVO = this.applicationSearchForVcr(applicationSearchVO);
		} catch (Exception e) {
			logger.error("Exception while getting vcrDetails with prNo: " + applicationSearchVO.getPrNo());
		}
		resultVo.setCitizenSearchReportVO(vcrVO);

		if (StringUtils.isNoneBlank(applicationSearchVO.getTrNo())) {
			resultVo.setTaxValidUpto(stagingRegistrationDetails.get().getTaxvalidity());
			resultVo.setTaxPaidDate(stagingRegistrationDetails.get().getTrGeneratedDate().toLocalDate());
		}
		return Optional.of(resultVo);
	}

	@Override
	public CitizenApplicationSearchResponceVO setRegistrationDetailsIntoResultVO(
			RegistrationDetailsDTO registrationDetailsDTO) {
		Optional<TaxDetailsDTO> taxDto = null;
		Optional<PermitDetailsDTO> permitDetails = null;
		FcDetailsDTO fcDetails = null;
		CitizenApplicationSearchResponceVO vo = registrationDetailsMapper
				.converSpecificicFireldForReport(registrationDetailsDTO);
		List<FcDetailsDTO> fcDetailsOpt = fcDetailsDAO
				.findFirst5ByStatusIsTrueAndPrNoOrderByCreatedDateDesc(registrationDetailsDTO.getPrNo());
		if (CollectionUtils.isNotEmpty(fcDetailsOpt)) {
			logger.info("Fc Details found for the prNo [{}]", registrationDetailsDTO.getPrNo());
			fcDetails = fcDetailsOpt.stream().findFirst().get();
		}
		taxDto = getLatestTaxTransaction(registrationDetailsDTO.getPrNo());
		if (taxDto.isPresent()) {
			vo.setTaxAmount(taxDto.get().getTaxAmount().toString());
			vo.setTaxPaidDate(taxDto.get().getTaxPeriodFrom());
			vo.setTaxValidUpto(taxDto.get().getTaxPeriodEnd());
		} else {
			logger.error("Tax Details not found for the prNo [{}]", registrationDetailsDTO.getPrNo());
			if (registrationDetailsDTO.getTaxAmount() != null && registrationDetailsDTO.getTaxvalidity() != null) {
				logger.info("Tax details getting from registrationDetails dto");
				vo.setTaxAmount("0");
				if (null != registrationDetailsDTO.getTaxAmount()) {
					vo.setTaxAmount(registrationDetailsDTO.getTaxAmount().toString());
				}
				vo.setTaxPaidDate(registrationDetailsDTO.getRegistrationValidity().getPrGeneratedDate());
				vo.setTaxValidUpto(registrationDetailsDTO.getTaxvalidity());
			}
		}
		if (fcDetails != null) {
			vo.setFcIssuedDate(fcDetails.getFcIssuedDate().toLocalDate());
			vo.setFcNumber(fcDetails.getFcNumber());
			vo.setFcValidUpto(fcDetails.getFcValidUpto());
			vo.setRegistrationValidUpto(fcDetails.getFcValidUpto());
		}
		permitDetails = permitDetailsDAO.findByPrNoAndPermitStatusAndPermitTypeTypeofPermitOrderByCreatedDateDesc(
				registrationDetailsDTO.getPrNo(), PermitsEnum.ACTIVE.getDescription(),
				PermitType.PRIMARY.getPermitTypeCode());
		if (permitDetails.isPresent()) {
			logger.info("Permit Details found for prNo:-[{}]", registrationDetailsDTO.getPrNo());
			vo.setPemitNumber(permitDetails.get().getPermitNo());
			vo.setPermitType(permitDetails.get().getPermitType().getDescription());
			vo.setPermitIssueDate(permitDetails.get().getPermitValidityDetails().getPermitValidFrom());
			vo.setPermitValidUpto(permitDetails.get().getPermitValidityDetails().getPermitValidTo());
		}
		if (StringUtils.isBlank(registrationDetailsDTO.getVehicleType())) {
			MasterCovDTO masterDto = masterCovDAO.findByCovcode(registrationDetailsDTO.getClassOfVehicle());
			vo.setVehicleType(masterDto.getCategory());
		} else {
			vo.setVehicleType(registrationDetailsDTO.getVehicleType());
		}
		if (registrationDetailsDTO.getNocDetails() != null
				&& StringUtils.isNotBlank(registrationDetailsDTO.getNocDetails().getState())
				&& StringUtils.isNotBlank(registrationDetailsDTO.getNocDetails().getDistrict())) {
			vo.setNocTo(registrationDetailsDTO.getNocDetails().getState() + "-"
					+ registrationDetailsDTO.getNocDetails().getDistrict());
			vo.setNocDate(registrationDetailsDTO.getNocDetails().getlUpdate().toLocalDate());
		}
		if (registrationDetailsDTO.getVahanDetails() != null
				&& StringUtils.isNoneBlank(registrationDetailsDTO.getVahanDetails().getChassisNumber())
				&& StringUtils.isNoneBlank(registrationDetailsDTO.getVahanDetails().getEngineNumber())) {
			vo.setAppChassiNumber(registrationDetailsDTO.getVahanDetails().getChassisNumber());
			vo.setAppEngineNumber(registrationDetailsDTO.getVahanDetails().getEngineNumber());

			String chassisNumber = registrationDetailsDTO.getVahanDetails().getChassisNumber();
			chassisNumber = chassisNumber.length() >= 5
					? chassisNumber.substring(0, chassisNumber.length() - 5) + "*****"
					: chassisNumber.replaceAll(chassisNumber, "****");
			String engineNumber = registrationDetailsDTO.getVahanDetails().getEngineNumber();
			engineNumber = engineNumber.length() >= 5 ? engineNumber.substring(0, engineNumber.length() - 5) + "*****"
					: engineNumber.replaceAll(engineNumber, "****");
			vo.setEngineNumber(engineNumber);
		}

		if (registrationDetailsDTO.getVahanDetails() != null
				&& StringUtils.isNotBlank(registrationDetailsDTO.getVahanDetails().getBodyTypeDesc())) {
			vo.setBodyType(registrationDetailsDTO.getVahanDetails().getBodyTypeDesc());
		}
		if (registrationDetailsDTO.getVahanDetails() != null
				&& StringUtils.isNotBlank(registrationDetailsDTO.getVahanDetails().getSeatingCapacity())) {
			vo.setSeatingCapacity(registrationDetailsDTO.getVahanDetails().getSeatingCapacity());
		}
		if (registrationDetailsDTO.isVehicleStoppaged()) {
			vo.setVehicleStoppaged(registrationDetailsDTO.isVehicleStoppaged());
			if (registrationDetailsDTO.getStoppageDate() != null) {
				vo.setStoppageDate(registrationDetailsDTO.getStoppageDate());
			}
		}
		if (registrationDetailsDTO.getInsuranceDetails() != null) {
			vo.setInsurenceVO(insuranceDetailsMapper.convertEntity(registrationDetailsDTO.getInsuranceDetails()));
		}

		if (registrationDetailsDTO.getApplicationStatus() != null && registrationDetailsDTO.getApplicationStatus()
				.equalsIgnoreCase(StatusRegistration.PRGENERATED.getDescription())) {
			vo.setStatus("ACTIVE");
		} else if (registrationDetailsDTO.getApplicationStatus() != null && registrationDetailsDTO
				.getApplicationStatus().equalsIgnoreCase(StatusRegistration.RCCANCELLED.getDescription())) {
			vo.setStatus("CANCELLED");
		}
		Optional<RegServiceDTO> regSerDTO = regServiceDAO
				.findByPrNoOrderByCreatedDateDesc(registrationDetailsDTO.getPrNo());
		if (regSerDTO.isPresent()) {
			if (CollectionUtils.isNotEmpty(regSerDTO.get().getServiceType())) {
				if (regSerDTO.get().getServiceType().stream().anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE))) {
					vo.setStatus("STOPPAGE");
				} else if (regSerDTO.get().getServiceType().stream()
						.anyMatch(serviceType -> serviceType.equals(ServiceEnum.THEFTINTIMATION))) {
					vo.setStatus("THEFT");
				}
			}
		}
		Optional<RCActionsDTO> rcActions = suspensionDAO.findByPrNoOrderByLUpdateDesc(registrationDetailsDTO.getPrNo());
		if (rcActions.isPresent()) {
			if (rcActions.get().getActionStatus() != null
					&& rcActions.get().getActionStatus().equals(Status.RCActionStatus.SUSPEND)) {
				vo.setStatus("SUSPENDED");
			}
		}
		return vo;
	}

	@Override
	public Optional<TaxDetailsDTO> getLatestTaxTransaction(String prNo) {
		List<String> taxTypes = new ArrayList<>();
		taxTypes.add(ServiceCodeEnum.QLY_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.HALF_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.YEAR_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.LIFE_TAX.getCode());
		List<TaxDetailsDTO> taxsList = taxDetailsDAO.findFirst10ByPrNoAndPaymentPeriodInOrderByCreatedDateDesc(prNo,
				taxTypes);
		if (CollectionUtils.isNotEmpty(taxsList)) {
			TaxDetailsDTO taxDto = new TaxDetailsDTO();
			updatePaidDateAsCreatedDate(taxsList);
			taxsList.sort((o1, o2) -> o2.getCreatedDate().compareTo(o1.getCreatedDate()));
			taxDto = taxsList.get(0);
			taxsList.clear();
			return Optional.of(taxDto);
		}
		logger.debug("TaxList not found for prno[{}]", prNo);
		return Optional.empty();
	}

	@Override
	public Optional<RegServiceVO> fetchRegServiceDetails(String applNo) {
		Optional<RegServiceDTO> regSerDTO = regServiceDAO.findByApplicationNo(applNo);
		if (!regSerDTO.isPresent()) {
			logger.error("No record found with this Application NO: [{}]", applNo);
			throw new BadRequestException("No record found with this Application NO: " + applNo);
		}

		return regServiceMapper.convertEntity(regSerDTO);
	}

	@Override
	public List<String> getBiLateralTaxCovs() {
		List<String> list = new ArrayList<>();

		Optional<PropertiesDTO> opTionalDoc = propertiesDAO.findByBiLateralTaxCovsTrue();
		if (!opTionalDoc.isPresent()) {
			logger.error("No Class of Vehicles for bilateral tax");

			throw new BadRequestException("No class of vehicles for bilateral tax");
		}
		for (String cov : opTionalDoc.get().getCovs()) {
			MasterCovDTO masterDto = masterCovDAO.findByCovcode(cov);
			list.add(masterDto.getCovdescription());
		}
		return list;
	}

	@Override
	public RegServiceVO getvehicleDetailsForBiLateralTax(String prNo, String purpose) {

		List<RegServiceDTO> list = regServiceDAO.findByPrNo(prNo.toUpperCase());
		if (list != null && !list.isEmpty()) {
			logger.info("Registration Services found with prNo [{}]", prNo);
			for (RegServiceDTO dto : list) {
				if (dto.getCreatedDate() == null) {
					dto.setCreatedDate(LocalDateTime.now());
				}
			}
			list.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
			RegServiceDTO serviceDto = list.stream().findFirst().get();
			// TODO need to check in save also
			if (!serviceDto.getApplicationStatus().equals(StatusRegistration.APPROVED)) {
				logger.error("Application [{}] in pending state", serviceDto.getApplicationNo());
				throw new BadRequestException("Application is at pending state.Please proced in application status."
						+ " Application number is: " + serviceDto.getApplicationNo());
			}
			Optional<BileteralTaxDTO> optionalBilateralDetails = bileteralTaxDAO
					.findByApplicationNo(serviceDto.getApplicationNo());
			if (!optionalBilateralDetails.isPresent()) {
				logger.error("Bilateral Details not found for [{}]", serviceDto.getApplicationNo());
				throw new BadRequestException("Bilater detalis not found for: " + serviceDto.getApplicationNo());
			}
			BileteralTaxDTO bilateralDto = optionalBilateralDetails.get();
			if (/* purpose.equalsIgnoreCase("FRESH") || */ purpose.equalsIgnoreCase(PurposeEnum.RENEWAL.getCode())) {
				if (bilateralDto.getValidityTo().isAfter(LocalDate.now())) {
					logger.error("Validity uptodate for prno:[{}]", prNo);
					throw new BadRequestException("validity upto date: " + prNo.toUpperCase());
				}
			}
			if (purpose.equalsIgnoreCase(PurposeEnum.TRANSFER.getCode())
					|| purpose.equalsIgnoreCase(PurposeEnum.VEHICLEREPLACE.getCode())) {
				if (bilateralDto.getValidityTo().isBefore(LocalDate.now())) {
					logger.error("Validity Completed for prNo: [{}]", prNo);
					throw new BadRequestException(
							"Validity completed. Please select fresh or renewal for: " + prNo.toUpperCase());
				}
			}

			RegServiceVO vo = regServiceMapper.convertEntity(serviceDto);
			vo.getBileteralTaxDetails().setPurpose(null);
			return vo;
		} else {
			if (!purpose.equalsIgnoreCase(PurposeEnum.FRESH.getCode())) {
				logger.error("No Records with this Vehicle number [{}]", prNo);
				throw new BadRequestException(
						"No records on this vehicle number. Please select fresh for: " + prNo.toUpperCase());
			} else {
				logger.error("No record with this Vehicle [{}]", prNo);
				throw new BadRequestException(
						"No vehicle details found. Please enter vehicle details for: " + prNo.toUpperCase());
			}
		}
		// return null;
	}

	@Override
	public VcrDetailsVO verifyOtherStateVCR(VcrValidationVo vcrValidationVo) {
		VcrInputVo vcrInputVo = new VcrInputVo();
		boolean offlineVcrPaid = false;
		vcrInputVo.setDocumentType("RC");
		vcrInputVo.setRegNo(vcrValidationVo.getPrNo().toUpperCase());
		VcrBookingData entity = restGateWayService.getVcrDetailsCfst(vcrInputVo);
		if (entity != null) {
			if (entity.getVcrStatus().equalsIgnoreCase("O")) {
				logger.error("VCr is pending on the prNo[{}]", vcrValidationVo.getPrNo());
				throw new BadRequestException("VCR is pending on this PRNO :" + vcrValidationVo.getPrNo()
						+ " with VCR NO :" + entity.getVcrNum());
			}
		}
		TaxPaidVCRDetailsVO taxPaidVcrNums = null;
		// online vcr
		if (StringUtils.isNotBlank(vcrValidationVo.getPrNo()) && StringUtils.isNotBlank(vcrValidationVo.getVcrNum())) {
			taxPaidVcrNums = getOnlineVcrDetalsForOtherState(vcrValidationVo);
		}
		if (taxPaidVcrNums == null || CollectionUtils.isEmpty(taxPaidVcrNums.getTaxPaidVcrDetails())) {

			taxPaidVcrNums = restGateWayService.getTaxPaidVCRData(vcrValidationVo.getPrNo());
		}
		VcrDetailsVO offlineVcrTax = new VcrDetailsVO();

		if (taxPaidVcrNums != null && !CollectionUtils.isEmpty(taxPaidVcrNums.getTaxPaidVcrDetails())) {
			for (VcrTaxDetails vcrTax : taxPaidVcrNums.getTaxPaidVcrDetails()) {

				if (vcrValidationVo.getPrNo().equalsIgnoreCase(vcrTax.getRegnNo())
						&& (vcrValidationVo.getVcrNum().equalsIgnoreCase(vcrTax.getVcrNo())
								|| vcrValidationVo.getVcrNum().equalsIgnoreCase(vcrTax.getManVcrNo()))) {

					if (vcrTax.getTaxAmt() == null || vcrTax.getTaxAmt() <= 0) {
						logger.error("VCR doesnt have tax amount for VCRNumber: [{}]", vcrValidationVo.getVcrNum());
						throw new BadRequestException(
								"This VCR doesn't have tax amount. Please check : " + vcrValidationVo.getVcrNum());
					}
					Optional<MasterPayperiodDTO> masterPayPeridOptional = masterPayperiodDAO
							.findByCovcode(vcrValidationVo.getCovCode());
					if (!masterPayPeridOptional.isPresent()) {
						logger.warn("Pay Period not available for COV Code:[{}]", vcrValidationVo.getCovCode());
						throw new BadRequestException("Pay period not available for cov code");
					}
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/MM/yyyy");
					LocalDate paydate = LocalDate.parse(vcrTax.getPaymtDate(), formatter);
					VcrDetailsVO vcrDetailsVO = new VcrDetailsVO();
					VcrDetailsDto vcrDto = new VcrDetailsDto();
					if (masterPayPeridOptional.get().getPayperiod().equals("Q")) {
						Integer payYear = paydate.getYear();
						Integer payMonth = paydate.getMonthValue();

						Optional<MasterQuaterPeriodDTO> monthsList = masterQuaterPeriodDAO.findByQuaterMonths(payMonth);
						if (!monthsList.isPresent()) {
							logger.error("data not available to calculate tax");
							throw new BadRequestException("data not available to calculate tax");
						}
						String date = monthsList.get().getQuaterLastMonthDate();
						date = date.replaceAll("@YEAR@", payYear.toString());
						vcrDetailsVO.setVcrQuaterTaxValidUpto(date);
						vcrDto.setVcrQuaterTaxValidUpto(date);
						/*
						 * List<Integer> months = new ArrayList<>(); Integer currYear =
						 * LocalDate.now().getYear(); Integer currMonth =
						 * LocalDate.now().getMonthValue(); months.add(currMonth); Integer payYear =
						 * paydate.getYear(); Integer payMonth = paydate.getMonthValue();
						 * months.add(payMonth); if (currYear.intValue() != payYear.intValue()) {
						 * logger.error("Tax not found for this Quarter ,please pay Tax for prno:: [{}]"
						 * , vcrValidationVo.getPrNo()); throw new
						 * BadRequestException("Tax not found for this quater,so please pay the tax"); }
						 * if (payMonth != currMonth) { List<MasterQuaterPeriodDTO> monthsList =
						 * masterQuaterPeriodDAO.findByQuaterMonthsIn(months); if
						 * (CollectionUtils.isEmpty(monthsList)) {
						 * logger.warn("Master data not availabe to calcualte tax [{}]"); throw new
						 * BadRequestException("master data not available to calculate tax"); } if
						 * (monthsList.size() != 1) {
						 * logger.info("Tax not found for this quarter so please pay the tax "); throw
						 * new
						 * BadRequestException("Tax not found for this quater,so please pay the tax"); }
						 * }
						 */

					}
					offlineVcrPaid = true;
					logger.info("vcr details founded to save in DB");
					vcrDto.setVcrTaxDetails(vcrTaxPaidMapper.convertVO(vcrTax));
					vcrDto.setCreatedDate(LocalDateTime.now());
					vcrDto.setCreatedDateStr(LocalDateTime.now().toString());
					vcrDetailsVO.setBookDate(vcrTax.getBookDate());
					vcrDetailsVO.setCfAmnt(vcrTax.getCfAmnt());
					vcrDetailsVO.setChallanNo(vcrTax.getChallanNo());
					vcrDetailsVO.setManVcrNo(vcrTax.getManVcrNo());
					vcrDetailsVO.setVcrNo(vcrTax.getVcrNo());
					vcrDetailsVO.setMviCode(vcrTax.getMviCode());
					vcrDetailsVO.setMviName(vcrTax.getMviName());
					vcrDetailsVO.setPaymtDate(DateConverters.convertLocalDateFormat(paydate));
					vcrDetailsVO.setTaxAmt(vcrTax.getTaxAmt());
					vcrDetailsDAO.save(vcrDto);
					offlineVcrTax = vcrDetailsVO;
					// return vcrDetailsVO;
				}

			}
		}
		List<TaxDetailsDTO> taxList = null;
		List<VcrFinalServiceDTO> VcrData = this.getOnlineVcrData(vcrInputVo);
		if (VcrData != null && VcrData.size() > 0) {
			VcrFinalServiceDTO singleVcr = VcrData.stream().findFirst().get();

			if (StringUtils.isNoneBlank(vcrInputVo.getChassisNo())) {
				taxList = taxDetailsDAO.findFirst10ByChassisNoOrderByCreatedDateDesc(vcrInputVo.getChassisNo());

			} else {

				taxList = taxDetailsDAO
						.findFirst10ByChassisNoOrderByCreatedDateDesc(singleVcr.getRegistration().getChassisNumber());
			}

			if (!offlineVcrPaid && (taxList == null || taxList.isEmpty())) {
				logger.warn("Vcr Details not found for prNo:[{}]", vcrInputVo.getChassisNo());
				throw new BadRequestException("vcr records not found");
			}
			if (taxList != null && !taxList.isEmpty()) {
				List<TaxDetailsDTO> vcrTax = taxList.stream()
						.filter(tax -> tax.getTaxPaidThroughVcr() != null && tax.getTaxPaidThroughVcr())
						.collect(Collectors.toList());
				if (!offlineVcrPaid && (vcrTax == null || vcrTax.isEmpty())) {
					logger.warn("Vcr Details not found for prNo:[{}]", vcrInputVo.getChassisNo());
					throw new BadRequestException("vcr records not found");
				}
				if (vcrTax != null && !vcrTax.isEmpty()) {
					TaxDetailsDTO taxDto = vcrTax.stream().findFirst().get();
					if (offlineVcrTax != null && StringUtils.isNoneBlank(offlineVcrTax.getVcrQuaterTaxValidUpto())
							&& LocalDate.parse(offlineVcrTax.getVcrQuaterTaxValidUpto())
									.isAfter(taxDto.getTaxPeriodEnd())) {
						return offlineVcrTax;
					}
					VcrDetailsVO onlineVcrDetails = new VcrDetailsVO();
					onlineVcrDetails.setBookDate(singleVcr.getVcr().getDateOfCheck().toLocalDate().toString());
					onlineVcrDetails.setCfAmnt(singleVcr.getOffencetotal().doubleValue());
					onlineVcrDetails.setVcrNo(singleVcr.getVcr().getVcrNumber());
					onlineVcrDetails.setMviCode(singleVcr.getUsername());
					onlineVcrDetails.setMviName(singleVcr.getUsername());
					onlineVcrDetails.setPaymtDate(taxDto.getTaxPaidDate().toString());
					onlineVcrDetails.setTaxAmt(taxDto.getTaxAmount().doubleValue());
					onlineVcrDetails.setVcrQuaterTaxValidUpto(taxDto.getTaxPeriodEnd().toString());
					return onlineVcrDetails;
				}
			}

			if (taxPaidVcrNums == null || CollectionUtils.isEmpty(taxPaidVcrNums.getTaxPaidVcrDetails())) {
				logger.warn("Vcr Details not found for prNo:[{}]", vcrValidationVo.getPrNo());
				throw new BadRequestException("vcr records not found");
			}
			logger.error("VCR Details not found with given vcr no [{}] or PrNo", vcrValidationVo.getVcrNum(),
					vcrValidationVo.getPrNo());
			throw new BadRequestException("vcr details not found with given vcr no" + vcrValidationVo.getVcrNum()
					+ "or PrNo" + vcrValidationVo.getPrNo() + " Please give correct vcr no");
		}
		return offlineVcrTax;
	}

	@Override
	public List<CitizenSearchReportVO> getNocPendingApplications(String officeCode) {

		List<RegServiceDTO> nocPendingList = regServiceDAO.findByOfficeCodeAndApplicationStatusAndOtherStateNOCStatus(
				officeCode, StatusRegistration.APPROVED, StatusRegistration.NOCVERIFICATIONPENDING);
		if (CollectionUtils.isNotEmpty(nocPendingList)) {
			return regServiceMapper.convertSpecificFieldsForCtizenSearch(nocPendingList);
		}
		logger.info("Noc pending list not found");
		return Collections.emptyList();
	}

	@Override
	public Boolean checkIsPanrequired(String cov) {
		MasterCovDTO masterCovDTO = masterCovDAO.findByCovcode(cov);
		if (masterCovDTO != null && masterCovDTO.getPanrequired().equals(Boolean.TRUE)) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	/**
	 * getAadhaarResponseService
	 */
	//disabling apt online aadhaar services
//	@Override
//	public Optional<AadharDetailsResponseVO> getAadhaarResponseService(AadhaarDetailsRequestVO requestModel) {
//
//		Optional<AadharDetailsResponseVO> otpResponse = restGateWayService.validateAadhaar(requestModel, null);
//
//		if (null == requestModel.getRequestType()
//				|| AadhaarConstants.RequestType.EKYC.getContent().equals(requestModel.getRequestType())) {
//			if (otpResponse.isPresent()) {
//				Optional<ApplicantDetailsDTO> applicant = applicantDetailsDAO
//						.findByUidToken(otpResponse.get().getUidToken());
//				logger.info("applicant details search with uidToken[{}],found aadhaar No as [{}]",
//						otpResponse.get().getUidToken(), otpResponse.get().getUid_num());
//				ApplicantDetailsDTO applicantDto = null;
//				if (applicant.isPresent()) {
//					logger.info("applicant found in applicant_details with uidToken[{}]",
//							otpResponse.get().getUidToken());
//					applicantDto = applicant.get();
//					applicantDto.setModifiedDate(LocalDateTime.now());
//					applicantDto.setModifiedDateStr(LocalDateTime.now().toString());
//					applicantDto.setlUpdate(LocalDateTime.now());
//					applicantDto.setAadharNo(applicant.get().getAadharNo());
//					logger.info("Applicant aadharNo set for existing Applicants [{}] ", applicant.get().getAadharNo());
//				} else {
//					// logger.info("New applicant with uidToken [{}]",
//					// applicant.get().getUidToken());
//					applicantDto = new ApplicantDetailsDTO();
//					applicantDto.setApplicantNo(dealerService.getTransactionId());
//					AadhaarDetailsResponseDTO dto = new AadhaarDetailsResponseDTO();
//					dto = aadhaarDetailsResponseMapper.convertVO(otpResponse.get());
//					dto.setUidToken(otpResponse.get().getUidToken());
//					// save Aaadhaar response in applicant_details
//					applicantDto.setAadharResponse(dto);
//					logger.info("aadhaar Response Saved in applicantDetailsDto");
//					applicantDto.setCreatedDate(LocalDateTime.now());
//					applicantDto.setCreatedDateStr(LocalDateTime.now().toString());
//					applicantDto.setModifiedDate(LocalDateTime.now());
//					applicantDto.setModifiedDateStr(LocalDateTime.now().toString());
//					applicantDto.setlUpdate(LocalDateTime.now());
//					applicantDto.setUidToken(otpResponse.get().getUidToken());
//					if (null != otpResponse.get().getUid()) {
//						applicantDto.setAadharNo(otpResponse.get().getUid().toString());
//					} else {
//						logger.error("Exception from aadhar resp: [{}] and status is : [{}]",
//								otpResponse.get().getAuth_err_code(), otpResponse.get().getStatecode());
//						throw new BadRequestException(otpResponse.get().getAuth_err_code());
//					}
//				}
//				if (requestModel.getRepresentativeVo() != null) {
//					ContactDTO contactDto = new ContactDTO();
//					contactDto.setMobile(requestModel.getRepresentativeVo().getMobileNo());
//					applicantDto.setContact(contactDto);
//				}
//				applicantDetailsDAO.save(applicantDto);
//				logger.info("Applicant Details saved in applicant_details");
//				if (requestModel.getIsRepresentativeService()) {
//					otpResponse.get().setRepresentativeVO(createParent(otpResponse.get()));
//				}
//			}
//		}
//		return otpResponse;
//	}
	
	@Override
	public Optional<AadharDetailsResponseVO> getAadhaarResponseService(AadhaarDetailsRequestVO requestModel) {

		Optional<AadharDetailsResponseVO> otpResponse = restGateWayService.validateAadhaar(requestModel, null);
		
		if (null == requestModel.getRequestType()
				|| AadhaarConstants.RequestType.EKYC.getContent().equals(requestModel.getRequestType())) {
			if (null == otpResponse.get().getUid()) {
				logger.error("Exception from aadhar resp: [{}] and status is : [{}]",
						otpResponse.get().getAuth_err_code(), otpResponse.get().getStatecode());
				throw new BadRequestException(otpResponse.get().getAuth_err_code());
			}
			if (otpResponse.isPresent()) {
				List<ApplicantDetailsDTO> applicantDetailsList = applicantDetailsDAO.findByAadharNo(String.valueOf(otpResponse.get().getUid()));
				logger.info("applicant details search with uidToken[{}],found aadhaar No as [{}]",
						otpResponse.get().getUidToken(), otpResponse.get().getUid_num());
				if(CollectionUtils.isNotEmpty(applicantDetailsList)) {
					applicantDetailsList.stream().forEach(applicantDetails->{
						logger.info("applicant found in applicant_details with uidToken[{}]",
								otpResponse.get().getUidToken());
						applicantDetails.setModifiedDate(LocalDateTime.now());
						applicantDetails.setModifiedDateStr(LocalDateTime.now().toString());
						applicantDetails.setlUpdate(LocalDateTime.now());
						applicantDetails.setAadharNo(applicantDetails.getAadharNo());
						logger.info("Applicant aadharNo set for existing Applicants [{}] ", applicantDetails.getAadharNo());
						if (requestModel.getRepresentativeVo() != null) {
							ContactDTO contactDto = new ContactDTO();
							contactDto.setMobile(requestModel.getRepresentativeVo().getMobileNo());
							applicantDetails.setContact(contactDto);
						}
					});
					applicantDetailsDAO.save(applicantDetailsList);
				}else {

					// logger.info("New applicant with uidToken [{}]",
					// applicant.get().getUidToken());
					ApplicantDetailsDTO applicantDto = new ApplicantDetailsDTO();
					applicantDto.setApplicantNo(dealerService.getTransactionId());
//					AadhaarDetailsResponseDTO dto = new AadhaarDetailsResponseDTO();
//					dto = aadhaarDetailsResponseMapper.convertVO(otpResponse.get());
					AadhaarDetailsResponseDTO dto = aptsAadhaarResponseMapper.convertDTO(otpResponse.get());
					dto.setUidToken(otpResponse.get().getUidToken());
					// save Aaadhaar response in applicant_details
					applicantDto.setAadharResponse(dto);
					logger.info("aadhaar Response Saved in applicantDetailsDto");
					applicantDto.setCreatedDate(LocalDateTime.now());
					applicantDto.setCreatedDateStr(LocalDateTime.now().toString());
					applicantDto.setModifiedDate(LocalDateTime.now());
					applicantDto.setModifiedDateStr(LocalDateTime.now().toString());
					applicantDto.setlUpdate(LocalDateTime.now());
					applicantDto.setUidToken(otpResponse.get().getUidToken());
					applicantDto.setAadharNo(String.valueOf(otpResponse.get().getUid()));
					if (requestModel.getRepresentativeVo() != null) {
						ContactDTO contactDto = new ContactDTO();
						contactDto.setMobile(requestModel.getRepresentativeVo().getMobileNo());
						applicantDto.setContact(contactDto);
					}
					applicantDetailsDAO.save(applicantDto);
				}
//				Optional<ApplicantDetailsDTO> applicant = applicantDetailsDAO
//				.findByUidToken(otpResponse.get().getUidToken());
//				ApplicantDetailsDTO applicantDto = null;
//				if (applicant.isPresent()) {
//					logger.info("applicant found in applicant_details with uidToken[{}]",
//							otpResponse.get().getUidToken());
//					applicantDto = applicant.get();
//					applicantDto.setModifiedDate(LocalDateTime.now());
//					applicantDto.setModifiedDateStr(LocalDateTime.now().toString());
//					applicantDto.setlUpdate(LocalDateTime.now());
//					applicantDto.setAadharNo(applicant.get().getAadharNo());
//					logger.info("Applicant aadharNo set for existing Applicants [{}] ", applicant.get().getAadharNo());
//				} else {
//					// logger.info("New applicant with uidToken [{}]",
//					// applicant.get().getUidToken());
//					applicantDto = new ApplicantDetailsDTO();
//					applicantDto.setApplicantNo(dealerService.getTransactionId());
//					AadhaarDetailsResponseDTO dto = new AadhaarDetailsResponseDTO();
//					dto = aadhaarDetailsResponseMapper.convertVO(otpResponse.get());
//					dto.setUidToken(otpResponse.get().getUidToken());
//					// save Aaadhaar response in applicant_details
//					applicantDto.setAadharResponse(dto);
//					logger.info("aadhaar Response Saved in applicantDetailsDto");
//					applicantDto.setCreatedDate(LocalDateTime.now());
//					applicantDto.setCreatedDateStr(LocalDateTime.now().toString());
//					applicantDto.setModifiedDate(LocalDateTime.now());
//					applicantDto.setModifiedDateStr(LocalDateTime.now().toString());
//					applicantDto.setlUpdate(LocalDateTime.now());
//					applicantDto.setUidToken(otpResponse.get().getUidToken());
//					if (null != otpResponse.get().getUid()) {
//						applicantDto.setAadharNo(otpResponse.get().getUid().toString());
//					} else {
//						logger.error("Exception from aadhar resp: [{}] and status is : [{}]",
//								otpResponse.get().getAuth_err_code(), otpResponse.get().getStatecode());
//						throw new BadRequestException(otpResponse.get().getAuth_err_code());
//					}
//				}
//				if (requestModel.getRepresentativeVo() != null) {
//					ContactDTO contactDto = new ContactDTO();
//					contactDto.setMobile(requestModel.getRepresentativeVo().getMobileNo());
//					applicantDto.setContact(contactDto);
//				}
//				applicantDetailsDAO.save(applicantDto);
				logger.info("Applicant Details saved in applicant_details");
				if (requestModel.getIsRepresentativeService()) {
					otpResponse.get().setRepresentativeVO(createParent(otpResponse.get()));
				}
			}
		}
		return otpResponse;
	}

	@Override
	public RegServiceVO saveBilateralTax(String regServiceVO, MultipartFile[] uploadfiles) {

		if (StringUtils.isBlank(regServiceVO)) {
			throw new BadRequestException("Input Details are required.");
		}
		Optional<RegServiceVO> inputOptional = readValue(regServiceVO, RegServiceVO.class);
		if (!inputOptional.isPresent()) {
			logger.warn("Error in VO ", regServiceVO);
			throw new BadRequestException("Invalid Input Details.");
		}
		synchronized (inputOptional.get().getBileteralTaxDetails().getPrNo().intern()) {
			try {
				RegServiceVO inputRegServiceVO = inputOptional.get();
				if (inputRegServiceVO.getServiceIds() == null || inputRegServiceVO.getServiceIds().isEmpty()) {
					throw new BadRequestException("Please provide service type.");
				}
				if (!inputRegServiceVO.getServiceIds().stream()
						.anyMatch(id -> id.equals(ServiceEnum.BILLATERALTAX.getId()))) {
					throw new BadRequestException("Invalid Service Type.");
				}
				RegServiceDTO dto = this.converBilaterTaxDetails(inputRegServiceVO);

				validationForRlNonadCount(dto.getBileteralTaxDetails().getPrNo(),
						dto.getBileteralTaxDetails().getPurpose(),
						dto.getBileteralTaxDetails().getRecommendationLetterNo(),
						dto.getBileteralTaxDetails().getPermitIssuedBy());
				Optional<DistrictDTO> disOptional = districtDAO
						.findByDistrictName(dto.getBileteralTaxDetails().getDistrict());
				if (!disOptional.isPresent()) {
					logger.error("District Details not found for ", dto.getBileteralTaxDetails().getDistrict());
					throw new BadRequestException(
							"district details not found for: " + dto.getBileteralTaxDetails().getDistrict());
				}
				Optional<OfficeDTO> optionalOffice = officeDAO
						.findByOfficeCode(disOptional.get().getBiLateralTaxOfficeCode());
				if (!optionalOffice.isPresent()) {
					logger.warn("office details not found for:", disOptional.get().getBiLateralTaxOfficeCode());
					throw new BadRequestException(
							"office details not found for: " + disOptional.get().getBiLateralTaxOfficeCode());
				}
				ClassOfVehiclesVO vo = covService
						.findByCovdescription(dto.getBileteralTaxDetails().getClassOfVehicle());
				dto.getBileteralTaxDetails().setCovCode(vo.getCovCode());
				// dto.getBileteralTaxDetails().setValidityTo(citizenTaxService.getBilaterTaxUpTo(LocalDate.now()));
				dto.setlUpdate(LocalDateTime.now());
				dto.setOfficeDetails(optionalOffice.get());
				dto.setOfficeCode(optionalOffice.get().getOfficeCode());
				Map<String, String> officeCodeMap = new HashMap<>();
				officeCodeMap.put("officeCode", "APSTA");
				dto.setApplicationNo(sequenceGenerator
						.getSequence(String.valueOf(Sequence.REGISTRATIONAPPNO.getSequenceId()), officeCodeMap));
				saveCitizenServiceDoc(inputRegServiceVO, dto, uploadfiles);
				return regServiceMapper.convertEntity(dto);
			} catch (Exception e) {
				logger.error("Exception [{}] in getBileteralTaxDetails for prNumber :[{}]", e,
						inputOptional.get().getBileteralTaxDetails().getPrNo());
				throw new BadRequestException(e.getMessage());
			}
		}
	}

	private RegServiceDTO converBilaterTaxDetails(RegServiceVO vo) {
		RegServiceDTO dto = new RegServiceDTO();
		if (vo.getBileteralTaxDetails() == null) {
			throw new BadRequestException("Please provide bilaterail tax details");
		}
		if (StringUtils.isBlank(vo.getBileteralTaxDetails().getPurpose())) {
			throw new BadRequestException("Please provide purpose");
		}
		if (StringUtils.isBlank(vo.getBileteralTaxDetails().getPrNo())) {
			throw new BadRequestException("Please provide prNo");
		}
		if (StringUtils.isBlank(vo.getBileteralTaxDetails().getClassOfVehicle())) {
			throw new BadRequestException("Please provide class of vehicle");
		}
		if (vo.getBileteralTaxDetails().getGvw() == null) {
			throw new BadRequestException("Please provide gvw");
		}
		if (StringUtils.isBlank(vo.getBileteralTaxDetails().getOwnerName())) {
			throw new BadRequestException("Please provide owner name");
		}
		if (StringUtils.isBlank(vo.getBileteralTaxDetails().getContactNo())) {
			throw new BadRequestException("Please provide contact no");
		}
		if (vo.getBileteralTaxDetails().getHomeTaxValidTo() == null) {
			throw new BadRequestException("Please provide home tax valid to");
		}
		if (vo.getBileteralTaxDetails().getIcValidTo() == null) {
			throw new BadRequestException("Please provide IC valid to");
		}
		if (StringUtils.isBlank(vo.getBileteralTaxDetails().getPermitNo())) {
			throw new BadRequestException("Please provide permit number");
		}
		if (StringUtils.isBlank(vo.getBileteralTaxDetails().getPermitIssuedBy())) {
			throw new BadRequestException("Please select permit issued state");
		}
		if (vo.getBileteralTaxDetails().getPermitValidFrom() == null) {
			throw new BadRequestException("Please provide permit valid from");
		}
		if (vo.getBileteralTaxDetails().getPermitValidTo() == null) {
			throw new BadRequestException("Please provide permit valid to");
		}
		if (vo.getBileteralTaxDetails().getFcValidFrom() == null) {
			throw new BadRequestException("Please provide fc valid from");
		}
		if (vo.getBileteralTaxDetails().getFcValidTo() == null) {
			throw new BadRequestException("Please provide fc valid to");
		}
		if (StringUtils.isBlank(vo.getBileteralTaxDetails().getRecommendationLetterNo())) {
			throw new BadRequestException("Please provide recommendation letter number");
		}
		if (vo.getBileteralTaxDetails().getRecommendationLatterDate() == null) {
			throw new BadRequestException("Please provide recommendation letter issued date");
		}
		if (StringUtils.isBlank(vo.getBileteralTaxDetails().getRlIssuedBy())) {
			throw new BadRequestException("Please provide recommendation letter issued person name");
		}
		if (StringUtils.isBlank(vo.getBileteralTaxDetails().getOwnerAddress())) {
			throw new BadRequestException("Please provide owner address");
		}
		if (StringUtils.isBlank(vo.getBileteralTaxDetails().getChassisNumber())) {
			throw new BadRequestException("Please provide vehicle chassis number");
		}
		if (StringUtils.isBlank(vo.getBileteralTaxDetails().getDistrict())) {
			throw new BadRequestException("Please provide district");
		}
		dto.setPrNo(vo.getBileteralTaxDetails().getPrNo());
		dto.setServiceIds(vo.getServiceIds());
		List<ServiceEnum> serviceIds = vo.getServiceIds().stream().map(id -> ServiceEnum.getServiceEnumById(id))
				.collect(Collectors.toList());
		dto.setServiceType(serviceIds);
		dto.setBileteralTaxDetails(bileteralTaxMapper.convertVO(vo.getBileteralTaxDetails()));
		if (vo.getBileteralTaxDetails().getPurpose().equalsIgnoreCase(PurposeEnum.TRANSFER.getCode())) {//
			if (StringUtils.isBlank(vo.getBileteralTaxDetails().getNewOwner())) {
				throw new BadRequestException("Please provide new owner details");
			}
			if (StringUtils.isBlank(vo.getBileteralTaxDetails().getNewOwnerAddress())) {
				throw new BadRequestException("Please provide new owner address");
			}
			Optional<BileteralTaxDTO> optionalBilateralDetails = bileteralTaxDAO
					.findByPrNoAndStatusIsTrue(dto.getPrNo());
			if (!optionalBilateralDetails.isPresent()) {
				logger.error("Bilater detalis not found for: ", dto.getPrNo());
				throw new BadRequestException("Bilater detalis not found for: " + dto.getPrNo());
			}
			BileteralTaxDTO bilateralDto = optionalBilateralDetails.get();
			bilateralDto.setPurpose(vo.getBileteralTaxDetails().getPurpose());
			bilateralDto.setOldOwner(bilateralDto.getOwnerName());
			bilateralDto.setOldOwnerAddress(bilateralDto.getOwnerAddress());
			bilateralDto.setOwnerName(vo.getBileteralTaxDetails().getNewOwner());
			bilateralDto.setOwnerAddress(vo.getBileteralTaxDetails().getNewOwnerAddress());
			bilateralDto.setDistrict(vo.getBileteralTaxDetails().getDistrict());
			dto.setBileteralTaxDetails(bilateralDto);
		}

		return dto;
	}

	@Override
	public void validationForRlNonadCount(String prNo, String purpose, String recommendationLetterNo,
			String permitIssuedBy) {
		List<RegServiceDTO> list = regServiceDAO.findByPrNo(prNo.toUpperCase());
		if (list != null && !list.isEmpty()) {
			for (RegServiceDTO dto : list) {
				if (dto.getCreatedDate() == null) {
					dto.setCreatedDate(LocalDateTime.now());
				}
			}
			list.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));

			RegServiceDTO serviceDto = list.stream().findFirst().get();
			if (!serviceDto.getApplicationStatus().equals(StatusRegistration.APPROVED)) {
				logger.error("Application is at pending state.Please proced in application status.",
						" Application number is: " + serviceDto.getApplicationNo());
				throw new BadRequestException("Application is at pending state.Please proced in application status."
						+ " Application number is: " + serviceDto.getApplicationNo());
			}
			if (purpose.equalsIgnoreCase(PurposeEnum.FRESH.getCode())) {
				if (list.stream().anyMatch(dto -> dto.getBileteralTaxDetails().getRecommendationLetterNo()
						.equalsIgnoreCase(recommendationLetterNo))) {
					logger.error("Entered Recommendation letter number is used: ", recommendationLetterNo);
					throw new BadRequestException(
							"Entered Recommendation letter number is used: " + recommendationLetterNo);
				}
			}
			Optional<BileteralTaxDTO> optionalBilateralDetails = bileteralTaxDAO
					.findByApplicationNo(serviceDto.getApplicationNo());
			if (!optionalBilateralDetails.isPresent()) {
				logger.error("Bilater detalis not found for: ", serviceDto.getApplicationNo());
				throw new BadRequestException("Bilater detalis not found for: " + serviceDto.getApplicationNo());
			}
			BileteralTaxDTO bilateralDto = optionalBilateralDetails.get();
			if (purpose.equalsIgnoreCase("FRESH")) {
				if (bilateralDto.getValidityTo().isAfter(LocalDate.now())) {
					if (bilateralDto.getPermitIssuedBy().equalsIgnoreCase(permitIssuedBy)
							|| bilateralDto.getRecommendationLetterNo().equalsIgnoreCase(recommendationLetterNo)) {
						logger.warn("Validity is uptodate for prNo :[ {}]", prNo);
						throw new BadRequestException("validity upto date: " + prNo.toUpperCase());
					}

				}
			}
			if (purpose.equalsIgnoreCase(PurposeEnum.RENEWAL.getCode())) {
				if (bilateralDto.getValidityTo().isAfter(LocalDate.now())) {
					logger.warn("Validity is upto date for PRNo :[{}]", prNo);
					throw new BadRequestException("validity upto date: " + prNo.toUpperCase());
				}
			}
			if (purpose.equalsIgnoreCase(PurposeEnum.TRANSFER.getCode())
					|| purpose.equalsIgnoreCase(PurposeEnum.VEHICLEREPLACE.getCode())) {
				if (bilateralDto.getValidityTo().isBefore(LocalDate.now())) {
					logger.error("Validity completed. Please select fresh or renewal for: ", prNo.toUpperCase());
					throw new BadRequestException(
							"Validity completed. Please select fresh or renewal for: " + prNo.toUpperCase());
				}
			}

		} else {
			if (!purpose.equalsIgnoreCase(PurposeEnum.FRESH.getCode())) {
				logger.error("No records on this vehicle number. Please select fresh for: ", prNo.toUpperCase());
				throw new BadRequestException(
						"No records on this vehicle number. Please select fresh for: " + prNo.toUpperCase());
			}
		}

	}

	private boolean isApprovedCCOandMVI(RegServiceDTO regDTO) {
		if (regDTO.getActionDetails() != null && regDTO.getActionDetails().size() > 0) {
			Optional<ActionDetails> actionDetailsOpt = regDTO.getActionDetails().stream()
					.filter(p -> RoleEnum.CCO.getName().equals(p.getRole())).findFirst();
			if (actionDetailsOpt.isPresent() && actionDetailsOpt.get().getIsDoneProcess()) {
				logger.info("Response from isApprovedCCOandMVI [{}]", true);
				return Boolean.TRUE;
			}
		}
		logger.info("Response from isApprovedCCOandMVI [{}]", false);
		return Boolean.FALSE;
	}

	// Validating representative having the permissions to perform actions.
	private RepresentativeDTO validateRepresentative(String aadhaarNo) {

		List<ApplicantDetailsDTO> applicantList = applicantDetailsDAO
				.findByAadharResponseUidAndUidTokenNotNull(Long.parseLong(aadhaarNo));
		if (CollectionUtils.isEmpty(applicantList)) {
			logger.error("Un Authorized User with aadhaarNo [{}]", aadhaarNo);
			throw new BadRequestException("Unauthorized user");
		}
		applicantList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));

		RepresentativeDTO represDto = representativeDAO.findOne(applicantList.get(0).getAadharResponse().getUidToken());
		if (represDto == null || represDto.getParentId() == null) {
			logger.error("Validating the Representative with Aadhaar No[{}} failed ", aadhaarNo);
			throw new BadRequestException("Missing authorized user");
		}
		represDto = representativeDAO.findOne(represDto.getParentApplicantId());
		represDto.setChildToken(applicantList.get(0).getAadharResponse().getUidToken());
		return represDto;

	}

	// creating parent if its not available in DB
	private RepresentativeVO createParent(AadharDetailsResponseVO aadharDetailsResponseVO) {
		List<RegistrationDetailsDTO> regDetailsList = registrationDetailDAO
				.findByApplicantDetailsAadharNoAndApplicantDetailsIsAadhaarValidatedTrue(
						aadharDetailsResponseVO.getUid().toString());
		if (CollectionUtils.isEmpty(regDetailsList)) {
			logger.error("Vehicles not found with aadhaar No", aadharDetailsResponseVO.getUid());
			throw new BadRequestException(
					"Vehicles not found with this aadhaarNo : " + aadharDetailsResponseVO.getUid());
		}
		RepresentativeDTO represDto = representativeDAO.findOne(aadharDetailsResponseVO.getUidToken());
		RepresentativeVO vo = null;
		if (represDto != null) {
			represDto.setlUpdate(LocalDateTime.now());
			represDto.setApplicationIds(
					regDetailsList.stream().map(x -> x.getApplicationNo()).collect(Collectors.toList()));
			vo = representativeMapper.convertEntity(represDto);
			if (CollectionUtils.isNotEmpty(represDto.getChilduidTokens())) {
				vo.setChildApplicantDetails(getApplicantDetails(represDto.getChilduidTokens()));
			}
			representativeDAO.save(represDto);

			return vo;
		}
		represDto = new RepresentativeDTO();
		represDto
				.setApplicationIds(regDetailsList.stream().map(x -> x.getApplicationNo()).collect(Collectors.toList()));
		List<ApplicantDetailsDTO> applicantList = applicantDetailsDAO
				.findByAadharResponseUidAndUidTokenNotNull(aadharDetailsResponseVO.getUid());
		if (CollectionUtils.isNotEmpty(applicantList)) {
			applicantList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		}
		represDto.setApplicantId(applicantList.stream().findFirst().get().getApplicantNo());
		represDto.setUidToken(aadharDetailsResponseVO.getUidToken());
		represDto.setUserType(RoleEnum.CITIZEN);
		represDto.setCreatedDate(LocalDateTime.now());
		represDto.setCreatedDateStr(LocalDateTime.now().toString());
		represDto.setCreatedBy(RoleEnum.CITIZEN.getName());
		representativeDAO.save(represDto);
		vo = representativeMapper.convertEntity(represDto);
		return vo;
	}

	// method to active
	@Override
	public List<ApplicantDetailsVO> activeRepresentative(AadhaarDetailsRequestVO requestModel) {
		try {
			if (requestModel.getRepresentativeVo() == null
					|| StringUtils.isEmpty(requestModel.getRepresentativeVo().getParentUidToken())) {
				logger.error("Owner details not available to create representative");
				throw new BadRequestException("Owner details not available to create representative");
			}

			RepresentativeDTO represDto = representativeDAO
					.findOne(requestModel.getRepresentativeVo().getParentUidToken());
			if (represDto == null) {
				logger.error("Owner details not available to create representative");
				throw new BadRequestException("Owner details not available to create representative");
			}
			RepresentativeDTO childDetails = representativeDAO
					.findOne(requestModel.getRepresentativeVo().getChildUidToken());
			if (childDetails != null && childDetails.getParentId() != null) {
				logger.error("Representative is already available for other owner");
				throw new BadRequestException("Representative is already available for other owner");

			} else if (childDetails != null && CollectionUtils.isNotEmpty(represDto.getChilduidTokens())
					&& represDto.getChilduidTokens().contains(requestModel.getRepresentativeVo().getChildUidToken())) {
				logger.warn("Representative is already available for this owner");
				throw new BadRequestException("Representative is already available for this owner");
			} else if (childDetails == null) {
				childDetails = new RepresentativeDTO();
				childDetails.setCreatedDate(LocalDateTime.now());
				childDetails.setCreatedDateStr(LocalDateTime.now().toString());
			}
			List<ApplicantDetailsDTO> applicantList = applicantDetailsDAO.findByAadharResponseUidAndUidTokenNotNull(
					Long.parseLong(requestModel.getRepresentativeVo().getChildAadhaarNo()));
			if (CollectionUtils.isNotEmpty(applicantList)) {
				applicantList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
			}

			childDetails.setApplicantId(applicantList.stream().findFirst().get().getApplicantNo());
			if (StringUtils.isEmpty(requestModel.getRepresentativeVo().getChildUidToken())) {
				logger.error("token is not available to generate representative");
				throw new BadRequestException("token is not available to generate representative");
			}
			if (requestModel.getRepresentativeVo().getChildUidToken()
					.equals(requestModel.getRepresentativeVo().getParentUidToken())) {
				logger.error("Representative is only allowed otherthan owner");
				throw new BadRequestException("Representative is only allowed otherthan owner");
			}
			childDetails.setUidToken(requestModel.getRepresentativeVo().getChildUidToken());
			childDetails.setUserType(RoleEnum.CITIZEN);
			childDetails.setParentId(represDto.getApplicantId());
			childDetails.setParentApplicantId(represDto.getUidToken());

			List<String> childTokens = represDto.getChilduidTokens();
			if (CollectionUtils.isEmpty(childTokens)) {
				childTokens = new ArrayList<>();
			}
			childTokens.add(requestModel.getRepresentativeVo().getChildUidToken());
			represDto.setChilduidTokens(childTokens);
			represDto.setlUpdate(LocalDateTime.now());
			List<RepresentativeDTO> saverepresentative = new ArrayList<>();
			saverepresentative.add(represDto);
			saverepresentative.add(childDetails);
			representativeDAO.save(saverepresentative);
			return getApplicantDetails(childTokens);
		} catch (Exception e) {
			logger.error("Exception Occured with aadhaarNo:[{}]", requestModel.getUid_num());
			throw new BadRequestException(e.getMessage());
		}

	}

	// Method to Inactive Representative
	@Override
	public List<ApplicantDetailsVO> InactiveRepresentative(String parentUidToken, String childAadharNo) {

		try {
			if (StringUtils.isEmpty(parentUidToken) || StringUtils.isEmpty(childAadharNo)) {
				logger.error("Invalid inputs");
				throw new BadRequestException("Invalid inputs");
			}
			RepresentativeDTO represDto = representativeDAO.findOne(parentUidToken);
			if (represDto == null) {
				logger.error("Owner details not available to Inactive representative");
				throw new BadRequestException("Owner details not available to Inactive representative");
			}
			/*
			 * Long userSessionTime = getTimeDifferance(represDto.getUserTokenTime());
			 * if(userSessionTime>1){ throw new
			 * BadRequestException("Session is expired,so please login again"); }
			 */
			List<ApplicantDetailsDTO> applicantList = applicantDetailsDAO
					.findByAadharResponseUidAndUidTokenNotNull(Long.parseLong(childAadharNo));
			if (CollectionUtils.isEmpty(applicantList)) {
				logger.error("Representative is not available to Inactive");
				throw new BadRequestException("Representative is not available to Inactive");
			}
			applicantList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
			String token = applicantList.stream().findFirst().get().getAadharResponse().getUidToken();
			if (represDto.getChilduidTokens().contains(token)) {
				represDto.getChilduidTokens().removeIf(c -> c.equals(token));
				RepresentativeDTO childDto = representativeDAO.findOne(token);
				RepresentativeLogDTO log = new RepresentativeLogDTO();
				log.setIsActive(Boolean.FALSE);
				log.setRepresentativeDTO(childDto);
				log.setCreatedDate(LocalDateTime.now());
				log.setCreatedDateStr(LocalDateTime.now().toString());
				if (CollectionUtils.isEmpty(childDto.getApplicationIds())) {
					representativeDAO.delete(childDto);
				} else {
					childDto.setParentApplicantId(null);
					childDto.setParentId(null);
				}
				representativeDAO.save(represDto);
				representativeLogDAO.save(log);
			} else {
				logger.error("Representative is not available to Inactive");
				throw new BadRequestException("Representative is not available to Inactive");
			}
			return getApplicantDetails(represDto.getChilduidTokens());
		} catch (Exception e) {
			logger.info("Exception raised while inactive record : {}", e.getMessage());
			throw new BadRequestException(e.getMessage());
		}

	}

	@Override
	public List<ApplicantDetailsVO> modifyRepresentative(String parentUidToken, String childUidToken,
			AadhaarDetailsRequestVO requestModel) {
		if (StringUtils.isEmpty(parentUidToken)) {
			logger.error("Invalid inputs");
			throw new BadRequestException("Invalid inputs");
		}
		RepresentativeDTO represDto = representativeDAO.findOne(parentUidToken);
		if (represDto == null) {
			logger.error("Owner Details not available,so please contact support team");
			throw new BadRequestException("Owner Details not available,so please contact support team");
		}
		if (!represDto.getChilduidTokens().contains(childUidToken)) {
			logger.error("Invalid representative");
			throw new BadRequestException("Invalid representative");
		}
		List<ApplicantDetailsDTO> applicantList = applicantDetailsDAO
				.findByAadharResponseUidAndUidTokenNotNull(Long.parseLong(requestModel.getUid_num()));
		if (CollectionUtils.isEmpty(applicantList)) {
			logger.error("representative details not available");
			throw new BadRequestException("representative details not available");
		}
		AadhaarDetailsResponseDTO aadhaarDTO = applicantList.get(0).getAadharResponse();
		if (!aadhaarDTO.getUidToken().equals(childUidToken)) {
			logger.error("Invalid representative");
			throw new BadRequestException("Invalid representative");
		}
		/* Optional<AadharDetailsResponseVO> aadhaarResponse = */ getAadhaarResponseService(requestModel);

		return getApplicantDetails(represDto.getChilduidTokens());
	}

	/*
	 * private Long getTimeDifferance(LocalDateTime tokenTime){ LocalDateTime
	 * tempDateTime = LocalDateTime.from(tokenTime); return
	 * tempDateTime.until(LocalDateTime.now(), ChronoUnit.MINUTES); }
	 */

	private List<ApplicantDetailsVO> getApplicantDetails(List<String> childUidTokens) {

		return applicantDeatilsMapper
				.convertSpecificRepresentiveFields(applicantDetailsDAO.findByUidTokenIn(childUidTokens));

	}

	@Override
	public boolean isPermitActiveOrNot(String prNo, String vehicleType) {

		if (vehicleType.equalsIgnoreCase(CovCategory.T.getCode())) {
			List<PermitDetailsDTO> listOfPermits = permitDetailsDAO.findByPrNoAndPermitStatus(prNo,
					PermitsEnum.ACTIVE.getDescription());
			if (!listOfPermits.isEmpty()) {
				PermitDetailsDTO listOfPermsasits = listOfPermits.stream().filter(type -> type.getPermitType()
						.getTypeofPermit().equalsIgnoreCase(PermitsEnum.PermitType.PRIMARY.getPermitTypeCode()))
						.findAny().get();
				if (listOfPermsasits != null) {

					return true;

				}
			}
		}
		return false;

	}

	@Override
	public Optional<AadharDetailsResponseVO> getRepresentativeDetails(String aadhaarNumber) {

		if (StringUtils.isBlank(aadhaarNumber)) {
			logger.error("Session Expired");
			throw new BadRequestException("Session Expired");
		}
		/*
		 * Optional<AadhaarDetailsResponseDTO> aadhaarDTO=
		 * aadhaarResponseDAO.findByUidAndUidTokenNotNull(Long.parseLong(
		 * aadhaarNumber));
		 */

		List<ApplicantDetailsDTO> applicantList = applicantDetailsDAO
				.findByAadharResponseUidAndUidTokenNotNull(Long.parseLong(aadhaarNumber));
		if (CollectionUtils.isEmpty(applicantList)) {
			logger.error("Owner Details not found");
			throw new BadRequestException("Owner Details not found");
		}
		applicantList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		/*
		 * if(!aadhaarDTO.isPresent()){ throw new
		 * BadRequestException("Owner Details not found"); }
		 */
		AadharDetailsResponseVO aadhaarDetails = aadhaarDetailsResponseMapper
				.convertEntity(applicantList.get(0).getAadharResponse());
		aadhaarDetails.setRepresentativeVO(
				createParent(aadhaarDetailsResponseMapper.convertEntity(applicantList.get(0).getAadharResponse())));
		return Optional.of(aadhaarDetails);
	}

	@Override
	public String updateRegServiceDetails(String applicationNo) {
		String status = "No action performed";
		Optional<RegServiceDTO> regOptional = this.findByApplicationNo(applicationNo);
		if (!regOptional.isPresent()) {
			logger.error("No app found from regservices with appNo:[{}]", applicationNo);
			throw new BadRequestException("No records found for application number: " + applicationNo);
		}
		RegServiceDTO dto = regOptional.get();
		if (dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.OTHERSTATIONFC.getId()))
				&& dto.getApplicationStatus().equals(StatusRegistration.MVIREJECTED)
				&& dto.isAllowFcForOtherStation()) {
			registratrionServicesApprovals.updateMviOfficeDetails(dto);
			dto.setApplicationStatus(StatusRegistration.REJECTED);
			regServiceDAO.save(dto);
			status = "Office changed to home MVI office.Please modifySlot and produce vehicle at MVI office.";
		}
		return status;

	}

	@Override
	public RegistrationDetailsVO applicationSearchForFeeCorrection(String prNo, String chassisno, Boolean isMobile) {

		Optional<RegServiceDTO> regService = regServiceDAO
				.findByPrNoAndServiceTypeInAndApplicationStatusOrderByCreatedDateDesc(prNo,
						Arrays.asList(ServiceEnum.DATAENTRY), StatusRegistration.APPROVED.getDescription());
		if (regService.isPresent() && regService.get().getOtherStateNOCStatus() != null
				&& !StringUtils.isEmpty(regService.get().getOtherStateNOCStatus().getDescription())) {
			if (regService.get().getOtherStateNOCStatus().toString()
					.equalsIgnoreCase(StatusRegistration.NOCVERIFICATIONPENDING.getDescription().toString())) {
				logger.error("No record found for prNo [{}]", prNo);
				throw new BadRequestException("Other state NOC VERIFICATION PENDING for this  prNo: " + prNo);
			}
		}

		Optional<RegistrationDetailsDTO> regDetails = registrationDetailDAO.findByPrNo(prNo);
		if (!regDetails.isPresent()) {

			logger.error("No record found for prNo..[{}]", prNo);
			throw new BadRequestException("No record found for prNo: " + prNo);
		}
		RegistrationDetailsDTO dto = regDetails.get();

		if (!dto.getApplicantDetails().getIsAadhaarValidated() || dto.getApplicantDetails().getAadharNo() == null) {
			logger.error("please select aadhaar seeding service to seed your aadhaar no");
			throw new BadRequestException("Please select aadhar seeding service to Seed your aadhar number");
		}

		if (isMobile.equals(Boolean.FALSE)) {
			if (StringUtils.isBlank(chassisno)) {
				logger.error("chassis no  is blank");
				throw new BadRequestException("Please provide correct chassis numbers ");
			}
			if (!dto.getVahanDetails().getChassisNumber().equalsIgnoreCase(chassisno)) {
				logger.error("Invalid ChassisNo:[{}]", chassisno);
				throw new BadRequestException("Please enter correct chassis numbers ");
			}
		}
		Optional<FeeCorrectionDTO> feecorrectionDto = feeCorrectionDAO
				.findByChassisNoAndStatusIsTrue(dto.getVahanDetails().getChassisNumber());
		if (!feecorrectionDto.isPresent()) {
			logger.error("No record found for Fee correction..[{}]", prNo);
			throw new BadRequestException("No record found for Fee correction : " + prNo);
		}
		if (!feecorrectionDto.get().isApproved()) {
			logger.error("approval pending. Current application at ",
					feecorrectionDto.get().getCurrentRoles().stream().findFirst().get() + " role");
			throw new BadRequestException("Approvel pending. Current application at "
					+ feecorrectionDto.get().getCurrentRoles().stream().findFirst().get() + " role");
		}

		// validations like vehicle suspend or not .
		List<String> errors = new ArrayList<>();
		this.checkVcrDues(dto, errors);
		if (!errors.isEmpty()) {
			logger.error("[{}]", errors.get(0));
			throw new BadRequestException(errors.get(0));
		}
		this.isVehicleStopped(dto, errors);
		if (errors != null && !errors.isEmpty()) {
			logger.error(errors.stream().findFirst().get());
			throw new BadRequestException(errors.stream().findFirst().get());
		}
		List<RegServiceDTO> listOfRegService = regServiceDAO
				.findByRegistrationDetailsApplicationNoAndServiceIdsAndSourceIsNull(dto.getApplicationNo(),
						ServiceEnum.FEECORRECTION.getId());
		PermitDetailsDTO permitDTO = new PermitDetailsDTO();
		if (!listOfRegService.isEmpty()) {
			listOfRegService.sort((s1, s2) -> s2.getCreatedDate().compareTo(s1.getCreatedDate()));
			RegServiceDTO regDto = listOfRegService.stream().findFirst().get();
			if (regDto.getServiceType().contains(ServiceEnum.NEWSTAGECARRIAGEPERMIT)) {
				permitDTO = regDto.getPdtl();
			}
			List<StatusRegistration> listOfStatus = new ArrayList<>();
			listOfStatus.add(StatusRegistration.CITIZENPAYMENTFAILED);
			listOfStatus.add(StatusRegistration.PAYMENTPENDING);
			if (listOfStatus.contains(regDto.getApplicationStatus())
					&& !regDto.getServiceType().contains(ServiceEnum.FEECORRECTION)
					&& dto.getClassOfVehicle() != null) {
				logger.error("application is in progress , Application number is[{}]", regDto.getApplicationNo());
				throw new BadRequestException("Application is in progress. Application No: " + regDto.getApplicationNo()
						+ ". Please verify the status of the application in registration search");
			}

		}

		MasterCovDTO masterCovDTO = masterCovDAO.findByCovdescription(dto.getClassOfVehicleDesc().trim().toUpperCase());
		dto.setClassOfVehicle(masterCovDTO.getCovcode());
		dto.getApplicantDetails().setAadharResponse(null);
		RegistrationDetailsVO regVo = registrationDetailsMapper.convertEntity(dto);
		if (regVo.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.ARVT.getCovCode())) {
			if (regVo.getVahanDetails() != null && regVo.getVahanDetails().getTrailerChassisDetailsVO() != null) {
				Integer gtw = regVo.getVahanDetails().getTrailerChassisDetailsVO().stream().findFirst().get().getGtw();
				for (TrailerChassisDetailsVO trailerDetails : regVo.getVahanDetails().getTrailerChassisDetailsVO()) {
					if (trailerDetails.getGtw() > gtw) {
						gtw = trailerDetails.getGtw();
					}
				}
				Integer rlw = regVo.getVahanDetails().getGvw() + gtw;
				regVo.getVahanDetails().setGvw(rlw);
			}
		}
		// RcValidationVO rcValidationVO = new RcValidationVO();
		// SearchVo vo = new SearchVo();
		// rcValidationVO.setServiceIds(Stream.of(ServiceEnum.FEECORRECTION.getId()).collect(Collectors.toSet()));
		// rcValidationVO.setPrNo(regVo.getPrNo());
		// getSearchResult(rcValidationVO, dto, vo);
		if (dto.getTaxvalidity() != null) {
			regVo.setTaxvalidity(dto.getTaxvalidity());
		}
		if (!ObjectUtils.isEmpty(permitDTO)) {
			regVo.setPermitDetailsVO(permitDetailsMapper.convertEntity(permitDTO));
		}
		return regVo;
	}

	private void validationForFeeCorrection(RegServiceVO input,
			KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {

		RegistrationDetailsDTO regDetails = citizenObjects.getValue();
		Optional<FeeCorrectionDTO> feeCorrection = feeCorrectionDAO
				.findByChassisNoAndStatusIsTrue(regDetails.getVahanDetails().getChassisNumber());
		if (!feeCorrection.isPresent() || !feeCorrection.get().isApproved()) {
			logger.error("Fee approval is pending for PrNo: [{}]", regDetails.getPrNo());
			throw new BadRequestException("Fee approvel pending : " + regDetails.getPrNo());
		}
	}

	@Override
	public Long isAllowedForOSTrFancy(LocalDate trValidity) {

		Long range = ChronoUnit.DAYS.between(LocalDate.now(), trValidity.plusDays(30));
		if (range >= 0 && range <= 30) {
			return range;
		} else {
			logger.error("please select ordinary No");
			/* throw new BadRequestException("please select ordinary number"); */
			range = 100L;
			return range;
		}
	}

	@Override
	public CitizenSearchReportVO applicationSearchForVcr(ApplicationSearchVO applicationSearchVO) {

		List<VcrFinalServiceDTO> vcrList = null;
		CitizenSearchReportVO outPut = new CitizenSearchReportVO();
		vcrList = getTotalVcrs(applicationSearchVO, vcrList);
		if (vcrList == null || vcrList.isEmpty()) {
			throw new BadRequestException("No vcr details found");
		}
		vcrList = this.getVcrDetails(Arrays.asList(vcrList.stream().findFirst().get().getVcr().getVcrNumber()),
				applicationSearchVO.isRequestFromAO(), false);
		getVcrAmount(applicationSearchVO, vcrList, outPut);
		VcrFinalServiceDTO dto = vcrList.stream().findFirst().get();
		boolean allowForCash = this.shouldNotAllowForPayCash(dto);
		if (outPut != null && outPut.getVcrList() != null && !outPut.getVcrList().isEmpty()) {
			outPut.getVcrList().stream().forEach(id -> {
				this.setTaxForPrint(id, dto.getTaxAmountForPrint());
				id.setShouldNotAllowForPayCash(allowForCash);
			});
		}

		return outPut;
	}

	@Override
	public List<VcrFinalServiceDTO> getTotalVcrs(ApplicationSearchVO applicationSearchVO,
			List<VcrFinalServiceDTO> vcrList) {
		if (StringUtils.isBlank(applicationSearchVO.getPrNo()) && StringUtils.isBlank(applicationSearchVO.getTrNo())
				&& StringUtils.isBlank(applicationSearchVO.getChassisNo())
				&& StringUtils.isBlank(applicationSearchVO.getVcrNo())) {
			logger.error("Please provide required data like prNo/trNo/chassisno/vcrno ");
			throw new BadRequestException("Please provide required data like prNo/trNo/chassisno/vcrno ");
		}
		if (applicationSearchVO.getPrNo() != null && StringUtils.isNoneBlank(applicationSearchVO.getPrNo())) {
			vcrList = vcrFinalServiceDAO.findByRegistrationRegNoAndIsVcrClosedIsFalse(applicationSearchVO.getPrNo());
		}

		if (applicationSearchVO.getTrNo() != null && StringUtils.isNoneBlank(applicationSearchVO.getTrNo())) {
			vcrList = vcrFinalServiceDAO.findByRegistrationTrNoAndIsVcrClosedIsFalse(applicationSearchVO.getTrNo());
		}

		if (applicationSearchVO.getChassisNo() != null && StringUtils.isNoneBlank(applicationSearchVO.getChassisNo())) {
			vcrList = vcrFinalServiceDAO
					.findByRegistrationChassisNumberAndIsVcrClosedIsFalse(applicationSearchVO.getChassisNo());
		}
		if (applicationSearchVO.getVcrNo() != null && StringUtils.isNoneBlank(applicationSearchVO.getVcrNo())) {
			vcrList = vcrFinalServiceDAO
					.findByVcrVcrNumberIgnoreCaseAndIsVcrClosedIsFalse(applicationSearchVO.getVcrNo());
		}
		if (vcrList != null && !vcrList.isEmpty()) {
			return vcrList.stream()
					.filter(paymentDone -> paymentDone.getPaymentType() == null
							|| !paymentDone.getPaymentType().equalsIgnoreCase(GatewayTypeEnum.CASH.getDescription()))
					.collect(Collectors.toList());
		}

		return vcrList;
	}

	@Override
	public CitizenSearchReportVO applicationSearchForVcrAfterPayment(ApplicationSearchVO applicationSearchVO) {

		List<VcrFinalServiceDTO> vcrList = null;
		CitizenSearchReportVO outPut = new CitizenSearchReportVO();
		vcrList = getTotalVcrsAfterPayments(applicationSearchVO, vcrList);
		if (vcrList == null || vcrList.isEmpty()) {
			throw new BadRequestException("No vcr details found");
		}
		VcrFinalServiceDTO dto = vcrList.stream().findFirst().get();
		vcrList = this.getVcrDetailsAfterPayment(Arrays.asList(dto.getVcr().getVcrNumber()));
		List<VcrFinalServiceVO> vo = calculateTaxAndTotal(vcrList);

		outPut.setVcrList(vo);
		return outPut;
	}

	@Override
	public List<VcrFinalServiceVO> calculateTaxAndTotal(List<VcrFinalServiceDTO> vcrList) {

		List<VcrFinalServiceVO> vo = vcrFinalServiceMapper.convertEntity(vcrList);

		if (vo != null && !vo.isEmpty()) {
			vo.stream().forEach(id -> {
				VcrFinalServiceDTO dto = vcrList.stream().filter(
						paymentDone -> paymentDone.getVcr().getVcrNumber().equalsIgnoreCase(id.getVcr().getVcrNumber()))
						.collect(Collectors.toList()).get(0);
				boolean allowForCash = this.shouldNotAllowForPayCash(dto);
				this.setTaxForPrint(id, dto.getTaxAmountForPrint());
				id.setShouldNotAllowForPayCash(allowForCash);
			});
		}

		vo.forEach(one -> {

			if (one.getIsVcrClosed() != null) {
				if ((!one.getIsVcrClosed() && one.getPaymentType() != null
						&& one.getPaymentType().equalsIgnoreCase(GatewayTypeEnum.CASH.getDescription()))
						|| one.getIsVcrClosed()
						|| (!one.getIsVcrClosed() && one.getPartiallyClosed() != null && one.getPartiallyClosed())) {
					this.setTaxForPrint(one);
					one.setIsVcrClosed(Boolean.TRUE);
				}
			}
		});
		return vo;
	}

	@Override
	public List<VcrFinalServiceDTO> getTotalVcrsAfterPayments(ApplicationSearchVO applicationSearchVO,
			List<VcrFinalServiceDTO> vcrList) {
		if (StringUtils.isBlank(applicationSearchVO.getPrNo()) && StringUtils.isBlank(applicationSearchVO.getTrNo())
				&& StringUtils.isBlank(applicationSearchVO.getChassisNo())
				&& StringUtils.isBlank(applicationSearchVO.getVcrNo())) {
			logger.error("Please provide required data like prNo/trNo/chassisno/vcrno ");
			throw new BadRequestException("Please provide required data like prNo/trNo/chassisno/vcrno ");
		}
		if (applicationSearchVO.getPrNo() != null && StringUtils.isNoneBlank(applicationSearchVO.getPrNo())) {
			vcrList = vcrFinalServiceDAO.findByRegistrationRegNo(applicationSearchVO.getPrNo());
		}

		if (applicationSearchVO.getTrNo() != null && StringUtils.isNoneBlank(applicationSearchVO.getTrNo())) {
			vcrList = vcrFinalServiceDAO.findByRegistrationTrNo(applicationSearchVO.getTrNo());
		}

		if (applicationSearchVO.getChassisNo() != null && StringUtils.isNoneBlank(applicationSearchVO.getChassisNo())) {
			vcrList = vcrFinalServiceDAO.findByRegistrationChassisNumber(applicationSearchVO.getChassisNo());
		}
		if (applicationSearchVO.getVcrNo() != null && StringUtils.isNoneBlank(applicationSearchVO.getVcrNo())) {
			vcrList = vcrFinalServiceDAO.findByVcrVcrNumberIgnoreCase(applicationSearchVO.getVcrNo());
		}

		return vcrList;
	}

	@Override
	public void getVcrAmount(ApplicationSearchVO applicationSearchVO, List<VcrFinalServiceDTO> vcrList,
			CitizenSearchReportVO outPut) {

		this.setAmount(vcrList, outPut, applicationSearchVO.isRequestFromAO());
	}

	@Override
	public RegServiceVO saveVcrDetails(String regServiceVO, MultipartFile[] uploadfiles, JwtUser jwtUser) {

		if (StringUtils.isBlank(regServiceVO)) {
			throw new BadRequestException("Input Details are required.");
		}
		Optional<RegServiceVO> inputOptional = readValue(regServiceVO, RegServiceVO.class);
		if (!inputOptional.isPresent()) {
			logger.warn("Error in VO ", regServiceVO);
			throw new BadRequestException("Invalid Input Details.");
		}
		synchronized (inputOptional.get().getVcrNosList().get(0).intern()) {
			try {
				RegServiceVO inputRegServiceVO = inputOptional.get();

				if (inputRegServiceVO.getServiceIds() == null || inputRegServiceVO.getServiceIds().isEmpty()) {
					throw new BadRequestException("Please provide service type.");
				}
				if (!inputRegServiceVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VCR.getId()))) {
					throw new BadRequestException("Invalid Service Type.");
				}
				RegServiceDTO dto = this.converVcrDetails(inputRegServiceVO);
				List<Integer> serviceIds = new ArrayList<>();
				serviceIds.add(ServiceEnum.VCR.getId());

				List<ServiceEnum> services = new ArrayList<ServiceEnum>();
				services.add(ServiceEnum.VCR);
				List<RegServiceDTO> listOfResServiceDetails = null;
				if (StringUtils.isNoneBlank(inputRegServiceVO.getPrNo())) {
					listOfResServiceDetails = regServiceDAO.findByPrNoAndServiceTypeIn(inputRegServiceVO.getPrNo(),
							services);

				} else if (StringUtils.isNoneBlank(inputRegServiceVO.getTrNo())) {
					listOfResServiceDetails = regServiceDAO.findByTrNoAndServiceTypeIn(inputRegServiceVO.getTrNo(),
							services);
				} else {
					listOfResServiceDetails = regServiceDAO
							.findByRegistrationDetailsVahanDetailsChassisNumberAndServiceIdsInAndSourceIsNull(
									dto.getRegistrationDetails().getVahanDetails().getChassisNumber(), serviceIds);
				}
				if (listOfResServiceDetails != null && !listOfResServiceDetails.isEmpty()) {
					listOfResServiceDetails.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
					RegServiceDTO pendingDoc = listOfResServiceDetails.stream().findFirst().get();
					if (!(pendingDoc.getApplicationStatus().equals(StatusRegistration.APPROVED)
							|| pendingDoc.getApplicationStatus().equals(StatusRegistration.CANCELED))) {
						throw new BadRequestException(
								"Application is in Pending state.Application No: " + pendingDoc.getApplicationNo());
					}
				}

				dto.setlUpdate(LocalDateTime.now());
				if (jwtUser != null && StringUtils.isNoneBlank(jwtUser.getUsername())) {
					dto.setCreatedBy(jwtUser.getUsername());
				}
				// dto.setOfficeDetails(optionalOffice.get());
				// dto.setOfficeCode(optionalOffice.get().getOfficeCode());
				Map<String, String> officeCodeMap = new HashMap<>();
				officeCodeMap.put("officeCode", "VCR");
				dto.setApplicationNo(sequenceGenerator
						.getSequence(String.valueOf(Sequence.REGISTRATIONAPPNO.getSequenceId()), officeCodeMap));
				if (inputRegServiceVO.getGatewayType().equals(GatewayTypeEnum.CASH.getDescription())) {
					dto.setGatewayType(inputRegServiceVO.getGatewayType());
				}
				saveCitizenServiceDoc(inputRegServiceVO, dto, uploadfiles);
				return regServiceMapper.convertEntity(dto);
			} catch (Exception e) {
				logger.error("Exception [{}] in vcr save :[{}]", e);
				throw new BadRequestException(e.getMessage());
			}
		}
	}

	private RegServiceDTO converVcrDetails(RegServiceVO vo) {
		RegServiceDTO dto = new RegServiceDTO();
		// VcrTaxDetailsDTO vcr = new VcrTaxDetailsDTO();

		List<VcrFinalServiceDTO> vcrList = getVcrDetails(vo.getVcrNosList(), false, vo.getSpecificVcrPayment());
		List<String> vcrNoList = new ArrayList<>();
		vcrList.stream().forEach(id -> {
			String vcrNo = id.getVcr().getVcrNumber();
			vcrNoList.add(vcrNo);
		});

		dto.setVcrNosList(vcrNoList);
		// dto.setPrNo(vo.getBileteralTaxDetails().getPrNo());
		dto.setServiceIds(vo.getServiceIds());
		List<ServiceEnum> serviceIds = vo.getServiceIds().stream().map(id -> ServiceEnum.getServiceEnumById(id))
				.collect(Collectors.toList());
		dto.setServiceType(serviceIds);
		if(vo.getSpecificVcrPayment()!=null)
		{
			dto.setSpecificVcrPayment(vo.getSpecificVcrPayment());
		}
		this.getregDoc(vcrList, dto);
		// dto.setVcrDetails(vcr);

		return dto;
	}

	@Override
	public List<VcrFinalServiceDTO> getVcrDetails(List<String> listOfVcrs, boolean requestFromAO,
			boolean applicationSearchfromMVI) {
		List<VcrFinalServiceDTO> allVcrDtosList = new ArrayList<>();
		List<VcrFinalServiceDTO> vcrList = vcrFinalServiceDAO.findByVcrVcrNumberInAndIsVcrClosedIsFalse(listOfVcrs);
		if (vcrList == null || vcrList.isEmpty()) {
			logger.error("No record found for vcr..[{}]");
			throw new BadRequestException("No record found for vcr : ");
		}
		
		VcrFinalServiceDTO vacrDto = vcrList.stream().findFirst().get();
		if(!applicationSearchfromMVI) {
		if (StringUtils.isNoneBlank(vacrDto.getRegistration().getRegApplicationNo())) {
			allVcrDtosList = vcrFinalServiceDAO.findByRegistrationRegApplicationNoAndIsVcrClosedIsFalse(
					vacrDto.getRegistration().getRegApplicationNo());

		} else {
			if (StringUtils.isBlank(vacrDto.getRegistration().getChassisNumber())) {
				logger.error("Chassis number not found for vcr..[{}]" + vacrDto.getVcr().getVcrNumber());
				throw new BadRequestException(
						"Chassis number not found for vcr..[{}]" + vacrDto.getVcr().getVcrNumber());
			}
			allVcrDtosList = vcrFinalServiceDAO
					.findByRegistrationChassisNumberAndIsVcrClosedIsFalse(vacrDto.getRegistration().getChassisNumber());
		}
		}else {

			String upperCase = listOfVcrs.stream().findFirst().get().toUpperCase();
			String[] vcrnum = upperCase.split("/");
			String year = vcrnum[1].substring(3);
			String month = String.valueOf(Character.toUpperCase(vcrnum[1].charAt(0)))
					+ String.valueOf(Character.toLowerCase(vcrnum[1].charAt(1)))
					+ String.valueOf(Character.toLowerCase(vcrnum[1].charAt(2))).concat(year);
			StringBuilder stringBuilder = new StringBuilder();
			StringBuilder vcrNumber = stringBuilder.append(vcrnum[0]).append("/").append(month).append("/")
					.append(vcrnum[2]);
			allVcrDtosList = vcrFinalServiceDAO.findByVcrVcrNumberIgnoreCaseAndIsVcrClosedIsFalse(vcrNumber.toString());
		
		}
		List<VcrFinalServiceDTO> vcrWithOutCahs = allVcrDtosList.stream()
				.filter(paymentDone -> paymentDone.getPaymentType() == null
						|| !paymentDone.getPaymentType().equalsIgnoreCase(GatewayTypeEnum.CASH.getDescription()))
				.collect(Collectors.toList());
		if (!requestFromAO && vcrWithOutCahs != null && !vcrWithOutCahs.isEmpty()) {
			VcrFinalServiceDTO vcrDto = rtaService.checkVehicleTrNotGenerated(vcrWithOutCahs);
			if (vcrDto != null && vcrDto.getPartiallyClosed() != null && vcrDto.getPartiallyClosed()) {
				logger.error("Please produce tr copy at RTO office for unregistered vehicles check report..[{}]"
						+ vacrDto.getVcr().getVcrNumber());
				throw new BadRequestException(
						"Please produce tr copy at RTO office for unregistered vehicles check report..[{}]"
								+ vacrDto.getVcr().getVcrNumber());
			}
		}
		return vcrWithOutCahs;
	}

	@Override
	public List<VcrFinalServiceDTO> getVcrDetailsAfterPayment(List<String> listOfVcrs) {
		List<VcrFinalServiceDTO> vcrList = vcrFinalServiceDAO.findByVcrVcrNumberIn(listOfVcrs);
		if (vcrList == null || vcrList.isEmpty()) {
			logger.error("No record found for vcr..[{}]");
			throw new BadRequestException("No record found for vcr : ");
		}
		VcrFinalServiceDTO vacrDto = vcrList.stream().findFirst().get();
		if (StringUtils.isNoneBlank(vacrDto.getRegistration().getRegApplicationNo())) {
			return vcrFinalServiceDAO
					.findByRegistrationRegApplicationNo(vacrDto.getRegistration().getRegApplicationNo());
		}
		if (StringUtils.isBlank(vacrDto.getRegistration().getChassisNumber())) {
			logger.error("Chassis number not found for vcr..[{}]" + vacrDto.getVcr().getVcrNumber());
			throw new BadRequestException("Chassis number not found for vcr..[{}]" + vacrDto.getVcr().getVcrNumber());
		}
		return vcrFinalServiceDAO.findByRegistrationChassisNumber(vacrDto.getRegistration().getChassisNumber());
	}

	private void getregDoc(List<VcrFinalServiceDTO> listOfVcrs, RegServiceDTO regService) {
		boolean isOtherSate = Boolean.FALSE;
		boolean isUnregistered = Boolean.FALSE;
		VcrFinalServiceDTO singleVcr = listOfVcrs.stream().findFirst().get();
		for (VcrFinalServiceDTO vcrDto : listOfVcrs) {
			if (vcrDto.getRegistration().isOtherState()) {
				isOtherSate = Boolean.TRUE;
			}
			if (vcrDto.getRegistration().isUnregisteredVehicle()) {
				isUnregistered = Boolean.TRUE;
			}

		}
		List<StatusRegistration> listOfStatus = new ArrayList<>();
		listOfStatus.add(StatusRegistration.PAYMENTPENDING);
		listOfStatus.add(StatusRegistration.CITIZENPAYMENTFAILED);
		if (StringUtils.isNoneBlank(singleVcr.getRegistration().getRegApplicationNo())) {
			List<RegServiceDTO> listOfRegService = regServiceDAO.findByRegistrationDetailsApplicationNoAndServiceIds(
					singleVcr.getRegistration().getRegApplicationNo(), ServiceEnum.VCR.getId());
			if (listOfRegService != null && !listOfRegService.isEmpty()) {
				listOfRegService.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
				RegServiceDTO serviceDto = listOfRegService.stream().findFirst().get();
				if (listOfStatus.stream().anyMatch(status -> status.equals(serviceDto.getApplicationStatus()))) {
					logger.error(
							"No record found in reg for chassisNo :" + singleVcr.getRegistration().getChassisNumber());
					throw new BadRequestException("You have already applied for " + serviceDto.getServiceType()
							+ " and is in progress Application No: " + serviceDto.getApplicationNo()
							+ ". Please verify the status at registration search / application search");
				}
			}
			if (isOtherSate) {

				Optional<RegistrationDetailsDTO> optionalReg = registrationDetailDAO
						.findByApplicationNo(singleVcr.getRegistration().getRegApplicationNo());
				if (optionalReg.isPresent()) {
					regService.setRegistrationDetails(optionalReg.get());
					regService.setPrNo(optionalReg.get().getPrNo());
					regService.setOfficeCode(optionalReg.get().getOfficeDetails().getOfficeCode());
				} else {
					Optional<RegServiceDTO> optionalRegService = regServiceDAO
							.findByApplicationNo(singleVcr.getRegistration().getRegApplicationNo());
					if (optionalRegService.isPresent()) {
						regService.setRegistrationDetails(optionalRegService.get().getRegistrationDetails());
						regService.setPrNo(optionalRegService.get().getPrNo());
						regService.setOfficeCode(
								optionalRegService.get().getRegistrationDetails().getOfficeDetails().getOfficeCode());
					} else {
						logger.error("No record found in reg for chassisNo :"
								+ singleVcr.getRegistration().getChassisNumber());
						throw new BadRequestException("No record found in reg for chassisNo :"
								+ singleVcr.getRegistration().getChassisNumber());
					}
				}

			} else {
				if (isUnregistered) {

					Optional<RegistrationDetailsDTO> optionalReg = registrationDetailDAO
							.findByApplicationNo(singleVcr.getRegistration().getRegApplicationNo());
					if (optionalReg.isPresent()) {
						regService.setRegistrationDetails(optionalReg.get());
						regService.setPrNo(optionalReg.get().getPrNo());
						regService.setOfficeCode(optionalReg.get().getOfficeDetails().getOfficeCode());
					} else {
						Optional<StagingRegistrationDetailsDTO> optionalStatging = stagingRegistrationDetailsDAO
								.findByApplicationNo(singleVcr.getRegistration().getRegApplicationNo());
						if (optionalStatging.isPresent()) {
							regService.setRegistrationDetails(optionalStatging.get());
							regService.setPrNo(optionalStatging.get().getPrNo());
							regService.setOfficeCode(optionalStatging.get().getOfficeDetails().getOfficeCode());
						} else {
							logger.error("No document found for :" + singleVcr.getRegistration().getRegApplicationNo());
							throw new BadRequestException(
									"No document found for :" + singleVcr.getRegistration().getRegApplicationNo());
						}
					}

				} else {
					regService.setRegistrationDetails(getRegDoc(singleVcr));
					regService.setPrNo(regService.getRegistrationDetails().getPrNo());
					regService.setOfficeCode(regService.getRegistrationDetails().getOfficeDetails().getOfficeCode());
				}
			}
		} else {
			List<RegServiceDTO> listOfRegService = null;
			if (StringUtils.isNoneBlank(singleVcr.getRegistration().getRegNo())) {
				listOfRegService = regServiceDAO.findByRegistrationDetailsPrNoAndServiceIdsNotIn(
						singleVcr.getRegistration().getRegNo(), ServiceEnum.VCR.getId());
			} else {
				listOfRegService = regServiceDAO
						.findByRegistrationDetailsVahanDetailsChassisNumberAndServiceIdsInAndSourceIsNull(
								singleVcr.getRegistration().getChassisNumber(), Arrays.asList(ServiceEnum.VCR.getId()));
			}

			if (listOfRegService != null && !listOfRegService.isEmpty()) {
				listOfRegService.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
				RegServiceDTO serviceDto = listOfRegService.stream().findFirst().get();
				if (listOfStatus.stream().anyMatch(status -> status.equals(serviceDto.getApplicationStatus()))) {
					logger.error(
							"No record found in reg for chassisNo :" + singleVcr.getRegistration().getChassisNumber());
					throw new BadRequestException("You have already applied for " + serviceDto.getServiceType()
							+ " and is in progress Application No: " + serviceDto.getApplicationNo()
							+ ". Please verify the status at registration search / application search");
				}
			}
			RegistrationDetailsDTO regDto = new RegistrationDetailsDTO();
			ApplicantDetailsDTO applicantDetails = new ApplicantDetailsDTO();
			regDto.setClassOfVehicle(singleVcr.getRegistration().getClasssOfVehicle().getCovcode());
			regDto.setPrNo(singleVcr.getRegistration().getRegNo());
			regDto.setTrNo(singleVcr.getRegistration().getTrNo());
			VahanDetailsDTO vahanDetails = new VahanDetailsDTO();
			vahanDetails.setGvw(singleVcr.getRegistration().getGvwc());
			vahanDetails.setUnladenWeight(singleVcr.getRegistration().getUlw());
			vahanDetails.setSeatingCapacity(String.valueOf(singleVcr.getRegistration().getSeatingCapacity()));
			vahanDetails.setChassisNumber(singleVcr.getRegistration().getChassisNumber());
			applicantDetails.setFirstName(singleVcr.getOwnerDetails().getFullName());
			regDto.setApplicantDetails(applicantDetails);
			regDto.setVahanDetails(vahanDetails);
			regService.setPrNo(singleVcr.getRegistration().getRegNo());
			regService.setTrNo(singleVcr.getRegistration().getTrNo());
			regService.setRegistrationDetails(regDto);
			listOfVcrs.sort((p1, p2) -> p1.getVcr().getDateOfCheck().compareTo(p2.getVcr().getDateOfCheck()));
			regService.setOfficeCode(listOfVcrs.stream().findFirst().get().getMviOfficeCode());
		}
	}

	private RegistrationDetailsDTO getRegDoc(VcrFinalServiceDTO singleVcr) {
		Optional<RegistrationDetailsDTO> regList = registrationDetailDAO
				.findByApplicationNo(singleVcr.getRegistration().getRegApplicationNo());
		if (regList == null || !regList.isPresent()) {
			logger.error("No record found in reg for chassisNo :" + singleVcr.getRegistration().getChassisNumber());
			throw new BadRequestException(
					"No record found in reg for chassisNo :" + singleVcr.getRegistration().getChassisNumber());
		}

		return regList.get();
	}

	@Override
	public boolean skipAadharValidationForTax() {
		Optional<PropertiesDTO> optionalPropertiesForSkipAadhar = propertiesDAO.findBySkipAadharForTaxTrue();
		return optionalPropertiesForSkipAadhar.isPresent();
	}

	private void checkvalidationRenewalOfPermitValidity(PermitDetailsDTO permitDetailsDTO, RegServiceVO regServiceVO) {
		Long transportValidDays = 0l;
		if (regServiceVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RENEWALOFPERMIT.getId()))) {
			transportValidDays = ChronoUnit.DAYS.between(LocalDate.now().minusDays(1),
					permitDetailsDTO.getPermitValidityDetails().getPermitValidTo().plusDays(1));
			if (transportValidDays >= 30) {
				throw new BadRequestException(
						"You are not eligible to apply for Renewal Of Permit, Permit validity not expired");
			}
		}
	}

	@Override
	public void clearTokenRequest() {
		LocalDateTime time = LocalDateTime.now();
		List<TokenAuthenticationDTO> list = tokenAuthenticationDAO.findByExpirationDateLessThanEqual(time);
		if (CollectionUtils.isNotEmpty(list)) {
			tokenAuthenticationDAO.delete(list);
		}
	}

	private void setAmount(List<VcrFinalServiceDTO> vcrList, CitizenSearchReportVO outPut, boolean requestFromAO) {

		Pair<List<VcrFinalServiceDTO>, Integer> amount = this.getAmount(vcrList, requestFromAO);
		outPut.setVcrList(vcrFinalServiceMapper.convertEntity(amount.getFirst()));
		outPut.setTotal(amount.getSecond());
	}

	@Override
	public Pair<List<VcrFinalServiceDTO>, Integer> getAmount(List<VcrFinalServiceDTO> vcrList, boolean requestFromAO) {
		Integer totalOffence = 0;
		for (VcrFinalServiceDTO vcdDoc : vcrList) {
			if (vcdDoc.getSeizedAndDocumentImpounded() != null
					&& vcdDoc.getSeizedAndDocumentImpounded().getVehicleSeizedDTO() != null
					&& vcdDoc.getSeizedAndDocumentImpounded() != null
					&& StringUtils
							.isNotEmpty(vcdDoc.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().getVehicleKeptAt())
					&& !vcdDoc.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().isReleaseOrder()
					&& !requestFromAO) {
				// if (vcrList.size() == 1) {
				logger.error(
						"It is decided to initiate court /Departmental action against the violations committed. Please contact concern  RTA  :"
								+ vcdDoc.getRegistration().getChassisNumber());
				throw new BadRequestException(
						"It is decided to initiate court /Departmental action against the violations committed. Please contact concern  RTA   :"
								+ vcdDoc.getRegistration().getChassisNumber());
				// }
				// continue;
			}
			if (vcdDoc.getPartiallyClosed() != null && vcdDoc.getPartiallyClosed()) {
				if (vcrList.size() == 1) {
					if (!requestFromAO) {
						logger.error(
								"drunk and drive offence is pending please take court order and close at AO level  :"
										+ vcdDoc.getRegistration().getChassisNumber());
						throw new BadRequestException(
								"drunk and drive offence is pending please take court order and close at AO level  :"
										+ vcdDoc.getRegistration().getChassisNumber());
					}
				}
				continue;
			}

			Integer offenceTotal = 0;
			for (OffenceDTO offence : vcdDoc.getOffence().getOffence()) {
				if (offence.isShouldNotClose()) {
					if (vcrList.size() == 1 && vcdDoc.getOffence().getOffence().size() == 1
							&& vcdDoc.getPartiallyClosed() != null && vcdDoc.getPartiallyClosed()) {
						if (!requestFromAO) {
							logger.error(
									"drunk and drive offence is pending please take court order and close at AO level  :"
											+ vcdDoc.getRegistration().getChassisNumber());
							throw new BadRequestException(
									"drunk and drive offence is pending please take court order and close at AO level  :"
											+ vcdDoc.getRegistration().getChassisNumber());
						}
					}
					continue;
				}
				if (offence.getOtherOffence() != null && offence.getOtherOffence()
						&& (offence.getAmount1() == null || offence.getAmount1() <= 0)) {
					if (!requestFromAO) {
						logger.error("MVI selected other offence. Please visit RTO office for finalize offence amount :"
								+ vcdDoc.getRegistration().getChassisNumber());
						throw new BadRequestException(
								"MVI selected other offence. Please visit RTO office for finalize offence amount :"
										+ vcdDoc.getRegistration().getChassisNumber());
					}
				}
				List<VcrFinalServiceDTO> allVcrDtosList = new ArrayList<>();
				allVcrDtosList = vcrFinalServiceDAO.findByRegistrationChassisNumberAndOffenceOffenceOffenceDescription(
						vcrList.stream().findFirst().get().getRegistration().getChassisNumber(),
						offence.getOffenceDescription());
				for (VcrFinalServiceDTO vcr : vcrList) {
					if (vcr.getSaveDoc() != null && vcr.getSaveDoc()) {
						allVcrDtosList.add(vcr);
					}
				}
				List<VcrFinalServiceDTO> vcrClosedList = allVcrDtosList.stream()
						.filter(closed -> closed.getIsVcrClosed()).collect(Collectors.toList());
				if (vcrClosedList == null || vcrClosedList.isEmpty()) {
					vcrClosedList = allVcrDtosList.stream()
							.filter(paymentDone -> paymentDone.getPaymentType() != null && paymentDone.getPaymentType()
									.equalsIgnoreCase(GatewayTypeEnum.CASH.getDescription()))
							.collect(Collectors.toList());
				}
				if (vcrClosedList != null && !vcrClosedList.isEmpty()) {
					offence.setFixedAmount(getOffenceAmount(offence, "TWO", vcdDoc));
					// offence.setFixedAmount(offence.getAmount2());
					totalOffence = totalOffence + offence.getFixedAmount();
					offenceTotal = offenceTotal + offence.getFixedAmount();
				} else {
					if (allVcrDtosList != null && !allVcrDtosList.isEmpty() && allVcrDtosList.size() > 1) {

						List<OffenceDTO> listOfOffences = new ArrayList<>();
						;
						for (VcrFinalServiceDTO offences : vcrList) {
							for (OffenceDTO singleOffence : offences.getOffence().getOffence()) {
								if (singleOffence.getOffenceDescription()
										.equalsIgnoreCase(offence.getOffenceDescription())) {
									listOfOffences.add(singleOffence);
								}
							}
						}
						if (listOfOffences.stream()
								.anyMatch(id -> id.getOffencePaid() != null && id.getOffencePaid())) {
							offence.setFixedAmount(getOffenceAmount(offence, "TWO", vcdDoc));
							// offence.setFixedAmount(offence.getAmount2());
							totalOffence = totalOffence + offence.getFixedAmount();
							offenceTotal = offenceTotal + offence.getFixedAmount();
						} else {
							offence.setOffencePaid(Boolean.TRUE);
							offence.setFixedAmount(getOffenceAmount(offence, "ONE", vcdDoc));
							// offence.setFixedAmount(offence.getAmount1());
							totalOffence = totalOffence + offence.getFixedAmount();
							offenceTotal = offenceTotal + offence.getFixedAmount();
						}

					} else {
						offence.setFixedAmount(getOffenceAmount(offence, "ONE", vcdDoc));
						// offence.setFixedAmount(offence.getAmount1());
						// offence.setOffencePaid(Boolean.TRUE);
						totalOffence = totalOffence + offence.getFixedAmount();
						offenceTotal = offenceTotal + offence.getFixedAmount();
					}
				}

				vcdDoc.setOffencetotal(offenceTotal);
			}
		}
		return Pair.of(vcrList, totalOffence);
	}

	private Integer getOffenceAmount(OffenceDTO offence, String amountType, VcrFinalServiceDTO vcrNo) {
		if (offence.getIsOffenceClosed() != null && offence.getIsOffenceClosed() && offence.getFixedAmount() != null) {
			return offence.getFixedAmount();
		}

		if (amountType.equalsIgnoreCase("ONE")) {

			if (offence.isBasedOnPersons()) {
				return basedOnPersons(offence, vcrNo.getVcr().getVcrNumber());
			} else if (offence.isBasedOnHrs()) {
				return basedOnHrs(offence.getNoOfHrs(), offence.getAmount1(), offence.getOffenceDescription(),
						vcrNo.getVcr().getVcrNumber());
			} else if (offence.isBasedOnWeight()) {
				return basedOnWeight(offence, offence.getAmount1(), offence.getOffenceDescription(), vcrNo);
			} else if (StringUtils.isNoneBlank(vcrNo.getPilledCov())
					&& offence.getOffenceDescription().equalsIgnoreCase("Over loading Passengers")) {
				return overLoadPersons(offence.getAmount1(), offence.getOffenceDescription(), vcrNo);
			} else if (offence.isBasedOnAnimals()) {
				return basedOnAnimals(offence, offence.getAmount1(), offence.getOffenceDescription(), vcrNo);
			}

			return offence.getAmount1();
		} else {
			if (offence.isBasedOnPersons()) {
				return basedOnPersons(offence, vcrNo.getVcr().getVcrNumber());
			} else if (offence.isBasedOnHrs()) {
				return basedOnHrs(offence.getNoOfHrs(), offence.getAmount2(), offence.getOffenceDescription(),
						vcrNo.getVcr().getVcrNumber());
			} else if (offence.isBasedOnWeight()) {
				return basedOnWeight(offence, offence.getAmount2(), offence.getOffenceDescription(), vcrNo);
			} else if (StringUtils.isNoneBlank(vcrNo.getPilledCov())
					&& offence.getOffenceDescription().equalsIgnoreCase("Over loading Passengers")) {
				return overLoadPersons(offence.getAmount2(), offence.getOffenceDescription(), vcrNo);
			} else if (offence.isBasedOnAnimals()) {
				return basedOnAnimals(offence, offence.getAmount2(), offence.getOffenceDescription(), vcrNo);
			}
			return offence.getAmount2();
		}

	}

	private Integer basedOnPersons(OffenceDTO offence, String vcrNo) {
		if (offence.getNoOfPersons() == null || offence.getNoOfPersons() <= 0) {
			logger.error("Number of persons missed for offence :" + offence.getOffenceDescription()
					+ " and vcr number is: " + vcrNo);
			throw new BadRequestException("Number of persons missed for offence :" + offence.getOffenceDescription()
					+ " and vcr number is: " + vcrNo);
		}
		// return (offenceAmount * noOfPersons)+offenceAmount;
		// if(offence.getPerPerson() == null || offence.getPerPerson()<=0) {
		if (offence.getPerPerson() == null) {
			logger.error("per person amount missed for offence :" + offence.getOffenceDescription()
					+ " and vcr number is: " + vcrNo);
			throw new BadRequestException("per person amount missed for offence :" + offence.getOffenceDescription()
					+ " and vcr number is: " + vcrNo);
		}
		return (offence.getNoOfPersons() * offence.getPerPerson());
	}

	private Integer basedOnHrs(Integer hrs, Integer offenceAmount, String offenceDescription, String vcrNo) {
		if (hrs == null || hrs <= 0) {
			logger.error("Number of hrs missed for offence :" + offenceDescription + " and vcr number is: " + vcrNo);
			throw new BadRequestException(
					"Number of hrs missed for offence :" + offenceDescription + " and vcr number is: " + vcrNo);
		}
		return (offenceAmount * hrs);
	}

	private Integer basedOnWeight(OffenceDTO offence, Integer offenceAmount, String offenceDescription,
			VcrFinalServiceDTO vcrNo) {
		if (offence.getMviEnteredweight() == null || offence.getMviEnteredweight() <= 0) {
			logger.error("Weight missed for offence :" + offenceDescription + " and vcr number is: "
					+ vcrNo.getVcr().getVcrNumber());
			throw new BadRequestException("Weight missed for offence :" + offenceDescription + " and vcr number is: "
					+ vcrNo.getVcr().getVcrNumber());
		}
		if (vcrNo.getRegistration().getGvwc() > offence.getMviEnteredweight()) {
			logger.error("Overload weight should be greater than RC weight for offence :" + offenceDescription
					+ " and vcr number is: " + vcrNo.getVcr().getVcrNumber());
			throw new BadRequestException("Overload weight should be greater than RC weight for offence :"
					+ offenceDescription + " and vcr number is: " + vcrNo.getVcr().getVcrNumber());
		}
		if (StringUtils.isBlank(offence.getPerkg())) {
			logger.error("per kg amount missing for offence :" + offenceDescription + " and vcr number is: "
					+ vcrNo.getVcr().getVcrNumber());
			throw new BadRequestException("per kg amount missing for offence :" + offenceDescription
					+ " and vcr number is: " + vcrNo.getVcr().getVcrNumber());
		}
		Double totalWeight = offence.getMviEnteredweight().doubleValue()
				- vcrNo.getRegistration().getGvwc().doubleValue();
		Double weight = totalWeight / 100d;
		Double result = ((Math.ceil(weight) * (Double.parseDouble(offence.getPerkg()))) + offenceAmount);
		return result.intValue();
		// int perKg = (int) (Double.parseDouble(offence.getPerkg()));
		// return ((offence.getMviEnteredweight() - vcrNo.getRegistration().getGvwc()) *
		// perKg) + offenceAmount;
	}

	private Integer overLoadPersons(Integer offenceAmount, String offenceDescription, VcrFinalServiceDTO vcrNo) {
		if (vcrNo.getPilledSeatings() != null) {
			if (vcrNo.getRegistration().getSeatingCapacity() == null
					|| vcrNo.getRegistration().getSeatingCapacity() <= 0) {
				logger.error("RC seating capacity  missed for offence :" + offenceDescription + " and vcr number is: "
						+ vcrNo.getVcr().getVcrNumber());
				throw new BadRequestException("RC seating capacity missed for offence :" + offenceDescription
						+ " and vcr number is: " + vcrNo.getVcr().getVcrNumber());
			}
			if (vcrNo.getPilledSeatings() > vcrNo.getRegistration().getSeatingCapacity()) {
				offenceAmount = ((vcrNo.getPilledSeatings() - vcrNo.getRegistration().getSeatingCapacity()) * 100)
						+ offenceAmount;
			}
		}
		return offenceAmount;
	}

	private Integer basedOnAnimals(OffenceDTO offence, Integer offenceAmount, String offenceDescription,
			VcrFinalServiceDTO vcrNo) {
		if (offence.getNoOfAnimals() == null || offence.getNoOfAnimals() <= 0) {
			logger.error("Number of animals missed for offence :" + offenceDescription + " and vcr number is: "
					+ vcrNo.getVcr().getVcrNumber());
			throw new BadRequestException("Number of animals missed for offence :" + offenceDescription
					+ " and vcr number is: " + vcrNo.getVcr().getVcrNumber());
		}

		return offence.getNoOfAnimals() * offenceAmount;
	}

	@Override
	public Optional<InsuranceResponseVO> getInsuranceDetails(String policyNumber) {

		final String uri = "";
		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
		UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(uri).queryParam("policyNumber", policyNumber);
		HttpEntity<?> entity = new HttpEntity<>(headers);
		HttpEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.GET, entity,
				String.class);
		if (response.hasBody()) {
			ObjectMapper mapper = new ObjectMapper();
			InsuranceResponseVO insuranceVO;
			try {
				insuranceVO = mapper.readValue(response.getBody(), InsuranceResponseVO.class);
				if ("SUCEESS".equalsIgnoreCase(insuranceVO.getStatus()))
					return Optional.of(insuranceVO);
			} catch (IOException e) {
				logger.error("Exception occured while fetching the details", e);
			}

		}
		return null;
	}

	@Override
	public String postPRDetails(InsuranceVO vo) {
		final String uri = "";
		ValidateingPostIngDetails(vo);
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<InsuranceVO> httpEntity = new HttpEntity<>(vo, headers);
		try {
			ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.POST, httpEntity, String.class);
			if (response.hasBody()) {
				ObjectMapper mapper = new ObjectMapper();
				InsuranceResponseVO insuranceVO = mapper.readValue(response.getBody(), InsuranceResponseVO.class);
				if ("SUCEESS".equalsIgnoreCase(insuranceVO.getStatus())
						&& "Successfully Updated".equalsIgnoreCase(insuranceVO.getMessage()))
					return insuranceVO.getMessage();
			}
		} catch (Exception e) {
			logger.info("While posting insurance detais services Exception occured [{}]", e);
		}

		return null;
	}

	private InsuranceVO ValidateingPostIngDetails(InsuranceVO vo) {
		if (StringUtils.isBlank(vo.getApplicantName())) {
			logger.error(" While posting insurance detais services Applicant Name is Missing :[{}]",
					vo.getApplicantName());
			throw new BadRequestException(
					"While posting insurance detais services Applicant Name is Missing :" + vo.getApplicantName());
		}
		if (StringUtils.isBlank(vo.getInsurenceNumber())) {
			logger.error(" While posting insurance detais services Insurence Number is Missing :[{}]",
					vo.getInsurenceNumber());
			throw new BadRequestException(
					"While posting insurance detais services Insurence Number is Missing :" + vo.getInsurenceNumber());
		}
		if (StringUtils.isBlank(vo.getEngineNumber())) {
			logger.error(" While posting insurance detais services Engine Number is Missing :[{}]",
					vo.getEngineNumber());
			throw new BadRequestException(
					"While posting insurance detais services Engine Number is Missing :" + vo.getEngineNumber());
		}
		if (StringUtils.isBlank(vo.getChassisNumber())) {
			logger.error(" While posting insurance detais services Chassis Number is Missing :[{}]",
					vo.getChassisNumber());
			throw new BadRequestException(
					"While posting insurance detais services Chassis Number is Missing :" + vo.getChassisNumber());
		}
		if (StringUtils.isBlank(vo.getPrNo())) {
			logger.error(" While posting insurance detais services prNo is Missing :[{}]", vo.getPrNo());
			throw new BadRequestException("While posting insurance detais services prNo is Missing :" + vo.getPrNo());
		}
		// if(StringUtils.isBlank(vo.getTrNo())) {
		// logger.error(" While posting insurance detais services tr number is Missing
		// :[{}]", vo.getTrNo());
		// throw new BadRequestException("While posting insurance detais services tr
		// Number is Missing :"+vo.getTrNo());
		// }

		return vo;

	}

	@Override
	public Boolean postPRDetailsInRegDetals(InsuranceVO vo) {
		ValidateingPostIngDetails(vo);
		Boolean verifyInsureanceDet = Boolean.FALSE;
		// todo need to add index to the db query
		Optional<RegistrationDetailsDTO> regDet = registrationDetailDAO
				.findByVahanDetailsChassisNumberAndVahanDetailsEngineNumberAndInsuranceDetailsCompanyAndInsuranceDetailsPolicyNumber(
						vo.getChassisNumber(), vo.getEngineNumber(), vo.getInsurenceCompany(), vo.getInsurenceNumber());
		if (regDet.isPresent() && regDet.get().getInsuranceDetails() != null) {
			verifyInsureanceDet = Boolean.TRUE;
			if (!vo.getPrNo().equalsIgnoreCase(regDet.get().getPrNo())) {
				verifyInsureanceDet = Boolean.FALSE;
				throw new BadRequestException(
						"While posting insurance detais services prNo is Invalid :" + vo.getPrNo());
			}
			if (!vo.getEngineNumber().equalsIgnoreCase(regDet.get().getVahanDetails().getEngineNumber())) {
				verifyInsureanceDet = Boolean.FALSE;
				throw new BadRequestException(
						"While posting insurance detais services Engine Number is Missing :" + vo.getEngineNumber());
			}
			if (!vo.getChassisNumber().equalsIgnoreCase(regDet.get().getVahanDetails().getChassisNumber())) {
				verifyInsureanceDet = Boolean.FALSE;
				throw new BadRequestException(
						"While posting insurance detais services Chassis Number is Invalid :" + vo.getChassisNumber());
			}
			if (!vo.getInsurenceNumber().equalsIgnoreCase(regDet.get().getInsuranceDetails().getPolicyNumber())) {
				verifyInsureanceDet = Boolean.FALSE;
				throw new BadRequestException("While posting insurance detais services Insurence Number is Invalid :"
						+ vo.getInsurenceNumber());
			}
			if (!vo.getTypeOfInsurence().equalsIgnoreCase(regDet.get().getInsuranceDetails().getPolicyType())) {
				verifyInsureanceDet = Boolean.FALSE;
			}
			if (!vo.getInsurenceCompany().equalsIgnoreCase(regDet.get().getInsuranceDetails().getCompany())) {
				verifyInsureanceDet = Boolean.FALSE;
			}

		}
		if (verifyInsureanceDet) {

			if (vo.getCancelledDate() != null && vo.getCancelledReason() != null) {
				RegistrationDetailsDTO regDetSave = regDet.get();
				if (regDetSave.getInsuranceDetails() != null
						&& regDetSave.getInsuranceDetails().getCancelledDate() != null) {
					throw new BadRequestException("insurance detais already updated  ");
				}
				regDetSave.getInsuranceDetails()
						.setCancelledDate(DateConverters.convertStirngTOlocalDate(vo.getCancelledDate()));
				regDetSave.getInsuranceDetails().setCancelledReason(vo.getCancelledReason());

				registrationDetailDAO.save(regDetSave);
				return Boolean.TRUE;
			}

		}
		return Boolean.FALSE;
	}

	@Override
	public Boolean confirmationToInsuranceCompany(InsuranceVO vo) {
		ValidateingPostIngDetails(vo);
		Boolean verifyInsureanceDet = Boolean.FALSE;
		Optional<RegistrationDetailsDTO> regDet = registrationDetailDAO
				.findByVahanDetailsChassisNumberAndVahanDetailsEngineNumberAndInsuranceDetailsCompanyAndInsuranceDetailsPolicyNumber(
						vo.getChassisNumber(), vo.getEngineNumber(), vo.getInsurenceCompany(), vo.getInsurenceNumber());
		if (regDet.isPresent() && regDet.get().getInsuranceDetails() != null) {
			verifyInsureanceDet = Boolean.TRUE;
			if (!vo.getPrNo().equalsIgnoreCase(regDet.get().getPrNo())) {
				verifyInsureanceDet = Boolean.FALSE;
				throw new BadRequestException(
						"While posting insurance detais services prNo is Invalid :" + vo.getPrNo());
			}
			if (!vo.getTrNo().equalsIgnoreCase(regDet.get().getTrNo())) {
				verifyInsureanceDet = Boolean.FALSE;
			}
			if (!vo.getEngineNumber().equalsIgnoreCase(regDet.get().getVahanDetails().getEngineNumber())) {
				verifyInsureanceDet = Boolean.FALSE;
				throw new BadRequestException(
						"While posting insurance detais services Engine Number is Invalid :" + vo.getEngineNumber());
			}
			if (!vo.getChassisNumber().equalsIgnoreCase(regDet.get().getVahanDetails().getChassisNumber())) {
				verifyInsureanceDet = Boolean.FALSE;
				throw new BadRequestException(
						"While posting insurance detais services Chassis Number is Invalid :" + vo.getChassisNumber());
			}
			if (!vo.getInsurenceNumber().equalsIgnoreCase(regDet.get().getInsuranceDetails().getPolicyNumber())) {
				verifyInsureanceDet = Boolean.FALSE;
				throw new BadRequestException("While posting insurance detais services Insurence Number is Invalid :"
						+ vo.getInsurenceNumber());
			}
			if (!vo.getTypeOfInsurence().equalsIgnoreCase(regDet.get().getInsuranceDetails().getPolicyType())) {
				verifyInsureanceDet = Boolean.FALSE;
			}
			if (!vo.getInsurenceCompany().equalsIgnoreCase(regDet.get().getInsuranceDetails().getCompany())) {
				verifyInsureanceDet = Boolean.FALSE;
			}

		}
		return verifyInsureanceDet ? Boolean.TRUE : Boolean.FALSE;
	}

	@Override
	public RegServiceVO saveOtherStateTPDetails(JwtUser jwtUser, String regServiceVO, MultipartFile[] uploadfiles,
			HttpServletRequest request, Boolean isRTAWebRequest) {
		List<RegServiceDTO> listOfResServiceDetails = null;

		if (StringUtils.isBlank(regServiceVO)) {
			throw new BadRequestException("Input Details are required.");
		}
		Optional<RegServiceVO> inputOptional = readValue(regServiceVO, RegServiceVO.class);
		if (!inputOptional.isPresent()) {
			logger.debug("Error in VO ", regServiceVO);
			saveInvalidInputDetails(regServiceVO, jwtUser);
			throw new BadRequestException("Invalid Input Details.");
		}

		synchronized (inputOptional.get().getPrNo()) {
			listOfResServiceDetails = regServiceDAO.findByPrNo(inputOptional.get().getPrNo());
			if (listOfResServiceDetails != null && !listOfResServiceDetails.isEmpty()) {
				listOfResServiceDetails.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
				RegServiceDTO regDto = listOfResServiceDetails.stream().findFirst().get();
				if (regDto.getCreatedDate().toLocalDate().equals(LocalDate.now())) {
					logger.debug("Application already inserted for vehicle number: " + inputOptional.get().getPrNo());
					throw new BadRequestException("Your application is already processed with the application number"
							+ regDto.getApplicationNo() + "https://aprtacitizen.epragathi.org/#!/regappstatus");
				}
			}
			try {
				RegServiceVO inputRegServiceVO = inputOptional.get();
				if (inputRegServiceVO.getServiceIds() == null || inputRegServiceVO.getServiceIds().isEmpty()) {
					throw new BadRequestException("Please provide service type.");
				}
				if (!inputRegServiceVO.getServiceIds().stream()
						.anyMatch(type -> type.equals(ServiceEnum.OTHERSTATETEMPORARYPERMIT.getId())
								|| type.equals(ServiceEnum.OTHERSTATESPECIALPERMIT.getId()))) {
					throw new BadRequestException("Invalid Service Type.");
				}
				permitValidationsService.validationForOtherStateSpecialPermit(inputRegServiceVO);
				RegServiceDTO dto = saveTemporaryPermitDetailsForOtherState(jwtUser, inputRegServiceVO);

				dto.setlUpdate(LocalDateTime.now());
				dto.setIpAddress(request.getRemoteAddr());

				Map<String, String> officeCodeMap = new HashMap<>();
				officeCodeMap.put("officeCode", dto.getOfficeCode());
				dto.setApplicationNo(sequenceGenerator
						.getSequence(String.valueOf(Sequence.REGISTRATIONAPPNO.getSequenceId()), officeCodeMap));

				saveCitizenServiceDoc(inputRegServiceVO, dto, uploadfiles);

				return regServiceMapper.convertEntity(dto);

			} catch (Exception e) {
				logger.error("Exception [{}] in vcr save :[{}]", e);
				throw new BadRequestException(e.getMessage());
			}
		}
	}

	private RegServiceDTO saveTemporaryPermitDetailsForOtherState(JwtUser jwtUser, RegServiceVO inputRegServiceVO) {
		Optional<OfficeDTO> officeDetails = null;

		RegServiceDTO dto = new RegServiceDTO();
		dto = regServiceMapper.convertVO(inputRegServiceVO);

		if (jwtUser != null) {
			dto.setCreatedBy(jwtUser.getId());
			officeDetails = officeDAO.findByOfficeCode(jwtUser.getOfficeCode());
		} else {
			officeDetails = officeDAO.findByOfficeCode("APSTA");
		}

		if (officeDetails != null) {
			dto.setOfficeDetails(officeDetails.get());
			dto.setOfficeCode(officeDetails.get().getOfficeCode());
		} else {
			dto.setOfficeCode("APSTA");
		}

		if (dto.getOfficeCode().equalsIgnoreCase("APSTA")
				&& GatewayTypeEnum.CASH.getDescription().equalsIgnoreCase(dto.getGatewayType())) {
			throw new BadRequestException("Invalid request Please try again");
		}

		dto.setServiceIds(inputRegServiceVO.getServiceIds());
		List<ServiceEnum> serviceIds = inputRegServiceVO.getServiceIds().stream()
				.map(id -> ServiceEnum.getServiceEnumById(id)).collect(Collectors.toList());
		dto.setServiceType(serviceIds);

		dto.setOtherStateTemporaryPermitDetails(
				otherStateTemporaryPermitDetailsMapper.convertVO(inputRegServiceVO.getOtherStateTemporaryPermit()));

		if (dto.getOtherStateTemporaryPermitDetails() != null) {
			RegistrationDetailsDTO regDocument = new RegistrationDetailsDTO();

			if (dto.getOtherStateTemporaryPermitDetails().getApplicantDetails() != null) {
				regDocument.setApplicantDetails(dto.getOtherStateTemporaryPermitDetails().getApplicantDetails());
			}

			if (dto.getOtherStateTemporaryPermitDetails().getVehicleDetails() != null) {
				regDocument.setVehicleDetails(dto.getOtherStateTemporaryPermitDetails().getVehicleDetails());
			}
			if (dto.getOtherStateTemporaryPermitDetails().getVehicleDetails() != null) {
				VahanDetailsDTO vahanDTO = new VahanDetailsDTO();
				vahanDTO.setEngineNumber(
						dto.getOtherStateTemporaryPermitDetails().getVehicleDetails().getEngineNumber());
				vahanDTO.setChassisNumber(
						dto.getOtherStateTemporaryPermitDetails().getVehicleDetails().getChassisNumber());
				vahanDTO.setGvw(dto.getOtherStateTemporaryPermitDetails().getVehicleDetails().getRlw());
				vahanDTO.setUnladenWeight(dto.getOtherStateTemporaryPermitDetails().getVehicleDetails().getUlw());
				vahanDTO.setSeatingCapacity(
						dto.getOtherStateTemporaryPermitDetails().getVehicleDetails().getSeatingCapacity());
				regDocument.setVahanDetails(vahanDTO);
				regDocument.setClassOfVehicle(
						dto.getOtherStateTemporaryPermitDetails().getVehicleDetails().getClassOfVehicle());
			}
			regDocument.setPrNo(dto.getPrNo());
			dto.setRegistrationDetails(regDocument);
		}

		dto.setStatus(StatusRegistration.APPROVED.getDescription());
		dto.setCreatedDate(LocalDateTime.now());
		dto.setCreatedDateStr(LocalDateTime.now().toString());
		return dto;
	}

	@Override
	public void checkonlineVcrDetailsForDataEntry(VcrInputVo vcrInputVo) {

		List<VcrFinalServiceDTO> vacrList = this.getOnlineVcrData(vcrInputVo);
		if (vacrList != null && !vacrList.isEmpty()) {
			throw new BadRequestException(
					"VCR details found. VCR number is: " + vacrList.stream().findFirst().get().getVcr().getVcrNumber());
		} else {
			if (StringUtils.isNoneBlank(vcrInputVo.getRegNo())) {
				FreezeVehiclsDTO dto = freezeVehiclsDAO.findByPrNoIn(vcrInputVo.getRegNo());
				if (dto != null) {
					throw new BadRequestException(
							"Sorry... You are not allowed for further services as your vehicle is caught by Motor Vehicle Inspector and vehicle challan report in under processing....");
				}
			}
		}

	}

	private List<VcrFinalServiceDTO> getOnlineVcrData(VcrInputVo vcrInputVo) {
		List<VcrFinalServiceDTO> vcrList = null;
		if (StringUtils.isNoneBlank(vcrInputVo.getApplicationNo())) {
			vcrList = vcrFinalServiceDAO
					.findByRegistrationRegApplicationNoAndIsVcrClosedIsFalse(vcrInputVo.getApplicationNo());
		} else if (StringUtils.isNoneBlank(vcrInputVo.getRegNo())) {
			vcrList = vcrFinalServiceDAO.findByRegistrationRegNoAndIsVcrClosedIsFalse(vcrInputVo.getRegNo());
		} else if (StringUtils.isNoneBlank(vcrInputVo.getTrNo())) {
			vcrList = vcrFinalServiceDAO.findByRegistrationTrNoAndIsVcrClosedIsFalse(vcrInputVo.getTrNo());
		} else if (StringUtils.isNoneBlank(vcrInputVo.getChassisNo())) {
			vcrList = vcrFinalServiceDAO
					.findByRegistrationChassisNumberAndIsVcrClosedIsFalse(vcrInputVo.getChassisNo());
		} else {
			logger.error("Please provide prno/trno/chassisno for vcr check");
			throw new BadRequestException("Please provide prno/trno/chassisno for vcr check");
		}
		if (vcrList != null && !vcrList.isEmpty()) {
			vcrList = this.getVcrDetails(Arrays.asList(vcrList.stream().findFirst().get().getVcr().getVcrNumber()),
					false, false);
		}
		return vcrList;
	}

	@Override
	public RegServiceVO saveVoluntaryTax(String voluntaryTaxVO, MultipartFile[] uploadfiles, UserDTO user,
			HttpServletRequest request, boolean requestFromApp) {

		if (StringUtils.isBlank(voluntaryTaxVO)) {
			throw new BadRequestException("Input Details are required.");
		}
		Optional<VoluntaryTaxVO> inputOptional = readValue(voluntaryTaxVO, VoluntaryTaxVO.class);
		if (!inputOptional.isPresent()) {
			logger.warn("Error in VO ", voluntaryTaxVO);
			throw new BadRequestException("Invalid Input Details.");
		}
		if (StringUtils.isBlank(inputOptional.get().getRegNo()) && StringUtils.isBlank(inputOptional.get().getTrNo())) {
			logger.warn("Please provide tr/pr number");
			throw new BadRequestException("Please provide tr/pr number");
		}
		String no = StringUtils.isNoneBlank(inputOptional.get().getRegNo()) ? inputOptional.get().getRegNo()
				: inputOptional.get().getTrNo();

		synchronized (no.intern()) {
			try {
				Set<Integer> serviceIds = new HashSet<>();
				serviceIds.add(ServiceEnum.VOLUNTARYTAX.getId());
				List<RegServiceDTO> listOfResServiceDetails = null;
				if (StringUtils.isNoneBlank(inputOptional.get().getRegNo())) {
					// no = inputOptional.get().getRegNo();
					listOfResServiceDetails = regServiceDAO.findByPrNo(inputOptional.get().getRegNo());

				} else {
					// no = inputOptional.get().getTrNo();
					listOfResServiceDetails = regServiceDAO
							.findByRegistrationDetailsTrNo(inputOptional.get().getTrNo());
				}
				Pair<Boolean, RegServiceDTO> pendingDetailsAndStatus = this
						.commonForPendinfApplicationStatus(serviceIds, null, listOfResServiceDetails);
				if (pendingDetailsAndStatus.getFirst()) {
					throw new BadRequestException("Application is in Pending state.Application No: "
							+ pendingDetailsAndStatus.getSecond().getApplicationNo());
				}
				if (listOfResServiceDetails != null && !listOfResServiceDetails.isEmpty()) {
					listOfResServiceDetails.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
					RegServiceDTO regDto = listOfResServiceDetails.stream().findFirst().get();
					if (regDto.getCreatedDate().toLocalDate().equals(LocalDate.now()) && regDto.getServiceIds() != null
							&& regDto.getServiceIds().contains(ServiceEnum.VOLUNTARYTAX.getId())) {
						logger.debug("Application already inserted for vehicle number: " + no);
						throw new BadRequestException("Application already inserted for vehicle number: " + no);
					}

					this.checkAitpTaxPaidORNot(regDto, inputOptional.get());
				}
				VoluntaryTaxVO vo = inputOptional.get();

				RegServiceDTO dto = this.convertVoluntaryTaxDetails(vo, request, user, requestFromApp);
				String officeCode = null;
				if (requestFromApp) {
					if (user == null) {
						logger.warn("User details not found ");
						throw new BadRequestException("User details not found ");
					}
					officeCode = user.getOffice().getOfficeCode();
					dto.setCreatedBy(user.getUserId());
				} else {
					// officeCode = user.getOffice().getOfficeCode();
					dto.setCreatedBy("CITIZEN");
					officeCode = "APSTA";
				}
				List<String> errors = new ArrayList<>();
				if (StringUtils.isNoneBlank(dto.getPrNo())) {
					checkVcrDues(dto.getRegistrationDetails(), errors);
				} else {
					List<VcrFinalServiceDTO> vcrList = vcrFinalServiceDAO
							.findByRegistrationTrNoAndIsVcrClosedIsFalse(dto.getRegistrationDetails().getTrNo());
					if (vcrList != null && !vcrList.isEmpty()) {
						errors.add("VCR details found with this vcr number"
								+ vcrList.stream().findFirst().get().getVcr().getVcrNumber());
					}
				}
				if (!errors.isEmpty()) {
					logger.error("[{}]", errors.get(0));
					throw new BadRequestException(errors.get(0));
				}
				dto.setServiceIds(serviceIds);
				dto.setServiceType(Arrays.asList(ServiceEnum.VOLUNTARYTAX));
				RegServiceVO inputRegServiceVO = regServiceMapper.convertEntity(dto);

				Optional<OfficeDTO> optionalOffice = officeDAO.findByOfficeCode(officeCode);
				if (!optionalOffice.isPresent()) {
					logger.warn("office details not found for:", officeCode);
					throw new BadRequestException("office details not found for: " + officeCode);
				}
				ClassOfVehiclesVO classOfvehicle = covService
						.findByCovCode(dto.getRegistrationDetails().getClassOfVehicle());
				dto.getRegistrationDetails().setClassOfVehicleDesc(classOfvehicle.getDescription());
				dto.getVoluntaryTaxDetails().setClassOfVehicleDesc(classOfvehicle.getDescription());
				dto.setCreatedDate(LocalDateTime.now());

				dto.setlUpdate(LocalDateTime.now());
				dto.setOfficeDetails(optionalOffice.get());
				dto.setOfficeCode(optionalOffice.get().getOfficeCode());
				dto.getVoluntaryTaxDetails().setOfficeCode(optionalOffice.get().getOfficeCode());
				Map<String, String> officeCodeMap = new HashMap<>();
				officeCodeMap.put("officeCode", optionalOffice.get().getOfficeCode());
				dto.setApplicationNo(sequenceGenerator
						.getSequence(String.valueOf(Sequence.REGISTRATIONAPPNO.getSequenceId()), officeCodeMap));
				dto.getVoluntaryTaxDetails().setApplicationNo(dto.getApplicationNo());
				saveCitizenServiceDoc(inputRegServiceVO, dto, uploadfiles);
				return regServiceMapper.convertEntity(dto);
			} catch (Exception e) {
				logger.error("Exception [{}] in voluntary tax", e.getMessage());
				throw new BadRequestException(e.getMessage());
			}
		}
	}

	private RegServiceDTO convertVoluntaryTaxDetails(VoluntaryTaxVO vo, HttpServletRequest request, UserDTO user,
			boolean requestFromApp) {
		RegServiceDTO regServiceDTO = new RegServiceDTO();
		VoluntaryTaxDTO voluntaryTaxDto = voluntaryTaxMapper.convertVO(vo);
		if (vo.getTaxvalidUpto() != null) {
			voluntaryTaxDto.setHomeTaxvalidUpto(vo.getTaxvalidUpto());
		}
		RegistrationDetailsDTO regDto = new RegistrationDetailsDTO();
		VahanDetailsDTO vahanDto = new VahanDetailsDTO();
		InvoiceDetailsDTO invoice = new InvoiceDetailsDTO();
		NOCDetailsDTO nocDto = new NOCDetailsDTO();
		ApplicantDetailsDTO applicantDetails = new ApplicantDetailsDTO();
		if (voluntaryTaxDto.getGvw() == null) {
			logger.error("Please provide gvw");
			throw new BadRequestException("Please provide gvw");
		}
		if (!requestFromApp) {
			if (StringUtils.isBlank(vo.getChassisNo())) {
				logger.error("Please provide chassis number");
				throw new BadRequestException("Please provide chassis number");
			}
			vahanDto.setChassisNumber(vo.getChassisNo());
		}
		vahanDto.setGvw(voluntaryTaxDto.getGvw());
		if (voluntaryTaxDto.getUlw() == null) {
			logger.error("Please provide ulw");
			throw new BadRequestException("Please provide ulw");
		}
		vahanDto.setUnladenWeight(voluntaryTaxDto.getUlw());
		if (StringUtils.isBlank(voluntaryTaxDto.getSeatingCapacity())) {
			logger.error("Please provide seating capacity");
			throw new BadRequestException("Please provide seating capacity");
		}
		vahanDto.setSeatingCapacity(voluntaryTaxDto.getSeatingCapacity());
		if (StringUtils.isNoneBlank(voluntaryTaxDto.getFuelDesc())) {
			vahanDto.setFuelDesc(voluntaryTaxDto.getFuelDesc());
		}
		if (StringUtils.isNoneBlank(voluntaryTaxDto.getMakersModel())) {
			vahanDto.setMakersModel(voluntaryTaxDto.getMakersModel());
		}
		if (StringUtils.isNoneBlank(voluntaryTaxDto.getChassisNo())) {
			vahanDto.setChassisNumber(voluntaryTaxDto.getChassisNo());
		}
		regDto.setVahanDetails(vahanDto);
		if (StringUtils.isBlank(voluntaryTaxDto.getClassOfVehicle())) {
			logger.error("Please provide class of vehicle");
			throw new BadRequestException("Please provide class of vehicle");
		}
		regDto.setClassOfVehicle(voluntaryTaxDto.getClassOfVehicle());
		if (voluntaryTaxDto.getInvoiceValue() != null) {
			invoice.setInvoiceValue(voluntaryTaxDto.getInvoiceValue());
		}
		regDto.setInvoiceDetails(invoice);
		if (voluntaryTaxDto.getOwnerType() != null) {
			regDto.setOwnerType(voluntaryTaxDto.getOwnerType());
		}
		regDto.setRegVehicleWithPR(voluntaryTaxDto.getOtherStateRegister());
		regDto.setRegVehicleWithTR(voluntaryTaxDto.getOtherStateUnregister());
		if (voluntaryTaxDto.getOtherStateRegister() && voluntaryTaxDto.getPrGeneratedDate() == null) {
			logger.error("Please provide Pr generation date");
			throw new BadRequestException("Please provide Pr generation date");
		}
		if (voluntaryTaxDto.getPrGeneratedDate() != null) {
			regDto.setPrIssueDate(voluntaryTaxDto.getPrGeneratedDate());
			regDto.setPrGeneratedDate(voluntaryTaxDto.getPrGeneratedDate().atStartOfDay());
		}
		if (StringUtils.isBlank(voluntaryTaxDto.getOwnerName())) {
			logger.error("Please provide Owner name");
			throw new BadRequestException("Please provide Owner name");
		}
		applicantDetails.setFirstName(voluntaryTaxDto.getOwnerName());
		regDto.setApplicantDetails(applicantDetails);
		regDto.setTrNo(vo.getTrNo());
		if (voluntaryTaxDto.getFirstVehicle() != null) {
			regDto.setIsFirstVehicle(voluntaryTaxDto.getFirstVehicle());
		}
		nocDto.setIssueDate(LocalDate.now());
		nocDto.setDateOfEntry(LocalDate.now());
		regServiceDTO.setnOCDetails(nocDto);
		if (StringUtils.isNoneBlank(voluntaryTaxDto.getRegNo())) {
			regServiceDTO.setPrNo(voluntaryTaxDto.getRegNo());
			regDto.setPrNo(voluntaryTaxDto.getRegNo());
		} else if (StringUtils.isNoneBlank(voluntaryTaxDto.getTrNo())) {
			regDto.setTrNo(voluntaryTaxDto.getTrNo());
			regServiceDTO.setTrNo(voluntaryTaxDto.getTrNo());
		} else {
			logger.error("Please provide prNo/trNo");
			throw new BadRequestException("Please provide prNo/trNo");
		}
		regServiceDTO.setVoluntaryTaxDetails(voluntaryTaxDto);
		if (voluntaryTaxDto.getGatewayType() == null) {
			logger.error("Please select payment gateway type");
			throw new BadRequestException("Please select payment gateway type");
		}
		regServiceDTO.setGatewayType(voluntaryTaxDto.getGatewayType().getDescription());
		if (voluntaryTaxDto.getHomeStateUnregister() != null && voluntaryTaxDto.getHomeStateUnregister()) {
			AlterationDTO alterDetails = new AlterationDTO();
			alterDetails.setCov(voluntaryTaxDto.getClassOfVehicle());
			alterDetails.setGvw(voluntaryTaxDto.getGvw());
			alterDetails.setSeating(voluntaryTaxDto.getSeatingCapacity());
			if (voluntaryTaxDto.getDateOfCompletion() == null) {
				logger.error("Please provide Date of completion");
				throw new BadRequestException("Please provide Date of completion");
			}
			alterDetails.setDateOfCompletion(voluntaryTaxDto.getDateOfCompletion());
			regServiceDTO.setAlterationDetails(alterDetails);
		} else {
			regDto.setApplicantType("OTHERSTATE");
		}
		regServiceDTO.setRegistrationDetails(regDto);
		this.setActions(voluntaryTaxDto, user, request.getRemoteAddr());
		return regServiceDTO;
	}

	private void setActions(VoluntaryTaxDTO voluntaryTaxDto, UserDTO userDetails, String ipAddress) {
		ActionDetails actions = new ActionDetails();
		if (userDetails != null) {
			actions.setUserId(userDetails.getUserId());
			actions.setAadharNo(userDetails.getAadharNo());
			voluntaryTaxDto.setUserId(userDetails.getUserId());
		} else {
			actions.setUserId("CITIZEN");
			// actions.setAadharNo(userDetails.getAadharNo());
			voluntaryTaxDto.setUserId("CITIZEN");
		}

		actions.setCreatedDate(LocalDateTime.now());
		actions.setIpAddress(ipAddress);
		if (voluntaryTaxDto.getGatewayType() != null) {
			actions.setModule(voluntaryTaxDto.getGatewayType().getDescription());
		}
		if (voluntaryTaxDto.getActions() == null || voluntaryTaxDto.getActions().isEmpty()) {
			voluntaryTaxDto.setActions(Arrays.asList(actions));
		} else {
			voluntaryTaxDto.getActions().add(actions);
		}

	}

	@Override
	public RegServiceVO fetchApplicationDetailsForCheckPostServices(ApplicationSearchVO applicationSearchVO) {
		List<RegServiceDTO> servicesList = null;

		List<ServiceEnum> serviceIds = new ArrayList<>();
		serviceIds.add(ServiceEnum.OTHERSTATETEMPORARYPERMIT);
		serviceIds.add(ServiceEnum.VOLUNTARYTAX);

		if (applicationSearchVO.getApplicationNo() != null
				&& StringUtils.isNoneBlank(applicationSearchVO.getApplicationNo())) {
			servicesList = regServiceDAO.findByApplicationNoAndServiceTypeIn(applicationSearchVO.getApplicationNo(),
					serviceIds);
		}

		if (applicationSearchVO.getTrNo() != null && StringUtils.isNoneBlank(applicationSearchVO.getTrNo())) {
			servicesList = regServiceDAO.findByTrNoAndServiceTypeIn(applicationSearchVO.getTrNo(), serviceIds);
		}

		if (applicationSearchVO.getPrNo() != null && StringUtils.isNoneBlank(applicationSearchVO.getPrNo())) {
			servicesList = regServiceDAO.findByPrNoAndServiceTypeIn(applicationSearchVO.getPrNo(), serviceIds);
		}

		RegServiceDTO regServiceDTO = getRegistrationServiceDetailsForCheckPostServices(servicesList);
		if (regServiceDTO == null) {
			throw new BadRequestException("No records found with Pr number");
		}

		return regServiceMapper.convertEntity(regServiceDTO);
	}

	private RegServiceDTO getRegistrationServiceDetailsForCheckPostServices(List<RegServiceDTO> servicesList) {
		servicesList.sort((s1, s2) -> s2.getCreatedDate().compareTo(s1.getCreatedDate()));
		return servicesList.stream().findFirst().get();
	}

	@Override
	public Optional<RegServiceVO> rcCancellationValidation(String prNo) {
		RegServiceVO vo = new RegServiceVO();
		List<RegServiceDTO> regList = regServiceDAO.findByPrNoAndServiceTypeIn(prNo,
				Arrays.asList(ServiceEnum.RCCANCELLATION));
		if (!regList.isEmpty()) {
			regList.sort((p1, p2) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
			RegServiceDTO regServiceDTO = regList.stream().findFirst().get();
			vo = regServiceMapper.convertEntity(regServiceDTO);
			return Optional.of(vo);
		}

		return Optional.empty();
	}

	@Override
	public RegistrationDetailsVO doRcCancellation(String prNo, String role, String officeCode)
			throws RcValidationException {
		Optional<RegistrationDetailsDTO> registrationDetailsDTO = null;
		try {
			registrationDetailsDTO = registrationDetailDAO.findByPrNo(prNo);
		} catch (BadRequestException e) {
			Optional<RegServiceVO> regServiceDetails = rcCancellationValidation(prNo);
			if (regServiceDetails.isPresent()) {
				checkRcValidation(regServiceDetails, prNo, role);
			} else {
				logger.error("No records found [{}]", prNo);
				throw new BadRequestException("No records found");
			}
		}
		checkRegValidations(registrationDetailsDTO, officeCode);
		RegistrationDetailsVO registrationDetailsVO = new RegistrationDetailsVO();
		Optional<RegServiceVO> regServiceDetails = rcCancellationValidation(prNo);
		if (regServiceDetails.isPresent()) {
			checkRcValidation(regServiceDetails, prNo, role);
		} else {
			registrationDetailsVO = registrationDetailsMapper.convertEntity(registrationDetailsDTO.get());
		}

		return registrationDetailsVO;

	}

	private List<String> freshRCValidations(RegistrationDetailsDTO registrationOptional, List<String> errors) {
		List<StatusRegistration> status = new ArrayList<StatusRegistration>();
		status.add(StatusRegistration.APPROVED);
		status.add(StatusRegistration.CANCELED);
		status.add(StatusRegistration.FRESHRCREJECTED);

		List<RegServiceDTO> regServiceList = regServiceDAO.findByprNoAndServiceIdsAndSourceIsNull(
				registrationOptional.getPrNo(), ServiceEnum.RCFORFINANCE.getId());
		if (!regServiceList.isEmpty()) {
			regServiceList.sort((p2, p1) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
			RegServiceDTO regServiceDTO = regServiceList.stream().findFirst().get();
			if (regServiceDTO.getApplicationStatus() != null
					&& !regServiceDTO.getApplicationStatus().equals(StatusRegistration.REJECTED)
					&& !status.contains(regServiceDTO.getApplicationStatus())) {
				errors.add("Fresh RC application pending with applicationNo" + regServiceDTO.getApplicationNo());
			}
			if ((regServiceDTO.getApplicationStatus().equals(StatusRegistration.REJECTED)
					&& !regServiceDTO.isMviDone())) {
				errors.add("Fresh RC application pending with applicationNo" + regServiceDTO.getApplicationNo());
			}
		}
		return errors;
	}

	private List<String> validationForRcCancellation(RegistrationDetailsDTO registrationOptional, List<String> errors) {
		if (registrationOptional.isRcCancelled() || registrationOptional.getApplicationStatus()
				.equals(StatusRegistration.RCCANCELLED.getDescription())) {
			logger.error("RC is cancelled for PR No [{}]", registrationOptional.getPrNo());
			errors.add("RC is cancelled for PR No " + registrationOptional.getPrNo());
		} else {
			Optional<RegServiceDTO> regServiceDtoOpt = regServiceDAO.findByPrNoAndServiceTypeInAndApplicationStatusIn(
					registrationOptional.getPrNo(), Arrays.asList(ServiceEnum.RCCANCELLATION),
					Arrays.asList(StatusRegistration.INITIATED.getDescription(),
							StatusRegistration.APPROVED.getDescription()));
			if (regServiceDtoOpt.isPresent()) {
				if (regServiceDtoOpt.get().getApplicationStatus().equals(StatusRegistration.APPROVED)) {
					errors.add("RC is cancelled for PR No " + registrationOptional.getPrNo());
				}
			}
		}
		return errors;
	}

	public CitizenSearchReportVO freshRcValidationForStatus(RegServiceDTO regServiceDTO, CitizenSearchReportVO vo) {
		if (regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId()))) {
			if (vo.getApplicationStatus().equals(StatusRegistration.PAYMENTDONE)) {
				vo.setApplicationStatus(StatusRegistration.FILEPENDINGATAO);
			}
		}
		return vo;
	}

	// As per murthy gaaru inputs commented TR flow for freshRC once murthy gaaru
	// gives approval uncomment code
	/*
	 * @Override public RegServiceVO savingRegistrationServicesForFreshRCTrNo(String
	 * regServiceVO, MultipartFile[] multipart, Boolean isTrNo, String user) throws
	 * IOException, RcValidationException { Optional<RegServiceVO> inputOptional =
	 * readValue(regServiceVO, RegServiceVO.class);
	 * inputOptional.get().setFinancierUserId(user);
	 * Optional<StagingRegistrationDetailsDTO> stageDetailsOpt =
	 * stagingRegistrationDetailsDAO .findByTrNo(inputOptional.get().getTrNo()); if
	 * (!stageDetailsOpt.isPresent()) { throw new
	 * BadRequestException("No record found for Tr no: [{}] " +
	 * inputOptional.get().getTrNo()); } return
	 * createCitizenDetailsForFreshRc(stageDetailsOpt.get(), multipart,
	 * inputOptional.get()); } private RegServiceVO
	 * createCitizenDetailsForFreshRc(RegistrationDetailsDTO
	 * stagingRegistrationDetailsDTO, MultipartFile[] multipart, RegServiceVO
	 * regServiceVO) throws IOException, RcValidationException { final String trNo =
	 * stagingRegistrationDetailsDTO.getTrNo(); synchronized (trNo.intern()) {
	 * Optional<KeyValue<RegServiceDTO, RegistrationDetailsDTO>>
	 * citizenObjectsOptional = Optional.empty(); List<ServiceEnum> serviceIds =
	 * regServiceVO.getServiceIds().stream() .map(id ->
	 * ServiceEnum.getServiceEnumById(id)).collect(Collectors.toList()); for
	 * (ServiceEnum service : serviceIds) { switch (service) { case RCFORFINANCE:
	 * citizenObjectsOptional = doRcForFinanceForFreshRc(citizenObjectsOptional,
	 * regServiceVO, this::doRcForFinanceForTrno); break; } } Map<String, String>
	 * officeCodeMap = new TreeMap<>(); OfficeVO vo = null; vo =
	 * getOfficeDetails(regServiceVO, citizenObjectsOptional.get().getKey());
	 * citizenObjectsOptional.get().getKey().setOfficeDetails(officeMapper.convertVO
	 * (vo));
	 * citizenObjectsOptional.get().getKey().setOfficeCode(vo.getOfficeCode());
	 * officeCodeMap.put("officeCode",
	 * citizenObjectsOptional.get().getKey().getOfficeCode());
	 * citizenObjectsOptional.get().getKey().setApplicationNo(sequenceGenerator
	 * .getSequence(String.valueOf(Sequence.REGISTRATIONAPPNO.getSequenceId()),
	 * officeCodeMap)); saveCitizenServiceDoc(regServiceVO,
	 * citizenObjectsOptional.get().getKey(), multipart); RegServiceVO regVo =
	 * regServiceMapper.convertEntity(citizenObjectsOptional.get().getKey()); return
	 * regVo; } }
	 * 
	 * 
	 * 
	 * private Optional<KeyValue<RegServiceDTO, RegistrationDetailsDTO>>
	 * doRcForFinanceForFreshRc( Optional<KeyValue<RegServiceDTO,
	 * RegistrationDetailsDTO>> regObjectsOptional, RegServiceVO input,
	 * BiConsumer<RegServiceVO, KeyValue<RegServiceDTO, RegistrationDetailsDTO>>
	 * regServiceFun) {
	 * 
	 * KeyValue<RegServiceDTO, RegistrationDetailsDTO> regObjects;
	 * 
	 * if (regObjectsOptional.isPresent()) {
	 * 
	 * regObjects = regObjectsOptional.get(); } else { regObjects =
	 * getBasicApplicationDetailsForFreshRc(input); }
	 * 
	 * regServiceFun.accept(input, regObjects);
	 * 
	 * return Optional.of(regObjects); }
	 * 
	 * 
	 * private KeyValue<RegServiceDTO, RegistrationDetailsDTO>
	 * getBasicApplicationDetailsForFreshRc( RegServiceVO input) {
	 * Optional<StagingRegistrationDetailsDTO> stageDetailsOpt =
	 * stagingRegistrationDetailsDAO .findByTrNo(input.getTrNo()); if
	 * (!stageDetailsOpt.isPresent()) { throw new
	 * BadRequestException("No  Application Details found. " + input.getTrNo()); }
	 * if (StringUtils.isNoneBlank(input.getAadhaarNo()) &&
	 * (stageDetailsOpt.get().getApplicantDetails() == null || !input.getAadhaarNo()
	 * .equals(stageDetailsOpt.get().getApplicantDetails().getAadharNo()))) { throw
	 * new BadRequestException("No  Application Details found with TR No: " +
	 * input.getTrNo() + " and aadhaar No: " + input.getAadhaarNo()); }
	 * RegistrationDetailsDTO regDetailsDTO = stageDetailsOpt.get(); RegServiceDTO
	 * regServiceDetails = registrationDetailsMapper.createNew(regDetailsDTO,
	 * input); return new KeyValue<>(regServiceDetails, regDetailsDTO); }
	 * 
	 * 
	 * private void doRcForFinanceForTrno(RegServiceVO input,
	 * KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {
	 * RegServiceDTO regServiceDTO = citizenObjects.getKey(); RegistrationDetailsDTO
	 * registrationDetailsDTO = citizenObjects.getValue(); String officeCode =
	 * StringUtils.EMPTY; Optional<UserDTO> userOptional =
	 * userDAO.findByUserId(input.getFinancierUserId()); if
	 * (!userOptional.isPresent()) { throw new
	 * BadRequestException("Financier details not found Please Sign Up"); }
	 * 
	 * officeCode = getMviOfficeCode(input,
	 * registrationDetailsDTO.getVehicleType());
	 * regServiceDTO.setMviOfficeCode(officeCode); Optional<OfficeDTO> officeDTOOpt
	 * = officeDAO.findByOfficeCode(officeCode); if (!officeDTOOpt.isPresent()) {
	 * throw new BadRequestException("Master Office Details Not Found wiht prNo" +
	 * input.getPrNo());
	 * 
	 * } Optional<StateDTO> stateDTOOpt = stateDao
	 * .findByStateName(input.getFreshRcVO().getYardAddress().getState().
	 * getStateName()); if (!stateDTOOpt.isPresent()) { throw new
	 * BadRequestException("state details not found with statename" +
	 * input.getFreshRcVO().getYardAddress().getState().getStateName()); }
	 * Optional<CountryDTO> countryDtoOpt =
	 * countryDao.findByCountryCode(NationalityEnum.IND.name()); if
	 * (!countryDtoOpt.isPresent()) { throw new
	 * BadRequestException("country details not found with Countryname " +
	 * input.getFreshRcVO().getYardAddress().getCountry().getCountryName()); }
	 * regServiceMapper.freshRcforFinanceMapper(input, userOptional.get(),
	 * regServiceDTO, officeDTOOpt.get());
	 * regServiceDTO.getFreshRcdetails().getYardAddress().setState(stateDTOOpt.get()
	 * );
	 * regServiceDTO.getFreshRcdetails().getYardAddress().setCountry(countryDtoOpt.
	 * get());
	 * 
	 * }
	 */

	private String getMviOfficeCode(RegServiceVO input, String vehileType) {
		if (input.getFreshRc().getYardAddress().getMandal().getMandalCode() != null) {
			Optional<MandalDTO> mandalDtoOpt = mandalDAO
					.findByMandalCode(input.getFreshRc().getYardAddress().getMandal().getMandalCode());
			if (mandalDtoOpt.isPresent()) {
				if (vehileType.equalsIgnoreCase(CovCategory.T.getCode())) {
					return mandalDtoOpt.get().getTransportOfice();
				} else if (vehileType.equalsIgnoreCase(CovCategory.N.getCode())) {
					return mandalDtoOpt.get().getNonTransportOffice();
				}
			}
			throw new BadRequestException(
					"Mandal Details Not Found " + input.getFreshRc().getYardAddress().getMandal().getMandalName());
		}
		throw new BadRequestException(
				"Mandal Details Not Found " + input.getFreshRc().getYardAddress().getMandal().getMandalName());

	}

	@Override
	public CitizenSearchReportVO applicationSearchForVcrClosed(ApplicationSearchVO applicationSearchVO) {

		List<VcrFinalServiceDTO> vcrList = null;
		CitizenSearchReportVO outPut = new CitizenSearchReportVO();
		vcrList = getClosedVcrs(applicationSearchVO, vcrList);
		if (vcrList == null || vcrList.isEmpty()) {
			throw new BadRequestException("No vcr details found");
		}
		// vcrList =
		// this.getVcrDetails(Arrays.asList(vcrList.stream().findFirst().get().getVcr().getVcrNumber()));
		outPut.setVcrList(vcrFinalServiceMapper.convertEntity(vcrList));
		// getVcrAmount(applicationSearchVO, vcrList, outPut);

		return outPut;
	}

	private List<VcrFinalServiceDTO> getClosedVcrs(ApplicationSearchVO applicationSearchVO,
			List<VcrFinalServiceDTO> vcrList) {
		if (StringUtils.isBlank(applicationSearchVO.getPrNo()) && StringUtils.isBlank(applicationSearchVO.getTrNo())
				&& StringUtils.isBlank(applicationSearchVO.getChassisNo())
				&& StringUtils.isBlank(applicationSearchVO.getVcrNo())) {
			logger.error("Please provide required data like prNo/trNo/chassisno/vcrno ");
			throw new BadRequestException("Please provide required data like prNo/trNo/chassisno/vcrno ");
		}
		if (applicationSearchVO.getPrNo() != null && StringUtils.isNoneBlank(applicationSearchVO.getPrNo())) {
			vcrList = vcrFinalServiceDAO.findTop10ByRegistrationRegNoAndIsVcrClosedIsTrueOrderByCreatedDateDesc(
					applicationSearchVO.getPrNo());
		}

		if (applicationSearchVO.getTrNo() != null && StringUtils.isNoneBlank(applicationSearchVO.getTrNo())) {
			vcrList = vcrFinalServiceDAO.findTop10ByRegistrationTrNoAndIsVcrClosedIsTrueOrderByCreatedDateDesc(
					applicationSearchVO.getTrNo());
		}

		if (applicationSearchVO.getChassisNo() != null && StringUtils.isNoneBlank(applicationSearchVO.getChassisNo())) {
			vcrList = vcrFinalServiceDAO.findTop10ByRegistrationChassisNumberAndIsVcrClosedIsTrueOrderByCreatedDateDesc(
					applicationSearchVO.getChassisNo());
		}
		if (applicationSearchVO.getVcrNo() != null && StringUtils.isNoneBlank(applicationSearchVO.getVcrNo())) {
			vcrList = vcrFinalServiceDAO.findByVcrVcrNumberIgnoreCaseAndIsVcrClosedIsTrueOrderByCreatedDateDesc(
					applicationSearchVO.getVcrNo());
		}

		return vcrList;
	}

	private List<String> taxTypes() {
		List<String> taxTypes = new ArrayList<>();
		taxTypes.add(ServiceCodeEnum.QLY_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.HALF_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.YEAR_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.LIFE_TAX.getCode());
		return taxTypes;
	}

	private void checkRegValidations(Optional<RegistrationDetailsDTO> registrationDetailsDTO, String officeCode) {
		if (!registrationDetailsDTO.get().getOfficeDetails().getOfficeCode().equals(officeCode)) {
			logger.error("PrNo is not related to your office  [{}]", registrationDetailsDTO.get().getPrNo());
			throw new BadRequestException(
					"PrNo is not related to your office  " + registrationDetailsDTO.get().getPrNo());
		}
		if (registrationDetailsDTO.get().getApplicationStatus()
				.equals(StatusRegistration.RCCANCELLED.getDescription())) {
			logger.error("RC is cancelled for prNo [{}]", registrationDetailsDTO.get().getPrNo());
			throw new BadRequestException("RC is cancelled for prNo  " + registrationDetailsDTO.get().getPrNo());
		}
	}

	private List<String> validationForRC(RegistrationDetailsDTO registrationDetailsDTO, List<String> errors) {
		checkVcrDues(registrationDetailsDTO, errors);
		if (registrationDetailsDTO.getVehicleType().equals(CovCategory.T.getCode())) {
			TaxHelper taxHelper = citizenTaxService.getLastPaidTax(registrationDetailsDTO, null, false, LocalDate.now(),
					null, false, taxTypes(), false, false);
			if (taxHelper != null) {
				if (taxHelper.isAnypendingQuaters()) {
					logger.error("Not eligible to apply for RC Cancellation until tax paid!",
							registrationDetailsDTO.getPrNo());
					errors.add("Not eligible to apply for RC Cancellation until tax paid! "
							+ registrationDetailsDTO.getPrNo());
				}
			}
		}
		return errors;
	}

	/**
	 * method to get aadhaar response from Db for TOW service for both web and
	 * mobile
	 **/
	private Optional<AadhaarDetailsResponseDTO> getAadhaarDetails(RegServiceVO regServiceVO) {
		if (regServiceVO.getTowDetails() != null) {
			if (regServiceVO.getTowDetails().getBuyerUUID() != null) {
				Optional<AadhaarDetailsResponseDTO> aadhaarDTO = aadhaarResponseDAO
						.findByUuId(regServiceVO.getTowDetails().getBuyerUUID());
				if (aadhaarDTO.isPresent() && aadhaarDTO.get().getUid() != null) {
					return aadhaarDTO;
				}
				if (regServiceVO.getTowDetails().getBuyerAadhaarNo() != null
						&& regServiceVO.getTowDetails().getIsMobileService()) {
					return aadhaarResponseDAO.findByUid(regServiceVO.getTowDetails().getBuyerAadhaarNo());
				}

			}
		}
		return Optional.empty();
	}

	@Override
	public List<VcrFinalServiceDTO> applicationSearchVcrReleaseOrder(ApplicationSearchVO applicationSearchVO) {
		List<VcrFinalServiceDTO> vcrList = null;
		if (StringUtils.isNoneBlank(applicationSearchVO.getVcrNo())) {
			vcrList = vcrFinalServiceDAO.findByVcrVcrNumberIgnoreCase(applicationSearchVO.getVcrNo());
		}
		return vcrList;
	}

	private void rcCancellationProcess(KeyValue<RegServiceDTO, RegistrationDetailsDTO> keyValue,
			RegServiceVO regServiceVO) {
		if (keyValue != null) {
			RegistrationDetailsDTO registrationDetails = keyValue.getValue();
			RegServiceDTO regServiceDTO = keyValue.getKey();
			registrationDetails.setApplicationStatus(StatusRegistration.RCCANCELLED.getDescription());
			registrationDetails.setRcCancelled(Boolean.TRUE);
			registrationDetails.setRcCancelledDate(LocalDate.now());
			registrationDetails.getApplicantDetails().getContact()
					.setMobile(regServiceVO.getContactDetails().getMobile());
			registrationDetails.getApplicantDetails().getContact()
					.setEmail(regServiceVO.getContactDetails().getEmail());
			regServiceDTO.getRegistrationDetails().getApplicantDetails().getContact()
					.setMobile(regServiceVO.getContactDetails().getMobile());
			regServiceDTO.getRegistrationDetails().getApplicantDetails().getContact()
					.setEmail(regServiceVO.getContactDetails().getEmail());
			makingPermitDetailsAsInactive(registrationDetails);
		}
	}

	@Override
	public void makingPermitDetailsAsInactive(RegistrationDetailsDTO registrationDetails) {
		List<PermitDetailsDTO> permitDetailsList = permitDetailsDAO
				.findByPrNoAndPermitStatus(registrationDetails.getPrNo(), PermitsEnum.ACTIVE.getDescription());
		if (CollectionUtils.isNotEmpty(permitDetailsList)) {
			if (registrationDetails.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())) {
				Optional<PermitMandalExemptionDTO> dto = permitMandalExemptionDAO.findByMandalCodeAndStatusTrue(
						registrationDetails.getApplicantDetails().getPresentAddress().getMandal().getMandalCode());
				if (!dto.isPresent()) {
					inActivePermitDeatils(permitDetailsList);
				} else {
					List<PermitDetailsDTO> inActiveArktOtherPermits = new ArrayList<>();
					permitDetailsList.stream().forEach(val -> {
						if (!val.getPermitClass().getCode().equalsIgnoreCase(PermitType.PRIMARY.getPermitTypeCode())) {
							inActiveArktOtherPermits.add(val);
						}
					});
					inActivePermitDeatils(inActiveArktOtherPermits);
				}
			} else {
				inActivePermitDeatils(permitDetailsList);
				permitDetailsDAO.save(permitDetailsList);
				logger.info("Permit Surrendered by citizen for PRNO {}", registrationDetails.getPrNo());
			}
		}
	}

	private void inActivePermitDeatils(List<PermitDetailsDTO> permitDetailsList) {
		if (!permitDetailsList.isEmpty()) {
			permitDetailsList.stream().forEach(permitDetails -> {
				permitDetails.setPermitStatus(PermitsEnum.INACTIVE.getDescription());
				permitDetails.setPermitSurrender(true);
				permitDetails.setPermitSurrenderDate(LocalDate.now());
				permitDetails.setIsRcCancelled(Boolean.TRUE);
				permitDetails.setRcCancelledDate(LocalDate.now());
				permitDetails.setRcCancelledBy(ModuleEnum.CITIZEN.getCode());
			});
		}
	}

	@Override
	public void cancellationOfRegServices(String applicationNumber) {
		synchronized (applicationNumber) {

			Long validDays = 0l;
			Optional<RegServiceDTO> regServiceDoc = regServiceDAO.findByApplicationNo(applicationNumber);

			if (!regServiceDoc.isPresent()) {

				Optional<StagingRegistrationDetailsDTO> stagingDocument = stagingRegistrationDetailsDAO
						.findByApplicationNo(applicationNumber);
				RegistrationDetailsDTO regDTO = null;
				StagingRegistrationDetailsDTO stagingDoc = stagingDocument.get();
				if (!stagingDocument.isPresent()) {

					Optional<RegistrationDetailsDTO> regData = registrationDetailDAO
							.findByApplicationNo(applicationNumber);
					regDTO = regData.get();
					stagingDoc = (StagingRegistrationDetailsDTO) regDTO;

					throw new BadRequestException(
							"No records found with this is application number " + applicationNumber);
				}

				if (stagingDoc.getTrGeneratedDate() != null) {
					validDays = ChronoUnit.DAYS.between(LocalDate.now(), stagingDocument.get().getTrGeneratedDate());
					if (validDays > 7l) {
						throw new BadRequestException(
								"You are not eligible to cancel the application" + applicationNumber);
					}
				}

				if (StringUtils.isNotBlank(stagingDoc.getPrNo())) {

					// Validation and Making reopen for the document if pr_pool collection
					validateAndRemovePrNumberFromPrPool(stagingDoc.getPrNo());

					stagingRegistrationDetailsDAO.delete(stagingDoc);
				}
			}

			// Action Details Verification
			if (regServiceDoc.get().getActionDetails() != null) {
				regServiceDoc.get().getActionDetails().stream().forEach(val -> {
					if (val.getStatus() != null && val.getUserId() != null) {
						throw new BadRequestException(
								"You are not eligible to cancel the application" + applicationNumber);
					}
				});
			}

			// Status verification
			if (!statusVerificationList().contains(regServiceDoc.get().getApplicationStatus())) {
				throw new BadRequestException("You are not eligible to cancel the application" + applicationNumber);
			}

			// HPA Validation if financier accepts the application
			if (regServiceDoc.get().getServiceIds().stream().anyMatch(id -> (id.equals(ServiceEnum.HPA.getId())))) {
				if (regServiceDoc.get().getIsHPADone()) {
					throw new BadRequestException("You are not eligible to cancel the application" + applicationNumber);
				}
			}

			// HPT Validation if financier accepts the application
			if (regServiceDoc.get().getServiceIds().stream()
					.anyMatch(id -> (id.equals(ServiceEnum.HIREPURCHASETERMINATION.getId())))) {
				if (regServiceDoc.get().getIsHPTDone()) {
					throw new BadRequestException("You are not eligible to cancel the application" + applicationNumber);
				}
			}

			// TOW validation if Buyer already initiates process
			if (regServiceDoc.get().getServiceIds().stream()
					.anyMatch(id -> (id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId())))) {
				if (regServiceDoc.get().getBuyerDetails() != null) {
					throw new BadRequestException("You are not eligible to cancel the application" + applicationNumber);
				}
			}

			if (regServiceDoc.get().getServiceIds().stream()
					.anyMatch(id -> (id.equals(ServiceEnum.VCR.getId())
							|| id.equals(ServiceEnum.OTHERSTATETEMPORARYPERMIT.getId())
							|| id.equals(ServiceEnum.OTHERSTATESPECIALPERMIT.getId())))) {
				throw new BadRequestException("You are not eligible to cancel the application" + applicationNumber);

			}

			regServiceDoc.get().setApplicationStatus(StatusRegistration.CANCELED);
			regServiceDAO.save(regServiceDoc.get());
		}
	}

	@Autowired
	private PRPoolDAO pRPoolDAO;

	@Autowired
	private GeneratedPrDetailsDAO generatedPrDetailsDAO;

	/**
	 * Don't use this method it's used exclusively for cancellation of application
	 * in staging_details and in registration_details
	 * 
	 * Validation with PRNUMBER number in pr_pool collection
	 * 
	 * and removing from generated_pr_Details to skip the prNumber validation
	 * 
	 * @param prNo
	 */
	private void validateAndRemovePrNumberFromPrPool(String prNo) {
		synchronized (prNo) {

			Optional<PRPoolDTO> prPoolDTO = pRPoolDAO.findByPrNo(prNo);
			if (prPoolDTO.isPresent() && NumberPoolStatus.ASSIGNED.equals(prPoolDTO.get().getPoolStatus())) {
				prPoolDTO.get().setPoolStatus(NumberPoolStatus.REOPEN);
			}
			List<GeneratedPrDetailsDTO> generatedPrDetails = generatedPrDetailsDAO.findByPrNo(prNo);
			if (CollectionUtils.isNotEmpty(generatedPrDetails)) {
				generatedPrDetailsDAO.delete(generatedPrDetails);
				pRPoolDAO.save(prPoolDTO.get());
			}
		}
	}

	private List<StatusRegistration> statusVerificationList() {
		List<StatusRegistration> statusList = new ArrayList<>();
		statusList.add(StatusRegistration.CITIZENPAYMENTFAILED);
		statusList.add(StatusRegistration.PAYMENTPENDING);
		statusList.add(StatusRegistration.CITIZENSUBMITTED);
		statusList.add(StatusRegistration.CITIZENSUBMITTED);
		statusList.add(StatusRegistration.PAYMENTDONE);
		statusList.add(StatusRegistration.OTHERSTATEPAYMENTPENDING);
		statusList.add(StatusRegistration.INITIATED);
		return statusList;
	}

	@Override
	public boolean shouldNotAllowForPayCash(VcrFinalServiceDTO vcdDoc) {
		try {
			TaxHelper taxAmount = citizenTaxService.getTaxDetails(null, false, false, ServiceCodeEnum.QLY_TAX.getCode(),
					false, null, Arrays.asList(ServiceEnum.VCR), null, null, false, null,
					Arrays.asList(vcdDoc.getVcr().getVcrNumber()), null,false);
			if (taxAmount != null) {
				vcdDoc.setTaxAmountForPrint(taxAmount);
			}
		} catch (Exception ex) {

		}

		for (OffenceDTO offence : vcdDoc.getOffence().getOffence()) {
			if (offence.isShouldNotClose()) {
				return true;
			}
			if (offence.getOtherOffence() != null && offence.getOtherOffence() && offence.getAmount1() != null
					&& offence.getAmount1() <= 0) {
				return true;
			}
		}
		if (this.getOpenedVcrs(vcdDoc)) {
			return true;
		}
		if (vcdDoc.getRegistration().isOtherState()) {
			if (vcdDoc.getOffence().getOffence().stream()
					.anyMatch(id -> (id.getOffenceDescription().equalsIgnoreCase("Without Payment of Tax"))
							|| (id.getIntrastate() != null && id.getIntrastate()))) {
				return true;
			}
			if (vcdDoc.getRegistration().getNocIssued() != null && vcdDoc.getRegistration().getNocIssued()) {
				return true;
			}
		} else if (StringUtils.isNoneBlank(vcdDoc.getRegistration().getRegApplicationNo())) {
			try {
				TaxHelper tax = citizenTaxService.getTaxDetails(null, false, false, ServiceCodeEnum.CESS_FEE.getCode(),
						false, null, Arrays.asList(ServiceEnum.VCR), null, null, false, null,
						Arrays.asList(vcdDoc.getVcr().getVcrNumber()), null,false);
				if (tax != null && tax.getTaxAmountForPayments() != 0) {
					return true;
				}
			} catch (Exception ex) {

			}

		}
		return false;

	}

	@Override
	public void savingImagesFromMviApp(String regServiceVO, MultipartFile[] multipart, JwtUser jwtUser) {
		logger.debug("regServiceVO [{}] ", regServiceVO);

		if (StringUtils.isBlank(regServiceVO)) {
			logger.error("regServiceVO is required.");
			throw new BadRequestException("regServiceVO is required.");
		}

		Optional<RegServiceVO> inputOptional = readValue(regServiceVO, RegServiceVO.class);

		if (!inputOptional.isPresent()) {
			logger.error(" Problem in reading json String to RegServiceVO [{}]");
			throw new BadRequestException("Invalid Inputs.");
		}

		RegServiceVO vo = inputOptional.get();
		if (StringUtils.isBlank(vo.getApplicationNo())) {
			logger.error("Please provide ApplicationNo.");
			throw new BadRequestException("Please provide ApplicationNo.");
		}
		/*
		 * List<RegServiceDTO> regServiceList
		 * =regServiceDAO.findByPrNo(vo.getApplicationNo()); if
		 * (regServiceList.isEmpty()) {
		 * logger.error(" Problem in getting records from regservice"); throw new
		 * BadRequestException("No Records Found."); } regServiceList.sort((p1, p2) ->
		 * p2.getCreatedDate().compareTo(p1.getCreatedDate())); Optional<RegServiceDTO>
		 * regOptional =regServiceList.stream().findFirst();
		 * 
		 * if (StringUtils.isBlank(regOptional.get().getApplicationNo())) {
		 * logger.error("Application Not Found For that PR Number.");//Please provide
		 * application number. throw new
		 * BadRequestException("Application No Not Found For that PR Number."); }
		 */

		if (multipart.length <= 0) {
			logger.error("Please upload images for application number: " + vo.getApplicationNo());
			throw new BadRequestException("Please upload images for application number: " + vo.getApplicationNo());

		}
		if (vo.getImageInput() == null || vo.getImageInput().isEmpty()) {
			logger.error("Please provide image input for application number: " + vo.getApplicationNo());
			throw new BadRequestException(
					"Please provide image input for application number: " + vo.getApplicationNo());

		}

		Optional<RegServiceDTO> regOptional = regServiceDAO.findByApplicationNo(vo.getApplicationNo());
		if (regOptional.isPresent()) {
			RegServiceDTO dto = regOptional.get();
			if (dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))) {
				this.checkMviNameForStoppage(dto, jwtUser);
				List<VehicleStoppageMVIReportVO> pendingReports = registratrionServicesApprovals
						.getPendingQuarters(dto);
				if (pendingReports == null || pendingReports.isEmpty()) {
					logger.error("No pending quaters to inspect for application number: " + vo.getApplicationNo());
					throw new BadRequestException(
							"No pending quaters to inspect for application number: " + vo.getApplicationNo());
				}
				int quarterNumber = pendingReports.stream().findFirst().get().getQuarterNumber();
				Optional<CitizenEnclosuresDTO> enclosresOptional = citizenEnclosuresDAO
						.findByApplicationNo(vo.getApplicationNo());
				if (enclosresOptional.isPresent()) {
					CitizenEnclosuresDTO enclosresDto = enclosresOptional.get();
					for (KeyValue<String, List<ImageEnclosureDTO>> key : enclosresDto.getEnclosures()) {
						for (ImageEnclosureDTO imgage : key.getValue()) {

							if (imgage.getQuarterNumber() != null && imgage.getQuarterNumber().equals(quarterNumber)) {
								logger.error("Current quarter images updated in DB for application number: "
										+ vo.getApplicationNo());
								throw new BadRequestException(
										"Current quarter images updated in DB for application number: "
												+ vo.getApplicationNo());
							}
						}
					}

				}
				vo.getImageInput().forEach(one -> {
					one.setQuarterNumber(quarterNumber);
				});
			} else if (dto.getServiceIds().stream()
					.anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId()))) {
				this.checkMviNameForStoppage(dto, jwtUser);
				Optional<CitizenEnclosuresDTO> enclosresOptional = citizenEnclosuresDAO
						.findByApplicationNo(vo.getApplicationNo());
				if (enclosresOptional.isPresent()) {
					logger.error("Enclosures added for this vehicle : " + vo.getApplicationNo());
					throw new BadRequestException("Enclosures added for this vehicle : " + vo.getApplicationNo());
				}

			}
			try {
				saveImages(vo, dto, multipart);
			} catch (IOException e) {
				logger.error(" Some thing wrong while save the image. Error is: " + e.getMessage());
				throw new BadRequestException(" Some thing wrong while save the image. Error is: " + e.getMessage());
			}
		} else {
			Optional<DealerRegDTO> dealerDTO = dealerRegDAO.findByApplicationNo(vo.getApplicationNo());

			if (!dealerDTO.isPresent()) {
				throw new BadRequestException("No records found with this application number " + vo.getApplicationNo());
			}
			if (dealerDTO.get().getIsMVIDone() != null && dealerDTO.get().getIsMVIDone()) {
				throw new BadRequestException("This application is at DTC level : " + vo.getApplicationNo());
			}

			DealerRegDTO dto = dealerDTO.get();
			if (dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.DEALERREGISTRATION.getId()))) {
				if (!dto.getLockedDetails().stream()
						.anyMatch(one -> one.getLockedBy().equalsIgnoreCase(jwtUser.getUsername()))) {
					logger.error("Unauthorized user of application No [{}]", dto.getApplicationNo());
					throw new BadRequestException("Unauthorized User");
				}
			}
			try {
				saveImagesForDealerRegistration(vo, dto, multipart, jwtUser);
			} catch (IOException e) {
				logger.error(" Some thing wrong while save the image. Error is: " + e.getMessage());
				throw new BadRequestException(" Some thing wrong while save the image. Error is: " + e.getMessage());
			}

		}
	}

	private void saveImagesForDealerRegistration(RegServiceVO regVO, DealerRegDTO dealerDTO,
			MultipartFile[] uploadfiles, JwtUser jwtUser) throws IOException {

		if (uploadfiles != null && uploadfiles.length == 0) {
			throw new BadRequestException("No images found");
		}
		CitizenEnclosuresDTO dto = new CitizenEnclosuresDTO();
		List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = null;

		enclosures = gridFsClient.convertImages(regVO.getImageInput(), dealerDTO.getApplicationNo(), uploadfiles,
				StatusRegistration.INITIATED.getDescription());

		ImageActionsDTO actiondto = new ImageActionsDTO();
		actiondto.setRole(RoleEnum.MVI.getName());
		for (KeyValue<String, List<ImageEnclosureDTO>> listImages : enclosures) {
			for (ImageEnclosureDTO enclosure : listImages.getValue()) {
				actiondto.setAction(enclosure.getImageStaus());
				actiondto.setComments(enclosure.getRemarks());
				enclosure.setImageActions(Arrays.asList(actiondto));
			}
		}

		Set<Integer> services = new HashSet<>();
		services.addAll(dealerDTO.getServiceIds());
		dto.setApplicationNo(dealerDTO.getApplicationNo());
		dto.setAadharNo(dealerDTO.getApplicantDetails().getAadharNo());
		dto.setEnclosures(enclosures);
		dto.setServiceIds(services);
		dealerDTO.setEnclosures(enclosures);

		Optional<UserDTO> user = userDAO.findByUserId(jwtUser.getUsername());
		if (user.isPresent() && !user.get().getPrimaryRole().getName().equals(RoleEnum.MVI.getName())
				|| (!user.get().getAdditionalRoles().isEmpty() && !user.get().getAdditionalRoles().stream()
						.anyMatch(name -> name.getName().equalsIgnoreCase(RoleEnum.MVI.getName())))) {

			// throw new BadRequestException("User not have mvi role:
			// "+user.get().getUserId());

		}

		ActionDetails actionDetail = getActionDetailByRoleForDealer(dealerDTO, RoleEnum.MVI.getName());

		this.updateActionDetailsStatus(RoleEnum.MVI.getName(), jwtUser.getId(), "DONE", actionDetail,
				dealerDTO.getApplicationNo());

		dealerDTO.setCurrentIndex(actionDetail.getNextIndex());
		Set<String> rolels = new HashSet<>();
		rolels.add(RoleEnum.DTC.getName());
		dealerDTO.setCurrentRoles(rolels);
		citizenEnclosuresDAO.save(dto);
		dealerDTO.setIsMVIDone(Boolean.TRUE);

		if (StringUtils.isNotEmpty(regVO.getStatus())) {
			if (regVO.getStatus().equalsIgnoreCase(StatusRegistration.APPROVED.getDescription())) {
				dealerDTO.setApplicationStatus(StatusRegistration.MVIAPPROVED);
			} else if (regVO.getStatus().equalsIgnoreCase(StatusRegistration.REJECTED.getDescription())) {
				dealerDTO.setApplicationStatus(StatusRegistration.MVIREJECTED);
			}
		}

		dealerRegDAO.save(dealerDTO);
	}

	private void updateActionDetailsStatus(String role, String id, String string, ActionDetails actionDetail,
			String applicationNo) {

		actionDetail.setApplicationNo(applicationNo);
		actionDetail.setRole(role);
		actionDetail.setUserId(id);
		// actionDetail.setEnclosures(actionVo.getEnclosures());
		actionDetail.setIsDoneProcess(Boolean.TRUE);
		actionDetail.setStatus(string);
		actionDetail.setlUpdate(LocalDateTime.now());

	}

	private ActionDetails getActionDetailByRoleForDealer(DealerRegDTO dealerRegDTO, String role) {

		Optional<ActionDetails> actionDetailsOpt = dealerRegDTO.getActionDetails().stream()
				.filter(p -> role.equals(p.getRole())).findFirst();
		if (!actionDetailsOpt.isPresent()) {
			logger.error("User role [{}] specific details not found in action detail", role);
			throw new BadRequestException("User role " + role + " specific details not found in actiondetail");
		}
		return actionDetailsOpt.get();
	}

	private void checkRcValidation(Optional<RegServiceVO> regServiceDetails, String prNo, String role) {

		RegServiceVO regServiceVO = regServiceDetails.get();
		if (role.equals(RoleEnum.CCO.getName())) {
			if (!regServiceVO.getServiceIds().contains(ServiceEnum.RCCANCELLATION.getId())) {
				logger.error("Vehicle [{}] is not applied for RC cancellation", prNo);
				throw new BadRequestException(
						"Vehicle[" + prNo + "] not applied for RC cancellation, Please apply RC cancellation");
			}
		}
		if ((regServiceVO.getServiceIds().contains(ServiceEnum.RCCANCELLATION.getId())
				&& regServiceVO.getApplicationStatus().toString().equals(StatusRegistration.INITIATED.getDescription()))
				|| (regServiceVO.getServiceIds().contains(ServiceEnum.RCCANCELLATION.getId())
						&& regServiceVO.getApplicationStatus().toString()
								.equals(StatusRegistration.APPROVED.getDescription())
						&& regServiceVO.getCurrentIndex() < (RoleEnum.RTO.getIndex() + 1))) {
			logger.error("RC CANCELLATION is already open for prNo [{}]", prNo);
			throw new BadRequestException("RC CANCELLATION is already open for prNo " + prNo);
		}
		if (regServiceVO.getServiceIds().contains(ServiceEnum.RCCANCELLATION.getId())
				&& regServiceVO.getApplicationStatus().toString().equals(StatusRegistration.APPROVED.getDescription())
				&& regServiceVO.getCurrentIndex().equals(RoleEnum.RTO.getIndex() + 1)) {
			logger.error("RC is Cancelled for prNo " + prNo);
			throw new BadRequestException("RC is Cancelled for prNo " + prNo);
		}

	}

	private TaxPaidVCRDetailsVO getOnlineVcrDetalsForOtherState(VcrValidationVo vcrValidationVo) {
		ApplicationSearchVO applicationSearchVO = new ApplicationSearchVO();
		applicationSearchVO.setPrNo(vcrValidationVo.getPrNo());
		applicationSearchVO.setVcrNo(vcrValidationVo.getVcrNum());
		List<VcrFinalServiceDTO> vcrList = null;
		vcrList = this.getClosedVcrs(applicationSearchVO, vcrList);
		if (vcrList != null && !vcrList.isEmpty()) {
			VcrFinalServiceDTO vcrList1 = vcrList.stream().findFirst().get();
			VcrTaxDetails onlineVCRDetal = new VcrTaxDetails();
			List<VcrTaxDetails> vcrTaxDetails = new ArrayList<>();
			onlineVCRDetal.setTaxAmt(vcrList1.getTax());
			onlineVCRDetal.setBookDate(DateConverters.convertCfstSyncLocalDateTimeFormat(vcrList1.getCreatedDate()));
			onlineVCRDetal.setManVcrNo(vcrList1.getVcr().getVcrNumber());
			onlineVCRDetal.setPaymtDate(DateConverters.convertCfstSyncLocalDateTimeFormat(vcrList1.getPaidDate()));
			onlineVCRDetal.setMviName(vcrList1.getCreatedBy());
			onlineVCRDetal.setVcrNo(vcrList1.getVcr().getVcrNumber());
			onlineVCRDetal.setRegnNo(vcrValidationVo.getPrNo());
			vcrTaxDetails.add(onlineVCRDetal);
			TaxPaidVCRDetailsVO onlineVCRDetals = new TaxPaidVCRDetailsVO();
			onlineVCRDetals.setTaxPaidVcrDetails(vcrTaxDetails);
			return onlineVCRDetals;
		}
		return null;
	}

	@Override
	public RegServiceVO saveStageCarriageServices(String regServiceVO, MultipartFile[] multipart, JwtUser user)
			throws IOException, RcValidationException {

		logger.debug("regServiceVO [{}] ", regServiceVO);

		if (StringUtils.isBlank(regServiceVO)) {
			logger.error("regServiceVO is required.");
			throw new BadRequestException("regServiceVO is required.");
		}

		Optional<RegServiceVO> inputOptional = readValue(regServiceVO, RegServiceVO.class);

		if (!inputOptional.isPresent()) {
			logger.error(" Problem in reading json String to RegServiceVO [{}]");
			throw new BadRequestException("Invalid Inputs.");
		}
		RegServiceVO vo = inputOptional.get();
		if (StringUtils.isBlank(vo.getPrNo())) {
			logger.error("Please provide pr number");
			throw new BadRequestException("Please provide pr number");
		}
		final String prNo = vo.getPrNo();

		synchronized (prNo.intern()) {

			if (vo.getServiceIds() == null) {
				logger.error("Please provide service type for pr no: " + vo.getPrNo());
				throw new BadRequestException("Please provide service type for pr no: " + vo.getPrNo());
			}
			if (StringUtils.isBlank(vo.getGatewayType())) {
				logger.error("Please select payment gatway type for pr no: " + vo.getPrNo());
				throw new BadRequestException("Please select payment gatway type for pr no: " + vo.getPrNo());
			}
			Optional<KeyValue<RegServiceDTO, RegistrationDetailsDTO>> citizenObjectsOptional = Optional.empty();
			List<ServiceEnum> serviceIds = vo.getServiceIds().stream().map(id -> ServiceEnum.getServiceEnumById(id))
					.collect(Collectors.toList());

			if (user != null) {
				vo.setCreatedBy(user.getId());
			}

			for (ServiceEnum se : serviceIds) {
				switch (se) {
				case STAGECARRIAGERENEWALOFPERMIT:

					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, vo, this::doSCRTRenewalOfPermit);
					break;
				case STAGECARRIAGEREPLACEMENTOFVEHICLE:

					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, vo, this::doSCRTReplaceOfvehicle);
					break;

				case NEWSTAGECARRIAGEPERMIT:
					citizenObjectsOptional = doCitizenService(citizenObjectsOptional, vo, this::doNewSCRTPermit);
					break;

				default:
					break;
				}
			}
			RcValidationVO rcValidationVO = new RcValidationVO();
			rcValidationVO.setPrNo(prNo);
			rcValidationVO.setServiceIds(vo.getServiceIds());
			try {
				stageCarriageServices.validationForSCRTServices(rcValidationVO,
						citizenObjectsOptional.get().getValue());
			} catch (RcValidationException e) {
				logger.error("Exception in RcValidation [{}] ", e);
				throw new RcValidationException(e.getErrors());
			}
			OfficeVO officeVo = null;
			officeVo = getOfficeDetails(vo, citizenObjectsOptional.get().getKey());
			citizenObjectsOptional.get().getKey().setOfficeDetails(officeMapper.convertVO(officeVo));
			citizenObjectsOptional.get().getKey().setOfficeCode(officeVo.getOfficeCode());
			Map<String, String> officeCodeMap = new TreeMap<>();
			officeCodeMap.put("officeCode", citizenObjectsOptional.get().getKey().getOfficeCode());
			citizenObjectsOptional.get().getKey().setApplicationNo(sequenceGenerator
					.getSequence(String.valueOf(Sequence.REGISTRATIONAPPNO.getSequenceId()), officeCodeMap));
			saveRegistrationDetailsDoc(vo, citizenObjectsOptional.get());
			saveCitizenServiceDoc(vo, citizenObjectsOptional.get().getKey(), multipart);
			RegServiceVO regVo = regServiceMapper.convertEntity(citizenObjectsOptional.get().getKey());
			return regVo;
		}

	}

	private void doNewSCRTPermit(RegServiceVO input, KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {
		RegServiceDTO regServiceDTO = citizenObjects.getKey();
		PermitDetailsDTO permitDetailsDTO = permitDetailsMapper.convertVO(input.getPermitDetailsVO());
		if (StringUtils.isBlank(permitDetailsDTO.getPermitNo())) {
			permitDetailsDTO.setPermitNo(
					permitsService.generatePermitNo(citizenObjects.getValue().getOfficeDetails().getOfficeCode(),
							permitDetailsDTO.getPermitType().getNumberCode()));
		}
		permitDetailsDTO.setCreatedBy(input.getCreatedBy());
		permitDetailsDTO.setPrNo(regServiceDTO.getPrNo());

		List<ActionDetails> actionDetailsList = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(regServiceDTO.getActionDetails())) {
			actionDetailsList = regServiceDTO.getActionDetails();
		}

		ActionDetails actionDetails = new ActionDetails();
		actionDetails.setRole(RoleEnum.CCO.getName());
		actionDetails.setUserId(input.getCreatedBy());
		actionDetails.setStatus(StatusRegistration.APPROVED.getDescription());
		actionDetails.setCreatedBy(input.getCreatedBy());
		actionDetails.setCreatedDate(LocalDateTime.now());
		actionDetailsList.add(actionDetails);

		regServiceDTO.setCreatedBy(input.getCreatedBy());
		regServiceDTO.setPdtl(permitDetailsDTO);

		regServiceDTO.setFeeDetails(feeDetailsMapper.convertVO(input.getFeeDetails()));

		regServiceDTO.setActionDetails(actionDetailsList);
	}

	@Autowired
	private FeeDetailsMapper feeDetailsMapper;

	private void doSCRTRenewalOfPermit(RegServiceVO input,
			KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {
		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		Optional<PermitDetailsDTO> dto = permitDetailsDAO.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(
				input.getPrNo(), PermitType.PRIMARY.getPermitTypeCode(), PermitsEnum.ACTIVE.getDescription());
		if (!dto.isPresent()) {
			logger.error("Permit details not found for pr number: " + input.getPrNo());
			throw new BadRequestException("Permit details not found for pr number: " + input.getPrNo());
		}
		regServiceDetails.setPdtl(dto.get());

	}

	private void doSCRTReplaceOfvehicle(RegServiceVO input,
			KeyValue<RegServiceDTO, RegistrationDetailsDTO> citizenObjects) {
		if (StringUtils.isBlank(input.getPermitVehiclePrNo())) {
			logger.error("Please give vehicle number for permit replacement: " + input.getPrNo());
			throw new BadRequestException("Please give vehicle number for permit replacement: " + input.getPrNo());
		}
		RegServiceDTO regServiceDetails = citizenObjects.getKey();
		Optional<PermitDetailsDTO> dto = permitDetailsDAO.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(
				input.getPermitVehiclePrNo(), PermitType.PRIMARY.getPermitTypeCode(),
				PermitsEnum.ACTIVE.getDescription());
		if (!dto.isPresent()) {
			logger.error("Permit details not found for pr number: " + input.getPrNo());
			throw new BadRequestException("Permit details not found for pr number: " + input.getPrNo());
		}

		if (input.getPermitVehiclePrNo().equalsIgnoreCase(input.getPrNo())) {
			logger.error("Please give vehicle number for permit replacement: " + input.getPrNo());
			throw new BadRequestException("Please give vehicle number for permit replacement: " + input.getPrNo());
		}
		stageCarriageServices.validationForScrtReplacementOfVehicle(input.getPrNo(), input.getPermitVehiclePrNo());
		PermitDetailsDTO permitDetails = dto.get();
		permitDetails.setPermitVehiclePrNo(input.getPermitVehiclePrNo());
		regServiceDetails.setPdtl(permitDetails);

	}

	private List<String> validationForScrtRenewalOfPermit(RegistrationDetailsDTO registrationOptional,
			List<String> errors) {
		this.validationForScrtCov(registrationOptional, errors);
		Optional<PermitDetailsDTO> dto = permitDetailsDAO.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(
				registrationOptional.getPrNo(), PermitType.PRIMARY.getPermitTypeCode(),
				PermitsEnum.ACTIVE.getDescription());
		if (!dto.isPresent()) {
			errors.add("Permit details not found for pr number: " + registrationOptional.getPrNo());
			return errors;
		}
		if (dto.get().getPermitValidityDetails() == null
				|| dto.get().getPermitValidityDetails().getPermitValidTo() == null) {
			errors.add("Permit validitys not found for pr number: " + registrationOptional.getPrNo());
		}
		if (dto.get().getPermitValidityDetails().getPermitValidTo().isAfter(LocalDate.now())) {
			errors.add("Please renewal permit after expire: " + registrationOptional.getPrNo());
		}
		// this.validationForScrtCov(registrationOptional, errors);
		return errors;
	}

	private List<String> validationForScrtReplacementOfVehicle(RegistrationDetailsDTO registrationOptional,
			List<String> errors) {
		Optional<PermitDetailsDTO> dto = permitDetailsDAO.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(
				registrationOptional.getPrNo(), PermitType.PRIMARY.getPermitTypeCode(),
				PermitsEnum.ACTIVE.getDescription());
		if (dto.isPresent()) {
			errors.add("This vehicle have pucca permit. Plesse surrender the permit for pr number: "
					+ registrationOptional.getPrNo());
		}
		this.validationForScrtCov(registrationOptional, errors);
		return errors;
	}

	private List<String> validationForScrtCov(RegistrationDetailsDTO registrationOptional, List<String> errors) {
		if (!registrationOptional.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.SCRT.getCovCode())) {
			errors.add("Please alter the vehicle to Stage Carriage: " + registrationOptional.getPrNo());
		}
		return errors;
	}

	@Override
	public CitizenFeeDetailsInput getPaymentInputs(TransactionDetailVO regServiceDetail) {

		CitizenFeeDetailsInput input = new CitizenFeeDetailsInput();
		input.setCovDetails(regServiceDetail.getCovs());
		input.setServiceEnums(regServiceDetail.getServiceEnumList());
		input.setWeightDetails(regServiceDetail.getWeightType());
		input.setRequestForPay(regServiceDetail.isRequestToPay());
		input.setTaxType(regServiceDetail.getTaxType());
		input.setCalculateFc(regServiceDetail.isCalculateFc());
		input.setIsApplicationFromMvi(regServiceDetail.isRtoRejectedIvcn());
		input.setIsChassesVehicle(regServiceDetail.isChassesVehicle());
		input.setOfficeCode(regServiceDetail.getOfficeCode());
		input.setApplicationNo(regServiceDetail.getFormNumber());
		input.setIsOtherState(regServiceDetail.isOtherState());
		input.setRegApplicationNo(regServiceDetail.getRegApplicationNo());
		input.setPermitType(regServiceDetail.getPermitType());
		input.setSlotDateForPayments(regServiceDetail.getSlotDate());
		input.setSeatingCapacity(regServiceDetail.getSeatingCapacity());
		input.setRouteCode(regServiceDetail.getRouteCode());
		input.setIsweightAlt(regServiceDetail.getIsWeightAlt());
		input.setPurpose(regServiceDetail.getPurpose());
		input.setListOfVcrs(regServiceDetail.getListOfVcrs());
		input.setVoluntyInputs(regServiceDetail.getInput());
		input.setSkipTaxForTPSP(regServiceDetail.isSkipTaxForTPSP());
		return input;
	}

	public Trackingvo fetchStatusFromRegistrationServices(ApplicationSearchVO applicationSearchVO) {
		Trackingvo trackingvo = new Trackingvo();
		Optional<RegServiceDTO> regServiceDTO = Optional.empty();

		RegServiceDTO regServiceDetailsDTO = null;
		Optional<RegistrationDetailsDTO> registrationDetailsDTO = Optional.empty();

		if (StringUtils.isNotEmpty(applicationSearchVO.getApplicationNo())) {
			regServiceDTO = regServiceDAO.findByApplicationNo(applicationSearchVO.getApplicationNo());
		}

		if ((StringUtils.isNotEmpty(applicationSearchVO.getPrNo()))) {
			regServiceDTO = regServiceDAO.findByPrNoOrderByCreatedDateDesc(applicationSearchVO.getPrNo());
		}

		if (regServiceDTO.isPresent()) {
			regServiceDetailsDTO = regServiceDTO.get();
		} else if (!regServiceDTO.isPresent()) {
			if (StringUtils.isNotEmpty(applicationSearchVO.getApplicationNo())) {
				registrationDetailsDTO = registrationDetailDAO
						.findByApplicationNo(applicationSearchVO.getApplicationNo());
			}
			if ((StringUtils.isNotEmpty(applicationSearchVO.getPrNo()))) {
				registrationDetailsDTO = registrationDetailDAO.findByPrNo(applicationSearchVO.getPrNo());
			}
			if (!registrationDetailsDTO.isPresent()) {
				throw new BadRequestException("NO RECORD FOUND");
			}
			RegistrationDetailsDTO optionalregistrationDetailsDTO = registrationDetailsDTO.get();
			if ((StringUtils.isNotEmpty(registrationDetailsDTO.get().getSource()))) {
				throw new BadRequestException("please apply any online transactions");
			}
			if (optionalregistrationDetailsDTO.getServiceIds().contains(ServiceEnum.FR.getId())) {
				if (CovCategory.N.toString().equals(optionalregistrationDetailsDTO.getVehicleType())) {
					MasterCovDTO masterDto = masterCovDAO
							.findByCovcode(optionalregistrationDetailsDTO.getClassOfVehicle());
					if (masterDto.isRequireCard()) {
						return findCardStatus(optionalregistrationDetailsDTO, trackingvo);
					} else {
						throw new BadRequestException("please check through application status");
					}

				} else {
					throw new BadRequestException("please check through application status");
				}
			} else {
				if (CovCategory.N.toString().equals(optionalregistrationDetailsDTO.getVehicleType())) {
					MasterCovDTO masterDto = masterCovDAO
							.findByCovcode(optionalregistrationDetailsDTO.getClassOfVehicle());
					if (masterDto.isRequireCard()) {
						return findCardStatus(optionalregistrationDetailsDTO, trackingvo);
					} else {
						throw new BadRequestException("please check through application status");
					}
				} else {
					throw new BadRequestException("please check through application status");
				}
			}
		}
		Iterator<Integer> iterator = regServiceDetailsDTO.getServiceIds().iterator();
		boolean b = false;
		while (iterator.hasNext()) {
			if ((ServiceEnum.getDispatchedRegistrationServices().contains(iterator.next()))) {
				b = true;
				break;
			}
		}

		/*
		 * if(!regServiceDetailsDTO.getServiceIds().stream().anyMatch(val ->
		 * ServiceEnum.getDispatchedRegistrationServices().contains(val))) { throw new
		 * BadRequestException("PLEASE CHECK YOUR STATUS THROUGH APPLICATION STATUS"); }
		 */

		if ((!b) || (CovCategory.T.toString().equals(regServiceDetailsDTO.getRegistrationDetails().getVehicleType()))) {
			throw new BadRequestException("PLEASE CHECK YOUR STATUS THROUGH APPLICATION STATUS");
		} else {
			findingApplicationStatus(regServiceDetailsDTO, trackingvo);
		}
		return trackingvo;
	}

	public Trackingvo findingStatus(RegServiceDTO regServiceDetailsDTO, Trackingvo trackingvo) {

		if (((regServiceDetailsDTO.getServiceIds().contains(ServiceEnum.HIREPURCHASETERMINATION.getId()))
				&& (regServiceDetailsDTO.getServiceIds().contains(ServiceEnum.HPA.getId())))) {
			if ((regServiceDetailsDTO.getIsHPTDone()) && (regServiceDetailsDTO.getIsHPADone())) {
				return trackingvo;
			} else {
				trackingvo.setStatus(StatusRegistration.DispatchStatusEnum.FINANCIER.getDesc());
				return trackingvo;
			}
		}
		if ((regServiceDetailsDTO.getServiceIds().contains(ServiceEnum.HIREPURCHASETERMINATION.getId()))) {
			if (!regServiceDetailsDTO.getIsHPTDone()) {
				trackingvo.setStatus(StatusRegistration.DispatchStatusEnum.FINANCIER.getDesc());
				return trackingvo;
			} else if (regServiceDetailsDTO.getApplicationStatus().equals(StatusRegistration.PAYMENTDONE)) {
				trackingvo.setStatus(StatusRegistration.DispatchStatusEnum.APPROVED.getDesc());
				return trackingvo;
			}
		}
		if ((regServiceDetailsDTO.getServiceIds().contains(ServiceEnum.HPA.getId()))) {
			if ((!regServiceDetailsDTO.getIsHPADone())) {
				trackingvo.setStatus(StatusRegistration.DispatchStatusEnum.FINANCIER.getDesc());
				return trackingvo;
			} else if (regServiceDetailsDTO.getApplicationStatus().equals(StatusRegistration.PAYMENTDONE)) {
				trackingvo.setStatus(StatusRegistration.DispatchStatusEnum.APPROVED.getDesc());
				return trackingvo;
			}
		}

		return trackingvo;
	}

	public Trackingvo findingApplicationStatus(RegServiceDTO regServiceDetailsDTO, Trackingvo trackingvo) {

		if (((regServiceDetailsDTO.getApplicationStatus()).equals(StatusRegistration.INITIATED)
				|| (regServiceDetailsDTO.getApplicationStatus()).equals(StatusRegistration.CITIZENPAYMENTFAILED)
				|| (regServiceDetailsDTO.getApplicationStatus()).equals(StatusRegistration.PAYMENTINITIATED)
				|| (regServiceDetailsDTO.getApplicationStatus()).equals(StatusRegistration.PAYMENTPENDING))) {
			trackingvo.setStatus(StatusRegistration.DispatchStatusEnum.PAYMENT.getDesc());
			return trackingvo;
		}
		trackingvo = findingStatus(regServiceDetailsDTO, trackingvo);
		if (StringUtils.isNotEmpty(trackingvo.getStatus())) {
			return trackingvo;
		}
		if ((regServiceDetailsDTO.getApplicationStatus()).equals(StatusRegistration.PAYMENTDONE)) {
			trackingvo.setStatus(StatusRegistration.DispatchStatusEnum.APPROVED.getDesc());
			return trackingvo;
		}
		if ((regServiceDetailsDTO.getApplicationStatus()).equals(StatusRegistration.SELLERCOMPLETED)) {
			trackingvo.setStatus(StatusRegistration.DispatchStatusEnum.BUYER.getDesc());
			return trackingvo;
		}
		if ((regServiceDetailsDTO.getApplicationStatus()).equals(StatusRegistration.CANCELED)) {
			trackingvo.setStatus(StatusRegistration.DispatchStatusEnum.CANCELLED.getDesc());
			return trackingvo;
		}

		if ((regServiceDetailsDTO.getApplicationStatus()).equals(StatusRegistration.APPROVED)) {
			if (((regServiceDetailsDTO.getServiceIds().contains(ServiceEnum.THEFTINTIMATION.getId()))
					|| (regServiceDetailsDTO.getServiceIds().contains(ServiceEnum.THEFTREVOCATION.getId()))
					|| (regServiceDetailsDTO.getServiceIds().contains(ServiceEnum.RCCANCELLATION.getId()))
					|| (regServiceDetailsDTO.getServiceIds().contains(ServiceEnum.VEHICLESTOPPAGE.getId()))
					|| (regServiceDetailsDTO.getServiceIds().contains(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId()))
					|| (regServiceDetailsDTO.getServiceIds().contains(ServiceEnum.ISSUEOFNOC.getId()))
					|| (regServiceDetailsDTO.getServiceIds().contains(ServiceEnum.CANCELLATIONOFNOC.getId())))) {
				trackingvo.setStatus(regServiceDetailsDTO.getApplicationStatus().toString());
				return trackingvo;
			}
			MasterCovDTO masterDto = masterCovDAO
					.findByCovcode(regServiceDetailsDTO.getRegistrationDetails().getClassOfVehicle());
			if (masterDto != null && masterDto.isRequireCard()) {
				Optional<RegistrationDetailsDTO> registrationDetailsDTO = null;
				if (regServiceDetailsDTO.getPrNo() != null) {
					registrationDetailsDTO = registrationDetailDAO.findByPrNo(regServiceDetailsDTO.getPrNo());

				}
				if (!registrationDetailsDTO.isPresent()) {
					throw new BadRequestException("NO RECORD FOUND");
				}
				RegistrationDetailsDTO optionalregistrationDetailsDTO = registrationDetailsDTO.get();
				return findCardStatus(optionalregistrationDetailsDTO, trackingvo);

			} else {
				trackingvo.setStatus(regServiceDetailsDTO.getApplicationStatus().getDescription());
				return trackingvo;
			}

		} else {
			trackingvo.setStatus(regServiceDetailsDTO.getApplicationStatus().getDescription());
			return trackingvo;
		}

	}

	public Trackingvo findCardStatus(RegistrationDetailsDTO optionalregistrationDetailsDTO, Trackingvo trackingvo) {
		Optional<DispatcherSubmissionDTO> dispatcherDetailsDTO = dispatcherDAO
				.findByPrNoOrderByCreatedDateDesc(optionalregistrationDetailsDTO.getPrNo());
		if ((optionalregistrationDetailsDTO.isCardPrinted()) && (!optionalregistrationDetailsDTO.isCardDispatched())) {
			trackingvo.setStatus(StatusRegistration.DispatchStatusEnum.CARDPRINTED_NODISPATCHED.getDesc());
			trackingvo.setPrintedDate(optionalregistrationDetailsDTO.getCardPrintedDate().toLocalDate());
			return trackingvo;
		}

		if (!(optionalregistrationDetailsDTO.isCardPrinted())) {
			trackingvo.setStatus(StatusRegistration.DispatchStatusEnum.NOCARDPRINTED.getDesc());

			return trackingvo;
		}
		if (optionalregistrationDetailsDTO.getDispatcherFormSubmissionDTO() != null) {

			if (optionalregistrationDetailsDTO.isCardDispatched()) {
				if (!dispatcherDetailsDTO.isPresent()) {
					throw new BadRequestException("CARD DETAILS NOT FOUND");
				}
				DispatcherSubmissionDTO optionaldispatcherDetailsDTO = dispatcherDetailsDTO.get();
				trackingvo.setStatus(StatusRegistration.DispatchStatusEnum.CARDPRINTED.getDesc());
				trackingvo.setPostedDate(optionaldispatcherDetailsDTO.getPostedDate().toLocalDate());
				trackingvo.setEmsNo(optionaldispatcherDetailsDTO.getEmsNumber());

				if (optionalregistrationDetailsDTO.getCardPrintedDate() != null) {
					trackingvo.setPrintedDate(optionalregistrationDetailsDTO.getCardPrintedDate().toLocalDate());
				}
				trackingvo.setRemarks(optionaldispatcherDetailsDTO.getRemarks());

				if (dispatcherDetailsDTO.get().getReturnDate() != null
						&& (!optionaldispatcherDetailsDTO.getReturnReason().isEmpty())) {
					trackingvo.setReturnedDate(optionaldispatcherDetailsDTO.getReturnDate());
					trackingvo.setReturnRemarks(optionaldispatcherDetailsDTO.getReturnReason());
				}

				return trackingvo;
			}

		}

		return trackingvo;
	}

	private boolean skipAadharForAuction(String token, boolean skipAadharCheck, Set<Integer> serviceIds,
			OwnerType ownerType, TransferType transferType, String prNo) {
		if (serviceIds.stream().anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId()))) {
			if (ownerType == null) {
				logger.error("Please select owner type. [{}] ", prNo);
				throw new BadRequestException("Please select owner type: " + prNo);
			}
			if (transferType == null) {
				logger.error("Please select transfer type. [{}] ", prNo);
				throw new BadRequestException("Please select transfer type: " + prNo);
			}
			if (ownerType.equals(OwnerType.BUYER) && transferType.equals(TransferType.AUCTION)) {
				if (serviceIds.size() > 1) {
					logger.error("combinations services not allow for auction case. [{}] ", prNo);
					throw new BadRequestException("combinations services not allow for auction case: " + prNo);
				}

				List<AuctionDetailsDTO> listOfAucton = auctionDetailsDAO.findByVehicleDetailsPrNoIn(prNo);
				if (listOfAucton == null || listOfAucton.isEmpty()) {
					logger.error("Auction details not found for [{}] ", prNo);
					throw new BadRequestException("Auction details not found for: " + prNo);
				}
				listOfAucton.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
				AuctionDetailsDTO auctionDto = listOfAucton.stream().findFirst().get();
				if (!auctionDto.isAuctionClosed()) {
					logger.error("MVI need to close all vehicles in auction [{}] ", prNo);
					throw new BadRequestException("MVI need to close all vehicles in auction: " + prNo);
				}
				if (!auctionDto.isDtcCompleted()) {
					logger.error("DTC approval pending [{}] ", prNo);
					throw new BadRequestException("DTC approval pending: " + prNo);
				}
				AuctionVehicleDetailsDTO vehicleDto = auctionDto.getVehicleDetails().stream()
						.filter(vehicle -> vehicle.getPrNo() != null && vehicle.getPrNo().equalsIgnoreCase(prNo))
						.collect(Collectors.toList()).stream().findFirst().get();
				if (vehicleDto.getOtherState() != null && vehicleDto.getOtherState()) {
					logger.error("Vehicle is under other state.You should cancel the RC   [{}] ", prNo);
					throw new BadRequestException("Vehicle is under other state.You should cancel the RC: " + prNo);
				}
				if (!(vehicleDto.getVehicleCondition().equals(TransferType.vehicleCondition.ROADWORTHY)
						&& vehicleDto.getGenuiness().equals(TransferType.genuiness.GENUINE))) {
					logger.error("Vehicle is not road worthy or genuine.Please cancel the RC   [{}] ", prNo);
					throw new BadRequestException(
							"Vehicle is not road worthy or genuine.Please cancel the RC: " + prNo);
				}
				if (vehicleDto.getTowDone() != null && vehicleDto.getTowDone()) {
					logger.error("Transfer of Ownership completed   [{}] ", prNo);
					throw new BadRequestException("Transfer of Ownership completed: " + prNo);
				}
				if (StringUtils.isBlank(vehicleDto.getToken())) {
					logger.error("Token not generated at MVI level please check with MVI officer   [{}] ", prNo);
					throw new BadRequestException(
							"Token not generated at MVI level please check with MVI officer: " + prNo);
				}
				if (StringUtils.isBlank(token)) {
					logger.error("Please enter token which is give by MVI officer   [{}] ", prNo);
					throw new BadRequestException("Please enter token which is give by MVI officer: " + prNo);
				}
				if (!token.equalsIgnoreCase(vehicleDto.getToken())) {
					logger.error("Please enter valid token    [{}] ", prNo);
					throw new BadRequestException("Please enter valid token: " + prNo);
				}
				skipAadharCheck = Boolean.FALSE;

			}

		}
		return skipAadharCheck;
	}

	@Override
	public List<Integer> scrtServices() {
		List<Integer> list = new ArrayList<>();
		list.add(ServiceEnum.STAGECARRIAGERENEWALOFPERMIT.getId());
		list.add(ServiceEnum.STAGECARRIAGEREPLACEMENTOFVEHICLE.getId());
		return list;
	}

	private void saveInvalidInputDetails(String regServiceVO, JwtUser jwtUser) {
		try {
			ServicesInvalidDetailsDTO dto = new ServicesInvalidDetailsDTO();
			dto.setInvalidJson(regServiceVO);
			if (jwtUser != null) {
				dto.setCreatedBy(jwtUser.getId());
			}
			dto.setCreatedDate(LocalDateTime.now());
			servicesInvalidDetailsDAO.save(dto);
		} catch (Exception e) {
			logger.debug("Problem occured while saving the invalid input details");
		}

	}

	@Override
	public RegServiceDTO returnLatestFcDoc(String prNo) {
		List<ServiceEnum> list = new ArrayList<>();
		list.add(ServiceEnum.TAXATION);
		list.add(ServiceEnum.VCR);
		List<RegServiceDTO> regList = regServiceDAO.findByPrNoAndServiceTypeNotInAndSourceIsNull(prNo, list);

		if (regList == null || regList.isEmpty()) {
			logger.error("No record found for [{}] ", prNo);
			throw new BadRequestException("No record found.Pr no: " + prNo);
		}

		regList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		return regList.stream().findFirst().get();
	}

	private void calculateEibtVehicleAge(RegistrationDetailsDTO registrationOptional, List<String> errors) {

		double vehicleAge = 0d;
		if (registrationOptional.getRegistrationValidity() != null
				&& registrationOptional.getRegistrationValidity().getPrGeneratedDate() != null) {
			vehicleAge = citizenTaxService.calculateAgeOfTheVehicle(
					registrationOptional.getRegistrationValidity().getPrGeneratedDate(), LocalDate.now());

		} else if (registrationOptional.getPrGeneratedDate() != null) {
			vehicleAge = citizenTaxService
					.calculateAgeOfTheVehicle(registrationOptional.getPrGeneratedDate().toLocalDate(), LocalDate.now());
		}
		if (vehicleAge > 15) {
			logger.error("Vehicle age is more than 15 years. Please alter the vehicle to ominibus:"
					+ registrationOptional.getPrNo());
			errors.add("Vehicle age is more than 15 years. Please alter the vehicle to ominibus:"
					+ registrationOptional.getPrNo());
		}

	}

	@Override
	public void setTaxForPrint(VcrFinalServiceVO vo, TaxHelper taxAndValidity) {
		double total = 0d;
		if (taxAndValidity != null) {
			if (taxAndValidity.getTaxAmountForPayments() != null && taxAndValidity.getTaxAmountForPayments() > 0) {
				total = total + taxAndValidity.getTaxAmountForPayments().doubleValue();
				vo.setTax(taxAndValidity.getTaxAmountForPayments().doubleValue());
				// taxType = taxAndValidity.getTaxName();

			}
			if (taxAndValidity.getPenalty() != null && taxAndValidity.getPenalty() > 0) {
				total = total + taxAndValidity.getPenalty().doubleValue();
				vo.setPenalty(taxAndValidity.getPenalty());
			}
			if (taxAndValidity.getPenaltyArrears() != null && taxAndValidity.getPenaltyArrears() > 0) {
				total = total + taxAndValidity.getPenaltyArrears().doubleValue();
				vo.setPenaltyArrears(taxAndValidity.getPenaltyArrears());
			}
			if (taxAndValidity.getTaxArrearsRound() != null && taxAndValidity.getTaxArrearsRound() > 0) {
				total = total + taxAndValidity.getTaxArrearsRound().doubleValue();
				vo.setTaxArrears(taxAndValidity.getTaxArrearsRound().doubleValue());
			}
			if (vo.getCompoundFee() != null && vo.getCompoundFee() > 0) {
				total = total + vo.getCompoundFee().doubleValue();
			}
			vo.setTotal(total);
			/*
			 * if (taxAndValidity.getQuaterTaxForNewGo() != null &&
			 * taxAndValidity.getQuaterTaxForNewGo() != 0) { quaterTaxForNewGo =
			 * taxAndValidity.getQuaterTaxForNewGo(); }
			 */
		}
	}

	public void setTaxForPrint(VcrFinalServiceVO dto) {
		double total = 0d;

		if (dto.getTax() != null && dto.getTax() > 0) {
			total = total + dto.getTax().doubleValue();

		}
		if (dto.getPenalty() != null && dto.getPenalty() > 0) {
			total = total + dto.getPenalty().doubleValue();
		}
		if (dto.getPenaltyArrears() != null && dto.getPenaltyArrears() > 0) {
			total = total + dto.getPenaltyArrears().doubleValue();
		}
		if (dto.getTaxArrears() != null && dto.getTaxArrears() > 0) {
			total = total + dto.getTaxArrears().doubleValue();
		}
		if (dto.getOffencetotal() != null && dto.getOffencetotal() > 0) {
			total = total + dto.getOffencetotal().doubleValue();
		}
		dto.setTotal(total);
		/*
		 * if (taxAndValidity.getQuaterTaxForNewGo() != null &&
		 * taxAndValidity.getQuaterTaxForNewGo() != 0) { quaterTaxForNewGo =
		 * taxAndValidity.getQuaterTaxForNewGo(); }
		 */

	}

	private void checkMviNameForStoppage(RegServiceDTO dto, JwtUser jwtUser) {
		if (!dto.getLockedDetails().stream()
				.anyMatch(one -> one.getLockedBy().equalsIgnoreCase(jwtUser.getUsername()))) {
			logger.error("Unauthorized user of application No [{}]", dto.getApplicationNo());
			throw new BadRequestException("Unauthorized User");
		}
	}

	@Override
	public Pair<Boolean, String> payReceiptDownloadAtCitizen(String applicationNo) {

		List<String> payStatusList = Arrays.asList(StatusRegistration.PAYMENTFAILED.getDescription(),
				StatusRegistration.PAYMENTPENDING.getDescription(),
				StatusRegistration.PAYMENTINITIATED.getDescription(), StatusRegistration.INITIATED.getDescription());

		Optional<RegServiceDTO> regServiceDTOOpt = regServiceDAO
				.findByApplicationNoAndApplicationStatusNotIn(applicationNo, payStatusList);

		if (regServiceDTOOpt.isPresent() && regServiceDTOOpt != null) {

			return Pair.of(Boolean.TRUE, applicationNo);
		}

		else {
			throw new BadRequestException(
					"Payment not completed for your application number.so unble to download pay receipt .please complete the payments");
		}

	}

	private boolean getOpenedVcrs(VcrFinalServiceDTO vcdDoc) {
		List<VcrFinalServiceDTO> vcrList = null;

		if (vcdDoc != null && vcdDoc.getRegistration() != null) {
			if (StringUtils.isNoneBlank(vcdDoc.getRegistration().getRegNo())) {
				vcrList = vcrFinalServiceDAO
						.findByRegistrationRegNoAndIsVcrClosedIsFalse(vcdDoc.getRegistration().getRegNo());
			} else if (StringUtils.isNoneBlank(vcdDoc.getRegistration().getTrNo())) {
				vcrList = vcrFinalServiceDAO
						.findByRegistrationTrNoAndIsVcrClosedIsFalse(vcdDoc.getRegistration().getTrNo());
			} else if (StringUtils.isNoneBlank(vcdDoc.getRegistration().getChassisNumber())) {
				vcrList = vcrFinalServiceDAO.findByRegistrationChassisNumberAndIsVcrClosedIsFalse(
						vcdDoc.getRegistration().getChassisNumber());
			}
			if (vcrList != null && !vcrList.isEmpty()) {
				List<VcrFinalServiceDTO> vcrWithOutCahs = vcrList
						.stream().filter(paymentDone -> paymentDone.getPaymentType() == null || !paymentDone
								.getPaymentType().equalsIgnoreCase(GatewayTypeEnum.CASH.getDescription()))
						.collect(Collectors.toList());

				if (vcrWithOutCahs != null && !vcrWithOutCahs.isEmpty()) {
					List<VcrFinalServiceDTO> unpaidVcrs = vcrWithOutCahs.stream()
							.filter(vcrDto -> vcrDto.getPartiallyClosed() == null || !vcrDto.getPartiallyClosed())
							.collect(Collectors.toList());

					if (unpaidVcrs != null && !unpaidVcrs.isEmpty()) {
						if (unpaidVcrs.size() >= 2) {
							return true;
						}
					}
				}
			}

		}
		return false;
	}

	@Override
	public Optional<CitizenApplicationSearchResponceVO> getVehicleDetailsByPrNoAndAadaharNoForExternalUser(
			ApplicationSearchVO applicationSearchVO) {
		if (StringUtils.isBlank(applicationSearchVO.getPrNo())
				&& StringUtils.isBlank(applicationSearchVO.getAadharNo())) {
			throw new BadRequestException("Required parameters are missing");
		}
		Optional<RegistrationDetailsDTO> regDoc = registrationDetailDAO.findByPrNo(applicationSearchVO.getPrNo());
		if (!regDoc.isPresent()) {
			throw new BadRequestException("Record not found with this Vehicle Number " + applicationSearchVO.getPrNo());
		}
		CitizenApplicationSearchResponceVO vo = new CitizenApplicationSearchResponceVO();

		if (regDoc.get().getOwnerType() != null && !regDoc.get().getOwnerType().equals(OwnerTypeEnum.Individual)) {
			throw new BadRequestException("Owner type " + "'" + regDoc.get().getOwnerType() + "'"
					+ " is not eligible to apply for this service");
		}

		if (regDoc.get().getApplicantDetails().getIsAadhaarValidated() != null
				&& regDoc.get().getApplicantDetails().getIsAadhaarValidated()) {
			if (!applicationSearchVO.getAadharNo().equalsIgnoreCase(regDoc.get().getApplicantDetails().getAadharNo())) {
				throw new BadRequestException("Record not found with this Vehicle Number "
						+ applicationSearchVO.getPrNo() + " Aadhar Number " + applicationSearchVO.getAadharNo());
			}
			vo.setIsAadharValidated(Boolean.TRUE);
		}
		String cov = StringUtils.isNotBlank(regDoc.get().getClassOfVehicle()) ? regDoc.get().getClassOfVehicle()
				: regDoc.get().getVehicleDetails().getClassOfVehicle();
		List<String> covsList = Arrays.asList("ARKT", "MAXT", "LTCT", "MTLT", "STCT");

		if (!covsList.contains(cov)) {
			throw new BadRequestException(cov + " is not eligible to apply for this service");
		}
		Optional<CitizenApplicationSearchResponceVO> dataVO = getApplicationSearchResultForCitizen(applicationSearchVO);
		vo.setRegistraionNumber(dataVO.get().getRegistraionNumber());
		vo.setOwnerName(dataVO.get().getOwnerName());
		vo.setFatherName(StringUtils.isNotBlank(regDoc.get().getApplicantDetails().getFatherName())
				? regDoc.get().getApplicantDetails().getFatherName()
				: StringUtils.EMPTY);
		vo.setPresentAddress(addressMapper.convertEntity(regDoc.get().getApplicantDetails().getPresentAddress()));
		vo.setClassOfVehicle(dataVO.get().getClassOfVehicle());
		vo.setTaxValidUpto(dataVO.get().getTaxValidUpto() != null ? dataVO.get().getTaxValidUpto() : null);
		if (dataVO.get().getFcValidUpto() != null) {
			vo.setFcValidUpto(dataVO.get().getFcValidUpto());
			vo.setRegistrationValidUpto(dataVO.get().getFcValidUpto());
		}
		vo.setPermitValidUpto(dataVO.get().getPermitValidUpto() != null ? dataVO.get().getPermitValidUpto() : null);
		if (dataVO.get().getRegistrationValidUpto() != null && regDoc.get().getRegistrationValidity() != null
				&& regDoc.get().getRegistrationValidity().getRegistrationValidity() != null) {
			vo.setRegistrationValidUpto(regDoc.get().getRegistrationValidity().getRegistrationValidity().toLocalDate());
		}
		return Optional.of(vo);
	}

	private void checkAitpTaxPaidORNot(RegServiceDTO dto, VoluntaryTaxVO vo) {
		if (dto.getVoluntaryTaxDetails() != null && StringUtils.isNoneBlank(dto.getVoluntaryTaxDetails().getTaxType())
				&& dto.getVoluntaryTaxDetails().getTaxType()
						.equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.Quarterly.getDesc())
				&& dto.getVoluntaryTaxDetails().getTaxvalidUpto() != null
				&& StringUtils.isNoneBlank(dto.getVoluntaryTaxDetails().getClassOfVehicle())) {
			LocalDate currentTaxValidity = citizenTaxService.validity(TaxTypeEnum.VoluntaryTaxType.Quarterly.getDesc());
			Optional<PropertiesDTO> properties = propertiesDAO
					.findByAllowNextQuarterTaxCovsInAndAllowNextQuarterTaxTrue(
							dto.getVoluntaryTaxDetails().getClassOfVehicle());
			if (properties.isPresent()) {
				if ((vo.getAllowNextQuarterTax() == null || !vo.getAllowNextQuarterTax())
						&& (currentTaxValidity.isBefore(dto.getVoluntaryTaxDetails().getTaxvalidUpto())
								|| currentTaxValidity.equals(dto.getVoluntaryTaxDetails().getTaxvalidUpto()))) {
					logger.error("Tax already paid for " + dto.getVoluntaryTaxDetails().getRegNo()
							+ " and validity is: " + dto.getVoluntaryTaxDetails().getTaxvalidUpto());
					throw new BadRequestException("Tax already paid for " + dto.getVoluntaryTaxDetails().getRegNo()
							+ " and validity is: " + dto.getVoluntaryTaxDetails().getTaxvalidUpto());
				} else if (vo.getAllowNextQuarterTax() != null && vo.getAllowNextQuarterTax()) {
					// LocalDate currentTaxValidity =
					// citizenTaxService.validity(TaxTypeEnum.VoluntaryTaxType.Quarterly.getDesc());
					Pair<Integer, Integer> monthPosition = citizenTaxService
							.getMonthposition(currentTaxValidity.plusDays(1));
					LocalDate nextQuarterValidity = citizenTaxService.calculateChassisTaxUpTo(monthPosition.getFirst(),
							monthPosition.getSecond(), currentTaxValidity.plusDays(1));
					if (!currentTaxValidity.isBefore(dto.getVoluntaryTaxDetails().getTaxvalidUpto())) {
						logger.error("Please pay current quaretr tax  for " + dto.getVoluntaryTaxDetails().getRegNo());
						throw new BadRequestException(
								"Please pay current quaretr tax  for " + dto.getVoluntaryTaxDetails().getRegNo());
					}
					if (nextQuarterValidity.equals(dto.getVoluntaryTaxDetails().getTaxvalidUpto())
							|| nextQuarterValidity.isBefore(dto.getVoluntaryTaxDetails().getTaxvalidUpto())) {
						logger.error("Already paid  next quarter tax for " + dto.getVoluntaryTaxDetails().getRegNo()
								+ " and validity is: " + dto.getVoluntaryTaxDetails().getTaxvalidUpto());
						throw new BadRequestException(
								"Already paid  next quarter tax for " + dto.getVoluntaryTaxDetails().getRegNo()
										+ " and validity is: " + dto.getVoluntaryTaxDetails().getTaxvalidUpto());
					}
				}
			}
		}
	}

	@Override
	public Optional<VoluntaryTaxVO> getVoluntaryTaxDetails(ApplicationSearchVO applicationSearchVO,
			HttpServletRequest request) {
		if (!StringUtils.isEmpty(applicationSearchVO.getPrNo())) {
			Optional<VoluntaryTaxDTO> voluntaryTax = voluntaryTaxDAO
					.findByRegNoOrderByCreatedDateDesc(applicationSearchVO.getPrNo());
			if (!voluntaryTax.isPresent()) {
				throw new BadRequestException("No record found");
			}
			if (voluntaryTax.isPresent()) {
				this.voluntaryTaxValidations(voluntaryTax.get());
				return voluntaryTaxMapper.convertEntity(voluntaryTax);
			}
		}
		if (!StringUtils.isEmpty(applicationSearchVO.getTrNo())) {
			Optional<VoluntaryTaxDTO> voluntaryTax = voluntaryTaxDAO
					.findByTrNoOrderByCreatedDateDesc(applicationSearchVO.getTrNo());
			if (!voluntaryTax.isPresent()) {
				throw new BadRequestException("No record found");
			}
			if (voluntaryTax.isPresent()) {
				this.voluntaryTaxValidations(voluntaryTax.get());
				return voluntaryTaxMapper.convertEntity(voluntaryTax);
			}
		}
		if (!StringUtils.isEmpty(applicationSearchVO.getChassisNo())) {
			Optional<VoluntaryTaxDTO> voluntaryTax = voluntaryTaxDAO
					.findByChassisNoOrderByCreatedDateDesc(applicationSearchVO.getChassisNo());
			if (voluntaryTax.isPresent()) {
				return voluntaryTaxMapper.convertEntity(voluntaryTax);
			}

		}
		if (!StringUtils.isEmpty(applicationSearchVO.getAadharNo())) {

		}

		return null;
	}

	private Optional<VoluntaryTaxDTO> voluntaryTaxValidations(VoluntaryTaxDTO voluntaryTax) {
		if (voluntaryTax.getTaxType().equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.Quarterly.getDesc())
				|| voluntaryTax.getTaxType().equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.LifeTax.getDesc())
				|| voluntaryTax.getTaxType().equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.Halfyearly.getDesc())
				|| voluntaryTax.getTaxType().equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.Annual.getDesc())) {
			if (voluntaryTax.getClassOfVehicle() != null && (voluntaryTax.getClassOfVehicle()
					.equalsIgnoreCase(ClassOfVehicleEnum.MTLT.getCovCode())
					|| voluntaryTax.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.LTCT.getCovCode())
					|| voluntaryTax.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.STCT.getCovCode()))) {
				if (!voluntaryTax.getTaxType().equalsIgnoreCase(TaxTypeEnum.VoluntaryTaxType.LifeTax.getDesc())) {
					throw new BadRequestException("Citizen has paid tax for" + "   " + voluntaryTax.getTaxType()
							+ " only . Hence, the vehicle is not eligible for tax exemption.");
				}
			}
			if (voluntaryTax.getTaxvalidUpto().isBefore(LocalDate.now())) {
				throw new BadRequestException(
						"Your voluntary tax is expire for this prNo/TrNo: " + voluntaryTax.getRegNo()
								+ voluntaryTax.getTrNo() + " and validity is: " + voluntaryTax.getTaxvalidUpto());
			}
		} else {
			throw new BadRequestException("Citizen has paid tax for" + "   " + voluntaryTax.getTaxType()
					+ " only . Hence, the vehicle is not eligible for tax exemption.");
		}
		return Optional.ofNullable(voluntaryTax);

	}

	@Override
	public Optional<PoliceDepartmentSearchResponceVO> vehicleDetailsSearchWithRegistrationNumber(
			ApplicationSearchVO applicationSearchVO) {
		Optional<RegistrationDetailsDTO> regDoc = null;

		if (StringUtils.isNotBlank(applicationSearchVO.getPrNo())) {
			regDoc = registrationDetailDAO.findByPrNo(applicationSearchVO.getPrNo().toUpperCase());
		} else if (StringUtils.isNotBlank(applicationSearchVO.getEngineNo())
				&& StringUtils.isNotBlank(applicationSearchVO.getChassisNo())) {
			regDoc = registrationDetailDAO
					.findTopByVahanDetailsChassisNumberAndVahanDetailsEngineNumberOrderByLUpdateDesc(
							applicationSearchVO.getChassisNo().toUpperCase(),
							applicationSearchVO.getEngineNo().toUpperCase());
		} else if (StringUtils.isNotBlank(applicationSearchVO.getTrNo())) {
			regDoc = registrationDetailDAO.findByTrNo(applicationSearchVO.getTrNo());
			if (!regDoc.isPresent()) {
				Optional<StagingRegistrationDetailsDTO> stagingDTO = stagingRegistrationDetailsDAO
						.findByTrNo(applicationSearchVO.getTrNo());
				if (stagingDTO.isPresent()) {
					regDoc = Optional.of((RegistrationDetailsDTO) stagingDTO.get());
				}
			}
		} else {
			throw new BadRequestException("Required parameters are missing");
		}

		if (!regDoc.isPresent()) {
			throw new BadRequestException("Record not found with this Vehicle Number " + applicationSearchVO.getPrNo());
		}
		PoliceDepartmentSearchResponceVO vo = new PoliceDepartmentSearchResponceVO();

		CitizenApplicationSearchResponceVO dataVO = setRegistrationDetailsIntoResultVO(regDoc.get());

		BeanUtils.copyProperties(dataVO, vo);

		vo.setFatherName(StringUtils.isNotBlank(regDoc.get().getApplicantDetails().getFatherName())
				? regDoc.get().getApplicantDetails().getFatherName()
				: StringUtils.EMPTY);
		vo.setPresentAddress(addressMapper.convertEntity(regDoc.get().getApplicantDetails().getPresentAddress()));

		vo.setChassisnumber(dataVO.getAppChassiNumber() != null ? dataVO.getAppChassiNumber() : StringUtils.EMPTY);
		vo.setEngineNumber(dataVO.getAppEngineNumber() != null ? dataVO.getAppEngineNumber() : StringUtils.EMPTY);
		if (regDoc.get().getVahanDetails() != null) {
			vo.setCubicCapacity(regDoc.get().getVahanDetails().getCubicCapacity() != null
					? regDoc.get().getVahanDetails().getCubicCapacity()
					: StringUtils.EMPTY);
			vo.setSeatingCapacity(regDoc.get().getVahanDetails().getSeatingCapacity() != null
					? regDoc.get().getVahanDetails().getSeatingCapacity()
					: StringUtils.EMPTY);
			vo.setWheelbase(regDoc.get().getVahanDetails().getWheelbase() != null
					? regDoc.get().getVahanDetails().getWheelbase()
					: null);
			vo.setGvw(regDoc.get().getVahanDetails().getGvw() != null ? regDoc.get().getVahanDetails().getGvw() : null);
			vo.setUlw(regDoc.get().getVahanDetails().getUnladenWeight() != null
					? regDoc.get().getVahanDetails().getUnladenWeight()
					: null);
			vo.setMnfMonthAndYear(dataVO.getMonthAndYear() != null ? dataVO.getMonthAndYear() : StringUtils.EMPTY);
		}
		if (regDoc.get().getInsuranceDetails() != null) {
			vo.setInsurenceValidUpto(regDoc.get().getInsuranceDetails().getValidTill() != null ? DateConverters
					.convertLocalDateToLocalDateTime(regDoc.get().getInsuranceDetails().getValidTill()).toLocalDate()
					: null);
		}
		if (regDoc.get().getPucDetailsDTO() != null) {
			vo.setPucValidUpto(
					regDoc.get().getPucDetailsDTO().getValidTo() != null ? regDoc.get().getPucDetailsDTO().getValidTo()
							: null);
		}

		vo.setTrNo(StringUtils.isNotEmpty(regDoc.get().getTrNo()) ? regDoc.get().getTrNo() : StringUtils.EMPTY);
		return Optional.of(vo);
	}

	@Override
	public boolean isFreshStageCarriage(RegServiceVO regServiceVO) {
		boolean returnValue = false;
		try {
			if (regServiceVO.getServiceIds().contains(ServiceEnum.FEECORRECTION.getId())) {
				List<RegServiceDTO> regServiceList = regServiceDAO.findByPrNoAndApplicationStatusOrderByCreatedDateDesc(
						regServiceVO.getPrNo(), StatusRegistration.CITIZENPAYMENTPENDING);
				if (CollectionUtils.isNotEmpty(regServiceList)) {
					for (RegServiceDTO regServiceDTO : regServiceList) {
						if (regServiceDTO.getServiceIds().contains(ServiceEnum.NEWSTAGECARRIAGEPERMIT.getId())) {
							returnValue = Boolean.TRUE;
						}
					}
				}
			}
		} catch (Exception e) {
			logger.debug("Exception occured while fetching the data,[{}]", e.getMessage());
		}
		return returnValue;
	}

	@Override
	public RegServiceVO validateAndSaveForFreshStageCarriage(RegServiceVO regServiceVO) {
		try {
			List<RegServiceDTO> regServiceList = regServiceDAO.findByPrNoAndApplicationStatusOrderByCreatedDateDesc(
					regServiceVO.getPrNo(), StatusRegistration.CITIZENPAYMENTPENDING);
			Optional<RegServiceDTO> dto = regServiceList.stream().findFirst();
			Set<Integer> serviceIds = dto.get().getServiceIds();
			List<ServiceEnum> serviceEnums = dto.get().getServiceType();
			serviceIds.add(ServiceEnum.FEECORRECTION.getId());
			serviceEnums.add(ServiceEnum.FEECORRECTION);
			dto.get().setServiceIds(serviceIds);
			dto.get().setServiceType(serviceEnums);
			regServiceDAO.save(dto.get());
			return regServiceMapper.convertEntity(dto).get();
		} catch (Exception e) {
			logger.debug("Exception occured while fetching the data,[{}]", e.getMessage());
		}
		return null;
	}

	@Override
	public Pair<Boolean, String> getEbiddingPaymentDoneOrNot(String trNo, String mobileNo, String selectedPrNo) {
		Optional<SpecialNumberDetailsDTO> specialNumberDetailsDTO = specialNumberDetailsDAO
				.findByVehicleDetailsTrNumberAndCustomerDetailsMobileNoAndSelectedPrSeriesAndBidStatusIn(trNo, mobileNo,
						selectedPrNo,
						Arrays.asList(BidStatus.BIDWIN.getDescription(), BidStatus.BIDABSENT.getDescription(),
								BidStatus.BIDTIE.getDescription(), BidStatus.BIDLOOSE.getDescription()));
		if (specialNumberDetailsDTO.isPresent()) {

			return Pair.of(Boolean.TRUE, specialNumberDetailsDTO.get().getSpecialNumberAppId());
		} else
			throw new BadRequestException("Payment not yet complete, So you unable to download recipte");
	}

	@SuppressWarnings("null")
	@Override
	public List<AadharDropListVO> aadharDropList(AadharReqServiceIdsVO aadharReqServiceIdsVO) {
		List<ServicesDTO> dtos = servicesDAO.findByStatusIsTrueAndAadharOtpStatusIsTrue();
		Set<Integer> serviceIdsList = dtos.stream().map(a -> a.getServiceId()).collect(Collectors.toSet());
		List<AadharDropListDTO> listDTOs = null;
		List<AadharDropListVO> listVo = null;
		if (serviceIdsList.containsAll(aadharReqServiceIdsVO.getServiceIds())
				&& !serviceIdsList.contains(ServiceEnum.TRANSFEROFOWNERSHIP.getId())) {
			listDTOs = aadharDropDAO.findByStatusIsTrue();
			listVo = aadharDropDownMapper.convertEntity(listDTOs);
			return listVo;
		} else {
			AadharDropListDTO dto = new AadharDropListDTO();
			// listVo=new ArrayList<AadharDropListVO>();
			listVo = new ArrayList<AadharDropListVO>();
			String name = "BIOMETRIC";
			dto = aadharDropDAO.findByName(name).get();
			AadharDropListVO vo = aadharDropDownMapper.convertEntity(dto);
			listVo.add(vo);
			return listVo;
		}

	}

	@Override
	public RegServiceVO getFCImgsByPrno(String prNo, String user, Boolean isStoppage) {
		CitizenEnclosuresDTO dto = null;
		RegServiceVO regServiceVO = null;
		List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = new ArrayList<KeyValue<String, List<ImageEnclosureDTO>>>();
		Optional<UserDTO> userDetails = userDAO.findByUserId(user);
		if (!userDetails.isPresent()) {
			logger.error("user details not found. [{}] ", user);
			throw new BadRequestException("No record found. " + user);
		}
		RegServiceDTO regDTO = this.returnLatestFcDoc(prNo);
		if (!isStoppage && !((regDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWFC.getId()))
				|| regDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RENEWALFC.getId()))
				|| regDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.OTHERSTATIONFC.getId())))
				&& regDTO.getServiceIds().size() == 1)) {
			logger.error("Application not belong to fitness service.");
			throw new BadRequestException("Application not belong to fitness service.");
		}
		if(isStoppage &&  !regDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))) {
			throw new BadRequestException("Application not belong to Stoppage service.");
		}
		if (regDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))
				&& regDTO.getApplicationStatus().equals(StatusRegistration.INITIATED)) {

			if (regDTO.getVehicleStoppageDetails() != null && regDTO.getVehicleStoppageDetails().getActions() != null
					&& regDTO.getVehicleStoppageDetails().getActions().stream()
							.anyMatch(a -> a.getRole().equals(RoleEnum.MVI.getName()))) {
				Optional<CitizenEnclosuresDTO> encDetails = citizenEnclosuresDAO
						.findByApplicationNo(regDTO.getApplicationNo());
				if (encDetails.isPresent()) {

					dto = encDetails.get();
					List<KeyValue<String, List<ImageEnclosureDTO>>> enclousersTo = dto.getEnclosures();
					enclousersTo = enclousersTo.stream()
							.filter(a -> a.getValue().stream()
									.anyMatch(b -> b.getImageStaus().equals(StatusRegistration.APPROVED.getDescription())))
							.collect(Collectors.toList());

					regServiceVO = regServiceMapper.convertEntity(regDTO);

					regServiceVO.setEnclosures(enclosuresLogMapper.convertNewEnclosures(enclousersTo));
				}

			} else
				throw new BadRequestException(
						"MVI action not completed for this application " + regDTO.getApplicationNo());
		} else {
			if (!regDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))
					&& regDTO.getApplicationStatus().equals(StatusRegistration.APPROVED)) {

				boolean userCheck = regDTO.getActionDetails().stream().anyMatch(u -> u.getUserId().equals(user));
				if (userCheck) {

					List<ActionDetails> actionDetails = regDTO.getActionDetails().stream()
							.filter(a -> a.getlUpdate().toLocalDate().isEqual(LocalDate.now()))
							.collect(Collectors.toList());

					if (!CollectionUtils.isEmpty(actionDetails)) {

						Optional<CitizenEnclosuresDTO> encDetails = citizenEnclosuresDAO
								.findByApplicationNo(regDTO.getApplicationNo());
						if (encDetails.isPresent()) {

							dto = encDetails.get();
							List<KeyValue<String, List<ImageEnclosureDTO>>> enclousersTo = dto.getEnclosures();
							enclousersTo = enclousersTo.stream().filter(a -> a.getValue().stream()
									.anyMatch(b -> b.getImageActions() != null
											&& b.getImageStaus().equals(StatusRegistration.APPROVED.getDescription())))
									.collect(Collectors.toList());

							regServiceVO = regServiceMapper.convertEntity(regDTO);

							regServiceVO.setEnclosures(enclosuresLogMapper.convertNewEnclosures(enclousersTo));

							List<EnclosuresDTO> masterEnclosures = enclosureDAO
									.findByServiceIDAndApplicantType(ServiceEnum.NEWFC.getId(), ApplicantTypeEnum.MVI);

							for (EnclosuresDTO enclosuresDTOs : masterEnclosures) {

								regServiceVO.getEnclosures().forEach(ae -> ae.getValue().forEach(be -> {

									if (be.getImageType().equalsIgnoreCase(enclosuresDTOs.getProof())) {
										be.setFileOrder(enclosuresDTOs.getEnclosureId());
									}
									if (be.getImageType().equals(EnclosureType.BodyBuilderSpeedDevice.getValue())) {
										be.setFileOrder(2);
									}

								}

								));

							}
						}
					} else
						throw new BadRequestException(
								"you cant reupload the Image,if the image upload date and reupload date is not same ");

				} else
					throw new BadRequestException("un authorized user to do Fitness image corrections.");
			} else
				throw new BadRequestException(
						"MVI action not completed for this application " + regDTO.getApplicationNo());
		}
		return regServiceVO;
	}

	@Override
	public CitizenApplicationSearchResponceLimitedVO getApplicationSearchResultForCitizenLimtedData(
			Optional<CitizenApplicationSearchResponceVO> citizenVO) {
		if (StringUtils.isNoneEmpty(citizenVO.get().getRegistraionNumber())
				|| StringUtils.isNotEmpty(citizenVO.get().getChassisnumber())) {
			CitizenApplicationSearchResponceLimitedVO limitedVO = new CitizenApplicationSearchResponceLimitedVO();
			limitedVO.setRegistraionNumber(citizenVO.get().getRegistraionNumber());
			limitedVO.setRegistrationAuthority(citizenVO.get().getRegistrationAuthority());
			limitedVO.setChassisnumber(citizenVO.get().getChassisnumber());
			limitedVO.setEngineNumber(citizenVO.get().getEngineNumber());
			limitedVO.setClassOfVehicle(citizenVO.get().getClassOfVehicle());
			limitedVO.setColor(citizenVO.get().getColor());
			limitedVO.setBodyType(citizenVO.get().getBodyType());
			limitedVO.setFuelType(citizenVO.get().getFuelType());
			limitedVO.setRlw(citizenVO.get().getRlw());
			limitedVO.setMakerClass(citizenVO.get().getMakerClass());
			limitedVO.setMonthAndYear(citizenVO.get().getMonthAndYear());
			limitedVO.setMakerName(citizenVO.get().getMakerName());
			limitedVO.setOwnerName(citizenVO.get().getOwnerName());
			limitedVO.setTaxAmount(citizenVO.get().getTaxAmount());
			limitedVO.setTaxPaidDate(citizenVO.get().getTaxPaidDate());
			limitedVO.setTaxValidUpto(citizenVO.get().getTaxValidUpto());
			limitedVO.setNocTo(citizenVO.get().getNocTo());
			limitedVO.setNocDate(citizenVO.get().getNocDate());
			limitedVO.setRegistrationValidUpto(citizenVO.get().getRegistrationValidUpto());
			limitedVO.setStatus(citizenVO.get().getStatus());
			return limitedVO;
		}
		return null;
	}

	@Override
	public CitizenSearchReportVO applicationSearchForVcrInMVI(ApplicationSearchVO applicationSearchVO) {
		List<VcrFinalServiceDTO> vcrList = null;
		CitizenSearchReportVO outPut = new CitizenSearchReportVO();
		vcrList = getTotalVcrs(applicationSearchVO, vcrList);
		if (vcrList == null || vcrList.isEmpty()) {
			throw new BadRequestException("No vcr details found");
		}
		vcrList = this.getVcrDetails(Arrays.asList(vcrList.stream().findFirst().get().getVcr().getVcrNumber()),
				applicationSearchVO.isRequestFromAO(), true);
		getVcrAmount(applicationSearchVO, vcrList, outPut);
		VcrFinalServiceDTO dto = vcrList.stream().findFirst().get();
		boolean allowForCash = this.shouldNotAllowForPayCash(dto);
		if (outPut != null && outPut.getVcrList() != null && !outPut.getVcrList().isEmpty()) {
			outPut.getVcrList().stream().forEach(id -> {
				this.setTaxForPrint(id, dto.getTaxAmountForPrint());
				id.setShouldNotAllowForPayCash(allowForCash);
			});
		}

		return outPut;

	}

	@Override
	public Optional<TowVO> towtokendetailsByprNo(String prNo){
		Optional<TowVO> towVO = null;
		List<RegServiceDTO> regServiceDTOList = regServiceDAO.findByPrNoAndServiceTypeIn(prNo,
				Arrays.asList(ServiceEnum.TRANSFEROFOWNERSHIP));
		if(regServiceDTOList.isEmpty()) {
			throw new BadRequestException("TOW details not found");
		}
		
		if (!regServiceDTOList.isEmpty()) {
			regServiceDTOList.sort((p2, p1) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
			RegServiceDTO regServiceDTO = regServiceDTOList.stream().findFirst().get();
			if(regServiceDTO.getBuyerDetails() == null) {
				throw new BadRequestException("TOW BuyerDetails not found");
			}
			//set last 4 digits of aadharnumber into buyer aadhardetails
			regServiceDTO.getBuyerDetails().setBuyerAadhaarNo(StringUtils.right(regServiceDTO.getBuyerDetails().getBuyerAadhaarNo(),4));
						
			TowVO vo = regServiceMapper.convertEntityToTowVO(regServiceDTO);	
			if(vo.getTokenNoGeneratedTime() !=null) {
				DateTimeFormatter aFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
				vo.setTokenDate(vo.getTokenNoGeneratedTime().format(aFormatter));	
			}		 
			towVO = Optional.of(vo);
			
		}
		
		return towVO;
	}
}
