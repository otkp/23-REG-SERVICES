/**
 * 
 */
package org.epragati.master.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.constants.OfficeType;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.MandalDAO;
import org.epragati.master.dao.MandalMappingDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dto.MandalDTO;
import org.epragati.master.dto.MandalMappingDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.mappers.MandalMapper;
import org.epragati.master.mappers.OfficeMapper;
import org.epragati.master.service.MandalService;
import org.epragati.master.vo.MandalVO;
import org.epragati.master.vo.OfficeVO;
import org.epragati.util.MandalServiceEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author kumaraswamy.asari
 *
 */
@Service
public class MandalServiceImpl implements MandalService {

	@Autowired
	private MandalDAO mandalDAO;

	@Autowired
	private MandalMapper mandalMapper;

	@Autowired
	private OfficeDAO officeDAO;

	@Autowired
	private OfficeMapper officeMappar;

	@Autowired
	private RegistrationDetailDAO regDetailsDAO;

	@Autowired
	private MandalMappingDAO mandalMappingDAO;

	private static final Logger logger = LoggerFactory.getLogger(MandalServiceImpl.class);

	@Override
	public Optional<OfficeVO> getOfficeDetailsByMandal(int mandalCode, String nthFlag) {

		logger.debug("calling getOfficeDetailsByMandal mandalCode:" + mandalCode + "officePrefix" + nthFlag);

		Optional<MandalDTO> optionalMandalDTO = mandalDAO.findByMandalCode(mandalCode);
		if (!optionalMandalDTO.isPresent()) {
			logger.error("No Details found with given madal");
			throw new BadRequestException("No Details found with given madal");
		}

		Optional<OfficeDTO> optionalOfficeDTO = null;

		if (nthFlag.equalsIgnoreCase(MandalServiceEnum.n.toString())) {
			optionalOfficeDTO = officeDAO.findByOfficeCode(optionalMandalDTO.get().getNonTransportOffice());
		} else if (nthFlag.equalsIgnoreCase(MandalServiceEnum.t.toString())) {
			optionalOfficeDTO = officeDAO.findByOfficeCode(optionalMandalDTO.get().getTransportOfice());
		} else if (nthFlag.equalsIgnoreCase(MandalServiceEnum.h.toString())) {
			optionalOfficeDTO = officeDAO.findByOfficeCode(optionalMandalDTO.get().getHsrpoffice());
		}

		return officeMappar.convertEntity(optionalOfficeDTO);

	}

	@Override
	public Optional<OfficeVO> getOfficeDetailsByMandal(int mandalCode, String nthFlag, String ownerType) {

		logger.debug("calling getOfficeDetailsByMandal mandalCode:" + mandalCode + "officePrefix" + nthFlag
				+ "ownerType " + ownerType);

		Optional<MandalDTO> optionalMandalDTO = mandalDAO.findByMandalCode(mandalCode);
		if (!optionalMandalDTO.isPresent()) {
			throw new BadRequestException("No Details found with given madal");
		}

		Optional<OfficeDTO> optionalOfficeDTO = null;

		if (nthFlag.equalsIgnoreCase(MandalServiceEnum.n.toString())) {
			optionalOfficeDTO = officeDAO.findByOfficeCode(optionalMandalDTO.get().getNonTransportOffice());
			optionalOfficeDTO.get().setHsrpOffice(optionalMandalDTO.get().getHsrpoffice());
		} else if (nthFlag.equalsIgnoreCase(MandalServiceEnum.t.toString())) {
			optionalOfficeDTO = officeDAO.findByOfficeCode(optionalMandalDTO.get().getTransportOfice());
			optionalOfficeDTO.get().setHsrpOffice(optionalMandalDTO.get().getHsrpoffice());
		} else if (nthFlag.equalsIgnoreCase(MandalServiceEnum.h.toString())) {
			optionalOfficeDTO = officeDAO.findByOfficeCode(optionalMandalDTO.get().getHsrpoffice());
			optionalOfficeDTO.get().setHsrpOffice(optionalMandalDTO.get().getHsrpoffice());
		}

		if (ownerType.equalsIgnoreCase(MandalServiceEnum.POLICE.toString())) {
			optionalOfficeDTO = officeDAO.findByOfficeCode(optionalMandalDTO.get().getPolice());
		}
		if (ownerType.equalsIgnoreCase(MandalServiceEnum.STU.getDescription())) {
			optionalOfficeDTO = officeDAO.findByOfficeCode(optionalMandalDTO.get().getStu());
		}

		return officeMappar.convertEntity(optionalOfficeDTO);

	}

	@Override
	public List<MandalVO> findByDid(Integer districtId) {
		return mandalMapper.convertEntity(mandalDAO.findByDistrictId(districtId));
	}

	@Override
	public List<MandalVO> findByDidAndPrNo(Integer districtId, String prNo) {
		// TODO Auto-generated method stub
		Optional<RegistrationDetailsDTO> regDTO = regDetailsDAO.findByPrNo(prNo);
		if (!regDTO.isPresent() || regDTO.get().getOfficeDetails() == null
				|| regDTO.get().getOfficeDetails().getOfficeCode() == null
				|| StringUtils.isEmpty(regDTO.get().getOfficeDetails().getType())) {
			logger.error("Office details not found / office code not found in record");
			throw new BadRequestException("Office details not found / office code not found in record");
		}
		
		Optional<MandalMappingDTO> reportingMandal = mandalMappingDAO
				.findByActualOfficeAndStatusTrue(regDTO.get().getOfficeDetails().
				getOfficeCode());
		if(reportingMandal.isPresent()) {
			return mandalMapper.convertEntity(mandalDAO.findByDistrictIdAndTransportOficeIn(districtId,
					reportingMandal.get().getReportingOffice()));
		}
		// TODO :: 
		if (regDTO.get().getOfficeDetails().getType().equalsIgnoreCase(OfficeType.RTA.getCode())) {
			return mandalMapper.convertEntity(mandalDAO.findByDistrictIdAndTransportOfice(districtId,
					(regDTO.get().getOfficeDetails().getOfficeCode())));
		} else if (regDTO.get().getOfficeDetails().getType().equalsIgnoreCase(OfficeType.UNI.getCode())
				|| regDTO.get().getOfficeDetails().getType().equalsIgnoreCase(OfficeType.MVI.getCode())) {
			return mandalMapper.convertEntity(mandalDAO.findByDistrictIdAndTransportOfice(districtId,
					(regDTO.get().getOfficeDetails().getReportingoffice())));
		}
		/*** As per jagan ipput commit the code change logic based on office type RTA,UNIT,MVI office */ 
		/*List<String> officeCodes = new ArrayList<>();
		officeCodes.add(regDTO.get().getOfficeDetails().getReportingoffice())
		; Optional<MandalMappingDTO> reportingMandal = mandalMappingDAO
		.findByActualOfficeAndStatus(regDTO.get().getOfficeDetails().
		getOfficeCode(), Boolean.TRUE); if (reportingMandal.isPresent()) {
		officeCodes.addAll(reportingMandal.get().getReportingOffice()); }
		return mandalMapper.convertEntity(mandalDAO.
		findByDistrictIdAndTransportOficeIn(districtId, officeCodes));*/
		return Collections.emptyList();

	}

	@Override
	public List<MandalVO> findByDidAndPrNoForTransport(Integer districtId, String prNo) {
		Optional<RegistrationDetailsDTO> regDTO = regDetailsDAO.findByPrNo(prNo);
		if (!regDTO.isPresent() || regDTO.get().getOfficeDetails() == null
				|| regDTO.get().getOfficeDetails().getOfficeCode() == null) {
			logger.error("Office details not found / office code not found in record");
			throw new BadRequestException("Office details not found / office code not found in record");
		}
		
		return mandalMapper.convertEntity(mandalDAO.findByDistrictIdAndTransportOfice(districtId,
				regDTO.get().getOfficeDetails().getOfficeCode()));

	}

	@Override
	public List<MandalVO> getMandalsbasedOnOffice(Integer districtId, String officeCode) {
		List<MandalDTO> mandalsList = mandalDAO.findByDistrictIdAndTransportOfice(districtId, officeCode);
		if (CollectionUtils.isNotEmpty(mandalsList)) {
			return mandalMapper.convertEntity(mandalsList);
		}
		return Collections.emptyList();

	}

}
