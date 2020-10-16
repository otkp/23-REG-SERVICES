package org.epragati.org.vahan.port.service;

import java.util.List;
import java.util.Optional;

import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.vahan.port.vo.RegVahanPortVO;
import org.epragati.vahan.port.vo.RtaToVahanVO;
import org.springframework.data.util.Pair;

public interface RegVahanPortService {
	
	void getRegVahanSyncRecords(Integer count);
	
	Pair<RegVahanPortVO, Boolean> setRegVahanSyncDetails(RegistrationDetailsDTO registrationDetailsDTO);

	void checkValidationForVahanSyncRecordSave(List<RtaToVahanVO> rtaToVahanVOlist);

	void getRegVahanSyncNewRecords(Integer count);

	void getRegVahanSyncRecordsNoc(Integer count);

	Optional<RegVahanPortVO> vahanSyncSearchForCitizen(String prNo);

	List<String> getVahanSyncWithPrNo(List<String> prNos);

	List<RegVahanPortVO> vahansyncrecords(Integer count);

	Optional<RegVahanPortVO> vahanservice(String prNo);

}