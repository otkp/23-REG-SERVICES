package org.epragati.hsrp.service.impl;

import java.time.LocalDateTime;
import java.util.Optional;

import org.epragati.aadhar.DateUtil;
import org.epragati.common.dto.HsrpDetailDTO;
import org.epragati.constants.MessageKeys;
import org.epragati.exception.BadRequestException;
import org.epragati.hsrp.dao.HSRPDetailDAO;
import org.epragati.hsrp.mapper.DataMapper;
import org.epragati.hsrp.servic.IntegrationService;
import org.epragati.hsrp.vo.DataVO;
import org.epragati.util.AppMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
/**
 * 
 * @author praveen.kuppannagari
 *
 */

@Service
public class IntegrationServiceImpl implements IntegrationService{
	/**
	 *used to presist the data in hsrp collection
	 * */
	
	private static final Logger logger = LoggerFactory.getLogger(IntegrationServiceImpl.class);
	
	@Autowired
	private AppMessages appMessage;
	
	@Autowired
	private DataMapper dataMapper;
	
	@Autowired
	private HSRPDetailDAO hsrpDetailDAO;

	@Override
	public void createHSRPTRData(DataVO dataVO) {
		Optional<HsrpDetailDTO> hsrpDetailDtoOptional = hsrpDetailDAO.findByTrNumber(dataVO.getTrNumber());
		if (hsrpDetailDtoOptional.isPresent()) {
			throw new BadRequestException(
					appMessage.getResponseMessage(MessageKeys.HSRP_EXIT_TRNUMBER) + dataVO.getTrNumber());
		}
		HsrpDetailDTO hsrpDetailDTO = dataMapper.convertVO(dataVO);
		hsrpDetailDTO.setIteration(0);
		hsrpDetailDTO.setHsrpStatus(10);
		hsrpDetailDTO.setAuthorizationRefNo(getAuthRefNo(dataVO));
		hsrpDetailDTO.setCreatedDate(LocalDateTime.now());
		hsrpDetailDAO.save(hsrpDetailDTO);
		
	}
	
	private String getAuthRefNo(DataVO dataVO) {
		logger.info(":::::::getAuthRefNo::::::start::");
		Long currentDate = DateUtil.toCurrentUTCTimeStamp();
		String authRefNo = "HSRPRTA" + dataVO.getTransactionNo() + currentDate.toString();
		logger.info(":::::::getAuthRefNo::::::ens::" + authRefNo);
		return authRefNo;
	}

	@Override
	public void updatePRData(DataVO dataVO) {
		Optional<HsrpDetailDTO> hsrpDetailDtoOptional = hsrpDetailDAO.findByTrNumberAndVehicleClassType(dataVO.getTrNumber(), dataVO.getVehicleClassType());
		if (hsrpDetailDtoOptional.isPresent()){
			HsrpDetailDTO hsrpDetailDTO = dataMapper.convertVO(dataVO);
			hsrpDetailDAO.save(hsrpDetailDTO);
		}
	}
	

	@Override
	public HsrpDetailDTO fetchHSRPData(String input,String catagory) {
		Optional<HsrpDetailDTO>  hsrpDetailOptional=null;
		HsrpDetailDTO hsrpDetails=null;
		if(catagory!=null && catagory.equals("TR")){
		 hsrpDetailOptional =hsrpDetailDAO.findByTrNumber(input);
		}else{
		 hsrpDetailOptional =hsrpDetailDAO.findByPrNumber(input);
		}
		if(hsrpDetailOptional.isPresent()){
			hsrpDetails=hsrpDetailOptional.get();
			hsrpDetails.setActionsDetails(null);
		}
		
		return hsrpDetails;
	}

}
