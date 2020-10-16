package org.epragati.vcr.service;

import java.util.List;

import org.epragati.master.vo.OffenceCategoryVO;
import org.epragati.master.vo.OffenceVO;

public interface OffenceService {

	void save(List<OffenceVO> vo) throws Exception;

	List<OffenceVO> findOffenceServiceByCategeory(OffenceCategoryVO categeroy);

	List<OffenceVO> findAllOffence();
	
	List<OffenceVO> findeListOfOffencesBasedOnCOVandCategory(String classOfVehicles, String category);
	
	List<OffenceVO> findeListOfOffencesBasedOnCOVandCategoryWeight(String classOfVehicles, String category,
			String weight);

}
