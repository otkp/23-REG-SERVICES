package org.epragati.images.serviceImp;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.epragati.constants.CovCategory;
import org.epragati.constants.EnclosureType;
import org.epragati.constants.OwnerTypeEnum;
import org.epragati.dto.enclosure.ImageEnclosureDTO;
import org.epragati.exception.BadRequestException;
import org.epragati.images.service.ImagesService;
import org.epragati.images.vo.ImageInput;
import org.epragati.master.dao.EnclosuresDAO;
import org.epragati.master.dao.StagingRegistrationDetailsDAO;
import org.epragati.master.dto.EnclosuresDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.mappers.EnclosuresMapper;
import org.epragati.master.service.LogMovingService;
import org.epragati.master.service.StagingRegistrationDetailsSerivce;
import org.epragati.regservice.RegistrationService;
import org.epragati.rta.service.impl.DTOUtilService;
import org.epragati.service.enclosure.mapper.EnclosureImageMapper;
import org.epragati.service.enclosure.vo.DealerEnclosureVO;
import org.epragati.service.enclosure.vo.DisplayEnclosures;
import org.epragati.service.enclosure.vo.EnclosureRejectedVO;
import org.epragati.service.enclosure.vo.ImageVO;
import org.epragati.service.files.GridFsClient;
import org.epragati.util.StatusRegistration;
import org.epragati.util.document.KeyValue;
import org.epragati.util.payment.ClassOfVehicleEnum;
import org.epragati.util.payment.ServiceEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImagesServiceImpl implements ImagesService {

	private static final Logger logger = LoggerFactory.getLogger(ImagesServiceImpl.class);

	@Autowired
	private GridFsClient gridFsClient;

	@Autowired
	private EnclosuresMapper enclosuresMapper;

	@Autowired
	private EnclosuresDAO enclosuresDAO;

	@Autowired
	private EnclosureImageMapper enclosureImageMapper;

	@Autowired
	private StagingRegistrationDetailsSerivce stagingRegistrationDetailsSerivce;

	@Autowired
	private StagingRegistrationDetailsDAO stagingRegistrationDetails;

	@Autowired
	private DTOUtilService dTOUtilService;

	@Autowired
	private RegistrationService reService;

	@Autowired
	private LogMovingService logMovingService;

	@Override
	public void saveImages(String applicationNo, List<ImageInput> images, MultipartFile[] uploadfiles)
			throws IOException {
		Optional<StagingRegistrationDetailsDTO> registrationDetails = stagingRegistrationDetailsSerivce
				.FindbBasedOnApplicationNo(applicationNo);
		if (!registrationDetails.isPresent()) {
			logger.error("Enclosures  is not found [{}] ", applicationNo);
			throw new BadRequestException("Application  is not found." + applicationNo);
		}
		StagingRegistrationDetailsDTO StagingRegistrationDetails = registrationDetails.get();
		saveEnclosures(StagingRegistrationDetails, images, uploadfiles);
	}

	@Override
	public DealerEnclosureVO getListOfSupportedEnclosuresForDealer(String applicationNo) {
		DealerEnclosureVO dealerEnclosure = new DealerEnclosureVO();
		Pair<Boolean, List<EnclosureRejectedVO>> enclosure = getListOfSupportedEnclosureDetails(applicationNo);
		dealerEnclosure.setEnclosureRejected(enclosure.getSecond());
		dealerEnclosure.setUploadRequired(enclosure.getFirst());
		return dealerEnclosure;
	}

	@Override
	public List<EnclosureRejectedVO> getListOfSupportedEnclosuresForMobile(String applicationNo) {

		Pair<Boolean, List<EnclosureRejectedVO>> enclosure = getListOfSupportedEnclosureDetails(applicationNo);

		List<EnclosureRejectedVO> listOfEnclosures = enclosure.getSecond();

		return listOfEnclosures;
	}

	private Pair<Boolean, List<EnclosureRejectedVO>> getListOfSupportedEnclosureDetails(String applicationNo) {

		List<EnclosuresDTO> dtos = enclosuresDAO.findByServiceID(ServiceEnum.TEMPORARYREGISTRATION.getId());
		List<EnclosureType> enclosuresTypes = null;
		boolean uploadStatus = false;
		List<EnclosureRejectedVO> rejectedEnclosures = new ArrayList<>();
		if (dtos.isEmpty()) {
			logger.error("Enclosures  is not found [{}]",applicationNo);
			throw new BadRequestException("Enclosures  is not found.");
		}
		dtos.sort((p1, p2) -> p1.getSlNo().compareTo(p2.getSlNo()));

		Optional<StagingRegistrationDetailsDTO> registrationDetails = stagingRegistrationDetailsSerivce
				.FindbBasedOnApplicationNo(applicationNo);

		if (!registrationDetails.isPresent()) {
			logger.error("Enclosures  is not found [{}] ." + applicationNo);
			throw new BadRequestException("Application  is not found.");
		}
		if (registrationDetails.get().getEnclosures() == null) {
			Map<String, List<ImageVO>> typeToVOs = new HashMap<>();

			for (EnclosuresDTO dto : dtos) {

				if (!checkImagesApplicableOrNot(dto, registrationDetails)) {
					continue;
				}
				if (dto.isRequired()) {
					uploadStatus = true;
				}
				typeToVOs.put(dto.getProof(), enclosureImageMapper.convertdtoToVo(dto));
			}
			List<EnclosureRejectedVO> enclosures = typeToVOs.keySet().stream()
					.map(k -> new EnclosureRejectedVO(k, typeToVOs.get(k))).collect(Collectors.toList());
			enclosures.sort((p1, p2) -> p1.getSlNo().compareTo(p2.getSlNo()));
			return Pair.of(uploadStatus, enclosures);

		} else {

			// DIsplay Rejected Image
			if (checkApplicationStatus(registrationDetails.get())) {
				registrationDetails.get().getEnclosures().stream()
						.forEach(values -> values.getValue().stream().forEach(value -> {
							if (value.getImageStaus().equalsIgnoreCase(StatusRegistration.REUPLOAD.getDescription())
									|| value.getImageStaus()
											.equalsIgnoreCase(StatusRegistration.REJECTED.getDescription())) {
								rejectedEnclosures.add(new EnclosureRejectedVO(
										values.getValue().stream().findFirst().get().getImageType(),
										enclosureImageMapper.convertNewEntity(Arrays.asList(value))));
							}
						}));

				if (registrationDetails.get().getEnclosures().stream()
						.anyMatch(images -> images.getValue().stream().anyMatch(status -> status.getImageStaus()
								.equalsIgnoreCase(StatusRegistration.REJECTED.getDescription())))) {
					uploadStatus = Boolean.TRUE;
				}
				// return rejectedEnclosures;
				return Pair.of(uploadStatus, rejectedEnclosures);
			}

			List<ImageVO> imagesVO = null;

			for (EnclosuresDTO dto : dtos) {
				List<ImageVO> matchedEnclosures = findAndGetUploadedImage(dto.getProof(),
						registrationDetails.get().getEnclosures());
				if (!matchedEnclosures.isEmpty()) {
					imagesVO = matchedEnclosures;
				} else {

					if (!checkImagesApplicableOrNot(dto, registrationDetails)) {
						continue;
					}
					if (dto.isRequired()) {
						uploadStatus = true;
					}
					imagesVO = enclosureImageMapper.convertNewEntity(enclosureImageMapper.convertNewEntity(dto));

				}
				if (!rejectedEnclosures.isEmpty()
						&& rejectedEnclosures.stream().anyMatch(en -> en.getType().equals(dto.getProof()))) {
					continue;
				}
				rejectedEnclosures
						.add(new EnclosureRejectedVO(imagesVO.stream().findFirst().get().getImageType(), imagesVO));
			}

			// return rejectedEnclosures;
			return Pair.of(uploadStatus, rejectedEnclosures);
		}
	}

	private boolean checkImagesApplicableOrNot(EnclosuresDTO dto,
			Optional<StagingRegistrationDetailsDTO> registrationDetails) {

		if (null != registrationDetails.get().getApplicantDetails().getIsDifferentlyAbled()
				&& !registrationDetails.get().getApplicantDetails().getIsDifferentlyAbled()) {
			if (dto.getIsDifferentlyAbled()) {
				return Boolean.FALSE;
			}
		}
		if (null != registrationDetails.get().getApplicantDetails().getSameAsAadhar()
				&& registrationDetails.get().getApplicantDetails().getSameAsAadhar() || null!= registrationDetails.get().getApplicantDetails().getPresentAddressFrom() && registrationDetails.get().getApplicantDetails().getPresentAddressFrom().equals("RATION CARD")) {
			if (dto.getProof().equalsIgnoreCase(EnclosureType.AddrBackView.getValue())
					|| dto.getProof().equalsIgnoreCase(EnclosureType.AddrFrontview.getValue())) {
				return Boolean.FALSE;
			}
		}
		if (!(registrationDetails.get().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.MCYN.getCovCode())
				|| registrationDetails.get().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.MMCN.getCovCode())
				|| registrationDetails.get().getClassOfVehicle()
						.equalsIgnoreCase(ClassOfVehicleEnum.IVCN.getCovCode()))) {
			if (dto.getProof().equalsIgnoreCase(EnclosureType.HelmetInvoice.getValue())) {
				return Boolean.FALSE;
			}
		}
		if (registrationDetails.get().getVehicleType().equalsIgnoreCase(CovCategory.N.getCode())) {
			if (dto.getProof().equalsIgnoreCase(EnclosureType.VehicleLeft.getValue())
					|| dto.getProof().equalsIgnoreCase(EnclosureType.VehicleRear.getValue())) {
				return Boolean.FALSE;
			}
		}
		if (registrationDetails.get().getOwnerType().equals(OwnerTypeEnum.Individual)) {
			if (dto.getProof().equalsIgnoreCase(EnclosureType.Certificate.getValue())
					|| dto.getProof().equalsIgnoreCase(EnclosureType.Representative.getValue())
					|| dto.getProof().equalsIgnoreCase(EnclosureType.Release.getValue())) {
				return Boolean.FALSE;
			}

		}
		if (registrationDetails.get().getOwnerType().equals(OwnerTypeEnum.Organization)
				|| registrationDetails.get().getOwnerType().equals(OwnerTypeEnum.Company)) {
			if (dto.getProof().equalsIgnoreCase(EnclosureType.Release.getValue())) {
				return Boolean.FALSE;
			}

		}

		if (registrationDetails.get().getOwnerType().equals(OwnerTypeEnum.Government)
				|| registrationDetails.get().getOwnerType().equals(OwnerTypeEnum.POLICE)
				|| registrationDetails.get().getOwnerType().equals(OwnerTypeEnum.Stu)) {
			if (dto.getProof().equalsIgnoreCase(EnclosureType.Insurance.getValue())) {
				return Boolean.FALSE;
			}

		}

		if (!(registrationDetails.get().getOwnerType().equals(OwnerTypeEnum.Organization)
				|| registrationDetails.get().getOwnerType().equals(OwnerTypeEnum.Company)
				|| registrationDetails.get().getOwnerType().equals(OwnerTypeEnum.Individual))) {
			if (dto.getProof().equalsIgnoreCase(EnclosureType.Certificate.getValue())
					|| dto.getProof().equalsIgnoreCase(EnclosureType.Representative.getValue())) {
				return Boolean.FALSE;
			}

		}
		if (!(registrationDetails.get().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())
				|| registrationDetails.get().getClassOfVehicle()
						.equalsIgnoreCase(ClassOfVehicleEnum.TTTT.getCovCode()))) {
			if (dto.getProof().equalsIgnoreCase(EnclosureType.vehicleFrontDiagonalView.getValue())
					|| dto.getProof().equalsIgnoreCase(EnclosureType.vehicleRearDiagonalView.getValue())
					|| dto.getProof().equalsIgnoreCase(EnclosureType.Form22A.getValue())
					|| dto.getProof().equalsIgnoreCase(EnclosureType.AgricultureCertificate.getValue())) {
				return Boolean.FALSE;
			}
		}
		if ((registrationDetails.get().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TTTT.getCovCode()))) {
			if (dto.getProof().equalsIgnoreCase(EnclosureType.AgricultureCertificate.getValue())) {
				return Boolean.FALSE;
			}
		}
		if (registrationDetails.get().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.ARVT.getCovCode())
				|| registrationDetails.get().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.TTRN.getCovCode())
				|| registrationDetails.get().getClassOfVehicle()
						.equalsIgnoreCase(ClassOfVehicleEnum.TTTT.getCovCode())) {
			if (dto.getProof().equalsIgnoreCase(EnclosureType.VehicleLeft.getValue())
					|| dto.getProof().equalsIgnoreCase(EnclosureType.VehicleRear.getValue())) {
				return Boolean.FALSE;
			}
		}
		
		if ( registrationDetails.get().getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.IVCN.getCovCode())) {
			if (dto.getProof().equalsIgnoreCase(EnclosureType.HelmetInvoice.getValue())) {
				if(registrationDetails.get().getVahanDetails()!= null && 
						registrationDetails.get().getVahanDetails().getVehicleClass() != null &&
						registrationDetails.get().getVahanDetails().getVehicleClass().equalsIgnoreCase("M1")) {
				return Boolean.FALSE;
				}
			}
		}
		/*
		 * if (registrationDetails.get().getIsFinancier() != null &&
		 * !registrationDetails.get().getIsFinancier()) { if
		 * (dto.getProof().equalsIgnoreCase(EnclosureType.Form20.getValue())) { status=
		 * Boolean.TRUE; } }
		 */
		return Boolean.TRUE;
	}

	private boolean checkApplicationStatus(StagingRegistrationDetailsDTO registrationDetails) {
		/**
		 * Need to remove after engine number rejected images completed
		 */
		removeMutipleEngineNumberImages(registrationDetails);
		if (registrationDetails.getApplicationStatus().equals(StatusRegistration.REJECTED.getDescription())
				|| registrationDetails.getApplicationStatus().equals(StatusRegistration.REUPLOAD.getDescription())
				|| registrationDetails.getEnclosures().stream()
						.anyMatch(images -> images.getValue().stream()
								.anyMatch(status -> status.getImageStaus()
										.equalsIgnoreCase(StatusRegistration.REJECTED.getDescription())))
				|| registrationDetails.getEnclosures().stream()
						.anyMatch(images -> images.getValue().stream().anyMatch(status -> status.getImageStaus()
								.equalsIgnoreCase(StatusRegistration.REUPLOAD.getDescription())))) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}

	@Override
	public boolean statusForMobileupload(String applicationNo) {

		Optional<StagingRegistrationDetailsDTO> registrationDetails = stagingRegistrationDetailsSerivce
				.FindbBasedOnApplicationNo(applicationNo);
		if (!registrationDetails.isPresent()) {
			logger.error("Application  is not found [{}] ." + applicationNo);
			throw new BadRequestException("Application  is not found.");
		}
		if (registrationDetails.get().getEnclosures() == null) {
			return Boolean.TRUE;
		}
		if (!StringUtils.isEmpty(registrationDetails.get().getTrNo())) {
			if (registrationDetails.get().getEnclosures().stream()
					.anyMatch(images -> images.getValue().stream().anyMatch(status -> status.getImageStaus()
							.equalsIgnoreCase(StatusRegistration.REJECTED.getDescription())))) {
				return Boolean.TRUE;
			} else {
				return Boolean.FALSE;
			}
		} else {
			return Boolean.TRUE;
		}
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
	public List<DisplayEnclosures> getListOfEnclosureDetailsForRta(String applicationFormNo, String userId,
			String inputRole) {

		String roles = dTOUtilService.getRole(userId, inputRole);

		Optional<StagingRegistrationDetailsDTO> registrationDetails = stagingRegistrationDetailsSerivce
				.FindbBasedOnApplicationNo(applicationFormNo);
		if (!registrationDetails.isPresent()) {
			logger.info("Application  is not found [{}] ." + applicationFormNo);
			throw new BadRequestException("Application  is not found. Application no :" + applicationFormNo);
		}
		List<EnclosuresDTO> enclosureDtos = enclosuresDAO.findByServiceID(ServiceEnum.TEMPORARYREGISTRATION.getId());
		enclosureDtos.sort((p1, p2) -> p1.getSlNo().compareTo(p2.getSlNo()));
		List<DisplayEnclosures> rejectedEnclosures = new ArrayList<>();

		if (registrationDetails.get().getEnclosures() == null) {
			logger.error("Enclosures  is not found [{}] ." + registrationDetails.get().getApplicationNo());
			throw new BadRequestException("Enclosures  is not found.");
		}
		List<KeyValue<String, List<ImageEnclosureDTO>>> listOfImages = new ArrayList<>();
		// Rejected images in RTO
		// To do need to change the status check
		if (registrationDetails.get().getEnclosures().stream().anyMatch(images -> images.getValue().stream().anyMatch(
				status -> status.getImageStaus().equalsIgnoreCase(StatusRegistration.REUPLOAD.getDescription())))) {
			registrationDetails.get().getEnclosures().stream()
					.forEach(values -> values.getValue().stream().forEach(value -> {
						if (value.getImageStaus().equalsIgnoreCase(StatusRegistration.REUPLOAD.getDescription())) {
							// rejectedEnclosures.add(new
							// DisplayEnclosures(enclosureImageMapper.convertNewEntity(Arrays.asList(value))));
							listOfImages.add(values);
						}
					}));

		} else {
			listOfImages.addAll(registrationDetails.get().getEnclosures());
		}
		/** Needs to remove after engine image issue resolved **/
		 LocalDateTime custDate1 = LocalDateTime.of(2019, 07, 20, 10, 30);
			if(registrationDetails.get().getTrGeneratedDate()!=null && registrationDetails.get().getTrGeneratedDate().isBefore(custDate1)
					&&!listOfImages.isEmpty()) {
				listOfImages.stream().forEach(action->{
					if(action.getKey().equals("EngineNumber")) {
						enclosureDtos.add(enclosuresMapper.getEnclouser());
					}
				});
			}

		for (KeyValue<String, List<ImageEnclosureDTO>> enclosureKeyValue : listOfImages) {

			boolean statue = false;
			for (EnclosuresDTO enclosures : enclosureDtos) {
				Optional<ImageEnclosureDTO> value = enclosureKeyValue.getValue().stream()
						.filter(dto -> dto.getImageType().equalsIgnoreCase(enclosures.getProof())).findFirst();
				if (value.isPresent()) {
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

		return rejectedEnclosures;
	}

	@Override
	public Boolean uploadEnclosures(String applicationNo, List<ImageInput> images, MultipartFile[] uploadfiles)
			throws IOException {

		Optional<StagingRegistrationDetailsDTO> registrationDetails = stagingRegistrationDetailsSerivce
				.FindbBasedOnApplicationNo(applicationNo);

		if (!registrationDetails.isPresent()) {
			logger.error("Application  is not found [{}] .", applicationNo);
			throw new BadRequestException("Application  is not found.");
		}
		StagingRegistrationDetailsDTO StagingRegistrationDetails = registrationDetails.get();
		Boolean secoundOrInvalidTaxStatus = uploadEnclosures(StagingRegistrationDetails, images, uploadfiles);
		/*
		 * if (!secoundOrInvalidTaxStatus) {
		 * StagingRegistrationDetails.setApplicationStatus(StatusRegistration.REUPLOAD.
		 * getDescription());
		 * stagingRegistrationDetails.save(StagingRegistrationDetails); }
		 */
		return secoundOrInvalidTaxStatus;
	}

	private Boolean uploadEnclosures(StagingRegistrationDetailsDTO registrationDetails, List<ImageInput> images,
			MultipartFile[] uploadfiles) throws IOException {

		if (!StatusRegistration.REJECTED.getDescription()
				.equalsIgnoreCase(registrationDetails.getApplicationStatus())) {
			logger.error("Application is not rejected status [{}] .", registrationDetails.getApplicationNo());
			throw new BadRequestException(
					"Application is not rejected status. " + registrationDetails.getApplicationNo());
		}
		images.forEach(imageInput -> {

			Optional<KeyValue<String, List<ImageEnclosureDTO>>> pagesOptional = getImages(
					registrationDetails.getEnclosures(), imageInput);

			if (pagesOptional.isPresent()) {

				if (pagesOptional.get().getValue().stream().anyMatch(
						status -> status.getImageStaus().equalsIgnoreCase(StatusRegistration.REJECTED.getDescription()))
						|| pagesOptional.get().getValue().stream().anyMatch(status -> status.getImageStaus()
								.equalsIgnoreCase(StatusRegistration.REUPLOAD.getDescription()))) {
					// TODO:Add equals and hashcode for KeyValue
					registrationDetails.getEnclosures().remove(pagesOptional.get());

					gridFsClient.removeImages(pagesOptional.get().getValue());
				}
			}
		});
		List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = gridFsClient.convertImages(images,
				registrationDetails.getApplicationNo(), uploadfiles, StatusRegistration.REUPLOAD.getDescription());

		registrationDetails.getEnclosures().addAll(enclosures);

		if (!registrationDetails.getEnclosures().stream().anyMatch(valu -> valu.getValue().stream().anyMatch(
				status -> status.getImageStaus().equalsIgnoreCase(StatusRegistration.REJECTED.getDescription())))) {
			if (!checkisSecondOrInvalidReject(registrationDetails)) {
				registrationDetails.setApplicationStatus(StatusRegistration.REUPLOAD.getDescription());
			}
		}
		logMovingService.moveStagingToLog(registrationDetails.getApplicationNo());
		stagingRegistrationDetails.save(registrationDetails);
		return false;

	}

	private boolean checkisSecondOrInvalidReject(StagingRegistrationDetailsDTO stagingRegDetails) {
		if (stagingRegDetails.getRejectionHistory() != null) {
			if ((stagingRegDetails.getRejectionHistory().getIsSecondVehicleRejected() != null
					&& stagingRegDetails.getRejectionHistory().getIsSecondVehicleRejected())
					|| (stagingRegDetails.getRejectionHistory().getIsInvalidVehicleRejection() != null
							&& stagingRegDetails.getRejectionHistory().getIsInvalidVehicleRejection())) {
				return Boolean.TRUE;
			}
		}
		return Boolean.FALSE;
	}

	private void saveEnclosures(StagingRegistrationDetailsDTO registrationDetails, List<ImageInput> images,
			MultipartFile[] uploadfiles) throws IOException {
		if (registrationDetails.getEnclosures() == null) {
			List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = gridFsClient.convertImages(images,
					registrationDetails.getApplicationNo(), uploadfiles, StatusRegistration.INITIATED.getDescription());

			registrationDetails.setEnclosures(enclosures);
			//TODO: Making these save as unique once QA certified remove below code
			/*
			 * logMovingService.moveStagingToLog(registrationDetails.getApplicationNo());
			 * stagingRegistrationDetails.save(registrationDetails);
			 */
			
		} else {

			if (registrationDetails.getEnclosures().stream().anyMatch(valu -> valu.getValue().stream().anyMatch(
					status -> status.getImageStaus().equalsIgnoreCase(StatusRegistration.REJECTED.getDescription())))
					|| registrationDetails.getEnclosures().stream()
							.anyMatch(valu -> valu.getValue().stream().anyMatch(status -> status.getImageStaus()
									.equalsIgnoreCase(StatusRegistration.REUPLOAD.getDescription())))) {

				uploadEnclosures(registrationDetails, images, uploadfiles);
			} else {
				images.forEach(imageInput -> {

					Optional<KeyValue<String, List<ImageEnclosureDTO>>> pagesOptional = getImages(
							registrationDetails.getEnclosures(), imageInput);
					if (pagesOptional.isPresent()) {
						registrationDetails.getEnclosures().remove(pagesOptional.get());
						gridFsClient.removeImages(pagesOptional.get().getValue());
					}

				});

				List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = gridFsClient.convertImages(images,
						registrationDetails.getApplicationNo(), uploadfiles,
						StatusRegistration.INITIATED.getDescription());

				registrationDetails.getEnclosures().addAll(enclosures);
			}
			//TODO: Making these save as unique once QA certified remove below code
			/*
			 * logMovingService.moveStagingToLog(registrationDetails.getApplicationNo());
			 * stagingRegistrationDetails.save(registrationDetails);
			 */
		}
		
		logMovingService.moveStagingToLog(registrationDetails.getApplicationNo());
		stagingRegistrationDetails.save(registrationDetails);
	}

	public Optional<KeyValue<String, List<ImageEnclosureDTO>>> getImages(
			List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures, ImageInput imageInput) {
		List<EnclosuresDTO> dtos = enclosuresDAO.findByServiceID(ServiceEnum.TEMPORARYREGISTRATION.getId());
		if (dtos.isEmpty()) {
			logger.error("Enclosures  is not found in master. [master_enclosures].");
			throw new BadRequestException("Enclosures  is not found in master_enclosures.");
		}

		logger.info("imageInput [{}]", imageInput.getType());
		if( imageInput!=null && imageInput.getType().equalsIgnoreCase("EngineNumber")) {
			dtos.add(enclosuresMapper.getEnclouser());
		}
		for (EnclosuresDTO e : dtos) {
			logger.debug("enclosure proof[{}]", e.getProof());
		}
		Optional<EnclosuresDTO> images = dtos.stream()
				.filter(dto -> dto.getProof().equalsIgnoreCase(imageInput.getType())).findFirst();

		if (!images.isPresent()) {
			return Optional.empty();
		}
		return enclosures.stream().filter(e -> images.get().getProof().equalsIgnoreCase(e.getKey())).findFirst();
	}

	/*
	 * @Override public List<EnclosureRejectedVO>
	 * getListOfSupportedEnclosuresForService(String serviceName) {
	 * 
	 * Pair<Boolean, List<EnclosureRejectedVO>> enclosure =
	 * getListOfSupportedEnclosureDetailsByService(serviceName);
	 * 
	 * List<EnclosureRejectedVO> listOfEnclosures = enclosure.getSecond();
	 * 
	 * return listOfEnclosures; }
	 */

	/*
	 * private Pair<Boolean, List<EnclosureRejectedVO>>
	 * getListOfSupportedEnclosureDetailsByService(String serviceType) {
	 * 
	 * List<Integer> serviceId = new ArrayList<>();
	 * if(ServiceEnum.DATAENTRY.getCode().equals(serviceType))
	 * serviceId.add(ServiceEnum.DATAENTRY.getId());
	 * if(ServiceEnum.TEMPORARYREGISTRATION.getCode().equals(serviceType))
	 * serviceId.add(ServiceEnum.TEMPORARYREGISTRATION.getId());
	 * 
	 * 
	 * 
	 * List<EnclosuresDTO> dtos = enclosuresDAO.findByServiceIDIn(serviceId);
	 * //List<EnclosureType> enclosuresTypes = null; boolean uploadStatus = false;
	 * List<EnclosureRejectedVO> rejectedEnclosures = new ArrayList<>(); if
	 * (dtos.isEmpty()) { throw new
	 * BadRequestException("Enclosures  is not found."); } dtos.sort((p1, p2) ->
	 * p1.getSlNo().compareTo(p2.getSlNo()));
	 * 
	 * Optional<StagingRegistrationDetailsDTO> registrationDetails =
	 * stagingRegistrationDetailsSerivce .FindbBasedOnApplicationNo(applicationNo);
	 * 
	 * if (!registrationDetails.isPresent()) {
	 * logger.error("Enclosures  is not found [{}] ." + applicationNo); throw new
	 * BadRequestException("Application  is not found."); } //if
	 * (registrationDetails.get().getEnclosures() == null) { Map<String,
	 * List<ImageVO>> typeToVOs = new HashMap<>();
	 * 
	 * for (EnclosuresDTO dto : dtos) {
	 * 
	 * if (!checkImagesApplicableOrNot(dto, registrationDetails)) { continue; } if
	 * (dto.isRequired()) { uploadStatus = true; } typeToVOs.put(dto.getProof(),
	 * enclosureImageMapper.convertdtoToVo(dto)); } List<EnclosureRejectedVO>
	 * enclosures = typeToVOs.keySet().stream() .map(k -> new EnclosureRejectedVO(k,
	 * typeToVOs.get(k))).collect(Collectors.toList()); enclosures.sort((p1, p2) ->
	 * p1.getSlNo().compareTo(p2.getSlNo())); return Pair.of(uploadStatus,
	 * enclosures);
	 * 
	 * } else {
	 * 
	 * // DIsplay Rejected Image if
	 * (checkApplicationStatus(registrationDetails.get())) {
	 * registrationDetails.get().getEnclosures().stream() .forEach(values ->
	 * values.getValue().stream().forEach(value -> { if
	 * (value.getImageStaus().equalsIgnoreCase(StatusRegistration.REUPLOAD.
	 * getDescription()) || value.getImageStaus()
	 * .equalsIgnoreCase(StatusRegistration.REJECTED.getDescription())) {
	 * rejectedEnclosures.add(new EnclosureRejectedVO(
	 * values.getValue().stream().findFirst().get().getImageType(),
	 * enclosureImageMapper.convertNewEntity(Arrays.asList(value)))); } }));
	 * 
	 * if (registrationDetails.get().getEnclosures().stream() .anyMatch(images ->
	 * images.getValue().stream().anyMatch(status -> status.getImageStaus()
	 * .equalsIgnoreCase(StatusRegistration.REJECTED.getDescription())))) {
	 * uploadStatus = Boolean.TRUE; } // return rejectedEnclosures; return
	 * Pair.of(uploadStatus, rejectedEnclosures); }
	 * 
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
	 * rejectedEnclosures); } }
	 */
	
	private void removeMutipleEngineNumberImages(StagingRegistrationDetailsDTO registrationDetails) {
		 LocalDateTime custDate1 = LocalDateTime.of(2019, 07, 20, 10, 30);
			if(registrationDetails.getTrGeneratedDate()!=null && registrationDetails.getTrGeneratedDate().isBefore(custDate1)
					&&CollectionUtils.isNotEmpty(registrationDetails.getEnclosures())) {
				Boolean flag = Boolean.FALSE;
				List<KeyValue<String, List<ImageEnclosureDTO>>> engineNumRejImgs = registrationDetails.getEnclosures();
				for(KeyValue<String, List<ImageEnclosureDTO>> list :engineNumRejImgs) {
					if(list.getKey().equalsIgnoreCase("EngineNumber") && list.getValue().get(0).getImageStaus().equalsIgnoreCase("REJECTED")) {
						flag =  Boolean.TRUE;
					}
				}
				if(flag) {
				engineNumRejImgs.removeIf(s -> s.getKey().equals("EngineNumber") && s.getValue()!=null &&
						s.getValue().get(0).getImageStaus().equalsIgnoreCase("REUPLOAD"));
				registrationDetails.setEnclosures(engineNumRejImgs);
				stagingRegistrationDetails.save(registrationDetails);
				}
			}
	}

}