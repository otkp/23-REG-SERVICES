package org.epragati.regservice.impl;

import java.util.List;
import java.util.Optional;

import org.epragati.regservice.CombinationServices;
import org.epragati.regservice.dao.CombinationServicesDAO;
import org.epragati.regservice.dto.CombinationServicesDTO;
import org.epragati.regservice.mapper.ServicesCombinationsMapper;
import org.epragati.regservice.vo.ServicesCombinationsVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author krishnarjun.pampana
 *
 */

@Service
public class CombinationServicesImpl implements CombinationServices {

	private static final Logger logger = LoggerFactory.getLogger(CombinationServicesImpl.class);
	
	@Autowired
	private CombinationServicesDAO servicesCombinationsDAO;

	@Autowired
	private ServicesCombinationsMapper servicesCombinationsMapper;

	@Override
	public Optional<ServicesCombinationsVO> getServiceCombinations(Integer id) {
		Optional<CombinationServicesDTO> combinationService = servicesCombinationsDAO.findByServiceId(id);
		logger.info("combination service dto",combinationService);
		// CombinationServicesDTO combinationService1 =
		// servicesCombinationsDAO.findByServiceId(id);
		/*
		 * if (combinationService.isPresent()) {
		 * Optional<ServicesCombinationsVO> OptionalServicesVO =
		 * servicesCombinationsMapper .convertEntity(combinationService); return
		 * OptionalServicesVO; }
		 */
		return Optional.empty();
	}

	@Override
	public List<ServicesCombinationsVO> getServiceCombinations() {

		return servicesCombinationsMapper.convertEntity(servicesCombinationsDAO.findByStatusTrueOrderByOrderNo());
	}

	@Override
	public Optional<ServicesCombinationsVO> getServiceByStatus(boolean status) {
		status = true;
		Optional<ServicesCombinationsVO> vo = servicesCombinationsMapper
				.convertEntity(servicesCombinationsDAO.findByStatus(status));
		return vo;
	}

	/**
	 * TO get the combinations from "master_reg_combinations" collections with the module code  
	 */
	@Override
	public List<ServicesCombinationsVO> findAllServiceCombinations(String module) {
		// TODO Auto-generated method stub
		List<ServicesCombinationsVO> list = null;
		List<CombinationServicesDTO> combinationDTOList = servicesCombinationsDAO.findByModuleOrderByServiceIdAsc(module);
		list = servicesCombinationsMapper.convertEntity(combinationDTOList);
		return list;
	}

	@Override
	public List<ServicesCombinationsVO> findAllServiceCombinationsForMobile() {
		List<ServicesCombinationsVO> list = null;
		List<CombinationServicesDTO> combinationDTOList = servicesCombinationsDAO.findByIsMobileTrue();
		list = servicesCombinationsMapper.convertEntity(combinationDTOList);
		return list;
	}

}
