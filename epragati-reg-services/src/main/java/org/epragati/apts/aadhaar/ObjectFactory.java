
package org.epragati.apts.aadhaar;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the services.ecentric.com.xsd package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _GetAadhaarDemographicDataBySRDHSecuredeKYCEncryptedPid_QNAME = new QName("http://services/ecentric/com/xsd", "encryptedPid");
    private final static QName _GetAadhaarDemographicDataBySRDHSecuredeKYCEncSessionKey_QNAME = new QName("http://services/ecentric/com/xsd", "encSessionKey");
    private final static QName _GetAadhaarDemographicDataBySRDHSecuredeKYCUdc_QNAME = new QName("http://services/ecentric/com/xsd", "udc");
    private final static QName _GetAadhaarDemographicDataBySRDHSecuredeKYCMc_QNAME = new QName("http://services/ecentric/com/xsd", "mc");
    private final static QName _GetAadhaarDemographicDataBySRDHSecuredeKYCEKYCOption_QNAME = new QName("http://services/ecentric/com/xsd", "eKYCOption");
    private final static QName _GetAadhaarDemographicDataBySRDHSecuredeKYCMi_QNAME = new QName("http://services/ecentric/com/xsd", "mi");
    private final static QName _GetAadhaarDemographicDataBySRDHSecuredeKYCRdsVer_QNAME = new QName("http://services/ecentric/com/xsd", "rdsVer");
    private final static QName _GetAadhaarDemographicDataBySRDHSecuredeKYCEncHmac_QNAME = new QName("http://services/ecentric/com/xsd", "encHmac");
    private final static QName _GetAadhaarDemographicDataBySRDHSecuredeKYCDc_QNAME = new QName("http://services/ecentric/com/xsd", "dc");
    private final static QName _GetAadhaarDemographicDataBySRDHSecuredeKYCCertificateIdentifier_QNAME = new QName("http://services/ecentric/com/xsd", "certificateIdentifier");
    private final static QName _GetAadhaarDemographicDataBySRDHSecuredeKYCAgencyName_QNAME = new QName("http://services/ecentric/com/xsd", "agency_name");
    private final static QName _GetAadhaarDemographicDataBySRDHSecuredeKYCUidNum_QNAME = new QName("http://services/ecentric/com/xsd", "uid_num");
    private final static QName _GetAadhaarDemographicDataBySRDHSecuredeKYCAgencyCode_QNAME = new QName("http://services/ecentric/com/xsd", "agency_code");
    private final static QName _GetAadhaarDemographicDataBySRDHSecuredeKYCDataType_QNAME = new QName("http://services/ecentric/com/xsd", "dataType");
    private final static QName _GetAadhaarDemographicDataBySRDHSecuredeKYCDpId_QNAME = new QName("http://services/ecentric/com/xsd", "dpId");
    private final static QName _GetAadhaarDemographicDataBySRDHSecuredeKYCRdsId_QNAME = new QName("http://services/ecentric/com/xsd", "rdsId");
    private final static QName _GetAadhaarDemographicDataBySRDHSecuredeKYCResponseReturn_QNAME = new QName("http://services/ecentric/com/xsd", "return");
    private final static QName _OTPGenerationBySRDHSecuredeKYCUidNum_QNAME = new QName("http://services/ecentric/com/xsd", "uidNum");
    private final static QName _OTPGenerationBySRDHSecuredeKYCAgencyCode_QNAME = new QName("http://services/ecentric/com/xsd", "agencyCode");
    private final static QName _OTPGenerationBySRDHSecuredeKYCAgencyName_QNAME = new QName("http://services/ecentric/com/xsd", "agencyName");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: services.ecentric.com.xsd
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link GetAadhaarDemographicDataBySRDHSecuredeKYC }
     * 
     */
    public GetAadhaarDemographicDataBySRDHSecuredeKYC createGetAadhaarDemographicDataBySRDHSecuredeKYC() {
        return new GetAadhaarDemographicDataBySRDHSecuredeKYC();
    }

    /**
     * Create an instance of {@link OTPGenerationBySRDHSecuredeKYCResponse }
     * 
     */
    public OTPGenerationBySRDHSecuredeKYCResponse createOTPGenerationBySRDHSecuredeKYCResponse() {
        return new OTPGenerationBySRDHSecuredeKYCResponse();
    }

    /**
     * Create an instance of {@link OTPProcessingBySRDHSecuredeKYC }
     * 
     */
    public OTPProcessingBySRDHSecuredeKYC createOTPProcessingBySRDHSecuredeKYC() {
        return new OTPProcessingBySRDHSecuredeKYC();
    }

    /**
     * Create an instance of {@link OTPGenerationBySRDHSecuredeKYC }
     * 
     */
    public OTPGenerationBySRDHSecuredeKYC createOTPGenerationBySRDHSecuredeKYC() {
        return new OTPGenerationBySRDHSecuredeKYC();
    }

    /**
     * Create an instance of {@link GetAadhaarDemographicDataBySRDHSecuredeKYCResponse }
     * 
     */
    public GetAadhaarDemographicDataBySRDHSecuredeKYCResponse createGetAadhaarDemographicDataBySRDHSecuredeKYCResponse() {
        return new GetAadhaarDemographicDataBySRDHSecuredeKYCResponse();
    }

    /**
     * Create an instance of {@link OTPProcessingBySRDHSecuredeKYCResponse }
     * 
     */
    public OTPProcessingBySRDHSecuredeKYCResponse createOTPProcessingBySRDHSecuredeKYCResponse() {
        return new OTPProcessingBySRDHSecuredeKYCResponse();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "encryptedPid", scope = GetAadhaarDemographicDataBySRDHSecuredeKYC.class)
    public JAXBElement<byte[]> createGetAadhaarDemographicDataBySRDHSecuredeKYCEncryptedPid(byte[] value) {
        return new JAXBElement<byte[]>(_GetAadhaarDemographicDataBySRDHSecuredeKYCEncryptedPid_QNAME, byte[].class, GetAadhaarDemographicDataBySRDHSecuredeKYC.class, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "encSessionKey", scope = GetAadhaarDemographicDataBySRDHSecuredeKYC.class)
    public JAXBElement<byte[]> createGetAadhaarDemographicDataBySRDHSecuredeKYCEncSessionKey(byte[] value) {
        return new JAXBElement<byte[]>(_GetAadhaarDemographicDataBySRDHSecuredeKYCEncSessionKey_QNAME, byte[].class, GetAadhaarDemographicDataBySRDHSecuredeKYC.class, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "udc", scope = GetAadhaarDemographicDataBySRDHSecuredeKYC.class)
    public JAXBElement<String> createGetAadhaarDemographicDataBySRDHSecuredeKYCUdc(String value) {
        return new JAXBElement<String>(_GetAadhaarDemographicDataBySRDHSecuredeKYCUdc_QNAME, String.class, GetAadhaarDemographicDataBySRDHSecuredeKYC.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "mc", scope = GetAadhaarDemographicDataBySRDHSecuredeKYC.class)
    public JAXBElement<String> createGetAadhaarDemographicDataBySRDHSecuredeKYCMc(String value) {
        return new JAXBElement<String>(_GetAadhaarDemographicDataBySRDHSecuredeKYCMc_QNAME, String.class, GetAadhaarDemographicDataBySRDHSecuredeKYC.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "eKYCOption", scope = GetAadhaarDemographicDataBySRDHSecuredeKYC.class)
    public JAXBElement<String> createGetAadhaarDemographicDataBySRDHSecuredeKYCEKYCOption(String value) {
        return new JAXBElement<String>(_GetAadhaarDemographicDataBySRDHSecuredeKYCEKYCOption_QNAME, String.class, GetAadhaarDemographicDataBySRDHSecuredeKYC.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "mi", scope = GetAadhaarDemographicDataBySRDHSecuredeKYC.class)
    public JAXBElement<String> createGetAadhaarDemographicDataBySRDHSecuredeKYCMi(String value) {
        return new JAXBElement<String>(_GetAadhaarDemographicDataBySRDHSecuredeKYCMi_QNAME, String.class, GetAadhaarDemographicDataBySRDHSecuredeKYC.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "rdsVer", scope = GetAadhaarDemographicDataBySRDHSecuredeKYC.class)
    public JAXBElement<String> createGetAadhaarDemographicDataBySRDHSecuredeKYCRdsVer(String value) {
        return new JAXBElement<String>(_GetAadhaarDemographicDataBySRDHSecuredeKYCRdsVer_QNAME, String.class, GetAadhaarDemographicDataBySRDHSecuredeKYC.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "encHmac", scope = GetAadhaarDemographicDataBySRDHSecuredeKYC.class)
    public JAXBElement<byte[]> createGetAadhaarDemographicDataBySRDHSecuredeKYCEncHmac(byte[] value) {
        return new JAXBElement<byte[]>(_GetAadhaarDemographicDataBySRDHSecuredeKYCEncHmac_QNAME, byte[].class, GetAadhaarDemographicDataBySRDHSecuredeKYC.class, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "dc", scope = GetAadhaarDemographicDataBySRDHSecuredeKYC.class)
    public JAXBElement<String> createGetAadhaarDemographicDataBySRDHSecuredeKYCDc(String value) {
        return new JAXBElement<String>(_GetAadhaarDemographicDataBySRDHSecuredeKYCDc_QNAME, String.class, GetAadhaarDemographicDataBySRDHSecuredeKYC.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "certificateIdentifier", scope = GetAadhaarDemographicDataBySRDHSecuredeKYC.class)
    public JAXBElement<String> createGetAadhaarDemographicDataBySRDHSecuredeKYCCertificateIdentifier(String value) {
        return new JAXBElement<String>(_GetAadhaarDemographicDataBySRDHSecuredeKYCCertificateIdentifier_QNAME, String.class, GetAadhaarDemographicDataBySRDHSecuredeKYC.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "agency_name", scope = GetAadhaarDemographicDataBySRDHSecuredeKYC.class)
    public JAXBElement<String> createGetAadhaarDemographicDataBySRDHSecuredeKYCAgencyName(String value) {
        return new JAXBElement<String>(_GetAadhaarDemographicDataBySRDHSecuredeKYCAgencyName_QNAME, String.class, GetAadhaarDemographicDataBySRDHSecuredeKYC.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "uid_num", scope = GetAadhaarDemographicDataBySRDHSecuredeKYC.class)
    public JAXBElement<String> createGetAadhaarDemographicDataBySRDHSecuredeKYCUidNum(String value) {
        return new JAXBElement<String>(_GetAadhaarDemographicDataBySRDHSecuredeKYCUidNum_QNAME, String.class, GetAadhaarDemographicDataBySRDHSecuredeKYC.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "agency_code", scope = GetAadhaarDemographicDataBySRDHSecuredeKYC.class)
    public JAXBElement<String> createGetAadhaarDemographicDataBySRDHSecuredeKYCAgencyCode(String value) {
        return new JAXBElement<String>(_GetAadhaarDemographicDataBySRDHSecuredeKYCAgencyCode_QNAME, String.class, GetAadhaarDemographicDataBySRDHSecuredeKYC.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "dataType", scope = GetAadhaarDemographicDataBySRDHSecuredeKYC.class)
    public JAXBElement<String> createGetAadhaarDemographicDataBySRDHSecuredeKYCDataType(String value) {
        return new JAXBElement<String>(_GetAadhaarDemographicDataBySRDHSecuredeKYCDataType_QNAME, String.class, GetAadhaarDemographicDataBySRDHSecuredeKYC.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "dpId", scope = GetAadhaarDemographicDataBySRDHSecuredeKYC.class)
    public JAXBElement<String> createGetAadhaarDemographicDataBySRDHSecuredeKYCDpId(String value) {
        return new JAXBElement<String>(_GetAadhaarDemographicDataBySRDHSecuredeKYCDpId_QNAME, String.class, GetAadhaarDemographicDataBySRDHSecuredeKYC.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "rdsId", scope = GetAadhaarDemographicDataBySRDHSecuredeKYC.class)
    public JAXBElement<String> createGetAadhaarDemographicDataBySRDHSecuredeKYCRdsId(String value) {
        return new JAXBElement<String>(_GetAadhaarDemographicDataBySRDHSecuredeKYCRdsId_QNAME, String.class, GetAadhaarDemographicDataBySRDHSecuredeKYC.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "return", scope = GetAadhaarDemographicDataBySRDHSecuredeKYCResponse.class)
    public JAXBElement<String> createGetAadhaarDemographicDataBySRDHSecuredeKYCResponseReturn(String value) {
        return new JAXBElement<String>(_GetAadhaarDemographicDataBySRDHSecuredeKYCResponseReturn_QNAME, String.class, GetAadhaarDemographicDataBySRDHSecuredeKYCResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "return", scope = OTPGenerationBySRDHSecuredeKYCResponse.class)
    public JAXBElement<String> createOTPGenerationBySRDHSecuredeKYCResponseReturn(String value) {
        return new JAXBElement<String>(_GetAadhaarDemographicDataBySRDHSecuredeKYCResponseReturn_QNAME, String.class, OTPGenerationBySRDHSecuredeKYCResponse.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "agency_name", scope = OTPProcessingBySRDHSecuredeKYC.class)
    public JAXBElement<String> createOTPProcessingBySRDHSecuredeKYCAgencyName(String value) {
        return new JAXBElement<String>(_GetAadhaarDemographicDataBySRDHSecuredeKYCAgencyName_QNAME, String.class, OTPProcessingBySRDHSecuredeKYC.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "uid_num", scope = OTPProcessingBySRDHSecuredeKYC.class)
    public JAXBElement<String> createOTPProcessingBySRDHSecuredeKYCUidNum(String value) {
        return new JAXBElement<String>(_GetAadhaarDemographicDataBySRDHSecuredeKYCUidNum_QNAME, String.class, OTPProcessingBySRDHSecuredeKYC.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "encryptedPid", scope = OTPProcessingBySRDHSecuredeKYC.class)
    public JAXBElement<byte[]> createOTPProcessingBySRDHSecuredeKYCEncryptedPid(byte[] value) {
        return new JAXBElement<byte[]>(_GetAadhaarDemographicDataBySRDHSecuredeKYCEncryptedPid_QNAME, byte[].class, OTPProcessingBySRDHSecuredeKYC.class, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "encSessionKey", scope = OTPProcessingBySRDHSecuredeKYC.class)
    public JAXBElement<byte[]> createOTPProcessingBySRDHSecuredeKYCEncSessionKey(byte[] value) {
        return new JAXBElement<byte[]>(_GetAadhaarDemographicDataBySRDHSecuredeKYCEncSessionKey_QNAME, byte[].class, OTPProcessingBySRDHSecuredeKYC.class, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "agency_code", scope = OTPProcessingBySRDHSecuredeKYC.class)
    public JAXBElement<String> createOTPProcessingBySRDHSecuredeKYCAgencyCode(String value) {
        return new JAXBElement<String>(_GetAadhaarDemographicDataBySRDHSecuredeKYCAgencyCode_QNAME, String.class, OTPProcessingBySRDHSecuredeKYC.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link byte[]}{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "encHmac", scope = OTPProcessingBySRDHSecuredeKYC.class)
    public JAXBElement<byte[]> createOTPProcessingBySRDHSecuredeKYCEncHmac(byte[] value) {
        return new JAXBElement<byte[]>(_GetAadhaarDemographicDataBySRDHSecuredeKYCEncHmac_QNAME, byte[].class, OTPProcessingBySRDHSecuredeKYC.class, ((byte[]) value));
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "uidNum", scope = OTPGenerationBySRDHSecuredeKYC.class)
    public JAXBElement<String> createOTPGenerationBySRDHSecuredeKYCUidNum(String value) {
        return new JAXBElement<String>(_OTPGenerationBySRDHSecuredeKYCUidNum_QNAME, String.class, OTPGenerationBySRDHSecuredeKYC.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "agencyCode", scope = OTPGenerationBySRDHSecuredeKYC.class)
    public JAXBElement<String> createOTPGenerationBySRDHSecuredeKYCAgencyCode(String value) {
        return new JAXBElement<String>(_OTPGenerationBySRDHSecuredeKYCAgencyCode_QNAME, String.class, OTPGenerationBySRDHSecuredeKYC.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "agencyName", scope = OTPGenerationBySRDHSecuredeKYC.class)
    public JAXBElement<String> createOTPGenerationBySRDHSecuredeKYCAgencyName(String value) {
        return new JAXBElement<String>(_OTPGenerationBySRDHSecuredeKYCAgencyName_QNAME, String.class, OTPGenerationBySRDHSecuredeKYC.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://services/ecentric/com/xsd", name = "return", scope = OTPProcessingBySRDHSecuredeKYCResponse.class)
    public JAXBElement<String> createOTPProcessingBySRDHSecuredeKYCResponseReturn(String value) {
        return new JAXBElement<String>(_GetAadhaarDemographicDataBySRDHSecuredeKYCResponseReturn_QNAME, String.class, OTPProcessingBySRDHSecuredeKYCResponse.class, value);
    }

}
