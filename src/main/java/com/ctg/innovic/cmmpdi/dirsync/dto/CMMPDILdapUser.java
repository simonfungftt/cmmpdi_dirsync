package com.ctg.innovic.cmmpdi.dirsync.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;

import javax.naming.ldap.LdapName;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Comparator;
import java.util.Date;
import java.util.Objects;

// Map to standard LDAP objectClasses (e.g. inetOrgPerson or user)
@Entry(objectClasses = {"top", "person", "organizationalPerson", "inetOrgPerson"}, base = "ou=Users")
public final class CMMPDILdapUser {

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

    @Attribute(name = "distinguishedName")
    private String distinguishedName;

    @Attribute(name = "cn")
    private String commonName;

    @Attribute(name = "sn")
    private String surname;

    @Attribute(name = "givenName")
    private String firstName;

    @Attribute(name = "mail")
    private String email;

    @Attribute(name = "userPassword")
    private String password;

    @Attribute(name = "gCert")
    private String gCert;

    @Attribute(name = "userCert")
    private String userCert;

    @Attribute(name = "userCert17")
    private String userCert17;

    @Attribute(name = "userCert18")
    private String userCert18;

    @Attribute(name = "title")
    private String title;

    @Attribute(name = "displayName")
    private String displayName;

    @Attribute(name = "extensionAttribute3")
    private int extensionAttribute3;

    @Attribute(name = "extensionAttribute1")
    private int extensionAttribute1;

    @Attribute(name = "extensionAttribute7")
    private int extensionAttribute7;

    @Attribute(name = "description")
    private String description;

    @Attribute(name = "proxyAddresses")
    private String proxyAddresses;

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

    public String getProxyAddresses() {
        return proxyAddresses;
    }

    public void setProxyAddresses(String proxyAddresses) {
        this.proxyAddresses = proxyAddresses;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String bdCode;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUserCert17() {
        return userCert17;
    }

    public String getUserCert18() {
        return userCert18;
    }

//    get

    public boolean isCMMPDIMailbox() {

        if ( this.extensionAttribute3 >= 4 ) {
            return true;
        }

        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CMMPDILdapUser that = (CMMPDILdapUser) o;

        // Compare email case-insensitively (or replace with the unique key shared across OUs)
        if (this.email == null || that.email == null) {
            return Objects.equals(this.email, that.email);
        }
        return this.email.equalsIgnoreCase(that.email);
    }

    @Override
    public int hashCode() {
        // Must normalize case to match equalsIgnoreCase()
        return Objects.hash(email != null ? email.toLowerCase() : null);
    }

    /**
     * Exposes the DN as a clean String in JSON output: "dn": "CN=a1chanchansitcmmpdi,OU=..."
     */
    @JsonGetter("dn")
    public String getDn() {
        return dn != null ? dn.toString() : null;
    }

    public LdapName getRealDn() {
        return dn;
    }

    public String getSurname() {
        return surname;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getDistinguishedName() {
        return distinguishedName;
    }

    public int getExtensionAttribute3() {
        return extensionAttribute3;
    }

    // Getters and Setters
    public void setDn(LdapName dn) { this.dn = dn; }

    public String getCommonName() { return commonName; }
    public void setCommonName(String commonName) { this.commonName = commonName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() {
        return displayName;
    }


    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public int getExtensionAttribute1() {
        return this.extensionAttribute1;
    }

    public String getgCert() {
        return gCert;
    }

    public String getUserCert() {
        return userCert;
    }

    public int getExtensionAttribute7() {
        return extensionAttribute7;
    }

    public void setExtensionAttribute7(int extensionAttribute7) {
        this.extensionAttribute7 = extensionAttribute7;
    }

    public void setExtensionAttribute3(int extensionAttribute3) {
        this.extensionAttribute3 = extensionAttribute3;
    }

    public void setExtensionAttribute1(int extensionAttribute1) {
        this.extensionAttribute1 = extensionAttribute1;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public void setgCert(String gCert) {
        this.gCert = gCert;
    }

    public void setUserCert(String userCert) {
        this.userCert = userCert;
    }

    //    pub
}