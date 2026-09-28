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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Service
public class CMMPDILdapWGDService {

    private static final Logger logger = LogManager.getLogger(CMMPDILdapWGDService.class);

    @Autowired
    private CMMPDILdapCacheService cmmpdiLdapCacheService;

    private List<CMMPDILdapUser> cmmpdiLdapUsers;

    public static void main(String[] args) {

        CMMPDILdapWGDService CMMPDILdapWGDService = new CMMPDILdapWGDService();
        CMMPDILdapWGDService.syncToWGD();
    }

    public CMMPDILdapWGDService() {
//        cmmpdiLdapUsers = new ArrayList<>();
////        cmmpdiLdapUsers = this.cmmpdiLdapCacheService.
    }


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

    public void syncWGDUser(String searchKey) {
        logger.log(Level.INFO, Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName());



        // Get current in WGD user with same smtp prefix
        // Get suppose in WGD users wit same smtp prefix
        List<CMMPDILdapUser> _bdoUsers = getWGDUserInBDOOU(searchKey);

        List<CMMPDILdapUser> _wgdUsers = getWGDUserInBDOOU(searchKey);

        List<CMMPDILdapUser> _inBDOButNotInWGD = (List<CMMPDILdapUser>) CollectionUtils.subtract(_bdoUsers, _wgdUsers);

        List<CMMPDILdapUser> _inWGDButNotInBDO = (List<CMMPDILdapUser>) CollectionUtils.subtract(_wgdUsers, _bdoUsers);

    }

    public void syncToWGD() {

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
