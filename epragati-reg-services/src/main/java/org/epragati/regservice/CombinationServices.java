package org.epragati.regservice;

import java.util.List;
import java.util.Optional;

import org.epragati.regservice.vo.ServicesCombinationsVO;

/**
 * 
 * @author krishnarjun.pampana
 *
 */
public interface CombinationServices {

	Optional<ServicesCombinationsVO> getServiceCombinations(Integer id);

	List<ServicesCombinationsVO> getServiceCombinations();

	Optional<ServicesCombinationsVO> getServiceByStatus(boolean status);

	/**
	 * TO get the combination of services based on module code
	 * 
	 * @param string
	 * @return
	 */
	List<ServicesCombinationsVO> findAllServiceCombinations(String string);

	List<ServicesCombinationsVO> findAllServiceCombinationsForMobile();

}
