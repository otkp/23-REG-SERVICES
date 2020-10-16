package org.epragati.master.service.impl;

import java.util.List;

import org.epragati.master.dao.QualificationDAO;
import org.epragati.master.mappers.QualificationMapper;
import org.epragati.master.service.QualificationService;
import org.epragati.master.vo.QualificationVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class QualificationServiceImpl implements QualificationService {

	@Autowired
	private QualificationDAO masterQualificationDAO;

	@Autowired
	private QualificationMapper masterQualificationMappers;

	@Override
	public List<QualificationVO> findAll() {
		return masterQualificationMappers.convertEntity(masterQualificationDAO.findAll());
	}

}
