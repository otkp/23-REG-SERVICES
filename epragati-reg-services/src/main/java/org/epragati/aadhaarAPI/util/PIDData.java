package org.epragati.aadhaarAPI.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Arrays;

import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.epragati.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PIDData {

	private static final Logger logger = LoggerFactory.getLogger(PIDData.class);
	private byte[] sessionKey;
	private String wadh = StringUtils.EMPTY;
	private String cerpath = StringUtils.EMPTY;
	private String otp = StringUtils.EMPTY;
	private String tsvalue;
	private PIDParams datavalues;
	
	// Authentication tag length - in bits
    public static final int AUTH_TAG_SIZE_BITS = 128; 


	public void setPIDData(String otpval, String wadhStr, String digcerpath,String version) {
		cerpath = digcerpath;
		wadh = wadhStr;
		otp = otpval;
		this.datavalues.skey = CreateSessionKey();// skey formation
		// XmlDocument pidBlock = createPIDXml(); // pid block formation
		String xmlPlain=getXMLPlain(version);
		this.datavalues.pid = getPid(xmlPlain);// Encrypt PID with dynamic session key
		this.datavalues.hmac = createHMAC(xmlPlain);// compute hash of pid.encrypt and then encode

	}
	

	private String CreateSessionKey() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null);// Make an empty store
			InputStream fis = new FileInputStream(new File(URLDecoder.decode(cerpath,"UTF-8")));
			BufferedInputStream bis = new BufferedInputStream(fis);

			CertificateFactory cf = CertificateFactory.getInstance("X.509");

			Certificate cert = cf.generateCertificate(bis);

			X509Certificate x509Cert = ((X509Certificate) cert);
			this.datavalues.ci = DateUtil.getDate(DateUtil.DATE_PATTERN_4, "IST", x509Cert.getNotAfter());
			AESCipher aesCipher = new AESCipher();
			this.tsvalue = aesCipher.getCurrentISOTimeInUTF8();
			this.sessionKey=aesCipher.generateSessionKey();

			PublicKey pubKey = cert.getPublicKey();
			Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1PADDING");
			cipher.init(Cipher.ENCRYPT_MODE, pubKey);
			
			byte[] plainBytes = this.sessionKey;
			byte[] cipherData = cipher.doFinal(plainBytes);
			
			IOUtils.closeQuietly(bis);
			IOUtils.closeQuietly(fis);
			
			return Base64.encodeBase64String(cipherData);
			
		}catch (Exception  e) {
			logger.error("{}",e);
			throw new BadRequestException(e.getMessage());
		}
	}
	
	private String createHMAC(String plainText) {
		try {
			byte[] inputBytes = encodeUTF8(plainText);
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] plainTextBytes = digest.digest(inputBytes);
			byte[] tsBytes = encodeUTF8(this.tsvalue);

			byte[] data = tsBytes;
			byte[] iv_ts_last12 = new byte[12];

			int startIndex=data.length - 12;
			iv_ts_last12=  Arrays.copyOfRange(data, startIndex, data.length);


			byte[] aad_ts_last16 = new byte[16];
			startIndex=data.length - 16;
			aad_ts_last16=  Arrays.copyOfRange(data, startIndex, data.length);

			AEADParameters aeadParam = new AEADParameters(new KeyParameter(this.sessionKey), AUTH_TAG_SIZE_BITS, iv_ts_last12, aad_ts_last16);
			//create aesgcm cipher with IV/Nonce and AAD
			GCMBlockCipher encryptCipher = new GCMBlockCipher(new AESEngine());
			encryptCipher.init(true, aeadParam);

			//get encrypted data with auth tag at end
			byte[] output = new byte[encryptCipher.getOutputSize(plainTextBytes.length)];
			int len = encryptCipher.processBytes(plainTextBytes, 0, plainTextBytes.length, output, 0);
			encryptCipher.doFinal(output, len);		
			
			return Base64.encodeBase64String(output);
		} catch (Exception e) {
			logger.error("{}",e);
			throw new BadRequestException(e.getMessage());
		}

	}
	private String getXMLPlain(String version) {
		version="2.0";
		String plainText= "<Pid ts=\""+this.tsvalue+"\" ver=\""+version+"\" wadh=\""+this.wadh+"\" xmlns=\"http://www.uidai.gov.in/authentication/uid-auth-request/1.0\"><Pv otp=\""+this.otp+"\"/></Pid>";
		return plainText;
	}
	private String getPid(String plainText) {
		try {
			byte[] plainTextBytes =encodeUTF8(plainText); //StandardCharsets.UTF_8.encode(plainText).array();//encodeUTF8(xmlPlain);
			byte[] tsBytes = encodeUTF8(this.tsvalue);//StandardCharsets.UTF_8.encode(this.tsvalue).array();//

			byte[] data = tsBytes;
			byte[] iv_ts_last12 = new byte[12];

			int startIndex=data.length - 12;
			iv_ts_last12=  Arrays.copyOfRange(data, startIndex, data.length);


			byte[] aad_ts_last16 = new byte[16];
			startIndex=data.length - 16;
			aad_ts_last16=  Arrays.copyOfRange(data, startIndex, data.length);
			
			AEADParameters aeadParam = new AEADParameters(new KeyParameter(this.sessionKey), AUTH_TAG_SIZE_BITS, iv_ts_last12, aad_ts_last16);

			//create aesgcm cipher with IV/Nonce and AAD
			GCMBlockCipher encryptCipher = new GCMBlockCipher(new AESEngine());
			encryptCipher.init(true, aeadParam);
			//get encrypted data with auth tag at end
			byte[] output = new byte[encryptCipher.getOutputSize(plainTextBytes.length)];
			int len = encryptCipher.processBytes(plainTextBytes, 0, plainTextBytes.length, output, 0);
			encryptCipher.doFinal(output, len);
			
			byte[] finalVal = new byte[output.length + tsBytes.length];
			System.arraycopy(tsBytes, 0, finalVal, 0, tsBytes.length);
			System.arraycopy(output, 0, finalVal, tsBytes.length, output.length);
			return Base64.encodeBase64String(finalVal);
			
		} catch (Exception e) {
			logger.error("{}",e);
			throw new BadRequestException(e.getMessage());
		} 

	}
	
	private byte[] encodeUTF8(String string) {
		try {
			return string.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			logger.error("{}",e);
			throw new BadRequestException(e.getMessage());
		}
	}
	public class PIDParams {

		private String pid;
		private String skey;
		private String hmac;
		private String ci;

		/**
		 * @return the pid
		 */
		public String getPid() {
			return pid;
		}

		/**
		 * @param pid
		 *            the pid to set
		 */
		public void setPid(String pid) {
			this.pid = pid;
		}

		/**
		 * @return the skey
		 */
		public String getSkey() {
			return skey;
		}

		/**
		 * @param skey
		 *            the skey to set
		 */
		public void setSkey(String skey) {
			this.skey = skey;
		}

		/**
		 * @return the hmac
		 */
		public String getHmac() {
			return hmac;
		}

		/**
		 * @param hmac
		 *            the hmac to set
		 */
		public void setHmac(String hmac) {
			this.hmac = hmac;
		}

		/**
		 * @return the ci
		 */
		public String getCi() {
			return ci;
		}

		/**
		 * @param ci
		 *            the ci to set
		 */
		public void setCi(String ci) {
			this.ci = ci;
		}

	}
	public PIDData() {

		datavalues = new PIDParams();
	}
	public PIDParams getPidValues() {
		return this.datavalues;
	}
}
