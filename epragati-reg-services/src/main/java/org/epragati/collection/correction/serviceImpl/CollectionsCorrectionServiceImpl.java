package org.epragati.collection.correction.serviceImpl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.actions.dao.CollectionCorrectionDAO;
import org.epragati.actions.dao.CorrectionHeaderInfoDAO;
import org.epragati.actions.dto.CorrectionDTO;
import org.epragati.actions.dto.CorrectionHeaderInfo;
import org.epragati.actions.dto.CorrectionParamsDTO;
import org.epragati.actions.dto.CorrectionRoles;
import org.epragati.collection.correction.service.CollectionCorrectionServices;
import org.epragati.collection.correction.service.FieldsCorrectionService;
import org.epragati.common.vo.CorrectionParamsVO;
import org.epragati.common.vo.CorrectionVO;
import org.epragati.exception.BadRequestException;
import org.epragati.master.vo.MasterFields;
import org.epragati.rta.vo.CorrectionsVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jayway.jsonpath.JsonPath;
/**
 * 
 * @author krishnarjun.pampana
 *
 */
@Service
public class CollectionsCorrectionServiceImpl implements CollectionCorrectionServices {
	
	private static final Logger logger = LoggerFactory.getLogger(CollectionsCorrectionServiceImpl.class);
	
	@Autowired
	private CollectionCorrectionDAO collectionDAO;
	
	@Autowired
	private FieldsCorrectionService fieldsCorrectionService;
	
	@Autowired
	private CorrectionHeaderInfoDAO correctionHeaderInfoDAO;
	
	@Override
	public Map<String,List<CorrectionVO>> getRegistrationDetails(CorrectionsVO collectionCorrectionVO){
		List<CorrectionDTO> dtos = collectionDAO.findByApplicationTypeAndRolesRoleNameAndStatusTrue(collectionCorrectionVO.getServiceType(),collectionCorrectionVO.getSelectedRole());
		
		if(CollectionUtils.isEmpty(dtos)){
			logger.debug("corrections collection not available");
			 throw new BadRequestException("Data is not available for correction");
		}

		List<CorrectionVO> corrVoList = new ArrayList<>();
		  
		  try{
			       String jsonStr = StringUtils.EMPTY;
			       ObjectMapper mapperObj = new ObjectMapper();
			        try {
			        	 mapperObj.registerModule(new JavaTimeModule());
			        	 mapperObj.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
			             jsonStr = mapperObj.writeValueAsString(collectionCorrectionVO.getTargetCollection());
			        } catch (IOException e) {
			            e.printStackTrace();
			        }
			       Field[] filed = null;
			   	Set<MasterFields> fields = fieldsCorrectionService.getFeildNames(collectionCorrectionVO.getSourceCollection(), StringUtils.EMPTY, filed);
			   	fields.stream().forEach(field->{
			   		String fieldName = removeFirstChar(field.getJsonFiled(),1);
			   		field.setJsonFiled(fieldName);
			   	});
			   	for(MasterFields masterFields:fields){
			   		try{
			   		masterFields.setFieldValue(JsonPath.read(jsonStr, "$."+masterFields.getJsonFiled()).toString());
			  
			   		}catch(Exception e){
			   			masterFields.setFieldValue(StringUtils.EMPTY);
			   		}
			   	}
			   
			   	for(MasterFields masterFields: fields){
			   		CorrectionDTO corrctionDTO = getElement(masterFields.getJsonFiled(),dtos);
		            if(corrctionDTO!=null){
		            	CorrectionRoles mode = getRoleElement(collectionCorrectionVO.getSelectedRole(),corrctionDTO.getRoles());
		            	CorrectionVO corrVo = new CorrectionVO();
		            	corrVo.setJsonPath(masterFields.getJsonFiled());
		            	corrVo.setFieldValue(masterFields.getFieldValue());
		            	corrVo.setDataType(masterFields.getFieldType());
		            	corrVo.setMode(mode.getMode());
		            	corrVo.setApi(corrctionDTO.getApi());
		            	corrVo.setFeildLabel(corrctionDTO.getFeildLabel());
		            	corrVo.setRegExpression(corrctionDTO.getRegExpression());
		            	corrVo.setFieldType(corrctionDTO.getFieldType());
		            	corrVo.setNotify(corrctionDTO.getNotify());
		            	if(CollectionUtils.isNotEmpty(corrctionDTO.getApiParam())){
		            		List<CorrectionParamsVO> list = new ArrayList<>();
		            		for(CorrectionParamsDTO dto : corrctionDTO.getApiParam()){
		            			CorrectionParamsVO vo = new CorrectionParamsVO();
		            			vo.setName(dto.getName());
		            			vo.setPath(dto.getPath());
		            			list.add(vo);
		            		}
		            		corrVo.setApiParam(list);
		            	}
		            	
		            	corrVo.setOnChange(corrctionDTO.getOnChange());
		            	corrVo.setNamePath(corrctionDTO.getNamePath());
		            	corrVo.setValuePath(corrctionDTO.getValuePath());
		            	corrVoList.add(corrVo);
		            	
		            }
			  } 	
		  }catch(Exception e){
			  e.printStackTrace();
		  }
		  List<CorrectionHeaderInfo> headerInfoList = correctionHeaderInfoDAO.findByModule(collectionCorrectionVO.getServiceType());
		 
		return  requsetedFields(headerInfoList,corrVoList);
	} 
	
    // To remove starting characters from string 
	private String removeFirstChar(String s,Integer value){
		if(s.length()>value){
			 return s.substring(value);
		}
	   return s;
	}
	//To get Required element/object from list
	private CorrectionDTO getElement(String name,List<CorrectionDTO> dtos){
		 return dtos.stream().
				 filter(p -> p.getJsonPath().equals(name)).
				 findAny().orElse(null);
	}
	
	private CorrectionRoles getRoleElement(String name,List<CorrectionRoles> dtos){
		 return dtos.stream().
				 filter(p -> p.getRoleName().equals(name)).
				 findAny().orElse(null);
	}
	
	private Map<String,List<CorrectionVO>> requsetedFields(List<CorrectionHeaderInfo> info,List<CorrectionVO> corrVo){
		Map<String,List<CorrectionVO>> finalFields = new HashMap<String,List<CorrectionVO>>();
		
		for(CorrectionHeaderInfo corrInfo : info){
			List<CorrectionVO> voList = new ArrayList<>();
			for(String str : corrInfo.getJsonPath()){
				CorrectionVO vo = getHeaderWiseElement(str,corrVo);
				if(vo!=null){
					voList.add(vo);
				}
			}
			finalFields.put(corrInfo.getHeaderName(), voList);
		}
		return finalFields;
	}
	private CorrectionVO getHeaderWiseElement(String name,List<CorrectionVO> corrVo){
		 return corrVo.stream().
				 filter(p -> p.getJsonPath().equals(name)).
				 findAny().orElse(null);
	}
	
}
