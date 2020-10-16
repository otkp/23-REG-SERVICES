package org.epragati.master.service.impl;

import java.util.List;

import org.epragati.master.dao.BloodGroupDAO;
import org.epragati.master.mappers.BloodGroupMapper;
import org.epragati.master.service.BloodGroupService;
import org.epragati.master.vo.BloodGroupVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author sairam.cheruku
 *
 */
@Service
public class BloodGroupServiceImpl implements BloodGroupService{
	
	private static final Logger logger = LoggerFactory.getLogger(BloodGroupServiceImpl.class);
	
	@Autowired
	private BloodGroupDAO masterBloodGroupDAO;
	
	@Autowired
	private BloodGroupMapper masterBloodGroupMapper;
	
	@Override
	public List<BloodGroupVO> findAll() {
		logger.debug("");
		return masterBloodGroupMapper.convertEntity(masterBloodGroupDAO.findAll());
	}

	
}
