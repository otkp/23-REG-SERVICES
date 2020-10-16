package org.epragati.master.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.epragati.common.service.CommonService;
import org.epragati.common.service.impl.SyncroServiceFactory;
import org.epragati.common.vo.PrGenerationInput;
import org.epragati.constants.MessageKeys;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.PrSeriesDAO;
import org.epragati.master.dao.StagingRegistrationDetailsDAO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.dto.PrSeriesDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.service.PrSeriesService;
import org.epragati.restGateway.RestGateWayService;
import org.epragati.rta.service.impl.service.RTAService;
import org.epragati.rta.vo.PrGenerationVO;
import org.epragati.sn.dao.BidConfigMasterDAO;
import org.epragati.sn.dto.BidConfigMaster;
import org.epragati.sn.mappers.BidConfigMasterMapper;
import org.epragati.sn.vo.BidConfigMasterVO;
import org.epragati.util.AppMessages;
import org.epragati.util.NumberPoolStatus.NumberAssignType;
import org.epragati.util.StatusRegistration;
import org.epragati.util.payment.ModuleEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


/**
 * @author sairam.cheruku
 *
 */
@Service
public class PrSeriesServiceImpl implements PrSeriesService {

	private static final Logger logger = LoggerFactory.getLogger(PrSeriesServiceImpl.class);

	@Autowired
	private AppMessages appMessages;

	@Autowired
	private PrSeriesDAO prSeriesDAO;

	@Autowired
	private StagingRegistrationDetailsDAO stagingRegistrationDetailsDAO;

	@Autowired
	private RTAService rtaService;

	@Autowired
	private RestGateWayService restGateWayService;
	
	@Autowired
	private BidConfigMasterDAO bidConfigMasterDAO;

	@Autowired
	private BidConfigMasterMapper bidConfigMasterMapper;
	
	private Optional<BidConfigMasterVO> bidConfigMasterOPT=Optional.empty();
	
	@Autowired
	private SyncroServiceFactory syncroServiceFactory;
	
	@Value("${reg.dealer.prGeneration.url.isSameServer:false}")
	private boolean prGenUrlIsSameServer;
	
	@PostConstruct
	private void init() {
		bidConfigMasterOPT=getActiveBidConfigDetails();
	}

	
	@Override
	public String geneatePrSeries(Integer prDistrictId) {

		if (prDistrictId == null) {
			logger.error("District Id is required to generate prSeries");
			throw new BadRequestException("District Id is required to generate prSeries");
		}
		Optional<PrSeriesDTO> prSeriesOptionalDTO;
		StringBuilder prSeries = new StringBuilder();
		final String distIdString= prDistrictId.toString();
		// To avoid multi thread issues.
		synchronized (distIdString.intern()) {

			// 1. read Sequence No based on DistrictId
			prSeriesOptionalDTO = prSeriesDAO.findByPrDistrictId(prDistrictId);

			if (!prSeriesOptionalDTO.isPresent()) {
				logger.error(appMessages.getResponseMessage(MessageKeys.MESSAGE_NO_RECORD_FOUND));
				throw new BadRequestException(appMessages.getResponseMessage(MessageKeys.MESSAGE_NO_RECORD_FOUND));
			}
			boolean flag = true;

			PrSeriesDTO prSeriesDTO = prSeriesOptionalDTO.get();
			// read CurrentNumber
			Integer currentNumber = prSeriesDTO.getCurrentNumber();
			// read series
			String series = prSeriesDTO.getSeries();
			// append series
			prSeries.append(series);
			// read StartFrom
			Integer start = prSeriesDTO.getStartFrom();
			// read EndTo
			Integer end = prSeriesDTO.getEndTo();
			// special numbers
			List<Integer> specialNumberSeris = new ArrayList<>();

			specialNumberSeris.add(99);
			specialNumberSeris.add(100);
			specialNumberSeris.add(111);
			specialNumberSeris.add(112);
			specialNumberSeris.add(113);
			specialNumberSeris.add(11);
			specialNumberSeris.add(22);
			specialNumberSeris.add(23);
			specialNumberSeris.add(24);
			specialNumberSeris.add(25);

			currentNumber = currentNumber + 1;
			while (flag) {

				if (specialNumberSeris.contains(currentNumber)) {
					currentNumber = currentNumber + 1;
					continue;
				}

				if (currentNumber > end) {
					prSeriesDTO.getFinishedSeries().add(prSeriesDTO.getPendingSeries().get(0));
					prSeriesDTO.getPendingSeries().remove(0);

					if (prSeriesDTO.getPendingSeries().isEmpty()) {
						logger.error(appMessages.getResponseMessage(MessageKeys.UNABLE_TO_FIND_NEXT_PRSERIES));
						throw new BadRequestException(
								appMessages.getResponseMessage(MessageKeys.UNABLE_TO_FIND_NEXT_PRSERIES));
					}
					currentNumber = start;
					// uPDATE sERIES
					series = prSeriesDTO.getSeries();
				}
				flag = false;
			}

			prSeriesDTO.setPendingSeries(prSeriesDTO.getPendingSeries());

			prSeriesDTO.setCurrentNumber(currentNumber);

			// update
			updateCurrentNo(prSeriesDTO);

			prSeries.append(prSeriesDTO.getPendingSeries().get(0));
			prSeries.append(currentNumber);

		}
		return prSeries.toString();
	}

	@Override
	public PrSeriesDTO updateCurrentNo(PrSeriesDTO prSeriesDTO) {
		PrSeriesDTO prSeries = null;
		prSeries = prSeriesDAO.save(prSeriesDTO);
		return prSeries;
		// TODO Auto-generated method stub

	}

	@Override
	public List<PrSeriesDTO> modifyData() {

		String series = null;

		List<PrSeriesDTO> dto = prSeriesDAO.findAll();
		for (PrSeriesDTO dto1 : dto) {
			series = dto1.getSeries();
			String x = series.replace(series.substring(4, 6), "");
			dto1.setSeries(x);
		}
		prSeriesDAO.save(dto);
		return dto;
	}

	@Override
	public String processPrForSP(PrGenerationInput prGenerationInput) {
		Optional<StagingRegistrationDetailsDTO> stagingOptional=null;
		if(prGenerationInput.getIsDoByOldPrNo()) {
			stagingOptional = stagingRegistrationDetailsDAO
					.findByOldPrNoAndApplicantDetailsContactMobile(prGenerationInput.getTrNumber(),prGenerationInput.getMobileNo());
		}else {
			stagingOptional = stagingRegistrationDetailsDAO
				.findByTrNo(prGenerationInput.getTrNumber());
		}
		
		if (stagingOptional.isPresent()) {
			if (prGenerationInput.isBlockNo()) {
				return geneatePrNo(stagingOptional.get().getApplicationNo(), prGenerationInput.getSelectedNo(),
						prGenerationInput.isBlockNo(),prGenerationInput.getPrSeries(),prGenerationInput.getModule(),Optional.of(stagingOptional.get().getOfficeDetails()));
			} else {
				try {
					if(this.isAssignNumberNow() || StringUtils.isNoneBlank(stagingOptional.get().getPrNo())) {
						rtaService.assignPR(stagingOptional.get());
					}else {
						stagingOptional.get().setApplicationStatus(StatusRegistration.PRNUMBERPENDING.getDescription());
						stagingRegistrationDetailsDAO.save(stagingOptional.get());
					}
				} catch (CloneNotSupportedException e) {
					logger.debug("Exception while assign application NO: {}, and  Exp {}",stagingOptional.get().getApplicationNo());

					logger.error("Exception while assign application NO: {}, and  Exp {}",stagingOptional.get().getApplicationNo());
				}
			}
		} else {
			logger.error("Application not found: [{}]" ,prGenerationInput.getTrNumber());
			throw new BadRequestException("Application not found: " + prGenerationInput.getTrNumber());
		}
		return null;
	}

	@Override
	public String geneatePrNo(String applicationNo, Integer selectedNo, boolean isNumberlocked, String prSeries,
			ModuleEnum citizen, Optional<OfficeDTO> officeDetails) { 
		PrGenerationVO prGenVO= new PrGenerationVO();
		prGenVO.setApplicationNo(applicationNo);
		prGenVO.setNumberlocked(isNumberlocked);
		prGenVO.setPrSeries(prSeries);
		prGenVO.setSelectedNo(selectedNo);
		prGenVO.setCitizen(citizen);
		if(prGenUrlIsSameServer) {
			CommonService commonService = syncroServiceFactory.getCommonServiceInst();
			return commonService.geneatePrNo(prGenVO);
			 
		}
		return restGateWayService.generatePrNo(prGenVO);
		
	}
	
	@Override
	public Optional<BidConfigMasterVO> getBidConfigMasterData(Boolean isRequiredDynamicData) {
		if(isRequiredDynamicData) {
			return this.getActiveBidConfigDetails();
		}
		if( !bidConfigMasterOPT.isPresent()) {
			bidConfigMasterOPT= this.getActiveBidConfigDetails();
		}
		return this.bidConfigMasterOPT;
	}
	/**
	 * return false for assign numbers through schedulers
	 */
	@Override
	public boolean isAssignNumberNow() {
		Optional<BidConfigMasterVO> resultOptional = this.getBidConfigMasterData(false);
		return (!resultOptional.isPresent() ||!NumberAssignType.SCHEDULER.equals(resultOptional.get().getNumberAssignType())); 
	}
	
	private Optional<BidConfigMasterVO> getActiveBidConfigDetails() {
		String status = "Active";
		Optional<BidConfigMaster> bidConfigMasterOptional = bidConfigMasterDAO.findByConfigStatus(status);

		Optional<BidConfigMasterVO> opt=bidConfigMasterMapper.convertEntity(bidConfigMasterOptional);
		if(!opt.isPresent()) {
			throw new BadRequestException("Bid mater config data not found");
		}
		 return opt;
	}

	
	
	
	
}
