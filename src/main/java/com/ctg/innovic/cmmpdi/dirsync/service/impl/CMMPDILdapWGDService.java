package com.ctg.innovic.cmmpdi.dirsync.service.impl;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapUser;
import com.ctg.innovic.cmmpdi.dirsync.utils.Constants;
import com.ctg.innovic.cmmpdi.dirsync.utils.LogUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.naming.InvalidNameException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class CMMPDILdapWGDService {

    private static final Logger logger = LogManager.getLogger(CMMPDILdapWGDService.class);

    private final boolean executionDryrun;
    private final CMMPDILdapCacheService cmmpdiLdapCacheService;
    private final CMMPDILdapUserService cmmpdiLdapUserService;

    public CMMPDILdapWGDService(
            @Value("${execution.dryrun:false}") boolean executionDryrun,
            CMMPDILdapCacheService cmmpdiLdapCacheService,
            CMMPDILdapUserService cmmpdiLdapUserService) {
        this.executionDryrun = executionDryrun;
        this.cmmpdiLdapCacheService = cmmpdiLdapCacheService;
        this.cmmpdiLdapUserService = cmmpdiLdapUserService;
    }




    /**
     * Fetch WGD users which smtp address matches searchKey
     * @param searchKey
     * @return
     */
    private List<CMMPDILdapUser> getWGDUserInWGDOU(@NotNull String searchKey) {

        List<CMMPDILdapUser> _result = new ArrayList<>();

        String _lowerCaseSearchKey = searchKey.toLowerCase(Locale.ROOT);

        for ( CMMPDILdapUser _user : this.cmmpdiLdapCacheService.getWGDUsersFromWGDFromCache() ) {

            String _email = StringUtils.trimToEmpty(_user.getEmail());

            if ( _email.toLowerCase().startsWith(_lowerCaseSearchKey) ) {
                _result.add(_user);
            }
        }

        return _result;
    }


    /**
     * Fetch WGD users which smtp address matches searchKey
     * @param searchKey
     * @return
     */
    private List<CMMPDILdapGroup> getWGDGroupInWGDOU(@NotNull String searchKey) {

        List<CMMPDILdapGroup> _result = new ArrayList<>();

        String _lowerCaseSearchKey = searchKey.toLowerCase(Locale.ROOT);

        for ( CMMPDILdapGroup _group : this.cmmpdiLdapCacheService.getWGDGroupsFromWGDFromCache() ) {

            String _email = StringUtils.trimToEmpty(_group.getEmail());

            if ( _email.toLowerCase().startsWith(_lowerCaseSearchKey) ) {
                _result.add(_group);
            }
        }

        return _result;
    }


    /**
     * Fetch BDO users (whcih should be in WGD) which smtp address matches searchKey
     * @param searchKey
     * @return
     */
    private List<CMMPDILdapUser> getWGDUserInBDOOU(@NotNull String searchKey) {

        List<CMMPDILdapUser> _result = new ArrayList<>();

        String _lowerCaseSearchKey = searchKey.toLowerCase(Locale.ROOT);

        for ( CMMPDILdapUser _user : this.cmmpdiLdapCacheService.getAllBDOUsersFromBDOFromCache() ) {

            String _email = StringUtils.trimToEmpty(_user.getEmail());

            if ( _email.toLowerCase().startsWith( _lowerCaseSearchKey ) ) {
                // Only populated to WGD if marked to be so, and not hidden from address book
                if ( "1".equals(_user.getExtensionAttribute7()) && !_user.isHiddenFromAddressLists()) {
                    _result.add(_user);
                }
            }
        }

        return _result;
    }


    private List<CMMPDILdapGroup> getWGDGroupInBDOOU(@NotNull String searchKey) {

        List<CMMPDILdapGroup> _result = new ArrayList<>();

        String _lowerCaseSearchKey = searchKey.toLowerCase(Locale.ROOT);

        for ( CMMPDILdapGroup _group : this.cmmpdiLdapCacheService.getAllBDOGroupsFromBDOFromCache() ) {

            String _email = StringUtils.trimToEmpty(_group.getEmail());

            if ( _email.toLowerCase().startsWith( _lowerCaseSearchKey ) ) {
                // Only populated to WGD if marked to be so, and not hidden from address book
                if ( "1".equals(_group.getExtensionAttribute7()) && !_group.isHiddenFromAddressLists()) {
                    _result.add(_group);
                }
            }
        }

        return _result;
    }


    protected void syncWGDUserBySearchKey(String pSearchKey) throws InvalidNameException {

        String _logMessage = Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName();
        logger.log(Level.TRACE, _logMessage);

        List<CMMPDILdapUser> _bdoUsers = getWGDUserInBDOOU(pSearchKey);

        List<CMMPDILdapUser> _wgdUsers = getWGDUserInWGDOU(pSearchKey);

        List<CMMPDILdapUser> _inWGDButNotInBDO = (List<CMMPDILdapUser>) CollectionUtils.subtract(_wgdUsers, _bdoUsers);

        for ( CMMPDILdapUser wgdUser : _inWGDButNotInBDO ){
            _logMessage = wgdUser.getEmail() + " could not be found in BDO OUs, removing WGD user by DN " + wgdUser.getDn();
            logger.log(Level.INFO, _logMessage);
            this.cmmpdiLdapUserService.deleteCMMPDIUserByDN(wgdUser);
        }

        List<CMMPDILdapUser> _inBDOButNotInWGD = (List<CMMPDILdapUser>) CollectionUtils.subtract(_bdoUsers, _wgdUsers);

        for ( CMMPDILdapUser bdoUser : _inBDOButNotInWGD ){
            _logMessage = bdoUser.getEmail() + " could not be found in WGD OUs, adding BDO user into WGD";
            logger.log(Level.INFO, _logMessage);
            this.cmmpdiLdapUserService.createWGDCMMPDIUser(bdoUser);
        }

        List<CMMPDILdapUser> unionUserList = new ArrayList<>(CollectionUtils.intersection(_bdoUsers, _wgdUsers));

        for ( CMMPDILdapUser bdoUser : unionUserList ){
            _logMessage = bdoUser.getEmail() + " found in both BDO and WGD OU, updating from BDO";
            logger.log(Level.INFO, _logMessage);
            //this.cmmpdiLdapUserService.createWGDCMMPDIUser(bdoUser);
        }
    }


    protected void syncWGDGroupBySearchKey(String pSearchKey) throws InvalidNameException {

        logEntering();

        List<CMMPDILdapGroup> _bdoGroups = getWGDGroupInBDOOU(pSearchKey);

        List<CMMPDILdapGroup> _wgdGroups = getWGDGroupInWGDOU(pSearchKey);

        List<CMMPDILdapGroup> _inWGDButNotInBDO = (List<CMMPDILdapGroup>) CollectionUtils.subtract(_wgdGroups, _bdoGroups);

        for ( CMMPDILdapGroup wgdGroup : _inWGDButNotInBDO ){
            String _logMessage = wgdGroup.getEmail() + " could not be found in BDO OUs, removing WGD user by DN " + wgdGroup.getDn();
            logger.log(Level.INFO, _logMessage);
            //this.cmmpdiLdapUserService.deleteCMMPDIUserByDN(wgdUser);
        }

        List<CMMPDILdapGroup> _inBDOButNotInWGD = (List<CMMPDILdapGroup>) CollectionUtils.subtract(_bdoGroups, _wgdGroups);

        for ( CMMPDILdapGroup bdoUser : _inBDOButNotInWGD ){
            String _logMessage = bdoUser.getEmail() + " could not be found in WGD OUs, adding BDO user into WGD";
            logger.log(Level.INFO, _logMessage);
            //this.cmmpdiLdapUserService.createWGDCMMPDIUser(bdoUser);
        }
    }


    public void syncWGDGroups() throws InvalidNameException {
        syncWGDObjects(Constants.WGD_OPERATION_GROUP);
    }


    public void syncWGDUsers() throws InvalidNameException {
        syncWGDObjects(Constants.WGD_OPERATION_USER);
    }


    private void syncWGDObjects(int mode) throws InvalidNameException {

        int length = 2;
        char[] chars = new char[length];
        Arrays.fill(chars, 'a');

        while (true) {
            String searchKey = new String(chars);

            String _log = String.format("Handling searchKey %s", searchKey);
            logger.log(Level.TRACE, _log);

            if ( Constants.WGD_OPERATION_USER == mode) {
                syncWGDUserBySearchKey(searchKey);
            }
            else if ( Constants.WGD_OPERATION_GROUP == mode) {
                syncWGDGroupBySearchKey(searchKey);
            }

            // Increment characters from right to left (like an odometer)
            int index = length - 1;
            while (index >= 0 && chars[index] == 'z') {
                chars[index] = 'a';
                index--;
            }

            // Reached "zzz" and wrapped around to the beginning
            if (index < 0) {
                break;
            }

            chars[index]++;
        }
    }

    private void logEntering() {
        logger.log(Level.TRACE, Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName());
    }
}
