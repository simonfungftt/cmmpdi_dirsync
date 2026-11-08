package com.ctg.innovic.cmmpdi.dirsync.service.impl;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeContainer;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapUser;
import com.ctg.innovic.cmmpdi.dirsync.service.CMMPLdapService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CMMPLdapFileService implements CMMPLdapService {

    private final static Logger logger = LogManager.getLogger(CMMPLdapFileService.class);

    @Value("${input.user.inputFileDirectory}")
    private String contactInputFileDirectory;

    @Value("${input.user.processedFilePathDirectory}")
    private String contactProcessedFilePathDirectory;

    @Value("${input.group.inputFileDirectory}")
    private String groupInputFileDirectory;

    @Value("${input.group.processedFilePathDirectory}")
    private String groupProcessedFilePathDirectory;

    @Value("${path.of.all.user.file.path}")
    private String pathOfAllUserFilePath;

    @Value("${path.of.all.group.file.path}")
    private String pathOfAllGroupFilePath;

    @Autowired
    private CMMPLdifReaderService cmmpLdifReaderService;

    @Autowired
    private CMMPCacheService cmmpCacheService;

    public void initCMMPCache() throws IOException {

        CMMPExchangeContainer _userContainer = cmmpLdifReaderService.parseLdifFile(pathOfAllUserFilePath);

        logger.log(Level.INFO, String.format( "%5d users had been initialised from all contacts file.", _userContainer.getUsers().size() ) );

        for ( CMMPExchangeLdapUser _user : _userContainer.getUsers() ) {
            cmmpCacheService.addCache( _user.getDn(), _user.getMail() );
            cmmpCacheService.addUserCache(_user);
        }

        CMMPExchangeContainer _groupContainer = cmmpLdifReaderService.parseLdifFile(pathOfAllGroupFilePath);

        logger.log(Level.INFO, String.format( "%5d groups had been initialised from all contacts file.", _groupContainer.getUsers().size() ) );

        for ( CMMPExchangeLdapGroup _group : _groupContainer.getGroups() ) {
            cmmpCacheService.addCache( _group.getDn(), _group.getMail() );
            cmmpCacheService.addGroupCache(_group);
        }
    }

    @Override
    public CMMPExchangeLdapGroup dipGroupBySMTP(String pSMTP) {
        return this.cmmpCacheService.getGroupFromCacheBySMTP(pSMTP);
    }

    @Override
    public CMMPExchangeLdapUser dipUserBySMTP(String pSMTP) {
        return this.cmmpCacheService.getUserFromCacheBySMTP(pSMTP);
    }

    @Override
    public List<CMMPExchangeLdapGroup> getLastModifiedGroupUnderOU(String pOU, Instant pLastSyncTime) {

        List<CMMPExchangeLdapGroup> _buffer
                = this.cmmpCacheService.getCmmpSmtp2GroupMap().values().stream().collect(Collectors.toCollection(ArrayList::new));

        List <CMMPExchangeLdapGroup> _filtered = new ArrayList<>();

        for ( CMMPExchangeLdapGroup _group : _buffer ) {

            if ( _group.getDn().toLowerCase().contains( pOU.toLowerCase() ) && _group.getWhenChanged().isBefore(pLastSyncTime) ) {
                _filtered.add(_group);
            }
        }

        return _filtered;
    }

    @Override
    public List<CMMPExchangeLdapGroup> getAllGroupUnderOU(String pOU) {
        return this.getLastModifiedGroupUnderOU(pOU, Instant.EPOCH);
    }

    @Override
    public List<CMMPExchangeLdapUser> getLastModifiedUserUnderOU(String pOU, Instant pLastSyncTime) {

        List<CMMPExchangeLdapUser> _buffer
                = this.cmmpCacheService.getCmmpSmtp2UserMap().values().stream().collect(Collectors.toCollection(ArrayList::new));

        List <CMMPExchangeLdapUser> _filtered = new ArrayList<>();

        for ( CMMPExchangeLdapUser _user : _buffer ) {

            if ( _user.getDn().toLowerCase().contains( pOU.toLowerCase() ) && _user.getWhenChanged().isBefore(pLastSyncTime) ) {
                _filtered.add(_user);
            }
        }

        return _filtered;

    }

    @Override
    public List<CMMPExchangeLdapUser> getAllUserUnderOU(String pOU) {
        return this.getLastModifiedUserUnderOU(pOU, Instant.EPOCH);
    }

}
