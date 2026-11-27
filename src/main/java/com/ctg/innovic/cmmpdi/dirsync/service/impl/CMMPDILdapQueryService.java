package com.ctg.innovic.cmmpdi.dirsync.service.impl;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapUser;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.utils.Constants;
import com.ctg.innovic.cmmpdi.dirsync.utils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.Name;
import javax.naming.directory.ModificationItem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class CMMPDILdapQueryService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");

    private static Logger logger = LogManager.getLogger(CMMPDILdapQueryService.class);

    private final LdapTemplate ldapTemplate;

    public CMMPDILdapQueryService(@Qualifier("cmmpdiLdapTemplate") LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    /**
     * LDAP query all user objects from CMMP-DI under LDAP_BASE_OU_BDO
     * @return
     */
    public List<CMMPDILdapUser> listCMMPDIUsers() {
        return listCMMPDIBDOorWGDUsers(Constants.LdapOu.BDO);
    }


    /**
     * LDAP query all user objects from CMMP-DI under LDAP_BASE_OU_WGD
     * @return
     */
    public List<CMMPDILdapUser> listCMMPDIWGDUsers() {
        return listCMMPDIBDOorWGDUsers(Constants.LdapOu.WGD);
    }


    private List<CMMPDILdapUser> listCMMPDIBDOorWGDUsers(@NotNull String searchBase) {
        logEntering();

        LdapQuery _query = LdapQueryBuilder.query()
                .base(searchBase)
                .where(Constants.LdapField.OBJECT_CLASS).is("inetOrgPerson");

        String _logMessage = String.format("LDAP Base DN: %s, filter: %s", _query.base(), _query.filter());
        logger.log(Level.DEBUG, _logMessage);

        List<CMMPDILdapUser> _result = ldapTemplate.find(_query, CMMPDILdapUser.class);

        _logMessage = String.format("%6d user object fetched", _result.size());
        logger.log(Level.DEBUG, _logMessage);

        return _result;
    }


    /**
     * LDAP query all group objects from CMMP-DI
     * @return
     */
    public List<CMMPDILdapGroup> listCMMPDIGroups() {
        return listCMMPDIBDOorWGDGroups(Constants.LdapOu.BDO);
    }


    public List<CMMPDILdapGroup> listCMMPDIWGDGroups() {
        return listCMMPDIBDOorWGDGroups(Constants.LdapOu.WGD);
    }


    private List<CMMPDILdapGroup> listCMMPDIBDOorWGDGroups(@NotNull String searchBase) {
        logEntering();

        LdapQuery _query = LdapQueryBuilder.query()
                .base(searchBase)
                .where(Constants.LdapField.OBJECT_CLASS).is("group");

        String _logMessage = String.format("LDAP Base DN: %s, filter: %s", _query.base(), _query.filter());
        logger.log(Level.DEBUG, _logMessage);

        List<CMMPDILdapGroup> _result = ldapTemplate.find(_query, CMMPDILdapGroup.class);

        _logMessage = String.format("%6d user object fetched", _result.size());
        logger.log(Level.DEBUG, _logMessage);

        return _result;
    }


    public CMMPDILdapUser findOneCMMPDIUserByEmail(@NotNull String email) {

        String _logMessage = Constants.Logging.ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName();
        logger.log(Level.TRACE, _logMessage);

        List<CMMPDILdapUser> _users = this.findUsersByEmail(email);

        CMMPDILdapUser _theOneUser = null;

        if ( !CollectionUtils.isEmpty(_users) ) {
            _theOneUser = _users.get(0);
        }

        return _theOneUser;
    }


    public List<CMMPDILdapUser> findUsersByEmail(String email) {

        int _retryCnt = 0;

        while (_retryCnt < 3) {

            try {
                return findUsersByEmailInternal(email);
            }
            catch (org.springframework.ldap.CommunicationException ce) {
                _retryCnt++;
            }
        }

        return null;
    }



    private List<CMMPDILdapUser> findUsersByEmailInternal(String email) {

        logEntering();

        LdapQuery _query = LdapQueryBuilder.query()
                .base(Constants.LdapOu.BDO)
                .where(Constants.LdapField.OBJECT_CLASS).is("inetOrgPerson").and(Constants.LdapField.MAIL).is(email);

        logger.log(Level.DEBUG, String.format("LDAP Base DN: %s, filter: %s", _query.base(), _query.filter()));

        List<CMMPDILdapUser> _result = ldapTemplate.find(_query, CMMPDILdapUser.class);

        if ( logger.isTraceEnabled() ) {

            Gson gson = new GsonBuilder()
                .disableHtmlEscaping()
                .setPrettyPrinting() // Optional: formats output nicely
                .create();

            for ( CMMPDILdapUser _user : _result ) {
                logger.trace( gson.toJson(_user) );
            }
        }

        return _result;
    }



    public CMMPDILdapGroup findOneCMMPDIGroupByEmail(String email) {

        int _retryCnt = 0;

        while (_retryCnt < 3) {

            try {
                return findOneCMMPDIGroupByEmailInternal(email);
            }
            catch (org.springframework.ldap.CommunicationException ce) {
                _retryCnt++;
            }
        }

        return null;
    }


    private CMMPDILdapGroup findOneCMMPDIGroupByEmailInternal(String email) {

        logEntering();

        logger.debug("Entering " + LogUtils.getCurrentMethodName() + " with search condition mail: '" + email + "'");

        List<CMMPDILdapGroup> _groups = this.findGroupByEmail(email);

        CMMPDILdapGroup _theOneGroup = null;

        if ( _groups != null && _groups.isEmpty() == false ) {
            _theOneGroup = _groups.get(0);
        }

        return _theOneGroup;
    }



    public List<CMMPDILdapGroup> findGroupByEmail(String email) {

        logger.debug("Entering " + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() + " with search condition mail: '" + email + "'");

        List<CMMPDILdapGroup> _result = ldapTemplate.find(
                LdapQueryBuilder.query().where("mail").is(email),
                CMMPDILdapGroup.class
        );

        if ( logger.isTraceEnabled() ) {
            Gson gson = new Gson();
            for ( CMMPDILdapGroup _group : _result ) {
                logger.trace( gson.toJson(_group) );
                logger.trace( _group.getDn().toString() );

                for ( Name _eachMemberDN : _group.getMembers() ) {
                    logger.trace( _eachMemberDN.toString());
                }

//                logger.debug( _group.getMembers().size() );
            }
        }

        return _result;
    }


    public boolean deleteCMMPDIBDOObjectByUser(@NotNull CMMPDILdapUser user) {
        logEntering();

        try {

            if ( user != null ) {

                String _logMessage = "Trying to removing user object with smtp='" + user.getEmail().toLowerCase() + "' from CMMP-DI directory.";
                logger.log(Level.INFO, _logMessage);

                String _datePart = LocalDate.now(ZoneId.systemDefault()).format(DATE_FORMATTER);
                String _eventNum = String.format("%s%08d", _datePart, ThreadLocalRandom.current().nextInt(100_000_000));

                _logMessage =
                        String.format(
                                "Going to removing user object with smtp=%s dn=%s from CMMP-DI directory, " +
                                "this event is marked as %s", user.getEmail().toLowerCase(), user.getDn(), _eventNum);
                logger.log(Level.INFO, _logMessage);

                // Backup before removing
                Path filePath = Path.of("backupdata/" + _eventNum + "_user.json");
                Files.writeString(filePath, new Gson().toJson(user));

                _logMessage = String.format("Data has been backup to %s", filePath.toString() );
                logger.log(Level.DEBUG, _logMessage);

                this.ldapTemplate.unbind(user.getRealDn());
                return true;
            }
        }
        catch(Exception e) {
            logger.error("Exception in removing user object with smtp '" +  user.getEmail().toLowerCase() + "' " + e.getMessage());
            return false;
        }

        return false;
    }



    public boolean deleteCMMPDIBDOObjectByGroup(@NotNull CMMPDILdapGroup group) {
        logEntering();

        try {

            if ( group != null ) {

                String _logMessage = "Trying to removing group object with smtp='" + group.getEmail().toLowerCase() + "' from CMMP-DI directory.";
                logger.log(Level.INFO, _logMessage);

                String _datePart = LocalDate.now(ZoneId.systemDefault()).format(DATE_FORMATTER);
                String _eventNum = String.format("%s%08d", _datePart, ThreadLocalRandom.current().nextInt(100_000_000));

                _logMessage =
                        String.format(
                                "Going to removing group object with smtp=%s dn=%s from CMMP-DI directory, " +
                                        "this event is marked as %s", group.getEmail().toLowerCase(), group.getDn(), _eventNum);
                logger.log(Level.INFO, _logMessage);

                // Backup before removing
                Path filePath = Path.of("backupdata/" + _eventNum + "_group.json");
                Files.writeString(filePath, new Gson().toJson(group));

                _logMessage = String.format("Data has been backup to %s", filePath.toString() );
                logger.log(Level.DEBUG, _logMessage);

                this.ldapTemplate.unbind(group.getDn());
                return true;
            }
        }
        catch(Exception e) {
            logger.error("Exception in removing group object with smtp '" +  group.getEmail().toLowerCase() + "' " + e.getMessage());
            return false;
        }

        return false;
    }


    /**
     * A central method to remove CMMP-DI objects for user and group
     *
     * @param pSmtpAddress
     * @return
     */
    public boolean deleteCMMPDIObjectBySMTPAddress111(String pSmtpAddress) {

        logEntering();

        String _logMessage = "Trying to removing object with smtp='" + pSmtpAddress + "' from CMMP-DI directory.";
        logger.log(Level.INFO, _logMessage);

        try {

            CMMPDILdapUser _user = this.findOneCMMPDIUserByEmail(pSmtpAddress);

            if ( _user != null ) {

                String _datePart = LocalDate.now(ZoneId.systemDefault()).format(DATE_FORMATTER);
                String _eventNum = String.format("%s%08d", _datePart, ThreadLocalRandom.current().nextInt(100_000_000));

                _logMessage = String.format("Going to removing user object with smtp=%s from CMMP-DI directory, this event is marked as %s", pSmtpAddress, _eventNum);
                logger.log(Level.INFO, _logMessage);

                // Backup before removing
                Path filePath = Path.of("backupdata/" + _eventNum + ".json");
                Files.writeString(filePath, new Gson().toJson(_user));

                _logMessage = String.format("Data has been backup to %s", filePath.toString() );
                logger.log(Level.DEBUG, _logMessage);

                this.ldapTemplate.unbind(_user.getRealDn());
                return true;
            }

            CMMPDILdapGroup _group = this.findOneCMMPDIGroupByEmail(pSmtpAddress);

            if ( _group != null ) {

                logger.trace("Entering " + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName()
                        + ", removing group object with smtp='" + pSmtpAddress +
                        "' from CMMP-DI directory" );

                this.ldapTemplate.unbind(_group.getRealDn());
//                this.CMMPDILdapCacheService.removeGroupCache(_group);
                return true;
            }
        }
        catch(Exception e) {
            logger.error("Exception in removing object with smtp '" +  pSmtpAddress + "'");
            return false;
        }

        return true;
    }



    public boolean queryUserOrGroupObjectByEmailAddress(String email) {

        logger.debug("Entering " + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() + " with search condition mail: '" + email + "'");

        boolean _result = false;

        List<CMMPDILdapUser> _users = this.findUsersByEmail(email);

        if ( !CollectionUtils.isEmpty(_users) ) {
            _result = true;
            return _result;
        }

        List<CMMPDILdapGroup> _groups = this.findGroupByEmail(email);

        if ( !CollectionUtils.isEmpty(_groups) ) {
            _result = true;
            return _result;
        }

        return _result;
    }


    /**
     * Assume the given SMTP address exists in the expected OU
     *
     * @param pCMMPExchangeLdapGroup
     * @param pCMMPDILdapGroup
     * @return
     */
    private boolean updateGroupAttributes(CMMPExchangeLdapGroup pCMMPExchangeLdapGroup, CMMPDILdapGroup pCMMPDILdapGroup) {

        logEntering();

        Name dn = LdapNameBuilder.newInstance(pCMMPDILdapGroup.getDn()).build();

        String _logMessage = "Going to update group attributes for "  + dn.toString() + "...";
        logger.log(Level.INFO, _logMessage);

        List<ModificationItem> _list = new ArrayList<>();

        return true;
    }

    private void logEntering() {
        logger.log(Level.TRACE, Constants.Logging.ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName());
    }
}
