package org.epragati.master.service.impl;

import java.util.List;
import java.util.Optional;

import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.DealerCovDAO;
import org.epragati.master.dto.DealerCovDTO;
import org.epragati.master.mappers.DealerCovMapper;
import org.epragati.master.service.DealerCovService;
import org.epragati.master.vo.DealerCovVO;
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
public class DealerCovServiceImpl implements DealerCovService {

	private static final Logger logger = LoggerFactory.getLogger(DealerCovServiceImpl.class);
	@Autowired
	private DealerCovDAO dealerCovDAO;

	@Autowired
	DealerCovMapper dealerCovMapper;

	/**
	 * get all Dealer data
	 */

	@Override
	public List<DealerCovVO> getAllDealerData() {
		logger.debug("entered ");
		List<DealerCovVO> DealerCovVoList = dealerCovMapper.convertEntity(dealerCovDAO.findAll());
		return DealerCovVoList;
	}

	/**
	 * get DealerCov data based on CovId
	 */
	@Override
	public Optional<DealerCovVO> getDealerCovBasedOnCovId(Integer covId) {
		logger.debug("entered {}");
		Optional<DealerCovDTO> dealerCovOptional = dealerCovDAO.findBycovId(covId);
		if (dealerCovOptional.isPresent()) {
			logger.debug("MasterDealerCov data based on covid is available");
			DealerCovVO dealerCovVo = dealerCovMapper.convertEntity(dealerCovOptional.get());
			return Optional.of(dealerCovVo);
		}

		logger.error("no data for  dealer cov based on covId");

		throw new BadRequestException("no data for  dealer cov based on covid");

	}

	/**
	 * get DelaerCov data based on DcId and RId
	 */

	@Override
	public Optional<DealerCovVO> getDealerCovBasedOnDcIdAndRId(Integer dcid, Integer rid) {
		// TODO Auto-generated method stub
		Optional<DealerCovDTO> dealerCovOptional = dealerCovDAO.findBydcIdAndRId(dcid, rid);
		if (dealerCovOptional.isPresent()) {
			logger.debug(" Dealer Cov data based on dcid and rid is available");
			DealerCovVO dealerCovVo = dealerCovMapper.convertEntity(dealerCovOptional.get());
			return Optional.of(dealerCovVo);
		}
		logger.error("No data available for  Dealer Cov  based on dcid and rid ");
		throw new BadRequestException("No data available for  Dealer Cov  based on dcid and rid ");

	}

}
