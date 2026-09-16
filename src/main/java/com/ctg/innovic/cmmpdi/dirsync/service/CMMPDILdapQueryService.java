package com.ctg.innovic.cmmpdi.dirsync.service;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapUser;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.utils.LogUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQueryBuilder;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.Name;
import javax.naming.directory.ModificationItem;
import java.util.ArrayList;
import java.util.List;

@Service
public class CMMPDILdapQueryService {

    private static Logger logger = LogManager.getLogger(CMMPDILdapQueryService.class);

    //private final ApplicationContext context;

    @Autowired
    private LdapTemplate ldapTemplate;

//    @Autowired
//    private CMMPDILdapCacheService CMMPDILdapCacheService;


    /**
     * LDAP query all user objects from CMMP-DI
     * @return
     */
    public List<CMMPDILdapUser> listCMMPDIUsers() {

        logger.debug("Entering " + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );

        List<CMMPDILdapUser> _result = ldapTemplate.find(
                LdapQueryBuilder.query()
                        .where("objectClass").is("inetOrgPerson"),
                CMMPDILdapUser.class
        );

        return _result;
    }


    /**
     * LDAP query all group objects from CMMP-DI
     * @return
     */
    public List<CMMPDILdapGroup> listCMMPDIGroups() {

        logger.debug("Entering " + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );

        List<CMMPDILdapGroup> _result = ldapTemplate.find(
                LdapQueryBuilder.query()
                        .where("objectClass").is("group"),
                CMMPDILdapGroup.class
        );

        return _result;
    }


    public CMMPDILdapUser findOneCMMPDIUserByEmail(String email) {

        logger.debug("Entering " + LogUtils.getCurrentMethodName() + " with search condition mail: '" + email + "'");

        List<CMMPDILdapUser> _users = this.findUsersByEmail(email);

        CMMPDILdapUser _theOneUser = null;

        if ( _users != null && _users.isEmpty() == false ) {
            _theOneUser = _users.get(0);
        }

        return _theOneUser;
    }


    public List<CMMPDILdapUser> findUsersByEmail(String email) {

        logger.debug("Entering " + LogUtils.getCurrentMethodName() + " with search condition mail: '" + email + "'");

        List<CMMPDILdapUser> _result = ldapTemplate.find(
                LdapQueryBuilder.query()
                        .where("objectClass").is("inetOrgPerson").and("mail").is(email),
                CMMPDILdapUser.class
        );

        if ( logger.isDebugEnabled() ) {
            Gson gson = new GsonBuilder()
                    .disableHtmlEscaping()
                    .setPrettyPrinting() // Optional: formats output nicely
                    .create();
            for ( CMMPDILdapUser _user : _result ) {
                logger.debug( "DN = " + _user.getDn().toString() );
                logger.debug( gson.toJson(_user) );
            }
        }

        return _result;
    }


    public CMMPDILdapGroup findOneCMMPDIGroupByEmail(String email) {

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


    public boolean deleteCMMPDIObject(String pSmtpAddress) {

        logger.trace("Entering " + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName()
                + ", removing object with smtp='" + pSmtpAddress + "' from CMMP-DI directory" );

        try {

            CMMPDILdapUser _user = this.findOneCMMPDIUserByEmail(pSmtpAddress);

            if ( _user != null ) {

                logger.trace("Entering " + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName()
                        + ", removing user object with smtp='" + pSmtpAddress +
                        "' from CMMP-DI directory" );

                this.ldapTemplate.unbind(_user.getRealDn());
//                this.CMMPDILdapCacheService.removeUserCache(_user);
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


    /*
    public boolean createOrUpdateContactInCMMPDI (CMMPExchangeLdapUser pCMMPExchangeLdapUser) {

        logger.debug("Entering " + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );

        logger.debug("Checking if SMTP address '" + pCMMPExchangeLdapUser.getMail() + "' exists or not ...");

        List<CMMPDILdapUser> _userList = findUsersByEmail(pCMMPExchangeLdapUser.getMail());

        List<CMMPDILdapGroup> _groupList = findGroupByEmail(pCMMPExchangeLdapUser.getMail());

        if ( CollectionUtils.isEmpty(_userList) == false ) { // Found under user

            logger.debug("Found SMTP address '" + pCMMPExchangeLdapUser.getMail() + "' is already in CMMP as user ...");

            CMMPDILdapUser _targetUser = _userList.get(0);

            if ( _targetUser.isCMMPDIMailbox() == true ) {
                logger.info("This user '" + _targetUser.getEmail() + "' is CMMP-DI, skipping update.");
            }
            else {
                logger.info("This user '" + _targetUser.getEmail() + "' is CMMP, performing update.");

                updateUserAttributes(pCMMPExchangeLdapUser, _targetUser);
            }
        }

        // New object
        if ( CollectionUtils.isEmpty(_userList) == true && CollectionUtils.isEmpty(_groupList) == true ) {

            logger.info( pCMMPExchangeLdapUser.getMail() + " could not be found in CMMP-DI, creating new...");

//            this.createUserObjectInDI(pCMMPExchangeLdapUser);
        }




        return true;
    }*/


    /**
     * Assume the given SMTP address exists in the expected OU
     *
     * @param pCMMPExchangeLdapGroup
     * @param pCMMPDILdapGroup
     * @return
     */
    private boolean updateGroupAttributes(CMMPExchangeLdapGroup pCMMPExchangeLdapGroup, CMMPDILdapGroup pCMMPDILdapGroup) {

        logger.debug("Entering " + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );

        Name dn = LdapNameBuilder.newInstance(pCMMPDILdapGroup.getDn()).build();

        logger.info("Going to update group attributes for "  + dn.toString() + "...");

        List<ModificationItem> _list = new ArrayList<>();

        if ( CollectionUtils.isEmpty(_list) == false ) {

            // Handling for member
//            Set<Member> _membersFromCMMP = pCMMPExchangeLdapGroup.get

        }
        else {

        }

        return true;
    }



//    private boolean updateUserAttributes(CMMPExchangeLdapUser pCMMPExchangeLdapUser, CMMPDILdapUser pCMMPDILdapUser) {
//
//        logger.debug("Entering " + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );
//
//        Name dn = LdapNameBuilder.newInstance(pCMMPDILdapUser.getDn()).build();
//
//        logger.info("Going to update user attributes for "  + dn.toString() + "...");
//
//        List<ModificationItem> _list = new ArrayList<>();
//
//        // Compare each of the field, and log delta changes
//        if ( StringUtils.trimToEmpty(pCMMPDILdapUser.getDisplayName()).equals(StringUtils.EMPTY) ) {
//            logger.info("Detected display name difference, CMMP='"
//                    +  pCMMPExchangeLdapUser.getDisplayName() +"', while CMMP-DI one is empty");
//
//            _list.add(
//                    new ModificationItem(
//                            DirContext.ADD_ATTRIBUTE,
//                            new BasicAttribute("displayName", pCMMPExchangeLdapUser.getDisplayName())
//                    )
//            );
//        }
//        else if ( pCMMPExchangeLdapUser.getDisplayName().compareTo(StringUtils.trimToEmpty(pCMMPDILdapUser.getDisplayName())) != 0 ) {
//            logger.info("Detected display name difference, CMMP='"
//                    +  pCMMPExchangeLdapUser.getDisplayName() +"', while CMMP-DI='" + pCMMPDILdapUser.getDisplayName() + "'");
//
//            _list.add(
//                    new ModificationItem(
//                            DirContext.REPLACE_ATTRIBUTE,
//                            new BasicAttribute("displayName", pCMMPExchangeLdapUser.getDisplayName())
//                    )
//            );
//        }
//
//        if ( pCMMPExchangeLdapUser.getExtensionAttribute1() != pCMMPDILdapUser.getExtensionAttribute1() ) {
//            logger.info("Detected ExtensionAttribute1 difference, CMMP='"
//                    +  pCMMPExchangeLdapUser.getExtensionAttribute1() +"', while CMMP-DI='" + pCMMPDILdapUser.getExtensionAttribute1() + "'");
//
//            _list.add(
//                    new ModificationItem(
//                            DirContext.REPLACE_ATTRIBUTE,
//                            new BasicAttribute("extensionAttribute1", pCMMPExchangeLdapUser.getExtensionAttribute1())
//                    )
//            );
//        }
//
//        // If there is any modification required
//        if ( CollectionUtils.isEmpty(_list) == false ) {
//
//            ModificationItem[] _mods = new ModificationItem[ _list.size() ];
//
//            for ( int i = 0; i < _list.size(); i++ ) {
//                _mods[i] = _list.get(i);
//                logger.info(_mods[i].toString());
//            }
//
//            // Execute modification
//            ldapTemplate.modifyAttributes(dn, _mods);
//        }
//        else {
//            logger.info("No difference is detected for user object '" + pCMMPExchangeLdapUser.getMail() + "'");
//        }
//
//        return true;
//    }


//    private boolean createUserObjectInDI(CMMPExchangeLdapUser pCMMPExchangeLdapUser) {
//
//        logger.debug("Entering " + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );
//
//        Name dn = LdapNameBuilder.newInstance("ou=Users")
//                .add("cn", pCMMPExchangeLdapUser.getCn())
//                .build();
//
//        logger.info("Going to create user with this DN "  + dn.toString() + "...");
//
//        return true;
//    }

}
