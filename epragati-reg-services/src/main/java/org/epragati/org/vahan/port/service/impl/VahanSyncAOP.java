package org.epragati.org.vahan.port.service.impl;

import java.util.List;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.epragati.vahan.sync.dao.VahanSyncDAO;
import org.epragati.vahan.sync.dto.RegVahanPortDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class VahanSyncAOP {

	private static final Logger logger = LoggerFactory.getLogger(VahanSyncAOP.class);
	
	@Autowired
	private VahanSyncDAO vahanSyncDAO;
	
	/**
	 * remove old success vahansync records 
	 *  
	 * */
	
	
	@Before("execution(public * org.epragati.vahan.sync.dao.VahanSyncDAO.save(*))")
	public void  removeOldSyncData(JoinPoint jp) {
		Object[] obj = jp.getArgs();
		try {
			if(obj!=null) {
				Object vahanSyncObj = obj[0];
			if (vahanSyncObj!=null &&vahanSyncObj instanceof RegVahanPortDTO) {
				RegVahanPortDTO vahanSyncDto = (RegVahanPortDTO) vahanSyncObj;
				List<RegVahanPortDTO> vahanSyncList = vahanSyncDAO
						.findByIsvahanSyncTrueAndIsErroFoundFalseAndIsPartiallyFalseAndPrNo(vahanSyncDto.getPrNo());
				if(!vahanSyncList.isEmpty()) {
					vahanSyncDAO.delete(vahanSyncList);
				}
				}
			}
		} catch (Throwable e) {
			logger.error("Exception occured while fetching vahanSync AOP : {}", e);
		}
	}
	
}
