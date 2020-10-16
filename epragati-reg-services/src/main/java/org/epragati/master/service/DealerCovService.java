package org.epragati.master.service;

import java.util.List;
import java.util.Optional;

import org.epragati.master.vo.DealerCovVO;

/**
 * 
 * @author saikiran.kola
 *
 */
public interface DealerCovService {

	/**
	 * get all DealerCov data
	 * 
	 * @return
	 */

	public List<DealerCovVO> getAllDealerData();

	/**
	 * find DealerCov data based on covId
	 * 
	 * @param covId
	 * @return
	 */

	public Optional<DealerCovVO> getDealerCovBasedOnCovId(Integer covId);

	/**
	 * get DealerCov based on DcId and RId
	 * 
	 * @param dcid
	 * @param rid
	 * @return
	 */

	public Optional<DealerCovVO> getDealerCovBasedOnDcIdAndRId(Integer dcid, Integer rid);
}
