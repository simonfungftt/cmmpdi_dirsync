package com.ctg.innovic.cmmpdi.dirsync;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapUser;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.exception.DirSyncApplicationException;
import com.ctg.innovic.cmmpdi.dirsync.service.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import java.io.IOException;
import java.util.List;

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

		ProcessService _processService = _context.getBean(ProcessService.class);

		try {
			_processService.initCMMPCache();
		}
//		catch (DirSyncApplicationException e) {
//			e.printStackTrace();
//		}
		catch (IOException e) {
			e.printStackTrace();
		}
//		catch (InvalidNameException e) {
//			e.printStackTrace();
//		}

//		CMMPDILdapQueryService _serivce = _context.getBean(CMMPDILdapQueryService.class);

		// Step 1: Scan input user folder, process file by file
		//CMMPLdifReaderService _ldifReaderService = _context.getBean(CMMPLdifReaderService.class);

		int _processedFileCount = 0;

//		while ( _processedFileCount < 10 ) {
//
//			CMMPExchangeContainer container = _ldifReaderService.
//
//
//			_processedFileCount++;
//		}

		// Step 2: Input loaded into a container


		// Step 3: Process the container


		//

		// Step 2: Scan input group folder



//		_serivce.findUsersByEmail("UATTestUATSSOUser08644@uatbdob.gov.hk");

//		_serivce.listUsers();
//		_serivce.findUsersByEmail("a1chan@chan.sit.cmmpdi");
//		_serivce.findGroupByEmail("20240129EGT4@uatbdob.gov.hk");
//		_serivce.findGroupByEmail("UATTestUATGroup00021@uat.cmmpdi");

//		logger.debug("a1chan@chan.sit.cmmpdi " + _serivce.queryUserOrGroupObjectByEmailAddress("a1chan@chan.sit.cmmpdi"));
//		logger.debug("20240129EGT4@uatbdob.gov.hk " + _serivce.queryUserOrGroupObjectByEmailAddress("20240129EGT4@uatbdob.gov.hk"));
//		logger.debug("UATTestUATGroup00021@uat.cmmpdi " + _serivce.queryUserOrGroupObjectByEmailAddress("UATTestUATGroup00021@uat.cmmpdi"));
		//_serivce.importUserData("C:\\Tmp\\DPO_activeDirectoryUser_initial_e.csv");

//		CMMPLdifReaderService _LdifReaderService = new CMMPLdifReaderService();
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
//
//		CMMPDILdapCacheService _CMMPDI_ldapCacheService = _context.getBean(CMMPDILdapCacheService.class);
//		_CMMPDI_ldapCacheService.initCMMPDI();
//
//		logger.debug( _CMMPDI_ldapCacheService.getDnBySMTP("a1chan@chan.sit.cmmpdi") );
//		logger.debug( _CMMPDI_ldapCacheService.getDnBySMTP("UATTestUATGroup00021@uat.cmmpdi") );

		if ( 1 == 2 ) {
/*
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

			logger.info(_result.getAbsolutePath());*/
		}


		boolean b = 1 == 2;
		if ( b ) {

			CMMPDILdapGroupService _CMMPDILdapGroupService = _context.getBean(CMMPDILdapGroupService.class);

			CMMPExchangeLdapGroup _CMMPExchangeLdapGroup = new CMMPExchangeLdapGroup();

			_CMMPExchangeLdapGroup.setMail("UATTestUATGroup00021@uat.cmmpdi");
			_CMMPExchangeLdapGroup.getMemberDNs().add("a1chan@chan.sit.cmmpdi");
			_CMMPExchangeLdapGroup.getMemberDNs().add("revho@ho.uat.cmmpdi");
			_CMMPExchangeLdapGroup.getMemberDNs().add("20240129EGT4@uatbdob.gov.hk");

			try {
				List<Name> _result = _CMMPDILdapGroupService.getMemberListBasedOnCMMPOfDI(_CMMPExchangeLdapGroup);
				for ( Name _name : _result ) {
					logger.debug("_name = " + _name);
				}

//				_CMMPDILdapGroupService.updateGroupMembership("UATTestUATGroup00021@uat.cmmpdi", _result);
			}
			catch (DirSyncApplicationException e) {
				e.printStackTrace();
			}
		}

		boolean c = 1 == 2;

		if ( c ) {

//			CMMPDILdapUserService cmmpdiLdapUserService = _context.getBean(CMMPDILdapUserService.class);
//
//			CMMPDILdapUser _user = _CMMPDI_ldapCacheService.getCMMPDIUserDnBySMTP("a1chan@chan.sit.cmmpdi");
//
//			logger.debug( _user.getRealDn() );
//
//			CMMPDILdapGroupService cmmpdiLdapGroupService = _context.getBean(CMMPDILdapGroupService.class);
//
//			CMMPDILdapGroup _group = _CMMPDI_ldapCacheService.getCMMPDIGroupDnBySMTP("UATTestUATGroup00021@uat.cmmpdi");
//
//			logger.debug( _user.getRealDn() );

//			try {
//				cmmpdiLdapGroupService.createCMMPDIGroup(_group);
//			} catch (InvalidNameException e) {
//				e.printStackTrace();
//			}
		}
	}

}
