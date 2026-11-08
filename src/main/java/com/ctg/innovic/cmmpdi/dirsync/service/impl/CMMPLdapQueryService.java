package com.ctg.innovic.cmmpdi.dirsync.service.impl;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapUser;
import com.ctg.innovic.cmmpdi.dirsync.service.CMMPLdapService;
import com.ctg.innovic.cmmpdi.dirsync.utils.Constants;
import com.ctg.innovic.cmmpdi.dirsync.utils.DataSyncManager;
import com.ctg.innovic.cmmpdi.dirsync.utils.LogUtils;
import com.unboundid.util.NotNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class CMMPLdapQueryService implements CMMPLdapService {

    private final static Logger logger = LogManager.getLogger(CMMPLdapQueryService.class);

    @Autowired
    @Qualifier("cmmpLdapTemplate")
    private LdapTemplate ldapTemplate;

    @Autowired
    private DataSyncManager dataSyncManager;


    public CMMPExchangeLdapGroup dipGroupBySMTP(@NotNull String pSMTP) {

        logger.log(Level.TRACE, Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName());

        CMMPExchangeLdapGroup _group = null;

        logger.log(Level.INFO, "Trying to fetch CMMP group where mail = %s", pSMTP);

        LdapQuery _query = LdapQueryBuilder.query()
                .base("OU=CMMPBDOs")
                .where(Constants.LDAP_FIELD_OBJECT_CLASS).is("group").and("mail").is(pSMTP);

        List<CMMPExchangeLdapGroup> _result = ldapTemplate.find(_query, CMMPExchangeLdapGroup.class);

        if ( !CollectionUtils.isEmpty(_result) ) {
            _group = _result.get(0);
        }

        return _group;
    }


    public CMMPExchangeLdapUser dipUserBySMTP(@NotNull String pSMTP) {

        logger.log(Level.TRACE, Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName());

        CMMPExchangeLdapUser _user = null;

        logger.log(Level.INFO, "Trying to fetch CMMP user where mail = %s", pSMTP);

        LdapQuery _query = LdapQueryBuilder.query()
                .base("OU=CMMPBDOs")
                .where(Constants.LDAP_FIELD_OBJECT_CLASS).is("user").and("mail").is(pSMTP);

        logger.log(Level.DEBUG, String.format(Constants.LOGGER_LDAPQUERY_DEBUG, _query.base(), _query.filter()));

        List<CMMPExchangeLdapUser> _result = ldapTemplate.find(_query, CMMPExchangeLdapUser.class);

        if ( !CollectionUtils.isEmpty(_result) ) {
            _user = _result.get(0);
        }

        return _user;
    }


    public List<CMMPExchangeLdapUser> getAllUserUnderOU(@NotNull String pOU) {

        logger.log(Level.TRACE, Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName());

        return this.getLastModifiedUserUnderOU(pOU, Instant.EPOCH);
    }


    /**
     * Get all user objects under the given OU and modified since the given timestamp
     *
     * @param pOU
     * @param pLastSyncTime
     * @return
     */
    public List<CMMPExchangeLdapUser> getLastModifiedUserUnderOU(@NotNull String pOU, @NotNull Instant pLastSyncTime) {

        logger.log(Level.TRACE, Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName());

        // Define your target timestamp
        String formattedTime = dataSyncManager.getLogFormat(pLastSyncTime);

        logger.log(Level.INFO, "Trying to fetch CMMP users under OU %s and modified time after %s", pOU, formattedTime);

        // TODO: change this OU
        LdapQuery _query = LdapQueryBuilder.query()
                .base(pOU /*"OU=Users,OU=ITB-OGCIO,OU=CMMPBDOs"*/)
                .where("objectClass").is("user").and("whenChanged").gte(formattedTime);

        logger.log(Level.DEBUG, String.format("LDAP Base DN: %s, filter: %s", _query.base(), _query.filter()));

        List<CMMPExchangeLdapUser> _result = ldapTemplate.find(
            _query, CMMPExchangeLdapUser.class
        );

        logger.log(Level.INFO, String.format("%05d users have been fetched from CMMP", _result.size()));

        return _result;
    }


    /**
     * Get all group objects under the given OU and modified since the given timestamp
     *
     * @param pOU
     * @return
     */
    public List<CMMPExchangeLdapGroup> getAllGroupUnderOU(@NotNull String pOU) {

        logger.log(Level.TRACE, Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName());

        // Find all modified group since 1970-01-01
        return this.getLastModifiedGroupUnderOU(pOU, Instant.EPOCH);
    }


    /**
     * Get all group objects under the given OU and modified since the given timestamp
     *
     * @param pOU
     * @param pLastSyncTime
     * @return
     */
    public List<CMMPExchangeLdapGroup> getLastModifiedGroupUnderOU(@NotNull String pOU, @NotNull Instant pLastSyncTime) {

        logger.log(Level.TRACE, Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName());

        // Define your target timestamp
        String formattedTime = dataSyncManager.getLogFormat(pLastSyncTime);

        logger.log(Level.INFO, "Trying to fetch CMMP groups under OU %s and modified time after %s", pOU, formattedTime);

        LdapQuery _query = LdapQueryBuilder.query()
                .base(pOU /*"OU=Users,OU=ITB-OGCIO,OU=CMMPBDOs"*/)
                .where("objectClass").is("group").and("whenChanged").gte(formattedTime);

        logger.log(Level.DEBUG, String.format("LDAP Base DN: %s, filter: %s", _query.base(), _query.filter()));

        List<CMMPExchangeLdapGroup> _result = ldapTemplate.find(
                _query, CMMPExchangeLdapGroup.class
        );

        logger.log(Level.INFO, String.format("%05d groups have been fetched from CMMP", _result.size()));

        return _result;
    }



}
