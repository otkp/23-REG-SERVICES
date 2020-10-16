package org.epragati.org.vahan.port.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.auditService.AuditServiceImpl;
import org.epragati.common.dao.ErrorTrackLogDAO;
import org.epragati.constants.Schedulers;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.org.vahan.port.service.RegVahanPortService;
import org.epragati.registration.service.RegistrationMigrationSolutionsService;
import org.epragati.vahan.port.vo.RegVahanPortVO;
import org.epragati.vahan.port.vo.RtaToVahanVO;
import org.epragati.vahan.sync.dao.RtaToVahanDAO;
import org.epragati.vahan.sync.dao.VahanSyncDAO;
import org.epragati.vahan.sync.dto.RegVahanPortDTO;
import org.epragati.vahan.sync.mapper.VahanSyncFailedMapper;
import org.epragati.vahan.sync.mapper.VahanSyncMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;

@Service
public class RegVahanPortServiceImpl implements RegVahanPortService {

	private static final Logger logger = LoggerFactory.getLogger(RegVahanPortServiceImpl.class);

	@Autowired
	RegistrationDetailDAO registrationDetailDAO;

	@Autowired
	RegVahanPortValidationServiceImpl regVahanPortValidationServiceImpl;

	@Autowired
	ErrorTrackLogDAO errorTrackLogDAO;

	@Autowired
	AuditServiceImpl auditServiceImpl;

	@Autowired
	private RegistrationMigrationSolutionsService registrationMigrationSolutionsService;

	@Autowired
	private VahanSyncMapper vahanSyncMapper;

	@Autowired
	private VahanSyncDAO vahanSyncDAO;

	@Autowired
	private RtaToVahanDAO rtaToVahanDAO;

	@Autowired
	private VahanSyncFailedMapper vahanSyncFailedMapper;

	@Value("${vahan.service.data.token.vahan:}")
	private String vahanToken;

	@Value("${no.of.records.vahan.sync:75}")
	private int noOfRecordsForVahanSync;

	@Autowired
	private MongoTemplate mongoTemplate;

	@Override
	public void getRegVahanSyncRecords(Integer count) {
		for (int i = 0; i < 2000; i++) {
			List<RegVahanPortVO> regVahanPortVOList = new ArrayList<RegVahanPortVO>();
			List<RegistrationDetailsDTO> regList = null;
			String input1 = LocalDate.of(2019, Month.JANUARY, 30) + "T00:00:00.000Z";
			ZonedDateTime zdt1 = ZonedDateTime.parse(input1);
			LocalDateTime ldt1 = zdt1.toLocalDateTime();
			long startTimeInMilli = System.currentTimeMillis();
			logger.info("Started Time to get data from REG for VahanSync {}ms", startTimeInMilli);
			Pageable pageable = new PageRequest(0, count);
			try {
				regList = registrationDetailDAO
						.findByPrNoIsNotNullAndIsvahanSyncFalseAndIsvahanSyncSkipFalseAndPrGeneratedDateLessThan(
								pageable, ldt1);

			} catch (Exception e) {
				List<RegistrationDetailsDTO> regIds = registrationDetailDAO
						.findByPrNoIsNotNullAndIsvahanSyncFalseAndIsvahanSyncSkipFalseAndPrGeneratedDateLessThanNative(
								ldt1, pageable);
				regList = new ArrayList<>();
				for (RegistrationDetailsDTO reg : regIds) {
					commonQureyHandle(regList, reg);
				}
			}

			logger.info("Total execution time to get data from REG for VahanSync {}ms",
					(System.currentTimeMillis() - startTimeInMilli));

			if (CollectionUtils.isNotEmpty(regList)) {
				commonRegVahanSync(regList, regVahanPortVOList);
			} else {
				logger.info("getRegVahanSyncRecords no records vahan sync");
				break;
			}
		}
	}

	public List<RegVahanPortVO> vahansyncrecords(Integer count) {
		Pageable pageable = new PageRequest(0, count);
		long startTimeInMilli = System.currentTimeMillis();
		logger.info("Started to get data from from DB, and count: {}", count);
		List<RegVahanPortDTO> regVahanPortDTOList = vahanSyncDAO.findByIsvahanSyncFalse(pageable);
		logger.info("Total execution time to get data from mongo DB vahan for VahanSync {}ms",
				(System.currentTimeMillis() - startTimeInMilli));
		if (!regVahanPortDTOList.isEmpty()) {
			return vahanSyncMapper.convertEntityLitmit(regVahanPortDTOList);
		}
		return Collections.emptyList();
	}

	@Override
	public Pair<RegVahanPortVO, Boolean> setRegVahanSyncDetails(RegistrationDetailsDTO registrationDetailsDTO) {
		RegVahanPortVO regVahanPortVO = new RegVahanPortVO();
		List<String> errors = new ArrayList<>();
		try {
			Pair<RegVahanPortVO, List<String>> result = regVahanPortValidationServiceImpl
					.validateRegFields(registrationDetailsDTO, regVahanPortVO, errors);
			if (CollectionUtils.isEmpty(result.getSecond())) {
				regVahanPortVO = result.getFirst();
				return Pair.of(regVahanPortVO, Boolean.TRUE);
			} else if (CollectionUtils.isNotEmpty(result.getSecond())
					&& result.getSecond().stream().anyMatch(val -> val.equalsIgnoreCase("Noc Data Not Found"))) {
				return null;
			} else {
				errors = result.getSecond();
				auditServiceImpl.saveErrorTrackLog(Schedulers.VAHANSYNC.name(), registrationDetailsDTO.getPrNo(),
						registrationDetailsDTO.getApplicationNo(), errors.toString(),
						Schedulers.VAHANSYNC.name());
				logger.error("Exception Occured while posting the data to Vahan : {} ", errors.toString());
				return Pair.of(regVahanPortVO, Boolean.FALSE);
			}
		} catch (BadRequestException bre) {
			auditServiceImpl.saveErrorTrackLog(Schedulers.VAHANSYNC.name(), registrationDetailsDTO.getPrNo(),
					registrationDetailsDTO.getApplicationNo(), bre.toString(),
					Schedulers.VAHANSYNC.name());
		} catch (NullPointerException eee) {
			auditServiceImpl.saveErrorTrackLog(Schedulers.VAHANSYNC.name(), registrationDetailsDTO.getPrNo(),
					registrationDetailsDTO.getApplicationNo(), eee.toString(),
					Schedulers.VAHANSYNC.name());
			logger.error("Exception Occured while posting the data to vahan nic : {} prNo : {}", eee,
					registrationDetailsDTO.getPrNo());
		} 
		catch (Exception e) {
			auditServiceImpl.saveErrorTrackLog(Schedulers.VAHANSYNC.name(), registrationDetailsDTO.getPrNo(),
					registrationDetailsDTO.getApplicationNo(), e.toString(),
					"Exception");
			logger.error("Exception Occured while posting the data to vahan nic : {} prNo : {} ", e,
					registrationDetailsDTO.getPrNo());
		} catch (Throwable e) {
			logger.error("Exception Occured while posting the data to vahan nic : {} ", e);
		}
		return Pair.of(regVahanPortVO, Boolean.FALSE);
	}

	@Override
	public void checkValidationForVahanSyncRecordSave(List<RtaToVahanVO> rtaToVahanVOList) {
		long startTimeInMilli = System.currentTimeMillis();
		logger.info("Started Time to update vahansync update records bulk :{}ms", startTimeInMilli);
		for (RtaToVahanVO rtaToVahanVO : rtaToVahanVOList) {
			if (StringUtils.isAnyBlank(rtaToVahanVO.getPrNo())) {
				logger.error("prNo Not Found for vahan sync updaate :{}", rtaToVahanVO.getPrNo());
			}
			Optional<RegVahanPortDTO> optionalRegVahanPortDTO = vahanSyncDAO
					.findByPrNoAndIsvahanSyncFalse(rtaToVahanVO.getPrNo());
			if (optionalRegVahanPortDTO.isPresent()) {
				if (StringUtils.isNotEmpty(rtaToVahanVO.getError())) {
					auditServiceImpl.saveErrorTrackLog(Schedulers.VAHANSYNCRESPONSE.name(), rtaToVahanVO.getPrNo(),
							rtaToVahanVO.getPrNo(), rtaToVahanVO.getError(), Schedulers.VAHANSYNCRESPONSE.name());
					logger.info("Exception Occured with Vahan sync {[]}", rtaToVahanVO.getPrNo());
					rtaToVahanDAO.save(vahanSyncFailedMapper.convertVO(rtaToVahanVO));
					optionalRegVahanPortDTO.get().setIsErroFound(Boolean.TRUE);
					optionalRegVahanPortDTO.get().setIsvahanSync(Boolean.TRUE);
				} else {
					boolean isFinancer = false;
					boolean isNoc = false;
					boolean isFitness = false;
					boolean isTheft = false;

					if (optionalRegVahanPortDTO.get().getIsFinancier() != null
							&& optionalRegVahanPortDTO.get().getIsFinancier()
							&& rtaToVahanVO.getIsFinancerSync() != null
							&& rtaToVahanVO.getIsFinancerSync().equals(Boolean.TRUE)) {
						isFinancer = true;
					} else if (optionalRegVahanPortDTO.get().getIsFinancier() != null
							&& optionalRegVahanPortDTO.get().getIsFinancier()
							&& rtaToVahanVO.getIsFinancerSync() != null
							&& rtaToVahanVO.getIsFinancerSync().equals(Boolean.FALSE)) {
						isFinancer = false;
					} else {
						isFinancer = true;
					}
					if (optionalRegVahanPortDTO.get().getIsNocIssued() != null
							&& optionalRegVahanPortDTO.get().getIsNocIssued()
							&& rtaToVahanVO.getIsNocOwnerSync() != null
							&& rtaToVahanVO.getIsNocOwnerSync().equals(Boolean.FALSE)
							&& rtaToVahanVO.getIsNocSync() != null
							&& rtaToVahanVO.getIsNocSync().equals(Boolean.TRUE)) {
						isNoc = true;
					} else if (optionalRegVahanPortDTO.get().getIsNocIssued() != null
							&& optionalRegVahanPortDTO.get().getIsNocIssued() && rtaToVahanVO.getIsNocSync() != null
							&& rtaToVahanVO.getIsNocOwnerSync() != null
							&& rtaToVahanVO.getIsNocOwnerSync().equals(Boolean.TRUE)
							&& rtaToVahanVO.getIsNocSync().equals(Boolean.FALSE)) {
						isNoc = false;
					} else {
						isNoc = true;
					}
					if (optionalRegVahanPortDTO.get().getIsFitness() != null
							&& optionalRegVahanPortDTO.get().getIsFitness() && rtaToVahanVO.getIsFitnesSync() != null
							&& rtaToVahanVO.getIsFitnesSync().equals(Boolean.TRUE)) {
						isFitness = true;
					} else if (optionalRegVahanPortDTO.get().getIsFitness() != null
							&& optionalRegVahanPortDTO.get().getIsFitness() && rtaToVahanVO.getIsFitnesSync() != null
							&& rtaToVahanVO.getIsFitnesSync().equals(Boolean.FALSE)) {
						isFitness = false;
					} else {
						isFitness = true;
					}
					if (optionalRegVahanPortDTO.get().getIsTheft() != null && optionalRegVahanPortDTO.get().getIsTheft()
							&& rtaToVahanVO.getIsBlackListSync() != null
							&& rtaToVahanVO.getIsBlackListSync().equals(Boolean.TRUE)) {
						isTheft = true;
					} else if (optionalRegVahanPortDTO.get().getIsTheft() != null
							&& optionalRegVahanPortDTO.get().getIsTheft() && rtaToVahanVO.getIsBlackListSync() != null
							&& rtaToVahanVO.getIsBlackListSync().equals(Boolean.FALSE)) {
						isTheft = false;
					} else {
						isTheft = true;
					}
					if (rtaToVahanVO.getIsOwnerSync() != null && rtaToVahanVO.getIsOwnerSync().equals(Boolean.TRUE)
							&& rtaToVahanVO.getIsAxleSync() != null && rtaToVahanVO.getIsAxleSync().equals(Boolean.TRUE)
							&& rtaToVahanVO.getIsFeeSync() != null && rtaToVahanVO.getIsFeeSync().equals(Boolean.TRUE)
							&& rtaToVahanVO.getIsTaxSync() != null && rtaToVahanVO.getIsTaxSync().equals(Boolean.TRUE)
							&& rtaToVahanVO.getIsInsuranceSync() != null
							&& rtaToVahanVO.getIsInsuranceSync().equals(Boolean.TRUE) && isFinancer && isNoc
							&& isFitness && isTheft) {
						optionalRegVahanPortDTO.get().setIsvahanSync(Boolean.TRUE);
					} else {
						optionalRegVahanPortDTO.get().setIsPartially(Boolean.TRUE);
						optionalRegVahanPortDTO.get().setIsvahanSync(Boolean.TRUE);
					}
				}
				optionalRegVahanPortDTO.get().setlUpdate(LocalDateTime.now());
				vahanSyncDAO.save(optionalRegVahanPortDTO.get());
				logger.info("Record upadated in Vahan sync with pr no[{}]", rtaToVahanVO.getPrNo());
			} else {
				logger.error("No Records Found with prNo in vahansync [{}]", rtaToVahanVO.getPrNo());
			}
		}
		logger.info("Total execution time to update vahansync update records bulk :{}ms",
				(System.currentTimeMillis() - startTimeInMilli));
	}

	private void updateFlagFailedrecord(RegistrationDetailsDTO reg) {
		DB db = mongoTemplate.getDb();
		DBCollection dBCollection = db.getCollection("registration_details");
		BasicDBObject newDocument = new BasicDBObject();
		newDocument.append("$set", new BasicDBObject().append("isvahanSyncSkip", true));
		BasicDBObject searchQuery = new BasicDBObject().append("_id", reg.getApplicationNo());
		dBCollection.update(searchQuery, newDocument);
		logger.info("reg to vahansync record isvahanSyncSkip flag set to true");
	}

	public Optional<RegVahanPortVO> vahanservice(String prNo) {
		Optional<RegVahanPortVO> RegVahanPortVOOtp = Optional.empty();
		Optional<RegistrationDetailsDTO> regDtoOpt = registrationDetailDAO.findByPrNoAndIsvahanSyncFalse(prNo);
		if (regDtoOpt.isPresent()) {
			Pair<RegVahanPortVO, Boolean> result = setRegVahanSyncDetails(regDtoOpt.get());
			if (result!=null && result.getFirst() != null) {
				return Optional.of(result.getFirst());
			}
		}
		return RegVahanPortVOOtp;
	}

	@Override
	public void getRegVahanSyncNewRecords(Integer count) {
		for (int i = 0; i < 2000; i++) {
			List<RegVahanPortVO> regVahanPortVOList = new ArrayList<RegVahanPortVO>();
			List<RegistrationDetailsDTO> regList = null;
			String input1 = LocalDate.of(2019, Month.JANUARY, 30) + "T00:00:00.000Z";
			ZonedDateTime zdt1 = ZonedDateTime.parse(input1);
			LocalDateTime ldt1 = zdt1.toLocalDateTime();

			long startTimeInMilli = System.currentTimeMillis();
			logger.info("Started Time to get data from REG new for VahanSync {}ms", startTimeInMilli);
			Pageable pageable = new PageRequest(0, count);
			try {
				regList = registrationDetailDAO
						.findByPrNoIsNotNullAndIsvahanSyncFalseAndIsvahanSyncSkipFalseAndPrGeneratedDateGreaterThan(
								pageable, ldt1);

			} catch (Exception e) {
				List<RegistrationDetailsDTO> regIds = registrationDetailDAO
						.findByPrNoIsNotNullAndIsvahanSyncFalseAndIsvahanSyncSkipFalseAndPrGeneratedDateGreaterThanNative(
								ldt1, pageable);
				regList = new ArrayList<>();
				for (RegistrationDetailsDTO reg : regIds) {
					commonQureyHandle(regList, reg);
				}
			}

			logger.info("Total execution time to get data from REG new for VahanSync {}ms",
					(System.currentTimeMillis() - startTimeInMilli));

			if (CollectionUtils.isNotEmpty(regList)) {
				commonRegVahanSync(regList, regVahanPortVOList);
			} else {
				logger.info("getRegVahanSyncNewRecords no records vahan sync");
				break;
			}
		}
	}

	@Override
	public void getRegVahanSyncRecordsNoc(Integer count) {
		for (int i = 0; i < 2000; i++) {
			List<RegVahanPortVO> regVahanPortVOList = new ArrayList<RegVahanPortVO>();
			List<RegistrationDetailsDTO> regList = null;
			long startTimeInMilli = System.currentTimeMillis();
			logger.info("Started Time to get data from REG for VahanSync {}ms", startTimeInMilli);
			Pageable pageable = new PageRequest(0, count);
			try {
				regList = registrationDetailDAO
						.findByPrNoIsNotNullAndNocDetailsIsNotNullAndIsvahanSyncFalseAndIsvahanSyncSkipFalseNative(
								pageable);

			} catch (Exception e) {
				List<RegistrationDetailsDTO> regIds = registrationDetailDAO
						.findByPrNoIsNotNullAndNocDetailsIsNotNullAndIsvahanSyncFalseAndIsvahanSyncSkipFalseWithFieldsNative(
								pageable);
				regList = new ArrayList<>();
				for (RegistrationDetailsDTO reg : regIds) {
					commonQureyHandle(regList, reg);
				}
			}
			logger.info("Total execution time to get data from REG for VahanSync {}ms",
					(System.currentTimeMillis() - startTimeInMilli));

			if (CollectionUtils.isNotEmpty(regList)) {
				commonRegVahanSync(regList, regVahanPortVOList);
			} else {
				logger.info("getRegVahanSyncRecord no records vahan sync");
				break;
			}
		}
	}

	@Override
	public Optional<RegVahanPortVO> vahanSyncSearchForCitizen(String prNo) {
		Optional<RegVahanPortDTO> vahansyncOpt = vahanSyncDAO.findByPrNoOrderByLUpdateDesc(prNo);
		if (vahansyncOpt.isPresent()) {
			return Optional.of(vahanSyncMapper.convertLimitedFileds(vahansyncOpt.get()));
		}
		return Optional.empty();
	}

	private void commonRegVahanSync(List<RegistrationDetailsDTO> regList, List<RegVahanPortVO> regVahanPortVOList) {
		regList = registrationMigrationSolutionsService.removeInactiveRecordsToList(regList);
		regList.parallelStream().forEach(registrationDetailsDTO -> {
			Pair<RegVahanPortVO, Boolean> result = setRegVahanSyncDetails(registrationDetailsDTO);
			if (result!=null && result.getSecond()!=null && result.getSecond().equals(Boolean.TRUE)) {
				regVahanPortVOList.add(result.getFirst());
				registrationDetailsDTO.setIsvahanSync(true);
			}
			if (result!=null && result.getSecond()!=null && result.getSecond().equals(Boolean.FALSE)) {
				registrationDetailsDTO.setIsvahanSyncSkip(true);
			}
		});
		vahanSyncDAO.save(vahanSyncMapper.convertVO(regVahanPortVOList));
		registrationDetailDAO.save(regList);
		regList.clear();
		regVahanPortVOList.clear();
	}

	private void commonQureyHandle(List<RegistrationDetailsDTO> regList, RegistrationDetailsDTO reg) {
		try {
			RegistrationDetailsDTO regDTO = registrationDetailDAO.findOne(reg.getApplicationNo());
			regList.add(regDTO);
		} catch (Exception ee) {
			logger.error("reg to vahansync base java exception is {}: meassage{} :", reg.getApplicationNo(),
					ee.getMessage());
			updateFlagFailedrecord(reg);
		}
	}

	@Override
	public List<String> getVahanSyncWithPrNo(List<String> prNos) {
		List<String> message = new ArrayList<>();
		for (String prNo : prNos) {
			try {
				Optional<RegistrationDetailsDTO> regDetailsOpt = registrationDetailDAO.findByPrNo(prNo);

				if (regDetailsOpt.isPresent() && !regDetailsOpt.get().getIsvahanSync()
						&& !regDetailsOpt.get().getIsvahanSyncSkip()) {
					Pair<RegVahanPortVO, Boolean> result = setRegVahanSyncDetails(regDetailsOpt.get());
					if (result != null && result.getSecond() != null && result.getSecond().equals(Boolean.TRUE)) {
						vahanSyncDAO.save(vahanSyncMapper.convertVO(result.getFirst()));
						regDetailsOpt.get().setIsvahanSync(true);
					}
					if (result != null && result.getSecond() != null && result.getSecond().equals(Boolean.FALSE)) {
						regDetailsOpt.get().setIsvahanSyncSkip(true);
					}
					registrationDetailDAO.save(regDetailsOpt.get());
				} else {
					message.add(prNo);
				}
			} catch (Throwable e) {
				logger.error("getVahanSyncWithPrNo prNo : {} : {} ",prNo, e.getMessage());
				message.add(prNo);
				message.add(e.toString());
			}
		}
		return message;
	}
}
