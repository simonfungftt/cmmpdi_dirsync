package com.ctg.innovic.cmmpdi.dirsync.service;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPDILdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeContainer;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapGroup;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapUser;
import com.ctg.innovic.cmmpdi.dirsync.exception.DirSyncApplicationException;
import com.ctg.innovic.cmmpdi.dirsync.utils.OldestFileFinderUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

    public void initCMMPCache() throws IOException {

        CMMPExchangeContainer _userContainer = cmmpLdifReaderService.parseLdifFile(pathOfAllUserFilePath);

        logger.info( _userContainer.getUsers().size() + " users had been initialised from all contacts file.");

        for ( CMMPExchangeLdapUser _user : _userContainer.getUsers() ) {
            cmmpCacheService.addCache( _user.getDn(), _user.getMail() );

            if ( _user.getMail().equals("cs6admin@uat.cmmp.gov.hk") ) {
                Gson gson = new GsonBuilder()
                        .disableHtmlEscaping()
                        .setPrettyPrinting()
                        .create();
//                logger.info( gson.toJson(_user) );
            }
        }

        CMMPExchangeContainer _groupContainer = cmmpLdifReaderService.parseLdifFile(pathOfAllGroupFilePath);

        logger.info( _groupContainer.getGroups().size() + " groups had been initialised from all contacts file.");

        for ( CMMPExchangeLdapGroup group : _groupContainer.getGroups() ) {
            cmmpCacheService.addCache( group.getDn(), group.getMail() );
        }

        _userContainer = null;
        _groupContainer = null;

        System.gc();
    }


    public void initCMMPDICache() throws IOException {

        this.cmmpdiLdapCacheService.initCMMPDI();
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

        List<File> _files =  fetchInputDataFromFolder(this.contactInputFileDirectory);

        for ( File _file : _files ) {

            logger.info("Processing CMMP contact info data file '" + _file.getAbsolutePath() + "'");

            CMMPExchangeContainer _container = this.importFileIntoMemory( _file );

            int _processUserCount = 1;


                Gson gson = new GsonBuilder()
					.disableHtmlEscaping()
					.setPrettyPrinting()
					.create();

			    for (CMMPExchangeLdapUser _exUser : _container.getUsers() ) {
//				    logger.debug( gson.toJson(_exUser) );

                    if ( _exUser.getMail().equalsIgnoreCase("ltkwokub@uatbdoa.gov.hk") ) {
                        this.cmmpdiLdapUserService.createOrUpdateCMMPDIUser(_exUser);
                    }

                    if ( _processUserCount % 100 == 0 )
                        logger.info("Process user count: " + _processUserCount);

                    _processUserCount++;
			    }

            logger.info("Process user count: " + _processUserCount);

//            moveProcessedFileToDest(_file, new File(this.contactProcessedFilePathDirectory + "/" + _file.getName()));
        }
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

/*
    public void syncUpdateFromCMMPUser2DI(List<CMMPExchangeLdapUser> pUsers) {

        for ( CMMPExchangeLdapUser _eachUserInLdifFile : pUsers ) {

            if ( _eachUserInLdifFile.getMail().equals("smcheung@cheung.uat.cmmpdi") ||  _eachUserInLdifFile.getMail().startsWith("aa") ) {

                logger.info("Sync update from CMMP to DI on " + _eachUserInLdifFile.getMail() );

                ldapQueryService.createOrUpdateContactInCMMPDI(_eachUserInLdifFile);
            }
        }
    }
*/
/*
    public void doProcessing() {

        logger.info("Step 1. Processing input data files from CMMP...");

        String _fileToProcess = null;

        try {
            OldestFileFinderUtils _oldestFileFinderService = new OldestFileFinderUtils();
            Optional<Path> _foundFile = _oldestFileFinderService.getOldestFile(this.contactInputFileDirectory, "ldif");

            _fileToProcess = _foundFile.map(Path::getFileName).map(Path::toString).orElse(null);


        }
        catch (IOException ioe) {
            logger.error("[DSYNC-ERR001] Failed to access input file directory '" + this.contactInputFileDirectory + "'");
        }

        if ( _fileToProcess != null ) {
            logger.info("Going to process file '" + _fileToProcess);


            logger.info("Step 2. Reading the file into memory");

            List<CMMPExchangeLdapUser> _userObjectsInMemory = new ArrayList<>();

            try {

                CMMPLdifReaderService _LdifReaderService = new CMMPLdifReaderService();
                CMMPExchangeContainer container
                        = _LdifReaderService.parseLdifFile(_fileToProcess);

                logger.info("File statistic, users: " + container.getUsers().size());

                _userObjectsInMemory.addAll(container.getUsers());

                if ( logger.isDebugEnabled() ) {
                    Gson gson = new Gson();
                    for (CMMPExchangeLdapUser _exUser : container.getUsers()) {
                        logger.debug(gson.toJson(_exUser));
                    }
                }
            }
            catch (IOException e) {
                logger.error("ABC");
            }

            logger.info("Step 3. Transformation from CMMP to CMMP-DI");


            logger.info("Step 4. Write into CMMP-DI");

        }
    }*/
}
