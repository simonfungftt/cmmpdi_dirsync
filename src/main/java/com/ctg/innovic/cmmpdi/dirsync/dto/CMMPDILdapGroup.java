package com.ctg.innovic.cmmpdi.dirsync.dto;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;
import javax.naming.Name;
import javax.naming.ldap.LdapName;
import java.util.HashSet;
import java.util.Set;

// Maps to group objectClasses under the ou=Groups tree
@Entry(objectClasses = {"top", "posixGroup", "group"}, base = "ou=Group")
public final class CMMPDILdapGroup {

    @Id
    @JsonIgnore
    private LdapName dn;

    @Attribute(name = "cn")
    private String groupName;

    @Attribute(name = "description")
    private String description;

    @Attribute(name = "mail")
    private String email;

    @Attribute(name = "distinguishedName")
    private String distinguishedName;

    // Multi-valued attribute mapping to store member DNs
    @Attribute(name = "member")
    private Set<Name> members = new HashSet<>();


    public LdapName getRealDn() {
        return dn;
    }

    @JsonGetter("dn")
    public String getDn() {
        return dn != null ? dn.toString() : null;
    }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Set<Name> getMembers() { return members; }
    public void setMembers(Set<Name> members) { this.members = members; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDistinguishedName() {
        return distinguishedName;
    }

    // Helper methods for managing group membership
    public void addMember(Name userDn) {
        this.members.add(userDn);
    }

    public void removeMember(Name userDn) {
        this.members.remove(userDn);
    }
}