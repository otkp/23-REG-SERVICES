package org.epragati.master.service;

import java.util.List;

import org.epragati.master.vo.CategoryVO;

/**
 * @author sairam.cheruku
 *
 */
public interface CategoryService {
	
	/**
	 * 
	 * @return findAll Categories
	 */
	List<CategoryVO> findAll();

	/**
	 * 
	 * @return findVehicleDetails
	 */
	List<CategoryVO> findVehicleDetails();

}
