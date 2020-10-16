package org.epragati.master.service.impl;

import java.util.List;

import org.epragati.master.dao.RolesDAO;
import org.epragati.master.mappers.RolesMapper;
import org.epragati.master.service.RolesService;
import org.epragati.master.vo.RolesVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author saikiran.kola
 *
 */
@Service
public class RolesServiceImpl implements RolesService {
	@Autowired
	RolesDAO rolesDAO;
	@Autowired
	RolesMapper rolesMapper;

	/**
	 * get all Roles data
	 * 
	 */

	@Override
	public List<RolesVO> getRolesDetails() {

		List<RolesVO> masterRolesVoList = rolesMapper.convertEntity(rolesDAO.findAll());
		// TODO Auto-generated method stub
		return masterRolesVoList;
	}

}
