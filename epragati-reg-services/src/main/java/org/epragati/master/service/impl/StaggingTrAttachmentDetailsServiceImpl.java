package org.epragati.master.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.epragati.master.service.StaggingTrAttachmentDetailsService;
import org.epragati.master.vo.StaggingTrAttachmentDetailsVO;
import org.epragati.master.vo.EnclosuresVO;

public class StaggingTrAttachmentDetailsServiceImpl implements StaggingTrAttachmentDetailsService {

	@Override
	public Integer save(Optional<StaggingTrAttachmentDetailsVO> staggingTrAttachmentDetailsVO) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<StaggingTrAttachmentDetailsVO> findAllUserEnclosure() {
		// TODO Auto-generated method stub
		return new ArrayList<StaggingTrAttachmentDetailsVO>();
	}

	@Override
	public Optional<EnclosuresVO> findUserEnclosureBasedOnenId(Integer enId) {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

}
