package org.epragati.aadhaarAPI.util;

public enum AadhaarConstant {
	SERVICETYPE;

	public enum ServiceType{

		EKYC("EKYC"),OPT("OTP");
		private String content;

		private ServiceType(String content) {
			this.content = content;
		}

		/**
		 * @return the content
		 */
		public String getContent() {
			return content;
		}

		/**
		 * @param content the content to set
		 */
		public void setContent(String content) {
			this.content = content;
		}

	}
	public enum BiometricType{

		FMR("FMR"),OPT("OTP"),EKYCOTP("EKYCOTP");
		private String content;

		private BiometricType(String content) {
			this.content = content;
		}

		/**
		 * @return the content
		 */
		public String getContent() {
			return content;
		}

		/**
		 * @param content the content to set
		 */
		public void setContent(String content) {
			this.content = content;
		}


	}
	public enum RequestType{

		EKYC("EKYC"),OPT("OTP");

		private String content;

		private RequestType(String content) {
			this.content = content;
		}

		/**
		 * @return the content
		 */
		public String getContent() {
			return content;
		}

		/**
		 * @param content the content to set
		 */
		public void setContent(String content) {
			this.content = content;
		}

	}
	public enum OtpChannel{

		MOBILE_EMAIL("00"),MOBILE("01"),EMAIL("02");

		private String content;

		private OtpChannel(String content) {
			this.content = content;
		}

		/**
		 * @return the content
		 */
		public String getContent() {
			return content;
		}

		/**
		 * @param content the content to set
		 */
		public void setContent(String content) {
			this.content = content;
		}

	}
	public enum AttemptType{

		OPT("1OR"),EKAYCOPT("1OA");

		private String content;

		private AttemptType(String content) {
			this.content = content;
		}

		/**
		 * @return the content
		 */
		public String getContent() {
			return content;
		}

		/**
		 * @param content the content to set
		 */
		public void setContent(String content) {
			this.content = content;
		}

	}


}
