package org.epragati.master.service.impl;

import java.util.List;

import org.epragati.master.dao.CountryDAO;
import org.epragati.master.mappers.CountryMapper;
import org.epragati.master.service.CountrySevice;
import org.epragati.master.vo.CountryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author sairam.cheruku
 *
 */

@Service
public class CountrySeviceImpl implements CountrySevice{
	
	@Autowired
	private CountryDAO masterCountryDAO;
	
	@Autowired
	private CountryMapper masterCountryMapper;

	@Override
	public List<CountryVO> findAll() {
		return masterCountryMapper.convertEntity(masterCountryDAO.findAll());
	} 
	
}
