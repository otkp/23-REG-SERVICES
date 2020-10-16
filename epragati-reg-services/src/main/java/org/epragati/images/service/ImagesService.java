package org.epragati.images.service;

import java.io.IOException;
import java.util.List;

import org.epragati.images.vo.ImageInput;
import org.epragati.service.enclosure.vo.DealerEnclosureVO;
import org.epragati.service.enclosure.vo.DisplayEnclosures;
import org.epragati.service.enclosure.vo.EnclosureRejectedVO;
import org.springframework.web.multipart.MultipartFile;

public interface ImagesService {


	//List<EnclosureRejectedVO> getListOfSupportedEnclosureDetails(String applicationNo);
	
	//public List<EnclosureRejectedVO> getListOfRejectedEnclosureDetails(String applicationFormNo);

	List<DisplayEnclosures> getListOfEnclosureDetailsForRta(String applicationFormNo, String token, String inputRole);

	Boolean uploadEnclosures(String applicationNo, List<ImageInput> images, MultipartFile[] uploadfiles) throws IOException;
	
	void saveImages(String applicationNo, List<ImageInput> images, MultipartFile[] uploadfiles) throws IOException;

	DealerEnclosureVO getListOfSupportedEnclosuresForDealer(String applicationNo);

	List<EnclosureRejectedVO> getListOfSupportedEnclosuresForMobile(String applicationNo);

	boolean statusForMobileupload(String applicationNo);

	//List<EnclosureRejectedVO> getListOfSupportedEnclosuresForService(String serviceName);

	
	

	

}
