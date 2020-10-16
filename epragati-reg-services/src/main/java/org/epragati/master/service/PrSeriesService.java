package org.epragati.master.service;

import java.util.List;
import java.util.Optional;

import org.epragati.common.vo.PrGenerationInput;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.PrSeriesDTO;
import org.epragati.sn.vo.BidConfigMasterVO;
import org.epragati.util.payment.ModuleEnum;

/**
 * @author sairam.cheruku
 *
 */

/*@Service*/
public interface PrSeriesService {

	PrSeriesDTO updateCurrentNo(PrSeriesDTO trSeriesDTO);

	String geneatePrSeries(Integer districtId);

	List<PrSeriesDTO> modifyData();


	/*String geneatePrNo(String applicationNo, String trNumber, Integer selectedNo, boolean generatePr, String numberType,
			boolean isNumberBlock);*/
	//String geneatePrNo(String applicationNo,Integer selectedNo, boolean isNumberlocked);

	String processPrForSP(PrGenerationInput prGenerationInput);


	String geneatePrNo(String applicationNo, Integer selectedNo, boolean isNumberlocked, String prSeries,
			ModuleEnum fromCitizen, Optional<OfficeDTO> officeDetails);
	
	Optional<BidConfigMasterVO> getBidConfigMasterData(Boolean isRequiredDynamicData);
	
	/**
	 * return false for assign numbers through schedulers
	 */
	boolean isAssignNumberNow();
}
