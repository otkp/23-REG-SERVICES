package org.epragati.sn.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.epragati.common.dao.PropertiesDAO;
import org.epragati.common.dto.PropertiesDTO;
import org.epragati.constants.CovCategory;
import org.epragati.constants.Schedulers;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.GeneratedPrDetailsDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.StagingRegistrationDetailsDAO;
import org.epragati.master.dto.GeneratedPrDetailsDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.service.PrSeriesService;
import org.epragati.rta.service.impl.service.RTAService;
import org.epragati.service.notification.NotificationUtil;
import org.epragati.sn.dao.SpecialNumberDetailsDAO;
import org.epragati.sn.numberseries.dao.PRDistrictConfigDAO;
import org.epragati.sn.numberseries.dao.PRPoolDAO;
import org.epragati.sn.numberseries.dao.PrimesNumbersDAO;
import org.epragati.sn.numberseries.dao.RandomNumberDAO;
import org.epragati.sn.numberseries.dao.SeriesTypeDAO;
import org.epragati.sn.numberseries.dao.SnPrSeriesDAO;
import org.epragati.sn.numberseries.dto.PRNumberSeriesConfigDTO;
import org.epragati.sn.numberseries.dto.PRPoolDTO;
import org.epragati.sn.numberseries.dto.PremiumNumbersDTO;
import org.epragati.sn.numberseries.mapper.BidNumbersDetailsMapper;
import org.epragati.sn.service.impl.ActionsDetailsHelper;
import org.epragati.sn.vo.BidConfigMasterVO;
import org.epragati.sn.vo.LeftOverVO;
import org.epragati.sn.vo.NumberSeriesDetailsVO;
import org.epragati.sn.vo.SpecialFeeAndNumberDetailsVO;
import org.epragati.util.BidNumberType;
import org.epragati.util.NumberPoolStatus;
import org.epragati.util.RecordStatus;
import org.epragati.util.SumOfDigits;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Service;

@Service
public abstract class NumberSeriesService {

	protected static final Logger logger = LoggerFactory.getLogger(NumberSeriesService.class);

	@Autowired
	protected PRPoolDAO numbersPoolDAO;

	@Autowired
	protected BidNumbersDetailsMapper bidNumbersDetailsMapper;

	@Autowired
	protected SnPrSeriesDAO prSeriesDAO;

	@Autowired
	protected PrimesNumbersDAO primesNumberMasterDAO;

	@Autowired
	protected SumOfDigits sumOfDigits;

	@Autowired
	protected PRPoolDAO prPoolDao;

	@Autowired
	protected OfficeDAO officeDAO;

	@Autowired
	protected ActionsDetailsHelper actionsDetailsHelper;

	@Autowired
	protected PRDistrictConfigDAO pRDistrictConfigDAO;

	@Autowired
	protected SeriesTypeDAO seriesTypeDAO;

	@Autowired
	protected PrimesNumbersDAO primeNumberDAO;

	@Autowired
	protected SpecialNumberDetailsDAO specialNumberDetailsDAO;

	@Autowired
	protected NotificationUtil notifications;

	protected List<Integer> list;

	protected List<PremiumNumbersDTO> listOfPrimerNumber;

	protected List<String> errors;
	@Autowired
	protected GeneratedPrDetailsDAO generatedPrDetailsDAO;

	@Autowired
	protected StagingRegistrationDetailsDAO stagingRegistrationDetailsDAO;

	@Autowired
	protected RegistrationDetailDAO registrationDetailDAO;

	@Autowired
	protected RTAService rtaService;

	@Autowired
	private PrSeriesService prSeriesService;

	protected Integer maxNumberPoolSize;

	protected BidConfigMasterVO bidConfigMasterVO;

	protected final String rangeConcater = " - ";

	@Autowired
	protected RandomNumberDAO randomNumberDAO;

	@Autowired
	protected PropertiesDAO propertiesDAO;

	@PostConstruct
	private void getPrimeNumbers() {

		listOfPrimerNumber = primeNumberDAO.findAll();

		this.list = listOfPrimerNumber.stream().map(n -> n.getPrimeNumber()).collect(Collectors.toList());

		Optional<BidConfigMasterVO> resultOptional = prSeriesService.getBidConfigMasterData(false);
		if (!resultOptional.isPresent()) {
			throw new BadRequestException("Bid mater config data not found");
		}
		bidConfigMasterVO = resultOptional.get();
		maxNumberPoolSize = bidConfigMasterVO.getMaxNumbersForDay();

	}

	public abstract SpecialFeeAndNumberDetailsVO getNumberSeriesByOfficeCode(String officeCode, CovCategory regType,
			String rang, String seriesId);

	public abstract List<String> generateNumbersIntoPool();

	public abstract void generateNumbersIntoPool(CovCategory covCategory, OfficeDTO officeDTO);

	public abstract void generateNumbersIntoPoolForOffice(String officeCode, String regType);

	public abstract List<NumberSeriesDetailsVO> getNumberRange(CovCategory regType, Boolean value);

	public abstract List<LeftOverVO> getrAvalibleLeftOverNumbers(String officeCode, CovCategory regType,
			String prSeries);

	public abstract Set<String> getListOfLeftOverAvalibleSeries(String officeCode, CovCategory regType);

	// TODO:Below method will Move to child class of other
	public List<PRNumberSeriesConfigDTO> getPRNumberSeriesConfigData(String officeCode, CovCategory covCategory) {
		List<PRNumberSeriesConfigDTO> prConfignfigList = prSeriesDAO.findByOfficeCodeAndRegTypeAndSeriesStatusIn(
				officeCode, covCategory, Arrays.asList(RecordStatus.ACTIVE, RecordStatus.ACTIVE_INCOMPLET));
		if (prConfignfigList.isEmpty()) {
			logger.warn("PR series not found for vehicle type: {}", covCategory);
			return Collections.emptyList();
		}

		prConfignfigList.sort((s1, s2) -> s2.getSeriesStatus().getCode().compareTo(s1.getSeriesStatus().getCode()));
		return prConfignfigList;

	}

	// public methods

	// concrete methods

	protected void handleLokedAndPriviousNos(CovCategory regType, String officeCode) {
		try {
			LocalDate date = LocalDate.now().minusDays(1);
			PRNumberSeriesConfigDTO prNumberSeriesConfigDTO = getPRNumberSeriesConfigDTO(officeCode, regType, date);
			List<PRPoolDTO> prPoolDTOList;
			Optional<PropertiesDTO> propertiesOpt = propertiesDAO.findByModule("SP");
			if (propertiesOpt.isPresent() && propertiesOpt.get().getStatus()) {
				prPoolDTOList = prPoolDao.findByOfficeCodeAndRegTypeAndPoolStatusNotInAndPrSeries(officeCode, regType,
						Arrays.asList(NumberPoolStatus.ASSIGNED, NumberPoolStatus.LEFTOVER),
						prNumberSeriesConfigDTO.getPrSeries());
			}

			else {

				prPoolDTOList = prPoolDao.findByOfficeCodeAndRegTypeAndPoolStatusNotInAndPrSeriesAndPrNumberLessThan(
						officeCode, regType, Arrays.asList(NumberPoolStatus.ASSIGNED, NumberPoolStatus.LEFTOVER),
						prNumberSeriesConfigDTO.getPrSeries(), prNumberSeriesConfigDTO.getCurrentNumber());
			}
			prPoolDTOList.stream().forEach(p -> {
				if (null == p.getReservedDate()) {
					p.setPoolStatus(BidNumberType.P.equals(p.getNumberType()) ? NumberPoolStatus.LEFTOVER
							: NumberPoolStatus.REOPEN);
					actionsDetailsHelper.updateActionsDetails(p, Schedulers.NUMBERPOOL.toString());
				} else {
					p.setPoolStatus(NumberPoolStatus.RESERVED);
				}
			});
			prPoolDTOList.addAll(releaseLockedRecord(prNumberSeriesConfigDTO));
			prPoolDao.save(prPoolDTOList);
			prPoolDTOList.clear();
		} catch (BadRequestException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error("Exception while do left over process for the office: {}, covType: {},Exp: {e}", officeCode,
					regType, e);
		}

	}

	protected PRNumberSeriesConfigDTO getPRNumberSeriesConfigDTO(String officeCode, CovCategory regType,
			LocalDate date) {
		Optional<PRNumberSeriesConfigDTO> prNumberSeriesOpt = prSeriesDAO
				.findByOfficeCodeAndRegTypeAndCurrentDateAndSeriesStatus(officeCode, regType, date,
						RecordStatus.ACTIVE_INCOMPLET);
		if (!prNumberSeriesOpt.isPresent()) {
			prNumberSeriesOpt = prSeriesDAO.findByOfficeCodeAndRegTypeAndCurrentDateAndSeriesStatus(officeCode, regType,
					date, RecordStatus.ACTIVE);
		}
		if (!prNumberSeriesOpt.isPresent()) {
			throw new BadRequestException(
					"PR number series not available for the office: " + officeCode + " and transport type: " + regType);
		}
		return prNumberSeriesOpt.get();
	}

	protected List<PRPoolDTO> releaseLockedRecord(PRNumberSeriesConfigDTO prNumberSeriesConfigDTO) {
		List<PRPoolDTO> prPoolDTOList = prPoolDao.findByOfficeCodeAndRegTypeAndPoolStatusAndPrSeries(
				prNumberSeriesConfigDTO.getOfficeCode(), prNumberSeriesConfigDTO.getRegType(), NumberPoolStatus.LOCKED,
				prNumberSeriesConfigDTO.getPrSeries());
		prPoolDTOList.stream().forEach(p -> {
			if (prNumberSeriesConfigDTO.getCurrentNumber().intValue() >= p.getPrNumber().intValue()) {
				p.setPoolStatus(NumberPoolStatus.REOPEN);
			} else {
				p.setPoolStatus(NumberPoolStatus.OPEN);
			}
		});

		return prPoolDTOList;
	}

	protected List<PRPoolDTO> prepareNextSeriesWithPool(PRNumberSeriesConfigDTO prNumberSeriesConfig, Integer from,
			Integer to) {

		List<PRPoolDTO> finalList = new ArrayList<>();
		try {
			if (list.isEmpty()) {
				getPrimeNumbers();
			}
			logger.info("Inserting Pools from :{}, to:{}", from, to);

			List<PRPoolDTO> existedPrPools = numbersPoolDAO.findByOfficeNumberSeriesAndPrSeriesAndPrNumberBetween(
					prNumberSeriesConfig.getOfficeNumberSeries(), prNumberSeriesConfig.getPrSeries(), (from - 1),
					(to + 1));
			List<Integer> existedNumbers = existedPrPools.stream().map(p -> p.getPrNumber())
					.collect(Collectors.toList());
			existedPrPools.clear();

			while (from <= to) {
				String prNo = prNumberSeriesConfig.getOfficeNumberSeries() + prNumberSeriesConfig.getPrSeries()
						+ appendZero(from, 4);
				if (existedNumbers.contains(from)) {
					existedNumbers.remove(from);
					logger.warn("The prNo: {} already existed in pr pools,skipping", prNo);
					from++;
					continue;
				}

				PRPoolDTO nPoolDto = new PRPoolDTO();
				nPoolDto.setPrNo(prNo);

				nPoolDto.setPrSeriesId(prNumberSeriesConfig.getPrSeriesId());
				nPoolDto.setOfficeCode(prNumberSeriesConfig.getOfficeCode());
				nPoolDto.setPoolStatus(NumberPoolStatus.OPEN);
				nPoolDto.setOfficeNumberSeries(prNumberSeriesConfig.getOfficeNumberSeries());
				nPoolDto.setPrNumber(from);
				nPoolDto.setPrSeries(prNumberSeriesConfig.getPrSeries());
				nPoolDto.setRegType(prNumberSeriesConfig.getRegType());
				nPoolDto.setNumberType(BidNumberType.N);
				nPoolDto.setCreatedDate(LocalDateTime.now());
				nPoolDto.setNumberSum((short) sumOfDigits.getSumOfDigits(nPoolDto.getPrNumber()));
				if (list.contains(from)) {
					nPoolDto.setNumberType(BidNumberType.P);
					Optional<PremiumNumbersDTO> primeNumberOptional = listOfPrimerNumber.stream()
							.filter(pn -> nPoolDto.getPrNumber().equals(pn.getPrimeNumber())).findFirst();
					if (!primeNumberOptional.isPresent()) {
						logger.error("Premium number does not exist in pr_prime_numbers", nPoolDto.getPrNo());
					} else {
						nPoolDto.setAmount(primeNumberOptional.get().getCost());
						nPoolDto.setFeeRefId(primeNumberOptional.get().getPrimeId());
					}

				}
				finalList.add(nPoolDto);
				from++;
				prNumberSeriesConfig.setLastGeneratedPoolNumber(to);

			}

		} catch (Exception e) {
			errors.add(e.getMessage());
			logger.error("{}", e);
		}
		return finalList;

	}

	protected void setStartNumber(PRNumberSeriesConfigDTO prSeriesDTO) {
		if (null == prSeriesDTO.getCurrentDate() || !prSeriesDTO.getCurrentDate().equals(LocalDate.now())) {
			// prSeriesDTO.setToDayStartNo((prSeriesDTO.getCurrentNumber() == 1) ? 1 :
			// prSeriesDTO.getCurrentNumber() + 1);
			prSeriesDTO.setToDayStartNo((prSeriesDTO.getCurrentNumber() == 1) ? 1 : prSeriesDTO.getCurrentNumber());
			prSeriesDTO.setCurrentDate(LocalDate.now());
		}

	}

	// TODO:This method will move to number pool generation scedhuler

	private String appendZero(Integer number, int length) {
		return String.format("%0" + (length) + "d", number);
	}

	protected void inactionCompletedPrNumberSeries(List<PRNumberSeriesConfigDTO> prConfignfigList) {

		for (PRNumberSeriesConfigDTO prNuberSeries : prConfignfigList) { // RecordStatus.ACTIVE_INCOMPLET
			if (RecordStatus.ACTIVE_INCOMPLET.equals(prNuberSeries.getSeriesStatus())) {
				/*
				 * Integer oprnNumberCount = prPoolDao
				 * .countByOfficeCodeAndRegTypeAndPrSeriesAndPoolStatusInAndPrNumberGreaterThan(
				 * prNuberSeries.getOfficeCode(), prNuberSeries.getRegType(),
				 * prNuberSeries.getPrSeries(), NumberPoolStatus.OPEN,
				 * prNuberSeries.getCurrentNumber());
				 */
				/** Random number series inactive starts **/
				Integer oprnNumberCount = getPrSeriesOpenedNumberCount(prNuberSeries);
				/** Random number series inactive ends **/
				if (oprnNumberCount == 0) {
					prNuberSeries.setSeriesStatus(RecordStatus.INACTIVE);

				} else {
					setStartNumber(prNuberSeries);
				}
				prNuberSeries.setModifiedDate(LocalDateTime.now());
				prSeriesDAO.save(prNuberSeries);
			}
		}

	}

	//// ---------------

	protected List<PRPoolDTO> getPoolSizeNumbers(String officeCode, CovCategory regType) {
		PRNumberSeriesConfigDTO prNumberSeries = getPRNumberSeriesConfigDTO(officeCode, regType, LocalDate.now());
		Pageable pageable = new PageRequest(0, maxNumberPoolSize, new Sort(new Order(Direction.ASC, "prNumber")));
		Page<PRPoolDTO> numberPoolPages = numbersPoolDAO
				.findByOfficeCodeAndRegTypeAndPrSeriesAndPoolStatusNotInAndPrNumberGreaterThan(officeCode, regType,
						prNumberSeries.getPrSeries(), NumberPoolStatus.LEFTOVER, (prNumberSeries.getToDayStartNo() - 1),
						pageable);

		List<PRPoolDTO> todayNmbers = numberPoolPages.getContent();
		List<PRPoolDTO> finalList = new ArrayList<PRPoolDTO>();
		finalList.addAll(todayNmbers);
		if (RecordStatus.ACTIVE_INCOMPLET.equals(prNumberSeries.getSeriesStatus())
				&& maxNumberPoolSize > todayNmbers.size()) {

			Optional<PRNumberSeriesConfigDTO> extraPrNumberSeriesOpt = prSeriesDAO
					.findByOfficeCodeAndRegTypeAndSeriesStatus(officeCode, regType, RecordStatus.ACTIVE);
			if (extraPrNumberSeriesOpt.isPresent()
					&& !extraPrNumberSeriesOpt.get().getPrSeries().equals(prNumberSeries.getPrSeries())) {

				Pageable pageablepageable = new PageRequest(0, maxNumberPoolSize - todayNmbers.size(),
						new Sort(new Order(Direction.ASC, "prNumber")));
				Page<PRPoolDTO> extraPools = numbersPoolDAO
						.findByOfficeCodeAndRegTypeAndPrSeriesAndPoolStatusNotInAndPrNumberGreaterThan(officeCode,
								regType, extraPrNumberSeriesOpt.get().getPrSeries(), NumberPoolStatus.LEFTOVER,
								(extraPrNumberSeriesOpt.get().getToDayStartNo() - 1), pageablepageable);
				finalList.addAll(extraPools.getContent());
			}
		}
		return finalList;
	}

	protected SpecialFeeAndNumberDetailsVO getNumberDetails(List<PRPoolDTO> numbersPoolPage) {

		/*
		 * numbersPoolPage.forEach(p -> { if (null == p.getNumberSum()) {
		 * p.setNumberSum((short) sumOfDigits.getSumOfDigits(p.getPrNumber())); } });
		 */

		// BidNumberStatus.ASSIGNED removed form db call
		numbersPoolPage.sort((p1, p2) -> p1.getPrNumber().compareTo(p2.getPrNumber()));
		Map<String, List<PRPoolDTO>> numberSeriesesGroupBySeriesId = numbersPoolPage.stream()
				.collect(Collectors.groupingBy(PRPoolDTO::getPrSeriesId));
		logger.debug(" After groupingBy number pool  size : {}", numberSeriesesGroupBySeriesId.entrySet().size());
		SpecialFeeAndNumberDetailsVO specialFeeAndNumberDetailsVO = new SpecialFeeAndNumberDetailsVO();

		numberSeriesesGroupBySeriesId.forEach((key, value) -> {
			PRNumberSeriesConfigDTO prSeries = prSeriesDAO.findOne(key);
			if (prSeries != null) {
				NumberSeriesDetailsVO numberSeriesDetailsVO = new NumberSeriesDetailsVO();
				numberSeriesDetailsVO.setPrSeries(prSeries.getPrSeries());
				numberSeriesDetailsVO.setOfficeCode(prSeries.getOfficeCode());
				numberSeriesDetailsVO.setOfficeNumberSeries(prSeries.getOfficeNumberSeries());
				numberSeriesDetailsVO.setNumberSeries(bidNumbersDetailsMapper.convertbitDetails(value));
				numberSeriesDetailsVO.setRegType(prSeries.getRegType());
				if (RecordStatus.ACTIVE.getCode().equals(prSeries.getSeriesStatus().getCode())) {
					specialFeeAndNumberDetailsVO.setCurrentSeriesDetail(numberSeriesDetailsVO);
				} else if (RecordStatus.ACTIVE_INCOMPLET.getCode().equals(prSeries.getSeriesStatus().getCode())) {
					specialFeeAndNumberDetailsVO.setOldSeriesDetail(numberSeriesDetailsVO);
				} else if ((RecordStatus.INACTIVE.getCode().equals(prSeries.getSeriesStatus().getCode()))) {
					specialFeeAndNumberDetailsVO.setInactiveSeries(numberSeriesDetailsVO);
				}
			}
		});
		return specialFeeAndNumberDetailsVO;

	}

	protected int getOpenNumberCount(List<PRNumberSeriesConfigDTO> prNumberSeriesConfigList) {

		int prPoolOpenStatusSize = 0;
		for (PRNumberSeriesConfigDTO prNumberSeriesConfigDTO : prNumberSeriesConfigList) {

			Optional<PropertiesDTO> propertiesDTO = propertiesDAO.findByModule("SP");
			if (propertiesDTO.isPresent() && propertiesDTO.get().getStatus()) {

				int count = numbersPoolDAO.countByOfficeCodeAndRegTypeAndPrSeriesIdAndAndPoolStatusNotIn(
						prNumberSeriesConfigDTO.getOfficeCode(), prNumberSeriesConfigDTO.getRegType(),
						prNumberSeriesConfigDTO.getPrSeriesId(),
						Arrays.asList(NumberPoolStatus.ASSIGNED, NumberPoolStatus.LEFTOVER));
				prPoolOpenStatusSize = prPoolOpenStatusSize + count;
			} else {
				// old query By Anji

				/*
				 * int count = numbersPoolDAO.
				 * countByOfficeCodeAndRegTypeAndPrSeriesIdAndPrNumberGreaterThan(
				 * prNumberSeriesConfigDTO.getOfficeCode(),
				 * prNumberSeriesConfigDTO.getRegType(),
				 * prNumberSeriesConfigDTO.getPrSeriesId(),
				 * prNumberSeriesConfigDTO.getCurrentNumber());
				 */

				int count = numbersPoolDAO
						.countByOfficeCodeAndRegTypeAndPrSeriesIdAndPrNumberGreaterThanAndPoolStatusNotIn(
								prNumberSeriesConfigDTO.getOfficeCode(), prNumberSeriesConfigDTO.getRegType(),
								prNumberSeriesConfigDTO.getPrSeriesId(), prNumberSeriesConfigDTO.getCurrentNumber(),
								Arrays.asList(NumberPoolStatus.ASSIGNED));
				prPoolOpenStatusSize = prPoolOpenStatusSize + count;
			}
		}

		return prPoolOpenStatusSize;
	}

	// --------leftOver
	protected List<LeftOverVO> getLeftOverNumberSeriesByOfficeCode(String officeCode, CovCategory regType,
			String prSeries) {

		logger.debug("etNumberSeriesByOfficeCode start. OfficeCode:{}, RegType:{}", officeCode, regType);
		List<PRPoolDTO> numbersPoolPage = numbersPoolDAO
				.findByOfficeCodeAndRegTypeAndPoolStatusAndPrSeriesAndNumberType(officeCode, regType,
						NumberPoolStatus.LEFTOVER, prSeries, BidNumberType.P);
		List<LeftOverVO> leftOverVOList = new ArrayList<>();
		numbersPoolPage.forEach(pool -> {
			List<GeneratedPrDetailsDTO> generatedPrDetails = generatedPrDetailsDAO.findByPrNo(pool.getPrNo());
			if (generatedPrDetails.isEmpty()) {
				if (null == pool.getNumberSum()) {
					pool.setNumberSum((short) sumOfDigits.getSumOfDigits(pool.getPrNumber()));
				}
				leftOverVOList.add(leftOverNumbers(pool));
			}
		});

		return leftOverVOList;
	}

	private LeftOverVO leftOverNumbers(PRPoolDTO prPoolDTO) {
		LeftOverVO leftOverResult = new LeftOverVO();
		leftOverResult.setPrNumber(prPoolDTO.getPrNumber());
		leftOverResult.setNumberType(prPoolDTO.getNumberType());
		leftOverResult.setOfficeCode(prPoolDTO.getOfficeCode());
		leftOverResult.setPrNo(prPoolDTO.getPrNo());
		leftOverResult.setPrSeries(prPoolDTO.getPrSeries());
		leftOverResult.setOfficeNumberSeries(prPoolDTO.getOfficeNumberSeries());
		leftOverResult.setRegType(prPoolDTO.getRegType());
		leftOverResult.setPoolStatus(prPoolDTO.getPoolStatus());
		if (bidNumbersDetailsMapper.isReservedBeforeToday(prPoolDTO)) {
			leftOverResult.setPoolStatus(NumberPoolStatus.ASSIGNED);
		}
		leftOverResult.setBidId(prPoolDTO.getNumberPoolId());
		leftOverResult.setServiceFee(100);
		leftOverResult
				.setBidParticipants(prPoolDTO.getBidParticipants() != null ? prPoolDTO.getBidParticipants().size() : 0);
		if (StringUtils.isNoneBlank(prPoolDTO.getPrNumber().toString())) {
			Optional<PremiumNumbersDTO> premiumResult = primesNumberMasterDAO
					.findByPrimeNumberAndPrimeStatus(prPoolDTO.getPrNumber(), RecordStatus.ACTIVE);
			if (premiumResult.isPresent()) {
				leftOverResult.setCost(premiumResult.get().getCost());
			}
		}
		return leftOverResult;

	}

	/** Method to get pr series numbers opened count to inactive series **/
	private Integer getPrSeriesOpenedNumberCount(PRNumberSeriesConfigDTO prNuberSeries) {

		Optional<PropertiesDTO> propertiesDTO = propertiesDAO.findByModule("SP");
		if (propertiesDTO.isPresent() && propertiesDTO.get().getStatus()) {
			 
			Integer value =	prPoolDao.countByOfficeCodeAndRegTypeAndPrSeriesAndPoolStatusInAndNumberTypeNot(
					prNuberSeries.getOfficeCode(), prNuberSeries.getRegType(), prNuberSeries.getPrSeries(),
					Arrays.asList(NumberPoolStatus.OPEN, NumberPoolStatus.REOPEN), BidNumberType.P);
				if(value == 0 && prNuberSeries.getLastGeneratedPoolNumber()>=9999 ) {
					return value;
				}
				return value = 1;
		}
		return prPoolDao.countByOfficeCodeAndRegTypeAndPrSeriesAndPoolStatusInAndPrNumberGreaterThan(
				prNuberSeries.getOfficeCode(), prNuberSeries.getRegType(), prNuberSeries.getPrSeries(),
				NumberPoolStatus.OPEN, prNuberSeries.getCurrentNumber());

	}

}
