package com.ctg.innovic.cmmpdi.dirsync.runner;

import com.ctg.innovic.cmmpdi.dirsync.config.PropertiesCmmpSync;
import com.ctg.innovic.cmmpdi.dirsync.service.impl.CMMPDILdapCacheService;
import com.ctg.innovic.cmmpdi.dirsync.service.impl.CMMPDILdapWGDService;
import com.ctg.innovic.cmmpdi.dirsync.utils.Constants;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ConsolidateWGDRunner implements ApplicationRunner {

    private static Logger logger = LogManager.getLogger(ConsolidateWGDRunner.class);

    @Value("${input.user.inputFileDirectory}")
    private String contactInputFileDirectory;

    @Autowired
    private PropertiesCmmpSync propertiesCmmpSync;

    @Autowired
    private CMMPDILdapWGDService cmmpdiLdapWGDService;

    @Autowired
    private CMMPDILdapCacheService cmmpdiLdapCacheService;

    @Override
    public void run(ApplicationArguments args) {

        if ( args.containsOption(Constants.ARGS_WGD) ) {

            /**
             * This process is to move CMMP-DI user/groups under BDO OUs, with
             * extension attributes7 = true into WGD. A summary of steps in this process is:
             *
             * (a). Read CMMP-DI directory information into memory
             * (b). From memory, move target entries into WGD OU. Details as following
             * (b.i). If the user object (check by same SMTP address) does not exist, create it
             * (b.ii). If the user object (check by same SMTP address) exists, update display name, post, alias fields
             * (b.iii). If a user object in WGD by not in BDO OU, remove it
             */

            String _logMessage = "Start WGD consolidation process in CMMP-DI...";
            logger.log(Level.INFO, _logMessage);

            // (a). Read CMMP-DI directory information into memory
            cmmpdiLdapCacheService.initCMMPDIBDO();
            cmmpdiLdapCacheService.initCMMPDIWGD();

            try {
                cmmpdiLdapWGDService.syncWGDUsers();
            }
            catch (Exception e) {

            }
/*
            try {
                cmmpdiLdapWGDService.syncWGDGroups();
            }
            catch (Exception e) {

            }
 */
        }
    }

}
