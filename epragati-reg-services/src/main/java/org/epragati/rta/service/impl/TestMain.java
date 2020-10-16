package org.epragati.rta.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.epragati.common.dto.FlowDTO;
import org.epragati.master.dto.RoleActionDTO;
import org.epragati.master.dto.StagingRegistrationDetailsDTO;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestMain {

	private static Map<Integer, List<RoleActionDTO>> details = new TreeMap<Integer, List<RoleActionDTO>>();

	static {

		RoleActionDTO r1 = new RoleActionDTO();
		r1.setRole("CCO");

		RoleActionDTO r2 = new RoleActionDTO();
		r2.setRole("MVI");

		ArrayList<RoleActionDTO> al = new ArrayList<RoleActionDTO>();
		al.add(r1);
		al.add(r2);

		RoleActionDTO r3 = new RoleActionDTO();
		r3.setRole("AO");

		ArrayList<RoleActionDTO> a2 = new ArrayList<RoleActionDTO>();
		a2.add(r3);

		details.put(1, al);
		details.put(2, a2);
	}

	public FlowDTO getBaseObj() {

		FlowDTO flowDto = new FlowDTO();

		Map<Integer, List<RoleActionDTO>> details = new TreeMap<Integer, List<RoleActionDTO>>();

		RoleActionDTO r1 = new RoleActionDTO();
		r1.setRole("CCO");

		RoleActionDTO r2 = new RoleActionDTO();
		r2.setRole("MVI");

		ArrayList<RoleActionDTO> al = new ArrayList<RoleActionDTO>();
		al.add(r1);
		al.add(r2);

		RoleActionDTO r3 = new RoleActionDTO();
		r3.setRole("AO");

		ArrayList<RoleActionDTO> a2 = new ArrayList<RoleActionDTO>();
		a2.add(r3);

		RoleActionDTO r4 = new RoleActionDTO();
		r4.setRole("RTO");

		ArrayList<RoleActionDTO> a3 = new ArrayList<RoleActionDTO>();
		a3.add(r4);

		details.put(1, al);
		details.put(2, a2);
		details.put(3, a3);

		flowDto.setFlowDetails(details);

		return flowDto;
	}

	public StagingRegistrationDetailsDTO doAction(StagingRegistrationDetailsDTO stagingDTO, String role) {

		boolean isFirst = true;

		Set<Integer> index = stagingDTO.getFlowDetails().getFlowDetails().keySet();

		Integer flowPosition = index.stream().findFirst().get();

		List<RoleActionDTO> roleActionDTOList = stagingDTO.getFlowDetails().getFlowDetails().get(flowPosition);

		// Read Base Obj
		FlowDTO flowBaseObj = getBaseObj();

		List<RoleActionDTO> baseList = flowBaseObj.getFlowDetails().get(1);

		List<String> baseRolesList = baseList.stream().map(h -> h.getRole()).collect(Collectors.toList());

		for (RoleActionDTO roleDto : roleActionDTOList) {
			if (!baseRolesList.contains(roleDto.getRole())) {
				isFirst = false;
			}
		}

		List<RoleActionDTO> filteredist = roleActionDTOList.stream().filter(val -> val.getRole().equals(role))
				.collect(Collectors.toList());

		if (filteredist.size() == 0) {
			//System.out.println("Not authorized..");
		}

		RoleActionDTO roleActionDTO = getRoleActionDTO("APPROVED", "APP", role, null);

		List<RoleActionDTO> existingList = stagingDTO.getFlowDetails().getFlowDetails().get(flowPosition);

		// Prepare Log
		List<FlowDTO> existingFlowLog = new ArrayList<FlowDTO>();

		if (stagingDTO.getFlowDetailsLog() != null) {

			existingFlowLog = stagingDTO.getFlowDetailsLog();

			FlowDTO logFlowDTO = null;

			if (existingFlowLog.size() == 0) {
				logFlowDTO = new FlowDTO();
				List<RoleActionDTO> logFlowDTOList = new ArrayList<RoleActionDTO>();
				logFlowDTOList.add(roleActionDTO);
				logFlowDTO.getFlowDetails().put(flowPosition, logFlowDTOList);
			} else {
				logFlowDTO = stagingDTO.getFlowDetailsLog().get(0);
				List<RoleActionDTO> logFlowDTOList = logFlowDTO.getFlowDetails().get(flowPosition);

				if (logFlowDTOList == null) {
					logFlowDTOList = new ArrayList<RoleActionDTO>();
				}
				logFlowDTOList.add(roleActionDTO);
				logFlowDTO.getFlowDetails().put(flowPosition, logFlowDTOList);
			}

			existingFlowLog.add(logFlowDTO);

			stagingDTO.setFlowDetailsLog(existingFlowLog);
		}

		roleActionDTOList.remove(filteredist.get(0));
		if (roleActionDTOList.size() == 0) {
			stagingDTO.getFlowDetails().getFlowDetails().remove(flowPosition);
		}
		// List<RoleActionDTO> toBeRemovedList =
		// roleActionDTOList.remove(filteredist.get(0));

		//System.out.println(stagingDTO.getFlowDetails());
	//	System.out.println(stagingDTO.getFlowDetailsLog());

		return stagingDTO;
	}

	public RoleActionDTO getRoleActionDTO(String action, String applicationNo, String role, List<RoleActionDTO> list) {

		RoleActionDTO roleActionDto = new RoleActionDTO();
		roleActionDto.setAction(action);
		roleActionDto.setActionTime(LocalDateTime.now());
		roleActionDto.setApplicatioNo(applicationNo);
		roleActionDto.setModule("DL");
		roleActionDto.setRole(role);
		return roleActionDto;

	}

	public static void main(String[] args) {

		TestMain testMain = new TestMain();
		StagingRegistrationDetailsDTO stagingDTO = new StagingRegistrationDetailsDTO();
		stagingDTO.setFlowDetails(testMain.getBaseObj());

		String[] roles = { "CCO", "MVI", "AO", "RTO" };

		ObjectMapper obj = new ObjectMapper();
		/*try {
			// System.out.println(obj.writeValueAsString(stagingDTO.getFlowDetails()));

		//	System.out.println(obj.writeValueAsString(stagingDTO.getEnclosures()));

		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
*/
		TestMain t = new TestMain();

		for (int i = 0; i < roles.length; i++) {
			StagingRegistrationDetailsDTO lOCALstagingDTO = t.doAction(stagingDTO, roles[i]);

			stagingDTO = lOCALstagingDTO;
		}

		/*
		 * StagingRegistrationDetailsDTO stagingDTO = new
		 * StagingRegistrationDetailsDTO();
		 * 
		 * FlowDTO flow = new FlowDTO(); flow.setFlowId("1");
		 * flow.setFlowDetails(details); stagingDTO.setFlowDetails(flow); Set<Integer>
		 * index = stagingDTO.getFlowDetails().getFlowDetails().keySet();
		 * 
		 * List<RoleActionDTO> roleActionDTOList =
		 * stagingDTO.getFlowDetails().getFlowDetails()
		 * .get(index.stream().findFirst().get());
		 * 
		 * List<RoleActionDTO> filteredist = roleActionDTOList.stream().filter(val ->
		 * val.getRole().equals("CCO")) .collect(Collectors.toList());
		 */

		
		
		
		
		
		
		
		
		
		
	}
}
