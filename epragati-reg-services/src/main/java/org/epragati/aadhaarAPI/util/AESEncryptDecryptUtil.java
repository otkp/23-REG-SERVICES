package org.epragati.aadhaarAPI.util;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

//AES Al
@Component
public class AESEncryptDecryptUtil {
	
	private static final Logger logger = LoggerFactory.getLogger(AESEncryptDecryptUtil.class);
	
	@Value("${encryption.secret.key:}")
	private String SECRET_KEY_1;
	
	private IvParameterSpec ivParameterSpec;
	
	private SecretKeySpec secretKeySpec;
	
	private Cipher cipher;

	@PostConstruct
    public void init() {
		try {
		
			secretKeySpec = new SecretKeySpec(SECRET_KEY_1.getBytes(), "AES");
			cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            byte[] iv = new byte[cipher.getBlockSize()];

            ivParameterSpec = new IvParameterSpec(iv);
			
		} catch (Exception e) {
			logger.error("Exception while EncryptDecryptUtil .ini() execution {0}",e);
		}

	}

	public String encrypt(String toBeEncrypt) {
		byte[] encrypted = null;
		try {
			cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
			encrypted = cipher.doFinal(toBeEncrypt.getBytes());
		} catch (Exception e) {
			logger.error("Exception while EncryptDecryptUtil .encrypt() execution {0}",e);
		}

		return Base64.encodeBase64String(encrypted);

	}

	public String decrypt(String encrypted) {
		byte[] decryptedBytes = null;
		try {
			cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
			decryptedBytes = cipher.doFinal(Base64.decodeBase64(encrypted));
		} catch (Exception e) {
			logger.error("Exception while EncryptDecryptUtil .decrypt() execution {0}",e);
		}

		return new String(decryptedBytes);
	}

}
