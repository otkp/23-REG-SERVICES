package org.epragati.master.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.epragati.constants.CommonConstants;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.ClassOfVehiclesDAO;
import org.epragati.master.dao.CovDAO;
import org.epragati.master.dao.WeightDAO;
import org.epragati.master.dto.WeightDTO;
import org.epragati.master.mappers.CovMapper;
import org.epragati.master.service.CovService;
import org.epragati.payment.dto.ClassOfVehiclesDTO;
import org.epragati.payment.mapper.ClassOfVehiclesMapper;
import org.epragati.payments.vo.ClassOfVehiclesVO;
import org.epragati.payments.vo.FeeDetailsVO;
import org.epragati.payments.vo.FeesVO;
import org.epragati.util.payment.ServiceCodeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
@Service
public class CovServiceImpl implements CovService {

	@Autowired
	private CovMapper covMapper;

	@Autowired
	private CovDAO covDAO;

	@Autowired
	private ClassOfVehiclesDAO classOfVehiclesDAO;

	@Autowired
	private ClassOfVehiclesMapper classOfVehiclesMapper;

	@Autowired
	private WeightDAO weightDAO;

	@Override
	public ClassOfVehiclesVO findByCovCode(String classOfVehicle) {
		return classOfVehiclesMapper.convertEntity(classOfVehiclesDAO.findByCovcode(classOfVehicle));
	}

	@Override
	public String getWeightTypeDetails(Integer rlw) {
		Integer weight = 0;
		if (rlw != null) {
			weight = rlw;
		}
		Double fromValue = null;
		Double toValue = null;
		String weightType = null;
		List<WeightDTO> weights = weightDAO.findAll();
		for (WeightDTO weightDTO : weights) {
			fromValue = weightDTO.getFromvalue();
			toValue = weightDTO.getTovalue();
			if (rlw >= fromValue && rlw <= toValue) {
				weightType = weightDTO.getWeighttype();
			}
		}
		return weightType;
	}

	@Override
	public List<ClassOfVehiclesVO> getAllCovs(boolean isRequiredAllField) {

		List<ClassOfVehiclesDTO> classOfVehicles = classOfVehiclesDAO.findAll();
		List<ClassOfVehiclesVO> classOfVehiclesList = new ArrayList<>();
		if(isRequiredAllField) {
			ClassOfVehiclesVO vehiclesVO = new ClassOfVehiclesVO();
			vehiclesVO.setCovCode(CommonConstants.ALL);
			vehiclesVO.setCovdescription(CommonConstants.ALL);
			classOfVehiclesList.add(vehiclesVO);
		}
		for (ClassOfVehiclesDTO vehicles : classOfVehicles) {
			ClassOfVehiclesVO vehiclesVO = new ClassOfVehiclesVO();
			vehiclesVO.setCovCode(vehicles.getCovcode());
			vehiclesVO.setCovdescription(vehicles.getCovdescription());
			vehiclesVO.setCategory(vehicles.getCategory());
			classOfVehiclesList.add(vehiclesVO);
		}
		return classOfVehiclesList;
	}

	@Override
	public Optional<List<ClassOfVehiclesVO>> getAllCovsByCategory(String category) {

		List<ClassOfVehiclesDTO> classOfVehicles = classOfVehiclesDAO.findByCategory(category);
		List<ClassOfVehiclesVO> classOfVehiclesList = new ArrayList<>();
		for (ClassOfVehiclesDTO vehicles : classOfVehicles) {
			ClassOfVehiclesVO vehiclesVO = new ClassOfVehiclesVO();
			vehiclesVO.setCovCode(vehicles.getCovcode());
			vehiclesVO.setCovdescription(vehicles.getCovdescription());
			vehiclesVO.setCategory(vehicles.getCategory());
			classOfVehiclesList.add(vehiclesVO);
		}
		return Optional.of(classOfVehiclesList);
	}

	@Override
	public Optional<List<ClassOfVehiclesVO>> getAllDataEntyCovsByCategory(String category, String regType) {
		List<ClassOfVehiclesDTO> classOfVehicles = null;
		if ("NONREG".equals(regType)) {
			classOfVehicles = classOfVehiclesDAO.findByCategoryAndIsUnRegistered(category, true);
		}
		if ("REG".equals(regType)) {
			classOfVehicles = classOfVehiclesDAO.findByCategoryAndIsRegistered(category, true);
		}
		List<ClassOfVehiclesVO> classOfVehiclesList = new ArrayList<>();
		for (ClassOfVehiclesDTO vehicles : classOfVehicles) {
			ClassOfVehiclesVO vehiclesVO = new ClassOfVehiclesVO();
			vehiclesVO.setCovCode(vehicles.getCovcode());
			vehiclesVO.setCovdescription(vehicles.getCovdescription());
			vehiclesVO.setCategory(vehicles.getCategory());
			vehiclesVO.setRegistered(vehicles.isRegistered());
			vehiclesVO.setUnRegistered(vehicles.isUnRegistered());
			classOfVehiclesList.add(vehiclesVO);
		}
		return Optional.of(classOfVehiclesList);
	}
	
	@Override
    public ClassOfVehiclesVO findByCovdescription(String covdescription) {
                    ClassOfVehiclesDTO dto = classOfVehiclesDAO.findByCovdescription(covdescription);
                    if(dto == null) {
                                    throw new BadRequestException("class of vehicle not found for:" + covdescription);
                    }
                    return classOfVehiclesMapper.convertEntity(dto);
    }

	@Override
	public Optional<List<ClassOfVehiclesVO>> getcovsForVoluntary(String category, String regType) {
		List<ClassOfVehiclesDTO> classOfVehicles = null;
	/*	if ("NONREG".equals(regType)) {
			classOfVehicles = classOfVehiclesDAO.findByCategoryAndIsUnRegistered(category, true);
		}*/
		//if ("REG".equals(regType)) {
			classOfVehicles = classOfVehiclesDAO.findByCategoryAndVoluntaryCovTrue(category);
		//}
		List<ClassOfVehiclesVO> classOfVehiclesList = new ArrayList<>();
		for (ClassOfVehiclesDTO vehicles : classOfVehicles) {
			ClassOfVehiclesVO vehiclesVO = new ClassOfVehiclesVO();
			vehiclesVO.setCovCode(vehicles.getCovcode());
			vehiclesVO.setCovdescription(vehicles.getCovdescription());
			vehiclesVO.setCategory(vehicles.getCategory());
			vehiclesVO.setRegistered(vehicles.isRegistered());
			vehiclesVO.setUnRegistered(vehicles.isUnRegistered());
			classOfVehiclesList.add(vehiclesVO);
		}
		return Optional.of(classOfVehiclesList);
	}
}
