package com.ctg.innovic.cmmpdi.dirsync.config;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.ldap.repository.config.EnableLdapRepositories;
import org.springframework.ldap.core.ContextSource;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.ldap.pool2.factory.PoolConfig;
import org.springframework.ldap.pool2.factory.PooledContextSource;
import org.springframework.ldap.pool2.validation.DefaultDirContextValidator;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableLdapRepositories(
        basePackages = "com.ctg.innovic.cmmpdi.dirsync.repro",
        ldapTemplateRef = "cmmpdiLdapTemplate"
)
public class ApplicationConfig {

    private static final Logger logger = LogManager.getLogger(ApplicationConfig.class);

    @Value("${ldap.userDN}")
    private String userDN;

    @Value("${ldap.password}")
    private String password;

    @Value("${ldap.url}")
    private String url;

    @Value("${ldap.exchange.userDN}")
    private String exchangeUserDN;

    @Value("${ldap.exchange.password}")
    private String exchangePassword;

    @Value("${ldap.exchange.url}")
    private String exchangeUrl;

    private Map<String, Object> buildBaseEnv() {
        Map<String, Object> env = new HashMap<>();
        env.put("java.naming.ldap.version", "3");
        env.put("com.sun.jndi.ldap.connect.timeout", "5000");
        env.put("com.sun.jndi.ldap.read.timeout", "15000");
        env.put("java.naming.ldap.attributes.binary",
                "gCert userCert userCert17 userCert18  userCertificate userSMIMECertificate");

        return env;
    }

    @Bean("cmmpdiLdapContextSource")
    public ContextSource cmmpdiLdapContextSource() {
        LdapContextSource ldapContextSource = new LdapContextSource();
        ldapContextSource.setUserDn(this.userDN);
        ldapContextSource.setPassword(this.password);
        ldapContextSource.setUrl(this.url);
        ldapContextSource.setPooled(false);
        ldapContextSource.setReferral("ignore");
        ldapContextSource.setBaseEnvironmentProperties(buildBaseEnv());
        ldapContextSource.afterPropertiesSet();

        PoolConfig poolConfig = new PoolConfig();
        poolConfig.setMaxTotal(10);                  // Max total connections across read-only & read-write
        poolConfig.setMaxTotalPerKey(5);            // Max connections per type (read-only or read-write)
        poolConfig.setMaxIdlePerKey(5);             // Max idle connections per type
        poolConfig.setMinIdlePerKey(2);              // Min idle connections kept warm
        poolConfig.setMaxWaitMillis(15000);           // Max time to wait for a connection before throwing
        poolConfig.setBlockWhenExhausted(true);      // Block until a connection is free (up to maxWaitMillis)

        // Validation & Eviction (recommended to avoid stale connections dropped by firewalls/LDAP server)
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTimeBetweenEvictionRunsMillis(60_000);      // Run evictor every 1 min
        poolConfig.setMinEvictableIdleTimeMillis(300_000);        // Evict idle connections after 5 mins

        // 3. Wrap in PooledContextSource
        PooledContextSource pooledContextSource = new PooledContextSource(poolConfig);
        pooledContextSource.setContextSource(ldapContextSource);
        // Required whenever testOnBorrow, testOnReturn, or testWhileIdle is true
        pooledContextSource.setDirContextValidator(new DefaultDirContextValidator());

        return pooledContextSource;
    }

    @Bean("exchangeLdapContextSource")
    public ContextSource exchangeLdapContextSource() {
        logger.log(Level.DEBUG, "Building Exchange LDAP configuration context...");
        LdapContextSource ldapContextSource = new LdapContextSource();
        ldapContextSource.setUserDn(this.exchangeUserDN);
        ldapContextSource.setPassword(this.exchangePassword);
        ldapContextSource.setUrl(this.exchangeUrl);
        ldapContextSource.setPooled(false);
        ldapContextSource.setReferral("ignore");
        ldapContextSource.setBaseEnvironmentProperties(buildBaseEnv());
        ldapContextSource.afterPropertiesSet();

        PoolConfig poolConfig = new PoolConfig();
        poolConfig.setMaxTotal(10);                  // Max total connections across read-only & read-write
        poolConfig.setMaxTotalPerKey(5);            // Max connections per type (read-only or read-write)
        poolConfig.setMaxIdlePerKey(5);             // Max idle connections per type
        poolConfig.setMinIdlePerKey(2);              // Min idle connections kept warm
        poolConfig.setMaxWaitMillis(15000);           // Max time to wait for a connection before throwing
        poolConfig.setBlockWhenExhausted(true);      // Block until a connection is free (up to maxWaitMillis)

        // Validation & Eviction (recommended to avoid stale connections dropped by firewalls/LDAP server)
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTimeBetweenEvictionRunsMillis(60_000);      // Run evictor every 1 min
        poolConfig.setMinEvictableIdleTimeMillis(300_000);        // Evict idle connections after 5 mins

        // 3. Wrap in PooledContextSource
        PooledContextSource pooledContextSource = new PooledContextSource(poolConfig);
        pooledContextSource.setContextSource(ldapContextSource);
        // Required whenever testOnBorrow, testOnReturn, or testWhileIdle is true
        pooledContextSource.setDirContextValidator(new DefaultDirContextValidator());

        return pooledContextSource;
    }

    @Bean("cmmpdiLdapTemplate")
    public LdapTemplate cmmpdiLdapTemplate(
            @Qualifier("cmmpdiLdapContextSource") ContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }

    @Bean("cmmpLdapTemplate")
    public LdapTemplate cmmpLdapTemplate(
            @Qualifier("exchangeLdapContextSource") ContextSource contextSource) {
        return new LdapTemplate(contextSource);
    }
}