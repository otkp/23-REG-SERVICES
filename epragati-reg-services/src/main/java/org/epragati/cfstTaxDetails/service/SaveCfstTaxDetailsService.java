package org.epragati.cfstTaxDetails.service;

import javax.servlet.http.HttpServletRequest;

import org.epragati.regservice.vo.CfstTaxDetailsVO;
import org.springframework.data.util.Pair;

public interface SaveCfstTaxDetailsService {

	Pair<String,String>  saveCfstTaxDetails(CfstTaxDetailsVO cfstTaxDetailsVO,String authString,HttpServletRequest request);

}
