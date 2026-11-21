package com.ctg.innovic.cmmpdi.dirsync.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.support.LdapUtils;

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

    // --- Core Identity ---
    @Attribute(name = "cn")
    private String cn;

    @Attribute(name = "sn")
    private String sn;

    @Attribute(name = "givenName")
    private String givenName;

    @Attribute(name = "displayName")
    private String displayName;

    @Attribute(name = "name")
    private String name;

    @Attribute(name = "sAMAccountName")
    private String sAMAccountName;

    @Attribute(name = "userPrincipalName")
    private String userPrincipalName;

    @Attribute(name = "distinguishedName", readonly = true)
    private String distinguishedName;

    // --- Organization & Contact ---
    @Attribute(name = "title")
    private String title;

    @Attribute(name = "description")
    private String description;

    @Attribute(name = "department")
    private String department;

    @Attribute(name = "postOfficeBox")
    private String postOfficeBox;

    @Attribute(name = "physicalDeliveryOfficeName")
    private String physicalDeliveryOfficeName;

    @Attribute(name = "generationQualifier")
    private String generationQualifier;

    @Attribute(name = "mail")
    private String mail;

    @Attribute(name = "mailNickname")
    private String mailNickname;

    @Attribute(name = "proxyAddresses")
    private String[] proxyAddresses;

    @Attribute(name = "homePhone")
    private String homePhone;

    @Attribute(name = "pager")
    private String pager;

    @Attribute(name = "wWWHomePage")
    private String wWWHomePage;

    @Attribute(name = "countryCode")
    private Integer countryCode;

    // --- Certificates (Binary) ---
    @Attribute(name = "userCert", type = Attribute.Type.BINARY)
    private byte[] userCert;

    @Attribute(name = "userCertificate", type = Attribute.Type.BINARY)
    private byte[] userCertificate;

    @Attribute(name = "userSMIMECertificate", type = Attribute.Type.BINARY)
    private byte[] userSMIMECertificate;

    // --- Extension Attributes ---
    @Attribute(name = "extensionAttribute1")
    private String extensionAttribute1;

    @Attribute(name = "extensionAttribute3")
    private String extensionAttribute3;

    @Attribute(name = "extensionAttribute4")
    private String extensionAttribute4;

    @Attribute(name = "extensionAttribute5")
    private String extensionAttribute5;

    @Attribute(name = "extensionAttribute7")
    private String extensionAttribute7;

    @Attribute(name = "extensionAttribute10")
    private String extensionAttribute10;

    @Attribute(name = "extensionAttribute11")
    private String extensionAttribute11;

    @Attribute(name = "extensionAttribute12")
    private String extensionAttribute12;

    // --- Exchange & System Metadata ---
    @Attribute(name = "msExchAddressBookFlags")
    private String msExchAddressBookFlags;

    @Attribute(name = "telephoneNumber")
    private String telephoneNumber;

    @Attribute(name = "whenCreated", readonly = true)
    private String whenCreated;

    @Attribute(name = "whenChanged", readonly = true)
    private String whenChanged;

    // --- DN Helpers ---
    public String getDn() {
        return this.dn != null ? this.dn.toString() : null;
    }

    public void setDnString(String dnString) {
        this.dn = (dnString != null && !dnString.isBlank())
                ? LdapUtils.newLdapName(dnString)
                : null;
    }

    // --- Timestamp Helpers ---
    public Instant getWhenCreatedInstant() {
        return parseLdapTimestamp(whenCreated);
    }

    public Instant getWhenChangedInstant() {
        return parseLdapTimestamp(whenChanged);
    }

    private static Instant parseLdapTimestamp(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return ZonedDateTime.parse(raw, LDAP_GENERALIZED_TIME).toInstant();
        } catch (Exception e) {
            return Instant.now();
        }
    }

    // --- Getters & Setters ---
    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public String getSn() {
        return sn;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getsAMAccountName() {
        return sAMAccountName;
    }

    public void setsAMAccountName(String sAMAccountName) {
        this.sAMAccountName = sAMAccountName;
    }

    public String getUserPrincipalName() {
        return userPrincipalName;
    }

    public void setUserPrincipalName(String userPrincipalName) {
        this.userPrincipalName = userPrincipalName;
    }

    public String getDistinguishedName() {
        return distinguishedName;
    }

    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPostOfficeBox() {
        return postOfficeBox;
    }

    public void setPostOfficeBox(String postOfficeBox) {
        this.postOfficeBox = postOfficeBox;
    }

    public String getPhysicalDeliveryOfficeName() {
        return physicalDeliveryOfficeName;
    }

    public void setPhysicalDeliveryOfficeName(String physicalDeliveryOfficeName) {
        this.physicalDeliveryOfficeName = physicalDeliveryOfficeName;
    }

    public String getGenerationQualifier() {
        return generationQualifier;
    }

    public void setGenerationQualifier(String generationQualifier) {
        this.generationQualifier = generationQualifier;
    }

    public String getMail() {

        if ( mail == null ) return null;

        return mail.toLowerCase();
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getMailNickname() {
        return mailNickname;
    }

    public void setMailNickname(String mailNickname) {
        this.mailNickname = mailNickname;
    }

    public String[] getProxyAddresses() {
        return proxyAddresses;
    }

    public void setProxyAddresses(String[] proxyAddresses) {
        this.proxyAddresses = proxyAddresses;
    }

    public String getHomePhone() {
        return homePhone;
    }

    public void setHomePhone(String homePhone) {
        this.homePhone = homePhone;
    }

    public String getPager() {
        return pager;
    }

    public void setPager(String pager) {
        this.pager = pager;
    }

    public String getwWWHomePage() {
        return wWWHomePage;
    }

    public void setwWWHomePage(String wWWHomePage) {
        this.wWWHomePage = wWWHomePage;
    }

    public Integer getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(Integer countryCode) {
        this.countryCode = countryCode;
    }

    public byte[] getUserCert() {
        return userCert;
    }

    public void setUserCert(byte[] userCert) {
        this.userCert = userCert;
    }

    public byte[] getUserCertificate() {
        return userCertificate;
    }

    public void setUserCertificate(byte[] userCertificate) {
        this.userCertificate = userCertificate;
    }

    public byte[] getUserSMIMECertificate() {
        return userSMIMECertificate;
    }

    public void setUserSMIMECertificate(byte[] userSMIMECertificate) {
        this.userSMIMECertificate = userSMIMECertificate;
    }

    public String getExtensionAttribute1() {
        return extensionAttribute1;
    }

    public void setExtensionAttribute1(String extensionAttribute1) {
        this.extensionAttribute1 = extensionAttribute1;
    }

    public String getExtensionAttribute3() {
        return extensionAttribute3;
    }

    public void setExtensionAttribute3(String extensionAttribute3) {
        this.extensionAttribute3 = extensionAttribute3;
    }

    public String getExtensionAttribute4() {
        return extensionAttribute4;
    }

    public void setExtensionAttribute4(String extensionAttribute4) {
        this.extensionAttribute4 = extensionAttribute4;
    }

    public String getExtensionAttribute5() {
        return extensionAttribute5;
    }

    public void setExtensionAttribute5(String extensionAttribute5) {
        this.extensionAttribute5 = extensionAttribute5;
    }

    public String getExtensionAttribute7() {
        return extensionAttribute7;
    }

    public void setExtensionAttribute7(String extensionAttribute7) {
        this.extensionAttribute7 = extensionAttribute7;
    }

    public String getExtensionAttribute10() {
        return extensionAttribute10;
    }

    public void setExtensionAttribute10(String extensionAttribute10) {
        this.extensionAttribute10 = extensionAttribute10;
    }

    public String getExtensionAttribute11() {
        return extensionAttribute11;
    }

    public void setExtensionAttribute11(String extensionAttribute11) {
        this.extensionAttribute11 = extensionAttribute11;
    }

    public String getExtensionAttribute12() {
        return extensionAttribute12;
    }

    public void setExtensionAttribute12(String extensionAttribute12) {
        this.extensionAttribute12 = extensionAttribute12;
    }

    public String getMsExchAddressBookFlags() {
        return msExchAddressBookFlags;
    }

    public void setMsExchAddressBookFlags(String msExchAddressBookFlags) {
        this.msExchAddressBookFlags = msExchAddressBookFlags;
    }

    public String getWhenCreated() {
        return whenCreated;
    }

    public void setWhenCreated(String whenCreated) {
        this.whenCreated = whenCreated;
    }

    public String getWhenChanged() {
        return whenChanged;
    }

    public void setWhenChanged(String whenChanged) {
        this.whenChanged = whenChanged;
    }
}