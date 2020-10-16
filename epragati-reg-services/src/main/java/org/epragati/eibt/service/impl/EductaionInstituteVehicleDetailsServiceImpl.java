package org.epragati.eibt.service.impl;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.ConnectException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFDataFormat;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.epragati.aadhaar.AadharDetailsResponseVO;
import org.epragati.aadhaarAPI.AadhaarSourceDTO;
import org.epragati.common.vo.UserStatusEnum;
import org.epragati.constants.CovCategory;
import org.epragati.constants.OfficeType;
import org.epragati.dispatcher.dto.DispatcherSubmissionDTO;
import org.epragati.dto.enclosure.ImageEnclosureDTO;
import org.epragati.eductionInstitute.mapper.EductaionInstituteVehicleDetailsMapper;
import org.epragati.eductionInstitute.mapper.StudentDetailsMapper;
import org.epragati.eibt.service.EductaionInstituteVehicleDetailsService;
import org.epragati.excel.service.CellProps;
import org.epragati.excel.service.ExcelService;
import org.epragati.excel.service.ExcelServiceImpl;
import org.epragati.exception.BadRequestException;
import org.epragati.images.vo.ImageInput;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dao.EductaionInstituteVehicleDetailsDao;
import org.epragati.master.dao.EductaionInstituteVehicleDetailsLogDAO;
import org.epragati.master.dao.FcDetailsDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.TaxDetailsDAO;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dto.AadhaarDetailsResponseDTO;
import org.epragati.master.dto.BloodGroupDTO;
import org.epragati.master.dto.EductaionInstituteVehicleDetailsDto;
import org.epragati.master.dto.EductaionInstituteVehicleDetailsLogDto;
import org.epragati.master.dto.FcDetailsDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.RolesDTO;
import org.epragati.master.dto.StudentDetailsDto;
import org.epragati.master.dto.TaxComponentDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.mappers.AadhaarDetailsResponseMapper;
import org.epragati.master.mappers.OfficeMapper;
import org.epragati.master.mappers.UserMapper;
import org.epragati.master.vo.ApplicantSearchWithOutIdInput;
import org.epragati.master.vo.AttendantDetailsVO;
import org.epragati.master.vo.CountryVO;
import org.epragati.master.vo.DLResponceVO;
import org.epragati.master.vo.DlDetailsVO;
import org.epragati.master.vo.DriverDetailsVO;
import org.epragati.master.vo.EductaionInstituteVehicleDetailsVO;
import org.epragati.master.vo.EibtSearchVO;
import org.epragati.master.vo.OfficeVO;
import org.epragati.master.vo.PostOfficeVO;
import org.epragati.master.vo.StudentDetailsVO;
import org.epragati.master.vo.UserVO;
import org.epragati.master.vo.VillageVO;
import org.epragati.payments.vo.ClassOfVehiclesVO;
import org.epragati.permits.dao.PermitDetailsDAO;
import org.epragati.permits.dto.PermitDetailsDTO;
import org.epragati.regservice.RegistrationService;
import org.epragati.regservice.vo.RcValidationVO;
import org.epragati.rta.vo.RtaActionVO;
import org.epragati.service.enclosure.mapper.EnclosureImageMapper;
import org.epragati.service.enclosure.vo.EnclosureRejectedVO;
import org.epragati.service.enclosure.vo.ImageVO;
import org.epragati.service.files.GridFsClient;
import org.epragati.util.PermitsEnum;
import org.epragati.util.RoleEnum;
import org.epragati.util.StatusRegistration;
import org.epragati.util.document.KeyValue;
import org.epragati.util.payment.ClassOfVehicleEnum;
import org.epragati.util.payment.ServiceCodeEnum;
import org.epragati.util.payment.ServiceEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class EductaionInstituteVehicleDetailsServiceImpl implements EductaionInstituteVehicleDetailsService {

	private static final Logger logger = LoggerFactory.getLogger(EductaionInstituteVehicleDetailsServiceImpl.class);
	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;
	@Autowired
	private EductaionInstituteVehicleDetailsDao eductaionInstituteVehicleDetailsDao;
	@Autowired
	private GridFsClient gridFsClient;

	@Value("${reg.driver.details.url:http://10.80.1.161:8443/dl/searchDlDataForEibtRegister}")
	private String driverDetailsUrl;

	@Autowired
	private EductaionInstituteVehicleDetailsMapper eductaionInstituteVehicleDetailsMapper;

	@Autowired
	private EductaionInstituteVehicleDetailsLogDAO eductaionInstituteVehicleDetailsLogDAO;

	@Autowired
	private EnclosureImageMapper enclosureImageMapper;

	@Autowired
	private TaxDetailsDAO taxDetailsDAO;

	@Autowired
	private RegistrationService registrationService;

	@Autowired
	private FcDetailsDAO fcDetailsDAO;
	@Autowired
	private PermitDetailsDAO permitDetailsDAO;
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	@Autowired
	private OfficeDAO officeDAO;

	@Autowired
	private OfficeMapper officeMapper;

	@Autowired
	private UserMapper userMapper;

	@Value("${financier.password:}")
	private String financierPassword;

	@Autowired
	private UserDAO userDao;
	@Autowired
	private StudentDetailsMapper studentDetailsMapper;
	@Autowired
	private AadhaarDetailsResponseMapper adddharMapper;

	@Override
	public EibtSearchVO getVehicleDetails(String prNo, boolean requetFromSave) {

		if (StringUtils.isBlank(prNo)) {
			logger.error("PR number not found [{}]", prNo);
			throw new BadRequestException("PR number not found");
		}

		Optional<RegistrationDetailsDTO> regOptional = registrationDetailDAO.findByPrNo(prNo);
		if (!regOptional.isPresent()) {
			logger.error("No record found for PrNo: [{}]", prNo);
			throw new BadRequestException("No record found for PrNo: " + prNo);
		}
		RegistrationDetailsDTO dto = regOptional.get();
		if (!dto.getClassOfVehicle().equalsIgnoreCase(ClassOfVehicleEnum.EIBT.getCovCode())) {
			logger.error("The given pr number is not related to education bus [{}]", prNo);
			throw new BadRequestException("The given pr number is not related to education bus. " + prNo);
		}
		Optional<EductaionInstituteVehicleDetailsDto> eibtVehicleDetails = eductaionInstituteVehicleDetailsDao
				.findByApplicationNo(dto.getApplicationNo());
		if (eibtVehicleDetails.isPresent()) {
			if (!requetFromSave && !eibtVehicleDetails.get().isTowDone()) {
				logger.error("Vehicle already registered [{}]", prNo);
				throw new BadRequestException("Vehicle already registered. " + prNo);
			}
		}
		EibtSearchVO vo = new EibtSearchVO();
		if (dto.getApplicantDetails() != null && StringUtils.isNoneBlank(dto.getApplicantDetails().getDisplayName())) {
			vo.setOwnerName(dto.getApplicantDetails().getDisplayName());
		}
		if (StringUtils.isNoneBlank(dto.getPrNo())) {
			vo.setPrNo(dto.getPrNo());
		}
		if (dto.getPrGeneratedDate() != null) {
			vo.setRegistrationDate(dto.getPrGeneratedDate().toLocalDate());
		} else {
			if (dto.getRegistrationValidity() != null && dto.getRegistrationValidity().getPrGeneratedDate() != null) {
				vo.setRegistrationDate(dto.getRegistrationValidity().getPrGeneratedDate());
			}
		}
		List<TaxDetailsDTO> taxDetailsList = taxDetailsDAO
				.findFirst10ByApplicationNoAndPaymentPeriodInOrderByCreatedDateDesc(dto.getApplicationNo(), taxTypes());
		if (taxDetailsList.isEmpty()) {
			logger.error("tax details not found [{}]", prNo);
			throw new BadRequestException("tax details not found. " + prNo);
		}
		registrationService.updatePaidDateAsCreatedDate(taxDetailsList);
		taxDetailsList.sort((p1, p2) -> p2.getCreatedDate().compareTo(p1.getCreatedDate()));
		TaxDetailsDTO taxDto = taxDetailsList.stream().findFirst().get();
		if (taxDto.getTaxDetails() == null) {
			logger.error("TaxDetails map not found: [{}]", prNo);
			throw new BadRequestException("TaxDetails map not found:" + prNo);
		}
		for (Map<String, TaxComponentDTO> mapDto : taxDto.getTaxDetails()) {
			for (Entry<String, TaxComponentDTO> entry : mapDto.entrySet()) {
				if (taxTypes().stream().anyMatch(id -> id.equalsIgnoreCase(entry.getKey()))) {
					if (entry.getValue().getValidityTo() != null) {
						vo.setTaxValidity(entry.getValue().getValidityTo());
					}
				}
			}
		}
		taxDetailsList.clear();
		List<FcDetailsDTO> listOfcDetails = fcDetailsDAO.findFirst5ByPrNoOrderByCreatedDateDesc(prNo);
		if (listOfcDetails.isEmpty()) {
			logger.error("FC details not found: [{}]", prNo);
			throw new BadRequestException("FC details not found:" + prNo);
		}
		FcDetailsDTO fcDto = listOfcDetails.stream().findFirst().get();
		if (StringUtils.isNoneBlank(fcDto.getFcNumber())) {
			vo.setFcNumber(fcDto.getFcNumber());
		}
		if (fcDto.getFcValidUpto() != null) {
			vo.setFcValidity(fcDto.getFcValidUpto());
		}
		List<PermitDetailsDTO> listOfPermits = permitDetailsDAO.findByPrNoAndPermitStatus(prNo,
				PermitsEnum.ACTIVE.getDescription());
		if (!listOfPermits.isEmpty()) {
			PermitDetailsDTO listOfPermsasits = listOfPermits.stream().filter(type -> type.getPermitType()
					.getTypeofPermit().equalsIgnoreCase(PermitsEnum.PermitType.PRIMARY.getPermitTypeCode())).findAny()
					.get();
			if (listOfPermsasits != null) {
				if (StringUtils.isNoneBlank(listOfPermsasits.getPermitNo())) {
					vo.setPermitNumber(listOfPermsasits.getPermitNo());
				}
				if (listOfPermsasits.getPermitValidityDetails() != null
						&& listOfPermsasits.getPermitValidityDetails().getPermitValidTo() != null) {
					vo.setPermitValidity(listOfPermsasits.getPermitValidityDetails().getPermitValidTo());
				}
			}
		}
		if (requetFromSave) {
			if (StringUtils.isNoneBlank(dto.getApplicationNo())) {
				vo.setApplicationNo(dto.getApplicationNo());
			}
			if (dto.getApplicantDetails() != null && StringUtils.isNoneBlank(dto.getApplicantDetails().getAadharNo())) {
				vo.setAadharNo(dto.getApplicantDetails().getAadharNo());
			}
		}
		return vo;
	}

	@Override
	public DriverDetailsVO getDlDetails(ApplicantSearchWithOutIdInput input) {
		ResponseEntity<String> result = null;
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ApplicantSearchWithOutIdInput applicantSearchWithOutIdInput = new ApplicantSearchWithOutIdInput();
		applicantSearchWithOutIdInput.setDlNumber(input.getDlNumber());
		// DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d-MM-yyyy");
		// LocalDate slotDate = LocalDate.parse(input.get, formatter);
		applicantSearchWithOutIdInput.setDob(input.getDob());
		applicantSearchWithOutIdInput.setOfficeCode(input.getOfficeCode());
		HttpEntity<ApplicantSearchWithOutIdInput> httpEntity = new HttpEntity<>(applicantSearchWithOutIdInput, headers);
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
		if (result == null || StringUtils.isBlank(result.getBody())) {
			logger.error("Exception while calling DL details ");
			throw new BadRequestException("Exception while calling DL details");
		}
		try {
			DLResponceVO resultOpt = objectMapper.readValue(result.getBody(), DLResponceVO.class);
			if (resultOpt == null) {
				logger.error("no recpords for rest call");
				throw new BadRequestException("no recpords for rest call");
			}
			if (resultOpt.getResult() == null) {
				logger.error(resultOpt.getMessage());
				throw new BadRequestException(resultOpt.getMessage());
			}
			DriverDetailsVO drivareDetais = mapDlDetails(resultOpt.getResult(), input);

			// TODO need to check dl status ACTIVE
			if (StringUtils.isBlank(drivareDetais.getDlStatus())) {
				logger.error("DL status not found for dlNo: [{}]", input.getDlNumber());
				throw new BadRequestException("DL status not found for dlNo: " + input.getDlNumber());
			}
			if (!drivareDetais.getDlStatus().equalsIgnoreCase("ACTIVE")) {
				logger.error("DL is not in actve: [{}]", input.getDlNumber());
				throw new BadRequestException("DL is not in actve: " + input.getDlNumber());
			}
			Optional<EductaionInstituteVehicleDetailsDto> eibtVehicleDetails = eductaionInstituteVehicleDetailsDao
					.findBydriverDetailsDlNo(input.getDlNumber());
			if (eibtVehicleDetails.isPresent()) {
				if (eibtVehicleDetails.get().getDriverDetails().getDlNo().equalsIgnoreCase(drivareDetais.getDlNo())
						&& eibtVehicleDetails.get().getDriverDetails().getFirstIssueOfficeCode()
								.equalsIgnoreCase(drivareDetais.getFirstIssueOfficeCode())) {
					if (!input.isEditDetails() && !eibtVehicleDetails.get().isTowDone()) {
						logger.error("This DL number already mapped to [{}]", eibtVehicleDetails.get().getPrNo(),
								"plz try with other DL number");
						throw new BadRequestException("This DL number already mapped to "
								+ eibtVehicleDetails.get().getPrNo() + ", plz try with other DL number ");
					}

				}
			}
			return drivareDetais;
		} catch (IOException e) {
			logger.debug("Error while fetching the data for DL [{}]", e);
			logger.error("Error while fetching the data for DL [{}]", e.getMessage());
			throw new BadRequestException("Error while fetching the data for DL: " + e);
		}
	}

	private DriverDetailsVO mapDlDetails(DlDetailsVO dlDetailsvo, ApplicantSearchWithOutIdInput input) {
		DriverDetailsVO vo = new DriverDetailsVO();
		if (StringUtils.isNoneBlank(dlDetailsvo.getDlStatus())) {
			vo.setDlStatus(dlDetailsvo.getDlStatus());
		}
		if (dlDetailsvo.getApplicantDetails() != null) {
			if (StringUtils.isNoneBlank(dlDetailsvo.getApplicantDetails().getDisplayName())) {
				vo.setDriverName(dlDetailsvo.getApplicantDetails().getDisplayName());
			}

			if (dlDetailsvo.getApplicantDetails().getDob() != null) {
				vo.setDob(dlDetailsvo.getApplicantDetails().getDob());
			}
			if (StringUtils.isNoneBlank(dlDetailsvo.getApplicantDetails().getFatherName())) {
				vo.setCareOf(dlDetailsvo.getApplicantDetails().getFatherName());
			}
			if (StringUtils.isNoneBlank(dlDetailsvo.getApplicantDetails().getGender())) {
				vo.setGender(dlDetailsvo.getApplicantDetails().getGender());
			}
			if (dlDetailsvo.getApplicantDetails().getPresentAddress() != null) {
				vo.setPresentAddress(dlDetailsvo.getApplicantDetails().getPresentAddress());

				if (dlDetailsvo.getApplicantDetails().getPresentAddress().getCountryVO() != null) {
					CountryVO countryVo = new CountryVO();
					if (dlDetailsvo.getApplicantDetails().getPresentAddress().getCountryVO().getId() != null) {
						countryVo.setCountryId(
								dlDetailsvo.getApplicantDetails().getPresentAddress().getCountryVO().getId());
					}
					if (dlDetailsvo.getApplicantDetails().getPresentAddress().getCountryVO().getName() != null) {
						countryVo.setCountryName(
								dlDetailsvo.getApplicantDetails().getPresentAddress().getCountryVO().getName());
					}
					if (dlDetailsvo.getApplicantDetails().getPresentAddress().getCountryVO().getCountryCode() != null) {
						countryVo.setCountryCode(
								dlDetailsvo.getApplicantDetails().getPresentAddress().getCountryVO().getCountryCode());
					}
					vo.getPresentAddress().setCountry(countryVo);
				}

				if (dlDetailsvo.getApplicantDetails().getPresentAddress().getVillageVO() != null) {
					VillageVO villageVO = new VillageVO();
					if (dlDetailsvo.getApplicantDetails().getPresentAddress().getVillageVO().getId() != null) {
						villageVO.setVillageId(
								dlDetailsvo.getApplicantDetails().getPresentAddress().getVillageVO().getId());
					}
					if (dlDetailsvo.getApplicantDetails().getPresentAddress().getVillageVO().getName() != null) {
						villageVO.setVillageName(
								dlDetailsvo.getApplicantDetails().getPresentAddress().getVillageVO().getName());
					}
					if (dlDetailsvo.getApplicantDetails().getPresentAddress().getVillageVO().getMandalId() != null) {
						villageVO.setMandalId(
								dlDetailsvo.getApplicantDetails().getPresentAddress().getVillageVO().getMandalId());
					}
					vo.getPresentAddress().setVillage(villageVO);
				}
				if (dlDetailsvo.getApplicantDetails().getPresentAddress().getPostOfficeVO() != null) {
					PostOfficeVO postOfficeVO = new PostOfficeVO();
					if (dlDetailsvo.getApplicantDetails().getPresentAddress().getPostOfficeVO().getId() != null) {
						postOfficeVO.setPostOfficeId(
								dlDetailsvo.getApplicantDetails().getPresentAddress().getPostOfficeVO().getId());
					}
					if (dlDetailsvo.getApplicantDetails().getPresentAddress().getPostOfficeVO().getName() != null) {
						postOfficeVO.setPostOfficeName(
								dlDetailsvo.getApplicantDetails().getPresentAddress().getPostOfficeVO().getName());
					}
					if (dlDetailsvo.getApplicantDetails().getPresentAddress().getPostOfficeVO().getPincode() != null) {
						postOfficeVO.setPostOfficeCode(
								dlDetailsvo.getApplicantDetails().getPresentAddress().getPostOfficeVO().getPincode());
					}
					if (dlDetailsvo.getApplicantDetails().getPresentAddress().getPostOfficeVO()
							.getDistrictId() != null) {
						postOfficeVO.setDistrict(dlDetailsvo.getApplicantDetails().getPresentAddress().getPostOfficeVO()
								.getDistrictId());
					}
					vo.getPresentAddress().setPostOffice(postOfficeVO);
				}
			}

			if (StringUtils.isNoneBlank(dlDetailsvo.getApplicantDetails().getMobile())) {
				vo.setMobile(dlDetailsvo.getApplicantDetails().getMobile());
			}
			if (StringUtils.isNoneBlank(dlDetailsvo.getApplicantDetails().getAadharNo())) {
				vo.setAadharNo(dlDetailsvo.getApplicantDetails().getAadharNo());
			}
		}
		if (StringUtils.isNoneBlank(dlDetailsvo.getDlNo())) {
			vo.setDlNo(dlDetailsvo.getDlNo());
		}
		if (StringUtils.isNoneBlank(dlDetailsvo.getFirstIssueOfficeCode())) {
			vo.setFirstIssueOfficeCode(dlDetailsvo.getFirstIssueOfficeCode());

		}
		if (StringUtils.isNoneBlank(dlDetailsvo.getOfficeCode())) {
			// vo.setFirstIssueOfficeCode(dlDetailsvo.getOfficeCode());
			Optional<OfficeDTO> optionalOfficeDetails = officeDAO.findByOfficeCode(dlDetailsvo.getOfficeCode());
			if (!optionalOfficeDetails.isPresent()) {
				throw new BadRequestException(
						"Office details not found from master for office code :" + dlDetailsvo.getOfficeCode());
			}
			// vo.setFirstIssueOfficeName(optionalOfficeDetails.get().getOfficeName());
			vo.setOfficeName(optionalOfficeDetails.get().getOfficeName());
			vo.setOfficeCode(dlDetailsvo.getOfficeCode());
		}
		if (dlDetailsvo.getFirstIssueDate() != null) {
			vo.setDlIssueDate(dlDetailsvo.getFirstIssueDate());
		}
		/*
		 * if (StringUtils.isBlank(dlDetailsvo.getBadgeNo())) { throw new
		 * BadRequestException("Driver dont have the badge"); }
		 */
		vo.setBadgeNo(dlDetailsvo.getBadgeNo());
		/*
		 * if(dlDetailsvo.getBadgeNoIssueDate() == null ) { throw new
		 * BadRequestException("Badge issue date missed"); } Double vehicleAge =
		 * calculateAgeOfTheVehicle(dlDetailsvo.getBadgeNoIssueDate(), LocalDate.now());
		 * if(vehicleAge<5) { throw new
		 * BadRequestException("Driver should have more than 5 years experience"); }
		 * vo.setDriverExperience(vehicleAge);
		 */

		if (dlDetailsvo.getNonTransportValidTo() != null) {
			vo.setNonTransportValidity(dlDetailsvo.getNonTransportValidTo());
		}
		if (dlDetailsvo.getTransportValidTo() != null) {
			vo.setTransportValidity(dlDetailsvo.getTransportValidTo());
		}
		if (dlDetailsvo.getApprovedCovs() == null || dlDetailsvo.getApprovedCovs().isEmpty()) {
			logger.error("Class of vehicles not found: [{}]", input.getDlNumber());
			throw new BadRequestException("Class of vehicles not found: " + input.getDlNumber());
		}
		if (dlDetailsvo.getApprovedCovs() != null && !dlDetailsvo.getApprovedCovs().isEmpty()) {
			List<String> nonTransportCovs = new ArrayList<>();
			List<String> trasportCovs = new ArrayList<>();
			for (ClassOfVehiclesVO classOfVehicleVo : dlDetailsvo.getApprovedCovs()) {
				if (classOfVehicleVo.getCategory().equalsIgnoreCase(CovCategory.T.getCode())) {
					trasportCovs.add(classOfVehicleVo.getDescription());
				} else if (classOfVehicleVo.getCategory().equalsIgnoreCase(CovCategory.N.getCode())) {
					nonTransportCovs.add(classOfVehicleVo.getDescription());
				}
			}
			vo.setNonTransportCovs(nonTransportCovs);
			vo.setTrasportCovs(trasportCovs);
		}
		Optional<RegistrationDetailsDTO> regOptional = registrationDetailDAO.findByPrNo(input.getPrNo());
		if (!regOptional.isPresent()) {
			logger.error("No record found for PrNo: [{}]", input.getPrNo());
			throw new BadRequestException("No record found for PrNo: " + input.getPrNo());
		}
		RegistrationDetailsDTO dto = regOptional.get();

		if (dto.getVahanDetails().getGvw() <= 7500) {
			if (dlDetailsvo.getTransportValidTo() == null && dlDetailsvo.getNonTransportValidTo() != null
					&& dlDetailsvo.getApprovedCovs().stream().anyMatch(cov -> cov.getCode().equalsIgnoreCase("LMVNT"))
					&& (LocalDate.now().equals(dlDetailsvo.getNonTransportValidTo())
							|| LocalDate.now().isAfter(dlDetailsvo.getNonTransportValidTo()))) {
				logger.error("Please renewal the DL: [{}]", input.getDlNumber());
				throw new BadRequestException("Please renewal the DL: " + input.getDlNumber());
			} else if (dlDetailsvo.getTransportValidTo() != null && dlDetailsvo.getNonTransportValidTo() != null
					&& dlDetailsvo.getApprovedCovs().stream().anyMatch(cov -> cov.getCode().equalsIgnoreCase("LMVNT"))
					&& (LocalDate.now().equals(dlDetailsvo.getNonTransportValidTo())
							|| LocalDate.now().isAfter(dlDetailsvo.getNonTransportValidTo()))
					&& (LocalDate.now().equals(dlDetailsvo.getTransportValidTo())
							|| LocalDate.now().isAfter(dlDetailsvo.getTransportValidTo()))) {
				logger.error("Please renewal the DL: [{}]", input.getDlNumber());
				throw new BadRequestException("Please renewal the DL: " + input.getDlNumber());
			} else if (dlDetailsvo.getTransportValidTo() != null && dlDetailsvo.getNonTransportValidTo() == null
					&& (LocalDate.now().equals(dlDetailsvo.getTransportValidTo())
							|| LocalDate.now().isAfter(dlDetailsvo.getTransportValidTo()))) {
				logger.error("Please renewal the DL: [{}]", input.getDlNumber());
				throw new BadRequestException("Please renewal the DL: " + input.getDlNumber());
			}
		} else {
			if (dlDetailsvo.getTransportValidTo() == null || (dlDetailsvo.getTransportValidTo() != null
					&& (LocalDate.now().equals(dlDetailsvo.getTransportValidTo())
							|| LocalDate.now().isAfter(dlDetailsvo.getTransportValidTo())))) {
				logger.error("Please renewal the DL: [{}]", input.getDlNumber());
				throw new BadRequestException("Please renewal the DL: " + input.getDlNumber());
			}
		}

		if (dlDetailsvo.getTransportValidTo() == null) {
			if (StringUtils.isBlank(input.getPrNo())) {
				logger.error("Please provide vehicle number: [{}]", input.getDlNumber());
				throw new BadRequestException("Please provide vehicle number: " + input.getDlNumber());
			}

			if (dto.getVahanDetails().getGvw() == null || dto.getVahanDetails().getGvw() == 0) {
				logger.error("GVW not found for vehicle number: [{}]", input.getPrNo());
				throw new BadRequestException("GVW not found for vehicle number: " + input.getPrNo());
			}
			if (dto.getVahanDetails().getGvw() <= 7500) {
				if (vo.getNonTransportCovs() == null || vo.getNonTransportCovs().isEmpty()) {
					logger.error("No light motor vehicle for dl number : [{}]", input.getDlNumber());
					throw new BadRequestException("No light motor vehicle for dl number : " + input.getDlNumber());
				}
				if (!dlDetailsvo.getApprovedCovs().stream().anyMatch(cov -> cov.getCode().equalsIgnoreCase("LMVNT"))) {
					logger.error("No light motor vehicle for dl number : [{}]", input.getDlNumber());
					throw new BadRequestException("No light motor vehicle for dl number : " + input.getDlNumber());
				}
			} else {
				logger.error("No transport class of vehicle for dl number : [{}]", input.getDlNumber());
				throw new BadRequestException(
						"No transport class of vehicle for dl number : [{}]" + input.getDlNumber());
			}

		}
		if (!input.getOfficeCode().equalsIgnoreCase(dlDetailsvo.getOfficeCode())) {
			logger.error("Please select correct office for dlNo: [{}]", input.getDlNumber());
			throw new BadRequestException("Please select correct office for dlNo: " + input.getDlNumber());
		}

		if (StringUtils.isNoneBlank(dlDetailsvo.getBase64())) {
			vo.setBase64(dlDetailsvo.getBase64());
		} else {
			vo.setEnableDriverThumb(Boolean.TRUE);
		}
		return vo;

	}

	@Override
	public List<OfficeVO> getAllOffices() {
		logger.debug("Start of getAllOffices()...");
		List<String> officeTypeList = Arrays.asList(OfficeType.MVI.getCode(), OfficeType.RTA.getCode(),
				OfficeType.UNI.getCode());
		return officeMapper.convertEntity(officeDAO.findByTypeIn(officeTypeList));
	}

	@Override
	public String saveEibtDetails(AttendantDetailsVO vo, String userId) throws IOException {
		if (StringUtils.isBlank(vo.getPrNo())) {
			logger.error("Please provide pr number");
			throw new BadRequestException("Please provide pr number");
		}
		if (StringUtils.isBlank(vo.getDlNo())) {
			logger.error("Please provide DL number");
			throw new BadRequestException("Please provide DL number");
		}
		if (StringUtils.isBlank(vo.getOfficeCode())) {
			logger.error("Please provide DL firdt issue office ");
			throw new BadRequestException("Please provide DL firdt issue office");
		}
		if (vo.getDriverDob() == null) {
			logger.error("Please provide driver date of birth");
			throw new BadRequestException("Please provide driver date of birth");
		}
		if (StringUtils.isBlank(vo.getRoute())) {
			logger.error("Please provide vehicle route");
			throw new BadRequestException("Please provide vehicle route");
		}

		EibtSearchVO vehicleDetails = this.getVehicleDetails(vo.getPrNo(), Boolean.TRUE);
		ApplicantSearchWithOutIdInput input = new ApplicantSearchWithOutIdInput();
		input.setDlNumber(vo.getDlNo());
		input.setDob(vo.getDriverDob());
		input.setOfficeCode(vo.getOfficeCode());
		input.setPrNo(vo.getPrNo());
		if (vo.getIsEditDetails() != null && vo.getIsEditDetails()) {
			input.setEditDetails(vo.getIsEditDetails());
		}
		DriverDetailsVO driverDetails = this.getDlDetails(input);

		EductaionInstituteVehicleDetailsVO instititevo = new EductaionInstituteVehicleDetailsVO();
		instititevo.setAttendantDetails(vo);
		instititevo.setDriverDetails(driverDetails);
		this.convertEibtSearchToInstituteVo(vehicleDetails, instititevo);
		instititevo.setUserId(userId);
		EductaionInstituteVehicleDetailsDto dto = eductaionInstituteVehicleDetailsMapper.convertVO(instititevo);
		dto.setCreatedDate(LocalDateTime.now());
		dto.setRoute(vo.getRoute());
		// need to move log if record is exists.

		Optional<EductaionInstituteVehicleDetailsDto> eibtVehicleDetails = eductaionInstituteVehicleDetailsDao
				.findByprNo(vo.getPrNo());
		if (eibtVehicleDetails.isPresent()) {
			if (!eibtVehicleDetails.get().getUserId().equalsIgnoreCase(userId)) {
				if (!eibtVehicleDetails.get().isTowDone()) {
					logger.error("Invalid user for : [{}]", vo.getPrNo());
					throw new BadRequestException("Invalid user for : " + vo.getPrNo());
				}
			}

			EductaionInstituteVehicleDetailsLogDto logDto = new EductaionInstituteVehicleDetailsLogDto();
			logDto.setEductaionInstituteVehicleDetails(eibtVehicleDetails.get());
			logDto.setCreatedDate(LocalDateTime.now());
			dto.setlUpdate(LocalDateTime.now());
			if (eibtVehicleDetails.get().getStudentDetails() != null
					&& !eibtVehicleDetails.get().getStudentDetails().isEmpty()) {
				dto.setStudentDetails(eibtVehicleDetails.get().getStudentDetails());
			}
			if (eibtVehicleDetails.get().getEnclosures() != null
					&& !eibtVehicleDetails.get().getEnclosures().isEmpty()) {
				dto.setEnclosures(eibtVehicleDetails.get().getEnclosures());
			}
			eductaionInstituteVehicleDetailsLogDAO.save(logDto);
		}
		if (driverDetails.isEnableDriverThumb()) {

			if (vo.getDriveraadhAarDetailsResponse() == null) {
				logger.error("Please provide driver aadhar details : [{}]", vo.getPrNo());
				throw new BadRequestException("Please provide driver aadhar details : " + vo.getPrNo());
			}
			/*
			 * AadharDetailsResponseVO aadhaarResForDriver =
			 * registrationService.getAadharResponse(vo.getDriveraadhAarDetailsRequestVO(),
			 * setAadhaarSourceDetails(vo.getPrNo(),
			 * vo.getDriveraadhAarDetailsRequestVO().getUid_num()));
			 */
			dto.getDriverDetails()
					.setAadharResponseForDriver(adddharMapper.convertVO(vo.getDriveraadhAarDetailsResponse()));
		}
		/*
		 * AadharDetailsResponseVO aadhaarResForAttender =
		 * registrationService.getAadharResponse(vo.getDriveraadhAarDetailsRequestVO(),
		 * setAadhaarSourceDetails(vo.getPrNo(),
		 * vo.getDriveraadhAarDetailsRequestVO().getUid_num()));
		 */
		dto.getAttendantDetails()
				.setAadharResponseForAttendant(adddharMapper.convertVO(vo.getAttendantAadharResponse()));
		this.saveimages(dto.getDriverDetails().getAadharResponseForDriver(), dto,
				dto.getAttendantDetails().getAadharResponseForAttendant(), driverDetails);
		eductaionInstituteVehicleDetailsDao.save(dto);
		return "Sucess";
	}

	private void saveimages(AadhaarDetailsResponseDTO driverAadhar, EductaionInstituteVehicleDetailsDto dto,
			AadhaarDetailsResponseDTO attendentAadha, DriverDetailsVO driverDetailsr) throws IOException {
		ImageInput imageInput = new ImageInput();
		imageInput.setFileOrder(0);
		imageInput.setPageNo(0);
		imageInput.setEnclosureName("Attendant Photo");
		imageInput.setType("Attendant Photo");
		byte[] imageByte = Base64.getDecoder().decode(attendentAadha.getBase64file().getBytes());

		MultipartFile[] multipartFile = new MultipartFile[1];
		multipartFile[0] = new BASE64DecodedMultipartFile(imageByte);

		List<KeyValue<String, List<ImageEnclosureDTO>>> enclosures = gridFsClient.convertImages(
				Arrays.asList(imageInput), dto.getApplicationNo(), multipartFile,
				StatusRegistration.APPROVED.getDescription());
		dto.setEnclosures(enclosures);

		byte[] imageByteForDriver = null;
		if (driverDetailsr.isEnableDriverThumb()) {
			imageByteForDriver = Base64.getDecoder().decode(driverAadhar.getBase64file().getBytes());
		} else {
			imageByteForDriver = Base64.getDecoder().decode(driverDetailsr.getBase64().getBytes());
		}
		ImageInput imageInput2 = new ImageInput();
		imageInput2.setFileOrder(0);
		imageInput2.setPageNo(0);
		imageInput2.setEnclosureName("Driver Photo");
		imageInput2.setType("Driver Photo");

		MultipartFile[] multipartFileForDriver = new MultipartFile[1];
		multipartFileForDriver[0] = new BASE64DecodedMultipartFile(imageByteForDriver);

		List<KeyValue<String, List<ImageEnclosureDTO>>> enclosuresForDriver = gridFsClient.convertImages(
				Arrays.asList(imageInput2), dto.getApplicationNo(), multipartFileForDriver,
				StatusRegistration.APPROVED.getDescription());
		dto.getEnclosures().addAll(enclosuresForDriver);

	}

	public AadhaarSourceDTO setAadhaarSourceDetails(String prNo, String aadharNo) {
		AadhaarSourceDTO aadhaarSourceDTO = new AadhaarSourceDTO();
		aadhaarSourceDTO.setPrNo(prNo);
		Set<Integer> id = new HashSet<>();
		id.add(ServiceEnum.EIBTREGISTRATION.getId());
		aadhaarSourceDTO.setServiceIds(id);
		aadhaarSourceDTO.setAadhaarNo(aadharNo);
		return aadhaarSourceDTO;
	}

	private void convertEibtSearchToInstituteVo(EibtSearchVO vehicleDetails,
			EductaionInstituteVehicleDetailsVO instititevo) {

		if (StringUtils.isNoneBlank(vehicleDetails.getApplicationNo())) {
			instititevo.setApplicationNo(vehicleDetails.getApplicationNo());
		}
		if (StringUtils.isNoneBlank(vehicleDetails.getPrNo())) {
			instititevo.setPrNo(vehicleDetails.getPrNo());
		}
		if (StringUtils.isNoneBlank(vehicleDetails.getAadharNo())) {
			instititevo.setAadharNo(vehicleDetails.getAadharNo());
		}
		if (StringUtils.isNoneBlank(vehicleDetails.getOwnerName())) {
			instititevo.setOwnerName(vehicleDetails.getOwnerName());
		}
		if (vehicleDetails.getRegistrationDate() != null) {
			instititevo.setRegistrationDate(vehicleDetails.getRegistrationDate());
		}
		if (vehicleDetails.getTaxValidity() != null) {
			instititevo.setTaxValidity(vehicleDetails.getTaxValidity());
		}
		if (StringUtils.isNoneBlank(vehicleDetails.getFcNumber())) {
			instititevo.setFcNumber(vehicleDetails.getFcNumber());
		}
		if (vehicleDetails.getFcValidity() != null) {
			instititevo.setFcValidity(vehicleDetails.getFcValidity());
		}
		if (StringUtils.isNoneBlank(vehicleDetails.getPermitNumber())) {
			instititevo.setPermitNumber(vehicleDetails.getPermitNumber());
		}
		if (vehicleDetails.getPermitValidity() != null) {
			instititevo.setPermitValidity(vehicleDetails.getPermitValidity());
		}
	}

	@Override
	public void saveEnclosuresForEibt(EductaionInstituteVehicleDetailsVO vo, JwtUser jwtUser) throws IOException {
		if (vo == null || vo.getStudentDetails() == null || vo.getStudentDetails().isEmpty()) {
			logger.error("Please provide Student details");
			throw new BadRequestException("Please provide Student details");
		}
		if (StringUtils.isBlank(vo.getPrNo())) {
			logger.error("Please provide pr number");
			throw new BadRequestException("Please provide pr number");
		}

		Optional<EductaionInstituteVehicleDetailsDto> eibtVehicleDetails = eductaionInstituteVehicleDetailsDao
				.findByprNo(vo.getPrNo());
		if (!eibtVehicleDetails.isPresent()) {
			logger.error("Please enter vehicle data for:  [{}]", vo.getPrNo());
			throw new BadRequestException("Please enter vehicle data for:  " + vo.getPrNo());
		}
		EductaionInstituteVehicleDetailsDto educationDto = eibtVehicleDetails.get();
		if (StringUtils.isBlank(educationDto.getUserId())) {
			logger.error("User id missing in vehicle resitration document for: [{}]", vo.getPrNo());
			throw new BadRequestException("User id missing in vehicle resitration document for: " + vo.getPrNo());
		}
		if (!educationDto.getUserId().equalsIgnoreCase(jwtUser.getId())) {
			logger.error("Invalid user for : [{}]", vo.getPrNo());
			throw new BadRequestException("Invalid user for : " + vo.getPrNo());
		}
		if (eibtVehicleDetails.isPresent()) {
			EductaionInstituteVehicleDetailsLogDto logDto = new EductaionInstituteVehicleDetailsLogDto();
			logDto.setEductaionInstituteVehicleDetails(eibtVehicleDetails.get());
			logDto.setCreatedDate(LocalDateTime.now());
			educationDto.setlUpdate(LocalDateTime.now());

			eductaionInstituteVehicleDetailsLogDAO.save(logDto);
		}
		List<StudentDetailsDto> studentList = studentDetailsMapper.convertVO(vo.getStudentDetails());

		educationDto.setStudentDetails(studentList);
		eductaionInstituteVehicleDetailsDao.save(educationDto);
	}

	@Override
	public EductaionInstituteVehicleDetailsVO viewEibtVehicleDetails(String prNo, JwtUser jwtUser) {
		if (StringUtils.isBlank(prNo)) {
			logger.error("PR number not found");
			throw new BadRequestException("PR number not found");
		}

		Optional<EductaionInstituteVehicleDetailsDto> eibtVehicleDetails = eductaionInstituteVehicleDetailsDao
				.findByprNo(prNo);
		if (!eibtVehicleDetails.isPresent()) {
			logger.error("Please register the vehicle. [{}]", prNo);
			throw new BadRequestException("Please register the vehicle. " + prNo);
		}
		if (!eibtVehicleDetails.get().getUserId().equalsIgnoreCase(jwtUser.getId())) {
			Optional<UserDTO> usderDto = userDao.findByUserId(jwtUser.getId());
			if (!usderDto.isPresent()) {
				logger.error("Invalid user for : [{}]", prNo);
				throw new BadRequestException("Invalid user for : " + prNo);
			}
			// TODO is need to check office code or not
			if (!RoleEnum.MVI.getName().equalsIgnoreCase(usderDto.get().getPrimaryRole().getName())) {
				if (usderDto.get().getAdditionalRoles() == null || usderDto.get().getAdditionalRoles().isEmpty()) {
					logger.error("Invalid user for : [{}]", prNo);
					throw new BadRequestException("Invalid user for : " + prNo);
				} else {
					if (!usderDto.get().getAdditionalRoles().stream()
							.anyMatch(role -> role.getName().equalsIgnoreCase(RoleEnum.MVI.getName()))) {
						logger.error("Invalid user for : [{}]", prNo);
						throw new BadRequestException("Invalid user for : " + prNo);
					}
				}

			}

		} else {
			if (eibtVehicleDetails.get().isTowDone()) {
				logger.error("Transfer of Ownership done for this vehicle .Please register the vehile");
				throw new BadRequestException(
						"Transfer of Ownership done for this vehicle .Please register the vehile");
			}
		}
		List<EnclosureRejectedVO> displayEnclosures = new ArrayList<>();
		if (eibtVehicleDetails.get().getEnclosures() != null && !eibtVehicleDetails.get().getEnclosures().isEmpty()) {
			for (KeyValue<String, List<ImageEnclosureDTO>> enclosureKeyValue : eibtVehicleDetails.get()
					.getEnclosures()) {
				List<ImageVO> imagesVO = enclosureImageMapper.convertNewEntity(enclosureKeyValue.getValue());
				displayEnclosures.add(new EnclosureRejectedVO(imagesVO.get(0).getImageType(), imagesVO));
			}
		}
		EductaionInstituteVehicleDetailsVO vo = eductaionInstituteVehicleDetailsMapper
				.convertEntity(eibtVehicleDetails.get());
		vo.setImageDetailsList(displayEnclosures);
		return vo;

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
	public Optional<UserVO> eibtSignUp(UserVO uservo) {
		UserDTO userDTO = new UserDTO();
		userDTO = userMapper.convertVO(uservo);
		if (StringUtils.isBlank(userDTO.getInstitutionName())) {
			logger.error("Please privide name of the School/College");
			throw new BadRequestException("Please privide name of the School/College");
		}
		if (StringUtils.isBlank(userDTO.getRepresentativeName())) {
			logger.error("Please privide name of the Representative");
			throw new BadRequestException("Please privide name of the Representative");
		}
		if (userDTO.getDesignation() == null) {
			logger.error("Please privide dignation");
			throw new BadRequestException("Please privide dignation");
		}
		if (StringUtils.isBlank(userDTO.getDoorNo())) {
			logger.error("Please privide house number");
			throw new BadRequestException("Please privide house number");
		}
		if (userDTO.getVillage() == null) {
			logger.error("Please select village");
			throw new BadRequestException("Please select village");
		}
		if (userDTO.getDistrict() == null) {
			logger.error("Please select district");
			throw new BadRequestException("Please select district");
		}
		if (userDTO.getMandal() == null) {
			logger.error("Please select mandal");
			throw new BadRequestException("Please select mandal");
		}
		if (StringUtils.isBlank(userDTO.getMobile())) {
			logger.error("Please privide mobile number");
			throw new BadRequestException("Please privide mobile number");
		}
		if (StringUtils.isBlank(userDTO.getUserName())) {
			logger.error("Please privide user name");
			throw new BadRequestException("Please privide user name");
		}
		userDTO.setUserId(userDTO.getUserName().toUpperCase());
		Optional<UserDTO> optionalUser = userDao.findByUserId(userDTO.getUserName().toUpperCase());
		if (optionalUser.isPresent()) {
			logger.error("User name exists. Please change the user name.");
			throw new BadRequestException("User name exists. Please change the user name.");
		}

		userDTO.setPasswordResetRequired(Boolean.TRUE);
		userDTO.setPassword(financierPassword);
		RolesDTO rolesDTO = new RolesDTO();
		rolesDTO.setName(StatusRegistration.EDUCATIONALINSTITUTE.getDescription());
		userDTO.setPrimaryRole(rolesDTO);
		userDTO.setAdditionalRoles(Collections.EMPTY_LIST);
		userDTO.setIsAccountNonLocked(Boolean.TRUE);
		userDTO.setUserLevel(1);
		userDTO.setStatus(Boolean.TRUE);
		userDTO.setUserStatus(UserStatusEnum.ACTIVE);
		userDao.save(userDTO);
		UserVO vo = userMapper.UserAndPassword(userDTO);
		return Optional.of(vo);
	}

	private double calculateAgeOfTheVehicle(LocalDate localDateTime, LocalDate entryDate) {

		Period age = Period.between(localDateTime, entryDate);
		String ageInString = String.valueOf(age.getYears()) + "." + String.valueOf(age.getMonths());
		Double ageInDouble = Double.valueOf(ageInString);

		return ageInDouble;

	}

	@Override
	public List<EductaionInstituteVehicleDetailsVO> viewAllApplications(JwtUser jwtUser) {

		List<EductaionInstituteVehicleDetailsVO> list = new ArrayList<>();
		List<EductaionInstituteVehicleDetailsDto> eibtVehicleDetails = eductaionInstituteVehicleDetailsDao
				.findByUserId(jwtUser.getId());
		if (eibtVehicleDetails.isEmpty()) {
			logger.error("No records found for this user:  [{}]", jwtUser.getId());
			throw new BadRequestException("No records found for this user:  " + jwtUser.getId());
		}
		for (EductaionInstituteVehicleDetailsDto dto : eibtVehicleDetails) {
			if (dto.isTowDone()) {
				continue;
			}
			EductaionInstituteVehicleDetailsVO vo = new EductaionInstituteVehicleDetailsVO();
			List<EnclosureRejectedVO> displayEnclosures = new ArrayList<>();
			if (dto.getEnclosures() != null && !dto.getEnclosures().isEmpty()) {
				for (KeyValue<String, List<ImageEnclosureDTO>> enclosureKeyValue : dto.getEnclosures()) {
					List<ImageVO> imagesVO = enclosureImageMapper.convertNewEntity(enclosureKeyValue.getValue());
					displayEnclosures.add(new EnclosureRejectedVO(imagesVO.get(0).getImageType(), imagesVO));
				}
			}
			vo = eductaionInstituteVehicleDetailsMapper.convertEntity(dto);
			vo.setImageDetailsList(displayEnclosures);
			list.add(vo);
		}
		return list;

	}

	@Override
	public Long vehiclesCount(JwtUser jwtUser) {

		List<EductaionInstituteVehicleDetailsDto> list = new ArrayList<>();
		List<EductaionInstituteVehicleDetailsDto> eibtVehicleDetails = eductaionInstituteVehicleDetailsDao
				.findByUserId(jwtUser.getId());
		if (eibtVehicleDetails.isEmpty()) {
			return 0l;
		}
		for (EductaionInstituteVehicleDetailsDto dto : eibtVehicleDetails) {
			if (dto.isTowDone()) {
				continue;
			}
			list.add(dto);
		}
		return Integer.toUnsignedLong(list.size());

	}

	@Override
	public HttpServletResponse sampleExcel(HttpServletResponse response) {

		String[] columns = { "SLNO", "Student Name", "Father Name", "DOB", "Gender", "Blood Group", "MobileNo",
				"Emergency Contact Number", "Email", "Class" };

		String name = "EMS_Details";
		String fileName = name + ".xls";
		// String sheetName = "EMSReport";

		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
		response.setHeader("Pragma", "public");
		response.setHeader("Cache-Control", "no-store");
		response.addHeader("Cache-Control", "max-age=0");
		Workbook workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet("studentDetails");

		Font headerFont = workbook.createFont();
		// headerFont.setBold(true);
		headerFont.setFontHeightInPoints((short) 14);
		headerFont.setColor(IndexedColors.RED.getIndex());

		CellStyle headerCellStyle = workbook.createCellStyle();
		headerCellStyle.setFont(headerFont);

		// Create a Row
		Row headerRow = sheet.createRow(0);

		for (int i = 0; i < columns.length; i++) {
			Cell cell = headerRow.createCell(i);
			cell.setCellValue(columns[i]);
			cell.setCellStyle(headerCellStyle);
		}

		// Create Other rows and cells with contacts data
		int rowNum = 1;

		/*
		 * for (Contact contact : contacts) { Row row = sheet.createRow(rowNum++);
		 * row.createCell(0).setCellValue(contact.firstName);
		 * row.createCell(1).setCellValue(contact.lastName);
		 * row.createCell(2).setCellValue(contact.email);
		 * row.createCell(3).setCellValue(contact.dateOfBirth); }
		 */

		// Resize all columns to fit the content size
		/*
		 * for (int i = 0; i < columns.length; i++) { sheet.autoSizeColumn(i); }
		 */

		// Write the output to a file
		FileOutputStream fileOut;
		try {
			ServletOutputStream outputStream = null;
			outputStream = response.getOutputStream();
			workbook.write(outputStream);
			outputStream.flush();
			outputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage());
			e.printStackTrace();
		}

		return response;

	}
/**
 *  get EIB user 
 */
	@Override
	public Optional<EductaionInstituteVehicleDetailsVO> getEibUserDataByPrNo(String prNo) {
		Optional<EductaionInstituteVehicleDetailsDto> dto = eductaionInstituteVehicleDetailsDao.findByprNoIgnoreCase(prNo);
		if(!dto.isPresent()) {
			logger.error("No EIB user found for prNo:- [{}] ",prNo);
			throw new BadRequestException("No EIB user Id found for prNo:- "+prNo);
		}
		String substring = "********" ;
		if( ! StringUtils.isBlank(dto.get().getAadharNo()) )
			substring=substring+dto.get().getAadharNo().substring(8, 12);
		Optional<EductaionInstituteVehicleDetailsVO> vo =eductaionInstituteVehicleDetailsMapper.convertEntity(dto);
		vo.get().setAadharNo(substring);
		return vo;
	}

}
