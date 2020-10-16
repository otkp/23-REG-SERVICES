package org.epragati.master.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.epragati.constants.OfficeType;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.mappers.OfficeMapper;
import org.epragati.master.service.OfficeService;
import org.epragati.master.vo.OfficeVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author sairam.cheruku
 *
 */
@Service
public class OfficeServiceImpl implements OfficeService {

	private static final Logger logger = LoggerFactory.getLogger(OfficeServiceImpl.class);

	@Autowired
	private OfficeDAO officeDAO;

	@Autowired
	private OfficeMapper officeMapper;

	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;

	/**
	 * find all offices
	 */
	@Override
	public List<OfficeVO> findAll() {
		return officeMapper.convertEntity(officeDAO.findAll());
	}

	/**
	 * 
	 */

	@Override
	public Optional<OfficeVO> findByOfficecode(String officeCode) {
		return officeMapper.convertEntity(officeDAO.findByOfficeCode(officeCode));
	}

	@Override
	public List<OfficeVO> getOfficeByDistrict(Integer districtId) {

		return officeMapper.convertEntity(officeDAO.findBydistrict(districtId));
	}

	@Override
	public List<OfficeVO> findRTAOffices() {

		return officeMapper.convertEntity(officeDAO.findByTypeInAndIsActive(
				Arrays.asList(OfficeType.RTA.getCode(), OfficeType.UNI.getCode()), Boolean.TRUE));
	}

	
	@Override
	public OfficeDTO getDistrictByofficeCode(String officeCode) {
		Optional<OfficeDTO> officeOptional = officeDAO.findByOfficeCode(officeCode);
		if (!officeOptional.isPresent()) {
			throw new BadRequestException("No District found with officeCode :" + officeCode);
		}
		return officeOptional.get();
	}
	
	@Override
	public List<OfficeVO> findRTAOfficesForFC() {

		List<OfficeDTO> listDto = officeDAO.findByTypeInAndIsActive(Arrays.asList(OfficeType.RTA.getCode()),
				Boolean.TRUE);
		List<OfficeDTO> listOfOffices = new ArrayList<>();
		if (listDto.isEmpty()) {
			logger.error("No mater data for office type RTA...");
			throw new BadRequestException("No mater data for office type RTA...");
		}
		for (OfficeDTO dto : listDto) {
			if (StringUtils.isNoneBlank(dto.getInspectionOffice())) {
				Optional<OfficeDTO> optionalDto = officeDAO.findByOfficeCode(dto.getInspectionOffice());
				if (!optionalDto.isPresent()) {
					logger.error("No mater data for office code: " + dto.getInspectionOffice());
					throw new BadRequestException("No mater data for office code: " + dto.getInspectionOffice());
				}
				dto.setOfficeAddress1(optionalDto.get().getOfficeAddress1());
				dto.setOfficeAddress2(optionalDto.get().getOfficeAddress2());
				// dto.setOfficeName(optionalDto.get().getOfficeName());
			}
			if (dto.getOfficeName().equalsIgnoreCase("OTHER")) {
				continue;
			}
			listOfOffices.add(dto);
		}
		List<OfficeVO> vo = officeMapper.convertEntity(listOfOffices);
		return vo;
	}

	@Override
	public List<OfficeVO> findRTAOfficesForNewFC(String prNo) {

		List<OfficeDTO> listDto = officeDAO.findByTypeInAndIsActive(Arrays.asList(OfficeType.RTA.getCode()),
				Boolean.TRUE);
		List<OfficeDTO> listOfOffices = new ArrayList<>();
		if (listDto.isEmpty()) {
			logger.error("No mater data for office type RTA...");
			throw new BadRequestException("No mater data for office type RTA...");
		}
		Optional<RegistrationDetailsDTO> regOptional = registrationDetailDAO.findByPrNo(prNo);
		if (!regOptional.isPresent()) {
			logger.error("application not found for pr no: : " + prNo);
			throw new BadRequestException("application not found for pr no: : " + prNo);
		}
		if (regOptional.get().getApplicantDetails().getPresentAddress() == null
				|| regOptional.get().getApplicantDetails().getPresentAddress().getDistrict() == null
				|| regOptional.get().getApplicantDetails().getPresentAddress().getDistrict().getDistrictId() == null) {
			logger.error("District details not found: : " + prNo);
			throw new BadRequestException("District details not found: : " + prNo);
		}
		Integer districtId = regOptional.get().getApplicantDetails().getPresentAddress().getDistrict().getDistrictId();
		for (OfficeDTO dto : listDto) {
			if (StringUtils.isNoneBlank(dto.getInspectionOffice())) {
				Optional<OfficeDTO> optionalDto = officeDAO.findByOfficeCode(dto.getInspectionOffice());
				if (!optionalDto.isPresent()) {
					logger.error("No mater data for office code: " + dto.getInspectionOffice());
					throw new BadRequestException("No mater data for office code: " + dto.getInspectionOffice());
				}
				dto.setOfficeAddress1(optionalDto.get().getOfficeAddress1());
				dto.setOfficeAddress2(optionalDto.get().getOfficeAddress2());
				// dto.setOfficeName(optionalDto.get().getOfficeName());
			}
			if (dto.getOfficeName().equalsIgnoreCase("OTHER") || dto.getDistrict().equals(districtId)) {
				continue;
			}
			listOfOffices.add(dto);
		}
		List<OfficeVO> vo = officeMapper.convertEntity(listOfOffices);
		return vo;
	}

	@Override
	public List<OfficeVO> getOfficeByDistrictLimited(Integer districtId) {
		return officeMapper.dashBoardLimitedFiledsList(officeDAO.findBydistrictNative(districtId));
	}
	
	
}
