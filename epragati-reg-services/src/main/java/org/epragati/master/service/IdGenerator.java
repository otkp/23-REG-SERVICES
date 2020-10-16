package org.epragati.master.service;
/**
 * 
 * @author saikiran.kola
 *
 */
public interface IdGenerator {

	/** 
	 * generate sequence Id based on seqName and Office code
	 * @param seqName
	 * @param officeCode
	 * @return
	 */
	public String generateSequence(String seqName,String officeCode,Integer inrementor);
	
	
	
}
