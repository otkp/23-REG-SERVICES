package org.epragati.master.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.epragati.common.dto.FlowDTO;
import org.epragati.constants.OwnerTypeEnum;
import org.epragati.elastic.vo.RtaSearchResponse;
import org.epragati.exception.BadRequestException;
import org.epragati.images.service.ImagesService;
import org.epragati.master.dao.FinanceDetailsDAO;
import org.epragati.master.dao.MasterTaxFuelTypeExcemptionDAO;
import org.epragati.master.dao.RegServiceDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.StagingRegistrationDetailsDAO;
import org.epragati.master.dto.ActionDetailsDTO;
import org.epragati.master.dto.FinanceDetailsDTO;
import org.epragati.master.dto.MasterAmountSecoundCovsDTO;
import org.epragati.master.dto.MasterTaxFuelTypeExcemptionDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.RoleActionDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.mappers.StagingRegistrationDetailsMapper;
import org.epragati.master.service.MasterAmountSecoundCovsService;
import org.epragati.master.service.StagingRegistrationDetailsSerivce;
import org.epragati.master.vo.RegistrationDetailsVO;
import org.epragati.master.vo.StagingRegistrationDetailsVO;
import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.util.AppMessages;
import org.epragati.util.StatusRegistration;
import org.epragati.util.payment.ServiceEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service

public class StagingRegistrationDetailsSerivceImpl implements StagingRegistrationDetailsSerivce {
	private static final Logger logger = LoggerFactory.getLogger(StagingRegistrationDetailsSerivceImpl.class);
	@Autowired
	private StagingRegistrationDetailsDAO stagingRegistrationDetailsDAO;
	@Autowired
	private AppMessages appMessages;

	@Autowired
	private ImagesService magesService;

	@Autowired
	private StagingRegistrationDetailsMapper stagingRegistrationDetailsMapper;

	@Autowired
	private FinanceDetailsDAO financeDetailsDAO;

	@Autowired
	private RegServiceDAO regServiceDAO;

	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;

	@Autowired
	private MasterAmountSecoundCovsService masterAmountSecoundCovsService;

	@Autowired
	private MasterTaxFuelTypeExcemptionDAO masterTaxFuelTypeExcemptionDAO;

	@Override
	public Optional<StagingRegistrationDetailsDTO> FindbBasedOnApplicationNo(String applicationNo) {
		Optional<StagingRegistrationDetailsDTO> stagingRegistrationOptional;
		stagingRegistrationOptional = stagingRegistrationDetailsDAO.findByApplicationNo(applicationNo);
		if (stagingRegistrationOptional.isPresent()) {
			logger.debug("registration details found for applicationNo: [{}]", applicationNo);
			return stagingRegistrationOptional;
		}
		Optional<RegServiceDTO> osSecondVechile = regServiceDAO.findByApplicationNo(applicationNo);
		if (osSecondVechile.isPresent() && osSecondVechile.get().getServiceIds() != null
				&& osSecondVechile.get().getServiceIds().stream()
						.anyMatch(id -> id.equals(ServiceEnum.DATAENTRY.getId()))
				&& !osSecondVechile.get().getRegistrationDetails().isRegVehicleWithPR()) {
			StagingRegistrationDetailsDTO stagingRegistrationDetails = new StagingRegistrationDetailsDTO();
			BeanUtils.copyProperties(osSecondVechile.get().getRegistrationDetails(), stagingRegistrationDetails);
			stagingRegistrationDetails.setIteration(1);
			return Optional.of(stagingRegistrationDetails);
		}

		return Optional.empty();
	}

	@Override
	public Optional<StagingRegistrationDetailsDTO> fLowLogs(String appplicationNo) {
		Optional<StagingRegistrationDetailsDTO> stagingRegistrationOptional;

		Integer stage_no = 1;
		stagingRegistrationOptional = FindbBasedOnApplicationNo(appplicationNo);
		if (stagingRegistrationOptional.isPresent()) {
			StagingRegistrationDetailsDTO stagingRegistrationDetailsDTO = stagingRegistrationOptional.get();
			List<FlowDTO> list = flowView(stage_no, stagingRegistrationDetailsDTO);

			stagingRegistrationDetailsDTO.setFlowDetailsLog(list);
			return Optional.of(stagingRegistrationDetailsDTO);
		}
		return Optional.empty();
	}

	public List<FlowDTO> flowView(Integer stage_no, StagingRegistrationDetailsDTO stagingRegistrationDetailsDTO) {
		StagingRegistrationDetailsDTO registrationDTO = null;
		Optional<StagingRegistrationDetailsDTO> registrationOption;
		String CCO = "A";
		String MVI = "A";
		String AO = "A";
		String DTC = "R";
		FlowDTO flowDTO = new FlowDTO();
		List<RoleActionDTO> flowList = new ArrayList<RoleActionDTO>();
		List<FlowDTO> list = new ArrayList<FlowDTO>();
		Map<Integer, List<String>> map = new HashMap<Integer, List<String>>();
		Map<Integer, List<RoleActionDTO>> flowDetailsMap = new HashMap<Integer, List<RoleActionDTO>>();
		RoleActionDTO roleAction = new RoleActionDTO();

		List<String> Stage1_List = new ArrayList<String>();
		List<String> Stage2_List = new ArrayList<String>();
		List<String> Stage3_List = new ArrayList<String>();
		Stage1_List.add("CCO");
		Stage1_List.add("MVI");
		Stage2_List.add("AO");
		Stage3_List.add("DTC");
		map.put(0, Stage1_List);
		map.put(1, Stage2_List);
		map.put(2, Stage3_List);
		switch (stage_no) {
		case 1:
			if (CCO.equals("A")) {
				roleAction.setRole(Stage1_List.get(0));
				roleAction.setAction(CCO);
				flowList.add(roleAction);
				flowDetailsMap.put(1, flowList);
				flowDTO.setFlowDetails(flowDetailsMap);
				stagingRegistrationDetailsDTO.setFlowDetails(flowDTO);
				registrationDTO = stagingRegistrationDetailsDAO.save(stagingRegistrationDetailsDTO);
				registrationOption = FindbBasedOnApplicationNo(registrationDTO.getApplicationNo());
				list.add(registrationOption.get().getFlowDetails());
				Stage1_List.remove(0);
				if (MVI.equals("A")) {
					flowList.clear();
					roleAction.setRole(Stage1_List.get(0));
					roleAction.setAction(MVI);
					flowList.add(roleAction);
					flowDetailsMap.put(1, flowList);
					flowDTO.setFlowDetails(flowDetailsMap);
					stagingRegistrationDetailsDTO.setFlowDetails(flowDTO);
					registrationDTO = stagingRegistrationDetailsDAO.save(stagingRegistrationDetailsDTO);
					registrationOption = FindbBasedOnApplicationNo(registrationDTO.getApplicationNo());
					list.add(registrationOption.get().getFlowDetails());
					Stage1_List.isEmpty();
				} else
					break;
			}
		case 2:
			if (AO.equals("A")) {
				flowList.clear();
				flowDetailsMap.remove(1);
				roleAction.setRole(Stage2_List.get(0));
				roleAction.setAction(AO);
				flowList.add(roleAction);
				flowDetailsMap.put(2, flowList);
				flowDTO.setFlowDetails(flowDetailsMap);
				stagingRegistrationDetailsDTO.setFlowDetails(flowDTO);
				registrationDTO = stagingRegistrationDetailsDAO.save(stagingRegistrationDetailsDTO);
				registrationOption = FindbBasedOnApplicationNo(registrationDTO.getApplicationNo());
				list.add(registrationOption.get().getFlowDetails());

			}
		case 3:
			if (DTC.equals("A")) {
				flowList.clear();
				flowDetailsMap.remove(2);
				roleAction.setRole(Stage3_List.get(0));
				roleAction.setAction(DTC);
				flowList.add(roleAction);
				flowDetailsMap.put(1, flowList);
				flowDTO.setFlowDetails(flowDetailsMap);
				stagingRegistrationDetailsDTO.setFlowDetails(flowDTO);
				registrationDTO = stagingRegistrationDetailsDAO.save(stagingRegistrationDetailsDTO);
				registrationOption = FindbBasedOnApplicationNo(registrationDTO.getApplicationNo());
				list.add(registrationOption.get().getFlowDetails());

			} else
				break;
		default: {
		}

		}

		return list;
	}

	public List<RoleActionDTO> role(String action, String applicationNo, String module, String role) {
		List<RoleActionDTO> list = new ArrayList<RoleActionDTO>();
		RoleActionDTO roleActionDto = new RoleActionDTO();
		roleActionDto.setAction(action);
		roleActionDto.setActionTime(LocalDateTime.now());
		roleActionDto.setApplicatioNo(applicationNo);
		roleActionDto.setModule("DL");
		roleActionDto.setRole(role);

		list.add(roleActionDto);
		return list;
	}

	@Override
	public Optional<StagingRegistrationDetailsVO> getTrDetailByApplicationNo(String applicationNo) {
		Optional<StagingRegistrationDetailsDTO> stagingRegistrationOptional;
		stagingRegistrationOptional = stagingRegistrationDetailsDAO.findByApplicationNo(applicationNo);
		if (stagingRegistrationOptional.isPresent()) {
			Optional<StagingRegistrationDetailsDTO> resultOpt = stagingRegistrationOptional;
			if (!resultOpt.get().getApplicationStatus().equals(StatusRegistration.TRGENERATED.getDescription())) {
				logger.error("Application Is not generated TR number.");
				throw new BadRequestException("Application Is not generated TR number.");
			}
			StagingRegistrationDetailsVO returnValue = new StagingRegistrationDetailsVO();
			returnValue.setTrNo(resultOpt.get().getTrNo());
			return Optional.of(returnValue);
		}
		return Optional.empty();
	}

	@Override
	public String validateFinacerToken(String applicationNo) {
		Optional<StagingRegistrationDetailsDTO> stagingRegistrationOptional;
		stagingRegistrationOptional = stagingRegistrationDetailsDAO.findByApplicationNo(applicationNo);
		if (stagingRegistrationOptional.isPresent() && null != stagingRegistrationOptional.get().getFinanceDetails()
				&& null != stagingRegistrationOptional.get().getFinanceDetails().getStatus()) {
			return stagingRegistrationOptional.get().getFinanceDetails().getStatus();
		}
		return null;
	}

	@Override
	public boolean dealerStatusUpdationForFinancerApproval(String applicationNo, Boolean status, String user) {
		Optional<StagingRegistrationDetailsDTO> stagingRegistrationOptional;
		stagingRegistrationOptional = stagingRegistrationDetailsDAO.findByApplicationNo(applicationNo);
		if (stagingRegistrationOptional.isPresent()) {
			if (status) {
				stagingRegistrationOptional.get().getFinanceDetails()
						.setStatus(StatusRegistration.DEALERTOKENAPPROVED.getDescription());
			} else {
				stagingRegistrationOptional.get().getFinanceDetails()
						.setStatus(StatusRegistration.DEALERREJECTED.getDescription());
				updateFinancerHistory(user, stagingRegistrationOptional);
				stagingRegistrationOptional.get().setFinanceDetails(null);
				stagingRegistrationOptional.get().setIsFinancier(false);
			}
			stagingRegistrationDetailsDAO.save(stagingRegistrationOptional.get());
			return true;
		}
		return false;
	}

	/**
	 * @param user
	 * @param stagingRegistrationOptional
	 */
	private void updateFinancerHistory(String user,
			Optional<StagingRegistrationDetailsDTO> stagingRegistrationOptional) {
		FinanceDetailsDTO financeDetailsHistoryDTO = stagingRegistrationOptional.get().getFinanceDetails();
		ActionDetailsDTO actionDetailsDTO = new ActionDetailsDTO();
		actionDetailsDTO.setActionBy(user);
		actionDetailsDTO.setCreatedDate(LocalDateTime.now());
		financeDetailsHistoryDTO.setActionDetailsDTO(actionDetailsDTO);
		financeDetailsDAO.save(financeDetailsHistoryDTO);
	}

	@Override
	public void saveTax(StagingRegistrationDetailsDTO taxDetails) {
		stagingRegistrationDetailsDAO.save(taxDetails);

	}

	@Override
	public RegistrationDetailsVO getRegistrationDetailWithEnclosuersByApplicationNo(String applicationNo) {

		logger.debug("Registration Details with Enclosures By Application Id :[{}]", applicationNo);

		Optional<StagingRegistrationDetailsDTO> resultOpt = stagingRegistrationDetailsDAO
				.findByApplicationNo(applicationNo);
		if (resultOpt.isPresent()) {
			RegistrationDetailsVO result = (RegistrationDetailsVO) stagingRegistrationDetailsMapper
					.convertEntity(resultOpt).get();
			// set reupload images
			result.setImageDetailsList(magesService.getListOfSupportedEnclosuresForMobile(applicationNo));
			return result;
		}
		return null;
	}

	@Override
	public Optional<StagingRegistrationDetailsDTO> findbBasedOnApplicationNo(String applicationFormNo) {
		Optional<StagingRegistrationDetailsDTO> stagingRegistrationOptional;
		stagingRegistrationOptional = stagingRegistrationDetailsDAO.findByApplicationNo(applicationFormNo);
		if (stagingRegistrationOptional.isPresent()) {
			return stagingRegistrationOptional;
		}
		return Optional.empty();
	}

	@Override
	public StagingRegistrationDetailsDTO findApplicationDetailsByTrNo(String trNo) {
		Optional<StagingRegistrationDetailsDTO> stagingRegistrationOptional;
		StagingRegistrationDetailsDTO stagingRegistrationDetailsDTO = null;
		stagingRegistrationOptional = stagingRegistrationDetailsDAO.findByTrNo(trNo);

		if (!stagingRegistrationOptional.isPresent()) {
			Optional<RegistrationDetailsDTO> registrationDetails = registrationDetailDAO.findByTrNo(trNo);
			if (!registrationDetails.isPresent()) {
				throw new BadRequestException("Applicant info is not present for TR no::: " + trNo);
			}
			stagingRegistrationDetailsDTO = (StagingRegistrationDetailsDTO) registrationDetails.get();
		} else {
			stagingRegistrationDetailsDTO = stagingRegistrationOptional.get();
		}
		return stagingRegistrationDetailsDTO;
	}

	@Override
	public RtaSearchResponse secondVehicleApplicabale(String applicationNo) {
		// TODO Auto-generated method stub

		Optional<StagingRegistrationDetailsDTO> stagingOptional = FindbBasedOnApplicationNo(applicationNo);
		StagingRegistrationDetailsDTO dto = null;
		RtaSearchResponse responseVO = new RtaSearchResponse();
		responseVO.setSecondvehicleisnotrequired(true);
		Optional<MasterAmountSecoundCovsDTO> masterAmountSecoundCovsOpt = null;
		if (!stagingOptional.isPresent()) {
			logger.info("No record found in staging with appNo [{}]", applicationNo);
		}
		dto = stagingOptional.get();
		if (dto.getIteration() != 1) {
			logger.info("secondVehilce search disabled at iteration[{}]", stagingOptional.get().getIteration());
			return responseVO;
		}
		List<MasterTaxFuelTypeExcemptionDTO> list = masterTaxFuelTypeExcemptionDAO.findAll();
		if (list.stream().anyMatch(type -> type.getFuelType().stream()
				.anyMatch(fuel -> fuel.equalsIgnoreCase(stagingOptional.get().getVahanDetails().getFuelDesc())))) {
			MasterTaxFuelTypeExcemptionDTO taxFuelTypeDto = list.stream().findFirst().get();
			Integer noOfYears = taxFuelTypeDto.getNoOfYears().get(dto.getVahanDetails().getFuelDesc());
			if (noOfYears == 0) {
				logger.info("secondVehilce search disabled for fuel exception[{}]",
						dto.getVahanDetails().getFuelDesc());
				return responseVO;
			}
		}
		if (!dto.getOfficeDetails().getOfficeCode().equalsIgnoreCase("Other") && dto.getIsFirstVehicle()
				&& dto.getVehicleType().equalsIgnoreCase("N") && dto.getOwnerType().equals(OwnerTypeEnum.Individual)) {
			logger.info("Second vehicle search : staging officeCode [{}] isFirst vehicle [{}] vehicleType [{}] ",
					dto.getOfficeDetails().getOfficeCode(), dto.getIsFirstVehicle(), dto.getVehicleType());

			if (dto.getInvoiceDetails().getInvoiceValue() <= 999999.0) {

				masterAmountSecoundCovsOpt = masterAmountSecoundCovsService.findByCovCode(dto.getClassOfVehicle());

				if (masterAmountSecoundCovsOpt.isPresent()) {
					logger.info("second vehicle search is required for cov [{}]", dto.getClassOfVehicle());
					responseVO.setSecondvehicleisnotrequired(false);
					return responseVO;
				} else {
					logger.info("second vehicle search is not required for cov [{}]", dto.getClassOfVehicle());
					return responseVO;
				}
			} else {
				logger.info("invoice amount in staging [{}]", dto.getInvoiceDetails().getInvoiceValue());
				return responseVO;
			}
		} else {
			logger.info("Second vehicle search : staging officeCode [{}] isFirst vehicle [{}] vehicleType [{}] ",
					dto.getOfficeDetails().getOfficeCode(), dto.getIsFirstVehicle(), dto.getVehicleType());
			return responseVO;
		}
	}

}
