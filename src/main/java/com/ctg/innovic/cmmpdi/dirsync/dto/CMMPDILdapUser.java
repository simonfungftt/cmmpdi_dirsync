package com.ctg.innovic.cmmpdi.dirsync.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;

import javax.naming.ldap.LdapName;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Locale;
import java.util.Objects;

/**
 * Maps to standard LDAP objectClasses (e.g., inetOrgPerson or user).
 */
@Entry(objectClasses = {"top", "person", "organizationalPerson", "inetOrgPerson"})
public final class CMMPDILdapUser {

    private static final DateTimeFormatter LDAP_GENERALIZED_TIME =
            new DateTimeFormatterBuilder()
                    .appendPattern("yyyyMMddHHmmss")
                    .optionalStart()
                    .appendFraction(ChronoField.MILLI_OF_SECOND, 1, 3, true)
                    .optionalEnd()
                    .appendOffset("+HHmm", "Z")
                    .toFormatter();

    // --- Identity & Core Info ---

    @Id
    @JsonIgnore
    private LdapName dn;

    @Attribute(name = "distinguishedName")
    private String distinguishedName;

    @Attribute(name = "cn")
    private String commonName;

    @Attribute(name = "sn")
    private String surname;

    @Attribute(name = "givenName")
    private String firstName;

    @Attribute(name = "displayName")
    private String displayName;

    @Attribute(name = "title")
    private String title;

    @Attribute(name = "description")
    private String description;

    // --- Contact, Location & Mail ---

    @Attribute(name = "mail")
    private String email;

    @Attribute(name = "proxyAddresses")
    private String proxyAddresses;

    @Attribute(name = "hideFromAddressLists")
    private String hideFromAddressLists;

    @Attribute(name = "telephoneNumber")
    private String telephoneNumber;

    @Attribute(name = "physicalDeliveryOfficeName")
    private String physicalDeliveryOfficeName;

    @Attribute(name = "homeDirectory")
    private String homeDirectory;

    @Attribute(name = "countryCode")
    private String countryCode;

    @Attribute(name = "houseIdentifier")
    private String houseIdentifier;

    // --- Security & Certificates ---

    @Attribute(name = "userPassword")
    private String password;

    @Attribute(name = "gCert;binary", type = Attribute.Type.BINARY)
    private byte[] gCert;

    @Attribute(name = "userCert;binary", type = Attribute.Type.BINARY)
    private byte[] userCert;

    @Attribute(name = "userCert17;binary", type = Attribute.Type.BINARY)
    private byte[] userCert17;

    @Attribute(name = "userCert18;binary", type = Attribute.Type.BINARY)
    private byte[] userCert18;

    // --- Extension Attributes & Timestamps ---

    @Attribute(name = "extensionAttribute1")
    private String extensionAttribute1;

    @Attribute(name = "extensionAttribute3")
    private String extensionAttribute3;

    @Attribute(name = "extensionAttribute7")
    private String extensionAttribute7;

    @Attribute(name = "whenCreated", readonly = true)
    private String whenCreated;

    @Attribute(name = "whenChanged", readonly = true)
    private String whenChanged;

    @Transient
    private String bdCode;

    // --- Convenience & Timestamp Helpers ---

    @JsonIgnore
    public boolean isHiddenFromAddressLists() {
        return "TRUE".equalsIgnoreCase(this.hideFromAddressLists);
    }

    public void setHiddenFromAddressLists(boolean hidden) {
        this.hideFromAddressLists = hidden ? "TRUE" : "FALSE";
    }

    @JsonIgnore
    public Instant getWhenCreatedInstant() {
        return safeParseLdapTimestamp(whenCreated);
    }

    @JsonIgnore
    public Instant getWhenChangedInstant() {
        return safeParseLdapTimestamp(whenChanged);
    }

    private static Instant safeParseLdapTimestamp(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return ZonedDateTime.parse(raw, LDAP_GENERALIZED_TIME).toInstant();
        } catch (Exception e) {
            e.printStackTrace();
            return Instant.now();
        }
    }

    // --- Getters & Setters ---

    /**
     * Exposes the DN as a clean String in JSON output: "dn": "CN=a1chanchansitcmmpdi,OU=..."
     */
    @JsonGetter("dn")
    public String getDn() {
        return dn != null ? dn.toString() : null;
    }

    @JsonIgnore
    public LdapName getRealDn() {
        return dn;
    }

    public void setDn(LdapName dn) {
        this.dn = dn;
    }

    public String getDistinguishedName() {
        return distinguishedName;
    }

    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
    }

    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProxyAddresses() {
        return proxyAddresses;
    }

    public void setProxyAddresses(String proxyAddresses) {
        this.proxyAddresses = proxyAddresses;
    }

    public String getHideFromAddressLists() {
        return hideFromAddressLists;
    }

    public void setHideFromAddressLists(String hideFromAddressLists) {
        this.hideFromAddressLists = hideFromAddressLists;
    }

    public String getTelephoneNumber() {
        return telephoneNumber;
    }

    public void setTelephoneNumber(String telephoneNumber) {
        this.telephoneNumber = telephoneNumber;
    }

    public String getPhysicalDeliveryOfficeName() {
        return physicalDeliveryOfficeName;
    }

    public void setPhysicalDeliveryOfficeName(String physicalDeliveryOfficeName) {
        this.physicalDeliveryOfficeName = physicalDeliveryOfficeName;
    }

    public String getHomeDirectory() {
        return homeDirectory;
    }

    public void setHomeDirectory(String homeDirectory) {
        this.homeDirectory = homeDirectory;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getHouseIdentifier() {
        return houseIdentifier;
    }

    public void setHouseIdentifier(String houseIdentifier) {
        this.houseIdentifier = houseIdentifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public byte[] getUserCert() {
        return userCert;
    }

    public byte[] getgCert() {
        return gCert;
    }

    public void setUserCert(byte[] userCert) {
        this.userCert = userCert;
    }

    public void setgCert(byte[] gCert) {
        this.gCert = gCert;
    }

    public byte[] getUserCert17() {
        return userCert17;
    }

    public byte[] getUserCert18() {
        return userCert18;
    }

    public void setUserCert17(byte[] userCert17) {
        this.userCert17 = userCert17;
    }

    public void setUserCert18(byte[] userCert18) {
        this.userCert18 = userCert18;
    }

    public String getExtensionAttribute1() {

        if (extensionAttribute1 == null || extensionAttribute1.isBlank() || "null".equalsIgnoreCase(extensionAttribute1.trim())) {
            return "0";
        }

        return extensionAttribute1.trim();
    }

    public void setExtensionAttribute1(String extensionAttribute1) {
        this.extensionAttribute1 = extensionAttribute1;
    }

    public String getExtensionAttribute3() {
        if (extensionAttribute3 == null || extensionAttribute3.isBlank() || "null".equalsIgnoreCase(extensionAttribute3.trim())) {
            return "3";
        }

        return extensionAttribute3.trim();
    }

    public void setExtensionAttribute3(String extensionAttribute3) {
        this.extensionAttribute3 = extensionAttribute3;
    }

    public String getExtensionAttribute7() {
        return extensionAttribute7;
    }

    public void setExtensionAttribute7(String extensionAttribute7) {
        this.extensionAttribute7 = extensionAttribute7;
    }

    public String getWhenCreated() {
        return whenCreated;
    }

    public String getWhenChanged() {
        return whenChanged;
    }

    public String getBdCode() {
        return bdCode;
    }

    public void setBdCode(String bdCode) {
        this.bdCode = bdCode;
    }

    // --- Equality ---

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CMMPDILdapUser that = (CMMPDILdapUser) o;

        if (this.email == null || that.email == null) {
            return Objects.equals(this.email, that.email);
        }
        return this.email.equalsIgnoreCase(that.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email != null ? email.toLowerCase(Locale.ROOT) : null);
    }
}