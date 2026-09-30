package com.ctg.innovic.cmmpdi.dirsync.service;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapUser;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapUser;
import com.ctg.innovic.cmmpdi.dirsync.utils.Constants;
import com.ctg.innovic.cmmpdi.dirsync.utils.LogUtils;
import com.unboundid.util.NotNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CMMPLdapQueryService {

    private final static Logger logger = LogManager.getLogger(CMMPLdapQueryService.class);

    @Autowired
    @Qualifier("cmmpLdapTemplate")
    private LdapTemplate ldapTemplate;


    public CMMPExchangeLdapGroup dipGroupByCN(@NotNull String pCN) {

        logger.log(Level.TRACE, Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName());

        CMMPExchangeLdapGroup _group = null;

        List<CMMPExchangeLdapGroup> _result = ldapTemplate.find(
                LdapQueryBuilder.query()
                        .base("OU=Users,OU=ITB-OGCIO,OU=CMMPBDOs")
                        .where("objectClass").is("group").and("cn").is(pCN),
                CMMPExchangeLdapGroup.class
        );

        if ( !CollectionUtils.isEmpty(_result) ) {
            _group = _result.get(0);
        }

        return _group;
    }



    public CMMPExchangeLdapUser dipUserByCN(@NotNull String pCN) {

        logger.log(Level.TRACE, Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName());

        CMMPExchangeLdapUser _user = null;

        List<CMMPExchangeLdapUser> _result = ldapTemplate.find(
                LdapQueryBuilder.query()
                        .base("OU=Users,OU=ITB-OGCIO,OU=CMMPBDOs")
                        .where("objectClass").is("user").and("cn").is(pCN),
                CMMPExchangeLdapUser.class
        );

        if ( !CollectionUtils.isEmpty(_result) ) {
            _user = _result.get(0);
        }

        return _user;
    }


    public List<CMMPExchangeLdapUser> getAllUserUnderOU(@NotNull String pOU) {

        logger.log(Level.TRACE, Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName());

        return this.getLastModifiedUserUnderOU(pOU, Instant.EPOCH);
    }



    public List<CMMPExchangeLdapUser> getLastModifiedUserUnderOU(@NotNull String pOU, @NotNull Instant pLastSyncTime) {

        logger.log(Level.TRACE, Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName());

        DateTimeFormatter AD_DATE_FORMATTER = DateTimeFormatter
                .ofPattern("yyyyMMddHHmmss'.0Z'")
                .withZone(ZoneOffset.UTC);

        // Define your target timestamp
        String formattedTime = AD_DATE_FORMATTER.format(pLastSyncTime);

        logger.log(Level.INFO, "Trying to fetch CMMP users under OU %s and modified time after %s", pOU, formattedTime);

        List<CMMPExchangeLdapUser> _result = ldapTemplate.find(
                LdapQueryBuilder.query()
                        .base("OU=Users,OU=ITB-OGCIO,OU=CMMPBDOs")
                        .where("objectClass").is("user").and("whenChanged").gte(formattedTime),
                CMMPExchangeLdapUser.class
        );

        logger.log(Level.INFO, String.format("%05d users have been fetched from CMMP", _result.size()));

        return _result;
    }



}
