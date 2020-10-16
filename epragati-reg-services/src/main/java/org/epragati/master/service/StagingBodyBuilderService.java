package org.epragati.master.service;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import org.epragati.images.vo.ImageInput;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.vo.OwnerAndVahanInfoVO;
import org.epragati.regservice.vo.AlterationVO;
import org.epragati.service.enclosure.vo.DisplayEnclosures;
import org.springframework.web.multipart.MultipartFile;

public interface StagingBodyBuilderService {

	/**
	 * 
	 * @param applicationFormNo
	 * @param userId
	 * @param inputRole
	 * @return
	 */

	List<DisplayEnclosures> getEnclosuresforView(String applicationFormNo, String userId, String inputRole);

	public Optional<AlterationVO> getVehicleAlterationData(String applicationNo);

	OwnerAndVahanInfoVO findByTrNo(String officecode, String trNo);

	String getSequenceNo();

	public void saveAlterationDetails(UserDTO userDTO, AlterationVO alterationVO, List<ImageInput> imageInput,
			MultipartFile[] uploadfiles) throws IOException;

}
