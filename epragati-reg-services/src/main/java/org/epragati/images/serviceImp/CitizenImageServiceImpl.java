package org.epragati.images.serviceImp;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.aadhaarseeding.vo.AadhaarSeedVO;
import org.epragati.common.dto.aadhaar.seed.AadhaarSeedDTO;
import org.epragati.constants.AlterationTypeEnum;
import org.epragati.constants.DuplicateReasonEnum;
import org.epragati.constants.EnclosureType;
import org.epragati.constants.MessageKeys;
import org.epragati.constants.OwnerType;
import org.epragati.constants.OwnerTypeEnum;
import org.epragati.constants.TransferType;
import org.epragati.dao.enclosure.CitizenEnclosuresDAO;
import org.epragati.dto.enclosure.CitizenEnclosuresDTO;
import org.epragati.dto.enclosure.ImageEnclosureDTO;
import org.epragati.exception.BadRequestException;
import org.epragati.images.service.CitizenImageService;
import org.epragati.images.vo.ImageInput;
import org.epragati.master.dao.DealerRegDAO;
import org.epragati.master.dao.EnclosuresDAO;
import org.epragati.master.dao.MasterFreshrcMviQuestionsDAO;
import org.epragati.master.dao.RegServiceDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.StagingRegistrationDetailsDAO;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dto.DealerRegDTO;
import org.epragati.master.dto.Enclosures;
import org.epragati.master.dto.EnclosuresDTO;
import org.epragati.master.dto.MasterFreshRcMviQuestions;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.mappers.DealerRegMapper;
import org.epragati.master.mappers.EnclosuresMapper;
import org.epragati.master.vo.DealerRegVO;
import org.epragati.master.vo.MasterFreshrcMviQuestionsVO;
import org.epragati.payment.dto.PaymentTransactionDTO;
import org.epragati.payments.dao.PaymentTransactionDAO;
import org.epragati.permits.dao.PermitDetailsDAO;
import org.epragati.permits.dto.PermitDetailsDTO;
import org.epragati.regservice.RegistrationService;
import org.epragati.regservice.dao.AadhaarSeedDAO;
import org.epragati.regservice.dto.ActionDetails;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.mapper.AadhaarSeedMapper;
import org.epragati.regservice.mapper.FreshRCMapper;
import org.epragati.regservice.mapper.RegServiceMapper;
import org.epragati.regservice.vo.RegServiceVO;
import org.epragati.reports.service.ReportService;
import org.epragati.rta.service.impl.DTOUtilService;
import org.epragati.rta.service.impl.service.RegistratrionServicesApprovals;
import org.epragati.service.enclosure.mapper.EnclosureImageMapper;
import org.epragati.service.enclosure.vo.CitizenImagesInput;
import org.epragati.service.enclosure.vo.DisplayEnclosures;
import org.epragati.service.enclosure.vo.EnclosureRejectedVO;
import org.epragati.service.enclosure.vo.EnclosureSupportedVO;
import org.epragati.service.enclosure.vo.ImageVO;
import org.epragati.service.files.GridFsClient;
import org.epragati.util.ApplicantTypeEnum;
import org.epragati.util.PermitsEnum;
import org.epragati.util.RoleEnum;
import org.epragati.util.Status.AadhaarSeedStatus;
import org.epragati.util.StatusRegistration;
import org.epragati.util.document.KeyValue;
import org.epragati.util.payment.ServiceEnum;
import org.epragati.util.payment.ServiceEnum.Flow;
import org.epragati.util.payment.TOServiceEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.epragati.service.enclosure.vo.CitizenEnclosersVO;
import com.google.zxing.WriterException;
@Service
public class CitizenImageServiceImpl implements CitizenImageService {

	private static final Logger logger = LoggerFactory.getLogger(CitizenImageServiceImpl.class);

	@Autowired
	private EnclosuresDAO enclosuresDAO;

	@Autowired
	private EnclosureImageMapper enclosureImageMapper;

	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;

	@Autowired
	private RegServiceDAO regServiceDAO;

	@Autowired
	private RegistrationService reService;

	@Autowired
	private GridFsClient gridFsClient;

	@Autowired
	private DTOUtilService dTOUtilService;

	@Autowired
	private RegServiceMapper regServiceMapper;

	@Autowired
	private CitizenEnclosuresDAO citizenEnclosuresDAO;

	@Autowired
	private RegistratrionServicesApprovals registratrionServicesApprovals;

	@Autowired
	private PaymentTransactionDAO paymentTransactionDAO;

	@Autowired
	private RegistrationService registrationService;

	@Autowired
	private AadhaarSeedDAO aadhaarSeedDAO;

	@Autowired
	private AadhaarSeedMapper aadhaarSeedMapper;

	@Autowired
	private ReportService reportService;
	
	@Autowired
	private PermitDetailsDAO permitDetailsDAO;
	
	@Autowired
	private EnclosuresMapper enclosuresMapper;
	
	@Autowired
	private MasterFreshrcMviQuestionsDAO  frcQuestionsDAO;
	
	
	@Autowired
	private FreshRCMapper frcMapper;
	
	@Autowired
	private UserDAO userdao;
	
	@Autowired
	private StagingRegistrationDetailsDAO stagingRegistrationDetailsDAO;

	@Override
	public List<EnclosureRejectedVO> getCitizenSupportedEnclosures(CitizenImagesInput input) {

		logger.debug("Start of getListOfSupportedEnclosureDetails()...");
		Map<String, List<ImageVO>> typeToVOs = new HashMap<>();
		Optional<RegistrationDetailsDTO> regDetails = Optional.empty();
		List<EnclosuresDTO> dtos =null;
		if(input.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId())) && input.getOwnerType() != null&&
				input.getOwnerType().equals(OwnerType.BUYER)&&input.getTransferType()!=null &&input.getTransferType().equals(TransferType.AUCTION)) {
			dtos = enclosuresDAO.findByServiceIDInAndApplicantType(input.getServiceIds(),
					ApplicantTypeEnum.AUCTION);
		}else {
			dtos =enclosuresDAO.findByServiceIDInAndApplicantType(input.getServiceIds(),
					ApplicantTypeEnum.CITIZEN);
		}
		/*
		 * if (input.getToEnclouserStatus() != null && input.getToEnclouserStatus() > 0
		 * && input.getToEnclouserStatus() <= 2) { EnclosuresDTO dto =
		 * enclosuresDAO.findByServiceIDAndSlNo(TOServiceEnum.TOFINANCIER.getId(),
		 * input.getToEnclouserStatus()); dtos.add(dto); }
		 */
		if(input.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGE.getId()))||
				input.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId()))) {
			return null;
		}
		if (input.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))) {
			for (EnclosuresDTO dto : dtos) {
				/*if(input.){
					
				}*/
				typeToVOs.put(dto.getProof(), enclosureImageMapper.convertdtoToVo(dto));
			}
			List<EnclosureRejectedVO> enclosures = typeToVOs.keySet().stream()
					.map(k -> new EnclosureRejectedVO(k, typeToVOs.get(k))).collect(Collectors.toList());
			return enclosures;
		}
		if (input.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.AADHARSEEDING.getId()))) {
			Optional<AadhaarSeedDTO> aadhaarSeedDTO = aadhaarSeedDAO.findById(input.getApplicationNo());

			if (aadhaarSeedDTO.isPresent() && aadhaarSeedDTO.get().getEnclosures() != null) {
				if (aadhaarSeedDTO.get().getStatus().equals(AadhaarSeedStatus.APPROVED)
						|| aadhaarSeedDTO.get().getStatus().equals(AadhaarSeedStatus.REJECTED)) {
					throw new BadRequestException("Application is already approved");
				}
				List<EnclosureRejectedVO> enclousers = new ArrayList<>();
				EnclosureRejectedVO enclouserVo = new EnclosureRejectedVO();
				AadhaarSeedVO aadhaarSeed = aadhaarSeedMapper.convertRequiredEnclousersEntity(aadhaarSeedDTO.get());
				enclouserVo.setMobileUploaded(Boolean.TRUE);
				enclouserVo.setImgUrls(aadhaarSeed);
				enclousers.add(enclouserVo);
				getQrCode(input, enclousers);
				return enclousers;
			}

		}
		if (input.getPrNo() != null) {
			regDetails = registrationDetailDAO.findByPrNo(input.getPrNo());
		}

		if (null != input.getPrNo() && !regDetails.isPresent()) {
			logger.error("No record found for prNo..[{}]", input.getPrNo());
			throw new BadRequestException("No record found for prNo..[{}]" + input.getPrNo());
		}

		// List<EnclosureType> enclosuresTypes = null;
		// boolean uploadStatus = false;
		Set<EnclosureRejectedVO> set = new LinkedHashSet<EnclosureRejectedVO>();
		if (dtos.isEmpty()) {
			throw new BadRequestException("Enclosures  is not found.");
		}
		dtos.sort((p1, p2) -> p1.getSlNo().compareTo(p2.getSlNo()));

		for (EnclosuresDTO dto : dtos) {

			if (!checkImagesApplicableOrNot(dto, (regDetails.isPresent() ? regDetails.get() : null), input)) {
				continue;
			}
			if (dto.isRequired()) {
				// uploadStatus = true;
			}
			typeToVOs.put(dto.getProof(), enclosureImageMapper.convertdtoToVo(dto));
		}
		List<EnclosureRejectedVO> enclosures = typeToVOs.keySet().stream()
				.map(k -> new EnclosureRejectedVO(k, typeToVOs.get(k))).collect(Collectors.toList());
		enclosures.sort((p1, p2) -> p1.getSlNo().compareTo(p2.getSlNo()));
		if (input.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.AADHARSEEDING.getId()))) {
			getQrCode(input, enclosures);
		}
		set.addAll(enclosures);
		enclosures.clear();
		enclosures.addAll(set);
		return enclosures;
	}

	private boolean checkImagesApplicableOrNot(EnclosuresDTO dto, RegistrationDetailsDTO regdetails,
			CitizenImagesInput input) {
		if ( null!=regdetails  && (regdetails.getOwnerType().equals(OwnerTypeEnum.Government)||regdetails.getOwnerType().equals(OwnerTypeEnum.POLICE)
				||regdetails.getOwnerType().equals(OwnerTypeEnum.Stu))) {
			if (dto.getProof().equalsIgnoreCase(EnclosureType.Insurance.getValue())) {
				return Boolean.FALSE;
			}
		}
		if (input.getServiceIds().contains(ServiceEnum.ISSUEOFNOC.getId())
				&& input.getServiceIds().contains(ServiceEnum.TRANSFEROFOWNERSHIP.getId())) {

			if (dto.getProof().equalsIgnoreCase(EnclosureType.FINANCECOPY.getValue())) {
				return Boolean.FALSE;
			}
		}
		if (input.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.HIREPURCHASETERMINATION.getId()))) {
			if(input.getIsFinanceContinue()){
				if (dto.getProof().equalsIgnoreCase(EnclosureType.Form35.getValue())) {
					return Boolean.FALSE;
				}
				if (dto.getProof().equalsIgnoreCase(EnclosureType.FINANCIERNOC.getValue())) {
					return Boolean.TRUE;
				}
			}
			if (regdetails.getFinanceDetails() != null && regdetails.getFinanceDetails().getUserId() != null
					&& registrationService.isOnlineFinance(regdetails.getFinanceDetails().getUserId())) {
				if (dto.getProof().equalsIgnoreCase(EnclosureType.Form35.getValue())) {
					return Boolean.FALSE;
				}
			}
			if (dto.getProof().equalsIgnoreCase(EnclosureType.FINANCIERNOC.getValue())) {
				return Boolean.FALSE;
			}
		}
		if (input.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.DUPLICATE.getId()))) {
			if (input.getDuplicateReasonEnum() != null) {
				if (input.getDuplicateReasonEnum().equals(DuplicateReasonEnum.LOST)) {
					if (dto.getProof().equalsIgnoreCase(EnclosureType.RC1.getValue())) {
						return Boolean.FALSE;
					}
					if (dto.getProof().equalsIgnoreCase(EnclosureType.RC2.getValue())) {
						return Boolean.FALSE;
					}
				}
				if (input.getDuplicateReasonEnum().equals(DuplicateReasonEnum.TORNOUT)) {
					if (dto.getProof().equalsIgnoreCase(EnclosureType.FIR.getValue())) {
						return Boolean.FALSE;
					}
				}
			}
		}

		if (input.getIsSameAsAadhar() != null && input.getIsSameAsAadhar()) {
			if (dto.getProof().equalsIgnoreCase(EnclosureType.AddrBackView.getValue())
					|| dto.getProof().equalsIgnoreCase(EnclosureType.AddrFrontview.getValue())) {
				return Boolean.FALSE;
			}
		}
		if (input.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId()))) {
			// List<RegServiceDTO> regList = regServiceDAO.findByPrNo(input.getPrNo());

			// regList.sort((p1,p2)->p1.getCreatedDate().compareTo(p2.getCreatedDate()));
			// RegServiceDTO regServiceDetail = regList.stream().findFirst().get();
			if ((input.getToAuctionDeathId() != null
					&& TOServiceEnum.TODEATH.getId().equals(input.getToAuctionDeathId()))) {

				if (dto.getProof().equalsIgnoreCase(EnclosureType.DEATHCERTIFICATE.getValue())) {
					return Boolean.TRUE;
				}
				if (dto.getProof().equalsIgnoreCase(EnclosureType.AFFIDAVIT.getValue())) {
					return Boolean.TRUE;
				}
				if (dto.getProof().equalsIgnoreCase(EnclosureType.LEGALHEIRCERTIFICATE.getValue())) {
					return Boolean.TRUE;
				}
				if (dto.getProof().equalsIgnoreCase(EnclosureType.CONSENTLETTERFROMFAMILYMEMBERS.getValue())) {
					return Boolean.TRUE;
				}
				if (dto.getProof().equalsIgnoreCase(EnclosureType.APROFILE.getValue())) {
					return Boolean.TRUE;
				}
				if (dto.getProof().equalsIgnoreCase(EnclosureType.Insurance.getValue())) {
					if (registrationService.regInsuranceValidity(regdetails)) {
						return Boolean.TRUE;
					}
					return Boolean.FALSE;
				}
				if (dto.getProof().equalsIgnoreCase(EnclosureType.PUC.getValue())) {
					if (registrationService.regPUCDetailsValidity(regdetails)) {
						return Boolean.TRUE;
					}
					return Boolean.FALSE;
				}
				

				return toEnclousers(dto, input, regdetails);
			}
			return toEnclousers(dto, input, regdetails);
		}

		if (input.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.ALTERATIONOFVEHICLE.getId()))) {
			if (input.getAlterationService() == null
					|| !input.getAlterationService().equalsIgnoreCase(AlterationTypeEnum.FUEL.toString()))
				if (dto.getProof().equalsIgnoreCase(EnclosureType.ARAI.getValue())) {
					return Boolean.FALSE;
				}
			if (dto.getApplicantType().equals(ApplicantTypeEnum.MVI)) {
				return Boolean.FALSE;
			}
		}
		if (input.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))) {
			if (input.getRegOwnerType() != null && (!input.getRegOwnerType().equals(OwnerTypeEnum.Government)
					|| !input.getRegOwnerType().equals(OwnerTypeEnum.Company))) {
				if (dto.getProof().equalsIgnoreCase(EnclosureType.Certificate.getValue())) {
					return Boolean.FALSE;
				}
			}
			if (null != input.getIsDifferentlyAbled() && !input.getIsDifferentlyAbled()) {
				if (dto.getProof().equalsIgnoreCase(EnclosureType.PHC.getValue())) {
					return Boolean.FALSE;
				}
			}
			if (null != input.getIsRegisteredVehicle() && !input.getIsRegisteredVehicle()) {
				if (dto.getProof().equalsIgnoreCase(EnclosureType.HelmetInvoice.getValue())) {
					return Boolean.FALSE;
				}
			}

		}

		if (input.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RENEWAL.getId()))) {
			if (dto.getApplicantType().equals(ApplicantTypeEnum.MVI)) {
				return Boolean.FALSE;
			}
		}
		if (input.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.AADHARSEEDING.getId()))) {

			if (dto.getProof().equalsIgnoreCase(EnclosureType.FINANCECOPY.getValue())) {
				return Boolean.FALSE;
			}
			if (!input.isCompanyVehicle()
					&& dto.getProof().equalsIgnoreCase(EnclosureType.COMPANYREPRESENTATAIVECOPY.getValue())) {
				return Boolean.FALSE;
			}

		}
		if (input.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWFC.getId()))) {
			if (dto.getApplicantType().equals(ApplicantTypeEnum.MVI)) {
				return Boolean.FALSE;
			}
			if (!input.getFitnessOtherStation()) {
				if (dto.getProof().equalsIgnoreCase(EnclosureType.CONTRACTCOPY.getValue())) {
					return Boolean.FALSE;
				}
			}
		}
		if (null != regdetails && null != regdetails.getRegistrationValidity().getTrGeneratedDate()) {
			if (regdetails.getRegistrationValidity().getTrGeneratedDate().plusMonths(6).isAfter(LocalDate.now())) {
				if (dto.getProof().equalsIgnoreCase(EnclosureType.PUC.getValue())) {
					return Boolean.FALSE;
				}
			}
		}
		if (dto.getProof().equalsIgnoreCase(EnclosureType.FINANCECOPY.getValue())) {
			if (regdetails != null) {
				if (regdetails.getFinanceDetails() != null) {
					return Boolean.TRUE;
				}
			}
			return Boolean.FALSE;

		}
		return Boolean.TRUE;
	}

	@Override
	public Boolean reuploadEnclosures(String applicationNo, List<ImageInput> images, MultipartFile[] uploadfiles)
			throws IOException {
		Optional<RegServiceDTO> registrationDetails = reService.findByApplicationNo(applicationNo);

		if (!registrationDetails.isPresent()) {
			logger.error("Application  is not found [{}] .", applicationNo);
			throw new BadRequestException("Application  is not found.");
		}
		RegServiceDTO regDTO = registrationDetails.get();
		Boolean uploadStatus = reupload(applicationNo, regDTO, images, uploadfiles);

		return uploadStatus;

	}

	private Boolean reupload(String applicationNo, RegServiceDTO dto, List<ImageInput> images,
			MultipartFile[] uploadfiles) throws IOException {

		if (!StatusRegistration.REJECTED.equals(dto.getApplicationStatus())) {
			logger.error("Application is not rejected status [{}] .", dto.getApplicationNo());
			throw new BadRequestException("Application is not rejected status. " + dto.getApplicationNo());
		}

		Optional<CitizenEnclosuresDTO> citizenEnclosuresDTO = citizenEnclosuresDAO.findByApplicationNo(applicationNo);
		if (!citizenEnclosuresDTO.isPresent()) {
			throw new BadRequestException(
					"no record found in citizen enclosure based on applicationNo" + applicationNo);
		}
		images.forEach(imageInput -> {

			Optional<KeyValue<String, List<ImageEnclosureDTO>>> pagesOptional = getImages(applicationNo, dto,
					imageInput);

			if (pagesOptional.isPresent()) {
				KeyValue<String, List<ImageEnclosureDTO>> keyValue = pagesOptional.get();
				if (pagesOptional.get().getValue().stream().anyMatch(status -> status.getImageStaus()
						.equalsIgnoreCase(StatusRegistration.REJECTED.getDescription()))) {
					// TODO:Add equals and hashcode for KeyValue
					citizenEnclosuresDTO.get().getEnclosures().removeIf(val -> keyValue.getKey().equals(val.getKey()));

					gridFsClient.removeImages(keyValue.getValue());
					logger.debug("Image Removed : [{}]", pagesOptional.get());
				}
			}
		});
		List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = gridFsClient.convertImages(images,
				dto.getApplicationNo(), uploadfiles, StatusRegistration.REUPLOAD.getDescription());

		citizenEnclosuresDTO.get().getEnclosures().addAll(enclosures);

		if (!citizenEnclosuresDTO.get().getEnclosures().stream().anyMatch(valu -> valu.getValue().stream().anyMatch(
				status -> status.getImageStaus().equalsIgnoreCase(StatusRegistration.REJECTED.getDescription())))) {
			dto.setApplicationStatus(StatusRegistration.REUPLOAD);
		}
		if(!dto.getServiceIds().stream().anyMatch(id->id.equals(ServiceEnum.BILLATERALTAX.getId()))) {
		registratrionServicesApprovals.initiateApprovalProcessFlow(dto);
		}
		regServiceDAO.save(dto);
		citizenEnclosuresDAO.save(citizenEnclosuresDTO.get());
		return true;

	}

	public Optional<KeyValue<String, List<ImageEnclosureDTO>>> getImages(String applicationNo,
			RegServiceDTO regServiceDTO, ImageInput imageInput) {

		Optional<CitizenEnclosuresDTO> citizenEnclosuresDTO = citizenEnclosuresDAO.findByApplicationNo(applicationNo);
		if (!citizenEnclosuresDTO.isPresent()) {
			throw new BadRequestException(
					"no record found in citizen Enclosures based on applicationNo" + applicationNo);
		}
		List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = citizenEnclosuresDTO.get().getEnclosures();
		List<EnclosuresDTO> dtos = enclosuresDAO.findByServiceIDIn(regServiceDTO.getServiceIds());

		if (dtos.isEmpty()) {
			logger.debug("Enclosures  is not found in master. [master_enclosures].");
			throw new BadRequestException("Enclosures  is not found in master_enclosures.");
		}

		logger.debug("imageInput [{}]", imageInput.getType());
		for (EnclosuresDTO e : dtos) {
			logger.debug("enclosure proof[{}]", e.getProof());
		}
		Optional<EnclosuresDTO> images = dtos.stream()
				.filter(dto -> dto.getProof().equalsIgnoreCase(imageInput.getType())).findFirst();

		if (!images.isPresent()) {
			logger.debug("no image found of master type");
			return Optional.empty();
		}
		return enclosures.stream().filter(e -> images.get().getProof().equalsIgnoreCase(e.getKey())).findFirst();
	}

	/**
	 * rejected list of images for reuploading
	 * 
	 */

	@Override
	public List<EnclosureRejectedVO> getListOfRejectedEnclosures(String applicationNo) {
		Pair<Boolean, List<EnclosureRejectedVO>> enclosure = rejectedEnclosures(applicationNo,true);

		List<EnclosureRejectedVO> listOfEnclosures = enclosure.getSecond();

		/*
		 * for (EnclosureRejectedVO rejected : listOfEnclosures) { ImageVO image =
		 * rejected.getImages().stream().findFirst().get(); imageVO.add(image); } if
		 * (!imageVO.isEmpty()) { imageInput =
		 * enclosureImageMapper.convertInputToImageVO(imageVO); }
		 * inputVO.setApplicationNo(applicationNo); inputVO.setImageInput(imageInput);
		 */

		return listOfEnclosures;
	}

	private Pair<Boolean, List<EnclosureRejectedVO>> rejectedEnclosures(String applicationNo,boolean isReqApprovedImg) {
		boolean uploadStatus = false;
		List<EnclosureRejectedVO> rejectedEnclosures = new ArrayList<EnclosureRejectedVO>();
		Optional<RegServiceDTO> registrationDetails = reService.findByApplicationNo(applicationNo);
		if (!registrationDetails.isPresent()) {
			logger.error("Enclosures  is not found [{}] .", applicationNo);
			throw new BadRequestException("Application  is not found.");
		}

		Set<Integer> serviceIds = registrationDetails.get().getServiceIds();
		List<EnclosuresDTO> enclosuresList = enclosuresDAO.findByServiceIDIn(serviceIds);

		if (enclosuresList.isEmpty()) {
			logger.error("Enclosures not found for application No [{}]",applicationNo);
			throw new BadRequestException("Enclosures  is not found.");
		}
		if (!isReqApprovedImg) {
			if (!registrationDetails.get().getApplicationStatus().equals(StatusRegistration.REJECTED)) {
				logger.info(" application Status [{}]", registrationDetails.get().getApplicationStatus());
				throw new BadRequestException("Application status is not REJECTED");
			}
		}
		

		Optional<CitizenEnclosuresDTO> citizenEnclosuresOptional = citizenEnclosuresDAO
				.findByApplicationNo(applicationNo);
		if (!citizenEnclosuresOptional.isPresent()) {
			logger.error("No Enclosures found for citizen enclosures [{}]",applicationNo);
			throw new BadRequestException(
					"no record found in citizen Enclosures based on ApplicationNo" + applicationNo);
		}

		for (EnclosuresDTO dto : enclosuresList) {
			citizenEnclosuresOptional.get().getEnclosures().stream()
					.forEach(val -> val.getValue().stream().forEach(type -> {
						if (type.getImageType().equalsIgnoreCase(dto.getProof())) {
							logger.debug("Image Type found  [{}]", type.getImageType());
							
						}
					}));
		}
		Set<String> imageType = new HashSet<>();
		if (citizenEnclosuresOptional.get().getEnclosures() != null) {
			for (KeyValue<String, List<ImageEnclosureDTO>> keyValue : citizenEnclosuresOptional.get().getEnclosures()) {
				List<ImageEnclosureDTO> encValue = keyValue.getValue();
				
				List<ImageEnclosureDTO> rejectedList=new ArrayList<>();
				
				if(isReqApprovedImg) 
					rejectedList = encValue.stream().filter(
						val -> val.getImageStaus().equalsIgnoreCase(StatusRegistration.APPROVED.getDescription())).collect(Collectors.toList());
						else
							rejectedList = encValue.stream().filter(
									val -> val.getImageStaus().equalsIgnoreCase(StatusRegistration.REJECTED.getDescription())).collect(Collectors.toList());
						
				
				/*
				 * || val.getImageStaus().equalsIgnoreCase(StatusRegistration.REUPLOAD.
				 * getDescription())
				 */
					
				if (rejectedList.size() > 0 && !imageType.contains(keyValue.getKey())) {
					imageType.add(keyValue.getKey());
					uploadStatus = true;
					rejectedEnclosures
							.add(new EnclosureRejectedVO(keyValue.getValue().stream().findFirst().get().getImageType(),
									enclosureImageMapper.convertNewEntity(rejectedList)));
				}
			}
		} else {
			logger.error("No enclosures found for ApplicationNo[{}]", applicationNo);
			throw new BadRequestException("No Enclosures found for applicationNo " + applicationNo);
		}
		return Pair.of(uploadStatus, rejectedEnclosures);
	}

	public List<DisplayEnclosures> getListOfEnclosureDetails() {

		return null;

	}

	@Override
	public List<EnclosureRejectedVO> getListOfSupportedEnclosuresForService(String serviceName, String applicationNo) {

		Pair<Boolean, List<EnclosureRejectedVO>> enclosure = getListOfSupportedEnclosureDetailsByService(serviceName,
				applicationNo);
		List<EnclosureRejectedVO> listOfEnclosures = null;
		if (null != enclosure)
			logger.info("Enclosures found with application No [{}]",applicationNo);
			listOfEnclosures = enclosure.getSecond();

		return listOfEnclosures;
	}

	private Pair<Boolean, List<EnclosureRejectedVO>> getListOfSupportedEnclosureDetailsByService(String serviceType,
			String applicationNo) {

		Set<Integer> serviceId = new TreeSet<>();
		if (ServiceEnum.DATAENTRY.getCode().equals(serviceType))
			serviceId.add(ServiceEnum.DATAENTRY.getId());
		if (ServiceEnum.TEMPORARYREGISTRATION.getCode().equals(serviceType))
			serviceId.add(ServiceEnum.TEMPORARYREGISTRATION.getId());

		if (ServiceEnum.BODYBUILDER.getCode().equals(serviceType))
			serviceId.add(ServiceEnum.BODYBUILDER.getId());

		List<EnclosuresDTO> dtos = enclosuresDAO.findByServiceIDIn(serviceId);
		// List<EnclosureType> enclosuresTypes = null;
		boolean uploadStatus = false;
		List<EnclosureRejectedVO> rejectedEnclosures = new ArrayList<>();
		if (dtos.isEmpty()) {
			throw new BadRequestException("Enclosures  is not found.");
		}
		dtos.sort((p1, p2) -> p1.getSlNo().compareTo(p2.getSlNo()));

		Optional<RegServiceDTO> registrationDetails = regServiceDAO.findByApplicationNo(applicationNo);

		/*
		 * if (!registrationDetails.isPresent()) {
		 * logger.error("Enclosures  is not found [{}] ." + applicationNo); throw new
		 * BadRequestException("Application  is not found."); }
		 */
		if (!registrationDetails.isPresent() || StringUtils.isEmpty(applicationNo)) {
			return supportedEnclosureListByService(dtos, uploadStatus);

		} else {

			// DIsplay Rejected Image
			if (checkApplicationStatus(registrationDetails.get())) {
				return rejectedOrReuploadEnclosures(uploadStatus, rejectedEnclosures, registrationDetails);
			}

			/*
			 * List<ImageVO> imagesVO = null;
			 * 
			 * for (EnclosuresDTO dto : dtos) { List<ImageVO> matchedEnclosures =
			 * findAndGetUploadedImage(dto.getProof(),
			 * registrationDetails.get().getEnclosures()); if (!matchedEnclosures.isEmpty())
			 * { imagesVO = matchedEnclosures; } else {
			 * 
			 * if (!checkImagesApplicableOrNot(dto, registrationDetails)) { continue; } if
			 * (dto.isRequired()) { uploadStatus = true; } imagesVO =
			 * enclosureImageMapper.convertNewEntity(enclosureImageMapper.convertNewEntity(
			 * dto));
			 * 
			 * } if (!rejectedEnclosures.isEmpty() &&
			 * rejectedEnclosures.stream().anyMatch(en ->
			 * en.getType().equals(dto.getProof()))) { continue; } rejectedEnclosures
			 * .add(new
			 * EnclosureRejectedVO(imagesVO.stream().findFirst().get().getImageType(),
			 * imagesVO)); }
			 * 
			 * // return rejectedEnclosures; return Pair.of(uploadStatus,
			 * rejectedEnclosures);
			 */
		}
		return null;
	}

	/**
	 * @param uploadStatus
	 * @param rejectedEnclosures
	 * @param registrationDetails
	 * @return
	 */
	private Pair<Boolean, List<EnclosureRejectedVO>> rejectedOrReuploadEnclosures(boolean uploadStatus,
			List<EnclosureRejectedVO> rejectedEnclosures, Optional<RegServiceDTO> registrationDetails) {
		registrationDetails.get().getEnclosures().stream()
				.forEach(values -> values.getValue().stream().forEach(value -> {
					if (value.getImageStaus().equalsIgnoreCase(StatusRegistration.REUPLOAD.getDescription())
							|| value.getImageStaus().equalsIgnoreCase(StatusRegistration.REJECTED.getDescription())) {
						rejectedEnclosures.add(
								new EnclosureRejectedVO(values.getValue().stream().findFirst().get().getImageType(),
										enclosureImageMapper.convertNewEntity(Arrays.asList(value))));
					}
				}));

		if (registrationDetails.get().getEnclosures().stream().anyMatch(images -> images.getValue().stream().anyMatch(
				status -> status.getImageStaus().equalsIgnoreCase(StatusRegistration.REJECTED.getDescription())))) {
			uploadStatus = Boolean.TRUE;
		}
		// return rejectedEnclosures;
		return Pair.of(uploadStatus, rejectedEnclosures);
	}

	/**
	 * @param dtos
	 * @param uploadStatus
	 * @return
	 */
	private Pair<Boolean, List<EnclosureRejectedVO>> supportedEnclosureListByService(List<EnclosuresDTO> dtos,
			boolean uploadStatus) {
		Map<String, List<ImageVO>> typeToVOs = new HashMap<>();

		for (EnclosuresDTO dto : dtos) {

			/*
			 * if (!checkImagesApplicableOrNot(dto, registrationDetails)) { continue; } if
			 * (dto.isRequired()) { uploadStatus = true; }
			 */
			typeToVOs.put(dto.getProof(), enclosureImageMapper.convertdtoToVo(dto));
		}
		List<EnclosureRejectedVO> enclosures = typeToVOs.keySet().stream()
				.map(k -> new EnclosureRejectedVO(k, typeToVOs.get(k))).collect(Collectors.toList());
		enclosures.sort((p1, p2) -> p1.getSlNo().compareTo(p2.getSlNo()));
		return Pair.of(uploadStatus, enclosures);
	}

	private boolean checkApplicationStatus(RegServiceDTO regServiceDTO) {
		if (regServiceDTO.getApplicationStatus().equals(StatusRegistration.REJECTED.getDescription())
				|| regServiceDTO.getApplicationStatus().equals(StatusRegistration.REUPLOAD.getDescription())
				|| regServiceDTO.getEnclosures().stream()
						.anyMatch(images -> images.getValue().stream()
								.anyMatch(status -> status.getImageStaus()
										.equalsIgnoreCase(StatusRegistration.REJECTED.getDescription())))
				|| regServiceDTO.getEnclosures().stream()
						.anyMatch(images -> images.getValue().stream().anyMatch(status -> status.getImageStaus()
								.equalsIgnoreCase(StatusRegistration.REUPLOAD.getDescription())))) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	private List<ImageVO> findAndGetUploadedImage(String type,
			List<KeyValue<String, List<ImageEnclosureDTO>>> uploadedEnclosures) {

		Optional<KeyValue<String, List<ImageEnclosureDTO>>> matchedEnclosures = uploadedEnclosures.stream()
				.filter(arg -> arg.getValue().stream().anyMatch(res -> res.getImageType().equalsIgnoreCase(type)))
				.findFirst();
		if (!matchedEnclosures.isPresent()) {
			return new ArrayList<>();
		}

		return enclosureImageMapper.convertNewEntity(matchedEnclosures.get().getValue());

	}

	@Override
	public Enclosures getListOfEnclosureCitizenToSubmit(String applicationFormNo, String userId, String selectedRole) {

		Enclosures enc = new Enclosures();
		String roles = dTOUtilService.getRole(userId, selectedRole);
		Optional<RegServiceDTO> registrationDetails = regServiceDAO.findByApplicationNo(applicationFormNo);
		if (!registrationDetails.isPresent()) {
			logger.info("Application  is not found [{}] ." + applicationFormNo);
			throw new BadRequestException("Application  is not found. Application no :" + applicationFormNo);
		}
		// feeDetail
		if (registrationDetails.get().getServiceType().stream().allMatch(s -> ServiceEnum.isFeesRequired(s))) {
			if (!(registrationDetails.get().getRegistrationDetails().isRegVehicleWithPR()
					&& registrationDetails.get().getRegistrationDetails().isTaxPaidByVcr())) {
				if (registrationDetails.get().getRegistrationDetails().getApplicantType() == null
						|| !registrationDetails.get().getRegistrationDetails().getApplicantType()
								.equalsIgnoreCase("WITHINTHESTATE")) {
					if (registrationDetails.get().getServiceIds() != null && !(registrationDetails.get().getServiceIds()
							.stream().anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId())
									&& selectedRole.equalsIgnoreCase(RoleEnum.CCO.getName())))) {
						Optional<PaymentTransactionDTO> paymentTransactionOpt = paymentTransactionDAO
								.findByApplicationFormRefNumOrderByRequestRequestTimeDesc(applicationFormNo);
						if (!paymentTransactionOpt.isPresent()) {
							throw new BadRequestException(
									"Fee Datails not found for applicationNo " + applicationFormNo);
						}
						registrationDetails.get().setFeeDetails(paymentTransactionOpt.get().getFeeDetailsDTO());
					}
				}
			}
		}
		RegServiceVO regServiceVO = null;
		regServiceVO = regServiceMapper.limitedFieldsDTOToVO(registrationDetails.get());
		if (registrationDetails.get().getServiceIds().contains(ServiceEnum.DATAENTRY.getId())) {
			regServiceVO = regServiceMapper.convertLimitedDtoToVoForDataEntry(registrationDetails.get(), regServiceVO);
		}
		if (registrationDetails.get().getServiceIds() != null
				&& registrationDetails.get().getServiceIds().contains(ServiceEnum.RCFORFINANCE.getId())) {
			regServiceVO = conditionForFrcQuestions(registrationDetails, selectedRole, regServiceVO);
		}

		if (registrationDetails.get().getServiceIds().contains(ServiceEnum.RCFORFINANCE.getId())
				&& registrationDetails.get().getFlowId() != null
				&& registrationDetails.get().getFlowId().equals(Flow.RCFORFINANCEMVIACTION)) {
			checkValidationForAOAction(registrationDetails.get().getActionDetails(), registrationDetails.get(),
					regServiceVO);
			regServiceVO = conditionForFrcQuestions(registrationDetails, selectedRole, regServiceVO);

		}
		if(registrationDetails.get().getServiceIds().contains(ServiceEnum.RCCANCELLATION.getId())){
			regServiceVO = regServiceMapper.convertEntity(registrationDetails.get());
		}
		if ((registrationDetails.get().getCitizenCOARecommendationLetterStatus() != null
				&& TransferType.permitTranfer.RECOMMENDATIONLETTERCOA
						.equals(registrationDetails.get().getCitizenCOARecommendationLetterStatus()))
				|| (registrationDetails.get().getBuyerDetails() != null
						&& registrationDetails.get().getBuyerDetails().getBuyerRecommedationLetterStatus() != null
						&& TransferType.permitTranfer.RECOMMENDATIONLETTERCOA.equals(
								registrationDetails.get().getBuyerDetails().getBuyerRecommedationLetterStatus()))) {

			List<PermitDetailsDTO> permitDto = permitDetailsDAO.findByPrNoAndPermitStatusAndIsRecommendationLetterTrue(
					regServiceVO.getPrNo(), PermitsEnum.ACTIVE.getDescription());
			if (CollectionUtils.isNotEmpty(permitDto)) {
				regServiceVO.setRecommendationLetterStatesList(
						permitDto.stream().distinct().map(val -> val.getRouteDetails().getState()).collect(Collectors.toList()));
			}

		}
		enc.setRegServiceVO(regServiceVO);
		if (registrationDetails.get().getServiceIds().contains(ServiceEnum.OBJECTION.getId()) || ((registrationDetails
				.get().getServiceIds().contains(ServiceEnum.VEHICLESTOPPAGE.getId())
				|| registrationDetails.get().getServiceIds().contains(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId()))
				&& selectedRole.equalsIgnoreCase(RoleEnum.MVI.getName()))) {
			return enc;
		}

		List<EnclosuresDTO> enclosureDtos = new ArrayList<>();
		if (registrationDetails.get().getServiceIds().contains(ServiceEnum.VEHICLESTOPPAGE.getId())
				|| registrationDetails.get().getServiceIds().contains(ServiceEnum.VEHICLESTOPPAGEREVOKATION.getId())) {
			enclosureDtos = enclosuresDAO.findByServiceIDInAndApplicantType(registrationDetails.get().getServiceIds(),
					ApplicantTypeEnum.MVI);
		} else if (registrationDetails.get().getServiceIds().contains(ServiceEnum.RCFORFINANCE.getId())) {
			enclosureDtos = enclosuresDAO.findByServiceIDInAndApplicantTypeIn(registrationDetails.get().getServiceIds(),
					listOfApplicantTypes());
			if (registrationDetails.get().isMviDone() && registrationDetails.get().getCurrentRoles() != null
					&& registrationDetails.get().getCurrentRoles().stream()
							.anyMatch(role -> role.equalsIgnoreCase(RoleEnum.AO.getName()))) {
				enclosureDtos = enclosuresDAO.findByServiceIDInAndApplicantType(
						registrationDetails.get().getServiceIds(), ApplicantTypeEnum.MVI);
			}
		}
		else if(registrationDetails.get().getServiceIds().contains(ServiceEnum.RCCANCELLATION.getId())) {
			enclosureDtos = enclosuresDAO.findByServiceIDInAndApplicantTypeIn(registrationDetails.get().getServiceIds(),
					listOfApplicantTypesForRc());
		}
		else {
			enclosureDtos = enclosuresDAO.findByServiceIDInAndApplicantType(registrationDetails.get().getServiceIds(),
					ApplicantTypeEnum.CITIZEN);
		}
		Optional<CitizenEnclosuresDTO> citizenEnclosures = citizenEnclosuresDAO.findByApplicationNoAndServiceIdsIn(
				registrationDetails.get().getApplicationNo(), registrationDetails.get().getServiceIds());
		if (!citizenEnclosures.isPresent()) {
			logger.error("Enclosures not found for ApplicationNo [{}] ." + applicationFormNo);
			throw new BadRequestException("Citizen uploaded Enclosures not found.");
		}

		// enclosureDtos.sort((p1, p2) -> p1.getSlNo().compareTo(p2.getSlNo()));
		List<DisplayEnclosures> rejectedEnclosures = new ArrayList<>();

		List<KeyValue<String, List<ImageEnclosureDTO>>> listOfImages = new ArrayList<>();
		// Rejected images in RTO
		// To do need to change the status check
		if (citizenEnclosures.get().getEnclosures().stream().anyMatch(images -> images.getValue().stream().anyMatch(
				status -> status.getImageStaus().equalsIgnoreCase(StatusRegistration.REUPLOAD.getDescription())))) {
			citizenEnclosures.get().getEnclosures().stream()
					.forEach(values -> values.getValue().stream().forEach(value -> {
						if (value.getImageStaus().equalsIgnoreCase(StatusRegistration.REUPLOAD.getDescription())) {
							// rejectedEnclosures.add(new
							// DisplayEnclosures(enclosureImageMapper.convertNewEntity(Arrays.asList(value))));
							listOfImages.add(values);
						}
					}));

		} else if ((regServiceVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.NEWFC.getId()))
				|| regServiceVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RENEWALFC.getId()))
				|| regServiceVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.OTHERSTATIONFC.getId())))
				&& regServiceVO.getServiceIds().size() == 1) {
			citizenEnclosures.get().getEnclosures().stream()
					.forEach(values -> values.getValue().stream().forEach(value -> {
						if (value.getImageType().equalsIgnoreCase(EnclosureType.CONTRACTCOPY.getValue())) {

							listOfImages.add(values);
						}
					}));
		} else if (regServiceVO.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.RCFORFINANCE.getId()))
				&& registrationDetails.get().isMviDone() && registrationDetails.get().getCurrentRoles().stream()
						.anyMatch(role -> role.equalsIgnoreCase(RoleEnum.AO.getName()))) {
			citizenEnclosures.get().getEnclosures().forEach(val -> {
				if (val.getKey().equals(EnclosureType.Vehicle.getValue())) {
					listOfImages.add(val);
				}
			});

		}
		else {
			listOfImages.addAll(citizenEnclosures.get().getEnclosures());
		}

		for (KeyValue<String, List<ImageEnclosureDTO>> enclosureKeyValue : listOfImages) {

			boolean statue = false;
			for (EnclosuresDTO enclosures : enclosureDtos) {
				Optional<ImageEnclosureDTO> value = enclosureKeyValue.getValue().stream()
						.filter(dto -> dto.getImageType().equalsIgnoreCase(enclosures.getProof())).findFirst();
				if (value.isPresent() && enclosures.getBasedOnRole() != null) {
					if (!enclosures.getBasedOnRole().stream().anyMatch(role -> roles.equalsIgnoreCase(role))) {
						statue = true;
					}
				}
			}

			if (statue) {
				continue;
			}
			Optional<EnclosuresDTO> matchedEnclosure = enclosureDtos.stream()
					.filter(dto -> dto.getProof().equals(enclosureKeyValue.getKey())).findFirst();

			if (!matchedEnclosure.isPresent()) {
				continue;
			}

			// EnclosureType enclosureType =
			// EnclosureType.getEnclosureType(matchedEnclosure.get().getProof());

			List<ImageVO> imagesVO = enclosureImageMapper.convertNewEntity(enclosureKeyValue.getValue());

			rejectedEnclosures.add(new DisplayEnclosures(imagesVO));
		}

		enc.setDisplayEnclosures(rejectedEnclosures);

		return enc;
		// return Pair.of(regServiceVO, rejectedEnclosures);
	}

	private RegServiceVO setFrcQuestions(RegServiceVO regServiceVO,
			String role,Optional<RegServiceDTO> registrationDetails ) {
		List<MasterFreshRcMviQuestions> frcQuestions = frcQuestionsDAO.findByRole(role);

		
		if (registrationDetails.get().getCurrentRoles() != null && registrationDetails.get().getCurrentRoles().stream()
				.anyMatch(selectedRole -> role.equalsIgnoreCase(RoleEnum.AO.getName()))) {
			if (registrationDetails.get().getApplicationStatus() != null
					&& (registrationDetails.get().getApplicationStatus().equals(StatusRegistration.PAYMENTDONE)
							|| registrationDetails.get().getApplicationStatus().equals(StatusRegistration.REUPLOAD))) {
				frcQuestions.forEach(question -> {
					if (question.getQuestion().equalsIgnoreCase(MessageKeys.FRESHRC_AO_QUESTION_FOR_FINACIER_MATCHED)) {
						frcMapper.convertFrcQuestionDtoToVoForAO(regServiceVO, question);

					}
				});
			}
			if (registrationDetails.get().getApplicationStatus() != null && (registrationDetails.get()
					.getApplicationStatus().equals(StatusRegistration.MVIAPPROVED)
					|| registrationDetails.get().getApplicationStatus().equals(StatusRegistration.MVIREJECTED))) {
				frcQuestions.forEach(question -> {
					if (question.getQuestion().equalsIgnoreCase(MessageKeys.FRESHRC_MVI_QUESTION_FOR_FORM37)) {
						frcMapper.convertFrcQuestionDtoToVoForAO(regServiceVO, question);

					}
				});
			}
		}
		if (registrationDetails.get().getCurrentRoles() != null && registrationDetails.get().getCurrentRoles().stream()
				.anyMatch(selectedRole -> role.equalsIgnoreCase(RoleEnum.MVI.getName()))) {
			frcMapper.convertFrcQuestionsDtoToVo(regServiceVO, frcQuestions);
		}

		return regServiceVO;
	}
	private List<ApplicantTypeEnum> listOfApplicantTypes() {
		List<ApplicantTypeEnum> applicantList = new ArrayList<>();
		applicantList.add(ApplicantTypeEnum.FINANCIER);
		applicantList.add(ApplicantTypeEnum.MVI);
		applicantList.add(ApplicantTypeEnum.AO);
		return applicantList;
	}

	private Boolean toEnclousers(EnclosuresDTO dto, CitizenImagesInput input, RegistrationDetailsDTO regdetails) {
		if(input.getServiceIds().stream().anyMatch(id -> id.equals(ServiceEnum.TRANSFEROFOWNERSHIP.getId())) && input.getOwnerType() != null&&
				input.getOwnerType().equals(OwnerType.BUYER)&&input.getTransferType()!=null &&input.getTransferType().equals(TransferType.AUCTION)) {
			return Boolean.TRUE;
		}
		if (dto.getProof().equalsIgnoreCase(EnclosureType.COMPANYNOC.getValue())) {
			if (input.getOwnerType() != null && !input.getOwnerType().equals(OwnerType.BUYER)
					&& regdetails.getOwnerType() != null
					&& !regdetails.getOwnerType().equals(OwnerTypeEnum.Individual)) {
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		}

		if (input.getOwnerType() != null && input.getOwnerType().equals(OwnerType.BUYER)) {
			if (dto.getProof().equalsIgnoreCase(EnclosureType.RC1.getValue())) {
				return Boolean.FALSE;
			}
		}
		if (input.getOwnerType() != null && input.getOwnerType().equals(OwnerType.BUYER)) {
			if (dto.getProof().equalsIgnoreCase(EnclosureType.RC2.getValue())) {
				return Boolean.FALSE;
			}
		}
		if (input.getOwnerType() != null
				&& (input.getOwnerType().equals(OwnerType.BUYER) || input.getOwnerType().equals(OwnerType.SELLER))) {
			if (dto.getProof().equalsIgnoreCase(EnclosureType.DEATHCERTIFICATE.getValue())) {
				return Boolean.FALSE;
			}
		}
		if (input.getOwnerType() != null && input.getOwnerType().equals(OwnerType.BUYER)) {
			if (dto.getProof().equalsIgnoreCase(EnclosureType.Insurance.getValue())) {
				if (registrationService.getInsuranceValidity(regdetails.getPrNo())) {
					return Boolean.FALSE;
				}
				return Boolean.TRUE;
			}
			if (dto.getProof().equalsIgnoreCase(EnclosureType.PUC.getValue())) {
				if (registrationService.getPUCValidity(regdetails.getPrNo())) {
					return Boolean.FALSE;
				}
				return Boolean.TRUE;
			}

		}

		if (input.getOwnerType() != null && input.getOwnerType().equals(OwnerType.BUYER)) {
			if ((dto.getProof().equalsIgnoreCase(EnclosureType.AddrBackView.getValue())
					|| dto.getProof().equalsIgnoreCase(EnclosureType.AddrFrontview.getValue()))
					&& input.getIsSameAsAadhar()) {
				return Boolean.FALSE;
			}
		}

		if (input.getOwnerType() != null && input.getOwnerType().equals(OwnerType.SELLER)) {
			if (dto.getProof().equalsIgnoreCase(EnclosureType.AddrBackView.getValue())
					|| dto.getProof().equalsIgnoreCase(EnclosureType.AddrFrontview.getValue())) {
				return Boolean.FALSE;
			}

		}
		if (dto.getProof().equalsIgnoreCase(EnclosureType.DEATHCERTIFICATE.getValue())) {
			return Boolean.FALSE;
		}
		if (dto.getProof().equalsIgnoreCase(EnclosureType.AFFIDAVIT.getValue())) {
			return Boolean.FALSE;
		}
		if (dto.getProof().equalsIgnoreCase(EnclosureType.LEGALHEIRCERTIFICATE.getValue())) {
			return Boolean.FALSE;
		}
		if (dto.getProof().equalsIgnoreCase(EnclosureType.CONSENTLETTERFROMFAMILYMEMBERS.getValue())) {
			return Boolean.FALSE;
		}
		if (dto.getProof().equalsIgnoreCase(EnclosureType.APROFILE.getValue())) {
			return Boolean.FALSE;
		}
		if (dto.getProof().equalsIgnoreCase(EnclosureType.FINANCECOPY.getValue())) {
			if (regdetails.getFinanceDetails() != null) {
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		}
		if (dto.getProof().equalsIgnoreCase(EnclosureType.PUC.getValue())) {
			if (registrationService.regPUCDetailsValidity(regdetails)) {
				return Boolean.TRUE;
			}
			return Boolean.FALSE;
		}
		return Boolean.TRUE;

	}

	private List<EnclosureRejectedVO> getQrCode(CitizenImagesInput input, List<EnclosureRejectedVO> enclosures) {
		if (!input.getIsMobileRequest()) {
			if (StringUtils.isEmpty(input.getApplicationNo())) {
				throw new BadRequestException("application number not available");
			}

			String qrData = "{" + "\"applicationNo\"" + ":\"" + input.getApplicationNo() + "\"," + "\""
					+ "serviceIds\":" + input.getServiceIds() + "," + "\"companyVehicle\":" + input.isCompanyVehicle()
					+ ",\"isMobileRequest\":" + Boolean.TRUE + "}";

			try {

				if (CollectionUtils.isNotEmpty(enclosures)) {

					enclosures.get(enclosures.size() - 1).setQrCode(reportService.sendPDF(qrData));

				} else if (input.getIsMobileRequest()) {
					EnclosureRejectedVO qrCodeImage = new EnclosureRejectedVO();
					qrCodeImage.setQrCode(reportService.sendPDF(qrData));
					enclosures.add(qrCodeImage);
				}
			} catch (FileNotFoundException e) {
				logger.info(e.getMessage());
			} catch (WriterException e) {
				logger.info(e.getMessage());
			} catch (IOException e) {
				logger.info(e.getMessage());
			}
		}
		return enclosures;
	}
	private List<ApplicantTypeEnum> listOfApplicantTypesForRc(){
		List<ApplicantTypeEnum> applicantList=new ArrayList<>();
		applicantList.add(ApplicantTypeEnum.CCO);
		applicantList.add(ApplicantTypeEnum.CITIZEN);
		applicantList.add(ApplicantTypeEnum.MVI);
		return applicantList;
		}

	private void checkValidationForAOAction(List<ActionDetails> actionDetailsList, RegServiceDTO regServiceDTO,
			RegServiceVO regServiceVO) {
		List<ActionDetails> filterlist = actionDetailsList.stream()
				.filter(val -> (val.getRole().equals(RoleEnum.AO.getName()) && val.getStatus() != null
						&& val.getStatus().equals(StatusRegistration.REJECTED.getDescription())))
				.collect(Collectors.toList());
		if (CollectionUtils.isNotEmpty(filterlist)) {
			filterlist.sort((p1, p2) -> p1.getlUpdate().compareTo(p2.getlUpdate()));
			ActionDetails actionDetails = filterlist.stream().findFirst().get();
			if (actionDetails.getIsDoneProcess() && actionDetails.getRole().equals(RoleEnum.AO.getName())
					&& actionDetails.getStatus().equals(StatusRegistration.REJECTED.getDescription())) {
				regServiceVO.getFreshRc().setAOReject(true);
			}
			if (actionDetails.getIsDoneProcess() && actionDetails.getRole().equals(RoleEnum.AO.getName())
					&& actionDetails.getStatus().equals(StatusRegistration.AOAPPROVED.getDescription())
					&& regServiceDTO.getFreshRcdetails() != null && regServiceDTO.getFreshRcdetails().isAoApproved()) {
				regServiceVO.getFreshRc().setAoApproved(true);
			}
		}
	}
	
	@Autowired
	private DealerRegDAO dealerRegDAO;
	
	@Autowired
	private DealerRegMapper dealerRegMapper;

	@Override
	public Enclosures getEnclosureDetailsForRtaSideForDealerModule(String applicationFormNo, String userId,
			String selectedRole) {

		Enclosures enc = new Enclosures();
		Set<Integer> serviceIds = new HashSet<>();
		String roles = dTOUtilService.getRole(userId, selectedRole);
		Optional<DealerRegDTO> dealerRegistrationDetails = dealerRegDAO.findByApplicationNo(applicationFormNo);
		if (!dealerRegistrationDetails.isPresent()) {
			logger.info("Application  is not found [{}] ." + applicationFormNo);
			throw new BadRequestException("Application  is not found. Application no :" + applicationFormNo);
		}
		serviceIds.addAll(dealerRegistrationDetails.get().getServiceIds());
		DealerRegVO dealerRegVO = null;
		dealerRegVO = dealerRegMapper.convertEntity(dealerRegistrationDetails.get());
		List<EnclosuresDTO> enclosureDtos = new ArrayList<>();
		enclosureDtos = enclosuresDAO.findByServiceIDInAndApplicantType(serviceIds, ApplicantTypeEnum.MVI);

		Optional<CitizenEnclosuresDTO> citizenEnclosures = citizenEnclosuresDAO
				.findByApplicationNoAndServiceIdsInOrderByLUpdateDesc(dealerRegistrationDetails.get().getApplicationNo(), serviceIds);
		if (!citizenEnclosures.isPresent()) {
			logger.error("Enclosures not found for ApplicationNo [{}] ." + applicationFormNo);
			throw new BadRequestException("Citizen uploaded Enclosures not found.");
		}
		enc.setDealerRegVO(dealerRegVO);
		List<DisplayEnclosures> rejectedEnclosures = new ArrayList<>();

		List<KeyValue<String, List<ImageEnclosureDTO>>> listOfImages = new ArrayList<>();
		// Rejected images in RTO
		// To do need to change the status check
		if (citizenEnclosures.get().getEnclosures().stream().anyMatch(images -> images.getValue().stream().anyMatch(
				status -> status.getImageStaus().equalsIgnoreCase(StatusRegistration.REUPLOAD.getDescription())))) {
			citizenEnclosures.get().getEnclosures().stream()
					.forEach(values -> values.getValue().stream().forEach(value -> {
						if (value.getImageStaus().equalsIgnoreCase(StatusRegistration.REUPLOAD.getDescription())) {
							// rejectedEnclosures.add(new
							// DisplayEnclosures(enclosureImageMapper.convertNewEntity(Arrays.asList(value))));
							listOfImages.add(values);
						}
					}));

		} else {
			listOfImages.addAll(citizenEnclosures.get().getEnclosures());
		}

		for (KeyValue<String, List<ImageEnclosureDTO>> enclosureKeyValue : listOfImages) {

			boolean statue = false;
			for (EnclosuresDTO enclosures : enclosureDtos) {
				Optional<ImageEnclosureDTO> value = enclosureKeyValue.getValue().stream()
						.filter(dto -> dto.getImageType().equalsIgnoreCase(enclosures.getProof())).findFirst();
				if (value.isPresent() && enclosures.getBasedOnRole() != null) {
					if (!enclosures.getBasedOnRole().stream().anyMatch(role -> roles.equalsIgnoreCase(role))) {
						statue = true;
					}
				}
			}

			if (statue) {
				continue;
			}
			Optional<EnclosuresDTO> matchedEnclosure = enclosureDtos.stream()
					.filter(dto -> dto.getProof().equals(enclosureKeyValue.getKey())).findFirst();

			if (!matchedEnclosure.isPresent()) {
				continue;
			}
			List<ImageVO> imagesVO = enclosureImageMapper.convertNewEntity(enclosureKeyValue.getValue());

			rejectedEnclosures.add(new DisplayEnclosures(imagesVO));
		}
		enc.setDisplayEnclosures(rejectedEnclosures);
		return enc;

	}
	@Override
	public List<EnclosureSupportedVO> getListOfSupportEnclosuresForFA(Set<Integer> id, String caste) {
		List<EnclosuresDTO> enclosures;
		if(!(id.isEmpty()||caste!=null)) {
			throw new BadRequestException("No Input Found");
		}
		 enclosures = enclosuresDAO.findByServiceIDInAndIsRequiredTrue(id);
		if(caste.equals("OTHERS")) {
			List<EnclosuresDTO> collect = enclosures.stream().filter(predicate->predicate.getProof().equals("CASTE")).collect(Collectors.toList());
			collect.forEach(s->{
			enclosures.remove(s);
				
			});
}
		if(!enclosures.isEmpty()) {
			enclosures.stream().forEach(x->{
			x.setCreatedBy(null);
				x.setModifiedBy(null);
				x.setModifiedDate(null);
			});
		}
	return enclosures.stream().map(m->new EnclosureSupportedVO(m.getProof(),enclosuresMapper.convertEntity(m))).collect(Collectors.toList());
		
		
	}
	
	private RegServiceVO conditionForFrcQuestions(Optional<RegServiceDTO> registrationDetails, String selectedRole,
			RegServiceVO regServiceVO) {

		Optional<UserDTO> users = null;
		if (registrationDetails.get().getRegistrationDetails().getFinanceDetails().getUserId() != null) {
			users = userdao.findByUserId(registrationDetails.get().getRegistrationDetails().getFinanceDetails().getUserId());
		}
		if (selectedRole.equals(RoleEnum.AO.getName())) {
			if (registrationDetails.get().getServiceIds() != null
					&& registrationDetails.get().getServiceIds().contains(ServiceEnum.RCFORFINANCE.getId())
					&& registrationDetails.get().getApplicationStatus() != null
					&& Arrays
							.asList(StatusRegistration.MVIAPPROVED, StatusRegistration.MVIREJECTED,
									StatusRegistration.REUPLOAD, StatusRegistration.PAYMENTDONE)
							.contains(registrationDetails.get().getApplicationStatus()) 
					&& CollectionUtils.isNotEmpty(registrationDetails.get().getCurrentRoles())
					&& registrationDetails.get().getCurrentRoles().stream()
							.anyMatch(role -> role.equalsIgnoreCase(RoleEnum.AO.getName()))) {
				regServiceVO = setFrcQuestions(regServiceVO, selectedRole,registrationDetails);
				if (registrationDetails.get().getRegistrationDetails() != null
						&& registrationDetails.get().getRegistrationDetails().getFinanceDetails() != null
						&& (registrationDetails.get().getRegistrationDetails().getFinanceDetails().getUserId() == null
								|| !users.isPresent())) {
					regServiceVO.setOffLineFinancier(Boolean.TRUE);
				}

			}
		}
		if (selectedRole.equals(RoleEnum.MVI.getName())) {
			if (registrationDetails.get().getCurrentRoles() != null
					&& registrationDetails.get().getCurrentRoles().stream()
							.anyMatch(role -> role.equalsIgnoreCase(RoleEnum.MVI.getName()))
					&& registrationDetails.get().getApplicationStatus() != null
					&& (registrationDetails.get().getApplicationStatus().equals(StatusRegistration.AOAPPROVED)
							|| registrationDetails.get().getApplicationStatus()
									.equals(StatusRegistration.RTOAPPROVED))) {
				regServiceVO = setFrcQuestions(regServiceVO, selectedRole,registrationDetails);
			}
			if (registrationDetails.get().getRegistrationDetails() != null
					&& registrationDetails.get().getRegistrationDetails().getFinanceDetails() != null
					&& (registrationDetails.get().getRegistrationDetails().getFinanceDetails().getUserId() == null
							|| !users.isPresent())) {
				regServiceVO.setOffLineFinancier(Boolean.TRUE);
			}
		}
		
		if (selectedRole.equals(RoleEnum.RTO.getName())) {
			if (registrationDetails.get().getRegistrationDetails() != null
					&& registrationDetails.get().getRegistrationDetails().getFinanceDetails() != null
					&& (registrationDetails.get().getRegistrationDetails().getFinanceDetails().getUserId() == null
							|| !users.isPresent())) {
				regServiceVO.setOffLineFinancier(Boolean.TRUE);
			}
		}
		return regServiceVO;
	}

	
	@Override 
	public List<ImageVO> getImages(String applicationNo) {
		
		List<ImageVO> imagesVO = new ArrayList<>();
		Optional<StagingRegistrationDetailsDTO> stagingDetailsDto  = FindbBasedOnApplicationNo(applicationNo);
		if(stagingDetailsDto.isPresent()) {
			if(stagingDetailsDto.get().getEnclosures()==null) {
				throw new BadRequestException("Enclousers not avaialable");
			}
			for (KeyValue<String, List<ImageEnclosureDTO>> dto : stagingDetailsDto.get().getEnclosures()) {
				List<ImageVO> images = enclosureImageMapper.convertNewEntity(dto.getValue());
				imagesVO.addAll(images);
			}
			return imagesVO;
		}
		Optional<CitizenEnclosuresDTO> citizenEnclosuresOptional = citizenEnclosuresDAO
				.findByApplicationNo(applicationNo);
		if(!citizenEnclosuresOptional.isPresent()) {
			throw new BadRequestException("Enclousers not avaialable");
		}
		for (KeyValue<String, List<ImageEnclosureDTO>> dto : citizenEnclosuresOptional.get().getEnclosures()) {
			List<ImageVO> images = enclosureImageMapper.convertNewEntity(dto.getValue());
			imagesVO.addAll(images);
		}
		return imagesVO;
	}
	
	public Optional<StagingRegistrationDetailsDTO> FindbBasedOnApplicationNo(String applicationNo) {
		Optional<StagingRegistrationDetailsDTO> stagingRegistrationOptional;
		stagingRegistrationOptional = stagingRegistrationDetailsDAO.findByApplicationNo(applicationNo);
		if (stagingRegistrationOptional.isPresent()) {
			logger.debug("registration details found for applicationNo: [{}]", applicationNo);
			return stagingRegistrationOptional;
		}
		StagingRegistrationDetailsDTO stagingRegistrationDetails = new StagingRegistrationDetailsDTO();
		Optional<RegistrationDetailsDTO> regDetailsDTO =registrationDetailDAO.findByApplicationNo(applicationNo);
		if(regDetailsDTO.isPresent()){
			BeanUtils.copyProperties(regDetailsDTO.get(), stagingRegistrationDetails);
			return Optional.of(stagingRegistrationDetails);
		}
		Optional<RegServiceDTO> osSecondVechile = regServiceDAO.findByApplicationNo(applicationNo);
		if (osSecondVechile.isPresent() && osSecondVechile.get().getServiceIds() != null
				&& osSecondVechile.get().getServiceIds().stream()
						.anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))
				&& !osSecondVechile.get().getRegistrationDetails().isRegVehicleWithPR()) {
			BeanUtils.copyProperties(osSecondVechile.get().getRegistrationDetails(), stagingRegistrationDetails);
			stagingRegistrationDetails.setIteration(1);
			return Optional.of(stagingRegistrationDetails);
		}
		return Optional.empty();
	}
    @Override
	public CitizenEnclosersVO getListOfApprovedEnclosures(String applicationNo) {
		try {
		CitizenEnclosersVO citizenEnclosersVO = new CitizenEnclosersVO();
		Pair<Boolean, List<EnclosureRejectedVO>> enclosure = rejectedEnclosures(applicationNo, true);
		List<EnclosureRejectedVO> listOfEnclosures = enclosure.getSecond();
		citizenEnclosersVO.setApprovedEclosures(listOfEnclosures);
		return citizenEnclosersVO;
		}catch(Exception e) {
			return null;
		}

	}
}
