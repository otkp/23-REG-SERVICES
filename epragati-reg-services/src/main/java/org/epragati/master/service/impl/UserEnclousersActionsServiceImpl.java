package org.epragati.master.service.impl;


import java.util.List;
import java.util.Optional;

import org.epragati.constants.MessageKeys;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.UserEnclousersActionsDAO;
import org.epragati.master.mappers.UserEnclousersActionsMapper;
import org.epragati.master.service.UserEnclousersActionsService;
import org.epragati.master.vo.UserEnclousersActionsVO;
import org.epragati.util.AppMessages;
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
public class UserEnclousersActionsServiceImpl implements UserEnclousersActionsService {
	
	private static final Logger logger = LoggerFactory.getLogger(UserEnclousersActionsServiceImpl.class);
	
	@Autowired
	private AppMessages appMessages;
	
	@Autowired 
	private UserEnclousersActionsDAO userEnclousersActionsDAO;
	
	@Autowired
	private UserEnclousersActionsMapper userEnclousersActionsMapper;
	
	//UserEnclousersActionsVO userEnclousersActionsVO;
	
	@Override
	public Integer save(Optional<UserEnclousersActionsVO> userEnclousersActionsVO){
		logger.info(appMessages.getLogMessage(MessageKeys.USERENCLOSURE_SAVE_ENTRY));
		UserEnclousersActionsVO userEnclousersActions = null;
		if(userEnclousersActionsVO.isPresent()){
			userEnclousersActions = userEnclousersActionsVO.get();
			userEnclousersActions.setUeaId(80004);
			userEnclousersActions.setUeId("ABC");
			userEnclousersActions.setAction(10);
			userEnclousersActions.setActionBy("Saroj");
			userEnclousersActions.setActionDate("2017-12-11");
			userEnclousersActions.setRole(11);
			userEnclousersActions.setRemarks("Good");
			userEnclousersActionsDAO.save(userEnclousersActionsMapper.convertVO(Optional.of(userEnclousersActions)));
		}else {
			logger.error("UserEnclousersActions insertion Failed");
			throw new BadRequestException("UserEnclousersActions insertion Failed");
		}
		logger.info(appMessages.getLogMessage(MessageKeys.USERENCLOSURE_SAVE_EXIT));
		return userEnclousersActions.getRole();
	}
	
	@Override
	public List<UserEnclousersActionsVO> findAllUserEnclousersActions() {
		logger.info(appMessages.getLogMessage(MessageKeys.USERENCLOSUREACTIONS_SAVE_ENTRY));
		List<UserEnclousersActionsVO> userEnclousersActionsList = userEnclousersActionsMapper.convertEntity(userEnclousersActionsDAO.findAll());
		logger.info(appMessages.getLogMessage(MessageKeys.USERENCLOSUREACTIONS_SAVE_EXIT));
		return userEnclousersActionsList;
	}
	
	
	@Override
	public Optional<UserEnclousersActionsVO> findUserEnclousersActionsByRole(Integer role) {
		Optional<UserEnclousersActionsVO> userEnclousersActionsVO = userEnclousersActionsMapper.convertEntity(userEnclousersActionsDAO.findUserEnclousersActionsByRole(role));
		return userEnclousersActionsVO;
	} 

}
