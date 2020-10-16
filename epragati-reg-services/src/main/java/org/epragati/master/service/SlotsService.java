package org.epragati.master.service;

import java.time.LocalDate;
import java.util.List;

import org.epragati.master.dto.SlotsDTO;
import org.epragati.master.vo.SlotBookInputVO;
import org.epragati.master.vo.SlotsAvailable;

/**
 * @author sairam.cheruku
 *
 */
public interface SlotsService {

	SlotsAvailable findAvilableSlotsBetweenDates(String module,LocalDate reInspectionDate,Boolean isReInspection);

	void slotsBooking(SlotBookInputVO slotBookingInputs);

	SlotsDTO releaseLock(String OfficeCode, LocalDate slotDate);

	SlotsDTO setLock(String OfficeCode, LocalDate slotDate);

	String bookSlot(String moduleCode, List<String> service, String officeCode, LocalDate date);

	String modifySlot(String moduleCode, List<String> service, String officeCode, LocalDate oldDate, LocalDate newDate);

	void releaseSlot(String moduleCode, String officeCode, LocalDate oldDate);

}
