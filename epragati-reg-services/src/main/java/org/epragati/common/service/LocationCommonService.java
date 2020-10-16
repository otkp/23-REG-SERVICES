package org.epragati.common.service;

import java.util.Optional;

import org.epragati.regservice.dto.RegServiceDTO;
import org.epragati.regservice.vo.RegServiceVO;

public interface LocationCommonService {
	Optional<?> prajaasachivalayamLogininfo(Optional<RegServiceVO> voObject, RegServiceVO regServiceDetail);
	void prajaasachivalayamApplicationStatus(RegServiceDTO regServiceDTO);
}
