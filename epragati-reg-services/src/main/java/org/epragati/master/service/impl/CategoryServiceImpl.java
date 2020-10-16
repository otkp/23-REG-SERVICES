package org.epragati.master.service.impl;

import java.util.List;

import org.epragati.master.dao.CategoryDAO;
import org.epragati.master.mappers.CategoryMapper;
import org.epragati.master.service.CategoryService;
import org.epragati.master.vo.CategoryVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author sairam.cheruku
 *
 */
@Service
public class CategoryServiceImpl implements CategoryService{
	
	@Autowired
	private CategoryDAO masterCategoryDAO; 
	
	@Autowired
	private CategoryMapper masterCategoryMapper;
	
	/**
	 * 
	 */
	@Override
	public List<CategoryVO> findAll() {
		return masterCategoryMapper.convertEntity(masterCategoryDAO.findAll());
	}

	/**
	 * 
	 */
	@Override
	public List<CategoryVO> findVehicleDetails() {
		masterCategoryMapper.convertEntity(masterCategoryDAO.findAll());
		return null;
	}

}
