package org.epragati.common.service.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.epragati.common.service.CommonService;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.OfficeDAO;
import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.service.PrSeriesService;
import org.epragati.sequence.SequenceGenerator;
import org.epragati.sn.vo.BidConfigMasterVO;
import org.epragati.util.NumberPoolStatus.NumberConfigLevel;
import org.epragati.util.document.Sequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class SyncroServiceFactory {
	
	private static final Logger logger = LoggerFactory.getLogger(SyncroServiceFactory.class);

	@Autowired
	@Qualifier("officeLevelImpl")
	private OfficeLevelImpl officeLevelImpl;

	@Autowired
	@Qualifier("stateLevelImpl")
	private StateLevelImpl stateLevelImpl;

	@Autowired
	private PrSeriesService prSeriesService;

	@Autowired
	private SequenceGenerator sequenceGenerator;

	@Autowired
	private OfficeDAO officeDAO;

	private BidConfigMasterVO getPrimeNumbers() {

		Optional<BidConfigMasterVO> resultOptional = prSeriesService.getBidConfigMasterData(false);
		if (!resultOptional.isPresent()) {
			throw new BadRequestException("Bid mater config data not found");
		}
		return resultOptional.get();
	}

	public CommonService getCommonServiceInst() {
		BidConfigMasterVO bidConfigMasterVO = getPrimeNumbers();
		if (NumberConfigLevel.STATE.getLabel().equals(bidConfigMasterVO.getNumberGenerationType())) {
			logger.info("state/office factory method executed ");
			return stateLevelImpl;
		} else {
			return officeLevelImpl;
		}
	}

	public String generatePaymentReciept() {
		Map<String, String> officeCodeMap = new HashMap<>();
		officeCodeMap.put("officeCode", "PaymentReciept");
		return sequenceGenerator.getSequence(String.valueOf(Sequence.PAYMENTRECIEPT.getSequenceId()), officeCodeMap);

	}

	public void testRandom() {
		List<OfficeDTO> list = getOfficeList();
		List<String> officeNamesList = list.stream().map(val -> val.getOfficeName()).collect(Collectors.toList());
		System.out.println("office Namee before shuffle" + officeNamesList);
		Collections.shuffle(officeNamesList);
		System.out.println("office Namee After shuffle" + officeNamesList);
		if (hasDuplicate(officeNamesList)) {
			System.out.println("Has Duplicates");
		}
	}

	public boolean hasDuplicate(List<String> items) {
		Set<String> appeared = new HashSet<>();
		for (String item : items) {
			if (!appeared.add(item)) {
				return true;
			}
		}
		if (appeared.size() != items.size()) {
			return true;
		}
		return false;
	}

	public List<OfficeDTO> getOfficeList() {
		return officeDAO.findBydistrict(5);
	}

}
