package org.epragati.master.service.impl;

import java.util.List;

import org.epragati.master.dao.OwnershipDAO;
import org.epragati.master.mappers.OwnershipMapper;
import org.epragati.master.service.OwnershipService;
import org.epragati.master.vo.OwnershipVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OwnershipServiceImpl implements OwnershipService {

	@Autowired
	private OwnershipDAO ownershipDAO;

	@Autowired
	private OwnershipMapper ownershipMapper;

	@Override
	public List<OwnershipVO> findAll() {
		return ownershipMapper.convertEntity(ownershipDAO.findAll());
	}

}
