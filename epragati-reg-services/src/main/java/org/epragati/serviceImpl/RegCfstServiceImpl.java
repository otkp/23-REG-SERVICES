package org.epragati.serviceImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.common.dao.ErrorTrackLogDAO;
import org.epragati.common.dto.ErrorTrackLogDTO;
import org.epragati.constants.MessageKeys;
import org.epragati.constants.Schedulers;
import org.epragati.master.dao.FcDetailsDAO;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.TaxDetailsDAO;
import org.epragati.master.dao.UserDAO;
import org.epragati.master.dto.FcDetailsDTO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.TaxComponentDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.master.dto.UserDTO;
import org.epragati.master.mappers.CfstFcDetailsMapper;
import org.epragati.master.mappers.CfstSyncRegstrationMapper;
import org.epragati.master.mappers.PermitDetailsMapper;
import org.epragati.master.mappers.UserMapper;
import org.epragati.master.vo.CfstSyncRegstrationVO;
import org.epragati.master.vo.UserVO;
import org.epragati.payment.dto.PaymentTransactionDTO;
import org.epragati.payments.dao.PaymentTransactionDAO;
import org.epragati.permits.dto.PermitDetailsDTO;
import org.epragati.permits.service.PermitValidationsService;
import org.epragati.registration.service.RegistrationMigrationSolutionsService;
import org.epragati.service.CfstSyncService;
import org.epragati.service.RegCfstService;
import org.epragati.util.AppMessages;
import org.epragati.util.CfstStatusTypes;
import org.epragati.util.payment.PayStatusEnum;
import org.epragati.util.payment.ServiceCodeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class RegCfstServiceImpl implements RegCfstService{

	private static final Logger logger = LoggerFactory.getLogger(RegCfstServiceImpl.class);
	
	
	/** cfst sync autowired **/

	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;
	
	@Autowired
	private CfstSyncRegstrationMapper cfstSyncRegstrationMapper;
	
	@Autowired
	private PaymentTransactionDAO paymentTransactionDAO;
	
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private UserMapper userMapper;
	
	@Autowired
	private FcDetailsDAO fcDetailsDAO;
	
	@Autowired
	private AppMessages appMessages;
	
	@Autowired
	private PermitValidationsService permitValidationsService;
	
	@Autowired
	private CfstFcDetailsMapper cfstFcDetailsMapper;
	
	
	@Autowired
	private PermitDetailsMapper permitDetailsMapper;
	
	@Autowired
	private RegistrationMigrationSolutionsService registrationMigrationSolutionsService;
	
	@Autowired
	private TaxDetailsDAO taxDetailsDAO;
	
	@Autowired
	private ErrorTrackLogDAO errorTrackLogDAO;
	
	@Autowired
	private CfstSyncService cfstSyncService;
	

	// Sync the data from the Registration to CFST

	@Override
	public void docfstSyncProcess(List<String> prNumbers,Boolean value) {

		List<RegistrationDetailsDTO>
		regDetailsList = Collections.emptyList();
		if(value) {
			for (int i = 0; i <= 3; i++) {
				Pageable pageable = new PageRequest(i,1000); Page<RegistrationDetailsDTO>
				rtegistrationDetailsDTOPageList = registrationDetailDAO
				.findByIsCfstSyncFalse(pageable); 

				if(rtegistrationDetailsDTOPageList.hasContent()){
					logger.info("List fetched from DB : {}",rtegistrationDetailsDTOPageList.
							getContent().size()); regDetailsList=
							registrationMigrationSolutionsService.removeInactiveRecordsToList(
									rtegistrationDetailsDTOPageList.getContent().stream().filter(pr->StringUtils.
											isNotBlank(pr.getPrNo()) && pr.getlUpdate()!=null
											).collect(Collectors.toList()));
							logger.info("List after removeInactiveRecords : {}"
									,regDetailsList.size());
							saveRegDataToCfst(regDetailsList);
				}
			}
		}else {
			regDetailsList = registrationDetailDAO.findByPrNoIn(prNumbers);
			saveRegDataToCfst(regDetailsList);
		}
	}
		
		private Optional<TaxDetailsDTO> getTaxDetails(String applicationNo) {
			TaxDetailsDTO dto = new TaxDetailsDTO();
			List<TaxDetailsDTO> taxDetailsList = taxDetailsDAO.findByApplicationNo(applicationNo);
			if (!taxDetailsList.isEmpty()) {
				taxDetailsList.sort((p1, p2) -> p1.getTaxPeriodEnd().compareTo(p2.getTaxPeriodEnd()));
				dto = taxDetailsList.get(taxDetailsList.size()-1);
				taxDetailsList.clear();
				return Optional.of(dto);
			}
			return Optional.empty();
		}
		
		private void getSavedRecordsInCfst(List<CfstSyncRegstrationVO> registrationDetailsVO) {
			if (registrationDetailsVO.size() > 0) {
				Map<String, String> resultMap = cfstSyncService.saveRegDetails(registrationDetailsVO);
					try {
						List<String> cfstSavedRecords = new ArrayList<>();
							for (Entry<String, String> e : resultMap.entrySet()) {
								String key = e.getKey();
								String value = StringUtils.EMPTY;
								if(StringUtils.isNotBlank(e.getValue())){
									value = e.getValue();
								}
								
								if (value.equalsIgnoreCase(CfstStatusTypes.Y.getCode())) {
									cfstSavedRecords.add(key);
								} else {
									logger.info("save failed record in the Cfst {}", key);
									try {
										ErrorTrackLogDTO exceptionlog = new ErrorTrackLogDTO();
										exceptionlog.setCreatedDate(LocalDateTime.now());
										exceptionlog.setError(value);
										exceptionlog.setIsFixed(Boolean.FALSE);
										exceptionlog.setModuleCode(Schedulers.CFSTSYNC.name());
										exceptionlog.setPrNo(key);
										exceptionlog.setContext(Schedulers.CFSTSYNC.name());
										errorTrackLogDAO.save(exceptionlog);
										
									} catch (Exception e1) {
										logger.error("Exception Occured while posting the data to CSFT : {} ", e1.getMessage());
									}
									cfstSavedRecords.add(key);
								}
							}
							statusChangeInReg(cfstSavedRecords);
							resultMap.clear();
						
					} catch (Exception e) {
						logger.info(appMessages.getLogMessage(MessageKeys.SVS_RESULTNOTAVAILABLE), e.getMessage());
						resultMap.clear();
					}

			} else {
				logger.info("No Records Available to Save");
			}
		}
		
		private void statusChangeInReg(List<String> prList) {

			List<RegistrationDetailsDTO> registrationDetailsList= registrationDetailDAO.findByPrNoIn(prList);
			logger.info("records saved in cfst :{}",registrationDetailsList.size());
			for (RegistrationDetailsDTO registrationDetailsDTO : registrationDetailsList) {
				registrationDetailsDTO.setCfstSync(true);
			}
			registrationDetailDAO.save(registrationDetailsList);
			if(!prList.isEmpty()){
			 prList.clear();
			 registrationDetailsList.clear();
			}
		}
		
		private void saveRegDataToCfst(List<RegistrationDetailsDTO>
		  regDetailsList) {

					List<CfstSyncRegstrationVO> registrationDetailsVO = new ArrayList<>();
					  List<ErrorTrackLogDTO> errorRecords = new ArrayList<>();
		                List<RegistrationDetailsDTO> regList = new ArrayList<>();
					for (RegistrationDetailsDTO registrationDetailsDTO : regDetailsList) {
						if(!StringUtils.isNotBlank(registrationDetailsDTO.getPrNo())){
							logger.info(" pr No is empty for application no:{}", registrationDetailsDTO.getApplicationNo());
						}else if(registrationDetailsDTO.getlUpdate()==null || registrationDetailsDTO.getVahanDetails()!=null&&
								registrationDetailsDTO.getVahanDetails().getManufacturedMonthYear()!=null && registrationDetailsDTO.getVahanDetails().getManufacturedMonthYear().length()>9){
							logger.info(" lUpdate is empty for application no / ManufacturedMonthYear in wrong format  :{}", registrationDetailsDTO.getApplicationNo());
							ErrorTrackLogDTO exceptionlog = new ErrorTrackLogDTO();
							exceptionlog.setCreatedDate(LocalDateTime.now());
							exceptionlog.setError("lUpdate is empty for application no / ManufacturedMonthYear in wrong format");
							exceptionlog.setIsFixed(Boolean.FALSE);
							exceptionlog.setModuleCode(Schedulers.CFSTSYNC.name());
							exceptionlog.setPrNo(registrationDetailsDTO.getPrNo());
							exceptionlog.setContext(Schedulers.CFSTSYNC.name());
							errorRecords.add(exceptionlog);
							registrationDetailsDTO.setCfstSync(Boolean.TRUE);
							regList.add(registrationDetailsDTO);
													
						}
						else{
						logger.info("Details of pr No to sent cfst:{}", registrationDetailsDTO.getPrNo());
						CfstSyncRegstrationVO registrationDetails = cfstSyncRegstrationMapper
								.convertEntity(registrationDetailsDTO);
						String status = PayStatusEnum.SUCCESS.getDescription();
						List<PaymentTransactionDTO> paymentTransactionList = paymentTransactionDAO
								.findByPayStatusAndApplicationFormRefNum(status, registrationDetailsDTO.getApplicationNo());
						if (paymentTransactionList != null && !paymentTransactionList.isEmpty()) {
							PaymentTransactionDTO payments = paymentTransactionList.get(paymentTransactionList.size() - 1);
							registrationDetails.setTaxDemandDate(payments.getResponse().getResponseTime());
						}
						if (registrationDetailsDTO.getDealerDetails() != null
								&& registrationDetailsDTO.getDealerDetails().getDealerId() != null) {
							Optional<UserDTO> userDtoDealer = userDAO
									.findByUserId(registrationDetailsDTO.getDealerDetails().getDealerId());
							if (userDtoDealer.isPresent()) {
								UserVO dealerVO = userMapper.requiredFields(userDtoDealer.get());
								registrationDetails.setDealerVO(dealerVO);
							}
						}

						if (registrationDetailsDTO.getFinanceDetails()!=null) {
							Optional<UserDTO> userDtoFinancier = userDAO
									.findByUserId(registrationDetailsDTO.getFinanceDetails().getUserId());
							if (userDtoFinancier.isPresent()) {
								UserVO financierVO = userMapper.requiredFields(userDtoFinancier.get());
								registrationDetails.setFinancierVO(financierVO);
							}
						}
						if (null != registrationDetailsDTO.getVehicleType()
								&& registrationDetailsDTO.getVehicleType().equals("T")) {

							List<FcDetailsDTO> fcDetailsDTO = fcDetailsDAO.findFirst5ByStatusIsTrueAndPrNoOrderByCreatedDateDesc(registrationDetailsDTO.getPrNo());
							if (!CollectionUtils.isEmpty(fcDetailsDTO)) {
															
								FcDetailsDTO fcDetails = fcDetailsDTO.get(fcDetailsDTO.size()-1);
								registrationDetails.setFcDetailsVO(cfstFcDetailsMapper.convertEntity(fcDetails));
							}

							
							Optional<PermitDetailsDTO> permitDetailsDTO =
									permitValidationsService.getPermitDetails(
											registrationDetailsDTO.getPrNo());
							if(permitDetailsDTO.isPresent()){
								registrationDetails.setPermitVO(permitDetailsMapper.
										convertEntity(permitDetailsDTO.get())); }
							
						}
						try {
							Optional<TaxDetailsDTO> taxDetailsDto =	getTaxDetails(registrationDetails.getApplicationNo());
							if(taxDetailsDto.isPresent()){
								for(Map<String, TaxComponentDTO> tax: taxDetailsDto.get().getTaxDetails()){
									
										for(TaxComponentDTO taxComp: tax.values()){
											if(taxComp.getTaxName().equals(ServiceCodeEnum.QLY_TAX.getCode())||taxComp.getTaxName().equals(ServiceCodeEnum.HALF_TAX.getCode()) || 
													taxComp.getTaxName().equals(ServiceCodeEnum.YEAR_TAX.getCode())||taxComp.getTaxName().equals(ServiceCodeEnum.LIFE_TAX.getCode())){
											registrationDetails.setTaxPaymentPeriod(taxComp.getTaxName());
											registrationDetails.setTaxDemandAmount(taxComp.getAmount());
											registrationDetails.setTaxCollectedAmount(taxComp.getAmount());
											if(taxComp.getPenalty()!=null){
												registrationDetails.setTaxPenaltyAmount(taxComp.getPenalty());
												registrationDetails.setTaxCollectedAmount(taxComp.getAmount()+taxComp.getPenalty());
											}
											registrationDetails.setTaxQuarterStartDate(taxComp.getValidityFrom());
											registrationDetails.setTaxValidUpto(taxComp.getValidityTo());
											}
										}
									
								}
								
							}
						} catch (Exception e) {
							logger.info("exception raised in tax details:{}",registrationDetailsDTO.getPrNo());
							registrationDetails.setTaxPaymentPeriod(registrationDetailsDTO.getTaxType());
						
							if(registrationDetailsDTO.getTaxAmount()!=null){
								registrationDetails.setTaxCollectedAmount(registrationDetailsDTO.getTaxAmount().doubleValue());
								registrationDetails.setTaxDemandAmount(registrationDetailsDTO.getTaxAmount().doubleValue());
							}
							registrationDetails.setTaxValidUpto(registrationDetailsDTO.getTaxvalidity());
						}
						registrationDetailsVO.add(registrationDetails);
						}
					}
					 if(CollectionUtils.isNotEmpty(errorRecords)){
		                	errorTrackLogDAO.save(errorRecords);
		                	registrationDetailDAO.save(regList);
		                	errorRecords.clear();
		                	regList.clear();
		                }
					if (!registrationDetailsVO.isEmpty()) {
						logger.info("Registration Details sent to CFST");
						getSavedRecordsInCfst(registrationDetailsVO);
						registrationDetailsVO.clear();
						regDetailsList.clear();
					}
		}
}
