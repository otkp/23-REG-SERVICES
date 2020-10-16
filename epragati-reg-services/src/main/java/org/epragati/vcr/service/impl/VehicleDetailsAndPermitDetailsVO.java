package org.epragati.vcr.service.impl;

import java.io.Serializable;
import java.util.Map;

import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.master.vo.RegistrationDetailsVO;
import org.epragati.permits.vo.PermitDetailsVO;
import org.epragati.vcr.vo.ValidityDetailsVo;

public class VehicleDetailsAndPermitDetailsVO implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	RegistrationDetailsVO regDetailsVO;
	PermitDetailsVO permitDetailsVO;
	ValidityDetailsVo fcDetails;
	TaxDetailsDTO taxDetails;
	private  Map<Object, Integer> OffenceListAndCount;
	public RegistrationDetailsVO getRegDetailsVO() {
		return regDetailsVO;
	}
	public void setRegDetailsVO(RegistrationDetailsVO regDetailsVO) {
		this.regDetailsVO = regDetailsVO;
	}
	public PermitDetailsVO getPermitDetailsVO() {
		return permitDetailsVO;
	}
	public void setPermitDetailsVO(PermitDetailsVO permitDetailsVO) {
		this.permitDetailsVO = permitDetailsVO;
	}
	public ValidityDetailsVo getFcDetails() {
		return fcDetails;
	}
	public void setFcDetails(ValidityDetailsVo fcDetails) {
		this.fcDetails = fcDetails;
	}
	public TaxDetailsDTO getTaxDetails() {
		return taxDetails;
	}
	public void setTaxDetails(TaxDetailsDTO taxDetails) {
		this.taxDetails = taxDetails;
	}
	public Map<Object, Integer> getOffenceListAndCount() {
		return OffenceListAndCount;
	}
	public void setOffenceListAndCount(Map<Object, Integer> offenceListAndCount) {
		OffenceListAndCount = offenceListAndCount;
	}
	
	
	
}
