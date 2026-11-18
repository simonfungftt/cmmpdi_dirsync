package com.ctg.innovic.cmmpdi.dirsync;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication(exclude = {
	org.springframework.boot.ldap.autoconfigure.LdapAutoConfiguration.class
})
@ComponentScan({"com.ctg.innovic.cmmpdi.dirsync"})
public class DirsyncApplication {

	public static void main(String[] args) {

		// Disable LDAP Endpoint Identification / SAN verification
		System.setProperty("com.sun.jndi.ldap.object.disableEndpointIdentification", "true");

		/*
		 * There are entry points under package
		 * com.ctg.innovic.cmmpdi.dirsync.runner and to be triggered by input arg
		 */

		SpringApplication application = new SpringApplication(DirsyncApplication.class);
		application.setBannerMode(Banner.Mode.OFF);
		application.run(args);
	}
}
