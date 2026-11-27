package com.ctg.innovic.cmmpdi.dirsync.runner;

import com.ctg.innovic.cmmpdi.dirsync.config.PropertiesCmmpSync;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapUser;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapUser;
import com.ctg.innovic.cmmpdi.dirsync.service.impl.*;
import com.ctg.innovic.cmmpdi.dirsync.utils.Constants;
import com.ctg.innovic.cmmpdi.dirsync.utils.DataSyncManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class FullSyncCMMP2CMMPDIRunner implements ApplicationRunner {

    private static Logger logger = LogManager.getLogger(FullSyncCMMP2CMMPDIRunner.class);
    private static final int ACTION_EXISTS_IN_EX_ONLY = 1;
    private static final int ACTION_BOTH_EXISTS = 2;
    private static final int ACTION_EXISTS_IN_NL_ONLY = 3;

    //private final DataSyncManager dataSyncManager;
    private final PropertiesCmmpSync propertiesCmmpSync;
    private final CMMPCacheService exchangeCacheService;
    private final CMMPLdapQueryService exchangeLdapQueryService;
    private final CMMPDILdapUserService ningtonLdapUserService;
    private final CMMPDILdapGroupService ningtonLdapGroupService;
    private final CMMPDILdapCacheService cmmpdiLdapCacheService;
    private final String cmmpdiBDOBaseOU;
    private final String cmmpBDOBaseOU;

    public FullSyncCMMP2CMMPDIRunner(
            //DataSyncManager dataSyncManager,
            PropertiesCmmpSync propertiesCmmpSync,
            CMMPCacheService exchangeCacheService,
            CMMPLdapQueryService exchangeLdapQueryService,
            CMMPDILdapUserService ningtonLdapUserService,
            CMMPDILdapGroupService ningtonLdapGroupService,
            CMMPDILdapCacheService cmmpdiLdapCacheService,
            @Value("${cmmpdi.bdo.baseOU}") String cmmpdiBDOBaseOU,
            @Value("${cmmp.bdo.baseOU}") String cmmpBDOBaseOU) {
        //this.dataSyncManager = dataSyncManager;
        this.propertiesCmmpSync = propertiesCmmpSync;
        this.exchangeCacheService = exchangeCacheService;
        this.exchangeLdapQueryService = exchangeLdapQueryService;
        this.ningtonLdapUserService = ningtonLdapUserService;
        this.ningtonLdapGroupService = ningtonLdapGroupService;
        this.cmmpdiLdapCacheService = cmmpdiLdapCacheService;
        this.cmmpdiBDOBaseOU = cmmpdiBDOBaseOU;
        this.cmmpBDOBaseOU = cmmpBDOBaseOU;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        if ( args.containsOption(Constants.SyncArg.FULL_USER.getValue()) ) {

            String _logMessage = "Start running full directory sync (user) to CMMP-DI";
            logger.log(Level.INFO, _logMessage);

            /**
             * This process is to read CMMP LDAP information (by OU), and write into same OU
             */
            cmmpdiLdapCacheService.initCMMPDIBDO();

            for (String _ou : propertiesCmmpSync.getBases() ) {

                // As base OU does not contains the User OU, so add it
                String _userOU = "OU=Users," + _ou;

                // Translate the CMMP OU into CMMP-DI as well
                String _cmmpDIUserOU = convertDnFromCMMP2CMMPDI(_userOU);

                _logMessage
                        = String.format(
                                "Scanning CMMP OU = %s and baseOU = %s, " +
                                "prepare to write to CMMPDI OU = %s and baseOU = %s",
                                _userOU, cmmpBDOBaseOU, _cmmpDIUserOU, cmmpdiBDOBaseOU);
                logger.log(Level.INFO, _logMessage);

                List<CMMPExchangeLdapUser> _exUsers = exchangeLdapQueryService.getAllUserUnderOU(_userOU);
                List<CMMPDILdapUser> _ningtonUsers = ningtonLdapUserService.getAllUserUnderParamOU(_cmmpDIUserOU);

                // Find not exists
                List<String> _onlyExistsInExSMTPList = this.findDiffInUserLists(_exUsers, _ningtonUsers, ACTION_EXISTS_IN_EX_ONLY);

                if ( logger.isDebugEnabled() ) {
                    _logMessage = String.format("%4d records found to be exists in Exchange only but not nington", _onlyExistsInExSMTPList.size());
                    logger.log(Level.DEBUG, _logMessage);
                }

                for ( CMMPExchangeLdapUser _eachUser : _exUsers ) {

                    String _userSmtp = StringUtils.trimToEmpty(_eachUser.getMail()).toLowerCase();

                    if ( _onlyExistsInExSMTPList.contains( _userSmtp ) ) {

                        _logMessage = String.format("Going to create mail contact %s under %s", _userSmtp, _cmmpDIUserOU);
                        logger.log(Level.DEBUG, _logMessage);

                        ningtonLdapUserService.createOrUpdateCMMPDIUser(_eachUser);
                    }
                }

                // Find modified
                List<String> _bothExistSMTPList = this.findDiffInUserLists(_exUsers, _ningtonUsers, ACTION_BOTH_EXISTS);

                if ( logger.isDebugEnabled() ) {
                    _logMessage = String.format("%4d records found to be exists in both Exchange and nington", _bothExistSMTPList.size());
                    logger.log(Level.DEBUG, _logMessage);
                }

                for ( CMMPExchangeLdapUser _eachUser : _exUsers ) {
                    if ( _bothExistSMTPList.contains( StringUtils.trimToEmpty( _eachUser.getMail()))) {
                        ningtonLdapUserService.createOrUpdateCMMPDIUser(_eachUser);
                    }
                }
/*
                // Find delete
                List<String> _onlyExistsInNLSMTPList = this.findDiffInUserLists(_exUsers, _ningtonUsers, ACTION_EXISTS_IN_NL_ONLY);

                for ( CMMPDILdapUser _eachUser : _ningtonUsers ) {
                    if ( _onlyExistsInNLSMTPList.contains( StringUtils.trimToEmpty( _eachUser.getEmail()))) {
                        ningtonLdapUserService.deleteCMMPDIUserByDN(_eachUser);
                    }
                }
                */
            }
        }


        if ( args.containsOption(Constants.SyncArg.FULL_GROUP.getValue()) ) {

            String _logMessage = "Start running full directory sync (group) to CMMP-DI";
            logger.log(Level.INFO, _logMessage);

            /**
             * This process is to read CMMP LDAP information (by OU), and write into same OU
             */
            cmmpdiLdapCacheService.initCMMPDIBDO();

            this.exchangeCacheService.initDN2SMTP();

            for (String _ou : propertiesCmmpSync.getBases() ) {

                // As base OU does not contains the User OU, so add it
                String _groupOU = "OU=Groups," + _ou;

                // Translate the CMMP OU into CMMP-DI as well
                String _cmmpDIGroupOU = convertDnFromCMMP2CMMPDI(_groupOU);

                _logMessage
                        = String.format(
                        "Scanning CMMP OU = %s and baseOU = %s, " +
                                "prepare to write to CMMPDI OU = %s and baseOU = %s",
                        _groupOU, cmmpBDOBaseOU, _cmmpDIGroupOU, cmmpdiBDOBaseOU);
                logger.log(Level.INFO, _logMessage);

                List<CMMPExchangeLdapGroup> _exGroups = exchangeLdapQueryService.getAllGroupUnderOU(_groupOU);
                List<CMMPDILdapGroup> _ningtonGroups = ningtonLdapGroupService.getAllGroupUnderParamOU(_cmmpDIGroupOU);

                // Find not exists
                List<String> _onlyExistsInExSMTPList = this.findDiffInGroupLists(_exGroups, _ningtonGroups, ACTION_EXISTS_IN_EX_ONLY);

                if ( logger.isDebugEnabled() ) {
                    _logMessage = String.format("%4d records found to be exists in Exchange only but not nington", _onlyExistsInExSMTPList.size());
                    logger.log(Level.DEBUG, _logMessage);
                }

                for ( CMMPExchangeLdapGroup _eachGroup : _exGroups ) {

                    String _groupSmtp = StringUtils.trimToEmpty(_eachGroup.getMail()).toLowerCase();

                    if ( _onlyExistsInExSMTPList.contains( _groupSmtp ) ) {

                        _logMessage = String.format("Going to create mail contact %s under %s", _groupSmtp, _cmmpDIGroupOU);
                        logger.log(Level.DEBUG, _logMessage);

                        this.ningtonLdapGroupService.createCMMPDIGroup(_eachGroup);
                    }
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

        if ( pAction == ACTION_EXISTS_IN_EX_ONLY ) {

            // 2. Find values only in ex-list but not in nt-list
            Set<String> diffSet = new LinkedHashSet<>(_exList);
            diffSet.removeAll(_ntist);

            // Convert back to List if required by subsequent method signatures
            _result = new ArrayList<>(diffSet);
        }
        else if ( pAction == ACTION_EXISTS_IN_NL_ONLY ) {

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


    private List<String> findDiffInGroupLists(List<CMMPExchangeLdapGroup> pExGroups, List<CMMPDILdapGroup> pNingtoGroups, int pAction) {

        Set<String> _exList = new LinkedHashSet<>();
        Set<String> _ntist = new LinkedHashSet();

        List<String> _result = new ArrayList<>();

        for ( CMMPExchangeLdapGroup _exUser : pExGroups ) {

            String _buffer = StringUtils.trimToEmpty(_exUser.getMail()).toLowerCase();
            if (StringUtils.isNotEmpty(_buffer)) {
                _exList.add(_buffer);
            }
        }

        for ( CMMPDILdapGroup _ntUser : pNingtoGroups ) {

            String _buffer = StringUtils.trimToEmpty(_ntUser.getEmail()).toLowerCase();
            if (StringUtils.isNotEmpty(_buffer)) {
                _ntist.add(_buffer);
            }
        }

        if ( pAction == ACTION_EXISTS_IN_EX_ONLY ) {

            // 2. Find values only in ex-list but not in nt-list
            Set<String> diffSet = new LinkedHashSet<>(_exList);
            diffSet.removeAll(_ntist);

            // Convert back to List if required by subsequent method signatures
            _result = new ArrayList<>(diffSet);
        }
        else if ( pAction == ACTION_EXISTS_IN_NL_ONLY ) {

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

    /**
     * Translate the source OU path from CMMP format into CMMP-DI format
     *
     * @param source
     * @return
     */
    private String convertDnFromCMMP2CMMPDI(String source) {

        String _buffer = source.replace("OU=CMMPBDOs", "OU=BDO,OU=CMMPDI-BDOs")
                            .replace("OU=Users", "OU=User")
                            .replace("OU=Groups", "OU=Group");

        return _buffer;
    }

}
