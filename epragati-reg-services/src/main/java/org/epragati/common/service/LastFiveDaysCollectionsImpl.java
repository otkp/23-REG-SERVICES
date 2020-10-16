package org.epragati.common.service;

import org.springframework.stereotype.Service;

@Service
public class LastFiveDaysCollectionsImpl implements LastFiveDaysCollections {

	@Override
	public void save() throws Exception {
		// TODO Auto-generated method stub
		
	}

	/*private static final Logger logger = LoggerFactory.getLogger(LastFiveDaysCollectionsImpl.class);
	@Autowired
	private ApplicantDetailsDAO applicantDetailsDAO;
	@Autowired
	private FiveDayApplicantDetailsDAO fapplicantDetailsDAO;

	@Autowired
	private PaymentTransactionDAO paymentTransactionDAO;

	@Autowired
	private FlowDAO flowDAO;
	@Autowired
	private PermitDetailsDAO permitDetailsDAO;

	@Autowired
	private TaxDetailsDAO taxDetailsDAO;

	@Autowired
	private FTaxDetailsDAO ftaxDetailsDAO;

	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;

	@Autowired
	private StagingRegistrationDetailsDAO stagingRegistrationDetailsDAO;

	@Autowired
	private LastFiveDaysCollectionsDAO dao;

	@Autowired
	private FiveDayPaymentTransactionDAO fpaymentTransactionDAO;

	@Autowired
	private FiveDayFlowDAO fflowDAO;

	@Autowired
	private FiveDayPermitDetailsDAO fpermitDetailsDAO;

	@Autowired
	private FiveDayRegistrationDetailDAO fregistrationDetailDAO;

	@Autowired
	private FiveDayStagingRegistrationDetailsDAO fstagingRegistrationDetailsDAO;

	@Override
	public void save() throws Exception {

		LocalDateTime fromDate = LocalDateTime.now().minusDays(5);
		LocalDateTime toDate = LocalDateTime.now();

		List<ApplicantDetailsDTO> applicantDetails = applicantDetailsDAO
				.findAllByLUpdateBetween(LocalDateTime.now().minusDays(5), LocalDateTime.now());

		applicantDetails.forEach(data -> {
			ApplicantDetails_last5d dest = new ApplicantDetails_last5d();
			try {
				BeanUtils.copyProperties(dest, data);
			} catch (IllegalAccessException e) {
				logger.debug("Exception:: [{}]", e);
				logger.error("Exception [{}]", e.getMessage());
			} catch (InvocationTargetException e) {
				logger.debug("Exception:: [{}]", e);
				logger.error("Exception:: [{}]", e.getMessage());
			}
			fapplicantDetailsDAO.save(dest);
		});

		logger.info("Count of last 5 days applicantDetails :- [{}] ", applicantDetails.size());

		List<PaymentTransactionDTO> paymentTransactionDetails = paymentTransactionDAO.findByLUpdateBetween(fromDate,
				toDate);

		logger.info("Count of last 5 days paymentTransactionDetails :- [{}] ", paymentTransactionDetails.size());
		paymentTransactionDetails.forEach(data -> {

			PaymentTransaction_last5d dest = new PaymentTransaction_last5d();
			try {
				BeanUtils.copyProperties(dest, data);
			} catch (IllegalAccessException e) {
				logger.debug("Exception:: [{}]", e);
				logger.error("Exception [{}]", e.getMessage());
			} catch (InvocationTargetException e) {
				logger.debug("Exception:: [{}]", e);
				logger.error("Exception:: [{}]", e.getMessage());
			}
			fpaymentTransactionDAO.save(dest);
		});
		List<FlowDTO> flowDetails = flowDAO.findByLUpdateBetween(fromDate, toDate);

		logger.info("Count of last 5 days flowDetails :- [{}] ", flowDetails.size());

		flowDetails.forEach(data -> {
			Flow_last5d dest = new Flow_last5d();
			try {
				BeanUtils.copyProperties(dest, data);
			} catch (IllegalAccessException e) {
				logger.debug("Exception:: [{}]", e);
				logger.error("Exception [{}]", e.getMessage());
			} catch (InvocationTargetException e) {
				logger.debug("Exception:: [{}]", e);
				logger.error("Exception:: [{}]", e.getMessage());
			}
			fflowDAO.save(dest);
		});

		List<PermitDetailsDTO> permitDetailsDetails = permitDetailsDAO.findByLUpdateBetween(fromDate, toDate);

		logger.info("Count of last 5 days permitDetailsDetails :- [{}] ", permitDetailsDetails.size());

		permitDetailsDetails.forEach(data -> {

			PermitDetails_last5d dest = new PermitDetails_last5d();
			try {
				BeanUtils.copyProperties(dest, data);
			} catch (IllegalAccessException e) {
				logger.debug("Exception:: [{}]", e);
				logger.error("Exception [{}]", e.getMessage());
			} catch (InvocationTargetException e) {
				logger.debug("Exception:: [{}]", e);
				logger.error("Exception:: [{}]", e.getMessage());
			}
			//fpermitDetailsDAO.save(dest);
		});
		List<TaxDetailsDTO> taxDetails = taxDetailsDAO.findByLUpdateBetween(fromDate, toDate);

		logger.info("Count of last 5 days taxDetails :- [{}] ", taxDetails.size());

		taxDetails.forEach(data -> {

			TaxDetails_last5d dest = new TaxDetails_last5d();
			try {
				BeanUtils.copyProperties(dest, data);
			} catch (IllegalAccessException e) {
				logger.debug("Exception:: [{}]", e);
				logger.error("Exception [{}]", e.getMessage());
			} catch (InvocationTargetException e) {
				logger.debug("Exception:: [{}]", e);
				logger.error("Exception:: [{}]", e.getMessage());
			}
			ftaxDetailsDAO.save(dest);
		});
		List<RegistrationDetailsDTO> registrationDetails = registrationDetailDAO.findByLUpdateBetween(fromDate, toDate);

		logger.info("Count of last 5 days registrationDetails :- [{}] ", registrationDetails.size());

		registrationDetails.forEach(data -> {
			RegistrationDetails_last5d dest = new RegistrationDetails_last5d();
			try {
				BeanUtils.copyProperties(dest, data);
			} catch (IllegalAccessException e) {
				logger.debug("Exception:: [{}]", e);
				logger.error("Exception [{}]", e.getMessage());
			} catch (InvocationTargetException e) {
				logger.debug("Exception:: [{}]", e);
				logger.error("Exception:: [{}]", e.getMessage());
			}
			fregistrationDetailDAO.save(dest);
		});
		List<StagingRegistrationDetailsDTO> stagingRegistrationDetails = stagingRegistrationDetailsDAO
				.findByLUpdateBetween(fromDate, toDate);
		logger.info("Count of last 5 days stagingRegistrationDetails :- [{}] ", stagingRegistrationDetails.size());

		stagingRegistrationDetails.forEach(data -> {
			StagingRegistrationDetails_last5d dest = new StagingRegistrationDetails_last5d();
			try {
				BeanUtils.copyProperties(dest, data);
			} catch (IllegalAccessException e) {
				logger.debug("Exception:: [{}]", e);
				logger.error("Exception [{}]", e.getMessage());
			} catch (InvocationTargetException e) {
				logger.debug("Exception:: [{}]", e);
				logger.error("Exception:: [{}]", e.getMessage());
			}
			fstagingRegistrationDetailsDAO.save(dest);
		});
		// APPLICANT DETAILS
		applicantDetails.forEach(data -> {
			LastFiveDaysCollectionsDTO dto = new LastFiveDaysCollectionsDTO();
			dto.setDate(data.getlUpdate());
			dto.setCollectionName("applicant_details");
			dto.setCollectionData(data);
			dao.save(dto);
			dto = null;

		});

		// FLOW DETAILS
		flowDetails.forEach(data -> {
			LastFiveDaysCollectionsDTO dto = new LastFiveDaysCollectionsDTO();
			dto.setDate(data.getlUpdate());
			dto.setCollectionName("flow_details");
			dto.setCollectionData(data);
			dao.save(dto);
			dto = null;
		});

		// PAYMENT DETAILS
		paymentTransactionDetails.forEach(data -> {
			LastFiveDaysCollectionsDTO dto = new LastFiveDaysCollectionsDTO();
			dto.setDate(data.getlUpdate());
			dto.setCollectionName("payment_details");
			dto.setCollectionData(data);
			dao.save(dto);
			dto = null;
		});

		// PERMIT DETAILS
		permitDetailsDetails.forEach(data -> {
			LastFiveDaysCollectionsDTO dto = new LastFiveDaysCollectionsDTO();
			dto.setDate(data.getlUpdate());
			dto.setCollectionName("permit_details");
			dto.setCollectionData(data);
			dao.save(dto);
			dto = null;
		});
		// REGISTRATION DETAILS
		registrationDetails.forEach(data -> {
			LastFiveDaysCollectionsDTO dto = new LastFiveDaysCollectionsDTO();
			dto.setDate(data.getlUpdate());
			dto.setCollectionName("registration_details");
			dto.setCollectionData(data);
			dao.save(dto);
			dto = null;
		});

		// STAGING REGISTRATION DETAILS
		stagingRegistrationDetails.forEach(data -> {
			LastFiveDaysCollectionsDTO dto = new LastFiveDaysCollectionsDTO();
			dto.setDate(data.getlUpdate());
			dto.setCollectionName("staging_registration_details");
			dto.setCollectionData(data);
			dao.save(dto);
			dto = null;
		});

		// TAX DETAILS
		taxDetails.forEach(data -> {
			LastFiveDaysCollectionsDTO dto = new LastFiveDaysCollectionsDTO();
			dto.setDate(data.getlUpdate());
			dto.setCollectionName("tax_details");
			dto.setCollectionData(data);
			dao.save(dto);
			dto = null;
			;
		});

		logger.info("Last 5 Days Collection data is moved to one collection last_fiveDays_collections ");

	}

	@Override
	public void save() throws Exception {
		// TODO Auto-generated method stub
		
	}*/

}
