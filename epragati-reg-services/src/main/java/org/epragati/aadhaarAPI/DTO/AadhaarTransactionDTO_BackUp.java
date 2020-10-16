package org.epragati.aadhaarAPI.DTO;

import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "aadhaar_logs")
public class AadhaarTransactionDTO_BackUp extends AadhaarTransactionDTO {

}
