package org.epragati.master.service;

import java.util.List;

import org.epragati.master.dto.TrSeriesDTO;

/**
 * 
 * @author saikiran.kola
 *
 */

public interface TrSeriesService {

	String geneateTrSeries(Integer districtId);

	List<TrSeriesDTO> modifyData();

}
