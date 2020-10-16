package org.epragati.master.service;

import java.util.List;
import java.util.Optional;

import org.epragati.master.vo.DealerMakerVO;

/**
 * 
 * @author saikiran.kola
 *
 */
public interface DealerMakerService {

	/**
	 * Find All Dealers
	 */
	public List<DealerMakerVO> getDealersMakerDetails();

	/**
	 * Find MasterDealerCovAction Based On actionby
	 * 
	 * @param mandalId
	 */
	public Optional<DealerMakerVO> findDealerByMandalId(String mandalid);

	
	public Optional<DealerMakerVO> findDealerMakerByrId(Integer rId) ;
}
