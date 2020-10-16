package org.epragati.rta.service.impl;

import java.beans.PropertyDescriptor;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.ConnectException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.script.Invocable;
import javax.script.ScriptException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.NestedNullException;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.GenericValidator;
import org.epragati.aadhaar.AadhaarDetailsRequestVO;
import org.epragati.aadhaar.AadharDetailsResponseVO;
import org.epragati.aadhaarAPI.AadhaarSourceDTO;
import org.epragati.aadhaarseeding.vo.AadhaarSeedVO;
import org.epragati.aadhar.DateUtil;
import org.epragati.actions.dao.CollectionCorrectionDAO;
import org.epragati.actions.dao.RCActionRulesDAO;
import org.epragati.actions.dao.SuspensionDAO;
import org.epragati.actions.dto.CorrectionDTO;
import org.epragati.actions.dto.RCActionRulesDTO;
import org.epragati.actions.dto.RCActionsDTO;
import org.epragati.actions.mapper.ActionRulesMapper;
import org.epragati.actions.mapper.SuspensionMapper;
import org.epragati.aop.QueryExecutionService;
import org.epragati.cfst.service.ElasticService;
import org.epragati.civilsupplies.vo.RationCardDetailsVO;
import org.epragati.collection.correction.service.CollectionCorrectionServices;
import org.epragati.common.dao.PropertiesDAO;
import org.epragati.common.dto.CollectionCorrectionModule;
import org.epragati.common.dto.FlowDTO;
import org.epragati.common.dto.HsrpDetailDTO;
import org.epragati.common.dto.PropertiesDTO;
import org.epragati.common.dto.aadhaar.seed.AadhaarSeedDTO;
import org.epragati.common.vo.CorrectionDropDown;
import org.epragati.common.vo.CorrectionVO;
import org.epragati.common.vo.PropertiesVO;
import org.epragati.common.vo.UserStatusEnum;
import org.epragati.constants.CovCategory;
import org.epragati.constants.DisposalType;
import org.epragati.constants.EnclosureType;
import org.epragati.constants.MessageKeys;
import org.epragati.constants.OwnerTypeEnum;
import org.epragati.constants.RtaRoles;
import org.epragati.constants.ServiceTypeCorrection;
import org.epragati.constants.SuspensionSources;
import org.epragati.constants.TransferType;
import org.epragati.dao.enclosure.CitizenEnclosuresDAO;
import org.epragati.dao.enclosure.CitizenEnclosuresLogDAO;
import org.epragati.dao.enclosure.FinancierEnclosureDAO;
import org.epragati.dao.enclosure.TemporaryEnclosuresDAO;
import org.epragati.dto.enclosure.CitizenEnclosuresDTO;
import org.epragati.dto.enclosure.CitizenEnclosuresLogsDTO;
import org.epragati.dto.enclosure.FinancierEnclosuresDTO;
import org.epragati.dto.enclosure.ImageActionsDTO;
import org.epragati.dto.enclosure.ImageEnclosureDTO;
import org.epragati.dto.enclosure.TemporaryEnclosuresDTO;
import org.epragati.eibt.service.impl.BASE64DecodedMultipartFile;
import org.epragati.exception.BadRequestException;
import org.epragati.fa.dto.FinancialAssistanceDAO;
import org.epragati.fa.dto.FinancialAssistanceDTO;
import org.epragati.fa.vo.FinancialAssistanceVO;
import org.epragati.hsrp.dao.HSRPDetailDAO;
import org.epragati.hsrp.mapper.ActionsDetailsMapper;
import org.epragati.images.service.CitizenImageService;
import org.epragati.images.vo.ImageInput;
import org.epragati.images.vo.InputVO;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dao.AlterationDAO;
import org.epragati.master.dao.AuctionDetailsDAO;
import org.epragati.master.dao.BodyTypeDAO;
import org.epragati.master.dao.ClassOfVehiclesDAO;
import org.epragati.master.dao.CorrectionsDAO;
import org.epragati.master.dao.DealerActionDetailsDAO;
import org.epragati.master.dao.DealerCovDAO;
import org.epragati.master.dao.DealerRegDAO;
import org.epragati.master.dao.FcCorrectionLogDAO;
import org.epragati.master.dao.FcDetailsDAO;
import org.epragati.master.dao.FeeCorrectionDAO;
import org.epragati.master.dao.FinanceSeedDetailsDAO;
import org.epragati.master.dao.FinancierCreateRequestDao;
import org.epragati.master.dao.LogsDAO;
import org.epragati.master.dao.MakersDAO;
import org.epragati.master.dao.MandalDAO;
import org.epragati.master.dao.MasterCovDAO;
import org.epragati.master.dao.MasterFcQuestionsDAO;
import org.epragati.master.dao.MasterPayperiodDAO;
import org.epragati.master.dao.MasterRcCancellationQuestionsDAO;
import org.epragati.master.dao.MasterTaxDAO;
import org.epragati.master.dao.MasterUsersDAO;
import org.epragati.master.dao.MsterStoppageQuationsDAO;
import org.epragati.master.dao.MsterStoppageRevocationQuationsDAO;
import org.epragati.master.dao.NocDetailsDAO;
import org.epragati.master.dao.NonPaymentDetailsDAO;
import org.epragati.master.dao.OffenceDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.PermitCorrectionLogDAO;
import org.epragati.master.dao.RTADashBoardDAO;
import org.epragati.master.dao.RegServiceCorrectionLogDAO;
import org.epragati.master.dao.RegServiceDAO;
import org.epragati.master.dao.RegistrationCorrectionLogDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.RegistrationDetailLogDAO;
import org.epragati.master.dao.RejectionHistoryLogsDAO;
import org.epragati.master.dao.SecondVehicleExcemptionDAO;
import org.epragati.master.dao.ShowCauseDetailsDAO;
import org.epragati.master.dao.StagingRegServiceDetailsAutoApprovalLogDAO;
import org.epragati.master.dao.StagingRegistrationDetailsDAO;
import org.epragati.master.dao.TaxCorrectionLogDAO;
import org.epragati.master.dao.TaxDetailsDAO;
import org.epragati.master.dao.TaxTypeDAO;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dao.VehicleStoppageDetailsDAO;
import org.epragati.master.dto.AadhaarDetailsResponseDTO;
import org.epragati.master.dto.ActionDetailsDTO;
import org.epragati.master.dto.ApplicantAddressDTO;
import org.epragati.master.dto.ApplicantDetailsDTO;
import org.epragati.master.dto.ApplicationTypeDTO;
import org.epragati.master.dto.ApprovalProcessFlowDTO;
import org.epragati.master.dto.AuctionDetailsDTO;
import org.epragati.master.dto.AuctionVehicleDetailsDTO;
import org.epragati.master.dto.BaseRegistrationDetailsDTO;
import org.epragati.master.dto.BloodGroupDTO;
import org.epragati.master.dto.BodyTypeDTO;
import org.epragati.master.dto.CcrRequestDetails;
import org.epragati.master.dto.CcrResponseDetails;
import org.epragati.master.dto.ClassOfVehiclesLogDTO;
import org.epragati.master.dto.ClientMetaData;
import org.epragati.master.dto.CollectionCorrectionServiceLogsDTO;
import org.epragati.master.dto.CollectionDTO;
import org.epragati.master.dto.ContactDTO;
import org.epragati.master.dto.CountryDTO;
import org.epragati.master.dto.DealerActionDetailsDTO;
import org.epragati.master.dto.DealerCovDTO;
import org.epragati.master.dto.DealerDetailsDTO;
import org.epragati.master.dto.DealerRegDTO;
import org.epragati.master.dto.DistrictDTO;
import org.epragati.master.dto.Enclosures;
import org.epragati.master.dto.FcCorrectionLogDTO;
import org.epragati.master.dto.FcDetailsDTO;
import org.epragati.master.dto.FeeCorrectionDTO;
import org.epragati.master.dto.FinanceDetailsDTO;
import org.epragati.master.dto.FinanceSeedDetailsDTO;
import org.epragati.master.dto.FinancierCreateRequestDTO;
import org.epragati.master.dto.HSRPStatusDTO;
import org.epragati.master.dto.InsuranceDetailsDTO;
import org.epragati.master.dto.InvoiceDetailsDTO;
import org.epragati.master.dto.LockedDetailsDTO;
import org.epragati.master.dto.LogsDTO;
import org.epragati.master.dto.MakersDTO;
import org.epragati.master.dto.MandalDTO;
import org.epragati.master.dto.MasterAmountSecoundCovsDTO;
import org.epragati.master.dto.MasterCovDTO;
import org.epragati.master.dto.MasterFCQuestionsDTO;
import org.epragati.master.dto.MasterPayperiodDTO;
import org.epragati.master.dto.MasterRcCancellationQuestionsDTO;
import org.epragati.master.dto.MasterStoppageQuationsDTO;
import org.epragati.master.dto.MasterStoppageRevocationQuationsDTO;
import org.epragati.master.dto.MasterTax;
import org.epragati.master.dto.MasterUsersDTO;
import org.epragati.master.dto.NonPaymentDetailsDTO;
import org.epragati.master.dto.OffenceDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.PanDetailsDTO;
import org.epragati.master.dto.PermitCorrectionLogDTO;
import org.epragati.master.dto.PostOfficeDTO;
import org.epragati.master.dto.QualificationDTO;
import org.epragati.master.dto.RCCancellationDTO;
import org.epragati.master.dto.RTADashBoardLinksDTO;
import org.epragati.master.dto.RTADashBoardServiceNames;
import org.epragati.master.dto.RTADashboardDTO;
import org.epragati.master.dto.RTOCorrections;
import org.epragati.master.dto.RegServiceCorrectionLogDTO;
import org.epragati.master.dto.RegistartionCorrectionLogDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.RegistrationDetailsLogDTO;
import org.epragati.master.dto.RegistrationValidityDTO;
import org.epragati.master.dto.RejectionHistoryDTO;
import org.epragati.master.dto.RejectionHistoryLogsDTO;
import org.epragati.master.dto.RoleActionDTO;
import org.epragati.master.dto.RolesDTO;
import org.epragati.master.dto.SecondVehicleExcemptionDetails;
import org.epragati.master.dto.ShowCauseDetailsDTO;
import org.epragati.master.dto.StagingRegServiceDetailsAutoApprovalDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.dto.StateDTO;
import org.epragati.master.dto.TaxComponentDTO;
import org.epragati.master.dto.TaxCorrectionLogDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.master.dto.TaxTypeDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.dto.VahanDetailsDTO;
import org.epragati.master.dto.VillageDTO;
import org.epragati.master.mappers.AadhaarDetailsResponseMapper;
import org.epragati.master.mappers.BodyTypeMapper;
import org.epragati.master.mappers.DealerRegMapper;
import org.epragati.master.mappers.DisabledDataMapper;
import org.epragati.master.mappers.DistrictMapper;
import org.epragati.master.mappers.FcDetailsMapper;
import org.epragati.master.mappers.FcQuestionsMapper;
import org.epragati.master.mappers.FinanceDetailsMapper;
import org.epragati.master.mappers.FinancialAssistanceMapper;
import org.epragati.master.mappers.MandalMapper;
import org.epragati.master.mappers.MasterCovMapper;
import org.epragati.master.mappers.MasterStoppageQuationsMapper;
import org.epragati.master.mappers.MasterStoppageRevocationQuationsMapper;
import org.epragati.master.mappers.NonPaymentDetailsMapper;
import org.epragati.master.mappers.PermitDetailsMapper;
import org.epragati.master.mappers.PermitGoodsDetailsMapper;
import org.epragati.master.mappers.RTADashboardMapper;
import org.epragati.master.mappers.RcCancellationQuestionsMappers;
import org.epragati.master.mappers.RegVehicleDetailsMapper;
import org.epragati.master.mappers.RegistrationCardDetailsMapper;
import org.epragati.master.mappers.RegistrationDetailsMapper;
import org.epragati.master.mappers.RejectionHistoryMapper;
import org.epragati.master.mappers.StagingRegistrationDetailsMapper;
import org.epragati.master.mappers.StateMapper;
import org.epragati.master.mappers.TaxTypeMapper;
import org.epragati.master.mappers.TrailerChassisDetailsMapper;
import org.epragati.master.mappers.UserMapper;
import org.epragati.master.service.LogMovingService;
import org.epragati.master.service.MasterAmountSecoundCovsService;
import org.epragati.master.service.PrSeriesService;
import org.epragati.master.vo.ActionDetailsVO;
import org.epragati.master.vo.ApplicantSearchWithOutIdInput;
import org.epragati.master.vo.BodyTypeVO;
import org.epragati.master.vo.DLResponceVO;
import org.epragati.master.vo.DealerRegVO;
import org.epragati.master.vo.DlDetailsVO;
import org.epragati.master.vo.FcDetailsVO;
import org.epragati.master.vo.FinanceDetailsVO;
import org.epragati.master.vo.MasterCovVO;
import org.epragati.master.vo.MasterFcQuestionVO;
import org.epragati.master.vo.MasterRcCancellationQuestionsVO;
import org.epragati.master.vo.MasterStoppageQuationsVO;
import org.epragati.master.vo.MasterStoppageRevocationQuationsVO;
import org.epragati.master.vo.OffenceVO;
import org.epragati.master.vo.RTADashboardVO;
import org.epragati.master.vo.RegistrationDetailsVO;
import org.epragati.master.vo.RejectionHistoryVO;
import org.epragati.master.vo.TaxTypeVO;
import org.epragati.master.vo.UserVO;
import org.epragati.master.vo.VCRVahanVehicleDetailsVO;
import org.epragati.payment.dto.BreakPaymentsSaveDTO;
import org.epragati.payment.dto.ClassOfVehiclesDTO;
import org.epragati.payment.dto.FeeDetailsDTO;
import org.epragati.payment.dto.FeesDTO;
import org.epragati.payment.dto.PaymentTransactionDTO;
import org.epragati.payment.mapper.FeeDetailsMapper;
import org.epragati.payment.report.vo.NonPaymentDetailsVO;
import org.epragati.payment.report.vo.RegReportVO;
import org.epragati.payment.report.vo.ReportsVO;
import org.epragati.payments.dao.PaymentTransactionDAO;
import org.epragati.payments.vo.FeeDetailsVO;
import org.epragati.payments.vo.FeesVO;
import org.epragati.permits.dao.OtherStateTemporaryPermitDetailsDAO;
import org.epragati.permits.dao.PermitDetailsDAO;
import org.epragati.permits.dao.PermitSuspCanRevDAO;
import org.epragati.permits.dto.OtherStateTemporaryPermitDetailsDTO;
import org.epragati.permits.dto.PermitDetailsDTO;
import org.epragati.permits.dto.PermitSuspCanRevDTO;
import org.epragati.permits.mappers.PermitSuspCanRevMapper;
import org.epragati.permits.mappers.RouteDetailsMapper;
import org.epragati.permits.vo.PermitDetailsVO;
import org.epragati.permits.vo.PermitSuspCanRevVO;
import org.epragati.rcactions.RCActionRulesVO;
import org.epragati.rcactions.RCActionsVO;
import org.epragati.rcactions.SearchRcRequestVO;
import org.epragati.registration.service.DealerRegistrationService;
import org.epragati.registration.service.DealerService;
import org.epragati.registration.service.RegistrationMigrationSolutionsService;
import org.epragati.regservice.CitizenTaxService;
import org.epragati.regservice.RegistrationService;
import org.epragati.regservice.dao.AadhaarSeedDAO;
import org.epragati.regservice.dao.RegServiceUnlockDAO;
import org.epragati.regservice.dto.ActionDetails;
import org.epragati.regservice.dto.AlterationDTO;
import org.epragati.regservice.dto.GreenTaxDTO;
import org.epragati.regservice.dto.NOCDetailsDTO;
import org.epragati.regservice.dto.RegServiceApprovedDTO;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.dto.RegServiceUnlockDTO;
import org.epragati.regservice.dto.TheftVehicleDetailsDTO;
import org.epragati.regservice.impl.RegistrationServiceImpl;
import org.epragati.regservice.mapper.AadhaarSeedMapper;
import org.epragati.regservice.mapper.AuctionDetailsMapper;
import org.epragati.regservice.mapper.AuctionVehicleDetailsMapper;
import org.epragati.regservice.mapper.FeeCorrectionMapper;
import org.epragati.regservice.mapper.LockedDetailsMapper;
import org.epragati.regservice.mapper.RegServiceMapper;
import org.epragati.regservice.mapper.ReportDataVO;
import org.epragati.regservice.mapper.TaxDataVO;
import org.epragati.regservice.mapper.TaxDetailsMasterMapper;
import org.epragati.regservice.mapper.TaxDetailsMasterVO;
import org.epragati.regservice.vo.AlterationVO;
import org.epragati.regservice.vo.ApplicationCancellationVO;
import org.epragati.regservice.vo.ApplicationSearchVO;
import org.epragati.regservice.vo.AuctionDetailsVO;
import org.epragati.regservice.vo.AuctionVehicleDetailsVO;
import org.epragati.regservice.vo.CitizenApplicationSearchResponceVO;
import org.epragati.regservice.vo.CommonFieldsVO;
import org.epragati.regservice.vo.FeeCorrectionVO;
import org.epragati.regservice.vo.LockedDetailsVO;
import org.epragati.regservice.vo.RegServiceVO;
import org.epragati.regservice.vo.VehicleStoppageMVIReportVO;
import org.epragati.restGateway.RestGateWayService;
import org.epragati.restGateway.service.impl.RegistratrionServicesApprovalsImpl;
import org.epragati.rta.reports.vo.CitizenSearchReportVO;
import org.epragati.rta.service.impl.service.RTAService;
import org.epragati.rta.service.impl.service.RegistratrionServicesApprovals;
import org.epragati.rta.vo.CitizenDashBordDetails;
import org.epragati.rta.vo.CitizenEnclosuresLogsVO;
import org.epragati.rta.vo.CollectionCorrectonSaveVO;
import org.epragati.rta.vo.CorrectionsVO;
import org.epragati.rta.vo.DashBordDetails;
import org.epragati.rta.vo.DisabledDataVO;
import org.epragati.rta.vo.EnclosuresVO;
import org.epragati.rta.vo.PendingCountVo;
import org.epragati.rta.vo.RCCancellationActionVO;
import org.epragati.rta.vo.RegistrationCorrectionsVO;
import org.epragati.rta.vo.RtaActionVO;
import org.epragati.rta.vo.ServiceWisePendingCountVO;
import org.epragati.rta.vo.UnlockServiceRecordVO;
import org.epragati.sequence.SequenceGenerator;
import org.epragati.service.enclosure.mapper.EnclosureImageMapper;
import org.epragati.service.enclosure.mapper.EnclosuresLogMapper;
import org.epragati.service.enclosure.vo.CitizenImagesInput;
import org.epragati.service.enclosure.vo.DisplayEnclosures;
import org.epragati.service.enclosure.vo.ImageVO;
import org.epragati.service.files.GridFsClient;
import org.epragati.service.notification.MessageTemplate;
import org.epragati.service.notification.NotificationTemplates;
import org.epragati.service.notification.NotificationUtil;
import org.epragati.tax.vo.TaxTypeEnum;
import org.epragati.util.AppMessages;
import org.epragati.util.BidNumberType;
import org.epragati.util.CorrectionEnum;
import org.epragati.util.IPWebUtils;
import org.epragati.util.JScriptEngine;
import org.epragati.util.PermitsEnum;
import org.epragati.util.PermitsEnum.PermitType;
import org.epragati.util.ResponseStatusEnum;
import org.epragati.util.RoleEnum;
import org.epragati.util.Status;
import org.epragati.util.Status.AadhaarSeedStatus;
import org.epragati.util.Status.RCActionStatus;
import org.epragati.util.Status.permitSuspCanRevStatus;
import org.epragati.util.StatusRegistration;
import org.epragati.util.StatusRegistration.FinancierCreateReqStatus;
import org.epragati.util.StatusRegistration.TheftState;
import org.epragati.util.SuspensionEnum;
import org.epragati.util.ValidityEnum;
import org.epragati.util.document.KeyValue;
import org.epragati.util.document.Sequence;
import org.epragati.util.payment.ClassOfVehicleEnum;
import org.epragati.util.payment.DynamicmenusEnum;
import org.epragati.util.payment.ServiceCodeEnum;
import org.epragati.util.payment.ServiceEnum;
import org.epragati.util.payment.ServiceEnum.Flow;
import org.epragati.vcr.vo.VcrFinalServiceVO;
import org.epragati.vcrImage.dao.VcrFinalServiceDAO;
import org.epragati.vcrImage.dao.VoluntaryTaxDAO;
import org.epragati.vcrImage.dto.OtherSectionDTO;
import org.epragati.vcrImage.dto.SeizedAndDocumentImpoundedDTO;
import org.epragati.vcrImage.dto.VcrCorrectionLogDTO;
import org.epragati.vcrImage.dto.VcrFinalServiceDTO;
import org.epragati.vcrImage.dto.VehicleSeizedDTO;
import org.epragati.vcrImage.dto.VoluntaryTaxDTO;
import org.epragati.vcrImage.mapper.VcrFinalServiceMapper;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author kumaraswamy.asari
 *
 */

@Service
public class RTAServiceImpl implements RTAService {

	private static final Logger logger = LoggerFactory.getLogger(RTAServiceImpl.class);

	@Autowired
	private JScriptEngine jsEngine;

	@Value("${script.engine.location}")
	private String scriptEngineLocation;

	@Value("${no.of.records.assign}")
	Integer noOfRecordsAssign;

	@Autowired
	AppMessages appMessages;

	@Autowired
	SequenceGenerator sequenceGenerator;

	@Autowired
	private RegistrationDetailsMapper<?> regDetailMapper;

	@Autowired
	private StagingRegistrationDetailsDAO stagingRegistrationDetailsDAO;

	@Autowired
	private MasterTaxDAO masterTaxDAO;

	@Autowired
	private MasterAmountSecoundCovsService masterAmountSecoundCovsService;
	@Autowired
	private RejectionHistoryMapper rejectionMapper;

	@Autowired
	private DTOUtilService dTOUtilService;

	@Autowired
	private PrSeriesService prService;

	@Autowired
	private HSRPDetailDAO hsrpDetailDAO;

	@Autowired
	private AlterationDAO alterationDAO;

	@Autowired
	private MasterCovDAO masterCovDAO;

	@Autowired
	private MasterCovMapper masterCovMapper;

	@Autowired
	private LogsDAO logsDAO;

	@Autowired
	private DisabledDataMapper disabledMapper;

	@Autowired
	private FcDetailsDAO fcDetailsDAO;

	@Autowired
	private NotificationUtil notifications;

	@Autowired
	private NotificationTemplates notificationTemplate;

	@Autowired
	private RejectionHistoryLogsDAO rejectionLogsDAO;

	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;

	@Autowired
	private ClassOfVehiclesDAO classOfVehiclesDAO;

	@Autowired
	private RegServiceDAO regServiceDAO;

	@Autowired
	private RegServiceMapper regServiceMapper;

	@Autowired
	private RegVehicleDetailsMapper vehicleDetailsMapper;

	@Autowired
	private EnclosuresLogMapper enclosuresLogMapper;

	@Autowired
	private TaxDetailsDAO taxDetailsDAO;

	@Autowired
	private AadhaarSeedDAO aadhaarSeedDAO;

	@Autowired
	private TaxTypeDAO taxTypeDAO;

	@Autowired
	private TaxTypeMapper taxTypeMapper;

	@Autowired
	private TrailerChassisDetailsMapper trailerChassisDetailsMapper;

	@Autowired
	private ElasticService elasticService;

	@Autowired
	private NocDetailsDAO nocDetailsDAO;

	@Autowired
	private LogMovingService logMovingService;

	@Autowired
	private EnclosureImageMapper enclosureMapper;

	@Autowired
	private SuspensionDAO suspensionDAO;

	@Autowired
	private SuspensionMapper suspensionMapper;

	@Autowired
	private RCActionRulesDAO rcActionRulesDAO;

	@Autowired
	private ActionRulesMapper actionRulesMapper;

	@Autowired
	private RegistratrionServicesApprovals registratrionServicesApprovals;

	@Autowired
	private RegistrationService registrationService;

	@Autowired
	private SecondVehicleExcemptionDAO secondVehicleExcemptionDAO;

	@Autowired
	private FcQuestionsMapper fcQuestionsMapper;

	@Autowired
	private MasterFcQuestionsDAO masterFcQuestionsDAO;

	@Autowired
	private FcDetailsMapper fcDetailsMapper;

	@Autowired
	private PermitSuspCanRevDAO permitSuspCanRevDAO;

	@Autowired
	private PermitDetailsDAO permitDetailsDAO;

	@Autowired
	private PermitSuspCanRevMapper permitSuspCanRevMapper;

	@Autowired
	private PaymentTransactionDAO paymentTransactionDAO;

	@Autowired
	private MsterStoppageQuationsDAO msterStoppageQuationsDAO;

	@Autowired
	private MsterStoppageRevocationQuationsDAO msterStoppageRevocationQuationsDAO;

	@Autowired
	private MasterStoppageQuationsMapper masterStoppageQuationsMapper;

	@Autowired
	private MasterStoppageRevocationQuationsMapper masterStoppageRevocationQuationsMapper;

	@Autowired
	private PermitDetailsMapper permitDetailsMapper;

	@Autowired
	private TaxDetailsMasterMapper taxDetailsMasterMapper;

	@Autowired
	private RestGateWayService restGateWayService;

	@Autowired
	private CorrectionsDAO correctionsDAO;

	@Autowired
	private BodyTypeDAO bodyTypeDAO;

	@Autowired
	private RegServiceUnlockDAO regServiceUnlockDAO;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private RegistrationCardDetailsMapper registrationCardDetailsMapper;

	@Autowired
	private DealerService dealerService;

	@Autowired
	private MakersDAO makersDAO;

	@Autowired
	private FinanceDetailsMapper financeDetailsMapper;

	@Autowired
	private MandalMapper mandalMapper;

	@Autowired
	private DistrictMapper districtMapper;

	@Autowired
	private StateMapper stateMapper;

	@Autowired
	private CitizenEnclosuresDAO citizenEnclosuresDAO;

	@Autowired
	private OfficeDAO officeDAO;
	@Autowired
	private FinancierCreateRequestDao finCreateReqDao;

	@Autowired
	private FinancierEnclosureDAO financierEncloserDao;

	@Autowired
	private MasterPayperiodDAO masterPayperiodDAO;

	@Autowired
	private CitizenTaxService citizenTaxService;

	@Autowired
	private PermitGoodsDetailsMapper permitGoodsDetailsMapper;

	@Autowired
	private RouteDetailsMapper routeDetailsMapper;
	@Autowired
	RegistratrionServicesApprovalsImpl registratrionServices;
	@Autowired
	private FeeCorrectionMapper feeCorrectionMapper;
	@Autowired
	private FeeCorrectionDAO feeCorrectionDAO;
	@Autowired
	private MandalDAO mandalDAO;

	@Autowired
	private RTADashBoardDAO rtadbDAO;

	@Autowired
	private RTADashboardMapper rtadbMapper;

	@Autowired
	private VcrFinalServiceDAO vcrFinalServiceDAO;
	@Autowired
	private PropertiesDAO propertiesDAO;
	@Autowired
	private OffenceDAO offenceDAO;
	@Autowired
	private CitizenEnclosuresLogDAO EnclosuresLogDAO;
	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private MasterUsersDAO masterUsersDAO;
	/*
	 * @Autowired private CollectionCorrectonDAO collectionCorrectonDAO;
	 */
	/*
	 * @Autowired private CollectionCorrectionServiceLogDAO ccorrectionDAO;
	 */
	@Autowired
	IPWebUtils ipWebUtils;

	@Autowired
	private CollectionCorrectionServices collectionCorrectionServices;

	@Autowired
	private CollectionCorrectionDAO collectionDAO;

	@Autowired
	private RegistrationCorrectionLogDAO regCorrectionLogDAO;

	@Autowired
	private PermitCorrectionLogDAO permitCorrectionLogDAO;

	@Autowired
	private TaxCorrectionLogDAO taxCorrectionLogDAO;

	@Autowired
	private FcCorrectionLogDAO fcCorrectionLogDAO;

	@Autowired
	private RegServiceCorrectionLogDAO regServiceCorrectionLogDAO;

	@Autowired
	private RegistrationServiceImpl registrationServiceImpl;

	@Autowired
	private GridFsClient gridFsClient;
	@Autowired
	private UserMapper userMapper;
	@Autowired
	private CitizenImageService magesService;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Autowired
	private NonPaymentDetailsDAO nonPaymentDetailsDAO;

	@Autowired
	private ShowCauseDetailsDAO showCauseDetailsDAO;

	@Autowired
	private NonPaymentDetailsMapper nonPaymentDetailsMapper;

	@Autowired
	private VehicleStoppageDetailsDAO vehicleStoppageDetailsDAO;

	@Autowired
	private MasterRcCancellationQuestionsDAO masterRcCancellationQuestionsDAO;

	@Autowired
	private RcCancellationQuestionsMappers rcCancellationQuestionsMappers;
	@Autowired
	private VcrFinalServiceMapper finalServiceMapper;

	@Autowired
	private DealerCovDAO dealerCovDAO;

	@Autowired
	private AuctionDetailsDAO auctionDetailsDAO;
	@Autowired
	private AuctionDetailsMapper auctionDetailsMapper;
	@Autowired
	private AuctionVehicleDetailsMapper auctionVehicleDetailsMapper;

	@Autowired
	private ActionsDetailsMapper actionsDetailsMapper;
	@Autowired
	private FeeDetailsMapper feeDetailsMapper;

	@Autowired
	private QueryExecutionService queryExecutionService;

	@Autowired
	private StagingRegistrationDetailsMapper stagingRegistrationDetailsMapper;

	@Autowired
	private AadhaarSeedMapper aadhaarSeedMapper;

	@Autowired
	private StagingRegServiceDetailsAutoApprovalLogDAO stagingRegLogDAO;
	@Autowired
	private RegistrationDetailLogDAO registrationDetailLogDAO;

	@Autowired
	private FinancialAssistanceDAO financialAssistanceDAO;
	@Autowired
	private FinancialAssistanceMapper financialAssistanceMapper;
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private AadhaarDetailsResponseMapper aadhaarDetailsResponseMapper;

	@Autowired
	private TemporaryEnclosuresDAO temporaryEnclosuresDAO;

	@Autowired
	private VoluntaryTaxDAO voluntaryTaxDAO;

	@Autowired
	private OtherStateTemporaryPermitDetailsDAO otherStateTemporaryPermitDetailsDAO;

	@Autowired
	private LockedDetailsMapper lockedDetailsMapper;
	
	@Autowired
	private FinanceSeedDetailsDAO financeSeedDetailsDAO;
	
	@Autowired
	private RegistratrionServicesApprovalsImpl registratrionServicesApprovalsImpl;
	

	@Value("${reg.driver.details.url:http://10.80.1.161:8443/dl/searchDlDataForEibtRegister}")
	private String driverDetailsUrl;

	@Override
	public DashBordDetails getDashBoard(String officeCode, String userId, String role) {
		DashBordDetails dashBordDetails = new DashBordDetails();
		PendingCountVo pendingCountVo = new PendingCountVo();
		Integer nonTransport = 0;
		Integer transport = 0;
		Integer total = 0;
		Integer bodyBuildingCount = 0;
		Integer aadhaarSeedCount = 0;
		// Pending count of New financier Requests
		Integer financierCreatePendingCount = 0;
		Integer aadhaarSeedAOApprovedCount = 0;
		Integer aadhaarSeedAORejectedCount = 0;

		List<StagingRegistrationDetailsDTO> list = getRoleOfficeCodeAndStatusBasedRecords(role, officeCode);
		logger.debug("New registration DashBoard Count [{}]", list.size());
		List<RegistrationDetailsVO> registrationDetailsList = new ArrayList<>();

		if (!list.isEmpty()) {
			nonTransport = list.stream().filter(val -> val.getVehicleType().equals(CovCategory.N.getCode()))
					.collect(Collectors.toList()).size();
			transport = list.stream().filter(val -> val.getVehicleType().equals(CovCategory.T.getCode()))
					.collect(Collectors.toList()).size();
			total = list.size();
		} else {
			logger.debug("No Pending Records Found [{}] ", registrationDetailsList.size());
		}
		pendingCountVo.setTotalCount(total);
		pendingCountVo.setNonTransportCount(nonTransport);
		pendingCountVo.setTransportCount(transport);
		pendingCountVo.setBodyBuildingCount(bodyBuildingCount);
		pendingCountVo.setAadhaarSeedPendingCount(aadhaarSeedCount);
		pendingCountVo.setAadhaarSeedApprovedCount(aadhaarSeedAOApprovedCount);
		pendingCountVo.setAadhaarSeedRejectedCount(aadhaarSeedAORejectedCount);
		// Financier Applications Count in CCO Dash Board
		if (role.equalsIgnoreCase(RoleEnum.CCO.name())) {
			List<FinancierCreateRequestDTO> listOfNewFin = finCreateReqDao.findByOfficeOfficeCodeAndApplicationStatus(
					officeCode, FinancierCreateReqStatus.INITIATED.getLabel());
			listOfNewFin.addAll(finCreateReqDao.findByOfficeOfficeCodeAndApplicationStatus(officeCode,
					FinancierCreateReqStatus.REUPLOAD.getLabel()));
			financierCreatePendingCount = listOfNewFin.size();

		}
		// Financier Applications Count in AO Dash Board
		else if (role.equalsIgnoreCase(RoleEnum.AO.name())) {
			List<FinancierCreateRequestDTO> listOfNewFin = null;
			listOfNewFin = finCreateReqDao.findByOfficeOfficeCodeAndApplicationStatus(officeCode,
					FinancierCreateReqStatus.CCO_APPROVED.getLabel());
			listOfNewFin.addAll(finCreateReqDao.findByOfficeOfficeCodeAndApplicationStatus(officeCode,
					FinancierCreateReqStatus.CCO_REJECTED.getLabel()));
			/*
			 * listOfNewFin.addAll(finCreateReqDao.
			 * findByOfficeOfficeCodeAndApplicationStatus(officeCode,
			 * FinancierCreateReqStatus.INITIATED.getLabel()));
			 * listOfNewFin.addAll(finCreateReqDao.
			 * findByOfficeOfficeCodeAndApplicationStatus(officeCode,
			 * FinancierCreateReqStatus.REUPLOAD.getLabel()));
			 */
			financierCreatePendingCount = listOfNewFin.size();
			// TODO: make ApplicationStatusIn < >
		}
		// Financier Applications Count
		pendingCountVo.setFinancierCreatePendingCount(financierCreatePendingCount);

		if (role.equalsIgnoreCase(RoleEnum.AO.name())) {
			List<AadhaarSeedDTO> aadhaarSeedCountList = aadhaarSeedDAO.findByIssuedOfficeCodeAndStatusIn(officeCode,
					Arrays.asList(Status.AadhaarSeedStatus.INITIATED));
			if (!aadhaarSeedCountList.isEmpty()) {
				pendingCountVo.setAadhaarSeedPendingCount(aadhaarSeedCountList.size());
			}
		}
		if (role.equalsIgnoreCase(RoleEnum.RTO.name())) {
			List<AadhaarSeedDTO> aadhaarSeedCountList = aadhaarSeedDAO.findByIssuedOfficeCodeAndStatusIn(officeCode,
					Arrays.asList(Status.AadhaarSeedStatus.AOAPPROVED, Status.AadhaarSeedStatus.AOREJECTED));
			List<AadhaarSeedDTO> aadhaarSeedRejectedCountList = aadhaarSeedCountList.stream()
					.filter(val -> val.getStatus().equals(Status.AadhaarSeedStatus.AOREJECTED))
					.collect(Collectors.toList());
			List<AadhaarSeedDTO> aadhaarSeedApprovedCountList = aadhaarSeedCountList.stream()
					.filter(val -> val.getStatus().equals(Status.AadhaarSeedStatus.AOAPPROVED))
					.collect(Collectors.toList());
			pendingCountVo.setAadhaarSeedRejectedCount(aadhaarSeedRejectedCountList.size());
			pendingCountVo.setAadhaarSeedApprovedCount(aadhaarSeedApprovedCountList.size());
		}
		dashBordDetails.setPendingCountVo(pendingCountVo);
		return dashBordDetails;
	}

	private List<StagingRegistrationDetailsDTO> getSpecificRoleBaseRecords(String role, String officeCode,
			String vehicleType) {
		List<StagingRegistrationDetailsDTO> pendingList = stagingRegistrationDetailsDAO
				.findByOfficeDetailsOfficeCodeAndApplicationStatusInAndVehicleTypeAndBodyBuilding(officeCode,
						getApplicationStatusForPendingList(), vehicleType, false);
		List<StagingRegistrationDetailsDTO> filteredList = pendingList.parallelStream()
				.filter(val -> val.getFlowDetails() != null && val.getFlowDetails().getFlowDetails() != null
						&& !val.getFlowDetails().getFlowDetails().keySet().isEmpty()
						&& val.getFlowDetails().getFlowDetails()
								.get(val.getFlowDetails().getFlowDetails().keySet().stream().findFirst().get()).stream()
								.anyMatch(roleAction -> roleAction.getRole().equalsIgnoreCase(role)))
				.collect(Collectors.toList());
		if (role.equals(RoleEnum.MVI.getName())) {
			filteredList = filteredList.stream()
					.filter(val -> !val.getApplicationStatus().equals(StatusRegistration.SLOTBOOKED.getDescription()))
					.collect(Collectors.toList());
		}
		return filteredList;
	}

	private List<StagingRegistrationDetailsDTO> getRoleOfficeCodeAndStatusBasedRecords(String role, String officeCode) {
		List<StagingRegistrationDetailsDTO> pendingList = stagingRegistrationDetailsDAO
				.findByOfficeDetailsOfficeCodeAndApplicationStatusInAndBodyBuilding(officeCode,
						getApplicationStatusForPendingList(), false);
		List<StagingRegistrationDetailsDTO> filteredList = pendingList.stream()
				.filter(val -> val.getFlowDetails() != null && val.getFlowDetails().getFlowDetails() != null
						&& !val.getFlowDetails().getFlowDetails().keySet().isEmpty()
						&& val.getFlowDetails().getFlowDetails()
								.get(val.getFlowDetails().getFlowDetails().keySet().stream().findFirst().get()).stream()
								.anyMatch(roleAction -> roleAction.getRole().equalsIgnoreCase(role)))
				.collect(Collectors.toList());
		if (role.equals(RoleEnum.MVI.getName())) {
			filteredList = filteredList.stream()
					.filter(val -> !val.getApplicationStatus().equals(StatusRegistration.SLOTBOOKED.getDescription()))
					.collect(Collectors.toList());
		}
		return filteredList;
	}

	@Override
	public List<RegistrationDetailsVO> getPendingList(String officeCode, String userId, String role,
			String vehicleType) {
		// Allows only One Office Code
		List<StagingRegistrationDetailsDTO> toBeAssignedList = new ArrayList<>();
		List<LockedDetailsDTO> lockedDetailsLog = new ArrayList<>();
		List<RegistrationDetailsVO> returnList = new ArrayList<>();
		List<StagingRegistrationDetailsDTO> pending = new ArrayList<>();
		synchronized (officeCode.intern()) {
			Optional<StagingRegistrationDetailsDTO> stagingDetailsDTO = stagingRegistrationDetailsDAO
					.findByLockedDetailsLockedByAndLockedDetailsLockedByRoleAndVehicleType(userId, role, vehicleType);
			if (stagingDetailsDTO.isPresent()) {
				logger.debug(" applicationNo [{}] lockedBy [{}]", stagingDetailsDTO.get().getApplicationNo(), userId);
				RegistrationDetailsVO regVO = regDetailMapper.limitedFiledsForDashBoard(stagingDetailsDTO.get());
				if (Arrays.asList(ClassOfVehicleEnum.CHST.getCovCode(), ClassOfVehicleEnum.CHSN.getCovCode())
						.contains(stagingDetailsDTO.get().getClassOfVehicle())) {
					Optional<AlterationDTO> alterationDTO = alterationDAO.findByApplicationNo(regVO.getApplicationNo());
					if (alterationDTO.isPresent()) {
						AlterationVO alterationVO = new AlterationVO();
						alterationVO.setCov(alterationDTO.get().getCov());
						regVO.setAlterationVO(alterationVO);
					}
				}
				returnList.add(regVO);
				return returnList;
			}
			List<StagingRegistrationDetailsDTO> pendingList = getSpecificRoleBaseRecords(role, officeCode, vehicleType);
			if (pendingList.parallelStream()
					.anyMatch(val -> val.getTrGeneratedDate() == null || val.getTrNo() == null)) {
				logger.error("Tr no or Tr generated Date is missing for officeCode [{}],vehicleType [{}],role [{}]",
						officeCode, vehicleType, role);
				throw new BadRequestException("Tr no or Tr generated Date is missing for officeCode[" + officeCode + "]"
						+ "vehicleType" + "[" + vehicleType + "]" + "role [" + role + "]");
			}
			if (!pendingList.isEmpty()) {
				pendingList.sort((o1, o2) -> o1.getTrGeneratedDate().compareTo(o2.getTrGeneratedDate()));

				pending = pendingList.stream()
						.filter((val -> val.getLockedDetails() == null || (val.getLockedDetails() != null && (val
								.getLockedDetails().stream().allMatch(data -> !data.getLockedByRole().equals(role))))))
						.collect(Collectors.toList());
				int assignedCount = 0;
				for (StagingRegistrationDetailsDTO stagingRegistrationDetailsDTO : pending) {
					if (noOfRecordsAssign > assignedCount) {
						LockedDetailsDTO lockedDetail = setLockedDetails(userId, role,
								stagingRegistrationDetailsDTO.getIteration(),
								stagingRegistrationDetailsDTO.getVehicleType(),
								stagingRegistrationDetailsDTO.getApplicationNo());
						if (stagingRegistrationDetailsDTO.getLockedDetails() == null) {
							lockedDetailsLog.add(lockedDetail);
							stagingRegistrationDetailsDTO.setLockedDetails(lockedDetailsLog);
						} else {

							stagingRegistrationDetailsDTO.getLockedDetails().add(lockedDetail);
						}
						assignedCount++;
						toBeAssignedList.add(stagingRegistrationDetailsDTO);
						continue;
					}
					break;
				}
				stagingRegistrationDetailsDAO.save(toBeAssignedList);
			}
		}
		for (StagingRegistrationDetailsDTO stagingDto : toBeAssignedList) {
			RegistrationDetailsVO regVO = regDetailMapper.limitedFiledsForDashBoard(stagingDto);
			if (Arrays.asList(ClassOfVehicleEnum.CHST.getCovCode(), ClassOfVehicleEnum.CHSN.getCovCode())
					.contains(stagingDto.getClassOfVehicle())) {
				Optional<AlterationDTO> alterationDTO = alterationDAO.findByApplicationNo(regVO.getApplicationNo());
				if (alterationDTO.isPresent()) {
					AlterationVO alterationVO = new AlterationVO();
					alterationVO.setCov(alterationDTO.get().getCov());
					regVO.setAlterationVO(alterationVO);
				}

			}
			returnList.add(regVO);
		}
		return returnList;
	}

	public List<StatusRegistration> getApplicationStatusForPendingList() {
		List<StatusRegistration> status = new ArrayList<>();
		status.add(StatusRegistration.TRGENERATED);
		status.add(StatusRegistration.DEALERRESUBMISSION);
		status.add(StatusRegistration.SECORINVALIDDONE);
		status.add(StatusRegistration.SLOTBOOKED);
		status.add(StatusRegistration.TAXPAID);
		status.add(StatusRegistration.APPROVED);
		return status;

	}

	public LockedDetailsDTO setLockedDetails(String userId, String role, Integer iterationNo, String module,
			String applicatioNo) {
		LockedDetailsDTO lockedDetail = new LockedDetailsDTO();
		lockedDetail.setApplicatioNo(applicatioNo);
		lockedDetail.setIterationNo(iterationNo);
		lockedDetail.setLockedDate(LocalDateTime.now());
		lockedDetail.setModule(module);
		lockedDetail.setLockedByRole(role);
		lockedDetail.setLockedBy(userId);
		return lockedDetail;

	}

	public LockedDetailsDTO releaseLockDetails(LockedDetailsDTO lockedDetails) {
		lockedDetails.setReleaseDate(LocalDateTime.now());
		return lockedDetails;
	}

	@Override
	public boolean processFlow(StagingRegistrationDetailsDTO stagingDTO, JwtUser jwtUser, String action,
			String applicationNo, String role) {
		// Async block for saving report data
		// Below method is moved to end of the method exception may occur in between
		// queryExecutionService.saveRegDetailsEodReport(stagingDTO, jwtUser, action,
		// LocalDateTime.now(), role);
		Set<Integer> index = stagingDTO.getFlowDetails().getFlowDetails().keySet();
		Integer flowPosition = index.stream().findFirst().get();
		logger.debug(appMessages.getLogMessage(MessageKeys.FLOW_POSITION));
		List<RoleActionDTO> roleActionDTOList = stagingDTO.getFlowDetails().getFlowDetails().get(flowPosition);
		List<RoleActionDTO> filteredist = roleActionDTOList.stream().filter(val -> val.getRole().equals(role))
				.collect(Collectors.toList());

		if (filteredist.isEmpty()) {
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.NO_AUTHORIZATION));
		}

		RoleActionDTO roleActionDTO = getRoleActionDTO(action, applicationNo, role, stagingDTO, jwtUser.getFirstname(),
				jwtUser.getId());

		if (role.equalsIgnoreCase(RoleEnum.RTO.getName())
				&& action.equalsIgnoreCase(StatusRegistration.REJECTED.getDescription())) {

			Integer templateId = MessageTemplate.NEW_REG_PR_REJECTED.getId();
			logger.info("send notification started for rejected Application No  [{}]", stagingDTO.getApplicationNo());
			notifications.sendNotifications(templateId, stagingDTO);
		}
		List<FlowDTO> existingFlowLog = new ArrayList<>();

		if (stagingDTO.getFlowDetailsLog() != null) {
			existingFlowLog = stagingDTO.getFlowDetailsLog();
			FlowDTO logFlowDTO = null;
			if (existingFlowLog.isEmpty()) {
				logFlowDTO = new FlowDTO();
				List<RoleActionDTO> logFlowDTOList = new ArrayList<RoleActionDTO>();
				logFlowDTOList.add(roleActionDTO);
				logFlowDTO.getFlowDetails().put(flowPosition, logFlowDTOList);
				existingFlowLog.add(logFlowDTO);
			} else {
				logFlowDTO = stagingDTO.getFlowDetailsLog().stream().findFirst().get();
				List<RoleActionDTO> logFlowDTOList = logFlowDTO.getFlowDetails().get(flowPosition);
				if (logFlowDTOList == null) {
					logFlowDTOList = new ArrayList<RoleActionDTO>();
				}
				logFlowDTOList.add(roleActionDTO);
				logFlowDTO.getFlowDetails().put(flowPosition, logFlowDTOList);
			}
			stagingDTO.setFlowDetailsLog(existingFlowLog);

		}
		roleActionDTOList.remove(filteredist.stream().findFirst().get());
		if (roleActionDTOList.isEmpty()) {
			stagingDTO.getFlowDetails().getFlowDetails().remove(flowPosition);
		}
		queryExecutionService.saveRegDetailsEodReport(stagingDTO, jwtUser, action, LocalDateTime.now(), role);
		return stagingDTO.getFlowDetails().getFlowDetails().isEmpty();
	}

	public boolean isProcessCompleted() {
		return true;
	}

	private Integer getCurrentFlowIndex(StagingRegistrationDetailsDTO stagingDTO) {
		Set<Integer> index = stagingDTO.getFlowDetails().getFlowDetails().keySet();
		Integer flowPosition = index.stream().findFirst().get();
		logger.debug(appMessages.getLogMessage(MessageKeys.FLOW_POSITION), flowPosition);
		return flowPosition;
	}

	@Override
	public void doAction(JwtUser jwtUser, RtaActionVO actionVo, String role) throws Exception {

		String applicationNo = actionVo.getApplicationNo();

		synchronized (applicationNo.intern()) {
			Optional<StagingRegistrationDetailsDTO> stagingDto = stagingRegistrationDetailsDAO
					.findByApplicationNo(actionVo.getApplicationNo());

			if (!stagingDto.isPresent()) {
				logger.error("no.authorization");
				throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.NO_AUTHORIZATION));
			}

			boolean isProcessCompleted = false;
			StagingRegistrationDetailsDTO staginDto = stagingDto.get();
			Integer flowPosition = getCurrentFlowIndex(staginDto);
			Invocable invocable = jsEngine.getInvocableObj(scriptEngineLocation);
			boolean isAnyFlow = verifyAnyFlow(invocable, flowPosition);
			logger.debug(appMessages.getLogMessage(MessageKeys.ANY_FLOW), isAnyFlow);

			if (isAnyFlow) {
				isProcessCompleted = prepareScriptInputAndExecute(stagingDto.get(), flowPosition, role,
						actionVo.getAction(), invocable);

			}
			if (role.equalsIgnoreCase(RoleEnum.RTO.getName())) {
				staginDto.setIterationFlag(Boolean.TRUE);
			}
			// Second Vehicle Rejected Status
			if (actionVo.getSecondVehicleList() != null) {
				if (null != actionVo.getSecondVehicleList().getIsSecondVehicleRejected()
						&& actionVo.getSecondVehicleList().getIsSecondVehicleRejected()) {
					if (actionVo.getSecondVehicleList().getRole().equalsIgnoreCase(RoleEnum.RTO.getName())) {
						staginDto.setIsFirstVehicle(false);
					}
				}
				if (null != actionVo.getSecondVehicleList().getIsInvalidVehicleRejection()
						&& actionVo.getSecondVehicleList().getIsInvalidVehicleRejection()) {
					if (actionVo.getSecondVehicleList().getRole().equalsIgnoreCase(RoleEnum.RTO.getName())) {
						if (actionVo.getSecondVehicleList().getClassOfVehicle() == null) {
							logger.error("Please select class of vehicle");
							throw new BadRequestException(
									appMessages.getResponseMessage(MessageKeys.CLASSOFVEHICLE_NOTSELECTED));
						}
						staginDto.setClassOfVehicle(actionVo.getSecondVehicleList().getClassOfVehicle());
						ClassOfVehiclesDTO cov = classOfVehiclesDAO
								.findByCovcode(actionVo.getSecondVehicleList().getClassOfVehicle());
						if (cov != null) {
							staginDto.setClassOfVehicleDesc(cov.getCovdescription());
						}
					}
				}
			}
			try {
				if (CollectionUtils.isNotEmpty(stagingDto.get().getEnclosures())
						&& CollectionUtils.isNotEmpty(actionVo.getEnclosures())) {
					actionVo.getEnclosures().stream().forEach(enclosure -> {
						updateEnclosures(role, enclosure.getImages(), staginDto);
					});
				} else {
					logger.warn(appMessages.getLogMessage(MessageKeys.NOT_FOUND_ENCLOSURES),
							actionVo.getApplicationNo());
				}
			} catch (Exception e) {
				logger.error("failed {}", e.getMessage());
				logger.debug("failed {}", e);
			}
			// Saving DisabledData
			updateDisabledData(actionVo, staginDto);

			if (actionVo.getSecondVehicleList() != null) {
				updateSeconVehilceDetails(actionVo, staginDto);
			} else {
				logger.warn(appMessages.getLogMessage(MessageKeys.NOT_FOUND_REJECTION_HISTORY),
						actionVo.getApplicationNo());
			}

			List<LockedDetailsDTO> filteredLockedDetails = removeMatchedLockedDetails(staginDto, jwtUser.getId(), role);
			if (filteredLockedDetails != null) {
				staginDto.setLockedDetails(filteredLockedDetails);
			}

			if (RoleEnum.MVI.getName().equals(role)
					&& Arrays.asList(ClassOfVehicleEnum.CHST.getCovCode(), ClassOfVehicleEnum.CHSN.getCovCode())
							.contains(staginDto.getClassOfVehicle())
					&& !staginDto.isDiffTaxPaid()) {

				staginDto.setApplicationStatus(StatusRegistration.TAXPENDING.getDescription());

			}
			boolean isFlowCompleted = processFlow(stagingDto.get(), jwtUser, actionVo.getAction(),
					stagingDto.get().getApplicationNo(), role);
			// saving logs
			/*
			 * if (actionVo.getAction().equals("REJECTED")) { //saveLogs(staginDto, role,
			 * actionVo.getAction(), flowPosition); }
			 */
			if (isProcessCompleted || isFlowCompleted) {
				approval(actionVo, role, applicationNo, staginDto);
			} else {
				updateStagingRegDetails(staginDto);
			}
		}
	}

	private List<LockedDetailsDTO> removeMatchedLockedDetails(StagingRegistrationDetailsDTO stagingDTO, String userId,
			String role) {
		List<LockedDetailsDTO> lockedDetails = new ArrayList<LockedDetailsDTO>();
		if (CollectionUtils.isNotEmpty(stagingDTO.getLockedDetails())) {

			/*
			 * lockedDetails = stagingDTO.getLockedDetails(); stagingDTO.getLockedDetails()
			 * .removeIf(locked -> locked.getLockedBy().equals(userId) &&
			 * locked.getLockedByRole().equals(role));
			 */

			Iterator<LockedDetailsDTO> iter = lockedDetails.iterator();
			while (iter.hasNext()) {
				LockedDetailsDTO locked = iter.next();
				if (locked.getLockedBy().equals(userId) && locked.getLockedByRole().equals(role)) {
					iter.remove();
				}
			}
		}
		return lockedDetails;
	}

	@Override
	public void updateStagingRegDetails(StagingRegistrationDetailsDTO staginDto) {
		logMovingService.moveStagingToLog(staginDto.getApplicationNo());
		stagingRegistrationDetailsDAO.save(staginDto);
	}

	// TODO need to move to common method for RegistratrionServicesApprovals
	private StagingRegistrationDetailsDTO updateEnclosures(String role, List<ImageVO> imageList,
			StagingRegistrationDetailsDTO stageDto) {
		if (imageList != null) {
			for (ImageVO imageEnclosureVO : imageList) {
				String imageType = imageEnclosureVO.getImageType();
				for (KeyValue<String, List<ImageEnclosureDTO>> keyValue : stageDto.getEnclosures()) {
					if (imageType.equals(keyValue.getKey())) {

						for (ImageEnclosureDTO enclosureDTO : keyValue.getValue()) {
							if (enclosureDTO.getImageId().equals(imageEnclosureVO.getAppImageDocId())) {
								enclosureMapper.imageVOtoEnclosureDTO(role, enclosureDTO, imageEnclosureVO);
								if (enclosureDTO.getImageStaus().equals(StatusRegistration.REJECTED.getDescription())) {
									stageDto.setRejectedByEnclosure(true);
								}
							}
						}
					}

				}
			}
		}
		return stageDto;
	}

	public void saveLogs(StagingRegistrationDetailsDTO stagingDTO, String role, String action, Integer FlowPosition) {
		LogsDTO logs = new LogsDTO();
		FlowDTO flowDetails = new FlowDTO();
		LogsDTO logsDTO = logsDAO.findByApplicationNo(stagingDTO.getApplicationNo());
		if (logsDTO == null) {
			Map<Integer, List<RoleActionDTO>> map = new HashMap<Integer, List<RoleActionDTO>>();
			RoleActionDTO roleAction = new RoleActionDTO();
			roleAction.setAction(action);
			roleAction.setRole(role);

			List<RoleActionDTO> roleActionList = new ArrayList<RoleActionDTO>();
			roleActionList.add(roleAction);
			map.put(FlowPosition, roleActionList);
			flowDetails.setFlowDetails(map);

			logs.setFlowDetails(flowDetails);
			logs.setApplicationNo(stagingDTO.getApplicationNo());
		}

		else {
			Map<Integer, List<RoleActionDTO>> map = logsDTO.getFlowDetails().getFlowDetails();
			RoleActionDTO roleAction = new RoleActionDTO();
			roleAction.setAction(action);
			roleAction.setRole(role);

			List<RoleActionDTO> roleActionList = map.get(FlowPosition);
			roleActionList.add(roleAction);

			map.put(FlowPosition, roleActionList);
			flowDetails.setFlowDetails(map);
			logs.setFlowDetails(flowDetails);
			logs.setApplicationNo(stagingDTO.getApplicationNo());

		}
		logsDAO.save(logs);
	}

	/*
	 * public void saveLogs(StagingRegistrationDetailsDTO stagingDTO) { LogsDTO logs
	 * = new LogsDTO(); logs.setApplicationNo(stagingDTO.getApplicationNo());
	 * logs.setFlowDetails(stagingDTO.getFlowDetails()); if
	 * (stagingDTO.getFlowDetailsLog() != null) {
	 * logs.setFlowDetailsLog(stagingDTO.getFlowDetailsLog()); } logsDAO.save(logs);
	 * 
	 * }
	 */
	public StagingRegistrationDetailsDTO updateDisabledData(RtaActionVO actionvo,
			StagingRegistrationDetailsDTO stagingDTO) {
		DisabledDataVO disabledData = actionvo.getDisabledData();
		if (disabledData != null) {
			stagingDTO.setDisabledData(disabledMapper.convertVO(disabledData));
		}
		return stagingDTO;

	}

	public StagingRegistrationDetailsDTO updateSeconVehilceDetails(RtaActionVO actionVO,
			StagingRegistrationDetailsDTO stagingDTO) {
		RejectionHistoryVO rejectionHistory = actionVO.getSecondVehicleList();
		if (stagingDTO.getApplicationStatus().equalsIgnoreCase(StatusRegistration.IVCNREJECTED.getDescription())) {
			RejectionHistoryDTO rejectiondto = new RejectionHistoryDTO();
			rejectiondto.setIsInvalidVehicleRejection(Boolean.TRUE);
			stagingDTO.setRejectionHistory(rejectiondto);
		} else {
			if (rejectionHistory != null) {
				if (stagingDTO.getRejectionHistory() != null) {
					if (stagingDTO.getRejectionHistoryLog() != null) {
						stagingDTO.getRejectionHistoryLog().add(stagingDTO.getRejectionHistory());
					} else {
						stagingDTO.setRejectionHistoryLog(Arrays.asList(stagingDTO.getRejectionHistory()));
					}

				}
				RejectionHistoryDTO rejectionDTO = rejectionMapper.convertVO(rejectionHistory);
				stagingDTO.setRejectionHistory(rejectionDTO);
			}
		}
		return stagingDTO;
	}

	public RoleActionDTO getRoleActionDTO(String action, String applicationNo, String role,
			StagingRegistrationDetailsDTO stagingDTO, String userName, String userId) {

		RoleActionDTO roleActionDto = new RoleActionDTO();

		if (stagingDTO.getIteration() != null) {
			roleActionDto.setIteration(stagingDTO.getIteration());
		}
		roleActionDto.setAction(action);
		roleActionDto.setActionTime(LocalDateTime.now());
		roleActionDto.setApplicatioNo(applicationNo);
		roleActionDto.setModule(StatusRegistration.REGISTRATION.getDescription());
		roleActionDto.setRole(role);
		roleActionDto.setActionBy(userName);
		roleActionDto.setUserId(userId);
		roleActionDto.setEnclosures(stagingDTO.getEnclosures());
		stagingDTO.setProcessActionStatus(action);
		if (!role.equalsIgnoreCase("SYSTEM")) {
			stagingDTO.setAutoApprovalInitiatedDate(LocalDate.now());
		}
		return roleActionDto;

	}

	private Boolean prepareScriptInputAndExecute(StagingRegistrationDetailsDTO stagingDTO, Integer flowPosition,
			String role, String action, Invocable invocable) throws NoSuchMethodException, ScriptException {

		List<FlowDTO> flowDTO = stagingDTO.getFlowDetailsLog();
		Integer iteration = stagingDTO.getIteration();
		if (iteration == 1) {
			List<RoleActionDTO> actions = flowDTO.stream().findFirst().get().getFlowDetails().get(flowPosition - 1);

			Map<String, Object> map = new TreeMap<String, Object>();

			actions.stream().forEach(roleAction -> {
				map.put(roleAction.getRole(), roleAction.getAction());
			});

			map.put(role, action);

			logger.info("With Current Status Application No {[]} Invoke Actions  : {[]}", "Application No",
					map.toString());
			Map mapResult = (Map) invocable.invokeFunction("process", map);
			Boolean result = (Boolean) mapResult.get("result");
			logger.info(appMessages.getLogMessage(MessageKeys.PROCESS_APPROVAL_RESULT), result);

			return result;

		} else {
			return role.equalsIgnoreCase(RoleEnum.AO.getName())
					&& action.equalsIgnoreCase(StatusRegistration.REJECTED.getDescription()) ? false : true;
		}
	}

	private boolean verifyAnyFlow(Invocable invocable, Integer index) {
		Map<?, ?> result = null;
		try {
			result = (Map<?, ?>) invocable.invokeFunction("verifyAnyFlow", index);
		} catch (NoSuchMethodException | ScriptException e) {
		}
		if (result == null) {
			return false;
		}
		return true;
	}

	private void savePrNoInTaxDetails(StagingRegistrationDetailsDTO staginDto) {
		List<TaxDetailsDTO> taxDetailsList = taxDetailsDAO.findByApplicationNo(staginDto.getApplicationNo());
		if (!taxDetailsList.isEmpty()) {
			registrationService.updatePaidDateAsCreatedDate(taxDetailsList);
			taxDetailsList.sort((p1, p2) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
			TaxDetailsDTO taxDetails = taxDetailsList.stream().findFirst().get();
			taxDetails.setPrNo(staginDto.getPrNo());
			taxDetailsDAO.save(taxDetails);
			taxDetailsList.clear();

		}
	}

	private void lifTaxValidityCal(StagingRegistrationDetailsDTO stagingRegDetails) {

		Optional<MasterPayperiodDTO> Payperiod = masterPayperiodDAO
				.findByCovcode(stagingRegDetails.getClassOfVehicle());

		if (!Payperiod.isPresent()) {
			// throw error message
			logger.error("No record found in master_payperiod for: " + stagingRegDetails.getClassOfVehicle());
			throw new BadRequestException(
					"No record found in master_payperiod for: " + stagingRegDetails.getClassOfVehicle());

		}
		if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.BOTH.getCode())) {
			citizenTaxService.getPayPeroidForBoth(Payperiod, stagingRegDetails.getVahanDetails().getSeatingCapacity(),
					stagingRegDetails.getVahanDetails().getGvw());
		}
		if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.LifeTax.getCode())) {
			List<TaxDetailsDTO> listOfTax = taxDetailsDAO
					.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(
							stagingRegDetails.getApplicationNo(), Arrays.asList(ServiceCodeEnum.LIFE_TAX.getCode()));
			if (listOfTax != null && !listOfTax.isEmpty()) {

				listOfTax.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
				TaxDetailsDTO dto = listOfTax.stream().findFirst().get();
				dto.setTaxPeriodEnd(LocalDate.now().minusDays(1).plusYears(12));
				stagingRegDetails.setTaxvalidity(LocalDate.now().minusDays(1).plusYears(12));
				if (dto.getTaxDetails() != null) {
					for (Map<String, TaxComponentDTO> map : dto.getTaxDetails()) {
						for (Entry<String, TaxComponentDTO> entry : map.entrySet()) {

							if (entry.getKey().equalsIgnoreCase(ServiceCodeEnum.LIFE_TAX.getCode())) {
								if (entry.getValue().getValidityTo() != null) {

									entry.getValue().setValidityTo(LocalDate.now().minusDays(1).plusYears(12));
								}
							}
						}
					}
				}
				taxDetailsDAO.save(dto);

			}

		}

	}

	@Override
	public void assignPR(StagingRegistrationDetailsDTO staginDto) throws CloneNotSupportedException {
		if (!(staginDto.getApplicationStatus().equals(StatusRegistration.SPECIALNOPENDING.getDescription())
				|| staginDto.getApplicationStatus().equals(StatusRegistration.SECORINVALIDDONE.getDescription()))) {
			logger.error(appMessages.getResponseMessage(MessageKeys.INVALID_STATUS));
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.INVALID_STATUS));
		}
		this.processPR(staginDto);
	}

	@Override
	public void processPR(StagingRegistrationDetailsDTO staginDto) throws CloneNotSupportedException {

		String applicationNo = staginDto.getApplicationNo();
		staginDto.setApplicationStatus(StatusRegistration.PRGENERATED.toString());
		Integer districtId = staginDto.getApplicantDetails().getPresentAddress().getDistrict().getDistrictId();
		logger.info(appMessages.getLogMessage(MessageKeys.DISTRICT_ID), districtId);

		if (districtId == null) {
			throw new BadRequestException(appMessages.getLogMessage(MessageKeys.DISTRICT_ID_IS_REQUIRED));
		}
		// String prNo = prService.geneatePrSeries(districtId);

		if (((staginDto.getSpecialNumberRequired()
				&& (null != staginDto.getPrType() && StringUtils.isEmpty(staginDto.getPrNo())
						&& staginDto.getPrType().equalsIgnoreCase(BidNumberType.N.getCode()))))
				|| (!staginDto.getSpecialNumberRequired() && StringUtils.isEmpty(staginDto.getPrNo()))) {
			String prNo = prService.geneatePrNo(staginDto.getApplicationNo(), Integer.MIN_VALUE, Boolean.FALSE,
					StringUtils.EMPTY, null, Optional.empty());
			logger.info(appMessages.getLogMessage(MessageKeys.PR_NUMBER), prNo);
			staginDto.setPrNo(prNo);
		}

		if (!staginDto.isFromReassigment()) {
			staginDto.setPrGeneratedDate(LocalDateTime.now());
			// Updating class of vehicle in staging
			if (ClassOfVehicleEnum.CHSN.getCovCode().equalsIgnoreCase(staginDto.getClassOfVehicle())
					|| ClassOfVehicleEnum.CHST.getCovCode().equalsIgnoreCase(staginDto.getClassOfVehicle())
					|| ClassOfVehicleEnum.ARVT.getCovCode().equalsIgnoreCase(staginDto.getClassOfVehicle())) {
				Optional<AlterationDTO> alterationDTO = alterationDAO.findByApplicationNo(staginDto.getApplicationNo());
				if (!alterationDTO.isPresent()) {
					throw new BadRequestException("No record found in Alteration based on applicationNo ["
							+ staginDto.getApplicationNo() + "]");
				}
				staginDto.setClassOfVehicle(alterationDTO.get().getCov());
				staginDto.setClassOfVehicleDesc(alterationDTO.get().getCovDescription());
				updateAlterationDetailsInVahan(staginDto);
			}
		} else if (staginDto.isNeedToUpdatePrNoInFc()) {
			// Fc updating
			List<FcDetailsDTO> listOfFcDetails = fcDetailsDAO
					.findFirst5ByApplicationNoOrderByCreatedDateDesc(staginDto.getApplicationNo());
			if (!listOfFcDetails.isEmpty()) {
				listOfFcDetails.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
				FcDetailsDTO dto = listOfFcDetails.stream().findFirst().get();
				if (null == dto.getPrNo() || !dto.getPrNo().equalsIgnoreCase(staginDto.getPrNo())) {
					dto.setPrNo(staginDto.getPrNo());
					fcDetailsDAO.save(dto);
				}
			}
			listOfFcDetails.clear();
		}

		// save vehicle details
		staginDto.setVehicleDetails(vehicleDetailsMapper.convetVehicleDetailsFromVahan(staginDto));
		// save pr no in taxDetails
		savePrNoInTaxDetails(staginDto);
		RegistrationValidityDTO registrationValidityDTO = new RegistrationValidityDTO();

		// RegistrationValidityDTO reg = staginDto.getRegistrationValidity();

		// Registration validity for Non Transport Type
		if (staginDto.getVehicleType().equals(CovCategory.N.getCode()) && null != staginDto.getPrGeneratedDate()) {
			registrationValidityDTO.setRegistrationValidity(staginDto.getPrGeneratedDate().minusDays(1)
					.plusYears(ValidityEnum.PR_NON_TRANSPORT_VALIDITY.getValidity()));
		}

		// Registration validity for Transport Type
		else if (staginDto.getVehicleType().equals(CovCategory.T.getCode()) && null != staginDto.getPrGeneratedDate()) {
			registrationValidityDTO.setRegistrationValidity(staginDto.getPrGeneratedDate().minusDays(1)
					.plusYears(ValidityEnum.PR_TRANSPORT_VALIDITY.getValidity()));
			registrationValidityDTO
					.setFcValidity(LocalDate.now().minusDays(1).plusYears(ValidityEnum.FCVALIDITY.getValidity()));
		}

		registrationValidityDTO.setTaxValidity(staginDto.getTaxvalidity());
		if (registrationValidityDTO.getCessValidity() != null) {
			registrationValidityDTO.setCessValidity(staginDto.getCesValidity());
		}

		LocalDate now = LocalDate.now();
		registrationValidityDTO.setPrGeneratedDate(now);
		registrationValidityDTO.setTrGeneratedDate(now);
		registrationValidityDTO.setTrGeneratedDateStr(now.toString());
		registrationValidityDTO.setPrGeneratedDateStr(now.toString());

		staginDto.setRegistrationValidity(registrationValidityDTO);

		StagingRegistrationDetailsDTO duplicateStaginDetails = (StagingRegistrationDetailsDTO) staginDto.clone();

		HSRPStatusDTO hsrpStatusDetails = duplicateStaginDetails.getHsrpStatusDetails();

		Optional<HsrpDetailDTO> hsrpDetailDtoOptional = hsrpDetailDAO.findByTrNumber(staginDto.getTrNo());

		if (hsrpStatusDetails == null) {
			hsrpStatusDetails = new HSRPStatusDTO();
		}

		duplicateStaginDetails.setHsrpStatusDetails(hsrpStatusDetails);

		// PR
		// HSRP
		// Document Update
		// Send Notification
		// Move this record to Base - Done
		// Move Actions to Log - Done

		// Move to Main Table

		// Move Action Details

		// this.moveLockedDetails(staginDto);

		staginDto.setLockedDetails(null);
		/*
		 * if (!staginDto.isFromReassigment()) { this.saveFCData(staginDto); }
		 */

		// saving rejection history
		RejectionHistoryLogsDTO rejectionHistoryLogs = new RejectionHistoryLogsDTO();
		if (staginDto.getRejectionHistory() != null) {
			if (staginDto.getRejectionHistoryLog() != null) {
				rejectionHistoryLogs.setRejectionHistoryDTO(staginDto.getRejectionHistoryLog());
				try {
					rejectionHistoryLogs.getRejectionHistoryDTO().add(staginDto.getRejectionHistory());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				rejectionHistoryLogs.setRejectionHistoryDTO(Arrays.asList(staginDto.getRejectionHistory()));
			}
			saveRejectionHistory(rejectionHistoryLogs, staginDto);
		} else if (staginDto.getRejectionHistoryLog() != null) {
			rejectionHistoryLogs.setRejectionHistoryDTO(staginDto.getRejectionHistoryLog());
			saveRejectionHistory(rejectionHistoryLogs, staginDto);
		}

		if (staginDto.getFinanceDetails() != null) {
			staginDto.getFinanceDetails().setAgreementDate(LocalDate.now());
			dTOUtilService.saveFinanceDetails(staginDto.getFinanceDetails(), applicationNo);
		}

		dTOUtilService.moveActionDetails(staginDto.getActionDetails(), applicationNo);
		dTOUtilService.moveActionDetails(staginDto.getActionDetailsLog(), applicationNo);

		// Move Finance Details
		// dTOUtilService.moveFinanceDetails(staginDto.getFinanceDetails());

		// Move Flow Details
		// dTOUtilService.moveFlowDetails(staginDto.getFlowDetails());
		if (null != staginDto.getFlowDetailsLog() && staginDto.getFlowDetailsLog().size() > 0)
			dTOUtilService.moveFlowDetails(staginDto.getFlowDetailsLog(), applicationNo);

		//

		duplicateStaginDetails.setActionDetailsLog(null);
		duplicateStaginDetails.setFlowDetails(null);
		duplicateStaginDetails.setFlowDetailsLog(null);
		duplicateStaginDetails.setRejectionHistory(null);
		duplicateStaginDetails.setRejectionHistoryLog(null);

		// Updating life tax validity
		if (!staginDto.isFromReassigment()) {
			this.saveFCData(staginDto);
			lifTaxValidityCal(duplicateStaginDetails);
			this.saveHSRPData(hsrpDetailDtoOptional, staginDto);
			ClassOfVehiclesLogDTO covLog = new ClassOfVehiclesLogDTO();
			covLog.setFromCov(staginDto.getClassOfVehicle());
			covLog.setFrom(LocalDateTime.now());
			covLog.setCurrentcov(Boolean.TRUE);
			covLog.setCurrentcovNo(1);
			duplicateStaginDetails.setCovHistory(Arrays.asList(covLog));
		}

		try {
			// Add Notifiation PR

			Integer templateId = MessageTemplate.NEW_REG_PR.getId();
			logger.info("send notification started for Application No {}", staginDto.getApplicationNo());
			sendNotifications(templateId, staginDto);
		} catch (Exception e) {
			logger.info("Exception raised While sending notification {}", e.getMessage());
		}

		dTOUtilService.moveStagingDetails(duplicateStaginDetails, applicationNo);
		dTOUtilService.moveToRegistrationLogs(staginDto);
		// duplicateStaginDetails.set

		// Delete record From Staging, Make sure it is moved to Base
		// Registration
		logMovingService.moveStagingToLog(staginDto.getApplicationNo());
		stagingRegistrationDetailsDAO.delete(staginDto);

		// TODO: need to remover in future.
		if ((!staginDto.getSpecialNumberRequired() || (staginDto.getSpecialNumberRequired()
				&& StatusRegistration.SPECIALNOPENDING.getDescription().equals(staginDto.getApplicationStatus())))
				&& staginDto.getOwnerType().equals(OwnerTypeEnum.Individual)) {
			elasticService.saveRegDetailsToSecondVehicleData(staginDto);
		}

	}

	/*
	 * public void moveLockedDetails(StagingRegistrationDetailsDTO staginDto) {
	 * ArrayList<LockedDetailsDTO> lockedList = null;
	 * staginDto.setLockedDetails(lockedList); }
	 */

	private void saveRejectionHistory(RejectionHistoryLogsDTO rejectionHistoryLogs,
			StagingRegistrationDetailsDTO staginDto) {
		rejectionHistoryLogs.setApplicationNo(staginDto.getApplicationNo());
		rejectionHistoryLogs.setTrNo(staginDto.getTrNo());
		rejectionHistoryLogs.setPrNo(staginDto.getPrNo());
		rejectionLogsDAO.save(rejectionHistoryLogs);
	}

	public void saveFCData(StagingRegistrationDetailsDTO staginDto) {

		try {
			if (staginDto.getVehicleType().equals(CovCategory.T.getCode())) {
				// TODO : Need to generate Sequence Number
				Map<String, String> detail = new HashMap<String, String>();
				detail.put("officeCode", staginDto.getOfficeDetails().getOfficeCode());
				String fcNumber = sequenceGenerator.getSequence(String.valueOf(Sequence.FC.getSequenceId()), detail);
				FcDetailsDTO fcdetails = new FcDetailsDTO();
				fcdetails.setApplicationNo(staginDto.getApplicationNo());
				fcdetails.setOfficeCode(staginDto.getOfficeDetails().getOfficeCode());
				fcdetails.setOfficeName(staginDto.getOfficeDetails().getOfficeName());
				fcdetails.setInspectedMviOfficeName(staginDto.getOfficeDetails().getOfficeName());
				fcdetails.setVehicleNumber("");
				fcdetails.setFctype(ServiceEnum.NEWFC.getDesc());
				List<FlowDTO> listFlowDTO = staginDto.getFlowDetailsLog();
				if (listFlowDTO.size() > 0) {

					listFlowDTO.stream().forEach(flowDTO -> {
						Set<Integer> keys = flowDTO.getFlowDetails().keySet();
						keys.stream().forEach(indx -> {
							List<RoleActionDTO> roleActions = flowDTO.getFlowDetails().get(indx);
							roleActions.stream().forEach(roles -> {
								if (roles.getRole().equals(RoleEnum.MVI.getName()) && (roles.getAction()
										.equals(StatusRegistration.APPROVED.getDescription())
										|| roles.getAction().equals(StatusRegistration.REJECTED.getDescription()))) {
									fcdetails.setInspectedMviName(roles.getActionBy());
									fcdetails.setInspectedDate(roles.getActionTime());
									fcdetails.setUserId(roles.getUserId());
								}

							});
						});
					});
				}
				// fcdetails.setInspectedMviName(getMVIName(staginDto).get(RoleEnum.MVI.toString()).toString()
				// !=null?getMVIName(staginDto).get(RoleEnum.MVI.toString()).toString():"");
				// fcdetails.setInspectedDate(getMVIName(staginDto).get("DATE"));
				fcdetails.setChassisNo(staginDto.getVahanDetails().getChassisNumber());
				fcdetails.setEngineNo(staginDto.getVahanDetails().getEngineNumber());
				fcdetails.setClassOfVehicle(staginDto.getClassOfVehicle());
				fcdetails.setFcValidUpto(LocalDate.now().minusDays(1).plusYears(ValidityEnum.FCVALIDITY.getValidity()));
				fcdetails.setFcIssuedDate(LocalDateTime.now());
				fcdetails.setCreatedDate(LocalDateTime.now());
				fcdetails.setFcNumber(fcNumber);
				fcdetails.setTrNo(staginDto.getTrNo());
				fcdetails.setPrNo(staginDto.getPrNo());
				fcDetailsDAO.save(fcdetails);
			}
		} catch (Exception e) {
			logger.error("Error :{}", e.getMessage());
		}
	}

	public Map<String, Object> getMVIName(StagingRegistrationDetailsDTO stagingDetails) {

		Map<String, Object> returnDetails = new HashMap<String, Object>();
		returnDetails.put(RoleEnum.MVI.toString(), StringUtils.EMPTY);
		returnDetails.put("DATE", StringUtils.EMPTY);
		returnDetails.put("DATE1", StringUtils.EMPTY);

		try {
			List<FlowDTO> listFlowDTO = stagingDetails.getFlowDetailsLog();

			for (FlowDTO flowDTO : listFlowDTO) {

				Set<Integer> keys = flowDTO.getFlowDetails().keySet();

				for (Integer indx : keys) {
					List<RoleActionDTO> roleActions = flowDTO.getFlowDetails().get(indx);
					for (RoleActionDTO roles : roleActions) {
						if (roles.getRole().equals(RoleEnum.MVI.getName())
								&& (roles.getAction().equals(StatusRegistration.APPROVED.getDescription())
										|| roles.getAction().equals(StatusRegistration.REJECTED.getDescription()))) {
							returnDetails.put("MVI", roles.getActionBy());
							returnDetails.put("DATE", roles.getActionTime());
							return returnDetails;
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Unable to Find MVI details for FC : Application No [{}]", stagingDetails.getApplicationNo());
			return returnDetails;
		}

		logger.warn("Not Found MVI Details for FC , Application No [{}] ", stagingDetails.getApplicationNo());
		return returnDetails;
	}

	public boolean saveHSRPData(Optional<HsrpDetailDTO> hsrpDetailDtoOptional,
			StagingRegistrationDetailsDTO staginDto) {

		boolean result = false;

		try {
			if (hsrpDetailDtoOptional.isPresent()) {
				HsrpDetailDTO hsrpDetailDTO = hsrpDetailDtoOptional.get();
				hsrpDetailDTO.setPrNumber(staginDto.getPrNo());
				hsrpDetailDTO.setErrorFound(Boolean.FALSE);
				// TODO: need to confirm registration date capture.
				// DD/MM/YYYY
				hsrpDetailDTO.setRegDate(DateUtil.getFormattedDate(DateUtil.DATE_DD_MM_YYYY,
						staginDto.getRegistrationValidity().getPrGeneratedDate()));
				hsrpDetailDTO.setAuthorizationDate(DateUtil.getFormattedDate(DateUtil.DATE_DD_MM_YYYY,
						staginDto.getRegistrationValidity().getPrGeneratedDate()));
				hsrpDetailDAO.save(hsrpDetailDTO);
				result = true;
			} else {
				result = false;
			}
		} catch (Exception e) {

			logger.error(appMessages.getLogMessage(MessageKeys.UNABLE_TO_PROCESS_HSRP_UPDATION), "Error:  {} ",
					staginDto.getApplicationNo(), e.getMessage());
			// hsrpStatusDetails.setPrStatus(false);
		}
		return result;
	}

	public FlowDTO getBaseObj() {

		FlowDTO flowDto = new FlowDTO();

		Map<Integer, List<RoleActionDTO>> details = new TreeMap<Integer, List<RoleActionDTO>>();

		RoleActionDTO r1 = new RoleActionDTO();
		r1.setRole("CCO");
		// r1.setAction("APPROVED");

		RoleActionDTO r2 = new RoleActionDTO();
		r2.setRole("MVI");
		// r2.setAction("APPROVED");

		ArrayList<RoleActionDTO> al = new ArrayList<RoleActionDTO>();
		al.add(r1);
		al.add(r2);

		RoleActionDTO r3 = new RoleActionDTO();
		r3.setRole("AO");
		// r3.setAction("APPROVED");

		ArrayList<RoleActionDTO> a2 = new ArrayList<RoleActionDTO>();
		a2.add(r3);

		RoleActionDTO r4 = new RoleActionDTO();
		r4.setRole("RTO");
		// r4.setAction("APPROVED");

		ArrayList<RoleActionDTO> a3 = new ArrayList<RoleActionDTO>();
		a3.add(r4);

		details.put(1, al);
		details.put(2, a2);
		details.put(3, a3);

		flowDto.setFlowDetails(details);

		return flowDto;
	}
	// public List<RegistrationDetailsVO> get(String officeCode, String
	// userName,
	// String role) {

	// }

	@Override
	public RegistrationDetailsVO viewList(String officeCode, String userId, String applicationNo) {
		// TODO Auto-generated method stub

		return null;
	}

	/*
	 * public StagingRegistrationDetailsDTO staging() {
	 * StagingRegistrationDetailsDTO staging = new StagingRegistrationDetailsDTO();
	 * staging.setFlowDetails(getBaseObj()); return staging; }
	 */

	@Override
	public List<RoleActionDTO> getImageActions(String ApplicationNo) {
		List<RoleActionDTO> list = new ArrayList<>();
		Optional<StagingRegistrationDetailsDTO> stagingDto = stagingRegistrationDetailsDAO
				.findByApplicationNo(ApplicationNo);
		if (!stagingDto.isPresent()) {
			logger.debug(appMessages.getLogMessage(MessageKeys.REG_NO_RECORDS_FOUND + "[{}]"), ApplicationNo);
			logger.error(appMessages.getResponseMessage(MessageKeys.NO_AUTHORIZATION + "[{}]"), ApplicationNo);
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.NO_AUTHORIZATION));
		}
		Integer iteration = stagingDto.get().getIteration();
		List<FlowDTO> flowList = stagingDto.get().getFlowDetailsLog();

		for (FlowDTO flowDto : flowList) {
			Map<Integer, List<RoleActionDTO>> map = flowDto.getFlowDetails();
			Set<Integer> keys = map.keySet();
			for (Integer key : keys) {
				List<RoleActionDTO> roleActionList = map.get(key);
				List<RoleActionDTO> filteredList = roleActionList.stream()
						.filter(val -> val.getIteration().equals(iteration)).collect(Collectors.toList());
				list.addAll(filteredList);

			}
		}
		return list;

	}

	@Override
	public BaseRegistrationDetailsDTO createBaseRegistrationDetailsDTODummyData() {
		BaseRegistrationDetailsDTO baseRegistrationDetailsDTO = new BaseRegistrationDetailsDTO();
		baseRegistrationDetailsDTO.setApplicationNo("AP12345");

		ApplicantDetailsDTO applicantDetailsDTO = new ApplicantDetailsDTO();
		applicantDetailsDTO.setApplicantNo("AP1234");
		applicantDetailsDTO.setDisplayName("Ashok K");
		applicantDetailsDTO.setFirstName("Ashok");
		applicantDetailsDTO.setLastName("Trivedi");
		applicantDetailsDTO.setMiddleName("Kumar");
		applicantDetailsDTO.setFatherName("Rahul");
		ContactDTO contactDTO = new ContactDTO();
		contactDTO.setMobile("8339937778");
		contactDTO.setPhone("0674254628");
		contactDTO.setAlternateMobile("8339937778");
		contactDTO.setEmail("example@gmail.com");
		applicantDetailsDTO.setContact(contactDTO);
		applicantDetailsDTO.setIsDifferentlyAbled(true);
		ApplicantAddressDTO applicantAddressDTO = new ApplicantAddressDTO();
		applicantAddressDTO.setType("Home");
		applicantAddressDTO.setDoorNo("C-121");
		applicantAddressDTO.setStreetName("James Street");
		applicantAddressDTO.setTownOrCity("Koti");
		applicantDetailsDTO.setPresentAddress(applicantAddressDTO);
		MandalDTO mandalDTO = new MandalDTO();
		mandalDTO.setMandalCode(1234);
		mandalDTO.setMandalName("Guntur");
		mandalDTO.setDistrictId(345);
		mandalDTO.setMandalId("AP789");
		mandalDTO.setOfficeCode("AP3344");
		mandalDTO.setNonTransportOffice("NT-vizag");
		mandalDTO.setTransportOfice("Tv-izag");
		// mandalDTO.setHsrpOffice("vizag");
		mandalDTO.setStatus("OK");
		applicantAddressDTO.setMandal(mandalDTO);
		DistrictDTO districtDTO = new DistrictDTO();
		districtDTO.setDistricCode(934);
		districtDTO.setDistrictId(5);
		districtDTO.setDistrictName("Nellore");
		districtDTO.setStateId("AP");
		districtDTO.setStatus("OK");
		districtDTO.setZonecode("05-Nellore");
		applicantAddressDTO.setDistrict(districtDTO);
		StateDTO stateDTO = new StateDTO();
		stateDTO.setCountryId("IND");
		stateDTO.setStateId("AP");
		stateDTO.setStateName("AP");
		stateDTO.setStateStatus("OK");
		applicantAddressDTO.setState(stateDTO);
		PostOfficeDTO postOfficeDTO = new PostOfficeDTO();
		postOfficeDTO.setDistrict(456);
		postOfficeDTO.setPostOfficeCode(567);
		postOfficeDTO.setPostOfficeId(123);
		postOfficeDTO.setPostOfficeName("PostName");
		postOfficeDTO.setStatus(true);
		applicantAddressDTO.setPostOffice(postOfficeDTO);
		VillageDTO villageDTO = new VillageDTO();
		villageDTO.setMandalId(456);
		villageDTO.setStatus("true");
		villageDTO.setVillageCode(77);
		villageDTO.setVillageId(88);
		villageDTO.setVillageName("VillageName");
		applicantAddressDTO.setVillage(villageDTO);
		CountryDTO countryDTO = new CountryDTO();
		countryDTO.setCountryCode("IND");
		countryDTO.setCountryId(999);
		countryDTO.setCountryName("IND");
		countryDTO.setCountryStatus(true);
		applicantAddressDTO.setCountry(countryDTO);
		applicantAddressDTO.setIsMigrated(false);
		/* applicantDetailsDTO.setPresentAddress(applicantAddressDTO); */
		AadhaarDetailsResponseDTO aadhaarDetailsResponseDTO = new AadhaarDetailsResponseDTO();
		aadhaarDetailsResponseDTO.setKSA_KUA_Txn("kSA_KUA_Txn");
		aadhaarDetailsResponseDTO.setAuth_err_code("auth_err_code");
		aadhaarDetailsResponseDTO.setAuth_date("auth_date");
		aadhaarDetailsResponseDTO.setAuth_status("auth_status");
		aadhaarDetailsResponseDTO.setAuth_transaction_code("auth_transaction_code");
		aadhaarDetailsResponseDTO.setBase64file("base64file");
		aadhaarDetailsResponseDTO.setCo("co");
		aadhaarDetailsResponseDTO.setDistrict("district");
		aadhaarDetailsResponseDTO.setDistrict_name("district_name");
		aadhaarDetailsResponseDTO.setDob("dob");
		aadhaarDetailsResponseDTO.setGender("Male");
		aadhaarDetailsResponseDTO.setHouse("house");
		aadhaarDetailsResponseDTO.setLc("lc");
		aadhaarDetailsResponseDTO.setMandal("mandal");
		aadhaarDetailsResponseDTO.setMandal_name("mandal_name");
		aadhaarDetailsResponseDTO.setName("name");
		aadhaarDetailsResponseDTO.setPincode("pincode");
		aadhaarDetailsResponseDTO.setPo("po");
		aadhaarDetailsResponseDTO.setStatecode("statecode");
		aadhaarDetailsResponseDTO.setStreet("street");
		aadhaarDetailsResponseDTO.setSubdist("subdist");
		// aadhaarDetailsResponseDTO.setUid();
		aadhaarDetailsResponseDTO.setVillage("village");
		aadhaarDetailsResponseDTO.setVillage_name("village_name");
		aadhaarDetailsResponseDTO.setVtc("vtc");
		aadhaarDetailsResponseDTO.setInsideAP(true);
		aadhaarDetailsResponseDTO.setStateMatched(true);
		aadhaarDetailsResponseDTO.setDistrictMatched(false);
		aadhaarDetailsResponseDTO.setMandalMatched(false);
		aadhaarDetailsResponseDTO.setStateMatchedCode("stateMatchedCode");
		aadhaarDetailsResponseDTO.setDistrictMatchedCode("districtMatchedCode");
		aadhaarDetailsResponseDTO.setMandalMatchedCode("mandalMatchedCode");
		aadhaarDetailsResponseDTO.setAge(34);
		aadhaarDetailsResponseDTO.setApplicationNumber("applicationNumber");
		aadhaarDetailsResponseDTO.setFirstName("firstName");
		aadhaarDetailsResponseDTO.setLastName("lastName");
		aadhaarDetailsResponseDTO.setNationality("nationality");
		aadhaarDetailsResponseDTO.setDoorNo("doorNo");
		aadhaarDetailsResponseDTO.setCountry("country");
		aadhaarDetailsResponseDTO.setPhone("phone");
		// aadhaarDetailsResponseDTO.setUuId();
		applicantDetailsDTO.setAadharResponse(aadhaarDetailsResponseDTO);
		applicantDetailsDTO.setIsAadhaarValidated(true);
		ApplicationTypeDTO applicationTypeDTO = new ApplicationTypeDTO();
		applicationTypeDTO.setType("type");
		applicantDetailsDTO.setApplicantionType(applicationTypeDTO);
		applicantDetailsDTO.setStatus("status");
		QualificationDTO qualificationDTO = new QualificationDTO();
		qualificationDTO.setStatus(true);
		// qualificationDTO.setCreateDate("createDate");
		qualificationDTO.setCreatedBy("createdBy");
		qualificationDTO.setModifiedBy("modifiedBy");
		// qualificationDTO.setModifiedDate("modifiedDate");
		qualificationDTO.setCode(200);
		qualificationDTO.setDescription("description");
		qualificationDTO.setSlNo(555);
		applicantDetailsDTO.setQualification(qualificationDTO);
		BloodGroupDTO bloodGroupDTO = new BloodGroupDTO();
		bloodGroupDTO.setSlNo("slNo");
		bloodGroupDTO.setBloodGrpName("B");
		bloodGroupDTO.setActive(true);
		applicantDetailsDTO.setBloodGrp(bloodGroupDTO);
		applicantDetailsDTO.setNationality("INDIAN");
		applicantDetailsDTO.setSameAsAadhar(true);
		applicantDetailsDTO.setAadharNo("aadharNo");
		applicantDetailsDTO.setGender("Male");
		// applicantDetailsDTO.setDateOfBirth("dateOfBirth");

		/* applicantDetailsDTO.setEnclosures(enclosures); */
		applicantDetailsDTO.setIsAvailablePresentAddrsProof(true);
		applicantDetailsDTO.setPresentAddrsProofBelongsTo("presentAddrsProofBelongsTo");
		applicantDetailsDTO.setNameOfPresentAddrsProofBelongsTo("nameOfPresentAddrsProofBelongsTo");
		baseRegistrationDetailsDTO.setApplicantDetails(applicantDetailsDTO);
		baseRegistrationDetailsDTO.setApplicationStatus("OK");
		VahanDetailsDTO vahanDetailsDTO = new VahanDetailsDTO();
		vahanDetailsDTO.setRegistrationNumber("registrationNumber");
		vahanDetailsDTO.setChassisNumber("chassisNumber");
		vahanDetailsDTO.setEngineNumber("engineNumber");
		vahanDetailsDTO.setOwnerName("ownerName");
		vahanDetailsDTO.setName("name");
		vahanDetailsDTO.setModel("model");
		// vahanDetailsDTO.setPermanentAddress("permanentAddress");
		vahanDetailsDTO.setColor("color");
		vahanDetailsDTO.setManufacturedMonthYear("manufacturedMonthYear");
		vahanDetailsDTO.setVehicleClass("vehicleClass");
		vahanDetailsDTO.setMakersModel("makersModel");
		vahanDetailsDTO.setSeatingCapacity("seatingCapacity");
		vahanDetailsDTO.setStatusMessage("statusMessage");
		vahanDetailsDTO.setRegistrationDate("registrationDate");
		vahanDetailsDTO.setOwnerSr("ownerSr");
		vahanDetailsDTO.setFirstName("firstName");
		vahanDetailsDTO.setPresentAddress("presentAddress");
		vahanDetailsDTO.setBodyTypeDesc("bodyTypeDesc");
		vahanDetailsDTO.setFitUpto("fitUpto");
		vahanDetailsDTO.setTaxUpto("taxUpto");
		vahanDetailsDTO.setFinancer("financer");
		vahanDetailsDTO.setInsuranceCompany("insuranceCompany");
		vahanDetailsDTO.setInsurancePolicyNumber("insurancePolicyNumber");
		vahanDetailsDTO.setInsuranceUpto("insuranceUpto");
		vahanDetailsDTO.setNoCyl("noCyl");
		vahanDetailsDTO.setCubicCapacity("cubicCapacity");
		vahanDetailsDTO.setSleeperCapacity("sleeperCapacity");
		vahanDetailsDTO.setStandCapacity("standCapacity");
		vahanDetailsDTO.setRegisteredAt("registeredAt");
		vahanDetailsDTO.setStatusAsOn("statusAsOn");
		// vahanDetailsDTO.setExShowroomPrice("exShowroomPrice");
		vahanDetailsDTO.setUsed(true);
		vahanDetailsDTO.setApplicationNo(false);
		vahanDetailsDTO.setEnginePower(12000.0);
		vahanDetailsDTO.setFrontAxleDesc("frontAxleDesc");
		vahanDetailsDTO.setFrontAxleWeight(777777);
		vahanDetailsDTO.setFuelDesc("fuelDesc");
		vahanDetailsDTO.setGvw(9999);
		vahanDetailsDTO.setHeight(7675.2);
		vahanDetailsDTO.setLength(7886.3);
		vahanDetailsDTO.setMakersDesc("makersDesc");
		vahanDetailsDTO.setPollutionNormsDesc("pollutionNormsDesc");
		vahanDetailsDTO.setO1AxleDesc("o1AxleDesc");
		vahanDetailsDTO.setO1AxleWeight(56764);
		vahanDetailsDTO.setO2AxleDesc("o2AxleDesc");
		vahanDetailsDTO.setO2AxleWeight(3354435);
		vahanDetailsDTO.setO3AxleDesc("o3AxleDesc");
		vahanDetailsDTO.setO3AxleWeight(9878);
		vahanDetailsDTO.setO4AxleDesc("o4AxleDesc");
		vahanDetailsDTO.setO4AxleWeight(3654354);
		vahanDetailsDTO.setO5AxleDesc("o5AxleDesc");
		vahanDetailsDTO.setO5AxleWeight(35435);
		vahanDetailsDTO.setRearAxleDesc("rearAxleDesc");
		vahanDetailsDTO.setRearAxleWeight(5435423);
		vahanDetailsDTO.setUnladenWeight(798986);
		vahanDetailsDTO.setWheelbase(54354354);
		vahanDetailsDTO.setWidth(7867868.6);
		vahanDetailsDTO.setTandemAxelDescp("tandemAxelDescp");
		vahanDetailsDTO.setTandemAxelWeight(654674677);
		baseRegistrationDetailsDTO.setVahanDetails(vahanDetailsDTO);
		PanDetailsDTO panDetailsDTO = new PanDetailsDTO();
		panDetailsDTO.setPanNo("panNo");
		panDetailsDTO.setPanVerifiedInOnlne(false);
		baseRegistrationDetailsDTO.setPanDetails(panDetailsDTO);

		FinanceDetailsDTO financeDetailsDTO = new FinanceDetailsDTO();
		financeDetailsDTO.setApplicationNo("applicationNo");
		financeDetailsDTO.setToken("token");
		// financeDetailsDTO.setTokenGeneratedTime("tokenGeneratedTime");
		financeDetailsDTO.setAadharResponse(aadhaarDetailsResponseDTO);
		financeDetailsDTO.setFinancerName("financerName");
		financeDetailsDTO.setSanctionedAmount(678575.4363);
		financeDetailsDTO.setIntrest(86868);
		financeDetailsDTO.setStatus("status");
		financeDetailsDTO.setUserId("userId");
		// financeDetailsDTO.setLastUpdated("lastUpdated");
		baseRegistrationDetailsDTO.setFinanceDetails(financeDetailsDTO);
		InsuranceDetailsDTO insuranceDetailsDTO = new InsuranceDetailsDTO();
		insuranceDetailsDTO.setId("id");
		insuranceDetailsDTO.setCompany("company");
		insuranceDetailsDTO.setPolicyType("policyType");
		// insuranceDetailsDTO.setPolicyNumber(7857657);
		insuranceDetailsDTO.setTenure(678698);
		// insuranceDetailsDTO.setValidTill("validTill");
		baseRegistrationDetailsDTO.setInsuranceDetails(insuranceDetailsDTO);
		baseRegistrationDetailsDTO.setAssigned(true);
		baseRegistrationDetailsDTO.setTrNo("trNo");
		baseRegistrationDetailsDTO.setPrNo("prNo");
		baseRegistrationDetailsDTO.setStageNo(4);
		baseRegistrationDetailsDTO.setIteration(3);
		ActionDetailsDTO actionDetailsDTO = new ActionDetailsDTO();
		actionDetailsDTO.setActionBy("actionBy");
		actionDetailsDTO.setModule("module");
		actionDetailsDTO.setReferenceNumber("referenceNumber");
		actionDetailsDTO.setIteration("iteration");
		// actionDetailsDTO.setActionDatetime("actionDatetime");
		actionDetailsDTO.setReason("reason");
		// actionDetailsDTO.setActionByRole("actionByRole"); //List<>
		baseRegistrationDetailsDTO.setActionDetails(actionDetailsDTO);
		List<ActionDetailsDTO> actionDetailsDTOList = new ArrayList<ActionDetailsDTO>();
		actionDetailsDTOList.add(actionDetailsDTO);
		FlowDTO flowDTO = new FlowDTO();
		flowDTO.setFlowId("flowId");
		flowDTO.setStatus(true);
		// flowDTO.setFlowDetails("flowDetails"); //TODO
		baseRegistrationDetailsDTO.setFlowDetails(flowDTO);
		List<FlowDTO> flowDTOList = new ArrayList<>();
		flowDTOList.add(flowDTO);
		LockedDetailsDTO lockedDetailsDTO = new LockedDetailsDTO();
		lockedDetailsDTO.setIterationNo(3);
		lockedDetailsDTO.setApplicatioNo("applicatioNo");
		lockedDetailsDTO.setModule("module");
		lockedDetailsDTO.setLockedBy("lockedBy");
		lockedDetailsDTO.setLockedByRole("lockedByRole");
		// lockedDetailsDTO.setLockedDate("lockedDate");
		// lockedDetailsDTO.setLockedDate("lockedDate");
		baseRegistrationDetailsDTO.setVehicleType("Commercial");
		baseRegistrationDetailsDTO.setIsTrailer(true);
		// baseRegistrationDetailsDTO.setAadharNo("6756745785785");
		// baseRegistrationDetailsDTO.setChassisNumber("6567AJH786");
		// baseRegistrationDetailsDTO.setEngineNumber("AP6576576DF");
		// baseRegistrationDetailsDTO.setMakersName("makersName");
		// baseRegistrationDetailsDTO.setMakerClass("makerClass");
		// baseRegistrationDetailsDTO.setDealerSelectedMakerName("Hero");
		// baseRegistrationDetailsDTO.setDealerSelectedMakerClass("Maruti
		// Suzuki");
		baseRegistrationDetailsDTO.setClassOfVehicle("ClassOfVehicle");
		baseRegistrationDetailsDTO.setTaxType("Quarterly");
		// baseRegistrationDetailsDTO.setBodyType("saloon");
		// baseRegistrationDetailsDTO.setManufacturedMonthYear("Aug-2017");
		// baseRegistrationDetailsDTO.setSeatingCapacity(9);
		// baseRegistrationDetailsDTO.setRlw(4345);
		// baseRegistrationDetailsDTO.setUlw(7564);
		InvoiceDetailsDTO invoiceDetailsDTO = new InvoiceDetailsDTO();
		invoiceDetailsDTO.setInvoiceNo("invoiceNo");
		// invoiceDetailsDTO.setInvoiceDate("invoiceDate");
		invoiceDetailsDTO.setInvoiceValue(67756.78);
		baseRegistrationDetailsDTO.setInvoiceDetails(invoiceDetailsDTO);
		OfficeDTO OfficeDTO = new OfficeDTO();
		OfficeDTO.setOfficeId(7688);
		OfficeDTO.setOfficeCode("officeCode");
		OfficeDTO.setOfficeName("officeName");
		OfficeDTO.setOfficeAddress1("officeAddress1");
		OfficeDTO.setOfficeAddress2("officeAddress2");
		OfficeDTO.setOfficeCity("officeCity");
		OfficeDTO.setOfficeVillage(67868);
		OfficeDTO.setOfficeMandal("officeMandal");
		OfficeDTO.setOfficeDist(86);
		OfficeDTO.setType("officeType");
		OfficeDTO.setOffice("office");
		OfficeDTO.setDdoCode("officeDdo");
		OfficeDTO.setOfficeGeocode("officeGeocode");
		OfficeDTO.setLongitude("longitude");
		OfficeDTO.setLatitude("latitude");
		OfficeDTO.setStatus(200);
		// OfficeDTO.setCreatedDate("createdDate");
		// OfficeDTO.setLupdate("lupdate");
		baseRegistrationDetailsDTO.setOfficeDetails(OfficeDTO);
		DealerDetailsDTO dealerDetailsDTO = new DealerDetailsDTO();
		dealerDetailsDTO.setDealerId("dealerId");
		dealerDetailsDTO.setDealerName("dealerName");
		dealerDetailsDTO.setMakersName("makersName");
		dealerDetailsDTO.setMakerClass("makerClass");
		dealerDetailsDTO.setDealerSelectedMakerName("dealerSelectedMakerName");
		dealerDetailsDTO.setDealerSelectedMakerClass("dealerSelectedMakerClass");
		baseRegistrationDetailsDTO.setDealerDetails(dealerDetailsDTO);
		baseRegistrationDetailsDTO.setTaxAmount(27358236L);
		baseRegistrationDetailsDTO.setIsFinancier(false);
		// baseRegistrationDetailsDTO.setTaxvalidity("taxvalidity");
		RejectionHistoryDTO rejectionHistoryDTO = new RejectionHistoryDTO();
		rejectionHistoryDTO.setApplicationNo("applicationNo");
		rejectionHistoryDTO.setModule("module");
		// rejectionHistoryDTO.setActionTime("actionTime");
		rejectionHistoryDTO.setIsSecondVehicleRejected(true);
		rejectionHistoryDTO.setSecondVehicleNotRejectedReason("secondVehicleRejectedReason");
		rejectionHistoryDTO.setIsInvalidVehicleRejection(false);
		rejectionHistoryDTO.setInvalidVehicleRejectionReason("invalidVehicleRejectionReason");
		rejectionHistoryDTO.setIsDocumentRejected(false);
		rejectionHistoryDTO.setDocumentRejectedReason("documentRejectedReason");
		rejectionHistoryDTO.setIterationNo(5745);
		rejectionHistoryDTO.setClassOfVehicle("classOfVehicle");
		baseRegistrationDetailsDTO.setRejectionHistory(rejectionHistoryDTO);
		baseRegistrationDetailsDTO.setCesFee(8678L);
		// baseRegistrationDetailsDTO.setCesValidity("cesValidity");

		return baseRegistrationDetailsDTO;
	}

	@Override
	public Optional<List<MasterCovVO>> findInvalidCovs(String cov) {

		Optional<List<MasterCovDTO>> masterCovList = masterCovDAO.findByInvalidCov(Boolean.TRUE);
		if (masterCovList.isPresent() && !masterCovList.get().isEmpty()) {
			List<MasterCovVO> voList = masterCovMapper.convertEntity(masterCovList.get());
			return Optional.of(voList);
		}
		return Optional.empty();
	}

	private void sendNotifications(Integer templateId, StagingRegistrationDetailsDTO staginDto) {

		try {
			if (staginDto != null) {

				notifications.sendEmailNotification(notificationTemplate::fillTemplate, templateId, staginDto,
						staginDto.getApplicantDetails().getContact().getEmail());
				notifications.sendMessageNotification(notificationTemplate::fillTemplate, templateId, staginDto,
						staginDto.getApplicantDetails().getContact().getMobile());
			}

		} catch (IOException e) {
			logger.error("Failed to send notifications for template id: {}; {}", templateId, e);
		}

	}

	private void sendNotificationsForActions(Integer templateId, RegistrationDetailsDTO regDto) {

		try {
			if (regDto != null) {

				notifications.sendEmailNotification(notificationTemplate::fillTemplate, templateId, regDto,
						regDto.getApplicantDetails().getContact().getEmail());
				notifications.sendMessageNotification(notificationTemplate::fillTemplate, templateId, regDto,
						regDto.getApplicantDetails().getContact().getMobile());
			}

		} catch (IOException e) {
			logger.error("Failed to send notifications for template id: {}; {}", templateId, e);
		}

	}

	@Override
	public Optional<CitizenDashBordDetails> getCitizenDashBoardDetails(String officeCode, String user, String role) {

		logger.info("citizen services dashboard for user [{}]", user);
		CitizenDashBordDetails dashBordDetails = new CitizenDashBordDetails();
		List<ServiceWisePendingCountVO> vo = new ArrayList<>();
		PendingCountVo pendingCountVo = new PendingCountVo();
		ServiceWisePendingCountVO serviceWisePendingCountVO = new ServiceWisePendingCountVO();
		Integer total = 0;
		Integer nonTransport = 0;
		Integer transport = 0;
		Integer fcOtherStation = 0;
		List<RegServiceDTO> list = null;
		// List<RegServiceDTO> list =
		// getRegistrationServicesPendingRecords(role, officeCode, null);
		// logger.info("citizen services pending count [{}] at dashboard",
		// list.size());
		// List<RegServiceVO> registrationDetailsList = new ArrayList<>();
		// Set<ServiceEnum> serviceIds = new HashSet<>();
		/*
		 * for (RegServiceDTO regServiceDto : list) {
		 * registrationDetailsList.add(regServiceMapper.limitedDashBoardfields(
		 * regServiceDto)); List<ServiceEnum> serviceIdCode =
		 * regServiceDto.getServiceIds().stream() .map(id ->
		 * ServiceEnum.getServiceEnumById(id)).collect(Collectors.toList());
		 * serviceIds.addAll(serviceIdCode); }
		 * 
		 */
		if (RoleEnum.RTO.getName().equals(role) || RoleEnum.AO.getName().equals(role)) {
			List<RegServiceDTO> osNocCount = regServiceDAO.findByOfficeCodeAndApplicationStatusAndOtherStateNOCStatus(
					officeCode, StatusRegistration.APPROVED, StatusRegistration.NOCVERIFICATIONPENDING);
			pendingCountVo.setOsNocPendingCount(0);
			if (CollectionUtils.isNotEmpty(osNocCount)) {
				pendingCountVo.setOsNocPendingCount(osNocCount.size());
			}
		}
		if (RoleEnum.MVI.getName().equals(role)) {

			List<RegServiceDTO> listodServices = regServiceDAO
					.findByMviOfficeCodeAndCurrentRolesInAndSourceIsNull(officeCode, Arrays.asList(role));

			List<Integer> serviceIds = Arrays.asList(ServiceEnum.VEHICLESTOPPAGE.getId(),
					ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId());
			if (listodServices != null && !listodServices.isEmpty()) {
				list = listodServices.stream()
						.filter(reg -> serviceIds.stream().anyMatch(sid -> reg.getServiceIds().contains(sid)))
						.collect(Collectors.toList());
			}

		} else {
			list = regServiceDAO.findTop70ByOfficeCodeAndCurrentRolesInAndSourceIsNull(officeCode, Arrays.asList(role));
		}
		if (list != null && !list.isEmpty()) {

			List<RegServiceDTO> regServiceDTOList = new ArrayList<>();
			regServiceDTOList = list.parallelStream()
					.filter(val -> null != val.getActionDetails() && val.getActionDetails().stream()
							.anyMatch(action -> role.equals(action.getRole()) && !action.getIsDoneProcess()))
					.collect(Collectors.toList());

			for (RegServiceDTO dto : regServiceDTOList) {

				if (dto.getRegistrationDetails() != null
						&& StringUtils.isNoneBlank(dto.getRegistrationDetails().getVehicleType())
						&& dto.getRegistrationDetails().getVehicleType().equalsIgnoreCase(CovCategory.T.getCode())) {
					if (dto.getServiceIds() != null && !dto.getServiceIds().isEmpty() && dto.getServiceIds().stream()
							.anyMatch(id -> id.equals(ServiceEnum.OTHERSTATIONFC.getId()))) {
						fcOtherStation++;
					} else {
						transport++;
					}
				}
				if (dto.getRegistrationDetails() != null
						&& StringUtils.isNoneBlank(dto.getRegistrationDetails().getVehicleType())
						&& dto.getRegistrationDetails().getVehicleType().equalsIgnoreCase(CovCategory.N.getCode())) {
					nonTransport++;
				}
				total = transport + nonTransport + fcOtherStation;
			}

			/*
			 * List<RegServiceVO> regServicesVo = new ArrayList<>(); for (ServiceEnum
			 * serviceEnum : serviceIds) { Integer nonTransport = 0; Integer transport = 0;
			 * 
			 * ServiceWisePendingCountVO serviceWisePendingCountVO = new
			 * ServiceWisePendingCountVO(); PendingCountVo pendingCountVo = new
			 * PendingCountVo(); registrationDetailsList.parallelStream().forEach(record ->
			 * { if (record.getServiceIds().stream().anyMatch(s ->
			 * s.equals(serviceEnum.getId()))) { regServicesVo.add(record); } }); try { for
			 * (RegServiceVO regServicesDetails : regServicesVo) { appNo =
			 * regServicesDetails.getApplicationNo(); if
			 * (regServicesDetails.getRegistrationDetails() != null &&
			 * regServicesDetails.getRegistrationDetails().getVehicleType() != null &&
			 * regServicesDetails.getRegistrationDetails().getVehicleType()
			 * .equals(CovCategory.T.getCode())) { transport++; } if
			 * (regServicesDetails.getRegistrationDetails() != null &&
			 * regServicesDetails.getRegistrationDetails().getVehicleType() != null &&
			 * regServicesDetails.getRegistrationDetails().getVehicleType()
			 * .equals(CovCategory.N.getCode())) { nonTransport++; } }
			 * 
			 * } catch (Exception e) {
			 * logger.error("exception occured for appno [{}] , message [{}]", appNo,
			 * e.getMessage()); }
			 * 
			 * total = transport + nonTransport; pendingCountVo.setTotalCount(total);
			 * pendingCountVo.setTransportCount(transport);
			 * pendingCountVo.setNonTransportCount(nonTransport);
			 * serviceWisePendingCountVO.setPendingCountVo(pendingCountVo);
			 * serviceWisePendingCountVO.setService(serviceEnum.getCode());
			 * serviceWisePendingCountVO.setServiceId(serviceEnum.getId());
			 * serviceWisePendingCountVO.setServiceDescripion(serviceEnum. getDesc());
			 * vo.add(serviceWisePendingCountVO); regServicesVo.clear(); }
			 * dashBordDetails.setServiceWisePendingCountVO(vo); total =
			 * registrationDetailsList.size();
			 * logger.info("registrationServices count [{}]", total);
			 */

		} else {
			logger.info("no records");
			logger.info(MessageKeys.MESSAGE_NO_RECORD_FOUND + "[{}]", list);
		}
		pendingCountVo.setTotalCount(total);
		pendingCountVo.setTransportCount(transport);
		pendingCountVo.setNonTransportCount(nonTransport);
		pendingCountVo.setFcOtherStationCount(fcOtherStation);
		serviceWisePendingCountVO.setPendingCountVo(pendingCountVo);
		vo.add(serviceWisePendingCountVO);
		dashBordDetails.setServiceWisePendingCountVO(vo);
		logger.info("dashBoard total count [{}]", total);
		dashBordDetails.setServicesTotalPendingCount(total);
		return Optional.of(dashBordDetails);
	}

	@Override
	public List<RegServiceVO> getServicesPendingList(String officeCode, String user, String role, String service,
			String vehicleType, String fc,Boolean isDataEntry) {
		List<RegServiceDTO> toBeAssignedList = new ArrayList<>();
		List<LockedDetailsDTO> lockedDetailsLog = new ArrayList<>();
		List<RegServiceVO> returnList = new ArrayList<>();
		List<RegServiceDTO> pending = new ArrayList<>();
		// Integer serviceId = ServiceEnum.getServiceEnum(service).getId();

		final String localOfficeCode = officeCode;

		synchronized (localOfficeCode.intern()) {

			List<RegServiceDTO> regServiceDTOList = regServiceDAO
					.findByLockedDetailsLockedByAndLockedDetailsLockedByRole(user, role);

			if (regServiceDTOList != null && !regServiceDTOList.isEmpty()) {
				/*
				 * Optional<RegServiceDTO> regServiceDTOOptinal = regServiceDTOList.stream()
				 * .filter(s -> s.getServiceIds().contains(serviceId)).findFirst();
				 */
				for (RegServiceDTO dto : regServiceDTOList) {
					if (dto.getCreatedDate() == null) {
						dto.setCreatedDate(LocalDateTime.now());
					}
				}
				regServiceDTOList.sort((p1, p2) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
				// Optional<RegServiceDTO> regServiceDTOOptinal =
				// regServiceDTOList.stream().findFirst();
				if(isDataEntry) {
					regServiceDTOList = regServiceDTOList.stream()
							.filter(a -> a.getServiceIds().contains(ServiceEnum.DATAENTRY.getId()))
							.collect(Collectors.toList());
					
				}
				for (RegServiceDTO regServiceDTOOptinal : regServiceDTOList) {
					if (!isDataEntry && regServiceDTOOptinal.getServiceIds().stream()
							.anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))) {
						continue;
					}
					if (regServiceDTOOptinal.getServiceIds().stream()
							.anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId())
									|| id.equals(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId()))) {
						continue;
					}
					if (regServiceDTOOptinal.getServiceIds().stream()
							.anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId())
									|| id.equals(ServiceEnum.RCCANCELLATION.getId()))
							&& StringUtils.isNoneBlank(service) && (service.equals(ServiceEnum.RCFORFINANCE.getCode())
									|| service.equals(ServiceEnum.RCCANCELLATION.getCode()))) {
						vehicleType = setVehicleTypeCode(regServiceDTOOptinal);
					}

					if (regServiceDTOOptinal.getRegistrationDetails() != null
							&& regServiceDTOOptinal.getRegistrationDetails().getVehicleType() != null
							&& regServiceDTOOptinal.getRegistrationDetails().getVehicleType()
									.equalsIgnoreCase(vehicleType)) {
						/*
						 * if (StringUtils.isNoneBlank(fc) &&
						 * ServiceEnum.OTHERSTATIONFC.getCode().equalsIgnoreCase(fc) &&
						 * regServiceDTOOptinal.getServiceIds().stream() .anyMatch(id ->
						 * id.equals(ServiceEnum.OTHERSTATIONFC.getId()))) {
						 * logger.info("ApplicationNo [{}] lockedBy [{}]",
						 * regServiceDTOOptinal.getApplicationNo(), user);
						 * returnList.add(regServiceMapper.limitedDashBoardfields(regServiceDTOOptinal))
						 * ; return returnList; } else
						 */if (regServiceDTOOptinal.getServiceIds().stream()
								.noneMatch(id -> /*
													 * id.equals(ServiceEnum.OTHERSTATIONFC.getId()) ||
													 */id.equals(ServiceEnum.RCFORFINANCE.getId())
										|| id.equals(ServiceEnum.RCCANCELLATION.getId()))) {
							logger.info("ApplicationNo [{}] lockedBy [{}]", regServiceDTOOptinal.getApplicationNo(),
									user);
							if (regServiceDTOOptinal.getServiceType() != null
									&& regServiceDTOOptinal.getServiceType().stream()
											.anyMatch(id -> id.equals(ServiceEnum.DATAENTRY))
									&& !regServiceDTOOptinal.getRegistrationDetails().isRegVehicleWithPR()) {
								Optional<RegServiceDTO> regService = regServiceDAO
										.findByApplicationNo(regServiceDTOOptinal.getApplicationNo());
								if (regService.isPresent()) {
									regServiceDTOOptinal.getRegistrationDetails().setApplicantDetails(
											regService.get().getRegistrationDetails().getApplicantDetails());
									regServiceDTOOptinal.getRegistrationDetails()
											.setTrNo(regService.get().getRegistrationDetails().getTrNo());
								}
							}
							
							if (isDataEntry
									&& regServiceDTOOptinal.getServiceIds().contains(ServiceEnum.DATAENTRY.getId())) {
								returnList.add(regServiceMapper.limitedDashBoardfields(regServiceDTOOptinal));
								return returnList;
							} else {
								returnList.add(regServiceMapper.limitedDashBoardfields(regServiceDTOOptinal));
								return returnList;
							}
							
						} else if (regServiceDTOOptinal.getServiceIds().stream().anyMatch(
								id -> id.equals(ServiceEnum.RCFORFINANCE.getId())) && StringUtils.isNoneBlank(service)
								&& ServiceEnum.RCFORFINANCE.getCode().equalsIgnoreCase(service)
								&& regServiceDTOOptinal.getServiceIds().stream()
										.anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId()))) {
							logger.info("ApplicationNo [{}] lockedBy [{}]", regServiceDTOOptinal.getApplicationNo(),
									user);
							returnList.add(regServiceMapper.limitedDashBoardfields(regServiceDTOOptinal));
							return returnList;
						} else if (regServiceDTOOptinal.getServiceIds().stream()
								.anyMatch(id -> id.equals(ServiceEnum.RCCANCELLATION.getId()))
								&& StringUtils.isNoneBlank(service)
								&& ServiceEnum.RCCANCELLATION.getCode().equalsIgnoreCase(service)
								&& regServiceDTOOptinal.getFlowId() != null
								&& regServiceDTOOptinal.getFlowId().equals(Flow.RCCANCELLATIONCCO)
								&& regServiceDTOOptinal.getServiceIds().stream()
										.anyMatch(id -> id.equals(ServiceEnum.RCCANCELLATION.getId()))) {
							logger.info("ApplicationNo [{}] lockedBy [{}]", regServiceDTOOptinal.getApplicationNo(),
									user);
							returnList.add(regServiceMapper.limitedDashBoardfields(regServiceDTOOptinal));
							return returnList;
						}
					}
				}
			}

			List<RegServiceDTO> pendingList = getRegistrationServicesPendingRecords(role, officeCode, null,isDataEntry);
			if (!CollectionUtils.isEmpty(pendingList)) {
				for (RegServiceDTO dto : pendingList) {
					if (dto.getCreatedDate() == null) {
						dto.setCreatedDate(LocalDateTime.now());
					}
				}
				pendingList.sort((o1, o2) -> o1.getCreatedDate().compareTo(o2.getCreatedDate()));

				pending = pendingList.stream()
						.filter((val -> val.getLockedDetails() == null || (val.getLockedDetails() != null && (val
								.getLockedDetails().stream().allMatch(data -> !data.getLockedByRole().equals(role))))))
						.collect(Collectors.toList());
				int assignedCount = 0;
				for (RegServiceDTO regDtoDetais : pending) {
					Optional<RegServiceDTO> regDtoOptional = regServiceDAO
							.findByApplicationNo(regDtoDetais.getApplicationNo());
					RegServiceDTO regDto = regDtoOptional.get();
					if (regDto.getServiceIds().stream()
							.anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId())
									|| id.equals(ServiceEnum.RCCANCELLATION.getId()))
							&& StringUtils.isNoneBlank(service) && (service.equals(ServiceEnum.RCFORFINANCE.getCode())
									|| service.equals(ServiceEnum.RCCANCELLATION.getCode()))) {
						vehicleType = setVehicleTypeCode(regDto);
					}
					if (regDto.getRegistrationDetails() != null
							&& regDto.getRegistrationDetails().getVehicleType() != null
							&& !regDto.getRegistrationDetails().getVehicleType().equalsIgnoreCase(vehicleType)) {
						continue;
					}
					if (regDto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId())
							|| id.equals(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId()))) {
						continue;
					}
					/*
					 * boolean statu = Boolean.FALSE; if (StringUtils.isNoneBlank(fc) &&
					 * ServiceEnum.OTHERSTATIONFC.getCode().equalsIgnoreCase(fc) &&
					 * regDto.getServiceIds().stream() .anyMatch(id ->
					 * id.equals(ServiceEnum.OTHERSTATIONFC.getId()))) { statu = Boolean.TRUE; }
					 * else if (!regDto.getServiceIds().stream() .anyMatch(id ->
					 * id.equals(ServiceEnum.OTHERSTATIONFC.getId()))) { statu = Boolean.TRUE; } if
					 * (!statu) { continue; }
					 */
					boolean freshRCstatus = Boolean.FALSE;
					if (StringUtils.isNoneBlank(service) && ServiceEnum.RCFORFINANCE.getCode().equalsIgnoreCase(service)
							&& regDto.getServiceIds().stream()
									.anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId()))) {
						freshRCstatus = Boolean.TRUE;
					} else if (!regDto.getServiceIds().stream()
							.anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId()))) {
						freshRCstatus = Boolean.TRUE;
					}
					if (!freshRCstatus) {
						continue;
					}
					List<ServiceEnum> serviceType = regDto.getServiceIds().stream()
							.map(id -> ServiceEnum.getServiceEnumById(id)).collect(Collectors.toList());
					regDto.setServiceType(serviceType);
					if (noOfRecordsAssign > assignedCount) {
						LockedDetailsDTO lockedDetail = setLockedDetails(user, role,
								regDto.getRegistrationDetails().getIteration(),
								regDto.getRegistrationDetails().getVehicleType(), regDto.getApplicationNo());
						if (regDto.getLockedDetails() == null || regDto.getLockedDetails().isEmpty()) {
							lockedDetailsLog.add(lockedDetail);
							regDto.setLockedDetails(lockedDetailsLog);
						} else {

							// regDto.getLockedDetails().add(lockedDetail);
							continue;
						}
						assignedCount++;
						toBeAssignedList.add(regDto);
						continue;
					}
					break;
				}
				regServiceDAO.save(toBeAssignedList);
			}
		}
		for (RegServiceDTO stagingDto : toBeAssignedList) {
			returnList.add(regServiceMapper.limitedDashBoardfields(stagingDto));
		}
		return returnList;
	}

	private List<RegServiceDTO> getSpecificRoleBaseRecord(String officeCode, List<String> serviceList,
			List<String> applicationStatus) {

		List<RegServiceDTO> totalMatchedRecords = regServiceDAO
				.findByOfficeDetailsOfficeCodeAndServiceTypeInAndApplicationStatusIn(officeCode, serviceList,
						applicationStatus);

		List<RegServiceDTO> regServiceDTOList = new ArrayList<>();
		for (RegServiceDTO regServiceDto : totalMatchedRecords) {
			regServiceDTOList.add(regServiceDto);
		}

		return totalMatchedRecords;
	}

	@Override
	public Integer getDashBoardCountsForDataEntry(String officeCode, String user) {

		Integer dashBoardCount = null;
		// Prepare Dash Board
		List<String> serviceList = new ArrayList<>();
		serviceList.add(ServiceEnum.DATAENTRY.toString());
		List<String> applicationStatus = new ArrayList<>();
		applicationStatus.add(StatusRegistration.CITIZENSUBMITTED.toString());
		List<RegServiceDTO> regServiceDTOList = getSpecificRoleBaseRecord(officeCode, serviceList, applicationStatus);
		List<RegServiceVO> regServiceVOList = new ArrayList<>();

		if (!regServiceDTOList.isEmpty()) {
			for (RegServiceDTO regServiceDTO : regServiceDTOList) {
				RegServiceVO regServiceVO = regServiceMapper.convertEntity(regServiceDTO);
				regServiceVOList.add(regServiceVO);
			}
			dashBoardCount = regServiceDTOList.size();
		} else {
			logger.info(MessageKeys.MESSAGE_NO_RECORD_FOUND + "[{}]", regServiceVOList);
		}
		return dashBoardCount;
	}

	@Override
	public List<RegServiceVO> getDashBoardRecordsForDataEntry(String officeCode, String user) {

		// Prepare Dash Board
		List<String> serviceList = new ArrayList<>();
		serviceList.add(ServiceEnum.DATAENTRY.toString());
		List<String> applicationStatus = new ArrayList<>();
		applicationStatus.add(StatusRegistration.CITIZENSUBMITTED.toString());
		List<RegServiceVO> regServiceVOList = new ArrayList<>();
		List<RegServiceDTO> regServiceDTOList = getSpecificRoleBaseRecord(officeCode, serviceList, applicationStatus);
		if (!regServiceDTOList.isEmpty()) {
			for (RegServiceDTO regServiceDTO : regServiceDTOList) {
				RegServiceVO regServiceVO = regServiceMapper.convertEntity(regServiceDTO);
				regServiceVOList.add(regServiceVO);
			}
		} else {
			logger.info(MessageKeys.MESSAGE_NO_RECORD_FOUND + "[{}]", regServiceVOList);
		}
		return regServiceVOList;
	}

	@Override
	public RegServiceVO viewDashBoardRecordsForDataEntry(String officeCode, String user, String applicationNo) {

		RegServiceVO regServiceVO = null;
		// Prepare Dash Board
		List<String> serviceList = new ArrayList<>();
		serviceList.add(ServiceEnum.DATAENTRY.toString());
		List<String> applicationStatus = new ArrayList<>();
		applicationStatus.add(StatusRegistration.CITIZENSUBMITTED.toString());
		List<RegServiceDTO> regServiceDTOList = getSpecificRoleBaseRecord(officeCode, serviceList, applicationStatus);
		if (!regServiceDTOList.isEmpty()) {
			for (RegServiceDTO regServiceDTO : regServiceDTOList) {
				if (regServiceDTO.getApplicationNo().equals(applicationNo)) {
					regServiceVO = regServiceMapper.convertEntity(regServiceDTO);
					regServiceVO.setEnclosures(enclosuresLogMapper.convertEnclosures(regServiceDTO.getEnclosures()));
				}
			}
		} else {
			logger.info(MessageKeys.MESSAGE_NO_RECORD_FOUND + "[{}]", regServiceVO);
		}
		return regServiceVO;
	}

	@Override
	public RegistrationDetailsVO findByTrNo(String trNo) {
		Optional<StagingRegistrationDetailsDTO> stagingOptional = stagingRegistrationDetailsDAO.findByTrNo(trNo);
		if (stagingOptional.isPresent()) {
			StagingRegistrationDetailsDTO stagingDTO = stagingOptional.get();
			String hsrpOffice = StringUtils.EMPTY;
			if (StringUtils.isNoneBlank(
					stagingDTO.getApplicantDetails().getPresentAddress().getMandal().getMviAddressOfficeCode())) {
				hsrpOffice = stagingDTO.getApplicantDetails().getPresentAddress().getMandal().getMviAddressOfficeCode();
			} else {
				hsrpOffice = stagingDTO.getApplicantDetails().getPresentAddress().getMandal().getHsrpoffice();
			}
			if (StringUtils.isBlank(hsrpOffice)) {
				logger.error("Hsrpoffice code not found based on trNo");
				throw new BadRequestException("Hsrpoffice code not found based on trNo");
			}

			RegistrationDetailsVO regVO = regDetailMapper.convertMandalDetailsDTOtoVO(stagingDTO);
			regVO.setVehicleType(stagingDTO.getVehicleType());
			return regVO;
		}
		logger.error("no record found based on trNo");
		throw new BadRequestException("no record found based on trNo");

	}

	@Override
	public void updateBodyBuildingDetails(String userId, RtaActionVO actionVo, Optional<UserDTO> userDetails,
			String role) throws Exception {
		// TODO Auto-generated method stub

		// String role = userDetails.get().getPrimaryRole().getName();

		String applicationNo = actionVo.getApplicationNo();

		synchronized (applicationNo.intern()) {
			Optional<StagingRegistrationDetailsDTO> stagingDto = stagingRegistrationDetailsDAO
					.findByApplicationNo(actionVo.getApplicationNo());

			if (!stagingDto.isPresent()) {
				logger.error("Application Not Found. [{}]", actionVo.getApplicationNo());
				throw new BadRequestException("Application Not Found.");
			}

			// boolean isProcessCompleted = false;
			StagingRegistrationDetailsDTO staginDto = stagingDto.get();

			// String action = StatusActions.APPROVED.getStatus();

			// Check Is there any approval in next stage
			// If no , then update flow detail into Log
			// Execute Is Process Completed Last
			// Integer flowPosition = getCurrentFlowIndex(stagingDto.get());
			// Integer flowPosition = getCurrentFlowIndex(staginDto);
			// Invocable invocable =
			// jsEngine.getInvocableObj(scriptEngineLocation);
			// boolean isAnyFlow = verifyAnyFlow(invocable, flowPosition);
			// logger.info(appMessages.getLogMessage(MessageKeys.ANY_FLOW),
			// isAnyFlow);

			// FlowDTO flowDto = stagingDto.get().getFlowDetailsLog().get(0);
			/*
			 * if (isAnyFlow) { isProcessCompleted =
			 * prepareScriptInputAndExecute(stagingDto.get(), flowPosition, role,
			 * actionVo.getAction(), invocable);
			 * 
			 * 
			 * if (actionVo.getAction().equals(StatusRegistration.APPROVED.
			 * getDescription())) { //this.processPR(staginDto); }
			 * 
			 * }
			 */
			/*
			 * if (role.equalsIgnoreCase(RoleEnum.RTO.getName())) {
			 * staginDto.setIterationFlag(Boolean.TRUE); }
			 */
			// Second Vehicle Rejected Status
			if (actionVo.getSecondVehicleList() != null) {
				if (null != actionVo.getSecondVehicleList().getIsSecondVehicleRejected()
						&& actionVo.getSecondVehicleList().getIsSecondVehicleRejected()) {
					if (actionVo.getSecondVehicleList().getRole().equalsIgnoreCase(RoleEnum.RTO.getName())) {
						staginDto.setIsFirstVehicle(false);
					}
				}
				if (null != actionVo.getSecondVehicleList().getIsInvalidVehicleRejection()
						&& actionVo.getSecondVehicleList().getIsInvalidVehicleRejection()) {
					if (actionVo.getSecondVehicleList().getRole().equalsIgnoreCase(RoleEnum.RTO.getName())) {
						if (actionVo.getSecondVehicleList().getClassOfVehicle() == null) {
							logger.error("Please select class of vehicle");
							throw new BadRequestException(
									appMessages.getResponseMessage(MessageKeys.CLASSOFVEHICLE_NOTSELECTED));
						}
						staginDto.setClassOfVehicle(actionVo.getSecondVehicleList().getClassOfVehicle());
						ClassOfVehiclesDTO cov = classOfVehiclesDAO
								.findByCovcode(actionVo.getSecondVehicleList().getClassOfVehicle());
						if (cov != null) {
							staginDto.setClassOfVehicleDesc(cov.getCovdescription());
						}
					}
				}

			}

			try {
				if (stagingDto.get().getEnclosures() != null && actionVo.getEnclosures() != null) {
					for (EnclosuresVO enclosure : actionVo.getEnclosures()) {
						updateEnclosures(role, enclosure.getImages(), staginDto);
					}
				} else {
					logger.warn(appMessages.getLogMessage(MessageKeys.NOT_FOUND_ENCLOSURES),
							actionVo.getApplicationNo());
				}

			} catch (Exception e) {
				logger.debug("failed {}", e);
				logger.error("failed {}", e.getMessage());

			}
			// Saving DisabledData
			updateDisabledData(actionVo, staginDto);

			if (actionVo.getSecondVehicleList() != null) {
				updateSeconVehilceDetails(actionVo, staginDto);
			} else {
				logger.warn(appMessages.getLogMessage(MessageKeys.NOT_FOUND_REJECTION_HISTORY),
						actionVo.getApplicationNo());
			}

			List<LockedDetailsDTO> filteredLockedDetails = removeMatchedLockedDetails(staginDto, userId, role);
			if (filteredLockedDetails != null) {
				staginDto.setLockedDetails(filteredLockedDetails);
			}

			/*
			 * boolean isFlowCompleted = processFlow(stagingDto.get(), userDetails,
			 * actionVo.getAction(), stagingDto.get().getApplicationNo(), role);
			 */
			// saving logs
			/*
			 * if (actionVo.getAction().equals("REJECTED")) { //saveLogs(staginDto, role,
			 * actionVo.getAction(), flowPosition); }
			 */
			// if (isProcessCompleted || isFlowCompleted) {

			staginDto.setlUpdate(LocalDateTime.now());
			/*
			 * if(!actionVo.getStatus().equals(StatusRegistration.APPROVED)) {
			 * staginDto.setApplicationStatus(StatusRegistration.REJECTED.
			 * getDescription()); }
			 */
			logger.debug(
					"Approval Actions for Application [{}]: IsSpecial No Required :[{}], Action :[{}], PrNo :[{}], prType :[{}], Role :[{}] ",
					applicationNo, staginDto.getSpecialNumberRequired(), actionVo.getAction(), staginDto.getPrNo(),
					staginDto.getPrType(), role);

			// if (!staginDto.getSpecialNumberRequired() && ) {
			staginDto.setApprovalStage(role);
			if ((actionVo.getAction().equals(StatusRegistration.APPROVED.getDescription()))
					&& ((!staginDto.getSpecialNumberRequired())
							|| (staginDto.getSpecialNumberRequired() && StringUtils.isNotBlank(staginDto.getPrNo()))
							|| (null != staginDto.getPrType()
									&& staginDto.getPrType().equalsIgnoreCase(BidNumberType.N.getCode())
									&& staginDto.getSpecialNumberRequired()))) {

				logger.debug(
						"(Yes PR Processing )Approval Actions for Application [{}]: IsSpecial No Required :[{}], Action :[{}], PrNo :[{}], prType :[{}], Role :[{}] ",
						applicationNo, staginDto.getSpecialNumberRequired(), actionVo.getAction(), staginDto.getPrNo(),
						staginDto.getPrType(), role);
				try {
					updateAlterationDetailsInVahan(staginDto);

				} catch (Exception e) {
					logger.debug("Unable to update alteration details in staging.", e);
				}
				if (prService.isAssignNumberNow()) {
					processPR(staginDto);
				}
			} else if (staginDto.getSpecialNumberRequired() && StringUtils.isBlank(staginDto.getPrNo())
					&& actionVo.getAction().equals(StatusRegistration.APPROVED.getDescription())) {
				staginDto.setApplicationStatus(StatusRegistration.SPECIALNOPENDING.getDescription());
				updateStagingRegDetails(staginDto);
			} else if (actionVo.getAction().equals(StatusRegistration.REJECTED.getDescription())) {
				staginDto.setApplicationStatus(StatusRegistration.REJECTED.getDescription());
				updateStagingRegDetails(staginDto);
			}

			// } else {
			updateStagingRegDetails(staginDto);
			// }
		}
	}

	/**
	 * @param staginDto
	 */
	private void updateAlterationDetailsInVahan(StagingRegistrationDetailsDTO staginDto) {
		Optional<AlterationDTO> alterationDTO = alterationDAO.findByApplicationNo(staginDto.getApplicationNo());
		if (!alterationDTO.isPresent()) {
			logger.error("No record found in Alteration based on applicationNo [{}]", staginDto.getApplicationNo());
			throw new BadRequestException("No record found in Alteration based on applicationNo");
		}
		AlterationDTO result = alterationDTO.get();
		VahanDetailsDTO detailsDTO = staginDto.getVahanDetails();

		detailsDTO.setColor(null != result.getColor() ? result.getColor() : detailsDTO.getColor());
		detailsDTO.setHeight(null != result.getHeight() ? result.getHeight() : detailsDTO.getHeight());
		detailsDTO.setLength(null != result.getLength() ? result.getLength() : detailsDTO.getLength());
		detailsDTO.setWidth(null != result.getWidth() ? result.getWidth() : detailsDTO.getWidth());
		detailsDTO.setUnladenWeight(null != result.getUlw() ? result.getUlw() : detailsDTO.getUnladenWeight());
		detailsDTO.setBodyTypeDesc(null != result.getBodyType() ? result.getBodyType() : detailsDTO.getBodyTypeDesc());
		// tailsDTO.setGvw(null != result.getGvw() ? result.getGvw() :
		// detailsDTO.getGvw());
		detailsDTO.setSeatingCapacity(
				null != result.getSeating() ? result.getSeating() : detailsDTO.getSeatingCapacity());

		staginDto.setClassOfVehicle(null != result.getCov() ? result.getCov() : staginDto.getClassOfVehicle());
		staginDto.setClassOfVehicleDesc(
				null != result.getCovDescription() ? result.getCovDescription() : staginDto.getClassOfVehicleDesc());
		if (result.getTrailers() != null && !result.getTrailers().isEmpty()) {
			staginDto.getVahanDetails().setTrailerChassisDetailsDTO(result.getTrailers());
		}
		if (result.getAxleType() != null && StringUtils.isNoneBlank(result.getAxleType())) {
			staginDto.getVahanDetails().setAxleType(result.getAxleType());
		}
		result.setMVIDone(false);
		alterationDAO.save(result);
	}

	@Override
	public List<RegistrationDetailsVO> getPendingListForBodyBuilding(String officeCode, String userId, String role) {
		List<StagingRegistrationDetailsDTO> toBeAssignedList = new ArrayList<>();
		List<LockedDetailsDTO> lockedDetailsLog = new ArrayList<>();
		List<RegistrationDetailsVO> returnList = new ArrayList<>();
		List<StagingRegistrationDetailsDTO> pending = new ArrayList<>();
		synchronized (officeCode.intern()) {
			Optional<StagingRegistrationDetailsDTO> stagingDetailsDTO = Optional.empty();
			stagingDetailsDTO = stagingRegistrationDetailsDAO
					.findByLockedDetailsLockedByAndLockedDetailsLockedByRoleAndBodyBuilding(userId, role, true);
			if (stagingDetailsDTO.isPresent()) {

				RegistrationDetailsVO regVO = regDetailMapper.convertEntity(stagingDetailsDTO.get());
				Optional<AlterationDTO> alterationDTO = alterationDAO.findByApplicationNo(regVO.getApplicationNo());
				if (!alterationDTO.isPresent()) {
					logger.error("No record found in Alteration based on applicationNo [{}]",
							stagingDetailsDTO.get().getApplicationNo());
					throw new BadRequestException("No record found in Alteration based on applicationNo ["
							+ stagingDetailsDTO.get().getApplicationNo() + "]");
				}
				AlterationVO alterationVO = new AlterationVO();
				alterationVO.setCov(alterationDTO.get().getCov());
				regVO.setAlterationVO(alterationVO);
				returnList.add(regVO);
				return returnList;
			}

			List<StagingRegistrationDetailsDTO> pendingList = getSpecificRoleBaseRecordsForBodyBuilding(role,
					officeCode);
			if (pendingList.stream().anyMatch(val -> val.getTrNo() == null || val.getTrGeneratedDate() == null)) {
				logger.error("trNo or trGeneratedNo not found ");
				throw new BadRequestException("trNo or trGeneratedNo not found ");
			}
			if (!pendingList.isEmpty()) {
				pendingList.sort((o1, o2) -> o1.getTrGeneratedDate().compareTo(o2.getTrGeneratedDate()));
				pending = pendingList.stream()
						.filter((val -> val.getLockedDetails() == null || (val.getLockedDetails() != null && (val
								.getLockedDetails().stream().allMatch(data -> !data.getLockedByRole().equals(role))))))
						.collect(Collectors.toList());
				int assignedCount = 0;
				for (StagingRegistrationDetailsDTO stagingRegistrationDetailsDTO : pending) {
					if (noOfRecordsAssign > assignedCount) {
						LockedDetailsDTO lockedDetail = setLockedDetails(userId, role,
								stagingRegistrationDetailsDTO.getIteration(),
								stagingRegistrationDetailsDTO.getVehicleType(),
								stagingRegistrationDetailsDTO.getApplicationNo());
						if (stagingRegistrationDetailsDTO.getLockedDetails() == null) {
							lockedDetailsLog.add(lockedDetail);
							stagingRegistrationDetailsDTO.setLockedDetails(lockedDetailsLog);
						} else {

							stagingRegistrationDetailsDTO.getLockedDetails().add(lockedDetail);
						}
						assignedCount++;
						toBeAssignedList.add(stagingRegistrationDetailsDTO);
						continue;
					}
					break;
				}
				stagingRegistrationDetailsDAO.save(toBeAssignedList);
			}
		}
		for (StagingRegistrationDetailsDTO stagingDto : toBeAssignedList) {
			RegistrationDetailsVO regVO = regDetailMapper.convertEntity(stagingDto);
			Optional<AlterationDTO> alterationDTO = alterationDAO.findByApplicationNo(regVO.getApplicationNo());
			if (!alterationDTO.isPresent()) {
				logger.error("No record found in Alteration based on applicationNo");
				throw new BadRequestException("No record found in Alteration based on applicationNo");
			}
			AlterationVO alterationVO = new AlterationVO();
			alterationVO.setCov(alterationDTO.get().getCov());
			regVO.setAlterationVO(alterationVO);
			returnList.add(regVO);
		}
		return returnList;
	}

	private List<StagingRegistrationDetailsDTO> getSpecificRoleBaseRecordsForBodyBuilding(String role,
			String officeCode) {
		List<StatusRegistration> status = new ArrayList<StatusRegistration>();
		status.add(StatusRegistration.APPROVED);
		List<StagingRegistrationDetailsDTO> pendingList = stagingRegistrationDetailsDAO
				.findByOfficeDetailsOfficeCodeAndApplicationStatusInAndBodyBuilding(officeCode, status, true);
		return pendingList;
	}

	@Override
	public DashBordDetails getCountForBB(String officeCode, String userId, String role) {
		DashBordDetails dashBordDetails = new DashBordDetails();
		PendingCountVo pendingCountVo = new PendingCountVo();
		Integer total = 0;
		Integer bodyBuildingCount = 0;
		pendingCountVo.setBodyBuildingCount(bodyBuildingCount);
		if (role.equalsIgnoreCase(RtaRoles.AO.getDesc()) || role.equalsIgnoreCase(RtaRoles.RTA.getDesc())) {
			List<StagingRegistrationDetailsDTO> listSD = getSpecificRoleBaseRecordsForBodyBuilding(role, officeCode);
			pendingCountVo.setBodyBuildingCount(listSD.size());
			total = total + bodyBuildingCount;
		}
		dashBordDetails.setPendingCountVo(pendingCountVo);
		return dashBordDetails;
	}

	@Override
	public void updateTrailerDetails(String userId, RtaActionVO actionVo, Optional<UserDTO> userDetails, String role)
			throws Exception {
		String applicationNo = actionVo.getApplicationNo();
		synchronized (applicationNo.intern()) {
			Optional<StagingRegistrationDetailsDTO> stagingDto = stagingRegistrationDetailsDAO
					.findByTrNo(actionVo.getTrNumber());
			if (!stagingDto.isPresent()) {
				logger.error("Application Not Found [{}]", actionVo.getTrNumber());
				throw new BadRequestException("Application Not Found.");
			}
			StagingRegistrationDetailsDTO staginDto = stagingDto.get();
			List<LockedDetailsDTO> filteredLockedDetails = removeMatchedLockedDetails(staginDto, userId, role);
			if (filteredLockedDetails != null) {
				staginDto.setLockedDetails(filteredLockedDetails);
			}
			staginDto.setlUpdate(LocalDateTime.now());
			staginDto.setApprovalStage(role);
			if ((actionVo.getAction().equals(StatusRegistration.APPROVED.getDescription()))) {
				if (role.equals(RoleEnum.MVI.getName())) {
					staginDto.getVahanDetails().setTrailerChassisDetailsDTO(
							trailerChassisDetailsMapper.convertVO(actionVo.getTrailerChassisDetailsVO()));
				} else if (role.equals(RoleEnum.AO.getName())) {
					try {
						if (stagingDto.get().getEnclosures() != null && actionVo.getEnclosures() != null) {
							for (EnclosuresVO enclosure : actionVo.getEnclosures()) {
								updateEnclosures(role, enclosure.getImages(), staginDto);
							}
						} else {
							logger.warn(appMessages.getLogMessage(MessageKeys.NOT_FOUND_ENCLOSURES),
									actionVo.getApplicationNo());
						}
					} catch (Exception e) {
						logger.debug("failed {}", e);
					}
				} else {
					logger.error("Undefined user type.");
					throw new BadRequestException("Undefined user type.");
				}
				logger.debug(
						"(Yes PR Processing )Approval Actions for Application [{}]: IsSpecial No Required :[{}], Action :[{}], PrNo :[{}], prType :[{}], Role :[{}] ",
						applicationNo, staginDto.getSpecialNumberRequired(), actionVo.getAction(), staginDto.getPrNo(),
						staginDto.getPrType(), role);
			}
			updateStagingRegDetails(staginDto);
		}
	}

	@Override
	public RegistrationDetailsVO findBasedOnTrNo(String trNo) {
		Optional<StagingRegistrationDetailsDTO> optionalDto = stagingRegistrationDetailsDAO.findByTrNo(trNo);
		if (optionalDto.isPresent()) {
			RegistrationDetailsVO registrationDetailsVO = regDetailMapper.convertEntity(optionalDto.get());
			return registrationDetailsVO;
		}
		logger.error("No record found based on trNo [{}]", trNo);
		throw new BadRequestException("No record found based on trNo");
	}

	@Override
	public Optional<List<TaxTypeVO>> taxType() {
		List<TaxTypeDTO> taxTypeDTOList = taxTypeDAO.findByStatusTrue();
		return Optional.of(taxTypeMapper.convertEntity(taxTypeDTOList));
	}

	@Override
	public void updateForDataEntryDetails(String userId, RtaActionVO actionVo, Optional<UserDTO> userDetails,
			String role) throws Exception {
		String applicationNo = actionVo.getApplicationNo();
		synchronized (applicationNo.intern()) {
			Optional<RegServiceDTO> stagingDto = regServiceDAO.findByApplicationNo(actionVo.getApplicationNo());
			if (!stagingDto.isPresent()) {
				throw new BadRequestException("Application Not Found.");
			}
			RegServiceDTO staginDto = stagingDto.get();
			try {
				if (stagingDto.get().getEnclosures() != null && actionVo.getEnclosures() != null) {
					for (EnclosuresVO enclosure : actionVo.getEnclosures()) {
						updateEnclosuresForDataEntry(role, enclosure.getImages(), staginDto);
					}
				} else {
					logger.warn(appMessages.getLogMessage(MessageKeys.NOT_FOUND_ENCLOSURES),
							actionVo.getApplicationNo());
				}

			} catch (Exception e) {
				logger.info("failed {}", e);

			}
			staginDto.getRegistrationDetails().setApprovalStage(role);
			if ((actionVo.getAction().equals(StatusRegistration.APPROVED.getDescription()))) {
				if (role.equals(RoleEnum.AO.getName())) {
					FcDetailsDTO fcDetails = updateFCDetailsforDataEntry(staginDto);
					TaxDetailsDTO taxDetails = updateTaxDetailsForDataEntry(staginDto);
					NOCDetailsDTO nOCDetailsDTO = updateNocDetailsForDataEntry(staginDto);
					nocDetailsDAO.save(nOCDetailsDTO);
					taxDetailsDAO.save(taxDetails);
					fcDetailsDAO.save(fcDetails);
					registrationDetailDAO.save(staginDto.getRegistrationDetails());
				}
				staginDto.setApplicationStatus(actionVo.getStatus());
			} else if (actionVo.getAction().equals(StatusRegistration.REJECTED.getDescription())) {
				staginDto.setApplicationStatus(actionVo.getStatus());
			}
			regServiceDAO.save(staginDto);
		}
	}

	/**
	 * @param staginDto
	 * @return
	 */
	private NOCDetailsDTO updateNocDetailsForDataEntry(RegServiceDTO staginDto) {
		NOCDetailsDTO nOCDetailsDTO = staginDto.getnOCDetails();
		nOCDetailsDTO.setApplicationNo(staginDto.getApplicationNo());
		return nOCDetailsDTO;
	}

	/**
	 * @param staginDto
	 * @return
	 */
	private TaxDetailsDTO updateTaxDetailsForDataEntry(RegServiceDTO staginDto) {
		TaxDetailsDTO taxDetails = new TaxDetailsDTO();
		taxDetails.setApplicationNo(staginDto.getApplicationNo());
		taxDetails.setTrNo(staginDto.getTrNo());
		taxDetails.setPrNo(staginDto.getPrNo());
		taxDetails.setClassOfVehicle(staginDto.getRegistrationDetails().getClassOfVehicle());

		GreenTaxDTO greenTaxDTO = staginDto.getGreenTaxDetails();
		taxDetails.setGreenTaxAmount(greenTaxDTO.getTaxAmount());
		taxDetails.setGreenTaxPeriodEnd(greenTaxDTO.getValidUpto());
		taxDetails.setGreenTaxPeriodFrom(greenTaxDTO.getPaymentDate());

		org.epragati.regservice.dto.TaxDetailsDTO taxDetailsDTO = staginDto.getTaxDetails();
		taxDetails.setTaxAmount(taxDetailsDTO.getTaxAmount());
		taxDetails.setTaxPeriodFrom(taxDetailsDTO.getPaymentDAte());
		taxDetails.setTaxPeriodEnd(taxDetailsDTO.getValidUpto());

		// Map for tax details
		List<Map<String, TaxComponentDTO>> taxDetailsList = new ArrayList<>();

		Map<String, TaxComponentDTO> taxMap = new HashMap<>();

		TaxComponentDTO tax = new TaxComponentDTO();
		tax.setTaxName(taxDetailsDTO.getTaxType());
		tax.setAmount(Double.valueOf(taxDetailsDTO.getTaxAmount().toString()));
		// tax.setPaidDate(taxDetailsDTO.getPaymentDAte());
		// tax.setValidityFrom(validityFrom);
		tax.setValidityTo(taxDetailsDTO.getValidUpto());
		taxMap.put(taxDetailsDTO.getTaxType(), tax);
		taxDetailsList.add(taxMap);
		taxDetails.setTaxDetails(taxDetailsList);
		taxDetails.setCreatedDate(LocalDateTime.now());
		taxDetails.setTaxStatus("Active");
		return taxDetails;
	}

	/**
	 * @param staginDto
	 * @return
	 */
	private FcDetailsDTO updateFCDetailsforDataEntry(RegServiceDTO staginDto) {
		FcDetailsDTO fcDetails = staginDto.getFcDetails();
		fcDetails.setApplicationNo(staginDto.getApplicationNo());
		fcDetails.setTrNo(staginDto.getTrNo());
		fcDetails.setPrNo(staginDto.getPrNo());
		fcDetails.setClassOfVehicle(staginDto.getRegistrationDetails().getClassOfVehicle());
		fcDetails.setOfficeCode(staginDto.getOfficeDetails().getOfficeCode());
		fcDetails.setOfficeName(staginDto.getOfficeDetails().getOfficeName());
		fcDetails.setCreatedDate(LocalDateTime.now());
		fcDetails.setFctype(ServiceEnum.NEWFC.getDesc());
		return fcDetails;
	}

	private RegServiceDTO updateEnclosuresForDataEntry(String role, List<ImageVO> ccoupdateddetails,
			RegServiceDTO stageDto) {
		if (ccoupdateddetails != null) {
			for (ImageVO imageEnclosureDTO : ccoupdateddetails) {
				String imageType = imageEnclosureDTO.getImageType();
				for (KeyValue<String, List<ImageEnclosureDTO>> keyValue : stageDto.getEnclosures()) {
					if (imageType.equals(keyValue.getKey())) {

						for (ImageEnclosureDTO enclosureDTO : keyValue.getValue()) {
							if (enclosureDTO.getImageId().equals(imageEnclosureDTO.getAppImageDocId())) {
								enclosureMapper.imageVOtoEnclosureDTO(role, enclosureDTO, imageEnclosureDTO);
							}
						}
					}

				}
			}
		}
		return stageDto;
	}

	@Override
	public RegServiceVO getRegDetailsByApplicationNo(String applicationNo) {
		RegServiceVO regServiceVO = null;
		Optional<RegServiceDTO> regServiceDTOList = regServiceDAO.findByApplicationNo(applicationNo);
		if (regServiceDTOList.isPresent()) {
			regServiceVO = regServiceMapper.convertEntity(regServiceDTOList.get());
			regServiceVO.setEnclosures(enclosuresLogMapper.convertEnclosures(regServiceDTOList.get().getEnclosures()));
		} else {
			logger.info(MessageKeys.MESSAGE_NO_RECORD_FOUND + "[{}]", regServiceVO);
		}
		return regServiceVO;
	}

	private List<RegServiceDTO> getRegistrationServicesPendingRecords(String role, String officeCode,
			Integer serviceId,Boolean isDataEntry) {
		
		List<RegServiceDTO> regServiceAllRecordsDTOList = null;
		if (RoleEnum.MVI.getName().equals(role)) {
			logger.info(" MVI start officeCode [{}] , currenrRole [{}] currentDate [{}]", officeCode, role, new Date());
			regServiceAllRecordsDTOList = regServiceDAO.findByMviOfficeCodeAndCurrentRolesInAndSourceIsNull(officeCode,
					Arrays.asList(role));
			logger.info(" MVI start officeCode [{}] , currenrRole [{}] currentDate [{}] count [{}]", officeCode, role,
					new Date(), regServiceAllRecordsDTOList.size());
			if (serviceId == null) {
				/*
				 * List<Integer> serviceIds = Arrays.asList(ServiceEnum.VEHICLESTOPPAGE.getId(),
				 * ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId());
				 */
				regServiceAllRecordsDTOList = regServiceAllRecordsDTOList.stream()
						.filter(reg -> /*
										 * serviceIds.stream() .anyMatch(sid -> reg.getServiceIds().contains(sid) || (
										 */reg.getFlowId() != null
								&& reg.getFlowId().equals(ServiceEnum.Flow.RCCANCELLATIONCCO))/* )) */
						.collect(Collectors.toList());
			} else {
				regServiceAllRecordsDTOList = regServiceAllRecordsDTOList.stream()
						.filter(reg -> reg.getServiceIds().contains(serviceId)).collect(Collectors.toList());
			}
		} else {
			if (serviceId != null) {
				ApprovalProcessFlowDTO approvalFlowDTO = registratrionServicesApprovals
						.getApprovalProcessFlowDTOForLock(role, serviceId);
				regServiceAllRecordsDTOList = regServiceDAO
						.findByOfficeCodeAndServiceIdsInAndCurrentIndexAndSourceIsNull(officeCode,
								Arrays.asList(serviceId), approvalFlowDTO.getIndex());
			} else {
				logger.info("Getting start top5 Service Rrecords  officeCode [{}] currentRole [{}] Date  :[{}]",
						officeCode, role, new Date());
				regServiceAllRecordsDTOList = regServiceDAO
						.findTop70ByOfficeCodeAndCurrentRolesInAndSourceIsNull(officeCode, Arrays.asList(role));
				logger.info("Getting End top5 Service Rrecords  officeCode [{}] currentRole [{}]  Date :[{}]",
						officeCode, role, new Date());
			}
		}
		List<RegServiceDTO> regServiceDTOList = new ArrayList<>();
		
		if (isDataEntry) {
			regServiceDTOList = regServiceAllRecordsDTOList.parallelStream()
					.filter(val -> null != val.getActionDetails()
							&& val.getActionDetails().stream()
									.anyMatch(action -> role.equals(action.getRole()) && !action.getIsDoneProcess())
							&& val.getServiceIds().contains(ServiceEnum.DATAENTRY.getId()))
					.collect(Collectors.toList());
		}
		else 
		{
			regServiceDTOList = regServiceAllRecordsDTOList.parallelStream()
					.filter(val -> null != val.getActionDetails()
							&& val.getActionDetails().stream()
									.anyMatch(action -> role.equals(action.getRole()) && !action.getIsDoneProcess())
							&& !val.getServiceIds().contains(ServiceEnum.DATAENTRY.getId()))
					.collect(Collectors.toList());
		}
		return regServiceDTOList;

	}

	@Override
	public Optional<RCActionsVO> getSuspensionInformation(SearchRcRequestVO searchRcRequestVO) {

		RCActionsDTO suspensionDTO = new RCActionsDTO();
		validationOfServices(searchRcRequestVO);
		Optional<RegistrationDetailsDTO> regDetails = registrationDetailDAO.findByPrNo(searchRcRequestVO.getPrNo());

		if (regDetails.isPresent()) {

			Optional<RCActionsDTO> suspensionOption = suspensionDAO
					.findOneByPrNoOrderByRcActionsDetailsActionDateDesc(regDetails.get().getPrNo());

			if (suspensionOption.isPresent()
					&& suspensionOption.get().getActionStatus() != Status.RCActionStatus.REVOKED) {
				suspensionDTO = suspensionOption.get();
			}

			suspensionDTO.setRcDetails(regDetails.get());
			suspensionDTO.setPrNo(searchRcRequestVO.getPrNo());

		} else {
			return Optional.ofNullable(null);
		}

		return Optional.ofNullable(suspensionMapper.convertEntity(suspensionDTO));
	}

	@Override
	public void createSuspensionDetails(RCActionsVO suspensionVO, JwtUser userDetails, String role) {

		if (!Status.RCActionStatus.isSuspensionRelatedStatus(suspensionVO.getActionStatus())) {
			logger.error("Invalid Action ");
			throw new BadRequestException("Invalid Action ");
		}

		List<RolesDTO> rolesList = userDetails.getAdditionalRoles();
		List<String> rolList = rolesList.stream().map(p -> p.getName()).collect(Collectors.toList());
		if (!(rolList.contains(role) || userDetails.getPrimaryRole().getName().equals(role))) {
			logger.error("Not authorized user");
			throw new BadRequestException("Not authorized user");
		}

		if ((role.equals(RoleEnum.CCO.getName()) && suspensionVO.getActionStatus().equals(RCActionStatus.SUSPEND))) {
			suspensionVO.setActionStatus(RCActionStatus.INITIATED);
		}
		RCActionsDTO rcActionDTO;

		Optional<RCActionsDTO> optionalsuspention = suspensionDAO
				.findOneByPrNoOrderByRcActionsDetailsActionDateDesc(suspensionVO.getPrNo());

		if (optionalsuspention.isPresent()) {

			rcActionDTO = optionalsuspention.get();
			validations(rcActionDTO, suspensionVO);
			rcActionDTO = suspensionMapper.convertVO(rcActionDTO, suspensionVO);

		} else {
			rcActionDTO = suspensionMapper.convertVO(suspensionVO);
		}
		if ((suspensionVO.getSelectedRole().equals(RoleEnum.AO.getName()))) {
			if (rcActionDTO.getRcActionsDetails().getFinalStatus()
					.equalsIgnoreCase(SuspensionEnum.APPROVED.getStatus())) {
				rcActionDTO.setActionStatus(RCActionStatus.SUSPEND);
			} else {
				rcActionDTO.setActionStatus(RCActionStatus.REJECTED);
			}
		} else if ((suspensionVO.getSelectedRole().equals(RoleEnum.AO.getName()))) {
			if (rcActionDTO.getRcActionsDetails().getFinalStatus() == SuspensionEnum.APPROVED.getStatus()) {
				rcActionDTO.setActionStatus(RCActionStatus.SUSPEND);
			} else
				rcActionDTO.setActionStatus(RCActionStatus.REJECTED);
		}
		rcActionDTO.setRcDetails(null);
		rcActionDTO.getRcActionsDetails().setAction(rcActionDTO.getActionStatus().toString());
		rcActionDTO.getRcActionsDetails().setActionBy(userDetails.getUsername());
		rcActionDTO.getRcActionsDetails().setActionDate(LocalDateTime.now());
		rcActionDTO.setOfficeCode(userDetails.getOfficeCode());
		Optional<RegistrationDetailsDTO> optinalInput = registrationDetailDAO.findByPrNo(suspensionVO.getPrNo());
		if (optinalInput.isPresent()) {
			if (rcActionDTO.getActionStatus().equals(Status.RCActionStatus.SUSPEND)) {
				optinalInput.get().setActionStatus(rcActionDTO.getActionStatus().toString());
			} else if (rcActionDTO.getActionStatus().equals(Status.RCActionStatus.REVOKED)) {
				optinalInput.get().setActionStatus(rcActionDTO.getActionStatus().toString());
			} else if (rcActionDTO.getActionStatus().equals(Status.RCActionStatus.INITIATED)) {
				optinalInput.get().setActionStatus(rcActionDTO.getActionStatus().toString());
			} else if (rcActionDTO.getActionStatus().equals(Status.RCActionStatus.REJECTED)) {
				optinalInput.get().setActionStatus(rcActionDTO.getActionStatus().toString());
			}

			registrationDetailDAO.save(optinalInput.get());
			sendRevocationNotification(optinalInput.get());
		} else {
			logger.error("RCNO not exist [{}]", suspensionVO.getPrNo());
			throw new BadRequestException("RCNO [" + suspensionVO.getPrNo() + "] not exist");

		}
		if (rcActionDTO.getActionStatus().equals(RCActionStatus.REVOKED)) {
			if ((role.equals(RoleEnum.RTO.getName())) && userDetails.getOfficeCode()
					.equalsIgnoreCase(optinalInput.get().getOfficeDetails().getOfficeCode())) {
				suspensionDAO.save(rcActionDTO);
			} else {
				logger.error("Please do revocation in corresponding office");
				throw new BadRequestException("Please do revocation in corresponding office");
			}
		} else {
			suspensionDAO.save(rcActionDTO);
		}
	}

	@Override
	public Optional<PermitSuspCanRevVO> getPermitSuspensionInformation(SearchRcRequestVO searchRcRequestVO) {
		PermitSuspCanRevVO permitSuspCanRevVO = null;
		List<PermitDetailsDTO> permitDetailsDTO = null;
		PermitSuspCanRevDTO suspensionDTO = new PermitSuspCanRevDTO();
		validationOfServices(searchRcRequestVO);
		Optional<RegistrationDetailsDTO> regDetails = registrationDetailDAO.findByPrNo(searchRcRequestVO.getPrNo());
		if (regDetails.isPresent()) {

			Optional<PermitSuspCanRevDTO> suspensionOption = permitSuspCanRevDAO
					.findOneByPrNoOrderByPermitActionsActionDateDesc(regDetails.get().getPrNo());

			if (suspensionOption.isPresent()
					&& suspensionOption.get().getActionStatus() != Status.permitSuspCanRevStatus.REVOKED) {
				suspensionDTO = suspensionOption.get();
			}
			permitDetailsDTO = permitDetailsDAO.findByPrNo(regDetails.get().getPrNo());

			suspensionDTO.setPermitDetailsDTO(permitDetailsDTO);
			suspensionDTO.setRcDetails(regDetails.get());
			suspensionDTO.setPrNo(searchRcRequestVO.getPrNo());

		} else {
			return Optional.ofNullable(null);
		}
		permitSuspCanRevVO = permitSuspCanRevMapper.convertEntity(suspensionDTO);
		return Optional.ofNullable(permitSuspCanRevVO);
	}

	@Override
	public void createPermitSuspensionDetails(PermitSuspCanRevVO suspensionVO, JwtUser userDetails, String role) {
		List<PermitDetailsDTO> permitDetailsDTOsList = null;
		if (!Status.permitSuspCanRevStatus.isSuspensionRelatedStatus(suspensionVO.getActionStatus())) {
			logger.error("Invalid Action ");
			throw new BadRequestException("Invalid Action ");
		}

		List<RolesDTO> rolesList = userDetails.getAdditionalRoles();
		List<String> rolList = rolesList.stream().map(p -> p.getName()).collect(Collectors.toList());
		if (!(rolList.contains(role) || userDetails.getPrimaryRole().getName().equals(role))) {
			logger.error("Not authorized user");
			throw new BadRequestException("Not authorized user");
		}

		if ((role.equals(RoleEnum.CCO.getName())
				&& (suspensionVO.getActionStatus().equals(permitSuspCanRevStatus.SUSPEND)
						|| suspensionVO.getActionStatus().equals(permitSuspCanRevStatus.CANCELED)))) {
			suspensionVO.setActionStatus(permitSuspCanRevStatus.INITIATED);
		}
		PermitSuspCanRevDTO rcActionDTO;

		Optional<PermitSuspCanRevDTO> optionalsuspention = permitSuspCanRevDAO
				.findOneByPrNoOrderByPermitActionsActionDateDesc(suspensionVO.getPrNo());

		if (optionalsuspention.isPresent()) {

			rcActionDTO = optionalsuspention.get();
			permitValidations(rcActionDTO, suspensionVO);
			rcActionDTO = permitSuspCanRevMapper.convertVO(rcActionDTO, suspensionVO);

		} else {
			rcActionDTO = permitSuspCanRevMapper.convertVO(suspensionVO);
		}
		if ((suspensionVO.getSelectedRole().equals(RoleEnum.AO.getName()))) {
			if (rcActionDTO.getPermitActions().getFinalStatus().equalsIgnoreCase(SuspensionEnum.APPROVED.getStatus())) {
				if (rcActionDTO.getActionStatus().equals(permitSuspCanRevStatus.SUSPEND)) {
					rcActionDTO.setActionStatus(permitSuspCanRevStatus.SUSPEND);
				} else {
					rcActionDTO.setActionStatus(permitSuspCanRevStatus.CANCELED);
				}
			} else {
				if (rcActionDTO.getActionStatus().equals(permitSuspCanRevStatus.SUSPEND)) {
					rcActionDTO.setActionStatus(permitSuspCanRevStatus.REJECTED);
				}
			}
		} else if ((suspensionVO.getSelectedRole().equals(RoleEnum.AO.getName()))) {
			if (rcActionDTO.getPermitActions().getFinalStatus() == SuspensionEnum.APPROVED.getStatus()) {
				rcActionDTO.setActionStatus(permitSuspCanRevStatus.SUSPEND);
			} else
				rcActionDTO.setActionStatus(permitSuspCanRevStatus.REJECTED);
		}
		permitDetailsDTOsList = permitDetailsDAO.findByPrNo(suspensionVO.getPrNo());
		if (permitDetailsDTOsList != null) {
			List<String> permitNumsList = permitDetailsDTOsList.stream().map(p -> p.getPermitNo())
					.collect(Collectors.toList());
			List<String> appliedCovs = permitDetailsDTOsList.stream().map(p -> p.getRdto().getClassOfVehicle())
					.collect(Collectors.toList());
			rcActionDTO.setPermitNums(permitNumsList);
			rcActionDTO.setAppliedCov(appliedCovs);
		} else {
			logger.error("No record found in permit details");
			throw new BadRequestException("No record found in permit details");
		}
		rcActionDTO.getPermitActions().setAction(rcActionDTO.getActionStatus().toString());
		rcActionDTO.getPermitActions().setActionBy(userDetails.getUsername());
		rcActionDTO.getPermitActions().setActionDate(LocalDateTime.now());
		rcActionDTO.setOfficeCode(userDetails.getOfficeCode());
		if (suspensionVO.getSelectedRole().equalsIgnoreCase("CCO")) {
			rcActionDTO.getPermitActions().setActionByCco(suspensionVO.getSelectedRole());
		} else if (suspensionVO.getSelectedRole().equalsIgnoreCase("AO")) {
			rcActionDTO.getPermitActions().setActionByAo(suspensionVO.getSelectedRole());
		} else if (suspensionVO.getSelectedRole().equalsIgnoreCase("RTO")) {
			rcActionDTO.getPermitActions().setActionByRto(suspensionVO.getSelectedRole());
		}
		rcActionDTO.setCreatedBy(LocalDate.now().toString());

		for (PermitDetailsDTO permitDetailsDTOOptinalInput : permitDetailsDTOsList) {
			if (Status.permitSuspCanRevStatus.SUSPEND.getStatus()
					.equalsIgnoreCase(rcActionDTO.getActionStatus().toString())) {
				permitDetailsDTOOptinalInput.setPermitStatus(rcActionDTO.getActionStatus().toString());
			} else if (Status.permitSuspCanRevStatus.REVOKED.getStatus()
					.equalsIgnoreCase(rcActionDTO.getActionStatus().toString())) {
				permitDetailsDTOOptinalInput.setPermitStatus(PermitsEnum.ACTIVE.getDescription());
			} else if (Status.permitSuspCanRevStatus.INITIATED.getStatus()
					.equalsIgnoreCase(rcActionDTO.getActionStatus().toString())) {
				permitDetailsDTOOptinalInput.setPermitStatus(rcActionDTO.getActionStatus().toString());
			} else if (Status.permitSuspCanRevStatus.REJECTED.getStatus()
					.equalsIgnoreCase(rcActionDTO.getActionStatus().toString())) {
				permitDetailsDTOOptinalInput.setPermitStatus(PermitsEnum.ACTIVE.getDescription());
			} else if (Status.permitSuspCanRevStatus.CANCELED.getStatus()
					.equalsIgnoreCase(rcActionDTO.getActionStatus().toString())) {
				permitDetailsDTOOptinalInput.setPermitStatus(PermitsEnum.INACTIVE.getDescription());
			}
		}
		Optional<RegistrationDetailsDTO> optinalInput = registrationDetailDAO.findByPrNo(suspensionVO.getPrNo());
		if (optinalInput.isPresent()) {
			if (rcActionDTO.getActionStatus().getStatus().equals(Status.RCActionStatus.SUSPEND.getStatus())) {
				optinalInput.get().setPermitActionStatus(rcActionDTO.getActionStatus().toString());
			} else if (rcActionDTO.getActionStatus().getStatus().equals(Status.RCActionStatus.REVOKED.getStatus())) {
				optinalInput.get().setPermitActionStatus(rcActionDTO.getActionStatus().toString());
			} else if (rcActionDTO.getActionStatus().getStatus().equals(Status.RCActionStatus.INITIATED.getStatus())) {
				optinalInput.get().setPermitActionStatus(rcActionDTO.getActionStatus().toString());
			} else if (rcActionDTO.getActionStatus().getStatus().equals(Status.RCActionStatus.REJECTED.getStatus())) {
				optinalInput.get().setPermitActionStatus(rcActionDTO.getActionStatus().toString());
			} else if (rcActionDTO.getActionStatus().getStatus().equals(Status.RCActionStatus.CANCELED.getStatus())) {
				optinalInput.get().setPermitActionStatus(rcActionDTO.getActionStatus().toString());
			}
		}
		registrationDetailDAO.save(optinalInput.get());
		permitDetailsDAO.save(permitDetailsDTOsList);

		sendPermitRevocationNotification(optinalInput.get());

		permitSuspCanRevDAO.save(rcActionDTO);

	}

	@Override
	public InputVO getEnclousersByServiceId() {
		InputVO inputVO = new InputVO();
		ImageInput imageInput2 = new ImageInput();
		List<ImageInput> imageInput = new ArrayList<>();
		imageInput2.setType("PERMIT ENCLOUSERS");
		imageInput2.setEnclosureName("PERMIT ENCLOUSERS");
		imageInput.add(imageInput2);
		inputVO.setImageInput(imageInput);

		return inputVO;

	}

	private void validationOfServices(SearchRcRequestVO searchRcRequestVO) {
		List<RegServiceDTO> listRegServices = regServiceDAO.findByPrNo(searchRcRequestVO.getPrNo());
		if (listRegServices != null && CollectionUtils.isNotEmpty(listRegServices)) {
			listRegServices.sort((p2, p1) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
			RegServiceDTO regServiceDTO = listRegServices.stream().findFirst().get();
			if (regServiceDTO.getServiceIds() != null) {
				if (!regServiceDTO.getServiceIds().isEmpty()) {
					if (regServiceDTO.getServiceIds().contains(ServiceEnum.ISSUEOFNOC.getId())) {
						throw new BadRequestException(
								"Issue of NOC is applied for this record can't suspend.Please do cancelation of NOC");
					}
				}
			}
		}
	}

	private void sendRevocationNotification(RegistrationDetailsDTO rcDetails) {

		Integer templateId = 0;
		if (rcDetails.getApplicantDetails() != null) {
			try {
				if (rcDetails.getActionStatus().equals(Status.RCActionStatus.SUSPEND.toString())) {

					templateId = MessageTemplate.RC_SUSPENSION.getId();
					sendNotificationsForActions(templateId, rcDetails);
				} else if (rcDetails.getActionStatus().equals(Status.RCActionStatus.CANCELED.toString())) {
					templateId = MessageTemplate.RC_CANCELLATION.getId();
					sendNotificationsForActions(templateId, rcDetails);
				} else if (rcDetails.getActionStatus().equals(Status.RCActionStatus.REVOKED.toString())) {
					templateId = MessageTemplate.RC_REVOKATION.getId();
					sendNotificationsForActions(templateId, rcDetails);
				}
			} catch (Exception e) {
				logger.debug("Exception [{}]", e);
				logger.error("Exception while sending revocation alerts reminder alerts:{}", e);
			}
		}
	}

	private void sendPermitRevocationNotification(RegistrationDetailsDTO rcDetails) {

		Integer templateId = 0;
		if (rcDetails.getApplicantDetails() != null) {
			try {
				if (rcDetails.getPermitActionStatus().equals(Status.RCActionStatus.SUSPEND.toString())) {
					templateId = MessageTemplate.P_SUSPENSION.getId();
					sendNotificationsForActions(templateId, rcDetails);
				} else if (rcDetails.getPermitActionStatus().equals(Status.RCActionStatus.CANCELED.toString())) {
					templateId = MessageTemplate.P_CANCELLATION.getId();
					sendNotificationsForActions(templateId, rcDetails);
				} else if (rcDetails.getPermitActionStatus().equals(Status.RCActionStatus.REVOKED.toString())) {
					templateId = MessageTemplate.P_REVOKATION.getId();
					sendNotificationsForActions(templateId, rcDetails);
				}
			} catch (Exception e) {
				logger.debug("Exception [{}]", e);
				logger.error("Exception while sending revocation alerts reminder alerts:{}", e);
			}
		}
	}

	private void validations(RCActionsDTO rcActionDTO, RCActionsVO suspensionVO) {

		if (rcActionDTO.getActionStatus().equals(Status.RCActionStatus.SUSPEND)
				&& suspensionVO.getActionStatus().equals(Status.RCActionStatus.SUSPEND)) {
			throw new BadRequestException("RC already suspended");
		}
		if (rcActionDTO.getActionStatus().equals(Status.RCActionStatus.CANCELED)
				&& suspensionVO.getActionStatus().equals(Status.RCActionStatus.SUSPEND)) {
			throw new BadRequestException("Cancelled application can't be suspended");
		}
		if (rcActionDTO.getActionStatus().equals(Status.RCActionStatus.INITIATED)
				&& suspensionVO.getActionStatus().equals(Status.RCActionStatus.INITIATED)) {
			throw new BadRequestException("RC Already Initated");
		}
		if (rcActionDTO.getActionStatus().equals(Status.RCActionStatus.REVOKED)
				&& suspensionVO.getActionStatus().equals(Status.RCActionStatus.REVOKED)) {
			throw new BadRequestException("RC Already Revoked");
		}
	}

	private void permitValidations(PermitSuspCanRevDTO rcActionDTO, PermitSuspCanRevVO suspensionVO) {

		if (rcActionDTO.getActionStatus().getStatus().equalsIgnoreCase(Status.RCActionStatus.SUSPEND.getStatus())
				&& suspensionVO.getActionStatus().getStatus()
						.equalsIgnoreCase(Status.RCActionStatus.SUSPEND.getStatus())) {
			throw new BadRequestException("permit already suspended");
		}
		if (rcActionDTO.getActionStatus().getStatus().equalsIgnoreCase(Status.RCActionStatus.CANCELED.getStatus())
				&& suspensionVO.getActionStatus().getStatus()
						.equalsIgnoreCase(Status.RCActionStatus.SUSPEND.getStatus())) {
			throw new BadRequestException("Cancelled application can't be suspended");
		}
		if (rcActionDTO.getActionStatus().getStatus().equalsIgnoreCase(Status.RCActionStatus.INITIATED.getStatus())
				&& suspensionVO.getActionStatus().getStatus()
						.equalsIgnoreCase(Status.RCActionStatus.INITIATED.getStatus())) {
			throw new BadRequestException("permit Already Initated");
		}
		if (rcActionDTO.getActionStatus().getStatus().equalsIgnoreCase(Status.RCActionStatus.REVOKED.getStatus())
				&& suspensionVO.getActionStatus().getStatus()
						.equalsIgnoreCase(Status.RCActionStatus.REVOKED.getStatus())) {
			throw new BadRequestException("permit Already Revoked");
		}
	}

	@Override
	public List<RCActionRulesVO> getAllActionRules() {

		List<RCActionRulesDTO> allActionRules = rcActionRulesDAO.findAll();
		allActionRules.removeIf(p -> p.getSource().equals(SuspensionSources.NOTROADWORTHY));
		allActionRules.removeIf(p -> p.getSource().equals(SuspensionSources.OTHERS));
		return actionRulesMapper.convertEntity(allActionRules);
	}

	@Override
	public List<RCActionRulesVO> getActionSectionsBasedOnSource(String sourceName) {
		List<RCActionRulesDTO> allActionRules = rcActionRulesDAO.findBySource(sourceName);
		allActionRules.removeIf(p -> p.getSource().equals(SuspensionSources.NOTROADWORTHY));
		allActionRules.removeIf(p -> p.getSource().equals(SuspensionSources.OTHERS));
		return actionRulesMapper.convertEntity(allActionRules);
	}

	@Override
	public String saveObjectionDetails(RegServiceVO regServiceVO, JwtUser jwtUser) {

		RegServiceVO registrationDetails = registrationService.findByprNo(regServiceVO.getPrNo());

		if (null != registrationDetails && registrationDetails.getServiceType().contains(ServiceEnum.OBJECTION)
				&& registrationDetails.getTheftDetails() != null
				&& !registrationDetails.getTheftDetails().getStatus().equals(TheftState.REVOKED.toString())) {
			logger.debug("THEFT OBJECTION Request is Pending " + "[{}]", regServiceVO.getPrNo());
			throw new BadRequestException("THEFT OBJECTION Request is Pending " + regServiceVO.getPrNo());
		}

		Optional<RegistrationDetailsDTO> registrationOptional = registrationDetailDAO
				.findByPrNo(regServiceVO.getPrNo());

		if (!registrationOptional.isPresent()) {
			logger.debug(MessageKeys.MESSAGE_NO_RECORD_FOUND + "[{}]", regServiceVO.getPrNo());
			throw new BadRequestException(MessageKeys.MESSAGE_NO_RECORD_FOUND + regServiceVO.getPrNo());
		}

		TheftVehicleDetailsDTO theftVehicleDetailsDTO = new TheftVehicleDetailsDTO();

		theftVehicleDetailsDTO.setRemarks(regServiceVO.getTheftDetails().getRemarks());
		theftVehicleDetailsDTO.setStatus(TheftState.OBJECTION.toString());

		RegServiceDTO regServiceDTO = new RegServiceDTO();
		regServiceDTO.setPrNo(regServiceVO.getPrNo());
		regServiceDTO.setServiceIds(Stream.of(ServiceEnum.OBJECTION.getId()).collect(Collectors.toSet()));
		regServiceDTO.setServiceType(Arrays.asList(ServiceEnum.OBJECTION));
		regServiceDTO.setRegistrationDetails(registrationOptional.get());
		regServiceDTO.setTheftDetails(theftVehicleDetailsDTO);
		regServiceDTO.setOfficeDetails(registrationOptional.get().getOfficeDetails());
		regServiceDTO.setOfficeCode(regServiceDTO.getOfficeDetails().getOfficeCode());
		if (null == regServiceDTO.getApplicationNo()) {
			Map<String, String> officeCodeMap = new TreeMap<>();
			officeCodeMap.put("officeCode", regServiceDTO.getRegistrationDetails().getOfficeDetails().getOfficeCode());
			regServiceDTO.setApplicationNo(sequenceGenerator
					.getSequence(String.valueOf(Sequence.REGISTRATIONAPPNO.getSequenceId()), officeCodeMap));
		}
		regServiceDTO.setApplicationStatus(StatusRegistration.INITIATED);
		regServiceDTO.setCreatedDate(LocalDateTime.now());
		registratrionServicesApprovals.initiateApprovalProcessFlow(regServiceDTO);
		regServiceDTO.setCurrentRoles(new HashSet<String>(Arrays.asList(RoleEnum.AO.getName())));
		ActionDetails actionDetail = getActionDetailByRole(regServiceDTO, RoleEnum.CCO.getName());
		updateActionDetailsStatus(RoleEnum.CCO.getName(), jwtUser.getId(), StatusRegistration.APPROVED.toString(),
				actionDetail, regServiceDTO.getApplicationNo());
		regServiceDTO.setCurrentIndex(2);
		regServiceDAO.save(regServiceDTO);
		return regServiceDTO.getApplicationNo();

	}

	private ActionDetails getActionDetailByRole(RegServiceDTO regServiceDTO, String role) {

		Optional<ActionDetails> actionDetailsOpt = regServiceDTO.getActionDetails().stream()
				.filter(p -> role.equals(p.getRole())).findFirst();
		if (!actionDetailsOpt.isPresent()) {
			throw new BadRequestException("User role " + role + " specific details not found in actiondetail");
		}
		return actionDetailsOpt.get();
	}

	private void updateActionDetailsStatus(String role, String userId, String status, ActionDetails actionDetail,
			String appNo) {
		actionDetail.setApplicationNo(appNo);
		actionDetail.setRole(role);
		actionDetail.setUserId(userId);
		// actionDetail.setEnclosures(actionVo.getEnclosures());
		actionDetail.setIsDoneProcess(Boolean.TRUE);
		actionDetail.setStatus(status);
		actionDetail.setlUpdate(LocalDateTime.now());
	}

	@Override
	public RegistrationDetailsVO findBasedOnPrNo(String prNo) {
		Optional<RegistrationDetailsDTO> optionalDto = registrationDetailDAO.findByPrNo(prNo);
		if (optionalDto.isPresent()) {
			RegistrationDetailsVO registrationDetailsVO = regDetailMapper.convertEntity(optionalDto.get());
			return registrationDetailsVO;
		}
		throw new BadRequestException("No record found based on prNo");
	}

	@Autowired
	private RegistrationMigrationSolutionsService registrationMigrationSolutionsService;

	@Override
	public RegistrationDetailsVO findBasedOnPrNos(List<String> prNos) {
		List<RegistrationDetailsDTO> registrationDetailsList = registrationMigrationSolutionsService
				.removeInactiveRecordsToList(registrationDetailDAO.findByPrNoIn(prNos));
		if (!registrationDetailsList.isEmpty()) {
			RegistrationDetailsVO registrationDetailsVO = regDetailMapper.convertEntity(registrationDetailsList.get(0));
			return registrationDetailsVO;
		}
		throw new BadRequestException("No record found based on prNo");
	}

	@Override
	public void saveRevocationDetails(RegServiceVO regServiceVO, JwtUser jwtUser) {
		List<RegServiceDTO> regOptional = regServiceDAO.findByPrNoAndServiceTypeNotIn(regServiceVO.getPrNo(),
				ServiceEnum.getServiceEnumById(ServiceEnum.TAXATION.getId()));

		if (regOptional.isEmpty()) {
			logger.debug(MessageKeys.MESSAGE_NO_RECORD_FOUND + "[{}]", regServiceVO.getApplicationNo());
			throw new BadRequestException(MessageKeys.MESSAGE_NO_RECORD_FOUND + regServiceVO.getApplicationNo());
		}
		regOptional.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		RegServiceDTO regDTO = regOptional.stream().findFirst().get();

		Optional<RegistrationDetailsDTO> registrationOptional = registrationDetailDAO.findByPrNo(regDTO.getPrNo());
		if (registrationOptional.isPresent() && null == registrationOptional.get().getTheftState()) {
			logger.debug("Approval pending at AO, Please Verify once." + "[{}]", regServiceVO.getApplicationNo());
			throw new BadRequestException(
					"Approval pending at AO, Please Verify once." + regServiceVO.getApplicationNo());
		}
		if (!registrationOptional.get().getTheftState().equals(TheftState.INTIMATIATED)) {
			logger.error("Please apply objection before revoke [{}]", registrationOptional.get().getPrNo());
			throw new BadRequestException(
					"Please apply objection before revoke." + registrationOptional.get().getPrNo());
		}
		if (null == regDTO.getTheftDetailsLog())
			regDTO.setTheftDetailsLog(new ArrayList<>());
		regDTO.getTheftDetailsLog().add(regDTO.getTheftDetails());

		TheftVehicleDetailsDTO theftVehicleDetailsDTO = new TheftVehicleDetailsDTO();
		theftVehicleDetailsDTO.setRemarks(regServiceVO.getTheftDetails().getRemarks());
		theftVehicleDetailsDTO.setStatus(TheftState.REVOKED.toString());

		regDTO.setTheftDetails(theftVehicleDetailsDTO);
		regDTO.setApplicationStatus(StatusRegistration.APPROVED);

		registrationOptional.get().setTheftState(TheftState.REVOKED);

		registrationDetailDAO.save(registrationOptional.get());
		regServiceDAO.save(regDTO);

	}

	@Override
	public RegistrationDetailsVO getSecondVehicleDetails(UserDTO userDetails, RtaActionVO actionActionVo) {
		Optional<StagingRegistrationDetailsDTO> optionalStagingDetails = secondVehicleValidations(userDetails,
				actionActionVo);
		StagingRegistrationDetailsDTO dto = optionalStagingDetails.get();
		dto.getRejectionHistoryLog().add(dto.getRejectionHistory());
		return regDetailMapper.convertEntity(dto);
	}

	@Override
	public Boolean releaseSecondVehicle(UserDTO userDetails, RtaActionVO actionActionVo) {
		Optional<StagingRegistrationDetailsDTO> optionalStagingDetails = secondVehicleValidations(userDetails,
				actionActionVo);
		StagingRegistrationDetailsDTO dto = optionalStagingDetails.get();
		/*
		 * if(actionActionVo.getStatus() == null) {
		 * logger.info("Please provide the application status."); throw new
		 * BadRequestException("Please provide the application status."); }
		 * if(!actionActionVo.getStatus().equals(StatusRegistration.APPROVED)) {
		 * logger.info("Only approve the application."); throw new
		 * BadRequestException("Only approve the application."); }
		 */
		checkValidtionForRtoAadharValiadtion(userDetails, actionActionVo.getAadhaarDetailsRequestVO());
		boolean needToSkipsaveStaging = Boolean.FALSE;
		RejectionHistoryDTO rejectionHistoryDTO = new RejectionHistoryDTO();
		dto.getRejectionHistoryLog().add(dto.getRejectionHistory());
		rejectionHistoryDTO.setIsSecondVehicleRejected(Boolean.FALSE);
		rejectionHistoryDTO.setActionBy(userDetails.getUserId());
		rejectionHistoryDTO.setRole(actionActionVo.getSelectedRole());
		rejectionHistoryDTO.setRemarks(actionActionVo.getRemarks());
		rejectionHistoryDTO.setSecondVehicleExcemption(Boolean.TRUE);
		SecondVehicleExcemptionDetails secondVehiclesDetails = new SecondVehicleExcemptionDetails();
		secondVehiclesDetails.setIsSecondVehicleRejected(Boolean.TRUE);
		secondVehiclesDetails.setActionBy(userDetails.getUserId());
		secondVehiclesDetails.setRole(actionActionVo.getSelectedRole());
		secondVehiclesDetails.setRemarks(actionActionVo.getRemarks());
		secondVehiclesDetails.setApplicationNo(dto.getApplicationNo());
		secondVehiclesDetails.setSecondVehicleId(dto.getRejectionHistory().getSecondVehicleId());
		secondVehiclesDetails.setClassOfVehicle(dto.getClassOfVehicle());
		secondVehiclesDetails.setActionTime(LocalDateTime.now());
		dto.setRejectionHistory(rejectionHistoryDTO);
		if (dto.getApplicationStatus().equals(StatusRegistration.SECORINVALIDFAILED.getDescription())) {
			if (dto.getEnclosures().stream().anyMatch(valu -> valu.getValue().stream().anyMatch(
					status -> status.getImageStaus().equalsIgnoreCase(StatusRegistration.REUPLOAD.getDescription())))) {
				dto.setApplicationStatus(StatusRegistration.REUPLOAD.getDescription());
			}
		}
		if (!(dto.getEnclosures().stream().anyMatch(valu -> valu.getValue().stream().anyMatch(
				status -> status.getImageStaus().equalsIgnoreCase(StatusRegistration.REUPLOAD.getDescription())))
				|| dto.getEnclosures().stream().anyMatch(valu -> valu.getValue().stream().anyMatch(status -> status
						.getImageStaus().equalsIgnoreCase(StatusRegistration.REJECTED.getDescription()))))) {
			if (dto.getSpecialNumberRequired() && StringUtils.isBlank(dto.getPrNo())) {
				dto.setApplicationStatus(StatusRegistration.SPECIALNOPENDING.getDescription());

			} else {
				try {
					if (prService.isAssignNumberNow() || StringUtils.isNotBlank(dto.getPrNo())) {
						this.processPR(dto);
						needToSkipsaveStaging = Boolean.TRUE;
					} else {
						dto.setApplicationStatus(StatusRegistration.PRNUMBERPENDING.getDescription());
					}
				} catch (Exception e) {
					logger.debug("Exception while assign PR: {}", e);
					logger.error("Exception while assign PR: {}", e.getMessage());
				}
			}
		}
		secondVehicleExcemptionDAO.save(secondVehiclesDetails);
		if (!needToSkipsaveStaging) {
			stagingRegistrationDetailsDAO.save(dto);
		}

		return Boolean.TRUE;
	}

	private Optional<StagingRegistrationDetailsDTO> secondVehicleValidations(UserDTO userDetails,
			RtaActionVO actionActionVo) {
		Optional<StagingRegistrationDetailsDTO> optionalStagingDetails = Optional.empty();
		String searchNo;
		if (!actionActionVo.getSelectedRole().equalsIgnoreCase(RoleEnum.RTO.getName())) {
			logger.error("user not an  RTO. ", actionActionVo.getSelectedRole());
			throw new BadRequestException("user not an  RTO.the user is: " + actionActionVo.getSelectedRole());
		}
		if (StringUtils.isNoneBlank(actionActionVo.getApplicationNo())) {
			optionalStagingDetails = stagingRegistrationDetailsDAO
					.findByApplicationNo(actionActionVo.getApplicationNo());
			searchNo = actionActionVo.getApplicationNo();
		} else if (StringUtils.isNoneBlank(actionActionVo.getTrNumber())) {
			optionalStagingDetails = stagingRegistrationDetailsDAO.findByTrNo(actionActionVo.getTrNumber());
			searchNo = actionActionVo.getTrNumber();
		} else {
			logger.error("Plese provide application number or tr number.");
			throw new BadRequestException("Plese provide application number or tr number.");
		}
		if (!optionalStagingDetails.isPresent()) {
			logger.error("No records found. ", searchNo);
			throw new BadRequestException("No records found. " + searchNo);
		}
		StagingRegistrationDetailsDTO dto = optionalStagingDetails.get();
		if (!dto.getOfficeDetails().getOfficeCode().equalsIgnoreCase(userDetails.getOffice().getOfficeCode())) {
			logger.error("Invalid user. ", searchNo);
			throw new BadRequestException(
					"This " + searchNo + " belongs to " + dto.getOfficeDetails().getOfficeCode() + " office");
		}
		if (dto.getRejectionHistory() == null) {
			logger.error("rejection histroy not found. ", searchNo);
			throw new BadRequestException("rejection histroy not found. " + searchNo);
		}
		if (StringUtils.isBlank(dto.getRejectionHistory().getRole())
				|| !dto.getRejectionHistory().getRole().equalsIgnoreCase(RoleEnum.RTO.getName())) {
			logger.error("application not rejected by RTO. ", searchNo);
			throw new BadRequestException("application not rejected by RTO. " + searchNo);
		}
		if (!dto.getRejectionHistory().getIsSecondVehicleRejected()) {
			logger.error("application not rejected for the reason of second vehicle. ", searchNo);
			throw new BadRequestException("application not rejected for the reason of second vehicle" + searchNo);
		}
		return optionalStagingDetails;
	}

	@Override
	public String doActionForFC(RegServiceVO regServiceVO, JwtUser jwtUser, MultipartFile[] uploadfiles)
			throws IOException {

		RegServiceDTO regServiceDTO = registrationService.returnLatestFcDoc(regServiceVO.getPrNo());
		if (!((regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWFC.getId()))
				|| regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RENEWALFC.getId()))
				|| regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.OTHERSTATIONFC.getId())))
				&& regServiceDTO.getServiceIds().size() == 1)) {
			logger.error("Application not belong to fitness service.");
			throw new BadRequestException("Application not belong to fitness service.");
		}
		if (!isApprovedRTO(regServiceDTO)) {
			logger.error("Approval Pending at RTO.Pr no:", regServiceDTO.getPrNo());
			throw new BadRequestException("Approval Pending From RTO.Pr no: " + regServiceDTO.getPrNo());

		}
		if (regServiceVO.getApplicationStatus().equals(StatusRegistration.APPROVED)) {
			if (null == regServiceVO.getImages() || regServiceVO.getImages().isEmpty()) {
				logger.error("Images not found from UI");
				throw new BadRequestException("Images not found from UI");
			}
			if (!registrationService.isallowImagesInapp(regServiceDTO.getMviOfficeCode())) {
				if (uploadfiles == null || uploadfiles.length == 0) {
					logger.error("Please upload the images sdfsdf");
					throw new BadRequestException("Please upload the images sdfsdf");
				}
			}
		}
		RtaActionVO vo = new RtaActionVO();
		vo.setAction(regServiceVO.getApplicationStatus().toString());
		vo.setImages(regServiceVO.getImages());
		vo.setFcValidityValue(regServiceVO.getFcValidityValue());
		vo.setFcQstList(regServiceVO.getFcQstList());
		vo.setApplicationNo(regServiceDTO.getApplicationNo());
		vo.setSelectedRole(regServiceVO.getSelectedRole());
		AlterationVO altVo = new AlterationVO();
		altVo.setFcType(regServiceVO.getFcType());
		vo.setFcType(regServiceVO.getFcType());
		vo.setAlterationVO(altVo);
		vo.setStatus(regServiceVO.getApplicationStatus());
		vo.setReasonForRejection(regServiceVO.getReasonForRejection());
		vo.setAadharResponseMVI(regServiceVO.getAadhaarResponse());
		vo.setFitnessforMVI(regServiceVO.getFitnessforMVI());
		if (regServiceVO.getReInspectionDate() != null && (regServiceVO.getReInspectionDate().isBefore(LocalDate.now())
				|| regServiceVO.getReInspectionDate().isEqual(LocalDate.now()))) {
			logger.error("Re-inspection date should be future date");
			throw new BadRequestException("Re-inspection date should be future date");
		}
		vo.setReInspectionDate(regServiceVO.getReInspectionDate());
		Optional<List<MasterFcQuestionVO>> voList = this.getMVIQuestionFOrFC(regServiceVO.getSelectedRole());
		if (!voList.isPresent()) {
			logger.error("No master data for quations");
			throw new BadRequestException("No master data for quations");
		}
		boolean flag = Boolean.FALSE;
		List<MasterFcQuestionVO> listOfQuation = voList.get();
		for (MasterFcQuestionVO quationVo : listOfQuation) {
			for (MasterFcQuestionVO quationFromUi : regServiceVO.getFcQstList()) {
				if (quationVo.getSerialNo().equalsIgnoreCase(quationFromUi.getSerialNo())) {
					if (quationVo.isIscfrr()) {
						if (!quationFromUi.isIscfrr()) {
							flag = Boolean.TRUE;
						}
					}
				}
			}
			if (flag) {
				break;
			}
		}
		if (flag) {
			if (!regServiceVO.getApplicationStatus().equals(StatusRegistration.REJECTED)) {
				logger.error("Invalid application status for UI");
				throw new BadRequestException("Invalid application status for UI");
			}
		}
		registratrionServicesApprovals.approvalProcess(jwtUser, vo, regServiceVO.getSelectedRole(), uploadfiles);
		/*
		 * if (!regServiceVO.getFcQstList().isEmpty())
		 * regServiceDTO.setFcQstList(fcQuestionsMapper.convertVO(regServiceVO.
		 * getFcQstList()));
		 * 
		 * if (regServiceVO.getApplicationStatus().equals(StatusRegistration. APPROVED))
		 * { regServiceVO.setImageInput(regServiceVO.getImages()); regServiceDTO =
		 * registrationService.saveCitizenServiceDoc(regServiceVO, regServiceDTO,
		 * uploadfiles); Optional<StagingFcDetailsDTO> StagingFcDetailsDTOOpt =
		 * stageFcDetailsDAO .findByApplicationNo(regServiceDTO.getApplicationNo());
		 * StagingFcDetailsDTO stagingFcDetailsDTO = StagingFcDetailsDTOOpt.get();
		 * FcDetailsDTO fcDetailsDTO =
		 * fcDetailsMapper.convertStageEntity(stagingFcDetailsDTO);
		 * fcDetailsDTO.setFcvalidfrom(LocalDate.now()); LocalDateTime date =
		 * calculateFcValidity(regServiceDTO, regServiceVO);
		 * fcDetailsDTO.setFcValidUpto(date.toLocalDate());
		 * 
		 * if(regServiceVO.getFcValidityValue()==24){
		 * fcDetailsDTO.setFcValidUpto(LocalDate.now().minusDays(1).plusYears(
		 * ValidityEnum.FCVALIDITY.getValidity())); }else
		 * if(regServiceVO.getFcValidityValue()==12){
		 * fcDetailsDTO.setFcValidUpto(LocalDate.now().minusDays(1).plusYears(1) );
		 * }else if(regServiceVO.getFcValidityValue()==6){
		 * fcDetailsDTO.setFcValidUpto(LocalDate.now().minusDays(1).plusMonths(6 )); }
		 * ActionDetails actionDetails = this.getActionDetailByRole(regServiceDTO,
		 * RoleEnum.MVI.getName());
		 * 
		 * MasterUsersDTO userDto =
		 * masterUsersDAO.findByUserId(regServiceVO.getUserId()); if (userDto != null) {
		 * fcDetailsDTO.setInspectedMviName(userDto.getFirstName()); }
		 * fcDetailsDAO.save(fcDetailsDTO);
		 * 
		 * Optional<RegistrationDetailsDTO> registrationOptional = registrationDetailDAO
		 * .findByPrNo(regServiceVO.getPrNo());
		 * registrationOptional.get().getRegistrationValidity().
		 * setRegistrationValidity( date);
		 * registrationOptional.get().getRegistrationValidity().setFcValidity( date.
		 * toLocalDate()); if (null != regServiceVO.getContactDetails()) {
		 * registrationOptional.get().getApplicantDetails().getContact()
		 * .setMobile(regServiceVO.getContactDetails().getMobile());
		 * registrationOptional.get().getApplicantDetails().getContact()
		 * .setEmail(regServiceVO.getContactDetails().getEmail()); }
		 * registrationDetailDAO.save(registrationOptional.get());
		 * 
		 * regServiceDTO.setApplicationStatus(StatusRegistration.APPROVED);
		 * regServiceDAO.save(regServiceDTO); } else {
		 * regServiceDTO.setApplicationStatus(StatusRegistration.REJECTED);
		 * regServiceDTO.setReInspectionDate(regServiceVO.getReInspectionDate()) ;
		 * regServiceDTO.setReasonForRejection(regServiceVO. getReasonForRejection());
		 * regServiceDAO.save(regServiceDTO); }
		 */
		return regServiceDTO.getRegistrationDetails().getApplicationNo();

	}

	@Override
	public Optional<List<MasterFcQuestionVO>> getMVIQuestionFOrFC(String selectedRole) {
		List<MasterFCQuestionsDTO> list = masterFcQuestionsDAO.findAll();
		return Optional.of(fcQuestionsMapper.convertEntity(list));
	}

	/*
	 * private LocalDateTime calculateFcValidity(RegServiceDTO regServiceDTO,
	 * RegServiceVO regServiceVO) { LocalDateTime fcValidity =
	 * LocalDateTime.now().minusDays(1).plusYears(1);
	 * 
	 * if (regServiceVO.getFcValidityValue() == 24) { fcValidity =
	 * LocalDateTime.now().minusDays(1).plusYears(2); fcValidity =
	 * calculateFcForEibt(regServiceDTO, fcValidity, 2); } else if
	 * (regServiceVO.getFcValidityValue() == 12) { fcValidity =
	 * LocalDateTime.now().minusDays(1).plusYears(1); fcValidity =
	 * calculateFcForEibt(regServiceDTO, fcValidity, 1); } else if
	 * (regServiceVO.getFcValidityValue() == 6) { fcValidity =
	 * LocalDateTime.now().minusDays(1).plusMonths(6); fcValidity =
	 * calculateFcForEibt(regServiceDTO, fcValidity, 1); }
	 * 
	 * return fcValidity; }
	 * 
	 * private LocalDateTime calculateFcForEibt(RegServiceDTO regServiceDTO,
	 * LocalDateTime fcValidity, int years) { if
	 * (regServiceDTO.getRegistrationDetails().getClassOfVehicle() != null &&
	 * StringUtils.isNoneBlank(regServiceDTO.getRegistrationDetails().
	 * getClassOfVehicle()) &&
	 * regServiceDTO.getRegistrationDetails().getClassOfVehicle()
	 * .equalsIgnoreCase(ClassOfVehicleEnum.EIBT.getCovCode())) {
	 * LocalDate.now().getMonth(); int val =
	 * LocalDate.now().getMonth().compareTo(Month.MAY); int year; if (val > 0) {
	 * year = LocalDateTime.now().getYear() + years; } else if (val == 0) { if
	 * (LocalDate.now().getDayOfMonth() <= 15) { year =
	 * LocalDateTime.now().getYear(); } else { year = LocalDateTime.now().getYear()
	 * + years; } } else { year = LocalDateTime.now().getYear(); } fcValidity =
	 * LocalDateTime.of(year, Month.MAY, 15, 0, 0); } return fcValidity; }
	 */

	@Override
	public Integer getCcoSuspendPending(String officeCode, String id, String role) {
		List<RCActionsDTO> actionDtoList = null;
		if (role.equalsIgnoreCase(RoleEnum.AO.getName())) {
			actionDtoList = suspensionDAO.findByActionStatusAndOfficeCode(Status.RCActionStatus.INITIATED, officeCode);
			if (actionDtoList != null) {
				List<String> prNos = actionDtoList.stream().map(RCActionsDTO::getPrNo).collect(Collectors.toList());
				return getCcoInitiatedSuspendCountForRc(prNos);
			}
		} else if (role.equalsIgnoreCase(RoleEnum.RTO.getName())) {
			actionDtoList = suspensionDAO.findByActionStatusAndOfficeCode(Status.RCActionStatus.SUSPEND, officeCode);
			if (actionDtoList != null) {
				return actionDtoList.size();
			}
		}
		return 0;
	}

	@Override
	public List<RCActionsVO> getPendingListOfSuspend(String officeCode, String user, String role) {
		List<RCActionsDTO> rcActionsDTOs = null;
		if (role.contains(RoleEnum.AO.getName())) {
			rcActionsDTOs = suspensionDAO.findByActionStatusAndOfficeCode(Status.RCActionStatus.INITIATED, officeCode);
			if (rcActionsDTOs != null) {
				List<String> prNos = rcActionsDTOs.stream().map(RCActionsDTO::getPrNo).collect(Collectors.toList());
				List<RegistrationDetailsDTO> regDtoList = getCcoInitiatedSuspendlListForRc(prNos);
				List<String> regPrList = regDtoList.stream().map(RegistrationDetailsDTO::getPrNo)
						.collect(Collectors.toList());

				rcActionsDTOs = suspensionDAO.findByActionStatusAndOfficeCodeAndPrNoIn(Status.RCActionStatus.INITIATED,
						officeCode, regPrList);
				/*
				 * rcActionsDTOs.sort((p1, p2) ->
				 * p2.getRcActionsDetails().getActionDate().compareTo(p1. getRcActionsDetails().
				 * getActionDate())); if(rcActionsDTOs.stream().findFirst().isPresent()){
				 * rcActionsDTO=rcActionsDTOs.stream().findFirst().get(); }
				 */
				return suspensionMapper.convertEntity(rcActionsDTOs);
			}
		}
		if (role.contains(RoleEnum.RTO.getName())) {
			rcActionsDTOs = suspensionDAO.findByActionStatusAndOfficeCode(Status.RCActionStatus.SUSPEND, officeCode);
			if (rcActionsDTOs != null) {
				List<String> prNos = rcActionsDTOs.stream().map(RCActionsDTO::getPrNo).collect(Collectors.toList());
				List<RegistrationDetailsDTO> regDtoList = getRtoInitiatedSuspendlListForRc(prNos);
				List<String> regPrList = regDtoList.stream().map(RegistrationDetailsDTO::getPrNo)
						.collect(Collectors.toList());

				rcActionsDTOs = suspensionDAO.findByActionStatusAndOfficeCodeAndPrNoIn(Status.RCActionStatus.SUSPEND,
						officeCode, regPrList);
				/*
				 * rcActionsDTOs.sort((p1, p2) ->
				 * p2.getRcActionsDetails().getActionDate().compareTo(p1. getRcActionsDetails().
				 * getActionDate())); if(rcActionsDTOs.stream().findFirst().isPresent()){
				 * rcActionsDTO=rcActionsDTOs.stream().findFirst().get(); }
				 */
				return suspensionMapper.convertEntity(rcActionsDTOs);
			}
		}
		return null;
	}

	private List<RegistrationDetailsDTO> getRtoInitiatedSuspendlListForRc(List<String> prNos) {
		List<RegistrationDetailsDTO> regInitatedList = registrationDetailDAO.findByPrNoInAndActionStatus(prNos,
				RCActionStatus.SUSPEND.getStatus());
		return regInitatedList;

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
	public Integer getCcoPermitSuspendCount(String officeCode, String id, String role) {
		List<PermitSuspCanRevDTO> actionDtoList = null;
		if (role.equalsIgnoreCase(RoleEnum.AO.getName())) {
			actionDtoList = permitSuspCanRevDAO.findByActionStatusAndOfficeCode(Status.RCActionStatus.INITIATED,
					officeCode);
			if (actionDtoList != null) {
				List<String> prNos = actionDtoList.stream().map(PermitSuspCanRevDTO::getPrNo)
						.collect(Collectors.toList());
				Integer regCount = getCcoInitiatedSuspendCount(prNos);
				return regCount;
			}
		} else if (role.equalsIgnoreCase(RoleEnum.RTO.getName())) {
			actionDtoList = permitSuspCanRevDAO.findByActionStatusAndOfficeCode(Status.RCActionStatus.SUSPEND,
					officeCode);
			if (actionDtoList != null) {
				return actionDtoList.size();
			}
		}
		return 0;
	}

	/**
	 * RC suspension related count and list
	 * 
	 * @param prNos
	 * @return
	 */
	private Integer getCcoInitiatedSuspendCountForRc(List<String> prNos) {
		List<RegistrationDetailsDTO> regInitatedList = registrationDetailDAO.findByPrNoInAndActionStatus(prNos,
				RCActionStatus.INITIATED.getStatus());
		regInitatedList.size();

		return regInitatedList.size();

	}

	private List<RegistrationDetailsDTO> getCcoInitiatedSuspendlListForRc(List<String> prNos) {
		List<RegistrationDetailsDTO> regInitatedList = registrationDetailDAO.findByPrNoInAndActionStatus(prNos,
				RCActionStatus.INITIATED.getStatus());
		return regInitatedList;

	}

	/**
	 * permit related count and list
	 * 
	 * @param prNos
	 * @return
	 */
	private Integer getCcoInitiatedSuspendCount(List<String> prNos) {
		List<RegistrationDetailsDTO> regInitatedList = registrationDetailDAO.findByPrNoInAndPermitActionStatus(prNos,
				permitSuspCanRevStatus.INITIATED.getStatus());
		regInitatedList.size();

		return regInitatedList.size();

	}

	private List<RegistrationDetailsDTO> getCcoInitiatedSuspendlList(List<String> prNos) {
		List<RegistrationDetailsDTO> regInitatedList = registrationDetailDAO.findByPrNoInAndPermitActionStatus(prNos,
				permitSuspCanRevStatus.INITIATED.getStatus());
		return regInitatedList;

	}

	@Override
	public List<PermitSuspCanRevVO> getPermitPendingListOfSuspend(String officeCode, String user, String role) {
		List<PermitSuspCanRevDTO> rcActionsDTOs = null;
		if (role.contains(RoleEnum.AO.getName())) {
			rcActionsDTOs = permitSuspCanRevDAO.findByActionStatusAndOfficeCode(Status.RCActionStatus.INITIATED,
					officeCode);
			if (rcActionsDTOs != null) {
				List<String> prNos = rcActionsDTOs.stream().map(PermitSuspCanRevDTO::getPrNo)
						.collect(Collectors.toList());
				List<RegistrationDetailsDTO> regDtoList = getCcoInitiatedSuspendlList(prNos);
				List<String> regPrList = regDtoList.stream().map(RegistrationDetailsDTO::getPrNo)
						.collect(Collectors.toList());

				rcActionsDTOs = permitSuspCanRevDAO.findByActionStatusAndOfficeCodeAndPrNoIn(
						Status.RCActionStatus.INITIATED, officeCode, regPrList);

				return permitSuspCanRevMapper.convertEntity(rcActionsDTOs);
			}
		}
		return null;
	}

	@Override
	public List<BreakPaymentsSaveDTO> fetchBreakPaymentDetails(String applicationNo) {
		BreakPaymentsSaveDTO breakPaymentsNum = null;
		List<BreakPaymentsSaveDTO> breakPaymentsNumList = new ArrayList<>();
		List<PaymentTransactionDTO> paymentTransList = paymentTransactionDAO
				.findByApplicationFormRefNumAndPayStatus(applicationNo, "success");
		if (paymentTransList.size() == 1 && paymentTransList != null) {
			for (PaymentTransactionDTO paymentTransactionDTO : paymentTransList) {
				breakPaymentsNum = paymentTransactionDTO.getBreakPaymentsSave();
				breakPaymentsNumList.add(breakPaymentsNum);
			}
		} else if (paymentTransList.size() > 1 && paymentTransList != null) {
			for (PaymentTransactionDTO paymentTransactionDTO : paymentTransList) {
				breakPaymentsNum = paymentTransactionDTO.getBreakPaymentsSave();
				breakPaymentsNumList.add(breakPaymentsNum);
			}
		}
		findByAppNo(applicationNo, breakPaymentsNumList, breakPaymentsNum);

		return breakPaymentsNumList;
	}

	public Float findByAppNo(String applicationNo, List<BreakPaymentsSaveDTO> breakPaymentsNumList,
			BreakPaymentsSaveDTO breakPaymentsDTO) {
		Optional<MasterAmountSecoundCovsDTO> masterAmountSecoundCovsOpt = null;
		Float percent = 1F;
		Optional<StagingRegistrationDetailsDTO> stagingOptional = stagingRegistrationDetailsDAO
				.findByApplicationNo(applicationNo);
		if (stagingOptional.isPresent()) {
			StagingRegistrationDetailsDTO stagingDTO = stagingOptional.get();
			String cov = stagingDTO.getClassOfVehicle();
			masterAmountSecoundCovsOpt = masterAmountSecoundCovsService.findByCovCode(cov);
			if (masterAmountSecoundCovsOpt.isPresent()) {
				splitTaxAmounts(breakPaymentsNumList, stagingDTO, breakPaymentsDTO);
			}
		}
		return percent;
	}

	public void splitTaxAmounts(List<BreakPaymentsSaveDTO> breakPaymentsNumList,
			StagingRegistrationDetailsDTO stagingDTO, BreakPaymentsSaveDTO breakPaymentsDTO) {
		Optional<MasterTax> masterTaxOptional = masterTaxDAO
				.findByCovcodeAndOwnershiptypeIgnoreCaseAndStatecodeAndFromageAndStatus(stagingDTO.getClassOfVehicle(),
						OwnerTypeEnum.Individual.getCode(), "AP", 0, "Recent");
		Float percent;
		if (masterTaxOptional.isPresent()) {
			MasterTax masterTax = masterTaxOptional.get();
			percent = masterTax.getPercent();
			List<TaxDetailsDTO> taxDetailsDtoList = taxDetailsDAO
					.findFirst10ByApplicationNoOrderByCreatedDateDesc(stagingDTO.getApplicationNo());
			if (!taxDetailsDtoList.isEmpty()) {
				TaxDetailsDTO taxDetailsDTO = taxDetailsDtoList.stream().findFirst().get();
				List<Map<String, TaxComponentDTO>> taxDetailsList = taxDetailsDTO.getTaxDetails();
				if (!taxDetailsList.isEmpty()) {

					taxDetailsList.forEach(taxDetails -> {
						Set<String> keys = taxDetails.keySet();

						for (String key : keys) {

							TaxComponentDTO taxComponent = taxDetails.get(key);
							if (taxComponent.getAmount() > ((taxDetailsDTO.getInvoiceValue() * percent) / 100)) {
								Double actualTax = (taxDetailsDTO.getInvoiceValue() * percent) / 100;
								Double diffTax = actualTax - ((taxDetailsDTO.getInvoiceValue() * 14) / 100);
								setBreakPayments(breakPaymentsNumList, actualTax, diffTax, key, breakPaymentsDTO);
							}
						}

					});

				}
				taxDetailsDtoList.clear();
			}
		}
	}

	public void setBreakPayments(List<BreakPaymentsSaveDTO> breakPaymentsNumList, Double actualTax, Double diffTax,
			String key, BreakPaymentsSaveDTO breakPaymentsDTO) {
		breakPaymentsNumList.stream().forEach(breakupPayments -> {
			if (!breakupPayments.getBreakPayments().isEmpty()) {
				breakupPayments.getBreakPayments().stream().forEach(breakPay -> {
					if (!breakPay.getBreakup().isEmpty()) {
						Double taxAmount = breakPay.getBreakup().get(key);
						if (taxAmount != null) {
							breakPay.getBreakup().put(key, actualTax);
							breakPaymentsDTO.setDiffTax(diffTax);
						}
					}
				});
			}
		});
	}

	@Override
	public List<MasterStoppageQuationsVO> getStoppageQuations(String role) {

		if (role.equalsIgnoreCase(RoleEnum.MVI.getName())) {

		}
		List<MasterStoppageQuationsDTO> listOfQuations = msterStoppageQuationsDAO.findByStatusIsTrue();
		if (listOfQuations.isEmpty()) {
			logger.error("no master quations for vehicle stoppage");
		}
		List<MasterStoppageQuationsVO> list = masterStoppageQuationsMapper.convertEntity(listOfQuations);
		return list;
	}

	@Override
	public List<MasterStoppageRevocationQuationsVO> getStoppageRevocationQuations(String role) {

		if (role.equalsIgnoreCase(RoleEnum.MVI.getName())) {

		}
		List<MasterStoppageRevocationQuationsDTO> listOfQuations = msterStoppageRevocationQuationsDAO
				.findByStatusIsTrue();
		if (listOfQuations.isEmpty()) {
			logger.error("no master quations for vehicle stoppage");
		}
		List<MasterStoppageRevocationQuationsVO> list = masterStoppageRevocationQuationsMapper
				.convertEntity(listOfQuations);
		return list;
	}

	@Override
	public TaxDataVO getCorrectionsDataEntryDetails(UserDTO userDetails, String prNo, CorrectionEnum serviceType,
			PermitType permitType) {
		roleValidation(userDetails, Boolean.TRUE);
		RegistrationDetailsVO registrationDetailsVO = findBasedOnPrNo(prNo.trim());
		if (registrationDetailsVO.getApplicationStatus().equals(StatusRegistration.RCCANCELLED.getDescription())) {
			throw new BadRequestException(
					StatusRegistration.RCCANCELLED.getDescription() + " status for this prNo: " + prNo);
		}
		if (registrationDetailsVO.getApplicantDetails() != null
				&& registrationDetailsVO.getApplicantDetails().getIsAadhaarValidated().equals(Boolean.FALSE)) {
			throw new BadRequestException("Aadhar seeding is mandatory to avail any service");
		}
		if (userDetails.getOffice() == null || userDetails.getOffice().getOfficeCode() == null
				&& registrationDetailsVO.getOfficeDetails().getOfficeCode() == null) {
			throw new BadRequestException("No Record Found prNo " + prNo);
		}
		validateOfficeCode(userDetails.getOffice().getOfficeCode(),
				registrationDetailsVO.getOfficeDetails().getOfficeCode());
		TaxDataVO vo = new TaxDataVO();

		if (registrationDetailsVO.getFinanceDetails() != null) {
			setFinanceDetailsinRegistrationDetails(registrationDetailsVO);
			registrationDetailsVO.setIsFinancier(Boolean.TRUE);
		}

		vo.setRegistrationDetails(registrationDetailsVO);
		switch (serviceType) {
		case TAXCORRECTION:
			getTaxData(vo);
			getPermitDetails(vo, true);
			break;
		case FCCORRECTION:
			vo.setFcDetailsVO(fcDetailsMapper.convertEntity(getFcDetails(prNo)));
			break;
		case RCCORRECTION:
			vo.setRegistrationDetails(registrationDetailsVO);
			break;
		case PERMITCORRECTION:
			getPermitData(vo, permitType);
			break;
		default:
			throw new BadRequestException("Correction Type Not Availble");
		}

		String makersDesc = StringUtils.isNotBlank(registrationDetailsVO.getVahanDetails().getMakersDesc())
				? registrationDetailsVO.getVahanDetails().getMakersDesc()
				: registrationDetailsVO.getVehicleDetails().getMakersDesc();
		if (StringUtils.isBlank(registrationDetailsVO.getVahanDetails().getMakersDesc())) {
			registrationDetailsVO.getVahanDetails().setMakersDesc(makersDesc.toUpperCase());
		}
		Optional<MakersDTO> makers = makersDAO.findByMakername(makersDesc);
		if (makers.isPresent()) {
			vo.setmId(makers.get().getMid());
		}
		return vo;
	}

	private void setFinanceDetailsinRegistrationDetails(RegistrationDetailsVO registrationDetailsVO) {
		FinanceDetailsVO vo = registrationDetailsVO.getFinanceDetails();
		if (StringUtils.isNotBlank(vo.getUserId())) {
			Optional<UserDTO> dto = userDAO.findByUserId(vo.getUserId());
			if (dto.isPresent()) {
				vo = financeDetailsMapper.converSpecificFields(dto.get(), vo);
			}
		}

	}

	private void getPermitData(TaxDataVO taxdataVo, PermitType permitType) {
		if (taxdataVo.getRegistrationDetails().getVehicleType() != null
				&& !taxdataVo.getRegistrationDetails().getVehicleType().equals(CovCategory.T.getCode())) {
			throw new BadRequestException("Non-Transport vehicle not allow for this Service");
		}
		List<PermitDetailsDTO> permitDtoList = permitDetailsDAO.findByPrNoAndPermitStatus(
				taxdataVo.getRegistrationDetails().getPrNo(), PermitsEnum.ACTIVE.getDescription());
		checkFcValidation(taxdataVo.getRegistrationDetails().getPrNo());
		checkPuccaPermitExistance(permitDtoList);
		if (CollectionUtils.isNotEmpty(permitDtoList)) {
			if (permitType.equals(PermitType.PRIMARY)) {
				List<PermitDetailsDTO> permitDto = permitDtoList.stream()
						.filter(e -> e.getPermitType().getTypeofPermit()
								.equals(PermitsEnum.PermitType.PRIMARY.getPermitTypeCode()))
						.collect(Collectors.toList());
				if (CollectionUtils.isNotEmpty(permitDto)) {
					taxdataVo.setPermitDetailsVO(
							permitDetailsMapper.convertEntity(permitDto.stream().findFirst().get()));
				}
			} else {
				List<PermitDetailsDTO> permitDto = permitDtoList.stream()
						.filter(e -> e.getPermitType().getTypeofPermit()
								.equalsIgnoreCase(PermitsEnum.PermitType.TEMPORARY.getPermitTypeCode()))
						.collect(Collectors.toList());
				if (permitDto.isEmpty()) {
					throw new BadRequestException("Vehicle doesn't have a valid TEMPORARY Permit prNo "
							+ taxdataVo.getRegistrationDetails().getPrNo());
				}
				if (CollectionUtils.isNotEmpty(permitDto)) {
					taxdataVo.setPermitDetailsList(permitDetailsMapper.convertEntity(permitDto));
				}
			}
		} else
			throw new BadRequestException(
					"Permit does not exist for prNo " + taxdataVo.getRegistrationDetails().getPrNo());

	}

	private void checkPuccaPermitExistance(List<PermitDetailsDTO> permitDtoList) {
		if (!permitDtoList.isEmpty()) {
			List<PermitDetailsDTO> permitDetailsDTO = permitDtoList.stream().filter(
					pType -> pType.getPermitClass().getDescription().equals(PermitType.PRIMARY.getDescription()))
					.collect(Collectors.toList());
			if (permitDetailsDTO.isEmpty()) {
				throw new BadRequestException("Vehicle doesn't have a valid PRIMARY Permit");
			}
		}
	}

	private FcDetailsDTO checkFcValidation(String prNo) {
		List<FcDetailsDTO> fcDetailsList = fcDetailsDAO.findFirst5ByPrNoOrderByCreatedDateDesc(prNo);
		if (fcDetailsList.isEmpty()) {
			throw new BadRequestException("FC Details not Found prNo " + prNo);
		}
		FcDetailsDTO fcDetailsDTO = fcDetailsList.stream().findFirst().get();
		if (!fcDetailsDTO.getFcValidUpto().isAfter(LocalDate.now())) {
			throw new BadRequestException("Vehicle doesn't have a valid FC prNo " + prNo);
		}
		return fcDetailsDTO;
	}

	// CreatedDate not available for migrated data so we are sorting based on
	private Optional<TaxDetailsDTO> getTaxDetails(String applicationNo) {
		List<TaxDetailsDTO> taxDetailsList = taxDetailsDAO
				.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(applicationNo, taxTypes());
		if (!taxDetailsList.isEmpty()) {
			TaxDetailsDTO dto = new TaxDetailsDTO();
			registrationService.updatePaidDateAsCreatedDate(taxDetailsList);
			taxDetailsList.sort((p1, p2) -> p2.getTaxPeriodEnd().compareTo(p1.getTaxPeriodEnd()));
			dto = taxDetailsList.stream().findFirst().get();
			taxDetailsList.clear();
			return Optional.of(dto);
		}
		return Optional.empty();
	}

	private List<String> taxTypes() {
		List<String> taxTypes = new ArrayList<>();
		taxTypes.add(ServiceCodeEnum.QLY_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.HALF_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.YEAR_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.LIFE_TAX.getCode());
		return taxTypes;
	}

	@Override
	public Boolean saveCorrectionsData(UserDTO userDetails, TaxDataVO taxDataVO, CorrectionEnum serviceType)
			throws CloneNotSupportedException {

		if (taxDataVO.getAadhaarDetailsRequestVO() == null) {
			throw new BadRequestException("Aadhaar Authentication is Mandatory");
		}
		if (userDetails.getAadharNo() == null) {
			throw new BadRequestException("RTO Aadhar Details Not available");
		}
		roleValidation(userDetails, Boolean.TRUE);

		Optional<AadharDetailsResponseVO> aadhaarResponseVO = getAadhaarResponse(
				taxDataVO.getAadhaarDetailsRequestVO());
		if (!userDetails.getAadharNo().equals(String.valueOf(aadhaarResponseVO.get().getUid()))) {
			throw new BadRequestException("Unauthorized User to do the corrections");
		}

		RegistrationDetailsDTO optionalDto = getPRRecord(taxDataVO.getPrNo());
		if (userDetails.getOffice() == null || userDetails.getOffice().getOfficeCode() == null
				&& optionalDto.getOfficeDetails().getOfficeCode() == null) {
			throw new BadRequestException("No record found based on prNo");
		}
		validateOfficeCode(userDetails.getOffice().getOfficeCode(), optionalDto.getOfficeDetails().getOfficeCode());

		switch (serviceType) {
		case TAXCORRECTION:
			if (taxDataVO.getTaxDetailsMasterVO() == null) {
				throw new BadRequestException("TaxDetails not available to save");
			}
			saveTaxDetails(optionalDto, taxDataVO.getTaxDetailsMasterVO(), serviceType, userDetails.getUserId());
			break;
		case FCCORRECTION:
			if (taxDataVO.getFcDetailsVO() == null) {
				throw new BadRequestException("FC Details not available to save");
			}
			saveFCDetails(optionalDto, taxDataVO.getFcDetailsVO(), serviceType, userDetails.getUserId());
			break;
		case RCCORRECTION:
			if (taxDataVO.getRegistrationCorrectionsVO() == null) {
				throw new BadRequestException("Registration Details not available to save");
			}
			regCorrectionsValidations(optionalDto, taxDataVO.getRegistrationCorrectionsVO());
			saveRegistrationDetails(optionalDto, taxDataVO.getRegistrationCorrectionsVO(), serviceType,
					userDetails.getUserId());
			break;
		case PERMITCORRECTION:
			if (taxDataVO.getPermitDetailsVO() == null) {
				throw new BadRequestException("PermitDetails not available to save");
			}
			savePermitDetails(optionalDto, taxDataVO.getPermitDetailsVO(), serviceType, userDetails.getUserId());
			break;
		default:
			throw new BadRequestException("Correction Type Not Availble");
		}
		logger.info("Corrected Details saved Successfully for the record [{}]", optionalDto.getPrNo());
		return Boolean.TRUE;
	}

	private void saveRegistrationDetails(RegistrationDetailsDTO oldRegDetailsDto,
			RegistrationCorrectionsVO registrationCorrectionDetailsVO, CorrectionEnum serviceType, String userId) {

		RTOCorrections rtoCorrections = new RTOCorrections();
		rtoCorrections.setPrNo(oldRegDetailsDto.getPrNo());
		rtoCorrections.setOldRegistrationDetails(oldRegDetailsDto);

		RegistrationDetailsDTO updateRegDetailsDTO = new RegistrationDetailsDTO();
		BeanUtils.copyProperties(oldRegDetailsDto, updateRegDetailsDTO);

		updateRegDetailsDTO = registrationCardDetailsMapper.convertVO(updateRegDetailsDTO,
				registrationCorrectionDetailsVO);

		MasterCovDTO covDto = masterCovDAO.findByCovdescription(updateRegDetailsDTO.getClassOfVehicle());

		updateRegDetailsDTO.setClassOfVehicleDesc(covDto.getCovdescription());
		updateRegDetailsDTO.setClassOfVehicle(covDto.getCovcode());
		if (updateRegDetailsDTO.getVehicleDetails() != null) {
			updateRegDetailsDTO.getVehicleDetails().setClassOfVehicle(covDto.getCovcode());
			updateRegDetailsDTO.getVehicleDetails().setClassOfVehicleDesc(covDto.getCovdescription());
		}
		updateRegDetailsDTO.setCardPrinted(Boolean.FALSE);

		rtoCorrections.setUpdatedRegistrationDetails(updateRegDetailsDTO);
		rtoCorrections.setCorrectedDate(LocalDateTime.now());
		rtoCorrections.setCorrectModule(serviceType);
		rtoCorrections.setCorrectedBy(userId);
		Optional<PermitDetailsDTO> permitDetails = null;

		if (oldRegDetailsDto.getVehicleType().equals(CovCategory.T.getCode())) {
			permitDetails = permitDetailsDAO.findByPrNoAndPermitStatusAndPermitTypeTypeofPermitOrderByCreatedDateDesc(
					oldRegDetailsDto.getPrNo(), PermitsEnum.ACTIVE.getDescription(),
					PermitType.PRIMARY.getPermitTypeCode());
			if (permitDetails.isPresent()) {
				permitDetails.get().setRdto(updateRegDetailsDTO);
			}
		}

		List<RegServiceDTO> savingRegistrationServicesList = new ArrayList<>();
		List<RegServiceDTO> listRegDetails = regServiceDAO.findByPrNo(oldRegDetailsDto.getPrNo());
		if (!CollectionUtils.isEmpty(listRegDetails)) {
			listRegDetails.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
			RegServiceDTO regSerDto = listRegDetails.stream().findFirst().get();
			if (regSerDto.getSource() != null && regSerDto.getApplicationStatus().equals(StatusRegistration.APPROVED)) {
				regSerDto.setRegistrationDetails(updateRegDetailsDTO);
				savingRegistrationServicesList.add(regSerDto);
			}
		}

		if (registrationCorrectionDetailsVO.getFinanceDetailsVO() != null) {
			saveFinanceDetailsinMasterUsers(registrationCorrectionDetailsVO.getFinanceDetailsVO(), updateRegDetailsDTO);
		}

		correctionsDAO.save(rtoCorrections);
		if (permitDetails != null && permitDetails.isPresent()) {
			permitDetailsDAO.save(permitDetails.get());
		}

		regServiceDAO.save(savingRegistrationServicesList);
		registrationDetailDAO.save(updateRegDetailsDTO);

	}

	private void saveFinanceDetailsinMasterUsers(FinanceDetailsVO financeDetailsVO,
			RegistrationDetailsDTO updateRegDetailsDTO) {

		Optional<UserDTO> dto = userDAO.findByUserId(updateRegDetailsDTO.getFinanceDetails().getUserId());
		if (dto.isPresent()) {
			if (StringUtils.isBlank(dto.get().getStreetName())
					&& StringUtils.isNotBlank(financeDetailsVO.getStreetName())) {
				dto.get().setStreetName(financeDetailsVO.getStreetName());
			}
			if (StringUtils.isBlank(dto.get().getCity()) && StringUtils.isNotBlank(financeDetailsVO.getCity())) {
				dto.get().setCity(financeDetailsVO.getCity());
			}
			if (dto.get().getMandal() == null && financeDetailsVO.getMandal() != null) {
				dto.get().setMandal(mandalMapper.convertVO(financeDetailsVO.getMandal()));
			}
			if (dto.get().getDistrict() == null && financeDetailsVO.getDistrict() != null) {
				dto.get().setDistrict(districtMapper.convertVO(financeDetailsVO.getDistrict()));
			}
			if (dto.get().getState() == null && financeDetailsVO.getState() != null) {
				dto.get().setState(stateMapper.convertVO(financeDetailsVO.getState()));
			}
			userDAO.save(dto.get());
		}

	}

	private void savePermitDetails(RegistrationDetailsDTO regDetailsDto, PermitDetailsVO permitDetails,
			CorrectionEnum serviceType, String userId) {
		PermitDetailsDTO oldPermitDetailsDTO = getPermitDetailsData(regDetailsDto, permitDetails);

		/*
		 * if (oldPermitDetailsDTO.getPermitValidityDetails().getPermitValidFrom().
		 * isBefore (LocalDate.now())) { throw new
		 * BadRequestException("Permit already started from [" +
		 * oldPermitDetailsDTO.getPermitValidityDetails().getPermitValidFrom() +
		 * "], can't rectify"); }
		 */
		checkFcValidation(regDetailsDto.getPrNo());
		Long years = ChronoUnit.YEARS.between(permitDetails.getPermitValidityDetailsVO().getPermitValidFrom(),
				permitDetails.getPermitValidityDetailsVO().getPermitValidTo().plusDays(1));

		if (years > 5) {
			throw new BadRequestException(
					"Permit validity less than or equal to 5 years : [" + regDetailsDto.getPrNo() + "]");
		}

		RTOCorrections rtoCorrections = new RTOCorrections();
		rtoCorrections.setPrNo(regDetailsDto.getPrNo());
		rtoCorrections.setOldPermitDetails(oldPermitDetailsDTO);
		PermitDetailsDTO updatedPermitDetails = new PermitDetailsDTO();
		BeanUtils.copyProperties(oldPermitDetailsDTO, updatedPermitDetails);
		setUpdatedPermits(permitDetails, updatedPermitDetails);
		rtoCorrections.setUpdatedPermitDetails(updatedPermitDetails);
		rtoCorrections.setCorrectedDate(LocalDateTime.now());
		rtoCorrections.setCorrectModule(serviceType);
		rtoCorrections.setCorrectedBy(userId);
		correctionsDAO.save(rtoCorrections);
		permitDetailsDAO.save(updatedPermitDetails);
	}

	private void setUpdatedPermits(PermitDetailsVO permitDetails, PermitDetailsDTO updatedPermitDetails) {
		PermitDetailsDTO permitDto = permitDetailsMapper.convertVO(permitDetails);

		// updatedPermitDetails.getPermitClass().setDescription(permitDto.getPermitClass().getDescription());
		if (updatedPermitDetails.getPermitClass().getDescription().equals(PermitType.TEMPORARY.getDescription())) {
			updatedPermitDetails.getRouteDetails().setForwardRoute(permitDto.getRouteDetails().getForwardRoute());
			updatedPermitDetails.getRouteDetails().setReturnRoute(permitDto.getRouteDetails().getReturnRoute());
		}

		// update permitvalidityDetails
		if (permitDto.getPermitValidityDetails().getPermitValidFrom() != null) {
			updatedPermitDetails.getPermitValidityDetails()
					.setPermitValidFrom(permitDto.getPermitValidityDetails().getPermitValidFrom());
		}
		if (permitDto.getPermitValidityDetails().getPermitValidTo() != null) {
			updatedPermitDetails.getPermitValidityDetails()
					.setPermitValidTo(permitDto.getPermitValidityDetails().getPermitValidTo());
		}
		if (permitDto.getPermitValidityDetails().getPermitAuthorizationValidFrom() != null) {
			updatedPermitDetails.getPermitValidityDetails().setPermitAuthorizationValidFrom(
					permitDto.getPermitValidityDetails().getPermitAuthorizationValidFrom());
		}
		if (permitDto.getPermitValidityDetails().getPermitAuthorizationValidTo() != null) {
			updatedPermitDetails.getPermitValidityDetails().setPermitAuthorizationValidTo(
					permitDto.getPermitValidityDetails().getPermitAuthorizationValidTo());
		}

		if (permitDto.getPermitValidityDetails().getNoOfMonths() != null) {
			updatedPermitDetails.getPermitValidityDetails()
					.setNoOfMonths(permitDto.getPermitValidityDetails().getNoOfMonths());
		}

		if (permitDto.getPermitValidityDetails().getExtentionDays() != null) {
			updatedPermitDetails.getPermitValidityDetails()
					.setExtentionDays(permitDto.getPermitValidityDetails().getExtentionDays());
		}

		updatedPermitDetails.setlUpdate(LocalDateTime.now());

		// update permitGoodsDetails
		if (permitDetails.getGoodDetails() != null) {
			updatedPermitDetails.setGoodsDetails(permitGoodsDetailsMapper.convertVO(permitDetails.getGoodDetails()));

		}
		// update routeDetailsMapper
		if (permitDetails.getRouteDetailsVO() != null) {
			updatedPermitDetails.setRouteDetails(routeDetailsMapper.convertVO(permitDetails.getRouteDetailsVO()));

		}

	}

	private PermitDetailsDTO getPermitDetailsData(RegistrationDetailsDTO regDetailsDto, PermitDetailsVO permitDetails) {
		Optional<PermitDetailsDTO> permitDto = null;
		if (regDetailsDto.getVehicleType() != null && regDetailsDto.getVehicleType().equals(CovCategory.T.getCode())) {
			if (null != permitDetails.getPermitClass() && permitDetails.getPermitClass().getDescription()
					.equals(PermitsEnum.PermitType.TEMPORARY.getDescription())) {
				permitDto = permitDetailsDAO.findByPermitNoAndPermitStatus(permitDetails.getPermitNo(),
						PermitsEnum.ACTIVE.getDescription());
			} else {
				permitDto = permitDetailsDAO.findByPrNoAndPermitTypeTypeofPermitAndPermitStatus(regDetailsDto.getPrNo(),
						PermitsEnum.PermitType.PRIMARY.getPermitTypeCode(), PermitsEnum.ACTIVE.getDescription());
			}
		}
		return permitDto.get();
	}

	private void roleValidation(UserDTO userDetails, Boolean roleValidation) {
		if (userDetails.getPrimaryRole() != null
				&& !userDetails.getPrimaryRole().getName().equals(RoleEnum.RTO.getName())) {
			roleValidation = Boolean.FALSE;
		}
		if (!roleValidation && userDetails.getAdditionalRoles() != null && !userDetails.getAdditionalRoles().stream()
				.anyMatch(id -> id.getName().equals(RoleEnum.RTO.getName()))) {
			throw new BadRequestException("This Service only avialble for RTO's");
		}
	}

	public Optional<AadharDetailsResponseVO> getAadhaarResponse(AadhaarDetailsRequestVO aadhaarDetailsRequestVO) {
		Optional<AadharDetailsResponseVO> applicantDetailsOptional = restGateWayService
				.validateAadhaar(aadhaarDetailsRequestVO, null);
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

	private void saveTaxDetails(RegistrationDetailsDTO registrationDetailsDTO, TaxDetailsMasterVO taxDetailsMasterVO,
			CorrectionEnum serviceType, String userId) {
		RTOCorrections rtoCorrections = new RTOCorrections();
		Optional<TaxDetailsDTO> oldTaxDeatils = getTaxDetails(registrationDetailsDTO.getApplicationNo());
		if (oldTaxDeatils.isPresent()) {
			TaxDetailsDTO updateTaxDetails = new TaxDetailsDTO();
			BeanUtils.copyProperties(oldTaxDeatils.get(), updateTaxDetails);
			rtoCorrections.setOldTaxDetails(oldTaxDeatils.get());
			if (taxDetailsMasterVO.getRemarks() != null) {
				updateTaxDetails.setRemarks(taxDetailsMasterVO.getRemarks());
				if (taxDetailsMasterVO.getPaymentPeriod() != null) {
					for (Map<String, TaxComponentDTO> tax : updateTaxDetails.getTaxDetails()) {
						if (tax.containsKey(ServiceCodeEnum.QLY_TAX.getCode())
								|| tax.containsKey(ServiceCodeEnum.HALF_TAX.getCode())
								|| tax.containsKey(ServiceCodeEnum.YEAR_TAX.getCode())
								|| tax.containsKey(ServiceCodeEnum.LIFE_TAX.getCode())) {
							for (TaxComponentDTO taxComp : tax.values()) {
								if (taxComp.getTaxName().equals(ServiceCodeEnum.QLY_TAX.getCode())
										|| taxComp.getTaxName().equals(ServiceCodeEnum.HALF_TAX.getCode())
										|| taxComp.getTaxName().equals(ServiceCodeEnum.YEAR_TAX.getCode())
										|| taxComp.getTaxName().equals(ServiceCodeEnum.LIFE_TAX.getCode())) {
									taxComp.setTaxName(taxDetailsMasterVO.getPaymentPeriod());
									if (taxDetailsMasterVO.getPenalityAmount() != null) {
										taxComp.setPenalty(taxDetailsMasterVO.getPenalityAmount());
									}
									taxComp.setAmount(taxDetailsMasterVO.getTaxAmount().doubleValue());
									taxComp.setValidityFrom(taxDetailsMasterVO.getTaxPeriodFrom());
									taxComp.setValidityTo(taxDetailsMasterVO.getTaxPeriodEnd());
									tax.replace(taxDetailsMasterVO.getPaymentPeriod(), taxComp);
								}
							}
						}
						updateTaxDetails.setTaxAmount(taxDetailsMasterVO.getTaxAmount());
						updateTaxDetails.setTaxPeriodEnd(taxDetailsMasterVO.getTaxPeriodEnd());
						updateTaxDetails.setTaxPeriodFrom(taxDetailsMasterVO.getTaxPeriodFrom());
					}
				}
				rtoCorrections.setPrNo(registrationDetailsDTO.getPrNo());
				rtoCorrections.setCorrectedBy(userId);
				rtoCorrections.setUpdatedTaxDetails(updateTaxDetails);
				rtoCorrections.setCorrectedDate(LocalDateTime.now());
				rtoCorrections.setCorrectModule(serviceType);
				correctionsDAO.save(rtoCorrections);
				taxDetailsDAO.save(updateTaxDetails);
			}
		} /*
			 * TaxDetailsDTO dto = new TaxDetailsDTO();
			 * dto.setApplicationNo(registrationDetailsDTO.getApplicationNo());
			 * dto.setCovCategory(registrationDetailsDTO.getOwnerType());
			 * dto.setModule(ModuleEnum.REG.getCode());
			 * dto.setPaymentPeriod(taxDetailsMasterVO.getPaymentPeriod());
			 * dto.setTaxAmount(taxDetailsMasterVO.getTaxAmount());
			 * dto.setTrNo(registrationDetailsDTO.getTrNo());
			 * dto.setPrNo(registrationDetailsDTO.getPrNo());
			 * dto.setTaxPeriodEnd(taxDetailsMasterVO.getTaxPeriodEnd());
			 * dto.setTaxPeriodFrom(taxDetailsMasterVO.getTaxPeriodFrom());
			 * dto.setPayTaxType(taxDetailsMasterVO.getPayTaxType());
			 * dto.setCreatedDate(LocalDateTime.now());
			 * dto.setTaxPaidDate(taxDetailsMasterVO.getTaxPeriodFrom());
			 * dto.setClassOfVehicle(registrationDetailsDTO.getClassOfVehicle()) ;
			 * List<Map<String, TaxComponentDTO>> list = new ArrayList<>(); Map<String,
			 * TaxComponentDTO> taxMap = new HashMap<>(); TaxComponentDTO tax = new
			 * TaxComponentDTO(); tax.setTaxName(taxDetailsMasterVO.getPaymentPeriod());
			 * tax.setAmount(Double.valueOf(taxDetailsMasterVO.getTaxAmount(). toString()));
			 * LocalDateTime localDateTime1 =
			 * taxDetailsMasterVO.getTaxPeriodFrom().atStartOfDay();
			 * tax.setPaidDate(localDateTime1);
			 * tax.setValidityFrom(taxDetailsMasterVO.getTaxPeriodFrom());
			 * tax.setValidityTo(taxDetailsMasterVO.getTaxPeriodEnd()); if
			 * (taxDetailsMasterVO.getPenalityAmount() != null &&
			 * taxDetailsMasterVO.getPenalityAmount() != 0) {
			 * tax.setPenalty(taxDetailsMasterVO.getPenalityAmount()); }
			 * taxMap.put(taxDetailsMasterVO.getPaymentPeriod(), tax); list.add(taxMap);
			 * dto.setTaxDetails(list);
			 * dto.setInvoiceValue(registrationDetailsDTO.getInvoiceDetails().
			 * getInvoiceValue()); dto.setTaxStatus(TaxStatusEnum.ACTIVE.getCode()); if
			 * (taxDetailsMasterVO.getRemarks() != null) {
			 * dto.setRemarks(taxDetailsMasterVO.getRemarks()); }
			 * dto.setOfficeCode(registrationDetailsDTO.getOfficeDetails().
			 * getOfficeCode()); dto.setStateCode("AP");
			 */
		/* return dto; */
	}

	public void validateOfficeCode(String userOfficeCode, String citizenOfficeCode) {
		Optional<OfficeDTO> officeDTO = officeDAO.findByOfficeCode(citizenOfficeCode);
		logger.debug("User Office Code:[{}]", userOfficeCode);
		logger.debug("Citizen Office Code:[{}]", citizenOfficeCode);
		if (!officeDTO.isPresent()) {

			logger.error("No Office exists with this officeCode:" + citizenOfficeCode);

			throw new BadRequestException("No Office exists with this officeCode: " + citizenOfficeCode);
		}
		List<String> officeCodes = Arrays.asList(officeDTO.get().getReportingoffice(), officeDTO.get().getOfficeCode());
		if (!officeCodes.contains(citizenOfficeCode)) {
			logger.error("RC not Related to this office code[{}] ", userOfficeCode);
			throw new BadRequestException("Record not comes under this Reporting Office");
		}

	}

	private void getPermitDetails(TaxDataVO taxdataVo, boolean value) {

		if (taxdataVo.getRegistrationDetails().getVehicleType() != null
				&& taxdataVo.getRegistrationDetails().getVehicleType().equals("T")) {
			List<PermitDetailsDTO> permitDtoList = permitDetailsDAO.findByPrNoAndPermitStatus(
					taxdataVo.getRegistrationDetails().getPrNo(), PermitsEnum.ACTIVE.getDescription());
			if (CollectionUtils.isNotEmpty(permitDtoList)) {
				if (value) {
					List<PermitDetailsDTO> permitDto = permitDtoList.stream()
							.filter(e -> e.getPermitStatus().equals(PermitsEnum.PermitType.PRIMARY.getDescription()))
							.collect(Collectors.toList());
					if (CollectionUtils.isNotEmpty(permitDto)) {
						taxdataVo.setPermitDetailsVO(
								permitDetailsMapper.convertEntity(permitDto.stream().findFirst().get()));
					}
				} else {
					taxdataVo.setPermitDetailsList(permitDetailsMapper.convertListEntity(permitDtoList));
				}
			}
		}
	}

	private void getTaxData(TaxDataVO taxdataVo) {
		Optional<TaxDetailsDTO> taxDeatils = getTaxDetails(taxdataVo.getRegistrationDetails().getApplicationNo());
		if (taxDeatils.isPresent()) {
			taxdataVo.setTaxDetailsMasterVO(taxDetailsMasterMapper.convertEntity(taxDeatils.get()));
		}
	}

	private RegistrationDetailsDTO getPRRecord(String prNo) {
		Optional<RegistrationDetailsDTO> optionalDto = registrationDetailDAO.findByPrNo(prNo);
		if (!optionalDto.isPresent()) {
			throw new BadRequestException("No record found based on prNo");
		}
		return optionalDto.get();
	}

	private void saveFCDetails(RegistrationDetailsDTO registrationDetailsDTO, FcDetailsVO fcVO,
			CorrectionEnum serviceType, String userId) {
		FcDetailsDTO oldFcDetails = getFcDetails(registrationDetailsDTO.getPrNo());
		FcDetailsDTO updateFcDetails = new FcDetailsDTO();
		BeanUtils.copyProperties(oldFcDetails, updateFcDetails);
		RTOCorrections rtoCorrections = new RTOCorrections();
		rtoCorrections.setPrNo(registrationDetailsDTO.getPrNo());
		rtoCorrections.setOldFcDetails(oldFcDetails);
		rtoCorrections.setCorrectedDate(LocalDateTime.now());
		rtoCorrections.setCorrectedBy(userId);
		rtoCorrections.setCorrectModule(serviceType);
		updateFcDetails.setFcIssuedDate(fcVO.getFcIssuedDate());
		updateFcDetails.setFcValidUpto(fcVO.getFcValidUpto());
		rtoCorrections.setUpdatedFcDetails(updateFcDetails);
		correctionsDAO.save(rtoCorrections);
		fcDetailsDAO.save(updateFcDetails);
	}

	private FcDetailsDTO getFcDetails(String prNo) {
		List<FcDetailsDTO> fcDetailsList = fcDetailsDAO.findFirst5ByPrNoOrderByCreatedDateDesc(prNo.trim());
		if (fcDetailsList.isEmpty()) {
			throw new BadRequestException("FC Details are not available");
		}
		fcDetailsList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		return fcDetailsList.stream().findFirst().get();
	}

	@Override
	public TaxDataVO getDiffTaxDetails(UserDTO userDetails, String prNo) {
		// roleValidation(userDetails,Boolean.TRUE);
		RegistrationDetailsVO registrationDetailsVO = findBasedOnPrNo(prNo);
		if (userDetails.getOffice() == null || userDetails.getOffice().getOfficeCode() == null
				&& registrationDetailsVO.getOfficeDetails().getOfficeCode() == null) {
			throw new BadRequestException("No record found based on prNo");
		}
		validateOfficeCode(userDetails.getOffice().getOfficeCode(),
				registrationDetailsVO.getOfficeDetails().getOfficeCode());
		TaxDataVO vo = new TaxDataVO();
		vo.setRegistrationDetails(registrationDetailsVO);
		getTaxData(vo);
		getPermitDetails(vo, true);
		return vo;
	}

	@Override
	public Boolean saveDiffTax(UserDTO userDetails, TaxDataVO taxDataVO) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Integer getFreshRcforFinanceCount(String officeCode, String id, String role) {
		List<RegServiceDTO> actionDtoList = null;
		if (role.equalsIgnoreCase(RoleEnum.MVI.getName())) {
			actionDtoList = regServiceDAO.findByOfficeDetailsOfficeCodeAndServiceIdsAndApplicationStatus(officeCode,
					ServiceEnum.RCFORFINANCE.getId(), StatusRegistration.INITIATED.toString());
			if (actionDtoList != null) {
				List<RegServiceDTO> regProcessCountList = actionDtoList.stream()
						.filter(val -> val.getCurrentIndex() == 2).collect(Collectors.toList());
				return regProcessCountList.size();
			}
		}
		return 0;
	}

	@Override
	public List<RegServiceVO> getFreshRcforFinanceList(JwtUser jwtUser) {
		List<RegServiceDTO> actionDtoList = null;
		List<RegServiceVO> voList = null;
		actionDtoList = regServiceDAO.findByOfficeDetailsOfficeCodeAndServiceIdsAndApplicationStatus(
				jwtUser.getOfficeCode(), ServiceEnum.RCFORFINANCE.getId(), StatusRegistration.INITIATED.toString());
		if (actionDtoList != null) {
			List<RegServiceDTO> regProcessCountList = actionDtoList.stream().filter(val -> val.getCurrentIndex() == 2)
					.collect(Collectors.toList());
			voList = regServiceMapper.convertEntity(regProcessCountList);
		}
		return voList;
	}

	@Override
	public TaxDataVO getTaxDetails(UserDTO userDetails, String prNo) {
		// roleValidation(userDetails,Boolean.TRUE);
		RegistrationDetailsVO registrationDetailsVO = findBasedOnPrNo(prNo);
		if (userDetails.getOffice() == null || userDetails.getOffice().getOfficeCode() == null
				&& registrationDetailsVO.getOfficeDetails().getOfficeCode() == null) {
			throw new BadRequestException("No record found based on prNo");
		}
		validateOfficeCode(userDetails.getOffice().getOfficeCode(),
				registrationDetailsVO.getOfficeDetails().getOfficeCode());
		TaxDataVO vo = new TaxDataVO();
		vo.setRegistrationDetails(registrationDetailsVO);
		List<TaxDetailsDTO> taxDetailsList = taxDetailsDAO
				.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(
						registrationDetailsVO.getApplicationNo(), taxTypes());
		if (!taxDetailsList.isEmpty()) {
			// TaxDetailsDTO dto = new TaxDetailsDTO();
			List<TaxDetailsDTO> listOfTaxDetails = new ArrayList<>();
			registrationService.updatePaidDateAsCreatedDate(taxDetailsList);
			taxDetailsList.sort((p1, p2) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));

			for (String type : taxTypes()) {
				for (TaxDetailsDTO taxDetails : taxDetailsList) {
					if (taxDetails.getTaxDetails() == null) {
						logger.error("TaxDetails map not found: [{}]", registrationDetailsVO.getApplicationNo());
						throw new BadRequestException(
								"TaxDetails map not found:" + registrationDetailsVO.getApplicationNo());
					}
					if (taxDetails.getTaxDetails().stream().anyMatch(key -> key.keySet().contains(type))) {
						listOfTaxDetails.add(taxDetails);
						// break;
					}
				}
			}
			listOfTaxDetails.sort((p1, p2) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
			// dto = listOfTaxDetails.stream().findFirst().get();
			vo.setTaxDetailsMasterVO(taxDetailsMasterMapper.converttaxDetails(listOfTaxDetails, taxTypes()));
			taxDetailsList.clear();
		}
		getPermitDetails(vo, true);
		return vo;
	}

	@Override
	public Optional<List<MasterCovVO>> getClassOfVehicle(String cov) {

		MasterCovDTO masterCov = masterCovDAO.findByCovcode(cov);
		List<MasterCovDTO> covDtoList = masterCovDAO.findByCategoryAndIsChassisTrue(masterCov.getCategory());
		List<MasterCovVO> covVOList = covDtoList.stream().map(val -> new MasterCovMapper().convertEntity(val))
				.collect(Collectors.toList());
		if (cov.equals(ClassOfVehicleEnum.CHSN.getCovCode())) {
			covVOList = covVOList.stream().filter(val -> !val.getCovcode().equals(ClassOfVehicleEnum.OBPN.getCovCode()))
					.collect(Collectors.toList());
		}
		return Optional.of(covVOList);
	}

	@Override
	public Optional<List<MasterCovVO>> getClassOfVehicleforMVI(String cov) {
		List<MasterCovDTO> covDtoList = masterCovDAO.findAll();
		List<MasterCovVO> covVOList = covDtoList.stream().map(val -> new MasterCovMapper().convertEntity(val))
				.collect(Collectors.toList());
		/*
		 * if (cov.equals(ClassOfVehicleEnum.CHSN.getCovCode())) { covVOList =
		 * covVOList.stream().filter(val ->
		 * !val.getCovcode().equals(ClassOfVehicleEnum.OBPN.getCovCode()))
		 * .collect(Collectors.toList()); }
		 */
		return Optional.of(covVOList);
	}

	@Override
	public List<BodyTypeVO> getBodyType() {

		List<BodyTypeDTO> bodyTypeDTOList = bodyTypeDAO.findAll();
		List<BodyTypeVO> bodyTypeVOList = bodyTypeDTOList.stream().map(val -> new BodyTypeMapper().convertEntity(val))
				.collect(Collectors.toList());

		return bodyTypeVOList;
	}

	@Override
	public void unlockServiceRecord(UnlockServiceRecordVO unlockServiceRecordVO) {

		// List<RegServiceDTO> regServiceDTOList = regServiceDAO
		// .findByLockedDetailsLockedByAndLockedDetailsLockedByRole(unlockServiceRecordVO.getForUserId(),
		// unlockServiceRecordVO.getForUserRole());
		RegServiceDTO regServiceDTO = regServiceDAO.findOne(unlockServiceRecordVO.getApplicationNo());
		if (null == regServiceDTO) {
			throw new BadRequestException(
					"No locked record found for the user: " + unlockServiceRecordVO.getForUserId());
		}
		if (null == regServiceDTO.getOfficeCode()) {
			throw new BadRequestException(
					"Office code not found for the application: " + unlockServiceRecordVO.getApplicationNo());
		}
		if (!regServiceDTO.getOfficeCode().equals(unlockServiceRecordVO.getOfficeCode())) {
			throw new BadRequestException("The application is not belongs to your office ");
		}
		if (-1 == regServiceDTO.getCurrentIndex().intValue()) {
			throw new BadRequestException("The application is already unlocked");
		}
		RegServiceUnlockDTO regServiceUnlockDTO = new RegServiceUnlockDTO();
		regServiceUnlockDTO.setUnlockedBy(unlockServiceRecordVO.getActionUserId());
		regServiceUnlockDTO.setIpAddress(unlockServiceRecordVO.getRequestIP());
		regServiceUnlockDTO.setCreatedDate(LocalDateTime.now());
		regServiceUnlockDTO.setRegServe(regServiceDTO);
		regServiceUnlockDAO.save(regServiceUnlockDTO);

		regServiceDTO.setLockedDetails(null);
		regServiceDTO.setCurrentIndex(-1);
		regServiceDTO.setCurrentRoles(null);
		regServiceDAO.save(regServiceDTO);
	}

	@Override
	public void checkValidtionForRtoAadharValiadtion(UserDTO userDetails,
			AadhaarDetailsRequestVO aadhaarDetailsRequestVO) {
		if (aadhaarDetailsRequestVO == null) {
			throw new BadRequestException("Aadhaar Authentication is Mandatory");
		}
		if (userDetails.getAadharNo() == null) {
			throw new BadRequestException("RTO Aadhar Details Not available");
		}
		roleValidation(userDetails, Boolean.TRUE);
		Optional<AadharDetailsResponseVO> aadhaarResponseVO = getAadhaarResponse(aadhaarDetailsRequestVO);
		if (!userDetails.getAadharNo().equals(String.valueOf(aadhaarResponseVO.get().getUid()))) {
			throw new BadRequestException("Unauthorized User");
		}
	}

	private void regCorrectionsValidations(RegistrationDetailsDTO oldRegDetailsDto, RegistrationCorrectionsVO inputVo) {

		// As per Murthy Input comented Invoice Validation
		/*
		 * if (inputVo.getInvoiceDetailsVO() != null &&
		 * !(oldRegDetailsDto.getInvoiceDetails() != null &&
		 * oldRegDetailsDto.getInvoiceDetails().getInvoiceNo() != null &&
		 * oldRegDetailsDto.getInvoiceDetails()
		 * .getInvoiceNo().equals(inputVo.getInvoiceDetailsVO().getInvoiceNo())) ) {
		 * invoiceValidation(inputVo); }
		 */
		if (!(oldRegDetailsDto.getVahanDetails() != null
				&& oldRegDetailsDto.getVahanDetails().getChassisNumber()
						.equals(inputVo.getVahanDetailsVO().getChassisNumber().toUpperCase())
				&& oldRegDetailsDto.getVahanDetails().getEngineNumber()
						.equals(inputVo.getVahanDetailsVO().getEngineNumber().toUpperCase()))) {
			chassisNoValidation(inputVo);
		}

		if (StringUtils.isNotBlank(inputVo.getVehicleType())
				&& StringUtils.isNotBlank(inputVo.getClassOfVehicleDesc())) {
			MasterCovDTO covDto = masterCovDAO.findByCovdescription(inputVo.getClassOfVehicleDesc());
			if (covDto == null) {
				throw new BadRequestException("Vehicle category is not matching with the Class of vehicle");
			}
		}
	}

	/**
	 * This method is useful for the invoice details validation in Registration
	 * corrections Now this method is not using as per the murthy inputs invoice
	 * validation is not required while doing the corrections
	 * 
	 * @param inputVo
	 */
	@SuppressWarnings("unused")
	private void invoiceValidation(RegistrationCorrectionsVO inputVo) {
		if (inputVo.getInvoiceDetailsVO() != null) {
			List<RegistrationDetailsDTO> invoiceList = registrationDetailDAO
					.findByInvoiceDetailsInvoiceNo(inputVo.getInvoiceDetailsVO().getInvoiceNo());
			if (CollectionUtils.isNotEmpty(invoiceList)) {
				for (RegistrationDetailsDTO regDto : invoiceList) {
					if (!regDto.getApplicationNo().equals(inputVo.getApplicationNo())) {
						throw new BadRequestException(
								regDto.getApplicationNo() + " : Application Having same Invoice Number");
					}
				}
			}
			Optional<StagingRegistrationDetailsDTO> invoice = stagingRegistrationDetailsDAO
					.findByInvoiceDetailsInvoiceNo(inputVo.getInvoiceDetailsVO().getInvoiceNo());
			if (invoice.isPresent()) {
				throw new BadRequestException(
						invoice.get().getApplicationNo() + " : Application Having same Invoice Number");
			}

			Integer serviceId = ServiceEnum.DATAENTRY.getId();
			List<RegServiceDTO> regSerList = regServiceDAO
					.findByServiceIdsAndRegistrationDetailsInvoiceDetailsInvoiceNo(serviceId,
							inputVo.getInvoiceDetailsVO().getInvoiceNo());

			if (CollectionUtils.isNotEmpty(regSerList)) {
				regSerList.removeIf(reg -> (StatusRegistration.APPROVED.equals(reg.getApplicationStatus())
						&& (reg.getOtherStateNOCStatus() == null
								|| StatusRegistration.APPROVED.equals(reg.getOtherStateNOCStatus()))));
				if (CollectionUtils.isNotEmpty(regSerList)) {
					throw new BadRequestException(
							regSerList.get(0).getApplicationNo() + " : Application Having same Invoice Number");
				}

			}
		}
	}

	/**
	 * Validating Chassis number and Engine number for corrections in RTO level
	 * In @vahanValidationInStagingForOtherSate is used to validate chassis and
	 * engine number in fresh Registration Then we are validating the chassis and
	 * engine number in registration details Then we are validating in the
	 * registration services in case it's other state vehicle with some identical
	 * application status
	 * 
	 * @param inputVo
	 */
	private void chassisNoValidation(RegistrationCorrectionsVO inputVo) {
		if (StringUtils.isNotEmpty(inputVo.getVahanDetailsVO().getChassisNumber())
				&& StringUtils.isNotEmpty(inputVo.getVahanDetailsVO().getEngineNumber())) {
			dealerService.vahanValidationInStagingForOtherSate(
					inputVo.getVahanDetailsVO().getChassisNumber().toUpperCase(),
					inputVo.getVahanDetailsVO().getEngineNumber().toUpperCase());
			List<RegistrationDetailsDTO> regDetails = registrationDetailDAO
					.findByVahanDetailsChassisNumberAndVahanDetailsEngineNumber(
							inputVo.getVahanDetailsVO().getChassisNumber().toUpperCase(),
							inputVo.getVahanDetailsVO().getEngineNumber().toUpperCase());
			if (CollectionUtils.isNotEmpty(regDetails)) {
				regDetails.stream().forEach(regDto -> {
					if (regDto.getOfficeDetails() != null && regDto.getOfficeDetails().getOfficeCode() != null) {
						String officeCode = regDto.getOfficeDetails().getOfficeCode();
						Optional<OfficeDTO> office = officeDAO.findByOfficeCodeAndIsActiveTrue(officeCode);
						if (!regDto.getPrNo().equals(inputVo.getPrNo()) && !office.isPresent()) {
							throw new BadRequestException(
									regDto.getApplicationNo() + " : Application Having same Chasiss/Engine Number");
						}
					}
				});
			}

			List<RegServiceDTO> regSerList = regServiceDAO
					.findByRegistrationDetailsVahanDetailsChassisNumberAndRegistrationDetailsVahanDetailsEngineNumberAndApplicationStatusInAndServiceIdsIn(
							inputVo.getVahanDetailsVO().getChassisNumber().toUpperCase(),
							inputVo.getVahanDetailsVO().getEngineNumber().toUpperCase(),
							Arrays.asList(StatusRegistration.OTHERSTATEPAYMENTPENDING, StatusRegistration.PAYMENTDONE,
									StatusRegistration.CITIZENSUBMITTED, StatusRegistration.APPROVED),
							Arrays.asList(ServiceEnum.DATAENTRY.getId()));

			if (CollectionUtils.isNotEmpty(regSerList)) {
				throw new BadRequestException("Vahan Details Already exist with Application No:" + "["
						+ regSerList.get(0).getApplicationNo() + "]");
			}
		}
	}

	@Override
	public TaxDataVO getCorrectionDetailsForAllServices(UserDTO userDetails, String prNo) {
		roleValidation(userDetails, Boolean.TRUE);
		RegistrationDetailsVO registrationDetailsVO = findBasedOnPrNo(prNo.trim());
		if (userDetails.getOffice() == null || userDetails.getOffice().getOfficeCode() == null
				&& registrationDetailsVO.getOfficeDetails().getOfficeCode() == null) {
			throw new BadRequestException("No Record Found prNo " + prNo);
		}
		validateOfficeCode(userDetails.getOffice().getOfficeCode(),
				registrationDetailsVO.getOfficeDetails().getOfficeCode());
		TaxDataVO vo = new TaxDataVO();
		vo.setRegistrationDetails(registrationDetailsVO);
		if (registrationDetailsVO.getVehicleType().equalsIgnoreCase("T")) {
			FcDetailsVO fcVo = fcDetailsMapper.convertEntity(getFcDetails(prNo));
			if (fcVo != null) {
				vo.setFcDetailsVO(fcVo);
			}
		}
		getTaxData(vo);
		getPermitDetails(vo, true);
		if (StringUtils.isNotBlank(registrationDetailsVO.getVahanDetails().getMakersDesc())) {
			/*
			 * MakersDTO makers =
			 * makersDAO.findByMakername(registrationDetailsVO.getVahanDetails() .
			 * getMakersDesc()); vo.setmId(makers.getMid());
			 */}
		return vo;
	}

	@Override
	public Boolean saveCorrectionsDataForMultipleServices(UserDTO userDetails, TaxDataVO taxDataVO) {

		if (taxDataVO.getAadhaarDetailsRequestVO() == null) {
			throw new BadRequestException("Aadhaar Authentication is Mandatory");
		}
		if (userDetails.getAadharNo() == null) {
			throw new BadRequestException("RTO Aadhar Details Not available");
		}
		roleValidation(userDetails, Boolean.TRUE);
		Optional<AadharDetailsResponseVO> aadhaarResponseVO = getAadhaarResponse(
				taxDataVO.getAadhaarDetailsRequestVO());
		if (!userDetails.getAadharNo().equals(String.valueOf(aadhaarResponseVO.get().getUid()))) {
			throw new BadRequestException("Unauthorized User");
		}
		RegistrationDetailsDTO optionalDto = getPRRecord(taxDataVO.getPrNo());
		if (userDetails.getOffice() == null || userDetails.getOffice().getOfficeCode() == null
				&& optionalDto.getOfficeDetails().getOfficeCode() == null) {
			throw new BadRequestException("No record found based on prNo");
		}
		validateOfficeCode(userDetails.getOffice().getOfficeCode(), optionalDto.getOfficeDetails().getOfficeCode());

		List<CorrectionEnum> corrctionsList = taxDataVO.getCorrectionServices();
		for (CorrectionEnum serviceType : corrctionsList) {
			switch (serviceType) {
			case TAXCORRECTION:
				if (taxDataVO.getTaxDetailsMasterVO() == null) {
					throw new BadRequestException("TaxDetails not available to save");
				}
				saveTaxDetails(optionalDto, taxDataVO.getTaxDetailsMasterVO(), serviceType, userDetails.getUserId());
				break;
			case FCCORRECTION:
				if (taxDataVO.getFcDetailsVO() == null) {
					throw new BadRequestException("FC Details not available to save");
				}
				saveFCDetails(optionalDto, taxDataVO.getFcDetailsVO(), serviceType, userDetails.getUserId());
				break;
			case RCCORRECTION:
				/*
				 * if (taxDataVO.getRegistrationDetailsVO() == null) { throw new
				 * BadRequestException("Registration Details not available to save" ); }
				 * regCorrectionsValidationsForFullSaveOfCorrections( optionalDto,
				 * taxDataVO.getRegistrationDetailsVO()); saveRegistrationDetails(optionalDto,
				 * taxDataVO.getRegistrationCardDetailsVO(), serviceType,
				 * userDetails.getUserId());
				 */
				break;
			case PERMITCORRECTION:
				if (taxDataVO.getPermitDetailsVO() == null) {
					throw new BadRequestException("PermitDetails not available to save");
				}
				savePermitDetails(optionalDto, taxDataVO.getPermitDetailsVO(), serviceType, userDetails.getUserId());
				break;
			default:
				throw new BadRequestException("Correction Type Not Availble");
			}
		}

		logger.info("Corrected Details saved Successfully for the record [{}]", optionalDto.getPrNo());
		return Boolean.TRUE;
	}

	@Override
	public Enclosures viewBiLaterailTaxDetails(String officeCode, String selectedRole) {
		Enclosures enc = new Enclosures();
		List<KeyValue<String, List<ImageEnclosureDTO>>> listOfImages = new ArrayList<>();
		List<DisplayEnclosures> rejectedEnclosures = new ArrayList<>();
		RegServiceVO regServiceVO = null;
		// Prepare Dash Board
		List<String> serviceList = new ArrayList<>();
		serviceList.add(ServiceEnum.BILLATERALTAX.toString());
		List<String> applicationStatus = new ArrayList<>();
		applicationStatus.add(StatusRegistration.PAYMENTDONE.toString());
		applicationStatus.add(StatusRegistration.REUPLOAD.toString());
		List<RegServiceDTO> regServiceDTOList = getSpecificRoleBaseRecord(officeCode, serviceList, applicationStatus);
		if (!regServiceDTOList.isEmpty()) {
			regServiceDTOList.sort((p1, p2) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
			RegServiceDTO regServiceDTO = regServiceDTOList.stream().findFirst().get();

			Optional<CitizenEnclosuresDTO> enclosures = citizenEnclosuresDAO
					.findByApplicationNo(regServiceDTO.getApplicationNo());
			if (!enclosures.isPresent()) {
				throw new BadRequestException("Enclosures not found for: " + regServiceDTO.getApplicationNo());
			}
			regServiceVO = regServiceMapper.convertEntity(regServiceDTO);
			listOfImages.addAll(enclosures.get().getEnclosures());
			for (KeyValue<String, List<ImageEnclosureDTO>> enclosureKeyValue : listOfImages) {
				List<ImageVO> imagesVO = enclosureMapper.convertNewEntity(enclosureKeyValue.getValue());

				rejectedEnclosures.add(new DisplayEnclosures(imagesVO));
			}
			enc.setDisplayEnclosures(rejectedEnclosures);
			enc.setRegServiceVO(regServiceVO);

		} else {
			logger.info(MessageKeys.MESSAGE_NO_RECORD_FOUND + "[{}]", regServiceVO);
		}
		return enc;
	}

	@Override
	public DashBordDetails bilateralTaxDetailsCount(String officeCode, String selectedRole) {
		DashBordDetails dashBordDetails = new DashBordDetails();
		PendingCountVo pendingCountVo = new PendingCountVo();

		pendingCountVo.setTotalCount(this.countForBilateral(officeCode, selectedRole));

		dashBordDetails.setPendingCountVo(pendingCountVo);
		return dashBordDetails;
	}

	private int countForBilateral(String officeCode, String selectedRole) {
		List<String> serviceList = new ArrayList<>();
		serviceList.add(ServiceEnum.BILLATERALTAX.toString());
		List<String> applicationStatus = new ArrayList<>();
		applicationStatus.add(StatusRegistration.PAYMENTDONE.toString());
		applicationStatus.add(StatusRegistration.REUPLOAD.toString());
		List<RegServiceDTO> regServiceDTOList = getSpecificRoleBaseRecord(officeCode, serviceList, applicationStatus);
		if (regServiceDTOList != null && !regServiceDTOList.isEmpty()) {
			return regServiceDTOList.size();
		} else {
			return 0;
		}

	}

	@Override
	public String saveBilateralTaxDetails(String userId, RtaActionVO actionVo, Optional<UserDTO> userDetails,
			String role) throws Exception {
		RegServiceDTO regDto = registratrionServicesApprovals.saveBilateralTaxDetails(userId, actionVo, userDetails,
				role);
		if (regDto != null) {
			return "Sucess";
		}
		return "Failed";
	}

	/**
	 * getPendingFinancierApplicationsList in role based Dash Board
	 */
	@Override
	public List<FinancierCreateRequestDTO> getPendingFinancierApplicationsList(String officeCode, String loggedInUser,
			String loggedInUserroles) {

		List<FinancierCreateRequestDTO> list = new ArrayList<FinancierCreateRequestDTO>();
		synchronized (officeCode.intern()) {

			if (loggedInUserroles.equals("CCO")) {
				list = finCreateReqDao.findByOfficeOfficeCodeAndApplicationStatus(officeCode,
						FinancierCreateReqStatus.INITIATED.getLabel());

				list.addAll(finCreateReqDao.findByOfficeOfficeCodeAndApplicationStatus(officeCode,
						FinancierCreateReqStatus.REUPLOAD.getLabel()));

			}
			if (loggedInUserroles.equals("AO")) {
				list = finCreateReqDao.findByOfficeOfficeCodeAndApplicationStatus(officeCode,
						FinancierCreateReqStatus.CCO_REJECTED.getLabel());
				list.addAll(finCreateReqDao.findByOfficeOfficeCodeAndApplicationStatus(officeCode,
						FinancierCreateReqStatus.CCO_APPROVED.getLabel()));
				/*
				 * list.addAll(finCreateReqDao. findByOfficeOfficeCodeAndApplicationStatus(
				 * officeCode, FinancierCreateReqStatus.REUPLOAD.getLabel()));
				 * list.addAll(finCreateReqDao. findByOfficeOfficeCodeAndApplicationStatus(
				 * officeCode, FinancierCreateReqStatus.INITIATED.getLabel()));
				 */
			}
			list.forEach(item -> {

				Optional<FinancierEnclosuresDTO> enclosures = financierEncloserDao
						.findByApplicationNo(item.getFinAppNo());
				if (enclosures.isPresent()) {
					item.setFinancierEnclosers(enclosures.get());
				}
				/*
				 * List<FinancierEnclosuresDTO> enclosers = financierEncloserDao.findAll();
				 * Optional<FinancierEnclosuresDTO> encloserOp =
				 * enclosers.stream().filter(encloser ->
				 * item.getFinAppNo().equals(encloser.getApplicationNo())). findFirst();
				 * if(encloserOp.isPresent()) { item.setFinancierEnclosers(encloserOp.get()); }
				 */

			});
		}
		return list;
	}

	@Override
	public Optional<RegServiceVO> secondVehicleValidationsforOtherState(UserDTO userDetails,
			RtaActionVO actionActionVo) {
		Optional<RegServiceDTO> optionalStagingDetails = Optional.empty();
		String searchNo;
		if (!actionActionVo.getSelectedRole().equalsIgnoreCase(RoleEnum.RTO.getName())) {
			logger.error("user not an  RTO. ", actionActionVo.getSelectedRole());
			throw new BadRequestException("user not an  RTO.the user is: " + actionActionVo.getSelectedRole());
		}
		if (StringUtils.isNoneBlank(actionActionVo.getApplicationNo())) {
			optionalStagingDetails = regServiceDAO.findByApplicationNo(actionActionVo.getApplicationNo());
			searchNo = actionActionVo.getApplicationNo();
		} else if (StringUtils.isNoneBlank(actionActionVo.getTrNumber())) {
			List<RegServiceDTO> regServiceList = regServiceDAO
					.findByRegistrationDetailsTrNoAndServiceIdsNotNull(actionActionVo.getTrNumber());
			if (!regServiceList.isEmpty()) {
				regServiceList = regServiceList.stream().filter(taxDate -> taxDate.getCreatedDate() != null)
						.collect(Collectors.toList());
				regServiceList.sort((p2, p1) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
				optionalStagingDetails = Optional.of(regServiceList.stream().findFirst().get());
				searchNo = actionActionVo.getTrNumber();
			} else {
				logger.error("Plese provide application number or tr number.");
				throw new BadRequestException("Plese provide application number or tr number.");
			}
			if (!optionalStagingDetails.isPresent()) {
				logger.error("No records found. ", searchNo);
				throw new BadRequestException("No records found. " + searchNo);
			}
			RegServiceDTO dto = optionalStagingDetails.get();
			if (!dto.getOfficeDetails().getOfficeCode().equalsIgnoreCase(userDetails.getOffice().getOfficeCode())) {
				logger.error("Invalid user. ", searchNo);
				throw new BadRequestException(
						"This " + searchNo + " belongs to " + dto.getOfficeDetails().getOfficeCode() + " office");
			}
			return regServiceMapper.convertEntity(optionalStagingDetails);
		}
		return regServiceMapper.convertEntity(optionalStagingDetails);

	}

	@Override
	public Boolean releaseSecondVechicleFortaxexemptions(UserDTO userDetails, RtaActionVO actionActionVo) {
		Boolean applicationUpdation = Boolean.FALSE;
		this.checkValidtionForRtoAadharValiadtion(userDetails, actionActionVo.getAadhaarDetailsRequestVO());

		if (StringUtils.isNoneEmpty(actionActionVo.getApplicationNo())) {
			Optional<RegServiceDTO> regServiceDetails = regServiceDAO
					.findByApplicationNo(actionActionVo.getApplicationNo());
			if (!regServiceDetails.isPresent()) {
				logger.error(" Details not found [{}]", actionActionVo.getApplicationNo());
				throw new BadRequestException(" Details not found");
			}
			if (!regServiceDetails.get().getServiceType().stream().anyMatch(id -> id.equals(ServiceEnum.DATAENTRY))
					|| !regServiceDetails.get().getServiceIds().stream()
							.anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))) {
				logger.error("Invalid Service Type");
				throw new BadRequestException("Invalid Service Type.");
			}
			RejectionHistoryDTO rejectionHistoryDTO = new RejectionHistoryDTO();
			rejectionHistoryDTO.setIsSecondVehicleRejected(Boolean.FALSE);
			rejectionHistoryDTO.setActionBy(userDetails.getUserId());
			rejectionHistoryDTO.setRole(actionActionVo.getSelectedRole());
			rejectionHistoryDTO.setRemarks(actionActionVo.getRemarks());
			rejectionHistoryDTO.setSecondVehicleExcemption(Boolean.TRUE);
			regServiceDetails.get().getRegistrationDetails().setRejectionHistory(rejectionHistoryDTO);
			SecondVehicleExcemptionDetails secondVehiclesDetails = new SecondVehicleExcemptionDetails();
			secondVehiclesDetails.setIsSecondVehicleRejected(Boolean.TRUE);
			secondVehiclesDetails.setActionBy(userDetails.getUserId());
			secondVehiclesDetails.setRole(actionActionVo.getSelectedRole());
			secondVehiclesDetails.setRemarks(actionActionVo.getRemarks());
			secondVehiclesDetails.setApplicationNo(regServiceDetails.get().getApplicationNo());
			secondVehiclesDetails.setSecondVehicleId(regServiceDetails.get().getRegistrationDetails().getPrNo());
			secondVehiclesDetails
					.setClassOfVehicle(regServiceDetails.get().getRegistrationDetails().getClassOfVehicle());
			secondVehiclesDetails.setActionTime(LocalDateTime.now());
			if ((regServiceDetails.get().getServiceIds().stream()
					.anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId())))
					&& (regServiceDetails.get().getRegistrationDetails() != null)
					&& citizenTaxService.secondVechileMasterData(
							regServiceDetails.get().getRegistrationDetails().getClassOfVehicle())
					&& !(regServiceDetails.get().getRegistrationDetails().isRegVehicleWithPR())
					&& regServiceDetails.get().getIsPRNoRequiredosSVRejected() != null
					&& regServiceDetails.get().getIsPRNoRequiredosSVRejected()) {
				regServiceDetails.get().setOsSecondVechicleFoundRTO(Boolean.FALSE);
				regServiceDetails.get().setIsPRNoRequiredosSVRejected(Boolean.FALSE);
				registratrionServices.updatesDataEntryData(regServiceDetails.get(),
						regServiceDetails.get().getRegistrationDetails());
				regServiceDetails.get().setApplicationStatus(StatusRegistration.APPROVED);
				notifications.sendNotifications(MessageTemplate.REG_OSSECONDVEHICLEPAYMENTDONE.getId(),
						regServiceDetails.get());
				applicationUpdation = Boolean.TRUE;
				regServiceDAO.save(regServiceDetails.get());
				secondVehicleExcemptionDAO.save(secondVehiclesDetails);
			}

			else if ((regServiceDetails.get().getServiceIds().stream()
					.anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId())))
					&& (regServiceDetails.get().getRegistrationDetails() != null)
					&& citizenTaxService.secondVechileMasterData(
							regServiceDetails.get().getRegistrationDetails().getClassOfVehicle())
					&& !(regServiceDetails.get().getRegistrationDetails().isRegVehicleWithPR())
					&& regServiceDetails.get().getOsSecondVechicleFoundRTO() != null
					&& regServiceDetails.get().getOsSecondVechicleFoundRTO()) {
				regServiceDetails.get().setOsSecondVechicleFoundRTO(Boolean.FALSE);
				regServiceDetails.get().setApplicationStatus(StatusRegistration.REUPLOAD);
				regServiceDAO.save(regServiceDetails.get());
				secondVehicleExcemptionDAO.save(secondVehiclesDetails);
				applicationUpdation = Boolean.TRUE;
			}

		}

		return applicationUpdation;

	}

	@Override
	public CitizenApplicationSearchResponceVO getVehicleData(ApplicationSearchVO applicationSearchVO,
			boolean requestFromSearch, UserDTO userDetails) {
		Optional<RegistrationDetailsDTO> registrationDetails = null;
		if (applicationSearchVO.getApplicationType()) {
			registrationDetails = getRegServiceDoc(applicationSearchVO.getPrNo(), applicationSearchVO.getTrNo(),
					applicationSearchVO.getApplicationNo());

		} else {
			registrationDetails = getRegDoc(applicationSearchVO.getPrNo(), applicationSearchVO.getTrNo());
		}
		String officeCode = getOfficeCode(registrationDetails.get());
		if (!applicationSearchVO.getApplicationType()) {
			if (!userDetails.getOffice().getOfficeCode().equalsIgnoreCase(officeCode)) {
				logger.error("Unauthorizaed user");
				throw new BadRequestException("Unauthorizaed user");
			}
		}
		FeeCorrectionDTO feeDto = this.getFeeDoc(registrationDetails.get().getVahanDetails().getChassisNumber(),
				applicationSearchVO.getSelectedRole());
		CitizenApplicationSearchResponceVO vo = this.getDataForFeeCorrectionSearc(feeDto, registrationDetails,
				applicationSearchVO.getSelectedRole(), requestFromSearch);
		if (applicationSearchVO.getApplicationType()) {
			Optional<PaymentTransactionDTO> paymentTransactionOpt = paymentTransactionDAO
					.findByApplicationFormRefNumOrderByRequestRequestTimeDesc(applicationSearchVO.getApplicationNo());
			if (!paymentTransactionOpt.isPresent()) {
				throw new BadRequestException(
						"Fee Datails not found for applicationNo " + applicationSearchVO.getApplicationNo());
			}
			vo.setFeeDetails(feeDetailsMapper.convertEntity(paymentTransactionOpt.get().getFeeDetailsDTO()));
		}

		return vo;

	}

	private void getTax(CitizenApplicationSearchResponceVO vo, RegistrationDetailsDTO dto) {
		List<String> taxTypes = new ArrayList<>();
		taxTypes.add(ServiceCodeEnum.QLY_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.HALF_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.YEAR_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.LIFE_TAX.getCode());
		Long amount = 0l;
		Long secondVehicleAmount = 0l;
		TaxDetailsDTO taxDetailsDto = null;
		List<TaxDetailsDTO> taxsList = taxDetailsDAO
				.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(dto.getApplicationNo(), taxTypes);
		if (CollectionUtils.isNotEmpty(taxsList)) {
			registrationService.updatePaidDateAsCreatedDate(taxsList);
			taxsList.sort((o1, o2) -> o2.getCreatedDate().compareTo(o1.getCreatedDate()));
			if (taxsList.stream().findFirst().get().getPaymentPeriod()
					.equalsIgnoreCase(ServiceCodeEnum.LIFE_TAX.getCode())) {

				// taxDetailsDto = listOfPaidTax.stream().findFirst().get();
				for (TaxDetailsDTO taxDetailsForFirstPay : taxsList) {
					if (ServiceCodeEnum.LIFE_TAX.getCode().equalsIgnoreCase(taxDetailsForFirstPay.getPaymentPeriod())) {
						if (taxDetailsForFirstPay.getTaxAmount() != null) {
							amount = amount + taxDetailsForFirstPay.getTaxAmount();
							break;
						}
					}
				}
				// amount = amount + taxDetailsDto.getTaxAmount();
				taxDetailsDto = taxsList.stream().findFirst().get();
				if (taxDetailsDto.getPaymentPeriod().equalsIgnoreCase(TaxTypeEnum.LifeTax.getDesc())) {
					if (taxsList.size() > 1) {
						for (TaxDetailsDTO taxDetailsForFirstPay : taxsList) {
							if (taxDetailsForFirstPay.getPaymentPeriod()
									.equalsIgnoreCase(TaxTypeEnum.LifeTax.getDesc())) {
								if (taxDetailsForFirstPay.getTaxAmount() != null) {
									if (!amount.equals(taxDetailsForFirstPay.getTaxAmount())) {
										secondVehicleAmount = taxDetailsForFirstPay.getTaxAmount();
									}
								}
							}
						}
					}
				}

			} else {
				taxDetailsDto = taxsList.stream().findFirst().get();
				for (TaxDetailsDTO taxDetailsForFirstPay : taxsList) {
					if (taxDetailsForFirstPay.getTaxPeriodEnd().equals(taxDetailsDto.getTaxPeriodEnd())) {
						if (taxDetailsForFirstPay.getTaxAmount() != null) {
							amount = amount + taxDetailsForFirstPay.getTaxAmount();
						}
					}
				}
			}
			Long total = amount + secondVehicleAmount;
			vo.setTaxAmount(total.toString());

			taxsList.clear();
		}
	}

	@Override
	public void saveFeeCorrection(FeeCorrectionVO vo, UserDTO userDetails) {
		Optional<RegistrationDetailsDTO> registrationDetails = null;
		if ("OTHERSTATE".equalsIgnoreCase(vo.getApplicationType())) {
			registrationDetails = getRegServiceDoc(vo.getPrNo(), vo.getTrNo(), vo.getApplicationNo());
		} else {
			registrationDetails = getRegDoc(vo.getPrNo(), vo.getTrNo());
		}
		FeeCorrectionDTO dto = feeCorrectionMapper.convertVO(vo);
		dto.setApplicationNo(registrationDetails.get().getApplicationNo());
		dto.setChassisNo(registrationDetails.get().getVahanDetails().getChassisNumber());
		FeeCorrectionDTO feeDto = this.getFeeDoc(registrationDetails.get().getVahanDetails().getChassisNumber(),
				vo.getRole());
		ActionDetailsDTO actions = new ActionDetailsDTO();
		actions.setActionBy(userDetails.getUserId());
		actions.setActionDatetime(LocalDateTime.now());
		actions.setRemarks(vo.getRemarks());
		actions.setActionByRole(Arrays.asList(vo.getRole()));
		boolean isAmountExcemted = this.validationForFeeParts(vo, feeDto, dto, userDetails, registrationDetails.get());
		if (feeDto != null) {
			if (feeDto.getActiondetails() == null || feeDto.getActiondetails().isEmpty()) {
				logger.error("action details missing for: " + feeDto.getId());
				throw new BadRequestException("action details missing for: " + feeDto.getId());
			}
			feeDto.getActiondetails().add(actions);
			dto = feeDto;
		} else {
			dto.setActiondetails(Arrays.asList(actions));
		}
		if (!userDetails.getOffice().getOfficeCode().equalsIgnoreCase(dto.getOfficeCode())) {
			logger.error("Unauthorizaed user");
			throw new BadRequestException("Unauthorizaed user");
		}
		dto.setStatus(true);
		if (isAmountExcemted) {
			dto.setStatus(false);
		}
		if (vo.getRole().equalsIgnoreCase(RoleEnum.RTO.getName())) {
			dto.setApproved(Boolean.TRUE);
		}
		feeCorrectionDAO.save(dto);
	}

	@Override
	public FeeCorrectionDTO getFeeDoc(String chassisNo, String role) {
		FeeCorrectionDTO feeCorrectionDto = null;
		Optional<FeeCorrectionDTO> optionalFeeCorrection = feeCorrectionDAO.findByChassisNoAndStatusIsTrue(chassisNo);
		if (optionalFeeCorrection.isPresent()) {
			feeCorrectionDto = optionalFeeCorrection.get();
		}
		return feeCorrectionDto;
	}

	private Optional<RegistrationDetailsDTO> getRegDoc(String prNo, String trNo) {
		if (StringUtils.isBlank(prNo) && StringUtils.isBlank(trNo)) {
			logger.error("Please provide tr number or pr number. ");
			throw new BadRequestException("Please provide tr number or pr number.");
		}
		Optional<RegistrationDetailsDTO> registrationDetails = Optional.empty();
		if (StringUtils.isNoneBlank(prNo)) {
			registrationDetails = registrationDetailDAO.findByPrNo(prNo.toUpperCase());
			if (!registrationDetails.isPresent()) {
				logger.error("no record found for : ", prNo.toUpperCase());
				throw new BadRequestException("no record found for : " + prNo.toUpperCase());
			}
		} else if (StringUtils.isNoneBlank(trNo)) {
			Optional<StagingRegistrationDetailsDTO> stagingdetailsOptional = stagingRegistrationDetailsDAO
					.findByTrNo(trNo.toUpperCase());
			if (!stagingdetailsOptional.isPresent()) {
				registrationDetails = registrationDetailDAO.findByTrNo(trNo.toUpperCase());
				if (!registrationDetails.isPresent()) {
					logger.error("no record foundfor : ", trNo.toUpperCase());
					throw new BadRequestException("no record foundfor : " + trNo.toUpperCase());
				}
			} else {
				RegistrationDetailsVO vo = regDetailMapper.convertEntity(stagingdetailsOptional.get());
				RegistrationDetailsDTO dto = regDetailMapper.convertVOForOtherState(vo);
				registrationDetails = Optional.of(dto);
			}
		} else {
			logger.error("Please provide tr number or pr number. ");
			throw new BadRequestException("Please provide tr number or pr number.");
		}
		return registrationDetails;
	}

	@Override
	public boolean validationForFeeParts(FeeCorrectionVO vo, FeeCorrectionDTO olddto, FeeCorrectionDTO newdto,
			UserDTO userDetails, RegistrationDetailsDTO registrationDetails) {
		if (StringUtils.isBlank(vo.getRemarks())) {
			logger.error("Please provide remarks. ");
			throw new BadRequestException("Please provide remarks.");
		}

		FeeDetailsDTO feeDto = new FeeDetailsDTO();
		List<FeesDTO> feeList = new ArrayList<FeesDTO>();
		Double totalFees = 0.0;
		if (vo.getApplicationFee() != null && vo.getApplicationFee() != 0) {
			FeesDTO feesDto = new FeesDTO();
			// FeePartsDetailsVo feePartsDetailsVo = entry.getValue();
			feesDto.setFeesType(ServiceCodeEnum.REGISTRATION.getTypeDesc());
			feesDto.setAmount(vo.getApplicationFee());
			totalFees += vo.getApplicationFee();
			feeList.add(feesDto);
		}
		if (vo.getCard() != null && vo.getCard() != 0) {
			FeesDTO feesDto = new FeesDTO();
			// FeePartsDetailsVo feePartsDetailsVo = entry.getValue();
			feesDto.setFeesType(ServiceCodeEnum.CARD.getTypeDesc());
			feesDto.setAmount(vo.getCard());
			totalFees += vo.getCard();
			feeList.add(feesDto);
		}
		if (vo.getPermitApplicationFee() != null && vo.getPermitApplicationFee() != 0) {
			FeesDTO feesDto = new FeesDTO();
			// FeePartsDetailsVo feePartsDetailsVo = entry.getValue();
			feesDto.setFeesType(ServiceCodeEnum.PERMIT_FEE.getTypeDesc());
			feesDto.setAmount(vo.getPermitApplicationFee());
			totalFees += vo.getPermitApplicationFee();
			feeList.add(feesDto);
		}
		if (vo.getQuarterlyTax() != null && vo.getQuarterlyTax() != 0) {
			FeesDTO feesDto = new FeesDTO();
			// FeePartsDetailsVo feePartsDetailsVo = entry.getValue();
			feesDto.setFeesType(ServiceCodeEnum.QLY_TAX.getCode());
			feesDto.setAmount(vo.getQuarterlyTax().doubleValue());
			totalFees += vo.getQuarterlyTax();
			feeList.add(feesDto);
		}
		if (vo.getCessFee() != null && vo.getCessFee() != 0) {
			FeesDTO feesDto = new FeesDTO();
			// FeePartsDetailsVo feePartsDetailsVo = entry.getValue();
			feesDto.setFeesType(ServiceCodeEnum.CESS_FEE.getCode());
			feesDto.setAmount(vo.getCessFee().doubleValue());
			totalFees += vo.getCessFee();
			feeList.add(feesDto);
		}
		if (vo.getGreenTax() != null && vo.getGreenTax() != 0) {
			FeesDTO feesDto = new FeesDTO();
			// FeePartsDetailsVo feePartsDetailsVo = entry.getValue();
			feesDto.setFeesType(ServiceCodeEnum.GREEN_TAX.getCode());
			feesDto.setAmount(vo.getGreenTax().doubleValue());
			totalFees += vo.getGreenTax();
			feeList.add(feesDto);
		}
		if (vo.getLifeTax() != null && vo.getLifeTax() != 0) {
			FeesDTO feesDto = new FeesDTO();
			// FeePartsDetailsVo feePartsDetailsVo = entry.getValue();
			feesDto.setFeesType(ServiceCodeEnum.LIFE_TAX.getCode());
			feesDto.setAmount(vo.getLifeTax().doubleValue());
			totalFees += vo.getLifeTax();
			feeList.add(feesDto);
		}
		if (vo.getServiceFee() != null && vo.getServiceFee() != 0) {
			FeesDTO feesDto = new FeesDTO();
			// FeePartsDetailsVo feePartsDetailsVo = entry.getValue();
			feesDto.setFeesType(ServiceCodeEnum.SERVICE_FEE.getTypeDesc());
			feesDto.setAmount(vo.getServiceFee());
			totalFees += vo.getServiceFee();
			feeList.add(feesDto);
		}
		if (vo.getPostal() != null && vo.getPostal() != 0) {
			FeesDTO feesDto = new FeesDTO();
			// FeePartsDetailsVo feePartsDetailsVo = entry.getValue();
			feesDto.setFeesType(ServiceCodeEnum.POSTAL_FEE.getTypeDesc());
			feesDto.setAmount(vo.getPostal());
			totalFees += vo.getPostal();
			feeList.add(feesDto);
		}
		feeDto.setFeeDetails(feeList);
		feeDto.setTotalFees(totalFees);
		feeDto.setRole(vo.getRole());
		if (olddto != null) {
			if (olddto.getFeeDetails() == null || olddto.getFeeDetails().isEmpty()) {
				logger.error("Fee details missing for: " + olddto.getId());
				throw new BadRequestException("Fee details missing for: " + olddto.getId());
			}
			// newdto.setFeeDetails(olddto.getFeeDetails());
			olddto.getFeeDetails().add(feeDto);
			if (!olddto.getCurrentRoles().contains(vo.getRole())) {
				logger.error("Unauthorizaed user");
				throw new BadRequestException("Demand Notice  is already raised");
			}
			setRoles(olddto, vo.getRole());
			if (isAmounZero(vo, Boolean.FALSE)) {
				return Boolean.TRUE;
			}
		} else {
			newdto.setFeeDetails(Arrays.asList(feeDto));
			newdto.setCreatedBy(userDetails.getUserId());
			newdto.setCreatedDate(LocalDateTime.now());
			newdto.setCreatedDateStr(LocalDateTime.now().toString());
			setRoles(newdto, vo.getRole());
			String officeCode = getOfficeCode(registrationDetails);
			newdto.setOfficeCode(officeCode);
			if (isAmounZero(vo, Boolean.TRUE)) {
				logger.error("Please provide Fee parts. ");
				throw new BadRequestException("Please provide Fee parts.");
			}
		}

		return Boolean.FALSE;
	}

	private String getOfficeCode(RegistrationDetailsDTO registrationDetails) {
		if (registrationDetails.getApplicantDetails() == null
				|| registrationDetails.getApplicantDetails().getPresentAddress() == null
				|| registrationDetails.getApplicantDetails().getPresentAddress().getMandal() == null
				|| registrationDetails.getApplicantDetails().getPresentAddress().getMandal().getMandalCode() == null) {
			logger.error("mandal details missing for office. ");
			throw new BadRequestException("mandal details missing for office.");
		}
		Optional<MandalDTO> mandalOptional = mandalDAO.findByMandalCode(
				registrationDetails.getApplicantDetails().getPresentAddress().getMandal().getMandalCode());
		if (!mandalOptional.isPresent()) {
			logger.error("Office details not found based on mandal code: "
					+ registrationDetails.getApplicantDetails().getPresentAddress().getMandal().getMandalCode());
			throw new BadRequestException("Office details not found based on mandal code: "
					+ registrationDetails.getApplicantDetails().getPresentAddress().getMandal().getMandalCode());
		}
		if (registrationDetails.getVehicleType().equalsIgnoreCase(CovCategory.N.getCode())) {
			return mandalOptional.get().getNonTransportOffice();
		} else {
			return mandalOptional.get().getTransportOfice();
		}
	}

	private void setRoles(FeeCorrectionDTO newdto, String role) {
		Set<String> rtaRoles = new HashSet<>();
		if (role.equalsIgnoreCase(RoleEnum.CCO.getName())) {
			rtaRoles.add(RoleEnum.AO.getName());
			newdto.setCurrentRoles(rtaRoles);
		} else if (role.equalsIgnoreCase(RoleEnum.AO.getName())) {
			rtaRoles.add(RoleEnum.RTO.getName());
			newdto.setCurrentRoles(rtaRoles);
		} else {
			newdto.setCurrentRoles(null);
		}
		newdto.setLockedDetails(null);

	}

	private boolean isAmounZero(FeeCorrectionVO vo, boolean isRequestNew) {
		if ((vo.getApplicationFee() == null || vo.getApplicationFee() == 0)
				&& (vo.getCard() == null || vo.getCard() == 0)
				&& (vo.getPermitApplicationFee() == null || vo.getPermitApplicationFee() == 0)
				&& (vo.getQuarterlyTax() == null || vo.getQuarterlyTax() == 0)
				&& (vo.getCessFee() == null || vo.getCessFee() == 0)
				&& (vo.getGreenTax() == null || vo.getGreenTax() == 0)
				&& (vo.getLifeTax() == null || vo.getLifeTax() == 0)
				&& (vo.getServiceFee() == null || vo.getServiceFee() == 0)
				&& (vo.getPostal() == null || vo.getPostal() == 0)) {
			if ((vo.getRole().equalsIgnoreCase(RoleEnum.RTO.getName())) && (!isRequestNew)) {
				return Boolean.TRUE;
			}

		}
		if ((vo.getQuarterlyTax() != null && vo.getQuarterlyTax() != 0)
				&& (vo.getLifeTax() != null && vo.getLifeTax() != 0)) {
			logger.error("Only life tax or quarter tax can pay not both.");
			throw new BadRequestException("Only life tax or quarter tax can pay not both.");
		}
		if ((vo.getCessFee() != null && vo.getCessFee() != 0) && (vo.getLifeTax() != null && vo.getLifeTax() != 0)) {
			logger.error("At the time of life tax should not pay cess.");
			throw new BadRequestException("At the time of life tax should not pay cess.");
		}
		return Boolean.FALSE;
	}

	@Override
	public DashBordDetails feeCorrectionCount(String officeCode, String selectedRole) {
		DashBordDetails dashBordDetails = new DashBordDetails();
		PendingCountVo pendingCountVo = new PendingCountVo();
		List<FeeCorrectionDTO> feeList = getTotalFeeCorrectionDocs(officeCode, selectedRole);
		if (feeList != null && !feeList.isEmpty()) {
			pendingCountVo.setTotalCount(feeList.size());
		} else {
			pendingCountVo.setTotalCount(0);
		}
		dashBordDetails.setPendingCountVo(pendingCountVo);
		return dashBordDetails;
	}

	private List<FeeCorrectionDTO> getTotalFeeCorrectionDocs(String officeCode, String selectedRole) {

		List<FeeCorrectionDTO> list = feeCorrectionDAO.findByOfficeCodeAndCurrentRolesIn(officeCode, selectedRole);

		return list;
	}

	@Override
	public CitizenApplicationSearchResponceVO getFeeCorrectionPendingDoc(String officeCode, String selectedRole,
			UserDTO userDetails) {
		Optional<RegistrationDetailsDTO> registrationDetails = null;
		List<FeeCorrectionDTO> feeList = getTotalFeeCorrectionDocs(officeCode, selectedRole);
		if (feeList != null && !feeList.isEmpty()) {
			feeList.sort((o1, o2) -> o1.getCreatedDate().compareTo(o2.getCreatedDate()));
			for (FeeCorrectionDTO feeDto : feeList) {
				if (feeDto.getLockedDetails() != null) {
					if (feeDto.getLockedDetails().getLockedBy().equalsIgnoreCase(userDetails.getUserId())) {
						if ("OTHERSTATE".equalsIgnoreCase(feeDto.getApplicationType())) {
							registrationDetails = getRegServiceDoc(feeDto.getPrNo(), feeDto.getTrNo(),
									feeDto.getApplicationNo());
						} else {
							registrationDetails = getRegDoc(feeDto.getPrNo(), feeDto.getTrNo());
						}

						CitizenApplicationSearchResponceVO vo = this.getDataForFeeCorrectionSearc(feeDto,
								registrationDetails, selectedRole, Boolean.FALSE);
						mapFee(vo, selectedRole);
						if ("OTHERSTATE".equalsIgnoreCase(feeDto.getApplicationType())) {
							Optional<PaymentTransactionDTO> paymentTransactionOpt = paymentTransactionDAO
									.findByApplicationFormRefNumOrderByRequestRequestTimeDesc(
											feeDto.getApplicationNo());
							if (!paymentTransactionOpt.isPresent()) {
								throw new BadRequestException(
										"Fee Datails not found for applicationNo " + feeDto.getApplicationNo());
							}
							vo.setFeeDetails(
									feeDetailsMapper.convertEntity(paymentTransactionOpt.get().getFeeDetailsDTO()));
						}
						return vo;
					}
				} else {
					if ("OTHERSTATE".equalsIgnoreCase(feeDto.getApplicationType())) {
						registrationDetails = getRegServiceDoc(feeDto.getPrNo(), feeDto.getTrNo(),
								feeDto.getApplicationNo());
					} else {
						registrationDetails = getRegDoc(feeDto.getPrNo(), feeDto.getTrNo());
					}

					CitizenApplicationSearchResponceVO vo = this.getDataForFeeCorrectionSearc(feeDto,
							registrationDetails, selectedRole, Boolean.FALSE);
					mapFee(vo, selectedRole);
					LockedDetailsDTO lockDetails = new LockedDetailsDTO();
					lockDetails.setLockedBy(userDetails.getUserId());
					lockDetails.setLockedDate(LocalDateTime.now());
					lockDetails.setLockedByRole(selectedRole);
					feeDto.setLockedDetails(lockDetails);
					if ("OTHERSTATE".equalsIgnoreCase(feeDto.getApplicationType())) {
						Optional<PaymentTransactionDTO> paymentTransactionOpt = paymentTransactionDAO
								.findByApplicationFormRefNumOrderByRequestRequestTimeDesc(feeDto.getApplicationNo());
						if (!paymentTransactionOpt.isPresent()) {
							throw new BadRequestException(
									"Fee Datails not found for applicationNo " + feeDto.getApplicationNo());
						}
						vo.setFeeDetails(
								feeDetailsMapper.convertEntity(paymentTransactionOpt.get().getFeeDetailsDTO()));
					}

					feeCorrectionDAO.save(feeDto);
					return vo;
				}
			}

		}
		return null;
	}

	private CitizenApplicationSearchResponceVO getDataForFeeCorrectionSearc(FeeCorrectionDTO feeDto,
			Optional<RegistrationDetailsDTO> registrationDetails, String selectedRole, boolean requestFromSearch) {
		CitizenApplicationSearchResponceVO vo = registrationService
				.setRegistrationDetailsIntoResultVO(registrationDetails.get());
		if (feeDto != null) {
			/*
			 * if (requestFromSearch) { logger.error("Application already process."); throw
			 * new BadRequestException("Application already process."); }
			 */
			vo.setFeeCorrectionVO(feeCorrectionMapper.convertEntity(feeDto));
		}

		getTax(vo, registrationDetails.get());
		return vo;
	}

	private void mapFee(CitizenApplicationSearchResponceVO vo, String selectedRole) {
		if (vo.getFeeCorrectionVO() != null && vo.getFeeCorrectionVO().getFeeDetails() != null
				&& !vo.getFeeCorrectionVO().getFeeDetails().isEmpty()) {
			if (selectedRole.equalsIgnoreCase(RoleEnum.AO.getName())) {
				getlastUpdatedFee(vo, RoleEnum.CCO.getName());
			} else if (selectedRole.equalsIgnoreCase(RoleEnum.RTO.getName())) {
				getlastUpdatedFee(vo, RoleEnum.AO.getName());
			}
		}
	}

	private void getlastUpdatedFee(CitizenApplicationSearchResponceVO citizenVo, String lastUser) {
		for (FeeDetailsVO vo : citizenVo.getFeeCorrectionVO().getFeeDetails()) {
			if (vo.getRole().equalsIgnoreCase(lastUser)) {
				for (FeesVO feeVo : vo.getFeeDetails()) {
					if (feeVo.getFeesType().equalsIgnoreCase(ServiceCodeEnum.REGISTRATION.getTypeDesc())) {
						citizenVo.getFeeCorrectionVO().setApplicationFee(feeVo.getAmount());
					} else if (feeVo.getFeesType().equalsIgnoreCase(ServiceCodeEnum.CARD.getTypeDesc())) {
						citizenVo.getFeeCorrectionVO().setCard(feeVo.getAmount());
					} else if (feeVo.getFeesType().equalsIgnoreCase(ServiceCodeEnum.PERMIT_FEE.getTypeDesc())) {
						citizenVo.getFeeCorrectionVO().setPermitApplicationFee(feeVo.getAmount());
					} else if (feeVo.getFeesType().equalsIgnoreCase(ServiceCodeEnum.QLY_TAX.getCode())) {
						citizenVo.getFeeCorrectionVO().setQuarterlyTax(feeVo.getAmount().longValue());
					} else if (feeVo.getFeesType().equalsIgnoreCase(ServiceCodeEnum.CESS_FEE.getCode())) {
						citizenVo.getFeeCorrectionVO().setCessFee(feeVo.getAmount().longValue());
					} else if (feeVo.getFeesType().equalsIgnoreCase(ServiceCodeEnum.GREEN_TAX.getCode())) {
						citizenVo.getFeeCorrectionVO().setGreenTax(feeVo.getAmount().longValue());
					} else if (feeVo.getFeesType().equalsIgnoreCase(ServiceCodeEnum.LIFE_TAX.getCode())) {
						citizenVo.getFeeCorrectionVO().setLifeTax(feeVo.getAmount().longValue());
					} else if (feeVo.getFeesType().equalsIgnoreCase(ServiceCodeEnum.SERVICE_FEE.getTypeDesc())) {
						citizenVo.getFeeCorrectionVO().setServiceFee(feeVo.getAmount());
					} else if (feeVo.getFeesType().equalsIgnoreCase(ServiceCodeEnum.POSTAL_FEE.getTypeDesc())) {
						citizenVo.getFeeCorrectionVO().setPostal(feeVo.getAmount());
					}
				}
			}
		}
	}

	@Override
	public CitizenSearchReportVO getVcrDetailsForCorrections(ApplicationSearchVO applicationSearchVO,
			UserDTO userDetails) {

		applicationSearchVO.setRequestFromAO(Boolean.TRUE);
		CitizenSearchReportVO searchResult = registrationService.applicationSearchForVcr(applicationSearchVO);
		if (StringUtils.isNoneBlank(
				searchResult.getVcrList().stream().findFirst().get().getRegistration().getRegApplicationNo())) {
			/*
			 * String applicationNo =
			 * searchResult.getVcrList().stream().findFirst().get().getRegistration()
			 * .getRegApplicationNo(); Optional<RegistrationDetailsDTO> registrationDetails
			 * = registrationDetailDAO .findByApplicationNo(applicationNo); if
			 * (registrationDetails.isPresent()) { if
			 * (!registrationDetails.get().getOfficeDetails().getOfficeCode()
			 * .equalsIgnoreCase(userDetails.getOffice().getOfficeCode())) {
			 * logger.error("No Authorization for this office Code: " +
			 * userDetails.getOffice().getOfficeCode()); throw new BadRequestException(
			 * "No Authorization for this office Code: " +
			 * userDetails.getOffice().getOfficeCode()); } } else {
			 * Optional<StagingRegistrationDetailsDTO> stagingdetailsOptional =
			 * stagingRegistrationDetailsDAO .findByApplicationNo(applicationNo); if
			 * (stagingdetailsOptional.isPresent()) { if
			 * (!stagingdetailsOptional.get().getOfficeDetails().getOfficeCode()
			 * .equalsIgnoreCase(userDetails.getOffice().getOfficeCode())) { logger.error(
			 * "No Authorization for this office Code: " +
			 * userDetails.getOffice().getOfficeCode()); throw new BadRequestException(
			 * "No Authorization for this office Code: " +
			 * userDetails.getOffice().getOfficeCode()); } } else { Optional<RegServiceDTO>
			 * regServices = regServiceDAO.findByApplicationNo(applicationNo); if
			 * (!regServices.isPresent()) { logger.error("no records found for: " +
			 * applicationNo); throw new BadRequestException("no records found for: " +
			 * applicationNo); } if (!regServices.get().getOfficeDetails().getOfficeCode()
			 * .equalsIgnoreCase(userDetails.getOffice().getOfficeCode())) { logger.error(
			 * "No Authorization for this office Code: " +
			 * userDetails.getOffice().getOfficeCode()); throw new BadRequestException(
			 * "No Authorization for this office Code: " +
			 * userDetails.getOffice().getOfficeCode()); } } }
			 */}
		VcrFinalServiceDTO vcrDto = this
				.checkVehicleTrNotGenerated(finalServiceMapper.convertVO(searchResult.getVcrList()));
		if (vcrDto != null) {
			if (vcrDto.getPartiallyClosed() != null && vcrDto.getPartiallyClosed()) {
				searchResult.setUploadTrCopy(true);
			}

		}
		return searchResult;
	}

	@Override
	public void saveOffences(CitizenSearchReportVO vo, UserDTO userDetails, HttpServletRequest request) {

		if (vo.getAadhaarDetailsRequestVO() == null) {
			logger.error("Please authenticate aadhaar: " + userDetails.getUserId());
			throw new BadRequestException("Please authenticate aadhaar: " + userDetails.getUserId());
		}

		if (StringUtils.isBlank(userDetails.getAadharNo())) {
			logger.error("Please update aadhaar number in RTA data base: " + userDetails.getUserId());
			throw new BadRequestException("Please update aadhaar number in RTA data base: " + userDetails.getUserId());
		}

		if (!userDetails.getAadharNo().equalsIgnoreCase(vo.getAadhaarDetailsRequestVO().getUid_num())) {
			logger.error("Given aadhar number[" + vo.getAadhaarDetailsRequestVO().getUid_num()
					+ "] RTA data base aadhar number missmatched for user id: " + userDetails.getUserId());
			throw new BadRequestException("Given aadhar number[" + vo.getAadhaarDetailsRequestVO().getUid_num()
					+ "] RTA data base aadhar number missmatched for user id: " + userDetails.getUserId());
		}

		List<VcrFinalServiceDTO> vcrList = registrationService
				.getVcrDetails(Arrays.asList(vo.getVcrList().stream().findFirst().get().getVcr().getVcrNumber()), true,false);
		if (StringUtils.isNoneBlank(vcrList.stream().findFirst().get().getRegistration().getRegApplicationNo())) {
			/*
			 * String applicationNo =
			 * vcrList.stream().findFirst().get().getRegistration().getRegApplicationNo();
			 * Optional<RegistrationDetailsDTO> registrationDetails = registrationDetailDAO
			 * .findByApplicationNo(applicationNo); if (registrationDetails.isPresent()) {
			 * if (!registrationDetails.get().getOfficeDetails().getOfficeCode()
			 * .equalsIgnoreCase(userDetails.getOffice().getOfficeCode())) {
			 * logger.error("No Authorization for this office Code: " +
			 * userDetails.getOffice().getOfficeCode()); throw new BadRequestException(
			 * "No Authorization for this office Code: " +
			 * userDetails.getOffice().getOfficeCode()); } } else {
			 * Optional<StagingRegistrationDetailsDTO> stagingdetailsOptional =
			 * stagingRegistrationDetailsDAO .findByApplicationNo(applicationNo); if
			 * (stagingdetailsOptional.isPresent()) { if
			 * (!stagingdetailsOptional.get().getOfficeDetails().getOfficeCode()
			 * .equalsIgnoreCase(userDetails.getOffice().getOfficeCode())) { logger.error(
			 * "No Authorization for this office Code: " +
			 * userDetails.getOffice().getOfficeCode()); throw new BadRequestException(
			 * "No Authorization for this office Code: " +
			 * userDetails.getOffice().getOfficeCode()); } } else { Optional<RegServiceDTO>
			 * regServices = regServiceDAO.findByApplicationNo(applicationNo); if
			 * (!regServices.isPresent()) { logger.error("no records found for: " +
			 * applicationNo); throw new BadRequestException("no records found for: " +
			 * applicationNo); } if (!regServices.get().getOfficeDetails().getOfficeCode()
			 * .equalsIgnoreCase(userDetails.getOffice().getOfficeCode())) { logger.error(
			 * "No Authorization for this office Code: " +
			 * userDetails.getOffice().getOfficeCode()); throw new BadRequestException(
			 * "No Authorization for this office Code: " +
			 * userDetails.getOffice().getOfficeCode());
			 * 
			 * } } }
			 */}

		AadharDetailsResponseVO aadhaarResVo = registrationService.getAadharResponse(vo.getAadhaarDetailsRequestVO(),
				setAadhaarSourceDetails(vo, userDetails));
		if (aadhaarResVo == null) {
			logger.error("Authentication Failed");
			throw new BadRequestException("Authentication Failed");
		}

		this.otherStateVcrDataCorrection(vo.getVcrList(), vcrList, userDetails, vo.getSelectedRole(),
				request.getRemoteAddr());
		for (VcrFinalServiceVO vcrVo : vo.getVcrList()) {
			for (OffenceVO offenceVo : vcrVo.getOffence().getOffence()) {
				for (VcrFinalServiceDTO vcrDto : vcrList) {
					for (int i = 0; i < vcrDto.getOffence().getOffence().size(); i++) {
						if (vcrDto.getVcr().getVcrNumber().equalsIgnoreCase(vcrVo.getVcr().getVcrNumber()) && vcrDto
								.getOffence().getOffence().get(i).getSlno().equalsIgnoreCase(offenceVo.getSlno())) {
							if (offenceVo.getOtherOffence() != null && offenceVo.getOtherOffence()
									&& offenceVo.getAmount1() != null && offenceVo.getAmount1() > 0
									&& (vcrDto.getOffence().getOffence().get(i).getAmount1() == null
											|| vcrDto.getOffence().getOffence().get(i).getAmount1() <= 0)) {
								vcrDto.getOffence().getOffence().get(i).setAmount1(offenceVo.getAmount1());
								vcrDto.getOffence().getOffence().get(i).setAmount2(offenceVo.getAmount1());
								vcrDto.getOffence().getOffence().get(i).setFixedAmount(offenceVo.getAmount1());
								setActions(vcrDto, "otheroffenceAmount", userDetails, vo.getSelectedRole(),
										request.getRemoteAddr(), null, null);
							}
							if (offenceVo.isCorrectionsDone()) {
								Optional<OffenceDTO> mappedOffence = offenceDAO.findByOffenceDescriptionAndStatusTrue(
										vcrDto.getOffence().getOffence().get(i).getMappedOffence());
								if (mappedOffence == null || !mappedOffence.isPresent()) {
									logger.error("master offence not found for: "
											+ vcrDto.getOffence().getOffence().get(i).getMappedOffence());
									throw new BadRequestException("master offence not found for: "
											+ vcrDto.getOffence().getOffence().get(i).getMappedOffence());
								}
								// Insurence
								if (mappedOffence.get().getSlno().equalsIgnoreCase("100000")) {
									if (StringUtils.isBlank(offenceVo.getCompanyName())) {
										logger.error("Please provide insurence company name: "
												+ vcrVo.getVcr().getVcrNumber());
										throw new BadRequestException("Please provide insurence company name: "
												+ vcrVo.getVcr().getVcrNumber());
									}
									if (offenceVo.getValidFrom() == null) {
										logger.error("Please provide insurence valid from: "
												+ vcrVo.getVcr().getVcrNumber());
										throw new BadRequestException("Please provide insurence valid from: "
												+ vcrVo.getVcr().getVcrNumber());
									}
									if (offenceVo.getValidTo() == null) {
										logger.error(
												"Please provide insurence valid to: " + vcrVo.getVcr().getVcrNumber());
										throw new BadRequestException(
												"Please provide insurence valid to: " + vcrVo.getVcr().getVcrNumber());
									}
									mappedOffence.get().setCompanyName(offenceVo.getCompanyName());
									mappedOffence.get().setValidFrom(offenceVo.getValidFrom());
									mappedOffence.get().setValidTo(offenceVo.getValidTo());
								}
								// Tax amount
								if (mappedOffence.get().getSlno().equalsIgnoreCase("1000001")) {
									if (offenceVo.getTaxAmount() == null) {
										logger.error("Please provide tax amount: " + vcrVo.getVcr().getVcrNumber());
										throw new BadRequestException(
												"Please provide tax amount: " + vcrVo.getVcr().getVcrNumber());
									}
									if (offenceVo.getValidFrom() == null) {
										logger.error("Please provide tax valid from: " + vcrVo.getVcr().getVcrNumber());
										throw new BadRequestException(
												"Please provide FC valid from: " + vcrVo.getVcr().getVcrNumber());
									}
									if (offenceVo.getValidTo() == null) {
										logger.error("Please provide tax valid to: " + vcrVo.getVcr().getVcrNumber());
										throw new BadRequestException(
												"Please provide FC valid to: " + vcrVo.getVcr().getVcrNumber());
									}
									mappedOffence.get().setTaxAmount(offenceVo.getTaxAmount());
									mappedOffence.get().setValidFrom(offenceVo.getValidFrom());
									mappedOffence.get().setValidTo(offenceVo.getValidTo());
								}
								// FC
								if (mappedOffence.get().getSlno().equalsIgnoreCase("1000002")) {
									if (StringUtils.isBlank(offenceVo.getFcNumber())) {
										logger.error("Please provide FC number: " + vcrVo.getVcr().getVcrNumber());
										throw new BadRequestException(
												"Please provide FC number: " + vcrVo.getVcr().getVcrNumber());
									}
									if (offenceVo.getValidFrom() == null) {
										logger.error("Please provide FC valid from: " + vcrVo.getVcr().getVcrNumber());
										throw new BadRequestException(
												"Please provide FC valid from: " + vcrVo.getVcr().getVcrNumber());
									}
									if (offenceVo.getValidTo() == null) {
										logger.error("Please provide FC valid to: " + vcrVo.getVcr().getVcrNumber());
										throw new BadRequestException(
												"Please provide FC valid to: " + vcrVo.getVcr().getVcrNumber());
									}
									mappedOffence.get().setFcNumber(offenceVo.getFcNumber());
									mappedOffence.get().setValidFrom(offenceVo.getValidFrom());
									mappedOffence.get().setValidTo(offenceVo.getValidTo());
								}
								// permit
								if (mappedOffence.get().getSlno().equalsIgnoreCase("1000003")) {
									if (StringUtils.isBlank(offenceVo.getPermitNumber())) {
										logger.error("Please provide permit number: " + vcrVo.getVcr().getVcrNumber());
										throw new BadRequestException(
												"Please provide permit number: " + vcrVo.getVcr().getVcrNumber());
									}
									if (offenceVo.getValidFrom() == null) {
										logger.error(
												"Please provide permit valid from: " + vcrVo.getVcr().getVcrNumber());
										throw new BadRequestException(
												"Please provide permit valid from: " + vcrVo.getVcr().getVcrNumber());
									}
									if (offenceVo.getValidTo() == null) {
										logger.error("Please provide FC valid to: " + vcrVo.getVcr().getVcrNumber());
										throw new BadRequestException(
												"Please provide FC valid to: " + vcrVo.getVcr().getVcrNumber());
									}
									mappedOffence.get().setPermitNumber(offenceVo.getPermitNumber());
									mappedOffence.get().setValidFrom(offenceVo.getValidFrom());
									mappedOffence.get().setValidTo(offenceVo.getValidTo());
								}
								// DL
								if (mappedOffence.get().getSlno().equalsIgnoreCase("1000004")) {
									if (StringUtils.isBlank(offenceVo.getDlNumber())) {
										logger.error("Please provide DL number: " + vcrVo.getVcr().getVcrNumber());
										throw new BadRequestException(
												"Please provide DL number: " + vcrVo.getVcr().getVcrNumber());
									}
									mappedOffence.get().setDlNumber(offenceVo.getDlNumber());

								}
								vcrDto.getOffence().getOffence().set(i, mappedOffence.get());// =
								// mappedOffence.get();
								setActions(vcrDto, "offenceCorrection", userDetails, vo.getSelectedRole(),
										request.getRemoteAddr(), null, null);
								break;
							}
						}

					}
				}
			}
		}
		String chassiNo = vcrList.stream().findFirst().get().getRegistration().getChassisNumber();
		if (vo.getDisposalType() != null) {
			switch (vo.getDisposalType()) {
			case COURTORDER:
				saveCourtOrder(vcrList, vo, chassiNo, userDetails, request.getRemoteAddr());
				break;

			case PROSECUTION:
				saveProsection(vcrList, vo, chassiNo, userDetails, request.getRemoteAddr());
				break;
			case ACTIONAGAINSTPERMIT:
				// saveCourtOrder(vcrList, vo,chassiNo);
				break;
			case ACTIONAGAINSTDL:
				// saveCourtOrder(vcrList, vo,chassiNo);
				break;
			case ACTIONAGAINSTREGISTRATION:
				// saveCourtOrder(vcrList, vo,chassiNo);
				break;
			case DEPARTMENTALAUCTION:
				saveDepartmentAuction(vcrList, vo, chassiNo, userDetails, request.getRemoteAddr());
				break;
			case SIZEDRELEASETYPE:
				saveSizedReleaseType(vcrList, vo, chassiNo, userDetails, request.getRemoteAddr());
				break;
			default:
				break;
			}
		}
		VcrFinalServiceDTO vcrDto = this.checkVehicleTrNotGenerated(vcrList);
		if (vcrDto != null) {
			for (VcrFinalServiceVO vcrVo : vo.getVcrList()) {
				if (vcrDto.getVcr().getVcrNumber().equalsIgnoreCase(vcrVo.getVcr().getVcrNumber())
						&& vo.getDisposalType() != null
						&& !vo.getDisposalType().equals(DisposalType.SIZEDRELEASETYPE)) {// SIZEDRELEASETYPE
					if (vcrDto.getPartiallyClosed() == null || !vcrDto.getPartiallyClosed()) {
						logger.error("VCR payment not done Please pay vcr payment first ",
								vcrDto.getVcr().getVcrNumber());
						throw new BadRequestException(
								"VCR payment not done Please pay vcr payment first " + vcrDto.getVcr().getVcrNumber());
					}

					if (vo.isUploadTrCopy()) {
						if (vo.getFiles() == null || vo.getFiles().isEmpty()) {
							logger.error("Please upload TR copy ", vcrDto.getVcr().getVcrNumber());
							throw new BadRequestException("Please upload TR copy " + vcrDto.getVcr().getVcrNumber());
						}
						setActions(vcrDto, "UploadTRCopy", userDetails, vo.getSelectedRole(), request.getRemoteAddr(),
								null, null);
						this.saveImagesForVCr(vcrDto, vo.getFiles(), EnclosureType.TRCOPY.getValue());
						vcrDto.setIsVcrClosed(Boolean.TRUE);
					}
				}
			}

		}
		vcrFinalServiceDAO.save(vcrList);
		vcrList = null;
	}

	private void otherStateVcrDataCorrection(List<VcrFinalServiceVO> vcrVo, List<VcrFinalServiceDTO> vcrDto,
			UserDTO userDetails, String selectedRole, String ipAddress) {
		for (VcrFinalServiceVO vo : vcrVo) {
			for (VcrFinalServiceDTO dto : vcrDto) {
				if (dto.getVcr().getVcrNumber().equalsIgnoreCase(vo.getVcr().getVcrNumber())) {

					List<VcrCorrectionLogDTO> correctionsData = new ArrayList<>();
					if (StringUtils.isBlank(dto.getRegistration().getRegNo())
							&& StringUtils.isNoneBlank(vo.getRegistration().getRegNo())) {
						dto.getRegistration().setRegNo(vo.getRegistration().getRegNo());
						setCoorectionsLog(correctionsData, "PRNO", "", vo.getRegistration().getRegNo(), null, null,
								null, null, userDetails, selectedRole, ipAddress);
					} else {
						if (StringUtils.isNoneBlank(dto.getRegistration().getRegNo())
								&& StringUtils.isNoneBlank(vo.getRegistration().getRegNo()) && !vo.getRegistration()
										.getRegNo().equalsIgnoreCase(dto.getRegistration().getRegNo())) {
							setCoorectionsLog(correctionsData, "PRNO", dto.getRegistration().getRegNo(),
									vo.getRegistration().getRegNo(), null, null, null, null, userDetails, selectedRole,
									ipAddress);
							dto.getRegistration().setRegNo(vo.getRegistration().getRegNo());

						}
					}
					if (StringUtils.isBlank(dto.getRegistration().getTrNo())
							&& StringUtils.isNoneBlank(vo.getRegistration().getTrNo())) {
						dto.getRegistration().setTrNo(vo.getRegistration().getTrNo());
						setCoorectionsLog(correctionsData, "TRNO", "", vo.getRegistration().getTrNo(), null, null, null,
								null, userDetails, selectedRole, ipAddress);
					} else {
						if (StringUtils.isNoneBlank(dto.getRegistration().getTrNo())
								&& StringUtils.isNoneBlank(vo.getRegistration().getTrNo())
								&& !vo.getRegistration().getTrNo().equalsIgnoreCase(dto.getRegistration().getTrNo())) {
							setCoorectionsLog(correctionsData, "TRNO", dto.getRegistration().getTrNo(),
									vo.getRegistration().getTrNo(), null, null, null, null, userDetails, selectedRole,
									ipAddress);
							dto.getRegistration().setTrNo(vo.getRegistration().getTrNo());

						}
					}
					if (StringUtils.isBlank(dto.getRegistration().getChassisNumber())
							&& StringUtils.isNoneBlank(vo.getRegistration().getChassisNumber())) {
						dto.getRegistration().setChassisNumber(vo.getRegistration().getChassisNumber());
						setCoorectionsLog(correctionsData, "CHASSIS NUMBER", "",
								vo.getRegistration().getChassisNumber(), null, null, null, null, userDetails,
								selectedRole, ipAddress);
					} else {
						if (StringUtils.isNoneBlank(dto.getRegistration().getChassisNumber())
								&& StringUtils.isNoneBlank(vo.getRegistration().getChassisNumber())
								&& !vo.getRegistration().getChassisNumber()
										.equalsIgnoreCase(dto.getRegistration().getChassisNumber())) {
							setCoorectionsLog(correctionsData, "CHASSIS NUMBER",
									dto.getRegistration().getChassisNumber(), vo.getRegistration().getChassisNumber(),
									null, null, null, null, userDetails, selectedRole, ipAddress);
							dto.getRegistration().setChassisNumber(vo.getRegistration().getChassisNumber());

						}
					}
					if (StringUtils.isBlank(dto.getRegistration().getEngineNumber())
							&& StringUtils.isNoneBlank(vo.getRegistration().getEngineNumber())) {
						dto.getRegistration().setEngineNumber(vo.getRegistration().getEngineNumber());
						setCoorectionsLog(correctionsData, "ENGINE NUMBER", "", vo.getRegistration().getEngineNumber(),
								null, null, null, null, userDetails, selectedRole, ipAddress);
					} else {
						if (StringUtils.isNoneBlank(dto.getRegistration().getEngineNumber())
								&& StringUtils.isNoneBlank(vo.getRegistration().getEngineNumber())
								&& !vo.getRegistration().getEngineNumber()
										.equalsIgnoreCase(dto.getRegistration().getEngineNumber())) {
							setCoorectionsLog(correctionsData, "ENGINE NUMBER", dto.getRegistration().getEngineNumber(),
									vo.getRegistration().getEngineNumber(), null, null, null, null, userDetails,
									selectedRole, ipAddress);
							dto.getRegistration().setEngineNumber(vo.getRegistration().getEngineNumber());

						}
					}
					/*
					 * if
					 * (StringUtils.isBlank(dto.getRegistration().getClasssOfVehicle().getCovcode())
					 * &&
					 * StringUtils.isNoneBlank(vo.getRegistration().getClasssOfVehicle().getCovcode(
					 * ))) { dto.getRegistration().getClasssOfVehicle()
					 * .setCovcode(vo.getRegistration().getClasssOfVehicle().getCovcode());
					 * setCoorectionsLog(correctionsData, "CLASS OF VEHICLE", "",
					 * vo.getRegistration().getClasssOfVehicle().getCovcode(), null, null, null,
					 * null, userDetails, selectedRole, ipAddress); } else { if
					 * (StringUtils.isNoneBlank(dto.getRegistration().getClasssOfVehicle().
					 * getCovcode()) && !vo.getRegistration().getClasssOfVehicle().getCovcode()
					 * .equalsIgnoreCase(dto.getRegistration().getClasssOfVehicle().getCovcode())) {
					 * dto.getRegistration().getClasssOfVehicle()
					 * .setCovcode(vo.getRegistration().getClasssOfVehicle().getCovcode());
					 * setCoorectionsLog(correctionsData, "CLASS OF VEHICLE",
					 * dto.getRegistration().getClasssOfVehicle().getCovcode(),
					 * vo.getRegistration().getClasssOfVehicle().getCovcode(), null, null, null,
					 * null, userDetails, selectedRole, ipAddress); } }
					 */
					if (StringUtils.isBlank(dto.getOwnerDetails().getFullName())
							&& StringUtils.isNoneBlank(vo.getOwnerDetails().getFullName())) {
						dto.getOwnerDetails().setFullName(vo.getOwnerDetails().getFullName());
						setCoorectionsLog(correctionsData, "OWNER NAME", "", vo.getOwnerDetails().getFullName(), null,
								null, null, null, userDetails, selectedRole, ipAddress);
					} else {
						if (StringUtils.isNoneBlank(dto.getOwnerDetails().getFullName())
								&& StringUtils.isNoneBlank(vo.getOwnerDetails().getFullName()) && !vo.getOwnerDetails()
										.getFullName().equalsIgnoreCase(dto.getOwnerDetails().getFullName())) {
							setCoorectionsLog(correctionsData, "OWNER NAME", dto.getOwnerDetails().getFullName(),
									vo.getOwnerDetails().getFullName(), null, null, null, null, userDetails,
									selectedRole, ipAddress);
							dto.getOwnerDetails().setFullName(vo.getOwnerDetails().getFullName());

						}
					}

					if (dto.getRegistration().getUlw() == null && vo.getRegistration().getUlw() != null) {
						dto.getRegistration().setUlw(vo.getRegistration().getUlw());
						setCoorectionsLog(correctionsData, "ULW", "", "", null, vo.getRegistration().getUlw(), null,
								null, userDetails, selectedRole, ipAddress);
					} else {
						if (dto.getRegistration().getUlw() != null && vo.getRegistration().getUlw() != null
								&& (!dto.getRegistration().getUlw().equals(vo.getRegistration().getUlw()))) {
							setCoorectionsLog(correctionsData, "ULW", "", "", dto.getRegistration().getUlw(),
									vo.getRegistration().getUlw(), null, null, userDetails, selectedRole, ipAddress);
							dto.getRegistration().setUlw(vo.getRegistration().getUlw());

						}
					}
					if (dto.getRegistration().getGvwc() == null && vo.getRegistration().getGvw() != null) {
						dto.getRegistration().setGvwc(vo.getRegistration().getGvw());
						setCoorectionsLog(correctionsData, "GLWC", "", "", null, vo.getRegistration().getGvw(), null,
								null, userDetails, selectedRole, ipAddress);
					} else {
						if (dto.getRegistration().getGvwc() != null && vo.getRegistration().getGvw() != null
								&& (!dto.getRegistration().getGvwc().equals(vo.getRegistration().getGvw()))) {
							setCoorectionsLog(correctionsData, "GLWC", "", "", dto.getRegistration().getGvwc(),
									vo.getRegistration().getGvw(), null, null, userDetails, selectedRole, ipAddress);
							dto.getRegistration().setGvwc(vo.getRegistration().getGvw());

						}
					}
					if (dto.getRegistration().getSeatingCapacity() == null
							&& vo.getRegistration().getSeatingCapacity() > 0) {
						dto.getRegistration().setSeatingCapacity(vo.getRegistration().getSeatingCapacity());
						setCoorectionsLog(correctionsData, "SEATING CAPACITY", "", "", null,
								vo.getRegistration().getSeatingCapacity(), null, null, userDetails, selectedRole,
								ipAddress);
					} else {
						if (dto.getRegistration().getSeatingCapacity() != null
								&& vo.getRegistration().getSeatingCapacity() > 0 && (dto.getRegistration()
										.getSeatingCapacity() != vo.getRegistration().getSeatingCapacity())) {
							setCoorectionsLog(correctionsData, "SEATING CAPACITY", "", "",
									dto.getRegistration().getSeatingCapacity(),
									vo.getRegistration().getSeatingCapacity(), null, null, userDetails, selectedRole,
									ipAddress);
							dto.getRegistration().setSeatingCapacity(vo.getRegistration().getSeatingCapacity());

						}
					}

					if (StringUtils.isBlank(dto.getDriver().getFullName())
							&& StringUtils.isNoneBlank(vo.getDriver().getFullName())) {
						dto.getDriver().setFullName(vo.getDriver().getFullName());
						setCoorectionsLog(correctionsData, "DRIVER NAME", "", vo.getDriver().getFullName(), null, null,
								null, null, userDetails, selectedRole, ipAddress);
					} else {
						if (StringUtils.isNoneBlank(dto.getDriver().getFullName())
								&& StringUtils.isNoneBlank(vo.getDriver().getFullName())
								&& !vo.getDriver().getFullName().equalsIgnoreCase(dto.getDriver().getFullName())) {
							setCoorectionsLog(correctionsData, "DRIVER NAME", dto.getDriver().getFullName(),
									vo.getDriver().getFullName(), null, null, null, null, userDetails, selectedRole,
									ipAddress);
							dto.getDriver().setFullName(vo.getDriver().getFullName());

						}
					}

					if (StringUtils.isBlank(dto.getDriver().getDriverLicense())
							&& StringUtils.isNoneBlank(vo.getDriver().getDriverLicense())) {
						dto.getDriver().setDriverLicense(vo.getDriver().getDriverLicense());
						setCoorectionsLog(correctionsData, "LICNENCE NUMBER", "", vo.getDriver().getDriverLicense(),
								null, null, null, null, userDetails, selectedRole, ipAddress);
					} else {
						if (StringUtils.isNoneBlank(dto.getDriver().getDriverLicense())
								&& StringUtils.isNoneBlank(vo.getDriver().getDriverLicense()) && !vo.getDriver()
										.getDriverLicense().equalsIgnoreCase(dto.getDriver().getDriverLicense())) {
							setCoorectionsLog(correctionsData, "LICNENCE NUMBER", dto.getDriver().getDriverLicense(),
									vo.getDriver().getDriverLicense(), null, null, null, null, userDetails,
									selectedRole, ipAddress);
							dto.getDriver().setDriverLicense(vo.getDriver().getDriverLicense());

						}
					}

					if (dto.getDriver().getDateOfBirth() == null && vo.getDriver().getDateOfBirth() != null) {
						dto.getDriver().setDateOfBirth(vo.getDriver().getDateOfBirth());
						setCoorectionsLog(correctionsData, "DRIVER DOB", "", "", null, null, null,
								vo.getDriver().getDateOfBirth(), userDetails, selectedRole, ipAddress);
					} else {
						if (dto.getDriver().getDateOfBirth() != null && vo.getDriver().getDateOfBirth() != null
								&& !vo.getDriver().getDateOfBirth().equals(dto.getDriver().getDateOfBirth())) {
							setCoorectionsLog(correctionsData, "DRIVER DOB", "", "", null, null,
									dto.getDriver().getDateOfBirth(), vo.getDriver().getDateOfBirth(), userDetails,
									selectedRole, ipAddress);
							dto.getDriver().setDateOfBirth(vo.getDriver().getDateOfBirth());

						}
					}

					if (dto.getVehicleProceeding() != null && dto.getVehicleProceeding().getFrom() != null
							&& StringUtils.isBlank(dto.getVehicleProceeding().getFrom().getPlace())
							&& StringUtils.isNoneBlank(vo.getVehicleProceeding().getFrom().getPlace())) {
						dto.getVehicleProceeding().getFrom().setPlace(vo.getVehicleProceeding().getFrom().getPlace());
						setCoorectionsLog(correctionsData, "Vehicle proceeding FROM PLACE", "",
								vo.getVehicleProceeding().getFrom().getPlace(), null, null, null, null, userDetails,
								selectedRole, ipAddress);
					} else {
						if (dto.getVehicleProceeding() != null && dto.getVehicleProceeding().getFrom() != null
								&& StringUtils.isNoneBlank(dto.getVehicleProceeding().getFrom().getPlace())
								&& vo.getVehicleProceeding() != null && vo.getVehicleProceeding().getFrom() != null
								&& StringUtils.isNoneBlank(vo.getVehicleProceeding().getFrom().getPlace())
								&& !dto.getVehicleProceeding().getFrom().getPlace()
										.equalsIgnoreCase(vo.getVehicleProceeding().getFrom().getPlace())) {
							setCoorectionsLog(correctionsData, "Vehicle proceeding FROM PLACE",
									dto.getVehicleProceeding().getFrom().getPlace(),
									vo.getVehicleProceeding().getFrom().getPlace(), null, null, null, null, userDetails,
									selectedRole, ipAddress);
							dto.getVehicleProceeding().getFrom()
									.setPlace(vo.getVehicleProceeding().getFrom().getPlace());

						}
					}

					if (dto.getVehicleProceeding() != null && dto.getVehicleProceeding().getTo() != null
							&& StringUtils.isBlank(dto.getVehicleProceeding().getTo().getPlace())
							&& StringUtils.isNoneBlank(vo.getVehicleProceeding().getTo().getPlace())) {
						dto.getVehicleProceeding().getTo().setPlace(vo.getVehicleProceeding().getTo().getPlace());
						setCoorectionsLog(correctionsData, "Vehicle proceeding TO PLACE", "",
								vo.getVehicleProceeding().getTo().getPlace(), null, null, null, null, userDetails,
								selectedRole, ipAddress);
					} else {
						if (dto.getVehicleProceeding() != null && dto.getVehicleProceeding().getTo() != null
								&& StringUtils.isNoneBlank(dto.getVehicleProceeding().getTo().getPlace())
								&& vo.getVehicleProceeding() != null && vo.getVehicleProceeding().getTo() != null
								&& StringUtils.isNoneBlank(vo.getVehicleProceeding().getTo().getPlace())
								&& !dto.getVehicleProceeding().getTo().getPlace()
										.equalsIgnoreCase(vo.getVehicleProceeding().getTo().getPlace())) {
							setCoorectionsLog(correctionsData, "Vehicle proceeding TO PLACE",
									dto.getVehicleProceeding().getTo().getPlace(),
									vo.getVehicleProceeding().getTo().getPlace(), null, null, null, null, userDetails,
									selectedRole, ipAddress);
							dto.getVehicleProceeding().getTo().setPlace(vo.getVehicleProceeding().getTo().getPlace());

						}
					}

					if (dto.getVehicleProceeding() != null && dto.getVehicleProceeding().getFrom() != null
							&& dto.getVehicleProceeding().getFrom().getState() != null
							&& StringUtils.isBlank(dto.getVehicleProceeding().getFrom().getState().getStateName())
							&& StringUtils.isNoneBlank(vo.getVehicleProceeding().getFrom().getState().getStateName())) {
						dto.getVehicleProceeding().getFrom().getState()
								.setStateName(vo.getVehicleProceeding().getFrom().getState().getStateName());
						setCoorectionsLog(correctionsData, "Vehicle proceeding FROM STATE", "",
								vo.getVehicleProceeding().getFrom().getState().getStateName(), null, null, null, null,
								userDetails, selectedRole, ipAddress);
					} else {
						if (dto.getVehicleProceeding() != null && dto.getVehicleProceeding().getFrom() != null
								&& dto.getVehicleProceeding().getFrom().getState() != null
								&& StringUtils
										.isNoneBlank(dto.getVehicleProceeding().getFrom().getState().getStateName())
								&& vo.getVehicleProceeding() != null && vo.getVehicleProceeding().getFrom() != null
								&& vo.getVehicleProceeding().getFrom().getState() != null
								&& StringUtils
										.isNoneBlank(vo.getVehicleProceeding().getFrom().getState().getStateName())
								&& !dto.getVehicleProceeding().getFrom().getState().getStateName().equalsIgnoreCase(
										vo.getVehicleProceeding().getFrom().getState().getStateName())) {
							setCoorectionsLog(correctionsData, "Vehicle proceeding FROM STATE",
									dto.getVehicleProceeding().getFrom().getState().getStateName(),
									vo.getVehicleProceeding().getFrom().getState().getStateName(), null, null, null,
									null, userDetails, selectedRole, ipAddress);
							dto.getVehicleProceeding().getFrom().getState()
									.setStateName(vo.getVehicleProceeding().getFrom().getState().getStateName());

						}
					}

					if (dto.getVehicleProceeding() != null && dto.getVehicleProceeding().getTo() != null
							&& dto.getVehicleProceeding().getTo().getState() != null
							&& StringUtils.isBlank(dto.getVehicleProceeding().getTo().getState().getStateName())
							&& StringUtils.isNoneBlank(vo.getVehicleProceeding().getTo().getState().getStateName())) {
						dto.getVehicleProceeding().getTo().getState()
								.setStateName(vo.getVehicleProceeding().getTo().getState().getStateName());
						setCoorectionsLog(correctionsData, "Vehicle proceeding TO STATE", "",
								vo.getVehicleProceeding().getTo().getState().getStateName(), null, null, null, null,
								userDetails, selectedRole, ipAddress);
					} else {
						if (dto.getVehicleProceeding() != null && dto.getVehicleProceeding().getTo() != null
								&& dto.getVehicleProceeding().getTo().getState() != null
								&& StringUtils.isNoneBlank(dto.getVehicleProceeding().getTo().getState().getStateName())
								&& vo.getVehicleProceeding() != null && vo.getVehicleProceeding().getTo() != null
								&& vo.getVehicleProceeding().getTo().getState() != null
								&& StringUtils.isNoneBlank(vo.getVehicleProceeding().getTo().getState().getStateName())
								&& !dto.getVehicleProceeding().getTo().getState().getStateName().equalsIgnoreCase(
										vo.getVehicleProceeding().getTo().getState().getStateName())) {
							setCoorectionsLog(correctionsData, "Vehicle proceeding TO STATE",
									dto.getVehicleProceeding().getTo().getState().getStateName(),
									vo.getVehicleProceeding().getTo().getState().getStateName(), null, null, null, null,
									userDetails, selectedRole, ipAddress);
							dto.getVehicleProceeding().getTo().getState()
									.setStateName(vo.getVehicleProceeding().getTo().getState().getStateName());

						}
					}
					if (dto.getRegistration().getInvoiceAmmount() == null
							&& vo.getRegistration().getInvoiceAmmount() != null) {
						dto.getRegistration().setInvoiceAmmount(vo.getRegistration().getInvoiceAmmount());
						setCoorectionsLog(correctionsData, "InvoiceAmmount", "", "", null,
								vo.getRegistration().getInvoiceAmmount(), null, null, userDetails, selectedRole,
								ipAddress);
					} else {
						if (dto.getRegistration().getInvoiceAmmount() != null
								&& vo.getRegistration().getInvoiceAmmount() != null && (!dto.getRegistration()
										.getInvoiceAmmount().equals(vo.getRegistration().getInvoiceAmmount()))) {
							setCoorectionsLog(correctionsData, "InvoiceAmmount", "", "",
									dto.getRegistration().getInvoiceAmmount(), vo.getRegistration().getInvoiceAmmount(),
									null, null, userDetails, selectedRole, ipAddress);
							dto.getRegistration().setInvoiceAmmount(vo.getRegistration().getInvoiceAmmount());

						}
					}
					if (dto.getRegistration().getClasssOfVehicle() != null
							&& dto.getRegistration().getClasssOfVehicle().getCovcode() != null
							&& vo.getRegistration().getClasssOfVehicle() != null
							&& vo.getRegistration().getClasssOfVehicle().getCovcode() != null
							&& !dto.getRegistration().getClasssOfVehicle().getCovcode()
									.equalsIgnoreCase(vo.getRegistration().getClasssOfVehicle().getCovcode())) {
						MasterCovDTO masterCov = masterCovDAO
								.findByCovcode(vo.getRegistration().getClasssOfVehicle().getCovcode());
						if (masterCov == null) {
							logger.error("no master data for class of vehicle ",
									vo.getRegistration().getClasssOfVehicle().getCovcode());
							throw new BadRequestException("No master data for class of vehicle "
									+ vo.getRegistration().getClasssOfVehicle().getCovcode());
						}
						setCoorectionsLog(correctionsData, "COV",
								dto.getRegistration().getClasssOfVehicle().getCovcode(),
								vo.getRegistration().getClasssOfVehicle().getCovcode(), null, null, null, null,
								userDetails, selectedRole, ipAddress);
						dto.getRegistration().setClasssOfVehicle(masterCov);

					}
					if (StringUtils.isBlank(dto.getRegistration().getFuelDesc())
							&& StringUtils.isNoneBlank(vo.getRegistration().getFuelDesc())) {
						dto.getRegistration().setFuelDesc(vo.getRegistration().getFuelDesc());
						setCoorectionsLog(correctionsData, "FuelDesc", "", vo.getRegistration().getFuelDesc(), null,
								null, null, null, userDetails, selectedRole, ipAddress);
					} else {
						if (StringUtils.isNoneBlank(dto.getRegistration().getFuelDesc())
								&& StringUtils.isNoneBlank(vo.getRegistration().getFuelDesc()) && !vo.getRegistration()
										.getFuelDesc().equalsIgnoreCase(dto.getRegistration().getFuelDesc())) {
							setCoorectionsLog(correctionsData, "FuelDesc", dto.getRegistration().getFuelDesc(),
									vo.getRegistration().getFuelDesc(), null, null, null, null, userDetails,
									selectedRole, ipAddress);
							dto.getRegistration().setFuelDesc(vo.getRegistration().getFuelDesc());

						}
					}
					if (dto.getRegistration().getOwnerType() == null && vo.getRegistration().getOwnerType() != null) {
						dto.getRegistration().setOwnerType(vo.getRegistration().getOwnerType());
						setCoorectionsLog(correctionsData, "OwnerType", "",
								vo.getRegistration().getOwnerType().getCode(), null, null, null, null, userDetails,
								selectedRole, ipAddress);
					} else {
						if (dto.getRegistration().getOwnerType() != null && vo.getRegistration().getOwnerType() != null
								&& (!dto.getRegistration().getOwnerType()
										.equals(vo.getRegistration().getOwnerType()))) {
							setCoorectionsLog(correctionsData, "OwnerType",
									dto.getRegistration().getOwnerType().getCode(),
									vo.getRegistration().getOwnerType().getCode(), null, null, null, null, userDetails,
									selectedRole, ipAddress);
							dto.getRegistration().setOwnerType(vo.getRegistration().getOwnerType());

						}
					}
					if (dto.getRegistration().getTaxCalculationDateForLifeTax() == null
							&& vo.getRegistration().getTaxCalculationDateForLifeTax() != null) {
						dto.getRegistration().setTaxCalculationDateForLifeTax(
								vo.getRegistration().getTaxCalculationDateForLifeTax());
						setCoorectionsLog(correctionsData, "LifeTaxCalDate", "", null, null, null, null,
								vo.getRegistration().getTaxCalculationDateForLifeTax(), userDetails, selectedRole,
								ipAddress);
					} else {
						if (dto.getRegistration().getTaxCalculationDateForLifeTax() != null
								&& vo.getRegistration().getTaxCalculationDateForLifeTax() != null
								&& (!dto.getRegistration().getTaxCalculationDateForLifeTax()
										.equals(vo.getRegistration().getTaxCalculationDateForLifeTax()))) {
							setCoorectionsLog(correctionsData, "LifeTaxCalDate", null, null, null, null,
									dto.getRegistration().getTaxCalculationDateForLifeTax(),
									vo.getRegistration().getTaxCalculationDateForLifeTax(), userDetails, selectedRole,
									ipAddress);
							dto.getRegistration().setTaxCalculationDateForLifeTax(
									vo.getRegistration().getTaxCalculationDateForLifeTax());

						}
					}
					if (dto.getRegistration().getPrGeneratedDate() == null
							&& vo.getRegistration().getPrGeneratedDate() != null) {
						dto.getRegistration().setPrGeneratedDate(vo.getRegistration().getPrGeneratedDate());
						setCoorectionsLog(correctionsData, "PRGenerationDate", "", null, null, null, null,
								vo.getRegistration().getPrGeneratedDate(), userDetails, selectedRole, ipAddress);
					} else {
						if (dto.getRegistration().getPrGeneratedDate() != null
								&& vo.getRegistration().getPrGeneratedDate() != null && (!dto.getRegistration()
										.getPrGeneratedDate().equals(vo.getRegistration().getPrGeneratedDate()))) {
							setCoorectionsLog(correctionsData, "PRGenerationDate", null, null, null, null,
									dto.getRegistration().getPrGeneratedDate(),
									vo.getRegistration().getPrGeneratedDate(), userDetails, selectedRole, ipAddress);
							dto.getRegistration().setPrGeneratedDate(vo.getRegistration().getPrGeneratedDate());

						}
					}
					if (dto.getRegistration().getTrGeneratedDate() == null
							&& vo.getRegistration().getTrGeneratedDate() != null) {
						dto.getRegistration().setTrGeneratedDate(vo.getRegistration().getTrGeneratedDate());
						setCoorectionsLog(correctionsData, "TRGenerationDate", "", null, null, null, null,
								vo.getRegistration().getTrGeneratedDate(), userDetails, selectedRole, ipAddress);
					} else {
						if (dto.getRegistration().getTrGeneratedDate() != null
								&& vo.getRegistration().getTrGeneratedDate() != null && (!dto.getRegistration()
										.getTrGeneratedDate().equals(vo.getRegistration().getTrGeneratedDate()))) {
							setCoorectionsLog(correctionsData, "TRGenerationDate", null, null, null, null,
									dto.getRegistration().getTrGeneratedDate(),
									vo.getRegistration().getTrGeneratedDate(), userDetails, selectedRole, ipAddress);
							dto.getRegistration().setTrGeneratedDate(vo.getRegistration().getTrGeneratedDate());

						}
					}
					if (StringUtils.isBlank(dto.getRegistration().getMakersModel())
							&& StringUtils.isNoneBlank(vo.getRegistration().getMakersModel())) {
						dto.getRegistration().setMakersModel(vo.getRegistration().getMakersModel());
						setCoorectionsLog(correctionsData, "MakersModel", "", vo.getRegistration().getMakersModel(),
								null, null, null, null, userDetails, selectedRole, ipAddress);
					} else {
						if (StringUtils.isNoneBlank(dto.getRegistration().getMakersModel())
								&& StringUtils.isNoneBlank(vo.getRegistration().getMakersModel())
								&& !vo.getRegistration().getMakersModel()
										.equalsIgnoreCase(dto.getRegistration().getMakersModel())) {
							setCoorectionsLog(correctionsData, "MakersModel", dto.getRegistration().getMakersModel(),
									vo.getRegistration().getMakersModel(), null, null, null, null, userDetails,
									selectedRole, ipAddress);
							dto.getRegistration().setMakersModel(vo.getRegistration().getMakersModel());

						}
					}
					if (dto.getRegistration().getFirstVehicle() == null && vo.getRegistration().isFirstVehicle()) {
						dto.getRegistration().setFirstVehicle(vo.getRegistration().isFirstVehicle());
						setCoorectionsLog(correctionsData, "FirstVehicle", "", Boolean.TRUE.toString(), null, null,
								null, null, userDetails, selectedRole, ipAddress);
					} else {
						if (dto.getRegistration().getFirstVehicle() != null && (!dto.getRegistration().getFirstVehicle()
								.equals(vo.getRegistration().isFirstVehicle()))) {
							setCoorectionsLog(correctionsData, "FirstVehicle",
									dto.getRegistration().getFirstVehicle().toString(),
									Boolean.toString(vo.getRegistration().isFirstVehicle()), null, null, null, null,
									userDetails, selectedRole, ipAddress);
							dto.getRegistration().setFirstVehicle(vo.getRegistration().isFirstVehicle());

						}
					}

					if (dto.getSeizedAndDocumentImpounded() != null
							&& dto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO() == null
							&& vo.getSeizedAndDocumentImpounded() != null
							&& vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO() != null) {
						this.corectionForSeizedDoc(vo, dto, correctionsData, userDetails, selectedRole, ipAddress);
					} else if (dto.getSeizedAndDocumentImpounded() != null
							&& dto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO() != null
							&& vo.getSeizedAndDocumentImpounded() != null
							&& vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO() != null) {
						if (StringUtils
								.isBlank(dto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().getVehicleKeptAt())
								&& StringUtils.isNoneBlank(
										vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getVehicleKeptAt())) {
							dto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().setVehicleKeptAt(
									vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getVehicleKeptAt());
							setCoorectionsLog(correctionsData, "VehicleKeptAt", "",
									vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getVehicleKeptAt(), null,
									null, null, null, userDetails, selectedRole, ipAddress);
						} else {
							if (StringUtils.isNoneBlank(
									dto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().getVehicleKeptAt())
									&& StringUtils.isNoneBlank(
											vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getVehicleKeptAt())
									&& !vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getVehicleKeptAt()
											.equalsIgnoreCase(dto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO()
													.getVehicleKeptAt())) {
								setCoorectionsLog(correctionsData, "VehicleKeptAt",
										dto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().getVehicleKeptAt(),
										vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getVehicleKeptAt(),
										null, null, null, null, userDetails, selectedRole, ipAddress);
								dto.getRegistration().setFuelDesc(
										vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getVehicleKeptAt());

							}
						}
						if (dto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().getDateOfSeized() == null
								&& vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getDateOfSeized() != null) {
							dto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().setDateOfSeized(
									vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getDateOfSeized());
							setCoorectionsLog(correctionsData, "DateOfSeized", "", null, null, null, null,
									vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getDateOfSeized(),
									userDetails, selectedRole, ipAddress);
						} else {
							if (dto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().getDateOfSeized() != null
									&& vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getDateOfSeized() != null
									&& !vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getDateOfSeized()
											.equals(dto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO()
													.getDateOfSeized())) {
								setCoorectionsLog(correctionsData, "DateOfSeized", null, null, null, null,
										dto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().getDateOfSeized(),
										vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getDateOfSeized(),
										userDetails, selectedRole, ipAddress);
								dto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().setDateOfSeized(
										vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getDateOfSeized());

							}
						}
					} else if (dto.getSeizedAndDocumentImpounded() == null && vo.getSeizedAndDocumentImpounded() != null
							&& vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO() != null) {
						SeizedAndDocumentImpoundedDTO sezideDoc = new SeizedAndDocumentImpoundedDTO();
						dto.setSeizedAndDocumentImpounded(sezideDoc);
						this.corectionForSeizedDoc(vo, dto, correctionsData, userDetails, selectedRole, ipAddress);
					}
					if (StringUtils.isBlank(dto.getPilledCov()) && StringUtils.isNoneBlank(vo.getPilledCov())) {
						dto.setPilledCov(vo.getPilledCov());
						setCoorectionsLog(correctionsData, "PilledCov", "", vo.getPilledCov(), null, null, null, null,
								userDetails, selectedRole, ipAddress);
					} else {
						if (StringUtils.isNoneBlank(dto.getPilledCov()) && StringUtils.isNoneBlank(vo.getPilledCov())
								&& !vo.getPilledCov().equalsIgnoreCase(dto.getPilledCov())) {
							setCoorectionsLog(correctionsData, "PilledCov", dto.getPilledCov(), vo.getPilledCov(), null,
									null, null, null, userDetails, selectedRole, ipAddress);
							dto.setPilledCov(vo.getPilledCov());

						}
					}
					if (StringUtils.isBlank(dto.getPilledPermit()) && StringUtils.isNoneBlank(vo.getPilledPermit())) {
						dto.setPilledPermit(vo.getPilledPermit());
						setCoorectionsLog(correctionsData, "PilledPermit", "", vo.getPilledPermit(), null, null, null,
								null, userDetails, selectedRole, ipAddress);
					} else {
						if (StringUtils.isNoneBlank(dto.getPilledPermit())
								&& StringUtils.isNoneBlank(vo.getPilledPermit())
								&& !vo.getPilledPermit().equalsIgnoreCase(dto.getPilledPermit())) {
							setCoorectionsLog(correctionsData, "PilledPermit", dto.getPilledPermit(),
									vo.getPilledPermit(), null, null, null, null, userDetails, selectedRole, ipAddress);
							dto.setPilledPermit(vo.getPilledPermit());

						}
					}
					if (StringUtils.isBlank(dto.getPilledRouteCode())
							&& StringUtils.isNoneBlank(vo.getPilledRouteCode())) {
						dto.setPilledRouteCode(vo.getPilledRouteCode());
						setCoorectionsLog(correctionsData, "PilledRouteCode", "", vo.getPilledRouteCode(), null, null,
								null, null, userDetails, selectedRole, ipAddress);
					} else {
						if (StringUtils.isNoneBlank(dto.getPilledRouteCode())
								&& StringUtils.isNoneBlank(vo.getPilledRouteCode())
								&& !vo.getPilledRouteCode().equalsIgnoreCase(dto.getPilledRouteCode())) {
							setCoorectionsLog(correctionsData, "PilledRouteCode", dto.getPilledRouteCode(),
									vo.getPilledRouteCode(), null, null, null, null, userDetails, selectedRole,
									ipAddress);
							dto.setPilledRouteCode(vo.getPilledRouteCode());

						}
					}
					if (dto.getPilledSeatings() == null && vo.getPilledSeatings() != null) {
						dto.setPilledSeatings(vo.getPilledSeatings());
						setCoorectionsLog(correctionsData, "PilledSeatings", "", "", null, vo.getPilledSeatings(), null,
								null, userDetails, selectedRole, ipAddress);
					} else {
						if (dto.getPilledSeatings() != null && vo.getPilledSeatings() != null
								&& !vo.getPilledSeatings().equals(dto.getPilledSeatings())) {
							setCoorectionsLog(correctionsData, "PilledSeatings", "", "", dto.getPilledSeatings(),
									vo.getPilledSeatings(), null, null, userDetails, selectedRole, ipAddress);
							dto.setPilledSeatings(vo.getPilledSeatings());

						}
					}
					if (dto.getRegistration().getNocIssued() == null && vo.getRegistration().isNocIssued()) {
						dto.getRegistration().setNocIssued(vo.getRegistration().isNocIssued());
						setCoorectionsLog(correctionsData, "NocIssued", "",
								String.valueOf(vo.getRegistration().isNocIssued()), null, null, null, null, userDetails,
								selectedRole, ipAddress);
					} else {
						if (dto.getRegistration().getNocIssued() != null && vo.getRegistration().isNocIssued()
								&& (!dto.getRegistration().getNocIssued().equals(vo.getRegistration().isNocIssued()))) {
							setCoorectionsLog(correctionsData, "NocIssued",
									dto.getRegistration().getNocIssued().toString(),
									String.valueOf(vo.getRegistration().isNocIssued()), null, null, null, null,
									userDetails, selectedRole, ipAddress);
							dto.getRegistration().setNocIssued(vo.getRegistration().isNocIssued());

						}
					}
					if (dto.getRegistration().getTaxCalculationDateForQuarterlyTax() == null
							&& vo.getRegistration().getTaxCalculationDateForQuarterlyTax() != null) {
						dto.getRegistration().setTaxCalculationDateForQuarterlyTax(
								vo.getRegistration().getTaxCalculationDateForQuarterlyTax());
						setCoorectionsLog(correctionsData, "QuarterlyTaxCalDate", "", null, null, null, null,
								vo.getRegistration().getTaxCalculationDateForQuarterlyTax(), userDetails, selectedRole,
								ipAddress);
					} else {
						if (dto.getRegistration().getTaxCalculationDateForQuarterlyTax() != null
								&& vo.getRegistration().getTaxCalculationDateForQuarterlyTax() != null
								&& (!dto.getRegistration().getTaxCalculationDateForLifeTax()
										.equals(vo.getRegistration().getTaxCalculationDateForQuarterlyTax()))) {
							setCoorectionsLog(correctionsData, "QuarterlyTaxCalDate", null, null, null, null,
									dto.getRegistration().getTaxCalculationDateForLifeTax(),
									vo.getRegistration().getTaxCalculationDateForQuarterlyTax(), userDetails,
									selectedRole, ipAddress);
							dto.getRegistration().setTaxCalculationDateForQuarterlyTax(
									vo.getRegistration().getTaxCalculationDateForQuarterlyTax());

						}
					}
					if (!dto.isAnnualTax() && vo.isAnnualTax()) {
						dto.setAnnualTax(vo.isAnnualTax());
						setCoorectionsLog(correctionsData, "AnnualTax", Boolean.FALSE.toString(),
								Boolean.TRUE.toString(), null, null, null, null, userDetails, selectedRole, ipAddress);
					} else {
						if (dto.isAnnualTax() && !vo.isAnnualTax()) {
							setCoorectionsLog(correctionsData, "AnnualTax", Boolean.TRUE.toString(),
									Boolean.FALSE.toString(), null, null, null, null, userDetails, selectedRole,
									ipAddress);
							dto.setAnnualTax(vo.isAnnualTax());

						}
					}
					if (vo.getRegistration().isOtherState() && !dto.getRegistration().isOtherState()) {
						if (StringUtils.isNoneBlank(dto.getRegistration().getRegApplicationNo())) {
							logger.error("Vehicle is belong to AP state only ", vo.getRegistration().getRegNo());
							throw new BadRequestException(
									"Vehicle is belong to AP state only " + vo.getRegistration().getRegNo());
						}
						dto.getRegistration().setOtherState(vo.getRegistration().isOtherState());
						setCoorectionsLog(correctionsData, "Changed to other Sate", "", "", null, null, null, null,
								userDetails, selectedRole, ipAddress);
					}
					setActionsForOtherStateCorrections(dto, "OTHER STATE CORRECTIONS", userDetails, selectedRole,
							ipAddress, null, null, correctionsData);
				}
			}
		}
	}

	private void setActionsForOtherStateCorrections(VcrFinalServiceDTO vcrDto, String module, UserDTO userDetails,
			String selectedRole, String ipAddress, String from, String to, List<VcrCorrectionLogDTO> correctionsData) {
		if (CollectionUtils.isNotEmpty(correctionsData)) {
			ActionDetails actions = new ActionDetails();
			actions.setRole(selectedRole);
			actions.setUserId(userDetails.getUserId());
			actions.setModule(module);
			actions.setCreatedDate(LocalDateTime.now());
			actions.setAadharNo(userDetails.getAadharNo());
			actions.setIpAddress(ipAddress);

			actions.setCoorectionLog(correctionsData);

			if (StringUtils.isNoneBlank(from) && StringUtils.isNoneBlank(to)) {
				actions.setFrom(from);
				actions.setTo(to);
			}
			if (vcrDto.getActions() == null || vcrDto.getActions().isEmpty()) {
				vcrDto.setActions(Arrays.asList(actions));
			} else {
				List<ActionDetails> actionist = new ArrayList<ActionDetails>();
				actionist.addAll(vcrDto.getActions());
				actionist.add(actions);
				vcrDto.setActions(actionist);
			}
		}
	}

	public AadhaarSourceDTO setAadhaarSourceDetails(CitizenSearchReportVO vo, UserDTO userDetails) {
		AadhaarSourceDTO aadhaarSourceDTO = new AadhaarSourceDTO();
		aadhaarSourceDTO.setServiceType(vo.getServiceType());
		aadhaarSourceDTO.setApplicationNo(vo.getApplicationNumber());
		aadhaarSourceDTO.setPrNo(vo.getPrNo());
		aadhaarSourceDTO.setUser(userDetails.getUserId());
		return aadhaarSourceDTO;
	}

	private void saveCourtOrder(List<VcrFinalServiceDTO> vcrList, CitizenSearchReportVO vo, String chassiNo,
			UserDTO userDetails, String ipAddress) {
		if (StringUtils.isBlank(vo.getOrderNo())) {
			logger.error("Please give order number: " + chassiNo);
			throw new BadRequestException("Please give order number: " + chassiNo);
		}
		if (vo.getOrderDate() == null) {
			logger.error("Please give order date: " + chassiNo);
			throw new BadRequestException("Please give order date: " + chassiNo);
		}
		if (StringUtils.isBlank(vo.getType())) {
			logger.error("Please select only one,PartialDisposal or FinalDisposal: " + chassiNo);
			throw new BadRequestException("Please select only one,PartialDisposal or FinalDisposal: " + chassiNo);
		}
		if (vo.getFiles() == null || vo.getFiles().isEmpty()) {
			logger.error("Please upload Court order");
			throw new BadRequestException("Please upload Court order");
		}

		vcrList.forEach(single -> {
			if (vo.getType().equalsIgnoreCase("partialDisposal")) {
				single.setPartialDisposal(Boolean.TRUE);
			} else {
				single.setFinalDisposal(Boolean.TRUE);
			}
			single.setOrderNo(vo.getOrderNo());
			single.setOrderDate(vo.getOrderDate());

		});
		if (vo.getType().equalsIgnoreCase("partialDisposal")) {
			/*
			 * for (VcrFinalServiceVO vcrVo : vo.getVcrList()) { if
			 * (vcrVo.isPartialDisposal()) { for (VcrFinalServiceDTO vcrDto : vcrList) { if
			 * (vcrDto.getVcr().getVcrNumber().equalsIgnoreCase(vcrVo.getVcr().getVcrNumber(
			 * ))) { vcrDto.setIsVcrClosed(Boolean.TRUE);
			 * vcrDto.setlUpdate(LocalDateTime.now());
			 * vcrDto.setPaidDate(LocalDateTime.now()); setActions(vcrDto,
			 * "partialDisposal", userDetails, vo.getSelectedRole(), ipAddress,null,null);
			 * break; } } }
			 * 
			 * }
			 */
			for (VcrFinalServiceVO vcrVo : vo.getVcrList()) {
				for (OffenceVO offenceVO : vcrVo.getOffence().getOffence()) {
					if (offenceVO.getIsOffenceClosed()) {
						for (VcrFinalServiceDTO vcrDto : vcrList) {
							if (vcrDto.getVcr().getVcrNumber().equalsIgnoreCase(vcrVo.getVcr().getVcrNumber())) {
								this.saveImagesForVCr(vcrDto, vo.getFiles(), EnclosureType.COURTORDERCOPY.getValue());
								for (OffenceDTO offenceDTO : vcrDto.getOffence().getOffence()) {
									if (offenceVO.getSlno().equals(offenceDTO.getSlno())) {
										offenceDTO.setIsOffenceClosed(Boolean.TRUE);
										String fixedAmount = "0";
										if (offenceDTO.getFixedAmount() != null) {
											fixedAmount = offenceDTO.getFixedAmount().toString();
										}
										if (offenceVO.getFixedAmount() == null) {
											logger.error("Please provide offence amount: " + chassiNo);
											throw new BadRequestException("Please provide offence amount: " + chassiNo);
										}
										setActionsForOffences(offenceDTO, offenceDTO.getOffenceDescription(),
												userDetails, vo.getSelectedRole(), ipAddress, fixedAmount,
												offenceVO.getFixedAmount().toString());
										// offenceDTO.setAmount1(0);
										// offenceDTO.setAmount2(0);
										offenceDTO.setFixedAmount(offenceVO.getFixedAmount());
										// offenceDTO.setPerPerson(0);
										// offenceDTO.setPerkg("0");
										offenceDTO.setOffencePaid(Boolean.TRUE);
										offenceDTO.setOffenceClosedDate(LocalDateTime.now());

									}
								}
							}
						}
					}
				}
			}
		} else if (vo.getType().equalsIgnoreCase("finalDisposal")) {
			vcrList.forEach(single -> {
				this.saveImagesForVCr(single, vo.getFiles(), EnclosureType.COURTORDERCOPY.getValue());
				single.setIsVcrClosed(Boolean.TRUE);
				single.setlUpdate(LocalDateTime.now());
				single.setPaidDate(LocalDateTime.now());
				setActions(single, "finalDisposal", userDetails, vo.getSelectedRole(), ipAddress, null, null);
			});
		} else {
			logger.error("Please select PartialDisposal or FinalDisposal: " + chassiNo);
			throw new BadRequestException("Please select PartialDisposal or FinalDisposal: " + chassiNo);
		}
	}

	private void saveProsection(List<VcrFinalServiceDTO> vcrList, CitizenSearchReportVO vo, String chassiNo,
			UserDTO userDetails, String ipaddress) {

		if (StringUtils.isBlank(vo.getCourtName())) {
			logger.error("Please give court name: " + chassiNo);
			throw new BadRequestException("Please give order name: " + chassiNo);
		}
		if (StringUtils.isBlank(vo.getCaseNo())) {
			logger.error("Please give case number: " + chassiNo);
			throw new BadRequestException("Please give case number: " + chassiNo);
		}
		if (vo.getOrderDate() == null) {
			logger.error("Please give order date: " + chassiNo);
			throw new BadRequestException("Please give order date: " + chassiNo);
		}
		if (vo.getCollectedDate() == null) {
			logger.error("Please give collected date: " + chassiNo);
			throw new BadRequestException("Please give collected date: " + chassiNo);
		}
		if (vo.getFineCollected() == null) {
			logger.error("Please give fine collected: " + chassiNo);
			throw new BadRequestException("Please give fine collected: " + chassiNo);
		}

		vcrList.forEach(single -> {
			checkvcrClosedOrNot(vcrList, single);
			single.setCourtName(vo.getCourtName());
			single.setCaseNo(vo.getCaseNo());
			single.setOrderDate(vo.getOrderDate());
			single.setCollectedDate(vo.getCollectedDate());
			single.setFineCollected(vo.getFineCollected());
			single.setIsVcrClosed(Boolean.TRUE);
			single.setlUpdate(LocalDateTime.now());
			single.setPaidDate(LocalDateTime.now());
			setActions(single, "Prosection", userDetails, vo.getSelectedRole(), ipaddress, null, null);
		});
	}

	private void saveDepartmentAuction(List<VcrFinalServiceDTO> vcrList, CitizenSearchReportVO vo, String chassiNo,
			UserDTO userDetails, String ipaddress) {

		if (StringUtils.isBlank(vo.getProceedingNo())) {
			logger.error("Please give Proceeding number: " + chassiNo);
			throw new BadRequestException("Please give Proceeding number: " + chassiNo);
		}

		if (vo.getProceedingDate() == null) {
			logger.error("Please give Proceeding date: " + chassiNo);
			throw new BadRequestException("Please give Proceeding date: " + chassiNo);
		}

		vcrList.forEach(single -> {
			// checkvcrClosedOrNot(single);
			single.setProceedingNo(vo.getProceedingNo());
			single.setProceedingDate(vo.getProceedingDate());
			single.setIsVcrClosed(Boolean.TRUE);
			single.setlUpdate(LocalDateTime.now());
			single.setPaidDate(LocalDateTime.now());
			setActions(single, "DepartmentAuction", userDetails, vo.getSelectedRole(), ipaddress, null, null);
		});
	}

	private void saveSizedReleaseType(List<VcrFinalServiceDTO> vcrList, CitizenSearchReportVO vo, String chassiNo,
			UserDTO userDetails, String ipaddress) {

		for (VcrFinalServiceVO vcrVo : vo.getVcrList()) {
			if (vcrVo.getSeizedAndDocumentImpounded() != null
					&& vcrVo.getSeizedAndDocumentImpounded().getVehicleSeizedVO() != null
					&& vcrVo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getDateOfSeized() != null) {
				for (VcrFinalServiceDTO vcrDto : vcrList) {
					if (vcrDto.getVcr().getVcrNumber().equalsIgnoreCase(vcrVo.getVcr().getVcrNumber())) {
						if (vcrDto.getSeizedAndDocumentImpounded() != null
								&& vcrDto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO() != null
								&& vcrDto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO()
										.getDateOfSeized() != null) {
							vcrDto.setlUpdate(LocalDateTime.now());
							String from;
							String to;
							if (vcrDto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().isReleaseOrder()) {
								from = "ReleaseOrder";
								vcrDto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO()
										.setReleaseOrder(Boolean.FALSE);
								if (vcrVo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().isCourtOrder()) {
									to = "CourtOrder";
									vcrDto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO()
											.setCourtOrder(Boolean.TRUE);
								} else if (vcrVo.getSeizedAndDocumentImpounded().getVehicleSeizedVO()
										.isDepartmentAction()) {
									to = "DepartmentAction";
									vcrDto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO()
											.setDepartmentAction(Boolean.TRUE);
								} else {
									logger.error("Please select sized vehicle release type: "
											+ vcrDto.getVcr().getVcrNumber());
									throw new BadRequestException("Please select sized vehicle release type: "
											+ vcrDto.getVcr().getVcrNumber());
								}
							} else if (vcrDto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().isCourtOrder()) {
								vcrDto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO()
										.setCourtOrder(Boolean.FALSE);
								from = "CourtOrder";
								if (vcrVo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().isReleaseOrder()) {
									to = "ReleaseOrder";
									vcrDto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO()
											.setReleaseOrder(Boolean.TRUE);
								} else if (vcrVo.getSeizedAndDocumentImpounded().getVehicleSeizedVO()
										.isDepartmentAction()) {
									to = "DepartmentAction";
									vcrDto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO()
											.setDepartmentAction(Boolean.TRUE);
								} else {
									logger.error("Please select sized vehicle release type: "
											+ vcrDto.getVcr().getVcrNumber());
									throw new BadRequestException("Please select sized vehicle release type: "
											+ vcrDto.getVcr().getVcrNumber());
								}
							} else if (vcrDto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO()
									.isDepartmentAction()) {
								vcrDto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO()
										.setDepartmentAction(Boolean.FALSE);
								from = "DepartmentAction";
								if (vcrVo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().isReleaseOrder()) {
									to = "ReleaseOrder";
									vcrDto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO()
											.setReleaseOrder(Boolean.TRUE);
								} else if (vcrVo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().isCourtOrder()) {
									to = "CourtOrder";
									vcrDto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO()
											.setCourtOrder(Boolean.TRUE);
								} else {
									logger.error("Please select sized vehicle release type: "
											+ vcrDto.getVcr().getVcrNumber());
									throw new BadRequestException("Please select sized vehicle release type: "
											+ vcrDto.getVcr().getVcrNumber());
								}
							} else {
								from = "None";
								if (vcrVo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().isReleaseOrder()) {
									to = "ReleaseOrder";
									vcrDto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO()
											.setReleaseOrder(Boolean.TRUE);
								} else if (vcrVo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().isCourtOrder()) {
									to = "CourtOrder";
									vcrDto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO()
											.setCourtOrder(Boolean.TRUE);
								} else if (vcrVo.getSeizedAndDocumentImpounded().getVehicleSeizedVO()
										.isDepartmentAction()) {
									to = "DepartmentAction";
									vcrDto.getSeizedAndDocumentImpounded().getVehicleSeizedDTO()
											.setDepartmentAction(Boolean.TRUE);
								} else {
									logger.error("Please select sized vehicle release type: "
											+ vcrDto.getVcr().getVcrNumber());
									throw new BadRequestException("Please select sized vehicle release type: "
											+ vcrDto.getVcr().getVcrNumber());
								}
							}
							setActions(vcrDto, "SeizedType", userDetails, vo.getSelectedRole(), ipaddress, from, to);
							break;
						}
					}
				}
			}

		}
	}

	private void setActions(VcrFinalServiceDTO vcrDto, String module, UserDTO userDetails, String selectedRole,
			String ipAddress, String from, String to) {
		ActionDetails actions = new ActionDetails();
		actions.setRole(selectedRole);
		actions.setUserId(userDetails.getUserId());
		actions.setModule(module);
		actions.setCreatedDate(LocalDateTime.now());
		actions.setAadharNo(userDetails.getAadharNo());
		actions.setIpAddress(ipAddress);
		if (StringUtils.isNoneBlank(from) && StringUtils.isNoneBlank(to)) {
			actions.setFrom(from);
			actions.setTo(to);
		}
		if (vcrDto.getActions() == null || vcrDto.getActions().isEmpty()) {
			vcrDto.setActions(Arrays.asList(actions));
		} else {
			List<ActionDetails> actionist = new ArrayList<ActionDetails>();
			actionist.addAll(vcrDto.getActions());
			actionist.add(actions);
			vcrDto.setActions(actionist);
		}
	}

	@Override
	public Optional<RTADashboardVO> getCitizenDashBoardMenuDetails(String officeCode, String user, String role) {
		if (!RoleEnum.getOfficersOnly().contains(role)) {
			logger.error("Role not available to display dash board");
			throw new BadRequestException("Role not available to display dash board");
		}
		Optional<RTADashboardDTO> rtaDashBoard = rtadbDAO.findByRole(RoleEnum.getRoleEnumByName(role));
		if (!rtaDashBoard.isPresent()) {
			throw new BadRequestException("Dash Board not available to display");
		}
		RTADashboardDTO rtaDashBoardDTO1 = null;
		List<RTADashBoardLinksDTO> newRegCounts = new ArrayList<>();
		// Dynamic Dashboard Creation.
		Integer nonTransport = 0;
		Integer transport = 0;
		Integer financierCreatePendingCount = 0;
		List<StagingRegistrationDetailsDTO> list = getRoleOfficeCodeAndStatusBasedRecords(role, officeCode);

		if (!list.isEmpty()) {
			nonTransport = list.stream().filter(val -> val.getVehicleType().equals(CovCategory.N.getCode()))
					.collect(Collectors.toList()).size();
			transport = list.stream().filter(val -> val.getVehicleType().equals(CovCategory.T.getCode()))
					.collect(Collectors.toList()).size();
			// total = list.size();
		} else {
			logger.info("No Pending Records Found  OfficeCode: {},role: {} and user :{}", officeCode, role, user);
		}
		for (RTADashBoardLinksDTO rtaDashBoardLinksDTO : rtaDashBoard.get().getRegistartionServices()) {
			if (rtaDashBoardLinksDTO.getModuleTitle().equals(DynamicmenusEnum.NEWREG.getDescription())) {
				for (RTADashBoardServiceNames rtaDashBoardServiceNames : rtaDashBoardLinksDTO.getServiceNames()) {
					if (rtaDashBoardServiceNames.getServiceName().equals(DynamicmenusEnum.TPENDING.getDescription())) {
						rtaDashBoardServiceNames.setCountValue(transport);

					}
					if (rtaDashBoardServiceNames.getServiceName().equals(DynamicmenusEnum.NTPENDING.getDescription())) {
						rtaDashBoardServiceNames.setCountValue(nonTransport);
					}
				}

			}
			if (rtaDashBoardLinksDTO.getModuleTitle().equals(DynamicmenusEnum.NEWREGFINANCIER.getDescription())) {
				// Financier Applications Count in CCO Dash Board
				if (role.equalsIgnoreCase(RoleEnum.CCO.name())) {
					List<FinancierCreateRequestDTO> listOfNewFin = finCreateReqDao
							.findByOfficeOfficeCodeAndApplicationStatus(officeCode,
									FinancierCreateReqStatus.INITIATED.getLabel());
					listOfNewFin.addAll(finCreateReqDao.findByOfficeOfficeCodeAndApplicationStatus(officeCode,
							FinancierCreateReqStatus.REUPLOAD.getLabel()));
					financierCreatePendingCount = listOfNewFin.size();

				}
				// Financier Applications Count in AO Dash Board
				else if (role.equalsIgnoreCase(RoleEnum.AO.name())) {
					List<FinancierCreateRequestDTO> listOfNewFin = null;
					listOfNewFin = finCreateReqDao.findByOfficeOfficeCodeAndApplicationStatus(officeCode,
							FinancierCreateReqStatus.CCO_APPROVED.getLabel());
					listOfNewFin.addAll(finCreateReqDao.findByOfficeOfficeCodeAndApplicationStatus(officeCode,
							FinancierCreateReqStatus.CCO_REJECTED.getLabel()));
					financierCreatePendingCount = listOfNewFin.size();

				}
				// Financier Applications Count
				for (RTADashBoardServiceNames rtaDashBoardServiceNames : rtaDashBoardLinksDTO.getServiceNames()) {
					if (rtaDashBoardServiceNames.getServiceName()
							.equals(DynamicmenusEnum.FINANCIERPENDING.getDescription())) {
						rtaDashBoardServiceNames.setCountValue(financierCreatePendingCount);

					}
				}
			}
			// aadhar seeding count
			if (rtaDashBoardLinksDTO.getModuleTitle().equals(DynamicmenusEnum.AADHARSEEDING.getDescription())) {
				for (RTADashBoardServiceNames rtaDashBoardServiceNames : rtaDashBoardLinksDTO.getServiceNames()) {
					if (rtaDashBoardServiceNames.getServiceName()
							.equals(DynamicmenusEnum.AADHARSEEDINGINITIATED.getDescription())) {
						if (role.equalsIgnoreCase(RoleEnum.AO.name())) {
							List<AadhaarSeedDTO> aadhaarSeedCountList = aadhaarSeedDAO
									.findByIssuedOfficeCodeAndStatusIn(officeCode,
											Arrays.asList(Status.AadhaarSeedStatus.INITIATED));
							if (!aadhaarSeedCountList.isEmpty()) {
								rtaDashBoardServiceNames.setCountValue(aadhaarSeedCountList.size());
							}
						}
					}

					if (role.equalsIgnoreCase(RoleEnum.RTO.name())) {
						List<AadhaarSeedDTO> aadhaarSeedCountList = aadhaarSeedDAO.findByIssuedOfficeCodeAndStatusIn(
								officeCode, Arrays.asList(Status.AadhaarSeedStatus.AOAPPROVED,
										Status.AadhaarSeedStatus.AOREJECTED));
						List<AadhaarSeedDTO> aadhaarSeedRejectedCountList = aadhaarSeedCountList.stream()
								.filter(val -> val.getStatus().equals(Status.AadhaarSeedStatus.AOREJECTED))
								.collect(Collectors.toList());
						List<AadhaarSeedDTO> aadhaarSeedApprovedCountList = aadhaarSeedCountList.stream()
								.filter(val -> val.getStatus().equals(Status.AadhaarSeedStatus.AOAPPROVED))
								.collect(Collectors.toList());
						if (rtaDashBoardServiceNames.getServiceName()
								.equals(DynamicmenusEnum.AADHARSEEDINGAOREJECTED.getDescription()))
							rtaDashBoardServiceNames.setCountValue(aadhaarSeedRejectedCountList.size());
						if (rtaDashBoardServiceNames.getServiceName()
								.equals(DynamicmenusEnum.AADHARSEEDINGAOAPPROVED.getDescription()))
							rtaDashBoardServiceNames.setCountValue(aadhaarSeedApprovedCountList.size());
					}
				}
			}

			// FinancierSeeding count
			if (rtaDashBoardLinksDTO.getModuleTitle().equals(DynamicmenusEnum.FINANCIERSEEDING.getDescription())) {
				for (RTADashBoardServiceNames rtaDashBoardServiceNames : rtaDashBoardLinksDTO.getServiceNames()) {
					if (rtaDashBoardServiceNames.getServiceName()
							.equals(DynamicmenusEnum.FINANCIERSEEDINGINITIATED.getDescription())) {
						if (role.equalsIgnoreCase(RoleEnum.AO.name())) {
							List<FinanceSeedDetailsDTO> financeSeedCountList = financeSeedDetailsDAO
									.findByStatusAndOfficeCode(StatusRegistration.SUBMITTED.getDescription(),
											officeCode);
							if (!financeSeedCountList.isEmpty()) {
								rtaDashBoardServiceNames.setCountValue(financeSeedCountList.size());
							}
						}
					}

					if (role.equalsIgnoreCase(RoleEnum.RTO.name())) {
						List<FinanceSeedDetailsDTO> financeSeedCountList = financeSeedDetailsDAO
								.findByOfficeCodeAndStatusIn(officeCode,
										Arrays.asList(Status.financeSeedStatus.AOAPPROVED.getStatus(),
												Status.financeSeedStatus.AOREJECTED.getStatus()));
						List<FinanceSeedDetailsDTO> financeSeedRejectedCountList = financeSeedCountList.stream()
								.filter(val -> val.getStatus().equals(Status.financeSeedStatus.AOREJECTED.getStatus()))
								.collect(Collectors.toList());
						List<FinanceSeedDetailsDTO> financeSeedApprovedCountList = financeSeedCountList.stream()
								.filter(val -> val.getStatus().equals(Status.financeSeedStatus.AOAPPROVED.getStatus()))
								.collect(Collectors.toList());
						if (rtaDashBoardServiceNames.getServiceName()
								.equals(DynamicmenusEnum.FINANCIERSEEDINGAOREJECTED.getDescription()))
							rtaDashBoardServiceNames.setCountValue(financeSeedRejectedCountList.size());
						if (rtaDashBoardServiceNames.getServiceName()
								.equals(DynamicmenusEnum.FINANCIERSEEDINGAOAPPROVED.getDescription()))
							rtaDashBoardServiceNames.setCountValue(financeSeedApprovedCountList.size());
					}
				}
			}

			if (rtaDashBoardLinksDTO.getModuleTitle().equals(DynamicmenusEnum.DEALERSERVICES.getDescription())) {
				for (RTADashBoardServiceNames rtaDashBoardServiceNames : rtaDashBoardLinksDTO.getServiceNames()) {
					if (role.equalsIgnoreCase(RoleEnum.DTC.name())) {
						dashBoardCountSetForDealerServices(officeCode, role, rtaDashBoardServiceNames);
					}
				}
			}
			// Reg Citizen end
			this.citizenServicesDashboard(officeCode, user, role, rtaDashBoard.get());
			newRegCounts.add(rtaDashBoardLinksDTO);
			rtaDashBoard.get().setRegistartionServices(newRegCounts);
			rtaDashBoardDTO1 = rtaDashBoard.get();
		}

		return Optional.of(rtadbMapper.convertEntity(rtaDashBoardDTO1));
	}

	/**
	 * DTC Dashboard count setting for Dealer services
	 * 
	 * @param officeCode
	 * @param role
	 * @param rtaDashBoardServiceNames
	 */
	private void dashBoardCountSetForDealerServices(String officeCode, String role,
			RTADashBoardServiceNames rtaDashBoardServiceNames) {
		final String localOfficeCode = officeCode;
		synchronized (localOfficeCode.intern()) {
			if (role.equalsIgnoreCase(RoleEnum.DTC.getName())) {
				List<String> officeCodesList = getOfficeCodesBasedOnDistrict(officeCode);
				List<StatusRegistration> statusList = new ArrayList<>();
				statusList.add(StatusRegistration.REJECTED);
				statusList.add(StatusRegistration.PAYMENTSUCCESS);

				List<DealerRegDTO> newRegList = dealerRegDAO
						.findByOfficeCodeInAndServiceIdsInAndApplicationStatusInAndIsMVIDoneFalse(officeCodesList,
								Arrays.asList(ServiceEnum.DEALERREGISTRATION.getId()),
								Arrays.asList(StatusRegistration.PAYMENTSUCCESS));

				List<DealerRegDTO> renewalList = dealerRegDAO
						.findByOfficeCodeInAndServiceIdsInAndApplicationStatusInAndIsMVIDoneFalse(officeCodesList,
								Arrays.asList(ServiceEnum.DEALERSHIPRENEWAL.getId()),
								Arrays.asList(StatusRegistration.PAYMENTSUCCESS));

				List<DealerRegDTO> mviApproved = dealerRegDAO
						.findByOfficeCodeInAndServiceIdsInAndApplicationStatusInAndIsMVIDoneTrue(officeCodesList,
								Arrays.asList(ServiceEnum.DEALERREGISTRATION.getId(),
										ServiceEnum.DEALERSHIPRENEWAL.getId()),
								Arrays.asList(StatusRegistration.MVIAPPROVED));

				List<DealerRegDTO> mviRejected = dealerRegDAO
						.findByOfficeCodeInAndServiceIdsInAndApplicationStatusInAndIsMVIDoneTrue(officeCodesList,
								Arrays.asList(ServiceEnum.DEALERREGISTRATION.getId(),
										ServiceEnum.DEALERSHIPRENEWAL.getId()),
								Arrays.asList(StatusRegistration.MVIREJECTED));

				List<DealerRegDTO> rejectedList = dealerRegDAO
						.findByOfficeCodeInAndServiceIdsInAndApplicationStatusInAndIsMVIDoneFalse(officeCodesList,
								Arrays.asList(ServiceEnum.DEALERREGISTRATION.getId(),
										ServiceEnum.DEALERSHIPRENEWAL.getId()),
								Arrays.asList(StatusRegistration.REJECTED));

				if (rtaDashBoardServiceNames.getServiceName().equals(DynamicmenusEnum.NEWDEALERSHIP.getDescription())) {
					rtaDashBoardServiceNames.setCountValue(newRegList.size());
				}
				if (rtaDashBoardServiceNames.getServiceName()
						.equals(DynamicmenusEnum.RENEWALOFDEALERSHIP.getDescription())) {
					rtaDashBoardServiceNames.setCountValue(renewalList.size());
				}
				if (rtaDashBoardServiceNames.getServiceName()
						.equals(DynamicmenusEnum.MVIRECOMMENDED.getDescription())) {
					rtaDashBoardServiceNames.setCountValue(mviApproved.size());
				}
				if (rtaDashBoardServiceNames.getServiceName()
						.equals(DynamicmenusEnum.MVINOTRECOMMENDED.getDescription())) {
					rtaDashBoardServiceNames.setCountValue(mviRejected.size());
				}

				if (rtaDashBoardServiceNames.getServiceName().equals(DynamicmenusEnum.REJECTED.getDescription())) {
					rtaDashBoardServiceNames.setCountValue(rejectedList.size());
				}
			}
		}
	}

	private RTADashboardDTO citizenServicesDashboard(String officeCode, String user, String role,
			RTADashboardDTO rtaDashboardCitizen) {

		// String appNo = StringUtils.EMPTY;
		logger.info("citizen services dashboard for user [{}]", user);
		// Integer total = 0;
		Integer nonTransport = 0;
		Integer transport = 0;
		Integer stoppageCount = 0;
		Integer stoppageRevocationCount = 0;
		Integer freshRC = 0;
		Integer freshRCMviAction = 0;
		Integer freshRCMviNoAction = 0;
		Integer showCauseCount = 0;
		Integer rcCancellation = 0;
		List<RegServiceDTO> list = null;
		Integer scrtReplacement = 0;
		Integer auctionCount = 0;
		Integer scrtRenewal = 0;
		Integer dataEntryNonTransport = 0;
		Integer dataEntryTransport = 0;
		if (RoleEnum.MVI.getName().equals(role)) {

			List<RegServiceDTO> listodServices = regServiceDAO
					.findByMviOfficeCodeAndCurrentRolesInAndSourceIsNull(officeCode, Arrays.asList(role));

			List<Integer> serviceIds = Arrays.asList(ServiceEnum.VEHICLESTOPPAGE.getId(),
					ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId(), ServiceEnum.RCFORFINANCE.getId());
			if (!listodServices.isEmpty()) {
				list = listodServices.stream()
						.filter(reg -> serviceIds.stream()
								.anyMatch(sid -> reg.getServiceIds().contains(sid) || (reg.getFlowId() != null
										&& reg.getFlowId().equals(ServiceEnum.Flow.RCCANCELLATIONEMVIACTION))))
						.collect(Collectors.toList());
			}
			List<AuctionDetailsDTO> auctionList = auctionDetailsDAO
					.findByMviOfficeCodeAndMviUserIdAndAuctionClosed(officeCode, user, Boolean.FALSE);
			if (auctionList != null && !auctionList.isEmpty()) {
				auctionCount = auctionList.size();
			}

		} else if (RoleEnum.DTC.getName().equals(role)) {
			List<RegServiceDTO> listodServices = regServiceDAO
					.findByDtcOfficeCodeAndCurrentRolesInAndSourceIsNull(officeCode, Arrays.asList(role));

			List<Integer> serviceIds = Arrays.asList(ServiceEnum.VEHICLESTOPPAGE.getId(),
					ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId());
			if (listodServices != null && !listodServices.isEmpty()) {
				list = listodServices.stream()
						.filter(reg -> serviceIds.stream().anyMatch(sid -> reg.getServiceIds().contains(sid)))
						.collect(Collectors.toList());
			}
			List<AuctionDetailsDTO> auctionList = auctionDetailsDAO
					.findByDtcOfficeCodeAndDtcUserIdAndAuctionClosedAndDtcCompleted(officeCode, user, Boolean.TRUE,
							Boolean.FALSE);
			if (auctionList != null && !auctionList.isEmpty()) {
				auctionCount = auctionList.size();
			}

		} else {
			list = regServiceDAO.findTop70ByOfficeCodeAndCurrentRolesInAndSourceIsNull(officeCode, Arrays.asList(role));
		}
		if (list != null && !list.isEmpty()) {

			List<RegServiceDTO> regServiceDTOList = new ArrayList<>();
			regServiceDTOList = list.parallelStream()
					.filter(val -> null != val.getActionDetails() && val.getActionDetails().stream()
							.anyMatch(action -> role.equals(action.getRole()) && !action.getIsDoneProcess()))
					.collect(Collectors.toList());
			
			

			for (RegServiceDTO dto : regServiceDTOList) {
				
				if (dto.getRegistrationDetails() != null
						&& StringUtils.isNoneBlank(dto.getRegistrationDetails().getVehicleType())
						&& dto.getRegistrationDetails().getVehicleType().equalsIgnoreCase(CovCategory.T.getCode())
						&& dto.getServiceIds().stream().anyMatch(id -> !id.equals(ServiceEnum.RCFORFINANCE.getId()))
						&& dto.getServiceIds().stream()
								.anyMatch(id -> !id.equals(ServiceEnum.RCCANCELLATION.getId()))) {
					if (dto.getServiceIds() != null && !dto.getServiceIds().isEmpty() && dto.getServiceIds().stream()
							.anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))) {
						stoppageCount++;
					} else if (dto.getServiceIds() != null && !dto.getServiceIds().isEmpty() && dto.getServiceIds()
							.stream().anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId()))) {
						if (RoleEnum.MVI.getName().equals(role)) {
							if (dto.getLockedDetails() != null && !dto.getLockedDetails().isEmpty()
									&& dto.getLockedDetails().stream().findFirst().get().getLockedBy()
											.equalsIgnoreCase(user)) {
								stoppageRevocationCount++;
							}
						} else {
							stoppageRevocationCount++;
						}
					} else if (dto.getServiceIds() != null && !dto.getServiceIds().isEmpty() && dto.getServiceIds()
							.stream().anyMatch(id -> id.equals(ServiceEnum.STAGECARRIAGERENEWALOFPERMIT.getId()))) {
						scrtRenewal++;
					} else if (dto.getServiceIds() != null && !dto.getServiceIds().isEmpty()
							&& dto.getServiceIds().stream()
									.anyMatch(id -> id.equals(ServiceEnum.STAGECARRIAGEREPLACEMENTOFVEHICLE.getId()))) {
						scrtReplacement++;
					} else if (dto.getServiceIds() != null && !dto.getServiceIds().isEmpty()
							&& dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))) {
//						if (dto.getRegistrationDetails() != null
//								&& StringUtils.isNoneBlank(dto.getRegistrationDetails().getVehicleType())
//								&& dto.getRegistrationDetails().getVehicleType()
//										.equalsIgnoreCase(CovCategory.N.getCode())
//								&& dto.getServiceIds().stream()
//										.anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))) {
//							dataEntryNonTransport++;
//						}

						if (dto.getRegistrationDetails() != null
								&& StringUtils.isNoneBlank(dto.getRegistrationDetails().getVehicleType())
								&& dto.getRegistrationDetails().getVehicleType()
										.equalsIgnoreCase(CovCategory.T.getCode())
								&& dto.getServiceIds().stream()
										.anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))) {
							dataEntryTransport++;
						}
					} else {
						transport++;
					}

				}

				if (dto.getRegistrationDetails() != null
						&& StringUtils.isNoneBlank(dto.getRegistrationDetails().getVehicleType())
						&& dto.getRegistrationDetails().getVehicleType().equalsIgnoreCase(CovCategory.N.getCode())
						&& dto.getServiceIds().stream().anyMatch(id -> !id.equals(ServiceEnum.RCFORFINANCE.getId()))
						&& dto.getServiceIds().stream().anyMatch(id -> !id.equals(ServiceEnum.RCCANCELLATION.getId()))
						&& dto.getServiceIds().stream().anyMatch(id -> !id.equals(ServiceEnum.DATAENTRY.getId()))) {
					if(dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))) {
						dataEntryNonTransport++;
					}
					else {
						nonTransport++;
					}
					
				}

				// total = transport + nonTransport + fcOtherStation;
				if (dto.getServiceIds() != null && !dto.getServiceIds().isEmpty()
						&& dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId()))) {
					if (role.equalsIgnoreCase(RoleEnum.AO.name()) && dto.getFlowId() != null
							&& dto.getFlowId().equals(ServiceEnum.Flow.RCFORFINANCEMVIACTION)
							&& (dto.getApplicationStatus() != null
									&& (dto.getApplicationStatus().equals(StatusRegistration.MVIAPPROVED)
											|| dto.getApplicationStatus().equals(StatusRegistration.MVIREJECTED)))) {
						freshRCMviAction++;
					} else if (dto.getFlowId() != null
							&& dto.getFlowId().equals(ServiceEnum.Flow.RCFORFINANCEMVINOACTION)) {
						freshRCMviNoAction++;
					} else if (role.equalsIgnoreCase(RoleEnum.MVI.name())
							&& (dto.getApplicationStatus().equals(StatusRegistration.AOAPPROVED)
									|| dto.getApplicationStatus().equals(StatusRegistration.RTOAPPROVED))) {
						freshRC++;
					} else if (role.equalsIgnoreCase(RoleEnum.RTO.name()) && (dto.getApplicationStatus()
							.equals(StatusRegistration.AOREJECTED)
							|| (dto.getApplicationStatus().equals(StatusRegistration.AOAPPROVED)
									&& dto.getActionDetails().stream()
											.anyMatch(selectedrole -> selectedrole.getRole().equals(RoleEnum.MVI.name())
													&& selectedrole.getIsDoneProcess())))) {
						freshRC++;
					} else if (role.equalsIgnoreCase(RoleEnum.AO.name())
							&& (dto.getApplicationStatus().equals(StatusRegistration.PAYMENTDONE)
									|| dto.getApplicationStatus().equals(StatusRegistration.REUPLOAD))) {
						freshRC++;
					}
				}
				if (dto.getServiceIds() != null && !dto.getServiceIds().isEmpty()
						&& dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RCCANCELLATION.getId()))) {
					if (role.equalsIgnoreCase(RoleEnum.RTO.name()) && dto.getFlowId() != null
							&& dto.getFlowId().equals(ServiceEnum.Flow.RCCANCELLATIONCCO)) {
						rcCancellation++;
					}
				}
				if (dto.getServiceIds() != null && !dto.getServiceIds().isEmpty()
						&& dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RCCANCELLATION.getId()))) {
					if (role.equalsIgnoreCase(RoleEnum.MVI.name()) && dto.getFlowId() != null
							&& dto.getFlowId().equals(ServiceEnum.Flow.RCCANCELLATIONEMVIACTION)) {
						rcCancellation++;
					}
				}
				if (dto.getServiceIds() != null && !dto.getServiceIds().isEmpty()
						&& dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RCCANCELLATION.getId()))) {
					if (role.equalsIgnoreCase(RoleEnum.AO.name()) && dto.getFlowId() != null
							&& dto.getFlowId().equals(ServiceEnum.Flow.RCCANCELLATIONEMVIACTION)) {
						rcCancellation++;
					}
				}
				if (dto.getServiceIds() != null && !dto.getServiceIds().isEmpty()
						&& dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RCCANCELLATION.getId()))) {
					if (role.equalsIgnoreCase(RoleEnum.RTO.name()) && dto.getFlowId() != null
							&& dto.getFlowId().equals(ServiceEnum.Flow.RCCANCELLATIONEMVIACTION)) {
						rcCancellation++;
					}
				}
			}

		} else {
			logger.info("no records OfficeCode: {},role: {} and user :{}", officeCode, role, user);
			// logger.info(MessageKeys.MESSAGE_NO_RECORD_FOUND + "[{}]", list);
		}
		List<RegServiceDTO> osNocCount = null;
		Integer feeCount = null;
		if (RoleEnum.RTO.getName().equals(role) || RoleEnum.AO.getName().equals(role)) {
			osNocCount = regServiceDAO.findByOfficeCodeAndApplicationStatusAndOtherStateNOCStatus(officeCode,
					StatusRegistration.APPROVED, StatusRegistration.NOCVERIFICATIONPENDING);
			showCauseCount = nonPaymentDetailsDAO.countByOfficeCodeAndAndApplicationStatus(officeCode,
					StatusRegistration.CCOISSUED);
			List<FeeCorrectionDTO> feeList = getTotalFeeCorrectionDocs(officeCode, role);
			if (feeList != null && !feeList.isEmpty()) {
				feeCount = feeList.size();
			}
		}
		for (RTADashBoardLinksDTO rtaDashBoardLinksDTO : rtaDashboardCitizen.getRegistartionServices()) {
			if (rtaDashBoardLinksDTO.getModuleTitle().equals(DynamicmenusEnum.REGISTRATIONSERVICES.getDescription())) {
				for (RTADashBoardServiceNames rtaDashBoardServiceNames : rtaDashBoardLinksDTO.getServiceNames()) {
					if (rtaDashBoardServiceNames.getServiceName().equals(DynamicmenusEnum.TPENDING.getDescription())) {
						rtaDashBoardServiceNames.setCountValue(transport);

					}
					if (rtaDashBoardServiceNames.getServiceName().equals(DynamicmenusEnum.NTPENDING.getDescription())) {
						rtaDashBoardServiceNames.setCountValue(nonTransport);
					}
					if (rtaDashBoardServiceNames.getServiceName().equals(ServiceEnum.VEHICLESTOPPAGE.getDesc())) {
						rtaDashBoardServiceNames.setCountValue(stoppageCount);
					}
					if (rtaDashBoardServiceNames.getServiceName()
							.equals(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getDesc())) {
						rtaDashBoardServiceNames.setCountValue(stoppageRevocationCount);
					}
					if (rtaDashBoardServiceNames.getServiceName()
							.equals(ServiceEnum.STAGECARRIAGERENEWALOFPERMIT.getDesc())) {
						rtaDashBoardServiceNames.setCountValue(scrtRenewal);
					}
					if (rtaDashBoardServiceNames.getServiceName()
							.equals(ServiceEnum.STAGECARRIAGEREPLACEMENTOFVEHICLE.getDesc())) {
						rtaDashBoardServiceNames.setCountValue(scrtReplacement);
					}
					if (rtaDashBoardServiceNames.getServiceName().equals(ServiceEnum.SHOWCAUSENO.getDesc())) {
						rtaDashBoardServiceNames.setCountValue(showCauseCount);
					}
					if (rtaDashBoardServiceNames.getServiceName()
							.equals(DynamicmenusEnum.NOCVERIFICATIONPENDING.getDescription())) {
						if (RoleEnum.RTO.getName().equals(role) || RoleEnum.AO.getName().equals(role)) {
							if (CollectionUtils.isNotEmpty(osNocCount)) {
								rtaDashBoardServiceNames.setCountValue(osNocCount.size());
							}
						}

					}
					if (rtaDashBoardServiceNames.getServiceName()
							.equals(DynamicmenusEnum.RCCANCELLATION.getDescription())) {
						rtaDashBoardServiceNames.setCountValue(rcCancellation);
					}
					if (rtaDashBoardServiceNames.getServiceName().equals(TransferType.AUCTION.getType())) {
						rtaDashBoardServiceNames.setCountValue(auctionCount);
					}
					if (rtaDashBoardServiceNames.getServiceName()
							.equals(DynamicmenusEnum.FEECORRECTIONS.getDescription())) {
						if (RoleEnum.RTO.getName().equals(role) || RoleEnum.AO.getName().equals(role)) {
							rtaDashBoardServiceNames.setCountValue(feeCount);

						}

					}
				}
			}
			if (rtaDashBoardLinksDTO.getModuleTitle().equals(DynamicmenusEnum.BILLATERALTAX.getDescription())) {
				for (RTADashBoardServiceNames rtaDashBoardServiceNames : rtaDashBoardLinksDTO.getServiceNames()) {
					if (rtaDashBoardServiceNames.getServiceName()
							.equals(DynamicmenusEnum.BILLATERALTAX.getDescription())) {
						rtaDashBoardServiceNames.setCountValue(this.countForBilateral(officeCode, role));
					}
				}
			}
			// TODO for vehicle stoppage
			/*
			 * if(rtaDashBoardLinksDTO.getModuleTitle().equals(DynamicmenusEnum.
			 * BILLATERALTAX.getDescription())){ for (RTADashBoardServiceNames
			 * rtaDashBoardServiceNames : rtaDashBoardLinksDTO.getServiceNames()) { if
			 * (rtaDashBoardServiceNames.getServiceName().equals(DynamicmenusEnum.
			 * BILLATERALTAX.getDescription())) {
			 * rtaDashBoardServiceNames.setCountValue(this.countForBilateral(officeCode,
			 * role)); } } }
			 */
			if (rtaDashBoardLinksDTO.getModuleTitle().equals(DynamicmenusEnum.OTHERS.getDescription())) {
				for (RTADashBoardServiceNames rtaDashBoardServiceNames : rtaDashBoardLinksDTO.getServiceNames()) {
					if (rtaDashBoardServiceNames.getServiceName()
							.equals(DynamicmenusEnum.FRESHRCPENDING.getDescription())) {
						rtaDashBoardServiceNames.setCountValue(freshRC);
					}
					if (rtaDashBoardServiceNames.getServiceName().equals(DynamicmenusEnum.MVIACTION.getDescription())) {
						rtaDashBoardServiceNames.setCountValue(freshRCMviAction);
					}
					if (rtaDashBoardServiceNames.getServiceName()
							.equals(DynamicmenusEnum.NOMVIACTION.getDescription())) {
						rtaDashBoardServiceNames.setCountValue(freshRCMviNoAction);
					}
				}
			}

			// for otherstateServices
			if (rtaDashBoardLinksDTO.getModuleTitle().equals(DynamicmenusEnum.OTHERSTATESERVICES.getDescription())) {
				for (RTADashBoardServiceNames rtaDashBoardServiceNames : rtaDashBoardLinksDTO.getServiceNames()) {
					if (rtaDashBoardServiceNames.getServiceName()
							.equals(DynamicmenusEnum.DATAENTRYNONTRANSPORT.getDescription())) {
						rtaDashBoardServiceNames.setCountValue(dataEntryNonTransport);
					}
					if (rtaDashBoardServiceNames.getServiceName()
							.equals(DynamicmenusEnum.DATAENTRYTRANSPORT.getDescription())) {
						rtaDashBoardServiceNames.setCountValue(dataEntryTransport);
					}
				}

			}

		}
		// RTADashboardDTO RTADashboard = null;
		return rtaDashboardCitizen;

	}

	@Override
	public List<CitizenEnclosuresLogsVO> previousIterationDetails(String applicationNo) {
		List<CitizenEnclosuresLogsVO> listofImageLogs = new ArrayList<>();
		List<CitizenEnclosuresLogsDTO> enclosureslog = EnclosuresLogDAO.findByapplicationNo(applicationNo);
		Optional<RegServiceDTO> regService = regServiceDAO.findByApplicationNo(applicationNo);
		if (!regService.isPresent()) {
		}
		RegServiceDTO regServiceDTO = regService.get();
		regServiceDTO.getRegistrationDetails().getEnclosures();
		for (CitizenEnclosuresLogsDTO citizenEnclosures : enclosureslog) {
			for (KeyValue<String, List<ImageEnclosureDTO>> keyValue : citizenEnclosures.getEnclosures()) {
				CitizenEnclosuresLogsVO enclosuresLogsVO = new CitizenEnclosuresLogsVO();
				enclosuresLogsVO.setIterationNo(citizenEnclosures.getIterator().toString());
				for (ImageEnclosureDTO enclosureDTO : keyValue.getValue()) {
					enclosuresLogsVO.setImageName(enclosureDTO.getEnclosureName());
					for (ImageActionsDTO ImageActionsDTO : enclosureDTO.getImageActions()) {
						if (ImageActionsDTO.getRole().equalsIgnoreCase(RoleEnum.CCO.name())) {
							enclosuresLogsVO.setRoleNameCCO(ImageActionsDTO.getRole());
							enclosuresLogsVO.setStatusCCO(ImageActionsDTO.getAction());
							enclosuresLogsVO.setCommentsCCO(ImageActionsDTO.getComments());
						}
						if (ImageActionsDTO.getRole().equalsIgnoreCase(RoleEnum.AO.name())) {
							enclosuresLogsVO.setRoleNameAO(ImageActionsDTO.getRole());
							enclosuresLogsVO.setStatusAO(ImageActionsDTO.getAction());
							enclosuresLogsVO.setCommentsAO(ImageActionsDTO.getComments());
						}
						if (ImageActionsDTO.getRole().equalsIgnoreCase(RoleEnum.RTO.name())) {
							enclosuresLogsVO.setRoleNameRTO(ImageActionsDTO.getRole());
							enclosuresLogsVO.setStatusRTO(ImageActionsDTO.getAction());
							enclosuresLogsVO.setCommentsRTO(ImageActionsDTO.getComments());
						}
						if (ImageActionsDTO.getRole().equalsIgnoreCase(RoleEnum.MVI.name())) {
							enclosuresLogsVO.setRoleNameMVI(ImageActionsDTO.getRole());
							enclosuresLogsVO.setStatusMVI(ImageActionsDTO.getAction());
							enclosuresLogsVO.setCommentsMVI(ImageActionsDTO.getComments());
						}
					}
				}
				if (enclosuresLogsVO.getImageName() != null) {
					listofImageLogs.add(enclosuresLogsVO);
				}

			}

		}
		return listofImageLogs;
	}

	@Override
	public PropertiesVO collectionTypedropdown(String role, String module) {
		Optional<PropertiesDTO> resut = propertiesDAO.findByRoleTypeAndCorrectionStatusTrueAndCorrectionModule(role,
				module);
		if (resut.isPresent() && CollectionUtils.isNotEmpty(resut.get().getCollectionType())) {
			PropertiesVO vo = new PropertiesVO();
			List<CorrectionDropDown> corrDropDownList = new ArrayList<>();
			resut.get().getCollectionType().forEach(fields -> {
				CorrectionDropDown corrDropDown = new CorrectionDropDown();
				corrDropDown.setId(fields.getServiceId());
				corrDropDown.setName(fields.getName());
				corrDropDownList.add(corrDropDown);

			});
			vo.setCollectionType(corrDropDownList);
			return vo;
		}
		throw new BadRequestException(" Drop Down Values Not Available ");
	}

	@Override
	public void saveForCollectionCorrections(CorrectionsVO vo, String user) {
		Optional<UserDTO> userDetails = userDAO.findByUserId(user);
		if (!userDetails.isPresent()) {
			throw new BadRequestException("User Details Not Found");
		}
		if (StringUtils.isNoneBlank(vo.getServiceType())) {
			switch (ServiceTypeCorrection.getType(vo.getServiceType())) {
			case REGDETAILS:
				saveforRegDetails(vo, userDetails.get());
				break;
			case REGSER:
				saveforRegServices(vo, userDetails.get());
				break;
			case FC:
				saveforFCDetails(vo, userDetails.get());
				break;
			case TAX:
				saveforTaxDetails(vo, userDetails.get());

				break;
			case PERMITS:
				saveforPermitsDetails(vo, userDetails.get());
				break;
			default:
				break;
			}
		}

	}

	private void saveforRegDetails(CorrectionsVO vo, UserDTO userDTO) {

		CollectionCorrectionServiceLogsDTO logDTO = validateCorrectionData(vo, userDTO);
		Optional<RegistrationDetailsDTO> regDetails = registrationDetailDAO.findByPrNo(vo.getPrNo());
		Object modifiedObject = setObjectPropertyNull(regDetails.get(), vo.getData());
		RegistrationDetailsDTO regDto = (RegistrationDetailsDTO) modifiedObject;
		logDTO.setStatus("CORRECTION_DONE");
		CollectionDTO collDTO = new CollectionDTO();
		collDTO.setRegDetails(regDto);
		logDTO.getCcrResponseDetails().setCollection(collDTO);
		registrationDetailDAO.save(regDto);
		// ccorrectionDAO.save(logDTO);
		saveCollectionCorrectionServiceLogs(vo.getServiceType(), logDTO);

	}

	private Object setObjectPropertyNull(Object obj, List<CollectionCorrectonSaveVO> fields) {
		if (null == obj) {
			throw new BadRequestException("Record not available for correction");
		}
		for (CollectionCorrectonSaveVO field : fields) {
			try {
				BeanUtilsBean.getInstance().getPropertyUtils().setProperty(obj, field.getPath(),
						parseString(field.getValue()));
			} catch (NestedNullException nne) {
				logger.info(nne.getMessage());
				instantiateNestedProperties(obj, field.getPath());
				try {
					BeanUtilsBean.getInstance().getPropertyUtils().setProperty(obj, field.getPath(),
							parseString(field.getValue()));
				} catch (Exception e) {
					logger.info(e.getMessage());
				}
			} catch (IllegalArgumentException ila) {
				if (GenericValidator.isLong(field.getValue())) {
					try {
						Double d = Double.valueOf(field.getValue());
						BeanUtilsBean.getInstance().getPropertyUtils().setProperty(obj, field.getPath(), d.longValue());
					} catch (IllegalArgumentException ia) {
						try {
							BeanUtilsBean.getInstance().getPropertyUtils().setProperty(obj, field.getPath(),
									Integer.valueOf(field.getValue()));
						} catch (Exception e) {
						}

					} catch (Exception e) {

					}
				}
			} catch (Exception e) {
				logger.info(e.getMessage());

			}

		}
		return obj;
	}

	private void saveforRegServices(CorrectionsVO vo, UserDTO userDTO) {
		CollectionCorrectionServiceLogsDTO logDTO = validateCorrectionData(vo, userDTO);
		RegServiceDTO regSerDTO = saveforRegServicesREQBackUP(vo);
		Object modifiedObject = setObjectPropertyNull(regSerDTO, vo.getData());
		RegServiceDTO serDTO = (RegServiceDTO) modifiedObject;
		logDTO.setStatus("CORRECTION_DONE");
		CollectionDTO collDTO = new CollectionDTO();
		collDTO.setRegService(serDTO);
		logDTO.getCcrResponseDetails().setCollection(collDTO);
		regServiceDAO.save(serDTO);
		// ccorrectionDAO.save(logDTO);
		saveCollectionCorrectionServiceLogs(vo.getServiceType(), logDTO);
	}

	private void saveforTaxDetails(CorrectionsVO vo, UserDTO userDTO) {

		CollectionCorrectionServiceLogsDTO logDTO = validateCorrectionData(vo, userDTO);
		TaxDetailsDTO taxDetails = saveforTaxDetailsREQBackUP(vo);
		Object modifiedObject = setObjectPropertyNull(taxDetails, vo.getData());
		TaxDetailsDTO taxModifiedDetails = (TaxDetailsDTO) modifiedObject;
		logDTO.setStatus("CORRECTION_DONE");
		CollectionDTO collDTO = new CollectionDTO();
		collDTO.setTaxDetails(taxModifiedDetails);
		logDTO.getCcrResponseDetails().setCollection(collDTO);
		taxDetailsDAO.save(taxModifiedDetails);
		// ccorrectionDAO.save(logDTO);
		saveTaxDetailsToREGDetails(taxModifiedDetails);
		saveCollectionCorrectionServiceLogs(vo.getServiceType(), logDTO);
	}

	private void saveforFCDetails(CorrectionsVO vo, UserDTO userDTO) {
		CollectionCorrectionServiceLogsDTO logDTO = validateCorrectionData(vo, userDTO);
		FcDetailsDTO fcDetails = saveforFCDetailsREQBackUP(vo);
		Object modifiedObject = setObjectPropertyNull(fcDetails, vo.getData());
		FcDetailsDTO fcDTO = (FcDetailsDTO) modifiedObject;
		logDTO.setStatus("CORRECTION_DONE");
		CollectionDTO collDTO = new CollectionDTO();
		collDTO.setFcDetails(fcDTO);
		logDTO.getCcrResponseDetails().setCollection(collDTO);
		fcDetailsDAO.save(fcDTO);
		// ccorrectionDAO.save(logDTO);
		saveFCDetailsToREGDetails(fcDTO);
		saveCollectionCorrectionServiceLogs(vo.getServiceType(), logDTO);
	}

	private void saveforPermitsDetails(CorrectionsVO vo, UserDTO userDTO) {
		CollectionCorrectionServiceLogsDTO logDTO = validateCorrectionData(vo, userDTO);
		PermitDetailsDTO permitDetails = saveforPermitsDetailsREQBackUP(vo);
		Object modifiedObject = setObjectPropertyNull(permitDetails, vo.getData());
		PermitDetailsDTO perDetails = (PermitDetailsDTO) modifiedObject;
		logDTO.setStatus("CORRECTION_DONE");
		CollectionDTO collDTO = new CollectionDTO();
		collDTO.setPermitDetails(perDetails);
		logDTO.getCcrResponseDetails().setCollection(collDTO);
		permitDetailsDAO.save(perDetails);
		// ccorrectionDAO.save(logDTO);
		saveCollectionCorrectionServiceLogs(vo.getServiceType(), logDTO);

	}

	public <T> Optional<T> readValue(String value, Class<T> valueType) {
		try {
			return Optional.of(objectMapper.readValue(value, valueType));
		} catch (IOException ioe) {

			logger.error("Exception occured while converting String to Object", ioe);
			throw new BadRequestException("Please Pass Valid Data.");
		}
	}

	@Override
	public CorrectionsVO saveForClientRequestDetails(CorrectionsVO vo, String user, HttpServletRequest request,
			Boolean requestType) {
		if (StringUtils.isBlank(vo.getTicketNumber())) {
			logger.error("Please Enter Ticket Number");
			throw new BadRequestException("Please Enter Ticket Number");
		}
		Optional<UserDTO> userDetails = userDAO.findByUserId(user);
		if (!userDetails.isPresent()) {
			throw new BadRequestException("User Details Not Found");
		}
		UserDTO userDTO = userDetails.get();
		CollectionCorrectionServiceLogsDTO ccrDetailslog = null;
		if (!requestType) {
			ccrDetailslog = getCorrectionLogByToken(vo.getServiceType(), vo.getToken());
			if (ccrDetailslog == null || !ccrDetailslog.getTicketNumber().equals(vo.getTicketNumber())) {
				logger.debug("Client Requset not found");
				throw new BadRequestException("Requsted record not available / Entered Ticket Number not valid ");
			}
			if (!ccrDetailslog.getRequestIp().equals(ipWebUtils.getClientIp(request))) {
				logger.debug("Invalid IP Address");
				ccrDetailslog.setResponseIp(ipWebUtils.getClientIp(request));
				ccrDetailslog.setStatus("INVALID_IP_SAVING");
				saveCollectionCorrectionServiceLogs(vo.getServiceType(), ccrDetailslog);
				throw new BadRequestException("Inavlid Request");
			}
			if (ccrDetailslog.getStatus().equalsIgnoreCase("SESSION EXPIRED")) {
				throw new BadRequestException("Session already expired");
			}
			ccrDetailslog.setAadhaarNumber(checkAadhaarDetails(vo, userDTO));
			ccrDetailslog.setUserName(userDTO.getUserId());
			ccrDetailslog.setResponseIp(ipWebUtils.getClientIp(request));

		} else {
			ccrDetailslog = new CollectionCorrectionServiceLogsDTO();
			ccrDetailslog.setRequestIp(ipWebUtils.getClientIp(request));
			ccrDetailslog.setCreatedDate(LocalDateTime.now());
			ccrDetailslog.setTicketNumber(vo.getTicketNumber());
		}

		ccrDetailslog.setPrNo(vo.getPrNo());
		ccrDetailslog.setApplicationNo(vo.getAppNo());
		ccrDetailslog.setChassisNo(vo.getChassisNo());
		ccrDetailslog.setTrNo(vo.getTrNo());

		ccrDetailslog.setCollectionName(vo.getServiceType());
		ClientMetaData clientMetaData = new ClientMetaData();
		// Find public IP address
		String url = request.getRequestURL().toString();
		String systemipaddress = "";
		try {
			URL url_name = new URL(url);
			BufferedReader sc = new BufferedReader(new InputStreamReader(url_name.openStream()));
			systemipaddress = sc.readLine().trim();
		} catch (Exception e) {
			logger.error("system ip address :" + e.getMessage());
		}
		clientMetaData.setPublicIPAddress(systemipaddress);
		clientMetaData.setUrl(url);
		clientMetaData.setUri(request.getRequestURI());
		clientMetaData.setScheme(request.getScheme());
		clientMetaData.setServerName(request.getServerName());
		clientMetaData.setPortNumber(request.getServerPort());
		clientMetaData.setContextPath(request.getContextPath());
		clientMetaData.setServletPath(request.getServletPath());
		clientMetaData.setRequestTime(LocalTime.now());
		clientMetaData.setSessionTime(request.getSession().getCreationTime());
		clientMetaData.setUserName(userDTO.getUserName());
		clientMetaData.setRoleType(vo.getSelectedRole());
		CorrectionsVO cov = new CorrectionsVO();
		if (requestType) {
			CcrRequestDetails ccrRequestDetails = new CcrRequestDetails();
			ccrRequestDetails.setClientMetaData(clientMetaData);
			CollectionDTO collDTO = requestCollectionBackUp(vo);
			ccrRequestDetails.setCollection(collDTO);
			ccrDetailslog.setCcrRequestDetails(ccrRequestDetails);
			ccrDetailslog.setStatus("REQCOMPLETED");
			vo.setTargetCollection(collDTO.getTargetObject());
			vo.setSourceCollection(collDTO.getSourceObject());
			Map<String, List<CorrectionVO>> list = collectionCorrectionServices.getRegistrationDetails(vo);
			String token = java.util.UUID.randomUUID().toString();
			ccrDetailslog.setRequestId(token);
			collDTO.setSourceObject(null);
			collDTO.setTargetObject(null);
			List<CorrectionVO> corrVOList = new ArrayList<>();
			for (Entry<String, List<CorrectionVO>> map : list.entrySet()) {
				corrVOList.addAll(map.getValue());
			}
			ccrRequestDetails.setRequsetedFields(corrVOList);
			saveCollectionCorrectionServiceLogs(vo.getServiceType(), ccrDetailslog);
			if (list.isEmpty()) {
				logger.debug("correction fields not available");
				throw new BadRequestException("Correction fields not available for the user");
			}
			cov.setToken(token);
			cov.setReg(list);
			return cov;
		}
		CcrResponseDetails clientResponse = new CcrResponseDetails();
		clientResponse.setClientMetaData(clientMetaData);
		ccrDetailslog.setCcrResponseDetails(clientResponse);
		long hours = ccrDetailslog.getCcrRequestDetails().getClientMetaData().getRequestTime()
				.until(LocalDateTime.now(), ChronoUnit.HOURS);
		if (hours > 2) {
			logger.debug("Client Requset time out");
			ccrDetailslog.setStatus("SESSION EXPIRED");
			saveCollectionCorrectionServiceLogs(vo.getServiceType(), ccrDetailslog);
			throw new BadRequestException("Session is expired to correct data");
		}
		ccrDetailslog.getCcrResponseDetails().setModifiedFields(
				setOldValueFields(vo.getData(), ccrDetailslog.getCcrRequestDetails().getRequsetedFields()));
		ccrDetailslog.setNotifyRequired(Boolean.TRUE);
		ccrDetailslog.setStatus("RESPONSE_INITIATED");
		if (vo.getUploadfiles() != null && vo.getUploadfiles().length > 0) {
			ccrDetailslog.setEnclosures(saveSupportTicketDocument(vo.getUploadfiles()));
		}
		saveCollectionCorrectionServiceLogs(vo.getServiceType(), ccrDetailslog);
		return cov;
	}

	private List<CollectionCorrectonSaveVO> setOldValueFields(List<CollectionCorrectonSaveVO> data,
			List<CorrectionVO> requsetedFields) {
		data.forEach(fields -> {
			Optional<CorrectionVO> vo = requsetedFields.stream().filter(s -> s.getJsonPath().equals(fields.getPath()))
					.findAny();

			if (vo.isPresent()) {
				CorrectionVO found = vo.get();
				fields.setOldValue(found.getFieldValue());
				fields.setFieldLabel(found.getFeildLabel());
				fields.setNotify(found.getNotify());
			}
		});
		return data;
	}

	@Override
	public void saveForClientResponseDetails(CorrectionsVO correctionsVO, String user, HttpServletResponse response) {
		Optional<UserDTO> userDetails = userDAO.findByUserId(user);
		if (!userDetails.isPresent()) {
			throw new BadRequestException("User Details Not Found");
		}
		UserDTO userDTO = userDetails.get();
		CorrectionsVO vo = correctionsVO;
		CollectionCorrectionServiceLogsDTO ccrDetailslog = new CollectionCorrectionServiceLogsDTO();
		ccrDetailslog.setApplicationNo(vo.getAppNo());
		ccrDetailslog.setRequestId(java.util.UUID.randomUUID().toString());
		// ccrDetailslog.setResponseIp(responseIp);
		ccrDetailslog.setCollectionName(vo.getServiceType());
		ClientMetaData clientMetaData = new ClientMetaData();
		// response.
		// Find public IP address
		// response.get
		// String url = response.getRequestURL().toString();
		// String systemipaddress = "";
		// try {
		// URL url_name = new URL(url);
		// BufferedReader sc = new BufferedReader(new
		// InputStreamReader(url_name.openStream()));
		// systemipaddress = sc.readLine().trim();
		// } catch (Exception e) {
		// logger.error("system ip address :" + e.getMessage());
		// }
		// clientMetaData.setPublicIPAddress(systemipaddress);
		// clientMetaData.setUrl(url);
		// clientMetaData.setUri(response.getRequestURI());
		// clientMetaData.setScheme(response.getScheme());
		// clientMetaData.setServerName(response.getServerName());
		// clientMetaData.setPortNumber(response.getServerPort());
		// clientMetaData.setContextPath(response.getContextPath());
		// clientMetaData.setServletPath(response.getServletPath());
		clientMetaData.setResponseTime(LocalTime.now());
		// clientMetaData.setSessionTime(response.get);
		clientMetaData.setUserName(userDTO.getUserId());
		clientMetaData.setRoleType(vo.getSelectedRole());
		CcrResponseDetails ccrResponseDetails = new CcrResponseDetails();
		ccrResponseDetails.setClientMetaData(clientMetaData);
		ccrResponseDetails.setCollection(this.requestCollectionBackUp(vo));
		ccrDetailslog.setCcrResponseDetails(ccrResponseDetails);
		ccrDetailslog.setStatus("REQCOMPLETED");
		// ccorrectionDAO.save(ccrDetailslog);
		saveCollectionCorrectionServiceLogs(vo.getServiceType(), ccrDetailslog);

	}

	private CollectionDTO requestCollectionBackUp(CorrectionsVO vo) {
		CollectionDTO Collection = new CollectionDTO();
		if (StringUtils.isNoneBlank(vo.getServiceType())) {
			switch (ServiceTypeCorrection.getType(vo.getServiceType())) {
			case REGDETAILS:
				Collection.setRegDetails(this.saveforRegDetailsREQBackUP(vo));
				BaseRegistrationDetailsDTO baseDTO = new BaseRegistrationDetailsDTO();
				Collection.setSourceObject(baseDTO);
				Collection.setTargetObject(Collection.getRegDetails());
				break;
			case REGSER:
				Collection.setRegService(this.saveforRegServicesREQBackUP(vo));
				RegServiceApprovedDTO regSerDTO = new RegServiceApprovedDTO();

				Collection.setSourceObject(regSerDTO);
				Collection.setTargetObject(Collection.getRegService());
				break;
			case FC:
				Collection.setFcDetails(this.saveforFCDetailsREQBackUP(vo));
				FcDetailsDTO fcDTO = new FcDetailsDTO();
				Collection.setSourceObject(fcDTO);
				Collection.setTargetObject(Collection.getFcDetails());
				break;
			case TAX:
				Collection.setTaxDetails(this.saveforTaxDetailsREQBackUP(vo));
				TaxDetailsDTO taxDTO = new TaxDetailsDTO();
				Collection.setSourceObject(taxDTO);
				Collection.setTargetObject(Collection.getTaxDetails());
				break;
			case PERMITS:
				Collection.setPermitDetails(this.saveforPermitsDetailsREQBackUP(vo));
				PermitDetailsDTO perDTO = new PermitDetailsDTO();
				Collection.setSourceObject(perDTO);
				Collection.setTargetObject(Collection.getPermitDetails());
				break;
			default:
				break;
			}
		}

		return Collection;

	}

	private RegistrationDetailsDTO saveforRegDetailsREQBackUP(CorrectionsVO vo) {

		Optional<RegistrationDetailsDTO> regDetails = registrationDetailDAO.findByPrNo(vo.getPrNo());
		if (regDetails.isPresent()) {
			return regDetails.get();
		}
		throw new BadRequestException("No Record Found with Registration Number : " + vo.getPrNo());

	}

	private RegServiceDTO saveforRegServicesREQBackUP(CorrectionsVO vo) {

		Optional<RegServiceDTO> regSericeDetails = regServiceDAO.findByApplicationNo(vo.getAppNo());
		if (regSericeDetails.isPresent()) {
			return regSericeDetails.get();
		}

		throw new BadRequestException("No Record Found with Application Number : " + vo.getAppNo());
	}

	private PermitDetailsDTO saveforPermitsDetailsREQBackUP(CorrectionsVO vo) {

		Optional<PermitDetailsDTO> permitDetails = permitDetailsDAO.findByPrNoOrderByCreatedDateDesc(vo.getPrNo());
		if (permitDetails.isPresent()) {
			return permitDetails.get();
		}

		throw new BadRequestException("No Record Found with Registration Number : " + vo.getPrNo());
	}

	private TaxDetailsDTO saveforTaxDetailsREQBackUP(CorrectionsVO vo) {

		Optional<TaxDetailsDTO> taxDetails = taxDetailsDAO.findByApplicationNoOrderByCreatedDateDesc(vo.getAppNo());
		if (taxDetails.isPresent()) {
			return taxDetails.get();
		}
		throw new BadRequestException("No Record Found with Registration Number : " + vo.getAppNo());
	}

	private FcDetailsDTO saveforFCDetailsREQBackUP(CorrectionsVO vo) {

		Optional<FcDetailsDTO> fcDetails = fcDetailsDAO.findByPrNoOrderByCreatedDateDesc(vo.getPrNo());
		if (fcDetails.isPresent()) {
			return fcDetails.get();
		}
		throw new BadRequestException("No Record Found with Registration Number : " + vo.getPrNo());
	}

	private static Object parseString(String myString) {

		if (StringUtils.isEmpty(myString)) {
			throw new BadRequestException("value missed for the Collection");
		}
		try {
			if (GenericValidator.isDouble(myString)) {
				return Double.valueOf(myString);
			}
			if (myString.equalsIgnoreCase("true") || myString.equalsIgnoreCase("false")) {
				return Boolean.valueOf(myString);
			}
			try {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
				return LocalDate.parse(myString, formatter);
			} catch (Exception e) {
				return myString;
			}
		} catch (Exception e) {
			return myString;
		}

	}

	private CollectionCorrectionServiceLogsDTO validateCorrectionData(CorrectionsVO vo, UserDTO userDTO) {

		List<String> pathList = new ArrayList<>();
		vo.getData().forEach(data -> {
			if (data.getPath() != null) {
				pathList.add(data.getPath());
			}
		});

		// CollectionCorrectionServiceLogsDTO logDTO =
		// ccorrectionDAO.findByRequestId(vo.getToken());
		CollectionCorrectionServiceLogsDTO logDTO = getCorrectionLogByToken(vo.getServiceType(), vo.getToken());
		/*
		 * String mode = "2"; Long value =
		 * collectionDAO.countByJsonPathInAndRolesRoleNameAndRolesModeAndApplicationType
		 * (pathList,userDTO.getPrimaryRole().getName(),mode,vo.getSeriveType());
		 * if(pathList.size()!=value){ logDTO.setStatus("FIELED_MISMATCH");
		 * ccorrectionDAO.save(logDTO); throw new
		 * BadRequestException("modification not done"); }
		 * logDTO.getCcrRequestDetails().getRequsetedFields().forEach(jsonPath->{
		 * if(jsonPath.equals("2")){ pathList.add(jsonPath.getJsonPath()); } });
		 */
		/*
		 * if(!pathList.containsAll(pathList)){ logDTO.setStatus("FIELED_MISMATCH");
		 * ccorrectionDAO.save(logDTO); throw new
		 * BadRequestException("modification not done"); }
		 */
		return logDTO;
	}

	private void instantiateNestedProperties(Object obj, String fieldName) {
		try {
			String[] fieldNames = fieldName.split("\\.");
			if (fieldNames.length > 1) {
				StringBuffer nestedProperty = new StringBuffer();
				for (int i = 0; i < fieldNames.length - 1; i++) {
					String fn = fieldNames[i];
					if (i != 0) {
						nestedProperty.append(".");
					}
					nestedProperty.append(fn);

					Object value = PropertyUtils.getProperty(obj, nestedProperty.toString());

					if (value == null) {
						PropertyDescriptor propertyDescriptor = PropertyUtils.getPropertyDescriptor(obj,
								nestedProperty.toString());
						Class<?> propertyType = propertyDescriptor.getPropertyType();
						Object newInstance = propertyType.newInstance();
						PropertyUtils.setProperty(obj, nestedProperty.toString(), newInstance);
					}
				}
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			throw new RuntimeException(e);
		} catch (NoSuchMethodException e) {
			throw new RuntimeException(e);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Saving Correction log DTO based on service Type
	 * 
	 * @param serviceType
	 * @param logDTO
	 */
	private void saveCollectionCorrectionServiceLogs(String serviceType, CollectionCorrectionServiceLogsDTO logDTO) {

		checkObjectNullOrNot(logDTO);

		switch (ServiceTypeCorrection.getType(serviceType)) {

		case REGDETAILS:
			RegistartionCorrectionLogDTO regDTO = new RegistartionCorrectionLogDTO();
			BeanUtils.copyProperties(logDTO, regDTO);
			regCorrectionLogDAO.save(regDTO);
			break;

		case REGSER:
			RegServiceCorrectionLogDTO regSer = new RegServiceCorrectionLogDTO();
			BeanUtils.copyProperties(logDTO, regSer);
			regServiceCorrectionLogDAO.save(regSer);
			break;

		case FC:
			FcCorrectionLogDTO fcLog = new FcCorrectionLogDTO();
			BeanUtils.copyProperties(logDTO, fcLog);
			fcCorrectionLogDAO.save(fcLog);
			break;

		case TAX:
			TaxCorrectionLogDTO taxLog = new TaxCorrectionLogDTO();
			BeanUtils.copyProperties(logDTO, taxLog);
			taxCorrectionLogDAO.save(taxLog);
			break;

		case PERMITS:
			PermitCorrectionLogDTO perLog = new PermitCorrectionLogDTO();
			BeanUtils.copyProperties(logDTO, perLog);
			permitCorrectionLogDAO.save(perLog);
			break;

		default:
			break;
		}

	}

	/**
	 * Getting Correction Log DTO based on token Id & ServiceType
	 * 
	 * @param serviceType
	 * @param token
	 * @return
	 */
	private CollectionCorrectionServiceLogsDTO getCorrectionLogByToken(String serviceType, String token) {

		if (!StringUtils.isNotBlank(serviceType)) {
			logger.debug("Service Type is getting blank in getCorrectionLogByToken method ");
			throw new BadRequestException("Service Type not Available");
		}
		CollectionCorrectionServiceLogsDTO correctionLog = null;
		switch (ServiceTypeCorrection.getType(serviceType)) {

		case REGDETAILS:
			correctionLog = regCorrectionLogDAO.findByRequestId(token);
			break;

		case REGSER:
			correctionLog = regServiceCorrectionLogDAO.findByRequestId(token);
			break;

		case FC:
			correctionLog = fcCorrectionLogDAO.findByRequestId(token);
			break;

		case TAX:
			correctionLog = taxCorrectionLogDAO.findByRequestId(token);
			break;

		case PERMITS:
			correctionLog = permitCorrectionLogDAO.findByRequestId(token);
			break;

		default:
			break;
		}
		checkObjectNullOrNot(correctionLog);
		return correctionLog;
	}

	/**
	 * Checking Object is null or not
	 * 
	 * @param obj
	 */
	private void checkObjectNullOrNot(Object obj) {
		if (obj == null) {
			logger.debug("Collection Correction Log DTO Not Available");
			throw new BadRequestException("Collection Correction Log Not Available");
		}
	}

	@Override
	public String saveRcCancellationDetails(RegServiceVO regVO, MultipartFile[] uploadfiles, JwtUser jwtUser) {
		RegServiceVO registrationDetails = registrationService.findByprNo(regVO.getPrNo());
		if (registrationDetails != null && registrationDetails.getRcCancellation() != null
				&& registrationDetails.getServiceType().contains(ServiceEnum.RCCANCELLATION)) {
			logger.debug("RC CANCELLATION Request is Pending " + "[{}]", regVO.getPrNo());
			throw new BadRequestException("RC CANCELLATION Request is Pending " + regVO.getPrNo());
		}

		Optional<RegistrationDetailsDTO> registrationOptional = registrationDetailDAO.findByPrNo(regVO.getPrNo());
		if (!registrationOptional.isPresent()) {
			logger.debug(MessageKeys.MESSAGE_NO_RECORD_FOUND + "[{}]", regVO.getPrNo());
			throw new BadRequestException(MessageKeys.MESSAGE_NO_RECORD_FOUND + regVO.getPrNo());
		}
		RCCancellationDTO rcCancellationDTO = new RCCancellationDTO();
		rcCancellationDTO.setReasonForCancellation(regVO.getRcCancellation().getReasonForCancellation());
		RegServiceDTO regServiceDTO = new RegServiceDTO();
		regServiceDTO.setPrNo(regVO.getPrNo());
		rcCancellationDTO.setIsCancelledByCitizen(false);
		regServiceDTO.setServiceIds(Stream.of(ServiceEnum.RCCANCELLATION.getId()).collect(Collectors.toSet()));
		regServiceDTO.setServiceType(Arrays.asList(ServiceEnum.RCCANCELLATION));
		regServiceDTO.setFlowId(ServiceEnum.Flow.RCCANCELLATIONCCO);
		regServiceDTO.setRcCancellation(rcCancellationDTO);
		regServiceDTO.setRegistrationDetails(registrationOptional.get());
		regServiceDTO.setOfficeDetails(registrationOptional.get().getOfficeDetails());
		regServiceDTO.setOfficeCode(regServiceDTO.getOfficeDetails().getOfficeCode());
		regServiceDTO.setCreatedBy(RoleEnum.CCO.getName());
		setTaxValididtyForRcCancellation(regServiceDTO);
		setPermitDetailsForRcCancellation(regServiceDTO);
		registrationService.setMviOfficeDetails(regServiceDTO, regVO);
		if (null == regServiceDTO.getApplicationNo()) {
			Map<String, String> officeCodeMap = new TreeMap<>();
			officeCodeMap.put("officeCode", regServiceDTO.getRegistrationDetails().getOfficeDetails().getOfficeCode());
			regServiceDTO.setApplicationNo(sequenceGenerator
					.getSequence(String.valueOf(Sequence.REGISTRATIONAPPNO.getSequenceId()), officeCodeMap));
		}
		regServiceDTO.setApplicationStatus(StatusRegistration.INITIATED);
		regServiceDTO.setCreatedDate(LocalDateTime.now());
		registratrionServicesApprovals.initiateApprovalProcessFlow(regServiceDTO);
		ActionDetails actionDetail = getActionDetailByRole(regServiceDTO, RoleEnum.CCO.getName());
		updateActionDetailsStatus(RoleEnum.CCO.getName(), jwtUser.getId(), StatusRegistration.APPROVED.toString(),
				actionDetail, regServiceDTO.getApplicationNo());
		incrementToNextIndex(regServiceDTO, actionDetail, RoleEnum.CCO.getName());
		if (null != regVO.getImages() && !regVO.getImages().isEmpty()) {
			regVO.setImageInput(regVO.getImages());
		}
		try {
			registrationServiceImpl.saveImages(regVO, regServiceDTO, uploadfiles);
		} catch (IOException e) {
			logger.debug("Exception Occured [{}]", e.getMessage());
			logger.error("Exception Occured [{}]", e);
		}
		regServiceDAO.save(regServiceDTO);
		return regServiceDTO.getApplicationNo();
	}

	private String checkAadhaarDetails(CorrectionsVO vo, UserDTO userDTO) {

		AadhaarSourceDTO aadhaarSourceDTO = new AadhaarSourceDTO();

		if (vo.getAadharRequestModel() == null || userDTO.getAadharNo() == null
				|| !userDTO.getAadharNo().equals(vo.getAadharRequestModel().getUid_num())) {
			throw new BadRequestException("Authentication Need To Modify Data / Aadhaar Number is Not Configured");
		}
		if (StringUtils.isNotBlank(vo.getPrNo())) {
			aadhaarSourceDTO.setPrNo(vo.getPrNo());
		} else {
			aadhaarSourceDTO.setApplicationNo(vo.getAppNo());
		}
		aadhaarSourceDTO.setService(AadhaarSeedStatus.INITIATED.getDesc());
		Optional<AadharDetailsResponseVO> aadharUserDetailsResponseVO = restGateWayService
				.validateAadhaar(vo.getAadharRequestModel(), aadhaarSourceDTO);

		if (!aadharUserDetailsResponseVO.isPresent()) {
			throw new BadRequestException("Aadhar details not found");
		}
		if (aadharUserDetailsResponseVO.get().getAuth_status()
				.equals(ResponseStatusEnum.AADHAARRESPONSE.FAILED.getLabel())) {
			throw new BadRequestException(aadharUserDetailsResponseVO.get().getAuth_err_code());
		}
		if (!userDTO.getAadharNo().equals(aadharUserDetailsResponseVO.get().getUid().toString())) {
			throw new BadRequestException("UnAuthorized user");
		}

		return aadharUserDetailsResponseVO.get().getUid().toString();
	}

	private List<ImageEnclosureDTO> saveSupportTicketDocument(MultipartFile[] uploadfiles) {
		try {
			return gridFsClient.saveFileForCollectionCorrection(uploadfiles);
		} catch (IOException e) {
			logger.error("Exception{}", e);
			throw new BadRequestException(e.getMessage());
		}
	}

	@Override
	public List<UserVO> getMviNames(String apNo) {
		Optional<RegServiceDTO> regSerDtoOpt = regServiceDAO.findByApplicationNo(apNo);
		if (!regSerDtoOpt.isPresent()) {
			throw new BadRequestException("Applicate Details Not Found with applicationNo " + apNo);
		}
		if (StringUtils.isEmpty(regSerDtoOpt.get().getMviOfficeCode())) {
			throw new BadRequestException("Office Details Not Found with applicationNo " + apNo);
		}
		List<UserDTO> userList = userDAO.findByPrimaryRoleNameOrAdditionalRolesNameAndOfficeOfficeCodeNative(
				RoleEnum.MVI.name(), RoleEnum.MVI.name(), regSerDtoOpt.get().getMviOfficeCode());
		if (CollectionUtils.isNotEmpty(userList)) {
			/*
			 * List<UserDTO> filterList = userList.stream().filter(p -> p.getFirstName() !=
			 * null) .collect(Collectors.toList());
			 */
			// if (CollectionUtils.isNotEmpty(filterList)) {
			userList.stream().map(val -> val.getFirstName()).collect(Collectors.toList());
			return userMapper.convertlimittedList(userList);
			// }
		}
		return Collections.emptyList();
	}

	private String setVehicleTypeCode(RegServiceDTO regServiceDTO) {
		if (regServiceDTO != null && regServiceDTO.getRegistrationDetails() != null
				&& StringUtils.isNotEmpty(regServiceDTO.getRegistrationDetails().getVehicleType())) {
			return regServiceDTO.getRegistrationDetails().getVehicleType();
		} else {
			if (regServiceDTO != null && StringUtils.isNotEmpty(regServiceDTO.getPrNo())) {
				Optional<RegistrationDetailsDTO> regDetails = registrationDetailDAO.findByPrNo(regServiceDTO.getPrNo());
				if (regDetails.isPresent()) {
					if (StringUtils.isNotEmpty(regDetails.get().getVehicleType())) {
						return regDetails.get().getVehicleType();
					}
				}
			}
		}
		throw new BadRequestException("Vehicle Details Found with applicationNo " + regServiceDTO.getApplicationNo());
	}

	@Override
	public List<String> getMviByOffice(String officeCode) {
		if (StringUtils.isEmpty(officeCode)) {
			throw new BadRequestException("No OfficeCode Found");
		}
		List<String> result = new ArrayList<>();
		List<UserDTO> mviList = userDAO.findByPrimaryRoleNameOrAdditionalRolesNameAndOfficeOfficeCodeNative(
				RoleEnum.MVI.getName(), RoleEnum.MVI.getName(), officeCode);
		if (CollectionUtils.isNotEmpty(mviList)) {
			mviList.forEach(val -> {
				StringBuilder name = new StringBuilder();
				if (StringUtils.isNoneEmpty(val.getFirstName()) && !val.getFirstName().equals(" ")) {
					name.append(val.getFirstName());
				}
				if (StringUtils.isNoneEmpty(val.getMiddleName()) && !val.getMiddleName().equals(" ")) {
					name.append(val.getMiddleName());
				}
				if (StringUtils.isNoneEmpty(val.getLastName()) && !val.getLastName().equals(" ")) {
					name.append(val.getLastName());
				}
				result.add(name.toString());
			});
			return result;
		}
		return Collections.emptyList();
	}

	@Override
	public Optional<CorrectionsVO> getReadOnlyCorrectionDetails(CorrectionsVO vo) {

		CorrectionsVO responseVo = new CorrectionsVO();
		CollectionDTO collDTO = requestCollectionBackUp(vo);
		vo.setTargetCollection(collDTO.getTargetObject());
		vo.setSourceCollection(collDTO.getSourceObject());
		Map<String, List<CorrectionVO>> map = collectionCorrectionServices.getRegistrationDetails(vo);
		responseVo.setReg(map);
		return Optional.of(responseVo);
	}

	@Override
	public List<CollectionCorrectionServiceLogsDTO> getDynamicCorrectionFieldsForDTC(Map<String, String> map)
			throws JSONException {

		JSONObject jsonObject = new JSONObject(map);
		String fromDate = null, toDate = null, serviceType = null, ticketNumber = null;
		if (jsonObject.has("fromDate")) {
			fromDate = jsonObject.getString("fromDate");
			toDate = jsonObject.getString("toDate");
		}
		if (jsonObject.has("serviceType"))
			serviceType = jsonObject.getString("serviceType");
		if (jsonObject.has("ticketNumber"))
			ticketNumber = jsonObject.getString("ticketNumber");
		if (serviceType == null || (fromDate == null || toDate == null) && ticketNumber == null) {
			throw new BadRequestException("Invalid Input. Please check all fields are correct or not");
		}

		List<CollectionCorrectionServiceLogsDTO> logResult = getCorrectionServiceLog(serviceType, fromDate, toDate,
				ticketNumber);

		if (logResult.isEmpty()) {
			throw new BadRequestException("No data found on selected dates");
		}

		List<CollectionCorrectionServiceLogsDTO> resultList = new ArrayList<>();
		logResult.forEach(result -> {
			CollectionCorrectionServiceLogsDTO logDto = new CollectionCorrectionServiceLogsDTO();
			CcrResponseDetails ccrResponse = result.getCcrResponseDetails();
			if (result.getStatus().equals("CORRECTION_DONE") && ccrResponse != null) {
				boolean isNotify = false;
				for (CollectionCorrectonSaveVO fields : ccrResponse.getModifiedFields()) {
					if (fields.getNotify() != null && fields.getNotify()) {
						isNotify = true;
					}
				}
				if (isNotify) {
					logDto.setAadhaarNumber(result.getAadhaarNumber());
					logDto.setCreatedDate(result.getCreatedDate());
					logDto.setUserName(result.getUserName());
					logDto.setRequestIp(result.getRequestIp());
					logDto.setResponseIp(result.getResponseIp());
					logDto.setRequestId(result.getRequestId());
					logDto.setTicketNumber(result.getTicketNumber());
					logDto.setCollectionName(result.getCollectionName());
					logDto.setEnclosures(result.getEnclosures());
					resultList.add(logDto);
				}
			}
		});

		return resultList;
	}

	private List<CollectionCorrectionServiceLogsDTO> getCorrectionServiceLog(String serviceType, String dateFrom,
			String dateTo, String ticketNumber) {

		LocalDateTime fromDate = null, toDate = null;
		if (dateFrom != null) {
			LocalDate from = LocalDate.parse(dateFrom);
			fromDate = LocalDateTime.of(from, LocalTime.MIDNIGHT);
			LocalDate to = LocalDate.parse(dateTo);
			toDate = LocalDateTime.of(to, LocalTime.MAX);
		}
		List<CollectionCorrectionServiceLogsDTO> logDTO;

		switch (ServiceTypeCorrection.getType(serviceType)) {
		case REGDETAILS:
			if (ticketNumber != null)
				logDTO = regCorrectionLogDAO.findAllByTicketNumber(ticketNumber);
			else
				logDTO = regCorrectionLogDAO.findAllByCreatedDateBetween(fromDate, toDate);
			break;

		case REGSER:
			if (ticketNumber != null)
				logDTO = regServiceCorrectionLogDAO.findAllByTicketNumber(ticketNumber);
			else
				logDTO = regServiceCorrectionLogDAO.findAllByCreatedDateBetween(fromDate, toDate);
			break;

		case FC:
			if (ticketNumber != null)
				logDTO = fcCorrectionLogDAO.findAllByTicketNumber(ticketNumber);
			else
				logDTO = fcCorrectionLogDAO.findAllByCreatedDateBetween(fromDate, toDate);
			break;

		case TAX:
			if (ticketNumber != null)
				logDTO = taxCorrectionLogDAO.findAllByTicketNumber(ticketNumber);
			else
				logDTO = taxCorrectionLogDAO.findAllByCreatedDateBetween(fromDate, toDate);
			break;

		case PERMITS:
			if (ticketNumber != null)
				logDTO = permitCorrectionLogDAO.findAllByTicketNumber(ticketNumber);
			else
				logDTO = permitCorrectionLogDAO.findAllByCreatedDateBetween(fromDate, toDate);
			break;

		default:
			logDTO = Collections.emptyList();
			break;
		}
		return logDTO;
	}

	@Override
	public Optional<CollectionCorrectionServiceLogsDTO> getCorrectionLogFields(String serviceType, String token) {

		CollectionCorrectionServiceLogsDTO correctionLog = getCorrectionLogByToken(serviceType, token);
		correctionLog.getCcrResponseDetails().setClientMetaData(null);
		correctionLog.getCcrResponseDetails().setCollection(null);
		correctionLog.setCcrRequestDetails(null);
		return Optional.of(correctionLog);
	}

	@Override
	public Enclosures getfRCRecordForAOBasedOnprNo(String prNo, String officeCode, String selectedRole, String userId) {
		Optional<RegServiceDTO> regServiceDTOOpt = regServiceDAO
				.findByPrNoAndServiceTypeInAndApplicationStatusOrderByCreatedDateDesc(prNo,
						Arrays.asList(ServiceEnum.RCFORFINANCE), StatusRegistration.APPROVED.getDescription());
		if (!regServiceDTOOpt.isPresent()) {
			throw new BadRequestException("With this prNo  freshRc Applications are not found" + prNo);
		}
		if (!officeCode.equals(regServiceDTOOpt.get().getOfficeCode())) {
			throw new BadRequestException("Application belongs to  " + regServiceDTOOpt.get().getOfficeCode());
		}
		return magesService.getListOfEnclosureCitizenToSubmit(regServiceDTOOpt.get().getApplicationNo(), userId,
				selectedRole);
	}

	@Override
	public Boolean enableFrom37ForAO(String applcationNo, String userId) {
		Optional<RegServiceDTO> regServiceOpt = regServiceDAO.findByApplicationNo(applcationNo);
		if (regServiceOpt.isPresent() && !regServiceOpt.get().isMviDone()
				&& regServiceOpt.get().getFlowId().equals(Flow.RCFORFINANCEMVIACTION)) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	@Override
	public List<CorrectionDTO> getDataForEntry() {
		return collectionDAO.findAll();
	}

	@Override
	public List<String> getModulesForCollectionCorrection(List<String> user) {
		Optional<PropertiesDTO> properties = propertiesDAO.findByIsCollectionCorrectionTrue();
		List<String> result = new ArrayList<>();
		if (properties.isPresent()) {
			List<CollectionCorrectionModule> modules = properties.get().getCcModules();
			modules = modules.stream().filter(module -> {
				return module.getModuleStatus() && module.getRoles().containsAll(user);
			}).collect(Collectors.toList());
			modules.forEach(module -> {
				result.add(module.getModuleName());
			});
		}
		return result;
	}

	@Override
	public void saveDataForEntry(CorrectionDTO dto) throws Exception {
		Query query = new Query(Criteria.where("jsonPath").is(dto.getJsonPath())
				.andOperator(Criteria.where("applicationType").is(dto.getApplicationType())));

		Update update = new Update();
		update.set("apiParam", dto.getApiParam());
		// update.set("applicationType", dto.getApplicationType());
		update.set("dataType", dto.getDataType());
		update.set("feildLabel", dto.getFeildLabel());
		update.set("fieldType", dto.getFieldType());
		// update.set("fieldValue", dto.getFieldValue());
		update.set("mode", dto.getMode());
		update.set("namePath", dto.getNamePath());
		update.set("onChange", dto.getOnChange());
		update.set("regExpression", dto.getRegExpression());
		update.set("roles", dto.getRoles());
		update.set("status", dto.getStatus());
		update.set("valuePath", dto.getValuePath());
		update.set("notify", dto.getNotify());
		update.set("api", dto.getApi());

		mongoTemplate.updateFirst(query, update, CorrectionDTO.class);
	}

	@Override
	public String saveRtoDetailsForShowCause(ApplicationSearchVO applicationVO, String role) {
		if (CollectionUtils.isNotEmpty(applicationVO.getScNos())) {
			List<ShowCauseDetailsDTO> showCauseList = new ArrayList<>();
			List<RegistrationDetailsDTO> regsitrationDetailsList = new ArrayList<>();
			List<RegServiceDTO> regServiceList = new ArrayList<>();
			List<NonPaymentDetailsDTO> nonPaymentList = new ArrayList<>();
			applicationVO.getScNos().stream().forEach(scNo -> {
				Optional<ShowCauseDetailsDTO> scDtoOpt = showCauseDetailsDAO.findByScNo(scNo);
				if (scDtoOpt.isPresent()
						&& scDtoOpt.get().getScStatus().equals(Status.ShowCauseStatus.CCOISSUED.getStatus())) {
					addShowCauseDetails(scDtoOpt.get(), applicationVO, role);
					showCauseList.add(scDtoOpt.get());
				}
				Optional<RegistrationDetailsDTO> regdetailsDto = registrationDetailDAO
						.findByPrNo(scDtoOpt.get().getPrNo());
				if (regdetailsDto.isPresent()) {
					RegistrationDetailsDTO regDto = regdetailsDto.get();
					if (regDto.getIsScNoGenerated() == false) {
						regDto.setIsScNoGenerated(true);
						regsitrationDetailsList.add(regDto);
					}
				}
				Optional<RegServiceDTO> regServiceDto = regServiceDAO.findByApplicationNo(scNo);
				if (regServiceDto.isPresent()) {
					RegServiceDTO regServDto = regServiceDto.get();
					addRegServiceDetails(regServDto, role);
					regServiceList.add(regServDto);
				}
				Optional<NonPaymentDetailsDTO> nonPaymentDto = nonPaymentDetailsDAO
						.findByPrNo(scDtoOpt.get().getPrNo());
				if (nonPaymentDto.isPresent()) {
					NonPaymentDetailsDTO nonPayDto = nonPaymentDto.get();
					addNonPaymentDetails(nonPayDto, role, applicationVO);
					nonPaymentList.add(nonPayDto);

				}
			});

			showCauseDetailsDAO.save(showCauseList);
			registrationDetailDAO.save(regsitrationDetailsList);
			regServiceDAO.save(regServiceList);
			nonPaymentDetailsDAO.save(nonPaymentList);
		} else {
			logger.error("Please select atleast one record!!");
			throw new BadRequestException("Please select atleast one record!!");
		}

		return "Success";
	}

	private void addShowCauseDetails(ShowCauseDetailsDTO showCauseDto, ApplicationSearchVO applicationVo, String role) {
		if (StringUtils.isNoneBlank(applicationVo.getShowCauseDetailsVO().getMviName())) {
			showCauseDto.setMviName(applicationVo.getShowCauseDetailsVO().getMviName());
		}
		if (StringUtils.isNoneBlank(role)) {
			if (role.equals(RoleEnum.AO.getName())) {
				showCauseDto.setApprovedBy(RoleEnum.AO.getName());
				showCauseDto.setScStatus(Status.ShowCauseStatus.AOAPPROVED.getStatus());
			} else if (role.equals(RoleEnum.RTO.getName())) {
				showCauseDto.setApprovedBy(RoleEnum.RTO.getName());
				showCauseDto.setScStatus(Status.ShowCauseStatus.RTOAPPROVED.getStatus());
			}
		}
		showCauseDto.setApprovedDate(LocalDateTime.now());
	}

	private void addRegServiceDetails(RegServiceDTO regServiceDto, String role) {
		if (StringUtils.isNoneBlank(role)) {
			if (role.equals(RoleEnum.AO.getName())) {
				regServiceDto.setApplicationStatus(StatusRegistration.AOAPPROVED);
			} else if (role.equals(RoleEnum.RTO.getName())) {
				regServiceDto.setApplicationStatus(StatusRegistration.RTOAPPROVED);
			}
		}
		if (regServiceDto.getIsScNoGenerated() == false) {
			regServiceDto.setIsScNoGenerated(true);
		}
	}

	private void addNonPaymentDetails(NonPaymentDetailsDTO nonPayDto, String role, ApplicationSearchVO applicationVo) {
		if (StringUtils.isNoneBlank(role)) {
			if (role.equals(RoleEnum.AO.getName())) {
				nonPayDto.setApplicationStatus(StatusRegistration.AOAPPROVED);
				nonPayDto.setApprovedBy(RoleEnum.AO.getName());
			} else if (role.equals(RoleEnum.RTO.getName())) {
				nonPayDto.setApplicationStatus(StatusRegistration.RTOAPPROVED);
				nonPayDto.setApprovedBy(RoleEnum.RTO.getName());
			}
		}
		if (nonPayDto.getIsScNoGenerated() == false) {
			nonPayDto.setIsScNoGenerated(true);
		}
		if (StringUtils.isNoneBlank(applicationVo.getShowCauseDetailsVO().getMviName())) {
			nonPayDto.setMviName(applicationVo.getShowCauseDetailsVO().getMviName());
		}
		nonPayDto.setApprovedDate(LocalDate.now());
	}

	@Override
	public Optional<ReportsVO> getRtoDetilsForShowCauseNo(JwtUser jwtUser, Pageable pagable) {
		String officeCode = jwtUser.getOfficeCode();
		Page<NonPaymentDetailsDTO> nonPayment = nonPaymentDetailsDAO.findByOfficeCodeAndAndApplicationStatus(officeCode,
				StatusRegistration.CCOISSUED, pagable.previousOrFirst());
		if (nonPayment.hasContent()) {
			List<NonPaymentDetailsDTO> nonPaymentDtoList = nonPayment.getContent();
			if (CollectionUtils.isNotEmpty(nonPaymentDtoList)) {
				ReportsVO reportVO = new ReportsVO();
				reportVO.setNonPaymentDetails(nonPaymentDetailsMapper.convertEntity(nonPaymentDtoList));
				reportVO.setPageNo(nonPayment.getNumber());
				reportVO.setTotalPage(nonPayment.getTotalPages());
				return Optional.of(reportVO);
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<ReportsVO> getRtoDetilsForShowCauseNoWithMandal(String mandalName, JwtUser jwtUser,
			Pageable pagable) {
		String officeCode = jwtUser.getOfficeCode();
		Page<NonPaymentDetailsDTO> nonPayment = nonPaymentDetailsDAO
				.findByOfficeCodeAndAndApplicationStatusAndMandalName(officeCode, StatusRegistration.CCOISSUED,
						mandalName, pagable.previousOrFirst());
		if (nonPayment.hasContent()) {
			List<NonPaymentDetailsDTO> nonPaymentDtoList = nonPayment.getContent();
			if (CollectionUtils.isNotEmpty(nonPaymentDtoList)) {
				ReportsVO reportVO = new ReportsVO();
				reportVO.setNonPaymentDetails(nonPaymentDetailsMapper.convertEntity(nonPaymentDtoList));
				reportVO.setPageNo(nonPayment.getNumber());
				reportVO.setTotalPage(nonPayment.getTotalPages());
				return Optional.of(reportVO);
			}
		}
		return Optional.empty();
	}

	@Override
	public Optional<NonPaymentDetailsVO> getRtoDetailsForShowCauseSingle(ApplicationSearchVO applicationSearchVO,
			JwtUser jwtUser) {
		String officeCode = jwtUser.getOfficeCode();
		if (StringUtils.isNotEmpty(applicationSearchVO.getPrNo())) {
			Optional<NonPaymentDetailsVO> nonPaymentVo = getDetailsByprNo(applicationSearchVO.getPrNo(), officeCode);
			return nonPaymentVo;
		} else if (StringUtils.isNotEmpty(applicationSearchVO.getScNo())) {
			Optional<NonPaymentDetailsVO> nonPaymentVo = getDetailsByscNo(applicationSearchVO.getScNo(), officeCode);
			return nonPaymentVo;

		}
		return Optional.empty();
	}

	private Optional<NonPaymentDetailsVO> getDetailsByprNo(String prNo, String officeCode) {
		Optional<NonPaymentDetailsDTO> nonPaymentDetailsDTO = nonPaymentDetailsDAO
				.findByOfficeCodeAndApplicationStatusInAndPrNo(officeCode, Arrays.asList(StatusRegistration.CCOISSUED),
						prNo);
		if (nonPaymentDetailsDTO.isPresent()) {
			NonPaymentDetailsDTO nonPaymentDetailsDto = nonPaymentDetailsDTO.get();
			return Optional.of(nonPaymentDetailsMapper.convertEntity(nonPaymentDetailsDto));
		} else {
			logger.error("No data available for prNo  [{}]", prNo);
			throw new BadRequestException("No data available for prNo " + prNo);
		}
	}

	private Optional<NonPaymentDetailsVO> getDetailsByscNo(String scNo, String officeCode) {
		Optional<NonPaymentDetailsDTO> nonPaymentDetailsDTO = nonPaymentDetailsDAO
				.findByOfficeCodeAndApplicationStatusInAndScNo(officeCode, Arrays.asList(StatusRegistration.CCOISSUED),
						scNo);
		if (nonPaymentDetailsDTO.isPresent()) {
			NonPaymentDetailsDTO nonPaymentDetailsDto = nonPaymentDetailsDTO.get();
			return Optional.of(nonPaymentDetailsMapper.convertEntity(nonPaymentDetailsDto));
		} else {
			logger.error("No data available for scNo  [{}]", scNo);
			throw new BadRequestException("No data available for scNo " + scNo);
		}
	}

	@Override
	public String checkMviNameValidation(String applicationNo, String mviName) {
		List<UserVO> mviNamesList = getMviNames(applicationNo);
		if (!mviNamesList.isEmpty()) {
			List<UserVO> filterList = mviNamesList.stream()
					.filter(name -> name.getUserName() != null && name.getUserName().equals(mviName))
					.collect(Collectors.toList());
			if (CollectionUtils.isNotEmpty(filterList)) {
				return filterList.stream().findFirst().get().getUserId();
			}
		}
		throw new BadRequestException("Please select valid user : " + applicationNo);
	}

	private void getPageCount(ReportsVO reportsVo, List<NonPaymentDetailsDTO> nonPaymentList) {
		int page = 0;
		if (!nonPaymentList.isEmpty()) {
			float nonPaymentSize = nonPaymentList.size();
			if (nonPaymentSize <= 10) {
				reportsVo.setPageNo(1);
			} else {
				page = (int) Math.ceil(nonPaymentSize / 10);
				reportsVo.setPageNo(page);

			}
		}
	}

	@Override
	public List<Map<String, String>> getAllVehicleTypes() {
		List<Map<String, String>> list = new ArrayList<>();
		Map<String, String> map = new HashMap<>();
		map.put("vehicleType", CovCategory.T.getCode());
		map.put("name", CovCategory.T.getDescription());
		list.add(map);
		map = new HashMap<>();
		map.put("vehicleType", CovCategory.N.getCode());
		map.put("name", CovCategory.N.getDescription());
		list.add(map);
		return list;
	}

	private void setActionsForOffences(OffenceDTO offenceDTO, String module, UserDTO userDetails, String selectedRole,
			String ipAddress, String from, String to) {
		ActionDetails actions = new ActionDetails();
		actions.setRole(selectedRole);
		actions.setUserId(userDetails.getUserId());
		actions.setModule(module);
		actions.setCreatedDate(LocalDateTime.now());
		actions.setAadharNo(userDetails.getAadharNo());
		actions.setIpAddress(ipAddress);
		if (StringUtils.isNoneBlank(from) && StringUtils.isNoneBlank(to)) {
			actions.setFrom(from);
			actions.setTo(to);
		}
		if (offenceDTO.getActions() == null || offenceDTO.getActions().isEmpty()) {
			offenceDTO.setActions(Arrays.asList(actions));
		} else {
			offenceDTO.getActions().add(actions);
		}

	}

	private void saveTaxDetailsToREGDetails(TaxDetailsDTO taxDetails) {
		Optional<RegistrationDetailsDTO> regDetails = registrationDetailDAO.findByPrNo(taxDetails.getPrNo());
		if (regDetails.isPresent()) {
			RegistrationDetailsDTO regDetail = regDetails.get();
			if (taxDetails.getChassisNo() != null) {
				regDetail.getVahanDetails().setChassisNumber(taxDetails.getChassisNo());
				regDetail.getVehicleDetails().setChassisNumber(taxDetails.getChassisNo());
			}
			if (taxDetails.getGreenTaxPeriodEnd() != null) {
				regDetail.getRegistrationValidity().setTaxValidity(taxDetails.getGreenTaxPeriodEnd());
				regDetail.setTaxvalidity(taxDetails.getGreenTaxPeriodEnd());
			}
			registrationDetailDAO.save(regDetail);
			if (regDetail.getVehicleType().equals(CovCategory.T.getCode())) {
				List<PermitDetailsDTO> savelistofPermits = new ArrayList<>();
				List<PermitDetailsDTO> listofPermits = permitDetailsDAO.findByPrNoAndPermitStatus(taxDetails.getPrNo(),
						PermitsEnum.ACTIVE.getDescription());
				{
					if (listofPermits.isEmpty() && listofPermits.size() > 0) {
						for (PermitDetailsDTO permitDetils : listofPermits) {
							permitDetils.setRdto(regDetail);
							savelistofPermits.add(permitDetils);
						}
						permitDetailsDAO.save(savelistofPermits);
					}
				}
			}
		}
	}

	private void saveFCDetailsToREGDetails(FcDetailsDTO fcDTO) {
		Optional<RegistrationDetailsDTO> regDetails = registrationDetailDAO.findByPrNo(fcDTO.getPrNo());
		if (regDetails.isPresent()) {
			RegistrationDetailsDTO regDetail = regDetails.get();
			if (fcDTO.getChassisNo() != null) {
				regDetail.getVahanDetails().setChassisNumber(fcDTO.getChassisNo());
				regDetail.getVehicleDetails().setChassisNumber(fcDTO.getChassisNo());
				regDetail.getVahanDetails().setEngineNumber(fcDTO.getEngineNo());
				regDetail.getVehicleDetails().setEngineNumber(fcDTO.getEngineNo());
			}
			if (fcDTO.getFcValidUpto() != null) {
				regDetail.getRegistrationValidity().setFcValidity(fcDTO.getFcValidUpto());
			}
			registrationDetailDAO.save(regDetail);
			if (regDetail.getVehicleType().equals(CovCategory.T.getCode())) {
				List<PermitDetailsDTO> savelistofPermits = new ArrayList<>();
				List<PermitDetailsDTO> listofPermits = permitDetailsDAO.findByPrNoAndPermitStatus(fcDTO.getPrNo(),
						PermitsEnum.ACTIVE.getDescription());
				{
					if (listofPermits.isEmpty() && listofPermits.size() > 0) {
						for (PermitDetailsDTO permitDetils : listofPermits) {
							permitDetils.setRdto(regDetail);
							savelistofPermits.add(permitDetils);
						}
						permitDetailsDAO.save(savelistofPermits);
					}
				}
			}
		}
	}

	private void setCoorectionsLog(List<VcrCorrectionLogDTO> correctionsData, String fieldName, String from, String to,
			Integer fromInteger, Integer toInteger, LocalDate fromDate, LocalDate toDate, UserDTO userDetails,
			String selectedRole, String ipAddress) {
		VcrCorrectionLogDTO dto = new VcrCorrectionLogDTO();
		dto.setFieldName(fieldName);
		if (StringUtils.isNoneEmpty(from)) {
			dto.setFrom(from);
		}
		if (StringUtils.isNoneEmpty(to)) {
			dto.setTo(to);
		}

		if (fromInteger != null) {
			dto.setIntFrom(fromInteger);
		}

		if (toInteger != null) {
			dto.setIntTo(toInteger);
		}

		if (fromDate != null) {
			dto.setFromDate(fromDate);
		}

		if (toDate != null) {
			dto.setToDate(toDate);
		}
		correctionsData.add(dto);
	}

	@Override
	public List<RegServiceVO> getStoppagePendingList(String officeCode, String role, String service) {

		List<RegServiceVO> returnList = new ArrayList<>();

		final String localOfficeCode = officeCode;

		synchronized (localOfficeCode.intern()) {

			if (StringUtils.isBlank(service)) {
				logger.error("Service type missing for vehicle stoppage");
				throw new BadRequestException("Service type missing for vehicle stoppage");
			}
			Integer serviceId = ServiceEnum.VEHICLESTOPPAGE.getId();
			// Not required revocation is auto approve.
			if (service.equalsIgnoreCase(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getCode())) {
				serviceId = ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId();
			}
			ApprovalProcessFlowDTO approvalFlowDTO = registratrionServicesApprovals
					.getApprovalProcessFlowDTOForLock(role, serviceId);
			List<RegServiceDTO> regServiceAllRecordsDTOList = regServiceDAO
					.findByOfficeCodeAndServiceIdsInAndCurrentIndexAndSourceIsNull(officeCode, Arrays.asList(serviceId),
							approvalFlowDTO.getIndex());
			if (regServiceAllRecordsDTOList != null && !regServiceAllRecordsDTOList.isEmpty()) {
				regServiceAllRecordsDTOList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
				returnList.add(regServiceMapper
						.limitedDashBoardfields(regServiceAllRecordsDTOList.stream().findFirst().get()));
			}

			return returnList;
		}
	}

	@Override
	public String saveMviNameInStoppage(String applicationNo, String userId, UserDTO userDetails, String ipAddress,
			String role) {

		synchronized (applicationNo.intern()) {

			if (StringUtils.isBlank(userId)) {
				throw new BadRequestException("Please provide MVI user id for application: " + applicationNo);
			}
			Optional<RegServiceDTO> regSerDtoOpt = regServiceDAO.findByApplicationNo(applicationNo);
			if (!regSerDtoOpt.isPresent()) {
				throw new BadRequestException("Applicate Details Not Found with applicationNo " + applicationNo);
			}
			if (StringUtils.isEmpty(regSerDtoOpt.get().getMviOfficeCode())) {
				throw new BadRequestException("Office Details Not Found with applicationNo " + applicationNo);
			}
			Optional<UserDTO> userDto = userDAO.findByUserId(userId);
			if (userDto == null || !userDto.isPresent()) {
				throw new BadRequestException("No user details found for user name: " + userId);
			}
			if (!userDto.get().getOffice().getOfficeCode().equalsIgnoreCase(regSerDtoOpt.get().getMviOfficeCode())) {
				throw new BadRequestException("Please provide correct MVI: " + applicationNo);
			}
			if (!userDto.get().getPrimaryRole().getName().equalsIgnoreCase(RoleEnum.MVI.getName())
					&& !userDto.get().getAdditionalRoles().stream()
							.anyMatch(name -> name.getName().equalsIgnoreCase(RoleEnum.MVI.getName()))) {
				throw new BadRequestException("Given user dont have MVI role. user id: " + userId);
			}
			Set<String> rolels = new HashSet<>();
			rolels.add(RoleEnum.MVI.getName());
			if (regSerDtoOpt.get().getCurrentRoles() != null && !regSerDtoOpt.get().getCurrentRoles().isEmpty()
					&& regSerDtoOpt.get().getCurrentRoles().contains(RoleEnum.RTO.getName())) {
				Optional<ActionDetails> actionDetailsOpt = regSerDtoOpt.get().getActionDetails().stream()
						.filter(p -> role.equals(p.getRole())).findFirst();
				if (!actionDetailsOpt.isPresent()) {
					logger.error("User role [{}] specific details not found in action detail", role);
					throw new BadRequestException("User role " + role + " specific details not found in actiondetail");
				}
				regSerDtoOpt.get().setCurrentIndex(2);
				regSerDtoOpt.get().setCurrentRoles(rolels);
				this.updateActionDetailsStatus(role, userDetails.getUserId(),
						StatusRegistration.APPROVED.getDescription(), actionDetailsOpt.get(), applicationNo);
			}
			List<LockedDetailsDTO> lockedDetailsLog = new ArrayList<>();
			ActionDetails actions = new ActionDetails();
			if (regSerDtoOpt.get().getLockedDetails() != null && !regSerDtoOpt.get().getLockedDetails().isEmpty()
					&& regSerDtoOpt.get().getVehicleStoppageDetails().getRtoCompleted() == null) {
				actions.setFromMvi(regSerDtoOpt.get().getLockedDetails().stream().findFirst().get().getLockedBy());// userDetails.getUserId()
			}
			regSerDtoOpt.get().getVehicleStoppageDetails().setRtoCompleted(Boolean.TRUE);
			if (role.equalsIgnoreCase(RoleEnum.DTC.getName())) {
				regSerDtoOpt.get().setCurrentRoles(rolels);
			}
			regSerDtoOpt.get().getVehicleStoppageDetails().setMviAssignedDate(LocalDate.now());
			if (regSerDtoOpt.get().getVehicleStoppageDetails().getDtcAssignedDate() != null) {
				regSerDtoOpt.get().getVehicleStoppageDetails().setDtcAssignedDate(null);
			}
			regSerDtoOpt.get().getVehicleStoppageDetails().setUserId(userId);
			if (StringUtils.isNoneBlank(userDto.get().getMobile())) {
				regSerDtoOpt.get().getVehicleStoppageDetails().setMviNumber(userDto.get().getMobile());
			}

			actions.setToMvi(userId);// userDetails.getUserId()
			actions.setRole(role);
			actions.setUserId(userDetails.getUserId());
			actions.setlUpdate(LocalDateTime.now());
			actions.setIpAddress(ipAddress);
			if (regSerDtoOpt.get().getVehicleStoppageDetails().getActions() == null
					|| regSerDtoOpt.get().getVehicleStoppageDetails().getActions().isEmpty()) {
				regSerDtoOpt.get().getVehicleStoppageDetails().setActions(Arrays.asList(actions));
			} else {
				regSerDtoOpt.get().getVehicleStoppageDetails().getActions().add(actions);
			}
			LockedDetailsDTO lockedDetail = setLockedDetails(userId, RoleEnum.MVI.getName(), 1,
					regSerDtoOpt.get().getRegistrationDetails().getVehicleType(),
					regSerDtoOpt.get().getApplicationNo());
			lockedDetailsLog.add(lockedDetail);
			regSerDtoOpt.get().setLockedDetails(lockedDetailsLog);
			regSerDtoOpt.get().setIterationCount(regSerDtoOpt.get().getIterationCount() + 1);
			vehicleStoppageDetailsDAO.save(regSerDtoOpt.get().getVehicleStoppageDetails());
			regServiceDAO.save(regSerDtoOpt.get());
			return "success";
		}
	}

	@Override
	public List<RegServiceVO> returnStoppageList(String officeCode, String role, Optional<UserDTO> userDetails,
			String service) {
		List<RegServiceVO> list = new ArrayList<>();

		List<RegServiceDTO> listodServices = null;
		if (role.equalsIgnoreCase(RoleEnum.DTC.getName())) {
			listodServices = regServiceDAO.findByDtcOfficeCodeAndCurrentRolesInAndSourceIsNull(officeCode,
					Arrays.asList(role));
		} else if (role.equalsIgnoreCase(RoleEnum.RTO.getName())
				&& service.equalsIgnoreCase(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getCode())) {
			listodServices = regServiceDAO.findByOfficeCodeAndServiceIdsInAndCurrentIndexAndSourceIsNull(officeCode,
					Arrays.asList(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId()), 3);
			List<RegServiceDTO> listOfrevoc = regServiceDAO
					.findByOfficeCodeAndServiceIdsInAndCurrentIndexAndSourceIsNull(officeCode,
							Arrays.asList(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId()), 1);
			if (listOfrevoc != null && !listOfrevoc.isEmpty()) {
				if (listodServices != null && !listodServices.isEmpty()) {
					listodServices.addAll(listOfrevoc);
				} else {
					listodServices = listOfrevoc;
				}
			}
		} else {
			listodServices = regServiceDAO
					.findByLockedDetailsLockedByAndLockedDetailsLockedByRole(userDetails.get().getUserId(), role);
		}
		Integer serviceId = ServiceEnum.VEHICLESTOPPAGE.getId();
		// Not required revocation is auto approve.
		if (service.equalsIgnoreCase(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getCode())) {
			serviceId = ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId();
		}
		List<Integer> serviceIds = Arrays.asList(serviceId);

		if (listodServices != null && !listodServices.isEmpty()) {
			List<RegServiceDTO> dtolist = listodServices.stream()
					.filter(reg -> serviceIds.stream().anyMatch(sid -> reg.getServiceIds().contains(sid)))
					.collect(Collectors.toList());
			if (dtolist != null && !dtolist.isEmpty()) {
				for (RegServiceDTO dto : dtolist) {
					RegServiceVO vo = regServiceMapper.convertEntity(dto);
					if (service.equalsIgnoreCase(ServiceEnum.VEHICLESTOPPAGE.getCode())) {
						List<VehicleStoppageMVIReportVO> reportvo = registratrionServicesApprovals
								.getPendingQuarters(dto);
						vo.getVehicleStoppageDetailsVO().setDaysLeftForAutoApprove(0l);
						if (reportvo != null && !reportvo.isEmpty()) {
							Long leftDays = registratrionServicesApprovals.daysLeftForAutoapproved(dto);
							vo.getVehicleStoppageDetailsVO().setDaysLeftForAutoApprove(leftDays);
							vo.getVehicleStoppageDetailsVO().setPendingcurrentQuarter(Boolean.TRUE);
						}
					} else if (service.equalsIgnoreCase(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getCode())) {
						Optional<CitizenEnclosuresDTO> enclosures = citizenEnclosuresDAO
								.findByApplicationNo(dto.getApplicationNo());
						if (enclosures.isPresent()) {
							List<InputVO> map = new ArrayList<InputVO>();
							registratrionServicesApprovals.mapStoppageRevocationImages(enclosures.get(), map);
							vo.getVehicleStoppageDetailsVO().setQrCodeRequired(Boolean.TRUE);
							vo.getVehicleStoppageDetailsVO().setCurrentQuarterImageUrls(map);
						}
					}
					list.add(vo);
				}
			}
		}
		return list;
	}

	private void incrementToNextIndex(RegServiceDTO regServiceDTO, ActionDetails actionDetail, String role) {
		Optional<ActionDetails> parlleActionDetails = regServiceDTO.getActionDetails().stream()
				.filter(p -> (!role.equals(p.getRole()) && p.getIndex().equals(actionDetail.getIndex()))).findFirst();
		if (!parlleActionDetails.isPresent()
				|| (parlleActionDetails.get().getIsDoneProcess()
						&& parlleActionDetails.get().getIsDoneOnlyPartially() == null)
				|| (parlleActionDetails.get().getIsDoneOnlyPartially() != null
						&& !parlleActionDetails.get().getIsDoneOnlyPartially())) {
			regServiceDTO.setCurrentIndex(actionDetail.getNextIndex());
			setCurrentRole(regServiceDTO);
		}
	}

	private void setCurrentRole(RegServiceDTO regServiceDTO) {
		if (1 == regServiceDTO.getIterationCount() || null != regServiceDTO.getCurrentRoles()) {
			regServiceDTO.setCurrentRoles(regServiceDTO.getActionDetails().stream()
					.filter(a -> !a.getIsDoneProcess() && regServiceDTO.getCurrentIndex().equals(a.getIndex()))
					.map(a -> a.getRole()).collect(Collectors.toCollection(LinkedHashSet::new)));
		}
	}

	@Override
	public List<RegServiceVO> getRcCancellationPendingListForMvi(String user, String role) {
		List<RegServiceVO> returnList = new ArrayList<>();
		List<RegServiceDTO> regServiceDTOList = regServiceDAO
				.findFirst10ByLockedDetailsLockedByAndLockedDetailsLockedByRoleOrderByCreatedDateAsc(user, role);
		if (regServiceDTOList != null && CollectionUtils.isNotEmpty(regServiceDTOList)) {
			regServiceDTOList.stream().forEach(dto -> {
				if (CollectionUtils.isNotEmpty(dto.getServiceIds())
						&& dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RCCANCELLATION.getId()))
						&& dto.getFlowId() != null
						&& dto.getFlowId().equals(ServiceEnum.Flow.RCCANCELLATIONEMVIACTION)) {
					returnList.add(regServiceMapper.limitedDashBoardfields(dto));
				}
			});
		}
		return returnList;
	}

	@Override
	public Optional<List<MasterRcCancellationQuestionsVO>> getMVIQuestionForRcCancellation(String selectedRole) {
		List<MasterRcCancellationQuestionsDTO> rcQuestionList = masterRcCancellationQuestionsDAO.findAll();
		return Optional.of(rcCancellationQuestionsMappers.convertEntity(rcQuestionList));
	}

	private void checkvcrClosedOrNot(List<VcrFinalServiceDTO> vcrList, VcrFinalServiceDTO vcdDoc) {
		if (vcdDoc.getPartiallyClosed() == null || !vcdDoc.getPartiallyClosed()) {
			if (vcdDoc.getSeizedAndDocumentImpounded() != null
					&& vcdDoc.getSeizedAndDocumentImpounded().getVehicleSeizedDTO() != null
					&& vcdDoc.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().getDateOfSeized() != null
					&& (vcdDoc.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().isDepartmentAction()
							|| vcdDoc.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().isCourtOrder())) {
				logger.error(
						"VCR payment not done. To pay amount in online, Please chenge relase type as release order :",
						vcdDoc.getVcr().getVcrNumber());
				throw new BadRequestException(
						"VCR payment not done. To pay amount in online, Please chenge relase type as release order :"
								+ vcdDoc.getVcr().getVcrNumber());
			} else {
				if (vcrList.size() == 1 && vcrList.stream().findFirst().get().getOffence().getOffence().size() == 1) {
					OffenceDTO single = vcrList.stream().findFirst().get().getOffence().getOffence().stream()
							.findFirst().get();
					if (single.isShouldNotClose()) {
						return;
					}
				}
			}

			logger.error("VCR payment not done Please pay vcr payment first ", vcdDoc.getVcr().getVcrNumber());
			throw new BadRequestException(
					"VCR payment not done Please pay vcr payment first " + vcdDoc.getVcr().getVcrNumber());
		} else if (vcrList.size() == 1 && vcrList.stream().findFirst().get().getOffence().getOffence().size() == 1) {

		}
	}

	@Override
	public String doActionForRcCancellation(RegServiceVO regServiceVO, JwtUser jwtUser, MultipartFile[] uploadfiles)
			throws IOException {
		Optional<RegServiceDTO> regServOptional = regServiceDAO.findByApplicationNo(regServiceVO.getApplicationNo());
		if (!regServOptional.isPresent()) {
			logger.error("No records found for prNo  [{}]", regServiceVO.getPrNo());
			throw new BadRequestException("No records found for prNo " + regServiceVO.getPrNo());
		}
		RegServiceDTO regDto = regServOptional.get();
		if (regDto.getFlowId() != null && regDto.getFlowId().equals(ServiceEnum.Flow.RCCANCELLATIONCCO)) {
			logger.error("Application Approval pending at RTO level for PrNo [{}]", regDto.getPrNo());
			throw new BadRequestException("Application Approval pending at RTO level for PrNo " + regDto.getPrNo());
		}
		if (regDto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RCCANCELLATION.getId()))
				&& regDto.getFlowId() != null && regDto.getFlowId().equals(ServiceEnum.Flow.RCCANCELLATIONEMVIACTION)
				&& regDto.getCurrentRoles().stream().anyMatch(role -> role.equals(RoleEnum.MVI.getName()))) {
			RtaActionVO vo = new RtaActionVO();
			RCCancellationActionVO rcActionVO = new RCCancellationActionVO();
			if (StringUtils.isNoneEmpty(regServiceVO.getRcCancellation().getMviRemarks())) {
				rcActionVO.setMviRemarks(regServiceVO.getRcCancellation().getMviRemarks());
			}
			vo.setApplicationNo(regServiceVO.getApplicationNo());
			vo.setRcQuesList(regServiceVO.getRcQuesList());
			vo.setImages(regServiceVO.getImages());
			vo.setSelectedRole(regServiceVO.getSelectedRole());
			vo.setAction(regServiceVO.getApplicationStatus().toString());
			vo.setStatus(regServiceVO.getApplicationStatus());
			vo.setRcCancellationAction(rcActionVO);
			registratrionServicesApprovals.approvalProcess(jwtUser, vo, regServiceVO.getSelectedRole(), uploadfiles);
			return "success";
		} else {
			throw new BadRequestException("Application is approved at MVI level");
		}

	}

	@Override
	public List<RegServiceVO> getRcCancellationPendingListForAOAndRTO(String officeCode, String role) {
		List<RegServiceVO> returnList = new ArrayList<>();
		final String localOfficeCode = officeCode;
		synchronized (localOfficeCode.intern()) {
			ApprovalProcessFlowDTO approvalFlowDTO = registratrionServicesApprovals
					.getApprovalProcessFlowDTOForLock(role, ServiceEnum.Flow.RCCANCELLATIONEMVIACTION.getId());
			List<RegServiceDTO> regServList = regServiceDAO
					.findByOfficeCodeAndServiceIdsInAndCurrentIndexAndSourceIsNull(officeCode,
							Arrays.asList(ServiceEnum.RCCANCELLATION.getId()), approvalFlowDTO.getIndex());
			if (regServList != null && !regServList.isEmpty()) {
				regServList.sort((p1, p2) -> p1.getCreatedDate().compareTo(p1.getCreatedDate()));
				returnList.add(regServiceMapper.limitedDashBoardfields(regServList.stream().findFirst().get()));

			}
		}
		return returnList;
	}

	@Override
	public String saveRcCancellationDetailsForOtherState(RegServiceVO regVO, MultipartFile[] uploadfiles,
			JwtUser jwtUser) {
		RegServiceVO registrationDetails = registrationService.findByprNo(regVO.getPrNo());
		if (registrationDetails != null && registrationDetails.getRcCancellation() != null
				&& registrationDetails.getServiceType().contains(ServiceEnum.RCCANCELLATION)) {
			logger.debug("RC CANCELLATION Request is Pending " + "[{}]", regVO.getPrNo());
			throw new BadRequestException("RC CANCELLATION Request is Pending " + regVO.getPrNo());
		}
		RegServiceDTO regServiceDTO = new RegServiceDTO();
		RCCancellationDTO rcCancellationDTO = new RCCancellationDTO();
		regServiceDTO = addRcCancellationDetailsForOtherState(regVO, regServiceDTO, rcCancellationDTO, jwtUser);
		registratrionServicesApprovals.initiateApprovalProcessFlow(regServiceDTO);
		ActionDetails actionDetail = getActionDetailByRole(regServiceDTO, RoleEnum.CCO.getName());
		updateActionDetailsStatus(RoleEnum.CCO.getName(), jwtUser.getId(), StatusRegistration.APPROVED.toString(),
				actionDetail, regServiceDTO.getApplicationNo());
		incrementToNextIndex(regServiceDTO, actionDetail, RoleEnum.CCO.getName());
		if (null != regVO.getImages() && !regVO.getImages().isEmpty()) {
			regVO.setImageInput(regVO.getImages());
		}
		try {
			registrationServiceImpl.saveImages(regVO, regServiceDTO, uploadfiles);
		} catch (IOException e) {
			logger.debug("Exception Occured [{}]", e.getMessage());
			logger.error("Exception Occured [{}]", e);
		}
		regServiceDAO.save(regServiceDTO);
		return regServiceDTO.getApplicationNo();

	}

	private RegServiceDTO addRcCancellationDetailsForOtherState(RegServiceVO regVO, RegServiceDTO regServiceDTO,
			RCCancellationDTO rcCancellationDTO, JwtUser jwtUser) {
		regServiceDTO = regServiceMapper.convertVO(regVO);
		rcCancellationDTO.setReasonForCancellation(regVO.getRcCancellation().getReasonForCancellation());
		rcCancellationDTO.setIsCancelledByCitizen(false);
		regServiceDTO.setServiceIds(Stream.of(ServiceEnum.RCCANCELLATION.getId()).collect(Collectors.toSet()));
		regServiceDTO.setServiceType(Arrays.asList(ServiceEnum.RCCANCELLATION));
		regServiceDTO.setFlowId(ServiceEnum.Flow.RCCANCELLATIONCCO);
		regServiceDTO.setRcCancellation(rcCancellationDTO);
		Optional<OfficeDTO> officeOpt = officeDAO.findByOfficeCode(jwtUser.getOfficeCode());
		regServiceDTO.setOfficeDetails(officeOpt.get());
		regServiceDTO.setMviOfficeDetails(officeOpt.get());
		regServiceDTO.setOfficeCode(regServiceDTO.getOfficeDetails().getOfficeCode());
		regServiceDTO.setMviOfficeCode(regServiceDTO.getOfficeDetails().getOfficeCode());
		if (null == regServiceDTO.getApplicationNo()) {
			Map<String, String> officeCodeMap = new TreeMap<>();
			officeCodeMap.put("officeCode", regServiceDTO.getOfficeCode());
			regServiceDTO.setApplicationNo(sequenceGenerator
					.getSequence(String.valueOf(Sequence.REGISTRATIONAPPNO.getSequenceId()), officeCodeMap));
		}
		regServiceDTO.setApplicationStatus(StatusRegistration.INITIATED);
		regServiceDTO.setCreatedDate(LocalDateTime.now());
		return regServiceDTO;

	}

	@Override
	public List<DealerRegVO> dealerReistrationPendingList(String officeCode, String role, String service,
			UserDTO userDetails) {
		List<DealerRegVO> returnList = new ArrayList<>();
		List<DealerRegDTO> regServiceAllRecordsDTOList = new ArrayList<>();
		final String localOfficeCode = officeCode;

		synchronized (localOfficeCode.intern()) {

			if (StringUtils.isBlank(service)) {
				logger.error("Service type missing for vehicle stoppage");
				throw new BadRequestException("Service type missing for vehicle stoppage");
			}
			if (role.equalsIgnoreCase(RoleEnum.DTC.getName())) {

				List<String> officeCodesList = getOfficeCodesBasedOnDistrict(officeCode);
				List<StatusRegistration> statusList = new ArrayList<>();
				statusList.add(StatusRegistration.REJECTED);
				statusList.add(StatusRegistration.PAYMENTSUCCESS);

				if (service.equalsIgnoreCase(DynamicmenusEnum.NEWDEALERSHIP.getDescription())) {
					regServiceAllRecordsDTOList = dealerRegDAO
							.findByOfficeCodeInAndServiceIdsInAndApplicationStatusInAndIsMVIDoneFalse(officeCodesList,
									Arrays.asList(ServiceEnum.DEALERREGISTRATION.getId()),
									Arrays.asList(StatusRegistration.PAYMENTSUCCESS));
				} else if (service.equalsIgnoreCase(DynamicmenusEnum.MVIRECOMMENDED.getDescription())) {
					regServiceAllRecordsDTOList = dealerRegDAO
							.findByOfficeCodeInAndServiceIdsInAndApplicationStatusInAndIsMVIDoneTrue(officeCodesList,
									Arrays.asList(ServiceEnum.DEALERREGISTRATION.getId(),
											ServiceEnum.DEALERSHIPRENEWAL.getId()),
									Arrays.asList(StatusRegistration.MVIAPPROVED));
				} else if (service.equalsIgnoreCase(DynamicmenusEnum.MVINOTRECOMMENDED.getDescription())) {
					regServiceAllRecordsDTOList = dealerRegDAO
							.findByOfficeCodeInAndServiceIdsInAndApplicationStatusInAndIsMVIDoneTrue(officeCodesList,
									Arrays.asList(ServiceEnum.DEALERREGISTRATION.getId(),
											ServiceEnum.DEALERSHIPRENEWAL.getId()),
									Arrays.asList(StatusRegistration.MVIREJECTED));
				} else if (service.equalsIgnoreCase(DynamicmenusEnum.REJECTED.getDescription())) {
					regServiceAllRecordsDTOList = dealerRegDAO
							.findByOfficeCodeInAndServiceIdsInAndApplicationStatusInAndIsMVIDoneFalse(officeCodesList,
									Arrays.asList(ServiceEnum.DEALERREGISTRATION.getId(),
											ServiceEnum.DEALERSHIPRENEWAL.getId()),
									Arrays.asList(StatusRegistration.REJECTED));
				}

				else if (service.equalsIgnoreCase(DynamicmenusEnum.RENEWALOFDEALERSHIP.getDescription())) {
					regServiceAllRecordsDTOList = dealerRegDAO
							.findByOfficeCodeInAndServiceIdsInAndApplicationStatusInAndIsMVIDoneFalse(officeCodesList,
									Arrays.asList(ServiceEnum.DEALERSHIPRENEWAL.getId()),
									Arrays.asList(StatusRegistration.PAYMENTSUCCESS));
				}

				else {
					regServiceAllRecordsDTOList = dealerRegDAO
							.findByOfficeCodeInAndServiceIdsInAndApplicationStatusInAndIsMVIDoneFalse(officeCodesList,
									Arrays.asList(ServiceEnum.DEALERREGISTRATION.getId()),
									Arrays.asList(StatusRegistration.PAYMENTSUCCESS));

				}
				if (regServiceAllRecordsDTOList != null && !regServiceAllRecordsDTOList.isEmpty()) {
					regServiceAllRecordsDTOList.sort((p2, p1) -> p2.getlUpdate().compareTo(p1.getlUpdate()));
					returnList.addAll(dealerRegMapper.convertEntity(regServiceAllRecordsDTOList));
				}

			} else if (role.equalsIgnoreCase(RoleEnum.MVI.getName())) {
				regServiceAllRecordsDTOList = dealerRegDAO
						.findByLockedDetailsLockedByAndLockedDetailsLockedByRoleAndIsMVIDoneFalse(
								userDetails.getUserId(), RoleEnum.MVI.getName());
				if (regServiceAllRecordsDTOList != null && !regServiceAllRecordsDTOList.isEmpty()) {
					regServiceAllRecordsDTOList.sort((p1, p2) -> p2.getlUpdate().compareTo(p1.getlUpdate()));
					returnList.addAll(dealerRegMapper.convertEntity(regServiceAllRecordsDTOList));
				}
			}
			return returnList;
		}
	}

	private List<String> getOfficeCodesBasedOnDistrict(String officeCode) {
		Optional<OfficeDTO> distict = officeDAO.findByOfficeCode(officeCode);
		List<OfficeDTO> officeList = officeDAO.findBydistrict(distict.get().getDistrict());
		return officeList.stream().map(office -> office.getOfficeCode()).collect(Collectors.toList());
	}

	@Autowired
	private DealerRegDAO dealerRegDAO;

	@Autowired
	private DealerRegMapper dealerRegMapper;

	@Override
	public List<UserVO> getMVInamesBasedOnMandal(String applicationNo) {

		Optional<DealerRegDTO> regSerDtoOpt = dealerRegDAO.findByApplicationNo(applicationNo);
		if (!regSerDtoOpt.isPresent()) {
			throw new BadRequestException("Applicate Details Not Found with applicationNo " + applicationNo);
		}
		if (StringUtils.isEmpty(regSerDtoOpt.get().getOfficeDetails().getOfficeCode())) {
			throw new BadRequestException("Office Details Not Found with applicationNo " + applicationNo);
		}
		List<UserDTO> userList = userDAO.findByPrimaryRoleNameOrAdditionalRolesNameAndOfficeOfficeCodeNative(
				RoleEnum.MVI.name(), RoleEnum.MVI.name(), regSerDtoOpt.get().getOfficeDetails().getOfficeCode());
		if (CollectionUtils.isNotEmpty(userList)) {
			/*
			 * List<UserDTO> filterList = userList.stream().filter(p -> p.getFirstName() !=
			 * null) .collect(Collectors.toList());
			 */
			// if (CollectionUtils.isNotEmpty(filterList)) {
			userList.stream().map(val -> val.getFirstName()).collect(Collectors.toList());
			return userMapper.convertlimittedList(userList);
			// }
		}
		return Collections.emptyList();

	}

	@Override
	public String assignMVIforDealerRegistration(String applicationNo, String userId, UserDTO userDTO,
			String remoteAddr, String role) {

		synchronized (applicationNo.intern()) {

			if (StringUtils.isBlank(userId)) {
				throw new BadRequestException("Please provide MVI user id for application: " + applicationNo);
			}
			Optional<DealerRegDTO> dealerRegDTO = dealerRegDAO.findByApplicationNo(applicationNo);
			if (!dealerRegDTO.isPresent()) {
				throw new BadRequestException("Applicate Details Not Found with applicationNo " + applicationNo);
			}

			if (dealerRegDTO.get().getActionDetails() == null) {
				dealerRegistrationService.initiateApprovalProcessFlow(dealerRegDTO.get());
			}

			Optional<UserDTO> userDto = userDAO.findByUserId(userId);
			if (userDto == null || !userDto.isPresent()) {
				throw new BadRequestException("No user details found for user name: " + userId);
			}

			if (!userDto.get().getPrimaryRole().getName().equalsIgnoreCase(RoleEnum.MVI.getName())
					&& !userDto.get().getAdditionalRoles().stream()
							.anyMatch(name -> name.getName().equalsIgnoreCase(RoleEnum.MVI.getName()))) {
				throw new BadRequestException("Given user dont have MVI role. user id: " + userId);
			}
			Set<String> rolels = new HashSet<>();
			rolels.add(RoleEnum.MVI.getName());
			if (dealerRegDTO.get().getCurrentRoles() != null && !dealerRegDTO.get().getCurrentRoles().isEmpty()
					&& dealerRegDTO.get().getCurrentRoles().contains(RoleEnum.DTC.getName())) {
				Optional<ActionDetails> actionDetailsOpt = dealerRegDTO.get().getActionDetails().stream()
						.filter(p -> role.equals(p.getRole())).findFirst();
				if (!actionDetailsOpt.isPresent()) {
					logger.error("User role [{}] specific details not found in action detail", role);
					throw new BadRequestException("User role " + role + " specific details not found in actiondetail");
				}
				dealerRegDTO.get().setCurrentIndex(2);
				dealerRegDTO.get().setCurrentRoles(rolels);
				this.updateActionDetailsStatus(role, userDTO.getUserId(), "DONE", actionDetailsOpt.get(),
						applicationNo);
			}
			List<LockedDetailsDTO> lockedDetailsLog = new ArrayList<>();
			ActionDetails actions = new ActionDetails();
			if (dealerRegDTO.get().getLockedDetails() != null && !dealerRegDTO.get().getLockedDetails().isEmpty()) {
				actions.setFromMvi(dealerRegDTO.get().getLockedDetails().stream().findFirst().get().getLockedBy());// userDetails.getUserId()
			}
			if (role.equalsIgnoreCase(RoleEnum.DTC.getName())) {
				dealerRegDTO.get().setCurrentRoles(rolels);
			}
			dealerRegDTO.get().setMviAssignedDate(LocalDateTime.now());
			actions.setToMvi(userId);
			actions.setRole(role);
			actions.setUserId(userDTO.getUserId());
			actions.setlUpdate(LocalDateTime.now());
			actions.setIpAddress(remoteAddr);

			LockedDetailsDTO lockedDetail = setLockedDetails(userId, RoleEnum.MVI.getName(), 1,
					ServiceEnum.DEALERREGISTRATION.getDesc(), dealerRegDTO.get().getApplicationNo());
			lockedDetailsLog.add(lockedDetail);
			dealerRegDTO.get()
					.setMviOfficeDetails(officeDAO.findByOfficeCode(userDto.get().getOffice().getOfficeCode()).get());
			dealerRegDTO.get().setMviOfficeCode(
					officeDAO.findByOfficeCode(userDto.get().getOffice().getOfficeCode()).get().getOfficeCode());
			dealerRegDTO.get().setLockedDetails(lockedDetailsLog);
			dealerRegDTO.get().setIsMVIassigned(Boolean.TRUE);
			dealerRegDAO.save(dealerRegDTO.get());
			return "success";
		}
	}

	private void setTaxValididtyForRcCancellation(RegServiceDTO regServiceDTO) {
		if (StringUtils.isNoneEmpty(regServiceDTO.getPrNo())) {
			List<TaxDetailsDTO> taxList = taxDetailsDAO
					.findFirst10ByPrNoAndPaymentPeriodInOrderByCreatedDateDesc(regServiceDTO.getPrNo(), taxTypes());
			if (!taxList.isEmpty()) {
				Optional<TaxDetailsDTO> taxDetailsDto = taxList.stream().findFirst();
				regServiceDTO.setTaxType(taxDetailsDto.get().getPaymentPeriod());
				regServiceDTO.setTaxvalidity(taxDetailsDto.get().getTaxPeriodEnd());
			}
		}

	}

	private void setPermitDetailsForRcCancellation(RegServiceDTO regServiceDTO) {
		if (StringUtils.isNoneEmpty(regServiceDTO.getPrNo())) {
			Optional<PermitDetailsDTO> primaryPermitDetailsOpt = permitDetailsDAO
					.findByPrNoAndPermitStatusAndPermitTypeTypeofPermitOrderByCreatedDateDesc(regServiceDTO.getPrNo(),
							PermitsEnum.ACTIVE.getDescription(), PermitType.PRIMARY.getPermitTypeCode());
			if (primaryPermitDetailsOpt.isPresent()) {
				regServiceDTO.setPdtl(primaryPermitDetailsOpt.get());
			}
		}
	}

	@Override
	public VcrFinalServiceDTO checkVehicleTrNotGenerated(List<VcrFinalServiceDTO> vcrList) {
		List<VcrFinalServiceDTO> vcrDtosList = vcrList.stream()
				.filter(sizedDetails -> sizedDetails.getSeizedAndDocumentImpounded() != null
						&& sizedDetails.getSeizedAndDocumentImpounded().getVehicleSeizedDTO() != null
						&& sizedDetails.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().getDateOfSeized() != null)
				.collect(Collectors.toList());
		if (vcrDtosList != null && !vcrDtosList.isEmpty()) {
			vcrDtosList.sort((p1, p2) -> p1.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().getDateOfSeized()
					.compareTo(p2.getSeizedAndDocumentImpounded().getVehicleSeizedDTO().getDateOfSeized()));
			VcrFinalServiceDTO vcrDto = vcrDtosList.stream().findFirst().get();
			if (vcrDto.getRegistration().isOtherState() && vcrDto.getRegistration().isUnregisteredVehicle()
					&& StringUtils.isBlank(vcrDto.getRegistration().getTrNo())) {
				return vcrDto;
			} else if (!vcrDto.getRegistration().isOtherState() && vcrDto.getRegistration().isUnregisteredVehicle()
					&& StringUtils.isBlank(vcrDto.getRegistration().getRegApplicationNo())) {
				return vcrDto;
			}

		}
		return null;
	}

	private void saveImagesForVCr(VcrFinalServiceDTO vcrDto, List<ImageInput> files, String type) {
		for (ImageInput file : files) {
			if (type.equalsIgnoreCase(file.getType())) {
				byte[] imageByte = Base64.getDecoder().decode(file.getUrl().getBytes());
				MultipartFile[] multipartFile = new MultipartFile[1];
				multipartFile[0] = new BASE64DecodedMultipartFile(imageByte);

				try {
					List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = gridFsClient.convertImages(
							Arrays.asList(file), vcrDto.getVcr().getVcrNumber(), multipartFile,
							StatusRegistration.APPROVED.getDescription());
					if (vcrDto.getOtherSections() != null && !vcrDto.getOtherSections().isEmpty()) {
						for (OtherSectionDTO otherSectionDto : vcrDto.getOtherSections()) {
							if (otherSectionDto.getEnclosures() != null && !otherSectionDto.getEnclosures().isEmpty()) {
								otherSectionDto.getEnclosures().addAll(enclosures);
							} else {
								otherSectionDto.setEnclosures(enclosures);
							}
						}
					} else {
						List<OtherSectionDTO> list = new ArrayList<OtherSectionDTO>();
						OtherSectionDTO otherSectionDto = new OtherSectionDTO();
						otherSectionDto.setEnclosures(enclosures);
						list.add(otherSectionDto);
						vcrDto.setOtherSections(list);
					}
				} catch (IOException e) {
					logger.error("Some thing went wrong while saving images: ", e.getMessage());
					throw new BadRequestException("Some thing went wrong while saving images: " + e.getMessage());

				}
				break;
			}
		}

	}

	@Override
	public Optional<UserVO> getDealerDetailsByUserIdOrName(String userId, String dealerName) {
		if (StringUtils.isEmpty(userId) && StringUtils.isEmpty(dealerName)) {
			throw new BadRequestException("Required input parameters are missing");
		}
		Optional<UserDTO> userDto = null;
		if (StringUtils.isNotEmpty(userId)) {
			userDto = userDAO.findByUserId(userId);
		} else if (StringUtils.isNotEmpty(dealerName)) {
			userDto = userDAO.findByFirstNameIgnoreCase(dealerName);
		}
		if (StringUtils.isNotEmpty(dealerName) && !userDto.isPresent()) {
			userDto = userDAO.findByFirstnameIgnoreCase(dealerName);
		}
		if (!userDto.isPresent()) {
			throw new BadRequestException(
					"No records found with this user id or deler name " + userId + " " + dealerName);
		}

		return userMapper.convertEntity(userDto);
	}

	@Override
	public List<String> getListOfDealersByOfficeCode(JwtUser jwtUser) {
		List<OfficeDTO> officesList = officeDAO.findByReportingoffice(jwtUser.getOfficeCode());
		List<String> officeCodesList = officesList.stream().map(val -> val.getOfficeCode())
				.collect(Collectors.toList());
		List<UserDTO> userList = userDAO.findByOfficeOfficeCodeInAndPrimaryRoleNameAndUserStatus(officeCodesList,
				RoleEnum.DEALER.getName(), UserStatusEnum.ACTIVE);
		if (userList.isEmpty()) {
			throw new BadRequestException("No records found with this office code " + jwtUser.getOfficeCode());
		}
		return userList.stream().map(val -> val.getUserId()).collect(Collectors.toList());
	}

	@Autowired
	private DealerRegistrationService dealerRegistrationService;

	@Override
	public void dealerSuspensionAndCancellation(DealerRegVO dealerRegVO, JwtUser jwtUser,
			HttpServletRequest httpRequest) {
		Optional<UserDTO> userDetails = userDAO.findByUserId(dealerRegVO.getDealerUserId());
		if (!userDetails.isPresent()) {
			throw new BadRequestException("No records found with this User Id " + dealerRegVO.getDealerUserId());
		}
		doValidateBeforeSuspensionAndCancellation(userDetails.get(), dealerRegVO.getDealerUserId());
		if (dealerRegVO.getActionStatus().equals(StatusRegistration.SUSPEND)) {
			userDetails.get().setStatus(Boolean.FALSE);
			userDetails.get().setSuspendedBy(jwtUser.getId());
			userDetails.get().setSuspendedFrom(dealerRegVO.getSuspendedFrom());
			userDetails.get().setSuspendedTo(dealerRegVO.getSuspendedTo());
			userDetails.get().setIpAddress(httpRequest.getRemoteAddr());
			userDetails.get().setUserStatus(UserStatusEnum.INACTIVE);
		} else if (dealerRegVO.getActionStatus().equals(StatusRegistration.CANCELED)) {
			userDetails.get().setStatus(Boolean.FALSE);
			userDetails.get().setCancelledBy(jwtUser.getId());
			userDetails.get().setUserStatus(UserStatusEnum.INACTIVE);
			userDetails.get().setIpAddress(httpRequest.getRemoteAddr());
		} else if (dealerRegVO.getActionStatus().equals(StatusRegistration.REVOKED)) {
			Optional<DealerActionDetailsDTO> suspensionRecord = dealerActionDetailsDAO
					.findByDealerUserIdAndApplicationStatusOrderByLUpdateDesc(dealerRegVO.getDealerUserId(),
							StatusRegistration.SUSPEND);
			if (!suspensionRecord.isPresent()) {
				throw new BadRequestException(
						"Suspesion details are not found with this dealer user id : " + dealerRegVO.getDealerUserId());
			}
			userDetails.get().setStatus(Boolean.TRUE);
			userDetails.get().setUserStatus(UserStatusEnum.ACTIVE);
			userDetails.get().setUpdatedBy(jwtUser.getId());
			userDetails.get().setIpAddress(httpRequest.getRemoteAddr());
			userDetails.get().setlUpdate(LocalDateTime.now());
		}
		dealerRegistrationService.saveDealerDetailsinDealerRegistrationCollection(userDetails.get(), dealerRegVO,
				jwtUser, httpRequest);
		userDAO.save(userDetails.get());
	}

	@Autowired
	private DealerActionDetailsDAO dealerActionDetailsDAO;

	private void doValidateBeforeSuspensionAndCancellation(UserDTO userDetails, String dealerUserId) {
		Optional<DealerActionDetailsDTO> actionDetails = dealerActionDetailsDAO
				.findByDealerUserIdOrderByLUpdateDesc(dealerUserId);
		if (actionDetails.isPresent()) {
			if (actionDetails.get().getApplicationStatus().equals(StatusRegistration.SUSPEND)) {
				if (userDetails.getSuspendedTo().isBefore(LocalDate.now())) {
					throw new BadRequestException("This Dealer is already suspended");
				}
			} else if (actionDetails.get().getApplicationStatus().equals(StatusRegistration.CANCELED)) {
				throw new BadRequestException("This Dealer is already Cancelled");
			}
		}
	}

	@Override
	public void setClassOfVehiclesForDealer(UserVO userVO) {
		List<DealerCovDTO> covDetails = dealerCovDAO.findByRIdAndStatusTrue(userVO.getRid());
		List<String> covCodes = new ArrayList<>();
		List<String> covCodesDescription = new ArrayList<>();
		if (CollectionUtils.isNotEmpty(covDetails)) {
			covCodes = covDetails.stream().map(val -> val.getCovId()).collect(Collectors.toList());
		}
		if (CollectionUtils.isNotEmpty(covCodes)) {
			List<MasterCovDTO> masterCovs = masterCovDAO.findByCovcodeInAndDealerCovTrue(covCodes);
			covCodesDescription = masterCovs.stream().map(val -> val.getCovdescription()).collect(Collectors.toList());
		}
		if (CollectionUtils.isNotEmpty(covCodesDescription)) {
			userVO.setClassOfVehicles(covCodesDescription);
		}
	}

	@Override
	public List<RegServiceVO> getStageCarriagePendingList(String officeCode, String role, String service) {

		List<RegServiceVO> returnList = new ArrayList<>();

		final String localOfficeCode = officeCode;

		synchronized (localOfficeCode.intern()) {

			if (StringUtils.isBlank(service)) {
				logger.error("Service type missing for Stage Carriage");
				throw new BadRequestException("Service type missing for Stage Carriage");
			}
			Integer serviceId = ServiceEnum.getServiceEnum(service).getId();
			ApprovalProcessFlowDTO approvalFlowDTO = registratrionServicesApprovals
					.getApprovalProcessFlowDTOForLock(role, serviceId);
			List<RegServiceDTO> regServiceAllRecordsDTOList = regServiceDAO
					.findByOfficeCodeAndServiceIdsInAndCurrentIndexAndSourceIsNull(officeCode, Arrays.asList(serviceId),
							approvalFlowDTO.getIndex());
			if (regServiceAllRecordsDTOList != null && !regServiceAllRecordsDTOList.isEmpty()) {
				regServiceAllRecordsDTOList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
				returnList.add(regServiceMapper
						.limitedDashBoardfields(regServiceAllRecordsDTOList.stream().findFirst().get()));
			}

			return returnList;
		}
	}

	@Override
	public CitizenSearchReportVO getStageCarriageByAppNO(JwtUser jwtUser, String applicationNo, String role) {
		synchronized (applicationNo.intern()) {
			CitizenSearchReportVO vo = new CitizenSearchReportVO();
			Optional<RegServiceDTO> optionalService = regServiceDAO.findByApplicationNo(applicationNo);
			if (!optionalService.isPresent()) {
				logger.error("No record found for application number: ", applicationNo);
				throw new BadRequestException("No record found for application number: " + applicationNo);
			}
			RegServiceDTO dto = optionalService.get();
			Optional<CitizenEnclosuresDTO> enclosresOptional = citizenEnclosuresDAO.findByApplicationNo(applicationNo);
			if (!enclosresOptional.isPresent()) {
				logger.error("Enclosures not found for application number: ", applicationNo);
				throw new BadRequestException("Enclosures not found for application number: " + applicationNo);
			}
			CitizenEnclosuresDTO enclosuresImg = enclosresOptional.get();
			List<KeyValue<String, List<ImageEnclosureDTO>>> enclousersTo = enclosuresImg.getEnclosures();
			List<KeyValue<EnclosureType, List<ImageVO>>> enclosuresInputList = enclosuresLogMapper
					.convertNewEnclosures(enclousersTo);
			vo.setImageList(enclosuresInputList);
			if (dto.getCurrentRoles() == null || dto.getCurrentRoles().isEmpty()
					|| !dto.getCurrentRoles().contains(role)) {
				logger.error("Employee role not matched: ", applicationNo);
				throw new BadRequestException("Employee role not matched: " + applicationNo);
			}
			vo.setPermitVehicle(regServiceMapper.convertEntity(dto));
			List<ActionDetailsVO> actionVo = actionsDetailsMapper.convertEntity(dto.getActionDetails());
			vo.getPermitVehicle().setActionDetailsVO(actionVo);
			if (dto.getServiceType().stream()
					.anyMatch(type -> type.equals(ServiceEnum.STAGECARRIAGEREPLACEMENTOFVEHICLE))) {
				vo.setNonPermitVehicle(regServiceMapper.convertEntity(dto));
				vo.getNonPermitVehicle().setActionDetailsVO(actionVo);
				Optional<RegistrationDetailsDTO> optinlaregDOc = registrationDetailDAO
						.findByPrNo(dto.getPdtl().getPermitVehiclePrNo());
				if (!optinlaregDOc.isPresent()) {
					logger.error("No record found for vehicle number: ", dto.getPdtl().getPermitVehiclePrNo());
					throw new BadRequestException(
							"No record found for vehicle number: " + dto.getPdtl().getPermitVehiclePrNo());
				}
				RegServiceDTO permitVehicleData = new RegServiceDTO();
				permitVehicleData.setRegistrationDetails(optinlaregDOc.get());
				vo.setPermitVehicle(regServiceMapper.convertEntity(permitVehicleData));

			}
			return vo;
		}
	}

	@Override
	public Integer dealerReistrationPendingListCount(String officeCode, String role, String service,
			UserDTO userDetails) {
		List<DealerRegVO> returnList = new ArrayList<>();
		final String localOfficeCode = officeCode;
		Integer serviceId = ServiceEnum.DEALERREGISTRATION.getId();
		synchronized (localOfficeCode.intern()) {
			if (role.equalsIgnoreCase(RoleEnum.MVI.getName())) {
				List<DealerRegDTO> regServiceAllRecordsDTOList = dealerRegDAO
						.findByLockedDetailsLockedByAndLockedDetailsLockedByRoleAndIsMVIDoneFalse(
								userDetails.getUserId(), RoleEnum.MVI.getName());
				if (regServiceAllRecordsDTOList != null && !regServiceAllRecordsDTOList.isEmpty()) {
					regServiceAllRecordsDTOList.sort((p1, p2) -> p2.getlUpdate().compareTo(p1.getlUpdate()));
					returnList.addAll(dealerRegMapper.convertEntity(regServiceAllRecordsDTOList));
				}
			}
			return returnList.size();
		}
	}

	@Override
	public List<UserVO> getMviNamesForAuction(String officeCode) {

		List<UserDTO> userList = userDAO.findByPrimaryRoleNameOrAdditionalRolesNameAndOfficeOfficeCodeNative(
				RoleEnum.MVI.name(), RoleEnum.MVI.name(), officeCode);
		if (CollectionUtils.isNotEmpty(userList)) {
			/*
			 * List<UserDTO> filterList = userList.stream().filter(p -> p.getFirstName() !=
			 * null) .collect(Collectors.toList());
			 */
			// if (CollectionUtils.isNotEmpty(filterList)) {
			userList.stream().map(val -> val.getFirstName()).collect(Collectors.toList());
			return userMapper.convertlimittedList(userList);
			// }
		}
		return Collections.emptyList();

	}

	@Override
	public String saveAuctionDetails(JwtUser jwtUser, String stringVo, MultipartFile[] uploadfiles,
			HttpServletRequest request) {
		Optional<AuctionDetailsVO> inputOptional = readValue(stringVo, AuctionDetailsVO.class);
		AuctionDetailsVO vo = inputOptional.get();
		if (StringUtils.isBlank(vo.getMviUserId())) {
			logger.error("Please select mvi name");
			throw new BadRequestException("Please select mvi name");
		}
		synchronized (vo.getMviUserId().intern()) {

			AuctionDetailsDTO dto = new AuctionDetailsDTO();
			if (StringUtils.isBlank(vo.getDepartment())) {
				logger.error("Please provide department");
				throw new BadRequestException("Please provide department");
			}
			dto.setDepartment(vo.getDepartment());
			if (StringUtils.isBlank(vo.getNameOfRequester())) {
				logger.error("Please provide Name Of Requester");
				throw new BadRequestException("Please provide name of requester");
			}
			dto.setNameOfRequester(vo.getNameOfRequester());

			Optional<UserDTO> userDto = userDAO.findByUserId(vo.getMviUserId());
			if (!userDto.isPresent()) {
				logger.error("User details not found for usser id: " + vo.getMviUserId());
				throw new BadRequestException("User details not found for usser id: " + vo.getMviUserId());
			}
			dto.setMviUserId(vo.getMviUserId());
			dto.setMviName(userDto.get().getFirstname());
			dto.setMviOfficeCode(userDto.get().getOffice().getOfficeCode());
			Optional<UserDTO> DtcuserDto = userDAO.findByUserId(jwtUser.getUsername());
			if (!DtcuserDto.isPresent()) {
				logger.error("User details not found for usser id: " + jwtUser.getUsername());
				throw new BadRequestException("User details not found for usser id: " + jwtUser.getUsername());
			}
			dto.setDtcName(DtcuserDto.get().getFirstname());
			dto.setDtcOfficeCode(DtcuserDto.get().getOffice().getOfficeCode());
			dto.setDtcUserId(jwtUser.getUsername());
			if (vo.getNoOfVehicles() == null) {
				logger.error("Please provide Name Of vehicles");
				throw new BadRequestException("Please provide name of vehicles");
			}
			dto.setNoOfVehicles(vo.getNoOfVehicles());
			if (vo.getImageInput() == null || vo.getImageInput().isEmpty()) {
				logger.error("Please upload image");
				throw new BadRequestException("Please upload image");
			}
			dto.setId(String.valueOf(System.currentTimeMillis()));
			try {
				List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = gridFsClient.convertImages(
						vo.getImageInput(), dto.getId(), uploadfiles, StatusRegistration.APPROVED.getDescription());
				dto.setEnclosures(enclosures);
			} catch (IOException e) {
				logger.error("Some thing went wrong while saving images: ", e.getMessage());
				throw new BadRequestException("Some thing went wrong while saving images: " + e.getMessage());
			}

			dto.setIpAddress(request.getRemoteAddr());
			dto.setCreatedDate(LocalDateTime.now());
			dto.setlUpdate(LocalDateTime.now());
			this.setActionsForAuction(dto, "Create Auction", DtcuserDto.get(), RoleEnum.DTC.getName(),
					request.getRemoteAddr(), null);
			// dto.setAuctionClosed(true);
			auctionDetailsDAO.save(dto);
			return "Success";
		}

	}

	@Override
	public List<AuctionDetailsVO> getAuctionDetailsPendingList(String officeCode, String role, UserDTO userDetails) {

		synchronized (userDetails.getUserId().intern()) {
			if (!role.equalsIgnoreCase(RoleEnum.MVI.getName())) {
				logger.error("Only mvi can enter auction details.Please select role as MVI");
				throw new BadRequestException("Only mvi can enter auction details.Please select role as MVI");
			}
			List<AuctionDetailsDTO> auctionList = auctionDetailsDAO.findByMviOfficeCodeAndMviUserIdAndAuctionClosed(
					officeCode, userDetails.getUserId(), Boolean.FALSE);
			if (auctionList != null && !auctionList.isEmpty()) {
				return auctionDetailsMapper.convertEntity(auctionList);
			}
			return null;
		}
	}

	@Override
	public AuctionDetailsVO getAuctionDetailsByAppNo(String role, UserDTO userDetails, String applicationNo) {

		if (StringUtils.isBlank(applicationNo)) {
			logger.error("Application number not found");
			throw new BadRequestException("Application number not found");
		}
		synchronized (applicationNo.intern()) {
			if (!role.equalsIgnoreCase(RoleEnum.MVI.getName())) {
				logger.error("Only mvi can enter auction details.Please select role as MVI");
				throw new BadRequestException("Only mvi can enter auction details.Please select role as MVI");
			}
			Optional<AuctionDetailsDTO> auctionOptionals = auctionDetailsDAO.findById(applicationNo);
			if (!auctionOptionals.isPresent()) {
				logger.error("No records found for: " + applicationNo);
				throw new BadRequestException("No records found for: " + applicationNo);
			}
			AuctionDetailsDTO dto = auctionOptionals.get();
			if (!dto.getMviOfficeCode().equalsIgnoreCase(userDetails.getOffice().getOfficeCode())) {
				logger.error("Unauthorized user");
				throw new BadRequestException("Unauthorized user");
			}
			if (!dto.getMviUserId().equalsIgnoreCase(userDetails.getUserId())) {
				logger.error("Unauthorized user");
				throw new BadRequestException("Unauthorized user");
			}
			AuctionDetailsVO vo = auctionDetailsMapper.convertEntity(dto);
			if (vo.getVehicleDetails() == null) {
				List<AuctionVehicleDetailsVO> vehicleVo = new ArrayList<>();
				vo.setVehicleDetails(vehicleVo);
			}
			/*
			 * List<KeyValue<String, List<ImageEnclosureDTO>>> enclousersTo =
			 * dto.getEnclosures(); List<KeyValue<EnclosureType, List<ImageVO>>>
			 * enclosuresInputList = enclosuresLogMapper
			 * .convertNewEnclosures(enclousersTo); vo.setEnclosures(enclosuresInputList);
			 */
			return vo;
		}
	}

	// @Override
	public VCRVahanVehicleDetailsVO getRegDetailsForAuction(ApplicationSearchVO searchvo) {

		if (StringUtils.isBlank(searchvo.getSelectedRole())) {
			logger.error("selected role not found");
			throw new BadRequestException("selected role not found");
		}

		// synchronized (vo.getPrNo().intern()) {
		if (!searchvo.getSelectedRole().equalsIgnoreCase(RoleEnum.MVI.getName())) {
			logger.error("Only mvi can enter auction details.Please select role as MVI");
			throw new BadRequestException("Only mvi can enter auction details.Please select role as MVI");
		}
		VCRVahanVehicleDetailsVO vo = new VCRVahanVehicleDetailsVO();
		if (StringUtils.isNoneBlank(searchvo.getPrNo())) {
			RegistrationDetailsVO regVo = this.getRegDoc(searchvo.getPrNo());
			if (regVo != null) {
				vo.setRegDetailsVO(regVo);
				return vo;
			}

			return restGateWayService.getVahanVehicleDetailsForVcr(searchvo.getPrNo());
		} else if (StringUtils.isNoneBlank(searchvo.getTrNo())) {
			RegistrationDetailsVO regVo = this.getRegDocForTr(searchvo.getTrNo());
			if (regVo != null) {
				vo.setRegDetailsVO(regVo);
				return vo;
			}

		} else {
			logger.error("vehicle  number not found");
			throw new BadRequestException("vehicle number not found");
		}
		return vo;

		// }
	}

	@Override
	public String saveAuctionVehicleDetails(JwtUser jwtUser, String stringVo, MultipartFile[] uploadfiles,
			HttpServletRequest request, UserDTO userDetails) {
		Optional<AuctionDetailsVO> inputOptional = readValue(stringVo, AuctionDetailsVO.class);
		AuctionDetailsVO vo = inputOptional.get();
		if (StringUtils.isBlank(vo.getMviUserId())) {
			logger.error("Please select mvi name");
			throw new BadRequestException("Please select mvi name");
		}
		if (StringUtils.isBlank(vo.getId())) {
			logger.error("Application number not found to save vehicle details");
			throw new BadRequestException("Application number not found to save vehicle details");
		}
		synchronized (vo.getId().intern()) {

			Optional<AuctionDetailsDTO> auctionDtoOptional = auctionDetailsDAO.findById(vo.getId());
			if (!auctionDtoOptional.isPresent()) {
				logger.error("No record found for application number: " + vo.getId());
				throw new BadRequestException("No record found for application number: " + vo.getId());
			}

			AuctionDetailsDTO auctionDto = auctionDtoOptional.get();
			if (auctionDto.isAuctionClosed()) {
				logger.error("Auction closed for application number: " + vo.getId());
				throw new BadRequestException("Auction closed for application number: " + vo.getId());
			}
			if (vo.getVehicleDetails() == null || vo.getVehicleDetails().isEmpty()) {
				logger.error("Please provide vehicle details for application number: " + vo.getId());
				throw new BadRequestException("Please provide vehicle details for application number: " + vo.getId());
			}
			AuctionVehicleDetailsVO vehiclesDetailsVo = vo.getVehicleDetails().stream().findFirst().get();
			this.validtionForAuctionVehiclesDetailsSave(auctionDto, vehiclesDetailsVo, vo, userDetails);
			if (StringUtils.isNoneBlank(vehiclesDetailsVo.getPrNo())) {
				RegistrationDetailsVO regVo = this.getRegDoc(vehiclesDetailsVo.getPrNo());
				if (regVo == null) {
					vehiclesDetailsVo.setOtherState(Boolean.TRUE);
				} else {
					// TODO need to check pr canceled or not or noc issued or not
				}
			} else if (StringUtils.isNoneBlank(vehiclesDetailsVo.getTrNo())) {
				RegistrationDetailsVO regVo = this.getRegDocForTr(vehiclesDetailsVo.getTrNo());
				if (regVo == null) {
					vehiclesDetailsVo.setOtherState(Boolean.TRUE);
				} else {
					// TODO need to check pr canceled or not or noc issued or not
				}
			}

			AuctionVehicleDetailsDTO vehicleDto = auctionVehicleDetailsMapper.convertVO(vehiclesDetailsVo);

			try {
				List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = gridFsClient.convertImages(
						vo.getImageInput(), auctionDto.getId(), uploadfiles,
						StatusRegistration.APPROVED.getDescription());
				vehicleDto.setEnclosures(enclosures);
			} catch (IOException e) {
				logger.error("Some thing went wrong while saving images: ", e.getMessage());
				throw new BadRequestException("Some thing went wrong while saving images: " + e.getMessage());
			}

			vehicleDto.setIpAddress(request.getRemoteAddr());
			vehicleDto.setCreatedDate(LocalDateTime.now());
			vehicleDto.setlUpdate(LocalDateTime.now());
			if ((vehicleDto.getOtherState() == null || !vehicleDto.getOtherState())
					&& vehicleDto.getVehicleCondition().equals(TransferType.vehicleCondition.ROADWORTHY)
					&& vehicleDto.getGenuiness().equals(TransferType.genuiness.GENUINE)) {
				vehicleDto.setToken(String.valueOf(System.currentTimeMillis()));
			}
			if (auctionDto.getVehicleDetails() != null && !auctionDto.getVehicleDetails().isEmpty()) {
				auctionDto.getVehicleDetails().add(vehicleDto);
				if (auctionDto.getVehicleDetails().size() == auctionDto.getNoOfVehicles()) {
					auctionDto.setAuctionClosed(Boolean.TRUE);
				}
			} else {
				List<AuctionVehicleDetailsDTO> list = new ArrayList<>();
				list.add(vehicleDto);
				auctionDto.setVehicleDetails(list);
			}
			auctionDetailsDAO.save(auctionDto);
			return "Success";
		}
	}

	private void validtionForAuctionVehiclesDetailsSave(AuctionDetailsDTO auctionDto,
			AuctionVehicleDetailsVO vehiclesDetailsVo, AuctionDetailsVO vo, UserDTO userDetails) {
		if (!auctionDto.getMviUserId().equalsIgnoreCase(vo.getMviUserId())) {
			logger.error("Unauthorized user");
			throw new BadRequestException("Unauthorized user");
		}

		if (!auctionDto.getMviOfficeCode().equalsIgnoreCase(userDetails.getOffice().getOfficeCode())) {
			logger.error("Unauthorized user");
			throw new BadRequestException("Unauthorized user");
		}

		if (StringUtils.isBlank(vehiclesDetailsVo.getPrNo()) && StringUtils.isBlank(vehiclesDetailsVo.getTrNo())) {
			logger.error("Please provide Tr / pr number  for application number: " + vo.getId());
			throw new BadRequestException("Please provide Tr / pr number for application number: " + vo.getId());
		}
		if (auctionDto.getVehicleDetails() != null && !auctionDto.getVehicleDetails().isEmpty()) {
			if (StringUtils.isNoneBlank(vehiclesDetailsVo.getPrNo())) {
				if (auctionDto.getVehicleDetails().stream().anyMatch(prNo -> prNo.getPrNo() != null
						&& prNo.getPrNo().equalsIgnoreCase(vehiclesDetailsVo.getPrNo()))) {
					logger.error("vehicle number already entered. Please provide another vehicle number: "
							+ vehiclesDetailsVo.getPrNo());
					throw new BadRequestException(
							"vehicle number already entered. Please provide another vehicle number: "
									+ vehiclesDetailsVo.getPrNo());
				}
				// TODO need to check pr canceled or not
				List<AuctionDetailsDTO> oldRecords = auctionDetailsDAO
						.findByVehicleDetailsPrNoIn(vehiclesDetailsVo.getPrNo());
				if (oldRecords != null && !oldRecords.isEmpty()) {
					// TODO need to check tow close or not
					logger.error(
							"vehicle number already entered by some other MVI. Please provide another vehicle number: "
									+ vehiclesDetailsVo.getTrNo());
					throw new BadRequestException(
							"vehicle number already entered by some other MVI. Please provide another vehicle number: "
									+ vehiclesDetailsVo.getTrNo());
				}
			} else if (StringUtils.isNoneBlank(vehiclesDetailsVo.getTrNo())) {
				if (auctionDto.getVehicleDetails().stream().anyMatch(trNo -> trNo.getTrNo() != null
						&& trNo.getTrNo().equalsIgnoreCase(vehiclesDetailsVo.getTrNo()))) {
					logger.error("vehicle number already entered. Please provide another vehicle number: "
							+ vehiclesDetailsVo.getTrNo());
					throw new BadRequestException(
							"vehicle number already entered. Please provide another vehicle number: "
									+ vehiclesDetailsVo.getTrNo());
				}
				List<AuctionDetailsDTO> oldRecords = auctionDetailsDAO
						.findByVehicleDetailsTrNoIn(vehiclesDetailsVo.getTrNo());
				if (oldRecords != null && !oldRecords.isEmpty()) {
					// TODO need to check tow close or not
					logger.error(
							"vehicle number already entered by some other MVI. Please provide another vehicle number: "
									+ vehiclesDetailsVo.getTrNo());
					throw new BadRequestException(
							"vehicle number already entered by some other MVI. Please provide another vehicle number: "
									+ vehiclesDetailsVo.getTrNo());
				}
			}
			if (auctionDto.getVehicleDetails().size() >= auctionDto.getNoOfVehicles()) {
				logger.error("Limit of the vehicles exceeded: " + vo.getId());
				throw new BadRequestException("Limit of the vehicles exceeded: " + vo.getId());
			}

		}

		if (StringUtils.isBlank(vehiclesDetailsVo.getOfficeCode())) {
			logger.error("Please provide office code for application number: " + vo.getId());
			throw new BadRequestException("Please provide office code for application number: " + vo.getId());
		}
		if (StringUtils.isBlank(vehiclesDetailsVo.getOfficeName())) {
			logger.error("Please provide office name for application number: " + vo.getId());
			throw new BadRequestException("Please provide office name for application number: " + vo.getId());
		}
		if (StringUtils.isBlank(vehiclesDetailsVo.getVehicleType())) {
			logger.error("Please provide vehilce type for application number: " + vo.getId());
			throw new BadRequestException("Please provide vehicle type for application number: " + vo.getId());
		}
		if (StringUtils.isBlank(vehiclesDetailsVo.getClassOfVehicle())) {
			logger.error("Please provide class of vehicle code for application number: " + vo.getId());
			throw new BadRequestException("Please provide class of vehicle code for application number: " + vo.getId());
		}
		if (StringUtils.isBlank(vehiclesDetailsVo.getClassOfVehicleDesc())) {
			logger.error("Please provide class of vehicle  for application number: " + vo.getId());
			throw new BadRequestException("Please provide class of vehicle  for application number: " + vo.getId());
		}
		if (StringUtils.isBlank(vehiclesDetailsVo.getChassisNumber())) {
			logger.error("Please provide chassis number  for application number: " + vo.getId());
			throw new BadRequestException("Please provide chassis number  for application number: " + vo.getId());
		}
		if (StringUtils.isBlank(vehiclesDetailsVo.getEngineNumber())) {
			logger.error("Please provide engine number  for application number: " + vo.getId());
			throw new BadRequestException("Please provide engine number  for application number: " + vo.getId());
		}
		if (vehiclesDetailsVo.getVehicleCondition() == null) {
			logger.error("Please provide vehicle condition  for application number: " + vo.getId());
			throw new BadRequestException("Please provide vehicle condition  for application number: " + vo.getId());
		}
		if (vehiclesDetailsVo.getGenuiness() == null) {
			logger.error("Please provide genuiness  for application number: " + vo.getId());
			throw new BadRequestException("Please provide genuiness  for application number: " + vo.getId());
		}
		if (vehiclesDetailsVo.getUpSetPrice() == null || vehiclesDetailsVo.getUpSetPrice() <= 0) {
			logger.error("Please provide upset price  for application number: " + vo.getId());
			throw new BadRequestException("Please provide upset price  for application number: " + vo.getId());
		}
		if (vo.getImageInput() == null || vo.getImageInput().isEmpty()) {
			logger.error("Please upload image");
			throw new BadRequestException("Please upload image");
		}
	}

	private RegistrationDetailsVO getRegDoc(String prNo) {
		List<RegistrationDetailsDTO> regDetailsOptional = registrationDetailDAO.findAllByPrNo(prNo);
		if (regDetailsOptional != null && !regDetailsOptional.isEmpty()) {
			return regDetailMapper.convertEntity(regDetailsOptional.stream().findFirst().get());
		}
		return null;
	}

	private RegistrationDetailsVO getRegDocForTr(String trNo) {
		Optional<RegistrationDetailsDTO> regDetailsOptional = registrationDetailDAO.findByTrNo(trNo);
		if (regDetailsOptional.isPresent()) {
			return regDetailMapper.convertEntity(regDetailsOptional.get());
		}
		Optional<StagingRegistrationDetailsDTO> staging = stagingRegistrationDetailsDAO.findByTrNo(trNo);
		if (staging.isPresent()) {
			return regDetailMapper.convertEntity(staging.get());
		}
		return null;
	}

	@Override
	public List<AuctionDetailsVO> getAuctionDetailsPendingListForDTC(String officeCode, String role,
			UserDTO userDetails) {

		synchronized (userDetails.getUserId().intern()) {
			if (!role.equalsIgnoreCase(RoleEnum.DTC.getName())) {
				logger.error("Unauthorized user");
				throw new BadRequestException("Unauthorized user");
			}
			List<AuctionDetailsDTO> auctionList = auctionDetailsDAO
					.findByDtcOfficeCodeAndDtcUserIdAndAuctionClosedAndDtcCompleted(officeCode, userDetails.getUserId(),
							Boolean.TRUE, Boolean.FALSE);
			if (auctionList != null && !auctionList.isEmpty()) {
				return auctionDetailsMapper.convertEntity(auctionList);
			}
			return null;
		}
	}

	@Override
	public String saveAuctionDetailsByDtc(String applicationNo, String role, UserDTO userDetails,
			HttpServletRequest request) {

		synchronized (userDetails.getUserId().intern()) {
			if (!role.equalsIgnoreCase(RoleEnum.DTC.getName())) {
				logger.error("Unauthorized user");
				throw new BadRequestException("Unauthorized user");
			}
			Optional<AuctionDetailsDTO> auctionOptional = auctionDetailsDAO.findById(applicationNo);
			if (!auctionOptional.isPresent()) {
				logger.error("No record found for application number: " + applicationNo);
				throw new BadRequestException("No record found for application number: " + applicationNo);
			}
			AuctionDetailsDTO auctionDto = auctionOptional.get();
			if (!auctionDto.isAuctionClosed()) {
				logger.error("Application pending at MVI level for : " + applicationNo);
				throw new BadRequestException("Application pending at MVI level for : " + applicationNo);
			}
			if (!auctionDto.getDtcOfficeCode().equalsIgnoreCase(userDetails.getOffice().getOfficeCode())) {
				logger.error("Unauthorized user");
				throw new BadRequestException("Unauthorized user");
			}
			if (!auctionDto.getDtcUserId().equalsIgnoreCase(userDetails.getUserId())) {
				logger.error("Unauthorized user");
				throw new BadRequestException("Unauthorized user");
			}
			if (auctionDto.isDtcCompleted()) {
				logger.error("DTC level completed for : " + applicationNo);
				throw new BadRequestException("DTC level completed for : " + applicationNo);
			}
			for (AuctionVehicleDetailsDTO vehicleDto : auctionDto.getVehicleDetails()) {
				// TODO other state doc need to cancel.
				if (vehicleDto.getOtherState() != null && vehicleDto.getOtherState()) {
					vehicleDto.setRcCanceled(Boolean.TRUE);
					vehicleDto.setlUpdate(LocalDateTime.now());
				} else if (!(vehicleDto.getVehicleCondition().equals(TransferType.vehicleCondition.ROADWORTHY)
						&& vehicleDto.getGenuiness().equals(TransferType.genuiness.GENUINE))) {
					if (StringUtils.isNoneBlank(vehicleDto.getPrNo())) {
						Optional<RegistrationDetailsDTO> regDoc = registrationDetailDAO
								.findByPrNo(vehicleDto.getPrNo());
						RegistrationDetailsDTO dto = regDoc.get();
						dto.setApplicationStatus(StatusRegistration.RCCANCELLED.getDescription());
						dto.setRcCancelled(Boolean.TRUE);
						dto.setlUpdate(LocalDateTime.now());
						vehicleDto.setRcCanceled(Boolean.TRUE);
						vehicleDto.setlUpdate(LocalDateTime.now());
						registrationDetailDAO.save(dto);
					}
				}
			}
			auctionDto.setDtcCompleted(Boolean.TRUE);
			this.setActionsForAuction(auctionDto, "Final Approve", userDetails, role, request.getRemoteAddr(), null);
			auctionDetailsDAO.save(auctionDto);
			return "Success";
		}
	}

	private void setActionsForAuction(AuctionDetailsDTO auctionDto, String module, UserDTO userDetails,
			String selectedRole, String ipAddress, String prNo) {
		ActionDetails actions = new ActionDetails();
		actions.setRole(selectedRole);
		actions.setUserId(userDetails.getUserId());
		actions.setModule(module);
		actions.setCreatedDate(LocalDateTime.now());
		actions.setAadharNo(userDetails.getAadharNo());
		actions.setIpAddress(ipAddress);
		if (StringUtils.isNoneBlank(prNo)) {
			actions.setRemarks(prNo);
		}
		if (auctionDto.getActions() == null || auctionDto.getActions().isEmpty()) {
			auctionDto.setActions(Arrays.asList(actions));
		} else {
			List<ActionDetails> actionist = new ArrayList<ActionDetails>();
			actionist.addAll(auctionDto.getActions());
			actionist.add(actions);
			auctionDto.setActions(actionist);
		}
	}

	private void corectionForSeizedDoc(VcrFinalServiceVO vo, VcrFinalServiceDTO dto,
			List<VcrCorrectionLogDTO> correctionsData, UserDTO userDetails, String selectedRole, String ipAddress) {
		VehicleSeizedDTO seizedDto = new VehicleSeizedDTO();
		if (StringUtils.isNoneBlank(vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getVehicleKeptAt())
				&& vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getDateOfSeized() == null) {
			logger.error("Please provide seized date");
			throw new BadRequestException("Please provide seized date");
		}
		if (StringUtils.isBlank(vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getVehicleKeptAt())
				&& vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getDateOfSeized() != null) {
			logger.error("Please provide vehicle kept at");
			throw new BadRequestException("Please provide vehicle kept at");
		}
		if (StringUtils.isNoneBlank(vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getVehicleKeptAt())) {
			seizedDto.setVehicleKeptAt(vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getVehicleKeptAt());
			setCoorectionsLog(correctionsData, "VehicleKeptAt",
					vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getVehicleKeptAt(), "", null, null, null,
					null, userDetails, selectedRole, ipAddress);
		}
		if (vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getDateOfSeized() != null) {
			seizedDto.setDateOfSeized(vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getDateOfSeized());
			setCoorectionsLog(correctionsData, "DateOfSeized",
					vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().getDateOfSeized().toString(), "", null,
					null, null, null, userDetails, selectedRole, ipAddress);
		}
		seizedDto.setDepartmentAction(vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().isDepartmentAction());
		seizedDto.setCourtOrder(vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().isCourtOrder());
		seizedDto.setReleaseOrder(vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().isReleaseOrder());
		if (!(vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().isCourtOrder()
				&& vo.getSeizedAndDocumentImpounded().getVehicleSeizedVO().isDepartmentAction())) {
			seizedDto.setReleaseOrder(Boolean.TRUE);
		}
	}

	@Override
	public String getAuctionToken(String prNo, String role, UserDTO userDetails, HttpServletRequest request) {

		if (!role.equalsIgnoreCase(RoleEnum.MVI.getName())) {
			logger.error("Unauthorized user");
			throw new BadRequestException("Unauthorized user");
		}
		List<AuctionDetailsDTO> listOfAucton = auctionDetailsDAO.findByVehicleDetailsPrNoIn(prNo);
		if (listOfAucton == null || listOfAucton.isEmpty()) {
			logger.error("Auction details not found for [{}] ", prNo);
			throw new BadRequestException("Auction details not found for: " + prNo);
		}
		listOfAucton.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		AuctionDetailsDTO auctionDto = listOfAucton.stream().findFirst().get();
		if (!auctionDto.isAuctionClosed()) {
			logger.error("Application pending at MVI level for : " + prNo);
			throw new BadRequestException("Application pending at MVI level for : " + prNo);
		}
		/*
		 * if(!auctionDto.getDtcOfficeCode().equalsIgnoreCase(userDetails.getOffice().
		 * getOfficeCode())) { logger.error("Unauthorized user"); throw new
		 * BadRequestException("Unauthorized user"); }
		 * if(!auctionDto.getDtcUserId().equalsIgnoreCase(userDetails.getUserId())) {
		 * logger.error("Unauthorized user"); throw new
		 * BadRequestException("Unauthorized user"); }
		 */
		if (!auctionDto.isDtcCompleted()) {
			logger.error("DTC level not completed for : " + prNo);
			throw new BadRequestException("DTC level not completed for : " + prNo);
		}
		AuctionVehicleDetailsDTO vehicleDto = auctionDto.getVehicleDetails().stream()
				.filter(one -> one.getPrNo() != null && one.getPrNo().equalsIgnoreCase(prNo))
				.collect(Collectors.toList()).stream().findFirst().get();

		if (vehicleDto.getRcCanceled() != null && vehicleDto.getRcCanceled()) {
			logger.error("your RC is cancelled  : " + prNo);
			throw new BadRequestException("your RC is cancelled: " + prNo);
		}

		if (vehicleDto.getOtherState() != null && vehicleDto.getOtherState()) {
			logger.error("Vehicle is other sate. Not allow for Transfer of Ownership : " + prNo);
			throw new BadRequestException("Vehicle is other sate. Not allow for Transfer of Ownership : " + prNo);
		}
		if (StringUtils.isBlank(vehicleDto.getToken())) {
			logger.error("Vehicle is not road worthy or genuine.your RC is cancelled : " + prNo);
			throw new BadRequestException("Vehicle is not road worthy or genuine.your RC is cancelled : " + prNo);
		}
		if (vehicleDto.getTowDone() != null && vehicleDto.getTowDone()) {
			logger.error("Transfer of Ownership completed for vehicle nymber  : " + prNo);
			throw new BadRequestException("Transfer of Ownership completed for vehicle nymber: " + prNo);
		}
		return vehicleDto.getToken();

	}

	private Optional<RegistrationDetailsDTO> getRegServiceDoc(String prNo, String trNo, String applicationNumber) {
		if (StringUtils.isBlank(prNo) && StringUtils.isBlank(trNo) && StringUtils.isBlank(applicationNumber)) {
			logger.error("Please provide application number ");
			throw new BadRequestException("Please provide application number.");
		}
		Optional<RegistrationDetailsDTO> registrationDetails = Optional.empty();
		if (StringUtils.isNoneBlank(prNo)) {
			registrationDetails = registrationDetailDAO.findByPrNo(prNo.toUpperCase());
			if (!registrationDetails.isPresent()) {
				logger.error("no record found for : ", prNo.toUpperCase());
				throw new BadRequestException("no record found for : " + prNo.toUpperCase());
			}
		} else if (StringUtils.isNoneBlank(trNo)) {
			Optional<StagingRegistrationDetailsDTO> stagingdetailsOptional = stagingRegistrationDetailsDAO
					.findByTrNo(trNo.toUpperCase());
			if (!stagingdetailsOptional.isPresent()) {
				registrationDetails = registrationDetailDAO.findByTrNo(trNo.toUpperCase());
				if (!registrationDetails.isPresent()) {
					logger.error("no record foundfor : ", trNo.toUpperCase());
					throw new BadRequestException("no record foundfor : " + trNo.toUpperCase());
				}
			} else {
				RegistrationDetailsVO vo = regDetailMapper.convertEntity(stagingdetailsOptional.get());
				RegistrationDetailsDTO dto = regDetailMapper.convertVOForOtherState(vo);
				registrationDetails = Optional.of(dto);
			}
		} else if (StringUtils.isNoneBlank(applicationNumber)) {
			Optional<RegServiceDTO> regService = regServiceDAO.findByApplicationNo(applicationNumber);
			if (regService.isPresent()) {
				RegistrationDetailsDTO dto = regService.get().getRegistrationDetails();
				registrationDetails = Optional.of(dto);
			}
		} else {
			logger.error("Please provide tr number or pr number. ");
			throw new BadRequestException("Please provide tr number or pr number.");
		}
		return registrationDetails;
	}

	@Override
	public CommonFieldsVO stoppagecountForApp(String officeCode, String user) {

		CommonFieldsVO vo = new CommonFieldsVO();
		// vo.setStoppageCount(0);
		// vo.setStoppageRevocationCount(0);
		vo.setFcCount(0);
		List<RegServiceDTO> listodServices = regServiceDAO.findByLockedDetailsLockedByAndLockedDetailsLockedByRole(user,
				RoleEnum.MVI.getName());

		vo.setStoppageCount(stoppageCountForApp(listodServices, ServiceEnum.VEHICLESTOPPAGE.getId()));
		vo.setStoppageRevocationCount(
				stoppageCountForApp(listodServices, ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId()));
		List<RegServiceDTO> fcDtoList = this.returnListOfFCDocs(user);
		if (fcDtoList != null && !fcDtoList.isEmpty()) {
			vo.setFcCount(fcDtoList.size());
		}
		return vo;

	}

	private int stoppageCountForApp(List<RegServiceDTO> listodServices, Integer serviceId) {
		List<Integer> serviceIds = Arrays.asList(serviceId);
		if (listodServices != null && !listodServices.isEmpty()) {
			List<RegServiceDTO> dtolist = listodServices.stream()
					.filter(reg -> serviceIds.stream().anyMatch(sid -> reg.getServiceIds().contains(sid)))
					.collect(Collectors.toList());
			if (dtolist != null && !dtolist.isEmpty()) {
				return dtolist.size();
			}
		}
		return 0;
	}

	/**
	 * Auto Approval Executions within 24 hours of citizen action
	 */
	@Override
	public void doDeemedAction(List<StagingRegistrationDetailsDTO> deemedStagingRegDetlsList) {
		long stTime = System.currentTimeMillis();
		logger.info("deemed auto action doDeemedAction started time {} : ms", stTime);

		// List<StagingRegistrationDetailsDTO> listToBeSkipped=new
		// ArrayList<StagingRegistrationDetailsDTO>();
		deemedStagingRegDetlsList.stream().forEach(data -> {
			// Boolean value = Boolean.TRUE;
			try {
				isToSkip(data);
				// value = Boolean.TRUE;
			} catch (BadRequestException e) {
				if (e.getMessage().equalsIgnoreCase(Status.RCActionStatus.REJECTED.getStatus())) {
					data.setProcessActionStatus(StatusRegistration.REJECTED.getDescription());
				}
			}

			if (StringUtils.isNotEmpty(data.getProcessActionStatus())
					&& StatusRegistration.APPROVED.getDescription().equals(data.getProcessActionStatus())) {
				// If Approved by last officer
				data.setAutoActionStatus(StatusRegistration.SYSTEMAUTOAPPROVED.getDescription());
				commonAutoAction(data, StatusRegistration.APPROVED.getDescription());
			} else if (StringUtils.isNotEmpty(data.getProcessActionStatus())
					&& StatusRegistration.REJECTED.getDescription().equals(data.getProcessActionStatus())) {
				// If Rejected By last officer
				data.setAutoActionStatus(StatusRegistration.SYSTEMAUTOREJECTED.getDescription());
				data.setRejectedByEnclosure(true);
				commonAutoAction(data, StatusRegistration.REJECTED.getDescription());
			} else {
				// If No Action taken in last 24 hours by any RTA official
				data.setAutoActionStatus(StatusRegistration.SYSTEMAUTOAPPROVED.getDescription());
				commonAutoAction(data, StatusRegistration.APPROVED.getDescription());
			}

		});
		// modify the AutoApprovalInitiatedDate for Skipped Record to null
		// stagingRegistrationDetailsDAO.save(listToBeSkipped);
		logger.info("deemed auto action doDeemedAction ended time {} : ms", (System.currentTimeMillis() - stTime));
	}

	private void commonAutoAction(StagingRegistrationDetailsDTO dto, String status) {
		dto.setAutoActionDate(LocalDate.now());
		dto.setApplicationStatus(status);
		RtaActionVO actionVo = new RtaActionVO();
		actionVo.setAction(status);
		List<KeyValue<String, List<ImageEnclosureDTO>>> allEnclosures = dto.getEnclosures();
		allEnclosures.forEach(enc -> {
			enc.getValue().forEach(value -> {

				if (value.getImageActions() == null) {
					enc.getValue().stream().forEach(dat -> {
						dat.setImageStaus(dto.getProcessActionStatus());
					});

				}
				if (null != value.getImageActions()) {
					List<ImageActionsDTO> imageActionsList = value.getImageActions().stream()
							.filter(val -> val.getActionDatetime() != null).collect(Collectors.toList());
					imageActionsList.sort((a, b) -> b.getActionDatetime().compareTo(a.getActionDatetime()));
					if (CollectionUtils.isNotEmpty(imageActionsList)) {
						enc.getValue().stream().forEach(dat -> {
							dat.setImageStaus(imageActionsList.stream().findFirst().get().getAction());
						});
					}
				}
			});
		});
		if (null != dto.getFlowDetails() && null != dto.getFlowDetails().getFlowDetails()) {
			Set<Integer> index = dto.getFlowDetails().getFlowDetails().keySet();

			if (!index.isEmpty()) {

				Integer flowPosition = index.stream().findFirst().get();

				RoleActionDTO roleActionDTO = getRoleActionDTO(status, dto.getApplicationNo(), "SYSTEM", dto, "System",
						"system");
				List<FlowDTO> existingFlowLog = new ArrayList<>();
				if (dto.getFlowDetailsLog() != null) {
					existingFlowLog = dto.getFlowDetailsLog();
					FlowDTO logFlowDTO = null;
					if (existingFlowLog.isEmpty()) {
						logFlowDTO = new FlowDTO();
						List<RoleActionDTO> logFlowDTOList = new ArrayList<RoleActionDTO>();
						logFlowDTOList.add(roleActionDTO);
						logFlowDTO.getFlowDetails().put(flowPosition, logFlowDTOList);
						existingFlowLog.add(logFlowDTO);
					} else {
						logFlowDTO = dto.getFlowDetailsLog().stream().findFirst().get();
						List<RoleActionDTO> logFlowDTOList = logFlowDTO.getFlowDetails().get(flowPosition);
						if (logFlowDTOList == null) {
							logFlowDTOList = new ArrayList<RoleActionDTO>();
						}
						logFlowDTOList.add(roleActionDTO);
						logFlowDTO.getFlowDetails().put(flowPosition, logFlowDTOList);
					}
					dto.getFlowDetails().getFlowDetails().clear();
					dto.setFlowDetailsLog(existingFlowLog);
					dto.setProcessActionStatus(null);
					updateStagingRegDetails(dto);
				}
			}
		}
		try {
			approval(actionVo, "SYSTEM", dto.getApplicationNo(), dto);
		} catch (CloneNotSupportedException e) {
			logger.error("Excpetion occurred in Auto Approval process  {} :", e);
			logger.debug("Excpetion occurred in Auto Approval process  {} :", e);

		}
		saveForStagingLog(dto);
		actionVo = null;
	}

	/**
	 * @param actionVo
	 * @param role
	 * @param applicationNo
	 * @param staginDto
	 * @throws CloneNotSupportedException
	 */
	private void approval(RtaActionVO actionVo, String role, String applicationNo,
			StagingRegistrationDetailsDTO staginDto) throws CloneNotSupportedException {
		staginDto.setlUpdate(LocalDateTime.now());
		/*
		 * if(!actionVo.getStatus().equals(StatusRegistration.APPROVED)) {
		 * staginDto.setApplicationStatus(StatusRegistration.REJECTED.
		 * getDescription()); }
		 */
		logger.debug(
				"Approval Actions for Application [{}]: IsSpecial No Required :[{}], Action :[{}], PrNo :[{}], prType :[{}], Role :[{}] ",
				applicationNo, staginDto.getSpecialNumberRequired(), actionVo.getAction(), staginDto.getPrNo(),
				staginDto.getPrType(), role);

		// if (!staginDto.getSpecialNumberRequired() && ) {
		if ((actionVo.getAction().equals(StatusRegistration.APPROVED.getDescription()))
				&& ((!staginDto.getSpecialNumberRequired())
						|| (staginDto.getSpecialNumberRequired() && StringUtils.isNotBlank(staginDto.getPrNo()))
						|| (null != staginDto.getPrType() && StringUtils.isEmpty(staginDto.getPrNo())
								&& staginDto.getPrType().equalsIgnoreCase(BidNumberType.N.getCode())
								&& staginDto.getSpecialNumberRequired()))) {

			logger.debug(
					"(Yes PR Processing )Approval Actions for Application [{}]: IsSpecial No Required :[{}], Action :[{}], PrNo :[{}], prType :[{}], Role :[{}] ",
					applicationNo, staginDto.getSpecialNumberRequired(), actionVo.getAction(), staginDto.getPrNo(),
					staginDto.getPrType(), role);
			if (staginDto.getClassOfVehicle().equals(ClassOfVehicleEnum.CHSN.getCovCode())
					|| staginDto.getClassOfVehicle().equals(ClassOfVehicleEnum.CHST.getCovCode())
					|| staginDto.getClassOfVehicle().equals(ClassOfVehicleEnum.ARVT.getCovCode())) {
				updateAlterationDetailsInVahan(staginDto);
			}
			if (prService.isAssignNumberNow() || StringUtils.isNotBlank(staginDto.getPrNo())) {
				processPR(staginDto);
				if (staginDto.getSpecialNumberRequired()
						&& !StatusRegistration.SPECIALNOPENDING.getDescription()
								.equals(staginDto.getApplicationStatus())
						&& staginDto.getOwnerType().equals(OwnerTypeEnum.Individual)) {
					elasticService.saveRegDetailsToSecondVehicleData(staginDto);
				}
			} else {
				staginDto.setApplicationStatus(StatusRegistration.PRNUMBERPENDING.getDescription());
				updateStagingRegDetails(staginDto);
			}
			if (staginDto.getSpecialNumberRequired()
					&& !StatusRegistration.SPECIALNOPENDING.getDescription().equals(staginDto.getApplicationStatus())
					&& staginDto.getOwnerType().equals(OwnerTypeEnum.Individual)) {
				elasticService.saveRegDetailsToSecondVehicleData(staginDto);
			}
		} else if (staginDto.getSpecialNumberRequired() && StringUtils.isBlank(staginDto.getPrNo())
				&& actionVo.getAction().equals(StatusRegistration.APPROVED.getDescription())) {
			staginDto.setApplicationStatus(StatusRegistration.SPECIALNOPENDING.getDescription());

			updateStagingRegDetails(staginDto);
		} else if (actionVo.getAction().equals(StatusRegistration.REJECTED.getDescription())) {
			staginDto.setApplicationStatus(StatusRegistration.REJECTED.getDescription());
			updateStagingRegDetails(staginDto);
		}
	}

	@Override
	public String saveStoppageRevocation(String applicationNo, UserDTO userDetails, String ipAddress, String role,
			String status) {
		synchronized (applicationNo.intern()) {
			RegistrationDetailsDTO registrationDetailsDTO = null;
			Optional<RegServiceDTO> regSerDtoOpt = regServiceDAO.findByApplicationNo(applicationNo);
			if (!regSerDtoOpt.isPresent()) {
				throw new BadRequestException("Applicate Details Not Found with applicationNo " + applicationNo);
			}
			if (StringUtils.isEmpty(regSerDtoOpt.get().getMviOfficeCode())) {
				throw new BadRequestException("Office Details Not Found with applicationNo " + applicationNo);
			}
			if (role.equalsIgnoreCase(RoleEnum.MVI.getName())) {
				if (!userDetails.getOffice().getOfficeCode().equalsIgnoreCase(regSerDtoOpt.get().getMviOfficeCode())) {
					throw new BadRequestException("Unauthorized User" + applicationNo);
				}
				if (!userDetails.getPrimaryRole().getName().equalsIgnoreCase(RoleEnum.MVI.getName())
						&& !userDetails.getAdditionalRoles().stream()
								.anyMatch(name -> name.getName().equalsIgnoreCase(RoleEnum.MVI.getName()))) {
					throw new BadRequestException("Given user dont have MVI role. user id: " + userDetails.getUserId());
				}
			} else if (role.equalsIgnoreCase(RoleEnum.RTO.getName())) {
				if (!userDetails.getOffice().getOfficeCode().equalsIgnoreCase(regSerDtoOpt.get().getOfficeCode())) {
					throw new BadRequestException("Unauthorized User" + applicationNo);
				}
			} else {
				throw new BadRequestException("Unauthorized User" + applicationNo);
			}
			Set<String> rolels = new HashSet<>();
			rolels.add(RoleEnum.RTO.getName());
			if (!regSerDtoOpt.get().getServiceType().contains(ServiceEnum.VEHICLESTOPPAGEREVOKATION)) {
				logger.error("serive is not belong to Vehicle stoppage revokation", applicationNo);
				throw new BadRequestException("serive is not belong to Vehicle stoppage revokation" + applicationNo);
			}

			if (regSerDtoOpt.get().getCurrentRoles() != null && !regSerDtoOpt.get().getCurrentRoles().isEmpty()
					&& regSerDtoOpt.get().getCurrentRoles().contains(RoleEnum.MVI.getName())
					&& role.equalsIgnoreCase(RoleEnum.MVI.getName())) {
				Optional<ActionDetails> actionDetailsOpt = regSerDtoOpt.get().getActionDetails().stream()
						.filter(p -> role.equals(p.getRole())).findFirst();
				if (!actionDetailsOpt.isPresent()) {
					logger.error("User role [{}] specific details not found in action detail", role);
					throw new BadRequestException("User role " + role + " specific details not found in actiondetail");
				}
				regSerDtoOpt.get().setCurrentIndex(3);
				regSerDtoOpt.get().setCurrentRoles(rolels);
				regSerDtoOpt.get().setLockedDetails(null);
				this.updateActionDetailsStatus(role, userDetails.getUserId(), status, actionDetailsOpt.get(),
						applicationNo);
			} else if (regSerDtoOpt.get().getCurrentRoles() != null && !regSerDtoOpt.get().getCurrentRoles().isEmpty()
					&& regSerDtoOpt.get().getCurrentRoles().contains(RoleEnum.RTO.getName())
					&& role.equalsIgnoreCase(RoleEnum.RTO.getName())) {
				Optional<ActionDetails> actionDetailsOpt = regSerDtoOpt.get().getActionDetails().stream()
						.filter(p -> role.equals(p.getRole())).findFirst();
				if (!actionDetailsOpt.isPresent()) {
					logger.error("User role [{}] specific details not found in action detail", role);
					throw new BadRequestException("User role " + role + " specific details not found in actiondetail");
				}
				regSerDtoOpt.get().setApplicationStatus(StatusRegistration.getStatusCode(status));
				regSerDtoOpt.get().setCurrentIndex(4);
				regSerDtoOpt.get().setCurrentRoles(null);
				regSerDtoOpt.get().setLockedDetails(null);
				this.updateActionDetailsStatus(role, userDetails.getUserId(), status, actionDetailsOpt.get(),
						applicationNo);
				if (regSerDtoOpt.get().getApplicationStatus().equals(StatusRegistration.APPROVED)) {
					Optional<RegistrationDetailsDTO> registrationDetailsOptinal = registrationDetailDAO
							.findByApplicationNo(regSerDtoOpt.get().getRegistrationDetails().getApplicationNo());
					registrationDetailsDTO = registrationDetailsOptinal.get();
					registratrionServicesApprovals.updatesVehicleStoppageRevokationNewFolw(regSerDtoOpt.get(),
							registrationDetailsDTO);
				}
			} else {
				logger.error("Some thing want wrong for application number : ", applicationNo);
				throw new BadRequestException("Some thing want wrong for application number : " + applicationNo);
			}

			vehicleStoppageDetailsDAO.save(regSerDtoOpt.get().getVehicleStoppageDetails());
			regServiceDAO.save(regSerDtoOpt.get());
			if (registrationDetailsDTO != null) {
				registrationDetailDAO.save(registrationDetailsDTO);
			}
			return "success";
		}
	}

	private void saveForStagingLog(StagingRegistrationDetailsDTO staginDto) {
		StagingRegServiceDetailsAutoApprovalDTO staginLogDto = stagingRegistrationDetailsMapper
				.convertStageAutoApprovalLog(staginDto);
		stagingRegLogDAO.save(staginLogDto);
	}

	@Override
	public ReportDataVO getRegistrationAndTax(RegReportVO regReportVO) {
		ReportDataVO reportDataVO = new ReportDataVO();
		RegistrationDetailsVO regVO = null;
		AadhaarSeedVO aadhaarVO = null;

		Optional<RegistrationDetailsLogDTO> registrationDetailsLogOptinal = registrationDetailLogDAO
				.findTopByRegiDetailsPrNoAndRegiDetailsMovedSourceOrderByLogCreatedDateTimeDesc(regReportVO.getPrNo(),
						"AADHAARSEEDING");
		if (!registrationDetailsLogOptinal.isPresent()) {
			throw new BadRequestException("No records found for the prNo: " + regReportVO.getPrNo());
		}
		RegistrationDetailsDTO regDTO = registrationDetailsLogOptinal.get().getRegiDetails();
		regVO = stagingRegistrationDetailsMapper.convertEntity(regDTO);
		reportDataVO.setRegistrationDetails(regVO);

		Optional<TaxDetailsDTO> taxDTO = getLatestTaxTransaction(regDTO.getApplicationNo());
		if (taxDTO.isPresent()) {
			TaxDetailsMasterVO vo = new TaxDetailsMasterVO();
			BeanUtils.copyProperties(taxDTO.get(), vo);
			reportDataVO.setTaxDetailsDTO(vo);
		}

		Optional<AadhaarSeedDTO> aadhaarDTO = aadhaarSeedDAO.findByPrNoAndStatusInOrderByCreatedDateDesc(
				regReportVO.getPrNo(),
				Arrays.asList(Status.AadhaarSeedStatus.AUTO_APPROVED, Status.AadhaarSeedStatus.AUTO_REJECTED,
						Status.AadhaarSeedStatus.APPROVED, Status.AadhaarSeedStatus.REJECTED));
		if (!aadhaarDTO.isPresent()) {
			throw new BadRequestException("No Record Found for the PrNo: " + regReportVO.getPrNo());
		}
		aadhaarVO = aadhaarSeedMapper.convertEntity(aadhaarDTO.get());
		reportDataVO.setAadhaarSeedVO(aadhaarVO);
		return reportDataVO;
	}

	public Optional<TaxDetailsDTO> getLatestTaxTransaction(String applicationNumber) {
		List<String> taxTypes = new ArrayList<>();
		taxTypes.add(ServiceCodeEnum.QLY_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.HALF_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.YEAR_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.LIFE_TAX.getCode());
		List<TaxDetailsDTO> taxsList = taxDetailsDAO
				.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(applicationNumber, taxTypes);
		if (CollectionUtils.isNotEmpty(taxsList)) {
			TaxDetailsDTO taxDto = new TaxDetailsDTO();
			registrationService.updatePaidDateAsCreatedDate(taxsList);
			taxsList.sort((o1, o2) -> o2.getCreatedDate().compareTo(o1.getCreatedDate()));
			for (TaxDetailsDTO taxDetailsDTO : taxsList) {
				if (taxDetailsDTO.getTaxPeriodEnd() != null) {
					taxDto = taxDetailsDTO;
					break;
				}
			}
			taxsList.clear();
			return Optional.of(taxDto);

		}
		return Optional.empty();
	}

	private void isToSkip(StagingRegistrationDetailsDTO data) {
		if (CollectionUtils.isNotEmpty(data.getFlowDetailsLog())) {
			data.getFlowDetailsLog().stream().forEach(flowlog -> {
				if (CollectionUtils.isNotEmpty(flowlog.getFlowDetails().entrySet())) {
					flowlog.getFlowDetails().entrySet().forEach(flow -> {
						List<RoleActionDTO> roleAct = flow.getValue();
						roleAct.sort((a, b) -> b.getActionTime().compareTo(a.getActionTime()));

						List<RoleActionDTO> ccoOrMVIRejected = roleAct.stream().filter(cc -> ((data
								.getAutoActionDate() == null)
								&& ((cc.getRole().equals(RoleEnum.CCO.getName())
										&& cc.getAction().equals(Status.RCActionStatus.REJECTED.getStatus()))
										|| (cc.getRole().equals(RoleEnum.MVI.getName())
												&& cc.getAction().equals(Status.RCActionStatus.REJECTED.getStatus())))))
								.collect(Collectors.toList());

						if (!ccoOrMVIRejected.isEmpty()) {
							throw new BadRequestException(Status.RCActionStatus.REJECTED.getStatus());
						}
						List<RoleActionDTO> isRtoRejected = roleAct.stream()
								.filter(abc -> null != data.getAutoActionDate()
										&& abc.getActionTime().toLocalDate().isAfter(data.getAutoActionDate())
										&& abc.getRole().equals(RoleEnum.RTO.getName())
										&& abc.getAction().equals(Status.RCActionStatus.REJECTED.getStatus()))
								.collect(Collectors.toList());

						List<RoleActionDTO> isAoRejected = roleAct.stream()
								.filter(abc -> null != data.getAutoActionDate()
										&& abc.getActionTime().toLocalDate().isAfter(data.getAutoActionDate())
										&& abc.getRole().equals(RoleEnum.AO.getName())
										&& abc.getAction().equals(Status.RCActionStatus.REJECTED.getStatus()))
								.collect(Collectors.toList());

						List<RoleActionDTO> isMviRejected = roleAct.stream()
								.filter(abc -> null != data.getAutoActionDate()
										&& abc.getActionTime().toLocalDate().isAfter(data.getAutoActionDate())
										&& abc.getRole().equals(RoleEnum.MVI.getName())
										&& abc.getAction().equals(Status.RCActionStatus.REJECTED.getStatus()))
								.collect(Collectors.toList());

						List<RoleActionDTO> isCcoRejected = roleAct.stream()
								.filter(abc -> null != data.getAutoActionDate()
										&& abc.getActionTime().toLocalDate().isAfter(data.getAutoActionDate())
										&& abc.getRole().equals(RoleEnum.CCO.getName())
										&& abc.getAction().equals(Status.RCActionStatus.REJECTED.getStatus()))
								.collect(Collectors.toList());

						if (!isRtoRejected.isEmpty()) {
							throw new BadRequestException(Status.RCActionStatus.REJECTED.getStatus());
						} else if (!isAoRejected.isEmpty()) {
							throw new BadRequestException(Status.RCActionStatus.REJECTED.getStatus());
						} else if (!isMviRejected.isEmpty()) {
							throw new BadRequestException(Status.RCActionStatus.REJECTED.getStatus());
						} else if (!isCcoRejected.isEmpty()) {
							throw new BadRequestException(Status.RCActionStatus.REJECTED.getStatus());

						}

						/*
						 * if(roleAct.stream().findFirst().get().getRole().equals("MVI")
						 * &&roleAct.stream().findFirst().get().getAction().equals(Status.RCActionStatus
						 * .REJECTED.getStatus())) { throw new
						 * BadRequestException(Status.RCActionStatus.REJECTED.getStatus()); }
						 */
						/*
						 * flow.getValue().stream().forEach(roleAction -> { if
						 * (roleAction.getRole().equals(RoleEnum.MVI.getName()) &&
						 * roleAction.getAction().equals(Status.RCActionStatus.REJECTED.getStatus())) {
						 * throw new BadRequestException(Status.RCActionStatus.REJECTED.getStatus()); }
						 * });
						 */
					});
				}
			});
		}
	}

	@Override
	public UserVO getFinacierData(String prNo) {
		UserVO userVO = null;
		Optional<RegistrationDetailsDTO> prNoDetails = registrationDetailDAO.findByPrNoOrderByCreatedDateDesc(prNo);
		if (prNoDetails.isPresent()) {
			RegistrationDetailsDTO registrationDetailsDTO = prNoDetails.get();
			if ((registrationDetailsDTO.getFinanceDetails() != null)) {

				if ((registrationDetailsDTO.getFinanceDetails().getUserId() != null)
						&& (!registrationDetailsDTO.getFinanceDetails().getUserId().isEmpty())) {
					MasterUsersDTO userDetails = masterUsersDAO
							.findByUserId(registrationDetailsDTO.getFinanceDetails().getUserId());
					if (!userDetails.equals(null)) {
						userVO = new UserVO();

						String substring = "********" + userDetails.getAadharNo().substring(8, 12);
						userVO.setUserId(registrationDetailsDTO.getFinanceDetails().getUserId());
						userVO.setFirstName(userDetails.getFirstName());
						userVO.setAadharNo(substring);
					} else {
						throw new BadRequestException("No User Found In Masters");
					}
				} else {
					throw new BadRequestException("This is Offline Finance" + " " + prNo);
				}

			} else {
				throw new BadRequestException("No Finance Details Found" + " " + prNo);
			}
		}

		return userVO;
	}

	@Override
	public FinancialAssistanceVO getVehicleDetails(String prNo, String aadharNo,
			AadhaarDetailsRequestVO aadhaarDetailsRequest) {
		FinancialAssistanceVO financialAssistanceVO = new FinancialAssistanceVO();
		AadharDetailsResponseVO aadharDetailsResponseVO = null;
		Predicate<Object> object = o -> o != null;

		if (!(object.test(prNo)) && (object.test(aadhaarDetailsRequest)) && (object.test(aadharNo))) {
			throw new BadRequestException("No Input Found");
		}

		Optional<FinancialAssistanceDTO> optionalFinancialDetails = financialAssistanceDAO
				.findByPersonalDetailsAadharNo(aadharNo);
		if (optionalFinancialDetails.isPresent()) {
			throw new BadRequestException("Application Submitted In This AadharNo " + aadharNo);
		}

		Optional<RegistrationDetailsDTO> prDetails = registrationDetailDAO
				.findByPrNoAndApplicantDetailsAadharNoOrderByCreatedDateDesc(prNo, aadharNo);

		if (!prDetails.isPresent()) {
			throw new BadRequestException("No Details Found On This PrNo " + prNo);
		}
		RegistrationDetailsDTO registrationDetailsDTO = prDetails.get();
		if (!((registrationDetailsDTO.getApplicantDetails() != null)
				|| (registrationDetailsDTO.getApplicantDetails().getAadharNo() != null)
				|| (registrationDetailsDTO.getApplicantDetails().getDateOfBirth() != null))) {
			throw new BadRequestException("Applicant Details Not Found");
		}

		Optional<PropertiesDTO> optionalpropertiesDTO = propertiesDAO
				.findByServiceName(ServiceEnum.FINANCIALASSISTANCE.getCode());
		if (!optionalpropertiesDTO.isPresent()) {
			throw new BadRequestException("No Properties Found");
		}
		PropertiesDTO propertiesDTO = optionalpropertiesDTO.get();

		validateDetails(registrationDetailsDTO, propertiesDTO);

		ApplicantSearchWithOutIdInput applicantSearchWithOutIdInput = new ApplicantSearchWithOutIdInput();

		applicantSearchWithOutIdInput.setAadharNo(registrationDetailsDTO.getApplicantDetails().getAadharNo());

		applicantSearchWithOutIdInput.setDob(registrationDetailsDTO.getApplicantDetails().getDateOfBirth());

		DlDetailsVO dlDetails = getDlDetails(applicantSearchWithOutIdInput, propertiesDTO);

		financialAssistanceVO = financialAssistanceMapper.validityChecking(registrationDetailsDTO,
				financialAssistanceVO, propertiesDTO, dlDetails);
		try {
			Optional<AadharDetailsResponseVO> otpResponse = restGateWayService.validateAadhaar(aadhaarDetailsRequest,
					null);
			if ((!otpResponse.isPresent())) {
				throw new BadRequestException("Aadhar Responce Not Found");
			}
			aadharDetailsResponseVO = otpResponse.get();
			if (!aadharDetailsResponseVO.getAuth_status().equalsIgnoreCase("SUCCESS")) {
				throw new BadRequestException(aadharDetailsResponseVO.getAuth_status());
			}

			financialAssistanceVO.setAadharDetailsResponse(aadharDetailsResponseVO);

		} catch (Exception e) {
			throw new BadRequestException(e.getMessage());

		}

		financialAssistanceVO.setPersonalDetails(financialAssistanceMapper
				.getPersonalDetails(registrationDetailsDTO.getApplicantDetails(), aadharDetailsResponseVO));

		try {
			RationCardDetailsVO rationCardDetails = restGateWayService.getRationCardDetails(
					registrationDetailsDTO.getApplicantDetails().getAadharNo(),
					registrationDetailsDTO.getApplicantDetails().getPresentAddress().getDistrict().getDistrictName());
			financialAssistanceVO.setFamilyMembersDetails(rationCardDetails);
		} catch (Exception e) {
			logger.debug(e.getMessage());
		}

		return financialAssistanceVO;
	}

	public void validateDetails(RegistrationDetailsDTO registrationDetailsDTO, PropertiesDTO propertiesDTO) {

		if (!propertiesDTO.getRegCovs().contains(registrationDetailsDTO.getClassOfVehicle())) {
			throw new BadRequestException("Not Ellibile To Financial Assistance");
		}
		/*
		 * if (!(object.test(registrationDetailsDTO.getApplicantDetails()) &&
		 * (registrationDetailsDTO.getApplicantDetails().getIsAadhaarValidated() !=
		 * null) &&
		 * ((registrationDetailsDTO.getApplicantDetails().getIsAadhaarValidated())))) {
		 * throw new BadRequestException("Please Select Aadhar Seeding Service"); }
		 */

	}

	public DlDetailsVO getDlDetails(ApplicantSearchWithOutIdInput input, PropertiesDTO propertiesDTO) {

		ResponseEntity<String> result = null;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		input.setAadharNo(input.getAadharNo());
		input.setServiceType(ServiceEnum.FINANCIALASSISTANCE.getCode());
		HttpEntity<ApplicantSearchWithOutIdInput> httpEntity = new HttpEntity<>(input, headers);
		DLResponceVO dLResponceVO = null;
		try {
			result = restTemplate.exchange(driverDetailsUrl, HttpMethod.POST, httpEntity, String.class);
		} catch (Exception ex) {

			try {
				logger.error("Exception while call. Exception is: [{}]", ex.getMessage());
				throw new ConnectException("Exception while call. Exception is: " + ex);
			} catch (ConnectException e) {
				logger.debug("Unable get DL details form DL server. [{}]", e);
				logger.error("Unable get DL details form DL server. [{}]", e.getMessage());
				throw new BadRequestException("Unable get DL details form DL server.");
			}
		}
		try {
			dLResponceVO = objectMapper.readValue(result.getBody(), DLResponceVO.class);
			if (dLResponceVO == null) {
				throw new BadRequestException("No Dl Record Found RestCall");
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		DlDetailsVO resultVO = dLResponceVO.getResult();

		if ((resultVO == null)) {
			throw new BadRequestException("No Dl Record Found on RestCall");
		}

		if (!resultVO.getApprovedCovs().stream().anyMatch(v -> propertiesDTO.getDlCovs().contains(v.getCode()))) {
			throw new BadRequestException("Elligible Covs Not Found");
		}

		if ((resultVO.getNonTransportValidTo() != null)
				&& (!resultVO.getNonTransportValidTo().isAfter(LocalDate.now()))) {
			throw new BadRequestException(" Non Transport Validity  expired");
		}
		if ((resultVO.getTransportValidTo() != null) && (!resultVO.getTransportValidTo().isAfter(LocalDate.now()))) {
			throw new BadRequestException(" Transport Validity  expired");
		}

		if ((resultVO.getTransportValidTo() == null) && (resultVO.getNonTransportValidTo() == null)) {
			throw new BadRequestException("No Validity Details Found");
		}

		return resultVO;

	}

	@Override
	public String uploadDataForFinancialAssistance(String vo, MultipartFile[] uploadFiles, String userId) {
		// TODO Auto-generated method stub
		FinancialAssistanceDTO financialAssistanceDTO = new FinancialAssistanceDTO();
		if (vo.isEmpty()) {
			throw new BadRequestException("No Input Found");
		}
		String sequence = null;
		try {
			FinancialAssistanceVO readValue = objectMapper.readValue(vo, FinancialAssistanceVO.class);
			logger.info("checking" + readValue);
			if (!((readValue.getPrNo() != null) && (readValue.getPersonalDetails() != null)
					&& (readValue.getPersonalDetails().getAadharNo() != null))) {
				throw new BadRequestException("Bad Input");
			}
			Optional<RegistrationDetailsDTO> prDetails = registrationDetailDAO
					.findByPrNoAndApplicantDetailsAadharNoOrderByCreatedDateDesc(readValue.getPrNo(),
							readValue.getPersonalDetails().getAadharNo());
			if (!prDetails.isPresent()) {
				throw new BadRequestException("Bad Input");
			}
			financialAssistanceDTO = financialAssistanceMapper.convertEntity(readValue, userId, prDetails.get());
			BeanUtils.copyProperties(readValue, financialAssistanceDTO);
			Map<String, String> map = new HashMap<String, String>();
			if ((readValue.getVehicleDetails() == null) || (readValue.getVehicleDetails().getOfficeCode() == null)) {
				throw new BadRequestException("No Office Code Found");
			}
			financialAssistanceDTO
					.setAadhaarResponse(aadhaarDetailsResponseMapper.convertVO(readValue.getAadharDetailsResponse()));
			map.put("officeCode", readValue.getVehicleDetails().getOfficeCode());
			sequence = sequenceGenerator.getSequence(String.valueOf(ServiceEnum.FINANCIALASSISTANCE.getId()), map);
			financialAssistanceDTO.setApplicationNumber(sequence);
			List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = gridFsClient.convertImages(
					readValue.getImages(), sequence, uploadFiles, StatusRegistration.INITIATED.getDescription());
			financialAssistanceDTO.setEnclosures(enclosures);

			financialAssistanceDAO.save(financialAssistanceDTO);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sequence;
	}

	@Override
	public FinancialAssistanceVO getMdoFinancialAssistance(JwtUser jwtUser, String district, String mandal,
			String village) {
		List<FinancialAssistanceDTO> financialAssistanceList = null;
		if (StringUtils.isNoneBlank(district) && StringUtils.isNoneBlank(mandal) && StringUtils.isNoneBlank(village)) {
			financialAssistanceList = financialAssistanceDAO
					.findByPersonalDetailsAddressDistrictAndPersonalDetailsAddressMandalAndVillage(district, mandal,
							village);
		} else if (StringUtils.isNoneBlank(district) && StringUtils.isNoneBlank(mandal)) {
			financialAssistanceList = financialAssistanceDAO
					.findByPersonalDetailsAddressDistrictAndPersonalDetailsAddressMandal(district, mandal);
		} else if (StringUtils.isNoneBlank(district)) {
			financialAssistanceList = financialAssistanceDAO.findByPersonalDetailsAddressDistrict(district);
		} else {
			throw new BadRequestException("No Input Found");
		}
		if (financialAssistanceList == null || financialAssistanceList.isEmpty()) {
			throw new BadRequestException("No data found for " + district + " " + mandal != null ? mandal
					: "" + " " + village != null ? village : "");
		}
		/*
		 * if(StringUtils.isNoneBlank(district) &&
		 * StringUtils.isBlank(mandal)&&StringUtils.isBlank(village)) {
		 * Optional<DistrictDTO> dist = districtDAO.findByDistrictName(district);
		 * if(!dist.isPresent()) { throw new
		 * BadRequestException("No master District for: "+district); } List<MandalDTO>
		 * masterMandl = mandalDAO.findByDistrictId(dist.get().getDistrictId());
		 * masterMandl.sort((p2,p1)->p1.getMandalName().compareTo(p2.getMandalName()));
		 * return financialAssistanceMapper.convertDistrictData(financialAssistanceList,
		 * masterMandl,district);
		 * 
		 * }else if(StringUtils.isNoneBlank(mandal)&&StringUtils.isBlank(village)) {
		 * Optional<MandalDTO> mandalDto = mandalDAO.findBymandalName(mandal);
		 * if(!mandalDto.isPresent()) { throw new
		 * BadRequestException("No master mandal for: "+mandal); } return
		 * financialAssistanceMapper.convertMandalData(financialAssistanceList,
		 * mandalDto.get(),district); }else { Optional<VillageDTO> villgeDTo =
		 * villageDAO.findByVillageName(village); if(!villgeDTo.isPresent()) { throw new
		 * BadRequestException("No master mandal for: "+mandal); } Optional<MandalDTO>
		 * mandalDto = mandalDAO.findBymandalName(mandal); if(!mandalDto.isPresent()) {
		 * throw new BadRequestException("No master mandal for: "+mandal); } return
		 * financialAssistanceMapper.convertVillageData(financialAssistanceList,
		 * mandalDto.get(), district, villgeDTo.get()); }
		 */

		return financialAssistanceMapper.getData(financialAssistanceList);
		// return null;
	}

	@Override
	public String updateFaDetails(FinancialAssistanceVO financialAssistance, String user, MultipartFile[] files) {

		if ((StringUtils.isEmpty(financialAssistance.getApplicationNumber()))
				|| (financialAssistance.getApplicationNumber() == null)) {
			throw new BadRequestException("ApplicationNumber Required");
		}
		Optional<FinancialAssistanceDTO> optionalFaDetails = financialAssistanceDAO
				.findByApplicationNumber(financialAssistance.getApplicationNumber());
		if (!optionalFaDetails.isPresent()) {
			throw new BadRequestException("No Record Found");
		}
		FinancialAssistanceDTO financialAssistanceDTO = optionalFaDetails.get();

		for (ImageVO imageVO : financialAssistance.getUpdateImageList()) {
			String imageType = imageVO.getImageType();
			for (KeyValue<String, List<ImageEnclosureDTO>> enclosureDTO : financialAssistanceDTO.getEnclosures()) {
				if (enclosureDTO.getKey().equals(imageType)) {
					for (ImageEnclosureDTO imageEnclosureDTO : enclosureDTO.getValue()) {
						if (imageEnclosureDTO.getImageId().equals(imageVO.getAppImageDocId())) {
							imageEnclosureDTO.setImageStaus(imageVO.getImageStaus());
							imageEnclosureDTO.setImageComment(imageVO.getImageComment());
						}
					}
				}
			}
		}
		try {
			List<KeyValue<String, List<ImageEnclosureDTO>>> addableEnclosures = gridFsClient.convertImagesForFa(
					financialAssistance.getImages(), financialAssistance.getApplicationNumber(), files,
					financialAssistance.getStatus(), financialAssistanceDTO, financialAssistance);
			financialAssistanceDTO.getEnclosures().addAll(addableEnclosures);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		financialAssistanceDTO.setApplicationStatus(financialAssistance.getStatus());
		/*
		 * if(financialAssistanceDTO.getLockedDetails()!=null) { ActionDetailsDTO
		 * lockedDetails = financialAssistanceDTO.getLockedDetails();
		 * if(financialAssistanceDTO.getLockedDetailsLog()!=null) {
		 * financialAssistanceDTO.getLockedDetailsLog().add(lockedDetails); }else {
		 * List<ActionDetailsDTO> actionDetailsLogList = new
		 * ArrayList<ActionDetailsDTO>(); actionDetailsLogList.add(lockedDetails);
		 * financialAssistanceDTO.setLockedDetailsLog(actionDetailsLogList); } }
		 * 
		 * financialAssistanceDTO.setLockedDetails(null);
		 */
		if (financialAssistance.getStatus().equals(StatusRegistration.APPROVED.getDescription())) {
			financialAssistanceDTO.setCurrentRole(null);

		} else if (financialAssistance.getStatus().equals(StatusRegistration.REJECTED.getDescription())) {
			financialAssistanceDTO.setCurrentRole(RoleEnum.CITIZEN.getName());
		}
		if (financialAssistanceDTO.getActionDetails() != null) {
			financialAssistanceDTO.getActionDetails()
					.add(financialAssistanceMapper.setActionDetails(user, financialAssistance.getStatus()));
		} else {
			List<ActionDetailsDTO> actionDetailsList = new ArrayList<ActionDetailsDTO>();
			actionDetailsList.add(financialAssistanceMapper.setActionDetails(user, financialAssistance.getStatus()));
			financialAssistanceDTO.setActionDetails(actionDetailsList);
		}

		financialAssistanceDAO.save(financialAssistanceDTO);

		return "success";
	}

	public FinancialAssistanceVO getFileOrderSize(FinancialAssistanceDTO financialAssistanceDTO,
			FinancialAssistanceVO financialAssistance) {
		List<Integer> l = new ArrayList<Integer>();
		financialAssistanceDTO.getEnclosures().forEach(x -> {
			List<ImageEnclosureDTO> value = x.getValue();
			l.add(value.size());

		});

		int asInt = l.stream().mapToInt(value -> value).sum();

		List<ImageInput> imageInput = financialAssistance.getImages();
		imageInput.forEach(c -> {
			c.setFileOrder(asInt);

		});
		return financialAssistance;
	}

	@Override
	public Optional<ApplicationCancellationVO> getDetailsForCancellation(String applicationNo, String prNo,
			JwtUser jwtUser, String selectedRole) {
		if (!(selectedRole.equalsIgnoreCase(RoleEnum.AO.getName())
				|| selectedRole.equalsIgnoreCase(RoleEnum.RTO.getName()))) {
			throw new BadRequestException("Invalid Role to do cancellation");
		}
		ApplicationCancellationVO vo = new ApplicationCancellationVO();
		CitizenApplicationSearchResponceVO resultVo = null;
		Optional<RegServiceDTO> regServiceDTO = regServiceDAO.findByApplicationNo(applicationNo);
		if (!regServiceDTO.isPresent()) {
			throw new BadRequestException("No record found with this application number " + applicationNo);
		}
		validateOfficeCode(jwtUser.getOfficeCode(), regServiceDTO.get().getOfficeDetails().getOfficeCode());
		if (StringUtils.isNotBlank(regServiceDTO.get().getPrNo())) {

			resultVo = regServiceDTO.get().getServiceIds().contains(ServiceEnum.DATAENTRY.getId())
					? registrationService
							.setRegistrationDetailsIntoResultVO(regServiceDTO.get().getRegistrationDetails())
					: registrationService.setRegistrationDetailsIntoResultVO(
							registrationDetailDAO.findByPrNo(regServiceDTO.get().getPrNo().toUpperCase()).get());
		}
		vo.setApplicationDetails(regServiceMapper.convertEntity(regServiceDTO.get()));
		vo.setVehicleDetails(resultVo);
		if (CollectionUtils.isNotEmpty(regServiceDTO.get().getServiceType())) {
			vo.setServiceType(regServiceDTO.get().getServiceType().toString().replace("[", "").replace("]", ""));
		}
		return Optional.of(vo);
	}

	@Override
	public void saveApplicationCancellationDetails(String applicationNo, String prNo, JwtUser jwtUser,
			String selectedRole, HttpServletRequest request, String remarks) {
		if (!(selectedRole.equalsIgnoreCase(RoleEnum.AO.getName())
				|| selectedRole.equalsIgnoreCase(RoleEnum.RTO.getName()))) {
			throw new BadRequestException("Invalid Role to do cancellation");
		}
		Optional<RegServiceDTO> regServiceDTO = regServiceDAO.findByApplicationNo(applicationNo);
		if (!regServiceDTO.isPresent()) {
			throw new BadRequestException("No record found with this application number " + applicationNo);
		}
		validateOfficeCode(jwtUser.getOfficeCode(), regServiceDTO.get().getOfficeDetails().getOfficeCode());
		if (regServiceDTO.get().getApplicationStatus().equals(StatusRegistration.APPROVED)
				|| regServiceDTO.get().getApplicationStatus().equals(StatusRegistration.CANCELED)) {
			throw new BadRequestException("Invalid status " + regServiceDTO.get().getApplicationStatus()
					+ "  to do the application " + applicationNo);
		}

		if (CollectionUtils.isNotEmpty(regServiceDTO.get().getServiceIds())
				&& regServiceDTO.get().getServiceIds().contains(ServiceEnum.TAXATION.getId())
				&& (regServiceDTO.get().getApplicationStatus().equals(StatusRegistration.PAYMENTDONE))) {
			throw new BadRequestException("Invalid status " + regServiceDTO.get().getApplicationStatus()
					+ "  to do the application " + applicationNo);
		}
		if (CollectionUtils.isNotEmpty(regServiceDTO.get().getServiceIds())
				&& regServiceDTO.get().getServiceIds().stream()
						.anyMatch(id -> id.equals(ServiceEnum.VCR.getId())
								|| id.equals(ServiceEnum.OTHERSTATETEMPORARYPERMIT.getId())
								|| id.equals(ServiceEnum.OTHERSTATESPECIALPERMIT.getId())
								|| id.equals(ServiceEnum.VOLUNTARYTAX.getId()))) {
			throw new BadRequestException(regServiceDTO.get().getServiceType().toArray()
					+ " Below mentioned services are not avilable for Application cancellation");
		}
		regServiceDTO.get().setApplicationStatus(StatusRegistration.CANCELED);
		regServiceDTO.get().setCancelledDate(LocalDateTime.now());
		regServiceDTO.get().setCancelledBy(jwtUser.getId());
		regServiceDTO.get().setUpdatedBy(jwtUser.getId());
		regServiceDTO.get().setCancelledIpAddress(request.getRemoteAddr());
		regServiceDTO.get().setIpAddress(request.getRemoteAddr());
		regServiceDTO.get().setlUpdate(LocalDateTime.now());
		regServiceDTO.get().setCancellationRemarks(remarks);
		regServiceDAO.save(regServiceDTO.get());
	}

	/*
	 * @Override public void freshRCAODashBoardLOckedDetailsRemove(String
	 * officeCode, String user, String role, String service) { if
	 * (!role.equals(RoleEnum.AO.getName())) { throw new
	 * BadRequestException("This service applicable  for only AO role"); }
	 * List<RegServiceDTO> regServiceDTOList = regServiceDAO
	 * .findByLockedDetailsLockedByAndLockedDetailsLockedByRole(user, role);
	 * 
	 * if (CollectionUtils.isNotEmpty(regServiceDTOList)) { List<RegServiceDTO>
	 * regList = new ArrayList<>(); for (RegServiceDTO regDTO : regServiceDTOList) {
	 * if ((regDTO.getServiceIds().stream().anyMatch(id ->
	 * id.equals(ServiceEnum.RCFORFINANCE.getId()))) &&
	 * !regDTO.getCurrentRoles().stream() .anyMatch(roleName ->
	 * roleName.equals(RoleEnum.MVI.getName()))) { Optional<RegServiceDTO> dto =
	 * regServiceDAO.findByApplicationNo(regDTO.getApplicationNo());
	 * dto.get().setLockedDetails(null); regList.add(dto.get()); } }
	 * regServiceDAO.save(regList); } }
	 */
	private List<String> frcStatusCheck(String frcStatusAO) {

		List<String> frcStatusList = new ArrayList<>();
		if (!frcStatusAO.isEmpty()) {
			if (frcStatusAO.contains(StatusRegistration.PAYMENTDONE.toString())) {
				frcStatusList.add(frcStatusAO);
			}
			if (frcStatusAO.contains(StatusRegistration.MVIAPPROVED.toString())) {
				frcStatusList.add(StatusRegistration.MVIREJECTED.toString());
				frcStatusList.add(StatusRegistration.MVIAPPROVED.toString());

			}
		}

		return frcStatusList;
	}

	@Override
	public List<CommonFieldsVO> getListOfFCDocs(JwtUser jwtUser) {
		List<CommonFieldsVO> vo = new ArrayList<>();
		List<RegServiceDTO> listOfServices = this.returnListOfFCDocs(jwtUser.getUsername());
		if (listOfServices != null && !listOfServices.isEmpty()) {
			CitizenImagesInput input = new CitizenImagesInput();
			Set<Integer> serviceIds = new HashSet<>();
			serviceIds.add(ServiceEnum.NEWFC.getId());
			input.setServiceIds(serviceIds);
			Optional<InputVO> imagesInput = registratrionServicesApprovals.getListOfSupportedEnclosers(input);
			List<ImageInput> images = imagesInput.get().getImageInput();

			vo = regServiceMapper.convertFCFields(listOfServices, images, jwtUser.getUsername());
		}
		return vo;
	}

	private List<RegServiceDTO> returnListOfFCDocs(String user) {

		Optional<UserDTO> userDto = userDAO.findByUserId(user);
		if (!registrationService.isallowImagesInapp(userDto.get().getOffice().getOfficeCode())) {
			return null;
		}
		List<StatusRegistration> status = new ArrayList<>();
		status.add(StatusRegistration.PAYMENTDONE);
		status.add(StatusRegistration.SLOTBOOKED);//
		List<Integer> serviceId = new ArrayList<>();
		serviceId.add(ServiceEnum.NEWFC.getId());
		serviceId.add(ServiceEnum.RENEWALFC.getId());
		serviceId.add(ServiceEnum.OTHERSTATIONFC.getId());
		List<RegServiceDTO> listOfServices = regServiceDAO
				.findByMviOfficeCodeAndApplicationStatusInAndServiceIdsInAndSourceIsNullNative(
						userDto.get().getOffice().getOfficeCode(), status, serviceId);
		return listOfServices;
	}

	@Override
	public void saveFCImagesFromApp(InputVO vo, JwtUser jwtUser, MultipartFile[] uploadfiles) throws IOException {

		this.saveValidationFCImagesFromApp(vo, jwtUser, uploadfiles);
		Optional<TemporaryEnclosuresDTO> enlosuresOptional = temporaryEnclosuresDAO
				.findByApplicationNoAndCreatedByAndStatusIsFalse(vo.getApplicationNo(), jwtUser.getUsername());
		if (enlosuresOptional == null || !enlosuresOptional.isPresent()) {
			Optional<TemporaryEnclosuresDTO> uploadedEnclosures = temporaryEnclosuresDAO
					.findByApplicationNoAndCreatedByAndStatusIsTrue(vo.getApplicationNo(), jwtUser.getUsername());
			if (uploadedEnclosures != null && uploadedEnclosures.isPresent()
					&& uploadedEnclosures.get().getStatus() != null && uploadedEnclosures.get().getStatus()) {
				throw new BadRequestException("Application transaction completed for: " + vo.getApplicationNo());
			}
			List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = gridFsClient.convertImages(vo.getImageInput(),
					vo.getApplicationNo(), uploadfiles, StatusRegistration.INITIATED.getDescription());
			TemporaryEnclosuresDTO dto = new TemporaryEnclosuresDTO();
			dto.setPrNo(vo.getPrNumber());
			dto.setApplicationNo(vo.getApplicationNo());
			dto.setEnclosures(enclosures);
			dto.setServiceType(vo.getServiceType());
			dto.setStatus(Boolean.FALSE);
			dto.setCreatedBy(jwtUser.getUsername());
			dto.setCreatedDate(LocalDateTime.now());
			dto.setCreatedDateStr(LocalDateTime.now().toString());
			dto.setlUpdate(LocalDateTime.now());
			temporaryEnclosuresDAO.save(dto);
		} else {

			List<ImageInput> imageList = new ArrayList<>();
			for (ImageInput image : vo.getImageInput()) {
				if (enlosuresOptional.get().getEnclosures().stream()
						.anyMatch(imagetype -> imagetype.getKey().equals(image.getType()))
						&& image.getImageUri() != null && StringUtils.isNoneBlank(image.getImageUri().getUri())) {
					imageList.add(image);
					KeyValue<String, List<ImageEnclosureDTO>> matchedImage = enlosuresOptional.get().getEnclosures()
							.stream().filter(val -> val.getKey().equals(image.getType())).findFirst().get();
					enlosuresOptional.get().getEnclosures().remove(matchedImage);
					gridFsClient.removeImages(matchedImage.getValue());
				}
			}
			if (imageList != null && !imageList.isEmpty()) {
				List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = gridFsClient.convertImages(imageList,
						vo.getApplicationNo(), uploadfiles, StatusRegistration.INITIATED.getDescription());
				enlosuresOptional.get().getEnclosures().addAll(enclosures);
				temporaryEnclosuresDAO.save(enlosuresOptional.get());
			}

		}
	}

	@Override
	public RegServiceVO getTpVtOfPrNo(String prNo) {
		OtherStateTemporaryPermitDetailsDTO osTpDetailsDto = null;
		VoluntaryTaxDTO vtDetailsDto = null;
		Optional<OtherStateTemporaryPermitDetailsDTO> osTpDetails = otherStateTemporaryPermitDetailsDAO
				.findByPrNoOrderByCreatedDateDesc(prNo);
		if (osTpDetails.isPresent()) {
			// OtherStateTemporaryPermitDetailsVO osTpDetailsVO =
			// otherStateTemporaryPermitDetailsMapper.convertEntity(osTpDetails.get());
			osTpDetailsDto = osTpDetails.get();
		}

		Optional<VoluntaryTaxDTO> vtDetails = voluntaryTaxDAO.findByRegNoOrderByCreatedDateDesc(prNo);
		if (vtDetails.isPresent()) {
			// VoluntaryTaxVO vtDetailsVO =
			// voluntaryTaxMapper.convertEntity(vtDetails.get());
			vtDetailsDto = vtDetails.get();
		}

		RegServiceVO regServiceVO = regServiceMapper.convertEntity(osTpDetailsDto, vtDetailsDto);
		regServiceVO.setPrNo(prNo);
		return regServiceVO;
	}

	private void saveValidationFCImagesFromApp(InputVO vo, JwtUser jwtUser, MultipartFile[] uploadfiles) {

		if (StringUtils.isBlank(vo.getPrNumber())) {
			throw new BadRequestException("Please provide vehicle number");
		}
		if (StringUtils.isBlank(vo.getApplicationNo())) {
			throw new BadRequestException("Please provide application number");
		}
		if (vo.getServiceType() == null) {
			throw new BadRequestException("Please provide service type");
		}
		Optional<RegServiceDTO> regServiceDoc = regServiceDAO.findByApplicationNo(vo.getApplicationNo());
		if (regServiceDoc == null || !regServiceDoc.isPresent()) {
			throw new BadRequestException("No data found for application number: " + vo.getApplicationNo());
		}
		RegServiceDTO regDTO = regServiceDoc.get();
		if (!regDTO.getServiceType().contains(vo.getServiceType())) {
			throw new BadRequestException("Service type miss matched for application number: " + vo.getApplicationNo());
		}
		if (!registrationService.isallowImagesInapp(regDTO.getMviOfficeCode())) {
			throw new BadRequestException("For this office (" + regDTO.getMviOfficeCode()
					+ ") not allowing for images upload in mobile app.");
		}
		registrationService.commonValidationForFc(jwtUser.getUsername(), regDTO);

	}

	@Override
	public List<DisplayEnclosures> getFCMobileUploadImages(String prNo, JwtUser jwtuser) {

		RegServiceDTO dto = registrationService.returnLatestFcDoc(prNo);
		List<DisplayEnclosures> rejectedEnclosures = new ArrayList<>();
		if (registrationService.isallowImagesInapp(dto.getMviOfficeCode())) {
			Optional<TemporaryEnclosuresDTO> enlosuresOptional = returnTemporaryImages(dto.getApplicationNo(), jwtuser);
			for (KeyValue<String, List<ImageEnclosureDTO>> enclosureKeyValue : enlosuresOptional.get()
					.getEnclosures()) {
				List<ImageVO> imagesVO = enclosureMapper.convertNewEntity(enclosureKeyValue.getValue());
				rejectedEnclosures.add(new DisplayEnclosures(imagesVO));
			}

		}
		return rejectedEnclosures;

	}

	private List<String> getMviNameOrderWise(List<UserDTO> mviList, List<String> result) {
		if (CollectionUtils.isNotEmpty(mviList)) {
			mviList.forEach(val -> {
				StringBuilder name = new StringBuilder();
				if (StringUtils.isNotBlank(val.getFirstName())) {
					name.append(val.getFirstName());
				}
				if (StringUtils.isNotBlank(val.getMiddleName())) {
					name.append(val.getMiddleName());
				}
				if (StringUtils.isNotBlank(val.getLastName())) {
					name.append(val.getLastName());
				}
				result.add(name.toString());
			});

		}
		return result.stream().filter(mvi -> StringUtils.isNotBlank(mvi)).collect(Collectors.toList()).stream()
				.sorted((n1, n2) -> n1.compareTo(n2)).collect(Collectors.toList());
	}

	@Override
	public List<String> getMviNamesByDistrict(String officeCode) {
		List<OfficeDTO> districtOffices = Collections.emptyList();
		List<UserDTO> users = Collections.emptyList();
		Optional<OfficeDTO> officeOpt = officeDAO.findByOfficeCodeAndIsActiveTrue(officeCode);
		if (!officeOpt.isPresent()) {
			throw new BadRequestException("No Record found with officeCode " + officeCode);
		}
		if (officeOpt.isPresent() && officeOpt.get().getDistrict() != null) {
			districtOffices = officeDAO.findByIsActiveTrueAndDistrict(officeOpt.get().getDistrict());
		}
		if (!districtOffices.isEmpty()) {
			List<String> offices = districtOffices.stream().map(ofc -> ofc.getOfficeCode())
					.collect(Collectors.toList());
			users = userDAO.findByPrimaryRoleNameOrAdditionalRolesNameAndOfficeOfficeCodeIn(RoleEnum.MVI.getName(),
					RoleEnum.MVI.getName(), offices);
		}
		if (!users.isEmpty()) {
			List<String> result = new ArrayList<String>();
			return getMviNameOrderWise(users, result);
		}
		return Collections.emptyList();
	}

	@Override
	public Optional<TemporaryEnclosuresDTO> returnTemporaryImages(String applicationNo, JwtUser jwtuser) {
		Optional<TemporaryEnclosuresDTO> enlosuresOptional = temporaryEnclosuresDAO
				.findByApplicationNoAndCreatedByAndStatusIsFalse(applicationNo, jwtuser.getUsername());
		if (enlosuresOptional == null || !enlosuresOptional.isPresent()) {
			throw new BadRequestException(
					"Please upload images for application number " + applicationNo + "through mobile app.");
		}
		return enlosuresOptional;
	}

	@Override
	public List<TemporaryEnclosuresDTO> returnAllTemporaryImages(String applicationNo) {
		List<TemporaryEnclosuresDTO> enlosuresList = temporaryEnclosuresDAO
				.findByApplicationNoAndStatusIsFalse(applicationNo);
		if (enlosuresList == null || enlosuresList.isEmpty()) {
			throw new BadRequestException(
					"Please upload images for application number " + applicationNo + "through mobile app.");
		}
		return enlosuresList;
	}

	@Override
	public Object getLockedDeatilsWithApplicationNo(String applicationNo) {

		List<LockedDetailsDTO> lokedDto = new ArrayList<>();
		List<LockedDetailsVO> locked = new ArrayList<>();

		Optional<RegServiceDTO> regService = regServiceDAO.findByApplicationNo(applicationNo);
		if (regService == null || !regService.isPresent()) {
			throw new BadRequestException("please provide correct application number: " + applicationNo);
		}

		if (regService.get().getLockedDetails() != null && regService.get().getLockedDetails().size() != 0) {

			String PrNo = regService.get().getPrNo();

			lokedDto = regService.get().getLockedDetails();

			lokedDto.stream().forEach(datadto -> {
				LockedDetailsVO data = new LockedDetailsVO();
				LocalDateTime localDateTime = timeCovertionLocal(datadto.getLockedDate());
				data.setLocallockedDate(localDateTime);
				data.setLockedByRole((datadto.getLockedByRole()));
				data.setApplicatioNo(datadto.getApplicatioNo());
				data.setLockedBy(datadto.getLockedBy());
				data.setPrNo(PrNo);
				locked.add(data);
			});

		} else {
			throw new BadRequestException("No Lock Details for application number: " + applicationNo);
		}
		return locked;

	}

	public LocalDateTime timeCovertionLocal(LocalDateTime datetime) {
		ZonedDateTime date1 = datetime.atZone(ZoneId.of("Asia/Kolkata"));
		LocalDateTime date = date1.toLocalDateTime();
		return date;
	}

	@Override
	public UserVO getFinacierUserData(String userId) {
		UserVO userVO = new UserVO();
		Boolean parent = Boolean.TRUE;
		String parentId = null;

		Optional<UserDTO> userDTO = userDAO.findByUserId(userId);
		if (userDTO.isPresent()) {
			userVO.setUserId(userDTO.get().getUserId());
			userVO.setFirstName(userDTO.get().getFirstName());
			if (userDTO.get().getAadharNo() == null) {

				throw new BadRequestException("Aadhar is not there for the given: " + userId);
			}
			String aadhar = "********" + userDTO.get().getAadharNo().substring(8, 12);
			userVO.setAadharNo(aadhar);

			if (userDTO.get().isParent() == parent) {
				userVO.setUserRelation("PARENT");
			} else {

				if (userDTO.get().getUserId().equals(userDTO.get().getParentId())
						|| userDTO.get().getUserId().equals(userDTO.get().getParent_userid())
						|| userDTO.get().getUserId().equals(userDTO.get().getParentUserId())) {
					userVO.setUserRelation("PARENT");

				} else {
					parentId = userDTO.get().getParentId() != null ? userDTO.get().getParentId()
							: userDTO.get().getParent_userid();
					userVO.setParentId(parentId);
					userVO.setUserRelation("CHILD");
				}
			}
			if (userDTO.get().getActionItems().size() >= 2) {
				userVO.setMessage("AGREEMENT" + "," + "TERMINATION");
			} else {
				userVO.setMessage("TERMINATION");
			}

		} else {

			throw new BadRequestException("No record is there based on the given userId: " + userId);
		}
		return userVO;
	}

	private LocalDateTime getTimewithDate(LocalDate date, Boolean timeZone) {

		String dateVal = date + "T00:00:00.000Z";
		if (timeZone) {
			dateVal = date + "T23:59:59.999Z";
		}
		ZonedDateTime zdt = ZonedDateTime.parse(dateVal);
		return zdt.toLocalDateTime();
	}

	@Override
	public List<LockedDetailsVO> getLockedDetailsOfficewise(String officeCode, LockedDetailsVO lockVO, Pageable page) {
		LocalDateTime fromDate = getTimewithDate(lockVO.getFromDate(), false);
		LocalDateTime toDate = getTimewithDate(lockVO.getToDate(), true);

		List<LockedDetailsVO> locked = new ArrayList<>();
		List<RegServiceDTO> regService = regServiceDAO.findByOfficeDetailsOfficeCodeAndCreatedDateBetween(officeCode,
				fromDate, toDate);

		if (CollectionUtils.isNotEmpty(regService)) {
			for (RegServiceDTO lockedList : regService)

			{
				if (lockedList.getServiceType() != null
						&& !lockedList.getServiceType().contains(ServiceEnum.VEHICLESTOPPAGE)
						&& !lockedList.getServiceType().contains(ServiceEnum.RCFORFINANCE)
						&& lockedList.getLockedDetails() != null && !(lockedList.getLockedDetails().size() == 0)) {

					String prNo = lockedList.getPrNo();
					List<LockedDetailsDTO> lokedDto = lockedList.getLockedDetails();

					lokedDto.stream().forEach(datadto -> {
						LockedDetailsVO data = new LockedDetailsVO();
						LocalDateTime localDateTime = timeCovertionLocal(datadto.getLockedDate());
						data.setLocallockedDate(localDateTime);
						data.setLockedByRole((datadto.getLockedByRole()));
						data.setApplicatioNo(datadto.getApplicatioNo());
						data.setLockedBy(datadto.getLockedBy());
						data.setPrNo(prNo);
						locked.add(data);
					});
				}
			}
		}
		return locked;
	}

	
	
	@Override
	public String reuploadFCEnclosures(RegServiceVO regServiceVO, JwtUser jwtUser, MultipartFile[] fcReuploadImgs) {

		RegServiceDTO regServiceDTO = registrationService.returnLatestFcDoc(regServiceVO.getPrNo());
		if (!((regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWFC.getId()))
				|| regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))
				|| regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RENEWALFC.getId()))
				|| regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.OTHERSTATIONFC.getId())))
				&& regServiceDTO.getServiceIds().size() == 1)) {
			logger.error("Application not belong to fitness or Stoppage service.");
			throw new BadRequestException("Application not belong to fitness or Stoppage service.");
		}
		if (!regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))
				&& !isApprovedRTO(regServiceDTO)) {
			logger.error("Approval Pending at RTO.Pr no:", regServiceDTO.getPrNo());
			throw new BadRequestException("Approval Pending From RTO.Pr no: " + regServiceDTO.getPrNo());

		}
		if (regServiceVO.getApplicationStatus().equals(StatusRegistration.APPROVED) || (regServiceDTO.getServiceIds()
				.stream().anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))
				&& regServiceDTO.getApplicationStatus().equals(StatusRegistration.INITIATED))) {
			if (null == regServiceVO.getImages() || regServiceVO.getImages().isEmpty()) {
				logger.error("Images not found from UI");
				throw new BadRequestException("Images not found from UI");
			}
			if (fcReuploadImgs == null || fcReuploadImgs.length == 0) {
				logger.error("Please upload the images sdfsdf");
				throw new BadRequestException("Please upload the images sdfsdf");
			}
		}

		if (regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))
				&& regServiceDTO.getApplicationStatus().equals(StatusRegistration.INITIATED)) {
			if (regServiceDTO.getVehicleStoppageDetails() != null
					&& regServiceDTO.getVehicleStoppageDetails().getActions() != null
					&& regServiceDTO.getVehicleStoppageDetails().getActions().stream()
							.anyMatch(a -> a.getRole().equals(RoleEnum.MVI.getName()))
					&& regServiceDTO.getVehicleStoppageDetails().getActions().stream()
							.anyMatch(b -> b.getUserId().equals(jwtUser.getUsername()))) {

				reuploadFcStoppage(regServiceDTO, regServiceVO, fcReuploadImgs);

			} else
				throw new BadRequestException(
						"Unauthorized User To do correction or MVI action not completed for this application "
								+ regServiceDTO.getApplicationNo());

		} else {

			if (!regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))
					&& regServiceDTO.getApplicationStatus().equals(StatusRegistration.APPROVED)) {
				List<ActionDetails> actionDetails = regServiceDTO.getActionDetails().stream()
						.filter(a -> a.getlUpdate().toLocalDate().isEqual(LocalDate.now()))
						.collect(Collectors.toList());

				if (!CollectionUtils.isEmpty(actionDetails)) {

					boolean userCheck = regServiceDTO.getActionDetails().stream()
							.anyMatch(a -> a.getUserId().equals(jwtUser.getUsername())
									&& a.getRole().equals(regServiceVO.getSelectedRole()));

					if (RoleEnum.MVI.getName().equals(regServiceVO.getSelectedRole()) && userCheck) {

						reuploadFcStoppage(regServiceDTO, regServiceVO, fcReuploadImgs);

					} else
						throw new BadRequestException("Un Authorized user to DO Fitness Image corrections ");

				} else
					throw new BadRequestException(
							"you cant reupload the Image,if the image upload date and reupload date is not same ");
			} else
				throw new BadRequestException(
						"MVI action not completed for this application " + regServiceDTO.getApplicationNo());
		}
		return regServiceDTO.getApplicationNo();
	}

	private void reuploadFcStoppage(RegServiceDTO regServiceDTO,RegServiceVO regServiceVO,MultipartFile[] fcReuploadImgs) {
		try {
		Optional<CitizenEnclosuresDTO> encDetails = citizenEnclosuresDAO
				.findByApplicationNo(regServiceDTO.getApplicationNo());
		List<KeyValue<String, List<ImageEnclosureDTO>>> enclousersTo = encDetails.get()
				.getEnclosures();
		if(!regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))) {
		enclousersTo = enclousersTo.stream().filter(a -> a.getValue().stream()
				.anyMatch(b -> b.getImageActions() != null
						&& b.getImageStaus().equals(StatusRegistration.APPROVED.getDescription())))
				.collect(Collectors.toList());
		regServiceDTO.setIsFcReupload(Boolean.TRUE);
		}else {
		enclousersTo = enclousersTo.stream().filter(a -> a.getValue().stream()
				.anyMatch(b -> b.getImageStaus().equals(StatusRegistration.APPROVED.getDescription())))
				.collect(Collectors.toList());
		regServiceDTO.setIsStoppageReupload(Boolean.TRUE);
		}
		encDetails.get().setEnclosures(enclousersTo);

		AtomicInteger i = new AtomicInteger(0);
		regServiceVO.getImages().forEach(a -> {
			a.setFileOrder(i.get());
			i.getAndIncrement();
		});

		registratrionServicesApprovalsImpl.saveImages(regServiceDTO.getApplicationNo(),
				regServiceVO.getImages(), fcReuploadImgs);
		if (encDetails.isPresent()) {
			EnclosuresLogDAO.save(encDetails.get());
		}
		
		regServiceDAO.save(regServiceDTO);
		}
		catch (IOException e) {
			logger.error("Exception [{}]", e.getMessage());
			throw new BadRequestException(
					"While saving images, some problem occurred: " + e.getMessage());
		}
	}

}