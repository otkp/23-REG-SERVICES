package org.epragati.tax.service;

import java.util.List;

import org.epragati.master.dto.StagingRegistrationDetailsDTO;

public interface TaxService {

	/**
	 * getting tax details
	 * @param stagingRegDetails
	 * @return
	 */
	StagingRegistrationDetailsDTO getTaxDetails(StagingRegistrationDetailsDTO stagingRegDetails);

	/**
	 * 
	 * @param State
	 * @param classOfVehicle
	 * @param seatingCapacity
	 * @return
	 */
	List<String> getTaxTypes(String State, String classOfVehicle, Integer seatingCapacity,Integer gvw);

}
