package org.epragati.master.service;

import java.util.List;
import java.util.Optional;

import org.epragati.master.vo.MakersVO;
import org.epragati.master.vo.MasterVariantVO;

/**
 * 
 * @author saikiran.kola
 *
 */
public interface MakersService {

	/**
	 * get all Makers data
	 * 
	 * @return
	 */

	public List<MakersVO> getAllMakersDetails();

	Optional<MakersVO> findMakerNameByMakerId(Integer mId);

	List<MasterVariantVO> getMakerClassByMakerNameAndCov(Integer mId, String cov);
	List<MasterVariantVO> getDataEntryMakerClassByMakerNameAndCov(Integer mId, String cov);

	public List<MakersVO> getDataEntryMakersDetails(String cov);

}
