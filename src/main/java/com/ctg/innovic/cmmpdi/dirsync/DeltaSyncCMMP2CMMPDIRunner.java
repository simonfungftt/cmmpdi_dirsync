package com.ctg.innovic.cmmpdi.dirsync;

import com.ctg.innovic.cmmpdi.dirsync.config.PropertiesCmmpSync;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapUser;
import com.ctg.innovic.cmmpdi.dirsync.service.CMMPDILdapGroupService;
import com.ctg.innovic.cmmpdi.dirsync.service.CMMPDILdapUserService;
import com.ctg.innovic.cmmpdi.dirsync.service.CMMPLdapQueryService;
import com.ctg.innovic.cmmpdi.dirsync.utils.DataSyncManager;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

import java.time.Instant;
import java.util.List;

public class DeltaSyncCMMP2CMMPDIRunner implements ApplicationRunner {

    private static Logger logger = LogManager.getLogger(DeltaSyncCMMP2CMMPDIRunner.class);

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

        if ( args.containsOption("delta") ) {

            Instant _lastDataSyncTime = dataSyncManager.getLastSyncTime();

            for (String _ou : propertiesCmmpSync.getSourceBases() ) {

                List<CMMPExchangeLdapUser> _exUsers = cmmpLdapQueryService.getLastModifiedUserUnderOU(_ou, _lastDataSyncTime);

                for ( CMMPExchangeLdapUser _exUser : _exUsers ) {
                    cmmpdiLdapUserService.createOrUpdateCMMPDIUser(_exUser);
                }
            }

            for (String _ou : propertiesCmmpSync.getSourceBases() ) {

//                List<CMMPExchangeLdapUser> _exUsers = cmmpLdapQueryService.getLastModifiedGroupUnderOU(_ou, _lastDataSyncTime);
//
//                for ( CMMPExchangeLdapUser _exUser : _exUsers ) {
//                    cmmpdiLdapUserService.createOrUpdateCMMPDIUser(_exUser);
//                }
            }

            // Foreach OU in CMMP, get users modified last since last data sync time

            // Fetch for the list

            // Do the create / update

            dataSyncManager.saveLastSyncTime(Instant.now());
        }
    }
}
