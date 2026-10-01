package com.ctg.innovic.cmmpdi.dirsync.runner;

import com.ctg.innovic.cmmpdi.dirsync.config.PropertiesCmmpSync;
import com.ctg.innovic.cmmpdi.dirsync.service.CMMPDILdapCacheService;
import com.ctg.innovic.cmmpdi.dirsync.service.CMMPDILdapQueryService;
import com.ctg.innovic.cmmpdi.dirsync.service.CMMPDILdapUserService;
import com.ctg.innovic.cmmpdi.dirsync.service.CMMPDILdapWGDService;
import com.ctg.innovic.cmmpdi.dirsync.utils.Constants;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class ConsolidateWGDRunner implements ApplicationRunner {

    private static Logger logger = LogManager.getLogger(ConsolidateWGDRunner.class);

    @Autowired
    private PropertiesCmmpSync propertiesCmmpSync;

    @Autowired
    private CMMPDILdapWGDService cmmpdiLdapWGDService;

    @Autowired
    private CMMPDILdapCacheService cmmpdiLdapCacheService;

    @Override
    public void run(ApplicationArguments args) {

        if ( args.containsOption(Constants.ARGS_WGD) ) {

            logger.log(Level.INFO, String.format("Start WGD consolidation process in CMMP-DI..."));

            cmmpdiLdapCacheService.initCMMPDI();
            cmmpdiLdapCacheService.initCMMPDIWGD();

            try {
                cmmpdiLdapWGDService.syncWGDUsers();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
/*
            try {
                cmmpdiLdapWGDService.syncWGDGroups();
            }
            catch (Exception e) {}*/
        }
    }

}
