package com.ctg.innovic.cmmpdi.dirsync;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.ldap.repository.config.EnableLdapRepositories;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

@Configuration
@PropertySource("classpath:application.properties")
@EnableLdapRepositories("com.ctg.innovic.cmmpdi.dirsync.repro")
public class ApplicationConfig {

    private static Logger logger = LogManager.getLogger(ApplicationConfig.class);

    @Value("${ldap.userDN}")
    private String userDN;

    @Value("${ldap.password}")
    private String password;

    @Value("${ldap.url}")
    private String url;

    @Bean
    ContextSource contextSource() {

        logger.trace("Building LDAP configuration context...");
        logger.trace("userDN is " + this.userDN);
        logger.trace("userPassword is " + this.password);

        LdapContextSource ldapContextSource = new LdapContextSource();

        ldapContextSource.setUserDn(this.userDN);
        ldapContextSource.setPassword(this.password);
        ldapContextSource.setUrl(this.url);

        return ldapContextSource;
    }

    @Bean
    LdapTemplate ldapTemplate(ContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }

}
