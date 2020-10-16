package org.epragati.aadhaarAPI.util;

public enum AadhaarProvider {

    ECENTRIC(1, "ecentric"), APTONLINE(2, "APTOnline");

    private Integer id;
    private String label;

    private AadhaarProvider() {}

    private AadhaarProvider(Integer id, String label) {
        this.id = id;
        this.label = label;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
