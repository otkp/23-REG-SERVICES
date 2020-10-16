package org.epragati.aadhaarAPI.DAO;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.epragati.aadhaarAPI.DTO.AadhaarTransactionDTO;

public interface AadhaarTransactionDAO extends BaseRepository<AadhaarTransactionDTO, Serializable> {

	Optional<AadhaarTransactionDTO> findByAadhaarRestRequestPid(String pid);

	List<AadhaarTransactionDTO> findByCreatedDateBefore(LocalDateTime createdDate);

	Optional<AadhaarTransactionDTO> findByAadhaarRestRequestPidOrderByCreatedDateDesc(String encryptedPid);

}
	