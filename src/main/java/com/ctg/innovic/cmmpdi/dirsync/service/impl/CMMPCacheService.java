package com.ctg.innovic.cmmpdi.dirsync.service.impl;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapUser;
import com.ctg.innovic.cmmpdi.dirsync.utils.Constants;
import com.unboundid.util.NotNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.scanner.Constant;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CMMPCacheService {

    private static final Logger logger = LogManager.getLogger(CMMPCacheService.class);

    private CMMPLdapQueryService cmmpLdapQueryService;

    private Map<String, String> cmmpDN2SmtpMap;

    private Map<String, CMMPExchangeLdapUser> cmmpSmtp2UserMap;

    private Map<String, CMMPExchangeLdapGroup> cmmpSmtp2GroupMap;


    public CMMPCacheService(CMMPLdapQueryService cmmpLdapQueryService) {
        this.cmmpDN2SmtpMap = new HashMap<>();
        this.cmmpSmtp2UserMap = new HashMap<>();
        this.cmmpSmtp2GroupMap = new HashMap<>();
        this.cmmpLdapQueryService = cmmpLdapQueryService;
    }

    public void initDN2SMTP() {

        logger.debug("Initialising CMMP BDO groups...");
        List<CMMPExchangeLdapGroup> _groups = this.cmmpLdapQueryService.getAllGroupUnderOU(Constants.CMMP_BASEOU);

        for ( CMMPExchangeLdapGroup _group : _groups ) {
            if ( _group.getDn() != null ) {
                String _dn = StringUtils.trimToNull(_group.getDn().toString());
                String _smtp = StringUtils.trimToNull(_group.getMail());

                if ( _dn != null && _smtp != null ) {
                    this.addCache(_dn, _smtp);
                }
            }
        }
        logger.info("{} BDO group entries have been initialised.", _groups.size());

        logger.debug("Initialising CMMP BDO users...");
        List<CMMPExchangeLdapUser> _users = this.cmmpLdapQueryService.getAllUserUnderOU(Constants.CMMP_BASEOU);

        for ( CMMPExchangeLdapUser _user : _users ) {
            if ( _user.getDn() != null ) {
                String _dn = StringUtils.trimToNull(_user.getDn().toString());
                String _smtp = StringUtils.trimToNull(_user.getMail());

                if ( _dn != null && _smtp != null ) {
                    this.addCache(_dn, _smtp);
                }
            }
        }
        logger.info("{} BDO user entries have been initialised.", _users.size());
    }

    private void addCache(@NotNull String key, @NotNull String value) {
        this.cmmpDN2SmtpMap.put(key, value);
    }

    public String lookupSmtpByCMMPDN(@NotNull String dn) {
        return this.cmmpDN2SmtpMap.get(dn);
    }

    public void addUserCache(@NotNull CMMPExchangeLdapUser user) {
        if ( user != null && user.getMail() != null ) {
            cmmpSmtp2UserMap.put(user.getMail().toLowerCase(), user);
        }
    }

    public CMMPExchangeLdapUser getUserFromCacheBySMTP(@NotNull String smtp) {
        return this.cmmpSmtp2UserMap.get(smtp.toLowerCase());
    }

    public void addGroupCache(@NotNull CMMPExchangeLdapGroup group) {
        if ( group != null && group.getMail() != null ) {
            cmmpSmtp2GroupMap.put(group.getMail().toLowerCase(), group);
        }
    }

    public CMMPExchangeLdapGroup getGroupFromCacheBySMTP(@NotNull String smtp) {
        return this.cmmpSmtp2GroupMap.get(smtp.toLowerCase());
    }

    protected Map<String, CMMPExchangeLdapUser> getCmmpSmtp2UserMap() {
        return cmmpSmtp2UserMap;
    }

    protected Map<String, CMMPExchangeLdapGroup> getCmmpSmtp2GroupMap() {
        return cmmpSmtp2GroupMap;
    }
}
