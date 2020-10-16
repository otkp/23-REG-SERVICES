package org.epragati.master.service.impl;

import java.util.List;

import org.epragati.master.dao.UserRegistrationApplicationsDAO;
import org.epragati.master.mappers.UserRegistrationApplicationsMapper;
import org.epragati.master.service.UserRegistrationApplicationsService;
import org.epragati.master.vo.UserRegistrationApplicationsVO;
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
public class UserRegistrationApplicationsServiceImpl implements UserRegistrationApplicationsService {
	
	private static final Logger logger = LoggerFactory.getLogger(UserRegistrationApplicationsServiceImpl.class);
	
	@Autowired
	private UserRegistrationApplicationsDAO userRegistrationApplicationsDAO;
	
	@Autowired
	private UserRegistrationApplicationsMapper userRegistrationApplicationsMapper;
	
	UserRegistrationApplicationsVO userRegistrationApplicationsVO;
	
	@Override
	public void save(){
		
	}
	
	@Override
	public List<UserRegistrationApplicationsVO> findAllUserRegistrationApplications() {
		List<UserRegistrationApplicationsVO> userRegistrationApplicationsList = userRegistrationApplicationsMapper.convertEntity(userRegistrationApplicationsDAO.findAll());
		return userRegistrationApplicationsList;
	}
	
	@Override
	public UserRegistrationApplicationsVO findUserRegistrationApplicationsByApplicationNo(Integer applicationno) {
		userRegistrationApplicationsVO = userRegistrationApplicationsMapper.convertEntity(userRegistrationApplicationsDAO.findUserRegistrationApplicationsByApplicationNo(applicationno));
		return userRegistrationApplicationsVO; 
	} 
	

}
