package org.epragati.taxreports.mappers; 

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.epragati.common.mappers.BaseMapper;
import org.epragati.master.dto.TaxComponentDTO;
import org.epragati.master.dto.TaxDetailsDTO;
import org.epragati.regservice.vo.TaxDetailsVO; 

import org.springframework.stereotype.Component;

@Component
public class TaxReportMappers extends BaseMapper<TaxDetailsDTO, TaxDetailsVO> {

	List<TaxDetailsVO> reportlist = Collections.EMPTY_LIST;

	public TaxDetailsVO convertEntity1(TaxComponentDTO dto) {
		TaxDetailsVO vo = new TaxDetailsVO();

		vo.setTaxType(dto.getTaxName());
		vo.setAmount(dto.getAmount());
		vo.setTaxPaymentDAte(dto.getPaidDate());
		vo.setValidfrom(dto.getValidityFrom());
		vo.setValidUpto(dto.getValidityTo()); 
		return vo;
	}

	@Override
	public TaxDetailsVO convertEntity(TaxDetailsDTO dto) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<TaxDetailsVO> convertDTO(List<TaxComponentDTO> dtos) {

		TaxDetailsVO vo = new TaxDetailsVO();

		reportlist = dtos.stream().map(e -> convertEntity1(e)).collect(Collectors.toList());

		return reportlist;
	}

}
