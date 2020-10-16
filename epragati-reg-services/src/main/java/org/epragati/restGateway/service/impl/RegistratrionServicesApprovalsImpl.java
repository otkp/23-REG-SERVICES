package org.epragati.restGateway.service.impl;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.aadhaar.AadharDetailsResponseVO;
import org.epragati.aadhaarAPI.AadhaarSourceDTO;
import org.epragati.aop.QueryExecutionService;
import org.epragati.cfst.dao.ElasticSecondVehicleDAO;
import org.epragati.cfst.dto.ElasticSecondVehicleDTO;
import org.epragati.common.dao.FitnessLogsDAO;
import org.epragati.common.dao.PropertiesDAO;
import org.epragati.common.dto.FlowDTO;
import org.epragati.common.dto.PropertiesDTO;
import org.epragati.common.vo.UserStatusEnum;
import org.epragati.constants.CovCategory;
import org.epragati.constants.EnclosureType;
import org.epragati.constants.FcValidityTypesEnum;
import org.epragati.constants.MessageKeys;
import org.epragati.constants.OwnerTypeEnum;
import org.epragati.constants.Schedulers;
import org.epragati.constants.TransferType;
import org.epragati.dao.enclosure.CitizenEnclosuresDAO;
import org.epragati.dao.enclosure.CitizenEnclosuresLogDAO;
import org.epragati.dao.enclosure.TemporaryEnclosuresDAO;
import org.epragati.dto.enclosure.CitizenEnclosuresDTO;
import org.epragati.dto.enclosure.CitizenEnclosuresLogsDTO;
import org.epragati.dto.enclosure.ImageActionsDTO;
import org.epragati.dto.enclosure.ImageEnclosureDTO;
import org.epragati.dto.enclosure.TemporaryEnclosuresDTO;
import org.epragati.exception.BadRequestException;
import org.epragati.images.vo.ImageInput;
import org.epragati.images.vo.InputVO;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dao.ActionDetailsLogDAO;
import org.epragati.master.dao.AlterationDAO;
import org.epragati.master.dao.ApplicantDetailsDAO;
import org.epragati.master.dao.ApprovalProcessFlowDAO;
import org.epragati.master.dao.BileteralTaxDAO;
import org.epragati.master.dao.ClassOfVehicleConversionDAO;
import org.epragati.master.dao.ClassOfVehiclesDAO;
import org.epragati.master.dao.DealerCovDAO;
import org.epragati.master.dao.DealerMakerDAO;
import org.epragati.master.dao.DealerRegDAO;
import org.epragati.master.dao.EductaionInstituteVehicleDetailsDao;
import org.epragati.master.dao.EnclosuresDAO;
import org.epragati.master.dao.FcDetailsDAO;
import org.epragati.master.dao.FinanceDetailsDAO;
import org.epragati.master.dao.FreshRCReassignMviDAO;
import org.epragati.master.dao.MakersDAO;
import org.epragati.master.dao.MasterBileteralTaxStatesConfigDAO;
import org.epragati.master.dao.MasterPayperiodDAO;
import org.epragati.master.dao.MasterTaxBasedDAO;
import org.epragati.master.dao.MasterTaxExcemptionsDAO;
import org.epragati.master.dao.MasterUsersDAO;
import org.epragati.master.dao.NocDetailsDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.RegServiceApprovedDAO;
import org.epragati.master.dao.RegServiceDAO;
import org.epragati.master.dao.RegServiceLogDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.RegistrationDetailLogDAO;
import org.epragati.master.dao.StagingRegServiceDetailsAutoApprovalLogDAO;
import org.epragati.master.dao.StagingRegistrationDetailsDAO;
import org.epragati.master.dao.StateDAO;
import org.epragati.master.dao.TaxDetailsDAO;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dao.VehicleStoppageDetailsDAO;
import org.epragati.master.dto.ActionDetailsLogDTO;
import org.epragati.master.dto.ApplicantDetailsDTO;
import org.epragati.master.dto.ApprovalProcessFlowDTO;
import org.epragati.master.dto.ClassOfVehiclesLogDTO;
import org.epragati.master.dto.ContactDTO;
import org.epragati.master.dto.DealerActionDetailsLog;
import org.epragati.master.dto.DealerCovDTO;
import org.epragati.master.dto.DealerMakerDTO;
import org.epragati.master.dto.DealerRegDTO;
import org.epragati.master.dto.EductaionInstituteVehicleDetailsDto;
import org.epragati.master.dto.EnclosuresDTO;
import org.epragati.master.dto.FcDetailsDTO;
import org.epragati.master.dto.FinanceDetailsDTO;
import org.epragati.master.dto.LockedDetailsDTO;
import org.epragati.master.dto.MasterBileteralTaxStatesConfig;
import org.epragati.master.dto.MasterFreshRcMviQuestions;
import org.epragati.master.dto.MasterPayperiodDTO;
import org.epragati.master.dto.MasterTaxBased;
import org.epragati.master.dto.MasterTaxExcemptionsDTO;
import org.epragati.master.dto.MasterUsersDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.PrBackUpDetailsDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.RegistrationDetailsLogDTO;
import org.epragati.master.dto.RegistrationValidityDTO;
import org.epragati.master.dto.RoleActionDTO;
import org.epragati.master.dto.RolesDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.dto.StateDTO;
import org.epragati.master.dto.TaxComponentDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.master.dto.TaxHelper;
import org.epragati.master.dto.TrailerChassisDetailsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.mappers.DealerRegMapper;
import org.epragati.master.mappers.EnclosuresMapper;
import org.epragati.master.mappers.FcDetailsMapper;
import org.epragati.master.mappers.FcQuestionsMapper;
import org.epragati.master.mappers.MasterStoppageQuationsMapper;
import org.epragati.master.mappers.MasterStoppageRevocationQuationsMapper;
import org.epragati.master.mappers.OfficeMapper;
import org.epragati.master.mappers.PermitDetailsMapper;
import org.epragati.master.mappers.RcCancellationQuestionsMappers;
import org.epragati.master.mappers.RegistrationDetailsMapper;
import org.epragati.master.mappers.TrailerChassisDetailsMapper;
import org.epragati.master.service.InfoService;
import org.epragati.master.service.LogMovingService;
import org.epragati.master.service.PermitsService;
import org.epragati.master.service.PrSeriesService;
import org.epragati.master.service.TrSeriesService;
import org.epragati.master.vo.DealerRegVO;
import org.epragati.master.vo.MasterFreshrcMviQuestionsVO;
import org.epragati.master.vo.MasterStoppageQuationsVO;
import org.epragati.master.vo.OfficeVO;
import org.epragati.master.vo.RegistrationDetailsVO;
import org.epragati.master.vo.StoppageQuationsSubOptionsSupportVO;
import org.epragati.master.vo.StoppageQuationsSubOptionsVO;
import org.epragati.master.vo.TaxTypeVO;
import org.epragati.org.vahan.port.service.VahanSync;
import org.epragati.payment.dto.ClassOfVehiclesDTO;
import org.epragati.payment.dto.FeesDTO;
import org.epragati.payment.dto.PaymentTransactionDTO;
import org.epragati.payments.dao.PaymentTransactionDAO;
import org.epragati.permits.dao.PermitDetailsDAO;
import org.epragati.permits.dto.PermitDetailsDTO;
import org.epragati.registration.service.DealerRegistrationService;
import org.epragati.registration.service.DealerService;
import org.epragati.regservice.CitizenTaxService;
import org.epragati.regservice.RegistrationService;
import org.epragati.regservice.dao.RepresentativeDAO;
import org.epragati.regservice.dto.ActionDetails;
import org.epragati.regservice.dto.AlterationDTO;
import org.epragati.regservice.dto.BileteralTaxDTO;
import org.epragati.regservice.dto.ClassOfVehicleConversion;
import org.epragati.regservice.dto.Comments;
import org.epragati.regservice.dto.FitnessApprovedlogs;
import org.epragati.regservice.dto.FreshRCReassignMviLogDTO;
import org.epragati.regservice.dto.FreshRcDTO;
import org.epragati.regservice.dto.NOCDetailsDTO;
import org.epragati.regservice.dto.RegServiceApprovedDTO;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.dto.RegServiceLogsDTO;
import org.epragati.regservice.dto.RepresentativeDTO;
import org.epragati.regservice.dto.VehicleStoppageMVIReportDTO;
import org.epragati.regservice.mapper.AlterationMapper;
import org.epragati.regservice.mapper.FreshRCMapper;
import org.epragati.regservice.mapper.FreshRCReassignMapper;
import org.epragati.regservice.mapper.RegServiceMapper;
import org.epragati.regservice.mapper.VehicleStoppageMVIReportMapper;
import org.epragati.regservice.vo.FreshApplicationSearchVO;
import org.epragati.regservice.vo.RegServiceVO;
import org.epragati.regservice.vo.VehicleStoppageMVIReportVO;
import org.epragati.reports.service.RCCancellationService;
import org.epragati.rta.service.impl.DTOUtilService;
import org.epragati.rta.service.impl.service.RTAService;
import org.epragati.rta.service.impl.service.RegistratrionServicesApprovals;
import org.epragati.rta.vo.EnclosuresVO;
import org.epragati.rta.vo.FreshRCActionVO;
import org.epragati.rta.vo.FreshRcReassignedMVIVO;
import org.epragati.rta.vo.RtaActionVO;
import org.epragati.rta.vo.TrailerChassisDetailsVO;
import org.epragati.secondvehicle.dao.SecondVehicleSearchDAO;
import org.epragati.secondvehicle.dto.SecondVehicleResultsDTO;
import org.epragati.secondvehicle.dto.SecondVehicleSearchResultsDTO;
import org.epragati.sequence.SequenceGenerator;
import org.epragati.service.enclosure.mapper.EnclosureImageMapper;
import org.epragati.service.enclosure.vo.CitizenImagesInput;
import org.epragati.service.enclosure.vo.ImageVO;
import org.epragati.service.files.GridFsClient;
import org.epragati.service.notification.MessageTemplate;
import org.epragati.service.notification.NotificationTemplates;
import org.epragati.service.notification.NotificationUtil;
import org.epragati.tax.vo.TaxStatusEnum;
import org.epragati.tax.vo.TaxTypeEnum;
import org.epragati.util.AppMessages;
import org.epragati.util.ApplicantTypeEnum;
import org.epragati.util.BidNumberType;
import org.epragati.util.DateConverters;
import org.epragati.util.PermitsEnum;
import org.epragati.util.RoleEnum;
import org.epragati.util.StatusRegistration;
import org.epragati.util.ValidityEnum;
import org.epragati.util.document.KeyValue;
import org.epragati.util.document.Sequence;
import org.epragati.util.payment.ClassOfVehicleEnum;
import org.epragati.util.payment.ModuleEnum;
import org.epragati.util.payment.PayStatusEnum;
import org.epragati.util.payment.ServiceCodeEnum;
import org.epragati.util.payment.ServiceEnum;
import org.epragati.util.payment.ServiceEnum.Flow;
import org.epragati.vcrImage.dao.VoluntaryTaxDAO;
import org.epragati.vcrImage.dto.VoluntaryTaxDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 
 * @author naga.pulaparthi
 *
 */
@Service
public class RegistratrionServicesApprovalsImpl implements RegistratrionServicesApprovals {

	private static final Logger logger = LoggerFactory.getLogger(RegistratrionServicesApprovalsImpl.class);

	private static final Long EXTENDYEAR = 5L;

	@Autowired
	private RegServiceDAO regServiceDAO;

	@Autowired
	AppMessages appMessages;

	@Autowired
	private CitizenEnclosuresDAO citizenEnclosuresDAO;

	@Autowired
	private ApprovalProcessFlowDAO approvalProcessFlowDAO;

	@Autowired
	private EnclosureImageMapper enclosureMapper;

	@Autowired
	private ActionDetailsLogDAO actionDetailsLogDAO;

	@Autowired
	private MasterPayperiodDAO masterPayperiodDAO;

	@Autowired
	private TaxDetailsDAO taxDetailsDAO;

	@Autowired
	private CitizenTaxService citizenTaxService;

	@Autowired
	private FinanceDetailsDAO financeDetailsDAO;

	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;

	@Autowired
	private CitizenEnclosuresLogDAO citizenEnclosuresLogDAO;

	@Autowired
	private NocDetailsDAO nocDetailsDAO;

	@Autowired
	private FcDetailsDAO fcDetailsDAO;

	@Autowired
	private ApplicantDetailsDAO applicantDetailsDAO;

	@Autowired
	private PrSeriesService prService;

	@Autowired
	private TrSeriesService trSeriesService;

	@Autowired
	private NotificationUtil notifications;

	@Autowired
	private NotificationTemplates notificationTemplate;

	@Autowired
	private EnclosuresDAO userEnclosureDAO;

	@Autowired
	private EnclosuresMapper userEnclosureMapper;

	@Autowired
	private GridFsClient gridFsClient;

	@Autowired
	private AlterationDAO alterationDAO;

	@Autowired
	private DTOUtilService dTOUtilService;

	@Autowired
	private RegServiceMapper regServiceMapper;

	@Autowired
	private ClassOfVehiclesDAO classOfVehiclesDAO;

	@Autowired
	private MasterUsersDAO masterUsersDAO;

	@Autowired
	SequenceGenerator sequenceGenerator;

	@Autowired
	private RegServiceApprovedDAO regServiceApprovedDAO;

	@Autowired
	private RegServiceLogDAO regServiceLogDAO;

	@Autowired
	private ElasticSecondVehicleDAO elasticSecondVehicleDAO;

	@Autowired
	private RegistrationService registrationService;

	@Autowired
	private AlterationMapper alterationMapper;

	@Autowired
	private ClassOfVehicleConversionDAO classOfVehicleConversionDAO;

	@Autowired
	private OfficeMapper officeMapper;

	@Autowired
	private FcQuestionsMapper fcQuestionsMapper;

	@SuppressWarnings("rawtypes")
	@Autowired
	private RegistrationDetailsMapper registrationDetailsMapper;

	@Autowired
	private PermitDetailsDAO permitDetailsDAO;

	@Autowired
	private OfficeDAO officeDAO;

	@Autowired
	private MasterStoppageQuationsMapper masterStoppageQuationsMapper;

	@Autowired
	private MasterStoppageRevocationQuationsMapper masterStoppageRevocationQuationsMapper;

	@Autowired
	private StagingRegistrationDetailsDAO stagingRegistrationDetailsDAO;

	@Autowired
	private EductaionInstituteVehicleDetailsDao eductaionInstituteVehicleDetailsDao;

	@Autowired
	private MasterTaxExcemptionsDAO masterTaxExcemptionsDAO;
	@Autowired
	private InfoService infoService;

	@Autowired
	private TrailerChassisDetailsMapper trailerChassisDetailsMapper;

	@Autowired
	private DealerService dealerService;
	@Autowired
	private PropertiesDAO propertiesDAO;
	@Autowired
	private BileteralTaxDAO bileteralTaxDAO;

	@Autowired
	private PaymentTransactionDAO paymentTransactionDAO;
	@Autowired
	private MasterBileteralTaxStatesConfigDAO masterBileteralTaxStatesConfigDAO;

	@Autowired
	private StateDAO stateDAO;
	@Autowired
	private SecondVehicleSearchDAO secondVehicleSearchDAO;

	@Autowired
	private RepresentativeDAO representativeDAO;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private PermitsService permitsService;

	@Autowired
	private MasterTaxBasedDAO masterTaxBasedDAO;
	@Autowired
	private FitnessLogsDAO fitnessLogsDAO;
	@Autowired
	private VoluntaryTaxDAO voluntaryTaxDAO;

	@Autowired
	private RegistrationDetailLogDAO regLog;

	@Autowired
	private RTAService rtaService;

	@Autowired
	private VahanSync vahanSync;
	@Autowired
	private VehicleStoppageMVIReportMapper vehicleStoppageMVIReportMapper;
	@Autowired
	private FreshRCMapper freshRCMapper;
	@Autowired
	private VehicleStoppageDetailsDAO vehicleStoppageDetailsDAO;
	@Autowired
	private PermitDetailsMapper permitDetailsMapper;
	@Autowired
	private FcDetailsMapper fcDetailsMapper;
	@Autowired
	private RcCancellationQuestionsMappers rcQuestionsMapper;
	@Value("${reg.service.images.new.url:}")
	private String imagePreUrl;

	@Autowired
	private QueryExecutionService reportService;

	@Autowired
	private SequenceGenerator sequencenGenerator;

	@Value("${financier.password:}")
	private String financierPassword;

	@Autowired
	private DealerRegistrationService dealerRegistrationService;

	@Autowired
	private DealerCovDAO dealerCovDAO;

	@Autowired
	private DealerMakerDAO dealerMakerDAO;

	@Autowired
	private MakersDAO makersDAO;

	@Autowired
	private DealerRegMapper dealerRegMapper;

	@Autowired
	private DealerRegDAO dealerRegDAO;

	@Autowired
	private LogMovingService logMovingService;

	@Autowired
	private StagingRegServiceDetailsAutoApprovalLogDAO stagingRegServiceLogDAO;

	@Autowired
	private RCCancellationService rcCancellationService;
	@Autowired
	private TemporaryEnclosuresDAO temporaryEnclosuresDAO;

	@Autowired
	private FreshRCReassignMapper freshRCReassignMapper;
	@Autowired
	private FreshRCReassignMviDAO freshRCReassignMviDAO;

	@Override
	public void approvalProcess(JwtUser jwtUser, RtaActionVO actionVo, String role, MultipartFile[] uploadfiles) {
		logger.info("In Approval process with role [{}]", role);
		Optional<RegServiceDTO> regServiceDTOOptional = regServiceDAO.findByApplicationNo(actionVo.getApplicationNo());

		if (!regServiceDTOOptional.isPresent()) {
			logger.error("No record in registration_services with applicationNo [{}]", actionVo.getApplicationNo());
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.NO_AUTHORIZATION));
		}

		RegServiceDTO regServiceDTO = regServiceDTOOptional.get();
		if (actionVo.getSelectedRole().equals(RoleEnum.AO.getName())
				&& regServiceDTO.getServiceIds().contains(ServiceEnum.DATAENTRY.getId())) {
			if (actionVo.getAadhaarDetailsRequestVO() == null) {
				logger.error("AO Authentication Details not available");
				throw new BadRequestException("AO Authentication Details not available");
			}
			AadharDetailsResponseVO aadhaarResVo = registrationService
					.getAadharResponse(actionVo.getAadhaarDetailsRequestVO(), setAadhaarSourceDetails(actionVo));
			if (aadhaarResVo == null) {
				logger.error("Authentication Failed");
				throw new BadRequestException("Authentication Failed");
			}
			Optional<UserDTO> userDto = userDAO.findByUserIdAndOfficeOfficeCode(jwtUser.getId(),
					jwtUser.getOfficeCode());
			if (!userDto.isPresent()) {
				logger.error("AO Details not found");
				throw new BadRequestException("AO Details not found");
			}
			if (StringUtils.isBlank(userDto.get().getAadharNo())) {
				logger.error("AO aadhaar number is not available [{}]", userDto.get().getAadharNo());
				throw new BadRequestException("AO aadhaar number is not available");
			}
			if (!userDto.get().getAadharNo().equals(aadhaarResVo.getUid().toString())) {
				logger.error("Unauthorized user of Aadhaar No [{}]", aadhaarResVo.getUid_num());
				throw new BadRequestException("Unauthorized User");
			}
		}

		if ((RoleEnum.RTO.getIndex() + 1) == (regServiceDTO.getCurrentIndex())) {
			logger.error("record.process.done");
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.RECORD_PROCESS_DONE));
		}

		/*
		 * if (actionVo.getEnclosures() == null) {
		 * logger.warn(appMessages.getLogMessage(MessageKeys.NOT_FOUND_ENCLOSURES),
		 * actionVo.getApplicationNo()); throw new
		 * BadRequestException("Enclosures coming Null"); }
		 */

		StatusRegistration applicationStatus = null;
		Optional<CitizenEnclosuresDTO> citizenEnclosuresOpt = Optional.empty();
		// TODO: check actionVo.getStatus() should be APPROVED or REJECTED.
		applicationStatus = actionVo.getStatus();

		if (!Arrays.asList(StatusRegistration.REJECTED, StatusRegistration.APPROVED).contains(actionVo.getStatus())) {
			logger.error("Invalid approval status ");
			throw new BadRequestException("Invalid approval status.Status should be either "
					+ StatusRegistration.REJECTED + " or " + StatusRegistration.APPROVED);
		}
		if ((RoleEnum.AO.getName().equals(role)
				&& regServiceDTO.getApplicationStatus().equals(StatusRegistration.MVIAPPROVED))
				&& (regServiceDTO.getServiceIds() != null && regServiceDTO.getServiceIds().stream()
						.anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId())))) {
			try {
				this.saveImages(actionVo.getApplicationNo(), actionVo.getImages(), uploadfiles);
			} catch (IOException e) {
				logger.error("Exception [{}]", e.getMessage());
				throw new BadRequestException("While saving images at AO level, for RC for Finance: " + e.getMessage());
			}
		}
		if (RoleEnum.MVI.getName().equals(role)) {
			try {
				this.saveAltDetailsAtMviLevel(actionVo, regServiceDTO, applicationStatus);
				if (this.isSaveImagesFromTemporaryFile(regServiceDTO, jwtUser, applicationStatus)) {
					this.saveTemporaryImagesInMainDoc(regServiceDTO, jwtUser);
				} else {
					this.saveImages(actionVo.getApplicationNo(), actionVo.getImages(), uploadfiles);
				}

			} catch (IOException e) {
				logger.error("Exception [{}]", e.getMessage());
				throw new BadRequestException("While saving images, some problem occurred: " + e.getMessage());
			}
		} else {
			if (regServiceDTO.getServiceType().stream().anyMatch(serviceEnum -> serviceEnum.getIsEnclouserRequired())) {
				citizenEnclosuresOpt = citizenEnclosuresDAO.findByApplicationNoAndServiceIdsIn(
						regServiceDTO.getApplicationNo(), regServiceDTO.getServiceIds());
				if (!citizenEnclosuresOpt.isPresent()) {
					logger.error("Enclosures not found for application No [{}]", regServiceDTO.getApplicationNo());
					throw new BadRequestException(
							"Enclosures not found for application [{" + regServiceDTO.getApplicationNo() + "}]");
				}
				applicationStatus = updateEnclosures(role, actionVo.getEnclosures(), citizenEnclosuresOpt.get());
			}

		}
		// other state second vechicle search related code

		if (regServiceDTOOptional.get().getServiceType() != null
				&& regServiceDTOOptional.get().getServiceType().stream()
						.anyMatch(id -> id.equals(ServiceEnum.DATAENTRY))
				&& citizenTaxService.secondVechileMasterData(
						regServiceDTOOptional.get().getRegistrationDetails().getClassOfVehicle())
				&& !regServiceDTOOptional.get().getRegistrationDetails().isRegVehicleWithPR()
				&& !RoleEnum.MVI.getName().equals(role) && !RoleEnum.CCO.getName().equals(role)
				&& regServiceDTOOptional.get().getRegistrationDetails().getIsFirstVehicle()) {
			Optional<SecondVehicleSearchResultsDTO> fundSecondVehicle = secondVehicleSearchDAO
					.findByApplicationNo(regServiceDTOOptional.get().getApplicationNo());
			logger.debug("Second Vehicle Search for applicaiton No [{}]",
					regServiceDTOOptional.get().getApplicationNo());
			if (fundSecondVehicle.isPresent()) {
				SecondVehicleSearchResultsDTO secondVehicleDTO = fundSecondVehicle.get();
				if (secondVehicleDTO.getSvResults().size() > 0) {
					List<SecondVehicleResultsDTO> aoresults = secondVehicleDTO.getSvResults().stream()
							.filter(ao -> RoleEnum.AO.getName().equals(ao.getRole())).collect(Collectors.toList());
					List<SecondVehicleResultsDTO> rtoresults = secondVehicleDTO.getSvResults().stream()
							.filter(rto -> RoleEnum.RTO.getName().equals(rto.getRole())).collect(Collectors.toList());
					if (aoresults != null && aoresults.size() > 0
							&& RoleEnum.AO.getName().equals(aoresults.get(aoresults.size() - 1).getRole())
							&& aoresults.get(aoresults.size() - 1).isFound() && RoleEnum.AO.getName().equals(role)) {
						applicationStatus = StatusRegistration.REJECTED;
					}
					if (rtoresults != null && rtoresults.size() > 0
							&& RoleEnum.RTO.getName().equals(rtoresults.get(rtoresults.size() - 1).getRole())
							&& rtoresults.get(rtoresults.size() - 1).isFound() && RoleEnum.RTO.getName().equals(role)
							&& applicationStatus.toString().equals(StatusRegistration.REJECTED.getDescription())) {
						applicationStatus = StatusRegistration.REJECTED;
						regServiceDTO.setOsSecondVechicleFoundRTO(Boolean.TRUE);
						regServiceDTO.getRegistrationDetails().setIsFirstVehicle(Boolean.FALSE);

					} else if (rtoresults != null && rtoresults.size() > 0
							&& RoleEnum.RTO.getName().equals(rtoresults.get(rtoresults.size() - 1).getRole())
							&& rtoresults.get(rtoresults.size() - 1).isFound() && RoleEnum.RTO.getName().equals(role)
							&& applicationStatus.toString().equals(StatusRegistration.APPROVED.getDescription())) {
						applicationStatus = StatusRegistration.REJECTED;
						regServiceDTO.setIsPRNoRequiredosSVRejected(Boolean.TRUE);
						regServiceDTO.setOsSecondVechicleFoundRTO(Boolean.TRUE);
						regServiceDTO.getRegistrationDetails().setIsFirstVehicle(Boolean.FALSE);
						notifications.sendNotifications(MessageTemplate.REG_OSSECONDVEHICLEPAYMENTPENDING.getId(),
								regServiceDTO);
					}
				}

			}

		}
		if (regServiceDTO.getServiceIds() != null
				&& regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId()))) {
			this.freshRCValidation(regServiceDTO, role, actionVo, applicationStatus);
			if (applicationStatus.equals(StatusRegistration.REJECTED) && role.equals(RoleEnum.MVI.getName())) {
				applicationStatus = StatusRegistration.MVIREJECTED;
			}

		}
		if (regServiceDTO.getServiceIds() != null && regServiceDTO.getServiceIds().stream()
				.anyMatch(id -> id.equals(ServiceEnum.RCCANCELLATION.getId()))) {
			this.rcCancellationValidation(regServiceDTO, role, actionVo);
		}
		setStatusAndIterationCount(regServiceDTO, role, jwtUser, applicationStatus, actionVo);
		List<LockedDetailsDTO> filteredLockedDetails = removeMatchedLockedDetails(regServiceDTO, jwtUser.getId(), role);
		if (filteredLockedDetails != null && !filteredLockedDetails.isEmpty()) {
			logger.debug("Setting default Lock value for User [{}] having Role [{}] and belongs to officeCode [{}]",
					jwtUser.getId(), role, regServiceDTO.getOfficeCode());
			regServiceDTO.setLockedDetails(filteredLockedDetails);
		}

		if (regServiceDTOOptional.get().getServiceType() != null && regServiceDTOOptional.get().getServiceType()
				.stream().anyMatch(id -> id.equals(ServiceEnum.DATAENTRY)) && RoleEnum.CCO.getName().equals(role)) {
			if (actionVo.getDateOfEntry() != null && regServiceDTO.getnOCDetails() != null) {
				regServiceDTO.getnOCDetails().setDateOfEntry(actionVo.getDateOfEntry());
			}
			if (actionVo.getIssueDate() != null && regServiceDTO.getnOCDetails() != null) {
				regServiceDTO.getnOCDetails().setIssueDate(actionVo.getIssueDate());
			}
			if (actionVo.getInvoiceValue() != null
					&& regServiceDTO.getRegistrationDetails().getInvoiceDetails() != null) {
				regServiceDTO.getRegistrationDetails().getInvoiceDetails().setInvoiceValue(actionVo.getInvoiceValue());
			}
		}

		validationForFreshRcStatus(actionVo, applicationStatus, regServiceDTO, role);
		if (regServiceDTO.getServiceIds() != null
				&& regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId()))) {
			regServiceDTO.getActionDetails().stream().forEach(action -> {
				if ((RoleEnum.MVI.getName().equals(role) && action.getRole().equals(RoleEnum.MVI.getName())
						&& action.getIsDoneProcess())
						|| (RoleEnum.AO.getName().equals(role) && action.getRole().equals(RoleEnum.AO.getName())
								&& action.getIsDoneProcess() && regServiceDTO.isMviDone()))
					saveFrcQuestions(role, actionVo, regServiceDTO);
			});

		}
		regServiceDTO.setlUpdate(LocalDateTime.now());
		regServiceDTO.setUpdatedBy(jwtUser.getId());
		regServiceDAO.save(regServiceDTO);

		if (RoleEnum.MVI.getName().equals(role) && "fitnessApproved".equalsIgnoreCase(actionVo.getFitnessforMVI())) {
			Optional<UserDTO> userDto = userDAO.findByUserIdAndOfficeOfficeCode(jwtUser.getId(),
					jwtUser.getOfficeCode());
			this.mviFitnessApprovedlog(actionVo, role, userDto.get(), regServiceDTO);
		}
		if (citizenEnclosuresOpt.isPresent()) {
			citizenEnclosuresOpt.get().setIterator(regServiceDTO.getIterationCount());
			citizenEnclosuresDAO.save(citizenEnclosuresOpt.get());
			copyToCitizenEnclosuresLogs(citizenEnclosuresOpt.get());
			if (StatusRegistration.APPROVED.equals(regServiceDTO.getApplicationStatus())
					&& RoleEnum.RTO.getIndex() < regServiceDTO.getCurrentIndex()) {
				updateImageStatus(citizenEnclosuresOpt.get());
			}
		}
		saveRegServicesLogs(regServiceDTO);

	}

	public AadhaarSourceDTO setAadhaarSourceDetails(RtaActionVO actionVo) {
		AadhaarSourceDTO aadhaarSourceDTO = new AadhaarSourceDTO();
		aadhaarSourceDTO.setApplicationNo(actionVo.getApplicationNo());
		aadhaarSourceDTO.setRole(actionVo.getSelectedRole());
		aadhaarSourceDTO.setPrNo(actionVo.getPrNo());
		return aadhaarSourceDTO;

	}

	@Override
	public void initiateApprovalProcessFlow(RegServiceDTO regServiceDTO) {
		if (CollectionUtils.isEmpty(regServiceDTO.getServiceIds())) {
			logger.error("Service ids not found");
			throw new BadRequestException("Service ids not found.");
		}

		Integer serviceId = getServiceId(regServiceDTO);
		List<ApprovalProcessFlowDTO> approvalProcessFlowDTO = approvalProcessFlowDAO.findByServiceId(serviceId);

		Integer iterator = (regServiceDTO.getIterationCount() == null) ? 1 : regServiceDTO.getIterationCount() + 1;
		regServiceDTO.setIterationCount(iterator);
		List<ActionDetails> actionDetailsList = new ArrayList<>();
		approvalProcessFlowDTO.stream().forEach(a -> {

			Boolean isProceed = Boolean.TRUE;
			if (1 != iterator.intValue() && null != a.getHeigherAuthor()) {// check AO status.
				ActionDetails actionDetail = getActionDetailByRole(regServiceDTO, a.getHeigherAuthor());
				if (!actionDetail.getIsDoneProcess()) {
					actionDetail = getActionDetailByRole(regServiceDTO, a.getRole());
				}
				if (RoleEnum.CCO.getName().equals(actionDetail.getRole())
						|| StatusRegistration.APPROVED.getDescription().equals(actionDetail.getStatus())) {
					actionDetailsList.add(actionDetail);
					isProceed = false;
				}
			}
			if (isProceed) {
				actionDetailsList.add(new org.epragati.regservice.dto.ActionDetails(a.getRole(),
						ModuleEnum.CITIZEN.getCode(), iterator, Boolean.FALSE, a.getNextIndex(), a.getIndex()));// Default
																												// initial
			}

		});
		Integer currentIndex = actionDetailsList.stream().filter(a -> !a.getIsDoneProcess())
				.sorted((a1, a2) -> a1.getIndex().compareTo(a2.getIndex())).findFirst().get().getIndex();

		regServiceDTO.setCurrentIndex(currentIndex);
		regServiceDTO.setActionDetails(actionDetailsList);
		setCurrentRole(regServiceDTO);

		// Initially insert
	}

	private void setCurrentRole(RegServiceDTO regServiceDTO) {
		if (1 == regServiceDTO.getIterationCount() || null != regServiceDTO.getCurrentRoles()) {
			regServiceDTO.setCurrentRoles(regServiceDTO.getActionDetails().stream()
					.filter(a -> !a.getIsDoneProcess() && regServiceDTO.getCurrentIndex().equals(a.getIndex()))
					.map(a -> a.getRole()).collect(Collectors.toCollection(LinkedHashSet::new)));
			regServiceDTO.setAutoApprovalInitiatedDate(LocalDate.now());
		}
	}

	@Override
	public void incrmentIndex(RegServiceDTO regServiceDTO, String role) {
		ActionDetails actionDetails = this.getActionDetailByRole(regServiceDTO, role);
		actionDetails.setIsDoneOnlyPartially(Boolean.FALSE);
		incrementToNextIndex(regServiceDTO, actionDetails, role);
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

	private List<LockedDetailsDTO> removeMatchedLockedDetails(RegServiceDTO regServiceDTO, String userId, String role) {
		List<LockedDetailsDTO> lockedDetails = new ArrayList<>();
		if (regServiceDTO.getLockedDetails() != null) {
			logger.info("Removing Lock from User [{}] having Role [{}] and belongs to officeCode [{}]", userId, role,
					regServiceDTO.getOfficeCode());
			lockedDetails = regServiceDTO.getLockedDetails();
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
	public StatusRegistration updateEnclosures(String role, List<EnclosuresVO> enclosureList,
			CitizenEnclosuresDTO citizenEnclosures) {
		StatusRegistration status = StatusRegistration.APPROVED;
		for (EnclosuresVO enclosure : enclosureList) {
			List<ImageVO> imageVOList = enclosure.getImages();
			if (imageVOList != null) {
				for (ImageVO imageVO : imageVOList) {
					String imageType = imageVO.getImageType();
					for (KeyValue<String, List<ImageEnclosureDTO>> keyValue : citizenEnclosures.getEnclosures()) {
						if (imageType.equals(keyValue.getKey())) {
							for (ImageEnclosureDTO enclosureDTO : keyValue.getValue()) {
								if (enclosureDTO.getImageId().equals(imageVO.getAppImageDocId())) {
									enclosureMapper.imageVOtoEnclosureDTO(role, enclosureDTO, imageVO);
									if (imageVO.getImageStaus().equals(StatusRegistration.REJECTED.getDescription())) {
										status = StatusRegistration.REJECTED;
									}
								}
							}
						}
					}
				}
			}
		}
		return status;
	}

	@Override
	public void prGenerationFromRegService(RegServiceDTO regServiceDTO) {
		if (regServiceDTO.getServiceIds().contains(ServiceEnum.DATAENTRY.getId())) {
			processForOSRegistration(regServiceDTO, regServiceDTO.getRegistrationDetails(), true);
		} else {
			if (regServiceDTO.getServiceIds().contains(ServiceEnum.ALTERATIONOFVEHICLE.getId())) {
				Integer serviceId = getServiceId(regServiceDTO);
				ApprovalProcessFlowDTO approvalFlowDTO = getApprovalProcessFlowDTO(RoleEnum.AO.getName(), serviceId);
				finalFlowOperations(regServiceDTO, approvalFlowDTO, true);
			} else {
				processForReassignmenetPRGeberation(regServiceDTO, regServiceDTO.getRegistrationDetails());
			}
		}
		regServiceDTO.setApplicationStatus(StatusRegistration.APPROVED);
		if (!regServiceDTO.getServiceIds().contains(ServiceEnum.ALTERATIONOFVEHICLE.getId())) {
			registrationUpdation(regServiceDTO, regServiceDTO.getRegistrationDetails(), null);
		}
		regServiceDAO.save(regServiceDTO);
	}

	private void setStatusAndIterationCount(RegServiceDTO regServiceDTO, String role, JwtUser jwtUser,
			StatusRegistration status, RtaActionVO actionVo) {
		Integer serviceId = getServiceId(regServiceDTO);
		ApprovalProcessFlowDTO approvalFlowDTO = getApprovalProcessFlowDTO(role, serviceId);
		ActionDetails actionDetail = getActionDetailByRole(regServiceDTO, role);
		updateActionDetailsStatus(role, jwtUser.getId(), status.getDescription(), actionDetail, actionVo);
		// Async
		reportService.saveServicesReport(regServiceDTO, actionDetail);
		if (null != regServiceDTO.getCurrentRoles()) {
			if (approvalFlowDTO.getIsFinal() != null && approvalFlowDTO.getIsFinal()) {
				regServiceDTO.getCurrentRoles().clear();
			} else {

				if (!regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))) {
					regServiceDTO.getCurrentRoles().remove(role);
				}
			}
		}
		// Need to check it final step or Not
		if (approvalFlowDTO.getFinalStepChecker() == null) {// this'll be execute for CCO and MVI
			logger.info("In final step checker of approvalflow ");
			if (RoleEnum.MVI.getName().equals(role)
					|| (approvalFlowDTO.getIsFinal() != null && approvalFlowDTO.getIsFinal())) {
				if (StatusRegistration.REJECTED.equals(status)) {
					regServiceDTO.setApplicationStatus(approvalFlowDTO.getRejectionStatus());
					// this.updateMviOfficeDetails(regServiceDTO);
					sendNotifications(MessageTemplate.REG_REJECTED.getId(), regServiceDTO);
					return;
				}
				if (isToUpdateStatusAsTaxPending(regServiceDTO)) {
					regServiceDTO.setApplicationStatus(StatusRegistration.TAXPENDING);
					regServiceDTO.setPaidPyamentsForRenewal(Boolean.FALSE);
					actionDetail.setIsDoneOnlyPartially(Boolean.TRUE);
					regServiceDTO.setMviDone(Boolean.TRUE);
					sendNotifications(MessageTemplate.REG_TAXPENDING.getId(), regServiceDTO);// TAXPENDING Intimation
					return;
				}

				if (regServiceDTO.getOsNewCombinatonsDataEntry()
						&& this.saveOtherState(actionVo, regServiceDTO, status)) {
					regServiceDTO.setApplicationStatus(StatusRegistration.OTHERSTATEPAYMENTPENDING);
					actionDetail.setIsDoneOnlyPartially(Boolean.TRUE);
					regServiceDTO.setMviDone(Boolean.TRUE);
					sendNotifications(MessageTemplate.REG_OTHERSTATEPAYMENTPENDING.getId(), regServiceDTO);
					return;
				}

				if (approvalFlowDTO.getApproveStatus() != null) {
					regServiceDTO.setApplicationStatus(approvalFlowDTO.getApproveStatus());
				}
			}
			incrementToNextIndex(regServiceDTO, actionDetail, role);
			// sendNotifications(MessageTemplate.REG_APPROVAL.getId(),regServiceDTO);//MVI
			// APPROVED intimation
			return;
		} // EX:.. CCO and MVI end's here

		// It'll execute for AO & RTO
		for (Map.Entry<String, String> keyValue : approvalFlowDTO.getFinalStepChecker().entrySet()) {
			ActionDetails finalActionDetail = getActionDetailByRole(regServiceDTO, keyValue.getKey());
			if (!keyValue.getValue().equals(finalActionDetail.getStatus())) {
				// So this is not final need to Iterate or go to next Index
				iterateOrGoToNextIndex(regServiceDTO, approvalFlowDTO);// RejectionCase
				return;
			}
			if (regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))
					&& !keyValue.getValue().equals(finalActionDetail.getStatus())) {
				iterateOrGoToNextIndex(regServiceDTO, approvalFlowDTO);// RejectionCase
				return;
			} else if (regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))
					&& keyValue.getKey().equalsIgnoreCase(RoleEnum.AO.getName())
					&& keyValue.getValue().equals(finalActionDetail.getStatus())) {
				incrementToNextIndex(regServiceDTO, actionDetail, role);
				return;
			}
		}
		if (regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))) {
			LocalDate montstartinDate = dateFormat();
			if (actionVo.getStoppageDate().isBefore(montstartinDate)) {
				throw new BadRequestException(
						"please give stopage date with in the month and not future date: " + regServiceDTO.getPrNo());
			}
			if (actionVo.getStoppageDate() != null && actionVo.getStoppageDate().isAfter(LocalDate.now())) {
				throw new BadRequestException(
						"Vehicle stopage date should not be future date: " + regServiceDTO.getPrNo());
			}
			regServiceDTO.getVehicleStoppageDetails().setAoOrRtostoppageDate(actionVo.getStoppageDate());
		}
		if (regServiceDTO.getServiceIds().stream()
				.anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId()))) {
			LocalDate montstartinDate = dateFormat();
			if (actionVo.getRevocationDate().isBefore(montstartinDate)) {
				throw new BadRequestException(
						"please give stopage revocation date with in the month and not feture date: "
								+ regServiceDTO.getPrNo());
			}
			if (actionVo.getRevocationDate().isAfter(LocalDate.now())) {
				throw new BadRequestException(
						"Vehicle stopage revocation date should not be feture date: " + regServiceDTO.getPrNo());
			}
			regServiceDTO.getVehicleStoppageDetails().setAoOrRtostoppageRevpkationDate(actionVo.getRevocationDate());
		}
		finalFlowOperations(regServiceDTO, approvalFlowDTO, false);
		if (regServiceDTO.getServiceIds() != null
				&& regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))
				&& regServiceDTO.getRegistrationDetails().isRegVehicleWithPR()
				&& regServiceDTO.getRegistrationDetails().getApplicantType() != null
				&& regServiceDTO.getRegistrationDetails().getApplicantType().equalsIgnoreCase("OTHERSTATE")) {
			sendNotifications(MessageTemplate.REG_OTHERSTATEVEHICLEWITHPR.getId(), regServiceDTO);
		} else if (regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId()))
				&& regServiceDTO.isMviDone() && regServiceDTO.getFlowId().equals(Flow.RCFORFINANCEMVIACTION)
				&& regServiceDTO.getApplicationStatus().getDescription()
						.equals(StatusRegistration.APPROVED.getDescription())) {
			sendNotifications(MessageTemplate.RCFORFINANCEFINAPP.getId(), regServiceDTO);
		} else if (regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId()))
				&& !regServiceDTO.isMviDone() && regServiceDTO.getCurrentRoles() != null
				&& regServiceDTO.getFlowId().equals(Flow.RCFORFINANCEMVIACTION)
				&& regServiceDTO.getCurrentRoles().stream().anyMatch(id -> id.equals(RoleEnum.MVI.getName()))) {
			sendNotifications(MessageTemplate.RCFORFINANCEFORM37FIN.getId(), regServiceDTO);
		} else if (regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RCCANCELLATION.getId()))
				&& regServiceDTO.getCurrentRoles() == null
				&& regServiceDTO.getFlowId().equals(Flow.RCCANCELLATIONEMVIACTION) && regServiceDTO
						.getApplicationStatus().getDescription().equals(StatusRegistration.APPROVED.getDescription())) {
			sendNotifications(MessageTemplate.RC_CANCELLATION.getId(), regServiceDTO);
		} else if (regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RCCANCELLATION.getId()))
				&& regServiceDTO.getCurrentRoles().stream().anyMatch(id -> id.equals(RoleEnum.MVI.getName()))
				&& regServiceDTO.getFlowId().equals(Flow.RCCANCELLATIONEMVIACTION) && regServiceDTO
						.getApplicationStatus().getDescription().equals(StatusRegistration.APPROVED.getDescription())) {
			logger.debug("no notifications send in this case for rc cancellation");
		} else {
			sendNotifications(MessageTemplate.REG_APPROVAL.getId(), regServiceDTO);// FINAL Approval cases
		}

	}

	private boolean saveOtherState(RtaActionVO actionVo, RegServiceDTO regServiceDTO, StatusRegistration status) {
		Boolean paymentEnable = false;
		if (regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))) {
			paymentEnable = true;
			if (actionVo.getOtherStateVO().getFirstName() != null) {
				regServiceDTO.getRegistrationDetails().getApplicantDetails()
						.setFirstName(actionVo.getOtherStateVO().getFirstName());
			}
			if (regServiceDTO.getRegistrationDetails().getApplicantDetails().getContact() != null) {
				if (actionVo.getOtherStateVO().getMobile() != null) {
					regServiceDTO.getRegistrationDetails().getApplicantDetails().getContact()
							.setMobile(actionVo.getOtherStateVO().getMobile());
				}
			}
			if (actionVo.getOtherStateVO().getVehicleType() != null)
				regServiceDTO.getRegistrationDetails().setVehicleType(actionVo.getOtherStateVO().getVehicleType());
			if (actionVo.getOtherStateVO().getChassisNumber() != null)
				regServiceDTO.getRegistrationDetails().getVehicleDetails()
						.setChassisNumber(actionVo.getOtherStateVO().getChassisNumber());
			if (actionVo.getOtherStateVO().getEngineNumber() != null)
				regServiceDTO.getRegistrationDetails().getVehicleDetails()
						.setEngineNumber(actionVo.getOtherStateVO().getEngineNumber());

			if (actionVo.getOtherStateVO().getBodyType() != null)
				regServiceDTO.getRegistrationDetails().getVehicleDetails()
						.setBodyTypeDesc(actionVo.getOtherStateVO().getBodyType());
			if (actionVo.getOtherStateVO().getManufacturedMonthYear() != null)
				regServiceDTO.getRegistrationDetails().getVehicleDetails()
						.setManufacturedMonthYear(actionVo.getOtherStateVO().getManufacturedMonthYear());
			if (actionVo.getOtherStateVO().getMakersModel() != null)
				regServiceDTO.getRegistrationDetails().getVehicleDetails()
						.setMakersModel(actionVo.getOtherStateVO().getMakersModel());
			if (actionVo.getOtherStateVO().getMakersDesc() != null)
				regServiceDTO.getRegistrationDetails().getVehicleDetails()
						.setMakersDesc(actionVo.getOtherStateVO().getMakersDesc());
			if (actionVo.getOtherStateVO().getFuelDesc() != null)
				regServiceDTO.getRegistrationDetails().getVehicleDetails()
						.setFuelDesc(actionVo.getOtherStateVO().getFuelDesc());
			if (actionVo.getOtherStateVO().getClassOfVehicleDesc() != null
					&& actionVo.getOtherStateVO().getCov() != null) {
				regServiceDTO.getRegistrationDetails().getVehicleDetails()
						.setVehicleClass(actionVo.getOtherStateVO().getCov());
				regServiceDTO.getRegistrationDetails().getVehicleDetails()
						.setClassOfVehicle(actionVo.getOtherStateVO().getCov());
				regServiceDTO.getRegistrationDetails().getVehicleDetails()
						.setClassOfVehicleDesc(actionVo.getOtherStateVO().getClassOfVehicleDesc());
				regServiceDTO.getRegistrationDetails().setClassOfVehicle(actionVo.getOtherStateVO().getCov());
				regServiceDTO.getRegistrationDetails()
						.setClassOfVehicleDesc(actionVo.getOtherStateVO().getClassOfVehicleDesc());
			}
			if (actionVo.getOtherStateVO().getSeating() != null)
				regServiceDTO.getRegistrationDetails().getVehicleDetails()
						.setSeatingCapacity(actionVo.getOtherStateVO().getSeating());
			if (actionVo.getOtherStateVO().getRlw() != null)
				regServiceDTO.getRegistrationDetails().getVehicleDetails().setRlw(actionVo.getOtherStateVO().getRlw());
			if (actionVo.getOtherStateVO().getUlw() != null)
				regServiceDTO.getRegistrationDetails().getVehicleDetails().setUlw(actionVo.getOtherStateVO().getUlw());
			if (actionVo.getOtherStateVO().getTrailers() != null
					&& actionVo.getOtherStateVO().getTrailers().size() > 0) {
				List<TrailerChassisDetailsDTO> trailerDetail = new ArrayList<>();
				if (regServiceDTO.getRegistrationDetails().getVehicleDetails() != null) {
					for (TrailerChassisDetailsVO trailerDetails : actionVo.getOtherStateVO().getTrailers()) {
						TrailerChassisDetailsDTO trailerDTO = new TrailerChassisDetailsDTO();
						if (trailerDetails.getMakerName() != null)
							trailerDTO.setMakerName(trailerDetails.getMakerName());
						if (trailerDetails.getChassisNo() != null)
							trailerDTO.setChassisNo(trailerDetails.getChassisNo());
						if (trailerDetails.getColor() != null)
							trailerDTO.setColour(trailerDetails.getColor());
						if (trailerDetails.getUlw() != null)
							trailerDTO.setUlw(trailerDetails.getUlw());
						if (trailerDetails.getGtw() != null)
							trailerDTO.setGtw(trailerDetails.getGtw());
						trailerDetail.add(trailerDTO);
					}
					regServiceDTO.getRegistrationDetails().getVehicleDetails().setTrailers(trailerDetail);

				}
			}
			if (actionVo.getOtherStateVO().getColor() != null) {
				regServiceDTO.getRegistrationDetails().getVehicleDetails()
						.setColor(actionVo.getOtherStateVO().getColor());
			}
			if (actionVo.getOtherStateVO().getAxleType() != null) {
				regServiceDTO.getRegistrationDetails().getVehicleDetails()
						.setAxleType(actionVo.getOtherStateVO().getAxleType());
			}

			this.SaveMviEditData(actionVo, regServiceDTO);
			if (regServiceDTO.getRegistrationDetails().isRegVehicleWithPR()
					&& regServiceDTO.getRegistrationDetails().isTaxPaidByVcr()
					&& !(regServiceDTO.getServiceIds().contains(ServiceEnum.TRANSFEROFOWNERSHIP.getId())
							|| regServiceDTO.getServiceIds().contains(ServiceEnum.CHANGEOFADDRESS.getId())
							|| regServiceDTO.getServiceIds().contains(ServiceEnum.HPA.getId()))) {
				// ToDO vcr vrification req.
				paymentEnable = false;
			} else if (regServiceDTO.getRegistrationDetails().isRegVehicleWithPR() && (regServiceDTO
					.getRegistrationDetails().getApplicantType().equalsIgnoreCase("Paper RC")
					|| regServiceDTO.getRegistrationDetails().getApplicantType().equalsIgnoreCase("WITHINTHESTATE"))) {
				paymentEnable = false;
			} else if (regServiceDTO.getRegistrationDetails().isRegVehicleWithPR()
					&& (regServiceDTO.getServiceIds().contains(ServiceEnum.TRANSFEROFOWNERSHIP.getId())
							|| regServiceDTO.getServiceIds().contains(ServiceEnum.CHANGEOFADDRESS.getId())
							|| regServiceDTO.getServiceIds().contains(ServiceEnum.HPA.getId()))) {
				paymentEnable = true;
			}
		}

		return paymentEnable;
	}

	private LocalDate dateFormat() {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-M-yyyy");
		String date1 = "01-" + String.valueOf(LocalDate.now().getMonthValue()) + "-"
				+ String.valueOf(LocalDate.now().getYear());
		LocalDate montstartinDate = LocalDate.parse(date1, formatter);
		return montstartinDate;
	}

	@Override
	public void updateMviOfficeDetails(RegServiceDTO regServiceDTO) {
		if ((regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWFC.getId()))
				|| regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RENEWALFC.getId()))
				|| regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.OTHERSTATIONFC.getId())))
				&& regServiceDTO.getServiceIds().size() == 1) {
			Pair<OfficeVO, String> mviOfficeDetails = registrationService.getOffice(
					regServiceDTO.getRegistrationDetails().getApplicantDetails().getPresentAddress().getMandal()
							.getMandalCode(),
					regServiceDTO.getRegistrationDetails().getVehicleType(),
					regServiceDTO.getRegistrationDetails().getOwnerType().toString(), StringUtils.EMPTY);
			regServiceDTO.setOldMviOfficeCode(regServiceDTO.getMviOfficeCode());
			regServiceDTO.setOldMviOfficeDetails(regServiceDTO.getMviOfficeDetails());
			regServiceDTO.setMviOfficeDetails(officeMapper.convertVO(mviOfficeDetails.getFirst()));
			regServiceDTO.setMviOfficeCode(mviOfficeDetails.getSecond());
			regServiceDTO.setAllowFcForOtherStation(Boolean.FALSE);
		}
	}

	private Integer getServiceId(RegServiceDTO regServiceDTO) {

		if (regServiceDTO.getFlowId() != null) {
			return regServiceDTO.getFlowId().getId();
		}
		if (regServiceDTO.getServiceIds().size() > 1) {
			Optional<ServiceEnum> serviceEnumOpt = ServiceEnum
					.getContainsMVIRequiredService(regServiceDTO.getServiceIds());
			if (serviceEnumOpt.isPresent()) {
				return serviceEnumOpt.get().getId();
			}
		}
		Set<Integer> serviceIds = new HashSet<>();
		regServiceDTO.getServiceIds().forEach(id -> {
			if (!ServiceEnum.getApprovalNotRequiredService().contains(id)) {
				serviceIds.add(id);
			}
		});
		return serviceIds.iterator().next();
	}

	private void copyToCitizenEnclosuresLogs(CitizenEnclosuresDTO citizenEnclosuresDTO) {
		CitizenEnclosuresLogsDTO citizenEnclosuresLogsDTO = citizenEnclosuresDTO;
		citizenEnclosuresLogDAO.save(citizenEnclosuresLogsDTO);

	}

	private void updateActionDetailsStatus(String role, String userId, String status, ActionDetails actionDetail,
			RtaActionVO actionVo) {
		actionDetail.setApplicationNo(actionVo.getApplicationNo());
		actionDetail.setRole(role);
		actionDetail.setUserId(userId);
		actionDetail.setIsDoneProcess(Boolean.TRUE);
		actionDetail.setStatus(status);
		actionDetail.setlUpdate(LocalDateTime.now());

	}

	private ActionDetails getActionDetailByRole(RegServiceDTO regServiceDTO, String role) {

		Optional<ActionDetails> actionDetailsOpt = regServiceDTO.getActionDetails().stream()
				.filter(p -> role.equals(p.getRole())).findFirst();
		if (!actionDetailsOpt.isPresent()) {
			logger.error("User role [{}] specific details not found in action detail", role);
			throw new BadRequestException("User role " + role + " specific details not found in actiondetail");
		}
		return actionDetailsOpt.get();
	}

	@Override
	public ApprovalProcessFlowDTO getApprovalProcessFlowDTO(String role, Integer serviceId) {
		Optional<ApprovalProcessFlowDTO> approvalProcessFlowDTO = approvalProcessFlowDAO
				.findByServiceIdAndRole(serviceId, role);
		if (!approvalProcessFlowDTO.isPresent()) {
			throw new BadRequestException("master_approval_process_flow  data not found");
		}
		return approvalProcessFlowDTO.get();
	}

	private void iterateOrGoToNextIndex(RegServiceDTO regServiceDTO, ApprovalProcessFlowDTO approveDTO) {

		if (null != approveDTO.getIsFinal() && approveDTO.getIsFinal()) {
			regServiceDTO.setApplicationStatus(approveDTO.getRejectionStatus());
			moveActionsDetailsToActionDetailsLogs(regServiceDTO);
			if (Arrays
					.asList(ServiceEnum.NEWFC.getId(), ServiceEnum.RENEWALFC.getId(),
							ServiceEnum.OTHERSTATIONFC.getId())
					.contains(regServiceDTO.getServiceIds().iterator().next())) {
				sendNotifications(MessageTemplate.REG_FC.getId(), regServiceDTO);// NEWFC/RENEWALFC Intimation
			} else {
				sendNotifications(MessageTemplate.REG_REUPLOAD.getId(), regServiceDTO);// ReUpload intimation
			}
		} else {
			regServiceDTO.setCurrentIndex(approveDTO.getNextIndex());
			setCurrentRole(regServiceDTO);
			if (regServiceDTO.getServiceIds() != null && regServiceDTO.getServiceIds().stream()
					.anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId()))) {
				logger.debug("here no notification for  RCFORFINANCE");
			} else if (regServiceDTO.getIsPRNoRequiredosSVRejected() == null
					|| !regServiceDTO.getIsPRNoRequiredosSVRejected()) {
				sendNotifications(MessageTemplate.REG_REJECTED.getId(), regServiceDTO);// AO REJECTION intimation
			}
		}

	}

	@Override
	public void moveActionsDetailsToActionDetailsLogs(RegServiceDTO regServiceDTO) {

		List<ActionDetailsLogDTO> actionLogs = new ArrayList<>();
		regServiceDTO.getActionDetails().stream().forEach(actionLogs::add);
		actionDetailsLogDAO.save(actionLogs);
		actionLogs.clear();

	}

	private void finalFlowOperations(RegServiceDTO regServiceDTO, ApprovalProcessFlowDTO approveDTO,
			boolean isfromScheduler) {

		regServiceDTO.setApplicationStatus(approveDTO.getApproveStatus());
		regServiceDTO.setCurrentIndex(RoleEnum.RTO.getIndex() + 1);
		regServiceDTO.setCurrentRoles(null);
		if (regServiceDTO.getServiceIds().contains(ServiceEnum.DATAENTRY.getId())
				&& regServiceDTO.getRegistrationDetails() != null
				&& regServiceDTO.getRegistrationDetails().getApplicantType() != null
				&& (regServiceDTO.getRegistrationDetails().getApplicantType().equals("OTHERSTATE")
						|| regServiceDTO.getRegistrationDetails().getApplicantType().equals("MILITARY"))
				&& regServiceDTO.getRegistrationDetails().isRegVehicleWithPR()
				&& regServiceDTO.getOtherStateNOCStatus() == null && regServiceDTO.getOsNewCombinatonsDataEntry()) {

			regServiceDTO.setOtherStateNOCStatus(StatusRegistration.NOCVERIFICATIONPENDING);
			regServiceDTO.setApprovedDateStr(LocalDateTime.now().toString());
			regServiceDTO.setApprovedDate(LocalDateTime.now());
			// regServiceDAO.save(regServiceDTO);
			return;
		}

		RegistrationDetailsDTO registrationDetailsDTO = findRegistrationDetails(regServiceDTO);
		RegistrationDetailsDTO oldregistrationDetailsDTO = null;
		try {
			oldregistrationDetailsDTO = (RegistrationDetailsDTO) registrationDetailsDTO.clone();
		} catch (CloneNotSupportedException e) {
			// TODO Auto-generated catch block
			logger.error("{}", e);
		}

		this.updateTaxDetailsInRegCollection(regServiceDTO, registrationDetailsDTO);
		this.updatesInsuranceAndPUCDetails(regServiceDTO, registrationDetailsDTO);
		this.updateBasicDetails(regServiceDTO, registrationDetailsDTO);
		registrationDetailsDTO.setCardPrinted(Boolean.FALSE);
		registrationDetailsDTO.setCardDispatched(Boolean.FALSE);
		// regServiceDTO.getServiceIds().sort((l1,l2)->l1.compareTo(l2));
		for (Integer serviceId : regServiceDTO.getServiceIds()) {
			ServiceEnum serviceEnum = ServiceEnum.getServiceEnumById(serviceId);
			regServiceDTO.setApprovedDateStr(LocalDateTime.now().toString());
			switch (serviceEnum) {

			case ISSUEOFNOC:
				updatesIssueofNOCData(regServiceDTO, registrationDetailsDTO);
				break;
			case ALTERATIONOFVEHICLE:
				updatesAlterationofVehicleData(regServiceDTO, registrationDetailsDTO);
				break;
			case RENEWAL:
				updatesRenewalData(regServiceDTO, registrationDetailsDTO);
				break;
			case CHANGEOFADDRESS:
				updatesChangeofAddressData(regServiceDTO, registrationDetailsDTO);
				break;
			case DUPLICATE:
				updatesDuplicateData(regServiceDTO, registrationDetailsDTO);
				break;
			case TRANSFEROFOWNERSHIP:
				updatesTransferofOwnershipData(regServiceDTO, registrationDetailsDTO);
				break;
			case HIREPURCHASETERMINATION:
				updatesHPT(regServiceDTO, registrationDetailsDTO);
				break;
			case AADHARSEEDING:
				break;
			case TEMPORARYREGISTRATION:
				break;
			case FR:
				break;
			case NEWFC:
				updatesNewFC(regServiceDTO, registrationDetailsDTO);
				break;
			case RENEWALFC:
				updatesNewFC(regServiceDTO, registrationDetailsDTO);
				break;
			case OTHERSTATIONFC:
				updatesNewFC(regServiceDTO, registrationDetailsDTO);
				break;
			case HPA:
				updatesHPA(regServiceDTO, registrationDetailsDTO);
				break;
			case SPNR:
				break;
			case SPNB:
				break;
			case DATAENTRY:
				updatesDataEntryData(regServiceDTO, registrationDetailsDTO);
				break;
			case BODYBUILDER:
				break;
			case TRAILER:
				break;
			case REASSIGNMENT:
				updatesReassignmentData(regServiceDTO, registrationDetailsDTO, isfromScheduler);
				break;
			case CANCELLATIONOFNOC:
				updatesCancellationofNOCData(regServiceDTO, registrationDetailsDTO);
				break;
			case NEWPERMIT:
				break;
			case NEWREG:
				break;
			case OBJECTION:
				updatesTheftIntimation(regServiceDTO, registrationDetailsDTO);
				break;
			case REVOCATION:
				updatesTheftRevocation(regServiceDTO, registrationDetailsDTO);
				break;
			case THEFTINTIMATION:
				updatesTheftIntimation(regServiceDTO, registrationDetailsDTO);
				break;
			case THEFTREVOCATION:
				updatesTheftRevocation(regServiceDTO, registrationDetailsDTO);
				break;

			case VEHICLESTOPPAGE:
				updatesVehicleStoppage(regServiceDTO, registrationDetailsDTO);
				break;
			case VEHICLESTOPPAGEREVOKATION:
				updatesVehicleStoppageRevokation(regServiceDTO, registrationDetailsDTO);
				break;
			case PERDATAENTRY:
				updatePermitDataEntry(regServiceDTO, registrationDetailsDTO);
				break;
			/*
			 * case TODEATH: break; case TOFINANCIER: break;
			 */
			case RCFORFINANCE:
				updateFreshRc(regServiceDTO, registrationDetailsDTO);
				break;
			case RCCANCELLATION:
				updateRcCancelltion(regServiceDTO, registrationDetailsDTO);
				break;
			default:
				break;

			}
		}
		regServiceDTO.setApprovedDate(LocalDateTime.now());

		registrationUpdation(regServiceDTO, registrationDetailsDTO, oldregistrationDetailsDTO);

	}

	private void registrationUpdation(RegServiceDTO regServiceDTO, RegistrationDetailsDTO registrationDetailsDTO,
			RegistrationDetailsDTO oldregistrationDetailsDTO) {
		if (null != registrationDetailsDTO
				&& !StatusRegistration.PRNUMBERPENDING.equals(regServiceDTO.getApplicationStatus())) {

			oldregistrationDetailsDTO = findRegistrationDetails(regServiceDTO);
			registrationDetailsDTO.setServiceIds(regServiceDTO.getServiceIds().stream().collect(Collectors.toList()));
			dTOUtilService.moveToRegistrationLogsWithChecks(oldregistrationDetailsDTO);
			registrationDetailsDTO.setlUpdate(LocalDateTime.now());
			registrationDetailsDTO.setIsvahanSync(Boolean.FALSE);
			registrationDetailsDTO.setIsvahanSyncSkip(Boolean.FALSE);
			if (registrationDetailsDTO.getApplicantDetails().getPresentAddress().getMandal() == null) {
				if (regServiceDTO.getRegistrationDetails() != null && regServiceDTO.getRegistrationDetails()
						.getApplicantDetails().getPresentAddress().getMandal() != null) {
					/*
					 * logger.
					 * info("Mandal Details Not Availble in Reg_Details so we inserting from reg_services for AppNo:[{}]"
					 * , regServiceDTO.getPrNo());
					 */
					registrationDetailsDTO.getApplicantDetails().getPresentAddress().setMandal(regServiceDTO
							.getRegistrationDetails().getApplicantDetails().getPresentAddress().getMandal());
				}
				if (registrationDetailsDTO.isCfstSync()) {
					registrationDetailsDTO.setCfstSync(Boolean.FALSE);
					if (regServiceDTO.getServiceType().size() == 1 && CollectionUtils
							.containsAny(ServiceEnum.getCfstNotSyncEnums(), regServiceDTO.getServiceType())) {
						registrationDetailsDTO.setCfstSync(Boolean.TRUE);
					}
				}
			}

			vahanSync.commonVahansync(registrationDetailsDTO);
			registrationDetailDAO.save(registrationDetailsDTO);
		}

	}

	private void updateBasicDetails(RegServiceDTO regServiceDTO, RegistrationDetailsDTO registrationDetailsDTO) {

		if (regServiceDTO.getContactDetails() != null) {

			if (registrationDetailsDTO.getApplicantDetails().getContact() != null) {
				registrationDetailsDTO.getApplicantDetails().getContact()
						.setMobile(regServiceDTO.getContactDetails().getMobile());
				if (StringUtils.isNoneBlank(regServiceDTO.getContactDetails().getEmail())) {
					registrationDetailsDTO.getApplicantDetails().getContact()
							.setEmail(regServiceDTO.getContactDetails().getEmail());
				}
			} else {
				registrationDetailsDTO.getApplicantDetails().setContact(regServiceDTO.getContactDetails());
			}
		}
	}

	private void updatesTheftRevocation(RegServiceDTO regServiceDTO, RegistrationDetailsDTO registrationDetailsDTO) {
		registrationDetailsDTO.setTheftState(StatusRegistration.TheftState.REVOKED);
		regServiceDTO.setApprovedDate(LocalDateTime.now());
		registrationDetailsDTO.setTheftDetails(null);

	}

	private void updatesTheftIntimation(RegServiceDTO regServiceDTO, RegistrationDetailsDTO registrationDetailsDTO) {
		registrationDetailsDTO.setTheftState(StatusRegistration.TheftState.INTIMATIATED);
		regServiceDTO.setApprovedDate(LocalDateTime.now());
		registrationDetailsDTO.setTheftDetails(regServiceDTO.getTheftDetails());
	}

	private void updatesInsuranceAndPUCDetails(RegServiceDTO regServiceDTO,
			RegistrationDetailsDTO registrationDetailsDTO) {
		if (regServiceDTO.getInsuranceDetails() != null) {
			registrationDetailsDTO.setInsuranceDetails(regServiceDTO.getInsuranceDetails());
		}
		if (regServiceDTO.getPucDetails() != null) {
			registrationDetailsDTO.setPucDetailsDTO(regServiceDTO.getPucDetails());
		}

	}

	private void updateImageStatus(CitizenEnclosuresDTO citizenEnclosuresDTO) {
		for (KeyValue<String, List<ImageEnclosureDTO>> enclosures : citizenEnclosuresDTO.getEnclosures()) {
			for (ImageEnclosureDTO dto : enclosures.getValue()) {
				dto.setImageStaus(StatusRegistration.APPROVED.getDescription());
			}
		}
	}

	private RegistrationDetailsDTO findRegistrationDetails(RegServiceDTO regServiceDTO) {

		RegistrationDetailsDTO dto = null;
		if (regServiceDTO.getServiceIds().contains(ServiceEnum.DATAENTRY.getId())) {
			return regServiceDTO.getRegistrationDetails();
		} else if (regServiceDTO.getServiceIds().contains(ServiceEnum.RCCANCELLATION.getId())
				&& regServiceDTO.getIsOtherState()) {
			return regServiceDTO.getRegistrationDetails();
		}
		if (regServiceDTO.getRegistrationDetails() != null) {
			dto = registrationDetailDAO.findOne(regServiceDTO.getRegistrationDetails().getApplicationNo());
		}
		if (dto == null) {
			throw new BadRequestException("Registraion Details not found, registration application no: {}"
					+ regServiceDTO.getRegistrationDetails().getApplicationNo());
		}
		return dto;

	}

	private void updatesReassignmentData(RegServiceDTO regServiceDTO, RegistrationDetailsDTO registrationDetailsDTO,
			boolean isfromScheduler) {
		if (regServiceDTO.isSpecialNoRequired()) {
			registrationDetailsDTO.setPrNo(null);
			registrationDetailsDTO.setOldPrNo(regServiceDTO.getPrNo());

			StagingRegistrationDetailsDTO stagingRegistrationDetails = new StagingRegistrationDetailsDTO();
			BeanUtils.copyProperties(registrationDetailsDTO, stagingRegistrationDetails);
			if (regServiceDTO.getServiceIds() != null && !regServiceDTO.getServiceIds().isEmpty()
					&& regServiceDTO.getServiceIds().stream()
							.anyMatch(id -> id.equals(ServiceEnum.ALTERATIONOFVEHICLE.getId()))
					&& regServiceDTO.getAlterationDetails().getVehicleTypeTo()
							.equalsIgnoreCase(CovCategory.T.getCode())) {

				Optional<OfficeDTO> optionalOffice = officeDAO
						.findByOfficeCode(registrationDetailsDTO.getOfficeDetails().getOfficeCode());
				if (!optionalOffice.isPresent()) {
					throw new BadRequestException("Office details not found for: {}"
							+ registrationDetailsDTO.getOfficeDetails().getOfficeCode());
				}
				if (StringUtils.isBlank(optionalOffice.get().getReportingoffice())) {
					throw new BadRequestException(
							"Reporting office field not found for: {}" + optionalOffice.get().getOfficeCode());
				}
				Optional<OfficeDTO> optionalReportingOffice = officeDAO
						.findByOfficeCode(optionalOffice.get().getReportingoffice());
				if (!optionalReportingOffice.isPresent()) {
					throw new BadRequestException(
							"Office details not found for: {}" + optionalOffice.get().getReportingoffice());
				}
				stagingRegistrationDetails.setOfficeDetails(optionalReportingOffice.get());
			}
			stagingRegistrationDetails.setApplicationStatus(StatusRegistration.SPECIALNOPENDING.getDescription());
			stagingRegistrationDetails.setSpecialNumberRequired(Boolean.TRUE);
			stagingRegistrationDetails.setReassignmentDoneDate(LocalDateTime.now());
			stagingRegistrationDetails.setPrType(BidNumberType.S.getCode());
			stagingRegistrationDetails.setFromReassigment(Boolean.TRUE);
			stagingRegistrationDetails.setOldPrNo(regServiceDTO.getPrNo());
			stagingRegistrationDetails.setPrNo(null);
			stagingRegistrationDetails.setIsNeedToUpdatePrNoInFc(regServiceDTO.isNeedToUpdatePrNoInFc());
			if (null != regServiceDTO.getContactDetails()) {
				ContactDTO contactDTO = stagingRegistrationDetails.getApplicantDetails().getContact();
				if (null == contactDTO) {
					contactDTO = new ContactDTO();
				}
				if (null != regServiceDTO.getContactDetails().getMobile()) {
					contactDTO.setMobile(regServiceDTO.getContactDetails().getMobile());
				}
				if (null != regServiceDTO.getContactDetails().getEmail()) {
					contactDTO.setEmail(regServiceDTO.getContactDetails().getEmail());
				}
				stagingRegistrationDetails.getApplicantDetails().setContact(contactDTO);
			}
			if (null != stagingRegistrationDetails.getApplicantDetails().getContact()
					&& null == stagingRegistrationDetails.getApplicantDetails().getContact().getEmail()) {
				Optional<PropertiesDTO> propertiesDto = propertiesDAO.findByIsEmailMissingTrue();
				if (propertiesDto.isPresent()) {
					stagingRegistrationDetails.getApplicantDetails().getContact()
							.setEmail(propertiesDto.get().getEmail());
				}
			}

			// Removing bidAlterationDetails.
			if (CollectionUtils.isNotEmpty(stagingRegistrationDetails.getBidAlterDetails())) {
				if (null == stagingRegistrationDetails.getBidAlterDetailsLogs()) {
					stagingRegistrationDetails.setBidAlterDetailsLogs(new ArrayList<>());
				}
				stagingRegistrationDetails.getBidAlterDetailsLogs()
						.addAll(stagingRegistrationDetails.getBidAlterDetails());
				stagingRegistrationDetails.setBidAlterDetails(new ArrayList<>());
			}

			stagingRegistrationDetailsDAO.save(stagingRegistrationDetails);
			// regServiceDTO.setRegistrationDetails(null);
			return;
		}
		if (prService.isAssignNumberNow() || isfromScheduler) {
			processForReassignmenetPRGeberation(regServiceDTO, registrationDetailsDTO);
		} else {
			regServiceDTO.setApplicationStatus(StatusRegistration.PRNUMBERPENDING);
			regServiceDAO.save(regServiceDTO);
			return;
		}

	}

	private void processForReassignmenetPRGeberation(RegServiceDTO regServiceDTO,
			RegistrationDetailsDTO registrationDetailsDTO) {
		List<PrBackUpDetailsDTO> PrBackUpDetailsList = new ArrayList<>();

		PrBackUpDetailsDTO prBackUpDetailsDTO = new PrBackUpDetailsDTO();
		prBackUpDetailsDTO.setFromPrNo(regServiceDTO.getPrNo());

		String prNo = prService.geneatePrNo(regServiceDTO.getApplicationNo(), Integer.MIN_VALUE, Boolean.FALSE,
				StringUtils.EMPTY, ModuleEnum.CITIZEN, Optional.empty());

		prBackUpDetailsDTO.setToPrNo(prNo);
		prBackUpDetailsDTO.setPrUpdatedDate(LocalDateTime.now());
		registrationDetailsDTO.setOldPrNo(registrationDetailsDTO.getPrNo());
		registrationDetailsDTO.setPrNo(prNo);
		// registrationDetailsDTO.setPrGeneratedDate(LocalDateTime.now());
		registrationDetailsDTO.setlUpdate(LocalDateTime.now());
		PrBackUpDetailsList.add(prBackUpDetailsDTO);
		if (null == registrationDetailsDTO.getPrBackUpDetailsList()) {
			registrationDetailsDTO.setPrBackUpDetailsList(PrBackUpDetailsList);
		} else {
			registrationDetailsDTO.getPrBackUpDetailsList().add(prBackUpDetailsDTO);
		}

		regServiceDTO.setPrNo(prNo);
		regServiceDTO.setApprovedDate(LocalDateTime.now());

		if (null == regServiceDTO.getRegistrationDetails().getPrBackUpDetailsList()) {
			regServiceDTO.getRegistrationDetails().setPrBackUpDetailsList(PrBackUpDetailsList);
		} else {
			regServiceDTO.getRegistrationDetails().getPrBackUpDetailsList().add(prBackUpDetailsDTO);
		}
		if (regServiceDTO.isNeedToUpdatePrNoInFc()) {
			List<FcDetailsDTO> listOfFcDetails = fcDetailsDAO
					.findFirst5ByApplicationNoOrderByCreatedDateDesc(registrationDetailsDTO.getApplicationNo());
			if (!listOfFcDetails.isEmpty()) {
				listOfFcDetails.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
				FcDetailsDTO dto = listOfFcDetails.stream().findFirst().get();
				if (!dto.getPrNo().equalsIgnoreCase(registrationDetailsDTO.getPrNo())) {
					dto.setPrNo(registrationDetailsDTO.getPrNo());
					fcDetailsDAO.save(dto);
				}
			}
			permitsService.updatePermitDetailsAfterReassignment(registrationDetailsDTO);
		}

	}

	private RegistrationDetailsDTO updatesTransferofOwnershipData(RegServiceDTO regServiceDTO,
			RegistrationDetailsDTO registrationDetailsDTO) {
		String aadhaarNo = StringUtils.EMPTY;
		try {
			if (registrationDetailsDTO.getApplicantDetails() != null
					&& registrationDetailsDTO.getApplicantDetails().getAadharNo() != null) {
				aadhaarNo = registrationDetailsDTO.getApplicantDetails().getAadharNo();
			} else if (regServiceDTO != null && regServiceDTO.getBuyerDetails() != null
					&& regServiceDTO.getBuyerDetails().getTransferType() != null
					&& regServiceDTO.getBuyerDetails().getTransferType().equals(TransferType.SALE)) {
				throw new BadRequestException("Aadhaar Number not available for seller : " + regServiceDTO.getPrNo());
			}
			registrationDetailsDTO.setApplicantDetails(regServiceDTO.getBuyerDetails().getBuyerApplicantDetails());
			if (regServiceDTO.getBuyerDetails().getOwnerShipType().getCode() != null) {
				registrationDetailsDTO.setOwnerType(
						OwnerTypeEnum.getOwnerType(regServiceDTO.getBuyerDetails().getOwnerShipType().getCode()));
			} else {
				registrationDetailsDTO.setOwnerType(regServiceDTO.getRegistrationDetails().getOwnerType());
			}

			registrationDetailsDTO.setCfstSync(Boolean.FALSE);
			regServiceDTO.getBuyerDetails().getBuyerApplicantDetails()
					.setRegId(registrationDetailsDTO.getApplicationNo());
			applicantDetailsDAO.save(regServiceDTO.getBuyerDetails().getBuyerApplicantDetails());
			registrationDetailsDTO.setOfficeDetails(regServiceDTO.getOfficeDetails());

			if (null != regServiceDTO.getInsuranceDetails()) {
				registrationDetailsDTO.setInsuranceDetails(regServiceDTO.getInsuranceDetails());
			}
			if (null != regServiceDTO.getPucDetails()) {
				registrationDetailsDTO.setPucDetailsDTO(regServiceDTO.getPucDetails());
			}
			registrationDetailsDTO.setPanDetails(null);
		} catch (Exception e) {
			logger.error("Exception occured while saving TOW for prNo:{}", registrationDetailsDTO.getPrNo());
			throw new BadRequestException("problem occured while approving, so please contact support team : "
					+ registrationDetailsDTO.getPrNo());
		}
		Optional<ElasticSecondVehicleDTO> elasticSecondVehicleDto = elasticSecondVehicleDAO
				.findByPrNumber(registrationDetailsDTO.getPrNo());

		if (elasticSecondVehicleDto.isPresent()) {
			saveElasticSearchData(elasticSecondVehicleDto.get(), registrationDetailsDTO);
		} else {
			ElasticSecondVehicleDTO elasticData = new ElasticSecondVehicleDTO();
			saveElasticSearchData(elasticData, registrationDetailsDTO);
		}
		if (regServiceDTO.getBuyerDetails().getSellerPermitStatus() != null) {
			List<PermitDetailsDTO> permitDetailsDTOList = permitDetails(registrationDetailsDTO.getPrNo());
			if (!permitDetailsDTOList.isEmpty()
					&& regServiceDTO.getBuyerDetails().getSellerPermitStatus()
							.equals(TransferType.permitTranfer.PERMITCANCEL)
					|| regServiceDTO.getBuyerDetails().getSellerPermitStatus()
							.equals(TransferType.permitTranfer.PERMITSURRENDER)) {
				for (PermitDetailsDTO permitDetails : permitDetailsDTOList) {
					if (!permitDetails.getPermitClass().getDescription()
							.equals(PermitsEnum.PermitType.COUNTER_SIGNATURE.getDescription())) {
						permitDetails.setPermitStatus(PermitsEnum.INACTIVE.getDescription());
						permitDetails.setPermitSurrender(true);
						permitDetails.setIsPermitSurrenderWithTOW(true);
						permitDetails.setPermitSurrenderDate(LocalDate.now());
					}
				}
				permitDetailsDAO.save(permitDetailsDTOList);
				logger.info("Permit Cancel/Surrendered by seller for PRNO {}", registrationDetailsDTO.getPrNo());
			}
			if (regServiceDTO.getBuyerDetails().getBuyerPermitStatus() != null
					&& TransferType.permitTranfer.PERMITTRANSFER
							.equals(regServiceDTO.getBuyerDetails().getBuyerPermitStatus())) {
				Optional<PermitDetailsDTO> permitDetails = permitDetailsDTOList.stream().filter(id -> id
						.getPermitClass().getDescription().equals(PermitsEnum.PermitType.PRIMARY.getDescription()))
						.findFirst();
				if (!permitDetails.isPresent()) {
					throw new BadRequestException(
							"No records found in permits to transfer: " + regServiceDTO.getPrNo());
				}
				if (permitDetails.get().getRdto() == null) {
					permitDetails.get().setRdto(regServiceDTO.getRegistrationDetails());
				}
				permitDetails.get().getRdto()
						.setApplicantDetails(regServiceDTO.getBuyerDetails().getBuyerApplicantDetails());
				if (regServiceDTO.getBuyerDetails().getOwnerShipType() != null) {
					permitDetails.get().getRdto().setOwnerType(
							OwnerTypeEnum.getOwnerType(regServiceDTO.getBuyerDetails().getOwnerShipType().getCode()));
				}
				logger.info("Permit Transfered to Buyer for PRNO {}", registrationDetailsDTO.getPrNo());
				permitDetailsDAO.save(permitDetails.get());
			}

			if (regServiceDTO.getBuyerDetails().getSellerRecommedationLetterStatus() != null
					&& TransferType.permitTranfer.RECOMMENDATIONLETTERSURRENDER
							.equals(regServiceDTO.getBuyerDetails().getBuyerRecommedationLetterStatus())) {

				permitDetailsDTOList.stream().forEach(permitDetails -> {

					if (permitDetails.getPermitClass().getDescription()
							.equalsIgnoreCase(PermitsEnum.PermitType.COUNTER_SIGNATURE.getDescription())) {
						permitDetails.setPermitStatus(PermitsEnum.INACTIVE.getDescription());
						permitDetails.setPermitSurrender(true);
						permitDetails.setIsPermitSurrenderWithTOW(true);
						permitDetails.setPermitSurrenderDate(LocalDate.now());
					}
				});
				permitDetailsDAO.save(permitDetailsDTOList);
			}

			if (regServiceDTO.getBuyerDetails().getBuyerRecommedationLetterStatus() != null
					&& TransferType.permitTranfer.RECOMMENDATIONLETTERTRANSFER
							.equals(regServiceDTO.getBuyerDetails().getBuyerRecommedationLetterStatus())) {

				permitDetailsDTOList.stream().forEach(permitDetails -> {
					if (permitDetails.getPermitClass().getDescription()
							.equalsIgnoreCase(PermitsEnum.PermitType.COUNTER_SIGNATURE.getDescription())) {
						if (permitDetails.getRdto() == null) {
							permitDetails.setRdto(regServiceDTO.getRegistrationDetails());
						}
						permitDetails.getRdto()
								.setApplicantDetails(regServiceDTO.getBuyerDetails().getBuyerApplicantDetails());
						if (regServiceDTO.getBuyerDetails().getOwnerShipType() != null) {
							permitDetails.getRdto().setOwnerType(OwnerTypeEnum
									.getOwnerType(regServiceDTO.getBuyerDetails().getOwnerShipType().getCode()));
						}
					}

					logger.info("Permit Transfered to Buyer for PRNO {}", registrationDetailsDTO.getPrNo());
					permitDetailsDAO.save(permitDetails);

				});
			}

		}

		Optional<EductaionInstituteVehicleDetailsDto> eibtdusDetials = eductaionInstituteVehicleDetailsDao
				.findByApplicationNo(registrationDetailsDTO.getApplicationNo());
		if (eibtdusDetials.isPresent()) {
			EductaionInstituteVehicleDetailsDto eibtDto = eibtdusDetials.get();
			eibtDto.setTowDone(Boolean.TRUE);
			eductaionInstituteVehicleDetailsDao.save(eibtDto);
		}
		removingRecordfromRepresentative(aadhaarNo, registrationDetailsDTO.getApplicationNo());
		return registrationDetailsDTO;

	}

	private RegistrationDetailsDTO updatesAlterationofVehicleData(RegServiceDTO regServiceDTO,
			RegistrationDetailsDTO dto) {

		if (regServiceDTO.getAlterationDetails() == null) {
			throw new BadRequestException("Alteration details no found  for prNo: " + regServiceDTO.getPrNo());
		}
		regServiceDTO.setApprovedDate(LocalDateTime.now());
		if (regServiceDTO.getAlterationDetails().getCov() != null
				&& StringUtils.isNoneBlank(regServiceDTO.getAlterationDetails().getCov())
				&& regServiceDTO.getPrNo() != null && StringUtils.isNoneBlank(regServiceDTO.getPrNo())
				&& regServiceDTO.getAlterationDetails().getOldPrNo() != null) {
			List<TaxDetailsDTO> taxDetailsList = taxDetailsDAO
					.findFirst10ByPrNoOrderByCreatedDateDesc(regServiceDTO.getAlterationDetails().getOldPrNo());
			if (!taxDetailsList.isEmpty()) {
				TaxDetailsDTO taxDetails = taxDetailsList.stream().findFirst().get();
				taxDetails.setPrNo(regServiceDTO.getPrNo());
				if (StringUtils.isNotEmpty(regServiceDTO.getAlterationDetails().getPrNo())
						&& regServiceDTO.getServiceIds().contains(ServiceEnum.REASSIGNMENT.getId())) {
					taxDetails.setPrNo(regServiceDTO.getAlterationDetails().getPrNo());
				}
				taxDetails.setClassOfVehicle(regServiceDTO.getAlterationDetails().getCov());
				taxDetailsDAO.save(taxDetails);
				rcCancellationService.nonPaymentMoveToHistory(taxDetails);
			}
		}
		if (regServiceDTO.getAlterationDetails().getCov() != null
				&& StringUtils.isNoneBlank(regServiceDTO.getAlterationDetails().getCov())) {
			updateCovsinLog(dto, regServiceDTO);
			dto.setClassOfVehicle(regServiceDTO.getAlterationDetails().getCov());
			dto.getVehicleDetails().setClassOfVehicle(regServiceDTO.getAlterationDetails().getCov());
			ClassOfVehiclesDTO classOfVehicle = classOfVehiclesDAO
					.findByCovcode(regServiceDTO.getAlterationDetails().getCov());
			if (classOfVehicle == null) {
				throw new BadRequestException("class Of vehicle not found in master collection: "
						+ regServiceDTO.getAlterationDetails().getCov());
			}
			dto.setClassOfVehicleDesc(classOfVehicle.getCovdescription());
			dto.getVehicleDetails().setClassOfVehicleDesc(classOfVehicle.getCovdescription());
		}

		if (regServiceDTO.getAlterationDetails().getVehicleTypeTo() != null) {
			dto.setVehicleType(regServiceDTO.getAlterationDetails().getVehicleTypeTo());
			if (regServiceDTO.getAlterationDetails().getVehicleTypeFrom() != null
					&& regServiceDTO.getAlterationDetails().getVehicleTypeFrom()
							.equalsIgnoreCase(CovCategory.T.getCode())
					&& regServiceDTO.getAlterationDetails().getVehicleTypeTo()
							.equalsIgnoreCase(CovCategory.N.getCode())) {
				if (LocalDate.now().isBefore(dto.getPrGeneratedDate().minusDays(1)
						.plusYears(ValidityEnum.PR_NON_TRANSPORT_VALIDITY.getValidity()).toLocalDate())) {
					dto.getRegistrationValidity().setRegistrationValidity(dto.getPrGeneratedDate().minusDays(1)
							.plusYears(ValidityEnum.PR_NON_TRANSPORT_VALIDITY.getValidity()));
				} else {
					dto.getRegistrationValidity().setRegistrationValidity(LocalDateTime.now().plusYears(5));
				}

			} else if (regServiceDTO.getAlterationDetails().getVehicleTypeFrom() != null
					&& regServiceDTO.getAlterationDetails().getVehicleTypeFrom()
							.equalsIgnoreCase(CovCategory.N.getCode())
					&& regServiceDTO.getAlterationDetails().getVehicleTypeTo()
							.equalsIgnoreCase(CovCategory.T.getCode())) {
				this.saveFCData(dto, regServiceDTO);
				regServiceDTO.setNeedToUpdatePrNoInFc(Boolean.TRUE);
				LocalDateTime date = calculateFcValidity(regServiceDTO);
				dto.getRegistrationValidity().setRegistrationValidity(date);
				dto.getRegistrationValidity().setFcValidity(date.toLocalDate());
			} else if (regServiceDTO.getAlterationDetails().getVehicleTypeFrom() != null
					&& regServiceDTO.getAlterationDetails().getVehicleTypeFrom()
							.equalsIgnoreCase(CovCategory.T.getCode())
					&& regServiceDTO.getAlterationDetails().getVehicleTypeTo()
							.equalsIgnoreCase(CovCategory.T.getCode())) {
				Optional<ClassOfVehicleConversion> covConversion = classOfVehicleConversionDAO
						.findByNewCovAndNewCategoryAndCovAndCategory(regServiceDTO.getAlterationDetails().getCov(),
								regServiceDTO.getAlterationDetails().getVehicleTypeTo(),
								regServiceDTO.getAlterationDetails().getFromCov(),
								regServiceDTO.getAlterationDetails().getVehicleTypeFrom());
				if (!covConversion.isPresent()) {
					logger.error("no records found in master conversion document :[{}] ", regServiceDTO.getPrNo());
					throw new BadRequestException(
							"no records found in master conversion document: " + regServiceDTO.getPrNo());
				}
				if (covConversion.get().isFcFee()) {
					this.saveFCData(dto, regServiceDTO);
					LocalDateTime date = calculateFcValidity(regServiceDTO);
					dto.getRegistrationValidity().setRegistrationValidity(date);
					dto.getRegistrationValidity().setFcValidity(date.toLocalDate());
				}
			}
		}

		if (regServiceDTO.getAlterationDetails().getBodyType() != null
				&& StringUtils.isNoneBlank(regServiceDTO.getAlterationDetails().getBodyType())) {
			dto.getVahanDetails().setBodyTypeDesc(regServiceDTO.getAlterationDetails().getBodyType());
			dto.getVehicleDetails().setBodyTypeDesc(regServiceDTO.getAlterationDetails().getBodyType());
		}
		// As per jagan inputs added color
		if (regServiceDTO.getAlterationDetails().getBodyType() != null
				&& (StringUtils.isNoneBlank(regServiceDTO.getAlterationDetails().getMviEnterdColor()))) {
			dto.getVahanDetails().setColor(regServiceDTO.getAlterationDetails().getMviEnterdColor());
			dto.getVehicleDetails().setColor(regServiceDTO.getAlterationDetails().getMviEnterdColor());
		}
		if (regServiceDTO.getAlterationDetails().getFuel() != null
				&& StringUtils.isNoneBlank(regServiceDTO.getAlterationDetails().getFuel())) {
			dto.getVahanDetails().setFuelDesc(regServiceDTO.getAlterationDetails().getFuel());
			dto.getVehicleDetails().setFuelDesc(regServiceDTO.getAlterationDetails().getFuel());
		}
		if (regServiceDTO.getAlterationDetails().getSeating() != null
				&& StringUtils.isNoneBlank(regServiceDTO.getAlterationDetails().getSeating())) {
			dto.getVahanDetails().setSeatingCapacity(regServiceDTO.getAlterationDetails().getSeating());
			dto.getVehicleDetails().setSeatingCapacity(regServiceDTO.getAlterationDetails().getSeating());
		}
		if (regServiceDTO.getAlterationDetails().getUlw() != null) {
			dto.getVahanDetails().setUnladenWeight(regServiceDTO.getAlterationDetails().getUlw());
			dto.getVehicleDetails().setUlw(regServiceDTO.getAlterationDetails().getUlw());
		}
		if (regServiceDTO.getAlterationDetails().getCov() != null && regServiceDTO.getAlterationDetails().getCov()
				.equalsIgnoreCase(ClassOfVehicleEnum.ARVT.getCovCode())) {
			if (regServiceDTO.getAlterationDetails().getTrailers() == null
					|| regServiceDTO.getAlterationDetails().getTrailers().isEmpty()) {
				logger.error("Trailer details not found :[{}] ", regServiceDTO.getPrNo());
				throw new BadRequestException("Trailer details not found: " + regServiceDTO.getPrNo());
			}
			dto.getVahanDetails().setTrailerChassisDetailsDTO(regServiceDTO.getAlterationDetails().getTrailers());
			dto.getVehicleDetails().setTrailers(regServiceDTO.getAlterationDetails().getTrailers());
		}
		if (regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VARIATIONOFPERMIT.getId()))) {
			List<PermitDetailsDTO> listOfPermits = permitDetailsDAO.findByPrNoInAndPermitClassCodeAndPermitStatus(
					Arrays.asList(regServiceDTO.getPrNo()), PermitsEnum.PermitType.PRIMARY.getPermitTypeCode(),
					PermitsEnum.ACTIVE.getDescription());

			if (listOfPermits != null && !listOfPermits.isEmpty()) {
				List<PermitDetailsDTO> list = new ArrayList<>();
				for (PermitDetailsDTO permits : listOfPermits) {
					if (permits.getCreatedDate() != null) {
						list.add(permits);
					}
				}
				list.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
				PermitDetailsDTO permitsDtos = list.stream().findFirst().get();
				if (permitsDtos.getRdto() != null) {
					permitsDtos.setRdto(dto);
					permitDetailsDAO.save(permitsDtos);
					list.clear();
				}

			}

		}
		return dto;

	}

	private RegistrationDetailsDTO updatesIssueofNOCData(RegServiceDTO regServiceDTO,
			RegistrationDetailsDTO registrationDetailsDTO) {

		regServiceDTO.setApprovedDate(LocalDateTime.now());
		regServiceDTO.getnOCDetails().setNocIssued(Boolean.TRUE);
		nocDetailsDAO.save(regServiceDTO.getnOCDetails());
		registrationDetailsDTO.setNocDetails(regServiceDTO.getnOCDetails());
		return registrationDetailsDTO;

	}

	private RegistrationDetailsDTO updatesCancellationofNOCData(RegServiceDTO regServiceDTO,
			RegistrationDetailsDTO registrationDetailsDTO) {

		regServiceDTO.setApprovedDate(LocalDateTime.now());
		regServiceDTO.getnOCDetails().setNocIssued(Boolean.FALSE);
		nocDetailsDAO.save(regServiceDTO.getnOCDetails());
		registrationDetailsDTO.setNocDetails(null);
		return registrationDetailsDTO;

	}

	private RegistrationDetailsDTO updatesDuplicateData(RegServiceDTO regServiceDTO,
			RegistrationDetailsDTO registrationDetailsDTO) {

		regServiceDTO.setApprovedDate(LocalDateTime.now());
		registrationDetailsDTO.setCardPrinted(Boolean.FALSE);
		registrationDetailsDTO.setDuplicate(Boolean.TRUE);
		return registrationDetailsDTO;
	}

	public void updatesDataEntryData(RegServiceDTO regServiceDTO, RegistrationDetailsDTO registrationDetailsDTO) {

		if (regServiceDTO.getRegistrationDetails().getClassOfVehicle()
				.equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode())
				|| regServiceDTO.getRegistrationDetails().getClassOfVehicle()
						.equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())) {
			Optional<AlterationDTO> optionalAlt = alterationDAO.findByApplicationNo(regServiceDTO.getApplicationNo());
			if (!optionalAlt.isPresent()) {
				logger.error("alteration details not found :[{}] ", regServiceDTO.getPrNo());
				throw new BadRequestException("alteration details not found: " + regServiceDTO.getPrNo());
			}
			AlterationDTO altDto = optionalAlt.get();
			regServiceDTO.getRegistrationDetails().setClassOfVehicle(altDto.getCov());
			regServiceDTO.getRegistrationDetails().getVehicleDetails().setClassOfVehicle(altDto.getCov());
			registrationDetailsDTO.setClassOfVehicle(altDto.getCov());
			registrationDetailsDTO.getVehicleDetails().setClassOfVehicle(altDto.getCov());
			ClassOfVehiclesDTO classOfVehicle = classOfVehiclesDAO.findByCovcode(altDto.getCov());
			if (classOfVehicle == null) {
				throw new BadRequestException("class Of vehicle not found in masetr collection: " + altDto.getCov());
			}
			regServiceDTO.getRegistrationDetails().setClassOfVehicleDesc(classOfVehicle.getCovdescription());
			registrationDetailsDTO.setClassOfVehicleDesc(classOfVehicle.getCovdescription());
			regServiceDTO.getRegistrationDetails().getVehicleDetails()
					.setClassOfVehicleDesc(classOfVehicle.getCovdescription());
			registrationDetailsDTO.getVehicleDetails().setClassOfVehicleDesc(classOfVehicle.getCovdescription());

			registrationDetailsDTO.getVahanDetails().setSeatingCapacity(altDto.getSeating());
			registrationDetailsDTO.getVehicleDetails().setSeatingCapacity(altDto.getSeating());
			regServiceDTO.getRegistrationDetails().getVahanDetails().setSeatingCapacity(altDto.getSeating());
			regServiceDTO.getRegistrationDetails().getVehicleDetails().setSeatingCapacity(altDto.getSeating());

			registrationDetailsDTO.getVahanDetails().setBodyTypeDesc(altDto.getBodyType());
			registrationDetailsDTO.getVehicleDetails().setBodyTypeDesc(altDto.getBodyType());
			regServiceDTO.getRegistrationDetails().getVahanDetails().setBodyTypeDesc(altDto.getBodyType());
			regServiceDTO.getRegistrationDetails().getVehicleDetails().setBodyTypeDesc(altDto.getBodyType());

			registrationDetailsDTO.getVahanDetails().setColor(altDto.getColor());
			registrationDetailsDTO.getVehicleDetails().setColor(altDto.getColor());
			regServiceDTO.getRegistrationDetails().getVahanDetails().setColor(altDto.getColor());
			regServiceDTO.getRegistrationDetails().getVehicleDetails().setColor(altDto.getColor());

			registrationDetailsDTO.getVahanDetails().setUnladenWeight(altDto.getUlw());
			registrationDetailsDTO.getVehicleDetails().setUlw(altDto.getUlw());
			regServiceDTO.getRegistrationDetails().getVahanDetails().setUnladenWeight(altDto.getUlw());
			regServiceDTO.getRegistrationDetails().getVehicleDetails().setUlw(altDto.getUlw());

			if (altDto.getCov().equalsIgnoreCase(ClassOfVehicleEnum.ARVT.getCovCode())) {
				if (altDto.getTrailers() == null || altDto.getTrailers().isEmpty()) {
					logger.error("Trailer details not found :[{}] ", regServiceDTO.getPrNo());
					throw new BadRequestException("Trailer details not found: " + regServiceDTO.getPrNo());
				}
				registrationDetailsDTO.getVahanDetails().setTrailerChassisDetailsDTO(altDto.getTrailers());
				registrationDetailsDTO.getVehicleDetails().setTrailers(altDto.getTrailers());
				regServiceDTO.getRegistrationDetails().getVahanDetails()
						.setTrailerChassisDetailsDTO(altDto.getTrailers());
				regServiceDTO.getRegistrationDetails().getVehicleDetails().setTrailers(altDto.getTrailers());

			}
		}
		if (regServiceDTO.isSpecialNoRequired() || regServiceDTO.getRegistrationDetails().getSpecialNumberRequired()) {
			if (!regServiceDTO.getRegistrationDetails().isRegVehicleWithTR()) {
				trGenerationForDataEntry(regServiceDTO);
			}
			StagingRegistrationDetailsDTO stagingRegistrationDetails = new StagingRegistrationDetailsDTO();
			BeanUtils.copyProperties(regServiceDTO.getRegistrationDetails(), stagingRegistrationDetails);
			if (regServiceDTO.getRegistrationDetails().getClassOfVehicle()
					.equalsIgnoreCase(ClassOfVehicleEnum.CHST.getCovCode())
					|| regServiceDTO.getRegistrationDetails().getClassOfVehicle()
							.equalsIgnoreCase(ClassOfVehicleEnum.CHSN.getCovCode())
					|| regServiceDTO.getRegistrationDetails().getClassOfVehicle()
							.equalsIgnoreCase(ClassOfVehicleEnum.ARVT.getCovCode())) {
				stagingRegistrationDetails.setApplicationStatus(StatusRegistration.CHASSISTRGENERATED.getDescription());
				this.otherStateAlterationDetails(regServiceDTO);
			} else {
				stagingRegistrationDetails.setApplicationStatus(StatusRegistration.SPECIALNOPENDING.getDescription());
			}
			stagingRegistrationDetails.setSpecialNumberRequired(Boolean.TRUE);
			stagingRegistrationDetails.setReassignmentDoneDate(LocalDateTime.now());
			stagingRegistrationDetails.setPrType(BidNumberType.S.getCode());
			// pr no should not be empty
			if (StringUtils.EMPTY.equals(stagingRegistrationDetails.getPrNo())) {
				stagingRegistrationDetails.setPrNo(null);
			}
			if (regServiceDTO.getRegistrationDetails().isRegVehicleWithPR()
					&& regServiceDTO.getRegistrationDetails().isTaxPaidByVcr()
					|| regServiceDTO.getRegistrationDetails().isRegVehicleWithTR()
							&& regServiceDTO.getRegistrationDetails().isTaxPaidByVcr()) {
				TaxDetailsDTO taxdetails = updateTaxDetailsForDataEntry(regServiceDTO);
				regServiceDTO.setTaxAmount(taxdetails.getTaxAmount());
				regServiceDTO.setTaxvalidity(taxdetails.getTaxPeriodEnd());
				taxDetailsDAO.save(taxdetails);
			} else {
				if (regServiceDTO.getTaxAmount() == null) {
					this.saveChassisTax(regServiceDTO);
				}
			}
			stagingRegistrationDetailsDAO.save(stagingRegistrationDetails);
			registrationDetailsDTO = null;
			return;
		}
		if (regServiceDTO.getOtherStateVoluntaryTax() != null
				&& regServiceDTO.getTaxType().equalsIgnoreCase("VoluntaryTax")) {
			TaxDetailsDTO taxdetails = updateVoluntaryTaxTaxDetailsForDataEntry(regServiceDTO);
			regServiceDTO.setTaxAmount(taxdetails.getTaxAmount());
			regServiceDTO.setTaxvalidity(taxdetails.getTaxPeriodEnd());
			taxDetailsDAO.save(taxdetails);
		}
		if (regServiceDTO.getRegistrationDetails().getApplicantType() != null
				&& !regServiceDTO.getRegistrationDetails().isTaxPaidByVcr()
				&& (regServiceDTO.getRegistrationDetails().getApplicantType().equalsIgnoreCase("WITHINTHESTATE")
						|| regServiceDTO.getRegistrationDetails().getApplicantType().equalsIgnoreCase("Paper RC"))) {
			TaxDetailsDTO taxdetails = updateTaxDetailsForDataEntryWithPaperRC(regServiceDTO);
			regServiceDTO.setTaxAmount(taxdetails.getTaxAmount());
			regServiceDTO.setTaxvalidity(taxdetails.getTaxPeriodEnd());
			regServiceDTO.getRegistrationDetails().setApplicantType("Paper RC");
			taxDetailsDAO.save(taxdetails);
		}

		// This block will execute for UnRegistered vehicle WITH-TR
		if (regServiceDTO.getOtherStateVoluntaryTax() != null
				&& regServiceDTO.getTaxType().equalsIgnoreCase("VoluntaryTax")
				&& regServiceDTO.getRegistrationDetails().getTaxType().equalsIgnoreCase("VoluntaryTax")) {
			regServiceDTO.getRegistrationDetails().setTaxType(regServiceDTO.getOtherStateVoluntaryTax().getTaxType());
		}
		processForOSRegistration(regServiceDTO, registrationDetailsDTO, false);
		registrationDetailDAO.save(regServiceDTO.getRegistrationDetails());

	}

	private void processForOSRegistration(RegServiceDTO regServiceDTO, RegistrationDetailsDTO registrationDetailsDTO,
			boolean isfromScheduler) {

		RegistrationValidityDTO registrationValidityDTO = (regServiceDTO.getRegistrationDetails()
				.getRegistrationValidity() != null) ? regServiceDTO.getRegistrationDetails().getRegistrationValidity()
						: new RegistrationValidityDTO();
		regServiceDTO.getRegistrationDetails().setDataInsertedByDataEntry(true);

		// This block will execute for UnRegistered vehicle WITH-TR
		if (regServiceDTO.getRegistrationDetails().isRegVehicleWithTR()) {
			// create PRNo
			if (StringUtils.isBlank(regServiceDTO.getPrNo())) {
				if (prService.isAssignNumberNow() || isfromScheduler) {
					prGenerationForDataEntry(regServiceDTO);
				} else {
					regServiceDTO.setApplicationStatus(StatusRegistration.PRNUMBERPENDING);
					return;
				}

			}
			if (null != regServiceDTO.getRegistrationDetails().getTrIssueDate()) {
				regServiceDTO.getRegistrationDetails()
						.setTrGeneratedDate((regServiceDTO.getRegistrationDetails().getTrIssueDate().atStartOfDay()));
				registrationValidityDTO.setTrGeneratedDate(regServiceDTO.getRegistrationDetails().getTrIssueDate());
			}
			this.saveFCDataForOtherState(registrationDetailsDTO, regServiceDTO);
		} else if (regServiceDTO.getRegistrationDetails().isRegVehicleWithPR()) {
			// This block will execute for Registered vehicle
			registeredVehicleUpdations(regServiceDTO, registrationValidityDTO);
			// Update FC and NOC Details
			updateRegVehicleDetaisForDataEntry(registrationDetailsDTO, regServiceDTO);
		} else { // This block will execute for UnRegistered vehicle WITHOUT-TR
			// create TRNo and PRNo
			if (StringUtils.isBlank(regServiceDTO.getTrNo())) {
				if (!(prService.isAssignNumberNow() || isfromScheduler)) {
					regServiceDTO.setApplicationStatus(StatusRegistration.PRNUMBERPENDING);
					return;
				}
				updateNonRegVehicleDetailsForDataEntry(regServiceDTO);
			}
		}
		suportDataUpdations(regServiceDTO, registrationValidityDTO);

	}

	private void suportDataUpdations(RegServiceDTO regServiceDTO, RegistrationValidityDTO registrationValidityDTO) {
		commonValidityUpdations(regServiceDTO, registrationValidityDTO);
		if (regServiceDTO.getRegistrationDetails().isRegVehicleWithPR()
				&& regServiceDTO.getRegistrationDetails().isTaxPaidByVcr()
				|| regServiceDTO.getRegistrationDetails().isRegVehicleWithTR()
						&& regServiceDTO.getRegistrationDetails().isTaxPaidByVcr()) {
			TaxDetailsDTO taxdetails = updateTaxDetailsForDataEntry(regServiceDTO);
			regServiceDTO.setTaxAmount(taxdetails.getTaxAmount());
			regServiceDTO.setTaxvalidity(taxdetails.getTaxPeriodEnd());
			taxDetailsDAO.save(taxdetails);
		} else {
			if (regServiceDTO.getTaxAmount() == null) {
				this.saveChassisTax(regServiceDTO);
			}
		}
		if (regServiceDTO.getRegistrationDetails().isTaxPaidByVcr()) {
			regServiceDTO.getRegistrationDetails().setTaxPaidByVcr(false);
		}
		regServiceDTO.getRegistrationDetails().setApplicationStatus(StatusRegistration.PRGENERATED.getDescription());
		regServiceDTO.getRegistrationDetails().setRegistrationValidity(registrationValidityDTO);
		applicantDetailsDAO.save(regServiceDTO.getRegistrationDetails().getApplicantDetails());
		regServiceDTO.setApprovedDate(LocalDateTime.now());
		regServiceDTO.getRegistrationDetails().setCreatedDate(LocalDateTime.now());
		if (regServiceDTO.getRegistrationDetails() != null
				&& regServiceDTO.getRegistrationDetails().getlUpdate() == null) {
			regServiceDTO.getRegistrationDetails().setlUpdate(LocalDateTime.now());
		}
		if (regServiceDTO.getRegistrationDetails() != null
				&& regServiceDTO.getRegistrationDetails().getVahanDetails() != null
				&& regServiceDTO.getRegistrationDetails().getVahanDetails().getManufacturedMonthYear() != null) {
			// regServiceDTO.getRegistrationDetails().getVahanDetails().setManufacturedMonthYear(regServiceDTO
			// .getRegistrationDetails().getVahanDetails().getManufacturedMonthYear().substring(0,
			// 2)
			// +
			// regServiceDTO.getRegistrationDetails().getVahanDetails().getManufacturedMonthYear().substring(3));
		}
		// regServiceDTO.getRegistrationDetails().setCardPrinted(Boolean.TRUE);
		if (regServiceDTO.getRegistrationDetails().getPrNo() != null
				&& regServiceDTO.getRegistrationDetails().getPrNo().length() > 0) {
			if (StringUtils.substring(regServiceDTO.getRegistrationDetails().getPrNo(), 0, 2).equalsIgnoreCase("AP"))
				regServiceDTO.getRegistrationDetails().setAllowForReassignment(Boolean.FALSE);
			// srinivas --prNo contains ap no need to apply Reassignment
		}
		if (regServiceDTO.getRegistrationDetails().isRegVehicleWithPR()
				&& !(regServiceDTO.getRegistrationDetails().getApplicantType().equalsIgnoreCase("Paper RC")
						|| regServiceDTO.getRegistrationDetails().getApplicantType().equalsIgnoreCase("WITHINTHESTATE")
						|| regServiceDTO.getRegistrationDetails().getOwnerType().equals(OwnerTypeEnum.Government)
						|| regServiceDTO.getRegistrationDetails().getOwnerType().equals(OwnerTypeEnum.Stu)
						|| regServiceDTO.getRegistrationDetails().getOwnerType().equals(OwnerTypeEnum.POLICE))) {
			fianlDocVerification(regServiceDTO);
		}
		if (regServiceDTO.getRegistrationDetails().isRegVehicleWithPR()
				&& regServiceDTO.getRegistrationDetails().getIsCentralGovernamentOrDefenceEmployee()) {
			TaxDetailsDTO taxdetails = updateTaxDetailsForDataEntryWithPaperRC(regServiceDTO);
			regServiceDTO.setTaxAmount(taxdetails.getTaxAmount());
			regServiceDTO.setTaxvalidity(taxdetails.getTaxPeriodEnd());
			taxDetailsDAO.save(taxdetails);
		}
		if (regServiceDTO.getRegistrationDetails().isRegVehicleWithTR()) {
			this.taxPaidOnlineVcrWithTR(regServiceDTO);
		}

	}

	private void fianlDocVerification(RegServiceDTO regServiceDTO) {
		// TODO Auto-generated method stub
		if (StringUtils.isNoneBlank(regServiceDTO.getRegistrationDetails().getClassOfVehicle()) && !(regServiceDTO
				.getRegistrationDetails().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())
				|| (regServiceDTO.getRegistrationDetails().getClassOfVehicle()
						.equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode())
						&& regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw() != null
						&& regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw() <= 3000)
				|| regServiceDTO.getRegistrationDetails().getClassOfVehicle()
						.equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())
				|| regServiceDTO.getRegistrationDetails().getClassOfVehicle()
						.equalsIgnoreCase(ClassOfVehicleEnum.TRTT.getCovCode())
				|| regServiceDTO.getRegistrationDetails().getClassOfVehicle()
						.equalsIgnoreCase(ClassOfVehicleEnum.TRCN.getCovCode()))) {
			Optional<TaxDetailsDTO> taxdetalsOpt = taxDetailsDAO
					.findByApplicationNoOrderByCreatedDateDesc(regServiceDTO.getApplicationNo());
			if (!taxdetalsOpt.isPresent()) {
				throw new BadRequestException("Tax details not found [{}] " + regServiceDTO.getApplicationNo());
			}
		}

		List<PaymentTransactionDTO> paymentTransactionDTOList = paymentTransactionDAO
				.findByPayStatusAndApplicationFormRefNum(PayStatusEnum.SUCCESS.getDescription(),
						regServiceDTO.getApplicationNo());
		if (paymentTransactionDTOList.isEmpty()) {
			logger.error("Payment not found [{}] ", regServiceDTO.getApplicationNo());
			throw new BadRequestException("Payment not found [{}] " + regServiceDTO.getApplicationNo());
		}
	}

	private void taxPaidOnlineVcrWithTR(RegServiceDTO regServiceDTO) {
		List<TaxDetailsDTO> taxList = taxDetailsDAO.findFirst10ByChassisNoOrderByCreatedDateDesc(
				regServiceDTO.getRegistrationDetails().getVahanDetails().getChassisNumber());
		if (taxList != null && !taxList.isEmpty()) {
			List<TaxDetailsDTO> vcrTax = taxList.stream()
					.filter(tax -> tax.getTaxPaidThroughVcr() != null && tax.getTaxPaidThroughVcr())
					.collect(Collectors.toList());
			if (vcrTax != null && !vcrTax.isEmpty()) {
				TaxDetailsDTO taxDto = vcrTax.stream().findFirst().get();
				taxDto.setApplicationNo(regServiceDTO.getApplicationNo());
				taxDto.setPrNo(regServiceDTO.getRegistrationDetails().getPrNo());
				taxDto.setTrNo(regServiceDTO.getRegistrationDetails().getTrNo());
				taxDetailsDAO.save(taxDto);
			}
		}
	}

	private void otherStateAlterationDetails(RegServiceDTO regServiceDTO) {
		AlterationDTO alterationData = new AlterationDTO();
		alterationData.setApplicationNo(regServiceDTO.getApplicationNo());
		if (regServiceDTO.getRegistrationDetails().getVahanDetails() != null) {
			alterationData.setHeight(regServiceDTO.getRegistrationDetails().getVahanDetails().getHeight());
			alterationData.setWidth(regServiceDTO.getRegistrationDetails().getVahanDetails().getWidth());
			alterationData.setLength(regServiceDTO.getRegistrationDetails().getVahanDetails().getLength());
			alterationData.setColor(regServiceDTO.getRegistrationDetails().getVahanDetails().getColor());
			alterationData.setBodyType(regServiceDTO.getRegistrationDetails().getVahanDetails().getBodyTypeDesc());
			alterationData.setDateOfCompletion(LocalDate.now());
			alterationData.setTrNo(regServiceDTO.getRegistrationDetails().getTrNo());
			alterationData.setFromCov(regServiceDTO.getRegistrationDetails().getVahanDetails().getVehicleClass());
			alterationData.setChasisNo(regServiceDTO.getRegistrationDetails().getVahanDetails().getChassisNumber());
			alterationData.setTrailers(
					regServiceDTO.getRegistrationDetails().getVahanDetails().getTrailerChassisDetailsDTO());
			alterationData.setCov(regServiceDTO.getRegistrationDetails().getVahanDetails().getVehicleClass());
			alterationData.setVehicleTypeFrom(regServiceDTO.getRegistrationDetails().getVehicleType());
			alterationData.setVehicleTypeTo(regServiceDTO.getRegistrationDetails().getVehicleType());
			alterationData.setCovDescription(regServiceDTO.getRegistrationDetails().getClassOfVehicleDesc());
			alterationData.setBodyType(regServiceDTO.getRegistrationDetails().getVahanDetails().getBodyTypeDesc());
			alterationData.setFrombodyType(regServiceDTO.getRegistrationDetails().getVahanDetails().getBodyTypeDesc());
			alterationData.setUlw(regServiceDTO.getRegistrationDetails().getVahanDetails().getUnladenWeight());
			alterationData.setSeating(regServiceDTO.getRegistrationDetails().getVahanDetails().getSeatingCapacity());
			alterationData.setFromSeatingCapacity(
					regServiceDTO.getRegistrationDetails().getVahanDetails().getSeatingCapacity());
			alterationData.setServiceType(ServiceEnum.DATAENTRY);
			alterationData.setGvw(regServiceDTO.getRegistrationDetails().getVahanDetails().getGvw());
			alterationData.setVahanDetails(regServiceDTO.getRegistrationDetails().getVahanDetails());
			alterationDAO.save(alterationData);
		}

	}

	private void commonValidityUpdations(RegServiceDTO regServiceDTO, RegistrationValidityDTO registrationValidityDTO) {

		if (null != regServiceDTO.getTaxvalidity()) {
			registrationValidityDTO.setTaxValidity(regServiceDTO.getTaxvalidity());
		}
		if (null != regServiceDTO.getRegistrationDetails().getVehicleType()
				&& !regServiceDTO.getRegistrationDetails().isRegVehicleWithPR()) {
			setVehicleRegistrationValidity(regServiceDTO, registrationValidityDTO);
		}
	}

	private void registeredVehicleUpdations(RegServiceDTO regServiceDTO,
			RegistrationValidityDTO registrationValidityDTO) {
		if (null != regServiceDTO.getRegistrationDetails().getPrNo()) {
			regServiceDTO.setPrNo(regServiceDTO.getRegistrationDetails().getPrNo());
		}
		if (null != regServiceDTO.getRegistrationDetails().getPrIssueDate()) {
			regServiceDTO.getRegistrationDetails()
					.setPrGeneratedDate(regServiceDTO.getRegistrationDetails().getPrIssueDate().atStartOfDay());
			registrationValidityDTO.setPrGeneratedDate(regServiceDTO.getRegistrationDetails().getPrIssueDate());
		}
	}

	private void setVehicleRegistrationValidity(RegServiceDTO regServiceDTO,
			RegistrationValidityDTO registrationValidityDTO) {
		// Registration validity for Non Transport Type
		if (regServiceDTO.getRegistrationDetails().getVehicleType().equals(CovCategory.N.getCode())) {
			registrationValidityDTO.setRegistrationValidity(regServiceDTO.getRegistrationDetails().getPrGeneratedDate()
					.minusDays(1).plusYears(ValidityEnum.PR_NON_TRANSPORT_VALIDITY.getValidity()));
		}
		// Registration validity for Transport Type
		else if (regServiceDTO.getRegistrationDetails().getVehicleType().equals(CovCategory.T.getCode())) {
			registrationValidityDTO.setRegistrationValidity(regServiceDTO.getRegistrationDetails().getPrGeneratedDate()
					.minusDays(1).plusYears(ValidityEnum.PR_TRANSPORT_VALIDITY.getValidity()));
			registrationValidityDTO
					.setFcValidity(LocalDate.now().minusDays(1).plusYears(ValidityEnum.FCVALIDITY.getValidity()));
		}
	}

	/**
	 * @param regServiceDTO
	 */
	private void updateNonRegVehicleDetailsForDataEntry(RegServiceDTO regServiceDTO) {

		trGenerationForDataEntry(regServiceDTO);

		prGenerationForDataEntry(regServiceDTO);

	}

	/**
	 * @param regServiceDTO
	 */
	private void prGenerationForDataEntry(RegServiceDTO regServiceDTO) {
		try {
			String prNo = prService.geneatePrNo(regServiceDTO.getApplicationNo(), Integer.MIN_VALUE, Boolean.FALSE,
					StringUtils.EMPTY, ModuleEnum.CITIZEN, Optional.empty());
			regServiceDTO.setPrNo(prNo);
			regServiceDTO.getRegistrationDetails().setPrNo(prNo);
			regServiceDTO.getRegistrationDetails().setPrGeneratedDate(LocalDateTime.now());
			regServiceDTO.getRegistrationDetails().getRegistrationValidity().setPrGeneratedDate(LocalDate.now());
			Optional<TaxDetailsDTO> taxdetails = taxDetailsDAO
					.findByApplicationNoOrderByCreatedDateDesc(regServiceDTO.getApplicationNo());
			if (taxdetails.isPresent()) {
				TaxDetailsDTO taxdetail = taxdetails.get();
				taxdetail.setPrNo(prNo);
				taxDetailsDAO.save(taxdetail);
			}

		} catch (Exception e) {
			logger.error("Failed to generate PR number for application No:[{}]", regServiceDTO.getApplicationNo());
			throw new BadRequestException(
					"Failed to generate PR number for application No [" + regServiceDTO.getApplicationNo() + "]");
		}
	}

	/**
	 * @param regServiceDTO
	 */
	private void trGenerationForDataEntry(RegServiceDTO regServiceDTO) {
		try {
			String trNo = trSeriesService.geneateTrSeries(regServiceDTO.getRegistrationDetails().getApplicantDetails()
					.getPresentAddress().getDistrict().getDistrictId());
			regServiceDTO.setTrNo(trNo);
			regServiceDTO.getRegistrationDetails().setTrNo(trNo);
			regServiceDTO.getRegistrationDetails().setTrGeneratedDate(LocalDateTime.now());
			regServiceDTO.getRegistrationDetails().getRegistrationValidity().setTrGeneratedDate(LocalDate.now());

		} catch (Exception e) {
			logger.error("Failed to generate TR number for application No:[{}]", regServiceDTO.getApplicationNo());
			throw new BadRequestException("Failed to generate TR number for application No [" + e.getMessage() + "]");
		}
	}

	/**
	 * @param regServiceDTO
	 */
	private void updateRegVehicleDetaisForDataEntry(RegistrationDetailsDTO registrationDetailsDTO,
			RegServiceDTO regServiceDTO) {
		// update fc details collection
		if (null != regServiceDTO.getFcDetails()
				&& regServiceDTO.getRegistrationDetails().getVehicleType().equals(CovCategory.T.getCode())) {
			FcDetailsDTO fcDetails = updateFCDetailsforDataEntry(registrationDetailsDTO, regServiceDTO);
			fcDetailsDAO.save(fcDetails);
		} else if (null == regServiceDTO.getFcDetails()
				&& regServiceDTO.getRegistrationDetails().getVehicleType().equals(CovCategory.T.getCode())) {
			FcDetailsDTO fcDetails = updateFCDetailsforDataEntry(registrationDetailsDTO, regServiceDTO);
			fcDetailsDAO.save(fcDetails);
		}

		// update tax details collection(tax & green tax)
		Optional<PropertiesDTO> propertiesDto = propertiesDAO.findByStatusTrue();
		if (!propertiesDto.isPresent()) {
			throw new BadRequestException("Properties details not found, Please contact to RTA admin");
		}
		PropertiesDTO propertiesDTO = propertiesDto.get();
		if ((StringUtils.isNoneBlank(regServiceDTO.getRegistrationDetails().getPrNo())
				&& regServiceDTO.getnOCDetails() != null
				&& regServiceDTO.getnOCDetails().getState().equals(propertiesDTO.getStateName())
				&& regServiceDTO.getRegistrationDetails().getPrIssueDate()
						.isBefore(DateConverters.convertStirngTOlocalDate(propertiesDTO.getRegDate())))) {
			this.updateTaxDetailsForDataEntryTSvehicle(regServiceDTO);

		}
		// update noc details collection
		if (null != regServiceDTO.getnOCDetails()) {
			NOCDetailsDTO nOCDetailsDTO = updateNocDetailsForDataEntry(regServiceDTO);
			nocDetailsDAO.save(nOCDetailsDTO);
		}
	}

	private RegistrationDetailsDTO updatesChangeofAddressData(RegServiceDTO regServiceDTO,
			RegistrationDetailsDTO registrationDetails) {
		regServiceDTO.setApprovedDate(LocalDateTime.now());
		registrationDetails.getApplicantDetails().setPresentAddress(regServiceDTO.getPresentAdderss());
		registrationDetails.setOfficeDetails(regServiceDTO.getOfficeDetails());
		registrationDetails.setAllowForReassignment(Boolean.TRUE);
		applicantDetailsDAO.save(registrationDetails.getApplicantDetails());
		if (null != regServiceDTO.getCitizenCOAPermitStatus()) {
			List<PermitDetailsDTO> permitDetailsDTOList = permitDetails(registrationDetails.getPrNo());
			if (!permitDetailsDTOList.isEmpty()
					&& regServiceDTO.getCitizenCOAPermitStatus().equals(TransferType.permitTranfer.PERMITSURRENDER)) {
				for (PermitDetailsDTO permitDetails : permitDetailsDTOList) {
					if (!permitDetails.getPermitClass().getDescription()
							.equals(PermitsEnum.PermitType.COUNTER_SIGNATURE.getDescription())) {
						permitDetails.setPermitStatus(PermitsEnum.INACTIVE.getDescription());
						permitDetails.setPermitSurrender(true);
						permitDetails.setIsPermitSurrenderWithCOA(true);
						permitDetails.setPermitSurrenderDate(LocalDate.now());
					}
				}
				permitDetailsDAO.save(permitDetailsDTOList);
				logger.info("Permit Surrendered by citizen for PRNO {}", registrationDetails.getPrNo());
			} else if (!permitDetailsDTOList.isEmpty()
					&& regServiceDTO.getCitizenCOAPermitStatus().equals(TransferType.permitTranfer.PERMITCOA)) {
				for (PermitDetailsDTO permitDetails : permitDetailsDTOList) {
					if (!permitDetails.getPermitClass().getDescription()
							.equals(PermitsEnum.PermitType.COUNTER_SIGNATURE.getDescription())) {
						if (permitDetails.getRdto() != null && permitDetails.getRdto().getApplicantDetails() != null
								&& permitDetails.getRdto().getApplicantDetails().getPresentAddress() != null) {
							permitDetails.getRdto().getApplicantDetails()
									.setPresentAddress(regServiceDTO.getPresentAdderss());
							permitDetails.getRdto().setOfficeDetails(regServiceDTO.getOfficeDetails());
						}
					}
				}
				permitDetailsDAO.save(permitDetailsDTOList);
				logger.info("Permit COA by citizen for PRNO {}", registrationDetails.getPrNo());
			}

			else if (!permitDetailsDTOList.isEmpty() && regServiceDTO.getCitizenCOARecommendationLetterStatus() != null
					&& regServiceDTO.getCitizenCOARecommendationLetterStatus()
							.equals(TransferType.permitTranfer.RECOMMENDATIONLETTERTRANSFER)) {
				for (PermitDetailsDTO permitDetails : permitDetailsDTOList) {
					if (permitDetails.getPermitClass().getDescription()
							.equals(PermitsEnum.PermitType.COUNTER_SIGNATURE.getDescription())) {
						if (permitDetails.getRdto() != null && permitDetails.getRdto().getApplicantDetails() != null
								&& permitDetails.getRdto().getApplicantDetails().getPresentAddress() != null) {
							permitDetails.getRdto().getApplicantDetails()
									.setPresentAddress(regServiceDTO.getPresentAdderss());
							permitDetails.getRdto().setOfficeDetails(regServiceDTO.getOfficeDetails());
						}
					}
				}
				permitDetailsDAO.save(permitDetailsDTOList);
				logger.info("Permit COA by citizen for PRNO {}", registrationDetails.getPrNo());
			}

			else if (!permitDetailsDTOList.isEmpty() && regServiceDTO.getCitizenCOARecommendationLetterStatus() != null
					&& regServiceDTO.getCitizenCOARecommendationLetterStatus()
							.equals(TransferType.permitTranfer.RECOMMENDATIONLETTERSURRENDER)) {
				for (PermitDetailsDTO permitDetails : permitDetailsDTOList) {
					if (permitDetails.getPermitClass().getDescription()
							.equals(PermitsEnum.PermitType.COUNTER_SIGNATURE.getDescription())) {
						permitDetails.setPermitStatus(PermitsEnum.INACTIVE.getDescription());
						permitDetails.setPermitSurrender(true);
						permitDetails.setIsPermitSurrenderWithCOA(true);
						permitDetails.setPermitSurrenderDate(LocalDate.now());
					}
				}
				permitDetailsDAO.save(permitDetailsDTOList);
				logger.info("Permit COA by citizen for PRNO {}", registrationDetails.getPrNo());
			}
		}
		return registrationDetails;
	}

	private RegistrationDetailsDTO updatesRenewalData(RegServiceDTO regServiceDTO,
			RegistrationDetailsDTO registrationDetails) {
		regServiceDTO.setApprovedDate(LocalDateTime.now());
		RegistrationValidityDTO registrationValidity = registrationDetails.getRegistrationValidity();
		registrationValidity
				.setRegistrationValidity(regServiceDTO.getApprovedDate().minusDays(1).plusYears(EXTENDYEAR));
		if (null != regServiceDTO.getTaxvalidity()) {
			registrationValidity.setTaxValidity(regServiceDTO.getTaxvalidity());
		}
		registrationDetails.setRegistrationValidity(registrationValidity);
		return registrationDetails;
	}

	private boolean isToUpdateStatusAsTaxPending(RegServiceDTO regDto) {

		if (regDto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.ALTERATIONOFVEHICLE.getId()))) {

			if (regDto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.REASSIGNMENT.getId()))) {
				return Boolean.TRUE;// true;
			}
			if (isVehicleHaveExcemtion(regDto)) {
				return Boolean.FALSE;
			}
			Boolean gostatus = Boolean.FALSE;
			String cov = regDto.getAlterationDetails().getCov() != null ? regDto.getAlterationDetails().getCov()
					: regDto.getRegistrationDetails().getClassOfVehicle();
			Optional<MasterPayperiodDTO> Payperiod = masterPayperiodDAO.findByCovcode(cov);
			if (!Payperiod.isPresent()) {
				throw new BadRequestException("No records found for payPerid for :" + cov);
			}
			String seats = regDto.getAlterationDetails().getSeating() != null
					? regDto.getAlterationDetails().getSeating()
					: regDto.getRegistrationDetails().getVahanDetails().getSeatingCapacity();
			if (Payperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.BOTH.getCode())) {
				// need to change gvw
				Pair<Optional<MasterPayperiodDTO>, Boolean> payperiodAndGoStatus = citizenTaxService
						.getPayPeroidForBoth(Payperiod, seats,
								regDto.getRegistrationDetails().getVahanDetails().getGvw());
				Payperiod = payperiodAndGoStatus.getFirst();
				gostatus = payperiodAndGoStatus.getSecond();
			}
			if (TaxTypeEnum.LifeTax.getCode().equals(Payperiod.get().getPayperiod())) {
				if (regDto.getAlterationDetails().getFromCov() != null && regDto.getAlterationDetails().getCov() != null
						&& regDto.getAlterationDetails().getFromCov()
								.equalsIgnoreCase(ClassOfVehicleEnum.IVCN.getCovCode())) {
					List<TaxDetailsDTO> listTax = taxDetailsDAO
							.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(
									regDto.getRegistrationDetails().getApplicationNo(),
									Arrays.asList(TaxTypeEnum.LifeTax.getDesc()));
					boolean flag = Boolean.FALSE;
					if (listTax.isEmpty()) {
						if (!gostatus) {
							return Boolean.TRUE;
						}

					} else {

						for (TaxDetailsDTO dto : listTax) {
							if (getListofTwoWeelerNotTransCovs().stream()
									.anyMatch(covs -> covs.equalsIgnoreCase(dto.getClassOfVehicle()))) {
								flag = Boolean.TRUE;
							}
						}
						listTax.clear();
					}
					if (!flag) {
						return Boolean.TRUE;// true;
					}
				}
				List<TaxDetailsDTO> listTax = taxDetailsDAO
						.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(
								regDto.getRegistrationDetails().getApplicationNo(),
								Arrays.asList(TaxTypeEnum.LifeTax.getDesc()));
				if (listTax.isEmpty()) {
					if (!gostatus) {
						return Boolean.TRUE;
					}

				}
				if (listTax != null && !listTax.isEmpty()) {
					for (TaxDetailsDTO dto : listTax) {
						for (Map<String, TaxComponentDTO> map : dto.getTaxDetails()) {
							for (Entry<String, TaxComponentDTO> entry : map.entrySet()) {
								if (TaxTypeEnum.LifeTax.getDesc().equalsIgnoreCase(entry.getKey())) {
									listTax.clear();
									return Boolean.FALSE;
								}
							}
						}
					}
					listTax.clear();
				}
			}
			/*
			 * if (regDto.getAlterationDetails().getCov() != null) {
			 * Optional<MasterPayperiodDTO> oldCovPayperiod = masterPayperiodDAO
			 * .findByCovcode(regDto.getRegistrationDetails().getClassOfVehicle()); if
			 * (!oldCovPayperiod.isPresent()) { throw new
			 * BadRequestException("No records found for payPerid for :" + cov); } if
			 * (!Payperiod.get().getPayperiod().equals(oldCovPayperiod.get().getPayperiod())
			 * ) { if
			 * (oldCovPayperiod.get().getPayperiod().equalsIgnoreCase(TaxTypeEnum.BOTH.
			 * getCode())) { // need to change gvw Pair<Optional<MasterPayperiodDTO>,
			 * Boolean> payperiodAndGoStatus = citizenTaxService
			 * .getPayPeroidForBoth(oldCovPayperiod, seats,
			 * regDto.getRegistrationDetails().getVahanDetails().getGvw()); Payperiod =
			 * payperiodAndGoStatus.getFirst(); gostatus = payperiodAndGoStatus.getSecond();
			 * } if(!gostatus) { return Boolean.TRUE; } } }
			 */
			Pair<String, String> permitCodeAndRouet = this.getPermitCode(regDto.getRegistrationDetails());
			String permitCode = permitCodeAndRouet.getFirst();
			String routeCode = permitCodeAndRouet.getSecond();

			if (regDto.getRegistrationDetails().getClassOfVehicle()
					.equalsIgnoreCase(ClassOfVehicleEnum.GCRT.getCovCode())
					&& regDto.getRegistrationDetails().getVehicleDetails().getRlw() <= 3000
					&& StringUtils.isNoneBlank(regDto.getAlterationDetails().getVehicleTypeTo())
					&& regDto.getAlterationDetails().getVehicleTypeTo().equalsIgnoreCase(CovCategory.T.getCode())) {
				return Boolean.TRUE;// true;
			}
			Double oldTax = citizenTaxService.getOldCovTax(regDto.getRegistrationDetails().getClassOfVehicle(),
					regDto.getRegistrationDetails().getVehicleDetails().getSeatingCapacity(),
					regDto.getRegistrationDetails().getVehicleDetails().getUlw(),
					regDto.getRegistrationDetails().getVehicleDetails().getRlw(),
					regDto.getRegistrationDetails().getApplicantDetails().getPresentAddress().getState().getStateId(),
					permitCode, routeCode);

			Integer ulw = regDto.getAlterationDetails().getUlw() != null ? regDto.getAlterationDetails().getUlw()
					: regDto.getRegistrationDetails().getVehicleDetails().getUlw();
			String seating = regDto.getAlterationDetails().getSeating() != null
					? regDto.getAlterationDetails().getSeating()
					: regDto.getRegistrationDetails().getVehicleDetails().getSeatingCapacity();
			if (!cov.equalsIgnoreCase(regDto.getRegistrationDetails().getClassOfVehicle())) {
				permitCode = "INA";
				routeCode = StringUtils.EMPTY;
			}
			Double newTax = citizenTaxService.getOldCovTax(cov, seating, ulw,
					regDto.getRegistrationDetails().getVehicleDetails().getRlw(),
					regDto.getRegistrationDetails().getApplicantDetails().getPresentAddress().getState().getStateId(),
					permitCode, routeCode);
			if (newTax > oldTax) {
				return Boolean.TRUE;// true;
			}

		}

		/*
		 * else if (regDto.getServiceIds().stream().anyMatch(id ->
		 * id.equals(ServiceEnum.DATAENTRY.getId()))) {
		 * if(regDto.getRegistrationDetails().getClassOfVehicle().equalsIgnoreCase(
		 * ClassOfVehicleEnum.CHSN.getCovCode()) ||
		 * regDto.getRegistrationDetails().getClassOfVehicle().equalsIgnoreCase(
		 * ClassOfVehicleEnum.CHST.getCovCode())) { return Boolean.TRUE;// true; } }
		 */
		return Boolean.FALSE;
	}

	private boolean isVehicleHaveExcemtion(RegServiceDTO regDto) {

		Optional<MasterTaxExcemptionsDTO> optionalTaxExcemption = masterTaxExcemptionsDAO.findByKeyvalueOrCovcode(
				regDto.getRegistrationDetails().getVahanDetails().getMakersModel(),
				regDto.getRegistrationDetails().getClassOfVehicle());
		if (optionalTaxExcemption.isPresent()) {
			return Boolean.TRUE;
		}
		String cov = regDto.getAlterationDetails().getCov() != null ? regDto.getAlterationDetails().getCov()
				: regDto.getRegistrationDetails().getClassOfVehicle();
		if (regDto.getRegistrationDetails().getOwnerType().getCode()
				.equalsIgnoreCase(OwnerTypeEnum.Government.getCode())
				|| regDto.getRegistrationDetails().getOwnerType().getCode()
						.equalsIgnoreCase(OwnerTypeEnum.POLICE.getCode())
				|| (regDto.getRegistrationDetails().getOwnerType().getCode().equalsIgnoreCase(
						OwnerTypeEnum.Stu.getCode()) && cov.equalsIgnoreCase(ClassOfVehicleEnum.OBT.getCovCode()))) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	public List<String> getListofTwoWeelerNotTransCovs() {
		List<String> list = new ArrayList<>();
		list.add(ClassOfVehicleEnum.MCYN.getCovCode());
		list.add(ClassOfVehicleEnum.MMCN.getCovCode());
		list.add(ClassOfVehicleEnum.MCRN.getCovCode());
		return list;
	}

	/**
	 * @param regServiceDTO
	 * @return
	 */
	private NOCDetailsDTO updateNocDetailsForDataEntry(RegServiceDTO regServiceDTO) {
		NOCDetailsDTO nOCDetailsDTO = regServiceDTO.getnOCDetails();
		nOCDetailsDTO.setApplicationNo(regServiceDTO.getApplicationNo());
		return nOCDetailsDTO;
	}

	/**
	 * @param staginDto
	 * @return
	 */
	private TaxDetailsDTO updateTaxDetailsForDataEntry(RegServiceDTO staginDto) {
		String taxTypeVcr = null;
		LocalDate taxUpTo = LocalDate.now();
		if (staginDto.getRegistrationDetails().getVahanDetails() != null
				&& staginDto.getRegistrationDetails().getVahanDetails().getVehicleClass() != null
				&& staginDto.getRegistrationDetails().getVahanDetails().getSeatingCapacity() != null
				&& staginDto.getRegistrationDetails().getVahanDetails().getGvw() != null) {
			Optional<List<TaxTypeVO>> taxTypeVOList = infoService.taxDataEntryType(
					staginDto.getRegistrationDetails().getVahanDetails().getVehicleClass(),
					staginDto.getRegistrationDetails().getVahanDetails().getSeatingCapacity(),
					staginDto.getRegistrationDetails().getVahanDetails().getGvw().toString());
			if (taxTypeVOList.isPresent()) {
				for (TaxTypeVO taxType : taxTypeVOList.get()) {
					if (taxType.getTaxId() != null && taxType.getTaxId().equals(TaxTypeEnum.LifeTax.getCode())) {
						taxTypeVcr = TaxTypeEnum.LifeTax.getDesc(); // MTLT GCRT---
						if (staginDto.getRegistrationDetails().getPrGeneratedDate() != null) {
							taxUpTo = staginDto.getRegistrationDetails().getPrGeneratedDate().toLocalDate().minusDays(1)
									.plusYears(12);
						} else {
							taxUpTo = LocalDate.now().minusDays(1).plusYears(12);
						}
					} else {
						if (taxType.getTaxId() != null
								&& taxType.getTaxId().equals(TaxTypeEnum.QuarterlyTax.getCode())) {
							taxTypeVcr = TaxTypeEnum.QuarterlyTax.getDesc();
						}
						if (taxType.getTaxId() != null
								&& taxType.getTaxId().equals(TaxTypeEnum.HalfyearlyTax.getCode())) {
							taxTypeVcr = TaxTypeEnum.HalfyearlyTax.getDesc();
						}
						if (taxType.getTaxId() != null && taxType.getTaxId().equals(TaxTypeEnum.YearlyTax.getCode())) {
							taxTypeVcr = TaxTypeEnum.YearlyTax.getDesc();
						}
						taxUpTo = citizenTaxService.validity(taxTypeVcr);
					}
				}
			}
		}
		TaxDetailsDTO taxDetails = new TaxDetailsDTO();
		taxDetails.setApplicationNo(staginDto.getApplicationNo());
		taxDetails.setTrNo(staginDto.getTrNo());
		taxDetails.setPrNo(staginDto.getPrNo());
		taxDetails.setClassOfVehicle(staginDto.getRegistrationDetails().getClassOfVehicle());
		// Murthy sir input, other state with prNo and tax paid by vcr every application
		// taken as Quarterly Tax
		org.epragati.regservice.dto.TaxDetailsDTO taxDetailsDTO = staginDto.getTaxDetails();
		taxDetails.setTaxAmount(taxDetailsDTO.getCollectedAmount());
		taxDetails.setTaxPeriodFrom(taxDetailsDTO.getPaymentDAte());
		taxDetails.setTaxPeriodEnd(taxUpTo);
		taxDetails.setPaymentPeriod(taxTypeVcr);
		// Map for tax details
		List<Map<String, TaxComponentDTO>> taxDetailsList = new ArrayList<>();

		Map<String, TaxComponentDTO> taxMap = new HashMap<>();

		TaxComponentDTO tax = new TaxComponentDTO();
		tax.setTaxName(taxTypeVcr);
		if (taxDetailsDTO.getCollectedAmount() != null)
			tax.setAmount(Double.valueOf(taxDetailsDTO.getCollectedAmount().toString()));
		if (taxDetailsDTO.getPaymentDAte() == null) {
			taxDetailsDTO.setPaymentDAte(LocalDate.now());
		}
		tax.setPaidDate(DateConverters.convertLocalDateToLocalDateTime(taxDetailsDTO.getPaymentDAte()));
		tax.setValidityFrom(taxDetailsDTO.getPaymentDAte());
		tax.setValidityTo(taxUpTo);
		taxMap.put(taxTypeVcr, tax);
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
	private FcDetailsDTO updateFCDetailsforDataEntry(RegistrationDetailsDTO staginDto, RegServiceDTO regServiceDTO) {
		// TODO : Need to generate Sequence Number
		Map<String, String> detail = new HashMap<String, String>();
		detail.put("officeCode", staginDto.getOfficeDetails().getOfficeCode());
		String fcNumber = sequenceGenerator.getSequence(String.valueOf(Sequence.FC.getSequenceId()), detail);
		FcDetailsDTO fcdetails = new FcDetailsDTO();
		fcdetails.setApplicationNo(staginDto.getApplicationNo());
		fcdetails.setOfficeCode(staginDto.getOfficeDetails().getOfficeCode());
		fcdetails.setOfficeName(staginDto.getOfficeDetails().getOfficeName());
		fcdetails.setVehicleNumber("");
		ActionDetails actionDetails = this.getActionDetailByRole(regServiceDTO, RoleEnum.MVI.getName());
		MasterUsersDTO userDto = masterUsersDAO.findByUserId(actionDetails.getUserId());
		if (userDto != null) {
			fcdetails.setInspectedMviName(userDto.getFirstName());
		}
		Optional<OfficeDTO> optionalOffice = officeDAO.findByOfficeCode(userDto.getOffice().getOfficeCode());

		if (!optionalOffice.isPresent()) {
			throw new BadRequestException("Office details not found for: " + userDto.getOffice().getOfficeCode());
		}
		fcdetails.setInspectedMviOfficeName(optionalOffice.get().getOfficeName());
		fcdetails.setInspectedDate(actionDetails.getlUpdate());
		fcdetails.setUserId(actionDetails.getUserId());
		fcdetails.setChassisNo(staginDto.getVahanDetails().getChassisNumber());
		fcdetails.setEngineNo(staginDto.getVahanDetails().getEngineNumber());
		fcdetails.setClassOfVehicle(staginDto.getClassOfVehicle());
		if (regServiceDTO.getFcDetails() != null && regServiceDTO.getFcDetails().getFcValidUpto() != null) {
			fcdetails.setFcValidUpto(regServiceDTO.getFcDetails().getFcValidUpto());
			if (regServiceDTO.getFcDetails() != null && regServiceDTO.getFcDetails().getFcValidUpto() != null)
				staginDto.getRegistrationValidity().setFcValidity(regServiceDTO.getFcDetails().getFcValidUpto());
			fcdetails.setFcIssuedDate(regServiceDTO.getFcDetails().getFcIssuedDate());
		} else if (regServiceDTO.getFcDetails() == null) {
			// Fc dateils is not enter in Citizen portal.
			if (regServiceDTO.getnOCDetails() != null && regServiceDTO.getnOCDetails().getDateOfEntry() != null) {
				LocalDate entryDate = getEarlerDate(regServiceDTO.getnOCDetails().getDateOfEntry(),
						regServiceDTO.getnOCDetails().getIssueDate());
				fcdetails.setFcValidUpto(entryDate);
				staginDto.getRegistrationValidity().setFcValidity(entryDate);
				fcdetails.setFcIssuedDate(LocalDateTime.now());
			}
		}
		fcdetails.setCreatedDate(LocalDateTime.now());
		fcdetails.setFcNumber(fcNumber);
		fcdetails.setTrNo(staginDto.getTrNo());
		if (regServiceDTO.getAlterationDetails() != null
				&& StringUtils.isNoneBlank(regServiceDTO.getAlterationDetails().getPrNo())) {
			fcdetails.setPrNo(regServiceDTO.getAlterationDetails().getPrNo());
		} else {
			fcdetails.setPrNo(staginDto.getPrNo());
		}
		List<FcDetailsDTO> fcDetailsDtoList = fcDetailsDAO
				.findFirst5ByStatusIsTrueAndPrNoOrderByCreatedDateDesc(staginDto.getPrNo());
		if (!fcDetailsDtoList.isEmpty()) {
			fcDetailsDtoList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
			for (FcDetailsDTO dto : fcDetailsDtoList) {
				dto.setStatus(Boolean.FALSE);
				fcDetailsDAO.save(dto);
			}
		}
		return fcdetails;

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

		flowDto.setStatus(true);

		flowDto.setFlowDetails(details);

		return flowDto;
	}

	private void sendNotifications(Integer templateId, RegServiceDTO entity) {

		try {
			if (entity != null) {
				if (entity.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId()))) {
					if (templateId == MessageTemplate.RCFORFINANCEFINAPP.getId()) {
						notifications.sendNotifications(MessageTemplate.RCFORFINANCEFINAPP.getId(), entity);
					} else if (templateId == MessageTemplate.RCFORFINANCEFORM37FIN.getId()) {
						notifications.sendNotifications(MessageTemplate.RCFORFINANCEFORM37FIN.getId(), entity);
					} else if (templateId == MessageTemplate.FINANCIERNOTMATCH.getId()) {
						notifications.sendNotifications(MessageTemplate.FINANCIERNOTMATCH.getId(), entity);
					} else {
						notifications.sendNotifications(MessageTemplate.RCFORFINANCEREG.getId(), entity);
					}

				} else {
					notifications.sendEmailNotification(notificationTemplate::fillTemplate, templateId, entity,
							entity.getRegistrationDetails().getApplicantDetails().getContact().getEmail());

					notifications.sendMessageNotification(notificationTemplate::fillTemplate, templateId, entity,
							entity.getRegistrationDetails().getApplicantDetails().getContact().getMobile());
				}
			}

		} catch (Exception e) {
			logger.error("Failed to send notifications for template id: {}; {}", templateId, e);
		}

	}

	@Override
	public Optional<InputVO> getListOfSupportedEnclosers(CitizenImagesInput input) {
		List<EnclosuresDTO> enclosureList;

		Integer serviceId = getEncServiceId(input);
		if (0 == serviceId.intValue()) {
			return Optional.empty();
		}
		enclosureList = userEnclosureDAO.findByServiceIDAndApplicantType(serviceId, ApplicantTypeEnum.MVI);

		if (input.getServiceIds().contains(ServiceEnum.RCFORFINANCE.getId())) {
			if (input.getApplicantType().equals(ApplicantTypeEnum.MVI)) {
				enclosureList = userEnclosureDAO.findByServiceIDAndApplicantType(serviceId, ApplicantTypeEnum.MVI);
			} 
			else if (input.getApplicantType().equals(ApplicantTypeEnum.AO)) {
				enclosureList = userEnclosureDAO.findByServiceIDAndApplicantType(serviceId, ApplicantTypeEnum.AO);
			}
			else {
				enclosureList = userEnclosureDAO.findByServiceIDAndApplicantType(serviceId,
						ApplicantTypeEnum.FINANCIER);
			}
		}

		enclosureList.sort((p1, p2) -> p1.getEnclosureId().compareTo(p2.getEnclosureId()));

		if (enclosureList.isEmpty()) {
			logger.error("No enclosures found based on given Service Id");
			throw new BadRequestException("No enclosures found based on given Service Id");
		}
		if (serviceId.equals(ServiceEnum.NEWFC.getId())) {

			for (EnclosuresDTO dto : enclosureList) {
				if (dto.getProof().equalsIgnoreCase(EnclosureType.BodyBuilderSpeedDevice.toString())) {
					if (dto.getEnclosureId() == 4) {
						dto.setEnclosureId(2);
					}
				}
			}
		}
		InputVO inputVO = userEnclosureMapper.convertEnclosureDTOtoInputVO(enclosureList);
		return Optional.of(inputVO);
	}

	private Integer getEncServiceId(CitizenImagesInput input) {

		if (input.getServiceIds().stream()
				.anyMatch(service -> service.equals(ServiceEnum.ALTERATIONOFVEHICLE.getId()))) {
			return ServiceEnum.ALTERATIONOFVEHICLE.getId();
		}
		if (input.getServiceIds().stream().anyMatch(service -> service.equals(ServiceEnum.RENEWAL.getId()))) {
			return ServiceEnum.RENEWAL.getId();
		}
		if (input.getServiceIds().stream().anyMatch(service -> service.equals(ServiceEnum.DATAENTRY.getId()))) {
			return ServiceEnum.DATAENTRY.getId();
		}
		if (input.getServiceIds().stream().anyMatch(service -> service.equals(ServiceEnum.NEWFC.getId()))) {
			return ServiceEnum.NEWFC.getId();
		}
		if (input.getServiceIds().stream().anyMatch(service -> service.equals(ServiceEnum.RENEWALFC.getId()))) {
			return ServiceEnum.RENEWALFC.getId();
		}
		if (input.getServiceIds().stream().anyMatch(service -> service.equals(ServiceEnum.OTHERSTATIONFC.getId()))) {
			return ServiceEnum.OTHERSTATIONFC.getId();
		}
		if (input.getServiceIds().stream().anyMatch(service -> service.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))) {
			return ServiceEnum.VEHICLESTOPPAGE.getId();
		}
		if (input.getServiceIds().stream().anyMatch(service -> service.equals(ServiceEnum.RCFORFINANCE.getId()))) {
			return ServiceEnum.RCFORFINANCE.getId();
		}
		if (input.getServiceIds().stream().anyMatch(service -> service.equals(ServiceEnum.RCCANCELLATION.getId()))) {
			return ServiceEnum.RCCANCELLATION.getId();
		}
		return 0;

	}

	public void saveImages(String applicationNo, List<ImageInput> images, MultipartFile[] uploadfiles)
			throws IOException {

		if (CollectionUtils.isNotEmpty(images)) {
			for (MultipartFile file : uploadfiles) {
				long size = file.getSize();
				logger.info("size of uploaded file {} :[{}]", file.getOriginalFilename(), size);
			}

			Optional<CitizenEnclosuresDTO> citizenEnclosuresDTO = citizenEnclosuresDAO
					.findByApplicationNo(applicationNo);
			if (!citizenEnclosuresDTO.isPresent()) {
				throw new BadRequestException(
						"no record found in citizen enclosure based on applicationNo" + applicationNo);
			}
			saveEnclosures(citizenEnclosuresDTO.get(), images, uploadfiles);

		}
	}

	private void saveEnclosures(CitizenEnclosuresDTO citizenEnclosuresDTO, List<ImageInput> images,
			MultipartFile[] uploadfiles) throws IOException {

		// ApplicantDetailsDTO applicantDto =
		// registrationDetails.getApplicantDetails();
		List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = gridFsClient.convertImages(images,
				citizenEnclosuresDTO.getApplicationNo(), uploadfiles, StatusRegistration.APPROVED.getDescription());
		if (citizenEnclosuresDTO.getEnclosures() == null) {

			citizenEnclosuresDTO.setEnclosures(enclosures);
		} else {

			for (ImageInput image : images) {
				if (citizenEnclosuresDTO.getEnclosures().stream()
						.anyMatch(imagetype -> imagetype.getKey().equals(image.getType()))) {

					KeyValue<String, List<ImageEnclosureDTO>> matchedImage = citizenEnclosuresDTO.getEnclosures()
							.stream().filter(val -> val.getKey().equals(image.getType())).findFirst().get();
					citizenEnclosuresDTO.getEnclosures().remove(matchedImage);

					gridFsClient.removeImages(matchedImage.getValue());
				}
			}
			ImageActionsDTO actiondto = new ImageActionsDTO();
			actiondto.setAction(StatusRegistration.APPROVED.getDescription());
			actiondto.setRole(RoleEnum.MVI.getName());
			for (KeyValue<String, List<ImageEnclosureDTO>> listImages : enclosures) {
				for (ImageEnclosureDTO dto : listImages.getValue()) {
					dto.setImageActions(Arrays.asList(actiondto));
				}
			}
			citizenEnclosuresDTO.getEnclosures().addAll(enclosures);
		}
		frcValidationForReupload(citizenEnclosuresDTO);
		citizenEnclosuresDAO.save(citizenEnclosuresDTO);
	}

	@SuppressWarnings({ "rawtypes" })
	public void saveAltDetailsAtMviLevel(RtaActionVO actionVo, RegServiceDTO dto,
			StatusRegistration applicationStatus) {

		if (dto.getServiceType().stream().anyMatch(service -> service.equals(ServiceEnum.ALTERATIONOFVEHICLE))) {
			if (actionVo.getAlterationVO() != null) {
				if (dto.getAlterationDetailsLog() == null || dto.getAlterationDetailsLog().isEmpty()) {
					dto.setAlterationDetailsLog(Arrays.asList(dto.getAlterationDetails()));
				} else {
					dto.getAlterationDetailsLog().add(dto.getAlterationDetails());
				}

				AlterationDTO altDto = dto.getAlterationDetails();// alterationMapper.convertVO(actionVo.getAlterationVO());
				if (dto.getAlterationDetails().getVehicleTypeTo() != null) {
					if (actionVo.getAlterationVO().getVehicleTypeTo() == null) {
						throw new BadRequestException("vehicle type getting error, applicationNo:"
								+ dto.getAlterationDetails().getApplicationNo());
					}

					altDto.setVehicleTypeTo(actionVo.getAlterationVO().getVehicleTypeTo());
				}
				if (dto.getAlterationDetails().getCov() != null) {
					if (actionVo.getAlterationVO().getCov() == null) {
						throw new BadRequestException("vehicle type getting error, applicationNo:"
								+ dto.getAlterationDetails().getApplicationNo());
					}

					altDto.setCov(actionVo.getAlterationVO().getCov());
				}
				if (dto.getAlterationDetails().getBodyType() != null) {
					if (actionVo.getAlterationVO().getBodyType() == null
							|| actionVo.getAlterationVO().getMviEnterdColor() == null) {
						throw new BadRequestException("body type getting error And Color Not Present, applicationNo:"
								+ dto.getAlterationDetails().getApplicationNo());
					}
					altDto.setBodyType(actionVo.getAlterationVO().getBodyType());
					altDto.setMviEnterdColor(actionVo.getAlterationVO().getMviEnterdColor());
				}
				if (dto.getAlterationDetails().getFuel() != null) {
					if (actionVo.getAlterationVO().getFuel() == null) {
						throw new BadRequestException(
								"fuel getting error, applicationNo:" + dto.getAlterationDetails().getApplicationNo());
					}

					if (actionVo.getAlterationVO().getGasKitValidity() == null) {
						throw new BadRequestException("Please enter gas kit validity, applicationNo:"
								+ dto.getAlterationDetails().getApplicationNo());
					}

					if (StringUtils.isBlank(actionVo.getAlterationVO().getGasKitNo())) {
						throw new BadRequestException("Please enter gas kit number, applicationNo:"
								+ dto.getAlterationDetails().getApplicationNo());
					}
					if (StringUtils.isBlank(actionVo.getAlterationVO().getAgencyDetails())) {
						throw new BadRequestException("Please enter gas kit agency details, applicationNo:"
								+ dto.getAlterationDetails().getApplicationNo());
					}
					altDto.setFuel(actionVo.getAlterationVO().getFuel());
					altDto.setGasKitValidity(actionVo.getAlterationVO().getGasKitValidity());
					altDto.setGasKitNo(actionVo.getAlterationVO().getGasKitNo());
					altDto.setAgencyDetails(actionVo.getAlterationVO().getAgencyDetails());
				}
				if (dto.getAlterationDetails().getSeating() != null) {
					if (actionVo.getAlterationVO().getSeating() == null) {
						throw new BadRequestException("Seatting getting error, applicationNo:"
								+ dto.getAlterationDetails().getApplicationNo());
					}

				}
				if (actionVo.getAlterationVO().getSeating() != null)
					altDto.setSeating(actionVo.getAlterationVO().getSeating());
				if (actionVo.getAlterationVO().getUlw() != null)
					altDto.setUlw(actionVo.getAlterationVO().getUlw());
				// altDto.setId(dto.getAlterationDetails().getId());
				// altDto.setApplicationNo(dto.getAlterationDetails().getApplicationNo());
				// altDto.setSpecialNoRequired(dto.getAlterationDetailsLog().isSpecialNoRequired());
				if (applicationStatus != null && applicationStatus.equals(StatusRegistration.APPROVED)) {
					altDto.setMVIDone(Boolean.TRUE);
				}
				if (actionVo.getAlterationVO().getFcType() != null) {
					altDto.setFcType(actionVo.getAlterationVO().getFcType());
				}
				if (actionVo.getAlterationVO().getCov() != null
						&& actionVo.getAlterationVO().getCov().equalsIgnoreCase(ClassOfVehicleEnum.ARVT.getCovCode())) {
					if (actionVo.getAlterationVO().getTrailers() == null
							|| actionVo.getAlterationVO().getTrailers().isEmpty()) {
						throw new BadRequestException(
								"trailer details not found form UI:" + dto.getAlterationDetails().getApplicationNo());
					}
					altDto.setTrailers(trailerChassisDetailsMapper.convertVO(actionVo.getAlterationVO().getTrailers()));
				}
				registrationService.alterationValidations(alterationMapper.convertEntity(altDto), dto, Boolean.TRUE,
						actionVo.getStatus());
				dto.setAlterationDetails(altDto);
				alterationDAO.save(altDto);
			}
		}
		if ((dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWFC.getId()))
				|| dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RENEWALFC.getId()))
				|| dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.OTHERSTATIONFC.getId())))
				&& dto.getServiceIds().size() == 1) {
			dto.setFcQstList(fcQuestionsMapper.convertVO(actionVo.getFcQstList()));
			if (applicationStatus != null && applicationStatus.equals(StatusRegistration.APPROVED)) {
				dto.setAlterationDetails(alterationMapper.convertVO(actionVo.getAlterationVO()));
			} else if (applicationStatus != null && applicationStatus.equals(StatusRegistration.REJECTED)) {
				dto.setReInspectionDate(actionVo.getReInspectionDate());
				dto.setReasonForRejection(actionVo.getReasonForRejection());
				dto.setCfxIssued(Boolean.TRUE);
			}
		}
		if (dto.getServiceType().stream().anyMatch(service -> ServiceEnum.VEHICLESTOPPAGE.equals(service))) {
			if (actionVo.getStoppageQuations() == null || actionVo.getStoppageQuations().isEmpty()) {
				throw new BadRequestException("stoppage quation and answers not found");
			}

			Map<String, Map> mapmasterStoppageQu = new HashMap<>();

			for (MasterStoppageQuationsVO masterStoppageQu : actionVo.getStoppageQuations()) {

				Map<String, Map> mapsubOptions = new HashMap<>();
				for (StoppageQuationsSubOptionsVO subOptions : masterStoppageQu.getOptions()) {

					if (subOptions.getType().equalsIgnoreCase("radio")
							&& subOptions.getValue().equalsIgnoreCase("true")) {
						Map<String, String> mapsupOptionsSupport = new HashMap<>();
						for (StoppageQuationsSubOptionsSupportVO supOptionsSupport : subOptions.getSupports()) {
							if (supOptionsSupport.getType().equalsIgnoreCase("radio")
									&& supOptionsSupport.getValue().equalsIgnoreCase("true")) {
								mapsupOptionsSupport.put(supOptionsSupport.getLabel(), supOptionsSupport.getContent());
							} else if (supOptionsSupport.getType().equalsIgnoreCase("text")) {
								mapsupOptionsSupport.put(supOptionsSupport.getLabel(), supOptionsSupport.getContent());
							}
						}
						mapsubOptions.put(subOptions.getLabel(), mapsupOptionsSupport);
					} else if (subOptions.getType().equalsIgnoreCase("text")) {
						Map<String, String> mapsupOptionsSupport = new HashMap<>();
						mapsupOptionsSupport.put(subOptions.getLabel(), subOptions.getContent());
						mapsubOptions.put("", mapsupOptionsSupport);
					} else if (subOptions.getType().equalsIgnoreCase("label")) {
						Map<String, String> mapsupOptionsSupport = new HashMap<>();
						for (StoppageQuationsSubOptionsSupportVO supOptionsSupport : subOptions.getSupports()) {
							if (supOptionsSupport.getType().equalsIgnoreCase("radio")
									&& supOptionsSupport.getValue().equalsIgnoreCase("true")) {
								mapsupOptionsSupport.put(supOptionsSupport.getLabel(), supOptionsSupport.getContent());
							} else if (supOptionsSupport.getType().equalsIgnoreCase("text")) {
								mapsupOptionsSupport.put(supOptionsSupport.getLabel(), supOptionsSupport.getContent());
							}
						}
						mapsubOptions.put(subOptions.getLabel(), mapsupOptionsSupport);

					}
				}
				mapmasterStoppageQu.put(masterStoppageQu.getQuestion(), mapsubOptions);

			}

			dto.getVehicleStoppageDetails()
					.setStoppageQuations(masterStoppageQuationsMapper.convertVO(actionVo.getStoppageQuations()));
			dto.getVehicleStoppageDetails().setStoppageMapQuations(mapmasterStoppageQu);
		}
		if (dto.getServiceType().stream().anyMatch(service -> service.equals(ServiceEnum.VEHICLESTOPPAGEREVOKATION))) {
			if (actionVo.getStoppageRevokationQuations() == null
					|| actionVo.getStoppageRevokationQuations().isEmpty()) {
				throw new BadRequestException("stoppage revocation quation and answers not found");
			}
			dto.getVehicleStoppageDetails().setStoppageRevocationQuations(
					masterStoppageRevocationQuationsMapper.convertVO(actionVo.getStoppageRevokationQuations()));
		}
		if (dto.getServiceType().stream().anyMatch(service -> service.equals(ServiceEnum.RCCANCELLATION))
				&& dto.getFlowId() != null && dto.getFlowId().equals(ServiceEnum.Flow.RCCANCELLATIONEMVIACTION)) {
			if (actionVo.getRcQuesList() == null && actionVo.getRcQuesList().isEmpty()) {
				throw new BadRequestException("Rc cancellation questions and answers not found");
			}
			dto.setRcQuesList(rcQuestionsMapper.convertVo(actionVo.getRcQuesList()));
			if (StringUtils.isNoneEmpty(actionVo.getRcCancellationAction().getMviRemarks())) {
				dto.getRcCancellation().setMviRemarks(actionVo.getRcCancellationAction().getMviRemarks());
			}
			dto.setMviDone(true);
		}

	}

	@Override
	public Optional<RegServiceVO> getServicesDetailsByApplicationNO(JwtUser jwtUser, String applicationNo,
			String role) {

		RegServiceDTO stagingDetailsDTO = regServiceDAO.findOne(applicationNo);
		if (null == stagingDetailsDTO) {
			logger.error("Application not found [{}]" + applicationNo);
			throw new BadRequestException("Application not found, applicationNo:" + applicationNo);
		}
		if (stagingDetailsDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWFC.getId()))
				|| stagingDetailsDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RENEWALFC.getId()))
				|| stagingDetailsDTO.getServiceIds().stream()
						.anyMatch(id -> id.equals(ServiceEnum.OTHERSTATIONFC.getId()))) {
			logger.error(
					"This application belongs to Fitness service. Please proceed through Fitness link from dashboard.");
			throw new BadRequestException(
					"This application belongs to Fitness service. Please proceed through Fitness link from dashboard.");
		}
		if (!jwtUser.getOfficeCode().equals(stagingDetailsDTO.getMviOfficeCode())) {
			logger.error("The application is not related to your office [{}]", applicationNo);
			throw new BadRequestException("The application '" + applicationNo + "' not related to your office:");
		}
		if (!LocalDate.now().equals(stagingDetailsDTO.getSlotDetails().getSlotDate())) {
			logger.error("The application Slot date is not today [{}]", applicationNo);
			throw new BadRequestException("The application '" + applicationNo + "' Slot date is not today");
		}
		ActionDetails actionDetails = getActionDetailByRole(stagingDetailsDTO, role);
		if (actionDetails.getIsDoneProcess()) {
			logger.error("The application already verified at MVI level");
			throw new BadRequestException("The application '" + applicationNo + "' already verified at MVI level");
		}
		// RegServiceVO regVO=regServiceMapper.convertEntity(stagingDetailsDTO);
		RegServiceVO vo = regServiceMapper.convertEntity(stagingDetailsDTO);
		if (stagingDetailsDTO.getServiceIds().stream()
				.anyMatch(id -> id.equals(ServiceEnum.ALTERATIONOFVEHICLE.getId()))) {
			if (StringUtils.isNoneBlank(stagingDetailsDTO.getAlterationDetails().getVehicleTypeFrom())
					&& StringUtils.isNoneBlank(stagingDetailsDTO.getAlterationDetails().getVehicleTypeTo())
					&& stagingDetailsDTO.getAlterationDetails().getVehicleTypeTo()
							.equalsIgnoreCase(CovCategory.T.getCode())) {
				Optional<PropertiesDTO> optionalPropertie = propertiesDAO.findByModule(ModuleEnum.FC.toString());
				if (optionalPropertie.isPresent()) {
					if (optionalPropertie.get().getListOfficesForGati().stream()
							.anyMatch(office -> office.equalsIgnoreCase(stagingDetailsDTO.getMviOfficeCode()))) {
						vo.setIsFor6Months(Boolean.TRUE);
					}
				}
				if (stagingDetailsDTO.getAlterationDetails().getVehicleTypeTo()
						.equalsIgnoreCase(CovCategory.T.getCode())
						&& stagingDetailsDTO.getAlterationDetails().getVehicleTypeFrom()
								.equalsIgnoreCase(CovCategory.T.getCode())) {
					Optional<ClassOfVehicleConversion> covConversion = classOfVehicleConversionDAO
							.findByNewCovAndNewCategoryAndCovAndCategory(
									stagingDetailsDTO.getAlterationDetails().getCov(),
									stagingDetailsDTO.getAlterationDetails().getVehicleTypeTo(),
									stagingDetailsDTO.getAlterationDetails().getFromCov(),
									stagingDetailsDTO.getAlterationDetails().getVehicleTypeFrom());
					if (!covConversion.isPresent()) {
						logger.error("no records found in master conversion document : ", stagingDetailsDTO.getPrNo());
						throw new BadRequestException(
								"no records found in master conversion document: " + stagingDetailsDTO.getPrNo());
					}
					if (!covConversion.get().isFcFee()) {
						vo.setIsFor6Months(Boolean.FALSE);
					}

				}
			}
		}

		return Optional.ofNullable(vo);
	}

	@Override
	public void saveMviActions(RtaActionVO vo, MultipartFile[] uploadfiles, JwtUser jwtUser) {

		String role = dTOUtilService.getRole(jwtUser.getId(), vo.getSelectedRole());
		logger.debug("In UserDetails Role [{}]", vo.getSelectedRole());

		synchronized (vo.getApplicationNo().intern()) {
			this.isImagesUploadOrNotCheck(vo, uploadfiles);
			if (null != vo.getImageInput() && !vo.getImageInput().isEmpty()) {
				vo.setImages(vo.getImageInput());
			}

			this.approvalProcess(jwtUser, vo, role, uploadfiles);
		}
	}

	public void saveFCData(RegistrationDetailsDTO staginDto, RegServiceDTO regServiceDTO) {

		if (staginDto.getVehicleType().equals(CovCategory.T.getCode())) {

			FcDetailsDTO fcdetails = new FcDetailsDTO();
			fcdetails.setApplicationNo(staginDto.getApplicationNo());
			fcdetails.setOfficeCode(staginDto.getOfficeDetails().getOfficeCode());
			fcdetails.setOfficeName(staginDto.getOfficeDetails().getOfficeName());
			fcdetails.setVehicleNumber("");

			ActionDetails actionDetails = this.getActionDetailByRole(regServiceDTO, RoleEnum.MVI.getName());
			MasterUsersDTO userDto = masterUsersDAO.findByUserId(actionDetails.getUserId());
			if (userDto != null) {
				fcdetails.setInspectedMviName(userDto.getFirstName());
			}
			Optional<OfficeDTO> optionalOffice = officeDAO.findByOfficeCode(userDto.getOffice().getOfficeCode());

			if (!optionalOffice.isPresent()) {
				throw new BadRequestException("Office details not found for: " + userDto.getOffice().getOfficeCode());
			}
			fcdetails.setInspectedMviOfficeName(optionalOffice.get().getOfficeName());

			fcdetails.setInspectedDate(actionDetails.getlUpdate());
			fcdetails.setUserId(actionDetails.getUserId());
			// fcdetails.setInspectedMviName(getMVIName(staginDto).get(RoleEnum.MVI.toString()).toString()
			// !=null?getMVIName(staginDto).get(RoleEnum.MVI.toString()).toString():"");
			// fcdetails.setInspectedDate(getMVIName(staginDto).get("DATE"));
			fcdetails.setChassisNo(staginDto.getVahanDetails().getChassisNumber());
			fcdetails.setEngineNo(staginDto.getVahanDetails().getEngineNumber());
			fcdetails.setClassOfVehicle(staginDto.getClassOfVehicle());
			LocalDateTime fcValidity = calculateFcValidity(regServiceDTO);
			fcdetails.setFcValidUpto(fcValidity.toLocalDate());
			fcdetails.setFcIssuedDate(LocalDateTime.now());
			fcdetails.setCreatedDate(LocalDateTime.now());
			// TODO : Need to generate Sequence Number
			Map<String, String> detail = new HashMap<String, String>();
			detail.put("officeCode", staginDto.getOfficeDetails().getOfficeCode());
			String fcNumber = sequenceGenerator.getSequence(String.valueOf(Sequence.FC.getSequenceId()), detail);
			fcdetails.setFcNumber(fcNumber);
			fcdetails.setTrNo(staginDto.getTrNo());
			List<ServiceEnum> serviceType = new ArrayList<>();
			serviceType.add(ServiceEnum.RENEWALFC);
			serviceType.add(ServiceEnum.OTHERSTATIONFC);
			if (regServiceDTO.getServiceType() != null
					&& regServiceDTO.getServiceType().stream().anyMatch(id -> serviceType.contains(id))) {
				fcdetails.setFctype(ServiceEnum.RENEWALFC.getDesc());
			} else {
				fcdetails.setFctype(ServiceEnum.NEWFC.getDesc());
			}

			if (regServiceDTO.getAlterationDetails() != null
					&& StringUtils.isNoneBlank(regServiceDTO.getAlterationDetails().getPrNo())) {
				fcdetails.setPrNo(regServiceDTO.getAlterationDetails().getPrNo());
			} else {
				fcdetails.setPrNo(staginDto.getPrNo());
			}
			List<FcDetailsDTO> fcDetailsDtoList = fcDetailsDAO
					.findFirst5ByStatusIsTrueAndPrNoOrderByCreatedDateDesc(staginDto.getPrNo());
			if (!fcDetailsDtoList.isEmpty()) {
				fcDetailsDtoList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
				for (FcDetailsDTO dto : fcDetailsDtoList) {
					dto.setStatus(Boolean.FALSE);
					fcDetailsDAO.save(dto);
				}
			}
			fcDetailsDAO.save(fcdetails);
		}

	}

	private LocalDateTime calculateFcValidity(RegServiceDTO regServiceDTO) {
		LocalDateTime fcValidity = LocalDateTime.now().minusDays(1).plusYears(1);

		/*
		 * if (regServiceDTO.getAlterationDetails().getFcType() == null) { throw new
		 * BadRequestException( "Please select FC type, applicationNo:" +
		 * regServiceDTO.getAlterationDetails().getApplicationNo()); } if
		 * (regServiceDTO.getAlterationDetails().getFcType().equals(FcValidityTypesEnum.
		 * TWOYEARSFC)) { fcValidity = LocalDateTime.now().minusDays(1).plusYears(2);
		 * fcValidity = calculateFcForEibt(regServiceDTO, fcValidity, 2); } else if
		 * (regServiceDTO.getAlterationDetails().getFcType().equals(FcValidityTypesEnum.
		 * ONEYEARFC)) { fcValidity = LocalDateTime.now().minusDays(1).plusYears(1);
		 * fcValidity = calculateFcForEibt(regServiceDTO, fcValidity, 1); } else
		 */if (regServiceDTO.getAlterationDetails().getFcType() != null
				&& regServiceDTO.getAlterationDetails().getFcType().equals(FcValidityTypesEnum.GATIFC)) {
			fcValidity = LocalDateTime.now().minusDays(1).plusMonths(6);
			fcValidity = calculateFcForEibt(regServiceDTO, fcValidity, 1);
		} else {
			if (regServiceDTO.getRegistrationDetails() == null
					|| regServiceDTO.getRegistrationDetails().getRegistrationValidity() == null
					|| regServiceDTO.getRegistrationDetails().getRegistrationValidity().getPrGeneratedDate() == null) {
				throw new BadRequestException(
						"PR generated date missing for calculating FC validity:" + regServiceDTO.getPrNo());

			}
			double vehicleAge = calculateAgeOfTheVehicle(
					regServiceDTO.getRegistrationDetails().getRegistrationValidity().getPrGeneratedDate(),
					LocalDate.now());
			LocalDate date = LocalDate.of(2019, Month.MAY, 14);
			int noOfyears = 1;
			if (date.isAfter(LocalDate.now()) || date.isEqual(LocalDate.now())) {
				noOfyears = 2;
			}
			if (vehicleAge > 8) {
				fcValidity = LocalDateTime.now().minusDays(1).plusYears(1);
				fcValidity = calculateFcForEibt(regServiceDTO, fcValidity, noOfyears);
			} else {
				fcValidity = LocalDateTime.now().minusDays(1).plusYears(2);
				fcValidity = calculateFcForEibt(regServiceDTO, fcValidity, noOfyears);
			}
		}

		return fcValidity;
	}

	public double calculateAgeOfTheVehicle(LocalDate localDateTime, LocalDate entryDate) {

		double yeare = localDateTime.until(entryDate, ChronoUnit.DAYS) / 365.2425f;
		yeare = Math.abs(yeare);
		yeare = Math.ceil(yeare);
		return yeare;

	}

	private LocalDateTime calculateFcForEibt(RegServiceDTO regServiceDTO, LocalDateTime fcValidity, int years) {
		if ((regServiceDTO.getAlterationDetails().getCov() != null
				&& StringUtils.isNoneBlank(regServiceDTO.getAlterationDetails().getCov())
				&& regServiceDTO.getAlterationDetails().getCov().equalsIgnoreCase(ClassOfVehicleEnum.EIBT.getCovCode()))
				|| ((regServiceDTO.getRegistrationDetails().getClassOfVehicle()
						.equalsIgnoreCase(ClassOfVehicleEnum.EIBT.getCovCode()))
						&& (regServiceDTO.getServiceIds().stream()
								.anyMatch(service -> service.equals(ServiceEnum.NEWFC.getId()))
								|| regServiceDTO.getServiceIds().stream()
										.anyMatch(service -> service.equals(ServiceEnum.RENEWALFC.getId()))
								|| regServiceDTO.getServiceIds().stream()
										.anyMatch(service -> service.equals(ServiceEnum.OTHERSTATIONFC.getId())
												&& regServiceDTO.getServiceIds().size() == 1)))) {
			LocalDate.now().getMonth();
			int val = LocalDate.now().getMonth().compareTo(Month.MAY);
			int year;
			if (val > 0) {
				year = LocalDateTime.now().getYear() + years;
			} else if (val == 0) {
				if (LocalDate.now().getDayOfMonth() <= 15) {
					year = LocalDateTime.now().getYear();
					if (years == 2) {
						year = year + 1;
					}
				} else {
					year = LocalDateTime.now().getYear() + years;
				}
			} else {
				year = LocalDateTime.now().getYear();
				if (years == 2) {
					year = year + 1;
				}
			}
			fcValidity = LocalDateTime.of(year, Month.MAY, 15, 0, 0);
		}
		return fcValidity;
	}

	private void saveRegServicesLogs(RegServiceDTO regServiceDTO) {
		if (StatusRegistration.APPROVED.equals(regServiceDTO.getApplicationStatus())
				&& RoleEnum.RTO.getIndex() < regServiceDTO.getCurrentIndex()) {
			regServiceApprovedDAO.save((RegServiceApprovedDTO) regServiceDTO);
		}
		RegServiceDTO regDTO = regServiceDAO.findOne(regServiceDTO.getApplicationNo());
		RegServiceLogsDTO regServiceLogsDTO = new RegServiceLogsDTO();
		regServiceLogsDTO.setRgServDetails(regDTO);
		regServiceLogDAO.save(regServiceLogsDTO);

	}

	@Override
	public void updateTaxDetailsInRegCollection(RegServiceDTO regServiceDTO, RegistrationDetailsDTO staginDto) {

		if (regServiceDTO.getTaxAmount() != null)
			staginDto.setTaxAmount(regServiceDTO.getTaxAmount());
		if (regServiceDTO.getTaxType() != null)
			staginDto.setTaxType(regServiceDTO.getTaxType());
		if (regServiceDTO.getTaxvalidity() != null && staginDto.getRegistrationValidity() != null) {
			staginDto.setTaxvalidity(regServiceDTO.getTaxvalidity());
			staginDto.getRegistrationValidity().setTaxValidity(regServiceDTO.getTaxvalidity());
		}

		if (regServiceDTO.getCesFee() != null)
			staginDto.setCesFee(regServiceDTO.getCesFee());

		if (regServiceDTO.getCesValidity() != null) {
			staginDto.setCesValidity(regServiceDTO.getCesValidity());
			staginDto.getRegistrationValidity().setCessValidity(regServiceDTO.getCesValidity());
		}
	}

	private void updatesHPT(RegServiceDTO regServiceDTO, RegistrationDetailsDTO registrationDetailsDTO) {
		FinanceDetailsDTO financeDetailsDTO = new FinanceDetailsDTO();
		financeDetailsDTO.setPrNo(registrationDetailsDTO.getPrNo());
		financeDetailsDTO.setServiceIds(regServiceDTO.getServiceIds());
		financeDetailsDTO.setApplicationNo(regServiceDTO.getApplicationNo());
		if (registrationDetailsDTO.getFinanceDetails() != null
				&& !regServiceDTO.getServiceIds().contains(ServiceEnum.HPA.getId())) {
			updateHPTFinanceLogs(financeDetailsDTO, registrationDetailsDTO);
		}
		if (!regServiceDTO.getServiceIds().contains(ServiceEnum.HPA.getId())) {
			regServiceDTO.setHptTerminatedDate(LocalDate.now());
			registrationDetailsDTO.setIsFinancier(false);
			registrationDetailsDTO.setFinanceDetails(null);
		}
		registrationDetailsDTO.setTerminationDate(LocalDateTime.now());

	}

	public void updateHPTFinanceLogs(FinanceDetailsDTO financeDetailsDTO,
			RegistrationDetailsDTO registrationDetailsDTO) {
		financeDetailsDTO.setActionType(ServiceEnum.HIREPURCHASETERMINATION.getDesc());
		financeDetailsDTO.setTerminateDate(LocalDateTime.now());
		if (registrationDetailsDTO.getFinanceDetails() != null) {
			financeDetailsDTO.setAadharResponse(registrationDetailsDTO.getFinanceDetails().getAadharResponse());
			financeDetailsDTO.setFinancerName(registrationDetailsDTO.getFinanceDetails().getFinancerName());
			financeDetailsDTO.setFinanceType(registrationDetailsDTO.getFinanceDetails().getFinanceType());
			financeDetailsDTO.setUserId(registrationDetailsDTO.getFinanceDetails().getUserId());
			financeDetailsDTO.setStatus(StatusRegistration.FINANCIERTERMINATED.getDescription());
			financeDetailsDTO.setTerminateDate(registrationDetailsDTO.getFinanceDetails().getTerminateDate());
			financeDetailsDTO.setActionType(ServiceEnum.HIREPURCHASETERMINATION.getDesc());
		}
		financeDetailsDAO.save(financeDetailsDTO);
	}

	public void updateHPAFinanceLogs(FinanceDetailsDTO financeDetailsDTO, RegServiceDTO regServiceDTO) {
		financeDetailsDTO.setActionType(ServiceEnum.HPA.getDesc());
		if (regServiceDTO.getFinanceDetails() != null) {
			financeDetailsDTO.setAadharResponse(regServiceDTO.getFinanceDetails().getAadharResponse());
			financeDetailsDTO.setFinancerName(regServiceDTO.getFinanceDetails().getFinancerName());
			financeDetailsDTO.setFinanceType(regServiceDTO.getFinanceDetails().getFinanceType());
			financeDetailsDTO.setUserId(regServiceDTO.getFinanceDetails().getUserId());
			financeDetailsDTO.setStatus(regServiceDTO.getFinanceDetails().getStatus());
			financeDetailsDTO.setAgreementDate(regServiceDTO.getFinanceDetails().getAgreementDate());
			financeDetailsDTO.setServiceIds(regServiceDTO.getServiceIds());
			financeDetailsDTO.setApplicationNo(regServiceDTO.getApplicationNo());
		}
		financeDetailsDAO.save(financeDetailsDTO);

	}

	private void updatesHPA(RegServiceDTO regServiceDTO, RegistrationDetailsDTO registrationDetailsDTO) {
		if (registrationDetailsDTO.getFinanceDetails() != null
				&& regServiceDTO.getServiceIds().contains(ServiceEnum.HIREPURCHASETERMINATION.getId())) {
			FinanceDetailsDTO financeDTO = new FinanceDetailsDTO();
			financeDTO.setPrNo(registrationDetailsDTO.getPrNo());
			financeDTO.setApplicationNo(regServiceDTO.getApplicationNo());
			financeDTO.setServiceIds(regServiceDTO.getServiceIds());
			updateHPTFinanceLogs(financeDTO, registrationDetailsDTO);

		}
		registrationDetailsDTO.setFinanceDetails(regServiceDTO.getFinanceDetails());
		registrationDetailsDTO.setIsFinancier(true);
		FinanceDetailsDTO financeDetailsDTO = new FinanceDetailsDTO();
		financeDetailsDTO.setPrNo(registrationDetailsDTO.getPrNo());
		updateHPAFinanceLogs(financeDetailsDTO, regServiceDTO);
	}

	private void updatesNewFC(RegServiceDTO regServiceDTO, RegistrationDetailsDTO registrationDetailsDTO) {
		// TODO Auto-generated method stub
		this.saveFCData(registrationDetailsDTO, regServiceDTO);
		LocalDateTime date = calculateFcValidity(regServiceDTO);
		registrationDetailsDTO.getRegistrationValidity().setRegistrationValidity(date);
		registrationDetailsDTO.getRegistrationValidity().setFcValidity(date.toLocalDate());
		registrationDetailsDTO.setCfxIssued(Boolean.FALSE);

	}

	@Override
	public RegServiceVO getVehicleDetails(String prNo, JwtUser jwtUser, String role, String aadharNo) {
		// TODO Auto-generated method stub
		Optional<UserDTO> userDetails = userDAO.findByUserId(jwtUser.getId());
		if (!userDetails.isPresent()) {
			logger.error("user details not found. [{}] ", jwtUser.getId());
			throw new BadRequestException("No record found. " + jwtUser.getId());
		}
		if (!userDetails.get().getAadharNo().equalsIgnoreCase(aadharNo)) {
			logger.error("you are not authorized to access this application ");
			throw new BadRequestException("you are not authorized to access this application");
		}
		Optional<RegistrationDetailsDTO> regDTO = registrationDetailDAO.findByPrNo(prNo);
		if (!regDTO.isPresent()) {
			logger.error("No record found. [{}],[{}] ", prNo);
			throw new BadRequestException("No record found.Pr no: " + prNo);
		}
		String roles = dTOUtilService.getRole(jwtUser.getId(), role);
		validatiformvi(regDTO, roles);
		List<FcDetailsDTO> listOfFc = fcDetailsDAO
				.findFirst5ByStatusIsTrueAndPrNoOrderByCreatedDateDesc(regDTO.get().getPrNo());
		if (listOfFc.isEmpty()) {
			logger.error("NO FITNESS CERTIFICATE DETAILS NOT FOUND");
			throw new BadRequestException("NO FITNESS CERTIFICATE DETAILS NOT FOUND");
		}
		if (regDTO.get().isCfxIssued()) {
			logger.error("Already CFX issued on this PR [{}]", prNo);
			throw new BadRequestException("Already CFX issued on this PR. " + prNo);
		}
		List<String> taxTypes = new ArrayList<>();
		taxTypes.add(ServiceCodeEnum.QLY_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.HALF_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.YEAR_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.LIFE_TAX.getCode());
		List<TaxDetailsDTO> taxDetailsList = taxDetailsDAO
				.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(regDTO.get().getApplicationNo(),
						taxTypes);

		if (taxDetailsList.isEmpty()) {
			logger.error("TaxDetails not found: [{}]", regDTO.get().getApplicationNo());
			throw new BadRequestException("TaxDetails not found:" + regDTO.get().getApplicationNo());
		}
		registrationService.updatePaidDateAsCreatedDate(taxDetailsList);
		taxDetailsList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		TaxDetailsDTO taxDto = taxDetailsList.stream().findFirst().get();
		RegServiceVO vo = new RegServiceVO();
		RegistrationDetailsVO regvo = registrationDetailsMapper.convertEntity(regDTO.get());
		if (taxDto.getTaxPeriodEnd() != null) {
			if (regvo.getRegistrationValidity() != null && regvo.getRegistrationValidity().getTaxValidity() != null) {
				regvo.getRegistrationValidity().setTaxValidity(taxDto.getTaxPeriodEnd());
			}
		}
		vo.setRegistrationDetails(regvo);
		taxDetailsList.clear();
		return vo;
	}

	@Override
	public String savecfxDetails(RtaActionVO actionActionVo, JwtUser jwtUser) {
		// TODO Auto-generated method stub
		Optional<RegistrationDetailsDTO> regDTO = registrationDetailDAO
				.findByApplicationNo(actionActionVo.getApplicationNo());
		if (!regDTO.isPresent()) {
			logger.error("No record found. [{}],[{}] ", actionActionVo.getApplicationNo());
			throw new BadRequestException("No record found.application no: " + actionActionVo.getApplicationNo());
		}
		String roles = dTOUtilService.getRole(jwtUser.getId(), actionActionVo.getSelectedRole());
		validatiformvi(regDTO, roles);
		List<FcDetailsDTO> listOfFc = fcDetailsDAO
				.findFirst5ByStatusIsTrueAndPrNoOrderByCreatedDateDesc(regDTO.get().getPrNo());
		if (listOfFc.isEmpty()) {
			logger.error("FITNESS CERTIFICATE DETAILS NOT FOUND");
			throw new BadRequestException("FITNESS CERTIFICATE DETAILS NOT FOUND");
		}
		FcDetailsDTO dto = listOfFc.stream().findFirst().get();

		dto.setDlNumber(actionActionVo.getDlNumber());
		dto.setVcrNumber(actionActionVo.getVcrNumber());
		dto.setPlaceOfChecking(actionActionVo.getPlaceOfChecking());
		dto.setDestination(actionActionVo.getDestination());
		dto.setMaxSpeed(actionActionVo.getMaxSpeed());
		dto.setDriverName(actionActionVo.getDriverName());
		dto.setCfxType(actionActionVo.getCfxType());
		dto.setDefectComment(actionActionVo.getDefectComment());
		dto.setCfxIssuedMviName(jwtUser.getFirstname());
		Optional<OfficeDTO> optionalOfficeDetails = officeDAO.findByOfficeCode(jwtUser.getOfficeCode());
		if (!optionalOfficeDetails.isPresent()) {
			logger.error("officeDetailas not found [{}]", jwtUser.getOfficeCode());
			throw new BadRequestException("officeDetailas not found. " + jwtUser.getOfficeCode());
		}
		dto.setCfxIssuedOfficeName(optionalOfficeDetails.get().getOfficeName());
		regDTO.get().setCfxIssued(Boolean.TRUE);
		fcDetailsDAO.save(dto);
		registrationDetailDAO.save(regDTO.get());
		return regDTO.get().getApplicationNo();
	}

	private void validatiformvi(Optional<RegistrationDetailsDTO> regDTO, String roles) {
		if (!roles.equalsIgnoreCase(RoleEnum.MVI.getName())) {
			logger.error("Invalid user");
			throw new BadRequestException("Invalid user");
		}
		if (!regDTO.get().getVehicleType().equalsIgnoreCase("T")) {
			logger.error("Invalid Vehicle type");
			throw new BadRequestException("Invalid Vehicle type");
		}
	}

	private String replaceDefaults(String input) {

		if (null == input || StringUtils.isBlank(input)) {
			return StringUtils.EMPTY;
		}
		return input;
	}

	private List<PermitDetailsDTO> permitDetails(String prNo) {
		return permitDetailsDAO.findByPrNoAndPermitStatus(prNo, PermitsEnum.ACTIVE.getDescription());
	}

	public LocalDate getTaxDate(RegistrationDetailsDTO registrationOptional) {

		List<String> taxTypes = new ArrayList<>();
		taxTypes.add(ServiceCodeEnum.QLY_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.HALF_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.YEAR_TAX.getCode());

		List<TaxDetailsDTO> listOfGreenTax = getTaxDetails(registrationOptional, taxTypes);
		if (listOfGreenTax != null && !listOfGreenTax.isEmpty()) {
			listOfGreenTax.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
			TaxDetailsDTO dto = listOfGreenTax.stream().findFirst().get();

			for (Map<String, TaxComponentDTO> map : dto.getTaxDetails()) {

				for (Entry<String, TaxComponentDTO> entry : map.entrySet()) {
					if (taxTypes.stream().anyMatch(key -> key.equalsIgnoreCase(entry.getKey()))) {
						return entry.getValue().getValidityTo();
					}
				}
			}
		}
		return null;

	}

	private List<TaxDetailsDTO> getTaxDetails(RegistrationDetailsDTO registrationOptional, List<String> taxType) {

		List<TaxDetailsDTO> listOfTaxDetails = new ArrayList<>();
		List<TaxDetailsDTO> listOfGreenTax = taxDetailsDAO
				.findFirst10ByApplicationNoOrderByCreatedDateDesc(registrationOptional.getApplicationNo());
		if (listOfGreenTax.isEmpty()) {
			logger.error("TaxDetails not found: [{}]", registrationOptional.getPrNo());
			throw new BadRequestException("TaxDetails not found:" + registrationOptional.getPrNo());
		}
		registrationService.updatePaidDateAsCreatedDate(listOfGreenTax);
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

	private void saveElasticSearchData(ElasticSecondVehicleDTO elasticSecondVehicleDto,
			RegistrationDetailsDTO registrationDetailsDTO) {
		elasticSecondVehicleDto.setPrNumber(registrationDetailsDTO.getPrNo());
		elasticSecondVehicleDto.setIsTowDone(Boolean.TRUE);
		elasticSecondVehicleDto
				.setFirstName(replaceDefaults(registrationDetailsDTO.getApplicantDetails().getFirstName()));
		elasticSecondVehicleDto
				.setLastName(replaceDefaults(registrationDetailsDTO.getApplicantDetails().getLastName()));
		elasticSecondVehicleDto
				.setFatherName(replaceDefaults(registrationDetailsDTO.getApplicantDetails().getFatherName()));
		elasticSecondVehicleDto.setDob(registrationDetailsDTO.getApplicantDetails().getDateOfBirth());
		elasticSecondVehicleDto
				.setDisplayName(replaceDefaults(registrationDetailsDTO.getApplicantDetails().getDisplayName()));
		elasticSecondVehicleDto.setAddress1(StringUtils.EMPTY);
		elasticSecondVehicleDto.setAddress2(StringUtils.EMPTY);
		elasticSecondVehicleDto.setAddress3(StringUtils.EMPTY);
		if (registrationDetailsDTO.getApplicantDetails().getPresentAddress() != null) {
			if (registrationDetailsDTO.getApplicantDetails().getPresentAddress().getVillage() != null) {
				elasticSecondVehicleDto.setAddress2(replaceDefaults(registrationDetailsDTO.getApplicantDetails()
						.getPresentAddress().getVillage().getVillageName()));
			}

			elasticSecondVehicleDto.setAddress1(
					replaceDefaults(registrationDetailsDTO.getApplicantDetails().getPresentAddress().getDoorNo()) + ","
							+ replaceDefaults(
									registrationDetailsDTO.getApplicantDetails().getPresentAddress().getStreetName()));
			if (registrationDetailsDTO.getApplicantDetails().getPresentAddress().getMandal() != null) {
				elasticSecondVehicleDto.setMandal(replaceDefaults(
						registrationDetailsDTO.getApplicantDetails().getPresentAddress().getMandal().getMandalName()));
			}
			if (registrationDetailsDTO.getApplicantDetails().getPresentAddress().getDistrict() != null) {
				elasticSecondVehicleDto.setDistrict(replaceDefaults(registrationDetailsDTO.getApplicantDetails()
						.getPresentAddress().getDistrict().getDistrictName()));
				elasticSecondVehicleDto.setAddress3(replaceDefaults(registrationDetailsDTO.getApplicantDetails()
						.getPresentAddress().getDistrict().getDistrictName()));
			}
		}
		elasticSecondVehicleDto.setClassOfVehicle(replaceDefaults(registrationDetailsDTO.getClassOfVehicle()));
		if (registrationDetailsDTO.getOfficeDetails() != null) {
			elasticSecondVehicleDto
					.setOfficeCode(replaceDefaults(registrationDetailsDTO.getOfficeDetails().getOfficeCode()));
			elasticSecondVehicleDto
					.setOfficeAddress(replaceDefaults(registrationDetailsDTO.getOfficeDetails().getOfficeName()));
		}
		elasticSecondVehicleDto.setIsAadhaarValidate(false);
		if (registrationDetailsDTO.getApplicantDetails() != null) {
			elasticSecondVehicleDto
					.setIsAadhaarValidate(registrationDetailsDTO.getApplicantDetails().getIsAadhaarValidated());
		}
		elasticSecondVehicleDto
				.setAadhaarNo(replaceDefaults(registrationDetailsDTO.getApplicantDetails().getAadharNo()));
		elasticSecondVehicleDAO.save(elasticSecondVehicleDto);
	}

	private TaxDetailsDTO saveCitizenTaxDetails(RegServiceDTO regServiceDTO, boolean secoundVehicleDiffTaxPaid,
			boolean isChassisVehicle, String stateCode) {
		// need to update pr and second vehicel tax and flag
		TaxDetailsDTO dto = new TaxDetailsDTO();
		if (regServiceDTO.getTaxAmount() != null && regServiceDTO.getTaxAmount() != 0
				|| regServiceDTO.getCesFee() != null && regServiceDTO.getCesFee() != 0
				|| regServiceDTO.getGreenTaxAmount() != null && regServiceDTO.getGreenTaxAmount() != 0) {

			List<Map<String, TaxComponentDTO>> taxDetails = new ArrayList<>();

			dto.setApplicationNo(regServiceDTO.getRegistrationDetails().getApplicationNo());
			dto.setCovCategory(regServiceDTO.getRegistrationDetails().getOwnerType());
			dto.setModule(ModuleEnum.REG.getCode());
			dto.setPaymentPeriod(regServiceDTO.getTaxType());

			dto.setTrNo(regServiceDTO.getRegistrationDetails().getTrNo());
			dto.setPrNo(regServiceDTO.getRegistrationDetails().getPrNo());

			dto.setTaxPeriodFrom(LocalDate.now());
			if (regServiceDTO.getPayTaxType() != null) {
				dto.setPaymentPeriod(regServiceDTO.getTaxType());
			} else {
				dto.setPaymentPeriod(TaxTypeEnum.QuarterlyTax.getDesc());
			}
			// Tax
			// TODO : Change to Enum
			if (regServiceDTO.getTaxAmount() != null && regServiceDTO.getTaxAmount() != 0) {
				dto.setTaxAmount(regServiceDTO.getTaxAmount());
				if (regServiceDTO.getTaxAmount() == 1) {
					dto.setTaxAmount(0l);
				}

				dto.setTaxPeriodEnd(regServiceDTO.getTaxvalidity());
				Double taxArrears = 0d;
				Long penalty = 0l;
				Long penaltyArrears = 0l;
				if (regServiceDTO.getTaxArrears() != null)
					taxArrears = Double.valueOf(regServiceDTO.getTaxArrears().toString());
				if (regServiceDTO.getPenalty() != null)
					penalty = regServiceDTO.getPenalty();
				if (regServiceDTO.getPenaltyArrears() != null)
					penaltyArrears = regServiceDTO.getPenaltyArrears();
				this.addTaxDetails(dto.getPaymentPeriod(), Double.valueOf(dto.getTaxAmount().toString()),
						LocalDateTime.now(), regServiceDTO.getTaxvalidity(), LocalDate.now(), taxDetails, penalty,
						taxArrears, penaltyArrears);
			}

			dto.setPermitType("INA");
			dto.setTaxExcemption(Boolean.TRUE);
			if (regServiceDTO.getCesFee() != null && regServiceDTO.getCesFee() != 0) {

				dto.setCessFee(regServiceDTO.getCesFee());
				dto.setCessPeriodEnd(regServiceDTO.getCesValidity());
				dto.setCessPeriodFrom(LocalDate.now());

				this.addTaxDetails(ServiceCodeEnum.CESS_FEE.getCode(),
						Double.valueOf(regServiceDTO.getCesFee().toString()), LocalDateTime.now(),
						regServiceDTO.getCesValidity(), LocalDate.now(), taxDetails, null, null, null);
			}
			if (regServiceDTO.getGreenTaxAmount() != null && regServiceDTO.getGreenTaxAmount() != 0) {

				dto.setGreenTaxAmount(regServiceDTO.getGreenTaxAmount());
				dto.setGreenTaxPeriodEnd(regServiceDTO.getGreenTaxvalidity());

				this.addTaxDetails(ServiceCodeEnum.GREEN_TAX.getCode(),
						Double.valueOf(regServiceDTO.getGreenTaxAmount().toString()), LocalDateTime.now(),
						regServiceDTO.getGreenTaxvalidity(), LocalDate.now(), taxDetails, null, null, null);
			}
			dto.setCreatedDate(LocalDateTime.now());
			dto.setTaxPaidDate(LocalDate.now());
			if (regServiceDTO.getServiceType().stream()
					.anyMatch(service -> service.equals(ServiceEnum.ALTERATIONOFVEHICLE))) {
				/*
				 * Optional<AlterationDTO> alterDetails = alterationDao
				 * .findByApplicationNo(regServiceDTO.getRegistrationDetails().getApplicationNo(
				 * )); if (!alterDetails.isPresent()) { throw new
				 * BadRequestException("No record found in alteration for: " +
				 * regServiceDTO.getRegistrationDetails().getApplicationNo()); }
				 */
				/*
				 * dto.setClassOfVehicle( regServiceDTO.getAlterationVO().getCov() != null ?
				 * regServiceDTO.getAlterationVO().getCov() :
				 * regServiceDTO.getRegistrationDetails().getClassOfVehicle());
				 */
			} else {
				dto.setClassOfVehicle(regServiceDTO.getRegistrationDetails().getClassOfVehicle());
			}
			dto.setTaxDetails(taxDetails);

			if (regServiceDTO.getRegistrationDetails().isSecondVehicleTaxPaid() && secoundVehicleDiffTaxPaid
					&& (!regServiceDTO.getRegistrationDetails().getIsFirstVehicle())) {
				dto.setSecondVehicleDiffTaxPaid(Boolean.TRUE);
			} else if (regServiceDTO.getRegistrationDetails().isSecondVehicleTaxPaid()
					&& (regServiceDTO.getRegistrationDetails().getIsFirstVehicle() != null
							&& !regServiceDTO.getRegistrationDetails().getIsFirstVehicle())) {
				dto.setSecondVehicleTaxPaid(Boolean.TRUE);
			}
			if (regServiceDTO.getRegistrationDetails().getInvoiceDetails() != null
					&& regServiceDTO.getRegistrationDetails().getInvoiceDetails().getInvoiceValue() != null) {
				dto.setInvoiceValue(regServiceDTO.getRegistrationDetails().getInvoiceDetails().getInvoiceValue());
			}

			dto.setTaxStatus(TaxStatusEnum.ACTIVE.getCode());
			dto.setRemarks("");
			dto.setOfficeCode(regServiceDTO.getRegistrationDetails().getOfficeDetails().getOfficeCode());
			dto.setStateCode(stateCode);
			taxDetailsDAO.save(dto);
		}
		return dto;
	}

	private void addTaxDetails(String taxType, Double taxAmount, LocalDateTime paidDate, LocalDate validityTo,
			LocalDate validityFrom, List<Map<String, TaxComponentDTO>> list, Long penalty, Double taxArrears,
			Long penaltyArrears) {

		Map<String, TaxComponentDTO> taxMap = new HashMap<>();

		TaxComponentDTO tax = new TaxComponentDTO();
		tax.setTaxName(taxType);
		tax.setAmount(taxAmount);
		tax.setPaidDate(paidDate);
		tax.setValidityFrom(validityFrom);
		tax.setValidityTo(validityTo);
		if (penalty != null && penalty != 0) {
			tax.setPenalty(penalty);
		}
		if (penaltyArrears != null && penaltyArrears != 0) {
			tax.setPenaltyArrears(penaltyArrears);
		}
		if (taxArrears != null && taxArrears != 0) {
			tax.setTaxArrears(taxArrears);
		}
		taxMap.put(taxType, tax);
		list.add(taxMap);

	}

	@Override
	public ApprovalProcessFlowDTO getApprovalProcessFlowDTOForLock(String role, Integer serviceId) {

		Optional<ApprovalProcessFlowDTO> approvalProcessFlowDTO = approvalProcessFlowDAO
				.findByServiceIdAndRole(serviceId, role);
		if (!approvalProcessFlowDTO.isPresent()) {
			Flow flowEnum = ServiceEnum.getBasedonParentId(serviceId);
			approvalProcessFlowDTO = approvalProcessFlowDAO.findByServiceIdAndRole(flowEnum.getId(), role);
		}

		return approvalProcessFlowDTO.get();
	}

	private void updatePermitDataEntry(RegServiceDTO regServiceDTO, RegistrationDetailsDTO registrationDetailsDTO) {
		PermitDetailsDTO permitDetailsDTO = regServiceDTO.getPdtl();
		if (regServiceDTO.getPdtl() == null) {
			logger.error("Permit details not found: [{}]", regServiceDTO.getApplicationNo());
			throw new BadRequestException("Permit details not found");
		}
		permitDetailsDTO.setPermitStatus(PermitsEnum.ACTIVE.getDescription());
		permitDetailsDTO.setCreatedDate(LocalDateTime.now());
		permitDetailsDTO.setServiceIds(regServiceDTO.getServiceIds());
		permitDetailsDTO.setServiceType(regServiceDTO.getServiceType());
		permitDetailsDTO.setPermitSurrender(Boolean.FALSE);
		permitDetailsDTO.setIsPermitDataEntry(Boolean.TRUE);
		permitDetailsDTO.setRdto(registrationDetailsDTO);
		permitDetailsDAO.save(permitDetailsDTO);
		permitDetailsDTO.setRdto(null);
		registrationDetailsDTO.setPermitDetails(permitDetailsDTO);
	}

	// paper rc taxDetails save Method
	private TaxDetailsDTO updateTaxDetailsForDataEntryWithPaperRC(RegServiceDTO staginDto) {
		TaxDetailsDTO taxDetails = new TaxDetailsDTO();
		taxDetails.setApplicationNo(staginDto.getApplicationNo());
		taxDetails.setTrNo(staginDto.getTrNo());
		taxDetails.setPrNo(staginDto.getPrNo());
		taxDetails.setClassOfVehicle(staginDto.getRegistrationDetails().getClassOfVehicle());
		LocalDate taxUpTo = citizenTaxService.validity(staginDto.getTaxDetails().getTaxType());
		org.epragati.regservice.dto.TaxDetailsDTO taxDetailsDTO = staginDto.getTaxDetails();
		taxDetails.setTaxAmount(taxDetailsDTO.getTaxAmount());
		taxDetails.setTaxPeriodFrom(taxDetailsDTO.getPaymentDAte());
		taxDetails.setTaxPeriodEnd(taxUpTo);
		taxDetails.setPaymentPeriod(staginDto.getTaxDetails().getTaxType());
		// Map for tax details
		List<Map<String, TaxComponentDTO>> taxDetailsList = new ArrayList<>();
		Map<String, TaxComponentDTO> taxMap = new HashMap<>();
		TaxComponentDTO tax = new TaxComponentDTO();
		tax.setTaxName(staginDto.getTaxDetails().getTaxType());
		tax.setAmount(Double.valueOf(taxDetailsDTO.getTaxAmount()));
		tax.setPaidDate(DateConverters.convertLocalDateToLocalDateTime(taxDetailsDTO.getPaymentDAte()));
		tax.setValidityFrom(taxDetailsDTO.getPaymentDAte());
		tax.setValidityTo(taxUpTo);
		taxMap.put(staginDto.getTaxDetails().getTaxType(), tax);
		taxDetailsList.add(taxMap);
		taxDetails.setTaxDetails(taxDetailsList);
		taxDetails.setCreatedDate(LocalDateTime.now());
		taxDetails.setTaxStatus("Active");
		return taxDetails;
	}

	// Missing fc Details
	@Override
	public void missingFcDetails() {
		logger.debug("Start other state missingFcDetails ");
		List<Integer> serviceIds = new ArrayList<>();
		serviceIds.add(ServiceEnum.DATAENTRY.getId());

		List<RegServiceDTO> listofOtherStateFc = regServiceDAO
				.findByRegistrationDetailsVehicleTypeAndServiceIdsInAndRegistrationDetailsApplicationStatus(
						CovCategory.T.getCode(), serviceIds, StatusRegistration.PRGENERATED.getDescription());

		for (RegServiceDTO regDTO : listofOtherStateFc) { // regDTO.getPrNo();
			if (StringUtils.isNoneBlank(regDTO.getPrNo())) {
				try {
					Optional<RegistrationDetailsDTO> regDetails = registrationDetailDAO.findByPrNo(regDTO.getPrNo());
					if (regDetails.isPresent()) {
						Optional<FcDetailsDTO> fcData = fcDetailsDAO.findByPrNo(regDTO.getPrNo());
						if (!fcData.isPresent()) {
							logger.info("other state PRNO:--- " + regDTO.getPrNo());
							this.saveFCDataForOtherState(regDetails.get(), regDTO);

						}
					}
					continue;
				} catch (Exception e) {
					logger.debug("Exception [{}]", e);
					logger.error(e.getMessage());
				}
			}

			logger.debug("End other state missingFcDetails ");
		}
	}

	public void saveFCDataForOtherState(RegistrationDetailsDTO staginDto, RegServiceDTO regServiceDTO) {

		if (staginDto.getVehicleType().equals(CovCategory.T.getCode())) {
			// TODO : Need to generate Sequence Number
			Map<String, String> detail = new HashMap<String, String>();
			detail.put("officeCode", staginDto.getOfficeDetails().getOfficeCode());
			String fcNumber = sequenceGenerator.getSequence(String.valueOf(Sequence.FC.getSequenceId()), detail);
			FcDetailsDTO fcdetails = new FcDetailsDTO();
			fcdetails.setApplicationNo(staginDto.getApplicationNo());
			fcdetails.setOfficeCode(staginDto.getOfficeDetails().getOfficeCode());
			fcdetails.setOfficeName(staginDto.getOfficeDetails().getOfficeName());
			fcdetails.setVehicleNumber("");
			fcdetails.setFctype(ServiceEnum.NEWFC.getDesc());
			ActionDetails actionDetails = this.getActionDetailByRole(regServiceDTO, RoleEnum.MVI.getName());
			MasterUsersDTO userDto = masterUsersDAO.findByUserId(actionDetails.getUserId());
			if (userDto != null) {
				fcdetails.setInspectedMviName(userDto.getFirstName());
			}
			Optional<OfficeDTO> optionalOffice = officeDAO.findByOfficeCode(userDto.getOffice().getOfficeCode());

			if (!optionalOffice.isPresent()) {
				logger.error("Office details not found for: [{}]", userDto.getOffice().getOfficeCode());
				throw new BadRequestException("Office details not found for: " + userDto.getOffice().getOfficeCode());
			}
			fcdetails.setInspectedMviOfficeName(optionalOffice.get().getOfficeName());

			fcdetails.setInspectedDate(actionDetails.getlUpdate());
			fcdetails.setUserId(actionDetails.getUserId());
			// fcdetails.setInspectedMviName(getMVIName(staginDto).get(RoleEnum.MVI.toString()).toString()
			// !=null?getMVIName(staginDto).get(RoleEnum.MVI.toString()).toString():"");
			// fcdetails.setInspectedDate(getMVIName(staginDto).get("DATE"));
			fcdetails.setChassisNo(staginDto.getVahanDetails().getChassisNumber());
			fcdetails.setEngineNo(staginDto.getVahanDetails().getEngineNumber());
			fcdetails.setClassOfVehicle(staginDto.getClassOfVehicle());
			// LocalDateTime fcValidity = calculateFcValidity(regServiceDTO);
			LocalDateTime fcValidity = LocalDateTime.now().minusDays(1).plusYears(2);
			fcdetails.setFcValidUpto(fcValidity.toLocalDate());
			staginDto.getRegistrationValidity().setFcValidity(fcValidity.toLocalDate());
			fcdetails.setFcIssuedDate(LocalDateTime.now());
			fcdetails.setCreatedDate(LocalDateTime.now());
			fcdetails.setFcNumber(fcNumber);
			fcdetails.setTrNo(staginDto.getTrNo());
			if (regServiceDTO.getAlterationDetails() != null
					&& StringUtils.isNoneBlank(regServiceDTO.getAlterationDetails().getPrNo())) {
				fcdetails.setPrNo(regServiceDTO.getAlterationDetails().getPrNo());
			} else {
				fcdetails.setPrNo(staginDto.getPrNo());
			}
			List<FcDetailsDTO> fcDetailsDtoList = fcDetailsDAO
					.findFirst5ByStatusIsTrueAndPrNoOrderByCreatedDateDesc(staginDto.getPrNo());
			if (!fcDetailsDtoList.isEmpty()) {
				fcDetailsDtoList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
				for (FcDetailsDTO dto : fcDetailsDtoList) {
					dto.setStatus(Boolean.FALSE);
					fcDetailsDAO.save(dto);
				}
			}
			fcDetailsDAO.save(fcdetails);
		}

	}

	private void isImagesUploadOrNotCheck(RtaActionVO vo, MultipartFile[] uploadfiles) {

		Optional<RegServiceDTO> regServiceDTOOptional = regServiceDAO.findByApplicationNo(vo.getApplicationNo());

		if (!regServiceDTOOptional.isPresent()) {
			logger.error(appMessages.getResponseMessage(MessageKeys.NO_AUTHORIZATION));
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.NO_AUTHORIZATION));
		}

		RegServiceDTO regServiceDTO = regServiceDTOOptional.get();
		if (regServiceDTO.getServiceType().stream().anyMatch(service -> getMviSideServices().contains(service))) {
			if (vo.getStatus().equals(StatusRegistration.APPROVED)) {
				if ((null == vo.getImageInput() || vo.getImageInput().isEmpty())
						&& (null == vo.getImages() || vo.getImages().isEmpty())) {
					logger.error("Images not found from UI");
					throw new BadRequestException("Images not found from UI");
				}
				if (uploadfiles == null || uploadfiles.length == 0) {
					logger.error("Please upload the images");
					throw new BadRequestException("Please upload the images");
				}
			}
		}

	}

	private List<ServiceEnum> getMviSideServices() {
		List<ServiceEnum> list = new ArrayList<>();
		list.add(ServiceEnum.NEWFC);
		list.add(ServiceEnum.RENEWALFC);
		list.add(ServiceEnum.OTHERSTATIONFC);
		list.add(ServiceEnum.ALTERATIONOFVEHICLE);
		list.add(ServiceEnum.RCFORFINANCE);

		return list;
	}

	@Override
	public void saveOtherStateData(RegServiceDTO regServiceDTO, RegistrationDetailsDTO registrationDetailsDTO) {
		this.updateTaxDetailsInRegCollection(regServiceDTO, registrationDetailsDTO);
		this.updatesInsuranceAndPUCDetails(regServiceDTO, registrationDetailsDTO);
		this.updateBasicDetails(regServiceDTO, registrationDetailsDTO);
		registrationDetailsDTO.setCardPrinted(Boolean.FALSE);
		// regServiceDTO.getServiceIds().sort((l1,l2)->l1.compareTo(l2));
		registrationDetailsDTO.setServiceIds(regServiceDTO.getServiceIds().stream().collect(Collectors.toList()));
		for (Integer serviceId : regServiceDTO.getServiceIds()) {
			ServiceEnum serviceEnum = ServiceEnum.getServiceEnumById(serviceId);
			regServiceDTO.setApprovedDateStr(LocalDateTime.now().toString());
			switch (serviceEnum) {

			case CHANGEOFADDRESS:
				updatesChangeofAddressData(regServiceDTO, registrationDetailsDTO);
				break;

			case TRANSFEROFOWNERSHIP:
				updatesTransferofOwnershipData(regServiceDTO, registrationDetailsDTO);
				break;

			case DATAENTRY:
				updatesDataEntryData(regServiceDTO, registrationDetailsDTO);
				break;
			case HPA:
				updatesHPA(regServiceDTO, registrationDetailsDTO);
				break;
			default:
				break;

			}
		}
		registrationDetailsDTO.setlUpdate(LocalDateTime.now());
		regServiceDAO.save(regServiceDTO);
		registrationDetailDAO.save(registrationDetailsDTO);
	}

	private RegServiceDTO SaveMviEditData(RtaActionVO actionVo, RegServiceDTO regServiceDTO) {

		dealerService.vahanValidationInStagingForOtherSate(actionVo.getOtherStateVO().getEngineNumber().toUpperCase(),
				actionVo.getOtherStateVO().getChassisNumber().toUpperCase());
		String prNo = null;
		if (regServiceDTO.getRegistrationDetails() != null
				&& regServiceDTO.getRegistrationDetails().getPrNo() != null) {
			prNo = regServiceDTO.getRegistrationDetails().getPrNo();
		}
		chassisNoAndEngineNoValidation(actionVo.getOtherStateVO().getChassisNumber().toUpperCase(),
				actionVo.getOtherStateVO().getEngineNumber().toUpperCase(), regServiceDTO.getApplicationNo(), prNo);
		if (actionVo.getOtherStateVO().getChassisNumber() != null)
			regServiceDTO.getRegistrationDetails().getVahanDetails()
					.setChassisNumber(actionVo.getOtherStateVO().getChassisNumber());
		if (actionVo.getOtherStateVO().getEngineNumber() != null)
			regServiceDTO.getRegistrationDetails().getVahanDetails()
					.setEngineNumber(actionVo.getOtherStateVO().getEngineNumber());

		if (actionVo.getOtherStateVO().getBodyType() != null)
			regServiceDTO.getRegistrationDetails().getVahanDetails()
					.setBodyTypeDesc(actionVo.getOtherStateVO().getBodyType());
		if (actionVo.getOtherStateVO().getManufacturedMonthYear() != null)
			regServiceDTO.getRegistrationDetails().getVahanDetails()
					.setManufacturedMonthYear(actionVo.getOtherStateVO().getManufacturedMonthYear());
		if (actionVo.getOtherStateVO().getMakersModel() != null)
			regServiceDTO.getRegistrationDetails().getVahanDetails()
					.setMakersModel(actionVo.getOtherStateVO().getMakersModel());
		if (actionVo.getOtherStateVO().getMakersDesc() != null)
			regServiceDTO.getRegistrationDetails().getVahanDetails()
					.setMakersDesc(actionVo.getOtherStateVO().getMakersDesc());
		if (actionVo.getOtherStateVO().getFuelDesc() != null)
			regServiceDTO.getRegistrationDetails().getVahanDetails()
					.setFuelDesc(actionVo.getOtherStateVO().getFuelDesc());
		/*
		 * if (actionVo.getOtherStateVO().getClassOfVehicleDesc() != null)
		 * regServiceDTO.getRegistrationDetails().getVahanDetails().setVehicleClass(
		 * actionVo.getOtherStateVO().getClassOfVehicleDesc());
		 */
		if (actionVo.getOtherStateVO().getSeating() != null)
			regServiceDTO.getRegistrationDetails().getVahanDetails()
					.setSeatingCapacity(actionVo.getOtherStateVO().getSeating());
		if (actionVo.getOtherStateVO().getRlw() != null)
			regServiceDTO.getRegistrationDetails().getVahanDetails().setGvw(actionVo.getOtherStateVO().getRlw());
		if (actionVo.getOtherStateVO().getUlw() != null)
			regServiceDTO.getRegistrationDetails().getVahanDetails()
					.setUnladenWeight(actionVo.getOtherStateVO().getUlw());
		if (actionVo.getOtherStateVO().getTrailers() != null && actionVo.getOtherStateVO().getTrailers().size() > 0) {
			List<TrailerChassisDetailsDTO> trailerDetail = new ArrayList<>();
			if (regServiceDTO.getRegistrationDetails().getVahanDetails() != null) {
				for (TrailerChassisDetailsVO trailerDetails : actionVo.getOtherStateVO().getTrailers()) {
					TrailerChassisDetailsDTO trailerDTO = new TrailerChassisDetailsDTO();
					if (trailerDetails.getMakerName() != null)
						trailerDTO.setMakerName(trailerDetails.getMakerName());
					if (trailerDetails.getChassisNo() != null)
						trailerDTO.setChassisNo(trailerDetails.getChassisNo());
					if (trailerDetails.getColor() != null)
						trailerDTO.setColour(trailerDetails.getColor());
					if (trailerDetails.getUlw() != null)
						trailerDTO.setUlw(trailerDetails.getUlw());
					if (trailerDetails.getGtw() != null)
						trailerDTO.setGtw(trailerDetails.getGtw());
					trailerDetail.add(trailerDTO);
				}
				regServiceDTO.getRegistrationDetails().getVahanDetails().setTrailerChassisDetailsDTO(trailerDetail);
			}
		}
		if (actionVo.getOtherStateVO().getClassOfVehicleDesc() != null && actionVo.getOtherStateVO().getCov() != null) {
			regServiceDTO.getRegistrationDetails().getVahanDetails()
					.setVehicleClass(actionVo.getOtherStateVO().getCov());
		}
		if (actionVo.getOtherStateVO().getColor() != null) {
			regServiceDTO.getRegistrationDetails().getVahanDetails().setColor(actionVo.getOtherStateVO().getColor());
		}
		// ChassisTrgenerated
		if (actionVo.getOtherStateVO().getLength() != null) {
			regServiceDTO.getRegistrationDetails().getVahanDetails()
					.setLength(Double.valueOf(actionVo.getOtherStateVO().getLength()));
		}
		if (actionVo.getOtherStateVO().getHeigth() != null) {
			regServiceDTO.getRegistrationDetails().getVahanDetails()
					.setHeight(Double.valueOf(actionVo.getOtherStateVO().getHeigth()));
		}
		if (actionVo.getOtherStateVO().getWidth() != null) {
			regServiceDTO.getRegistrationDetails().getVahanDetails()
					.setWidth(Double.valueOf(actionVo.getOtherStateVO().getWidth()));
		}

		return regServiceDTO;

	}

	@Override
	public Optional<RegServiceVO> getServicesDetailsByAppNO(JwtUser jwtUser, String applicationNo, String role) {
		synchronized (applicationNo.intern()) {
			RegServiceDTO stagingDetailsDTO = stoppagecommonValidation(jwtUser, applicationNo, role);
			Optional<RegServiceVO> vo = Optional.ofNullable(regServiceMapper.convertEntity(stagingDetailsDTO));
			if (stagingDetailsDTO.getServiceIds().stream()
					.anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))
					&& stagingDetailsDTO.getVehicleStoppageDetails() != null) {
				Long leftDays = daysLeftForAutoapproved(stagingDetailsDTO);
				vo.get().getVehicleStoppageDetailsVO().setDaysLeftForAutoApprove(leftDays - 1);
				List<VehicleStoppageMVIReportVO> pendingReports = this.getPendingQuarters(stagingDetailsDTO);
				int quarterNumber = 0;
				boolean imageNotUploded = Boolean.TRUE;
				if (role.equalsIgnoreCase(RoleEnum.MVI.getName()) && pendingReports != null
						&& !pendingReports.isEmpty()) {
					quarterNumber = pendingReports.stream().findFirst().get().getQuarterNumber();

				} else {
					imageNotUploded = Boolean.FALSE;
				}
				Optional<CitizenEnclosuresDTO> enclosresOptional = citizenEnclosuresDAO
						.findByApplicationNo(applicationNo);
				if (enclosresOptional.isPresent()) {
					CitizenEnclosuresDTO enclosresDto = enclosresOptional.get();
					List<InputVO> map = new ArrayList<InputVO>();
					imageNotUploded = mapStoppageImages(vo, quarterNumber, imageNotUploded, enclosresDto, map);
					if (imageNotUploded) {
						vo.get().getVehicleStoppageDetailsVO().setQrCodeRequired(Boolean.TRUE);
					}
					vo.get().getVehicleStoppageDetailsVO().setCurrentQuarterImageUrls(map);
				} else {
					if (pendingReports != null && !pendingReports.isEmpty()) {
						vo.get().getVehicleStoppageDetailsVO().setQrCodeRequired(Boolean.TRUE);
					}

				}
				vo.get().getVehicleStoppageDetailsVO().setPendingReports(pendingReports);
			} else if (stagingDetailsDTO.getServiceIds().stream()
					.anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId()))
					&& stagingDetailsDTO.getVehicleStoppageDetails() != null) {
				Optional<CitizenEnclosuresDTO> enclosresOptional = null;
				if (StringUtils.isNoneBlank(stagingDetailsDTO.getVehicleStoppageDetails().getStoppageApplicationNo())) {
					enclosresOptional = citizenEnclosuresDAO.findByApplicationNo(
							stagingDetailsDTO.getVehicleStoppageDetails().getStoppageApplicationNo());
				}
				List<InputVO> map = new ArrayList<InputVO>();
				if (enclosresOptional != null && enclosresOptional.isPresent()) {
					CitizenEnclosuresDTO enclosresDto = enclosresOptional.get();

					mapStoppageImages(vo, 0, false, enclosresDto, map);

				}
				Optional<CitizenEnclosuresDTO> revocationImages = citizenEnclosuresDAO
						.findByApplicationNo(applicationNo);
				if (revocationImages != null && revocationImages.isPresent()) {
					this.mapStoppageRevocationImages(revocationImages.get(), map);
					vo.get().getVehicleStoppageDetailsVO().setQrCodeRequired(Boolean.TRUE);
					if (role.equalsIgnoreCase(RoleEnum.RTO.getName())) {
						Double stoppageTax = this.getTax(stagingDetailsDTO.getRegistrationDetails(), stagingDetailsDTO);
						if (stoppageTax > 0) {
							vo.get().setStoppageTax(stoppageTax);
						}
					}
				}
				vo.get().getVehicleStoppageDetailsVO().setCurrentQuarterImageUrls(map);
			}
			if (role.equalsIgnoreCase(RoleEnum.RTO.getName()) || role.equalsIgnoreCase(RoleEnum.DTC.getName())) {
				vo.get().getVehicleStoppageDetailsVO().setQrCodeRequired(Boolean.FALSE);
			}
			List<PermitDetailsDTO> listOfPermits = permitDetails(stagingDetailsDTO.getPrNo());
			if (!listOfPermits.isEmpty()) {
				PermitDetailsDTO listOfPermsasits = listOfPermits.stream().filter(type -> type.getPermitType()
						.getTypeofPermit().equalsIgnoreCase(PermitsEnum.PermitType.PRIMARY.getPermitTypeCode()))
						.findAny().get();
				if (listOfPermsasits != null) {
					vo.get().setPermitDetailsVO(permitDetailsMapper.convertEntity(listOfPermsasits));
				}
			}
			List<FcDetailsDTO> fcDetailsDtoList = fcDetailsDAO
					.findFirst5ByStatusIsTrueAndPrNoOrderByCreatedDateDesc(stagingDetailsDTO.getPrNo());
			if (fcDetailsDtoList != null && !fcDetailsDtoList.isEmpty()) {
				vo.get().setFcDetails(fcDetailsMapper.convertEntity(fcDetailsDtoList.stream().findFirst().get()));
			}
			return vo;
		}
	}

	private boolean mapStoppageImages(Optional<RegServiceVO> vo, int quarterNumber, boolean imageNotUploded,
			CitizenEnclosuresDTO enclosresDto, List<InputVO> map) {
		for (KeyValue<String, List<ImageEnclosureDTO>> key : enclosresDto.getEnclosures()) {
			ImageEnclosureDTO imgage = key.getValue().stream().findFirst().get();
			if (vo.get().getVehicleStoppageDetailsVO().getMviReport() != null
					&& !vo.get().getVehicleStoppageDetailsVO().getMviReport().isEmpty()) {
				for (VehicleStoppageMVIReportVO mviReportVo : vo.get().getVehicleStoppageDetailsVO().getMviReport()) {
					if (imgage.getQuarterNumber() != null
							&& imgage.getQuarterNumber().equals(mviReportVo.getQuarterNumber())) {
						if (mviReportVo.getImageUrls() != null && !mviReportVo.getImageUrls().isEmpty()) {
							// if(mviReportVo.getImageUrls().size()<3) {
							InputVO imgageVo = new InputVO();
							imgageVo.setName(imgage.getEnclosureName());
							imgageVo.setUrl(imagePreUrl + "?appImageDocId=" + imgage.getImageId());
							// approvedUrls.add(imgageVo);
							mviReportVo.getImageUrls().add(imgageVo);
							// }

						} else {
							List<InputVO> approvedUrls = new ArrayList<InputVO>();
							InputVO imgageVo = new InputVO();
							imgageVo.setName(imgage.getEnclosureName());
							imgageVo.setUrl(imagePreUrl + "?appImageDocId=" + imgage.getImageId());
							approvedUrls.add(imgageVo);
							mviReportVo.setImageUrls(approvedUrls);
						}
					}
				}

			}
			if (quarterNumber != 0 && imgage.getQuarterNumber() != null
					&& imgage.getQuarterNumber().equals(quarterNumber)) {
				imageNotUploded = Boolean.FALSE;
				InputVO imgageVo = new InputVO();
				imgageVo.setName(imgage.getEnclosureName());
				imgageVo.setUrl(imagePreUrl + "?appImageDocId=" + imgage.getImageId());
				map.add(imgageVo);
			}

		}
		return imageNotUploded;
	}

	@Override
	public Long daysLeftForAutoapproved(RegServiceDTO stagingDetailsDTO) {
		Optional<PropertiesDTO> daysConfig = propertiesDAO.findByStoppageDaysStatusTrue();
		int totalDays = daysConfig.get().getStoppageMvidays() + daysConfig.get().getStoppageDtcdays();
		LocalDate stoppageDate = stagingDetailsDTO.getCreatedDate().toLocalDate();
		if (stagingDetailsDTO.getVehicleStoppageDetails().getMviAssignedDate() != null) {
			stoppageDate = stagingDetailsDTO.getVehicleStoppageDetails().getMviAssignedDate();
		}
		return ChronoUnit.DAYS.between(LocalDate.now(), stoppageDate.plusDays(totalDays));
	}

	private RegServiceDTO stoppagecommonValidation(JwtUser jwtUser, String applicationNo, String role) {
		RegServiceDTO stagingDetailsDTO = regServiceDAO.findOne(applicationNo);
		if (null == stagingDetailsDTO) {
			logger.error("Application not found, applicationNo:[{}]", applicationNo);
			throw new BadRequestException("Application not found, applicationNo:" + applicationNo);
		}
		if (!(stagingDetailsDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))
				|| stagingDetailsDTO.getServiceIds().stream()
						.anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId())))) {
			logger.error(
					"This application belongs to stoppage service. Please proceed through Fitness link from dashboard.");
			throw new BadRequestException(
					"This application belongs to stoppage service. Please proceed through Fitness link from dashboard.");
		}
		if (role.equalsIgnoreCase(RoleEnum.RTO.getName())
				&& !jwtUser.getOfficeCode().equals(stagingDetailsDTO.getOfficeCode())) {
			logger.error("The application [{}] not related to your office", applicationNo);
			throw new BadRequestException("The application '" + applicationNo + "' not related to your office");
		} else {
			if (role.equalsIgnoreCase(RoleEnum.MVI.getName())
					&& !jwtUser.getOfficeCode().equals(stagingDetailsDTO.getMviOfficeCode())) {
				logger.error("The application [{}] not related to your office", applicationNo);
				throw new BadRequestException("The application '" + applicationNo + "' not related to your office");
			} else if (role.equalsIgnoreCase(RoleEnum.DTC.getName())
					&& !jwtUser.getOfficeCode().equals(stagingDetailsDTO.getDtcOfficeCode())) {
				logger.error("The application [{}] not related to your office", applicationNo);
				throw new BadRequestException("The application '" + applicationNo + "' not related to your office");
			}
		}
		if (stagingDetailsDTO.getCurrentRoles().contains(RoleEnum.RTO.getName())
				&& !role.equalsIgnoreCase(RoleEnum.RTO.getName())) {
			logger.error("The application [{}] not completed at RTO level", applicationNo);
			throw new BadRequestException("The application '" + applicationNo + "' not completed at RTO level");
		}
		if (stagingDetailsDTO.getApplicationStatus().equals(StatusRegistration.APPROVED)
				|| stagingDetailsDTO.getApplicationStatus().equals(StatusRegistration.REJECTED)) {
			logger.error("The application [{}] process completed and application status is: ", applicationNo,
					stagingDetailsDTO.getApplicationStatus());
			throw new BadRequestException("The application '" + applicationNo
					+ "' process completed and application status is:" + stagingDetailsDTO.getApplicationStatus());
		}
		if ((role.equalsIgnoreCase(RoleEnum.MVI.getName()) && !stagingDetailsDTO.getLockedDetails().stream()
				.anyMatch(one -> one.getLockedBy().equalsIgnoreCase(jwtUser.getUsername())))) {
			logger.error("Unauthorized user of application No [{}]", stagingDetailsDTO.getApplicationNo());
			throw new BadRequestException("Unauthorized User");
		}
		return stagingDetailsDTO;
	}

	private Pair<LocalDate, LocalDate> quarterStartAndEndDate(LocalDate date) {
		Pair<Integer, Integer> indexPosiAndQuarterNo = citizenTaxService.getMonthposition(date);
		LocalDate quaterstarts = citizenTaxService.getQuarterStatrtDate(indexPosiAndQuarterNo.getFirst(),
				indexPosiAndQuarterNo.getSecond(), date);
		LocalDate quaterEnds = citizenTaxService.calculateTaxUpTo(indexPosiAndQuarterNo.getFirst(),
				indexPosiAndQuarterNo.getSecond());
		return Pair.of(quaterstarts, quaterEnds);

	}

	@Override
	public List<VehicleStoppageMVIReportVO> getPendingQuarters(RegServiceDTO stagingDetailsDTO) {
		LocalDate date = null;
		int quarterNo = 0;
		Pair<LocalDate, LocalDate> quaterstartsAndEndDate = quarterStartAndEndDate(LocalDate.now());
		if (stagingDetailsDTO.getVehicleStoppageDetails().getMviReport() != null
				&& !stagingDetailsDTO.getVehicleStoppageDetails().getMviReport().isEmpty()) {

			List<VehicleStoppageMVIReportDTO> mviReport = stagingDetailsDTO.getVehicleStoppageDetails().getMviReport();
			mviReport.sort((p1, p2) -> p2.getQuarterEndDate().compareTo(p1.getQuarterEndDate()));
			VehicleStoppageMVIReportDTO reportDto = mviReport.stream().findFirst().get();

			if (!(reportDto.getQuarterEndDate().equals(quaterstartsAndEndDate.getSecond())
					|| reportDto.getQuarterEndDate().isAfter(quaterstartsAndEndDate.getSecond()))) {
				date = reportDto.getQuarterEndDate().plusDays(1);
				quarterNo = reportDto.getQuarterNumber();
			}
		} else {
			date = stagingDetailsDTO.getCreatedDate().toLocalDate();
			quarterNo = 0;
		}
		if (date != null) {
			List<VehicleStoppageMVIReportVO> pendingReports = new ArrayList<>();
			LocalDate quaterEnds = LocalDate.now();
			do {
				VehicleStoppageMVIReportVO report = new VehicleStoppageMVIReportVO();
				Pair<Integer, Integer> indexPosiAndQuarterNo = citizenTaxService.getMonthposition(date);
				LocalDate quaterstarts = citizenTaxService.getQuarterStatrtDate(indexPosiAndQuarterNo.getFirst(),
						indexPosiAndQuarterNo.getSecond(), date);
				quaterEnds = citizenTaxService.calculateChassisTaxUpTo(indexPosiAndQuarterNo.getFirst(),
						indexPosiAndQuarterNo.getSecond(), date);
				if (quarterNo == 0) {
					quaterstarts = date;
				}
				report.setQuarterStartDate(quaterstarts);
				report.setQuarterEndDate(quaterEnds);
				date = quaterEnds.plusDays(2);
				report.setQuarterNumber(++quarterNo);
				pendingReports.add(report);
			} while (!(quaterEnds.equals(quaterstartsAndEndDate.getSecond())
					|| quaterEnds.isAfter(quaterstartsAndEndDate.getSecond())));
			return pendingReports;
		}

		return null;

	}

	private void chassisNoAndEngineNoValidation(String chassisNo, String engineNo, String appNo, String prNo) {
		try {
			logger.info("Start other state chassisNo and engineNo verification at mvi level ");
			List<RegistrationDetailsDTO> regDetails = registrationDetailDAO
					.findByVahanDetailsChassisNumberOrVahanDetailsEngineNumber(chassisNo, engineNo);
			if (CollectionUtils.isNotEmpty(regDetails)) {
				for (RegistrationDetailsDTO reg : regDetails) {
					if (reg.getOfficeDetails() != null && reg.getOfficeDetails().getOfficeCode() != null) {
						String officeCode = reg.getOfficeDetails().getOfficeCode();
						Optional<OfficeDTO> office = officeDAO.findByOfficeCodeAndIsActiveTrue(officeCode);
						if (office.isPresent()) {
							if (!(prNo != null && reg.getPrNo() != null && prNo.equals(reg.getPrNo()))) {
								throw new BadRequestException("Vahan Details Already exist with Application No:" + "["
										+ reg.getApplicationNo() + "]");
							}

						}
					}
				}

				/*
				 * throw new
				 * BadRequestException("Vahan Details Already exist with Application No:" + "["
				 * + regDetails.get().getApplicationNo() + "]");
				 */
			}
			List<StatusRegistration> applicationStatus = new ArrayList<>();
			applicationStatus.add(StatusRegistration.OTHERSTATEPAYMENTPENDING);
			applicationStatus.add(StatusRegistration.PAYMENTDONE);
			applicationStatus.add(StatusRegistration.CITIZENSUBMITTED);
			applicationStatus.add(StatusRegistration.APPROVED);
			Integer serviceId = ServiceEnum.DATAENTRY.getId();
			List<RegServiceDTO> regSerList = regServiceDAO.findByServiceIdsAndApplicationStatusIn(serviceId,
					applicationStatus);
			for (RegServiceDTO regSer : regSerList) {
				if (regSer.getRegistrationDetails() != null && (regSer.getRegistrationDetails().getVahanDetails()
						.getChassisNumber().equals(chassisNo)
						|| regSer.getRegistrationDetails().getVahanDetails().getEngineNumber().equals(engineNo))) {
					if (!appNo.equals(regSer.getApplicationNo())) {
						if (!(prNo != null && regSer.getRegistrationDetails() != null
								&& regSer.getRegistrationDetails().getPrNo() != null
								&& prNo.equals(regSer.getRegistrationDetails().getPrNo()))) {
							throw new BadRequestException("Vahan Details Already exist with Application No:" + "["
									+ regSer.getApplicationNo() + "]");
						}
					}

				}
			}
		} catch (Exception e) {
			logger.error(" other state chassisNo and engineNo verification at mvi level " + e.getMessage());
			throw new BadRequestException(e.getMessage());
		}
	}

	private LocalDate getEarlerDate(LocalDate dateOfEnter, LocalDate nocIssueDate) {
		LocalDate entryDate;
		if (dateOfEnter.isAfter(nocIssueDate)) {
			entryDate = dateOfEnter;

		} else {
			entryDate = nocIssueDate;
		}
		return entryDate;
	}

	private void updateTaxDetailsForDataEntryTSvehicle(RegServiceDTO regServiceDTO) {
		// TODO Auto-generated method stub
		if (StringUtils.isNoneBlank(regServiceDTO.getPrNo())) {
			List<TaxDetailsDTO> taxdetailsCreateDste = null;
			List<TaxDetailsDTO> taxdetails = taxDetailsDAO
					.findFirst10ByPrNoOrderByCreatedDateDesc(regServiceDTO.getPrNo());
			if (!taxdetails.isEmpty()) {

				Comparator<TaxDetailsDTO> createDateComparator = (p1, p2) -> p1.getCreatedDate()
						.compareTo(p2.getCreatedDate());
				taxdetailsCreateDste = taxdetails.stream().filter(taxDate -> taxDate.getCreatedDate() != null)
						.sorted(createDateComparator).collect(Collectors.toList());
				if (taxdetailsCreateDste.isEmpty()) {
					Comparator<TaxDetailsDTO> taxPaidDate = (p1, p2) -> p1.getTaxPaidDate()
							.compareTo(p2.getTaxPaidDate());
					taxdetails = taxdetails.stream().filter(taxDate -> taxDate.getTaxPaidDate() != null)
							.sorted(taxPaidDate).collect(Collectors.toList());
				} else {
					taxdetails = taxdetailsCreateDste;
				}
				TaxDetailsDTO taxDTO = taxdetails.stream().findFirst().get();
				taxDTO.setApplicationNo(regServiceDTO.getApplicationNo());
				taxDTO.setStateCode("TS");
				taxDetailsDAO.save(taxDTO);
			}
		}

	}

	@Override
	public void updateApplicationNOInTaxDetails() {
		logger.debug("Start other state TS Application Number update in taxDetails ");
		List<Integer> serviceIds = new ArrayList<>();
		List<StatusRegistration> applicationStatus = new ArrayList<>();
		serviceIds.add(ServiceEnum.DATAENTRY.getId());
		applicationStatus.add(StatusRegistration.PRGENERATED);
		applicationStatus.add(StatusRegistration.APPROVED);
		List<RegServiceDTO> listofOtherStateApplications = regServiceDAO
				.findByServiceIdsAndApplicationStatusIn(ServiceEnum.DATAENTRY.getId(), applicationStatus);
		for (RegServiceDTO regDTO : listofOtherStateApplications) { // regDTO.getPrNo();
			if (StringUtils.isNoneBlank(regDTO.getPrNo())) {
				try {
					Optional<RegistrationDetailsDTO> regDetails = registrationDetailDAO.findByPrNo(regDTO.getPrNo());
					if (regDetails.isPresent()) {
						Optional<TaxDetailsDTO> taxData = taxDetailsDAO
								.findByApplicationNoOrderByCreatedDateDesc(regDTO.getApplicationNo());
						if (!taxData.isPresent()) {
							Optional<PropertiesDTO> propertiesDto = propertiesDAO.findByStatusTrue();
							if (!propertiesDto.isPresent()) {
								logger.error("Properties details not found, Please contact to RTA admin");
								throw new BadRequestException(
										"Properties details not found, Please contact to RTA admin");
							}
							PropertiesDTO propertiesDTO = propertiesDto.get();
							if ((StringUtils.isNoneBlank(regDTO.getRegistrationDetails().getPrNo())
									&& regDTO.getnOCDetails() != null
									&& regDTO.getnOCDetails().getState().equals(propertiesDTO.getStateName())
									&& regDTO.getRegistrationDetails().getPrIssueDate().isBefore(
											DateConverters.convertStirngTOlocalDate(propertiesDTO.getRegDate())))) {
								logger.info("other state TS Application Number:--- " + regDTO.getApplicationNo());
								this.updateTaxDetailsForDataEntryTSvehicle(regDTO);
							} else { // regDTO.getRegistrationDetails().setFromReassigment(true);
								regDTO.getRegistrationDetails().setAllowForReassignment(true);
								regServiceDAO.save(regDTO);
							}
						}
					}
					continue;
				} catch (Exception e) {
					logger.debug("Exception [{}]", e);
					logger.error("Exception [{}]", e.getMessage());
				}
			}

			logger.debug("End other state TS Application Number update in taxDetails");
		}
	}

	@Override
	public RegServiceDTO saveBilateralTaxDetails(String userId, RtaActionVO actionVo, Optional<UserDTO> userDetails,
			String role) throws Exception {
		String applicationNo = actionVo.getApplicationNo();
		RegServiceDTO staginDto = null;
		MasterBileteralTaxStatesConfig cofigDto = null;
		synchronized (applicationNo.intern()) {
			Optional<RegServiceDTO> stagingDto = regServiceDAO.findByApplicationNo(actionVo.getApplicationNo());
			if (!stagingDto.isPresent()) {
				throw new BadRequestException("Application Not Found.");
			}
			staginDto = stagingDto.get();
			if (!(staginDto.getApplicationStatus().equals(StatusRegistration.PAYMENTDONE)
					|| staginDto.getApplicationStatus().equals(StatusRegistration.REUPLOAD))) {
				throw new BadRequestException("Invalid application status for: " + actionVo.getApplicationNo());
			}
			Optional<MasterBileteralTaxStatesConfig> optionalMasterDto = masterBileteralTaxStatesConfigDAO
					.findByStateName(staginDto.getBileteralTaxDetails().getPermitIssuedBy());
			if (!optionalMasterDto.isPresent()) {
				logger.error("No master state config data for sate: ",
						staginDto.getBileteralTaxDetails().getPermitIssuedBy());
				throw new BadRequestException("No master state config data for sate: "
						+ staginDto.getBileteralTaxDetails().getPermitIssuedBy());
			}
			if (staginDto.getBileteralTaxDetails().getPurpose().equalsIgnoreCase("FRESH")) {
				cofigDto = optionalMasterDto.get();
				List<Integer> quaterFour = new ArrayList<>();
				quaterFour.add(0, 1);
				quaterFour.add(1, 2);
				quaterFour.add(2, 3);

				int year = LocalDate.now().getYear();
				if (!quaterFour.contains(LocalDate.now().getMonthValue())) {
					year = LocalDate.now().plusYears(1).getYear();

				}
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
				String date1 = cofigDto.getCutOffDateAndMonth() + "-" + String.valueOf(year);
				LocalDate yearEndDate = LocalDate.parse(date1, formatter);
				if (!(yearEndDate.equals(cofigDto.getYearEndDate()))) {
					updateMasterStateData(cofigDto, Boolean.TRUE, yearEndDate);
				} else {
					updateMasterStateData(cofigDto, Boolean.FALSE, yearEndDate);
				}

			}
			Optional<CitizenEnclosuresDTO> citizenEnclosuresOpt = citizenEnclosuresDAO
					.findByApplicationNoAndServiceIdsIn(actionVo.getApplicationNo(), staginDto.getServiceIds());
			if (!citizenEnclosuresOpt.isPresent()) {
				throw new BadRequestException("Enclosures not found for: " + actionVo.getApplicationNo());
			}
			StatusRegistration applicationStatus = updateEnclosures(role, actionVo.getEnclosures(),
					citizenEnclosuresOpt.get());
			if (staginDto.getActionDetails() == null || staginDto.getActionDetails().isEmpty()) {
				ActionDetails actionDetails = new ActionDetails();
				actionDetails.setUserId(userId);
				actionDetails.setRole(role);
				actionDetails.setStatus(applicationStatus.toString());
				actionDetails.setCreatedDate(LocalDateTime.now());
				staginDto.setActionDetails(Arrays.asList(actionDetails));
			} else {
				ActionDetails actionDetails = new ActionDetails();
				actionDetails.setUserId(userId);
				actionDetails.setRole(role);
				actionDetails.setStatus(applicationStatus.toString());
				actionDetails.setCreatedDate(LocalDateTime.now());
				staginDto.getActionDetails().add(actionDetails);
			}
			staginDto.setlUpdate(LocalDateTime.now());
			if (applicationStatus.equals(StatusRegistration.REJECTED)) {
				staginDto.setApplicationStatus(StatusRegistration.REJECTED);
			} else {
				staginDto.setApplicationStatus(StatusRegistration.APPROVED);
				BileteralTaxDTO dto = staginDto.getBileteralTaxDetails();
				dto.setStatus(Boolean.TRUE);
				dto.setApplicationNo(staginDto.getApplicationNo());
				dto.setApprovedBy(userId);
				dto.setApprovedDate(LocalDateTime.now());
				dto.setOfficeCode(staginDto.getOfficeCode());
				List<PaymentTransactionDTO> paymentTransactionDTOList = paymentTransactionDAO
						.findByPayStatusAndApplicationFormRefNum(PayStatusEnum.SUCCESS.getDescription(),
								staginDto.getApplicationNo());
				if (paymentTransactionDTOList == null || paymentTransactionDTOList.isEmpty()) {
					throw new BadRequestException("payment details not found for: " + staginDto.getApplicationNo());
				}

				PaymentTransactionDTO payments = paymentTransactionDTOList.stream().findFirst().get();
				for (FeesDTO fee : payments.getFeeDetailsDTO().getFeeDetails()) {
					if (fee.getFeesType().equalsIgnoreCase(ServiceCodeEnum.REGISTRATION.getCode())) {
						dto.setApplicationFee(fee.getAmount());
					}
					if (fee.getFeesType().equalsIgnoreCase(ServiceCodeEnum.SERVICE_FEE.getCode())) {
						dto.setServiceFee(fee.getAmount());
					}
					if (fee.getFeesType().equalsIgnoreCase(ServiceCodeEnum.QLY_TAX.getCode())) {
						dto.setTax(fee.getAmount());
					}
					if (fee.getFeesType().equalsIgnoreCase(ServiceCodeEnum.PENALTY.getCode())) {
						dto.setPenality(fee.getAmount());
					}
				}
				LocalDate taxValidityTo = citizenTaxService.getBilaterTaxUpTo(payments.getCreatedDate().toLocalDate());
				dto.setValidityTo(taxValidityTo);
				Optional<BileteralTaxDTO> optionalDto = bileteralTaxDAO.findByPrNoAndStatusIsTrue(dto.getPrNo());
				if (!optionalDto.isPresent() && !dto.getPurpose().equalsIgnoreCase("FRESH")) {
					throw new BadRequestException(
							"Old bilateral tax details not found for pr number: " + dto.getPrNo());
				}
				if (optionalDto.isPresent()) {
					BileteralTaxDTO oldDetails = optionalDto.get();
					oldDetails.setStatus(Boolean.FALSE);
					bileteralTaxDAO.save(oldDetails);
				}
				if (dto.getPurpose().equalsIgnoreCase("FRESH")) {
					// TODO need to generate CS number
					Optional<StateDTO> optionalState = stateDAO.findByStateName(dto.getPermitIssuedBy());
					if (!optionalState.isPresent()) {
						throw new BadRequestException("Master sate not found for : " + dto.getPermitIssuedBy());
					}
					String cspPuNo = generateBilateralPermitNo(staginDto, optionalState.get().getStateId(), "CS", "GV");
					dto.setCspPuNo(cspPuNo);
				}
				bileteralTaxDAO.save(dto);
			}
			citizenEnclosuresDAO.save(citizenEnclosuresOpt.get());
			regServiceDAO.save(staginDto);
			if (cofigDto != null) {
				masterBileteralTaxStatesConfigDAO.save(cofigDto);
			}
		}
		return staginDto;
	}

	private void updateMasterStateData(MasterBileteralTaxStatesConfig cofigDto, boolean resetFlag,
			LocalDate yearEndDate) {

		if (resetFlag) {
			Map<String, Integer> log = new HashMap<>();

			String stringYear = String.valueOf(cofigDto.getYearEndDate().getYear() - 1) + "-"
					+ String.valueOf(cofigDto.getYearEndDate().getYear());
			log.put(stringYear, cofigDto.getUsedCount());
			if (cofigDto.getLog() == null) {
				cofigDto.setLog(log);
			} else {
				cofigDto.getLog().put(stringYear, cofigDto.getUsedCount());
			}

			cofigDto.setAvailableCount(cofigDto.getTotalCount() - 1);
			cofigDto.setUsedCount(1);
			cofigDto.setYearEndDate(yearEndDate);
		} else {
			if (cofigDto.getAvailableCount() == 0) {
				logger.error("Maximum permits reached for state: ", cofigDto.getStateName());
				throw new BadRequestException("Maximum permits reached for state: " + cofigDto.getStateName());
			}
			cofigDto.setAvailableCount(cofigDto.getAvailableCount() - 1);
			cofigDto.setUsedCount(cofigDto.getUsedCount() + 1);
			// cofigDto.setResetFlag(Boolean.FALSE);
		}
	}

	private String generateBilateralPermitNo(RegServiceDTO regServiceDTO, String stateCode, String permitCode,
			String covCode) {
		String permitNumber;
		Map<String, String> detail = new HashMap<>();
		detail.put("stateCode", stateCode);
		detail.put("permitCode", permitCode);
		detail.put("covCode", covCode);
		detail.put("officeCode", regServiceDTO.getOfficeCode());

		permitNumber = sequenceGenerator.getSequence(Sequence.BILATERALTAX.getSequenceId().toString(), detail);
		return permitNumber;
	}

	private void removingRecordfromRepresentative(String aadhaarNo, String applicationId) {
		if (!aadhaarNo.isEmpty()) {
			List<ApplicantDetailsDTO> applicantList = applicantDetailsDAO
					.findByAadharResponseUidAndUidTokenNotNull(Long.parseLong(aadhaarNo));
			if (CollectionUtils.isNotEmpty(applicantList)) {
				applicantList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
				RepresentativeDTO represDto = representativeDAO
						.findOne(applicantList.get(0).getAadharResponse().getUidToken());
				if (represDto != null && represDto.getApplicationIds() != null) {
					represDto.getApplicationIds().removeIf(c -> c.equals(applicationId));
					representativeDAO.save(represDto);
				}
			}
		}
	}

	private void updateCovsinLog(RegistrationDetailsDTO dto, RegServiceDTO regServiceDTO) {

		if (dto.getCovHistory() != null && !dto.getCovHistory().isEmpty()) {
			/*
			 * for(ClassOfVehiclesLogDTO covDto : dto.getCovDetails()) {
			 * if(covDto.isCurrentcov()) {
			 * covDto.setToCov(regServiceDTO.getAlterationDetails().getCov());
			 * covDto.setTo(LocalDateTime.now()); covDto.setCurrentcov(Boolean.FALSE); } }
			 */
			ClassOfVehiclesLogDTO covDto = dto.getCovHistory().get(dto.getCovHistory().size() - 1);
			covDto.setToCov(regServiceDTO.getAlterationDetails().getCov());
			covDto.setTo(LocalDateTime.now());
			covDto.setCurrentcov(Boolean.FALSE);
			ClassOfVehiclesLogDTO covLog = new ClassOfVehiclesLogDTO();
			covLog.setFromCov(regServiceDTO.getAlterationDetails().getCov());
			covLog.setFrom(LocalDateTime.now());
			covLog.setCurrentcov(Boolean.TRUE);
			covLog.setCurrentcovNo(dto.getCovHistory().size() + 1);
			dto.getCovHistory().add(covLog);
		} else {
			ClassOfVehiclesLogDTO covLog = new ClassOfVehiclesLogDTO();
			covLog.setFromCov(regServiceDTO.getAlterationDetails().getCov());
			covLog.setFrom(LocalDateTime.now());
			covLog.setCurrentcov(Boolean.TRUE);
			covLog.setCurrentcovNo(1);
			dto.setCovHistory(Arrays.asList(covLog));
		}
	}

	private void mviFitnessApprovedlog(RtaActionVO actionVo, String role, UserDTO userDTO,
			RegServiceDTO regServiceDTO) {
		// TODO Auto-generated method stub
		FitnessApprovedlogs fclogs = new FitnessApprovedlogs();
		fclogs.setPrNo(actionVo.getPrNo());
		fclogs.setPrNo(regServiceDTO.getRegistrationDetails().getPrNo());
		fclogs.setRoleType(role);
		fclogs.setApprovedDate(LocalDate.now());
		fclogs.setUserName(userDTO.getUserId());
		fclogs.setAadharResponseMVI(actionVo.getAadharResponseMVI());
		fitnessLogsDAO.save(fclogs);
	}

	@Override
	public RegServiceVO getVehicleDetails(String prNo, JwtUser jwtUser, String role) {
		// TODO Auto-generated method stub
		Optional<UserDTO> userDetails = userDAO.findByUserId(jwtUser.getId());
		if (!userDetails.isPresent()) {
			logger.error("user details not found. [{}] ", jwtUser.getId());
			throw new BadRequestException("No record found. " + jwtUser.getId());
		}
		Optional<RegistrationDetailsDTO> regDTO = registrationDetailDAO.findByPrNo(prNo);
		if (!regDTO.isPresent()) {
			logger.error("No record found. [{}],[{}] ", prNo);
			throw new BadRequestException("No record found.Pr no: " + prNo);
		}
		String roles = dTOUtilService.getRole(jwtUser.getId(), role);
		validatiformvi(regDTO, roles);
		List<FcDetailsDTO> listOfFc = fcDetailsDAO
				.findFirst5ByStatusIsTrueAndPrNoOrderByCreatedDateDesc(regDTO.get().getPrNo());
		if (listOfFc.isEmpty()) {
			logger.error("NO FITNESS CERTIFICATE DETAILS NOT FOUND");
			throw new BadRequestException("NO FITNESS CERTIFICATE DETAILS NOT FOUND");
		}
		if (regDTO.get().isCfxIssued()) {
			logger.error("Already CFX issued on this PR [{}]", prNo);
			throw new BadRequestException("Already CFX issued on this PR. " + prNo);
		}
		List<String> taxTypes = new ArrayList<>();
		taxTypes.add(ServiceCodeEnum.QLY_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.HALF_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.YEAR_TAX.getCode());
		taxTypes.add(ServiceCodeEnum.LIFE_TAX.getCode());
		List<TaxDetailsDTO> taxDetailsList = taxDetailsDAO
				.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(regDTO.get().getApplicationNo(),
						taxTypes);

		if (taxDetailsList.isEmpty()) {
			logger.error("TaxDetails not found: [{}]", regDTO.get().getApplicationNo());
			throw new BadRequestException("TaxDetails not found for RegistrationNo:" + regDTO.get().getPrNo());
		}
		registrationService.updatePaidDateAsCreatedDate(taxDetailsList);
		taxDetailsList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		TaxDetailsDTO taxDto = taxDetailsList.stream().findFirst().get();
		RegServiceVO vo = new RegServiceVO();
		RegistrationDetailsVO regvo = registrationDetailsMapper.convertEntity(regDTO.get());
		if (taxDto.getTaxPeriodEnd() != null) {
			if (regvo.getRegistrationValidity() != null && regvo.getRegistrationValidity().getTaxValidity() != null) {
				regvo.getRegistrationValidity().setTaxValidity(taxDto.getTaxPeriodEnd());
			}
		}
		vo.setRegistrationDetails(regvo);
		taxDetailsList.clear();
		return vo;
	}

	private void saveChassisTax(RegServiceDTO regServiceDTO) {
		Optional<VoluntaryTaxDTO> voluntaryTax = null;
		if (regServiceDTO.getRegistrationDetails().isRegVehicleWithPR()) {
			voluntaryTax = voluntaryTaxDAO.findByTrNoOrderByCreatedDateDesc(regServiceDTO.getPrNo());
		} else if (regServiceDTO.getRegistrationDetails().isRegVehicleWithTR()) {
			voluntaryTax = voluntaryTaxDAO
					.findByTrNoOrderByCreatedDateDesc(regServiceDTO.getRegistrationDetails().getTrNo());
		}

		if (voluntaryTax != null && voluntaryTax.isPresent()) {
			if (voluntaryTax.get().getTax() != null) {
				regServiceDTO.setTaxAmount(voluntaryTax.get().getTax().longValue());
			}

			regServiceDTO.setPenalty(voluntaryTax.get().getPenalty());
			if (voluntaryTax.get().getTaxArrears() != null) {
				regServiceDTO.setTaxArrears(voluntaryTax.get().getTaxArrears().longValue());
			}
			if (voluntaryTax.get().getPenaltyArrears() != null) {
				regServiceDTO.setPenaltyArrears(voluntaryTax.get().getPenaltyArrears().longValue());
			}

			regServiceDTO.setTaxvalidity(voluntaryTax.get().getTaxvalidUpto());
			RegServiceVO vo = regServiceMapper.convertEntity(regServiceDTO);
			TaxDetailsDTO taxDetailsDTO = this.saveCitizenTaxDetails(vo, Boolean.FALSE, Boolean.FALSE, "OS");
			taxDetailsDAO.save(taxDetailsDTO);
		}
	}

	public TaxDetailsDTO saveCitizenTaxDetails(RegServiceVO regServiceDTO, boolean secoundVehicleDiffTaxPaid,
			boolean isChassisVehicle, String stateCode) {
		// need to update pr and second vehicel tax and flag
		TaxDetailsDTO dto = new TaxDetailsDTO();
		if (regServiceDTO.getTaxAmount() != null && regServiceDTO.getTaxAmount() != 0
				|| regServiceDTO.getCesFee() != null && regServiceDTO.getCesFee() != 0
				|| regServiceDTO.getGreenTaxAmount() != null && regServiceDTO.getGreenTaxAmount() != 0) {

			List<Map<String, TaxComponentDTO>> taxDetails = new ArrayList<>();

			dto.setApplicationNo(regServiceDTO.getRegistrationDetails().getApplicationNo());
			dto.setCovCategory(regServiceDTO.getRegistrationDetails().getOwnerType());
			dto.setModule(ModuleEnum.REG.getCode());
			dto.setPaymentPeriod(regServiceDTO.getTaxType());

			dto.setTrNo(regServiceDTO.getRegistrationDetails().getTrNo());
			dto.setPrNo(regServiceDTO.getRegistrationDetails().getPrNo());

			dto.setTaxPeriodFrom(LocalDate.now());
			if (regServiceDTO.getPayTaxType() != null) {
				dto.setPayTaxType(regServiceDTO.getPayTaxType());
			}
			// Tax
			// TODO : Change to Enum
			if (regServiceDTO.getTaxAmount() != null && regServiceDTO.getTaxAmount() != 0) {
				if (regServiceDTO.getQuaterTaxForNewGo() != null) {
					dto.setTaxAmount(regServiceDTO.getTaxAmount());
					dto.setTaxPeriodEnd(regServiceDTO.getTaxvalidity());
					Double taxArrears = 0d;
					Long penalty = 0l;
					Long penaltyArrears = 0l;
					if (regServiceDTO.getTaxArrears() != null)
						taxArrears = Double.valueOf(regServiceDTO.getTaxArrears().toString());
					if (regServiceDTO.getPenalty() != null)
						penalty = regServiceDTO.getPenalty();
					if (regServiceDTO.getPenaltyArrears() != null)
						penaltyArrears = regServiceDTO.getPenaltyArrears();
					this.addTaxDetails(TaxTypeEnum.QuarterlyTax.getDesc(),
							Double.valueOf(regServiceDTO.getQuaterTaxForNewGo().toString()), LocalDateTime.now(),
							citizenTaxService.validity(TaxTypeEnum.QuarterlyTax.getDesc()), LocalDate.now(), taxDetails,
							penalty, taxArrears, penaltyArrears);
					this.addTaxDetails(regServiceDTO.getTaxType(),
							Double.valueOf(regServiceDTO.getTaxAmount().toString()), LocalDateTime.now(),
							regServiceDTO.getTaxvalidity(), LocalDate.now(), taxDetails, 0l, 0d, 0l);

				} else {
					dto.setTaxAmount(regServiceDTO.getTaxAmount());
					dto.setTaxPeriodEnd(regServiceDTO.getTaxvalidity());
					Double taxArrears = 0d;
					Long penalty = 0l;
					Long penaltyArrears = 0l;
					if (regServiceDTO.getTaxArrears() != null)
						taxArrears = Double.valueOf(regServiceDTO.getTaxArrears().toString());
					if (regServiceDTO.getPenalty() != null)
						penalty = regServiceDTO.getPenalty();
					if (regServiceDTO.getPenaltyArrears() != null)
						penaltyArrears = regServiceDTO.getPenaltyArrears();
					this.addTaxDetails(regServiceDTO.getTaxType(),
							Double.valueOf(regServiceDTO.getTaxAmount().toString()), LocalDateTime.now(),
							regServiceDTO.getTaxvalidity(), LocalDate.now(), taxDetails, penalty, taxArrears,
							penaltyArrears);
				}

			}

			if (StringUtils.isNoneBlank(regServiceDTO.getPermitCode())) {
				dto.setPermitType(regServiceDTO.getPermitCode());
			} else {
				dto.setPermitType("INA");
			}

			if (regServiceDTO.getCesFee() != null && regServiceDTO.getCesFee() != 0) {

				dto.setCessFee(regServiceDTO.getCesFee());
				dto.setCessPeriodEnd(regServiceDTO.getCesValidity());
				dto.setCessPeriodFrom(LocalDate.now());

				this.addTaxDetails(ServiceCodeEnum.CESS_FEE.getCode(),
						Double.valueOf(regServiceDTO.getCesFee().toString()), LocalDateTime.now(),
						regServiceDTO.getCesValidity(), LocalDate.now(), taxDetails, null, null, null);
			}
			if (regServiceDTO.getGreenTaxAmount() != null && regServiceDTO.getGreenTaxAmount() != 0) {

				dto.setGreenTaxAmount(regServiceDTO.getGreenTaxAmount());
				dto.setGreenTaxPeriodEnd(regServiceDTO.getGreenTaxvalidity());

				this.addTaxDetails(ServiceCodeEnum.GREEN_TAX.getCode(),
						Double.valueOf(regServiceDTO.getGreenTaxAmount().toString()), LocalDateTime.now(),
						regServiceDTO.getGreenTaxvalidity(), LocalDate.now(), taxDetails, null, null, null);
			}
			dto.setCreatedDate(LocalDateTime.now());
			dto.setTaxPaidDate(LocalDate.now());
			if (regServiceDTO.getServiceType().stream()
					.anyMatch(service -> service.equals(ServiceEnum.ALTERATIONOFVEHICLE))) {
				/*
				 * Optional<AlterationDTO> alterDetails = alterationDao
				 * .findByApplicationNo(regServiceDTO.getRegistrationDetails().
				 * getApplicationNo( )); if (!alterDetails.isPresent()) { throw new
				 * BadRequestException("No record found in alteration for: " +
				 * regServiceDTO.getRegistrationDetails().getApplicationNo()); }
				 */

				dto.setClassOfVehicle(
						regServiceDTO.getAlterationVO().getCov() != null ? regServiceDTO.getAlterationVO().getCov()
								: regServiceDTO.getRegistrationDetails().getClassOfVehicle());
			} else {
				dto.setClassOfVehicle(regServiceDTO.getRegistrationDetails().getClassOfVehicle());
			}
			dto.setTaxDetails(taxDetails);

			if (regServiceDTO.getRegistrationDetails().isSecondVehicleTaxPaid() && secoundVehicleDiffTaxPaid
					&& (!regServiceDTO.getRegistrationDetails().getIsFirstVehicle())) {
				dto.setSecondVehicleDiffTaxPaid(Boolean.TRUE);
			} else if (regServiceDTO.getRegistrationDetails().isSecondVehicleTaxPaid()
					&& (regServiceDTO.getRegistrationDetails().getIsFirstVehicle() != null
							&& !regServiceDTO.getRegistrationDetails().getIsFirstVehicle())) {
				dto.setSecondVehicleTaxPaid(Boolean.TRUE);
			}
			if (regServiceDTO.getRegistrationDetails().getInvoiceDetails() != null
					&& regServiceDTO.getRegistrationDetails().getInvoiceDetails().getInvoiceValue() != null) {
				dto.setInvoiceValue(regServiceDTO.getRegistrationDetails().getInvoiceDetails().getInvoiceValue());
			}

			dto.setTaxStatus(TaxStatusEnum.ACTIVE.getCode());
			dto.setRemarks("");
			if (regServiceDTO.getRegistrationDetails() != null
					&& regServiceDTO.getRegistrationDetails().getOfficeDetails() != null && StringUtils
							.isNoneBlank(regServiceDTO.getRegistrationDetails().getOfficeDetails().getOfficeCode())) {
				dto.setOfficeCode(regServiceDTO.getRegistrationDetails().getOfficeDetails().getOfficeCode());
			} else {
				if (StringUtils.isNoneBlank(regServiceDTO.getOfficeCode())) {
					dto.setOfficeCode(regServiceDTO.getOfficeCode());
				}
			}

			dto.setStateCode(stateCode);
			if (regServiceDTO.getRegistrationDetails() != null
					&& regServiceDTO.getRegistrationDetails().getVahanDetails() != null && StringUtils
							.isNoneBlank(regServiceDTO.getRegistrationDetails().getVahanDetails().getChassisNumber())) {
				dto.setChassisNo(regServiceDTO.getRegistrationDetails().getVahanDetails().getChassisNumber());
			}
			if (regServiceDTO.getServiceIds() != null && !regServiceDTO.getServiceIds().isEmpty()
					&& regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VCR.getId()))) {
				dto.setTaxPaidThroughVcr(Boolean.TRUE);
			}
			taxDetailsDAO.save(dto);
		}
		return dto;
	}

	private RegistrationDetailsDTO updateFreshRc(RegServiceDTO regServiceDTO,
			RegistrationDetailsDTO registrationDetailsDTO) {
		RegistrationDetailsLogDTO regLogDetails = new RegistrationDetailsLogDTO();
		setFreshMviAOAction(regServiceDTO);
		if (regServiceDTO.isMviDone() && regServiceDTO.getFlowId() != null) {
			Optional<UserDTO> userDtoOpt = userDAO.findByUserId(regServiceDTO.getFreshRcdetails().getFinancerUserId());
			if (!userDtoOpt.isPresent()) {
				throw new BadRequestException("Master user details not found with prno : " + regServiceDTO.getPrNo());
			}
			freshRCMapper.convertRegDetailsToLog(registrationDetailsDTO, regLogDetails);
			regLog.save(regLogDetails);
			freshRCMapper.convertDataOfFinancierToReg(registrationDetailsDTO, userDtoOpt);
		}
		return registrationDetailsDTO;
	}

	private void setMviLockedDetailsFreshRC(RegServiceDTO regServiceDTO) {
		LockedDetailsDTO lockedDetail = rtaService.setLockedDetails(regServiceDTO.getFreshRcdetails().getMviUserId(),
				RoleEnum.MVI.name(), regServiceDTO.getIterationCount(),
				regServiceDTO.getRegistrationDetails().getVehicleType(), regServiceDTO.getApplicationNo());
		regServiceDTO.setLockedDetails(Arrays.asList(lockedDetail));
	}

	private RegistrationDetailsDTO updateRcCancelltion(RegServiceDTO regServiceDTO,
			RegistrationDetailsDTO registrationDetailsDTO) {
		if (!regServiceDTO.isMviDone() && regServiceDTO.getFlowId() != null
				&& regServiceDTO.getFlowId().equals(Flow.RCCANCELLATIONCCO)) {
			regServiceDTO.setFlowId(Flow.RCCANCELLATIONEMVIACTION);
			rtaService.checkMviNameValidation(regServiceDTO.getApplicationNo(),
					regServiceDTO.getRcCancellation().getMviName());
			moveActionsDetailsToActionDetailsLogs(regServiceDTO);
			clearAcionDetails(regServiceDTO);
			initiateApprovalProcessFlow(regServiceDTO);
			setMviLockedDetailsRCCancellation(regServiceDTO);
		}
		if (regServiceDTO.isAoDone() && regServiceDTO.getFlowId() != null
				&& regServiceDTO.getFlowId().equals(Flow.RCCANCELLATIONEMVIACTION)) {
			registrationDetailsDTO.setRcCancelled(true);
			registrationDetailsDTO.setRcCancelledDate(LocalDate.now());
			registrationDetailsDTO.setApplicationStatus(StatusRegistration.RCCANCELLED.getDescription());
			registrationService.makingPermitDetailsAsInactive(registrationDetailsDTO);
		}
		return registrationDetailsDTO;
	}

	private void setFreshMviAOAction(RegServiceDTO regServiceDTO) {
		if (!regServiceDTO.isMviDone()) {
			regServiceDTO.setFlowId(Flow.RCFORFINANCEMVIACTION);
			rtaService.checkMviNameValidation(regServiceDTO.getApplicationNo(),
					regServiceDTO.getFreshRcdetails().getMviName());
			moveActionsDetailsToActionDetailsLogs(regServiceDTO);
			regServiceDTO.setActionDetails(null);
			regServiceDTO.setIterationCount(0);
			regServiceDTO.setCurrentIndex(0);
			regServiceDTO.setCurrentRoles(null);
			regServiceDTO.getFreshRcdetails().setAoApproved(true);
			initiateApprovalProcessFlow(regServiceDTO);
			setMviLockedDetailsFreshRC(regServiceDTO);
		}
	}

	private void checkValiadtionForAOAction(FreshRcDTO freshRcDTO, String status) {
		if (status.equals(StatusRegistration.REJECTED.getDescription())) {
			freshRcDTO.setAOReject(true);
		}

	}

	private void validationForFreshRcStatus(RtaActionVO actionVo, StatusRegistration applicationStatus,
			RegServiceDTO regServiceDTO, String role) {
		if (applicationStatus.equals(StatusRegistration.MVIREJECTED) && role.equals(RoleEnum.MVI.getName())) {
			regServiceDTO.setApplicationStatus(StatusRegistration.MVIREJECTED);
		}

		if (regServiceDTO.getServiceIds() != null
				&& regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId()))
				&& regServiceDTO.getServiceType() != null
				&& regServiceDTO.getServiceType().stream().anyMatch(type -> type.equals(ServiceEnum.RCFORFINANCE))) {
			if (applicationStatus.equals(StatusRegistration.REJECTED) && role.equals(RoleEnum.AO.getName())) {
				regServiceDTO.setApplicationStatus(StatusRegistration.AOREJECTED);
			}
			if (applicationStatus.equals(StatusRegistration.APPROVED) && role.equals(RoleEnum.AO.getName())) {
				regServiceDTO.setApplicationStatus(StatusRegistration.AOAPPROVED);
			}
			if (actionVo != null && actionVo.getStatus() != null
					&& actionVo.getStatus().equals(StatusRegistration.REJECTED)
					&& regServiceDTO.getActionDetails() != null
					&& regServiceDTO.getActionDetails().stream()
							.anyMatch(data -> data.getRole().equals(RoleEnum.MVI.name()) && data.getIsDoneProcess()
									&& data.getStatus()
											.equalsIgnoreCase(StatusRegistration.MVIREJECTED.getDescription()))
					&& applicationStatus.equals(StatusRegistration.APPROVED) && role.equals(RoleEnum.AO.getName())) {
				regServiceDTO.setApplicationStatus(StatusRegistration.AOREJECTED);
			}
			if (regServiceDTO.getFlowId() != null && regServiceDTO.getFlowId().equals(Flow.RCFORFINANCEMVIACTION)) {
				regServiceDTO.getFreshRcdetails().setAOAssignedToMVI(true);
			}
		}
	}

	private void frcValidationForReupload(CitizenEnclosuresDTO citizenEnclosuresDTO) {
		KeyValue<String, List<ImageEnclosureDTO>> enclosures = null;
		Optional<RegServiceDTO> regServiceDTO = null;
		if (StringUtils.isNotBlank(citizenEnclosuresDTO.getApplicationNo())) {
			regServiceDTO = regServiceDAO.findByApplicationNo(citizenEnclosuresDTO.getApplicationNo());
		}

		if (regServiceDTO.isPresent() && citizenEnclosuresDTO.getEnclosures() != null && regServiceDTO.get()
				.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId()))) {
			if (citizenEnclosuresDTO.getEnclosures().stream().anyMatch(images -> images.getValue().stream().anyMatch(
					status -> status.getImageStaus().equalsIgnoreCase(StatusRegistration.REUPLOAD.getDescription())))) {
				enclosures = citizenEnclosuresDTO.getEnclosures().stream()
						.filter(val -> val.getKey().equals(EnclosureType.Vehicle.getValue())).findFirst().get();
				for (ImageEnclosureDTO image : enclosures.getValue()) {
					image.setImageStaus(StatusRegistration.REUPLOAD.getDescription());
				}
			}
		}
	}

	private void updatesVehicleStoppageRevokation(RegServiceDTO regServiceDTO,
			RegistrationDetailsDTO registrationDetailsDTO) {
		registrationDetailsDTO.setVehicleStoppaged(Boolean.FALSE);
		regServiceDTO.setApprovedDate(LocalDateTime.now());
		registrationDetailsDTO.setVehicleStoppageRevoked(Boolean.TRUE);
		// registrationDetailsDTO.setStoppageDate(regServiceDTO.getVehicleStoppageDetails().getAoOrRtostoppageDate());

		regServiceDTO.getVehicleStoppageDetails().setAoOrRtostoppageRevpkationDate(
				regServiceDTO.getVehicleStoppageDetails().getAoOrRtostoppageRevpkationDate());
		registrationDetailsDTO.setVehicleStoppageRevokedDate(
				regServiceDTO.getVehicleStoppageDetails().getAoOrRtostoppageRevpkationDate());
		// LocalDate oldTaxUpTo = getTaxDate(registrationDetailsDTO);
		LocalDate taxUpTo = citizenTaxService.validity(TaxTypeEnum.QuarterlyTax.getDesc());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
		String date1 = "01-" + regServiceDTO.getVehicleStoppageDetails().getStoppageRevpkationDate().getMonthValue()
				+ "-" + String.valueOf(regServiceDTO.getVehicleStoppageDetails().getStoppageRevpkationDate().getYear());
		LocalDate localDate = LocalDate.parse(date1, formatter);
		long totalMonthsStopage = ChronoUnit.MONTHS.between(localDate, taxUpTo);
		totalMonthsStopage = totalMonthsStopage + 1;
		if (regServiceDTO.getVehicleStoppageDetails().getAoOrRtostoppageDate().getMonthValue() == regServiceDTO
				.getVehicleStoppageDetails().getAoOrRtostoppageRevpkationDate().getMonthValue()
				&& regServiceDTO.getVehicleStoppageDetails().getAoOrRtostoppageDate().getYear() == regServiceDTO
						.getVehicleStoppageDetails().getAoOrRtostoppageRevpkationDate().getYear()) {
			registrationDetailsDTO.setVehicleStoppageRevoked(Boolean.FALSE);
			registrationDetailsDTO.setTaxExemMonths(0);
		}
		if (totalMonthsStopage == registrationDetailsDTO.getTaxExemMonths()) {
			registrationDetailsDTO.setVehicleStoppageRevoked(Boolean.FALSE);
			if (totalMonthsStopage != 0) {
				regServiceDTO.setTaxAmount(1l);
				regServiceDTO.setTaxvalidity(taxUpTo);
				regServiceDTO.setTaxExcemption(Boolean.TRUE);
				// save the tax details
				saveCitizenTaxDetails(regServiceDTO, false, false, "AP");
			}
		}

	}

	private void updatesVehicleStoppage(RegServiceDTO regServiceDTO, RegistrationDetailsDTO registrationDetailsDTO) {
		registrationDetailsDTO.setVehicleStoppaged(Boolean.TRUE);
		regServiceDTO.setApprovedDate(LocalDateTime.now());
		registrationDetailsDTO.setStoppageDate(regServiceDTO.getVehicleStoppageDetails().getAoOrRtostoppageDate());
		LocalDate oldTaxUpTo = getTaxDate(registrationDetailsDTO);
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
		String date1 = "01-" + regServiceDTO.getVehicleStoppageDetails().getStoppageDate().getMonthValue() + "-"
				+ String.valueOf(regServiceDTO.getVehicleStoppageDetails().getStoppageDate().getYear());
		LocalDate localDate = LocalDate.parse(date1, formatter);
		// localDate = localDate.withDayOfMonth(localDate.getMonth().maxLength());
		long totalMonthsStopage = ChronoUnit.MONTHS.between(localDate, oldTaxUpTo);

		registrationDetailsDTO.setTaxExemMonths(totalMonthsStopage);
	}

	@Override
	public void updatesVehicleStoppageNewFlow(RtaActionVO reportVo, JwtUser jwtUser, String ipAddress, String role) {

		synchronized (reportVo.getApplicationNo().intern()) {
			if (StringUtils.isBlank(reportVo.getApplicationNo())) {
				logger.error("Application number not found");
				throw new BadRequestException("Application number not found");
			}
			RegServiceDTO stagingDetailsDTO = stoppagecommonValidation(jwtUser, reportVo.getApplicationNo(), role);

			LockedDetailsDTO lockedUserDetails = stagingDetailsDTO.getLockedDetails().stream().findFirst().get();
			if (!lockedUserDetails.getLockedByRole().equalsIgnoreCase(role)
					|| !role.equalsIgnoreCase(RoleEnum.MVI.getName())) {
				logger.error("Invalid user to insert MVI report: " + stagingDetailsDTO.getApplicationNo());
				throw new BadRequestException(
						"Invalid user to insert MVI report: " + stagingDetailsDTO.getApplicationNo());
			}
			if (!jwtUser.getUsername().equalsIgnoreCase(lockedUserDetails.getLockedBy())) {
				logger.error("Invalid user to insert MVI report: " + stagingDetailsDTO.getApplicationNo());
				throw new BadRequestException(
						"Invalid user to insert MVI report: " + stagingDetailsDTO.getApplicationNo());
			}
			Optional<RegistrationDetailsDTO> regDoc = registrationDetailDAO
					.findByApplicationNo(stagingDetailsDTO.getRegistrationDetails().getApplicationNo());
			if (!regDoc.isPresent()) {
				logger.error("Registration details not found for application number: "
						+ stagingDetailsDTO.getRegistrationDetails().getApplicationNo());
				throw new BadRequestException("Registration details not found for application number: "
						+ stagingDetailsDTO.getRegistrationDetails().getApplicationNo());
			}
			RegistrationDetailsDTO registrationDetailsDTO = regDoc.get();
			ActionDetails actions = new ActionDetails();
			actions.setRole(role);
			actions.setUserId(jwtUser.getUsername());
			actions.setlUpdate(LocalDateTime.now());
			actions.setIpAddress(ipAddress);
			actions.setStatus(reportVo.getStatus().getDescription());
			if (stagingDetailsDTO.getVehicleStoppageDetails().getActions() == null
					|| stagingDetailsDTO.getVehicleStoppageDetails().getActions().isEmpty()) {
				stagingDetailsDTO.getVehicleStoppageDetails().setActions(Arrays.asList(actions));
			} else {
				stagingDetailsDTO.getVehicleStoppageDetails().getActions().add(actions);
			}
			List<VehicleStoppageMVIReportVO> pendingReports = getPendingQuarters(stagingDetailsDTO);
			if (pendingReports != null && !pendingReports.isEmpty()) {
				if (reportVo.getStatus().equals(StatusRegistration.APPROVED)) {
					if (reportVo.getReport() == null || reportVo.getReport().isEmpty()) {
						logger.error("Please provide inspection report for application number: "
								+ stagingDetailsDTO.getApplicationNo());
						throw new BadRequestException("Please provide inspection report for application number: "
								+ stagingDetailsDTO.getApplicationNo());
					}
					saveMviReportCommon(jwtUser.getUsername(), reportVo.getReport(), stagingDetailsDTO,
							registrationDetailsDTO, Boolean.FALSE, pendingReports, reportVo.getStoppageQuations());
					regServiceDAO.save(stagingDetailsDTO);
					vehicleStoppageDetailsDAO.save(stagingDetailsDTO.getVehicleStoppageDetails());
					// TODO send messages.
				} else {
					if (stagingDetailsDTO.getVehicleStoppageDetails().getMviReport() != null
							&& !stagingDetailsDTO.getVehicleStoppageDetails().getMviReport().isEmpty()) {
						stagingDetailsDTO.setApplicationStatus(StatusRegistration.APPROVED);
						stagingDetailsDTO.setApprovedDate(LocalDateTime.now());
						RegServiceVO input = new RegServiceVO();
						input.setAadhaarNo(registrationDetailsDTO.getApplicantDetails().getAadharNo());
						input.setPrNo(registrationDetailsDTO.getPrNo());
						Set<Integer> ids = new HashSet<>();
						ids.add(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId());
						input.setServiceIds(ids);
						List<ServiceEnum> serviceType = new ArrayList<>();
						serviceType.add(ServiceEnum.VEHICLESTOPPAGEREVOKATION);
						input.setServiceType(serviceType);
						RegServiceDTO regServiceDto = registrationDetailsMapper.createNew(registrationDetailsDTO,
								input);
						regServiceDto.setVehicleStoppageDetails(stagingDetailsDTO.getVehicleStoppageDetails());
						regServiceDto.getVehicleStoppageDetails().setStoppageRevpkationDate(LocalDate.now());
						regServiceDto.setOfficeCode(stagingDetailsDTO.getOfficeCode());
						regServiceDto.setOfficeDetails(stagingDetailsDTO.getOfficeDetails());
						regServiceDto.setMviOfficeCode(stagingDetailsDTO.getMviOfficeCode());
						regServiceDto.setMviOfficeDetails(stagingDetailsDTO.getMviOfficeDetails());
						this.updatesVehicleStoppageRevokationNewFolw(regServiceDto, registrationDetailsDTO);
						this.updatecurrentRole(stagingDetailsDTO, role);
						stagingDetailsDTO.setLockedDetails(null);
						regServiceDto.getVehicleStoppageDetails().setAutoRevoked(Boolean.TRUE);
						stagingDetailsDTO.getVehicleStoppageDetails().setStaus(Boolean.FALSE);
						regServiceDto.setApplicationStatus(StatusRegistration.APPROVED);
						stagingDetailsDTO.getVehicleStoppageDetails()
								.setRejectionComments(reportVo.getApplicationComment());
						// regServiceDto.setApprovedDate(LocalDateTime.now());
						Map<String, String> officeCodeMap = new TreeMap<>();
						officeCodeMap.put("officeCode", regServiceDto.getOfficeCode());
						regServiceDto.setApplicationNo(sequenceGenerator.getSequence(
								String.valueOf(Sequence.REGISTRATIONAPPNO.getSequenceId()), officeCodeMap));
						regServiceDAO.save(stagingDetailsDTO);
						regServiceDAO.save(regServiceDto);
						vehicleStoppageDetailsDAO.save(stagingDetailsDTO.getVehicleStoppageDetails());
						registrationDetailDAO.save(registrationDetailsDTO);
						// TODO auto approve revocation if mvi inspect the vehicle or auto approved.
					} else {
						stagingDetailsDTO.setApplicationStatus(StatusRegistration.REJECTED);
						stagingDetailsDTO.getVehicleStoppageDetails().setRejected(Boolean.TRUE);
						stagingDetailsDTO.getVehicleStoppageDetails().setStaus(Boolean.FALSE);
						stagingDetailsDTO.getVehicleStoppageDetails()
								.setRejectionComments(reportVo.getApplicationComment());
						this.updatecurrentRole(stagingDetailsDTO, role);
						stagingDetailsDTO.setLockedDetails(null);
						vehicleStoppageDetailsDAO.save(stagingDetailsDTO.getVehicleStoppageDetails());
						regServiceDAO.save(stagingDetailsDTO);
					}

				}
			}
		}

	}

	@Override
	public void saveMviReportCommon(String jwtUser, List<VehicleStoppageMVIReportVO> report,
			RegServiceDTO stagingDetailsDTO, RegistrationDetailsDTO registrationDetailsDTO, boolean autoApprove,
			List<VehicleStoppageMVIReportVO> pendingReports, List<MasterStoppageQuationsVO> stoppageQuations) {
		List<VehicleStoppageMVIReportVO> finalReport = new ArrayList<>();

		if (pendingReports != null && !pendingReports.isEmpty()) {

			for (VehicleStoppageMVIReportVO pendingREprotFormCode : pendingReports) {
				for (VehicleStoppageMVIReportVO pendingREprotFormUi : report) {
					if (pendingREprotFormUi.getQuarterNumber() == pendingREprotFormCode.getQuarterNumber()) {
						pendingREprotFormCode.setRemarks(pendingREprotFormUi.getRemarks());
						finalReport.add(pendingREprotFormCode);
					}
				}
			}
			if (finalReport != null && !finalReport.isEmpty()) {
				finalReport.forEach(id ->

				{
					// id.getQuarterNumber()
					id.setApprovedDate(LocalDate.now());
					id.setAutoApproved(autoApprove);
					id.setUserId(jwtUser);
					if (!autoApprove) {
						id.setStoppageQuations(stoppageQuations);
					}

				});
			}

			if (stagingDetailsDTO.getVehicleStoppageDetails().getMviReport() != null
					&& !stagingDetailsDTO.getVehicleStoppageDetails().getMviReport().isEmpty()) {
				stagingDetailsDTO.getVehicleStoppageDetails().getMviReport()
						.addAll(vehicleStoppageMVIReportMapper.convertVO(finalReport));
			} else {
				stagingDetailsDTO.getVehicleStoppageDetails()
						.setMviReport(vehicleStoppageMVIReportMapper.convertVO(finalReport));
			}

			if (!registrationDetailsDTO.isVehicleStoppaged()) {
				registrationDetailsDTO.setVehicleStoppaged(Boolean.TRUE);
				registrationDetailsDTO.setStoppageDate(stagingDetailsDTO.getVehicleStoppageDetails().getStoppageDate());
				LocalDate oldTaxUpTo = getTaxDate(registrationDetailsDTO);
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
				String date1 = "01-" + stagingDetailsDTO.getVehicleStoppageDetails().getStoppageDate().getMonthValue()
						+ "-"
						+ String.valueOf(stagingDetailsDTO.getVehicleStoppageDetails().getStoppageDate().getYear());
				LocalDate localDate = LocalDate.parse(date1, formatter);
				long totalMonthsStopage = 0l;
				if (oldTaxUpTo != null) {
					totalMonthsStopage = ChronoUnit.MONTHS.between(localDate, oldTaxUpTo);
				}

				registrationDetailsDTO.setTaxExemMonths(totalMonthsStopage);
				stagingDetailsDTO.getVehicleStoppageDetails().setTaxExemMonths(totalMonthsStopage);
				registrationDetailDAO.save(registrationDetailsDTO);
			}
		}
	}

	@Override
	public void updatesVehicleStoppageRevokationNewFolw(RegServiceDTO regServiceDTO,
			RegistrationDetailsDTO registrationDetailsDTO) {
		List<ServiceEnum> serviceType = new ArrayList<>();
		serviceType.add(ServiceEnum.VEHICLESTOPPAGEREVOKATION);
		regServiceDTO.setServiceType(serviceType);
		registrationDetailsDTO.setVehicleStoppaged(Boolean.FALSE);
		regServiceDTO.setApprovedDate(LocalDateTime.now());
		registrationDetailsDTO.setVehicleStoppageRevoked(Boolean.TRUE);
		// registrationDetailsDTO.setStoppageDate(regServiceDTO.getVehicleStoppageDetails().getAoOrRtostoppageDate());
		registrationDetailsDTO
				.setVehicleStoppageRevokedDate(regServiceDTO.getVehicleStoppageDetails().getStoppageRevpkationDate());
		// LocalDate oldTaxUpTo = getTaxDate(registrationDetailsDTO);
		LocalDate taxUpTo = citizenTaxService.validity(TaxTypeEnum.QuarterlyTax.getDesc());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
		String date1 = "01-" + regServiceDTO.getVehicleStoppageDetails().getStoppageRevpkationDate().getMonthValue()
				+ "-" + String.valueOf(regServiceDTO.getVehicleStoppageDetails().getStoppageRevpkationDate().getYear());
		LocalDate localDate = LocalDate.parse(date1, formatter);
		long totalMonthsStopage = ChronoUnit.MONTHS.between(localDate, taxUpTo);
		totalMonthsStopage = totalMonthsStopage + 1;
		if (regServiceDTO.getVehicleStoppageDetails().getStoppageDate().getMonthValue() == regServiceDTO
				.getVehicleStoppageDetails().getStoppageRevpkationDate().getMonthValue()
				&& regServiceDTO.getVehicleStoppageDetails().getStoppageDate().getYear() == regServiceDTO
						.getVehicleStoppageDetails().getStoppageRevpkationDate().getYear()) {
			registrationDetailsDTO.setVehicleStoppageRevoked(Boolean.FALSE);
			registrationDetailsDTO.setTaxExemMonths(0);
		} else if (this.istaxExcemptionCov(regServiceDTO.getRegistrationDetails())) {
			registrationDetailsDTO.setVehicleStoppageRevoked(Boolean.FALSE);
			registrationDetailsDTO.setTaxExemMonths(0);
		}

		if (totalMonthsStopage == registrationDetailsDTO.getTaxExemMonths()) {
			registrationDetailsDTO.setVehicleStoppageRevoked(Boolean.FALSE);
			if (totalMonthsStopage != 0) {
				regServiceDTO.setTaxAmount(1l);
				regServiceDTO.setTaxvalidity(taxUpTo);
				regServiceDTO.setTaxExcemption(Boolean.TRUE);
				// save the tax details
				saveCitizenTaxDetails(regServiceDTO, false, false, "AP");
			}
		}

	}

	private boolean istaxExcemptionCov(RegistrationDetailsDTO regDto) {

		if (citizenTaxService.ownertypecheck(regDto.getOwnerType().getCode(), regDto.getClassOfVehicle())) {
			return true;
		}
		Optional<MasterTaxExcemptionsDTO> optionalTaxExcemption = masterTaxExcemptionsDAO
				.findByKeyvalueOrCovcode(regDto.getVahanDetails().getMakersModel(), regDto.getClassOfVehicle());
		if (optionalTaxExcemption.isPresent()) {
			return true;
		}
		if (regDto.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())
				|| (regDto.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TGVT.getCovCode())
						&& regDto.getVahanDetails().getGvw() <= 3000)
				|| regDto.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())
				|| regDto.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TRTT.getCovCode())
				|| regDto.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TRCN.getCovCode())) {
			return true;
		}
		if ((regDto.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.GCRT.getCovCode())
				&& regDto.getVahanDetails().getGvw() <= 3000)) {
			List<TaxDetailsDTO> listOfTaxDetails = taxDetailsDAO
					.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(regDto.getApplicationNo(),
							Arrays.asList(TaxTypeEnum.LifeTax.getDesc()));
			if (listOfTaxDetails != null && !listOfTaxDetails.isEmpty()) {
				for (TaxDetailsDTO dto : listOfTaxDetails) {
					if (regDto.getClassOfVehicle().equalsIgnoreCase(dto.getClassOfVehicle())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	private void updatecurrentRole(RegServiceDTO regServiceDTO, String role) {
		if (regServiceDTO.getCurrentRoles() != null && !regServiceDTO.getCurrentRoles().isEmpty()
				&& regServiceDTO.getCurrentRoles().contains(role)) {
			Optional<ActionDetails> actionDetailsOpt = regServiceDTO.getActionDetails().stream()
					.filter(p -> role.equals(p.getRole())).findFirst();
			if (!actionDetailsOpt.isPresent()) {
				logger.error("User role [{}] specific details not found in action detail", role);
				throw new BadRequestException("User role " + role + " specific details not found in actiondetail");
			}
			regServiceDTO.setCurrentIndex(4);
			regServiceDTO.setCurrentRoles(null);
		}
	}

	private void freshRCValidation(RegServiceDTO regServiceDTO, String role, RtaActionVO actionVo,
			StatusRegistration status) {
		List<Comments> commentsList = null;
		if (regServiceDTO.getFreshRcdetails().getFrcComments() != null) {
			commentsList = regServiceDTO.getFreshRcdetails().getFrcComments();
		} else {
			commentsList = new ArrayList<>();
		}
		if (actionVo.getFreshRCAction() == null) {
			throw new BadRequestException("FreshRC Action Details not found");
		}

		Comments comments = new Comments();
		actionVo.getFreshRCAction().getComments().forEach(val -> {
			if (val.getComments() != null && val.getDate() != null && val.getRole() != null
					&& val.getUserId() != null) {
				comments.setComments(val.getComments().toUpperCase());
				comments.setUserId(val.getUserId());
				comments.setRole(val.getRole());
				comments.setDate(val.getDate());
				if (regServiceDTO.getCurrentRoles() != null
						&& regServiceDTO.getCurrentRoles().stream().anyMatch(name -> role.equals(role))
						&& regServiceDTO.isMviDone()
						&& actionVo.getStatus().getDescription().equals(StatusRegistration.REJECTED.toString())) {
					comments.setStatus(StatusRegistration.REJECTED);
				} else {
					comments.setStatus(status);
				}
			}
		});

		if (role.equals(RoleEnum.MVI.getName()) && (!actionVo.getFreshRCAction().getComments().isEmpty())) {
			regServiceDTO.setMviDone(true);
			regServiceDTO.getFreshRcdetails().setAOAssignedToMVI(true);
		} else {
			if (StringUtils.isNotEmpty(actionVo.getFreshRCAction().getMviName())) {
				regServiceDTO.getFreshRcdetails().setMviName(actionVo.getFreshRCAction().getMviName());
				regServiceDTO.getFreshRcdetails().setMviUserId(rtaService
						.checkMviNameValidation(actionVo.getApplicationNo(), actionVo.getFreshRCAction().getMviName()));
				checkValiadtionForAOAction(regServiceDTO.getFreshRcdetails(),
						regServiceDTO.getApplicationStatus().getDescription());
			}
		}
		commentsList.add(comments);
		regServiceDTO.getFreshRcdetails().setFrcComments(commentsList);
	}

	private RegServiceDTO clearAcionDetails(RegServiceDTO regServiceDTO) {
		regServiceDTO.setActionDetails(null);
		regServiceDTO.setIterationCount(0);
		regServiceDTO.setCurrentIndex(0);
		regServiceDTO.setCurrentRoles(null);
		return regServiceDTO;
	}

	private void rcCancellationValidation(RegServiceDTO regServiceDTO, String role, RtaActionVO actionVo) {
		if (role.equals(RoleEnum.RTO.getName()) && regServiceDTO.getFlowId() != null
				&& regServiceDTO.getFlowId().equals(ServiceEnum.Flow.RCCANCELLATIONCCO)) {
			if (StringUtils.isNotEmpty(actionVo.getRcCancellationAction().getMviName())) {
				regServiceDTO.getRcCancellation().setMviName(actionVo.getRcCancellationAction().getMviName());
				regServiceDTO.getRcCancellation().setMviUserId(rtaService.checkMviNameValidation(
						actionVo.getApplicationNo(), actionVo.getRcCancellationAction().getMviName()));

			}
		}
		if (role.equals(RoleEnum.AO.getName())) {
			if (StringUtils.isNotEmpty(actionVo.getRcCancellationAction().getAoRemarks())) {
				regServiceDTO.getRcCancellation().setAoRemarks(actionVo.getRcCancellationAction().getAoRemarks());
				regServiceDTO.setAoDone(true);
			}
		}
	}

	private void setMviLockedDetailsRCCancellation(RegServiceDTO regServiceDTO) {
		LockedDetailsDTO lockedDetail = rtaService.setLockedDetails(regServiceDTO.getRcCancellation().getMviUserId(),
				RoleEnum.MVI.name(), regServiceDTO.getIterationCount(),
				regServiceDTO.getRegistrationDetails().getVehicleType(), regServiceDTO.getApplicationNo());
		regServiceDTO.setLockedDetails(Arrays.asList(lockedDetail));
	}

	@Override
	public Optional<InputVO> getListOfSupportedEnclosersForApp(RtaActionVO input, JwtUser jwtUser) {
		List<EnclosuresDTO> enclosureList;
		Set<Integer> serviceIds = new HashSet<Integer>();
		List<String> serviceEnumList = new ArrayList<String>();
		Optional<DealerRegVO> dealerRegVO = null;
		RegServiceDTO dto = null;
		if (StringUtils.isBlank(input.getApplicationNo())) {
			logger.error("Please provide application number for images");
			throw new BadRequestException("Please provide application number for images");
		}

		dto = getRegServiceApplication(input.getApplicationNo());

		if (dto != null) {
			if (dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))) {
				if (dto.getVehicleStoppageDetails().getRtoCompleted() == null
						|| !dto.getVehicleStoppageDetails().getRtoCompleted()) {
					logger.error("RTO action not completed please check with RTO officer for assign MVI : "
							+ input.getApplicationNo());
					throw new BadRequestException(
							"RTO action not completed please check with RTO officer for assign MVI : "
									+ input.getApplicationNo());
				}
				this.commonValidationForstoppage(dto, jwtUser);
			} else if (dto.getServiceIds().stream()
					.anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId()))) {
				if (dto.getVehicleStoppageDetails().getRtoCompleted() == null
						|| !dto.getVehicleStoppageDetails().getRtoCompleted()) {
					logger.error("RTO action not completed please check with RTO officer for assign MVI : "
							+ input.getApplicationNo());
					throw new BadRequestException(
							"RTO action not completed please check with RTO officer for assign MVI : "
									+ input.getApplicationNo());
				}
				this.checkMviNameForStoppage(dto, jwtUser);
				Optional<CitizenEnclosuresDTO> enclosures = citizenEnclosuresDAO
						.findByApplicationNo(dto.getApplicationNo());
				if (enclosures.isPresent()) {
					logger.error("Enclosures added for this vehicle : " + input.getApplicationNo());
					throw new BadRequestException("Enclosures added for this vehicle : " + input.getApplicationNo());
				}
			}
			serviceIds.addAll(dto.getServiceIds());
			dto.getServiceType().stream().forEach(id -> {
				serviceEnumList.add(id.getDesc());
			});
			// serviceEnumList.add(dto.getServiceType().toString());
		} else {
			Optional<DealerRegDTO> dealerRegDTO = dealerRegDAO.findByApplicationNo(input.getApplicationNo());
			if (!dealerRegDTO.isPresent()) {
				logger.error("No record found for application number: " + input.getApplicationNo());
				throw new BadRequestException("No record found for application number: " + input.getApplicationNo());
			}
			if (dealerRegDTO.get().getIsMVIDone() != null && dealerRegDTO.get().getIsMVIDone()) {
				throw new BadRequestException("This application is at DTC level : " + input.getApplicationNo());
			}
			serviceIds.addAll(dealerRegDTO.get().getServiceIds());
			// serviceEnumList.add(dealerRegDTO.get().getServiceType().toString());
			dealerRegDTO.get().getServiceType().stream().forEach(id -> {
				serviceEnumList.add(id.getDesc());
			});
			dealerRegVO = dealerRegMapper.convertEntity(dealerRegDTO);
		}

		enclosureList = userEnclosureDAO.findByServiceIDInAndApplicantType(serviceIds,
				ApplicantTypeEnum.getName(input.getSelectedRole()));

		if (enclosureList.isEmpty()) {
			logger.error("No enclosures found based on given Service Id: " + serviceIds);
			throw new BadRequestException("No enclosures found based on given Service Id: " + serviceIds);
		}

		InputVO inputVO = userEnclosureMapper.convertEnclosureDTOtoInputVO(enclosureList);
		inputVO.setSerivceType(serviceEnumList);
		if (dealerRegVO != null && dealerRegVO.isPresent()) {
			inputVO.setDealerRegVO(dealerRegVO.get());
		}
		if (dto != null) {
			inputVO.setRegServiceVO(regServiceMapper.convertEntity(dto));
		}
		return Optional.of(inputVO);
	}

	private RegServiceDTO getRegServiceApplication(String applicationNo) {

		Optional<RegServiceDTO> regServiceDto = regServiceDAO.findByApplicationNo(applicationNo);
		if (regServiceDto.isPresent()) {
			logger.error("No record found for application number: " + applicationNo);
			// throw new BadRequestException("No record found for application number:
			// "+applicationNo);
			return regServiceDto.get();
		}
		return null;
	}

	private List<VehicleStoppageMVIReportVO> commonValidationForstoppage(RegServiceDTO dto, JwtUser jwtUser) {

		this.checkMviNameForStoppage(dto, jwtUser);
		List<VehicleStoppageMVIReportVO> pendingReports = this.getPendingQuarters(dto);
		if (pendingReports == null || pendingReports.isEmpty()) {
			logger.error("No pending quaters to inspect for application number: " + dto.getApplicationNo());
			throw new BadRequestException(
					"No pending quaters to inspect for application number: " + dto.getApplicationNo());
		}
		return pendingReports;
	}

	private void checkMviNameForStoppage(RegServiceDTO dto, JwtUser jwtUser) {
		if (!dto.getLockedDetails().stream()
				.anyMatch(one -> one.getLockedBy().equalsIgnoreCase(jwtUser.getUsername()))) {
			logger.error("Unauthorized user of application No [{}]", dto.getApplicationNo());
			throw new BadRequestException("Unauthorized User");
		}
	}

	@Override
	public List<InputVO> returnCurrentQuarterImagesForStoppage(String applicationNo, JwtUser jwtUser) {
		RegServiceDTO dto = getRegServiceApplication(applicationNo);
		if (!(dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId())) || dto
				.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId())))) {
			logger.error("Application not belongs to vehicle stoopage: " + dto.getApplicationNo());
			throw new BadRequestException(
					"Application not belongs to vehicle stoopage for application number: " + dto.getApplicationNo());
		}
		Optional<CitizenEnclosuresDTO> enclosresOptional = citizenEnclosuresDAO.findByApplicationNo(applicationNo);
		if (!enclosresOptional.isPresent()) {
			logger.error(
					"Please upload enclosures using mobile app for the application number: " + dto.getApplicationNo());
			throw new BadRequestException(
					"Please upload enclosures using mobile app for the application number: " + dto.getApplicationNo());
		}
		CitizenEnclosuresDTO enclosresDto = enclosresOptional.get();
		List<InputVO> map = new ArrayList<InputVO>();
		if (dto.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))) {
			List<VehicleStoppageMVIReportVO> pendingReports = this.commonValidationForstoppage(dto, jwtUser);
			int quarterNumber = pendingReports.stream().findFirst().get().getQuarterNumber();
			boolean imageNotUploded = Boolean.TRUE;
			for (KeyValue<String, List<ImageEnclosureDTO>> key : enclosresDto.getEnclosures()) {
				for (ImageEnclosureDTO imgage : key.getValue()) {
					if (imgage.getQuarterNumber() != null && imgage.getQuarterNumber().equals(quarterNumber)) {
						imageNotUploded = Boolean.FALSE;
						InputVO vo = new InputVO();
						vo.setName(imgage.getEnclosureName());
						vo.setUrl(imagePreUrl + "?appImageDocId=" + imgage.getImageId());
						map.add(vo);
					}
				}
			}
			if (imageNotUploded) {
				logger.error(
						"Please upload current quarter enclosures for application number: " + dto.getApplicationNo());
				throw new BadRequestException(
						"Please upload current quarter enclosures for application number: " + dto.getApplicationNo());
			}
		} else if (dto.getServiceIds().stream()
				.anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId()))) {
			this.checkMviNameForStoppage(dto, jwtUser);
			for (KeyValue<String, List<ImageEnclosureDTO>> key : enclosresDto.getEnclosures()) {
				for (ImageEnclosureDTO imgage : key.getValue()) {
					InputVO vo = new InputVO();
					vo.setName(imgage.getEnclosureName());
					vo.setUrl(imagePreUrl + "?appImageDocId=" + imgage.getImageId());
					map.add(vo);
				}
			}
		}
		return map;
	}

	@Override
	public void dealerApprovalProcess(JwtUser jwtUser, RtaActionVO rtaActionVO, String role,
			MultipartFile[] uploadfiles) {
		logger.info("In Approval process with role [{}]", role);
		Optional<DealerRegDTO> regServiceDTOOptional = dealerRegDAO.findByApplicationNo(rtaActionVO.getApplicationNo());

		if (!regServiceDTOOptional.isPresent()) {
			logger.error("No record in registration_services with applicationNo [{}]", rtaActionVO.getApplicationNo());
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.NO_AUTHORIZATION));
		}

		DealerRegDTO dealerRegDTO = regServiceDTOOptional.get();

		if ((RoleEnum.DTC.getIndex() + 1) == (dealerRegDTO.getCurrentIndex())) {
			logger.error("record.process.done");
			throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.RECORD_PROCESS_DONE));
		}

		StatusRegistration applicationStatus = null;
		Optional<CitizenEnclosuresDTO> citizenEnclosuresOpt = Optional.empty();
		// TODO: check actionVo.getStatus() should be APPROVED or REJECTED.
		applicationStatus = rtaActionVO.getStatus();
		if (!Arrays.asList(StatusRegistration.REJECTED, StatusRegistration.APPROVED)
				.contains(rtaActionVO.getStatus())) {
			logger.error("Invalid approval status ");
			throw new BadRequestException("Invalid approval status.Status should be either "
					+ StatusRegistration.REJECTED + " or " + StatusRegistration.APPROVED);
		}

		if (dealerRegDTO.getServiceType().stream().anyMatch(serviceEnum -> serviceEnum.getIsEnclouserRequired())) {
			Set<Integer> serviceId = new HashSet<>();
			serviceId.addAll(dealerRegDTO.getServiceIds());
			citizenEnclosuresOpt = citizenEnclosuresDAO
					.findByApplicationNoAndServiceIdsIn(dealerRegDTO.getApplicationNo(), serviceId);
			if (!citizenEnclosuresOpt.isPresent()) {
				logger.error("Enclosures not found for application No [{}]", dealerRegDTO.getApplicationNo());
				throw new BadRequestException(
						"Enclosures not found for application [{" + dealerRegDTO.getApplicationNo() + "}]");
			}
			applicationStatus = updateEnclosures(role, rtaActionVO.getEnclosures(), citizenEnclosuresOpt.get());
		}

		setStatusAndIterationCountForDealer(dealerRegDTO, role, jwtUser, applicationStatus, rtaActionVO);
		List<LockedDetailsDTO> filteredLockedDetails = removeMatchedLockedDetailsForDealer(dealerRegDTO,
				jwtUser.getId(), role);
		if (filteredLockedDetails != null && !filteredLockedDetails.isEmpty()) {
			logger.debug("Setting default Lock value for User [{}] having Role [{}] and belongs to officeCode [{}]",
					jwtUser.getId(), role, dealerRegDTO.getOfficeCode());
			dealerRegDTO.setLockedDetails(filteredLockedDetails);
		}

		dealerRegDTO.setlUpdate(LocalDateTime.now());
		dealerRegDTO.setUpdatedBy(jwtUser.getId());
		dealerRegDAO.save(dealerRegDTO);

		if (citizenEnclosuresOpt.isPresent()) {
			citizenEnclosuresOpt.get().setIterator(dealerRegDTO.getIterationCount());
			citizenEnclosuresDAO.save(citizenEnclosuresOpt.get());
			copyToCitizenEnclosuresLogs(citizenEnclosuresOpt.get());
			if (StatusRegistration.APPROVED.equals(dealerRegDTO.getApplicationStatus())
					&& RoleEnum.RTO.getIndex() < dealerRegDTO.getCurrentIndex()) {
				updateImageStatus(citizenEnclosuresOpt.get());
			}
		}
	}

	private List<LockedDetailsDTO> removeMatchedLockedDetailsForDealer(DealerRegDTO dealerRegDTO, String userId,
			String role) {
		List<LockedDetailsDTO> lockedDetails = new ArrayList<>();
		if (dealerRegDTO.getLockedDetails() != null) {
			logger.info("Removing Lock from User [{}] having Role [{}] and belongs to officeCode [{}]", userId, role,
					dealerRegDTO.getOfficeCode());
			lockedDetails = dealerRegDTO.getLockedDetails();
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

	private void setStatusAndIterationCountForDealer(DealerRegDTO dealerRegDTO, String role, JwtUser jwtUser,
			StatusRegistration status, RtaActionVO actionVO) {
		Integer serviceId = getServiceIdForDealer(dealerRegDTO);
		Optional<ApprovalProcessFlowDTO> approvalProcessFlowDTO = approvalProcessFlowDAO
				.findByServiceIdAndRoleAndIsFinalTrue(serviceId, role);
		ApprovalProcessFlowDTO approvalFlowDTO = approvalProcessFlowDTO.get();
		ActionDetails actionDetail = getActionDetailByRoleForDealerFinalStepChecker(dealerRegDTO, role);
		updateActionDetailsStatus(role, jwtUser.getId(), status.getDescription(), actionDetail, actionVO);
		if (null != dealerRegDTO.getCurrentRoles()) {
			if (approvalFlowDTO.getIsFinal() != null && approvalFlowDTO.getIsFinal()) {
				dealerRegDTO.getCurrentRoles().clear();
			} else {
				dealerRegDTO.getCurrentRoles().remove(role);
			}
		}
		// Need to check it final step or Not
		if (approvalFlowDTO.getFinalStepChecker() == null) {// this'll be execute for CCO and MVI
			logger.info("In final step checker of approvalflow ");
			if (RoleEnum.MVI.getName().equals(role)
					|| (approvalFlowDTO.getIsFinal() != null && approvalFlowDTO.getIsFinal())) {
				if (StatusRegistration.REJECTED.equals(status)) {
					dealerRegDTO.setApplicationStatus(approvalFlowDTO.getRejectionStatus());
					// sendNotifications(MessageTemplate.REG_REJECTED.getId(), dealerRegDTO);
					return;
				}

				if (approvalFlowDTO.getApproveStatus() != null) {
					dealerRegDTO.setApplicationStatus(approvalFlowDTO.getApproveStatus());
				}
			}
			incrementToNextIndexToDealer(dealerRegDTO, actionDetail, role);
			// sendNotifications(MessageTemplate.REG_APPROVAL.getId(),regServiceDTO);//MVI
			// APPROVED intimation
			return;
		} // EX:.. CCO and MVI end's here

		// It'll execute for AO & RTO
		for (Map.Entry<String, String> keyValue : approvalFlowDTO.getFinalStepChecker().entrySet()) {
			ActionDetails finalActionDetail = getActionDetailByRoleForDealerFinalStepCheckerForFinalFlow(dealerRegDTO,
					keyValue.getKey());
			if (!keyValue.getValue().equals(finalActionDetail.getStatus())) {
				// So this is not final need to Iterate or go to next Index
				iterateOrGoToNextIndexForDealer(dealerRegDTO, approvalFlowDTO);// RejectionCase
				return;
			}
			updateDealerDetails(dealerRegDTO, approvalFlowDTO);
		}
		// sendNotifications(MessageTemplate.REG_APPROVAL.getId(), dealerRegDTO);//
		// FINAL Approval cases

	}

	private ActionDetails getActionDetailByRoleForDealerFinalStepCheckerForFinalFlow(DealerRegDTO dealerRegDTO,
			String role) {
		ActionDetails details = new ActionDetails();
		if (dealerRegDTO.getActionDetails() != null && dealerRegDTO.getActionDetails().size() > 0) {
			dealerRegDTO.getActionDetails().sort((o1, o2) -> o2.getlUpdate().compareTo(o1.getlUpdate()));
			details = dealerRegDTO.getActionDetails().get(0);
		}
		if (!role.equals(details.getRole())) {
			throw new BadRequestException("User role " + role + " specific details not found in actiondetail");
		}
		return details;
	}

	private ActionDetails getActionDetailByRoleForDealerFinalStepChecker(DealerRegDTO dealerRegDTO, String role) {

		Optional<ActionDetails> actionDetailsOpt = dealerRegDTO.getActionDetails().stream()
				.filter(p -> role.equals(p.getRole()) && p.getIsDoneProcess().equals(Boolean.FALSE)).findFirst();
		if (!actionDetailsOpt.isPresent()) {
			logger.error("User role [{}] specific details not found in action detail", role);
			throw new BadRequestException("User role " + role + " specific details not found in actiondetail");
		}
		return actionDetailsOpt.get();
	}

	private void updateDealerDetails(DealerRegDTO dealerRegDTO, ApprovalProcessFlowDTO approvalFlowDTO) {
		dealerRegDTO.setApplicationStatus(approvalFlowDTO.getApproveStatus());
		dealerRegDTO.setCurrentIndex(RoleEnum.DTC.getIndex() + 1);
		dealerRegDTO.setCurrentRoles(null);

		if (dealerRegDTO.getServiceType().contains(ServiceEnum.DEALERREGISTRATION)) {
			updateDealerDetailsinMasterUsers(dealerRegDTO);
		} else if (dealerRegDTO.getServiceType().contains(ServiceEnum.DEALERSHIPRENEWAL)) {
			updateRenewalofDealerShipDetails(dealerRegDTO);
		}

	}

	@Override
	public void updateRenewalofDealerShipDetails(DealerRegDTO dealerRegDTO) {
		dealerRegDTO.setApplicationStatus(StatusRegistration.APPROVED);
		Optional<UserDTO> userDTO = userDAO.findByUserId(dealerRegDTO.getDealerUserId());
		if (userDTO.isPresent()) {
			if (userDTO.get().getValidTo() != null && userDTO.get().getValidTo().isAfter(LocalDate.now())) {
				userDTO.get().setValidFrom(userDTO.get().getValidTo());
				userDTO.get().setValidTo(userDTO.get().getValidTo().plusYears(1).minusDays(1));
			} else {
				userDTO.get().setValidFrom(LocalDate.now());
				userDTO.get().setValidTo(LocalDate.now().plusYears(1).minusDays(1));
			}
			userDTO.get().setValidFrom(LocalDate.now());
			userDTO.get().setValidTo(LocalDate.now().plusYears(1).minusDays(1));
		}
		if (dealerRegDTO.getDealerAddress() != null) {
			if (dealerRegDTO.getDealerAddress().getState() != null) {
				userDTO.get().setState(dealerRegDTO.getDealerAddress().getState());
			}
			if (dealerRegDTO.getDealerAddress().getDistrict() != null) {
				userDTO.get().setDistrict(dealerRegDTO.getDealerAddress().getDistrict());
			}
			if (dealerRegDTO.getDealerAddress().getMandal() != null) {
				userDTO.get().setMandal(dealerRegDTO.getDealerAddress().getMandal());
			}
			if (dealerRegDTO.getDealerAddress().getVillage() != null) {
				userDTO.get().setVillage(dealerRegDTO.getDealerAddress().getVillage());
			}
			if (StringUtils.isNotBlank(dealerRegDTO.getDealerAddress().getDoorNo())) {
				userDTO.get().setDoorNo(dealerRegDTO.getDealerAddress().getDoorNo());
			}
			if (StringUtils.isNotBlank(dealerRegDTO.getDealerAddress().getStreetName())) {
				userDTO.get().setStreetName(dealerRegDTO.getDealerAddress().getStreetName());
			}
		}
		userDAO.save(userDTO.get());
	}

	private void updateDealerDetailsinMasterUsers(DealerRegDTO dealerRegDTO) {

		String dealerUserName = getDealerUserName(dealerRegDTO);
		Integer rId = getDealerRid(dealerRegDTO);

		UserDTO userDto = new UserDTO();
		userDto.setFirstname(dealerRegDTO.getGstnData().getEntityName());
		userDto.setEmail(dealerRegDTO.getApplicantDetails().getContact().getEmail());
		userDto.setMobile(dealerRegDTO.getApplicantDetails().getContact().getMobile());
		userDto.setMobile(dealerRegDTO.getApplicantDetails().getContact().getMobile());
		userDto.setOffice(dealerRegDTO.getOfficeDetails());
		userDto.setPassword(financierPassword);
		userDto.setUserId(dealerUserName);

		RolesDTO primaryRole = new RolesDTO();
		primaryRole.setName(RoleEnum.DEALER.getName());
		primaryRole.setId(RoleEnum.DEALER.getIndex().toString());
		userDto.setPrimaryRole(primaryRole);
		userDto.setValidFrom(LocalDate.now());
		userDto.setValidTo(LocalDate.now().plusYears(1).minusDays(1));
		userDto.setUserStatus(UserStatusEnum.ACTIVE);
		userDto.setStatus(Boolean.TRUE);
		userDto.setUserLevel(1);
		userDto.setRid(rId);
		userDto.setAdditionalRoles(new ArrayList<>());
		userDto.setIsAccountNonLocked(Boolean.TRUE);

		if (dealerRegDTO.getApplicantDetails().getPresentAddress() != null) {
			if (dealerRegDTO.getApplicantDetails().getPresentAddress().getState() != null) {
				userDto.setState(dealerRegDTO.getApplicantDetails().getPresentAddress().getState());
			}
			if (dealerRegDTO.getApplicantDetails().getPresentAddress().getDistrict() != null) {
				userDto.setDistrict(dealerRegDTO.getApplicantDetails().getPresentAddress().getDistrict());
			}
			if (dealerRegDTO.getApplicantDetails().getPresentAddress().getMandal() != null) {
				userDto.setMandal(dealerRegDTO.getApplicantDetails().getPresentAddress().getMandal());
			}
			if (dealerRegDTO.getApplicantDetails().getPresentAddress().getVillage() != null) {
				userDto.setVillage(dealerRegDTO.getApplicantDetails().getPresentAddress().getVillage());
			}
			if (StringUtils.isNotBlank(dealerRegDTO.getApplicantDetails().getPresentAddress().getDoorNo())) {
				userDto.setDoorNo(dealerRegDTO.getApplicantDetails().getPresentAddress().getDoorNo());
			}
			if (StringUtils.isNotBlank(dealerRegDTO.getApplicantDetails().getPresentAddress().getStreetName())) {
				userDto.setStreetName(dealerRegDTO.getApplicantDetails().getPresentAddress().getStreetName());
			}
		}

		userDAO.save(userDto);

		dealerRegDTO.setDealerUserId(dealerUserName);
		dealerCovAndMakerMapping(dealerRegDTO, userDto);

	}

	private void dealerCovAndMakerMapping(DealerRegDTO dealerRegDTO, UserDTO userDto) {
		List<DealerCovDTO> covsList = new ArrayList<>();
		List<DealerMakerDTO> makersList = new ArrayList<>();
		// For Class Of Vehicle Mapping
		dealerRegDTO.getClassOfVehicles().stream().forEach(val -> {
			DealerCovDTO covDTO = new DealerCovDTO();
			covDTO.setrId(userDto.getRid());
			covDTO.setCovId(val.getCovcode());
			covDTO.setStatus(Boolean.TRUE);
			covDTO.setCreatedDate(LocalDateTime.now());
			covDTO.setlUpdate(LocalDateTime.now());
			covsList.add(covDTO);
		});

		// For Makers Mapping
		dealerRegDTO.getMakers().stream().forEach(val -> {
			DealerMakerDTO makerDTO = new DealerMakerDTO();
			makerDTO.setrId(userDto.getRid());
			if (val.getMid() != null) {
				makerDTO.setMmId(val.getMid());
			} else {
				makerDTO.setMmId(makersDAO.findByMakername(val.getMakername()).get().getMid());
			}
			makerDTO.setStatus(Boolean.TRUE);
			makerDTO.setCreatedDate(LocalDateTime.now());
			makerDTO.setlUpdate(LocalDateTime.now());
			makersList.add(makerDTO);
		});
		dealerMakerDAO.save(makersList);
		dealerCovDAO.save(covsList);
	}

	private Integer getDealerRid(DealerRegDTO dealerRegDTO) {
		String dealerUserName;
		Map<String, String> detail = new HashMap<>();
		detail.put("officeCode", dealerRegDTO.getOfficeDetails().getOfficeNumberSeries());// regServiceDTO.getOfficeDetails().getOfficeCode()
		dealerUserName = sequencenGenerator.getSequence(Sequence.DEALERRID.getSequenceId().toString(), detail);
		return Integer.parseInt(dealerUserName);
	}

	private String getDealerUserName(DealerRegDTO dealerRegDTO) {
		String dealerUserName;
		Map<String, String> detail = new HashMap<>();
		detail.put("officeCode", dealerRegDTO.getOfficeDetails().getOfficeNumberSeries());// regServiceDTO.getOfficeDetails().getOfficeCode()
		dealerUserName = sequencenGenerator.getSequence(Sequence.DEALERID.getSequenceId().toString(), detail);
		return dealerUserName;
	}

	private void iterateOrGoToNextIndexForDealer(DealerRegDTO dealerRegDTO, ApprovalProcessFlowDTO approveDTO) {

		if (null != approveDTO.getIsFinal() && approveDTO.getIsFinal()) {

			moveActionsDetailsToActionDetailsLogsForDealer(dealerRegDTO);
			dealerRegDTO.setApplicationStatus(approveDTO.getRejectionStatus());
			moveActionDetailsintoLog(dealerRegDTO);
			clearAcionDetailsForDealer(dealerRegDTO);
			dealerRegDTO.setIsMVIDone(Boolean.FALSE);
			dealerRegistrationService.initiateApprovalProcessFlow(dealerRegDTO);
			notifications.sendNotifications(MessageTemplate.REG_REJECTED.getId(), dealerRegDTO);
		} else {
			dealerRegDTO.setCurrentIndex(approveDTO.getNextIndex());
			setCurrentRoleForDealer(dealerRegDTO);
			if (dealerRegDTO.getServiceIds() != null && dealerRegDTO.getServiceIds().stream()
					.anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId()))) {
				logger.debug("here no notification for  RCFORFINANCE");
			}
		}

	}

	private void moveActionDetailsintoLog(DealerRegDTO dealerRegDTO) {
		try {
			List<DealerActionDetailsLog> actionDetailsLog = CollectionUtils.isNotEmpty(
					dealerRegDTO.getActionDetailsLog()) ? dealerRegDTO.getActionDetailsLog() : new ArrayList<>();
			DealerActionDetailsLog actionDetails = new DealerActionDetailsLog();
			if (dealerRegDTO.getIterationCount() != null
					&& CollectionUtils.isNotEmpty(dealerRegDTO.getActionDetails())) {
				actionDetails.setIterationCount(dealerRegDTO.getIterationCount());
				actionDetails.setActionDetails(dealerRegDTO.getActionDetails());
			}
			actionDetailsLog.add(actionDetails);
		} catch (Exception e) {
			logger.error("Error occured while saving the action details log for dealer services", e);
		}
	}

	private DealerRegDTO clearAcionDetailsForDealer(DealerRegDTO dealerRegDTO) {
		dealerRegDTO.setActionDetails(null);
		dealerRegDTO.setIterationCount(0);
		dealerRegDTO.setCurrentIndex(0);
		dealerRegDTO.setCurrentRoles(null);
		dealerRegDTO.setIsMVIassigned(Boolean.FALSE);
		dealerRegDTO.setLockedDetails(null);
		dealerRegDTO.setEnclosures(null);
		return dealerRegDTO;

	}

	private void moveActionsDetailsToActionDetailsLogsForDealer(DealerRegDTO dealerRegDTO) {

		List<ActionDetailsLogDTO> actionLogs = new ArrayList<>();
		dealerRegDTO.getActionDetails().stream().forEach(actionLogs::add);
		actionDetailsLogDAO.save(actionLogs);
		actionLogs.clear();

	}

	private void incrementToNextIndexToDealer(DealerRegDTO dealerRegDTO, ActionDetails actionDetail, String role) {
		Optional<ActionDetails> parlleActionDetails = dealerRegDTO.getActionDetails().stream()
				.filter(p -> (!role.equals(p.getRole()) && p.getIndex().equals(actionDetail.getIndex()))).findFirst();
		if (!parlleActionDetails.isPresent()
				|| (parlleActionDetails.get().getIsDoneProcess()
						&& parlleActionDetails.get().getIsDoneOnlyPartially() == null)
				|| (parlleActionDetails.get().getIsDoneOnlyPartially() != null
						&& !parlleActionDetails.get().getIsDoneOnlyPartially())) {
			dealerRegDTO.setCurrentIndex(actionDetail.getNextIndex());
			setCurrentRoleForDealer(dealerRegDTO);
		}
	}

	private void setCurrentRoleForDealer(DealerRegDTO dealerRegDTO) {
		if (1 == dealerRegDTO.getIterationCount() || null != dealerRegDTO.getCurrentRoles()) {
			dealerRegDTO.setCurrentRoles(dealerRegDTO.getActionDetails().stream()
					.filter(a -> !a.getIsDoneProcess() && dealerRegDTO.getCurrentIndex().equals(a.getIndex()))
					.map(a -> a.getRole()).collect(Collectors.toCollection(LinkedHashSet::new)));
		}
	}

	private Integer getServiceIdForDealer(DealerRegDTO dealerRegDTO) {
		Set<Integer> serviceid = new HashSet<>();
		serviceid.addAll(dealerRegDTO.getServiceIds());
		if (dealerRegDTO.getFlowId() != null) {
			return dealerRegDTO.getFlowId().getId();
		}
		if (dealerRegDTO.getServiceIds().size() > 1) {
			Optional<ServiceEnum> serviceEnumOpt = ServiceEnum.getContainsMVIRequiredService(serviceid);
			if (serviceEnumOpt.isPresent()) {
				return serviceEnumOpt.get().getId();
			}
		}
		Set<Integer> serviceIds = new HashSet<>();
		dealerRegDTO.getServiceIds().forEach(id -> {
			if (!ServiceEnum.getApprovalNotRequiredService().contains(id)) {
				serviceIds.add(id);
			}
		});
		return serviceIds.iterator().next();
	}

	@Override
	public void saveScrtServices(RtaActionVO reportVo, JwtUser jwtUser, String ipAddress, String role) {

		if (StringUtils.isBlank(reportVo.getApplicationNo())) {
			logger.error("Application number not found");
			throw new BadRequestException("Application number not found");
		}
		synchronized (reportVo.getApplicationNo().intern()) {
			try {
				Optional<RegServiceDTO> optionalService = regServiceDAO
						.findByApplicationNo(reportVo.getApplicationNo());
				RegServiceDTO dto = optionalService.get();
				Optional<CitizenEnclosuresDTO> citizenEnclosuresOpt = citizenEnclosuresDAO
						.findByApplicationNoAndServiceIdsIn(dto.getApplicationNo(), dto.getServiceIds());
				if (!citizenEnclosuresOpt.isPresent()) {
					logger.error("Enclosures not found for application No [{}]", dto.getApplicationNo());
					throw new BadRequestException(
							"Enclosures not found for application [{" + dto.getApplicationNo() + "}]");
				}
				updateEnclosures(role, reportVo.getEnclosures(), citizenEnclosuresOpt.get());
				updateFlowForScrt(dto, role, jwtUser, reportVo.getStatus(), reportVo);
				dto.setlUpdate(LocalDateTime.now());
				dto.setUpdatedBy(jwtUser.getId());
				regServiceDAO.save(dto);
			} catch (Exception e) {
				logger.error("Some thing went wrong error is:", e.getMessage());
				throw new BadRequestException("Some thing went wrong error is:" + e.getMessage());
			}
		}
	}

	private void updateFlowForScrt(RegServiceDTO dto, String role, JwtUser jwtUser, StatusRegistration status,
			RtaActionVO reportVo) {
		Integer serviceId = getServiceId(dto);
		ApprovalProcessFlowDTO approvalFlowDTO = getApprovalProcessFlowDTO(role, serviceId);
		ActionDetails actionDetail = getActionDetailByRole(dto, role);
		updateActionDetailsStatus(role, jwtUser.getId(), status.getDescription(), actionDetail, reportVo);
		// Async
		reportService.saveServicesReport(dto, actionDetail);
		if (null != dto.getCurrentRoles()) {
			if (approvalFlowDTO.getIsFinal() != null && approvalFlowDTO.getIsFinal()) {
				dto.getCurrentRoles().clear();
			} else {
				dto.getCurrentRoles().remove(role);
			}
		}

		if (approvalFlowDTO.getFinalStepChecker() == null) {// this'll be execute for CCO and MVI

			incrementToNextIndex(dto, actionDetail, role);

			return;
		} // EX:.. CCO and MVI end's here

		// It'll execute for AO & RTO
		for (Map.Entry<String, String> keyValue : approvalFlowDTO.getFinalStepChecker().entrySet()) {
			ActionDetails finalActionDetail = getActionDetailByRole(dto, keyValue.getKey());
			if (!keyValue.getValue().equals(finalActionDetail.getStatus())) {
				// So this is not final need to Iterate or go to next Index
				iterateOrGoToNextIndex(dto, approvalFlowDTO);// RejectionCase
				return;
			}
		}
		permitsService.saveScrtPermit(dto);

	}

	@Override
	public void reApproveServRecod(RegServiceDTO regServiceDTO, String role) {
		Integer serviceId = getServiceId(regServiceDTO);
		ApprovalProcessFlowDTO approvalFlowDTO = getApprovalProcessFlowDTO(role, serviceId);
		finalFlowOperations(regServiceDTO, approvalFlowDTO, false);

	}

	@Override
	public void doActionAutoForServices(List<RegServiceDTO> regserviceslist) {
		for (RegServiceDTO regService : regserviceslist) {
			try {
				if (CollectionUtils.isNotEmpty(regService.getActionDetails())) {
					Set<String> roles = regService.getCurrentRoles();
					List<ActionDetails> actionDetailslist = regService.getActionDetails().stream()
							.filter(val -> val.getlUpdate() != null && val.getIsDoneProcess())
							.collect(Collectors.toList());
					if (CollectionUtils.isNotEmpty(actionDetailslist)) {
						actionDetailslist.sort((p1, p2) -> p2.getlUpdate().compareTo(p1.getlUpdate()));
						ActionDetails actionDetails = actionDetailslist.stream().findFirst().get();
						if (StatusRegistration.APPROVED.getDescription().equalsIgnoreCase(actionDetails.getStatus())) {
							autoApprovalRegServices(regService, roles);
						} else if (StatusRegistration.REJECTED.getDescription()
								.equalsIgnoreCase(actionDetails.getStatus())) {
							deemedCitizenImagesRejectedAction(regService.getApplicationNo(),
									StatusRegistration.REJECTED.getDescription());
							logMovingService.moveRegServiceToLog(regService.getApplicationNo());
							actionDetailsUpdateForDeemed(regService, StatusRegistration.REJECTED.getDescription(),
									roles);
							regService.setApplicationStatus(StatusRegistration.REJECTED);
							regService.setAutoActionDate(LocalDate.now());
							regService.setAutoActionStatus(StatusRegistration.SYSTEMAUTOREJECTED.getDescription());
							regService.setCurrentRoles(Collections.emptySet());
							regServiceDAO.save(regService);
							stagingRegServiceLogDAO.save(
									regServiceMapper.convertStagingRegServiceDetailsAutoApproval(regService, roles));
						}
					} else {
						autoApprovalRegServices(regService, roles);
					}
					roles.clear();
				}
			} catch (Exception e) {
				logMovingService.moveRegServiceToLog(regService.getApplicationNo());
				regService.setAutoActionStatus(StatusRegistration.FAILED.getDescription());
				regService.setAutoActionDate(LocalDate.now());
				if (regService.getSchedulerIssues() == null) {
					regService.setSchedulerIssues(new ArrayList<>());
				}
				regService.getSchedulerIssues().add(
						LocalDateTime.now() + ": where " + Schedulers.REGSERAUTOACTION + ", Issue: " + e.getMessage());
				regServiceDAO.save(regService);
				logger.error("Exception reg services auto approval with application no: {} Exception :{}",
						regService.getApplicationNo(), e.getMessage());
			}
			regService = null;

		}
		regserviceslist.clear();
	}

	private void autoApprovalRegServices(RegServiceDTO regServiceDTO, Set<String> roles) {
		Integer serviceId = getServiceId(regServiceDTO);
		String role = regServiceDTO.getActionDetails().stream()
				.sorted((a2, a1) -> a1.getIndex().compareTo(a2.getIndex())).findFirst().get().getRole();
		logMovingService.moveRegServiceToLog(regServiceDTO.getApplicationNo());
		ApprovalProcessFlowDTO approvalFlowDTO = getApprovalProcessFlowDTO(role, serviceId);
		regServiceDTO.setAutoActionDate(LocalDate.now());
		regServiceDTO.setAutoActionStatus(StatusRegistration.SYSTEMAUTOAPPROVED.getDescription());
		actionDetailsUpdateForDeemed(regServiceDTO, StatusRegistration.APPROVED.getDescription(), roles);
		finalFlowOperations(regServiceDTO, approvalFlowDTO, false);
		regServiceDAO.save(regServiceDTO);
		deemedCitizenImagesActions(regServiceDTO.getApplicationNo());
		stagingRegServiceLogDAO
				.save(regServiceMapper.convertStagingRegServiceDetailsAutoApproval(regServiceDTO, roles));
		approvalFlowDTO = null;
	}

	@Override
	public void mapStoppageRevocationImages(CitizenEnclosuresDTO enclosresDto, List<InputVO> map) {
		for (KeyValue<String, List<ImageEnclosureDTO>> key : enclosresDto.getEnclosures()) {
			ImageEnclosureDTO imgage = key.getValue().stream().findFirst().get();
			InputVO imgageVo = new InputVO();
			imgageVo.setName(imgage.getEnclosureName());
			imgageVo.setUrl(imagePreUrl + "?appImageDocId=" + imgage.getImageId());
			map.add(imgageVo);
		}
	}

	private void actionDetailsUpdateForDeemed(RegServiceDTO regServiceDTO, String status, Set<String> currentRoles) {
		RtaActionVO rtaActionVO = new RtaActionVO();
		rtaActionVO.setApplicationNo(regServiceDTO.getApplicationNo());
		for (ActionDetails actionDetailsByRole : deemedActionRoles(regServiceDTO)) {
			ActionDetails actionDetail = getActionDetailByRole(regServiceDTO, actionDetailsByRole.getRole());
			updateActionDetailsStatus(actionDetailsByRole.getRole(), "SYSTEM", status, actionDetail, rtaActionVO);
		}
	}

	private void deemedCitizenImagesRejectedAction(String applicationNo, String status) {
		Optional<CitizenEnclosuresDTO> citizenEnclosuresOptional = citizenEnclosuresDAO
				.findByApplicationNo(applicationNo);
		if (citizenEnclosuresOptional.isPresent()) {

			List<KeyValue<String, List<ImageEnclosureDTO>>> allEnclosures = citizenEnclosuresOptional.get()
					.getEnclosures();
			allEnclosures.forEach(enc -> {
				enc.getValue().forEach(value -> {

					if (value.getImageActions() == null) {
						ImageActionsDTO actiondto = new ImageActionsDTO();
						actiondto.setAction(StatusRegistration.APPROVED.getDescription());
						actiondto.setActionDatetime(LocalDateTime.now());
						actiondto.setName("SYSTEM");
						actiondto.setRole("SYSTEM");
						actiondto.setComments("System auto approved");
						enc.getValue().stream().forEach(dat -> {
							List<ImageActionsDTO> imageActions = new ArrayList<>();
							imageActions.add(actiondto);
							dat.setImageStaus(StatusRegistration.APPROVED.getDescription());
						});

					}
					if (null != value.getImageActions()) {
						List<ImageActionsDTO> imageActionsList = value.getImageActions().stream()
								.filter(val -> val.getActionDatetime() != null).collect(Collectors.toList());
						imageActionsList.sort((a, b) -> b.getActionDatetime().compareTo(a.getActionDatetime()));
						if (CollectionUtils.isNotEmpty(imageActionsList)) {
							enc.getValue().stream().forEach(dat -> {
								dat.setImageActions(dat.getImageActions());
								List<ImageActionsDTO> previousImageActions = dat.getImageActions();
								ImageActionsDTO actiondto = new ImageActionsDTO();
								String latestAction = imageActionsList.stream().findFirst().get().getAction();
								actiondto.setAction(latestAction);
								actiondto.setActionDatetime(LocalDateTime.now());
								actiondto.setName("SYSTEM");
								actiondto.setRole("SYSTEM");
								actiondto.setComments("System auto " + latestAction);
								previousImageActions.add(actiondto);
								dat.setImageStaus(latestAction);
							});
						}
					}
				});
			});

//			
//			
//			
//			
//			
//			
//			
//			if (citizenEnclosuresOptional.get().getEnclosures() != null) {
//				for (KeyValue<String, List<ImageEnclosureDTO>> keyValue : citizenEnclosuresOptional
//						.get().getEnclosures()) {
//					ImageActionsDTO actiondto = new ImageActionsDTO();
//					List<ImageEnclosureDTO> encValue = keyValue.getValue();
//					List<ImageEnclosureDTO> rejectedList = encValue.stream()
//							.filter(val -> CollectionUtils.isNotEmpty(val.getImageActions()) && val
//									.getImageActions().stream()
//									.anyMatch(image -> image.getActionDatetime() != null
//											&& image.getAction().equals(
//													StatusRegistration.REJECTED.getDescription())))
//							.collect(Collectors.toList());
//					
//					List<ImageEnclosureDTO> approvedList = encValue.stream()
//							.filter(val -> CollectionUtils.isNotEmpty(val.getImageActions()) && val
//									.getImageActions().stream()
//									.anyMatch(image -> image.getActionDatetime() != null
//											&& image.getAction().equals(
//													StatusRegistration.APPROVED.getDescription())))
//							.collect(Collectors.toList());
//					if (CollectionUtils.isNotEmpty(rejectedList)) {
//						actiondto.setAction(StatusRegistration.REJECTED.getDescription());
//						actiondto.setRole("SYSTEM");
//						actiondto.setActionDatetime(LocalDateTime.now());
//						for (ImageEnclosureDTO imageEnclosure : rejectedList) {
//							imageEnclosure
//									.setImageStaus(status);
//							List<ImageActionsDTO> prevActions = imageEnclosure.getImageActions();
//							prevActions.add(actiondto);
//							imageEnclosure.setImageActions(prevActions);
//						}
//					}
//					if (CollectionUtils.isNotEmpty(approvedList)) {
//						actiondto.setAction(StatusRegistration.APPROVED.getDescription());
//						actiondto.setRole("SYSTEM");
//						actiondto.setActionDatetime(LocalDateTime.now());
//						for (ImageEnclosureDTO imageEnclosure : approvedList) {
//							imageEnclosure
//									.setImageStaus(StatusRegistration.APPROVED.getDescription());
//					List<ImageActionsDTO> prevActions = imageEnclosure.getImageActions();
//					prevActions.add(actiondto);
//					imageEnclosure.setImageActions(prevActions);
//						}
//					}
//				}
//			}
			citizenEnclosuresDAO.save(citizenEnclosuresOptional.get());
		} else {
			throw new BadRequestException(
					"no record found in citizen enclosure based on applicationNo" + applicationNo);
		}
	}

	private List<ActionDetails> deemedActionRoles(RegServiceDTO regServiceDTO) {
		List<ActionDetails> actionDetailsList = regServiceDTO.getActionDetails().stream()
				.filter(done -> !done.getIsDoneProcess()).collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(actionDetailsList)) {
			return actionDetailsList;
		}
		return Collections.emptyList();
	}

	private void deemedCitizenImagesActions(String applicationNo) {
		Optional<CitizenEnclosuresDTO> citizenEnclosuresOptional = citizenEnclosuresDAO
				.findByApplicationNo(applicationNo);
		if (citizenEnclosuresOptional.isPresent() && citizenEnclosuresOptional.get().getEnclosures() != null) {
			ImageActionsDTO actiondto = new ImageActionsDTO();
			actiondto.setAction(StatusRegistration.APPROVED.getDescription());
			actiondto.setActionDatetime(LocalDateTime.now());
			actiondto.setName("SYSTEM");
			actiondto.setRole("SYSTEM");
			actiondto.setComments("System auto " + StatusRegistration.APPROVED.getDescription());
			for (KeyValue<String, List<ImageEnclosureDTO>> enclosures : citizenEnclosuresOptional.get()
					.getEnclosures()) {
				for (ImageEnclosureDTO dto : enclosures.getValue()) {
					dto.setImageStaus(StatusRegistration.APPROVED.getDescription());
					List<ImageActionsDTO> prevActions = new ArrayList<ImageActionsDTO>();
					if (!CollectionUtils.isEmpty(dto.getImageActions())) {
						prevActions.addAll(dto.getImageActions());
					}
					prevActions.add(actiondto);
					dto.setImageActions(prevActions);
				}
			}
			citizenEnclosuresDAO.save(citizenEnclosuresOptional.get());
		}
	}

	@Override
	public FreshRCActionVO frcFinancierValidation(String role, RtaActionVO rtaActionVO) {
		List<MasterFreshRcMviQuestions> frcQuestionsList = new ArrayList<>();
		FreshRCActionVO freshRCActionVO = new FreshRCActionVO();
		Optional<RegServiceDTO> regServiceDTOOptional = regServiceDAO
				.findByApplicationNo(rtaActionVO.getApplicationNo());

		if (!regServiceDTOOptional.isPresent()) {
			logger.error("No record in registration_services with applicationNo [{}]", rtaActionVO.getApplicationNo());
			throw new BadRequestException("No record in registration_services with applicationNo");
		}
		if (CollectionUtils.isNotEmpty(regServiceDTOOptional.get().getServiceIds())
				&& !regServiceDTOOptional.get().getServiceIds().contains(ServiceEnum.RCFORFINANCE.getId())) {
			throw new BadRequestException("application not related to freshRc");
		}
		if (RoleEnum.AO.getName().equals(role) && rtaActionVO.getFreshRCAction().getFrcQuestions() != null) {
			rtaActionVO.getFreshRCAction().getFrcQuestions().forEach(question -> {
				if (question.getQuestion() != null && question.getSerialNo() != null && question.getRole() != null) {
					if (question.getRole().equals(role) && question.isSelectedOption()) {
						if (question.getQuestion() != null && question.getSerialNo() != null
								&& question.isSelectedOption()) {
							frcQuestionsList.add(freshRCMapper.setQuestionsToDTO(question));
						}
						regServiceDTOOptional.get().getFreshRcdetails().setFrcQuestions(frcQuestionsList);
						freshRCActionVO.setValideFinancier(Boolean.TRUE);
					}

					if (question.getRole().equals(role) && !question.isSelectedOption()) {
						if (question.getQuestion() != null && question.getSerialNo() != null
								&& !question.isSelectedOption()) {
							frcQuestionsList.add(freshRCMapper.setQuestionsToDTO(question));
						}
						regServiceDTOOptional.get().getFreshRcdetails().setFrcQuestions(frcQuestionsList);
						freshRCMapper.convertFrcStatus(regServiceDTOOptional.get());
						freshRCActionVO.setValideFinancier(Boolean.FALSE);
					}
				} else {
					throw new BadRequestException("please provide answers for respective questions");
				}
			}

			);
		}

		if (CollectionUtils.isNotEmpty(frcQuestionsList)) {
			regServiceDTOOptional.get().getFreshRcdetails().setFrcQuestions(frcQuestionsList);
		}
		regServiceDAO.save(regServiceDTOOptional.get());

		if (regServiceDTOOptional.get().getApplicationStatus().equals(StatusRegistration.FRESHRCREJECTED)) {
			sendNotifications(MessageTemplate.FINANCIERNOTMATCH.getId(), regServiceDTOOptional.get());
		}
		return freshRCActionVO;
	}

	private RegServiceDTO saveFrcQuestions(String role, RtaActionVO rtaActionVO, RegServiceDTO regServiceDTO) {

		if (rtaActionVO.getFreshRCAction().getFrcQuestions() != null) {
			if (RoleEnum.MVI.getName().equals(role)) {
				rtaActionVO.getFreshRCAction().getFrcQuestions().forEach(question -> {
					List<MasterFreshRcMviQuestions> frcQuestionsList = null;

					frcQuestionsList = CollectionUtils.isNotEmpty(regServiceDTO.getFreshRcdetails().getFrcQuestions())
							? regServiceDTO.getFreshRcdetails().getFrcQuestions()
							: new ArrayList<>();

					if (question.getRole() != null && question.getQuestion() != null && question.getSerialNo() != null
							&& question.getRole().equals(role) && !question.getRole().isEmpty()
							&& !question.getQuestion().isEmpty() && !question.getSerialNo().isEmpty()) {
						if (question.getQuestion().equalsIgnoreCase(MessageKeys.FRESHRC_MVI_QUESTION)) {
							frcQuestionsList.add(freshRCMapper.setQuestionsToDTO(question));
							if (!question.isSelectedOption()) {
								freshRCMapper.convertFrcStatus(regServiceDTO);
							}
						}
						if (question.getQuestion().equalsIgnoreCase(MessageKeys.FRESHRC_MVI_QUESTION_FOR_FORM37)) {
							frcQuestionsList.add(freshRCMapper.setQuestionsToDTO(question));
						}
					} else {
						throw new BadRequestException("Please provide answers for respective questions");
					}
					if (CollectionUtils.isNotEmpty(frcQuestionsList)) {
						regServiceDTO.getFreshRcdetails().setFrcQuestions(frcQuestionsList);
					}
				});
			}

			if (RoleEnum.AO.getName().equals(role)) {
				rtaActionVO.getFreshRCAction().getFrcQuestions().forEach(question -> {
					List<MasterFreshRcMviQuestions> frcQuestionsListAO = null;

					frcQuestionsListAO = CollectionUtils.isNotEmpty(regServiceDTO.getFreshRcdetails().getFrcQuestions())
							? regServiceDTO.getFreshRcdetails().getFrcQuestions()
							: new ArrayList<>();

					if (question.getQuestion() != null && question.getSerialNo() != null && question.getRole() != null
							&& question.getRole().equals(role) && !question.getQuestion().isEmpty()
							&& !question.getSerialNo().isEmpty() && !question.getRole().isEmpty()) {
						frcQuestionsListAO.add(freshRCMapper.setQuestionsToDTO(question));
					} else {
						throw new BadRequestException("Please provide answers for respective questions");
					}
					if (CollectionUtils.isNotEmpty(frcQuestionsListAO)) {
						regServiceDTO.getFreshRcdetails().setFrcQuestions(frcQuestionsListAO);
					} else {
						throw new BadRequestException("Please provide answers for respective questions");
					}
				});
			}
		} else {
			throw new BadRequestException("need to provide questions and answers");
		}

		if (regServiceDTO.getApplicationStatus() != null
				&& regServiceDTO.getApplicationStatus().equals(StatusRegistration.FRESHRCREJECTED)) {
			sendNotifications(MessageTemplate.FINANCIERNOTMATCH.getId(), regServiceDTO);
		}
		return regServiceDTO;
	}

	@Override
	public List<RegServiceVO> frcMviListData(String service, String selectedRole, String user, String officeCode) {
		// TODO Auto-generated method stub
		List<RegServiceVO> regServiceVOList = new ArrayList<>();
		List<RegServiceDTO> regServiceDTOList = regServiceDAO
				.findByLockedDetailsLockedByAndLockedDetailsLockedByRole(user, selectedRole);
		if (regServiceDTOList.isEmpty()) {
			throw new BadRequestException("No records  locked by this user" + user);
		}
		regServiceDTOList = regServiceDTOList.stream().filter(
				data -> data.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId())))
				.collect(Collectors.toList());
		regServiceDTOList = regServiceDTOList.stream().filter(office -> office.getMviOfficeCode().equals(officeCode))
				.collect(Collectors.toList());
		regServiceDTOList.stream().forEach(value -> {
			regServiceVOList.add(regServiceMapper.limitedDashBoardfields(value));
		});
		return regServiceVOList;
	}

	@Override
	public List<RegServiceVO> frcRTOListData(String service, String selectedRole, String user, String officeCode) {
		// TODO Auto-generated method stub

		List<RegServiceVO> regServiceVOList = new ArrayList<>();

		List<String> serviceList = new ArrayList<>();
		serviceList.add(ServiceEnum.RCFORFINANCE.toString());
		List<String> applicationStatus = Arrays.asList(StatusRegistration.AOAPPROVED.toString(),
				StatusRegistration.AOREJECTED.toString());
		List<RegServiceDTO> regServiceDTOList = regServiceDAO
				.findByOfficeDetailsOfficeCodeAndServiceTypeInAndApplicationStatusIn(officeCode, serviceList,
						applicationStatus);
		if (regServiceDTOList.isEmpty()) {
			throw new BadRequestException("No records  locked by this user" + user);
		}

		regServiceDTOList.stream().forEach(data -> {
			if (data.getCurrentRoles() != null
					&& data.getCurrentRoles().stream().anyMatch(role -> role.equals(selectedRole))) {

				if (data.getApplicationStatus() != null
						&& Arrays.asList(StatusRegistration.AOAPPROVED, StatusRegistration.AOREJECTED)
								.contains(data.getApplicationStatus())
						&& data.getFreshRcdetails().isAOAssignedToMVI()) {
					regServiceVOList.add(regServiceMapper.limitedDashBoardfields(data));
				}

				if (data.getApplicationStatus() != null
						&& data.getApplicationStatus().equals(StatusRegistration.AOREJECTED)
						&& !data.getFreshRcdetails().isAOAssignedToMVI()) {
					regServiceVOList.add(regServiceMapper.limitedDashBoardfields(data));
				}
			}
		});

		return regServiceVOList;
	}

	@Override
	public FreshApplicationSearchVO downloadForm37AtAODashBoard(String prNo, String officeCode) {
		// TODO Auto-generated method stub
		FreshApplicationSearchVO freshApplicationSearchVO = new FreshApplicationSearchVO();
		List<ServiceEnum> serviceList = new ArrayList<>();
		serviceList.add(ServiceEnum.RCFORFINANCE);
		List<String> applicationStatus = new ArrayList<>();
		applicationStatus.add(StatusRegistration.AOAPPROVED.toString());
		applicationStatus.add(StatusRegistration.MVIREJECTED.toString());
		applicationStatus.add(StatusRegistration.MVIAPPROVED.toString());
		applicationStatus.add(StatusRegistration.APPROVED.toString());
		applicationStatus.add(StatusRegistration.RTOAPPROVED.toString());
		Optional<RegServiceDTO> regOptional = regServiceDAO.findByPrNoAndServiceTypeInAndApplicationStatusIn(prNo,
				serviceList, applicationStatus);
		if (regOptional.isPresent() && regOptional.get().getOfficeCode().equals(officeCode)
				&& regOptional.get().getOfficeDetails().getOfficeCode().equals(officeCode)) {
			freshApplicationSearchVO = freshRCMapper.convertenablefrom37record(regOptional.get());
		}

		else {
			throw new BadRequestException(
					"With entered prNo we are  unable to down load form 37,could please enter proper prNo " + prNo);
		}
		return freshApplicationSearchVO;
	}

	@Override
	public List<RegServiceVO> frcAORecordsDisplay(String service, String selectedRole, String officeCode, String id,
			String status) {
		// TODO Auto-generated method stub
		List<RegServiceVO> regServiceVOList = new ArrayList<>();
		List<String> serviceList = new ArrayList<>();
		serviceList.add(ServiceEnum.RCFORFINANCE.toString());
		List<String> frcStatusList = new ArrayList<>();
		List<RegServiceDTO> regList = new ArrayList<>();
		if (status != null && !status.isEmpty()) {

			if (status.contains(StatusRegistration.PAYMENTDONE.toString())) {
				frcStatusList.add(StatusRegistration.PAYMENTDONE.toString());
				frcStatusList.add(StatusRegistration.REUPLOAD.toString());
			}
			if (status.contains(StatusRegistration.MVIAPPROVED.toString())) {
				frcStatusList.add(StatusRegistration.MVIREJECTED.toString());
				frcStatusList.add(StatusRegistration.MVIAPPROVED.toString());
			}
			List<RegServiceDTO> regServiceDTOList = regServiceDAO
					.findByOfficeDetailsOfficeCodeAndServiceTypeInAndApplicationStatusIn(officeCode, serviceList,
							frcStatusList);
			if (regServiceDTOList.isEmpty()) {
				throw new BadRequestException("No records  locked by this user" + id);
			}
			regServiceDTOList.stream().forEach(data -> {
				if (data.getCurrentRoles() != null
						&& data.getCurrentRoles().stream().anyMatch(role -> role.equals(selectedRole))) {
					if (data.getLockedDetails() != null) {
						data.setLockedDetails(null);
						regList.add(data);
					}
					regServiceVOList.add(regServiceMapper.limitedDashBoardfields(data));
				}
				if (regList != null) {
					regServiceDAO.save(regList);
				}
			});
		}
		return regServiceVOList;
	}

	private Double getTax(RegistrationDetailsDTO regDto, RegServiceDTO regServiceDTO) {

		if (this.istaxExcemptionCov(regDto)) {
			return 0d;
		}

		regDto.setVehicleStoppageRevoked(Boolean.TRUE);
		// registrationDetailsDTO.setStoppageDate(regServiceDTO.getVehicleStoppageDetails().getAoOrRtostoppageDate());
		regDto.setVehicleStoppageRevokedDate(regServiceDTO.getVehicleStoppageDetails().getStoppageRevpkationDate());

		LocalDate taxUpTo = citizenTaxService.validity(TaxTypeEnum.QuarterlyTax.getDesc());
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-M-yyyy");
		String date1 = "01-" + regServiceDTO.getVehicleStoppageDetails().getStoppageRevpkationDate().getMonthValue()
				+ "-" + String.valueOf(regServiceDTO.getVehicleStoppageDetails().getStoppageRevpkationDate().getYear());
		LocalDate localDate = LocalDate.parse(date1, formatter);
		long totalMonthsStopage = ChronoUnit.MONTHS.between(localDate, taxUpTo);
		totalMonthsStopage = totalMonthsStopage + 1;
		if (regServiceDTO.getVehicleStoppageDetails().getStoppageDate().getMonthValue() == regServiceDTO
				.getVehicleStoppageDetails().getStoppageRevpkationDate().getMonthValue()
				&& regServiceDTO.getVehicleStoppageDetails().getStoppageDate().getYear() == regServiceDTO
						.getVehicleStoppageDetails().getStoppageRevpkationDate().getYear()) {
			return 0d;
		}
		if (totalMonthsStopage == regDto.getTaxExemMonths()) {
			return 0d;
		}
		Pair<String, String> permitCodeAndRouet = this.getPermitCode(regDto);
		String permitCode = permitCodeAndRouet.getFirst();
		String routeCode = permitCodeAndRouet.getSecond();

		Double oldTax = citizenTaxService.getOldCovTax(regDto.getClassOfVehicle(),
				regDto.getVehicleDetails().getSeatingCapacity(), regDto.getVehicleDetails().getUlw(),
				regDto.getVehicleDetails().getRlw(),
				regDto.getApplicantDetails().getPresentAddress().getState().getStateId(), permitCode, routeCode);
		TaxHelper lastTaxTillDate = citizenTaxService.getLastPaidTax(regDto, null, false,
				citizenTaxService.validity(TaxTypeEnum.QuarterlyTax.getDesc()), null, false,
				citizenTaxService.taxTypes(), false, false);
		if (lastTaxTillDate == null || lastTaxTillDate.getTax() == null || lastTaxTillDate.getValidityTo() == null) {
			throw new BadRequestException("TaxDetails not found");
		}
		TaxHelper tax = citizenTaxService.vehicleRevokationTaxCalculation(regDto, lastTaxTillDate, oldTax, false,
				new TaxHelper());
		return tax.getTax();
	}

	private Pair<String, String> getPermitCode(RegistrationDetailsDTO regDto) {
		String permitCode = "INA";
		String routeCode = StringUtils.EMPTY;
		Optional<MasterTaxBased> taxCalBasedOn = masterTaxBasedDAO.findByCovcode(regDto.getClassOfVehicle());
		if (!taxCalBasedOn.isPresent()) {
			logger.error("No record found in master_taxbased for: " + regDto.getClassOfVehicle());
			// throw error message
			throw new BadRequestException("No record found in master_taxbased for: " + regDto.getClassOfVehicle());
		}
		if (taxCalBasedOn.get().getBasedon().equalsIgnoreCase("S")) {
			Pair<String, String> permitTypeAndRoutType = citizenTaxService.getPermitCode(regDto);
			permitCode = permitTypeAndRoutType.getFirst();
			routeCode = permitTypeAndRoutType.getSecond();
			if (regDto.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.ARKT.getCovCode())) {
				permitCode = "INA";
				routeCode = StringUtils.EMPTY;
			}
			Optional<PropertiesDTO> propertiesOptional = propertiesDAO
					.findByCovsInAndPermitCodeTrue(regDto.getClassOfVehicle());
			if (!propertiesOptional.isPresent()) {
				permitCode = "INA";
				routeCode = StringUtils.EMPTY;
			}
		}
		return Pair.of(permitCode, routeCode);
	}

	private boolean isSaveImagesFromTemporaryFile(RegServiceDTO regServiceDTO, JwtUser jwtuser,
			StatusRegistration applicationStatus) {
		if (((regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWFC.getId()))
				|| regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RENEWALFC.getId()))
				|| regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.OTHERSTATIONFC.getId())))
				&& regServiceDTO.getServiceIds().size() == 1)
				&& applicationStatus.equals(StatusRegistration.APPROVED)) {
			if (registrationService.isallowImagesInapp(regServiceDTO.getMviOfficeCode())) {
				return true;
			}
		}
		return false;
	}

	private void saveTemporaryImagesInMainDoc(RegServiceDTO regServiceDTO, JwtUser jwtuser) {

		if (((regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWFC.getId()))
				|| regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RENEWALFC.getId()))
				|| regServiceDTO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.OTHERSTATIONFC.getId())))
				&& regServiceDTO.getServiceIds().size() == 1)) {
			if (registrationService.isallowImagesInapp(regServiceDTO.getMviOfficeCode())) {
				Optional<TemporaryEnclosuresDTO> enlosuresOptional = rtaService
						.returnTemporaryImages(regServiceDTO.getApplicationNo(), jwtuser);
				Optional<CitizenEnclosuresDTO> citizenEnclosuresDTO = citizenEnclosuresDAO
						.findByApplicationNo(regServiceDTO.getApplicationNo());
				if (!citizenEnclosuresDTO.isPresent()) {
					throw new BadRequestException("no record found in citizen enclosure based on applicationNo"
							+ regServiceDTO.getApplicationNo());
				}
				CitizenEnclosuresDTO enlosures = citizenEnclosuresDTO.get();
				ImageActionsDTO actiondto = new ImageActionsDTO();
				actiondto.setAction(StatusRegistration.APPROVED.getDescription());
				actiondto.setRole(RoleEnum.MVI.getName());
				for (KeyValue<String, List<ImageEnclosureDTO>> listImages : enlosuresOptional.get().getEnclosures()) {
					for (ImageEnclosureDTO dto : listImages.getValue()) {
						dto.setImageActions(Arrays.asList(actiondto));
					}
				}
				enlosures.getEnclosures().addAll(enlosuresOptional.get().getEnclosures());
				citizenEnclosuresDAO.save(enlosures);
				List<TemporaryEnclosuresDTO> enlosuresList = rtaService
						.returnAllTemporaryImages(regServiceDTO.getApplicationNo());
				for (TemporaryEnclosuresDTO dto : enlosuresList) {
					if (dto.getCreatedBy().equalsIgnoreCase(enlosuresOptional.get().getCreatedBy())) {
						dto.setUploaded(Boolean.TRUE);
					}
					dto.setStatus(Boolean.TRUE);
					dto.setlUpdate(LocalDateTime.now());
					dto.setUpdatedBy(jwtuser.getUsername());
					temporaryEnclosuresDAO.save(dto);
				}

			}
		}
	}

	private TaxDetailsDTO updateVoluntaryTaxTaxDetailsForDataEntry(RegServiceDTO staginDto) {
		String taxTypeVcr = null;
		LocalDate taxUpTo = LocalDate.now();
		if (staginDto.getRegistrationDetails().getVahanDetails() != null
				&& staginDto.getRegistrationDetails().getVahanDetails().getVehicleClass() != null
				&& staginDto.getRegistrationDetails().getVahanDetails().getSeatingCapacity() != null
				&& staginDto.getRegistrationDetails().getVahanDetails().getGvw() != null) {
			Optional<List<TaxTypeVO>> taxTypeVOList = infoService.taxDataEntryType(
					staginDto.getRegistrationDetails().getVahanDetails().getVehicleClass(),
					staginDto.getRegistrationDetails().getVahanDetails().getSeatingCapacity(),
					staginDto.getRegistrationDetails().getVahanDetails().getGvw().toString());
			if (taxTypeVOList.isPresent()) {
				for (TaxTypeVO taxType : taxTypeVOList.get()) {
					if (taxType.getTaxId() != null && taxType.getTaxId().equals(TaxTypeEnum.LifeTax.getCode())) {
						taxTypeVcr = TaxTypeEnum.LifeTax.getDesc(); // MTLT GCRT---
						if (staginDto.getRegistrationDetails().getPrGeneratedDate() != null) {
							taxUpTo = staginDto.getRegistrationDetails().getPrGeneratedDate().toLocalDate().minusDays(1)
									.plusYears(12);
						} else {
							taxUpTo = LocalDate.now().minusDays(1).plusYears(12);
						}
					} else {
						if (taxType.getTaxId() != null
								&& taxType.getTaxId().equals(TaxTypeEnum.QuarterlyTax.getCode())) {
							taxTypeVcr = TaxTypeEnum.QuarterlyTax.getDesc();
						}
						if (taxType.getTaxId() != null
								&& taxType.getTaxId().equals(TaxTypeEnum.HalfyearlyTax.getCode())) {
							taxTypeVcr = TaxTypeEnum.HalfyearlyTax.getDesc();
						}
						if (taxType.getTaxId() != null && taxType.getTaxId().equals(TaxTypeEnum.YearlyTax.getCode())) {
							taxTypeVcr = TaxTypeEnum.YearlyTax.getDesc();
						}
						taxUpTo = citizenTaxService.validity(taxTypeVcr);
					}
				}
			}
		}
		TaxDetailsDTO taxDetails = new TaxDetailsDTO();
		taxDetails.setApplicationNo(staginDto.getApplicationNo());
		taxDetails.setTrNo(staginDto.getTrNo());
		taxDetails.setPrNo(staginDto.getPrNo());
		taxDetails.setClassOfVehicle(staginDto.getRegistrationDetails().getClassOfVehicle());
		org.epragati.regservice.dto.TaxDetailsDTO taxDetailsDTO = staginDto.getTaxDetails();
		taxDetails.setTaxAmount(Long.parseLong(staginDto.getOtherStateVoluntaryTax().getTax()));
		taxDetails.setTaxPeriodFrom(
				DateConverters.StringLocalDate(staginDto.getOtherStateVoluntaryTax().getTaxvalidFrom()));
		taxDetails.setTaxPeriodEnd(
				DateConverters.StringLocalDate(staginDto.getOtherStateVoluntaryTax().getTaxvalidUpto()));
		taxDetails.setPaymentPeriod(taxTypeVcr);
		// Map for tax details
		List<Map<String, TaxComponentDTO>> taxDetailsList = new ArrayList<>();
		Map<String, TaxComponentDTO> taxMap = new HashMap<>();
		TaxComponentDTO tax = new TaxComponentDTO();
		tax.setTaxName(taxTypeVcr);
		tax.setAmount(Double.valueOf(staginDto.getOtherStateVoluntaryTax().getTax()));
		if (taxDetailsDTO.getPaymentDAte() == null) {
			taxDetailsDTO.setPaymentDAte(LocalDate.now());
		}
		tax.setPaidDate(DateConverters.convertLocalDateToLocalDateTime(taxDetailsDTO.getPaymentDAte()));
		tax.setValidityFrom(DateConverters.StringLocalDate(staginDto.getOtherStateVoluntaryTax().getTaxvalidFrom()));
		tax.setValidityTo(DateConverters.StringLocalDate(staginDto.getOtherStateVoluntaryTax().getTaxvalidUpto()));
		taxMap.put(taxTypeVcr, tax);
		taxDetailsList.add(taxMap);
		taxDetails.setTaxDetails(taxDetailsList);
		taxDetails.setCreatedDate(LocalDateTime.now());
		taxDetails.setTaxStatus("Active");
		return taxDetails;
	}

	@Override
	public Optional<FreshRcReassignedMVIVO> getFrcServiceReassignDetails(JwtUser jwtUser, String applicationNo,
			String role, String prNo) {
		Optional<UserDTO> userDTO = userDAO.findByUserId(jwtUser.getUsername());
		RegServiceDTO stagingDetailsDTO = new RegServiceDTO();
		FreshRcReassignedMVIVO vo = new FreshRcReassignedMVIVO();
		if (!userDTO.isPresent()) {
			throw new BadRequestException("User Details not found..!");
		}
		/*
		 * if(!(RoleEnum.RTO.getName().equals(role))&&!(userDTO.get().getAdditionalRoles
		 * ().contains(RoleEnum.RTO.getName())||userDTO.get().getPrimaryRole().equals(
		 * RoleEnum.RTO.getName()))) { throw new
		 * BadRequestException("Rto only have authority to access..!"); } RegServiceVO
		 * vo = regServiceMapper.convertEntity(stagingDetailsDTO);
		 */
		if (!StringUtils.isEmpty(applicationNo)) {
			stagingDetailsDTO = regServiceDAO.findOne(applicationNo);
		} else {
			stagingDetailsDTO = regServiceDAO.findByPrNoOrderByCreatedDateDesc(prNo).get();
		}
		if (stagingDetailsDTO == null) {
			throw new BadRequestException("Application not found ");
		}
		if (!(userDTO.get().getOffice().getOfficeCode()
				.equals(stagingDetailsDTO.getFreshRcdetails().getYardAddress().getMandal().getTransportOfice()))) {
			throw new BadRequestException("Login User not related MVI ASSIGNED office : "
					+ stagingDetailsDTO.getFreshRcdetails().getYardAddress().getMandal().getTransportOfice());
		}
		vo = freshRCReassignMapper.convertEntity(stagingDetailsDTO);
		if (vo.getLockedDetails().isEmpty()
				&& !(vo.getApplicationStatus().equals(StatusRegistration.AOAPPROVED.getDescription())
						|| vo.getApplicationStatus().equals(StatusRegistration.RTOAPPROVED.getDescription()))) {
			throw new BadRequestException("Application not assigned to MVI. please check application status..! ");
		}
		if (vo.isMviDone()) {
			throw new BadRequestException("MVI action already done. please check application status..! ");
		}
		if (!(vo.getLockedDetails().stream().anyMatch(a -> a.getLockedByRole().equals(RoleEnum.MVI.getName())))) {
			throw new BadRequestException("Application not yet MVI Level,it is in another Level ...!");
		}
		if (!(userDTO.get().getOffice().getOfficeCode().equals(vo.getYardAddress().getMandal().getTransportOfice()))) {
			throw new BadRequestException("Reassigned MVI possiable at yard located RTA office RTO level only"
					+ stagingDetailsDTO.getFreshRcdetails().getYardAddress().getMandal().getTransportOfice());
		}
		if (!(vo.getServiceIds().contains(ServiceEnum.RCFORFINANCE.getId()))) {
			throw new BadRequestException("application not a Fresh Rc application!" + vo.getApplicationNo());
		}
		return Optional.of(vo);

	}

	@Override
	public String reAssignMVIforFreshRC(FreshRcReassignedMVIVO freshRcReassignedMVIVO, JwtUser jwtUser) {
		RegServiceDTO stagingDetailsDTO = new RegServiceDTO();
		if (StringUtils.isEmpty(freshRcReassignedMVIVO.getNewMVIname())) {
			throw new BadRequestException(
					"Please provide MVI user id for application: " + freshRcReassignedMVIVO.getApplicationNo());
		}
		if (StringUtils.isEmpty(freshRcReassignedMVIVO.getApplicationNo())) {
			throw new BadRequestException("Please provide application: " + freshRcReassignedMVIVO.getApplicationNo());
		}
		stagingDetailsDTO = regServiceDAO.findOne(freshRcReassignedMVIVO.getApplicationNo());
		Optional<UserDTO> logInuserDto = userDAO.findByUserId(jwtUser.getUsername());
		if (stagingDetailsDTO == null) {
			throw new BadRequestException("Application not found ");
		}

		if (stagingDetailsDTO.getLockedDetails() == null
				&& !(stagingDetailsDTO.getApplicationStatus().equals(StatusRegistration.AOAPPROVED.getDescription())
						|| stagingDetailsDTO.getApplicationStatus().equals(StatusRegistration.RTOAPPROVED.getDescription()))) {
			throw new BadRequestException("Application not assigned to MVI..! ");
		}
		if (!(stagingDetailsDTO.getLockedDetails().stream()
				.anyMatch(a -> a.getLockedByRole().equals(RoleEnum.MVI.getName())))) {
			throw new BadRequestException("Application not yet MVI Level,it is in another Level ...!");
		}
		if (stagingDetailsDTO.isMviDone()) {
			throw new BadRequestException("MVI action already done. please check application status..! ");
		}
		if (stagingDetailsDTO.getLockedDetails().stream()
				.anyMatch(dto -> dto.getLockedBy().equals(freshRcReassignedMVIVO.getNewMVIname()))) {
			throw new BadRequestException("already same user Assigned : " + freshRcReassignedMVIVO.getNewMVIname());
		}
		Optional<UserDTO> userDto = userDAO.findByUserId(freshRcReassignedMVIVO.getNewMVIname());
		if (userDto == null || !userDto.isPresent()) {
			throw new BadRequestException(
					"No user details found for user name: " + freshRcReassignedMVIVO.getNewMVIname());
		}
		if (!userDto.get().getPrimaryRole().getName().equalsIgnoreCase(RoleEnum.MVI.getName())
				&& !userDto.get().getAdditionalRoles().stream()
						.anyMatch(name -> name.getName().equalsIgnoreCase(RoleEnum.MVI.getName()))) {
			throw new BadRequestException(
					"Given user dont have MVI role. user id: " + freshRcReassignedMVIVO.getNewMVIname());
		}

		if (!(stagingDetailsDTO.getServiceIds().contains(ServiceEnum.RCFORFINANCE.getId()))) {
			throw new BadRequestException(
					"application not a Fresh Rc application!" + stagingDetailsDTO.getApplicationNo());
		}
		if (!(logInuserDto.get().getOffice().getOfficeCode()
				.equals(stagingDetailsDTO.getFreshRcdetails().getYardAddress().getMandal().getTransportOfice()))) {
			throw new BadRequestException("Reassigned MVI possiable at yard located RTA office RTO level only : "
					+ stagingDetailsDTO.getFreshRcdetails().getYardAddress().getMandal().getTransportOfice());
		}

		FreshRCReassignMviLogDTO freshRCReassignMviLogDTO = new FreshRCReassignMviLogDTO();
		freshRCReassignMviLogDTO.setApplicationNo(stagingDetailsDTO.getApplicationNo());
		freshRCReassignMviLogDTO.setPrNo(stagingDetailsDTO.getPrNo());
		freshRCReassignMviLogDTO.setOldlockedDetails(stagingDetailsDTO.getLockedDetails());
		freshRCReassignMviLogDTO.setOldOfficeDetails(stagingDetailsDTO.getMviOfficeDetails());
		freshRCReassignMviLogDTO.setActionDoneUserId(jwtUser.getUsername());
		freshRCReassignMviLogDTO.setRemarks(freshRcReassignedMVIVO.getRemarks());
		OfficeDTO reassign = new OfficeDTO();
		reassign = officeDAO.findByOfficeCode(jwtUser.getOfficeCode()).get();
		freshRCReassignMviLogDTO.setReassignedOfficeDetails(reassign);
		List<LockedDetailsDTO> lockedDetailsLog = new ArrayList<>();
		LockedDetailsDTO lockedDetail = new LockedDetailsDTO();
		lockedDetail.setApplicatioNo(stagingDetailsDTO.getApplicationNo());
		lockedDetail.setIterationNo(stagingDetailsDTO.getIterationCount());
		lockedDetail.setLockedDate(LocalDateTime.now());
		lockedDetail.setModule(ServiceEnum.RCFORFINANCE.getDesc());
		lockedDetail.setLockedByRole(RoleEnum.MVI.getName());
		lockedDetail.setLockedBy(userDto.get().getUserId());
		lockedDetailsLog.add(lockedDetail);
		stagingDetailsDTO.setLockedDetails(lockedDetailsLog);
		freshRCReassignMviLogDTO.setActionDoneDate(LocalDate.now());
		freshRCReassignMviDAO.save(freshRCReassignMviLogDTO);
		regServiceDAO.save(stagingDetailsDTO);
		return "Successfully reassigned user : " + userDto.get().getUserId();
	}
	@Override
	public RegServiceVO withOutAadharListData(String applicationNo,String selectedRole, String userId, String officeCode) {
		Optional<RegServiceDTO> regServiceVOList = null;
		RegServiceVO RegServiceVO = null;
		Integer currentIndex = 0;
		List<String> applicationStatus = new ArrayList<String>();
		
		
		Optional<PropertiesDTO> props = propertiesDAO.findByIsWithoutAadharTrue();
		
		if (!props.isPresent()) {
			throw new BadRequestException("No records Found for WithOutAadhar");
		}
		if(selectedRole.equals(RoleEnum.CCO.getName())) {
			currentIndex=1;
			 applicationStatus = Arrays.asList(StatusRegistration.PAYMENTDONE.toString());
			 regServiceVOList = regServiceDAO.
					  findByApplicationNoAndOfficeCodeAndCurrentIndexAndApplicationStatusIn(applicationNo,officeCode,currentIndex,applicationStatus);
				if (!regServiceVOList.isPresent()) {
					throw new BadRequestException("No records  locked by this user" + userId);
				}
				//regServiceDTOList.sort((p1, p2) -> p1.getCreatedDate().compareTo(p2.getCreatedDate()));
				//regServiceVOList=regServiceDTOList.get();
				
				if(regServiceVOList.get().getCurrentRoles() !=null &&regServiceVOList.get().getCurrentRoles().stream().anyMatch(role -> role.equals(selectedRole))){
					if (regServiceVOList.get().getApplicationStatus() != null
							&& regServiceVOList.get().getApplicationStatus().equals(StatusRegistration.PAYMENTDONE)) {
						if(regServiceVOList.get().getTransactionType()!=null) {
							RegServiceVO= regServiceMapper.limitedDashBoardfields(regServiceVOList.get());
						}
					}
				}
		}
		if(selectedRole.equals(RoleEnum.AO.getName())) {
			currentIndex=2;	
			 applicationStatus = Arrays.asList(StatusRegistration.PAYMENTDONE.toString(),
					 StatusRegistration.CCOAPPROVED.toString());
			 regServiceVOList = regServiceDAO.
					  findByApplicationNoAndOfficeCodeAndCurrentIndexAndApplicationStatusIn(applicationNo,officeCode,currentIndex,applicationStatus);
				if (!regServiceVOList.isPresent()) {
					throw new BadRequestException("No records  locked by this user" + userId);
				}
				
				if(regServiceVOList.get().getCurrentRoles() !=null &&regServiceVOList.get().getCurrentRoles().stream().anyMatch(role -> role.equals(selectedRole))){
					//String status = regServiceVOList.get().getApplicationStatus().toString();
					if (regServiceVOList.get().getApplicationStatus() != null) {
						StatusRegistration status= regServiceVOList.get().getApplicationStatus();
						if(applicationStatus.stream().anyMatch(status1 -> status1.equals(status.toString()))) {
							
							if(regServiceVOList.get().getTransactionType()!=null) {
								RegServiceVO= regServiceMapper.limitedDashBoardfields(regServiceVOList.get());
							}
					}
							
						
					}
				}
		}
		if(selectedRole.equals(RoleEnum.MVI.getName())) {
			currentIndex=4;	
			 applicationStatus = Arrays.asList(StatusRegistration.PAYMENTDONE.toString(),
					 StatusRegistration.AOAPPROVED.toString(),
					 StatusRegistration.AOREJECTED.toString());
			 regServiceVOList = regServiceDAO.
					  findByApplicationNoAndOfficeCodeAndCurrentIndexAndApplicationStatusIn(applicationNo,officeCode,currentIndex,applicationStatus);
				if (!regServiceVOList.isPresent()) {
					throw new BadRequestException("No records  locked by this user" + userId);
				}
				
				if(regServiceVOList.get().getCurrentRoles() !=null &&regServiceVOList.get().getCurrentRoles().stream().anyMatch(role -> role.equals(selectedRole))){
					if (regServiceVOList.get().getApplicationStatus() != null) {
						StatusRegistration status= regServiceVOList.get().getApplicationStatus();
						if(applicationStatus.stream().anyMatch(status1 -> status1.equals(status.toString()))) {
							
							if(regServiceVOList.get().getTransactionType()!=null) {
								RegServiceVO= regServiceMapper.limitedDashBoardfields(regServiceVOList.get());
							}
					}
							
						
					}
				}	
		}
		if(selectedRole.equals(RoleEnum.RTO.getName())) {
			currentIndex=3;
			 applicationStatus = Arrays.asList(StatusRegistration.PAYMENTDONE.toString(),
					 StatusRegistration.AOAPPROVED.toString(),
					 StatusRegistration.AOREJECTED.toString());
			 regServiceVOList = regServiceDAO.
					  findByApplicationNoAndOfficeCodeAndCurrentIndexAndApplicationStatusIn(applicationNo,officeCode,currentIndex,applicationStatus);
				if (!regServiceVOList.isPresent()) {
					throw new BadRequestException("No records  locked by this user" + userId);
				}
				
				if(regServiceVOList.get().getCurrentRoles() !=null &&regServiceVOList.get().getCurrentRoles().stream().anyMatch(role -> role.equals(selectedRole))){
					if (regServiceVOList.get().getApplicationStatus() != null) {
						StatusRegistration status= regServiceVOList.get().getApplicationStatus();
						if(applicationStatus.stream().anyMatch(status1 -> status1.equals(status.toString()))) {
							
							if(regServiceVOList.get().getTransactionType()!=null) {
								RegServiceVO= regServiceMapper.limitedDashBoardfields(regServiceVOList.get());
							}
					}
							
						
					}
					
				}
}
		return RegServiceVO;
	}
}
