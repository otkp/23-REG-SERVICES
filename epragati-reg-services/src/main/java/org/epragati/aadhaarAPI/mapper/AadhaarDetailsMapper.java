package org.epragati.aadhaarAPI.mapper;

import org.epragati.aadhaarAPI.DTO.AadhaarTransactionDTO;
import org.epragati.aadhaarAPI.DTO.AadhaarTransactionDTO_BackUp;
import org.epragati.aadhaarAPI.util.BaseMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
public class AadhaarDetailsMapper extends BaseMapper<AadhaarTransactionDTO, AadhaarTransactionDTO_BackUp> {

	@Override
	public AadhaarTransactionDTO_BackUp convertEntity(AadhaarTransactionDTO dto) {
		AadhaarTransactionDTO_BackUp backUp = new AadhaarTransactionDTO_BackUp();
		BeanUtils.copyProperties(dto, backUp);
		return backUp;
	}

}
