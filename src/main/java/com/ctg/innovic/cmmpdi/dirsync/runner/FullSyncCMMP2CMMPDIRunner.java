package com.ctg.innovic.cmmpdi.dirsync.runner;

import com.ctg.innovic.cmmpdi.dirsync.config.PropertiesCmmpSync;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapUser;
import com.ctg.innovic.cmmpdi.dirsync.service.CMMPDILdapGroupService;
import com.ctg.innovic.cmmpdi.dirsync.service.CMMPDILdapUserService;
import com.ctg.innovic.cmmpdi.dirsync.service.CMMPLdapQueryService;
import com.ctg.innovic.cmmpdi.dirsync.utils.Constants;
import com.ctg.innovic.cmmpdi.dirsync.utils.DataSyncManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FullSyncCMMP2CMMPDIRunner implements ApplicationRunner {

    private static Logger logger = LogManager.getLogger(FullSyncCMMP2CMMPDIRunner.class);

    @Autowired
    private DataSyncManager dataSyncManager;

    @Autowired
    private PropertiesCmmpSync propertiesCmmpSync;

    @Autowired
    private CMMPLdapQueryService cmmpLdapQueryService;

    @Autowired
    private CMMPDILdapUserService cmmpdiLdapUserService;

    @Autowired
    private CMMPDILdapGroupService cmmpdiLdapGroupService;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        if ( args.containsOption(Constants.ARGS_FULL) ) {

            logger.log(Level.INFO, String.format("Start running full directory sync to CMMP-DI"));

            for (String _ou : propertiesCmmpSync.getSourceBases() ) {

                List<CMMPExchangeLdapUser> _exUsers = cmmpLdapQueryService.getAllUserUnderOU(_ou);

                for ( CMMPExchangeLdapUser _exUser : _exUsers ) {
                    cmmpdiLdapUserService.createOrUpdateCMMPDIUser(_exUser);
                }
            }

            for (String _ou : propertiesCmmpSync.getSourceBases() ) {

                List<CMMPExchangeLdapGroup> _exGroups = cmmpLdapQueryService.getAllGroupUnderOU(_ou);

                for ( CMMPExchangeLdapGroup _exGroup : _exGroups ) {
                    cmmpdiLdapGroupService.createOrUpdateCMMPDIGroup(_exGroup);
                }
            }
        }
    }
}
