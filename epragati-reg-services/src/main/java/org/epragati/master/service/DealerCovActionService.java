package org.epragati.master.service;

import java.util.List;
import java.util.Optional;

import org.epragati.master.vo.DealerCovActionVO;

/**
 * 
 * @author saikiran.kola
 *
 */
public interface DealerCovActionService {

	/**
	 * get all DealerCovAction details
	 * 
	 * @return
	 */

	public List<DealerCovActionVO> getDealerCovActionDetails();

	/**
	 * Find DealersCovAction Based On actionby
	 * 
	 * @param mandalId
	 */

	public DealerCovActionVO getDealerCovActionBasedOnActionBy(String actionby);

	/**
	 * find DealerCovAction based on Role
	 * 
	 * @param role
	 * @return
	 */

	public Optional<DealerCovActionVO> findDealerBasedOnRole(Integer role);

	/**
	 * find Dealer Based on role and action
	 */

	public Optional<DealerCovActionVO> findDealerBasedOnRoleAndAction(Integer role, Integer action);
}
