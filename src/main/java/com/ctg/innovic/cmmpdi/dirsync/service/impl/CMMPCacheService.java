package com.ctg.innovic.cmmpdi.dirsync.service.impl;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapUser;
import com.unboundid.util.NotNull;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class CMMPCacheService {

    private static Logger logger = LogManager.getLogger(CMMPCacheService.class);

    private Map<String, String> cmmpDN2SmtpMap;

    private Map<String, CMMPExchangeLdapUser> cmmpSmtp2UserMap;

    private Map<String, CMMPExchangeLdapGroup> cmmpSmtp2GroupMap;

    public CMMPCacheService() {
        this.cmmpDN2SmtpMap = new HashMap<>();
        this.cmmpSmtp2UserMap = new HashMap<>();
        this.cmmpSmtp2GroupMap = new HashMap<>();
    }

    public void addCache(@NotNull String key, @NotNull String value) {
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
