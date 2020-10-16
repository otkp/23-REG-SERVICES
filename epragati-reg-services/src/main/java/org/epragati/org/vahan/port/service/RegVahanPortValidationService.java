package org.epragati.org.vahan.port.service;

import java.util.List;

import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.vahan.port.vo.RegVahanPortVO;
import org.springframework.data.util.Pair;

public interface RegVahanPortValidationService {

	Pair<RegVahanPortVO, List<String>> validateRegFields(RegistrationDetailsDTO regDto,
			RegVahanPortVO regVahanPortVO, List<String> errors);

}
