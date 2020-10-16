package org.epragati.sn.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.collections4.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.epragati.constants.CovCategory;
import org.epragati.constants.OfficeType;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.sn.dto.PRDistrictConfigDTO;
import org.epragati.sn.dto.SeriesTypeDTO;
import org.epragati.sn.numberseries.dto.PRNumberSeriesConfigDTO;
import org.epragati.sn.numberseries.dto.PRPoolDTO;
import org.epragati.sn.service.NumberSeriesService;
import org.epragati.sn.vo.LeftOverVO;
import org.epragati.sn.vo.NumberSeriesDetailsVO;
import org.epragati.sn.vo.SpecialFeeAndNumberDetailsVO;
import org.epragati.util.BidNumberType;
import org.epragati.util.NumberPoolStatus;
import org.epragati.util.RecordStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("officeLevel")
public class NumberSeriesServiceImpl extends NumberSeriesService {

	@Override
	public SpecialFeeAndNumberDetailsVO getNumberSeriesByOfficeCode(String officeCode, CovCategory regType
			,String rang, String seriesId) {
		logger.info("etNumberSeriesByOfficeCode start. OfficeCode:{}, RegType:{}", officeCode, regType);
		return getNumberDetails(getPoolSizeNumbers(officeCode, regType));
	}

	@Override
	public List<String> generateNumbersIntoPool() {
		logger.info("generateNumbersIntoPool start");
		errors = new ArrayList<>();
		List<OfficeDTO> officeList = officeDAO.findByTypeInAndIsActive(
				Arrays.asList(OfficeType.RTA.getCode(), OfficeType.UNI.getCode()), Boolean.TRUE);
		// TODO: 'INA' meens NOt rta office to register vehicle, Need to change
		// to enum
		// officeList.stream().filter(o->!o.getOfficeNumberSeries().equals("INA")).collect(Collectors.toList());
		// TODO:need to change enum
		List<SeriesTypeDTO> seriesList = seriesTypeDAO.findBySeriesStatus(RecordStatus.ACTIVE.getDescription());
		for (OfficeDTO office : officeList) {
			logger.info("Start office:{}", office.getOfficeCode());
			seriesList.stream().forEach(series -> {
				try {
					// Note: P category type should be allow only AP016 office.
					if ((CovCategory.P.getCode().equals(series.getSeriesTypeId())
							&& "AP016".equals(office.getOfficeCode()))
							|| OfficeType.RTA.getCode().equals(office.getType())
							|| (OfficeType.UNI.getCode().equals(office.getType())
									&& CovCategory.N.getCode().equals(series.getSeriesTypeId()))) {
						CovCategory covCategory = CovCategory.getCovCategory(series.getSeriesTypeId());
						generateNumbersIntoPool(covCategory, office);
					}
				} catch (Exception e) {
					errors.add(e.getMessage());
					logger.error("Exception while pool{}", e);
				}

			});
			logger.info("END The office");
		}
		logger.info("generateNumbersIntoPool END");
		return errors;
	}
	@Override
	public void generateNumbersIntoPoolForOffice(String officeCode, String regType) {
		Optional<OfficeDTO> officeOpt = officeDAO.findByOfficeCode(officeCode);
		if (officeOpt.isPresent() && Arrays.asList(OfficeType.RTA.getCode(), OfficeType.UNI.getCode())
				.contains(officeOpt.get().getType())) {
			List<SeriesTypeDTO> seriesList = seriesTypeDAO.findBySeriesStatus(RecordStatus.ACTIVE.getDescription());
			Optional<SeriesTypeDTO> series = seriesList.stream().filter(s -> s.getSeriesTypeId().equals(regType))
					.findFirst();
			CovCategory covCategory = CovCategory.getCovCategory(series.get().getSeriesTypeId());
			generateNumbersIntoPool(covCategory, officeOpt.get());
		}

	}


	/**
	 * TODO: need to implement the logic for office level
	 */
	@Override
	public List<NumberSeriesDetailsVO> getNumberRange(CovCategory regType,Boolean value) {
		return null;
	}
	
	@Override
	public List<LeftOverVO> getrAvalibleLeftOverNumbers(String officeCode, CovCategory regType, String prSeries) {
		List<LeftOverVO> leftOverListResult = null;
		if (StringUtils.isNoneBlank(officeCode) && null!=regType) {
			leftOverListResult = this.getLeftOverNumberSeriesByOfficeCode(officeCode,regType, prSeries);
		}
		return leftOverListResult;
	}

	@Override
	public Set<String> getListOfLeftOverAvalibleSeries(String officeCode, CovCategory regType) {
		Set<String> prSeriesData = new HashSet<String>();
		if (StringUtils.isNoneBlank(officeCode) && null!=regType) {
			List<PRPoolDTO> leftOverList = numbersPoolDAO.findByOfficeCodeAndRegTypeAndPoolStatusAndNumberType(officeCode,
					regType, NumberPoolStatus.LEFTOVER, BidNumberType.P);
			leftOverList.forEach(pooldata -> {
				if (pooldata.getPrSeries() != null)
					prSeriesData.add(pooldata.getPrSeries());
			});

		}
		return prSeriesData;
	}

	private Optional<PRNumberSeriesConfigDTO> updateAndConfigureNextPrSeries(OfficeDTO office, CovCategory covCategory,
			PRNumberSeriesConfigDTO prSeriesDTO) {

		prSeriesDTO.setSeriesStatus(RecordStatus.ACTIVE_INCOMPLET);
		prSeriesDAO.save(prSeriesDTO);
		return configureNextPrSeries(office, covCategory);

	}

	private Optional<PRNumberSeriesConfigDTO> configureNextPrSeries(OfficeDTO office, CovCategory covCategory) {

		PRNumberSeriesConfigDTO newPRNumberSeriesConfigDTO = new PRNumberSeriesConfigDTO();

		newPRNumberSeriesConfigDTO.setStartNumber(1);
		newPRNumberSeriesConfigDTO.setEndNumber(10000);
		newPRNumberSeriesConfigDTO.setCurrentNumber(1);
		// Max Number
		// newPRNumberSeriesConfigDTO.setLastGeneratedPoolNumber(100);
		newPRNumberSeriesConfigDTO.setSeriesStatus(RecordStatus.ACTIVE);
		newPRNumberSeriesConfigDTO.setPrSeries(this.getNextSeries(office, covCategory));
		newPRNumberSeriesConfigDTO.setOfficeCode(office.getOfficeCode());
		newPRNumberSeriesConfigDTO.setRegType(covCategory);
		newPRNumberSeriesConfigDTO.setLastGeneratedPoolNumber(0);
		if (CovCategory.P.equals(covCategory)) {
			newPRNumberSeriesConfigDTO.setOfficeNumberSeries(office.getPoliceNumberSeries());
		} else {
			newPRNumberSeriesConfigDTO.setOfficeNumberSeries(office.getOfficeNumberSeries());
		}
		newPRNumberSeriesConfigDTO.setCreatedDate(LocalDateTime.now());
		if (newPRNumberSeriesConfigDTO.getPrSeries() != null) {
			prSeriesDAO.save(newPRNumberSeriesConfigDTO);

		} else {
			// Need handle if pr_distict_config data not found for the offices
			// series type
			newPRNumberSeriesConfigDTO = null;
		}
		return Optional.ofNullable(newPRNumberSeriesConfigDTO);

	}

	private String getNextSeries(OfficeDTO office, CovCategory covCategory) {

		Optional<PRDistrictConfigDTO> prDistrictConfigOptional = pRDistrictConfigDAO
				.findByOfficeNumberSeriesAndVehicleType(office.getOfficeNumberSeries(), covCategory);
		if (prDistrictConfigOptional.isPresent()) {
			Optional<String> newSeries = prDistrictConfigOptional.get().getPendingSeries().stream().findFirst();
			if (newSeries.isPresent()) {
				prDistrictConfigOptional.get().getPendingSeries().remove(0);
				if (prDistrictConfigOptional.get().getRunningSeries() == null) {
					prDistrictConfigOptional.get().setRunningSeries(new HashedMap<>());
				}
				prDistrictConfigOptional.get().getRunningSeries().put(newSeries.get(), office.getOfficeCode());

				// TODO:need handle fineshed series.
				pRDistrictConfigDAO.save(prDistrictConfigOptional.get());
				return newSeries.get();
			} else {
				// TODO need to handle if pr pending series is not found
			}

		} else {
			// TODO need to handle if PRDistrictConfigDTO Master data not found
			// is not found
		}
		return null;

	}


	@Override
	public void generateNumbersIntoPool(CovCategory covCategory, OfficeDTO office) {

		logger.info("OfficeCode:{} & Cov Type: {}", office.getOfficeCode(), covCategory);
		
		try {
			if (Arrays.asList(CovCategory.N, CovCategory.T).contains(covCategory)) {
				handleLokedAndPriviousNos(covCategory, office.getOfficeCode());
			}
		} catch (BadRequestException e) {
			logger.error("{}", e.getMessage());
		} catch (Exception e) {
			logger.error("{}", e);
		}
		PRNumberSeriesConfigDTO prNumberSeriesConfigDTO =null;
		List<PRNumberSeriesConfigDTO> prConfignfigList = this.getPRNumberSeriesConfigData(office.getOfficeCode(), covCategory);
		List<PRPoolDTO> prPoolList = new ArrayList<PRPoolDTO>();
		if (!prConfignfigList.isEmpty()) {
			prNumberSeriesConfigDTO =prConfignfigList.get((prConfignfigList.size()-1));
			prNumberSeriesConfigDTO = this.createPool(prPoolList, prNumberSeriesConfigDTO, office,this.getOpenNumberCount(prConfignfigList));

		} else {
			// Fresh case
			Optional<PRNumberSeriesConfigDTO> prSeriesOptional = this.configureNextPrSeries(office, covCategory);
			if (prSeriesOptional.isPresent()) {
				prNumberSeriesConfigDTO=prSeriesOptional.get();
				prNumberSeriesConfigDTO = this.createPool(prPoolList, prNumberSeriesConfigDTO, office,0);
			}
		}

		// Part 1
		if (null!=prNumberSeriesConfigDTO) {
			prNumberSeriesConfigDTO.setModifiedDate(LocalDateTime.now());
			prSeriesDAO.save(prNumberSeriesConfigDTO);
		}
		prPoolDao.save(prPoolList);
		// prSeries current status updations.
		inactionCompletedPrNumberSeries(prConfignfigList);

	}

	private PRNumberSeriesConfigDTO createPool(List<PRPoolDTO> prPoolList,
			PRNumberSeriesConfigDTO prSeriesDTO, OfficeDTO office,int prPoolOpenStatusSize) {

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

}
