package org.epragati.common.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.epragati.common.service.NumberGenerationService;
import org.epragati.constants.CommonConstants;
import org.epragati.constants.CovCategory;
import org.epragati.constants.MessageKeys;
import org.epragati.constants.OfficeType;
import org.epragati.constants.RegistrationTypeEnum;
import org.epragati.constants.Schedulers;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.MasterCovDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.RegServiceDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.StagingRegistrationDetailsDAO;
import org.epragati.master.dao.TrSeriesDAO;
import org.epragati.master.dto.MasterCovDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.dto.TrSeriesDTO;
import org.epragati.master.service.PrSeriesService;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.reports.dao.RegistrationCountReportDAO;
import org.epragati.reports.dto.RegistrationCountReportDTO;
import org.epragati.rta.service.impl.service.RTAService;
import org.epragati.rta.service.impl.service.RegistratrionServicesApprovals;
import org.epragati.sn.vo.BidConfigMasterVO;
import org.epragati.util.AppMessages;
import org.epragati.util.StatusRegistration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

/**
 * 
 * @author krishnarjun.pampana
 *
 */
@Service
public class NumberGenerationServiceImpl implements NumberGenerationService{
	
	private static final Logger logger = LoggerFactory.getLogger(NumberGenerationServiceImpl.class);

	@Autowired
	private AppMessages appMessages;

	@Autowired
	private TrSeriesDAO trSeriesDAO;
	
	@Autowired
	private StagingRegistrationDetailsDAO stagingRegistrationDetailsDAO;
	
	@Autowired
	private RTAService rtaService;
	
	@Autowired
	private RegServiceDAO regServiceDAO;
	
	@Autowired
	private RegistratrionServicesApprovals registrationServices;
	
	@Autowired
	private PrSeriesService prSeriesService;
	
	@Autowired
	private OfficeDAO officeDAO;
	
	@Autowired
	private MasterCovDAO masterCovDAO;
	
	@Autowired
	private RegistrationCountReportDAO registrationCountReportDAO;
	
	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;

	@Override
	public String getTrGeneratedSeries(Integer trDistrictId) {
		final String districtId = trDistrictId.toString();
		Optional<TrSeriesDTO> trSeriesOptional;
		StringBuilder series = new StringBuilder();
		logger.info(appMessages.getLogMessage(MessageKeys.DISTRICT_ID), districtId);
		// To avoid multi thread issues.
		synchronized (districtId.intern()) {
			trSeriesOptional = trSeriesDAO.findBytrDistrictId(trDistrictId);
			if (!trSeriesOptional.isPresent()) {
				logger.error(MessageKeys.TR_SERIES_NO_RECORDS_FOUND_BASED_ONDISTRICTID);
				throw new BadRequestException(
						appMessages.getResponseMessage(MessageKeys.TR_SERIES_NO_RECORDS_FOUND_BASED_ONDISTRICTID));
			}
			logger.info("TrSeries Series  [{}] & District [{}]", trSeriesOptional.get().getSeries(), districtId);
			TrSeriesDTO trSeriesDTO = trSeriesOptional.get();
			Integer currentNumber = 1;
			if (trSeriesDTO.getCurrentNo() != null) {
				currentNumber = trSeriesDTO.getCurrentNo();
			}
			String officeCode = trSeriesDTO.getSeries();
			series.append(officeCode);
			Integer start = trSeriesDTO.getStartFrom();
			Integer end = trSeriesDTO.getEndTo();
			currentNumber = currentNumber + 1;
			if (currentNumber > end) {
				trSeriesDTO.getFinishedSeries().add(trSeriesDTO.getPendingSeries().get(0));
				trSeriesDTO.getPendingSeries().remove(0);
				currentNumber = start;
			}
			trSeriesDTO.setPendingSeries(trSeriesDTO.getPendingSeries());
			String formattedNumber = appendZero(currentNumber, 4);
			trSeriesDTO.setCurrentNo(currentNumber);
			updateCurrentNo(trSeriesDTO);
			series.append(trSeriesDTO.getPendingSeries().get(0));
			series.append(RegistrationTypeEnum.TR.getType());
			series.append(formattedNumber);
		}
		return series.toString();
	}
	
	@Override
	public void prGenerationScheduler() {
		prGenerationsForReassigmentAndOSRecords();
		prGenerationsForNewRegistrationsRecords();
	}
	
	
	private void prGenerationsForReassigmentAndOSRecords() {
		logger.info("PR Generation for Reassignment and OS Records");
		final String orderName= "registrationDetails.applicantDetails.firstName";
		
		for (int i = 0; i < 200; i++) {
			Pageable pageble = new PageRequest(0, 20,new Sort(orderName));
			List<RegServiceDTO> regServiceDTOList = regServiceDAO
					.findByApplicationStatus(StatusRegistration.PRNUMBERPENDING.getDescription(), pageble);
			logger.info("Count of registration services PRNUMBERPENDING records: "+regServiceDTOList.size());

			if (regServiceDTOList.isEmpty()) {
				return;
			}

			regServiceDTOList.stream().forEach(regSerDTO -> {
				try {
					logger.info("Start reg serve prgeneration for: "+regSerDTO.getApplicationNo());
					registrationServices.prGenerationFromRegService(regSerDTO);
					logger.info("End reg serve prgeneration for: "+regSerDTO.getApplicationNo());
				} catch (Exception e) {
					logger.error("Exception While generating PR from RegServices:{},ex:{}", regSerDTO.getApplicationNo(),e);
					if (regSerDTO.getSchedulerIssues() == null) {
						regSerDTO.setSchedulerIssues(new ArrayList<>());
					}
					regSerDTO.getSchedulerIssues().add(LocalDateTime.now() + ": where " + Schedulers.PRNUMBERGENERATION
							+ ", Issue: " + e.getMessage());
					regServiceDAO.save(regSerDTO);
				}
				regSerDTO = null;
			});
			regServiceDTOList.clear();
		}

	}	

	private void prGenerationsForNewRegistrationsRecords() {
		logger.info("PR Generation for New Registration Records");
		final String orderName= "applicantDetails.firstName";
		for (int i = 0; i < 1000; i++) {
			Pageable pageble = new PageRequest(0, 20,new Sort(orderName));
			List<StagingRegistrationDetailsDTO> dtoList = stagingRegistrationDetailsDAO
					.findByApplicationStatus(StatusRegistration.PRNUMBERPENDING.getDescription(), pageble);
			logger.info("Count of REG PRNUMBERPENDING record: {}",dtoList.size());
			if (dtoList.isEmpty()) {
				return;
			}
			dtoList.stream().forEach(dto -> {
				try {
					logger.info("Start new reg prgeneration for: "+dto.getApplicationNo());
					rtaService.processPR(dto);
					logger.info("End new reg prgeneration for: "+dto.getApplicationNo());
				} catch (Exception e) {
					logger.error("Exception while PR Generation {}, is: {}", dto.getApplicationNo(),e);
					if (dto.getSchedulerIssues() == null) {
						dto.setSchedulerIssues(new ArrayList<>());
					}
					dto.getSchedulerIssues().add(LocalDateTime.now() + ": where " + Schedulers.PRNUMBERGENERATION
							+ ", Issue: " + e.getMessage());
					stagingRegistrationDetailsDAO.save(dto);
				}
				dto = null;
			});

			dtoList.clear();
		}

	}
	

	private String appendZero(Integer number, int length) {
		return String.format("%0" + (length) + "d", number);
	}


	private TrSeriesDTO updateCurrentNo(TrSeriesDTO trSeriesDTO) {
		TrSeriesDTO trSeries = null;
		trSeriesDAO.save(trSeriesDTO);
		return trSeries;
	}
	
	public void validateRequest(String authToken, String ip) {
		logger.warn(" [{}] These IP  is trying to access our schedulers", ip);
		Optional<BidConfigMasterVO> resultOptional= prSeriesService.getBidConfigMasterData(Boolean.TRUE);
		if (resultOptional.isPresent()
				&& resultOptional.get().getSchedulerAuthToken().equals(authToken)
				&& resultOptional.get().getIpNoToAccesSchedulers().contains(ip)) {
			return;
		}
		throw new BadRequestException("Autherization failed.");
	}
	
	@Override
	public void caluclateRegistrationCount(LocalDate countDatefromUI) {
		logger.info("caluclateRegistrationCount service starts");
		List<MasterCovDTO> masterCovDTOList = masterCovDAO.findAll(); 
		List<OfficeDTO> officeList= officeDAO.findLimitedDataNative(Arrays.asList(OfficeType.RTA.getCode(),OfficeType.UNI.getCode()),CommonConstants.OTHER,Boolean.TRUE);
		LocalDate countDate; 
		if(null==countDatefromUI) {
			countDate=LocalDate.now().minusDays(1);
		}else {
			countDate=countDatefromUI;
		} 
		officeList.stream().forEach(officeDTO-> {
			List<RegistrationCountReportDTO> registrationCountReportDTOList= new ArrayList<RegistrationCountReportDTO>();

			masterCovDTOList.stream().forEach(masterCovDTO->{
				synchronized((officeDTO.getOfficeCode()+masterCovDTO.getCovcode()).intern()) {
					if(!(OfficeType.UNI.getCode().equalsIgnoreCase(officeDTO.getType()) && 
							CovCategory.T.getCode().equalsIgnoreCase(masterCovDTO.getCategory()))) {

						Optional<RegistrationCountReportDTO> registrationCountReportDTOOptional=registrationCountReportDAO.
								findByOfficeCodeAndCovcodeAndCountDate(officeDTO.getOfficeCode(),masterCovDTO.getCovcode(),countDate);


						RegistrationCountReportDTO registrationCountReportDTO=null;
						if(registrationCountReportDTOOptional.isPresent()) {
							registrationCountReportDTO =registrationCountReportDTOOptional.get();
						}else {
							registrationCountReportDTO=new RegistrationCountReportDTO();
							registrationCountReportDTO.setCovcode(masterCovDTO.getCovcode());
							registrationCountReportDTO.setCovdescription(masterCovDTO.getCovdescription());
							registrationCountReportDTO.setOfficeCode(officeDTO.getOfficeCode());
							registrationCountReportDTO.setOfficeName(officeDTO.getOfficeName());
							registrationCountReportDTO.setDistId(officeDTO.getDistrict());
							//registrationCountReportDTO.setMandalCode(officeDTO.getm);
							//registrationCountReportDTO.setMandalName(mandalName);
							registrationCountReportDTO.setGroupCategory(masterCovDTO.getGroupCategory());
							registrationCountReportDTO.setVehicleType(masterCovDTO.getCategory());
							registrationCountReportDTO.setCountDate(countDate);
							registrationCountReportDTO.setCreatedDate(LocalDateTime.now());
						}
						long startTime = System.currentTimeMillis();
						registrationCountReportDTO.setCount(registrationDetailDAO.countByOfficeDetailsOfficeCodeAndClassOfVehicleAndIsActiveAndPrGeneratedDateLessThan(officeDTO.getOfficeCode(),masterCovDTO.getCovcode(),true,countDate.plusDays(1).atTime(00,00,00)));
						logger.warn(" offcieCOde: {}, ClassOfVehicle: {},PrGeneratedDate: {}, tottal excecution time of countByOfficeDetailsOfficeCodeAndClassOfVehicleAndIsActiveAndPrGeneratedDateLessThan is:{} ",officeDTO.getOfficeCode(),masterCovDTO.getCovcode(),countDate,
								(System.currentTimeMillis()-startTime));
						registrationCountReportDTOList.add(registrationCountReportDTO);
					} 
				}


			});
			if(!registrationCountReportDTOList.isEmpty()) {
				long startTime = System.currentTimeMillis();
				registrationCountReportDAO.save(registrationCountReportDTOList);
				logger.warn(" Saving for offcie:{}  is:{} ",officeDTO.getOfficeCode(),(System.currentTimeMillis()-startTime));
				registrationCountReportDTOList.clear();
			}

		});
		logger.info("caluclateRegistrationCount service Ends.");
	}
}
