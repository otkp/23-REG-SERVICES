package org.epragati.master.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.epragati.constants.MessageKeys;
import org.epragati.exception.BadRequestException;
import org.epragati.images.vo.InputVO;
import org.epragati.master.dao.EnclosuresDAO;
import org.epragati.master.dto.EnclosuresDTO;
import org.epragati.master.mappers.EnclosuresMapper;
import org.epragati.master.service.EnclosuresService;
import org.epragati.master.vo.EnclosuresVO;
import org.epragati.util.AppMessages;
import org.epragati.util.payment.ClassOfVehicleEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EnclosuresServiceImpl implements EnclosuresService {

	private static final Logger logger = LoggerFactory.getLogger(EnclosuresServiceImpl.class);

	@Autowired
	private AppMessages appMessages;

	@Autowired
	private EnclosuresDAO userEnclosureDAO;

	@Autowired
	private EnclosuresMapper userEnclosureMapper;

	/**
	 * 
	 * Save UserEnclosure Details
	 * 
	 */
	@Override
	public Integer save(Optional<EnclosuresVO> userEnclosureVO) {
		logger.info(appMessages.getLogMessage(MessageKeys.USERENCLOSURE_SAVE_ENTRY), new Date());
		EnclosuresVO userEnclosure = null;
		if (userEnclosureVO.isPresent()) {
			Optional<EnclosuresDTO> dto = userEnclosureMapper.convertVO(Optional.of(userEnclosure));
			if (dto.isPresent()) {
				userEnclosureDAO.save(dto.get());
				logger.info(appMessages.getLogMessage(MessageKeys.USERENCLOSURE_SAVE_EXIT));
				return dto.get().getEnclosureId();
			}
		} else {
			throw new BadRequestException("UserEnclosure insertion Failed");
		}
		logger.info(appMessages.getLogMessage(MessageKeys.USERENCLOSURE_SAVE_EXIT));
		return null;

	}

	/**
	 * 
	 * Finding UserEnclosure Details
	 * 
	 */
	@Override
	public List<EnclosuresVO> findAllUserEnclosure() {
		logger.debug(appMessages.getLogMessage(MessageKeys.USERENCLOSURE_FINDALLRECORD_ENTRY), new Date());
		List<EnclosuresVO> userEnclosureList = userEnclosureMapper.convertEntity(userEnclosureDAO.findAll());
		logger.debug(appMessages.getLogMessage(MessageKeys.USERENCLOSURE_FINDALLRECORD_EXIT), new Date());
		logger.debug("Found : {}", userEnclosureList.size());
		return userEnclosureList;
	}

	/**
	 * 
	 * Find UserEnclosure details by enId
	 * 
	 */
	@Override
	public Optional<EnclosuresVO> findUserEnclosureBasedOnenId(Integer enclosureId) {
		logger.info(appMessages.getLogMessage(MessageKeys.USERENCLOSURE_FINDBYID_ENTRY), enclosureId);
		Optional<EnclosuresVO> userEnclosureVO = userEnclosureMapper
				.convertEntity(userEnclosureDAO.findByenclosureId(enclosureId));
		logger.info(appMessages.getLogMessage(MessageKeys.USERENCLOSURE_FINDBYID_EXIT), enclosureId);
		return userEnclosureVO;
	}

	@Override
	public Optional<InputVO> findBodyBuilderSupportedEnclosures(Integer serviceId, String cov) {
		List<EnclosuresDTO> enclosureList;
		if (cov.equals(ClassOfVehicleEnum.ARVT.getCovCode())) {
			enclosureList = userEnclosureDAO.findByclassOfVehicleAndEnclosureIdIn(cov, slNos(cov));
			enclosureList.sort((p1, p2) -> p1.getEnclosureId().compareTo(p2.getEnclosureId()));
		} else if(cov.equals(ClassOfVehicleEnum.IVCN.getCovCode())||
				cov.equals(ClassOfVehicleEnum.TTTT.getCovCode())||cov.equals(ClassOfVehicleEnum.TTRN.getCovCode())){
			enclosureList = userEnclosureDAO.findByclassOfVehicle(cov);
			enclosureList.sort((p1, p2) -> p1.getEnclosureId().compareTo(p2.getEnclosureId()));
			
		}else {
			enclosureList = userEnclosureDAO.findByServiceIDAndSlNoIn(serviceId, slNos(cov));
			enclosureList.sort((p1, p2) -> p1.getSlNo().compareTo(p2.getSlNo()));
		}
		if (enclosureList.isEmpty()) {
			logger.error("No enclosures found based on given Service Id [{}]",serviceId);
			throw new BadRequestException("No enclosures found based on given Service Id");
		}

		InputVO inputVO = userEnclosureMapper.convertEnclosureDTOtoInputVO(cov, enclosureList);
		return Optional.of(inputVO);
	}

	public List<Integer> slNos(String cov) {
		if (cov.equals(ClassOfVehicleEnum.ARVT.getCovCode())) {
			Integer[] arvtSlNos = { 0, 1, 2, 3, 4 ,5};
			List<Integer> slNos = Arrays.asList(arvtSlNos);
			return slNos;
		} else {
			Integer[] arvtSlNos = { 0, 1, 2, 3 };
			List<Integer> slNos = Arrays.asList(arvtSlNos);
			return slNos;
		}
	}
}