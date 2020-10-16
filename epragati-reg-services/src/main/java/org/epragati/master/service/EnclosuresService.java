package org.epragati.master.service;

import java.util.List;
import java.util.Optional;

import org.epragati.images.vo.InputVO;
import org.epragati.master.vo.EnclosuresVO;

/**
 * 
 * @author saroj.sahoo
 *
 */
public interface EnclosuresService {
	
	/**
	 * Save UserEnclosure
	 * @return 
	 */
	public Integer save(Optional<EnclosuresVO> userEnclosureVO);
	/**
	 * Find All UserEnclosure
	 * @return 
	 */
	public List<EnclosuresVO> findAllUserEnclosure();
	/**
	 * Find UserEnclosure by enId
	 * @param enId
	 * @return 
	 */
	public Optional<EnclosuresVO> findUserEnclosureBasedOnenId(Integer enclosureId);
	/**
	 * 
	 * @param ServiceId
	 * @return
	 */
	Optional<InputVO> findBodyBuilderSupportedEnclosures(Integer ServiceId,String cov);
	
	
}
