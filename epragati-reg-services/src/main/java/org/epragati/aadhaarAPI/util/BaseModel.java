package org.epragati.aadhaarAPI.util;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
/**
 * BaseModel for all data models.
 * @author naga.pulaparthi
 *
 */
public class BaseModel implements Serializable {

    private static final long serialVersionUID = -1982911421322404738L;

    @JsonIgnore
    private String createdBy;
    @JsonIgnore
    private Long createdOn;
    @JsonIgnore
    private String modifiedBy;
    @JsonIgnore
    private Long modifiedOn;


    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Long getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(Long createdOn) {
        this.createdOn = createdOn;
    }

    public String getModifiedBy() {
        return modifiedBy;
    }

    public void setModifiedBy(String modifiedBy) {
        this.modifiedBy = modifiedBy;
    }

    public Long getModifiedOn() {
        return modifiedOn;
    }

    public void setModifiedOn(Long modifiedOn) {
        this.modifiedOn = modifiedOn;
    }

    @Override
    public String toString() {
        return "BaseModel [createdBy=" + createdBy + ", createdOn=" + createdOn + ", modifiedBy=" + modifiedBy
                + ", modifiedOn=" + modifiedOn + "]";
    }


}
