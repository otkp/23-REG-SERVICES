package org.epragati.common.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.epragati.common.dao.PropertiesDAO;
import org.epragati.common.dto.AutoActionDTO;
import org.epragati.common.dto.PropertiesDTO;
import org.epragati.common.service.DeemedAutoActionService;
import org.epragati.constants.CommonConstants;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.HolidayDAO;
import org.epragati.master.dao.HolidayExcemptionDAO;
import org.epragati.master.dao.MasterAmountSecoundCovsDAO;
import org.epragati.master.dao.RegServiceDAO;
import org.epragati.master.dao.StagingRegistrationDetailsDAO;
import org.epragati.master.dto.HolidayDTO;
import org.epragati.master.dto.HolidayExcemptionDTO;
import org.epragati.master.dto.MasterAmountSecoundCovsDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.rta.service.impl.service.RTAService;
import org.epragati.rta.service.impl.service.RegistratrionServicesApprovals;
import org.epragati.util.StatusRegistration;
import org.epragati.util.payment.ModuleEnum;
import org.epragati.util.payment.ServiceEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
public class DeemedAutoActionServiceImpl implements DeemedAutoActionService{

	private static final Logger logger = LoggerFactory.getLogger(DeemedAutoActionServiceImpl.class);
	
	@Autowired
	private RegServiceDAO regServiceDAO;

	@Autowired
	private StagingRegistrationDetailsDAO stagingRegistrationDetailsDAO;
	
	@Autowired
	private PropertiesDAO propertiesDAO;
	
	@Autowired
	private RegistratrionServicesApprovals registrationServices;
	
	@Autowired
	private HolidayDAO holidayDAO;

	@Autowired
	private HolidayExcemptionDAO holidayExcemptionDAO;

	@Autowired
	private MasterAmountSecoundCovsDAO masterAmountSecoundCovsDAO;
	
	@Autowired
	private RTAService rtaService;
	
	@Override
	public void regServiceAutoAction() {
		LocalDate date = LocalDate.now();
		logger.info("deemed auto action regServiceAutoAction started");
		Optional<PropertiesDTO> propertiesOpt = propertiesDAO.findByDeemandApprovalForRegServiceTrue();
		if (propertiesOpt.isPresent() && propertiesOpt.get().getDeemandAutoAction() != null) {
			logger.info("deemed auto action reg services started with db properties file");
			List<ServiceEnum> deemedServices = propertiesOpt.get().getDeemandAutoAction().stream()
					.filter(st -> st.getStatus()).collect(Collectors.toList()).stream().map(s -> s.getServiceName())
					.collect(Collectors.toList());
			for (AutoActionDTO autoActionDTO : propertiesOpt.get().getDeemandAutoAction()) {
				if (autoActionDTO.getServiceName() != null && autoActionDTO.getDays() != null
						&& autoActionDTO.getStatus()&&autoActionDTO.getServiceId()!=null) {
					if (checkForHoliday(date, autoActionDTO.getDays())) {
						logger.info("deemed auto action reg services started with db properties file with services type {} :",autoActionDTO.getServiceName());
						List<String> officeList=new ArrayList<String>();
						officeList.add(CommonConstants.OTHER);
						officeList.addAll(isHolidayExcemptionOfficeBase(date).getFirst());
						Pageable pageable = new PageRequest(0, 50);
						for (int i = 0; i < 1000; i++) {
							List<RegServiceDTO> regServiceList = regServiceDAO
									.findByAutoApprovalInitiatedDateLessThanAndServiceIdsAndApplicationStatusInAndOfficeCodeNotInAndSourceIsNullAndAutoActionStatusNotIn(
											date.minusDays(autoActionDTO.getDays()), autoActionDTO.getServiceId(),
											Arrays.asList(StatusRegistration.PAYMENTDONE.getDescription(),
													StatusRegistration.REUPLOAD.getDescription(),StatusRegistration.INITIATED.getDescription()),
											officeList, Arrays.asList(StatusRegistration.FAILED.getDescription()), pageable);
							if (regServiceList.isEmpty()) {
								break;
							}
							List<RegServiceDTO> filterlist = regServiceList.stream()
									.filter(reg -> reg.getMviOfficeCode() == null && CollectionUtils.isNotEmpty(reg.getActionDetails()) 
											&& reg.getServiceType().stream().allMatch(service->deemedServices.contains(service)))
									.collect(Collectors.toList());
							if (filterlist.isEmpty()) {
								break;
							}
							try {
								if (CollectionUtils.isNotEmpty(filterlist)) {
									registrationServices.doActionAutoForServices(filterlist);
								}
							} catch (Throwable e) {
								logger.error("Exception reg services auto approval : {}", e.getMessage());
							}
							filterlist.clear();
							regServiceList.clear();
						}
					}

				}
			}
			deemedServices.clear();
		}
	}

	/**
	 * method for Deemed Auto Approvals within 24 hours  in Scheduled process.
	 */
	@Override
	public void newRegDeemedAutoAction() {
		LocalDate date = LocalDate.now();
		long stTime = System.currentTimeMillis();
		logger.info("deemed auto action newRegDeemedAutoAction started time {} : ms" ,stTime);
		if (checkDeemedApprovalsNeedorNot(date)) {
			Optional<PropertiesDTO> propertiesOpt = propertiesDAO.findByDeemandApprovalForNewRegTrue();
			int daysBeforeDate = 0;
			if(propertiesOpt.isPresent()) {
				daysBeforeDate=propertiesOpt.get().getDeemandNewRegDays();
			}
			else if(daysBeforeDate==0) {
				throw new BadRequestException("Please provide Proper Days config for Auto Approvals");
			}	
			List<MasterAmountSecoundCovsDTO> masterCovsforSecondVehicle = masterAmountSecoundCovsDAO.findAll();
			List<String> secondCovList = masterCovsforSecondVehicle.stream().findFirst().get().getSecondcovcode();
			List<String> officeList=new ArrayList<String>();
			officeList.add(CommonConstants.OTHER);
			officeList.addAll(isHolidayExcemptionOfficeBase(date).getFirst());
			
			for (int i = 0; i < 1000; i++) {
				Pageable pageable = new PageRequest(0, 50);
				List<StagingRegistrationDetailsDTO> stagingList = stagingRegistrationDetailsDAO
						.findByOfficeDetailsOfficeCodeNotInAndAutoApprovalInitiatedDateLessThanAndApplicationStatusInAndClassOfVehicleNotIn(
								officeList, date.minusDays(daysBeforeDate),
								Arrays.asList(StatusRegistration.TRGENERATED.getDescription(),
										StatusRegistration.TAXPAID.getDescription(),
										StatusRegistration.DEALERRESUBMISSION.getDescription()),
								secondCovList, pageable);
				if (stagingList.isEmpty()) {
					break;
				}
				logger.info("total records : {} , iterationCount : {} ",stagingList.size(),i);
				rtaService.doDeemedAction(stagingList);
				stagingList.clear();
			}
			secondCovList.clear();
		}
		logger.info("deemed auto action newRegDeemedAutoAction ended time {} : ms",(System.currentTimeMillis()-stTime));
	}
	
	private boolean isHoliday(LocalDate date) {
		LocalDate nextDay=date.plusDays(1);
		List<HolidayDTO> holiday = holidayDAO.findByModuleAndHolidayDateBetweenAndHolidayStatusTrue( ModuleEnum.REG,date,nextDay,true);
		if (!holiday.isEmpty()) {
			return true;
		}
		return false;
	}

	private boolean isHolidayExcemption(LocalDate date) {
		Optional<HolidayExcemptionDTO> holidayExcemption = holidayExcemptionDAO
				.findByExcemptionDateAndHolidayTypeTrue(date);
		if (holidayExcemption.isPresent() && CollectionUtils.isEmpty(holidayExcemption.get().getOfficeCode())) {
			return true;
		}
		return false;
	}

	private Pair<List<String>, Boolean> isHolidayExcemptionOfficeBase(LocalDate date) {
		Optional<HolidayExcemptionDTO> holidayExcemption = holidayExcemptionDAO
				.findByExcemptionDateAndHolidayTypeTrue(date);
		if (holidayExcemption.isPresent()) {
			if(CollectionUtils.isNotEmpty(holidayExcemption.get().getOfficeCode())) {
				return Pair.of(holidayExcemption.get().getOfficeCode(), true);
			}
			return Pair.of(Collections.emptyList(), true);	
		}
		return Pair.of(Collections.emptyList(), false);
	}

	private boolean checkDeemedApprovalsNeedorNot(LocalDate date) {
		Optional<PropertiesDTO> propertiesOpt = propertiesDAO.findByDeemandApprovalForNewRegTrue();
		if (propertiesOpt.isPresent() && propertiesOpt.get().getDeemandNewRegDays() != null) {
			if (!checkForHoliday(date,propertiesOpt.get().getDeemandNewRegDays())) {
				return false;
			}
		}else {
			return false;
		}
		return true;
	}
	
	private boolean checkForHoliday(LocalDate date, Integer day) {
		if (dayCheck(date) || isHoliday(date) || isHoliday(date.plusDays(1)) || isHoliday(date.minusDays(day))
				|| isHoliday(date.minusDays(day + 1)) || isHolidayExcemption(date)
				|| isHolidayExcemption(date.plusDays(1)) || isHolidayExcemption(date.minusDays(day))
				|| isHolidayExcemption(date.minusDays(day + 1))) {
			return false;
		}
		return true;
	}
	
	private boolean dayCheck(LocalDate date) {
		if(date.getDayOfWeek().equals(DayOfWeek.SUNDAY)||date.getDayOfWeek().equals(DayOfWeek.MONDAY)) {
			return true;
		}
		return false;
	}
}
