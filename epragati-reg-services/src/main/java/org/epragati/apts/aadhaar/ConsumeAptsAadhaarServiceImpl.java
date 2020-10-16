package org.epragati.apts.aadhaar;

import java.beans.XMLDecoder;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.MessageDigest;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.xml.ws.WebServiceException;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.crypto.engines.AESFastEngine;
import org.bouncycastle.crypto.modes.GCMBlockCipher;
import org.bouncycastle.crypto.params.AEADParameters;
import org.bouncycastle.crypto.params.KeyParameter;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.epragati.aadhaar.AadhaarDetailsRequestVO;
import org.epragati.aadhaarAPI.DAO.AadhaarTransactionDAO;
import org.epragati.aadhaarAPI.DTO.AadhaarTransactionDTO;
import org.epragati.aadhaarAPI.util.AadhaarConstant;
import org.epragati.aadhar.APIResponse;
import org.epragati.exception.BadRequestException;
import org.jpos.iso.ISOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.ecentric.bean.AptsRequiredPid;
import com.ecentric.bean.BiometricresponseBean;



@Service
public class ConsumeAptsAadhaarServiceImpl implements ConsumeAptsAadhaarService {

	/**
	 * @author srikanth.bommasani
	 */

	@Autowired
	private static final Logger logger = LoggerFactory.getLogger(ConsumeAptsAadhaarServiceImpl.class);

	private static String timestamp;
	private static Date date = new Date();

	private static final String ASYMMETRIC_ALGO = "RSA/ECB/PKCS1Padding";
	private static final int SYMMETRIC_KEY_SIZE = 256;
	private static final String CERTIFICATE_TYPE = "X.509";
	private static final String JCE_PROVIDER = "BC";
	public final static String DATE_PATTERN_4 = "yyyyMMdd";

	private byte[] echmacBytes;
	private byte[] encSkeyBytes;
	private byte[] encPidBytes;
	private PublicKey publicKey;
	private String skey;
	private String certIdentifier;
	private Date certExpiryDate;

	@Value("${opt.cer.file.path}")
	private String otpCerFilePath;

	@Value("${apts.agencyname.aadhaar}")
	private String agencyName;

	@Value("${apts.agencycode.aadhaar}")
	private String agenyCode;

	@Value("${rdservice.datatype.value}")
	private String rdServiceDataType;

	@Value("${requestType.biometric.value}")
	private String biometricEkycOption;

	@Value("${requestType.iris.value}")
	private String irisEkycOption;
	@Value("${apts.soap.aadhaar.url}")
	private String aptsSaopUrl;
	
	@Autowired
	private AadhaarTransactionDAO aadhaarTransactionDAO;
	
	private  BSNLeKYCWebservices bsnLeKYCWebservices;
	private  BSNLeKYCWebservicesPortType bsnLeKYCWebservicesHttpsSoap11Endpoint;
	
	 static  {
		 
		  Security.addProvider(new BouncyCastleProvider());
	}
	
	public static String getDate(String format, String timeZone, Date date) {
		SimpleDateFormat sdf = new SimpleDateFormat(format);
		sdf.setTimeZone(TimeZone.getTimeZone(timeZone));
		return sdf.format(date);
	}
	
	private void setSaopUrl() {
		  WebServiceException e = null;
		try {
			bsnLeKYCWebservices=new BSNLeKYCWebservices(new URL(aptsSaopUrl));
			 bsnLeKYCWebservicesHttpsSoap11Endpoint = bsnLeKYCWebservices
					.getBSNLeKYCWebservicesHttpsSoap11Endpoint();
		} catch (MalformedURLException ex) {
			// TODO Auto-generated catch block
			 e = new WebServiceException(ex);
			BSNLeKYCWebservices.BSNLEKYCWEBSERVICES_EXCEPTION=e;
			logger.error("Error:"+ex.getStackTrace());
			throw new BadRequestException(ex.getMessage());
		}	
	}

	@Override
	public APIResponse<BiometricresponseBean> cosumeAptsAaadhaarResponse(
			AadhaarDetailsRequestVO model) {
		String aptsAaadhaarResponse = StringUtils.EMPTY;
		try {
			this.setSaopUrl();
			if (StringUtils.isEmpty(model.getRequestType())) {
				byte[] encPidBytes = Base64.getDecoder().decode(model.getEncryptedPid());
				byte[] encSessionbytes = Base64.getDecoder().decode(model.getEncSessionKey());
				byte[] encHmcBytes = Base64.getDecoder().decode(model.getEncHmac());
				aptsAaadhaarResponse = bsnLeKYCWebservicesHttpsSoap11Endpoint
						.getAadhaarDemographicDataBySRDHSecuredeKYC(model.getUid_num(), agencyName, agenyCode,
								encPidBytes, encSessionbytes, encHmcBytes, getCeri(), biometricEkycOption,
								rdServiceDataType, model.getUdc(), model.getRdsId(), model.getRdsVer(), model.getDpId(),
								model.getDc(), model.getMi(), model.getMc());

			} else if (AadhaarConstant.RequestType.OPT.getContent().equals(model.getRequestType())) {
//				BSNLeKYCWebservicesPortType bsnLeKYCWebservicesHttpsSoap11Endpoint = this.getSoapBuilder();
				aptsAaadhaarResponse = bsnLeKYCWebservicesHttpsSoap11Endpoint
						.otpGenerationBySRDHSecuredeKYC(model.getUid_num(), agencyName, agenyCode);
				com.ecentric.bean.otpgenration responseBean = (com.ecentric.bean.otpgenration) encode(
						aptsAaadhaarResponse);
				BiometricresponseBean biometricresponseBean = new com.ecentric.bean.BiometricresponseBean();
				BeanUtils.copyProperties(responseBean,biometricresponseBean);
				biometricresponseBean.setAuth_err_code(responseBean.getAuth_reason());
				this.saveAptsResponse(biometricresponseBean, model, bsnLeKYCWebservices.getWSDLDocumentLocation().toString());
				return new APIResponse<>(true, HttpStatus.OK, biometricresponseBean);

			} else if (AadhaarConstant.RequestType.EKYC.getContent().equals(model.getRequestType())) {
				ClassLoader classLoader = getClass().getClassLoader();
				String file = classLoader.getResource(otpCerFilePath).getFile();
				this.initPublicKey(file);
				String xmlPlain = this.getXMLPlain(model.getVercode());
				AptsRequiredPid createPIDBlock = this.createPIDBlock(xmlPlain);
				aptsAaadhaarResponse = bsnLeKYCWebservicesHttpsSoap11Endpoint.otpProcessingBySRDHSecuredeKYC(
						model.getUid_num(), agencyName, agenyCode, createPIDBlock.getEncryptedPid(),
						createPIDBlock.getEncryptedSessionKey(), createPIDBlock.getEncryptedHamc());
			} else {
				logger.error("No Request Type Found:" + model.getRequestType());
				return new APIResponse<>(HttpStatus.NOT_FOUND, "Invalid RequestType");
			}
		} catch (Exception exception) {
			logger.error("error occured while apts service consuming" + exception.getMessage());
			return new APIResponse<>(HttpStatus.NOT_FOUND, exception.getMessage());
		}
		try {
			com.ecentric.bean.BiometricresponseBean responseBean = (com.ecentric.bean.BiometricresponseBean) encode(
					aptsAaadhaarResponse);
			responseBean.setAuth_err_code(responseBean.getAuth_reason());
			this.saveAptsResponse(responseBean, model, bsnLeKYCWebservices.getWSDLDocumentLocation().toString());
			return new APIResponse<>(true, HttpStatus.OK, responseBean);
		} catch (IOException ioException) {
			logger.error("error occured while apts service unboxing" + ioException.getMessage());
			return new APIResponse<>(HttpStatus.INTERNAL_SERVER_ERROR, ioException.getMessage());
		} catch (Exception exception) {
			logger.error("error occured while apts service unboxing" + exception.getMessage());
			return new APIResponse<>(HttpStatus.NOT_FOUND, exception.getMessage());
		}

	}
	
	private void saveAptsResponse(com.ecentric.bean.BiometricresponseBean biometricresponseBean,
			AadhaarDetailsRequestVO model,String url) {
		AadhaarTransactionDTO aadhaarTransactionDTO = new AadhaarTransactionDTO();
		try {
			aadhaarTransactionDTO.setUuId(UUID.randomUUID());
			aadhaarTransactionDTO.setAptsAadhaarRequest(model);
			aadhaarTransactionDTO.setAptsAadhaarResponse(biometricresponseBean);
			aadhaarTransactionDTO.setCreatedDate(LocalDateTime.now());
			aadhaarTransactionDTO.setUrl(url);
			aadhaarTransactionDTO.setIsAptsRequest(Boolean.TRUE);
			aadhaarTransactionDAO.save(aadhaarTransactionDTO);
			biometricresponseBean.setUuId(aadhaarTransactionDTO.getUuId());

		} catch (Exception e) {
			logger.error("Save Failed : {} , Exception : {}, Cause : {}",
					aadhaarTransactionDTO.getAadhaarRestRequest().getId(), e.getMessage(), e.getCause().getMessage());
		}	
	}

	public Object encode(String data) throws IOException {
		ByteArrayInputStream bis = new ByteArrayInputStream(data.getBytes());
//		 BufferedInputStream bufferedInputStream = new BufferedInputStream(bis);
		XMLDecoder decoder = new XMLDecoder(bis);
		Object obj = decoder.readObject();
		decoder.close();
		bis.close();
		return obj;
	}


	private String getCeri() {
		try {
			KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			trustStore.load(null);// Make an empty store
			ClassLoader classLoader = getClass().getClassLoader();
			String file = classLoader.getResource(otpCerFilePath).getFile();
			InputStream fis = new FileInputStream(new File(URLDecoder.decode(file, "UTF-8")));
			BufferedInputStream bis = new BufferedInputStream(fis);

			CertificateFactory cf = CertificateFactory.getInstance("X.509");

			Certificate cert = cf.generateCertificate(bis);
			X509Certificate x509Cert = ((X509Certificate) cert);
			String date = getDate(DATE_PATTERN_4, "IST", x509Cert.getNotAfter());
			logger.info("certificate expiry date"+date);
			return date;
		} catch (Exception e) {
			logger.info("error whlie opening certificate"+e.getMessage());
		}
		return null;

	}

	public String time() {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		return timestamp = dateFormat.format(new Date());
	}

	private String getIV(String timeIv) {
//			return timestamp.substring(timestamp.length()-12, timestamp.length());
		return timeIv.substring(timeIv.length() - 12, timeIv.length());
	}

	private String getAAD(String aadTime) {
//			return timestamp.substring(timestamp.length()-16, timestamp.length());
		return aadTime.substring(aadTime.length() - 16, aadTime.length());
	}

	public AptsRequiredPid createPIDBlock(String pidXml) throws Exception {
		byte[] sessionKey = generateSessionKey();
		String ivTime = this.time();
		byte[] iv = getIV(ivTime).getBytes();
		byte[] aad = getAAD(ivTime).getBytes();
		byte[] hmacBytes = generateSha256Hash(pidXml.getBytes());
		AptsRequiredPid aptsRequiredPid = new AptsRequiredPid();
		aptsRequiredPid.setEncryptedHamc(encryptUsingSessionKey(sessionKey, hmacBytes, iv, aad));
//				hmacBytes=encryptUsingSessionKey(sessionKey, hmacBytes, iv, aad);
		logger.info("Successfully Encrypted HMAC with Skey");
//				hmac=new String(Base64.getEncoder().encode(hmacBytes));

//				echmacBytes=hmacBytes;
		byte[] pidBytes = encryptUsingSessionKey(sessionKey, pidXml.getBytes(), iv, aad);
		pidBytes = ISOUtil.concat(timestamp.getBytes(), pidBytes);
		logger.info("Successfully Encrypted PID with Skey");
//				encPidBytes=pidBytes;
		aptsRequiredPid.setEncryptedPid(pidBytes);
//				pid=new String(Base64.getEncoder().encode(pidBytes));
//				initPublicKey(verifyOtpCerFilePath);
		byte[] encryptedSKey = encryptUsingPublicKey(sessionKey);
		encSkeyBytes = encryptedSKey;
		aptsRequiredPid.setEncryptedSessionKey(encSkeyBytes);
		skey = new String(Base64.getEncoder().encode(encryptedSKey));
		return aptsRequiredPid;
	}

	private byte[] generateSha256Hash(byte[] message) throws Exception {
		byte[] hash = null;
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.reset();
		hash = digest.digest(message);
		logger.info("Successfully Generated HMAC");
		return hash;
	}

	private byte[] encryptUsingSessionKey(byte[] skey, byte[] data, byte[] iv, byte[] aad) throws Exception {
		AEADParameters parameters = new AEADParameters(new KeyParameter(skey), 128, iv, aad);
		GCMBlockCipher gcmEngine = new GCMBlockCipher(new AESFastEngine());
		gcmEngine.init(true, parameters);
		byte[] ct = new byte[gcmEngine.getOutputSize(data.length)];
		int len = gcmEngine.processBytes(data, 0, data.length, ct, 0);
		gcmEngine.doFinal(ct, len);
		return ct;
	}

	public void initPublicKey(String fileNameLoc) throws Exception {
		FileInputStream fileInputStream = null;
		try {
			String fileName = fileNameLoc;
			System.out.println("File name:" + fileName);
			CertificateFactory certFactory = CertificateFactory.getInstance(CERTIFICATE_TYPE);
			fileInputStream = new FileInputStream(new File(fileName));
			X509Certificate cert = (X509Certificate) certFactory.generateCertificate(fileInputStream);
			publicKey = cert.getPublicKey();
			certIdentifier = extractCertIdentifier(cert.getNotAfter());
			logger.info("Successfully Initialized Public Key");
		} finally {
			if (fileInputStream != null) {
				fileInputStream.close();
			}
		}
	}

	private String extractCertIdentifier(Date date) {
		SimpleDateFormat ciDateFormat = new SimpleDateFormat("yyyyMMdd");
		ciDateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
		String certificateIdentifier = ciDateFormat.format(date);
		return certificateIdentifier;
	}

	private String getXMLPlain(String otpValue) {
		String version = "2.0";
//				String plainText= "<Pid ts=\""+timestamp+"\" ver=\""+version+"\" wadh=\""+this.wadh+"\" xmlns=\"http://www.uidai.gov.in/authentication/uid-auth-request/1.0\"><Pv otp=\""+this.otp+"\"/></Pid>";
		String plainText = "<Pid ts=\"" + timestamp + "\" ver=\"" + version
				+ "\" xmlns=\"http://www.uidai.gov.in/authentication/uid-auth-request/1.0\"><Pv otp=\"" + otpValue
				+ "\"/></Pid>";
		return plainText;
	}

	public byte[] encryptUsingPublicKey(byte[] data) throws IOException, GeneralSecurityException {
		// encrypt the session key with the public key
		Cipher pkCipher = Cipher.getInstance(ASYMMETRIC_ALGO, JCE_PROVIDER);
		pkCipher.init(Cipher.ENCRYPT_MODE, publicKey);
		byte[] encSessionKey = pkCipher.doFinal(data);
		return encSessionKey;
	}

	public void Encrypter(String publicKeyFileName) {
		FileInputStream fileInputStream = null;
		try {
			CertificateFactory certFactory = CertificateFactory.getInstance(CERTIFICATE_TYPE, JCE_PROVIDER);
			fileInputStream = new FileInputStream(new File(publicKeyFileName));
			X509Certificate cert = (X509Certificate) certFactory.generateCertificate(fileInputStream);
			publicKey = cert.getPublicKey();
			certExpiryDate = cert.getNotAfter();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Could not intialize encryption module", e);
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private byte[] generateSessionKey() throws Exception {
		KeyGenerator kgen = KeyGenerator.getInstance("AES", JCE_PROVIDER);
		kgen.init(SYMMETRIC_KEY_SIZE);
		SecretKey key = kgen.generateKey();
		byte[] symmKey = key.getEncoded();
		logger.info("Successfully Session Key Generated");
		return symmKey;
	}

}