package org.epragati.master.service.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.epragati.master.dao.PostOfficeDAO;
import org.epragati.master.dao.VillageDAO;
import org.epragati.master.mappers.PostOfficeMapper;
import org.epragati.master.mappers.VillageMapper;
import org.epragati.master.service.VillageService;
import org.epragati.master.vo.MandalVO;
import org.epragati.master.vo.PostOfficeVO;
import org.epragati.master.vo.VillagePostalOfficeVO;
import org.epragati.master.vo.VillageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author saroj.sahoo
 *
 */
@Service
public class VillageServiceImpl implements VillageService {

	@Autowired
	private VillageDAO villageDAO;
	
	@Autowired
	private PostOfficeDAO postOfficeDAO;

	@Autowired
	private VillageMapper villageMapper;
	
	@Autowired
	private PostOfficeMapper postOfficeMapper;

	@Override
	public List<VillageVO> findAll() {
		return villageMapper.convertEntity(villageDAO.findAll());
	}

	@Override
	public VillagePostalOfficeVO findByMid(Integer mandalId,Integer districtId) {
		
		VillagePostalOfficeVO villagePostalOfficeVO = new VillagePostalOfficeVO();
		
		List<VillageVO> villageVO = villageMapper.convertEntity(villageDAO.findByMandalId(mandalId));
		
		Collections.sort(villageVO, new Comparator<VillageVO>(){
			  public int compare(VillageVO p1, VillageVO p2){
			    return p1.getVillageName().compareTo(p2.getVillageName());
			  }
		});

		villagePostalOfficeVO.setVillageVO(villageVO);
		
		List<PostOfficeVO> postOfficeList = postOfficeMapper.convertEntity(postOfficeDAO.findByDistrict(districtId));
		
		Collections.sort(postOfficeList, new Comparator<PostOfficeVO>(){
			  public int compare(PostOfficeVO p1, PostOfficeVO p2){
			    return p1.getPostOfficeCode().compareTo(p2.getPostOfficeCode());
			  }
		});

		villagePostalOfficeVO.setPostOfficeVO(postOfficeList); 
		
		return villagePostalOfficeVO;
	}
	
	
	@Override
	public List<VillageVO> findByMandalId(Integer mandalId) {
		
		
		return villageMapper.convertEntity(villageDAO.findByMandalIdAndStatusIgnoreCase(mandalId, "TRUE"));
	}

}
