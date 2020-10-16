package org.epragati.vcr.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.epragati.cache.CacheData;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.OffenceCategoryDAO;
import org.epragati.master.dao.OffenceDAO;
import org.epragati.master.dto.OffenceDTO;
import org.epragati.master.mappers.OffenceCategoryMapper;
import org.epragati.master.mappers.OffenceMapper;
import org.epragati.master.vo.OffenceCategoryVO;
import org.epragati.master.vo.OffenceVO;
import org.epragati.vcr.service.OffenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OffenceServiceImpl implements OffenceService {
	@Autowired
	private OffenceMapper mapper;
	@Autowired
	private OffenceDAO offenceDAO;
	@Autowired
	private OffenceCategoryMapper offenceCategeoryMapper;
	@Autowired
	private OffenceCategoryDAO offenceCategeoryDao;

	private static final String OFFENCE_CATEGEORY = "OFFENCE CATEGORY NOT FOUND";
	private static final Logger logger = LoggerFactory.getLogger(OffenceServiceImpl.class);

	@Override
	public void save(List<OffenceVO> vo) throws Exception {

		offenceDAO.save(mapper.convertVO(vo));

	}

	@Override
	public List<OffenceVO> findOffenceServiceByCategeory(OffenceCategoryVO categeroy) {
		List<OffenceDTO> offence = null; 
		return mapper.convertEntity(offence);
	}

	@Override
	public List<OffenceVO> findAllOffence() {
		List<OffenceVO> offenceVOList = new ArrayList<>();
		//putting total Offences List into cache , because we have heavy list
		if(CacheData.getFromCache("totalOffenceList") ==null)
		{
			List<OffenceDTO> findeListOfOffenceCategeory = offenceDAO.findAll();
			offenceVOList = mapper.convertEntity(findeListOfOffenceCategeory);
			CacheData.storeIntoCache("totalOffenceList", offenceVOList);
		}else {
			offenceVOList = (List<OffenceVO>) CacheData.getFromCache("totalOffenceList");
		}		
	
		return offenceVOList;
	}

	@Override
	public List<OffenceVO> findeListOfOffencesBasedOnCOVandCategory(String classOfVehicles, String category) {
		List<OffenceDTO> listOfOffences =  offenceDAO.findByClassOfVehiclesAndCategory(classOfVehicles,category);	
		listOfOffences.forEach(id->{
			if(id.getPriority()==null) {
				logger.error("Priority missing for offence: "+id.getOffenceDescription());
				throw new BadRequestException("Priority missing for offence: "+id.getOffenceDescription());
			}
			
			
		});
		listOfOffences.sort((p1, p2) -> p2.getPriority().compareTo(p1.getPriority()));
		return mapper.convertEntity(listOfOffences);
	}
	
	@Override
	public List<OffenceVO> findeListOfOffencesBasedOnCOVandCategoryWeight(String classOfVehicles, String category,String weight) {
		List<OffenceDTO> listOfOffences =  offenceDAO.findByClassOfVehiclesAndCategoryAndWeightInOrderByPriorityAsc(classOfVehicles,category,Arrays.asList(weight,"ALL"));		
		return mapper.convertEntity(listOfOffences);
	}
	
}
