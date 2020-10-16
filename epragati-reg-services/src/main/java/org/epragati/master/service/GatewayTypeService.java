package org.epragati.master.service;

import java.util.List;

import org.epragati.master.vo.GatewayTypeVO;

/**
 * @author sairam.cheruku
 *
 */
public interface GatewayTypeService {

	/**
	 * 
	 * @return findAll gateWayTypes
	 */
	List<GatewayTypeVO> findAll();

}
