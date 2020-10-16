package org.epragati.aadhaarAPI.util;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataConverter {
	
	private static final Logger logger = LoggerFactory.getLogger(DataConverter.class);
	
	private final static ObjectMapper mapper = new ObjectMapper();
	
	public String objectTOJSON(Object obj) throws JsonProcessingException{ 
		//Object to JSON in String
		return mapper.writeValueAsString(obj);
	}
	/**
	 * 
	 * @param file Might be null
	 * @param cls
	 * @return Object
	 * @throws IOException
	 */
	public static Object jsonFileToObject(File file,Class cls, String aadharNumber) throws IOException{ 
		
		if(file==null){
			DataConverter dataConverter=new DataConverter();
			ClassLoader classLoader = dataConverter.getClassLoader();
			String fileName = "aadhar-response-"+aadharNumber+".json";
			logger.info(" File [{}] ", fileName );
			if(classLoader.getResource(fileName )==null) {
				fileName = "aadhar-response-1.json";
			}
			file = new File(classLoader.getResource(fileName ).getFile());
		}
			return mapper.readValue(file, cls);
	}
	private ClassLoader getClassLoader() {
		// TODO Auto-generated method stub
		return getClass().getClassLoader();
	}
}

