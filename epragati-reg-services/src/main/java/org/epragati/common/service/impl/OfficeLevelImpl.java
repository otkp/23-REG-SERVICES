package org.epragati.common.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.epragati.common.service.CommonService;
import org.epragati.constants.CovCategory;
import org.epragati.constants.ExceptionDescEnum;
import org.epragati.constants.OwnerTypeEnum;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dto.DuplicatePrNumbers;
import org.epragati.master.dto.GeneratedPrDetailsDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.rta.vo.PrGenerationVO;
import org.epragati.sn.numberseries.dto.PRNumberSeriesConfigDTO;
import org.epragati.sn.numberseries.dto.PRPoolDTO;
import org.epragati.util.BidNumberType;
import org.epragati.util.NumberPoolStatus;
import org.epragati.util.RecordStatus;
import org.epragati.util.payment.ModuleEnum;
import org.epragati.util.payment.ServiceEnum;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

@Service
@Qualifier("districtLevelImpl")
public class OfficeLevelImpl extends CommonService{


	@Override
	public String geneatePrNo(PrGenerationVO prGenVO){

		Optional<StagingRegistrationDetailsDTO> stagingRegDetailsoptional = null;
		Optional<OfficeDTO> officeDetails = Optional.empty();
		Optional<RegServiceDTO> regServiceDTO = null;
		RegServiceDTO regServiceDetails = null;
		Integer districtId = null;
		StagingRegistrationDetailsDTO stagingRegDetails = null;
		String vehicleType = null;
		boolean fromCitizen = false;
		if (prGenVO.getCitizen() != null && ModuleEnum.CITIZEN.equals(prGenVO.getCitizen())) {

			if (!StringUtils.isEmpty(prGenVO.getApplicationNo())) {
				regServiceDTO = regServiceDAO.findByApplicationNo(prGenVO.getApplicationNo());
			}

			if (!regServiceDTO.isPresent()) {
				throw new BadRequestException("Application not found: " + prGenVO.getApplicationNo());
			}

			regServiceDetails = regServiceDTO.get();

			if (regServiceDetails.getServiceIds().stream()
					.anyMatch(id -> id.equals(ServiceEnum.CHANGEOFADDRESS.getId()))) {
				districtId = regServiceDetails.getPresentAdderss().getDistrict().getDistricCode();
			} else {
				districtId = regServiceDetails.getRegistrationDetails().getApplicantDetails().getPresentAddress()
						.getDistrict().getDistricCode();
			}
			/*if (regServiceDetails.getAlterationDetails() == null
					|| regServiceDetails.getAlterationDetails().getVehicleTypeTo() == null) {
				logger.info("vehicle details not found in alteration :[{}] ", regServiceDetails.getApplicationNo());
				throw new BadRequestException(
						"vehicle details not found in alteration : " + regServiceDetails.getApplicationNo());
			}*/
			if (regServiceDetails.getAlterationDetails() != null && null != regServiceDetails.getAlterationDetails().getVehicleTypeTo()) {
				vehicleType = regServiceDetails.getAlterationDetails().getVehicleTypeTo();
			} else {
				vehicleType = regServiceDetails.getRegistrationDetails().getVehicleType();
			}
			fromCitizen = Boolean.TRUE;
			officeDetails = officeDAO.findByOfficeCode(regServiceDetails.getOfficeCode());
		} else {
			if (!StringUtils.isEmpty(prGenVO.getApplicationNo())) {
				stagingRegDetailsoptional = stagingRegistrationDetailsDAO.findByApplicationNo(prGenVO.getApplicationNo());
			}

			if (!stagingRegDetailsoptional.isPresent()) {
				throw new BadRequestException("Application not found: " + prGenVO.getApplicationNo());
			}

			stagingRegDetails = stagingRegDetailsoptional.get();
			if (stagingRegDetails.getApplicantDetails() == null
					|| stagingRegDetails.getApplicantDetails().getPresentAddress() == null
					|| stagingRegDetails.getApplicantDetails().getPresentAddress().getDistrict() == null
					|| stagingRegDetails.getApplicantDetails().getPresentAddress().getDistrict()
					.getDistricCode() == null) {
				logger.error("District details not found in present address: [{}]",
						stagingRegDetails.getApplicationNo());
				throw new BadRequestException(
						"District details not found in present address: " + stagingRegDetails.getApplicationNo());
			}


			districtId = stagingRegDetails.getApplicantDetails().getPresentAddress().getDistrict().getDistricCode();

			officeDetails = officeDAO.findByOfficeCode(stagingRegDetails.getOfficeDetails().getOfficeCode());
			vehicleType = stagingRegDetails.getVehicleType();
		}

		if (districtId == null) {
			throw new BadRequestException("District Id is invalid for: "
					+ stagingRegDetails.getApplicantDetails().getPresentAddress().getDistrict().getDistrictName());
		}

		if (!officeDetails.isPresent()) {
			throw new BadRequestException(
					"office details not found for: " + stagingRegDetails.getOfficeDetails().getOfficeCode());
		}

		final String distIdString= districtId.toString();

		synchronized (distIdString.intern()) {

			if (prGenVO.isNumberlocked()) {
				lockSpNumber(prGenVO.getSelectedNo(), prGenVO.getPrSeries(), officeDetails, vehicleType);
			} else {

				if (!fromCitizen && StringUtils.isNotBlank(stagingRegDetails.getPrNo())) {
					List<GeneratedPrDetailsDTO> oldPrRecords = generatedPrDetailsDAO
							.findByPrNo(stagingRegDetails.getPrNo());
					if (!oldPrRecords.isEmpty()) {
						if (oldPrRecords.size() > 1) {
							logger.error("More then on same pr found. PR number is: [{}]", stagingRegDetails.getPrNo());
							throw new BadRequestException(
									"More then on same pr found. PR number is: " + stagingRegDetails.getPrNo());
						} else {

							logger.error("Same pr found. PR number is: [{}]", stagingRegDetails.getPrNo());
							throw new BadRequestException(
									"Same pr found. PR number is: " + stagingRegDetails.getPrNo());

						}
					}
					saveGeneratedPrDetails(stagingRegDetails, stagingRegDetails.getPrNo(), "SP" , Boolean.FALSE , regServiceDetails);
					return stagingRegDetails.getPrNo();
				} else {
					return normalPRGeneration(vehicleType,officeDetails,stagingRegDetails,regServiceDetails,fromCitizen);
				}
			}
		}
		return null;
	}





	private String lockSpNumber(Integer selectedNo, String prSeries, Optional<OfficeDTO> officeDetails, String vehicleType) {

		Optional<PRPoolDTO> numberPoolOptional = numbersPoolDAO
				.findByOfficeCodeAndRegTypeAndPoolStatusNotInAndPrNumberAndPrSeries(officeDetails.get().getOfficeCode(),
						CovCategory.getCovCategory(vehicleType), Arrays.asList(NumberPoolStatus.ASSIGNED), selectedNo,
						prSeries);

		if (numberPoolOptional.isPresent()) {
			String formatNumber = appendZero(numberPoolOptional.get().getPrNumber(), 4);
			String	prNo = officeDetails.get().getOfficeNumberSeries() + numberPoolOptional.get().getPrSeries() + formatNumber;
			List<GeneratedPrDetailsDTO> oldPrRecords = generatedPrDetailsDAO.findByPrNo(prNo);
			if (!oldPrRecords.isEmpty()) {
				logger.info("Same pr found. PR number is: " + prNo);
				DuplicatePrNumbers duplicateNumbers = new DuplicatePrNumbers();
				duplicateNumbers.setPr(oldPrRecords.stream().findFirst().get().getPrNo());
				duplicateNumbers.setPrCount(oldPrRecords.size());
				duplicateNumbers.setSource(BidNumberType.N.getCode());
				duplicatePrNumberDAO.save(duplicateNumbers);
				throw new BadRequestException("Please select another new Number. The number " + prNo+" already assigned.");
			}
			numberPoolOptional.get().setPoolStatus(NumberPoolStatus.LOCKED);
			numbersPoolDAO.save(numberPoolOptional.get());
			return NumberPoolStatus.LOCKED.getDescription();
		} else {
			throw new BadRequestException("Please select new Number..");
		}
	}


	private String normalPRGeneration(String vehicleType,
			Optional<OfficeDTO> officeDetails,StagingRegistrationDetailsDTO stagingRegDetails,RegServiceDTO regServiceDetails,boolean fromCitizen) {
		if(!fromCitizen && stagingRegDetails.getOwnerType().equals(OwnerTypeEnum.POLICE)) {
			vehicleType = CovCategory.P.getCode();
		}else if(!fromCitizen && stagingRegDetails.getOwnerType().equals(OwnerTypeEnum.Stu)) {
			vehicleType = CovCategory.Z.getCode();
			Optional<OfficeDTO> officeOptional = officeDAO.findByOfficeCode(officeDetails.get().getOfficeCode());
			if(officeOptional.isPresent()) {
				officeDetails = officeOptional;
			}else {
				logger.info("No matched records found for office :[{}] ",
						officeDetails.get().getOfficeCode());
				throw new BadRequestException(
						"No matched records found for office : " + officeDetails.get().getOfficeCode());
			}
		}

		CovCategory covCategory = CovCategory.getCovCategory(vehicleType);

		boolean isRequiredReopenStatus = requiredToSkipReopenStatusValidation(officeDetails.get(), (!fromCitizen?stagingRegDetails.getOwnerType():regServiceDetails.getRegistrationDetails().getOwnerType()));

		String officeCode=null;
		if(!fromCitizen && stagingRegDetails.getOwnerType().equals(OwnerTypeEnum.Stu)) {
			officeCode=officeDetails.get().getReportingoffice();
		}else {
			officeCode=officeDetails.get().getOfficeCode();

		}
		List<PRNumberSeriesConfigDTO> prConfignfigList= snPrSeriesDAO.findByOfficeCodeAndRegTypeAndSeriesStatusIn(officeCode, covCategory,Arrays.asList(RecordStatus.ACTIVE,RecordStatus.ACTIVE_INCOMPLET));

		prConfignfigList.sort((s1,s2)->s1.getSeriesStatus().getCode().compareTo(s2.getSeriesStatus().getCode()));

		for(PRNumberSeriesConfigDTO p:prConfignfigList) {
			Pageable pageable = new PageRequest(0, 200, new Sort(new Order(Direction.ASC, "prNumber")));
			List<PRPoolDTO> numbersPoolList  =numbersPoolDAO.findByOfficeCodeAndRegTypeAndPoolStatusInAndPrSeriesIdAndNumberTypeNot(officeCode,
					covCategory, getPoolStatus(isRequiredReopenStatus),p.getPrSeriesId(),CovCategory.P.getCode(),pageable);
			if (numbersPoolList.isEmpty()) {
				logger.info("No matched records found for office :[{}] ",
						officeDetails.get().getOfficeCode());
				throw new BadRequestException(
						"No matched records found for office : " + officeDetails.get().getOfficeCode());
			}
			boolean primeNoStatus = false;


			for (PRPoolDTO numbersPool : numbersPoolList) {
				primeNoStatus = isPrimeNumber(numbersPool.getPrNumber());
				if (primeNoStatus) {
					continue;
				} else {

					// numbersPool.setIsLastAssignedNumber(Boolean.TRUE);
					//String prNo = generateNextNumber(officeDetails, numbersPool, stagingRegDetails,regServiceDetails,fromCitizen);
					String prNo = null;
					actionsDetailsHelper.updateActionsDetails(numbersPool, ExceptionDescEnum.ACTIONBY.getDesciption());
					if (!fromCitizen && stagingRegDetails.getOwnerType().equals(OwnerTypeEnum.POLICE)) {
						if (officeDetails.get().getPoliceNumberSeries() == null) {
							logger.error("police field not found for application no:[{}] ", stagingRegDetails.getApplicationNo());
							throw new BadRequestException(
									"police field not found for application no: " + stagingRegDetails.getApplicationNo());
						}
						prNo = officeDetails.get().getPoliceNumberSeries() + numbersPool.getPrSeries() + appendZero(numbersPool.getPrNumber(), 4);

					} else {
						prNo = officeDetails.get().getOfficeNumberSeries() + numbersPool.getPrSeries() + appendZero(numbersPool.getPrNumber(), 4);
					}
					List<GeneratedPrDetailsDTO> oldPrRecords = generatedPrDetailsDAO.findByPrNo(prNo);
					if (!oldPrRecords.isEmpty()) {
						if (oldPrRecords.size() > 1) {
							logger.info("More then on same pr found. PR number is [{}]", prNo);
							DuplicatePrNumbers duplicateNumbers = new DuplicatePrNumbers();
							duplicateNumbers.setPr(oldPrRecords.stream().findFirst().get().getPrNo());
							duplicateNumbers.setPrCount(oldPrRecords.size());
							duplicateNumbers.setSource(BidNumberType.N.getCode());
							duplicatePrNumberDAO.save(duplicateNumbers);
							
							//throw new BadRequestException("More then on same pr found. PR number is: " + prNo);
						} else {

							logger.info("Same pr found. PR number is: " + prNo);
							DuplicatePrNumbers duplicateNumbers = new DuplicatePrNumbers();
							duplicateNumbers.setPr(oldPrRecords.stream().findFirst().get().getPrNo());
							duplicateNumbers.setPrCount(oldPrRecords.size());
							duplicateNumbers.setSource(BidNumberType.N.getCode());
							duplicatePrNumberDAO.save(duplicateNumbers);
							
						}
						continue;
					}
					Optional<StagingRegistrationDetailsDTO> stagingDetails = stagingRegistrationDetailsDAO.findByPrNo(prNo);
					if(stagingDetails.isPresent()) {
						continue;
					}
					//TODO need to check in reg document also
					/*Optional<RegistrationDetailsDTO> regDetails = registrationDetailDAO.findByPrNo(prNo);
					if(regDetails.isPresent()) {
						continue;
					}*/
					if(!NumberPoolStatus.REOPEN.equals(numbersPool.getPoolStatus())) {
						Optional<PRNumberSeriesConfigDTO> pRNumberSeries = snPrSeriesDAO.findByOfficeCodeAndRegTypeAndPrSeries(
								numbersPool.getOfficeCode(), numbersPool.getRegType(), numbersPool.getPrSeries());
						if (pRNumberSeries.isPresent()) {
							pRNumberSeries.get().setCurrentNumber(numbersPool.getPrNumber());
							snPrSeriesDAO.save(pRNumberSeries.get());
						}
					}

					if(fromCitizen) {
						if(null != regServiceDetails.getAlterationDetails())
							regServiceDetails.getAlterationDetails().setPrNo(prNo);
						regServiceDAO.save(regServiceDetails);

					}else {
						//stagingRegDetails.setPrNo(prNo);
						//logMovingService.moveStagingToLog(stagingRegDetails.getApplicationNo());
						//stagingRegistrationDetailsDAO.save(stagingRegDetails);
					}
					numbersPool.setModifiedDate(LocalDateTime.now());
					numbersPool.setModifiedBy(ExceptionDescEnum.ACTIONBY.getDesciption());
					numbersPool.setPoolStatus(NumberPoolStatus.ASSIGNED);
					numbersPoolDAO.save(numbersPool);
					logger.info("PR generated.  pr no is: " + prNo);
					saveGeneratedPrDetails(stagingRegDetails, prNo, ExceptionDescEnum.ACTIONBY.getDesciption() , fromCitizen , regServiceDetails);
					return prNo;


					//return prNo;
				}
			}

		}
		return null;
	}

}
