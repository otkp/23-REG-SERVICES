package org.epragati.dispatcher.service;

import java.util.List;
import java.util.Optional;

import org.epragati.dispatcher.vo.DispatcherReportVO;
import org.epragati.dispatcher.vo.DispatcherSubmissionVO;
import org.epragati.dispatcher.vo.FormDetailsVo;
import org.epragati.jwt.JwtUser;
import org.epragati.master.vo.OfficeVO;
import org.epragati.master.vo.UserVO;
import org.springframework.data.domain.Pageable;

/**
 * 
 * 
 * @author roshan
 *
 */
public interface DispatcherSubmissionService {

	/***
	 *  
	 * @param dispatcherFormSubmissionDispatherVOs
	 * @return
	 */
	String insertDetails(List<DispatcherSubmissionVO> dispatcherFormSubmissionDispatherVOs);
	
	/*** 
	 * @param vo
	 * @param officeCode
	 * @param pagable 
	 * @return
	 */
	DispatcherReportVO getDispatcherDetailsForReport(FormDetailsVo vo, String officeCode, Pageable pagable);

	List<String> getAllCardReasons();

	DispatcherSubmissionVO  saveDetails(DispatcherSubmissionVO vo);

	DispatcherReportVO getDetails(DispatcherSubmissionVO dispatcherSubmissionVO, JwtUser jwtUser, Pageable pagable);

	DispatcherSubmissionVO getDetailsByregNo(String regNo);

	Optional<UserVO> getUserDetails(String userId, String officeCode);

	List<OfficeVO> getMviForDist(String officeCode);


	//List<DispatcherSubmissionDTO> getDispatcherDetailsByDate(LocalDateTimeConverterDTO dto);
}
