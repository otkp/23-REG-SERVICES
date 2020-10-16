package org.epragati.aadhaar.seed.engine; 
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.language.ColognePhonetic;

import info.debatty.java.stringsimilarity.NormalizedLevenshtein;

public class AadharSeedingEngine {

	public String getApprovalStatus(PersonDetails aadharDetails, PersonDetails regDetails) {
		String codeToPrint = getVerifyCode(aadharDetails, regDetails);
		int code=  Integer.parseInt(codeToPrint);
		//System.out.println(codeToPrint);
		return codeToPrint;
		/*if (Arrays.asList(VerificationConstants.AUTO_APPROVAL_CODE).contains(code)) {
			return VerificationConstants.AUTO_APPROVAL;
		} else if (Arrays.asList(VerificationConstants.CCO_APPROVAL_CODE).contains(code)) {
			return VerificationConstants.CCO_APPROVAL;
		} else {
			return VerificationConstants.NOT_FOUND;
		}*/

	}

	public String getVerifyCode(PersonDetails aadharDetails, PersonDetails regDetails) {
		int aadharCode = fullOrNoMatch(aadharDetails.getAadharNo(), regDetails.getAadharNo());
		int nameCode = verifyNameParameter(aadharDetails.getName(), regDetails.getName());
		int fatherNameCode = verifyNameParameter(aadharDetails.getFatherName(), regDetails.getFatherName());
		//int dobCode = fullOrNoMatch(aadharDetails.getDob(), regDetails.getDob());
		int mandalCode = stringFullOrNoMatch(aadharDetails.getMandal(), regDetails.getMandal());
		int addressCode = verifyAddressParameter(aadharDetails.getAddress(), regDetails.getAddress());
		int districtCode=stringFullOrNoMatch(aadharDetails.getDistrict(),regDetails.getDistrict());
		// aadharcode+namecode+fathernamecode+dobcode+mandalcode+addresscode+districtcode
		
		//String code = "" + aadharCode + nameCode + fatherNameCode + dobCode + mandalCode + addressCode+districtCode;
		String code = "" + aadharCode + nameCode + fatherNameCode + mandalCode + addressCode+districtCode;
		return code;

	}

	public int fullOrNoMatch(String eKYC, String reg) {
		if (eKYC.equals(reg)) {
			return 2;
		} else {
			return 0;
		}
	}

	public int stringFullOrNoMatch(String eKYC, String reg) {
		eKYC = removeStopWords(eKYC.toLowerCase()).replaceAll("[^a-zA-Z ]+|\\s{2,}", " ");
		reg = removeStopWords(reg.toLowerCase()).replaceAll("[^a-zA-Z ]+|\\s{2,}", " ");
		int phoneticCode=verifyFullOrNoPhonetic(eKYC,reg," ");
		return phoneticCode;
	}


	public int verifyFullOrNoPhonetic(String eKYC, String reg, String splitBy) {
		if(eKYC.isEmpty() && reg.isEmpty()) {
			return 0;
		}
		//Replacing double spaces
		eKYC = eKYC.replaceAll("\\s{2,}", " ");
		reg = reg.replaceAll("\\s{2,}", " ");
		if (eKYC.equals(reg)) {
			return 2;
		} 
		List<Boolean> phoResult = new ArrayList<Boolean>();

		ColognePhonetic phonetic = new ColognePhonetic();
		String[] ekyName = eKYC.split(splitBy);
		String[] regName = reg.split(splitBy);

		for (String en : ekyName) {
			for (String rn : regName) {
				if(rn.length()>2){
					if (!phonetic.isEncodeEqual(rn, en)) {
						phoResult.add(false);
					} else {

						phoResult.add(true);
					}}else{
						if (!rn.equalsIgnoreCase(en)) {
							phoResult.add(false);
						} else {

							phoResult.add(true);
						}	
					}
			}
		}

		int wordLength=ekyName.length;
		int regWordLength=regName.length;
		int matchedWords=Collections.frequency(phoResult, true);



		if(matchedWords == wordLength){
			if(wordLength==regWordLength)
				return 2;
			else
				return 0;

		}else{
			return 0;
		}
	}






	public int verifyNameParameter(String eKYC, String reg) {
		eKYC = removeStopWords(eKYC.toLowerCase()).replaceAll("[^a-zA-Z ]+|\\s{2,}", " ").trim();
		reg = removeStopWords(reg.toLowerCase()).replaceAll("[^a-zA-Z ]+|\\s{2,}", " ").trim();
		int phoneticCode=verifyPhonetic(eKYC, reg, " ");
		if (phoneticCode!=0) {
			return phoneticCode;
		}else {
			return verifyStringDistance(eKYC, reg, 60);
		}

	}

	public int verifyAddressParameter(String eKYC, String reg) {
		eKYC = eKYC.replaceAll("[^a-zA-Z, ]+|\\s{2,}", "").toLowerCase().trim();
		reg = reg.replaceAll("[^a-zA-Z, ]+|\\s{2,}", "").toLowerCase().trim();
		int phoneticCode=verifyPhonetic(eKYC, reg, ",");
		if (phoneticCode!=0) {
			return phoneticCode;
		} else {
			return verifyStringDistance(eKYC, reg, 50);
		}
	}


	public int verifyPhonetic(String eKYC, String reg, String splitBy) {
		if(eKYC.isEmpty() && reg.isEmpty()) {
			return 0;
		}
		if (eKYC.equals(reg)) {
			return 2;
		} else {
			List<Boolean> phoResult = new ArrayList<Boolean>();
			int dig2Character=0;
			int dig2gCharacter=0;
			ColognePhonetic phonetic = new ColognePhonetic();
			String[] ekyName = eKYC.split(splitBy);
			String[] regName = reg.split(splitBy);

			for (String en : ekyName) {
				for (String rn : regName) {
					if(rn.length()>2){
						if (!phonetic.isEncodeEqual(rn, en)) {
							phoResult.add(false);
						} else {
							dig2gCharacter++;
							phoResult.add(true);
						}}else{
							if (!rn.equalsIgnoreCase(en)) {
								phoResult.add(false);
							} else {
								dig2Character++;
								phoResult.add(true);
							}	
						}
				}
			}

			int wordLength=ekyName.length;
			int regWordLength=regName.length;
			int matchedWords=Collections.frequency(phoResult, true);



			if(matchedWords == wordLength){
				if(wordLength==regWordLength)
					return 2;
				else
					return 1;
			}else if(matchedWords >= 1){
				if(dig2gCharacter>=1){
					return 1;
				}else if(dig2Character>=3){
					return 1;
				}else{
					return 0;
				}

			}else{
				return 0;
			}




		}

	}

	public int verifyStringDistance(String eKYC, String reg, int percentage) {
		// double matchPercentage=cosineSimilarity(eKYC,reg);
		NormalizedLevenshtein nl = new NormalizedLevenshtein();
		double matchPercentage = nl.distance(eKYC, reg) * 100;
		if (matchPercentage == 0) {
			return 2;
		} else if (matchPercentage < percentage && matchPercentage > 0) {
			return 1;
		} else {
			return verifyCosineStringDistance(eKYC, reg);
		}
	}

	public int verifyCosineStringDistance(String eKYC, String reg) {
		double matchPercentage = cosineSimilarity(eKYC, reg);

		if (matchPercentage >= 90) {
			return 2;
		} else if (matchPercentage > 50 && matchPercentage < 90) {
			return 1;
		} else {
			return 0;
		}
	}

	public static Map<String, Integer> getTermFrequencyMap(String[] terms) {
		Map<String, Integer> termFrequencyMap = new HashMap<String, Integer>();
		for (String term : terms) {
			Integer n = termFrequencyMap.get(term);
			n = (n == null) ? 1 : ++n;
			termFrequencyMap.put(term, n);
		}
		return termFrequencyMap;
	}

	public static double cosineSimilarity(String text1, String text2) {
		// Get vectors
		Map<String, Integer> a = getTermFrequencyMap(text1.split("\\W+"));
		Map<String, Integer> b = getTermFrequencyMap(text2.split("\\W+"));

		// Get unique words from both sequences
		HashSet<String> intersection = new HashSet<String>(a.keySet());
		intersection.retainAll(b.keySet());

		double dotProduct = 0, magnitudeA = 0, magnitudeB = 0;

		// Calculate dot product
		for (String item : intersection) {
			dotProduct += a.get(item) * b.get(item);
		}

		// Calculate magnitude a
		for (String k : a.keySet()) {
			magnitudeA += Math.pow(a.get(k), 2);
		}

		// Calculate magnitude b
		for (String k : b.keySet()) {
			magnitudeB += Math.pow(b.get(k), 2);
		}

		// return cosine similarity
		return (dotProduct / Math.sqrt(magnitudeA * magnitudeB)) * 100;
	}

	public String removeStopWords(String input) {
		return input.replaceAll("s/o|f/o|w/o", " ");

	}

}
