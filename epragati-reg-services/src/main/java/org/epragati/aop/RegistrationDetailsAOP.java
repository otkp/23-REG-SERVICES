package org.epragati.aop;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.beanutils.BeanUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.epragati.common.dto.ApplicantDetails_last5d;
import org.epragati.common.dto.BaseEntity;
import org.epragati.common.dto.Flow_last5d;
import org.epragati.common.dto.RegistrationDetails_last5d;
import org.epragati.common.dto.StagingRegistrationDetails_last5d;
import org.epragati.common.dto.TaxDetails_last5d;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dto.BaseRegistrationDetailsDTO;
import org.epragati.master.dto.PermitDetails_last5d;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.payment.dto.PaymentTransaction_last5d;
import org.epragati.registration.service.RegistrationMigrationSolutionsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
public class RegistrationDetailsAOP {

	private static final Logger logger = LoggerFactory.getLogger(RegistrationDetailsAOP.class);

	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;

	@Autowired
	private RegistrationMigrationSolutionsService registrationMigrationSolutionsService;
	
	@Autowired
	private QueryExecutionService queryExecutionService;

	/*
	 * @Autowired private QueryExceutorDAO queryExceutorDAO;
	 * 
	 * @Autowired private QueryExecutionService queryExecutionService;
	 * 
	 * private Boolean isExecutorEnabled = Boolean.TRUE;
	 * 
	 * @PostConstruct public void isQueryExecutionEnabled(){
	 * if(propertiesDAO.findByIsQueryExecutorEnabledTrue().isPresent()){
	 * isExecutorEnabled = Boolean.TRUE; }else{ isExecutorEnabled = Boolean.FALSE; }
	 * }
	 */

	@Around("execution(public * org.epragati.master.dao.RegistrationDetailDAO.findByPrNo(*))")
	public Optional<RegistrationDetailsDTO> findByPrNoAOP(ProceedingJoinPoint pjp) {
		Object[] args = pjp.getArgs();
		Object regObj = null;
		try {
			regObj = pjp.proceed(args);

			if (regObj instanceof Optional<?>) {
				@SuppressWarnings("rawtypes")
				Optional optinalObj = (Optional) regObj;
				if (!optinalObj.isPresent()) {
					throw new BadRequestException("No records found");
				}
				RegistrationDetailsDTO regDto = (RegistrationDetailsDTO) optinalObj.get();
				List<RegistrationDetailsDTO> registrationDetailsDTOs = registrationDetailDAO
						.findByPrNoIn(Arrays.asList(regDto.getPrNo()));
				return registrationMigrationSolutionsService.removeInactiveRecordsToSongle(registrationDetailsDTOs);
			}
		} catch (Throwable e) {
			logger.error("Exception occured while fetching Registration AOP", e);
		}
		logger.error("Unable to Cast [{}]");
		throw new BadRequestException("No records found");

	}

	/*
	 * @SuppressWarnings({ "rawtypes", "unchecked" })
	 * 
	 * @AfterReturning(pointcut =
	 * "execution(public * org.epragati.master.dao.RegistrationDetailDAO.findByPrNoIn(*))"
	 * , returning="regObj") public List<RegistrationDetailsDTO>
	 * findByPrNoIn(JoinPoint joinpoint, Object regObj) {
	 * if(joinpoint.getSignature().getName().equals("findByPrNoAOP")) { return
	 * (List<RegistrationDetailsDTO>) regObj; } List<RegistrationDetailsDTO>
	 * regDetails = null; if(regObj instanceof List<?> && regObj!=null) { try {
	 * regDetails = (List<RegistrationDetailsDTO>)(List)regObj; return
	 * registrationMigrationSolutionsService.removeInactiveRecordsToList(regDetails)
	 * ; }catch(ClassCastException cse) { logger.error("Unable to Cast [{}]",cse); }
	 * } logger.error("Unable to Cast [{}]"); return null; }
	 */

	/**
	 * Below Asynchronous is used to traked the every request
	 */

	/*
	 * @Before("execution(public * org.epragati.*.*.controller.*.*(*))") public void
	 * trackRequest(JoinPoint joinPoint) {
	 * 
	 * logger.info("AOP for tracking all requests ");
	 * 
	 * }
	 */

	@Async
	public void doprocess(JoinPoint joinPoint) {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes())
				.getRequest();
		logger.info("Request Headers {}", request.getHeaderNames().toString());
	}

	/*
	 * @Pointcut("execution(public * org.springframework.data.repository.Repository+.*(..)) && !execution(public * org.springframework.data.repository.CrudRepository.save(Object))"
	 * ) public void monitor() {}
	 * 
	 * @Around("monitor()") public Object profile(ProceedingJoinPoint pjp) throws
	 * InterruptedException { if(isExecutorEnabled){ long start =
	 * System.currentTimeMillis(); logger.info("JVM memory in use = "+
	 * (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
	 * Object output = null; try { output = pjp.proceed(); } catch (Throwable e) {
	 * logger.info(e.getMessage(), e); } long elapsedTime =
	 * System.currentTimeMillis() - start;
	 * logger.info(pjp.getTarget()+"."+pjp.getSignature()+": Execution time: " +
	 * elapsedTime + " ms. ("+ elapsedTime/60000 + " minutes)");
	 * logger.info(pjp.getSignature()+": Execution time: " + elapsedTime + " ms.");
	 * QueryExecutionDetailsDTO exceutionTime = new QueryExecutionDetailsDTO();
	 * exceutionTime.setExecutedTime(elapsedTime);
	 * exceutionTime.setExecutionStartedTime(LocalDateTime.ofInstant(Instant.
	 * ofEpochMilli(start),TimeZone.getDefault().toZoneId()));
	 * exceutionTime.setExecutionEndTime(LocalDateTime.ofInstant(Instant.
	 * ofEpochMilli(System.currentTimeMillis()),TimeZone.getDefault().toZoneId()));
	 * exceutionTime.setFreeJVMMemory(Runtime.getRuntime().totalMemory() -
	 * Runtime.getRuntime().freeMemory());
	 * exceutionTime.setTotalJVMMemory(Runtime.getRuntime().totalMemory() );
	 * exceutionTime.setQueryExecuted(pjp.getSignature().toLongString());
	 * queryExecutionService.saveQueryExecutionDetails(exceutionTime); return
	 * output;
	 * 
	 * } return pjp;
	 * 
	 * }
	 * 
	 * 
	 * /*@Pointcut(
	 * "org.springframework.data.repository.CrudRepository.save(Object)") public
	 * void lastUpdatedDate() { }
	 * 
	 * @Around("org.springframework.data.repository.CrudRepository.save(Object)")
	 * public Object lastUpdatedDateAOP(ProceedingJoinPoint pjp, LocalDateTime
	 * lupdatedDate) throws InterruptedException {
	 * 
	 * ApplicantDetailsDTO dto = new ApplicantDetailsDTO();
	 * dto.setlUpdate(lupdatedDate); return pjp;
	 * 
	 * }
	 */
	// /############################## This Around Advice is for Setting the lupdate
	// date for all Collections ##########3
	@Around("execution(public * org.springframework.data.repository.CrudRepository.save(Object)) && !execution(public * org.epragati.common.service.LastFiveDaysCollections.save() )")
	public Object saveLUpdatedDateAOP(ProceedingJoinPoint pjp) {

		Object[] args = pjp.getArgs();
		Object regObj = null;
		try {
			BaseEntity dto = new BaseEntity();
			for (int i = 0; i < args.length; i++) {

				if (args[i] instanceof BaseEntity && !((args[i] instanceof ApplicantDetails_last5d)
						|| ((args[i] instanceof PaymentTransaction_last5d)
								|| ((args[i] instanceof PaymentTransaction_last5d) || ((args[i] instanceof Flow_last5d))
										|| ((args[i] instanceof PermitDetails_last5d))
										|| ((args[i] instanceof TaxDetails_last5d)
												|| ((args[i] instanceof RegistrationDetails_last5d)
														|| ((args[i] instanceof StagingRegistrationDetails_last5d)))))))) {
					dto = (BaseEntity) args[i];
					dto.setlUpdate(LocalDateTime.now());
					BeanUtils.copyProperties(args[i], dto);
					args[i] = dto;
				}

				if (args[i] instanceof Optional<?>) {
					dto = (BaseEntity) args[i];
					dto.setlUpdate(LocalDateTime.now());
					BeanUtils.copyProperties(args[i], dto);
					args[i] = dto;
				}
				regObj = pjp.proceed(args);
				return regObj;
			}

		} catch (Throwable e) {
			logger.debug("Exception in REG AOP : [{}]", e);
			logger.error("Exception occured while fetching Registration AOP: [{}]", e.getMessage());
		}
		return regObj;

	}

	@Before("execution(public * org.springframework.data.repository.CrudRepository.save(*))")
	public void before(JoinPoint joinPoint) {
		Object[] args = joinPoint.getArgs();
		Object baseRepositoryObj = null;
		try {
			baseRepositoryObj = args[0];
			if (baseRepositoryObj instanceof RegistrationDetailsDTO
					|| baseRepositoryObj instanceof StagingRegistrationDetailsDTO) {
				return;
			}

			if (baseRepositoryObj instanceof BaseEntity) {
				BaseEntity baseEntity = (BaseEntity) baseRepositoryObj;
				baseEntity.setlUpdate(LocalDateTime.now());

			} else if (baseRepositoryObj instanceof List) {
				@SuppressWarnings("unchecked")
				List<BaseEntity> baseEntityList = (List<BaseEntity>) baseRepositoryObj;
				for (BaseEntity baseEntity : baseEntityList) {
					if (baseEntity instanceof BaseRegistrationDetailsDTO) {
						return;
					}
					baseEntity.setlUpdate(LocalDateTime.now());
				}

			}
		} catch (Throwable e) {
			logger.error("Exception while lUpdate field updating", e);
		}

	}
	@After("execution(public * org.epragati.master.dao.RegistrationDetailDAO.save(*))")
	public void saveAfterRegDetails(JoinPoint joinPoint) {
		logger.debug("Reg AOP Initiated ");
		Object[] obj = joinPoint.getArgs();
		if (obj != null) {
			Object regDTO = obj[0];
			if (regDTO != null && regDTO instanceof RegistrationDetailsDTO) {
				RegistrationDetailsDTO dto = (RegistrationDetailsDTO) regDTO;
				queryExecutionService.saveRegDetailsReport(dto);
				}
		}

	}

}
