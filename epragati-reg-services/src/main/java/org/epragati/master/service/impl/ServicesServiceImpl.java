package org.epragati.master.service.impl;

import java.util.List;

import org.epragati.master.dao.ServicesDAO;
import org.epragati.master.mappers.ServicesMapper;
import org.epragati.master.service.ServicesService;
import org.epragati.master.vo.ServicesVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author sairam.cheruku
 *
 */

@Service
public class ServicesServiceImpl implements ServicesService{

	@Autowired
	private ServicesDAO servicesDAO;
	
	@Autowired
	private ServicesMapper servicesMapper;
	
	@Override
	public List<ServicesVO> findAll() {
		return servicesMapper.convertEntity(servicesDAO.findAll());
	}

}
