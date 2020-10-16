package org.epragati.vcr.service.impl;

import java.util.List;

import org.epragati.master.dao.OffenceCategoryDAO;
import org.epragati.master.dto.OffenceCategory;
import org.epragati.master.mappers.OffenceCategoryMapper;
import org.epragati.master.vo.OffenceCategoryVO;
import org.epragati.vcr.service.OffenceCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OffenceCategoryServiceImpl implements OffenceCategoryService {
	@Autowired
	private OffenceCategoryMapper mapper;
	@Autowired
	private OffenceCategoryDAO dao;
	private static final Logger logger = LoggerFactory.getLogger(OffenceCategoryServiceImpl.class);

	@Override
	public void saveCategeory(OffenceCategoryVO vo) throws Exception {
		if (dao.findByOffenceCategeory(vo.getOffenceCategeory()).isPresent())
			throw new Exception("offence category is already available in our DB.");
		logger.warn("DATA SAVED [{}]", vo);
		dao.save(mapper.convertVO(vo));

	}

	@Override
	public List<OffenceCategoryVO> findeOffenceCategeory() {
		List<OffenceCategory> offenceType = dao.findAll();
		return mapper.convertEntity(offenceType);
	}

}
