package org.epragati.master.service.impl;

import java.util.List;
import java.util.Optional;

import org.epragati.master.dao.DealerMakerDAO;
import org.epragati.master.dto.DealerMakerDTO;
import org.epragati.master.mappers.DealerMakerMapper;
import org.epragati.master.service.DealerMakerService;
import org.epragati.master.vo.DealerMakerVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 *
 * @author saikiran.kola
 *
 */
@Service
public class DealerMakerServiceImpl implements DealerMakerService {

	private static final Logger logger = LoggerFactory.getLogger(DealerMakerServiceImpl.class);

	@Autowired
	private DealerMakerDAO dealerMakerDAO;

	@Autowired
	private DealerMakerMapper dealerMakerMapper;

	/**
	 * find Delears data based on MandalId
	 */

	@Override
	public Optional<DealerMakerVO> findDealerByMandalId(String mandalid) {
		Optional<DealerMakerDTO> dealerOptional = dealerMakerDAO.findBymmId(mandalid);
		if (dealerOptional.isPresent()) {
			DealerMakerVO dealerMakerVo = dealerMakerMapper.convertEntity(dealerOptional.get());
			return Optional.of(dealerMakerVo);
		}
		return Optional.empty();
		// return Optional.empty();
	}

	/**
	 * get all DealersMaker data
	 */
	@Override
	public List<DealerMakerVO> getDealersMakerDetails() {
		// TODO Auto-generated method stub
		List<DealerMakerVO> dealerMakerList = dealerMakerMapper.convertEntity(dealerMakerDAO.findAll());
		return dealerMakerList;
	}
	
	@Override
	public Optional<DealerMakerVO> findDealerMakerByrId(Integer rId) {
		Optional<DealerMakerDTO> dealerMakerDTO = dealerMakerDAO.findByRId(rId);
		if (dealerMakerDTO.isPresent()) {
			DealerMakerDTO dealerDTO = dealerMakerDTO.get();
			DealerMakerVO dealerVO = dealerMakerMapper.convertEntity(dealerDTO);
			Optional.of(dealerVO);
		}
		return Optional.empty();
	}
	
	
	
	
	
}
