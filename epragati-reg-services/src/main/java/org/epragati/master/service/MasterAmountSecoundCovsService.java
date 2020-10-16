package org.epragati.master.service;

import java.util.Optional;

import org.epragati.master.dto.MasterAmountSecoundCovsDTO;

/**
 * @author sairam.cheruku
 *
 */
public interface MasterAmountSecoundCovsService {

	Optional<MasterAmountSecoundCovsDTO> findByCovCode(String cov);

}
