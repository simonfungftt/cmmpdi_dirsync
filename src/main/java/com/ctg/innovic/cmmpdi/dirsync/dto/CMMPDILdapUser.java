package com.ctg.innovic.cmmpdi.dirsync.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.ldap.LdapName;
import java.util.Comparator;

// Map to standard LDAP objectClasses (e.g. inetOrgPerson or user)
@Entry(objectClasses = {"top", "person", "organizationalPerson", "inetOrgPerson"}, base = "ou=Users")
public final class CMMPDILdapUser {

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

    @Attribute(name = "userCert17")
    private String userCert18;

    @Attribute(name = "displayName")
    private String displayName;

    @Attribute(name = "extensionAttribute3")
    private int extensionAttribute3;

    @Attribute(name = "extensionAttribute1")
    private int extensionAttribute1;

    @Attribute(name = "extensionAttribute7")
    private int extensionAttribute7;


    private String bdCode;

    public boolean isCMMPDIMailbox() {

        if ( this.extensionAttribute3 >= 4 ) {
            return true;
        }

        return false;
    }

    public static final Comparator<CMMPDILdapUser> BY_EMAIL = Comparator.comparing(CMMPDILdapUser::getEmail);


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