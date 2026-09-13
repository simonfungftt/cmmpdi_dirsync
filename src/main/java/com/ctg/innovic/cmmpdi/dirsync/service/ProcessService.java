package com.ctg.innovic.cmmpdi.dirsync.service;

import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeContainer;
import com.ctg.innovic.cmmpdi.dirsync.dto.CMMPExchangeLdapUser;
import com.ctg.innovic.cmmpdi.dirsync.exception.DirSyncApplicationException;
import com.ctg.innovic.cmmpdi.dirsync.utils.OldestFileFinderService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class ProcessService {

    private static Logger logger = LogManager.getLogger(ProcessService.class);

    private final ApplicationContext context;

    @Value("${input.user.inputFileDirectory}")
    private String inputFileDirectory;

    @Value("${input.user.processedFilePathDirectory}")
    private String processedFilePathDirectory;

    @Autowired
    private LdifReaderService ldifReaderService;

    @Autowired
    private CMMPDILdapQueryService ldapQueryService;

    public ProcessService(ApplicationContext context) {
        this.context = context;
    }


    public File readInputLdifFile() throws DirSyncApplicationException {

        logger.info("Scanning new input data files from CMMP under path " + inputFileDirectory);

        File _result = null;
        String _fileToProcess = null;

        try {

            OldestFileFinderService _oldestFileFinderService = new OldestFileFinderService();
            Optional<Path> _foundFile = _oldestFileFinderService.getOldestFile(inputFileDirectory, ".ldif");

            _fileToProcess = _foundFile.map(Path::getFileName).map(Path::toString).orElse(null);
        }
        catch (IOException ioe) {
            String _exceptionMessage = "[DSYNC-ERR001] Failed to access input file directory '" + inputFileDirectory + "'";
            logger.error(_exceptionMessage);
            throw new DirSyncApplicationException(_exceptionMessage);
        }

        if ( _fileToProcess == null ) {
            logger.info("No new file to process");
        }
        else {

            // Verify if file is exists and readable
            _result = new File(inputFileDirectory + "/" + _fileToProcess);

            if ( _result.exists() && _result.canRead() && _result.canWrite() ) {
                logger.info("Located next file to process is " + inputFileDirectory + "/" + _fileToProcess);
            }
            else {
                String _exceptionMessage = "[DSYNC-ERR002] Invalid access right for file " + _result.getAbsolutePath();
                logger.error(_exceptionMessage);
                throw new DirSyncApplicationException(_exceptionMessage);
            }
        }

        return _result;
    }


    public CMMPExchangeContainer importFileIntoMemory(@NotNull File pInputFile) throws DirSyncApplicationException {

        logger.info("Reading input data files from " + pInputFile.getAbsolutePath());

        CMMPExchangeContainer container = null;

        try {

			container = ldifReaderService.parseLdifFile(pInputFile.getAbsolutePath());

			logger.info("Loaded Users: " + container.getUsers().size());

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


    public void syncUpdateFromCMMPUser2DI(List<CMMPExchangeLdapUser> pUsers) {

        for ( CMMPExchangeLdapUser _eachUserInLdifFile : pUsers ) {

            if ( _eachUserInLdifFile.getMail().equals("smcheung@cheung.uat.cmmpdi") ||  _eachUserInLdifFile.getMail().startsWith("aa") ) {

                logger.info("Sync update from CMMP to DI on " + _eachUserInLdifFile.getMail() );

                ldapQueryService.createOrUpdateContactInCMMPDI(_eachUserInLdifFile);
            }
        }
    }


    public void doProcessing() {

        logger.info("Step 1. Processing input data files from CMMP...");

        String _fileToProcess = null;

        try {
            OldestFileFinderService _oldestFileFinderService = new OldestFileFinderService();
            Optional<Path> _foundFile = _oldestFileFinderService.getOldestFile(inputFileDirectory, "ldif");

            _fileToProcess = _foundFile.map(Path::getFileName).map(Path::toString).orElse(null);


        }
        catch (IOException ioe) {
            logger.error("[DSYNC-ERR001] Failed to access input file directory '" + inputFileDirectory + "'");
        }

        if ( _fileToProcess != null ) {
            logger.info("Going to process file '" + _fileToProcess);


            logger.info("Step 2. Reading the file into memory");

            List<CMMPExchangeLdapUser> _userObjectsInMemory = new ArrayList<>();

            try {

                LdifReaderService _LdifReaderService = new LdifReaderService();
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
    }
}
