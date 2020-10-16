package org.epragati.secondvehicle.service;

import org.epragati.elastic.vo.SecondVehicleSearchResultsVO;

public interface SecondVehicleTopResultsService {

	public SecondVehicleTopResultsService findByApplcationNo();

	public void saveSecondVehicleResults(SecondVehicleSearchResultsVO applicationNo);

}
