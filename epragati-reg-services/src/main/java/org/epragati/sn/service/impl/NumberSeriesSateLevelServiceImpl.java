package org.epragati.sn.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.common.dto.PropertiesDTO;
import org.epragati.constants.CovCategory;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.sn.dto.PRDistrictConfigDTO;
import org.epragati.sn.numberseries.dto.PRNumberSeriesConfigDTO;
import org.epragati.sn.numberseries.dto.PRPoolDTO;
import org.epragati.sn.numberseries.dto.RandomNumberLogDTO;
import org.epragati.sn.service.NumberSeriesService;
import org.epragati.sn.vo.LeftOverVO;
import org.epragati.sn.vo.NumberSeriesDetailsVO;
import org.epragati.sn.vo.SpecialFeeAndNumberDetailsVO;
import org.epragati.util.BidNumberType;
import org.epragati.util.NumberPoolStatus;
import org.epragati.util.RecordStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

@Service
@Qualifier("stateLevel")
public class NumberSeriesSateLevelServiceImpl extends NumberSeriesService {

	private char startChar = 'A';

	private final char lastChar = '[';

	@Override
	public SpecialFeeAndNumberDetailsVO getNumberSeriesByOfficeCode(String officeCode, CovCategory regType,
			String range, String seriesId) {

		if (StringUtils.isNoneBlank(range) && StringUtils.isNoneBlank(seriesId)) {
			return getNumberDetails(
					getPoolSizeNumbers(bidConfigMasterVO.getNumberGenerationType(), regType, range, seriesId));
		}
		return getNumberDetails(getPoolSizeNumbers(bidConfigMasterVO.getNumberGenerationType(), regType));

	}

	@Override
	public List<String> generateNumbersIntoPool() {
		Optional<OfficeDTO> officeOpt = officeDAO.findByOfficeCode(NumberPoolStatus.NumberConfigLevel.STATE.getLabel());
		if (!officeOpt.isPresent()) {
			throw new BadRequestException("Office Master Data not found for officeCode:"
					+ NumberPoolStatus.NumberConfigLevel.STATE.getLabel());
		}
		errors = new ArrayList<>();
		CovCategory.getNumbersRequiredCovs().stream()
				.forEach(cov -> this.generateNumbersIntoPool(cov, officeOpt.get()));
		return errors;
	}

	// TODO:need to implement
	@Override
	public void generateNumbersIntoPoolForOffice(String officeCode, String regType) {

	}

	@Override
	public List<NumberSeriesDetailsVO> getNumberRange(CovCategory regType,Boolean status) {//,Boolean status
		//previous method getPRNumberSeriesConfigData
		List<PRNumberSeriesConfigDTO> prConfignfigList = this
				.getCountDropDown(NumberPoolStatus.NumberConfigLevel.STATE.getLabel(), regType,status);
		List<NumberSeriesDetailsVO> numberSeriesDetailsList = new ArrayList<>();
		for (PRNumberSeriesConfigDTO pRNumberSeriesConfigDTO : prConfignfigList) {
			NumberSeriesDetailsVO numberSeriesDetailsVO = new NumberSeriesDetailsVO();
			mapper(pRNumberSeriesConfigDTO, numberSeriesDetailsVO);
			numberSeriesDetailsList.add(numberSeriesDetailsVO);
		}
		return numberSeriesDetailsList;
	}

	@Override
	public List<LeftOverVO> getrAvalibleLeftOverNumbers(String officeCode, CovCategory regType, String prSeries) {
		List<LeftOverVO> leftOverListResult = null;
		if (null != regType) {
			leftOverListResult = this.getLeftOverNumberSeriesByOfficeCode(bidConfigMasterVO.getNumberGenerationType(),
					regType, prSeries);
		}
		return leftOverListResult;
	}

	@Override
	public Set<String> getListOfLeftOverAvalibleSeries(String officeCode, CovCategory regType) {

		if (null != regType) {
			List<PRPoolDTO> leftOverList = numbersPoolDAO.findByOfficeCodeAndRegTypeAndPoolStatusAndNumberType(
					bidConfigMasterVO.getNumberGenerationType(), regType, NumberPoolStatus.LEFTOVER, BidNumberType.P);
			return leftOverList.stream().map(p -> p.getPrSeries()).collect(Collectors.toSet());
		}
		return Collections.emptySet();
	}

	private List<PRPoolDTO> getPoolSizeNumbers(String officeCode, CovCategory regType, String range, String seriesId) {
		PRNumberSeriesConfigDTO prNumberSeries = prSeriesDAO.findOne(seriesId);
		if (null == prNumberSeries) {
			throw new BadRequestException("Selected Series not found");
		}
		String[] rangeArray = range.split(rangeConcater);
		if (rangeArray.length != 2) {
			throw new BadRequestException("Invalid range values count");
		}
		int from = 0;
		int to = 0;
		try {
			from = Integer.parseInt(rangeArray[0]) - 1;
			to = Integer.parseInt(rangeArray[1]) + 1;
		} catch (Exception e) {
			throw new BadRequestException("Invalid range values");
		}
		Pageable pageable = new PageRequest(0, bidConfigMasterVO.getTotalNumberForWindow(),
				new Sort(new Order(Direction.ASC, "prNumber")));

		return numbersPoolDAO.findByPrSeriesIdAndPrNumberBetween(seriesId, from, to, pageable);

	}

	private void mapper(PRNumberSeriesConfigDTO prNumberSeries, NumberSeriesDetailsVO numberSeriesDetailsVO) {
		numberSeriesDetailsVO.setOfficeNumberSeries(prNumberSeries.getOfficeNumberSeries());
		numberSeriesDetailsVO.setPrSeries(prNumberSeries.getPrSeries());
		numberSeriesDetailsVO.setRegType(prNumberSeries.getRegType());
		numberSeriesDetailsVO.setSeriesStatus(prNumberSeries.getSeriesStatus());
		numberSeriesDetailsVO.setToDayStartNo(prNumberSeries.getToDayStartNo());
		if(prNumberSeries.getSeriesStatus().equals(RecordStatus.INACTIVE)) {
			numberSeriesDetailsVO.setToDayStartNo(1);
		}
		numberSeriesDetailsVO.setToDayEndNumber(prNumberSeries.getLastGeneratedPoolNumber());
		numberSeriesDetailsVO.setTotalNumberForWindow(bidConfigMasterVO.getTotalNumberForWindow());
		numberSeriesDetailsVO.setPrSeriesId(prNumberSeries.getPrSeriesId());

		int start = numberSeriesDetailsVO.getToDayStartNo();
		int endNumber = start + numberSeriesDetailsVO.getTotalNumberForWindow() - 1;
		List<String> numberRages = new ArrayList<>();
		do {
			String rang = start + rangeConcater + endNumber;
			start = endNumber + 1;
			endNumber += numberSeriesDetailsVO.getTotalNumberForWindow();
			if (endNumber > numberSeriesDetailsVO.getToDayEndNumber()) {
				endNumber = numberSeriesDetailsVO.getToDayEndNumber();
				numberRages.add(rang);
				rang = start + " - " + endNumber;
			}
			numberRages.add(rang);
		} while (endNumber != numberSeriesDetailsVO.getToDayEndNumber());
		numberSeriesDetailsVO.setNumberRages(numberRages);
		logger.debug("numberSeriesDetailsVO {}", numberSeriesDetailsVO);
	}

	// ----------------------private methods----------------
	@Override
	public void generateNumbersIntoPool(CovCategory covCategory, OfficeDTO office) {
		try {

			try {
				if (Arrays.asList(CovCategory.N, CovCategory.T).contains(covCategory)) {
					handleLokedAndPriviousNos(covCategory, office.getOfficeCode());
				}
			} catch (BadRequestException e) {
				logger.error("{}", e.getMessage());
				errors.add(e.getMessage());
			} catch (Exception e) {
				logger.error("{}", e);
				errors.add(e.getMessage());
			}

			PRNumberSeriesConfigDTO prNumberSeriesConfigDTO = null;

			List<PRNumberSeriesConfigDTO> prConfignfigList = this.getPRNumberSeriesConfigData(office.getOfficeCode(),
					covCategory);

			List<PRPoolDTO> prPoolList = new ArrayList<PRPoolDTO>();
			if (!prConfignfigList.isEmpty()) {
				prNumberSeriesConfigDTO = prConfignfigList.get((prConfignfigList.size() - 1));
				/** random number generation code starts **/
				Optional<PropertiesDTO> propertiesDTO = propertiesDAO.findByModule("SP");
				if (propertiesDTO.isPresent() && propertiesDTO.get().getStatus()) {
					for(int i=0;i<prConfignfigList.size();i++) {
						PRNumberSeriesConfigDTO prNumber = prConfignfigList.get((i));
						/**
						 * Capturing open numbers from pool
						 */
					List<PRPoolDTO> prPoolDTOList = prPoolDao
							.findByOfficeCodeAndRegTypeAndPoolStatusNotInAndPrSeriesAndNumberTypeNotAndPrNumberLessThan(
									office.getOfficeCode(), covCategory,
									Arrays.asList(NumberPoolStatus.ASSIGNED, NumberPoolStatus.LEFTOVER),
									prNumber.getPrSeries(), CovCategory.P.getCode(), prNumber.getCurrentNumber());

					if (CollectionUtils.isNotEmpty(prPoolDTOList)) {
						RandomNumberLogDTO randomNumberDto = new RandomNumberLogDTO();
						randomNumberDto.setPreviousSeries(prNumber);
						randomNumberDto.setCreatedDate(LocalDateTime.now());
						randomNumberDAO.save(randomNumberDto);
						prPoolDTOList.sort((s2, s1) -> s2.getPrNumber().compareTo(s1.getPrNumber()));
						prNumber.setCurrentNumber(prPoolDTOList.get(0).getPrNumber());
						prNumber.setToDayStartNo(prPoolDTOList.get(0).getPrNumber());
						prNumber.setIsRandomGenerated(Boolean.TRUE);
						prSeriesDAO.save(prNumber);

					}
					}
				}
				/** random number generation code ends **/

				prNumberSeriesConfigDTO = this.createPool(prPoolList, prNumberSeriesConfigDTO, office,
						getOpenNumberCount(prConfignfigList));

			} else {
				// Fresh case
				Optional<PRNumberSeriesConfigDTO> prSeriesOptional = this.configureNextPrSeries(office, covCategory);
				if (prSeriesOptional.isPresent()) {
					prNumberSeriesConfigDTO = prSeriesOptional.get();
					prNumberSeriesConfigDTO = this.createPool(prPoolList, prNumberSeriesConfigDTO, office, 0);
				}
			}

			// Part 1
			if (null != prNumberSeriesConfigDTO) {
				prNumberSeriesConfigDTO.setModifiedDate(LocalDateTime.now());
				prSeriesDAO.save(prNumberSeriesConfigDTO);
			}
			prPoolDao.save(prPoolList);
			// prSeries current status updations.
			inactionCompletedPrNumberSeries(prConfignfigList);

		} catch (Exception e) {
			logger.error("Exception while generate pool for vehicle type,{}, {}", covCategory, e);
		}
	}

	private PRNumberSeriesConfigDTO createPool(List<PRPoolDTO> prPoolList, PRNumberSeriesConfigDTO prSeriesDTO,
			OfficeDTO office, int prPoolOpenStatusSize) {

		Integer numbersToGenerate = 0;
		if (prPoolOpenStatusSize < maxNumberPoolSize) {
			numbersToGenerate = maxNumberPoolSize - prPoolOpenStatusSize;

			Integer endNumber = prSeriesDTO.getLastGeneratedPoolNumber() + numbersToGenerate;
			if (endNumber >= prSeriesDTO.getEndNumber()) {
				setStartNumber(prSeriesDTO);
				prPoolList.addAll(this.prepareNextSeriesWithPool(prSeriesDTO,
						prSeriesDTO.getLastGeneratedPoolNumber() + 1, prSeriesDTO.getEndNumber() - 1));

				prSeriesDTO = this.updateAndConfigureNextPrSeries(office, prSeriesDTO.getRegType(), prSeriesDTO).get();
				endNumber = endNumber - prSeriesDTO.getEndNumber() + 1;
				prPoolList.addAll(this.prepareNextSeriesWithPool(prSeriesDTO,
						prSeriesDTO.getLastGeneratedPoolNumber() + 1, endNumber));

			} else {
				prPoolList.addAll(this.prepareNextSeriesWithPool(prSeriesDTO,
						prSeriesDTO.getLastGeneratedPoolNumber() + 1, endNumber));
			}

		}
		setStartNumber(prSeriesDTO);
		return prSeriesDTO;
	}

	private Optional<PRNumberSeriesConfigDTO> configureNextPrSeries(OfficeDTO office, CovCategory covCategory) {

		PRDistrictConfigDTO prStateConfigDTO = this.getPrStateConfigDTO(office.getOfficeCode(), covCategory);
		if (null == prStateConfigDTO) {
			return Optional.empty();
		}
		PRNumberSeriesConfigDTO newPRNumberSeriesConfigDTO = new PRNumberSeriesConfigDTO();
		newPRNumberSeriesConfigDTO.setStartNumber(prStateConfigDTO.getStartNumber());
		newPRNumberSeriesConfigDTO.setEndNumber(prStateConfigDTO.getEndNumber() + 1);
		newPRNumberSeriesConfigDTO.setCurrentNumber(prStateConfigDTO.getStartNumber());
		newPRNumberSeriesConfigDTO.setPrSeries(prStateConfigDTO.getCurrentSeries());
		// Max Number
		// newPRNumberSeriesConfigDTO.setLastGeneratedPoolNumber(100);
		newPRNumberSeriesConfigDTO.setSeriesStatus(RecordStatus.ACTIVE);
		newPRNumberSeriesConfigDTO.setOfficeCode(office.getOfficeCode());
		newPRNumberSeriesConfigDTO.setRegType(covCategory);
		newPRNumberSeriesConfigDTO.setLastGeneratedPoolNumber(0);
		newPRNumberSeriesConfigDTO.setOfficeNumberSeries(prStateConfigDTO.getOfficeNumberSeries());
		newPRNumberSeriesConfigDTO.setCreatedDate(LocalDateTime.now());
		if (newPRNumberSeriesConfigDTO.getPrSeries() != null) {
			prSeriesDAO.save(newPRNumberSeriesConfigDTO);
		} else {
			// Need handle if pr_distict_config data not found for the offices series type
			newPRNumberSeriesConfigDTO = null;
		}
		return Optional.ofNullable(newPRNumberSeriesConfigDTO);
	}

	private Optional<PRNumberSeriesConfigDTO> updateAndConfigureNextPrSeries(OfficeDTO office, CovCategory covCategory,
			PRNumberSeriesConfigDTO prSeriesDTO) {

		prSeriesDTO.setSeriesStatus(RecordStatus.ACTIVE_INCOMPLET);
		prSeriesDAO.save(prSeriesDTO);
		return configureNextPrSeries(office, covCategory);

	}

	private PRDistrictConfigDTO getPrStateConfigDTO(String type, CovCategory covCategory) {

		Optional<PRDistrictConfigDTO> prDistrictConfigOptional = pRDistrictConfigDAO
				.findByGenerationTypeAndVehicleType(type, covCategory);
		if (prDistrictConfigOptional.isPresent()) {
			PRDistrictConfigDTO prDistrictConfigDTO = prDistrictConfigOptional.get();
			if (StringUtils.isBlank(prDistrictConfigDTO.getCurrentSeries())) {
				prDistrictConfigDTO.setCurrentSeries(prDistrictConfigDTO.getStartSeries());
			} else {
				if (null == prDistrictConfigDTO.getFinishedSeries()) {
					prDistrictConfigDTO.setFinishedSeries(new ArrayList<>());
				}
				prDistrictConfigDTO.getFinishedSeries().add(prDistrictConfigDTO.getCurrentSeries());
				generateNextSeries(prDistrictConfigDTO);
			}
			pRDistrictConfigDAO.save(prDistrictConfigDTO);
			return prDistrictConfigDTO;
		} else {
			// TODO need to handle if PRDistrictConfigDTO Master data not found is not found
		}
		return null;
	}

	// TODO: Need to Implement Logic
	private void generateNextSeries(PRDistrictConfigDTO prDistrictConfigDTO) {
		if (prDistrictConfigDTO.getCurrentSeries().equals(prDistrictConfigDTO.getEndSeries())) {
			// Increment the OfficeNumberSeries
			prDistrictConfigDTO.setOfficeNumberSeries(getCurrentOfficeNumberSeries(prDistrictConfigDTO));
			// Insert Current Series to Finished Series
			List<String> finishedSeries = (prDistrictConfigDTO.getFinishedSeries().isEmpty()) ? new ArrayList<String>()
					: prDistrictConfigDTO.getFinishedSeries();
			finishedSeries.add(prDistrictConfigDTO.getCurrentSeries());
			prDistrictConfigDTO.setFinishedSeries(finishedSeries);
			// Reset the Start Series
			prDistrictConfigDTO.setCurrentSeries(prDistrictConfigDTO.getStartSeries());
		} else {
			// Increment the CurrentSeries
			prDistrictConfigDTO.setCurrentSeries(getCurrentSeries(prDistrictConfigDTO));
		}
	}

	private String getCurrentOfficeNumberSeries(PRDistrictConfigDTO prDistrictConfigDTO) {

		StringBuilder sb = new StringBuilder();
		String officeNoSeries = prDistrictConfigDTO.getOfficeNumberSeries();
		sb.append(officeNoSeries.substring(0, 2));
		Integer officeNo = Integer.valueOf(officeNoSeries.substring(2, 4));
		officeNo++;
		sb.append(officeNo.toString());
		return sb.toString();

	}

	private String getCurrentSeries(PRDistrictConfigDTO prDistrictConfigDTO) {

		StringBuilder newSeries = new StringBuilder(prDistrictConfigDTO.getCurrentSeries());
		int endIndex = (newSeries.length() - 1);
		char endChar = newSeries.charAt(endIndex);
		return String.valueOf(getUnRestrictedChar(++endChar, endIndex, newSeries, prDistrictConfigDTO));

	}

	private StringBuilder getUnRestrictedChar(char endChar, int endIndex, StringBuilder newSeries,
			PRDistrictConfigDTO prDistrictConfigDTO) {
		if (prDistrictConfigDTO.getSeriesCharNotIn().contains(endChar)) {
			return getUnRestrictedChar(++endChar, endIndex, newSeries, prDistrictConfigDTO);
		}
		if (endIndex == 0 && prDistrictConfigDTO.getStartCharNotIn().contains(endChar)) {
			return getUnRestrictedChar(++endChar, endIndex, newSeries, prDistrictConfigDTO);
		}
		if (endChar == lastChar) {
			if (endIndex == prDistrictConfigDTO.getMaxSeriesLength() - 1) {
				endChar = startChar;
			} else {
				endChar = prDistrictConfigDTO.getStartSeries().charAt(0);
			}
			newSeries.setCharAt(endIndex, endChar);
			if (newSeries.length() != prDistrictConfigDTO.getMaxSeriesLength() - 1) {
				endIndex--;
				endChar = newSeries.charAt(endIndex);
				return getUnRestrictedChar(++endChar, endIndex, newSeries, prDistrictConfigDTO);
			}
		}
		newSeries.setCharAt(endIndex, endChar);
		if (prDistrictConfigDTO.getEndSeriesOfStartIndex().equals(prDistrictConfigDTO.getCurrentSeries())) {
			newSeries.append(startChar);
		}
		return newSeries;
	}

	/*
	 * private static PRDistrictConfigDTO setPRDistrictConfigDTO() {
	 * PRDistrictConfigDTO dto = new PRDistrictConfigDTO();
	 * dto.setOfficeNumberSeries("AP39"); dto.setVehicleType(CovCategory.N);
	 * dto.setFinishedSeries(new ArrayList<String>()); dto.setStartNumber(1);
	 * dto.setCurrentNumber(1); dto.setEndNumber(9999); dto.setStartSeries("A");
	 * dto.setCurrentSeries("SZ"); dto.setEndSeries("SZ");
	 * dto.setGenerationType("STATE"); dto.setMaxSeriesLength(2);
	 * dto.setStartCharNotIn(Arrays.asList('T', 'U', 'V', 'W', 'X', 'Y', 'Z', 'P'));
	 * dto.setSeriesCharNotIn(Arrays.asList('I', 'O'));
	 * dto.setEndSeriesOfStartIndex("S"); return dto; }
	 * 
	 * public static void main(String args[]) { PRDistrictConfigDTO
	 * prDistrictConfigDTO = setPRDistrictConfigDTO();
	 * System.out.println("Current Series : "+prDistrictConfigDTO.getCurrentSeries()
	 * ); generateNextSeries(prDistrictConfigDTO);
	 * System.out.println("Next Series : "+prDistrictConfigDTO.getCurrentSeries());
	 * }
	 */

	
	// TODO:Below method will Move to child class of other
		private List<PRNumberSeriesConfigDTO> getCountDropDown(String officeCode, CovCategory covCategory,Boolean status) {
			List<PRNumberSeriesConfigDTO> prConfignfigList = prSeriesDAO.findByOfficeCodeAndRegTypeAndSeriesStatusIn(
					officeCode, covCategory, Arrays.asList(RecordStatus.ACTIVE, RecordStatus.ACTIVE_INCOMPLET));
		/*
		 * if (prConfignfigList.isEmpty()) {
		 * logger.warn("PR series not found for vehicle type: {}", covCategory); return
		 * Collections.emptyList(); }
		 * 
		 * prConfignfigList.sort((s1, s2) ->
		 * s2.getSeriesStatus().getCode().compareTo(s1.getSeriesStatus().getCode()));
		 */
			Optional<PropertiesDTO> propertiesOpt = propertiesDAO.findByModule("SP");
			if (propertiesOpt.isPresent() && propertiesOpt.get().getStatus() && status) {
				Optional<PRNumberSeriesConfigDTO> seriesDto = prSeriesDAO.findByOfficeCodeAndRegTypeAndCurrentDateAndSeriesStatus(officeCode, covCategory,LocalDate.now(),RecordStatus.INACTIVE);
				if(seriesDto.isPresent()) {
					Integer count = prPoolDao.countByOfficeCodeAndPrSeriesIdAndNumberTypeAndPoolStatus(officeCode,seriesDto.get().getPrSeriesId(),"P",NumberPoolStatus.OPEN);
					if(count!=0) {
						prConfignfigList.add(0, seriesDto.get());
					}
				}
			}
			return prConfignfigList;

		}
}
