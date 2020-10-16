package org.epragati.cfst.service;

import java.util.List;

import org.epragati.cfstSync.vo.CfstSyncResponceVO;
import org.epragati.cfstSync.vo.ElasticSecondVehicleVO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;

public interface ElasticService {
	
	public void saveData(List<ElasticSecondVehicleVO> esList);
	
	public void delete(ElasticSecondVehicleVO elasticVo);

	public void saveRegDetailsToSecondVehicleData(StagingRegistrationDetailsDTO registrationDetailsDTO);

	public CfstSyncResponceVO validateElasticSecondVehicelDetails(ElasticSecondVehicleVO elasticSecondVehicleVO);
	
}
