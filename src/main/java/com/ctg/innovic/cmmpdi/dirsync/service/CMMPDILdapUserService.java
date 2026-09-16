package com.ctg.innovic.cmmpdi.dirsync.service;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapUser;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapUser;
import com.ctg.innovic.cmmpdi.dirsync.utils.Constants;
import com.ctg.innovic.cmmpdi.dirsync.utils.LogUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.util.ArrayList;
import java.util.List;

@Service
public class CMMPDILdapUserService {

    private static final Logger logger = LogManager.getLogger(CMMPDILdapGroupService.class);

    @Autowired
    private CMMPDILdapCacheService CMMPDILdapCacheService;

    @Autowired
    private CMMPDILdapQueryService CMMPDILdapQueryService;

    @Autowired
    private LdapTemplate ldapTemplate;

    @Autowired
    private CMMPDILdapQueryService cmmpdiLdapQueryService;
/*
    private String getLocalPart(String smtpAddress) {

        if (StringUtils.trimToNull(smtpAddress) != null ) {

            try {

                InternetAddress address = new InternetAddress(smtpAddress);
                return address.getAddress().split("@")[0];

            } catch (AddressException ae) {
                logger.error("Cannot find local part from '" + smtpAddress + "'");
            }
        }

        return null;
    }
*/

//    private String get

//    private String extractBaseDn(String fullDn)  {
//
//        String result = fullDn.substring( fullDn.indexOf(",") + 1 );
//
//        return result;
//    }


    private String[] extractOus(String dnString) throws InvalidNameException {
        LdapName dn = new LdapName(dnString);
        List<String> ouList = new ArrayList<>();

        // Iterate backward through RDNs (from leaf to root order as written in string)
        for (int i = dn.size() - 1; i >= 0; i--) {
            Rdn rdn = dn.getRdn(i);
            if ("OU".equalsIgnoreCase(rdn.getType())) {
                ouList.add((String) rdn.getValue());
            }
        }

        return ouList.toArray(new String[0]);
    }


    private CMMPDILdapUser transformFromCMMPExchangeLdapUser2CMMPDILdapUser(CMMPExchangeLdapUser pCMMPExchangeLdapUser) {

        logger.debug("Entering " + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );

        CMMPDILdapUser _cmmpdiLdapUser = this.CMMPDILdapCacheService.getCMMPDIUserDnBySMTP(pCMMPExchangeLdapUser.getMail());

        if ( _cmmpdiLdapUser == null ) {
            _cmmpdiLdapUser = new CMMPDILdapUser();

        }
        else {

        }

        return null;
    }


    /**
     * Central method to create/update contact objects in CMMP-DI
     *
     * @param pCMMPExchangeLdapUser
     * @return
     * @throws InvalidNameException
     */
    public boolean createOrUpdateCMMPDIUser(CMMPExchangeLdapUser pCMMPExchangeLdapUser) throws InvalidNameException {

        logger.trace("Entering " + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );

        boolean _result = false;

        // Determine create or update case
        CMMPDILdapUser _user = this.CMMPDILdapCacheService.getCMMPDIUserDnBySMTP(pCMMPExchangeLdapUser.getMail());
        CMMPDILdapGroup _group = this.CMMPDILdapCacheService.getCMMPDIGroupDnBySMTP(pCMMPExchangeLdapUser.getMail());

        if ( _user == null && _group == null ) {
            // Create case
            _result = this.createCMMPDIUser(pCMMPExchangeLdapUser);
        }
        else if ( _user == null && _group != null ) {

            logger.info("Going to remove object " + _group.getRealDn()
                    + " due to SMTP address " + pCMMPExchangeLdapUser.getMail()
                    + " conflict with users...");

            this.cmmpdiLdapQueryService.deleteCMMPDIObject(_group.getEmail());

            logger.info("Creating new contact object as below...");

            _result = this.createCMMPDIUser(pCMMPExchangeLdapUser);
        }
        else {
            // Update case
            _result = this.updateCMMPDIUser(pCMMPExchangeLdapUser);
        }



        return _result;
    }


    private boolean createCMMPDIUser(CMMPExchangeLdapUser pCMMPExchangeLdapUser) throws InvalidNameException {

        CMMPDILdapUser pCMMPDILdapUser = this.transformFromCMMPExchangeLdapUser2CMMPDILdapUser(pCMMPExchangeLdapUser);

        // If it is a new object, build the base DN
        LdapNameBuilder builder = LdapNameBuilder.newInstance(Constants.BASE_OU_WO_STARTING_COMMAND);

        // Assume the LDAP user does not exist
        String[] _array = extractOus( pCMMPDILdapUser.getDn() );

        for ( short i = 0; i < _array.length; i++ ) {
            builder.add("ou", _array[i]);
        }

        builder.add("cn", pCMMPDILdapUser.getCommonName().toLowerCase());
        Name dn = builder.build();

        logger.debug("Generate DN = " + dn.toString());

        // 2. Prepare the context with objectClasses and attributes
        DirContextAdapter context = new DirContextAdapter(dn);

        context.setAttributeValues("objectClass", new String[] {
                "top",
                "person",
                "organizationalPerson",
                "inetOrgPerson"
        });


        context.setAttributeValue("instanceType", 4);
        context.setAttributeValue("cn", pCMMPDILdapUser.getCommonName());
        context.setAttributeValue("sn", pCMMPDILdapUser.getSurname());
        context.setAttributeValue("givenName", pCMMPDILdapUser.getFirstName());
        context.setAttributeValue("displayName", pCMMPDILdapUser.getDisplayName());
        context.setAttributeValue("distinguishedName", pCMMPDILdapUser.getDistinguishedName());
        context.setAttributeValue("mail", pCMMPDILdapUser.getEmail());
        //context.setAttributeValue( "proxyAddresses", new String[]{ "123", "456"} );

        context.setAttributeValue( "extensionAttribute1", pCMMPDILdapUser.getExtensionAttribute1());
        context.setAttributeValue( "extensionAttribute3", pCMMPDILdapUser.getExtensionAttribute3());

//        this.ldapTemplate.bind(context);

        logger.debug("context = " + context);

        this.CMMPDILdapCacheService.addUserCache(pCMMPDILdapUser.getEmail());

        return true;
    }


    private boolean updateCMMPDIUser(CMMPExchangeLdapUser pCMMPExchangeLdapUser) {

        CMMPDILdapUser pCMMPDILdapUser = this.CMMPDILdapCacheService.getCMMPDIUserDnBySMTP(pCMMPExchangeLdapUser.getMail());

        if ( pCMMPDILdapUser != null && pCMMPDILdapUser.getExtensionAttribute3() <= 3) {

            this.updateUserAttributes(pCMMPExchangeLdapUser, pCMMPDILdapUser);
        }

        return true;
    }



    private boolean updateUserAttributes(CMMPExchangeLdapUser pCMMPExchangeLdapUser, CMMPDILdapUser pCMMPDILdapUser) {

        logger.debug("Entering " + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );

        Name dn = LdapNameBuilder.newInstance(pCMMPDILdapUser.getDn()).build();

        logger.info("Going to update user attributes for "  + dn.toString() + "...");

        List<ModificationItem> _list = new ArrayList<>();

        // Compare each of the field, and log delta changes
        if ( StringUtils.trimToEmpty(pCMMPDILdapUser.getDisplayName()).equals(StringUtils.EMPTY) ) {
            logger.info("Detected display name difference, CMMP='"
                    +  pCMMPExchangeLdapUser.getDisplayName() +"', while CMMP-DI one is empty");

            _list.add(
                    new ModificationItem(
                            DirContext.ADD_ATTRIBUTE,
                            new BasicAttribute("displayName", pCMMPExchangeLdapUser.getDisplayName())
                    )
            );
        }
        else if ( pCMMPExchangeLdapUser.getDisplayName().compareTo(StringUtils.trimToEmpty(pCMMPDILdapUser.getDisplayName())) != 0 ) {
            logger.info("Detected display name difference, CMMP='"
                    +  pCMMPExchangeLdapUser.getDisplayName() +"', while CMMP-DI='" + pCMMPDILdapUser.getDisplayName() + "'");

            _list.add(
                    new ModificationItem(
                            DirContext.REPLACE_ATTRIBUTE,
                            new BasicAttribute("displayName", pCMMPExchangeLdapUser.getDisplayName())
                    )
            );
        }

        if ( pCMMPExchangeLdapUser.getExtensionAttribute1() != pCMMPDILdapUser.getExtensionAttribute1() ) {
            logger.info("Detected ExtensionAttribute1 difference, CMMP='"
                    +  pCMMPExchangeLdapUser.getExtensionAttribute1() +"', while CMMP-DI='" + pCMMPDILdapUser.getExtensionAttribute1() + "'");

            _list.add(
                    new ModificationItem(
                            DirContext.REPLACE_ATTRIBUTE,
                            new BasicAttribute("extensionAttribute1", pCMMPExchangeLdapUser.getExtensionAttribute1())
                    )
            );
        }

        // If there is any modification required
        if ( !CollectionUtils.isEmpty(_list) ) {

            ModificationItem[] _mods = new ModificationItem[ _list.size() ];

            for ( int i = 0; i < _list.size(); i++ ) {
                _mods[i] = _list.get(i);
                logger.info(_mods[i].toString());
            }

            // Execute modification
            ldapTemplate.modifyAttributes(dn, _mods);
        }
        else {
            logger.info("No difference is detected for user object '" + pCMMPExchangeLdapUser.getMail() + "'");
        }

        return true;
    }


}
