package com.ctg.innovic.cmmpdi.dirsync.service;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapUser;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.exception.DirSyncApplicationException;
import com.ctg.innovic.cmmpdi.dirsync.utils.Constants;
import com.ctg.innovic.cmmpdi.dirsync.utils.ListCompare;
import com.ctg.innovic.cmmpdi.dirsync.utils.LogUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class CMMPDILdapGroupService {

    private static Logger logger = LogManager.getLogger(CMMPDILdapGroupService.class);

    @Autowired
    private CMMPDILdapCacheService CMMPDILdapCacheService;

    @Autowired
    @Qualifier("cmmpdiLdapTemplate")
    private LdapTemplate ldapTemplate;

    @Autowired
    private CMMPDILdapQueryService cmmpdiLdapQueryService;

    public boolean createOrUpdateCMMPDIGroup(CMMPExchangeLdapGroup pCMMPExchangeLdapGroup) throws InvalidNameException {

        boolean _result = false;

        logger.debug(Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );

        // Determine create or update case
        CMMPDILdapUser _user = this.CMMPDILdapCacheService.getCMMPDIUserDnBySMTP(pCMMPExchangeLdapGroup.getMail());
        CMMPDILdapGroup _group = this.CMMPDILdapCacheService.getCMMPDIGroupDnBySMTP(pCMMPExchangeLdapGroup.getMail());

        if ( _user == null && _group == null ) {

            logger.info("Going to create group " + pCMMPExchangeLdapGroup.getDn()
                    + "with SMTP address " + pCMMPExchangeLdapGroup.getMail());

            _result = this.createCMMPDIGroup(pCMMPExchangeLdapGroup);
        }
        else if ( _user == null && _group != null ) {

            logger.info("Going to remove object " + _group.getRealDn()
                    + " due to SMTP address " + pCMMPExchangeLdapGroup.getMail()
                    + " conflict with users...");

            this.cmmpdiLdapQueryService.deleteCMMPDIObject(_group.getEmail());

            logger.info("Creating new contact object as below...");

            _result = this.createCMMPDIGroup(pCMMPExchangeLdapGroup);
        }
        else if ( _group != null ) {
            // Update case
            this.updateGroupAttributes(pCMMPExchangeLdapGroup, _group);
            _result = this.updateGroupMembership(pCMMPExchangeLdapGroup.getMail(), pCMMPExchangeLdapGroup.getMemberDNs());
        }



        return true;
    }


//    public boolean syncGroupFromCMMP2DI(CMMPExchangeLdapGroup pCMMPExchangeLdapGroup) throws DirSyncApplicationException {
//
//        logger.debug("Entering " + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );
//
//        String _targetEmail = StringUtils.trimToNull( pCMMPExchangeLdapGroup.getMail() );
//
//        if ( _targetEmail == null ) {
//            throw new DirSyncApplicationException("[DIREX] Unexpected SMTP address is null");
//        }
//
//        // Check if the DN exists in memory
////        String _dn = this.CMMPDILdapCacheService.getCMMPDIDnBySMTP( _targetEmail );
//
//        if ( this.CMMPDILdapCacheService.getCMMPDIGroupDnBySMTP( _targetEmail ) != null ) {
//
//            // Update group
//        }
//        else if ( this.CMMPDILdapCacheService.getCMMPDIUserDnBySMTP( _targetEmail ) != null ) {
//
//            // remove user (only if not CMMPDI mailbox)
//            // then create it
//        }
//        else {
//
//            // Does not exists, create it
//        }
//
////        return null;
//        return false;
//    }


    private List<String> convertMembershipListFromCMMP(CMMPExchangeLdapGroup pCMMPExchangeLdapGroup) throws DirSyncApplicationException {

        List<String> _memberSmtpList = new ArrayList<>();

        // Assume this is DN from source system
        // A transformation is required to convert DN into SMTP address
        for ( String _eachMember : pCMMPExchangeLdapGroup.getMemberDNs() ) {
            _memberSmtpList.add(_eachMember);
        }

        return _memberSmtpList;
    }


    public List<Name> getMemberListBasedOnCMMPOfDI(CMMPExchangeLdapGroup pCMMPExchangeLdapGroup) throws DirSyncApplicationException {

        logger.debug("Entering " + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );

        List<String> _memberSmtpList = this.convertMembershipListFromCMMP(pCMMPExchangeLdapGroup);

        List<Name> _result = new ArrayList<>();

        for ( String _smtpAddress : _memberSmtpList ) {

            CMMPDILdapUser _user = this.CMMPDILdapCacheService.getCMMPDIUserDnBySMTP(_smtpAddress);

            if ( _user != null ) {
                _result.add( _user.getRealDn() );
            }

            CMMPDILdapGroup _group = this.CMMPDILdapCacheService.getCMMPDIGroupDnBySMTP(_smtpAddress);

            if ( _group != null ) {
                _result.add( _group.getRealDn() );
            }
        }

        return _result;
    }


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



    public boolean createCMMPDIGroup(CMMPExchangeLdapGroup pCMMPExchangeLdapGroup) throws InvalidNameException {

        CMMPDILdapGroup _cmmpdiLdapGroup = new CMMPDILdapGroup();

        logger.debug(Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );

        LdapNameBuilder builder = LdapNameBuilder.newInstance(Constants.BASE_OU_WO_STARTING_COMMAND);

        // Assume the LDAP user does not exist
        String[] _array = extractOus( pCMMPExchangeLdapGroup.getDn() );

        for ( short i = 0; i < _array.length; i++ ) {
            builder.add("ou", _array[i]);
        }

        builder.add("cn", pCMMPExchangeLdapGroup.getDisplayName().toLowerCase());
        Name dn = builder.build();

        logger.debug("Generate DN = " + dn.toString());

        // 2. Prepare the context with objectClasses and attributes
        DirContextAdapter context = new DirContextAdapter(dn);

        context.setAttributeValues("objectClass", new String[] {
                "top",
                "posixGroup",
                "group"
        });

        context.setAttributeValue("mail", pCMMPExchangeLdapGroup.getMail());
        context.setAttributeValue("cn", pCMMPExchangeLdapGroup.getCn());
        context.setAttributeValue("distinguishedName", pCMMPExchangeLdapGroup.getDistinguishedName());

        String[] _memberArray = this.getCMMPDIMemberByDN( pCMMPExchangeLdapGroup.getMemberDNs() );

        for ( int i = 0; i < _memberArray.length; i++ ) {
            context.addAttributeValue("member", _memberArray[ i ]);
        }

        logger.info("context = " + context);

        return true;
    }


    private String[] getCMMPDIMemberByDN(List<String> newMemberList) {

        List<String> _result = new ArrayList<>();

        for (String memberDn : newMemberList) {
            _result.add(memberDn.toString() + Constants.BASE_OU);
        }

        String[] array = _result.toArray(String[]::new);

        return array;
    }



    private boolean updateGroupMembership(String smtpAddress, List<String> newMemberList) {

        logger.debug("Entering " + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );

        // Assume the group already exists
        CMMPDILdapGroup _group = this.CMMPDILdapCacheService.getCMMPDIGroupDnBySMTP(smtpAddress);

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

            this.CMMPDILdapCacheService.initCMMPDI();

            // Assume the group already exists
            CMMPDILdapGroup _groupNewImage = this.CMMPDILdapCacheService.getCMMPDIGroupDnBySMTP(smtpAddress);

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
    private boolean updateGroupAttributes(CMMPExchangeLdapGroup pCMMPExchangeLdapGroup, CMMPDILdapGroup pCMMPDILdapGroup) {

        logger.trace("Entering " + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );

        Name dn = LdapNameBuilder.newInstance(pCMMPDILdapGroup.getDn()).build();

        logger.info("Going to update group attributes for "  + dn.toString() + "...");

        List<ModificationItem> _list = new ArrayList<>();

        if ( StringUtils.trimToEmpty( pCMMPExchangeLdapGroup.getCn() ).equals( pCMMPDILdapGroup.getGroupName() ) == false ) {
            _list.add(
                    new ModificationItem(
                            DirContext.REPLACE_ATTRIBUTE,
                            new BasicAttribute("cn", pCMMPExchangeLdapGroup.getCn())
                    )
            );
        }

        if (ListCompare.equalsIgnoreOrder( pCMMPExchangeLdapGroup.getProxyAddresses(), pCMMPDILdapGroup.getProxyAddresses() ) == false ) {
            _list.add(
                    new ModificationItem(
                            DirContext.REPLACE_ATTRIBUTE,
                            new BasicAttribute("proxyAddresses", pCMMPExchangeLdapGroup.getProxyAddresses())
                    )
            );
        }

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
