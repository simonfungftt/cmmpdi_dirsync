package com.ctg.innovic.cmmpdi.dirsync.runner;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapUser;
import com.ctg.innovic.cmmpdi.dirsync.service.impl.CMMPDILdapCacheService;
import com.ctg.innovic.cmmpdi.dirsync.service.impl.CMMPDILdapGroupService;
import com.ctg.innovic.cmmpdi.dirsync.service.impl.CMMPDILdapUserService;
import com.ctg.innovic.cmmpdi.dirsync.utils.Constants;
import com.google.gson.Gson;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ExportDIUserFile2CMMPRunner implements ApplicationRunner {

    private static Logger logger = LogManager.getLogger(ExportDIUserFile2CMMPRunner.class);

    private final CMMPDILdapUserService ningtonLdapUserService;
    private final CMMPDILdapGroupService ningtonLdapGroupService;
    private final CMMPDILdapCacheService cmmpdiLdapCacheService;

    public ExportDIUserFile2CMMPRunner(
            CMMPDILdapUserService ningtonLdapUserService,
            CMMPDILdapGroupService ningtonLdapGroupService,
            CMMPDILdapCacheService cmmpdiLdapCacheService) {

        this.ningtonLdapUserService = ningtonLdapUserService;
        this.ningtonLdapGroupService = ningtonLdapGroupService;
        this.cmmpdiLdapCacheService = cmmpdiLdapCacheService;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        if ( args.containsOption(Constants.SyncArg.EXPORT.getValue()) ) {

            String _logMessage = "Start running full directory sync to CMMP-DI";
            logger.log(Level.INFO, _logMessage);

            /**
             * This process is to read CMMP LDAP information (by OU), and write into same OU
             */
            cmmpdiLdapCacheService.initCMMPDIBDO();

            List<CMMPDILdapUser> _users = cmmpdiLdapCacheService.getAllBDOUsersFromBDOFromCache();

            Gson _gson = new Gson();

            for ( CMMPDILdapUser _user : _users ) {

                if ( Integer.valueOf(_user.getExtensionAttribute3()) >= 4 ) {
                    logger.info( _gson.toJson(_user));
                }
            }
        }
    }
}
