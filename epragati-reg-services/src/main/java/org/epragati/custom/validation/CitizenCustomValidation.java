package org.epragati.custom.validation;

import java.util.ArrayList;
import java.util.List;

import org.epragati.aadhaar.AadhaarDetailsRequestVO;
import org.springframework.stereotype.Service;
import org.springframework.validation.FieldError;

@Service
public class CitizenCustomValidation {

	/**
	 * custom Validation for Aadhaar OTP
	 * 
	 * @param requestModel
	 * @return
	 */
	public List<FieldError> aadhaarOtp(AadhaarDetailsRequestVO requestModel) {
		List<FieldError> error = new ArrayList();
		if (null == requestModel.getAuthType())
			error.add(getFieldErrorMessage(requestModel, "AuthType", "AuthType not found"));
		if (null == requestModel.getCrt())
			error.add(getFieldErrorMessage(requestModel, "Crt", "Crt not found"));
		if (null == requestModel.getIdType())
			error.add(getFieldErrorMessage(requestModel, "IdType", "IdType not found"));
		if (null == requestModel.getRequestType())
			error.add(getFieldErrorMessage(requestModel, "RequestType", "RequestType not found"));
		if (null == requestModel.getUid_num())
			error.add(getFieldErrorMessage(requestModel, "Uid_num", "Uid_num not found"));
		return error;
	}

	private <T> FieldError getFieldErrorMessage(T requestModel, String field, String message) {
		return new FieldError(requestModel.getClass().getName(), field, message);
	}
}
