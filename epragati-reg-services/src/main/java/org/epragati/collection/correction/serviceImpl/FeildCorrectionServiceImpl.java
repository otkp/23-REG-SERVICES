package org.epragati.collection.correction.serviceImpl;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.epragati.collection.correction.service.FieldsCorrectionService;
import org.epragati.master.vo.MasterFields;
import org.springframework.stereotype.Service;


@Service
public class FeildCorrectionServiceImpl implements FieldsCorrectionService {
   
	@Override
	public Set<MasterFields> getFeildNames(Object registrationDetailsDTO, String src,Field[] filed) {
		  Set<MasterFields> masterList = new HashSet<>(); 
         if(registrationDetailsDTO!=null){
        		 filed = registrationDetailsDTO.getClass().getDeclaredFields();
		for (Field fld : filed) {
			MasterFields master = new MasterFields();
		
			if (fld.getType().toString().contains("org.epragati")) {
				Class<?> targetType = fld.getType();
				Field[] fileds = null;
				Object objectValue = null;
				try {
					objectValue = targetType.newInstance();
					src = src+"."+fld.getName();
					master.setJsonFiled(src);
				} catch (Exception e1) {
					if (!fld.getType().getName().contains("reflect.Field")) {
						src = src+"."+fld.getName();
						master.setJsonFiled(src);
						String type = StringUtils.EMPTY;
						try {
							fld.setAccessible(true);
							if(fld.getType().getName().contains("java.lang")){
								type = fld.getType().getName().replaceAll("java.lang.", "");
							master.setFieldType(type);
							masterList.add(master);
							}else if(fld.getType().getName().contains("java.time")){
								type = fld.getType().getName().replaceAll("java.time.", "");
								master.setFieldType(type);
								masterList.add(master);
							}
							else if(fld.getType().getName().contains("java.util")){
								type = fld.getType().getName().replaceAll("java.util.", "");
								master.setFieldType(type);
								masterList.add(master);
							}else{
								type = fld.getType().getName();
								master.setFieldType(type);
								masterList.add(master);
							}
						} catch (Exception e) {
							master.setJsonFiled(src);
							master.setFieldValue("");
							master.setFieldType(type);
							masterList.add(master);
						}
					}
				}
				Set<MasterFields> data = getFeildNames(objectValue, src,fileds);
				if(!CollectionUtils.isEmpty(data)){
					masterList.addAll(data);
				}
				String[] splitted1 = src.split("\\.");
				 List<String> list2 = Arrays.asList(splitted1);  
				if(list2.get(0).isEmpty()&& list2.size()==2){
					src = StringUtils.EMPTY;
				}else{
					int i= 1;
					src = StringUtils.EMPTY;
					for(String str :list2){
						if(i<list2.size()){
							src = src+"."+str;
							if(src.equals(".")){
								src = StringUtils.EMPTY;
							}
						}
						i++;
					}	
				}
				
			} else {

				if (!fld.getType().getName().contains("reflect.Field")) {
					src = src+"."+fld.getName();
					master.setJsonFiled(src);
					String type = StringUtils.EMPTY;
					String value = StringUtils.EMPTY;
					try {
						fld.setAccessible(true);
						if(fld.getType().getName().contains("java.lang")){
							type = fld.getType().getName().replaceAll("java.lang.", "");
							master.setFieldType(type);
							masterList.add(master);
						}else if(fld.getType().getName().contains("java.time")){
							type = fld.getType().getName().replaceAll("java.time.", "");
							master.setFieldType(type);
							masterList.add(master);
						}
						else if(fld.getType().getName().contains("java.util")){
							type = fld.getType().getName().replaceAll("java.util.", "");
							master.setFieldType(type);
							masterList.add(master);
						}else{
							type = fld.getType().getName();
							master.setFieldType(type);
							masterList.add(master);
						}
						master.setFieldValue(value);
					} catch (Exception e) {
						master.setJsonFiled(src);
						master.setFieldType(type);
						masterList.add(master);
					}
					String[] splitted1 = src.split("\\.");
					List<String> list2 = Arrays.asList(splitted1);  
					if(list2.get(0).isEmpty()&& list2.size()==2){
						src = StringUtils.EMPTY;
					}else{
						int i= 1;
						src = StringUtils.EMPTY;
						for(String str :list2){
							if(i<list2.size()){
								src = src+"."+str;
								if(src.equals(".")){
									src = StringUtils.EMPTY;
								}
							}
							i++;
						}	
					}

				}

			}
			//System.out.println(master.getJsonFiled()+"  "+master.getFieldType());
			masterList.add(master);
		  }
			return masterList;
         }
     	return masterList;
	}
	
	
	public Class<?> setFieldValues(Object targetClass,Object sourceClass){
		
		
		
		
		
		return (Class<?>) targetClass;
		
	}
}        




