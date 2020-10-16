package org.epragati.apts.aadhaar;


import org.epragati.aadhaar.AadhaarDetailsRequestVO;
import org.epragati.aadhar.APIResponse;

import com.ecentric.bean.BiometricresponseBean;

public interface ConsumeAptsAadhaarService {

	public  APIResponse<BiometricresponseBean> cosumeAptsAaadhaarResponse(AadhaarDetailsRequestVO model);
	
}

