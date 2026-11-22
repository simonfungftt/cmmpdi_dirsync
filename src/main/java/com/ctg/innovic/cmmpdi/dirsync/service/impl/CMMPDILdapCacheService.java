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
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * In-memory lookup cache mapping lowercase SMTP email addresses to CMMP-DI BDO and WGD
 * LDAP user and group DTOs to minimize repetitive LDAP round-trips during directory sync.
 */
@Service
public class CMMPDILdapCacheService {

    private static final Logger logger = LogManager.getLogger(CMMPDILdapCacheService.class);

    private final CMMPDILdapQueryService ldapQueryService;

    private final Map<String, CMMPDILdapUser> cmmpdiUserSMTP2DnMap;
    private final Map<String, CMMPDILdapGroup> cmmpdiGroupSMTP2DnMap;

    private final Map<String, CMMPDILdapUser> cmmpdiWGDUserSMTP2DnMap;
    private final Map<String, CMMPDILdapGroup> cmmpdiWGDGroupSMTP2DnMap;


    public CMMPDILdapCacheService(CMMPDILdapQueryService ldapQueryService) {
        this.ldapQueryService = ldapQueryService;

        cmmpdiUserSMTP2DnMap = new ConcurrentHashMap<>();
        cmmpdiGroupSMTP2DnMap = new ConcurrentHashMap<>();
        cmmpdiWGDUserSMTP2DnMap = new ConcurrentHashMap<>();
        cmmpdiWGDGroupSMTP2DnMap = new ConcurrentHashMap<>();
    }

    // -------------------------------------------------------------------------
    // Cache Initialization
    // -------------------------------------------------------------------------

    /**
     * Reloads all CMMP-DI BDO users and groups from LDAP into the in-memory SMTP lookup maps.
     */
    public void initCMMPDIBDO() {
        logEntering();

        logger.debug("Initialising CMMP-DI BDO users...");
        populateCache(cmmpdiUserSMTP2DnMap, ldapQueryService.listCMMPDIUsers(), CMMPDILdapUser::getEmail);
        logger.info("{} BDO user entries have been initialised.", cmmpdiUserSMTP2DnMap.size());

        logger.debug("Initialising CMMP-DI BDO groups...");
        populateCache(cmmpdiGroupSMTP2DnMap, ldapQueryService.listCMMPDIGroups(), CMMPDILdapGroup::getEmail);
        logger.info("{} BDO group entries have been initialised.", cmmpdiGroupSMTP2DnMap.size());
    }

    /**
     * Reloads all CMMP-DI WGD users and groups from LDAP into the in-memory SMTP lookup maps.
     */
    public void initCMMPDIWGD() {
        logEntering();

        logger.debug("Initialising CMMP-DI WGD users...");
        List<CMMPDILdapUser> wgdUsers = ldapQueryService.listCMMPDIWGDUsers();
        populateCache(cmmpdiWGDUserSMTP2DnMap, wgdUsers, CMMPDILdapUser::getEmail);

        if (logger.isDebugEnabled() && wgdUsers != null) {
            for (CMMPDILdapUser user : wgdUsers) {
                logger.debug("WGD User SMTP = {}, whenCreated = {}, whenChanged = {}",
                        user.getEmail(), user.getWhenCreated(), user.getWhenChanged());
            }
        }
        logger.info("{} WGD user entries have been initialised.", cmmpdiWGDUserSMTP2DnMap.size());

        logger.debug("Initialising CMMP-DI WGD groups...");
        populateCache(cmmpdiWGDGroupSMTP2DnMap, ldapQueryService.listCMMPDIWGDGroups(), CMMPDILdapGroup::getEmail);
        logger.info("{} WGD group entries have been initialised.", cmmpdiWGDGroupSMTP2DnMap.size());
    }

    // -------------------------------------------------------------------------
    // Cache Mutations
    // -------------------------------------------------------------------------

    public void addUserCache(String smtp) {
        String key = normalizeEmail(smtp);
        if (key == null) {
            return;
        }
        List<CMMPDILdapUser> users = ldapQueryService.findUsersByEmail(key);
        if (CollectionUtils.isNotEmpty(users)) {
            addUserCache(users.get(0));
        }
    }

    public void addUserCache(CMMPDILdapUser user) {
        if (user == null) {
            return;
        }
        String key = normalizeEmail(user.getEmail());
        if (key != null) {
            cmmpdiUserSMTP2DnMap.put(key, user);
        }
    }

    public void removeUserCache(CMMPDILdapUser user) {
        if (user == null) {
            return;
        }
        String key = normalizeEmail(user.getEmail());
        if (key != null) {
            cmmpdiUserSMTP2DnMap.remove(key);
        }
    }

    public void removeGroupCache(CMMPDILdapGroup group) {
        if (group == null) {
            return;
        }
        String key = normalizeEmail(group.getEmail());
        if (key != null) {
            cmmpdiGroupSMTP2DnMap.remove(key);
        }
    }

    // -------------------------------------------------------------------------
    // Cache Lookups
    // -------------------------------------------------------------------------

    public CMMPDILdapUser getCMMPDIUserDnBySMTP(@NotNull String smtp) {
        String key = normalizeEmail(smtp);
        return key != null ? cmmpdiUserSMTP2DnMap.get(key) : null;
    }

    public CMMPDILdapGroup getCMMPDIGroupDnBySMTP(@NotNull String smtp) {
        String key = normalizeEmail(smtp);
        return key != null ? cmmpdiGroupSMTP2DnMap.get(key) : null;
    }

    public List<CMMPDILdapUser> getAllBDOUsersFromBDOFromCache() {
        return new ArrayList<>(cmmpdiUserSMTP2DnMap.values());
    }

    public List<CMMPDILdapGroup> getAllBDOGroupsFromBDOFromCache() {
        return new ArrayList<>(cmmpdiGroupSMTP2DnMap.values());
    }

    public List<CMMPDILdapUser> getWGDUsersFromWGDFromCache() {
        return new ArrayList<>(cmmpdiWGDUserSMTP2DnMap.values());
    }

    public List<CMMPDILdapGroup> getWGDGroupsFromWGDFromCache() {
        return new ArrayList<>(this.cmmpdiWGDGroupSMTP2DnMap.values());
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private <T> void populateCache(Map<String, T> targetMap, List<T> entries, Function<T, String> emailExtractor) {
        targetMap.clear();
        if (entries == null) {
            return;
        }
        for (T entry : entries) {
            String key = normalizeEmail(emailExtractor.apply(entry));
            if (key != null) {
                targetMap.put(key, entry);
            }
        }
    }

    private static String normalizeEmail(String email) {
        String trimmed = StringUtils.trimToNull(email);
        return trimmed != null ? trimmed.toLowerCase() : null;
    }

    private void logEntering() {
        logger.log(Level.TRACE, Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName());
    }
}