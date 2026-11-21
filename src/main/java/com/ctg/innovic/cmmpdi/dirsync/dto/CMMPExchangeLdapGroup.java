package com.ctg.innovic.cmmpdi.dirsync.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import org.springframework.ldap.odm.annotations.Transient;
import org.springframework.ldap.support.LdapUtils;

import javax.naming.Name;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;

@Entry(objectClasses = {"top", "group"})
public final class CMMPExchangeLdapGroup {

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
    private Name dn;

    @Attribute(name = "cn")
    private String cn;

    @Attribute(name = "description")
    private String description;

    @Attribute(name = "mail")
    private String mail;

    @Attribute(name = "mailNickname")
    private String mailNickname;

    @Attribute(name = "displayName")
    private String displayName;

    @Attribute(name = "distinguishedName", readonly = true)
    private String distinguishedName;

    @Attribute(name = "proxyAddresses")
    private List<String> proxyAddresses = new ArrayList<>();

    // Must be String: AD groups contain non-numeric values (e.g., "CMMP Enquiry RI", "Corritsdgroup|")
    @Attribute(name = "extensionAttribute1")
    private String extensionAttribute1;

    @Attribute(name = "extensionAttribute3")
    private String extensionAttribute3;

    @Attribute(name = "extensionAttribute4")
    private String extensionAttribute4;

    @Attribute(name = "extensionAttribute5")
    private String extensionAttribute5;

    @Attribute(name = "extensionAttribute6")
    private String extensionAttribute6;

    @Attribute(name = "extensionAttribute7")
    private String extensionAttribute7;

    @Attribute(name = "extensionAttribute10")
    private String extensionAttribute10;

    @Attribute(name = "member")
    private List<String> memberDNs = new ArrayList<>();

    @Attribute(name = "whenChanged", readonly = true)
    private String whenChanged;

    // Populated by service after resolving memberDNs -> SMTP addresses
    @Transient
    private List<String> memberSmtpAddresses = new ArrayList<>();

    public CMMPExchangeLdapGroup() {
    }

    // --- DN & Timestamp Helpers ---
    @JsonIgnore
    public Name getDn() {
        return dn;
    }

    public void setDn(Name dn) {
        this.dn = dn;
    }

    public String getDnString() {
        return dn != null ? dn.toString() : null;
    }

    public void setDnString(String dnString) {
        this.dn = (dnString != null && !dnString.isBlank())
                ? LdapUtils.newLdapName(dnString)
                : null;
    }

    public Instant getWhenChangedInstant() {
        if (whenChanged == null || whenChanged.isBlank()) {
            return null;
        }
        try {
            return ZonedDateTime.parse(whenChanged, LDAP_GENERALIZED_TIME).toInstant();
        } catch (Exception e) {
            return null;
        }
    }

    // --- Getters & Setters ---
    public String getCn() { return cn; }
    public void setCn(String cn) { this.cn = cn; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getMail() { return mail != null ? mail.toLowerCase() : null; }
    public void setMail(String mail) { this.mail = mail; }

    public String getMailNickname() { return mailNickname; }
    public void setMailNickname(String mailNickname) { this.mailNickname = mailNickname; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getDistinguishedName() { return distinguishedName; }
    public void setDistinguishedName(String distinguishedName) { this.distinguishedName = distinguishedName; }

    public List<String> getProxyAddresses() { return proxyAddresses; }
    public void setProxyAddresses(List<String> proxyAddresses) {
        this.proxyAddresses = proxyAddresses != null ? proxyAddresses : new ArrayList<>();
    }

    public String getExtensionAttribute1() { return extensionAttribute1; }
    public void setExtensionAttribute1(String extensionAttribute1) { this.extensionAttribute1 = extensionAttribute1; }

    public String getExtensionAttribute3() { return extensionAttribute3; }
    public void setExtensionAttribute3(String extensionAttribute3) { this.extensionAttribute3 = extensionAttribute3; }

    public String getExtensionAttribute4() { return extensionAttribute4; }
    public void setExtensionAttribute4(String extensionAttribute4) { this.extensionAttribute4 = extensionAttribute4; }

    public String getExtensionAttribute5() { return extensionAttribute5; }
    public void setExtensionAttribute5(String extensionAttribute5) { this.extensionAttribute5 = extensionAttribute5; }

    public String getExtensionAttribute6() { return extensionAttribute6; }
    public void setExtensionAttribute6(String extensionAttribute6) { this.extensionAttribute6 = extensionAttribute6; }

    public String getExtensionAttribute7() { return extensionAttribute7; }
    public void setExtensionAttribute7(String extensionAttribute7) { this.extensionAttribute7 = extensionAttribute7; }

    public String getExtensionAttribute10() { return extensionAttribute10; }
    public void setExtensionAttribute10(String extensionAttribute10) { this.extensionAttribute10 = extensionAttribute10; }

    public List<String> getMemberDNs() { return memberDNs; }
    public void setMemberDNs(List<String> memberDNs) {
        this.memberDNs = memberDNs != null ? memberDNs : new ArrayList<>();
    }

    public String getWhenChanged() { return whenChanged; }
    public void setWhenChanged(String whenChanged) { this.whenChanged = whenChanged; }

    public List<String> getMemberSmtpAddresses() { return memberSmtpAddresses; }
    public void setMemberSmtpAddresses(List<String> memberSmtpAddresses) {
        this.memberSmtpAddresses = memberSmtpAddresses != null ? memberSmtpAddresses : new ArrayList<>();
    }
}