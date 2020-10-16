package org.epragati.master.service.impl;

import java.util.List;

import org.epragati.master.dao.PostOfficeDAO;
import org.epragati.master.dto.PostOfficeDTO;
import org.epragati.master.mappers.PostOfficeMapper;
import org.epragati.master.service.PostalService;
import org.epragati.master.vo.PostOfficeVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PostalServiceImpl implements PostalService{

	
@Autowired
private PostOfficeDAO postOfficeDao;

@Autowired
private PostOfficeMapper postOfficeMapper;

	@Override
	public List<PostOfficeVO> findByDidtictId(Integer district) {
		// TODO Auto-generated method stub
		
		return postOfficeMapper.convertEntity(postOfficeDao.findByDistrict(district));
		
	}

}
