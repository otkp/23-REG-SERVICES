
package org.epragati.apts.aadhaar;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="uid_num" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="agency_name" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="agency_code" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="encryptedPid" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="encSessionKey" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="encHmac" type="{http://www.w3.org/2001/XMLSchema}base64Binary" minOccurs="0"/>
 *         &lt;element name="certificateIdentifier" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="eKYCOption" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dataType" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="udc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rdsId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="rdsVer" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dpId" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="dc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mi" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="mc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "uidNum",
    "agencyName",
    "agencyCode",
    "encryptedPid",
    "encSessionKey",
    "encHmac",
    "certificateIdentifier",
    "ekycOption",
    "dataType",
    "udc",
    "rdsId",
    "rdsVer",
    "dpId",
    "dc",
    "mi",
    "mc"
})
@XmlRootElement(name = "getAadhaarDemographicDataBySRDHSecuredeKYC")
public class GetAadhaarDemographicDataBySRDHSecuredeKYC {

    @XmlElementRef(name = "uid_num", namespace = "http://services/ecentric/com/xsd", type = JAXBElement.class, required = false)
    protected JAXBElement<String> uidNum;
    @XmlElementRef(name = "agency_name", namespace = "http://services/ecentric/com/xsd", type = JAXBElement.class, required = false)
    protected JAXBElement<String> agencyName;
    @XmlElementRef(name = "agency_code", namespace = "http://services/ecentric/com/xsd", type = JAXBElement.class, required = false)
    protected JAXBElement<String> agencyCode;
    @XmlElementRef(name = "encryptedPid", namespace = "http://services/ecentric/com/xsd", type = JAXBElement.class, required = false)
    protected JAXBElement<byte[]> encryptedPid;
    @XmlElementRef(name = "encSessionKey", namespace = "http://services/ecentric/com/xsd", type = JAXBElement.class, required = false)
    protected JAXBElement<byte[]> encSessionKey;
    @XmlElementRef(name = "encHmac", namespace = "http://services/ecentric/com/xsd", type = JAXBElement.class, required = false)
    protected JAXBElement<byte[]> encHmac;
    @XmlElementRef(name = "certificateIdentifier", namespace = "http://services/ecentric/com/xsd", type = JAXBElement.class, required = false)
    protected JAXBElement<String> certificateIdentifier;
    @XmlElementRef(name = "eKYCOption", namespace = "http://services/ecentric/com/xsd", type = JAXBElement.class, required = false)
    protected JAXBElement<String> ekycOption;
    @XmlElementRef(name = "dataType", namespace = "http://services/ecentric/com/xsd", type = JAXBElement.class, required = false)
    protected JAXBElement<String> dataType;
    @XmlElementRef(name = "udc", namespace = "http://services/ecentric/com/xsd", type = JAXBElement.class, required = false)
    protected JAXBElement<String> udc;
    @XmlElementRef(name = "rdsId", namespace = "http://services/ecentric/com/xsd", type = JAXBElement.class, required = false)
    protected JAXBElement<String> rdsId;
    @XmlElementRef(name = "rdsVer", namespace = "http://services/ecentric/com/xsd", type = JAXBElement.class, required = false)
    protected JAXBElement<String> rdsVer;
    @XmlElementRef(name = "dpId", namespace = "http://services/ecentric/com/xsd", type = JAXBElement.class, required = false)
    protected JAXBElement<String> dpId;
    @XmlElementRef(name = "dc", namespace = "http://services/ecentric/com/xsd", type = JAXBElement.class, required = false)
    protected JAXBElement<String> dc;
    @XmlElementRef(name = "mi", namespace = "http://services/ecentric/com/xsd", type = JAXBElement.class, required = false)
    protected JAXBElement<String> mi;
    @XmlElementRef(name = "mc", namespace = "http://services/ecentric/com/xsd", type = JAXBElement.class, required = false)
    protected JAXBElement<String> mc;

    /**
     * Gets the value of the uidNum property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUidNum() {
        return uidNum;
    }

    /**
     * Sets the value of the uidNum property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUidNum(JAXBElement<String> value) {
        this.uidNum = value;
    }

    /**
     * Gets the value of the agencyName property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getAgencyName() {
        return agencyName;
    }

    /**
     * Sets the value of the agencyName property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setAgencyName(JAXBElement<String> value) {
        this.agencyName = value;
    }

    /**
     * Gets the value of the agencyCode property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getAgencyCode() {
        return agencyCode;
    }

    /**
     * Sets the value of the agencyCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setAgencyCode(JAXBElement<String> value) {
        this.agencyCode = value;
    }

    /**
     * Gets the value of the encryptedPid property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link byte[]}{@code >}
     *     
     */
    public JAXBElement<byte[]> getEncryptedPid() {
        return encryptedPid;
    }

    /**
     * Sets the value of the encryptedPid property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link byte[]}{@code >}
     *     
     */
    public void setEncryptedPid(JAXBElement<byte[]> value) {
        this.encryptedPid = value;
    }

    /**
     * Gets the value of the encSessionKey property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link byte[]}{@code >}
     *     
     */
    public JAXBElement<byte[]> getEncSessionKey() {
        return encSessionKey;
    }

    /**
     * Sets the value of the encSessionKey property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link byte[]}{@code >}
     *     
     */
    public void setEncSessionKey(JAXBElement<byte[]> value) {
        this.encSessionKey = value;
    }

    /**
     * Gets the value of the encHmac property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link byte[]}{@code >}
     *     
     */
    public JAXBElement<byte[]> getEncHmac() {
        return encHmac;
    }

    /**
     * Sets the value of the encHmac property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link byte[]}{@code >}
     *     
     */
    public void setEncHmac(JAXBElement<byte[]> value) {
        this.encHmac = value;
    }

    /**
     * Gets the value of the certificateIdentifier property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getCertificateIdentifier() {
        return certificateIdentifier;
    }

    /**
     * Sets the value of the certificateIdentifier property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setCertificateIdentifier(JAXBElement<String> value) {
        this.certificateIdentifier = value;
    }

    /**
     * Gets the value of the ekycOption property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getEKYCOption() {
        return ekycOption;
    }

    /**
     * Sets the value of the ekycOption property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setEKYCOption(JAXBElement<String> value) {
        this.ekycOption = value;
    }

    /**
     * Gets the value of the dataType property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getDataType() {
        return dataType;
    }

    /**
     * Sets the value of the dataType property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setDataType(JAXBElement<String> value) {
        this.dataType = value;
    }

    /**
     * Gets the value of the udc property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getUdc() {
        return udc;
    }

    /**
     * Sets the value of the udc property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setUdc(JAXBElement<String> value) {
        this.udc = value;
    }

    /**
     * Gets the value of the rdsId property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getRdsId() {
        return rdsId;
    }

    /**
     * Sets the value of the rdsId property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setRdsId(JAXBElement<String> value) {
        this.rdsId = value;
    }

    /**
     * Gets the value of the rdsVer property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getRdsVer() {
        return rdsVer;
    }

    /**
     * Sets the value of the rdsVer property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setRdsVer(JAXBElement<String> value) {
        this.rdsVer = value;
    }

    /**
     * Gets the value of the dpId property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getDpId() {
        return dpId;
    }

    /**
     * Sets the value of the dpId property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setDpId(JAXBElement<String> value) {
        this.dpId = value;
    }

    /**
     * Gets the value of the dc property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getDc() {
        return dc;
    }

    /**
     * Sets the value of the dc property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setDc(JAXBElement<String> value) {
        this.dc = value;
    }

    /**
     * Gets the value of the mi property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getMi() {
        return mi;
    }

    /**
     * Sets the value of the mi property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setMi(JAXBElement<String> value) {
        this.mi = value;
    }

    /**
     * Gets the value of the mc property.
     * 
     * @return
     *     possible object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public JAXBElement<String> getMc() {
        return mc;
    }

    /**
     * Sets the value of the mc property.
     * 
     * @param value
     *     allowed object is
     *     {@link JAXBElement }{@code <}{@link String }{@code >}
     *     
     */
    public void setMc(JAXBElement<String> value) {
        this.mc = value;
    }

}
