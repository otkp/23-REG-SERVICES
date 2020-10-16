package org.epragati.master.service;

import java.util.List;
import java.util.Optional;

import org.epragati.payments.vo.ClassOfVehiclesVO;
import org.epragati.payments.vo.FeeDetailsVO;

/**
 * @author sairam.cheruku
 *
 */
public interface CovService {

	/**
	 * 
	 * @param classOfVehicle
	 * @return classOfVehicle Object
	 */
	ClassOfVehiclesVO findByCovCode(String classOfVehicle);

	String getWeightTypeDetails(Integer rlw);
	/**
	 * 
	 * @return
	 */
	List<ClassOfVehiclesVO> getAllCovs(boolean isRequiredAllField);

	 Optional<List<ClassOfVehiclesVO>> getAllCovsByCategory(String category);
	 Optional<List<ClassOfVehiclesVO>> getAllDataEntyCovsByCategory(String category,String regType);
     ClassOfVehiclesVO findByCovdescription(String covdescription);

     Optional<List<ClassOfVehiclesVO>> getcovsForVoluntary(String category, String regType);
}
