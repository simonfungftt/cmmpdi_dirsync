package com.ctg.innovic.cmmpdi.dirsync.service;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapUser;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapUser;
import com.ctg.innovic.cmmpdi.dirsync.utils.Constants;
import com.ctg.innovic.cmmpdi.dirsync.utils.LogUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class CMMPDILdapUserService {

    private static final Logger logger = LogManager.getLogger(CMMPDILdapUserService.class);

    @Autowired
    private CMMPDILdapCacheService cmmpdiLdapCacheService;

    @Autowired
    @Qualifier("cmmpdiLdapTemplate")
    private LdapTemplate ldapTemplate;

    @Autowired
    private CMMPDILdapQueryService cmmpdiLdapQueryService;

    private String getLocalPart(String smtpAddress) {

        if (StringUtils.trimToNull(smtpAddress) != null ) {

            try {

                InternetAddress address = new InternetAddress(smtpAddress);
                return address.getAddress().split("@")[0].toLowerCase();

            } catch (AddressException ae) {
                logger.error("Cannot find local part from '" + smtpAddress + "'");
            }
        }

        return null;
    }


    private String[] extractOus(String dnString) throws InvalidNameException {
        LdapName dn = new LdapName(dnString);
        List<String> ouList = new ArrayList<>();

        // Iterate backward through RDNs (from leaf to root order as written in string)
        for (int i = dn.size() - 1; i >= 0; i--) {
            Rdn rdn = dn.getRdn(i);
            if ("OU".equalsIgnoreCase(rdn.getType())) {

                if ( rdn.getValue().toString().equalsIgnoreCase("Users") ) {
                    ouList.add("User");
                }
                else {
                    ouList.add((String) rdn.getValue());
                }
            }
        }

        return ouList.toArray(new String[0]);
    }


    private CMMPDILdapUser transformFromCMMPExchangeLdapUser2CMMPDILdapUser(CMMPExchangeLdapUser pCMMPExchangeLdapUser) throws InvalidNameException {

        logger.trace(Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );

        CMMPDILdapUser _cmmpdiLdapUser = this.cmmpdiLdapCacheService.getCMMPDIUserDnBySMTP(pCMMPExchangeLdapUser.getMail());

        if ( _cmmpdiLdapUser == null ) {
            _cmmpdiLdapUser = new CMMPDILdapUser();
        }

        _cmmpdiLdapUser.setDn( new LdapName(pCMMPExchangeLdapUser.getDn()) );
        _cmmpdiLdapUser.setDisplayName( pCMMPExchangeLdapUser.getDisplayName() );
        _cmmpdiLdapUser.setCommonName( pCMMPExchangeLdapUser.getCn() );
        _cmmpdiLdapUser.setDistinguishedName( pCMMPExchangeLdapUser.getDistinguishedName() );
        _cmmpdiLdapUser.setEmail( pCMMPExchangeLdapUser.getMail() );
        _cmmpdiLdapUser.setExtensionAttribute1( pCMMPExchangeLdapUser.getExtensionAttribute1() );
        _cmmpdiLdapUser.setExtensionAttribute7( pCMMPExchangeLdapUser.getExtensionAttribute7() );

        return _cmmpdiLdapUser;
    }


    /**
     * Central method to create/update contact objects in CMMP-DI
     *
     * @param pCMMPExchangeLdapUser
     * @return
     * @throws InvalidNameException
     */
    public boolean createOrUpdateCMMPDIUser(CMMPExchangeLdapUser pCMMPExchangeLdapUser) throws InvalidNameException {

        logger.trace(Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );

        boolean _result = false;

        // Determine create or update case
        CMMPDILdapUser _user = this.cmmpdiLdapCacheService.getCMMPDIUserDnBySMTP(pCMMPExchangeLdapUser.getMail());
        CMMPDILdapGroup _group = this.cmmpdiLdapCacheService.getCMMPDIGroupDnBySMTP(pCMMPExchangeLdapUser.getMail());

        if ( _user == null && _group == null ) {

            logger.info("Going to create object " + getLocalPart(pCMMPExchangeLdapUser.getMail())
                    + ", original DN = " + pCMMPExchangeLdapUser.getDn()
                    + ", with SMTP address " + pCMMPExchangeLdapUser.getMail());

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



    /**
     * Method to create CMMP-DI users into BDO OU path based CMMP Exchange user
     *
     * @param pCMMPExchangeLdapUser
     * @return
     * @throws InvalidNameException
     */
    protected boolean createCMMPDIUser(CMMPExchangeLdapUser pCMMPExchangeLdapUser) throws InvalidNameException {

        CMMPDILdapUser pCMMPDILdapUser = this.transformFromCMMPExchangeLdapUser2CMMPDILdapUser(pCMMPExchangeLdapUser);

        return createCMMPDIUser(pCMMPDILdapUser);
    }


    protected boolean createWGDCMMPDIUser(CMMPDILdapUser pCMMPDILdapUser) throws InvalidNameException {

        // If it is a new object, build the base DN
        LdapNameBuilder builder = LdapNameBuilder.newInstance(Constants.LDAP_BASE_OU_WGD);

        // Assume the LDAP user does not exist
        String[] _array = extractOus( pCMMPDILdapUser.getDn() );

        logger.debug("OU _array = " + Arrays.toString(_array));

        for ( int i = _array.length - 2; i >= 0; i-- ) {
            builder.add("ou", _array[i]);
        }

        builder.add("cn", getLocalPart(pCMMPDILdapUser.getEmail()));
        Name dn = builder.build();

        logger.debug("Generate DN = " + dn.toString());

        // 2. Prepare the context with objectClasses and attributes
        DirContextAdapter context = new DirContextAdapter(dn);

        context.addAttributeValue(Constants.LDAP_FIELD_OBJECT_CLASS, "top");
        context.addAttributeValue(Constants.LDAP_FIELD_OBJECT_CLASS, "person");
        context.addAttributeValue(Constants.LDAP_FIELD_OBJECT_CLASS, "organizationalPerson");
        context.addAttributeValue(Constants.LDAP_FIELD_OBJECT_CLASS, "inetOrgPerson");
        context.addAttributeValue(Constants.LDAP_FIELD_OBJECT_CLASS, "user");

        context.addAttributeValue("cn", getLocalPart(pCMMPDILdapUser.getEmail()));
        context.addAttributeValue("sn", pCMMPDILdapUser.getSurname());
        context.addAttributeValue("givenName", pCMMPDILdapUser.getFirstName());
        context.addAttributeValue(Constants.LDAP_FIELD_DISPLAY_NAME, pCMMPDILdapUser.getDisplayName());
        context.addAttributeValue("distinguishedName", pCMMPDILdapUser.getDistinguishedName());
        context.addAttributeValue("mail", pCMMPDILdapUser.getEmail());

        //context.addAttributeValue();

        logger.info("context = " + context);

        this.ldapTemplate.bind(context);

        this.cmmpdiLdapCacheService.addUserCache(pCMMPDILdapUser.getEmail());

        return true;
    }



    protected boolean createCMMPDIUser(CMMPDILdapUser pCMMPDILdapUser) throws InvalidNameException {

        // If it is a new object, build the base DN
        LdapNameBuilder builder = LdapNameBuilder.newInstance(Constants.BASE_OU_WO_STARTING_COMMAND);

        // Assume the LDAP user does not exist
        String[] _array = extractOus( pCMMPDILdapUser.getDn() );

        logger.debug("OU _array = " + Arrays.toString(_array));

        for ( int i = _array.length - 2; i >= 0; i-- ) {
            builder.add("ou", _array[i]);
        }

        builder.add("cn", getLocalPart(pCMMPDILdapUser.getEmail()));
        Name dn = builder.build();

        logger.debug("Generate DN = " + dn.toString());

        // 2. Prepare the context with objectClasses and attributes
        DirContextAdapter context = new DirContextAdapter(dn);

        context.addAttributeValue(Constants.LDAP_FIELD_OBJECT_CLASS, "top");
        context.addAttributeValue(Constants.LDAP_FIELD_OBJECT_CLASS, "person");
        context.addAttributeValue(Constants.LDAP_FIELD_OBJECT_CLASS, "organizationalPerson");
        context.addAttributeValue(Constants.LDAP_FIELD_OBJECT_CLASS, "inetOrgPerson");
        context.addAttributeValue(Constants.LDAP_FIELD_OBJECT_CLASS, "user");

        context.addAttributeValue("cn", getLocalPart(pCMMPDILdapUser.getEmail()));
        context.addAttributeValue("sn", pCMMPDILdapUser.getSurname());
        context.addAttributeValue("givenName", pCMMPDILdapUser.getFirstName());
        context.addAttributeValue(Constants.LDAP_FIELD_DISPLAY_NAME, pCMMPDILdapUser.getDisplayName());
        context.addAttributeValue("distinguishedName", pCMMPDILdapUser.getDistinguishedName());
        context.addAttributeValue("mail", pCMMPDILdapUser.getEmail());

        //context.addAttributeValue();

        logger.info("context = " + context);

        this.ldapTemplate.bind(context);

        this.cmmpdiLdapCacheService.addUserCache(pCMMPDILdapUser.getEmail());

        return true;
    }


    protected boolean updateCMMPDIUser(CMMPExchangeLdapUser pCMMPExchangeLdapUser) {

        CMMPDILdapUser pCMMPDILdapUser = this.cmmpdiLdapCacheService.getCMMPDIUserDnBySMTP(pCMMPExchangeLdapUser.getMail());

        // If the user is not a CMMP-DI mailbox
        if ( pCMMPDILdapUser != null && pCMMPDILdapUser.getExtensionAttribute3() <= 3) {
            this.updateUserAttributes(pCMMPExchangeLdapUser, pCMMPDILdapUser);
        }

        return true;
    }


    public boolean deleteCMMPDIUser(@NotNull CMMPDILdapUser pCMMPDILdapUser) {

        logger.debug(Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );

        Name dn = LdapNameBuilder.newInstance(pCMMPDILdapUser.getDn()).build();

        logger.log(Level.DEBUG,
                String.format(
                    "Going to delete user object with DN '%s'", dn
                ));

        this.ldapTemplate.unbind(dn);

        return true;
    }



    private boolean updateUserAttributes(CMMPExchangeLdapUser pCMMPExchangeLdapUser, CMMPDILdapUser pCMMPDILdapUser) {

        logger.debug(Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );

        Name dn = LdapNameBuilder.newInstance(pCMMPDILdapUser.getDn()).build();

        logger.info("Going to update user attributes for smtp='{}', DN='{}'...", pCMMPDILdapUser.getEmail(), dn);

        logger.info("pCMMPExchangeLdapUser.getDisplayName() = '" + pCMMPExchangeLdapUser.getDisplayName() + "'");
        logger.info("pCMMPDILdapUser.getDisplayName() = '" + pCMMPDILdapUser.getDisplayName() + "'");

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
        else if ( pCMMPExchangeLdapUser.getDisplayName() != null && pCMMPExchangeLdapUser.getDisplayName().compareTo(StringUtils.trimToEmpty(pCMMPDILdapUser.getDisplayName())) != 0 ) {
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
                    +  pCMMPExchangeLdapUser.getExtensionAttribute1()
                    + "', while CMMP-DI='" + pCMMPDILdapUser.getExtensionAttribute1() + "'");

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
            logger.info(String.format("No difference is detected for user object '%s'", pCMMPExchangeLdapUser.getMail()));
        }

        return true;
    }


}
