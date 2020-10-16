package org.epragati.master.service.impl;

import java.util.List;

import org.epragati.master.dao.InsuranceTypeDAO;
import org.epragati.master.mappers.InsuranceTypeMapper;
import org.epragati.master.service.InsuranceTypeService;
import org.epragati.master.vo.InsuranceTypeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author saroj.sahoo
 *
 */
@Service
public class InsuranceTypeServiceImpl implements InsuranceTypeService {
	
	@Autowired
	private InsuranceTypeDAO insuranceTypeDAO;
	
	@Autowired
	private InsuranceTypeMapper insuranceTypeMapper;

	@Override
	public List<InsuranceTypeVO> findAll() {
		return insuranceTypeMapper.convertEntity(insuranceTypeDAO.findByStatusTrue());
	}
}
