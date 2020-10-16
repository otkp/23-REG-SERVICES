package org.epragati.aadhaarAPI;
import org.epragati.aadhaar.AadharDetailsResponseVO;
import org.epragati.common.mappers.BaseMapper;
import org.epragati.master.dto.AadhaarDetailsResponseDTO;
import org.springframework.stereotype.Component;

import com.ecentric.bean.BiometricresponseBean;


/**
 * @author srikanth.bommasani
 *
 */
@Component
public class AptsAadhaarResponseMapper extends BaseMapper<com.ecentric.bean.BiometricresponseBean, AadharDetailsResponseVO>{

	@Override
	public AadharDetailsResponseVO convertEntity(com.ecentric.bean.BiometricresponseBean dto) {
		AadharDetailsResponseVO aadharDetailsResponseVO = new AadharDetailsResponseVO();
		funPoint(dto.getAuth_date(), aadharDetailsResponseVO::setAuth_date);
		funPoint(dto.getAuth_err_code(), aadharDetailsResponseVO::setAuth_err_code);
		funPoint(dto.getAuth_status(), aadharDetailsResponseVO::setAuth_status);
		funPoint(dto.getAuth_transaction_code(), aadharDetailsResponseVO::setAuth_transaction_code);
		funPoint(dto.getBase64file(), aadharDetailsResponseVO::setBase64file);
		funPoint(dto.getBuildingName(), aadharDetailsResponseVO::setBuildingName);
		funPoint(dto.getCareof(),aadharDetailsResponseVO::setCareof);
		funPoint(dto.getCo(), aadharDetailsResponseVO::setCo);
		funPoint(dto.getDistrict(), aadharDetailsResponseVO::setDistrict);
		funPoint(dto.getDistrict_name(), aadharDetailsResponseVO::setDistrict_name);
		funPoint(dto.getDob(), aadharDetailsResponseVO::setDob);
		funPoint(dto.getEid(), aadharDetailsResponseVO::setEid);
		funPoint(dto.getEmail(),aadharDetailsResponseVO::setEmail);
		funPoint(dto.getGender(), aadharDetailsResponseVO::setGender);
		funPoint(dto.getHouse(), aadharDetailsResponseVO::setHouse);
		funPoint(dto.getKSA_KUA_Txn(), aadharDetailsResponseVO::setKSA_KUA_Txn);
		funPoint(dto.getLandmark(), aadharDetailsResponseVO::setLandmark);
		funPoint(dto.getLc(), aadharDetailsResponseVO::setLc);
		funPoint(dto.getMandal(), aadharDetailsResponseVO::setMandal);
		funPoint(dto.getMandal_name(), aadharDetailsResponseVO::setMandal_name);
		funPoint(dto.getName(), aadharDetailsResponseVO::setName);
		funPoint(dto.getOrgnlAuth_ErrorCode(), aadharDetailsResponseVO::setOrgnlAuth_ErrorCode);
		funPoint(dto.getOrgnlAuth_Status(), aadharDetailsResponseVO::setOrgnlAuth_Status);
		funPoint(dto.getPhoneNumber(), aadharDetailsResponseVO::setPhoneNumber);
		funPoint(dto.getPincode(), aadharDetailsResponseVO::setPincode);
		funPoint(dto.getPo(), aadharDetailsResponseVO::setPo);
		funPoint(dto.getRrn(), aadharDetailsResponseVO::setRrn);
		funPoint(dto.getSrdhwstxn(), aadharDetailsResponseVO::setSrdhwstxn);
		funPoint(dto.getStatecode(), aadharDetailsResponseVO::setStatecode);
		funPoint(dto.getStreet(), aadharDetailsResponseVO::setStreet);
		funPoint(dto.getSubdist(), aadharDetailsResponseVO::setSubdist);
		if(dto.getUid()!=null){
			Long uidNum = Long.parseLong(dto.getUid());
			aadharDetailsResponseVO.setUid(uidNum);
		}
		funPoint(dto.getUIDAIeKYCTxn(), aadharDetailsResponseVO::setUIDAIeKYCTxn);
		funPoint(dto.getUuId(), aadharDetailsResponseVO::setUuId);
		funPoint(dto.getVillage(), aadharDetailsResponseVO::setVillage);
		funPoint(dto.getVillage_name(), aadharDetailsResponseVO::setVillage_name);
		funPoint(dto.getVtc(), aadharDetailsResponseVO::setVtc);
		
		return aadharDetailsResponseVO;
	
	}
	
	public AadhaarDetailsResponseDTO convertDTO(AadharDetailsResponseVO aadharDetailsResponseVO){
		AadhaarDetailsResponseDTO dto = new AadhaarDetailsResponseDTO();
		funPoint(aadharDetailsResponseVO.getAuth_date(), dto::setAuth_date);
		funPoint(aadharDetailsResponseVO.getAuth_err_code(), dto::setAuth_err_code);
		funPoint(aadharDetailsResponseVO.getAuth_status(), dto::setAuth_status);
		funPoint(aadharDetailsResponseVO.getAuth_transaction_code(), dto::setAuth_transaction_code);
		funPoint(aadharDetailsResponseVO.getBase64file(), dto::setBase64file);
		funPoint(aadharDetailsResponseVO.getBuildingName(), dto::setBuildingName);
		funPoint(aadharDetailsResponseVO.getCareof(),dto::setCareof);
		funPoint(aadharDetailsResponseVO.getCo(), dto::setCo);
		funPoint(aadharDetailsResponseVO.getDistrict(), dto::setDistrict);
		funPoint(aadharDetailsResponseVO.getDistrict_name(), dto::setDistrict_name);
		funPoint(aadharDetailsResponseVO.getDob(), dto::setDob);
		funPoint(aadharDetailsResponseVO.getEid(), dto::setEid);
		funPoint(aadharDetailsResponseVO.getEmail(),dto::setEmail);
		funPoint(aadharDetailsResponseVO.getGender(), dto::setGender);
		funPoint(aadharDetailsResponseVO.getHouse(), dto::setHouse);
		funPoint(aadharDetailsResponseVO.getKSA_KUA_Txn(), dto::setKSA_KUA_Txn);
		funPoint(aadharDetailsResponseVO.getLandmark(), dto::setLandmark);
		funPoint(aadharDetailsResponseVO.getLc(), dto::setLc);
		funPoint(aadharDetailsResponseVO.getMandal(), dto::setMandal);
		funPoint(aadharDetailsResponseVO.getMandal_name(), dto::setMandal_name);
		funPoint(aadharDetailsResponseVO.getName(), dto::setName);
		funPoint(aadharDetailsResponseVO.getOrgnlAuth_ErrorCode(), dto::setOrgnlAuth_ErrorCode);
		funPoint(aadharDetailsResponseVO.getOrgnlAuth_Status(), dto::setOrgnlAuth_Status);
		funPoint(aadharDetailsResponseVO.getPhoneNumber(), dto::setPhoneNumber);
		funPoint(aadharDetailsResponseVO.getPincode(), dto::setPincode);
		funPoint(aadharDetailsResponseVO.getPo(), dto::setPo);
		funPoint(aadharDetailsResponseVO.getRrn(), dto::setRrn);
		funPoint(aadharDetailsResponseVO.getSrdhwstxn(), dto::setSrdhwstxn);
		funPoint(aadharDetailsResponseVO.getStatecode(), dto::setStatecode);
		funPoint(aadharDetailsResponseVO.getStreet(), dto::setStreet);
		funPoint(aadharDetailsResponseVO.getSubdist(), dto::setSubdist);
		funPoint(aadharDetailsResponseVO.getUid(), dto::setUid);
		funPoint(aadharDetailsResponseVO.getUIDAIeKYCTxn(), dto::setUidaieKYCTxn);
		funPoint(aadharDetailsResponseVO.getUuId(), dto::setUuId);
		funPoint(aadharDetailsResponseVO.getVillage(), dto::setVillage);
		funPoint(aadharDetailsResponseVO.getVillage_name(), dto::setVillage_name);
		funPoint(aadharDetailsResponseVO.getVtc(), dto::setVtc);
		
		return dto;
		
	}

}

