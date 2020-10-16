package org.epragati.master.service;

import java.util.List;

import org.epragati.master.vo.CountryVO;

/**
 * @author sairam.cheruku
 *
 */
public interface CountrySevice {
	
	/**
	 * 
	 * @return findAll Countries
	 */
	List<CountryVO> findAll();
}

