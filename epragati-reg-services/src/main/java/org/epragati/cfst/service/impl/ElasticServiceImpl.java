package org.epragati.cfst.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.epragati.cfst.dao.ElasticSecondVehicleDAO;
import org.epragati.cfst.dto.ElasticSecondVehicleDTO;
import org.epragati.cfst.mapper.ElasticSecondVehicleMapper;
import org.epragati.cfst.service.ElasticService;
import org.epragati.cfstSync.vo.CfstSyncResponceVO;
import org.epragati.cfstSync.vo.ElasticSecondVehicleVO;
import org.epragati.master.dao.CfstSyncResponceDAO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.mappers.CfstSyncResponceMapper;
import org.epragati.util.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ElasticServiceImpl implements ElasticService {

	
	private static final Logger logger = LoggerFactory.getLogger(ElasticServiceImpl.class);

	
	@Autowired
	private ElasticSecondVehicleDAO elasticSecondVehicleDAO;

	@Autowired
	private ElasticSecondVehicleMapper elasticSecondVehicleMapper;

	@Autowired
	private CfstSyncResponceDAO cfstSyncResponceDAO;

	@Autowired
	private CfstSyncResponceMapper cfstSyncResponceMapper;

	@Override
	public void saveData(List<ElasticSecondVehicleVO> esList) {
		List<String> prNumbers = esList.stream().map(es -> es.getPrNumber()).collect(Collectors.toList());
		List<ElasticSecondVehicleDTO> esDTOList = elasticSecondVehicleDAO.findByPrNumberIn(prNumbers);

		List<ElasticSecondVehicleDTO> missingList = new ArrayList<>();

		esList.parallelStream().forEach(vo -> {

			ElasticSecondVehicleDTO elasticSecondVehicleDTO = esDTOList.stream()
					.filter(dto -> dto.getPrNumber().equals(vo.getPrNumber())).findFirst().orElse(null);

			if (elasticSecondVehicleDTO == null) {
				elasticSecondVehicleDTO = new ElasticSecondVehicleDTO();
				elasticSecondVehicleDTO.setSynchStatus(true);
				missingList.add(elasticSecondVehicleDTO);
			}
			elasticSecondVehicleMapper.convertVO(elasticSecondVehicleDTO, vo);
		});
		esDTOList.addAll(missingList);
		elasticSecondVehicleDAO.save(esDTOList);
		esDTOList.clear();
		missingList.clear();

	}
	
	
	@Override
	public void delete(ElasticSecondVehicleVO elasticVo) {

	}

	@Override
	public void saveRegDetailsToSecondVehicleData(StagingRegistrationDetailsDTO registrationDetailsDTO) {
		
		ElasticSecondVehicleVO vo = new ElasticSecondVehicleVO();
		ElasticSecondVehicleDTO elasticSecondVehicleDTO = new ElasticSecondVehicleDTO();

		vo.setPrNumber(registrationDetailsDTO.getPrNo());
		vo.setFirstName(registrationDetailsDTO.getApplicantDetails().getFirstName());
		vo.setDob(registrationDetailsDTO.getApplicantDetails().getDateOfBirth());
		vo.setClassOfVehicle(registrationDetailsDTO.getClassOfVehicle());
		vo.setOfficeCode(registrationDetailsDTO.getOfficeDetails().getOfficeCode());
		vo.setIsAadhaarValidate(registrationDetailsDTO.getApplicantDetails().getIsAadhaarValidated());
		if (StringUtils.isNoneBlank(registrationDetailsDTO.getLastName())
				|| null != registrationDetailsDTO.getLastName()) {
			vo.setLastName(registrationDetailsDTO.getApplicantDetails().getLastName());
		}
		if (StringUtils.isNoneBlank(registrationDetailsDTO.getApplicantDetails().getFatherName())
				|| null != registrationDetailsDTO.getLastName()) {
			vo.setFatherName(registrationDetailsDTO.getApplicantDetails().getFatherName());
		}

		if (StringUtils.isNoneBlank(registrationDetailsDTO.getApplicantDetails().getPresentAddress().getDoorNo())
				|| null != registrationDetailsDTO.getApplicantDetails().getPresentAddress().getDoorNo()) {
			vo.setAddress1(registrationDetailsDTO.getApplicantDetails().getPresentAddress().getDoorNo());
		}
		if (StringUtils.isNoneBlank(registrationDetailsDTO.getApplicantDetails().getPresentAddress().getStreetName())
				|| null != registrationDetailsDTO.getApplicantDetails().getPresentAddress().getStreetName()) {
			vo.setAddress2(registrationDetailsDTO.getApplicantDetails().getPresentAddress().getStreetName());
		}
		if (StringUtils.isNoneBlank(
				registrationDetailsDTO.getApplicantDetails().getPresentAddress().getVillage().getVillageName())
				|| null != registrationDetailsDTO.getApplicantDetails().getPresentAddress().getVillage()
						.getVillageName()) {
			vo.setAddress3(
					registrationDetailsDTO.getApplicantDetails().getPresentAddress().getVillage().getVillageName());
		}
		if (StringUtils.isNoneBlank(
				registrationDetailsDTO.getApplicantDetails().getPresentAddress().getMandal().getMandalName())
				|| null != registrationDetailsDTO.getApplicantDetails().getPresentAddress().getMandal()
						.getMandalName()) {
			vo.setMandal(registrationDetailsDTO.getApplicantDetails().getPresentAddress().getMandal().getMandalName());
		}
		if (StringUtils.isNoneBlank(
				registrationDetailsDTO.getApplicantDetails().getPresentAddress().getDistrict().getDistrictName())
				|| null != registrationDetailsDTO.getApplicantDetails().getPresentAddress().getDistrict()
						.getDistrictName()) {
			vo.setDistrict(
					registrationDetailsDTO.getApplicantDetails().getPresentAddress().getDistrict().getDistrictName());
		}

		if (StringUtils.isNoneBlank(registrationDetailsDTO.getOfficeDetails().getOfficeAddress1())
				|| null != registrationDetailsDTO.getOfficeDetails().getOfficeAddress1()) {
			vo.setOfficeAddress(registrationDetailsDTO.getOfficeDetails().getOfficeAddress1());
		}

		vo.setAadhaarNo((StringUtils.isNoneBlank(registrationDetailsDTO.getApplicantDetails().getAadharNo())
		|| null != registrationDetailsDTO.getApplicantDetails().getAadharNo())?registrationDetailsDTO.getApplicantDetails().getAadharNo():StringUtils.EMPTY);
		
		elasticSecondVehicleDTO.setCreatedDate(LocalDateTime.now());

		elasticSecondVehicleMapper.convertVO(elasticSecondVehicleDTO, vo);
		elasticSecondVehicleDAO.save(elasticSecondVehicleDTO);
		logger.info("[{}] Saved Into Elastic Data Saved Success", elasticSecondVehicleDTO.getPrNumber());
	}
	

	@Override
	public CfstSyncResponceVO validateElasticSecondVehicelDetails(ElasticSecondVehicleVO elasticSecondVehicleVO) {
		CfstSyncResponceVO cfstSyncResponceVO = new CfstSyncResponceVO();
		List<String> errors = new ArrayList<>();
		String value = null;
		StringBuffer sb = new StringBuffer();
		cfstSyncResponceVO.setStatus(Status.SuccessFailStatus.SUCCESS);
		if (elasticSecondVehicleVO.getPrNumber() != null) {
			if (elasticSecondVehicleVO.getIsAadhaarValidate() && null == elasticSecondVehicleVO.getAadhaarNo()) {
				errors.add("Aadhar number is missing");
				cfstSyncResponceVO.setStatus(Status.SuccessFailStatus.FAILURE);
			}
			if (elasticSecondVehicleVO.getFirstName() == null) {
				errors.add("First name is missing");
				cfstSyncResponceVO.setStatus(Status.SuccessFailStatus.FAILURE);
			}
			if (elasticSecondVehicleVO.getDob() == null) {
				errors.add("DOB is missing");
				cfstSyncResponceVO.setStatus(Status.SuccessFailStatus.FAILURE);
			}
			cfstSyncResponceVO.setPrNo(elasticSecondVehicleVO.getPrNumber());
		} else {
			cfstSyncResponceVO.setFirstName(elasticSecondVehicleVO.getFirstName());
			cfstSyncResponceVO.setStatus(Status.SuccessFailStatus.FAILURE);
			errors.add("PR number is Missing");
		}
		if (elasticSecondVehicleVO.getFirstName() != null) {
			cfstSyncResponceVO.setFirstName(elasticSecondVehicleVO.getFirstName());
		}

		for (String string : errors) {
			sb.append(string);
			sb.append(",");
		}
		value = removeLastChar(sb.toString());
		cfstSyncResponceVO.setErrors(value);
		cfstSyncResponceDAO.save(cfstSyncResponceMapper.convertVO(cfstSyncResponceVO));
		return cfstSyncResponceVO;
	}

	public String removeLastChar(String s) {
		if (s == null || s.length() == 0) {
			return s;
		}
		return s.substring(0, s.length() - 1);
	}

}
