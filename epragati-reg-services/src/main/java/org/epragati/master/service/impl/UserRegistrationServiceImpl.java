package org.epragati.master.service.impl;

import java.util.List;

import org.epragati.master.dao.UserRegistrationDAO;
import org.epragati.master.mappers.UserRegistrationMapper;
import org.epragati.master.service.UserRegistrationService;
import org.epragati.master.vo.UserRegistrationVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author saroj.sahoo
 *
 */
@Service
public class UserRegistrationServiceImpl implements UserRegistrationService {
	
	private static final Logger logger = LoggerFactory.getLogger(UserRegistrationServiceImpl.class);
	
	@Autowired
	private UserRegistrationDAO userRegistrationDAO;
	
	@Autowired
	private UserRegistrationMapper userRegistrationMapper;
	
	UserRegistrationVO userRegistrationVO;
	
	@Override
	public void save(){
		
	}
	
	@Override
	public List<UserRegistrationVO> findAllUserRegistration() {
		List<UserRegistrationVO> userRegistrationList = userRegistrationMapper.convertEntity(userRegistrationDAO.findAll());
		return userRegistrationList;
	}
	
	@Override
	public UserRegistrationVO findUserRegistrationByMandal(Integer mandal) {
		userRegistrationVO = userRegistrationMapper.convertEntity(userRegistrationDAO.findUserRegistrationByMandal(mandal));
		return userRegistrationVO; 
	} 

}
