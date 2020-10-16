package org.epragati.master.service.impl;

import java.io.IOException;
import java.net.ConnectException;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.epragati.constants.MessageKeys;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.TrSeriesDAO;
import org.epragati.master.dto.TrSeriesDTO;
import org.epragati.master.service.TrSeriesService;
import org.epragati.util.AppMessages;
import org.epragati.util.GateWayResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 
 * @author saikiran.kola
 *
 */
@Service
public class TrSeriesServiceImpl implements TrSeriesService {

	private static final Logger logger = LoggerFactory.getLogger(TrSeriesServiceImpl.class);


	@Autowired
	private TrSeriesDAO trSeriesDAO;
	
	@Value("${reg.dealer.prGeneration.url:}")
	private String trGenerationUrl;
	
	@Autowired
	private AppMessages appMessages;
	
	@Autowired
	private ObjectMapper objectMapper;
	

	@Override
	public String geneateTrSeries(Integer trDistrictId) {

		HttpHeaders headers = new HttpHeaders();
		headers.set("Accept", "application/json");
		HttpEntity entity = new HttpEntity(headers);
		ResponseEntity<String> response = null;
		try {
			RestTemplate restTemplate = new RestTemplate();
			 response = restTemplate.exchange(trGenerationUrl + "generateTRSeries?trDistrictId=" + trDistrictId,
					HttpMethod.GET, entity, String.class);

			
			if (response.hasBody()) {
				GateWayResponse<String> inputOptional = parseJson(response.getBody(),
						new TypeReference<GateWayResponse<String>>() {
						});
				if (!inputOptional.getStatus()) {
					if (StringUtils.isEmpty(inputOptional.getMessage())) {
						throw new BadRequestException(inputOptional.getResult());
					}
					throw new BadRequestException(inputOptional.getMessage());
				}
				return inputOptional.getResult();
			}
		}
		catch (Exception e) {
			logger.error(e.getMessage());
			throw new BadRequestException(" TR number not generated ");
		}
		throw new BadRequestException(" TR number not generated ");
	}


	@Override
	public List<TrSeriesDTO> modifyData() {

		String series = null;

		List<TrSeriesDTO> dto = trSeriesDAO.findAll();
		for (TrSeriesDTO dto1 : dto) {
			series = dto1.getSeries();
			String x = series.replace(series.substring(4, 6), "");
			dto1.setSeries(x);
		}
		trSeriesDAO.save(dto);
		return dto;
	}
	
	private <T> T parseJson(String value, TypeReference<T> valueTypeRef) {
		try {
			return objectMapper.readValue(value, valueTypeRef);
		} catch (IOException ioe) {
			logger.debug(appMessages.getLogMessage(MessageKeys.PARSEJSON_JSONTOOBJECT), ioe);
			logger.error(appMessages.getLogMessage(MessageKeys.PARSEJSON_JSONTOOBJECT), ioe.getMessage());

		}
		return null;
	}

}
