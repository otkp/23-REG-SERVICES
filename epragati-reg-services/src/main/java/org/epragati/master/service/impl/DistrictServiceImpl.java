package org.epragati.master.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections4.CollectionUtils;
import org.epragati.constants.CommonConstants;
import org.epragati.master.dao.DistrictDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.mappers.DistrictMapper;
import org.epragati.master.service.DistrictService;
import org.epragati.master.vo.DistrictVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author saroj.sahoo
 *
 */
@Service
public class DistrictServiceImpl implements DistrictService {

	@Autowired
	private DistrictDAO districtDAO;

	@Autowired
	private DistrictMapper districtMapper;

	@Autowired
	private UserDAO masterUsersDAO;

	@Autowired
	private OfficeDAO officeDAO;

	private static List<DistrictVO> districtListVO = new ArrayList<>();

	@Override
	public List<DistrictVO> findAll() {

		return districtMapper.convertEntity(districtDAO.findAll());
	}

	@Override
	public List<DistrictVO> findBySid(String stateId) {
		if (CollectionUtils.isEmpty(districtListVO)) {
			districtListVO = districtMapper.convertEntity(districtDAO.findByStateIdAndStatus(stateId, CommonConstants.Y));
		}
		return districtListVO;
	}

	@Override
	public List<DistrictVO> findDistrictsByUser(String userId) {
		UserDTO masterUserDTO = masterUsersDAO.findByUserIdAndStatusTrue(userId);
		Optional<OfficeDTO> officeDetails = officeDAO.findByOfficeCode(masterUserDTO.getOffice().getOfficeCode());
		return districtMapper.convertEntity(districtDAO.findByDistrictId(officeDetails.get().getDistrict()));
	}

	@Override
	public List<DistrictVO> getDistrictsForBiLateralTax() {
		
		 List<DistrictVO>  vo = districtMapper.convertEntity(districtDAO.findByAllowBiLateralTaxIsTrue());
		
		return vo;
	}
	@Override
	public String findDistNameFromInput(List<DistrictVO> districtVOList,Integer distId) {
		
		return districtVOList.stream().filter(vo->distId.equals(vo.getDistrictId())).map(vo->vo.getName()).findFirst().orElse(null);
	}
}
