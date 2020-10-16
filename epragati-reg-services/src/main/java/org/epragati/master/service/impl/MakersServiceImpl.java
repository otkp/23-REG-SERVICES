package org.epragati.master.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.epragati.master.dao.MakersDAO;
import org.epragati.master.dao.MasterVariantDAO;
import org.epragati.master.dto.MakersDTO;
import org.epragati.master.dto.MasterVariantDTO;
import org.epragati.master.mappers.MakersMapper;
import org.epragati.master.mappers.MasterVaraintMapper;
import org.epragati.master.service.MakersService;
import org.epragati.master.vo.MakersVO;
import org.epragati.master.vo.MasterVariantVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author saikiran.kola
 *
 */
@Service
public class MakersServiceImpl implements MakersService {
	@Autowired
	private MakersDAO makersDAO;
	@Autowired
	private MakersMapper makersMapper;
	@Autowired
	private MasterVariantDAO masterVariantDAO;
	@Autowired
	private MasterVaraintMapper masterVaraintMapper;

	/**
	 * get all makers details
	 */
	@Override
	public List<MakersVO> getAllMakersDetails() {
		// TODO Auto-generated method stub
		List<MakersVO> MasterMakersVoList = makersMapper.convertEntity(makersDAO.findByStatusTrue());
		return MasterMakersVoList;
	}

	@Override
	public Optional<MakersVO> findMakerNameByMakerId(Integer mId) {
		// TODO Auto-generated method stub
		Optional<MakersDTO> makersDTO = makersDAO.findBymid(mId);
		if (makersDTO.isPresent()) {
			MakersDTO dto = makersDTO.get();
			MakersVO makersVO = makersMapper.convertRequiredDTOtoVO(dto);
			return Optional.of(makersVO);
		}

		return Optional.empty();
	}

	@Override
	public List<MasterVariantVO> getMakerClassByMakerNameAndCov(Integer mId, String cov) {
		
		List<Integer> mmIdList = new ArrayList<>();
		mmIdList.add(mId);
		List<MasterVariantDTO> masterVarientDetails = masterVariantDAO.findByMidInAndVehicleClassID(mmIdList, cov);
		return masterVaraintMapper.convertEntity(masterVarientDetails);
	}
	
	@Override
	public List<MasterVariantVO> getDataEntryMakerClassByMakerNameAndCov(Integer mId, String cov) {
		List<MasterVariantDTO> masterVarientDetails=null;
		List<Integer> mmIdList = new ArrayList<>();
		mmIdList.add(mId);
		if(!"IVCN".equals(cov)){
		 masterVarientDetails = masterVariantDAO.findByMidInAndVehicleClassID(mmIdList, cov);
		}else
		{  cov="MMCN";
		    masterVarientDetails = masterVariantDAO.findByMidInAndVehicleClassID(mmIdList, cov);
		    cov="MCYN";
		    List<MasterVariantDTO> masterVarientDet=masterVariantDAO.findByMidInAndVehicleClassID(mmIdList, cov);
		    masterVarientDetails.addAll(masterVarientDet);
	  }
		
	  return masterVaraintMapper.convertEntity(masterVarientDetails);
	}

	@Override
	public List<MakersVO> getDataEntryMakersDetails(String cov) {
		 List<MasterVariantDTO>  masterVarientDetails = masterVariantDAO.findByVehicleClassID(cov);
		 List<Integer> mid=new ArrayList<Integer>();
		 if(!masterVarientDetails.isEmpty()){
			 for(MasterVariantDTO mids:masterVarientDetails){
			    mid.add(mids.getMid());
			 }
		 }
		// List<MakersDTO> findByStatusTrueAndMidIn(List<Integer> mmId);
		List<MakersVO> MasterMakersVoList = makersMapper.convertEntity(makersDAO.findByStatusTrueAndMidIn(mid));
		return MasterMakersVoList;
	}
	
}
