package com.ctg.innovic.cmmpdi.dirsync.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;
import javax.naming.ldap.LdapName;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Maps to group objectClasses under the ou=Group tree.
 */
@Entry(objectClasses = {"top", "posixGroup", "group"})
public final class CMMPDILdapGroup {

    @Id
    @JsonIgnore
    private LdapName dn;

    @Attribute(name = "cn")
    private String groupName;

    @Attribute(name = "description")
    private String description;

    @Attribute(name = "displayName")
    private String displayName;

    @Attribute(name = "mail")
    private String email;

    @Attribute(name = "distinguishedName")
    private String distinguishedName;

    @Attribute(name = "member")
    private Set<Name> members = new HashSet<>();

    @Attribute(name = "proxyAddresses")
    private String proxyAddresses;

    @Attribute(name = "extensionAttribute7")
    private String extensionAttribute7;

    @Attribute(name = "hideFromAddressLists")
    private String hideFromAddressLists;

    public LdapName getRealDn() {
        return dn;
    }

    @JsonGetter("dn")
    public String getDn() {
        return dn != null ? dn.toString() : null;
    }

    public void setDn(LdapName dn) {
        this.dn = dn;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDistinguishedName() {
        return distinguishedName;
    }

    public void setDistinguishedName(String distinguishedName) {
        this.distinguishedName = distinguishedName;
    }

    public Set<Name> getMembers() {
        return members;
    }

    public void setMembers(Set<Name> members) {
        this.members = members != null ? members : new HashSet<>();
    }

    public void addMember(Name userDn) {
        if (userDn != null) {
            this.members.add(userDn);
        }
    }

    public void removeMember(Name userDn) {
        if (userDn != null) {
            this.members.remove(userDn);
        }
    }

    public String getProxyAddresses() {
        return proxyAddresses;
    }

    public void setProxyAddresses(String proxyAddresses) {
        this.proxyAddresses = proxyAddresses;
    }

    public String getExtensionAttribute7() {
        return extensionAttribute7;
    }

    public void setExtensionAttribute7(String extensionAttribute7) {
        this.extensionAttribute7 = extensionAttribute7;
    }

    public String getHideFromAddressLists() {
        return hideFromAddressLists;
    }

    public void setHideFromAddressLists(String hideFromAddressLists) {
        this.hideFromAddressLists = hideFromAddressLists;
    }

    @JsonIgnore
    public boolean isHiddenFromAddressLists() {
        return "TRUE".equalsIgnoreCase(this.hideFromAddressLists);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CMMPDILdapGroup that = (CMMPDILdapGroup) o;

        if (this.email == null || that.email == null) {
            return Objects.equals(this.email, that.email);
        }
        return this.email.equalsIgnoreCase(that.email);
    }
}