/**
 * 
 */
package org.epragati.master.service;

import java.util.List;
import java.util.Optional;

import org.epragati.master.vo.MandalVO;
import org.epragati.master.vo.OfficeVO;

/**
 * @author kumaraswamy.asari
 *
 */
public interface MandalService {

	/**
	 * @param mandalCode
	 * @param nthFlag
	 * @return
	 */
	Optional<OfficeVO> getOfficeDetailsByMandal(int mandalCode, String nthFlag);

	Optional<OfficeVO> getOfficeDetailsByMandal(int mandalCode, String nthFlag, String ownerType);
	// TODO Auto-generated method stub

	/**
	 * 
	 * @param districtId
	 * @return MandalList By county
	 */
	List<MandalVO> findByDid(Integer districtId);

	List<MandalVO> findByDidAndPrNo(Integer districtId, String prNo);

	/**
	 * 
	 * @param districtCode
	 * @param prNo
	 * @return
	 */
	List<MandalVO> findByDidAndPrNoForTransport(Integer districtCode, String prNo);

	/**
	 * 
	 * @param districtId
	 * @param officeCode
	 * @return
	 */
	List<MandalVO> getMandalsbasedOnOffice(Integer districtId, String officeCode);
}
