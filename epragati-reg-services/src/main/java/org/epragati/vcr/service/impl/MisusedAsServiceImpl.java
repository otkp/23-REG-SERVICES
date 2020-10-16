package org.epragati.vcr.service.impl;

import java.util.List;

import org.epragati.master.dao.MisusedAsDAO;
import org.epragati.master.mappers.MisusedAsMapper;
import org.epragati.master.vo.MisusedAsVO;
import org.epragati.vcr.service.MisusedAsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MisusedAsServiceImpl implements MisusedAsService {

	@Autowired
	private MisusedAsDAO dao;
	@Autowired
	private MisusedAsMapper mapper;
	@Autowired
	private static final Logger logger = LoggerFactory.getLogger(MisusedAsServiceImpl.class);

	@Override
	public void save(MisusedAsVO vo) throws Exception {
		logger.warn("Data saving");
		dao.save(mapper.convertVO(vo));

	}

	@Override
	public List<MisusedAsVO> findListOfMisusedAs() {
		logger.warn("fetching details");
		
		return mapper.convertEntity(dao.findAll());
	}

}
