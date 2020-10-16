package org.epragati.eibt.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletResponse;

import org.epragati.jwt.JwtUser;
import org.epragati.master.vo.ApplicantSearchWithOutIdInput;
import org.epragati.master.vo.AttendantDetailsVO;
import org.epragati.master.vo.DriverDetailsVO;
import org.epragati.master.vo.EductaionInstituteVehicleDetailsVO;
import org.epragati.master.vo.EibtSearchVO;
import org.epragati.master.vo.OfficeVO;
import org.epragati.master.vo.StudentDetailsVO;
import org.epragati.master.vo.UserVO;
import org.epragati.rta.vo.RtaActionVO;
import org.springframework.web.multipart.MultipartFile;

public interface EductaionInstituteVehicleDetailsService {

	EibtSearchVO getVehicleDetails(String prNo,boolean requetFromSave);

	DriverDetailsVO getDlDetails(ApplicantSearchWithOutIdInput input);

	List<OfficeVO> getAllOffices();
	
	String saveEibtDetails(AttendantDetailsVO vo, String userId) throws IOException;

	void saveEnclosuresForEibt(EductaionInstituteVehicleDetailsVO vo, JwtUser jwtUser) throws IOException;

	EductaionInstituteVehicleDetailsVO viewEibtVehicleDetails(String prNo, JwtUser jwtUser);

	Optional<UserVO> eibtSignUp(UserVO uservo);

	List<EductaionInstituteVehicleDetailsVO> viewAllApplications(JwtUser jwtUser);

	Long vehiclesCount(JwtUser jwtUser);

	HttpServletResponse sampleExcel(HttpServletResponse response);
	
	Optional<EductaionInstituteVehicleDetailsVO> getEibUserDataByPrNo(String prNo);
}
