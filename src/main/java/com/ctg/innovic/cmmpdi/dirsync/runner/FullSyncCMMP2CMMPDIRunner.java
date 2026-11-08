package com.ctg.innovic.cmmpdi.dirsync.runner;

import com.ctg.innovic.cmmpdi.dirsync.config.PropertiesCmmpSync;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapUser;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapUser;
import com.ctg.innovic.cmmpdi.dirsync.service.impl.CMMPDILdapGroupService;
import com.ctg.innovic.cmmpdi.dirsync.service.impl.CMMPDILdapUserService;
import com.ctg.innovic.cmmpdi.dirsync.service.impl.CMMPLdapQueryService;
import com.ctg.innovic.cmmpdi.dirsync.utils.Constants;
import com.ctg.innovic.cmmpdi.dirsync.utils.DataSyncManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FullSyncCMMP2CMMPDIRunner implements ApplicationRunner {

    private static Logger logger = LogManager.getLogger(FullSyncCMMP2CMMPDIRunner.class);

    private static final int ACTION_NOT_EXISTS_IN_EX_ONLY = 1;
    private static final int ACTION_BOTH_EXISTS = 2;
    private static final int ACTION_NOT_EXISTS_IN_NL_ONLY = 3;

    @Autowired
    private DataSyncManager dataSyncManager;

    @Autowired
    private PropertiesCmmpSync propertiesCmmpSync;

    @Autowired
    private CMMPLdapQueryService exchangeLdapQueryService;

    @Autowired
    private CMMPDILdapUserService ningtonLdapUserService;

    @Autowired
    private CMMPDILdapGroupService cmmpdiLdapGroupService;

    @Override
    public void run(ApplicationArguments args) throws Exception {

        if ( args.containsOption(Constants.ARGS_FULL) ) {

            logger.log(Level.INFO, String.format("Start running full directory sync to CMMP-DI"));

            /**
             * This process is to read CMMP LDAP information (by OU), and write into same OU
             */

            for (String _ou : propertiesCmmpSync.getSourceBases() ) {

                List<CMMPExchangeLdapUser> _exUsers = exchangeLdapQueryService.getAllUserUnderOU(_ou);
                List<CMMPDILdapUser> _ningtonUsers = ningtonLdapUserService.getAllUserUnderOU(_ou);

                // Find not exists
                List<String> _onlyExistsInExSMTPList = this.findDiffInUserLists(_exUsers, _ningtonUsers, ACTION_NOT_EXISTS_IN_EX_ONLY);

                for ( CMMPExchangeLdapUser _eachUser : _exUsers ) {
                    if ( _onlyExistsInExSMTPList.contains( StringUtils.trimToEmpty( _eachUser.getMail()))) {
                        ningtonLdapUserService.createOrUpdateCMMPDIUser(_eachUser);
                    }
                }

                // Find modified
                List<String> _bothExistSMTPList = this.findDiffInUserLists(_exUsers, _ningtonUsers, ACTION_BOTH_EXISTS);

                for ( CMMPExchangeLdapUser _eachUser : _exUsers ) {
                    if ( _bothExistSMTPList.contains( StringUtils.trimToEmpty( _eachUser.getMail()))) {
                        ningtonLdapUserService.createOrUpdateCMMPDIUser(_eachUser);
                    }
                }

                // Find delete
                List<String> _onlyExistsInNLSMTPList = this.findDiffInUserLists(_exUsers, _ningtonUsers, ACTION_NOT_EXISTS_IN_EX_ONLY);

                for ( CMMPDILdapUser _eachUser : _ningtonUsers ) {
                    if ( _onlyExistsInNLSMTPList.contains( StringUtils.trimToEmpty( _eachUser.getEmail()))) {
                        ningtonLdapUserService.deleteCMMPDIUserByDN(_eachUser);
                    }
                }
            }

            for (String _ou : propertiesCmmpSync.getSourceBases() ) {

                List<CMMPExchangeLdapGroup> _exGroups = exchangeLdapQueryService.getAllGroupUnderOU(_ou);

                for ( CMMPExchangeLdapGroup _exGroup : _exGroups ) {
                    cmmpdiLdapGroupService.createOrUpdateCMMPDIGroup(_exGroup);
                }
            }
        }
    }


    private List<String> findDiffInUserLists(List<CMMPExchangeLdapUser> pExUsers, List<CMMPDILdapUser> pNingtonUsers, int pAction) {

        Set<String> _exList = new LinkedHashSet<>();
        Set<String> _ntist = new LinkedHashSet();

        List<String> _result = new ArrayList<>();

        for ( CMMPExchangeLdapUser _exUser : pExUsers ) {

            String _buffer = StringUtils.trimToEmpty(_exUser.getMail()).toLowerCase();
            if (StringUtils.isNotEmpty(_buffer)) {
                _exList.add(_buffer);
            }
        }

        for ( CMMPDILdapUser _ntUser : pNingtonUsers ) {

            String _buffer = StringUtils.trimToEmpty(_ntUser.getEmail()).toLowerCase();
            if (StringUtils.isNotEmpty(_buffer)) {
                _ntist.add(_buffer);
            }
        }

        if ( pAction == ACTION_NOT_EXISTS_IN_EX_ONLY ) {

            // 2. Find values only in ex-list but not in nt-list
            Set<String> diffSet = new LinkedHashSet<>(_exList);
            diffSet.removeAll(_ntist);

            // Convert back to List if required by subsequent method signatures
            _result = new ArrayList<>(diffSet);
        }
        else if ( pAction == ACTION_NOT_EXISTS_IN_NL_ONLY ) {

            // 2. Find values only in ex-list but not in nt-list
            Set<String> diffSet = new LinkedHashSet<>(_ntist);
            diffSet.removeAll(_exList);

            // Convert back to List if required by subsequent method signatures
            _result = new ArrayList<>(diffSet);
        }
        else if ( pAction == ACTION_BOTH_EXISTS ) {

            // 2. Find values only in ex-list but not in nt-list
            Set<String> intersectionSet = new LinkedHashSet<>(_ntist);
            intersectionSet.retainAll(_exList);

            // Convert back to List if required by subsequent method signatures
            _result = new ArrayList<>(intersectionSet);
        }

        return _result;
    }


}
