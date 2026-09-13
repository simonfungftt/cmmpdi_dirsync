package com.ctg.innovic.cmmpdi.dirsync.service;

import com.ctg.innovic.cmmpdi.dirsync.ApplicationConfig;
import com.ctg.innovic.cmmpdi.dirsync.dto.Person;
import com.ctg.innovic.cmmpdi.dirsync.mapper.UserAttributeMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.Name;
import java.util.List;

@Service
public class UserService {

    private final ApplicationContext context;

    @Autowired
    public UserService(ApplicationContext applicationContext) {
        this.context = applicationContext;
    }

    private static Logger logger = LogManager.getLogger(UserService.class);

    public List<Person> listUsers() {

        LdapTemplate _ldapTemplate = context.getBean(LdapTemplate.class);

        String base = "OU=Users,OU=ITB-DPO,OU=CMMPBDOs,DC=cmmp,DC=hksarg"; // search the entire base DN configured in application.properties
        String filter = "(&(objectclass=user))";

        return _ldapTemplate.search(base, filter, new UserAttributeMapper());
    }


    public String createUser(String emailAddress) {

        try {

            String _localPart = emailAddress.split("@")[0];

            Name dn = LdapNameBuilder
                    .newInstance()
                    .add("DC", "hksarg")
                    .add("DC", "cmmp")
                    .add("ou", "CMMPBDOs")
                    .add("ou", "ITB-DPO")
                    .add("ou", "Users")
                    .add("cn", _localPart)
                    .build();

            DirContextAdapter dirContext = new DirContextAdapter(dn);

            dirContext.setAttributeValues(
                    "objectclass",
                    new String[]
                            {
                                    "top",
                                    "user",
                            });
            dirContext.setAttributeValue("cn", _localPart);

            logger.info("Trying to bind to LDAP with DN " + dn.toString() + " cn = " + _localPart);

            LdapTemplate _ldapTemplate = context.getBean(LdapTemplate.class);
            _ldapTemplate.bind(dirContext);
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }


}
