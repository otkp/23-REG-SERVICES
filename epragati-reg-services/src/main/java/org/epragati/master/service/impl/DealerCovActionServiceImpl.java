package org.epragati.master.service.impl;

import java.util.List;
import java.util.Optional;

import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.DealerCovActionDAO;
import org.epragati.master.dto.DealerCovActionDTO;
import org.epragati.master.mappers.DealerCovActionMapper;
import org.epragati.master.service.DealerCovActionService;
import org.epragati.master.vo.DealerCovActionVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import ch.qos.logback.classic.Logger;

/**
 * 
 * @author saikiran.kola
 *
 */
@Service
public class DealerCovActionServiceImpl implements DealerCovActionService {

	@Autowired
	private DealerCovActionDAO dealerCovActionDAO;

	@Autowired
	private DealerCovActionMapper dealerCovActionMapper;

	/**
	 * get all DealersCovAction data
	 */

	@Override
	public List<DealerCovActionVO> getDealerCovActionDetails() {
		List<DealerCovActionVO> dealerCovActionList = dealerCovActionMapper.convertEntity(dealerCovActionDAO.findAll());
		return dealerCovActionList;
	}

	/**
	 * get DealerCovAction data based on ActionBy
	 */

	@Override
	public DealerCovActionVO getDealerCovActionBasedOnActionBy(String actionby) {
		DealerCovActionVO dealercovactionvo = dealerCovActionMapper
				.convertEntity(dealerCovActionDAO.findByactionBy(actionby));
		return dealercovactionvo;
	}

	/**
	 * find Dealer data based on Role
	 */

	@Override
	public Optional<DealerCovActionVO> findDealerBasedOnRole(Integer role) {

		Optional<DealerCovActionDTO> dealerCovActionOptional = dealerCovActionDAO.findByrole(role);
		if (dealerCovActionOptional.isPresent()) {
			DealerCovActionVO dealercovactionvo = dealerCovActionMapper.convertEntity(dealerCovActionOptional.get());
			return Optional.of(dealercovactionvo);
		}
		
		throw new BadRequestException("No data is available for Dealer based on Role");
	}

	/**
	 * find Dealer data based on Role and Action
	 */

	@Override
	public Optional<DealerCovActionVO> findDealerBasedOnRoleAndAction(Integer role, Integer action) {

		Optional<DealerCovActionDTO> dealerCovActionDTOOptional = dealerCovActionDAO.findByRoleAndAction(role, action);

		if (dealerCovActionDTOOptional.isPresent()) {
			DealerCovActionVO dealercovactionvo = dealerCovActionMapper.convertEntity(dealerCovActionDTOOptional.get());
			return Optional.of(dealercovactionvo);
		}
		
		throw new BadRequestException("No data for DealerCovAction based on Role and Action");
	}
}