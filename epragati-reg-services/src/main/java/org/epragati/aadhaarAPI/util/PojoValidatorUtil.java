package org.epragati.aadhaarAPI.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.FieldError;

import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class PojoValidatorUtil {

	private static final Logger logger = LoggerFactory.getLogger(PojoValidatorUtil.class);
	private Validator validator;
	@Autowired
	private ObjectMapper objectMapper;
	public PojoValidatorUtil() {
		super();
		ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
		validator = validatorFactory.getValidator();
	}
	
	public <T> List<FieldError> doValidation(T model,String indicator) {

		Set<ConstraintViolation<T>> violations = validator.validate(model);

		List<FieldError> errors = new ArrayList<>();

		for (ConstraintViolation<T> violation : violations) {
			String propertyPath = violation.getPropertyPath().toString();
			String message = violation.getMessage();
			// Add JSR-303 errors to BindingResult
			// This allows Spring to display them in view via a FieldError
			errors.add(new FieldError(indicator, propertyPath, message));
		}

		return errors;
	}
	//TODO: Based on service we have to implement custom validations
	public <T> List<FieldError> doCustomValidation(T model,String indicator) {/*

		AadhaarDetailsRequestModel requestModel =(AadhaarDetailsRequestModel)model;
		List<FieldError> errors = new ArrayList<>();
		FieldError f;
		
		if(null==requestModel.getUid_num()) {
			 f= new FieldError(requestModel.getClass().getName(),"uid_num","Input value 'uid_num' not found");
			errors.add(f);
		}
		if(null==requestModel.getIdType()) {
			 f= new FieldError(requestModel.getClass().getName(),"idType","Input value 'idType' not found");
			errors.add(f);
		}

		if(null==requestModel.getRequestType()) {
			 f= new FieldError(requestModel.getClass().getName(),"requestType","Input value 'requestType' not found");
			errors.add(f);
		}
		if(null==requestModel.getCrt()) {
			 f= new FieldError(requestModel.getClass().getName(),"crt","Input value 'crt' not found");
			errors.add(f);
		}
		
		if(RequestType.EKYC.getContent().equals(requestModel.getRequestType())) {
			if(null==requestModel.getOldTid()) {
				 f= new FieldError(requestModel.getClass().getName(),"oldTid","Input value 'oldTid' not found");
				errors.add(f);
			}
			if(null==requestModel.getVercode()) {
				 f= new FieldError(requestModel.getClass().getName(),"vercode","Input value 'vercode' not found");
				errors.add(f);
			}
		}

		return errors;
	*/
		return null;
		}
	
	
	public <T> Optional<T> readValue(String value, Class<T> valueType) {

		try {
			return Optional.of(objectMapper.readValue(value, valueType));
		} catch (IOException ioe) {
			logger.debug("Exception occured while converting String to Object [{}]", ioe);

			logger.error("Exception occured while converting String to Object [{}]", ioe.getMessage());
		}

		return Optional.empty();
	}
}
