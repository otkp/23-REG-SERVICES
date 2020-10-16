package org.epragati.master.service.impl;

import java.util.List;

import org.epragati.master.dao.GatewayTypeDAO;
import org.epragati.master.mappers.GatewayTypeMapper;
import org.epragati.master.service.GatewayTypeService;
import org.epragati.master.vo.GatewayTypeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author sairam.cheruku
 *
 */
@Service
public class GatewayTypeServiceImpl implements GatewayTypeService {

	@Autowired
	private GatewayTypeDAO gatewayTypeDAO;

	@Autowired
	private GatewayTypeMapper gatewayTypeMapper;

	@Override
	public List<GatewayTypeVO> findAll() {
		return gatewayTypeMapper.convertEntity(gatewayTypeDAO.findAll());
	}

}
