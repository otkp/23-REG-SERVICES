package org.epragati.master.service.impl;

import java.util.List;

import org.epragati.master.dao.StateDAO;
import org.epragati.master.mappers.StateMapper;
import org.epragati.master.service.StateService;
import org.epragati.master.vo.StateVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author saroj.sahoo
 *
 */
@Service
public class StateServiceImpl implements StateService {
	
	@Autowired
	private StateDAO stateDAO;
	
	@Autowired
	private StateMapper stateMapper;
	
	@Override
	public List<StateVO> findAll() {
		return stateMapper.convertEntity(stateDAO.findAll());
	} 

	@Override
	public List<StateVO> findByCid(String countryId) {
		return stateMapper.convertEntity(stateDAO.findByCountryId(countryId));
	}

	@Override
	public List<StateVO> getStatesForBiLateralTax() {
		return stateMapper.convertEntity(stateDAO.findByAllowBiLateralTaxIsTrue());
	}
}
