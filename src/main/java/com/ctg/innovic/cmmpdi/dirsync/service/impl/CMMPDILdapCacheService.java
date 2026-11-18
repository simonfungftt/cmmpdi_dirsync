package com.ctg.innovic.cmmpdi.dirsync.service.impl;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapUser;
import com.ctg.innovic.cmmpdi.dirsync.utils.Constants;
import com.ctg.innovic.cmmpdi.dirsync.utils.LogUtils;
import com.unboundid.util.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CMMPDILdapCacheService {

    private static Logger logger = LogManager.getLogger(CMMPDILdapCacheService.class);

    @Autowired
    private CMMPDILdapQueryService ldapQueryService;

    private Map<String, CMMPDILdapUser> cmmpdiUserSMTP2DnMap;
    private Map<String, CMMPDILdapGroup> cmmpdiGroupSMTP2DnMap;

    private Map<String, CMMPDILdapUser> cmmpdiWGDUserSMTP2DnMap;
    private Map<String, CMMPDILdapGroup> cmmpdiWGDGroupSMTP2DnMap;

    private Map<String, String> cmmpUserDn2SMTPMap;
    private Map<String, String> cmmpGroupDn2SMTPMap;

    public CMMPDILdapCacheService() {
        this.cmmpdiUserSMTP2DnMap = new HashMap<>();
        this.cmmpdiGroupSMTP2DnMap = new HashMap<>();

        this.cmmpdiWGDUserSMTP2DnMap = new HashMap<>();
        this.cmmpdiWGDGroupSMTP2DnMap = new HashMap<>();

        this.cmmpUserDn2SMTPMap = new HashMap<>();
        this.cmmpGroupDn2SMTPMap = new HashMap<>();
    }

    /**
     * Initialise CMMP-DI BDO users and group into a map structure (smtp, object)
     */
    public void initCMMPDIBDO() {

        String _logMessage = Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName();
        logger.log(Level.TRACE, _logMessage);

        this.cmmpdiUserSMTP2DnMap.clear();
        this.cmmpdiGroupSMTP2DnMap.clear();

        // List all users from CMMP-DI BDO OUs users
        _logMessage = "Initialising CMMP-DI BDO users...";
        logger.log(Level.DEBUG, _logMessage );

        List<CMMPDILdapUser> _users = this.ldapQueryService.listCMMPDIUsers();

        // For each of the users, set up a map, by SMTP address to the user object
        for ( CMMPDILdapUser _user : _users ) {
            if ( _user.getEmail() != null ) {
                cmmpdiUserSMTP2DnMap.put(_user.getEmail().toLowerCase(), _user);
            }
        }

        _logMessage = this.cmmpdiUserSMTP2DnMap.size()+ " user entries had been initialised...";
        logger.log(Level.INFO, _logMessage);


        // List all users from CMMP-DI BDO OUs groups
        _logMessage = "Initialising CMMP-DI BDO groups...";
        logger.log(Level.DEBUG, _logMessage );

        List<CMMPDILdapGroup> _groups = this.ldapQueryService.listCMMPDIGroups();

        for ( CMMPDILdapGroup _group : _groups ) {
            if ( _group.getEmail() != null ) {
                cmmpdiGroupSMTP2DnMap.put(_group.getEmail().toLowerCase(), _group);
            }
        }
        _logMessage = this.cmmpdiGroupSMTP2DnMap.size()+ " group entries had been initialised...";
        logger.log(Level.INFO, _logMessage);
    }


    /**
     * Initialise CMMP-DI WGD users and group into a map structure (smtp, object)
     */
    public void initCMMPDIWGD() {

        String _logMessage = Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName();
        logger.log(Level.TRACE, _logMessage);

        this.cmmpdiWGDUserSMTP2DnMap.clear();
        this.cmmpdiWGDGroupSMTP2DnMap.clear();

        // List all users from CMMP-DI WGD OUs users
        _logMessage = "Initialising CMMP-DI WGD users...";
        logger.log(Level.DEBUG, _logMessage );

        List<CMMPDILdapUser> _users = this.ldapQueryService.listCMMPDIWGDUsers();

        for ( CMMPDILdapUser _user : _users ) {
            if ( _user.getEmail() != null ) {
                cmmpdiWGDUserSMTP2DnMap.put(_user.getEmail().toLowerCase(), _user);
            }

            String _buffer
                    = String.format("WGD User SMTP address = %s, whenCreated = %s and whenUpdated = %s", _user.getEmail(),
                    _user.getWhenCreated(),
                    _user.getWhenChanged()
            );
            //logger.log(Level.DEBUG, _buffer);
        }

        _logMessage = this.cmmpdiWGDUserSMTP2DnMap.size()+ " WGD user entries had been initialised...";
        logger.log(Level.INFO, _logMessage);


        // List all users from CMMP-DI WGD OUs groups
        _logMessage = "Initialising CMMP-DI WGD groups...";
        logger.log(Level.DEBUG, _logMessage );

        List<CMMPDILdapGroup> _groups = this.ldapQueryService.listCMMPDIWGDGroups();

        for ( CMMPDILdapGroup _group : _groups ) {
            if ( _group.getEmail() != null ) {
                cmmpdiWGDGroupSMTP2DnMap.put(_group.getEmail().toLowerCase(), _group);
            }
        }
        _logMessage = this.cmmpdiWGDGroupSMTP2DnMap.size()+ " WGD group entries had been initialised...";
        logger.log(Level.INFO, _logMessage);
    }


    public void removeGroupCache(CMMPDILdapGroup pCMMPDILdapGroup) {

        if ( pCMMPDILdapGroup != null ) {
            this.cmmpdiGroupSMTP2DnMap.remove(pCMMPDILdapGroup.getEmail());
        }
    }

    public void addGroupCache(String smtp) {

        List<CMMPDILdapGroup> _groups = this.ldapQueryService.findGroupByEmail(smtp);

        if ( _groups != null && _groups.isEmpty() == false) {
            this.addGroupCache(_groups.get(0));
        }
    }

    public void addGroupCache(CMMPDILdapGroup pCMMPDILdapGroup) {

        if ( pCMMPDILdapGroup != null ) {
            this.cmmpdiGroupSMTP2DnMap.put(pCMMPDILdapGroup.getEmail(), pCMMPDILdapGroup);
        }
    }

    public void removeUserCache(CMMPDILdapUser pCMMPDILdapUser) {

        if ( pCMMPDILdapUser != null ) {
            this.cmmpdiUserSMTP2DnMap.remove(pCMMPDILdapUser.getEmail());
        }
    }

    public void addUserCache(String smtp) {

        List<CMMPDILdapUser> _users = this.ldapQueryService.findUsersByEmail(smtp);

        if ( _users != null && _users.isEmpty() == false) {
            this.addUserCache(_users.get(0));
        }
    }

    public void addUserCache(CMMPDILdapUser pCMMPDILdapUser) {

        if ( pCMMPDILdapUser != null ) {
            this.cmmpdiUserSMTP2DnMap.put(pCMMPDILdapUser.getEmail().toLowerCase(), pCMMPDILdapUser);
        }
    }



    public CMMPDILdapGroup getCMMPDIGroupDnBySMTP(@NotNull String pSmtp) {

        if ( StringUtils.trimToNull(pSmtp) != null ) {

            if ( this.cmmpdiGroupSMTP2DnMap.containsKey(pSmtp.toLowerCase()) ) {
                return this.cmmpdiGroupSMTP2DnMap.get(pSmtp.toLowerCase());
            }
        }

        return null;
    }


    public CMMPDILdapUser getCMMPDIUserDnBySMTP(@NotNull String pSmtp) {

        if ( StringUtils.trimToNull(pSmtp) != null ) {

            if ( this.cmmpdiUserSMTP2DnMap.containsKey(pSmtp.toLowerCase()) ) {
                return this.cmmpdiUserSMTP2DnMap.get(pSmtp.toLowerCase());
            }
        }

        return null;
    }


    public List<CMMPDILdapUser> getAllBDOUsersFromBDOFromCache() {

        return new ArrayList<>(this.cmmpdiUserSMTP2DnMap.values());
    }

    public List<CMMPDILdapUser> getWGDUsersFromWGDFromCache() {

        return new ArrayList<>(this.cmmpdiWGDUserSMTP2DnMap.values());
    }

}
