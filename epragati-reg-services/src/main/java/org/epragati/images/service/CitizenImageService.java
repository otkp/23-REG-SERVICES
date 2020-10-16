package org.epragati.images.service;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.epragati.images.vo.ImageInput;
import org.epragati.master.dto.Enclosures;
import org.epragati.service.enclosure.vo.CitizenImagesInput;
import org.epragati.service.enclosure.vo.EnclosureRejectedVO;
import org.epragati.service.enclosure.vo.EnclosureSupportedVO;
import org.epragati.service.enclosure.vo.ImageVO;
import org.springframework.web.multipart.MultipartFile;
import org.epragati.service.enclosure.vo.CitizenEnclosersVO;
public interface CitizenImageService {

	List<EnclosureRejectedVO> getCitizenSupportedEnclosures(CitizenImagesInput input);

	List<EnclosureRejectedVO> getListOfRejectedEnclosures(String applicationNo);

	Boolean reuploadEnclosures(String applicationNo, List<ImageInput> images, MultipartFile[] uploadfiles)
			throws IOException;

	//List<EnclosureRejectedVO> getListOfSupportedEnclosuresForService(String serviceName);

	List<EnclosureRejectedVO> getListOfSupportedEnclosuresForService(String serviceName, String applicationNo);

	Enclosures getListOfEnclosureCitizenToSubmit(String applicationFormNo, String userId, String selectedRole);
	
	Enclosures getEnclosureDetailsForRtaSideForDealerModule(String applicationFormNo, String userId,
			String selectedRole);
	
	List<EnclosureSupportedVO> getListOfSupportEnclosuresForFA(Set<Integer> id,String caste);
	
	List<ImageVO> getImages(String applicationNo);
	
	CitizenEnclosersVO getListOfApprovedEnclosures(String applicationNumber);
}
