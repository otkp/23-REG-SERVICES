package org.epragati.master.service;

import java.util.Optional;

import org.epragati.elastic.vo.RtaSearchResponse;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;
import org.epragati.master.vo.RegistrationDetailsVO;
import org.epragati.master.vo.StagingRegistrationDetailsVO;

public interface StagingRegistrationDetailsSerivce {

	public Optional<StagingRegistrationDetailsDTO> FindbBasedOnApplicationNo(String appplicationNo);

	public Optional<StagingRegistrationDetailsDTO> fLowLogs(String appplicationNo);

	public Optional<StagingRegistrationDetailsVO> getTrDetailByApplicationNo(String applicationNo);

	public String validateFinacerToken(String applicationNo);

	boolean dealerStatusUpdationForFinancerApproval(String applicationNo, Boolean status, String user);

	void saveTax(StagingRegistrationDetailsDTO taxDetails);

	public RegistrationDetailsVO getRegistrationDetailWithEnclosuersByApplicationNo(String applicationNo);

	public Optional<StagingRegistrationDetailsDTO> findbBasedOnApplicationNo(String applicationFormNo);

	public StagingRegistrationDetailsDTO findApplicationDetailsByTrNo(String trNo);

	public RtaSearchResponse secondVehicleApplicabale(String applicationNo);

}
