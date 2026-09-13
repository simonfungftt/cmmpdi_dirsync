package com.ctg.innovic.cmmpdi.dirsync;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeContainer;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapUser;
import com.ctg.innovic.cmmpdi.dirsync.exception.DirSyncApplicationException;
import com.ctg.innovic.cmmpdi.dirsync.service.*;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import java.io.File;
import java.io.IOException;

@SpringBootApplication
@ComponentScan({"com.ctg.innovic.cmmpdi.dirsync"})
public class DirsyncApplication {

	private static Logger logger =  LogManager.getLogger(DirsyncApplication.class);



	public static void main(String[] args) {

		logger.info("Starting CMMP-DI DirSync Application...");

		// Disable LDAP Endpoint Identification / SAN verification
		System.setProperty("com.sun.jndi.ldap.object.disableEndpointIdentification", "true");

		SpringApplication application = new SpringApplication(DirsyncApplication.class);
		application.setBannerMode(Banner.Mode.OFF);
		application.run(args);

		ApplicationContext _context = new AnnotationConfigApplicationContext(ApplicationConfig.class);

		CMMPDILdapQueryService _serivce = _context.getBean(CMMPDILdapQueryService.class);
//		_serivce.findUsersByEmail("UATTestUATSSOUser08644@uatbdob.gov.hk");

//		_serivce.listUsers();
//		_serivce.findUsersByEmail("a1chan@chan.sit.cmmpdi");
//		_serivce.findGroupByEmail("20240129EGT4@uatbdob.gov.hk");
//		_serivce.findGroupByEmail("UATTestUATGroup00021@uat.cmmpdi");

//		logger.debug("a1chan@chan.sit.cmmpdi " + _serivce.queryUserOrGroupObjectByEmailAddress("a1chan@chan.sit.cmmpdi"));
//		logger.debug("20240129EGT4@uatbdob.gov.hk " + _serivce.queryUserOrGroupObjectByEmailAddress("20240129EGT4@uatbdob.gov.hk"));
//		logger.debug("UATTestUATGroup00021@uat.cmmpdi " + _serivce.queryUserOrGroupObjectByEmailAddress("UATTestUATGroup00021@uat.cmmpdi"));
		//_serivce.importUserData("C:\\Tmp\\DPO_activeDirectoryUser_initial_e.csv");

//		LdifReaderService _LdifReaderService = new LdifReaderService();
//		try {
//
//			CMMPExchangeContainer container = _LdifReaderService.parseLdifFile("C:\\Tmp\\3C\\Coremail Log\\cmmp_ad_full_export_20260908_144518.ldif");
//
//			System.out.println("Loaded Users: " + container.getUsers().size());
//			Gson gson = new GsonBuilder()
//					.disableHtmlEscaping()
//					.setPrettyPrinting() // Optional: formats output nicely
//					.create();
//			for (CMMPExchangeLdapUser _exUser : container.getUsers() ) {
//				logger.debug( gson.toJson(_exUser) );
//			}
//
//			System.out.println("Loaded OUs: " + container.getOrganizationalUnits().size());
//		} catch (IOException e) {
//			e.printStackTrace();
//		}

		LdapCacheService _ldapCacheService = _context.getBean(LdapCacheService.class);
		_ldapCacheService.initCMMPDI();
//
//		logger.debug( _ldapCacheService.getDnBySMTP("a1chan@chan.sit.cmmpdi") );
//		logger.debug( _ldapCacheService.getDnBySMTP("UATTestUATGroup00021@uat.cmmpdi") );

		if ( 1 == 2 ) {

			ProcessService _processService = _context.getBean(ProcessService.class);
			File _result = null;


			try {
				_result = _processService.readInputLdifFile();
			} catch (DirSyncApplicationException e) {
				e.printStackTrace();
			}

			CMMPExchangeContainer _container = null;

			try {
				_container = _processService.importFileIntoMemory(_result);
			} catch (DirSyncApplicationException e) {
				e.printStackTrace();
			}

			_processService.syncUpdateFromCMMPUser2DI(_container.getUsers());

			logger.info(_result.getAbsolutePath());
		}


		boolean b = 1 == 1;
		if ( b ) {

			CMMPDILdapGroupService _CMMPDILdapGroupService = _context.getBean(CMMPDILdapGroupService.class);

			CMMPExchangeLdapGroup _CMMPExchangeLdapGroup = new CMMPExchangeLdapGroup();

			_CMMPExchangeLdapGroup.setEmail("UATTestUATGroup00021@uat.cmmpdi");
			_CMMPExchangeLdapGroup.getMemberDNs().add("a1chan@chan.sit.cmmpdi");
			_CMMPExchangeLdapGroup.getMemberDNs().add("20240129EGT4@uatbdob.gov.hk");

			try {
				_CMMPDILdapGroupService.getMemberListBasedOnCMMPOfDI(_CMMPExchangeLdapGroup);
			}
			catch (DirSyncApplicationException e) {
				e.printStackTrace();
			}
		}
	}

}
