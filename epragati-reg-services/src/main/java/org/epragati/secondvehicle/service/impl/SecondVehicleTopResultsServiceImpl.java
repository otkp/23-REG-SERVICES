package org.epragati.secondvehicle.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.epragati.elastic.vo.SecondVehicleSearchResultsVO;
import org.epragati.secondvehicle.dao.SecondVehicleSearchDAO;
import org.epragati.secondvehicle.dto.SecondVehicleResultsDTO;
import org.epragati.secondvehicle.dto.SecondVehicleSearchResultsDTO;
import org.epragati.secondvehicle.mappers.SecondVehicleResultsMapper;
import org.epragati.secondvehicle.mappers.SecondVehicleSearchResultsMapper;
import org.epragati.secondvehicle.service.SecondVehicleTopResultsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SecondVehicleTopResultsServiceImpl implements SecondVehicleTopResultsService {
	private static final Logger logger = LoggerFactory.getLogger(SecondVehicleTopResultsServiceImpl.class);
	@Autowired
	private SecondVehicleSearchDAO secondVehicleSearchDAO;

	@Autowired
	private SecondVehicleResultsMapper secondVehicleResultsMapper;
	@Override
	public SecondVehicleTopResultsService findByApplcationNo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveSecondVehicleResults(SecondVehicleSearchResultsVO secondVehicleResults) {
		Optional<SecondVehicleSearchResultsDTO> secondVehicleOpt = secondVehicleSearchDAO
				.findByApplicationNo(secondVehicleResults.getApplicationNo());
		List<SecondVehicleResultsDTO> dtoList = new ArrayList<SecondVehicleResultsDTO>();
		SecondVehicleSearchResultsDTO secondVehicleSearchResultsDTO = new SecondVehicleSearchResultsDTO();
		
		if (secondVehicleOpt.isPresent()) {
			logger.info("found secondvehicle top results with appNo [{}]", secondVehicleResults.getApplicationNo());
			secondVehicleSearchResultsDTO = secondVehicleOpt.get();
			SecondVehicleResultsDTO secondDto = secondVehicleResultsMapper
					.convertVO(secondVehicleResults.getSvResults());

			secondVehicleSearchResultsDTO.getSvResults().add(secondDto);
		} else {
			logger.info("second vehicle top result saving with appNo [{}]", secondVehicleResults.getApplicationNo());
			secondVehicleSearchResultsDTO.setApplicationNo(secondVehicleResults.getApplicationNo());
			SecondVehicleResultsDTO secondDto = secondVehicleResultsMapper
					.convertVO(secondVehicleResults.getSvResults());
			dtoList.add(secondDto);
			secondVehicleSearchResultsDTO.setSvResults(dtoList);
		}
		secondVehicleSearchDAO.save(secondVehicleSearchResultsDTO);

	}
}
