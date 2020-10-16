/**
 * 
 */
package org.epragati.aadhaarAPI.util;

/**
 * @author sprusty
 *
 */
public enum ResponseStatusEnum {
	FOO;
	public enum LLRFRESHRESPONSE{

		SUCCESS("Success"),
		FAILLURE("Failed"),
		RECORDUNAVILABLE("Record Not Found."),
		TRYWITHVALIDDATA("Try Again with Valid Data."),
		ADHARAUTHENTICATIONFAILED("Adhar authentication failed."),
		ADHARARNOTFOUND("Adhar Details Not Found."),
		ADHARARNOTSEED("Aadhar not seeded."),
		INVALIDUSERROLE("In valid user for this operation");
         
		private String responseStatus;

		public String getResponseStatus() {
			return responseStatus;
		}

		public void setResponseStatus(String responseStatus) {
			this.responseStatus = responseStatus;
		}

		LLRFRESHRESPONSE(String responseStatus){
			this.responseStatus = responseStatus;
		}
	}
	public enum AADHAARRESPONSE{
		SUCCESS("SUCCESS"),
		FAILED("FAILED");
		private String responseStatus;

		public  String getResponseStatus() {
			return responseStatus;
		}

		public void setResponseStatus(String responseStatus) {
			this.responseStatus = responseStatus;
		}

		AADHAARRESPONSE(String responseStatus){
			this.responseStatus = responseStatus;
		}
	}


}
