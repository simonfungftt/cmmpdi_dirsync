package com.ctg.innovic.cmmpdi.dirsync.dto;

import lombok.Data;

@Data
public class CMMPExchangeLdapUser {

    private String dn;
    private String cn;
    private String sn;
    private String givenName;
    private String mail;
    private String distinguishedName;
    private String displayName;
    private String proxyAddresses;
    private String title;
    private int countryCode;
    private byte[] userCert;
    private byte[] userSMIMECertificate;
    private byte[] userCertificate;
    private int extensionAttribute1;
    private int extensionAttribute4;
    private int extensionAttribute3;
    private int extensionAttribute7;
    private int msExchAddressBookFlags;

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getDn() {
        return dn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public String getDisplayName() {
        return displayName;
    }


    public void setDn(String dn) {
        this.dn = dn;
    }


    public String getDistinguishedName() {
        return distinguishedName;
    }

    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
    }

    public void setUserCertificate(byte[] userCertificate) {
        this.userCertificate = userCertificate;
    }

    public void setUserSMIMECertificate(byte[] userSMIMECertificate) {
        this.userSMIMECertificate = userSMIMECertificate;
    }

    public int getExtensionAttribute1() {
        return extensionAttribute1;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setCountryCode(int countryCode) {
        this.countryCode = countryCode;
    }

    public String getCn() {
        return cn;
    }
}
