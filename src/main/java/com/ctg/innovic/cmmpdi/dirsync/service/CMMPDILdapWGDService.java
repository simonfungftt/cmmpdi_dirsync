package com.ctg.innovic.cmmpdi.dirsync.service;

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
import org.springframework.stereotype.Service;

import javax.naming.InvalidNameException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class CMMPDILdapWGDService {

    private final static Logger logger = LogManager.getLogger(CMMPDILdapWGDService.class);

    @Autowired
    private CMMPDILdapCacheService cmmpdiLdapCacheService;

    @Autowired
    private CMMPDILdapUserService cmmpdiLdapUserService;


    private List<CMMPDILdapUser> getWGDUserInWGDOU(@NotNull String searchKey) {

        List<CMMPDILdapUser> _result = new ArrayList<>();

        for ( CMMPDILdapUser _user : this.cmmpdiLdapCacheService.getWGDUsersFromWGD() ) {

            String _email = StringUtils.trimToEmpty(_user.getEmail());

            if ( _email.toLowerCase().startsWith(searchKey.toLowerCase() ) ) {
                _result.add(_user);
            }
        }

        return _result;
    }


    private List<CMMPDILdapUser> getWGDUserInBDOOU(@NotNull String searchKey) {

        List<CMMPDILdapUser> _result = new ArrayList<>();

        for ( CMMPDILdapUser _user : this.cmmpdiLdapCacheService.getAllBDOUsersFromBDO() ) {

            String _email = StringUtils.trimToEmpty(_user.getEmail());

            if ( _email.toLowerCase().startsWith(searchKey.toLowerCase() ) ) {

                if ( _user.getExtensionAttribute7() == 1 ) {
                    _result.add(_user);
                }
            }
        }

        return _result;
    }


    protected void syncWGDUser(String pSearchKey) throws InvalidNameException {

        logger.log(Level.TRACE, Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName());

        List<CMMPDILdapUser> _bdoUsers = getWGDUserInBDOOU(pSearchKey);

        List<CMMPDILdapUser> _wgdUsers = getWGDUserInWGDOU(pSearchKey);

        List<CMMPDILdapUser> _inWGDButNotInBDO = (List<CMMPDILdapUser>) CollectionUtils.subtract(_wgdUsers, _bdoUsers);

        for ( CMMPDILdapUser wgdUser : _inWGDButNotInBDO ){
            this.cmmpdiLdapUserService.deleteCMMPDIUser(wgdUser);
        }

        List<CMMPDILdapUser> _inBDOButNotInWGD = (List<CMMPDILdapUser>) CollectionUtils.subtract(_bdoUsers, _wgdUsers);

        for ( CMMPDILdapUser bdoUser : _inBDOButNotInWGD ){
            this.cmmpdiLdapUserService.createWGDCMMPDIUser(bdoUser);
        }
    }


    public void syncWGDGroups() throws InvalidNameException {

        int length = 3;
        char[] chars = new char[length];
        Arrays.fill(chars, 'a');

        while (true) {
            String searchKey = new String(chars);
            //syncWGDUser(searchKey);

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

    public void syncWGDUsers() throws InvalidNameException {

        int length = 3;
        char[] chars = new char[length];
        Arrays.fill(chars, 'a');

        while (true) {
            String searchKey = new String(chars);
            syncWGDUser(searchKey);

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
}
