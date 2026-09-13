package com.ctg.innovic.cmmpdi.dirsync.service;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapUser;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.exception.DirSyncApplicationException;
import com.ctg.innovic.cmmpdi.dirsync.utils.Constants;
import com.ctg.innovic.cmmpdi.dirsync.utils.LogUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;
import org.springframework.stereotype.Service;

import javax.naming.Name;
import javax.naming.directory.ModificationItem;
import java.util.ArrayList;
import java.util.List;

@Service
public class CMMPDILdapGroupService {

    private static Logger logger = LogManager.getLogger(CMMPDILdapGroupService.class);

    @Autowired
    private LdapCacheService ldapCacheService;

    @Autowired
    private LdapTemplate ldapTemplate;


    public boolean syncGroupFromCMMP2DI(CMMPExchangeLdapGroup pCMMPExchangeLdapGroup) throws DirSyncApplicationException {

        logger.debug("Entering " + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );

        String _targetEmail = StringUtils.trimToNull( pCMMPExchangeLdapGroup.getEmail() );

        if ( _targetEmail == null ) {
            throw new DirSyncApplicationException("[DIREX] Unexpected SMTP address is null");
        }

        // Check if the DN exists in memory
//        String _dn = this.ldapCacheService.getCMMPDIDnBySMTP( _targetEmail );

        if ( this.ldapCacheService.getCMMPDIGroupDnBySMTP( _targetEmail ) != null ) {

            // Update group
        }
        else if ( this.ldapCacheService.getCMMPDIUserDnBySMTP( _targetEmail ) != null ) {

            // remove user (only if not CMMPDI mailbox)
            // then create it
        }
        else {

            // Does not exists, create it
        }

//        return null;
        return false;
    }


    private List<String> convertMembershipListFromCMMP(CMMPExchangeLdapGroup pCMMPExchangeLdapGroup) throws DirSyncApplicationException {

        List<String> _memberSmtpList = new ArrayList<>();

        // Assume this is DN from source system
        // A transformation is required to convert DN into SMTP address
        for ( String _eachMember : pCMMPExchangeLdapGroup.getMemberDNs() ) {

            String _email = this.ldapCacheService.translateCMMPDnIntoSMTP(_eachMember);

            if ( _email != null ) {
                _memberSmtpList.add(_email);
            }
            else {
                throw new DirSyncApplicationException("[" + Constants.ERRORCODE_PREFIX + "004] cannot find SMTP address from DN '" + _eachMember + "'");
            }
        }

        return _memberSmtpList;
    }


    public List<Name> getMemberListBasedOnCMMPOfDI(CMMPExchangeLdapGroup pCMMPExchangeLdapGroup) throws DirSyncApplicationException {

        logger.debug("Entering " + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName() );

        List<String> _memberSmtpList = this.convertMembershipListFromCMMP(pCMMPExchangeLdapGroup);

        List<Name> _result = new ArrayList<>();

        for ( String _smtpAddress : _memberSmtpList ) {

            CMMPDILdapUser _user = this.ldapCacheService.getCMMPDIUserDnBySMTP(_smtpAddress);

            if ( _user != null ) {
                _result.add( _user.getRealDn() );
            }

            CMMPDILdapGroup _group = this.ldapCacheService.getCMMPDIGroupDnBySMTP(_smtpAddress);

            if ( _user != null ) {
                _result.add( _group.getRealDn() );
            }
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


}
