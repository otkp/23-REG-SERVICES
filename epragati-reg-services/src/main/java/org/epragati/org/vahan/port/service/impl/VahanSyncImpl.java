package org.epragati.org.vahan.port.service.impl;

import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.org.vahan.port.service.RegVahanPortService;
import org.epragati.org.vahan.port.service.VahanSync;
import org.epragati.vahan.port.vo.RegVahanPortVO;
import org.epragati.vahan.sync.dao.VahanSyncDAO;
import org.epragati.vahan.sync.mapper.VahanSyncMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Async
public class VahanSyncImpl implements VahanSync{

	private static final Logger logger = LoggerFactory.getLogger(VahanSyncImpl.class);
	
	@Autowired
	RegVahanPortService regVahanPortService;	
	
	@Autowired
	private VahanSyncDAO vahanSyncDAO;
	
	@Autowired
	private VahanSyncMapper vahanSyncMapper;
	
	@Override
	public RegistrationDetailsDTO commonVahansync(RegistrationDetailsDTO registrationDetailsDTO) {
		try {
			if (registrationDetailsDTO != null) {
				long startTime= System.currentTimeMillis();
				Pair<RegVahanPortVO, Boolean> result = regVahanPortService
						.setRegVahanSyncDetails(registrationDetailsDTO);
				if (result!=null && result.getSecond()!=null && result.getSecond().equals(Boolean.TRUE)) {
					registrationDetailsDTO.setIsvahanSync(true);
					vahanSyncDAO.save(vahanSyncMapper.convertVO(result.getFirst()));
				}
				if (result!=null && result.getSecond()!=null && result.getSecond().equals(Boolean.FALSE)) {
					registrationDetailsDTO.setIsvahanSyncSkip(true);
				}
				logger.info("Total execution time for commonVahansync is {}ms",(System.currentTimeMillis()-startTime));
				return registrationDetailsDTO;
			}
		} catch (Exception e) {
			logger.error("Exception Occured while vahansync convertion: {} ", e.getMessage());
		} catch (Throwable e) {
			logger.error("Exception Occured while vahansync convertion: {} ", e.getMessage());
		}
		return registrationDetailsDTO;
	}

}
