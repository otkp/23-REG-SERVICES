/*package org.epragati.regservice.impl;

import java.util.List;

import org.epragati.aadhaar.AadhaarRequestVO;
import org.epragati.redisrepository.RedisRepository;
import org.epragati.regservice.RegistrationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RadisCacheForFourWheel  {
	private static final Logger logger = LoggerFactory.getLogger(RadisCacheForFourWheel.class);
	@Autowired
	private RedisRepository redisRepository;
	@Autowired
	private RegistrationService registration;

	public List<AadhaarRequestVO> createAadharResponseFromCache(String aadharNMumber) {

		try {
			List<AadhaarRequestVO> adhaarResponseList = redisRepository.findAadharNumber(aadharNMumber);
			if (adhaarResponseList == null) {
				adhaarResponseList = registration.getAdhaarData(aadharNMumber);
				redisRepository.addAadharResponseList(aadharNMumber, adhaarResponseList);
				registration.getAdhaarData(aadharNMumber);
				return adhaarResponseList;
			}
			else if (adhaarResponseList != null && adhaarResponseList.isEmpty()) {
				return adhaarResponseList;
			}  else {
				adhaarResponseList = registration.getAdhaarData(aadharNMumber);
				redisRepository.addAadharResponseList(aadharNMumber, adhaarResponseList);
				registration.getAdhaarData(aadharNMumber);
				return adhaarResponseList;

			}

		} catch (Exception e) {
			logger.warn("Unable to Process Redis Cache [{}]", e.getMessage());
		}
		return null;

	}

	public List<AadhaarRequestVO> getAadharResponse(String aadharNMumber) {
		List<AadhaarRequestVO> adhaarResponseList = registration.getAdhaarData(aadharNMumber);
		return adhaarResponseList;
	}

}*/
