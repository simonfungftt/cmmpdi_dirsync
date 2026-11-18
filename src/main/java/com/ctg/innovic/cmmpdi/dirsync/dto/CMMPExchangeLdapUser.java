package com.ctg.innovic.cmmpdi.dirsync.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.Name;
import javax.naming.ldap.LdapName;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

@Entry(objectClasses = {"top", "person", "organizationalPerson", "user"})
public class CMMPExchangeLdapUser {


    private static final DateTimeFormatter LDAP_GENERALIZED_TIME =
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyyMMddHHmmss")
                    .optionalStart()
                    .appendFraction(ChronoField.MILLI_OF_SECOND, 1, 3, true)
                    .optionalEnd()
                    .appendOffset("+HHmm", "Z")
                    .toFormatter();


    @Id
    @JsonIgnore
    private LdapName dn;

    @Attribute(name = "cn")
    private String cn;

    @Attribute(name = "sn")
    private String sn;

    @Attribute(name = "givenName")
    private String givenName;

    @Attribute(name = "mail")
    private String mail;

    @Attribute(name = "distinguishedName")
    private String distinguishedName;

    @Attribute(name = "displayName")
    private String displayName;

    @Attribute(name = "proxyAddresses")
    private String[] proxyAddresses;

    @Attribute(name = "title")
    private String title;

    @Attribute(name = "countryCode")
    private int countryCode;

    @Attribute(name = "userCert")
    private byte[] userCert;

    @Attribute(name = "userSMIMECertificate")
    private byte[] userSMIMECertificate;

    @Attribute(name = "userCertificate")
    private byte[] userCertificate;

    @Attribute(name = "extensionAttribute1")
    private int extensionAttribute1;

    @Attribute(name = "extensionAttribute4")
    private int extensionAttribute4;

    @Attribute(name = "extensionAttribute3")
    private int extensionAttribute3;

    @Attribute(name = "extensionAttribute7")
    private int extensionAttribute7;

    @Attribute(name = "description")
    private String description;

    @Attribute(name = "wWWHomePage")
    private int wWWHomePage;

    @Attribute(name = "msExchAddressBookFlags")
    private int msExchAddressBookFlags;

    @Attribute(name = "whenChanged", readonly = true)
    private String whenChanged;

    @Attribute(name = "whenCreated", readonly = true)
    private String whenCreated;



    public Instant getWhenCreatedInstant() {

        Instant _instant = null;

        try {
            _instant = parseLdapTimestamp(whenCreated);
        }
        catch(Exception e) {
            e.printStackTrace();
            _instant = Instant.now();
        }

        return _instant;
    }

    public Instant getWhenChangedInstant() {
        Instant _instant = null;

        try {
            _instant = parseLdapTimestamp(whenChanged);
        }
        catch(Exception e) {
            e.printStackTrace();
            _instant = Instant.now();
        }

        return _instant;
    }

    public String getWhenChanged() {
        return whenChanged;
    }

    public String getWhenCreated() {
        return whenCreated;
    }

    private static Instant parseLdapTimestamp(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        return ZonedDateTime.parse(raw, LDAP_GENERALIZED_TIME).toInstant();
    }


    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getDn() {
        return this.dn.toString();
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public String getDisplayName() {
        return displayName;
    }


//    public void setDn(String dn) {
//        this.dn = dn;
//    }

//    public Name getDn() {
//        return dn;
//    }

    public void setDnString(String dnString) {
        this.dn = (dnString != null && !dnString.isEmpty())
                ? LdapUtils.newLdapName(dnString)
                : null;
    }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public int getCountryCode() {
        return countryCode;
    }

    public int getExtensionAttribute4() {
        return extensionAttribute4;
    }

    public int getMsExchAddressBookFlags() {
        return msExchAddressBookFlags;
    }

    public String[] getProxyAddresses() {
        return proxyAddresses;
    }

    public int getwWWHomePage() {
        return wWWHomePage;
    }

    public byte[] getUserSMIMECertificate() {
        return userSMIMECertificate;
    }

    public void setProxyAddresses(String[] proxyAddresses) {
        this.proxyAddresses = proxyAddresses;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setUserCert(byte[] userCert) {
        this.userCert = userCert;
    }

    public void setExtensionAttribute1(int extensionAttribute1) {
        this.extensionAttribute1 = extensionAttribute1;
    }

    public void setExtensionAttribute3(int extensionAttribute3) {
        this.extensionAttribute3 = extensionAttribute3;
    }

    public void setExtensionAttribute7(int extensionAttribute7) {
        this.extensionAttribute7 = extensionAttribute7;
    }

    public void setExtensionAttribute4(int extensionAttribute4) {
        this.extensionAttribute4 = extensionAttribute4;
    }

    public void setMsExchAddressBookFlags(int msExchAddressBookFlags) {
        this.msExchAddressBookFlags = msExchAddressBookFlags;
    }

    public void setwWWHomePage(int wWWHomePage) {
        this.wWWHomePage = wWWHomePage;
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

    public String getSn() {
        return sn;
    }

    public String getGivenName() {
        return givenName;
    }

    public byte[] getUserCert() {
        return userCert;
    }

    public byte[] getUserCertificate() {
        return userCertificate;
    }

    public int getExtensionAttribute7() {
        return extensionAttribute7;
    }

    public int getExtensionAttribute3() {
        return extensionAttribute3;
    }

    public void setWhenChanged(String whenChanged) {
        this.whenChanged = whenChanged;
    }

    public void setWhenCreated(String whenCreated) {
        this.whenCreated = whenCreated;
    }
}
