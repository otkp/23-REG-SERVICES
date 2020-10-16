package org.epragati.common.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.epragati.common.dao.PropertiesDAO;
import org.epragati.common.dto.PropertiesDTO;
import org.epragati.common.service.CommonService;
import org.epragati.constants.CovCategory;
import org.epragati.constants.ExceptionDescEnum;
import org.epragati.constants.OwnerTypeEnum;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dto.DuplicatePrNumbers;
import org.epragati.master.dto.GeneratedPrDetailsDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.registration.service.ServiceProviderFactory;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.rta.vo.PrGenerationVO;
import org.epragati.sn.numberseries.dto.PRNumberSeriesConfigDTO;
import org.epragati.sn.numberseries.dto.PRPoolDTO;
import org.epragati.sn.service.NumberSeriesService;
import org.epragati.util.BidNumberType;
import org.epragati.util.NumberPoolStatus;
import org.epragati.util.NumberPoolStatus.NumberConfigLevel;
import org.epragati.util.payment.ModuleEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

@Service
@Qualifier("stateLevelImpl")
public class StateLevelImpl extends CommonService {
	
	//private static final Logger logger = LoggerFactory.getLogger(StateLevelImpl.class);


	@Autowired
	private ServiceProviderFactory serviceProviderFactory;

	@Autowired
	private PropertiesDAO propertiesDAO;
	
	@Autowired 
	private RegistrationDetailDAO registrationDetailDAO;

	@Override
	public String geneatePrNo(PrGenerationVO prGenVO) {

		Optional<OfficeDTO> officeDetails = officeDAO.findByOfficeCode(NumberConfigLevel.STATE.getLabel());
		if (!officeDetails.isPresent()) {
			throw new BadRequestException("office details not found for: " + NumberConfigLevel.STATE.getLabel());
		}

		String vehicleType = null;

		if (ModuleEnum.CITIZEN.equals(prGenVO.getCitizen())) {
			logger.info("citizen pr number generation service initiated");
			Optional<RegServiceDTO> regServiceOpt = regServiceDAO.findByApplicationNo(prGenVO.getApplicationNo());
			if (!regServiceOpt.isPresent()) {
				throw new BadRequestException("Application not found: " + prGenVO.getApplicationNo());
			}

			RegServiceDTO regServiceDTO = regServiceOpt.get();
			if (OwnerTypeEnum.POLICE.equals(regServiceDTO.getRegistrationDetails().getOwnerType())) {
				vehicleType = CovCategory.P.getCode();
			} else if (OwnerTypeEnum.Stu.equals(regServiceDTO.getRegistrationDetails().getOwnerType())) {
				vehicleType = CovCategory.Z.getCode();
			} else if (regServiceDTO.getAlterationDetails() != null
					&& null != regServiceDTO.getAlterationDetails().getVehicleTypeTo()) {
				vehicleType = regServiceDTO.getAlterationDetails().getVehicleTypeTo();
			} else {
				vehicleType = regServiceDTO.getRegistrationDetails().getVehicleType();
			}

			return returnPrnumber(prGenVO, officeDetails.get(), vehicleType, null, regServiceDTO);
		} else {
			logger.info("normal pr number generation service initiated");
			
			Optional<RegistrationDetailsDTO> regDTO = registrationDetailDAO.findByApplicationNo(prGenVO.getApplicationNo());
			if(regDTO.isPresent() && regDTO.get().getPrNo()!=null ) {
				throw new BadRequestException("pr number is already generated : " + prGenVO.getApplicationNo());
			}
			
			Optional<StagingRegistrationDetailsDTO> stagingRegDetailsoptional = stagingRegistrationDetailsDAO
					.findByApplicationNo(prGenVO.getApplicationNo());

			if (!stagingRegDetailsoptional.isPresent()) {
				throw new BadRequestException("Application not found: " + prGenVO.getApplicationNo());
			}

			StagingRegistrationDetailsDTO stagingRegDetails = stagingRegDetailsoptional.get();

			if (stagingRegDetails.getOwnerType().equals(OwnerTypeEnum.POLICE)) {
				vehicleType = CovCategory.P.getCode();
			} else if (stagingRegDetails.getOwnerType().equals(OwnerTypeEnum.Stu)) {
				vehicleType = CovCategory.Z.getCode();
			} else {
				vehicleType = stagingRegDetails.getVehicleType();
			}
			return returnPrnumber(prGenVO, officeDetails.get(), vehicleType, stagingRegDetailsoptional.get(), null);
		}
	}

	private synchronized String returnPrnumber(PrGenerationVO prGenVO, OfficeDTO officeDTO, String vehicleType,
			StagingRegistrationDetailsDTO stagingRegDetails, RegServiceDTO regServiceDTO) {

		synchronized (vehicleType.intern()) {
			if (prGenVO.isNumberlocked()) {
				return lockSpNumber(prGenVO.getSelectedNo(), prGenVO.getPrSeries(), officeDTO, vehicleType);

			}
			logger.info("vehicle type synchronized block initiated");
			if (null != stagingRegDetails && StringUtils.isNotBlank(stagingRegDetails.getPrNo())) {
				List<GeneratedPrDetailsDTO> oldPrRecords = generatedPrDetailsDAO
						.findByPrNo(stagingRegDetails.getPrNo());
				if (!oldPrRecords.isEmpty()) {
					if (oldPrRecords.size() > 1) {
						logger.error("More then on same pr found. PR number is: [{}]", stagingRegDetails.getPrNo());
						throw new BadRequestException(
								"More then on same pr found. PR number is: " + stagingRegDetails.getPrNo());
					} else {

						logger.error("Same pr found. PR number is: [{}]", stagingRegDetails.getPrNo());
						throw new BadRequestException("Same pr found. PR number is: " + stagingRegDetails.getPrNo());

					}
				}
				saveGeneratedPrDetails(stagingRegDetails, stagingRegDetails.getPrNo(), "SP", Boolean.FALSE,
						regServiceDTO);
				return stagingRegDetails.getPrNo();
			}
			return normalPRGeneration(vehicleType, officeDTO, stagingRegDetails, regServiceDTO,
					ModuleEnum.CITIZEN.equals(prGenVO.getCitizen()));
		}

	}

	private String lockSpNumber(Integer selectedNo, String prSeries, OfficeDTO officeDTO, String vehicleType) {

		Optional<PRPoolDTO> numberPoolOptional = numbersPoolDAO
				.findByOfficeCodeAndRegTypeAndPoolStatusNotInAndPrNumberAndPrSeries(officeDTO.getOfficeCode(),
						CovCategory.getCovCategory(vehicleType), Arrays.asList(NumberPoolStatus.ASSIGNED), selectedNo,
						prSeries);

		if (numberPoolOptional.isPresent()) {
			String formatNumber = appendZero(numberPoolOptional.get().getPrNumber(), 4);
			String prNo = officeDTO.getOfficeNumberSeries() + numberPoolOptional.get().getPrSeries() + formatNumber;
			List<GeneratedPrDetailsDTO> oldPrRecords = generatedPrDetailsDAO.findByPrNo(prNo);
			if (!oldPrRecords.isEmpty()) {
				logger.info("Same pr found. PR number is: " + prNo);
				DuplicatePrNumbers duplicateNumbers = new DuplicatePrNumbers();
				duplicateNumbers.setPr(oldPrRecords.stream().findFirst().get().getPrNo());
				duplicateNumbers.setPrCount(oldPrRecords.size());
				duplicateNumbers.setSource(BidNumberType.N.getCode());
				duplicatePrNumberDAO.save(duplicateNumbers);
				throw new BadRequestException("Please select another new Number..: " + prNo);
			}
			numberPoolOptional.get().setPoolStatus(NumberPoolStatus.LOCKED);
			numbersPoolDAO.save(numberPoolOptional.get());
			return prNo;
		} else {
			throw new BadRequestException("Please select new Number..");
		}
	}

	private String normalPRGeneration(String vehicleType, OfficeDTO officeDTO,
			StagingRegistrationDetailsDTO stagingRegDetails, RegServiceDTO regServiceDetails, boolean fromCitizen) {
		CovCategory covCategory = CovCategory.getCovCategory(vehicleType);

		// boolean isRequiredReopenStatus =
		// requiredToSkipReopenStatusValidation(officeDTO,
		// (!fromCitizen?stagingRegDetails.getOwnerType():regServiceDetails.getRegistrationDetails().getOwnerType()));

		String officeCode = null;
		/*
		 * if(!fromCitizen &&
		 * stagingRegDetails.getOwnerType().equals(OwnerTypeEnum.Stu)) {
		 * officeCode=officeDTO.getReportingoffice(); }else { //Commented to remove
		 * issue( PA-1225 ) officeCode=officeDTO.getOfficeCode();}
		 */
		officeCode = officeDTO.getOfficeCode();
		logger.info("normalPRGeneration method initiated");
		NumberSeriesService numberSeriesService = serviceProviderFactory.getNumberSeriesServiceInstent();
		List<PRNumberSeriesConfigDTO> prConfignfigList = numberSeriesService.getPRNumberSeriesConfigData(officeCode,
				covCategory);
		if (prConfignfigList.isEmpty()) {
			throw new BadRequestException("PR series not found for vehicle type : " + covCategory);
		}
		return this.validateOrGeneratePool(stagingRegDetails, regServiceDetails, fromCitizen, covCategory, officeDTO,
				prConfignfigList);
	}

	private String validateOrGeneratePool(StagingRegistrationDetailsDTO stagingRegDetails,
			RegServiceDTO regServiceDetails, boolean fromCitizen, CovCategory covCategory, OfficeDTO officeDTO,
			List<PRNumberSeriesConfigDTO> prConfignfigList) {
		int iteration = 0;
		logger.info("validateOrGeneratePool method initiated");
		Optional<PropertiesDTO> propertiesDTO = propertiesDAO.findByModule("SP");
		for (PRNumberSeriesConfigDTO prSeriesDTO : prConfignfigList) {

			List<PRPoolDTO> numbersPoolList;
			if (propertiesDTO.isPresent()&&propertiesDTO.get().getStatus()) {
				numbersPoolList = numbersPoolDAO.findByOfficeCodeAndRegTypeAndPoolStatusInAndPrSeriesIdAndNumberTypeNot(
						officeDTO.getOfficeCode(), covCategory, getPoolStatus(true), prSeriesDTO.getPrSeriesId(),
						CovCategory.P.getCode());
			}

			else {
				Pageable pageable = new PageRequest(0, 300, new Sort(new Order(Direction.ASC, "prNumber")));
				numbersPoolList = numbersPoolDAO.findByOfficeCodeAndRegTypeAndPoolStatusInAndPrSeriesIdAndNumberTypeNot(
						officeDTO.getOfficeCode(), covCategory, getPoolStatus(true), prSeriesDTO.getPrSeriesId(),
						CovCategory.P.getCode(), pageable);
			}

			if (numbersPoolList.isEmpty()) {
				iteration++;
				if (prConfignfigList.size() == iteration) {
					try {
						NumberSeriesService numberSeriesService = serviceProviderFactory
								.getNumberSeriesServiceInstent();
						numberSeriesService.generateNumbersIntoPool(covCategory, officeDTO);
					} catch (BadRequestException bre) {
						logger.debug("{}", bre.getMessage());
						logger.error("Exception is [{}]", bre.getMessage());
					} catch (Exception e) {
						logger.debug("{}", e);
						logger.error("Exception is [{}]", e);
					}
				return	this.normalPRGeneration(stagingRegDetails.getVehicleType(), officeDTO, stagingRegDetails,
							regServiceDetails, fromCitizen);
				}
				continue;
			}
			boolean primeNoStatus = false;

			// Sort On pr number.
			// sort based on created date
			// numbersPoolList.sort((p1, p2) ->
			// p1.getCreatedDate().compareTo(p2.getCreatedDate()));
			List<PRPoolDTO> pRPoolDTOListAssigned = new ArrayList<>();
			for (PRPoolDTO numbersPool : numbersPoolList) {
				primeNoStatus = isPrimeNumber(numbersPool.getPrNumber());
				if (primeNoStatus) {
					continue;
				} else {

					String prNo;
					if (propertiesDTO.get().getStatus()) {
						Random ran = new Random();
						Integer ranNumber = 0;
						if (numbersPoolList.size() > 1) {
							ranNumber = ran.nextInt(numbersPoolList.size() - 1);
						}

						logger.info(" Starting prNo :[{}] Ending prNo : [{}] random Number [{}]",
								numbersPoolList.stream().findFirst().get().getPrNo(),
								numbersPoolList.get(numbersPoolList.size() - 1).getPrNo(),
								numbersPoolList.get(ranNumber));
						PRPoolDTO prPoolDTO = numbersPoolList.get(ranNumber);
						prNo = prPoolDTO.getPrNo();
						numbersPool = prPoolDTO;
					} else {
						prNo = numbersPool.getPrNo();
					}

					List<GeneratedPrDetailsDTO> oldPrRecords = generatedPrDetailsDAO.findByPrNo(prNo);

					if (!oldPrRecords.isEmpty()) {
						logger.info("Same pr found. PR number is: " + prNo);
						DuplicatePrNumbers duplicateNumbers = new DuplicatePrNumbers();
						duplicateNumbers.setPr(oldPrRecords.stream().findFirst().get().getPrNo());
						duplicateNumbers.setPrCount(oldPrRecords.size());
						duplicateNumbers.setSource(BidNumberType.N.getCode());
						duplicatePrNumberDAO.save(duplicateNumbers);

						numbersPool.setPoolStatus(NumberPoolStatus.ASSIGNED);
						numbersPool.setSource("Checked from generated_pr_Details collection");
						pRPoolDTOListAssigned.add(numbersPool);
						continue;
					}
					Optional<StagingRegistrationDetailsDTO> stagingDetails = stagingRegistrationDetailsDAO
							.findByPrNo(prNo);
					if (stagingDetails.isPresent()) {
						continue;
					}
					if (propertiesDTO.isPresent()&&propertiesDTO.get().getStatus()) {
						prSeriesDTO.setCurrentNumber(numbersPool.getPrNumber());
						snPrSeriesDAO.save(prSeriesDTO);
					}else {
					if (!NumberPoolStatus.REOPEN.equals(numbersPool.getPoolStatus())) {
						prSeriesDTO.setCurrentNumber(numbersPool.getPrNumber());
						snPrSeriesDAO.save(prSeriesDTO);
					}}


					if (fromCitizen) {
						if (null != regServiceDetails.getAlterationDetails())
							regServiceDetails.getAlterationDetails().setPrNo(prNo);
						regServiceDAO.save(regServiceDetails);

					} else {
						// stagingRegDetails.setPrNo(prNo);
						// logMovingService.moveStagingToLog(stagingRegDetails.getApplicationNo());
						// stagingRegistrationDetailsDAO.save(stagingRegDetails);
					}
					numbersPool.setModifiedDate(LocalDateTime.now());
					numbersPool.setModifiedBy(ExceptionDescEnum.ACTIONBY.getDesciption());
					numbersPool.setPoolStatus(NumberPoolStatus.ASSIGNED);
					numbersPoolDAO.save(numbersPool);
					logger.info("PR generated.  pr no is: " + prNo);
					saveGeneratedPrDetails(stagingRegDetails, prNo, ExceptionDescEnum.ACTIONBY.getDesciption(),
							fromCitizen, regServiceDetails);
					if (!pRPoolDTOListAssigned.isEmpty()) {
						numbersPoolDAO.save(pRPoolDTOListAssigned);
					}
					numbersPoolList.clear();
					logger.info("pr number genertion process executed successfully");
					return prNo;
				}
			}
		}
		throw new BadRequestException(
				"Open numbers are not found for vehicleType : " + stagingRegDetails.getVehicleType());
	}

}
