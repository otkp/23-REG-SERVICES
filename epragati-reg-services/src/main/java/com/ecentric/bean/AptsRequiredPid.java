package com.ecentric.bean;

public class AptsRequiredPid {

	/**
	 * @author srikanth.bommasani
	 */
	private static final long serialVersionUID = 1L;

	private byte[] encryptedSessionKey;
	
	private byte[] encryptedHamc;
	
	private byte[] encryptedPid;

	/**
	 * @return the encryptedSessionKey
	 */
	public byte[] getEncryptedSessionKey() {
		return encryptedSessionKey;
	}

	/**
	 * @param encryptedSessionKey the encryptedSessionKey to set
	 */
	public void setEncryptedSessionKey(byte[] encryptedSessionKey) {
		this.encryptedSessionKey = encryptedSessionKey;
	}

	/**
	 * @return the encryptedHamc
	 */
	public byte[] getEncryptedHamc() {
		return encryptedHamc;
	}

	/**
	 * @param encryptedHamc the encryptedHamc to set
	 */
	public void setEncryptedHamc(byte[] encryptedHamc) {
		this.encryptedHamc = encryptedHamc;
	}

	/**
	 * @return the encryptedPid
	 */
	public byte[] getEncryptedPid() {
		return encryptedPid;
	}

	/**
	 * @param encryptedPid the encryptedPid to set
	 */
	public void setEncryptedPid(byte[] encryptedPid) {
		this.encryptedPid = encryptedPid;
	}
	
	

}

