package com.ctg.innovic.cmmpdi.dirsync.dto;

import lombok.Data;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.DnAttribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;

@Entry(objectClasses = {"person", "inetOrgPerson", "top"})
@Data
public class Person {

    @Id
    private Name dn;

    @Attribute(name = "cn")
    @DnAttribute(value = "cn", index = 1)
    private String fullName;

    @Attribute(name = "sn")
    private String lastName;

    @Attribute(name = "uid")
    private String userId;

    @Attribute(name = "mail")
    private String mail;

    @Attribute(name = "givenName")
    private String firstName;

    @Attribute(name = "telephoneNumber")
    private String phone;

    @Attribute(name = "userPassword")
    private String password;

    public void setCn(String cn) {
        this.fullName = cn;
    }
}
