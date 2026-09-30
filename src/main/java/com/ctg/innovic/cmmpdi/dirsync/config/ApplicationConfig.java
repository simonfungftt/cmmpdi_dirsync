package com.ctg.innovic.cmmpdi.dirsync.config;

import com.ctg.innovic.cmmpdi.dirsync.utils.TrustAllLdapSocketFactory;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.ldap.repository.config.EnableLdapRepositories;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource("classpath:application.properties")
@ComponentScan({"com.ctg.innovic.cmmpdi.dirsync"})
@EnableLdapRepositories("com.ctg.innovic.cmmpdi.dirsync.repro")
@Getter
public class ApplicationConfig {

    private static Logger logger = LogManager.getLogger(ApplicationConfig.class);

    @Value("${ldap.userDN}")
    private String userDN;

    @Value("${ldap.password}")
    private String password;

    @Value("${ldap.url}")
    private String url;

    @Bean("cmmpdiLdapContextSource")
    ContextSource contextSource() {

        logger.trace("Building LDAP configuration context...");
        logger.trace("userDN is " + this.userDN);
        logger.trace("userPassword is " + this.password);

        LdapContextSource ldapContextSource = new LdapContextSource();

        ldapContextSource.setUserDn(this.userDN);
        ldapContextSource.setPassword(this.password);
        ldapContextSource.setUrl(this.url);
        ldapContextSource.setPooled(false);

        // Set custom environment properties to bypass SSL certificate validation
        Map<String, Object> baseEnvironmentProperties = new HashMap<>();
        baseEnvironmentProperties.put("java.naming.ldap.factory.socket", TrustAllLdapSocketFactory.class.getName());
        baseEnvironmentProperties.put("java.naming.ldap.version", "3");
        baseEnvironmentProperties.put("com.sun.jndi.ldap.connect.timeout", "5000"); // 5s connection timeout
        baseEnvironmentProperties.put("com.sun.jndi.ldap.read.timeout", "10000");   // 10s read timeout

        ldapContextSource.setBaseEnvironmentProperties(baseEnvironmentProperties);
        ldapContextSource.afterPropertiesSet();

        return ldapContextSource;
    }

    @Bean("cmmpdiLdapTemplate")
    LdapTemplate cmmpdiLdapTemplate(@Qualifier("cmmpdiLdapContextSource") ContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }

    @Bean("cmmpLdapTemplate")
    LdapTemplate cmmpLdapTemplate(@Qualifier("cmmpdiLdapContextSource") ContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }

}
