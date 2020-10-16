package org.epragati.collection.correction.service;

import java.lang.reflect.Field;
import java.util.Set;

import org.epragati.master.vo.MasterFields;

public interface FieldsCorrectionService {

	Set<MasterFields> getFeildNames(Object registrationDetailsDTO, String src,Field[] filed);

}
