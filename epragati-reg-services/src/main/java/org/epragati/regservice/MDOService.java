package org.epragati.regservice;

import java.util.List;

import org.epragati.jwt.JwtUser;
import org.epragati.master.vo.DistrictVO;
import org.epragati.master.vo.MandalVO;
import org.epragati.master.vo.VillageVO;

public interface MDOService {

	/**
	 * To get the district details of user by token
	 * 
	 * @param jwtUser
	 * @return {@link District with the District Data}
	 */
	List<DistrictVO> getDistrictDetails(JwtUser jwtUser);

	/**
	 * To get the mandal details based on distictId
	 * 
	 * @param districtCode
	 * @return {@link MandalVO with the madal Data}
	 */
	List<MandalVO> getMandalDetailsByDistrict(Integer districtCode);

	/**
	 * To get the village list based on mandal Id
	 * 
	 * @param mandalId
	 * @return {@link VillageVO with the village Data}
	 */
	List<VillageVO> getVillageDetailsByMandal(Integer mandalId);

}
