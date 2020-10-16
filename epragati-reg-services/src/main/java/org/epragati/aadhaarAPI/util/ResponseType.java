package org.epragati.aadhaarAPI.util;

/**
 * ResponseType provides details for aadhar response success or error
 * 
 * @author naga.pulaparthi
 *
 */
public enum ResponseType {

    SUCCESS(1, "SUCCESS"),
    ERROR(2, "ERROR"),
	FAILD(3,"FAILD");

    private int value;
    private String label;

    private ResponseType(int value, String label) {
        this.value = value;
        this.label = label;
    }

    public int getValue() {
        return this.value;
    }

    public String getLabel() {
        return this.label;
    }

}
