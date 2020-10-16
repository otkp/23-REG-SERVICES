package org.epragati.common.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.epragati.constants.OwnerTypeEnum;
import org.epragati.master.dao.DuplicatePrNumberDAO;
import org.epragati.master.dao.GeneratedPrDetailsDAO;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dao.RegServiceDAO;
import org.epragati.master.dao.StagingRegistrationDetailsDAO;
import org.epragati.master.dto.GeneratedPrDetailsDTO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.rta.vo.PrGenerationVO;
import org.epragati.sn.numberseries.dao.PRPoolDAO;
import org.epragati.sn.numberseries.dao.PrNumberAssignDetailsConfigDAO;
import org.epragati.sn.numberseries.dao.PrNumberAssignDetailsConfigLogDAO;
import org.epragati.sn.numberseries.dao.PrimesNumbersDAO;
import org.epragati.sn.numberseries.dao.SnPrSeriesDAO;
import org.epragati.sn.numberseries.dto.PrNumberAssignDetailsConfigDTO;
import org.epragati.sn.numberseries.dto.PrNumberAssignDetailsConfigLogDTO;
import org.epragati.sn.numberseries.dto.PremiumNumbersDTO;
import org.epragati.sn.service.impl.ActionsDetailsHelper;
import org.epragati.util.NumberPoolStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;


public abstract class CommonService {
	protected static final Logger logger = LoggerFactory.getLogger(CommonService.class);


	@Value("${sn.number.series.pool.size:1000}")
	protected Integer maxNumberPoolSize;

	@Autowired
	protected RegServiceDAO regServiceDAO;

	@Autowired
	protected PRPoolDAO numbersPoolDAO;

	@Autowired
	protected PrimesNumbersDAO primesNumberMasterDAO;
	@Autowired
	protected OfficeDAO officeDAO;

	@Autowired
	protected StagingRegistrationDetailsDAO stagingRegistrationDetailsDAO;
	@Autowired
	protected ActionsDetailsHelper actionsDetailsHelper;

	@Autowired
	protected GeneratedPrDetailsDAO generatedPrDetailsDAO;


	@Autowired
	protected DuplicatePrNumberDAO duplicatePrNumberDAO;



	@Autowired
	protected PrNumberAssignDetailsConfigDAO prNumberAssignDetailsConfigDAO;
	
	@Autowired
	protected PrNumberAssignDetailsConfigLogDAO prNumberAssignDetailsConfigLogDAO;
	
	@Autowired
	protected  SnPrSeriesDAO snPrSeriesDAO;
	
	public abstract String geneatePrNo(PrGenerationVO prGenVO);
	
	
	
	protected List<NumberPoolStatus> getPoolStatus(boolean isRequiredReopenStatus) {
		List<NumberPoolStatus> poolStatus = new ArrayList<>();
		poolStatus.add(NumberPoolStatus.OPEN);
		if(isRequiredReopenStatus) {
			poolStatus.add(NumberPoolStatus.REOPEN);
		}
		return poolStatus;
	}
	protected void saveGeneratedPrDetails(StagingRegistrationDetailsDTO stagingRegDetails, String prNo, String source ,
			boolean fromCitizen,RegServiceDTO regServiceDetails) {

		GeneratedPrDetailsDTO generatedPrDetailsDTO = new GeneratedPrDetailsDTO();
		generatedPrDetailsDTO.setCreatedDate(LocalDateTime.now());
		generatedPrDetailsDTO.setPrNo(prNo);
		generatedPrDetailsDTO.setSource(source);

		if(fromCitizen) {
			generatedPrDetailsDTO.setApplicationNo(regServiceDetails.getApplicationNo());
			generatedPrDetailsDTO.setName(regServiceDetails.getRegistrationDetails().getApplicantDetails().getDisplayName());
			generatedPrDetailsDTO.setOfficeCode(regServiceDetails.getOfficeDetails().getOfficeCode());
			generatedPrDetailsDTO.setTrNo(regServiceDetails.getRegistrationDetails().getTrNo());
		}else {
			generatedPrDetailsDTO.setApplicationNo(stagingRegDetails.getApplicationNo());
			generatedPrDetailsDTO.setName(stagingRegDetails.getApplicantDetails().getDisplayName());
			generatedPrDetailsDTO.setOfficeCode(stagingRegDetails.getOfficeDetails().getOfficeCode());
			generatedPrDetailsDTO.setTrNo(stagingRegDetails.getTrNo());
			generatedPrDetailsDAO.save(generatedPrDetailsDTO);
		}

	}



	/**
	 * @param officeDTO
	 * @param ownerTypeEnum 
	 * @return
	 */
	protected boolean requiredToSkipReopenStatusValidation(OfficeDTO officeDTO, OwnerTypeEnum ownerTypeEnum) {
		boolean isRequiredReopenStatus = false;
		String officeCode = officeDTO.getOfficeCode();
		if(ownerTypeEnum.equals(OwnerTypeEnum.Stu)) {
			officeCode = officeDTO.getReportingoffice();
		}
		Optional<PrNumberAssignDetailsConfigDTO> prNumberAssignDetailsConfigDTO = prNumberAssignDetailsConfigDAO.findByOfficeCodeAndStatusTrue(officeCode);
		if(prNumberAssignDetailsConfigDTO.isPresent()){
			Optional<PrNumberAssignDetailsConfigLogDTO> prNumberAssignDetailsConfigLogDTO = prNumberAssignDetailsConfigLogDAO.findByOfficeCodeAndCurrentDate(officeCode, LocalDate.now());
			if(prNumberAssignDetailsConfigLogDTO.isPresent()){
				if(prNumberAssignDetailsConfigLogDTO.get().getCurrentNumber()<=prNumberAssignDetailsConfigDTO.get().getMaxLimit()){
					prNumberAssignDetailsConfigLogDTO.get().setCurrentNumber(prNumberAssignDetailsConfigLogDTO.get().getCurrentNumber()+1);
					prNumberAssignDetailsConfigLogDAO.save(prNumberAssignDetailsConfigLogDTO.get());
					isRequiredReopenStatus = true;
				}
			}else{
				PrNumberAssignDetailsConfigLogDTO inputDTO = new PrNumberAssignDetailsConfigLogDTO();
				inputDTO.setOfficeCode(officeCode);
				inputDTO.setCurrentNumber(1);
				inputDTO.setCurrentDate(LocalDate.now());
				isRequiredReopenStatus = true;
				prNumberAssignDetailsConfigLogDAO.save(inputDTO);
			}
		}
		return isRequiredReopenStatus;
	}

	// TOD:need to change logic
	protected boolean isPrimeNumber(Integer number) {
		// Reading Details from prime number collection
		List<PremiumNumbersDTO> numberStatusOptional = primesNumberMasterDAO.findAll();
		return numberStatusOptional.stream().anyMatch(prNo -> prNo.getPrimeNumber().equals(number));
	}

	protected static String appendZero(Integer number, int length) {
		return String.format("%0" + (length) + "d", number);
	}
	
	
	
	

	
}
