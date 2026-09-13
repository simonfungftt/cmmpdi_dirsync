package com.ctg.innovic.cmmpdi.dirsync.dto;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Data
public class CMMPExchangeOrganizationalUnit {

    private String dn;
    private String ou;
    private byte[] objectGUID;
    private List<String> objectClasses;
    private List<String> ouPaths;

    // Getters and Setters
    public String getDn() { return dn; }
    public void setDn(String dn) { this.dn = dn; }

    public String getOu() { return ou; }
    public void setOu(String ou) { this.ou = ou; }

    public byte[] getObjectGUID() { return objectGUID; }
    public void setObjectGUID(byte[] objectGUID) { this.objectGUID = objectGUID; }

    public List<String> getObjectClasses() { return objectClasses; }
    public void setObjectClasses(List<String> objectClasses) { this.objectClasses = objectClasses; }

    public List<String> getOuPaths() { return ouPaths; }
    public void setOuPaths(List<String> ouPaths) { this.ouPaths = ouPaths; }
}
