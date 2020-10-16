package org.epragati.registration.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;
import org.epragati.exception.BadRequestException;
import org.epragati.master.dao.RegistrationDetailDAO;
import org.epragati.master.dao.RegistrationInactiveRecordsDAO;
import org.epragati.master.dto.RegistrationDetailsDTO;
import org.epragati.master.dto.RegistrationInactiveRecordsDTO;
import org.epragati.registration.service.RegistrationMigrationSolutionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

@Service
public class RegistrationMigrationSolutionsServiceImpl implements RegistrationMigrationSolutionsService {
	
	@Autowired
	private RegistrationDetailDAO registrationDetailDAO;
	
	@Autowired
	private RegistrationInactiveRecordsDAO registrationInactiveRecordsDAO;

	/**
	 *  Method to consume for single registartion_details, (if findByPrNo...)
	 */
	@Override
	public Optional<RegistrationDetailsDTO> removeInactiveRecordsToSongle(List<RegistrationDetailsDTO> registrationDetailsDTOs){

		if(registrationDetailsDTOs.isEmpty()) {
			throw new BadRequestException("Registration details not found");
		}
		if(registrationDetailsDTOs.size()>1) {
			Pair<RegistrationDetailsDTO,List<RegistrationDetailsDTO>> pair=getActiveInactiveRecords(registrationDetailsDTOs );
			//We already inactive the record through script no need below line of code.
			//saveInactiveRecords(pair.getSecond());
			return Optional.of(pair.getFirst());
		}

		return Optional.of(registrationDetailsDTOs.get(0));
	}

	/**
	 *  Method to consume for single registartion_details, (if findByPrNoIn...)
	 */
	@Override
	public List<RegistrationDetailsDTO> removeInactiveRecordsToList(List<RegistrationDetailsDTO> registrationDetailsDTOs) {

		Map<String, List<RegistrationDetailsDTO>> registrationDetailsByPrNo = registrationDetailsDTOs.stream()
				.collect(Collectors.groupingBy(RegistrationDetailsDTO::getPrNo));
		List<RegistrationDetailsDTO> activeRecords=new ArrayList<>();
		List<RegistrationDetailsDTO> inActiveRecords=new ArrayList<>();
		registrationDetailsByPrNo.forEach((key,value)->{
			if(value.size()>1) {
				Pair<RegistrationDetailsDTO,List<RegistrationDetailsDTO>> pair=getActiveInactiveRecords(registrationDetailsDTOs );
				activeRecords.add(pair.getFirst());
				inActiveRecords.addAll(pair.getSecond());
			}else {
				activeRecords.add(value.get(0));
			}
			
			
		});
		//We already inactive the record through script no need below line of code.
		//saveInactiveRecords(inActiveRecords);
		return activeRecords;
		
	}

	private Pair<RegistrationDetailsDTO, List<RegistrationDetailsDTO>> getActiveInactiveRecords(
			List<RegistrationDetailsDTO> registrationDetailsDTOs) {
		
		registrationDetailsDTOs.sort((r1,r2)->r2.getlUpdate().compareTo(r1.getlUpdate()));
		List<RegistrationDetailsDTO> inactiveRecords= registrationDetailsDTOs.stream().filter(r->
		!r.getApplicationNo().equals(registrationDetailsDTOs.get(0).getApplicationNo()) )
				.map(r->{
					r.setIsActive(Boolean.FALSE);
					return r;
				}).collect(Collectors.toList());
		
		return Pair.of(registrationDetailsDTOs.get(0), inactiveRecords);
	}
	
//	private void saveInactiveRecords(List<RegistrationDetailsDTO> inactiveRecords) {
//		if(CollectionUtils.isNotEmpty(inactiveRecords)) {
//			List<RegistrationDetailsDTO> regList= new ArrayList<>();
//			for(RegistrationDetailsDTO regDTO:inactiveRecords){
//				regDTO.setCfstSync(Boolean.TRUE);
//				regList.add(regDTO);
//			}
//			registrationDetailDAO.save(regList);
//			
//			List<RegistrationInactiveRecordsDTO> inactiveRecordsLogs= new ArrayList<>();
//			inactiveRecords.stream().forEach(i->{
//				RegistrationInactiveRecordsDTO regInactiveRecordsLogs= new RegistrationInactiveRecordsDTO();
//				regInactiveRecordsLogs.setApplicationNo(i.getApplicationNo());
//				regInactiveRecordsLogs.setPrNo(i.getPrNo());
//				regInactiveRecordsLogs.setCreatedDate(LocalDateTime.now());
//				inactiveRecordsLogs.add(regInactiveRecordsLogs);
//			});
//			registrationInactiveRecordsDAO.save(inactiveRecordsLogs);
//		}
//		
//	}

}
