package com.ctg.innovic.cmmpdi.dirsync.mapper;

import com.ctg.innovic.cmmpdi.dirsync.dto.Person;
import org.springframework.ldap.core.AttributesMapper;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;

public class UserAttributeMapper implements AttributesMapper<Person> {

    @Override
    public Person mapFromAttributes(Attributes attrs) throws NamingException {
        Person person = new Person();

        if (attrs.get("cn") != null) {
            person.setCn((String) attrs.get("cn").get());
        }

        // Map other attributes as needed
        return person;
    }
}
