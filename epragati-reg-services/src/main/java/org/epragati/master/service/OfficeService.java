package org.epragati.master.service;

import java.util.List;
import java.util.Optional;

import org.epragati.master.dto.OfficeDTO;
import org.epragati.master.vo.OfficeVO;

/**
 * 
 * @author sairam.cheruku
 *
 */
public interface OfficeService {

	/**
	 * 
	 * @return findAllOffices
	 */
	List<OfficeVO> findAll();

	List<OfficeVO> findRTAOffices();

	/**
	 * 
	 * @param officeCode
	 * @return List offices By officeCode
	 */

	Optional<OfficeVO> findByOfficecode(String officeCode);

	/**
	 * 
	 * @param districtId
	 * @return
	 */
	List<OfficeVO> getOfficeByDistrict(Integer districtId);

	/**
	 * 
	 * @return
	 */
	List<OfficeVO> findRTAOfficesForFC();

	/**
	 * 
	 * @param prNo
	 * @return
	 */
	List<OfficeVO> findRTAOfficesForNewFC(String prNo);

	/**
	 * 
	 * @param officeCode
	 * @return
	 */
	OfficeDTO getDistrictByofficeCode(String officeCode);

	List<OfficeVO> getOfficeByDistrictLimited(Integer districtId);
}
