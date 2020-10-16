package org.epragati.vcr.service;

import java.util.List;

import org.epragati.master.vo.MisusedAsVO;

public interface MisusedAsService {

	public void save(MisusedAsVO vo) throws Exception;

	List<MisusedAsVO> findListOfMisusedAs();
}
