package org.epragati.master.service.impl;

import java.util.Arrays;
import java.util.List;

import javax.transaction.Transactional;

import org.epragati.constants.MessageKeys;
import org.epragati.constants.SequenceIDFormatType;
import org.epragati.master.dao.SequenceDAO;
import org.epragati.master.dto.SequenceDTO;
import org.epragati.master.service.IdGenerator;
import org.epragati.util.AppMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author saikiran.kola
 *
 */
@Service
public class SequenceServiceImpl implements IdGenerator {

	private static final Logger logger = LoggerFactory.getLogger(SequenceServiceImpl.class);
	@Autowired
	AppMessages appMessages;

	@Autowired
	SequenceDAO sequenceDAO;

	/**
	 * generate sequenceId based on seqName and office coce
	 */
	@Override
	public String generateSequence(String seqName, String officeCode,Integer incrementor) {

		StringBuilder SequenceId = new StringBuilder();

		SequenceDTO sequenceDTO = getSeqNumDetails(seqName, officeCode,incrementor);

		if (sequenceDTO.getSeqNameFormat().trim().length() == 0) {
			logger.error(appMessages.getLogMessage(MessageKeys.INVALID_SEQUENCE_FORMAT));

		}

		String[] seqArray = sequenceDTO.getSeqNameFormat().split(",");

		// if(seqArray.length)

		List<String> sequenceDetails = Arrays.asList(seqArray);


		sequenceDetails.stream().forEach(object->{

			if (object.equals(SequenceIDFormatType.CONST.getType())) {
				SequenceId.append(sequenceDTO.getConstVal());
			}
			if (object.equals(SequenceIDFormatType.OFFICE_CODE.getType())) {
				SequenceId.append(sequenceDTO.getOfficeCode());
			}

			if (object.equals(SequenceIDFormatType.YEAR.getType())) {
				SequenceId.append(sequenceDTO.getYear());
			}
			if (object.equals(SequenceIDFormatType.CURRENT_NUM.getType())) {
				SequenceId.append(sequenceDTO.getCurrentNum());
			}
		});

		if (SequenceId.length() == 0) {
			logger.error(appMessages.getLogMessage(MessageKeys.INVALID_SEQUENCE_FORMAT));
		}

		// ODO Auto-generated method stub
		logger.info("SequenceId is [{}]", SequenceId);
		return SequenceId.toString();
	}

	@Transactional
	private SequenceDTO getSeqNumDetails(String seqName, String officeCode,Integer incrementor) {
		SequenceDTO sequenceDTO=null;
		synchronized ((seqName.concat(officeCode)).intern()) {
			sequenceDTO = sequenceDAO.findBySeqNameAndOfficeCode(seqName, officeCode);
			sequenceDTO.setCurrentNum(sequenceDTO.getCurrentNum() + incrementor);
			sequenceDAO.save(sequenceDTO);
		}
		return sequenceDTO;
	}

}
