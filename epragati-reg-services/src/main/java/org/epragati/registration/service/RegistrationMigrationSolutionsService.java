package org.epragati.registration.service;

import java.util.List;
import java.util.Optional;

import org.epragati.master.dto.RegistrationDetailsDTO;

public interface RegistrationMigrationSolutionsService {

	Optional<RegistrationDetailsDTO> removeInactiveRecordsToSongle(List<RegistrationDetailsDTO> registrationDetailsDTOs);
	
	List<RegistrationDetailsDTO> removeInactiveRecordsToList(List<RegistrationDetailsDTO> registrationDetailsDTOs);
}
