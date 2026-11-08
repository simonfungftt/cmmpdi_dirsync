package com.ctg.innovic.cmmpdi.dirsync;

import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({"com.ctg.innovic.cmmpdi.dirsync"})
public class DirsyncApplication {

	public static void main(String[] args) {

		// Disable LDAP Endpoint Identification / SAN verification
		System.setProperty("com.sun.jndi.ldap.object.disableEndpointIdentification", "true");

		SpringApplication application = new SpringApplication(DirsyncApplication.class);
		application.setBannerMode(Banner.Mode.OFF);
		application.run(args);
	}

}
