package org.epragati.vcr.service;

import java.util.List;

import org.epragati.master.vo.OffenceCategoryVO;

public interface OffenceCategoryService {

	public void saveCategeory(OffenceCategoryVO vo) throws Exception;
	public List<OffenceCategoryVO> findeOffenceCategeory();

}
