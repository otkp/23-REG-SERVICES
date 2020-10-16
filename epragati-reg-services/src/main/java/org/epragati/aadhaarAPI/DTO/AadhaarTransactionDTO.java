package org.epragati.aadhaarAPI.DTO;

import java.util.UUID;

import javax.persistence.Id;

import org.epragati.aadhaar.AadhaarDetailsRequestVO;
import org.epragati.aadhaarAPI.AadhaarLogModel;
import org.epragati.aadhaarAPI.AadhaarSourceDTO;
import org.epragati.aadhaarAPI.util.AadhaarRestRequest;
import org.epragati.aadhaarAPI.util.AadhaarRestResponse;
import org.epragati.common.dto.BaseEntity;
import org.springframework.data.mongodb.core.mapping.Document;

import com.ecentric.bean.BiometricresponseBean;

@Document(collection = "aadhaar_logs_new")
public class AadhaarTransactionDTO extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1613570850037869958L;
	@Id
	private String id;
	private UUID uuId;
	private AadhaarLogModel aadhaarRequest;
	private AadhaarRestRequest aadhaarRestRequest;
	private String aadhaarResponse;
	private AadhaarRestResponse response;
	private String url;
	private AadhaarSourceDTO aadhaarSourceDTO;
	private AadhaarDetailsRequestVO aptsAadhaarRequest;   
	private BiometricresponseBean aptsAadhaarResponse;	
	private Boolean isAptsRequest=Boolean.FALSE;


	public AadhaarSourceDTO getAadhaarSourceDTO() {
		return aadhaarSourceDTO;
	}

	public void setAadhaarSourceDTO(AadhaarSourceDTO aadhaarSourceDTO) {
		this.aadhaarSourceDTO = aadhaarSourceDTO;
	}

	// private String source;
	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the uuId
	 */
	public UUID getUuId() {
		return uuId;
	}

	/**
	 * @param uuId the uuId to set
	 */
	public void setUuId(UUID uuId) {
		this.uuId = uuId;
	}

	/**
	 * @return the aadhaarRestRequest
	 */
	public AadhaarRestRequest getAadhaarRestRequest() {
		return aadhaarRestRequest;
	}

	/**
	 * @param aadhaarRestRequest the aadhaarRestRequest to set
	 */
	public void setAadhaarRestRequest(AadhaarRestRequest aadhaarRestRequest) {
		this.aadhaarRestRequest = aadhaarRestRequest;
	}

	/**
	 * @return the aadhaarResponse
	 */
	public String getAadhaarResponse() {
		return aadhaarResponse;
	}

	/**
	 * @param aadhaarResponse the aadhaarResponse to set
	 */
	public void setAadhaarResponse(String aadhaarResponse) {
		this.aadhaarResponse = aadhaarResponse;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	/**
	 * @return the aadhaarRequest
	 */
	public AadhaarLogModel getAadhaarRequest() {
		return aadhaarRequest;
	}

	/**
	 * @param aadhaarRequest the aadhaarRequest to set
	 */
	public void setAadhaarRequest(AadhaarLogModel aadhaarRequest) {
		this.aadhaarRequest = aadhaarRequest;
	}

	/**
	 * @return the response
	 */
	public AadhaarRestResponse getResponse() {
		return response;
	}

	/**
	 * @param response the response to set
	 */
	public void setResponse(AadhaarRestResponse response) {
		this.response = response;
	}

	/**
	 * @return the aptsAadhaarRequest
	 */
	public AadhaarDetailsRequestVO getAptsAadhaarRequest() {
		return aptsAadhaarRequest;
	}

	/**
	 * @param aptsAadhaarRequest the aptsAadhaarRequest to set
	 */
	public void setAptsAadhaarRequest(AadhaarDetailsRequestVO aptsAadhaarRequest) {
		this.aptsAadhaarRequest = aptsAadhaarRequest;
	}

	/**
	 * @return the aptsAadhaarResponse
	 */
	public BiometricresponseBean getAptsAadhaarResponse() {
		return aptsAadhaarResponse;
	}

	/**
	 * @param aptsAadhaarResponse the aptsAadhaarResponse to set
	 */
	public void setAptsAadhaarResponse(BiometricresponseBean aptsAadhaarResponse) {
		this.aptsAadhaarResponse = aptsAadhaarResponse;
	}

	/**
	 * @return the isAptsRequest
	 */
	public Boolean getIsAptsRequest() {
		return isAptsRequest;
	}

	/**
	 * @param isAptsRequest the isAptsRequest to set
	 */
	public void setIsAptsRequest(Boolean isAptsRequest) {
		this.isAptsRequest = isAptsRequest;
	}
	
	

}
