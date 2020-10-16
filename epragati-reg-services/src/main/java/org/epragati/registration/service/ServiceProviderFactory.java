package org.epragati.registration.service;

import java.util.Optional;

import org.epragati.exception.BadRequestException;
import org.epragati.master.service.PrSeriesService;
import org.epragati.sn.service.NumberSeriesService;
import org.epragati.sn.service.impl.NumberSeriesSateLevelServiceImpl;
import org.epragati.sn.service.impl.NumberSeriesServiceImpl;
import org.epragati.sn.vo.BidConfigMasterVO;
import org.epragati.util.NumberPoolStatus.NumberConfigLevel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class ServiceProviderFactory {

	@Autowired
	@Qualifier("officeLevel")
	private  NumberSeriesServiceImpl officeLevelServiceImpl;

	@Autowired
	@Qualifier("stateLevel")
	private  NumberSeriesSateLevelServiceImpl stateLevelServiceImpl;
	
	@Autowired
	private PrSeriesService prSeriesService;	

	private BidConfigMasterVO getPrimeNumbers() {
		
		Optional<BidConfigMasterVO> resultOptional = prSeriesService.getBidConfigMasterData(false);
		if (!resultOptional.isPresent()) {
			 throw new BadRequestException("Bid mater config data not found");
		}
		return resultOptional.get();
	}

	public  NumberSeriesService getNumberSeriesServiceInstent() {
		
		BidConfigMasterVO bidConfigMasterVO=getPrimeNumbers();
		if(NumberConfigLevel.STATE.getLabel().equals(bidConfigMasterVO.getNumberGenerationType())) {
			return stateLevelServiceImpl;
		}else {
			return officeLevelServiceImpl;
		}
	}


	


}
