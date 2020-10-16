package org.epragati.regservice.impl;

import java.util.List;
import java.util.Optional;

import org.epragati.constants.NationalityEnum;
import org.epragati.jwt.JwtUser;
import org.epragati.master.dao.DistrictDAO;
import org.epragati.master.dao.MandalDAO;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dao.VillageDAO;
import org.epragati.master.dto.DistrictDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.mappers.DistrictMapper;
import org.epragati.master.mappers.MandalMapper;
import org.epragati.master.mappers.VillageMapper;
import org.epragati.master.vo.DistrictVO;
import org.epragati.master.vo.MandalVO;
import org.epragati.master.vo.VillageVO;
import org.epragati.regservice.MDOService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MDOServiceImpl implements MDOService {

	@Autowired
	private DistrictDAO districtDAO;

	@Autowired
	private UserDAO userDAO;

	@Autowired
	private DistrictMapper districtMapper;

	@Autowired
	private MandalDAO mandalDAO;

	@Autowired
	private MandalMapper mandalMapper;

	@Autowired
	private VillageDAO villageDAO;

	@Autowired
	private VillageMapper villageMapper;

	@Override
	public List<DistrictVO> getDistrictDetails(JwtUser jwtUser) {
		Optional<UserDTO> userDetails = userDAO.findByUserId(jwtUser.getId());
		List<DistrictDTO> districlist = districtDAO.findByStateIdAndStatus(NationalityEnum.AP.getName(), "Y");

		return districtMapper.convertEntity(districlist);
	}

	@Override
	public List<MandalVO> getMandalDetailsByDistrict(Integer districtCode) {
		return mandalMapper.convertEntity(mandalDAO.findByDistrictIdOrderByMandalNameAsc(districtCode));
	}

	@Override
	public List<VillageVO> getVillageDetailsByMandal(Integer mandalId) {
		return villageMapper
				.convertEntity(villageDAO.findByMandalIdAndStatusIgnoreCaseOrderByVillageNameAsc(mandalId, "TRUE"));
	}

}
