package org.epragati.master.service.impl;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.HolidayDAO;
import org.epragati.master.dao.HolidayExcemptionDAO;
import org.epragati.master.dao.OfficeSlotsAvailabilityDAO;
import org.epragati.master.dao.SlotsDAO;
import org.epragati.master.dto.HolidayDTO;
import org.epragati.master.dto.HolidayExcemptionDTO;
import org.epragati.master.dto.SlotsDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.mappers.SlotsMapper;
import org.epragati.master.service.SlotsService;
import org.epragati.master.service.StagingRegistrationDetailsSerivce;
import org.epragati.master.vo.SlotBookInputVO;
import org.epragati.master.vo.SlotsAvailable;
import org.epragati.master.vo.SlotsVO;
import org.epragati.regservice.dto.OfficeSlotsAvailabilityDTO;
import org.epragati.util.payment.ModuleEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SlotsServiceImpl implements SlotsService {

	private static final Logger logger = LoggerFactory.getLogger(SlotsServiceImpl.class);

	@Autowired
	private SlotsDAO slotsDAO;

	@Autowired
	private SlotsMapper slotsMapper;

	@Autowired
	private HolidayDAO holidayDAO;

	@Autowired
	private HolidayExcemptionDAO holidayExcemptionDAO;

	@Autowired
	private StagingRegistrationDetailsSerivce stagingRegistrationDetailsSerivce;
	
	@Autowired
	private OfficeSlotsAvailabilityDAO officeSlotAvailabilityDao;

	@Override
	public SlotsAvailable findAvilableSlotsBetweenDates(String officeCode,LocalDate reInspectionDate,Boolean isReInspection) {
		LocalDate localFromDate = null;
		if(reInspectionDate!=null&&isReInspection!=null&&isReInspection.equals(Boolean.TRUE)){
			localFromDate = reInspectionDate;
		}else{
			localFromDate = LocalDate.now().plusDays(1);
		}
		LocalDate localToDate = localFromDate.plusDays(90);
		SlotsAvailable slotAvailablilityVO = new SlotsAvailable();
		// slotAvailablilityVO.setMinDate(DateConverters.convertDateToString(localFromDate));
		// slotAvailablilityVO.setMaxDate(DateConverters.convertDateToString(localToDate));
		slotAvailablilityVO.setMinDate(localFromDate);
		slotAvailablilityVO.setMaxDate(localToDate);
		if (LocalTime.now().isBefore(LocalTime.parse("14:00"))) {

			slotAvailablilityVO.setSlotVO(slotsAvail(officeCode, localFromDate, localToDate));
			return slotAvailablilityVO;
		}
		slotAvailablilityVO.setSlotVO(slotsAvail(officeCode, localFromDate, localToDate));
		return slotAvailablilityVO;
	}
	
	
	private void verifyHolidayAndSunday(LocalDate date, String officeCode){
		
		if(date.getDayOfWeek() == DayOfWeek.SUNDAY){
			logger.error("Can Not Book Slots On Sundays, Office Code [{}] / Date [{}]", officeCode, date);
			throw new BadRequestException("Can Not Book Slots On Sundays");
		}
		// Check Holiday.
		
		Optional<HolidayDTO> holidayDetails =  holidayDAO.findByHolidayDateAndModuleAndHolidayStatusTrue(date, ModuleEnum.REG);
		if(holidayDetails.isPresent() ){
			logger.error("Can Not Book Slots On Holidays , Office Code [{}] / Date [{}]", officeCode, date);
			throw new BadRequestException("Can Not Book Slots On Holidays");
		}
		
	}
	
	@Override
	public String bookSlot(String moduleCode, List<String> service,  String officeCode, LocalDate date) {
		// TODO Auto-generated method stub
		verifyHolidayAndSunday(date, officeCode);
		synchronized(officeCode.intern()){
			SlotsDTO slotsDTO = new SlotsDTO();
			Optional<SlotsDTO> slotDetails =  slotsDAO.findByModuleAndOfficeCodeAndDate(moduleCode,officeCode, date);
			Optional<OfficeSlotsAvailabilityDTO> officeAvblHoursOptional = officeSlotAvailabilityDao.findByOfficeCode(officeCode);
			if(!officeAvblHoursOptional.isPresent()) {
				logger.error("OfficeSlotsAvailabilityDTO detail not found for the Office {}",officeCode);
				throw new BadRequestException("OfficeSlotsAvailabilityDTO detail not found for the Office {}"+officeCode);  			
			}
			if(slotDetails.isPresent()){
				
				slotsDTO = slotDetails.get();
				
				if((slotsDTO.getAvailableSlots() - 1)<=0){
					logger.error("Slot Not Available for Office Code  [{}] and Date [{}]", officeCode, date);
					throw new BadRequestException("Slot Not Available for Office Code  ("+officeCode+") and Date ("+date+") ");
				}
				slotsDTO.setAvailableSlots(slotDetails.get().getAvailableSlots() - 1);
				slotsDTO.setBooked(slotDetails.get().getBooked() + 1);
				
			}else{
				createNewSlot(moduleCode,service,officeCode,date,slotsDTO,officeAvblHoursOptional.get());
			}
			String slotTime;
			Integer bCount = slotsDTO.getBooked() - 1;
			Integer totalslots = slotsDTO.getTotalSlots();

			int hours = officeAvblHoursOptional.get().getNoOfHours();
			int setTime = calculateTime(bCount, totalslots, hours);

			if (setTime < officeAvblHoursOptional.get().getSlotsTime().size()) {
				slotTime=officeAvblHoursOptional.get().getSlotsTime().get(setTime);
			} else {
				throw new BadRequestException("Invalid slot time");
			}
			slotsDAO.save(slotsDTO);
			return slotTime;
		}
		
	}
	
	private void createNewSlot(String moduleCode, List<String> service,  String officeCode, LocalDate date
			,SlotsDTO slotsDTO,OfficeSlotsAvailabilityDTO officeSlotsAvailabilityDTO) {
		
		Integer perHourSlots =officeSlotsAvailabilityDTO.getPerHour();
		Integer noOfHours = officeSlotsAvailabilityDTO.getNoOfHours();
		Integer totalSlotes=perHourSlots*noOfHours;
		
		slotsDTO.setDate(date);
		slotsDTO.setStatus("Available");
		slotsDTO.setIsFastFilling(false);
		slotsDTO.setService(service);
		slotsDTO.setModule(moduleCode);
		
		slotsDTO.setAvailableSlots(totalSlotes-1);
		slotsDTO.setBooked(1);
		slotsDTO.setTotalSlots(totalSlotes); // getAvailbleSLot()
		slotsDTO.setOfficeCode(officeCode);
		
	}
	private int calculateTime(int bCount, int totalslots, int hours) {
		double d = Math.ceil((bCount / (totalslots / hours)));
		int hour = (int) (d);
		return hour;

	}
	
	@Override
	public String modifySlot(String moduleCode, List<String> service,  String officeCode, LocalDate oldDate, LocalDate newDate) {
		
		synchronized(officeCode.intern()){
			String sloTime=bookSlot(moduleCode,service,officeCode,newDate);
			Optional<SlotsDTO> oldSlotDetails =  slotsDAO.findByModuleAndOfficeCodeAndDate(moduleCode,officeCode, oldDate);
			if(!oldSlotDetails.isPresent()){
				logger.error("Old Slot Details Not Found For Office Code [{}] and Date [{}]", officeCode, oldDate);
				throw new BadRequestException("Old Slot Details Not Available for Office Code  ("+officeCode+") and Date ("+oldDate+") ");
			}
			oldSlotDetails.get().setAvailableSlots(oldSlotDetails.get().getAvailableSlots()+1);
			oldSlotDetails.get().setBooked(oldSlotDetails.get().getBooked()-1);
			slotsDAO.save(oldSlotDetails.get());
			return sloTime;

		}
		
	}

	private List<SlotsVO> AvailableSlotsDates(String officeCode, LocalDate localFromDate, LocalDate localToDate) {
		List<SlotsDTO> slotsDTOList = slotsDAO.findByOfficeCodeAndDateBetween(officeCode, localFromDate, localToDate);
		List<HolidayDTO> holidayDTO = holidayDAO.findByModuleAndHolidayDateBetweenAndHolidayStatusTrue(ModuleEnum.REG, localFromDate,
				localToDate,true);
		// To remove the duplicates in slots
		List<LocalDate> slotDatesList = slotsDTOList.stream().map(e -> e.getDate()).collect(Collectors.toList());
		List<LocalDate> holidayDatesList = holidayDTO.stream().map(h -> h.getHolidayDate())
				.collect(Collectors.toList());

		for (int i = 0; i <= slotDatesList.size() - 1; i++) {
			if (holidayDatesList.contains(slotDatesList.get(i))) {
				LocalDate date = slotDatesList.get(i);
				slotsDTOList.removeIf(s -> s.getDate().equals(date));
			}

		}

		holidayDTO.forEach(a -> {
			SlotsDTO slotsDTO = new SlotsDTO();
			slotsDTO.setDate(a.getHolidayDate());
			slotsDTO.setHoliday(true);
			slotsDTO.setStatus("Holiday");
			slotsDTOList.add(slotsDTO);
		});

		List<LocalDate> datesList = slotsDTOList.stream().map(e -> e.getDate()).collect(Collectors.toList());

		while (!localFromDate.isAfter(localToDate)) {

			if (!datesList.contains(localFromDate)) {
				SlotsDTO slotsDTO = new SlotsDTO();
				slotsDTO.setDate(localFromDate);
				slotsDTO.setStatus("Available");
				slotsDTO.setIsFastFilling(false);
				slotsDTO.setDate(localFromDate);
				slotsDTOList.add(slotsDTO);
			}
			localFromDate = localFromDate.plusDays(1);
		}
		List<SlotsVO> slotsAvilability = slotsMapper.convertEntity(slotsDTOList);
		logger.info(" slots available size=[{}]", slotsAvilability.size());
		List<SlotsVO> sortedList = slotsAvilability.stream().sorted((e1, e2) -> e2.getDate().compareTo(e1.getDate()))
				.collect(Collectors.toList());
		Collections.reverse(sortedList);
		return sortedList;
	}

	/**
	 * Booking and Modification of slots
	 */

	@Override
	public void slotsBooking(SlotBookInputVO slotBookingInputs) {
		Optional<StagingRegistrationDetailsDTO> stagingRegistionDetails = stagingRegistrationDetailsSerivce
				.FindbBasedOnApplicationNo(slotBookingInputs.getApplicationNo());
		if (!stagingRegistionDetails.isPresent()) {
			logger.error("No Records found with Application No [{}]",slotBookingInputs.getApplicationNo());
			throw new BadRequestException(
					"No records found for Application No : " + slotBookingInputs.getApplicationNo());
		}

		StagingRegistrationDetailsDTO stagingDetails = stagingRegistionDetails.get();
		slotBooking(stagingDetails, slotBookingInputs);
	}
	
	

	private void slotBooking(StagingRegistrationDetailsDTO stagingDetails, SlotBookInputVO slotBookingInputs) {
		synchronized (stagingDetails.getOfficeDetails().getOfficeCode().intern()) {
			LocalDate slotBookingDate = null;
			LocalDate slotModifiedDate = null;
			slotBookingDate = slotBookingInputs.getDate();
			List<SlotsDTO> slotsAvilability = slotsDAO
					.findByOfficeCode(stagingDetails.getOfficeDetails().getOfficeCode());
			if (slotBookingInputs.getModified() == true) {
				slotBookingDate = slotBookingInputs.getDate();
				slotModifiedDate = slotBookingInputs.getModifiedDate();
			}
			SlotsDTO slotsDtoValue = null;
			
			for (SlotsDTO slotsDTO : slotsAvilability) {
				if (slotsDTO.getDate().equals(slotBookingDate)) {
					slotsDtoValue = slotBookingDeails(slotsDTO, slotBookingDate, slotModifiedDate,
							slotBookingInputs.getModified());
				} else if (slotBookingInputs.getModified() == true) {
					if (slotsDTO.getDate().equals(slotModifiedDate)) {
						SlotsDTO slotsDtoValue1 = slotModficationDeails(slotsDTO, slotBookingDate, slotModifiedDate);
						slotsDAO.save(slotsDtoValue1);
					}
				}
			}
			slotsDAO.save(slotsDtoValue);
		}
	}

	private SlotsDTO slotBookingDeails(SlotsDTO slotsDTO, LocalDate slotBookingDate, LocalDate slotModifiedDate,
			Boolean modified) {
		if (slotsDTO.getDate().equals(slotBookingDate)) {
			if (slotsDTO.getAvailableSlots() < 0) {
				logger.debug("Slots are Not availabe for given date");
				throw new BadRequestException("No Slots avilable");
			}

			slotsDTO.setAvailableSlots(slotsDTO.getAvailableSlots() - 1);
			slotsDTO.setBooked(slotsDTO.getBooked() + 1);
		}
		return slotsDTO;
	}

	private SlotsDTO slotModficationDeails(SlotsDTO slotsDTO, LocalDate slotBookingDate, LocalDate slotModifiedDate) {
		if (slotsDTO.getDate().equals(slotModifiedDate)) {
			if (slotsDTO.getAvailableSlots() < 0) {
				logger.debug("Slots are Not availabe for given date");
				throw new BadRequestException("No Slots avilable");
			}

			slotsDTO.setAvailableSlots(slotsDTO.getAvailableSlots() + 1);
			slotsDTO.setBooked(slotsDTO.getBooked() - 1);
		}
		return slotsDTO;
	}

	@Override
	public SlotsDTO setLock(String OfficeCode, LocalDate slotDate) {
		Integer covsCount = 1;
		SlotsDTO dto = null;
		LocalDate reInspectionDate = null;
		Boolean isReInspection = Boolean.FALSE;
		SlotsAvailable slotsAvilability = findAvilableSlotsBetweenDates(OfficeCode,reInspectionDate,isReInspection);
		if (slotsAvilability != null) {
			for (SlotsVO slots : slotsAvilability.getSlotVO()) {
				if (slots.getDate().equals(slotDate)) {
					if (slots.getAvailableSlots() > 0) {
						/*if (slots.getLockedSlots() == null) {
							logger.info("no locked slots found on [{}]", slots.getDate());
							throw new BadRequestException("no locked slots slots founds");
						}*/
						if (slots.getAvailableSlots() == null) {
							logger.info("no available  slots found on [{}]", slots.getDate());
							logger.error("no available slots slots founds");
							throw new BadRequestException("no available slots slots founds");
						}
						slots.setLockedSlots(slots.getLockedSlots() + covsCount);
						slots.setAvailableSlots(slots.getAvailableSlots() - covsCount);
						slots.setBooked(slots.getBooked()+covsCount);
						slots.setCreatedDate(LocalDateTime.now());
						dto = slotsMapper.convertVO(slots);
					} else {
						logger.error("Number of slots are less than applied class of vehicles");
						throw new BadRequestException("Number of slots are less than applied class of vehicles");
					}
					break;
				}
			}
		}
		return dto;

	}

	@Override
	public SlotsDTO releaseLock(String OfficeCode, LocalDate slotDate) {
		Integer covsCount = 1;
		SlotsDTO dto = null;
		LocalDate reInspectionDate = null;
		Boolean isReInspection = Boolean.FALSE;
		SlotsAvailable slotsAvilability = findAvilableSlotsBetweenDates(OfficeCode,reInspectionDate,isReInspection);
		if (slotsAvilability != null) {
			for (SlotsVO slots : slotsAvilability.getSlotVO()) {
				if (slots.getDate().equals(slotDate)) {
					if (slots.getAvailableSlots() > 0) {
						if (slots.getLockedSlots() == null) {
							logger.error("No Locked Slots found");
							logger.info("no locked slots found on [{}]", slots.getDate());
							throw new BadRequestException("no locked slots slots founds");
						}
						if (slots.getAvailableSlots() == null) {
							logger.error("No available Slots found");
							logger.info("no available  slots found on [{}]", slots.getDate());
							throw new BadRequestException("no available slots slots founds");
						}
						slots.setLockedSlots(slots.getLockedSlots() - covsCount);
						slots.setAvailableSlots(slots.getAvailableSlots() + covsCount);
						slots.setBooked(slots.getBooked()-covsCount);
						slots.setCreatedDate(LocalDateTime.now());
						dto = slotsMapper.convertVO(slots);
					} else {
						logger.error("Number of slots are less than applied class of vehicles");
						throw new BadRequestException("Number of slots are less than applied class of vehicles");
					}
					break;
				}
			}
		}
		return dto;
	}

	public List<SlotsVO> slotsAvail(String officeCode, LocalDate fromDate, LocalDate toDate) {
		List<SlotsVO> slotsVo = new ArrayList<SlotsVO>();
		
		List<SlotsDTO> slotsList = slotsDAO.findByOfficeCodeAndDateBetween(officeCode, fromDate, toDate);
		if (slotsList.isEmpty() || slotsList == null) {
			logger.info("No slots found from Slots collection from [{}] to [{}]", fromDate, toDate);
		}

		List<HolidayDTO> holidayDTO = holidayDAO.findByModuleAndHolidayDateBetweenAndHolidayStatusTrue(ModuleEnum.REG, fromDate, toDate,true);
		if (holidayDTO.isEmpty() || holidayDTO == null) {
			logger.info("No Holiday  found from [{}]  to [{}]", fromDate, toDate);
		}

		List<HolidayExcemptionDTO> holidayExcemption = holidayExcemptionDAO
				.findByExcemptionDateBetweenAndHolidayTypeTrue(fromDate, toDate);
		if (holidayExcemption.isEmpty() || holidayExcemption == null) {
			logger.info("No Holiday Excemption found from [{}]  to [{}]", fromDate, toDate);
		}

		List<LocalDate> slotDatesList = slotsList.stream().map(e -> e.getDate()).collect(Collectors.toList());
		List<LocalDate> holidayDatesList = holidayDTO.stream().map(h -> h.getHolidayDate())
				.collect(Collectors.toList());
		List<SlotsDTO> list = new ArrayList<>();

		Iterator<SlotsDTO> iter = slotsList.iterator();

		while (iter.hasNext()) {
			SlotsDTO str = iter.next();
			if (holidayDatesList.contains(str.getDate())) {
				iter.remove();
				
			}
		}
	

		/*for (SlotsDTO slotDto : slotsList) {
			if (holidayDatesList.contains(slotDto.getDate())) {
				LocalDate date = slotDto.getDate();
				slotsList.removeIf(s -> s.getDate().equals(date));
			}

		}
*/
		// Holidays status as Holiday in slots Availability

		slotsList.forEach(a -> {
			SlotsDTO slots = new SlotsDTO();
			slots.setDate(a.getDate());
			slots.setHoliday(false);
			slots.setStatus(a.getStatus());
			slots.setOfficeCode(a.getOfficeCode());
			slots.setAvailableSlots(a.getAvailableSlots());
			slots.setTotalSlots(a.getTotalSlots());
			slots.setBooked(a.getBooked());
			slots.setIsFastFilling(a.getIsFastFilling());
			slots.setModule(a.getModule());
			list.add(slots);
		});

		holidayDTO.forEach(a -> {
			SlotsDTO slots = new SlotsDTO();
			slots.setDate(a.getHolidayDate());
			slots.setHoliday(true);
			slots.setStatus("Holiday");
			list.add(slots);
		});

		// Holidays Excemption as Status Holiday in slots Availabilty

		holidayExcemption.forEach(a -> {
			SlotsDTO slots = new SlotsDTO();
			slots.setDate(a.getExcemptionDate());
			slots.setHoliday(true);
			slots.setStatus("Holiday");
			list.add(slots);
		});
		// except Holidays and HolidaysExcemption rest all days as Available Status

		List<LocalDate> datesList = list.stream().map(e -> e.getDate()).collect(Collectors.toList());
		while (fromDate.isBefore(toDate) || fromDate.equals(toDate)) {
			if (!datesList.contains(fromDate)) {
				SlotsDTO slot = new SlotsDTO();
				slot.setDate(fromDate);
				slot.setStatus("Available");
				slot.setAvailableSlots(150);
				slot.setLockedSlots(0);
				slot.setIsFastFilling(false);
				list.add(slot);
			}
			fromDate = fromDate.plusDays(1);
		}
		
		List<SlotsDTO> slotsExcludingSundays=	list.stream().filter(day ->day.getDate().getDayOfWeek()!=DayOfWeek.SUNDAY).collect(Collectors.toList());
		slotsExcludingSundays.sort((d1, d2) -> d1.getDate().compareTo(d2.getDate()));
		logger.info("available slots size[{}]", list.size());
		slotsVo = slotsMapper.convertEntity(slotsExcludingSundays);
		return slotsVo;
	}

	@Override
	public void releaseSlot(String moduleCode,   String officeCode, LocalDate oldDate) {
		
		synchronized(officeCode.intern()){
			Optional<SlotsDTO> oldSlotDetails =  slotsDAO.findByModuleAndOfficeCodeAndDate(moduleCode,officeCode, oldDate);
			if(!oldSlotDetails.isPresent()){
				logger.error("Old Slot Details Not Found For Office Code [{}] and Date [{}]", officeCode, oldDate);
				throw new BadRequestException("Old Slot Details Not Available for Office Code  ("+officeCode+") and Date ("+oldDate+") ");
			}
			oldSlotDetails.get().setAvailableSlots(oldSlotDetails.get().getAvailableSlots()+1);
			oldSlotDetails.get().setBooked(oldSlotDetails.get().getBooked()-1);
			slotsDAO.save(oldSlotDetails.get());

		}
		
	}
}
