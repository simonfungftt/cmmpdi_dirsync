package com.ctg.innovic.cmmpdi.dirsync.service;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapUser;
import com.ctg.innovic.cmmpdi.dirsync.utils.LogUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    private Map<String, String> cmmpUserDn2SMTPMap;
    private Map<String, String> cmmpGroupDn2SMTPMap;

    public CMMPDILdapCacheService() {
        this.cmmpdiUserSMTP2DnMap = new HashMap<>();
        this.cmmpdiGroupSMTP2DnMap = new HashMap<>();

        this.cmmpUserDn2SMTPMap = new HashMap<>();
        this.cmmpGroupDn2SMTPMap = new HashMap<>();
    }


    public void initCMMP() {

        logger.debug("Entering " + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );

        logger.info("Initialising CMMP users, loading DN and SMTP mapping into memory...");
    }


    public void initCMMPDI() {

        logger.debug("Entering " + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );

        logger.info("Initialising CMMP-DI users...");

        this.cmmpUserDn2SMTPMap.clear();
        this.cmmpGroupDn2SMTPMap.clear();

        List<CMMPDILdapUser> _users = this.ldapQueryService.listCMMPDIUsers();

        for ( CMMPDILdapUser _user : _users ) {
            if ( _user.getEmail() != null ) {
                cmmpdiUserSMTP2DnMap.put(_user.getEmail().toLowerCase(), _user);
            }
        }

        logger.info(this.cmmpdiUserSMTP2DnMap.size()+ " user entries had been initialised...");

        logger.info("Initialising CMMP-DI groups...");

        List<CMMPDILdapGroup> _groups = this.ldapQueryService.listCMMPDIGroups();

        for ( CMMPDILdapGroup _group : _groups ) {
            if ( _group.getEmail() != null ) {
                cmmpdiGroupSMTP2DnMap.put(_group.getEmail().toLowerCase(), _group);
            }
        }

        logger.info(this.cmmpdiGroupSMTP2DnMap.size() + " group entries had been initialised...");
    }

/*
    public String getCMMPDIDnBySMTP(String pSmtp) {

        if ( StringUtils.trimToNull(pSmtp) != null ) {

            CMMPDILdapUser _user = getCMMPDIUserDnBySMTP(pSmtp.toLowerCase());

            if ( _user != null ) {
                return _user.getDn();
            }

            CMMPDILdapGroup _group = getCMMPDIGroupDnBySMTP(pSmtp.toLowerCase());

            if ( _group != null ) {
                return _user.getDn();
            }
        }

        return null;
    }
*/

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
            this.cmmpdiUserSMTP2DnMap.put(pCMMPDILdapUser.getEmail(), pCMMPDILdapUser);
        }
    }



    public CMMPDILdapGroup getCMMPDIGroupDnBySMTP(String pSmtp) {

        if ( StringUtils.trimToNull(pSmtp) != null ) {

            if ( this.cmmpdiGroupSMTP2DnMap.containsKey(pSmtp.toLowerCase()) ) {
                return this.cmmpdiGroupSMTP2DnMap.get(pSmtp.toLowerCase());
            }
        }

        return null;
    }


    public CMMPDILdapUser getCMMPDIUserDnBySMTP(String pSmtp) {

        if ( StringUtils.trimToNull(pSmtp) != null ) {

            if ( this.cmmpdiUserSMTP2DnMap.containsKey(pSmtp.toLowerCase()) ) {
                return this.cmmpdiUserSMTP2DnMap.get(pSmtp.toLowerCase());
            }
        }

        return null;
    }

/*
    public String translateCMMPDnIntoSMTP(String dn) {

        logger.debug("Entering " + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );
        logger.debug("Going to translate '" + dn + "' into SMTP address");

        if ( StringUtils.trimToNull(dn) != null ) {

            if ( this.cmmpGroupDn2SMTPMap.containsKey(dn) ) {
                return this.cmmpGroupDn2SMTPMap.get(dn);
            }

            if ( this.cmmpUserDn2SMTPMap.containsKey(dn) ) {
                return this.cmmpUserDn2SMTPMap.get(dn);
            }
        }

        return null;
    }
*/
}
