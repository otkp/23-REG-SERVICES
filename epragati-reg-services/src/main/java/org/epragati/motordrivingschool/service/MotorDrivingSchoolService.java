package org.epragati.motordrivingschool.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.epragati.images.vo.InputVO;
import org.epragati.master.dto.MasterUsersDTO;
import org.epragati.master.vo.ApplicantSearchWithOutIdInput;
import org.epragati.master.vo.DriverDetailsVO;
import org.epragati.master.vo.MotorDrivingSchoolVO;
import org.epragati.master.vo.OfficeVO;
import org.epragati.regservice.vo.MobileApplicationStatusVO;
import org.epragati.service.enclosure.vo.CitizenImagesInput;
import org.springframework.web.multipart.MultipartFile;

public interface MotorDrivingSchoolService {
	





	List<MotorDrivingSchoolVO> getVehicleDetails(List<String> prNo, String schoolType);




	void userIdGeneration(MotorDrivingSchoolVO motorDrivingSchool);



	List<OfficeVO> getAllOffices(String officeCode);







	List<MotorDrivingSchoolVO> getMVIDetailsBasedonOffice(String officeCode);




	List<MotorDrivingSchoolVO> getRTAUsers(Set<String> officeCode);
	

}
