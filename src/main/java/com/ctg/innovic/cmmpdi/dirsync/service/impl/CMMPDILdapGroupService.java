package com.ctg.innovic.cmmpdi.dirsync.service.impl;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapUser;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.exception.DirSyncApplicationException;
import com.ctg.innovic.cmmpdi.dirsync.utils.Constants;
import com.ctg.innovic.cmmpdi.dirsync.utils.LogUtils;
import com.unboundid.util.NotNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.query.LdapQuery;
import org.springframework.ldap.query.LdapQueryBuilder;
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
import java.util.*;

@Service
public class CMMPDILdapGroupService {

    private static Logger logger = LogManager.getLogger(CMMPDILdapGroupService.class);

    private final CMMPDILdapCacheService cmmpdiLdapCacheService;
    private final CMMPCacheService exchangeCacheService;
    private final LdapTemplate ldapTemplate;
    private final CMMPDILdapQueryService cmmpdiLdapQueryService;

    public CMMPDILdapGroupService(
            CMMPDILdapCacheService cmmpdiLdapCacheService,
            CMMPCacheService exchangeCacheService,
            @Qualifier("cmmpdiLdapTemplate") LdapTemplate ldapTemplate,
            CMMPDILdapQueryService cmmpdiLdapQueryService) {
        this.cmmpdiLdapCacheService = cmmpdiLdapCacheService;
        this.exchangeCacheService = exchangeCacheService;
        this.ldapTemplate = ldapTemplate;
        this.cmmpdiLdapQueryService = cmmpdiLdapQueryService;
    }


    private void logEntering() {
        logger.log(Level.TRACE, Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName());
    }

    public List<CMMPDILdapGroup> getAllGroupUnderParamOU(@NotNull String pOU) {
        logEntering();

        String _logMessage = String.format("Trying to fetch CMMP-DI directory group under OU %s", pOU);
        logger.log(Level.INFO, _logMessage);

        LdapQuery query = LdapQueryBuilder.query()
                .base(pOU)
                .where(Constants.LDAP_FIELD_OBJECT_CLASS).is("group");

        _logMessage = String.format("LDAP Base DN: %s, filter: %s", query.base(), query.filter());
        logger.log(Level.DEBUG, _logMessage);

        List<CMMPDILdapGroup> result = ldapTemplate.find(query, CMMPDILdapGroup.class);

        _logMessage = String.format("%06d group have been fetched from directory", result.size());
        logger.log(Level.INFO, _logMessage);

        return result;
    }


    public boolean createOrUpdateCMMPDIGroup(CMMPExchangeLdapGroup pCMMPExchangeLdapGroup) throws InvalidNameException, DirSyncApplicationException {

        boolean _result = false;

        logger.trace(Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );

        // Determine create or update case
        CMMPDILdapUser _user = this.cmmpdiLdapCacheService.getCMMPDIUserDnBySMTP(pCMMPExchangeLdapGroup.getMail());
        CMMPDILdapGroup _group = this.cmmpdiLdapCacheService.getCMMPDIGroupDnBySMTP(pCMMPExchangeLdapGroup.getMail());

        if ( _user == null && _group == null ) {

            logger.info("Going to create group " + pCMMPExchangeLdapGroup.getDn()
                    + "with SMTP address " + pCMMPExchangeLdapGroup.getMail());

            _result = this.createCMMPDIGroup(pCMMPExchangeLdapGroup);
        }
        else if ( _user == null && _group != null ) {

            logger.info("Going to remove object " + _group.getRealDn()
                    + " due to SMTP address " + pCMMPExchangeLdapGroup.getMail()
                    + " conflict with users...");

            if ( this.cmmpdiLdapQueryService.deleteCMMPDIBDOObjectByGroup(_group) ) {
                this.cmmpdiLdapCacheService.removeGroupCache(_group);
            }

            logger.info("Creating new contact object as below...");

            _result = this.createCMMPDIGroup(pCMMPExchangeLdapGroup);
        }
        else if ( _group != null ) {
            // Update case
            this.updateGroupAttributes(pCMMPExchangeLdapGroup, _group);
            _result = this.updateGroupMembership(pCMMPExchangeLdapGroup.getMail(), pCMMPExchangeLdapGroup.getMemberDNs());
        }

        return _result;
    }


    private List<String> convertMembershipListFromCMMP(@NotNull CMMPExchangeLdapGroup exchangeGroup)
            throws DirSyncApplicationException {
        logEntering();

        String groupMail = exchangeGroup.getMail() != null ? exchangeGroup.getMail() : exchangeGroup.getCn();
        List<String> sourceMemberDns = exchangeGroup.getMemberDNs();
        if (sourceMemberDns == null || sourceMemberDns.isEmpty()) {
            return Collections.emptyList();
        }

        if (logger.isEnabled(Level.DEBUG)) {
            logger.log(Level.DEBUG, String.format(
                    "Going to translate member list of Exchange group %s, with members size of %5d",
                    groupMail, sourceMemberDns.size()));
        }

        // Use LinkedHashSet to prevent duplicate member DNs in LDAP
        Set<String> translatedMemberDns = new LinkedHashSet<>();

        for (String eachMemberDn : sourceMemberDns) {
            String memberSmtp = exchangeCacheService.lookupSmtpByCMMPDN(eachMemberDn);
            if (memberSmtp == null || memberSmtp.isBlank()) {
                logger.log(Level.WARN, String.format(
                        "Member DN not found in Exchange cache (skipped): %s", eachMemberDn));
                continue;
            }

            String targetDn = resolveCmmpdiMemberDn(memberSmtp);
            if (targetDn != null) {
                translatedMemberDns.add(targetDn);
            } else {
                logger.log(Level.WARN, String.format(
                        "SMTP %s (from %s) not found in CMMP-DI user/group cache (skipped)",
                        memberSmtp, eachMemberDn));
            }
        }

        if (logger.isEnabled(Level.DEBUG)) {
            logger.log(Level.DEBUG, String.format(
                    "After translation, %s in CMMP has %5d members, translated %5d.",
                    groupMail, sourceMemberDns.size(), translatedMemberDns.size()));
        }

        return new ArrayList<>(translatedMemberDns);
    }

    private String resolveCmmpdiMemberDn(String memberSmtp) {
        CMMPDILdapUser cmmpdiUser = this.cmmpdiLdapCacheService.getCMMPDIUserDnBySMTP(memberSmtp);
        if (cmmpdiUser != null && cmmpdiUser.getDn() != null) {
            return cmmpdiUser.getDn();
        }

        CMMPDILdapGroup cmmpdiGroup = this.cmmpdiLdapCacheService.getCMMPDIGroupDnBySMTP(memberSmtp);
        if (cmmpdiGroup != null && cmmpdiGroup.getDn() != null) {
            return cmmpdiGroup.getDn();
        }

        return null;
    }


    public List<Name> getMemberListBasedOnCMMPOfDI(CMMPExchangeLdapGroup pCMMPExchangeLdapGroup) throws DirSyncApplicationException {

        String _logMessage = Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName();
        logger.log(Level.TRACE, _logMessage);

        List<String> _memberSmtpList = this.convertMembershipListFromCMMP(pCMMPExchangeLdapGroup);

        List<Name> _result = new ArrayList<>();

        for ( String _smtpAddress : _memberSmtpList ) {

            CMMPDILdapUser _user = this.cmmpdiLdapCacheService.getCMMPDIUserDnBySMTP(_smtpAddress);

            if ( _user != null ) {
                _result.add( _user.getRealDn() );
            }

            CMMPDILdapGroup _group = this.cmmpdiLdapCacheService.getCMMPDIGroupDnBySMTP(_smtpAddress);

            if ( _group != null ) {
                _result.add( _group.getRealDn() );
            }
        }

        return _result;
    }


    private String[] extractOus(String dnString) throws InvalidNameException {
        LdapName dn = new LdapName(dnString);
        List<String> ouList = new ArrayList<>();

        for (int i = dn.size() - 1; i >= 0; i--) {
            Rdn rdn = dn.getRdn(i);
            if ("OU".equalsIgnoreCase(rdn.getType())) {
                String value = rdn.getValue().toString();
                ouList.add("Groups".equalsIgnoreCase(value) ? "Group" : value);
            }
        }
        return ouList.toArray(new String[0]);
    }

    private String getLocalPart(String smtpAddress) {
        if (StringUtils.trimToNull(smtpAddress) != null) {
            try {
                InternetAddress address = new InternetAddress(smtpAddress);
                return address.getAddress().split("@")[0].toLowerCase();
            } catch (AddressException ae) {
                logger.error("Cannot find local part from '{}'", smtpAddress);
            }
        }
        return null;
    }


    public boolean createCMMPDIGroup(@NotNull CMMPExchangeLdapGroup pCMMPExchangeLdapGroup) throws InvalidNameException, DirSyncApplicationException {
        logEntering();

        LdapNameBuilder builder = LdapNameBuilder.newInstance(Constants.BASE_OU_WO_STARTING_COMMAND);

        String[] ous = extractOus(pCMMPExchangeLdapGroup.getDn().toString());

        for (int i = ous.length - 2; i >= 0; i--) {
            builder.add("ou", ous[i]);
        }

        String cn = getLocalPart(pCMMPExchangeLdapGroup.getMail().toLowerCase());
        Name dn = builder.add("cn", cn).build();

        String _logMessage = "Generate DN = " + dn.toString();
        logger.log(Level.DEBUG, _logMessage);

        // 2. Prepare the context with objectClasses and attributes
        DirContextAdapter context = new DirContextAdapter(dn);

        context.setAttributeValues("objectClass", new String[] {
                "top",
                "posixGroup",
                "group"
        });

        context.setAttributeValue(Constants.LDAP_FIELD_MAIL, pCMMPExchangeLdapGroup.getMail().toLowerCase());
        context.setAttributeValue(Constants.LDAP_FIELD_CN, pCMMPExchangeLdapGroup.getCn());
        context.setAttributeValue(Constants.LDAP_FIELD_DISTINGUISHEDNAME, pCMMPExchangeLdapGroup.getDistinguishedName());
        context.setAttributeValue(Constants.LDAP_FIELD_DISPLAY_NAME, pCMMPExchangeLdapGroup.getDisplayName());
        context.setAttributeValue(Constants.LDAP_FIELD_PROXY_ADDRESS, pCMMPExchangeLdapGroup.getProxyAddresses());

        // The expected format is DN of members in CMMP-DI
        List<String> _memberArray = this.convertMembershipListFromCMMP( pCMMPExchangeLdapGroup );

        for ( int i = 0; i < _memberArray.size(); i++ ) {
            context.addAttributeValue(Constants.LDAP_FIELD_MEMBER, _memberArray.get(i));
        }

        _logMessage = "Group context = " + context;
        logger.log(Level.DEBUG, _logMessage);

        return true;
    }


    private String[] getCMMPDIMemberByDN(List<String> newMemberList) {

        List<String> _result = new ArrayList<>();

        for (String memberDn : newMemberList) {
            String _buffer = memberDn.toString() + Constants.BASE_OU;
            _result.add(_buffer);
        }

        String[] array = _result.toArray(String[]::new);

        return array;
    }




    private boolean updateGroupMembership(String smtpAddress, List<String> newMemberList) {

        String _logMessage = Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName();
        logger.log(Level.TRACE, _logMessage);

        // Assume the group already exists
        CMMPDILdapGroup _group = this.cmmpdiLdapCacheService.getCMMPDIGroupDnBySMTP(smtpAddress);

        if ( _group != null ) {

            logger.debug( "Group SMTP = " + _group.getEmail());
            logger.debug( "Group DN = " + _group.getDn());
            logger.debug( "Before action, there are " + _group.getMembers().size() + " members in group.");

            for ( Name _name : _group.getMembers() ) {
                logger.debug(_name);
            }

            // Remove all members from the group
            for ( Name _name : _group.getMembers() ) {

                BasicAttribute attr2Remove = new BasicAttribute("member");
                attr2Remove.add(_name.toString());

                ModificationItem removeItem = new ModificationItem(
                        DirContext.REMOVE_ATTRIBUTE,
                        attr2Remove
                );

                logger.debug(removeItem);

                this.ldapTemplate.modifyAttributes(_group.getRealDn(), new ModificationItem[] { removeItem });
            }

            for (String memberDn : newMemberList) {

                BasicAttribute attr2Add = new BasicAttribute("member");
                attr2Add.add(memberDn.toString() + Constants.BASE_OU);

                ModificationItem addItem = new ModificationItem(
                        DirContext.ADD_ATTRIBUTE,
                        attr2Add
                );

                logger.debug(addItem);

                this.ldapTemplate.modifyAttributes(_group.getRealDn(), new ModificationItem[] { addItem });
            }
        }

        if ( logger.isDebugEnabled() ) {

            this.cmmpdiLdapCacheService.initCMMPDIBDO();

            // Assume the group already exists
            CMMPDILdapGroup _groupNewImage = this.cmmpdiLdapCacheService.getCMMPDIGroupDnBySMTP(smtpAddress);


            logger.debug( "After action, there are " + _groupNewImage.getMembers().size() + " members in group.");

            for ( Name _name : _groupNewImage.getMembers() ) {
                logger.debug(_name);
            }
        }

        // TODO
        return true;
    }


    /**
     * Assume the given SMTP address exists in the expected OU
     *
     * @param pCMMPExchangeLdapGroup
     * @param pCMMPDILdapGroup
     * @return
     */
    private boolean updateGroupAttributes(@NotNull CMMPExchangeLdapGroup pCMMPExchangeLdapGroup, @NotNull CMMPDILdapGroup pCMMPDILdapGroup) {

        logEntering();

        Name dn = LdapNameBuilder.newInstance(pCMMPDILdapGroup.getDn()).build();

        String _logMessage = "Going to update group attributes for "  + dn.toString() + "...";
        logger.log(Level.INFO, _logMessage);

        List<ModificationItem> _list = new ArrayList<>();

        if ( StringUtils.trimToEmpty( pCMMPExchangeLdapGroup.getCn() ).equals( pCMMPDILdapGroup.getGroupName() ) == false ) {
            _list.add(
                    new ModificationItem(
                            DirContext.REPLACE_ATTRIBUTE,
                            new BasicAttribute("cn", pCMMPExchangeLdapGroup.getCn())
                    )
            );
        }

//        if (ListCompare.equalsIgnoreOrder( pCMMPExchangeLdapGroup.getProxyAddresses(), pCMMPDILdapGroup.getProxyAddresses() ) == false ) {
//            _list.add(
//                    new ModificationItem(
//                            DirContext.REPLACE_ATTRIBUTE,
//                            new BasicAttribute("proxyAddresses", pCMMPExchangeLdapGroup.getProxyAddresses())
//                    )
//            );
//        }

        if ( !CollectionUtils.isEmpty(_list) ) {

            ModificationItem[] _mods = new ModificationItem[ _list.size() ];

            for ( int i = 0; i < _list.size(); i++ ) {
                _mods[i] = _list.get(i);
                logger.info(_mods[i].toString());
            }

            // Execute modification
            ldapTemplate.modifyAttributes(dn, _mods);
        }

        return true;
    }


}
