package org.epragati.master.service;

import java.util.List;

import org.epragati.master.vo.OwnershipVO;
import org.springframework.stereotype.Service;

/**
 * @author sairam.cheruku
 *
 */
@Service
public interface OwnershipService {
	
	/**
	 * 
	 * @return findAllOwnerships
	 */
	List<OwnershipVO> findAll();

}
