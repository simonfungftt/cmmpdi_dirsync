package com.ctg.innovic.cmmpdi.dirsync;

import com.ctg.innovic.cmmpdi.dirsync.config.ApplicationConfig;
import com.ctg.innovic.cmmpdi.dirsync.config.PropertiesCmmpSync;
import com.ctg.innovic.cmmpdi.dirsync.service.CMMPLdapQueryService;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

@Component
public class ConsolidateWGDRunner implements ApplicationRunner {

    private static Logger logger = LogManager.getLogger(ConsolidateWGDRunner.class);

    @Autowired
    private PropertiesCmmpSync propertiesCmmpSync;

    @Override
    public void run(ApplicationArguments args) {

        //if ( args.containsOption("wgd") ) {

            logger.log(Level.INFO, "Processing WGD consolidation process...");

            for (String _ou : propertiesCmmpSync.getSourceBases() ) {
                logger.log(Level.INFO, String.format("ou=%s", _ou));
//                cmmpLdapQueryService.
            }
        //}
    }

}
