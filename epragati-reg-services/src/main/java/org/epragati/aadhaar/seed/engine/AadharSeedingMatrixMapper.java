package org.epragati.aadhaar.seed.engine;

import org.epragati.aadhaarseeding.vo.AadharSeedingMatrixVO;
import org.epragati.common.mappers.BaseMapper;
import org.epragati.master.dto.AadharSeedingMatrixDTO;

public class AadharSeedingMatrixMapper extends BaseMapper<AadharSeedingMatrixDTO, AadharSeedingMatrixVO> {

	@Override
	public AadharSeedingMatrixVO convertEntity(AadharSeedingMatrixDTO dto) {

		AadharSeedingMatrixVO aadharVO = new AadharSeedingMatrixVO();
		funPoint(dto.getId(), aadharVO::setId);
		funPoint(dto.getCode(), aadharVO::setCode);
		funPoint(dto.getApprovalStatus(), aadharVO::setApprovalStatus);
		return aadharVO;

	}
}