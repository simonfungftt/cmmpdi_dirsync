package com.ctg.innovic.cmmpdi.dirsync.dto;

import java.util.ArrayList;
import java.util.List;

public class CMMPExchangeLdapGroup {

    public CMMPExchangeLdapGroup() {
        this.memberDNs = new ArrayList<>();
    }

    private String dn;
    private String cn;
    private String description;
    private String mail;
    private String displayName;
    private String distinguishedName;

    private List<String> proxyAddresses = new ArrayList<>();

    private Integer extensionAttribute1;
    private Integer extensionAttribute3;
    private Integer extensionAttribute5;
    private Integer extensionAttribute6;
    private Integer extensionAttribute7;
    private Integer extensionAttribute10;

    private List<String> memberDNs;

    public String getMail() {
        return mail;
    }

    public void setMail(String email) {
        this.mail = email;
    }

    public List<String> getMemberDNs() {
        return memberDNs;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getProxyAddresses() {
        return proxyAddresses;
    }

    public String getCn() {
        return cn;
    }

    public void setDn(String dn) {
        this.dn = dn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setExtensionAttribute1(Integer extensionAttribute1) {
        this.extensionAttribute1 = extensionAttribute1;
    }

    public void setExtensionAttribute3(Integer extensionAttribute3) {
        this.extensionAttribute3 = extensionAttribute3;
    }

    public void setExtensionAttribute5(Integer extensionAttribute5) {
        this.extensionAttribute5 = extensionAttribute5;
    }

    public void setExtensionAttribute6(Integer extensionAttribute6) {
        this.extensionAttribute6 = extensionAttribute6;
    }

    public void setExtensionAttribute7(Integer extensionAttribute7) {
        this.extensionAttribute7 = extensionAttribute7;
    }

    public void setExtensionAttribute10(Integer extensionAttribute10) {
        this.extensionAttribute10 = extensionAttribute10;
    }

    public String getDn() {
        return dn;
    }

    public String getDistinguishedName() {
        return distinguishedName;
    }
}
