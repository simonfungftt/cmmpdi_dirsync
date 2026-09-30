package com.ctg.innovic.cmmpdi.dirsync.service;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeContainer;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapUser;
import com.ctg.innovic.cmmpdi.dirsync.exception.DirSyncApplicationException;
import com.ctg.innovic.cmmpdi.dirsync.utils.Constants;
import com.ctg.innovic.cmmpdi.dirsync.utils.DataSyncManager;
import com.ctg.innovic.cmmpdi.dirsync.utils.LogUtils;
import com.ctg.innovic.cmmpdi.dirsync.utils.OldestFileFinderUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.naming.InvalidNameException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProcessService {

    private static Logger logger = LogManager.getLogger(ProcessService.class);

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
    private CMMPCacheService cmmpCacheService;

    @Autowired
    private CMMPLdifReaderService cmmpLdifReaderService;

    @Autowired
    private CMMPDILdapQueryService cmmpdiLdapQueryService;

    @Autowired
    private CMMPDILdapCacheService cmmpdiLdapCacheService;

    @Autowired
    private CMMPDILdapUserService cmmpdiLdapUserService;

    @Autowired
    private CMMPDILdapGroupService cmmpdiLdapGroupService;

    @Autowired
    private CMMPDILdapWGDService cmmpdiLdapWGDService;

    @Autowired
    private DataSyncManager dataSyncManager;

    /**
     * To effective resolve membership (original in CN form), need all the CN of users to build up members
     *
     * @throws IOException
     */
    public void initCMMPCache() throws IOException {

        CMMPExchangeContainer _userContainer = cmmpLdifReaderService.parseLdifFile(pathOfAllUserFilePath);

        logger.info( _userContainer.getUsers().size() + " users had been initialised from all contacts file.");

        for ( CMMPExchangeLdapUser _user : _userContainer.getUsers() ) {
            cmmpCacheService.addCache( _user.getDn(), _user.getMail() );
        }

        CMMPExchangeContainer _groupContainer = cmmpLdifReaderService.parseLdifFile(pathOfAllGroupFilePath);

        logger.info( _groupContainer.getGroups().size() + " groups had been initialised from all contacts file.");

        for ( CMMPExchangeLdapGroup group : _groupContainer.getGroups() ) {
            cmmpCacheService.addCache( group.getDn(), group.getMail() );
        }
    }


    public void initCMMPDICache() throws IOException {

        this.cmmpdiLdapCacheService.initCMMPDI();
        this.cmmpdiLdapCacheService.initCMMPDIWGD();
    }


    public void fullSyncCMMP2CMMPDI() {

    }

    public void deltaSyncCMMP2CMMPDI() {

        // 1. Read last data sync time
        Instant _lastDataSyncTime = dataSyncManager.getLastSyncTime();

        // Foreach OU in CMMP, get users modified last since last data sync time

        // Fetch for the list

        // Do the create / update

        dataSyncManager.saveLastSyncTime(Instant.now());
    }


    public void processGroupInputData() throws DirSyncApplicationException, IOException, InvalidNameException {

        List<File> _files =  fetchInputDataFromFolder(groupInputFileDirectory);

        for ( File _file : _files ) {

            logger.info("Processing CMMP group info data file '" + _file.getAbsolutePath() + "'");

            CMMPExchangeContainer _container = this.importFileIntoMemory( _file );

            if ( logger.isDebugEnabled() ) {

                Gson gson = new GsonBuilder()
                        .disableHtmlEscaping()
                        .setPrettyPrinting()
                        .create();
            }

            for (CMMPExchangeLdapGroup _group : _container.getGroups() ) {
                this.cmmpdiLdapGroupService.createOrUpdateCMMPDIGroup(_group);
            }

//            moveProcessedFileToDest(_file, new File(this.groupProcessedFilePathDirectory + "/" + _file.getName()));
        }
    }


    public void processContactInputData() throws DirSyncApplicationException, IOException, InvalidNameException {

        // Fetch files to be processed from input file path
        List<File> _files =  fetchInputDataFromFolder(this.contactInputFileDirectory);

        // For each file
        for ( File _file : _files ) {

            logger.info("Processing CMMP contact info data file '" + _file.getAbsolutePath() + "'");

            // Parse the input file
            CMMPExchangeContainer _container = this.importFileIntoMemory( _file );

            int _processUserCount = 1;


            for (CMMPExchangeLdapUser _exUser : _container.getUsers() ) {

                // Testing code
                if ( _exUser.getMail().equalsIgnoreCase("ltkwokub@uatbdoa.gov.hk") ) {
                    this.cmmpdiLdapUserService.createOrUpdateCMMPDIUser(_exUser);
                }

                if ( _processUserCount % 100 == 0 )
                    logger.info("Process user count: " + _processUserCount);

                _processUserCount++;
            }

            logger.info("Process user count: " + _processUserCount);

            moveProcessedFileToDest(_file, new File(this.contactProcessedFilePathDirectory + "/" + _file.getName()));
        }
    }


    public void consolidateWGDUser() throws InvalidNameException, IOException {

        logger.log(Level.TRACE, Constants.LOGGING_ENTERING + LogUtils.getCurrentClassName() + "." + LogUtils.getCurrentMethodName());

        this.initCMMPDICache();

        cmmpdiLdapWGDService.syncWGDUsers();
        cmmpdiLdapWGDService.syncWGDGroups();
    }


    /**
     * Move processed file to destination
     *
     * @param pSource
     * @return
     */
    private boolean moveProcessedFileToDest(File pSource, File pDest) {

        Path _source = null;
        Path _dest = null;

        try {

            // Move the file to processed folder
            _source = Paths.get(pSource.getAbsolutePath());
            _dest = Paths.get(pDest.getAbsolutePath());

            if ( _source != null && _dest != null ) {
                logger.info("Moving file " + _source.toAbsolutePath() + " to " + _dest.toAbsolutePath());
                Files.move(_source, _dest, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch(IOException ioe) {
            logger.error("Cannot move " + _source.toAbsolutePath() + " to " + _dest.toAbsolutePath() + ", " + ioe.getMessage());
            return false;
        }

        return true;
    }


    /**
     * Scan for pending processing contact information file from CMMP
     *
     * @return
     * @throws DirSyncApplicationException
     */
    private List<File> fetchInputDataFromFolder(String pScanningFolder) throws DirSyncApplicationException {

        List<File> _contactInputFile = new ArrayList<>();

        int _processedFileCount = 0;

		//while ( _processedFileCount < 10 ) {

			File _nextFileToProcess = this.readNextInputLdifFile(pScanningFolder);

            if ( _nextFileToProcess == null ) {
                logger.info("No pending file is found in path '" + pScanningFolder + "'");
                //break;
            }
            else {
                logger.info("Located file '" + _nextFileToProcess.getAbsolutePath() + "'");

                _contactInputFile.add(_nextFileToProcess);
            }

			_processedFileCount++;
		//}

        return _contactInputFile;
    }


    /**
     * Get next available file in the path to process
     *
     * @param pScanningDir
     * @return
     * @throws DirSyncApplicationException
     */
    private File readNextInputLdifFile(String pScanningDir) throws DirSyncApplicationException {

        logger.debug("Scanning new input data files from CMMP under path " + pScanningDir);

        File _result = null;
        String _fileToProcess = null;

        try {

            OldestFileFinderUtils _oldestFileFinderUtils = new OldestFileFinderUtils();
            Optional<Path> _foundFile = _oldestFileFinderUtils.getOldestFile(pScanningDir, ".ldif");

            _fileToProcess = _foundFile.map(Path::getFileName).map(Path::toString).orElse(null);
        }
        catch (IOException ioe) {
            String _exceptionMessage = "[DSYNC-ERR001] Failed to access input file directory '" + pScanningDir + "'";
            logger.error(_exceptionMessage);
            throw new DirSyncApplicationException(_exceptionMessage);
        }

        if ( _fileToProcess == null ) {
            logger.debug("No new file to process");
        }
        else {

            // Verify if file is exists and readable
            _result = new File(pScanningDir + "/" + _fileToProcess);

            if ( _result.exists() && _result.canRead() && _result.canWrite() ) {
                logger.debug("Located next file to process is " + pScanningDir + "/" + _fileToProcess);
            }
            else {
                String _exceptionMessage = "[DSYNC-ERR002] Invalid access right for file " + _result.getAbsolutePath();
                logger.error(_exceptionMessage);
                throw new DirSyncApplicationException(_exceptionMessage);
            }
        }

        return _result;
    }


    /**
     * Import input file into memory
     *
     * @param pInputFile
     * @return
     * @throws DirSyncApplicationException
     */
    private CMMPExchangeContainer importFileIntoMemory(@NotNull File pInputFile) throws DirSyncApplicationException {

        logger.info("Reading input data files from " + pInputFile.getAbsolutePath());

        CMMPExchangeContainer container = null;

        try {

			container = cmmpLdifReaderService.parseLdifFile(pInputFile.getAbsolutePath());

			logger.info("Loaded Users: " + container.getUsers().size());
            logger.info("Loaded Groups: " + container.getGroups().size());

            if ( logger.isTraceEnabled() ) {

                Gson gson = new GsonBuilder()
                        .disableHtmlEscaping()
                        .setPrettyPrinting() // Optional: formats output nicely
                        .create();
                for (CMMPExchangeLdapUser _exUser : container.getUsers()) {
                    logger.trace(gson.toJson(_exUser));
                }
            }
		}
        catch (IOException e) {
            String _exceptionMessage = "[DSYNC-ERR003] Exception in process file '" + pInputFile.getAbsolutePath() + "'";
            logger.error(_exceptionMessage);
            throw new DirSyncApplicationException(_exceptionMessage);
		}

        return container;
    }

}
