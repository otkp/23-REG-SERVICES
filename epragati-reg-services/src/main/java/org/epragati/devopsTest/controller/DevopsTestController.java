package org.epragati.devopsTest.controller;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.epragati.common.dao.PropertiesDAO;
import org.epragati.common.dto.PropertiesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class DevopsTestController {
	@Autowired
	private PropertiesDAO propertiesDAO;
	
	@GetMapping(value = "devopsTest", produces = { MediaType.TEXT_PLAIN_VALUE })
	@ResponseBody
	public String getWarUpStatus(@RequestHeader("Authorization") String authString) {
		
		if(StringUtils.isNoneEmpty(authString)){
			Optional<PropertiesDTO> authTokenValidate = propertiesDAO.findByDevopsTestTokenAndAuthorizationAllowedTrue(authString);
			if(authTokenValidate.isPresent()){
				return "Success";
			}
		}
		return "UnAuthorized Requset";
	}
}
