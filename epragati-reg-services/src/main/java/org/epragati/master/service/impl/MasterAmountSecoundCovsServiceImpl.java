package org.epragati.master.service.impl;


import java.util.Optional;

import org.epragati.master.dao.MasterAmountSecoundCovsDAO;
import org.epragati.master.dto.MasterAmountSecoundCovsDTO;
import org.epragati.master.service.MasterAmountSecoundCovsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author sairam.cheruku
 *
 */
@Service
public class MasterAmountSecoundCovsServiceImpl implements MasterAmountSecoundCovsService{

	@Autowired
	private MasterAmountSecoundCovsDAO masterAmountSecoundCovsDAO;
	
	public Optional<MasterAmountSecoundCovsDTO> findByCovCode(String cov) {
		return masterAmountSecoundCovsDAO.findBysecondcovcodeIn(cov);
	}
	
	
}
